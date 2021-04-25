package cn.krossframework.chat.factory;

import cn.krossframework.chat.websocket.Chatter;
import cn.krossframework.websocket.AbstractCharacterFactory;
import cn.krossframework.websocket.Character;
import cn.krossframework.websocket.Session;
import cn.krossframework.websocket.User;
import org.springframework.stereotype.Component;

@Component
public class ChatterFactory extends AbstractCharacterFactory {

    @Override
    public Character create(Session session, User user) {
        return new Chatter(session, user);
    }
}
