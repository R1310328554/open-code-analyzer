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

package org.keycloak.services.clientregistration;

import java.util.Set;

import org.keycloak.representations.JsonWebToken;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 注册访问令牌 JWT 表示。
 * <p>客户端注册成功后签发，用于后续对该客户端的查看、更新与删除操作，并携带 {@link org.keycloak.services.clientregistration.policy.RegistrationAuth} 授权级别。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RegistrationAccessToken extends JsonWebToken {

    /** JWT 声明名：注册授权级别 */
    public static final String REGISTRATION_AUTH = "registration_auth";

    /** 注册授权级别（如 authenticated、anonymous 等） */
    @JsonProperty(REGISTRATION_AUTH)
    protected String registrationAuth;

    /** 允许调用注册端点的 Web 来源集合 */
    @JsonProperty("allowed-origins")
    protected Set<String> allowedOrigins;

    /** @return 注册授权级别 */
    public String getRegistrationAuth() {
        return registrationAuth;
    }

    /** @param registrationAuth 注册授权级别 */
    public void setRegistrationAuth(String registrationAuth) {
        this.registrationAuth = registrationAuth;
    }

    /** @return 允许的 Web 来源 */
    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /** @param allowedOrigins 允许的 Web 来源 */
    public void setAllowedOrigins(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
