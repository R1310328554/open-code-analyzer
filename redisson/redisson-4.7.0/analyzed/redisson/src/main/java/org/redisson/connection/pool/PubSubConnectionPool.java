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

import org.redisson.client.RedisPubSubConnection;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.ConnectionsHolder;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.misc.Tuple;

import java.util.concurrent.CompletableFuture;

/**
 * 主从模式下的 Pub/Sub 连接池，通过负载均衡从可用节点获取订阅连接。
 * <p>
 * 默认以 {@link RedisCommands#SUBSCRIBE} 作为负载均衡命令类型。
 *
 * @author Nikita Koksharov
 *
 */
public class PubSubConnectionPool extends ConnectionPool<RedisPubSubConnection> {

    /** 构造 Pub/Sub 连接池。 */
    public PubSubConnectionPool(MasterSlaveServersConfig config, ConnectionManager connectionManager, MasterSlaveEntry masterSlaveEntry) {
        super(config, connectionManager, masterSlaveEntry);
    }

    /** 以 SUBSCRIBE 命令类型负载均衡获取 Pub/Sub 连接，返回 Future/异常元组。 */
    public Tuple<CompletableFuture<RedisPubSubConnection>, Throwable> getTuple() {
        return getTuple(RedisCommands.SUBSCRIBE, false);
    }

    /** 负载均衡获取 Pub/Sub 连接。 */
    public CompletableFuture<RedisPubSubConnection> get() {
        return get(RedisCommands.SUBSCRIBE, false);
    }

    /** 从指定节点入口获取 Pub/Sub 连接。 */
    public CompletableFuture<RedisPubSubConnection> get(ClientConnectionsEntry entry) {
        return get(RedisCommands.SUBSCRIBE, entry, false);
    }

    /** Pub/Sub 连接使用专用的 pubSubConnectionsHolder。 */
    @Override
    protected ConnectionsHolder<RedisPubSubConnection> getConnectionHolder(ClientConnectionsEntry entry, boolean trackChanges) {
        return entry.getPubSubConnectionsHolder();
    }

}
