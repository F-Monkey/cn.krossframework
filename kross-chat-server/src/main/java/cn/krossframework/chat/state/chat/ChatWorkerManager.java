package cn.krossframework.chat.state.chat;

import cn.krossframework.state.*;

public class ChatWorkerManager extends AbstractWorkerManager {

    public ChatWorkerManager(int workerUpdatePeriod, int workerCapacity, int workerThreadSize, int removeEmptyWorkerPeriod, int removeDeposedStateGroupPeriod, int taskDispatcherSize, StateGroupPool stateGroupPool) {
        super(workerUpdatePeriod, workerCapacity, workerThreadSize, removeEmptyWorkerPeriod, removeDeposedStateGroupPeriod, taskDispatcherSize, stateGroupPool);
    }

    @Override
    public void addTask(Worker.GroupIdTaskPair groupIdTaskPair) {
        Task task = groupIdTaskPair.getTask();
        if (task instanceof ChatTask) {
            super.addTask(groupIdTaskPair);
            return;
        }
        FailCallBack callBack = groupIdTaskPair.getCallBack();
        if (callBack != null) {
            callBack.call();
        }
    }
}
