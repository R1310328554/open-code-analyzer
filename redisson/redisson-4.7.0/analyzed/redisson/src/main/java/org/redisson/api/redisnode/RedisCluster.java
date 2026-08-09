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
 * Valkey 或 Redis 集群节点访问 API。
 * <p>
 * 用于按主从角色浏览集群拓扑，并获取指定地址的主节点或从节点句柄。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisCluster extends BaseRedisNodes {

    /**
     * 返回当前 Redis 集群中所有主（Master）节点。
     *
     * @return 主节点集合
     */
    Collection<RedisClusterMaster> getMasters();

    /**
     * 按地址获取 Redis 主节点。
     * <p>
     * 地址示例：<code>redis://127.0.0.1:9233</code>
     *
     * @return 对应的主节点
     */
    RedisClusterMaster getMaster(String address);

    /**
     * 返回当前 Redis 集群中所有从（Slave）节点。
     *
     * @return 从节点集合
     */
    Collection<RedisClusterSlave> getSlaves();

    /**
     * 按地址获取 Redis 从节点。
     * <p>
     * 地址示例：<code>redis://127.0.0.1:9233</code>
     *
     * @return 对应的从节点
     */
    RedisClusterSlave getSlave(String address);

}
