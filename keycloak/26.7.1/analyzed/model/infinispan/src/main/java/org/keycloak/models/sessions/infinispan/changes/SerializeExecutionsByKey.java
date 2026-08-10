/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.sessions.infinispan.changes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.logging.Logger;

/**
 * 按键在 JVM 内串行化执行，避免同一 ID 的并发冲突。
 * <p>
 * 突发请求仅放行首个线程，其余线程排队等待，适用于已知并发会产生写冲突的缓存操作。
 *
 * @author Alexander Schwartz
 */
public class SerializeExecutionsByKey<K> {
    private static final Logger LOG = Logger.getLogger(SerializeExecutionsByKey.class);
    /** 每个键对应一把可重入锁，保证同键操作互斥。 */
    private final ConcurrentHashMap<K, ReentrantLock> cacheInteractions = new ConcurrentHashMap<>();

    public void runSerialized(K key, Runnable task) {
        // 确保下方 synchronized 块内针对同一 id 的计算持有同一锁实例
        // it will have the same object instance to lock the current execution until the other is finished.
        ReentrantLock lock = cacheInteractions.computeIfAbsent(key, s -> new ReentrantLock());
        try {
            lock.lock();
            // 若前一线程已在 finally 中移除条目，则重新注册锁
            ReentrantLock existingLock = cacheInteractions.putIfAbsent(key, lock);
            if (existingLock != lock) {
                LOG.debugf("Concurrent execution detected for key '%s'.", key);
            }
            task.run();
        } finally {
            lock.unlock();
            cacheInteractions.remove(key, lock);
        }
    }
}
