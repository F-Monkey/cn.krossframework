package cn.krossframework.state;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractTaskDispatcher implements TaskDispatcher, Lock {

    private static final Logger log = LoggerFactory.getLogger(AbstractTaskDispatcher.class);

    protected final long id;

    protected final ExecutorService executorService;

    protected final StateGroupPool stateGroupPool;

    protected final Thread thread;

    private final Object LOCK;

    protected final BlockingQueue<Task> groupIdTaskQueue;

    protected volatile boolean isStart;

    public AbstractTaskDispatcher(long id,
                                  ExecutorService executorService,
                                  StateGroupPool stateGroupPool) {
        Preconditions.checkNotNull(executorService);
        this.id = id;
        this.executorService = executorService;
        this.thread = new Thread(AbstractTaskDispatcher.this::run);
        this.LOCK = new Object();
        this.groupIdTaskQueue = new LinkedBlockingQueue<>();
        this.stateGroupPool = stateGroupPool;
    }

    protected void run() {
        for (; ; ) {
            try {
                this.update();
            } catch (Exception e) {
                log.error("update error:\n", e);
            }
            this.tryLock();
        }
    }


    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public void start() {
        this.isStart = true;
        this.executorService.submit(this.thread);
    }

    @Override
    public boolean isStart() {
        return this.isStart;
    }

    @Override
    public void update() {
        do {
            List<Task> taskList = new ArrayList<>(this.groupIdTaskQueue.size());
            this.groupIdTaskQueue.drainTo(taskList);
            for (Task task : taskList) {
                if (task == null) continue;

                if (!this.findStateGroupAndAddTask(task)) {
                    log.error("stateGroup task add fail, task type:{}, task:\n{}", task.getClass(), task);
                }
            }
        } while (this.groupIdTaskQueue.size() > 0);
    }

    protected boolean findStateGroupAndAddTask(Task task) {
        if (task instanceof AbstractWorkerManager.EnterGroupTask) {
            // This task must happened before add task
            ((AbstractWorkerManager.EnterGroupTask) task).run();
            return true;
        }
        if (task instanceof ExecuteTask) {
            ExecuteTask groupIdTask = (ExecuteTask) task;
            Long groupId = groupIdTask.getGroupId();
            FailCallBack failCallBack = groupIdTask.getFailCallBack();
            if (groupId == null) {
                if (failCallBack != null) {
                    failCallBack.call();
                }
            } else {
                StateGroup stateGroup = this.stateGroupPool.find(groupId);
                if (stateGroup == null) {
                    if (failCallBack != null) {
                        failCallBack.call();
                    }
                    return false;
                }
                return stateGroup.tryAddTask(groupIdTask.getTask());
            }
        }
        return false;
    }

    @Override
    public void stop() {
        this.thread.interrupt();
        this.isStart = false;
    }

    @Override
    public boolean tryAddTask(Task task) {
        boolean b = this.isStart() && this.groupIdTaskQueue.offer(task);
        if (b) {
            this.unlock();
        } else {
            log.error("dispatcher task add error");
        }
        return b;
    }

    @Override
    public boolean tryLock() {
        synchronized (this.LOCK) {
            try {
                this.LOCK.wait();
            } catch (InterruptedException ignore) {
                return false;
            }
            return true;
        }
    }

    @Override
    public void unlock() {
        synchronized (this.LOCK) {
            this.LOCK.notifyAll();
        }
    }
}
