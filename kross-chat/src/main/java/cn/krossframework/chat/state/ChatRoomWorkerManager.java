package cn.krossframework.chat.state;

import cn.krossframework.state.AbstractWorkerManager;
import cn.krossframework.state.StateGroupPool;
import cn.krossframework.state.data.WorkerManagerProperties;

public class ChatRoomWorkerManager extends AbstractWorkerManager {

    /**
     * @param workerManagerProperties workerManagerProperties
     * @param stateGroupPool stateGroupPool
     */
    public ChatRoomWorkerManager(WorkerManagerProperties workerManagerProperties, StateGroupPool stateGroupPool) {
        super(workerManagerProperties, stateGroupPool);
    }
}
