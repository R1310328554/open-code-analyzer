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

import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinitionHolder;

/**
 * 供 {@link DefaultBeanDefinitionDocumentReader} 处理自定义嵌套标签（直接位于 {@code <bean>} 下）的接口。
 *
 * <p>装饰也可基于应用于 {@code <bean>} 的自定义属性。实现可将自定义标签元数据转为任意数量的
 * {@link org.springframework.beans.factory.config.BeanDefinition}，并变换外层 {@code <bean>} 的
 * {@link org.springframework.beans.factory.config.BeanDefinition}，甚至返回全新定义以替换原定义。
 *
 * <p>{@link BeanDefinitionDecorator} 可能处于责任链中：前序装饰器可能已将原
 * {@link org.springframework.beans.factory.config.BeanDefinition} 替换为
 * {@link org.springframework.aop.framework.ProxyFactoryBean}，以便添加自定义
 * {@link org.aopalliance.intercept.MethodInterceptor}。
 *
 * <p>若需为外层 Bean 添加拦截器，应继承
 * {@link org.springframework.aop.config.AbstractInterceptorDrivenBeanDefinitionDecorator}，
 * 由其处理链式代理，确保只创建一个包含链上所有拦截器的代理。
 *
 * <p>解析器从自定义标签所在命名空间的 {@link NamespaceHandler} 中定位 {@link BeanDefinitionDecorator}。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see BeanDefinitionParser
 */
public interface BeanDefinitionDecorator {

	/**
	 * 解析指定 {@link Node}（元素或属性），装饰提供的
	 * {@link org.springframework.beans.factory.config.BeanDefinition}，返回装饰后的定义。
	 * <p>实现可返回全新定义以在 {@link org.springframework.beans.factory.BeanFactory} 中替换原定义。
	 * <p>可通过 {@link ParserContext} 注册支撑主定义的额外 Bean。
	 */
	BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext);

}
