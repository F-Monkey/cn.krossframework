package cn.krossframework.state;

public interface WorkerManager {

    void enter(GroupIdTask groupIdTask);

    void addTask(GroupIdTask groupIdTask);

}
