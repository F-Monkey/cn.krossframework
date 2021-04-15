package cn.krossframework.chat.server;

import cn.krossframework.chat.cmd.ChatCmdUtil;
import cn.krossframework.chat.state.ChatTask;
import cn.krossframework.proto.Chat;
import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.ResultCode;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.state.ExecuteTask;
import cn.krossframework.state.WorkerManager;
import cn.krossframework.websocket.CharacterPool;
import cn.krossframework.websocket.Dispatcher;
import cn.krossframework.commons.web.Session;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.nio.NioEventLoopGroup;
import cn.krossframework.commons.web.Character;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;


@Component
public class ChatDispatcher implements Dispatcher {

    private static final Logger log = LoggerFactory.getLogger(ChatDispatcher.class);

    private final WorkerManager workerManager;

    private final LoadingCache<String, ReentrantLock> loadLock;

    private final NioEventLoopGroup eventLoopGroup;

    public ChatDispatcher(WorkerManager workerManager,
                          CharacterPool characterPool) {
        this.workerManager = workerManager;
        this.eventLoopGroup = new NioEventLoopGroup();
        this.loadLock = CacheBuilder.newBuilder()
                .weakValues()
                .build(new CacheLoader<String, ReentrantLock>() {
                    @Override
                    public ReentrantLock load(String key) {
                        return new ReentrantLock();
                    }
                });
    }

    @Override
    public void dispatch(Session session, Command.Cmd cmd) {
        int cmdType = cmd.getCmdType();
        ReentrantLock lock = this.loadLock.getUnchecked(session.getId());
        try {
            lock.tryLock();
            switch (cmdType) {
                case CmdType.CREATE_ROOM:
                    this.eventLoopGroup.submit(() -> this.createRoom(session, cmd));
                    break;
                case CmdType.ENTER:
                    this.eventLoopGroup.submit(() -> this.enterRoom(session, cmd));
                    break;
                case CmdType.SEND_MESSAGE:
                    this.eventLoopGroup.submit(() -> this.sendMessage(session, cmd));
                    break;
                default:
                    log.error("invalid cmdType:{}", cmdType);
                    session.send(CmdUtil.pkg(ResultCode.FAIL, "invalid cmd", cmdType, null));
                    break;
            }
        } finally {
            lock.unlock();
        }
    }

    private void createRoom(Session session, Command.Cmd cmd) {
        Character character = session.getAttribute(Character.KEY);
        ChatTask chatTask = new ChatTask(character, cmd);
        this.workerManager.enter(new ExecuteTask(null,
                chatTask,
                () -> character.sendMsg(ChatCmdUtil.enterRoomResult(ResultCode.FAIL, "room create fail"))));
    }

    private void sendMessage(Session session, Command.Cmd cmd) {
        Character character = session.getAttribute(Character.KEY);
        long currentGroupId = character.getCurrentGroupId();
        this.workerManager.addTask(new ExecuteTask(currentGroupId, new ChatTask(character, cmd), () -> character.sendMsg(ChatCmdUtil.sendMsgResult(ResultCode.FAIL, "message send fail", null, null))));
    }

    private void enterRoom(Session session, Command.Cmd cmd) {
        Character character = session.getAttribute(Character.KEY);
        ChatTask chatTask = new ChatTask(character, cmd);
        this.workerManager.enter(new ExecuteTask(null,
                chatTask,
                () -> character.sendMsg(ChatCmdUtil.enterRoomResult(ResultCode.FAIL, "enter fail"))));
    }
}
