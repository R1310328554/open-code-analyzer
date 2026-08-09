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

import java.util.List;

/**
 * Redis JSON 数据类型持有者 {@link RJsonBucket}；数据以 JSON 对象存储。
 * <p>支持 JSONPath 读写、数组/字符串操作与 CAS 更新。
 *
 * @author Nikita Koksharov
 * @param <V> 对象类型
 */
public interface RJsonBucket<V> extends RBucket<V>, RJsonBucketAsync<V> {

    /**
     * 按 JSONPath 获取 JSON 对象。
     *
     * @param codec 对象编解码器
     * @param paths JSON 路径
     * @return 对象
     *
     * @param <T> 对象类型
     */
    <T> T get(JsonCodec codec, String... paths);

    /**
     * 仅当 JSONPath 处原值为空时写入 JSON 对象。
     *
     * @param path JSON 路径
     * @param value object
     * @return {@code true} if successful, or {@code false} if
     *         value was already set
     */
    boolean setIfAbsent(String path, Object value);

    /**
     * 请改用 {@link #setIfAbsent(String, Object)}
     *
     * @param path JSON 路径
     * @param value object
     * @return {@code true} if successful, or {@code false} if
     *         value was already set
     */
    @Deprecated
    boolean trySet(String path, Object value);

    /**
     * 仅当 JSONPath 处原值非空时写入 JSON 对象。
     *
     * @param path JSON 路径
     * @param value object
     * @return {@code true} if successful, or {@code false} if
     *         element wasn't set
     */
    boolean setIfExists(String path, Object value);

    /**
     * 仅当 JSONPath 处当前值序列化状态等于期望值时，原子写入新值。
     *
     * @param path JSON 路径
     * @param expect 期望值
     * @param update 新值
     * @return {@code true} if successful; or {@code false} if the actual value
     *         was not equal to the expected value.
     */
    boolean compareAndSet(String path, Object expect, Object update);

    /**
     * Retrieves current value of element specified by JSONPath
     * and replaces it with <code>newValue</code>.
     *
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @param newValue value to set
     * @return previous value
     */
    <T> T getAndSet(JsonCodec codec, String path, Object newValue);

    /**
     * Stores object into element by specified JSONPath.
     *
     * @param path JSON 路径
     * @param value value to set
     */
    void set(String path, Object value);

    /**
     * Stores object into element by specified JSONPath using FPHA argument
     * to enforce floating-point array precision.
     * 需要 <b>Redis 8.8.0 及以上</b>。
     *
     * @param path JSON 路径
     * @param value value to set
     * @param fphaType floating-point precision type
     */
    void set(String path, Object value, FPHAType fphaType);

    /**
     * 仅当 JSONPath 处原值为空时写入 JSON 对象。,
     * using FPHA argument to enforce floating-point array precision.
     * 需要 <b>Redis 8.8.0 及以上</b>。
     *
     * @param path JSON 路径
     * @param value object
     * @param fphaType floating-point precision type
     * @return {@code true} if successful, or {@code false} if
     *         value was already set
     */
    boolean setIfAbsent(String path, Object value, FPHAType fphaType);

    /**
     * 仅当 JSONPath 处原值非空时写入 JSON 对象。,
     * using FPHA argument to enforce floating-point array precision.
     * 需要 <b>Redis 8.8.0 及以上</b>。
     *
     * @param path JSON 路径
     * @param value object
     * @param fphaType floating-point precision type
     * @return {@code true} if successful, or {@code false} if
     *         element wasn't set
     */
    boolean setIfExists(String path, Object value, FPHAType fphaType);

    /**
     * Returns size of string data by JSONPath
     *
     * @param path JSON 路径
     * @return size of string
     */
    Long stringSize(String path);

    /**
     * Returns list of string data size by JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @return list of string data sizes
     */
    List<Long> stringSizeMulti(String path);

    /**
     * Appends string data to element specified by JSONPath.
     * Returns new size of string data.
     *
     * @param path JSON 路径
     * @param value data
     * @return size of string data
     */
    long stringAppend(String path, Object value);

    /**
     * Appends string data to elements specified by JSONPath.
     * Returns new size of string data.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @param value data
     * @return list of string data sizes
     */
    List<Long> stringAppendMulti(String path, Object value);

    /**
     * Appends values to array specified by JSONPath.
     * Returns new size of array.
     *
     * @param path JSON 路径
     * @param values values to append
     * @return size of array
     */
    long arrayAppend(String path, Object... values);

    /**
     * Appends values to arrays specified by JSONPath.
     * Returns new size of arrays.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @param values values to append
     * @return list of arrays size
     */
    List<Long> arrayAppendMulti(String path, Object... values);

    /**
     * Returns index of object in array specified by JSONPath.
     * 返回 {@code -1} 表示未找到。
     *
     * @param path JSON 路径
     * @param value value to search
     * @return index in array
     */
    long arrayIndex(String path, Object value);

    /**
     * Returns index of object in arrays specified by JSONPath.
     * 返回 {@code -1} 表示未找到。
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @param value value to search
     * @return list of index in arrays
     */
    List<Long> arrayIndexMulti(String path, Object value);

    /**
     * Returns index of object in array specified by JSONPath
     * in range between <code>start</code> (inclusive) and <code>end</code> (exclusive) indexes.
     * 返回 {@code -1} 表示未找到。
     *
     * @param path JSON 路径
     * @param value value to search
     * @param start start index, inclusive
     * @param end end index, exclusive
     * @return index in array
     */
    long arrayIndex(String path, Object value, long start, long end);

    /**
     * Returns index of object in arrays specified by JSONPath
     * in range between <code>start</code> (inclusive) and <code>end</code> (exclusive) indexes.
     * 返回 {@code -1} 表示未找到。
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @param value value to search
     * @param start start index, inclusive
     * @param end end index, exclusive
     * @return list of index in arrays
     */
    List<Long> arrayIndexMulti(String path, Object value, long start, long end);

    /**
     * Inserts values into array specified by JSONPath.
     * Values are inserted at defined <code>index</code>.
     *
     * @param path JSON 路径
     * @param index array index at which values are inserted
     * @param values values to insert
     * @return size of array
     */
    long arrayInsert(String path, long index, Object... values);

    /**
     * Inserts values into arrays specified by JSONPath.
     * Values are inserted at defined <code>index</code>.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @param index array index at which values are inserted
     * @param values values to insert
     * @return list of arrays size
     */
    List<Long> arrayInsertMulti(String path, long index, Object... values);

    /**
     * Returns size of array specified by JSONPath.
     *
     * @param path JSON 路径
     * @return size of array
     */
    long arraySize(String path);

    /**
     * Returns size of arrays specified by JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @return list of arrays size
     */
    List<Long> arraySizeMulti(String path);

    /**
     * Polls last element of array specified by JSONPath.
     *
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @return last element
     *
     * @param <T> 对象类型
     */
    <T> T arrayPollLast(JsonCodec codec, String path);

    /**
     * Polls last element of arrays specified by JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @return list of last elements
     *
     * @param <T> 对象类型
     */
    <T> List<T> arrayPollLastMulti(JsonCodec codec, String path);

    /**
     * Polls first element of array specified by JSONPath.
     *
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @return first element
     *
     * @param <T> 对象类型
     */
    <T> T arrayPollFirst(JsonCodec codec, String path);

    /**
     * Polls first element of arrays specified by JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @return list of first elements
     *
     * @param <T> 对象类型
     */
    <T> List<T> arrayPollFirstMulti(JsonCodec codec, String path);

    /**
     * Pops element located at index of array specified by JSONPath.
     *
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @param index array index
     * @return element
     *
     * @param <T> 对象类型
     */
    <T> T arrayPop(JsonCodec codec, String path, long index);

    /**
     * Pops elements located at index of arrays specified by JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param codec 对象编解码器
     * @param path JSON 路径
     * @param index array index
     * @return list of elements
     *
     * @param <T> 对象类型
     */
    <T> List<T> arrayPopMulti(JsonCodec codec, String path, long index);

    /**
     * Trims array specified by JSONPath in range
     * between <code>start</code> (inclusive) and <code>end</code> (inclusive) indexes.
     *
     * @param path JSON 路径
     * @param start start index, inclusive
     * @param end end index, inclusive
     * @return length of array
     */
    long arrayTrim(String path, long start, long end);

    /**
     * Trims arrays specified by JSONPath in range
     * between <code>start</code> (inclusive) and <code>end</code> (inclusive) indexes.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @param start start index, inclusive
     * @param end end index, inclusive
     * @return length of array
     */
    List<Long> arrayTrimMulti(String path, long start, long end);

    /**
     * 清空 JSON 容器。
     *
     * @return number of cleared containers
     */
    long clear();

    /**
     * Clears json container specified by JSONPath.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @return number of cleared containers
     */
    long clear(String path);

    /**
     * Increments the current value specified by JSONPath by <code>delta</code>.
     *
     * @param path JSON 路径
     * @param delta increment value
     * @return the updated value
     */
    <T extends Number> T incrementAndGet(String path, T delta);

    /**
     * Increments the current values specified by JSONPath by <code>delta</code>.
     * 仅兼容以 {@code $} 开头的增强 JSONPath 语法。
     *
     * @param path JSON 路径
     * @param delta increment value
     * @return list of updated value
     */
    <T extends Number> List<T> incrementAndGetMulti(String path, T delta);

    /**
     * Merges object into element by the specified JSONPath.
     *
     * @param path JSON 路径
     * @param value value to merge
     */
    void merge(String path, Object value);

    /**
     * Returns keys amount in JSON container
     *
     * @return 键集合 amount
     */
    long countKeys();

    /**
     * Returns keys amount in JSON container specified by JSONPath
     *
     * @param path JSON 路径
     * @return 键集合 amount
     */
    long countKeys(String path);

    /**
     * Returns list of keys amount in JSON containers specified by JSONPath
     *
     * @param path JSON 路径
     * @return list of keys amount
     */
    List<Long> countKeysMulti(String path);

    /**
     * Returns list of keys in JSON container
     *
     * @return list of keys
     */
    List<String> getKeys();

    /**
     * Returns list of keys in JSON container specified by JSONPath
     *
     * @param path JSON 路径
     * @return list of keys
     */
    List<String> getKeys(String path);

    /**
     * Returns list of keys in JSON containers specified by JSONPath
     *
     * @param path JSON 路径
     * @return list of keys
     */
    List<List<String>> getKeysMulti(String path);

    /**
     * Toggle boolean value specified by JSONPath
     *
     * @param path JSON 路径
     * @return new boolean value
     */
    boolean toggle(String path);

    /**
     * Toggle boolean values specified by JSONPath
     *
     * @param path JSON 路径
     * @return list of boolean values
     */
    List<Boolean> toggleMulti(String path);

    /**
     * Returns type of element
     *
     * @return type of element
     */
    JsonType getType();

    /**
     * Returns type of element specified by JSONPath
     *
     * @param path JSON 路径
     * @return type of element
     */
    JsonType getType(String path);

    /**
     * Deletes JSON elements specified by JSONPath
     *
     * @param path JSON 路径
     * @return number of deleted elements
     */
    long delete(String path);

}
