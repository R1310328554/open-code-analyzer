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

import org.redisson.api.RFuture;
import org.redisson.api.RList;
import org.redisson.api.SortOrder;
import org.redisson.api.mapreduce.RCollectionMapReduce;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.convertor.BooleanNumberReplayConvertor;
import org.redisson.client.protocol.convertor.Convertor;
import org.redisson.client.protocol.convertor.IntegerReplayConvertor;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.iterator.RedissonBaseIterator;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * {@link RedissonListMultimap} 某 key 下元素列表的只读/可变视图。
 * <p>实现 {@link java.util.List}，变更会同步到底层 Multimap。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public class RedissonListMultimapValues<V> extends RedissonExpirable implements RList<V> {

    private final RList<V> list;
    private final Object key;
    /** 延迟元素 ZSET 键名。 */
    private final String timeoutSetName;

    public RedissonListMultimapValues(Codec codec, CommandAsyncExecutor commandExecutor, String name, String timeoutSetName, Object key) {
        super(codec, commandExecutor, name);
        this.timeoutSetName = timeoutSetName;
        this.key = key;
        this.list = new RedissonList<V>(codec, commandExecutor, name, null);
    }
    
    /** 创建 MapReduce 任务入口。 */
    @Override
    public <KOut, VOut> RCollectionMapReduce<V, KOut, VOut> mapReduce() {
        return null;
    }
    
    /** 异步清除 TTL。 */
    @Override
    public RFuture<Boolean> clearExpireAsync() {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values Set");
    }
    
    /** 异步执行 expire。 */
    @Override
    protected RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values Set");
    }

    /** 异步执行 expireAt。 */
    @Override
    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values Set");
    }
    
    /** 异步返回条目 TTL。 */
    @Override
    public RFuture<Long> remainTimeToLiveAsync() {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values Set");
    }
    
    /** 异步执行 rename。 */
    @Override
    public RFuture<Void> renameAsync(String newName) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values Set");
    }
    
    /** 异步执行 renamenx。 */
    @Override
    public RFuture<Boolean> renamenxAsync(String newName) {
        throw new UnsupportedOperationException("This operation is not supported for SetMultimap values Set");
    }
    
    /** 异步执行 sizeInMemory。 */
    @Override
    public RFuture<Long> sizeInMemoryAsync() {
        List<Object> keys = Arrays.<Object>asList(getRawName(), timeoutSetName);
        return super.sizeInMemoryAsync(keys);
    }
    
    /** 异步 JSON 删除。 */
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
                Arrays.<Object>asList(timeoutSetName, getRawName()), System.currentTimeMillis(), key);
    }

    /** 异步执行 copy。 */
    @Override
    public RFuture<Boolean> copyAsync(List<Object> keys, int database, boolean replace) {
        throw new UnsupportedOperationException();
    }

    /** 返回元素/条目数量。 */
    @Override
    public int size() {
        return get(sizeAsync());
    }

    /** 异步返回数量。 */
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
              + "return redis.call('llen', KEYS[2]);",
         Arrays.<Object>asList(timeoutSetName, getRawName()),
         System.currentTimeMillis(), encodeMapKey(key));
    }

    /** 是否为空。 */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /** List Multimap contains 操作。 */
    @Override
    public boolean contains(Object o) {
        return get(containsAsync(o));
    }

    /** List Multimap iterator 操作。 */
    @Override
    public Iterator<V> iterator() {
        return listIterator();
    }

    /** List Multimap toArray 操作。 */
    @Override
    public Object[] toArray() {
        List<V> list = readAll();
        return list.toArray();
    }

    /** List Multimap readAll 操作。 */
    @Override
    public List<V> readAll() {
        return get(readAllAsync());
    }

    /** 异步执行 readAll。 */
    @Override
    public RFuture<List<V>> readAllAsync() {
        return rangeAsync(0, -1);
    }

    /** List Multimap toArray 操作。 */
    @Override
    public <T> T[] toArray(T[] a) {
        List<V> list = readAll();
        return list.toArray(a);
    }

    /** 追加元素（满则覆盖最旧）。 */
    @Override
    public boolean add(V e) {
        return list.add(e);
    }

    /** 异步追加元素。 */
    @Override
    public RFuture<Boolean> addAsync(V e) {
        return list.addAsync(e);
    }
    
    /** 异步追加元素。 */
    @Override
    public RFuture<Boolean> addAsync(int index, V element) {
        return list.addAsync(index, element);
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
    @Override
    public RFuture<Boolean> removeAsync(Object o, int count) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[3]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore) "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return 0;"
              + "end; "
              + "return redis.call('lrem', KEYS[2], ARGV[2], ARGV[4]) > 0 and 1 or 0;",
         Arrays.<Object>asList(timeoutSetName, getRawName()),
         System.currentTimeMillis(), count, encodeMapKey(key), encodeMapValue(o));
    }

    /** 从延迟队列移除元素。 */
    @Override
    public boolean remove(Object o, int count) {
        return get(removeAsync(o, count));
    }

    /** 异步执行 containsAll。 */
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
                "local items = redis.call('lrange', KEYS[2], 0, -1);" +
                        "for i = 1, #items, 1 do " +
                            "for j = #ARGV, 3, -1 do "
                            + "if ARGV[j] == items[i] "
                            + "then table.remove(ARGV, j) end "
                        + "end; "
                       + "end;"
                       + "return #ARGV == 2 and 1 or 0; ",
                   Arrays.<Object>asList(timeoutSetName, getRawName()), args.toArray());
        
    }

    /** List Multimap containsAll 操作。 */
    @Override
    public boolean containsAll(Collection<?> c) {
        return get(containsAllAsync(c));
    }

    /** List Multimap addAll 操作。 */
    @Override
    public boolean addAll(Collection<? extends V> c) {
        return list.addAll(c);
    }

    /** 异步执行 addAll。 */
    @Override
    public RFuture<Boolean> addAllAsync(final Collection<? extends V> c) {
        return list.addAllAsync(c);
    }

    /** 异步执行 addAll。 */
    @Override
    public RFuture<Boolean> addAllAsync(int index, Collection<? extends V> coll) {
        return list.addAllAsync(index, coll);
    }

    /** List Multimap addAll 操作。 */
    @Override
    public boolean addAll(int index, Collection<? extends V> coll) {
        return list.addAll(index, coll);
    }

    /** 异步 removeAll。 */
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
                            + "if redis.call('lrem', KEYS[2], 0, ARGV[i]) == 1 "
                            + "then v = 1 end "
                        +"end "
                       + "return v ",
               Arrays.<Object>asList(timeoutSetName, getRawName()), args.toArray());
    }

    /** 移除 key 下全部值。 */
    @Override
    public boolean removeAll(Collection<?> c) {
        return get(removeAllAsync(c));
    }

    /** List Multimap retainAll 操作。 */
    @Override
    public boolean retainAll(Collection<?> c) {
        return get(retainAllAsync(c));
    }

    /** 异步执行 retainAll。 */
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

                    "local changed = 0; " +
                    "local s = redis.call('lrange', KEYS[2], 0, -1); "
                       + "local i = 1; "
                       + "while i <= #s do "
                            + "local element = s[i]; "
                            + "local isInAgrs = false; "
                            + "for j = 3, #ARGV, 1 do "
                                + "if ARGV[j] == element then "
                                    + "isInAgrs = true; "
                                    + "break; "
                                + "end; "
                            + "end; "
                            + "if isInAgrs == false then "
                                + "redis.call('lrem', KEYS[2], 0, element); "
                                + "changed = 1; "
                            + "end; "
                            + "i = i + 1; "
                       + "end; "
                       + "return changed; ",
                       Arrays.<Object>asList(timeoutSetName, getRawName()), args.toArray());
    }


    /** 清空全部条目。 */
    @Override
    public void clear() {
        delete();
    }

    /** JSON 路径读取。 */
    @Override
    public List<V> get(int... indexes) {
        return get(getAsync(indexes));
    }

    /** List Multimap distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(final int count) {
        String iteratorName = "__redisson_set_cursor_{" + getRawName() + "}";
        return distributedIterator(iteratorName, count);
    }

    /** List Multimap distributedIterator 操作。 */
    @Override
    public Iterator<V> distributedIterator(final String iteratorName, final int count) {
        return new RedissonBaseIterator<V>() {

            @Override
            protected ScanResult<Object> iterator(RedisClient client, String nextIterPos) {
                return distributedScanIterator(iteratorName, count);
            }

            @Override
            protected void remove(Object value) {
                RedissonListMultimapValues.this.remove((V) value);
            }
        };
    }

    /** List Multimap distributedScanIterator 操作。 */
    private ScanResult<Object> distributedScanIterator(String iteratorName, int count) {
        return get(distributedScanIteratorAsync(iteratorName, count));
    }

    /** 异步执行 distributedScanIterator。 */
    private RFuture<ScanResult<Object>> distributedScanIteratorAsync(String iteratorName, int count) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_SCAN,
                "local cursor = redis.call('get', KEYS[3]); "
                + "if cursor ~= false then "
                    + "cursor = tonumber(cursor); "
                + "else "
                    + "cursor = 0;"
                + "end;"
                + "if start_index == -1 then "
                    + "return {0, {}}; "
                + "end;"
                + "local end_index = start_index + ARGV[1];"
                + "local result; "
                + "result = redis.call('lrange', KEYS[1], start_index, end_index - 1); "
                + "if end_index > redis.call('llen', KEYS[1]) then "
                    + "end_index = -1;"
                + "end; "
                + "redis.call('setex', KEYS[2], 3600, end_index);"
                + "local expireDate = 92233720368547758; "
                + "local expirations = redis.call('zmscore', KEYS[1], result[2])"
                + "for i = #expirations, 1, -1 do "
                    + "if expirations[i] ~= false then "
                        + "local expireDate = tonumber(expireDateScore) "
                        + "if expireDate <= tonumber(ARGV[1]) then "
                            + "table.remove(result[2], i);"
                        + "end; "
                    + "end; "
                + "end; "
                + "return {end_index, result[2]};",
                Arrays.<Object>asList(timeoutSetName, getRawName(), iteratorName), System.currentTimeMillis(), count);
    }

    /** 异步 JSON 读取。 */
    @Override
    public RFuture<List<V>> getAsync(int... indexes) {
        List<Object> params = new ArrayList<Object>();
        params.add(System.currentTimeMillis());
        params.add(encodeMapKey(key));
        for (Integer index : indexes) {
            params.add(index);
        }
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_LIST,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore); "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return nil;"
              + "end; " +
                
                "local result = {}; " + 
                "for i = 3, #ARGV, 1 do "
                    + "local value = redis.call('lindex', KEYS[1], ARGV[i]);"
                    + "table.insert(result, value);" + 
                "end; " +
                "return result;",
                Collections.<Object>singletonList(getRawName()), params.toArray());
    }
    
    /** 异步 JSON 读取。 */
    @Override
    public RFuture<V> getAsync(int index) {
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_MAP_VALUE,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[3]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore); "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return nil;"
              + "end; "
              + "return redis.call('lindex', KEYS[2], ARGV[2]);",
         Arrays.<Object>asList(timeoutSetName, getRawName()),
         System.currentTimeMillis(), index, encodeMapKey(key));
    }

    /** JSON 路径读取。 */
    @Override
    public V get(int index) {
        return getValue(index);
    }

    V getValue(int index) {
        return get(getAsync(index));
    }

    /** JSON 路径写入。 */
    public V set(int index, V element) {
        return list.set(index, element);
    }

    /** 异步 JSON 写入。 */
    @Override
    public RFuture<V> setAsync(int index, V element) {
        return list.setAsync(index, element);
    }

    /** List Multimap fastSet 操作。 */
    @Override
    public void fastSet(int index, V element) {
        list.fastSet(index, element);
    }

    /** 异步执行 fastSet。 */
    @Override
    public RFuture<Void> fastSetAsync(int index, V element) {
        return list.fastSetAsync(index, element);
    }

    /** 追加元素（满则覆盖最旧）。 */
    @Override
    public void add(int index, V element) {
        addAll(index, Collections.singleton(element));
    }

    /** 从延迟队列移除元素。 */
    @Override
    public V remove(int index) {
        return list.remove(index);
    }
    
    /** 异步移除。 */
    @Override
    public RFuture<V> removeAsync(int index) {
        return list.removeAsync(index);
    }

    /** 快速删除多个键。 */
    @Override
    public void fastRemove(int index) {
        list.fastRemove(index);
    }
    
    /** 异步执行 fastRemove。 */
    @Override
    public RFuture<Void> fastRemoveAsync(int index) {
        return list.fastRemoveAsync(index);
    }
    
    /** List Multimap indexOf 操作。 */
    @Override
    public int indexOf(Object o) {
        return get(indexOfAsync(o));
    }

    /** 异步执行 contains。 */
    @Override
    public RFuture<Boolean> containsAsync(Object o) {
        return indexOfAsync(o, new BooleanNumberReplayConvertor(-1L));
    }

    /** 异步执行 indexOf。 */
    private <R> RFuture<R> indexOfAsync(Object o, Convertor<R> convertor) {
        return commandExecutor.evalReadAsync(getRawName(), codec, new RedisCommand<R>("EVAL", convertor),
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore); "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return -1;"
              + "end; " +

                "local items = redis.call('lrange', KEYS[2], 0, -1); " +
                "for i=1,#items do " +
                    "if items[i] == ARGV[3] then " +
                        "return i - 1; " +
                    "end; " +
                "end; " +
                "return -1;",
                Arrays.<Object>asList(timeoutSetName, getRawName()),
                System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(o));
    }

    /** 异步执行 indexOf。 */
    @Override
    public RFuture<Integer> indexOfAsync(Object o) {
        return indexOfAsync(o, new IntegerReplayConvertor());
    }

    /** List Multimap lastIndexOf 操作。 */
    @Override
    public int lastIndexOf(Object o) {
        return get(lastIndexOfAsync(o));
    }

    /** 异步执行 lastIndexOf。 */
    @Override
    public RFuture<Integer> lastIndexOfAsync(Object o) {
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_INTEGER,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore); "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return -1;"
              + "end; " +
                
                "local items = redis.call('lrange', KEYS[1], 0, -1) " +
                "for i = #items, 1, -1 do " +
                    "if items[i] == ARGV[1] then " +
                        "return i - 1 " +
                    "end " +
                "end " +
                "return -1",
                Arrays.<Object>asList(timeoutSetName, getRawName()),
                System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(o));
    }

    /** List Multimap trim 操作。 */
    @Override
    public void trim(int fromIndex, int toIndex) {
        list.trim(fromIndex, toIndex);
    }

    /** 异步执行 trim。 */
    @Override
    public RFuture<Void> trimAsync(int fromIndex, int toIndex) {
        return list.trimAsync(fromIndex, toIndex);
    }

    /** List Multimap listIterator 操作。 */
    @Override
    public ListIterator<V> listIterator() {
        return listIterator(0);
    }

    /** List Multimap listIterator 操作。 */
    @Override
    public ListIterator<V> listIterator(final int ind) {
        return new ListIterator<V>() {

            private V prevCurrentValue;
            private V nextCurrentValue;
            private V currentValueHasRead;
            private int currentIndex = ind - 1;
            private boolean hasBeenModified = true;

            @Override
            public boolean hasNext() {
                V val = RedissonListMultimapValues.this.getValue(currentIndex+1);
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
                RedissonListMultimapValues.this.remove(currentIndex);
                currentIndex--;
                hasBeenModified = true;
                currentValueHasRead = null;
            }

            @Override
            public boolean hasPrevious() {
                if (currentIndex < 0) {
                    return false;
                }
                V val = RedissonListMultimapValues.this.getValue(currentIndex);
                if (val != null) {
                    prevCurrentValue = val;
                }
                return val != null;
            }

            @Override
            public V previous() {
                if (prevCurrentValue == null && !hasPrevious()) {
                    throw new NoSuchElementException("No such element at index " + currentIndex);
                }
                currentIndex--;
                hasBeenModified = false;
                currentValueHasRead = prevCurrentValue;
                prevCurrentValue = null;
                return currentValueHasRead;
            }

            @Override
            public int nextIndex() {
                return currentIndex + 1;
            }

            @Override
            public int previousIndex() {
                return currentIndex;
            }

            @Override
            public void set(V e) {
                if (hasBeenModified) {
                    throw new IllegalStateException();
                }

                RedissonListMultimapValues.this.fastSet(currentIndex, e);
            }

            @Override
            public void add(V e) {
                RedissonListMultimapValues.this.add(currentIndex+1, e);
                currentIndex++;
                hasBeenModified = true;
            }
        };
    }

    /** List Multimap subList 操作。 */
    @Override
    public RList<V> subList(int fromIndex, int toIndex) {
        int size = size();
        if (fromIndex < 0 || toIndex > size) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + " size: " + size);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex: " + fromIndex + " toIndex: " + toIndex);
        }

        return new RedissonSubList<V>(codec, commandExecutor, getRawName(), fromIndex, toIndex);
    }

    @Override
    @SuppressWarnings("AvoidInlineConditionals")
    /** List Multimap toString 操作。 */
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

    @Override
    @SuppressWarnings("AvoidInlineConditionals")
    /** List Multimap equals 操作。 */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof List))
            return false;

        Iterator<V> e1 = iterator();
        Iterator<?> e2 = ((List<?>) o).iterator();
        while (e1.hasNext() && e2.hasNext()) {
            V o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    @Override
    @SuppressWarnings("AvoidInlineConditionals")
    /** List Multimap hashCode 操作。 */
    public int hashCode() {
        int hashCode = 1;
        for (V e : this) {
            hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    /** 异步执行 addAfter。 */
    @Override
    public RFuture<Integer> addAfterAsync(V elementToFind, V element) {
        return list.addAfterAsync(elementToFind, element);
    }

    /** 异步执行 addBefore。 */
    @Override
    public RFuture<Integer> addBeforeAsync(V elementToFind, V element) {
        return list.addBeforeAsync(elementToFind, element);
    }

    /** List Multimap addAfter 操作。 */
    @Override
    public int addAfter(V elementToFind, V element) {
        return list.addAfter(elementToFind, element);
    }

    /** List Multimap addBefore 操作。 */
    @Override
    public int addBefore(V elementToFind, V element) {
        return list.addBefore(elementToFind, element);
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<List<V>> readSortAsync(SortOrder order) {
        return list.readSortAsync(order);
    }

    /** List Multimap readSort 操作。 */
    @Override
    public List<V> readSort(SortOrder order) {
        return list.readSort(order);
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<List<V>> readSortAsync(SortOrder order, int offset, int count) {
        return list.readSortAsync(order, offset, count);
    }

    /** List Multimap readSort 操作。 */
    @Override
    public List<V> readSort(SortOrder order, int offset, int count) {
        return list.readSort(order, offset, count);
    }

    /** List Multimap readSort 操作。 */
    @Override
    public List<V> readSort(String byPattern, SortOrder order, int offset, int count) {
        return list.readSort(byPattern, order, offset, count);
    }

    /** 异步执行 readSort。 */
    @Override
    public RFuture<List<V>> readSortAsync(String byPattern, SortOrder order, int offset, int count) {
        return list.readSortAsync(byPattern, order, offset, count);
    }

    /** List Multimap readSort 操作。 */
    @Override
    public <T> Collection<T> readSort(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return list.readSort(byPattern, getPatterns, order, offset, count);
    }

    /** 异步执行 readSort。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset,
            int count) {
        return list.readSortAsync(byPattern, getPatterns, order, offset, count);
    }

    /** List Multimap readSortAlpha 操作。 */
    @Override
    public List<V> readSortAlpha(SortOrder order) {
        return list.readSortAlpha(order);
    }

    /** List Multimap readSortAlpha 操作。 */
    @Override
    public List<V> readSortAlpha(SortOrder order, int offset, int count) {
        return list.readSortAlpha(order, offset, count);
    }

    /** List Multimap readSortAlpha 操作。 */
    @Override
    public List<V> readSortAlpha(String byPattern, SortOrder order) {
        return list.readSortAlpha(byPattern, order);
    }

    /** List Multimap readSortAlpha 操作。 */
    @Override
    public List<V> readSortAlpha(String byPattern, SortOrder order, int offset, int count) {
        return list.readSortAlpha(byPattern, order, offset, count);
    }

    /** List Multimap readSortAlpha 操作。 */
    @Override
    public <T> Collection<T> readSortAlpha(String byPattern, List<String> getPatterns, SortOrder order) {
        return list.readSortAlpha(byPattern, getPatterns, order);
    }

    /** List Multimap readSortAlpha 操作。 */
    @Override
    public <T> Collection<T> readSortAlpha(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return list.readSortAlpha(byPattern, getPatterns, order, offset, count);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<List<V>> readSortAlphaAsync(SortOrder order) {
        return list.readSortAlphaAsync(order);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<List<V>> readSortAlphaAsync(SortOrder order, int offset, int count) {
        return list.readSortAlphaAsync(order, offset, count);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<List<V>> readSortAlphaAsync(String byPattern, SortOrder order) {
        return list.readSortAlphaAsync(byPattern, order);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public RFuture<List<V>> readSortAlphaAsync(String byPattern, SortOrder order, int offset, int count) {
        return list.readSortAlphaAsync(byPattern, order, offset, count);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order) {
        return list.readSortAlphaAsync(byPattern, getPatterns, order);
    }

    /** 异步执行 readSortAlpha。 */
    @Override
    public <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count) {
        return list.readSortAlphaAsync(byPattern, getPatterns, order, offset, count);
    }

    /** List Multimap sortTo 操作。 */
    @Override
    public int sortTo(String destName, SortOrder order) {
        return list.sortTo(destName, order);
    }

    /** 异步执行 sortTo。 */
    @Override
    public RFuture<Integer> sortToAsync(String destName, SortOrder order) {
        return list.sortToAsync(destName, order);
    }

    /** List Multimap readSort 操作。 */
    public List<V> readSort(String byPattern, SortOrder order) {
        return list.readSort(byPattern, order);
    }

    /** 异步执行 readSort。 */
    public RFuture<List<V>> readSortAsync(String byPattern, SortOrder order) {
        return list.readSortAsync(byPattern, order);
    }

    /** List Multimap readSort 操作。 */
    public <T> Collection<T> readSort(String byPattern, List<String> getPatterns, SortOrder order) {
        return list.readSort(byPattern, getPatterns, order);
    }

    /** 异步执行 readSort。 */
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order) {
        return list.readSortAsync(byPattern, getPatterns, order);
    }

    /** List Multimap sortTo 操作。 */
    public int sortTo(String destName, SortOrder order, int offset, int count) {
        return list.sortTo(destName, order, offset, count);
    }

    /** List Multimap sortTo 操作。 */
    public int sortTo(String destName, String byPattern, SortOrder order) {
        return list.sortTo(destName, byPattern, order);
    }

    /** 异步执行 sortTo。 */
    public RFuture<Integer> sortToAsync(String destName, SortOrder order, int offset, int count) {
        return list.sortToAsync(destName, order, offset, count);
    }

    /** List Multimap sortTo 操作。 */
    public int sortTo(String destName, String byPattern, SortOrder order, int offset, int count) {
        return list.sortTo(destName, byPattern, order, offset, count);
    }

    /** 异步执行 sortTo。 */
    public RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order) {
        return list.sortToAsync(destName, byPattern, order);
    }

    /** List Multimap sortTo 操作。 */
    public int sortTo(String destName, String byPattern, List<String> getPatterns, SortOrder order) {
        return list.sortTo(destName, byPattern, getPatterns, order);
    }

    /** 异步执行 sortTo。 */
    public RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order, int offset,
            int count) {
        return list.sortToAsync(destName, byPattern, order, offset, count);
    }

    /** List Multimap sortTo 操作。 */
    public int sortTo(String destName, String byPattern, List<String> getPatterns, SortOrder order, int offset,
            int count) {
        return list.sortTo(destName, byPattern, getPatterns, order, offset, count);
    }

    /** 异步执行 sortTo。 */
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns,
            SortOrder order) {
        return list.sortToAsync(destName, byPattern, getPatterns, order);
    }

    /** 异步执行 sortTo。 */
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns,
            SortOrder order, int offset, int count) {
        return list.sortToAsync(destName, byPattern, getPatterns, order, offset, count);
    }

    /** 异步执行 range。 */
    @Override
    public RFuture<List<V>> rangeAsync(int toIndex) {
        return rangeAsync(0, toIndex);
    }

    /** 异步执行 range。 */
    @Override
    public RFuture<List<V>> rangeAsync(int fromIndex, int toIndex) {
        return commandExecutor.evalReadAsync(getRawName(), codec, RedisCommands.EVAL_MAP_VALUE_LIST,
                "local expireDate = 92233720368547758; " +
                "local expireDateScore = redis.call('zscore', KEYS[1], ARGV[2]); "
              + "if expireDateScore ~= false then "
                  + "expireDate = tonumber(expireDateScore) "
              + "end; "
              + "if expireDate <= tonumber(ARGV[1]) then "
                  + "return {};"
              + "end; "
              + "return redis.call('lrange', KEYS[2], ARGV[3], ARGV[4]);",
              Arrays.<Object>asList(timeoutSetName, getRawName()),
              System.currentTimeMillis(), encodeMapKey(key), fromIndex, toIndex);
    }

    /** List Multimap range 操作。 */
    @Override
    public List<V> range(int toIndex) {
        return get(rangeAsync(toIndex));
    }

    /** List Multimap range 操作。 */
    @Override
    public List<V> range(int fromIndex, int toIndex) {
        return get(rangeAsync(fromIndex, toIndex));
    }

    
    
}
