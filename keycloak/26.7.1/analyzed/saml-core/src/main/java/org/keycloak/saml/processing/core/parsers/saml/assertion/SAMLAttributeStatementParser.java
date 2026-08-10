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

import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 SAML 断言中的 {@code AttributeStatement} 元素。
 * <p>将嵌套的 {@code Attribute} 子元素解析为 {@link AttributeStatementType}。</p>
 *
 * @since Oct 14, 2010
 */
public class SAMLAttributeStatementParser extends AbstractStaxSamlAssertionParser<AttributeStatementType> {

    /** 单例实例。 */
    private static final SAMLAttributeStatementParser INSTANCE = new SAMLAttributeStatementParser();

    /** 私有构造，绑定 ATTRIBUTE_STATEMENT 根元素。 */
    private SAMLAttributeStatementParser() {
        super(SAMLAssertionQNames.ATTRIBUTE_STATEMENT);
    }

    /** @return 解析器单例 */
    public static SAMLAttributeStatementParser getInstance() {
        return INSTANCE;
    }

    /** 创建空的属性声明对象。 */
    @Override
    protected AttributeStatementType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new AttributeStatementType();
    }

    /** 解析 Attribute 子元素并加入声明。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, AttributeStatementType target, SAMLAssertionQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case ATTRIBUTE:
                target.addAttribute(new ASTChoiceType(SAMLAttributeParser.getInstance().parse(xmlEventReader)));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}