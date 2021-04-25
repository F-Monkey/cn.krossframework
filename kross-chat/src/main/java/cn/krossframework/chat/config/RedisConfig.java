package cn.krossframework.chat.config;

import cn.krossframework.chat.db.IDGenerator;
import cn.krossframework.chat.db.RedisChatIdGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig implements RedisConfigurationAware {

    @ConfigurationProperties(prefix = "spring.data.redis.id")
    @Bean
    RedisProperties idRedisProperties() {
        return new RedisProperties();
    }

    @Bean
    RedisTemplate<String, String> idRedisTemplate(@Qualifier("idRedisProperties") RedisProperties redisProperties) {
        long timeout = redisProperties.getTimeout().toMillis();
        return this.redisTemplate(redisProperties.getHost(), redisProperties.getPort(), redisProperties.getPassword(),
                timeout, timeout, redisProperties.getDatabase());
    }

    @Bean
    IDGenerator redisIdGenerator(@Qualifier("idRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new RedisChatIdGenerator("UID_", 10, "userId", redisTemplate);
    }
}
