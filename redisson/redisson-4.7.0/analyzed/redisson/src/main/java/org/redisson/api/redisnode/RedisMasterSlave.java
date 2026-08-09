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

import java.util.Collection;

/**
 * Valkey 或 Redis 主从（Master-Slave）部署的节点访问 API。
 * <p>
 * 用于获取当前拓扑中的主节点及从节点集合，并按地址定位具体节点。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisMasterSlave extends BaseRedisNodes {

    /**
     * 返回当前 Redis 主从部署中的主节点。
     *
     * @return 主节点
     */
    RedisMaster getMaster();

    /**
     * 按地址获取 Redis 主节点。
     * <p>
     * 地址示例：<code>redis://127.0.0.1:9233</code>
     *
     * @return 对应的主节点
     */
    RedisMaster getMaster(String address);

    /**
     * 返回当前 Redis 主从部署中的所有从节点。
     *
     * @return 从节点集合
     */
    Collection<RedisSlave> getSlaves();

    /**
     * 按地址获取 Redis 从节点。
     * <p>
     * 地址示例：<code>redis://127.0.0.1:9233</code>
     *
     * @return 对应的从节点
     */
    RedisSlave getSlave(String address);

}
