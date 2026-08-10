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

import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.protocol.StatusResponseType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * SAML 响应类型解析抽象基类。
 * <p>封装所有 {@link StatusResponseType} 子类型共用的属性解析逻辑。</p>
 *
 * @param <T> 响应类型，须继承 StatusResponseType
 */
public abstract class SAMLStatusResponseTypeParser<T extends StatusResponseType> extends AbstractStaxSamlProtocolParser<T> {

    /** SAML 协议版本号 2.0。 */
    protected static final String VERSION_2_0 = "2.0";

    /** 构造并绑定期望的起始响应元素。 */
    protected SAMLStatusResponseTypeParser(SAMLProtocolQNames expectedStartElement) {
        super(expectedStartElement);
    }

    /**
     * 解析所有 SAML 响应类型共有的 Destination、Consent 与 InResponseTo 属性。
     *
     * @param startElement 响应根元素
     * @param response 目标响应对象
     * @throws org.keycloak.saml.common.exceptions.ParsingException 解析失败时抛出
     */
    protected void parseBaseAttributes(StartElement startElement, T response) throws ParsingException {
        response.setDestination(StaxParserUtil.getAttributeValue(startElement, SAMLProtocolQNames.ATTR_DESTINATION));
        response.setConsent(StaxParserUtil.getAttributeValue(startElement, SAMLProtocolQNames.ATTR_CONSENT));
        response.setInResponseTo(StaxParserUtil.getAttributeValue(startElement, SAMLProtocolQNames.ATTR_IN_RESPONSE_TO));
    }
}