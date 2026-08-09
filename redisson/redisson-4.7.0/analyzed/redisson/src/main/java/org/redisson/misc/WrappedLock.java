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
package org.redisson.misc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * {@link ReentrantLock} 的薄封装：通过 {@link #execute(Runnable)} /
 * {@link #execute(Supplier)} 保证 try/finally 解锁，简化临界区代码。
 *
 * @author Nikita Koksharov
 *
 */
public final class WrappedLock {

    /** 底层可重入互斥锁。 */
    private final Lock lock = new ReentrantLock();

    /** 在锁保护下执行无返回值任务。 */
    public void execute(Runnable r) {
        lock.lock();
        try {
            r.run();
        } finally {
            lock.unlock();
        }
    }

    /** 在锁保护下执行有返回值任务。 */
    public <T> T execute(Supplier<T> r) {
        lock.lock();
        try {
            return r.get();
        } finally {
            lock.unlock();
        }
    }

}
