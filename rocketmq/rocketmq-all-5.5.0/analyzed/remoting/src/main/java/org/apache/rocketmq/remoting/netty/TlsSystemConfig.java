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

import io.netty.handler.ssl.SslContext;
import org.apache.rocketmq.remoting.common.TlsMode;

/**
 * TLS 系统配置：JVM 属性与 tls.properties 文件中的证书路径、认证模式等默认值。
 */
public class TlsSystemConfig {
    public static final String TLS_SERVER_MODE = "tls.server.mode";
    public static final String TLS_ENABLE = "tls.enable";
    public static final String TLS_CONFIG_FILE = "tls.config.file";
    public static final String TLS_TEST_MODE_ENABLE = "tls.test.mode.enable";

    public static final String TLS_SERVER_NEED_CLIENT_AUTH = "tls.server.need.client.auth";
    public static final String TLS_SERVER_KEYPATH = "tls.server.keyPath";
    public static final String TLS_SERVER_KEYPASSWORD = "tls.server.keyPassword";
    public static final String TLS_SERVER_CERTPATH = "tls.server.certPath";
    public static final String TLS_SERVER_AUTHCLIENT = "tls.server.authClient";
    public static final String TLS_SERVER_TRUSTCERTPATH = "tls.server.trustCertPath";

    public static final String TLS_CLIENT_KEYPATH = "tls.client.keyPath";
    public static final String TLS_CLIENT_KEYPASSWORD = "tls.client.keyPassword";
    public static final String TLS_CLIENT_CERTPATH = "tls.client.certPath";
    public static final String TLS_CLIENT_AUTHSERVER = "tls.client.authServer";
    public static final String TLS_CLIENT_TRUSTCERTPATH = "tls.client.trustCertPath";

    public static final String TLS_CIPHERS = "tls.ciphers";
    public static final String TLS_PROTOCOLS = "tls.protocols";


    /** 客户端（含 SDK 与 BrokerOuterAPI）是否启用 SSL。 */
    public static boolean tlsEnable = Boolean.parseBoolean(System.getProperty(TLS_ENABLE, "false"));

    /** 初始化 TLS 上下文时是否使用测试模式（自签名证书等）。 */
    public static boolean tlsTestModeEnable = Boolean.parseBoolean(System.getProperty(TLS_TEST_MODE_ENABLE, "true"));

    /**
     * 服务端 {@link SslContext} 的客户端认证模式：none、require 或 optional。
     */
    public static String tlsServerNeedClientAuth = System.getProperty(TLS_SERVER_NEED_CLIENT_AUTH, "none");
    /** 服务端私钥存储路径。 */
    public static String tlsServerKeyPath = System.getProperty(TLS_SERVER_KEYPATH, null);

    /** 服务端私钥密码。 */
    public static String tlsServerKeyPassword = System.getProperty(TLS_SERVER_KEYPASSWORD, null);

    /** 服务端 PEM 格式 X.509 证书链路径。 */
    public static String tlsServerCertPath = System.getProperty(TLS_SERVER_CERTPATH, null);

    /** 是否严格校验客户端证书。 */
    public static boolean tlsServerAuthClient = Boolean.parseBoolean(System.getProperty(TLS_SERVER_AUTHCLIENT, "false"));

    /** 校验客户端证书所用的信任根证书路径。 */
    public static String tlsServerTrustCertPath = System.getProperty(TLS_SERVER_TRUSTCERTPATH, null);

    /** 客户端私钥存储路径。 */
    public static String tlsClientKeyPath = System.getProperty(TLS_CLIENT_KEYPATH, null);

    /** 客户端私钥密码。 */
    public static String tlsClientKeyPassword = System.getProperty(TLS_CLIENT_KEYPASSWORD, null);

    /** 客户端 PEM 格式 X.509 证书链路径。 */
    public static String tlsClientCertPath = System.getProperty(TLS_CLIENT_CERTPATH, null);

    /** 是否严格校验服务端证书。 */
    public static boolean tlsClientAuthServer = Boolean.parseBoolean(System.getProperty(TLS_CLIENT_AUTHSERVER, "false"));

    /** 校验服务端证书所用的信任根证书路径。 */
    public static String tlsClientTrustCertPath = System.getProperty(TLS_CLIENT_TRUSTCERTPATH, null);

    /**
     * 服务端 SSL 模式：disabled（拒绝 SSL）、permissive（可选 SSL）、enforcing（强制 SSL）。
     * 客户端是否启用 SSL 由 {@link TlsSystemConfig#tlsEnable} 决定。
     */
    public static TlsMode tlsMode = TlsMode.parse(System.getProperty(TLS_SERVER_MODE, "permissive"));

    /** TLS 相关配置持久化文件路径（不含 tlsMode 与 tlsEnable）。 */
    public static String tlsConfigFile = System.getProperty(TLS_CONFIG_FILE, "/etc/rocketmq/tls.properties");

    /** TLS 密码套件列表；null 时使用 JVM 默认套件。 */
    public static String tlsCiphers = System.getProperty(TLS_CIPHERS, null);

    /** TLS 协议版本列表；null 时使用 JVM 默认协议。 */
    public static String tlsProtocols = System.getProperty(TLS_PROTOCOLS, null);
}
