/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.tls;

/**
 * TLS 系统级配置：通过 JVM {@link System#getProperty(String)} 读取客户端与服务端的
 * 证书路径、密钥、双向认证开关及证书文件轮询间隔等参数，供 gRPC/HTTP 模块初始化 SSL 上下文。
 * tls system config.
 *
 * @author wangwei
 */
public final class TlsSystemConfig {
    
    /** 系统属性键：是否启用 TLS 测试模式（跳过部分证书校验，仅用于开发调试） */
    public static final String TLS_TEST_MODE_ENABLE = "tls.test";
    
    /** 系统属性键：是否在客户端侧启用 SSL/TLS 加密通信 */
    public static final String TLS_ENABLE = "tls.enable";
    
    /** 系统属性键：客户端是否严格校验服务端证书（双向 TLS 中的服务端认证） */
    public static final String CLIENT_AUTH = "tls.client.authServer";
    
    /** 系统属性键：客户端私钥 PEM 文件路径 */
    public static final String CLIENT_KEYPATH = "tls.client.keyPath";
    
    /** 系统属性键：客户端私钥解密密码 */
    public static final String CLIENT_KEYPASSWORD = "tls.client.keyPassword";
    
    /** 系统属性键：客户端 X.509 证书链 PEM 文件路径 */
    public static final String CLIENT_CERTPATH = "tls.client.certPath";
    
    /** 系统属性键：客户端信任 CA/服务端证书 PEM 路径，用于校验对端 */
    public static final String CLIENT_TRUST_CERT = "tls.client.trustCertPath";
    
    /** 系统属性键：服务端是否要求并校验客户端证书（mTLS） */
    public static final String SERVER_AUTH = "tls.server.authClient";
    
    /** 系统属性键：服务端私钥 PEM 文件路径 */
    public static final String SERVER_KEYPATH = "tls.server.keyPath";
    
    /** 系统属性键：服务端私钥解密密码 */
    public static final String SERVER_KEYPASSWORD = "tls.server.keyPassword";
    
    /** 系统属性键：服务端 X.509 证书链 PEM 文件路径 */
    public static final String SERVER_CERTPATH = "tls.server.certPath";
    
    /** 系统属性键：服务端信任 CA/客户端证书 PEM 路径 */
    public static final String SERVER_TRUST_CERT = "tls.server.trustCertPath";
    
    /** 系统属性键：TLS 证书文件变更检测间隔（分钟），用于热更新证书 */
    public static final String CHECK_INTERVAL = "checkIntervalTlsFile";
    
    /** 是否在客户端侧启用 SSL/TLS，默认 {@code false} */
    /**
     * To determine whether use SSL in client-side.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static boolean tlsEnable = Boolean.parseBoolean(System.getProperty(TLS_ENABLE, "false"));
    
    /** 初始化 TLS 上下文时是否进入测试模式（放宽校验），默认 {@code false} */
    /**
     * To determine whether use test mode when initialize TLS context.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static boolean tlsTestModeEnable =
        Boolean.parseBoolean(System.getProperty(TLS_TEST_MODE_ENABLE, "false"));
    
    /** 客户端是否严格校验服务端证书，默认 {@code false} */
    /**
     * To determine whether verify the server endpoint's certificate strictly.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static boolean tlsClientAuthServer =
        Boolean.parseBoolean(System.getProperty(CLIENT_AUTH, "false"));
    
    /** 服务端是否严格校验客户端证书，默认 {@code false} */
    /**
     * To determine whether verify the client endpoint's certificate strictly.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static boolean tlsServerAuthClient =
        Boolean.parseBoolean(System.getProperty(SERVER_AUTH, "false"));
    
    /** 客户端私钥存储路径（PEM） */
    /**
     * The store path of client-side private key.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static String tlsClientKeyPath = System.getProperty(CLIENT_KEYPATH, null);
    
    /** 客户端私钥密码 */
    /**
     * The password of the client-side private key.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static String tlsClientKeyPassword = System.getProperty(CLIENT_KEYPASSWORD, null);
    
    /** 客户端 X.509 证书链 PEM 存储路径 */
    /**
     * The store path of client-side X.509 certificate chain in PEM format.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static String tlsClientCertPath = System.getProperty(CLIENT_CERTPATH, null);
    
    /** 用于校验服务端证书的信任证书（CA）存储路径 */
    /**
     * The store path of trusted certificates for verifying the server endpoint's certificate.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static String tlsClientTrustCertPath = System.getProperty(CLIENT_TRUST_CERT, null);
    
    /** 服务端私钥存储路径（PEM） */
    /**
     * The store path of server-side private key.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static String tlsServerKeyPath = System.getProperty(SERVER_KEYPATH, null);
    
    /** 服务端私钥密码 */
    /**
     * The  password of the server-side private key.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static String tlsServerKeyPassword = System.getProperty(SERVER_KEYPASSWORD, null);
    
    /** 服务端 X.509 证书链 PEM 存储路径 */
    /**
     * The store path of server-side X.509 certificate chain in PEM format.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static String tlsServerCertPath = System.getProperty(SERVER_CERTPATH, null);
    
    /** 用于校验客户端证书的信任证书（CA）存储路径 */
    /**
     * The store path of trusted certificates for verifying the client endpoint's certificate.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static String tlsServerTrustCertPath = System.getProperty(SERVER_TRUST_CERT, null);
    
    /** TLS 证书文件轮询检测间隔（分钟），默认 10 */
    /**
     * tls file check interval , default is 10 min.
      * <p>TLS 系统配置；详见类级说明。</p>
     */
    public static int tlsFileCheckInterval =
        Integer.parseInt(System.getProperty(CHECK_INTERVAL, "10"));
    
}
