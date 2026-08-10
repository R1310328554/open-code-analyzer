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

package org.keycloak.adapters.saml.config.parsers;

import java.util.Collections;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

import org.keycloak.saml.common.parsers.AbstractStaxParser;
import org.keycloak.saml.common.util.StaxParserUtil;
import org.keycloak.saml.processing.core.parsers.util.QNameEnumLookup;

/**
 * Keycloak SAML 适配器 V1 配置 XML 解析器抽象基类。
 *
 * <p>统一处理无命名空间（{@code NULL_NS_URI}）与标准命名空间下的元素 QName 映射，
 * 子类实现具体元素的实例化与子元素解析逻辑。</p>
 */
public abstract class AbstractKeycloakSamlAdapterV1Parser<T> extends AbstractStaxParser<T, KeycloakSamlAdapterV1QNames> {

    /** QName 到枚举的快速查找表。 */
    protected static final QNameEnumLookup<KeycloakSamlAdapterV1QNames> LOOKUP = new QNameEnumLookup(KeycloakSamlAdapterV1QNames.values());

    /** 允许将无命名空间元素视为标准 NS_URI 下的同名元素。 */
    private static final Set<String> ALTERNATE_NAMESPACES = Collections.singleton(XMLConstants.NULL_NS_URI);

    /**
     * @param expectedStartElement 本解析器期望的根/起始元素
     */
    public AbstractKeycloakSamlAdapterV1Parser(KeycloakSamlAdapterV1QNames expectedStartElement) {
        super(expectedStartElement.getQName(), KeycloakSamlAdapterV1QNames.UNKNOWN_ELEMENT);
    }

    /** 将 StAX 元素名映射为配置枚举；无命名空间时补全标准 URI。 */
    @Override
    protected KeycloakSamlAdapterV1QNames getElementFromName(QName name) {
        return (ALTERNATE_NAMESPACES.contains(name.getNamespaceURI()))
          ? LOOKUP.from(new QName(KeycloakSamlAdapterV1QNames.NS_URI, name.getLocalPart()))
          : LOOKUP.from(name);
    }

    /** 校验起始元素 QName，兼容无命名空间配置写法。 */
    @Override
    protected void validateStartElement(StartElement startElement) {
        QName name = startElement.getName();
        QName validatedQName = ALTERNATE_NAMESPACES.contains(name.getNamespaceURI())
          ? new QName(name.getNamespaceURI(), expectedStartElement.getLocalPart())
          : expectedStartElement;
        StaxParserUtil.validate(startElement, validatedQName);
    }

}
