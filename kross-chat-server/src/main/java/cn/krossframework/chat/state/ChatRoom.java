package cn.krossframework.chat.state;

import cn.krossframework.state.*;

public class ChatRoom extends AbstractStateGroup {

    public ChatRoom(long id, Time time, StateGroupConfig stateGroupConfig) {
        super(id, time, stateGroupConfig);
    }

    @Override
    public boolean tryAddTask(Task task) {
        if (task instanceof ChatTask) {
            return super.tryAddTask(task);
        }
        return false;
    }

    @Override
    protected StateData initStateData() {
        RoomData roomData = new RoomData();
        roomData.setGroupId(super.id);
        return roomData;
    }
}
