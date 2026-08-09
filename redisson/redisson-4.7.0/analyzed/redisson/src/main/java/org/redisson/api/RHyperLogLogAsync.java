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

/**
 * {@link RHyperLogLog} 异步 API。
 * <p>各方法返回 {@link RFuture}。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RHyperLogLogAsync<V> extends RExpirableAsync {

    /**
     * 向 HyperLogLog 添加元素。
     * 
     * @param obj 待添加元素
     * @return 见方法说明
     *          or <code>false</code> if it was already added
     */
    RFuture<Boolean> addAsync(V obj);

    /**
     * 批量向 HyperLogLog 添加元素。
     * 
     * @param objects 待添加元素集合
     * @return 见方法说明
     *          or <code>false</code> if all were already added
     */
    RFuture<Boolean> addAllAsync(Collection<V> objects);

    /**
     * 返回已添加唯一元素的近似基数。
     * 
     * @return 已添加唯一元素的近似基数
     */
    RFuture<Long> countAsync();

    /**
     * 返回本实例与其他指定实例合并后的近似唯一元素数。
     * 本实例与 {@code otherLogNames} 指定其他实例合并后的近似唯一元素数。
     * 
     * @param otherLogNames 其他 HyperLogLog 键名
     * @return 唯一元素近似基数
     */
    RFuture<Long> countWithAsync(String... otherLogNames);

    /**
     * 将多个 HyperLogLog 实例合并到本实例。
     * 
     * @param otherLogNames 其他 HyperLogLog 键名
     * @return void
     */
    RFuture<Void> mergeWithAsync(String... otherLogNames);

}
