package cn.krossframework.chat.state;

import cn.krossframework.state.*;

public class ChatWorkerManager extends AbstractWorkerManager {

    public ChatWorkerManager(int workerUpdatePeriod, int workerCapacity, int workerThreadSize, int removeEmptyWorkerPeriod, int removeDeposedStateGroupPeriod, int taskDispatcherSize, StateGroupPool stateGroupPool) {
        super(workerUpdatePeriod, workerCapacity, workerThreadSize, removeEmptyWorkerPeriod, removeDeposedStateGroupPeriod, taskDispatcherSize, stateGroupPool);
    }

    @Override
    public void addTask(GroupIdTask groupIdTask) {
        super.addTask(groupIdTask);
    }
}
