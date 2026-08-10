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
package org.keycloak.saml.processing.core.parsers.saml.assertion;

import javax.xml.namespace.QName;

import org.keycloak.saml.common.parsers.AbstractStaxParser;
import org.keycloak.saml.processing.core.parsers.util.QNameEnumLookup;

/**
 * SAML 2.0 断言子元素 StAX 解析器抽象基类。
 *
 * @author hmlnarik
 */
public abstract class AbstractStaxSamlAssertionParser<T> extends AbstractStaxParser<T, SAMLAssertionQNames> {

    /** 断言命名空间 QName 到枚举的查找表。 */
    protected static final QNameEnumLookup<SAMLAssertionQNames> LOOKUP = new QNameEnumLookup(SAMLAssertionQNames.values());

    /** 指定期望的起始元素类型。 */
    public AbstractStaxSamlAssertionParser(SAMLAssertionQNames expectedStartElement) {
        super(expectedStartElement.getQName(), SAMLAssertionQNames.UNKNOWN_ELEMENT);
    }

    @Override
    protected SAMLAssertionQNames getElementFromName(QName name) {
        return LOOKUP.from(name);
    }

}
