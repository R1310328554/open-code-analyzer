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

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.StringUtil;

/**
 * Base class for all MQTT message types.
 * <p>所有 MQTT 报文的抽象基类，统一承载固定头、可变头与载荷三段结构；
 * 解码失败时 {@link #decoderResult()} 会携带错误信息而非抛出异常。</p>
 */
public class MqttMessage {

    /** 固定头：消息类型、QoS、DUP、Retain 及剩余长度。 */
    private final MqttFixedHeader mqttFixedHeader;
    /** 可变头，具体类型随 {@link MqttMessageType} 变化（如 CONNECT 的协议名、PUBLISH 的主题名）。 */
    private final Object variableHeader;
    /** 载荷，CONNECT/SUBSCRIBE 等为结构化对象，PUBLISH 为 {@link io.netty.buffer.ByteBuf}。 */
    private final Object payload;
    /** 解码结果；成功时为 SUCCESS，帧格式非法时记录失败原因。 */
    private final DecoderResult decoderResult;

    // Constants for fixed-header only message types with all flags set to 0 (see
    // https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Table_2.2_-)
    // 以下三种报文仅有固定头、无可变头与载荷，故预建单例避免重复分配。
    /** 客户端心跳请求，broker 应以 PINGRESP 应答。 */
    public static final MqttMessage PINGREQ = new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGREQ, false,
            MqttQoS.AT_MOST_ONCE, false, 0));

    /** 服务端对 PINGREQ 的心跳响应。 */
    public static final MqttMessage PINGRESP = new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGRESP, false,
            MqttQoS.AT_MOST_ONCE, false, 0));

    /** 断开连接通知（MQTT 3.1.1 无载荷；v5 可变头可含原因码与属性）。 */
    public static final MqttMessage DISCONNECT = new MqttMessage(new MqttFixedHeader(MqttMessageType.DISCONNECT, false,
            MqttQoS.AT_MOST_ONCE, false, 0));

    public MqttMessage(MqttFixedHeader mqttFixedHeader) {
        this(mqttFixedHeader, null, null);
    }

    public MqttMessage(MqttFixedHeader mqttFixedHeader, Object variableHeader) {
        this(mqttFixedHeader, variableHeader, null);
    }

    public MqttMessage(MqttFixedHeader mqttFixedHeader, Object variableHeader, Object payload) {
        this(mqttFixedHeader, variableHeader, payload, DecoderResult.SUCCESS);
    }

    public MqttMessage(
            MqttFixedHeader mqttFixedHeader,
            Object variableHeader,
            Object payload,
            DecoderResult decoderResult) {
        this.mqttFixedHeader = mqttFixedHeader;
        this.variableHeader = variableHeader;
        this.payload = payload;
        this.decoderResult = decoderResult;
    }

    public MqttFixedHeader fixedHeader() {
        return mqttFixedHeader;
    }

    public Object variableHeader() {
        return variableHeader;
    }

    public Object payload() {
        return payload;
    }

    public DecoderResult decoderResult() {
        return decoderResult;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("fixedHeader=").append(fixedHeader() != null ? fixedHeader().toString() : "")
            .append(", variableHeader=").append(variableHeader() != null ? variableHeader.toString() : "")
            .append(", payload=").append(payload() != null ? payload.toString() : "")
            .append(']')
            .toString();
    }
}
