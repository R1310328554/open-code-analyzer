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
package org.redisson.transaction.operation.bucket;

import java.util.concurrent.TimeUnit;

import org.redisson.RedissonBucket;
import org.redisson.RedissonLock;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.transaction.RedissonTransactionalLock;
import org.redisson.transaction.operation.TransactionalOperation;

/**
 * Bucket 条件写入（trySet / SETNX）的事务操作：
 * 仅当键不存在时写入；支持可选 TTL，commit 后释放事务锁。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class BucketTrySetOperation<V> extends TransactionalOperation {

    /** 所属事务 ID。 */
    private String transactionId;
    /** 尝试写入的值。 */
    private Object value;
    /** 键对应的事务读锁名。 */
    private String lockName;
    /** 可选存活时间，0 表示不设过期。 */
    private long timeToLive;
    /** timeToLive 的时间单位。 */
    private TimeUnit timeUnit;
    
    public BucketTrySetOperation(String name, String lockName, Codec codec, Object value, long timeToLive, TimeUnit timeUnit, String transactionId, long threadId) {
        this(name, lockName, codec, value, transactionId, threadId);
        this.timeToLive = timeToLive;
        this.timeUnit = timeUnit;
    }
    
    public BucketTrySetOperation(String name, String lockName, Codec codec, Object value, String transactionId, long threadId) {
        super(name, codec, threadId);
        this.value = value;
        this.lockName = lockName;
        this.transactionId = transactionId;
    }

    /** 提交：trySet（含可选 TTL）并 unlock。 */
    @Override
    public void commit(CommandAsyncExecutor commandExecutor) {
        RedissonBucket<V> bucket = new RedissonBucket<V>(codec, commandExecutor, name);
        if (timeToLive != 0) {
            bucket.trySetAsync((V) value, timeToLive, timeUnit);
        } else {
            bucket.trySetAsync((V) value);
        }
        RedissonLock lock = new RedissonTransactionalLock(commandExecutor, lockName, transactionId);
        lock.unlockAsync(getThreadId());
    }

    /** 回滚：不写入 Bucket，仅释放事务锁。 */
    @Override
    public void rollback(CommandAsyncExecutor commandExecutor) {
        RedissonLock lock = new RedissonTransactionalLock(commandExecutor, lockName, transactionId);
        lock.unlockAsync(getThreadId());
    }
    
    public Object getValue() {
        return value;
    }
    
    public String getLockName() {
        return lockName;
    }
    
    public long getTimeToLive() {
        return timeToLive;
    }
    
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

}
