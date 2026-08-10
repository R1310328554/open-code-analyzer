/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.handler.codec.mqtt;

import io.netty.util.internal.StringUtil;

/**
 * Contains a topic name and Qos Level.
 * This is part of the {@link MqttSubscribePayload}
 * <p>单条主题订阅：含过滤器字符串及订阅选项。过滤器可含 {@code +}/{@code #} 通配符；
 * {@link #setTopicFilter} 支持服务端主题重写而无需客户端重配。</p>
 */
public final class MqttTopicSubscription {

    private String topicFilter;
    private final MqttSubscriptionOption option;

    /** 3.x 风格构造：仅 QoS，内部包装为默认 {@link MqttSubscriptionOption}。 */
    public MqttTopicSubscription(String topicFilter, MqttQoS qualityOfService) {
        this.topicFilter = topicFilter;
        this.option = MqttSubscriptionOption.onlyFromQos(qualityOfService);
    }

    public MqttTopicSubscription(String topicFilter, MqttSubscriptionOption option) {
        this.topicFilter = topicFilter;
        this.option = option;
    }

    /**
     * @deprecated use topicFilter
     */
    @Deprecated
    public String topicName() {
        return topicFilter;
    }

    public String topicFilter() {
        return topicFilter;
    }

    /**
     * Rewrite topic filter.
     * <p>
     *
     * Many IoT devices do not support reconfiguration or upgrade, so it is hard to
     * change their subscribed topics. To resolve this issue, MQTT server may offer
     * topic rewrite capability.
     * <p>服务端可在解码后、业务处理前改写过滤器，解决固件无法变更订阅主题的场景。</p>
     *
     * @param topicFilter Topic to rewrite to
     */
    public void setTopicFilter(String topicFilter) {
        this.topicFilter = topicFilter;
    }

    public MqttQoS qualityOfService() {
        return option.qos();
    }

    public MqttSubscriptionOption option() {
        return option;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
                .append('[')
                .append("topicFilter=").append(topicFilter)
                .append(", option=").append(this.option)
                .append(']')
                .toString();
    }
}
