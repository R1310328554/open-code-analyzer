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
 * {@link org.redisson.RedissonListMultimap} 等 Multimap 过期清理任务。
 * <p>
 * 过期 key 对应 value 集合存储在独立 Redis 键中，清理时需一并删除。
 *
 * @author Nikita Koksharov
 *
 */
public class MultimapEvictionTask extends EvictionTask {

    /** Multimap 主哈希表 Redis 键名。 */
    private final String name;
    /** 记录各 key 过期时间戳的有序集合键名。 */
    private final String timeoutSetName;
    
    /** 构造 Multimap 过期清理任务。 */
    public MultimapEvictionTask(String name, String timeoutSetName, CommandAsyncExecutor executor) {
        super(executor);
        this.name = name;
        this.timeoutSetName = timeoutSetName;
    }

    /** 返回被清理结构的名称。 */
    @Override
    String getName() {
        return name;
    }

    /**
     * 清理过期 key：删除超时 ZSET 条目、关联 value 集合键及主表映射。
     * @return 本次清理的 key 数量
     */
    CompletionStage<Integer> execute() {
        return executor.evalWriteAsync(name, LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                "local expiredKeys = redis.call('zrangebyscore', KEYS[2], 0, ARGV[1], 'limit', 0, ARGV[2]); "
              + "if #expiredKeys > 0 then "
                  + "redis.call('zrem', KEYS[2], unpack(expiredKeys)); "

                  + "local values = redis.call('hmget', KEYS[1], unpack(expiredKeys)); "
                  + "local keys = {}; "
                  + "for i, v in ipairs(values) do " +
                        // value 非空时构造 {mapName}:value 形式的子键并删除
                        "if v ~= false then "
                        + "local name = '{' .. KEYS[1] .. '}:' .. v; "
                        + "table.insert(keys, name); "
                      + "end;"
                  + "end; "
                  + "if #keys > 0 then "
                      + "redis.call('del', unpack(keys)); "
                  + "end; "

                  + "redis.call('hdel', KEYS[1], unpack(expiredKeys)); "
              + "end; "
              + "return #expiredKeys;",
              Arrays.asList(name, timeoutSetName), System.currentTimeMillis(), keysLimit);
    }
    
}
