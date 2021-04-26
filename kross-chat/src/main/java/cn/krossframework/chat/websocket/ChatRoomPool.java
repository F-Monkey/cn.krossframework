package cn.krossframework.chat.websocket;

import cn.krossframework.state.AbstractStateGroupPool;
import cn.krossframework.state.StateGroupFactory;

public class ChatRoomPool extends AbstractStateGroupPool {
    public ChatRoomPool(StateGroupFactory stateGroupFactory) {
        super(stateGroupFactory);
    }
}
