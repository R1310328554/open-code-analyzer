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
 * Redis 节点 API 类型标识符。
 * <p>
 * 预定义集群、主从、哨兵主从及单机等部署模式对应的 {@link BaseRedisNodes} 实现类型，
 * 供 {@link org.redisson.Redisson#getRedisNodes(RedisNodes)} 选择正确的节点访问入口。
 *
 * @author Nikita Koksharov
 *
 */
public final class RedisNodes<T extends BaseRedisNodes> {

    /** 集群模式节点 API 类型 */
    public static final RedisNodes<RedisCluster> CLUSTER = new RedisNodes<>(RedisCluster.class);
    /** 主从模式节点 API 类型 */
    public static final RedisNodes<RedisMasterSlave> MASTER_SLAVE = new RedisNodes<>(RedisMasterSlave.class);
    /** 哨兵 + 主从模式节点 API 类型 */
    public static final RedisNodes<RedisSentinelMasterSlave> SENTINEL_MASTER_SLAVE = new RedisNodes<>(RedisSentinelMasterSlave.class);
    /** 单机模式节点 API 类型 */
    public static final RedisNodes<RedisSingle> SINGLE = new RedisNodes<>(RedisSingle.class);

    private final Class<T> clazz;

    RedisNodes(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * 返回绑定的 {@link BaseRedisNodes} 实现类。
     *
     * @return 节点 API 接口类型
     */
    public Class<T> getClazz() {
        return clazz;
    }
}
