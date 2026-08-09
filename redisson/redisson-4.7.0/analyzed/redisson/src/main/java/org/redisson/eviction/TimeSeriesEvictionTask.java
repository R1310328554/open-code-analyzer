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

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

/**
 * {@link org.redisson.RedissonTimeSeries} 过期清理任务。
 * <p>
 * 从超时有序集合取过期 entry，同步删除主 ZSET 与超时 ZSET 中的对应成员。
 *
 * @author Nikita Koksharov
 *
 */
public class TimeSeriesEvictionTask extends EvictionTask {

    /** TimeSeries 主有序集合 Redis 键名。 */
    private final String name;
    /** 记录各 entry 过期时间戳的有序集合键名。 */
    private final String timeoutSetName;

    /** 构造 TimeSeries 过期清理任务。 */
    public TimeSeriesEvictionTask(String name, String timeoutSetName, CommandAsyncExecutor executor) {
        super(executor);
        this.name = name;
        this.timeoutSetName = timeoutSetName;
    }

    /** 清理过期 entry，返回本次删除数量。 */
    @Override
    CompletionStage<Integer> execute() {
        return executor.evalWriteAsync(name, LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                "local expiredKeys = redis.call('zrangebyscore', KEYS[2], 0, ARGV[1], 'limit', 0, ARGV[2]); "
              + "if #expiredKeys > 0 then "
                  + "redis.call('zrem', KEYS[2], unpack(expiredKeys)); "
                  + "redis.call('zrem', KEYS[1], unpack(expiredKeys)); "
              + "end; "
              + "return #expiredKeys;",
              Arrays.asList(name, timeoutSetName),
                System.currentTimeMillis(), keysLimit);
    }

    /** 返回被清理结构的名称。 */
    @Override
    String getName() {
        return name;
    }
}
