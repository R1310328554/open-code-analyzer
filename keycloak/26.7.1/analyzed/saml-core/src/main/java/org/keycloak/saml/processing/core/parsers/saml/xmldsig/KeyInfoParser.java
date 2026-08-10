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

import org.keycloak.dom.xmlsec.w3.xmldsig.KeyInfoType;
import org.keycloak.dom.xmlsec.w3.xmldsig.KeyValueType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 XMLDSig {@code KeyInfo} 元素的 StAX 解析器。
 * <p>支持 X509 证书数据与 RSA/DSA 密钥值等子元素，未知标签将被跳过。</p>
 */
public class KeyInfoParser extends AbstractStaxXmlDSigParser<KeyInfoType> {

    /** 单例实例。 */
    public static final KeyInfoParser INSTANCE = new KeyInfoParser();

    private KeyInfoParser() {
        super(XmlDSigQNames.KEY_INFO);
    }

    /** @return 解析器单例 */
    public static KeyInfoParser getInstance() {
        return INSTANCE;
    }

    /** @return 新建的空 {@link KeyInfoType} 对象 */
    @Override
    protected KeyInfoType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new KeyInfoType();
    }

    /**
     * 分发 KeyInfo 子元素到对应专用解析器。
     *
     * @param xmlEventReader StAX 事件读取器
     * @param target 待填充的 KeyInfo 对象
     * @param element 子元素枚举
     * @param elementDetail 子元素起始事件
     */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, KeyInfoType target, XmlDSigQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case X509_DATA:
                target.addContent(X509DataParser.getInstance().parse(xmlEventReader));
                break;

            case KEY_VALUE:
                StaxParserUtil.advance(xmlEventReader);
                StartElement startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
                KeyValueType keyValue;

                switch (LOOKUP.from(startElement.getName())) {
                    case RSA_KEY_VALUE:
                        keyValue = RsaKeyValueParser.getInstance().parse(xmlEventReader);
                        break;

                    case DSA_KEY_VALUE:
                        keyValue = DsaKeyValueParser.getInstance().parse(xmlEventReader);
                        break;

                    default:
                        String tag = StaxParserUtil.getElementName(startElement);
                        throw LOGGER.parserUnknownTag(tag, elementDetail.getLocation());
                }

                target.addContent(keyValue);
                break;

            default:
                // 忽略未知标签
                StaxParserUtil.bypassElementBlock(xmlEventReader);
        }
    }
}