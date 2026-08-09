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

import java.util.concurrent.atomic.AtomicLong;

import org.redisson.api.RFuture;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

import reactor.core.publisher.Flux;

/**
 * {@link RTopic} 的 Reactor 响应式封装：
 * 将 Pub/Sub 主题订阅转为有限长度的 {@link Flux} 消息流。
 * <p>
 * 在下游 {@code request(n)} 时注册 {@link MessageListener}；
 * 收到 n 条消息后自动注销监听并完成流；dispose 时同样清理。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonTopicReactive {

    /** 底层 Redis 主题。 */
    private final RTopic topic;
    
    /** @param topic 同步 RTopic 实例 */
    public RedissonTopicReactive(RTopic topic) {
        this.topic = topic;
    }

    /** 订阅最多 n 条类型化消息（n 由下游 request 决定）。 */
    public <M> Flux<M> getMessages(Class<M> type) {
        return Flux.create(emitter -> {
            // 下游背压请求到达后再注册 Pub/Sub 监听
            emitter.onRequest(n -> {
                // 剩余待投递消息计数
                AtomicLong counter = new AtomicLong(n);
                RFuture<Integer> t = topic.addListenerAsync(type, new MessageListener<M>() {
                    @Override
                    public void onMessage(CharSequence channel, M msg) {
                        emitter.next(msg);
                        // 已满足请求数量：注销监听并完成
                        if (counter.decrementAndGet() == 0) {
                            topic.removeListenerAsync(this);
                            emitter.complete();
                        }
                    }
                });
                t.whenComplete((id, e) -> {
                    if (e != null) {
                        emitter.error(e);
                        return;
                    }
                    
                    // 订阅取消时异步移除监听器
                    emitter.onDispose(() -> {
                        topic.removeListenerAsync(id);
                    });
                });
            });
        });
    }
    
}
