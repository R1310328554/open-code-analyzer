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
package org.redisson.connection.balancer;

import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.connection.ClientConnectionsEntry;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询（Round-Robin）负载均衡器，按顺序循环选取从节点连接入口。
 * <p>
 * Pub/Sub 命令与普通命令使用独立的轮询计数器，避免订阅连接与普通连接互相干扰。
 *
 * @author Nikita Koksharov
 *
 */
public class RoundRobinLoadBalancer extends BaseLoadBalancer {

    /** 普通 Redis 命令的轮询计数器（初始 -1，首次 increment 后为 0）。 */
    private final AtomicInteger index = new AtomicInteger(-1);
    /** Pub/Sub 命令专用的轮询计数器。 */
    private final AtomicInteger pubSubIndex = new AtomicInteger(-1);

    /** 根据命令类型选择对应计数器进行轮询选取。 */
    @Override
    public ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clientsCopy, RedisCommand<?> redisCommand) {
        if (redisCommand != null
                && RedisCommands.PUBSUB_COMMANDS.contains(redisCommand.getName())) {
            return getEntry(clientsCopy, pubSubIndex);
        }
        return getEntry(clientsCopy, index);
    }

    /** 使用普通命令计数器轮询选取。 */
    @Override
    public ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clientsCopy) {
        return getEntry(clientsCopy, index);
    }

    /**
     * 使用指定原子计数器对过滤后的列表做取模轮询。
     *
     * @param clientsCopy 候选连接入口
     * @param counter 轮询计数器
     * @return 本轮选中的入口
     */
    private ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clientsCopy, AtomicInteger counter) {
        clientsCopy = filter(clientsCopy);
        if (clientsCopy.isEmpty()) {
            return null;
        }

        // floorMod 保证负数 increment 结果仍为非负索引
        int ind = Math.floorMod(counter.incrementAndGet(), clientsCopy.size());
        return clientsCopy.get(ind);
    }

}
