/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.util.List;
import java.util.Objects;

import org.keycloak.representations.AuthorizationDetailsJSONRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.keycloak.OID4VCConstants.CREDENTIAL_CONFIGURATION_ID;
import static org.keycloak.OID4VCConstants.CREDENTIAL_IDENTIFIERS;

/**
 * OID4VCI 令牌请求中的 {@code authorization_details} 对象。
 * <p>扩展 OAuth 2.0 授权详情，携带凭证配置 ID、声明约束及发放流程关联标识。</p>
 *
 * @author <a href="mailto:Forkim.Akwichek@adorsys.com">Forkim Akwichek</a>
 */
public class OID4VCAuthorizationDetail extends AuthorizationDetailsJSONRepresentation implements Cloneable {

    /** JSON 字段名：声明描述列表 {@code claims}。 */
    public static final String CLAIMS = "claims";
    /** JSON 字段名：凭证发放 ID {@code credentials_offer_id}。 */
    public static final String CREDENTIALS_OFFER_ID = "credentials_offer_id";

    /** 访问/刷新令牌中引用的已签发凭证 ID，用于凭证请求时校验凭证是否仍存在。 */
    public static final String ISSUED_CREDENTIAL_ID = "issued_credential_id";

    /** 凭证配置标识符。 */
    @JsonProperty(CREDENTIAL_CONFIGURATION_ID)
    private String credentialConfigurationId;

    /**
     * 颁发者在访问令牌响应中填充的凭证标识符列表。
     * <p>用于在整个发放流程中标识待签发凭证；不应出现在授权或访问令牌请求中。</p>
     */
    @JsonProperty(CREDENTIAL_IDENTIFIERS)
    private List<String> credentialIdentifiers;

    /** 钱包请求的声明描述列表。 */
    @JsonProperty(CLAIMS)
    private List<ClaimsDescription> claims;

    /** 关联的凭证发放 ID。 */
    @JsonProperty(CREDENTIALS_OFFER_ID)
    private String credentialsOfferId;

    /** 已签发凭证的内部 ID。 */
    @JsonProperty(ISSUED_CREDENTIAL_ID)
    private String issuedCredentialId;

    /** @return 凭证配置 ID */
    public String getCredentialConfigurationId() {
        return credentialConfigurationId;
    }

    /** @param credentialConfigurationId 凭证配置 ID */
    public void setCredentialConfigurationId(String credentialConfigurationId) {
        this.credentialConfigurationId = credentialConfigurationId;
    }

    /** @return 凭证标识符列表 */
    public List<String> getCredentialIdentifiers() {
        return credentialIdentifiers;
    }

    /** @param credentialIdentifiers 凭证标识符列表 */
    public void setCredentialIdentifiers(List<String> credentialIdentifiers) {
        this.credentialIdentifiers = credentialIdentifiers;
    }

    /** @return 凭证发放 ID */
    public String getCredentialsOfferId() {
        return credentialsOfferId;
    }

    /** @param credentialsOfferId 凭证发放 ID */
    public void setCredentialsOfferId(String credentialsOfferId) {
        this.credentialsOfferId = credentialsOfferId;
    }

    /** @return 已签发凭证 ID */
    public String getIssuedCredentialId() {
        return issuedCredentialId;
    }

    /** @param issuedCredentialId 已签发凭证 ID */
    public void setIssuedCredentialId(String issuedCredentialId) {
        this.issuedCredentialId = issuedCredentialId;
    }

    /** @return 声明描述列表 */
    public List<ClaimsDescription> getClaims() {
        return claims;
    }

    /** @param claims 声明描述列表 */
    public void setClaims(List<ClaimsDescription> claims) {
        this.claims = claims;
    }

    /** @return 通过 JSON 序列化实现的深拷贝 */
    @Override
    public OID4VCAuthorizationDetail clone() {
        String encoded = JsonSerialization.valueAsString(this);
        return JsonSerialization.valueFromString(encoded, OID4VCAuthorizationDetail.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OID4VCAuthorizationDetail that = (OID4VCAuthorizationDetail) o;
        return Objects.equals(credentialConfigurationId, that.credentialConfigurationId)
                && Objects.equals(credentialIdentifiers, that.credentialIdentifiers)
                && Objects.equals(credentialsOfferId, that.credentialsOfferId)
                && Objects.equals(issuedCredentialId, that.issuedCredentialId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                credentialConfigurationId, credentialIdentifiers, credentialsOfferId, issuedCredentialId);
    }

    @Override
    public String toString() {
        return JsonSerialization.valueAsString(this);
    }
}
