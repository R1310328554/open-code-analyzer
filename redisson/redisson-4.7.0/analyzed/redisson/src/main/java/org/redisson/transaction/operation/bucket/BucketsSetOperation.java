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

import org.redisson.RedissonBuckets;
import org.redisson.api.RBuckets;
import org.redisson.api.RLock;
import org.redisson.api.bucket.SetArgs;
import org.redisson.api.bucket.SetParams;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.transaction.RedissonTransactionalLock;
import org.redisson.transaction.operation.TransactionalOperation;

/**
 * 批量 Bucket 写入操作的抽象基类。
 * 子类定义具体写入语义（全量 set、条件 set 等）；commit 后逐键释放 {@code :transaction_lock}。
 *
 * @author seakider
 *
 */
public abstract class BucketsSetOperation extends TransactionalOperation {
    /** 所属事务 ID。 */
    private final String transactionId;
    /** 批量写入参数（键值对及 TTL 等）。 */
    private final SetArgs setArgs;

    public BucketsSetOperation(Codec codec, SetArgs setArgs, String transactionId) {
        super(null, codec);
        this.setArgs = setArgs;
        this.transactionId = transactionId;
    }

    /** 提交：执行子类写入逻辑，再 unlock 所有涉及键。 */
    @Override
    public void commit(CommandAsyncExecutor commandExecutor) {
        RBuckets buckets = new RedissonBuckets(codec, commandExecutor);
        commit(buckets, setArgs);
        unlock(commandExecutor);
    }

    /** 释放 setArgs 中每个键对应的事务锁。 */
    protected void unlock(CommandAsyncExecutor commandExecutor) {
        SetParams pps = (SetParams) setArgs;
        for (String key : pps.getEntries().keySet()) {
            RLock lock = new RedissonTransactionalLock(commandExecutor, getLockName(key), transactionId);
            lock.unlockAsync();
        }
    }

    /** 子类实现：对 {@link RBuckets} 发起具体异步写入。 */
    protected abstract void commit(RBuckets buckets, SetArgs setArgs);

    /** 回滚：不写入，仅释放各键事务锁。 */
    @Override
    public void rollback(CommandAsyncExecutor commandExecutor) {
        unlock(commandExecutor);
    }

    public SetArgs getSetArgs() {
        return setArgs;
    }

    /** 键名后缀 {@code :transaction_lock} 构成事务锁名。 */
    private String getLockName(String name) {
        return name + ":transaction_lock";
    }
}
