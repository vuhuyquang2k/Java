package com.base.demo.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Distributed Lock using Redis
 * 
 * Usage:
 * 
 * <pre>
 * // Option 1: Manual lock/unlock
 * String lockValue = redisLock.tryLock("order:123", 30, TimeUnit.SECONDS);
 * if (lockValue != null) {
 *     try {
 *         // do work
 *     } finally {
 *         redisLock.unlock("order:123", lockValue);
 *     }
 * }
 * 
 * // Option 2: Auto-release with callback
 * Result result = redisLock.executeWithLock("order:123", 30, TimeUnit.SECONDS, () -> {
 *     return processOrder();
 * });
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockComponent {

    // Dùng StringRedisTemplate để Lua script compare chính xác
    private final StringRedisTemplate stringRedisTemplate;

    // Lua script: chỉ xóa lock nếu value đúng (tránh xóa lock của process khác)
    private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "   return redis.call('del', KEYS[1]) " +
            "else " +
            "   return 0 " +
            "end";

    /**
     * Thử acquire lock
     * 
     * @return lockValue nếu thành công (dùng để unlock), null nếu thất bại
     */
    public String tryLock(String lockKey, long timeout, TimeUnit timeUnit) {
        String lockValue = UUID.randomUUID().toString();
        try {
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, timeout, timeUnit);

            if (acquired != null && acquired) {
                log.debug("Lock acquired: key={}", lockKey);
                return lockValue;
            }
            log.debug("Lock busy: key={}", lockKey);
            return null;
        } catch (Exception e) {
            log.error("Redis lock error: key={}", lockKey, e);
            return null;
        }
    }

    /**
     * Release lock - chỉ unlock nếu đúng owner
     */
    public boolean unlock(String lockKey, String lockValue) {
        if (lockValue == null)
            return false;

        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(UNLOCK_SCRIPT);
            script.setResultType(Long.class);

            Long result = stringRedisTemplate.execute(
                    script,
                    Collections.singletonList(lockKey),
                    lockValue);

            boolean success = Long.valueOf(1).equals(result);
            if (success) {
                log.debug("Lock released: key={}", lockKey);
            }
            return success;
        } catch (Exception e) {
            log.error("Redis unlock error: key={}", lockKey, e);
            return false;
        }
    }

    /**
     * Execute action với auto-release lock
     * 
     * @throws LockAcquisitionException nếu không lấy được lock
     */
    public <T> T executeWithLock(String lockKey, long timeout, TimeUnit timeUnit,
            Supplier<T> action) {
        String lockValue = tryLock(lockKey, timeout, timeUnit);
        if (lockValue == null) {
            throw new LockAcquisitionException("Cannot acquire lock: " + lockKey);
        }
        try {
            return action.get();
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    /**
     * Execute action với auto-release lock (void version)
     */
    public void executeWithLock(String lockKey, long timeout, TimeUnit timeUnit,
            Runnable action) {
        executeWithLock(lockKey, timeout, timeUnit, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Check nếu key đang bị lock
     */
    public boolean isLocked(String lockKey) {
        try {
            Boolean result = stringRedisTemplate.hasKey(lockKey);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis check lock error: key={}", lockKey, e);
            return false;
        }
    }

    // ==================== Exception ====================

    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }
}
