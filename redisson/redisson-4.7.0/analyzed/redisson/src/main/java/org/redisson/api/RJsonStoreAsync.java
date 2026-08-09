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

import org.redisson.codec.JsonCodec;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link RJsonStore} 异步 API；各方法返回 {@link RFuture}。
 * <p>实现仅 Redisson PRO 提供。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RJsonStoreAsync<K, V> extends RExpirableAsync {

    /**
     * Gets value by specified key and JSONPath
     *
     * @param key 条目键
     * @param codec entry value codec
     * @param paths JSON 路径
     * @return 条目值
     *
     * @param <T> 对象类型
     */
    <T> RFuture<T> getAsync(K key, JsonCodec codec, String... paths);

    /**
     * Sets value by specified key and JSONPath only if previous value is empty.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value entry value
     * @return {@code true} if successful, or {@code false} if
     *         value was already set
     */
    RFuture<Boolean> setIfAbsentAsync(K key, String path, Object value);

    /**
     * Sets value by specified key and JSONPath only if previous value is non-empty.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value object
     * @return {@code true} if successful, or {@code false} if
     *         element wasn't set
     */
    RFuture<Boolean> setIfExistsAsync(K key, String path, Object value);

    /**
     * Atomically sets the value to the given updated value
     * by specified key and JSONPath, only if serialized state of
     * the current value equals to serialized state of the expected value.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param expect 期望值
     * @param update 新值
     * @return {@code true} if successful; or {@code false} if the actual value
     *         was not equal to the expected value.
     */
    RFuture<Boolean> compareAndSetAsync(K key, String path, Object expect, Object update);

    /**
     * Retrieves current value stored by specified key and JSONPath then
     * replaces it with new value.
     *
     * @param key 条目键
     * @param codec entry value codec
     * @param path JSON 路径
     * @param newValue value to set
     * @return previous value
     */
    <T> RFuture<T> getAndSetAsync(K key, JsonCodec codec, String path, Object newValue);

    /**
     * Stores value by specified key and JSONPath.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value value to set
     */
    RFuture<Void> setAsync(K key, String path, Object value);

    /**
     * Returns size of string data by specified key and JSONPath
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return size of string
     */
    RFuture<Long> stringSizeAsync(K key, String path);

    /**
     * Returns list of string data size by specified key and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return list of string data sizes
     */
    RFuture<List<Long>> stringSizeMultiAsync(K key, String path);

    /**
     * Appends string data to element specified by specified key and JSONPath.
     * Returns new size of string data.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value data
     * @return size of string data
     */
    RFuture<Long> stringAppendAsync(K key, String path, Object value);

    /**
     * Appends string data to elements by specified key and JSONPath.
     * Returns new size of string data.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value data
     * @return list of string data sizes
     */
    RFuture<List<Long>> stringAppendMultiAsync(K key, String path, Object value);

    /**
     * Appends values to array by specified key and JSONPath.
     * Returns new size of array.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param values values to append
     * @return size of array
     */
    RFuture<Long> arrayAppendAsync(K key, String path, Object... values);

    /**
     * Appends values to arrays by specified key and JSONPath.
     * Returns new size of arrays.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param values values to append
     * @return list of arrays size
     */
    RFuture<List<Long>> arrayAppendMultiAsync(K key, String path, Object... values);

    /**
     * Returns index of object in array by specified key and JSONPath.
     * 返回 {@code -1} 表示未找到。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value value to search
     * @return index in array
     */
    RFuture<Long> arrayIndexAsync(K key, String path, Object value);

    /**
     * Returns index of object in arrays by specified key and JSONPath.
     * 返回 {@code -1} 表示未找到。
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value value to search
     * @return list of index in arrays
     */
    RFuture<List<Long>> arrayIndexMultiAsync(K key, String path, Object value);

    /**
     * Returns index of object in array by specified key and JSONPath
     * in range between <code>start</code> (inclusive) and <code>end</code> (exclusive) indexes.
     * 返回 {@code -1} 表示未找到。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value value to search
     * @param start start index, inclusive
     * @param end end index, exclusive
     * @return index in array
     */
    RFuture<Long> arrayIndexAsync(K key, String path, Object value, long start, long end);

    /**
     * Returns index of object in arrays by specified key and JSONPath
     * in range between <code>start</code> (inclusive) and <code>end</code> (exclusive) indexes.
     * 返回 {@code -1} 表示未找到。
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value value to search
     * @param start start index, inclusive
     * @param end end index, exclusive
     * @return list of index in arrays
     */
    RFuture<List<Long>> arrayIndexMultiAsync(K key, String path, Object value, long start, long end);

    /**
     * Inserts values into array by specified key and JSONPath.
     * Values are inserted at defined <code>index</code>.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param index array index at which values are inserted
     * @param values values to insert
     * @return size of array
     */
    RFuture<Long> arrayInsertAsync(K key, String path, long index, Object... values);

    /**
     * Inserts values into arrays by specified key and JSONPath.
     * Values are inserted at defined <code>index</code>.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param index array index at which values are inserted
     * @param values values to insert
     * @return list of arrays size
     */
    RFuture<List<Long>> arrayInsertMultiAsync(K key, String path, long index, Object... values);

    /**
     * Returns size of array by specified key and JSONPath.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return size of array
     */
    RFuture<Long> arraySizeAsync(K key, String path);

    /**
     * Returns size of arrays by specified key and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return list of arrays size
     */
    RFuture<List<Long>> arraySizeMultiAsync(K key, String path);

    /**
     * Polls last element of array by specified key and JSONPath.
     *
     * @param key 条目键
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @return last element
     *
     * @param <T> 对象类型
     */
    <T> RFuture<T> arrayPollLastAsync(K key, JsonCodec codec, String path);

    /**
     * Polls last element of arrays by specified key and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @return list of last elements
     *
     * @param <T> 对象类型
     */
    <T> RFuture<List<T>> arrayPollLastMultiAsync(K key, JsonCodec codec, String path);

    /**
     * Polls first element of array by specified key and JSONPath.
     *
     * @param key 条目键
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @return first element
     *
     * @param <T> 对象类型
     */
    <T> RFuture<T> arrayPollFirstAsync(K key, JsonCodec codec, String path);

    /**
     * Polls first element of arrays by specified key and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @return list of first elements
     *
     * @param <T> 对象类型
     */
    <T> RFuture<List<T>> arrayPollFirstMultiAsync(K key, JsonCodec codec, String path);

    /**
     * Pops element located at index of array by specified key and JSONPath.
     *
     * @param key 条目键
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @param index array index
     * @return element
     *
     * @param <T> 对象类型
     */
    <T> RFuture<T> arrayPopAsync(K key, JsonCodec codec, String path, long index);

    /**
     * Pops elements located at index of arrays by specified key and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @param index array index
     * @return list of elements
     *
     * @param <T> 对象类型
     */
    <T> RFuture<List<T>> arrayPopMultiAsync(K key, JsonCodec codec, String path, long index);

    /**
     * Trims array by specified key and JSONPath in range
     * between <code>start</code> (inclusive) and <code>end</code> (inclusive) indexes.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param start start index, inclusive
     * @param end end index, inclusive
     * @return length of array
     */
    RFuture<Long> arrayTrimAsync(K key, String path, long start, long end);

    /**
     * Trims arrays by specified key and JSONPath in range
     * between <code>start</code> (inclusive) and <code>end</code> (inclusive) indexes.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param start start index, inclusive
     * @param end end index, inclusive
     * @return length of array
     */
    RFuture<List<Long>> arrayTrimMultiAsync(K key, String path, long start, long end);

    /**
     * Clears value by specified key
     *
     * @param key 条目键
     * @return {@code true} if successful, or {@code false} if
     *         entry doesn't exist
     */
    RFuture<Boolean> clearAsync(K key);

    /**
     * Clears json containers by specified keys.
     *
     * @param keys entry keys
     * @return number of cleared containers
     */
    RFuture<Long> clearAsync(Set<K> keys);

    /**
     * Clears json container by specified keys and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @param keys entry keys
     * @return number of cleared containers
     */
    RFuture<Long> clearAsync(String path, Set<K> keys);

    /**
     * Increments the current value specified by key and JSONPath.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param delta increment value
     * @return the updated value
     */
    <T extends Number> RFuture<T> incrementAndGetAsync(K key, String path, T delta);

    /**
     * Increments the current values specified by key and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param delta increment value
     * @return list of updated value
     */
    <T extends Number> RFuture<List<T>> incrementAndGetMultiAsync(K key, String path, T delta);

    /**
     * Merges object into element by the specified key and JSONPath.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value value to merge
     */
    RFuture<Void> mergeAsync(K key, String path, Object value);

    /**
     * Returns keys amount in JSON container by specified key
     *
     * @param key 条目键
     * @return 键集合 amount
     */
    RFuture<Long> countKeysAsync(K key);

    /**
     * Returns keys amount in JSON container specified by key and JSONPath
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return 键集合 amount
     */
    RFuture<Long> countKeysAsync(K key, String path);

    /**
     * Returns list of keys amount in JSON containers specified by key and JSONPath
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return list of keys amount
     */
    RFuture<List<Long>> countKeysMultiAsync(K key, String path);

    /**
     * Returns list of keys in JSON container by specified key
     *
     * @return list of keys
     */
    RFuture<List<String>> getKeysAsync(K key);

    /**
     * Returns list of keys in JSON container by specified key and JSONPath
     *
     * @param path JSON 路径
     * @return list of keys
     */
    RFuture<List<String>> getKeysAsync(K key, String path);

    /**
     * Returns list of keys in JSON containers by specified key and JSONPath
     *
     * @param path JSON 路径
     * @return list of keys
     */
    RFuture<List<List<String>>> getKeysMultiAsync(K key, String path);

    /**
     * Toggle boolean value by specified key and JSONPath
     *
     * @param path JSON 路径
     * @return new boolean value
     */
    RFuture<Boolean> toggleAsync(K key, String path);

    /**
     * Toggle boolean values by specified key and JSONPath
     *
     * @param path JSON 路径
     * @return list of boolean values
     */
    RFuture<List<Boolean>> toggleMultiAsync(K key, String path);

    /**
     * Returns type of value
     *
     * @return type of element
     */
    RFuture<JsonType> getTypeAsync(K key);

    /**
     * Returns type of element specified by key and JSONPath
     *
     * @param path JSON 路径
     * @return type of element
     */
    RFuture<JsonType> getTypeAsync(K key, String path);

    /**
     * Deletes entry by specified key
     *
     * @param key 条目键
     * @return {@code true} if successful, or {@code false} if
     *         entry doesn't exist
     */
    RFuture<Boolean> deleteAsync(K key);

    /**
     * Deletes JSON elements specified by keys
     *
     * @param keys entry keys
     * @return number of deleted elements
     */
    RFuture<Long> deleteAsync(Set<K> keys);

    /**
     * Deletes JSON elements specified by keys and JSONPath
     *
     * @param path JSON 路径
     * @param keys entry keys
     * @return number of deleted elements
     */
    RFuture<Long> deleteAsync(String path, Set<K> keys);

    /**
     * Returns size of entry in bytes specified by key.
     *
     * @param key 条目键
     * @return entry size
     */
    RFuture<Long> sizeInMemoryAsync(K key);

    /**
     * Retrieves value by specified key.
     *
     * @param key 条目键
     * @return element
     */
    RFuture<V> getAsync(K key);

    /**
     * Retrieves values by specified keys.
     *
     * @param keys entry keys
     * @return map with entries where value mapped by key
     */
    RFuture<Map<K, V>> getAsync(Set<K> keys);

    /**
     * Retrieves values by specified keys and JSONPath.
     *
     * @param path JSON 路径
     * @param keys entry keys
     * @return map with entries where value mapped by key
     */
    RFuture<Map<K, V>> getAsync(String path, Set<K> keys);

    /**
     * Retrieves entry value by specified key and removes it.
     *
     * @param key 条目键
     * @return element
     */
    RFuture<V> getAndDeleteAsync(K key);

    /**
     * Sets value only if entry doesn't exist.
     *
     * @param key 条目键
     * @param value value to set
     * @return {@code true} if successful, or {@code false} if
     *         element was already set
     */
    RFuture<Boolean> setIfAbsentAsync(K key, V value);

    /**
     * Sets value with defined duration only if entry doesn't exist.
     *
     * @param key 条目键
     * @param value value to set
     * @param duration expiration duration
     * @return {@code true} if successful, or {@code false} if
     *         element was already set
     */
    RFuture<Boolean> setIfAbsentAsync(K key, V value, Duration duration);

    /**
     * Sets value only if entry already exists.
     *
     * @param key 条目键
     * @param value value to set
     * @return {@code true} if successful, or {@code false} if
     *         element wasn't set
     */
    RFuture<Boolean> setIfExistsAsync(K key, V value);

    /**
     * Sets <code>value</code> with expiration <code>duration</code> only if entry already exists.
     *
     * @param key 条目键
     * @param value value to set
     * @param duration expiration duration
     * @return {@code true} if successful, or {@code false} if
     *         element wasn't set
     */
    RFuture<Boolean> setIfExistsAsync(K key, V value, Duration duration);

    /**
     * Atomically sets the value to the given updated value
     * by specified key only if serialized state of the current value equals
     * to serialized state of the expected value.
     *
     * @param key 条目键
     * @param expect 期望值
     * @param update 新值
     * @return {@code true} if successful; or {@code false} if the actual value
     *         was not equal to the expected value.
     */
    RFuture<Boolean> compareAndSetAsync(K key, V expect, V update);

    /**
     * Retrieves current value by specified key and replaces it with new value.
     *
     * @param key 条目键
     * @param newValue value to set
     * @return previous value
     */
    RFuture<V> getAndSetAsync(K key, V newValue);

    /**
     * Retrieves current value by specified key and replaces it
     * with value and defines expiration <code>duration</code>.
     *
     * @param key 条目键
     * @param value value to set
     * @param duration expiration duration
     * @return previous value
     */
    RFuture<V> getAndSetAsync(K key, V value, Duration duration);

    /**
     * Retrieves current value by specified key and sets an expiration duration for it.
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param key 条目键
     * @param duration of object time to live interval
     * @return value
     */
    RFuture<V> getAndExpireAsync(K key, Duration duration);

    /**
     * Retrieves current value by specified key and sets an expiration date for it.
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param key 条目键
     * @param time of exact object expiration moment
     * @return value
     */
    RFuture<V> getAndExpireAsync(K key, Instant time);

    /**
     * Retrieves current value by specified key and clears expiration date set before.
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param key 条目键
     * @return value
     */
    RFuture<V> getAndClearExpireAsync(K key);

    /**
     * Stores value by specified key.
     *
     * @param key 条目键
     * @param value value to set
     */
    RFuture<Void> setAsync(K key, V value);

    /**
     * Stores values by specified keys.
     *
     * @param entries entries to store
     */
    RFuture<Void> setAsync(Map<K, V> entries);

    /**
     * Stores values by specified keys and JSONPath.
     *
     * @param path JSONPath
     * @param entries entries to store
     */
    RFuture<Void> setAsync(String path, Map<K, V> entries);

    /**
     * Stores value by specified key with defined expiration duration.
     *
     * @param key 条目键
     * @param value value to set
     * @param duration expiration duration
     */
    RFuture<Void> setAsync(K key, V value, Duration duration);

    /**
     * Stores values by specified keys with defined expiration duration.
     *
     * @param entries entries to store
     * @param duration expiration duration
     */
    RFuture<Void> setAsync(Map<K, V> entries, Duration duration);

    /**
     * Sets value by specified key and keep existing TTL.
     * <p>
     * Requires <b>Redis 6.0.0 and higher.</b>
     *
     * @param value value to set
     * @return 无返回值
     */
    RFuture<Void> setAndKeepTTLAsync(K key, V value);
//
//    /**
//     * 注册对象事件监听器。
//     *
//     * @see org.redisson.api.listener.TrackingListener
//     * @see org.redisson.api.ExpiredObjectListener
//     * @see org.redisson.api.DeletedObjectListener
//     * @see org.redisson.api.listener.SetObjectListener
//     *
//     * @param listener 对象事件监听器
//     * @return 监听器 ID
//     */
//    RFuture<Integer> addListenerAsync(K key, ObjectListener listener);

    /**
     * Remaining time to live of map entry associated with a <code>key</code>.
     *
     * @param key 映射键
     * @return time in milliseconds
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    RFuture<Long> remainTimeToLiveAsync(K key);

    /**
     * 若包含指定 {@code key} 的映射条目则返回 {@code true}，否则 {@code false}。
     *
     * @param key 映射键
     * @return <code>true</code> if this map contains map entry
     *          mapped by specified <code>key</code>, otherwise <code>false</code>
     */
    RFuture<Boolean> containsKeyAsync(Object key);

    /**
     * 一次性读取全部键。
     *
     * @return 键集合
     */
    RFuture<Set<K>> readAllKeySetAsync();

    /**
     * Returns entries amount in store
     *
     * @return entries amount
     */
    RFuture<Integer> sizeAsync();

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     * @see org.redisson.api.listener.SetObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    RFuture<Integer> addListenerAsync(ObjectListener listener);

}
