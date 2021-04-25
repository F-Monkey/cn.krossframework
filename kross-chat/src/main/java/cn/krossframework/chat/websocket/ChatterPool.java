package cn.krossframework.chat.websocket;

import cn.krossframework.websocket.AbstractCharacterPool;
import cn.krossframework.websocket.CharacterFactory;
import org.springframework.stereotype.Component;

@Component
public class ChatterPool extends AbstractCharacterPool {

    public ChatterPool(CharacterFactory characterFactory) {
        super(characterFactory);
    }
}
