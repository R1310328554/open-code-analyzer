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

import org.keycloak.jose.jwk.JWK;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 密钥证明（key attestation）JWT 载荷。
 * <p>描述被证明密钥、存储与用户认证要求、nonce 及状态等信息。</p>
 *
 * @see <a href="https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-15.html#name-key-attestations">OID4VCI Specification</a>
 *
 * @author Bertrand Ogen
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyAttestationJwtBody {

    /** 签发时间（Unix 秒）。 */
    @JsonProperty("iat")
    private Long iat;

    /** 过期时间（Unix 秒）。 */
    @JsonProperty("exp")
    private Long exp;

    /** 被证明的公钥列表（JWK 格式）。 */
    @JsonProperty("attested_keys")
    private List<JWK> attestedKeys;

    /** 密钥存储安全级别声明。 */
    @JsonProperty("key_storage")
    private List<String> keyStorage;

    /** 用户认证抗攻击能力声明。 */
    @JsonProperty("user_authentication")
    private List<String> userAuthentication;

    /** 认证/合规标识（如 FIDO 认证级别）。 */
    @JsonProperty("certification")
    private String certification;

    /** 与凭证请求绑定的 nonce。 */
    @JsonProperty("nonce")
    private String nonce;

    /** 密钥或证明的扩展状态信息。 */
    @JsonProperty("status")
    private Map<String, Object> status;

    /** @return 签发时间 */
    public Long getIat() {
        return iat;
    }

    /** @param iat 签发时间 */
    public void setIat(Long iat) {
        this.iat = iat;
    }

    /** @return 过期时间 */
    public Long getExp() {
        return exp;
    }

    /** @param exp 过期时间 */
    public void setExp(Long exp) {
        this.exp = exp;
    }

    /** @return 被证明公钥列表 */
    public List<JWK> getAttestedKeys() {
        return attestedKeys;
    }

    /** @param attestedKeys 被证明公钥 @return 当前实例 */
    public KeyAttestationJwtBody setAttestedKeys(List<JWK> attestedKeys) {
        this.attestedKeys = attestedKeys;
        return this;
    }

    /** @return 密钥存储声明 */
    public List<String> getKeyStorage() {
        return keyStorage;
    }

    /** @param keyStorage 密钥存储声明 @return 当前实例 */
    public KeyAttestationJwtBody setKeyStorage(List<String> keyStorage) {
        this.keyStorage = keyStorage;
        return this;
    }

    /** @return 用户认证声明 */
    public List<String> getUserAuthentication() {
        return userAuthentication;
    }

    /** @param userAuthentication 用户认证声明 @return 当前实例 */
    public KeyAttestationJwtBody setUserAuthentication(List<String> userAuthentication) {
        this.userAuthentication = userAuthentication;
        return this;
    }

    /** @return 认证标识 */
    public String getCertification() {
        return certification;
    }

    /** @param certification 认证标识 @return 当前实例 */
    public KeyAttestationJwtBody setCertification(String certification) {
        this.certification = certification;
        return this;
    }

    /** @return 绑定 nonce */
    public String getNonce() {
        return nonce;
    }

    /** @param nonce 绑定 nonce @return 当前实例 */
    public KeyAttestationJwtBody setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    /** @return 扩展状态映射 */
    public Map<String, Object> getStatus() {
        return status;
    }

    /** @param status 扩展状态 */
    public void setStatus(Map<String, Object> status) {
        this.status = status;
    }
}
