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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 凭证签发者元数据中的 credential_response_encryption 段。
 * <p>声明签发者支持的密钥管理算法、内容加密算法及是否强制加密响应。</p>
 * @see <a href="https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-issuer-metadata-p"></a>
 *
 * @author <a href="mailto:Bertrand.Ogen@adorsys.com">Bertrand Ogen</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialResponseEncryptionMetadata {

    /** 支持的密钥管理算法（alg）列表。 */
    @JsonProperty("alg_values_supported")
    private List<String> algValuesSupported;

    /** 支持的内容加密算法（enc）列表。 */
    @JsonProperty("enc_values_supported")
    private List<String> encValuesSupported;

    /** 支持的压缩算法（zip）列表。 */
    @JsonProperty("zip_values_supported")
    private List<String> zipValuesSupported;

    /** 是否强制加密凭证响应。 */
    @JsonProperty("encryption_required")
    private Boolean encryptionRequired;

    /** @return 密钥管理算法列表 */
    public List<String> getAlgValuesSupported() {
        return algValuesSupported;
    }

    /** @param algValuesSupported 密钥管理算法列表 */
    public CredentialResponseEncryptionMetadata setAlgValuesSupported(List<String> algValuesSupported) {
        this.algValuesSupported = algValuesSupported;
        return this;
    }

    /** @return 内容加密算法列表 */
    public List<String> getEncValuesSupported() {
        return encValuesSupported;
    }

    /** @param encValuesSupported 内容加密算法列表 */
    public CredentialResponseEncryptionMetadata setEncValuesSupported(List<String> encValuesSupported) {
        this.encValuesSupported = encValuesSupported;
        return this;
    }

    /** @return 压缩算法列表 */
    public List<String> getZipValuesSupported() {
        return zipValuesSupported;
    }

    /** @param zipValuesSupported 压缩算法列表 */
    public CredentialResponseEncryptionMetadata setZipValuesSupported(List<String> zipValuesSupported) {
        this.zipValuesSupported = zipValuesSupported;
        return this;
    }

    /** @return 是否强制响应加密 */
    public Boolean getEncryptionRequired() {
        return encryptionRequired;
    }

    /** @param encryptionRequired 是否强制加密 */
    public CredentialResponseEncryptionMetadata setEncryptionRequired(Boolean encryptionRequired) {
        this.encryptionRequired = encryptionRequired;
        return this;
    }
}
