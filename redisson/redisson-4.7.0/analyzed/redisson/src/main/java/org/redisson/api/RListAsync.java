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
import java.util.RandomAccess;

/**
 * {@link RList} 异步 API。
 * <p>各方法返回 {@link RFuture}，基于 Redis {@code LIST} 命令。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RListAsync<V> extends RCollectionAsync<V>, RSortableAsync<List<V>>, RandomAccess {

    /**
     * 按指定下标批量加载元素。
     * 
     * @param indexes 元素下标
     * @return 元素列表
     */
    RFuture<List<V>> getAsync(int... indexes);
    
    /**
     * 在 {@code elementToFind} 之后异步插入 {@code element}。
     * 
     * @param elementToFind 定位元素
     * @param element 待插入元素
     * @return 插入后列表长度
     */
    RFuture<Integer> addAfterAsync(V elementToFind, V element);
    
    /**
     * 在 {@code elementToFind} 之前异步插入 {@code element}。
     * 
     * @param elementToFind 定位元素
     * @param element 待插入元素
     * @return 插入后列表长度
     */
    RFuture<Integer> addBeforeAsync(V elementToFind, V element);
    
    /**
     * 在 {@code index} 处异步插入元素，后续元素后移。
     * 
     * @param index 插入位置
     * @param element 待插入元素
     * @return 列表发生变化时为 {@code true}
     */
    RFuture<Boolean> addAsync(int index, V element);
    
    /**
     * 在 {@code index} 处异步批量插入元素，后续元素后移。
     * 
     * @param index 插入位置
     * @param elements 待插入元素集合
     * @return 列表发生变化时为 {@code true}
     */
    RFuture<Boolean> addAllAsync(int index, Collection<? extends V> elements);

    /**
     * 异步返回 {@code element} 最后一次出现的下标；未找到时返回 -1。
     * 
     * @param element 待查元素
     * @return 下标；未找到时为 -1
     */
    RFuture<Integer> lastIndexOfAsync(Object element);

    /**
     * 异步返回 {@code element} 最后一次出现的下标；未找到时返回 -1。
     * 
     * @param element 待查元素
     * @return 下标；未找到时为 -1
     */
    RFuture<Integer> indexOfAsync(Object element);

    /**
     * 在 {@code index} 处设置元素（快速版，不返回旧值）。
     * 比 {@link #setAsync(int, Object)} 更快，但不返回被替换元素。
     * 
     * @param index 下标
     * @param element 新元素
     * @return void
     */
    RFuture<Void> fastSetAsync(int index, V element);

    /**
     * 在 {@code index} 处异步设置元素并返回旧值。
     * 
     * @param index 下标
     * @param element 新元素
     * @return 被替换的旧元素；未设置时为 null
     */
    RFuture<V> setAsync(int index, V element);

    /**
     * 异步获取 {@code index} 处元素。
     * 
     * @param index 下标
     * @return 元素
     */
    RFuture<V> getAsync(int index);

    /**
     * 一次性读取全部元素。
     *
     * @return 元素列表
     */
    RFuture<List<V>> readAllAsync();

    /**
     * 裁剪列表，仅保留 {@code fromIndex} 到 {@code toIndex}（均含）区间内的元素。
     *
     * @param fromIndex 起始下标
     * @param toIndex 结束下标
     * @return void
     */
    RFuture<Void> trimAsync(int fromIndex, int toIndex);

    /**
     * 按指定下标异步快速移除元素（不返回值）。
     * 比 {@link #removeAsync(Object, int)} 更快，但不返回被移除元素。
     * 
     * @param index 元素下标
     * @return void
     */
    RFuture<Void> fastRemoveAsync(int index);

    /**
     * 异步移除 {@code index} 处元素并返回被移除值。
     * 
     * @param index 下标
     * @return 被移除元素；未设置时为 null
     */
    RFuture<V> removeAsync(int index);
    
    /**
     * 移除至多 {@code count} 个与 {@code element} 相等的元素。
     * 
     * @param element 待移除元素
     * @param count 最多移除个数
     * @return 至少移除一个时为 {@code true}；未找到时为 {@code false}
     */
    RFuture<Boolean> removeAsync(Object element, int count);
    
    /**
     * 返回从 0 到 {@code toIndex} 的元素区间（下标从 0 起）。
     * {@code -1} 表示最后一个元素，{@code -2} 表示倒数第二个，依此类推。
     * 
     * @param toIndex 结束下标
     * @return 元素列表
     */
    RFuture<List<V>> rangeAsync(int toIndex);
    
    /**
     * 返回 {@code fromIndex} 到 {@code toIndex}（均含）的元素区间。
     * 下标从 0 起；{@code -1} 表示最后一个元素，{@code -2} 表示倒数第二个。
     * 
     * @param fromIndex 起始下标
     * @param toIndex 结束下标
     * @return 元素列表
     */
    RFuture<List<V>> rangeAsync(int fromIndex, int toIndex);

    /**
     * 异步注册 List 对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     * @see org.redisson.api.listener.ListAddListener
     * @see org.redisson.api.listener.ListInsertListener
     * @see org.redisson.api.listener.ListSetListener
     * @see org.redisson.api.listener.ListRemoveListener
     * @see org.redisson.api.listener.ListTrimListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    RFuture<Integer> addListenerAsync(ObjectListener listener);

}
