package cn.krossframework.state;

import cn.krossframework.commons.thread.AutoTask;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected final LinkedBlockingQueue<EnterWorkerTask> enterWorkerTaskQueue;

    protected final StateGroupPool stateGroupPool;

    protected final Object LOCK;

    protected final ConcurrentHashMap<Long, TaskDispatcher> dispatcherMap;

    protected volatile ConcurrentHashMap<Long, StateGroupWorker> workerMap;

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
        this.enterWorkerTaskQueue = new LinkedBlockingQueue<>();
        this.workerCapacity = workerCapacity;
        this.LOCK = new Object();
        this.executorService = new ThreadPoolExecutor(workerThreadSize + taskDispatcherSize,
                workerCapacity + taskDispatcherSize,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        this.workerMap = this.initWorkerMap();
        this.dispatcherMap = this.initDispatcherMap();
        this.executorService.submit(() -> {
            for (; ; ) {
                this.tryLock();
                EnterWorkerTask task = this.enterWorkerTaskQueue.poll();
                if (task == null) {
                    continue;
                }
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("enterWorkerTask run error:\n", e);
                }
            }
        });
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

    class EnterWorkerTask {
        private final Worker.GroupIdTaskPair groupIdTaskPair;

        public EnterWorkerTask(Worker.GroupIdTaskPair groupIdTaskPair) {
            Preconditions.checkNotNull(groupIdTaskPair);
            this.groupIdTaskPair = groupIdTaskPair;
        }

        public void run() {
            AbstractWorkerManager.this.findBestWorker2Enter(this.groupIdTaskPair);
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

    protected void dispatcherTask(Worker.GroupIdTaskPair groupIdTaskPair) {
        Long groupId = groupIdTaskPair.getGroupId();
        FailCallBack callBack = groupIdTaskPair.getCallBack();
        if (groupId == null) {
            if (callBack != null) {
                callBack.call();
            }
            return;
        }
        TaskDispatcher dispatcher = this.findDispatcher(groupId);
        if (dispatcher == null) {
            if (callBack != null) {
                callBack.call();
            }
            return;
        }
        if (!dispatcher.tryAddTask(groupIdTaskPair)) {
            if (callBack != null) {
                callBack.call();
            }
        }
    }

    protected TaskDispatcher findDispatcher(long groupId) {
        long index = groupId % this.taskDispatcherSize;
        return this.dispatcherMap.computeIfAbsent(index, (i) -> {
            TaskDispatcher taskDispatcher = new AbstractTaskDispatcher(i, this.executorService, this.stateGroupPool) {
            };
            taskDispatcher.start();
            return taskDispatcher;
        });
    }

    protected void findBestWorker2Enter(Worker.GroupIdTaskPair groupIdTaskPair) {
        Long groupId = groupIdTaskPair.getGroupId();
        StateGroupPool.FetchStateGroup fetchStateGroup = this.stateGroupPool.findOrCreate(groupId);
        StateGroup stateGroup = fetchStateGroup.getStateGroup();
        if (stateGroup.canDeposed()) {
            return;
        }
        stateGroup.tryAddTask(groupIdTaskPair.getTask());
        final ConcurrentHashMap<Long, StateGroupWorker> workerMap = this.workerMap;
        Long currentWorkerId = stateGroup.getCurrentWorkerId();
        boolean enterSuccess = false;
        if (!fetchStateGroup.isNew() && currentWorkerId != null) {
            StateGroupWorker worker = workerMap.get(currentWorkerId);
            if (worker != null) {
                if (worker.tryAddStateGroup(stateGroup)) {
                    enterSuccess = true;
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
                    enterSuccess = true;
                }
            }
        }
        if (!enterSuccess) {
            AbstractStateGroupWorker worker = this.createWorker();
            worker.start();
            workerMap.put(worker.getId(), worker);
            worker.tryAddStateGroup(stateGroup);
        }
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
    public boolean enter(Worker.GroupIdTaskPair groupIdTaskPair) {
        if (!this.enterWorkerTaskQueue.offer(new EnterWorkerTask(groupIdTaskPair))) {
            log.error("enter fail");
            return false;
        }
        this.unlock();
        return true;
    }

    @Override
    public void addTask(Worker.GroupIdTaskPair groupIdTaskPair) {
        StateGroupPool.FetchStateGroup fetchStateGroup = this.stateGroupPool.findOrCreate(groupIdTaskPair.getGroupId());
        // this should be enter
        if (fetchStateGroup.isNew()) {
            this.enter(groupIdTaskPair);
            return;
        }
        StateGroup stateGroup = fetchStateGroup.getStateGroup();
        Long currentWorkerId = stateGroup.getCurrentWorkerId();
        if (currentWorkerId == null) {
            this.enter(groupIdTaskPair);
            return;
        }
        Worker worker = this.workerMap.get(currentWorkerId);
        if (worker == null) {
            this.enter(groupIdTaskPair);
            return;
        }
        this.dispatcherTask(groupIdTaskPair);
    }
}
