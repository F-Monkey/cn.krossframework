package cn.krossframework.chat.config;

import cn.krossframework.chat.server.ChatterFactor;
import cn.krossframework.chat.server.ChatterPool;
import cn.krossframework.websocket.CharacterFactory;
import cn.krossframework.websocket.CharacterPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfig {
    @Bean
    CharacterFactory characterFactory() {
        return new ChatterFactor();
    }

    @Bean
    CharacterPool characterPool() {
        return new ChatterPool(this.characterFactory());
    }
}
