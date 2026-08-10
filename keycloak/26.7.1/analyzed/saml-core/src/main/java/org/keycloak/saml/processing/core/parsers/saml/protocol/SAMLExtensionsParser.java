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

import org.keycloak.dom.saml.v2.protocol.ExtensionsType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 SAML 2.0 {@code samlp:Extensions} 元素为 DOM 节点序列。
 * <p>扩展容器允许在协议消息中携带任意命名空间的扩展内容。</p>
 *
 * @author hmlnarik
 */
public class SAMLExtensionsParser extends AbstractStaxSamlProtocolParser<ExtensionsType> {

    /** 单例实例。 */
    private static final SAMLExtensionsParser INSTANCE = new SAMLExtensionsParser();

    private SAMLExtensionsParser() {
        super(SAMLProtocolQNames.EXTENSIONS);
    }

    /** 返回解析器单例。 */
    public static SAMLExtensionsParser getInstance() {
        return INSTANCE;
    }

    /** 创建空的 ExtensionsType 容器。 */
    @Override
    protected ExtensionsType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new ExtensionsType();
    }

    /** 将每个子元素转为 DOM 节点并加入扩展列表。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, ExtensionsType target, SAMLProtocolQNames element, StartElement elementDetail) throws ParsingException {
        target.addExtension(StaxParserUtil.getDOMElement(xmlEventReader));
    }
}
