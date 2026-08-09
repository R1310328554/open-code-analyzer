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

import org.redisson.api.RCircularBuffer;
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.convertor.DoubleReplayConvertor;
import org.redisson.client.protocol.convertor.IntegerReplayConvertor;
import org.redisson.client.protocol.decoder.ListFirstObjectDecoder;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis {@code ARRAY} 环形缓冲区 {@link RCircularBuffer} 实现（Redis 8+）。
 * <p>固定容量循环覆盖写入，支持 ARPUSH/ARLTRIM 等 ARRAY 命令。
 *
 * @param <V> 元素类型
 * @author Nikita Koksharov
 */
public class RedissonCircularBuffer<V> extends RedissonExpirable implements RCircularBuffer<V> {

    private static final RedisStrictCommand<Integer> GET_INTEGER =
            new RedisStrictCommand<>("GET", new IntegerReplayConvertor(0));

    private static final RedisStrictCommand<Integer> ARLEN_INTEGER =
            new RedisStrictCommand<>("ARLEN", new IntegerReplayConvertor(0));

    private static final RedisStrictCommand<Double> EVAL_DOUBLE =
            new RedisStrictCommand<>("EVAL", new DoubleReplayConvertor());

    private static final RedisCommand<Object> PEEK_LAST =
            new RedisCommand<>("ARLASTITEMS", new ListFirstObjectDecoder());

    private static final RedisCommand<Boolean> ARLEN_BOOL = new RedisCommand<Boolean>("ARLEN", obj -> (Long) obj == 0);

    /** 环形缓冲区容量配置的 Redis 键。 */
    private final String settingsName;

    public RedissonCircularBuffer(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        settingsName = prefixName("redisson_acb", getRawName());
    }

    public RedissonCircularBuffer(Codec codec, CommandAsyncExecutor commandExecutor, String name) {
        super(codec, commandExecutor, name);
        settingsName = prefixName("redisson_acb", getRawName());
    }

    /** 尝试设置环形缓冲区容量（仅首次）。 */
    @Override
    public boolean trySetCapacity(int capacity) {
        return get(trySetCapacityAsync(capacity));
    }

    /** 异步 trySetCapacity。 */
    @Override
    public RFuture<Boolean> trySetCapacityAsync(int capacity) {
        checkCapacity(capacity);
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.SETNX, settingsName, capacity);
    }

    /** 设置环形缓冲区容量。 */
    @Override
    public void setCapacity(int capacity) {
        get(setCapacityAsync(capacity));
    }

    /** 设置CapacityAsync。 */
    @Override
    public RFuture<Void> setCapacityAsync(int capacity) {
        checkCapacity(capacity);
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.SET, settingsName, capacity);
    }

    /** 返回当前容量。 */
    @Override
    public int capacity() {
        return get(capacityAsync());
    }

    /** 异步执行 capacity。 */
    @Override
    public RFuture<Integer> capacityAsync() {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, GET_INTEGER, settingsName);
    }

    /** 返回剩余可写容量。 */
    @Override
    public int remainingCapacity() {
        return get(remainingCapacityAsync());
    }

    /** 异步执行 remainingCapacity。 */
    @Override
    public RFuture<Integer> remainingCapacityAsync() {
        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                "local limit = tonumber(redis.call('get', KEYS[2]) or '0'); "
              + "local size = redis.call('ARLEN', KEYS[1]); "
              + "local rem = limit - size; "
              + "if rem < 0 then rem = 0; end; "
              + "return rem; ",
             Arrays.asList(getRawName(), settingsName));
    }

    /** 追加元素（满则覆盖最旧）。 */
    @Override
    public boolean add(V value) {
        return get(addAsync(value));
    }

    /** 异步追加元素。 */
    @Override
    public RFuture<Boolean> addAsync(V value) {
        Objects.requireNonNull(value, "Value can't be null");
        return commandExecutor.evalWriteNoRetryAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "local limit = redis.call('get', KEYS[2]); "
              + "assert(limit ~= false, 'CircularBuffer capacity is not defined'); "
              + "redis.call('ARRING', KEYS[1], limit, ARGV[1]); "
              + "return 1; ",
             Arrays.asList(getRawName(), settingsName), encode(value));
    }

    /** 环形缓冲区 addAll 操作。 */
    @Override
    public boolean addAll(Collection<? extends V> values) {
        return get(addAllAsync(values));
    }

    /** 异步执行 addAll。 */
    @Override
    public RFuture<Boolean> addAllAsync(Collection<? extends V> values) {
        Objects.requireNonNull(values, "Values can't be null");
        if (values.isEmpty()) {
            return new CompletableFutureWrapper<>(false);
        }

        List<Object> args = new ArrayList<>(values.size());
        encode(args, values);
        return commandExecutor.evalWriteNoRetryAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "local limit = redis.call('get', KEYS[2]); "
              + "assert(limit ~= false, 'CircularBuffer capacity is not defined'); "
              + "for i = 1, #ARGV, 4000 do "
                  + "redis.call('ARRING', KEYS[1], limit, unpack(ARGV, i, math.min(i + 3999, #ARGV))); "
              + "end; "
              + "return 1; ",
             Arrays.asList(getRawName(), settingsName), args.toArray());
    }

    /** JSON 路径写入。 */
    @Override
    public long set(int size, V... values) {
        return get(setAsync(size, values));
    }

    /** 异步 JSON 写入。 */
    @Override
    public RFuture<Long> setAsync(int size, V... values) {
        checkCapacity(size);
        Objects.requireNonNull(values, "Values can't be null");
        for (V value : values) {
            Objects.requireNonNull(value, "Value can't be null");
        }
        if (values.length == 0) {
            throw new IllegalArgumentException("Values can't be empty");
        }

        List<Object> args = new ArrayList<>(values.length + 1);
        args.add(size);
        encode(args, Arrays.asList(values));
        return commandExecutor.evalWriteNoRetryAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.EVAL_LONG,
                "redis.call('set', KEYS[2], ARGV[1]); "
              + "local last; "
              + "for i = 2, #ARGV, 4000 do "
                  + "last = redis.call('ARRING', KEYS[1], ARGV[1], unpack(ARGV, i, math.min(i + 3999, #ARGV))); "
              + "end; "
              + "return last; ",
             Arrays.asList(getRawName(), settingsName), args.toArray());
    }

    /** JSON 路径读取。 */
    @Override
    public V get(long index) {
        return get(getAsync(index));
    }

    /** 异步 JSON 读取。 */
    @Override
    public RFuture<V> getAsync(long index) {
        checkIndex(index);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ARGET, getRawName(), index);
    }

    /** 环形缓冲区 lastItems 操作。 */
    @Override
    public List<V> lastItems(int count, boolean reverse) {
        return get(lastItemsAsync(count, reverse));
    }

    /** 异步执行 lastItems。 */
    @Override
    public RFuture<List<V>> lastItemsAsync(int count, boolean reverse) {
        if (count <= 0) {
            return new CompletableFutureWrapper<>(Collections.emptyList());
        }
        if (reverse) {
            return commandExecutor.readAsync(getRawName(), codec,
                    RedisCommands.ARLASTITEMS, getRawName(), count, "REV");
        }
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ARLASTITEMS, getRawName(), count);
    }

    /** 环形缓冲区 range 操作。 */
    @Override
    public List<V> range(long startIndex, long endIndex) {
        return get(rangeAsync(startIndex, endIndex));
    }

    /** 异步执行 range。 */
    @Override
    public RFuture<List<V>> rangeAsync(long startIndex, long endIndex) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        return commandExecutor.readAsync(getRawName(), codec,
                RedisCommands.ARGETRANGE, getRawName(), startIndex, endIndex);
    }

    /** 环形缓冲区 readAll 操作。 */
    @Override
    public List<V> readAll() {
        return get(readAllAsync());
    }

    /** 异步执行 readAll。 */
    @Override
    public RFuture<List<V>> readAllAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ARLASTITEMS, getRawName(), Integer.MAX_VALUE);
    }

    /** 返回元素/条目数量。 */
    @Override
    public int size() {
        return get(sizeAsync());
    }

    /** 异步返回数量。 */
    @Override
    public RFuture<Integer> sizeAsync() {
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, ARLEN_INTEGER, getRawName());
    }

    /** 环形缓冲区 sum 操作。 */
    @Override
    public Double sum() {
        return get(sumAsync());
    }

    /** 异步执行 sum。 */
    @Override
    public RFuture<Double> sumAsync() {
        return aggregateAllAsync("SUM");
    }

    /** 环形缓冲区 sum 操作。 */
    @Override
    public Double sum(long startIndex, long endIndex) {
        return get(sumAsync(startIndex, endIndex));
    }

    /** 异步执行 sum。 */
    @Override
    public RFuture<Double> sumAsync(long startIndex, long endIndex) {
        return doubleOperationAsync(startIndex, endIndex, "SUM");
    }

    /** 环形缓冲区 min 操作。 */
    @Override
    public Double min() {
        return get(minAsync());
    }

    /** 异步执行 min。 */
    @Override
    public RFuture<Double> minAsync() {
        return aggregateAllAsync("MIN");
    }

    /** 环形缓冲区 min 操作。 */
    @Override
    public Double min(long startIndex, long endIndex) {
        return get(minAsync(startIndex, endIndex));
    }

    /** 异步执行 min。 */
    @Override
    public RFuture<Double> minAsync(long startIndex, long endIndex) {
        return doubleOperationAsync(startIndex, endIndex, "MIN");
    }

    /** 环形缓冲区 max 操作。 */
    @Override
    public Double max() {
        return get(maxAsync());
    }

    /** 异步执行 max。 */
    @Override
    public RFuture<Double> maxAsync() {
        return aggregateAllAsync("MAX");
    }

    /** 环形缓冲区 max 操作。 */
    @Override
    public Double max(long startIndex, long endIndex) {
        return get(maxAsync(startIndex, endIndex));
    }

    /** 异步执行 max。 */
    @Override
    public RFuture<Double> maxAsync(long startIndex, long endIndex) {
        return doubleOperationAsync(startIndex, endIndex, "MAX");
    }

    /** 清空全部条目。 */
    @Override
    public void clear() {
        get(clearAsync());
    }

    /** 异步清空。 */
    @Override
    public RFuture<Void> clearAsync() {
        return commandExecutor.writeAsync(getRawName(), RedisCommands.DEL_VOID, getRawName());
    }

    /** 是否为空。 */
    @Override
    public boolean isEmpty() {
        return get(isEmptyAsync());
    }

    /** 是否EmptyAsync。 */
    @Override
    public RFuture<Boolean> isEmptyAsync() {
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, ARLEN_BOOL, getRawName());
    }

    /** 是否Full。 */
    @Override
    public boolean isFull() {
        return get(isFullAsync());
    }

    /** 是否FullAsync。 */
    @Override
    public RFuture<Boolean> isFullAsync() {
        return commandExecutor.evalReadAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "local cap = tonumber(redis.call('get', KEYS[2]) or '0'); "
              + "if cap == 0 then "
                  + "return 0; "
              + "end; "
              + "local len = redis.call('exists', KEYS[1]) == 1 and redis.call('ARLEN', KEYS[1]) or 0; "
              + "return len >= cap and 1 or 0; ",
             Arrays.asList(getRawName(), settingsName));
    }

    /** 环形缓冲区 peekLast 操作。 */
    @Override
    public V peekLast() {
        return get(peekLastAsync());
    }

    /** 异步执行 peekLast。 */
    @Override
    public RFuture<V> peekLastAsync() {
        return commandExecutor.readAsync(getRawName(), codec, PEEK_LAST, getRawName(), 1, "REV");
    }

    /** 环形缓冲区 peekFirst 操作。 */
    @Override
    public V peekFirst() {
        return get(peekFirstAsync());
    }

    /** 异步执行 peekFirst。 */
    @Override
    public RFuture<V> peekFirstAsync() {
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
         "local idx = redis.call('ARNEXT', KEYS[1]); "
              + "if not idx then idx = 0; end; "
              + "return redis.call('ARGET', KEYS[1], idx); ",
             Arrays.asList(getRawName(), settingsName));
    }

    /** JSON 路径读取。 */
    @Override
    public List<V> get(long... indexes) {
        return get(getAsync(indexes));
    }

    /** 异步 JSON 读取。 */
    @Override
    public RFuture<List<V>> getAsync(long... indexes) {
        if (indexes.length == 0) {
            return new CompletableFutureWrapper<>(Collections.emptyList());
        }

        List<Object> args = new ArrayList<>(indexes.length + 1);
        args.add(getRawName());
        for (long index : indexes) {
            checkIndex(index);
            args.add(index);
        }
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ARMGET, args.toArray());
    }

    /** 环形缓冲区 count 操作。 */
    @Override
    public long count(V value) {
        return get(countAsync(value));
    }

    /** 异步执行 count。 */
    @Override
    public RFuture<Long> countAsync(V value) {
        Objects.requireNonNull(value, "Value can't be null");
        return commandExecutor.evalReadAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.EVAL_LONG,
                "local len = redis.call('exists', KEYS[1]) == 1 and redis.call('ARLEN', KEYS[1]) or 0; "
              + "if len == 0 then "
                  + "return 0; "
              + "end; "
              + "return redis.call('AROP', KEYS[1], 0, len - 1, 'MATCH', ARGV[1]); ",
             Arrays.asList(getRawName()), encode(value));
    }

    /** 环形缓冲区 contains 操作。 */
    @Override
    public boolean contains(V value) {
        return get(containsAsync(value));
    }

    /** 异步执行 contains。 */
    @Override
    public RFuture<Boolean> containsAsync(V value) {
        Objects.requireNonNull(value, "Value can't be null");
        return commandExecutor.evalReadAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
          "local len = redis.call('exists', KEYS[1]) == 1 and redis.call('ARLEN', KEYS[1]) or 0; "
              + "if len == 0 then "
                  + "return 0; "
              + "end; "
              + "return redis.call('AROP', KEYS[1], 0, len - 1, 'MATCH', ARGV[1]) > 0 and 1 or 0; ",
             Arrays.asList(getRawName()), encode(value));
    }

    /** 环形缓冲区 average 操作。 */
    @Override
    public Double average() {
        return get(averageAsync());
    }

    /** 异步执行 average。 */
    @Override
    public RFuture<Double> averageAsync() {
        return commandExecutor.evalReadAsync(getRawName(), StringCodec.INSTANCE, EVAL_DOUBLE,
          "local len = redis.call('exists', KEYS[1]) == 1 and redis.call('ARLEN', KEYS[1]) or 0; "
              + "if len == 0 then "
                  + "return false; "
              + "end; "
              + "local s = redis.call('AROP', KEYS[1], 0, len - 1, 'SUM'); "
              + "if not s then "
                  + "return false; "
              + "end; "
              + "return tostring(tonumber(s) / len); ",
             Arrays.asList(getRawName()));
    }

    /** 环形缓冲区 bitAnd 操作。 */
    @Override
    public Long bitAnd() {
        return get(bitAndAsync());
    }

    /** 异步执行 bitAnd。 */
    @Override
    public RFuture<Long> bitAndAsync() {
        return longAggregateAllAsync("AND");
    }

    /** 环形缓冲区 bitAnd 操作。 */
    @Override
    public Long bitAnd(long startIndex, long endIndex) {
        return get(bitAndAsync(startIndex, endIndex));
    }

    /** 异步执行 bitAnd。 */
    @Override
    public RFuture<Long> bitAndAsync(long startIndex, long endIndex) {
        return longOperationAsync(startIndex, endIndex, "AND");
    }

    /** 环形缓冲区 bitOr 操作。 */
    @Override
    public Long bitOr() {
        return get(bitOrAsync());
    }

    /** 异步执行 bitOr。 */
    @Override
    public RFuture<Long> bitOrAsync() {
        return longAggregateAllAsync("OR");
    }

    /** 环形缓冲区 bitOr 操作。 */
    @Override
    public Long bitOr(long startIndex, long endIndex) {
        return get(bitOrAsync(startIndex, endIndex));
    }

    /** 异步执行 bitOr。 */
    @Override
    public RFuture<Long> bitOrAsync(long startIndex, long endIndex) {
        return longOperationAsync(startIndex, endIndex, "OR");
    }

    /** 环形缓冲区 bitXor 操作。 */
    @Override
    public Long bitXor() {
        return get(bitXorAsync());
    }

    /** 异步执行 bitXor。 */
    @Override
    public RFuture<Long> bitXorAsync() {
        return longAggregateAllAsync("XOR");
    }

    /** 环形缓冲区 bitXor 操作。 */
    @Override
    public Long bitXor(long startIndex, long endIndex) {
        return get(bitXorAsync(startIndex, endIndex));
    }

    /** 异步执行 bitXor。 */
    @Override
    public RFuture<Long> bitXorAsync(long startIndex, long endIndex) {
        return longOperationAsync(startIndex, endIndex, "XOR");
    }

    /** 异步执行 longAggregateAll。 */
    private RFuture<Long> longAggregateAllAsync(String operation) {
        return commandExecutor.evalReadAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.EVAL_LONG,
                "local len = redis.call('exists', KEYS[1]) == 1 and redis.call('ARLEN', KEYS[1]) or 0; "
              + "if len == 0 then "
                  + "return nil; "
              + "end; "
              + "return redis.call('AROP', KEYS[1], 0, len - 1, ARGV[1]); ",
             Arrays.asList(getRawName()), operation);
    }

    /** 异步执行 longOperation。 */
    private RFuture<Long> longOperationAsync(long startIndex, long endIndex, String operation) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE,
                RedisCommands.AROP_LONG, getRawName(), startIndex, endIndex, operation);
    }

    /** 异步执行 aggregateAll。 */
    private RFuture<Double> aggregateAllAsync(String operation) {
        return commandExecutor.evalReadAsync(getRawName(), StringCodec.INSTANCE, EVAL_DOUBLE,
                "local len = redis.call('exists', KEYS[1]) == 1 and redis.call('ARLEN', KEYS[1]) or 0; "
              + "if len == 0 then "
                  + "return false; "
              + "end; "
              + "local r = redis.call('AROP', KEYS[1], 0, len - 1, ARGV[1]); "
              + "if not r then "
                  + "return false; "
              + "end; "
              + "return tostring(r); ",
             Arrays.asList(getRawName()), operation);
    }

    /** 异步执行 doubleOperation。 */
    private RFuture<Double> doubleOperationAsync(long startIndex, long endIndex, String operation) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE,
                RedisCommands.AROP_DOUBLE, getRawName(), startIndex, endIndex, operation);
    }

    /** 环形缓冲区 checkIndex 操作。 */
    private void checkIndex(long index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index must be non-negative");
        }
    }

    /** 环形缓冲区 checkCapacity 操作。 */
    private void checkCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {
        return super.expireAsync(timeToLive, timeUnit, param, getRawName(), settingsName);
    }

    /** 异步执行 expireAt。 */
    @Override
    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
        return super.expireAtAsync(timestamp, param, getRawName(), settingsName);
    }

    /** 异步清除 TTL。 */
    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return clearExpireAsync(getRawName(), settingsName);
    }

    /** 异步 JSON 删除。 */
    @Override
    public RFuture<Boolean> deleteAsync() {
        return super.deleteAsync(getRawName(), settingsName);
    }

}
