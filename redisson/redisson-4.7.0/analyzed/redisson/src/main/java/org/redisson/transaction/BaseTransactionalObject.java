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
package org.redisson.transaction;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 分布式事务对象的公共基类。
 * <p>
 * 持有 {@link #transactionId} 与对象级 {@link #lockName}，
 * 提供 {@link #executeLocked} 在获取事务锁后运行异步逻辑；
 * move/migrate 在事务内不支持。
 *
 * @author Nikita Koksharov
 *
 */
public class BaseTransactionalObject {

    /** 当前事务唯一 ID，参与锁名后缀与操作记录。 */
    final String transactionId;
    /** 对象级事务锁 Redis 键名。 */
    final String lockName;
    /** 异步命令执行器。 */
    final CommandAsyncExecutor commandExecutor;

    public BaseTransactionalObject(String transactionId, String lockName, CommandAsyncExecutor commandExecutor) {
        this.transactionId = transactionId;
        this.lockName = lockName;
        this.commandExecutor = commandExecutor;
    }

    /** 事务内不支持 MOVE。 */
    public RFuture<Boolean> moveAsync(int database) {
        throw new UnsupportedOperationException("move method is not supported in transaction");
    }
    
    public RFuture<Void> migrateAsync(String host, int port, int database) {
        throw new UnsupportedOperationException("migrate method is not supported in transaction");
    }

    /** 对象级写锁（{@link RedissonTransactionalWriteLock}）。 */
    protected RLock getWriteLock() {
        return new RedissonTransactionalWriteLock(commandExecutor, lockName, transactionId);
    }

    /** 对象级读锁（{@link RedissonTransactionalReadLock}）。 */
    protected RLock getReadLock() {
        return new RedissonTransactionalReadLock(commandExecutor, lockName, transactionId);
    }

    /** 约定后缀 {@code :transaction_lock} 作为对象事务锁名。 */
    protected static String getLockName(String name) {
        return name + ":transaction_lock";
    }

    /** 单锁：lockAsync 成功后执行 runnable。 */
    protected <R> RFuture<R> executeLocked(long timeout, Supplier<CompletionStage<R>> runnable, RLock lock) {
        return executeLocked(Thread.currentThread().getId(), timeout, runnable, lock);
    }

    protected <R> RFuture<R> executeLocked(long threadId, long timeout, Supplier<CompletionStage<R>> runnable, RLock lock) {
        CompletionStage<R> f = lock.lockAsync(timeout, TimeUnit.MILLISECONDS, threadId).thenCompose(res -> runnable.get());
        return new CompletableFutureWrapper<>(f);
    }

    /** 多锁：{@link RedissonMultiLock} 加锁后执行；异常时 unlock。 */
    protected <R> RFuture<R> executeLocked(long timeout, Supplier<CompletionStage<R>> runnable, List<RLock> locks) {
        RedissonMultiLock multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));
        long threadId = Thread.currentThread().getId();
        CompletionStage<R> f = multiLock.lockAsync(timeout, TimeUnit.MILLISECONDS)
                .thenCompose(res -> runnable.get())
                .whenComplete((r, e) -> {
                    if (e != null) {
                        multiLock.unlockAsync(threadId);
                    }
                });
        return new CompletableFutureWrapper<>(f);
    }

}
