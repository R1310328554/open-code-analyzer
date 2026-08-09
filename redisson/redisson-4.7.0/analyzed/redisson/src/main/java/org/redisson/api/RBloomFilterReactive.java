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

import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

/**
 * 基于 Highway 128 位哈希的分布式布隆过滤器 API。
 * <p>支持添加、批量检测、初始化参数查询及概率计数等操作。
 *
 * @author Nikita Koksharov
 * @param <T> 元素类型
 */
public interface RBloomFilterReactive<T> extends RExpirableReactive {

    /**
     * 添加单个元素。
     *
     * @param object 待添加元素
     * @return 新插入为 {@code true}，已存在为 {@code false}
     */
    Mono<Boolean> add(T object);

    /**
     * 批量添加元素。
     *
     * @param elements 待添加元素集合
     * @return 成功添加的数量
     */
    Mono<Long> add(Collection<T> elements);

    /**
     * 检测单个元素是否可能存在。
     *
     * @param object 待检测元素
     * @return 可能存在为 {@code true}，肯定不存在为 {@code false}
     */
    Mono<Boolean> contains(T object);

    /**
     * 批量检测元素是否可能存在。
     *
     * @param elements 待检测元素集合
     * @return 判定为可能存在的元素数量
     */
    Mono<Long> contains(Collection<T> elements);

    /**
     * 批量检测元素，返回可能存在于过滤器中的元素集合。
     * <p>未出现在返回集合中的元素一定不存在。
     *
     * @param elements 待检测元素
     * @return 可能存在的元素集合
     */
    Mono<Set<T>> exists(Collection<T> elements);

    /**
     * 根据期望插入量与误判率初始化布隆过滤器参数（位数组大小与哈希迭代次数），并写入 Redis。
     *
     * @param expectedInsertions 预期插入元素数量
     * @param falseProbability 可接受的误判率
     * @return 首次初始化成功为 {@code true}，已初始化过为 {@code false}
     */
    Mono<Boolean> tryInit(long expectedInsertions, double falseProbability);

    /**
     * 返回初始化时设定的预期插入元素数量。
     *
     * @return 预期插入量
     */
    Mono<Long> getExpectedInsertions();

    /**
     * 返回初始化时设定的误判率。
     *
     * @return 误判率
     */
    Mono<Double> getFalseProbability();

    /**
     * 返回本实例在 Redis 中占用的位数。
     *
     * @return 位数
     */
    Mono<Long> getSize();

    /**
     * 返回每个元素使用的哈希迭代次数（初始化时计算）。
     *
     * @return 哈希迭代次数
     */
    Mono<Integer> getHashIterations();

    /**
     * 估算已添加到布隆过滤器的元素数量（概率性统计）。
     *
     * @return 估算元素数量
     */
    Mono<Long> count();

}
