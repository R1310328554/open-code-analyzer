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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 读写锁读侧续期条目：
 * 在 {@link LockEntry} 基础上为每个线程记录 {@link #keyPrefix}，
 * 供 {@link ReadLockTask} 续期读锁超时键（{@code rwlock_timeout}）。
 *
 * @author Nikita Koksharov
 *
 */
public class ReadLockEntry extends LockEntry {

    /** 线程 ID → 读锁 Redis 键前缀。 */
    private final Map<Long, String> threadId2keyPrefix = new ConcurrentHashMap<>();

    /** @param threadId 线程 ID @return 读锁键前缀 */
    public String getKeyPrefix(Long threadId) {
        return threadId2keyPrefix.get(threadId);
    }

    /** 增加读锁持有计数并记录 lockName 与 keyPrefix。 */
    public void addThreadId(long threadId, String lockName, String keyPrefix) {
        threadId2counter.compute(threadId, (t, counter) -> {
            counter = Optional.ofNullable(counter).orElse(0);
            counter++;
            threadsQueue.add(threadId);
            return counter;
        });
        threadId2lockName.putIfAbsent(threadId, lockName);
        threadId2keyPrefix.putIfAbsent(threadId, keyPrefix);
    }

    /** 递减计数；归零时同时清理 keyPrefix 映射。 */
    @Override
    public void removeThreadId(long threadId) {
        threadId2counter.computeIfPresent(threadId, (t, counter) -> {
            counter--;
            if (counter == 0) {
                threadsQueue.removeIf(v -> v == threadId);
                threadId2lockName.remove(threadId);
                threadId2keyPrefix.remove(threadId);
                return null;
            }
            return counter;
        });
    }

}
