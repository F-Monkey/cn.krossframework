package cn.krossframework.chat.state;

import cn.krossframework.state.State;
import cn.krossframework.state.StateData;
import cn.krossframework.state.StateGroupConfig;
import com.google.common.collect.Lists;

import java.util.Collection;

public class ChatConfig implements StateGroupConfig {

    @Override
    public Collection<State> createStates() {
        return Lists.newArrayList(new ChatState());
    }

    @Override
    public StateData createStateData() {
        return new RoomData();
    }

}
