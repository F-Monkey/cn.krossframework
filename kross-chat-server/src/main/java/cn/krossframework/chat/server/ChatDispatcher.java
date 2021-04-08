package cn.krossframework.chat.server;

import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.Command;
import cn.krossframework.state.Worker;
import cn.krossframework.state.WorkerManager;
import cn.krossframework.websocket.Dispatcher;
import cn.krossframework.websocket.Session;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
        this.workerManager.enter(new Worker.GroupIdTaskPair(null, null, null));
    }

    private void sendMessage(Session session, Command.Cmd cmd) {
        Character character = session.getAttribute(Character.KEY);
        long currentGroupId = character.getCurrentGroupId();
    }

    private void enterRoom(Session session, Command.Cmd cmd) {
        Character character = session.getAttribute(Character.KEY);
        this.workerManager.addTask(new Worker.GroupIdTaskPair(null, null, null));
    }
}
