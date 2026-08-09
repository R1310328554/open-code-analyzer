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
 * 代理处理器通用功能基类，
 * 尤其包含 ClassLoader 管理与 {@link #evaluateProxyInterfaces} 算法。
 *
 * @author Juergen Hoeller
 * @since 4.1
 * @see AbstractAdvisingBeanPostProcessor
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator
 */
@SuppressWarnings("serial")
public class ProxyProcessorSupport extends ProxyConfig implements Ordered, BeanClassLoaderAware, AopInfrastructureBean {

	/**
	 * 应在所有其他处理器之后运行，
	 * 以便向现有代理添加 Advisor 而非双重代理。
	 */
	private int order = Ordered.LOWEST_PRECEDENCE;

	private @Nullable ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

	private boolean classLoaderConfigured = false;


	/**
	 * 设置本处理器 {@link Ordered} 实现的排序值，
	 * 用于应用多个处理器时。
	 * <p>默认值为 {@code Ordered.LOWEST_PRECEDENCE}，表示无序。
	 * @param order 排序值
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * 设置生成代理类所用的 ClassLoader。
	 * <p>默认为 Bean ClassLoader，即包含的
	 * {@link org.springframework.beans.factory.BeanFactory} 加载所有 Bean 类所用的 ClassLoader。
	 * 可在此为特定代理覆盖。
	 */
	public void setProxyClassLoader(@Nullable ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
		this.classLoaderConfigured = (classLoader != null);
	}

	/**
	 * 返回本处理器配置的代理 ClassLoader。
	 */
	protected @Nullable ClassLoader getProxyClassLoader() {
		return this.proxyClassLoader;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (!this.classLoaderConfigured) {
			this.proxyClassLoader = classLoader;
		}
	}


	/**
	 * 检查给定 Bean 类的接口，并在适当时应用到 {@link ProxyFactory}。
	 * <p>调用 {@link #isConfigurationCallbackInterface} 与 {@link #isInternalLanguageInterface}
	 * 过滤合理的代理接口，否则回退到目标类代理。
	 * @param beanClass Bean 的类
	 * @param proxyFactory Bean 的 ProxyFactory
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
			// 须允许引介；不能仅将接口设为目标接口。
			for (Class<?> ifc : targetInterfaces) {
				proxyFactory.addInterface(ifc);
			}
		}
		else {
			proxyFactory.setProxyTargetClass(true);
		}
	}

	/**
	 * 判断给定接口是否仅为容器回调，
	 * 因此不应视为合理的代理接口。
	 * <p>若给定 Bean 找不到合理代理接口，
	 * 将以其完整目标类代理，假定此为用户的意图。
	 * @param ifc 要检查的接口
	 * @return 给定接口是否仅为容器回调
	 */
	protected boolean isConfigurationCallbackInterface(Class<?> ifc) {
		return (InitializingBean.class == ifc || DisposableBean.class == ifc || Closeable.class == ifc ||
				AutoCloseable.class == ifc || ObjectUtils.containsElement(ifc.getInterfaces(), Aware.class));
	}

	/**
	 * 判断给定接口是否为已知内部语言接口，
	 * 因此不应视为合理的代理接口。
	 * <p>若给定 Bean 找不到合理代理接口，
	 * 将以其完整目标类代理，假定此为用户的意图。
	 * @param ifc 要检查的接口
	 * @return 给定接口是否为内部语言接口
	 */
	protected boolean isInternalLanguageInterface(Class<?> ifc) {
		return (ifc.getName().equals("groovy.lang.GroovyObject") ||
				ifc.getName().endsWith(".cglib.proxy.Factory") ||
				ifc.getName().endsWith(".bytebuddy.MockAccess"));
	}

}
