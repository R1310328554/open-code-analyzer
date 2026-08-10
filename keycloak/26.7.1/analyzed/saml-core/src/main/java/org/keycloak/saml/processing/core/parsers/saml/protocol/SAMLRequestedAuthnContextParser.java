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
package org.keycloak.saml.processing.core.parsers.saml.protocol;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.protocol.AuthnContextComparisonType;
import org.keycloak.dom.saml.v2.protocol.RequestedAuthnContextType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 SAML 2.0 {@link RequestedAuthnContextType} 元素。
 * <p>指定 SP 期望的认证上下文类或声明引用及比较方式。</p>
 *
 * @since Nov 2, 2010
 */
public class SAMLRequestedAuthnContextParser extends AbstractStaxSamlProtocolParser<RequestedAuthnContextType> {

    /** 单例实例。 */
    private static final SAMLRequestedAuthnContextParser INSTANCE = new SAMLRequestedAuthnContextParser();

    private SAMLRequestedAuthnContextParser() {
        super(SAMLProtocolQNames.REQUESTED_AUTHN_CONTEXT);
    }

    /** 返回解析器单例。 */
    public static SAMLRequestedAuthnContextParser getInstance() {
        return INSTANCE;
    }

    /**
     * 创建 RequestedAuthnContext 并读取 Comparison 属性。
     *
     * @param xmlEventReader XML 事件读取器
     * @param startElement 起始元素
     * @return RequestedAuthnContextType 对象
     * @throws ParsingException 解析失败时抛出
     */
    @Override
    protected RequestedAuthnContextType instantiateElement(XMLEventReader xmlEventReader, StartElement startElement) throws ParsingException {
        RequestedAuthnContextType context = new RequestedAuthnContextType();

        Attribute comparison = startElement.getAttributeByName(SAMLProtocolQNames.ATTR_COMPARISON.getQName());
        if (comparison != null) {
            context.setComparison(AuthnContextComparisonType.fromValue(comparison.getValue()));
        }

        return context;
    }

    /** 解析 AuthnContextClassRef 或 AuthnContextDeclRef 子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, RequestedAuthnContextType target, SAMLProtocolQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case AUTHN_CONTEXT_CLASS_REF:
                StaxParserUtil.advance(xmlEventReader);
                String value = StaxParserUtil.getElementText(xmlEventReader);
                target.addAuthnContextClassRef(value);
                break;

            case AUTHN_CONTEXT_DECL_REF:
                StaxParserUtil.advance(xmlEventReader);
                value = StaxParserUtil.getElementText(xmlEventReader);
                target.addAuthnContextDeclRef(value);
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}