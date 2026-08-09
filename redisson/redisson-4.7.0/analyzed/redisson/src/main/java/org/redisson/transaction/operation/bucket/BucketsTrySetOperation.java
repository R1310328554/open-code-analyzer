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

import java.util.Map;

import org.redisson.RedissonBuckets;
import org.redisson.api.RBuckets;
import org.redisson.api.RLock;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.transaction.RedissonTransactionalLock;
import org.redisson.transaction.operation.TransactionalOperation;

/**
 * 批量 Bucket 条件写入（trySet）的事务操作：
 * 对每个键仅在不存在时写入；commit 后逐键释放事务锁。
 *
 * @author Nikita Koksharov
 *
 */
public class BucketsTrySetOperation extends TransactionalOperation {

    /** 所属事务 ID。 */
    private String transactionId;
    /** 键到待写入值的映射。 */
    private Map<String, Object> values;
    
    public BucketsTrySetOperation(Codec codec, Map<String, Object> values, String transactionId) {
        super(null, codec);
        this.values = values;
        this.transactionId = transactionId;
    }

    /** 提交：批量 trySet 并 unlock 所有涉及键。 */
    @Override
    public void commit(CommandAsyncExecutor commandExecutor) {
        RBuckets bucket = new RedissonBuckets(codec, commandExecutor);
        bucket.trySetAsync(values);
        
        unlock(commandExecutor);
    }

    /** 释放 values 中每个键对应的事务锁。 */
    protected void unlock(CommandAsyncExecutor commandExecutor) {
        for (String key : values.keySet()) {
            RLock lock = new RedissonTransactionalLock(commandExecutor, getLockName(key), transactionId);
            lock.unlockAsync();
        }
    }

    /** 回滚：不写入，仅释放各键事务锁。 */
    @Override
    public void rollback(CommandAsyncExecutor commandExecutor) {
        unlock(commandExecutor);
    }
    
    public Map<String, Object> getValues() {
        return values;
    }

    /** 键名后缀 {@code :transaction_lock} 构成事务锁名。 */
    private String getLockName(String name) {
        return name + ":transaction_lock";
    }

}
