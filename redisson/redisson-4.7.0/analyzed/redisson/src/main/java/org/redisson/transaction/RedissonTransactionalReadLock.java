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

import org.redisson.RedissonReadLock;
import org.redisson.command.CommandAsyncExecutor;

/**
 * 事务内读锁：继承 {@link org.redisson.RedissonReadLock}，
 * 锁名追加 {@code :transactionId}，使同一事务内的锁与全局锁隔离。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonTransactionalReadLock extends RedissonReadLock {

    /** 所属 Redisson 事务 ID，用于锁名后缀。 */
    private final String transactionId;

    /** @param transactionId 事务标识，写入锁名以隔离并发 */
    public RedissonTransactionalReadLock(CommandAsyncExecutor commandExecutor, String name, String transactionId) {
        super(commandExecutor, name);
        this.transactionId = transactionId;
    }
    
    /** 在读锁名后附加事务 ID，避免与已提交锁冲突。 */
    @Override
    protected String getLockName(long threadId) {
        return super.getLockName(threadId) + ":" + transactionId;
    }
    
}
