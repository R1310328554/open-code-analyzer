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

import java.util.List;

/**
 * 共享相同字段名的 Map 批量导入 Reactor API。
 * <p>
 * 缓冲达到 batch 大小或调用 {@link #flush()} 时写入；未 flush 的数据不会写入。
 *
 * @author Nikita Koksharov
 * @param <K> 字段类型
 * @param <V> 值类型
 */
public interface RMapsImportReactive<K, V> {

    /**
     * 添加存储于 {@code name} 下的 Map 对象（变参形式）。
     * <p>
     * 值按位置与导入对象定义的字段名一一对应。
     *
     * @param name Redis 对象名
     * @param values 与字段名顺序对应的值
     * @return void
     */
    Mono<Void> add(String name, V... values);

    /**
     * 添加存储于 {@code name} 下的 Map 对象（List 形式）。
     * <p>
     * 值按位置与导入对象定义的字段名一一对应。
     *
     * @param name Redis 对象名
     * @param values 与字段名顺序对应的值
     * @return void
     */
    Mono<Void> add(String name, List<V> values);

    /**
     * 将缓冲中的全部 Map 对象写入 Redis。
     *
     * @return void
     */
    Mono<Void> flush();

    /**
     * 返回本导入对象已成功写入 Redis 的 Map 数量。
     *
     * @return 已导入的 Map 数量
     */
    long getImportedCount();

}
