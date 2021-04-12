package cn.krossframework.state;

import java.util.Collection;

public interface StateGroupWorker extends Worker {

    boolean tryAddStateGroup(StateGroup stateGroup);

    Collection<Long> stateGroupIdIterator();
}
