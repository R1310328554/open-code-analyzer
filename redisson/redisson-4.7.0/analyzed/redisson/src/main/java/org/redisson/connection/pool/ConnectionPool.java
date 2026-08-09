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

import org.redisson.api.NodeType;
import org.redisson.client.FailedNodeDetector;
import org.redisson.client.RedisConnection;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.ConnectionsHolder;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.misc.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 连接池抽象基类，负责从 {@link MasterSlaveEntry} 选取节点并获取连接。
 * <p>
 * 通过 {@link LoadBalancer} 在可用从节点间分配负载；
 * 连接获取失败时触发 {@link FailedNodeDetector} 并从池归还连接。
 *
 * @author Nikita Koksharov
 *
 * @param <T> 连接类型（{@link RedisConnection} 或 {@link RedisPubSubConnection}）
 */
abstract class ConnectionPool<T extends RedisConnection> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** 全局连接管理器。 */
    final ConnectionManager connectionManager;

    /** 主从服务器配置（含负载均衡器、失败检测器等）。 */
    final MasterSlaveServersConfig config;

    /** 所属主从条目，聚合各节点连接入口。 */
    final MasterSlaveEntry masterSlaveEntry;

    /** 绑定配置、连接管理器与主从条目。 */
    ConnectionPool(MasterSlaveServersConfig config, ConnectionManager connectionManager, MasterSlaveEntry masterSlaveEntry) {
        this.config = config;
        this.masterSlaveEntry = masterSlaveEntry;
        this.connectionManager = connectionManager;
    }

    /** 返回指定入口对应的连接持有者（普通/PubSub/追踪连接）。 */
    protected abstract ConnectionsHolder<T> getConnectionHolder(ClientConnectionsEntry entry, boolean trackChanges);

    /**
     * 尝试获取连接，返回 Future 与异常的元组（无可用节点时 T1 为 null）。
     *
     * @param command 待执行的 Redis 命令（影响负载均衡选择）
     * @param trackChanges 是否使用 CLIENT TRACKING 连接池
     */
        // 收集所有节点入口，排除冻结及失败检测标记为不可用的从节点
        Collection<ClientConnectionsEntry> entries = masterSlaveEntry.getAllEntries();
        List<ClientConnectionsEntry> entriesCopy = new ArrayList<>(entries);
        entriesCopy.removeIf(n -> n.isFreezed() || !isHealthy(n));
        if (!entriesCopy.isEmpty()) {
            ClientConnectionsEntry entry = config.getLoadBalancer().getEntry(entriesCopy, command);
            if (entry != null) {
                log.debug("Entry {} selected as connection source", entry);
                return new Tuple<>(acquireConnection(command, entry, trackChanges), null);
            }
        }
        
        List<InetSocketAddress> failed = new ArrayList<>();
        List<InetSocketAddress> freezed = new ArrayList<>();
        for (ClientConnectionsEntry entry : entries) {
            if (entry.getClient().getConfig().getFailedNodeDetector().isNodeFailed()) {
                failed.add(entry.getClient().getAddr());
            } else if (entry.isFreezed()) {
                freezed.add(entry.getClient().getAddr());
            }
        }

        StringBuilder errorMsg = new StringBuilder(getClass().getSimpleName() + " no available Redis entries. " +
                "Master entry host: " + masterSlaveEntry.getClient().getAddr() + " entries " + entries);
        if (!freezed.isEmpty()) {
            errorMsg.append(" Disconnected hosts: ").append(freezed);
        }
        if (!failed.isEmpty()) {
            errorMsg.append(" Hosts disconnected by 'failedNodeDetector:' ").append(failed);
        }

        RedisConnectionException exception = new RedisConnectionException(errorMsg.toString());
        return new Tuple<>(null, exception);
    }

    /** 获取连接；无可用节点时返回已完成 exceptionally 的 Future。 */
    public CompletableFuture<T> get(RedisCommand<?> command, boolean trackChanges) {
        Tuple<CompletableFuture<T>, Throwable> tuple = getTuple(command, trackChanges);
        if (tuple.getT2() != null) {
            CompletableFuture<T> result = new CompletableFuture<>();
            result.completeExceptionally(tuple.getT2());
            return result;
        }
        return tuple.getT1();
    }

    /** 从指定连接入口直接获取连接（跳过负载均衡）。 */
    public CompletableFuture<T> get(RedisCommand<?> command, ClientConnectionsEntry entry, boolean trackChanges) {
        return acquireConnection(command, entry, trackChanges);
    }

    /**
     * 从指定入口的 ConnectionsHolder 异步获取连接。
     * <p>
     * 从节点连接失败时通知 FailedNodeDetector，必要时触发 shutdownAndReconnectAsync。
     */
        ConnectionsHolder<T> handler = getConnectionHolder(entry, trackChanges);
        CompletableFuture<T> result = handler.acquireConnection(command);
        CompletableFuture<T> cancelableFuture = new CompletableFuture<>();
        cancelableFuture.whenComplete((r, e) -> {
            if (e != null) {
                result.completeExceptionally(e);
            }
        });
        result.whenComplete((r, e) -> {
            if (e != null) {
                if (entry.getNodeType() == NodeType.SLAVE) {
                    FailedNodeDetector detector = entry.getClient().getConfig().getFailedNodeDetector();
                    detector.onConnectFailed(e);
                    if (detector.isNodeFailed()) {
                        log.error("Redis node {} has been marked as failed according to the detection logic defined in {}",
                                        entry.getClient().getAddr(), detector);
                        masterSlaveEntry.shutdownAndReconnectAsync(entry.getClient(), e);
                    }
                }
                cancelableFuture.completeExceptionally(e);
                return;
            }

            entry.addHandler(r, handler);

            if (entry.getNodeType() == NodeType.SLAVE) {
                entry.getClient().getConfig().getFailedNodeDetector().onConnectSuccessful();
            }

            if (!cancelableFuture.complete(r)) {
                entry.returnConnection(r);
            }
        });
        return cancelableFuture;
    }
        
    /** 从节点若被失败检测器标记为 failed 则视为不健康。 */
    private boolean isHealthy(ClientConnectionsEntry entry) {
        if (entry.getNodeType() == NodeType.SLAVE
                && entry.getClient().getConfig().getFailedNodeDetector().isNodeFailed()) {
            return false;
        }
        return true;
    }

}
