package cn.krossframework.state;

import cn.krossframework.commons.thread.AutoTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractWorkerManager implements WorkerManager, Lock {

    private static final Logger log = LoggerFactory.getLogger(AbstractWorkerManager.class);

    private static final AtomicLong ID_COUNT = new AtomicLong(0);

    protected final int workerUpdatePeriod;

    protected final int workerSize;

    protected final int removeDeposedStateGroupPeriod;

    protected final int workerCapacity;

    protected final int taskDispatcherSize;

    protected final StateGroupPool stateGroupPool;

    protected final Object LOCK;

    protected final ConcurrentHashMap<Long, TaskDispatcher> dispatcherMap;

    protected volatile ConcurrentHashMap<Long, StateGroupWorker> workerMap;

    /**
     * @param workerUpdatePeriod            worker的刷新频率
     * @param workerCapacity                worker内的stateGroup数量
     * @param workerSize                    worker的数量
     * @param removeEmptyWorkerPeriod       移除worker的频率
     * @param removeDeposedStateGroupPeriod 移除worker内的失效的stateGroup标记
     * @param taskDispatcherSize            taskDispatcher的数量
     * @param stateGroupPool                stateGroup池
     */
    public AbstractWorkerManager(int workerUpdatePeriod, int workerCapacity, int workerSize,
                                 int removeEmptyWorkerPeriod, int removeDeposedStateGroupPeriod, int taskDispatcherSize,
                                 StateGroupPool stateGroupPool) {
        this.workerUpdatePeriod = workerUpdatePeriod;
        this.removeDeposedStateGroupPeriod = removeDeposedStateGroupPeriod;
        this.taskDispatcherSize = taskDispatcherSize;
        this.workerSize = workerSize;
        this.stateGroupPool = stateGroupPool;
        this.workerCapacity = workerCapacity;
        this.LOCK = new Object();
        this.workerMap = this.initWorkerMap();
        this.dispatcherMap = this.initDispatcherMap();
        new AutoTask(removeEmptyWorkerPeriod, 2) {

            @Override
            protected void run() {
                AbstractWorkerManager.this.removeEmptyWorker();
            }
        }.start();
    }

    public class EnterGroupTask extends ExecuteTask implements Runnable {

        private final StateGroupConfig stateGroupConfig;

        public EnterGroupTask(Long groupId, Task task, FailCallBack failCallBack, StateGroupConfig stateGroupConfig) {
            super(groupId, task, failCallBack);
            this.stateGroupConfig = stateGroupConfig;
        }

        @Override
        public void run() {
            AbstractWorkerManager.this.findBestGroup2Enter(super.getGroupId(), super.getTask(),
                    super.getFailCallBack(), this.stateGroupConfig);
        }
    }

    protected ConcurrentHashMap<Long, TaskDispatcher> initDispatcherMap() {
        return new ConcurrentHashMap<>(this.taskDispatcherSize);
    }

    protected ConcurrentHashMap<Long, StateGroupWorker> initWorkerMap() {
        return new ConcurrentHashMap<>(this.workerSize);
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

    protected void addDispatcherTask(Task task) {
        Long groupId = null;
        FailCallBack callBack = null;
        if (task instanceof ExecuteTask) {
            ExecuteTask groupIdTask = (ExecuteTask) task;
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
        long index;

        if (groupId == null) {
            // 单独的一个处理空groupId的worker线程处理，避免干扰其他正常的线程
            index = this.taskDispatcherSize + 1;
        } else {
            index = groupId % this.taskDispatcherSize;
        }
        return this.dispatcherMap
                .computeIfAbsent(index, (i) -> this.createDispatcher(i, this.stateGroupPool));
    }

    protected TaskDispatcher createDispatcher(long id, StateGroupPool stateGroupPool) {
        TaskDispatcher taskDispatcher = new AbstractTaskDispatcher(id, stateGroupPool) {

        };
        taskDispatcher.start();
        return taskDispatcher;
    }

    private boolean findGroup2Enter(Long groupId, Task task, StateGroupConfig stateGroupConfig) {
        StateGroupPool.FetchStateGroup fetchStateGroup = this.stateGroupPool.findOrCreate(groupId, stateGroupConfig);
        StateGroup stateGroup = fetchStateGroup.getStateGroup();
        if (stateGroup.canDeposed()) {
            return false;
        }
        if (!stateGroup.tryEnterGroup(task)) {
            return false;
        }

        final ConcurrentHashMap<Long, StateGroupWorker> workerMap = this.workerMap;

        Long currentWorkerId = stateGroup.getCurrentWorkerId();
        if (!fetchStateGroup.isNew() && currentWorkerId != null) {
            StateGroupWorker worker = workerMap.get(currentWorkerId);
            if (worker != null) {
                if (worker.tryAddStateGroup(stateGroup)) {
                    return true;
                }
            }
        }

        for (StateGroupWorker worker : workerMap.values()) {
            if (!worker.isStart()) {
                continue;
            }
            if (worker.isFull()) {
                continue;
            }
            if (worker.tryAddStateGroup(stateGroup)) {
                return true;
            }
        }

        if (this.workerMap.size() >= this.workerSize) {
            return false;
        }

        StateGroupWorker worker = this.createWorker();
        worker.start();
        workerMap.put(worker.getId(), worker);
        worker.tryAddStateGroup(stateGroup);
        this.workerMap = workerMap;
        return true;
    }

    protected void findBestGroup2Enter(Long groupId, Task task, FailCallBack failCallBack, StateGroupConfig stateGroupConfig) {
        if (this.findGroup2Enter(groupId, task, stateGroupConfig)) {
            return;
        }
        log.error("can not find stateGroup to enter");
        if (failCallBack != null) {
            failCallBack.call();
        }
    }

    protected StateGroupWorker createWorker() {
        return new AbstractStateGroupWorker(ID_COUNT.incrementAndGet(), this.workerUpdatePeriod,
                this.workerCapacity, this.removeDeposedStateGroupPeriod, this.stateGroupPool) {
        };
    }

    @Override
    public void enter(ExecuteTask executeTask, StateGroupConfig stateGroupConfig) {
        this.addDispatcherTask(new EnterGroupTask(executeTask.getGroupId(), executeTask.getTask(),
                executeTask.getFailCallBack(), stateGroupConfig));
    }

    @Override
    public void addTask(ExecuteTask executeTask) {
        if (!this.tryAddTask(executeTask)) {
            FailCallBack failCallBack = executeTask.getFailCallBack();
            if (failCallBack != null) {
                failCallBack.call();
            }
        }
    }

    protected boolean tryAddTask(ExecuteTask executeTask) {
        Long groupId = executeTask.getGroupId();
        if (groupId == null) {
            log.error("groupId is empty");
            return false;
        }
        StateGroup stateGroup = this.stateGroupPool.find(groupId);
        // this should be enter
        if (stateGroup == null) {
            log.error("can not find stateGroup by groupId:{}", groupId);
            return false;
        }
        Long currentWorkerId = stateGroup.getCurrentWorkerId();
        if (currentWorkerId == null) {
            log.error("group: {} worker is null", groupId);
            return false;
        }
        Worker worker = this.workerMap.get(currentWorkerId);
        if (worker == null) {
            log.error("group: {} worker: {} is not exists", groupId, currentWorkerId);
            return false;
        }
        this.addDispatcherTask(executeTask);
        return true;
    }
}
