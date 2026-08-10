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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Comparator;

import org.keycloak.common.crypto.CryptoIntegration;

import org.jboss.logging.Logger;


/**
 * 自定义 {@link javax.net.ssl.SSLSocketFactory}，将 Keycloak 配置的信任库注入默认 LDAP 客户端等 JSSE 调用链。
 * <p>
 * 本工厂仅在 {@link TruststoreProviderFactory} 通过 SPI 完成初始化后可用；即必须在 Keycloak Provider SPI
 * 配置中启用 {@code truststore} 提供者。
 * <p>
 * 若 {@link TruststoreProvider} 不可用，则委托 {@link org.keycloak.common.crypto.CryptoProvider#wrapFactoryForTruststore(javax.net.ssl.SSLSocketFactory)}，
 * 最终回退到 {@link javax.net.ssl.SSLSocketFactory#getDefault()}。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */

public class SSLSocketFactory extends javax.net.ssl.SSLSocketFactory implements Comparator {

    private static final Logger log = Logger.getLogger(SSLSocketFactory.class);

    /** 单例实例。 */
    private static SSLSocketFactory instance;

    /** 实际委托的底层 SSL 套接字工厂。 */
    private final javax.net.ssl.SSLSocketFactory sslsf;

    /** 从 {@link TruststoreProviderSingleton} 或系统默认工厂构建委托实例。 */
    private SSLSocketFactory() {

        TruststoreProvider provider = TruststoreProviderSingleton.get();
        javax.net.ssl.SSLSocketFactory sf = null;
        if (provider != null) {
            sf = new JSSETruststoreConfigurator(provider).getSSLSocketFactory();
        }

        if (sf == null) {
            log.info("No truststore provider found - using default SSLSocketFactory");
            sf = (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();
        }

        sslsf = CryptoIntegration.getProvider().wrapFactoryForTruststore(sf);
    }

    /** 返回懒加载的单例 {@link SSLSocketFactory}。 */
    public static synchronized SSLSocketFactory getDefault() {
        if (instance == null) {
            instance = new SSLSocketFactory();
        }
        return instance;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return sslsf.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sslsf.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return sslsf.createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return sslsf.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return sslsf.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return sslsf.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return sslsf.createSocket(address, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslsf.createSocket();
    }

    @Override
    public int compare(Object socketFactory1, Object socketFactory2) {
        return socketFactory1.equals(socketFactory2) ? 0 : -1;
    }
}
