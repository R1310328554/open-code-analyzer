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

package org.keycloak.protocol.oid4vc.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static org.keycloak.OID4VCConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER;
import static org.keycloak.protocol.oid4vc.model.AuthorizationCodeGrant.AUTH_CODE_GRANT_TYPE;
import static org.keycloak.protocol.oid4vc.model.PreAuthorizedCodeGrant.PRE_AUTH_GRANT_TYPE;

/**
 * OID4VCI 规范中的凭证发放（Credential Offer）模型。
 * <p>描述签发者、可发放凭证配置 ID 及授权码/预授权码等 grant 信息。</p>
 * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-offer}
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialsOffer {

    /** 凭证签发者标识 URI。 */
    @JsonProperty("credential_issuer")
    private String credentialIssuer;

    // 签发者元数据中提供的凭证配置 ID 列表
    /** 本次发放包含的凭证配置 ID 列表。 */
    @JsonProperty("credential_configuration_ids")
    private List<String> credentialConfigurationIds;

    /** grant 映射（键为 grant 类型，如 authorization_code、urn:ietf:params:oauth:grant-type:pre-authorized_code）。 */
    @JsonProperty("grants")
    @JsonDeserialize(using = CredentialOfferGrantsDeserializer.class)
    private Map<String, CredentialOfferGrant> grants = new HashMap<>();

    /** @return 凭证签发者 URI */
    public String getCredentialIssuer() {
        return credentialIssuer;
    }

    /** @param credentialIssuer 签发者 URI */
    public CredentialsOffer setCredentialIssuer(String credentialIssuer) {
        this.credentialIssuer = credentialIssuer;
        return this;
    }

    /**
     * 推导凭证签发者元数据 well-known URL。
     *
     * @return openid-credential-issuer 元数据地址
     */
        var metadataUrl = KeycloakUriBuilder
                .fromUri(credentialIssuer)
                .path("/.well-known/" + WELL_KNOWN_OPENID_CREDENTIAL_ISSUER);
        var idx = credentialIssuer.indexOf("/realms");
        if (idx > 0) {
            var baseUrl = credentialIssuer.substring(0, idx);
            var realmPath = credentialIssuer.substring(idx);
            metadataUrl = KeycloakUriBuilder
                    .fromUri(baseUrl)
                    .path("/.well-known/" + WELL_KNOWN_OPENID_CREDENTIAL_ISSUER)
                    .path(realmPath);
        }
        return metadataUrl.buildAsString();
    }

    /** @return 凭证配置 ID 列表 */
    public List<String> getCredentialConfigurationIds() {
        return credentialConfigurationIds;
    }

    /** @param credentialConfigurationIds 配置 ID 列表 */
    public CredentialsOffer setCredentialConfigurationIds(List<String> credentialConfigurationIds) {
        this.credentialConfigurationIds = Collections.unmodifiableList(credentialConfigurationIds);
        return this;
    }

    /**
     * 添加 grant 条目。
     *
     * @param grant grant 对象
     * @return 当前实例
     */
        grants.put(grant.getGrantType(), grant);
        return this;
    }

    /** @return 授权码 grant，无则 null */
    @JsonIgnore
    public AuthorizationCodeGrant getAuthorizationCodeGrant() {
        return (AuthorizationCodeGrant) grants.get(AUTH_CODE_GRANT_TYPE);
    }

    /** @return 授权码 grant 中的 issuer_state */
    @JsonIgnore
    public String getIssuerState() {
        return Optional.ofNullable(getAuthorizationCodeGrant())
                .map(AuthorizationCodeGrant::getIssuerState)
                .orElse(null);
    }

    /** @return 预授权码 grant */
    @JsonIgnore
    public PreAuthorizedCodeGrant getPreAuthorizedGrant() {
        return (PreAuthorizedCodeGrant) grants.get(PRE_AUTH_GRANT_TYPE);
    }

    /** @return 预授权码字符串 */
    @JsonIgnore
    public String getPreAuthorizedCode() {
        return Optional.ofNullable(getPreAuthorizedGrant())
                .map(PreAuthorizedCodeGrant::getPreAuthorizedCode)
                .orElse(null);
    }

    /** @return 是否包含预授权 grant */
    @JsonIgnore
    public boolean hasPreAuthorizedGrant() {
        return grants.get(PRE_AUTH_GRANT_TYPE) != null;
    }

    /** 按签发者、配置 ID 与 grants 比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CredentialsOffer that)) return false;
        boolean match = Objects.equals(credentialIssuer, that.credentialIssuer);
        match &= Objects.equals(credentialConfigurationIds, that.credentialConfigurationIds);
        match &= Objects.equals(grants, that.grants);
        return match;
    }

    /** @return 哈希码 */
    @Override
    public int hashCode() {
        return Objects.hash(credentialIssuer, credentialConfigurationIds, grants);
    }

    /** @return JSON 字符串 */
    public String toString() {
        return JsonSerialization.valueAsString(this);
    }
}
