package com.base.demo.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisComponent {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // ==================== STRING ====================

    /**
     * Lưu giá trị vào Redis với thời gian hết hạn (TTL).
     *
     * @param key      Key để lưu trữ
     * @param value    Giá trị cần lưu (có thể là bất kỳ Object nào)
     * @param timeout  Thời gian tồn tại của key
     * @param timeUnit Đơn vị thời gian (SECONDS, MINUTES, HOURS, ...)
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis SET error: key={}", key, e);
        }
    }

    /**
     * Lưu giá trị vào Redis không có thời gian hết hạn (persistent).
     *
     * @param key   Key để lưu trữ
     * @param value Giá trị cần lưu (có thể là bất kỳ Object nào)
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis SET error: key={}", key, e);
        }
    }

    /**
     * Lấy giá trị từ Redis theo key.
     *
     * @param key Key cần lấy giá trị
     * @return Giá trị được lưu trữ, hoặc null nếu key không tồn tại hoặc có lỗi
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis GET error: key={}", key, e);
            return null;
        }
    }

    /**
     * Lấy giá trị từ Redis và tự động convert sang kiểu dữ liệu mong muốn.
     *
     * <p>
     * Method này sử dụng Jackson ObjectMapper để convert giá trị nếu cần thiết.
     *
     * @param <T>   Kiểu dữ liệu mong muốn
     * @param key   Key cần lấy giá trị
     * @param clazz Class của kiểu dữ liệu cần convert
     * @return Giá trị đã được convert, hoặc null nếu key không tồn tại hoặc có lỗi
     */
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

    /**
     * Lấy giá trị từ Redis và convert sang generic type (ví dụ: List, Map).
     *
     * <p>
     * Sử dụng TypeReference để xử lý các kiểu generic như {@code List<User>}
     * hoặc {@code Map<String, Object>}.
     *
     * @param <T>     Kiểu dữ liệu mong muốn
     * @param key     Key cần lấy giá trị
     * @param typeRef TypeReference mô tả kiểu dữ liệu cần convert
     * @return Giá trị đã được convert, hoặc null nếu key không tồn tại hoặc có lỗi
     */
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

    /**
     * Xóa một key khỏi Redis.
     *
     * @param key Key cần xóa
     * @return true nếu xóa thành công, false nếu key không tồn tại hoặc có lỗi
     */
    public boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis DELETE error: key={}", key, e);
            return false;
        }
    }

    /**
     * Kiểm tra xem key có tồn tại trong Redis hay không.
     *
     * @param key Key cần kiểm tra
     * @return true nếu key tồn tại, false nếu không tồn tại hoặc có lỗi
     */
    public boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis EXISTS error: key={}", key, e);
            return false;
        }
    }

    /**
     * Đặt thời gian hết hạn (TTL) cho một key đã tồn tại.
     *
     * @param key      Key cần đặt TTL
     * @param timeout  Thời gian tồn tại
     * @param timeUnit Đơn vị thời gian
     * @return true nếu đặt TTL thành công, false nếu key không tồn tại hoặc có lỗi
     */
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis EXPIRE error: key={}", key, e);
            return false;
        }
    }

    // ==================== HASH ====================

    /**
     * Lưu một field-value vào Redis Hash.
     *
     * <p>
     * Hash cho phép lưu trữ nhiều field trong cùng một key,
     * phù hợp để lưu các object có nhiều thuộc tính.
     *
     * @param key   Key của Hash
     * @param field Tên field trong Hash
     * @param value Giá trị của field
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Redis HSET error: key={}, field={}", key, field, e);
        }
    }

    /**
     * Lấy giá trị của một field trong Redis Hash.
     *
     * @param key   Key của Hash
     * @param field Tên field cần lấy
     * @return Giá trị của field, hoặc null nếu không tồn tại hoặc có lỗi
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Redis HGET error: key={}, field={}", key, field, e);
            return null;
        }
    }

    /**
     * Lấy tất cả các field-value trong một Redis Hash.
     *
     * @param key Key của Hash
     * @return Map chứa tất cả field-value, hoặc empty Map nếu có lỗi
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis HGETALL error: key={}", key, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Tăng giá trị của một field trong Hash theo delta.
     *
     * <p>
     * Field phải chứa giá trị số. Nếu field chưa tồn tại,
     * nó sẽ được tạo với giá trị = delta.
     *
     * @param key   Key của Hash
     * @param field Field cần tăng giá trị
     * @param delta Giá trị cần tăng (có thể âm để giảm)
     * @return Giá trị mới sau khi tăng, hoặc null nếu có lỗi
     */
    public Long hIncrement(String key, String field, long delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("Redis HINCRBY error: key={}, field={}", key, field, e);
            return null;
        }
    }

    // ==================== LIST ====================

    /**
     * Thêm một hoặc nhiều phần tử vào cuối danh sách (right push).
     *
     * <p>
     * Nếu key chưa tồn tại, một danh sách mới sẽ được tạo.
     * Kết hợp với lPop() để tạo queue FIFO.
     *
     * @param key    Key của danh sách
     * @param values Các giá trị cần thêm vào cuối danh sách
     * @return Số lượng phần tử trong danh sách sau khi thêm, hoặc null nếu có lỗi
     */
    public Long rPush(String key, Object... values) {
        try {
            return redisTemplate.opsForList().rightPushAll(key, values);
        } catch (Exception e) {
            log.error("Redis RPUSH error: key={}", key, e);
            return null;
        }
    }

    /**
     * Lấy và xóa phần tử đầu tiên của danh sách (left pop).
     *
     * <p>
     * Thường được dùng trong pattern queue (FIFO - First In First Out).
     *
     * @param key Key của danh sách
     * @return Phần tử đầu tiên, hoặc null nếu danh sách rỗng hoặc có lỗi
     */
    public Object lPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("Redis LPOP error: key={}", key, e);
            return null;
        }
    }

    /**
     * Lấy các phần tử trong một khoảng của danh sách (không xóa).
     *
     * <p>
     * Index 0 là phần tử đầu tiên, -1 là phần tử cuối cùng.
     * Ví dụ: lRange(key, 0, -1) lấy toàn bộ danh sách.
     *
     * @param key   Key của danh sách
     * @param start Index bắt đầu (inclusive, 0-based)
     * @param end   Index kết thúc (inclusive, có thể âm)
     * @return Danh sách các phần tử, hoặc empty list nếu có lỗi
     */
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

    /**
     * Thêm một hoặc nhiều phần tử vào Redis Set.
     *
     * <p>
     * Set không cho phép phần tử trùng lặp và không có thứ tự.
     *
     * @param key    Key của Set
     * @param values Các giá trị cần thêm
     * @return Số phần tử mới được thêm (không tính trùng lặp), hoặc null nếu có lỗi
     */
    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis SADD error: key={}", key, e);
            return null;
        }
    }

    /**
     * Kiểm tra xem một giá trị có tồn tại trong Set hay không.
     *
     * @param key   Key của Set
     * @param value Giá trị cần kiểm tra
     * @return true nếu giá trị tồn tại trong Set, false nếu không hoặc có lỗi
     */
    public boolean sIsMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("Redis SISMEMBER error: key={}", key, e);
            return false;
        }
    }

    /**
     * Lấy tất cả các phần tử trong Redis Set.
     *
     * @param key Key của Set
     * @return Set chứa tất cả phần tử, hoặc empty Set nếu có lỗi
     */
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

    /**
     * Tăng giá trị của key lên 1 (atomic increment).
     *
     * <p>
     * Nếu key chưa tồn tại, nó sẽ được khởi tạo với giá trị 0 trước khi tăng.
     * Operation này là atomic, an toàn cho concurrent access.
     *
     * @param key Key cần tăng giá trị
     * @return Giá trị mới sau khi tăng, hoặc null nếu có lỗi
     */
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Redis INCR error: key={}", key, e);
            return null;
        }
    }

    /**
     * Tăng giá trị của key theo delta (atomic increment by).
     *
     * <p>
     * Nếu key chưa tồn tại, nó sẽ được khởi tạo với giá trị 0 trước khi tăng.
     * Operation này là atomic, an toàn cho concurrent access.
     * Delta có thể là số âm để giảm giá trị.
     *
     * @param key   Key cần tăng giá trị
     * @param delta Giá trị cần tăng (có thể âm)
     * @return Giá trị mới sau khi tăng, hoặc null nếu có lỗi
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis INCRBY error: key={}", key, e);
            return null;
        }
    }

    // ==================== CACHE-ASIDE PATTERN ====================

    /**
     * Lấy giá trị từ cache, nếu không có thì load từ source và cache lại.
     *
     * <p>
     * Đây là implementation của Cache-Aside pattern (Lazy Loading):
     * <ol>
     * <li>Kiểm tra cache, nếu có thì trả về ngay</li>
     * <li>Nếu cache miss, gọi loader để lấy data từ source (DB, API, ...)</li>
     * <li>Lưu kết quả vào cache (nếu không null)</li>
     * <li>Trả về kết quả</li>
     * </ol>
     *
     * @param <T>    Kiểu dữ liệu
     * @param key    Cache key
     * @param clazz  Class của kiểu dữ liệu
     * @param loader Supplier để load data khi cache miss
     * @return Giá trị từ cache hoặc từ loader
     */
    public <T> T getOrLoad(String key, Class<T> clazz, java.util.function.Supplier<T> loader) {
        T cached = get(key, clazz);
        if (cached != null) {
            return cached;
        }
        T loaded = loader.get();
        if (loaded != null) {
            set(key, loaded);
        }
        return loaded;
    }

    /**
     * Lấy giá trị từ cache với TTL, nếu không có thì load từ source và cache lại.
     *
     * @param <T>      Kiểu dữ liệu
     * @param key      Cache key
     * @param clazz    Class của kiểu dữ liệu
     * @param timeout  TTL của cache
     * @param timeUnit Đơn vị thời gian
     * @param loader   Supplier để load data khi cache miss
     * @return Giá trị từ cache hoặc từ loader
     */
    public <T> T getOrLoad(String key, Class<T> clazz, long timeout, TimeUnit timeUnit,
            java.util.function.Supplier<T> loader) {
        T cached = get(key, clazz);
        if (cached != null) {
            return cached;
        }
        T loaded = loader.get();
        if (loaded != null) {
            set(key, loaded, timeout, timeUnit);
        }
        return loaded;
    }

    // ==================== BULK OPERATIONS ====================

    /**
     * Lưu nhiều key-value cùng lúc (atomic operation).
     *
     * <p>
     * Hiệu quả hơn việc gọi set() nhiều lần vì chỉ cần 1 round-trip tới Redis.
     *
     * @param keyValues Map chứa các key-value cần lưu
     */
    public void multiSet(Map<String, Object> keyValues) {
        try {
            redisTemplate.opsForValue().multiSet(keyValues);
        } catch (Exception e) {
            log.error("Redis MSET error: keys={}", keyValues.keySet(), e);
        }
    }

    /**
     * Lấy nhiều giá trị cùng lúc theo danh sách keys.
     *
     * <p>
     * Hiệu quả hơn việc gọi get() nhiều lần vì chỉ cần 1 round-trip tới Redis.
     * Thứ tự values trả về tương ứng với thứ tự keys đầu vào.
     *
     * @param keys Collection các keys cần lấy
     * @return List các giá trị (có thể chứa null cho keys không tồn tại)
     */
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
