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

package org.keycloak.client.registration;

import java.io.IOException;
import java.io.InputStream;

import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Keycloak 客户端动态注册 Java API 的主入口。
 * <p>
 * 封装对 {@code /realms/{realm}/clients-registrations/} 端点的 CRUD 操作，
 * 支持 default、OIDC、SAML 及 adapter install 等多种注册协议；
 * 通过 {@link ClientRegistrationBuilder} 配置 realm URL 与 {@link HttpClient} 后构建实例。
 * </p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientRegistration {

    /**
     * 序列化输出用 ObjectMapper：混入 MixIn 以隐藏敏感/只读字段，并忽略 null 值。
     */
    public static final ObjectMapper outputMapper = new ObjectMapper();
    static {
        outputMapper.addMixIn(ClientRepresentation.class, ClientRepresentationMixIn.class);
        outputMapper.addMixIn(OIDCClientRepresentation.class, OIDCClientRepresentationMixIn.class);
        outputMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private final String JSON = "application/json";
    private final String XML = "application/xml";

    /** default 端点路径段。 */
    private final String DEFAULT = "default";
    /** adapter 安装配置端点路径段。 */
    private final String INSTALLATION = "install";
    /** OIDC 动态客户端注册端点路径段。 */
    private final String OIDC = "openid-connect";
    /** SAML 2.0 实体描述符端点路径段。 */
    private final String SAML = "saml2-entity-descriptor";

    private HttpUtil httpUtil;

    /**
     * 创建 {@link ClientRegistrationBuilder} 以配置并构建客户端注册实例。
     *
     * @return 新的构建器
     */
    public static ClientRegistrationBuilder create() {
        return new ClientRegistrationBuilder();
    }

    ClientRegistration(HttpUtil httpUtil) {
        this.httpUtil = httpUtil;
    }

    /**
     * 关闭底层 HTTP 客户端并释放资源。
     *
     * @throws ClientRegistrationException 关闭失败时抛出
     */
    public void close() throws ClientRegistrationException {
        if (httpUtil != null) {
            httpUtil.close();
        }
        httpUtil = null;
    }

    /**
     * 设置后续请求的认证策略。
     *
     * @param auth {@link Auth} 实例
     * @return 当前 {@link ClientRegistration}（链式调用）
     */
    public ClientRegistration auth(Auth auth) {
        httpUtil.setAuth(auth);
        return this;
    }

    /**
     * 在 default 端点注册新客户端。
     *
     * @param client 客户端表示
     * @return 服务端返回的客户端（含 registrationAccessToken）
     * @throws ClientRegistrationException 请求或序列化失败
     */
    public ClientRepresentation create(ClientRepresentation client) throws ClientRegistrationException {
        String content = serialize(client);
        InputStream resultStream = httpUtil.doPost(content, JSON, UTF_8, JSON, DEFAULT);
        return deserialize(resultStream, ClientRepresentation.class);
    }

    /**
     * 按 clientId 获取 default 格式的客户端配置。
     *
     * @param clientId 客户端标识
     * @return 客户端表示，404 时返回 {@code null}
     */
    public ClientRepresentation get(String clientId) throws ClientRegistrationException {
        InputStream resultStream = httpUtil.doGet(JSON, DEFAULT, clientId);
        return resultStream != null ? deserialize(resultStream, ClientRepresentation.class) : null;
    }

    /**
     * 获取指定客户端的 adapter 安装配置（JSON 格式）。
     *
     * @param clientId 客户端标识
     * @return {@link AdapterConfig}，不存在时返回 {@code null}
     */
    public AdapterConfig getAdapterConfig(String clientId) throws ClientRegistrationException {
        InputStream resultStream = httpUtil.doGet(JSON, INSTALLATION, clientId);
        return resultStream != null ? deserialize(resultStream, AdapterConfig.class) : null;
    }

    /**
     * 更新 default 端点上的客户端配置。
     *
     * @param client 含 clientId 的完整或部分客户端表示
     * @return 更新后的客户端表示
     */
    public ClientRepresentation update(ClientRepresentation client) throws ClientRegistrationException {
        String content = serialize(client);
        InputStream resultStream = httpUtil.doPut(content, JSON, UTF_8, JSON, DEFAULT, client.getClientId());
        return resultStream != null ? deserialize(resultStream, ClientRepresentation.class) : null;
    }

    /** 按客户端表示中的 clientId 删除客户端。 */
    public void delete(ClientRepresentation client) throws ClientRegistrationException {
        delete(client.getClientId());
    }

    /**
     * 按 clientId 删除客户端。
     *
     * @param clientId 客户端标识
     */
    public void delete(String clientId) throws ClientRegistrationException {
        httpUtil.doDelete(DEFAULT, clientId);
    }

    /** 获取 OIDC 动态客户端注册 API 的嵌套访问器。 */
    public OIDCClientRegistration oidc() {
        return new OIDCClientRegistration();
    }

    /** 获取 SAML 2.0 客户端注册 API 的嵌套访问器。 */
    public SAMLClientRegistration saml() {
        return new SAMLClientRegistration();
    }

    /**
     * 将对象序列化为 JSON 字符串（应用 MixIn 与 NON_NULL 策略）。
     *
     * @param obj 待序列化对象
     * @return JSON 字符串
     */
    public static String serialize(Object obj) throws ClientRegistrationException {
        try {
            return outputMapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new ClientRegistrationException("Failed to write json object", e);
        }
    }

    /** 从响应流反序列化为指定类型。 */
    private static <T> T deserialize(InputStream inputStream, Class<T> clazz) throws ClientRegistrationException {
        try {
            return JsonSerialization.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new ClientRegistrationException("Failed to read json object", e);
        }
    }

    /**
     * OIDC 动态客户端注册（{@code openid-connect} 端点）的操作集合。
     */
    public class OIDCClientRegistration {

        /** 注册新 OIDC 客户端。 */
        public OIDCClientRepresentation create(OIDCClientRepresentation client) throws ClientRegistrationException {
            String content = serialize(client);
            InputStream resultStream = httpUtil.doPost(content, JSON, UTF_8, JSON, OIDC);
            return deserialize(resultStream, OIDCClientRepresentation.class);
        }

        /** 按 clientId 获取 OIDC 格式客户端配置。 */
        public OIDCClientRepresentation get(String clientId) throws ClientRegistrationException {
            InputStream resultStream = httpUtil.doGet(JSON, OIDC, clientId);
            return resultStream != null ? deserialize(resultStream, OIDCClientRepresentation.class) : null;
        }

        /** 更新 OIDC 客户端配置。 */
        public OIDCClientRepresentation update(OIDCClientRepresentation client) throws ClientRegistrationException {
            String content = serialize(client);
            InputStream resultStream = httpUtil.doPut(content, JSON, UTF_8, JSON, OIDC, client.getClientId());
            return resultStream != null ? deserialize(resultStream, OIDCClientRepresentation.class) : null;
        }

        /** 按 OIDC 客户端表示删除客户端。 */
        public void delete(OIDCClientRepresentation client) throws ClientRegistrationException {
            delete(client.getClientId());
        }

        /** 按 clientId 删除 OIDC 客户端。 */
        public void delete(String clientId) throws ClientRegistrationException {
            httpUtil.doDelete(OIDC, clientId);
        }

    }

    /**
     * SAML 2.0 实体描述符注册（{@code saml2-entity-descriptor} 端点）的操作集合。
     */
    public class SAMLClientRegistration {

        /**
         * 提交 SAML EntityDescriptor XML 以注册 SAML 客户端。
         *
         * @param entityDescriptor SAML 元数据 XML 字符串
         * @return 注册后的 {@link ClientRepresentation}
         */
        public ClientRepresentation create(String entityDescriptor) throws ClientRegistrationException {
            InputStream resultStream = httpUtil.doPost(entityDescriptor, XML, UTF_8, JSON, SAML);
            return deserialize(resultStream, ClientRepresentation.class);
        }

    }

    /**
     * {@link ClientRegistration} 的流式构建器。
     */
    public static class ClientRegistrationBuilder {

        private String url;
        private HttpClient httpClient;

        ClientRegistrationBuilder() {
        }

        /**
         * 直接指定客户端注册 API 的完整 base URL。
         *
         * @param realmUrl 形如 {@code https://host/realms/{realm}/clients-registrations}
         */
        public ClientRegistrationBuilder url(String realmUrl) {
            url = realmUrl;
            return this;
        }

        /**
         * 由认证服务器 URL 与 realm 名拼装注册 API base URL。
         *
         * @param authUrl Keycloak 基础 URL
         * @param realm realm 名称
         */
        public ClientRegistrationBuilder url(String authUrl, String realm) {
            url = HttpUtil.getUrl(authUrl, "realms", realm, "clients-registrations");
            return this;
        }

        /**
         * 指定自定义 Apache {@link HttpClient}（默认使用 {@link HttpClients#createDefault()}）。
         */
        public ClientRegistrationBuilder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * 构建 {@link ClientRegistration} 实例。
         *
         * @return 配置完成的客户端注册客户端
         * @throws IllegalStateException 未设置 url 时
         */
        public ClientRegistration build() {
            if (url == null) {
                throw new IllegalStateException("url not configured");
            }

            if (httpClient == null) {
                httpClient = HttpClients.createDefault();
            }

            return new ClientRegistration(new HttpUtil(httpClient, url));
        }

    }

}
