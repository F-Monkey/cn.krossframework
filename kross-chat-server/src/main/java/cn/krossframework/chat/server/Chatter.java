package cn.krossframework.chat.server;

import cn.krossframework.chat.cmd.ChatCmdUtil;
import cn.krossframework.websocket.Character;
import cn.krossframework.websocket.Session;

public class Chatter implements Character {

    private volatile Session session;

    private String nickName;

    private Long currentGroupId;

    private volatile boolean isOnline;

    public Chatter(Session session, String nickName) {
        this.session = session;
        this.nickName = nickName;
    }

    @Override
    public String getId() {
        return this.session.getId();
    }

    @Override
    public String getRemoteAddress() {
        return this.session.getRemoteAddress();
    }

    @Override
    public void setCurrentGroupId(long groupId) {
        this.currentGroupId = groupId;
    }

    @Override
    public Long getCurrentGroupId() {
        return this.currentGroupId;
    }

    @Override
    public String getNickName() {
        return this.nickName;
    }

    @Override
    public void setSession(Session session) {
        if (this.session != null) {
            this.session.send(ChatCmdUtil.clickOff());
        }
        this.session = session;
    }

    @Override
    public void sendMsg(Object msg) {
        this.session.send(msg);
    }

    @Override
    public void onLine() {
        this.isOnline = true;
    }

    @Override
    public void offLine() {
        this.isOnline = false;
    }

    @Override
    public boolean isOffLine() {
        return !this.isOnline;
    }
}
