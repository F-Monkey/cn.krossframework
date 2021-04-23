package cn.krossframework.websocket;

import cn.krossframework.proto.Command;

public interface Filter {
    boolean filter(Session session, Command.Package cmd);
}
