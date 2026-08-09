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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * 分布式 List 的 RxJava3 API。
 * <p>各方法返回 {@link Single}、{@link Maybe}、{@link Completable} 或 {@link Flowable}。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
// TODO 添加 subList 支持
public interface RListRx<V> extends RCollectionRx<V>, RSortableRx<List<V>> {

    /**
     * 按指定下标批量加载元素。
     * 
     * @param indexes 元素下标
     * @return 元素列表
     */
    Single<List<V>> get(int... indexes);
    
    /**
     * 在 {@code elementToFind} 之后插入 {@code element}。
     * 
     * @param elementToFind 锚点元素
     * @param element 待插入/设置的元素
     * @return 新列表长度
     */
    Single<Integer> addAfter(V elementToFind, V element);
    
    /**
     * 在 {@code elementToFind} 之前插入 {@code element}。
     * 
     * @param elementToFind 锚点元素
     * @param element 待插入/设置的元素
     * @return 新列表长度
     */
    Single<Integer> addBefore(V elementToFind, V element);
    
    Flowable<V> descendingIterator();

    Flowable<V> descendingIterator(int startIndex);

    Flowable<V> iterator(int startIndex);

    /**
     * 返回 {@code element} 最后一次出现的下标；未找到则返回 -1。
     * -1 if element isn't found
     * 
     * @param element 待插入/设置的元素
     * @return 下标；未找到则为 -1
     */
    Single<Integer> lastIndexOf(Object element);

    /**
     * Returns last index of <code>element</code> or 
     * -1 if element isn't found
     * 
     * @param element to find
     * @return index of -1 if element isn't found
     */
    Single<Integer> indexOf(Object element);

    /**
     * 在 {@code index} 处插入 {@code element}，后续元素后移。
     * 后续元素后移。 
     * 
     * @param index 下标
     * @param element 待插入/设置的元素
     * @return {@code true} if list was changed
     */
    Completable add(int index, V element);

    /**
     * 在 {@code index} 处批量插入 {@code elements}，后续元素后移。
     * 后续元素后移。 
     * 
     * @param index 下标
     * @param elements 待插入的元素集合
     * @return {@code true} if list changed
     *      or {@code false} if element isn't found
     */
    Single<Boolean> addAll(int index, Collection<? extends V> elements);

    /**
     * 在 {@code index} 处设置 {@code element}（不返回旧值，比 {@link #set(int, Object)} 更快）。
     * 
     * @param index 下标
     * @param element 待插入/设置的元素
     * @return 无返回值
     */
    Completable fastSet(int index, V element);

    /**
     * 在 {@code index} 处设置 {@code element} 并返回旧值。
     * 
     * @param index 下标
     * @param element 待插入/设置的元素
     * @return 旧元素；未设置时为 null
     */
    Maybe<V> set(int index, V element);

    /**
     * 获取 {@code index} 处的元素。
     * 
     * @param index 下标
     * @return 元素
     */
    Maybe<V> get(int index);

    /**
     * 移除 {@code index} 处的元素。
     * 
     * @param index 下标
     * @return 被移除的元素；不存在时为 null
     */
    Maybe<V> remove(int index);
    
    /**
     * 一次性读取全部元素。
     *
     * @return 全部元素列表
     */
    Single<List<V>> readAll();

    /**
     * 裁剪列表，仅保留 {@code fromIndex}（含）到 {@code toIndex}（含）范围内的元素。
     * <code>fromIndex</code>, inclusive, and <code>toIndex</code>, inclusive.
     *
     * @param fromIndex 起始下标
     * @param toIndex 结束下标
     * @return 无返回值
     */
    Completable trim(int fromIndex, int toIndex);

    /**
     * 按指定下标快速移除元素。
     * 
     * @param index 下标
     * @return 无返回值
     */
    Completable fastRemove(int index);

    /**
     * 返回从 0 到 {@code toIndex} 的元素区间（下标从 0 起）。
     * {@code -1} 表示最后一个元素，{@code -2} 表示倒数第二个，依此类推。
     * 
     * @param toIndex 结束下标
     * @return 元素列表
     */
    Single<List<V>> range(int toIndex);
    
    /**
     * 返回 {@code fromIndex} 到 {@code toIndex}（含）闭区间内的元素。
     * 下标从 0 起计。 <code>-1</code> means the last element, <code>-2</code> means penultimate and so on.
     * 
     * @param fromIndex 起始下标
     * @param toIndex 结束下标
     * @return 元素列表
     */
    Single<List<V>> range(int fromIndex, int toIndex);

    /**
     * 注册对象事件监听器。
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
    Single<Integer> addListener(ObjectListener listener);

}
