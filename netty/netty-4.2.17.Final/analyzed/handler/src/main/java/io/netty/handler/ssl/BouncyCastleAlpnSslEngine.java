/*
 * Copyright 2021 The Netty Project
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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import javax.net.ssl.SSLEngine;

/**
 * 基于 Bouncy Castle JSSE 的 ALPN {@link SSLEngine} 包装，委托 {@link BouncyCastleAlpnSslUtils} 反射调用 BC API。
 *
 * <p>在 JDK 未内置 ALPN 或选用 BC 提供程序时，通过此类与 {@link JdkAlpnSslEngine} 相同方式参与
 * ClientHello/ServerHello 中的应用层协议协商。</p>
 */
final class BouncyCastleAlpnSslEngine extends JdkAlpnSslEngine {

    /**
     * @param engine 底层 BC {@link SSLEngine}
     * @param applicationNegotiator Netty 侧 ALPN 协商策略
     * @param isServer 是否为服务端（影响 selector 与 advertised 列表角色）
     */
    BouncyCastleAlpnSslEngine(SSLEngine engine,
                     @SuppressWarnings("deprecation") JdkApplicationProtocolNegotiator applicationNegotiator,
                     boolean isServer) {
        super(engine, applicationNegotiator, isServer,
                new BiConsumer<SSLEngine, AlpnSelector>() {
                    @Override
                    public void accept(SSLEngine e, AlpnSelector s) {
                        // 服务端：注册 BC 握手期协议选择器
                        BouncyCastleAlpnSslUtils.setHandshakeApplicationProtocolSelector(e, s);
                    }
                },
                new BiConsumer<SSLEngine, List<String>>() {
                    @Override
                    public void accept(SSLEngine e, List<String> p) {
                        // 客户端：设置本端 advertised 协议列表
                        BouncyCastleAlpnSslUtils.setApplicationProtocols(e, p);
                    }
                });
    }

    /** 返回当前连接已协商的应用层协议（握手完成后）。 */
    public String getApplicationProtocol() {
        return BouncyCastleAlpnSslUtils.getApplicationProtocol(getWrappedEngine());
    }

    /** 返回握手过程中临时选定的协议（握手完成前可读）。 */
    public String getHandshakeApplicationProtocol() {
        return BouncyCastleAlpnSslUtils.getHandshakeApplicationProtocol(getWrappedEngine());
    }

    /** 设置服务端在 ClientHello 后从客户端列表中选择协议的回调。 */
    public void setHandshakeApplicationProtocolSelector(BiFunction<SSLEngine, List<String>, String> selector) {
        BouncyCastleAlpnSslUtils.setHandshakeApplicationProtocolSelector(getWrappedEngine(), selector);
    }

    /** 获取当前注册的握手期协议选择器。 */
    public BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector() {
        return BouncyCastleAlpnSslUtils.getHandshakeApplicationProtocolSelector(getWrappedEngine());
    }

}
