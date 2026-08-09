/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.broker.client;

import io.netty.channel.Channel;
import org.apache.rocketmq.remoting.protocol.LanguageCode;

/**
 * 客户端通道元数据：绑定 Netty {@link Channel} 与 clientId、语言、协议版本及心跳时间戳。
 */
public class ClientChannelInfo {
    private final Channel channel;
    private final String clientId;
    private final LanguageCode language;
    private final int version;
    private volatile long lastUpdateTimestamp = System.currentTimeMillis();

    /** 仅绑定通道，其余字段使用默认值。 */
    public ClientChannelInfo(Channel channel) {
        this(channel, null, null, 0);
    }

    /** 构造完整客户端通道描述。 */
    public ClientChannelInfo(Channel channel, String clientId, LanguageCode language, int version) {
        this.channel = channel;
        this.clientId = clientId;
        this.language = language;
        this.version = version;
    }

    /** 返回底层 Netty 通道。 */
    public Channel getChannel() {
        return channel;
    }

    /** 返回客户端唯一标识。 */
    public String getClientId() {
        return clientId;
    }

    /** 返回客户端 SDK 语言类型。 */
    public LanguageCode getLanguage() {
        return language;
    }

    /** 返回 Remoting 协议版本号。 */
    public int getVersion() {
        return version;
    }

    /** 返回最近一次心跳或注册更新时间戳（毫秒）。 */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /** 更新最近活跃时间戳。 */
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channel == null) ? 0 : channel.hashCode());
        result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
        result = prime * result + ((language == null) ? 0 : language.hashCode());
        result = prime * result + (int) (lastUpdateTimestamp ^ (lastUpdateTimestamp >>> 32));
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClientChannelInfo other = (ClientChannelInfo) obj;
        if (channel == null) {
            if (other.channel != null)
                return false;
        } else if (this.channel != other.channel) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "ClientChannelInfo [channel=" + channel + ", clientId=" + clientId + ", language=" + language
            + ", version=" + version + ", lastUpdateTimestamp=" + lastUpdateTimestamp + "]";
    }
}
