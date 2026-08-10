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
 * 使用数据完整性（Data Integrity）保护的 W3C 可验证展示（VP）。
 * <p>作为 OID4VCI di_vp proof 类型（规范附录 F.2），用于持有者绑定证明。</p>
 *
 * @see <a href="https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-di_vp-proof-type">OID4VCI di_vp Proof Type</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiVpProof {

    /** JSON-LD @context 列表。 */
    @JsonProperty("@context")
    private List<String> context;

    /** VP 类型 URI 列表。 */
    @JsonProperty("type")
    private List<String> type;

    /** 持有者标识（通常为 DID）。 */
    @JsonProperty("holder")
    private String holder;

    /** 数据完整性 proof 条目列表。 */
    @JsonProperty("proof")
    private List<DataIntegrityProof> proof;

    /** 无参构造。 */
    public DiVpProof() {
    }

    /**
     * 全字段构造。
     *
     * @param context @context 列表
     * @param type    VP 类型
     * @param holder  持有者标识
     * @param proof   数据完整性 proof 列表
     */
        this.context = context;
        this.type = type;
        this.holder = holder;
        this.proof = proof;
    }

    /** @return @context 列表 */
    public List<String> getContext() {
        return context;
    }

    /** @param context @context 列表 */
    public DiVpProof setContext(List<String> context) {
        this.context = context;
        return this;
    }

    /** @return VP 类型列表 */
    public List<String> getType() {
        return type;
    }

    /** @param type VP 类型列表 */
    public DiVpProof setType(List<String> type) {
        this.type = type;
        return this;
    }

    /** @return 持有者标识 */
    public String getHolder() {
        return holder;
    }

    /** @param holder 持有者标识 */
    public DiVpProof setHolder(String holder) {
        this.holder = holder;
        return this;
    }

    /** @return 数据完整性 proof 列表 */
    public List<DataIntegrityProof> getProof() {
        return proof;
    }

    /** @param proof proof 列表 */
    public DiVpProof setProof(List<DataIntegrityProof> proof) {
        this.proof = proof;
        return this;
    }

    /**
     * [VC_Data_Integrity] 定义的数据完整性 proof 结构。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DataIntegrityProof {

        /** proof 类型（如 DataIntegrityProof）。 */
        @JsonProperty("type")
        private String type;

        /** 密码套件标识。 */
        @JsonProperty("cryptosuite")
        private String cryptosuite;

        /** proof 用途（如 authentication）。 */
        @JsonProperty("proofPurpose")
        private String proofPurpose;

        /** 验证方法 URI。 */
        @JsonProperty("verificationMethod")
        private String verificationMethod;

        /** proof 创建时间（ISO 8601）。 */
        @JsonProperty("created")
        private String created;

        /** 签发者下发的 nonce/challenge。 */
        @JsonProperty("challenge")
        private String challenge;

        /** 绑定域（通常为签发者 URI）。 */
        @JsonProperty("domain")
        private String domain;

        /** 数据完整性签名值。 */
        @JsonProperty("proofValue")
        private String proofValue;

        /** 无参构造。 */
        public DataIntegrityProof() {
        }

        /** 全字段构造数据完整性 proof。 */
            this.type = type;
            this.cryptosuite = cryptosuite;
            this.proofPurpose = proofPurpose;
            this.verificationMethod = verificationMethod;
            this.created = created;
            this.challenge = challenge;
            this.domain = domain;
            this.proofValue = proofValue;
        }

        /** @return proof 类型 */
        public String getType() {
            return type;
        }

        /** @param type proof 类型 */
        public DataIntegrityProof setType(String type) {
            this.type = type;
            return this;
        }

        /** @return 密码套件 */
        public String getCryptosuite() {
            return cryptosuite;
        }

        /** @param cryptosuite 密码套件 */
        public DataIntegrityProof setCryptosuite(String cryptosuite) {
            this.cryptosuite = cryptosuite;
            return this;
        }

        /** @return proof 用途 */
        public String getProofPurpose() {
            return proofPurpose;
        }

        /** @param proofPurpose proof 用途 */
        public DataIntegrityProof setProofPurpose(String proofPurpose) {
            this.proofPurpose = proofPurpose;
            return this;
        }

        /** @return 验证方法 URI */
        public String getVerificationMethod() {
            return verificationMethod;
        }

        /** @param verificationMethod 验证方法 URI */
        public DataIntegrityProof setVerificationMethod(String verificationMethod) {
            this.verificationMethod = verificationMethod;
            return this;
        }

        /** @return 创建时间 */
        public String getCreated() {
            return created;
        }

        /** @param created 创建时间 */
        public DataIntegrityProof setCreated(String created) {
            this.created = created;
            return this;
        }

        /** @return challenge/nonce */
        public String getChallenge() {
            return challenge;
        }

        /** @param challenge challenge 值 */
        public DataIntegrityProof setChallenge(String challenge) {
            this.challenge = challenge;
            return this;
        }

        /** @return 绑定域 */
        public String getDomain() {
            return domain;
        }

        /** @param domain 绑定域 */
        public DataIntegrityProof setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        /** @return 签名值 */
        public String getProofValue() {
            return proofValue;
        }

        /** @param proofValue 签名值 */
        public DataIntegrityProof setProofValue(String proofValue) {
            this.proofValue = proofValue;
            return this;
        }
    }
} 
