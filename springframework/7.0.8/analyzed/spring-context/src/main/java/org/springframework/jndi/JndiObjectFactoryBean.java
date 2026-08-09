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

package org.springframework.jndi;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.naming.Context;
import javax.naming.NamingException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 查找 JNDI 对象的 {@link org.springframework.beans.factory.FactoryBean}。
 * 将 JNDI 中找到的对象暴露为 Bean 引用，例如 {@link javax.sql.DataSource} 用于 DAO 的 dataSource 属性。
 *
 * <p>典型用法是在应用上下文中注册为单例工厂（如某 JNDI 绑定的 DataSource），
 * 再注入需要它的应用服务。
 *
 * <p>默认在启动时查找并缓存 JNDI 对象。可通过 {@code lookupOnStartup} 与 {@code cache} 定制，
 * 底层使用 {@link JndiObjectTargetSource}。此场景需指定 {@code proxyInterface}，
 * 因为实际 JNDI 对象类型事先未知。
 *
 * <p>Spring 环境中的 Bean 类也可自行从 JNDI 查找 DataSource 等。
 * 本类便于集中配置 JNDI 名称并轻松切换到非 JNDI 替代方案，尤其利于测试与独立客户端。
 *
 * <p>切换到 {@link org.springframework.jdbc.datasource.DriverManagerDataSource} 等只需改配置：
 * 用其 Bean 定义替换本 FactoryBean 即可。
 *
 * @author Juergen Hoeller
 * @since 22.05.2003
 * @see #setProxyInterface
 * @see #setLookupOnStartup
 * @see #setCache
 * @see JndiObjectTargetSource
 */
public class JndiObjectFactoryBean extends JndiObjectLocator
		implements FactoryBean<Object>, BeanFactoryAware, BeanClassLoaderAware {

	private Class<?> @Nullable [] proxyInterfaces;

	private boolean lookupOnStartup = true;

	private boolean cache = true;

	private boolean exposeAccessContext = false;

	private @Nullable Object defaultObject;

	private @Nullable ConfigurableBeanFactory beanFactory;

	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private @Nullable Object jndiObject;


	/**
	 * 指定 JNDI 对象使用的代理接口。
	 * <p>通常与 {@code lookupOnStartup=false} 和/或 {@code cache=false} 配合使用。
	 * 懒加载时须指定，因实际 JNDI 对象类型事先未知。
	 * @see #setProxyInterfaces
	 * @see #setLookupOnStartup
	 * @see #setCache
	 */
	public void setProxyInterface(Class<?> proxyInterface) {
		this.proxyInterfaces = new Class<?>[] {proxyInterface};
	}

	/**
	 * 指定 JNDI 对象使用的多个代理接口。
	 * <p>通常与 {@code lookupOnStartup=false} 和/或 {@code cache=false} 配合使用。
	 * 必要时可从 {@code expectedType} 自动检测代理接口。
	 * @see #setExpectedType
	 * @see #setLookupOnStartup
	 * @see #setCache
	 */
	public void setProxyInterfaces(Class<?>... proxyInterfaces) {
		this.proxyInterfaces = proxyInterfaces;
	}

	/**
	 * 设置是否在启动时查找 JNDI 对象。默认为 {@code true}。
	 * <p>可关闭以允许 JNDI 对象延迟可用，首次访问时才获取。
	 * <p>懒加载须指定代理接口。
	 * @see #setProxyInterface
	 * @see #setCache
	 */
	public void setLookupOnStartup(boolean lookupOnStartup) {
		this.lookupOnStartup = lookupOnStartup;
	}

	/**
	 * 设置定位后是否缓存 JNDI 对象。默认为 {@code true}。
	 * <p>可关闭以支持热部署，每次调用重新获取。
	 * <p>热部署须指定代理接口。
	 * @see #setProxyInterface
	 * @see #setLookupOnStartup
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	/**
	 * 设置是否对所有目标对象访问（即暴露对象引用的全部方法调用）暴露 JNDI 环境上下文。
	 * <p>默认为 {@code false}，仅在对象查找时暴露 JNDI 上下文。
	 * 设为 {@code true} 可在每次方法调用时暴露 JNDI 环境（含授权上下文），
	 * 满足 WebLogic 对带授权要求的 JNDI 工厂（如 JDBC DataSource、JMS ConnectionFactory）的需求。
	 */
	public void setExposeAccessContext(boolean exposeAccessContext) {
		this.exposeAccessContext = exposeAccessContext;
	}

	/**
	 * 指定 JNDI 查找失败时的回退默认对象。默认为无。
	 * <p>可为任意 Bean 引用或字面值，常用于 JNDI 环境可能定义但非必需的配置项。
	 * <p>注意：仅支持启动时查找。若与 {@link #setExpectedType} 同时指定，
	 * 默认值须为该类型或可转换到该类型。
	 * @see #setLookupOnStartup
	 * @see ConfigurableBeanFactory#getTypeConverter()
	 * @see SimpleTypeConverter
	 */
	public void setDefaultObject(Object defaultObject) {
		this.defaultObject = defaultObject;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (beanFactory instanceof ConfigurableBeanFactory cbf) {
			// Just optional - for getting a specifically configured TypeConverter if needed.
			// We'll simply fall back to a SimpleTypeConverter if no specific one available.
			this.beanFactory = cbf;
		}
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}


	/** 查找 JNDI 对象并保存。 */
	@Override
	public void afterPropertiesSet() throws IllegalArgumentException, NamingException {
		super.afterPropertiesSet();

		if (this.proxyInterfaces != null || !this.lookupOnStartup || !this.cache || this.exposeAccessContext) {
			// We need to create a proxy for this...
			if (this.defaultObject != null) {
				throw new IllegalArgumentException(
						"'defaultObject' is not supported in combination with 'proxyInterface'");
			}
			// We need a proxy and a JndiObjectTargetSource.
			this.jndiObject = JndiObjectProxyFactory.createJndiObjectProxy(this);
		}
		else {
			if (this.defaultObject != null && getExpectedType() != null &&
					!getExpectedType().isInstance(this.defaultObject)) {
				TypeConverter converter = (this.beanFactory != null ?
						this.beanFactory.getTypeConverter() : new SimpleTypeConverter());
				try {
					this.defaultObject = converter.convertIfNecessary(this.defaultObject, getExpectedType());
				}
				catch (TypeMismatchException ex) {
					throw new IllegalArgumentException("Default object [" + this.defaultObject + "] of type [" +
							this.defaultObject.getClass().getName() + "] is not of expected type [" +
							getExpectedType().getName() + "] and cannot be converted either", ex);
				}
			}
			// Locate specified JNDI object.
			this.jndiObject = lookupWithFallback();
		}
	}

	/**
	 * 查找变体：查找失败时返回指定的 {@code defaultObject}（若有）。
	 * @return 查找到的对象，或回退的 {@code defaultObject}
	 * @throws NamingException 无回退且查找失败时
	 * @see #setDefaultObject
	 */
	protected Object lookupWithFallback() throws NamingException {
		ClassLoader originalClassLoader = ClassUtils.overrideThreadContextClassLoader(this.beanClassLoader);
		try {
			return lookup();
		}
		catch (TypeMismatchNamingException ex) {
			// Always let TypeMismatchNamingException through -
			// we don't want to fall back to the defaultObject in this case.
			throw ex;
		}
		catch (NamingException ex) {
			if (this.defaultObject != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("JNDI lookup failed - returning specified default object instead", ex);
				}
				else if (logger.isDebugEnabled()) {
					logger.debug("JNDI lookup failed - returning specified default object instead: " + ex);
				}
				return this.defaultObject;
			}
			throw ex;
		}
		finally {
			if (originalClassLoader != null) {
				Thread.currentThread().setContextClassLoader(originalClassLoader);
			}
		}
	}


	/** 返回单例 JNDI 对象。 */
	@Override
	public @Nullable Object getObject() {
		return this.jndiObject;
	}

	@Override
	public @Nullable Class<?> getObjectType() {
		if (this.proxyInterfaces != null) {
			if (this.proxyInterfaces.length == 1) {
				return this.proxyInterfaces[0];
			}
			else if (this.proxyInterfaces.length > 1) {
				return createCompositeInterface(this.proxyInterfaces);
			}
		}
		if (this.jndiObject != null) {
			return this.jndiObject.getClass();
		}
		else {
			return getExpectedType();
		}
	}

	@Override
	public boolean isSingleton() {
		return true;
	}


	/**
	 * 为给定接口创建合并的复合接口 Class。
	 * <p>默认实现为给定接口构建 JDK 代理类。
	 * @param interfaces 要合并的接口
	 * @return 合并后的接口 Class
	 * @see java.lang.reflect.Proxy#getProxyClass
	 */
	protected Class<?> createCompositeInterface(Class<?>[] interfaces) {
		return ClassUtils.createCompositeInterface(interfaces, this.beanClassLoader);
	}


	/** 内部类：仅在实际创建代理时引入 AOP 依赖。 */
	private static class JndiObjectProxyFactory {

		private static Object createJndiObjectProxy(JndiObjectFactoryBean jof) throws NamingException {
			// Create a JndiObjectTargetSource that mirrors the JndiObjectFactoryBean's configuration.
			JndiObjectTargetSource targetSource = new JndiObjectTargetSource();
			targetSource.setJndiTemplate(jof.getJndiTemplate());
			String jndiName = jof.getJndiName();
			Assert.state(jndiName != null, "No JNDI name specified");
			targetSource.setJndiName(jndiName);
			targetSource.setExpectedType(jof.getExpectedType());
			targetSource.setResourceRef(jof.isResourceRef());
			targetSource.setLookupOnStartup(jof.lookupOnStartup);
			targetSource.setCache(jof.cache);
			targetSource.afterPropertiesSet();

			// Create a proxy with JndiObjectFactoryBean's proxy interface and the JndiObjectTargetSource.
			ProxyFactory proxyFactory = new ProxyFactory();
			if (jof.proxyInterfaces != null) {
				proxyFactory.setInterfaces(jof.proxyInterfaces);
			}
			else {
				Class<?> targetClass = targetSource.getTargetClass();
				if (targetClass == null) {
					throw new IllegalStateException(
							"Cannot deactivate 'lookupOnStartup' without specifying a 'proxyInterface' or 'expectedType'");
				}
				Class<?>[] ifcs = ClassUtils.getAllInterfacesForClass(targetClass, jof.beanClassLoader);
				for (Class<?> ifc : ifcs) {
					if (Modifier.isPublic(ifc.getModifiers())) {
						proxyFactory.addInterface(ifc);
					}
				}
			}
			if (jof.exposeAccessContext) {
				proxyFactory.addAdvice(new JndiContextExposingInterceptor(jof.getJndiTemplate()));
			}
			proxyFactory.setTargetSource(targetSource);
			return proxyFactory.getProxy(jof.beanClassLoader);
		}
	}


	/**
	 * 根据 JndiObjectFactoryBean 的 {@code exposeAccessContext} 标志，
	 * 为全部方法调用暴露 JNDI 上下文的拦截器。
	 */
	private static class JndiContextExposingInterceptor implements MethodInterceptor {

		private final JndiTemplate jndiTemplate;

		public JndiContextExposingInterceptor(JndiTemplate jndiTemplate) {
			this.jndiTemplate = jndiTemplate;
		}

		@Override
		public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {
			Context ctx = (isEligible(invocation.getMethod()) ? this.jndiTemplate.getContext() : null);
			try {
				return invocation.proceed();
			}
			finally {
				this.jndiTemplate.releaseContext(ctx);
			}
		}

		protected boolean isEligible(Method method) {
			return (Object.class != method.getDeclaringClass());
		}
	}

}
