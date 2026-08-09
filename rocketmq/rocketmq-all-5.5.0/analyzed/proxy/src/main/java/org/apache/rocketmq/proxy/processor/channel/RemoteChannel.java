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

package org.apache.rocketmq.proxy.processor.channel;

import com.google.common.base.MoreObjects;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.apache.rocketmq.proxy.service.channel.SimpleChannel;

/**
 * 远程客户端虚拟通道：封装跨 Proxy 转发时的地址、协议与扩展属性。
 */
public class RemoteChannel extends SimpleChannel implements ChannelExtendAttributeGetter {
    /** 客户端接入协议类型。 */
    protected final ChannelProtocolType type;
    /** 客户端所连接的 Proxy 节点 IP。 */
    protected final String remoteProxyIp;
    /** 可变的通道扩展属性（如 gRPC Settings）。 */
    protected volatile String extendAttribute;

    /** 构造远程通道并生成复合 {@link RemoteChannelId}。 */
    public RemoteChannel(String remoteProxyIp, String remoteAddress, String localAddress, ChannelProtocolType type, String extendAttribute) {
        super(null,
            new RemoteChannelId(remoteProxyIp, remoteAddress, localAddress, type),
            remoteAddress, localAddress);
        this.type = type;
        this.remoteProxyIp = remoteProxyIp;
        this.extendAttribute = extendAttribute;
    }

    /** 由 Proxy IP、地址与协议拼接而成的唯一通道标识。 */
    public static class RemoteChannelId implements ChannelId {

        private final String id;

        /** 拼接 remoteProxyIp@remoteAddress@localAddress@type 作为 ID。 */
        public RemoteChannelId(String remoteProxyIp, String remoteAddress, String localAddress, ChannelProtocolType type) {
            this.id = remoteProxyIp + "@" + remoteAddress + "@" + localAddress + "@" + type;
        }

        @Override
        public String asShortText() {
            return this.id;
        }

        @Override
        public String asLongText() {
            return this.id;
        }

        @Override
        public int compareTo(ChannelId o) {
            return this.id.compareTo(o.asLongText());
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("id", id)
                .toString();
        }
    }

    @Override
    /** 远程虚拟通道不可写，响应需经原 Proxy 回传。 */
    public boolean isWritable() {
        return false;
    }

    public ChannelProtocolType getType() {
        return type;
    }

    /** 序列化为 JSON 字符串以便跨节点传递。 */
    public String encode() {
        return RemoteChannelSerializer.toJson(this);
    }

    /** 从 JSON 字符串反序列化远程通道。 */
    public static RemoteChannel decode(String data) {
        return RemoteChannelSerializer.decodeFromJson(data);
    }

    /** 若 {@link Channel} 实现 {@link RemoteChannelConverter} 则提取远程通道。 */
    public static RemoteChannel create(Channel channel) {
        if (channel instanceof RemoteChannelConverter) {
            return ((RemoteChannelConverter) channel).toRemoteChannel();
        }
        return null;
    }

    public String getRemoteProxyIp() {
        return remoteProxyIp;
    }

    public void setExtendAttribute(String extendAttribute) {
        this.extendAttribute = extendAttribute;
    }

    @Override
    public String getChannelExtendAttribute() {
        return this.extendAttribute;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("channelId", id())
            .add("type", type)
            .add("remoteProxyIp", remoteProxyIp)
            .add("extendAttribute", extendAttribute)
            .toString();
    }
}
