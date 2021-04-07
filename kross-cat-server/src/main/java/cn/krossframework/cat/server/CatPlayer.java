package cn.krossframework.cat.server;

import cn.krossframework.websocket.Character;
import cn.krossframework.websocket.Session;

public class CatPlayer implements Character {

    private Session session;

    private Long currentGroupId;

    private String nickName;

    private boolean isOnLine;

    public CatPlayer(Session session,
                     String nickName) {
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
    public long getCurrentGroupId() {
        return this.currentGroupId;
    }

    @Override
    public String getNickName() {
        return this.nickName;
    }

    @Override
    public void setSession(Session session) {
        Session oldSession = this.session;
        if (oldSession != null) {
        }
        this.session = session;
    }

    @Override
    public void sendMsg(Object msg) {
        this.session.send(msg);
    }

    @Override
    public void onLine() {
        this.isOnLine = true;
    }

    @Override
    public void offLine() {
        this.isOnLine = false;
    }

    @Override
    public boolean isOffLine() {
        return !this.isOnLine;
    }
}
