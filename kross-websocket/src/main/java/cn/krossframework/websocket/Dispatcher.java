package cn.krossframework.websocket;

public interface Dispatcher {
    void dispatch(Session session, Command.Cmd cmd);
}
