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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 便捷基类：待解析元素上的属性名与待配置 {@link Class} 的属性名一一对应。
 *
 * <p>当需要从相对简单的自定义 XML 元素创建单个 Bean 定义时继承本解析器。
 * 生成的 {@code BeanDefinition} 会自动注册到
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}。
 *
 * <p>示例：给定类 {@code SimpleCache} 含 {@code setName}、{@code setTimeout}、
 * {@code setEvictionPolicy} 等 setter，XML 标签
 * {@code <caching:cache name="..." timeout="..." eviction-policy="..."/>}
 * 只需子类实现 {@link #getBeanClass(Element)} 返回 {@code SimpleCache.class} 即可。
 *
 * <p>注意：{@code AbstractSimpleBeanDefinitionParser} 仅能将属性值填入 Bean 定义。
 * 若需解析构造器参数或嵌套元素，应实现
 * {@link #postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.w3c.dom.Element)}
 * 或继承 {@link AbstractSingleBeanDefinitionParser} / {@link AbstractBeanDefinitionParser}。
 *
 * <p>注册解析器到 Spring XML 基础设施的说明见 Spring 参考文档附录。
 * 可参考 {@link org.springframework.beans.factory.xml.UtilNamespaceHandler.PropertiesBeanDefinitionParser}：
 * {@code <util:properties location="jdbc.properties"/>} 的 {@code location} 属性
 * 对应 {@link org.springframework.beans.factory.config.PropertiesFactoryBean#setLocation(org.springframework.core.io.Resource)}，
 * 解析器只需在 {@link #getBeanClass(org.w3c.dom.Element)} 中返回 {@code PropertiesFactoryBean} 类型。
 *
 * @author Rob Harrop
 * @author Rick Evans
 * @author Juergen Hoeller
 * @since 2.0
 * @see Conventions#attributeNameToPropertyName(String)
 */
public abstract class AbstractSimpleBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	/**
	 * 解析给定 {@link Element} 并填充 {@link BeanDefinitionBuilder}。
	 * <p>本实现将元素上的属性映射为 {@link org.springframework.beans.PropertyValue}，
	 * 并通过 {@link BeanDefinitionBuilder#addPropertyValue(String, Object)} 加入构建器。
	 * {@link #extractPropertyName(String)} 用于将属性名与 JavaBean 属性名对齐。
	 * @param element 待解析的 XML 元素
	 * @param builder 用于构建 {@code BeanDefinition}
	 * @see #extractPropertyName(String)
	 */
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		NamedNodeMap attributes = element.getAttributes();
		for (int x = 0; x < attributes.getLength(); x++) {
			Attr attribute = (Attr) attributes.item(x);
			if (isEligibleAttribute(attribute, parserContext)) {
				String propertyName = extractPropertyName(attribute.getLocalName());
				Assert.state(StringUtils.hasText(propertyName),
						"Illegal property name returned from 'extractPropertyName(String)': cannot be null or empty.");
				builder.addPropertyValue(propertyName, attribute.getValue());
			}
		}
		postProcess(builder, element);
	}

	/**
	 * 判断给定属性是否应转为 Bean 属性值。
	 * <p>默认除 {@code id} 与命名空间声明属性外均可。
	 * @param attribute 待检查的 XML 属性
	 * @param parserContext {@code ParserContext}
	 * @see #isEligibleAttribute(String)
	 */
	protected boolean isEligibleAttribute(Attr attribute, ParserContext parserContext) {
		String fullName = attribute.getName();
		return (!fullName.equals("xmlns") && !fullName.startsWith("xmlns:") &&
				isEligibleAttribute(parserContext.getDelegate().getLocalName(attribute)));
	}

	/**
	 * 判断给定属性名是否应转为 Bean 属性值。
	 * <p>默认除 {@code id} 外均可。
	 * @param attributeName 来自 XML 元素的属性名（永不为 {@code null}）
	 */
	protected boolean isEligibleAttribute(String attributeName) {
		return !ID_ATTRIBUTE.equals(attributeName);
	}

	/**
	 * 从属性名提取 JavaBean 属性名。
	 * <p>默认使用 {@link Conventions#attributeNameToPropertyName(String)}。
	 * 返回名须符合 JavaBean 规范，例如 setter {@code setBingoHallFavourite(String)}
	 * 对应属性名 {@code bingoHallFavourite}（大小写须一致）。
	 * @param attributeName 来自 XML 元素的属性名（永不为 {@code null}）
	 * @return 提取的 JavaBean 属性名（永不为 {@code null}）
	 */
	protected String extractPropertyName(String attributeName) {
		return Conventions.attributeNameToPropertyName(attributeName);
	}

	/**
	 * 解析完成后的钩子，子类可检查或修改 Bean 定义。
	 * <p>默认无操作。
	 * @param beanDefinition 已解析（通常已完整定义）的 Bean 定义
	 * @param element 元数据来源的 XML 元素
	 */
	protected void postProcess(BeanDefinitionBuilder beanDefinition, Element element) {
	}

}
