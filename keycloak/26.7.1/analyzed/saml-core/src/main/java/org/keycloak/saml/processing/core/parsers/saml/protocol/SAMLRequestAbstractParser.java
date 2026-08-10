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
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.protocol.RequestAbstractType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;
import org.keycloak.saml.processing.core.parsers.util.SAMLParserUtil;

/**
 * SAML 请求解析抽象基类。
 * <p>封装所有 {@link RequestAbstractType} 子类型共用的属性与子元素解析逻辑。</p>
 *
 * @param <T> 请求类型，须继承 RequestAbstractType
 * @since Nov 2, 2010
 */
public abstract class SAMLRequestAbstractParser<T extends RequestAbstractType> extends AbstractStaxSamlProtocolParser<T> {

    /** SAML 协议版本号 2.0。 */
    protected static final String VERSION_2_0 = "2.0";

    /** 构造并绑定期望的起始请求元素。 */
    protected SAMLRequestAbstractParser(SAMLProtocolQNames expectedStartElement) {
        super(expectedStartElement);
    }

    /**
     * 解析所有 SAML 请求类型共有的 Destination 与 Consent 属性。
     *
     * @param startElement 请求根元素
     * @param request 目标请求对象
     * @throws ParsingException 解析失败时抛出
     */
    protected void parseBaseAttributes(StartElement startElement, T request) throws ParsingException {
        request.setDestination(StaxParserUtil.getUriAttributeValue(startElement, SAMLProtocolQNames.ATTR_DESTINATION));
        request.setConsent(StaxParserUtil.getAttributeValue(startElement, SAMLProtocolQNames.ATTR_CONSENT));
    }

    /**
     * 若当前子元素为 Issuer、Signature 或 Extensions，则解析并写入请求对象。
     * @param element 协议元素枚举
     * @param elementDetail 子元素起始标签
     * @param xmlEventReader XML 事件读取器
     * @param request 目标请求对象
     * @throws ParsingException 解析失败时抛出
     */
    protected void parseCommonElements(SAMLProtocolQNames element, StartElement elementDetail, XMLEventReader xmlEventReader, RequestAbstractType request)
            throws ParsingException {
        switch (element) {
            case ISSUER:
                request.setIssuer(SAMLParserUtil.parseNameIDType(xmlEventReader));
                break;

            case SIGNATURE:
                request.setSignature(StaxParserUtil.getDOMElement(xmlEventReader));
                break;
            
            case EXTENSIONS:
                request.setExtensions(SAMLExtensionsParser.getInstance().parse(xmlEventReader));
                break;
        }
    }
}