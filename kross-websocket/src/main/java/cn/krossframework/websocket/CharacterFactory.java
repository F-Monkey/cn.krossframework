package cn.krossframework.websocket;

public interface CharacterFactory {

    Character create(Session session, User user);
    
}
