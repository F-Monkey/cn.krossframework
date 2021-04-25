package cn.krossframework.chat.config;

import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

public interface RedisConfigurationAware {
    default LettuceConnectionFactory createLettuceConnectionFactory(String host, int port, String password, long timeout, long shutdownTimeout, int dbIndex) {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(host, port);
        redisConfiguration.setDatabase(dbIndex);
        redisConfiguration.setPassword(password);
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder
                builder = LettucePoolingClientConfiguration.builder().
                commandTimeout(Duration.ofMillis(timeout));

        builder.shutdownTimeout(Duration.ofMillis(shutdownTimeout));
        LettuceClientConfiguration lettuceClientConfiguration = builder.build();

        LettuceConnectionFactory lettuceConnectionFactory = new
                LettuceConnectionFactory(redisConfiguration, lettuceClientConfiguration);
        lettuceConnectionFactory.afterPropertiesSet();
        return lettuceConnectionFactory;
    }

    default RedisTemplate<String, String> redisTemplate(String host, int port, String password, long timeout, long shutdownTimeout, int dbIndex) {
        LettuceConnectionFactory lettuceConnectionFactory = createLettuceConnectionFactory(host, port, password, timeout, shutdownTimeout, dbIndex);
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
