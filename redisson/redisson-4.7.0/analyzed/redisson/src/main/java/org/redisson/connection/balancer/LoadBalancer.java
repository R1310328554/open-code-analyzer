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

/**
 * 从节点负载均衡器接口。
 * <p>
 * 读操作根据 {@link ReadMode} 从从节点列表中选取 {@link ClientConnectionsEntry}；
 * 推荐使用带 {@link RedisCommand} 参数的 {@link #getEntry(List, RedisCommand)}。
 *
 * @author Nikita Koksharov
 *
 */
public interface LoadBalancer {

    /* 请改用 getEntry(List, RedisCommand) 方法 */
    @Deprecated
    ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clientsCopy);

    /** 默认忽略命令，委托无命令版本。 */
    default ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clientsCopy, RedisCommand<?> redisCommand) {
        return getEntry(clientsCopy);
    }

}
