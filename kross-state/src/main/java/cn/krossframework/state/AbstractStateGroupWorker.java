package cn.krossframework.state;

import cn.krossframework.commons.thread.AutoTask;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class AbstractStateGroupWorker implements StateGroupWorker, Lock {

    protected static final Logger log = LoggerFactory.getLogger(AbstractStateGroupWorker.class);

    protected final long id;

    protected final int period;

    protected final Thread thread;

    protected final Object LOCK;

    protected final int stateGroupCapacity;

    protected final StateGroupPool stateGroupPool;

    protected final AutoTask autoTask;

    protected volatile boolean isStart;

    protected volatile StateGroup currentAddStateGroup;

    protected volatile ConcurrentSkipListSet<Long> groupIdSet;

    public AbstractStateGroupWorker(long id,
                                    int period,
                                    int stateGroupCapacity,
                                    int removeDeposedStateGroupPeriod,
                                    StateGroupPool stateGroupPool) {
        Preconditions.checkArgument(period > 0);
        Preconditions.checkNotNull(stateGroupPool);
        this.period = period;
        this.stateGroupCapacity = stateGroupCapacity;
        this.stateGroupPool = stateGroupPool;
        this.id = id;
        this.groupIdSet = new ConcurrentSkipListSet<>();
        this.LOCK = new Object();
        this.thread = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    try {
                        AbstractStateGroupWorker.this.update();
                    } catch (Exception ignore) {
                    }
                    AbstractStateGroupWorker.this.tryLock();
                }
            }
        };
        this.autoTask = new AutoTask(removeDeposedStateGroupPeriod, 2) {
            @Override
            protected void run() {
                AbstractStateGroupWorker.this.removeDeposedStateGroup();
            }
        };
    }

    @Override
    public boolean tryLock() {
        synchronized (this.LOCK) {
            try {
                this.LOCK.wait(this.period);
                return true;
            } catch (InterruptedException ignore) {
                return false;
            }
        }
    }

    @Override
    public void unlock() {
        throw new UnsupportedOperationException();
    }

    protected void removeDeposedStateGroup() {
        final ConcurrentSkipListSet<Long> groupIdSet = this.groupIdSet;
        groupIdSet.removeIf(id -> {
            StateGroup stateGroup = this.stateGroupPool.find(id);
            if (stateGroup == null) {
                return true;
            }
            return stateGroup.canDeposed();
        });
        this.groupIdSet = groupIdSet;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public void start() {
        this.thread.start();
        this.autoTask.start();
        this.isStart = true;
    }

    @Override
    public boolean isStart() {
        return this.isStart;
    }

    @Override
    public boolean isFull() {
        return (this.currentAddStateGroup == null ? 0 : 1) + this.groupIdSet.size() >= this.stateGroupCapacity;
    }

    @Override
    public boolean isEmpty() {
        return this.groupIdSet.isEmpty() && this.currentAddStateGroup == null;
    }

    @Override
    public void update() {
        long start = System.currentTimeMillis();
        for (Long id : this.groupIdSet) {
            StateGroup stateGroup = this.stateGroupPool.find(id);
            if (stateGroup != null) {
                stateGroup.update();
            }
        }
        log.debug("id: {} update size:{} cost: {} ms", this.id, this.groupIdSet.size(), (System.currentTimeMillis() - start));
    }

    @Override
    public void stop() {
        this.isStart = false;
        this.thread.interrupt();
        this.autoTask.stop();
    }

    @Override
    public boolean tryAddStateGroup(StateGroup stateGroup) {
        if (this.currentAddStateGroup != null) {
            return false;
        }
        this.currentAddStateGroup = stateGroup;
        stateGroup.setCurrentWorkerId(this.id);
        this.groupIdSet.add(stateGroup.getId());
        this.currentAddStateGroup = null;
        return true;
    }

    @Override
    public Collection<Long> stateGroupIdIterator() {
        return this.groupIdSet;
    }
}
