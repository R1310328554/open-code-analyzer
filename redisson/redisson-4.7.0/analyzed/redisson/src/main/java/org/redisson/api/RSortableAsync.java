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
import java.util.List;

/**
 * Redis {@code SORT} 命令异步 API；各方法返回 {@link RFuture}。
 *
 * @author Nikita Koksharov
 * @param <V> 对象类型
 */
public interface RSortableAsync<V> {

    /**
     * 按排序顺序读取数据
     * 
     * @param order 排序方向
     * @return 排序后的集合
     */
    RFuture<V> readSortAsync(SortOrder order);

    /**
     * 按排序顺序读取数据
     * 
     * @param order 排序方向
     * @param offset 结果偏移量
     * @param count 返回数量上限
     * @return 排序后的集合
     */
    RFuture<V> readSortAsync(SortOrder order, int offset, int count);

    /**
     * 按排序顺序读取数据
     * 
     * @param byPattern 用于生成排序键的模式
     * @param order 排序方向
     * @return 排序后的集合
     */
    RFuture<V> readSortAsync(String byPattern, SortOrder order);

    /**
     * 按排序顺序读取数据
     * 
     * @param byPattern 用于生成排序键的模式
     * @param order 排序方向
     * @param offset 结果偏移量
     * @param count 返回数量上限
     * @return 排序后的集合
     */
    RFuture<V> readSortAsync(String byPattern, SortOrder order, int offset, int count);

    /**
     * 按排序顺序读取数据
     * 
     * @param <T> 对象类型 
     * @param byPattern 用于生成排序键的模式
     * @param getPatterns 按排序键加载值的 GET 模式列表
     * @param order 排序方向
     * @return 排序后的集合
     */
    <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order);

    /**
     * 按排序顺序读取数据
     * 
     * @param <T> 对象类型
     * @param byPattern 用于生成排序键的模式
     * @param getPatterns 按排序键加载值的 GET 模式列表
     * @param order 排序方向
     * @param offset 结果偏移量
     * @param count 返回数量上限
     * @return 排序后的集合
     */
    <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count);

    /**
     * 按排序顺序读取数据 lexicographically
     *
     * @param order 排序方向
     * @return 排序后的集合 lexicographically
     */
    RFuture<V> readSortAlphaAsync(SortOrder order);

    /**
     * 按排序顺序读取数据 lexicographically
     *
     * @param order 排序方向
     * @param offset 结果偏移量
     * @param count 返回数量上限
     * @return 排序后的集合 lexicographically
     */
    RFuture<V> readSortAlphaAsync(SortOrder order, int offset, int count);

    /**
     * 按排序顺序读取数据 lexicographically
     *
     * @param byPattern 用于生成排序键的模式
     * @param order 排序方向
     * @return 排序后的集合 lexicographically
     */
    RFuture<V> readSortAlphaAsync(String byPattern, SortOrder order);

    /**
     * 按排序顺序读取数据 lexicographically
     *
     * @param byPattern 用于生成排序键的模式
     * @param order 排序方向
     * @param offset 结果偏移量
     * @param count 返回数量上限
     * @return 排序后的集合 lexicographically
     */
    RFuture<V> readSortAlphaAsync(String byPattern, SortOrder order, int offset, int count);

    /**
     * 按排序顺序读取数据 lexicographically
     *
     * @param <T> 对象类型
     * @param byPattern 用于生成排序键的模式
     * @param getPatterns 按排序键加载值的 GET 模式列表
     * @param order 排序方向
     * @return 排序后的集合 lexicographically
     */
    <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order);

    /**
     * 按排序顺序读取数据 lexicographically
     *
     * @param <T> 对象类型
     * @param byPattern 用于生成排序键的模式
     * @param getPatterns 按排序键加载值的 GET 模式列表
     * @param order 排序方向
     * @param offset 结果偏移量
     * @param count 返回数量上限
     * @return 排序后的集合 lexicographically
     */
    <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order, int offset, int count);

    /**
     * 排序后将结果写入 {@code destName} 列表
     * 
     * @param destName 目标列表名称 
     * @param order 排序方向
     * @return 排序结果长度
     */
    RFuture<Integer> sortToAsync(String destName, SortOrder order);

    /**
     * 排序后将结果写入 {@code destName} 列表
     * 
     * @param destName 目标列表名称
     * @param order 排序方向
     * @param offset 结果偏移量
     * @param count 返回数量上限
     * @return 排序结果长度
     */
    RFuture<Integer> sortToAsync(String destName, SortOrder order, int offset, int count);

    /**
     * 排序后将结果写入 {@code destName} 列表
     * 
     * @param destName 目标列表名称
     * @param byPattern 用于生成排序键的模式
     * @param order 排序方向
     * @return 排序结果长度
     */
    RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order);

    /**
     * 排序后将结果写入 {@code destName} 列表
     * 
     * @param destName 目标列表名称
     * @param byPattern 用于生成排序键的模式
     * @param order 排序方向
     * @param offset 结果偏移量
     * @param count 返回数量上限
     * @return 排序结果长度
     */
    RFuture<Integer> sortToAsync(String destName, String byPattern, SortOrder order, int offset, int count);

    /**
     * 排序后将结果写入 {@code destName} 列表
     * 
     * @param destName 目标列表名称
     * @param byPattern 用于生成排序键的模式
     * @param getPatterns 按排序键加载值的 GET 模式列表
     * @param order 排序方向
     * @return 排序结果长度
     */
    RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order);

    /**
     * 排序后将结果写入 {@code destName} 列表
     * 
     * @param destName 目标列表名称
     * @param byPattern 用于生成排序键的模式
     * @param getPatterns 按排序键加载值的 GET 模式列表
     * @param order 排序方向
     * @param offset 结果偏移量
     * @param count 返回数量上限
     * @return 排序结果长度
     */
    RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order, int offset, int count);

}
