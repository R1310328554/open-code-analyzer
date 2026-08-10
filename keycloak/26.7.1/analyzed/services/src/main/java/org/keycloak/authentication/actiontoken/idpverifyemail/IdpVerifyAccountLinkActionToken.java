/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authentication.actiontoken.idpverifyemail;

import org.keycloak.authentication.actiontoken.DefaultActionToken;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of a token that represents a time-limited verify e-mail action.
 *
 * @author hmlnarik
 */
public class IdpVerifyAccountLinkActionToken extends DefaultActionToken {

    /** 令牌类型 idp-verify-account-via-email。 */
    public static final String TOKEN_TYPE = "idp-verify-account-via-email";

    /** JSON 字段：身份提供者用户名。 */
    private static final String JSON_FIELD_IDENTITY_PROVIDER_USERNAME = "idpu";
    /** JSON 字段：IdP 外部用户 ID。 */
    private static final String JSON_FIELD_IDENTITY_PROVIDER_EXTERNAL_ID = "idpuid";
    /** JSON 字段：身份提供者别名。 */
    private static final String JSON_FIELD_IDENTITY_PROVIDER_ALIAS = "idpa";
    /** JSON 字段：原始复合认证会话 ID。 */
    private static final String JSON_FIELD_ORIGINAL_AUTHENTICATION_SESSION_ID = "oasid";

    @JsonProperty(value = JSON_FIELD_IDENTITY_PROVIDER_USERNAME)
    /** 身份提供者侧用户名。 */
    private String identityProviderUsername;

    @JsonProperty(value = JSON_FIELD_IDENTITY_PROVIDER_ALIAS)
    /** 身份提供者别名。 */
    private String identityProviderAlias;

    @JsonProperty(value = JSON_FIELD_ORIGINAL_AUTHENTICATION_SESSION_ID)
    /** 发起关联流程的原始认证会话 ID。 */
    private String originalAuthenticationSessionId;

    @JsonProperty(value = JSON_FIELD_IDENTITY_PROVIDER_EXTERNAL_ID)
    /** IdP 外部用户唯一标识。 */
    private String externalId;

    /** 构造 IdP 账户关联邮箱验证令牌。 */
    public IdpVerifyAccountLinkActionToken(String userId, String email, int absoluteExpirationInSecs, String compoundAuthenticationSessionId,
      String identityProviderUsername, String externalId, String identityProviderAlias, String clientId) {
        super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null, compoundAuthenticationSessionId);
        this.identityProviderUsername = identityProviderUsername;
        this.externalId = externalId;
        this.identityProviderAlias = identityProviderAlias;
        this.issuedFor = clientId;
        setEmail(email);
    }

    private IdpVerifyAccountLinkActionToken() {
    }

    /** @return 身份提供者用户名 */
    public String getIdentityProviderUsername() {
        return identityProviderUsername;
    }

    public void setIdentityProviderUsername(String identityProviderUsername) {
        this.identityProviderUsername = identityProviderUsername;
    }

    /** @return IdP 外部用户 ID */
    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /** @return 身份提供者别名 */
    public String getIdentityProviderAlias() {
        return identityProviderAlias;
    }

    public void setIdentityProviderAlias(String identityProviderAlias) {
        this.identityProviderAlias = identityProviderAlias;
    }

    /** @return 原始复合认证会话 ID */
    public String getOriginalCompoundAuthenticationSessionId() {
        return originalAuthenticationSessionId;
    }

    public void setOriginalCompoundAuthenticationSessionId(String originalCompoundAuthenticationSessionId) {
        this.originalAuthenticationSessionId = originalCompoundAuthenticationSessionId;
    }

}
