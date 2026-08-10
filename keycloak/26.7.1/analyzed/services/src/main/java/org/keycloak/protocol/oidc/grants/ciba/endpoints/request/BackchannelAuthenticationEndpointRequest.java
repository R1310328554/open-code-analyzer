/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 *
 */
package org.keycloak.protocol.oidc.grants.ciba.endpoints.request;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台认证端点请求参数容器。
 * <p>由请求解析器填充，包含 scope、用户提示、binding_message 等 CIBA/OIDC 参数。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class BackchannelAuthenticationEndpointRequest {

    /** 请求的 OAuth scope */
    String scope;
    /** 客户端通知令牌（ping 模式） */
    String clientNotificationToken;
    /** 认证上下文类引用（ACR） */
    String acr;
    /** login_hint_token 参数 */
    String loginHintToken;
    /** id_token_hint 参数 */
    String idTokenHint;
    /** login_hint 参数 */
    String loginHint;
    /** 绑定消息（供 AD 展示） */
    String bindingMessage;
    /** 用户码（CIBA 不支持，保留字段） */
    String userCode;
    /** 请求的认证过期时间（秒） */
    Integer requestedExpiry;

    /** OIDC prompt 参数 */
    String prompt;
    /** OIDC nonce 参数 */
    String nonce;
    /** OIDC max_age 参数 */
    Integer maxAge;
    /** OIDC display 参数 */
    String display;
    /** OIDC ui_locales 参数 */
    String uiLocales;
    /** OIDC claims 参数 */
    String claims;

    /** 未在已知参数列表中的附加请求参数 */
    Map<String, String> additionalReqParams = new HashMap<>();

    /** 无效请求错误消息（如重复参数） */
    String invalidRequestMessage;

    /** @return scope 参数值 */
    public String getScope() {
        return scope;
    }

    public String getClientNotificationToken() {
        return clientNotificationToken;
    }

    public String getAcr() {
        return acr;
    }

    public String getLoginHintToken() {
        return loginHintToken;
    }

    public String getIdTokenHint() {
        return idTokenHint;
    }

    public String getLoginHint() {
        return loginHint;
    }

    public String getBindingMessage() {
        return bindingMessage;
    }

    public String getUserCode() {
        return userCode;
    }

    public Integer getRequestedExpiry() {
        return requestedExpiry;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getNonce() {
        return nonce;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public String getDisplay() {
        return display;
    }

    public String getUiLocales() {
        return uiLocales;
    }

    public String getClaims() {
        return claims;
    }

    public Map<String, String> getAdditionalReqParams() {
        return additionalReqParams;
    }


    /** @return 无效请求消息，无错误时返回 null */
    public String getInvalidRequestMessage() {
        return invalidRequestMessage;
    }
}
