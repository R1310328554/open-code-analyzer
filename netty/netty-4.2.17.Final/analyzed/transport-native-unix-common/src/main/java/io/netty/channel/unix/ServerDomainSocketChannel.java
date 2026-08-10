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
package io.netty.channel.unix;

import io.netty.channel.ServerChannel;

/**
 * {@link ServerChannel} that accepts {@link DomainSocketChannel}'s via
 * <a href="https://en.wikipedia.org/wiki/Unix_domain_socket">Unix Domain Socket</a>.
 * <p>Unix 域监听服务端通道：{@code accept} 返回流式 {@link DomainSocketChannel}； 本地/远程地址均为 {@link DomainSocketAddress}。</p>
 */
public interface ServerDomainSocketChannel extends ServerChannel, UnixChannel {
    /** 已接受连接的对端 Unix 域路径 */
    @Override
    DomainSocketAddress remoteAddress();

    /** 监听绑定的 Unix 域路径 */
    @Override
    DomainSocketAddress localAddress();
}
