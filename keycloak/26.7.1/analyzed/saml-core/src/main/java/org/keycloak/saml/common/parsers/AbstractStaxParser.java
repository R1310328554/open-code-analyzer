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
package org.keycloak.saml.common.parsers;

import java.util.Objects;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * STAX 单元素解析框架：解析指定根元素并分发处理其直接子元素。
 *
 * @param <T> 解析结果对应的 Java 类型
 * @param <E> 子元素标记类型，通常为枚举，用于映射 XML 子标签
 * @author hmlnarik
 */
public abstract class AbstractStaxParser<T, E> implements StaxParser {

    /** 解析器日志实例。 */
    protected static final PicketLinkLogger LOGGER = PicketLinkLoggerFactory.getLogger();
    /** 期望的起始元素 QName。 */
    protected final QName expectedStartElement;
    /** 未知子元素时使用的占位标记。 */
    private final E unknownElement;

    /** 构造解析器并指定根元素与未知子元素标记。 */
    public AbstractStaxParser(QName expectedStartElement, E unknownElement) {
        this.unknownElement = unknownElement;
        this.expectedStartElement = expectedStartElement;
    }

    @Override
    public T parse(XMLEventReader xmlEventReader) throws ParsingException {
        // 状态：应位于期望起始元素之前

        // 读取起始元素并校验是否为期望标签
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        final QName actualQName = startElement.getName();
        validateStartElement(startElement);
        T target = instantiateElement(xmlEventReader, startElement);

        // 状态：起始元素已读取
        QName currentSubelement = null;

        while (xmlEventReader.hasNext()) {
            // 状态：此阶段唯一合法的结束元素应对应当前起始元素
            XMLEvent xmlEvent = StaxParserUtil.peekNextTag(xmlEventReader);
            if (xmlEvent == null) {
                break;
            }

            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) xmlEvent;
                final QName qName = endElement.getName();

                // 已处理子元素的残留结束标签，直接消费
                if (Objects.equals(qName, currentSubelement)) {
                    StaxParserUtil.advance(xmlEventReader);
                    currentSubelement = null;
                    continue;
                }

                // 遇到与当前起始元素匹配的结束标签，结束解析
                if (Objects.equals(qName, actualQName)) {
                    // 消费结束元素并完成当前标签解析
                    StaxParserUtil.advance(xmlEventReader);
                    break;
                }

                // 其他结束标签均视为非法
                String elementName = StaxParserUtil.getElementName(endElement);
                throw LOGGER.parserUnknownEndElement(elementName, xmlEvent.getLocation());
            }

            startElement = (StartElement) xmlEvent;
            currentSubelement = startElement.getName();
            E token = getElementFromName(currentSubelement);
            if (token == null) {
                token = unknownElement;
            }
            processSubElement(xmlEventReader, target, token, startElement);

            // 若 processSubElement 未推进读取器（使用 == 而非 equals 判断），则跳过该子元素块
            if (StaxParserUtil.peek(xmlEventReader) == startElement) {
                StaxParserUtil.bypassElementBlock(xmlEventReader);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("Element %s bypassed", currentSubelement));
                }
            }

            // 递归嵌套同名元素时，对应结束标签必须在 {@code processSubElement} 中处理，此处不得消费
            if (Objects.equals(actualQName, currentSubelement) || isUnknownElement(token)) {
                currentSubelement = null;
            }
        }
        return target;
    }

    /**
     * 校验起始元素的 {@link QName} 是否与期望一致。
     * @param startElement 待校验的起始元素
     */
    protected void validateStartElement(StartElement startElement) {
        StaxParserUtil.validate(startElement, expectedStartElement);
    }

    /** 判断子元素标记是否表示未知/未识别元素。 */
    protected boolean isUnknownElement(E token) {
        return token == null || Objects.equals(token, unknownElement);
    }

    /** 将子元素 QName 映射为标记枚举/常量。 */
    protected abstract E getElementFromName(QName name);

    /**
     * 实例化表示当前 XML 元素的 Java 对象。<br>
     * <b>前置条件：</b>当前事件为 {@link StartElement}<br>
     * <b>后置条件：</b>当前事件仍为 {@link StartElement} 或与之对应的 {@link EndElement}
     * @param xmlEventReader 事件读取器
     * @param element 刚从 {@code xmlEventReader} 读取的起始元素
     * @return 解析目标对象
     * @throws ParsingException
     */
    protected abstract T instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException;

    /**
     * 处理 {@link #instantiateElement} 所创建元素的一个直接子元素。<br>
     * <b>前置条件：</b>当前位于待处理子元素 {@link StartElement} 之前，
     * 下一次 {@link XMLEventReader#next()} 即为该子元素起始标签<br>
     * <b>后置条件：</b>下一次 {@link XMLEventReader#next()} 为同一 {@link StartElement}（表示跳过）、
     * 对应 {@link EndElement}，或结束标签之后的事件。
     * <p>
     * 递归嵌套同名元素时，必须在子类中消费对应的结束标签。
     * @param xmlEventReader 事件读取器
     * @param target {@link #instantiateElement} 创建的目标对象
     * @param element 当前子元素对应的标记常量
     * @param elementDetail 刚从 {@code xmlEventReader} 读取的起始元素事件
     * @throws ParsingException
     */
    protected abstract void processSubElement(XMLEventReader xmlEventReader, T target, E element, StartElement elementDetail) throws ParsingException;

}
