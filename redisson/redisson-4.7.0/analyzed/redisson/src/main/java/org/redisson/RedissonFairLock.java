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
package org.redisson;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.pubsub.LockPubSub;

/**
 * {@link java.util.concurrent.locks.Lock} 的分布式<b>公平</b>可重入锁实现。
 * <p>客户端断开连接后锁会自动释放；通过等待队列与超时有序集合保证 FIFO 获取顺序。
 *
 * @author Nikita Koksharov
 */
public class RedissonFairLock extends RedissonLock implements RLock {

    private final long threadWaitTime;
    private final CommandAsyncExecutor commandExecutor;
    final String threadsQueueName;
    final String timeoutSetName;

    public RedissonFairLock(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        this.commandExecutor = commandExecutor;
        this.threadWaitTime = getServiceManager().getCfg().getFairLockWaitTimeout();
        threadsQueueName = prefixName("redisson_lock_queue", getRawName());
        timeoutSetName = prefixName("redisson_lock_timeout", getRawName());
    }

    @Override
    protected CompletableFuture<RedissonLockEntry> subscribe(long threadId) {
        return pubSub.subscribe(getEntryName() + ":" + threadId,
                getChannelName() + ":" + getLockName(threadId));
    }

    @Override
    protected void unsubscribe(RedissonLockEntry entry, long threadId) {
        pubSub.unsubscribe(entry, getEntryName() + ":" + threadId,
                getChannelName() + ":" + getLockName(threadId));
    }

    @Override
    protected CompletableFuture<Void> acquireFailedAsync(long waitTime, TimeUnit unit, long threadId) {
        long wait = threadWaitTime;
        if (waitTime > 0) {
            wait = unit.toMillis(waitTime);
        }

        RFuture<Void> f = commandExecutor.syncedEvalWithRetry(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_VOID,
                // 查找待移除线程在队列中的超时记录
                "local queue = redis.call('lrange', KEYS[1], 0, -1);" +
                        // 定位该线程在公平队列中的位置
                        "local i = 1;" +
                        "while i <= #queue and queue[i] ~= ARGV[1] do " +
                        "i = i + 1;" +
                        "end;" +
                        // 从移除后的下一位置起调整后续等待者
                        "i = i + 1;" +
                        // 为队列中后续线程递减超时时间
                        "while i <= #queue do " +
                        "redis.call('zincrby', KEYS[2], -tonumber(ARGV[2]), queue[i]);" +
                        "i = i + 1;" +
                        "end;" +
                        // 从队列与超时集合中移除该线程
                        "redis.call('zrem', KEYS[2], ARGV[1]);" +
                        "redis.call('lrem', KEYS[1], 0, ARGV[1]);",
                Arrays.asList(threadsQueueName, timeoutSetName),
                getLockName(threadId), wait);
        return f.toCompletableFuture();
    }

    @Override
    <T> RFuture<T> tryLockInnerAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
        long wait = threadWaitTime;
        if (waitTime > 0) {
            wait = unit.toMillis(waitTime);
        }

        long currentTime = System.currentTimeMillis();
        if (command == RedisCommands.EVAL_NULL_BOOLEAN) {
            return commandExecutor.syncedEvalNoRetry(getRawName(), LongCodec.INSTANCE, command,
                    // 清理已超时的过期等待线程
                    "while true do " +
                        "local firstThreadId2 = redis.call('lindex', KEYS[2], 0);" +
                        "if firstThreadId2 == false then " +
                            "break;" +
                        "end;" +
                        "local timeout = redis.call('zscore', KEYS[3], firstThreadId2);" +
                        "if timeout ~= false and tonumber(timeout) <= tonumber(ARGV[3]) then " +
                            // 从队列与超时集合中移除队首过期项
                            // 注意：不修改其余等待者的超时
                            "redis.call('zrem', KEYS[3], firstThreadId2);" +
                            "redis.call('lpop', KEYS[2]);" +
                        "else " +
                            "break;" +
                        "end;" +
                    "end;" +

                    "if (redis.call('exists', KEYS[1]) == 0) " +
                        "and ((redis.call('exists', KEYS[2]) == 0) " +
                            "or (redis.call('lindex', KEYS[2], 0) == ARGV[2])) then " +
                        "redis.call('lpop', KEYS[2]);" +
                        "redis.call('zrem', KEYS[3], ARGV[2]);" +

                        // 为队列中所有等待者递减超时
                        "local keys = redis.call('zrange', KEYS[3], 0, -1);" +
                        "for i = 1, #keys, 1 do " +
                            "redis.call('zincrby', KEYS[3], -tonumber(ARGV[4]), keys[i]);" +
                        "end;" +

                        "redis.call('hset', KEYS[1], ARGV[2], 1);" +
                        "redis.call('pexpire', KEYS[1], ARGV[1]);" +
                        "return nil;" +
                    "end;" +
                    "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                        "redis.call('hincrby', KEYS[1], ARGV[2], 1);" +
                        "redis.call('pexpire', KEYS[1], ARGV[1]);" +
                        "return nil;" +
                    "end;" +
                    "return 1;",
                    Arrays.asList(getRawName(), threadsQueueName, timeoutSetName),
                    unit.toMillis(leaseTime), getLockName(threadId), currentTime, wait);
        }

        if (command == RedisCommands.EVAL_LONG) {
            return commandExecutor.syncedEvalNoRetry(getRawName(), LongCodec.INSTANCE, command,
                    // 清理已超时的过期等待线程
                    "while true do " +
                        "local firstThreadId2 = redis.call('lindex', KEYS[2], 0);" +
                        "if firstThreadId2 == false then " +
                            "break;" +
                        "end;" +

                        "local timeout = redis.call('zscore', KEYS[3], firstThreadId2);" +
                        "if timeout ~= false and tonumber(timeout) <= tonumber(ARGV[4]) then " +
                            // 从队列与超时集合中移除队首过期项
                            // 注意：不修改其余等待者的超时
                            "redis.call('zrem', KEYS[3], firstThreadId2);" +
                            "redis.call('lpop', KEYS[2]);" +
                        "else " +
                            "break;" +
                        "end;" +
                    "end;" +

                    // 判断当前是否可立即获锁
                    "if (redis.call('exists', KEYS[1]) == 0) " +
                        "and ((redis.call('exists', KEYS[2]) == 0) " +
                            "or (redis.call('lindex', KEYS[2], 0) == ARGV[2])) then " +

                        // 将本线程从队列与超时集合中移除
                        "redis.call('lpop', KEYS[2]);" +
                        "redis.call('zrem', KEYS[3], ARGV[2]);" +

                        // 为队列中所有等待者递减超时
                        "local keys = redis.call('zrange', KEYS[3], 0, -1);" +
                        "for i = 1, #keys, 1 do " +
                            "redis.call('zincrby', KEYS[3], -tonumber(ARGV[3]), keys[i]);" +
                        "end;" +

                        // 获锁并设置租约 TTL
                        "redis.call('hset', KEYS[1], ARGV[2], 1);" +
                        "redis.call('pexpire', KEYS[1], ARGV[1]);" +
                        "return nil;" +
                    "end;" +

                    // 已持有锁：可重入递增计数
                    "if redis.call('hexists', KEYS[1], ARGV[2]) == 1 then " +
                        "redis.call('hincrby', KEYS[1], ARGV[2],1);" +
                        "redis.call('pexpire', KEYS[1], ARGV[1]);" +
                        "return nil;" +
                    "end;" +

                    // 当前无法获锁
                    // 检查线程是否已在等待队列中
                    "local timeout = redis.call('zscore', KEYS[3], ARGV[2]);" +
                    "if timeout ~= false then " +
                            "local ttl = redis.call('pttl', KEYS[1]);" +
                            "return math.max(0, ttl); " +
                        // 真实超时应为前驱线程的超时
                        // 此处为近似值，但可避免遍历整个队列
                        // （完整计算见下方注释掉的 Lua 表达式）
//                        "return timeout - tonumber(ARGV[3]) - tonumber(ARGV[4]);" +
                    "end;" +

                    // 将线程入队到末尾，并在超时集合中设置其超时为
                    // 前驱线程超时（队列为空则用锁 TTL）加上
                    // threadWaitTime
                    "local lastThreadId = redis.call('lindex', KEYS[2], -1);" +
                    "local ttl;" +
                    "if lastThreadId ~= false and lastThreadId ~= ARGV[2] and redis.call('zscore', KEYS[3], lastThreadId) ~= false then " +
                        "ttl = tonumber(redis.call('zscore', KEYS[3], lastThreadId)) - tonumber(ARGV[4]);" +
                    "else " +
                        "ttl = redis.call('pttl', KEYS[1]);" +
                    "end;" +
                    "local timeout = ttl + tonumber(ARGV[3]) + tonumber(ARGV[4]);" +
                    "if redis.call('zadd', KEYS[3], timeout, ARGV[2]) == 1 then " +
                        "redis.call('rpush', KEYS[2], ARGV[2]);" +
                    "end;" +
                    "return ttl;",
                    Arrays.asList(getRawName(), threadsQueueName, timeoutSetName),
                    unit.toMillis(leaseTime), getLockName(threadId), wait, currentTime);
        }

        throw new IllegalArgumentException();
    }

    @Override
    protected RFuture<Boolean> unlockInnerAsync(long threadId, String requestId, long timeout) {
        return evalWriteSyncedNoRetryAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
          "local val = redis.call('get', KEYS[5]); " +
                "if val ~= false then " +
                    "return tonumber(val);" +
                "end; " +

                // 清理已超时的过期等待线程
                "while true do "
                + "local firstThreadId2 = redis.call('lindex', KEYS[2], 0);"
                + "if firstThreadId2 == false then "
                    + "break;"
                + "end; "
                + "local timeout = redis.call('zscore', KEYS[3], firstThreadId2);"
                + "if timeout ~= false and tonumber(timeout) <= tonumber(ARGV[4]) then "
                    + "redis.call('zrem', KEYS[3], firstThreadId2); "
                    + "redis.call('lpop', KEYS[2]); "
                + "else "
                    + "break;"
                + "end; "
              + "end;"
                
              + "if (redis.call('exists', KEYS[1]) == 0) then " + 
                    "local nextThreadId = redis.call('lindex', KEYS[2], 0); " + 
                    "if nextThreadId ~= false then " +
                        "redis.call(ARGV[5], KEYS[4] .. ':' .. nextThreadId, ARGV[1]); " +
                    "end; " +
                    "redis.call('set', KEYS[5], 1, 'px', ARGV[6]); " +
                    "return 1; " +
                "end;" +
                "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                    "return nil;" +
                "end; " +
                "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
                "if (counter > 0) then " +
                    "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "redis.call('set', KEYS[5], 0, 'px', ARGV[6]); " +
                    "return 0; " +
                "end; " +
                    
                "redis.call('del', KEYS[1]); " +
                "redis.call('set', KEYS[5], 1, 'px', ARGV[6]); " +
                "local nextThreadId = redis.call('lindex', KEYS[2], 0); " + 
                "if nextThreadId ~= false then " +
                    "redis.call(ARGV[5], KEYS[4] .. ':' .. nextThreadId, ARGV[1]); " +
                "end; " +
                "return 1; ",
                Arrays.asList(getRawName(), threadsQueueName, timeoutSetName, getChannelName(), getUnlockLatchName(requestId)),
                    LockPubSub.UNLOCK_MESSAGE, internalLockLeaseTime, getLockName(threadId),
                    System.currentTimeMillis(), getSubscribeService().getPublishCommand(), timeout);
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> deleteAsync() {
        return deleteAsync(getRawName(), threadsQueueName, timeoutSetName);
    }

    @Override
    public RFuture<Long> sizeInMemoryAsync() {
        List<Object> keys = Arrays.asList(getRawName(), threadsQueueName, timeoutSetName);
        return super.sizeInMemoryAsync(keys);
    }

    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {
        return super.expireAsync(timeToLive, timeUnit, param, getRawName(), threadsQueueName, timeoutSetName);
    }

    @Override
    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
        return super.expireAtAsync(timestamp, param, getRawName(), threadsQueueName, timeoutSetName);
    }

    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return clearExpireAsync(getRawName(), threadsQueueName, timeoutSetName);
    }

    
    @Override
    public RFuture<Boolean> forceUnlockAsync() {
        cancelExpirationRenewal(null, null);
        return commandExecutor.syncedEvalWithRetry(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                // 清理已超时的过期等待线程
                "while true do "
                + "local firstThreadId2 = redis.call('lindex', KEYS[2], 0);"
                + "if firstThreadId2 == false then "
                    + "break;"
                + "end; "
                + "local timeout = redis.call('zscore', KEYS[3], firstThreadId2);"
                + "if timeout ~= false and tonumber(timeout) <= tonumber(ARGV[2]) then "
                    + "redis.call('zrem', KEYS[3], firstThreadId2); "
                    + "redis.call('lpop', KEYS[2]); "
                + "else "
                    + "break;"
                + "end; "
              + "end;"
                + 
                
                "if (redis.call('del', KEYS[1]) == 1) then " + 
                    "local nextThreadId = redis.call('lindex', KEYS[2], 0); " + 
                    "if nextThreadId ~= false then " +
                        "redis.call(ARGV[3], KEYS[4] .. ':' .. nextThreadId, ARGV[1]); " +
                    "end; " + 
                    "return 1; " + 
                "end; " + 
                "return 0;",
                Arrays.asList(getRawName(), threadsQueueName, timeoutSetName, getChannelName()),
                LockPubSub.UNLOCK_MESSAGE, System.currentTimeMillis(),
                getSubscribeService().getPublishCommand());
    }

}