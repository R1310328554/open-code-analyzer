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

import org.keycloak.dom.xmlsec.w3.xmldsig.DSAKeyValueType;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 XMLDSig {@code DSAKeyValue} 元素的 StAX 解析器。
 * <p>读取 DSA 公钥参数（P、Q、G、Y 等）并填充 {@link DSAKeyValueType}。</p>
 */
public class DsaKeyValueParser extends AbstractStaxXmlDSigParser<DSAKeyValueType> {

    /** 单例实例。 */
    public static final DsaKeyValueParser INSTANCE = new DsaKeyValueParser();

    /** 私有构造，使用 {@link #getInstance()} 获取实例。 */
    private DsaKeyValueParser() {
        super(XmlDSigQNames.DSA_KEY_VALUE);
    }

    /** @return 解析器单例 */
    public static DsaKeyValueParser getInstance() {
        return INSTANCE;
    }

    /** @return 新建的空 {@link DSAKeyValueType} 对象 */
    @Override
    protected DSAKeyValueType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new DSAKeyValueType();
    }

    /**
     * 处理 DSAKeyValue 子元素并写入目标对象。
     *
     * @param xmlEventReader StAX 事件读取器
     * @param target 待填充的 DSA 密钥值对象
     * @param element 子元素对应的 {@link XmlDSigQNames}
     * @param elementDetail 子元素起始事件
     */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, DSAKeyValueType target, XmlDSigQNames element, StartElement elementDetail) throws ParsingException {
        String text;
        switch (element) {
            case P:
                StaxParserUtil.advance(xmlEventReader);
                text = StaxParserUtil.getElementText(xmlEventReader);
                target.setP(text.getBytes(GeneralConstants.SAML_CHARSET));
                break;

            case Q:
                StaxParserUtil.advance(xmlEventReader);
                text = StaxParserUtil.getElementText(xmlEventReader);
                target.setQ(text.getBytes(GeneralConstants.SAML_CHARSET));
                break;

            case G:
                StaxParserUtil.advance(xmlEventReader);
                text = StaxParserUtil.getElementText(xmlEventReader);
                target.setG(text.getBytes(GeneralConstants.SAML_CHARSET));
                break;

            case Y:
                StaxParserUtil.advance(xmlEventReader);
                text = StaxParserUtil.getElementText(xmlEventReader);
                target.setY(text.getBytes(GeneralConstants.SAML_CHARSET));
                break;

            case J:
                StaxParserUtil.advance(xmlEventReader);
                text = StaxParserUtil.getElementText(xmlEventReader);
                target.setJ(text.getBytes(GeneralConstants.SAML_CHARSET));
                break;

            case SEED:
                StaxParserUtil.advance(xmlEventReader);
                text = StaxParserUtil.getElementText(xmlEventReader);
                target.setSeed(text.getBytes(GeneralConstants.SAML_CHARSET));
                break;

            case PGEN_COUNTER:
                StaxParserUtil.advance(xmlEventReader);
                text = StaxParserUtil.getElementText(xmlEventReader);
                target.setPgenCounter(text.getBytes(GeneralConstants.SAML_CHARSET));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}