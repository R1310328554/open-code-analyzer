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
import java.util.Objects;
import java.util.Optional;

import org.keycloak.crypto.KeyWrapper;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.issuance.OID4VCIssuerWellKnownProvider;
import org.keycloak.utils.StringUtil;

/**
 * 凭证构建器专用的配置对象。
 * <p>聚合签发方、格式类型、SD-JWT 可见声明、诱饵数量及签名密钥/算法等参数，由 {@link CredentialScopeModel} 与 {@link SupportedCredentialConfiguration} 解析生成。</p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class CredentialBuildConfig {

    /** 多值字符串配置项的分隔符。 */
    public static final String MULTIVALUED_STRING_SEPARATOR = ",";

    private static final String TOKEN_JWS_TYPE_KEY = "token_jws_type";
    private static final String HASH_ALGORITHM_KEY = "hash_algorithm";
    private static final String VISIBLE_CLAIMS_KEY = "visible_claims";
    private static final String NUMBER_OF_DECOYS_KEY = "decoys";

    private static final String SIGNING_KEY_ID_KEY = "signing_key_id";
    private static final String OVERRIDE_KEY_ID_KEY = "override_key_id";
    private static final String SIGNING_ALGORITHM_KEY = "signing_algorithm";
    private static final String LDP_PROOF_TYPE_KEY = "ldp_proof_type";

    /** 凭证签发方标识（issuer）。 */
    private String credentialIssuer;

    /** 支持的凭证配置 ID。 */
    private String credentialConfigId;

    //-- 凭证构建相关配置字段 --//

    /** SD-JWT 使用的凭证类型（vct）字段值。 */
    private String credentialType;

    /** 待创建 JWT 的 typ 声明值（写入 JWT Header）。 */
    private String tokenJwsType;

    /** SD-JWT 选择性披露使用的哈希算法。 */
    private String hashAlgorithm;

    /** SD-JWT 中保持明文披露的声明路径列表。 */
    private List<String> sdJwtVisibleClaims;

    /** 加入 SD-JWT 的诱饵（decoy）数量。 */
    private Integer numberOfDecoys;

    //-- 签名相关配置字段 --//

    /** 凭证签名使用的领域密钥 kid（须为 realm 密钥）。 */
    private String signingKeyId;

    /** 优先使用的替代 kid（如 DID 方案需覆盖默认 signingKeyId）。 */
    private String overrideKeyId;

    /** 签名算法标识，须与所选签名密钥匹配。 */
    private String signingAlgorithm;

    /** 待创建的 Linked Data Proof 类型，须与签名密钥匹配。 */
    private String ldpProofType;

    /**
     * 从凭证配置与范围模型解析构建配置。
     * @param keycloakSession Keycloak 会话
     * @param credentialConfiguration 支持的凭证配置
     * @param credentialModel 凭证范围模型
     * @return 填充完毕的构建配置
     */
    public static CredentialBuildConfig parse(KeycloakSession keycloakSession,
                                              SupportedCredentialConfiguration credentialConfiguration,
                                              CredentialScopeModel credentialModel) {
        final String credentialIssuer = Optional.ofNullable(credentialModel.getIssuerDid()).orElse(
                OID4VCIssuerWellKnownProvider.getIssuer(keycloakSession.getContext()));


        String signingAlg = Constants.DEFAULT_SIGNATURE_ALGORITHM;
        if (StringUtil.isNotBlank(credentialModel.getSigningKeyId())) {
            signingAlg = keycloakSession.keys()
                    .getKeysStream(keycloakSession.getContext().getRealm())
                    .filter(key-> key.getKid().equals(credentialModel.getSigningKeyId()))
                    .findAny()
                    .map(KeyWrapper::getAlgorithm)
                    .orElse(Constants.DEFAULT_SIGNATURE_ALGORITHM);
        }
        else if (StringUtil.isNotBlank(credentialModel.getSigningAlg())) {
            signingAlg = credentialModel.getSigningAlg();
        }

        return new CredentialBuildConfig().setCredentialIssuer(credentialIssuer)
                                          .setCredentialConfigId(credentialConfiguration.getId())
                                          .setCredentialType(credentialModel.getVct())
                                          .setTokenJwsType(credentialModel.getBuildConfigTokenJwsType())
                                          .setNumberOfDecoys(credentialModel.getSdJwtNumberOfDecoys())
                                          .setSigningKeyId(credentialModel.getSigningKeyId())
                                          .setSigningAlgorithm(signingAlg)
                                          .setHashAlgorithm(credentialModel.getBuildConfigHashAlgorithm())
                                          .setSdJwtVisibleClaims(credentialModel.getBuildConfigSdJwtVisibleClaims());
    }

    public String getCredentialIssuer() {
        return credentialIssuer;
    }

    public CredentialBuildConfig setCredentialIssuer(String credentialIssuer) {
        this.credentialIssuer = credentialIssuer;
        return this;
    }

    public String getCredentialConfigId() {
        return credentialConfigId;
    }

    public CredentialBuildConfig setCredentialConfigId(String credentialConfigId) {
        this.credentialConfigId = credentialConfigId;
        return this;
    }

    public String getTokenJwsType() {
        return tokenJwsType;
    }

    public CredentialBuildConfig setTokenJwsType(String tokenJwsType) {
        this.tokenJwsType = tokenJwsType;
        return this;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public CredentialBuildConfig setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
        return this;
    }

    public List<String> getSdJwtVisibleClaims() {
        return sdJwtVisibleClaims;
    }

    public CredentialBuildConfig setSdJwtVisibleClaims(List<String> sdJwtVisibleClaims) {
        this.sdJwtVisibleClaims = sdJwtVisibleClaims;
        return this;
    }

    public int getNumberOfDecoys() {
        return numberOfDecoys;
    }

    public CredentialBuildConfig setNumberOfDecoys(int numberOfDecoys) {
        this.numberOfDecoys = numberOfDecoys;
        return this;
    }

    public String getCredentialType() {
        return credentialType;
    }

    public CredentialBuildConfig setCredentialType(String credentialType) {
        this.credentialType = credentialType;
        return this;
    }

    public String getSigningKeyId() {
        return signingKeyId;
    }

    public CredentialBuildConfig setSigningKeyId(String signingKeyId) {
        this.signingKeyId = signingKeyId;
        return this;
    }

    public String getOverrideKeyId() {
        return overrideKeyId;
    }

    public CredentialBuildConfig setOverrideKeyId(String overrideKeyId) {
        this.overrideKeyId = overrideKeyId;
        return this;
    }

    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public CredentialBuildConfig setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
        return this;
    }

    public String getLdpProofType() {
        return ldpProofType;
    }

    public CredentialBuildConfig setLdpProofType(String ldpProofType) {
        this.ldpProofType = ldpProofType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CredentialBuildConfig that = (CredentialBuildConfig) o;
        return Objects.equals(credentialConfigId, that.credentialConfigId) && Objects.equals(credentialType,
                                                                                 that.credentialType) && Objects.equals(
                tokenJwsType,
                that.tokenJwsType) && Objects.equals(hashAlgorithm, that.hashAlgorithm) && Objects.equals(
                sdJwtVisibleClaims,
                that.sdJwtVisibleClaims) && Objects.equals(
                numberOfDecoys,
                that.numberOfDecoys) && Objects.equals(signingKeyId, that.signingKeyId) && Objects.equals(overrideKeyId,
                                                                                                          that.overrideKeyId) && Objects.equals(
                signingAlgorithm,
                that.signingAlgorithm) && Objects.equals(ldpProofType, that.ldpProofType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentialConfigId,
                            credentialType,
                            tokenJwsType,
                            hashAlgorithm,
                            sdJwtVisibleClaims,
                            numberOfDecoys,
                            signingKeyId,
                            overrideKeyId,
                            signingAlgorithm,
                            ldpProofType);
    }
}
