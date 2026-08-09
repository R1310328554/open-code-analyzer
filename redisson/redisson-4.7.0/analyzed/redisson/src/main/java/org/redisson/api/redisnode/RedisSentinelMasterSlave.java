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
 * Valkey 或 Redis Sentinel 主从部署的节点访问 API。
 * <p>
 * 在 {@link RedisMasterSlave} 主从节点能力之上，额外提供 Sentinel 哨兵节点的查询与定位。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisSentinelMasterSlave extends RedisMasterSlave {

    /**
     * 返回当前 Redis 部署所关联的所有 Sentinel 哨兵节点。
     *
     * @return Sentinel 哨兵节点集合
     */
    Collection<RedisSentinel> getSentinels();

    /**
     * 按地址获取 Redis Sentinel 哨兵节点。
     * <p>
     * 地址示例：<code>redis://127.0.0.1:9233</code>
     *
     * @return 对应的 Sentinel 哨兵节点
     */
    RedisSentinel getSentinel(String address);

}
