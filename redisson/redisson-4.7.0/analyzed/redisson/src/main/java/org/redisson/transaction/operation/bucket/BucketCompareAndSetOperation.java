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

import org.redisson.RedissonBucket;
import org.redisson.RedissonLock;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.transaction.RedissonTransactionalLock;
import org.redisson.transaction.operation.TransactionalOperation;

/**
 * Bucket 比较并交换（compareAndSet）的事务操作：
 * 仅当当前值等于 expected 时才写入 value；commit 后释放键级事务锁。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class BucketCompareAndSetOperation<V> extends TransactionalOperation {

    /** 期望的当前值，CAS 条件。 */
    private V expected;
    /** 条件满足时写入的新值。 */
    private V value;
    /** 键对应的事务读锁名。 */
    private String lockName;
    /** 所属事务 ID。 */
    private String transactionId;
    
    public BucketCompareAndSetOperation(String name, String lockName, Codec codec, V expected, V value, String transactionId, long threadId) {
        super(name, codec, threadId);
        this.expected = expected;
        this.value = value;
        this.lockName = lockName;
        this.transactionId = transactionId;
    }

    /** 提交：执行 compareAndSet 并 unlock。 */
    @Override
    public void commit(CommandAsyncExecutor commandExecutor) {
        RedissonBucket<V> bucket = new RedissonBucket<V>(codec, commandExecutor, name);
        bucket.compareAndSetAsync(expected, value);
        RedissonLock lock = new RedissonTransactionalLock(commandExecutor, lockName, transactionId);
        lock.unlockAsync(getThreadId());
    }

    /** 回滚：不修改 Bucket，仅释放事务锁。 */
    @Override
    public void rollback(CommandAsyncExecutor commandExecutor) {
        RedissonLock lock = new RedissonTransactionalLock(commandExecutor, lockName, transactionId);
        lock.unlockAsync(getThreadId());
    }
    
    public V getExpected() {
        return expected;
    }
    
    public V getValue() {
        return value;
    }
    
    public String getLockName() {
        return lockName;
    }

}
