package com.ncs.order.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

/**
 * 基于 Redis 的分布式锁（SET NX EX + Lua 原子释放）
 */
@Component
public class RedisLock {

    private static final String RELEASE_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(RELEASE_SCRIPT, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试加锁，成功返回 true
     */
    public boolean tryLock(String key, String value, long expireSeconds) {
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(expireSeconds));
        return Boolean.TRUE.equals(ok);
    }

    /**
     * 释放锁（仅当 value 匹配时才删除，避免误删他人锁）
     */
    public void unlock(String key, String value) {
        redisTemplate.execute(RELEASE, Collections.singletonList(key), value);
    }
}
