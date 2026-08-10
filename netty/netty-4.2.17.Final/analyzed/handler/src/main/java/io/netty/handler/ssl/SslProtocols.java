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

/**
 * SSL/TLS 协议版本字符串常量。
 * <p>取值与 JDK {@link javax.net.ssl.SSLContext} / {@link javax.net.ssl.SSLEngine}
 * 的 {@code getSupportedProtocols()} 命名一致，供 {@link SslContextBuilder} 等配置启用协议时使用。</p>
 */
public final class SslProtocols {

    /**
     * SSL v2 Hello
     * <p>兼容探测用的 SSLv2 ClientHello 标识；已不安全，勿在生产启用。</p>
     *
     * @deprecated SSLv2Hello is no longer secure. Consider using {@link #TLS_v1_2} or {@link #TLS_v1_3}
     */
    @Deprecated
    public static final String SSL_v2_HELLO = "SSLv2Hello";

    /**
     * SSL v2
     * <p>SSL 2.0 协议名；已废弃。</p>
     *
     * @deprecated SSLv2 is no longer secure. Consider using {@link #TLS_v1_2} or {@link #TLS_v1_3}
     */
    @Deprecated
    public static final String SSL_v2 = "SSLv2";

    /**
     * SSLv3
     * <p>SSL 3.0 协议名；POODLE 等漏洞后应禁用。</p>
     *
     * @deprecated SSLv3 is no longer secure. Consider using {@link #TLS_v1_2} or {@link #TLS_v1_3}
     */
    @Deprecated
    public static final String SSL_v3 = "SSLv3";

    /**
     * TLS v1
     * <p>TLS 1.0 协议名。</p>
     *
     * @deprecated TLSv1 is no longer secure. Consider using {@link #TLS_v1_2} or {@link #TLS_v1_3}
     */
    @Deprecated
    public static final String TLS_v1 = "TLSv1";

    /**
     * TLS v1.1
     * <p>TLS 1.1 协议名。</p>
     *
     * @deprecated TLSv1.1 is no longer secure. Consider using {@link #TLS_v1_2} or {@link #TLS_v1_3}
     */
    @Deprecated
    public static final String TLS_v1_1 = "TLSv1.1";

    /**
     * TLS v1.2
     * <p>TLS 1.2；当前广泛部署的安全基线之一。</p>
     */
    public static final String TLS_v1_2 = "TLSv1.2";

    /**
     * TLS v1.3
     * <p>TLS 1.3；握手更快、默认密码套件更现代。</p>
     */
    public static final String TLS_v1_3 = "TLSv1.3";

    private SslProtocols() {
        // Prevent outside initialization — 禁止实例化
    }
}
