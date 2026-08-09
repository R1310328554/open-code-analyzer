/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson;

import org.redisson.api.RFuture;
import org.redisson.client.RedisException;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.Collections;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * {@link java.util.concurrent.locks.Lock} 的分布式<b>不可重入</b>锁实现。
 * <p>已持有锁的线程再次加锁时，无论 {@code lock()} 还是 {@code tryLock()}
 * 均抛出 {@link IllegalMonitorStateException}；其他线程正常竞争。
 * <p>客户端断开连接后锁会自动释放。
 * <p>采用<b>非公平</b>加锁，不保证获取顺序。
 *
 * @author Nikita Koksharov
 */
public class RedissonNonReentrantLock extends RedissonLock {

    static final String REENTRY_ERR = "NON_REENTRANT_REACQUIRE";

    public RedissonNonReentrantLock(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
    }

    @Override
    <T> RFuture<T> tryLockInnerAsync(long waitTime, long leaseTime, TimeUnit unit,
                                     long threadId, RedisStrictCommand<T> command) {
        RFuture<T> raw = evalWriteSyncedNoRetryAsync(getRawName(), LongCodec.INSTANCE, command,
                "if redis.call('exists', KEYS[1]) == 0 then " +
                    "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return nil; " +
                "end; " +
                "if redis.call('hexists', KEYS[1], ARGV[2]) == 1 then " +
                    "return redis.error_reply(ARGV[3]); " +
                "end; " +
                "return redis.call('pttl', KEYS[1]);",
                Collections.singletonList(getRawName()),
                unit.toMillis(leaseTime), getLockName(threadId), REENTRY_ERR);

        return new CompletableFutureWrapper<>(raw.handle(this::translate));
    }

    private <V> V translate(V v, Throwable ex) {
        if (ex == null) {
            return v;
        }
        if (isReentryError(ex)) {
            throw new CompletionException(new IllegalMonitorStateException(
                    "Lock '" + getRawName() + "' is non-reentrant and is already held by thread "
                            + Thread.currentThread().getId()));
        }
        if (ex instanceof CompletionException) {
            throw (CompletionException) ex;
        }
        throw new CompletionException(ex);
    }

    private static boolean isReentryError(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c.getMessage() != null && c.getMessage().contains(REENTRY_ERR)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 异步路径通过 {@code CompletionException} 传播 {@code IllegalMonitorStateException}；
     * 同步 {@code get(future)} 路径经 {@link org.redisson.command.CommandAsyncService#convertException}
     * 将非 {@code RedisException} 原因包装为通用 {@code RedisException}。
     * 与 {@link RedissonBaseLock#unlock()} 的解包逻辑一致，使同步 {@code lock}/{@code tryLock}
     * 调用方直接收到 {@code IllegalMonitorStateException}。
     */
    private static RuntimeException unwrapImse(RedisException e) {
        if (e.getCause() instanceof IllegalMonitorStateException) {
            return (IllegalMonitorStateException) e.getCause();
        }
        return e;
    }

    @Override
    public void lock() {
        try {
            super.lock();
        } catch (RedisException e) {
            throw unwrapImse(e);
        }
    }

    @Override
    public void lock(long leaseTime, TimeUnit unit) {
        try {
            super.lock(leaseTime, unit);
        } catch (RedisException e) {
            throw unwrapImse(e);
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        try {
            super.lockInterruptibly();
        } catch (RedisException e) {
            throw unwrapImse(e);
        }
    }

    @Override
    public void lockInterruptibly(long leaseTime, TimeUnit unit) throws InterruptedException {
        try {
            super.lockInterruptibly(leaseTime, unit);
        } catch (RedisException e) {
            throw unwrapImse(e);
        }
    }

    @Override
    public boolean tryLock() {
        try {
            return super.tryLock();
        } catch (RedisException e) {
            throw unwrapImse(e);
        }
    }

    @Override
    public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
        try {
            return super.tryLock(waitTime, unit);
        } catch (RedisException e) {
            throw unwrapImse(e);
        }
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        try {
            return super.tryLock(waitTime, leaseTime, unit);
        } catch (RedisException e) {
            throw unwrapImse(e);
        }
    }
}
