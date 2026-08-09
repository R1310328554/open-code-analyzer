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
import org.redisson.api.listener.ScoredSortedSetAddListener;
import org.redisson.api.listener.ScoredSortedSetDiffStoreListener;
import org.redisson.api.listener.ScoredSortedSetIncrListener;
import org.redisson.api.listener.ScoredSortedSetInterStoreListener;
import org.redisson.api.listener.ScoredSortedSetRemoveListener;
import org.redisson.api.listener.ScoredSortedSetUnionStoreListener;
import org.redisson.api.listener.TrackingListener;
import org.redisson.api.mapreduce.RCollectionMapReduce;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.*;
import org.redisson.client.protocol.RankedEntry;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.iterator.RedissonBaseIterator;
import org.redisson.mapreduce.RedissonCollectionMapReduce;
import org.redisson.misc.CompletableFutureWrapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Redis 有序集合 {@link RScoredSortedSet}（ZSET）实现。
 * <p>封装 ZADD/ZREM、ZRANGE/ZREVRANGE、ZSCAN、ZUNION/ZINTER
 * 及按分数/排名范围查询、MapReduce 等操作。
 *
 * @author Nikita Koksharov
 * @param <V> 成员类型
 */
public class RedissonScoredSortedSet<V> extends RedissonExpirable implements RScoredSortedSet<V> {

    private RedissonClient redisson;
    
    public RedissonScoredSortedSet(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(commandExecutor, name);
        this.redisson = redisson;
    }

    public RedissonScoredSortedSet(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(codec, commandExecutor, name);
        this.redisson = redisson;
    }

    /** 创建 MapReduce 任务入口。 */
    @Override
    public <KOut, VOut> RCollectionMapReduce<V, KOut, VOut> mapReduce() {
        return new RedissonCollectionMapReduce<V, KOut, VOut>(this, redisson, commandExecutor);
    }

    /** 一次性读取全部成员。 */
    @Override
    public Collection<V> readAll() {
        return get(readAllAsync());
    }
    
    /** 异步读取全部成员。 */
    @Override
    public RFuture<Collection<V>> readAllAsync() {
        return valueRangeAsync(0, -1);
    }
    
    /** 移除并返回分数最小的成员。 */
    @Override
    public V pollFirst() {
        return get(pollFirstAsync());
    }

    /** 移除并返回分数最大的成员。 */
    @Override
    public V pollLast() {
        return get(pollLastAsync());
    }

    /** 移除并返回分数最小的成员。 */
    @Override
    public Collection<V> pollFirst(int count) {
        return get(pollFirstAsync(count));
    }
    
    /** 移除并返回分数最大的成员。 */
    @Override
    public Collection<V> pollLast(int count) {
        return get(pollLastAsync(count));
    }
    
    /** 异步执行 pollFirst。 */
    @Override
    public RFuture<Collection<V>> pollFirstAsync(int count) {
        if (count <= 0) {
             return new CompletableFutureWrapper<>(Collections.<V>emptyList());
        }

        return poll(0, count-1, RedisCommands.EVAL_LIST);
    }
    
    /** 异步执行 pollLast。 */
    @Override
    public RFuture<Collection<V>> pollLastAsync(int count) {
        if (count <= 0) {
            return new CompletableFutureWrapper<>(Collections.<V>emptyList());
        }
        return poll(-count, -1, RedisCommands.EVAL_LIST);
    }
    
    /** 异步执行 pollFirst。 */
    @Override
    public RFuture<V> pollFirstAsync() {
        return poll(0, 0, RedisCommands.EVAL_FIRST_LIST);
    }

    /** 异步执行 pollLast。 */
    @Override
    public RFuture<V> pollLastAsync() {
        return poll(-1, -1, RedisCommands.EVAL_FIRST_LIST);
    }

    /** 移除并返回队首/最高优先级元素。 */
    private <T> RFuture<T> poll(int from, int to, RedisCommand<?> command) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, command,
                "local v = redis.call('zrange', KEYS[1], ARGV[1], ARGV[2]); "
                + "if #v > 0 then "
                    + "redis.call('zremrangebyrank', KEYS[1], ARGV[1], ARGV[2]); "
                    + "return v; "
                + "end "
                + "return v;",
                Collections.<Object>singletonList(name), from, to);
    }

    /** ZSet pollEntries 操作。 */
    private <T> RFuture<T> pollEntries(int from, int to, RedisCommand<?> command) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, command,
                "local v = redis.call('zrange', KEYS[1], ARGV[1], ARGV[2], 'WITHSCORES'); "
                + "if #v > 0 then "
                    + "redis.call('zremrangebyrank', KEYS[1], ARGV[1], ARGV[2]); "
                    + "return v; "
                + "end "
                + "return v;",
                Collections.singletonList(name), from, to);
    }


    /** ZSet pollFirstEntry 操作。 */
    @Override
    public ScoredEntry<V> pollFirstEntry() {
        return get(pollFirstEntryAsync());
    }

    /** ZSet pollLastEntry 操作。 */
    @Override
    public ScoredEntry<V> pollLastEntry() {
        return get(pollLastEntryAsync());
    }

    /** 异步执行 pollFirstEntry。 */
    @Override
    public RFuture<ScoredEntry<V>> pollFirstEntryAsync() {
        return pollEntry(0, 0, RedisCommands.EVAL_FIRST_LIST_ENTRY);
    }

    /** 异步执行 pollLastEntry。 */
    @Override
    public RFuture<ScoredEntry<V>> pollLastEntryAsync() {
        return pollEntry(-1, -1, RedisCommands.EVAL_FIRST_LIST_ENTRY);
    }

    /** ZSet pollFirstEntries 操作。 */
    @Override
    public List<ScoredEntry<V>> pollFirstEntries(int count) {
        return get(pollFirstEntriesAsync(count));
    }

    /** ZSet pollLastEntries 操作。 */
    @Override
    public List<ScoredEntry<V>> pollLastEntries(int count) {
        return get(pollLastEntriesAsync(count));
    }

    /** 异步执行 pollFirstEntries。 */
    @Override
    public RFuture<List<ScoredEntry<V>>> pollFirstEntriesAsync(int count) {
        if (count <= 0) {
            return new CompletableFutureWrapper<>(Collections.<ScoredEntry<V>>emptyList());
        }

        return pollEntries(0, count-1, RedisCommands.EVAL_LIST_ENTRY);
    }

    /** 异步执行 pollLastEntries。 */
    @Override
    public RFuture<List<ScoredEntry<V>>> pollLastEntriesAsync(int count) {
        if (count <= 0) {
            return new CompletableFutureWrapper<>(Collections.<ScoredEntry<V>>emptyList());
        }
        return pollEntries(-count, -1, RedisCommands.EVAL_LIST_ENTRY);
    }

    /** ZSet pollEntry 操作。 */
    private <T> RFuture<T> pollEntry(int from, int to, RedisCommand<?> command) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, command,
                "local v = redis.call('zrange', KEYS[1], ARGV[1], ARGV[2], 'withscores'); "
                    + "if #v > 0 then "
                        + "redis.call('zremrangebyrank', KEYS[1], ARGV[1], ARGV[2]); "
                        + "return v; "
                    + "end "
                    + "return v;",
                Collections.singletonList(getRawName()), from, to);
    }

    /** ZSet pollFirstEntries 操作。 */
    @Override
    public List<ScoredEntry<V>> pollFirstEntries(Duration duration, int count) {
        return get(pollFirstEntriesAsync(duration, count));
    }

    /** ZSet pollLastEntries 操作。 */
    @Override
    public List<ScoredEntry<V>> pollLastEntries(Duration duration, int count) {
        return get(pollLastEntriesAsync(duration, count));
    }

    /** 异步执行 pollFirstEntries。 */
    @Override
    public RFuture<List<ScoredEntry<V>>> pollFirstEntriesAsync(Duration duration, int count) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZMPOP_ENTRIES,
                duration.getSeconds(), 1, getRawName(), "MIN", "COUNT", count);
    }

    /** 异步执行 pollLastEntries。 */
    @Override
    public RFuture<List<ScoredEntry<V>>> pollLastEntriesAsync(Duration duration, int count) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZMPOP_ENTRIES,
                duration.getSeconds(), 1, getRawName(), "MAX", "COUNT", count);
    }

    /** 移除并返回分数最小的成员。 */
    @Override
    public V pollFirst(long timeout, TimeUnit unit) {
        return get(pollFirstAsync(timeout, unit));
    }

    /** 移除并返回分数最小的成员。 */
    @Override
    public V pollFirst(Duration duration) {
        return get(pollFirstAsync(duration));
    }

    /** 异步执行 pollFirst。 */
    @Override
    public RFuture<V> pollFirstAsync(long timeout, TimeUnit unit) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZPOPMIN_VALUE, getRawName(), toSeconds(timeout, unit));
    }

    /** 异步执行 pollFirst。 */
    @Override
    public RFuture<V> pollFirstAsync(Duration duration) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZPOPMIN_VALUE, getRawName(), duration.getSeconds());
    }

    /** ZSet pollFirstFromAny 操作。 */
    @Override
    public V pollFirstFromAny(long timeout, TimeUnit unit, String... queueNames) {
        return get(pollFirstFromAnyAsync(timeout, unit, queueNames));
    }

    /** 异步执行 pollFirstFromAny。 */
    @Override
    public RFuture<V> pollFirstFromAnyAsync(long timeout, TimeUnit unit, String... queueNames) {
        return commandExecutor.pollFromAnyAsync(getRawName(), codec, RedisCommands.BZPOPMIN_VALUE, toSeconds(timeout, unit), queueNames);
    }

    /** ZSet pollFirstFromAny 操作。 */
    @Override
    public List<V> pollFirstFromAny(Duration duration, int count, String... queueNames) {
        return get(pollFirstFromAnyAsync(duration, count, queueNames));
    }

    /** 异步执行 pollFirstFromAny。 */
    @Override
    public RFuture<List<V>> pollFirstFromAnyAsync(Duration duration, int count, String... queueNames) {
        List<Object> params = new ArrayList<>();
        params.add(duration.getSeconds());
        params.add(queueNames.length + 1);
        params.add(getRawName());
        params.addAll(map(queueNames));
        params.add("MIN");
        params.add("COUNT");
        params.add(count);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZMPOP_SINGLE_LIST, params.toArray());
    }

    /** ZSet pollFirstFromAny 操作。 */
    @Override
    public List<V> pollFirstFromAny(int count, String... queueNames) {
        return get(pollFirstFromAnyAsync(count, queueNames));
    }

    /** 异步执行 pollFirstFromAny。 */
    @Override
    public RFuture<List<V>> pollFirstFromAnyAsync(int count, String... queueNames) {
        List<Object> params = new ArrayList<>();
        params.add(queueNames.length + 1);
        params.add(getRawName());
        params.addAll(map(queueNames));
        params.add("MIN");
        params.add("COUNT");
        params.add(count);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZMPOP_VALUES, params.toArray());
    }

    /** ZSet pollFirstEntriesFromAny 操作。 */
    @Override
    public Map<String, Map<V, Double>> pollFirstEntriesFromAny(int count, String... queueNames) {
        return get(pollFirstEntriesFromAnyAsync(count, queueNames));
    }

    /** 异步执行 pollFirstEntriesFromAny。 */
    @Override
    public RFuture<Map<String, Map<V, Double>>> pollFirstEntriesFromAnyAsync(int count, String... queueNames) {
        List<Object> params = new ArrayList<>();
        params.add(queueNames.length + 1);
        params.add(getRawName());
        params.addAll(map(queueNames));
        params.add("MIN");
        params.add("COUNT");
        params.add(count);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZMPOP, params.toArray());
    }

    /** ZSet pollFirstEntriesFromAny 操作。 */
    @Override
    public Map<String, Map<V, Double>> pollFirstEntriesFromAny(Duration duration, int count, String... queueNames) {
        return get(pollFirstEntriesFromAnyAsync(duration, count, queueNames));
    }

    /** 异步执行 pollFirstEntriesFromAny。 */
    @Override
    public RFuture<Map<String, Map<V, Double>>> pollFirstEntriesFromAnyAsync(Duration duration, int count, String... queueNames) {
        List<Object> params = new ArrayList<>();
        params.add(duration.getSeconds());
        params.add(queueNames.length + 1);
        params.add(getRawName());
        params.addAll(map(queueNames));
        params.add("MIN");
        params.add("COUNT");
        params.add(count);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZMPOP, params.toArray());
    }

    /** ZSet pollLastFromAny 操作。 */
    @Override
    public V pollLastFromAny(long timeout, TimeUnit unit, String... queueNames) {
        return get(pollLastFromAnyAsync(timeout, unit, queueNames));
    }

    /** 异步执行 pollLastFromAny。 */
    @Override
    public RFuture<V> pollLastFromAnyAsync(long timeout, TimeUnit unit, String... queueNames) {
        return commandExecutor.pollFromAnyAsync(getRawName(), codec, RedisCommands.BZPOPMAX_VALUE, toSeconds(timeout, unit), queueNames);
    }

    /** ZSet pollLastFromAny 操作。 */
    @Override
    public List<V> pollLastFromAny(Duration duration, int count, String... queueNames) {
        return get(pollLastFromAnyAsync(duration, count, queueNames));
    }

    /** 异步执行 pollLastFromAny。 */
    @Override
    public RFuture<List<V>> pollLastFromAnyAsync(Duration duration, int count, String... queueNames) {
        List<Object> params = new ArrayList<>();
        params.add(duration.getSeconds());
        params.add(queueNames.length + 1);
        params.add(getRawName());
        params.addAll(map(queueNames));
        params.add("MAX");
        params.add("COUNT");
        params.add(count);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZMPOP_SINGLE_LIST, params.toArray());
    }

    /** ZSet pollLastFromAny 操作。 */
    @Override
    public List<V> pollLastFromAny(int count, String... queueNames) {
        return get(pollFirstFromAnyAsync(count, queueNames));
    }

    /** 异步执行 pollLastFromAny。 */
    @Override
    public RFuture<List<V>> pollLastFromAnyAsync(int count, String... queueNames) {
        List<Object> params = new ArrayList<>();
        params.add(queueNames.length + 1);
        params.add(getRawName());
        params.addAll(map(queueNames));
        params.add("MAX");
        params.add("COUNT");
        params.add(count);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZMPOP_VALUES, params.toArray());
    }

    /** ZSet pollLastEntriesFromAny 操作。 */
    @Override
    public Map<String, Map<V, Double>> pollLastEntriesFromAny(int count, String... queueNames) {
        return get(pollLastEntriesFromAnyAsync(count, queueNames));
    }

    /** 异步执行 pollLastEntriesFromAny。 */
    @Override
    public RFuture<Map<String, Map<V, Double>>> pollLastEntriesFromAnyAsync(int count, String... queueNames) {
        List<Object> params = new ArrayList<>();
        params.add(queueNames.length + 1);
        params.add(getRawName());
        params.addAll(map(queueNames));
        params.add("MAX");
        params.add("COUNT");
        params.add(count);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZMPOP, params.toArray());
    }

    /** ZSet pollLastEntriesFromAny 操作。 */
    @Override
    public Map<String, Map<V, Double>> pollLastEntriesFromAny(Duration duration, int count, String... queueNames) {
        return get(pollLastEntriesFromAnyAsync(duration, count, queueNames));
    }

    /** 异步执行 pollLastEntriesFromAny。 */
    @Override
    public RFuture<Map<String, Map<V, Double>>> pollLastEntriesFromAnyAsync(Duration duration, int count, String... queueNames) {
        List<Object> params = new ArrayList<>();
        params.add(duration.getSeconds());
        params.add(queueNames.length + 1);
        params.add(getRawName());
        params.addAll(map(queueNames));
        params.add("MAX");
        params.add("COUNT");
        params.add(count);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZMPOP, params.toArray());
    }

    /** 移除并返回分数最大的成员。 */
    @Override
    public V pollLast(long timeout, TimeUnit unit) {
        return get(pollLastAsync(timeout, unit));
    }

    /** 异步执行 pollLast。 */
    @Override
    public RFuture<V> pollLastAsync(long timeout, TimeUnit unit) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZPOPMAX_VALUE, getRawName(), toSeconds(timeout, unit));
    }

    /** 移除并返回分数最小的成员。 */
    @Override
    public List<V> pollFirst(Duration duration, int count) {
        return get(pollFirstAsync(duration, count));
    }

    /** 异步执行 pollFirst。 */
    @Override
    public RFuture<List<V>> pollFirstAsync(Duration duration, int count) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZMPOP_SINGLE_LIST,
                                    duration.getSeconds(), 1, getRawName(), "MIN", "COUNT", count);
    }

    /** 移除并返回分数最大的成员。 */
    @Override
    public List<V> pollLast(Duration duration, int count) {
        return get(pollLastAsync(duration, count));
    }

    /** 异步执行 pollLast。 */
    @Override
    public RFuture<List<V>> pollLastAsync(Duration duration, int count) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZMPOP_SINGLE_LIST,
                duration.getSeconds(), 1, getRawName(), "MAX", "COUNT", count);
    }

    /** 移除并返回分数最大的成员。 */
    @Override
    public V pollLast(Duration duration) {
        return get(pollLastAsync(duration));
    }

    /** 异步执行 pollLast。 */
    @Override
    public RFuture<V> pollLastAsync(Duration duration) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.BZPOPMAX_VALUE, getRawName(), duration.getSeconds());
    }

    /** 随机返回一个成员（不移除）。 */
    @Override
    public V random() {
        return get(randomAsync());
    }

    /** 随机返回一个成员（不移除）。 */
    @Override
    public Collection<V> random(int count) {
        return get(randomAsync(count));
    }

    /** 异步随机返回成员。 */
    @Override
    public RFuture<V> randomAsync() {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZRANDMEMBER_SINGLE, getRawName());
    }

    /** 异步随机返回成员。 */
    @Override
    public RFuture<Collection<V>> randomAsync(int count) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZRANDMEMBER, getRawName(), count);
    }

    /** ZSet randomEntries 操作。 */
    @Override
    public Map<V, Double> randomEntries(int count) {
        return get(randomEntriesAsync(count));
    }

    /** 异步执行 randomEntries。 */
    @Override
    public RFuture<Map<V, Double>> randomEntriesAsync(int count) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZRANDMEMBER_ENTRIES, getRawName(), count, "WITHSCORES");
    }

    /** 添加元素/成员。 */
    @Override
    public boolean add(double score, V object) {
        return get(addAsync(score, object));
    }

    /** addAndGetRank：添加操作。 */
    @Override
    public Integer addAndGetRank(double score, V object) {
        return get(addAndGetRankAsync(score, object));
    }

    /** 异步执行 addAndGetRank。 */
    @Override
    public RFuture<Integer> addAndGetRankAsync(double score, V object) {
        String name = getRawName(object);
        return commandExecutor.evalWriteAsync(name, LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                "redis.call('zadd', KEYS[1], ARGV[1], ARGV[2]);" +
                "return redis.call('zrank', KEYS[1], ARGV[2]); ",
                Collections.<Object>singletonList(name), new BigDecimal(score).toPlainString(), encode(object));
    }

    /** addAndGetRevRank：添加操作。 */
    @Override
    public Integer addAndGetRevRank(double score, V object) {
        return get(addAndGetRevRankAsync(score, object));
    }

    /** addAndGetRevRank：添加操作。 */
    @Override
    public List<Integer> addAndGetRevRank(Map<? extends V, Double> map) {
        return get(addAndGetRevRankAsync(map));
    }

    /** 异步执行 addAndGetRevRank。 */
    @Override
    public RFuture<Integer> addAndGetRevRankAsync(double score, V object) {
        String name = getRawName(object);
        return commandExecutor.evalWriteAsync(name, LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                "redis.call('zadd', KEYS[1], ARGV[1], ARGV[2]);" +
                "return redis.call('zrevrank', KEYS[1], ARGV[2]); ",
                Collections.<Object>singletonList(name), new BigDecimal(score).toPlainString(), encode(object));
    }

    /** 异步执行 addAndGetRevRank。 */
    @Override
    public RFuture<List<Integer>> addAndGetRevRankAsync(Map<? extends V, Double> map) {
        List<Object> params = new ArrayList<>(map.size() * 2);
        for (java.util.Map.Entry<? extends V, Double> t : map.entrySet()) {
            if (t.getKey() == null) {
                throw new NullPointerException("map key can't be null");
            }
            if (t.getValue() == null) {
                throw new NullPointerException("map value can't be null");
            }
            encode(params, t.getKey());
            params.add(BigDecimal.valueOf(t.getValue()).toPlainString());
        }

        return commandExecutor.evalWriteAsync((String) null, IntegerCodec.INSTANCE, RedisCommands.EVAL_INT_LIST,
                    "local r = {} " +
                    "for i, v in ipairs(ARGV) do " +
                        "if i % 2 == 0 then " +
                            "redis.call('zadd', KEYS[1], ARGV[i], ARGV[i-1]); " +
                        "end; " +
                    "end;" +
                    "for i, v in ipairs(ARGV) do " +
                        "if i % 2 == 0 then " +
                            "r[#r+1] = redis.call('zrevrank', KEYS[1], ARGV[i-1]); " +
                        "end; " +
                    "end;" +
                    "return r;",
                Collections.singletonList(getRawName()), params.toArray());
    }

    /** ZSet tryAdd 操作。 */
    @Override
    public boolean tryAdd(double score, V object) {
        return get(tryAddAsync(score, object));
    }

    /** addIfExists：添加操作。 */
    @Override
    public boolean addIfExists(double score, V object) {
        return get(addIfExistsAsync(score, object));
    }

    /** 异步执行 addIfExists。 */
    @Override
    public RFuture<Boolean> addIfExistsAsync(double score, V object) {
        String name = getRawName(object);
        return commandExecutor.writeAsync(name, codec, RedisCommands.ZADD_BOOL, name, "XX", "CH", BigDecimal.valueOf(score).toPlainString(), encode(object));
    }

    /** addIfLess：添加操作。 */
    @Override
    public boolean addIfLess(double score, V object) {
        return get(addIfLessAsync(score, object));
    }

    /** addIfGreater：添加操作。 */
    @Override
    public boolean addIfGreater(double score, V object) {
        return get(addIfGreaterAsync(score, object));
    }

    /** 异步执行 addIfLess。 */
    @Override
    public RFuture<Boolean> addIfLessAsync(double score, V object) {
        String name = getRawName(object);
        return commandExecutor.writeAsync(name, codec, RedisCommands.ZADD_BOOL,
                name, "LT", "CH", BigDecimal.valueOf(score).toPlainString(), encode(object));
    }

    /** 异步执行 addIfGreater。 */
    @Override
    public RFuture<Boolean> addIfGreaterAsync(double score, V object) {
        String name = getRawName(object);
        return commandExecutor.writeAsync(name, codec, RedisCommands.ZADD_BOOL,
                name, "GT", "CH", BigDecimal.valueOf(score).toPlainString(), encode(object));
    }

    /** ZSet first 操作。 */
    @Override
    public V first() {
        return get(firstAsync());
    }

    /** 异步执行 first。 */
    @Override
    public RFuture<V> firstAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGE_SINGLE, getRawName(), 0, 0);
    }

    /** ZSet firstEntry 操作。 */
    @Override
    public ScoredEntry<V> firstEntry() {
        return get(firstEntryAsync());
    }

    /** 异步执行 firstEntry。 */
    @Override
    public RFuture<ScoredEntry<V>> firstEntryAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGE_SINGLE_ENTRY, getRawName(), 0, 0, "WITHSCORES");
    }

    /** ZSet last 操作。 */
    @Override
    public V last() {
        return get(lastAsync());
    }

    /** 异步执行 last。 */
    @Override
    public RFuture<V> lastAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGE_SINGLE, getRawName(), -1, -1);
    }

    /** ZSet lastEntry 操作。 */
    @Override
    public ScoredEntry<V> lastEntry() {
        return get(lastEntryAsync());
    }

    /** 异步执行 lastEntry。 */
    @Override
    public RFuture<ScoredEntry<V>> lastEntryAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGE_SINGLE_ENTRY, getRawName(), -1, -1, "WITHSCORES");
    }

    /** ZSet firstScore 操作。 */
    @Override
    public Double firstScore() {
        return get(firstScoreAsync());
    }

    /** 异步执行 firstScore。 */
    @Override
    public RFuture<Double> firstScoreAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGE_SINGLE_SCORE, getRawName(), 0, 0, "WITHSCORES");
    }

    /** ZSet lastScore 操作。 */
    @Override
    public Double lastScore() {
        return get(lastScoreAsync());
    }

    /** 异步执行 lastScore。 */
    @Override
    public RFuture<Double> lastScoreAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGE_SINGLE_SCORE, getRawName(), -1, -1, "WITHSCORES");
    }


    /** 异步添加 ZSet 成员。 */
    @Override
    public RFuture<Boolean> addAsync(double score, V object) {
        String name = getRawName(object);
        return commandExecutor.writeAsync(name, codec, RedisCommands.ZADD_BOOL, name, BigDecimal.valueOf(score).toPlainString(), encode(object));
    }

    /** 批量添加元素。 */
    @Override
    public int addAll(Map<V, Double> objects) {
        return get(addAllAsync(objects));
    }

    /** 异步执行 addAll。 */
    @Override
    public RFuture<Integer> addAllAsync(Map<V, Double> objects) {
        if (objects.isEmpty()) {
            return new CompletableFutureWrapper<>(0);
        }
        List<Object> params = new ArrayList<>(objects.size() * 2 + 1);
        params.add(getRawName());
        for (Entry<V, Double> entry : objects.entrySet()) {
            params.add(BigDecimal.valueOf(entry.getValue()).toPlainString());
            encode(params, entry.getKey());
        }

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZADD_INT, params.toArray());
    }

    /** addAllIfAbsent：添加操作。 */
    @Override
    public int addAllIfAbsent(Map<V, Double> objects) {
        return get(addAllIfAbsentAsync(objects));
    }

    /** 异步执行 addAllIfAbsent。 */
    @Override
    public RFuture<Integer> addAllIfAbsentAsync(Map<V, Double> objects) {
        if (objects.isEmpty()) {
            return new CompletableFutureWrapper<>(0);
        }
        List<Object> params = new ArrayList<>(objects.size()*2+1);
        params.add(getRawName());
        params.add("NX");
        for (Entry<V, Double> entry : objects.entrySet()) {
            params.add(BigDecimal.valueOf(entry.getValue()).toPlainString());
            encode(params, entry.getKey());
        }

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZADD_INT, params.toArray());
    }

    /** addAllIfExist：添加操作。 */
    @Override
    public int addAllIfExist(Map<V, Double> objects) {
        return get(addAllIfExistAsync(objects));
    }

    /** 异步执行 addAllIfExist。 */
    @Override
    public RFuture<Integer> addAllIfExistAsync(Map<V, Double> objects) {
        if (objects.isEmpty()) {
            return new CompletableFutureWrapper<>(0);
        }
        List<Object> params = new ArrayList<>(objects.size()*2+3);
        params.add(getRawName());
        params.add("XX");
        params.add("CH");
        for (Entry<V, Double> entry : objects.entrySet()) {
            params.add(BigDecimal.valueOf(entry.getValue()).toPlainString());
            encode(params, entry.getKey());
        }

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZADD_INT, params.toArray());
    }

    /** addAllIfGreater：添加操作。 */
    @Override
    public int addAllIfGreater(Map<V, Double> objects) {
        return get(addAllIfGreaterAsync(objects));
    }

    /** 异步执行 addAllIfGreater。 */
    @Override
    public RFuture<Integer> addAllIfGreaterAsync(Map<V, Double> objects) {
        if (objects.isEmpty()) {
            return new CompletableFutureWrapper<>(0);
        }
        List<Object> params = new ArrayList<>(objects.size()*2+1);
        params.add(getRawName());
        params.add("GT");
        params.add("CH");
        for (Entry<V, Double> entry : objects.entrySet()) {
            params.add(BigDecimal.valueOf(entry.getValue()).toPlainString());
            encode(params, entry.getKey());
        }

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZADD_INT, params.toArray());
    }

    /** addAllIfLess：添加操作。 */
    @Override
    public int addAllIfLess(Map<V, Double> objects) {
        return get(addAllIfLessAsync(objects));
    }

    /** 异步执行 addAllIfLess。 */
    @Override
    public RFuture<Integer> addAllIfLessAsync(Map<V, Double> objects) {
        if (objects.isEmpty()) {
            return new CompletableFutureWrapper<>(0);
        }
        List<Object> params = new ArrayList<>(objects.size()*2+1);
        params.add(getRawName());
        params.add("LT");
        params.add("CH");
        for (Entry<V, Double> entry : objects.entrySet()) {
            params.add(BigDecimal.valueOf(entry.getValue()).toPlainString());
            encode(params, entry.getKey());
        }

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZADD_INT, params.toArray());
    }

    /** 异步执行 tryAdd。 */
    @Override
    public RFuture<Boolean> tryAddAsync(double score, V object) {
        String name = getRawName(object);
        return commandExecutor.writeAsync(name, codec, RedisCommands.ZADD_BOOL, name, "NX", BigDecimal.valueOf(score).toPlainString(), encode(object));
    }

    /** addIfAbsent：添加操作。 */
    @Override
    public boolean addIfAbsent(double score, V object) {
        return get(addIfAbsentAsync(score, object));
    }

    /** 异步执行 addIfAbsent。 */
    @Override
    public RFuture<Boolean> addIfAbsentAsync(double score, V object) {
        String name = getRawName(object);
        return commandExecutor.writeAsync(name, codec, RedisCommands.ZADD_BOOL, name, "NX", BigDecimal.valueOf(score).toPlainString(), encode(object));
    }

    /** 移除元素。 */
    @Override
    public boolean remove(Object object) {
        return get(removeAsync(object));
    }

    /** 按排名范围删除成员。 */
    @Override
    public int removeRangeByRank(int startIndex, int endIndex) {
        return get(removeRangeByRankAsync(startIndex, endIndex));
    }

    /** 异步执行 removeRangeByRank。 */
    @Override
    public RFuture<Integer> removeRangeByRankAsync(int startIndex, int endIndex) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZREMRANGEBYRANK, getRawName(), startIndex, endIndex);
    }

    /** 按分数范围删除成员。 */
    @Override
    public int removeRangeByScore(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        return get(removeRangeByScoreAsync(startScore, startScoreInclusive, endScore, endScoreInclusive));
    }

    /** 异步执行 removeRangeByScore。 */
    @Override
    public RFuture<Integer> removeRangeByScoreAsync(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZREMRANGEBYSCORE, getRawName(), startValue, endValue);
    }

    /** ZSet value 操作。 */
    protected final String value(double score, boolean inclusive) {
        StringBuilder element = new StringBuilder();
        if (!inclusive) {
            element.append("(");
        }
        if (Double.isInfinite(score)) {
            if (score > 0) {
                element.append("+inf");
            } else {
                element.append("-inf");
            }
        } else {
            element.append(BigDecimal.valueOf(score).toPlainString());
        }
        return element.toString();
    }

    /** 清空全部元素。 */
    @Override
    public void clear() {
        delete();
    }

    /** 异步移除 ZSet 成员。 */
    @Override
    public RFuture<Boolean> removeAsync(Object object) {
        String name = getRawName(object);
        return commandExecutor.writeAsync(name, codec, RedisCommands.ZREM, name, encode(object));
    }

    /** ZSet replace 操作。 */
    @Override
    public boolean replace(V oldValue, V newValue) {
        return get(replaceAsync(oldValue, newValue));
    }

    /** 异步执行 replace。 */
    @Override
    public RFuture<Boolean> replaceAsync(V oldObject, V newObject) {
        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                        "local score = redis.call('zscore', KEYS[1], ARGV[1]);" +
                              "if score ~= false then " +
                                   "redis.call('zrem', KEYS[1], ARGV[1]);" +
                                   "redis.call('zadd', KEYS[1], score, ARGV[2]);" +
                                   "return 1;" +
                              "end;" +
                              "return 0;",
                Collections.singletonList(getRawName()), encode(oldObject), encode(newObject));
    }

    /** 集合是否为空。 */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /** 返回元素数量。 */
    @Override
    public int size() {
        return get(sizeAsync());
    }

    /** 异步返回元素数量。 */
    @Override
    public RFuture<Integer> sizeAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZCARD_INT, getRawName());
    }

    /** 是否包含指定元素。 */
    @Override
    public boolean contains(Object object) {
        return get(containsAsync(object));
    }

    /** 异步执行 contains。 */
    @Override
    public RFuture<Boolean> containsAsync(Object o) {
        String name = getRawName(o);
        return commandExecutor.readAsync(name, StringCodec.INSTANCE, RedisCommands.ZSCORE_CONTAINS, name, encode(o));
    }

    /** 获取 Score。 */
    @Override
    public Double getScore(V o) {
        return get(getScoreAsync(o));
    }

    /** 获取 Score。 */
    @Override
    public List<Double> getScore(List<V> keys) {
        return get(getScoreAsync(keys));
    }

    /** 异步获取 Score 或执行 Score 操作。 */
    @Override
    public RFuture<Double> getScoreAsync(V o) {
        String name = getRawName(o);
        return commandExecutor.readAsync(name, StringCodec.INSTANCE, RedisCommands.ZSCORE, name, encode(o));
    }

    /** 异步获取 Score 或执行 Score 操作。 */
    @Override
    public RFuture<List<Double>> getScoreAsync(Collection<V> elements) {
        return commandExecutor.evalReadAsync((String) null, DoubleCodec.INSTANCE, RedisCommands.EVAL_LIST,
                "local r = {} " +
                "for i, v in ipairs(ARGV) do " +
                    "r[#r+1] = redis.call('ZSCORE', KEYS[1], ARGV[i]); " +
                "end;" +
                "return r;",
                Collections.singletonList(getRawName()), encode(elements).toArray());
    }

    /** 返回成员升序排名（0 起）。 */
    @Override
    public Integer rank(V o) {
        return get(rankAsync(o));
    }

    /** 异步执行 rank。 */
    @Override
    public RFuture<Integer> rankAsync(V o) {
        String name = getRawName(o);
        return commandExecutor.readAsync(name, codec, RedisCommands.ZRANK_INT, name, encode(o));
    }

    /** ZSet rankEntry 操作。 */
    @Override
    public RankedEntry<V> rankEntry(V o) {
        return get(rankEntryAsync(o));
    }

    /** 异步执行 rankEntry。 */
    @Override
    public RFuture<RankedEntry<V>> rankEntryAsync(V o) {
        String name = getRawName(o);
        return commandExecutor.readAsync(name, codec, RedisCommands.ZRANK_ENTRY, name, encode(o), "WITHSCORE");
    }

    /** SSCAN 增量扫描迭代器。 */
    private ScanResult<Object> scanIterator(RedisClient client, String startPos, String pattern, int count) {
        RFuture<ScanResult<Object>> f = scanIteratorAsync(client, startPos, pattern, count);
        return get(f);
    }

    /** 异步 SSCAN 迭代器。 */
    public RFuture<ScanResult<Object>> scanIteratorAsync(RedisClient client, String startPos, String pattern, int count) {
        if (pattern == null) {
            RFuture<ScanResult<Object>> f = commandExecutor.readAsync(client, getRawName(), codec, RedisCommands.ZSCAN, getRawName(), startPos, "COUNT", count);
            return f;
        }
        RFuture<ScanResult<Object>> f = commandExecutor.readAsync(client, getRawName(), codec, RedisCommands.ZSCAN, getRawName(), startPos, "MATCH", pattern, "COUNT", count);
        return f;
    }

    /** 返回成员迭代器。 */
    @Override
    public Iterator<V> iterator() {
        return iterator(null, 10);
    }

    /** 返回成员迭代器。 */
    @Override
    public Iterator<V> iterator(String pattern) {
        return iterator(pattern, 10);
    }

    /** 返回成员迭代器。 */
    @Override
    public Iterator<V> iterator(int count) {
        return iterator(null, count);
    }

    /** 返回成员迭代器。 */
    @Override
    public Iterator<V> iterator(String pattern, int count) {
        return new RedissonBaseIterator<V>() {

            @Override
            protected ScanResult<Object> iterator(RedisClient client, String nextIterPos) {
                return scanIterator(client, nextIterPos, pattern, count);
            }

            @Override
            protected void remove(Object value) {
                RedissonScoredSortedSet.this.remove(value);
            }

        };
    }

    /** ZSet entryIterator 操作。 */
    public Iterator<ScoredEntry<V>> entryIterator() {
        return entryIterator(null, 10);
    }

    /** ZSet entryIterator 操作。 */
    @Override
    public Iterator<ScoredEntry<V>> entryIterator(String pattern) {
        return entryIterator(pattern, 10);
    }

    /** ZSet entryIterator 操作。 */
    @Override
    public Iterator<ScoredEntry<V>> entryIterator(int count) {
        return entryIterator(null, count);
    }

    /** ZSet entryIterator 操作。 */
    @Override
    public Iterator<ScoredEntry<V>> entryIterator(String pattern, int count) {
        return new RedissonBaseIterator<ScoredEntry<V>>() {

            @Override
            protected ScanResult<Object> iterator(RedisClient client, String nextIterPos) {
                return entryScanIterator(client, nextIterPos, pattern, count);
            }

            @Override
            protected void remove(Object value) {
                RedissonScoredSortedSet.this.remove(value);
            }

        };
    }

    /** ZSet entryScanIterator 操作。 */
    private ScanResult<Object> entryScanIterator(RedisClient client, String startPos, String pattern, int count) {
        RFuture<ScanResult<Object>> f = entryScanIteratorAsync(client, startPos, pattern, count);
        return get(f);
    }

    /** 异步执行 entryScanIterator。 */
    public RFuture<ScanResult<Object>> entryScanIteratorAsync(RedisClient client, String startPos, String pattern, int count) {
        if (pattern == null) {
            RFuture<ScanResult<Object>> f = commandExecutor.readAsync(client, getRawName(), codec, RedisCommands.ZSCAN_ENTRY, getRawName(), startPos, "COUNT", count);
            return f;
        }
        RFuture<ScanResult<Object>> f = commandExecutor.readAsync(client, getRawName(), codec, RedisCommands.ZSCAN_ENTRY, getRawName(), startPos, "MATCH", pattern, "COUNT", count);
        return f;
    }

    /** ZSet distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(String pattern) {
        String iteratorName = "__redisson_scored_sorted_set_cursor_{" + getRawName() + "}";
        return distributedIterator(iteratorName, pattern, 10);
    }

    /** ZSet distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(int count) {
        String iteratorName = "__redisson_scored_sorted_set_cursor_{" + getRawName() + "}";
        return distributedIterator(iteratorName, null, count);
    }

    /** ZSet distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(String iteratorName, String pattern, int count) {
        return new RedissonBaseIterator<V>() {

            @Override
            protected ScanResult<Object> iterator(RedisClient client, String nextIterPos) {
                return distributedScanIterator(iteratorName, pattern, count);
            }

            @Override
            protected void remove(Object value) {
                RedissonScoredSortedSet.this.remove((V) value);
            }
        };
    }

    /** ZSet distributedScanIterator 操作。 */
    private ScanResult<Object> distributedScanIterator(String iteratorName, String pattern, int count) {
        return get(distributedScanIteratorAsync(iteratorName, pattern, count));
    }

    /** 异步执行 distributedScanIterator。 */
    private RFuture<ScanResult<Object>> distributedScanIteratorAsync(String iteratorName, String pattern, int count) {
        List<Object> args = new ArrayList<>(2);
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
                    + "return {'0', {}}; "
                + "end;"
                + "local result; "
                + "if (#ARGV == 2) then "
                    + "result = redis.call('zscan', KEYS[1], cursor, 'match', ARGV[1], 'count', ARGV[2]); "
                + "else "
                    + "result = redis.call('zscan', KEYS[1], cursor, 'count', ARGV[1]); "
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
                        + "table.insert(res, result[2][i-1]); "
                    + "end; "
                + "end;"
                + "return {tostring(result[1]), res};",
                Arrays.asList(getRawName(), iteratorName), args.toArray());
    }

    /** ZSet toArray 操作。 */
    @Override
    public Object[] toArray() {
        List<Object> res = (List<Object>) get(valueRangeAsync(0, -1));
        return res.toArray();
    }

    /** ZSet toArray 操作。 */
    @Override
    public <T> T[] toArray(T[] a) {
        List<Object> res = (List<Object>) get(valueRangeAsync(0, -1));
        return res.toArray(a);
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

        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                            "for j = 1, #ARGV, 1 do "
                            + "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[j]) "
                            + "if expireDateScore == false then "
                                + "return 0;"
                            + "end; "
                        + "end; "
                       + "return 1; ",
                Collections.<Object>singletonList(getRawName()), encode(c).toArray());
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

        List<Object> params = new ArrayList<>(c.size() * 2);
        for (Object object : c) {
            params.add(0);
            encode(params, object);
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "redis.call('zadd', KEYS[2], unpack(ARGV)); "
                 + "local prevSize = redis.call('zcard', KEYS[1]); "
                 + "local size = redis.call('zinterstore', KEYS[1], 2, KEYS[1], KEYS[2], 'aggregate', 'sum');"
                 + "redis.call('del', KEYS[2]); "
                 + "return size ~= prevSize and 1 or 0; ",
             Arrays.asList(getRawName(), "redisson_temp__{" + getRawName() + "}"), params.toArray());
    }

    /** 为 ZSet 成员设置分数。 */
    @Override
    public Double addScore(V object, Number value) {
        return get(addScoreAsync(object, value));
    }

    /** 异步设置成员分数。 */
    @Override
    public RFuture<Double> addScoreAsync(V object, Number value) {
        String name = getRawName(object);
        return commandExecutor.writeAsync(name, DoubleCodec.INSTANCE, RedisCommands.ZINCRBY,
                                            name, new BigDecimal(value.toString()).toPlainString(), encode(object));
    }

    /** addScoreAndGetRank：添加操作。 */
    @Override
    public Integer addScoreAndGetRank(V object, Number value) {
        return get(addScoreAndGetRankAsync(object, value));
    }

    /** 异步执行 addScoreAndGetRank。 */
    @Override
    public RFuture<Integer> addScoreAndGetRankAsync(V object, Number value) {
        String name = getRawName(object);
        return commandExecutor.evalWriteAsync(name, LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                "redis.call('zincrby', KEYS[1], ARGV[1], ARGV[2]); "
               +"return redis.call('zrank', KEYS[1], ARGV[2]); ",
                Collections.singletonList(name), new BigDecimal(value.toString()).toPlainString(), encode(object));
    }

    /** addScoreAndGetRevRank：添加操作。 */
    @Override
    public Integer addScoreAndGetRevRank(V object, Number value) {
        return get(addScoreAndGetRevRankAsync(object, value));
    }

    /** 异步执行 addScoreAndGetRevRank。 */
    @Override
    public RFuture<Integer> addScoreAndGetRevRankAsync(V object, Number value) {
        String name = getRawName(object);
        return commandExecutor.evalWriteAsync(name, LongCodec.INSTANCE, RedisCommands.EVAL_INTEGER,
                "redis.call('zincrby', KEYS[1], ARGV[1], ARGV[2]); "
               +"return redis.call('zrevrank', KEYS[1], ARGV[2]); ",
                Collections.singletonList(name), new BigDecimal(value.toString()).toPlainString(), encode(object));
    }

    /** 按排名范围返回成员。 */
    @Override
    public Collection<V> valueRange(int startIndex, int endIndex) {
        return get(valueRangeAsync(startIndex, endIndex));
    }

    /** 异步执行 valueRange。 */
    @Override
    public RFuture<Collection<V>> valueRangeAsync(int startIndex, int endIndex) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGE, getRawName(), startIndex, endIndex);
    }

    /** 按排名降序范围返回成员。 */
    @Override
    public Collection<V> valueRangeReversed(int startIndex, int endIndex) {
        return get(valueRangeReversedAsync(startIndex, endIndex));
    }
    
    /** 异步执行 valueRangeReversed。 */
    @Override
    public RFuture<Collection<V>> valueRangeReversedAsync(int startIndex, int endIndex) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZREVRANGE, getRawName(), startIndex, endIndex);
    }
    
    /** 按排名范围返回成员与分数。 */
    @Override
    public Collection<ScoredEntry<V>> entryRange(int startIndex, int endIndex) {
        return get(entryRangeAsync(startIndex, endIndex));
    }

    /** 异步执行 entryRange。 */
    @Override
    public RFuture<Collection<ScoredEntry<V>>> entryRangeAsync(int startIndex, int endIndex) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGE_ENTRY, getRawName(), startIndex, endIndex, "WITHSCORES");
    }

    /** ZSet entryRangeReversed 操作。 */
    @Override
    public Collection<ScoredEntry<V>> entryRangeReversed(int startIndex, int endIndex) {
        return get(entryRangeReversedAsync(startIndex, endIndex));
    }
    
    /** 异步执行 entryRangeReversed。 */
    @Override
    public RFuture<Collection<ScoredEntry<V>>> entryRangeReversedAsync(int startIndex, int endIndex) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZREVRANGE_ENTRY, getRawName(), startIndex, endIndex, "WITHSCORES");
    }

    /** 按排名范围返回成员。 */
    @Override
    public Collection<V> valueRange(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        return get(valueRangeAsync(startScore, startScoreInclusive, endScore, endScoreInclusive));
    }

    /** 异步执行 valueRange。 */
    @Override
    public RFuture<Collection<V>> valueRangeAsync(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGEBYSCORE_LIST, getRawName(), startValue, endValue);
    }

    /** 按排名降序范围返回成员。 */
    @Override
    public Collection<V> valueRangeReversed(double startScore, boolean startScoreInclusive, double endScore,
            boolean endScoreInclusive) {
        return get(valueRangeReversedAsync(startScore, startScoreInclusive, endScore, endScoreInclusive));
    }

    /** 异步执行 valueRangeReversed。 */
    @Override
    public RFuture<Collection<V>> valueRangeReversedAsync(double startScore, boolean startScoreInclusive, double endScore,
            boolean endScoreInclusive) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZREVRANGEBYSCORE, getRawName(), endValue, startValue);
    }


    /** 按排名范围返回成员与分数。 */
    @Override
    public Collection<ScoredEntry<V>> entryRange(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        return get(entryRangeAsync(startScore, startScoreInclusive, endScore, endScoreInclusive));
    }

    /** 异步执行 entryRange。 */
    @Override
    public RFuture<Collection<ScoredEntry<V>>> entryRangeAsync(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGEBYSCORE_ENTRY, getRawName(), startValue, endValue, "WITHSCORES");
    }

    /** 按排名范围返回成员。 */
    @Override
    public Collection<V> valueRange(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        return get(valueRangeAsync(startScore, startScoreInclusive, endScore, endScoreInclusive, offset, count));
    }

    /** 异步执行 valueRange。 */
    @Override
    public RFuture<Collection<V>> valueRangeAsync(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGEBYSCORE_LIST, getRawName(), startValue, endValue, "LIMIT", offset, count);
    }

    /** 按排名降序范围返回成员。 */
    @Override
    public Collection<V> valueRangeReversed(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        return get(valueRangeReversedAsync(startScore, startScoreInclusive, endScore, endScoreInclusive, offset, count));
    }

    /** 异步执行 valueRangeReversed。 */
    @Override
    public RFuture<Collection<V>> valueRangeReversedAsync(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZREVRANGEBYSCORE, getRawName(), endValue, startValue, "LIMIT", offset, count);
    }

    /** 按排名范围返回成员与分数。 */
    @Override
    public Collection<ScoredEntry<V>> entryRange(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        return get(entryRangeAsync(startScore, startScoreInclusive, endScore, endScoreInclusive, offset, count));
    }

    /** ZSet entryRangeReversed 操作。 */
    @Override
    public Collection<ScoredEntry<V>> entryRangeReversed(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        return get(entryRangeReversedAsync(startScore, startScoreInclusive, endScore, endScoreInclusive, offset, count));
    }

    /** 异步执行 entryRange。 */
    @Override
    public RFuture<Collection<ScoredEntry<V>>> entryRangeAsync(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZRANGEBYSCORE_ENTRY, getRawName(), startValue, endValue, "WITHSCORES", "LIMIT", offset, count);
    }

    /** ZSet entryRangeReversed 操作。 */
    @Override
    public Collection<ScoredEntry<V>> entryRangeReversed(double startScore, boolean startScoreInclusive,
            double endScore, boolean endScoreInclusive) {
        return get(entryRangeReversedAsync(startScore, startScoreInclusive, endScore, endScoreInclusive));
    }
    
    /** 异步执行 entryRangeReversed。 */
    @Override
    public RFuture<Collection<ScoredEntry<V>>> entryRangeReversedAsync(double startScore, boolean startScoreInclusive,
            double endScore, boolean endScoreInclusive) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZREVRANGEBYSCORE_ENTRY, getRawName(), endValue, startValue, "WITHSCORES");
    }
    
    /** 异步执行 entryRangeReversed。 */
    @Override
    public RFuture<Collection<ScoredEntry<V>>> entryRangeReversedAsync(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZREVRANGEBYSCORE_ENTRY, getRawName(), endValue, startValue, "WITHSCORES", "LIMIT", offset, count);
    }

    /** 异步执行 revRank。 */
    @Override
    public RFuture<Integer> revRankAsync(V o) {
        String name = getRawName(o);
        return commandExecutor.readAsync(name, codec, RedisCommands.ZREVRANK_INT, name, encode(o));
    }

    /** ZSet revRankEntry 操作。 */
    @Override
    public RankedEntry<V> revRankEntry(V o) {
        return get(revRankEntryAsync(o));
    }

    /** 异步执行 revRankEntry。 */
    @Override
    public RFuture<RankedEntry<V>> revRankEntryAsync(V o) {
        String name = getRawName(o);
        return commandExecutor.readAsync(name, codec, RedisCommands.ZREVRANK_ENTRY, name, encode(o), "WITHSCORE");
    }

    /** 返回成员降序排名。 */
    @Override
    public Integer revRank(V o) {
        return get(revRankAsync(o));
    }


    /** 异步执行 revRank。 */
    @Override
    public RFuture<List<Integer>> revRankAsync(Collection<V> elements) {
        return commandExecutor.evalReadAsync((String) null, IntegerCodec.INSTANCE, RedisCommands.EVAL_INT_LIST,
                        "local r = {} " +
                        "for i, v in ipairs(ARGV) do " +
                            "r[#r+1] = redis.call('zrevrank', KEYS[1], ARGV[i]); " +
                        "end;" +
                        "return r;",
                Collections.singletonList(getRawName()), encode(elements).toArray());
    }

    /** 返回成员降序排名。 */
    @Override
    public List<Integer> revRank(Collection<V> elements) {
        return get(revRankAsync(elements));
    }

    /** 统计分数区间内成员数。 */
    @Override
    public int count(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        return get(countAsync(startScore, startScoreInclusive, endScore, endScoreInclusive));
    }
    
    /** 异步执行 count。 */
    @Override
    public RFuture<Integer> countAsync(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZCOUNT, getRawName(), startValue, endValue);
    }
    
    /** 计算并存储集合交集。 */
    @Override
    public int intersection(String... names) {
        return get(intersectionAsync(names));        
    }

    /** 异步计算 ZSet 交集。 */
    @Override
    public RFuture<Integer> intersectionAsync(String... names) {
        return intersectionAsync(Aggregate.SUM, names);
    }
    
    /** 计算并存储集合交集。 */
    @Override
    public int intersection(Aggregate aggregate, String... names) {
        return get(intersectionAsync(aggregate, names));        
    }
    
    /** 异步计算 ZSet 交集。 */
    @Override
    public RFuture<Integer> intersectionAsync(Aggregate aggregate, String... names) {
        List<Object> args = new ArrayList<>(names.length + 4);
        args.add(getRawName());
        args.add(names.length);
        args.addAll(map(names));
        args.add("AGGREGATE");
        args.add(aggregate.name());
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.ZINTERSTORE_INT, args.toArray());
    }

    /** 计算并存储集合交集。 */
    @Override
    public int intersection(Map<String, Double> nameWithWeight) {
        return get(intersectionAsync(nameWithWeight));        
    }
    
    /** 异步计算 ZSet 交集。 */
    @Override
    public RFuture<Integer> intersectionAsync(Map<String, Double> nameWithWeight) {
        return intersectionAsync(Aggregate.SUM, nameWithWeight);
    }

    /** 计算并存储集合交集。 */
    @Override
    public int intersection(Aggregate aggregate, Map<String, Double> nameWithWeight) {
        return get(intersectionAsync(aggregate, nameWithWeight));        
    }

    /** 异步计算 ZSet 交集。 */
    @Override
    public RFuture<Integer> intersectionAsync(Aggregate aggregate, Map<String, Double> nameWithWeight) {
        List<Object> args = new ArrayList<>(nameWithWeight.size() * 2 + 5);
        args.add(getRawName());
        args.add(nameWithWeight.size());
        args.addAll(nameWithWeight.keySet());
        args.add("WEIGHTS");
        List<String> weights = new ArrayList<>();
        for (Double weight : nameWithWeight.values()) {
            weights.add(BigDecimal.valueOf(weight).toPlainString());
        }
        args.addAll(weights);
        args.add("AGGREGATE");
        args.add(aggregate.name());
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.ZINTERSTORE_INT, args.toArray());
    }
    
    /** 计算并存储集合交集。 */
    @Override
    public int intersection(SetIntersectionArgs args) {
        return get(intersectionAsync(args));
    }
    
    /** 异步计算 ZSet 交集。 */
    @Override
    public RFuture<Integer> intersectionAsync(SetIntersectionArgs args) {
        SetIntersectionParams sip = (SetIntersectionParams) args;
        List<Object> params = new ArrayList<>();
        params.add(getRawName());
        params.add(sip.getNames().length);
        params.addAll(map(sip.getNames()));
        if (sip.getWeights() != null && sip.getWeights().length > 0) {
            params.add("WEIGHTS");
            params.addAll(Arrays.asList(sip.getWeights()));
        }
        params.add("AGGREGATE");
        params.add(sip.getAggregate().name());
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.ZINTERSTORE_INT, params.toArray());
    }

    /** ZSet readIntersection 操作。 */
    @Override
    public Collection<V> readIntersection(String... names) {
        return get(readIntersectionAsync(names));
    }

    /** 异步执行 readIntersection。 */
    @Override
    public RFuture<Collection<V>> readIntersectionAsync(String... names) {
        return readIntersectionAsync(Aggregate.SUM, names);
    }

    /** ZSet readIntersection 操作。 */
    @Override
    public Collection<V> readIntersection(Aggregate aggregate, String... names) {
        return get(readIntersectionAsync(aggregate, names));
    }

    /** 异步执行 readIntersection。 */
    @Override
    public RFuture<Collection<V>> readIntersectionAsync(Aggregate aggregate, String... names) {
        List<Object> args = new ArrayList<>(names.length + 4);
        args.add(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        args.add("AGGREGATE");
        args.add(aggregate.name());
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZINTER, args.toArray());
    }

    /** ZSet readIntersection 操作。 */
    @Override
    public Collection<V> readIntersection(Map<String, Double> nameWithWeight) {
        return get(readIntersectionAsync(nameWithWeight));
    }

    /** 异步执行 readIntersection。 */
    @Override
    public RFuture<Collection<V>> readIntersectionAsync(Map<String, Double> nameWithWeight) {
        return readIntersectionAsync(Aggregate.SUM, nameWithWeight);
    }

    /** ZSet readIntersection 操作。 */
    @Override
    public Collection<V> readIntersection(Aggregate aggregate, Map<String, Double> nameWithWeight) {
        return get(readIntersectionAsync(aggregate, nameWithWeight));
    }

    /** 异步执行 readIntersection。 */
    @Override
    public RFuture<Collection<V>> readIntersectionAsync(Aggregate aggregate, Map<String, Double> nameWithWeight) {
        List<Object> args = new ArrayList<>(nameWithWeight.size() * 2 + 5);
        args.add(nameWithWeight.size() + 1);
        args.add(getRawName());
        args.addAll(nameWithWeight.keySet());
        args.add("WEIGHTS");
        List<String> weights = new ArrayList<>();
        for (Double weight : nameWithWeight.values()) {
            weights.add(BigDecimal.valueOf(weight).toPlainString());
        }
        args.addAll(weights);
        args.add("AGGREGATE");
        args.add(aggregate.name());
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZINTER, args.toArray());
    }

    /** ZSet readIntersection 操作。 */
    @Override
    public Collection<V> readIntersection(SetIntersectionArgs args) {
        return get(readIntersectionAsync(args));
    }

    /** 异步执行 readIntersection。 */
    @Override
    public RFuture<Collection<V>> readIntersectionAsync(SetIntersectionArgs args) {
        SetIntersectionParams sip = (SetIntersectionParams) args;
        List<Object> params = new ArrayList<>();
        params.add(sip.getNames().length + 1);
        params.add(getRawName());
        params.addAll(map(sip.getNames()));
        if (sip.getWeights() != null && sip.getWeights().length > 0) {
            params.add("WEIGHTS");
            params.addAll(Arrays.asList(sip.getWeights()));
        }
        params.add("AGGREGATE");
        params.add(sip.getAggregate().name());
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZINTER, params.toArray());
    }

    /** ZSet readIntersectionEntries 操作。 */
    @Override
    public Collection<ScoredEntry<V>> readIntersectionEntries(SetIntersectionArgs args) {
        return get(readIntersectionEntriesAsync(args));
    }

    /** 异步执行 readIntersectionEntries。 */
    @Override
    public RFuture<Collection<ScoredEntry<V>>> readIntersectionEntriesAsync(SetIntersectionArgs args) {
        SetIntersectionParams sip = (SetIntersectionParams) args;
        List<Object> params = new ArrayList<>();
        params.add(sip.getNames().length + 1);
        params.add(getRawName());
        params.addAll(map(sip.getNames()));
        if (sip.getWeights() != null && sip.getWeights().length > 0) {
            params.add("WEIGHTS");
            params.addAll(Arrays.asList(sip.getWeights()));
        }
        params.add("AGGREGATE");
        params.add(sip.getAggregate().name());
        params.add("WITHSCORES");
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZINITER_ENTRY, params.toArray());
    }

    /** ZSet countIntersection 操作。 */
    @Override
    public Integer countIntersection(String... names) {
        return get(countIntersectionAsync(names));
    }

    /** ZSet countIntersection 操作。 */
    @Override
    public Integer countIntersection(int limit, String... names) {
        return get(countIntersectionAsync(limit, names));
    }

    /** 异步执行 countIntersection。 */
    @Override
    public RFuture<Integer> countIntersectionAsync(String... names) {
        return countIntersectionAsync(0, names);
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
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZINTERCARD_INT, args.toArray());
    }

    /** 计算并存储集合并集。 */
    @Override
    public int union(String... names) {
        return get(unionAsync(names));        
    }

    /** 异步计算 ZSet 并集。 */
    @Override
    public RFuture<Integer> unionAsync(String... names) {
        return unionAsync(Aggregate.SUM, names);
    }
    
    /** 计算并存储集合并集。 */
    @Override
    public int union(Aggregate aggregate, String... names) {
        return get(unionAsync(aggregate, names));        
    }
    
    /** 异步计算 ZSet 并集。 */
    @Override
    public RFuture<Integer> unionAsync(Aggregate aggregate, String... names) {
        List<Object> args = new ArrayList<>(names.length + 4);
        args.add(getRawName());
        args.add(names.length);
        args.addAll(map(names));
        args.add("AGGREGATE");
        args.add(aggregate.name());
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.ZUNIONSTORE_INT, args.toArray());
    }

    /** 计算并存储集合并集。 */
    @Override
    public int union(Map<String, Double> nameWithWeight) {
        return get(unionAsync(nameWithWeight));        
    }
    
    /** 异步计算 ZSet 并集。 */
    @Override
    public RFuture<Integer> unionAsync(Map<String, Double> nameWithWeight) {
        return unionAsync(Aggregate.SUM, nameWithWeight);
    }

    /** 计算并存储集合并集。 */
    @Override
    public int union(Aggregate aggregate, Map<String, Double> nameWithWeight) {
        return get(unionAsync(aggregate, nameWithWeight));        
    }

    /** 异步计算 ZSet 并集。 */
    @Override
    public RFuture<Integer> unionAsync(Aggregate aggregate, Map<String, Double> nameWithWeight) {
        List<Object> args = new ArrayList<>(nameWithWeight.size() * 2 + 5);
        args.add(getRawName());
        args.add(nameWithWeight.size());
        args.addAll(nameWithWeight.keySet());
        args.add("WEIGHTS");
        List<String> weights = new ArrayList<>();
        for (Double weight : nameWithWeight.values()) {
            weights.add(BigDecimal.valueOf(weight).toPlainString());
        }
        args.addAll(weights);
        args.add("AGGREGATE");
        args.add(aggregate.name());
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.ZUNIONSTORE_INT, args.toArray());
    }
    
    /** 计算并存储集合并集。 */
    @Override
    public int union(SetUnionArgs args) {
        return get(unionAsync(args));
    }
    
    /** 异步计算 ZSet 并集。 */
    @Override
    public RFuture<Integer> unionAsync(SetUnionArgs args) {
        SetUnionParams sup = (SetUnionParams) args;
        List<Object> params = new ArrayList<>();
        params.add(getRawName());
        params.add(sup.getNames().length);
        params.addAll(map(sup.getNames()));
        if (sup.getWeights() != null && sup.getWeights().length > 0) {
            params.add("WEIGHTS");
            params.addAll(Arrays.asList(sup.getWeights()));
        }
        params.add("AGGREGATE");
        params.add(sup.getAggregate().name());
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.ZUNIONSTORE_INT, params.toArray());
    }

    /** ZSet readUnion 操作。 */
    @Override
    public Collection<V> readUnion(String... names) {
        return get(readUnionAsync(names));
    }

    /** 异步执行 readUnion。 */
    @Override
    public RFuture<Collection<V>> readUnionAsync(String... names) {
        return readUnionAsync(Aggregate.SUM, names);
    }

    /** ZSet readUnion 操作。 */
    @Override
    public Collection<V> readUnion(Aggregate aggregate, String... names) {
        return get(readUnionAsync(aggregate, names));
    }

    /** 异步执行 readUnion。 */
    @Override
    public RFuture<Collection<V>> readUnionAsync(Aggregate aggregate, String... names) {
        List<Object> args = new ArrayList<>(names.length + 4);
        args.add(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        args.add("AGGREGATE");
        args.add(aggregate.name());
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZUNION, args.toArray());
    }

    /** ZSet readUnion 操作。 */
    @Override
    public Collection<V> readUnion(Map<String, Double> nameWithWeight) {
        return get(readUnionAsync(nameWithWeight));
    }

    /** 异步执行 readUnion。 */
    @Override
    public RFuture<Collection<V>> readUnionAsync(Map<String, Double> nameWithWeight) {
        return readUnionAsync(Aggregate.SUM, nameWithWeight);
    }

    /** ZSet readUnion 操作。 */
    @Override
    public Collection<V> readUnion(Aggregate aggregate, Map<String, Double> nameWithWeight) {
        return get(readUnionAsync(aggregate, nameWithWeight));
    }

    /** 异步执行 readUnion。 */
    @Override
    public RFuture<Collection<V>> readUnionAsync(Aggregate aggregate, Map<String, Double> nameWithWeight) {
        List<Object> args = new ArrayList<>(nameWithWeight.size() * 2 + 5);
        args.add(nameWithWeight.size() + 1);
        args.add(getRawName());
        args.addAll(nameWithWeight.keySet());
        args.add("WEIGHTS");
        List<String> weights = new ArrayList<>();
        for (Double weight : nameWithWeight.values()) {
            weights.add(BigDecimal.valueOf(weight).toPlainString());
        }
        args.addAll(weights);
        args.add("AGGREGATE");
        args.add(aggregate.name());
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZUNION, args.toArray());
    }

    /** ZSet readUnion 操作。 */
    @Override
    public Collection<V> readUnion(SetUnionArgs args) {
        return get(readUnionAsync(args));
    }

    /** 异步执行 readUnion。 */
    @Override
    public RFuture<Collection<V>> readUnionAsync(SetUnionArgs args) {
        SetUnionParams sup = (SetUnionParams) args;
        List<Object> params = new ArrayList<>();
        params.add(sup.getNames().length + 1);
        params.add(getRawName());
        params.addAll(map(sup.getNames()));
        if (sup.getWeights() != null && sup.getWeights().length > 0) {
            params.add("WEIGHTS");
            params.addAll(Arrays.asList(sup.getWeights()));
        }
        params.add("AGGREGATE");
        params.add(sup.getAggregate().name());
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZUNION, params.toArray());
    }

    /** ZSet readUnionEntries 操作。 */
    @Override
    public Collection<ScoredEntry<V>> readUnionEntries(SetUnionArgs args) {
        return get(readUnionEntriesAsync(args));
    }

    /** 异步执行 readUnionEntries。 */
    @Override
    public RFuture<Collection<ScoredEntry<V>>> readUnionEntriesAsync(SetUnionArgs args) {
        SetUnionParams sup = (SetUnionParams) args;
        List<Object> params = new ArrayList<>();
        params.add(sup.getNames().length + 1);
        params.add(getRawName());
        params.addAll(map(sup.getNames()));
        if (sup.getWeights() != null && sup.getWeights().length > 0) {
            params.add("WEIGHTS");
            params.addAll(Arrays.asList(sup.getWeights()));
        }
        params.add("AGGREGATE");
        params.add(sup.getAggregate().name());
        params.add("WITHSCORES");
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZUNION_ENTRY, params.toArray());
    }

    /** ZSet readSort 操作。 */
    @Override
    public Set<V> readSort(SortOrder order) {
        return get(readSortAsync(order));
    }
    
    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(SortOrder order) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), order);
    }

    /** ZSet readSort 操作。 */
    @Override
    public Set<V> readSort(SortOrder order, int offset, int count) {
        return get(readSortAsync(order, offset, count));
    }
    
    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(SortOrder order, int offset, int count) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), "LIMIT", offset, count, order);
    }

    /** ZSet readSort 操作。 */
    @Override
    public Set<V> readSort(String byPattern, SortOrder order) {
        return get(readSortAsync(byPattern, order));
    }
    
    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(String byPattern, SortOrder order) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), "BY", byPattern, order);
    }
    
    /** ZSet readSort 操作。 */
    @Override
    public Set<V> readSort(String byPattern, SortOrder order, int offset, int count) {
        return get(readSortAsync(byPattern, order, offset, count));
    }
    
    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(String byPattern, SortOrder order, int offset, int count) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), "BY", byPattern, "LIMIT", offset, count, order);
    }

    /** ZSet readSort 操作。 */
    @Override
    public <T> Collection<T> readSort(String byPattern, List<String> getPatterns, SortOrder order) {
        return get(readSortAsync(byPattern, getPatterns, order));
    }
    
    /** 异步执行 readSort。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order) {
        return readSortAsync(byPattern, getPatterns, order, -1, -1);
    }
    
    /** ZSet readSort 操作。 */
    @Override
    public <T> Collection<T> readSort(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return get(readSortAsync(byPattern, getPatterns, order, offset, count));
    }

    /** 异步执行 readSort。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return readSortAsync(byPattern, getPatterns, order, offset, count, false);
    }

    /** ZSet readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(SortOrder order) {
        return get(readSortAlphaAsync(order));
    }

    /** ZSet readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(SortOrder order, int offset, int count) {
        return get(readSortAlphaAsync(order, offset, count));
    }

    /** ZSet readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(String byPattern, SortOrder order) {
        return get(readSortAlphaAsync(byPattern, order));
    }

    /** ZSet readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(String byPattern, SortOrder order, int offset, int count) {
        return get(readSortAlphaAsync(byPattern, order, offset, count));
    }

    /** ZSet readSortAlpha 操作。 */
    @Override
    public <T> Collection<T> readSortAlpha(String byPattern, List<String> getPatterns, SortOrder order) {
        return get(readSortAlphaAsync(byPattern, getPatterns, order));
    }

    /** ZSet readSortAlpha 操作。 */
    @Override
    public <T> Collection<T> readSortAlpha(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return get(readSortAlphaAsync(byPattern, getPatterns, order, offset, count));
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(SortOrder order) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), "ALPHA", order);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(SortOrder order, int offset, int count) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), "LIMIT", offset, count, "ALPHA", order);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(String byPattern, SortOrder order) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), "BY", byPattern, "ALPHA", order);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<Set<V>> readSortAlphaAsync(String byPattern, SortOrder order, int offset, int count) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), "BY", byPattern, "LIMIT", offset, count, "ALPHA", order);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order) {
        return readSortAlphaAsync(byPattern, getPatterns, order, -1, -1);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return readSortAsync(byPattern, getPatterns, order, offset, count, true);
    }

    /** ZSet sortTo 操作。 */
    @Override
    public int sortTo(String destName, SortOrder order) {
        return get(sortToAsync(destName, order));
    }
    
    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, SortOrder order) {
        return sortToAsync(destName, null, Collections.emptyList(), order, -1, -1);
    }
    
    /** ZSet sortTo 操作。 */
    @Override
    public int sortTo(String destName, SortOrder order, int offset, int count) {
        return get(sortToAsync(destName, order, offset, count));
    }
    
    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, SortOrder order, int offset, int count) {
        return sortToAsync(destName, null, Collections.emptyList(), order, offset, count);
    }

    /** ZSet sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, SortOrder order, int offset, int count) {
        return get(sortToAsync(destName, byPattern, order, offset, count));
    }
    
    /** ZSet sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, SortOrder order) {
        return get(sortToAsync(destName, byPattern, order));
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order) {
        return sortToAsync(destName, byPattern, Collections.emptyList(), order, -1, -1);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order, int offset, int count) {
        return sortToAsync(destName, byPattern, Collections.emptyList(), order, offset, count);
    }

    /** ZSet sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, List<String> getPatterns, SortOrder order) {
        return get(sortToAsync(destName, byPattern, getPatterns, order));
    }
    
    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order) {
        return sortToAsync(destName, byPattern, getPatterns, order, -1, -1);
    }
    
    /** ZSet sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return get(sortToAsync(destName, byPattern, getPatterns, order, offset, count));
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        List<Object> params = new ArrayList<>();
        params.add(getRawName());
        if (byPattern != null) {
            params.add("BY");
            params.add(byPattern);
        }
        if (offset != -1 && count != -1) {
            params.add("LIMIT");
        }
        if (offset != -1) {
            params.add(offset);
        }
        if (count != -1) {
            params.add(count);
        }
        for (String pattern : getPatterns) {
            params.add("GET");
            params.add(pattern);
        }
        params.add(order);
        params.add("STORE");
        params.add(destName);
        
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SORT_TO, params.toArray());
    }

    /** 异步执行 readSort。 */
    private <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count, boolean alpha) {
        List<Object> params = new ArrayList<>();
        params.add(getRawName());
        if (byPattern != null) {
            params.add("BY");
            params.add(byPattern);
        }
        if (offset != -1 && count != -1) {
            params.add("LIMIT");
        }
        if (offset != -1) {
            params.add(offset);
        }
        if (count != -1) {
            params.add(count);
        }
        if (getPatterns != null) {
            for (String pattern : getPatterns) {
                params.add("GET");
                params.add(pattern);
            }
        }
        if (alpha) {
            params.add("ALPHA");
        }
        if (order != null) {
            params.add(order);
        }

        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, params.toArray());
    }

    /** ZSet readDiff 操作。 */
    @Override
    public Collection<V> readDiff(String... names) {
        return get(readDiffAsync(names));
    }

    /** 异步执行 readDiff。 */
    @Override
    public RFuture<Collection<V>> readDiffAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 2);
        args.add(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZDIFF, args.toArray());
    }

    /** ZSet readDiffEntries 操作。 */
    @Override
    public Collection<ScoredEntry<V>> readDiffEntries(String... names) {
        return get(readDiffEntriesAsync(names));
    }

    /** 异步执行 readDiffEntries。 */
    @Override
    public RFuture<Collection<ScoredEntry<V>>> readDiffEntriesAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 3);
        args.add(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        args.add("WITHSCORES");
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ZDIFF_ENTRY, args.toArray());
    }

    /** ZSet diff 操作。 */
    @Override
    public int diff(String... names) {
        return get(diffAsync(names));
    }

    /** 异步执行 diff。 */
    @Override
    public RFuture<Integer> diffAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 2);
        args.add(getRawName());
        args.add(names.length);
        args.addAll(map(names));
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZDIFFSTORE_INT, args.toArray());
    }

    /** ZSet rangeTo 操作。 */
    @Override
    public int rangeTo(String destName, int startIndex, int endIndex) {
        return get(rangeToAsync(destName, startIndex, endIndex));
    }

    /** ZSet rangeTo 操作。 */
    @Override
    public int rangeTo(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        return get(rangeToAsync(destName, startScore, startScoreInclusive, endScore, endScoreInclusive));
    }

    /** ZSet rangeTo 操作。 */
    @Override
    public int rangeTo(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        return get(rangeToAsync(destName, startScore, startScoreInclusive, endScore, endScoreInclusive, offset, count));
    }

    /** ZSet revRangeTo 操作。 */
    @Override
    public int revRangeTo(String destName, int startIndex, int endIndex) {
        return get(revRangeToAsync(destName, startIndex, endIndex));
    }

    /** ZSet revRangeTo 操作。 */
    @Override
    public int revRangeTo(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        return get(revRangeToAsync(destName, startScore, startScoreInclusive, endScore, endScoreInclusive));
    }

    /** ZSet revRangeTo 操作。 */
    @Override
    public int revRangeTo(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        return get(revRangeToAsync(destName, startScore, startScoreInclusive, endScore, endScoreInclusive, offset, count));
    }

    /** 异步执行 revRangeTo。 */
    @Override
    public RFuture<Integer> revRangeToAsync(String destName, int startIndex, int endIndex) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZRANGESTORE, destName, getRawName(), startIndex, endIndex, "REV");
    }

    /** 异步执行 revRangeTo。 */
    @Override
    public RFuture<Integer> revRangeToAsync(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZRANGESTORE, destName, getRawName(), startValue, endValue, "BYSCORE", "REV");
    }

    /** 异步执行 revRangeTo。 */
    @Override
    public RFuture<Integer> revRangeToAsync(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZRANGESTORE, destName, getRawName(), startValue, endValue, "BYSCORE", "REV", "LIMIT", offset, count);
    }

    /** 异步执行 rangeTo。 */
    @Override
    public RFuture<Integer> rangeToAsync(String destName, int startIndex, int endIndex) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZRANGESTORE, destName, getRawName(), startIndex, endIndex);
    }

    /** 异步执行 rangeTo。 */
    @Override
    public RFuture<Integer> rangeToAsync(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZRANGESTORE, destName, getRawName(), startValue, endValue, "BYSCORE");
    }

    /** 异步执行 rangeTo。 */
    @Override
    public RFuture<Integer> rangeToAsync(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count) {
        String startValue = value(startScore, startScoreInclusive);
        String endValue = value(endScore, endScoreInclusive);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.ZRANGESTORE, destName, getRawName(), startValue, endValue, "BYSCORE", "LIMIT", offset, count);
    }

    /** 异步执行 takeFirst。 */
    @Override
    public RFuture<V> takeFirstAsync() {
        return pollFirstAsync(0, TimeUnit.SECONDS);
    }

    /** 异步执行 takeLast。 */
    @Override
    public RFuture<V> takeLastAsync() {
        return pollLastAsync(0, TimeUnit.SECONDS);
    }

    /** ZSet takeFirst 操作。 */
    @Override
    public V takeFirst() {
        return get(takeFirstAsync());
    }

    /** ZSet takeLast 操作。 */
    @Override
    public V takeLast() {
        return get(takeLastAsync());
    }

    /** ZSet subscribeOnFirstElements 操作。 */
    @Override
    public int subscribeOnFirstElements(Consumer<V> consumer) {
        return getServiceManager().getElementsSubscribeService()
                .subscribeOnElements(this::takeFirstAsync, consumer);
    }

    /** ZSet subscribeOnLastElements 操作。 */
    @Override
    public int subscribeOnLastElements(Consumer<V> consumer) {
        return getServiceManager().getElementsSubscribeService()
                .subscribeOnElements(this::takeLastAsync, consumer);
    }

    /** ZSet subscribeOnFirstElements 操作。 */
    @Override
    public int subscribeOnFirstElements(Function<V, CompletionStage<Void>> consumer) {
        return getServiceManager().getElementsSubscribeService()
                .subscribeOnElements(this::takeFirstAsync, consumer);
    }

    /** ZSet subscribeOnLastElements 操作。 */
    @Override
    public int subscribeOnLastElements(Function<V, CompletionStage<Void>> consumer) {
        return getServiceManager().getElementsSubscribeService()
                .subscribeOnElements(this::takeLastAsync, consumer);
    }

    /** ZSet unsubscribe 操作。 */
    @Override
    public void unsubscribe(int listenerId) {
        getServiceManager().getElementsSubscribeService().unsubscribe(listenerId);
    }

    /** ZSet stream 操作。 */
    @Override
    public Stream<V> stream() {
        return toStream(iterator());
    }

    /** ZSet stream 操作。 */
    @Override
    public Stream<V> stream(String pattern) {
        return toStream(iterator(pattern));
    }

    /** ZSet stream 操作。 */
    @Override
    public Stream<V> stream(int count) {
        return toStream(iterator(count));
    }

    /** ZSet stream 操作。 */
    @Override
    public Stream<V> stream(String pattern, int count) {
        return toStream(iterator(pattern, count));
    }

    /** 注册对象变更监听器。 */
    @Override
    public int addListener(ObjectListener listener) {
        if (listener instanceof ScoredSortedSetAddListener) {
            return addListener("__keyevent@*:zadd", (ScoredSortedSetAddListener) listener, ScoredSortedSetAddListener::onAdd);
        }
        if (listener instanceof ScoredSortedSetRemoveListener) {
            return addListener("__keyevent@*:zrem", (ScoredSortedSetRemoveListener) listener, ScoredSortedSetRemoveListener::onRemove);
        }
        if (listener instanceof ScoredSortedSetIncrListener) {
            return addListener("__keyevent@*:zincr", (ScoredSortedSetIncrListener) listener, ScoredSortedSetIncrListener::onIncrement);
        }
        if (listener instanceof ScoredSortedSetUnionStoreListener) {
            return addListener("__keyevent@*:zunionstore", (ScoredSortedSetUnionStoreListener) listener, ScoredSortedSetUnionStoreListener::onStore);
        }
        if (listener instanceof ScoredSortedSetInterStoreListener) {
            return addListener("__keyevent@*:zinterstore", (ScoredSortedSetInterStoreListener) listener, ScoredSortedSetInterStoreListener::onStore);
        }
        if (listener instanceof ScoredSortedSetDiffStoreListener) {
            return addListener("__keyevent@*:zdiffstore", (ScoredSortedSetDiffStoreListener) listener, ScoredSortedSetDiffStoreListener::onStore);
        }
        if (listener instanceof TrackingListener) {
            return addTrackingListener((TrackingListener) listener);
        }

        return super.addListener(listener);
    }

    /** 异步执行 addListener。 */
    @Override
    public RFuture<Integer> addListenerAsync(ObjectListener listener) {
        if (listener instanceof ScoredSortedSetAddListener) {
            return addListenerAsync("__keyevent@*:zadd", (ScoredSortedSetAddListener) listener, ScoredSortedSetAddListener::onAdd);
        }
        if (listener instanceof ScoredSortedSetRemoveListener) {
            return addListenerAsync("__keyevent@*:zrem", (ScoredSortedSetRemoveListener) listener, ScoredSortedSetRemoveListener::onRemove);
        }
        if (listener instanceof ScoredSortedSetIncrListener) {
            return addListenerAsync("__keyevent@*:zincr", (ScoredSortedSetIncrListener) listener, ScoredSortedSetIncrListener::onIncrement);
        }
        if (listener instanceof ScoredSortedSetUnionStoreListener) {
            return addListenerAsync("__keyevent@*:zunionstore", (ScoredSortedSetUnionStoreListener) listener, ScoredSortedSetUnionStoreListener::onStore);
        }
        if (listener instanceof ScoredSortedSetInterStoreListener) {
            return addListenerAsync("__keyevent@*:zinterstore", (ScoredSortedSetInterStoreListener) listener, ScoredSortedSetInterStoreListener::onStore);
        }
        if (listener instanceof ScoredSortedSetDiffStoreListener) {
            return addListenerAsync("__keyevent@*:zdiffstore", (ScoredSortedSetDiffStoreListener) listener, ScoredSortedSetDiffStoreListener::onStore);
        }
        if (listener instanceof TrackingListener) {
            return addTrackingListenerAsync((TrackingListener) listener);
        }

        return super.addListenerAsync(listener);
    }

    /** 移除监听器。 */
    @Override
    public void removeListener(int listenerId) {
        removeTrackingListener(listenerId);
        removeListener(listenerId, "__keyevent@*:zadd", "__keyevent@*:zrem", "__keyevent@*:zincr", "__keyevent@*:zunionstore", "__keyevent@*:zinterstore", "__keyevent@*:zdiffstore");
        super.removeListener(listenerId);
    }

    /** 异步执行 removeListener。 */
    @Override
    public RFuture<Void> removeListenerAsync(int listenerId) {
        return removeListenerAsync(removeTrackingListenerAsync(listenerId), listenerId,
                "__keyevent@*:zadd", "__keyevent@*:zrem", "__keyevent@*:zincr", "__keyevent@*:zunionstore", "__keyevent@*:zinterstore", "__keyevent@*:zdiffstore");
    }

}
