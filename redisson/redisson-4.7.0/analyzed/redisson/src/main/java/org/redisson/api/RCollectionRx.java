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

import org.reactivestreams.Publisher;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * 分布式集合对象的通用 RxJava 风格 API 接口。
 * <p>各方法返回 {@link Single} 或 {@link Flowable}。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RCollectionRx<V> extends RExpirableRx {

    /**
     * 返回集合元素的响应式迭代流。
     * 
     * @return 元素流
     */
    Flowable<V> iterator();

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).
     *
     * @param c collection containing elements to be retained in this collection
     * @return <code>true</code> if this collection changed as a result of the call
     */
    Single<Boolean> retainAll(Collection<?> c);

    /**
     * Removes all of this collection's elements that are also contained in the
     * specified collection (optional operation).
     *
     * @param c collection containing elements to be removed from this collection
     * @return <code>true</code> if this collection changed as a result of the
     *         call
     */
    Single<Boolean> removeAll(Collection<?> c);

    /**
     * 若本集合包含指定元素（按序列化状态比较）则返回 {@code true}。
     *
     * @param o 元素
     * @return <code>true</code> if this collection contains the specified
     *         element and <code>false</code> otherwise
     */
    Single<Boolean> contains(V o);

    /**
     * Returns <code>true</code> if this collection contains all of the elements
     * in the specified collection.
     *
     * @param  c collection to be checked for containment in this collection
     * @return <code>true</code> if this collection contains all of the elements
     *         in the specified collection
     */
    Single<Boolean> containsAll(Collection<?> c);

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation).
     *
     * @param o element to be removed from this collection, if present
     * @return <code>true</code> if an element was removed as a result of this call
     */
    Single<Boolean> remove(V o);

    /**
     * 返回集合元素数量。
     *
     * @return 元素数量
     */
    Single<Integer> size();

    /**
     * 向集合添加元素。
     * 
     * @param e 待添加元素
     * @return <code>true</code> if an element was added 
     *          and <code>false</code> if it is already present
     */
    Single<Boolean> add(V e);

    /**
     * 批量添加指定集合中的全部元素。
     * 
     * @param c 集合
     * @return <code>true</code> if at least one element was added 
     *          and <code>false</code> if all elements are already present
     */
    Single<Boolean> addAll(Publisher<? extends V> c);
    
    /**
     * Adds all elements contained in the specified collection
     * 
     * @param c - collection of elements to add
     * @return <code>true</code> if at least one element was added 
     *          and <code>false</code> if all elements are already present
     */
    Single<Boolean> addAll(Collection<? extends V> c);

}
