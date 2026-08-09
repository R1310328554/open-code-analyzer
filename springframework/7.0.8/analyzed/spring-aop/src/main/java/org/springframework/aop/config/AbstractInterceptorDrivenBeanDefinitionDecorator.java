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
 * 希望向结果 Bean 添加 {@link org.aopalliance.intercept.MethodInterceptor 拦截器} 的
 * {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator BeanDefinitionDecorator}
 * 基类实现。
 *
 * <p>本基类控制 {@link ProxyFactoryBean} Bean 定义的创建，
 * 并将原始定义包装为 {@link ProxyFactoryBean} {@code target} 属性的内部 Bean 定义。
 *
 * <p>正确处理链式装饰，确保仅创建一个 {@link ProxyFactoryBean} 定义。
 * 若先前的 {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator}
 * 已创建 {@link org.springframework.aop.framework.ProxyFactoryBean}，
 * 则仅将拦截器添加到现有定义。
 *
 * <p>子类只需创建要添加的拦截器 {@code BeanDefinition}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.aopalliance.intercept.MethodInterceptor
 */
public abstract class AbstractInterceptorDrivenBeanDefinitionDecorator implements BeanDefinitionDecorator {

	@Override
	public final BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definitionHolder, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();

		// 获取根 Bean 名称——将作为生成的代理工厂 Bean 名称
		String existingBeanName = definitionHolder.getBeanName();
		BeanDefinition targetDefinition = definitionHolder.getBeanDefinition();
		BeanDefinitionHolder targetHolder = new BeanDefinitionHolder(targetDefinition, existingBeanName + ".TARGET");

		// 委托子类创建拦截器定义
		BeanDefinition interceptorDefinition = createInterceptorDefinition(node);

		// 生成名称并注册拦截器
		String interceptorName = existingBeanName + '.' + getInterceptorNameSuffix(interceptorDefinition);
		BeanDefinitionReaderUtils.registerBeanDefinition(
				new BeanDefinitionHolder(interceptorDefinition, interceptorName), registry);

		BeanDefinitionHolder result = definitionHolder;

		if (!isProxyFactoryBeanDefinition(targetDefinition)) {
			// 创建代理定义
			RootBeanDefinition proxyDefinition = new RootBeanDefinition();
			// 创建 ProxyFactoryBean 定义
			proxyDefinition.setBeanClass(ProxyFactoryBean.class);
			proxyDefinition.setScope(targetDefinition.getScope());
			proxyDefinition.setLazyInit(targetDefinition.isLazyInit());
			// 设置 target
			proxyDefinition.setDecoratedDefinition(targetHolder);
			proxyDefinition.getPropertyValues().add("target", targetHolder);
			// 创建 interceptorNames 列表
			proxyDefinition.getPropertyValues().add("interceptorNames", new ManagedList<>());
			// 从原始 Bean 定义复制 autowire 设置。
			proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
			proxyDefinition.setPrimary(targetDefinition.isPrimary());
			if (targetDefinition instanceof AbstractBeanDefinition abd) {
				proxyDefinition.copyQualifiersFrom(abd);
			}
			// 用 BeanDefinitionHolder 包装并指定 Bean 名称
			result = new BeanDefinitionHolder(proxyDefinition, existingBeanName);
		}

		addInterceptorNameToList(interceptorName, result.getBeanDefinition());
		return result;
	}

	@SuppressWarnings("unchecked")
	private void addInterceptorNameToList(String interceptorName, BeanDefinition beanDefinition) {
		List<String> list = (List<String>) beanDefinition.getPropertyValues().get("interceptorNames");
		Assert.state(list != null, "Missing 'interceptorNames' property");
		list.add(interceptorName);
	}

	private boolean isProxyFactoryBeanDefinition(BeanDefinition existingDefinition) {
		return ProxyFactoryBean.class.getName().equals(existingDefinition.getBeanClassName());
	}

	protected String getInterceptorNameSuffix(BeanDefinition interceptorDefinition) {
		String beanClassName = interceptorDefinition.getBeanClassName();
		return (StringUtils.hasLength(beanClassName) ?
				StringUtils.uncapitalize(ClassUtils.getShortName(beanClassName)) : "");
	}

	/**
	 * 子类应实现本方法，返回要应用于被装饰 Bean 的拦截器 {@code BeanDefinition}。
	 */
	protected abstract BeanDefinition createInterceptorDefinition(Node node);

}
