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
package org.redisson.api.redisnode;

import org.redisson.client.protocol.Time;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 节点基础 API。
 * <p>
 * 提供内存统计、服务器时间、连通性检测、INFO 查询、运行时配置及持久化等通用运维能力，
 * 适用于集群、主从、哨兵及单机等多种部署形态下的单个节点。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisNode {

    /**
     * 返回 Redis 内存使用统计信息。
     *
     * @return 统计信息键值对
     */
    Map<String, String> getMemoryStatistics();

    /**
     * 返回 Redis 服务器当前时间（秒级）。
     *
     * @return 服务器时间
     */
    Time time();

    /**
     * 获取 Redis 节点网络地址。
     *
     * @return 节点地址
     */
    InetSocketAddress getAddr();

    /**
     * 向 Redis 节点发送 PING 命令。
     * 默认超时为 1000 毫秒。
     *
     * @return 收到 "PONG" 回复时为 <code>true</code>，否则为 <code>false</code>
     */
    boolean ping();

    /**
     * 以指定超时向 Redis 节点发送 PING 命令。
     *
     * @param timeout 超时时间
     * @param timeUnit 超时单位
     * @return 收到 "PONG" 回复时为 <code>true</code>，否则为 <code>false</code>
     */
    boolean ping(long timeout, TimeUnit timeUnit);

    /** INFO 命令可查询的信息分区 */
    enum InfoSection {ALL, DEFAULT, SERVER, CLIENTS, MEMORY, PERSISTENCE, STATS, REPLICATION, CPU, COMMANDSTATS, CLUSTER, KEYSPACE}

    /**
     * 返回 Redis 节点的 INFO 信息。
     *
     * @param section 信息分区
     * @return 信息键值对
     */
    Map<String, String> info(RedisNode.InfoSection section);

    /**
     * 读取 Redis 配置参数的值。
     *
     * @param parameter 参数名
     * @return 参数值
     */
    Map<String, String> getConfig(String parameter);

    /**
     * 设置 Redis 配置参数的值。
     *
     * @param parameter 参数名
     * @param value 参数值
     */
    void setConfig(String parameter, String value);

    /**
     * 在后台异步执行 RDB 快照保存（BGSAVE）。
     *
     */
    void bgSave();

    /**
     * 在后台调度 RDB 快照保存。
     * 若当前正在进行 AOF 重写，则会在重写完成后执行 BGSAVE。
     *
     */
    void scheduleBgSave();

    /**
     * 同步阻塞保存 Redis 数据库到磁盘（SAVE）。
     *
     */
    void save();

    /**
     * 返回最近一次成功完成 RDB 保存的时间。
     *
     * @return 上次保存时间
     */
    Instant getLastSaveTime();

    /**
     * 在后台执行 AOF 重写（BGREWRITEAOF）。
     * 仅当没有其他持久化后台进程运行时才启动；
     * 失败时不会丢失已有数据。
     * <p>
     *
     */
    void bgRewriteAOF();

    /**
     * 返回该 Redis 节点当前存储的键数量。
     *
     * @return 键数量
     */
    long size();

}
