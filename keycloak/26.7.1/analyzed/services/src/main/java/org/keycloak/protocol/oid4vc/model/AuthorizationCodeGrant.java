/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 凭证发放（Credential Offer）中的授权码（authorization_code）授权类型容器。
 * <p>实现 {@link CredentialOfferGrant}，携带 {@code issuer_state} 供钱包在后续授权流程中关联签发方状态。规范见 {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-offer}。</p>
 *
 * @author <a href="mailto:tdiesler@ibm.com">Thomas Diesler</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizationCodeGrant implements CredentialOfferGrant {

    /** 授权类型键名：{@code authorization_code}。 */
    public static final String AUTH_CODE_GRANT_TYPE = "authorization_code";
    /** JSON 字段名：签发方状态 {@code issuer_state}。 */
    public static final String ISSUER_STATE = "issuer_state";

    @Override
    @JsonIgnore
    public String getGrantType() {
        return AUTH_CODE_GRANT_TYPE;
    }

    /** 签发方状态值，用于授权码流程中关联 offer 上下文。 */
    @JsonProperty(ISSUER_STATE)
    private String issuerState;

    /** @return 签发方状态值 */
    public String getIssuerState() {
        return issuerState;
    }

    /** @param issuerState 签发方状态值 */
    public AuthorizationCodeGrant setIssuerState(String issuerState) {
        this.issuerState = issuerState;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorizationCodeGrant grant)) return false;
        return Objects.equals(issuerState, grant.issuerState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issuerState);
    }
}
