package cn.krossframework.state;

import cn.krossframework.commons.collection.RingBuffer;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class AbstractStateGroup implements StateGroup {

    private static final Logger log = LoggerFactory.getLogger(AbstractStateGroup.class);

    protected final long id;

    protected final Time time;

    protected final StateGroupConfig stateGroupConfig;

    protected final Map<String, State> stateMap;

    protected final Queue<Task> taskQueue;

    protected StateData stateData;

    protected State currentState;

    protected volatile Long currentWorkerId;

    private int lastUpdateTime;

    public AbstractStateGroup(long id,
                              Time time,
                              StateGroupConfig stateGroupConfig) {
        Preconditions.checkArgument(id > 0);
        Preconditions.checkNotNull(time);
        Preconditions.checkNotNull(stateGroupConfig);
        this.time = time;
        this.id = id;
        this.stateGroupConfig = stateGroupConfig;
        this.stateMap = new HashMap<>();
        this.taskQueue = this.initTaskQueue();
        this.init();
    }

    /**
     * return an async queue,
     * all task should be provide by {@link #tryAddTask(Task)}
     * and consume by {@link #update()}
     * provider and consumer can be in different thread
     *
     * @return queue
     */
    protected Queue<Task> initTaskQueue() {
        return new RingBuffer<>();
    }

    @Override
    public long getId() {
        return this.id;
    }

    protected void init() {
        Collection<State> states = this.stateGroupConfig.createStates();
        if (states != null && states.size() > 0) {
            for (State state : states) {
                this.addState(state);
            }
        }
        this.stateData = this.stateGroupConfig.createStateData();
        this.stateData.setGroupId(this.id);
        this.initAndSetCurrentState(this.stateGroupConfig.getStartState());
    }

    @Override
    public Long getCurrentWorkerId() {
        return this.currentWorkerId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends StateGroupConfig> T getConfig() {
        return (T) this.stateGroupConfig;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends StateData> T getStateData() {
        return (T) this.stateData;
    }

    @Override
    public void setCurrentWorkerId(long id) {
        this.currentWorkerId = id;
    }

    protected void addState(State state) {
        state.setStateGroup(this);
        this.stateMap.put(state.getCode(), state);
    }

    @Override
    public void initAndSetCurrentState(String code) throws NullPointerException {
        State state = this.stateMap.get(code);
        if (state == null) {
            throw new NullPointerException("state: " + code + " is not exists");
        }
        try {
            state.init(this.time);
        } catch (Throwable e) {
            state.initOnError(this.time, e);
            return;
        }
        this.currentState = state;
    }

    @Override
    public boolean tryEnterGroup(Task task) {
        return !this.canDeposed() && !this.stateData.isFull();
    }

    @Override
    public boolean tryAddTask(Task task) {
        return !this.canDeposed() && this.taskQueue.offer(task);
    }

    @Override
    public void update() {
        Task task = this.taskQueue.poll();
        if (task != null) {
            try {
                this.currentState.handleTask(this.time, task);
            } catch (Throwable e) {
                this.currentState.handleTaskOnError(this.time, task, e);
            }
        } else {
            if (!this.stateGroupConfig.autoUpdate()) {
                return;
            }
        }

        if (this.time.getCurrentTimeMillis() - this.lastUpdateTime < this.stateGroupConfig.updatePeriod()) {
            return;
        }

        this.lastUpdateTime = 0;
        StateInfo stateInfo = new StateInfo();
        try {
            this.currentState.update(this.time, stateInfo);
        } catch (Throwable e) {
            this.currentState.updateOnError(this.time, stateInfo, e);
        }
        if (stateInfo.isFinished) {
            String nextStateCode;
            try {
                nextStateCode = this.currentState.finish(this.time);
            } catch (Throwable e) {
                nextStateCode = this.currentState.finishOnError(this.time, e);
            }
            try {
                log.info("id: {} try to switch to state: {}", this.getId(), nextStateCode);
                this.initAndSetCurrentState(nextStateCode);
            } catch (Exception e) {
                log.error("id: {} initAndSetCurrentState error:\n", this.getId(), e);
                this.currentState = null;
            }
        }
    }

    @Override
    public boolean canDeposed() {
        return (this.currentState == null || DefaultErrorState.CODE.equals(this.currentState.getCode())) && this.getStateData().isEmpty();
    }
}
