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

import org.keycloak.jose.jwk.JSONWebKeySet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 凭证签发者元数据中的 credential_request_encryption 段。
 * <p>声明签发者支持的 JWE 密钥集、加密算法及是否强制加密请求。</p>
 * @see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-16.html#name-credential-issuer-metadata-p
 *
 * @author <a href="mailto:Bertrand.Ogen@adorsys.com">Bertrand Ogen</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialRequestEncryptionMetadata {

    /** 用于解密凭证请求的 JWK 集合。 */
    @JsonProperty("jwks")
    private JSONWebKeySet jwks;

    /** 支持的内容加密算法列表（enc）。 */
    @JsonProperty("enc_values_supported")
    private List<String> encValuesSupported;

    /** 支持的压缩算法列表（zip）。 */
    @JsonProperty("zip_values_supported")
    private List<String> zipValuesSupported;

    /** 是否强制客户端加密凭证请求。 */
    @JsonProperty("encryption_required")
    private Boolean encryptionRequired;

    /** @return 解密用 JWK 集合 */
    public JSONWebKeySet getJwks() {
        return jwks;
    }

    /** @param jwks 解密用 JWK 集合 */
    public CredentialRequestEncryptionMetadata setJwks(JSONWebKeySet jwks) {
        this.jwks = jwks;
        return this;
    }

    /** @return 支持的内容加密算法 */
    public List<String> getEncValuesSupported() {
        return encValuesSupported;
    }

    /** @param encValuesSupported 内容加密算法列表 */
    public CredentialRequestEncryptionMetadata setEncValuesSupported(List<String> encValuesSupported) {
        this.encValuesSupported = encValuesSupported;
        return this;
    }

    /** @return 支持的压缩算法 */
    public List<String> getZipValuesSupported() {
        return zipValuesSupported;
    }

    /** @param zipValuesSupported 压缩算法列表 */
    public CredentialRequestEncryptionMetadata setZipValuesSupported(List<String> zipValuesSupported) {
        this.zipValuesSupported = zipValuesSupported;
        return this;
    }

    /** @return 是否强制请求加密 */
    public Boolean isEncryptionRequired() {
        return encryptionRequired;
    }

    /** @param encryptionRequired 是否强制加密 */
    public CredentialRequestEncryptionMetadata setEncryptionRequired(Boolean encryptionRequired) {
        this.encryptionRequired = encryptionRequired;
        return this;
    }
}
