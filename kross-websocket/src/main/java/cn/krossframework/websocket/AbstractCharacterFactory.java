package cn.krossframework.websocket;

import cn.krossframework.commons.web.Character;
import cn.krossframework.commons.web.Session;

public class AbstractCharacterFactory implements CharacterFactory {

    @Override
    public Character create(Session session, User user) {
        return new AbstractCharacter(session, user) {
        };
    }
}
