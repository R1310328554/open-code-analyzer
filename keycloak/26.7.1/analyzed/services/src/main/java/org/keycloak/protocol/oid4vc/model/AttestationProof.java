package org.keycloak.protocol.oid4vc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 已弃用：表示单个基于 attestation 的证明（历史 {@code proof} 结构）。
 * <p>请优先使用 {@link Proofs} 及对应数组字段（如 {@code attestation}）。本类仅保留向后兼容，支持 OID4VCI Draft 15 的 {@code attestation} proof 类型。</p>
 *
 * @see <a href="https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-ID2.html#name-credential-request">OID4VCI Credential Request</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated
public class AttestationProof {

    /** attestation 证明载荷（JSON 字段 {@code attestation}）。 */
    @JsonProperty("attestation")
    private String attestation;

    /** 证明类型标识（JSON 字段 {@code proof_type}）。 */
    @JsonProperty("proof_type")
    private String proofType;

    /** 无参构造，供 Jackson 反序列化使用。 */
    public AttestationProof() {
    }

    /**
     * @param attestation attestation 证明载荷
     * @param proofType 证明类型
     */
    public AttestationProof(String attestation, String proofType) {
        this.attestation = attestation;
        this.proofType = proofType;
    }

    /** @return attestation 证明载荷 */
    public String getAttestation() {
        return attestation;
    }

    /** @param attestation attestation 证明载荷 */
    public AttestationProof setAttestation(String attestation) {
        this.attestation = attestation;
        return this;
    }

    /** @return 证明类型标识 */
    public String getProofType() {
        return proofType;
    }

    /** @param proofType 证明类型标识 */
    public AttestationProof setProofType(String proofType) {
        this.proofType = proofType;
        return this;
    }
}
