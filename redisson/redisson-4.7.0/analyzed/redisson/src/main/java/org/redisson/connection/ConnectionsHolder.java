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

import org.redisson.client.RedisClient;
import org.redisson.client.RedisConnection;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.misc.AsyncSemaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

/**
 * 通用 Redis 连接池持有者，管理空闲/活跃连接与并发借还。
 * <p>
 * 通过 {@link AsyncSemaphore} 限制最大连接数；
 * 支持普通命令连接与 Pub/Sub 连接两种模式（changeUsage 标志）。
 *
 * @author Nikita Koksharov
 *
 * @param <T> 连接类型（{@link RedisConnection} 或其子类）
 */
public class ConnectionsHolder<T extends RedisConnection> {

    final Logger log = LoggerFactory.getLogger(getClass());

    /** 池中所有连接（含借出中的）。 */
    private final Queue<T> allConnections = new ConcurrentLinkedQueue<>();
    /** 空闲可借连接双端队列。 */
    private final Deque<T> freeConnections = new ConcurrentLinkedDeque<>();
    /** 空闲连接计数信号量（上限 = poolMaxSize）。 */
    private final AsyncSemaphore freeConnectionsCounter;

    /** 关联的 Redis 客户端。 */
    private final RedisClient client;

    /** 创建新连接的异步回调。 */
    private final Function<RedisClient, CompletionStage<T>> connectionCallback;

    /** 服务管理器（事件循环、定时器等）。 */
    private final ServiceManager serviceManager;

    /** 是否在借还时增减连接的 usage 计数。 */

    /** 构造连接池，以 poolMaxSize 初始化信号量。 */
    public ConnectionsHolder(RedisClient client, int poolMaxSize,
                             Function<RedisClient, CompletionStage<T>> connectionCallback,
                             ServiceManager serviceManager, boolean changeUsage) {
        this.freeConnectionsCounter = new AsyncSemaphore(poolMaxSize, serviceManager.getGroup());
        this.client = client;
        this.connectionCallback = connectionCallback;
        this.serviceManager = serviceManager;
        this.changeUsage = changeUsage;
    }

    /** 从空闲队列与全量集合中移除连接。 */
    public <R extends RedisConnection> boolean remove(R connection) {
        if (freeConnections.remove(connection)) {
            return allConnections.remove(connection);
        }
        return false;
    }

    public Queue<T> getFreeConnections() {
        return freeConnections;
    }

    public AsyncSemaphore getFreeConnectionsCounter() {
        return freeConnectionsCounter;
    }

    /** 异步获取连接许可（信号量 acquire）。 */
    protected CompletableFuture<Void> acquireConnection() {
        return freeConnectionsCounter.acquire();
    }
    
    private void releaseConnection() {
        freeConnectionsCounter.release();
    }

    private void addConnection(T conn) {
        conn.setLastUsageTime(System.nanoTime());
        freeConnections.add(conn);
    }

    /** 从空闲队列轮询活跃连接，跳过 inactive 通道。 */
    private T pollConnection(RedisCommand<?> command) {
        int size = freeConnections.size();
        for (int i = 0; i < size; i++) {
            T conn = freeConnections.poll();
            if (conn == null) {
                return null;
            }
            if (conn.isActive()) {
                if (i > 0) {
                    log.debug("skipped connections with inactive channel: {}", i);
                }
                if (changeUsage) {
                    conn.incUsage();
                }
                return conn;
            }

            freeConnections.addLast(conn);
        }
        return null;
    }

    private void releaseConnection(T connection) {
        if (connection.isClosed()) {
            return;
        }

        if (client != null && client != connection.getRedisClient()) {
            connection.closeAsync();
            return;
        }

        connection.setLastUsageTime(System.nanoTime());
        if (connection.isActive()) {
            freeConnections.addFirst(connection);
        } else {
            freeConnections.addLast(connection);
        }
        if (changeUsage) {
            connection.decUsage();
        }
    }

    public Queue<T> getAllConnections() {
        return allConnections;
    }

    /** 顺序创建 minimumIdleSize 条连接并加入空闲池。 */
    public CompletableFuture<Void> initConnections(int minimumIdleSize) {
        if (minimumIdleSize == 0) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> f = createConnection(minimumIdleSize, 1);
        for (int i = 2; i <= minimumIdleSize; i++) {
            int k = i;
            f = f.thenCompose(r -> createConnection(minimumIdleSize, k));
        }
        return f.thenAccept(r -> {
            log.info("{} connections initialized for {}", minimumIdleSize, client.getAddr());
        });
    }

    private CompletableFuture<Void> createConnection(int minimumIdleSize, int index) {
        CompletableFuture<Void> f = acquireConnection();
        return f.thenCompose(r -> {
            CompletableFuture<T> promise = new CompletableFuture<>();
            createConnection(promise);
            return promise.handle((conn, e) -> {
                if (e == null) {
                    if (changeUsage) {
                        conn.decUsage();
                    }
                    addConnection(conn);
                    // 仅在成功时 release；失败路径已在 createConnection(promise) 中 release，
                    // 此处无条件 release 会导致初始化失败时双重释放，使计数器超过池上限
                    releaseConnection();
                }

                if (e != null) {
                    for (RedisConnection connection : getAllConnections()) {
                        if (!connection.isClosed()) {
                            connection.closeAsync();
                        }
                    }
                    getAllConnections().clear();

                    int totalInitializedConnections = index - 1;
                    String errorMsg;
                    if (totalInitializedConnections == 0) {
                        errorMsg = "Unable to connect to Redis server: " + client.getAddr();
                    } else {
                        errorMsg = "Unable to init enough connections amount! Only " + totalInitializedConnections
                                + " of " + minimumIdleSize + " were initialized. Redis server: " + client.getAddr();
                    }
                    Exception cause = new RedisConnectionException(errorMsg, e);
                    throw new CompletionException(cause);
                }
                return null;
            });
        });
    }

    private void createConnection(CompletableFuture<T> promise) {
        CompletionStage<T> connFuture = connectionCallback.apply(client);
        connFuture.whenComplete((conn, e) -> {
            if (e != null) {
                releaseConnection();

                promise.completeExceptionally(e);
                return;
            }

            log.debug("new connection created: {}", conn);

            allConnections.add(conn);

            if (changeUsage) {
                promise.thenApply(c -> c.incUsage());
            }
            connectedSuccessful(promise, conn);
        });
    }

    private void connectedSuccessful(CompletableFuture<T> promise, T conn) {
        if (!promise.complete(conn)) {
            releaseConnection(conn);
            releaseConnection();
        }
    }

    /** 异步借出连接：优先复用空闲连接，否则新建。 */
    public CompletableFuture<T> acquireConnection(RedisCommand<?> command) {
        CompletableFuture<T> result = new CompletableFuture<>();

        CompletableFuture<Void> f = acquireConnection();
        f.thenAccept(r -> {
            connectTo(result, command);
        });
        result.whenComplete((r, e) -> {
            if (e != null) {
                f.completeExceptionally(e);
            }
        });
        return result;
    }

    private void connectTo(CompletableFuture<T> promise, RedisCommand<?> command) {
        if (promise.isDone()) {
            releaseConnection();
            return;
        }

        T conn = pollConnection(command);
        if (conn != null) {
            connectedSuccessful(promise, conn);
            return;
        }

        createConnection(promise);
    }

    @Override
    public String toString() {
        return "ConnectionsHolder{" +
                "allConnections=" + allConnections.size() +
                ", freeConnections=" + freeConnections.size() +
                ", freeConnectionsCounter=" + freeConnectionsCounter +
                '}';
    }

    /** 归还连接；若条目已冻结则关闭连接而非回池。 */
    public void releaseConnection(ClientConnectionsEntry entry, T connection) {
        if (entry.isFreezed()) {
            connection.closeAsync();
            getAllConnections().remove(connection);
        } else {
            releaseConnection(connection);
        }
        releaseConnection();
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }
}

