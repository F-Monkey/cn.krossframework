package cn.krossframework.state;

import cn.krossframework.commons.bean.InitializeBean;
import cn.krossframework.commons.thread.AutoTask;
import cn.krossframework.state.data.AbstractTask;
import cn.krossframework.state.data.ExecuteTask;
import cn.krossframework.state.data.Task;
import cn.krossframework.state.data.WorkerManagerProperties;
import cn.krossframework.state.util.FailCallBack;
import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.util.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractWorkerManager implements WorkerManager, Lock, InitializeBean {

    private static final Logger log = LoggerFactory.getLogger(AbstractWorkerManager.class);

    private static final AtomicLong ID_COUNT = new AtomicLong(0);

    protected final int workerUpdatePeriod;

    protected final int workerSize;

    protected final int removeDeposedStateGroupPeriod;

    protected final int removeEmptyWorkerPeriod;

    protected final int workerCapacity;

    protected final int taskDispatcherSize;

    protected final StateGroupPool stateGroupPool;

    protected final Object LOCK;

    protected final ConcurrentHashMap<Long, TaskDispatcher> dispatcherMap;

    protected volatile ConcurrentHashMap<Long, StateGroupWorker> workerMap;

    /**
     * @param workerManagerProperties workerManagerProperties;
     * @param stateGroupPool          stateGroup池
     */
    public AbstractWorkerManager(WorkerManagerProperties workerManagerProperties,
                                 StateGroupPool stateGroupPool) {
        this.workerUpdatePeriod = workerManagerProperties.getWorkerUpdatePeriod();
        this.removeDeposedStateGroupPeriod = workerManagerProperties.getRemoveDeposedStateGroupPeriod();
        this.taskDispatcherSize = workerManagerProperties.getTaskDispatcherSize();
        this.workerSize = workerManagerProperties.getWorkerSize();
        this.stateGroupPool = stateGroupPool;
        this.workerCapacity = workerManagerProperties.getWorkerCapacity();
        this.removeEmptyWorkerPeriod = workerManagerProperties.getRemoveEmptyWorkerPeriod();
        this.LOCK = new Object();
        this.workerMap = this.initWorkerMap();
        this.dispatcherMap = this.initDispatcherMap();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new AutoTask(this.removeEmptyWorkerPeriod, 1) {
            @Override
            protected void run() {
                AbstractWorkerManager.this.removeEmptyWorker();
            }
        }.start();
    }

    public class EnterGroupTask extends AbstractTask implements Runnable {

        protected final StateGroupConfig stateGroupConfig;

        public EnterGroupTask(Long groupId, Task task, FailCallBack failCallBack, StateGroupConfig stateGroupConfig) {
            super(groupId, task, failCallBack);
            this.stateGroupConfig = stateGroupConfig;
        }

        @Override
        public void run() {
            AbstractWorkerManager.this.findBestGroup2Enter(super.getGroupId(), super.task,
                    super.failCallBack(), this.stateGroupConfig);
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
        if (workerMap.isEmpty()) {
            return;
        }
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

    protected void addDispatcherTask(AbstractTask task) {
        Long groupId = null;
        FailCallBack callBack = null;
        if (task instanceof ExecuteTask) {
            ExecuteTask groupIdTask = (ExecuteTask) task;
            groupId = groupIdTask.getGroupId();
            callBack = groupIdTask.failCallBack();
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

    protected boolean findGroup2Enter(Long groupId, Task task, StateGroupConfig stateGroupConfig) {
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
    public void enter(AbstractTask task, StateGroupConfig stateGroupConfig) {
        this.addDispatcherTask(new EnterGroupTask(task.getGroupId(), task.getTask(),
                task.failCallBack(), stateGroupConfig));
    }

    @Override
    public void addTask(AbstractTask executeTask) {
        if (!this.tryAddTask(executeTask)) {
            FailCallBack failCallBack = executeTask.failCallBack();
            if (failCallBack != null) {
                failCallBack.call();
            }
        }
    }

    protected boolean tryAddTask(AbstractTask executeTask) {
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
