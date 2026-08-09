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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * {@link FactoryBean} 实现的简单模板超类，根据标志位创建单例或原型对象。
 *
 * <p>若 "singleton" 标志为 {@code true}（默认值），
 * 此类将在初始化时恰好创建一次对象，随后在每次调用 {@link #getObject()} 方法时
 * 返回该单例实例。
 *
 * <p>否则，每次调用 {@link #getObject()} 方法时都会创建新实例。
 * 子类负责实现抽象模板方法 {@link #createInstance()} 以实际创建要暴露的对象。
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @since 1.0.2
 * @param <T> bean 类型
 * @see #setSingleton
 * @see #createInstance()
 */
public abstract class AbstractFactoryBean<T>
		implements FactoryBean<T>, BeanClassLoaderAware, BeanFactoryAware, InitializingBean, DisposableBean {

	/** 子类可用的日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 是否创建单例（默认为 true）。 */
	private boolean singleton = true;

	/** bean 类加载器。 */
	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/** 运行所在的 BeanFactory。 */
	private @Nullable BeanFactory beanFactory;

	/** 单例是否已完成初始化。 */
	private boolean initialized = false;

	@SuppressWarnings("NullAway.Init")
	/** 单例实例。 */
	private T singletonInstance;

	/** 早期单例代理实例（用于循环引用场景）。 */
	private @Nullable T earlySingletonInstance;


	/**
	 * 设置是否创建单例；否则每次请求创建新对象。
	 * 默认为 {@code true}（单例）。
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	@Override
	public boolean isSingleton() {
		return this.singleton;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	@Override
	public void setBeanFactory(@Nullable BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * 返回此 bean 运行所在的 BeanFactory。
	 */
	protected @Nullable BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * 从此 bean 运行所在的 BeanFactory 获取类型转换器。
	 * 通常每次调用返回新实例，因为 TypeConverter 通常<i>不是</i>线程安全的。
	 * <p>若不在 BeanFactory 中运行，则回退到 SimpleTypeConverter。
	 * @see ConfigurableBeanFactory#getTypeConverter()
	 * @see org.springframework.beans.SimpleTypeConverter
	 */
	protected TypeConverter getBeanTypeConverter() {
		BeanFactory beanFactory = getBeanFactory();
		if (beanFactory instanceof ConfigurableBeanFactory cbf) {
			return cbf.getTypeConverter();
		}
		else {
			return new SimpleTypeConverter();
		}
	}

	/**
	 * 必要时急切创建单例实例。
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (isSingleton()) {
			this.initialized = true;
			this.singletonInstance = createInstance();
			this.earlySingletonInstance = null;
		}
	}


	/**
	 * 暴露单例实例，或创建新的原型实例。
	 * @see #createInstance()
	 * @see #getEarlySingletonInterfaces()
	 */
	@Override
	public final T getObject() throws Exception {
		if (isSingleton()) {
			return (this.initialized ? this.singletonInstance : getEarlySingletonInstance());
		}
		else {
			return createInstance();
		}
	}

	/**
	 * 确定"早期单例"实例，在循环引用场景下暴露。
	 * 非循环场景下不会调用。
	 */
	@SuppressWarnings("unchecked")
	private T getEarlySingletonInstance() throws Exception {
		Class<?>[] ifcs = getEarlySingletonInterfaces();
		if (ifcs == null) {
			throw new FactoryBeanNotInitializedException(
					getClass().getName() + " does not support circular references");
		}
		if (this.earlySingletonInstance == null) {
			// 创建 JDK 动态代理作为早期单例占位
			this.earlySingletonInstance = (T) Proxy.newProxyInstance(
					this.beanClassLoader, ifcs, new EarlySingletonInvocationHandler());
		}
		return this.earlySingletonInstance;
	}

	/**
	 * 暴露单例实例（供"早期单例"代理访问）。
	 * @return 此 FactoryBean 持有的单例实例
	 * @throws IllegalStateException 若单例实例尚未初始化
	 */
	private @Nullable T getSingletonInstance() throws IllegalStateException {
		Assert.state(this.initialized, "Singleton instance not initialized yet");
		return this.singletonInstance;
	}

	/**
	 * 销毁单例实例（如有）。
	 * @see #destroyInstance(Object)
	 */
	@Override
	public void destroy() throws Exception {
		if (isSingleton()) {
			destroyInstance(this.singletonInstance);
		}
	}


	/**
	 * 此抽象方法声明与 FactoryBean 接口中的方法对应，
	 * 以提供一致的抽象模板方法。
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public abstract @Nullable Class<?> getObjectType();

	/**
	 * 子类必须覆盖的模板方法，用于构造此工厂返回的对象。
	 * <p>对于单例，在 FactoryBean 初始化时调用；否则在每次 {@link #getObject()} 调用时调用。
	 * @return 此工厂返回的对象
	 * @throws Exception 对象创建过程中发生异常
	 * @see #getObject()
	 */
	protected abstract T createInstance() throws Exception;

	/**
	 * 返回此 FactoryBean 暴露的单例对象应实现的接口数组，
	 * 用于循环引用场景下的"早期单例代理"。
	 * <p>默认实现返回此 FactoryBean 的对象类型（若为接口），否则返回 {@code null}。
	 * 后者表示此 FactoryBean 不支持早期单例访问，将抛出 FactoryBeanNotInitializedException。
	 * @return 用于"早期单例"的接口，或 {@code null} 表示将抛出 FactoryBeanNotInitializedException
	 * @see org.springframework.beans.factory.FactoryBeanNotInitializedException
	 */
	protected Class<?> @Nullable [] getEarlySingletonInterfaces() {
		Class<?> type = getObjectType();
		return (type != null && type.isInterface() ? new Class<?>[] {type} : null);
	}

	/**
	 * 销毁单例实例的回调。子类可覆盖以销毁先前创建的实例。
	 * <p>默认实现为空。
	 * @param instance 单例实例，由 {@link #createInstance()} 返回
	 * @throws Exception 关闭过程中发生错误
	 * @see #createInstance()
	 */
	protected void destroyInstance(@Nullable T instance) throws Exception {
	}


	/**
	 * 用于延迟访问实际单例对象的反射 InvocationHandler。
	 */
	private class EarlySingletonInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (ReflectionUtils.isEqualsMethod(method)) {
				// 仅当代理对象相同时才认为相等
				return (proxy == args[0]);
			}
			else if (ReflectionUtils.isHashCodeMethod(method)) {
				// 使用代理引用的 hashCode
				return System.identityHashCode(proxy);
			}
			else if (!initialized && ReflectionUtils.isToStringMethod(method)) {
				return "Early singleton proxy for interfaces " +
						ObjectUtils.nullSafeToString(getEarlySingletonInterfaces());
			}
			try {
				// 委托给实际单例实例
				return method.invoke(getSingletonInstance(), args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
