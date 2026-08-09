/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.xml;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

/**
 * 实现自定义 {@link NamespaceHandler NamespaceHandlers} 的支持类。
 * 对单个 {@link Node Nodes} 的解析与装饰分别通过 {@link BeanDefinitionParser}
 * 与 {@link BeanDefinitionDecorator} 策略接口完成。
 *
 * <p>提供 {@link #registerBeanDefinitionParser} 与 {@link #registerBeanDefinitionDecorator}
 * 方法，用于注册处理特定元素的 {@link BeanDefinitionParser} 或 {@link BeanDefinitionDecorator}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see #registerBeanDefinitionParser(String, BeanDefinitionParser)
 * @see #registerBeanDefinitionDecorator(String, BeanDefinitionDecorator)
 */
public abstract class NamespaceHandlerSupport implements NamespaceHandler {

	/**
	 * 按所处理 {@link Element Elements} 的本地名称存储 {@link BeanDefinitionParser} 实现。
	 */
	private final Map<String, BeanDefinitionParser> parsers = new HashMap<>();

	/**
	 * 按所处理 {@link Element Elements} 的本地名称存储 {@link BeanDefinitionDecorator} 实现。
	 */
	private final Map<String, BeanDefinitionDecorator> decorators = new HashMap<>();

	/**
	 * 按所处理 {@link Attr Attrs} 的本地名称存储 {@link BeanDefinitionDecorator} 实现。
	 */
	private final Map<String, BeanDefinitionDecorator> attributeDecorators = new HashMap<>();


	/**
	 * 通过委托给为该 {@link Element} 注册的 {@link BeanDefinitionParser} 来解析所供 {@link Element}。
	 */
	@Override
	public @Nullable BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionParser parser = findParserForElement(element, parserContext);
		return (parser != null ? parser.parse(element, parserContext) : null);
	}

	/**
	 * 使用所供 {@link Element} 的本地名称，从已注册实现中定位 {@link BeanDefinitionParser}。
	 */
	private @Nullable BeanDefinitionParser findParserForElement(Element element, ParserContext parserContext) {
		String localName = parserContext.getDelegate().getLocalName(element);
		BeanDefinitionParser parser = this.parsers.get(localName);
		if (parser == null) {
			parserContext.getReaderContext().fatal(
					"Cannot locate BeanDefinitionParser for element [" + localName + "]", element);
		}
		return parser;
	}

	/**
	 * 通过委托给为所供 {@link Node} 注册的 {@link BeanDefinitionDecorator} 来装饰该 {@link Node}。
	 */
	@Override
	public @Nullable BeanDefinitionHolder decorate(
			Node node, BeanDefinitionHolder definition, ParserContext parserContext) {

		BeanDefinitionDecorator decorator = findDecoratorForNode(node, parserContext);
		return (decorator != null ? decorator.decorate(node, definition, parserContext) : null);
	}

	/**
	 * 使用所供 {@link Node} 的本地名称，从已注册实现中定位 {@link BeanDefinitionParser}。
	 * 同时支持 {@link Element Elements} 与 {@link Attr Attrs}。
	 */
	private @Nullable BeanDefinitionDecorator findDecoratorForNode(Node node, ParserContext parserContext) {
		BeanDefinitionDecorator decorator = null;
		String localName = parserContext.getDelegate().getLocalName(node);
		if (node instanceof Element) {
			decorator = this.decorators.get(localName);
		}
		else if (node instanceof Attr) {
			decorator = this.attributeDecorators.get(localName);
		}
		else {
			parserContext.getReaderContext().fatal(
					"Cannot decorate based on Nodes of type [" + node.getClass().getName() + "]", node);
		}
		if (decorator == null) {
			parserContext.getReaderContext().fatal("Cannot locate BeanDefinitionDecorator for " +
					(node instanceof Element ? "element" : "attribute") + " [" + localName + "]", node);
		}
		return decorator;
	}


	/**
	 * 子类可调用此方法，将所供 {@link BeanDefinitionParser} 注册为处理指定元素。
	 * 元素名称为本地（非命名空间限定）名称。
	 */
	protected final void registerBeanDefinitionParser(String elementName, BeanDefinitionParser parser) {
		this.parsers.put(elementName, parser);
	}

	/**
	 * 子类可调用此方法，将所供 {@link BeanDefinitionDecorator} 注册为处理指定元素。
	 * 元素名称为本地（非命名空间限定）名称。
	 */
	protected final void registerBeanDefinitionDecorator(String elementName, BeanDefinitionDecorator dec) {
		this.decorators.put(elementName, dec);
	}

	/**
	 * 子类可调用此方法，将所供 {@link BeanDefinitionDecorator} 注册为处理指定属性。
	 * 属性名称为本地（非命名空间限定）名称。
	 */
	protected final void registerBeanDefinitionDecoratorForAttribute(String attrName, BeanDefinitionDecorator dec) {
		this.attributeDecorators.put(attrName, dec);
	}

}
