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
package org.redisson.api.options;

/**
 * {@link org.redisson.api.RMapCache} 实例的配置选项。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface MapCacheOptions<K, V> extends ExMapOptions<MapCacheOptions<K, V>, K, V> {

    /**
     * 按对象实例名称创建配置。
     *
     * @param name 对象实例名称
     * @return 配置实例
     */
    static <K, V> MapCacheOptions<K, V> name(String name) {
        return new MapCacheParams<>(name);
    }

    /**
     * 条目淘汰流程结束后，若 Map 已为空则从内存中移除淘汰任务。
     *
     * @return MapCacheOptions 配置实例
     */
    MapCacheOptions<K, V> removeEmptyEvictionTask();


}
