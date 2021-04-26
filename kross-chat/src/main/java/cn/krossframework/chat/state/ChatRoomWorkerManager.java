package cn.krossframework.chat.state;

import cn.krossframework.chat.cmd.ChatCmdType;
import cn.krossframework.chat.state.data.ChatTask;
import cn.krossframework.proto.Command;
import cn.krossframework.state.AbstractWorkerManager;
import cn.krossframework.state.StateGroupPool;
import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.data.DefaultTask;
import cn.krossframework.state.data.ExecuteTask;
import cn.krossframework.state.data.Task;
import cn.krossframework.state.data.WorkerManagerProperties;
import cn.krossframework.websocket.Character;

public class ChatRoomWorkerManager extends AbstractWorkerManager {

    /**
     * @param workerManagerProperties workerManagerProperties
     * @param stateGroupPool          stateGroupPool
     */
    public ChatRoomWorkerManager(WorkerManagerProperties workerManagerProperties, StateGroupPool stateGroupPool) {
        super(workerManagerProperties, stateGroupPool);
    }

    @Override
    protected boolean findGroup2Enter(Long groupId, Task task, StateGroupConfig stateGroupConfig) {
        ChatTask chatTask = (ChatTask) task;
        Character character = chatTask.getCharacter();
        Long currentGroupId = character.getCurrentGroupId();
        if (!super.findGroup2Enter(groupId, task, stateGroupConfig)) {
            return false;
        }
        if (currentGroupId != null && !currentGroupId.equals(groupId)) {
            Command.Package.Builder builder = Command.Package.newBuilder();
            builder.setCmdType(ChatCmdType.EXISTS_ROOM);
            super.addTask(new ExecuteTask(currentGroupId, new ChatTask(character, builder.build()), null));
        }
        return true;
    }
}
