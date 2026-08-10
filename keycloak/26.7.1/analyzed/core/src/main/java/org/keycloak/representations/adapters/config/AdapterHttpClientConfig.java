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

package org.keycloak.representations.adapters.config;

/**
 * adapter 所用 HTTP 客户端的配置选项接口。
 * <p>
 * 注意：在统一之前需与 {@code adapters/saml/core/.../AdapterHttpClientConfig.java} 保持同步。
 *
 * @author hmlnarik
 */
public interface AdapterHttpClientConfig {

    /** 返回信任库（truststore）文件路径。 */
    String getTruststore();

    /** 返回信任库密码。 */
    String getTruststorePassword();

    /** 返回客户端密钥库（keystore）文件路径。 */
    String getClientKeystore();

    /** 返回客户端密钥库密码。 */
    String getClientKeystorePassword();

    /**
     * 是否跳过对服务端证书的主机名校验。
     *
     * @return {@code true} 表示不校验主机名
     */
    boolean isAllowAnyHostname();

    /**
     * 是否禁用信任管理与主机名校验。
     * <p>
     * <i>注意</i>：禁用信任管理器会造成安全漏洞，仅在无法或不需要验证通信对端身份时启用。
     */
    boolean isDisableTrustManager();

    /** 返回 HTTP 连接池大小。 */
    int getConnectionPoolSize();

    /** 返回 HTTP 代理 URL。 */
    String getProxyUrl();

    /** 返回等待套接字数据的超时时间（毫秒）。 */
    long getSocketTimeout();

    /** 返回与远程主机建立连接的超时时间（毫秒）。 */
    long getConnectionTimeout();

    /** 返回连接存活时间（TTL，毫秒）。 */
    long getConnectionTTL();
}
