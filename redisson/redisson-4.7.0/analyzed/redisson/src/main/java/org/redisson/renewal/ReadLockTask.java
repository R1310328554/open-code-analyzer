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
 * 读写锁读锁看门狗续期任务：
 * Lua 脚本批量延长主 Hash 键与各读锁 {@code rwlock_timeout} 子键 TTL。
 * <p>
 * 读锁可重入且多线程共享；续期需遍历条目内所有 lockName 字段。
 *
 * @author Nikita Koksharov
 *
 */
public class ReadLockTask extends LockTask {

    /** @param internalLockLeaseTime lease 毫秒 @param executor 执行器 @param chunkSize 批大小 */
    public ReadLockTask(long internalLockLeaseTime, CommandAsyncExecutor executor, int chunkSize) {
        super(internalLockLeaseTime, executor, chunkSize);
    }

    /** 分块续期所有读锁注册项。 */
    @Override
    CompletionStage<Void> renew(Iterator<String> iter, int chunkSize) {
        return AsyncChunkProcessor.processAll(iter, chunkSize, this::buildChunk);
    }

    /** 组装读锁 Lua 参数：主键、keyPrefix、各 lockName 列表。 */
    private ChunkExecution<List<Object>> buildChunk(Iterator<String> iter, int chunkSize) {
        Map<String, List<Long>> name2threadIds = new HashMap<>();
        List<Object> args = new ArrayList<>();
        args.add(internalLockLeaseTime);

        List<Object> keys = new ArrayList<>(chunkSize);
        List<Object> keysArgs = new ArrayList<>(chunkSize);

        // 跳过无效 ReadLockEntry
        while (iter.hasNext() && keys.size() < chunkSize) {
            String key = iter.next();

            ReadLockEntry entry = (ReadLockEntry) name2entry.get(key);
            if (entry == null) {
                continue;
            }

            Long threadId = entry.getFirstThreadId();
            if (threadId == null) {
                continue;
            }

            String keyPrefix = entry.getKeyPrefix(threadId);
            if (keyPrefix == null) {
                continue;
            }

            keys.add(key);
            keysArgs.add(key);
            keysArgs.add(keyPrefix);

            Map<Long, String> snapshot = new LinkedHashMap<>(entry.threadId2lockName);
            List<String> lockNames = new ArrayList<>(snapshot.values());
            args.add(lockNames.size());
            args.addAll(lockNames);

            List<Long> threadIds = new ArrayList<>(snapshot.keySet());
            name2threadIds.put(key, threadIds);
        }

        // 无有效读锁条目
        if (keys.isEmpty()) {
            return null;
        }

        String firstName = keys.get(0).toString();

        CompletionStage<List<Object>> f = executor.syncedEval(firstName, LongCodec.INSTANCE,
                new RedisCommand<>("EVAL", new ContainsDecoder<>(keys)),
          "local result = {} " +
                "local argIdx = 2 " +
                "for i = 1, #KEYS, 2 do " +
                    "local anyAlive = false; " +
                    "local lockNamesCount = tonumber(ARGV[argIdx]); " +
                    "argIdx = argIdx + 1; " +
                    "for k = 1, lockNamesCount do " +
                        "local counter = redis.call('hget', KEYS[i], ARGV[argIdx]); " +
                        "if (counter ~= false) then " +
                            "anyAlive = true; " +
                            "for c=counter, 1, -1 do " +
                                "redis.call('pexpire', KEYS[i+1] .. ':' .. ARGV[argIdx] .. ':rwlock_timeout:' .. c, ARGV[1]); " +
                            "end; " +
                        "end; " +
                        "argIdx = argIdx + 1; " +
                    "end; " +
                    "if (anyAlive) then " +
                        "redis.call('pexpire', KEYS[i], ARGV[1]); " +
                        "table.insert(result, 1); " +
                    "else " +
                        "table.insert(result, 0); " +
                    "end; " +
                "end; " +
                "return result;",
                keysArgs,
                args.toArray());

        // 续期失败的键：对该键下所有 threadId 取消续期
        return new ChunkExecution<>(f, existingNames -> {
            keys.removeAll(existingNames);
            for (Object k : keys) {
                String key = k.toString();
                List<Long> threadIds = name2threadIds.get(key);
                if (threadIds != null) {
                    for (Long threadId : threadIds) {
                        cancelExpirationRenewal(key, threadId);
                    }
                }
            }
        });
    }

    /** 注册读锁续期；首次条目时启动定时任务。 */
    public void add(String rawName, String lockName, long threadId, String keyPrefix) {
        addSlotName(rawName);

        ReadLockEntry entry = new ReadLockEntry();
        entry.addThreadId(threadId, lockName, keyPrefix);

        ReadLockEntry oldEntry = (ReadLockEntry) name2entry.putIfAbsent(rawName, entry);
        if (oldEntry != null) {
            oldEntry.addThreadId(threadId, lockName, keyPrefix);
        } else {
            if (tryRun()) {
                schedule();
            }
        }
    }

}
