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

import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;

/**
 * Mqtt version specific constant values used by multiple classes in mqtt-codec.
 * <p>协议版本枚举：CONNECT 可变头中的协议名 + 级别二元组唯一确定版本。
 * 3.1 用 {@code MQIsdp}/3，3.1.1 与 5.0 均用 {@code MQTT} 但级别分别为 4 和 5。</p>
 */
public enum MqttVersion {
    MQTT_3_1("MQIsdp", (byte) 3),
    MQTT_3_1_1("MQTT", (byte) 4),
    MQTT_5("MQTT", (byte) 5);

    private final String name;
    private final byte level;

    MqttVersion(String protocolName, byte protocolLevel) {
        name = ObjectUtil.checkNotNull(protocolName, "protocolName");
        level = protocolLevel;
    }

    public String protocolName() {
        return name;
    }

    /** 供编码器直接写入 CONNECT 可变头的 UTF-8 字节。 */
    public byte[] protocolNameBytes() {
        return name.getBytes(CharsetUtil.UTF_8);
    }

    public byte protocolLevel() {
        return level;
    }

    /**
     * 根据 CONNECT 中的协议名与级别解析版本；名/级不匹配时抛 {@link MqttUnacceptableProtocolVersionException}。
     */
    public static MqttVersion fromProtocolNameAndLevel(String protocolName, byte protocolLevel) {
        MqttVersion mv;
        switch (protocolLevel) {
        case 3:
            mv = MQTT_3_1;
            break;
        case 4:
            mv = MQTT_3_1_1;
            break;
        case 5:
            mv = MQTT_5;
            break;
        default:
            throw new MqttUnacceptableProtocolVersionException(protocolName + " is an unknown protocol name");
        }
        if (mv.name.equals(protocolName)) {
            return mv;
        }
        // 级别合法但协议名不符（如 "MQTT" + 3）同样拒绝
        throw new MqttUnacceptableProtocolVersionException(protocolName + " and " + protocolLevel + " don't match");
    }
}
