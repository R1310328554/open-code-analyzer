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
 */

package org.keycloak.protocol.oidc.grants.ciba.channel;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.OAuth2Constants;
import org.keycloak.protocol.oidc.grants.ciba.CibaGrantType;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CIBA 认证通道 HTTP 请求体：消费设备（CD）向认证设备（AD）传递的后台认证参数。
 * <p>序列化为 JSON 后 POST 至配置的认证通道 URI。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AuthenticationChannelRequest {

    /** 绑定消息，用于关联 CIBA 流程与用户侧认证 */
    @JsonProperty(CibaGrantType.BINDING_MESSAGE)
    private String bindingMessage;

    /** 登录提示（如用户名、邮箱），供认证设备识别用户 */
    @JsonProperty(CibaGrantType.LOGIN_HINT)
    private String loginHint;

    /** 是否需要在认证设备上展示同意界面 */
    @JsonProperty(CibaGrantType.IS_CONSENT_REQUIRED)
    private Boolean consentRequired;

    /** 请求的认证上下文类参考值（ACR） */
    @JsonProperty(OAuth2Constants.ACR_VALUES)
    private String acrValues;

    /** 附加自定义参数（Jackson 任意属性映射） */
    private Map<String, Object> additionalParameters = new HashMap<>();

    /** 请求的 OAuth2/OIDC 作用域 */
    private String scope;

    /** 设置绑定消息 */
    public void setBindingMessage(String bindingMessage) {
        this.bindingMessage = bindingMessage;
    }

    /** @return 绑定消息 */
    public String getBindingMessage() {
        return bindingMessage;
    }

    /** 设置登录提示 */
    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }

    /** @return 登录提示 */
    public String getLoginHint() {
        return loginHint;
    }

    /** 设置是否需要用户同意 */
    public void setConsentRequired(Boolean consentRequired) {
        this.consentRequired = consentRequired;
    }

    /** @return 是否需要用户同意 */
    public Boolean getConsentRequired() {
        return consentRequired;
    }

    /** @return ACR 值 */
    public String getAcrValues() {
        return acrValues;
    }

    /** 设置 ACR 值 */
    public void setAcrValues(String acrValues) {
        this.acrValues = acrValues;
    }

    /** 设置作用域 */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /** @return 作用域 */
    public String getScope() {
        return scope;
    }

    /** @return 附加参数映射 */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalParameters() {
        return additionalParameters;
    }

    /** 批量设置附加参数 */
    public void setAdditionalParameters(Map<String, Object> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    /** 设置单个附加参数（Jackson 反序列化入口） */
    @JsonAnySetter
    public void setAdditionalParameter(String name, String value) {
        additionalParameters.put(name, value);
    }
}
