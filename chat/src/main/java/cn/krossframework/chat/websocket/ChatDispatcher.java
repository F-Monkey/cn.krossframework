package cn.krossframework.chat.websocket;

import cn.krossframework.chat.service.IUserService;
import cn.krossframework.proto.Command;
import cn.krossframework.websocket.Dispatcher;
import cn.krossframework.websocket.Session;
import org.springframework.stereotype.Component;

@Component
public class ChatDispatcher implements Dispatcher {

    private final IUserService userService;

    public ChatDispatcher(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void dispatch(Session session, Command.Package cmd) {

    }
}
