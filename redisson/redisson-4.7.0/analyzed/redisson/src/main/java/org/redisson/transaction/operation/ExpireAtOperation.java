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
package org.redisson.transaction.operation;

import org.redisson.RedissonBucket;
import org.redisson.RedissonLock;
import org.redisson.api.RFuture;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.transaction.RedissonTransactionalLock;
import org.redisson.transaction.RedissonTransactionalWriteLock;

/**
 * 按绝对时间戳设置过期（EXPIREAT）的事务操作。
 * 使用 {@link RedissonBucketExtended} 暴露 protected 的 expireAtAsync；
 * commit 后解锁，rollback 仅解锁。
 *
 * @author Nikita Koksharov
 *
 */
public class ExpireAtOperation extends TransactionalOperation {

    /** 扩展 Bucket 以调用包级 expireAtAsync。 */
    public static final class RedissonBucketExtended extends RedissonBucket {

        public RedissonBucketExtended(CommandAsyncExecutor connectionManager, String name) {
            super(connectionManager, name);
        }

        @Override
        protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
            return super.expireAtAsync(timestamp, param, keys);
        }

    }


    private String writeLockName;
    private String lockName;
    private String transactionId;
    /** 过期 Unix 时间戳（秒或毫秒，取决于 param）。 */
    private long timestamp;
    /** EXPIREAT 附加参数。 */
    private String param;
    /** 关联键名（多键过期时使用）。 */
    private String[] keys;

    public ExpireAtOperation(String name) {
        this(name, null, 0, null, 0, null, (String[]) null);
    }

    public ExpireAtOperation(String name, String lockName, long threadId, String transactionId, long timestamp, String param, String... keys) {
        super(name, null, threadId);
        this.lockName = lockName;
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.param = param;
        this.keys = keys;
    }

    public ExpireAtOperation(String name, String lockName, String writeLockName, long threadId, String transactionId, long timestamp, String param, String... keys) {
        super(name, null, threadId);
        this.lockName = lockName;
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.param = param;
        this.keys = keys;
        this.writeLockName = writeLockName;
    }

    /** 提交：EXPIREAT 并释放事务锁。 */
    @Override
    public void commit(CommandAsyncExecutor commandExecutor) {
        RedissonBucketExtended bucket = new RedissonBucketExtended(commandExecutor, name);
        bucket.expireAtAsync(timestamp, param, keys);
        if (lockName != null) {
            RedissonLock lock = new RedissonTransactionalLock(commandExecutor, lockName, transactionId);
            lock.unlockAsync(getThreadId());
        }
        if (writeLockName != null) {
            RedissonLock lock = new RedissonTransactionalWriteLock(commandExecutor, writeLockName, transactionId);
            lock.unlockAsync(getThreadId());
        }
    }

    @Override
    public void rollback(CommandAsyncExecutor commandExecutor) {
        if (lockName != null) {
            RedissonLock lock = new RedissonTransactionalLock(commandExecutor, lockName, transactionId);
            lock.unlockAsync(getThreadId());
        }
        if (writeLockName != null) {
            RedissonLock lock = new RedissonTransactionalWriteLock(commandExecutor, writeLockName, transactionId);
            lock.unlockAsync(getThreadId());
        }
    }
    
    public String getLockName() {
        return lockName;
    }

}
