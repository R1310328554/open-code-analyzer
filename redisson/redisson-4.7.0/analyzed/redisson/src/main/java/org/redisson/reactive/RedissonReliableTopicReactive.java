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
package org.redisson.reactive;

import org.redisson.api.RFuture;
import org.redisson.api.RReliableTopic;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link RReliableTopic} 的 Reactor 响应式封装：
 * 将可靠主题订阅转为有限长度的 {@link Flux} 消息流。
 * <p>
 * 订阅者在 {@code onRequest} 时注册监听器；收到指定数量消息后
 * 自动移除监听并完成流；取消订阅时同样清理监听器。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReliableTopicReactive {

    /** 底层可靠主题实例。 */
    private final RReliableTopic topic;

    /** @param topic 同步可靠主题 */
    public RedissonReliableTopicReactive(RReliableTopic topic) {
        this.topic = topic;
    }

    /** 订阅最多 {@code n} 条消息（由下游 request 决定），返回类型化 Flux。 */
    public <M> Flux<M> getMessages(Class<M> type) {
        return Flux.create(emitter -> {
            // 下游请求 n 条消息时注册异步监听器
            emitter.onRequest(n -> {
                // 剩余待接收消息计数；监听器 ID 在注册完成后写入
                AtomicLong counter = new AtomicLong(n);
                AtomicReference<String> idRef = new AtomicReference<>();
                RFuture<String> t = topic.addListenerAsync(type, (channel, msg) -> {
                    emitter.next(msg);
                    // 已达请求数量：移除监听并完成 Flux
                    if (counter.decrementAndGet() == 0) {
                        topic.removeListenerAsync(idRef.get());
                        emitter.complete();
                    }
                });
                t.whenComplete((id, e) -> {
                    if (e != null) {
                        emitter.error(e);
                        return;
                    }

                    idRef.set(id);
                    // 订阅取消时清理 Redis 监听器
                    emitter.onDispose(() -> {
                        topic.removeListenerAsync(id);
                    });
                });
            });
        });
    }
    
}
