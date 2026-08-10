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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 签发者元数据中某一 proof 类型的支持数据。
 * <p>包含支持的签名算法及密钥 attestation 要求。</p>
 *
 * @author <a href="mailto:Bertrand.Ogena@adorsys.com">Bertrand Ogen</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupportedProofTypeData {

    /** 该 proof 类型支持的签名算法。 */
    @JsonProperty("proof_signing_alg_values_supported")
    private List<String> signingAlgorithmsSupported;

    /** 密钥 attestation 要求；不需要 attestation 时不应出现。 */
    @JsonProperty("key_attestations_required")
    private KeyAttestationsRequired keyAttestationsRequired;
    
    /** Jackson 反序列化默认构造器。 */
    public SupportedProofTypeData() {
        // Default constructor for Jackson deserialization
    }

    /**
     * @param signingAlgorithmsSupported 支持的签名算法
     * @param keyAttestationsRequired 密钥 attestation 要求
     */
        this.signingAlgorithmsSupported = signingAlgorithmsSupported;
        this.keyAttestationsRequired = keyAttestationsRequired;
    }

    /** @return 支持的签名算法列表 */
    public List<String> getSigningAlgorithmsSupported() {
        return signingAlgorithmsSupported;
    }

    /** @param signingAlgorithmsSupported 签名算法列表
     * @return 当前实例 */
    public SupportedProofTypeData setSigningAlgorithmsSupported(List<String> signingAlgorithmsSupported) {
        this.signingAlgorithmsSupported = signingAlgorithmsSupported;
        return this;
    }

    /**
     * 返回密钥 attestation 要求。
     * <p>签发者不要求 attestation 时该字段必须为 null；两子字段均为空表示需要 attestation 但无额外约束。</p>
     *
     * @return attestation 要求对象，不需要时为 null
     */
    public KeyAttestationsRequired getKeyAttestationsRequired() {
        return keyAttestationsRequired;
    }

    /** @param keyAttestationsRequired attestation 要求
     * @return 当前实例 */
    public SupportedProofTypeData setKeyAttestationsRequired(KeyAttestationsRequired keyAttestationsRequired) {
        this.keyAttestationsRequired = keyAttestationsRequired;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof SupportedProofTypeData that)) {
            return false;
        }

        return Objects.equals(signingAlgorithmsSupported,
                that.signingAlgorithmsSupported) && Objects.equals(keyAttestationsRequired,
                that.keyAttestationsRequired);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(signingAlgorithmsSupported);
        result = 31 * result + Objects.hashCode(keyAttestationsRequired);
        return result;
    }
}
