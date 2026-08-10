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

package org.keycloak.adapters.cloned;

import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.keycloak.common.util.EnvUtil;
import org.keycloak.common.util.KeystoreUtil;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

/**
 * 创建 {@link HttpClient} 的构建器抽象，支持 SSL/TLS 与连接池配置。
 *
 * <p>供 SAML 适配器与 IdP 建立 HTTPS 通信时使用，可配置信任库、客户端证书、
 * 主机名校验策略、代理及超时等参数。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpClientBuilder {
    /**
     * TLS 主机名校验策略。
     */
    public static enum HostnameVerificationPolicy {
        /**
         * 不校验服务端证书中的主机名
         */
        ANY,
        /**
         * 允许子域名通配符，例如 *.foo.com
         */
        WILDCARD,
        /**
         * 证书的 CN 必须与连接目标主机名完全匹配
         */
        STRICT
    }


    /**
     * 透传式信任管理器：接受所有服务端证书（禁用校验时使用）。
     *
     * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
     * @version $Revision: 1 $
     */
    private static class PassthroughTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    protected KeyStore truststore;
    protected KeyStore clientKeyStore;
    protected String clientPrivateKeyPassword;
    protected boolean disableTrustManager;
    protected boolean disableCookieCache = true;
    protected HostnameVerificationPolicy policy = HostnameVerificationPolicy.WILDCARD;
    protected SSLContext sslContext;
    protected int connectionPoolSize = 100;
    protected int maxPooledPerRoute = 0;
    protected long connectionTTL = -1;
    protected TimeUnit connectionTTLUnit = TimeUnit.MILLISECONDS;
    protected HostnameVerifier verifier = null;
    protected long socketTimeout = -1;
    protected TimeUnit socketTimeoutUnits = TimeUnit.MILLISECONDS;
    protected long establishConnectionTimeout = -1;
    protected TimeUnit establishConnectionTimeoutUnits = TimeUnit.MILLISECONDS;
    protected HttpHost proxyHost;


    /**
     * 设置套接字无数据可读时的超时时间。
     *
     * @param timeout 超时数值
     * @param unit 时间单位
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder socketTimeout(long timeout, TimeUnit unit) {
        this.socketTimeout = timeout;
        this.socketTimeoutUnits = unit;
        return this;
    }

    /**
     * 设置首次建立 TCP 连接的超时时间。
     *
     * @param timeout 超时数值
     * @param unit 时间单位
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder establishConnectionTimeout(long timeout, TimeUnit unit) {
        this.establishConnectionTimeout = timeout;
        this.establishConnectionTimeoutUnits = unit;
        return this;
    }

    /**
     * 设置连接在连接池中的存活时间（TTL）。
     *
     * @param ttl 存活时间数值
     * @param unit 时间单位
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder connectionTTL(long ttl, TimeUnit unit) {
        this.connectionTTL = ttl;
        this.connectionTTLUnit = unit;
        return this;
    }

    /**
     * 设置每个路由（目标主机）的最大并发连接数。
     *
     * @param maxPooledPerRoute 每路由最大连接数
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder maxPooledPerRoute(int maxPooledPerRoute) {
        this.maxPooledPerRoute = maxPooledPerRoute;
        return this;
    }

    /**
     * 设置连接池总容量。
     *
     * @param connectionPoolSize 连接池大小；0 表示不使用连接池
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder connectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
        return this;
    }

    /**
     * 禁用信任管理器与主机名校验。
     *
     * <p><i>注意</i>：此选项存在安全风险，仅在无法或不需要验证通信对端身份时使用。</p>
     *
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder disableTrustManager() {
        this.disableTrustManager = true;
        return this;
    }

    /**
     * 禁用 HTTP Cookie 缓存（负载均衡场景下避免会话粘连）。
     *
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder disableCookieCache() {
        this.disableCookieCache = true;
        return this;
    }

    /**
     * 设置 SSL 主机名校验策略。
     *
     * @param policy 校验策略
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder hostnameVerification(HostnameVerificationPolicy policy) {
        this.policy = policy;
        return this;
    }


    /**
     * 设置自定义 {@link SSLContext}。
     *
     * @param sslContext SSL 上下文
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder sslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    /**
     * 设置服务端证书信任库。
     *
     * @param truststore 信任库
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder trustStore(KeyStore truststore) {
        this.truststore = truststore;
        return this;
    }

    /**
     * 设置客户端双向 TLS 密钥库及密码。
     *
     * @param keyStore 客户端密钥库
     * @param password 密钥库密码
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder keyStore(KeyStore keyStore, String password) {
        this.clientKeyStore = keyStore;
        this.clientPrivateKeyPassword = password;
        return this;
    }

    /**
     * 设置客户端双向 TLS 密钥库及密码（字符数组形式）。
     *
     * @param keyStore 客户端密钥库
     * @param password 密钥库密码
     * @return 当前构建器（链式调用）
     */
    public HttpClientBuilder keyStore(KeyStore keyStore, char[] password) {
        this.clientKeyStore = keyStore;
        this.clientPrivateKeyPassword = new String(password);
        return this;
    }


    static class VerifierWrapper implements X509HostnameVerifier {
        protected HostnameVerifier verifier;

        VerifierWrapper(HostnameVerifier verifier) {
            this.verifier = verifier;
        }

        @Override
        public void verify(String host, SSLSocket ssl) throws IOException {
            if (!verifier.verify(host, ssl.getSession())) throw new SSLException("Hostname verification failure");
        }

        @Override
        public void verify(String host, X509Certificate cert) throws SSLException {
            throw new SSLException("This verification path not implemented");
        }

        @Override
        public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
            throw new SSLException("This verification path not implemented");
        }

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return verifier.verify(s, sslSession);
        }
    }

    /**
     * 根据当前配置构建 {@link HttpClient} 实例。
     *
     * @return 配置完成的 HTTP 客户端
     */
    public HttpClient build() {
        X509HostnameVerifier verifier = null;
        if (this.verifier != null) verifier = new VerifierWrapper(this.verifier);
        else {
            switch (policy) {
                case ANY:
                    verifier = new AllowAllHostnameVerifier();
                    break;
                case WILDCARD:
                    verifier = new BrowserCompatHostnameVerifier();
                    break;
                case STRICT:
                    verifier = new StrictHostnameVerifier();
                    break;
            }
        }
        try {
            SSLSocketFactory sslsf = null;
            SSLContext theContext = sslContext;
            if (disableTrustManager) {
                theContext = SSLContext.getInstance("SSL");
                theContext.init(null, new TrustManager[]{new PassthroughTrustManager()},
                        new SecureRandom());
                verifier = new AllowAllHostnameVerifier();
                sslsf = new SniSSLSocketFactory(theContext, verifier);
            } else if (theContext != null) {
                sslsf = new SniSSLSocketFactory(theContext, verifier);
            } else if (clientKeyStore != null || truststore != null) {
                sslsf = new SniSSLSocketFactory(SSLSocketFactory.TLS, clientKeyStore, clientPrivateKeyPassword, truststore, null, verifier);
            } else {
                final SSLContext tlsContext = SSLContext.getInstance(SSLSocketFactory.TLS);
                tlsContext.init(null, null, null);
                sslsf = new SniSSLSocketFactory(tlsContext, verifier);
            }
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(
                    new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
            Scheme httpsScheme = new Scheme("https", 443, sslsf);
            registry.register(httpsScheme);
            ClientConnectionManager cm = null;
            if (connectionPoolSize > 0) {
                ThreadSafeClientConnManager tcm = new ThreadSafeClientConnManager(registry, connectionTTL, connectionTTLUnit);
                tcm.setMaxTotal(connectionPoolSize);
                if (maxPooledPerRoute == 0) maxPooledPerRoute = connectionPoolSize;
                tcm.setDefaultMaxPerRoute(maxPooledPerRoute);
                cm = tcm;

            } else {
                cm = new SingleClientConnManager(registry);
            }
            BasicHttpParams params = new BasicHttpParams();
            params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

            if (proxyHost != null) {
                params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
            }

            if (socketTimeout > -1) {
                HttpConnectionParams.setSoTimeout(params, (int) socketTimeoutUnits.toMillis(socketTimeout));

            }
            if (establishConnectionTimeout > -1) {
                HttpConnectionParams.setConnectionTimeout(params, (int) establishConnectionTimeoutUnits.toMillis(establishConnectionTimeout));
            }
            DefaultHttpClient client = new DefaultHttpClient(cm, params);

            if (disableCookieCache) {
                client.setCookieStore(new CookieStore() {
                    @Override
                    public void addCookie(Cookie cookie) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public List<Cookie> getCookies() {
                        return Collections.emptyList();
                    }

                    @Override
                    public boolean clearExpired(Date date) {
                        return false;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void clear() {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                });

            }
            return client;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 {@link AdapterHttpClientConfig} 配置构建 HTTP 客户端。
     *
     * <p>自动加载信任库/密钥库、应用超时与代理设置，并禁用 Cookie 缓存
     * 以避免负载均衡下的会话粘连。</p>
     *
     * @param adapterConfig 适配器 HTTP 客户端配置
     * @return 配置完成的 HTTP 客户端
     */
    public HttpClient build(AdapterHttpClientConfig adapterConfig) {
        disableCookieCache(); // 禁用 Cookie 缓存，负载均衡下避免会话粘连

        String truststorePath = adapterConfig.getTruststore();
        if (truststorePath != null) {
            truststorePath = EnvUtil.replace(truststorePath);
            String truststorePassword = adapterConfig.getTruststorePassword();
            try {
                this.truststore = KeystoreUtil.loadKeyStore(truststorePath, truststorePassword);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load truststore", e);
            }
        }
        String clientKeystore = adapterConfig.getClientKeystore();
        if (clientKeystore != null) {
            clientKeystore = EnvUtil.replace(clientKeystore);
            String clientKeystorePassword = adapterConfig.getClientKeystorePassword();
            try {
                KeyStore clientCertKeystore = KeystoreUtil.loadKeyStore(clientKeystore, clientKeystorePassword);
                keyStore(clientCertKeystore, clientKeystorePassword);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load keystore", e);
            }
        }

        HttpClientBuilder.HostnameVerificationPolicy policy = HttpClientBuilder.HostnameVerificationPolicy.WILDCARD;
        if (adapterConfig.isAllowAnyHostname())
            policy = HttpClientBuilder.HostnameVerificationPolicy.ANY;
        connectionPoolSize(adapterConfig.getConnectionPoolSize());
        hostnameVerification(policy);
        if (adapterConfig.isDisableTrustManager()) {
            disableTrustManager();
        } else {
            trustStore(truststore);
        }

        if (socketTimeout == -1 && adapterConfig.getSocketTimeout() > 0) {
            socketTimeout(adapterConfig.getSocketTimeout(), TimeUnit.MILLISECONDS);
        }

        if (establishConnectionTimeout == -1 && adapterConfig.getConnectionTimeout() > 0) {
            establishConnectionTimeout(adapterConfig.getConnectionTimeout(), TimeUnit.MILLISECONDS);
        }

        if (connectionTTL == -1 && adapterConfig.getConnectionTTL() > 0) {
            connectionTTL(adapterConfig.getConnectionTTL(), TimeUnit.MILLISECONDS);
        }
        
        configureProxyForAuthServerIfProvided(adapterConfig);

        return build();
    }

    /**
     * 若配置中提供了 {@code proxy-url}，则为认证服务器请求设置 HTTP 代理。
     * <p>
     * 若 {@link AdapterHttpClientConfig} 未包含有效代理 URL，则忽略代理配置。
     * </p>
     *
     * @param adapterConfig 适配器 HTTP 客户端配置
     */
    private void configureProxyForAuthServerIfProvided(AdapterHttpClientConfig adapterConfig) {

        if (adapterConfig == null || adapterConfig.getProxyUrl() == null || adapterConfig.getProxyUrl().trim().isEmpty()) {
            return;
        }

        URI uri = URI.create(adapterConfig.getProxyUrl());
        this.proxyHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
    }
}