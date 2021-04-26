package cn.krossframework.chat.websocket;

import cn.krossframework.chat.cmd.ChatCmdType;
import cn.krossframework.chat.cmd.ChatCmdUtil;
import cn.krossframework.chat.service.IUserService;
import cn.krossframework.chat.state.data.ChatRoomConfig;
import cn.krossframework.chat.state.data.ChatTask;
import cn.krossframework.commons.model.Result;
import cn.krossframework.commons.model.ResultCode;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.chat.Chat;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.state.WorkerManager;
import cn.krossframework.state.data.AbstractTask;
import cn.krossframework.state.data.DefaultTask;
import cn.krossframework.state.data.ExecuteTask;
import cn.krossframework.websocket.*;
import cn.krossframework.websocket.Character;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ChatDispatcher implements Dispatcher {

    private static final Logger log = LoggerFactory.getLogger(ChatDispatcher.class);

    private final IUserService userService;

    private final WorkerManager workerManager;

    private final CharacterPool characterPool;

    private final NioEventLoopGroup eventExecutors;

    private final LoadingCache<String, ReentrantLock> lockCache;

    public ChatDispatcher(IUserService userService,
                          WorkerManager workerManager,
                          CharacterPool characterPool) {
        this.userService = userService;
        this.workerManager = workerManager;
        this.characterPool = characterPool;
        this.eventExecutors = new NioEventLoopGroup();
        this.lockCache = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(1))
                .build(new CacheLoader<String, ReentrantLock>() {
                    @Override
                    public ReentrantLock load(String key) throws Exception {
                        return new ReentrantLock();
                    }
                });
    }

    @Override
    public void dispatch(Session session, Command.Package cmd) {
        int cmdType = cmd.getCmdType();
        ReentrantLock lock = this.lockCache.getUnchecked(session.getId());
        if (!lock.tryLock()) {
            return;
        }
        try {
            switch (cmdType) {
                case ChatCmdType.LOGIN:
                    this.eventExecutors.submit(() -> this.login(session, cmd));
                    return;
                case ChatCmdType.CREATE_ROOM:
                    this.eventExecutors.submit(() -> this.createRoom(session, cmd));
                    return;
                case ChatCmdType.ENTER_ROOM:
                    this.eventExecutors.submit(() -> this.enterRoom(session, cmd));
                    return;
                default:
                    this.eventExecutors.submit(() -> this.trySendTask(session, cmd));
            }
        } finally {
            lock.unlock();
        }
    }

    private void enterRoom(Session session, Command.Package cmd) {
        Character character = session.getAttribute(Character.KEY);
        ChatTask chatTask = new ChatTask(character, cmd);
        Long groupId = null;
        try {
            Chat.Enter enter = Chat.Enter.parseFrom(cmd.getContent());
            if (enter.getRoomId() != 0) {
                groupId = enter.getRoomId();
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("invalid enter content");
        }
        if (groupId == null) {
            character.sendMsg(ChatCmdUtil.enterRoomResult(ResultCode.FAIL, "enter room fail[0]", null));
            log.error("enter room fail, cause group id is empty");
            return;
        }
        AbstractTask task = new DefaultTask(groupId, chatTask, () -> {
            character.sendMsg(ChatCmdUtil.enterRoomResult(ResultCode.FAIL, "enter room fail[1]", null));
        });
        this.workerManager.enter(task, new ChatRoomConfig("1"));
    }

    private void trySendTask(Session session, Command.Package cmd) {
        Character character = session.getAttribute(Character.KEY);
        Long currentGroupId = character.getCurrentGroupId();
        if (currentGroupId == null) {
            character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "character has not join home yet", ChatCmdType.LOGIN_RESULT, null)));
            return;
        }
        int cmdType = cmd.getCmdType();
        ExecuteTask executeTask;
        try {
            ChatTask chatTask = new ChatTask(character, cmd);
            executeTask = new ExecuteTask(currentGroupId, chatTask, () -> {
                character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "invalid cmd type:" + cmdType, cmdType, null)));
            });
        } catch (Exception e) {
            character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "bad data", cmdType, null)));
            log.error("task create error:\n", e);
            return;
        }
        this.workerManager.addTask(executeTask);
    }

    private void createRoom(Session session, Command.Package cmd) {
        Character character = session.getAttribute(Character.KEY);
        ChatTask chatTask = new ChatTask(character, cmd);
        AbstractTask task = new DefaultTask(null, chatTask, () -> {
            character.sendMsg(ChatCmdUtil.enterRoomResult(ResultCode.FAIL, "room create fail", null));
        });
        this.workerManager.enter(task, new ChatRoomConfig("1"));
    }

    private void login(Session session, Command.Package cmd) {
        Chat.Login login;
        ByteString content = cmd.getContent();
        try {
            login = Chat.Login.parseFrom(content);
        } catch (InvalidProtocolBufferException e) {
            log.error("login error,invalid content data:\n", e);
            session.send(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.ERROR, "invalid login content", ChatCmdType.LOGIN_RESULT, null)));
            return;
        }
        String uid = login.getUid();
        String username = login.getUsername();
        Result<User> userResult;
        if (!Strings.isNullOrEmpty(uid)) {
            userResult = this.userService.loginByUid(uid);
            if (userResult.getCode() != ResultCode.SUCCESS) {
                session.send(ChatCmdUtil.loginResult(ResultCode.FAIL, "登录失败", null));
                return;
            }
            CharacterPool.FetchCharacter fetchCharacter = this.characterPool.findOrCreate(session, userResult.getData());
            Character character = fetchCharacter.getCharacter();
            if (!fetchCharacter.isNew()) {
                character.setSession(session);
            }
            session.setAttribute(Character.KEY, character);
            session.send(ChatCmdUtil.loginResult(ResultCode.SUCCESS, "登录成功", userResult.getData()));
            return;
        }
        if (!Strings.isNullOrEmpty(username)) {
            userResult = this.userService.loginByUsername(username);
            if (userResult.getCode() != ResultCode.SUCCESS) {
                session.send(ChatCmdUtil.loginResult(ResultCode.FAIL, "登录失败", null));
                return;
            }
            CharacterPool.FetchCharacter fetchCharacter = this.characterPool.findOrCreate(session, userResult.getData());
            Character character = fetchCharacter.getCharacter();
            if (!fetchCharacter.isNew()) {
                character.setSession(session);
            }
            session.setAttribute(Character.KEY, character);
            session.send(ChatCmdUtil.loginResult(ResultCode.SUCCESS, "登录成功", userResult.getData()));
        }
    }
}
