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
 * Redis Component - Wrapper cho các Redis operations thông dụng.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisComponent {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // ==================== STRING ====================

    /** Lưu value với TTL. */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis SET error: key={}", key, e);
        }
    }

    /** Lưu value không TTL (persistent). */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis SET error: key={}", key, e);
        }
    }

    /** Lấy value theo key. */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis GET error: key={}", key, e);
            return null;
        }
    }

    /** Lấy value và convert sang type mong muốn. */
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

    /** Lấy value với generic type (List, Map...). */
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

    /** Xóa key. */
    public boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis DELETE error: key={}", key, e);
            return false;
        }
    }

    /** Kiểm tra key tồn tại. */
    public boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis EXISTS error: key={}", key, e);
            return false;
        }
    }

    /** Đặt TTL cho key. */
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis EXPIRE error: key={}", key, e);
            return false;
        }
    }

    // ==================== HASH ====================

    /** Lưu field-value vào Hash. */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Redis HSET error: key={}, field={}", key, field, e);
        }
    }

    /** Lấy value của field trong Hash. */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Redis HGET error: key={}, field={}", key, field, e);
            return null;
        }
    }

    /** Lấy tất cả field-value trong Hash. */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis HGETALL error: key={}", key, e);
            return Collections.emptyMap();
        }
    }

    /** Tăng giá trị field trong Hash. */
    public Long hIncrement(String key, String field, long delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("Redis HINCRBY error: key={}, field={}", key, field, e);
            return null;
        }
    }

    // ==================== LIST ====================

    /** Thêm phần tử vào cuối list (RPUSH). */
    public Long rPush(String key, Object... values) {
        try {
            return redisTemplate.opsForList().rightPushAll(key, values);
        } catch (Exception e) {
            log.error("Redis RPUSH error: key={}", key, e);
            return null;
        }
    }

    /** Lấy và xóa phần tử đầu list (LPOP). */
    public Object lPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("Redis LPOP error: key={}", key, e);
            return null;
        }
    }

    /** Lấy các phần tử trong khoảng [start, end]. */
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

    /** Thêm phần tử vào Set. */
    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis SADD error: key={}", key, e);
            return null;
        }
    }

    /** Kiểm tra value có trong Set không. */
    public boolean sIsMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("Redis SISMEMBER error: key={}", key, e);
            return false;
        }
    }

    /** Lấy tất cả phần tử trong Set. */
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

    /** Tăng giá trị lên 1 (atomic). */
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Redis INCR error: key={}", key, e);
            return null;
        }
    }

    /** Tăng giá trị theo delta (atomic). */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis INCRBY error: key={}", key, e);
            return null;
        }
    }

    // ==================== CACHE-ASIDE ====================

    /** Cache-aside: lấy từ cache, nếu miss thì load và cache lại. */
    public <T> T getOrLoad(String key, Class<T> clazz, java.util.function.Supplier<T> loader) {
        T cached = get(key, clazz);
        if (cached != null)
            return cached;
        T loaded = loader.get();
        if (loaded != null)
            set(key, loaded);
        return loaded;
    }

    /** Cache-aside với TTL. */
    public <T> T getOrLoad(String key, Class<T> clazz, long timeout, TimeUnit timeUnit,
            java.util.function.Supplier<T> loader) {
        T cached = get(key, clazz);
        if (cached != null)
            return cached;
        T loaded = loader.get();
        if (loaded != null)
            set(key, loaded, timeout, timeUnit);
        return loaded;
    }

    // ==================== BULK ====================

    /** Lưu nhiều key-value cùng lúc (MSET). */
    public void multiSet(Map<String, Object> keyValues) {
        try {
            redisTemplate.opsForValue().multiSet(keyValues);
        } catch (Exception e) {
            log.error("Redis MSET error: keys={}", keyValues.keySet(), e);
        }
    }

    /** Lấy nhiều values cùng lúc (MGET). */
    public List<Object> multiGet(Collection<String> keys) {
        try {
            List<Object> result = redisTemplate.opsForValue().multiGet(keys);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.error("Redis MGET error: keys={}", keys, e);
            return Collections.emptyList();
        }
    }
}
