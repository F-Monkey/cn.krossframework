package cn.krossframework.state;

import java.util.Collection;

public interface StateGroupConfig {
    boolean autoUpdate();

    Collection<State> getStates();
}
