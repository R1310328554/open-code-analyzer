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

/**
 * 供 {@link DefaultBeanDefinitionDocumentReader} 处理自定义顶层标签（直接位于 {@code <beans/>} 下）的接口。
 *
 * <p>实现可将自定义标签元数据转为任意数量的 {@link BeanDefinition}。
 *
 * <p>解析器从自定义标签所在命名空间的关联 {@link NamespaceHandler} 中定位 {@link BeanDefinitionParser}。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see AbstractBeanDefinitionParser
 */
public interface BeanDefinitionParser {

	/**
	 * 解析指定 {@link Element}，将得到的 {@link BeanDefinition} 注册到
	 * {@link ParserContext} 内嵌的
	 * {@link org.springframework.beans.factory.xml.ParserContext#getRegistry() BeanDefinitionRegistry}。
	 * <p>若将用于嵌套场景（如 {@code <property/>} 内的内部标签），必须返回解析得到的主 {@link BeanDefinition}；
	 * 若<strong>不会</strong>嵌套使用，可返回 {@code null}。
	 * @param element 待解析为一个或多个 {@link BeanDefinition} 的元素
	 * @param parserContext 封装当前解析状态，可访问 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * @return 主 {@link BeanDefinition}
	 */
	@Nullable BeanDefinition parse(Element element, ParserContext parserContext);

}
