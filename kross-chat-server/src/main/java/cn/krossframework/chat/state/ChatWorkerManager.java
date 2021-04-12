package cn.krossframework.chat.state;

import cn.krossframework.state.*;

public class ChatWorkerManager extends AbstractWorkerManager {

    public ChatWorkerManager(int workerUpdatePeriod, int workerCapacity, int workerSize, int removeEmptyWorkerPeriod, int removeDeposedStateGroupPeriod, int taskDispatcherSize, StateGroupPool stateGroupPool) {
        super(workerUpdatePeriod, workerCapacity, workerSize, removeEmptyWorkerPeriod, removeDeposedStateGroupPeriod, taskDispatcherSize, stateGroupPool);
    }
}
