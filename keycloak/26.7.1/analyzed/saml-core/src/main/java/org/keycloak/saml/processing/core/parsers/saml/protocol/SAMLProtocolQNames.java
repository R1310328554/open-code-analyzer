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
package org.keycloak.saml.processing.core.parsers.saml.protocol;

import javax.xml.namespace.QName;

import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.processing.core.parsers.saml.assertion.SAMLAssertionQNames;
import org.keycloak.saml.processing.core.parsers.saml.xmldsig.XmlDSigQNames;
import org.keycloak.saml.processing.core.parsers.util.HasQName;

/**
 * SAML 协议解析器使用的 XML 元素与属性 QName 枚举。
 * <p>对应 saml-schema-protocol-2.0.xsd，并映射断言与 XML-DSig 命名空间的子元素。</p>
 * @author hmlnarik
 */
public enum SAMLProtocolQNames implements HasQName {

    /** Artifact 值。 */
    ARTIFACT("Artifact"),
    /** Artifact 解析请求。 */
    ARTIFACT_RESOLVE("ArtifactResolve"),
    /** Artifact 解析响应。 */
    ARTIFACT_RESPONSE("ArtifactResponse"),
    /** 断言 ID 查询请求。 */
    ASSERTION_ID_REQUEST("AssertionIDRequest"),
    /** 属性查询请求。 */
    ATTRIBUTE_QUERY("AttributeQuery"),
    /** 认证查询请求。 */
    AUTHN_QUERY("AuthnQuery"),
    /** 认证请求（AuthnRequest）。 */
    AUTHN_REQUEST("AuthnRequest"),
    /** 授权决策查询请求。 */
    AUTHZ_DECISION_QUERY("AuthzDecisionQuery"),
    /** 扩展容器。 */
    EXTENSIONS("Extensions"),
    /** 获取完整元数据引用。 */
    GET_COMPLETE("GetComplete"),
    /** IdP 列表条目。 */
    IDP_ENTRY("IDPEntry"),
    /** IdP 列表。 */
    IDP_LIST("IDPList"),
    /** 单点登出请求。 */
    LOGOUT_REQUEST("LogoutRequest"),
    /** 单点登出响应。 */
    LOGOUT_RESPONSE("LogoutResponse"),
    /** NameID 管理请求。 */
    MANAGE_NAMEID_REQUEST("ManageNameIDRequest"),
    /** NameID 管理响应。 */
    MANAGE_NAMEID_RESPONSE("ManageNameIDResponse"),
    /** NameID 映射请求。 */
    NAMEID_MAPPING_REQUEST("NameIDMappingRequest"),
    /** NameID 映射响应。 */
    NAMEID_MAPPING_RESPONSE("NameIDMappingResponse"),
    /** NameID 策略。 */
    NAMEID_POLICY("NameIDPolicy"),
    /** 新加密 NameID。 */
    NEW_ENCRYPTEDID("NewEncryptedID"),
    /** 新 NameID。 */
    NEWID("NewID"),
    /** 请求的认证上下文。 */
    REQUESTED_AUTHN_CONTEXT("RequestedAuthnContext"),
    /** 请求方标识。 */
    REQUESTERID("RequesterID"),
    /** SAML 响应。 */
    RESPONSE("Response"),
    /** IdP 范围限定。 */
    SCOPING("Scoping"),
    /** 会话索引。 */
    SESSION_INDEX("SessionIndex"),
    /** 状态码。 */
    STATUS_CODE("StatusCode"),
    /** 状态详情。 */
    STATUS_DETAIL("StatusDetail"),
    /** 状态消息。 */
    STATUS_MESSAGE("StatusMessage"),
    /** 响应状态。 */
    STATUS("Status"),
    /** 主体查询抽象类型。 */
    SUBJECT_QUERY("SubjectQuery"),
    /** 终止标识。 */
    TERMINATE("Terminate"),

    // 协议消息属性名
    /** AllowCreate 属性。 */
    ATTR_ALLOW_CREATE(null, "AllowCreate"),
    /** AssertionConsumerServiceURL 属性。 */
    ATTR_ASSERTION_CONSUMER_SERVICE_URL(null, "AssertionConsumerServiceURL"),
    /** AssertionConsumerServiceIndex 属性。 */
    ATTR_ASSERTION_CONSUMER_SERVICE_INDEX(null, "AssertionConsumerServiceIndex"),
    /** AttributeConsumingServiceIndex 属性。 */
    ATTR_ATTRIBUTE_CONSUMING_SERVICE_INDEX(null, "AttributeConsumingServiceIndex"),
    /** Comparison 属性。 */
    ATTR_COMPARISON(null, "Comparison"),
    /** Consent 属性。 */
    ATTR_CONSENT(null, "Consent"),
    /** Destination 属性。 */
    ATTR_DESTINATION(null, "Destination"),
    /** ForceAuthn 属性。 */
    ATTR_FORCE_AUTHN(null, "ForceAuthn"),
    /** Format 属性。 */
    ATTR_FORMAT(null, "Format"),
    /** ID 属性。 */
    ATTR_ID(null, "ID"),
    /** InResponseTo 属性。 */
    ATTR_IN_RESPONSE_TO(null, "InResponseTo"),
    /** IsPassive 属性。 */
    ATTR_IS_PASSIVE(null, "IsPassive"),
    /** IssueInstant 属性。 */
    ATTR_ISSUE_INSTANT(null, "IssueInstant"),
    /** NotBefore 属性。 */
    ATTR_NOT_BEFORE(null, "NotBefore"),
    /** NotOnOrAfter 属性。 */
    ATTR_NOT_ON_OR_AFTER(null, "NotOnOrAfter"),
    /** ProtocolBinding 属性。 */
    ATTR_PROTOCOL_BINDING(null, "ProtocolBinding"),
    /** ProviderName 属性。 */
    ATTR_PROVIDER_NAME(null, "ProviderName"),
    /** Reason 属性。 */
    ATTR_REASON(null, "Reason"),
    /** Value 属性。 */
    ATTR_VALUE(null, "Value"),
    /** Version 属性。 */
    ATTR_VERSION(null, "Version"),

    // 其他命名空间中可作为协议元素直接子元素的节点
    /** 断言命名空间 Attribute 子元素。 */
    ATTRIBUTE(SAMLAssertionQNames.ATTRIBUTE),
    /** 断言命名空间 Assertion 子元素。 */
    ASSERTION(SAMLAssertionQNames.ASSERTION),
    /** 认证上下文类引用。 */
    AUTHN_CONTEXT_CLASS_REF(SAMLAssertionQNames.AUTHN_CONTEXT_CLASS_REF),
    /** 认证上下文声明引用。 */
    AUTHN_CONTEXT_DECL_REF(SAMLAssertionQNames.AUTHN_CONTEXT_DECL_REF),
    /** BaseID 子元素。 */
    BASEID(SAMLAssertionQNames.BASEID),
    /** Conditions 子元素。 */
    CONDITIONS(SAMLAssertionQNames.CONDITIONS),
    /** 加密断言。 */
    ENCRYPTED_ASSERTION(SAMLAssertionQNames.ENCRYPTED_ASSERTION),
    /** Issuer 子元素。 */
    ISSUER(SAMLAssertionQNames.ISSUER),
    /** NameID 子元素。 */
    NAMEID(SAMLAssertionQNames.NAMEID),
    /** XML 数字签名。 */
    SIGNATURE(XmlDSigQNames.SIGNATURE),
    /** 加密 NameID。 */
    ENCRYPTED_ID(SAMLAssertionQNames.ENCRYPTED_ID),
    /** Subject 子元素。 */
    SUBJECT(SAMLAssertionQNames.SUBJECT),

    /** 未知元素占位符。 */
    UNKNOWN_ELEMENT("")
    ;

    /** 对应的 XML QName。 */
    private final QName qName;

    SAMLProtocolQNames(String localName) {
        this(JBossSAMLURIConstants.PROTOCOL_NSURI, localName);
    }

    SAMLProtocolQNames(HasQName source) {
        this.qName = source.getQName();
    }

    SAMLProtocolQNames(JBossSAMLURIConstants nsUri, String localName) {
        this.qName = new QName(nsUri == null ? null : nsUri.get(), localName);
    }

    /** 返回枚举常量对应的 QName。 */
    @Override
    public QName getQName() {
        return qName;
    }

    /** 返回带指定前缀的 QName。 */
    public QName getQName(String prefix) {
        return new QName(this.qName.getNamespaceURI(), this.qName.getLocalPart(), prefix);
    }
}
