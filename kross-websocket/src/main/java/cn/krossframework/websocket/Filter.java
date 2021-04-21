package cn.krossframework.websocket;

public interface Filter {
    boolean filter(Session session, Command.Cmd cmd);
}
