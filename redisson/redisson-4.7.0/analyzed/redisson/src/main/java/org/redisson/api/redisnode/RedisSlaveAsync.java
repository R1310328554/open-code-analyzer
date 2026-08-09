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
 * Redis 从节点（Slave）异步访问 API。
 * <p>
 * 继承 {@link RedisNodeAsync}，提供非阻塞方式访问主从复制拓扑中的从节点。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisSlaveAsync extends RedisNodeAsync {
}
