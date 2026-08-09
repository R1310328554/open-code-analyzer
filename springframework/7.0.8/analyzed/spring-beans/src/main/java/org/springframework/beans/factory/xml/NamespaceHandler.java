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

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

/**
 * {@link DefaultBeanDefinitionDocumentReader} 用于处理 Spring XML 配置文件中
 * 自定义命名空间的基础接口。
 *
 * <p>实现类应为自定义顶层标签返回 {@link BeanDefinitionParser} 实现，
 * 为自定义嵌套标签返回 {@link BeanDefinitionDecorator} 实现。
 *
 * <p>解析器在 {@code <beans>} 标签下直接遇到自定义标签时调用 {@link #parse}，
 * 在 {@code <bean>} 标签下直接遇到自定义标签时调用 {@link #decorate}。
 *
 * <p>编写自定义元素扩展的开发者通常不会直接实现此接口，而是使用提供的
 * {@link NamespaceHandlerSupport} 类。
 *
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 2.0
 * @see DefaultBeanDefinitionDocumentReader
 * @see NamespaceHandlerResolver
 */
public interface NamespaceHandler {

	/**
	 * 由 {@link DefaultBeanDefinitionDocumentReader} 在构造之后、
	 * 解析任何自定义元素之前调用。
	 * @see NamespaceHandlerSupport#registerBeanDefinitionParser(String, BeanDefinitionParser)
	 */
	void init();

	/**
	 * 解析指定的 {@link Element}，并将得到的 {@link BeanDefinition BeanDefinitions}
	 * 注册到内嵌于所供 {@link ParserContext} 中的
	 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}。
	 * <p>若希望用于嵌套场景（例如 {@code <property>} 标签内），实现应返回
	 * 解析阶段产生的主 {@code BeanDefinition}。
	 * <p>若<strong>不会</strong>在嵌套场景中使用，实现可返回 {@code null}。
	 * @param element 待解析为一个或多个 {@code BeanDefinitions} 的元素
	 * @param parserContext 封装当前解析过程状态的对象
	 * @return 主 {@code BeanDefinition}（如上所述可为 {@code null}）
	 */
	@Nullable BeanDefinition parse(Element element, ParserContext parserContext);

	/**
	 * 解析指定的 {@link Node} 并装饰所供 {@link BeanDefinitionHolder}，返回装饰后的定义。
	 * <p>{@link Node} 可能是 {@link org.w3c.dom.Attr} 或 {@link Element}，
	 * 取决于解析的是自定义属性还是自定义元素。
	 * <p>实现可选择返回全新的定义，以替换结果
	 * {@link org.springframework.beans.factory.BeanFactory} 中的原始定义。
	 * <p>可使用所供 {@link ParserContext} 注册支撑主定义所需的额外 bean。
	 * @param source 待解析的源元素或属性
	 * @param definition 当前 bean 定义
	 * @param parserContext 封装当前解析过程状态的对象
	 * @return 装饰后的定义（将注册到 BeanFactory 中），
	 * 或若无需装饰则直接返回原始 bean 定义。
	 * 严格来说 {@code null} 无效，但会宽松地视为返回原始 bean 定义的情况。
	 */
	@Nullable BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder definition, ParserContext parserContext);

}
