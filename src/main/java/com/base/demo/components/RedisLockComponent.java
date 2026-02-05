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
 * Redis Distributed Lock Component - Cung cấp cơ chế khóa phân tán sử dụng
 * Redis.
 * 
 * <p>
 * Component này implement distributed locking pattern để đảm bảo mutual
 * exclusion
 * trong môi trường distributed (nhiều instances, nhiều servers).
 * 
 * <h2>Đặc điểm chính:</h2>
 * <ul>
 * <li><b>Atomic Lock</b>: Sử dụng Redis SETNX (SET if Not Exists) để acquire
 * lock atomic</li>
 * <li><b>TTL Auto-expiry</b>: Lock tự động hết hạn để tránh deadlock khi
 * process crash</li>
 * <li><b>Safe Unlock</b>: Sử dụng Lua script để đảm bảo chỉ owner mới có thể
 * unlock</li>
 * <li><b>Retry with Backoff</b>: Hỗ trợ retry với exponential backoff khi lock
 * busy</li>
 * </ul>
 * 
 * <h2>Ví dụ sử dụng:</h2>
 * 
 * <pre>{@code
 * // Cách 1: Manual lock/unlock
 * String lockValue = redisLockComponent.tryLock("order:123", 30, TimeUnit.SECONDS);
 * if (lockValue != null) {
 *     try {
 *         // Critical section
 *     } finally {
 *         redisLockComponent.unlock("order:123", lockValue);
 *     }
 * }
 * 
 * // Cách 2: Auto-release với lambda (recommended)
 * redisLockComponent.executeWithLock("order:123", 30, 5, TimeUnit.SECONDS, () -> {
 *     // Critical section - lock sẽ tự động release khi hoàn thành hoặc exception
 *     return processOrder();
 * });
 * }</pre>
 * 
 * <h2>Lưu ý quan trọng:</h2>
 * <ul>
 * <li>Lock TTL nên lớn hơn thời gian xử lý dự kiến để tránh lock hết hạn giữa
 * chừng</li>
 * <li>Component này KHÔNG implement lock renewal (watch dog) - cân nhắc nếu
 * cần</li>
 * <li>Trong môi trường Redis Cluster, lock chỉ hoạt động trên single master
 * node</li>
 * </ul>
 * 
 * @see StringRedisTemplate
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockComponent {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Lua script để giải phóng lock một cách an toàn.
     * 
     * <p>
     * Script này đảm bảo atomicity khi unlock:
     * <ol>
     * <li>Kiểm tra value hiện tại của lock có khớp với lockValue không</li>
     * <li>Chỉ xóa lock nếu value khớp (đúng owner)</li>
     * <li>Return 1 nếu unlock thành công, 0 nếu lock không thuộc về caller</li>
     * </ol>
     * 
     * <p>
     * Điều này ngăn chặn scenario nguy hiểm: Process A lock, chạy lâu, lock hết
     * hạn,
     * Process B acquire lock mới, Process A xong việc và xóa mất lock của Process
     * B.
     */
    private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "   return redis.call('del', KEYS[1]) " +
            "else " +
            "   return 0 " +
            "end";

    /** Pre-compiled Lua script object để tránh tạo mới mỗi lần unlock */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT_OBJECT = createUnlockScript();

    private static DefaultRedisScript<Long> createUnlockScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(UNLOCK_SCRIPT);
        script.setResultType(Long.class);
        return script;
    }

    /** Khoảng thời gian retry ban đầu (50ms) */
    private static final long DEFAULT_RETRY_INTERVAL_MS = 50;

    /** Khoảng thời gian retry tối đa sau exponential backoff (500ms) */
    private static final long MAX_RETRY_INTERVAL_MS = 500;

    // ==================== VALIDATION ====================

    /**
     * Validate các tham số lock.
     *
     * @param lockKey Key của lock
     * @param timeout Thời gian timeout
     * @throws IllegalArgumentException nếu tham số không hợp lệ
     */
    private void validateLockParams(String lockKey, long timeout) {
        if (lockKey == null || lockKey.isEmpty()) {
            throw new IllegalArgumentException("lockKey cannot be null or empty");
        }
        if (timeout <= 0) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }

    // ==================== LOCK OPERATIONS ====================

    /**
     * Thử acquire lock một lần duy nhất (không retry).
     * 
     * <p>
     * Sử dụng Redis SET NX EX command để atomic set key nếu chưa tồn tại.
     * Lock value là UUID random để đảm bảo uniqueness và cho phép safe unlock.
     *
     * @param lockKey  Key dùng làm lock (ví dụ: "lock:order:123")
     * @param timeout  Thời gian lock tồn tại (TTL) - lock sẽ tự hết hạn sau thời
     *                 gian này
     * @param timeUnit Đơn vị thời gian của timeout
     * @return lockValue (UUID) nếu acquire thành công - CẦN GIỮ LẠI ĐỂ UNLOCK,
     *         null nếu lock đang bị giữ bởi process khác hoặc có lỗi
     * @throws IllegalArgumentException nếu lockKey null/empty hoặc timeout <= 0
     */
    public String tryLock(String lockKey, long timeout, TimeUnit timeUnit) {
        validateLockParams(lockKey, timeout);
        String lockValue = UUID.randomUUID().toString();
        try {
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, timeout, timeUnit);

            if (Boolean.TRUE.equals(acquired)) {
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
     * Thử acquire lock với retry và exponential backoff.
     * 
     * <p>
     * Method này sẽ retry nhiều lần trong khoảng maxWaitTime nếu lock đang busy.
     * Sử dụng exponential backoff để giảm contention:
     * <ul>
     * <li>Lần 1: chờ 50ms</li>
     * <li>Lần 2: chờ 100ms</li>
     * <li>Lần 3: chờ 200ms</li>
     * <li>... (max 500ms mỗi lần)</li>
     * </ul>
     *
     * @param lockKey     Key dùng làm lock
     * @param timeout     TTL của lock (thời gian lock tự hết hạn)
     * @param maxWaitTime Thời gian tối đa chờ để acquire lock
     * @param timeUnit    Đơn vị thời gian cho cả timeout và maxWaitTime
     * @return lockValue nếu acquire thành công, null nếu hết thời gian chờ hoặc bị
     *         interrupt
     */
    @SuppressWarnings("BusyWait") // Intentional: exponential backoff retry for distributed lock
    public String tryLockWithRetry(String lockKey, long timeout, long maxWaitTime, TimeUnit timeUnit) {
        long deadlineMs = System.currentTimeMillis() + timeUnit.toMillis(maxWaitTime);
        long retryIntervalMs = DEFAULT_RETRY_INTERVAL_MS;

        while (System.currentTimeMillis() < deadlineMs) {
            String lockValue = tryLock(lockKey, timeout, timeUnit);
            if (lockValue != null) {
                return lockValue;
            }

            try {
                Thread.sleep(retryIntervalMs);
                // Exponential backoff với cap
                retryIntervalMs = Math.min(retryIntervalMs * 2, MAX_RETRY_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Lock retry interrupted: key={}", lockKey);
                return null;
            }
        }

        log.warn("Lock acquisition timeout after {}ms: key={}", timeUnit.toMillis(maxWaitTime), lockKey);
        return null;
    }

    /**
     * Giải phóng lock một cách an toàn.
     * 
     * <p>
     * Sử dụng Lua script để đảm bảo chỉ owner (process giữ lockValue gốc)
     * mới có thể giải phóng lock. Điều này ngăn scenario:
     * <ol>
     * <li>Process A acquire lock</li>
     * <li>Process A chạy quá lâu, lock hết hạn</li>
     * <li>Process B acquire lock mới</li>
     * <li>Process A hoàn thành và xóa lock → SAI! Đang xóa lock của B</li>
     * </ol>
     *
     * @param lockKey   Key của lock cần giải phóng
     * @param lockValue Value trả về từ tryLock - dùng để verify ownership
     * @return true nếu unlock thành công, false nếu lock không thuộc về caller
     *         (đã hết hạn hoặc bị chiếm bởi process khác)
     */
    @SuppressWarnings("UnusedReturnValue") // Return value is optional for callers
    public boolean unlock(String lockKey, String lockValue) {
        if (lockValue == null) {
            return false;
        }

        try {
            Long result = stringRedisTemplate.execute(
                    UNLOCK_SCRIPT_OBJECT,
                    Collections.singletonList(lockKey),
                    lockValue);

            boolean success = result == 1L;
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
     * Thực thi action trong critical section với auto-release lock (không retry).
     * 
     * <p>
     * Đây là convenient method cho executeWithLock với maxWaitTime = 0.
     *
     * @param <T>      Kiểu trả về của action
     * @param lockKey  Key dùng làm lock
     * @param timeout  TTL của lock
     * @param timeUnit Đơn vị thời gian
     * @param action   Supplier chứa logic cần thực thi trong critical section
     * @return Kết quả của action
     * @throws LockAcquisitionException nếu không thể acquire lock
     */
    public <T> T executeWithLock(String lockKey, long timeout, TimeUnit timeUnit,
            Supplier<T> action) {
        return executeWithLock(lockKey, timeout, 0, timeUnit, action);
    }

    /**
     * Thực thi action trong critical section với auto-release lock và hỗ trợ retry.
     * 
     * <p>
     * Đây là method được khuyến nghị sử dụng vì đảm bảo lock luôn được release,
     * kể cả khi có exception xảy ra.
     * 
     * <h3>Ví dụ:</h3>
     * 
     * <pre>{@code
     * BigDecimal newBalance = redisLockComponent.executeWithLock(
     *         "lock:wallet:" + walletId,
     *         30, // lock TTL 30 giây
     *         5, // chờ tối đa 5 giây để acquire lock
     *         TimeUnit.SECONDS,
     *         () -> {
     *             // Critical section: update wallet balance
     *             return walletService.deposit(walletId, amount);
     *         });
     * }</pre>
     *
     * @param <T>         Kiểu trả về của action
     * @param lockKey     Key dùng làm lock (khuyến nghị format:
     *                    "lock:{resource}:{id}")
     * @param lockTimeout TTL của lock - nên lớn hơn thời gian xử lý dự kiến
     * @param maxWaitTime Thời gian tối đa chờ acquire lock (0 = không retry)
     * @param timeUnit    Đơn vị thời gian cho cả lockTimeout và maxWaitTime
     * @param action      Supplier chứa logic cần thực thi trong critical section
     * @return Kết quả trả về của action
     * @throws LockAcquisitionException nếu không thể acquire lock trong thời gian
     *                                  cho phép
     * @throws RuntimeException         nếu action throw exception (lock vẫn được
     *                                  release)
     */
    public <T> T executeWithLock(String lockKey, long lockTimeout, long maxWaitTime,
            TimeUnit timeUnit, Supplier<T> action) {
        String lockValue = maxWaitTime > 0
                ? tryLockWithRetry(lockKey, lockTimeout, maxWaitTime, timeUnit)
                : tryLock(lockKey, lockTimeout, timeUnit);

        if (lockValue == null) {
            throw new LockAcquisitionException("Cannot acquire lock: " + lockKey);
        }

        try {
            return action.get();
        } catch (Exception e) {
            log.error("Action failed while holding lock: key={}", lockKey, e);
            throw e;
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    /**
     * Thực thi action void trong critical section với auto-release lock.
     * 
     * <p>
     * Version cho các action không cần return value.
     *
     * @param lockKey     Key dùng làm lock
     * @param lockTimeout TTL của lock
     * @param maxWaitTime Thời gian tối đa chờ acquire lock (0 = không retry)
     * @param timeUnit    Đơn vị thời gian
     * @param action      Runnable chứa logic cần thực thi
     * @throws LockAcquisitionException nếu không thể acquire lock
     */
    public void executeWithLock(String lockKey, long lockTimeout, long maxWaitTime,
            TimeUnit timeUnit, Runnable action) {
        executeWithLock(lockKey, lockTimeout, maxWaitTime, timeUnit, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Kiểm tra xem một key có đang bị lock hay không.
     * 
     * <p>
     * <b>Lưu ý:</b> Kết quả của method này có thể stale ngay lập tức
     * (lock có thể được acquire/release bởi process khác ngay sau khi check).
     * Chỉ nên dùng cho mục đích monitoring/debugging, KHÔNG dùng để ra quyết định
     * logic.
     *
     * @param lockKey Key cần kiểm tra
     * @return true nếu key tồn tại (đang bị lock), false nếu không hoặc có lỗi
     */
    public boolean isLocked(String lockKey) {
        try {
            return stringRedisTemplate.hasKey(lockKey);
        } catch (Exception e) {
            log.error("Redis check lock error: key={}", lockKey, e);
            return false;
        }
    }

    // ==================== Exception ====================

    /**
     * Exception được throw khi không thể acquire lock trong thời gian cho phép.
     * 
     * <p>
     * Có thể xảy ra khi:
     * <ul>
     * <li>Lock đang bị giữ bởi process khác và hết thời gian chờ</li>
     * <li>Redis không khả dụng</li>
     * <li>Thread bị interrupt trong khi chờ</li>
     * </ul>
     */
    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }
}
