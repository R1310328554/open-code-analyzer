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
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.api.queue.QueueMoveElementsArgs;
import org.redisson.api.queue.QueueMoveElementsParams;
import org.redisson.misc.CompletableFutureWrapper;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.redisson.client.protocol.RedisCommands.LINDEX;
import static org.redisson.client.protocol.RedisCommands.LLEN_INT;

/**
 * 基于 Redis List 的二分有序 {@link RPriorityQueue} 实现。
 * <p>按 Comparator 维护升序，支持 offer/poll/peek 及阻塞变体。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public class RedissonPriorityQueue<V> extends BaseRedissonList<V> implements RPriorityQueue<V> {

    public static class BinarySearchResult<V> {

        /** 二分查找命中的元素值。 */
        private V value;
        /** 二分查找命中的列表索引，-1 表示未找到。 */
        private int index = -1;

        public BinarySearchResult(V value) {
            super();
            this.value = value;
        }

        public BinarySearchResult() {
        }

        public void setIndex(int index) {
            this.index = index;
        }
        public int getIndex() {
            return index;
        }

        public V getValue() {
            return value;
        }


    }

    private Comparator comparator = Comparator.naturalOrder();

    CommandAsyncExecutor commandExecutor;
    
    RLock lock;
    private RBucket<String> comparatorHolder;

    public RedissonPriorityQueue(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(commandExecutor, name, redisson);
        this.commandExecutor = commandExecutor;

        comparatorHolder = redisson.getBucket(getComparatorKeyName(), StringCodec.INSTANCE);
        lock = redisson.getLock(getLockName());
    }

    public RedissonPriorityQueue(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(codec, commandExecutor, name, redisson);
        this.commandExecutor = commandExecutor;

        comparatorHolder = redisson.getBucket(getComparatorKeyName(), StringCodec.INSTANCE);
        lock = redisson.getLock(getLockName());
    }

    /** 获取 LockName。 */
    private String getLockName() {
        return prefixName("redisson_sortedset_lock", getName());
    }

    /** 优先级队列 loadComparator 操作。 */
    private void loadComparator() {
        try {
            String comparatorSign = comparatorHolder.get();
            if (comparatorSign != null) {
                String[] parts = comparatorSign.split(":");
                String className = parts[0];
                String sign = parts[1];

                String result = calcClassSign(className);
                if (!result.equals(sign)) {
                    throw new IllegalStateException("Local class signature of " + className + " differs from used by this SortedSet!");
                }

                Class<?> clazz = Class.forName(className);
                comparator = (Comparator<V>) clazz.newInstance();
            } else {
                throw new IllegalStateException("Comparator is not set!");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // TODO cache result
    private static String calcClassSign(String name) {
        try {
            Class<?> clazz = Class.forName(name);

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(result);
            outputStream.writeObject(clazz);
            outputStream.close();

            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(result.toByteArray());

            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (Exception e) {
            throw new IllegalStateException("Can't calculate sign of " + name, e);
        }
    }

    /** 按优先级插入元素。 */
    @Override
    public boolean offer(V e) {
        return add(e);
    }

    /** 是否包含指定元素。 */
    @Override
    public boolean contains(Object o) {
        checkComparator();
        return binarySearch((V) o).getIndex() >= 0;
    }

    /** 添加元素/成员。 */
    @Override
    public boolean add(V value) {
        lock.lock();
        
        try {
            checkComparator();
    
            BinarySearchResult<V> res = binarySearch(value, false);
            int index = 0;
            if (res.getIndex() < 0) {
                index = -(res.getIndex() + 1);
            } else {
                index = res.getIndex() + 1;
            }
                
            get(commandExecutor.evalWriteNoRetryAsync(getRawName(), codec, RedisCommands.EVAL_VOID,
               "local len = redis.call('llen', KEYS[1]);"
                + "if tonumber(ARGV[1]) < len then "
                    + "local pivot = redis.call('lindex', KEYS[1], ARGV[1]);"
                    + "redis.call('linsert', KEYS[1], 'before', pivot, ARGV[2]);"
                    + "return;"
                + "end;"
                + "redis.call('rpush', KEYS[1], ARGV[2]);", 
                    Arrays.asList(getRawName()),
                    index, encode(value)));
            return true;
        } finally {
            lock.unlock();
        }
    }

    V getValue(int index) {
        return getValue(index, false);
    }

    V getValue(int index, boolean readOnlyMode) {
        if (readOnlyMode) return get(commandExecutor.readAsync(getRawName(), codec, LINDEX, getRawName(), index));
        return get(commandExecutor.writeAsync(getRawName(), codec, LINDEX, getRawName(), index));
    }

    /** 返回元素数量。 */
    @Override
    public int size() {
        return get(sizeAsync());
    }

    int size(boolean readOnlyMode) {
        if (readOnlyMode) return get(commandExecutor.readAsync(getRawName(), codec, LLEN_INT, getRawName()));
        return get(commandExecutor.writeAsync(getRawName(), codec, LLEN_INT, getRawName()));
    }

    /** 优先级队列 checkComparator 操作。 */
    private void checkComparator() {
        String comparatorSign = comparatorHolder.get();
        if (comparatorSign != null) {
            String[] vals = comparatorSign.split(":");
            String className = vals[0];
            if (!comparator.getClass().getName().equals(className)) {
                loadComparator();
            }
        }
    }

    /** 移除元素。 */
    @Override
    public boolean remove(Object value) {
        lock.lock();

        try {
            checkComparator();
            
            BinarySearchResult<V> res = binarySearch((V) value);
            if (res.getIndex() < 0) {
                return false;
            }

            remove((int) res.getIndex());
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** 是否包含全部给定元素。 */
    @Override
    public boolean containsAll(Collection<?> c) {
        checkComparator();
        for (Object object : c) {
            if (binarySearch((V) object).getIndex() < 0) {
                return false;
            }
        }
        return true;
    }

    /** 批量添加元素。 */
    @Override
    public boolean addAll(Collection<? extends V> c) {
        boolean changed = false;
        for (V v : c) {
            if (add(v)) {
                changed = true;
            }
        }
        return changed;
    }

    /** 仅保留与给定集合的交集。 */
    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (Iterator<?> iterator = iterator(); iterator.hasNext();) {
            Object object = (Object) iterator.next();
            if (!c.contains(object)) {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    /** 批量移除元素。 */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object obj : c) {
            if (remove(obj)) {
                changed = true;
            }
        }
        return changed;
    }

    /** 清空全部元素。 */
    @Override
    public void clear() {
        delete();
    }

    /** 返回元素比较器。 */
    @Override
    public Comparator<? super V> comparator() {
        return comparator;
    }

    /** 异步执行 poll。 */
    @Override
    public RFuture<V> pollAsync() {
        return wrapLockedAsync(RedisCommands.LPOP, getRawName());
    }

    /** 异步执行 wrapLocked。 */
    protected final <T> RFuture<V> wrapLockedAsync(RedisCommand<T> command, Object... params) {
        CompletionStage<V> f = sizeAsync().thenCompose(r -> {
            if (r > 0) {
                return wrapLockedAsync(() -> {
                    return commandExecutor.writeAsync(getRawName(), codec, command, params);
                });
            }
            return CompletableFuture.completedFuture(null);
        });
        return new CompletableFutureWrapper<>(f);
    }

    /** 异步执行 wrapLocked。 */
    protected final <T, R> RFuture<R> wrapLockedAsync(Supplier<RFuture<R>> callable) {
        long randomId = getServiceManager().getRandom().nextLong();
        CompletionStage<R> f = lock.lockAsync(randomId).thenCompose(r -> {
            RFuture<R> callback = callable.get();
            return callback.handle((value, ex) -> {
                CompletableFuture<R> result = new CompletableFuture<>();
                lock.unlockAsync(randomId)
                        .whenComplete((r2, ex2) -> {
                            if (ex2 != null) {
                                if (ex != null) {
                                    ex2.addSuppressed(ex);
                                }
                                result.completeExceptionally(ex2);
                                return;
                            }
                            if (ex != null) {
                                result.completeExceptionally(ex);
                                return;
                            }
                            result.complete(value);
                        });
                return result;
            }).thenCompose(ff -> ff);
        });
        return new CompletableFutureWrapper<>(f);
    }

    /** 获取 First。 */
    public V getFirst() {
        V value = getValue(0);
        if (value == null) {
            throw new NoSuchElementException();
        }
        return value;
    }

    /** 移除并返回队首/最高优先级元素。 */
    @Override
    public V poll() {
        return get(pollAsync());
    }

    /** 优先级队列 element 操作。 */
    @Override
    public V element() {
        return getFirst();
    }

    /** 异步执行 peek。 */
    @Override
    public RFuture<V> peekAsync() {
        return getAsync(0);
    }

    /** 查看但不移除最高优先级元素。 */
    @Override
    public V peek() {
        return getValue(0);
    }

    /** 获取 ComparatorKeyName。 */
    private String getComparatorKeyName() {
        return suffixName(getRawName(), "redisson_sortedset_comparator");
    }

    /** 优先级队列 trySetComparator 操作。 */
    @Override
    public boolean trySetComparator(Comparator<? super V> comparator) {
        if (comparator.getClass().isSynthetic()) {
            throw new IllegalArgumentException("Synthetic classes aren't allowed");
        }

        String className = comparator.getClass().getName();
        String comparatorSign = className + ":" + calcClassSign(className);

        Boolean res = get(commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.SETNX, getComparatorKeyName(), comparatorSign));
        if (res) {
            this.comparator = comparator;
        }
        return res;
    }
    
    /** 移除元素。 */
    @Override
    public V remove() {
        return removeFirst();
    }

    /** removeFirst：移除操作。 */
    public V removeFirst() {
        V value = poll();
        if (value == null) {
            throw new NoSuchElementException();
        }
        return value;
    }

    /** 优先级队列 binarySearch 操作。 */
    public BinarySearchResult<V> binarySearch(V value) {
        return binarySearch(value, true);
    }
    
    // TODO optimize: get three values each time instead of single
    public BinarySearchResult<V> binarySearch(V value,  boolean readOnlyMode) {
        int size = size(readOnlyMode);
        int upperIndex = size - 1;
        int lowerIndex = 0;
        while (lowerIndex <= upperIndex) {
            int index = lowerIndex + (upperIndex - lowerIndex) / 2;

            V res = getValue(index, readOnlyMode);

            if (res == null) {
                return new BinarySearchResult<V>();
            }
            int cmp = comparator.compare(value, res);

            if (cmp == 0) {
                BinarySearchResult<V> indexRes = new BinarySearchResult<V>();
                indexRes.setIndex(index);
                return indexRes;
            } else if (cmp < 0) {
                upperIndex = index - 1;
            } else {
                lowerIndex = index + 1;
            }
        }

        BinarySearchResult<V> indexRes = new BinarySearchResult<V>();
        indexRes.setIndex(-(lowerIndex + 1));
        return indexRes;
    }

    @Override
    @SuppressWarnings("AvoidInlineConditionals")
    /** 优先级队列 toString 操作。 */
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
    
    /** 优先级队列 pollLastAndOfferFirstTo 操作。 */
    @Override
    public V pollLastAndOfferFirstTo(String queueName) {
        return get(pollLastAndOfferFirstToAsync(queueName));
    }

    /** 将成员移动到另一 Set。 */
    @Override
    public List<V> move(QueueMoveElementsArgs args) {
        return get(moveAsync(args));
    }

    /** 异步执行 move。 */
    @Override
    public RFuture<List<V>> moveAsync(QueueMoveElementsArgs args) {
        QueueMoveElementsParams pp = (QueueMoveElementsParams) args;
        List<Object> params = new ArrayList<>();
        params.add(getRawName());
        params.add(mapName(pp.getDestName()));
        params.add("LEFT");
        params.add("RIGHT");
        if (pp.getSelector() != null) {
            params.add(pp.getSelector());
            params.add(pp.getCount());
            params.add(pp.getOrdering());
        }
        return wrapLockedAsync(() -> {
            return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.LMOVEM, params.toArray());
        });
    }

    /** 异步执行 pollLastAndOfferFirstTo。 */
    @Override
    public RFuture<V> pollLastAndOfferFirstToAsync(String queueName) {
        CompletionStage<V> f = sizeAsync().thenCompose(r -> {
            if (r > 0) {
                return wrapLockedAsync(RedisCommands.RPOPLPUSH, getRawName(), queueName);
            }
            return CompletableFuture.completedFuture(null);
        });
        return new CompletableFutureWrapper<>(f);
    }

    /** 异步执行 delete。 */
    @Override
    public RFuture<Boolean> deleteAsync() {
        return deleteAsync(getRawName(), getComparatorKeyName());
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {
        return super.expireAsync(timeToLive, timeUnit, param, getRawName(), getComparatorKeyName());
    }

    /** 异步执行 expireAt。 */
    @Override
    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
        return super.expireAtAsync(timestamp, param, getRawName(), getComparatorKeyName());
    }

    /** 异步执行 clearExpire。 */
    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return clearExpireAsync(getRawName(), getComparatorKeyName());
    }

    /** 移除并返回队首/最高优先级元素。 */
    @Override
    public List<V> poll(int limit) {
        return get(pollAsync(limit));
    }

    /** 异步执行 offer。 */
    @Override
    public RFuture<Boolean> offerAsync(V e) {
        throw new UnsupportedOperationException();
    }

    /** 异步添加 ZSet 成员。 */
    @Override
    public RFuture<Boolean> addAsync(V e) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 poll。 */
    @Override
    public RFuture<List<V>> pollAsync(int limit) {
        return wrapLockedAsync(() -> {
            return commandExecutor.evalWriteNoRetryAsync(getRawName(), codec, RedisCommands.EVAL_LIST,
                       "local result = {};"
                     + "for i = 1, ARGV[1], 1 do " +
                           "local value = redis.call('lpop', KEYS[1]);" +
                           "if value ~= false then " +
                               "table.insert(result, value);" +
                           "else " +
                               "return result;" +
                           "end;" +
                       "end; " +
                       "return result;",
                    Collections.singletonList(getRawName()), limit);
        });
    }
}
