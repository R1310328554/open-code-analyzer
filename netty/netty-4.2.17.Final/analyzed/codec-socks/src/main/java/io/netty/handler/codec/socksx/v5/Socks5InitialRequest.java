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
package io.netty.handler.codec.socksx.v5;

import java.util.List;

/**
 * An initial SOCKS5 authentication method selection request, as defined in
 * <a href="https://tools.ietf.org/html/rfc1928#section-3">the section 3, RFC1928</a>.
 *
 * <p>SOCKS5 握手第一阶段：客户端发送 VER + NMETHOD + METHODS，
 * 服务端在 {@link Socks5InitialResponse} 中择一 {@link Socks5AuthMethod} 回复。</p>
 */
public interface Socks5InitialRequest extends Socks5Message {
    /**
     * Returns the list of desired authentication methods.
     *
     * <p>客户端支持的认证方法列表，至少一项；顺序无优先级语义，由服务端选择。</p>
     */
    List<Socks5AuthMethod> authMethods();
}
