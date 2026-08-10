/*
 * Copyright 2015 The Netty Project
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

package io.netty.handler.ssl;

/**
 * Provides a way to get the application-level protocol name from ALPN or NPN.
 *
 * <p>从 ALPN/NPN 协商结果读取应用层协议名（如 {@code h2}、{@code http/1.1}）的统一访问接口，
 * 由 {@link JdkAlpnSslEngine} 等 {@link SSLEngine} 包装类实现。</p>
 */
interface ApplicationProtocolAccessor {
    /**
     * Returns the name of the negotiated application-level protocol.
     *
     * @return the application-level protocol name or
     *         {@code null} if the negotiation failed or the client does not have ALPN/NPN extension
     *
     * <p>返回协商后的协议名；未协商或客户端不支持扩展时为 {@code null}。</p>
     */
    String getNegotiatedApplicationProtocol();
}
