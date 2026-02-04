package com.base.demo.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis Component - Operations cơ bản cho toàn dự án
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisComponent {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // ==================== STRING ====================

    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis SET error: key={}", key, e);
        }
    }

    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis SET error: key={}", key, e);
        }
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis GET error: key={}", key, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = get(key);
            if (value == null)
                return null;
            if (clazz.isInstance(value))
                return (T) value;
            return objectMapper.convertValue(value, clazz);
        } catch (Exception e) {
            log.error("Redis GET error: key={}", key, e);
            return null;
        }
    }

    public <T> T get(String key, TypeReference<T> typeRef) {
        try {
            Object value = get(key);
            if (value == null)
                return null;
            return objectMapper.convertValue(value, typeRef);
        } catch (Exception e) {
            log.error("Redis GET error: key={}", key, e);
            return null;
        }
    }

    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis DELETE error: key={}", key, e);
            return false;
        }
    }

    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis EXISTS error: key={}", key, e);
            return false;
        }
    }

    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, timeUnit);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis EXPIRE error: key={}", key, e);
            return false;
        }
    }

    // ==================== HASH ====================

    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Redis HSET error: key={}, field={}", key, field, e);
        }
    }

    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Redis HGET error: key={}, field={}", key, field, e);
            return null;
        }
    }

    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis HGETALL error: key={}", key, e);
            return Collections.emptyMap();
        }
    }

    public Long hIncrement(String key, String field, long delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("Redis HINCRBY error: key={}, field={}", key, field, e);
            return null;
        }
    }

    // ==================== LIST ====================

    public Long lPush(String key, Object... values) {
        try {
            return redisTemplate.opsForList().rightPushAll(key, values);
        } catch (Exception e) {
            log.error("Redis RPUSH error: key={}", key, e);
            return null;
        }
    }

    public Object lPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("Redis LPOP error: key={}", key, e);
            return null;
        }
    }

    public List<Object> lRange(String key, long start, long end) {
        try {
            List<Object> result = redisTemplate.opsForList().range(key, start, end);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.error("Redis LRANGE error: key={}", key, e);
            return Collections.emptyList();
        }
    }

    // ==================== SET ====================

    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis SADD error: key={}", key, e);
            return null;
        }
    }

    public boolean sIsMember(String key, Object value) {
        try {
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis SISMEMBER error: key={}", key, e);
            return false;
        }
    }

    public Set<Object> sMembers(String key) {
        try {
            Set<Object> result = redisTemplate.opsForSet().members(key);
            return result != null ? result : Collections.emptySet();
        } catch (Exception e) {
            log.error("Redis SMEMBERS error: key={}", key, e);
            return Collections.emptySet();
        }
    }

    // ==================== COUNTER ====================

    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Redis INCR error: key={}", key, e);
            return null;
        }
    }

    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis INCRBY error: key={}", key, e);
            return null;
        }
    }
}
