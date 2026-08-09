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
package org.redisson.renewal;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 单锁续期注册条目：
 * 跟踪同一 Redis 锁键上各线程的持有计数、锁名映射
 * 及 FIFO 线程队列，供 {@link LockTask} 看门狗批量续期。
 * <p>
 * 支持同一线程多次加锁（可重入计数）。
 *
 * @author Nikita Koksharov
 *
 */
public class LockEntry {

    /** 等待续期的线程 ID 队列（FIFO）。 */
    final Queue<Long> threadsQueue = new ConcurrentLinkedQueue<>();
    /** 线程 ID → 可重入持有次数。 */
    final Map<Long, Integer> threadId2counter = new ConcurrentHashMap<>();
    /** 线程 ID → Redis Hash 中的锁字段名。 */
    final Map<Long, String> threadId2lockName = new ConcurrentHashMap<>();

    /** 包内可见的无参构造。 */
    LockEntry() {
        super();
    }

    /** @param threadId 线程 ID @return 对应锁字段名 */
    public String getLockName(long threadId) {
        return threadId2lockName.get(threadId);
    }

    /** 增加线程持有计数并记录锁名（可重入）。 */
    public void addThreadId(long threadId, String lockName) {
        threadId2counter.compute(threadId, (t, counter) -> {
            counter = Optional.ofNullable(counter).orElse(0);
            counter++;
            threadsQueue.add(threadId);
            return counter;
        });
        threadId2lockName.putIfAbsent(threadId, lockName);
    }

    /** @return 是否无任何线程等待续期 */
    public boolean hasNoThreads() {
        return threadsQueue.isEmpty();
    }

    /** @return 队列首部线程 ID，无则 null */
    public Long getFirstThreadId() {
        return threadsQueue.peek();
    }

    /** 减少可重入计数；归零时从队列与映射中移除。 */
    public void removeThreadId(long threadId) {
        threadId2counter.computeIfPresent(threadId, (t, counter) -> {
            counter--;
            if (counter == 0) {
                threadsQueue.removeIf(v-> v == threadId);
                threadId2lockName.remove(threadId);
                return null;
            }
            return counter;
        });
    }

}
