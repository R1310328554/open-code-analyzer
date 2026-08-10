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

import io.netty.util.internal.ObjectUtil;

/**
 * An abstract class that defines a SocksRequest, providing common properties for
 * {@link SocksInitRequest}, {@link SocksAuthRequest}, {@link SocksCmdRequest} and {@link UnknownSocksRequest}.
 *
 * <p>SOCKS5 客户端出站消息的抽象基类。构造时固定 {@link SocksMessageType#REQUEST}，
 * 并通过 {@link SocksRequestType} 区分握手、认证、命令三个阶段。
 * 子类实现 {@link SocksMessage#encodeAsByteBuf} 完成 RFC 1928/1929 线格式序列化。</p>
 *
 * @see SocksInitRequest
 * @see SocksAuthRequest
 * @see SocksCmdRequest
 * @see UnknownSocksRequest
 */
public abstract class SocksRequest extends SocksMessage {
    /** 请求所处 SOCKS5 状态机阶段（INIT / AUTH / CMD / UNKNOWN）。 */
    private final SocksRequestType requestType;

    protected SocksRequest(SocksRequestType requestType) {
        super(SocksMessageType.REQUEST);
        this.requestType = ObjectUtil.checkNotNull(requestType, "requestType");
    }

    /**
     * Returns socks request type
     *
     * @return socks request type
     */
    public SocksRequestType requestType() {
        return requestType;
    }
}
