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

import org.keycloak.dom.saml.v2.protocol.StatusCodeType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 SAML 2.0 {@link StatusCodeType} 状态码元素。
 * <p>支持嵌套子状态码以表达多级错误层次。</p>
 */
public class SAMLStatusCodeParser extends AbstractStaxSamlProtocolParser<StatusCodeType> {

    /** 单例实例。 */
    private static final SAMLStatusCodeParser INSTANCE = new SAMLStatusCodeParser();

    private SAMLStatusCodeParser() {
        super(SAMLProtocolQNames.STATUS_CODE);
    }

    /** 返回解析器单例。 */
    public static SAMLStatusCodeParser getInstance() {
        return INSTANCE;
    }

    /** 创建 StatusCode 并读取 Value URI 属性。 */
    @Override
    protected StatusCodeType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        final StatusCodeType res = new StatusCodeType();
        res.setValue(StaxParserUtil.getUriAttributeValue(element, SAMLProtocolQNames.ATTR_VALUE));
        return res;
    }

    /** 递归解析嵌套的 StatusCode 子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, StatusCodeType target, SAMLProtocolQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case STATUS_CODE:
                target.setStatusCode(SAMLStatusCodeParser.getInstance().parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}