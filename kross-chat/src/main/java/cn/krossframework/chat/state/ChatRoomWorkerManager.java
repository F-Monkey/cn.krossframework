package cn.krossframework.chat.state;

import cn.krossframework.state.AbstractWorkerManager;
import cn.krossframework.state.StateGroupPool;

public class ChatRoomWorkerManager extends AbstractWorkerManager {
    /**
     * @param workerUpdatePeriod            worker的刷新频率
     * @param workerCapacity                worker内的stateGroup数量
     * @param workerSize                    worker的数量
     * @param removeEmptyWorkerPeriod       移除worker的频率
     * @param removeDeposedStateGroupPeriod 移除worker内的失效的stateGroup标记
     * @param taskDispatcherSize            taskDispatcher的数量
     * @param stateGroupPool                stateGroup池
     */
    public ChatRoomWorkerManager(int workerUpdatePeriod, int workerCapacity, int workerSize, int removeEmptyWorkerPeriod, int removeDeposedStateGroupPeriod, int taskDispatcherSize, StateGroupPool stateGroupPool) {
        super(workerUpdatePeriod, workerCapacity, workerSize, removeEmptyWorkerPeriod, removeDeposedStateGroupPeriod, taskDispatcherSize, stateGroupPool);
    }
}
