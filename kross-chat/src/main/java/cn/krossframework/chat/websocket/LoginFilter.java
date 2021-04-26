package cn.krossframework.chat.websocket;

import cn.krossframework.chat.cmd.ChatCmdType;
import cn.krossframework.commons.model.ResultCode;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.websocket.Character;
import cn.krossframework.websocket.Filter;
import cn.krossframework.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoginFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);

    @Override
    public boolean filter(Session session, Command.Package cmd) {
        Character character = session.getAttribute(Character.KEY);
        if (character == null && cmd.getCmdType() != ChatCmdType.LOGIN) {
            log.error("character has not created but cmd type is not LOGIN type");
            session.send(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "please login", cmd.getCmdType(), null)));
            return false;
        }
        return true;
    }
}
