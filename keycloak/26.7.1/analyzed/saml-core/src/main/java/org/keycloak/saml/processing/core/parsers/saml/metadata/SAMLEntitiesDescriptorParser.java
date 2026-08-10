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

import org.keycloak.dom.saml.v2.metadata.EntitiesDescriptorType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import org.w3c.dom.Element;

/**
 * 解析 SAML 元数据中的 {@code EntitiesDescriptor} 元素。
 * <p>可包含多个实体描述符或嵌套的实体集合，并读取 ID、有效期、缓存时长及名称等属性。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 31, 2011
 */
public class SAMLEntitiesDescriptorParser extends AbstractStaxSamlMetadataParser<EntitiesDescriptorType> {

    /** 单例实例。 */
    private static final SAMLEntitiesDescriptorParser INSTANCE = new SAMLEntitiesDescriptorParser();

    /** 构造并绑定 ENTITIES_DESCRIPTOR 根元素。 */
    public SAMLEntitiesDescriptorParser() {
        super(SAMLMetadataQNames.ENTITIES_DESCRIPTOR);
    }

    /** 返回解析器单例。 */
    public static SAMLEntitiesDescriptorParser getInstance() {
        return INSTANCE;
    }

    /** 创建实体集合描述符并读取 ID、validUntil、cacheDuration 及 name 属性。 */
    @Override
    protected EntitiesDescriptorType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        EntitiesDescriptorType descriptor = new EntitiesDescriptorType();

        // 解析属性
        descriptor.setID(StaxParserUtil.getAttributeValue(element, SAMLMetadataQNames.ATTR_ID));
        descriptor.setValidUntil(StaxParserUtil.getXmlTimeAttributeValue(element, SAMLMetadataQNames.ATTR_VALID_UNTIL));
        descriptor.setCacheDuration(StaxParserUtil.getXmlDurationAttributeValue(element, SAMLMetadataQNames.ATTR_CACHE_DURATION));
        descriptor.setName(StaxParserUtil.getAttributeValue(element, SAMLMetadataQNames.ATTR_NAME));

        return descriptor;
    }

    /** 解析签名、扩展、实体描述符及嵌套实体集合等子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, EntitiesDescriptorType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case SIGNATURE:
                Element sig = StaxParserUtil.getDOMElement(xmlEventReader);
                target.setSignature(sig);
                break;

            case EXTENSIONS:
                target.setExtensions(SAMLExtensionsParser.getInstance().parse(xmlEventReader));
                break;

            case ENTITY_DESCRIPTOR:
                target.addEntityDescriptor(SAMLEntityDescriptorParser.getInstance().parse(xmlEventReader));
                break;

            case ENTITIES_DESCRIPTOR:
                target.addEntityDescriptor(parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}
