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
package org.keycloak.saml.processing.core.parsers.saml.metadata;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;
import org.keycloak.saml.processing.core.parsers.saml.assertion.SAMLAttributeValueParser;

/**
 * 解析 SAML 元数据中的 {@code Attribute} 元素。
 * <p>读取属性名称、友好名称、名称格式及 X500 编码等属性，并解析 AttributeValue 子元素。</p>
 *
 * @since Oct 14, 2010
 */
public class SAMLAttributeParser extends AbstractStaxSamlMetadataParser<AttributeType> {

    /** 单例实例。 */
    private static final SAMLAttributeParser INSTANCE = new SAMLAttributeParser();

    /** 构造并绑定 ATTRIBUTE 根元素。 */
    private SAMLAttributeParser() {
        super(SAMLMetadataQNames.ATTRIBUTE);
    }

    /** 返回解析器单例。 */
    public static SAMLAttributeParser getInstance() {
        return INSTANCE;
    }

    /** 创建属性对象并读取 name、friendlyName、nameFormat 及 x500Encoding 属性。 */
    @Override
    protected AttributeType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        String name = StaxParserUtil.getRequiredAttributeValue(element, SAMLMetadataQNames.ATTR_NAME);
        final AttributeType attribute = new AttributeType(name);

        attribute.setFriendlyName(StaxParserUtil.getAttributeValue(element, SAMLMetadataQNames.ATTR_FRIENDLY_NAME));
        attribute.setNameFormat(StaxParserUtil.getAttributeValue(element, SAMLMetadataQNames.ATTR_NAME_FORMAT));

        final String x500Encoding = StaxParserUtil.getAttributeValue(element, SAMLMetadataQNames.ATTR_X500_ENCODING);
        if (x500Encoding != null) {
            attribute.getOtherAttributes().put(SAMLMetadataQNames.ATTR_X500_ENCODING.getQName(), x500Encoding);
        }

        return attribute;
    }

    /** 解析 AttributeValue 子元素并追加到属性值列表。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, AttributeType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case ATTRIBUTE_VALUE:
                target.addAttributeValue(SAMLAttributeValueParser.getInstance().parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}
