package cn.krossframework.websocket;

import io.netty.util.AttributeKey;

public interface Character {

    AttributeKey<Character> KEY = AttributeKey.newInstance("character");

    String getId();

    String getRemoteAddress();

    void setCurrentGroupId(long groupId);

    Long getCurrentGroupId();

    String getNickName();

    void setSession(Session session);

    void sendMsg(Object msg);

    void onLine();

    void offLine();

    boolean isOffLine();
}
