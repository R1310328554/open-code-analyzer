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

package org.keycloak.truststore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 基于 {@link TruststoreProvider} 配置 JSSE {@link SSLContext} 与 {@link TrustManager}。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class JSSETruststoreConfigurator {

    private TruststoreProvider provider;
    /** 懒加载的 {@link javax.net.ssl.SSLSocketFactory}。 */
    private volatile javax.net.ssl.SSLSocketFactory sslFactory;
    /** 懒加载的信任管理器数组。 */
    private volatile TrustManager[] tm;

    /** 从会话的 file 型 {@link TruststoreProviderFactory} 获取信任库提供者。 */
    public JSSETruststoreConfigurator(KeycloakSession session) {
        KeycloakSessionFactory factory = session.getKeycloakSessionFactory();
        TruststoreProviderFactory truststoreFactory = (TruststoreProviderFactory) factory.getProviderFactory(TruststoreProvider.class, "file");

        provider = truststoreFactory.create(session);
        if (provider != null && provider.getTruststore() == null) {
            provider = null;
        }
    }

    /** 直接使用已初始化的 {@link TruststoreProvider}。 */
    public JSSETruststoreConfigurator(TruststoreProvider provider) {
        this.provider = provider;
    }

    /** 返回基于信任库初始化的 TLS {@link javax.net.ssl.SSLSocketFactory}，无提供者时返回 null。 */
    public javax.net.ssl.SSLSocketFactory getSSLSocketFactory() {
        if (provider == null) {
            return null;
        }

        if (sslFactory == null) {
            synchronized(this) {
                if (sslFactory == null) {
                    try {
                        SSLContext sslctx = SSLContext.getInstance("TLS");
                        sslctx.init(null, getTrustManagers(), null);
                        sslFactory = sslctx.getSocketFactory();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize SSLContext: ", e);
                    }
                }
            }
        }
        return sslFactory;
    }

    /** 返回基于信任库初始化的 {@link TrustManager} 数组。 */
    public TrustManager[] getTrustManagers() {
        if (provider == null) {
            return null;
        }

        if (tm == null) {
            synchronized (this) {
                if (tm == null) {
                    TrustManagerFactory tmf = null;
                    try {
                        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        tmf.init(provider.getTruststore());
                        tm = tmf.getTrustManagers();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize TrustManager: ", e);
                    }
                }
            }
        }
        return tm;
    }

    public TruststoreProvider getProvider() {
        return provider;
    }
}
