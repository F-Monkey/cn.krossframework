package cn.krossframework.state;

public interface WorkerManager {

    boolean enter(Worker.GroupIdTaskPair groupIdTaskPair);

    void addTask(Worker.GroupIdTaskPair groupIdTaskPair);

}
