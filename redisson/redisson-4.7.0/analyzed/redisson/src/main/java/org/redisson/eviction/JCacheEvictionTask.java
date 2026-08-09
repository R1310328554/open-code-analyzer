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
 * JSR-107（JCache）缓存过期清理任务。
 * <p>
 * 从超时有序集合中扫描已过期 key，向过期频道发布 key/value 通知后
 * 从超时集合与哈希表一并删除。
 *
 * @author Nikita Koksharov
 *
 */
public class JCacheEvictionTask extends EvictionTask {

    /** JCache 哈希表 Redis 键名。 */
    private final String name;
    /** 记录各 entry 过期时间戳的有序集合键名。 */
    private final String timeoutSetName;
    /** 过期事件 Pub/Sub 频道名。 */
    private final String expiredChannelName;
    
    /** 构造 JCache 过期清理任务。 */
    public JCacheEvictionTask(String name, String timeoutSetName, String expiredChannelName, CommandAsyncExecutor executor) {
        super(executor);
        this.name = name;
        this.timeoutSetName = timeoutSetName;
        this.expiredChannelName = expiredChannelName;
    }

    /** 返回被清理结构的名称，供日志使用。 */
    @Override
    String getName() {
        return name;
    }
    
    /**
     * 执行一次清理：Lua 脚本批量取过期 key、发布通知并删除。
     * @return 本次清理的条目数
     */
    @Override
    CompletionStage<Integer> execute() {
        return executor.evalWriteAsync(name, LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                // 按当前时间与 keysLimit 从超时 ZSET 取过期 key
                "local expiredKeys = redis.call('zrangebyscore', KEYS[2], 0, ARGV[1], 'limit', 0, ARGV[2]); "
              + "for i, k in ipairs(expiredKeys) do "
                  + "local v = redis.call('hget', KEYS[1], k);"
                  + "local msg = struct.pack('Lc0Lc0', string.len(tostring(k)), tostring(k), string.len(tostring(v)), tostring(v));"
                  // 向过期频道发布 key/value 二进制消息
                  + "redis.call('publish', KEYS[3], msg);"
              + "end; "
              + "if #expiredKeys > 0 then "
                  + "redis.call('zrem', KEYS[2], unpack(expiredKeys)); "
                  // 从 JCache 哈希表删除过期 entry
                  + "redis.call('hdel', KEYS[1], unpack(expiredKeys)); "
              + "end; "
              + "return #expiredKeys;",
              Arrays.<Object>asList(name, timeoutSetName, expiredChannelName), System.currentTimeMillis(), keysLimit);
    }
    
}
