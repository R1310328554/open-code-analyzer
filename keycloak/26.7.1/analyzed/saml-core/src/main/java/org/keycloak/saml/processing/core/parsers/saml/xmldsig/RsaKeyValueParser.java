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
package org.keycloak.saml.processing.core.parsers.saml.xmldsig;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.xmlsec.w3.xmldsig.RSAKeyValueType;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 XMLDSig {@code RSAKeyValue} 元素的 StAX 解析器。
 * <p>读取 RSA 公钥模数（Modulus）与指数（Exponent）并填充 {@link RSAKeyValueType}。</p>
 */
public class RsaKeyValueParser extends AbstractStaxXmlDSigParser<RSAKeyValueType> {

    /** 单例实例。 */
    public static final RsaKeyValueParser INSTANCE = new RsaKeyValueParser();

    private RsaKeyValueParser() {
        super(XmlDSigQNames.RSA_KEY_VALUE);
    }

    /** @return 解析器单例 */
    public static RsaKeyValueParser getInstance() {
        return INSTANCE;
    }

    /** @return 新建的空 {@link RSAKeyValueType} 对象 */
    @Override
    protected RSAKeyValueType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new RSAKeyValueType();
    }

    /**
     * 处理 RSAKeyValue 子元素（Modulus / Exponent）。
     *
     * @param xmlEventReader StAX 事件读取器
     * @param target 待填充的 RSA 密钥值对象
     * @param element 子元素枚举
     * @param elementDetail 子元素起始事件
     */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, RSAKeyValueType target, XmlDSigQNames element, StartElement elementDetail) throws ParsingException {
        String text;
        switch (element) {
            case MODULUS:
                StaxParserUtil.advance(xmlEventReader);
                text = StaxParserUtil.getElementText(xmlEventReader);
                target.setModulus(text.getBytes(GeneralConstants.SAML_CHARSET));
                break;

            case EXPONENT:
                StaxParserUtil.advance(xmlEventReader);
                text = StaxParserUtil.getElementText(xmlEventReader);
                target.setExponent(text.getBytes(GeneralConstants.SAML_CHARSET));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}