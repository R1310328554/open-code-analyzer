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
 * Variable Header containing only Message Id
 * See <a href="https://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#msg-id">MQTTV3.1/msg-id</a>
 * <p>仅含 16 位报文标识符的可变头。QoS&gt;0 的 PUBLISH 及 SUBSCRIBE/PUBACK 等
 * 请求-响应型报文用同一 ID 配对，取值范围 1～65535（0 保留）。</p>
 */
public class MqttMessageIdVariableHeader {

    /** 报文标识符，用于关联 PUBLISH 与其 PUBACK/PUBREC 等应答。 */
    private final int messageId;

    /** 校验范围后创建实例，非法 ID 立即抛异常。 */
    public static MqttMessageIdVariableHeader from(int messageId) {
      if (messageId < 1 || messageId > 0xffff) {
        throw new IllegalArgumentException("messageId: " + messageId + " (expected: 1 ~ 65535)");
      }
      return new MqttMessageIdVariableHeader(messageId);
    }

    protected MqttMessageIdVariableHeader(int messageId) {
        this.messageId = messageId;
    }

    public int messageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("messageId=").append(messageId)
            .append(']')
            .toString();
    }

    /** 升级为 v5 可变头，附加空属性集。 */
    public MqttMessageIdAndPropertiesVariableHeader withEmptyProperties() {
        return new MqttMessageIdAndPropertiesVariableHeader(messageId, MqttProperties.NO_PROPERTIES);
    }

    MqttMessageIdAndPropertiesVariableHeader withDefaultEmptyProperties() {
        return withEmptyProperties();
    }
}
