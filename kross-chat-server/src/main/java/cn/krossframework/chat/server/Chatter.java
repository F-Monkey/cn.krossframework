package cn.krossframework.chat.server;

import cn.krossframework.websocket.AbstractCharacter;
import cn.krossframework.commons.web.Session;
import cn.krossframework.websocket.User;

public class Chatter extends AbstractCharacter {

    public Chatter(Session session, User user) {
        super(session, user);
    }
}
