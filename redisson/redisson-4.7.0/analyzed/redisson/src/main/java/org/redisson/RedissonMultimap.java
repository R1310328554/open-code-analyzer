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
import org.redisson.api.*;
import org.redisson.api.listener.MapPutListener;
import org.redisson.api.listener.MapRemoveListener;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.decoder.MapScanResult;
import org.redisson.codec.CompositeCodec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.iterator.RedissonBaseMapIterator;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.misc.Hash;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * {@link RMultimap} 抽象基类：每个键映射到 Redis Set 子键。
 * <p>通过 {@code prefix} 后缀命名子集合，封装 getAll/putAll/removeAll 等通用逻辑。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public abstract class RedissonMultimap<K, V> extends RedissonExpirable implements RMultimap<K, V> {

    /** 多值映射子键前缀。 */
    final String prefix;
    
    RedissonMultimap(CommandAsyncExecutor commandAsyncExecutor, String name) {
        super(commandAsyncExecutor, name);
        prefix = suffixName(getRawName(), "");
    }

    RedissonMultimap(Codec codec, CommandAsyncExecutor commandAsyncExecutor, String name) {
        super(codec, commandAsyncExecutor, name);
        prefix = suffixName(getRawName(), "");
    }

    /** 异步执行 sizeInMemory。 */
    @Override
    public RFuture<Long> sizeInMemoryAsync() {
        return commandExecutor.evalWriteAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.EVAL_LONG,
                "local keys = redis.call('hgetall', KEYS[1]); " +
                "local size = 0; " +
                "for i, v in ipairs(keys) do " +
                    "if i % 2 == 0 then " +
                        "local name = ARGV[1] .. v; " +
                        "size = size + redis.call('memory', 'usage', name); " +
                    "end;" +
                "end; " +
                "return size; ", Arrays.asList(getRawName()), prefix);
    }

    /** 异步执行 copy。 */
    @Override
    public RFuture<Boolean> copyAsync(List<Object> keys, int database, boolean replace) {
        throw new UnsupportedOperationException();
    }

    /** 获取公平锁。 */
    @Override
    public RLock getFairLock(K key) {
        String lockName = getLockByMapKey(key, "fairlock");
        return new RedissonFairLock(commandExecutor, lockName);
    }
    
    /** 获取可过期许可信号量。 */
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(K key) {
        String lockName = getLockByMapKey(key, "permitexpirablesemaphore");
        return new RedissonPermitExpirableSemaphore(commandExecutor, lockName);
    }
    
    /** 获取 CountDownLatch。 */
    @Override
    public RCountDownLatch getCountDownLatch(K key) {
        String lockName = getLockByMapKey(key, "countdownlatch");
        return new RedissonCountDownLatch(commandExecutor, lockName);
    }
    
    /** 获取分布式信号量。 */
    @Override
    public RSemaphore getSemaphore(K key) {
        String lockName = getLockByMapKey(key, "semaphore");
        return new RedissonSemaphore(commandExecutor, lockName);
    }
    
    /** 获取分布式锁。 */
    @Override
    public RLock getLock(K key) {
        String lockName = getLockByMapKey(key, "lock");
        return new RedissonLock(commandExecutor, lockName);
    }
    
    /** 获取读写锁。 */
    @Override
    public RReadWriteLock getReadWriteLock(K key) {
        String lockName = getLockByMapKey(key, "rw_lock");
        return new RedissonReadWriteLock(commandExecutor, lockName);
    }
    
    String hash(ByteBuf objectState) {
        return Hash.hash128toBase64(objectState);
    }

    /** Multimap keyHash 操作。 */
    protected String keyHash(Object key) {
        ByteBuf objectState = encodeMapKey(key);
        try {
            return Hash.hash128toBase64(objectState);
        } finally {
            objectState.release();
        }
    }
    
    /** 返回元素数量。 */
    @Override
    public int size() {
        return get(sizeAsync());
    }

    /** Multimap fastRemoveValue 操作。 */
    @Override
    public long fastRemoveValue(V... values) {
        return get(fastRemoveValueAsync(values));
    }

    /** Multimap keySize 操作。 */
    @Override
    public int keySize() {
        return get(keySizeAsync());
    }

    /** 集合是否为空。 */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /** 是否包含指定键。 */
    @Override
    public boolean containsKey(Object key) {
        return get(containsKeyAsync(key));
    }

    /** 是否包含指定值。 */
    @Override
    public boolean containsValue(Object value) {
        return get(containsValueAsync(value));
    }

    /** 是否包含键值对。 */
    @Override
    public boolean containsEntry(Object key, Object value) {
        return get(containsEntryAsync(key, value));
    }

    /** 向指定键的 Set 添加值。 */
    @Override
    public boolean put(K key, V value) {
        return get(putAsync(key, value));
    }

    String getValuesName(String hash) {
        return suffixName(getRawName(), hash);
    }

    /** 移除元素。 */
    @Override
    public boolean remove(Object key, Object value) {
        return get(removeAsync(key, value));
    }

    /** 批量写入多键多值。 */
    @Override
    public boolean putAll(K key, Iterable<? extends V> values) {
        return get(putAllAsync(key, values));
    }

    /** 清空全部元素。 */
    @Override
    public void clear() {
        delete();
    }

    /** 返回键集合视图。 */
    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    /** 返回键集合视图。 */
    @Override
    public Set<K> keySet(int count) {
        return new KeySet(count);
    }

    /** 返回值集合视图。 */
    @Override
    public Collection<V> values() {
        return new Values();
    }

    /** 返回值集合视图。 */
    @Override
    public Collection<V> values(int count) {
        return new Values(count);
    }

    /** 返回全部键及其 Set 值。 */
    @Override
    public Collection<V> getAll(K key) {
        return get(getAllAsync(key));
    }

    /** 批量移除元素。 */
    @Override
    public Collection<V> removeAll(Object key) {
        return get(removeAllAsync(key));
    }

    /** 替换指定键的全部值。 */
    @Override
    public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
        return get(replaceValuesAsync(key, values));
    }

    /** 返回键值对集合视图。 */
    @Override
    public Collection<Entry<K, V>> entries() {
        return new EntrySet();
    }

    /** 返回键值对集合视图。 */
    @Override
    public Collection<Entry<K, V>> entries(int count) {
        return new EntrySet(count);
    }

    /** Multimap readAllKeySet 操作。 */
    @Override
    public Set<K> readAllKeySet() {
        return get(readAllKeySetAsync());
    }

    /** 异步执行 readAllKeySet。 */
    @Override
    public RFuture<Set<K>> readAllKeySetAsync() {
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.HKEYS, getRawName());
    }

    /** Multimap fastRemove 操作。 */
    @Override
    public long fastRemove(K... keys) {
        return get(fastRemoveAsync(keys));
    }

    /** 异步执行 fastRemove。 */
    @Override
    public RFuture<Long> fastRemoveAsync(K... keys) {
        if (keys == null || keys.length == 0) {
            return new CompletableFutureWrapper<>(0L);
        }

        List<Object> mapKeys = new ArrayList<Object>(keys.length);
        List<Object> listKeys = new ArrayList<Object>(keys.length + 1);
        listKeys.add(getRawName());
        for (K key : keys) {
            ByteBuf keyState = encodeMapKey(key);
            mapKeys.add(keyState);
            String keyHash = hash(keyState);
            String name = getValuesName(keyHash);
            listKeys.add(name);
        }

        return fastRemoveAsync(mapKeys, listKeys, RedisCommands.EVAL_LONG);
    }

    /** 异步执行 fastRemove。 */
    protected <T> RFuture<T> fastRemoveAsync(List<Object> mapKeys, List<Object> listKeys, RedisCommand<T> evalCommandType) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, evalCommandType,
                    "local res = redis.call('hdel', KEYS[1], unpack(ARGV)); " +
                    "if res > 0 then " +
                        "redis.call('del', unpack(KEYS, 2, #KEYS)); " +
                    "end; " +
                    "return res; ",
                    listKeys, mapKeys.toArray());
    }
    
    /** 异步执行 delete。 */
    @Override
    public RFuture<Boolean> deleteAsync() {
        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN_AMOUNT,
                "local entries = redis.call('hgetall', KEYS[1]); " +
                "local keys = {KEYS[1]}; " +
                "for i, v in ipairs(entries) do " +
                    "if i % 2 == 0 then " +
                        "local name = ARGV[1] .. v; " + 
                        "table.insert(keys, name); " +
                    "end;" +
                "end; " +
                
                "local n = 0 "
                + "for i=1, #keys,5000 do "
                    + "n = n + redis.call('del', unpack(keys, i, math.min(i+4999, table.getn(keys)))) "
                + "end; "
                + "return n;",
                Arrays.<Object>asList(getRawName()), prefix);
    }

    /** 异步执行 rename。 */
    @Override
    public RFuture<Void> renameAsync(String newName) {
        String newPrefix = suffixName(newName, "");
        RFuture<Void> future = commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_VOID,
                "local entries = redis.call('hgetall', KEYS[1]); " +
                "local keys = {}; " +
                "for i, v in ipairs(entries) do " +
                    "if i % 2 == 0 then " +
                        "table.insert(keys, v); " +
                    "end;" +
                "end; " +

                "redis.call('rename', KEYS[1], ARGV[3]); "
              + "for i=1, #keys, 1 do "
                  + "redis.call('rename', ARGV[1] .. keys[i], ARGV[2] .. keys[i]); "
              + "end; ",
                Arrays.asList(getRawName()), prefix, newPrefix, newName);
        CompletionStage<Void> f = future.thenAccept(r -> setName(newName));
        return new CompletableFutureWrapper<>(f);
    }

    /** 异步执行 renamenx。 */
    @Override
    public RFuture<Boolean> renamenxAsync(String newName) {
        String newPrefix = suffixName(newName, "");
        RFuture<Boolean> future = commandExecutor.evalWriteAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "local entries = redis.call('hgetall', KEYS[1]); " +
                "local keys = {}; " +
                "for i, v in ipairs(entries) do " +
                    "if i % 2 == 0 then " +
                        "table.insert(keys, v); " +
                    "end;" +
                "end; " +

                "local r = redis.call('exists', ARGV[3]);" +
                "if r == 1 then " +
                    "return 0;" +
                "end; " +
                "for i=1, #keys, 1 do " +
                    "local r = redis.call('exists', ARGV[2] .. keys[i]);" +
                    "if r == 1 then " +
                        "return 0;" +
                    "end; " +
                "end; " +

                "redis.call('rename', KEYS[1], ARGV[3]); "
              + "for i=1, #keys, 1 do "
                  + "redis.call('rename', ARGV[1] .. keys[i], ARGV[2] .. keys[i]); "
              + "end; " +
                "return 1; ",
                Arrays.asList(getRawName()), prefix, newPrefix, newName);
        CompletionStage<Boolean> f = future.thenApply(value -> {
            if (value) {
                setName(newName);
            }
            return value;
        });
        return new CompletableFutureWrapper<>(f);
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {
        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "local entries = redis.call('hgetall', KEYS[1]); " +
                "for i, v in ipairs(entries) do " +
                    "if i % 2 == 0 then " +
                        "local name = ARGV[2] .. v; "
                      + "if ARGV[3] ~= '' then "
                          + "redis.call('pexpire', name, ARGV[1], ARGV[3]); "
                      + "else "
                          + "redis.call('pexpire', name, ARGV[1]); "
                      + "end; "
                  + "end;" +
                "end; "
              + "if ARGV[3] ~= '' then "
                  + "return redis.call('pexpire', KEYS[1], ARGV[1], ARGV[3]); "
              + "end; "
              + "return redis.call('pexpire', KEYS[1], ARGV[1]); ",
                Arrays.asList(getRawName()),
                timeUnit.toMillis(timeToLive), prefix, param);
    }

    /** 异步执行 expireAt。 */
    @Override
    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
          "local entries = redis.call('hgetall', KEYS[1]); " +
                "for i, v in ipairs(entries) do " +
                    "if i % 2 == 0 then " +
                        "local name = ARGV[2] .. v; "
                      + "if ARGV[3] ~= '' then "
                          + "redis.call('pexpireat', name, ARGV[1], ARGV[3]); "
                      + "else "
                          + "redis.call('pexpireat', name, ARGV[1]); "
                      + "end; "
                  + "end;"
              + "end; "
              + "if ARGV[3] ~= '' then "
                  + "return redis.call('pexpireat', KEYS[1], ARGV[1], ARGV[3]); "
              + "end; "
              + "return redis.call('pexpireat', KEYS[1], ARGV[1]); ",
                Arrays.asList(getRawName()),
                timestamp, prefix, param);
    }

    /** 异步执行 clearExpire。 */
    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "local entries = redis.call('hgetall', KEYS[1]); " +
                "for i, v in ipairs(entries) do " +
                    "if i % 2 == 0 then " +
                        "local name = ARGV[1] .. v; " + 
                        "redis.call('persist', name); " +
                    "end;" +
                "end; " +
                "return redis.call('persist', KEYS[1]); ",
                Arrays.<Object>asList(getRawName()),
                prefix);
    }
    
    /** 异步执行 keySize。 */
    @Override
    public RFuture<Integer> keySizeAsync() {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.HLEN, getRawName());
    }
    
    MapScanResult<Object, Object> scanIterator(RedisClient client, String startPos, int count) {
        RFuture<MapScanResult<Object, Object>> f = commandExecutor.readAsync(client, getRawName(),
                new CompositeCodec(codec, StringCodec.INSTANCE, codec),
                RedisCommands.HSCAN, getRawName(), startPos, "COUNT", count);
        return get(f);
    }

    abstract Iterator<V> valuesIterator();

    abstract RedissonMultiMapIterator<K, V, Entry<K, V>> entryIterator();

    Iterator<V> valuesIterator(int count) {
        return valuesIterator();
    }

    RedissonMultiMapIterator<K, V, Entry<K, V>> entryIterator(int count) {
        return entryIterator();
    }

    final class KeySet extends AbstractSet<K> {

        final int count;

        KeySet() {
            this(10);
        }

        KeySet(int count) {
            this.count = count;
        }

        @Override
        public Iterator<K> iterator() {
            return new RedissonBaseMapIterator<K>() {
                @Override
                protected K getValue(java.util.Map.Entry<Object, Object> entry) {
                    return (K) entry.getKey();
                }

                @Override
                protected Object put(Entry<Object, Object> entry, Object value) {
                    return RedissonMultimap.this.put((K) entry.getKey(), (V) value);
                }

                @Override
                protected ScanResult<Entry<Object, Object>> iterator(RedisClient client, String nextIterPos) {
                    return RedissonMultimap.this.scanIterator(client, nextIterPos, count);
                }

                @Override
                protected void remove(Entry<Object, Object> value) {
                    RedissonMultimap.this.fastRemove((K) value.getKey());
                }


            };
        }

        @Override
        public boolean contains(Object o) {
            return RedissonMultimap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return RedissonMultimap.this.fastRemove((K) o) == 1;
        }

        @Override
        public int size() {
            return RedissonMultimap.this.keySize();
        }

        @Override
        public void clear() {
            RedissonMultimap.this.clear();
        }

    }

    final class Values extends AbstractCollection<V> {

        private final int count;

        Values() {
            this(10);
        }

        Values(int count) {
            this.count = count;
        }

        @Override
        public Iterator<V> iterator() {
            return valuesIterator(count);
        }

        @Override
        public boolean contains(Object o) {
            return RedissonMultimap.this.containsValue(o);
        }

        @Override
        public int size() {
            return RedissonMultimap.this.size();
        }

        @Override
        public void clear() {
            RedissonMultimap.this.clear();
        }

    }

    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        private final int count;

        EntrySet() {
            this(10);
        }

        EntrySet(int count) {
            this.count = count;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return entryIterator(count);
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return containsEntry(e.getKey(), e.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return RedissonMultimap.this.remove(key, value);
            }
            return false;
        }

        @Override
        public int size() {
            return RedissonMultimap.this.size();
        }

        @Override
        public void clear() {
            RedissonMultimap.this.clear();
        }

    }

    /** 注册对象变更监听器。 */
    @Override
    public int addListener(ObjectListener listener) {
        if (listener instanceof MapPutListener) {
            return addMapFieldListener("__subkeyevent@*:hset", "__keyevent@*:hset",
                    (MapPutListener) listener, MapPutListener::onPut);
        }
        if (listener instanceof MapRemoveListener) {
            return addMapFieldListener("__subkeyevent@*:hdel", "__keyevent@*:hdel",
                    (MapRemoveListener) listener, MapRemoveListener::onRemove);
        }

        return super.addListener(listener);
    }

    /** 异步执行 addListener。 */
    @Override
    public RFuture<Integer> addListenerAsync(ObjectListener listener) {
        if (listener instanceof MapPutListener) {
            return addMapFieldListenerAsync("__subkeyevent@*:hset", "__keyevent@*:hset",
                    (MapPutListener) listener, MapPutListener::onPut);
        }
        if (listener instanceof MapRemoveListener) {
            return addMapFieldListenerAsync("__subkeyevent@*:hdel", "__keyevent@*:hdel",
                    (MapRemoveListener) listener, MapRemoveListener::onRemove);
        }

        return super.addListenerAsync(listener);
    }

    /** 移除监听器。 */
    @Override
    public void removeListener(int listenerId) {
        removeListener(listenerId, "__subkeyevent@*:hset", "__keyevent@*:hset",
                "__subkeyevent@*:hdel", "__keyevent@*:hdel");
        super.removeListener(listenerId);
    }

    /** 异步执行 removeListener。 */
    @Override
    public RFuture<Void> removeListenerAsync(int listenerId) {
        return removeListenerAsync(listenerId, "__subkeyevent@*:hset", "__keyevent@*:hset",
                "__subkeyevent@*:hdel", "__keyevent@*:hdel");
    }


}
