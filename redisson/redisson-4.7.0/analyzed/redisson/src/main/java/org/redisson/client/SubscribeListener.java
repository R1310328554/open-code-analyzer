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
package org.redisson.client;

import org.redisson.client.protocol.pubsub.PubSubType;

import java.util.concurrent.CompletableFuture;

/**
 * 等待指定频道订阅成功的 Pub/Sub 监听器。
 * <p>
 * 当 {@link #onStatus} 收到匹配的订阅确认时完成内部 {@link CompletableFuture}。
 *
 * @author Nikita Koksharov
 *
 */
public class SubscribeListener extends BaseRedisPubSubListener {

    /** 订阅成功时完成的 Future。 */
    private final CompletableFuture<Void> promise = new CompletableFuture<>();
    /** 待确认的频道名称。 */
    private final ChannelName name;
    /** 期望的订阅类型（SUBSCRIBE/PSUBSCRIBE 等）。 */
    private final PubSubType type;

    /** 指定目标频道与订阅类型创建监听器。 */
    public SubscribeListener(ChannelName name, PubSubType type) {
        super();
        this.name = name;
        this.type = type;
    }

    /** 收到订阅状态回调时，若频道与类型均匹配则标记成功。 */
    @Override
    public void onStatus(PubSubType type, CharSequence channel) {
        if (name.equals(channel) && this.type.equals(type)) {
            promise.complete(null);
        }
    }

    /** 返回订阅成功时完成的 Future。 */
    public CompletableFuture<Void> getSuccessFuture() {
        return promise;
    }
    
}
