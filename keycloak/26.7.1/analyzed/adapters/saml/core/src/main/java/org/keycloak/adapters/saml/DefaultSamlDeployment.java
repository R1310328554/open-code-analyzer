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

package org.keycloak.adapters.saml;

import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.keycloak.adapters.saml.rotation.SamlDescriptorPublicKeyLocator;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.rotation.CompositeKeyLocator;
import org.keycloak.rotation.HardcodedKeyLocator;
import org.keycloak.rotation.KeyLocator;
import org.keycloak.saml.SignatureAlgorithm;

import org.apache.http.client.HttpClient;

/**
 * {@link SamlDeployment} 的默认实现，承载 SP 与 IdP 的完整部署配置。
 *
 * <p>包含 IdP 元数据、SSO/SLO 服务配置、签名/解密密钥、角色映射提供者及
 * 主体命名策略等运行时参数。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DefaultSamlDeployment implements SamlDeployment {

    /**
     * IdP 单点登录（SSO）服务的默认实现。
     */
    public static class DefaultSingleSignOnService implements IDP.SingleSignOnService {
        private boolean signRequest;
        private boolean validateResponseSignature;
        private boolean validateAssertionSignature;
        private Binding requestBinding;
        private Binding responseBinding;
        private String requestBindingUrl;
        private URI assertionConsumerServiceUrl;

        @Override
        public boolean signRequest() {
            return signRequest;
        }

        @Override
        public boolean validateResponseSignature() {
            return validateResponseSignature;
        }

        @Override
        public boolean validateAssertionSignature() {
            return validateAssertionSignature;
        }

        @Override
        public Binding getRequestBinding() {
            return requestBinding;
        }

        @Override
        public Binding getResponseBinding() {
            return responseBinding;
        }

        @Override
        public String getRequestBindingUrl() {
            return requestBindingUrl;
        }

        @Override
        public URI getAssertionConsumerServiceUrl() {
            return assertionConsumerServiceUrl;
        }

        public void setAssertionConsumerServiceUrl(URI assertionConsumerServiceUrl) {
            this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        }

        public void setSignRequest(boolean signRequest) {
            this.signRequest = signRequest;
        }

        public void setValidateResponseSignature(boolean validateResponseSignature) {
            this.validateResponseSignature = validateResponseSignature;
        }

        public void setValidateAssertionSignature(boolean validateAssertionSignature) {
            this.validateAssertionSignature = validateAssertionSignature;
        }

        public void setRequestBinding(Binding requestBinding) {
            this.requestBinding = requestBinding;
        }

        public void setResponseBinding(Binding responseBinding) {
            this.responseBinding = responseBinding;
        }

        public void setRequestBindingUrl(String requestBindingUrl) {
            this.requestBindingUrl = requestBindingUrl;
        }
    }

    /**
     * IdP 单点登出（SLO）服务的默认实现。
     */
    public static class DefaultSingleLogoutService implements IDP.SingleLogoutService {
        private boolean validateRequestSignature;
        private boolean validateResponseSignature;
        private boolean signRequest;
        private boolean signResponse;
        private Binding requestBinding;
        private Binding responseBinding;
        private String requestBindingUrl;
        private String responseBindingUrl;

        @Override
        public boolean validateRequestSignature() {
            return validateRequestSignature;
        }

        @Override
        public boolean validateResponseSignature() {
            return validateResponseSignature;
        }

        @Override
        public boolean signRequest() {
            return signRequest;
        }

        @Override
        public boolean signResponse() {
            return signResponse;
        }

        @Override
        public Binding getRequestBinding() {
            return requestBinding;
        }

        @Override
        public Binding getResponseBinding() {
            return responseBinding;
        }

        @Override
        public String getRequestBindingUrl() {
            return requestBindingUrl;
        }

        @Override
        public String getResponseBindingUrl() {
            return responseBindingUrl;
        }

        public void setValidateRequestSignature(boolean validateRequestSignature) {
            this.validateRequestSignature = validateRequestSignature;
        }

        public void setValidateResponseSignature(boolean validateResponseSignature) {
            this.validateResponseSignature = validateResponseSignature;
        }

        public void setSignRequest(boolean signRequest) {
            this.signRequest = signRequest;
        }

        public void setSignResponse(boolean signResponse) {
            this.signResponse = signResponse;
        }

        public void setRequestBinding(Binding requestBinding) {
            this.requestBinding = requestBinding;
        }

        public void setResponseBinding(Binding responseBinding) {
            this.responseBinding = responseBinding;
        }

        public void setRequestBindingUrl(String requestBindingUrl) {
            this.requestBindingUrl = requestBindingUrl;
        }

        public void setResponseBindingUrl(String responseBindingUrl) {
            this.responseBindingUrl = responseBindingUrl;
        }
    }

    /**
     * 身份提供者（IdP）配置的默认实现。
     *
     * <p>管理 IdP EntityID、SSO/SLO 端点、签名验证密钥定位器及
     * SAML 元数据获取相关参数。</p>
     */
    public static class DefaultIDP implements IDP {

        /** 描述符公钥缓存默认 TTL：24 小时（秒） */
        private static final int DEFAULT_CACHE_TTL = 24 * 60 * 60;

        private String entityID;
        private final CompositeKeyLocator signatureValidationKeyLocator = new CompositeKeyLocator();
        private SingleSignOnService singleSignOnService;
        private SingleLogoutService singleLogoutService;
        private final List<PublicKey> signatureValidationKeys = new LinkedList<>();
        private int minTimeBetweenDescriptorRequests;
        private int allowedClockSkew;
        private HttpClient client;
        private String metadataUrl;

        @Override
        public String getEntityID() {
            return entityID;
        }

        @Override
        public SingleSignOnService getSingleSignOnService() {
            return singleSignOnService;
        }

        @Override
        public SingleLogoutService getSingleLogoutService() {
            return singleLogoutService;
        }

        @Override
        public KeyLocator getSignatureValidationKeyLocator() {
            return this.signatureValidationKeyLocator;
        }

        @Override
        public int getMinTimeBetweenDescriptorRequests() {
            return minTimeBetweenDescriptorRequests;
        }

        public void setMinTimeBetweenDescriptorRequests(int minTimeBetweenDescriptorRequests) {
            this.minTimeBetweenDescriptorRequests = minTimeBetweenDescriptorRequests;
        }

        public void setEntityID(String entityID) {
            this.entityID = entityID;
        }

        public void addSignatureValidationKey(PublicKey signatureValidationKey) {
            this.signatureValidationKeys.add(signatureValidationKey);
        }

        public void setSingleSignOnService(SingleSignOnService singleSignOnService) {
            this.singleSignOnService = singleSignOnService;
        }

        public void setSingleLogoutService(SingleLogoutService singleLogoutService) {
            this.singleLogoutService = singleLogoutService;
        }

        /**
         * 刷新签名验证密钥定位器配置。
         *
         * <p>若配置了硬编码公钥则优先使用；否则从 IdP SAML 元数据动态获取。</p>
         */
        public void refreshKeyLocatorConfiguration() {
            this.signatureValidationKeyLocator.clear();

            // 配置了硬编码密钥时仅使用该密钥，否则配置动态密钥定位器
            if (! this.signatureValidationKeys.isEmpty()) {
                this.signatureValidationKeyLocator.add(new HardcodedKeyLocator(this.signatureValidationKeys));
            } else if (this.singleSignOnService != null) {
                HttpClient httpClient = getClient();
                SamlDescriptorPublicKeyLocator samlDescriptorPublicKeyLocator =
                  new SamlDescriptorPublicKeyLocator(
                    getMetadataUrl(), this.minTimeBetweenDescriptorRequests, DEFAULT_CACHE_TTL, httpClient);
                this.signatureValidationKeyLocator.add(samlDescriptorPublicKeyLocator);
            }
        }

        @Override
        public HttpClient getClient() {
            return this.client;
        }

        public void setClient(HttpClient client) {
            this.client = client;
        }

        /**
         * 获取 IdP SAML 元数据 URL；未显式配置时默认为 SSO 请求 URL + "/descriptor"。
         */
        public String getMetadataUrl() {
            return metadataUrl == null ? singleSignOnService.getRequestBindingUrl() + "/descriptor" : metadataUrl;
        }

        public void setMetadataUrl(String metadataUrl) {
            this.metadataUrl = metadataUrl;
        }

        @Override
        public int getAllowedClockSkew() {
            return allowedClockSkew;
        }


        public void setAllowedClockSkew(int allowedClockSkew) {
            this.allowedClockSkew = allowedClockSkew;
        }
    }

    /** IdP 配置 */
    private IDP idp;
    /** 适配器是否已完成配置 */
    private boolean configured;
    /** SSL 要求级别 */
    private SslRequired sslRequired = SslRequired.EXTERNAL;
    /** SP Entity ID */
    private String entityID;
    /** NameID 策略格式 URI */
    private String nameIDPolicyFormat;
    /** 是否强制重新认证（ForceAuthn） */
    private boolean forceAuthentication;
    /** 是否被动认证（IsPassive） */
    private boolean isPassive;
    /** 登录时是否禁止更换 HTTP Session ID */
    private boolean turnOffChangeSessionIdOnLogin;
    /** SAML 断言解密私钥 */
    private PrivateKey decryptionKey;
    /** AuthnRequest 签名密钥对 */
    private KeyPair signingKeyPair;
    /** 角色属性名集合 */
    private Set<String> roleAttributeNames;
    /** 角色映射 SPI 提供者 */
    private RoleMappingsProvider roleMappingsProvider;
    /** 主体名称提取策略 */
    private PrincipalNamePolicy principalNamePolicy = PrincipalNamePolicy.FROM_NAME_ID;
    /** 从属性提取主体名时使用的属性名 */
    private String principalAttributeName;
    /** 自定义登出页面 URL */
    private String logoutPage;
    /** 签名算法 */
    private SignatureAlgorithm signatureAlgorithm;
    /** 签名规范化方法 URI */
    private String signatureCanonicalizationMethod;
    /** 是否自动检测 Bearer-Only 请求 */
    private boolean autodetectBearerOnly;
    /** 是否保留断言的 DOM 形式 */
    private boolean keepDOMAssertion;

    @Override
    public boolean turnOffChangeSessionIdOnLogin() {
        return turnOffChangeSessionIdOnLogin;
    }

    public void setTurnOffChangeSessionIdOnLogin(boolean turnOffChangeSessionIdOnLogin) {
        this.turnOffChangeSessionIdOnLogin = turnOffChangeSessionIdOnLogin;
    }


    @Override
    public IDP getIDP() {
        return idp;
    }

    @Override
    public boolean isConfigured() {
        return configured;
    }

    @Override
    public SslRequired getSslRequired() {
        return sslRequired;
    }

    @Override
    public String getEntityID() {
        return entityID;
    }

    @Override
    public String getNameIDPolicyFormat() {
        return nameIDPolicyFormat;
    }

    @Override
    public boolean isForceAuthentication() {
        return forceAuthentication;
    }
    
   @Override
    public boolean isIsPassive() {
        return isPassive;
    }

    @Override
    public PrivateKey getDecryptionKey() {
        return decryptionKey;
    }

    @Override
    public KeyPair getSigningKeyPair() {
        return signingKeyPair;
    }

    @Override
    public Set<String> getRoleAttributeNames() {
        return roleAttributeNames;
    }

    @Override
    public RoleMappingsProvider getRoleMappingsProvider() {
        return this.roleMappingsProvider;
    }

    @Override
    public PrincipalNamePolicy getPrincipalNamePolicy() {
        return principalNamePolicy;
    }

    @Override
    public String getPrincipalAttributeName() {
        return principalAttributeName;
    }

    public void setIdp(IDP idp) {
        this.idp = idp;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public void setSslRequired(SslRequired sslRequired) {
        this.sslRequired = sslRequired;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public void setNameIDPolicyFormat(String nameIDPolicyFormat) {
        this.nameIDPolicyFormat = nameIDPolicyFormat;
    }

    public void setForceAuthentication(boolean forceAuthentication) {
        this.forceAuthentication = forceAuthentication;
    }
    
    public void setIsPassive(boolean isPassive){
        this.isPassive = isPassive;
    }

    public void setDecryptionKey(PrivateKey decryptionKey) {
        this.decryptionKey = decryptionKey;
    }

    public void setSigningKeyPair(KeyPair signingKeyPair) {
        this.signingKeyPair = signingKeyPair;
    }

    public void setRoleAttributeNames(Set<String> roleAttributeNames) {
        this.roleAttributeNames = roleAttributeNames;
    }

    public void setRoleMappingsProvider(final RoleMappingsProvider provider) {
        this.roleMappingsProvider = provider;
    }

    public void setPrincipalNamePolicy(PrincipalNamePolicy principalNamePolicy) {
        this.principalNamePolicy = principalNamePolicy;
    }

    public void setPrincipalAttributeName(String principalAttributeName) {
        this.principalAttributeName = principalAttributeName;
    }

    @Override
    public String getLogoutPage() {
        return logoutPage;
    }

    public void setLogoutPage(String logoutPage) {
        this.logoutPage = logoutPage;
    }

    @Override
    public String getSignatureCanonicalizationMethod() {
        return signatureCanonicalizationMethod;
    }

    public void setSignatureCanonicalizationMethod(String signatureCanonicalizationMethod) {
        this.signatureCanonicalizationMethod = signatureCanonicalizationMethod;
    }

    @Override
    public SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    @Override
    public boolean isAutodetectBearerOnly() {
        return autodetectBearerOnly;
    }

    public void setAutodetectBearerOnly(boolean autodetectBearerOnly) {
        this.autodetectBearerOnly = autodetectBearerOnly;
    }

    @Override
    public boolean isKeepDOMAssertion() {
        return keepDOMAssertion;
    }

    public void setKeepDOMAssertion(Boolean keepDOMAssertion) {
        this.keepDOMAssertion = keepDOMAssertion != null && keepDOMAssertion;
    }
}
