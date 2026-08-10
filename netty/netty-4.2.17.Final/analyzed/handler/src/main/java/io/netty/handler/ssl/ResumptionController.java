/*
 * Copyright 2024 The Netty Project
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

import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * 协调 TLS 会话恢复与 {@link ResumableX509ExtendedTrustManager}：包装 TrustManager、
 * 记录已完成完整校验的 {@link SSLEngine}，并在 ticket 恢复路径触发 resume 回调。
 */
final class ResumptionController {
    /** 已通过 check*Trusted(SSLEngine) 的引擎集合（WeakHashMap，防泄漏）。 */
    private final Set<SSLEngine> confirmedValidations;
    /** 全局唯一可配置的 Resumable TM（compareAndSet 保证仅一个）。 */
    private final AtomicReference<ResumableX509ExtendedTrustManager> resumableTm;

    ResumptionController() {
        confirmedValidations = Collections.synchronizedSet(
                Collections.newSetFromMap(new WeakHashMap<SSLEngine, Boolean>()));
        resumableTm = new AtomicReference<ResumableX509ExtendedTrustManager>();
    }

    /** 若为 {@link ResumableX509ExtendedTrustManager} 则包装并注册；否则原样返回。 */
    public TrustManager wrapIfNeeded(TrustManager tm) {
        if (tm instanceof ResumableX509ExtendedTrustManager) {
            if (!(tm instanceof X509ExtendedTrustManager)) {
                throw new IllegalStateException("ResumableX509ExtendedTrustManager implementation must be a " +
                        "subclass of X509ExtendedTrustManager, found: " + (tm == null ? null : tm.getClass()));
            }
            if (!resumableTm.compareAndSet(null, (ResumableX509ExtendedTrustManager) tm)) {
                throw new IllegalStateException(
                        "Only one ResumableX509ExtendedTrustManager can be configured for resumed sessions");
            }
            return new X509ExtendedWrapTrustManager((X509ExtendedTrustManager) tm, confirmedValidations);
        }
        return tm;
    }

    /** 引擎销毁时从 confirmed 集合移除，避免 WeakHashMap 长期持有。 */
    public void remove(SSLEngine engine) {
        if (resumableTm.get() != null) {
            confirmedValidations.remove(unwrapEngine(engine));
        }
    }

    /**
     * 会话有效且非首次完整校验路径时，调用 Resumable TM 的 resume 方法。
     * @return true 表示触发了 resume 回调
     */
    public boolean validateResumeIfNeeded(SSLEngine engine)
            throws CertificateException, SSLPeerUnverifiedException {
        ResumableX509ExtendedTrustManager tm;
        SSLSession session = engine.getSession();
        boolean valid = session.isValid();

        // 会话恢复场景：valid 且需要验证对端（客户端验服；服务端 need/want 客户端证）
        // 若 OPTIONAL 且客户端未提供证书，getPeerCertificates 会抛 SSLPeerUnverifiedException
        if (valid && (engine.getUseClientMode() || engine.getNeedClientAuth() || engine.getWantClientAuth()) &&
                (tm = resumableTm.get()) != null) {
            // JdkSslEngine 包装层须 unwrap，否则集合键与 check* 时不一致
            engine = unwrapEngine(engine);

            if (!confirmedValidations.remove(engine)) {
                Certificate[] peerCertificates;
                try {
                    peerCertificates = session.getPeerCertificates();
                } catch (SSLPeerUnverifiedException e) {
                    if (engine.getUseClientMode() || engine.getNeedClientAuth()) {
                        // 必须验证对端但未提供证书
                        throw e;
                    }
                    // OPTIONAL 且无客户端证书：会话恢复但无需 resume 校验
                    return false;
                }

                // ticket 恢复路径：调用 Resumable TM
                if (engine.getUseClientMode()) {
                    // We are the client, resuming a session trusting the server
                    tm.resumeServerTrusted(chainOf(peerCertificates), engine);
                } else {
                    // We are the server, resuming a session trusting the client
                    tm.resumeClientTrusted(chainOf(peerCertificates), engine);
                }
                return true;
            }
        }
        return false;
    }

    private static SSLEngine unwrapEngine(SSLEngine engine) {
        if (engine instanceof JdkSslEngine) {
            return ((JdkSslEngine) engine).getWrappedEngine();
        }
        return engine;
    }

    private static X509Certificate[] chainOf(Certificate[] peerCertificates) {
        if (peerCertificates instanceof X509Certificate[]) {
            //noinspection SuspiciousArrayCast
            return (X509Certificate[]) peerCertificates;
        }
        X509Certificate[] chain = new X509Certificate[peerCertificates.length];
        for (int i = 0; i < peerCertificates.length; i++) {
            Certificate cert = peerCertificates[i];
            if (cert instanceof X509Certificate || cert == null) {
                chain[i] = (X509Certificate) cert;
            } else {
                throw new IllegalArgumentException("Only X509Certificates are supported, found: " + cert.getClass());
            }
        }
        return chain;
    }

    /** 仅允许 SSLEngine 参数的 X509ExtendedTrustManager 包装：check* 后登记 engine。 */
    private static final class X509ExtendedWrapTrustManager extends X509ExtendedTrustManager {
        private final X509ExtendedTrustManager trustManager;
        private final Set<SSLEngine> confirmedValidations;

        X509ExtendedWrapTrustManager(X509ExtendedTrustManager trustManager, Set<SSLEngine> confirmedValidations) {
            this.trustManager = trustManager;
            this.confirmedValidations = confirmedValidations;
        }

        private static void unsupported() throws CertificateException {
            throw new CertificateException(
                    new UnsupportedOperationException("Resumable trust managers require the SSLEngine parameter"));
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {
            unsupported();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {
            unsupported();
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            unsupported();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            unsupported();
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {
            trustManager.checkClientTrusted(chain, authType, engine);
            confirmedValidations.add(engine);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {
            trustManager.checkServerTrusted(chain, authType, engine);
            confirmedValidations.add(engine);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return trustManager.getAcceptedIssuers();
        }
    }
}
