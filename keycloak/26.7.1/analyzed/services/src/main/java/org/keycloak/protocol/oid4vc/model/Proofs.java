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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 凭证请求（第 8.2 节）中的 proofs 对象。
 * <p>按 proof 类型分组存放 jwt、di_vp、attestation 等证明数组。</p>
 *
 * @see <a href="https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-16.html#name-credential-request">OID4VCI Credential Request</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Proofs {

    /** JWT 类型 proof 值列表。 */
    @JsonProperty("jwt")
    private List<String> jwt;

    /** 可验证展示（di_vp）类型 proof 列表。 */
    @JsonProperty("di_vp")
    private List<DiVpProof> diVp;

    /** 密钥 attestation 类型 proof 值列表。 */
    @JsonProperty("attestation")
    private List<String> attestation;

    /** @return JWT proof 列表 */
    public List<String> getJwt() {
        return jwt;
    }

    /** @param jwt JWT proof 列表
     * @return 当前实例 */
    public Proofs setJwt(List<String> jwt) {
        this.jwt = jwt;
        return this;
    }

    /** @return di_vp proof 列表 */
    public List<DiVpProof> getDiVp() {
        return diVp;
    }

    /** @param diVp di_vp proof 列表
     * @return 当前实例 */
    public Proofs setDiVp(List<DiVpProof> diVp) {
        this.diVp = diVp;
        return this;
    }

    /** @return attestation proof 列表 */
    public List<String> getAttestation() {
        return attestation;
    }

    /** @param attestation attestation proof 列表
     * @return 当前实例 */
    public Proofs setAttestation(List<String> attestation) {
        this.attestation = attestation;
        return this;
    }

    /**
     * 按 proof 类型创建 {@link Proofs} 实例。
     * <p>根据类型写入 jwt 或 attestation 字段。</p>
     *
     * @param proofType   proof 类型（{@link ProofType#JWT} 或 {@link ProofType#ATTESTATION}）
     * @param proofValues 要写入的 proof 值
     * @return 填充后的 Proofs
     */
    public static Proofs create(String proofType, String... proofValues) {
        if (proofType == null) {
            throw new IllegalArgumentException("proofType cannot be null");
        }
        if (proofValues == null || proofValues.length == 0) {
            throw new IllegalArgumentException("proofValues cannot be null or empty");
        }
        for (String proof : proofValues) {
            if (proof == null || proof.isBlank()) {
                throw new IllegalArgumentException("Null or blank proof value");
            }
        }
        Proofs proofs = new Proofs();
        switch (proofType) {
            case ProofType.JWT ->
                    proofs.setJwt(List.of(proofValues));
            case ProofType.ATTESTATION ->
                    proofs.setAttestation(List.of(proofValues));
            default -> throw new IllegalArgumentException("Unknown proof type: " + proofType);
        }
        return proofs;
    }

    /**
     * 根据已填充字段推断 proof 类型（先查 JWT，再查 attestation）。
     *
     * @return proof 类型字符串，未找到时返回 null
     */
    @JsonIgnore
    public String getProofType() {
        if (jwt != null && !jwt.isEmpty()) {
            return ProofType.JWT;
        } else if (attestation != null && !attestation.isEmpty()) {
            return ProofType.ATTESTATION;
        }
        return null;
    }

    /**
     * 返回所有 proof 值的扁平列表（优先 JWT，其次 attestation）。
     *
     * @return proof 值列表，无 proof 时为空列表
     */
    @JsonIgnore
    public List<String> getAllProofs() {
        List<String> allProofs = new ArrayList<>();
        if (jwt != null && !jwt.isEmpty()) {
            allProofs.addAll(jwt);
        } else if (attestation != null && !attestation.isEmpty()) {
            allProofs.addAll(attestation);
        }
        return allProofs;
    }

    /**
     * 返回当前存在的 proof 类型列表（非空字段）。
     * <p>可用于遍历需要校验的 proof 类型。</p>
     *
     * @return 已填充的 proof 类型字符串列表
     */
    @JsonIgnore
    public List<String> getPresentProofTypes() {
        List<String> presentTypes = new ArrayList<>();
        if (jwt != null && !jwt.isEmpty()) {
            presentTypes.add(ProofType.JWT);
        }
        if (attestation != null && !attestation.isEmpty()) {
            presentTypes.add(ProofType.ATTESTATION);
        }
        return presentTypes;
    }
} 
