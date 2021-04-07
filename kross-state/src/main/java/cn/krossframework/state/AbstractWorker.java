package cn.krossframework.state;

import cn.krossframework.commons.thread.AutoTask;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public abstract class AbstractWorker implements Worker {

    protected static final Logger log = LoggerFactory.getLogger(AbstractWorker.class);

    protected final long id;

    protected final int period;

    protected final Thread thread;

    protected final ExecutorService executorService;

    protected final Object LOCK;

    protected final int stateGroupCapacity;

    protected volatile boolean isStart;

    protected volatile StateGroup currentAddStateGroup;

    protected volatile ConcurrentHashMap<Long, StateGroup> stateGroupMap;

    public AbstractWorker(long id,
                          int period,
                          int stateGroupCapacity,
                          int removeDeposedStateGroupPeriod,
                          ExecutorService executor) {
        Preconditions.checkArgument(period > 0);
        this.period = period;
        this.stateGroupCapacity = stateGroupCapacity;
        this.id = id;
        this.LOCK = new Object();
        this.thread = new Thread(AbstractWorker.this::run);
        this.stateGroupMap = new ConcurrentHashMap<>();
        this.executorService = executor;
        new AutoTask(removeDeposedStateGroupPeriod, 2) {
            @Override
            protected void run() {
                AbstractWorker.this.removeDeposedStateGroup();
            }
        }.start();
    }

    protected void removeDeposedStateGroup() {
        final ConcurrentHashMap<Long, StateGroup> stateGroupMap = new ConcurrentHashMap<>(this.stateGroupMap);
        stateGroupMap.entrySet().removeIf(e -> {
            boolean b = e.getValue().canDeposed();
            if (b) {
                log.info("stateGroup: {} has been removed", e.getKey());
            }
            return b;
        });
        this.stateGroupMap = stateGroupMap;
    }

    protected void run() {
        for (; ; ) {
            synchronized (this.LOCK) {
                try {
                    this.LOCK.wait(this.period);
                } catch (InterruptedException ignore) {
                }
                this.update();
            }
        }
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public void start() {
        this.executorService.submit(this.thread);
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
    }

    @Override
    public boolean tryAddStateGroup(StateGroup stateGroup) {
        if (this.currentAddStateGroup != null) {
            return false;
        }
        this.currentAddStateGroup = stateGroup;
        StateGroup result = this.stateGroupMap.putIfAbsent(stateGroup.getId(), stateGroup);
        if (result == null) {
            stateGroup.setCurrentWorkerId(this.id);
        }
        this.currentAddStateGroup = null;
        return true;
    }

    @Override
    public boolean tryAddTask(GroupIdTaskPair groupIdTaskPair) {
        final ConcurrentHashMap<Long, StateGroup> stateGroupMap = this.stateGroupMap;
        StateGroup stateGroup = stateGroupMap.get(groupIdTaskPair.getGroupId());
        if (stateGroup == null) {
            return false;
        }
        boolean addResult = stateGroup.tryAddTask(groupIdTaskPair.getTask());
        this.stateGroupMap = stateGroupMap;
        return addResult;
    }
}
