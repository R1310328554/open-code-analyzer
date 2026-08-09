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

import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.AsyncChunkProcessor;
import org.redisson.misc.AsyncChunkProcessor.ChunkExecution;

import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 * 快速联锁看门狗续期任务：
 * 通过 Lua 脚本在单个 Redis Hash 键上批量校验并延长
 * 多个锁字段的过期时间，适用于 {@link org.redisson.RedissonMultiLock}。
 * <p>
 * 分块调用 {@link AsyncChunkProcessor}，chunk 大小固定为 1。
 *
 * @author Nikita Koksharov
 *
 */
public class FastMultilockTask extends LockTask {

    /** @param internalLockLeaseTime 看门狗 lease 毫秒数 @param executor 命令执行器 */
    public FastMultilockTask(long internalLockLeaseTime, CommandAsyncExecutor executor) {
        super(internalLockLeaseTime, executor, 1);
    }

    /** 迭代所有注册锁名，分块执行 Lua 续期。 */
    @Override
    CompletionStage<Void> renew(Iterator<String> iter, int chunkSize) {
        return AsyncChunkProcessor.processAll(iter, chunkSize, this::buildChunk);
    }

    /** 构建单块联锁续期：校验各字段仍由当前线程持有。 */
    private ChunkExecution<Boolean> buildChunk(Iterator<String> iter, int chunkSize) {
        Map<String, Long> name2lockName = new HashMap<>();
        List<Object> args = new ArrayList<>();
        args.add(internalLockLeaseTime);
        args.add(System.currentTimeMillis());

        List<String> keys = new ArrayList<>(chunkSize);

        // 组装 chunk，跳过已失效条目
        while (iter.hasNext() && keys.size() < chunkSize) {
            String key = iter.next();

            FastMultilockEntry entry = (FastMultilockEntry) name2entry.get(key);
            if (entry == null) {
                continue;
            }

            Long threadId = entry.getFirstThreadId();
            if (threadId == null) {
                continue;
            }

            keys.add(key);
            args.add(entry.getLockName(threadId));
            args.addAll(entry.getFields());
            name2lockName.put(key, threadId);
        }

        // 无有效条目则结束本轮
        if (keys.isEmpty()) {
            return null;
        }

        String firstName = keys.get(0);

        CompletionStage<Boolean> f = executor.syncedEval(firstName, LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                        "local leaseTime = tonumber(ARGV[1]);" +
                        "local currentTime = tonumber(ARGV[2]);" +
                        "local currentThread = ARGV[3];" +
                        "if (redis.call('exists',KEYS[1]) > 0) then" +
                        "   local newExpireTime = leaseTime + currentTime;" +
                        "   for i=4, #ARGV, 1 do " +
                        "       local lockThread = redis.call('hget', KEYS[1], ARGV[i]);" +
                        "       if(lockThread ~= false and lockThread == currentThread) then " +
                        "           local expireFieldName = ARGV[i]..':'..lockThread..':expire_time';" +
                        "           local expireTime = redis.call('hget', KEYS[1], expireFieldName);" +
                        "           if(tonumber(expireTime) < newExpireTime) then " +
                        "               redis.call('hset', KEYS[1],expireFieldName, newExpireTime);" +
                        "           end;" +
                        "       else" +
                        "           return 0;" +
                        "       end;" +
                        "   end; " +
                        "   local expireTime = redis.call('pttl',KEYS[1]);" +
                        "   if(tonumber(expireTime) < tonumber(leaseTime)) then " +
                        "       redis.call('pexpire',KEYS[1], leaseTime);" +
                        "   end;" +
                        "   return 1;" +
                        "end;" +
                        "return 0;",
                Collections.singletonList(firstName),
                args.toArray());

        // Lua 返回 0 表示锁已丢失，取消对应续期注册
        return new ChunkExecution<>(f, exists -> {
            if (!exists) {
                cancelExpirationRenewal(firstName, name2lockName.get(firstName));
            }
        });
    }

    /** 注册一条联锁续期：rawName 为 Redis 键，fields 为 Hash 字段。 */
    public void add(String rawName, String lockName, long threadId, Collection<String> fields) {
        FastMultilockEntry entry = new FastMultilockEntry(fields);
        entry.addThreadId(threadId, lockName);

        add(rawName, lockName, threadId, entry);
    }

}
