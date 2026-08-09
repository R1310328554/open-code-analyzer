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
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.decoder.ContainsDecoder;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.AsyncChunkProcessor;
import org.redisson.misc.AsyncChunkProcessor.ChunkExecution;

import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 * 普通分布式锁看门狗续期任务：
 * 分块执行 Lua EVAL，对每个 Redis Hash 锁键调用 {@code pexpire}
 * 延长 TTL；若字段已不存在则取消本地续期注册。
 * <p>
 * 继承 {@link RenewalTask} 的定时调度与条目管理。
 *
 * @author Nikita Koksharov
 *
 */
public class LockTask extends RenewalTask {

    /** @param internalLockLeaseTime lease 毫秒 @param executor 执行器 @param chunkSize 批大小 */
    public LockTask(long internalLockLeaseTime,
                    CommandAsyncExecutor executor, int chunkSize) {
        super(internalLockLeaseTime, executor, chunkSize);
    }

    /** 分块续期所有已注册锁名。 */
    @Override
    CompletionStage<Void> renew(Iterator<String> iter, int chunkSize) {
        return AsyncChunkProcessor.processAll(iter, chunkSize, this::buildChunk);
    }

    /** 构建一批锁键与对应 lockName 参数，执行批量 pexpire Lua。 */
    private ChunkExecution<List<String>> buildChunk(Iterator<String> iter, int chunkSize) {
        Map<String, Long> name2threadId = new HashMap<>(chunkSize);
        List<Object> args = new ArrayList<>(chunkSize + 1);
        args.add(internalLockLeaseTime);

        List<String> keys = new ArrayList<>(chunkSize);

        // 跳过无有效 threadId 或 lockName 的条目
        while (iter.hasNext() && keys.size() < chunkSize) {
            String key = iter.next();

            LockEntry entry = name2entry.get(key);
            if (entry == null) {
                continue;
            }
            Long threadId = entry.getFirstThreadId();
            if (threadId == null) {
                continue;
            }

            String lockName = entry.getLockName(threadId);
            if (lockName == null) {
                continue;
            }

            keys.add(key);
            args.add(lockName);
            name2threadId.put(key, threadId);
        }

        // 本块无有效键则返回 null 结束
        if (keys.isEmpty()) {
            return null;
        }

        String firstName = keys.get(0);

        CompletionStage<List<String>> f = executor.syncedEval(firstName, LongCodec.INSTANCE,
                new RedisCommand<>("EVAL", new ContainsDecoder<>(keys)),
                  "local result = {} " +
                        "for i = 1, #KEYS, 1 do " +
                            "if (redis.call('hexists', KEYS[i], ARGV[i + 1]) == 1) then " +
                                "redis.call('pexpire', KEYS[i], ARGV[1]); " +
                                "table.insert(result, 1); " +
                            "else " +
                                "table.insert(result, 0); " +
                            "end; " +
                        "end; " +
                        "return result;",
                new ArrayList<>(keys),
                args.toArray());

        // Lua 返回仍存在的键；其余视为已释放并 cancel
        return new ChunkExecution<>(f, existingNames -> {
            keys.removeAll(existingNames);
            for (String key : keys) {
                cancelExpirationRenewal(key, name2threadId.get(key));
            }
        });
    }

    /** 注册单锁续期条目并触发调度。 */
    public void add(String rawName, String lockName, long threadId) {
        LockEntry entry = new LockEntry();
        entry.addThreadId(threadId, lockName);

        add(rawName, lockName, threadId, entry);
    }

}
