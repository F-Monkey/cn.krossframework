package cn.krossframework.chat.websocket;

import cn.krossframework.state.AbstractStateGroupPool;
import cn.krossframework.state.StateGroupFactory;
import cn.krossframework.state.config.StateGroupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatRoomPool extends AbstractStateGroupPool {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomPool.class);

    public ChatRoomPool(StateGroupFactory stateGroupFactory) {
        super(stateGroupFactory);
    }

    @Override
    public FetchStateGroup findOrCreate(Long id, StateGroupConfig stateGroupConfig) {
        if (id != null && id > ID_COUNT.get()) {
            log.warn("invalid id: {}", id);
            id = this.getNextGroupId();
        }
        return super.findOrCreate(id, stateGroupConfig);
    }
}
