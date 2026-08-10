/*
 * Copyright 2016 The Netty Project
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

import io.netty.buffer.ByteBufAllocator;
import io.netty.internal.tcnative.CertificateCallback;
import io.netty.internal.tcnative.SSL;
import io.netty.internal.tcnative.SSLContext;
import io.netty.internal.tcnative.SniHostNameMatcher;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * A server-side {@link SslContext} which uses OpenSSL's SSL/TLS implementation.
 * <p>Instances of this class must be {@link #release() released} or else native memory will leak!
 *
 * <p>Instances of this class <strong>must not</strong> be released before any {@link ReferenceCountedOpenSslEngine}
 * which depends upon the instance of this class is released. Otherwise if any method of
 * {@link ReferenceCountedOpenSslEngine} is called which uses this class's JNI resources the JVM may crash.
 *
 * <p>服务端引用计数 OpenSSL {@link SslContext}：配置服务端证书、客户端认证、SNI 主机名匹配与
 * 会话 ticket；释放顺序要求同客户端上下文。</p>
 */
public final class ReferenceCountedOpenSslServerContext extends ReferenceCountedOpenSslContext {
    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(ReferenceCountedOpenSslServerContext.class);
    /** 写入 OpenSSL session id context 的固定标识。 */
    private static final byte[] ID = {'n', 'e', 't', 't', 'y'};
    /** 服务端会话缓存上下文。 */
    private final OpenSslServerSessionContext sessionContext;

    ReferenceCountedOpenSslServerContext(
            X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory,
            X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory,
            Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn,
            long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls,
            boolean enableOcsp, String keyStore, ResumptionController resumptionController,
            Map.Entry<SslContextOption<?>, Object>[] options,
            List<OpenSslCredential> credentials) throws SSLException {
        this(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers,
                cipherFilter, toNegotiator(apn), sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls,
                enableOcsp, keyStore, resumptionController, options, credentials);
    }

    ReferenceCountedOpenSslServerContext(
            X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory,
            X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory,
            Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn,
            long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls,
            boolean enableOcsp, String keyStore, ResumptionController resumptionController,
            Map.Entry<SslContextOption<?>, Object>[] options,
            List<OpenSslCredential> credentials) throws SSLException {
        super(ciphers, cipherFilter, apn, SSL.SSL_MODE_SERVER, keyCertChain,
                clientAuth, protocols, startTls,
                null, // 服务端不做 endpoint 主机名校验
                enableOcsp, true, null, resumptionController, options, credentials);
        // 创建 SSL_CTX 并完成服务端特有配置
        boolean success = false;
        try {
            sessionContext = newSessionContext(this, ctx, engines, trustCertCollection, trustManagerFactory,
                    keyCertChain, key, keyPassword, keyManagerFactory, keyStore,
                    sessionCacheSize, sessionTimeout, resumptionController, isJdkSignatureFallbackEnabled(options));
            if (SERVER_ENABLE_SESSION_TICKET) {
                sessionContext.setTicketKeys();
            }
            success = true;
        } finally {
            if (!success) {
                release();
            }
        }
    }

    @Override
    public OpenSslServerSessionContext sessionContext() {
        return sessionContext;
    }

    static OpenSslServerSessionContext newSessionContext(ReferenceCountedOpenSslContext thiz, long ctx,
                                                         OpenSslEngineMap engines,
                                                         X509Certificate[] trustCertCollection,
                                                         TrustManagerFactory trustManagerFactory,
                                                         X509Certificate[] keyCertChain, PrivateKey key,
                                                         String keyPassword, KeyManagerFactory keyManagerFactory,
                                                         String keyStore, long sessionCacheSize, long sessionTimeout,
                                                         ResumptionController resumptionController,
                                                         boolean fallbackToJdkSignatureProviders)
            throws SSLException {
        OpenSslKeyMaterialProvider keyMaterialProvider = null;
        try {
            try {
                SSLContext.setVerify(ctx, SSL.SSL_CVERIFY_NONE, VERIFY_DEPTH);

                // 替代签名私钥（HSM 等）路径
                if (keyManagerFactory == null && key != null && key.getEncoded() == null) {
                    if (!fallbackToJdkSignatureProviders) {
                        // 未启用 JDK Provider 回退时不支持
                        throw new SSLException("Private key requiring alternative signature provider detected " +
                                "(such as hardware security key, smart card, or remote signing service) but " +
                                "alternative key fallback is disabled.");
                    }
                    keyMaterialProvider = setupSecurityProviderSignatureSource(thiz, ctx, keyCertChain, key,
                            manager -> new OpenSslServerCertificateCallback(engines, manager));
                } else if (!OpenSsl.useKeyManagerFactory()) {
                    if (keyManagerFactory != null) {
                        throw new IllegalArgumentException(
                                "KeyManagerFactory not supported with external keys");
                    } else {
                        checkNotNull(keyCertChain, "keyCertChain");
                        // 无 KMF 支持时直接 setKeyMaterial
                        setKeyMaterial(ctx, keyCertChain, key, keyPassword);
                    }
                } else {
                    // 服务端必须提供 KeyManagerFactory（或由 certChain 构造）
                    if (keyManagerFactory == null) {
                        keyManagerFactory = certChainToKeyManagerFactory(keyCertChain, key, keyPassword, keyStore);
                    }
                    keyMaterialProvider = providerFor(keyManagerFactory, keyPassword);

                    SSLContext.setCertificateCallback(ctx, new OpenSslServerCertificateCallback(
                            engines, new OpenSslKeyMaterialManager(keyMaterialProvider, thiz.hasTmpDhKeys)));
                }
            } catch (Exception e) {
                throw new SSLException("failed to set certificate and key", e);
            }
            try {
                if (trustCertCollection != null) {
                    trustManagerFactory = buildTrustManagerFactory(trustCertCollection, trustManagerFactory, keyStore);
                } else if (trustManagerFactory == null) {
                    // Mimic the way SSLContext.getInstance(KeyManager[], null, null) works
                    trustManagerFactory = TrustManagerFactory.getInstance(
                            TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init((KeyStore) null);
                }

                final X509TrustManager manager = chooseTrustManager(
                        trustManagerFactory.getTrustManagers(), resumptionController);

                // static 校验回调，避免 JNI 全局引用泄漏（#5372）
                //
                //            See https://github.com/netty/netty/issues/5372

                setVerifyCallback(ctx, engines, manager);

                X509Certificate[] issuers = manager.getAcceptedIssuers();
                if (issuers != null && issuers.length > 0) {
                    long bio = 0;
                    try {
                        bio = toBIO(ByteBufAllocator.DEFAULT, issuers);
                        if (!SSLContext.setCACertificateBio(ctx, bio)) {
                            String msg = "unable to setup accepted issuers for trustmanager " + manager;
                            int error = SSL.getLastErrorNumber();
                            if (error != 0) {
                                msg += ". " + SSL.getErrorString(error);
                            }
                            throw new SSLException(msg);
                        }
                    } finally {
                        freeBio(bio);
                    }
                }

                // SNI 主机名匹配回调同样须 static（#5372 同类问题）
                SSLContext.setSniHostnameMatcher(ctx, new OpenSslSniHostnameMatcher(engines));
            } catch (SSLException e) {
                throw e;
            } catch (Exception e) {
                throw new SSLException("unable to setup trustmanager", e);
            }

            OpenSslServerSessionContext sessionContext = new OpenSslServerSessionContext(thiz, keyMaterialProvider);
            sessionContext.setSessionIdContext(ID);
            // 默认启用服务端会话缓存
            sessionContext.setSessionCacheEnabled(SERVER_ENABLE_SESSION_CACHE);
            if (sessionCacheSize > 0) {
                sessionContext.setSessionCacheSize((int) Math.min(sessionCacheSize, Integer.MAX_VALUE));
            }
            if (sessionTimeout > 0) {
                sessionContext.setSessionTimeout((int) Math.min(sessionTimeout, Integer.MAX_VALUE));
            }

            keyMaterialProvider = null;

            return sessionContext;
        } finally {
            if (keyMaterialProvider != null) {
                keyMaterialProvider.destroy();
            }
        }
    }

    private static void setVerifyCallback(long ctx,
                                          OpenSslEngineMap engines,
                                          X509TrustManager manager) {
        // Use this to prevent an error when running on java < 7
        if (useExtendedTrustManager(manager)) {
            SSLContext.setCertVerifyCallback(ctx, new ExtendedTrustManagerVerifyCallback(
                    engines, (X509ExtendedTrustManager) manager));
        } else {
            SSLContext.setCertVerifyCallback(ctx, new TrustManagerVerifyCallback(engines, manager));
        }
    }

    /** 服务端证书选择 native 回调（忽略客户端 CA 列表，与 OpenJDK SSLEngineImpl 行为一致）。 */
    private static final class OpenSslServerCertificateCallback implements CertificateCallback {
        private final OpenSslEngineMap engines;
        private final OpenSslKeyMaterialManager keyManagerHolder;

        OpenSslServerCertificateCallback(OpenSslEngineMap engines,
                                         OpenSslKeyMaterialManager keyManagerHolder) {
            this.engines = engines;
            this.keyManagerHolder = keyManagerHolder;
        }

        @Override
        public void handle(long ssl, byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals) throws Exception {
            final ReferenceCountedOpenSslEngine engine = engines.get(ssl);
            if (engine == null) {
                // Maybe null if destroyed in the meantime.
                return;
            }
            try {
                // 暂不解析 asn1DerEncodedPrincipals，与 OpenJDK 行为对齐
                keyManagerHolder.setKeyMaterialServerSide(engine);
            } catch (Throwable cause) {
                engine.initHandshakeException(cause);

                if (cause instanceof Exception) {
                    throw (Exception) cause;
                }
                throw new SSLException(cause);
            }
        }
    }

    private static final class TrustManagerVerifyCallback extends AbstractCertificateVerifier {
        private final X509TrustManager manager;

        TrustManagerVerifyCallback(OpenSslEngineMap engines,
                                   X509TrustManager manager) {
            super(engines);
            this.manager = manager;
        }

        @Override
        void verify(ReferenceCountedOpenSslEngine engine, X509Certificate[] peerCerts, String auth)
                throws Exception {
            manager.checkClientTrusted(peerCerts, auth);
        }
    }

    private static final class ExtendedTrustManagerVerifyCallback extends AbstractCertificateVerifier {
        private final X509ExtendedTrustManager manager;

        ExtendedTrustManagerVerifyCallback(OpenSslEngineMap engines,
                                           X509ExtendedTrustManager manager) {
            super(engines);
            this.manager = manager;
        }

        @Override
        void verify(ReferenceCountedOpenSslEngine engine, X509Certificate[] peerCerts, String auth)
                throws Exception {
            manager.checkClientTrusted(peerCerts, auth, engine);
        }
    }

    /** SNI 扩展中的主机名与引擎 {@link SNIMatcher} 规则匹配。 */
    private static final class OpenSslSniHostnameMatcher implements SniHostNameMatcher {
        private final OpenSslEngineMap engines;

        OpenSslSniHostnameMatcher(OpenSslEngineMap engines) {
            this.engines = engines;
        }

        @Override
        public boolean match(long ssl, String hostname) {
            ReferenceCountedOpenSslEngine engine = engines.get(ssl);
            if (engine != null) {
                // TODO: 下版 tcnative 宜直接传 byte[] 而非 String
                return engine.checkSniHostnameMatch(hostname);
            }
            logger.warn("No ReferenceCountedOpenSslEngine found for SSL pointer: {}", ssl);
            return false;
        }
    }
}
