/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.client.authentication;

import java.util.Map;

import org.keycloak.representations.adapters.config.AdapterConfig;

/**
 * 客户端/应用认证 SPI：适配器在向 Keycloak 发起 OIDC 反向通道请求时使用
 * （授权码换 token、刷新 token、反向通道登出等）。也可在应用中用于直接访问授权或服务账户请求。
 *
 * <p>在适配器侧实现本 SPI 时，需在服务端实现对应的 {@code org.keycloak.authentication.ClientAuthenticator}，
 * 以便服务器能验证客户端身份。</p>
 *
 * <p>实现类须在本 WAR 的 {@code META-INF/services/org.keycloak.protocol.oidc.client.authentication.ClientCredentialsProvider}
 * 中注册（或放在 WEB-INF/lib 的 JAR 中，或以 JBoss 模块形式在多个 WAR 间共享）。</p>
 *
 * <p>注意：SPI 尚未定稿，方法签名在未来版本可能变更（例如支持客户端证书认证）。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientCredentialsProvider {

    /**
     * 返回提供者 ID，在 keycloak.json 的 {@code credentials} 子元素中引用。
     *
     * <p>例如提供者 ID 为 {@code kerberos-keytab} 时，配置示例：</p>
     * <pre>
     * "credentials": {
     *     "kerberos-keytab": {
     *         "keytab": "/tmp/foo"
     *     }
     * }
     * </pre>
     *
     * @return 提供者唯一标识
     */
    String getId();

    /**
     * 应用部署时由适配器调用，可在此读取配置并初始化认证器。
     *
     * @param adapterConfig 适配器配置
     * @param config 从 keycloak.json 读取的本提供者配置；以上述 kerberos-keytab 为例，返回含键 {@code keytab}、值 {@code /tmp/foo} 的 Map
     */
    void init(AdapterConfig adapterConfig, Object config);

    /**
     * 每次适配器发起反向通道请求时调用：向 HTTP 头或表单参数写入客户端认证凭据。
     *
     * @param adapterConfig 已解析的部署配置
     * @param requestHeaders 应写入的 HTTP 请求头，将随请求发送至 Keycloak
     * @param formParams 应写入的表单参数，将随请求发送至 Keycloak
     */
    void setClientCredentials(AdapterConfig adapterConfig, Map<String, String> requestHeaders, Map<String, String> formParams);
}
