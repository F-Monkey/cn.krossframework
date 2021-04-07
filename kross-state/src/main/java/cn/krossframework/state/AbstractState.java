package cn.krossframework.state;

public abstract class AbstractState implements State {

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
