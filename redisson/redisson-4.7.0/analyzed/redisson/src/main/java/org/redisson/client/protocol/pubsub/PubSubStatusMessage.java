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
package org.redisson.client.protocol.pubsub;

import org.redisson.client.ChannelName;

/**
 * Pub/Sub 订阅/退订状态通知消息。
 * <p>
 * Redis 在执行 {@code SUBSCRIBE}、{@code PSUBSCRIBE} 或对应退订命令后，
 * 会先推送一条状态回复，本类封装该回复的类型与频道信息。
 *
 * @author Nikita Koksharov
 *
 */
public class PubSubStatusMessage implements Message {

    /** 订阅状态变更类型（订阅或退订）。 */
    private final PubSubType type;
    /** 涉及的频道或模式名。 */
    private final ChannelName channel;

    /** @param type 状态类型 @param channel 频道名 */
    public PubSubStatusMessage(PubSubType type, ChannelName channel) {
        super();
        this.type = type;
        this.channel = channel;
    }

    /** 返回状态消息关联的频道。 */
    @Override
    public ChannelName getChannel() {
        return channel;
    }

    /** 返回订阅状态变更类型。 */
    public PubSubType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PubSubStatusMessage [type=" + type + ", channels=" + channel + "]";
    }

}
