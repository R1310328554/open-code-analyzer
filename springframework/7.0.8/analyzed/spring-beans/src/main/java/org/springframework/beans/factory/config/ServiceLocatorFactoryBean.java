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

package org.springframework.beans.factory.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@link FactoryBean} 实现：接受一个接口，该接口须有一个或多个签名为
 * {@code MyType xxx()} 或 {@code MyType xxx(MyIdType id)} 的方法
 * （通常为 {@code MyService getService()} 或 {@code MyService getService(String id)}），
 * 并创建实现该接口的动态代理，委托给底层
 * {@link org.springframework.beans.factory.BeanFactory}。
 *
 * <p>此类服务定位器使调用代码与
 * {@link org.springframework.beans.factory.BeanFactory} API 解耦，
 * 通过合适的自定义定位器接口实现。通常用于<b>原型 Bean</b>，
 * 即每次调用应返回新实例的工厂方法。客户端通过 setter 或构造器注入
 * 获得服务定位器引用，以便按需调用定位器的工厂方法。
 * <b>对于单例 Bean，直接通过 setter 或构造器注入目标 Bean 更为合适。</b>
 *
 * <p>调用无参工厂方法，或传入 {@code null} 或空字符串的 String id 的单参工厂方法时，
 * 若工厂中<b>恰好有一个</b> Bean 与工厂方法的返回类型匹配，则返回该 Bean；
 * 否则抛出 {@link org.springframework.beans.factory.NoSuchBeanDefinitionException}。
 *
 * <p>调用传入非 null（且非空）参数的单参工厂方法时，代理将返回
 * {@link org.springframework.beans.factory.BeanFactory#getBean(String)} 调用的结果，
 * 使用传入 id 的字符串形式作为 Bean 名称。
 *
 * <p>工厂方法参数通常为 String，但也可以是 int 或自定义枚举类型，
 * 例如通过 {@code toString} 字符串化。只要 Bean 工厂中定义了对应 Bean，
 * 所得字符串即可直接用作 Bean 名称。也可通过
 * {@linkplain #setServiceMappings(java.util.Properties) 自定义映射}
 * 定义服务 ID 与 Bean 名称之间的对应关系。
 *
 * <p>以下示例展示服务定位器接口。注意该接口不依赖任何 Spring API。
 *
 * <pre class="code">package a.b.c;
 *
 *public interface ServiceFactory {
 *
 *    public MyService getService();
 *}</pre>
 *
 * <p>基于 XML 的 {@link org.springframework.beans.factory.BeanFactory} 配置示例如下：
 *
 * <pre class="code">&lt;beans&gt;
 *
 *   &lt;!-- Prototype bean since we have state --&gt;
 *   &lt;bean id="myService" class="a.b.c.MyService" singleton="false"/&gt;
 *
 *   &lt;!-- will lookup the above 'myService' bean by *TYPE* --&gt;
 *   &lt;bean id="myServiceFactory"
 *            class="org.springframework.beans.factory.config.ServiceLocatorFactoryBean"&gt;
 *     &lt;property name="serviceLocatorInterface" value="a.b.c.ServiceFactory"/&gt;
 *   &lt;/bean&gt;
 *
 *   &lt;bean id="clientBean" class="a.b.c.MyClientBean"&gt;
 *     &lt;property name="myServiceFactory" ref="myServiceFactory"/&gt;
 *   &lt;/bean&gt;
 *
 *&lt;/beans&gt;</pre>
 *
 * <p>配套的 {@code MyClientBean} 类实现可能如下：
 *
 * <pre class="code">package a.b.c;
 *
 *public class MyClientBean {
 *
 *    private ServiceFactory myServiceFactory;
 *
 *    // actual implementation provided by the Spring container
 *    public void setServiceFactory(ServiceFactory myServiceFactory) {
 *        this.myServiceFactory = myServiceFactory;
 *    }
 *
 *    public void someBusinessMethod() {
 *        // get a 'fresh', brand new MyService instance
 *        MyService service = this.myServiceFactory.getService();
 *        // use the service object to effect the business logic...
 *    }
 *}</pre>
 *
 * <p>以下示例按<b>名称</b>查找 Bean，展示另一种服务定位器接口。
 * 同样，该接口不依赖任何 Spring API。
 *
 * <pre class="code">package a.b.c;
 *
 *public interface ServiceFactory {
 *
 *    public MyService getService (String serviceName);
 *}</pre>
 *
 * <p>基于 XML 的 {@link org.springframework.beans.factory.BeanFactory} 配置示例如下：
 *
 * <pre class="code">&lt;beans&gt;
 *
 *   &lt;!-- Prototype beans since we have state (both extend MyService) --&gt;
 *   &lt;bean id="specialService" class="a.b.c.SpecialService" singleton="false"/&gt;
 *   &lt;bean id="anotherService" class="a.b.c.AnotherService" singleton="false"/&gt;
 *
 *   &lt;bean id="myServiceFactory"
 *            class="org.springframework.beans.factory.config.ServiceLocatorFactoryBean"&gt;
 *     &lt;property name="serviceLocatorInterface" value="a.b.c.ServiceFactory"/&gt;
 *   &lt;/bean&gt;
 *
 *   &lt;bean id="clientBean" class="a.b.c.MyClientBean"&gt;
 *     &lt;property name="myServiceFactory" ref="myServiceFactory"/&gt;
 *   &lt;/bean&gt;
 *
 *&lt;/beans&gt;</pre>
 *
 * <p>配套的 {@code MyClientBean} 类实现可能如下：
 *
 * <pre class="code">package a.b.c;
 *
 *public class MyClientBean {
 *
 *    private ServiceFactory myServiceFactory;
 *
 *    // actual implementation provided by the Spring container
 *    public void setServiceFactory(ServiceFactory myServiceFactory) {
 *        this.myServiceFactory = myServiceFactory;
 *    }
 *
 *    public void someBusinessMethod() {
 *        // get a 'fresh', brand new MyService instance
 *        MyService service = this.myServiceFactory.getService("specialService");
 *        // use the service object to effect the business logic...
 *    }
 *
 *    public void anotherBusinessMethod() {
 *        // get a 'fresh', brand new MyService instance
 *        MyService service = this.myServiceFactory.getService("anotherService");
 *        // use the service object to effect the business logic...
 *    }
 *}</pre>
 *
 * <p>另见 {@link ObjectFactoryCreatingFactoryBean} 作为替代方案。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.1.4
 * @see #setServiceLocatorInterface
 * @see #setServiceMappings
 * @see ObjectFactoryCreatingFactoryBean
 */
public class ServiceLocatorFactoryBean implements FactoryBean<Object>, BeanFactoryAware, InitializingBean {

	/** 服务定位器接口类型 */
	private @Nullable Class<?> serviceLocatorInterface;

	/** 自定义服务定位器异常的构造器 */
	private @Nullable Constructor<Exception> serviceLocatorExceptionConstructor;

	/** 服务 ID 到 Bean 名称的映射 */
	private @Nullable Properties serviceMappings;

	/** 底层可列举的 BeanFactory */
	private @Nullable ListableBeanFactory beanFactory;

	/** 动态代理对象 */
	private @Nullable Object proxy;


	/**
	 * 设置要使用的服务定位器接口，该接口须有一个或多个签名为
	 * {@code MyType xxx()} 或 {@code MyType xxx(MyIdType id)} 的方法
	 * （通常为 {@code MyService getService()} 或 {@code MyService getService(String id)}）。
	 * 有关此类方法语义的说明，请参见 {@link ServiceLocatorFactoryBean 类级 Javadoc}。
	 */
	public void setServiceLocatorInterface(Class<?> interfaceType) {
		this.serviceLocatorInterface = interfaceType;
	}

	/**
	 * 设置服务查找失败时服务定位器应抛出的异常类。
	 * 指定的异常类须具有以下参数类型之一的构造器：
	 * {@code (String, Throwable)}、{@code (Throwable)} 或 {@code (String)}。
	 * <p>若未指定，将抛出 Spring BeansException 的子类，例如 NoSuchBeanDefinitionException。
	 * 由于这些是未检查异常，调用方无需处理；只要泛化处理，
	 * 抛出 Spring 异常通常也可接受。
	 * @see #determineServiceLocatorExceptionConstructor
	 * @see #createServiceLocatorException
	 */
	public void setServiceLocatorExceptionClass(Class<? extends Exception> serviceLocatorExceptionClass) {
		this.serviceLocatorExceptionConstructor =
				determineServiceLocatorExceptionConstructor(serviceLocatorExceptionClass);
	}

	/**
	 * 设置服务 ID（传入服务定位器）与 Bean 名称（在 Bean 工厂中）之间的映射。
	 * 此处未定义的服务 ID 将直接作为 Bean 名称处理。
	 * <p>以空字符串作为服务 ID 键定义 {@code null} 和空字符串以及无参工厂方法的映射。
	 * 若未定义，将从 Bean 工厂获取唯一匹配的 Bean。
	 * @param serviceMappings 服务 ID 到 Bean 名称的映射，以服务 ID 为键、Bean 名称为值
	 */
	public void setServiceMappings(Properties serviceMappings) {
		this.serviceMappings = serviceMappings;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ListableBeanFactory lbf)) {
			throw new FatalBeanException(
					"ServiceLocatorFactoryBean needs to run in a BeanFactory that is a ListableBeanFactory");
		}
		this.beanFactory = lbf;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.serviceLocatorInterface == null) {
			throw new IllegalArgumentException("Property 'serviceLocatorInterface' is required");
		}

		// 创建服务定位器代理
		this.proxy = Proxy.newProxyInstance(
				this.serviceLocatorInterface.getClassLoader(),
				new Class<?>[] {this.serviceLocatorInterface},
				new ServiceLocatorInvocationHandler());
	}


	/**
	 * 确定给定服务定位器异常类应使用的构造器。仅在自定义服务定位器异常时调用。
	 * <p>默认实现查找具有以下参数类型之一的构造器：
	 * {@code (String, Throwable)}、{@code (Throwable)} 或 {@code (String)}。
	 * @param exceptionClass 异常类
	 * @return 要使用的构造器
	 * @see #setServiceLocatorExceptionClass
	 */
	@SuppressWarnings("unchecked")
	protected Constructor<Exception> determineServiceLocatorExceptionConstructor(Class<? extends Exception> exceptionClass) {
		try {
			return (Constructor<Exception>) exceptionClass.getConstructor(String.class, Throwable.class);
		}
		catch (NoSuchMethodException ex) {
			try {
				return (Constructor<Exception>) exceptionClass.getConstructor(Throwable.class);
			}
			catch (NoSuchMethodException ex2) {
				try {
					return (Constructor<Exception>) exceptionClass.getConstructor(String.class);
				}
				catch (NoSuchMethodException ex3) {
					throw new IllegalArgumentException(
							"Service locator exception [" + exceptionClass.getName() +
							"] neither has a (String, Throwable) constructor nor a (String) constructor");
				}
			}
		}
	}

	/**
	 * 为给定原因创建服务定位器异常。仅在自定义服务定位器异常时调用。
	 * <p>默认实现可处理消息和异常参数的所有变体。
	 * @param exceptionConstructor 要使用的构造器
	 * @param cause 服务查找失败的原因
	 * @return 要抛出的服务定位器异常
	 * @see #setServiceLocatorExceptionClass
	 */
	protected Exception createServiceLocatorException(Constructor<Exception> exceptionConstructor, BeansException cause) {
		Class<?>[] paramTypes = exceptionConstructor.getParameterTypes();
		@Nullable Object[] args = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			if (String.class == paramTypes[i]) {
				args[i] = cause.getMessage();
			}
			else if (paramTypes[i].isInstance(cause)) {
				args[i] = cause;
			}
		}
		return BeanUtils.instantiateClass(exceptionConstructor, args);
	}


	@Override
	public @Nullable Object getObject() {
		return this.proxy;
	}

	@Override
	public @Nullable Class<?> getObjectType() {
		return this.serviceLocatorInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}


	/**
	 * 将服务定位器调用委托给 Bean 工厂的调用处理器。
	 */
	private class ServiceLocatorInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (ReflectionUtils.isEqualsMethod(method)) {
				// 仅在代理相同时才认为相等
				return (proxy == args[0]);
			}
			else if (ReflectionUtils.isHashCodeMethod(method)) {
				// 使用服务定位器代理的 hashCode
				return System.identityHashCode(proxy);
			}
			else if (ReflectionUtils.isToStringMethod(method)) {
				return "Service locator: " + serviceLocatorInterface;
			}
			else {
				return invokeServiceLocatorMethod(method, args);
			}
		}

		private Object invokeServiceLocatorMethod(Method method, Object[] args) throws Exception {
			Class<?> serviceLocatorMethodReturnType = getServiceLocatorMethodReturnType(method);
			try {
				String beanName = tryGetBeanName(args);
				Assert.state(beanFactory != null, "No BeanFactory available");
				if (StringUtils.hasLength(beanName)) {
					// 按特定 Bean 名称定位服务
					return beanFactory.getBean(beanName, serviceLocatorMethodReturnType);
				}
				else {
					// 按 Bean 类型定位服务
					return beanFactory.getBean(serviceLocatorMethodReturnType);
				}
			}
			catch (BeansException ex) {
				if (serviceLocatorExceptionConstructor != null) {
					throw createServiceLocatorException(serviceLocatorExceptionConstructor, ex);
				}
				throw ex;
			}
		}

		/**
		 * 检查是否传入了服务 ID。
		 */
		private String tryGetBeanName(Object @Nullable [] args) {
			String beanName = "";
			if (args != null && args.length == 1 && args[0] != null) {
				beanName = args[0].toString();
			}
			// 查找显式的 serviceId 到 beanName 映射
			if (serviceMappings != null) {
				String mappedName = serviceMappings.getProperty(beanName);
				if (mappedName != null) {
					beanName = mappedName;
				}
			}
			return beanName;
		}

		private Class<?> getServiceLocatorMethodReturnType(Method method) throws NoSuchMethodException {
			Assert.state(serviceLocatorInterface != null, "No service locator interface specified");
			Class<?>[] paramTypes = method.getParameterTypes();
			Method interfaceMethod = serviceLocatorInterface.getMethod(method.getName(), paramTypes);
			Class<?> serviceLocatorReturnType = interfaceMethod.getReturnType();

			// 检查方法是否为有效的服务定位器方法
			if (paramTypes.length > 1 || void.class == serviceLocatorReturnType) {
				throw new UnsupportedOperationException(
						"May only call methods with signature '<type> xxx()' or '<type> xxx(<idtype> id)' " +
						"on factory interface, but tried to call: " + interfaceMethod);
			}
			return serviceLocatorReturnType;
		}
	}

}
