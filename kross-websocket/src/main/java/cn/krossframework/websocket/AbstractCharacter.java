package cn.krossframework.websocket;

import cn.krossframework.proto.util.CmdUtil;
import com.google.common.base.Preconditions;

public abstract class AbstractCharacter implements Character {

    private volatile Session session;

    private final User user;

    private Long currentGroupId;

    private volatile boolean isOnline;

    public AbstractCharacter(Session session, User user) {
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(user);
        this.session = session;
        this.user = user;
    }

    @Override
    public String getId() {
        return this.user.getUid();
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
    public void setNickName(String nickName) {
        this.user.setUsername(nickName);
    }

    @Override
    public String getNickName() {
        return this.user.getUsername();
    }

    @Override
    public String getHeadIcon() {
        return this.user.getHeadIcon();
    }

    @Override
    public void setSession(Session session) {
        if (this.session != null) {
            this.session.send(CmdUtil.clickOff());
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
