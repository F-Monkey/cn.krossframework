package cn.krossframework.chat.websocket;

import cn.krossframework.commons.model.ResultCode;
import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.websocket.AbstractCharacter;
import cn.krossframework.websocket.Session;
import cn.krossframework.websocket.User;

public class Chatter extends AbstractCharacter {
    public Chatter(Session session, User user) {
        super(session, user);
    }

    @Override
    public void setSession(Session session) {
        Session oldSession = super.session;
        if (!oldSession.equals(session)) {
            oldSession.send(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "其他地区登录", CmdType.UNKNOWN, null)));
        }
        super.setSession(session);
    }
}
