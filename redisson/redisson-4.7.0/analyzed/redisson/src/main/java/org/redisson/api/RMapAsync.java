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

import org.redisson.api.map.MapLoader;
import org.redisson.api.map.MapWriter;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 基于 Redis 的 {@link java.util.concurrent.ConcurrentMap} 与 {@link java.util.Map} 异步 API。
 * <p>使用键序列化状态而非 hashCode/equals；不允许 {@code null} 键或值。
 * 各方法返回 {@link RFuture}；支持 MapLoader/MapWriter 与 per-key 分布式锁。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RMapAsync<K, V> extends RExpirableAsync {

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
    RFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction);

    /**
     * 根据键及其当前映射值计算新映射。
     *
     * @param key 映射键
     * @param remappingFunction - function to compute a value
     * @return the new value associated with the specified key, or {@code null} if none
     */
    RFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    /**
     * 仅当键尚未映射时计算并存储新值。
     *
     * @param key 映射键
     * @param mappingFunction 映射函数
     * @return current or new computed value associated with
     *         the specified key, or {@code null} if the computed value is null
     */
    RFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction);

    /**
     * 仅当键已有映射时计算并更新值。
     *
     * @param key 映射键
     * @param remappingFunction - function to compute a value
     * @return the new value associated with the specified key, or null if none
     */
    RFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    /**
     * 使用 {@link org.redisson.api.map.MapLoader} 加载全部映射条目。
     * 
     * @param replaceExistingValues 是否替换已有值  
     * @param parallelism 并行度
     * @return 无返回值
     */
    RFuture<Void> loadAllAsync(boolean replaceExistingValues, int parallelism);
    
    /**
     * 使用 {@link org.redisson.api.map.MapLoader} 加载指定 {@code keys} 的条目。
     * 
     * @param keys 键集合
     * @param replaceExistingValues 是否替换已有值
     * @param parallelism 并行度
     * @return 无返回值
     */
    RFuture<Void> loadAllAsync(Set<? extends K> keys, boolean replaceExistingValues, int parallelism);
    
    /**
     * Returns size of value mapped by key in bytes
     * 
     * @param key 映射键
     * @return size of value
     */
    RFuture<Integer> valueSizeAsync(K key);
    
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
    RFuture<Map<K, V>> getAllAsync(Set<K> keys);

    /**
     * Stores map entries specified in <code>map</code> object in batch mode.
     * <p>
     * If {@link MapWriter} is defined then map entries will be stored in write-through mode.
     *
     * @param map mappings to be stored in this map
     * @return 无返回值
     */
    RFuture<Void> putAllAsync(Map<? extends K, ? extends V> map);

    /**
     * Stores map entries specified in <code>map</code> object in batch mode.
     * Batch inserted by chunks limited by <code>batchSize</code> value
     * to avoid OOM and/or Redis response timeout error for map with big size.
     * <p>
     * If {@link MapWriter} is defined then map entries are stored in write-through mode.
     *
     * @param map mappings to be stored in this map
     * @param batchSize - size of map entries batch
     * @return 无返回值
     */
    RFuture<Void> putAllAsync(Map<? extends K, ? extends V> map, int batchSize);

    /**
     * 随机返回至多 {@code count} 个键。
     *
     * @param count - keys amount to return
     * @return random keys
     */
    RFuture<Set<K>> randomKeysAsync(int count);

    /**
     * 随机返回至多 {@code count} 个键值对。
     *
     * @param count - entries amount to return
     * @return random entries
     */
    RFuture<Map<K, V>> randomEntriesAsync(int count);

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
    RFuture<V> addAndGetAsync(K key, Number delta);

    /**
     * Returns <code>true</code> if this map contains any map entry
     * with specified <code>value</code>, otherwise <code>false</code>
     *
     * @param value 映射值
     * @return <code>true</code> if this map contains any map entry
     *          with specified <code>value</code>, otherwise <code>false</code>
     */
    RFuture<Boolean> containsValueAsync(Object value);

    /**
     * 若包含指定 {@code key} 的映射条目则返回 {@code true}，否则 {@code false}。
     *
     * @param key 映射键
     * @return <code>true</code> if this map contains map entry
     *          mapped by specified <code>key</code>, otherwise <code>false</code>
     */
    RFuture<Boolean> containsKeyAsync(Object key);

    /**
     * 返回本 Map 的元素数量。
     * 
     * @return size
     */
    RFuture<Integer> sizeAsync();

    /**
     * 通过可迭代对象返回本 Map 的全部值。
     * Values are loaded in batch. Batch size is <code>10</code>.
     *
     * @return Asynchronous Iterable object
     */
    AsyncIterator<V> valuesAsync();

    /**
     * 通过可迭代对象返回本 Map 的全部值。
     * Values are loaded in batch. Batch size is <code>10</code>.
     * If <code>keyPattern</code> is not null then only values mapped by matched keys of this pattern are loaded.
     * <p>
     * Use <code>org.redisson.client.codec.StringCodec</code> for Map keys.
     * <p>
     * Usage example:
     * <pre>
     *     Codec valueCodec = ...
     *     RMap<String, MyObject> map = redissonClient.getMap("simpleMap", new CompositeCodec(StringCodec.INSTANCE, valueCodec, valueCodec));
     *
     *     // or
     *
     *     RMap<String, String> map = redissonClient.getMap("simpleMap", StringCodec.INSTANCE);
     * </pre>
     * <pre>
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     * </pre>
     *
     * @param keyPattern - key pattern
     * @return Asynchronous Iterable object
     */
    AsyncIterator<V> valuesAsync(String keyPattern);

    /**
     * 通过可迭代对象返回本 Map 的全部值。
     * Values are loaded in batch. Batch size is <code>10</code>.
     * If <code>keyPattern</code> is not null then only values mapped by matched keys of this pattern are loaded.
     * <p>
     * Use <code>org.redisson.client.codec.StringCodec</code> for Map keys.
     * <p>
     * Usage example:
     * <pre>
     *     Codec valueCodec = ...
     *     RMap<String, MyObject> map = redissonClient.getMap("simpleMap", new CompositeCodec(StringCodec.INSTANCE, valueCodec, valueCodec));
     *
     *     // or
     *
     *     RMap<String, String> map = redissonClient.getMap("simpleMap", StringCodec.INSTANCE);
     * </pre>
     * <pre>
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     * </pre>
     *
     * @param keyPattern - key pattern
     * @param count - size of values batch
     * @return Asynchronous Iterable object
     */
    AsyncIterator<V> valuesAsync(String keyPattern, int count);

    /**
     * 通过可迭代对象返回本 Map 的全部值。
     * Values are loaded in batch. Batch size is defined by <code>count</code> param.
     *
     * @param count - size of values batch
     * @return Asynchronous Iterable object
     */
    AsyncIterator<V> valuesAsync(int count);

    /**
     * Returns keys of this map using iterable.
     * Keys are loaded in batch. Batch size is <code>10</code>.
     *
     * @return Asynchronous Iterable object
     */
    AsyncIterator<K> keysAsync();

    /**
     * Returns keys of this map using iterable.
     * Keys are loaded in batch. Batch size is defined by <code>count</code> param.
     *
     * @param count - size of keys batch
     * @return Asynchronous Iterable object
     */
    AsyncIterator<K> keysAsync(int count);

    /**
     * 通过可迭代对象返回本 Map 的全部键值对。
     * Map entries are loaded in batch. Batch size is <code>10</code>.
     *
     * @return Asynchronous Iterable object
     */
    AsyncIterator<java.util.Map.Entry<K, V>> entrySetAsync();

    /**
     * 通过可迭代对象返回本 Map 的全部键值对。
     * Map entries are loaded in batch. Batch size is <code>10</code>.
     * If <code>keyPattern</code> is not null then only entries mapped by matched keys of this pattern are loaded.
     * <p>
     * Use <code>org.redisson.client.codec.StringCodec</code> for Map keys.
     * <p>
     * Usage example:
     * <pre>
     *     Codec valueCodec = ...
     *     RMap<String, MyObject> map = redissonClient.getMap("simpleMap", new CompositeCodec(StringCodec.INSTANCE, valueCodec, valueCodec));
     *
     *     // or
     *
     *     RMap<String, String> map = redissonClient.getMap("simpleMap", StringCodec.INSTANCE);
     * </pre>
     * <pre>
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     * </pre>
     *
     * @param keyPattern key pattern
     * @return Asynchronous Iterable object
     */
    AsyncIterator<java.util.Map.Entry<K, V>> entrySetAsync(String keyPattern);

    /**
     * 通过可迭代对象返回本 Map 的全部键值对。
     * Map entries are loaded in batch. Batch size is defined by <code>count</code> param.
     * If <code>keyPattern</code> is not null then only entries mapped by matched keys of this pattern are loaded.
     * <p>
     * Use <code>org.redisson.client.codec.StringCodec</code> for Map keys.
     * <p>
     * Usage example:
     * <pre>
     *     Codec valueCodec = ...
     *     RMap<String, MyObject> map = redissonClient.getMap("simpleMap", new CompositeCodec(StringCodec.INSTANCE, valueCodec, valueCodec));
     *
     *     // or
     *
     *     RMap<String, String> map = redissonClient.getMap("simpleMap", StringCodec.INSTANCE);
     * </pre>
     * <pre>
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     * </pre>
     *
     * @param keyPattern key pattern
     * @param count size of entries batch
     * @return Asynchronous Iterable object
     */
    AsyncIterator<java.util.Map.Entry<K, V>> entrySetAsync(String keyPattern, int count);

    /**
     * 通过可迭代对象返回本 Map 的全部键值对。
     * Map entries are loaded in batch. Batch size is defined by <code>count</code> param.
     *
     * @param count - size of entries batch
     * @return Asynchronous Iterable object
     */
    AsyncIterator<java.util.Map.Entry<K, V>> entrySetAsync(int count);

    /**
     * Removes map entries mapped by specified <code>keys</code>.
     * <p>
     * Works faster than <code>{@link #removeAsync(Object)}</code> but not returning
     * the value.
     * <p>
     * If {@link MapWriter} is defined then <code>keys</code>are deleted in write-through mode.
     *
     * @param keys 键集合
     * @return the number of keys that were removed from the hash, not including specified but non existing keys
     */
    RFuture<Long> fastRemoveAsync(K... keys);

    /**
     * Stores the specified <code>value</code> mapped by specified <code>key</code>.
     * <p>
     * Works faster than <code>{@link #putAsync(Object, Object)}</code> but not returning
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
    RFuture<Boolean> fastPutAsync(K key, V value);

    /**
     * Replaces previous value with a new <code>value</code> mapped by specified <code>key</code>.
     * <p>
     * Works faster than <code>{@link #replaceAsync(Object, Object)}</code> but not returning
     * the previous value.
     * <p>
     * Returns <code>true</code> if key exists and value was updated or
     * <code>false</code> if key doesn't exists and value wasn't updated.
     * <p>
     * If {@link MapWriter} is defined then new map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>true</code> if key exists and value was updated.
     *         <code>false</code> if key doesn't exists and value wasn't updated.
     */
    RFuture<Boolean> fastReplaceAsync(K key, V value);
    
    /**
     * Stores the specified <code>value</code> mapped by specified <code>key</code>
     * only if there is no value with specified<code>key</code> stored before.
     * <p>
     * Returns <code>true</code> if key is a new one in the hash and value was set or
     * <code>false</code> if key already exists in the hash and change hasn't been made.
     * <p>
     * Works faster than <code>{@link #putIfAbsentAsync(Object, Object)}</code> but not returning
     * the previous value associated with <code>key</code>
     * <p>
     * If {@link MapWriter} is defined then new map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>true</code> if key is a new one in the hash and value was set.
     *         <code>false</code> if key already exists in the hash and change hasn't been made.
     */
    RFuture<Boolean> fastPutIfAbsentAsync(K key, V value);

    /**
     * Stores the specified <code>value</code> mapped by <code>key</code>
     * only if mapping already exists.
     * <p>
     * Returns <code>true</code> if key is a new one in the hash and value was set or
     * <code>false</code> if key already exists in the hash and change hasn't been made.
     * <p>
     * Works faster than <code>{@link #putIfExistsAsync(Object, Object)}</code> but doesn't return
     * previous value associated with <code>key</code>
     * <p>
     * If {@link MapWriter} is defined then new map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>true</code> if key already exists in the hash and new value has been stored.
     *         <code>false</code> if key doesn't exist in the hash and value hasn't been set.
     */
    RFuture<Boolean> fastPutIfExistsAsync(K key, V value);

    /**
     * 一次性读取全部键。
     *
     * @return 键集合
     */
    RFuture<Set<K>> readAllKeySetAsync();

    /**
     * Read all keys mapped by matched keys of this pattern at once
     *
     * @param keyPattern - key Pattern
     * @return 键集合
     */
    RFuture<Set<K>> readAllKeySetAsync(String keyPattern);

    /**
     * 一次性读取全部值。
     *
     * @return 值集合
     */
    RFuture<Collection<V>> readAllValuesAsync();

    /**
     * Read all values mapped by matched keys of this pattern at once
     *
     * @param keyPattern - key Pattern
     * @return 值集合
     */
    RFuture<Collection<V>> readAllValuesAsync(String keyPattern);

    /**
     * 一次性读取全部键值对。
     *
     * @return entries
     */
    RFuture<Set<Entry<K, V>>> readAllEntrySetAsync();


    /**
     * Read all entries mapped by matched keys of this pattern at once
     *
     * @param keyPattern - key Pattern
     * @return entries
     */
    RFuture<Set<Entry<K, V>>> readAllEntrySetAsync(String keyPattern);

    /**
     * 一次性读取全部 Map 条目到本地实例。
     *
     * @return map
     */
    RFuture<Map<K, V>> readAllMapAsync();

    /**
     * 返回 {@code key} 映射的值；不存在时返回 {@code null}。
     * <p>
     * If map doesn't contain value for specified key and {@link MapLoader} is defined
     * then value will be loaded in read-through mode.
     *
     * @param key 键
     * @return the value mapped by defined <code>key</code> or {@code null} if value is absent
     */
    RFuture<V> getAsync(K key);

    /**
     * 存储 {@code key}-{@code value}；键已存在时返回旧值。
     * <p>
     * If {@link MapWriter} is defined then map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return 先前关联的值
     */
    RFuture<V> putAsync(K key, V value);

    /**
     * Removes map entry by specified <code>key</code> and returns value.
     * <p>
     * If {@link MapWriter} is defined then <code>key</code>is deleted in write-through mode.
     *
     * @param key 映射键
     * @return deleted value, <code>null</code> if map entry doesn't exist
     */
    RFuture<V> removeAsync(K key);

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
    RFuture<V> replaceAsync(K key, V value);

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
    RFuture<Boolean> replaceAsync(K key, V oldValue, V newValue);

    /**
     * Removes map entry only if it exists with specified <code>key</code> and <code>value</code>.
     * <p>
     * If {@link MapWriter} is defined then <code>key</code>is deleted in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>true</code> if map entry has been removed otherwise <code>false</code>.
     */
    RFuture<Boolean> removeAsync(Object key, Object value);

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
    RFuture<V> putIfAbsentAsync(K key, V value);

    /**
     * Stores the specified <code>value</code> mapped by <code>key</code>
     * only if mapping already exists.
     * <p>
     * If {@link MapWriter} is defined then new map entry is stored in write-through mode.
     *
     * @param key 映射键
     * @param value 映射值
     * @return <code>null</code> if key is doesn't exists in the hash and value hasn't been set.
     *         Previous value if key already exists in the hash and new value has been stored.
     */
    RFuture<V> putIfExistsAsync(K key, V value);

    /**
     * Clears map without removing options data used during map creation.
     *
     * @return <code>true</code> if map was cleared <code>false</code> if not
     */
    RFuture<Boolean> clearAsync();

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
    RFuture<Integer> addListenerAsync(ObjectListener listener);

}
