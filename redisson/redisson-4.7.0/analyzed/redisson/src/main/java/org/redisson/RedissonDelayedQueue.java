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

import org.redisson.api.RDelayedQueue;
import org.redisson.api.RFuture;
import org.redisson.api.RTopic;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.api.queue.QueueMoveElementsArgs;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 延迟队列 {@link RDelayedQueue} 实现（已废弃）。
 * <p>通过 ZSET 调度到期元素并转移到目标队列；后台 {@link QueueTransferTask} 轮询迁移。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
@Deprecated
public class RedissonDelayedQueue<V> extends RedissonExpirable implements RDelayedQueue<V> {

    /** 延迟队列到期通知 Pub/Sub 通道。 */
    private final String channelName;
    /** 目标队列 Redis 键名。 */
    private final String queueName;
    /** 延迟元素 ZSET 键名。 */
    private final String timeoutSetName;
    
    protected RedissonDelayedQueue(Codec codec, CommandAsyncExecutor commandExecutor, String name) {
        super(codec, commandExecutor, name);
        channelName = prefixName("redisson_delay_queue_channel", getRawName());
        queueName = prefixName("redisson_delay_queue", getRawName());
        timeoutSetName = getTimeoutSetName(getRawName());
        
        QueueTransferTask task = new QueueTransferTask(commandExecutor.getServiceManager()) {
            
            @Override
            protected RFuture<Long> pushTaskAsync() {
                return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_LONG,
                        "local expiredValues = redis.call('zrangebyscore', KEYS[2], 0, ARGV[1], 'limit', 0, ARGV[2]); "
                      + "if #expiredValues > 0 then "
                          + "for i, v in ipairs(expiredValues) do "
                              + "local randomId, value = struct.unpack('Bc0Lc0', v);"
                              + "redis.call('rpush', KEYS[1], value);"
                              + "redis.call('lrem', KEYS[3], 1, v);"
                          + "end; "
                          + "redis.call('zrem', KEYS[2], unpack(expiredValues));"
                      + "end; "
                        // get startTime from scheduler queue head task
                      + "local v = redis.call('zrange', KEYS[2], 0, 0, 'WITHSCORES'); "
                      + "if v[1] ~= nil then "
                         + "return v[2]; "
                      + "end "
                      + "return nil;",
                      Arrays.asList(getRawName(), timeoutSetName, queueName),
                      System.currentTimeMillis(), 100);
            }
            
            @Override
            protected RTopic getTopic() {
                return RedissonTopic.createRaw(LongCodec.INSTANCE, commandExecutor, channelName);
            }
        };

        commandExecutor.getServiceManager().getQueueTransferService().schedule(queueName, task);
    }

    /** 获取 TimeoutSetName。 */
    private String getTimeoutSetName(String rawName) {
        return prefixName("redisson_delay_queue_timeout", rawName);
    }

    /** 向延迟队列添加带延迟的元素。 */
    @Override
    public void offer(V e, long delay, TimeUnit timeUnit) {
        get(offerAsync(e, delay, timeUnit));
    }
    
    /** 异步添加延迟元素。 */
    @Override
    public RFuture<Void> offerAsync(V e, long delay, TimeUnit timeUnit) {
        if (delay < 0) {
            throw new IllegalArgumentException("Delay can't be negative");
        }
        
        long delayInMs = timeUnit.toMillis(delay);
        long timeout = System.currentTimeMillis() + delayInMs;

        byte[] random = getServiceManager().generateIdArray(8);
        return commandExecutor.evalWriteNoRetryAsync(getRawName(), codec, RedisCommands.EVAL_VOID,
                "local value = struct.pack('Bc0Lc0', string.len(ARGV[2]), ARGV[2], string.len(ARGV[3]), ARGV[3]);"
              + "redis.call('zadd', KEYS[2], ARGV[1], value);"
              + "redis.call('rpush', KEYS[3], value);"
              // if new object added to queue head when publish its startTime 
              // to all scheduler workers 
              + "local v = redis.call('zrange', KEYS[2], 0, 0); "
              + "if v[1] == value then "
                 + "redis.call('publish', KEYS[4], ARGV[1]); "
              + "end;",
              Arrays.asList(getRawName(), timeoutSetName, queueName, channelName),
              timeout, random, encode(e));
    }

    /** 追加元素（满则覆盖最旧）。 */
    @Override
    public boolean add(V e) {
        throw new UnsupportedOperationException("Use 'offer' method with timeout param");
    }

    /** 向延迟队列添加带延迟的元素。 */
    @Override
    public boolean offer(V e) {
        throw new UnsupportedOperationException("Use 'offer' method with timeout param");
    }

    /** 从延迟队列移除元素。 */
    @Override
    public V remove() {
        V value = poll();
        if (value == null) {
            throw new NoSuchElementException();
        }
        return value;
    }

    /** 出队队首元素。 */
    @Override
    public V poll() {
        return get(pollAsync());
    }

    /** 延迟队列 element 操作。 */
    @Override
    public V element() {
        V value = peek();
        if (value == null) {
            throw new NoSuchElementException();
        }
        return value;
    }

    /** 延迟队列 peek 操作。 */
    @Override
    public V peek() {
        return get(peekAsync());
    }

    /** 返回元素/条目数量。 */
    @Override
    public int size() {
        return get(sizeAsync());
    }

    /** 是否为空。 */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /** 延迟队列 contains 操作。 */
    @Override
    public boolean contains(Object o) {
        return get(containsAsync(o));
    }

    V getValue(int index) {
        return (V) get(commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                "local v = redis.call('lindex', KEYS[1], ARGV[1]); "
              + "if v ~= false then "
                  + "local randomId, value = struct.unpack('Bc0Lc0', v);"
                  + "return value; "
              + "end "
              + "return nil;",
              Arrays.<Object>asList(queueName), index));
    }
    
    void remove(int index) {
        get(commandExecutor.evalWriteAsync(getRawName(), null, RedisCommands.EVAL_VOID,
                "local v = redis.call('lindex', KEYS[1], ARGV[1]);" + 
                "if v ~= false then " + 
                   "local randomId, value = struct.unpack('Bc0Lc0', v);" +
                   "redis.call('lrem', KEYS[1], 1, v);" + 
                   "redis.call('zrem', KEYS[2], v);" +
                "end; ",
                Arrays.<Object>asList(queueName, timeoutSetName), index));
    }
    
    /** 延迟队列 iterator 操作。 */
    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {

            private V nextCurrentValue;
            private V currentValueHasRead;
            private int currentIndex = -1;
            private boolean hasBeenModified = true;

            @Override
            public boolean hasNext() {
                V val = RedissonDelayedQueue.this.getValue(currentIndex+1);
                if (val != null) {
                    nextCurrentValue = val;
                }
                return val != null;
            }

            @Override
            public V next() {
                if (nextCurrentValue == null && !hasNext()) {
                    throw new NoSuchElementException("No such element at index " + currentIndex);
                }
                currentIndex++;
                currentValueHasRead = nextCurrentValue;
                nextCurrentValue = null;
                hasBeenModified = false;
                return currentValueHasRead;
            }

            @Override
            public void remove() {
                if (currentValueHasRead == null) {
                    throw new IllegalStateException("Neither next nor previous have been called");
                }
                if (hasBeenModified) {
                    throw new IllegalStateException("Element been already deleted");
                }
                RedissonDelayedQueue.this.remove(currentIndex);
                currentIndex--;
                hasBeenModified = true;
                currentValueHasRead = null;
            }

        };
    }

    /** 延迟队列 toArray 操作。 */
    @Override
    public Object[] toArray() {
        List<V> list = readAll();
        return list.toArray();
    }
    
    /** 延迟队列 toArray 操作。 */
    @Override
    public <T> T[] toArray(T[] a) {
        List<V> list = readAll();
        return list.toArray(a);
    }

    /** 延迟队列 readAll 操作。 */
    @Override
    public List<V> readAll() {
        return get(readAllAsync());
    }

    /** 出队队首元素。 */
    @Override
    public List<V> poll(int limit) {
        return get(pollAsync(limit));
    }

    /** 异步执行 readAll。 */
    @Override
    public RFuture<List<V>> readAllAsync() {
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_LIST,
                "local result = {}; " +
                "local items = redis.call('lrange', KEYS[1], 0, -1); "
              + "for i, v in ipairs(items) do "
                   + "local randomId, value = struct.unpack('Bc0Lc0', v); "
                   + "table.insert(result, value);"
              + "end; "
              + "return result; ",
           Collections.singletonList(queueName));
    }

    /** 异步出队。 */
    @Override
    public RFuture<List<V>> pollAsync(int limit) {
        return commandExecutor.evalWriteNoRetryAsync(getRawName(), codec, RedisCommands.EVAL_LIST,
                   "local result = {};"
                 + "for i = 1, ARGV[1], 1 do " +
                       "local v = redis.call('lpop', KEYS[1]);" +
                       "if v ~= false then " +
                           "redis.call('zrem', KEYS[2], v); " +
                           "local randomId, value = struct.unpack('Bc0Lc0', v);" +
                           "table.insert(result, value);" +
                       "else " +
                           "return result;" +
                       "end;" +
                   "end; " +
                   "return result;",
                Arrays.asList(queueName, timeoutSetName), limit);
    }

    /** 从延迟队列移除元素。 */
    @Override
    public boolean remove(Object o) {
        return get(removeAsync(o));
    }

    /** 异步移除。 */
    @Override
    public RFuture<Boolean> removeAsync(Object o) {
        return removeAsync(o, 1);
    }

    /** 异步移除。 */
    protected RFuture<Boolean> removeAsync(Object o, int count) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local s = redis.call('llen', KEYS[1]);" +
                "for i = 0, s-1, 1 do "
                    + "local v = redis.call('lindex', KEYS[1], i);"
                    + "local randomId, value = struct.unpack('Bc0Lc0', v);"
                    + "if ARGV[1] == value then "
                        + "redis.call('zrem', KEYS[2], v);"
                        + "redis.call('lrem', KEYS[1], 1, v);"
                        + "return 1;"
                    + "end; "
               + "end;" +
               "return 0;",
        Arrays.<Object>asList(queueName, timeoutSetName), encode(o));
    }

    /** 异步执行 containsAll。 */
    @Override
    public RFuture<Boolean> containsAllAsync(Collection<?> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(true);
        }

        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local s = redis.call('llen', KEYS[1]);" +
                "for i = 0, s-1, 1 do "
                    + "local v = redis.call('lindex', KEYS[1], i);"
                    + "local randomId, value = struct.unpack('Bc0Lc0', v);"
                    
                    + "for j = #ARGV, 1, -1 do "
                        + "if value == ARGV[j] then "
                          + "table.remove(ARGV, j) "
                        + "end; "
                    + "end; "
               + "end;" +
               "return #ARGV == 0 and 1 or 0;",
                Collections.<Object>singletonList(queueName), encode(c).toArray());
    }

    /** 延迟队列 containsAll 操作。 */
    @Override
    public boolean containsAll(Collection<?> c) {
        return get(containsAllAsync(c));
    }

    /** 延迟队列 addAll 操作。 */
    @Override
    public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException("Use 'offer' method with timeout param");
    }

    /** 异步 removeAll。 */
    @Override
    public RFuture<Boolean> removeAllAsync(Collection<?> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(false);
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local result = 0;" + 
                "local s = redis.call('llen', KEYS[1]);" + 
                "local i = 0;" +
                "while i < s do "
                    + "local v = redis.call('lindex', KEYS[1], i);"
                    + "local randomId, value = struct.unpack('Bc0Lc0', v);"
                    
                    + "for j = 1, #ARGV, 1 do "
                        + "if value == ARGV[j] then "
                            + "result = 1; "
                            + "i = i - 1; "
                            + "s = s - 1; "
                            + "redis.call('zrem', KEYS[2], v);"
                            + "redis.call('lrem', KEYS[1], 0, v); "
                            + "break; "
                        + "end; "
                    + "end; "
                    + "i = i + 1;"
               + "end; " 
               + "return result;",
               Arrays.asList(queueName, timeoutSetName), encode(c).toArray());
    }

    /** 移除 key 下全部值。 */
    @Override
    public boolean removeAll(Collection<?> c) {
        return get(removeAllAsync(c));
    }

    /** 延迟队列 retainAll 操作。 */
    @Override
    public boolean retainAll(Collection<?> c) {
        return get(retainAllAsync(c));
    }

    /** 异步执行 retainAll。 */
    @Override
    public RFuture<Boolean> retainAllAsync(Collection<?> c) {
        if (c.isEmpty()) {
            return deleteAsync();
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                     "local changed = 0; " +
                     "local items = redis.call('lrange', KEYS[1], 0, -1); "
                   + "local i = 1; "
                   + "while i <= #items do "
                        + "local randomId, element = struct.unpack('Bc0Lc0', items[i]); "
                        + "local isInAgrs = false; "
                        + "for j = 1, #ARGV, 1 do "
                            + "if ARGV[j] == element then "
                                + "isInAgrs = true; "
                                + "break; "
                            + "end; "
                        + "end; "
                        + "if isInAgrs == false then "
                            + "redis.call('LREM', KEYS[1], 0, items[i]) "
                            + "changed = 1; "
                        + "end; "
                        + "i = i + 1; "
                   + "end; "
                   + "return changed; ",
                Collections.singletonList(queueName), encode(c).toArray());
    }  

    /** 清空全部条目。 */
    @Override
    public void clear() {
        delete();
    }
    
    /** 异步 JSON 删除。 */
    @Override
    public RFuture<Boolean> deleteAsync() {
        return deleteAsync(queueName, timeoutSetName);
    }
    
    /** 异步执行 sizeInMemory。 */
    @Override
    public RFuture<Long> sizeInMemoryAsync() {
        List<Object> keys = Arrays.asList(queueName, timeoutSetName);
        return super.sizeInMemoryAsync(keys);
    }

    /** 异步执行 copy。 */
    @Override
    public RFuture<Boolean> copyAsync(List<Object> keys, int database, boolean replace) {
        String newName = (String) keys.get(1);
        List<Object> kks = Arrays.asList(queueName, timeoutSetName,
                newName, getTimeoutSetName(newName));
        return super.copyAsync(kks, database, replace);
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {
        return super.expireAsync(timeToLive, timeUnit, param, queueName, timeoutSetName);
    }

    /** 异步执行 expireAt。 */
    @Override
    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
        return super.expireAtAsync(timestamp, param, queueName, timeoutSetName);
    }

    /** 异步清除 TTL。 */
    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return clearExpireAsync(queueName, timeoutSetName);
    }

    /** 异步执行 peek。 */
    @Override
    public RFuture<V> peekAsync() {
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                "local v = redis.call('lindex', KEYS[1], 0); "
              + "if v ~= false then "
                  + "local randomId, value = struct.unpack('Bc0Lc0', v);"
                  + "return value; "
              + "end "
              + "return nil;",
              Arrays.asList(queueName));
    }

    /** 异步出队。 */
    @Override
    public RFuture<V> pollAsync() {
        return commandExecutor.evalWriteNoRetryAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                  "local v = redis.call('lpop', KEYS[1]); "
                + "if v ~= false then "
                    + "redis.call('zrem', KEYS[2], v); "
                    + "local randomId, value = struct.unpack('Bc0Lc0', v);"
                    + "return value; "
                + "end "
                + "return nil;",
                Arrays.asList(queueName, timeoutSetName));
    }

    /** 异步添加延迟元素。 */
    @Override
    public RFuture<Boolean> offerAsync(V e) {
        throw new UnsupportedOperationException("Use 'offer' method with timeout param");
    }

    /** 异步执行 pollLastAndOfferFirstTo。 */
    @Override
    public RFuture<V> pollLastAndOfferFirstToAsync(String queueName) {
        return commandExecutor.evalWriteNoRetryAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                "local v = redis.call('rpop', KEYS[1]); "
              + "if v ~= false then "
                  + "redis.call('zrem', KEYS[2], v); "
                  + "local randomId, value = struct.unpack('Bc0Lc0', v);"
                  + "redis.call('lpush', KEYS[3], value); "
                  + "return value; "
              + "end "
              + "return nil;",
              Arrays.asList(this.queueName, timeoutSetName, queueName));
    }

    /** 异步执行 contains。 */
    @Override
    public RFuture<Boolean> containsAsync(Object o) {
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                        "local s = redis.call('llen', KEYS[1]);" +
                        "for i = 0, s-1, 1 do "
                            + "local v = redis.call('lindex', KEYS[1], i);"
                            + "local randomId, value = struct.unpack('Bc0Lc0', v);"
                            + "if ARGV[1] == value then "
                                + "return 1;"
                            + "end; "
                       + "end;" +
                       "return 0;",
                Collections.singletonList(queueName), encode(o));
    }

    /** 延迟队列 indexOf 操作。 */
    @Override
    public int indexOf(V e) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 indexOf。 */
    @Override
    public RFuture<Integer> indexOfAsync(V e) {
        throw new UnsupportedOperationException();
    }

    /** 异步返回数量。 */
    @Override
    public RFuture<Integer> sizeAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.LLEN_INT, queueName);
    }

    /** 异步追加元素。 */
    @Override
    public RFuture<Boolean> addAsync(V e) {
        throw new UnsupportedOperationException("Use 'offer' method with timeout param");
    }

    /** 异步执行 addAll。 */
    @Override
    public RFuture<Boolean> addAllAsync(Collection<? extends V> c) {
        throw new UnsupportedOperationException("Use 'offer' method with timeout param");
    }

    /** 延迟队列 pollLastAndOfferFirstTo 操作。 */
    @Override
    public V pollLastAndOfferFirstTo(String dequeName) {
        return get(pollLastAndOfferFirstToAsync(dequeName));
    }

    /** 将键移动到指定数据库。 */
    @Override
    public List<V> move(QueueMoveElementsArgs args) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 move。 */
    @Override
    public RFuture<List<V>> moveAsync(QueueMoveElementsArgs args) {
        throw new UnsupportedOperationException();
    }
    /** 销毁延迟队列后台任务。 */
    @Override
    public void destroy() {
        commandExecutor.getServiceManager().getQueueTransferService().remove(queueName);
        removeListeners();
    }
    
}
