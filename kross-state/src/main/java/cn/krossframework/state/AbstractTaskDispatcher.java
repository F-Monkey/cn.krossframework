package cn.krossframework.state;

import cn.krossframework.state.data.ExecuteTask;
import cn.krossframework.state.util.FailCallBack;
import cn.krossframework.state.util.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractTaskDispatcher implements TaskDispatcher, Lock {

    private static final Logger log = LoggerFactory.getLogger(AbstractTaskDispatcher.class);

    protected final long id;

    protected final StateGroupPool stateGroupPool;

    protected final Thread thread;

    private final Object LOCK;

    protected final BlockingQueue<AbstractTask> groupIdTaskQueue;

    protected volatile boolean isStart;

    public AbstractTaskDispatcher(long id,
                                  StateGroupPool stateGroupPool) {
        this.id = id;
        this.thread = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    try {
                        AbstractTaskDispatcher.this.update();
                    } catch (Exception e) {
                        log.error("update error:\n", e);
                    }
                    AbstractTaskDispatcher.this.tryLock();
                }
            }
        };
        this.LOCK = new Object();
        this.groupIdTaskQueue = new LinkedBlockingQueue<>();
        this.stateGroupPool = stateGroupPool;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public void start() {
        this.thread.start();
        this.isStart = true;
    }

    @Override
    public boolean isStart() {
        return this.isStart;
    }

    @Override
    public void update() {
        do {
            List<AbstractTask> taskList = new ArrayList<>(this.groupIdTaskQueue.size());
            this.groupIdTaskQueue.drainTo(taskList);
            for (AbstractTask task : taskList) {
                if (task == null) continue;
                FailCallBack failCallBack = task.failCallBack();
                if (!this.findStateGroupAndAddTask(task)) {
                    log.error("stateGroup task add fail, task type:{}, task:\n{}", task.getClass(), task);
                    if (failCallBack != null) {
                        failCallBack.call();
                    }
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
            StateGroup stateGroup = this.stateGroupPool.find(groupId);
            if (stateGroup == null) {
                return false;
            }
            return stateGroup.tryAddTask(groupIdTask.getTask());
        }
        return false;
    }

    @Override
    public void stop() {
        this.isStart = false;
        this.thread.interrupt();
    }

    @Override
    public boolean tryAddTask(AbstractTask task) {
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
