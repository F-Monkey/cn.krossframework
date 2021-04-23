package cn.krossframework.chat.websocket;

import cn.krossframework.websocket.AbstractCharacter;
import cn.krossframework.websocket.Session;
import cn.krossframework.websocket.User;

public class Chatter extends AbstractCharacter {
    public Chatter(Session session, User user) {
        super(session, user);
    }

    @Override
    public void setSession(Session session) {
        super.setSession(session);
    }
}
