package cn.krossframework.test.websocket;

import cn.krossframework.proto.Command;
import cn.krossframework.websocket.Dispatcher;
import cn.krossframework.websocket.Session;

public class TestDispatcher implements Dispatcher {
    @Override
    public void dispatch(Session session, Command.Package cmd) {
        System.out.println(cmd.getCmdType());
    }
}
