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
package org.redisson.eviction;

import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;

import java.util.concurrent.CompletionStage;

/**
 * {@link org.redisson.RedissonScoredSortedSet} 过期清理任务。
 * <p>
 * 按 score（通常为插入时间戳）删除早于 {@code now - shiftInMilliseconds} 的成员。
 * 不发布过期通知，仅做 ZREMRANGEBYSCORE。
 *
 * @author Nikita Koksharov
 *
 */
public class ScoredSetEvictionTask extends EvictionTask {

    /** 有序集合 Redis 键名。 */
    private final String name;
    /** 保留窗口偏移（毫秒），score 早于此阈值的成员将被删除。 */
    private final long shiftInMilliseconds;
    
    /** 构造 ScoredSet 过期清理任务。 */
    public ScoredSetEvictionTask(String name, CommandAsyncExecutor executor, long shiftInMilliseconds) {
        super(executor);
        this.name = name;
        this.shiftInMilliseconds = shiftInMilliseconds;
    }

    /** 返回被清理结构的名称。 */
    @Override
    String getName() {
        return name;
    }
    
    /** 删除 score 在 [0, now-shift] 区间内的成员。 */
    @Override
    CompletionStage<Integer> execute() {
        return executor.writeAsync(name, LongCodec.INSTANCE, RedisCommands.ZREMRANGEBYSCORE, name, 0, System.currentTimeMillis() - shiftInMilliseconds);
    }
    
}
