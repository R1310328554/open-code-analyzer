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
package org.redisson.connection;

import io.netty.channel.ChannelFuture;
import org.redisson.api.NodeType;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisConnection;
import org.redisson.client.RedisPubSubConnection;
import org.redisson.client.protocol.CommandData;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.misc.WrappedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 单个 Redis 节点（{@link RedisClient}）的连接池入口。
 * <p>
 * 管理普通命令连接池、Pub/Sub 连接池及被追踪连接；
 * 处理节点下线、冻结及阻塞命令重挂接逻辑。
 *
 * @author Nikita Koksharov
 *
 */
public class ClientConnectionsEntry {

    final Logger log = LoggerFactory.getLogger(getClass());

    /** 普通 Redis 命令连接池。 */
    private final ConnectionsHolder<RedisConnection> connectionsHolder;

    /** Pub/Sub 专用连接池。 */
    private final ConnectionsHolder<RedisPubSubConnection> pubSubConnectionsHolder;

    /** 被客户端追踪（CLIENT TRACKING）的连接持有者。 */
    private final TrackedConnectionsHolder trackedConnectionsHolder;

    /** 连接池冻结原因：管理器主动冻结或重连流程。 */
    public enum FreezeReason {MANAGER, RECONNECT}

    /** 当前冻结原因，非 null 表示连接池已冻结。 */
    private volatile FreezeReason freezeReason;
    /** 关联的 Redis 客户端实例。 */
    final RedisClient client;

    /** 节点类型（主/从/哨兵等）。 */
    private final NodeType nodeType;
    /** 空闲连接回收监视器。 */
    private final IdleConnectionWatcher idleConnectionWatcher;
    /** 所属连接管理器。 */
    private final ConnectionManager connectionManager;

    /** 连接池是否已完成初始化。 */
    private volatile boolean initialized = false;

    /** 连接池操作互斥锁。 */
    private final WrappedLock lock = new WrappedLock();

    /** 连接实例到其所属 ConnectionsHolder 的映射（归还连接时用）。 */
    private final Map<RedisConnection, ConnectionsHolder<?>> connection2holder = new ConcurrentHashMap<>();

    /**
     * 构造节点连接池入口，注册空闲连接监视。
     *
     * @param client 目标 Redis 客户端
     * @param poolMinSize 普通连接池最小空闲数
     * @param poolMaxSize 普通连接池最大连接数
     * @param connectionManager 连接管理器
     * @param nodeType 节点类型
     * @param config 主从服务器配置
     */
        this.client = client;
        this.connectionsHolder = new ConnectionsHolder<>(client, poolMaxSize, r -> r.connectAsync(),
                connectionManager.getServiceManager(), true);
        this.idleConnectionWatcher = connectionManager.getServiceManager().getConnectionWatcher();
        this.connectionManager = connectionManager;
        this.nodeType = nodeType;
        this.pubSubConnectionsHolder = new ConnectionsHolder<>(client, config.getSubscriptionConnectionPoolSize(),
                r -> r.connectPubSubAsync(), connectionManager.getServiceManager(), false);

        if (config.getSubscriptionConnectionPoolSize() > 0) {
            idleConnectionWatcher.add(this, config.getSubscriptionConnectionMinimumIdleSize(),
                                                config.getSubscriptionConnectionPoolSize(), pubSubConnectionsHolder);
        }
        idleConnectionWatcher.add(this, poolMinSize, poolMaxSize, connectionsHolder);

        this.trackedConnectionsHolder = new TrackedConnectionsHolder(connectionsHolder);
    }

    /** 异步预热普通命令连接池至指定最小空闲数。 */
    public CompletableFuture<Void> initConnections(int minimumIdleSize) {
        return connectionsHolder.initConnections(minimumIdleSize);
    }

    /** 异步预热 Pub/Sub 连接池。 */
    public CompletableFuture<Void> initPubSubConnections(int minimumIdleSize) {
        return pubSubConnectionsHolder.initConnections(minimumIdleSize);
    }

    /** 连接池是否已初始化。 */
    public boolean isInitialized() {
        return this.initialized;
    }

    /** 设置初始化标志。 */
    public void setInitialized(boolean isInited) {
        this.initialized = isInited;
    }
    
    /** 返回节点类型。 */
    public NodeType getNodeType() {
        return nodeType;
    }

    /** 异步关闭：移除空闲监视并关闭 RedisClient。 */
    public CompletableFuture<Void> shutdownAsync() {
        idleConnectionWatcher.remove(this);
        return client.shutdownAsync().toCompletableFuture();
    }

    /** 返回关联的 RedisClient。 */
    public RedisClient getClient() {
        return client;
    }

    /** 连接池是否处于冻结状态。 */
    public boolean isFreezed() {
        return freezeReason != null;
    }

    /** 设置冻结原因；冻结时重置 initialized 标志。 */
    public void setFreezeReason(FreezeReason freezeReason) {
        this.freezeReason = freezeReason;
        if (freezeReason != null) {
            this.initialized = false;
        }
    }

    public FreezeReason getFreezeReason() {
        return freezeReason;
    }

    public WrappedLock getLock() {
        return lock;
    }

    /** 节点下线时关闭 Pub/Sub 连接并触发订阅服务重挂接。 */
    public void reattachPubSub() {
        pubSubConnectionsHolder.getFreeConnectionsCounter().removeListeners();

        for (RedisPubSubConnection connection : pubSubConnectionsHolder.getAllConnections()) {
            connection.closeAsync();
            connectionManager.getSubscribeService().reattachPubSub(connection);
        }

        log.debug("{} PubSub connections to {} have been closed", pubSubConnectionsHolder.getAllConnections().size(), client.getAddr());

        pubSubConnectionsHolder.getFreeConnections().clear();
        pubSubConnectionsHolder.getAllConnections().clear();
    }

    /** 节点下线：关闭普通连接并重挂接 Pub/Sub。 */
    public void nodeDown() {
        nodeDown(connectionsHolder);
        reattachPubSub();
    }

    /** 关闭指定连接池内所有连接并重挂接阻塞命令。 */
    protected final void nodeDown(ConnectionsHolder<RedisConnection> connectionsHolder) {
        connectionsHolder.getFreeConnectionsCounter().removeListeners();

        for (RedisConnection connection : connectionsHolder.getAllConnections()) {
            connection.closeAsync();
            reattachBlockingQueue(connection.getCurrentCommand());
        }

        log.debug("{} connections to {} have been closed", connectionsHolder.getAllConnections().size(), client.getAddr());

        connectionsHolder.getFreeConnections().clear();
        connectionsHolder.getAllConnections().clear();
    }

    /** 将未完成的阻塞命令重新发送到新连接（节点切换/故障转移场景）。 */
    void reattachBlockingQueue(CommandData<?, ?> commandData) {
        if (commandData == null
                || !commandData.isBlockingCommand()
                || commandData.getPromise().isDone()) {
            return;
        }

        String key = getKey(commandData);

        MasterSlaveEntry entry = connectionManager.getEntry(key);
        if (entry == null) {
            log.debug("Unable to get entry for {} during blocking command reattach {}", key, commandData);
            connectionManager.getServiceManager().newTimeout(timeout ->
                    reattachBlockingQueue(commandData), 1, TimeUnit.SECONDS);
            return;
        }

        CompletableFuture<RedisConnection> newConnectionFuture = entry.connectionWriteOp(commandData.getCommand());
        newConnectionFuture.whenComplete((newConnection, e) -> {
            if (e != null) {
                log.debug("Unable to acquire connection during blocking command reattach {}", commandData, e);
                connectionManager.getServiceManager().newTimeout(timeout ->
                        reattachBlockingQueue(commandData), 1, TimeUnit.SECONDS);
                return;
            }

            commandData.getPromise().whenComplete((r, ex) -> {
                entry.releaseWrite(newConnection);
            });

            ChannelFuture channelFuture = newConnection.send(commandData);
            channelFuture.addListener(future -> {
                if (!future.isSuccess()) {
                    log.debug("Unable to send a command during blocking command reattach {}", commandData, future.cause());
                    connectionManager.getServiceManager().newTimeout(timeout ->
                            reattachBlockingQueue(commandData), 1, TimeUnit.SECONDS);
                    return;
                }
                log.info("command '{}' has been resent to '{}'", commandData, newConnection.getRedisClient());
            });
        });
    }

    /** 从命令参数提取路由 key（支持 STREAMS 与普通命令）。 */
    private String getKey(CommandData<?, ?> commandData) {
        String key = null;
        for (int i = 0; i < commandData.getParams().length; i++) {
            Object param = commandData.getParams()[i];
            if ("STREAMS".equals(param)) {
                Object k = commandData.getParams()[i+1];
                if (k instanceof byte[]) {
                    key = new String((byte[]) k, StandardCharsets.UTF_8);
                } else {
                    key = (String) k;
                }
                break;
            }
        }
        if (key == null) {
            Object k = commandData.getParams()[0];
            if (k instanceof byte[]) {
                key = new String((byte[]) k, StandardCharsets.UTF_8);
            } else {
                key = (String) k;
            }
        }
        return key;
    }

    public ConnectionsHolder<RedisConnection> getConnectionsHolder() {
        return connectionsHolder;
    }

    public TrackedConnectionsHolder getTrackedConnectionsHolder() {
        return trackedConnectionsHolder;
    }

    public ConnectionsHolder<RedisPubSubConnection> getPubSubConnectionsHolder() {
        return pubSubConnectionsHolder;
    }

    public void addHandler(RedisConnection connection, ConnectionsHolder<?> handler) {
        connection2holder.put(connection, handler);
    }

    /** 将借出的连接归还到对应连接池。 */
    public <T extends RedisConnection> void returnConnection(T connection) {
        ConnectionsHolder<T> handler;
        if (connection.getUsage() > 1) {
            handler = (ConnectionsHolder<T>) connection2holder.get(connection);
        } else {
            handler = (ConnectionsHolder<T>) connection2holder.remove(connection);
        }
        if (handler != null) {
            handler.releaseConnection(this, connection);
        }
    }

    @Override
    public String toString() {
        return "ClientConnectionsEntry{" +
                "connectionsHolder=" + connectionsHolder +
                ", pubSubConnectionsHolder=" + pubSubConnectionsHolder +
                ", freezeReason=" + freezeReason +
                ", client=" + client +
                ", nodeType=" + nodeType +
                ", initialized=" + initialized +
                '}';
    }
}

