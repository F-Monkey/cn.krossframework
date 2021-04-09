package cn.krossframework.chat.state;

import cn.krossframework.state.DefaultStateGroupFactory;
import cn.krossframework.state.StateGroup;
import cn.krossframework.state.StateGroupConfig;
import cn.krossframework.state.Time;

public class ChatRoomFactory extends DefaultStateGroupFactory {

    public ChatRoomFactory(Time time, StateGroupConfig stateGroupConfig) {
        super(time, stateGroupConfig);
    }

    @Override
    public StateGroup create(long id) {
        return new ChatRoom(id, super.time, super.stateGroupConfig);
    }
}
