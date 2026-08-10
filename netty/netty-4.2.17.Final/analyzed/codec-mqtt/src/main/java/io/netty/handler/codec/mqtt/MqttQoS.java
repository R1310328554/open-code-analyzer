/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.mqtt;

/**
 * MQTT 服务质量（QoS）等级，编码在固定头的 QoS 标志位中。
 * <p>0=至多一次、1=至少一次、2=恰好一次；0x80 为 SUBACK 中表示订阅被拒绝的特殊值。</p>
 */
public enum MqttQoS {
    /** QoS 0：发后即忘，无确认、无重传。 */
    AT_MOST_ONCE(0),
    /** QoS 1：至少送达一次，依赖 PUBACK 确认，可能重复。 */
    AT_LEAST_ONCE(1),
    /** QoS 2：恰好一次，四步握手（PUBREC/PUBREL/PUBCOMP）保证不重复。 */
    EXACTLY_ONCE(2),
    /** SUBACK 返回码 0x80，表示 broker 拒绝该订阅请求。 */
    FAILURE(0x80);

    private final int value;

    MqttQoS(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    /** 从协议整型值解析；非法 QoS（如 3）抛异常。 */
    public static MqttQoS valueOf(int value) {
        switch (value) {
        case 0:
            return AT_MOST_ONCE;
        case 1:
            return AT_LEAST_ONCE;
        case 2:
            return EXACTLY_ONCE;
        case 0x80:
            return FAILURE;
        default:
            throw new IllegalArgumentException("invalid QoS: " + value);
        }
    }
}
