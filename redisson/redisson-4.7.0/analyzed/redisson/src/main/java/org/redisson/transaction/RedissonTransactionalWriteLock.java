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

import org.redisson.RedissonWriteLock;
import org.redisson.command.CommandAsyncExecutor;

/**
 * 事务内写锁：继承 {@link org.redisson.RedissonWriteLock}，
 * 锁名追加 {@code :transactionId}，保证事务提交前写锁仅对本事务可见。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonTransactionalWriteLock extends RedissonWriteLock {

    /** 所属事务 ID。 */
    private final String transactionId;

    /** @param transactionId 写入锁名后缀的事务标识 */
    public RedissonTransactionalWriteLock(CommandAsyncExecutor commandExecutor, String name, String transactionId) {
        super(commandExecutor, name);
        this.transactionId = transactionId;
    }
    
    /** 写锁名 = 基类锁名 + ":" + transactionId。 */
    @Override
    protected String getLockName(long threadId) {
        return super.getLockName(threadId) + ":" + transactionId;
    }
    
}
