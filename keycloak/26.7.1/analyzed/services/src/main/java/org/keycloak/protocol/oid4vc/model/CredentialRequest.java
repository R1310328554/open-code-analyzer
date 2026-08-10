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


import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * OID4VCI 规范中的凭证请求（Credential Request）模型。
 * <p>钱包向签发者提交凭证配置 ID、proof 及可选的响应加密参数。</p>
 * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-request}
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CredentialRequest {

    /** 凭证配置 ID，对应签发者元数据中的 supported_credentials 条目。 */
    @JsonProperty("credential_configuration_id")
    private String credentialConfigurationId;

    /** 凭证标识符，用于延迟签发或多步交易场景。 */
    @JsonProperty("credential_identifier")
    private String credentialIdentifier;

    /** 持有者绑定 proof 集合（JWT 或 attestation 等）。 */
    @JsonProperty("proofs")
    private Proofs proofs;

    /**
     * 已弃用：请使用 {@link #proofs}。
     * <p>为兼容仅发送单个 proof 的旧客户端而保留；类型可为 {@link JwtProof} 或 {@link AttestationProof}。</p>
     */
    @Deprecated
    @JsonProperty("proof")
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "proof_type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = JwtProof.class, name = "jwt"),
            @JsonSubTypes.Type(value = AttestationProof.class, name = "attestation")
    })
    private Object proof;

    // 格式标识符，参见 OID4VCI 规范 format identifier 章节
    /** 凭证定义，含格式标识符与类型/context 等。 */
    @JsonProperty("credential_definition")
    private CredentialDefinition credentialDefinition;

    /** 凭证响应加密参数（算法、压缩、接收方公钥 JWK）。 */
    @JsonProperty("credential_response_encryption")
    private CredentialResponseEncryption credentialResponseEncryption;

    /** @return 凭证标识符 */
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }

    /** @param credentialIdentifier 凭证标识符 */
    public CredentialRequest setCredentialIdentifier(String credentialIdentifier) {
        this.credentialIdentifier = credentialIdentifier;
        return this;
    }

    /** @return 凭证配置 ID */
    public String getCredentialConfigurationId() {
        return credentialConfigurationId;
    }

    /** @param credentialConfigurationId 凭证配置 ID */
    public CredentialRequest setCredentialConfigurationId(String credentialConfigurationId) {
        this.credentialConfigurationId = credentialConfigurationId;
        return this;
    }

    /** @return proof 集合 */
    public Proofs getProofs() {
        return proofs;
    }

    /** @param proofs proof 集合 */
    public CredentialRequest setProofs(Proofs proofs) {
        this.proofs = proofs;
        return this;
    }

    /** @return 单个 proof（已弃用字段） */
    public Object getProof() {
        return proof;
    }

    /** @param proof 单个 proof 对象 */
    public CredentialRequest setProof(Object proof) {
        this.proof = proof;
        return this;
    }

    /** @return 凭证定义 */
    public CredentialDefinition getCredentialDefinition() {
        return credentialDefinition;
    }

    /** @param credentialDefinition 凭证定义 */
    public CredentialRequest setCredentialDefinition(CredentialDefinition credentialDefinition) {
        this.credentialDefinition = credentialDefinition;
        return this;
    }

    /** @return 凭证响应加密参数 */
    public CredentialResponseEncryption getCredentialResponseEncryption() {
        return credentialResponseEncryption;
    }

    /** @param credentialResponseEncryption 响应加密参数 */
    public CredentialRequest setCredentialResponseEncryption(CredentialResponseEncryption credentialResponseEncryption) {
        this.credentialResponseEncryption = credentialResponseEncryption;
        return this;
    }

    /** @return JSON 序列化字符串 */
    @Override
    public String toString() {
        try {
            return JsonSerialization.mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
