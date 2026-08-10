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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 已弃用：历史 {@code proof} 结构中的单条 JWT 证明。
 * <p>请改用 {@link Proofs} 及对应数组字段（如 {@code jwt}）。本类仅保留向后兼容，支持 OID4VCI Draft 15 的 {@code jwt} proof 类型。</p>
 *
 * @see <a href="https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-15.html#name-credential-request">OID4VCI Credential Request</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated
public class JwtProof {

    /** JWT 格式的密钥持有证明字符串。 */
    @JsonProperty("jwt")
    private String jwt;

    /** proof 类型标识（如 {@code jwt}）。 */
    @JsonProperty("proof_type")
    private String proofType;

    /** 无参构造，供 Jackson 反序列化使用。 */
    public JwtProof() {
    }

    /**
     * @param jwt JWT 证明字符串
     * @param proofType proof 类型
     */
    public JwtProof(String jwt, String proofType) {
        this.jwt = jwt;
        this.proofType = proofType;
    }

    /** @return JWT 证明字符串 */
    public String getJwt() {
        return jwt;
    }

    /** @param jwt JWT 证明 @return 当前实例 */
    public JwtProof setJwt(String jwt) {
        this.jwt = jwt;
        return this;
    }

    /** @return proof 类型标识 */
    public String getProofType() {
        return proofType;
    }

    /** @param proofType proof 类型 @return 当前实例 */
    public JwtProof setProofType(String proofType) {
        this.proofType = proofType;
        return this;
    }
}
