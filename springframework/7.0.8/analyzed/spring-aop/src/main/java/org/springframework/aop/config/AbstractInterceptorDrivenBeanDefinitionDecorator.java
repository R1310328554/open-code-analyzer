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

import java.util.List;

import org.w3c.dom.Node;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 希望将 {@link org.aopalliance.intercept.MethodInterceptor interceptor} 添加到生成的 bean 的
 * {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator
 * BeanDefinitionDecorators} 的基本实现。
 * <p>此基类控制 {@link ProxyFactoryBean} bean 定义的创建，并将原始内容包装为 {@link ProxyFactoryBean} 的
 * {@code target} 属性的内部 bean 定义。
 * <p>Caining 已正确处理，确保仅创建一个 {@link ProxyFactoryBean} 定义。如果以前的 {@link
 * org.springframework.beans.factory.xml.BeanDefinitionDecorator} 已经创建了 {@link
 * org.springframework.aop.framework.ProxyFactoryBean}，则只需将拦截器添加到现有定义中。
 * <p>子类只需为它们希望添加的拦截器创建{@code BeanDefinition}。
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.aopalliance.intercept.MethodInterceptor
 */
public abstract class AbstractInterceptorDrivenBeanDefinitionDecorator implements BeanDefinitionDecorator {

	/**
	 * 方法 `decorate`：完成本类中与「decorate」相关的职责。
	 */
	@Override
	public final BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definitionHolder, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();

		// 获取根 bean 名称 - 将是生成的代理工厂 bean 的名称
		String existingBeanName = definitionHolder.getBeanName();
		BeanDefinition targetDefinition = definitionHolder.getBeanDefinition();
		BeanDefinitionHolder targetHolder = new BeanDefinitionHolder(targetDefinition, existingBeanName + ".TARGET");

		// 委托给子类进行拦截器定义
		BeanDefinition interceptorDefinition = createInterceptorDefinition(node);

		// 生成名称并注册拦截器
		String interceptorName = existingBeanName + '.' + getInterceptorNameSuffix(interceptorDefinition);
		BeanDefinitionReaderUtils.registerBeanDefinition(
				new BeanDefinitionHolder(interceptorDefinition, interceptorName), registry);

		BeanDefinitionHolder result = definitionHolder;

		if (!isProxyFactoryBeanDefinition(targetDefinition)) {
			// 创建代理定义
			RootBeanDefinition proxyDefinition = new RootBeanDefinition();
			// 创建代理工厂 bean 定义
			proxyDefinition.setBeanClass(ProxyFactoryBean.class);
			proxyDefinition.setScope(targetDefinition.getScope());
			proxyDefinition.setLazyInit(targetDefinition.isLazyInit());
			// 设定目标
			proxyDefinition.setDecoratedDefinition(targetHolder);
			proxyDefinition.getPropertyValues().add("target", targetHolder);
			// 创建拦截器名称列表
			proxyDefinition.getPropertyValues().add("interceptorNames", new ManagedList<>());
			// 从原始 bean 定义复制自动装配设置。
			proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
			proxyDefinition.setPrimary(targetDefinition.isPrimary());
			if (targetDefinition instanceof AbstractBeanDefinition abd) {
				proxyDefinition.copyQualifiersFrom(abd);
			}
			// 将其包装在带有 bean 名称的 BeanDefinitionHolder 中
			result = new BeanDefinitionHolder(proxyDefinition, existingBeanName);
		}

		addInterceptorNameToList(interceptorName, result.getBeanDefinition());
		return result;
	}

	/**
	 * 添加：Interceptor Name To List（方法 `addInterceptorNameToList`）。
	 */
	@SuppressWarnings("unchecked")
	private void addInterceptorNameToList(String interceptorName, BeanDefinition beanDefinition) {
		List<String> list = (List<String>) beanDefinition.getPropertyValues().get("interceptorNames");
		Assert.state(list != null, "Missing 'interceptorNames' property");
		list.add(interceptorName);
	}

	/**
	 * 判断是否 Proxy Factory Bean Definition。
	 */
	private boolean isProxyFactoryBeanDefinition(BeanDefinition existingDefinition) {
		return ProxyFactoryBean.class.getName().equals(existingDefinition.getBeanClassName());
	}

	/**
	 * 获取 Interceptor Name Suffix（`InterceptorNameSuffix`）。
	 */
	protected String getInterceptorNameSuffix(BeanDefinition interceptorDefinition) {
		String beanClassName = interceptorDefinition.getBeanClassName();
		return (StringUtils.hasLength(beanClassName) ?
				StringUtils.uncapitalize(ClassUtils.getShortName(beanClassName)) : "");
	}

	/**
	 * 子类应该实现此方法，以返回它们希望应用于正在装饰的 bean 的拦截器的 {@code BeanDefinition}。
	 */
	protected abstract BeanDefinition createInterceptorDefinition(Node node);

}
