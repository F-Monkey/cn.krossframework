package cn.krossframework.cat.server;

import cn.krossframework.cat.server.state.CatTask;
import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.Command;
import cn.krossframework.state.Worker;
import cn.krossframework.state.WorkerManager;
import cn.krossframework.websocket.Dispatcher;
import cn.krossframework.websocket.Character;
import cn.krossframework.websocket.Session;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
public class CatDispatcher implements Dispatcher {

    private static final Logger log = LoggerFactory.getLogger(CatDispatcher.class);

    private final WorkerManager workerManager;

    private final LoadingCache<String, ReentrantLock> loadLock;

    private final NioEventLoopGroup eventLoopGroup;

    public CatDispatcher(WorkerManager workerManager) {
        this.workerManager = workerManager;
        this.eventLoopGroup = new NioEventLoopGroup();
        this.loadLock = CacheBuilder.newBuilder().weakValues().build(new CacheLoader<String, ReentrantLock>() {
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
                case CmdType.ADD_CAT:
                    this.addCat(session, cmd);
                    break;
                case CmdType.KILL_CAT:
                    this.killCat(session, cmd);
                    break;
                default:
                    log.error("invalid cmdType:{}", cmdType);
            }
        } finally {
            lock.unlock();
        }
    }

    private void killCat(Session session, Command.Cmd cmd) {
        Character character = session.getAttribute(Character.KEY);
        long currentGroupId = character.getCurrentGroupId();
        this.workerManager.addTask(new Worker.GroupIdTaskPair(currentGroupId, new CatTask(cmd)));
    }

    private void addCat(Session session, Command.Cmd cmd) {
        Character character = session.getAttribute(Character.KEY);
        if (character == null) {
            character = new CatPlayer(session, "");
            session.setAttribute(Character.KEY, character);
        }
        this.workerManager.enter(new Worker.GroupIdTaskPair(null, new CatTask(cmd)));
    }
}
