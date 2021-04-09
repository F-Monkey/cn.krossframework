package cn.krossframework.state;

import cn.krossframework.commons.thread.AutoTask;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractWorkerManager implements WorkerManager, Lock {

    private static final Logger log = LoggerFactory.getLogger(AbstractWorkerManager.class);

    private static final AtomicLong ID_COUNT = new AtomicLong(0);

    protected final int workerUpdatePeriod;

    protected final int removeDeposedStateGroupPeriod;

    protected final int workerCapacity;

    protected final int taskDispatcherSize;

    protected final ExecutorService executorService;

    protected final StateGroupPool stateGroupPool;

    protected final Object LOCK;

    protected final ConcurrentHashMap<Long, TaskDispatcher> dispatcherMap;

    protected volatile ConcurrentHashMap<Long, StateGroupWorker> workerMap;

    /**
     * @param workerUpdatePeriod            worker的刷新频率
     * @param workerCapacity                worker内的stateGroup数量
     * @param workerThreadSize              worker的数量
     * @param removeEmptyWorkerPeriod       移除worker的频率
     * @param removeDeposedStateGroupPeriod 移除worker内的失效的stateGroup标记
     * @param taskDispatcherSize            taskDispatcher的数量
     * @param stateGroupPool                stateGroup池
     */
    public AbstractWorkerManager(int workerUpdatePeriod,
                                 int workerCapacity,
                                 int workerThreadSize,
                                 int removeEmptyWorkerPeriod,
                                 int removeDeposedStateGroupPeriod,
                                 int taskDispatcherSize,
                                 StateGroupPool stateGroupPool) {
        this.workerUpdatePeriod = workerUpdatePeriod;
        this.removeDeposedStateGroupPeriod = removeDeposedStateGroupPeriod;
        this.taskDispatcherSize = taskDispatcherSize;
        this.stateGroupPool = stateGroupPool;
        this.workerCapacity = workerCapacity;
        this.LOCK = new Object();
        this.executorService = new ThreadPoolExecutor(workerThreadSize + taskDispatcherSize,
                (workerThreadSize + taskDispatcherSize) * 2,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        this.workerMap = this.initWorkerMap();
        this.dispatcherMap = this.initDispatcherMap();
        new AutoTask(removeEmptyWorkerPeriod, 2) {
            @Override
            protected void run() {
                AbstractWorkerManager.this.removeEmptyWorker();
            }
        }.start();
    }

    protected ConcurrentHashMap<Long, TaskDispatcher> initDispatcherMap() {
        return new ConcurrentHashMap<>(this.taskDispatcherSize);
    }

    protected ConcurrentHashMap<Long, StateGroupWorker> initWorkerMap() {
        return new ConcurrentHashMap<>();
    }

    public boolean tryLock() {
        synchronized (this.LOCK) {
            try {
                this.LOCK.wait();
                return true;
            } catch (InterruptedException ignore) {
                return false;
            }
        }
    }

    public void unlock() {
        synchronized (this.LOCK) {
            this.LOCK.notifyAll();
        }
    }

    public static class EnterGroupTask implements Task {

        private final Runnable runnable;

        public EnterGroupTask(Runnable runnable) {
            Preconditions.checkNotNull(runnable);
            this.runnable = runnable;
        }

        public void run() {
            this.runnable.run();
        }
    }

    protected void removeEmptyWorker() {
        final ConcurrentHashMap<Long, StateGroupWorker> workerMap = this.workerMap;
        workerMap.entrySet().removeIf(e -> {
            Worker worker = e.getValue();
            boolean empty = worker.isEmpty();
            if (empty) {
                worker.stop();
                log.info("remove empty worker, id: {}", e.getKey());
            }
            return empty;
        });
        this.workerMap = workerMap;
    }

    protected void dispatcherTask(Task task) {
        Long groupId = null;
        FailCallBack callBack = null;
        if (task instanceof GroupIdTask) {
            GroupIdTask groupIdTask = (GroupIdTask) task;
            groupId = groupIdTask.getGroupId();
            callBack = groupIdTask.getFailCallBack();
        }

        TaskDispatcher dispatcher = this.findDispatcher(groupId);
        if (!dispatcher.tryAddTask(task)) {
            if (callBack != null) {
                callBack.call();
            }
        }
    }

    protected TaskDispatcher findDispatcher(Long groupId) {
        long index = (groupId == null ? 0 : groupId) % this.taskDispatcherSize;
        return this.dispatcherMap.computeIfAbsent(index, (i) -> {
            TaskDispatcher taskDispatcher = new AbstractTaskDispatcher(i, this.executorService, this.stateGroupPool) {
            };
            taskDispatcher.start();
            return taskDispatcher;
        });
    }

    protected void findBestGroup2Enter(Long groupId, Task task, FailCallBack failCallBack) {
        if (groupId == null) {
            for (StateGroupWorker worker : this.workerMap.values()) {
                Iterator<Long> idIterator = worker.stateGroupIdIterator();
                while (idIterator.hasNext()) {
                    Long id = idIterator.next();
                    StateGroup stateGroup = this.stateGroupPool.find(id);
                    if (stateGroup != null && stateGroup.tryEnterGroup(task)) {
                        return;
                    }
                }
            }
        }
        StateGroupPool.FetchStateGroup fetchStateGroup = this.stateGroupPool.findOrCreate(groupId);
        StateGroup stateGroup = fetchStateGroup.getStateGroup();
        if (stateGroup.canDeposed()) {
            return;
        }

        stateGroup.tryEnterGroup(task);

        final ConcurrentHashMap<Long, StateGroupWorker> workerMap = this.workerMap;
        Long currentWorkerId = stateGroup.getCurrentWorkerId();
        if (!fetchStateGroup.isNew() && currentWorkerId != null) {
            StateGroupWorker worker = workerMap.get(currentWorkerId);
            if (worker != null) {
                if (worker.tryAddStateGroup(stateGroup)) {
                    return;
                }
            }
        } else {
            for (StateGroupWorker worker : workerMap.values()) {
                if (!worker.isStart()) {
                    continue;
                }

                if (worker.isFull()) {
                    continue;
                }

                if (worker.tryAddStateGroup(stateGroup)) {
                    return;
                }
            }
        }

        if (this.workerMap.size() > this.workerCapacity) {
            failCallBack.call();
            return;
        }

        AbstractStateGroupWorker worker = this.createWorker();
        worker.start();
        workerMap.put(worker.getId(), worker);
        worker.tryAddStateGroup(stateGroup);
        this.workerMap = workerMap;
    }

    protected AbstractStateGroupWorker createWorker() {
        return new AbstractStateGroupWorker(ID_COUNT.incrementAndGet(),
                this.workerUpdatePeriod,
                this.workerCapacity,
                this.removeDeposedStateGroupPeriod,
                this.stateGroupPool,
                this.executorService) {
        };
    }

    @Override
    public void enter(GroupIdTask groupIdTask) {
        this.dispatcherTask(new EnterGroupTask(() -> {
            AbstractWorkerManager.this.findBestGroup2Enter(groupIdTask.getGroupId(), groupIdTask.getTask(), groupIdTask.getFailCallBack());
        }));
    }

    @Override
    public void addTask(GroupIdTask groupIdTask) {
        Long groupId = groupIdTask.getGroupId();
        if (groupId == null) {
            this.enter(groupIdTask);
            return;
        }
        StateGroupPool.FetchStateGroup fetchStateGroup = this.stateGroupPool.findOrCreate(groupIdTask.getGroupId());
        // this should be enter
        if (fetchStateGroup.isNew()) {
            this.enter(groupIdTask);
            return;
        }
        StateGroup stateGroup = fetchStateGroup.getStateGroup();
        Long currentWorkerId = stateGroup.getCurrentWorkerId();
        if (currentWorkerId == null) {
            this.enter(groupIdTask);
            return;
        }
        Worker worker = this.workerMap.get(currentWorkerId);
        if (worker == null) {
            this.enter(groupIdTask);
            return;
        }
        this.dispatcherTask(groupIdTask);
    }
}
