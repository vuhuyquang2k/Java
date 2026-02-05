package com.base.demo.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis Distributed Lock - Sử dụng Redisson RLock.
 * Features: Watch Dog auto-renewal, Fair Lock, Reentrant.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockComponent {

    private final RedissonClient redissonClient;

    // ==================== LOCK FACTORY ====================

    /** Lấy lock instance (reentrant). */
    public RLock getLock(String lockKey) {
        validateLockKey(lockKey);
        return redissonClient.getLock(lockKey);
    }

    /** Lấy fair lock (FIFO ordering, chậm hơn 20-30%). */
    public RLock getFairLock(String lockKey) {
        validateLockKey(lockKey);
        return redissonClient.getFairLock(lockKey);
    }

    // ==================== VALIDATION ====================

    private void validateLockKey(String lockKey) {
        if (lockKey == null || lockKey.isEmpty()) {
            throw new IllegalArgumentException("lockKey cannot be null or empty");
        }
    }

    private void validateTimeout(long timeout) {
        if (timeout < -1) {
            throw new IllegalArgumentException("timeout must be >= -1");
        }
    }

    // ==================== BASIC LOCK ====================

    /** Thử acquire lock 1 lần (watch dog auto-renewal). */
    public boolean tryLock(String lockKey) {
        try {
            return getLock(lockKey).tryLock();
        } catch (Exception e) {
            log.error("Redis tryLock error: key={}", lockKey, e);
            return false;
        }
    }

    /** Thử acquire lock với TTL cố định. */
    public boolean tryLock(String lockKey, long leaseTime, TimeUnit timeUnit) {
        validateTimeout(leaseTime);
        try {
            return getLock(lockKey).tryLock(0, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock interrupted: key={}", lockKey);
            return false;
        } catch (Exception e) {
            log.error("Redis tryLock error: key={}", lockKey, e);
            return false;
        }
    }

    /** Thử acquire lock với retry. leaseTime=-1 để bật watch dog. */
    public boolean tryLockWithRetry(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        validateTimeout(leaseTime);
        try {
            boolean acquired = getLock(lockKey).tryLock(waitTime, leaseTime, timeUnit);
            if (!acquired) {
                log.warn("Lock timeout after {}ms: key={}", timeUnit.toMillis(waitTime), lockKey);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock interrupted: key={}", lockKey);
            return false;
        } catch (Exception e) {
            log.error("Redis tryLock error: key={}", lockKey, e);
            return false;
        }
    }

    /** Giải phóng lock. Chỉ owner thread mới unlock được. */
    public boolean unlock(String lockKey) {
        try {
            RLock lock = getLock(lockKey);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                return true;
            }
            log.warn("Cannot unlock - not owner: key={}", lockKey);
            return false;
        } catch (Exception e) {
            log.error("Redis unlock error: key={}", lockKey, e);
            return false;
        }
    }

    /** Force unlock (NGUY HIỂM - có thể gây race condition). */
    public boolean forceUnlock(String lockKey) {
        try {
            boolean result = getLock(lockKey).forceUnlock();
            if (result)
                log.warn("Force unlocked: key={}", lockKey);
            return result;
        } catch (Exception e) {
            log.error("Redis force unlock error: key={}", lockKey, e);
            return false;
        }
    }

    // ==================== HIGH-LEVEL API ====================

    /** Execute với lock (watch dog auto-renewal). */
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        RLock lock = getLock(lockKey);
        try {
            lock.lock();
            return action.get();
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    /** Execute với lock có TTL cố định. */
    public <T> T executeWithLock(String lockKey, long leaseTime, TimeUnit timeUnit, Supplier<T> action) {
        return executeWithLock(lockKey, 0, leaseTime, timeUnit, action);
    }

    /** Execute với retry. leaseTime=-1 để bật watch dog. */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime,
            TimeUnit timeUnit, Supplier<T> action) {
        validateTimeout(leaseTime);
        RLock lock = getLock(lockKey);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!acquired) {
                throw new LockAcquisitionException("Cannot acquire lock: " + lockKey);
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException("Lock interrupted: " + lockKey);
        } catch (LockAcquisitionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Action failed: key={}", lockKey, e);
            throw e;
        } finally {
            if (acquired && lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    /** Execute void action với lock. */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime,
            TimeUnit timeUnit, Runnable action) {
        executeWithLock(lockKey, waitTime, leaseTime, timeUnit, () -> {
            action.run();
            return null;
        });
    }

    // ==================== UTILITY ====================

    /** Kiểm tra lock đang bị giữ. */
    public boolean isLocked(String lockKey) {
        try {
            return getLock(lockKey).isLocked();
        } catch (Exception e) {
            log.error("Check lock error: key={}", lockKey, e);
            return false;
        }
    }

    /** Kiểm tra thread hiện tại có giữ lock không. */
    public boolean isHeldByCurrentThread(String lockKey) {
        try {
            return getLock(lockKey).isHeldByCurrentThread();
        } catch (Exception e) {
            log.error("Check holder error: key={}", lockKey, e);
            return false;
        }
    }

    /** Lấy số lần lock được acquire (reentrant counter). */
    public int getHoldCount(String lockKey) {
        try {
            return getLock(lockKey).getHoldCount();
        } catch (Exception e) {
            log.error("Get hold count error: key={}", lockKey, e);
            return 0;
        }
    }

    // ==================== EXCEPTION ====================

    /** Exception khi không acquire được lock. */
    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }
}
