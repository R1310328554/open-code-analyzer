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

package org.springframework.aop.framework;

import java.io.Closeable;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 具有代理处理器通用功能的基类，特别是类加载器管理和 {@link #evaluateProxyInterfaces} 算法。
 * @author Juergen Hoeller
 * @since 4.1
 * @see AbstractAdvisingBeanPostProcessor
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator
 */
@SuppressWarnings("serial")
public class ProxyProcessorSupport extends ProxyConfig implements Ordered, BeanClassLoaderAware, AopInfrastructureBean {

	/**
	 * 它应该在所有其他处理器之后运行，以便它可以仅向现有代理添加顾问程序而不是双重代理。
	 */
	private int order = Ordered.LOWEST_PRECEDENCE;

	/**
	 * 获取 Default Class Loader（`DefaultClassLoader`）。
	 */
	private @Nullable ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

	/** `false`：该类的成员状态。 */
	private boolean classLoaderConfigured = false;


	/**
	 * 设置将应用于该处理器的 {@link Ordered} 实现的顺序，在应用多个处理器时使用。 <p>默认值为{@code
	 * Ordered.LOWEST_PRECEDENCE}，意思是无序的。
	 * @param order 订购值
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * 获取 Order（`Order`）。
	 */
	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * 设置ClassLoader以生成代理类。<p>Default是bean ClassLoader，即包含{@link org.springframework.beans.fact
	 * ory.BeanFactory}用于加载所有bean类的ClassLoader。对于特定代理，可以在此处覆盖此设置。
	 */
	public void setProxyClassLoader(@Nullable ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
		this.classLoaderConfigured = (classLoader != null);
	}

	/**
	 * 返回为此处理器配置的代理类加载器。
	 */
	protected @Nullable ClassLoader getProxyClassLoader() {
		return this.proxyClassLoader;
	}

	/**
	 * 设置 Bean Class Loader（`BeanClassLoader`）。
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (!this.classLoaderConfigured) {
			this.proxyClassLoader = classLoader;
		}
	}


	/**
	 * 检查给定 bean 类上的接口并将它们应用到 {@link ProxyFactory}（如果适用）。 <p>调用 {@link
	 * #isConfigurationCallbackInterface} 和 {@link #isInternalLanguageInterface}
	 * 来过滤合理的代理接口，否则回退到目标类代理。
	 * @param beanClass 豆类
	 * @param proxyFactory bean 的 ProxyFactory
	 */
	protected void evaluateProxyInterfaces(Class<?> beanClass, ProxyFactory proxyFactory) {
		Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, getProxyClassLoader());
		boolean hasReasonableProxyInterface = false;
		for (Class<?> ifc : targetInterfaces) {
			if (!isConfigurationCallbackInterface(ifc) && !isInternalLanguageInterface(ifc) &&
					ifc.getMethods().length > 0) {
				hasReasonableProxyInterface = true;
				break;
			}
		}
		if (hasReasonableProxyInterface) {
			// 必须允许介绍；不能只将接口设置为目标的接口。
			for (Class<?> ifc : targetInterfaces) {
				proxyFactory.addInterface(ifc);
			}
		}
		else {
			proxyFactory.setProxyTargetClass(true);
		}
	}

	/**
	 * 确定给定的接口是否只是一个容器回调，因此不应被视为合理的代理接口。 <p>如果没有为给定的 bean 找到合理的代理接口，它将使用其完整目标类进行代理，假设这是用户的意图。
	 * @param ifc 要检查的接口
	 * @return 给定的接口只是一个容器回调
	 */
	protected boolean isConfigurationCallbackInterface(Class<?> ifc) {
		return (InitializingBean.class == ifc || DisposableBean.class == ifc || Closeable.class == ifc ||
				AutoCloseable.class == ifc || ObjectUtils.containsElement(ifc.getInterfaces(), Aware.class));
	}

	/**
	 * 确定给定的接口是否是众所周知的内部语言接口，因此不被视为合理的代理接口。 <p>如果没有为给定的 bean 找到合理的代理接口，它将使用其完整目标类进行代理，假设这是用户的意图
	 * 。
	 * @param ifc 要检查的接口
	 * @return 给定的接口是内部语言接口
	 */
	protected boolean isInternalLanguageInterface(Class<?> ifc) {
		return (ifc.getName().equals("groovy.lang.GroovyObject") ||
				ifc.getName().endsWith(".cglib.proxy.Factory") ||
				ifc.getName().endsWith(".bytebuddy.MockAccess"));
	}

}
