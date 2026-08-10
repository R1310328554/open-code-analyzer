/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ObjectUtil;

/**
 * An abstract class that defines a SocksMessage, providing common properties for
 * {@link SocksRequest} and {@link SocksResponse}.
 *
 * <p>SOCKS5 消息的抽象基类：固定协议版本 {@link SocksProtocolVersion#SOCKS5}，
 * 并通过 {@link SocksMessageType} 标记请求/响应方向。
 * 具体子类实现 {@link #encodeAsByteBuf} 完成 RFC 1928 线格式序列化。</p>
 *
 * @see SocksRequest
 * @see SocksResponse
 */

public abstract class SocksMessage {
    private final SocksMessageType type;
    /** 本模块仅实现 SOCKS5；版本字节恒为 0x05。 */
    private final SocksProtocolVersion protocolVersion = SocksProtocolVersion.SOCKS5;

    protected SocksMessage(SocksMessageType type) {
        this.type = ObjectUtil.checkNotNull(type, "type");
    }

    /**
     * Returns the {@link SocksMessageType} of this {@link SocksMessage}
     *
     * @return The {@link SocksMessageType} of this {@link SocksMessage}
     */
    public SocksMessageType type() {
        return type;
    }

    /**
     * Returns the {@link SocksProtocolVersion} of this {@link SocksMessage}
     *
     * @return The {@link SocksProtocolVersion} of this {@link SocksMessage}
     */
    public SocksProtocolVersion protocolVersion() {
        return protocolVersion;
    }

    /**
     * @deprecated Do not use; this method was intended for an internal use only.
     * <p>由 {@link SocksMessageEncoder} 调用，将消息写入出站 {@link ByteBuf}；外部应使用编码器而非直接调用。</p>
     */
    @Deprecated
    public abstract void encodeAsByteBuf(ByteBuf byteBuf);
}
