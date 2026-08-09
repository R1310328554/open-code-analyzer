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

import org.redisson.api.*;
import org.redisson.api.mapreduce.RCollectionMapReduce;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.decoder.*;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.iterator.BaseAsyncIterator;
import org.redisson.iterator.RedissonBaseIterator;
import org.redisson.misc.CompositeAsyncIterator;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 集合型 Multimap Cache 值视图 {@link RSet} 实现。
 * <p>表示 {@link RedissonSetMultimapCache} 某 key 下的 Set 元素集合；
 * 读操作会校验过期 ZSET，写操作委托底层 {@link RedissonSet}。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public class RedissonSetMultimapValues<V> extends RedissonExpirable implements RSet<V> {

    private static final RedisCommand<ListScanResult<Object>> EVAL_SSCAN = new RedisCommand<ListScanResult<Object>>("EVAL", 
                new ListMultiDecoder2(new ListScanResultReplayDecoder(), new MapValueDecoder(new ObjectListReplayDecoder())));
    
    /** 底层 Set 委托对象。 */
    private final RSet<V> set;
    /** Multimap 外层键。 */
    private final Object key;
    /** 过期时间 ZSET 键名。 */
    private final String timeoutSetName;
    
    public RedissonSetMultimapValues(Codec codec, CommandAsyncExecutor commandExecutor, String name, String timeoutSetName, Object key) {
        super(codec, commandExecutor, name);
        this.timeoutSetName = timeoutSetName;
        this.key = key;
        this.set = new RedissonSet<V>(codec, commandExecutor, name, null);
    }

    /** 返回元素/条目数量。 */
    @Override
    public int size() {
        return get(sizeAsync());
    }
    
    /** 创建 MapReduce 任务入口。 */
    @Override
    public <KOut, VOut> RCollectionMapReduce<V, KOut, VOut> mapReduce() {
        return null;
    }
    
    /** 尝试添加元素（Set 语义）。 */
    @Override
    public boolean tryAdd(V... values) {
        return get(tryAddAsync(values));
    }

    /** Set Multimap 值视图 containsEach 操作。 */
    @Override
    public Set<V> containsEach(Collection<V> c) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values");
    }

    /** 异步 tryAdd。 */
    @Override
    public RFuture<Boolean> tryAddAsync(V... values) {
        return set.tryAddAsync(values);
    }

    /** 异步执行 clearExpire。 */
    @Override
    public RFuture<Boolean> clearExpireAsync() {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values");
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values");
    }
    
    /** 异步执行 expireAt。 */
    @Override
    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values");
    }
    
    /** 异步执行 remainTimeToLive。 */
    @Override
    public RFuture<Long> remainTimeToLiveAsync() {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values");
    }
    
    /** 异步执行 rename。 */
    @Override
    public RFuture<Void> renameAsync(String newName) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values");
    }
    
    /** 异步执行 renamenx。 */
    @Override
    public RFuture<Boolean> renamenxAsync(String newName) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values");
    }
    
    /** 异步删除键。 */
    @Override
    public RFuture<Boolean> deleteAsync() {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore) "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return 0;"
              + "end; " +
                "local res = redis.call('zrem', KEYS[1], ARGV[2]); " +
                "if res > 0 then " +
                    "redis.call('del', KEYS[2]); " +
                "end; " +
                "return res; ",
                Arrays.<Object>asList(timeoutSetName, getRawName()),
                System.currentTimeMillis(), encodeMapKey(key));
    }

    /** 异步执行 sizeInMemory。 */
    @Override
    public RFuture<Long> sizeInMemoryAsync() {
        List<Object> keys = Arrays.<Object>asList(getRawName(), timeoutSetName);
        return super.sizeInMemoryAsync(keys);
    }

    /** 异步执行 copy。 */
    @Override
    public RFuture<Boolean> copyAsync(List<Object> keys, int database, boolean replace) {
        throw new UnsupportedOperationException();
    }

    /** 异步返回数量。 */
    @Override
    public RFuture<Integer> sizeAsync() {
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_INTEGER,
                      "local expireDate = 92233720368547758; " +
                      "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
                    + "if expireDateScore ~= false then "
                        + "expireDate = tonumber(expireDateScore) "
                    + "end; "
                    + "if expireDate <= tonumber(ARGV[1]) then "
                        + "return 0;"
                    + "end; "
                    + "return redis.call('scard', KEYS[2]);",
               Arrays.<Object>asList(timeoutSetName, getRawName()),
               System.currentTimeMillis(), encodeMapKey(key));
    }

    /** 是否为空。 */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /** 是否包含指定元素。 */
    @Override
    public boolean contains(Object o) {
        return get(containsAsync(o));
    }

    /** 异步检查是否包含。 */
    @Override
    public RFuture<Boolean> containsAsync(Object o) {
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore) "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return 0;"
              + "end; "
              + "return redis.call('sismember', KEYS[2], ARGV[3]);",
         Arrays.<Object>asList(timeoutSetName, getRawName()),
         System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(o));
    }

    /** Set Multimap 值视图 scanIterator 操作。 */
    private ScanResult<Object> scanIterator(RedisClient client, String startPos, String pattern, int count) {
        return get(scanIteratorAsync(client, startPos, pattern, count));
    }

    /** 异步执行 scanIterator。 */
    private RFuture<ScanResult<Object>> scanIteratorAsync(RedisClient client, String startPos, String pattern, int count) {
        List<Object> params = new ArrayList<Object>();
        params.add(System.currentTimeMillis());
        params.add(startPos);
        params.add(encodeMapKey(key));
        if (pattern != null) {
            params.add(pattern);
        }
        params.add(count);
        
        return commandExecutor.evalReadAsync(client, getRawName(), codec, EVAL_SSCAN,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[3]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore) "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return {0, {}};"
              + "end;"

              + "local res; "
              + "if (#ARGV == 5) then "
                  + "res = redis.call('sscan', KEYS[2], ARGV[2], 'match', ARGV[4], 'count', ARGV[5]); "
              + "else "
                  + "res = redis.call('sscan', KEYS[2], ARGV[2], 'count', ARGV[4]); "
              + "end;"

              + "return res;", 
              Arrays.<Object>asList(timeoutSetName, getRawName()),
              params.toArray());
    }

    /** 返回元素迭代器。 */
    @Override
    public Iterator<V> iterator(int count) {
        return iterator(null, count);
    }
    
    /** 返回元素迭代器。 */
    @Override
    public Iterator<V> iterator(String pattern) {
        return iterator(pattern, 10);
    }

    /** Set Multimap 值视图 distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(final String pattern) {
        String iteratorName = "__redisson_set_cursor_{" + getRawName() + "}";
        return distributedIterator(iteratorName, pattern, 10);
    }

    /** Set Multimap 值视图 distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(final int count) {
        String iteratorName = "__redisson_set_cursor_{" + getRawName() + "}";
        return distributedIterator(iteratorName, null, count);
    }

    /** Set Multimap 值视图 distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(final String iteratorName, final String pattern, final int count) {
        return new RedissonBaseIterator<V>() {

            @Override
            protected ScanResult<Object> iterator(RedisClient client, String nextIterPos) {
                return distributedScanIterator(iteratorName, pattern, count);
            }

            @Override
            protected void remove(Object value) {
                RedissonSetMultimapValues.this.remove((V) value);
            }
        };
    }

    /** Set Multimap 值视图 distributedScanIterator 操作。 */
    private ScanResult<Object> distributedScanIterator(String iteratorName, String pattern, int count) {
        return get(distributedScanIteratorAsync(iteratorName, pattern, count));
    }

    /** 异步执行 distributedScanIterator。 */
    private RFuture<ScanResult<Object>> distributedScanIteratorAsync(String iteratorName, String pattern, int count) {
        List<Object> args = new ArrayList<>(3);
        args.add(System.currentTimeMillis());
        if (pattern != null) {
            args.add(pattern);
        }
        args.add(count);

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_SCAN,
                "local cursor = redis.call('get', KEYS[3]); "
                + "if cursor ~= false then "
                    + "cursor = tonumber(cursor); "
                + "else"
                    + " cursor = 0;"
                + "end;"
                + "if cursor == -1 then "
                    + "return {0, {}}; "
                + "end;"
                + "local result; "
                + "if (#ARGV == 3) then "
                    + "result = redis.call('sscan', KEYS[2], cursor, 'match', ARGV[2], 'count', ARGV[3]); "
                + "else"
                    + "result = redis.call('sscan', KEYS[2], cursor, 'count', ARGV[2]); "
                + "end;"
                + "local next_cursor = result[1]"
                + "if next_cursor ~= \"0\" then "
                    + "redis.call('setex', KEYS[3], 3600, next_cursor);"
                + "else "
                    + "redis.call('setex', KEYS[3], 3600, -1);"
                + "end; "

                + "local expireDate = 92233720368547758; "
                + "local expirations = redis.call('zmscore', KEYS[1], result[2])"
                + "for i = #expirations, 1, -1 do "
                    + "if expirations[i] ~= false then "
                        + "local expireDate = tonumber(expireDateScore) "
                        + "if expireDate <= tonumber(ARGV[1]) then "
                        +   "table.remove(result[2], i);"
                        + "end; "
                    + "end; "
                + "end; "
                + "return result;",
                Arrays.<Object>asList(timeoutSetName, getRawName(), iteratorName), args.toArray());
    }

    /** 返回元素迭代器。 */
    @Override
    public Iterator<V> iterator(final String pattern, final int count) {
        return new RedissonBaseIterator<V>() {

            @Override
            protected ScanResult<Object> iterator(RedisClient client, String nextIterPos) {
                return scanIterator(client, nextIterPos, pattern, count);
            }

            @Override
            protected void remove(Object value) {
                RedissonSetMultimapValues.this.remove((V) value);
            }
            
        };
    }
    
    /** 返回元素迭代器。 */
    @Override
    public Iterator<V> iterator() {
        return iterator(null);
    }

    /** 异步一次性读取全部元素。 */
    @Override
    public RFuture<Set<V>> readAllAsync() {
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_MAP_VALUE_SET,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore) "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return {};"
              + "end; "
              + "return redis.call('smembers', KEYS[2]);",
              Arrays.<Object>asList(timeoutSetName, getRawName()),
              System.currentTimeMillis(), encodeMapKey(key));
    }

    /** 一次性读取全部元素。 */
    @Override
    public Set<V> readAll() {
        return get(readAllAsync());
    }

    /** Set Multimap 值视图 toArray 操作。 */
    @Override
    public Object[] toArray() {
        Set<Object> res = (Set<Object>) get(readAllAsync());
        return res.toArray();
    }

    /** Set Multimap 值视图 toArray 操作。 */
    @Override
    public <T> T[] toArray(T[] a) {
        Set<Object> res = (Set<Object>) get(readAllAsync());
        return res.toArray(a);
    }

    /** 向 Stream 追加条目。 */
    @Override
    public boolean add(V e) {
        return set.add(e);
    }

    /** 异步 XADD。 */
    @Override
    public RFuture<Boolean> addAsync(V e) {
        return set.addAsync(e);
    }

    /** Set Multimap 值视图 removeRandom 操作。 */
    @Override
    public V removeRandom() {
        return set.removeRandom();
    }

    /** 异步执行 removeRandom。 */
    @Override
    public RFuture<V> removeRandomAsync() {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SPOP_SINGLE, getRawName());
    }

    /** Set Multimap 值视图 removeRandom 操作。 */
    @Override
    public Set<V> removeRandom(int amount) {
        return get(removeRandomAsync(amount));
    }

    /** 异步执行 removeRandom。 */
    @Override
    public RFuture<Set<V>> removeRandomAsync(int amount) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SPOP, getRawName(), amount);
    }
    
    /** Set Multimap 值视图 random 操作。 */
    @Override
    public V random() {
        return get(randomAsync());
    }

    /** 异步执行 random。 */
    @Override
    public RFuture<V> randomAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SRANDMEMBER_SINGLE, getRawName());
    }

    /** Set Multimap 值视图 random 操作。 */
    @Override
    public Set<V> random(int count) {
        return get(randomAsync(count));
    }

    /** 异步执行 random。 */
    @Override
    public RFuture<Set<V>> randomAsync(int count) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SRANDMEMBER, getRawName(), count);
    }
    
    /** 异步移除元素。 */
    @Override
    public RFuture<Boolean> removeAsync(Object o) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore) "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return 0;"
              + "end; "
              + "return redis.call('srem', KEYS[2], ARGV[3]) > 0 and 1 or 0;",
         Arrays.<Object>asList(timeoutSetName, getRawName()),
         System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(o));
    }

    /** 移除元素。 */
    @Override
    public boolean remove(Object value) {
        return get(removeAsync((V) value));
    }

    /** 异步执行 move。 */
    @Override
    public RFuture<Boolean> moveAsync(String destination, V member) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SMOVE, getRawName(), destination, encode(member));
    }

    /** Set Multimap 值视图 move 操作。 */
    @Override
    public boolean move(String destination, V member) {
        return get(moveAsync(destination, member));
    }

    /** 是否包含指定集合的全部元素。 */
    @Override
    public boolean containsAll(Collection<?> c) {
        return get(containsAllAsync(c));
    }

    /** 异步 containsAll。 */
    @Override
    public RFuture<Boolean> containsAllAsync(Collection<?> c) {
        List<Object> args = new ArrayList<Object>(c.size() + 2);
        args.add(System.currentTimeMillis());
        args.add(encodeMapKey(key));
        encodeMapValues(args, c);
        
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore) "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return 0;"
              + "end; " +
                "local s = redis.call('smembers', KEYS[2]);" +
                        "for i = 1, #s, 1 do " +
                            "for j = #ARGV, 3, -1 do "
                            + "if ARGV[j] == s[i] "
                            + "then table.remove(ARGV, j) end "
                        + "end; "
                       + "end;"
                       + "return #ARGV == 2 and 1 or 0; ",
                   Arrays.<Object>asList(timeoutSetName, getRawName()), args.toArray());
    }

    /** 批量添加元素。 */
    @Override
    public boolean addAll(Collection<? extends V> c) {
        if (c.isEmpty()) {
            return false;
        }

        return get(addAllAsync(c));
    }

    /** 异步批量添加。 */
    @Override
    public RFuture<Boolean> addAllAsync(Collection<? extends V> c) {
        List<Object> args = new ArrayList<Object>(c.size() + 1);
        args.add(getRawName());
        encodeMapValues(args, c);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SADD_BOOL, args.toArray());
    }

    /** 异步执行 addAllCounted。 */
    @Override
    public RFuture<Integer> addAllCountedAsync(Collection<? extends V> c) {
        List<Object> args = new ArrayList<>(c.size() + 1);
        args.add(getRawName());
        encodeMapValues(args, c);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SADD, args.toArray());
    }

    /** Set Multimap 值视图 addAllCounted 操作。 */
    @Override
    public int addAllCounted(Collection<? extends V> c) {
        return get(addAllCountedAsync(c));
    }

    /** Set Multimap 值视图 removeAllCounted 操作。 */
    @Override
    public int removeAllCounted(Collection<? extends V> c) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values");
    }

    /** 异步执行 removeAllCounted。 */
    @Override
    public RFuture<Integer> removeAllCountedAsync(Collection<? extends V> c) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values");
    }

    /** 异步执行 containsEach。 */
    @Override
    public RFuture<Set<V>> containsEachAsync(Collection<V> c) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values");
    }

    /** 异步执行 iterator。 */
    @Override
    public AsyncIterator<V> iteratorAsync() {
        return iteratorAsync(10);
    }

    /** 异步执行 iterator。 */
    @Override
    public AsyncIterator<V> iteratorAsync(int count) {
        AsyncIterator<V> asyncIterator = new BaseAsyncIterator<V, Object>() {

            @Override
            protected RFuture<ScanResult<Object>> iterator(RedisClient client, String nextItPos) {
                return scanIteratorAsync(client, nextItPos, null, count);
            }

        };
        return new CompositeAsyncIterator<>(Arrays.asList(asyncIterator), 0);
    }

    /** 仅保留指定集合中的元素。 */
    @Override
    public boolean retainAll(Collection<?> c) {
        return get(retainAllAsync(c));
    }

    /** 异步 retainAll。 */
    @Override
    public RFuture<Boolean> retainAllAsync(Collection<?> c) {
        List<Object> args = new ArrayList<Object>(c.size() + 2);
        args.add(System.currentTimeMillis());
        args.add(encodeMapKey(key));
        encodeMapValues(args, c);

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                    "local expireDate = 92233720368547758; " +
                    "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
                  + "if expireDateScore ~= false then "
                      + "expireDate = tonumber(expireDateScore) "
                  + "end; "
                  + "if expireDate <= tonumber(ARGV[1]) then "
                      + "return 0;"
                  + "end; " +

                    "local changed = 0 " +
                    "local s = redis.call('smembers', KEYS[2]) "
                       + "local i = 1 "
                       + "while i <= #s do "
                            + "local element = s[i] "
                            + "local isInAgrs = false "
                            + "for j = 3, #ARGV, 1 do "
                                + "if ARGV[j] == element then "
                                    + "isInAgrs = true "
                                    + "break "
                                + "end "
                            + "end "
                            + "if isInAgrs == false then "
                                + "redis.call('SREM', KEYS[2], element) "
                                + "changed = 1 "
                            + "end "
                            + "i = i + 1 "
                       + "end "
                       + "return changed ",
                       Arrays.<Object>asList(timeoutSetName, getRawName()), args.toArray());
    }

    /** 异步批量移除。 */
    @Override
    public RFuture<Boolean> removeAllAsync(Collection<?> c) {
        List<Object> args = new ArrayList<Object>(c.size() + 2);
        args.add(System.currentTimeMillis());
        args.add(encodeMapKey(key));
        encodeMapValues(args, c);
        
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                        "local expireDate = 92233720368547758; " +
                        "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
                      + "if expireDateScore ~= false then "
                          + "expireDate = tonumber(expireDateScore) "
                      + "end; "
                      + "if expireDate <= tonumber(ARGV[1]) then "
                          + "return 0;"
                      + "end; " +
                
                        "local v = 0 " +
                        "for i = 3, #ARGV, 1 do "
                            + "if redis.call('srem', KEYS[2], ARGV[i]) == 1 "
                            + "then v = 1 end "
                        +"end "
                       + "return v ",
               Arrays.<Object>asList(timeoutSetName, getRawName()), args.toArray());
    }

    /** 获取 CountDownLatch。 */
    @Override
    public RCountDownLatch getCountDownLatch(V value) {
        return set.getCountDownLatch(value);
    }

    /** 获取 PermitExpirableSemaphore。 */
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(V value) {
        return set.getPermitExpirableSemaphore(value);
    }

    /** 获取 Semaphore。 */
    @Override
    public RSemaphore getSemaphore(V value) {
        return set.getSemaphore(value);
    }

    /** 获取 FairLock。 */
    @Override
    public RLock getFairLock(V value) {
        return set.getFairLock(value);
    }

    /** 获取 ReadWriteLock。 */
    @Override
    public RReadWriteLock getReadWriteLock(V value) {
        return set.getReadWriteLock(value);
    }

    /** 获取 Lock。 */
    @Override
    public RLock getLock(V value) {
        return set.getLock(value);
    }
    
    /** 批量移除元素。 */
    @Override
    public boolean removeAll(Collection<?> c) {
        return get(removeAllAsync(c));
    }

    /** 与指定集合求并集并写回。 */
    @Override
    public int union(String... names) {
        return get(unionAsync(names));
    }

    /** 异步求并集。 */
    @Override
    public RFuture<Integer> unionAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SUNIONSTORE_INT, args.toArray());
    }

    /** Set Multimap 值视图 readUnion 操作。 */
    @Override
    public Set<V> readUnion(String... names) {
        return get(readUnionAsync(names));
    }

    /** 异步执行 readUnion。 */
    @Override
    public RFuture<Set<V>> readUnionAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SUNION, args.toArray());
    }

    /** 清空全部元素。 */
    @Override
    public void clear() {
        delete();
    }

    /** 与指定集合求差集并写回。 */
    @Override
    public int diff(String... names) {
        return get(diffAsync(names));
    }

    /** 异步求差集。 */
    @Override
    public RFuture<Integer> diffAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SDIFFSTORE_INT, args.toArray());
    }

    /** Set Multimap 值视图 readDiff 操作。 */
    @Override
    public Set<V> readDiff(String... names) {
        return get(readDiffAsync(names));
    }

    /** 异步执行 readDiff。 */
    @Override
    public RFuture<Set<V>> readDiffAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SDIFF, args.toArray());
    }

    /** 与指定集合求交集并写回。 */
    @Override
    public int intersection(String... names) {
        return get(intersectionAsync(names));
    }

    /** 异步求交集。 */
    @Override
    public RFuture<Integer> intersectionAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SINTERSTORE_INT, args.toArray());
    }

    /** Set Multimap 值视图 readIntersection 操作。 */
    @Override
    public Set<V> readIntersection(String... names) {
        return get(readIntersectionAsync(names));
    }

    /** 异步执行 readIntersection。 */
    @Override
    public RFuture<Set<V>> readIntersectionAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SINTER, args.toArray());
    }

    /** Set Multimap 值视图 countIntersection 操作。 */
    @Override
    public Integer countIntersection(String... names) {
        return get(countIntersectionAsync(names));
    }

    /** 异步执行 countIntersection。 */
    @Override
    public RFuture<Integer> countIntersectionAsync(String... names) {
        return countIntersectionAsync(0, names);
    }

    /** Set Multimap 值视图 countIntersection 操作。 */
    @Override
    public Integer countIntersection(int limit, String... names) {
        return get(countIntersectionAsync(limit, names));
    }

    /** 异步执行 countIntersection。 */
    @Override
    public RFuture<Integer> countIntersectionAsync(int limit, String... names) {
        List<Object> args = new ArrayList<>(names.length + 1);
        args.add(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        if (limit > 0) {
            args.add("LIMIT");
            args.add(limit);
        }
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SINTERCARD_INT, args.toArray());
    }

    /** Set Multimap 值视图 countUnion 操作。 */
    @Override
    public Integer countUnion(String... names) {
        return get(countUnionAsync(names));
    }

    /** 异步执行 countUnion。 */
    @Override
    public RFuture<Integer> countUnionAsync(String... names) {
        return countUnionAsync(0, names);
    }

    /** Set Multimap 值视图 countUnion 操作。 */
    @Override
    public Integer countUnion(int limit, String... names) {
        return get(countUnionAsync(limit, names));
    }

    /** 异步执行 countUnion。 */
    @Override
    public RFuture<Integer> countUnionAsync(int limit, String... names) {
        return unionCardAsync(false, limit, names);
    }

    /** Set Multimap 值视图 countUnionApprox 操作。 */
    @Override
    public Integer countUnionApprox(String... names) {
        return get(countUnionApproxAsync(names));
    }

    /** 异步执行 countUnionApprox。 */
    @Override
    public RFuture<Integer> countUnionApproxAsync(String... names) {
        return countUnionApproxAsync(0, names);
    }

    /** Set Multimap 值视图 countUnionApprox 操作。 */
    @Override
    public Integer countUnionApprox(int limit, String... names) {
        return get(countUnionApproxAsync(limit, names));
    }

    /** 异步执行 countUnionApprox。 */
    @Override
    public RFuture<Integer> countUnionApproxAsync(int limit, String... names) {
        return unionCardAsync(true, limit, names);
    }

    /** 异步执行 unionCard。 */
    private RFuture<Integer> unionCardAsync(boolean approx, int limit, String... names) {
        List<Object> args = new ArrayList<>(names.length + 5);
        args.add(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        if (approx) {
            args.add("APPROX");
        }
        if (limit > 0) {
            args.add("LIMIT");
            args.add(limit);
        }
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SUNIONCARD_INT, args.toArray());
    }

    /** Set Multimap 值视图 countDiff 操作。 */
    @Override
    public Integer countDiff(String... names) {
        return get(countDiffAsync(names));
    }

    /** 异步执行 countDiff。 */
    @Override
    public RFuture<Integer> countDiffAsync(String... names) {
        return countDiffAsync(0, names);
    }

    /** Set Multimap 值视图 countDiff 操作。 */
    @Override
    public Integer countDiff(int limit, String... names) {
        return get(countDiffAsync(limit, names));
    }

    /** 异步执行 countDiff。 */
    @Override
    public RFuture<Integer> countDiffAsync(int limit, String... names) {
        List<Object> args = new ArrayList<>(names.length + 4);
        args.add(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        if (limit > 0) {
            args.add("LIMIT");
            args.add(limit);
        }
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SDIFFCARD_INT, args.toArray());
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(SortOrder order) {
        return set.readSortAsync(order);
    }

    /** Set Multimap 值视图 readSort 操作。 */
    @Override
    public Set<V> readSort(SortOrder order) {
        return set.readSort(order);
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(SortOrder order, int offset, int count) {
        return set.readSortAsync(order, offset, count);
    }

    /** Set Multimap 值视图 readSort 操作。 */
    @Override
    public Set<V> readSort(SortOrder order, int offset, int count) {
        return set.readSort(order, offset, count);
    }

    /** Set Multimap 值视图 readSort 操作。 */
    @Override
    public Set<V> readSort(String byPattern, SortOrder order) {
        return set.readSort(byPattern, order);
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(String byPattern, SortOrder order) {
        return set.readSortAsync(byPattern, order);
    }

    /** Set Multimap 值视图 readSort 操作。 */
    @Override
    public Set<V> readSort(String byPattern, SortOrder order, int offset, int count) {
        return set.readSort(byPattern, order, offset, count);
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(String byPattern, SortOrder order, int offset, int count) {
        return set.readSortAsync(byPattern, order, offset, count);
    }

    /** Set Multimap 值视图 readSort 操作。 */
    @Override
    public <T> Collection<T> readSort(String byPattern, List<String> getPatterns, SortOrder order) {
        return set.readSort(byPattern, getPatterns, order);
    }

    /** 异步执行 readSort。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order) {
        return set.readSortAsync(byPattern, getPatterns, order);
    }

    /** Set Multimap 值视图 readSort 操作。 */
    @Override
    public <T> Collection<T> readSort(String byPattern, List<String> getPatterns, SortOrder order, int offset,
            int count) {
        return set.readSort(byPattern, getPatterns, order, offset, count);
    }

    /** 异步执行 readSort。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order,
            int offset, int count) {
        return set.readSortAsync(byPattern, getPatterns, order, offset, count);
    }

    /** Set Multimap 值视图 readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(SortOrder order) {
        return set.readSortAlpha(order);
    }

    /** Set Multimap 值视图 readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(SortOrder order, int offset, int count) {
        return set.readSortAlpha(order, offset, count);
    }

    /** Set Multimap 值视图 readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(String byPattern, SortOrder order) {
        return set.readSortAlpha(byPattern, order);
    }

    /** Set Multimap 值视图 readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(String byPattern, SortOrder order, int offset, int count) {
        return set.readSortAlpha(byPattern, order, offset, count);
    }

    /** Set Multimap 值视图 readSortAlpha 操作。 */
    @Override
    public <T> Collection<T> readSortAlpha(String byPattern, List<String> getPatterns, SortOrder order) {
        return set.readSortAlpha(byPattern, getPatterns, order);
    }

    /** Set Multimap 值视图 readSortAlpha 操作。 */
    @Override
    public <T> Collection<T> readSortAlpha(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return set.readSortAlpha(byPattern, getPatterns, order, offset, count);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(SortOrder order) {
        return set.readSortAlphaAsync(order);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(SortOrder order, int offset, int count) {
        return set.readSortAlphaAsync(order, offset, count);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(String byPattern, SortOrder order) {
        return set.readSortAlphaAsync(byPattern, order);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(String byPattern, SortOrder order, int offset, int count) {
        return set.readSortAlphaAsync(byPattern, order, offset, count);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order) {
        return set.readSortAlphaAsync(byPattern, getPatterns, order);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return set.readSortAlphaAsync(byPattern, getPatterns, order, offset, count);
    }

    /** Set Multimap 值视图 sortTo 操作。 */
    @Override
    public int sortTo(String destName, SortOrder order) {
        return set.sortTo(destName, order);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, SortOrder order) {
        return set.sortToAsync(destName, order);
    }

    /** Set Multimap 值视图 sortTo 操作。 */
    @Override
    public int sortTo(String destName, SortOrder order, int offset, int count) {
        return set.sortTo(destName, order, offset, count);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, SortOrder order, int offset, int count) {
        return set.sortToAsync(destName, order, offset, count);
    }

    /** Set Multimap 值视图 sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, SortOrder order) {
        return set.sortTo(destName, byPattern, order);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order) {
        return set.sortToAsync(destName, byPattern, order);
    }

    /** Set Multimap 值视图 sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, SortOrder order, int offset, int count) {
        return set.sortTo(destName, byPattern, order, offset, count);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order, int offset, int count) {
        return set.sortToAsync(destName, byPattern, order, offset, count);
    }

    /** Set Multimap 值视图 sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, List<String> getPatterns, SortOrder order) {
        return set.sortTo(destName, byPattern, getPatterns, order);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order) {
        return set.sortToAsync(destName, byPattern, getPatterns, order);
    }

    /** Set Multimap 值视图 sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, List<String> getPatterns, SortOrder order, int offset,
            int count) {
        return set.sortTo(destName, byPattern, getPatterns, order, offset, count);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order,
            int offset, int count) {
        return set.sortToAsync(destName, byPattern, getPatterns, order, offset, count);
    }

    /** Set Multimap 值视图 stream 操作。 */
    @Override
    public Stream<V> stream(int count) {
        return toStream(iterator(count));
    }

    /** Set Multimap 值视图 stream 操作。 */
    @Override
    public Stream<V> stream(String pattern, int count) {
        return toStream(iterator(pattern, count));
    }

    /** Set Multimap 值视图 stream 操作。 */
    @Override
    public Stream<V> stream(String pattern) {
        return toStream(iterator(pattern));
    }
    
}
