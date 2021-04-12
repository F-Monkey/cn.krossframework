package cn.krossframework.websocket;

import cn.krossframework.commons.web.Character;
import cn.krossframework.commons.web.Session;

public interface CharacterFactory {

    Character create(Session session, User user);
    
}
