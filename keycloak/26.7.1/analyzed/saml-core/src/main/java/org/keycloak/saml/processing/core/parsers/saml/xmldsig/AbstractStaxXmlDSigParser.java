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

import javax.xml.namespace.QName;

import org.keycloak.saml.common.parsers.AbstractStaxParser;
import org.keycloak.saml.processing.core.parsers.util.QNameEnumLookup;

/**
 * XML 数字签名（XMLDSig）StAX 解析器抽象基类。
 * <p>将 {@link XmlDSigQNames} 枚举与 StAX 事件流解析框架衔接，子类负责具体元素类型的实例化与子元素处理。</p>
 *
 * @author hmlnarik
 */
public abstract class AbstractStaxXmlDSigParser<T> extends AbstractStaxParser<T, XmlDSigQNames> {

    /** XMLDSig 元素 QName 到枚举常量的查找表。 */
    protected static final QNameEnumLookup<XmlDSigQNames> LOOKUP = new QNameEnumLookup(XmlDSigQNames.values());

    /**
     * 构造解析器并指定期望的根元素类型。
     *
     * @param expectedStartElement 解析起始元素对应的 {@link XmlDSigQNames} 常量
     */
    public AbstractStaxXmlDSigParser(XmlDSigQNames expectedStartElement) {
        super(expectedStartElement.getQName(), XmlDSigQNames.UNKNOWN_ELEMENT);
    }

    /**
     * 根据 XML 元素 QName 解析为 {@link XmlDSigQNames} 枚举值。
     *
     * @param name 元素 QName
     * @return 匹配的枚举常量，未知元素时返回 {@code null}
     */
    @Override
    protected XmlDSigQNames getElementFromName(QName name) {
        return LOOKUP.from(name);
    }

}
