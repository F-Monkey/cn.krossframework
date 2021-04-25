package cn.krossframework.chat.state;

import cn.krossframework.state.DefaultStateGroupFactory;
import cn.krossframework.state.StateGroup;
import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.util.Time;

public class ChatRoomFactory extends DefaultStateGroupFactory {
    public ChatRoomFactory(Time time) {
        super(time);
    }

    @Override
    public StateGroup create(long id, StateGroupConfig stateGroupConfig) {
        return new ChatRoom(id, super.time, stateGroupConfig);
    }
}
