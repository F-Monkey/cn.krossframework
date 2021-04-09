package cn.krossframework.chat.config;

import cn.krossframework.chat.config.properties.StateProperty;
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

    private final StateProperty stateProperty;

    public StateConfig(StateProperty stateProperty) {
        this.stateProperty = stateProperty;
    }

    @Bean
    Time time() {
        return new DefaultLazyTime(10);
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
        return new ChatWorkerManager(
                this.stateProperty.getWorkerUpdatePeriod(),
                this.stateProperty.getWorkerCapacity(),
                this.stateProperty.getWorkerThreadSize(),
                this.stateProperty.getRemoveEmptyWorkerPeriod(),
                this.stateProperty.getRemoveDeposedStateGroupPeriod(),
                this.stateProperty.getTaskDispatcherSize(),
                stateGroupPool);
    }
}
