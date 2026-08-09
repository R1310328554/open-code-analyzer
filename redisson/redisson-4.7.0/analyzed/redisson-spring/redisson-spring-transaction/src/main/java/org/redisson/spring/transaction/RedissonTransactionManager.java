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

import org.redisson.api.RTransaction;
import org.redisson.api.RedissonClient;
import org.redisson.api.TransactionOptions;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson {@link org.redisson.api.RTransaction} 的 Spring
 * {@link org.springframework.transaction.PlatformTransactionManager} 实现。
 * <p>继承 {@link org.springframework.transaction.support.AbstractPlatformTransactionManager}，
 * 将 Spring 声明式/编程式事务映射到 Redisson 分布式事务 API；
 * 同时实现 {@link org.springframework.transaction.support.ResourceTransactionManager}，
 * 以 {@link org.redisson.api.RedissonClient} 作为资源工厂键。
 *
 * @author Nikita Koksharov
 */
public class RedissonTransactionManager extends AbstractPlatformTransactionManager implements ResourceTransactionManager {

    private static final long serialVersionUID = -6151310954082124041L;
    
    private RedissonClient redisson;
    
    /** @param redisson 提供 {@link org.redisson.api.RTransaction} 的 Redisson 客户端 */
    public RedissonTransactionManager(RedissonClient redisson) {
        this.redisson = redisson;
    }
    
    /** 从 {@link org.springframework.transaction.support.TransactionSynchronizationManager} 获取当前线程活动事务。 */
    public RTransaction getCurrentTransaction() {
        RedissonTransactionHolder to = (RedissonTransactionHolder) TransactionSynchronizationManager.getResource(redisson);
        if (to == null) {
            throw new NoTransactionException("No transaction is available for the current thread");
        }
        return to.getTransaction();
    }

    /** 创建 {@link RedissonTransactionObject} 并探测是否已有绑定资源。 */
    @Override
    protected Object doGetTransaction() throws TransactionException {
        RedissonTransactionObject transactionObject = new RedissonTransactionObject();
        
        RedissonTransactionHolder holder = (RedissonTransactionHolder) TransactionSynchronizationManager.getResource(redisson);
        if (holder != null) {
            transactionObject.setTransactionHolder(holder);
        }
        return transactionObject;
    }
    
    /** 若线程已绑定 {@link RedissonTransactionHolder} 则视为存在活动事务。 */
    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        RedissonTransactionObject transactionObject = (RedissonTransactionObject) transaction;
        return transactionObject.getTransactionHolder() != null;
    }

    /** 按 {@link TransactionDefinition} 超时创建 Redisson 事务并绑定到同步管理器。 */
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        RedissonTransactionObject tObject = (RedissonTransactionObject) transaction;
        
        if (tObject.getTransactionHolder() == null) {
            int timeout = determineTimeout(definition);
            TransactionOptions options = TransactionOptions.defaults();
            if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                options.timeout(timeout, TimeUnit.SECONDS);
            }
            
            RTransaction trans = redisson.createTransaction(options);
            RedissonTransactionHolder holder = new RedissonTransactionHolder();
            holder.setTransaction(trans);
            tObject.setTransactionHolder(holder);
            TransactionSynchronizationManager.bindResource(redisson, holder);
        }
    }

    /** 提交底层 {@link org.redisson.api.RTransaction}；失败时包装为 {@link TransactionSystemException}。 */
    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        RedissonTransactionObject to = (RedissonTransactionObject) status.getTransaction();
        try {
            to.getTransactionHolder().getTransaction().commit();
        } catch (org.redisson.transaction.TransactionException e) {
            throw new TransactionSystemException("Unable to commit transaction", e);
        }
    }

    /** 回滚底层 Redisson 事务。 */
    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        RedissonTransactionObject to = (RedissonTransactionObject) status.getTransaction();
        try {
            to.getTransactionHolder().getTransaction().rollback();
        } catch (org.redisson.transaction.TransactionException e) {
            throw new TransactionSystemException("Unable to rollback transaction", e);
        }
    }

    /** 将事务标记为仅回滚（rollback-only）。 */
    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        RedissonTransactionObject to = (RedissonTransactionObject) status.getTransaction();
        to.setRollbackOnly(true);
    }

    /** 恢复挂起时解绑的 {@link RedissonTransactionHolder}。 */
    @Override
    protected void doResume(Object transaction, Object suspendedResources) throws TransactionException {
        TransactionSynchronizationManager.bindResource(redisson, suspendedResources);
    }

    /** 挂起当前事务：解绑资源并返回供后续 resume 的 holder。 */
    @Override
    protected Object doSuspend(Object transaction) throws TransactionException {
        RedissonTransactionObject to = (RedissonTransactionObject) transaction;
        to.setTransactionHolder(null);
        return TransactionSynchronizationManager.unbindResource(redisson);
    }

    /** 事务完成后解绑资源并清空 holder 中的事务引用。 */
    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        TransactionSynchronizationManager.unbindResourceIfPossible(redisson);
        RedissonTransactionObject to = (RedissonTransactionObject) transaction;
        to.getTransactionHolder().setTransaction(null);
    }
    
    /** 返回作为同步管理器资源键的 {@link org.redisson.api.RedissonClient}。 */
    @Override
    public Object getResourceFactory() {
        return redisson;
    }

}
