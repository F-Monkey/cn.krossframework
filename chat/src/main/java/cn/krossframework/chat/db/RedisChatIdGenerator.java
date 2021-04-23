package cn.krossframework.chat.db;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisChatIdGenerator implements IDGenerator {

    private final String suffix;

    private final int idLength;

    private final String redisKey;

    private final RedisTemplate<String, String> redisTemplate;

    public RedisChatIdGenerator(String suffix,
                                int idLength,
                                String redisKey,
                                RedisTemplate<String, String> redisTemplate) {
        Preconditions.checkNotNull(suffix);
        Preconditions.checkNotNull(redisKey);
        Preconditions.checkArgument(idLength > 0);
        Preconditions.checkNotNull(redisTemplate);
        this.suffix = suffix;
        this.idLength = idLength;
        this.redisKey = redisKey;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public String suffix() {
        return this.suffix;
    }

    @Override
    public String generate() {
        Long increment = this.redisTemplate.opsForValue().increment(this.redisKey);
        if (increment == null) {
            throw new NullPointerException();
        }
        return this.suffix + Strings.padStart(String.valueOf(increment), this.idLength, '0');
    }
}
