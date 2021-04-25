package cn.krossframework.state;

import cn.krossframework.state.data.AbstractTask;

public interface TaskDispatcher extends Worker {
    boolean tryAddTask(AbstractTask task);
}
