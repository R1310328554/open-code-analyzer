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

package org.keycloak.spi.infinispan;

import java.time.Duration;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.keycloak.provider.Provider;

/**
 * JGroups 集群通信 TLS 证书的 {@link Provider}。
 * <p>
 * <b>实现说明</b>
 * <p>
 * 若 {@link #isEnabled()} 返回 {@code true}，则必须实现 {@link #keyManager()} 与 {@link #trustManager()}。
 * <p>
 * 若 {@link #supportRotateAndReload()} 返回 {@code true}，则必须实现
 * {@link #rotateCertificate()}、{@link #reloadCertificate()} 与 {@link #nextRotation()}。
 */
public interface JGroupsCertificateProvider extends Provider {

    /** 禁用 TLS 时的空实现占位符。 */
    JGroupsCertificateProvider DISABLED = new JGroupsCertificateProvider() {
    };

    /**
     * 生成新证书。
     * <p>
     * 新证书不应立即生效，须待调用 {@link #reloadCertificate()} 后才会应用。
     * <p>
     * 当 {@link #supportRotateAndReload()} 为 {@code true} 时必须实现。
     */
    default void rotateCertificate() {
        throw new UnsupportedOperationException();
    }

    /**
     * 重新加载最新证书并应用到 {@link KeyManager} 与 {@link TrustManager}。
     * <p>
     * 当 {@link #supportRotateAndReload()} 为 {@code true} 时必须实现。
     */
    default void reloadCertificate() {
        throw new UnsupportedOperationException();
    }

    /**
     * 返回距离下次证书轮换的剩余时间，用于周期性自动轮换。
     * <p>
     * 当 {@link #supportRotateAndReload()} 为 {@code true} 时必须实现。
     *
     * @return 距下次轮换的时间间隔
     */
    default Duration nextRotation() {
        throw new UnsupportedOperationException();
    }

    /**
     * 返回受管理的 {@link KeyManager}。
     * <p>
     * 若支持轮换，{@link #reloadCertificate()} 后返回的实例须反映新证书；
     * 启动时仅调用一次。
     * <p>
     * 当 {@link #isEnabled()} 为 {@code true} 时必须实现。
     *
     * @return 供 {@link javax.net.ssl.SSLContext} 使用的 {@link KeyManager}
     */
    default KeyManager keyManager() {
        throw new UnsupportedOperationException();
    }

    /**
     * 返回受管理的 {@link TrustManager}。
     * <p>
     * 若支持轮换，{@link #reloadCertificate()} 后返回的实例须反映新证书；
     * 启动时仅调用一次。
     * <p>
     * 当 {@link #isEnabled()} 为 {@code true} 时必须实现。
     *
     * @return 供 {@link javax.net.ssl.SSLContext} 使用的 {@link TrustManager}
     */
    default TrustManager trustManager() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return 是否支持证书轮换与热重载
     */
    default boolean supportRotateAndReload() {
        return false;
    }

    /**
     * @return JGroups 通信是否启用 TLS
     */
    default boolean isEnabled() {
        return false;
    }

    @Override
    default void close() {
    }
}
