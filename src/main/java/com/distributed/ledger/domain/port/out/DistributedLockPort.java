package com.distributed.ledger.domain.port.out;

public interface DistributedLockPort {
    /**
     * Attempts to acquire a lock for the specified key.
     * If the lock is acquired, the action is executed.
     * If the lock cannot be acquired, a runtime exception is thrown.
     *
     * @param key    The lock key
     * @param action The action to execute under the lock
     */
    void executeInLock(String key, Runnable action);
}