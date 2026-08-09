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
package org.redisson.client.handler;

import org.redisson.client.RedisClient;
import org.redisson.client.RedisConnection;

import io.netty.channel.ChannelHandlerContext;

/**
 * 普通 Redis 命令连接的 Netty 入站处理器。
 * <p>
 * 在通道激活时创建 {@link RedisConnection} 并完成认证与协议协商。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisConnectionHandler extends BaseConnectionHandler<RedisConnection> {

    /** 绑定所属 {@link RedisClient}。 */
    public RedisConnectionHandler(RedisClient redisClient) {
        super(redisClient);
    }
    
    /** 为当前通道创建普通命令 {@link RedisConnection}。 */
    @Override
    RedisConnection createConnection(ChannelHandlerContext ctx) {
        return new RedisConnection(redisClient, ctx.channel(), connectionPromise);
    }

}
