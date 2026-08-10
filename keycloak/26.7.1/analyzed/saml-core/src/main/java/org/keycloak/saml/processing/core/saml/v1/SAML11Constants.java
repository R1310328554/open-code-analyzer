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
package org.keycloak.saml.processing.core.saml.v1;

/**
 * SAML 1.1 规范中使用的 XML 元素名与属性名常量。
 * <p>涵盖断言（assertion）与协议（protocol）命名空间下的本地名。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public interface SAML11Constants {

    /** Action 元素本地名。 */
    String ACTION = "Action";

    /** AssertionID 属性名。 */
    String ASSERTIONID = "AssertionID";

    /** SAML 1.1 断言命名空间 URI。 */
    String ASSERTION_11_NSURI = "urn:oasis:names:tc:SAML:1.0:assertion";

    /** AssertionArtifact 元素本地名。 */
    String ASSERTION_ARTIFACT = "AssertionArtifact";

    /** AssertionIDReference 元素本地名。 */
    String ASSERTION_ID_REF = "AssertionIDReference";

    /** AttributeQuery 请求类型。 */
    String ATTRIBUTE_QUERY = "AttributeQuery";

    /** AttributeName 属性名。 */
    String ATTRIBUTE_NAME = "AttributeName";

    /** AttributeNamespace 属性名。 */
    String ATTRIBUTE_NAMESPACE = "AttributeNamespace";

    /** AttributeStatement 语句类型。 */
    String ATTRIBUTE_STATEMENT = "AttributeStatement";

    /** AudienceRestrictionCondition 条件类型。 */
    String AUDIENCE_RESTRICTION_CONDITION = "AudienceRestrictionCondition";

    /** AuthenticationInstant 属性名。 */
    String AUTHENTICATION_INSTANT = "AuthenticationInstant";

    /** AuthenticationMethod 属性名。 */
    String AUTHENTICATION_METHOD = "AuthenticationMethod";

    /** AuthenticationQuery 请求类型。 */
    String AUTHENTICATION_QUERY = "AuthenticationQuery";

    /** AuthenticationStatement 语句类型。 */
    String AUTHENTICATION_STATEMENT = "AuthenticationStatement";

    /** AuthorityBinding 元素本地名。 */
    String AUTHORITY_BINDING = "AuthorityBinding";

    /** AuthorityKind 属性名。 */
    String AUTHORITY_KIND = "AuthorityKind";

    /** AuthorizationDecisionQuery 请求类型。 */
    String AUTHORIZATION_DECISION_QUERY = "AuthorizationDecisionQuery";

    /** AuthorizationDecisionStatement 语句类型。 */
    String AUTHORIZATION_DECISION_STATEMENT = "AuthorizationDecisionStatement";

    /** Binding 属性名。 */
    String BINDING = "Binding";

    /** ConfirmationMethod 元素本地名。 */
    String CONFIRMATION_METHOD = "ConfirmationMethod";

    /** Decision 属性名（Permit/Deny/Indeterminate）。 */
    String DECISION = "Decision";

    /** DNSAddress 属性名。 */
    String DNS_ADDRESS = "DNSAddress";

    /** Evidence 元素本地名。 */
    String EVIDENCE = "Evidence";

    /** Format 属性名（NameIdentifier 格式 URI）。 */
    String FORMAT = "Format";

    /** InResponseTo 属性名。 */
    String IN_RESPONSE_TO = "InResponseTo";

    /** IPAddress 属性名。 */
    String IP_ADDRESS = "IPAddress";

    /** Issuer 属性名。 */
    String ISSUER = "Issuer";

    /** IssueInstant 属性名。 */
    String ISSUE_INSTANT = "IssueInstant";

    /** Location 属性名。 */
    String LOCATION = "Location";

    /** MajorVersion 属性名。 */
    String MAJOR_VERSION = "MajorVersion";

    /** MinorVersion 属性名。 */
    String MINOR_VERSION = "MinorVersion";

    /** NameIdentifier 元素本地名。 */
    String NAME_IDENTIFIER = "NameIdentifier";

    /** NameQualifier 属性名。 */
    String NAME_QUALIFIER = "NameQualifier";

    /** Namespace 属性名（Action 等）。 */
    String NAMESPACE = "Namespace";

    /** SAML 1.1 协议命名空间 URI。 */
    String PROTOCOL_11_NSURI = "urn:oasis:names:tc:SAML:1.0:protocol";

    /** Recipient 属性名。 */
    String RECIPIENT = "Recipient";

    /** Request 根元素本地名。 */
    String REQUEST = "Request";

    /** RequestID 属性名。 */
    String REQUEST_ID = "RequestID";

    /** Resource 属性/元素本地名。 */
    String RESOURCE = "Resource";

    /** Response 根元素本地名。 */
    String RESPONSE = "Response";

    /** ResponseID 属性名。 */
    String RESPONSE_ID = "ResponseID";

    /** Status 元素本地名。 */
    String STATUS = "Status";

    /** StatusCode 元素本地名。 */
    String STATUS_CODE = "StatusCode";

    /** StatusDetail 元素本地名。 */
    String STATUS_DETAIL = "StatusDetail";

    /** StatusMessage 元素本地名。 */
    String STATUS_MSG = "StatusMessage";

    /** Value 属性名（StatusCode 等）。 */
    String VALUE = "Value";
}
