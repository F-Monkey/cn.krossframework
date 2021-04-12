package cn.krossframework.chat.state;

import cn.krossframework.state.*;
import cn.krossframework.commons.web.Character;

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
    public boolean tryEnterGroup(Task task) {
        if (!super.tryEnterGroup(task)) {
            return false;
        }
        if(task instanceof ChatTask){
            Character character = ((ChatTask) task).getCharacter();
            ((RoomData) super.stateData).addCharacter(character);
            return true;
        }
        return false;
    }
}
