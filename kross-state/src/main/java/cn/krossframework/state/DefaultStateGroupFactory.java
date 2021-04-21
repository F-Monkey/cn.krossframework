package cn.krossframework.state;

import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.util.Time;
import com.google.common.base.Preconditions;

public class DefaultStateGroupFactory implements StateGroupFactory {

    protected final Time time;

    public DefaultStateGroupFactory(Time time) {
        Preconditions.checkNotNull(time);
        this.time = time;
    }

    @Override
    public StateGroup create(long id, StateGroupConfig stateGroupConfig) {
        return new AbstractStateGroup(id, this.time, stateGroupConfig) {

        };
    }
}
