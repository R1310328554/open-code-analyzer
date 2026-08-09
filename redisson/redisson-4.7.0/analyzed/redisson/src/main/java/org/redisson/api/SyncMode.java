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

/**
 * 定义与 Valkey 或 Redis 副本实例复制时的同步模式。
 *
 * @author Nikita Koksharov
 *
 */
public enum SyncMode {

    /**
     * 通过阻塞直至写操作在主节点及副本的内存与 AOF 中持久化确认来保证数据持久性（启用 AOF 时）；
     * 若 AOF 不可用，则退化为阻塞直至副本确认写操作已写入内存；
     * 若两种机制均不可用，则不提供同步保证继续执行。
     *
     */
    AUTO,

    /**
     * 阻塞直至副本确认写操作已写入内存，以保证数据持久性。
     */
    ACK,

    /**
     * 阻塞直至主节点及副本的 AOF 确认写操作已持久化，以保证数据持久性。
     * <p>
     * NOTE: Redis 7.2.0+ or any Valkey version is required
     *
     */
    ACK_AOF

}
