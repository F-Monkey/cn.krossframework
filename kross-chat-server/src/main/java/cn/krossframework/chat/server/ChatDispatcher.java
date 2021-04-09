package cn.krossframework.chat.server;

import cn.krossframework.chat.cmd.ChatCmdUtil;
import cn.krossframework.chat.state.ChatTask;
import cn.krossframework.proto.Chat;
import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.ResultCode;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.state.Worker;
import cn.krossframework.state.WorkerManager;
import cn.krossframework.websocket.Dispatcher;
import cn.krossframework.websocket.Session;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.nio.NioEventLoopGroup;
import cn.krossframework.websocket.Character;
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

    public ChatDispatcher(WorkerManager workerManager) {
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
            }
        } finally {
            lock.unlock();
        }
    }

    private void createRoom(Session session, Command.Cmd cmd) {
        Character character = session.getAttribute(Character.KEY);
        ByteString content = cmd.getContent();
        int cmdType = cmd.getCmdType();
        Chat.CreateRoom createRoom;
        try {
            createRoom = Chat.CreateRoom.parseFrom(content);
        } catch (InvalidProtocolBufferException e) {
            log.error("createRoom parse error");
            character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "invalid content", cmdType, null)));
            return;
        }
        this.workerManager.enter(new Worker.GroupIdTaskPair(createRoom.getRoomId(), new ChatTask(character, cmd), () -> character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "createFail", cmdType, null)))));
    }

    private void sendMessage(Session session, Command.Cmd cmd) {
        Character character = session.getAttribute(Character.KEY);
        long currentGroupId = character.getCurrentGroupId();
        this.workerManager.addTask(new Worker.GroupIdTaskPair(currentGroupId, new ChatTask(character, cmd), () -> character.sendMsg(ChatCmdUtil.sendMsgResult(ResultCode.FAIL, "message send fail", null, null))));
    }

    private void enterRoom(Session session, Command.Cmd cmd) {
        Character character = session.getAttribute(Character.KEY);
        this.workerManager.addTask(new Worker.GroupIdTaskPair(null, null, null));
    }
}
