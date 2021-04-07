package cn.krossframework.state;

import com.google.common.base.Preconditions;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultStateGroupFactory implements StateGroupFactory {

    protected final StateGroupConfig stateGroupConfig;

    protected final Time time;

    public DefaultStateGroupFactory(Time time, StateGroupConfig stateGroupConfig) {
        Preconditions.checkNotNull(time);
        Preconditions.checkNotNull(stateGroupConfig);
        this.stateGroupConfig = stateGroupConfig;
        this.time = time;
    }

    @Override
    public StateGroup create(long id) {
        return new AbstractStateGroup(id, this.time, this.stateGroupConfig) {
            @Override
            protected BlockingQueue<Task> initTaskQueue() {
                return new LinkedBlockingQueue<>();
            }
        };
    }
}
