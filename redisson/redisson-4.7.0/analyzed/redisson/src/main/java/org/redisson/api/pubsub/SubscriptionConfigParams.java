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
package org.redisson.api.pubsub;

import java.time.Duration;

/**
 * {@link SubscriptionConfig} 的可变参数实现类。
 * <p>
 * 持有订阅名称、死信主题、投递次数上限、可见性超时、消费起始位置及 ack 后保留等配置。
 *
 * @author Nikita Koksharov
 *
 */
public class SubscriptionConfigParams implements SubscriptionConfig {

    private boolean retainAfterAck;
    private String deadLetterTopicName;
    private int deliveryLimit;
    private Duration visibility = Duration.ofSeconds(0);
    private Position position = Position.latest();

    private final String name;

    /**
     * 使用指定订阅名称构造配置参数。
     *
     * @param name 订阅名称，可为 {@code null} 表示自动生成
     */
    public SubscriptionConfigParams(String name) {
        this.name = name;
    }

    @Override
    public SubscriptionConfig deadLetterTopicName(String value) {
        this.deadLetterTopicName = value;
        return this;
    }

    @Override
    public SubscriptionConfig deliveryLimit(int value) {
        this.deliveryLimit = value;
        return this;
    }

    @Override
    public SubscriptionConfig visibility(Duration value) {
        this.visibility = value;
        return this;
    }

    @Override
    public SubscriptionConfig position(Position value) {
        this.position = value;
        return this;
    }

    @Override
    public SubscriptionConfig retainAfterAck() {
        this.retainAfterAck = true;
        return this;
    }

    /** @return 死信主题名称 */
    public String getDeadLetterTopicName() {
        return deadLetterTopicName;
    }

    /** @return 最大投递次数 */
    public int getDeliveryLimit() {
        return deliveryLimit;
    }

    /** @return 消息可见性超时 */
    public Duration getVisibility() {
        return visibility;
    }

    /** @return 消费起始位置 */
    public Position getPosition() {
        return position;
    }

    /** @return 订阅名称 */
    public String getName() {
        return name;
    }

    /** @return 是否在 ack 后保留消息 */
    public boolean isRetainAfterAck() {
        return retainAfterAck;
    }
}
