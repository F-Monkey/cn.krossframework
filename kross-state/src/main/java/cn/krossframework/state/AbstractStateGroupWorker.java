package cn.krossframework.state;

import cn.krossframework.commons.thread.AutoTask;
import cn.krossframework.state.util.Lock;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
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

    protected volatile ConcurrentHashMap<Long, StateGroup> stateGroupMap;

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
        this.stateGroupMap = new ConcurrentHashMap<>(this.stateGroupCapacity);
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
        synchronized (this.LOCK) {
            this.LOCK.notifyAll();
        }
    }

    protected void removeDeposedStateGroup() {
        if (this.stateGroupMap.isEmpty()) {
            return;
        }
        log.info("start remove worker id:{} stateGroups, currentSize:{}", this.id, this.stateGroupMap.size());
        final ConcurrentHashMap<Long, StateGroup> stateGroupMap = this.stateGroupMap;
        stateGroupMap.entrySet().removeIf(e -> e.getValue().canDeposed());
        log.info("end remove worker id:{} stateGroups, currentSize:{}", this.id, stateGroupMap.size());
        this.stateGroupMap = stateGroupMap;
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
        return (this.currentAddStateGroup == null ? 0 : 1) + this.stateGroupMap.size() >= this.stateGroupCapacity;
    }

    @Override
    public boolean isEmpty() {
        return this.stateGroupMap.isEmpty() && this.currentAddStateGroup == null;
    }

    @Override
    public void update() {
        for (StateGroup stateGroup : this.stateGroupMap.values()) {
            stateGroup.update();
        }
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
        this.stateGroupMap.putIfAbsent(stateGroup.getId(), stateGroup);
        stateGroup.setCurrentWorkerId(this.id);
        this.currentAddStateGroup = null;
        return true;
    }

    @Override
    public Collection<StateGroup> stateGroupIterator() {
        return this.stateGroupMap.values();
    }
}
