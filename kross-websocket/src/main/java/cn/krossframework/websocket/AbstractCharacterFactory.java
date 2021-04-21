package cn.krossframework.websocket;

public class AbstractCharacterFactory implements CharacterFactory {

    @Override
    public Character create(Session session, User user) {
        return new AbstractCharacter(session, user) {
        };
    }
}
