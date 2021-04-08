package cn.krossframework.state;

public interface StateGroupWorker extends Worker {
    boolean tryAddStateGroup(StateGroup stateGroup);
}
