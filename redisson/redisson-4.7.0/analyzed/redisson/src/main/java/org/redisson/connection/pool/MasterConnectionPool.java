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
package org.redisson.connection.pool;

import org.redisson.client.RedisConnection;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.ConnectionsHolder;
import org.redisson.connection.MasterSlaveEntry;

import java.util.concurrent.CompletableFuture;


/**
 * 主节点普通命令连接池。
 * <p>
 * 写操作及强制走主的读操作通过此池获取 {@link RedisConnection}；
 * 始终绑定 {@link MasterSlaveEntry#getEntry()} 主节点入口。
 *
 * @author Nikita Koksharov
 *
 */
public class MasterConnectionPool extends ConnectionPool<RedisConnection> {

    /** 构造主节点连接池。 */
    public MasterConnectionPool(MasterSlaveServersConfig config,
            ConnectionManager connectionManager, MasterSlaveEntry masterSlaveEntry) {
        super(config, connectionManager, masterSlaveEntry);
    }

    /** trackChanges 为 true 时使用 CLIENT TRACKING 专用连接池。 */
    @Override
    protected ConnectionsHolder<RedisConnection> getConnectionHolder(ClientConnectionsEntry entry, boolean trackChanges) {
        if (trackChanges) {
            return entry.getTrackedConnectionsHolder();
        }
        return entry.getConnectionsHolder();
    }

    /** 直接从主节点入口获取连接，不经过负载均衡。 */
    @Override
    public CompletableFuture<RedisConnection> get(RedisCommand<?> command, boolean trackChanges) {
        return acquireConnection(command, masterSlaveEntry.getEntry(), trackChanges);
    }

}
