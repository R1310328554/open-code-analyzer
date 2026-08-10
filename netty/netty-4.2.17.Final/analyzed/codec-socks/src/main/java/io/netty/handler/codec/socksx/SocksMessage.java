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
package io.netty.handler.codec.socksx;

import io.netty.handler.codec.DecoderResultProvider;

/**
 * An interface that all SOCKS protocol messages implement.
 *
 * <p>{@code socksx} 模块的统一消息根接口，覆盖 SOCKS4/4a 与 SOCKS5 各阶段报文。
 * 继承 {@link DecoderResultProvider} 以支持"尽力解析"语义；
 * {@link #version()} 返回报文所属协议版本，便于多版本 pipeline 路由。</p>
 */
public interface SocksMessage extends DecoderResultProvider {

    /**
     * Returns the protocol version of this message.
     *
     * <p>对应线格式首字节 VER 字段的语义化枚举（{@link SocksVersion}）。</p>
     */
    SocksVersion version();
}
