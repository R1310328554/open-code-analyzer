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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link RList} 的 Reactor API。
 * <p>各方法返回 {@link Mono} 或 {@link Flux}，基于 Redis {@code LIST} 命令。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
// TODO 后续支持 subList
public interface RListReactive<V> extends RCollectionReactive<V>, RSortableReactive<List<V>> {

    /**
     * 按指定下标批量加载元素。
     * 
     * @param indexes 元素下标
     * @return 元素列表
     */
    Mono<List<V>> get(int... indexes);
    
    /**
     * 在 {@code elementToFind} 之后插入 {@code element}。
     * 
     * @param elementToFind 定位元素
     * @param element 待插入元素
     * @return 插入后列表长度
     */
    Mono<Integer> addAfter(V elementToFind, V element);
    
    /**
     * 在 {@code elementToFind} 之前插入 {@code element}。
     * 
     * @param elementToFind 定位元素
     * @param element 待插入元素
     * @return 插入后列表长度
     */
    Mono<Integer> addBefore(V elementToFind, V element);
    
    Flux<V> descendingIterator();

    Flux<V> descendingIterator(int startIndex);

    Flux<V> iterator(int startIndex);

    /**
     * 返回 {@code element} 最后一次出现的下标；未找到时返回 -1。
     * 
     * @param element 待查元素
     * @return 下标；未找到时为 -1
     */
    Mono<Integer> lastIndexOf(Object element);

    /**
     * 返回 {@code element} 最后一次出现的下标；未找到时返回 -1。
     * 
     * @param element 待查元素
     * @return 下标；未找到时为 -1
     */
    Mono<Integer> indexOf(Object element);

    /**
     * 在 {@code index} 处插入元素，后续元素后移。
     * 
     * @param index 插入位置
     * @param element 待插入元素
     * @return 列表发生变化时为 {@code true}
     */
    Mono<Void> add(int index, V element);

    /**
     * 在 {@code index} 处批量插入元素，后续元素后移。
     * 
     * @param index 插入位置
     * @param elements 待插入元素集合
     * @return 列表发生变化时为 {@code true}
     */
    Mono<Boolean> addAll(int index, Collection<? extends V> elements);

    /**
     * 在 {@code index} 处设置元素（快速版，不返回旧值）。
     * 比 {@link #set(int, Object)} 更快，但不返回被替换元素。
     * 
     * @param index 下标
     * @param element 新元素
     * @return void
     */
    Mono<Void> fastSet(int index, V element);

    /**
     * 在 {@code index} 处设置元素并返回旧值。
     * 
     * @param index 下标
     * @param element 新元素
     * @return 被替换的旧元素；未设置时为 null
     */
    Mono<V> set(int index, V element);

    /**
     * 获取 {@code index} 处元素。
     * 
     * @param index 下标
     * @return 元素
     */
    Mono<V> get(int index);

    /**
     * 移除 {@code index} 处元素并返回被移除值。
     * 
     * @param index 下标
     * @return 被移除元素；未设置时为 null
     */
    Mono<V> remove(int index);
    
    /**
     * 一次性读取全部元素。
     *
     * @return 元素列表
     */
    Mono<List<V>> readAll();

    /**
     * 裁剪列表，仅保留 {@code fromIndex} 到 {@code toIndex}（均含）区间内的元素。
     *
     * @param fromIndex 起始下标
     * @param toIndex 结束下标
     * @return void
     */
    Mono<Void> trim(int fromIndex, int toIndex);

    /**
     * 按指定下标快速移除元素（不返回值）。
     * 
     * @param index 元素下标
     * @return void
     */
    Mono<Void> fastRemove(int index);

    /**
     * 返回从 0 到 {@code toIndex} 的元素区间（下标从 0 起）。
     * {@code -1} 表示最后一个元素，{@code -2} 表示倒数第二个，依此类推。
     * 
     * @param toIndex 结束下标
     * @return 元素列表
     */
    Mono<List<V>> range(int toIndex);
    
    /**
     * 返回 {@code fromIndex} 到 {@code toIndex}（均含）的元素区间。
     * 下标从 0 起；{@code -1} 表示最后一个元素，{@code -2} 表示倒数第二个。
     * 
     * @param fromIndex 起始下标
     * @param toIndex 结束下标
     * @return 元素列表
     */
    Mono<List<V>> range(int fromIndex, int toIndex);

    /**
     * 注册 List 对象事件监听器。
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
    Mono<Integer> addListener(ObjectListener listener);

}
