package cn.krossframework.state.config;

import cn.krossframework.state.data.AbstractStateData;
import cn.krossframework.state.data.State;
import cn.krossframework.state.data.StateData;

import java.util.Collection;
import java.util.Collections;

public interface StateGroupConfig {

    String getId();

    String getStartState();

    default boolean autoUpdate() {
        return false;
    }

    default int updatePeriod() {
        return 0;
    }

    /**
     * return new States
     *
     * @return
     */
    default Collection<State> createStates() {
        return Collections.emptyList();
    }

    /**
     * @return return new StateData
     */
    default StateData createStateData() {
        return new AbstractStateData() {

        };
    }
}
