package cn.krossframework.websocket;


import cn.krossframework.commons.web.Session;
import cn.krossframework.proto.Command;

public interface Dispatcher {
    void dispatch(Session session, Command.Cmd cmd);
}
