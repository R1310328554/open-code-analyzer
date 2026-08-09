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

import org.redisson.client.BaseRedisPubSubListener;
import org.redisson.client.ChannelName;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.protocol.pubsub.PubSubType;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.pubsub.PubSubConnectionEntry;
import org.redisson.pubsub.PublishSubscribeService;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.util.AbstractSubscription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Spring Data Redis Pub/Sub {@link AbstractSubscription} 的 Redisson 实现。
 * <p>通过 {@link PublishSubscribeService} 管理频道/模式订阅，
将 Redisson 消息转为 {@link DefaultMessage} 回调 {@link MessageListener}。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonSubscription extends AbstractSubscription {

    private final CommandAsyncExecutor commandExecutor;
    private final PublishSubscribeService subscribeService;
    
    /** 绑定异步命令执行器与 Spring 消息监听器。 */
    public RedissonSubscription(CommandAsyncExecutor commandExecutor, MessageListener listener) {
        super(listener, null, null);
        this.commandExecutor = commandExecutor;
        this.subscribeService = commandExecutor.getConnectionManager().getSubscribeService();
    }

    /** 对每个频道注册 {@link BaseRedisPubSubListener} 并阻塞等待订阅完成。 */
    @Override
    protected void doSubscribe(byte[]... channels) {
        List<CompletableFuture<?>> list = new ArrayList<>();
        for (byte[] channel : channels) {
            CompletableFuture<List<PubSubConnectionEntry>> f = subscribeService.subscribe(ByteArrayCodec.INSTANCE, new ChannelName(channel), new BaseRedisPubSubListener() {
                @Override
                public void onMessage(CharSequence ch, Object message) {
                    // 忽略非目标频道的回调（连接复用时可能收到其他频道消息）。
                    if (!Arrays.equals(((ChannelName) ch).getName(), channel)) {
                        return;
                    }

                    byte[] m = toBytes(message);
                    DefaultMessage msg = new DefaultMessage(((ChannelName) ch).getName(), m);
                    getListener().onMessage(msg, null);
                }
            });
            list.add(f);
        }
        for (CompletableFuture<?> future : list) {
            commandExecutor.get(future);
        }
    }

    @Override
    protected void doUnsubscribe(boolean all, byte[]... channels) {
        for (byte[] channel : channels) {
            subscribeService.unsubscribe(new ChannelName(channel), PubSubType.UNSUBSCRIBE);
        }
    }

    /** 按模式订阅（PSUBSCRIBE），回调携带 pattern 与 channel。 */
    @Override
    protected void doPsubscribe(byte[]... patterns) {
        List<CompletableFuture<?>> list = new ArrayList<>();
        for (byte[] channel : patterns) {
            CompletableFuture<Collection<PubSubConnectionEntry>> f = subscribeService.psubscribe(new ChannelName(channel), ByteArrayCodec.INSTANCE, new BaseRedisPubSubListener() {
                @Override
                public void onPatternMessage(CharSequence pattern, CharSequence ch, Object message) {
                    if (!Arrays.equals(((ChannelName) pattern).getName(), channel)) {
                        return;
                    }

                    byte[] m = toBytes(message);
                    DefaultMessage msg = new DefaultMessage(((ChannelName)ch).getName(), m);
                    getListener().onMessage(msg, ((ChannelName)pattern).getName());
                }
            });
            list.add(f);
        }
        for (CompletableFuture<?> future : list) {
            commandExecutor.get(future);
        }
    }

    /** 将 String 或 byte[] 载荷统一为字节数组。 */
    private byte[] toBytes(Object message) {
        if (message instanceof String) {
            return  ((String) message).getBytes();
        }
        return (byte[]) message;
    }

    @Override
    protected void doPUnsubscribe(boolean all, byte[]... patterns) {
        for (byte[] pattern : patterns) {
            subscribeService.unsubscribe(new ChannelName(pattern), PubSubType.PUNSUBSCRIBE);
        }
    }

    /** 关闭时取消所有频道与模式订阅。 */
    @Override
    protected void doClose() {
        doUnsubscribe(false, getChannels().toArray(new byte[getChannels().size()][]));
        doPUnsubscribe(false, getPatterns().toArray(new byte[getPatterns().size()][]));
    }

}
