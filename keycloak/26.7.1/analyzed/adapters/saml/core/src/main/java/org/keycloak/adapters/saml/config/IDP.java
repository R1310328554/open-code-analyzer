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

package org.keycloak.adapters.saml.config;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.keycloak.adapters.cloned.AdapterHttpClientConfig;

/**
 * {@code keycloak-saml.xml} 中 IdP 段的配置模型（可序列化 POJO）。
 *
 * <p>包含 SSO/SLO 端点、签名策略、密钥列表、元数据 URL、时钟偏移及 HTTP 客户端设置。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class IDP implements Serializable {
    /** IdP 单点登录服务 XML 配置。 */
    public static class SingleSignOnService implements Serializable {
        /** 是否对 AuthnRequest 签名（可覆盖全局 signaturesRequired）。 */
        private Boolean signRequest;
        /** 是否校验 IdP 响应整体签名。 */
        private Boolean validateResponseSignature;
        /** 出站请求绑定（POST/REDIRECT）。 */
        private String requestBinding;
        /** 期望的响应绑定类型。 */
        private String responseBinding;
        /** SSO 端点 URL。 */
        private String bindingUrl;
        /** 指定的断言消费者服务 URL。 */
        private String assertionConsumerServiceUrl;
        /** 是否校验断言级签名。 */
        private Boolean validateAssertionSignature;
        /** 全局是否要求签名（各布尔项未显式设置时的默认值）。 */
        private boolean signaturesRequired = false;

        public boolean isSignRequest() {
            return signRequest == null ? signaturesRequired : signRequest;
        }

        public void setSignRequest(Boolean signRequest) {
            this.signRequest = signRequest;
        }

        public boolean isValidateResponseSignature() {
            return validateResponseSignature == null ? signaturesRequired : validateResponseSignature;
        }

        public void setValidateResponseSignature(Boolean validateResponseSignature) {
            this.validateResponseSignature = validateResponseSignature;
        }

        public boolean isValidateAssertionSignature() {
            return validateAssertionSignature == null ? false : validateAssertionSignature;
        }

        public void setValidateAssertionSignature(Boolean validateAssertionSignature) {
            this.validateAssertionSignature = validateAssertionSignature;
        }

        public String getRequestBinding() {
            return requestBinding;
        }

        public void setRequestBinding(String requestBinding) {
            this.requestBinding = requestBinding;
        }

        public String getResponseBinding() {
            return responseBinding;
        }

        public void setResponseBinding(String responseBinding) {
            this.responseBinding = responseBinding;
        }

        public String getBindingUrl() {
            return bindingUrl;
        }

        public void setBindingUrl(String bindingUrl) {
            this.bindingUrl = bindingUrl;
        }

        public String getAssertionConsumerServiceUrl() {
            return assertionConsumerServiceUrl;
        }

        public void setAssertionConsumerServiceUrl(String assertionConsumerServiceUrl) {
            this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        }

        private void setSignaturesRequired(boolean signaturesRequired) {
            this.signaturesRequired = signaturesRequired;
        }
    }

    /** IdP 单点登出服务 XML 配置。 */
    public static class SingleLogoutService implements Serializable {
        private Boolean signRequest;
        private Boolean signResponse;
        private Boolean validateRequestSignature;
        private Boolean validateResponseSignature;
        private String requestBinding;
        private String responseBinding;
        /** POST 绑定 SLO URL。 */
        private String postBindingUrl;
        /** Redirect 绑定 SLO URL。 */
        private String redirectBindingUrl;
        private boolean signaturesRequired = false;

        public boolean isSignRequest() {
            return signRequest == null ? signaturesRequired : signRequest;
        }

        public void setSignRequest(Boolean signRequest) {
            this.signRequest = signRequest;
        }

        public boolean isSignResponse() {
            return signResponse == null ? signaturesRequired : signResponse;
        }

        public void setSignResponse(Boolean signResponse) {
            this.signResponse = signResponse;
        }

        public boolean isValidateRequestSignature() {
            return validateRequestSignature == null ? signaturesRequired : validateRequestSignature;
        }

        public void setValidateRequestSignature(Boolean validateRequestSignature) {
            this.validateRequestSignature = validateRequestSignature;
        }

        public boolean isValidateResponseSignature() {
            return validateResponseSignature == null ? signaturesRequired : validateResponseSignature;
        }

        public void setValidateResponseSignature(Boolean validateResponseSignature) {
            this.validateResponseSignature = validateResponseSignature;
        }

        public String getRequestBinding() {
            return requestBinding;
        }

        public void setRequestBinding(String requestBinding) {
            this.requestBinding = requestBinding;
        }

        public String getResponseBinding() {
            return responseBinding;
        }

        public void setResponseBinding(String responseBinding) {
            this.responseBinding = responseBinding;
        }

        public String getPostBindingUrl() {
            return postBindingUrl;
        }

        public void setPostBindingUrl(String postBindingUrl) {
            this.postBindingUrl = postBindingUrl;
        }

        public String getRedirectBindingUrl() {
            return redirectBindingUrl;
        }

        public void setRedirectBindingUrl(String redirectBindingUrl) {
            this.redirectBindingUrl = redirectBindingUrl;
        }

        private void setSignaturesRequired(boolean signaturesRequired) {
            this.signaturesRequired = signaturesRequired;
        }
    }

    /** 与 IdP 通信的 HTTP 客户端配置（信任库、连接池、代理、超时等）。 */
    public static class HttpClientConfig implements AdapterHttpClientConfig {

        private String truststore;
        private String truststorePassword;
        private String clientKeystore;
        private String clientKeystorePassword;
        /** 是否允许任意主机名（跳过主机名校验）。 */
        private boolean allowAnyHostname;
        /** 是否禁用信任管理器（仅测试环境）。 */
        private boolean disableTrustManager;
        private int connectionPoolSize;
        private String proxyUrl;
        private long socketTimeout;
        private long connectionTimeout;
        private long connectionTTL;

        @Override
        public String getTruststore() {
            return truststore;
        }

        public void setTruststore(String truststore) {
            this.truststore = truststore;
        }

        @Override
        public String getTruststorePassword() {
            return truststorePassword;
        }

        public void setTruststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
        }

        @Override
        public String getClientKeystore() {
            return clientKeystore;
        }

        public void setClientKeystore(String clientKeystore) {
            this.clientKeystore = clientKeystore;
        }

        @Override
        public String getClientKeystorePassword() {
            return clientKeystorePassword;
        }

        public void setClientKeystorePassword(String clientKeystorePassword) {
            this.clientKeystorePassword = clientKeystorePassword;
        }

        @Override
        public boolean isAllowAnyHostname() {
            return allowAnyHostname;
        }

        public void setAllowAnyHostname(boolean allowAnyHostname) {
            this.allowAnyHostname = allowAnyHostname;
        }

        @Override
        public boolean isDisableTrustManager() {
            return disableTrustManager;
        }

        public void setDisableTrustManager(boolean disableTrustManager) {
            this.disableTrustManager = disableTrustManager;
        }

        @Override
        public int getConnectionPoolSize() {
            return connectionPoolSize;
        }

        public void setConnectionPoolSize(int connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
        }

        @Override
        public String getProxyUrl() {
            return proxyUrl;
        }

        @Override
        public long getSocketTimeout() {
            return socketTimeout;
        }

        public void setSocketTimeout(long socketTimeout) {
            this.socketTimeout = socketTimeout;
        }

        @Override
        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        @Override
        public long getConnectionTTL() {
            return connectionTTL;
        }

        public void setConnectionTTL(long connectionTTL) {
            this.connectionTTL = connectionTTL;
        }

        public void setProxyUrl(String proxyUrl) {
            this.proxyUrl = proxyUrl;
        }
    }

    /** IdP 实体标识符。 */
    private String entityID;
    /** 签名算法名称（如 RSA_SHA256）。 */
    private String signatureAlgorithm;
    /** 签名规范化方法 URI。 */
    private String signatureCanonicalizationMethod;
    private SingleSignOnService singleSignOnService;
    private SingleLogoutService singleLogoutService;
    /** IdP 验签/加密相关密钥列表。 */
    private List<Key> keys;
    private AdapterHttpClientConfig httpClientConfig = new HttpClientConfig();
    /** 是否全局要求 SAML 消息签名。 */
    private boolean signaturesRequired = false;
    /** IdP SAML 元数据描述符 URL（用于密钥轮询）。 */
    private String metadataUrl;
    /** 允许的 IdP/SP 时钟偏差数值。 */
    private Integer allowedClockSkew;
    /** 时钟偏差的时间单位。 */
    private TimeUnit allowedClockSkewUnit;

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public SingleSignOnService getSingleSignOnService() {
        return singleSignOnService;
    }

    public void setSingleSignOnService(SingleSignOnService singleSignOnService) {
        this.singleSignOnService = singleSignOnService;
        if (singleSignOnService != null) {
            singleSignOnService.setSignaturesRequired(signaturesRequired);
        }
    }

    public SingleLogoutService getSingleLogoutService() {
        return singleLogoutService;
    }

    public void setSingleLogoutService(SingleLogoutService singleLogoutService) {
        this.singleLogoutService = singleLogoutService;
        if (singleLogoutService != null) {
            singleLogoutService.setSignaturesRequired(signaturesRequired);
        }
    }

    public List<Key> getKeys() {
        return keys;
    }

    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getSignatureCanonicalizationMethod() {
        return signatureCanonicalizationMethod;
    }

    public void setSignatureCanonicalizationMethod(String signatureCanonicalizationMethod) {
        this.signatureCanonicalizationMethod = signatureCanonicalizationMethod;
    }

    public AdapterHttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    public void setHttpClientConfig(AdapterHttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    public boolean isSignaturesRequired() {
        return signaturesRequired;
    }

    public void setSignaturesRequired(boolean signaturesRequired) {
        this.signaturesRequired = signaturesRequired;
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    public Integer getAllowedClockSkew() {
        return allowedClockSkew;
    }

    public void setAllowedClockSkew(Integer allowedClockSkew) {
        this.allowedClockSkew = allowedClockSkew;
    }

    public TimeUnit getAllowedClockSkewUnit() {
        return allowedClockSkewUnit;
    }

    public void setAllowedClockSkewUnit(TimeUnit allowedClockSkewUnit) {
        this.allowedClockSkewUnit = allowedClockSkewUnit;
    }
}
