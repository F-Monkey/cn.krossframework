package cn.krossframework.chat.server;

import cn.krossframework.websocket.AbstractCharacterFactory;
import cn.krossframework.websocket.Character;
import cn.krossframework.websocket.Session;
import cn.krossframework.websocket.User;

public class ChatterFactor extends AbstractCharacterFactory {
    @Override
    public Character create(Session session, User user) {
        return new Chatter(session, user);
    }
}
