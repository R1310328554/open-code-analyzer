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

import io.netty.channel.ChannelHandlerContext;
import org.redisson.client.ChannelName;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisPubSubConnection;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.pubsub.PubSubType;
import org.redisson.config.Protocol;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Pub/Sub 专用连接的 Netty 入站处理器。
 * <p>
 * 重认证前会暂存并取消现有订阅，认证成功后恢复频道订阅。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisPubSubConnectionHandler extends BaseConnectionHandler<RedisPubSubConnection> {

    /** 绑定所属 {@link RedisClient}。 */
    public RedisPubSubConnectionHandler(RedisClient redisClient) {
        super(redisClient);
    }
    
    /** 为当前通道创建 {@link RedisPubSubConnection}。 */
    @Override
    RedisPubSubConnection createConnection(ChannelHandlerContext ctx) {
        return new RedisPubSubConnection(redisClient, ctx.channel(), connectionPromise);
    }

    /** RESP2 下重认证前先取消订阅，认证后按原映射恢复订阅。 */
    @Override
    protected CompletableFuture<Void> authWithCredential() {
        if (redisClient.getConfig().getProtocol() == Protocol.RESP3) {
            return super.authWithCredential();
        }

        Map<ChannelName, Codec> channels = new LinkedHashMap<>(connection.getChannels());
        Map<ChannelName, Codec> patternChannels = new LinkedHashMap<>(connection.getPatternChannels());
        Map<ChannelName, Codec> shardedChannels = new LinkedHashMap<>(connection.getShardedChannels());

        if (channels.isEmpty() && patternChannels.isEmpty() && shardedChannels.isEmpty()) {
            return super.authWithCredential();
        }

        if (!channels.isEmpty()) {
            connection.unsubscribe(PubSubType.UNSUBSCRIBE,
                    channels.keySet().toArray(new ChannelName[0]));
        }
        if (!patternChannels.isEmpty()) {
            connection.unsubscribe(PubSubType.PUNSUBSCRIBE,
                    patternChannels.keySet().toArray(new ChannelName[0]));
        }
        if (!shardedChannels.isEmpty()) {
            connection.unsubscribe(PubSubType.SUNSUBSCRIBE,
                    shardedChannels.keySet().toArray(new ChannelName[0]));
        }

        return super.authWithCredential().thenRun(() -> {
            for (Map.Entry<ChannelName, Codec> e : channels.entrySet()) {
                connection.subscribe(e.getValue(), e.getKey());
            }
            for (Map.Entry<ChannelName, Codec> e : patternChannels.entrySet()) {
                connection.psubscribe(e.getValue(), e.getKey());
            }
            for (Map.Entry<ChannelName, Codec> e : shardedChannels.entrySet()) {
                connection.ssubscribe(e.getValue(), e.getKey());
            }
        });
    }

}