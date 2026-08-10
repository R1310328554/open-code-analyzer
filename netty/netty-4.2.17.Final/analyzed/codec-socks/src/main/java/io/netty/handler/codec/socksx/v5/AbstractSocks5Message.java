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

package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.socksx.AbstractSocksMessage;
import io.netty.handler.codec.socksx.SocksVersion;

/**
 * An abstract {@link Socks5Message}.
 *
 * <p>SOCKS5 消息的抽象基类：固定协议版本为 {@link SocksVersion#SOCKS5}（线格式 VER=0x05），
 * 并继承 {@link AbstractSocksMessage} 的解码结果与引用计数语义。</p>
 */
public abstract class AbstractSocks5Message extends AbstractSocksMessage implements Socks5Message {
    @Override
    public final SocksVersion version() {
        // RFC 1928 规定 SOCKS5 版本字节为 0x05
        return SocksVersion.SOCKS5;
    }
}
