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

import org.redisson.api.topk.TopKInitArgs;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Top-K（{@code TOPK.*} 命令）Reactor 响应式 API。
 *
 * @param <V> 元素类型
 *
 * @author Nikita Koksharov
 *
 */
public interface RTopKReactive<V> extends RExpirableReactive {

    /**
     * 初始化 Top-K，跟踪频率最高的 {@code topK} 个元素。
     * <p>
     * Equivalent to {@code TOPK.RESERVE key topk}.
     *
     * @param topK 跟踪的最高频元素数量
     * @return 无返回值
     */
    Mono<Void> init(int topK);

    /**
     * 使用详细参数初始化 Top-K。
     * <p>
     * Equivalent to {@code TOPK.RESERVE key topk width depth decay}.
     *
     * @param args 初始化参数
     * @return 无返回值
     */
    Mono<Void> init(TopKInitArgs args);

    /**
     * 向 Top-K 添加一个元素。
     * <p>
     * Equivalent to {@code TOPK.ADD}.
     *
     * @param item 待添加元素
     * @return item dropped from the top-K list as a result,
     *         无挤出则为 empty
     */
    Mono<V> add(V item);

    /**
     * 批量向 Top-K 添加元素。
     * <p>
     * 返回列表与输入按位置对齐：索引 {@code i} 处为添加 {@code items.get(i)} 时
     * 从 Top-K 列表中被挤出的元素，无挤出则为 {@code null}。
     * <p>
     * Equivalent to {@code TOPK.ADD}.
     *
     * @param items 待添加元素列表
     * @return 与输入对齐的被挤出元素列表，含 null 占位
     */
    Mono<List<V>> add(List<V> items);

    /**
     * 将指定元素的计数增加给定增量。
     * <p>
     * Equivalent to {@code TOPK.INCRBY}.
     *
     * @param item 待增量元素
     * @param increment 增量值
     * @return item dropped from the top-K list as a result,
     *         无挤出则为 empty
     */
    Mono<V> incrementBy(V item, int increment);

    /**
     * 批量增加多个元素的计数。
     * <p>
     * 返回被挤出 Top-K 的元素列表，顺序与 Map 迭代顺序一致；
     * 建议使用有序 Map（如 {@link java.util.LinkedHashMap}）以便与输入对应。
     * <p>
     * Equivalent to {@code TOPK.INCRBY}.
     *
     * @param itemIncrements 元素与增量值的映射
     * @return 被挤出元素列表，含 null 占位
     */
    Mono<List<V>> incrementBy(Map<V, Integer> itemIncrements);

    /**
     * 检查元素是否当前位于 Top-K 列表中。
     * <p>
     * Equivalent to {@code TOPK.QUERY}.
     *
     * @param item 待检查元素
     * @return 元素在 Top-K 中则为 true
     */
    Mono<Boolean> contains(V item);

    /**
     * 批量检查元素是否位于 Top-K 列表中；结果与输入按位置对齐。
     * <p>
     * Equivalent to {@code TOPK.QUERY}.
     *
     * @param items 待检查元素列表
     * @return 与输入对齐的检查结果列表
     */
    Mono<List<Boolean>> contains(List<V> items);

    /**
     * 返回元素的近似出现次数。
     * <p>
     * Equivalent to {@code TOPK.COUNT}.
     *
     * @param item 待统计元素
     * @return 近似出现次数
     * @deprecated 自 Redis Bloom 2.4.0 起计数可能不准确。
     *             请改用 {@link #listWithCount()}。
     */
    @Deprecated
    Mono<Long> count(V item);

    /**
     * 返回多个元素的近似出现次数；结果与输入按位置对齐。
     * <p>
     * Equivalent to {@code TOPK.COUNT}.
     *
     * @param items items to count
     * @return 与输入对齐的近似次数列表
     * @deprecated 自 Redis Bloom 2.4.0 起计数可能不准确。
     *             请改用 {@link #listWithCount()}。
     */
    @Deprecated
    Mono<List<Long>> count(List<V> items);

    /**
     * 返回当前 Top-K 列表中的全部元素。
     * <p>
     * Equivalent to {@code TOPK.LIST}.
     *
     * @return Top-K 元素列表
     */
    Mono<List<V>> list();

    /**
     * 返回当前 Top-K 列表中的全部元素及其近似出现次数。
     * <p>
     * Equivalent to {@code TOPK.LIST WITHCOUNT}.
     *
     * @return Top-K 元素到近似次数的映射
     */
    Mono<Map<V, Long>> listWithCount();

    /**
     * 返回 Top-K 元信息。
     * <p>
     * Equivalent to {@code TOPK.INFO}.
     *
     * @return Top-K 元信息
     */
    Mono<TopKInfo> getInfo();
}
