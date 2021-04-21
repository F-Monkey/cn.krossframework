package cn.krossframework.state;

public interface WorkerManager {

    /**
     *
     */
    void enter(ExecuteTask executeTask, StateGroupConfig stateGroupConfig);

    void addTask(ExecuteTask executeTask);

}
