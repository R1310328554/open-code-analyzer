/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.connections.httpclient;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.keycloak.common.enums.HostnameVerificationPolicy;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

/**
 * 创建 Apache HttpClient 的构建器抽象，集中配置 SSL、连接池、超时与代理。
 * <p>支持信任库/密钥库、主机名校验策略、连接复用与 {@link ProxyMappings} 路由。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpClientBuilder {

    /**
     * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
     * @version $Revision: 1 $
     */
    /** 跳过证书校验的信任管理器（仅在与 {@link #disableTrustManager()} 联用时启用）。 */
    private static class PassthroughTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    /** 默认连接池获取连接超时（毫秒）。 */
    public static long DEFAULT_CONNECTION_REQUEST_TIMEOUT_MILLIS = 5000L;

    protected KeyStore truststore;
    protected KeyStore clientKeyStore;
    protected String clientPrivateKeyPassword;
    protected boolean disableTrustManager;
    protected HostnameVerificationPolicy policy = HostnameVerificationPolicy.DEFAULT;
    protected SSLContext sslContext;
    protected int connectionPoolSize = 128;
    protected int maxPooledPerRoute = 64;
    protected long connectionTTL = -1;
    protected boolean reuseConnections = true;
    protected TimeUnit connectionTTLUnit = TimeUnit.MILLISECONDS;
    protected long maxConnectionIdleTime = 900000;
    protected TimeUnit maxConnectionIdleTimeUnit = TimeUnit.MILLISECONDS;
    protected long socketTimeout = -1;
    protected TimeUnit socketTimeoutUnits = TimeUnit.MILLISECONDS;
    protected long establishConnectionTimeout = -1;
    protected TimeUnit establishConnectionTimeoutUnits = TimeUnit.MILLISECONDS;
    protected long connectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT_MILLIS;
    protected TimeUnit connectionRequestTimeoutUnits = TimeUnit.MILLISECONDS;
    protected boolean disableCookies = false;
    /** 主机名到代理的映射规则。 */
    protected ProxyMappings proxyMappings;
    protected boolean expectContinueEnabled = false;
    protected final org.apache.http.impl.client.HttpClientBuilder apacheBuilder;

    /** 使用默认 Apache HttpClient 构建器。 */
    public HttpClientBuilder() {
        this(HttpClients.custom());
    }

    /** @param apacheBuilder 底层 Apache 构建器实例 */
    public HttpClientBuilder(org.apache.http.impl.client.HttpClientBuilder apacheBuilder) {
        this.apacheBuilder = apacheBuilder;
    }

    /**
     * 设置 Socket 读超时（无数据可读时的最长等待）。
     *
     * @param timeout
     * @param unit
     * @return
     */
    public HttpClientBuilder socketTimeout(long timeout, TimeUnit unit)
    {
        this.socketTimeout = timeout;
        this.socketTimeoutUnits = unit;
        return this;
    }

    /**
     * 建立 TCP 连接的超时时间。
     *
     * @param timeout
     * @param unit
     * @return
     */
    public HttpClientBuilder establishConnectionTimeout(long timeout, TimeUnit unit)
    {
        this.establishConnectionTimeout = timeout;
        this.establishConnectionTimeoutUnits = unit;
        return this;
    }

    /**
     * 从连接池获取连接的超时时间。
     *
     * @param timeout
     * @param unit
     * @return
     */
    public HttpClientBuilder connectionRequestTimeout(long timeout, TimeUnit unit)
    {
        this.connectionRequestTimeout = timeout;
        this.connectionRequestTimeoutUnits = unit;
        return this;
    }

    /** 设置连接在池中的存活时间（TTL）。 */
    public HttpClientBuilder connectionTTL(long ttl, TimeUnit unit) {
        this.connectionTTL = ttl;
        this.connectionTTLUnit = unit;
        return this;
    }

    /** 是否复用 HTTP 连接（Keep-Alive）。 */
    public HttpClientBuilder reuseConnections(boolean reuseConnections) {
        this.reuseConnections = reuseConnections;
        return this;
    }

    /** 空闲连接最大存活时间，超时后由后台线程清理。 */
    public HttpClientBuilder maxConnectionIdleTime(long maxConnectionIdleTime, TimeUnit unit) {
        this.maxConnectionIdleTime = maxConnectionIdleTime;
        this.maxConnectionIdleTimeUnit = unit;
        return this;
    }

    /** 每个路由（目标主机）的最大并发连接数。 */
    public HttpClientBuilder maxPooledPerRoute(int maxPooledPerRoute) {
        this.maxPooledPerRoute = maxPooledPerRoute;
        return this;
    }

    /** 连接池总容量上限。 */
    public HttpClientBuilder connectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
        return this;
    }

    /**
     * 禁用信任管理与主机名校验。<i>注意</i>：存在中间人攻击风险，仅在无法或无需校验对端身份时使用。
     */
    public HttpClientBuilder disableTrustManager() {
        this.disableTrustManager = true;
        return this;
    }

    /**
     * 禁用 HTTP 自动重定向跟随。
     * @return
     */
    public HttpClientBuilder disableRedirectHandling() {
        apacheBuilder.disableRedirectHandling();
        return this;
    }

    /**
     * 禁用 HttpClient 内置 Cookie 管理。
     */
    public HttpClientBuilder disableCookies(boolean disable) {
        this.disableCookies = disable;
        return this;
    }

    /**
     * 设置 SSL 主机名校验策略。
     *
     * @param policy
     * @return
     */
    public HttpClientBuilder hostnameVerification(HostnameVerificationPolicy policy) {
        this.policy = policy;
        return this;
    }


    /** 注入自定义 {@link SSLContext}。 */
    public HttpClientBuilder sslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    /** 设置信任 CA 证书库。 */
    public HttpClientBuilder trustStore(KeyStore truststore) {
        this.truststore = truststore;
        return this;
    }

    /** 设置客户端 TLS 密钥库及私钥口令。 */
    public HttpClientBuilder keyStore(KeyStore keyStore, String password) {
        this.clientKeyStore = keyStore;
        this.clientPrivateKeyPassword = password;
        return this;
    }

    public HttpClientBuilder keyStore(KeyStore keyStore, char[] password) {
        this.clientKeyStore = keyStore;
        this.clientPrivateKeyPassword = new String(password);
        return this;
    }

    /** 配置按主机名匹配的 HTTP 代理映射。 */
    public HttpClientBuilder proxyMappings(ProxyMappings proxyMappings) {
        this.proxyMappings = proxyMappings;
        return this;
    }

    /** 是否启用 HTTP Expect: 100-continue 握手。 */
    public HttpClientBuilder expectContinueEnabled(boolean expectContinueEnabled) {
        this.expectContinueEnabled = expectContinueEnabled;
        return this;
    }

    /** 根据当前配置构建 {@link CloseableHttpClient} 实例。 */
    public CloseableHttpClient build() {
        HostnameVerifier verifier = null;
        switch (policy) {
            case ANY:
                verifier = new NoopHostnameVerifier();
                break;
            case WILDCARD:
                verifier = new BrowserCompatHostnameVerifier();
                break;
            case STRICT:
                verifier = new StrictHostnameVerifier();
                break;
            case DEFAULT:
                verifier = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
                break;
        }
        try {
            SSLConnectionSocketFactory sslsf = null;
            SSLContext theContext = sslContext;
            if (disableTrustManager) {
                theContext = SSLContext.getInstance("TLS");
                theContext.init(null, new TrustManager[]{new PassthroughTrustManager()},
                        new SecureRandom());
                verifier = new NoopHostnameVerifier();
                sslsf = new SSLConnectionSocketFactory(theContext, verifier);
            } else if (theContext != null) {
                sslsf = new SSLConnectionSocketFactory(theContext, verifier);
            } else if (clientKeyStore != null || truststore != null) {
                theContext = createSslContext("TLS", clientKeyStore, clientPrivateKeyPassword, truststore, null);
                sslsf = new SSLConnectionSocketFactory(theContext, verifier);
            } else {
                final SSLContext tlsContext = SSLContext.getInstance("TLS");
                tlsContext.init(null, null, null);
                sslsf = new SSLConnectionSocketFactory(tlsContext, verifier);
            }

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout((int) TimeUnit.MILLISECONDS.convert(establishConnectionTimeout, establishConnectionTimeoutUnits))
                    .setSocketTimeout((int) TimeUnit.MILLISECONDS.convert(socketTimeout, socketTimeoutUnits))
                    .setConnectionRequestTimeout((int) TimeUnit.MILLISECONDS.convert(connectionRequestTimeout, connectionRequestTimeoutUnits))
                    .setExpectContinueEnabled(expectContinueEnabled).build();

            org.apache.http.impl.client.HttpClientBuilder builder = getApacheHttpClientBuilder()
                    .setDefaultRequestConfig(requestConfig)
                    .setSSLSocketFactory(sslsf)
                    .setMaxConnTotal(connectionPoolSize)
                    .setMaxConnPerRoute(maxPooledPerRoute)
                    .setConnectionTimeToLive(connectionTTL, connectionTTLUnit);

            if (!reuseConnections) {
                builder.setConnectionReuseStrategy(new NoConnectionReuseStrategy());
            }

            if (proxyMappings != null && !proxyMappings.isEmpty()) {
                builder.setRoutePlanner(new ProxyMappingsAwareRoutePlanner(proxyMappings));
            }

            if (maxConnectionIdleTime > 0) {
                // 启动后台线程清理空闲连接
                builder.evictIdleConnections(maxConnectionIdleTime, maxConnectionIdleTimeUnit);
            }

            if (disableCookies) builder.disableCookieManagement();

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** @return 底层 Apache HttpClient 构建器（供子类扩展） */
    protected org.apache.http.impl.client.HttpClientBuilder getApacheHttpClientBuilder() {
        return apacheBuilder;
    }

    private SSLContext createSslContext(
            final String algorithm,
            final KeyStore keystore,
            final String keyPassword,
            final KeyStore truststore,
            final SecureRandom random)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        return SSLContexts.custom()
                        .setProtocol(algorithm)
                        .setSecureRandom(random)
                        .loadKeyMaterial(keystore, keyPassword != null ? keyPassword.toCharArray() : null)
                        .loadTrustMaterial(truststore, null)
                        .build();
    }

}
