/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.grants;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.provider.Provider;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.cors.Cors;

/**
 * OAuth 2.0 授权类型（Grant Type）提供者接口：处理 Token 端点上的各类 grant 请求。
 * <p>每种 grant 实现 {@link #process(Context)} 完成令牌签发逻辑。</p>
 *
 * @author <a href="mailto:demetrio@carretti.pro">Dmitry Telegin</a>
 */
public interface OAuth2GrantType extends Provider {

    /** @return 与本 grant 类型关联的 {@link EventType} */

    EventType getEventType();

    /**
     * @return 本 grant 允许重复出现的请求参数名集合；未列出的参数若出现多值则通常拒绝请求
     */
    default Set<String> getSupportedMultivaluedRequestParameters() {
        return Collections.emptySet();
    }

    /**
     * @return 本 grant 支持的“令牌型”参数名集合（如 token exchange 的 {@code subject_token}，可能含大型 JWT/SAML）
     */
    Set<String> getTokenParameterNames();

    /** 在 grant 处理前执行客户端策略预处理。 */

    default void preProcess(KeycloakSession session, MultivaluedMap<String, String> formParams) throws ClientPolicyException {
        // 默认无预处理
    }

    /**
     * 处理 grant 请求并返回令牌响应。
     * @param context grant request context
     *
     * @return token response
     */
    Response process(Context context);

    /**
     * 检查本 grant 签发的令牌是否允许用于当前请求（可限制特定端点）。
     * <p>默认返回 {@code true}；需限制用途的 grant 应覆盖此方法。</p>
     *
     * @param session the Keycloak session
     * @param token   the access token
     * @return true if the token is allowed for the current request, false otherwise
     */
    default boolean isTokenAllowed(KeycloakSession session, AccessToken token) {
        return true;
    }

    /** Grant 请求上下文：封装会话、客户端、表单参数、CORS 等运行时信息。 */
    public static class Context {
        protected KeycloakSession session;
        protected RealmModel realm;
        protected ClientModel client;
        protected Object clientConfig;
        protected ClientConnection clientConnection;
        protected Map<String, String> clientAuthAttributes;
        protected HttpRequest request;
        protected HttpResponse response;
        protected HttpHeaders headers;
        protected MultivaluedMap<String, String> formParams;
        protected EventBuilder event;
        protected Cors cors;
        protected Object tokenManager;
        protected String grantType;
        protected LoginProtocol protocol;

        /** 从会话与请求参数构建 grant 上下文。 */
        public Context(KeycloakSession session, Object clientConfig, Map<String, String> clientAuthAttributes,
                MultivaluedMap<String, String> formParams, EventBuilder event, Cors cors, Object tokenManager) {
            this.session = session;
            this.realm = session.getContext().getRealm();
            this.client = session.getContext().getClient();
            this.clientConfig = clientConfig;
            this.clientConnection = session.getContext().getConnection();
            this.clientAuthAttributes = clientAuthAttributes;
            this.request = session.getContext().getHttpRequest();
            this.response = session.getContext().getHttpResponse();
            this.headers = session.getContext().getRequestHeaders();
            this.formParams = formParams;
            this.event = event;
            this.cors = cors;
            this.tokenManager = tokenManager;
            this.grantType = formParams.getFirst(OAuth2Constants.GRANT_TYPE);
            if (this.client != null) {
                String protocolName = this.client.getProtocol() != null ? this.client.getProtocol() : Constants.OIDC_PROTOCOL;
                this.protocol = session.getProvider(LoginProtocol.class, protocolName);
            }
        }

        public void setFormParams(MultivaluedHashMap<String, String> formParams) {
            this.formParams = formParams;
        }

        public void setClient(ClientModel client) {
            this.client = client;
            if (client != null) {
                String protocolName = this.client.getProtocol() != null ? this.client.getProtocol() : Constants.OIDC_PROTOCOL;
                this.protocol = session.getProvider(LoginProtocol.class, protocolName);
            }
        }

        public void setClientConfig(Object clientConfig) {
            this.clientConfig = clientConfig;
        }

        public void setClientAuthAttributes(Map<String, String> clientAuthAttributes) {
            this.clientAuthAttributes = clientAuthAttributes;
        }

        /** @return 当前客户端模型 */
        public ClientModel getClient() {
            return client;
        }

        public Map<String, String> getClientAuthAttributes() {
            return clientAuthAttributes;
        }

        public Object getClientConfig() {
            return clientConfig;
        }

        public ClientConnection getClientConnection() {
            return clientConnection;
        }

        public Cors getCors() {
            return cors;
        }

        public EventBuilder getEvent() {
            return event;
        }

        public MultivaluedMap<String, String> getFormParams() {
            return formParams;
        }

        public HttpHeaders getHeaders() {
            return headers;
        }

        /** @return 当前 realm */
        public RealmModel getRealm() {
            return realm;
        }

        public HttpRequest getRequest() {
            return request;
        }

        public HttpResponse getResponse() {
            return response;
        }

        /** @return Keycloak 会话 */
        public KeycloakSession getSession() {
            return session;
        }

        public Object getTokenManager() {
            return tokenManager;
        }

        /** @return 请求中的 grant_type 参数值 */
        public String getGrantType() {
            return grantType;
        }
    }

}
