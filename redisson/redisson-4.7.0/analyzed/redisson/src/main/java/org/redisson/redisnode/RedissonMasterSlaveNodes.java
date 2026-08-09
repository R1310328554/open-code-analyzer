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
import org.redisson.api.redisnode.RedisMaster;
import org.redisson.api.redisnode.RedisMasterSlave;
import org.redisson.api.redisnode.RedisSlave;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.ConnectionManager;

import java.util.Collection;

/**
 * 主从复制拓扑的节点管理，实现 {@link RedisMasterSlave}。
 * <p>
 * 单主多从场景下 {@link #getMaster()} 返回首个 Master；
 * 从节点列表通过 {@link NodeType#SLAVE} 过滤。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonMasterSlaveNodes extends RedissonBaseNodes implements RedisMasterSlave {

    /** @param connectionManager 主从连接管理器 @param commandExecutor 命令执行器 */
    public RedissonMasterSlaveNodes(ConnectionManager connectionManager, CommandAsyncExecutor commandExecutor) {
        super(connectionManager, commandExecutor);
    }

    /** @return 当前拓扑中的主节点；无从节点配置时仍返回 Master */
    @Override
    public RedisMaster getMaster() {
        Collection<RedisMaster> list = getNodes(NodeType.MASTER);
        // 无主节点条目时返回 null
        if (list.isEmpty()) {
            return null;
        }
        return list.iterator().next();
    }

    /** @param address 主节点地址 @return 匹配的 Master 或 null */
    @Override
    public RedisMaster getMaster(String address) {
        return getNode(address, NodeType.MASTER);
    }

    /** @return 全部从节点 */
    @Override
    public Collection<RedisSlave> getSlaves() {
        return getNodes(NodeType.SLAVE);
    }

    /** @param address 从节点地址 @return 匹配的 Slave 或 null */
    @Override
    public RedisSlave getSlave(String address) {
        return getNode(address, NodeType.SLAVE);
    }

}
