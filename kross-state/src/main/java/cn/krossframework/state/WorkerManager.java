package cn.krossframework.state;

import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.data.AbstractTask;
import cn.krossframework.state.data.ExecuteTask;

public interface WorkerManager {

    void enter(AbstractTask task, StateGroupConfig stateGroupConfig);

    void addTask(ExecuteTask executeTask);

}
