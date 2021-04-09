package cn.krossframework.chat.state.chat;

import cn.krossframework.state.State;
import cn.krossframework.state.StateGroupConfig;
import com.google.common.collect.Lists;

import java.util.Collection;

public class ChatConfig implements StateGroupConfig {



    @Override
    public Collection<State> getStates() {
        return Lists.newArrayList(new ChatState());
    }
}
