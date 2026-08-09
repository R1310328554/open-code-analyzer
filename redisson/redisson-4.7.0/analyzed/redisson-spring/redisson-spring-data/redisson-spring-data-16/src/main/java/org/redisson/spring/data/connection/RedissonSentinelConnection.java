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
package org.redisson.spring.data.connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.redisson.client.RedisConnection;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.springframework.data.redis.connection.NamedNode;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.data.redis.connection.convert.Converters;

/**
 * Spring Data Redis {@link RedisSentinelConnection} 的 Redisson 实现。
 * <p>通过底层 {@link RedisConnection} 同步发送 Sentinel 管理命令
（failover、monitor、masters/slaves 查询等）。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonSentinelConnection implements RedisSentinelConnection {

    private final RedisConnection connection;
    
    /** 绑定已连通的 Sentinel {@link RedisConnection}。 */
    public RedissonSentinelConnection(RedisConnection connection) {
        this.connection = connection;
    }

    /** 对指定 master 执行 {@code SENTINEL FAILOVER}。 */
    @Override
    public void failover(NamedNode master) {
        connection.sync(RedisCommands.SENTINEL_FAILOVER, master.getName());
    }

    private static List<RedisServer> toRedisServersList(List<Map<String, String>> source) {
        List<RedisServer> servers = new ArrayList<RedisServer>(source.size());
        for (Map<String, String> info : source) {
            servers.add(RedisServer.newServerFrom(Converters.toProperties(info)));
        }
        return servers;
    }
    
    /** 查询所有被监控的 master 并转为 {@link RedisServer} 列表。 */
    @Override
    public Collection<RedisServer> masters() {
        List<Map<String, String>> masters = connection.sync(StringCodec.INSTANCE, RedisCommands.SENTINEL_MASTERS);
        return toRedisServersList(masters);
    }

    /** 查询指定 master 下的 replica 节点。 */
    @Override
    public Collection<RedisServer> slaves(NamedNode master) {
        List<Map<String, String>> slaves = connection.sync(StringCodec.INSTANCE, RedisCommands.SENTINEL_SLAVES, master.getName());
        return toRedisServersList(slaves);
    }

    @Override
    public void remove(NamedNode master) {
        connection.sync(RedisCommands.SENTINEL_REMOVE, master.getName());
    }

    /** 向 Sentinel 注册新的 master 监控（host/port/quorum）。 */
    @Override
    public void monitor(RedisServer master) {
        connection.sync(RedisCommands.SENTINEL_MONITOR, master.getName(), master.getHost(), 
                                    master.getPort().intValue(), master.getQuorum().intValue());
    }

    @Override
    public void close() throws IOException {
        connection.closeAsync();
    }

    @Override
    public boolean isOpen() {
        return !connection.isClosed();
    }

}
