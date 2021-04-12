package cn.krossframework.commons.web;

import io.netty.util.AttributeKey;

public interface Session {

    AttributeKey<Session> KEY = AttributeKey.newInstance("session");

    String getId();

    String getRemoteAddress();

    <T> T getAttribute(AttributeKey<T> key);

    <T> void setAttribute(AttributeKey<T> key, T val);

    void send(Object data);

    boolean isAlive();
}
