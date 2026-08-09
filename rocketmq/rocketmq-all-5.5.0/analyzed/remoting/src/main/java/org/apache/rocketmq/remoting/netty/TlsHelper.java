/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.remoting.netty;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Properties;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_CIPHERS;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_CLIENT_AUTHSERVER;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_CLIENT_CERTPATH;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_CLIENT_KEYPASSWORD;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_CLIENT_KEYPATH;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_CLIENT_TRUSTCERTPATH;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_PROTOCOLS;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_SERVER_AUTHCLIENT;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_SERVER_CERTPATH;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_SERVER_KEYPASSWORD;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_SERVER_KEYPATH;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_SERVER_NEED_CLIENT_AUTH;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_SERVER_TRUSTCERTPATH;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_TEST_MODE_ENABLE;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsCiphers;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsClientAuthServer;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsClientCertPath;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsClientKeyPassword;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsClientKeyPath;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsClientTrustCertPath;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsProtocols;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsServerAuthClient;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsServerCertPath;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsServerKeyPassword;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsServerKeyPath;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsServerNeedClientAuth;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsServerTrustCertPath;
import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.tlsTestModeEnable;

/**
 * TLS/SSL 上下文构建工具：读取配置、解密私钥并组装 Netty {@link SslContext}。
 */
public class TlsHelper {

    public interface DecryptionStrategy {
        /**
         * 解密加密的私钥文件。
         *
         * @param privateKeyEncryptPath 私钥文件路径
         * @param forClient 是否为客户端侧私钥
         * @return 解密后私钥的输入流
         * @throws IOException I/O 错误时抛出
         */
        InputStream decryptPrivateKey(String privateKeyEncryptPath, boolean forClient) throws IOException;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.ROCKETMQ_REMOTING_NAME);

    private static DecryptionStrategy decryptionStrategy = new DecryptionStrategy() {
        @Override
        public InputStream decryptPrivateKey(final String privateKeyEncryptPath,
            final boolean forClient) throws IOException {
            return new FileInputStream(privateKeyEncryptPath);
        }
    };


    /** 注册自定义私钥解密策略。 */
    public static void registerDecryptionStrategy(final DecryptionStrategy decryptionStrategy) {
        TlsHelper.decryptionStrategy = decryptionStrategy;
    }

    /** 按客户端或服务端模式构建 {@link SslContext}，支持测试模式与双向认证。 */
    public static SslContext buildSslContext(boolean forClient) throws IOException, CertificateException {
        File configFile = new File(TlsSystemConfig.tlsConfigFile);
        extractTlsConfigFromFile(configFile);
        logTheFinalUsedTlsConfig();

        SslProvider provider;
        if (OpenSsl.isAvailable()) {
            provider = SslProvider.OPENSSL;
            LOGGER.info("Using OpenSSL provider"); // 使用 OpenSSL 加速
        } else {
            provider = SslProvider.JDK;
            LOGGER.info("Using JDK SSL provider"); // 回退至 JDK SSL
        }

        SslContextBuilder sslContextBuilder = null;
        if (forClient) {
            if (tlsTestModeEnable) {
                sslContextBuilder = SslContextBuilder
                    .forClient()
                    .sslProvider(SslProvider.JDK)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE);
            } else {
                sslContextBuilder = SslContextBuilder.forClient().sslProvider(SslProvider.JDK);


                if (!tlsClientAuthServer) {
                    sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                } else {
                    if (!isNullOrEmpty(tlsClientTrustCertPath)) {
                        sslContextBuilder.trustManager(new File(tlsClientTrustCertPath));
                    }
                }

                sslContextBuilder = sslContextBuilder.keyManager(
                    !isNullOrEmpty(tlsClientCertPath) ? new FileInputStream(tlsClientCertPath) : null,
                    !isNullOrEmpty(tlsClientKeyPath) ? decryptionStrategy.decryptPrivateKey(tlsClientKeyPath, true) : null,
                    !isNullOrEmpty(tlsClientKeyPassword) ? tlsClientKeyPassword : null);
            }
        } else {

            if (tlsTestModeEnable) {
                SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
                sslContextBuilder = SslContextBuilder
                    .forServer(selfSignedCertificate.certificate(), selfSignedCertificate.privateKey())
                    .sslProvider(provider)
                    .clientAuth(ClientAuth.OPTIONAL);
            } else {
                sslContextBuilder = SslContextBuilder.forServer(
                    !isNullOrEmpty(tlsServerCertPath) ? new FileInputStream(tlsServerCertPath) : null,
                    !isNullOrEmpty(tlsServerKeyPath) ? decryptionStrategy.decryptPrivateKey(tlsServerKeyPath, false) : null,
                    !isNullOrEmpty(tlsServerKeyPassword) ? tlsServerKeyPassword : null)
                    .sslProvider(provider);

                if (!tlsServerAuthClient) {
                    sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                } else {
                    if (!isNullOrEmpty(tlsServerTrustCertPath)) {
                        sslContextBuilder.trustManager(new File(tlsServerTrustCertPath));
                    }
                }

                sslContextBuilder.clientAuth(parseClientAuthMode(tlsServerNeedClientAuth));
            }
        }
        moreTlsConfig(sslContextBuilder);
        return sslContextBuilder.build();
    }

    /** 追加密码套件与协议版本配置。 */
    protected static void moreTlsConfig(SslContextBuilder sslContextBuilder) {
        if (tlsCiphers != null) {
            sslContextBuilder.ciphers(Arrays.asList(tlsCiphers.split(",")));
        }
        if (tlsProtocols != null) {
            sslContextBuilder.protocols(tlsProtocols.split(","));
        }
    }
    private static void extractTlsConfigFromFile(final File configFile) {
        if (!(configFile.exists() && configFile.isFile() && configFile.canRead())) {
            LOGGER.info("Tls config file doesn't exist, skip it"); // 配置文件不存在则跳过
            return;
        }

        Properties properties;
        properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(configFile);
            properties.load(inputStream);
        } catch (IOException ignore) {
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }

        tlsTestModeEnable = Boolean.parseBoolean(properties.getProperty(TLS_TEST_MODE_ENABLE, String.valueOf(tlsTestModeEnable)));
        tlsServerNeedClientAuth = properties.getProperty(TLS_SERVER_NEED_CLIENT_AUTH, tlsServerNeedClientAuth);
        tlsServerKeyPath = properties.getProperty(TLS_SERVER_KEYPATH, tlsServerKeyPath);
        tlsServerKeyPassword = properties.getProperty(TLS_SERVER_KEYPASSWORD, tlsServerKeyPassword);
        tlsServerCertPath = properties.getProperty(TLS_SERVER_CERTPATH, tlsServerCertPath);
        tlsServerAuthClient = Boolean.parseBoolean(properties.getProperty(TLS_SERVER_AUTHCLIENT, String.valueOf(tlsServerAuthClient)));
        tlsServerTrustCertPath = properties.getProperty(TLS_SERVER_TRUSTCERTPATH, tlsServerTrustCertPath);

        tlsClientKeyPath = properties.getProperty(TLS_CLIENT_KEYPATH, tlsClientKeyPath);
        tlsClientKeyPassword = properties.getProperty(TLS_CLIENT_KEYPASSWORD, tlsClientKeyPassword);
        tlsClientCertPath = properties.getProperty(TLS_CLIENT_CERTPATH, tlsClientCertPath);
        tlsClientAuthServer = Boolean.parseBoolean(properties.getProperty(TLS_CLIENT_AUTHSERVER, String.valueOf(tlsClientAuthServer)));
        tlsClientTrustCertPath = properties.getProperty(TLS_CLIENT_TRUSTCERTPATH, tlsClientTrustCertPath);

        tlsCiphers = properties.getProperty(TLS_CIPHERS, tlsCiphers);
        tlsProtocols = properties.getProperty(TLS_PROTOCOLS, tlsProtocols);
    }

    private static void logTheFinalUsedTlsConfig() {
        LOGGER.info("Log the final used tls related configuration"); // 输出最终 TLS 配置
        LOGGER.info("{} = {}", TLS_TEST_MODE_ENABLE, tlsTestModeEnable);
        LOGGER.debug("{} = {}", TLS_SERVER_NEED_CLIENT_AUTH, tlsServerNeedClientAuth);
        LOGGER.debug("{} = {}", TLS_SERVER_KEYPATH, tlsServerKeyPath);
        LOGGER.debug("{} = {}", TLS_SERVER_CERTPATH, tlsServerCertPath);
        LOGGER.debug("{} = {}", TLS_SERVER_AUTHCLIENT, tlsServerAuthClient);
        LOGGER.debug("{} = {}", TLS_SERVER_TRUSTCERTPATH, tlsServerTrustCertPath);

        LOGGER.debug("{} = {}", TLS_CLIENT_KEYPATH, tlsClientKeyPath);
        LOGGER.debug("{} = {}", TLS_CLIENT_CERTPATH, tlsClientCertPath);
        LOGGER.debug("{} = {}", TLS_CLIENT_AUTHSERVER, tlsClientAuthServer);
        LOGGER.debug("{} = {}", TLS_CLIENT_TRUSTCERTPATH, tlsClientTrustCertPath);

        LOGGER.debug("{} = {}", TLS_CIPHERS, tlsCiphers);
        LOGGER.debug("{} = {}", TLS_PROTOCOLS, tlsProtocols);
    }

    /** 将配置字符串解析为 Netty {@link ClientAuth} 枚举。 */
    private static ClientAuth parseClientAuthMode(String authMode) {
        if (null == authMode || authMode.trim().isEmpty()) {
            return ClientAuth.NONE;
        }

        String authModeUpper = authMode.toUpperCase();
        for (ClientAuth clientAuth : ClientAuth.values()) {
            if (clientAuth.name().equals(authModeUpper)) {
                return clientAuth;
            }
        }

        return ClientAuth.NONE;
    }

    /** 判断字符串是否为 null 或空串。 */
    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
