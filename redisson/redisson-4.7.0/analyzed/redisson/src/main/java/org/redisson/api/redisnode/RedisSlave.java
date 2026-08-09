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
package org.redisson.api.redisnode;

/**
 * Redis 从节点（Slave）同步访问 API。
 * <p>
 * 组合 {@link RedisNode} 通用节点操作与 {@link RedisSlaveAsync} 异步能力，
 * 用于主从复制拓扑中的从节点管理与命令执行。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisSlave extends RedisNode, RedisSlaveAsync {
}
