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
package org.redisson.api;

import java.util.concurrent.locks.Lock;

/**
 * {@link java.util.concurrent.locks.ReadWriteLock} 的分布式实现。
 * <p>维护一对关联的 {@link Lock}：读锁供只读操作，写锁供写入操作。
 * 无写锁时多个读线程可同时持有读锁；写锁互斥。
 * <p>采用非公平模式，读写加锁顺序不作保证。
 *
 * @author Nikita Koksharov
 */
public interface RReadWriteLockReactive {

    /**
     * 返回用于读操作的锁。
     *
     * @return 读锁
     */
    RLockReactive readLock();

    /**
     * 返回用于写操作的锁。
     *
     * @return 写锁
     */
    RLockReactive writeLock();

}
