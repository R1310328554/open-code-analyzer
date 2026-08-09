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
package org.redisson.api;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.redisson.api.map.MapLoader;
import org.redisson.api.map.MapWriter;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * 基于 Redis 的 {@link java.util.concurrent.ConcurrentMap} 与 {@link java.util.Map} RxJava3 API。
 * <p>使用键序列化状态而非 hashCode/equals；不允许 {@code null} 键或值。
 * 各方法返回 {@link Single}/{@link Maybe}/{@link Completable}；支持 MapLoader/MapWriter。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RMapRx<K, V> extends RExpirableRx {

    /**
     * 若键尚无映射则关联指定值；否则用 remapping 函数合并并更新值。
     * Otherwise, replaces the associated value with the results of the given
     * remapping function, or removes if the result is {@code null}.
     *
     * @param key 映射键
     * @param value 待合并的值
     *        associated with the key or to be associated with the key,
     *        if no existing value
     * @param remappingFunction 合并函数
     * @return new value associated with the specified key or
     *         {@code null} if no value associated with the key
     */
    Maybe<V> merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction);

    /**
     * 根据键及其当前映射值计算新映射。
     *
     * @param key 映射键
     * @param remappingFunction - function to compute a value
     * @return the new value associated with the specified key, or {@code null} if none
     */
    Maybe<V> compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    /**
     * 仅当键尚未映射时计算并存储新值。
     *
     * @param key 映射键
     * @param mappingFunction 映射函数
     * @return current or new computed value associated with
     *         the specified key, or {@code null} if the computed value is null
     */
    Maybe<V> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);

    /**
     * 仅当键已有映射时计算并更新值。
     *
     * @param key 映射键
     * @param remappingFunction - function to compute a value
     * @return the new value associated with the specified key, or null if none
     */
    Maybe<V> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    /**
     * 使用 {@link org.redisson.api.map.MapLoader} 加载全部映射条目。
     * 
     * @param replaceExistingValues 是否替换已有值  
     * @param parallelism 并行度
     * @return 无返回值
     */
    Completable loadAll(boolean replaceExistingValues, int parallelism);
    
    /**
     * 使用 {@link org.redisson.api.map.MapLoader} 加载指定 {@code keys} 的条目。
     * 
     * @param keys 键集合
     * @param replaceExistingValues 是否替换已有值
     * @param parallelism 并行度
     * @return 无返回值
     */
    Completable loadAll(Set<? extends K> keys, boolean replaceExistingValues, int parallelism);

    /**
     * Returns size of value mapped by key in bytes
     * 
     * @param key 映射键
     * @return size of value
     */
    Single<Integer> valueSize(K key);

    /**
     * 返回指定 {@code keys} 对应的 Map 切片。
     * <p>
     * If map doesn't contain value/values for specified key/keys and {@link MapLoader} is defined 
     * then value/values will be loaded in read-through mode. 
     * <p>
     * The returned map is <b>NOT</b> backed by the original map.
     *
     * @param keys 键集合
     * @return Map slice
     */
    Single<Map<K, V>> getAll(Set<K> keys);

    /**
     * Stores map entries specified in <code>map</code> object in batch mode.
     * <p>
     * If {@link MapWriter} is defined then map entries will be stored in write-through mode.
     *
     * @param map mappings to be stored in this map
     * @return 无返回值
     */
    Completable putAll(Map<? extends K, ? extends V> map);

    /**
     * Adds the given <code>delta</code> to the current value
     * by mapped <code>key</code>.
     * <p>
     * Works only with codecs below
     * <p>
     * {@link org.redisson.codec.JsonJacksonCodec},
     * <p>
     * {@link org.redisson.client.codec.StringCodec},
     * <p>
     * {@link org.redisson.client.codec.IntegerCodec},
     * <p>
     * {@link org.redisson.client.codec.DoubleCodec}
     * <p>
     * {@link org.redisson.client.codec.LongCodec}
     *
     * @param key 映射键
     * @param delta the value to add
     * @return the updated value
     */
    Single<V> addAndGet(K key, Number delta);

    /**
     * Returns <code>true</code> if this map contains any map entry
     * with specified <code>value</code>, otherwise <code>false</code>
     *
     * @param value 映射值
     * @return <code>true</code> if this map contains any map entry
     *          with specified <code>value</code>, otherwise <code>false</code>
     */
    Single<Boolean> containsValue(Object value);

    /**
     * 若包含指定 {@code key} 的映射条目则返回 {@code true}，否则 {@code false}。
     *
     * @param key 映射键
     * @return <code>true</code> if this map contains map entry
     *          mapped by specified <code>key</code>, otherwise <code>false</code>
     */
    Single<Boolean> containsKey(Object key);

    /**
     * 返回本 Map 的元素数量。
     *
     * @return size
     */
    Single<Integer> size();

    /**
     * Removes map entries mapped by specified <code>keys</code>.
     * <p>
     * Works faster than <code>{@link #remove(Object)}</code> but not returning
     * the value.
     * <p>
     * If {@link MapWriter} is defined then <code>keys</code>are deleted in write-through mode.
     *
     * @param keys 键集合
     * @return the number of keys that were removed from the hash, not including specified but non existing keys
     */
    Single<Long> fastRemove(K... keys);

    /**
     * Stores the specified <code>value</code> mapped by specified <code>key</code>.
     * <p>
     * Works faster than <code>{@link #put(Object, Object)}</code> but not returning
     * previous value.
     * <p>
     * Returns <code>true</code> if key is a new key in the hash and value was set or
     * <code>false</code> if key already exists in the hash and the value was updated.
     * <p>
     * If {@link MapWriter} is defined then map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>true</code> if key is a new key in the hash and value was set.
     *         <code>false</code> if key already exists in the hash and the value was updated.
     */
    Single<Boolean> fastPut(K key, V value);

    /**
     * Stores the specified <code>value</code> mapped by specified <code>key</code>
     * only if there is no value with specified<code>key</code> stored before.
     * <p>
     * Returns <code>true</code> if key is a new one in the hash and value was set or
     * <code>false</code> if key already exists in the hash and change hasn't been made.
     * <p>
     * Works faster than <code>{@link #putIfAbsent(Object, Object)}</code> but not returning
     * the previous value associated with <code>key</code>
     * <p>
     * If {@link MapWriter} is defined then new map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>true</code> if key is a new one in the hash and value was set.
     *         <code>false</code> if key already exists in the hash and change hasn't been made.
     */
    Single<Boolean> fastPutIfAbsent(K key, V value);
    
    /**
     * 一次性读取全部键。
     *
     * @return 键集合
     */
    Single<Set<K>> readAllKeySet();

    /**
     * 一次性读取全部值。
     *
     * @return 值集合
     */
    Single<Collection<V>> readAllValues();

    /**
     * 一次性读取全部键值对。
     *
     * @return entries
     */
    Single<Set<Entry<K, V>>> readAllEntrySet();

    /**
     * 一次性读取全部 Map 条目到本地实例。
     *
     * @return map
     */
    Single<Map<K, V>> readAllMap();

    /**
     * 返回 {@code key} 映射的值；不存在时返回 {@code null}。
     * <p>
     * If map doesn't contain value for specified key and {@link MapLoader} is defined
     * then value will be loaded in read-through mode.
     *
     * @param key 键
     * @return the value mapped by defined <code>key</code> or {@code null} if value is absent
     */
    Maybe<V> get(K key);

    /**
     * 存储 {@code key}-{@code value}；键已存在时返回旧值。
     * <p>
     * If {@link MapWriter} is defined then map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return 先前关联的值
     */
    Maybe<V> put(K key, V value);

    /**
     * Removes map entry by specified <code>key</code> and returns value.
     * <p>
     * If {@link MapWriter} is defined then <code>key</code>is deleted in write-through mode.
     *
     * @param key 映射键
     * @return deleted value, <code>null</code> if map entry doesn't exist
     */
    Maybe<V> remove(K key);

    /**
     * Replaces previous value with a new <code>value</code> mapped by specified <code>key</code>.
     * Returns <code>null</code> if there is no map entry stored before and doesn't store new map entry.
     * <p>
     * If {@link MapWriter} is defined then new <code>value</code>is written in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return 先前关联的值
     *         or <code>null</code> if there is no map entry stored before and doesn't store new map entry
     */
    Maybe<V> replace(K key, V value);

    /**
     * Replaces previous <code>oldValue</code> with a <code>newValue</code> mapped by specified <code>key</code>.
     * Returns <code>false</code> if previous value doesn't exist or equal to <code>oldValue</code>.
     * <p>
     * If {@link MapWriter} is defined then <code>newValue</code>is written in write-through mode.
     *
     * @param key 映射键
     * @param oldValue - map old value
     * @param newValue - map new value
     * @return <code>true</code> if value has been replaced otherwise <code>false</code>.
     */
    Single<Boolean> replace(K key, V oldValue, V newValue);

    /**
     * Removes map entry only if it exists with specified <code>key</code> and <code>value</code>.
     * <p>
     * If {@link MapWriter} is defined then <code>key</code>is deleted in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>true</code> if map entry has been removed otherwise <code>false</code>.
     */
    Single<Boolean> remove(Object key, Object value);

    /**
     * Stores the specified <code>value</code> mapped by specified <code>key</code>
     * only if there is no value with specified<code>key</code> stored before.
     * <p>
     * If {@link MapWriter} is defined then new map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>null</code> if key is a new one in the hash and value was set.
     *         Previous value if key already exists in the hash and change hasn't been made.
     */
    Maybe<V> putIfAbsent(K key, V value);

    /**
     * Stores the specified <code>value</code> mapped by <code>key</code>
     * only if mapping already exists.
     * <p>
     * If {@link MapWriter} is defined then new map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>null</code> if key doesn't exist in the hash and value hasn't been set.
     *         Previous value if key already exists in the hash and new value has been stored.
     */
    Maybe<V> putIfExists(K key, V value);

    /**
     * 随机返回至多 {@code count} 个键。
     *
     * @param count - keys amount to return
     * @return random keys
     */
    Single<Set<K>> randomKeys(int count);

    /**
     * 随机返回至多 {@code count} 个键值对。
     *
     * @param count - entries amount to return
     * @return random entries
     */
    Single<Map<K, V>> randomEntries(int count);

    /**
     * Stores the specified <code>value</code> mapped by <code>key</code>
     * only if mapping already exists.
     * <p>
     * Returns <code>true</code> if key is a new one in the hash and value was set or
     * <code>false</code> if key already exists in the hash and change hasn't been made.
     * <p>
     * Works faster than <code>{@link #putIfExists(Object, Object)}</code> but doesn't return
     * previous value associated with <code>key</code>
     * <p>
     * If {@link MapWriter} is defined then new map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>true</code> if key already exists in the hash and new value has been stored.
     *         <code>false</code> if key doesn't exist in the hash and value hasn't been set.
     */
    Single<Boolean> fastPutIfExists(K key, V value);

    /**
     * 返回 Map 条目集合的迭代器。 
     * Map entries are loaded in batch. Batch size is <code>10</code>.
     * 
     * @see #readAllEntrySet()
     *  
     * @return iterator
     */
    Flowable<Map.Entry<K, V>> entryIterator();
    
    /**
     * 返回 Map 条目集合的迭代器。
     * Map entries are loaded in batch. Batch size is defined by <code>count</code> param. 
     * 
     * @see #readAllEntrySet()
     * 
     * @param count - size of entries batch
     * @return iterator
     */
    Flowable<Map.Entry<K, V>> entryIterator(int count);
    
    /**
     * 返回 Map 条目集合的迭代器。
     * Map entries are loaded in batch. Batch size is <code>10</code>. 
     * If <code>keyPattern</code> is not null then only entries mapped by matched keys of this pattern are loaded.
     * 
     *  Supported glob-style patterns:
     *  <p>
     *    h?llo subscribes to hello, hallo and hxllo
     *    <p>
     *    h*llo subscribes to hllo and heeeello
     *    <p>
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     * 
     * @see #readAllEntrySet()
     * 
     * @param pattern - key pattern
     * @return iterator
     */
    Flowable<Map.Entry<K, V>> entryIterator(String pattern);
    
    /**
     * 返回 Map 条目集合的迭代器。
     * Map entries are loaded in batch. Batch size is defined by <code>count</code> param. 
     * If <code>keyPattern</code> is not null then only entries mapped by matched keys of this pattern are loaded.
     * 
     *  Supported glob-style patterns:
     *  <p>
     *    h?llo subscribes to hello, hallo and hxllo
     *    <p>
     *    h*llo subscribes to hllo and heeeello
     *    <p>
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     * 
     * @see #readAllEntrySet()
     * 
     * @param pattern - key pattern
     * @param count - size of entries batch
     * @return iterator
     */
    Flowable<Map.Entry<K, V>> entryIterator(String pattern, int count);

    /**
     * 返回本 Map 值集合的迭代器。 
     * Values are loaded in batch. Batch size is <code>10</code>.
     * 
     * @see #readAllValues()
     * 
     * @return iterator
     */
    Flowable<V> valueIterator();
    
    /**
     * 返回本 Map 值集合的迭代器。
     * Values are loaded in batch. Batch size is defined by <code>count</code> param. 
     * 
     * @see #readAllValues()
     * 
     * @param count - size of values batch
     * @return iterator
     */
    Flowable<V> valueIterator(int count);
    
    /**
     * 返回本 Map 值集合的迭代器。
     * Values are loaded in batch. Batch size is <code>10</code>. 
     * If <code>keyPattern</code> is not null then only values mapped by matched keys of this pattern are loaded.
     * <p>
     * Use <code>org.redisson.client.codec.StringCodec</code> for Map keys.
     * <p>
     * Usage example:
     * <pre>
     *     Codec valueCodec = ...
     *     RMapRx<String, MyObject> map = redissonClient.getMap("simpleMap", new CompositeCodec(StringCodec.INSTANCE, valueCodec, valueCodec));
     *
     *     // or
     *
     *     RMapRx<String, String> map = redissonClient.getMap("simpleMap", StringCodec.INSTANCE);
     * </pre>
     * <pre>
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     * </pre>
     * @see #readAllValues()
     * 
     * @param pattern - key pattern
     * @return iterator
     */
    Flowable<V> valueIterator(String pattern);
    
    /**
     * 返回本 Map 值集合的迭代器。
     * Values are loaded in batch. Batch size is defined by <code>count</code> param.
     * If <code>keyPattern</code> is not null then only values mapped by matched keys of this pattern are loaded.
     * <p>
     * Use <code>org.redisson.client.codec.StringCodec</code> for Map keys.
     * <p>
     * Usage example:
     * <pre>
     *     Codec valueCodec = ...
     *     RMapRx<String, MyObject> map = redissonClient.getMap("simpleMap", new CompositeCodec(StringCodec.INSTANCE, valueCodec, valueCodec));
     *
     *     // or
     *
     *     RMapRx<String, String> map = redissonClient.getMap("simpleMap", StringCodec.INSTANCE);
     * </pre>
     * <pre>
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     * </pre>
     * @see #readAllValues()
     * 
     * @param pattern - key pattern
     * @param count - size of values batch
     * @return iterator
     */
    Flowable<V> valueIterator(String pattern, int count);

    /**
     * 返回本 Map 键集合的迭代器。 
     * Keys are loaded in batch. Batch size is <code>10</code>.
     * 
     * @see #readAllKeySet()
     * 
     * @return iterator
     */
    Flowable<K> keyIterator();
    
    /**
     * 返回本 Map 键集合的迭代器。
     * Keys are loaded in batch. Batch size is defined by <code>count</code> param. 
     * 
     * @see #readAllKeySet()
     * 
     * @param count - size of keys batch
     * @return iterator
     */
    Flowable<K> keyIterator(int count);
    
    /**
     * 返回本 Map 键集合的迭代器。 
     * If <code>pattern</code> is not null then only keys match this pattern are loaded.
     * <p>
     * Use <code>org.redisson.client.codec.StringCodec</code> for Map keys.
     * <p>
     * Usage example:
     * <pre>
     *     Codec valueCodec = ...
     *     RMapRx<String, MyObject> map = redissonClient.getMap("simpleMap", new CompositeCodec(StringCodec.INSTANCE, valueCodec, valueCodec));
     *
     *     // or
     *
     *     RMapRx<String, String> map = redissonClient.getMap("simpleMap", StringCodec.INSTANCE);
     * </pre>
     * <pre>
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     * </pre>
     * @see #readAllKeySet()
     * 
     * @param pattern key pattern
     * @return iterator
     */
    Flowable<K> keyIterator(String pattern);

    /**
     * 返回本 Map 键集合的迭代器。
     * If <code>pattern</code> is not null then only keys match this pattern are loaded.
     * Keys are loaded in batch. Batch size is defined by <code>count</code> param.
     * <p>
     * Use <code>org.redisson.client.codec.StringCodec</code> for Map keys.
     * <p>
     * Usage example:
     * <pre>
     *     Codec valueCodec = ...
     *     RMapRx<String, MyObject> map = redissonClient.getMap("simpleMap", new CompositeCodec(StringCodec.INSTANCE, valueCodec, valueCodec));
     *
     *     // or
     *
     *     RMapRx<String, String> map = redissonClient.getMap("simpleMap", StringCodec.INSTANCE);
     * </pre>
     * <pre>
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     * </pre>
     * @see #readAllKeySet()
     * 
     * @param pattern key pattern
     * @param count size of keys batch
     * @return iterator
     */
    Flowable<K> keyIterator(String pattern, int count);
    
    /**
     * 返回与键关联的 {@link RPermitExpirableSemaphore}。
     * 
     * @param key 映射键
     * @return permitExpirableSemaphore
     */
    RPermitExpirableSemaphoreRx getPermitExpirableSemaphore(K key);

    /**
     * 返回与键关联的 {@link RSemaphore}。
     * 
     * @param key 映射键
     * @return semaphore
     */
    RSemaphoreRx getSemaphore(K key);
    
    /**
     * 返回与键关联的 {@link RLock}。
     * 
     * @param key 映射键
     * @return fairLock
     */
    RLockRx getFairLock(K key);
    
    /**
     * 返回与键关联的 {@link RReadWriteLock}。
     * 
     * @param key 映射键
     * @return readWriteLock
     */
    RReadWriteLockRx getReadWriteLock(K key);
    
    /**
     * 返回与键关联的 {@link RLock}。
     * 
     * @param key 映射键
     * @return lock
     */
    RLockRx getLock(K key);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.MapPutListener
     * @see org.redisson.api.listener.MapRemoveListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    Single<Integer> addListener(ObjectListener listener);


}
