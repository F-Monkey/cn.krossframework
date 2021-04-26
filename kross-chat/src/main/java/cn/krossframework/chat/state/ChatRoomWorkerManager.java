package cn.krossframework.chat.state;

import cn.krossframework.state.AbstractWorkerManager;
import cn.krossframework.state.StateGroupPool;
import cn.krossframework.state.data.WorkerManagerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatRoomWorkerManager extends AbstractWorkerManager {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomWorkerManager.class);

    /**
     * @param workerManagerProperties workerManagerProperties
     * @param stateGroupPool          stateGroupPool
     */
    public ChatRoomWorkerManager(WorkerManagerProperties workerManagerProperties, StateGroupPool stateGroupPool) {
        super(workerManagerProperties, stateGroupPool);
    }
}
