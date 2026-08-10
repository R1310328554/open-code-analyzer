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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 SAML 断言中的 {@code Attribute} 元素。
 * <p>读取属性名、友好名、格式及非标准属性，并递归解析 {@code AttributeValue} 子元素。</p>
 *
 * @since Oct 14, 2010
 */
public class SAMLAttributeParser extends AbstractStaxSamlAssertionParser<AttributeType> {

    /** 单例实例。 */
    private static final SAMLAttributeParser INSTANCE = new SAMLAttributeParser();

    /** 标准属性 QName 集合，其余属性归入 otherAttributes。 */
    private static final Set<QName> DEFAULT_KNOWN_ATTRIBUTE_NAMES = new HashSet<>(Arrays.asList(
            SAMLAssertionQNames.ATTR_NAME.getQName(),
            SAMLAssertionQNames.ATTR_FRIENDLY_NAME.getQName(),
            SAMLAssertionQNames.ATTR_NAME_FORMAT.getQName()
    ));

    /** 私有构造，绑定 ATTRIBUTE 根元素。 */
    private SAMLAttributeParser() {
        super(SAMLAssertionQNames.ATTRIBUTE);
    }

    /** @return 解析器单例 */
    public static SAMLAttributeParser getInstance() {
        return INSTANCE;
    }

    /** 从起始元素创建 {@link AttributeType} 并填充标准属性。 */
    @Override
    protected AttributeType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        String name = StaxParserUtil.getRequiredAttributeValue(element, SAMLAssertionQNames.ATTR_NAME);
        final AttributeType attribute = new AttributeType(name);

        attribute.setFriendlyName(StaxParserUtil.getAttributeValue(element, SAMLAssertionQNames.ATTR_FRIENDLY_NAME));
        attribute.setNameFormat(StaxParserUtil.getAttributeValue(element, SAMLAssertionQNames.ATTR_NAME_FORMAT));

        // 将非标准属性（如 ATTR_X500_ENCODING）收集到 otherAttributes
        attribute.getOtherAttributes().putAll(collectUnknownAttributesFrom(element));

        return attribute;
    }

    /**
     * 收集给定 {@link StartElement} 上的非标准属性值。
     * <p>不在 {@code DEFAULT_KNOWN_ATTRIBUTE_NAMES} 中的属性视为非标准属性。</p>
     *
     * @param element 起始元素
     * @return 非标准属性 QName 到值的映射
     */
    private static Map<QName, String> collectUnknownAttributesFrom(StartElement element) {

        Map<QName, String> otherAttributes = new HashMap<>();

        Iterator<?> attributes = element.getAttributes();
        while (attributes.hasNext()) {
            Attribute currentAttribute = (Attribute) attributes.next();
            QName attributeQName = currentAttribute.getName();
            if (attributeQName == null || DEFAULT_KNOWN_ATTRIBUTE_NAMES.contains(attributeQName)) {
                continue;
            }
            String attributeValue = currentAttribute.getValue();
            otherAttributes.put(attributeQName, attributeValue);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("Adding attribute %s with value %s", attributeQName, attributeValue));
            }
        }

        return otherAttributes;
    }

    /** 分发处理 AttributeValue 等子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, AttributeType target, SAMLAssertionQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case ATTRIBUTE_VALUE:
                target.addAttributeValue(SAMLAttributeValueParser.getInstance().parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}