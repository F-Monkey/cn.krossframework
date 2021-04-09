package cn.krossframework.state;

import cn.krossframework.commons.collection.RingBuffer;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class AbstractStateGroup implements StateGroup {

    private static final Logger log = LoggerFactory.getLogger(AbstractStateGroup.class);

    protected final long id;

    protected final StateData stateData;

    protected final Time time;

    protected final StateGroupConfig stateGroupConfig;

    protected final Map<String, State> stateMap;

    protected final Queue<Task> taskQueue;

    protected State currentState;

    protected volatile Long currentWorkerId;

    public AbstractStateGroup(long id,
                              Time time,
                              StateGroupConfig stateGroupConfig) {
        Preconditions.checkArgument(id > 0);
        Preconditions.checkNotNull(time);
        Preconditions.checkNotNull(stateGroupConfig);
        this.time = time;
        this.id = id;
        this.stateGroupConfig = stateGroupConfig;
        Collection<State> states = stateGroupConfig.getStates();
        if (states != null && states.size() > 0) {
            for (State state : states) {
                this.addState(state);
            }
        }
        this.stateData = this.initStateData();
        this.stateMap = new HashMap<>();
        this.taskQueue = this.initTaskQueue();
    }

    protected StateData initStateData() {
        AbstractStateData stateData = new AbstractStateData() {
        };
        stateData.setGroupId(this.id);
        return stateData;
    }

    /**
     * return an async queue,
     * all task should be provide by {@link #tryAddTask(Task)}
     * and consume by {@link #update()}
     * provider and consumer can be different thread
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

    @Override
    public Long getCurrentWorkerId() {
        return this.currentWorkerId;
    }

    @Override
    public void setCurrentWorkerId(long id) {
        this.currentWorkerId = id;
    }

    @Override
    public void addState(State state) {
        state.setStateData(this.stateData);
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
                log.info("try to switch to state: {}", nextStateCode);
                this.initAndSetCurrentState(nextStateCode);
            } catch (Exception e) {
                log.error("{} initAndSetCurrentState error:\n", this.getId(), e);
                this.currentState = null;
            }
        }
    }

    @Override
    public boolean canDeposed() {
        return this.currentState == null || DefaultErrorState.CODE.equals(this.currentState.getCode());
    }
}
