package cn.krossframework.state;

public abstract class AbstractState implements State {

    protected StateData stateData;

    @Override
    public void setStateData(StateData stateData) {
        this.stateData = stateData;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends StateData> T getStateData() {
        return (T) stateData;
    }

    @Override
    public void init(Time time) {

    }

    @Override
    public void handleTask(Time time, Task task) {

    }

    @Override
    public void update(Time time, StateInfo stateInfo) {

    }

    @Override
    public String finish(Time time) {
        return null;
    }
}
