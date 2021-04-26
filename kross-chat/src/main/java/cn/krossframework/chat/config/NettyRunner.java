package cn.krossframework.chat.config;

import cn.krossframework.websocket.Dispatcher;
import cn.krossframework.websocket.Filter;
import cn.krossframework.websocket.NettyServer;
import cn.krossframework.websocket.ProtoWebSocketHandler;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NettyRunner implements ApplicationRunner, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(NettyRunner.class);

    private static final int DEFAULT_PORT = 8080;

    private final Integer port;

    private ApplicationContext applicationContext;

    public NettyRunner(@Value("${server.port}") Integer port) {
        this.port = port;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.start();
    }

    private void start() throws InterruptedException {
        Integer port = this.port;
        if (port == null) {
            port = DEFAULT_PORT;
            log.info("${server.port} has not configured, use default port:{}", DEFAULT_PORT);
        }
        Map<String, Filter> beans = this.applicationContext.getBeansOfType(Filter.class);
        List<Filter> filters = null;
        if (!beans.isEmpty()) {
            filters = new ArrayList<>(beans.values());
        }
        Dispatcher dispatcher = this.applicationContext.getBean(Dispatcher.class);
        ProtoWebSocketHandler protoWebSocketHandler = new ProtoWebSocketHandler(filters, dispatcher);
        NettyServer nettyServer = new NettyServer(port, "/chat", protoWebSocketHandler);
        nettyServer.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Preconditions.checkNotNull(applicationContext);
        this.applicationContext = applicationContext;
    }
}
