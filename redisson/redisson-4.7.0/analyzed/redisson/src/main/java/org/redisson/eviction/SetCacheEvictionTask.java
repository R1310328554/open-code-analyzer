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

import org.redisson.RedissonObject;
import org.redisson.api.RFuture;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

/**
 * {@link org.redisson.RedissonSetCache} 过期清理任务。
 * <p>
 * 与 {@link ScoredSetEvictionTask} 不同，删除前向过期频道发布每个过期 value，
 * 以便 {@code SetExpiredListener} 收到通知。
 *
 * @author Nikita Koksharov
 *
 */
public class SetCacheEvictionTask extends EvictionTask {

    /** SetCache 有序集合 Redis 键名（member 为 value，score 为过期时间）。 */
    private final String name;
    /** 过期事件 Pub/Sub 频道名。 */
    private final String expiredChannelName;
    /** 分布式单次执行锁键名。 */
    private final String executeTaskOnceLatchName;
    /** Lua 中使用的发布命令名。 */
    private final String publishCommand;

    /** 构造 SetCache 过期清理任务。 */
    public SetCacheEvictionTask(String name, String expiredChannelName,
                                CommandAsyncExecutor executor, String publishCommand) {
        super(executor);
        this.name = name;
        this.expiredChannelName = expiredChannelName;
        this.executeTaskOnceLatchName = RedissonObject.prefixName("redisson__execute_task_once_latch", name);
        this.publishCommand = publishCommand;
    }

    /** 返回被清理结构的名称。 */
    @Override
    String getName() {
        return name;
    }

    /** 抢锁后扫描过期 value、发布通知并批量 ZREM。 */
    @Override
    CompletionStage<Integer> execute() {
        int latchExpireTime = Math.min(delay, 30); // 锁 TTL 上限

        RFuture<Integer> expiredFuture = executor.evalWriteNoRetryAsync(name, LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                "if redis.call('setnx', KEYS[3], ARGV[4]) == 0 then "
                 + "return -1;"
              + "end;"
              + "redis.call('expire', KEYS[3], ARGV[3]); "
              + "local expiredValues = redis.call('zrangebyscore', KEYS[1], 0, ARGV[1], 'limit', 0, ARGV[2]); "
              + "for i, v in ipairs(expiredValues) do "
                  + "local msg = struct.pack('Lc0', string.len(v), v); "
                  + "local listeners = redis.call(ARGV[5], KEYS[2], msg); "
                  + "if (listeners == 0) then "
                      + "break;"
                  + "end; "
              + "end;"
              + "for i=1, #expiredValues, 5000 do "
                  + "redis.call('zrem', KEYS[1], unpack(expiredValues, i, math.min(i+4999, table.getn(expiredValues)))); "
              + "end; "
              + "return #expiredValues;",
              Arrays.asList(name, expiredChannelName, executeTaskOnceLatchName),
              System.currentTimeMillis(), keysLimit, latchExpireTime, 1, publishCommand);

        return expiredFuture;
    }

}
