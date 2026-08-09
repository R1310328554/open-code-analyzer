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
package org.redisson.rx;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.ReplayProcessor;
import org.redisson.api.RFuture;
import org.redisson.api.RReliableTopic;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 可靠主题（Reliable Topic）的 Rx 消息流适配。
 * <p>
 * 与 {@link RedissonTopicRx} 不同：消息由 Redis Stream/可靠队列语义保证至少一次投递；
 * {@code getMessages} 在收到 {@code request(n)} 个元素后自动移除监听器并结束流。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReliableTopicRx {

    /** 底层可靠主题实例。 */
    private final RReliableTopic topic;

    public RedissonReliableTopicRx(RReliableTopic topic) {
        this.topic = topic;
    }

    /**
     * 订阅至多 {@code n} 条类型为 {@code type} 的消息。
     * <p>
     * 流程：{@code doOnRequest} → 注册 {@code addListenerAsync} → 每条 {@code onNext} 递减计数 →
     * 计数归零时 {@code removeListenerAsync} 并 {@code onComplete}；取消订阅同样移除监听。
     */
    public <M> Flowable<M> getMessages(Class<M> type) {
        ReplayProcessor<M> p = ReplayProcessor.create();
        return p.doOnRequest(n -> {
            AtomicLong counter = new AtomicLong(n);
            AtomicReference<String> idRef = new AtomicReference<>();
            RFuture<String> t = topic.addListenerAsync(type, (channel, msg) -> {
                p.onNext(msg);
                if (counter.decrementAndGet() == 0) {
                    topic.removeListenerAsync(idRef.get());
                    p.onComplete();
                }
            });
            t.whenComplete((id, e) -> {
                if (e != null) {
                    p.onError(e);
                    return;
                }

                idRef.set(id);
                p.doOnCancel(() -> topic.removeListenerAsync(id));
            });
        });
    }
    
}
