package cn.krossframework.state;

import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.data.ExecuteTask;

public interface WorkerManager {

    /**
     *
     */
    void enter(ExecuteTask executeTask, StateGroupConfig stateGroupConfig);

    void addTask(ExecuteTask executeTask);

}
