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
package org.redisson.rx;

import org.redisson.RedissonReadWriteLock;
import org.redisson.api.RLockRx;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RReadWriteLockRx;

/**
 * {@link RReadWriteLockRx} 的 Redisson 实现：读写锁的 Rx 门面。
 * <p>
 * 读锁与写锁分别通过 {@link RxProxyBuilder} 包装底层 {@link RLock}，
 * 所有加锁/解锁操作经 {@link CommandRxExecutor} 转为 RxJava {@code Single}/{@code Completable}。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReadWriteLockRx implements RReadWriteLockRx {

    /** 底层同步读写锁。 */
    private final RReadWriteLock instance;
    /** Rx 命令调度器。 */
    private final CommandRxExecutor commandExecutor;
    
    public RedissonReadWriteLockRx(CommandRxExecutor commandExecutor, String name) {
        this.commandExecutor = commandExecutor;
        this.instance = new RedissonReadWriteLock(commandExecutor, name);
    }

    /** 返回共享读锁的 Rx 代理（允许多读者并发）。 */
    @Override
    public RLockRx readLock() {
        return RxProxyBuilder.create(commandExecutor, instance.readLock(), RLockRx.class);
    }

    /** 返回独占写锁的 Rx 代理（与读锁/写锁互斥）。 */
    @Override
    public RLockRx writeLock() {
        return RxProxyBuilder.create(commandExecutor, instance.writeLock(), RLockRx.class);
    }

    
}
