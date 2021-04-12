package cn.krossframework.state;

import java.util.Collection;
import java.util.Collections;

public interface StateGroupConfig {

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
