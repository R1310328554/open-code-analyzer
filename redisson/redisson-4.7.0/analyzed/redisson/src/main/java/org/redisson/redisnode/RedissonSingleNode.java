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
package org.redisson.redisnode;

import org.redisson.api.NodeType;
import org.redisson.api.redisnode.RedisMaster;
import org.redisson.api.redisnode.RedisSingle;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.ConnectionManager;

import java.util.Collection;

/**
 * 单节点 Redis 部署的节点管理，实现 {@link RedisSingle}。
 * <p>
 * {@link #getInstance()} 返回唯一 Master 视图，适用于 standalone 模式。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonSingleNode extends RedissonBaseNodes implements RedisSingle {

    /** @param connectionManager 单节点连接管理器 @param commandExecutor 命令执行器 */
    public RedissonSingleNode(ConnectionManager connectionManager, CommandAsyncExecutor commandExecutor) {
        super(connectionManager, commandExecutor);
    }

    /** @return 唯一的 Redis 实例（Master 角色） */
    @Override
    public RedisMaster getInstance() {
        Collection<RedisMaster> list = getNodes(NodeType.MASTER);
        return list.iterator().next();
    }
}
