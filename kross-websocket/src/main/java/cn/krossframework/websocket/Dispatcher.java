package cn.krossframework.websocket;

import cn.krossframework.proto.Command;

public interface Dispatcher {
    void dispatch(Session session, Command.Package cmd);
}
