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
 * Variable Header for the {@link MqttConnectMessage}
 * <p>CONNECT 可变头：协议名/级别、连接标志（遗嘱、clean session、认证位）、keep-alive 及 MQTT 5 属性。</p>
 */
public final class MqttConnectVariableHeader {

    /** 协议名，如 "MQTT" 或历史 "MQIsdp"。 */
    private final String name;
    /** 协议级别字节，与 {@link MqttVersion} 对应。 */
    private final int version;
    private final boolean hasUserName;
    private final boolean hasPassword;
    private final boolean isWillRetain;
    private final int willQos;
    /** 是否设置遗嘱消息（客户端异常断开时 broker 代为发布）。 */
    private final boolean isWillFlag;
    /** true 表示 clean start，不恢复持久会话。 */
    private final boolean isCleanSession;
    /** 心跳间隔（秒），0 表示禁用 keep-alive。 */
    private final int keepAliveTimeSeconds;
    private final MqttProperties properties;

    public MqttConnectVariableHeader(
            String name,
            int version,
            boolean hasUserName,
            boolean hasPassword,
            boolean isWillRetain,
            int willQos,
            boolean isWillFlag,
            boolean isCleanSession,
            int keepAliveTimeSeconds) {
        this(name,
                version,
                hasUserName,
                hasPassword,
                isWillRetain,
                willQos,
                isWillFlag,
                isCleanSession,
                keepAliveTimeSeconds,
                MqttProperties.NO_PROPERTIES);
    }

    public MqttConnectVariableHeader(
            String name,
            int version,
            boolean hasUserName,
            boolean hasPassword,
            boolean isWillRetain,
            int willQos,
            boolean isWillFlag,
            boolean isCleanSession,
            int keepAliveTimeSeconds,
            MqttProperties properties) {
        this.name = name;
        this.version = version;
        this.hasUserName = hasUserName;
        this.hasPassword = hasPassword;
        this.isWillRetain = isWillRetain;
        this.willQos = willQos;
        this.isWillFlag = isWillFlag;
        this.isCleanSession = isCleanSession;
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
        this.properties = MqttProperties.withEmptyDefaults(properties);
    }

    public String name() {
        return name;
    }

    public int version() {
        return version;
    }

    public boolean hasUserName() {
        return hasUserName;
    }

    public boolean hasPassword() {
        return hasPassword;
    }

    public boolean isWillRetain() {
        return isWillRetain;
    }

    public int willQos() {
        return willQos;
    }

    public boolean isWillFlag() {
        return isWillFlag;
    }

    public boolean isCleanSession() {
        return isCleanSession;
    }

    public int keepAliveTimeSeconds() {
        return keepAliveTimeSeconds;
    }

    public MqttProperties properties() {
        return properties;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("name=").append(name)
            .append(", version=").append(version)
            .append(", hasUserName=").append(hasUserName)
            .append(", hasPassword=").append(hasPassword)
            .append(", isWillRetain=").append(isWillRetain)
            .append(", isWillFlag=").append(isWillFlag)
            .append(", isCleanSession=").append(isCleanSession)
            .append(", keepAliveTimeSeconds=").append(keepAliveTimeSeconds)
            .append(']')
            .toString();
    }
}
