package cn.krossframework.cat.server.state;

import cn.krossframework.state.DefaultStateGroupFactory;
import cn.krossframework.state.StateGroupConfig;
import cn.krossframework.state.Time;

public class CatFactory extends DefaultStateGroupFactory {

    public CatFactory(Time time, StateGroupConfig stateGroupConfig) {
        super(time, stateGroupConfig);
    }
}
