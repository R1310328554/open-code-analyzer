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

package org.springframework.aop.config;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@code NamespaceHandler} 为 {@code aop} 命名空间。
 * <p> 为 {@code <aop:config>} 标记提供 {@link
 * org.springframework.beans.factory.xml.BeanDefinitionParser}。 {@code config} 标签可以包含嵌套的
 * {@code pointcut}、{@code advisor} 和 {@code aspect} 标签。
 * <p>{@code pointcut} 标记允许使用简单语法创建命名 {@link AspectJExpressionPointcut} bean： <pre
 * class="code"> <aop:pointcut id=“getNameCalls”表达式=“执行(* *..ITestBean.getName(..))”/>
 * </pre>
 * <p> 使用 {@code advisor} 标签，您可以配置 {@link org.springframework.aop.Advisor} 并将其自动应用于 {@link
 * org.springframework.beans.factory.BeanFactory} 中的所有相关 bean。 {@code advisor} 标签支持内联和引用
 * {@link org.springframework.aop.Pointcut Pointcuts}：
 * <pre class="code"> <aop:advisor id="getAgeAdvisor";切入点=“执行(* *..ITestBean.getAge(..))”
 * Advice-ref =“getAgeCounter”/>
 * <aop:advisor id="getNameAdvisor"; pointcut-ref="getNameCalls";
 * Advice-ref=“getNameCounter”/></pre>
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AopNamespaceHandler extends NamespaceHandlerSupport {

	/**
	 * 为“{@code config}”、“{@code spring-configured}”、“{@code aspectj-autoproxy}”和“{@code
	 * scoped-proxy}”标签注册 {@link BeanDefinitionParser BeanDefinitionParsers}。
	 */
	@Override
	public void init() {
		// 在 2.0 XSD 以及 2.5+ XSD 中
		registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
		registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser());
		registerBeanDefinitionDecorator("scoped-proxy", new ScopedProxyBeanDefinitionDecorator());

		// 仅在 2.0 XSD 中：在 2.5+ 中移至上下文命名空间
		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
	}

}
