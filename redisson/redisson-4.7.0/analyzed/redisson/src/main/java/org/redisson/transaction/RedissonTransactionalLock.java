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

import org.redisson.RedissonLock;
import org.redisson.command.CommandAsyncExecutor;

/**
 * 事务专用分布式锁：在标准 {@link RedissonLock} 锁名后追加 {@link #transactionId}。
 * <p>
 * 使同一 Redis 键在事务内的细粒度锁与普通锁隔离，
 * commit/rollback 时由 {@link TransactionalOperation} 统一释放。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonTransactionalLock extends RedissonLock {

    /** 所属事务 ID，参与 {@link #getLockName(long)} 后缀。 */
    private final String transactionId;
    
    public RedissonTransactionalLock(CommandAsyncExecutor commandExecutor, String name, String transactionId) {
        super(commandExecutor, name);
        this.transactionId = transactionId;
    }
    
    @Override
    /** 锁名格式：{@code 基锁名:threadId:transactionId}。 */
    protected String getLockName(long threadId) {
        return super.getLockName(threadId) + ":" + transactionId;
    }
    
}
