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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.keycloak.VCFormat;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.utils.StringUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;

/**
 * OID4VCI 签发者元数据中的 supported credential 配置。
 * <p>描述格式、scope、绑定方式、签名算法及 proof 类型等发行参数。</p>
 * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-issuer-metadata}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupportedCredentialConfiguration {

    /** 日志记录器。 */
    private static final Logger LOGGER = Logger.getLogger(SupportedCredentialConfiguration.class);

    @JsonIgnore
    private static final String FORMAT_KEY = "format";
    @JsonIgnore
    private static final String SCOPE_KEY = "scope";
    @JsonIgnore
    private static final String CRYPTOGRAPHIC_BINDING_METHODS_SUPPORTED_KEY = "cryptographic_binding_methods_supported";
    @JsonIgnore
    private static final String CREDENTIAL_SIGNING_ALG_VALUES_SUPPORTED_KEY = "credential_signing_alg_values_supported";
    @JsonIgnore
    private static final String PROOF_TYPES_SUPPORTED_KEY = "proof_types_supported";
    @JsonIgnore
    public static final String VERIFIABLE_CREDENTIAL_TYPE_KEY = "vct";
    @JsonIgnore
    private static final String CREDENTIAL_DEFINITION_KEY = "credential_definition";
    @JsonIgnore
    public static final String CREDENTIAL_BUILD_CONFIG_KEY = "credential_build_config";
    @JsonIgnore
    private static final String CREDENTIAL_METADATA_KEY = "credential_metadata";

    /** 凭证配置 ID（内部使用，不序列化到元数据）。 */
    @JsonIgnore
    private String id;

    /** 凭证格式（如 sd-jwt-vc、jwt_vc 等）。 */
    @JsonProperty(FORMAT_KEY)
    private String format;

    /** OAuth scope 名称。 */
    @JsonProperty(SCOPE_KEY)
    private String scope;

    /** 支持的密码学持有者绑定方式。 */
    @JsonProperty(CRYPTOGRAPHIC_BINDING_METHODS_SUPPORTED_KEY)
    private List<String> cryptographicBindingMethodsSupported;

    /** 支持的凭证签名算法列表。 */
    @JsonProperty(CREDENTIAL_SIGNING_ALG_VALUES_SUPPORTED_KEY)
    private List<String> credentialSigningAlgValuesSupported;

    /** SD-JWT VC 的可验证凭证类型（vct）。 */
    @JsonProperty(VERIFIABLE_CREDENTIAL_TYPE_KEY)
    private String vct;

    /** JWT/LDP VC 的凭证定义。 */
    @JsonProperty(CREDENTIAL_DEFINITION_KEY)
    private CredentialDefinition credentialDefinition;

    /** 支持的 proof 类型及算法。 */
    @JsonProperty(PROOF_TYPES_SUPPORTED_KEY)
    private ProofTypesSupported proofTypesSupported;

    /** 凭证展示元数据（display、claims 等）。 */
    @JsonProperty(CREDENTIAL_METADATA_KEY)
    private CredentialMetadata credentialMetadata;

    // 非规范字段：用于内部配置凭证构建方式
    @JsonIgnore
    private CredentialBuildConfig credentialBuildConfig;

    /**
     * 从 {@link CredentialScopeModel} 解析 supported credential 配置。
     * <p>规范化绑定方式与 proof 类型，并按 OID4VCI 12.2.4 决定是否输出绑定相关元数据。</p>
     *
     * @param keycloakSession                  Keycloak 会话
     * @param credentialScope                  持有凭证配置的客户端范围
     * @param globalSupportedSigningAlgorithms 全局支持的签名算法（避免重复读配置）
     * @return 解析后的 supported credential 配置
     */
    public static SupportedCredentialConfiguration parse(KeycloakSession keycloakSession,
                                                         CredentialScopeModel credentialScope,
                                                         List<String> globalSupportedSigningAlgorithms) {
        SupportedCredentialConfiguration credentialConfiguration = new SupportedCredentialConfiguration();

        String credentialConfigurationId = Optional.ofNullable(credentialScope.getCredentialConfigurationId())
                                                   .orElse(credentialScope.getName());
        credentialConfiguration.setId(credentialConfigurationId);

        credentialConfiguration.setScope(credentialScope.getName());

        String format = Optional.ofNullable(credentialScope.getFormat()).orElse(VCFormat.SD_JWT_VC);
        credentialConfiguration.setFormat(format);

        KeyAttestationsRequired keyAttestationsRequired = KeyAttestationsRequired.parse(credentialScope);
        boolean bindingRequired = credentialScope.isBindingRequired();
        List<String> requiredProofTypes = credentialScope.getRequiredProofTypes();
        List<String> configuredBindingMethods = credentialScope.getCryptographicBindingMethods();

        // 规范化并校验绑定方式与 proof 类型，防止管理端配置的未知值泄漏到签发者元数据
        List<String> allowedBindingMethods = List.of(CredentialScopeModel.CRYPTOGRAPHIC_BINDING_METHODS_DEFAULT);
        List<String> effectiveBindingMethods = Optional.ofNullable(configuredBindingMethods)
                .orElse(Collections.emptyList())
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(allowedBindingMethods::contains)
                .collect(Collectors.toList());

        if (configuredBindingMethods != null && !configuredBindingMethods.isEmpty()
                && effectiveBindingMethods.isEmpty()) {
            LOGGER.warnf("All configured cryptographic binding methods %s are unsupported. " +
                            "This credential configuration will not advertise cryptographic binding in metadata.",
                    configuredBindingMethods);
        }

        List<String> allowedProofTypes = List.of(ProofType.JWT, ProofType.ATTESTATION);
        List<String> effectiveProofTypes = Optional.ofNullable(requiredProofTypes)
                .orElse(Collections.emptyList())
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(allowedProofTypes::contains)
                .collect(Collectors.toList());

        if (requiredProofTypes != null && !requiredProofTypes.isEmpty()
                && effectiveProofTypes.isEmpty()) {
            LOGGER.warnf("All configured proof types %s are unsupported. " +
                            "This credential configuration will not advertise proof_types_supported in metadata.",
                    requiredProofTypes);
        }

        // OID4VCI 12.2.4：存在 cryptographic_binding_methods_supported 则必须要求持有者绑定，
        // 且必须同时提供 proof_types_supported；仅当显式要求绑定且配置了有效 proof 与绑定方式时才输出
        if (bindingRequired
                && !effectiveProofTypes.isEmpty()
                && !effectiveBindingMethods.isEmpty()) {

            ProofTypesSupported allProofTypes = ProofTypesSupported.parse(keycloakSession, keyAttestationsRequired,
                    globalSupportedSigningAlgorithms);
            ProofTypesSupported proofTypesSupported = allProofTypes.filterByTypes(effectiveProofTypes);
            credentialConfiguration.setProofTypesSupported(proofTypesSupported);

            credentialConfiguration.setCryptographicBindingMethodsSupported(effectiveBindingMethods);
        }

        // 优先使用 scope 配置的签名算法，否则回退全局列表
        String signingAlgSupported = credentialScope.getSigningAlg();
        List<String> signingAlgsSupported = StringUtil.isBlank(signingAlgSupported) ? globalSupportedSigningAlgorithms :
                Collections.singletonList(signingAlgSupported);
        credentialConfiguration.setCredentialSigningAlgValuesSupported(signingAlgsSupported);

        // 解析凭证元数据（展示信息与 claims）
        CredentialMetadata credentialMetadata = CredentialMetadata.parse(keycloakSession, credentialScope);
        credentialConfiguration.setCredentialMetadata(credentialMetadata);

        CredentialBuildConfig credentialBuildConfig = CredentialBuildConfig.parse(keycloakSession,
                                                                                  credentialConfiguration,
                                                                                  credentialScope);
        credentialConfiguration.setCredentialBuildConfig(credentialBuildConfig);

        return credentialConfiguration;
    }

    /**
     * 推导可验证凭证类型。
     * <p>SD-JWT 使用 vct；ISO mDL 使用 doctype（尚未支持）；JWT/LDP VC 从 credential_definition 推断。</p>
     *
     * @return 凭证类型包装对象，当前仅 SD-JWT 格式有值
     */
    public VerifiableCredentialType deriveType() {
        if (Objects.equals(format, VCFormat.SD_JWT_VC)) {
            return VerifiableCredentialType.from(vct);
        }
        return null;
    }

    /** @return 凭证配置 ID 值对象 */
    public CredentialConfigId deriveConfigId() {
        return CredentialConfigId.from(id);
    }

    /** @return 凭证格式 */
    public String getFormat() {
        return format;
    }

    /** @param format 凭证格式
     * @return 当前实例 */
    public SupportedCredentialConfiguration setFormat(String format) {
        this.format = format;
        return this;
    }

    /** @return OAuth scope */
    public String getScope() {
        return scope;
    }

    /** @param scope OAuth scope
     * @return 当前实例 */
    public SupportedCredentialConfiguration setScope(String scope) {
        this.scope = scope;
        return this;
    }

    /** @return 支持的密码学绑定方式 */
    public List<String> getCryptographicBindingMethodsSupported() {
        return cryptographicBindingMethodsSupported;
    }

    /** @param cryptographicBindingMethodsSupported 绑定方式列表
     * @return 当前实例 */
    public SupportedCredentialConfiguration setCryptographicBindingMethodsSupported(List<String> cryptographicBindingMethodsSupported) {
        this.cryptographicBindingMethodsSupported = Collections.unmodifiableList(cryptographicBindingMethodsSupported);
        return this;
    }

    /** @return 凭证配置 ID */
    public String getId() {
        return id;
    }

    /** @param id 凭证配置 ID
     * @return 当前实例 */
    public SupportedCredentialConfiguration setId(String id) {
        this.id = id;
        return this;
    }

    /** @return 支持的签名算法 */
    public List<String> getCredentialSigningAlgValuesSupported() {
        return credentialSigningAlgValuesSupported;
    }

    /** @param credentialSigningAlgValuesSupported 签名算法列表
     * @return 当前实例 */
    public SupportedCredentialConfiguration setCredentialSigningAlgValuesSupported(List<String> credentialSigningAlgValuesSupported) {
        this.credentialSigningAlgValuesSupported = Collections.unmodifiableList(credentialSigningAlgValuesSupported);
        return this;
    }

    /** @return SD-JWT vct 值 */
    public String getVct() {
        return vct;
    }

    /** @param vct vct 值
     * @return 当前实例 */
    public SupportedCredentialConfiguration setVct(String vct) {
        this.vct = vct;
        return this;
    }

    /** @return 凭证定义 */
    public CredentialDefinition getCredentialDefinition() {
        return credentialDefinition;
    }

    /** @param credentialDefinition 凭证定义
     * @return 当前实例 */
    public SupportedCredentialConfiguration setCredentialDefinition(CredentialDefinition credentialDefinition) {
        this.credentialDefinition = credentialDefinition;
        return this;
    }

    /** @return 支持的 proof 类型 */
    public ProofTypesSupported getProofTypesSupported() {
        return proofTypesSupported;
    }

    /** @param proofTypesSupported proof 类型配置
     * @return 当前实例 */
    public SupportedCredentialConfiguration setProofTypesSupported(ProofTypesSupported proofTypesSupported) {
        this.proofTypesSupported = proofTypesSupported;
        return this;
    }

    /** @return 凭证元数据 */
    public CredentialMetadata getCredentialMetadata() {
        return credentialMetadata;
    }

    /** @param credentialMetadata 凭证元数据
     * @return 当前实例 */
    public SupportedCredentialConfiguration setCredentialMetadata(CredentialMetadata credentialMetadata) {
        this.credentialMetadata = credentialMetadata;
        return this;
    }

    /** @return 内部凭证构建配置 */
    public CredentialBuildConfig getCredentialBuildConfig() {
        return credentialBuildConfig;
    }

    /** @param credentialBuildConfig 构建配置
     * @return 当前实例 */
    public SupportedCredentialConfiguration setCredentialBuildConfig(CredentialBuildConfig credentialBuildConfig) {
        this.credentialBuildConfig = credentialBuildConfig;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupportedCredentialConfiguration that = (SupportedCredentialConfiguration) o;
        return Objects.equals(id, that.id) && Objects.equals(format, that.format) && Objects.equals(scope, that.scope) && Objects.equals(cryptographicBindingMethodsSupported, that.cryptographicBindingMethodsSupported) && Objects.equals(credentialSigningAlgValuesSupported, that.credentialSigningAlgValuesSupported) && Objects.equals(vct, that.vct) && Objects.equals(credentialDefinition, that.credentialDefinition) && Objects.equals(proofTypesSupported, that.proofTypesSupported) && Objects.equals(credentialMetadata, that.credentialMetadata) && Objects.equals(credentialBuildConfig, that.credentialBuildConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, format, scope, cryptographicBindingMethodsSupported, credentialSigningAlgValuesSupported, vct, credentialDefinition, proofTypesSupported, credentialMetadata, credentialBuildConfig);
    }
}
