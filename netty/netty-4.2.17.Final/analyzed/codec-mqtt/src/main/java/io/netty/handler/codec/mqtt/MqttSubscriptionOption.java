/*
 * Copyright 2020 The Netty Project
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

/**
 * Model the SubscriptionOption used in Subscribe MQTT v5 packet
 * <p>MQTT 5 订阅选项：除 QoS 外还控制本地投递、保留消息转发及订阅时是否下发已有 retain。
 * 3.x 客户端仅需 QoS，可通过 {@link #onlyFromQos} 构造默认选项。</p>
 */
public final class MqttSubscriptionOption {

    /** 订阅时对已有 retain 消息的处理策略（对应 SUBSCRIBE 载荷中的 Retain Handling 字段）。 */
    public enum RetainedHandlingPolicy {
        /** 订阅即下发匹配主题的 retain 消息。 */
        SEND_AT_SUBSCRIBE(0),
        /** 仅当该订阅尚无活跃会话时下发 retain。 */
        SEND_AT_SUBSCRIBE_IF_NOT_YET_EXISTS(1),
        /** 订阅时不推送 retain，仅接收后续新 publish。 */
        DONT_SEND_AT_SUBSCRIBE(2);

        private final int value;

        RetainedHandlingPolicy(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static RetainedHandlingPolicy valueOf(int value) {
            switch (value) {
            case 0:
                return SEND_AT_SUBSCRIBE;
            case 1:
                return SEND_AT_SUBSCRIBE_IF_NOT_YET_EXISTS;
            case 2:
                return DONT_SEND_AT_SUBSCRIBE;
            default:
                throw new IllegalArgumentException("invalid RetainedHandlingPolicy: " + value);
            }
        }
    }

    private final MqttQoS qos;
    /** 为 true 时 broker 不向发布该订阅的同一客户端回环投递（No Local 位）。 */
    private final boolean noLocal;
    /** 为 true 时转发 retain 消息时保留原 RETAIN 标志。 */
    private final boolean retainAsPublished;
    private final RetainedHandlingPolicy retainHandling;

    /** 仅指定 QoS 的便捷工厂，其余选项取 MQTT 5 默认值。 */
    public static MqttSubscriptionOption onlyFromQos(MqttQoS qos) {
        return new MqttSubscriptionOption(qos, false, false, RetainedHandlingPolicy.SEND_AT_SUBSCRIBE);
    }

    public MqttSubscriptionOption(MqttQoS qos,
                                  boolean noLocal,
                                  boolean retainAsPublished,
                                  RetainedHandlingPolicy retainHandling) {
        this.qos = qos;
        this.noLocal = noLocal;
        this.retainAsPublished = retainAsPublished;
        this.retainHandling = retainHandling;
    }

    public MqttQoS qos() {
        return qos;
    }

    public boolean isNoLocal() {
        return noLocal;
    }

    public boolean isRetainAsPublished() {
        return retainAsPublished;
    }

    public RetainedHandlingPolicy retainHandling() {
        return retainHandling;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MqttSubscriptionOption that = (MqttSubscriptionOption) o;

        if (noLocal != that.noLocal) {
            return false;
        }
        if (retainAsPublished != that.retainAsPublished) {
            return false;
        }
        if (qos != that.qos) {
            return false;
        }
        return retainHandling == that.retainHandling;
    }

    @Override
    public int hashCode() {
        int result = qos.hashCode();
        result = 31 * result + (noLocal ? 1 : 0);
        result = 31 * result + (retainAsPublished ? 1 : 0);
        result = 31 * result + retainHandling.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SubscriptionOption[" +
                "qos=" + qos +
                ", noLocal=" + noLocal +
                ", retainAsPublished=" + retainAsPublished +
                ", retainHandling=" + retainHandling +
                ']';
    }
}
