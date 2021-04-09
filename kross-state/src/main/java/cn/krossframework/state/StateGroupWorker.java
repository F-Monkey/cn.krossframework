package cn.krossframework.state;

import java.util.Iterator;

public interface StateGroupWorker extends Worker {
    boolean tryAddStateGroup(StateGroup stateGroup);

    Iterator<Long> stateGroupIdIterator();
}
