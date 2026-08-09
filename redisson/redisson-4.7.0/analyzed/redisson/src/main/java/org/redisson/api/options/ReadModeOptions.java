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

import org.redisson.config.ReadMode;

/**
 * 允许按对象实例覆盖 Redisson 全局配置中定义的 {@link ReadMode}。
 *
 * @author Nikita Koksharov
 *
 * @param <T> 返回的选项对象类型
 */
public interface ReadModeOptions<T extends InvocationOptions<T>> extends InvocationOptions<T> {

    /**
     * 定义此对象读取操作使用的 {@link ReadMode}。
     * <p>
     * 仅覆盖本对象实例在 Redisson 全局配置中声明的 {@code readMode} 设置。
     * <p>
     * 设为 {@code null}（默认）时使用全局配置的 {@code readMode}。
     * <p>
     * <b>注意：</b>此覆盖是否生效取决于从节点连接池是否已初始化，这由全局
     * {@code readMode} 与 {@code subscriptionMode} 共同决定。当两项全局设置均为
     * {@code MASTER} 时不会初始化从节点池，因此在此配置下将 {@code readMode}
     * 覆盖为 {@code SLAVE} 或 {@code MASTER_SLAVE} 无效。此外，从节点池初始化后，
     * 仅当全局 {@code readMode} 为 {@code MASTER_SLAVE} 时主节点才会被纳入该池。
     * 因此：
     * <ul>
     * <li>覆盖为 {@code MASTER}：始终无条件生效。</li>
     * <li>覆盖为 {@code SLAVE}：要求全局设置为 {@code SLAVE} 或
     *     {@code MASTER_SLAVE}。全局为 {@code MASTER_SLAVE} 时，读取仍可能落到主节点，
     *     因为主节点属于从节点池。</li>
     * <li>覆盖为 {@code MASTER_SLAVE}：要求全局设置为 {@code MASTER_SLAVE}；
     *     全局为 {@code SLAVE} 时行为等同于 {@code SLAVE}，因为主节点不在从节点池中。</li>
     * </ul>
     *
     * @param readMode 应用于本对象实例的读取模式
     * @return 选项实例
     */
    T readMode(ReadMode readMode);

}
