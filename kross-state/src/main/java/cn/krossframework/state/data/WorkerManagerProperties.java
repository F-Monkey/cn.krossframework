package cn.krossframework.state.data;

public class WorkerManagerProperties {
    private int workerUpdatePeriod;
    private int workerCapacity;
    private int workerSize;
    private int removeEmptyWorkerPeriod;
    private int removeDeposedStateGroupPeriod;
    private int taskDispatcherSize;

    public int getWorkerUpdatePeriod() {
        return workerUpdatePeriod;
    }

    public void setWorkerUpdatePeriod(int workerUpdatePeriod) {
        this.workerUpdatePeriod = workerUpdatePeriod;
    }

    public int getWorkerCapacity() {
        return workerCapacity;
    }

    public void setWorkerCapacity(int workerCapacity) {
        this.workerCapacity = workerCapacity;
    }

    public int getWorkerSize() {
        return workerSize;
    }

    public void setWorkerSize(int workerSize) {
        this.workerSize = workerSize;
    }

    public int getRemoveEmptyWorkerPeriod() {
        return removeEmptyWorkerPeriod;
    }

    public void setRemoveEmptyWorkerPeriod(int removeEmptyWorkerPeriod) {
        this.removeEmptyWorkerPeriod = removeEmptyWorkerPeriod;
    }

    public int getRemoveDeposedStateGroupPeriod() {
        return removeDeposedStateGroupPeriod;
    }

    public void setRemoveDeposedStateGroupPeriod(int removeDeposedStateGroupPeriod) {
        this.removeDeposedStateGroupPeriod = removeDeposedStateGroupPeriod;
    }

    public int getTaskDispatcherSize() {
        return taskDispatcherSize;
    }

    public void setTaskDispatcherSize(int taskDispatcherSize) {
        this.taskDispatcherSize = taskDispatcherSize;
    }
}
