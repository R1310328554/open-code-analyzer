package org.keycloak.protocol.oid4vc.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.keycloak.constants.OID4VCIConstants;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.protocol.oid4vc.clientpolicy.CredentialClientPolicy;
import org.keycloak.representations.idm.ClientScopeRepresentation;

import static org.keycloak.models.ClientScopeModel.INCLUDE_IN_TOKEN_SCOPE;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VCT;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_BINDING_REQUIRED;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_BUILD_CONFIG_HASH_ALGORITHM;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_BUILD_CONFIG_HASH_ALGORITHM_DEFAULT;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_BUILD_CONFIG_SD_JWT_VISIBLE_CLAIMS;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_BUILD_CONFIG_SD_JWT_VISIBLE_CLAIMS_DEFAULT;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_BUILD_CONFIG_TOKEN_JWS_TYPE;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_CONFIGURATION_ID;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_CONTEXTS;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_CRYPTOGRAPHIC_BINDING_METHODS;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_DISPLAY;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_EXPIRY_IN_SECONDS;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_EXPIRY_IN_SECONDS_DEFAULT;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_FORMAT;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_FORMAT_DEFAULT;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_IDENTIFIER;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_ISSUER_DID;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_KEY_ATTESTATION_REQUIRED;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_KEY_ATTESTATION_REQUIRED_KEY_STORAGE;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_KEY_ATTESTATION_REQUIRED_USER_AUTH;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_REFRESH_INTERVAL_IN_SECONDS;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_SD_JWT_NUMBER_OF_DECOYS;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_SD_JWT_NUMBER_OF_DECOYS_DEFAULT;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_SIGNING_ALG;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_SIGNING_KEY_ID;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_SUPPORTED_TYPES;
import static org.keycloak.models.oid4vci.CredentialScopeModel.getDefaultTokenJwsTypeForFormat;

/**
 * 扩展 {@link ClientScopeRepresentation} 的 OID4VCI 凭证范围表示。
 * <p>封装凭证格式、有效期、签名算法、绑定方式、SD-JWT 配置及密钥证明要求等属性，
 * 供管理 API 与签发元数据构建使用。</p>
 *
 * @author Thomas Diesler
 */
public class CredentialScopeRepresentation extends ClientScopeRepresentation {

    /**
     * 按名称创建 OID4VCI 凭证范围，并设置默认格式与构建参数。
     *
     * @param name 客户端范围名称
     */
        this.name = name;
        this.protocol = OID4VCIConstants.OID4VC_PROTOCOL;
        setFormat(VC_FORMAT_DEFAULT);
        setBuildConfigHashAlgorithm(VC_BUILD_CONFIG_HASH_ALGORITHM_DEFAULT);
        setBuildConfigSdJwtVisibleClaims(VC_BUILD_CONFIG_SD_JWT_VISIBLE_CLAIMS_DEFAULT);
        setBuildConfigTokenJwsType(getDefaultTokenJwsTypeForFormat(VC_FORMAT_DEFAULT));
        setSdJwtNumberOfDecoys(VC_SD_JWT_NUMBER_OF_DECOYS_DEFAULT);
        setExpiryInSeconds(VC_EXPIRY_IN_SECONDS_DEFAULT);
    }

    /** 从 {@link ClientScopeModel} 转换构造。 */
    public CredentialScopeRepresentation(ClientScopeModel clientScope) {
        this(ModelToRepresentation.toRepresentation(clientScope));
    }

    /** 从已有 {@link ClientScopeRepresentation} 复制属性构造。 */
    public CredentialScopeRepresentation(ClientScopeRepresentation clientScope) {
        this.id = clientScope.getId();
        this.name = clientScope.getName();
        this.description = clientScope.getDescription();
        this.protocol = clientScope.getProtocol();
        this.attributes = clientScope.getAttributes();
        this.protocolMappers = clientScope.getProtocolMappers();
    }

    /** @return 是否包含在令牌 scope 中 */
    public boolean getIncludeInTokenScope() {
        return Boolean.parseBoolean(getAttribute(INCLUDE_IN_TOKEN_SCOPE));
    }

    /** @param includeInScope 是否包含在令牌 scope */
    public CredentialScopeRepresentation setIncludeInTokenScope(boolean includeInScope) {
        return setAttribute(INCLUDE_IN_TOKEN_SCOPE, String.valueOf(includeInScope));
    }

    /** @return 签发者 DID */
    public String getIssuerDid() {
        return getAttribute(VC_ISSUER_DID);
    }

    /** @param issuerDid 签发者 DID */
    public CredentialScopeRepresentation setIssuerDid(String issuerDid) {
        return setAttribute(VC_ISSUER_DID, issuerDid);
    }

    /** @return 凭证配置 ID（元数据 supported_credentials 键） */
    public String getCredentialConfigurationId() {
        return getAttribute(VC_CONFIGURATION_ID);
    }

    /** @param credentialConfigurationId 凭证配置 ID */
    public CredentialScopeRepresentation setCredentialConfigurationId(String credentialConfigurationId) {
        return setAttribute(VC_CONFIGURATION_ID, credentialConfigurationId);
    }

    /** @return 凭证标识符 */
    public String getCredentialIdentifier() {
        return getAttribute(VC_IDENTIFIER);
    }

    /** @param credentialIdentifier 凭证标识符 */
    public CredentialScopeRepresentation setCredentialIdentifier(String credentialIdentifier) {
        return setAttribute(VC_IDENTIFIER, credentialIdentifier);
    }

    /** @return 凭证格式（jwt_vc、ldp_vc、vc+sd-jwt 等） */
    public String getFormat() {
        return getAttribute(VC_FORMAT);
    }

    /** @param credentialFormat 凭证格式 */
    public CredentialScopeRepresentation setFormat(String credentialFormat) {
        return setAttribute(VC_FORMAT, credentialFormat);
    }

    /** @return 凭证有效期（秒） */
    public Integer getExpiryInSeconds() {
        return Optional.ofNullable(getAttribute(VC_EXPIRY_IN_SECONDS))
                .map(Integer::parseInt)
                .orElse(null);
    }

    /** @param expiryInSeconds 有效期秒数 */
    public CredentialScopeRepresentation setExpiryInSeconds(Integer expiryInSeconds) {
        return setAttribute(VC_EXPIRY_IN_SECONDS, Optional.ofNullable(expiryInSeconds)
                        .map(String::valueOf)
                        .orElse(null));
    }

    /** @return 刷新间隔（秒） */
    public Integer getRefreshIntervalInSeconds() {
        return Optional.ofNullable(getAttribute(VC_REFRESH_INTERVAL_IN_SECONDS))
                .map(Integer::parseInt)
                .orElse(null);
    }

    /** @param refreshIntervalInSeconds 刷新间隔秒数 */
    public CredentialScopeRepresentation setRefreshIntervalInSeconds(Integer refreshIntervalInSeconds) {
        return setAttribute(VC_REFRESH_INTERVAL_IN_SECONDS, Optional.ofNullable(refreshIntervalInSeconds)
                .map(String::valueOf)
                .orElse(null));
    }

    /** @return SD-JWT 诱饵声明数量 */
    public Integer getSdJwtNumberOfDecoys() {
        return Optional.ofNullable(getAttribute(VC_SD_JWT_NUMBER_OF_DECOYS))
                .map(Integer::parseInt)
                .orElse(null);
    }

    /** @param sdJwtNumberOfDecoys 诱饵数量 */
    public CredentialScopeRepresentation setSdJwtNumberOfDecoys(Integer sdJwtNumberOfDecoys) {
        return setAttribute(VC_SD_JWT_NUMBER_OF_DECOYS, Optional.ofNullable(sdJwtNumberOfDecoys)
                .map(String::valueOf)
                .orElse(null));
    }

    /** @return SD-JWT 凭证类型（vct） */
    public String getVct() {
        return getAttribute(VCT);
    }

    /** @param vct 凭证类型标识 */
    public CredentialScopeRepresentation setVct(String vct) {
        return setAttribute(VCT, vct);
    }

    /** @return 令牌 JWS typ 头值 */
    public String getBuildConfigTokenJwsType() {
        return getAttribute(VC_BUILD_CONFIG_TOKEN_JWS_TYPE);
    }

    /** @param tokenJwsType JWS typ 值 */
    public CredentialScopeRepresentation setBuildConfigTokenJwsType(String tokenJwsType) {
        return setAttribute(VC_BUILD_CONFIG_TOKEN_JWS_TYPE, tokenJwsType);
    }

    /** @return 签名密钥 ID */
    public String getSigningKeyId() {
        return getAttribute(VC_SIGNING_KEY_ID);
    }

    /** @param signingKeyId 签名密钥 ID */
    public CredentialScopeRepresentation setSigningKeyId(String signingKeyId) {
        return setAttribute(VC_SIGNING_KEY_ID, signingKeyId);
    }

    /** @return 构建配置哈希算法 */
    public String getBuildConfigHashAlgorithm() {
        return getAttribute(VC_BUILD_CONFIG_HASH_ALGORITHM);
    }

    /** @param hashAlgorithm 哈希算法名 */
    public CredentialScopeRepresentation setBuildConfigHashAlgorithm(String hashAlgorithm) {
        return setAttribute(VC_BUILD_CONFIG_HASH_ALGORITHM, hashAlgorithm);
    }

    /** @return 支持的凭证类型列表 */
    public List<String> getSupportedCredentialTypes() {
        return Optional.ofNullable(getAttribute(VC_SUPPORTED_TYPES))
                .map(s -> s.split(","))
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    /** @param supportedCredentialTypes 逗号分隔的类型字符串 */
    public CredentialScopeRepresentation setSupportedCredentialTypes(String supportedCredentialTypes) {
        return setAttribute(VC_SUPPORTED_TYPES, supportedCredentialTypes);
    }

    /** @param supportedCredentialTypes 类型列表 */
    public CredentialScopeRepresentation setSupportedCredentialTypes(List<String> supportedCredentialTypes) {
        return setAttribute(VC_SUPPORTED_TYPES, String.join(",", supportedCredentialTypes));
    }

    /** @return JSON-LD @context URI 列表 */
    public List<String> getVcContexts() {
        return Optional.ofNullable(getAttribute(VC_CONTEXTS))
                .map(s -> s.split(","))
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    /** @param vcContexts 逗号分隔的 context URI */
    public CredentialScopeRepresentation setVcContexts(String vcContexts) {
        return setAttribute(VC_CONTEXTS, vcContexts);
    }

    /** @param vcContexts context URI 列表 */
    public CredentialScopeRepresentation setVcContexts(List<String> vcContexts) {
        return setAttribute(VC_CONTEXTS, String.join(",", vcContexts));
    }

    /** @return 签名算法（如 ES256） */
    public String getSigningAlg() {
        return getAttribute(VC_SIGNING_ALG);
    }

    /** @param signingAlg 签名算法 */
    public CredentialScopeRepresentation setSigningAlg(String signingAlg) {
        return setAttribute(VC_SIGNING_ALG, signingAlg);
    }

    /** 本凭证配置是否要求密码学持有者绑定（holder binding）。 */
    public boolean isBindingRequired() {
        return Boolean.parseBoolean(getAttribute(VC_BINDING_REQUIRED));
    }

    /** @param required 是否要求持有者绑定 */
    public CredentialScopeRepresentation setBindingRequired(boolean required) {
        return setAttribute(VC_BINDING_REQUIRED, String.valueOf(required));
    }

    /** @return 支持的密码学绑定方式列表 */
    public List<String> getCryptographicBindingMethods() {
        return Optional.ofNullable(getAttribute(VC_CRYPTOGRAPHIC_BINDING_METHODS))
                .map(s -> s.split(","))
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    /** @param cryptographicBindingMethods 逗号分隔的绑定方式 */
    public CredentialScopeRepresentation setCryptographicBindingMethods(String cryptographicBindingMethods) {
        return setAttribute(VC_CRYPTOGRAPHIC_BINDING_METHODS, cryptographicBindingMethods);
    }

    /** @param cryptographicBindingMethods 绑定方式列表 */
    public CredentialScopeRepresentation setCryptographicBindingMethods(List<String> cryptographicBindingMethods) {
        return setAttribute(VC_CRYPTOGRAPHIC_BINDING_METHODS, String.join(",", cryptographicBindingMethods));
    }

    /** @return SD-JWT 可见声明名列表 */
    public List<String> getBuildConfigSdJwtVisibleClaims() {
        return Optional.ofNullable(getAttribute(VC_BUILD_CONFIG_SD_JWT_VISIBLE_CLAIMS))
                .map(s -> s.split(","))
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    /** @param sdJwtVisibleClaims 逗号分隔的可见声明 */
    public CredentialScopeRepresentation setBuildConfigSdJwtVisibleClaims(String sdJwtVisibleClaims) {
        return setAttribute(VC_BUILD_CONFIG_SD_JWT_VISIBLE_CLAIMS, sdJwtVisibleClaims);
    }

    /** @param sdJwtVisibleClaims 可见声明列表 */
    public CredentialScopeRepresentation setBuildConfigSdJwtVisibleClaims(List<String> sdJwtVisibleClaims) {
        return setAttribute(VC_BUILD_CONFIG_SD_JWT_VISIBLE_CLAIMS, String.join(",", sdJwtVisibleClaims));
    }

    /** @return 展示元数据 JSON 字符串 */
    public String getDisplay() {
        return getAttribute(VC_DISPLAY);
    }

    /** @param vcDisplay 展示元数据 JSON */
    public CredentialScopeRepresentation setDisplay(String vcDisplay) {
        return setAttribute(VC_DISPLAY, vcDisplay);
    }

    /** @return 是否要求密钥证明（key attestation） */
    public boolean isKeyAttestationRequired() {
        return Boolean.parseBoolean(getAttribute(VC_KEY_ATTESTATION_REQUIRED));
    }

    /** @param keyAttestationRequired 是否要求密钥证明 */
    public CredentialScopeRepresentation setKeyAttestationRequired(boolean keyAttestationRequired) {
        return setAttribute(VC_KEY_ATTESTATION_REQUIRED, String.valueOf(keyAttestationRequired));
    }

    public List<String> getRequiredKeyAttestationKeyStorage() {
        return Optional.ofNullable(getAttribute(VC_KEY_ATTESTATION_REQUIRED_KEY_STORAGE))
                .map(s -> Arrays.asList(s.split(",")))
                // 须返回 null 而非空列表：key_storage 与 user_authentication 均缺省时
                // key_attestations_required 可为空对象，表示需要密钥证明但无额外约束；
                // 元数据端点不应写入空对象
                .orElse(null);
    }

    /** @param keyStorage 要求的密钥存储类型列表 */
    public CredentialScopeRepresentation setRequiredKeyAttestationKeyStorage(List<String> keyStorage) {
        return setAttribute(VC_KEY_ATTESTATION_REQUIRED_KEY_STORAGE, Optional.ofNullable(keyStorage)
                .map(list -> String.join(",")).orElse(null));
    }

    public List<String> getRequiredKeyAttestationUserAuthentication() {
        return Optional.ofNullable(getAttribute(VC_KEY_ATTESTATION_REQUIRED_USER_AUTH))
                .map(s -> Arrays.asList(s.split(",")))
                // it is important to return null here instead of an empty list:
                // If both key_storage and user_authentication parameters are absent, the
                // key_attestations_required parameter may be empty, indicating a key attestation is needed
                // without additional constraints. Meaning we must not add empty objects to the metadata endpoint
                .orElse(null);
    }

    /** @param userAuthentication 要求的用户认证方式列表 */
    public CredentialScopeRepresentation setRequiredKeyAttestationUserAuthentication(List<String> userAuthentication) {
        return setAttribute(VC_KEY_ATTESTATION_REQUIRED_USER_AUTH, Optional.ofNullable(userAuthentication)
                .map(list -> String.join(",")).orElse(null));
    }

    /**
     * 读取凭证客户端策略当前值。
     *
     * @param policy 策略定义
     * @return 策略值
     */
        T currentValue = policy.getCurrentValue(this);
        return currentValue;
    }

    /**
     * 设置凭证客户端策略值。
     *
     * @param policy 策略定义
     * @param value  新值
     * @return 当前实例
     */
        return setAttribute(policy.getAttrName(), String.valueOf(value));
    }

    /** @param key 属性键 @return 属性值，无则 null */
    public String getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    // 私有辅助 ---------------------------------------------------------------------------------------------------------

    /** 写入或移除单个范围属性。 */
    private CredentialScopeRepresentation setAttribute(String key, String value) {
        if (attributes == null) {
            attributes = new LinkedHashMap<>();
        }
        if (value != null) {
            attributes.put(key, value);
        } else {
            attributes.remove(key);
        }
        return this;
    }
}
