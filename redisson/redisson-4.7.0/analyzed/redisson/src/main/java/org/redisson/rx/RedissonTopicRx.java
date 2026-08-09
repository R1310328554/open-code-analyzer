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

import java.util.concurrent.atomic.AtomicLong;

import org.redisson.api.RFuture;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.ReplayProcessor;

/**
 * Redis Pub/Sub {@link RTopic} 的 Rx 消息流适配。
 * <p>
 * {@code getMessages} 在首次 {@code request(n)} 时注册 {@link MessageListener}，
 * 收到 {@code n} 条消息后自动注销监听并 {@code onComplete}；下游 cancel 同样移除监听。
 * 与 {@link RedissonReliableTopicRx} 相比不保证持久化与重投。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonTopicRx {

    /** 底层主题（Pub/Sub channel）。 */
    private final RTopic topic;
    
    public RedissonTopicRx(RTopic topic) {
        this.topic = topic;
    }

    /**
     * 订阅至多 {@code n} 条反序列化为 {@code type} 的广播消息。
     * <p>
     * 使用 {@link AtomicLong} 跟踪剩余配额；注册失败时 {@code onError}。
     */
    public <M> Flowable<M> getMessages(Class<M> type) {
        ReplayProcessor<M> p = ReplayProcessor.create();
        return p.doOnRequest(n -> {
            AtomicLong counter = new AtomicLong(n);
            RFuture<Integer> t = topic.addListenerAsync(type, new MessageListener<M>() {
                @Override
                public void onMessage(CharSequence channel, M msg) {
                    p.onNext(msg);
                    if (counter.decrementAndGet() == 0) {
                        topic.removeListenerAsync(this);
                        p.onComplete();
                    }
                }
            });
            t.whenComplete((id, e) -> {
                if (e != null) {
                    p.onError(e);
                    return;
                }

                p.doOnCancel(() -> topic.removeListenerAsync(id));
            });
        });
    }
    
}
