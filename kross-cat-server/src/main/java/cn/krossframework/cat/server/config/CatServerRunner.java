package cn.krossframework.cat.server.config;

import cn.krossframework.cat.server.state.*;
import cn.krossframework.state.*;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class CatServerRunner implements ApplicationRunner, ApplicationContextAware {

    @Value("${spring.netty.port}")
    int port;

    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.run();
    }

    private void run() throws InterruptedException {
        Map<String, Filter> filterMap = this.applicationContext.getBeansOfType(Filter.class);
        List<Filter> filters = null;
        if (filterMap.size() > 0) {
            filters = filterMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        ProtoWebSocketHandler webSocketHandler = new ProtoWebSocketHandler(filters, this.applicationContext.getBean(Dispatcher.class));
        new NettyServer(this.port, webSocketHandler).start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Preconditions.checkNotNull(applicationContext);
        this.applicationContext = applicationContext;
    }


    @Bean
    WorkerManager workerManager() throws Exception {
        DefaultLazyTime defaultLazyTime = new DefaultLazyTime(100);
        CatFactory catFactory = new CatFactory(defaultLazyTime, () -> true) {
            @Override
            public StateGroup create(long id) {
                StateGroup stateGroup = new Cat(id, super.time, super.stateGroupConfig);
                stateGroup.addState(new Sleep());
                stateGroup.addState(new Eat());
                stateGroup.addState(new Walk());
                stateGroup.addState(new DefaultErrorState());
                stateGroup.initAndSetCurrentState(Walk.CODE);
                return stateGroup;
            }
        };
        CatPool catPool = new CatPool(catFactory);
        catPool.afterPropertiesSet();
        return new AbstractWorkerManager(500, 20, 10, 2000, 2000, catPool);
    }
}
