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
package org.apache.rocketmq.remoting.netty;

import io.netty.channel.Channel;

/**
 * Netty 通道生命周期事件封装：携带事件类型、远端地址与 {@link Channel} 引用。
 */
public class NettyEvent {
    /** 事件类型。 */
    private final NettyEventType type;
    /** 远端地址字符串。 */
    private final String remoteAddr;
    /** 关联的 Netty 通道。 */
    private final Channel channel;

    /** 构造通道事件对象。 */
    public NettyEvent(NettyEventType type, String remoteAddr, Channel channel) {
        this.type = type;
        this.remoteAddr = remoteAddr;
        this.channel = channel;
    }

    /** 返回事件类型。 */
    public NettyEventType getType() {
        return type;
    }

    /** 返回远端地址。 */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /** 返回关联通道。 */
    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "NettyEvent [type=" + type + ", remoteAddr=" + remoteAddr + ", channel=" + channel + "]";
    }
}
