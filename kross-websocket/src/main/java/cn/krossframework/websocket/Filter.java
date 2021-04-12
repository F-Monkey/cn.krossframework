package cn.krossframework.websocket;

import cn.krossframework.commons.web.Session;
import cn.krossframework.proto.Command;

public interface Filter {
    boolean filter(Session session, Command.Cmd cmd);
}
