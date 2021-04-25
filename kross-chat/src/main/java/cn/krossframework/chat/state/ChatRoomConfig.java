package cn.krossframework.chat.state;

import cn.krossframework.state.config.AbstractStateGroupConfig;
import cn.krossframework.state.data.DefaultErrorState;
import cn.krossframework.state.data.State;
import com.google.common.collect.Lists;

import java.util.Collection;


public class ChatRoomConfig extends AbstractStateGroupConfig {

    public ChatRoomConfig(String id) {
        super(id);
    }

    @Override
    public String getStartState() {
        return ChatState.CODE;
    }

    @Override
    public Collection<State> createStates() {
        return Lists.newArrayList(new ChatState(), new DefaultErrorState());
    }
}
