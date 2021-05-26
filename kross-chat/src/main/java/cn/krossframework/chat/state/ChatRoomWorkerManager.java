package cn.krossframework.chat.state;

import cn.krossframework.state.AbstractWorkerManager;
import cn.krossframework.state.StateGroup;
import cn.krossframework.state.StateGroupPool;
import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.data.Task;
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

    private boolean findExistsGroupIdToEnter(long groupId, Task task) {
        StateGroup stateGroup = super.stateGroupPool.find(groupId);
        if (stateGroup == null) {
            return false;
        }
        return stateGroup.tryEnterGroup(task);
    }

    @Override
    protected void findBestGroup2Enter(Long groupId, Task task, StateGroupConfig stateGroupConfig) {
        if (groupId != null) {
            if (this.findExistsGroupIdToEnter(groupId, task)) {
                return;
            }
        }
        super.findBestGroup2Enter(groupId, task, stateGroupConfig);
    }
}
