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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;

/**
 * Utility class with factory methods to create different types of MQTT messages.
 * <p>解码器在解析完固定头、可变头与载荷后，按 {@link MqttMessageType} 实例化对应的具体子类，
 * 使 pipeline 下游能以强类型处理各类 MQTT 报文。</p>
 */
public final class MqttMessageFactory {

    /**
     * 根据固定头中的消息类型，将已解码的三段组装为强类型 {@link MqttMessage} 子类。
     *
     * @param mqttFixedHeader 固定头
     * @param variableHeader  可变头（类型因消息而异）
     * @param payload         载荷（部分报文为 null）
     */
    public static MqttMessage newMessage(MqttFixedHeader mqttFixedHeader, Object variableHeader, Object payload) {
        switch (mqttFixedHeader.messageType()) {
            case CONNECT :
                return new MqttConnectMessage(
                        mqttFixedHeader,
                        (MqttConnectVariableHeader) variableHeader,
                        (MqttConnectPayload) payload);

            case CONNACK:
                return new MqttConnAckMessage(mqttFixedHeader, (MqttConnAckVariableHeader) variableHeader);

            case SUBSCRIBE:
                return new MqttSubscribeMessage(
                        mqttFixedHeader,
                        (MqttMessageIdVariableHeader) variableHeader,
                        (MqttSubscribePayload) payload);

            case SUBACK:
                return new MqttSubAckMessage(
                        mqttFixedHeader,
                        (MqttMessageIdVariableHeader) variableHeader,
                        (MqttSubAckPayload) payload);

            case UNSUBACK:
                return new MqttUnsubAckMessage(
                        mqttFixedHeader,
                        (MqttMessageIdVariableHeader) variableHeader,
                        (MqttUnsubAckPayload) payload);

            case UNSUBSCRIBE:
                return new MqttUnsubscribeMessage(
                        mqttFixedHeader,
                        (MqttMessageIdVariableHeader) variableHeader,
                        (MqttUnsubscribePayload) payload);

            case PUBLISH:
                return new MqttPublishMessage(
                        mqttFixedHeader,
                        (MqttPublishVariableHeader) variableHeader,
                        (ByteBuf) payload);

            case PUBACK:
                //Having MqttPubReplyMessageVariableHeader or MqttMessageIdVariableHeader
                // v5 使用带原因码的可变头；v3 仅含报文标识符
                return new MqttPubAckMessage(mqttFixedHeader, (MqttMessageIdVariableHeader) variableHeader);
            case PUBREC:
            case PUBREL:
            case PUBCOMP:
                //Having MqttPubReplyMessageVariableHeader or MqttMessageIdVariableHeader
                // QoS 2 四步握手的中途报文，可变头结构同 PUBACK
                return new MqttMessage(mqttFixedHeader, variableHeader);

            case PINGREQ:
            case PINGRESP:
                // 心跳报文无可变头与载荷
                return new MqttMessage(mqttFixedHeader);

            case DISCONNECT:
            case AUTH:
                //Having MqttReasonCodeAndPropertiesVariableHeader
                // MQTT v5：断开或增强认证，可变头含原因码与属性
                return new MqttMessage(mqttFixedHeader,
                        variableHeader);

            default:
                throw new IllegalArgumentException("unknown message type: " + mqttFixedHeader.messageType());
        }
    }

    /** 构造解码完全失败的占位消息，固定头/可变头/载荷均为 null。 */
    public static MqttMessage newInvalidMessage(Throwable cause) {
        return new MqttMessage(null, null, null, DecoderResult.failure(cause));
    }

    /** 构造部分字段已解析但后续失败的无效消息，保留已读出的头信息便于诊断。 */
    public static MqttMessage newInvalidMessage(MqttFixedHeader mqttFixedHeader, Object variableHeader,
                                                Throwable cause) {
        return new MqttMessage(mqttFixedHeader, variableHeader, null, DecoderResult.failure(cause));
    }

    private MqttMessageFactory() { }
}
