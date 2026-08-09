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

import java.util.Comparator;
import java.util.List;

/**
 * 基于 Redis 实现的优先级队列。
 * <p>底层使用有序集合维护元素顺序，由 {@link Comparator} 决定优先级。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RPriorityQueue<V> extends RQueue<V>, RObject {

    /**
     * 返回本队列用于排序的 {@link Comparator}
     * 
     * @return 比较器实例
     */
    Comparator<? super V> comparator();
    
    /**
     * 一次性读取队列中的全部元素
     * 
     * @return 元素列表
     */
    List<V> readAll();
    
    /**
     * 取出本队列队尾最后一个可用元素，并将其插入 {@code queueName} 队列队首。
     *
     * @param queueName 目标队列名称
     * @return 队尾元素；超时无可用元素时为 {@code null}
     */
    V pollLastAndOfferFirstTo(String queueName);
    
    /**
     * 仅当当前队列为空时设置新的 {@link Comparator}
     *
     * @param comparator 元素比较器
     * @return 设置成功则为 {@code true}，否则 {@code false}
     */
    boolean trySetComparator(Comparator<? super V> comparator);

}
