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

import org.redisson.api.RFuture;
import org.redisson.client.protocol.Time;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 节点异步基础 API。
 * <p>
 * 以 {@link RFuture} 形式暴露 {@link RedisNode} 中的监控、配置与持久化操作。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisNodeAsync {

    /**
     * 异步返回 Redis 内存使用统计信息。
     *
     * @return 统计信息键值对
     */
    RFuture<Map<String, String>> getMemoryStatisticsAsync();

    /**
     * 异步返回 Redis 服务器当前时间（秒级）。
     *
     * @return 服务器时间
     */
    RFuture<Time> timeAsync();

    /**
     * 异步向 Redis 节点发送 PING 命令。
     * 默认超时为 1000 毫秒。
     *
     * @return 收到 "PONG" 回复时为 <code>true</code>，否则为 <code>false</code>
     */
    RFuture<Boolean> pingAsync();

    /**
     * 异步以指定超时向 Redis 节点发送 PING 命令。
     *
     * @param timeout 超时时间
     * @param timeUnit 超时单位
     * @return 收到 "PONG" 回复时为 <code>true</code>，否则为 <code>false</code>
     */
    RFuture<Boolean> pingAsync(long timeout, TimeUnit timeUnit);

    /**
     * 异步返回 Redis 节点的 INFO 信息。
     *
     * @param section 信息分区
     * @return 信息键值对
     */
    RFuture<Map<String, String>> infoAsync(RedisNode.InfoSection section);

    /**
     * 异步读取 Redis 配置参数的值。
     *
     * @param parameter 参数名
     * @return 参数值
     */
    RFuture<Map<String, String>> getConfigAsync(String parameter);

    /**
     * 异步设置 Redis 配置参数的值。
     *
     * @param parameter 参数名
     * @param value 参数值
     * @return void
     */
    RFuture<Void> setConfigAsync(String parameter, String value);

    /**
     * 异步在后台执行 RDB 快照保存（BGSAVE）。
     *
     */
    RFuture<Void> bgSaveAsync();

    /**
     * 异步在后台调度 RDB 快照保存。
     * 若当前正在进行 AOF 重写，则会在重写完成后执行 BGSAVE。
     *
     */
    RFuture<Void> scheduleBgSaveAsync();

    /**
     * 异步同步阻塞保存 Redis 数据库到磁盘（SAVE）。
     *
     */
    RFuture<Void> saveAsync();

    /**
     * 异步返回最近一次成功完成 RDB 保存的时间。
     *
     * @return 上次保存时间
     */
    RFuture<Instant> getLastSaveTimeAsync();

    /**
     * 异步在后台执行 AOF 重写（BGREWRITEAOF）。
     * 仅当没有其他持久化后台进程运行时才启动；
     * 失败时不会丢失已有数据。
     * <p>
     *
     */
    RFuture<Void> bgRewriteAOFAsync();

    /**
     * 异步返回该 Redis 节点当前存储的键数量。
     *
     * @return 键数量
     */
    RFuture<Long> sizeAsync();

}
