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

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 在 {@link BeanFactory} 中使用的简单对象实例化策略。
 *
 * <p>不支持方法注入（Method Injection），但为子类提供了可覆盖的钩子，
 * 以便通过覆盖方法等方式添加方法注入支持。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 1.1
 */
public class SimpleInstantiationStrategy implements InstantiationStrategy {

	/** 当前正在调用的工厂方法，用于区分容器调用与用户代码调用。 */
	private static final ThreadLocal<Method> currentlyInvokedFactoryMethod = new ThreadLocal<>();


	/**
	 * 返回当前正在调用的工厂方法；若无则返回 {@code null}。
	 * <p>允许工厂方法实现判断当前调用方是容器本身还是用户代码。
	 */
	public static @Nullable Method getCurrentlyInvokedFactoryMethod() {
		return currentlyInvokedFactoryMethod.get();
	}

	/**
	 * 在将指定工厂方法标记为"正在调用"的上下文中，执行给定的 {@code instanceSupplier}。
	 * @param method 要暴露的工厂方法
	 * @param instanceSupplier 实例供应器
	 * @param <T> 实例类型
	 * @return 实例供应器的执行结果
	 * @since 6.2
	 */
	public static <T> T instantiateWithFactoryMethod(Method method, Supplier<T> instanceSupplier) {
		Method priorInvokedFactoryMethod = currentlyInvokedFactoryMethod.get();
		try {
			currentlyInvokedFactoryMethod.set(method);
			return instanceSupplier.get();
		}
		finally {
			if (priorInvokedFactoryMethod != null) {
				currentlyInvokedFactoryMethod.set(priorInvokedFactoryMethod);
			}
			else {
				currentlyInvokedFactoryMethod.remove();
			}
		}
	}


	@Override
	public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
		// 无方法覆盖时，无需用 CGLIB 替换类
		if (!bd.hasMethodOverrides()) {
			Constructor<?> constructorToUse;
			synchronized (bd.constructorArgumentLock) {
				constructorToUse = (Constructor<?>) bd.resolvedConstructorOrFactoryMethod;
				if (constructorToUse == null) {
					Class<?> clazz = bd.getBeanClass();
					if (clazz.isInterface()) {
						throw new BeanInstantiationException(clazz, "Specified class is an interface");
					}
					try {
						// 解析并缓存无参构造器
						constructorToUse = clazz.getDeclaredConstructor();
						bd.resolvedConstructorOrFactoryMethod = constructorToUse;
					}
					catch (Throwable ex) {
						throw new BeanInstantiationException(clazz, "No default constructor found", ex);
					}
				}
			}
			return BeanUtils.instantiateClass(constructorToUse);
		}
		else {
			// 存在方法覆盖，必须生成 CGLIB 子类
			return instantiateWithMethodInjection(bd, beanName, owner);
		}
	}

	/**
	 * 子类可覆盖此方法：若支持按给定 {@link RootBeanDefinition} 中的方法注入来实例化对象，
	 * 则提供实现。实例化应使用无参构造器。
	 * <p>默认实现抛出 {@link UnsupportedOperationException}。
	 */
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}

	@Override
	public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			Constructor<?> ctor, Object... args) {

		if (!bd.hasMethodOverrides()) {
			return BeanUtils.instantiateClass(ctor, args);
		}
		else {
			return instantiateWithMethodInjection(bd, beanName, owner, ctor, args);
		}
	}

	/**
	 * 子类可覆盖此方法：若支持按给定 {@link RootBeanDefinition} 中的方法注入来实例化对象，
	 * 则提供实现。实例化应使用指定构造器及参数。
	 * <p>默认实现抛出 {@link UnsupportedOperationException}。
	 */
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName,
			BeanFactory owner, @Nullable Constructor<?> ctor, Object... args) {

		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}

	@Override
	public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			@Nullable Object factoryBean, Method factoryMethod, @Nullable Object... args) {

		return instantiateWithFactoryMethod(factoryMethod, () -> {
			try {
				ReflectionUtils.makeAccessible(factoryMethod);
				Object result = factoryMethod.invoke(factoryBean, args);
				// 工厂方法返回 null 时用 NullBean 占位
				if (result == null) {
					result = new NullBean();
				}
				return result;
			}
			catch (IllegalArgumentException ex) {
				if (factoryBean != null && !factoryMethod.getDeclaringClass().isAssignableFrom(factoryBean.getClass())) {
					throw new BeanInstantiationException(factoryMethod,
							"Illegal factory instance for factory method '" + factoryMethod.getName() + "'; " +
									"instance: " + factoryBean.getClass().getName(), ex);
				}
				throw new BeanInstantiationException(factoryMethod,
						"Illegal arguments to factory method '" + factoryMethod.getName() + "'; " +
								"args: " + StringUtils.arrayToCommaDelimitedString(args), ex);
			}
			catch (IllegalAccessException | InaccessibleObjectException ex) {
				throw new BeanInstantiationException(factoryMethod,
						"Cannot access factory method '" + factoryMethod.getName() + "'; is it public?", ex);
			}
			catch (InvocationTargetException ex) {
				String msg = "Factory method '" + factoryMethod.getName() + "' threw exception with message: " +
						ex.getTargetException().getMessage();
				// 检测工厂 Bean 循环依赖，提示改为 static 工厂方法
				if (bd.getFactoryBeanName() != null && owner instanceof ConfigurableBeanFactory cbf &&
						cbf.isCurrentlyInCreation(bd.getFactoryBeanName())) {
					msg = "Circular reference involving containing bean '" + bd.getFactoryBeanName() + "' - consider " +
							"declaring the factory method as static for independence from its containing instance. " + msg;
				}
				throw new BeanInstantiationException(factoryMethod, msg, ex.getTargetException());
			}
		});
	}

}
