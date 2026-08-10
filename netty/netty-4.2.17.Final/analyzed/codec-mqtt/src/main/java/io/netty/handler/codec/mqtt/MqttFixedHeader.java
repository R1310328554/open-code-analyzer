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

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * See <a href="https://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#fixed-header">
 *     MQTTV3.1/fixed-header</a>
 * <p>MQTT 固定头：消息类型、DUP/QoS/Retain 标志，以及剩余长度（可变头+载荷的字节数）。</p>
 */
public final class MqttFixedHeader {

    private final MqttMessageType messageType;
    /** 重传标志，QoS&gt;0 时标识是否为重复投递。 */
    private final boolean isDup;
    private final MqttQoS qosLevel;
    /** PUBLISH 时 broker 是否应保留该消息。 */
    private final boolean isRetain;
    /** 本包剩余部分（可变头+payload）的字节长度。 */
    private final int remainingLength;

    public MqttFixedHeader(
            MqttMessageType messageType,
            boolean isDup,
            MqttQoS qosLevel,
            boolean isRetain,
            int remainingLength) {
        this.messageType = ObjectUtil.checkNotNull(messageType, "messageType");
        this.isDup = isDup;
        this.qosLevel = ObjectUtil.checkNotNull(qosLevel, "qosLevel");
        this.isRetain = isRetain;
        this.remainingLength = remainingLength;
    }

    public MqttMessageType messageType() {
        return messageType;
    }

    public boolean isDup() {
        return isDup;
    }

    public MqttQoS qosLevel() {
        return qosLevel;
    }

    public boolean isRetain() {
        return isRetain;
    }

    public int remainingLength() {
        return remainingLength;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("messageType=").append(messageType)
            .append(", isDup=").append(isDup)
            .append(", qosLevel=").append(qosLevel)
            .append(", isRetain=").append(isRetain)
            .append(", remainingLength=").append(remainingLength)
            .append(']')
            .toString();
    }
}
