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
package org.keycloak.saml.processing.core.parsers.saml.assertion;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.assertion.EncryptedElementType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.assertion.SubjectType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.parsers.StaxParser;
import org.keycloak.saml.common.util.StaxParserUtil;
import org.keycloak.saml.processing.core.parsers.util.SAMLParserUtil;

import org.w3c.dom.Element;

/**
 * 解析 SAML 断言中的 {@code Subject} 元素。
 * <p>处理 NameID、EncryptedID 及 SubjectConfirmation 子元素，构建主体类型。</p>
 *
 * @since Oct 12, 2010
 */
public class SAMLSubjectParser extends AbstractStaxSamlAssertionParser<SubjectType> implements StaxParser {

    /** 单例实例。 */
    private static final SAMLSubjectParser INSTANCE = new SAMLSubjectParser();

    /** 私有构造，绑定 SUBJECT 根元素。 */
    private SAMLSubjectParser() {
        super(SAMLAssertionQNames.SUBJECT);
    }

    /** @return 解析器单例 */
    public static SAMLSubjectParser getInstance() {
        return INSTANCE;
    }

    /** 创建空的主体对象。 */
    @Override
    protected SubjectType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new SubjectType();
    }

    /** 解析 NameID、EncryptedID 或 SubjectConfirmation 子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, SubjectType target, SAMLAssertionQNames element, StartElement elementDetail) throws ParsingException {
        SubjectType.STSubType subType;
        switch (element) {
            case NAMEID:
                NameIDType nameID = SAMLParserUtil.parseNameIDType(xmlEventReader);
                subType = new SubjectType.STSubType();
                subType.addBaseID(nameID);
                target.setSubType(subType);
                break;

            case ENCRYPTED_ID:
                Element domElement = StaxParserUtil.getDOMElement(xmlEventReader);
                subType = new SubjectType.STSubType();
                subType.setEncryptedID(new EncryptedElementType(domElement));
                target.setSubType(subType);
                break;

            case SUBJECT_CONFIRMATION:
                target.addConfirmation(SAMLSubjectConfirmationParser.INSTANCE.parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}