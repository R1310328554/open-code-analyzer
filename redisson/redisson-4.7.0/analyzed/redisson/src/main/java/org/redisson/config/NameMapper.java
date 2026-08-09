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
package org.redisson.config;

/**
 * Redisson 分布式对象名称映射器。
 * <p>在客户端与 Redis 之间对 RMap、RLock 等对象的逻辑名做双向转换，
 * 便于多租户前缀、环境隔离等场景；{@link #direct()} 为恒等映射。
 *
 * @author Nikita Koksharov
 *
 */
public interface NameMapper {

    /**
     * 将业务侧对象名映射为 Redis 实际使用的键名/通道名。
     *
     * @param name 原始 Redisson 对象名
     * @return 映射后的名称
     */
    String map(String name);

    /**
     * 将 Redis 侧名称还原为业务侧原始对象名（与 {@link #map} 互逆）。
     *
     * @param name 映射后的名称
     * @return 原始 Redisson 对象名
     */
    String unmap(String name);

    /**
     * 返回恒等映射器，输入名称原样输出（默认行为）。
     *
     * @return 不做任何转换的 {@link NameMapper} 实例
     */
    static NameMapper direct() {
        return new DefaultNameMapper();
    }

}
