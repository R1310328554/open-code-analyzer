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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 凭证签发方元数据（Credential Issuer Metadata）模型。
 * <p>描述签发端点、nonce、延迟签发、授权服务器及支持的凭证配置等，规范见 {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-15.html#name-credential-issuer-metadata}。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CredentialIssuer {

    /** 凭证签发方标识 URI（JSON 字段 {@code credential_issuer}）。 */
    @JsonProperty("credential_issuer")
    private String credentialIssuer;

    /** 凭证签发 HTTP 端点 URL。 */
    @JsonProperty("credential_endpoint")
    private String credentialEndpoint;

    /** nonce 获取端点 URL（密钥绑定 proof 用）。 */
    @JsonProperty("nonce_endpoint")
    private String nonceEndpoint;

    /** 延迟凭证签发端点 URL。 */
    @JsonProperty("deferred_credential_endpoint")
    private String deferredCredentialEndpoint;

    /** 关联的 OAuth 2.0 授权服务器标识列表。 */
    @JsonProperty("authorization_servers")
    private List<String> authorizationServers;

    /** 批量凭证签发能力元数据。 */
    @JsonProperty("batch_credential_issuance")
    private BatchCredentialIssuance batchCredentialIssuance;

    /** 支持的凭证配置映射（键为配置 ID）。 */
    @JsonProperty("credential_configurations_supported")
    private Map<String, SupportedCredentialConfiguration> credentialsSupported;

    /** 签发方多语言展示信息。 */
    @JsonProperty("display")
    private List<DisplayObject> display;

    /** 凭证响应加密元数据。 */
    @JsonProperty("credential_response_encryption")
    private CredentialResponseEncryptionMetadata credentialResponseEncryption;

    /** 凭证请求加密元数据。 */
    @JsonProperty("credential_request_encryption")
    private CredentialRequestEncryptionMetadata credentialRequestEncryption;

    public String getCredentialIssuer() {
        return credentialIssuer;
    }

    public CredentialIssuer setCredentialIssuer(String credentialIssuer) {
        this.credentialIssuer = credentialIssuer;
        return this;
    }

    public String getCredentialEndpoint() {
        return credentialEndpoint;
    }

    public CredentialIssuer setCredentialEndpoint(String credentialEndpoint) {
        this.credentialEndpoint = credentialEndpoint;
        return this;
    }

    public String getNonceEndpoint() {
        return nonceEndpoint;
    }

    public CredentialIssuer setNonceEndpoint(String nonceEndpoint) {
        this.nonceEndpoint = nonceEndpoint;
        return this;
    }

    public String getDeferredCredentialEndpoint() {
        return deferredCredentialEndpoint;
    }

    public CredentialIssuer setDeferredCredentialEndpoint(String deferredCredentialEndpoint) {
        this.deferredCredentialEndpoint = deferredCredentialEndpoint;
        return this;
    }

    public List<String> getAuthorizationServers() {
        return authorizationServers;
    }

    public CredentialIssuer setAuthorizationServers(List<String> authorizationServers) {
        this.authorizationServers = authorizationServers;
        return this;
    }

    public BatchCredentialIssuance getBatchCredentialIssuance() {
        return batchCredentialIssuance;
    }

    public CredentialIssuer setBatchCredentialIssuance(BatchCredentialIssuance batchCredentialIssuance) {
        this.batchCredentialIssuance = batchCredentialIssuance;
        return this;
    }

    public Map<String, SupportedCredentialConfiguration> getCredentialsSupported() {
        return credentialsSupported;
    }

    public CredentialIssuer setCredentialsSupported(Map<String, SupportedCredentialConfiguration> credentialsSupported) {
        if (credentialsSupported == null) {
            throw new IllegalArgumentException("credentialsSupported cannot be null");
        }
        credentialsSupported.forEach((k, v) -> v.setId(k));
        this.credentialsSupported = Map.copyOf(credentialsSupported);
        return this;
    }

    public List<DisplayObject> getDisplay() {
        return display;
    }

    public CredentialIssuer setDisplay(List<DisplayObject> display) {
        this.display = display;
        return this;
    }

    public CredentialResponseEncryptionMetadata getCredentialResponseEncryption() {
        return credentialResponseEncryption;
    }

    public CredentialIssuer setCredentialResponseEncryption(CredentialResponseEncryptionMetadata credentialResponseEncryption) {
        this.credentialResponseEncryption = credentialResponseEncryption;
        return this;
    }

    public CredentialRequestEncryptionMetadata getCredentialRequestEncryption() {
        return credentialRequestEncryption;
    }

    public CredentialIssuer setCredentialRequestEncryption(CredentialRequestEncryptionMetadata credentialRequestEncryption) {
        this.credentialRequestEncryption = credentialRequestEncryption;
        return this;
    }

    /** 批量凭证签发（{@code batch_credential_issuance}）元数据参数。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BatchCredentialIssuance {
        /** 单次批量请求允许的最大凭证数量。 */
        @JsonProperty("batch_size")
        private Integer batchSize;

        /** @return 批量大小上限 */
        public Integer getBatchSize() {
            return batchSize;
        }

        /** @param batchSize 批量大小上限 */
        public BatchCredentialIssuance setBatchSize(Integer batchSize) {
            this.batchSize = batchSize;
            return this;
        }
    }
}
