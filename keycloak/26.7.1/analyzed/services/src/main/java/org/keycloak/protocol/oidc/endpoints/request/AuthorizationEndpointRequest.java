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

package org.keycloak.protocol.oidc.endpoints.request;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.protocol.ClientData;
import org.keycloak.rar.AuthorizationRequestContext;

/**
 * OIDC/OAuth2 授权端点请求参数模型。
 * <p>封装查询串、Request Object 或 PAR 解析后的标准与扩展参数。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationEndpointRequest {

    /** 请求无效时的错误描述（解析阶段设置）。 */
    String invalidRequestMessage;

    String clientId;
    String redirectUri;
    String responseType;
    String responseMode;
    String state;
    String scope;
    String resource;
    String loginHint;
    String display;
    String prompt;
    String nonce;
    Integer maxAge;
    String idpHint;
    String action;
    String claims;
    String uiLocales;
    /** 未映射到标准字段的额外请求参数。 */
    Map<String, String> additionalReqParams = new HashMap<>();

    // RFC 7636 §6.1 PKCE 参数
    String codeChallenge;
    String codeChallengeMethod;

    String dpopJkt;

    String acr;

    /** 参数化 scope 解析后的 RAR 授权请求上下文。 */
    AuthorizationRequestContext authorizationRequestContext;

    /** 获取Acr。 */
    public String getAcr() {
        return acr;
    }

    /** 获取ClientId。 */
    public String getClientId() {
        return clientId;
    }

    /** 获取RedirectUri。 */
    public String getRedirectUri() {
        return redirectUri;
    }

    /** 从 {@link ClientData} 构建最小授权请求（response_type/mode/redirect_uri）。 */
    public static AuthorizationEndpointRequest fromClientData(ClientData cData) {
        AuthorizationEndpointRequest request = new AuthorizationEndpointRequest();
        request.responseType = cData.getResponseType();
        request.responseMode = cData.getResponseMode();
        request.redirectUri = cData.getRedirectUri();
        return request;
    }

    /** 获取ResponseType。 */
    public String getResponseType() {
        return responseType;
    }

    /** 获取ResponseMode。 */
    public String getResponseMode() {
        return responseMode;
    }

    /** 获取State。 */
    public String getState() {
        return state;
    }

    /** 获取Scope。 */
    public String getScope() {
        return scope;
    }

    /** 获取Resource。 */
    public String getResource() {
        return resource;
    }

    /** 获取LoginHint。 */
    public String getLoginHint() {
        return loginHint;
    }

    /** 获取Prompt。 */
    public String getPrompt() {
        return prompt;
    }

    /** 获取Nonce。 */
    public String getNonce() {
        return nonce;
    }

    /** 获取MaxAge。 */
    public Integer getMaxAge() {
        return maxAge;
    }

    /** 获取IdpHint。 */
    public String getIdpHint() {
        return idpHint;
    }

    /** 获取Action。 */
    public String getAction() {
        return action;
    }

    /** 获取Claims。 */
    public String getClaims() {
        return claims;
    }

    /** 获取AdditionalReqParams。 */
    public Map<String, String> getAdditionalReqParams() {
        return additionalReqParams;
    }

    // https://tools.ietf.org/html/rfc7636#section-6.1
    /** 获取CodeChallenge。 */
    public String getCodeChallenge() {
        return codeChallenge;
    }

    // https://tools.ietf.org/html/rfc7636#section-6.1
    /** 获取CodeChallengeMethod。 */
    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    /** 获取DpopJkt。 */
    public String getDpopJkt() { return dpopJkt; }

    /** 设置DpopJkt。 */
    public void setDpopJkt(String dpopJkt) { this.dpopJkt = dpopJkt; }

    /** 获取Display。 */
    public String getDisplay() {
        return display;
    }

    /** 获取InvalidRequestMessage。 */
    public String getInvalidRequestMessage() {
        return invalidRequestMessage;
    }

    /** 设置InvalidRequestMessage。 */
    public void setInvalidRequestMessage(String invalidRequestMessage) {
        this.invalidRequestMessage = invalidRequestMessage;
    }

    /** 获取UiLocales。 */
    public String getUiLocales() {
        return uiLocales;
    }

    /** 获取AuthorizationRequestContext。 */
    public AuthorizationRequestContext getAuthorizationRequestContext() {
        return authorizationRequestContext;
    }

    /** 设置AuthorizationRequestContext。 */
    public void setAuthorizationRequestContext(AuthorizationRequestContext authorizationRequestContext) {
        this.authorizationRequestContext = authorizationRequestContext;
    }
}
