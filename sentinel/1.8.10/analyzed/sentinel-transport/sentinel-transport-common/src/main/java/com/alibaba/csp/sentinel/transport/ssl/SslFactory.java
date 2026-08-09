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
package com.alibaba.csp.sentinel.transport.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * HTTPS 传输用 SSL 上下文工厂：提供信任所有证书的 {@link SSLContext}（内网 Dashboard 场景）。
 * 使用静态内部类实现懒加载单例。
 *
 * @author Leo Li
 */
public class SslFactory {

    /** 持有单例 SSLContext 的静态内部类。 */
    private static class SslContextInstance {
        private static final SSLContext SSL_CONTEXT = initSslContext();
    }

    /** 初始化 TLS 上下文并注册信任所有证书的 TrustManager。 */
    private static SSLContext initSslContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            X509TrustManager x509TrustManager = new X509TrustManager() {
                public boolean isServerTrusted(X509Certificate[] certs) {
                    return true;
                }

                public boolean isClientTrusted(X509Certificate[] certs) {
                    return true;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { x509TrustManager }, null);
        } catch (Exception e) {
            RecordLog.error("get ssl socket factory error", e);
        }
        return sslContext;
    }

    /** @return 全局 SSLContext 单例。 */
    public static SSLContext getSslConnectionSocketFactory() {
        return SslContextInstance.SSL_CONTEXT;
    }
}
