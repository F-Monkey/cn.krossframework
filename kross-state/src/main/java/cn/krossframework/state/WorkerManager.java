package cn.krossframework.state;

public interface WorkerManager {

    void enter(ExecuteTask executeTask);

    void addTask(ExecuteTask executeTask);

}
