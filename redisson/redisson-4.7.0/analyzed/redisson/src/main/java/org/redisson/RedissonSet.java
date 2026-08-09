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
import org.redisson.api.listener.SetAddListener;
import org.redisson.api.listener.SetDiffStoreListener;
import org.redisson.api.listener.SetInterStoreListener;
import org.redisson.api.listener.SetRemoveListener;
import org.redisson.api.listener.SetRemoveRandomListener;
import org.redisson.api.listener.SetUnionStoreListener;
import org.redisson.api.listener.TrackingListener;
import org.redisson.api.mapreduce.RCollectionMapReduce;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.decoder.ContainsSetDecoder;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.iterator.BaseAsyncIterator;
import org.redisson.iterator.RedissonBaseIterator;
import org.redisson.mapreduce.RedissonCollectionMapReduce;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.misc.CompositeAsyncIterator;

import java.util.*;
import java.util.stream.Stream;

/**
 * 分布式 Set {@link RSet}，对应 {@link java.util.Set}。
 * <p>封装 SADD/SREM、SMEMBERS、SINTER/SUNION/SDIFF、SSCAN
 * 及随机成员、MapReduce 等操作。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public class RedissonSet<V> extends RedissonExpirable implements RSet<V>, ScanIterator {

    /** 关联 Redisson 客户端（MapReduce 等）。 */
    RedissonClient redisson;
    
    public RedissonSet(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(commandExecutor, name);
        this.redisson = redisson;
    }

    public RedissonSet(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(codec, commandExecutor, name);
        this.redisson = redisson;
    }

    /** 创建 MapReduce 任务入口。 */
    @Override
    public <KOut, VOut> RCollectionMapReduce<V, KOut, VOut> mapReduce() {
        return new RedissonCollectionMapReduce<V, KOut, VOut>(this, redisson, commandExecutor);
    }

    /** 返回元素数量。 */
    @Override
    public int size() {
        return get(sizeAsync());
    }

    /** 异步返回元素数量。 */
    @Override
    public RFuture<Integer> sizeAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SCARD_INT, getRawName());
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
        return commandExecutor.readAsync(name, codec, RedisCommands.SISMEMBER, name, encode(o));
    }

    /** SSCAN 增量扫描迭代器。 */
    @Override
    public ScanResult<Object> scanIterator(String name, RedisClient client, String startPos, String pattern, int count) {
        return get(scanIteratorAsync(name, client, startPos, pattern, count));
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
                RedissonSet.this.remove((V) value);
            }
            
        };
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

    /** Set distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(final String pattern) {
        String iteratorName = "__redisson_set_cursor_{" + getRawName() + "}";
        return distributedIterator(iteratorName, pattern, 10);
    }

    /** Set distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(final int count) {
        String iteratorName = "__redisson_set_cursor_{" + getRawName() + "}";
        return distributedIterator(iteratorName, null, count);
    }

    /** Set distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(final String iteratorName, final String pattern, final int count) {
        return new RedissonBaseIterator<V>() {

            @Override
            protected ScanResult<Object> iterator(RedisClient client, String nextIterPos) {
                return distributedScanIterator(iteratorName, pattern, count);
            }

            @Override
            protected void remove(Object value) {
                RedissonSet.this.remove((V) value);
            }
        };
    }

    /** Set distributedScanIterator 操作。 */
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
                    + "result = redis.call('sscan', KEYS[1], cursor, 'match', ARGV[1], 'count', ARGV[2]); "
                + "else "
                    + "result = redis.call('sscan', KEYS[1], cursor, 'count', ARGV[1]); "
                + "end;"
                + "local next_cursor = result[1]"
                + "if next_cursor ~= \"0\" then "
                    + "redis.call('setex', KEYS[2], 3600, next_cursor);"
                + "else "
                    + "redis.call('setex', KEYS[2], 3600, -1);"
                + "end; "
                + "return result;",
                Arrays.<Object>asList(getRawName(), iteratorName), args.toArray());
    }

    /** 返回成员迭代器。 */
    @Override
    public Iterator<V> iterator() {
        return iterator(null);
    }

    /** 异步读取全部成员。 */
    @Override
    public RFuture<Set<V>> readAllAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SMEMBERS, getRawName());
    }

    /** 一次性读取全部成员。 */
    @Override
    public Set<V> readAll() {
        return get(readAllAsync());
    }

    /** Set toArray 操作。 */
    @Override
    public Object[] toArray() {
        Set<Object> res = (Set<Object>) get(readAllAsync());
        return res.toArray();
    }

    /** Set toArray 操作。 */
    @Override
    public <T> T[] toArray(T[] a) {
        Set<Object> res = (Set<Object>) get(readAllAsync());
        return res.toArray(a);
    }

    /** 添加元素/成员。 */
    @Override
    public boolean add(V e) {
        return get(addAsync(e));
    }

    /** 异步添加 ZSet 成员。 */
    @Override
    public RFuture<Boolean> addAsync(V e) {
        String name = getRawName(e);
        return commandExecutor.writeAsync(name, codec, RedisCommands.SADD_SINGLE, name, encode(e));
    }

    /** 随机移除并返回一个成员。 */
    @Override
    public V removeRandom() {
        return get(removeRandomAsync());
    }

    /** 异步随机移除成员。 */
    @Override
    public RFuture<V> removeRandomAsync() {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SPOP_SINGLE, getRawName());
    }

    /** 随机移除并返回一个成员。 */
    @Override
    public Set<V> removeRandom(int amount) {
        return get(removeRandomAsync(amount));
    }

    /** 异步随机移除成员。 */
    @Override
    public RFuture<Set<V>> removeRandomAsync(int amount) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SPOP, getRawName(), amount);
    }
    
    /** 随机返回一个成员（不移除）。 */
    @Override
    public V random() {
        return get(randomAsync());
    }

    /** 异步随机返回成员。 */
    @Override
    public RFuture<V> randomAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SRANDMEMBER_SINGLE, getRawName());
    }

    /** 随机返回一个成员（不移除）。 */
    @Override
    public Set<V> random(int count) {
        return get(randomAsync(count));
    }

    /** 异步随机返回成员。 */
    @Override
    public RFuture<Set<V>> randomAsync(int count) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SRANDMEMBER, getRawName(), count);
    }

    /** 异步移除 ZSet 成员。 */
    @Override
    public RFuture<Boolean> removeAsync(Object o) {
        String name = getRawName(o);
        return commandExecutor.writeAsync(name, codec, RedisCommands.SREM_SINGLE, name, encode(o));
    }

    /** 移除元素。 */
    @Override
    public boolean remove(Object value) {
        return get(removeAsync((V) value));
    }

    /** 异步执行 move。 */
    @Override
    public RFuture<Boolean> moveAsync(String destination, V member) {
        String name = getRawName(member);
        return commandExecutor.writeAsync(name, codec, RedisCommands.SMOVE, name, destination, encode(member));
    }

    /** 将成员移动到另一 Set。 */
    @Override
    public boolean move(String destination, V member) {
        return get(moveAsync(destination, member));
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
        
        String tempName = suffixName(getRawName(), "redisson_temp");
        
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                    "for i=1, #ARGV, 5000 do " +
                              "redis.call('sadd', KEYS[2], unpack(ARGV, i, math.min(i+4999, #ARGV))); " +
                          "end; " +

                          "local size = redis.call('sdiff', KEYS[2], KEYS[1]);"
                        + "redis.call('del', KEYS[2]); "
                        + "return #size == 0 and 1 or 0; ",
                       Arrays.<Object>asList(getRawName(), tempName), encode(c).toArray());
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
        
        List<Object> args = new ArrayList<Object>(c.size() + 1);
        args.add(getRawName());
        encode(args, c);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SADD_BOOL, args.toArray());
    }

    /** 异步执行 addAllCounted。 */
    @Override
    public RFuture<Integer> addAllCountedAsync(Collection<? extends V> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(0);
        }

        List<Object> args = new ArrayList<>(c.size() + 1);
        args.add(getRawName());
        encode(args, c);
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SADD, args.toArray());
    }

    /** addAllCounted：添加操作。 */
    @Override
    public int addAllCounted(Collection<? extends V> c) {
        return get(addAllCountedAsync(c));
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
        
        String tempName = suffixName(getRawName(), "redisson_temp");
        
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
            "for i=1, #ARGV, 5000 do " +
                      "redis.call('sadd', KEYS[2], unpack(ARGV, i, math.min(i+4999, #ARGV))); " +
                  "end; " +

                  "local prevSize = redis.call('scard', KEYS[1]); "
                + "local size = redis.call('sinterstore', KEYS[1], KEYS[1], KEYS[2]);"
                + "redis.call('del', KEYS[2]); "
                + "return size ~= prevSize and 1 or 0; ",
            Arrays.<Object>asList(getRawName(), tempName),
            encode(c).toArray());
    }

    /** 异步执行 removeAll。 */
    @Override
    public RFuture<Boolean> removeAllAsync(Collection<?> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(false);
        }
        
        List<Object> args = new ArrayList<Object>(c.size() + 1);
        args.add(getRawName());
        encode(args, c);
        
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SREM_SINGLE, args.toArray());
    }

    /** removeAllCounted：移除操作。 */
    @Override
    public int removeAllCounted(Collection<? extends V> c) {
        return get(removeAllCountedAsync(c));
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

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SREM, args.toArray());
    }

    /** 异步执行 containsEach。 */
    @Override
    public RFuture<Set<V>> containsEachAsync(Collection<V> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(Collections.<V>emptySet());
        }

        List<Object> args = new ArrayList<>(c.size() + 1);
        args.add(getRawName());
        encode(args, c);

        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE,
                new RedisCommand<>("SMISMEMBER", new ContainsSetDecoder<>(c)), args.toArray());
    }

    /** 批量移除元素。 */
    @Override
    public boolean removeAll(Collection<?> c) {
        return get(removeAllAsync(c));
    }

    /** 计算并存储集合并集。 */
    @Override
    public int union(String... names) {
        return get(unionAsync(names));
    }

    /** 异步计算 ZSet 并集。 */
    @Override
    public RFuture<Integer> unionAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SUNIONSTORE_INT, args.toArray());
    }

    /** Set readUnion 操作。 */
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
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SUNION, args.toArray());
    }

    /** Set diff 操作。 */
    @Override
    public int diff(String... names) {
        return get(diffAsync(names));
    }

    /** 异步执行 diff。 */
    @Override
    public RFuture<Integer> diffAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SDIFFSTORE_INT, args.toArray());
    }

    /** Set readDiff 操作。 */
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
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SDIFF, args.toArray());
    }

    /** 计算并存储集合交集。 */
    @Override
    public int intersection(String... names) {
        return get(intersectionAsync(names));
    }

    /** 异步计算 ZSet 交集。 */
    @Override
    public RFuture<Integer> intersectionAsync(String... names) {
        List<Object> args = new ArrayList<>(names.length + 1);
        args.add(getRawName());
        args.addAll(map(names));
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.SINTERSTORE_INT, args.toArray());
    }

    /** Set readIntersection 操作。 */
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
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SINTER, args.toArray());
    }
    
    /** Set countIntersection 操作。 */
    @Override
    public Integer countIntersection(String... names) {
        return get(countIntersectionAsync(names));
    }

    /** 异步执行 countIntersection。 */
    @Override
    public RFuture<Integer> countIntersectionAsync(String... names) {
        return countIntersectionAsync(0, names);
    }

    /** Set countIntersection 操作。 */
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

    /** Set countUnion 操作。 */
    @Override
    public Integer countUnion(String... names) {
        return get(countUnionAsync(names));
    }

    /** 异步执行 countUnion。 */
    @Override
    public RFuture<Integer> countUnionAsync(String... names) {
        return countUnionAsync(0, names);
    }

    /** Set countUnion 操作。 */
    @Override
    public Integer countUnion(int limit, String... names) {
        return get(countUnionAsync(limit, names));
    }

    /** 异步执行 countUnion。 */
    @Override
    public RFuture<Integer> countUnionAsync(int limit, String... names) {
        return unionCardAsync(false, limit, names);
    }

    /** Set countUnionApprox 操作。 */
    @Override
    public Integer countUnionApprox(String... names) {
        return get(countUnionApproxAsync(names));
    }

    /** 异步执行 countUnionApprox。 */
    @Override
    public RFuture<Integer> countUnionApproxAsync(String... names) {
        return countUnionApproxAsync(0, names);
    }

    /** Set countUnionApprox 操作。 */
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

    /** Set countDiff 操作。 */
    @Override
    public Integer countDiff(String... names) {
        return get(countDiffAsync(names));
    }

    /** 异步执行 countDiff。 */
    @Override
    public RFuture<Integer> countDiffAsync(String... names) {
        return countDiffAsync(0, names);
    }

    /** Set countDiff 操作。 */
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

    /** 清空全部元素。 */
    @Override
    public void clear() {
        delete();
    }

    @Override
    @SuppressWarnings("AvoidInlineConditionals")
    /** Set toString 操作。 */
    public String toString() {
        Iterator<V> it = iterator();
        if (! it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            V e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }
    
    /** Set readSort 操作。 */
    @Override
    public Set<V> readSort(SortOrder order) {
        return get(readSortAsync(order));
    }
    
    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(SortOrder order) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), order);
    }

    /** Set readSort 操作。 */
    @Override
    public Set<V> readSort(SortOrder order, int offset, int count) {
        return get(readSortAsync(order, offset, count));
    }
    
    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(SortOrder order, int offset, int count) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), "LIMIT", offset, count, order);
    }

    /** Set readSort 操作。 */
    @Override
    public Set<V> readSort(String byPattern, SortOrder order) {
        return get(readSortAsync(byPattern, order));
    }
    
    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(String byPattern, SortOrder order) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), "BY", byPattern, order);
    }
    
    /** Set readSort 操作。 */
    @Override
    public Set<V> readSort(String byPattern, SortOrder order, int offset, int count) {
        return get(readSortAsync(byPattern, order, offset, count));
    }
    
    /** 异步执行 readSort。 */
    @Override
    public RFuture<Set<V>> readSortAsync(String byPattern, SortOrder order, int offset, int count) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.SORT_SET, getRawName(), "BY", byPattern, "LIMIT", offset, count, order);
    }

    /** Set readSort 操作。 */
    @Override
    public <T> Collection<T> readSort(String byPattern, List<String> getPatterns, SortOrder order) {
        return (Collection<T>) get(readSortAsync(byPattern, getPatterns, order));
    }
    
    /** 异步执行 readSort。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order) {
        return readSortAsync(byPattern, getPatterns, order, -1, -1);
    }
    
    /** Set readSort 操作。 */
    @Override
    public <T> Collection<T> readSort(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return (Collection<T>) get(readSortAsync(byPattern, getPatterns, order, offset, count));
    }

    /** 异步执行 readSort。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return readSortAsync(byPattern, getPatterns, order, offset, count, false);
    }

    /** Set readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(SortOrder order) {
        return get(readSortAlphaAsync(order));
    }

    /** Set readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(SortOrder order, int offset, int count) {
        return get(readSortAlphaAsync(order, offset, count));
    }

    /** Set readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(String byPattern, SortOrder order) {
        return get(readSortAlphaAsync(byPattern, order));
    }

    /** Set readSortAlpha 操作。 */
    @Override
    public Set<V> readSortAlpha(String byPattern, SortOrder order, int offset, int count) {
        return get(readSortAlphaAsync(byPattern, order, offset, count));
    }

    /** Set readSortAlpha 操作。 */
    @Override
    public <T> Collection<T> readSortAlpha(String byPattern, List<String> getPatterns, SortOrder order) {
        return (Collection<T>) get(readSortAlphaAsync(byPattern, getPatterns, order));
    }

    /** Set readSortAlpha 操作。 */
    @Override
    public <T> Collection<T> readSortAlpha(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return (Collection<T>) get(readSortAlphaAsync(byPattern, getPatterns, order, offset, count));
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

    /** Set sortTo 操作。 */
    @Override
    public int sortTo(String destName, SortOrder order) {
        return get(sortToAsync(destName, order));
    }
    
    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, SortOrder order) {
        return sortToAsync(destName, null, Collections.<String>emptyList(), order, -1, -1);
    }
    
    /** Set sortTo 操作。 */
    @Override
    public int sortTo(String destName, SortOrder order, int offset, int count) {
        return get(sortToAsync(destName, order, offset, count));
    }
    
    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, SortOrder order, int offset, int count) {
        return sortToAsync(destName, null, Collections.<String>emptyList(), order, offset, count);
    }

    /** Set sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, SortOrder order, int offset, int count) {
        return get(sortToAsync(destName, byPattern, order, offset, count));
    }
    
    /** Set sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, SortOrder order) {
        return get(sortToAsync(destName, byPattern, order));
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order) {
        return sortToAsync(destName, byPattern, Collections.<String>emptyList(), order, -1, -1);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order, int offset, int count) {
        return sortToAsync(destName, byPattern, Collections.<String>emptyList(), order, offset, count);
    }

    /** Set sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, List<String> getPatterns, SortOrder order) {
        return get(sortToAsync(destName, byPattern, getPatterns, order));
    }
    
    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order) {
        return sortToAsync(destName, byPattern, getPatterns, order, -1, -1);
    }
    
    /** Set sortTo 操作。 */
    @Override
    public int sortTo(String destName, String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return get(sortToAsync(destName, byPattern, getPatterns, order, offset, count));
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        List<Object> params = new ArrayList<Object>();
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

    /** Set tryAdd 操作。 */
    @Override
    public boolean tryAdd(V... values) {
        return get(tryAddAsync(values));
    }

    /** Set containsEach 操作。 */
    @Override
    public Set<V> containsEach(Collection<V> c) {
        return get(containsEachAsync(c));
    }

    /** 异步执行 tryAdd。 */
    @Override
    public RFuture<Boolean> tryAddAsync(V... values) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                  "for i, v in ipairs(ARGV) do " +
                            "if redis.call('sismember', KEYS[1], v) == 1 then " +
                                "return 0; " +
                            "end; " +
                        "end; " +

                        "for i=1, #ARGV, 5000 do " +
                            "redis.call('sadd', KEYS[1], unpack(ARGV, i, math.min(i+4999, #ARGV))); " +
                        "end; " +
                        "return 1; ",
                Arrays.asList(getRawName()), encode(Arrays.asList(values)).toArray());
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

    /** 异步 SSCAN 迭代器。 */
    @Override
    public RFuture<ScanResult<Object>> scanIteratorAsync(String name, RedisClient client, String startPos,
            String pattern, int count) {
        if (pattern == null) {
            return commandExecutor.readAsync(client, name, codec, RedisCommands.SSCAN, name, startPos, "COUNT", count);
        }

        return commandExecutor.readAsync(client, name, codec, RedisCommands.SSCAN, name, startPos, "MATCH", pattern, "COUNT", count);
    }

    /** 异步执行 readSort。 */
    private <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count, boolean alpha) {
        List<Object> params = new ArrayList<Object>();
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

    /** Set stream 操作。 */
    @Override
    public Stream<V> stream(int count) {
        return toStream(iterator(count));
    }

    /** Set stream 操作。 */
    @Override
    public Stream<V> stream(String pattern, int count) {
        return toStream(iterator(pattern, count));
    }

    /** Set stream 操作。 */
    @Override
    public Stream<V> stream(String pattern) {
        return toStream(iterator(pattern));
    }

    /** 注册对象变更监听器。 */
    @Override
    public int addListener(ObjectListener listener) {
        if (listener instanceof SetAddListener) {
            return addListener("__keyevent@*:sadd", (SetAddListener) listener, SetAddListener::onAdd);
        }
        if (listener instanceof SetRemoveListener) {
            return addListener("__keyevent@*:srem", (SetRemoveListener) listener, SetRemoveListener::onRemove);
        }
        if (listener instanceof SetRemoveRandomListener) {
            return addListener("__keyevent@*:spop", (SetRemoveRandomListener) listener, SetRemoveRandomListener::onRandomRemove);
        }
        if (listener instanceof SetInterStoreListener) {
            return addListener("__keyevent@*:sinterstore", (SetInterStoreListener) listener, SetInterStoreListener::onStore);
        }
        if (listener instanceof SetUnionStoreListener) {
            return addListener("__keyevent@*:sunionstore", (SetUnionStoreListener) listener, SetUnionStoreListener::onStore);
        }
        if (listener instanceof SetDiffStoreListener) {
            return addListener("__keyevent@*:sdiffstore", (SetDiffStoreListener) listener, SetDiffStoreListener::onStore);
        }
        if (listener instanceof TrackingListener) {
            return addTrackingListener((TrackingListener) listener);
        }

        return super.addListener(listener);
    }

    /** 异步执行 addListener。 */
    @Override
    public RFuture<Integer> addListenerAsync(ObjectListener listener) {
        if (listener instanceof SetAddListener) {
            return addListenerAsync("__keyevent@*:sadd", (SetAddListener) listener, SetAddListener::onAdd);
        }
        if (listener instanceof SetRemoveListener) {
            return addListenerAsync("__keyevent@*:srem", (SetRemoveListener) listener, SetRemoveListener::onRemove);
        }
        if (listener instanceof SetRemoveRandomListener) {
            return addListenerAsync("__keyevent@*:spop", (SetRemoveRandomListener) listener, SetRemoveRandomListener::onRandomRemove);
        }
        if (listener instanceof SetInterStoreListener) {
            return addListenerAsync("__keyevent@*:sinterstore", (SetInterStoreListener) listener, SetInterStoreListener::onStore);
        }
        if (listener instanceof SetUnionStoreListener) {
            return addListenerAsync("__keyevent@*:sunionstore", (SetUnionStoreListener) listener, SetUnionStoreListener::onStore);
        }
        if (listener instanceof SetDiffStoreListener) {
            return addListenerAsync("__keyevent@*:sdiffstore", (SetDiffStoreListener) listener, SetDiffStoreListener::onStore);
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
        removeListener(listenerId, "__keyevent@*:sadd", "__keyevent@*:srem", "__keyevent@*:spop", "__keyevent@*:sinterstore", "__keyevent@*:sunionstore", "__keyevent@*:sdiffstore");
        super.removeListener(listenerId);
    }

    /** 异步执行 removeListener。 */
    @Override
    public RFuture<Void> removeListenerAsync(int listenerId) {
        return removeListenerAsync(removeTrackingListenerAsync(listenerId), listenerId,
                "__keyevent@*:sadd", "__keyevent@*:srem", "__keyevent@*:spop", "__keyevent@*:sinterstore", "__keyevent@*:sunionstore", "__keyevent@*:sdiffstore");
    }


}
