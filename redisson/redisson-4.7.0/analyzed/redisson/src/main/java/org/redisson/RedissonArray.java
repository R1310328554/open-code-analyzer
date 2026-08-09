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

import org.redisson.api.AsyncIterator;
import org.redisson.api.RArray;
import org.redisson.api.RFuture;
import org.redisson.api.array.ArrayEntry;
import org.redisson.api.array.ArrayFullInfo;
import org.redisson.api.array.ArrayGrepArgs;
import org.redisson.api.array.ArrayGrepParams;
import org.redisson.api.array.ArrayInfo;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

/**
 * Redis {@code ARRAY} 类型（Redis 8+）的 {@link RArray} 实现。
 * <p>支持按索引读写、范围查询、追加与批量操作。
 *
 * @param <V> 元素类型
 * @author lamnt2008
 * @author Nikita Koksharov
 */
public class RedissonArray<V> extends RedissonExpirable implements RArray<V> {

    private static final String START_BOUND = "-";
    private static final String END_BOUND = "+";

    public RedissonArray(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
    }

    public RedissonArray(Codec codec, CommandAsyncExecutor commandExecutor, String name) {
        super(codec, commandExecutor, name);
    }

    /** 返回底层 Native MapCache 实例。 */
    @Override
    public V get(long index) {
        return get(getAsync(index));
    }

    /** 异步获取  对象或执行  操作。 */
    @Override
    public RFuture<V> getAsync(long index) {
        checkIndex(index);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ARGET, getRawName(), index);
    }

    /** 返回底层 Native MapCache 实例。 */
    @Override
    public List<V> get(long... indexes) {
        return get(getAsync(indexes));
    }

    /** 异步获取  对象或执行  操作。 */
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

    /** 是否Set。 */
    @Override
    public boolean isSet(long index) {
        return get(isSetAsync(index));
    }

    /** 是否SetAsync。 */
    @Override
    public RFuture<Boolean> isSetAsync(long index) {
        checkIndex(index);
        CompletionStage<Boolean> f = countAsync(index, index).thenApply(c -> c != null && c > 0);
        return new CompletableFutureWrapper<>(f);
    }

    /** 列表/数组 iterator 操作。 */
    @Override
    public Iterator<ArrayEntry<V>> iterator() {
        return iterator(10);
    }

    /** 列表/数组 iterator 操作。 */
    @Override
    public Iterator<ArrayEntry<V>> iterator(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        return new Iterator<ArrayEntry<V>>() {

            private Iterator<ArrayEntry<V>> buffer = Collections.emptyIterator();
            private long nextStart;
            private long endBound;
            private boolean endResolved;
            private boolean finished;

            private void fill() {
                while (!buffer.hasNext() && !finished) {
                    if (!endResolved) {
                        endBound = length() - 1;
                        endResolved = true;
                    }
                    if (nextStart > endBound) {
                        finished = true;
                        return;
                    }
                    List<ArrayEntry<V>> page = scan(nextStart, endBound, count);
                    if (page.isEmpty()) {
                        finished = true;
                        return;
                    }
                    nextStart = page.get(page.size() - 1).getIndex() + 1;
                    buffer = page.iterator();
                }
            }

            @Override
            public boolean hasNext() {
                fill();
                return buffer.hasNext();
            }

            @Override
            public ArrayEntry<V> next() {
                fill();
                if (!buffer.hasNext()) {
                    throw new NoSuchElementException();
                }
                return buffer.next();
            }
        };
    }

    /** 异步执行 iterator。 */
    @Override
    public AsyncIterator<ArrayEntry<V>> iteratorAsync() {
        return iteratorAsync(10);
    }

    /** 异步执行 iterator。 */
    @Override
    public AsyncIterator<ArrayEntry<V>> iteratorAsync(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        return new AsyncIterator<ArrayEntry<V>>() {

            private Iterator<ArrayEntry<V>> buffer = Collections.emptyIterator();
            private long nextStart;
            private long endBound;
            private boolean endResolved;
            private boolean finished;

            @Override
            public CompletionStage<Boolean> hasNext() {
                if (buffer.hasNext()) {
                    return CompletableFuture.completedFuture(true);
                }
                if (finished) {
                    return CompletableFuture.completedFuture(false);
                }
                return fill();
            }

            private CompletionStage<Boolean> fill() {
                CompletionStage<Long> endStage;
                if (!endResolved) {
                    endStage = lengthAsync().thenApply(len -> {
                        endBound = len - 1;
                        endResolved = true;
                        return endBound;
                    });
                } else {
                    endStage = CompletableFuture.completedFuture(endBound);
                }
                return endStage.thenCompose(end -> {
                    if (nextStart > end) {
                        finished = true;
                        return CompletableFuture.completedFuture(false);
                    }
                    return scanAsync(nextStart, end, count).thenCompose(page -> {
                        if (page.isEmpty()) {
                            finished = true;
                            return CompletableFuture.completedFuture(false);
                        }
                        nextStart = page.get(page.size() - 1).getIndex() + 1;
                        buffer = page.iterator();
                        if (buffer.hasNext()) {
                            return CompletableFuture.completedFuture(true);
                        }
                        return fill();
                    });
                });
            }

            @Override
            public CompletionStage<ArrayEntry<V>> next() {
                return hasNext().thenApply(has -> {
                    if (!has) {
                        throw new NoSuchElementException();
                    }
                    return buffer.next();
                });
            }
        };
    }

    /** 列表/数组 stream 操作。 */
    @Override
    public Stream<ArrayEntry<V>> stream() {
        return toStream(iterator());
    }

    /** 按索引或键写入元素/值。 */
    @Override
    public long set(long index, V value) {
        return get(setAsync(index, value));
    }

    /** 设置Async。 */
    @Override
    public RFuture<Long> setAsync(long index, V value) {
        checkIndex(index);
        Objects.requireNonNull(value, "Value can't be null");
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE,
                RedisCommands.ARSET, getRawName(), index, encode(value));
    }

    /** 按索引或键写入元素/值。 */
    @Override
    public long set(long index, V... values) {
        return get(setAsync(index, values));
    }

    /** 设置Async。 */
    @Override
    public RFuture<Long> setAsync(long index, V... values) {
        checkIndex(index);
        validateValues(values);
        if (values.length == 0) {
            return new CompletableFutureWrapper<>(0L);
        }

        List<Object> args = new ArrayList<>(values.length + 2);
        args.add(getRawName());
        args.add(index);
        encode(args, Arrays.asList(values));
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARSET, args.toArray());
    }

    /** 按索引或键写入元素/值。 */
    @Override
    public long set(Map<Long, V> entries) {
        return get(setAsync(entries));
    }

    /** 设置Async。 */
    @Override
    public RFuture<Long> setAsync(Map<Long, V> entries) {
        Objects.requireNonNull(entries, "Entries can't be null");
        if (entries.isEmpty()) {
            return new CompletableFutureWrapper<>(0L);
        }

        for (Map.Entry<Long, V> entry : entries.entrySet()) {
            checkIndex(entry.getKey());
            Objects.requireNonNull(entry.getValue(), "Value can't be null");
        }

        List<Object> args = new ArrayList<>(entries.size() * 2 + 1);
        args.add(getRawName());
        for (Map.Entry<Long, V> entry : entries.entrySet()) {
            args.add(entry.getKey());
            encode(args, entry.getValue());
        }
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARMSET, args.toArray());
    }

    /** 删除键或 Session。 */
    @Override
    public long delete(long... indexes) {
        return get(deleteAsync(indexes));
    }

    /** 异步执行 delete。 */
    @Override
    public RFuture<Long> deleteAsync(long... indexes) {
        if (indexes.length == 0) {
            return new CompletableFutureWrapper<>(0L);
        }

        List<Object> args = new ArrayList<>(indexes.length + 1);
        args.add(getRawName());
        for (long index : indexes) {
            checkIndex(index);
            args.add(index);
        }
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARDEL, args.toArray());
    }

    /** 列表/数组 deleteRange 操作。 */
    @Override
    public long deleteRange(long startIndex, long endIndex) {
        return get(deleteRangeAsync(startIndex, endIndex));
    }

    /** 异步执行 deleteRange。 */
    @Override
    public RFuture<Long> deleteRangeAsync(long startIndex, long endIndex) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE,
                RedisCommands.ARDELRANGE, getRawName(), startIndex, endIndex);
    }

    /** 列表/数组 deleteRanges 操作。 */
    @Override
    public long deleteRanges(long... startEndIndexes) {
        return get(deleteRangesAsync(startEndIndexes));
    }

    /** 异步执行 deleteRanges。 */
    @Override
    public RFuture<Long> deleteRangesAsync(long... startEndIndexes) {
        if (startEndIndexes.length == 0) {
            return new CompletableFutureWrapper<>(0L);
        }
        if (startEndIndexes.length % 2 != 0) {
            throw new IllegalArgumentException("Ranges should contain start and end index pairs");
        }

        List<Object> args = new ArrayList<>(startEndIndexes.length + 1);
        args.add(getRawName());
        for (long index : startEndIndexes) {
            checkIndex(index);
            args.add(index);
        }
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARDELRANGE, args.toArray());
    }

    /** 统计可能包含的元素数量。 */
    @Override
    public long count() {
        return get(countAsync());
    }

    /** 异步执行 count。 */
    @Override
    public RFuture<Long> countAsync() {
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARCOUNT, getRawName());
    }

    /** 统计可能包含的元素数量。 */
    @Override
    public long count(long startIndex, long endIndex) {
        return get(countAsync(startIndex, endIndex));
    }

    /** 异步执行 count。 */
    @Override
    public RFuture<Long> countAsync(long startIndex, long endIndex) {
        return operationAsync(startIndex, endIndex, "USED");
    }

    /** 列表/数组 countMatches 操作。 */
    @Override
    public long countMatches(long startIndex, long endIndex, V value) {
        return get(countMatchesAsync(startIndex, endIndex, value));
    }

    /** 异步执行 countMatches。 */
    @Override
    public RFuture<Long> countMatchesAsync(long startIndex, long endIndex, V value) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        Objects.requireNonNull(value, "Value can't be null");
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.AROP_LONG,
                getRawName(), startIndex, endIndex, "MATCH", encode(value));
    }

    /** 列表/数组 length 操作。 */
    @Override
    public long length() {
        return get(lengthAsync());
    }

    /** 异步执行 length。 */
    @Override
    public RFuture<Long> lengthAsync() {
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARLEN, getRawName());
    }

    /** 列表/数组 range 操作。 */
    @Override
    public List<V> range(long startIndex, long endIndex) {
        return get(rangeAsync(startIndex, endIndex));
    }

    /** 异步执行 range。 */
    @Override
    public RFuture<List<V>> rangeAsync(long startIndex, long endIndex) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ARGETRANGE, getRawName(), startIndex, endIndex);
    }

    /** 列表/数组 scan 操作。 */
    @Override
    public List<ArrayEntry<V>> scan(long startIndex, long endIndex) {
        return get(scanAsync(startIndex, endIndex));
    }

    /** 异步执行 scan。 */
    @Override
    public RFuture<List<ArrayEntry<V>>> scanAsync(long startIndex, long endIndex) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ARSCAN, getRawName(), startIndex, endIndex);
    }

    /** 列表/数组 scan 操作。 */
    @Override
    public List<ArrayEntry<V>> scan(long startIndex, long endIndex, long limit) {
        return get(scanAsync(startIndex, endIndex, limit));
    }

    /** 异步执行 scan。 */
    @Override
    public RFuture<List<ArrayEntry<V>>> scanAsync(long startIndex, long endIndex, long limit) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        return commandExecutor.readAsync(getRawName(), codec,
                RedisCommands.ARSCAN, getRawName(), startIndex, endIndex, "LIMIT", limit);
    }

    /** 列表/数组 insert 操作。 */
    @Override
    public long insert(V... values) {
        return get(insertAsync(values));
    }

    /** 异步执行 insert。 */
    @Override
    public RFuture<Long> insertAsync(V... values) {
        validateValues(values);
        if (values.length == 0) {
            throw new IllegalArgumentException("Values can't be empty");
        }

        List<Object> args = new ArrayList<>(values.length + 1);
        args.add(getRawName());
        encode(args, Arrays.asList(values));
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARINSERT, args.toArray());
    }

    /** 列表/数组 ring 操作。 */
    @Override
    public long ring(long size, V... values) {
        return get(ringAsync(size, values));
    }

    /** 异步执行 ring。 */
    @Override
    public RFuture<Long> ringAsync(long size, V... values) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        validateValues(values);
        if (values.length == 0) {
            throw new IllegalArgumentException("Values can't be empty");
        }

        List<Object> args = new ArrayList<>(values.length + 2);
        args.add(getRawName());
        args.add(size);
        encode(args, Arrays.asList(values));
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARRING, args.toArray());
    }

    /** 列表/数组 next 操作。 */
    @Override
    public Long next() {
        return get(nextAsync());
    }

    /** 异步执行 next。 */
    @Override
    public RFuture<Long> nextAsync() {
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARNEXT, getRawName());
    }

    /** 列表/数组 seek 操作。 */
    @Override
    public boolean seek(long index) {
        return get(seekAsync(index));
    }

    /** 异步执行 seek。 */
    @Override
    public RFuture<Boolean> seekAsync(long index) {
        checkIndex(index);
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARSEEK, getRawName(), index);
    }

    /** 列表/数组 lastItems 操作。 */
    @Override
    public List<V> lastItems(long count) {
        return get(lastItemsAsync(count));
    }

    /** 异步执行 lastItems。 */
    @Override
    public RFuture<List<V>> lastItemsAsync(long count) {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.ARLASTITEMS, getRawName(), count);
    }

    /** 列表/数组 lastItemsReversed 操作。 */
    @Override
    public List<V> lastItemsReversed(long count) {
        return get(lastItemsReversedAsync(count));
    }

    /** 异步执行 lastItemsReversed。 */
    @Override
    public RFuture<List<V>> lastItemsReversedAsync(long count) {
        return commandExecutor.readAsync(getRawName(), codec,
                RedisCommands.ARLASTITEMS, getRawName(), count, "REV");
    }

    /** 获取 Info。 */
    @Override
    public ArrayInfo getInfo() {
        return get(getInfoAsync());
    }

    /** 异步获取 Info 对象或执行 Info 操作。 */
    @Override
    public RFuture<ArrayInfo> getInfoAsync() {
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARINFO, getRawName());
    }

    /** 获取 FullInfo。 */
    @Override
    public ArrayFullInfo getFullInfo() {
        return get(getFullInfoAsync());
    }

    /** 异步获取 FullInfo 对象或执行 FullInfo 操作。 */
    @Override
    public RFuture<ArrayFullInfo> getFullInfoAsync() {
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.ARINFO_FULL, getRawName(), "FULL");
    }

    /** 列表/数组 grep 操作。 */
    @Override
    public List<Long> grep(ArrayGrepArgs args) {
        return get(grepAsync(args));
    }

    /** 异步执行 grep。 */
    @Override
    public RFuture<List<Long>> grepAsync(ArrayGrepArgs args) {
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE,
                RedisCommands.ARGREP, createGrepArgs(START_BOUND, END_BOUND, args, false).toArray());
    }

    /** 列表/数组 grep 操作。 */
    @Override
    public List<Long> grep(long startIndex, long endIndex, ArrayGrepArgs args) {
        return get(grepAsync(startIndex, endIndex, args));
    }

    /** 异步执行 grep。 */
    @Override
    public RFuture<List<Long>> grepAsync(long startIndex, long endIndex, ArrayGrepArgs args) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE,
                RedisCommands.ARGREP, createGrepArgs(startIndex, endIndex, args, false).toArray());
    }

    /** 列表/数组 grepEntries 操作。 */
    @Override
    public List<ArrayEntry<V>> grepEntries(ArrayGrepArgs args) {
        return get(grepEntriesAsync(args));
    }

    /** 异步执行 grepEntries。 */
    @Override
    public RFuture<List<ArrayEntry<V>>> grepEntriesAsync(ArrayGrepArgs args) {
        return commandExecutor.readAsync(getRawName(), codec,
                RedisCommands.ARGREP_WITHVALUES, createGrepArgs(START_BOUND, END_BOUND, args, true).toArray());
    }

    /** 列表/数组 grepEntries 操作。 */
    @Override
    public List<ArrayEntry<V>> grepEntries(long startIndex, long endIndex, ArrayGrepArgs args) {
        return get(grepEntriesAsync(startIndex, endIndex, args));
    }

    /** 异步执行 grepEntries。 */
    @Override
    public RFuture<List<ArrayEntry<V>>> grepEntriesAsync(long startIndex, long endIndex, ArrayGrepArgs args) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        return commandExecutor.readAsync(getRawName(), codec,
                RedisCommands.ARGREP_WITHVALUES, createGrepArgs(startIndex, endIndex, args, true).toArray());
    }

    /** 列表/数组 sum 操作。 */
    @Override
    public Double sum(long startIndex, long endIndex) {
        return get(sumAsync(startIndex, endIndex));
    }

    /** 异步执行 sum。 */
    @Override
    public RFuture<Double> sumAsync(long startIndex, long endIndex) {
        return doubleOperationAsync(startIndex, endIndex, "SUM");
    }

    /** 列表/数组 min 操作。 */
    @Override
    public Double min(long startIndex, long endIndex) {
        return get(minAsync(startIndex, endIndex));
    }

    /** 异步执行 min。 */
    @Override
    public RFuture<Double> minAsync(long startIndex, long endIndex) {
        return doubleOperationAsync(startIndex, endIndex, "MIN");
    }

    /** 列表/数组 max 操作。 */
    @Override
    public Double max(long startIndex, long endIndex) {
        return get(maxAsync(startIndex, endIndex));
    }

    /** 异步执行 max。 */
    @Override
    public RFuture<Double> maxAsync(long startIndex, long endIndex) {
        return doubleOperationAsync(startIndex, endIndex, "MAX");
    }

    /** 列表/数组 bitAnd 操作。 */
    @Override
    public Long bitAnd(long startIndex, long endIndex) {
        return get(bitAndAsync(startIndex, endIndex));
    }

    /** 异步执行 bitAnd。 */
    @Override
    public RFuture<Long> bitAndAsync(long startIndex, long endIndex) {
        return operationAsync(startIndex, endIndex, "AND");
    }

    /** 列表/数组 bitOr 操作。 */
    @Override
    public Long bitOr(long startIndex, long endIndex) {
        return get(bitOrAsync(startIndex, endIndex));
    }

    /** 异步执行 bitOr。 */
    @Override
    public RFuture<Long> bitOrAsync(long startIndex, long endIndex) {
        return operationAsync(startIndex, endIndex, "OR");
    }

    /** 列表/数组 bitXor 操作。 */
    @Override
    public Long bitXor(long startIndex, long endIndex) {
        return get(bitXorAsync(startIndex, endIndex));
    }

    /** 异步执行 bitXor。 */
    @Override
    public RFuture<Long> bitXorAsync(long startIndex, long endIndex) {
        return operationAsync(startIndex, endIndex, "XOR");
    }

    /** 异步执行 doubleOperation。 */
    private RFuture<Double> doubleOperationAsync(long startIndex, long endIndex, String operation) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE,
                RedisCommands.AROP_DOUBLE, getRawName(), startIndex, endIndex, operation);
    }

    /** 异步执行 operation。 */
    private RFuture<Long> operationAsync(long startIndex, long endIndex, String operation) {
        checkIndex(startIndex);
        checkIndex(endIndex);
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE,
                RedisCommands.AROP_LONG, getRawName(), startIndex, endIndex, operation);
    }

    /** 列表/数组 createGrepArgs 操作。 */
    private List<Object> createGrepArgs(Object startIndex, Object endIndex, ArrayGrepArgs args, boolean withValues) {
        Objects.requireNonNull(args, "Args can't be null");

        ArrayGrepParams params = (ArrayGrepParams) args;
        if (params.getPredicates().isEmpty()) {
            throw new IllegalArgumentException("At least one grep predicate is required");
        }

        List<Object> result = new ArrayList<>();
        encode(result, commandArgs -> {
            commandArgs.add(getRawName());
            commandArgs.add(startIndex);
            commandArgs.add(endIndex);
            for (ArrayGrepParams.Predicate predicate : params.getPredicates()) {
                commandArgs.add(predicate.getType().name());
                addGrepValue(commandArgs, predicate);
            }
            if (params.getAnd() != null) {
                if (params.getAnd()) {
                    commandArgs.add("AND");
                } else {
                    commandArgs.add("OR");
                }
            }
            if (params.getLimit() != null) {
                commandArgs.add("LIMIT");
                commandArgs.add(params.getLimit());
            }
            if (withValues) {
                commandArgs.add("WITHVALUES");
            }
            if (params.isNoCase()) {
                commandArgs.add("NOCASE");
            }
        });
        return result;
    }

    /** addGrepValue：添加操作。 */
    private void addGrepValue(Collection<Object> args, ArrayGrepParams.Predicate predicate) {
        if (predicate.getType() == ArrayGrepParams.Predicate.Type.EXACT
                || predicate.getType() == ArrayGrepParams.Predicate.Type.MATCH) {
            encode(args, predicate.getValue());
            return;
        }
        args.add(predicate.getValue());
    }

    /** 列表/数组 validateValues 操作。 */
    private void validateValues(V[] values) {
        Objects.requireNonNull(values, "Values can't be null");
        for (V value : values) {
            Objects.requireNonNull(value, "Value can't be null");
        }
    }

    /** 列表/数组 checkIndex 操作。 */
    private void checkIndex(Long index) {
        Objects.requireNonNull(index, "Index can't be null");
        checkIndex(index.longValue());
    }

    /** 列表/数组 checkIndex 操作。 */
    private void checkIndex(long index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index must be non-negative");
        }
    }

}
