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
package org.keycloak.saml.processing.core.parsers.saml.assertion;

import java.util.Objects;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.assertion.SubjectConfirmationDataType;
import org.keycloak.dom.xmlsec.w3.xmldsig.KeyInfoType;
import org.keycloak.saml.common.constants.WSTrustConstants;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;
import org.keycloak.saml.processing.core.parsers.saml.xmldsig.KeyInfoParser;

/**
 * 解析 SAML 断言中的 {@code SubjectConfirmationData} 元素。
 * <p>读取 InResponseTo、有效期、Recipient 等属性，并支持 KeyInfo 或 EncryptedKey 子元素。</p>
 */
public class SAMLSubjectConfirmationDataParser extends AbstractStaxSamlAssertionParser<SubjectConfirmationDataType> {

    /** 单例实例。 */
    public static final SAMLSubjectConfirmationDataParser INSTANCE = new SAMLSubjectConfirmationDataParser();

    /** 构造并绑定 SUBJECT_CONFIRMATION_DATA 根元素。 */
    public SAMLSubjectConfirmationDataParser() {
        super(SAMLAssertionQNames.SUBJECT_CONFIRMATION_DATA);
    }

    /** 从起始元素属性创建主体确认数据对象。 */
    @Override
    protected SubjectConfirmationDataType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        final SubjectConfirmationDataType subjectConfirmationData = new SubjectConfirmationDataType();

        subjectConfirmationData.setInResponseTo(StaxParserUtil.getAttributeValue(element, SAMLAssertionQNames.ATTR_IN_RESPONSE_TO));
        subjectConfirmationData.setNotBefore(StaxParserUtil.getXmlTimeAttributeValue(element, SAMLAssertionQNames.ATTR_NOT_BEFORE));
        subjectConfirmationData.setNotOnOrAfter(StaxParserUtil.getXmlTimeAttributeValue(element, SAMLAssertionQNames.ATTR_NOT_ON_OR_AFTER));
        subjectConfirmationData.setRecipient(StaxParserUtil.getAttributeValue(element, SAMLAssertionQNames.ATTR_RECIPIENT));
        subjectConfirmationData.setAddress(StaxParserUtil.getAttributeValue(element, SAMLAssertionQNames.ATTR_ADDRESS));

        return subjectConfirmationData;
    }

    /** 解析 KeyInfo 或 WS-Trust EncryptedKey 子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, SubjectConfirmationDataType target, SAMLAssertionQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case KEY_INFO:
                KeyInfoType keyInfo = KeyInfoParser.getInstance().parse(xmlEventReader);
                target.setAnyType(keyInfo);
                break;

            default:
                String tag = StaxParserUtil.getElementName(elementDetail);

                if (Objects.equals(tag, WSTrustConstants.XMLEnc.ENCRYPTED_KEY)) {
                    target.setAnyType(StaxParserUtil.getDOMElement(xmlEventReader));
                } else {
                    throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
                }
        }
    }
}