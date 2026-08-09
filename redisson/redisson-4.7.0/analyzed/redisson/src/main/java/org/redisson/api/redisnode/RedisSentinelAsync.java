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

import org.redisson.api.RFuture;
import org.redisson.misc.RedisURI;

import java.util.List;
import java.util.Map;

/**
 * Redis Sentinel 节点异步 API。
 * <p>
 * 以 {@link RFuture} 形式暴露 {@link RedisSentinel} 中的拓扑查询与故障转移能力。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisSentinelAsync extends RedisNodeAsync {

    /**
     * 异步返回指定主节点名称对应的网络地址。
     *
     * @param masterName 主节点名称
     * @return 网络地址
     */
    RFuture<RedisURI> getMasterAddrAsync(String masterName);

    /**
     * 异步返回监控指定主节点的 Sentinel 实例信息列表。
     *
     * @param masterName 主节点名称
     * @return Sentinel 信息列表
     */
    RFuture<List<Map<String, String>>> getSentinelsAsync(String masterName);

    /**
     * 异步返回当前 Sentinel 所监控的所有 Redis 主节点信息列表。
     *
     * @return 主节点信息列表
     */
    RFuture<List<Map<String, String>>> getMastersAsync();

    /**
     * 异步返回指定主节点下所有从节点信息列表。
     *
     * @param masterName 主节点名称
     * @return 从节点信息列表
     */
    RFuture<List<Map<String, String>>> getSlavesAsync(String masterName);

    /**
     * 异步返回指定主节点的详细信息。
     *
     * @param masterName 主节点名称
     * @return 主节点信息键值对
     */
    RFuture<Map<String, String>> getMasterAsync(String masterName);

    /**
     * 异步对指定主节点发起手动故障转移（failover）。
     *
     * @param masterName 主节点名称
     */
    RFuture<Void> failoverAsync(String masterName);

}
