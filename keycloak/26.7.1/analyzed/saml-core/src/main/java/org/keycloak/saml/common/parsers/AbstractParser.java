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
package org.keycloak.saml.common.parsers;

import java.io.InputStream;
import java.util.regex.Pattern;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.EventReaderDelegate;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.keycloak.common.util.Environment;
import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.SecurityActions;
import org.keycloak.saml.common.util.StaxParserUtil;
import org.keycloak.saml.common.util.SystemPropertiesUtil;

import org.w3c.dom.Node;

/**
 * 基于 StAX 的 SAML/XML 解析器抽象基类，提供事件读取器创建与白空间过滤等通用能力。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public abstract class AbstractParser implements StaxParser {

    /** 解析器共享日志实例。 */
    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /** 线程本地 {@link XMLInputFactory}，避免重复创建工厂实例。 */
    private static final ThreadLocal<XMLInputFactory> XML_INPUT_FACTORY = new ThreadLocal<XMLInputFactory>() {
        @Override
        protected XMLInputFactory initialValue() {
            return getXMLInputFactory();
        }

        /**
         * 获取 JAXP {@link XMLInputFactory}，可按系统属性切换 TCCL。
         *
         * @return 配置好的 XML 输入工厂
         */
        private XMLInputFactory getXMLInputFactory() {
            boolean tccl_jaxp = SystemPropertiesUtil.getSystemProperty(GeneralConstants.TCCL_JAXP, "false")
                    .equalsIgnoreCase("true");
            ClassLoader prevTCCL = SecurityActions.getTCCL();
            try {
                if (tccl_jaxp) {
                    SecurityActions.setTCCL(AbstractParser.class.getClassLoader());
                }
                return XMLInputFactory.newInstance();
            } finally {
                if (tccl_jaxp) {
                    SecurityActions.setTCCL(prevTCCL);
                }
            }
        }
    };

    /**
     * 从输入流解析 SAML/XML 载荷。
     *
     * @param stream 待解析输入流
     *
     * @return 解析结果对象
     *
     * @throws {@link IllegalArgumentException}
     * @throws {@link IllegalArgumentException} when the configStream is null
     */
    public Object parse(InputStream stream) throws ParsingException {
        XMLEventReader xmlEventReader = createEventReader(stream);
        return parse(xmlEventReader);
    }

    /** 从 {@link Source} 解析并返回对象。 */
    public Object parse(Source source) throws ParsingException {
        XMLEventReader xmlEventReader = createEventReader(source);
        return parse(xmlEventReader);
    }

    /** 从 DOM {@link Node} 解析并返回对象。 */
    public Object parse(Node node) throws ParsingException {
        return parse(new DOMSource(node));
    }

    /** 创建过滤空白字符后的 {@link XMLEventReader}。 */
    public static XMLEventReader createEventReader(InputStream configStream) throws ParsingException {
        if (configStream == null)
            throw logger.nullArgumentError("InputStream");

        XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(configStream);

        return filterWhitespaces(xmlEventReader);
    }

    /** 从 {@link Source} 创建过滤空白后的 {@link XMLEventReader}。 */
    public XMLEventReader createEventReader(Source source) throws ParsingException {
        if (source == null)
            throw logger.nullArgumentError("Source");

        XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(source);

        return filterWhitespaces(xmlEventReader);
    }

    /** 匹配纯空白字符的正则。 */
    private static final Pattern WHITESPACE_ONLY = Pattern.compile("\\s*");

    /**
     * 创建派生 {@link XMLEventReader}，仅保留 {@link StartElement}、{@link EndElement}
     * 以及非空且非纯空白的 {@link Characters} 事件。
     * 
     * @param xmlEventReader 原始 {@link XMLEventReader}
     * @return 过滤后的 {@link XMLEventReader}
     * @throws XMLStreamException
     */
    private static XMLEventReader filterWhitespaces(XMLEventReader xmlEventReader) throws ParsingException {
        XMLInputFactory xmlInputFactory = XML_INPUT_FACTORY.get();

        try {
            xmlEventReader = xmlInputFactory.createFilteredReader(xmlEventReader, new EventFilter() {
                @Override
                public boolean accept(XMLEvent xmlEvent) {
                    // 忽略换行与纯空白字符事件
                    if (xmlEvent.isCharacters()) {
                        Characters chars = xmlEvent.asCharacters();
                        String data = chars.getData();
                        return data != null && ! WHITESPACE_ONLY.matcher(data).matches();
                    } else {
                        return xmlEvent.isStartElement() || xmlEvent.isEndElement();
                    }
                }
            });
        } catch (XMLStreamException ex) {
            throw logger.parserException(ex);
        }

        // 处理 IBM JDK 在 StAX EventReader 上的已知缺陷
        if (Environment.IS_IBM_JAVA) {
            final XMLEventReader origReader = xmlEventReader;

            xmlEventReader = new EventReaderDelegate(origReader) {

                @Override
                public boolean hasNext() {
                    boolean hasNext = super.hasNext();
                    try {
                        return hasNext && (origReader.peek() != null);
                    } catch (XMLStreamException xse) {
                        throw new IllegalStateException(xse);
                    }
                }

            };
        }

        return xmlEventReader;
    }

}