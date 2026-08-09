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
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link RJsonStore} Reactor 响应式 API。
 * <p>实现仅 Redisson PRO 提供。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RJsonStoreReactive<K, V> extends RExpirableReactive {

    /**
     * Gets value by specified key and JSONPath
     *
     * @param key   entry key
     * @param codec entry value codec
     * @param paths JSON 路径
     * @param <T>   the type of object
     * @return 条目值
     */
    <T> Mono<T> get(K key, JsonCodec codec, String... paths);

    /**
     * Sets value by specified key and JSONPath only if previous value is empty.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value entry value
     * @return {@code true} if successful, or {@code false} if
     *         value was already set
     */
    Mono<Boolean> setIfAbsent(K key, String path, Object value);

    /**
     * Sets value by specified key and JSONPath only if previous value is non-empty.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value object
     * @return {@code true} if successful, or {@code false} if
     *         element wasn't set
     */
    Mono<Boolean> setIfExists(K key, String path, Object value);

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
    Mono<Boolean> compareAndSet(K key, String path, Object expect, Object update);

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
    <T> Mono<T> getAndSet(K key, JsonCodec codec, String path, Object newValue);

    /**
     * Stores value by specified key and JSONPath.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value value to set
     */
    Mono<Void> set(K key, String path, Object value);

    /**
     * Returns size of string data by specified key and JSONPath
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return size of string
     */
    Mono<Long> stringSize(K key, String path);

    /**
     * Returns list of string data size by specified key and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return list of string data sizes
     */
    Mono<List<Long>> stringSizeMulti(K key, String path);

    /**
     * Appends string data to element specified by specified key and JSONPath.
     * Returns new size of string data.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value data
     * @return size of string data
     */
    Mono<Long> stringAppend(K key, String path, Object value);

    /**
     * Appends string data to elements specified by specified key and JSONPath.
     * Returns new size of string data.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value data
     * @return list of string data sizes
     */
    Mono<List<Long>> stringAppendMulti(K key, String path, Object value);

    /**
     * Appends values to array by specified key and JSONPath.
     * Returns new size of array.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param values values to append
     * @return size of array
     */
    Mono<Long> arrayAppend(K key, String path, Object... values);

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
    Mono<List<Long>> arrayAppendMulti(K key, String path, Object... values);

    /**
     * Returns index of object in array by specified key and JSONPath.
     * 返回 {@code -1} 表示未找到。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value value to search
     * @return index in array
     */
    Mono<Long> arrayIndex(K key, String path, Object value);

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
    Mono<List<Long>> arrayIndexMulti(K key, String path, Object value);

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
    Mono<Long> arrayIndex(K key, String path, Object value, Long start, Long end);

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
    Mono<List<Long>> arrayIndexMulti(K key, String path, Object value, Long start, Long end);

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
    Mono<Long> arrayInsert(K key, String path, Long index, Object... values);

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
    Mono<List<Long>> arrayInsertMulti(K key, String path, Long index, Object... values);

    /**
     * Returns size of array by specified key and JSONPath.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return size of array
     */
    Mono<Long> arraySize(K key, String path);

    /**
     * Returns size of arrays by specified key and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return list of arrays size
     */
    Mono<List<Long>> arraySizeMulti(K key, String path);

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
    <T> Mono<T> arrayPollLast(K key, JsonCodec codec, String path);

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
    <T> Mono<List<T>> arrayPollLastMulti(K key, JsonCodec codec, String path);

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
    <T> Mono<T> arrayPollFirst(K key, JsonCodec codec, String path);

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
    <T> Mono<List<T>> arrayPollFirstMulti(K key, JsonCodec codec, String path);

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
    <T> Mono<T> arrayPop(K key, JsonCodec codec, String path, Long index);

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
    <T> Mono<List<T>> arrayPopMulti(K key, JsonCodec codec, String path, Long index);

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
    Mono<Long> arrayTrim(K key, String path, Long start, Long end);

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
    Mono<List<Long>> arrayTrimMulti(K key, String path, Long start, Long end);

    /**
     * Clears value by specified key
     *
     * @param key 条目键
     * @return {@code true} if successful, or {@code false} if
     *         entry doesn't exist
     */
    Mono<Boolean> clear(K key);

    /**
     * Clears json containers by specified keys.
     *
     * @param keys entry keys
     * @return number of cleared containers
     */
    Mono<Long> clear(Set<K> keys);

    /**
     * Clears json container by specified keys and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @param keys entry keys
     * @return number of cleared containers
     */
    Mono<Long> clear(String path, Set<K> keys);

    /**
     * Increments the current value specified by key and JSONPath.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param delta increment value
     * @return the updated value
     */
    <T extends Number> Mono<T> incrementAndGet(K key, String path, T delta);

    /**
     * Increments the current values specified by key and JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param delta increment value
     * @return list of updated value
     */
    <T extends Number> Mono<List<T>> incrementAndGetMulti(K key, String path, T delta);

    /**
     * Merges value into element by the specified key and JSONPath.
     *
     * @param key 条目键
     * @param path JSON 路径
     * @param value value to merge
     */
    Mono<Void> merge(K key, String path, Object value);

    /**
     * Returns keys amount in JSON container by specified key
     *
     * @param key 条目键
     * @return 键集合 amount
     */
    Mono<Long> countKeys(K key);

    /**
     * Returns keys amount in JSON container specified by key and JSONPath
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return 键集合 amount
     */
    Mono<Long> countKeys(K key, String path);

    /**
     * Returns list of keys amount in JSON containers specified by key and JSONPath
     *
     * @param key 条目键
     * @param path JSON 路径
     * @return list of keys amount
     */
    Mono<List<Long>> countKeysMulti(K key, String path);

    /**
     * Returns list of keys in JSON container by specified key
     *
     * @return list of keys
     */
    Mono<List<String>> getKeys(K key);

    /**
     * Returns list of keys in JSON container by specified key and JSONPath
     *
     * @param path JSON 路径
     * @return list of keys
     */
    Mono<List<String>> getKeys(K key, String path);

    /**
     * Returns list of keys in JSON containers by specified key and JSONPath
     *
     * @param path JSON 路径
     * @return list of keys
     */
    List<Mono<List<String>>> getKeysMulti(K key, String path);

    /**
     * Toggle Mono<Boolean> value by specified key and JSONPath
     *
     * @param path JSON 路径
     * @return new Mono<Boolean> value
     */
    Mono<Boolean> toggle(K key, String path);

    /**
     * Toggle Mono<Boolean> values by specified key and JSONPath
     *
     * @param path JSON 路径
     * @return list of Mono<Boolean> values
     */
    List<Mono<Boolean>> toggleMulti(K key, String path);

    /**
     * Returns type of value
     *
     * @return type of element
     */
    Mono<JsonType> getType(K key);

    /**
     * Returns type of element specified by key and JSONPath
     *
     * @param path JSON 路径
     * @return type of element
     */
    Mono<JsonType> getType(K key, String path);

    /**
     * Deletes entry by specified key
     *
     * @param key 条目键
     * @return {@code true} if successful, or {@code false} if
     *         entry doesn't exist
     */
    Mono<Boolean> delete(K key);

    /**
     * Deletes entries by specified keys
     *
     * @param keys entry keys
     * @return number of deleted elements
     */
    Mono<Long> delete(Set<K> keys);

    /**
     * Deletes JSON elements specified by keys and JSONPath
     *
     * @param path JSON 路径
     * @param keys entry keys
     * @return number of deleted elements
     */
    Mono<Long> delete(String path, Set<K> keys);

    /**
     * Returns size of entry in bytes specified by key.
     *
     * @param key 条目键
     * @return entry size
     */
    Mono<Long> sizeInMemory(K key);

    /**
     * Retrieves value by specified key.
     *
     * @param key 条目键
     * @return element
     */
    Mono<V> get(K key);

    /**
     * Retrieves values by specified keys.
     *
     * @param keys entry keys
     * @return map with entries where value mapped by key
     */
    Mono<Map<K, V>> get(Set<K> keys);

    /**
     * Retrieves values by specified keys and JSONPath.
     *
     * @param path JSON 路径
     * @param keys entry keys
     * @return map with entries where value mapped by key
     */
    Mono<Map<K, V>> get(String path, Set<K> keys);

    /**
     * Retrieves entry value by specified key and removes it.
     *
     * @param key 条目键
     * @return element
     */
    Mono<V> getAndDelete(K key);

    /**
     * Sets value only if entry doesn't exist.
     *
     * @param key 条目键
     * @param value value to set
     * @return {@code true} if successful, or {@code false} if
     *         element was already set
     */
    Mono<Boolean> setIfAbsent(K key, V value);

    /**
     * Sets value with defined duration only if entry doesn't exist.
     *
     * @param key 条目键
     * @param value value to set
     * @param duration expiration duration
     * @return {@code true} if successful, or {@code false} if
     *         element was already set
     */
    Mono<Boolean> setIfAbsent(K key, V value, Duration duration);

    /**
     * Sets value only if entry already exists.
     *
     * @param key 条目键
     * @param value value to set
     * @return {@code true} if successful, or {@code false} if
     *         element wasn't set
     */
    Mono<Boolean> setIfExists(K key, V value);

    /**
     * Sets <code>value</code> with expiration <code>duration</code> only if entry already exists.
     *
     * @param key 条目键
     * @param value value to set
     * @param duration expiration duration
     * @return {@code true} if successful, or {@code false} if
     *         element wasn't set
     */
    Mono<Boolean> setIfExists(K key, V value, Duration duration);

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
    Mono<Boolean> compareAndSet(K key, V expect, V update);

    /**
     * Retrieves current value by specified key and replaces it with new value.
     *
     * @param key 条目键
     * @param newValue value to set
     * @return previous value
     */
    Mono<V> getAndSet(K key, V newValue);

    /**
     * Retrieves current value by specified key and replaces it
     * with value and defines expiration <code>duration</code>.
     *
     * @param key 条目键
     * @param value value to set
     * @param duration expiration duration
     * @return previous value
     */
    Mono<V> getAndSet(K key, V value, Duration duration);

    /**
     * Retrieves current value by specified key and sets an expiration duration for it.
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param key 条目键
     * @param duration of object time to live interval
     * @return value
     */
    Mono<V> getAndExpire(K key, Duration duration);

    /**
     * Retrieves current value by specified key and sets an expiration date for it.
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param key 条目键
     * @param time of exact object expiration moment
     * @return value
     */
    Mono<V> getAndExpire(K key, Instant time);

    /**
     * Retrieves current value by specified key and clears expiration date set before.
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param key 条目键
     * @return value
     */
    Mono<V> getAndClearExpire(K key);

    /**
     * Stores value by specified key.
     *
     * @param key 条目键
     * @param value value to set
     */
    Mono<Void> set(K key, V value);

    /**
     * Stores values by specified keys.
     *
     * @param entries entries to store
     */
    Mono<Void> set(Map<K, V> entries);

    /**
     * Stores values by specified keys and JSONPath.
     *
     * @param path JSONPath
     * @param entries entries to store
     */
    Mono<Void> set(String path, Map<K, V> entries);

    /**
     * Stores value by specified key with defined expiration duration.
     *
     * @param key 条目键
     * @param value value to set
     * @param duration expiration duration
     */
    Mono<Void> set(K key, V value, Duration duration);

    /**
     * Stores values by specified keys with defined expiration duration.
     *
     * @param entries entries to store
     * @param duration expiration duration
     */
    Mono<Void> set(Map<K, V> entries, Duration duration);

    /**
     * Sets value by specified key and keep existing TTL.
     * <p>
     * Requires <b>Redis 6.0.0 and higher.</b>
     *
     * @param value value to set
     */
    Mono<Void> setAndKeepTTL(K key, V value);

    /**
     * 注册对象事件监听器。
     *
     * @see ExpiredObjectListener
     * @see DeletedObjectListener
     * @see org.redisson.api.listener.SetObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    Mono<Integer> addListener(ObjectListener listener);

    /**
     * Remaining time to live of map entry associated with a <code>key</code>.
     *
     * @param key 映射键
     * @return time in milliseconds
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    Mono<Long> remainTimeToLive(K key);

    /**
     * 若包含指定 {@code key} 的映射条目则返回 {@code true}，否则 {@code false}。
     *
     * @param key 映射键
     * @return <code>true</code> if this map contains map entry
     *          mapped by specified <code>key</code>, otherwise <code>false</code>
     */
    Mono<Boolean> containsKey(Object key);

    /**
     * 一次性读取全部键。
     *
     * @return 键集合
     */
    Mono<Set<K>> readAllKeySet();

    /**
     * Returns entries amount in store
     *
     * @return entries amount
     */
    Mono<Integer> size();

    /**
     * 返回与键关联的 {@link RCountDownLatch}。
     *
     * @param key 映射键
     * @return countdownlatch
     */
    RCountDownLatchReactive getCountDownLatch(K key);

    /**
     * 返回与键关联的 {@link RPermitExpirableSemaphore}。
     *
     * @param key 映射键
     * @return permitExpirableSemaphore
     */
    RPermitExpirableSemaphoreReactive getPermitExpirableSemaphore(K key);

    /**
     * 返回与键关联的 {@link RSemaphore}。
     *
     * @param key 映射键
     * @return semaphore
     */
    RSemaphoreReactive getSemaphore(K key);

    /**
     * 返回与键关联的 {@link RLock}。
     *
     * @param key 映射键
     * @return fairlock
     */
    RLockReactive getFairLock(K key);

    /**
     * 返回与键关联的 {@link RReadWriteLock}。
     *
     * @param key 映射键
     * @return readWriteLock
     */
    RReadWriteLockReactive getReadWriteLock(K key);

    /**
     * 返回与键关联的 {@link RLock}。
     *
     * @param key 映射键
     * @return lock
     */
    RLockReactive getLock(K key);

}
