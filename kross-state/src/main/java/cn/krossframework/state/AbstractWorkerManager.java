package cn.krossframework.state;

import cn.krossframework.commons.thread.AutoTask;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractWorkerManager implements WorkerManager {

    private static final Logger log = LoggerFactory.getLogger(AbstractWorkerManager.class);

    private static final AtomicLong ID_COUNT = new AtomicLong(0);

    protected final int workerUpdatePeriod;

    protected final int removeDeposedStateGroupPeriod;

    protected final int workerCapacity;

    protected final ExecutorService executorService;

    protected final LinkedBlockingQueue<EnterWorkerTask> enterWorkerTaskQueue;

    protected final StateGroupPool stateGroupPool;

    protected final Object LOCK;

    protected volatile ConcurrentHashMap<Long, Worker> workerMap;

    public AbstractWorkerManager(int workerUpdatePeriod,
                                 int workerCapacity,
                                 int workerSize,
                                 int removeEmptyWorkerPeriod,
                                 int removeDeposedStateGroupPeriod,
                                 StateGroupPool stateGroupPool) {
        this.workerUpdatePeriod = workerUpdatePeriod;
        this.removeDeposedStateGroupPeriod = removeDeposedStateGroupPeriod;
        this.stateGroupPool = stateGroupPool;
        this.enterWorkerTaskQueue = new LinkedBlockingQueue<>();
        this.workerCapacity = workerCapacity;
        this.LOCK = new Object();
        this.workerMap = new ConcurrentHashMap<>();
        this.executorService = new ThreadPoolExecutor(workerSize, workerSize, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        this.executorService.submit(() -> {
            for (; ; ) {
                this.lock();
                EnterWorkerTask task = this.enterWorkerTaskQueue.poll();
                if (task == null) {
                    continue;
                }
                task.run();
            }
        });
        new AutoTask(removeEmptyWorkerPeriod, 2) {

            @Override
            protected void run() {
                AbstractWorkerManager.this.removeEmptyWorker();
            }
        }.start();
    }

    protected void lock() {
        synchronized (this.LOCK) {
            try {
                this.LOCK.wait();
            } catch (InterruptedException ignore) {
            }
        }
    }

    protected void unlock() {
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
        final ConcurrentHashMap<Long, Worker> workerMap = this.workerMap;
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

    protected void findBestWorker2Enter(Worker.GroupIdTaskPair groupIdTaskPair) {
        Long groupId = groupIdTaskPair.getGroupId();
        StateGroupPool.FetchStateGroup fetchStateGroup = this.stateGroupPool.findOrCreate(groupId);
        StateGroup stateGroup = fetchStateGroup.getStateGroup();
        stateGroup.tryAddTask(groupIdTaskPair.getTask());
        boolean enterSuccess = false;
        final ConcurrentHashMap<Long, Worker> workerMap = this.workerMap;
        if (!fetchStateGroup.isNew()) {
            long currentWorkerId = stateGroup.getCurrentWorkerId();
            Worker worker = workerMap.get(currentWorkerId);
            if (worker != null) {
                if (worker.tryAddStateGroup(stateGroup)) {
                    enterSuccess = true;
                }
            }
        } else {
            for (Worker worker : workerMap.values()) {
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
            Worker worker = new AbstractWorker(ID_COUNT.incrementAndGet(),
                    this.workerUpdatePeriod,
                    this.workerCapacity,
                    this.removeDeposedStateGroupPeriod,
                    this.executorService) {
            };
            worker.start();
            workerMap.put(worker.getId(), worker);
            worker.tryAddStateGroup(stateGroup);
        }
        this.workerMap = workerMap;
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

    protected abstract boolean isAutoExecuteTask(Task task);

    @Override
    public void addTask(Worker.GroupIdTaskPair groupIdTaskPair) {
        StateGroupPool.FetchStateGroup fetchStateGroup = this.stateGroupPool.findOrCreate(groupIdTaskPair.getGroupId());
        // this should be enter
        Task task = groupIdTaskPair.getTask();
        if (fetchStateGroup.isNew()) {
            if (this.isAutoExecuteTask(task)) {
                this.enter(groupIdTaskPair);
                return;
            }
        }
        StateGroup stateGroup = fetchStateGroup.getStateGroup();
        long currentWorkerId = stateGroup.getCurrentWorkerId();
        Worker worker = this.workerMap.get(currentWorkerId);
        if (worker == null) {
            if (this.isAutoExecuteTask(task)) {
                this.enter(groupIdTaskPair);
            }
        } else {
            if (!worker.tryAddTask(groupIdTaskPair)) {
                log.error("task add fail");
            }
        }
    }
}
