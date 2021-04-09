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

    default Collection<State> getStates() {
        return Collections.emptyList();
    }

    default StateData getStateData(){
        return new AbstractStateData() {
        };
    }
}
