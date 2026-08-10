/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.jgroups.certificates;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * 支持运行时热更新的 {@link X509ExtendedTrustManager} 包装器。
 * <p>
 * 证书校验失败时可触发 {@link Runnable} 回调（通常用于重载证书），
 * 以应对集群成员证书轮换期间的短暂不一致。
 */
class ReloadingX509ExtendedTrustManager extends X509ExtendedTrustManager {

    /** 当前生效的底层信任管理器委托。 */
    private volatile X509ExtendedTrustManager delegate;
    /** 证书校验异常时的回调（如触发证书重载）。 */
    private volatile Runnable onException;

    public ReloadingX509ExtendedTrustManager() {
        this.onException = () -> {
        };
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        try {
            delegate.checkClientTrusted(chain, authType, engine);
        } catch (CertificateException e) {
            onException.run();
            throw e;
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        try {
            delegate.checkClientTrusted(chain, authType, socket);
        } catch (CertificateException e) {
            onException.run();
            throw e;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        try {

            delegate.checkServerTrusted(chain, authType, engine);
        } catch (CertificateException e) {
            onException.run();
            throw e;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        try {

            delegate.checkServerTrusted(chain, authType, socket);
        } catch (CertificateException e) {
            onException.run();
            throw e;
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {

            delegate.checkClientTrusted(chain, authType);
        } catch (CertificateException e) {
            onException.run();
            throw e;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            delegate.checkServerTrusted(chain, authType);
        } catch (CertificateException e) {
            onException.run();
            throw e;
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return delegate.getAcceptedIssuers();
    }

    /** 注册证书校验失败时的回调处理。 */
    public void setExceptionHandler(Runnable runnable) {
        this.onException = Objects.requireNonNullElse(runnable, () -> {
        });
    }

    /** 替换底层信任管理器，后续校验将使用新受信证书。 */
    public void reload(X509ExtendedTrustManager trustManager) {
        delegate = Objects.requireNonNull(trustManager);
    }
}
