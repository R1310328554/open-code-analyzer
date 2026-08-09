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

import org.redisson.api.map.MapsImportArgs;

import java.util.Map;

/**
 * Map 批量操作接口。
 * <p>
 * 共享相同字段名的 Map 对象可一次性声明字段名，减少传输数据量，
 * 并让 Redis 对所有此类对象只保留一份字段名副本。
 *
 * @author Nikita Koksharov
 * @param <K> 字段类型
 * @param <V> 值类型
 */
public interface RMaps<K, V> extends RMapsAsync<K, V> {

    /**
     * 按名称批量写入 Map 对象，每个对象替换同名 Redis Map。
     * 替换同名 Redis Map 的全部内容。
     *
     * @param maps Map 对象映射（键名为 Redis 对象名）
     */
    void set(Map<String, Map<K, V>> maps);

    /**
     * 按名称批量写入 Map 对象，每个对象替换同名 Redis Map。
     * 替换同名 Redis Map 的全部内容。
     * <p>
     * 按 {@code batchSize} 分批写入。
     *
     * @param maps Map 对象映射（键名为 Redis 对象名）
     * @param batchSize 每批写入的 Map 数量
     */
    void set(Map<String, Map<K, V>> maps, int batchSize);

    /**
     * 返回共享 {@code args} 中字段名的 Map 批量导入对象。
     * <p>
     * 适合无法一次性载入内存的大量对象导入，
     * 逐个添加而非先收集到 Map。
     *
     * @param args 导入参数
     * @return 导入对象
     */
    @Override
    RMapsImport<K, V> createImport(MapsImportArgs<K> args);

}
