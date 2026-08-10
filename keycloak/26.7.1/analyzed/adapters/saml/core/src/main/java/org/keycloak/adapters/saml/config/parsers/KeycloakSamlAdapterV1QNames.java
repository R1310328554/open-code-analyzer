/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.adapters.saml.config.parsers;

import javax.xml.namespace.QName;

import org.keycloak.saml.processing.core.parsers.util.HasQName;

/**
 * Keycloak SAML 适配器 V1 配置 schema 的元素与属性 QName 枚举。
 *
 * <p>命名空间 URI 为 {@link #NS_URI}；枚举值涵盖 IdP/SP、密钥、角色映射及 HTTP 客户端等配置节点。</p>
 *
 * @author hmlnarik
 */
public enum KeycloakSamlAdapterV1QNames implements HasQName {

    /** 允许的 SAML 断言时钟偏差。 */
    ALLOWED_CLOCK_SKEW("AllowedClockSkew"),
    /** SAML 属性名（角色映射等）。 */
    ATTRIBUTE("Attribute"),
    /** KeyStore 内证书条目。 */
    CERTIFICATE("Certificate"),
    /** PEM 格式证书文本。 */
    CERTIFICATE_PEM("CertificatePem"),
    /** IdP HTTP 客户端 TLS/连接配置。 */
    HTTP_CLIENT("HttpClient"),
    /** 身份提供者根元素。 */
    IDP("IDP"),
    /** 单个签名/加密密钥。 */
    KEY("Key"),
    /** 适配器配置文档根元素。 */
    KEYCLOAK_SAML_ADAPTER("keycloak-saml-adapter"),
    /** 密钥列表容器。 */
    KEYS("Keys"),
    /** Java KeyStore 配置。 */
    KEY_STORE("KeyStore"),
    /** 主体名称映射策略。 */
    PRINCIPAL_NAME_MAPPING("PrincipalNameMapping"),
    /** KeyStore 私钥条目。 */
    PRIVATE_KEY("PrivateKey"),
    /** PEM 格式私钥文本。 */
    PRIVATE_KEY_PEM("PrivateKeyPem"),
    /** 键值对属性（角色映射提供者等）。 */
    PROPERTY("Property"),
    /** PEM 格式公钥文本。 */
    PUBLIC_KEY_PEM("PublicKeyPem"),
    /** 角色标识符（SAML Attribute 名）集合。 */
    ROLE_IDENTIFIERS("RoleIdentifiers"),
    /** 角色映射 SPI 提供者配置。 */
    ROLE_MAPPINGS_PROVIDER("RoleMappingsProvider"),
    /** 单点登出服务端点。 */
    SINGLE_LOGOUT_SERVICE("SingleLogoutService"),
    /** 单点登录服务端点。 */
    SINGLE_SIGN_ON_SERVICE("SingleSignOnService"),
    /** 服务提供者根元素。 */
    SP("SP"),

    /** KeyStore/证书条目别名。 */
    ATTR_ALIAS(null, "alias"),
    ATTR_ALLOW_ANY_HOSTNAME(null, "allowAnyHostname"),
    ATTR_ASSERTION_CONSUMER_SERVICE_URL(null, "assertionConsumerServiceUrl"),
    ATTR_ATTRIBUTE(null, "attribute"),
    ATTR_AUTODETECT_BEARER_ONLY(null, "autodetectBearerOnly"),
    ATTR_BINDING_URL(null, "bindingUrl"),
    ATTR_CLIENT_KEYSTORE(null, "clientKeystore"),
    ATTR_CLIENT_KEYSTORE_PASSWORD(null, "clientKeystorePassword"),
    ATTR_CONNECTION_POOL_SIZE(null, "connectionPoolSize"),
    ATTR_DISABLE_TRUST_MANAGER(null, "disableTrustManager"),
    ATTR_ENCRYPTION(null, "encryption"),
    ATTR_ENTITY_ID(null, "entityID"),
    ATTR_FILE(null, "file"),
    ATTR_FORCE_AUTHENTICATION(null, "forceAuthentication"),
    ATTR_ID(null, "id"),
    ATTR_IS_PASSIVE(null, "isPassive"),
    ATTR_LOGOUT_PAGE(null, "logoutPage"),
    ATTR_METADATA_URL(null, "metadataUrl"),
    ATTR_NAME(null, "name"),
    ATTR_NAME_ID_POLICY_FORMAT(null, "nameIDPolicyFormat"),
    ATTR_PASSWORD(null, "password"),
    ATTR_POLICY(null, "policy"),
    ATTR_POST_BINDING_URL(null, "postBindingUrl"),
    ATTR_PROXY_URL(null, "proxyUrl"),
    ATTR_REDIRECT_BINDING_URL(null, "redirectBindingUrl"),
    ATTR_REQUEST_BINDING(null, "requestBinding"),
    ATTR_RESOURCE(null, "resource"),
    ATTR_RESPONSE_BINDING(null, "responseBinding"),
    ATTR_SIGNATURES_REQUIRED(null, "signaturesRequired"),
    ATTR_SIGNATURE_ALGORITHM(null, "signatureAlgorithm"),
    ATTR_SIGNATURE_CANONICALIZATION_METHOD(null, "signatureCanonicalizationMethod"),
    ATTR_SIGNING(null, "signing"),
    ATTR_SIGN_REQUEST(null, "signRequest"),
    ATTR_SIGN_RESPONSE(null, "signResponse"),
    ATTR_SSL_POLICY(null, "sslPolicy"),
    ATTR_TRUSTSTORE(null, "truststore"),
    ATTR_TRUSTSTORE_PASSWORD(null, "truststorePassword"),
    ATTR_TURN_OFF_CHANGE_SESSSION_ID_ON_LOGIN(null, "turnOffChangeSessionIdOnLogin"),
    ATTR_TYPE(null, "type"),
    ATTR_UNIT(null, "unit"),
    ATTR_VALIDATE_ASSERTION_SIGNATURE(null, "validateAssertionSignature"),
    ATTR_VALIDATE_REQUEST_SIGNATURE(null, "validateRequestSignature"),
    ATTR_VALIDATE_RESPONSE_SIGNATURE(null, "validateResponseSignature"),
    ATTR_VALUE(null, "value"),
    ATTR_KEEP_DOM_ASSERTION(null, "keepDOMAssertion"),
    ATTR_SOCKET_TIMEOUT(null, "socketTimeout"),
    ATTR_CONNECTION_TIMEOUT(null, "connectionTimeout"),
    ATTR_CONNECTION_TTL(null, "connectionTtl"),

    UNKNOWN_ELEMENT("");

    /** V1 适配器配置 XML 的标准命名空间 URI。 */
    public static final String NS_URI = "urn:keycloak:saml:adapter";

    /** 元素或属性对应的 {@link QName}。 */
    private final QName qName;

    /** 使用标准命名空间构造元素 QName。 */
    private KeycloakSamlAdapterV1QNames(String localName) {
        this(NS_URI, localName);
    }

    /** 从已有 {@link HasQName} 复制 QName。 */
    private KeycloakSamlAdapterV1QNames(HasQName source) {
        this.qName = source.getQName();
    }

    /** 显式指定命名空间 URI 与 localName。 */
    private KeycloakSamlAdapterV1QNames(String nsUri, String localName) {
        this.qName = new QName(nsUri == null ? null : nsUri, localName);
    }

    /** @return 本枚举项的 {@link QName} */
    @Override
    public QName getQName() {
        return qName;
    }

    /** @return 带指定前缀的 {@link QName} 副本 */
    public QName getQName(String prefix) {
        return new QName(this.qName.getNamespaceURI(), this.qName.getLocalPart(), prefix);
    }
}
