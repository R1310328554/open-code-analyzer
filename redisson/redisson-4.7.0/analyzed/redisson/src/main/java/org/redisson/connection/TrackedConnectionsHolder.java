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

import org.redisson.api.RFuture;
import org.redisson.client.RedisConnection;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.misc.AsyncSemaphore;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 客户端追踪（CLIENT TRACKING）专用连接持有者装饰器。
 * <p>
 * 复用底层 {@link ConnectionsHolder} 的连接池，但 acquire 时共享同一 Future；
 * {@link #reset()} 关闭 CLIENT TRACKING 并归还连接。
 *
 * @author Nikita Koksharov
 *
 */
public class TrackedConnectionsHolder extends ConnectionsHolder<RedisConnection> {

    /** 被装饰的底层连接池。 */
    private final ConnectionsHolder<RedisConnection> holder;

    /** 当前追踪连接的 acquire Future（全局共享）。 */
    private final AtomicReference<CompletableFuture<RedisConnection>> connectionFuture = new AtomicReference<>();

    /** 追踪连接所属节点条目（release 时记录）。 */
    private volatile ClientConnectionsEntry entry;

    /** 追踪连接引用计数。 */
    private final AtomicInteger usage = new AtomicInteger();

    /** 包装现有连接池，不支持独立 initConnections。 */
    public TrackedConnectionsHolder(ConnectionsHolder<RedisConnection> holder) {
        super(null, 0, null, holder.getServiceManager(), false);
        this.holder = holder;
    }

    @Override
    public <R extends RedisConnection> boolean remove(R connection) {
        return holder.remove(connection);
    }

    @Override
    public Queue<RedisConnection> getFreeConnections() {
        return holder.getFreeConnections();
    }

    @Override
    public AsyncSemaphore getFreeConnectionsCounter() {
        return holder.getFreeConnectionsCounter();
    }

    @Override
    public Queue<RedisConnection> getAllConnections() {
        return holder.getAllConnections();
    }

    @Override
    public CompletableFuture<Void> initConnections(int minimumIdleSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    /** 获取追踪连接：首次 acquire 创建共享 Future，后续复用。 */
    public CompletableFuture<RedisConnection> acquireConnection(RedisCommand<?> command) {
        CompletableFuture<RedisConnection> newFuture = new CompletableFuture<>();
        if (!connectionFuture.compareAndSet(null, newFuture)) {
            return connectionFuture.get();
        }

        CompletableFuture<RedisConnection> f = holder.acquireConnection(command);
        newFuture.whenComplete((r, e) -> {
            if (e != null) {
                f.completeExceptionally(e);
                connectionFuture.set(null);
            }
        });
        f.whenComplete((r, e) -> {
            if (e != null) {
                newFuture.completeExceptionally(e);
                connectionFuture.set(null);
                return;
            }

            newFuture.complete(r);
        });
        return newFuture;
    }

    @Override
    public void releaseConnection(ClientConnectionsEntry entry, RedisConnection connection) {
        this.entry = entry;
        // 追踪模式下暂不立即归还，由 reset() 统一处理
        //holder.releaseConnection(entry, connection);
    }

    /** 发送 CLIENT TRACKING OFF 并归还追踪连接到连接池。 */
    public void reset() {
        if (connectionFuture.get() != null
                && connectionFuture.get().getNow(null) != null) {
            RedisConnection c = connectionFuture.get().getNow(null);
            RFuture<Void> f = c.async(RedisCommands.CLIENT_TRACKING, "OFF");
            f.whenComplete((res, ex) -> {
                holder.releaseConnection(entry, connectionFuture.get().getNow(null));
            });
        }
    }

    /** 增加追踪连接引用计数。 */
    public void incUsage() {
        usage.incrementAndGet();
    }

    /** 减少追踪连接引用计数。 */
    public int decUsage() {
        return usage.decrementAndGet();
    }

}
