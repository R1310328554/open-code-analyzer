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
package org.keycloak.saml.processing.core.saml.v1.writers;

import java.net.URI;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.keycloak.dom.saml.v1.assertion.SAML11ActionType;
import org.keycloak.dom.saml.v1.assertion.SAML11AdviceType;
import org.keycloak.dom.saml.v1.assertion.SAML11AssertionType;
import org.keycloak.dom.saml.v1.assertion.SAML11AttributeStatementType;
import org.keycloak.dom.saml.v1.assertion.SAML11AttributeType;
import org.keycloak.dom.saml.v1.assertion.SAML11AudienceRestrictionCondition;
import org.keycloak.dom.saml.v1.assertion.SAML11AuthenticationStatementType;
import org.keycloak.dom.saml.v1.assertion.SAML11AuthorityBindingType;
import org.keycloak.dom.saml.v1.assertion.SAML11AuthorizationDecisionStatementType;
import org.keycloak.dom.saml.v1.assertion.SAML11ConditionAbstractType;
import org.keycloak.dom.saml.v1.assertion.SAML11ConditionsType;
import org.keycloak.dom.saml.v1.assertion.SAML11EvidenceType;
import org.keycloak.dom.saml.v1.assertion.SAML11NameIdentifierType;
import org.keycloak.dom.saml.v1.assertion.SAML11StatementAbstractType;
import org.keycloak.dom.saml.v1.assertion.SAML11SubjectConfirmationType;
import org.keycloak.dom.saml.v1.assertion.SAML11SubjectLocalityType;
import org.keycloak.dom.saml.v1.assertion.SAML11SubjectStatementType;
import org.keycloak.dom.saml.v1.assertion.SAML11SubjectType;
import org.keycloak.dom.saml.v1.assertion.SAML11SubjectType.SAML11SubjectTypeChoice;
import org.keycloak.dom.saml.v2.assertion.StatementAbstractType;
import org.keycloak.dom.saml.v2.metadata.LocalizedNameType;
import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.StaxUtil;
import org.keycloak.saml.common.util.StringUtil;
import org.keycloak.saml.processing.core.saml.v1.SAML11Constants;

import org.w3c.dom.Element;

/**
 * 将 SAML 1.1 断言对象序列化为 XML 流。
 * <p>支持认证、属性、授权决策等语句及条件、主题、签名等子结构。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since June 24, 2011
 */
public class SAML11AssertionWriter extends BaseSAML11Writer {

    /** 断言命名空间 URI。 */
    String ns = SAML11Constants.ASSERTION_11_NSURI;

    /**
     * 使用给定 StAX 写入器构造断言写入器。
     *
     * @param writer XML 流写入器
     */
    public SAML11AssertionWriter(XMLStreamWriter writer) {
        super(writer);
    }

    /**
     * 将 {@code SAML11AssertionType} 写入输出流。
     *
     * @param assertion 待序列化的断言对象
     *
     * @throws ProcessingException 写入失败时抛出
     */
    public void write(SAML11AssertionType assertion) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, JBossSAMLConstants.ASSERTION.get(), ns);
        StaxUtil.writeNameSpace(writer, ASSERTION_PREFIX, ns);
        StaxUtil.writeDefaultNameSpace(writer, ns);

        // 写入根元素属性
        StaxUtil.writeAttribute(writer, SAML11Constants.ASSERTIONID, assertion.getID());
        StaxUtil.writeAttribute(writer, SAML11Constants.MAJOR_VERSION, assertion.getMajorVersion() + "");
        StaxUtil.writeAttribute(writer, SAML11Constants.MINOR_VERSION, assertion.getMinorVersion() + "");
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ISSUE_INSTANT.get(), assertion.getIssueInstant().toString());

        String issuer = assertion.getIssuer();
        if (issuer != null) {
            StaxUtil.writeAttribute(writer, SAML11Constants.ISSUER, issuer);
        }

        SAML11ConditionsType conditions = assertion.getConditions();
        if (conditions != null) {
            StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, JBossSAMLConstants.CONDITIONS.get(), ns);

            StaxUtil.writeAttribute(writer, JBossSAMLConstants.NOT_BEFORE.get(), conditions.getNotBefore().toString());
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.NOT_ON_OR_AFTER.get(), conditions.getNotOnOrAfter().toString());

            List<SAML11ConditionAbstractType> typeOfConditions = conditions.get();
            if (typeOfConditions != null) {
                for (SAML11ConditionAbstractType typeCondition : typeOfConditions) {
                    if (typeCondition instanceof SAML11AudienceRestrictionCondition) {
                        SAML11AudienceRestrictionCondition art = (SAML11AudienceRestrictionCondition) typeCondition;
                        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, SAML11Constants.AUDIENCE_RESTRICTION_CONDITION, ns);
                        List<URI> audiences = art.get();
                        if (audiences != null) {
                            for (URI audience : audiences) {
                                StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, JBossSAMLConstants.AUDIENCE.get(), ns);
                                StaxUtil.writeCharacters(writer, audience.toString());
                                StaxUtil.writeEndElement(writer);
                            }
                        }

                        StaxUtil.writeEndElement(writer);
                    }
                }
            }

            StaxUtil.writeEndElement(writer);
        }

        SAML11AdviceType advice = assertion.getAdvice();
        if (advice != null)
            throw logger.notImplementedYet("Advice");

        List<SAML11StatementAbstractType> statements = assertion.getStatements();
        if (statements != null) {
            for (SAML11StatementAbstractType statement : statements) {
                if (statement instanceof SAML11AuthenticationStatementType) {
                    write((SAML11AuthenticationStatementType) statement);
                } else if (statement instanceof SAML11AttributeStatementType) {
                    write((SAML11AttributeStatementType) statement);
                } else if (statement instanceof SAML11AuthorizationDecisionStatementType) {
                    write((SAML11AuthorizationDecisionStatementType) statement);
                } else if (statement instanceof SAML11SubjectStatementType) {
                    write((SAML11SubjectStatementType) statement);
                } else
                    throw logger.writerUnknownTypeError(statement.getClass().getName());
            }
        }

        Element sig = assertion.getSignature();
        if (sig != null)
            StaxUtil.writeDOMElement(writer, sig);

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /**
     * 写入 {@code StatementAbstractType}（当前未实现）。
     *
     * @param statement 语句对象
     *
     * @throws ProcessingException 未实现时抛出
     */
    public void write(StatementAbstractType statement) throws ProcessingException {
        throw logger.notImplementedYet("StatementAbstractType");
    }

    /** 写入 {@code SAML11SubjectStatementType}（当前未实现）。 */
    public void write(SAML11SubjectStatementType statement) throws ProcessingException {
        throw logger.notImplementedYet("SAML11SubjectStatementType");
    }

    /** 写入 {@code SAML11AttributeStatementType} 及其主题与属性列表。 */
    public void write(SAML11AttributeStatementType statement) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, JBossSAMLConstants.ATTRIBUTE_STATEMENT.get(),
                SAML11Constants.ASSERTION_11_NSURI);

        SAML11SubjectType subject = statement.getSubject();
        if (subject != null)
            write(subject);

        List<SAML11AttributeType> attributes = statement.get();
        if (attributes != null) {
            for (SAML11AttributeType attr : attributes) {
                write(attr);
            }
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /**
     * 将认证语句 {@code AuthnStatementType} 写入输出流。
     *
     * @param authnStatement 认证语句对象
     *
     * @throws ProcessingException 写入失败时抛出
     */
    public void write(SAML11AuthenticationStatementType authnStatement) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, SAML11Constants.AUTHENTICATION_STATEMENT,
                SAML11Constants.ASSERTION_11_NSURI);

        XMLGregorianCalendar authnInstant = authnStatement.getAuthenticationInstant();
        if (authnInstant != null) {
            StaxUtil.writeAttribute(writer, SAML11Constants.AUTHENTICATION_INSTANT, authnInstant.toString());
        }

        URI authMethod = authnStatement.getAuthenticationMethod();
        if (authMethod != null) {
            StaxUtil.writeAttribute(writer, SAML11Constants.AUTHENTICATION_METHOD, authMethod.toString());
        }

        SAML11SubjectType subject = authnStatement.getSubject();
        if (subject != null)
            write(subject);

        SAML11SubjectLocalityType locality = authnStatement.getSubjectLocality();
        if (locality != null)
            write(locality);

        List<SAML11AuthorityBindingType> authorities = authnStatement.getAuthorityBindingType();
        for (SAML11AuthorityBindingType authority : authorities) {
            write(authority);
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /** 写入 AuthorityBinding 元素。 */
    public void write(SAML11AuthorityBindingType authority) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, SAML11Constants.AUTHORITY_BINDING,
                SAML11Constants.ASSERTION_11_NSURI);

        QName authorityKind = authority.getAuthorityKind();
        StaxUtil.writeAttribute(writer, SAML11Constants.AUTHORITY_KIND, authorityKind);

        String binding = authority.getBinding().toString();
        StaxUtil.writeAttribute(writer, SAML11Constants.BINDING, binding);

        String location = authority.getLocation().toString();
        StaxUtil.writeAttribute(writer, SAML11Constants.LOCATION, location);

        StaxUtil.writeEndElement(writer);
    }

    /** 写入 SubjectLocality 元素（IP/DNS 地址）。 */
    public void write(SAML11SubjectLocalityType locality) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, JBossSAMLConstants.SUBJECT_LOCALITY.get(),
                SAML11Constants.ASSERTION_11_NSURI);
        String ip = locality.getIpAddress();
        if (StringUtil.isNotNull(ip)) {
            StaxUtil.writeAttribute(writer, SAML11Constants.IP_ADDRESS, ip);
        }
        String dns = locality.getDnsAddress();
        if (StringUtil.isNotNull(dns)) {
            StaxUtil.writeAttribute(writer, SAML11Constants.DNS_ADDRESS, dns);
        }
        StaxUtil.writeEndElement(writer);
    }

    /** 写入 AuthorizationDecisionStatement 及关联 Action。 */
    public void write(SAML11AuthorizationDecisionStatementType xacmlStat) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, SAML11Constants.AUTHORIZATION_DECISION_STATEMENT, ns);

        String resource = xacmlStat.getResource().toString();
        StaxUtil.writeAttribute(writer, SAML11Constants.RESOURCE, resource);

        StaxUtil.writeAttribute(writer, SAML11Constants.DECISION, xacmlStat.getDecision().name());

        SAML11SubjectType subject = xacmlStat.getSubject();
        if (subject != null)
            write(subject);

        List<SAML11ActionType> actions = xacmlStat.getActions();
        for (SAML11ActionType action : actions) {
            write(action);
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /**
     * 将 {@code SubjectType} 写入输出流。
     *
     * @param subject 主题对象
     *
     * @throws ProcessingException 写入失败时抛出
     */
    public void write(SAML11SubjectType subject) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, JBossSAMLConstants.SUBJECT.get(),
                SAML11Constants.ASSERTION_11_NSURI);
        StaxUtil.writeNameSpace(writer, ASSERTION_PREFIX, ns);

        SAML11SubjectTypeChoice choice = subject.getChoice();
        if (choice != null) {
            SAML11NameIdentifierType nameid = choice.getNameID();
            if (nameid != null) {
                write(nameid);
            }

            SAML11SubjectConfirmationType confirmation = choice.getSubjectConfirmation();
            if (confirmation != null)
                write(confirmation);
        }

        SAML11SubjectConfirmationType confirmation = subject.getSubjectConfirmation();
        if (confirmation != null)
            write(confirmation);

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /** 写入 SubjectConfirmation 及其确认方法与 KeyInfo。 */
    public void write(SAML11SubjectConfirmationType confirmation) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, JBossSAMLConstants.SUBJECT_CONFIRMATION.get(),
                SAML11Constants.ASSERTION_11_NSURI);
        List<URI> confirmationMethods = confirmation.getConfirmationMethod();
        if (confirmationMethods != null) {
            for (URI confirmationMethod : confirmationMethods) {
                StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, SAML11Constants.CONFIRMATION_METHOD,
                        SAML11Constants.ASSERTION_11_NSURI);
                StaxUtil.writeCharacters(writer, confirmationMethod.toString());
                StaxUtil.writeEndElement(writer);
            }
        }

        Element keyInfo = confirmation.getKeyInfo();
        if (keyInfo != null) {
            StaxUtil.writeDOMElement(writer, keyInfo);
        }

        Object subjectConfirmationData = confirmation.getSubjectConfirmationData();
        if (subjectConfirmationData != null) {
            writeSubjectConfirmationData(subjectConfirmationData);
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /** 写入 SubjectConfirmationData（当前未实现）。 */
    public void writeSubjectConfirmationData(Object scData) throws ProcessingException {
        throw logger.notImplementedYet("SubjectConfirmationData");
    }

    /** 写入 NameIdentifier 元素。 */
    public void write(SAML11NameIdentifierType nameid) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, SAML11Constants.NAME_IDENTIFIER,
                SAML11Constants.ASSERTION_11_NSURI);

        URI format = nameid.getFormat();
        if (format != null) {
            StaxUtil.writeAttribute(writer, SAML11Constants.FORMAT, format.toString());
        }
        String nameQualifier = nameid.getNameQualifier();
        if (StringUtil.isNotNull(nameQualifier)) {
            StaxUtil.writeAttribute(writer, SAML11Constants.NAME_QUALIFIER, nameQualifier);
        }

        StaxUtil.writeCharacters(writer, nameid.getValue());

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /**
     * 将 {@code AttributeType} 写入输出流。
     *
     * @param attributeType 属性对象
     *
     * @throws ProcessingException 写入失败时抛出
     */
    public void write(SAML11AttributeType attributeType) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, JBossSAMLConstants.ATTRIBUTE.get(), ns);

        writeAttributeTypeWithoutRootTag(attributeType);

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /** 写入 Attribute 元素内容（不含根标签）。 */
    public void writeAttributeTypeWithoutRootTag(SAML11AttributeType attributeType) throws ProcessingException {
        String attributeName = attributeType.getAttributeName();
        if (StringUtil.isNullOrEmpty(attributeName))
            throw logger.writerNullValueError("attribute name");
        StaxUtil.writeAttribute(writer, SAML11Constants.ATTRIBUTE_NAME, attributeName);

        String attributeNamespace = attributeType.getAttributeNamespace().toString();
        if (StringUtil.isNullOrEmpty(attributeNamespace))
            throw logger.writerNullValueError("attribute namespace");
        StaxUtil.writeAttribute(writer, SAML11Constants.ATTRIBUTE_NAMESPACE, attributeNamespace);

        List<Object> attributeValues = attributeType.get();
        if (attributeValues != null) {
            for (Object attributeValue : attributeValues) {
                if (attributeValue instanceof String) {
                    writeStringAttributeValue((String) attributeValue);
                } else
                    throw logger.writerUnsupportedAttributeValueError(attributeValue.getClass().getName());
            }
        }
    }

    /** 写入 xs:string 类型的 AttributeValue。 */
    public void writeStringAttributeValue(String attributeValue) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, JBossSAMLConstants.ATTRIBUTE_VALUE.get(), ns);

        StaxUtil.writeNameSpace(writer, JBossSAMLURIConstants.XSI_PREFIX.get(), JBossSAMLURIConstants.XSI_NSURI.get());
        StaxUtil.writeNameSpace(writer, "xs", JBossSAMLURIConstants.XMLSCHEMA_NSURI.get());
        StaxUtil.writeAttribute(writer, "xsi", JBossSAMLURIConstants.XSI_NSURI.get(), "type", "xs:string");
        StaxUtil.writeCharacters(writer, attributeValue);
        StaxUtil.writeEndElement(writer);
    }

    /** 写入带 xml:lang 的本地化名称元素。 */
    public void writeLocalizedNameType(LocalizedNameType localizedNameType, QName startElement) throws ProcessingException {
        StaxUtil.writeStartElement(writer, startElement.getPrefix(), startElement.getLocalPart(),
                startElement.getNamespaceURI());
        StaxUtil.writeAttribute(writer, new QName(JBossSAMLURIConstants.XML.get(), "lang", "xml"), localizedNameType.getLang());
        StaxUtil.writeCharacters(writer, localizedNameType.getValue());
        StaxUtil.writeEndElement(writer);
    }

    /** 写入 Action 元素。 */
    public void write(SAML11ActionType action) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, SAML11Constants.ACTION, ns);
        String ns = action.getNamespace();
        if (StringUtil.isNotNull(ns)) {
            StaxUtil.writeAttribute(writer, SAML11Constants.NAMESPACE, ns);
        }
        String val = action.getValue();
        if (StringUtil.isNotNull(val)) {
            StaxUtil.writeCharacters(writer, val);
        }
        StaxUtil.writeEndElement(writer);
    }

    /** 写入 Evidence 元素（含 AssertionIDReference 与嵌套断言）。 */
    public void write(SAML11EvidenceType evidence) throws ProcessingException {
        StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, SAML11Constants.EVIDENCE, ns);

        List<String> assertionIDRefs = evidence.getAssertionIDReference();
        for (String assertionIDRef : assertionIDRefs) {
            StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, SAML11Constants.ASSERTION_ID_REF, ns);
            StaxUtil.writeCharacters(writer, assertionIDRef);
            StaxUtil.writeEndElement(writer);
        }

        List<SAML11AssertionType> assertions = evidence.getAssertions();
        for (SAML11AssertionType assertion : assertions) {
            write(assertion);
        }
        StaxUtil.writeEndElement(writer);
    }
}