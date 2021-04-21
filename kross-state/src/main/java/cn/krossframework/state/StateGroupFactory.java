package cn.krossframework.state;

import cn.krossframework.state.StateGroup;
import cn.krossframework.state.config.StateGroupConfig;

public interface StateGroupFactory {
    StateGroup create(long id, StateGroupConfig stateGroupConfig);
}
