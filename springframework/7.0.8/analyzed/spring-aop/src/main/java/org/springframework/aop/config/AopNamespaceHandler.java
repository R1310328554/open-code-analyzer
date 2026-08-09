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
 * {@code aop} 命名空间的 {@code NamespaceHandler}。
 *
 * <p>为 {@code <aop:config>} 标签提供
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser}。
 * {@code config} 标签可包含嵌套的 {@code pointcut}、{@code advisor} 与 {@code aspect} 标签。
 *
 * <p>{@code pointcut} 标签可用简单语法创建命名的
 * {@link AspectJExpressionPointcut} Bean：
 * <pre class="code">
 * &lt;aop:pointcut id=&quot;getNameCalls&quot; expression=&quot;execution(* *..ITestBean.getName(..))&quot;/&gt;
 * </pre>
 *
 * <p>使用 {@code advisor} 标签可配置 {@link org.springframework.aop.Advisor}，
 * 并自动应用于 {@link org.springframework.beans.factory.BeanFactory} 中所有相关 Bean。
 * {@code advisor} 标签支持内联与引用的 {@link org.springframework.aop.Pointcut 切入点}：
 *
 * <pre class="code">
 * &lt;aop:advisor id=&quot;getAgeAdvisor&quot;
 *     pointcut=&quot;execution(* *..ITestBean.getAge(..))&quot;
 *     advice-ref=&quot;getAgeCounter&quot;/&gt;
 *
 * &lt;aop:advisor id=&quot;getNameAdvisor&quot;
 *     pointcut-ref=&quot;getNameCalls&quot;
 *     advice-ref=&quot;getNameCounter&quot;/&gt;</pre>
 *
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AopNamespaceHandler extends NamespaceHandlerSupport {

	/**
	 * 注册 '{@code config}'、'{@code spring-configured}'、
	 * '{@code aspectj-autoproxy}' 与 '{@code scoped-proxy}' 标签的
	 * {@link BeanDefinitionParser BeanDefinitionParser}。
	 */
	@Override
	public void init() {
		// 2.0 XSD 及 2.5+ XSD 均包含
		registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
		registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser());
		registerBeanDefinitionDecorator("scoped-proxy", new ScopedProxyBeanDefinitionDecorator());

		// 仅 2.0 XSD 包含：2.5+ 已移至 context 命名空间
		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
	}

}
