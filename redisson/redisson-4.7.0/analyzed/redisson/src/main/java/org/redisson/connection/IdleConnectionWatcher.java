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
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import org.redisson.client.RedisConnection;
import org.redisson.client.RedisPubSubConnection;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.misc.AsyncSemaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 空闲连接监视器：周期性扫描连接池，关闭超时未使用的空闲连接。
 * <p>
 * 由 {@link ServiceManager} 创建并在 {@link ClientConnectionsEntry} 注册各连接池条目；
 * 关闭前校验最小连接数，且跳过仍持有 Pub/Sub 订阅的连接。
 */
public class IdleConnectionWatcher {

    /** 日志记录器。 */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** 单个连接池的空闲回收配置与状态快照。 */
    public static class Entry {

        /** 该池允许保留的最小连接数（含借出）。 */
        private final int minimumAmount;
        /** 该池最大连接数上限。 */
        private final int maximumAmount;

        /** 关联的连接池持有者。 */
        private final ConnectionsHolder<? extends RedisConnection> holder;
        /** 空闲连接计数信号量。 */
        private final AsyncSemaphore freeConnectionsCounter;
        /** 当前空闲连接集合（只读视图）。 */
        private final Collection<? extends RedisConnection> connections;

        /** 从连接池持有者构造监视条目。 */
        public Entry(int minimumAmount, int maximumAmount, ConnectionsHolder<? extends RedisConnection> holder) {
            super();
            this.minimumAmount = minimumAmount;
            this.maximumAmount = maximumAmount;
            this.connections = holder.getFreeConnections();
            this.freeConnectionsCounter = holder.getFreeConnectionsCounter();
            this.holder = holder;
        }

    };

    /** 节点连接池 → 监视条目列表（普通池与 Pub/Sub 池各一条）。 */
    private final Map<ClientConnectionsEntry, List<Entry>> entries = new ConcurrentHashMap<>();
    /** 定时扫描任务的 Future，用于 stop() 取消。 */
    private final ScheduledFuture<?> monitorFuture;

    /**
     * 启动空闲连接定时扫描。
     * <p>
     * 扫描间隔与超时阈值均为 {@code idleConnectionTimeout}。
     */
    public IdleConnectionWatcher(EventLoopGroup group, MasterSlaveServersConfig config) {
        monitorFuture = group.scheduleWithFixedDelay(() -> {
            long currTime = System.nanoTime();
            for (Entry entry : entries.values().stream().flatMap(m -> m.stream()).collect(Collectors.toList())) {
                if (!validateAmount(entry)) {
                    continue;
                }

                for (RedisConnection c : entry.connections) {
                    long timeInPool = TimeUnit.NANOSECONDS.toMillis(currTime - c.getLastUsageTime());

                    if (c instanceof RedisPubSubConnection
                            && (!((RedisPubSubConnection) c).getChannels().isEmpty()
                                    || !((RedisPubSubConnection) c).getPatternChannels().isEmpty()
                                        || !((RedisPubSubConnection) c).getShardedChannels().isEmpty())) {
                        continue;
                    }

                    if (timeInPool > config.getIdleConnectionTimeout()
                            && validateAmount(entry)
                                && entry.holder.remove(c)) {
                        ChannelFuture future = c.closeIdleAsync();
                        future.addListener((FutureListener<Void>) f ->
                                log.debug("Connection {} has been closed due to idle timeout. Not used for {} ms", c.getChannel(), timeInPool));
                    }
                }
            }
        }, config.getIdleConnectionTimeout(), config.getIdleConnectionTimeout(), TimeUnit.MILLISECONDS);
    }

    /** 关闭空闲连接后仍不低于最小连接数时才允许回收。 */
    private boolean validateAmount(Entry entry) {
        return entry.maximumAmount - entry.freeConnectionsCounter.getCounter() + entry.connections.size() > entry.minimumAmount;
    }

    /** 节点下线时移除其监视条目。 */
    public void remove(ClientConnectionsEntry entry) {
        entries.remove(entry);
    }

    /** 注册连接池到空闲监视（同一节点可注册多个 holder）。 */
    public void add(ClientConnectionsEntry entry, int minimumAmount, int maximumAmount, ConnectionsHolder<? extends RedisConnection> holder) {
        List<Entry> list = entries.computeIfAbsent(entry, k -> new ArrayList<>(2));
        list.add(new Entry(minimumAmount, maximumAmount, holder));
    }
    
    /** 取消定时扫描任务。 */
    public void stop() {
        if (monitorFuture != null) {
            monitorFuture.cancel(true);
        }
    }

}
