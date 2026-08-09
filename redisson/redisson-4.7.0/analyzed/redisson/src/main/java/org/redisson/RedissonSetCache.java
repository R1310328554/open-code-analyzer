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

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.redisson.api.*;
import org.redisson.api.listener.MessageListener;
import org.redisson.api.listener.SetAddListener;
import org.redisson.api.listener.SetExpiredListener;
import org.redisson.api.listener.SetRemoveListener;
import org.redisson.api.listener.TrackingListener;
import org.redisson.api.mapreduce.RCollectionMapReduce;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.IntegerCodec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.codec.BaseEventCodec;
import org.redisson.codec.SetCacheEventCodec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.eviction.EvictionScheduler;
import org.redisson.iterator.BaseAsyncIterator;
import org.redisson.iterator.RedissonBaseIterator;
import org.redisson.mapreduce.RedissonCollectionMapReduce;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.misc.CompositeAsyncIterator;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 带逐元素 TTL 的 Set 缓存 {@link RSetCache}。
 * <p>通过 {@link RSetCache#add(Object, long, TimeUnit)} 为每个成员设置过期；
 * 读操作触发惰性过期检查，{@link org.redisson.eviction.EvictionScheduler}
 * 定期异步清理。若无需逐元素 TTL 建议使用 {@link org.redisson.api.RSet}。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public class RedissonSetCache<V> extends RedissonExpirable implements RSetCache<V>, ScanIterator {

    /** 关联 Redisson 客户端（MapReduce 等）。 */
    final RedissonClient redisson;
    /** 过期条目异步淘汰调度器。 */
    final EvictionScheduler evictionScheduler;
    /** 过期事件 Pub/Sub 发布命令名。 */
    final String publishCommand;

    public RedissonSetCache(EvictionScheduler evictionScheduler, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(commandExecutor, name);
        this.publishCommand = commandExecutor.getConnectionManager().getSubscribeService().getPublishCommand();
        if (evictionScheduler != null) {
            evictionScheduler.scheduleSetCache(getRawName(), getExpiredChannelName(), publishCommand);
        }
        this.evictionScheduler = evictionScheduler;
        this.redisson = redisson;
    }

    public RedissonSetCache(Codec codec, EvictionScheduler evictionScheduler, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(codec, commandExecutor, name);
        this.publishCommand = commandExecutor.getConnectionManager().getSubscribeService().getPublishCommand();
        if (evictionScheduler != null) {
            evictionScheduler.scheduleSetCache(getRawName(), getExpiredChannelName(), publishCommand);
        }
        this.evictionScheduler = evictionScheduler;
        this.redisson = redisson;
    }

    String getExpiredChannelName() {
        return prefixName("redisson_set_cache_expired", getRawName());
    }

    
    /** 创建 MapReduce 任务入口。 */
    @Override
    public <KOut, VOut> RCollectionMapReduce<V, KOut, VOut> mapReduce() {
        return new RedissonCollectionMapReduce<>(this, redisson, commandExecutor);
    }

    /** 返回元素数量。 */
    @Override
    public int size() {
        return get(sizeAsync());
    }

    /** 异步返回元素数量。 */
    @Override
    public RFuture<Integer> sizeAsync() {
        return commandExecutor.evalReadAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                  "local values = redis.call('zrangebyscore', KEYS[1], ARGV[1], ARGV[2]);" +
                        "return #values;",
                Arrays.asList(getRawName()),
                System.currentTimeMillis(), 92233720368547758L);
    }

    /** 集合是否为空。 */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /** 是否包含指定元素。 */
    @Override
    public boolean contains(Object o) {
        return get(containsAsync(o));
    }

    /** 异步执行 contains。 */
    @Override
    public RFuture<Boolean> containsAsync(Object o) {
        String name = getRawName(o);
        return commandExecutor.evalReadAsync(name, codec, RedisCommands.EVAL_BOOLEAN,
                    "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); " + 
                     "if expireDateScore ~= false then " +
                         "if tonumber(expireDateScore) <= tonumber(ARGV[1]) then " +
                             "return 0;" + 
                         "end;" +
                         "return 1;" +
                     "end; " +
                     "return 0;",
               Arrays.<Object>asList(name),
                System.currentTimeMillis(), encode(o));
    }

    /** SSCAN 增量扫描迭代器。 */
    @Override
    public ScanResult<Object> scanIterator(String name, RedisClient client, String startPos, String pattern, int count) {
        RFuture<ScanResult<Object>> f = scanIteratorAsync(name, client, startPos, pattern, count);
        return get(f);
    }

    /** 异步 SSCAN 迭代器。 */
    @Override
    public RFuture<ScanResult<Object>> scanIteratorAsync(String name, RedisClient client, String startPos, String pattern, int count) {
        List<Object> params = new ArrayList<>();
        params.add(startPos);
        params.add(System.currentTimeMillis());
        if (pattern != null) {
            params.add(pattern);
        }
        params.add(count);
        
        return commandExecutor.evalReadAsync(client, name, codec, RedisCommands.EVAL_SCAN,
                  "local result = {}; "
                + "local res; "
                + "if (#ARGV == 4) then "
                  + " res = redis.call('zscan', KEYS[1], ARGV[1], 'match', ARGV[3], 'count', ARGV[4]); "
                + "else "
                  + " res = redis.call('zscan', KEYS[1], ARGV[1], 'count', ARGV[3]); "
                + "end;"
                + "for i, value in ipairs(res[2]) do "
                    + "if i % 2 == 0 then "
                        + "local expireDate = value; "
                        + "if tonumber(expireDate) > tonumber(ARGV[2]) then "
                            + "table.insert(result, res[2][i-1]); "
                        + "end; "
                    + "end;"
                + "end;"
                + "return {res[1], result};", Arrays.asList(name), params.toArray());
    }

    /** 返回成员迭代器。 */
    @Override
    public Iterator<V> iterator(int count) {
        return iterator(null, count);
    }
    
    /** 返回成员迭代器。 */
    @Override
    public Iterator<V> iterator(String pattern) {
        return iterator(pattern, 10);
    }
    
    /** 返回成员迭代器。 */
    @Override
    public Iterator<V> iterator(String pattern, int count) {
        return new RedissonBaseIterator<V>() {

            @Override
            protected ScanResult<Object> iterator(RedisClient client, String nextIterPos) {
                return scanIterator(getRawName(), client, nextIterPos, pattern, count);
            }

            @Override
            protected void remove(Object value) {
                RedissonSetCache.this.remove((V) value);
            }
            
        };
    }
    
    /** 返回成员迭代器。 */
    @Override
    public Iterator<V> iterator() {
        return iterator(null);
    }

    /** 一次性读取全部成员。 */
    @Override
    public Set<V> readAll() {
        return get(readAllAsync());
    }

    /** 异步读取全部成员。 */
    @Override
    public RFuture<Set<V>> readAllAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGEBYSCORE, getRawName(), System.currentTimeMillis(), 92233720368547758L);
    }

    /** SetCache toArray 操作。 */
    @Override
    public Object[] toArray() {
        Set<V> res = get(readAllAsync());
        return res.toArray();
    }

    /** SetCache toArray 操作。 */
    @Override
    public <T> T[] toArray(T[] a) {
        Set<V> res = get(readAllAsync());
        return res.toArray(a);
    }

    /** 添加元素/成员。 */
    @Override
    public boolean add(V e) {
        return get(addAsync(e));
    }

    /** 添加元素/成员。 */
    @Override
    public boolean add(V value, long ttl, TimeUnit unit) {
        return get(addAsync(value, ttl, unit));
    }

    /** 异步添加 ZSet 成员。 */
    @Override
    public RFuture<Boolean> addAsync(V value, long ttl, TimeUnit unit) {
        if (ttl < 0) {
            throw new IllegalArgumentException("TTL can't be negative");
        }
        if (ttl == 0) {
            return addAsync(value);
        }

        if (unit == null) {
            throw new NullPointerException("TimeUnit param can't be null");
        }

        ByteBuf objectState = encode(value);

        long timeoutDate = System.currentTimeMillis() + unit.toMillis(ttl);
        String name = getRawName(value);
        return commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[3]); " +
                "redis.call('zadd', KEYS[1], ARGV[2], ARGV[3]); " +
                "if expireDateScore ~= false and tonumber(expireDateScore) > tonumber(ARGV[1]) then " +
                    "return 0;" +
                "end; " +
                "return 1; ",
                Arrays.asList(name), System.currentTimeMillis(), timeoutDate, objectState);
    }

    /** SetCache tryAdd 操作。 */
    @Override
    public boolean tryAdd(V... values) {
        return get(tryAddAsync(values));
    }

    /** 异步执行 tryAdd。 */
    @Override
    public RFuture<Boolean> tryAddAsync(V... values) {
        return tryAddAsync(92233720368547758L - System.currentTimeMillis(), TimeUnit.MILLISECONDS, values);
    }

    /** SetCache tryAdd 操作。 */
    @Override
    public boolean tryAdd(long ttl, TimeUnit unit, V... values) {
        return get(tryAddAsync(ttl, unit, values));
    }

    /** 异步执行 tryAdd。 */
    @Override
    public RFuture<Boolean> tryAddAsync(long ttl, TimeUnit unit, V... values) {
        long timeoutDate = System.currentTimeMillis() + unit.toMillis(ttl);
        if (ttl == 0) {
            timeoutDate = 92233720368547758L - System.currentTimeMillis();
        }

        List<Object> params = new ArrayList<>();
        params.add(System.currentTimeMillis());
        params.add(timeoutDate);
        params.addAll(encode(Arrays.asList(values)));

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                  "for i, v in ipairs(ARGV) do " +
                            "local expireDateScore = redis.call('zscore', KEYS[1], v); " +
                            "if expireDateScore ~= false and tonumber(expireDateScore) > tonumber(ARGV[1]) then " +
                                "return 0; " +
                            "end; " +
                        "end; " +

                        "for i=3, #ARGV, 1 do " +
                            "redis.call('zadd', KEYS[1], ARGV[2], ARGV[i]); " +
                        "end; " +
                        "return 1; ",
                       Arrays.asList(getRawName()), params.toArray());
    }

    /** 异步添加 ZSet 成员。 */
    @Override
    public RFuture<Boolean> addAsync(V value) {
        return addAsync(value, 92233720368547758L - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /** 异步移除 ZSet 成员。 */
    @Override
    public RFuture<Boolean> removeAsync(Object o) {
        String name = getRawName(o);
        return commandExecutor.writeAsync(name, codec, RedisCommands.ZREM, name, encode(o));
    }

    /** 移除元素。 */
    @Override
    public boolean remove(Object value) {
        return get(removeAsync((V) value));
    }

    /** 是否包含全部给定元素。 */
    @Override
    public boolean containsAll(Collection<?> c) {
        return get(containsAllAsync(c));
    }

    /** 异步执行 containsAll。 */
    @Override
    public RFuture<Boolean> containsAllAsync(Collection<?> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(true);
        }
        
        List<Object> params = new ArrayList<Object>(c.size() + 1);
        params.add(System.currentTimeMillis());
        encode(params, c);
        
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                            "for j = 2, #ARGV, 1 do "
                            + "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[j]) "
                            + "if expireDateScore ~= false then "
                                + "if tonumber(expireDateScore) <= tonumber(ARGV[1]) then "
                                    + "return 0;"
                                + "end; "
                            + "else "
                                + "return 0;"
                            + "end; "
                        + "end; "
                       + "return 1; ",
                Collections.<Object>singletonList(getRawName()), params.toArray());
    }

    /** 批量添加元素。 */
    @Override
    public boolean addAll(Collection<? extends V> c) {
        return get(addAllAsync(c));
    }

    /** 异步执行 addAll。 */
    @Override
    public RFuture<Boolean> addAllAsync(Collection<? extends V> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(false);
        }

        long score = 92233720368547758L - System.currentTimeMillis();
        List<Object> params = new ArrayList<Object>(c.size()*2 + 1);
        params.add(getRawName());
        for (V value : c) {
            ByteBuf objectState = encode(value);
            params.add(score);
            params.add(objectState);
        }

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZADD_BOOL_RAW, params.toArray());
    }

    /** 仅保留与给定集合的交集。 */
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
        
        long score = 92233720368547758L - System.currentTimeMillis();
        List<Object> params = new ArrayList<>(c.size() * 2);
        for (Object object : c) {
            params.add(score);
            encode(params, object);
        }
        
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "redis.call('zadd', KEYS[2], unpack(ARGV)); "
                 + "local prevSize = redis.call('zcard', KEYS[1]); "
                 + "local size = redis.call('zinterstore', KEYS[1], #ARGV/2, KEYS[1], KEYS[2], 'aggregate', 'min');"
                 + "redis.call('del', KEYS[2]); "
                 + "return size ~= prevSize and 1 or 0; ",
             Arrays.<Object>asList(getRawName(), "redisson_temp__{" + getRawName() + "}"), params.toArray());
    }

    /** 异步执行 removeAll。 */
    @Override
    public RFuture<Boolean> removeAllAsync(Collection<?> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(false);
        }
        
        List<Object> params = new ArrayList<Object>(c.size()+1);
        params.add(getRawName());
        encode(params, c);

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZREM, params.toArray());
    }

    /** 批量移除元素。 */
    @Override
    public boolean removeAll(Collection<?> c) {
        return get(removeAllAsync(c));
    }

    /** 清空全部元素。 */
    @Override
    public void clear() {
        delete();
    }

    /** 获取可过期许可信号量。 */
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(V value) {
        String lockName = getLockByValue(value, "permitexpirablesemaphore");
        return new RedissonPermitExpirableSemaphore(commandExecutor, lockName);
    }

    /** 获取分布式信号量。 */
    @Override
    public RSemaphore getSemaphore(V value) {
        String lockName = getLockByValue(value, "semaphore");
        return new RedissonSemaphore(commandExecutor, lockName);
    }
    
    /** 获取 CountDownLatch。 */
    @Override
    public RCountDownLatch getCountDownLatch(V value) {
        String lockName = getLockByValue(value, "countdownlatch");
        return new RedissonCountDownLatch(commandExecutor, lockName);
    }
    
    /** 获取公平锁。 */
    @Override
    public RLock getFairLock(V value) {
        String lockName = getLockByValue(value, "fairlock");
        return new RedissonFairLock(commandExecutor, lockName);
    }
    
    /** 获取分布式锁。 */
    @Override
    public RLock getLock(V value) {
        String lockName = getLockByValue(value, "lock");
        return new RedissonLock(commandExecutor, lockName);
    }
    
    /** 获取读写锁。 */
    @Override
    public RReadWriteLock getReadWriteLock(V value) {
        String lockName = getLockByValue(value, "rw_lock");
        return new RedissonReadWriteLock(commandExecutor, lockName);
    }

    /** SetCache destroy 操作。 */
    @Override
    public void destroy() {
        if (evictionScheduler != null) {
            evictionScheduler.remove(getRawName());
        }

        String expiredChannelName = getExpiredChannelName();
        Collection<Integer> ids = getListenerIdsByName(expiredChannelName);
        if (!ids.isEmpty()) {
            RTopic topic = getTopic(expiredChannelName);
            for (Integer listenerId : new ArrayList<>(ids)) {
                removeListenerId(expiredChannelName, listenerId);
                topic.removeListener(listenerId);
            }
        }

        removeListeners();
    }

    /** SetCache stream 操作。 */
    @Override
    public Stream<V> stream(int count) {
        return toStream(iterator(count));
    }

    /** SetCache stream 操作。 */
    @Override
    public Stream<V> stream(String pattern, int count) {
        return toStream(iterator(pattern, count));
    }

    /** SetCache stream 操作。 */
    @Override
    public Stream<V> stream(String pattern) {
        return toStream(iterator(pattern));
    }

    /** addAllCounted：添加操作。 */
    @Override
    public int addAllCounted(Collection<? extends V> c) {
        return get(addAllCountedAsync(c));
    }

    /** removeAllCounted：移除操作。 */
    @Override
    public int removeAllCounted(Collection<? extends V> c) {
        return get(removeAllCountedAsync(c));
    }

    /** SetCache distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(String pattern) {
        String iteratorName = "__redisson_scored_sorted_set_cursor_{" + getRawName() + "}";
        return distributedIterator(iteratorName, pattern, 10);
    }

    /** SetCache distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(int count) {
        String iteratorName = "__redisson_scored_sorted_set_cursor_{" + getRawName() + "}";
        return distributedIterator(iteratorName, null, count);
    }

    /** SetCache distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(String iteratorName, String pattern, int count) {
        return new RedissonBaseIterator<V>() {

            @Override
            protected ScanResult<Object> iterator(RedisClient client, String nextIterPos) {
                return distributedScanIterator(iteratorName, pattern, count);
            }

            @Override
            protected void remove(Object value) {
                RedissonSetCache.this.remove(value);
            }
        };
    }

    /** SetCache distributedScanIterator 操作。 */
    private ScanResult<Object> distributedScanIterator(String iteratorName, String pattern, int count) {
        return get(distributedScanIteratorAsync(iteratorName, pattern, count));
    }

    /** 异步执行 distributedScanIterator。 */
    private RFuture<ScanResult<Object>> distributedScanIteratorAsync(String iteratorName, String pattern, int count) {
        List<Object> args = new ArrayList<>(2);
        args.add(System.currentTimeMillis());
        if (pattern != null) {
            args.add(pattern);
        }
        args.add(count);

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_SCAN,
                "local cursor = redis.call('get', KEYS[2]); "
                + "if cursor ~= false then "
                    + "cursor = tonumber(cursor); "
                + "else "
                    + "cursor = 0;"
                + "end;"
                + "if cursor == -1 then "
                    + "return {0, {}}; "
                + "end;"
                + "local result; "
                + "if (#ARGV == 3) then "
                    + "result = redis.call('zscan', KEYS[1], cursor, 'match', ARGV[2], 'count', ARGV[3]); "
                + "else "
                    + "result = redis.call('zscan', KEYS[1], cursor, 'count', ARGV[2]); "
                + "end;"
                + "local next_cursor = result[1]"
                + "if next_cursor ~= \"0\" then "
                    + "redis.call('setex', KEYS[2], 3600, next_cursor);"
                + "else "
                    + "redis.call('setex', KEYS[2], 3600, -1);"
                + "end; "
                + "local res = {};"
                + "for i, value in ipairs(result[2]) do "
                    + "if i % 2 == 0 then "
                        + "local expireDate = value; "
                        + "if tonumber(expireDate) > tonumber(ARGV[1]) then "
                            + "table.insert(res, result[2][i-1]); "
                        + "end; "
                    + "end; "
                + "end;"
                + "return {result[1], res};",
                Arrays.asList(getRawName(), iteratorName), args.toArray());
    }

    /** 随机移除并返回一个成员。 */
    @Override
    public Set<V> removeRandom(int amount) {
        throw new UnsupportedOperationException();
    }

    /** 随机移除并返回一个成员。 */
    @Override
    public V removeRandom() {
        throw new UnsupportedOperationException();
    }

    /** 随机返回一个成员（不移除）。 */
    @Override
    public V random() {
        return get(randomAsync());
    }

    /** 随机返回一个成员（不移除）。 */
    @Override
    public Set<V> random(int count) {
        return get(randomAsync(count));
    }

    /** 将成员移动到另一 Set。 */
    @Override
    public boolean move(String destination, V member) {
        throw new UnsupportedOperationException();
    }

    /** 计算并存储集合并集。 */
    @Override
    public int union(String... names) {
        return get(unionAsync(names));
    }

    /** SetCache readUnion 操作。 */
    @Override
    public Set<V> readUnion(String... names) {
        return get(readUnionAsync(names));
    }

    /** SetCache diff 操作。 */
    @Override
    public int diff(String... names) {
        return get(diffAsync(names));
    }

    /** SetCache readDiff 操作。 */
    @Override
    public Set<V> readDiff(String... names) {
        return get(readDiffAsync(names));
    }

    /** 计算并存储集合交集。 */
    @Override
    public int intersection(String... names) {
        return get(intersectionAsync(names));
    }

    /** SetCache readIntersection 操作。 */
    @Override
    public Set<V> readIntersection(String... names) {
        return get(readIntersectionAsync(names));
    }

    /** SetCache countIntersection 操作。 */
    @Override
    public Integer countIntersection(String... names) {
        return get(countIntersectionAsync(names));
    }

    /** SetCache countIntersection 操作。 */
    @Override
    public Integer countIntersection(int limit, String... names) {
        return get(countIntersectionAsync(limit, names));
    }

    /** SetCache countUnion 操作。 */
    @Override
    public Integer countUnion(String... names) {
        return get(countUnionAsync(names));
    }

    /** SetCache countUnion 操作。 */
    @Override
    public Integer countUnion(int limit, String... names) {
        return get(countUnionAsync(limit, names));
    }

    /** SetCache countUnionApprox 操作。 */
    @Override
    public Integer countUnionApprox(String... names) {
        return get(countUnionApproxAsync(names));
    }

    /** SetCache countUnionApprox 操作。 */
    @Override
    public Integer countUnionApprox(int limit, String... names) {
        return get(countUnionApproxAsync(limit, names));
    }

    /** SetCache countDiff 操作。 */
    @Override
    public Integer countDiff(String... names) {
        return get(countDiffAsync(names));
    }

    /** SetCache countDiff 操作。 */
    @Override
    public Integer countDiff(int limit, String... names) {
        return get(countDiffAsync(limit, names));
    }

    /** SetCache containsEach 操作。 */
    @Override
    public Set<V> containsEach(Collection<V> c) {
        throw new UnsupportedOperationException();
    }

    /** 异步随机移除成员。 */
    @Override
    public RFuture<Set<V>> removeRandomAsync(int amount) {
        throw new UnsupportedOperationException();
    }

    /** 异步随机移除成员。 */
    @Override
    public RFuture<V> removeRandomAsync() {
        throw new UnsupportedOperationException();
    }

    /** 异步随机返回成员。 */
    @Override
    public RFuture<V> randomAsync() {
        String tempName = prefixName("__redisson_cache_temp", getRawName());
        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_OBJECT,
                  "local values = redis.call('zrangebyscore', KEYS[1], ARGV[1], ARGV[2], 'WITHSCORES');" +
                        "for i = 1, #values, 2 do "
                          + "redis.call('zadd', KEYS[2], values[i], values[i+1]); " +
                        "end;" +
                        "local res = redis.call('zrandmember', KEYS[2]); " +
                        "redis.call('del', KEYS[2]); " +
                        "return res;",
                Arrays.asList(getRawName(), tempName),
                System.currentTimeMillis(), 92233720368547758L);
    }

    /** 异步随机返回成员。 */
    @Override
    public RFuture<Set<V>> randomAsync(int count) {
        String tempName = prefixName("__redisson_cache_temp", getRawName());
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_SET,
                  "local values = redis.call('zrangebyscore', KEYS[1], ARGV[1], ARGV[2], 'WITHSCORES');" +
                        "for i = 1, #values, 2 do "
                            + "redis.call('zadd', KEYS[2], values[i], values[i+1]); " +
                        "end;" +
                        "local res = redis.call('zrandmember', KEYS[2], ARGV[3]); " +
                        "redis.call('del', KEYS[2]); " +
                        "return res;",
                Arrays.asList(getRawName(), tempName),
                System.currentTimeMillis(), 92233720368547758L, count);
    }

    /** 异步执行 move。 */
    @Override
    public RFuture<Boolean> moveAsync(String destination, V member) {
        throw new UnsupportedOperationException();
    }

    /** 异步计算 ZSet 并集。 */
    @Override
    public RFuture<Integer> unionAsync(String... names) {
        List<Object> keys = new ArrayList<>();
        keys.add(getRawName());
        keys.addAll(map(names));
        for (Object key : names) {
            String tempName = prefixName("__redisson_cache_temp", key.toString());
            keys.add(tempName);
        }

        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                   "local args = {KEYS[1], (#KEYS-1)/2};" +
                         "for i = 2, (#KEYS-1)/2 + 1, 1 do " +
                             "local values = redis.call('zrangebyscore', KEYS[i], ARGV[1], ARGV[2], 'WITHSCORES');" +
                             "local k = (#KEYS-1)/2 + i; " +
                             "table.insert(args, KEYS[k]); " +
                             "for j = 1, #values, 2 do " +
                                 "redis.call('zadd', KEYS[k], values[j+1], values[j]); " +
                             "end;" +
                        "end; " +
                        "table.insert(args, 'AGGREGATE'); " +
                        "table.insert(args, 'SUM'); " +
                        "local res = redis.call('zunionstore', unpack(args));" +
                        "redis.call('del', unpack(KEYS, (#KEYS-1)/2+2, #KEYS)); " +
                        "return res;",
                        keys,
                System.currentTimeMillis(), 92233720368547758L, names.length+1);
    }

    /** 异步执行 readUnion。 */
    @Override
    public RFuture<Set<V>> readUnionAsync(String... names) {
        List<Object> keys = new ArrayList<>();
        keys.add(getRawName());
        keys.addAll(map(names));

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_SET,
                     "local args = {} " +
                     "table.insert(args, #KEYS) " +
                     "for _, key_name in ipairs(KEYS) do " +
                          "table.insert(args, key_name) " +
                     "end " +
                     "table.insert(args, 'WITHSCORES')" +

                     "local values = redis.call('zunion', unpack(args)) " +
                     "local res = {} " +
                     "for j = 1, #values, 2 do " +
                         "if tonumber(values[j+1]) > tonumber(ARGV[1]) then " +
                             "table.insert(res, values[j]);" +
                         "end " +
                     "end;" +
                     "return res;",
                keys,
                System.currentTimeMillis(), 92233720368547758L, names.length+1);
    }

    /** 异步执行 diff。 */
    @Override
    public RFuture<Integer> diffAsync(String... names) {
        List<Object> keys = new ArrayList<>();
        keys.add(getRawName());
        keys.addAll(map(names));
        for (Object key : names) {
            String tempName = prefixName("__redisson_cache_temp", key.toString());
            keys.add(tempName);
        }

        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                   "local args = {KEYS[1], (#KEYS-1)/2};" +
                         "for i = 2, (#KEYS-1)/2 + 1, 1 do " +
                             "local values = redis.call('zrangebyscore', KEYS[i], ARGV[1], ARGV[2], 'WITHSCORES');" +
                             "local k = (#KEYS-1)/2 + i; " +
                             "table.insert(args, KEYS[k]); " +
                             "for j = 1, #values, 2 do " +
                                 "redis.call('zadd', KEYS[k], values[j+1], values[j]); " +
                             "end;" +
                        "end; " +
                        "local res = redis.call('zdiffstore', unpack(args));" +
                        "redis.call('del', unpack(KEYS, (#KEYS-1)/2+2, #KEYS)); " +
                        "return res;",
                        keys,
                System.currentTimeMillis(), 92233720368547758L, names.length+1);
    }

    /** 异步执行 readDiff。 */
    @Override
    public RFuture<Set<V>> readDiffAsync(String... names) {
        List<Object> keys = new ArrayList<>();
        keys.add(getRawName());
        keys.addAll(map(names));
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_SET,
               "local args = {} " +
                     "table.insert(args, #KEYS) " +
                     "for _, key_name in ipairs(KEYS) do " +
                          "table.insert(args, key_name) " +
                     "end " +
                     "table.insert(args, 'WITHSCORES')" +

                     "local values = redis.call('zdiff', unpack(args)) " +
                     "local res = {} " +
                     "for j = 1, #values, 2 do " +
                         "if tonumber(values[j+1]) > tonumber(ARGV[1]) then " +
                             "table.insert(res, values[j]);" +
                         "end " +
                     "end;" +
                     "return res;",
                        keys,
                System.currentTimeMillis());
    }

    /** 异步计算 ZSet 交集。 */
    @Override
    public RFuture<Integer> intersectionAsync(String... names) {
        List<Object> keys = new ArrayList<>();
        keys.add(getRawName());
        keys.addAll(map(names));
        for (Object key : names) {
            String tempName = prefixName("__redisson_cache_temp", key.toString());
            keys.add(tempName);
        }

        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                   "local args = {KEYS[1], (#KEYS-1)/2};" +
                         "for i = 2, (#KEYS-1)/2 + 1, 1 do " +
                             "local values = redis.call('zrangebyscore', KEYS[i], ARGV[1], ARGV[2], 'WITHSCORES');" +
                             "local k = (#KEYS-1)/2 + i; " +
                             "table.insert(args, KEYS[k]); " +
                             "for j = 1, #values, 2 do " +
                                 "redis.call('zadd', KEYS[k], values[j+1], values[j]); " +
                             "end;" +
                        "end; " +
                        "table.insert(args, 'AGGREGATE'); " +
                        "table.insert(args, 'SUM'); " +
                        "local res = redis.call('zinterstore', unpack(args));" +
                        "redis.call('del', unpack(KEYS, (#KEYS-1)/2+2, #KEYS)); " +
                        "return res;",
                        keys,
                System.currentTimeMillis(), 92233720368547758L, names.length+1);
    }

    /** 异步执行 readIntersection。 */
    @Override
    public RFuture<Set<V>> readIntersectionAsync(String... names) {
        List<Object> keys = new ArrayList<>();
        keys.add(getRawName());
        keys.addAll(map(names));

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_SET,
               "local args = {} " +
                     "table.insert(args, #KEYS) " +
                     "for _, key_name in ipairs(KEYS) do " +
                          "table.insert(args, key_name) " +
                     "end " +
                     "table.insert(args, 'WITHSCORES')" +

                     "local values = redis.call('zinter', unpack(args)) " +
                     "local res = {} " +
                     "for j = 1, #values, 2 do " +
                         "if tonumber(values[j+1]) > tonumber(ARGV[1]) then " +
                             "table.insert(res, values[j]);" +
                         "end " +
                     "end;" +
                     "return res;",
                         keys,
                System.currentTimeMillis());
    }

    /** 异步执行 countIntersection。 */
    @Override
    public RFuture<Integer> countIntersectionAsync(String... names) {
        return countIntersectionAsync(0, names);
    }

    /** 异步执行 countIntersection。 */
    @Override
    public RFuture<Integer> countIntersectionAsync(int limit, String... names) {
        List<Object> keys = new ArrayList<>();
        keys.add(getRawName());
        keys.addAll(map(names));
        for (Object key : new ArrayList<>(keys)) {
            String tempName = prefixName("__redisson_cache_temp", key.toString());
            keys.add(tempName);
        }

        return commandExecutor.evalWriteAsync(getRawName(), IntegerCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                   "local args = {ARGV[3]};" +
                         "for i = 1, ARGV[3], 1 do " +
                             "local values = redis.call('zrangebyscore', KEYS[i], ARGV[1], ARGV[2], 'WITHSCORES');" +
                             "local k = tonumber(ARGV[3]) + i; " +
                             "table.insert(args, KEYS[k]); " +
                             "for j = 1, #values, 2 do " +
                                 "redis.call('zadd', KEYS[k], values[j+1], values[j]); " +
                             "end;" +
                        "end; " +
                        "table.insert(args, 'LIMIT'); " +
                        "table.insert(args, ARGV[4]); " +
                        "local res = redis.call('zintercard', unpack(args));" +
                        "redis.call('del', unpack(KEYS, ARGV[3]+1, #KEYS)); " +
                        "return res;",
                         keys,
                System.currentTimeMillis(), 92233720368547758L, names.length+1, limit);
    }

    /** 异步执行 countUnion。 */
    @Override
    public RFuture<Integer> countUnionAsync(String... names) {
        return countUnionAsync(0, names);
    }

    /** 异步执行 countUnion。 */
    @Override
    public RFuture<Integer> countUnionAsync(int limit, String... names) {
        List<Object> keys = new ArrayList<>();
        keys.add(getRawName());
        keys.addAll(map(names));

        return commandExecutor.evalReadAsync(getRawName(), IntegerCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                   "local limit = tonumber(ARGV[3]);" +
                         "local found = {};" +
                         "local count = 0;" +
                         "for i = 1, #KEYS, 1 do " +
                             "local values = redis.call('zrangebyscore', KEYS[i], ARGV[1], ARGV[2]);" +
                             "for j = 1, #values, 1 do " +
                                 "if found[values[j]] == nil then " +
                                     "found[values[j]] = true;" +
                                     "count = count + 1;" +
                                     "if limit > 0 and count >= limit then " +
                                         "return count;" +
                                     "end;" +
                                 "end;" +
                             "end;" +
                        "end;" +
                        "return count;",
                        keys,
                System.currentTimeMillis(), 92233720368547758L, limit);
    }

    /*
     * RSetCache is backed by a sorted set, so the union cardinality is computed
     * exactly rather than estimated. An exact value is a valid result for an
     * approximate count, so both variants share the same implementation.
     */
    /** 异步执行 countUnionApprox。 */
    @Override
    public RFuture<Integer> countUnionApproxAsync(String... names) {
        return countUnionAsync(0, names);
    }

    /** 异步执行 countUnionApprox。 */
    @Override
    public RFuture<Integer> countUnionApproxAsync(int limit, String... names) {
        return countUnionAsync(limit, names);
    }

    /** 异步执行 countDiff。 */
    @Override
    public RFuture<Integer> countDiffAsync(String... names) {
        return countDiffAsync(0, names);
    }

    /** 异步执行 countDiff。 */
    @Override
    public RFuture<Integer> countDiffAsync(int limit, String... names) {
        List<Object> keys = new ArrayList<>();
        keys.add(getRawName());
        keys.addAll(map(names));

        return commandExecutor.evalReadAsync(getRawName(), IntegerCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                   "local limit = tonumber(ARGV[3]);" +
                         "local excluded = {};" +
                         "for i = 2, #KEYS, 1 do " +
                             "local values = redis.call('zrangebyscore', KEYS[i], ARGV[1], ARGV[2]);" +
                             "for j = 1, #values, 1 do " +
                                 "excluded[values[j]] = true;" +
                             "end;" +
                        "end;" +
                        "local count = 0;" +
                        "local values = redis.call('zrangebyscore', KEYS[1], ARGV[1], ARGV[2]);" +
                        "for j = 1, #values, 1 do " +
                            "if excluded[values[j]] == nil then " +
                                "count = count + 1;" +
                                "if limit > 0 and count >= limit then " +
                                    "return count;" +
                                "end;" +
                            "end;" +
                        "end;" +
                        "return count;",
                        keys,
                System.currentTimeMillis(), 92233720368547758L, limit);
    }

    /** 异步执行 addAllCounted。 */
    @Override
    public RFuture<Integer> addAllCountedAsync(Collection<? extends V> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(0);
        }

        List<Object> args = new ArrayList<>(c.size() + 1);
        args.add(getRawName());
        for (V v : c) {
            args.add(92233720368547758L);
            try {
                args.add(v);
            } catch (Exception e) {
                args.forEach(vv -> {
                    ReferenceCountUtil.safeRelease(vv);
                });
                throw e;
            }
        }

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZADD_INT, args.toArray());
    }

    /** 异步执行 removeAllCounted。 */
    @Override
    public RFuture<Integer> removeAllCountedAsync(Collection<? extends V> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(0);
        }

        List<Object> args = new ArrayList<>(c.size() + 1);
        args.add(getRawName());
        encode(args, c);

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZREM_INT, args.toArray());
    }

    /** 异步执行 containsEach。 */
    @Override
    public RFuture<Set<V>> containsEachAsync(Collection<V> c) {
        throw new UnsupportedOperationException();
    }

    /** SetCache readSort 操作。 */
    @Override
    public Set<V> readSort(SortOrder order) {
        return get(readSortAsync(order));
    }

    /** SetCache readSort 操作。 */
    @Override
    public Set<V> readSort(SortOrder order, int offset, int count) {
        return get(readSortAsync(order, offset, count));
    }

    /** SetCache readSort 操作。 */
    @Override
    public Set<V> readSort(String byPattern, SortOrder order) {
        return get(readSortAsync(byPattern, order));
    }

    /** SetCache readSort 操作。 */
    @Override
    public Set<V> readSort(String byPattern, SortOrder order, int offset, int count) {
        return get(readSortAsync(byPattern, order, offset, count));
    }

    /** SetCache readSort 操作。 */
    @Override
    public <T> Collection<T> readSort(String byPattern, List<String> getPatterns, SortOrder order) {
        return get(readSortAsync(byPattern, getPatterns, order));
    }

    /** SetCache readSort 操作。 */
    @Override
    public <T> Collection<T> readSort(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return get(readSortAsync(byPattern, getPatterns, order, offset, count));
    }

    /** SetCache readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(SortOrder order) {
        return get(readSortAlphaAsync(order));
    }

    /** SetCache readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(SortOrder order, int offset, int count) {
        return get(readSortAlphaAsync(order, offset, count));
    }

    /** SetCache readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(String byPattern, SortOrder order) {
        return get(readSortAlphaAsync(byPattern, order));
    }

    /** SetCache readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(String byPattern, SortOrder order, int offset, int count) {
        return get(readSortAlphaAsync(byPattern, order, offset, count));
    }

    /** SetCache readSortAlpha 操作。 */
    @Override
    public <T> Collection<T> readSortAlpha(String byPattern, List<String> getPatterns, SortOrder order) {
        return get(readSortAlphaAsync(byPattern, getPatterns, order));
    }

    /** SetCache readSortAlpha 操作。 */
    @Override
    public <T> Collection<T> readSortAlpha(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return get(readSortAlphaAsync(byPattern, getPatterns, order, offset, count));
    }

    /** SetCache sortTo 操作。 */
    @Override
    public int sortTo(String destName, SortOrder order) {
        return get(sortToAsync(destName, order));
    }

    /** SetCache sortTo 操作。 */
    @Override
    public int sortTo(String destName, SortOrder order, int offset, int count) {
        return get(sortToAsync(destName, order, offset, count));
    }

    /** SetCache sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, SortOrder order) {
        return get(sortToAsync(destName, byPattern, order));
    }

    /** SetCache sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, SortOrder order, int offset, int count) {
        return get(sortToAsync(destName, byPattern, order, offset, count));
    }

    /** SetCache sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, List<String> getPatterns, SortOrder order) {
        return get(sortToAsync(destName, byPattern, getPatterns, order));
    }

    /** SetCache sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return get(sortToAsync(destName, byPattern, getPatterns, order, offset, count));
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(SortOrder order) {
        return readSortAsync(null, null, order, -1, -1, false);
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(SortOrder order, int offset, int count) {
        return readSortAsync(null, null, order, offset, count, false);
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(String byPattern, SortOrder order) {
        return readSortAsync(byPattern, null, order, -1, -1, false);
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(String byPattern, SortOrder order, int offset, int count) {
        return readSortAsync(byPattern, null, order, offset, count, false);
    }

    /** 异步执行 readSort。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order) {
        return readSortAsync(byPattern, getPatterns, order, -1, -1);
    }

    /** 异步执行 readSort。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return readSortAsync(byPattern, getPatterns, order, offset, count, false);
    }

    /** 异步执行 readSort。 */
    private <T> RFuture<T> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count, boolean alpha) {
        throw new UnsupportedOperationException();
//        List<Object> params = new ArrayList<>();
//        params.add(System.currentTimeMillis());
//        params.add(92233720368547758L);
//        if (byPattern != null) {
//            params.add("BY");
//            params.add(byPattern);
//        }
//        if (offset != -1 && count != -1) {
//            params.add("LIMIT");
//        }
//        if (offset != -1) {
//            params.add(offset);
//        }
//        if (count != -1) {
//            params.add(count);
//        }
//        if (getPatterns != null) {
//            for (String pattern : getPatterns) {
//                params.add("GET");
//                params.add(pattern);
//            }
//        }
//        if (alpha) {
//            params.add("ALPHA");
//        }
//        if (order != null) {
//            params.add(order);
//        }
//
//        String tempName = prefixName("__redisson_cache_temp", getRawName());
//        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_SET,
//                "local values = redis.call('zrangebyscore', KEYS[1], ARGV[1], ARGV[2], 'WITHSCORES');" +
//                        "for i = 1, #values, 2 do "
//                        + "redis.call('zadd', KEYS[2], values[i], values[i+1]); " +
//                        "end;" +
//                        "local res = redis.call('sort', KEYS[2], unpack(ARGV, 3, #ARGV)); " +
//                        "redis.call('del', KEYS[2]); " +
//                        "return res;",
//                Arrays.asList(getRawName(), tempName), params.toArray());
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(SortOrder order) {
        return readSortAsync(null, null, order, -1, -1, true);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(SortOrder order, int offset, int count) {
        return readSortAsync(null, null, order, offset, count, true);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(String byPattern, SortOrder order) {
        return readSortAsync(byPattern, null, order, -1, -1, true);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(String byPattern, SortOrder order, int offset, int count) {
        return readSortAsync(byPattern, null, order, offset, count, true);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order) {
        return readSortAsync(byPattern, getPatterns, order, -1, -1, true);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return readSortAsync(byPattern, getPatterns, order, offset, count, true);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, SortOrder order) {
        return sortToAsync(destName, null, null, order, -1, -1);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, SortOrder order, int offset, int count) {
        return sortToAsync(destName, null, null, order, offset, count);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order) {
        return sortToAsync(destName, byPattern, null, order, -1, -1);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order, int offset, int count) {
        return sortToAsync(destName, byPattern, null, order, offset, count);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order) {
        return sortToAsync(destName, byPattern, getPatterns, order, -1, -1);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        throw new UnsupportedOperationException();
//        List<Object> params = new ArrayList<>();
//        params.add(System.currentTimeMillis());
//        params.add(92233720368547758L);
//        String tempName = prefixName("__redisson_cache_temp", getRawName());
//        params.add(tempName);
//        if (byPattern != null) {
//            params.add("BY");
//            params.add(byPattern);
//        }
//        if (offset != -1 && count != -1) {
//            params.add("LIMIT");
//        }
//        if (offset != -1) {
//            params.add(offset);
//        }
//        if (count != -1) {
//            params.add(count);
//        }
//        if (getPatterns != null) {
//            for (String pattern : getPatterns) {
//                params.add("GET");
//                params.add(pattern);
//            }
//        }
//        params.add(order);
//        params.add("STORE");
//        params.add(destName);
//
//        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
//                  "local values = redis.call('zrangebyscore', KEYS[1], ARGV[1], ARGV[2], 'WITHSCORES');" +
//                        "for i = 1, #values, 2 do "
//                        + "redis.call('zadd', KEYS[2], values[i], values[i+1]); " +
//                        "end;" +
//                        "local res = redis.call('sort', unpack(ARGV, 3, #ARGV)); " +
//                        "redis.call('del', KEYS[2]); " +
//                        "return res;",
//                Arrays.asList(getRawName(), tempName), params.toArray());
    }

    /** addIfAbsent：添加操作。 */
    @Override
    public boolean addIfAbsent(Duration ttl, V object) {
        return get(addIfAbsentAsync(ttl, object));
    }

    /** addIfExists：添加操作。 */
    @Override
    public boolean addIfExists(Duration ttl, V object) {
        return get(addIfExistsAsync(ttl, object));
    }

    /** addIfLess：添加操作。 */
    @Override
    public boolean addIfLess(Duration ttl, V object) {
        return get(addIfLessAsync(ttl, object));
    }

    /** addIfGreater：添加操作。 */
    @Override
    public boolean addIfGreater(Duration ttl, V object) {
        return get(addIfGreaterAsync(ttl, object));
    }

    /** 异步执行 addIfAbsent。 */
    @Override
    public RFuture<Boolean> addIfAbsentAsync(Duration ttl, V object) {
        long timeoutDate = System.currentTimeMillis() + ttl.toMillis();
        if (ttl.isZero()) {
            timeoutDate = 92233720368547758L - System.currentTimeMillis();
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[3]); " +
                        "if expireDateScore ~= false and tonumber(expireDateScore) > tonumber(ARGV[1]) then " +
                            "return 0; " +
                        "end; " +

                        "redis.call('zadd', KEYS[1], ARGV[2], ARGV[3]); " +
                        "return 1; ",
                Arrays.asList(getRawName()),
                System.currentTimeMillis(), timeoutDate, encode(object));
    }

    /** 异步执行 addIfExists。 */
    @Override
    public RFuture<Boolean> addIfExistsAsync(Duration ttl, V object) {
        long timeoutDate = System.currentTimeMillis() + ttl.toMillis();
        if (ttl.isZero()) {
            timeoutDate = 92233720368547758L - System.currentTimeMillis();
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[3]); " +
                      "if expireDateScore ~= false then " +
                        "if tonumber(expireDateScore) < tonumber(ARGV[1]) then " +
                            "return 0; " +
                        "end; " +
                        "redis.call('zadd', KEYS[1], ARGV[2], ARGV[3]); " +
                        "return 1; " +
                      "end; " +

                      "return 0; ",
                Arrays.asList(getRawName()),
                System.currentTimeMillis(), timeoutDate, encode(object));
    }

    /** 异步执行 addIfLess。 */
    @Override
    public RFuture<Boolean> addIfLessAsync(Duration ttl, V object) {
        long timeoutDate = System.currentTimeMillis() + ttl.toMillis();
        if (ttl.isZero()) {
            timeoutDate = 92233720368547758L - System.currentTimeMillis();
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[3]); " +
                      "if expireDateScore ~= false then " +
                        "if tonumber(expireDateScore) < tonumber(ARGV[1]) or tonumber(ARGV[2]) >= tonumber(expireDateScore) then " +
                            "return 0; " +
                        "end; " +
                        "redis.call('zadd', KEYS[1], ARGV[2], ARGV[3]); " +
                        "return 1; " +
                      "end; " +

                      "return 0; ",
                Arrays.asList(getRawName()),
                System.currentTimeMillis(), timeoutDate, encode(object));
    }

    /** 异步执行 addIfGreater。 */
    @Override
    public RFuture<Boolean> addIfGreaterAsync(Duration ttl, V object) {
        long timeoutDate = System.currentTimeMillis() + ttl.toMillis();
        if (ttl.isZero()) {
            timeoutDate = 92233720368547758L - System.currentTimeMillis();
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[3]); " +
                      "if expireDateScore ~= false then " +
                        "if tonumber(expireDateScore) < tonumber(ARGV[1]) or tonumber(ARGV[2]) <= tonumber(expireDateScore) then " +
                            "return 0; " +
                        "end; " +
                        "redis.call('zadd', KEYS[1], ARGV[2], ARGV[3]); " +
                        "return 1; " +
                      "end; " +

                      "return 0; ",
                Arrays.asList(getRawName()),
                System.currentTimeMillis(), timeoutDate, encode(object));
    }

    /** addAllIfAbsent：添加操作。 */
    @Override
    public int addAllIfAbsent(Map<V, Duration> objects) {
        return get(addAllIfAbsentAsync(objects));
    }

    /** addIfAbsent：添加操作。 */
    @Override
    public boolean addIfAbsent(Map<V, Duration> objects) {
        return get(addIfAbsentAsync(objects));
    }

    /** addAllIfExist：添加操作。 */
    @Override
    public int addAllIfExist(Map<V, Duration> objects) {
        return get(addAllIfExistAsync(objects));
    }

    /** addAllIfGreater：添加操作。 */
    @Override
    public int addAllIfGreater(Map<V, Duration> objects) {
        return get(addAllIfGreaterAsync(objects));
    }

    /** addAllIfLess：添加操作。 */
    @Override
    public int addAllIfLess(Map<V, Duration> objects) {
        return get(addAllIfLessAsync(objects));
    }

    /** 异步执行 addAllIfAbsent。 */
    @Override
    public RFuture<Integer> addAllIfAbsentAsync(Map<V, Duration> objects) {
        List<Object> params = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        params.add(currentTime);
        for (Map.Entry<V, Duration> entry : objects.entrySet()) {
            long timeoutDate = currentTime + entry.getValue().toMillis();
            if (entry.getValue().isZero()) {
                timeoutDate = 92233720368547758L - currentTime;
            }
            params.add(timeoutDate);
            encode(params, entry.getKey());
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_INTEGER,
                  "local result = 0; " +
                        "for i=2, #ARGV, 2 do " +
                            "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[i+1]); " +
                            "if expireDateScore == false or tonumber(expireDateScore) <= tonumber(ARGV[1]) then " +
                                "result = result + 1; " +
                                "redis.call('zadd', KEYS[1], ARGV[i], ARGV[i+1]); " +
                            "end; " +
                        "end; " +
                        "return result; ",
                Arrays.asList(getRawName()), params.toArray());
    }
    /** 异步执行 addIfAbsent。 */
    @Override
    public RFuture<Boolean> addIfAbsentAsync(Map<V, Duration> objects) {
        List<Object> params = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        params.add(currentTime);
        for (Map.Entry<V, Duration> entry : objects.entrySet()) {
            long timeoutDate = currentTime + entry.getValue().toMillis();
            if (entry.getValue().isZero()) {
                timeoutDate = 92233720368547758L - currentTime;
            }
            params.add(timeoutDate);
            encode(params, entry.getKey());
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
            "for i=2, #ARGV, 2 do " +
                    "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[i+1]); " +
                    "if expireDateScore ~= false and tonumber(expireDateScore) > tonumber(ARGV[1]) then " +
                        "return 0; " +
                    "end; " +
                 "end; " +
                 "for i=2, #ARGV, 2 do " +
                    "redis.call('zadd', KEYS[1], ARGV[i], ARGV[i+1]); " +
                 "end; " +
                 "return 1; ",
                Collections.singletonList(getRawName()), params.toArray());
    }
    /** 异步执行 addAllIfExist。 */
    @Override
    public RFuture<Integer> addAllIfExistAsync(Map<V, Duration> objects) {
        List<Object> params = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        params.add(currentTime);
        for (Map.Entry<V, Duration> entry : objects.entrySet()) {
            long timeoutDate = currentTime + entry.getValue().toMillis();
            if (entry.getValue().isZero()) {
                timeoutDate = 92233720368547758L - currentTime;
            }
            params.add(timeoutDate);
            encode(params, entry.getKey());
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_INTEGER,
                  "local result = 0; " +
                        "for i=2, #ARGV, 2 do " +
                            "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[i+1]); " +
                            "if expireDateScore ~= false and tonumber(expireDateScore) > tonumber(ARGV[1]) then " +
                                "result = result + 1; " +
                                "redis.call('zadd', KEYS[1], ARGV[i], ARGV[i+1]); " +
                            "end; " +
                        "end; " +
                        "return result; ",
                Arrays.asList(getRawName()), params.toArray());
    }

    /** 异步执行 addAllIfGreater。 */
    @Override
    public RFuture<Integer> addAllIfGreaterAsync(Map<V, Duration> objects) {
        List<Object> params = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        params.add(currentTime);
        for (Map.Entry<V, Duration> entry : objects.entrySet()) {
            long timeoutDate = currentTime + entry.getValue().toMillis();
            if (entry.getValue().isZero()) {
                timeoutDate = 92233720368547758L - currentTime;
            }
            params.add(timeoutDate);
            encode(params, entry.getKey());
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_INTEGER,
                  "local result = 0; " +
                        "for i=2, #ARGV, 2 do " +
                            "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[i+1]); " +
                            "if expireDateScore ~= false and tonumber(expireDateScore) > tonumber(ARGV[1]) and tonumber(ARGV[i]) > tonumber(expireDateScore) then " +
                                "result = result + 1; " +
                                "redis.call('zadd', KEYS[1], ARGV[i], ARGV[i+1]); " +
                            "end; " +
                        "end; " +
                        "return result; ",
                Arrays.asList(getRawName()), params.toArray());
    }

    /** 异步执行 addAllIfLess。 */
    @Override
    public RFuture<Integer> addAllIfLessAsync(Map<V, Duration> objects) {
        List<Object> params = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        params.add(currentTime);
        for (Map.Entry<V, Duration> entry : objects.entrySet()) {
            long timeoutDate = currentTime + entry.getValue().toMillis();
            if (entry.getValue().isZero()) {
                timeoutDate = 92233720368547758L - currentTime;
            }
            params.add(timeoutDate);
            encode(params, entry.getKey());
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_INTEGER,
                  "local result = 0; " +
                        "for i=2, #ARGV, 2 do " +
                            "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[i+1]); " +
                            "if expireDateScore ~= false and tonumber(expireDateScore) > tonumber(ARGV[1]) and tonumber(ARGV[i]) < tonumber(expireDateScore) then " +
                                "result = result + 1; " +
                                "redis.call('zadd', KEYS[1], ARGV[i], ARGV[i+1]); " +
                            "end; " +
                        "end; " +
                        "return result; ",
                Arrays.asList(getRawName()), params.toArray());
    }

    /** 批量添加元素。 */
    @Override
    public int addAll(Map<V, Duration> objects) {
        return get(addAllAsync(objects));
    }

    /** 异步执行 addAll。 */
    @Override
    public RFuture<Integer> addAllAsync(Map<V, Duration> objects) {
        List<Object> params = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        params.add(currentTime);
        for (Map.Entry<V, Duration> entry : objects.entrySet()) {
            long timeoutDate = currentTime + entry.getValue().toMillis();
            if (entry.getValue().isZero()) {
                timeoutDate = 92233720368547758L - currentTime;
            }
            params.add(timeoutDate);
            encode(params, entry.getKey());
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_INTEGER,
                  "local result = 0; " +
                        "for i=2, #ARGV, 2 do " +
                            "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[i+1]); " +
                            "if not (expireDateScore ~= false and tonumber(expireDateScore) > tonumber(ARGV[1])) then " +
                                "result = result + 1; " +
                            "end; " +
                            "redis.call('zadd', KEYS[1], ARGV[i], ARGV[i+1]); " +
                        "end; " +
                        "return result; ",
                Arrays.asList(getRawName()), params.toArray());
    }

    /** 注册对象变更监听器。 */
    @Override
    public int addListener(ObjectListener listener) {
        if (listener instanceof SetAddListener) {
            return addListener("__keyevent@*:zadd", (SetAddListener) listener, SetAddListener::onAdd);
        }
        if (listener instanceof SetRemoveListener) {
            return addListener("__keyevent@*:zrem", (SetRemoveListener) listener, SetRemoveListener::onRemove);
        }
        if (listener instanceof TrackingListener) {
            return addTrackingListener((TrackingListener) listener);
        }
        if (listener instanceof SetExpiredListener) {
            return get(addExpiredListenerAsync((SetExpiredListener<V>) listener));
        }

        return super.addListener(listener);
    }

    /** 异步执行 addListener。 */
    @Override
    public RFuture<Integer> addListenerAsync(ObjectListener listener) {
        if (listener instanceof SetAddListener) {
            return addListenerAsync("__keyevent@*:zadd", (SetAddListener) listener, SetAddListener::onAdd);
        }
        if (listener instanceof SetRemoveListener) {
            return addListenerAsync("__keyevent@*:zrem", (SetRemoveListener) listener, SetRemoveListener::onRemove);
        }
        if (listener instanceof TrackingListener) {
            return addTrackingListenerAsync((TrackingListener) listener);
        }
        if (listener instanceof SetExpiredListener) {
            return addExpiredListenerAsync((SetExpiredListener<V>) listener);
        }

        return super.addListenerAsync(listener);
    }

    private volatile BaseEventCodec.OSType osType;
    private volatile Codec topicCodec;

    /** 获取 Topic。 */
    protected RTopic getTopic(String name) {
        if (getSubscribeService().isShardingSupported()) {
            return RedissonShardedTopic.createRaw(topicCodec, commandExecutor, name);
        }
        return RedissonTopic.createRaw(topicCodec, commandExecutor, name);
    }

    /** 异步执行 addExpiredListener。 */
    private RFuture<Integer> addExpiredListenerAsync(SetExpiredListener<V> listener) {
        CompletionStage<Void> osTypeFuture = CompletableFuture.completedFuture(null);
        if (osType == null) {
            RFuture<Map<String, String>> serverFuture = commandExecutor.readAsync((String) null, StringCodec.INSTANCE, RedisCommands.INFO_SERVER);
            osTypeFuture = serverFuture.thenAccept(res -> {
                String os = res.get("os");
                if (os == null || os.contains("Windows")) {
                    osType = BaseEventCodec.OSType.WINDOWS;
                } else if (os.contains("NONSTOP")) {
                    osType = BaseEventCodec.OSType.HPNONSTOP;
                }
                topicCodec = new SetCacheEventCodec(codec, osType);
            });
        }

        CompletionStage<Integer> f = osTypeFuture.thenCompose(osType -> {
            RTopic topic = getTopic(getExpiredChannelName());
            return topic.addListenerAsync(List.class, (MessageListener<List<Object>>) (channel, msg) -> {
                listener.onExpired((V) msg.get(0));
            });
        });
        f = f.thenApply(id -> {
            addListenerId(getExpiredChannelName(), id);
            return id;
        });
        return new CompletableFutureWrapper<>(f);
    }

    /** 返回异步成员迭代器。 */
    @Override
    public AsyncIterator<V> iteratorAsync() {
        return iteratorAsync(10);
    }

    /** 返回异步成员迭代器。 */
    @Override
    public AsyncIterator<V> iteratorAsync(int count) {
        AsyncIterator<V> asyncIterator = new BaseAsyncIterator<V, Object>() {

            @Override
            protected RFuture<ScanResult<Object>> iterator(RedisClient client, String nextItPos) {
                return scanIteratorAsync(name, client, nextItPos, null, count);
            }

        };
        return new CompositeAsyncIterator<>(Arrays.asList(asyncIterator), 0);
    }

    /** 移除监听器。 */
    @Override
    public void removeListener(int listenerId) {
        removeTrackingListener(listenerId);
        removeListener(listenerId, "__keyevent@*:zadd", "__keyevent@*:zrem");

        String expiredChannelName = getExpiredChannelName();
        if (getListenerIdsByName(expiredChannelName).contains(listenerId)) {
            RTopic topic = getTopic(expiredChannelName);
            removeListenerId(expiredChannelName, listenerId);
            topic.removeListener(listenerId);
        }

        super.removeListener(listenerId);
    }

    /** 异步执行 removeListener。 */
    @Override
    public RFuture<Void> removeListenerAsync(int listenerId) {
        String expiredChannelName = getExpiredChannelName();
        if (getListenerIdsByName(expiredChannelName).contains(listenerId)) {
            RTopic topic = getTopic(expiredChannelName);
            removeListenerId(expiredChannelName, listenerId);
            CompletionStage<Void> r = topic.removeListenerAsync(listenerId)
                    .thenCompose(v -> removeTrackingListenerAsync(listenerId))
                    .thenCompose(v -> removeListenerAsync(null, listenerId, "__keyevent@*:zadd", "__keyevent@*:zrem"));
            return new CompletableFutureWrapper<>(r);
        }

        return removeListenerAsync(removeTrackingListenerAsync(listenerId), listenerId,
                "__keyevent@*:zadd", "__keyevent@*:zrem");
    }

}
