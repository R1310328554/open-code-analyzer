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
package org.redisson.reactive;

import org.redisson.RedissonReadWriteLock;
import org.redisson.api.RLockReactive;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RReadWriteLockReactive;

/**
 * {@link RReadWriteLock} 的 Reactor 响应式实现：
 * 通过 {@link ReactiveProxyBuilder} 将读写锁的异步 API
 * 包装为 {@link RLockReactive}。
 * <p>
 * 读锁可并发持有，写锁互斥；底层仍使用 Redis 分布式锁语义。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReadWriteLockReactive implements RReadWriteLockReactive {

    /** 底层同步读写锁。 */
    private final RReadWriteLock instance;
    /** 响应式命令执行器。 */
    private final CommandReactiveExecutor commandExecutor;
    
    /** @param commandExecutor 响应式执行器 @param name Redis 锁键名 */
    public RedissonReadWriteLockReactive(CommandReactiveExecutor commandExecutor, String name) {
        this.commandExecutor = commandExecutor;
        this.instance = new RedissonReadWriteLock(commandExecutor, name);
    }

    /** 返回共享读锁的响应式视图。 */
    @Override
    public RLockReactive readLock() {
        return ReactiveProxyBuilder.create(commandExecutor, instance.readLock(), RLockReactive.class);
    }

    /** 返回独占写锁的响应式视图。 */
    @Override
    public RLockReactive writeLock() {
        return ReactiveProxyBuilder.create(commandExecutor, instance.writeLock(), RLockReactive.class);
    }

    
}
