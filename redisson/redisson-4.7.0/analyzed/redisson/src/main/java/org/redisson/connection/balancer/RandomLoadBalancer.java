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
import org.redisson.connection.ClientConnectionsEntry;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡器：从可用从节点连接池中均匀随机选取一个入口。
 * <p>
 * 继承 {@link BaseLoadBalancer}，先过滤冻结/不健康节点再随机选择。
 *
 * @author Nikita Koksharov
 *
 */
public class RandomLoadBalancer extends BaseLoadBalancer {

    /** 忽略命令类型，随机选取一个可用连接入口。 */
    @Override
    public ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clientsCopy) {
        return getEntry(clientsCopy, null);
    }

    /**
     * 过滤后从候选列表中随机选取一个 {@link ClientConnectionsEntry}。
     *
     * @param clientsCopy 候选连接入口列表（会被 filter 过滤）
     * @param redisCommand 当前 Redis 命令（本实现未使用）
     * @return 选中的入口，无可用节点时返回 null
     */
    @Override
    public ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clientsCopy, RedisCommand<?> redisCommand) {
        clientsCopy = filter(clientsCopy);
        if (clientsCopy.isEmpty()) {
            return null;
        }

        // 在过滤后的列表中均匀随机选取索引
        int ind = ThreadLocalRandom.current().nextInt(clientsCopy.size());
        return clientsCopy.get(ind);
    }
}
