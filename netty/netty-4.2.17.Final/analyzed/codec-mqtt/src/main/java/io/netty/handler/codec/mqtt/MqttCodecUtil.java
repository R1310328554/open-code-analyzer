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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import static io.netty.handler.codec.mqtt.MqttConstant.MIN_CLIENT_ID_LENGTH;

/**
 * MQTT 编解码内部工具：Channel 属性存协议版本、字段合法性校验、固定头规范化。
 * <p>CONNECT 解析后通过 {@link #setMqttVersion} 写入版本，后续报文按 3.1/3.1.1/5.0 分支处理。</p>
 */
final class MqttCodecUtil {

    /** 绑定在 Channel 上，记录本连接协商出的 MQTT 协议版本。 */
    static final AttributeKey<MqttVersion> MQTT_VERSION_KEY = AttributeKey.valueOf("NETTY_CODEC_MQTT_VERSION");

    /** 未收到 CONNECT 前默认按 MQTT 3.1.1 语义解码。 */
    static MqttVersion getMqttVersion(ChannelHandlerContext ctx) {
        Attribute<MqttVersion> attr = ctx.channel().attr(MQTT_VERSION_KEY);
        MqttVersion version = attr.get();
        if (version == null) {
            return MqttVersion.MQTT_3_1_1;
        }
        return version;
    }

    static void setMqttVersion(ChannelHandlerContext ctx, MqttVersion version) {
        Attribute<MqttVersion> attr = ctx.channel().attr(MQTT_VERSION_KEY);
        attr.set(version);
    }

    static boolean isValidPublishTopicName(String topicName) {
        if (topicName == null) {
            return false;
        }
        // PUBLISH 主题名禁止通配符 #/+ 及 NUL（与 SUBSCRIBE 过滤语法不同）
        for (int i = 0; i < topicName.length(); i++) {
            char c = topicName.charAt(i);
            if (c == '#' || c == '+' || c == '\0') {
                return false;
            }
        }
        return true;
    }

    static boolean isValidMessageId(int messageId) {
        return messageId != 0;
    }

    static boolean isValidUserName(String userName) {
        return userName == null || userName.indexOf('\0') == -1;
    }

    /**
     * Determine if a client identifier is valid.
     * <p>3.1 强制长度 1–max；3.1.1/5.0 允许零长度与更长 ID；acceptNulBytes 控制是否容忍内嵌 NUL。</p>
     * @param mqttVersion The MQTT version semantics to use.
     * @param maxClientIdLength The max client id length.
     * @param clientId The client id value.
     * @param acceptNulBytes MQTT normally does not allow NUL bytes in client identifiers.
     * Set this to {@code true} to enable "legacy"/"lenient" mode, otherwise {@code false} for strict spec compliance.
     * @return {@code true} if the client id is valid, otherwise {@code false}.
     */
    static boolean isValidClientId(MqttVersion mqttVersion, int maxClientIdLength, String clientId,
                                   boolean acceptNulBytes) {
        if (clientId == null || (!acceptNulBytes && clientId.indexOf('\0') != -1)) {
            return false;
        }
        if (mqttVersion == MqttVersion.MQTT_3_1) {
            return clientId.length() >= MIN_CLIENT_ID_LENGTH && clientId.length() <= maxClientIdLength;
        }
        if (mqttVersion == MqttVersion.MQTT_3_1_1 || mqttVersion == MqttVersion.MQTT_5) {
            // In 3.1.3.1 Client Identifier of MQTT 3.1.1 and 5.0 specifications, The Server MAY allow ClientId’s
            // that contain more than 23 encoded bytes. And, The Server MAY allow zero-length ClientId.
            return true;
        }
        throw new IllegalArgumentException(mqttVersion + " is unknown mqtt version");
    }

    /** 校验固定头与消息类型的协议约束（如 SUBSCRIBE 必须 QoS 1，AUTH 仅 MQTT 5）。 */
    static MqttFixedHeader validateFixedHeader(ChannelHandlerContext ctx, MqttFixedHeader mqttFixedHeader) {
        switch (mqttFixedHeader.messageType()) {
            case PUBREL:
            case SUBSCRIBE:
            case UNSUBSCRIBE:
                if (mqttFixedHeader.qosLevel() != MqttQoS.AT_LEAST_ONCE) {
                    throw new DecoderException(mqttFixedHeader.messageType().name() + " message must have QoS 1");
                }
                return mqttFixedHeader;
            case AUTH:
                if (MqttCodecUtil.getMqttVersion(ctx) != MqttVersion.MQTT_5) {
                    throw new DecoderException("AUTH message requires at least MQTT 5");
                }
                return mqttFixedHeader;
            default:
                return mqttFixedHeader;
        }
    }

    /**
     * 将固定头中对该消息类型无意义的 DUP/QoS/Retain 位清零，便于 equals/编码一致性。
     * <p>例如 CONNECT/CONNACK 规范要求 QoS=0 且 Retain=0。</p>
     */
    static MqttFixedHeader resetUnusedFields(MqttFixedHeader mqttFixedHeader) {
        switch (mqttFixedHeader.messageType()) {
            case CONNECT:
            case CONNACK:
            case PUBACK:
            case PUBREC:
            case PUBCOMP:
            case SUBACK:
            case UNSUBACK:
            case PINGREQ:
            case PINGRESP:
            case DISCONNECT:
                if (mqttFixedHeader.isDup() ||
                        mqttFixedHeader.qosLevel() != MqttQoS.AT_MOST_ONCE ||
                        mqttFixedHeader.isRetain()) {
                    return new MqttFixedHeader(
                            mqttFixedHeader.messageType(),
                            false,
                            MqttQoS.AT_MOST_ONCE,
                            false,
                            mqttFixedHeader.remainingLength());
                }
                return mqttFixedHeader;
            case PUBREL:
            case SUBSCRIBE:
            case UNSUBSCRIBE:
                if (mqttFixedHeader.isRetain()) {
                    return new MqttFixedHeader(
                            mqttFixedHeader.messageType(),
                            mqttFixedHeader.isDup(),
                            mqttFixedHeader.qosLevel(),
                            false,
                            mqttFixedHeader.remainingLength());
                }
                return mqttFixedHeader;
            default:
                return mqttFixedHeader;
        }
    }

    private MqttCodecUtil() { }
}
