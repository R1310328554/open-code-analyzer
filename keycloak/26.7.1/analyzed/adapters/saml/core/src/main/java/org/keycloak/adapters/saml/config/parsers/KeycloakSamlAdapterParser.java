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
package org.keycloak.adapters.saml.config.parsers;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.keycloak.saml.common.ErrorCodes;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.parsers.AbstractParser;
import org.keycloak.saml.common.parsers.StaxParser;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * Keycloak SAML 适配器配置 XML 顶层解析入口。
 *
 * <p>根据根元素 QName 选择 {@link KeycloakSamlAdapterV1Parser}，兼容带命名空间与无命名空间两种写法。</p>
 *
 * @author hmlnarik
 */
public class KeycloakSamlAdapterParser extends AbstractParser {

    /** 按 QName 创建具体 StAX 解析器的工厂接口。 */
    private interface ParserFactory {
        public StaxParser create();
    }
    /** 根元素 QName 到解析器工厂的映射表。 */
    private static final Map<QName, ParserFactory> PARSERS = new HashMap<QName, ParserFactory>();

    /** 无命名空间变体：仅保留 localPart 的 QName。 */
    private static final QName ALTERNATE_KEYCLOAK_SAML_ADAPTER_V1 = new QName(KeycloakSamlAdapterV1QNames.KEYCLOAK_SAML_ADAPTER.getQName().getLocalPart());

    static {
        PARSERS.put(KeycloakSamlAdapterV1QNames.KEYCLOAK_SAML_ADAPTER.getQName(),   new ParserFactory() { @Override public StaxParser create() { return KeycloakSamlAdapterV1Parser.getInstance(); }});
        PARSERS.put(ALTERNATE_KEYCLOAK_SAML_ADAPTER_V1,                             new ParserFactory() { @Override public StaxParser create() { return KeycloakSamlAdapterV1Parser.getInstance(); }});
    }

    /** 单例顶层解析器。 */
    private static final KeycloakSamlAdapterParser INSTANCE = new KeycloakSamlAdapterParser();

    /** @return 共享的 {@link KeycloakSamlAdapterParser} 实例 */
    public static KeycloakSamlAdapterParser getInstance() {
        return INSTANCE;
    }

    protected KeycloakSamlAdapterParser() {
    }

    /**
     * 扫描 XML 事件流，定位首个 {@link StartElement} 并委派对应解析器。
     *
     * @see {@link org.keycloak.saml.common.parsers.ParserNamespaceSupport#parse(XMLEventReader)}
     */
    @Override
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);

            if (xmlEvent instanceof StartElement) {
                StartElement startElement = (StartElement) xmlEvent;
                final QName name = startElement.getName();

                ParserFactory pf = PARSERS.get(name);
                if (pf == null) {
                    throw logger.parserException(new RuntimeException(ErrorCodes.UNKNOWN_START_ELEMENT + name + "::location="
                            + startElement.getLocation()));
                }

                return pf.create().parse(xmlEventReader);
            }

            StaxParserUtil.getNextEvent(xmlEventReader);
        }

        throw new RuntimeException(ErrorCodes.FAILED_PARSING + "SAML Parsing has failed");
    }
}
