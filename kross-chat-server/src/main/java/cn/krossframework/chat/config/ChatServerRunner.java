package cn.krossframework.chat.config;

import cn.krossframework.websocket.Dispatcher;
import cn.krossframework.websocket.Filter;
import cn.krossframework.websocket.NettyServer;
import cn.krossframework.websocket.ProtoWebSocketHandler;
import com.google.common.base.Preconditions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ChatServerRunner implements ApplicationContextAware, ApplicationRunner {

    @Value("${spring.netty.port}")
    int port;

    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, Filter> filterMap = this.applicationContext.getBeansOfType(Filter.class);
        List<Filter> filters = null;
        if (filterMap.size() > 0) {
            filters = filterMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        ProtoWebSocketHandler webSocketHandler = new ProtoWebSocketHandler(filters,
                this.applicationContext.getBean(Dispatcher.class));
        new NettyServer(this.port, webSocketHandler).start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Preconditions.checkNotNull(applicationContext);
        this.applicationContext = applicationContext;
    }
}
