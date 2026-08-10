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
 * Variable header of {@link MqttConnectMessage}
 * <p>CONNACK 可变头：连接返回码、session present 标志，MQTT 5 还可携带属性（如 reason string）。</p>
 */
public final class MqttConnAckVariableHeader {

    /** 服务端返回的连接结果码。 */
    private final MqttConnectReturnCode connectReturnCode;

    /** 是否复用了 broker 上已存在的会话状态（仅当 clean session=false 时有意义）。 */
    private final boolean sessionPresent;

    /** MQTT 5 扩展属性；3.x 解码路径使用 {@link MqttProperties#NO_PROPERTIES}。 */
    private final MqttProperties properties;

    public MqttConnAckVariableHeader(MqttConnectReturnCode connectReturnCode, boolean sessionPresent) {
        this(connectReturnCode, sessionPresent, MqttProperties.NO_PROPERTIES);
    }

    public MqttConnAckVariableHeader(MqttConnectReturnCode connectReturnCode, boolean sessionPresent,
                                     MqttProperties properties) {
        this.connectReturnCode = connectReturnCode;
        this.sessionPresent = sessionPresent;
        this.properties = MqttProperties.withEmptyDefaults(properties);
    }

    public MqttConnectReturnCode connectReturnCode() {
        return connectReturnCode;
    }

    public boolean isSessionPresent() {
        return sessionPresent;
    }

    public MqttProperties properties() {
        return properties;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("connectReturnCode=").append(connectReturnCode)
            .append(", sessionPresent=").append(sessionPresent)
            .append(']')
            .toString();
    }
}
