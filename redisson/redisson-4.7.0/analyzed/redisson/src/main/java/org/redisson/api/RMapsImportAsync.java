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

import java.util.List;

/**
 * 共享相同字段名的 Map 批量导入异步 API。
 * <p>各方法返回 {@link RFuture}；缓冲满或 flush 时写入 Redis。
 *
 * @author Nikita Koksharov
 * @param <K> 字段类型
 * @param <V> 值类型
 */
public interface RMapsImportAsync<K, V> {

    /**
     * 异步添加存储于 {@code name} 下的 Map 对象（变参形式）。
     * <p>
     * 值按位置与字段名一一对应，数量须与字段数相等；值会立即编码。
     *
     * @param name Redis 对象名
     * @param values 与字段名顺序对应的值
     * @return void
     */
    RFuture<Void> addAsync(String name, V... values);

    /**
     * 异步添加存储于 {@code name} 下的 Map 对象（List 形式）。
     * <p>
     * 值按位置与字段名一一对应，数量须与字段数相等；值会立即编码。
     *
     * @param name Redis 对象名
     * @param values 与字段名顺序对应的值
     * @return void
     */
    RFuture<Void> addAsync(String name, List<V> values);

    /**
     * 异步将缓冲中的全部 Map 对象写入 Redis。
     *
     * @return void
     */
    RFuture<Void> flushAsync();

}
