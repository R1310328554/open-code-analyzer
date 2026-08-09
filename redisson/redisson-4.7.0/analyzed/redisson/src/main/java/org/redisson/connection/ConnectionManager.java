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

import io.netty.buffer.ByteBuf;
import org.redisson.api.NodeType;
import org.redisson.client.RedisClient;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.*;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.redisson.misc.RedisURI;
import org.redisson.pubsub.PublishSubscribeService;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 连接管理器核心接口。
 * <p>
 * 负责槽位计算、主从条目查找、客户端创建及生命周期管理；
 * 具体实现包括 {@link MasterSlaveConnectionManager}、{@link ClusterConnectionManager} 等。
 *
 * @author Nikita Koksharov
 *
 */
public interface ConnectionManager {

    /** 建立与 Redis 集群/主从拓扑的连接。 */
    void connect();

    /** 返回 Pub/Sub 订阅服务。 */
    PublishSubscribeService getSubscribeService();

    /** 返回最近一次成功通信的集群节点 URI。 */
    RedisURI getLastClusterNode();

    /** 计算字符串 key 的集群槽号。 */
    int calcSlot(String key);

    /** 计算 ByteBuf key 的集群槽号。 */
    int calcSlot(ByteBuf key);

    /** 计算字节数组 key 的集群槽号。 */
    int calcSlot(byte[] key);

    /** 返回所有主从条目集合。 */
    Collection<MasterSlaveEntry> getEntrySet();

    /**
     * 以轮询策略返回下一个主节点条目。
     * 单节点模式返回唯一主节点；集群模式在所有 master 间均衡分配。
     */
    MasterSlaveEntry getNextEntry();

    /** 按 key 名称查找主从条目。 */
    MasterSlaveEntry getEntry(String name);

    /** 按槽号查找主从条目。 */
    MasterSlaveEntry getEntry(int slot);

    /** 按槽号查找写操作目标条目。 */
    MasterSlaveEntry getWriteEntry(int slot);

    /** 按槽号查找读操作目标条目（受 ReadMode 影响）。 */
    MasterSlaveEntry getReadEntry(int slot);

    /** 按网络地址查找条目。 */
    MasterSlaveEntry getEntry(InetSocketAddress address);

    /** 按 RedisURI 查找条目。 */
    MasterSlaveEntry getEntry(RedisURI addr);

    /** 创建 RedisClient（指定地址与 SSL 主机名）。 */
    RedisClient createClient(NodeType type, InetSocketAddress address, RedisURI uri, String sslHostname);

    /** 创建 RedisClient（从 RedisURI 解析地址）。 */
    RedisClient createClient(NodeType type, RedisURI address, String sslHostname);

    /** 按 RedisClient 实例查找主从条目。 */
    MasterSlaveEntry getEntry(RedisClient redisClient);

    /** 同步关闭连接管理器。 */
    void shutdown();

    /** 带静默期与超时的同步关闭。 */
    void shutdown(long quietPeriod, long timeout, TimeUnit unit);

    /** 异步关闭连接管理器。 */
    CompletionStage<Void> shutdownAsync(long quietPeriod, long timeout, TimeUnit unit);

    /** 返回底层 ServiceManager。 */
    ServiceManager getServiceManager();

    /** 创建命令异步执行器（Live Object 等场景）。 */
    CommandAsyncExecutor createCommandExecutor(RedissonObjectBuilder objectBuilder,
                                               RedissonObjectBuilder.ReferenceType referenceType);

    /** 根据 {@link Config} 类型创建对应的 ConnectionManager 实现并连接。 */
    static ConnectionManager create(Config configCopy) {
        BaseConfig<?> cfg = ConfigSupport.getConfig(configCopy);
        ConnectionManager cm = null;
        if (cfg instanceof MasterSlaveServersConfig) {
            cm = new MasterSlaveConnectionManager((MasterSlaveServersConfig) cfg, configCopy);
        } else if (cfg instanceof SingleServerConfig) {
            cm = new SingleConnectionManager((SingleServerConfig) cfg, configCopy);
        } else if (cfg instanceof SentinelServersConfig) {
            cm = new SentinelConnectionManager((SentinelServersConfig) cfg, configCopy);
        } else if (cfg instanceof ClusterServersConfig) {
            cm = new ClusterConnectionManager((ClusterServersConfig) cfg, configCopy);
        } else if (cfg instanceof ReplicatedServersConfig) {
            cm = new ReplicatedConnectionManager((ReplicatedServersConfig) cfg, configCopy);
        }

        if (cm == null) {
            throw new IllegalArgumentException("server(s) address(es) not defined!");
        }
        if (!configCopy.isLazyInitialization()) {
            cm.connect();
        }
        return cm;
    }

}
