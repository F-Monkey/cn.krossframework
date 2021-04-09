package cn.krossframework.chat.server.config;

import cn.krossframework.chat.state.ChatConfig;
import cn.krossframework.chat.state.ChatRoomFactory;
import cn.krossframework.chat.state.ChatRoomPool;
import cn.krossframework.chat.state.ChatWorkerManager;
import cn.krossframework.state.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StateConfig {

    @Bean
    Time time() {
        return new DefaultLazyTime(100);
    }

    @Bean
    StateGroupFactory stateGroupFactory() {
        return new ChatRoomFactory(this.time(), new ChatConfig());
    }

    @Bean
    StateGroupPool stateGroupPool() throws Exception {
        ChatRoomPool chatRoomPool = new ChatRoomPool(this.stateGroupFactory());
        chatRoomPool.afterPropertiesSet();
        return chatRoomPool;
    }

    @Bean
    @ConditionalOnBean(StateGroupPool.class)
    WorkerManager workerManager(StateGroupPool stateGroupPool) {
        return new ChatWorkerManager(20, 100, 20,
                0, 0, 20, stateGroupPool);
    }
}
