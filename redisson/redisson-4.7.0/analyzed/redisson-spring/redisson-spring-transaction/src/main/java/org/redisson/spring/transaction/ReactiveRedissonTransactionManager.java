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
package org.redisson.spring.transaction;

import org.redisson.api.RTransactionReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.TransactionOptions;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.reactive.AbstractReactiveTransactionManager;
import org.springframework.transaction.reactive.GenericReactiveTransaction;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * 基于 {@link org.redisson.api.RedissonReactiveClient} 的 Spring 响应式事务管理器。
 * <p>继承 {@link org.springframework.transaction.reactive.AbstractReactiveTransactionManager}，
 * 将 {@link TransactionDefinition} 映射为 {@link TransactionOptions} 并绑定
 * {@link RTransactionReactive} 到 {@link TransactionSynchronizationManager}。
 * <p>提交/回滚以 Reactor {@link Mono} 返回，错误包装为 {@link TransactionSystemException}。
 *
 * @author Nikita Koksharov
 *
 */
public class ReactiveRedissonTransactionManager extends AbstractReactiveTransactionManager {

    /** Redisson 响应式客户端，兼作事务资源键。 */
    private final RedissonReactiveClient redissonClient;

    /** 指定提供 {@link RTransactionReactive} 的 Redisson 响应式客户端。 */
    public ReactiveRedissonTransactionManager(RedissonReactiveClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /** 从当前 Reactor 事务上下文获取绑定的 {@link RTransactionReactive}。 */
    public Mono<RTransactionReactive> getCurrentTransaction() {
        return TransactionSynchronizationManager.forCurrentTransaction().map(manager -> {
            ReactiveRedissonResourceHolder holder = (ReactiveRedissonResourceHolder) manager.getResource(redissonClient);
            if (holder == null) {
                throw new NoTransactionException("No transaction is available for the current thread");
            } else {
                return holder.getTransaction();
            }
        });
    }


    /** 创建事务对象并读取已绑定的 {@link ReactiveRedissonResourceHolder}（若有）。 */
    @Override
    protected Object doGetTransaction(TransactionSynchronizationManager synchronizationManager) throws TransactionException {
        ReactiveRedissonTransactionObject transactionObject = new ReactiveRedissonTransactionObject();

        ReactiveRedissonResourceHolder holder = (ReactiveRedissonResourceHolder) synchronizationManager.getResource(redissonClient);
        transactionObject.setResourceHolder(holder);
        return transactionObject;
    }

    /** 按 {@link TransactionDefinition} 超时创建 {@link RTransactionReactive} 并绑定资源。 */
    @Override
    protected Mono<Void> doBegin(TransactionSynchronizationManager synchronizationManager, Object transaction, TransactionDefinition definition) throws TransactionException {
        ReactiveRedissonTransactionObject tObject = (ReactiveRedissonTransactionObject) transaction;

        TransactionOptions options = TransactionOptions.defaults();
        // 将 Spring 事务超时（秒）写入 Redisson TransactionOptions。
        if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
            options.timeout(definition.getTimeout(), TimeUnit.SECONDS);
        }

        RTransactionReactive trans = redissonClient.createTransaction(options);
        ReactiveRedissonResourceHolder holder = new ReactiveRedissonResourceHolder();
        holder.setTransaction(trans);
        tObject.setResourceHolder(holder);
        synchronizationManager.bindResource(redissonClient, holder);

        return Mono.empty();
    }

    /** 提交绑定的 {@link RTransactionReactive}；失败映射为 {@link TransactionSystemException}。 */
    @Override
    protected Mono<Void> doCommit(TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction status) throws TransactionException {
        ReactiveRedissonTransactionObject to = (ReactiveRedissonTransactionObject) status.getTransaction();
        return to.getResourceHolder().getTransaction().commit().onErrorMap(ex -> {
            return new TransactionSystemException("Unable to commit transaction " + to.getResourceHolder().getTransaction(), ex);
        });
    }

    /** 回滚绑定的 {@link RTransactionReactive}；失败映射为 {@link TransactionSystemException}。 */
    @Override
    protected Mono<Void> doRollback(TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction status) throws TransactionException {
        ReactiveRedissonTransactionObject to = (ReactiveRedissonTransactionObject) status.getTransaction();
        return to.getResourceHolder().getTransaction().rollback().onErrorMap(ex -> {
            return new TransactionSystemException("Unable to rollback transaction", ex);
        });
    }

    /** 挂起当前事务：解绑资源并返回供后续 resume 的 suspendedResources。 */
    @Override
    protected Mono<Object> doSuspend(TransactionSynchronizationManager synchronizationManager, Object transaction) throws TransactionException {
        return Mono.fromSupplier(() -> {
            ReactiveRedissonTransactionObject to = (ReactiveRedissonTransactionObject) transaction;
            to.setResourceHolder(null);
            return synchronizationManager.unbindResource(redissonClient);
        });
    }

    /** 恢复挂起的事务资源到 {@link TransactionSynchronizationManager}。 */
    @Override
    protected Mono<Void> doResume(TransactionSynchronizationManager synchronizationManager, Object transaction, Object suspendedResources) throws TransactionException {
        return Mono.fromRunnable(() -> {
            synchronizationManager.bindResource(redissonClient, suspendedResources);
        });
    }

    /** 将资源持有者标记为 rollback-only。 */
    @Override
    protected Mono<Void> doSetRollbackOnly(TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction status) throws TransactionException {
        return Mono.fromRunnable(() -> {
            ReactiveRedissonTransactionObject to = (ReactiveRedissonTransactionObject) status.getTransaction();
            to.getResourceHolder().setRollbackOnly();
        });
    }

    /** 事务完成后解绑资源并清空 {@link RTransactionReactive} 引用。 */
    @Override
    protected Mono<Void> doCleanupAfterCompletion(TransactionSynchronizationManager synchronizationManager, Object transaction) {
        return Mono.fromRunnable(() -> {
            synchronizationManager.unbindResource(redissonClient);
            ReactiveRedissonTransactionObject to = (ReactiveRedissonTransactionObject) transaction;
            to.getResourceHolder().setTransaction(null);
        });
    }

    /** 若事务对象已持有资源则视为存在活动事务。 */
    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        ReactiveRedissonTransactionObject transactionObject = (ReactiveRedissonTransactionObject) transaction;
        return transactionObject.getResourceHolder() != null;
    }
}
