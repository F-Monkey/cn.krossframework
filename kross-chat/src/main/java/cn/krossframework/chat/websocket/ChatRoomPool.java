package cn.krossframework.chat.websocket;

import cn.krossframework.state.AbstractStateGroupPool;
import cn.krossframework.state.StateGroupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatRoomPool extends AbstractStateGroupPool {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomPool.class);

    public ChatRoomPool(StateGroupFactory stateGroupFactory) {
        super(stateGroupFactory);
    }
}
