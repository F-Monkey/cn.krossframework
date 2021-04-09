package cn.krossframework.chat.server;

import cn.krossframework.websocket.AbstractCharacterPool;
import cn.krossframework.websocket.CharacterFactory;

public class ChatterPool extends AbstractCharacterPool {

    public ChatterPool(CharacterFactory characterFactory) {
        super(characterFactory);
    }
}
