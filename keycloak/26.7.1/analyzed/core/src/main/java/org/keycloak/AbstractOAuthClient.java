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

package org.keycloak;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.keycloak.common.enums.RelativeUrlsUsed;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.common.util.SecretGenerator;

/**
 * OAuth/OIDC 客户端实现的抽象基类，封装授权端点、令牌端点及 state 管理等通用配置。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AbstractOAuthClient {
    /** OAuth 令牌请求 state 在 Cookie 中的默认键名。 */
    private static final String OAUTH_TOKEN_REQUEST_STATE = "OAuth_Token_Request_State";
    /** 用于生成唯一 state 值的递增计数器。 */
    private final AtomicLong counter = new AtomicLong();

    /** OAuth 客户端标识符。 */
    protected String clientId;
    /** 客户端凭证（如密钥、JWT 等）。 */
    protected Map<String, Object> credentials;
    /** 授权端点 URL。 */
    protected String authUrl;
    /** 令牌端点 URL。 */
    protected String tokenUrl;
    /** 相对 URL 使用策略。 */
    protected RelativeUrlsUsed relativeUrlsUsed;
    /** 请求的 OAuth scope。 */
    protected String scope;
    /** 存储 OAuth state 的 Cookie 名称。 */
    protected String stateCookieName = OAUTH_TOKEN_REQUEST_STATE;
    /** state Cookie 的路径。 */
    protected String stateCookiePath;
    /** 是否通过 HTTPS 通信。 */
    protected boolean isSecure;
    /** 是否为公开客户端（无 client_secret）。 */
    protected boolean publicClient;

    /** 生成「计数器/安全随机 ID」格式的 state 码。 */
    protected String getStateCode() {
        return counter.getAndIncrement() + "/" + SecretGenerator.getInstance().generateSecureID();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, Object> credentials) {
        this.credentials = credentials;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getStateCookieName() {
        return stateCookieName;
    }

    public void setStateCookieName(String stateCookieName) {
        this.stateCookieName = stateCookieName;
    }

    public String getStateCookiePath() {
        return stateCookiePath;
    }

    public void setStateCookiePath(String stateCookiePath) {
        this.stateCookiePath = stateCookiePath;
    }

    public boolean isPublicClient() {
        return publicClient;
    }

    public void setPublicClient(boolean publicClient) {
        this.publicClient = publicClient;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public void setSecure(boolean secure) {
        isSecure = secure;
    }

    public RelativeUrlsUsed getRelativeUrlsUsed() {
        return relativeUrlsUsed;
    }

    public void setRelativeUrlsUsed(RelativeUrlsUsed relativeUrlsUsed) {
        this.relativeUrlsUsed = relativeUrlsUsed;
    }

    /**
     * 从重定向 URI 中移除 {@code code} 与 {@code state} 查询参数。
     *
     * @param uri 原始重定向 URI
     * @return 去除 OAuth 参数后的 URI 字符串
     */
    protected String stripOauthParametersFromRedirect(String uri) {
        KeycloakUriBuilder builder = KeycloakUriBuilder.fromUri(uri)
                .replaceQueryParam(OAuth2Constants.CODE, null)
                .replaceQueryParam(OAuth2Constants.STATE, null);
        return builder.buildAsString();
    }

}
