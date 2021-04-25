package cn.krossframework.state.data;

import cn.krossframework.state.StateGroup;
import cn.krossframework.state.util.Time;

public abstract class AbstractState implements State {

    private StateGroup stateGroup;

    @Override
    public void setStateGroup(StateGroup stateGroup) {
        this.stateGroup = stateGroup;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends StateGroup> T getStateGroup() {
        return (T) this.stateGroup;
    }

    @Override
    public void init(Time time) {

    }

    @Override
    public void handleTask(Time time, Task task) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(Time time, StateInfo stateInfo) {

    }

    @Override
    public String finish(Time time) {
        return null;
    }
}
