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

import org.redisson.api.RKeys;
import org.redisson.api.RType;

/**
 * {@link RKeys#getKeys()} 方法的扫描配置选项。
 *
 * @author Nikita Koksharov
 *
 */
public interface KeysScanOptions {

    /**
     * 创建默认配置。
     *
     * @return 配置实例
     */
    static KeysScanOptions defaults() {
        return new KeysScanParams();
    }

    /**
     * 设置返回键的总数量上限。
     *
     * @param value 返回键的总数量
     * @return 配置实例
     */
    KeysScanOptions limit(int value);

    /**
     * 设置键名匹配模式，所有返回的键须符合该 glob 模式。
     * 支持的 glob 风格模式：
     *  <p>
     *    h?llo 匹配 hello、hallo 和 hxllo
     *    <p>
     *    h*llo 匹配 hllo 和 heeeello
     *    <p>
     *    h[ae]llo 匹配 hello 和 hallo，但不匹配 hillo
     *
     * @param value 键名匹配模式
     * @return 配置实例
     */
    KeysScanOptions pattern(String value);

    /**
     * 设置每次请求从 Redis 加载的键数量（分块大小）。
     *
     * @param value 每次请求加载的键数量
     * @return 配置实例
     */
    KeysScanOptions chunkSize(int value);

    /**
     * 设置键对应 Redis 对象类型过滤条件。
     *
     * @param value Redis 对象类型
     * @return 配置实例
     */
    KeysScanOptions type(RType value);

}
