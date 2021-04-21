package cn.krossframework.state;

public interface StateGroupFactory {
    StateGroup create(long id, StateGroupConfig stateGroupConfig);
}
