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
package org.keycloak.saml.processing.core.parsers.saml.xmldsig;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.xmlsec.w3.xmldsig.X509CertificateType;
import org.keycloak.dom.xmlsec.w3.xmldsig.X509DataType;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 XMLDSig {@code X509Data} 元素的 StAX 解析器。
 * <p>当前支持 {@code X509Certificate} 子元素，将 Base64 编码证书写入 {@link X509DataType}。</p>
 */
public class X509DataParser extends AbstractStaxXmlDSigParser<X509DataType> {

    /** 单例实例。 */
    private static final X509DataParser INSTANCE = new X509DataParser();

    /** 构造以 X509Data 为根元素的解析器。 */
    public X509DataParser() {
        super(XmlDSigQNames.X509_DATA);
    }

    /** @return 解析器单例 */
    public static X509DataParser getInstance() {
        return INSTANCE;
    }

    /** @return 新建的空 {@link X509DataType} 对象 */
    @Override
    protected X509DataType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new X509DataType();
    }

    /**
     * 处理 X509Data 子元素（目前仅 X509Certificate）。
     *
     * @param xmlEventReader StAX 事件读取器
     * @param target 待填充的 X509 数据对象
     * @param element 子元素枚举
     * @param elementDetail 子元素起始事件
     */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, X509DataType target, XmlDSigQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case X509_CERTIFICATE:
                StaxParserUtil.advance(xmlEventReader);
                String certValue = StaxParserUtil.getElementText(xmlEventReader);

                X509CertificateType cert = new X509CertificateType();
                cert.setEncodedCertificate(certValue.getBytes(GeneralConstants.SAML_CHARSET));
                target.add(cert);
                
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}