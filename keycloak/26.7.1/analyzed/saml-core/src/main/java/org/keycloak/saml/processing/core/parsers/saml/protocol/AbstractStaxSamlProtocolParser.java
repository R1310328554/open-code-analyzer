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
package org.keycloak.saml.processing.core.parsers.saml.protocol;

import javax.xml.namespace.QName;

import org.keycloak.saml.common.parsers.AbstractStaxParser;
import org.keycloak.saml.processing.core.parsers.util.QNameEnumLookup;

/**
 * SAML 协议 STAX 解析器抽象基类。
 * <p>通过 {@link SAMLProtocolQNames} 枚举将 XML 子标签映射为解析分支。</p>
 *
 * @param <T> 解析结果对应的 Java 类型
 * @author hmlnarik
 */
public abstract class AbstractStaxSamlProtocolParser<T> extends AbstractStaxParser<T, SAMLProtocolQNames> {

    /** QName 到 {@link SAMLProtocolQNames} 的查找表。 */
    protected static final QNameEnumLookup<SAMLProtocolQNames> LOOKUP = new QNameEnumLookup(SAMLProtocolQNames.values());

    /** 构造并指定期望的起始协议元素。 */
    public AbstractStaxSamlProtocolParser(SAMLProtocolQNames expectedStartElement) {
        super(expectedStartElement.getQName(), SAMLProtocolQNames.UNKNOWN_ELEMENT);
    }

    /** 将 XML 元素 QName 映射为枚举常量。 */
    @Override
    protected SAMLProtocolQNames getElementFromName(QName name) {
        return LOOKUP.from(name);
    }

}
