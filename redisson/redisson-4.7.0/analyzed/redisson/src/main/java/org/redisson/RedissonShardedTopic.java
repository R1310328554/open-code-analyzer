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
package org.redisson;

import org.redisson.config.NameMapper;
import org.redisson.api.RFuture;
import org.redisson.api.RShardedTopic;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.ChannelName;
import org.redisson.client.RedisPubSubListener;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.pubsub.PubSubType;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.pubsub.PubSubConnectionEntry;

import java.util.concurrent.CompletableFuture;

/**
 * Redis Cluster 分片主题 {@link org.redisson.api.RShardedTopic} 实现。
 * <p>使用 {@code SPUBLISH}/{@code SSUBSCRIBE}；消息仅投递到同一 slot 上的监听器。
 *
 * @author Nikita Koksharov
 */
public class RedissonShardedTopic extends RedissonTopic implements RShardedTopic {

    /** 使用默认 codec 构造。 */
    public RedissonShardedTopic(CommandAsyncExecutor commandExecutor, String... names) {
        super(commandExecutor, names);
    }

    public RedissonShardedTopic(Codec codec, CommandAsyncExecutor commandExecutor, String... names) {
        super(codec, commandExecutor, names);
    }

    public RedissonShardedTopic(Codec codec, CommandAsyncExecutor commandExecutor, NameMapper nameMapper, String... names) {
        super(codec, commandExecutor, nameMapper, names);
    }

    /** 创建不经 {@link NameMapper} 映射的原始分片主题实例。 */
    public static RedissonTopic createRaw(Codec codec, CommandAsyncExecutor commandExecutor, String... names) {
        return new RedissonShardedTopic(codec, commandExecutor, NameMapper.direct(), names);
    }

    @Override
    /** 通过 {@code SSUBSCRIBE} 注册分片 Pub/Sub 监听器。 */
    protected RFuture<Integer> addListenerAsync(RedisPubSubListener<?> pubSubListener) {
        CompletableFuture<PubSubConnectionEntry> future = subscribeService.ssubscribe(codec, channelNames, pubSubListener);
        CompletableFuture<Integer> f = future.thenApply(res -> {
            return System.identityHashCode(pubSubListener);
        });
        return new CompletableFutureWrapper<>(f);
    }

    @Override
    /** 通过 {@code SPUBLISH} 向分片频道发布消息。 */
    public RFuture<Long> publishAsync(Object message) {
        String name = getName();
        return commandExecutor.writeAsync(name, StringCodec.INSTANCE, RedisCommands.SPUBLISH, name, commandExecutor.encode(codec, message));
    }

    @Override
    public RFuture<Void> removeListenerAsync(MessageListener<?> listener) {
        CompletableFuture<Void> f = subscribeService.removeListenerAsync(PubSubType.SUNSUBSCRIBE, channelNames, listener);
        return new CompletableFutureWrapper<>(f);
    }

    @Override
    public RFuture<Void> removeListenerAsync(Integer... listenerIds) {
        CompletableFuture<Void> f = subscribeService.removeListenerAsync(PubSubType.SUNSUBSCRIBE, channelNames, listenerIds);
        return new CompletableFutureWrapper<>(f);
    }

    @Override
    public RFuture<Void> removeAllListenersAsync() {
        CompletableFuture<Void> f = subscribeService.removeAllListenersAsync(PubSubType.SUNSUBSCRIBE, channelNames.toArray(new ChannelName[0]));
        return new CompletableFutureWrapper<>(f);
    }

    @Override
    public RFuture<Long> countSubscribersAsync() {
        return commandExecutor.writeAsync(names.get(0), LongCodec.INSTANCE, RedisCommands.PUBSUB_SHARDNUMSUB, names.toArray());
    }
}
