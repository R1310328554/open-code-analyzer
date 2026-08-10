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

import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import org.w3c.dom.Element;

/**
 * 通用 DOM 解析器：将指定根元素下的所有子节点解析为 {@link Element} 列表。
 */
public class AnyDomParser extends AbstractStaxParser<List<Element>, AnyDomParser.Dom> {

    /** 子元素标记：任意 DOM 节点均按同一策略处理。 */
    public static enum Dom { ANY_DOM };

    /** 以根元素 QName 构造解析器。 */
    public AnyDomParser(QName name) {
        super(name, Dom.ANY_DOM);
    }

    /** 工厂方法：创建针对指定根元素的解析器实例。 */
    public static AnyDomParser getInstance(QName name) {
        return new AnyDomParser(name);
    }

    @Override
    protected List<Element> instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new LinkedList<>();
    }

    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, List<Element> target, Dom element, StartElement elementDetail) throws ParsingException {
        target.add(StaxParserUtil.getDOMElement(xmlEventReader));
    }

    @Override
    protected boolean isUnknownElement(Dom token) {
        return true;
    }

    @Override
    protected Dom getElementFromName(QName name) {
        return Dom.ANY_DOM;
    }

}