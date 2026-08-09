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
import org.redisson.api.RFuture;
import org.redisson.api.redisnode.BaseRedisNodes;
import org.redisson.client.RedisConnection;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.misc.RedisURI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Redis 节点集合管理的抽象基类，实现 {@link BaseRedisNodes} 通用能力。
 * <p>
 * 从 {@link ConnectionManager} 的 {@link MasterSlaveEntry} 枚举主/从节点，
 * 封装 {@link RedisNode} 视图，并提供批量 PING 健康检查。
 * <p>
 * 子类（Cluster、MasterSlave、Single、Sentinel）仅暴露不同拓扑下的查询 API。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonBaseNodes implements BaseRedisNodes {

    /** 连接管理器：维护主从/集群拓扑条目。 */
    ConnectionManager connectionManager;
    /** 异步命令执行器，供 {@link RedisNode} 发起 Redis 命令。 */
    CommandAsyncExecutor commandExecutor;

    /** @param connectionManager 连接拓扑 @param commandExecutor 命令执行器 */
    public RedissonBaseNodes(ConnectionManager connectionManager, CommandAsyncExecutor commandExecutor) {
        this.connectionManager = connectionManager;
        this.commandExecutor = commandExecutor;
    }

    /** 按 {@link NodeType} 收集所有匹配节点（主节点或指定类型的从节点）。 */
    protected <T extends org.redisson.api.redisnode.RedisNode> Collection<T> getNodes(NodeType type) {
        Collection<MasterSlaveEntry> entries = connectionManager.getEntrySet();
        List<T> result = new ArrayList<>();
        for (MasterSlaveEntry masterSlaveEntry : entries) {
            // 主节点：每个 MasterSlaveEntry 对应一个 Master
            if (type == NodeType.MASTER) {
                RedisNode entry = new RedisNode(masterSlaveEntry.getClient(), commandExecutor, NodeType.MASTER);
                result.add((T) entry);
                continue;
            }

            for (ClientConnectionsEntry slaveEntry : masterSlaveEntry.getAllEntries()) {
                // 跳过被 Manager 冻结的连接；仅保留指定类型的从节点
                if (slaveEntry.getFreezeReason() != ClientConnectionsEntry.FreezeReason.MANAGER
                        && slaveEntry.getNodeType() == type) {
                    RedisNode entry = new RedisNode(slaveEntry.getClient(), commandExecutor, slaveEntry.getNodeType());
                    result.add((T) entry);
                }
            }
        }
        return result;
    }

    /** 按地址与节点类型查找单个 {@link RedisNode}，未找到返回 {@code null}。 */
    protected RedisNode getNode(String address, NodeType nodeType) {
        Collection<MasterSlaveEntry> entries = connectionManager.getEntrySet();
        RedisURI addr = new RedisURI(address);
        for (MasterSlaveEntry masterSlaveEntry : entries) {
            if (nodeType == NodeType.MASTER
                    && addr.equals(masterSlaveEntry.getClient().getAddr())) {
                return new RedisNode(masterSlaveEntry.getClient(), commandExecutor, NodeType.MASTER);
            }

            for (ClientConnectionsEntry entry : masterSlaveEntry.getAllEntries()) {
                if (addr.equals(entry.getClient().getAddr())
                        && entry.getFreezeReason() != ClientConnectionsEntry.FreezeReason.MANAGER) {
                    return new RedisNode(entry.getClient(), commandExecutor, entry.getNodeType());
                }
            }
        }
        return null;
    }

    /** 返回当前拓扑下全部活跃节点（主 + 未冻结的从）。 */
    protected List<RedisNode> getNodes() {
        Collection<MasterSlaveEntry> entries = connectionManager.getEntrySet();
        List<RedisNode> result = new ArrayList<>();
        for (MasterSlaveEntry masterSlaveEntry : entries) {
            // 无从节点时仍暴露主节点
            if (masterSlaveEntry.getAllEntries().isEmpty()) {
                RedisNode masterEntry = new RedisNode(masterSlaveEntry.getClient(), commandExecutor, NodeType.MASTER);
                result.add(masterEntry);
            }

            for (ClientConnectionsEntry slaveEntry : masterSlaveEntry.getAllEntries()) {
                if (slaveEntry.getFreezeReason() != ClientConnectionsEntry.FreezeReason.MANAGER) {
                    RedisNode entry = new RedisNode(slaveEntry.getClient(), commandExecutor, slaveEntry.getNodeType());
                    result.add(entry);
                }
            }
        }
        return result;
    }

    /** 并发 PING 所有节点；全部返回 PONG 且在超时内完成则 {@code true}。 */
    @Override
    public boolean pingAll(long timeout, TimeUnit timeUnit) {
        List<RedisNode> clients = getNodes();
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (RedisNode entry : clients) {
            CompletionStage<RedisConnection> f = entry.getClient().connectAsync();
            // 连接成功后发 PING，完成后关闭临时连接
            CompletionStage<Boolean> ff = f.thenCompose(c -> {
                RFuture<String> r = c.async(timeUnit.toMillis(timeout), RedisCommands.PING);
                return r.whenComplete((rr, ex) -> {
                    c.closeAsync();
                });
            }).thenApply("PONG"::equals);
            futures.add(ff.toCompletableFuture());
        }

        CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            f.get(timeout, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            return false;
        }

        // 任一节点 PING 失败则整体 false
        return futures.stream()
                        .map(r -> r.getNow(false))
                        .filter(r -> !r).findAny()
                        .orElse(true);
    }

    /** 默认 1 秒超时的批量 PING。 */
    @Override
    public boolean pingAll() {
        return pingAll(1, TimeUnit.SECONDS);
    }

}
