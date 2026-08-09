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
package org.redisson.redisnode;

import org.redisson.api.NodeType;
import org.redisson.api.redisnode.RedisCluster;
import org.redisson.api.redisnode.RedisClusterMaster;
import org.redisson.api.redisnode.RedisClusterSlave;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.ConnectionManager;

import java.util.Collection;

/**
 * Redis Cluster 拓扑下的节点管理实现，实现 {@link RedisCluster}。
 * <p>
 * 通过 {@link RedissonBaseNodes#getNodes(NodeType)} 枚举集群内
 * 所有 Master 与 Slave 节点，并按地址精确查找。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonClusterNodes extends RedissonBaseNodes implements RedisCluster {

    /** @param connectionManager 集群连接管理器 @param commandExecutor 命令执行器 */
    public RedissonClusterNodes(ConnectionManager connectionManager, CommandAsyncExecutor commandExecutor) {
        super(connectionManager, commandExecutor);
    }

    /** @return 全部集群 Master 节点视图 */
    @Override
    public Collection<RedisClusterMaster> getMasters() {
        return getNodes(NodeType.MASTER);
    }

    /** @param address 节点地址 @return 指定 Master，不存在则为 null */
    @Override
    public RedisClusterMaster getMaster(String address) {
        return getNode(address, NodeType.MASTER);
    }

    /** @return 全部集群 Slave 节点视图 */
    @Override
    public Collection<RedisClusterSlave> getSlaves() {
        return getNodes(NodeType.SLAVE);
    }

    /** @param address 节点地址 @return 指定 Slave，不存在则为 null */
    @Override
    public RedisClusterSlave getSlave(String address) {
        return getNode(address, NodeType.SLAVE);
    }

}
