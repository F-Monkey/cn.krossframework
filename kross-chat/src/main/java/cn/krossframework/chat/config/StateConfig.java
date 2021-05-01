package cn.krossframework.chat.config;

import cn.krossframework.chat.config.properties.TimeProperties;
import cn.krossframework.chat.state.ChatRoomFactory;
import cn.krossframework.chat.state.ChatRoomPool;
import cn.krossframework.chat.state.ChatRoomWorkerManager;
import cn.krossframework.state.*;
import cn.krossframework.state.data.WorkerManagerProperties;
import cn.krossframework.state.util.DefaultLazyTime;
import cn.krossframework.state.util.Time;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StateConfig {

    @Bean
    @ConfigurationProperties(prefix = "kross.state")
    WorkerManagerProperties workerManagerProperties() {
        return new WorkerManagerProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "kross.util.time")
    TimeProperties timeProperties() {
        return new TimeProperties();
    }

    @Bean
    Time time(TimeProperties timeProperties) {
        return new DefaultLazyTime(timeProperties.getRefreshPeriod());
    }

    @Bean
    StateGroupFactory stateGroupFactory(Time time) {
        return new ChatRoomFactory(time);
    }

    @Bean
    StateGroupPool stateGroupPool(StateGroupFactory stateGroupFactory) {
        return new ChatRoomPool(stateGroupFactory);
    }

    @Bean
    WorkerManager workerManager(WorkerManagerProperties workerManagerProperties,
                                StateGroupPool stateGroupPool) {
        return new ChatRoomWorkerManager(workerManagerProperties, stateGroupPool);
    }
}
