/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.broker.saml;

import org.keycloak.common.Profile;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.dom.saml.v2.protocol.AuthnContextComparisonType;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.saml.SamlPrincipalType;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.util.XmlKeyInfoKeyNameTransformer;
import org.keycloak.utils.StringUtil;

import static org.keycloak.common.util.UriUtils.checkUrl;

/**
 * SAML 2.0 身份代理配置：SP/IdP 实体 ID、端点 URL、签名/加密与 NameID 策略。
 * <p>支持元数据导入、Artifact 绑定与 AuthnContext 约束等选项。</p>
 * @author Pedro Igor
 */
public class SAMLIdentityProviderConfig extends IdentityProviderModel {

    public static final XmlKeyInfoKeyNameTransformer DEFAULT_XML_KEY_INFO_KEY_NAME_TRANSFORMER = XmlKeyInfoKeyNameTransformer.NONE;

    /** 配置键：SP 实体 ID。 */
    public static final String ENTITY_ID = "entityId";
    /** 配置键：IdP 实体 ID。 */
    public static final String IDP_ENTITY_ID = "idpEntityId";
    public static final String ADD_EXTENSIONS_ELEMENT_WITH_KEY_INFO = "addExtensionsElementWithKeyInfo";
    public static final String BACKCHANNEL_SUPPORTED = "backchannelSupported";
    public static final String ENCRYPTION_PUBLIC_KEY = "encryptionPublicKey";
    public static final String FORCE_AUTHN = "forceAuthn";
    public static final String NAME_ID_POLICY_FORMAT = "nameIDPolicyFormat";
    public static final String POST_BINDING_AUTHN_REQUEST = "postBindingAuthnRequest";
    public static final String POST_BINDING_LOGOUT = "postBindingLogout";
    /** 配置键：SAML 响应是否使用 HTTP-POST 绑定。 */
    public static final String POST_BINDING_RESPONSE = "postBindingResponse";
    /** 配置键：是否使用 Artifact 绑定接收响应。 */
    public static final String ARTIFACT_BINDING_RESPONSE = "artifactBindingResponse";
    public static final String SIGNATURE_ALGORITHM = "signatureAlgorithm";
    public static final String ENCRYPTION_ALGORITHM = "encryptionAlgorithm";
    /** 配置键：IdP 签名证书（PEM，多个以逗号分隔）。 */
    public static final String SIGNING_CERTIFICATE_KEY = "signingCertificate";
    /** 配置键：单点登出服务 URL。 */
    public static final String SINGLE_LOGOUT_SERVICE_URL = "singleLogoutServiceUrl";
    /** 配置键：单点登录服务 URL。 */
    public static final String SINGLE_SIGN_ON_SERVICE_URL = "singleSignOnServiceUrl";
    public static final String ARTIFACT_RESOLUTION_SERVICE_URL = "artifactResolutionServiceUrl";
    /** 配置键：是否校验 SAML 响应/断言签名。 */
    public static final String VALIDATE_SIGNATURE = "validateSignature";
    public static final String PRINCIPAL_TYPE = "principalType";
    public static final String PRINCIPAL_ATTRIBUTE = "principalAttribute";
    public static final String WANT_ASSERTIONS_ENCRYPTED = "wantAssertionsEncrypted";
    public static final String WANT_ASSERTIONS_SIGNED = "wantAssertionsSigned";
    public static final String WANT_AUTHN_REQUESTS_SIGNED = "wantAuthnRequestsSigned";
    public static final String XML_SIG_KEY_INFO_KEY_NAME_TRANSFORMER = "xmlSigKeyInfoKeyNameTransformer";
    public static final String ENABLED_FROM_METADATA  = "enabledFromMetadata";
    public static final String AUTHN_CONTEXT_COMPARISON_TYPE = "authnContextComparisonType";
    public static final String AUTHN_CONTEXT_CLASS_REFS = "authnContextClassRefs";
    public static final String AUTHN_CONTEXT_DECL_REFS = "authnContextDeclRefs";
    public static final String SIGN_SP_METADATA = "signSpMetadata";
    public static final String ALLOW_CREATE = "allowCreate";
    public static final String ATTRIBUTE_CONSUMING_SERVICE_INDEX = "attributeConsumingServiceIndex";
    public static final String ATTRIBUTE_CONSUMING_SERVICE_NAME = "attributeConsumingServiceName";
    public static final String USE_METADATA_DESCRIPTOR_URL = "useMetadataDescriptorUrl";
    public static final String DESCRIPTOR_CACHE_SECONDS = "descriptorCacheSeconds";

    public SAMLIdentityProviderConfig() {
    }

    public SAMLIdentityProviderConfig(IdentityProviderModel identityProviderModel) {
        super(identityProviderModel);
    }

    /** @return SP 实体 ID */
    public String getEntityId() {
        return getConfig().get(ENTITY_ID);
    }

    public void setEntityId(String entityId) {
        getConfig().put(ENTITY_ID, entityId);
    }

    /** @return IdP 实体 ID */
    public String getIdpEntityId() {
        return getConfig().get(IDP_ENTITY_ID);
    }

    public void setIdpEntityId(String idpEntityId) {
        getConfig().put(IDP_ENTITY_ID, idpEntityId);
    }

    /** @return SSO 服务 URL */
    public String getSingleSignOnServiceUrl() {
        return getConfig().get(SINGLE_SIGN_ON_SERVICE_URL);
    }

    public void setSingleSignOnServiceUrl(String singleSignOnServiceUrl) {
        getConfig().put(SINGLE_SIGN_ON_SERVICE_URL, singleSignOnServiceUrl);
    }

    public String getArtifactResolutionServiceUrl() {
        return getConfig().get(ARTIFACT_RESOLUTION_SERVICE_URL);
    }

    public void setArtifactResolutionServiceUrl(String artifactResolutionServiceUrl) {
        getConfig().put(ARTIFACT_RESOLUTION_SERVICE_URL, artifactResolutionServiceUrl);
    }

    public String getSingleLogoutServiceUrl() {
        return getConfig().get(SINGLE_LOGOUT_SERVICE_URL);
    }

    public void setSingleLogoutServiceUrl(String singleLogoutServiceUrl) {
        getConfig().put(SINGLE_LOGOUT_SERVICE_URL, singleLogoutServiceUrl);
    }

    /** @return 是否校验 SAML 签名 */
    public boolean isValidateSignature() {
        return Boolean.valueOf(getConfig().get(VALIDATE_SIGNATURE));
    }

    public void setValidateSignature(boolean validateSignature) {
        getConfig().put(VALIDATE_SIGNATURE, String.valueOf(validateSignature));
    }

    public boolean isForceAuthn() {
        return Boolean.valueOf(getConfig().get(FORCE_AUTHN));
    }

    public void setForceAuthn(boolean forceAuthn) {
        getConfig().put(FORCE_AUTHN, String.valueOf(forceAuthn));
    }

    /**
     * @deprecated Prefer {@link #getSigningCertificates()}}
     * @param signingCertificate
     */
    public String getSigningCertificate() {
        return getConfig().get(SIGNING_CERTIFICATE_KEY);
    }

    /**
     * @deprecated Prefer {@link #addSigningCertificate(String)}}
     * @param signingCertificate
     */
    public void setSigningCertificate(String signingCertificate) {
        getConfig().put(SIGNING_CERTIFICATE_KEY, signingCertificate);
    }

    /** 追加 IdP 签名证书（PEM 格式，逗号非编码字符可安全分隔）。 */
    public void addSigningCertificate(String signingCertificate) {
        String crt = getConfig().get(SIGNING_CERTIFICATE_KEY);
        if (crt == null || crt.isEmpty()) {
            getConfig().put(SIGNING_CERTIFICATE_KEY, signingCertificate);
        } else {
            // PEM 规范中逗号非编码字符，可安全用作多证书分隔符
            // see https://tools.ietf.org/html/rfc1421, section 4.3.2.4 Step 4: Printable Encoding
            getConfig().put(SIGNING_CERTIFICATE_KEY, crt + "," + signingCertificate);
        }
    }

    /** @return IdP 签名证书 PEM 数组 */
    public String[] getSigningCertificates() {
        String crt = getConfig().get(SIGNING_CERTIFICATE_KEY);
        if (crt == null || crt.isEmpty()) {
            return new String[] { };
        }
        // Note that "," is not coding character per PEM format specification:
        // see https://tools.ietf.org/html/rfc1421, section 4.3.2.4 Step 4: Printable Encoding
        return crt.split(",");
    }

    public String getNameIDPolicyFormat() {
        return getConfig().get(NAME_ID_POLICY_FORMAT);
    }

    public void setNameIDPolicyFormat(String nameIDPolicyFormat) {
        getConfig().put(NAME_ID_POLICY_FORMAT, nameIDPolicyFormat);
    }

    /** @return 是否要求 AuthnRequest 签名 */
    public boolean isWantAuthnRequestsSigned() {
        return Boolean.valueOf(getConfig().get(WANT_AUTHN_REQUESTS_SIGNED));
    }

    public void setWantAuthnRequestsSigned(boolean wantAuthnRequestsSigned) {
        getConfig().put(WANT_AUTHN_REQUESTS_SIGNED, String.valueOf(wantAuthnRequestsSigned));
    }

    /** @return 是否要求断言签名 */
    public boolean isWantAssertionsSigned() {
        return Boolean.valueOf(getConfig().get(WANT_ASSERTIONS_SIGNED));
    }

    public void setWantAssertionsSigned(boolean wantAssertionsSigned) {
        getConfig().put(WANT_ASSERTIONS_SIGNED, String.valueOf(wantAssertionsSigned));
    }

    /** @return 是否要求断言加密 */
    public boolean isWantAssertionsEncrypted() {
        return Boolean.valueOf(getConfig().get(WANT_ASSERTIONS_ENCRYPTED));
    }

    public void setWantAssertionsEncrypted(boolean wantAssertionsEncrypted) {
        getConfig().put(WANT_ASSERTIONS_ENCRYPTED, String.valueOf(wantAssertionsEncrypted));
    }

    public boolean isAddExtensionsElementWithKeyInfo() {
        return Boolean.valueOf(getConfig().get(ADD_EXTENSIONS_ELEMENT_WITH_KEY_INFO));
    }

    public void setAddExtensionsElementWithKeyInfo(boolean addExtensionsElementWithKeyInfo) {
        getConfig().put(ADD_EXTENSIONS_ELEMENT_WITH_KEY_INFO, String.valueOf(addExtensionsElementWithKeyInfo));
    }

    public String getSignatureAlgorithm() {
        return getConfig().get(SIGNATURE_ALGORITHM);
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        getConfig().put(SIGNATURE_ALGORITHM, signatureAlgorithm);
    }

    public String getEncryptionAlgorithm() {
        return getConfig().get(ENCRYPTION_ALGORITHM);
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        getConfig().put(ENCRYPTION_ALGORITHM, encryptionAlgorithm);
    }

    public String getEncryptionPublicKey() {
        return getConfig().get(ENCRYPTION_PUBLIC_KEY);
    }

    public void setEncryptionPublicKey(String encryptionPublicKey) {
        getConfig().put(ENCRYPTION_PUBLIC_KEY, encryptionPublicKey);
    }

    public boolean isPostBindingAuthnRequest() {
        return Boolean.valueOf(getConfig().get(POST_BINDING_AUTHN_REQUEST));
    }

    public void setPostBindingAuthnRequest(boolean postBindingAuthnRequest) {
        getConfig().put(POST_BINDING_AUTHN_REQUEST, String.valueOf(postBindingAuthnRequest));
    }

    /** @return SAML 响应是否使用 POST 绑定 */
    public boolean isPostBindingResponse() {
        return Boolean.valueOf(getConfig().get(POST_BINDING_RESPONSE));
    }

    public void setPostBindingResponse(boolean postBindingResponse) {
        getConfig().put(POST_BINDING_RESPONSE, String.valueOf(postBindingResponse));
    }

    /** @return SLO 请求是否使用 POST 绑定（默认跟随响应绑定） */
    public boolean isPostBindingLogout() {
        String postBindingLogout = getConfig().get(POST_BINDING_LOGOUT);
        if (postBindingLogout == null) {
            // 新增字段时保持兼容：未配置时默认与响应绑定方式一致
            return isPostBindingResponse();
        }
        return Boolean.valueOf(postBindingLogout);
    }

    public void setPostBindingLogout(boolean postBindingLogout) {
        getConfig().put(POST_BINDING_LOGOUT, String.valueOf(postBindingLogout));
    }

    /** @return 是否支持 backchannel 登出 */
    public boolean isBackchannelSupported() {
        return Boolean.valueOf(getConfig().get(BACKCHANNEL_SUPPORTED));
    }

    public void setBackchannelSupported(boolean backchannel) {
        getConfig().put(BACKCHANNEL_SUPPORTED, String.valueOf(backchannel));
    }

    /** @return 是否使用 Artifact 绑定接收响应 */
    public boolean isArtifactBindingResponse() {
        return Boolean.valueOf(getConfig().get(ARTIFACT_BINDING_RESPONSE));
    }

    public void setArtifactBindingResponse(boolean backchannel) {
        getConfig().put(ARTIFACT_BINDING_RESPONSE, String.valueOf(backchannel));
    }

    /**
     * Always returns non-{@code null} result.
     * @return Configured ransformer of {@link #DEFAULT_XML_KEY_INFO_KEY_NAME_TRANSFORMER} if not set.
     */
    /** @return XML 签名 KeyInfo KeyName 转换器，未配置时返回默认值 */
    public XmlKeyInfoKeyNameTransformer getXmlSigKeyInfoKeyNameTransformer() {
        return XmlKeyInfoKeyNameTransformer.from(getConfig().get(XML_SIG_KEY_INFO_KEY_NAME_TRANSFORMER), DEFAULT_XML_KEY_INFO_KEY_NAME_TRANSFORMER);
    }

    public void setXmlSigKeyInfoKeyNameTransformer(XmlKeyInfoKeyNameTransformer xmlSigKeyInfoKeyNameTransformer) {
        getConfig().put(XML_SIG_KEY_INFO_KEY_NAME_TRANSFORMER,
          xmlSigKeyInfoKeyNameTransformer == null
            ? null
            : xmlSigKeyInfoKeyNameTransformer.name());
    }

    /** @return 断言校验允许的时钟偏差（秒） */
    public int getAllowedClockSkew() {
        int result = 0;
        String allowedClockSkew = getConfig().get(ALLOWED_CLOCK_SKEW);
        if (allowedClockSkew != null && !allowedClockSkew.isEmpty()) {
            try {
                result = Integer.parseInt(allowedClockSkew);
                if (result < 0) {
                    result = 0;
                }
            } catch (NumberFormatException e) {
                // 解析失败时使用 0
            }
        }
        return result;
    }

    public void setAllowedClockSkew(int allowedClockSkew) {
        if (allowedClockSkew < 0) {
            getConfig().remove(ALLOWED_CLOCK_SKEW);
        } else {
            getConfig().put(ALLOWED_CLOCK_SKEW, String.valueOf(allowedClockSkew));
        }
    }

    /** @return 联邦用户主标识类型，默认 SUBJECT */
    public SamlPrincipalType getPrincipalType() {
        return SamlPrincipalType.from(getConfig().get(PRINCIPAL_TYPE), SamlPrincipalType.SUBJECT);
    }

    public void setPrincipalType(SamlPrincipalType principalType) {
        getConfig().put(PRINCIPAL_TYPE,
            principalType == null
                ? null
                : principalType.name());
    }

    /** @return 当 principalType 为 ATTRIBUTE 时的属性名 */
    public String getPrincipalAttribute() {
        return getConfig().get(PRINCIPAL_ATTRIBUTE);
    }

    public void setPrincipalAttribute(String principalAttribute) {
        getConfig().put(PRINCIPAL_ATTRIBUTE, principalAttribute);
    }

    /** @return 是否根据元数据 validUntil 自动启用 IdP */
    public boolean isEnabledFromMetadata() {
        return Boolean.valueOf(getConfig().get(ENABLED_FROM_METADATA ));
    }

    public void setEnabledFromMetadata(boolean enabled) {
        getConfig().put(ENABLED_FROM_METADATA , String.valueOf(enabled));
    }

    /** @return AuthnContext 比较类型，默认 EXACT */
    public AuthnContextComparisonType getAuthnContextComparisonType() {
        return AuthnContextComparisonType.fromValue(getConfig().getOrDefault(AUTHN_CONTEXT_COMPARISON_TYPE, AuthnContextComparisonType.EXACT.value()));
    }

    public void setAuthnContextComparisonType(AuthnContextComparisonType authnContextComparisonType) {
        getConfig().put(AUTHN_CONTEXT_COMPARISON_TYPE, authnContextComparisonType.value());
    }

    public String getAuthnContextClassRefs() {
        return getConfig().get(AUTHN_CONTEXT_CLASS_REFS);
    }

    public void setAuthnContextClassRefs(String authnContextClassRefs) {
        getConfig().put(AUTHN_CONTEXT_CLASS_REFS, authnContextClassRefs);
    }

    public String getAuthnContextDeclRefs() {
        return getConfig().get(AUTHN_CONTEXT_DECL_REFS);
    }

    public void setAuthnContextDeclRefs(String authnContextDeclRefs) {
        getConfig().put(AUTHN_CONTEXT_DECL_REFS, authnContextDeclRefs);
    }

    public boolean isSignSpMetadata() {
        return Boolean.valueOf(getConfig().get(SIGN_SP_METADATA));
    }

    public void setSignSpMetadata(boolean signSpMetadata) {
        getConfig().put(SIGN_SP_METADATA, String.valueOf(signSpMetadata));
    }

    public boolean isAllowCreate() {
        return Boolean.valueOf(getConfig().get(ALLOW_CREATE));
    }

    public void setAllowCreated(boolean allowCreate) {
        getConfig().put(ALLOW_CREATE, String.valueOf(allowCreate));
    }

    public Integer getAttributeConsumingServiceIndex() {
        Integer result = null;
        String strAttributeConsumingServiceIndex = getConfig().get(ATTRIBUTE_CONSUMING_SERVICE_INDEX);
        if (strAttributeConsumingServiceIndex != null && !strAttributeConsumingServiceIndex.isEmpty()) {
            try {
                result = Integer.parseInt(strAttributeConsumingServiceIndex);
                if (result < 0) {
                    result = null;
                }
            } catch (NumberFormatException e) {
                // 解析失败时使用 null
            }
        }
        return result;
    }

    public void setAttributeConsumingServiceIndex(Integer attributeConsumingServiceIndex) {
        if (attributeConsumingServiceIndex == null || attributeConsumingServiceIndex < 0) {
            getConfig().remove(ATTRIBUTE_CONSUMING_SERVICE_INDEX);
        } else {
            getConfig().put(ATTRIBUTE_CONSUMING_SERVICE_INDEX, String.valueOf(attributeConsumingServiceIndex));
        }
    }

    public void setAttributeConsumingServiceName(String attributeConsumingServiceName) {
        getConfig().put(ATTRIBUTE_CONSUMING_SERVICE_NAME, attributeConsumingServiceName);
    }

    public String getAttributeConsumingServiceName() {
        return getConfig().get(ATTRIBUTE_CONSUMING_SERVICE_NAME);
    }

    public void setUseMetadataDescriptorUrl(Boolean useDescriptorUrl) {
        if (useDescriptorUrl == null || !useDescriptorUrl) {
            getConfig().remove(USE_METADATA_DESCRIPTOR_URL);
        } else {
            getConfig().put(USE_METADATA_DESCRIPTOR_URL, Boolean.TRUE.toString());
        }
    }

    /** @return 是否从元数据描述符 URL 动态加载配置 */
    public boolean isUseMetadataDescriptorUrl() {
        return Boolean.parseBoolean(getConfig().get(USE_METADATA_DESCRIPTOR_URL));
    }

    /** @return 元数据描述符缓存秒数，无效时为 null */
    public Long getDescriptorCacheSeconds() {
        String descriptorCacheSeconds = getConfig().get(DESCRIPTOR_CACHE_SECONDS);
        if (descriptorCacheSeconds != null && !descriptorCacheSeconds.isEmpty()) {
            try {
                Long descriptorCacheSecondsLong = Long.valueOf(descriptorCacheSeconds);
                if (descriptorCacheSecondsLong > 0) {
                    return descriptorCacheSecondsLong;
                }
            } catch (NumberFormatException e) {
                // ignore it and use null
            }
        }
        return null;
    }

    public void setDescriptorCacheSeconds(Long descriptorCacheSeconds) {
        if (descriptorCacheSeconds == null || descriptorCacheSeconds <= 0) {
            getConfig().remove(DESCRIPTOR_CACHE_SECONDS);
        } else {
            getConfig().put(DESCRIPTOR_CACHE_SECONDS, String.valueOf(descriptorCacheSeconds));
        }
    }

    /** 校验 SSO/SLO/元数据/Artifact URL 的 SSL 要求及 NameID 与主标识兼容性。 */
    @Override
    public void validate(RealmModel realm) {
        SslRequired sslRequired = realm.getSslRequired();

        checkUrl(sslRequired, getSingleLogoutServiceUrl(), SINGLE_LOGOUT_SERVICE_URL);
        checkUrl(sslRequired, getSingleSignOnServiceUrl(), SINGLE_SIGN_ON_SERVICE_URL);
        if (StringUtil.isNotBlank(getMetadataDescriptorUrl())) {
            checkUrl(sslRequired, getMetadataDescriptorUrl(), METADATA_DESCRIPTOR_URL);
        }
        if (isUseMetadataDescriptorUrl()) {
            if (StringUtil.isBlank(getMetadataDescriptorUrl())) {
                throw new IllegalArgumentException(USE_METADATA_DESCRIPTOR_URL + " needs a non-empty URL for " + METADATA_DESCRIPTOR_URL);
            }
        }
        if (StringUtil.isNotBlank(getArtifactResolutionServiceUrl())) {
            checkUrl(sslRequired, getArtifactResolutionServiceUrl(), ARTIFACT_RESOLUTION_SERVICE_URL);
        }
        // Transient NameID 格式不能与 SUBJECT 主标识类型同时使用
        if (JBossSAMLURIConstants.NAMEID_FORMAT_TRANSIENT.get().equals(getNameIDPolicyFormat()) && SamlPrincipalType.SUBJECT == getPrincipalType())
            throw new IllegalArgumentException("Can not have Transient NameID Policy Format together with SUBJECT Principal Type");

    }

    /** SAML 默认不在会话存储 token；V2 API 特性开启且显式配置时为 true。 */
    @Override
    public boolean isStoreTokenInSession() {
        // SAML 默认不在会话中存储 token
        return Profile.isFeatureEnabled(Profile.Feature.IDENTITY_BROKERING_API_V2)
                & Boolean.parseBoolean(getConfig().get(STORE_TOKEN_IN_SESSION));
    }
}
