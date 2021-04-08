package cn.krossframework.state;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected final BlockingQueue<GroupIdTaskPair> groupIdTaskPairQueue;

    protected volatile boolean isStart;

    public AbstractTaskDispatcher(long id,
                                  ExecutorService executorService,
                                  StateGroupPool stateGroupPool) {
        Preconditions.checkNotNull(executorService);
        this.id = id;
        this.executorService = executorService;
        this.thread = new Thread(AbstractTaskDispatcher.this::run);
        this.LOCK = new Object();
        this.groupIdTaskPairQueue = new LinkedBlockingQueue<>();
        this.stateGroupPool = stateGroupPool;
    }

    protected void run() {
        for (; ; ) {
            this.tryLock();
            try {
                this.update();
            } catch (Exception e) {
                log.error("update error:\n", e);
            }
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
            GroupIdTaskPair poll = this.groupIdTaskPairQueue.poll();
            if (poll == null) {
                return;
            }
            if (!this.findStateGroupAndAddTask(poll)) {
                FailCallBack callBack = poll.getCallBack();
                if (callBack != null) {
                    callBack.call();
                }
            }
        } while (this.groupIdTaskPairQueue.size() > 0);
    }

    protected boolean findStateGroupAndAddTask(GroupIdTaskPair groupIdTaskPair) {
        Long groupId = groupIdTaskPair.getGroupId();
        if (groupId == null) {
            return false;
        }
        StateGroup stateGroup = this.stateGroupPool.find(groupId);
        if (stateGroup == null) {
            return false;
        }
        return stateGroup.tryAddTask(groupIdTaskPair.getTask());
    }

    @Override
    public void stop() {
        this.thread.interrupt();
        this.isStart = false;
    }

    @Override
    public boolean tryAddTask(GroupIdTaskPair groupIdTaskPair) {
        boolean b = this.isStart() && this.groupIdTaskPairQueue.offer(groupIdTaskPair);
        if (b) {
            this.unlock();
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
