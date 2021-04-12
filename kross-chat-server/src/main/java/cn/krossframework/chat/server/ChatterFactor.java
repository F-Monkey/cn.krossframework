package cn.krossframework.chat.server;

import cn.krossframework.websocket.AbstractCharacterFactory;
import cn.krossframework.commons.web.Character;
import cn.krossframework.commons.web.Session;
import cn.krossframework.websocket.User;

public class ChatterFactor extends AbstractCharacterFactory {
    @Override
    public Character create(Session session, User user) {
        return new Chatter(session, user);
    }
}
