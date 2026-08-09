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

package org.springframework.beans.factory.aot;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.function.ThrowingConsumer;

/**
 * 支持方法自动装配的解析器。通常在 AOT 处理后的应用中作为
 * {@link org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 * AutowiredAnnotationBeanPostProcessor} 的定向替代方案使用。
 *
 * <p>在原生镜像中解析参数时，所用 {@link Method} 必须标记
 * {@link ExecutableMode#INTROSPECT 内省} 提示，以便读取方法注解。
 * 仅当使用本类的 {@link #resolveAndInvoke(RegisteredBean, Object)} 方法时
 * （通常用于支持私有方法），才需要完整的 {@link ExecutableMode#INVOKE 调用} 提示。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
public final class AutowiredMethodArgumentsResolver extends AutowiredElementResolver {

	/** 方法名称。 */
	private final String methodName;

	/** 方法参数类型。 */
	private final Class<?>[] parameterTypes;

	/** 是否必须注入。 */
	private final boolean required;

	/** 各参数的快捷 bean 名称（若有）。 */
	private final String @Nullable [] shortcutBeanNames;


	private AutowiredMethodArgumentsResolver(String methodName, Class<?>[] parameterTypes,
			boolean required, String @Nullable [] shortcutBeanNames) {

		Assert.hasText(methodName, "'methodName' must not be empty");
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.required = required;
		this.shortcutBeanNames = shortcutBeanNames;
	}


	/**
	 * 为指定方法创建新的 {@link AutowiredMethodArgumentsResolver}，注入为可选。
	 * @param methodName 方法名
	 * @param parameterTypes 工厂方法参数类型
	 * @return 新的 {@link AutowiredFieldValueResolver} 实例
	 */
	public static AutowiredMethodArgumentsResolver forMethod(String methodName, Class<?>... parameterTypes) {
		return new AutowiredMethodArgumentsResolver(methodName, parameterTypes, false, null);
	}

	/**
	 * 为指定方法创建新的 {@link AutowiredMethodArgumentsResolver}，注入为必须。
	 * @param methodName 方法名
	 * @param parameterTypes 工厂方法参数类型
	 * @return 新的 {@link AutowiredFieldValueResolver} 实例
	 */
	public static AutowiredMethodArgumentsResolver forRequiredMethod(String methodName, Class<?>... parameterTypes) {
		return new AutowiredMethodArgumentsResolver(methodName, parameterTypes, true, null);
	}

	/**
	 * 返回为特定参数使用直接 bean 名称注入快捷方式的新
	 * {@link AutowiredMethodArgumentsResolver} 实例。
	 * @param beanNames 用作快捷方式的 bean 名称（与方法参数对齐）
	 * @return 使用给定快捷 bean 名称的新 {@link AutowiredMethodArgumentsResolver} 实例
	 */
	public AutowiredMethodArgumentsResolver withShortcut(String... beanNames) {
		return new AutowiredMethodArgumentsResolver(this.methodName, this.parameterTypes, this.required, beanNames);
	}

	/**
	 * 为指定已注册 bean 解析方法参数，并将结果提供给给定操作。
	 * @param registeredBean 已注册 bean
	 * @param action 接收解析后方法参数的操作
	 */
	public void resolve(RegisteredBean registeredBean, ThrowingConsumer<AutowiredArguments> action) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		Assert.notNull(action, "'action' must not be null");
		AutowiredArguments resolved = resolve(registeredBean);
		if (resolved != null) {
			action.accept(resolved);
		}
	}

	/**
	 * 为指定已注册 bean 解析方法参数。
	 * @param registeredBean 已注册 bean
	 * @return 解析后的方法参数
	 */
	public @Nullable AutowiredArguments resolve(RegisteredBean registeredBean) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		return resolveArguments(registeredBean, getMethod(registeredBean));
	}

	/**
	 * 为指定已注册 bean 解析方法参数，并通过反射调用该方法。
	 * @param registeredBean 已注册 bean
	 * @param instance bean 实例
	 */
	public void resolveAndInvoke(RegisteredBean registeredBean, Object instance) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		Assert.notNull(instance, "'instance' must not be null");
		Method method = getMethod(registeredBean);
		AutowiredArguments resolved = resolveArguments(registeredBean, method);
		if (resolved != null) {
			ReflectionUtils.makeAccessible(method);
			ReflectionUtils.invokeMethod(method, instance, resolved.toArray());
		}
	}

	private @Nullable AutowiredArguments resolveArguments(RegisteredBean registeredBean,
			Method method) {

		String beanName = registeredBean.getBeanName();
		Class<?> beanClass = registeredBean.getBeanClass();
		ConfigurableBeanFactory beanFactory = registeredBean.getBeanFactory();
		Assert.isInstanceOf(AutowireCapableBeanFactory.class, beanFactory);
		AutowireCapableBeanFactory autowireCapableBeanFactory = (AutowireCapableBeanFactory) beanFactory;
		int argumentCount = method.getParameterCount();
		@Nullable Object[] arguments = new Object[argumentCount];
		Set<String> autowiredBeanNames = CollectionUtils.newLinkedHashSet(argumentCount);
		TypeConverter typeConverter = beanFactory.getTypeConverter();
		// 逐个解析每个方法参数
		for (int i = 0; i < argumentCount; i++) {
			MethodParameter parameter = new MethodParameter(method, i);
			DependencyDescriptor descriptor = new DependencyDescriptor(parameter, this.required);
			descriptor.setContainingClass(beanClass);
			String shortcut = (this.shortcutBeanNames != null ? this.shortcutBeanNames[i] : null);
			if (shortcut != null) {
				descriptor = new ShortcutDependencyDescriptor(descriptor, shortcut);
			}
			try {
				Object argument = autowireCapableBeanFactory.resolveDependency(
						descriptor, beanName, autowiredBeanNames, typeConverter);
				if (argument == null && !this.required) {
					return null;
				}
				arguments[i] = argument;
			}
			catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(parameter), ex);
			}
		}
		registerDependentBeans(beanFactory, beanName, autowiredBeanNames);
		return AutowiredArguments.of(arguments);
	}

	private Method getMethod(RegisteredBean registeredBean) {
		Method method = ReflectionUtils.findMethod(registeredBean.getBeanClass(),
				this.methodName, this.parameterTypes);
		Assert.notNull(method, () ->
				"Method '%s' with parameter types [%s] declared on %s could not be found.".formatted(
						this.methodName, toCommaSeparatedNames(this.parameterTypes),
						registeredBean.getBeanClass().getName()));
		return method;
	}

	private String toCommaSeparatedNames(Class<?>... parameterTypes) {
		return Arrays.stream(parameterTypes).map(Class::getName)
				.collect(Collectors.joining(", "));
	}

}
