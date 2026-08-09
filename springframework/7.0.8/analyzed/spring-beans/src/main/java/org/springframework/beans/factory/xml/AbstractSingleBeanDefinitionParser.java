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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * 仅需解析并定义<i>单个</i> {@code BeanDefinition} 的 {@link BeanDefinitionParser} 实现基类。
 *
 * <p>当需要从任意复杂 XML 元素创建单个 Bean 定义时继承本类。
 * 若 XML 元素较简单，可考虑 {@link AbstractSimpleBeanDefinitionParser}。
 *
 * <p>生成的 {@code BeanDefinition} 会自动注册到
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}。
 * 子类只需实现 {@link #doParse}，将自定义 XML {@link Element} 解析为单个 {@code BeanDefinition}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 * @see #getBeanClass
 * @see #getBeanClassName
 * @see #doParse
 */
public abstract class AbstractSingleBeanDefinitionParser extends AbstractBeanDefinitionParser {

	/**
	 * 为 {@link #getBeanClass Bean 类} 创建 {@link BeanDefinitionBuilder}，并交给 {@link #doParse} 填充。
	 * @param element 待解析为单个 BeanDefinition 的元素
	 * @param parserContext 封装当前解析状态
	 * @return 解析得到的 BeanDefinition
	 * @throws IllegalStateException 若 {@link #getBeanClass(org.w3c.dom.Element)} 返回 {@code null}
	 * @see #doParse
	 */
	@Override
	protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
		String parentName = getParentName(element);
		if (parentName != null) {
			builder.getRawBeanDefinition().setParentName(parentName);
		}
		Class<?> beanClass = getBeanClass(element);
		if (beanClass != null) {
			builder.getRawBeanDefinition().setBeanClass(beanClass);
		}
		else {
			String beanClassName = getBeanClassName(element);
			if (beanClassName != null) {
				builder.getRawBeanDefinition().setBeanClassName(beanClassName);
			}
		}
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
		BeanDefinition containingBd = parserContext.getContainingBeanDefinition();
		if (containingBd != null) {
			// 内部 Bean 与外层 Bean 使用相同作用域
			builder.setScope(containingBd.getScope());
		}
		if (parserContext.isDefaultLazyInit()) {
			// default-lazy-init 同样适用于自定义 Bean 定义
			builder.setLazyInit(true);
		}
		doParse(element, parserContext, builder);
		return builder.getBeanDefinition();
	}

	/**
	 * 确定当前解析 Bean 的父 Bean 名（子 Bean 定义场景）。
	 * <p>默认返回 {@code null}，表示根 Bean 定义。
	 * @param element 正在解析的 {@code Element}
	 * @return 父 Bean 名，无则 {@code null}
	 */
	protected @Nullable String getParentName(Element element) {
		return null;
	}

	/**
	 * 确定与给定 {@link Element} 对应的 Bean 类。
	 * <p>对应用类，通常优先覆盖 {@link #getBeanClassName}，避免直接依赖实现类，
	 * 以便解析器在 IDE 插件中可用（应用类未必在插件类路径上）。
	 * @param element 正在解析的 {@code Element}
	 * @return Bean 的 {@link Class}，无则 {@code null}
	 * @see #getBeanClassName
	 */
	protected @Nullable Class<?> getBeanClass(Element element) {
		return null;
	}

	/**
	 * 确定与给定 {@link Element} 对应的 Bean 类名。
	 * @param element 正在解析的 {@code Element}
	 * @return Bean 类名，无则 {@code null}
	 * @see #getBeanClass
	 */
	protected @Nullable String getBeanClassName(Element element) {
		return null;
	}

	/**
	 * 解析给定 {@link Element} 并填充 {@link BeanDefinitionBuilder}。
	 * <p>默认委托给不带 ParserContext 的 {@code doParse} 重载。
	 * @param element 待解析的 XML 元素
	 * @param parserContext 封装当前解析状态
	 * @param builder 用于构建 {@code BeanDefinition}
	 * @see #doParse(Element, BeanDefinitionBuilder)
	 */
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		doParse(element, builder);
	}

	/**
	 * 解析给定 {@link Element} 并填充 {@link BeanDefinitionBuilder}。
	 * <p>默认无操作。
	 * @param element 待解析的 XML 元素
	 * @param builder 用于构建 {@code BeanDefinition}
	 */
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
	}

}
