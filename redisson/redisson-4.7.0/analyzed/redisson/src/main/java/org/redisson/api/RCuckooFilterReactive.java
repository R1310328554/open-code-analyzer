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

import org.redisson.api.cuckoofilter.CuckooFilterAddArgs;
import org.redisson.api.cuckoofilter.CuckooFilterInitArgs;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

/**
 * 布谷鸟过滤器 Reactor 风格 API（{@code CF.*} 命令）。
 * <p>各方法返回 {@link Mono}。
 *
 * @param <V> 元素类型
 * @author Nikita Koksharov
 */
public interface RCuckooFilterReactive<V> extends RExpirableReactive {

    /**
     * 以指定容量初始化布谷鸟过滤器。
     * <p>
     * Equivalent to {@code CF.RESERVE key capacity}.
     *
     * @param capacity 预期元素数量
     * @return void
     */
    Mono<Void> init(long capacity);

    /**
     * 以详细参数初始化布谷鸟过滤器。
     * <p>
     * Equivalent to {@code CF.RESERVE key capacity [BUCKETSIZE ..] [MAXITERATIONS ..] [EXPANSION ..]}.
     *
     * @param args 初始化/添加参数
     * @return void
     */
    Mono<Void> init(CuckooFilterInitArgs args);

    /**
     * 向过滤器添加元素。
     * 允许重复添加同一元素。
     * <p>
     * Equivalent to {@code CF.ADD}.
     *
     * @param element 元素
     * @return {@code true} if the element was successfully added
     */
    Mono<Boolean> add(V element);

    /**
     * 批量添加元素，可选容量与 noCreate 控制。
     * 返回成功添加的元素集合。
     * <p>
     * Equivalent to {@code CF.INSERT}.
     *
     * @param args 初始化/添加参数
     * @return 元素集合
     */
    Mono<Set<V>> add(CuckooFilterAddArgs<V> args);

    /**
     * 仅当元素可能不存在时添加（ADDNX）。
     * <p>
     * Equivalent to {@code CF.ADDNX}.
     *
     * @param element 元素
     * @return {@code true} if the element was added,
     *         {@code false} if it may already exist
     */
    Mono<Boolean> addIfAbsent(V element);

    /**
     * 批量添加可能不存在的元素（INSERTNX）。
     * 返回成功添加的元素集合。
     * <p>
     * Equivalent to {@code CF.INSERTNX}.
     *
     * @param args 初始化/添加参数
     * @return 元素集合
     */
    Mono<Set<V>> addIfAbsent(CuckooFilterAddArgs<V> args);

    /**
     * 探测元素是否可能存在（允许假阳性）。
     * <p>
     * Equivalent to {@code CF.EXISTS}.
     *
     * @param element 元素
     * @return {@code true} if the element may exist,
     *         {@code false} if it definitely does not
     */
    Mono<Boolean> exists(V element);

    /**
     * 批量探测多个元素是否可能存在。
     * 返回可能存在（假阳性）的元素集合。
     * <p>
     * Equivalent to {@code CF.MEXISTS}.
     *
     * @param elements 元素集合
     * @return 元素集合
     */
    Mono<Set<V>> exists(Collection<V> elements);

    /**
     * 从过滤器删除元素。
     * <p>
     * Equivalent to {@code CF.DEL}.
     *
     * @param element 元素
     * @return {@code true} if the element was found and removed,
     *         {@code false} if the element was not found
     */
    Mono<Boolean> remove(V element);

    /**
     * 返回元素在过滤器中的近似出现次数。
     * 在过滤器中的近似出现次数。
     * <p>
     * Equivalent to {@code CF.COUNT}.
     *
     * @param element 元素
     * @return 近似出现次数
     */
    Mono<Long> count(V element);

    /**
     * 返回过滤器统计信息。
     * <p>
     * Equivalent to {@code CF.INFO}.
     *
     * @return 过滤器信息
     */
    Mono<CuckooFilterInfo> getInfo();
}
