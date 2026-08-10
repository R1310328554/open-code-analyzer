/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.tls;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * 客户端 {@link SSLContext} 构建工具：读取 {@link TlsSystemConfig} 中的 TLS 开关、
 * 客户端认证及信任证书路径，委托 {@link SelfTrustManager} 初始化 TLS 协议上下文。
 * 当前仅支持客户端侧；下方示例展示无认证与单向认证的系统属性配置方式。
 * Utils for build {@link SSLContext}.
 *
 * <p>Currently only supports client-side
 *
 * <h3>Making your client support TLS without authentication</h3>
 * <pre>
 * System.setProperty({@link TlsSystemConfig#TLS_ENABLE}, "true");
 * </pre>
 *
 * <h3>Making your client support TLS one-way authentication</h3>
 *
 * <pre>
 * System.setProperty({@link TlsSystemConfig#TLS_ENABLE}, "true");
 * System.setProperty({@link TlsSystemConfig#CLIENT_AUTH}, "true");
 * System.setProperty({@link TlsSystemConfig#CLIENT_TRUST_CERT}, "trustCert");
 * </pre>
 *
 * @author wangwei
 * @date 2020/8/19 2:59 PM
 */
public final class TlsHelper {
    
    /**
     * 构建 TLS {@link SSLContext}，TrustManager 由 {@link SelfTrustManager#trustManager} 提供。
     *
     * <p>示例：</p>
     * <code>HttpsURLConnection.setDefaultSSLSocketFactory(TlsHelper.buildSslContext(true).getSocketFactory());</code>
     *
     * @param forClient 是否为客户端场景（当前实现均按客户端配置）
     * @return 已初始化的 SSLContext
     * @throws NoSuchAlgorithmException 不支持的 TLS 算法
     * @throws KeyManagementException   密钥管理初始化失败
     */
    public static SSLContext buildSslContext(boolean forClient)
        throws NoSuchAlgorithmException, KeyManagementException {
        
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, SelfTrustManager
            .trustManager(TlsSystemConfig.tlsClientAuthServer,
                TlsSystemConfig.tlsClientTrustCertPath),
            new java.security.SecureRandom());
        return sslcontext;
    }
}
