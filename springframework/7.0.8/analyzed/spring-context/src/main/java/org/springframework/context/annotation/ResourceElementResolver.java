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

package org.springframework.context.annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 按 {@link jakarta.annotation.Resource} 注解规则（不含 JNDI 支持），
 * 解析字段或方法元素上命名 Bean 注入的解析器。主要用于 AOT 处理。
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 6.1.2
 * @see CommonAnnotationBeanPostProcessor
 * @see jakarta.annotation.Resource
 */
public abstract class ResourceElementResolver {

	/** 资源（Bean）名称。 */
	private final String name;

	/** 是否使用默认名称（字段名或 setter 推导名）进行按类型解析。 */
	private final boolean defaultName;


	ResourceElementResolver(String name, boolean defaultName) {
		this.name = name;
		this.defaultName = defaultName;
	}


	/**
	 * 为指定字段创建 {@link ResourceFieldResolver}。
	 * @param fieldName 字段名
	 * @return 新的 {@link ResourceFieldResolver} 实例
	 */
	public static ResourceElementResolver forField(String fieldName) {
		return new ResourceFieldResolver(fieldName, true, fieldName);
	}

	/**
	 * 为指定字段与资源名创建 {@link ResourceFieldResolver}。
	 * @param fieldName 字段名
	 * @param resourceName 资源名
	 * @return 新的 {@link ResourceFieldResolver} 实例
	 */
	public static ResourceElementResolver forField(String fieldName, String resourceName) {
		return new ResourceFieldResolver(resourceName, false, fieldName);
	}

	/**
	 * 为指定方法创建 {@link ResourceMethodResolver}，资源名由方法名推导。
	 * @param methodName 方法名
	 * @param parameterType 参数类型
	 * @return 新的 {@link ResourceMethodResolver} 实例
	 */
	public static ResourceElementResolver forMethod(String methodName, Class<?> parameterType) {
		return new ResourceMethodResolver(defaultResourceNameForMethod(methodName), true,
				methodName, parameterType);
	}

	/**
	 * 为指定方法与资源名创建 {@link ResourceMethodResolver}。
	 * @param methodName 方法名
	 * @param parameterType 参数类型
	 * @param resourceName 资源名
	 * @return 新的 {@link ResourceMethodResolver} 实例
	 */
	public static ResourceElementResolver forMethod(String methodName, Class<?> parameterType, String resourceName) {
		return new ResourceMethodResolver(resourceName, false, methodName, parameterType);
	}

	/** 从 setter 方法名推导默认资源名（如 setFoo → foo）。 */
	private static String defaultResourceNameForMethod(String methodName) {
		if (methodName.startsWith("set") && methodName.length() > 3) {
			return StringUtils.uncapitalizeAsProperty(methodName.substring(3));
		}
		return methodName;
	}


	/**
	 * 为指定已注册 Bean 解析注入值。
	 * @param registeredBean 已注册 Bean
	 * @return 解析得到的字段或方法参数值
	 */
	@SuppressWarnings("unchecked")
	public <T> @Nullable T resolve(RegisteredBean registeredBean) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		return (T) (isLazyLookup(registeredBean) ? buildLazyResourceProxy(registeredBean) :
				resolveValue(registeredBean));
	}

	/**
	 * 为指定已注册 Bean 解析值并通过反射设置。
	 * @param registeredBean 已注册 Bean
	 * @param instance Bean 实例
	 */
	public abstract void resolveAndSet(RegisteredBean registeredBean, Object instance);

	/**
	 * 为指定 Bean 创建合适的 {@link DependencyDescriptor}。
	 * @param registeredBean 已注册 Bean
	 * @return 该 Bean 的依赖描述符
	 */
	abstract DependencyDescriptor createDependencyDescriptor(RegisteredBean registeredBean);

	abstract Class<?> getLookupType(RegisteredBean registeredBean);

	abstract AnnotatedElement getAnnotatedElement(RegisteredBean registeredBean);

	/** 注入点是否标注了 {@code @Lazy(true)}。 */
	boolean isLazyLookup(RegisteredBean registeredBean) {
		AnnotatedElement ae = getAnnotatedElement(registeredBean);
		Lazy lazy = ae.getAnnotation(Lazy.class);
		return (lazy != null && lazy.value());
	}

	/** 为懒查找构建代理，首次访问时才解析目标 Bean。 */
	private Object buildLazyResourceProxy(RegisteredBean registeredBean) {
		Class<?> lookupType = getLookupType(registeredBean);

		TargetSource ts = new TargetSource() {
			@Override
			public Class<?> getTargetClass() {
				return lookupType;
			}
			@Override
			public Object getTarget() {
				return resolveValue(registeredBean);
			}
		};

		ProxyFactory pf = new ProxyFactory();
		pf.setTargetSource(ts);
		if (lookupType.isInterface()) {
			pf.addInterface(lookupType);
		}
		return pf.getProxy(registeredBean.getBeanFactory().getBeanClassLoader());
	}

	/**
	 * 解析本实例要注入的值。
	 * @param registeredBean Bean 注册信息
	 * @return 要注入的值
	 */
	private Object resolveValue(RegisteredBean registeredBean) {
		ConfigurableListableBeanFactory factory = registeredBean.getBeanFactory();

		Object resource;
		Set<String> autowiredBeanNames;
		DependencyDescriptor descriptor = createDependencyDescriptor(registeredBean);
		if (this.defaultName && !factory.containsBean(this.name)) {
			// 默认名称对应 Bean 不存在时，按类型解析
			autowiredBeanNames = new LinkedHashSet<>();
			resource = factory.resolveDependency(descriptor, registeredBean.getBeanName(), autowiredBeanNames, null);
			if (resource == null) {
				throw new NoSuchBeanDefinitionException(descriptor.getDependencyType(), "No resolvable resource object");
			}
		}
		else {
			// 按名称解析
			resource = factory.resolveBeanByName(this.name, descriptor);
			autowiredBeanNames = Collections.singleton(this.name);
		}

		// 登记依赖关系
		for (String autowiredBeanName : autowiredBeanNames) {
			if (factory.containsBean(autowiredBeanName)) {
				factory.registerDependentBean(autowiredBeanName, registeredBean.getBeanName());
			}
		}
		return resource;
	}


	/** 字段注入解析器。 */
	private static final class ResourceFieldResolver extends ResourceElementResolver {

		private final String fieldName;

		public ResourceFieldResolver(String name, boolean defaultName, String fieldName) {
			super(name, defaultName);
			this.fieldName = fieldName;
		}

		@Override
		public void resolveAndSet(RegisteredBean registeredBean, Object instance) {
			Assert.notNull(registeredBean, "'registeredBean' must not be null");
			Assert.notNull(instance, "'instance' must not be null");
			Field field = getField(registeredBean);
			Object resolved = resolve(registeredBean);
			ReflectionUtils.makeAccessible(field);
			ReflectionUtils.setField(field, instance, resolved);
		}

		@Override
		protected DependencyDescriptor createDependencyDescriptor(RegisteredBean registeredBean) {
			Field field = getField(registeredBean);
			return new LookupDependencyDescriptor(field, field.getType(), isLazyLookup(registeredBean));
		}

		@Override
		protected Class<?> getLookupType(RegisteredBean registeredBean) {
			return getField(registeredBean).getType();
		}

		@Override
		protected AnnotatedElement getAnnotatedElement(RegisteredBean registeredBean) {
			return getField(registeredBean);
		}

		private Field getField(RegisteredBean registeredBean) {
			Field field = ReflectionUtils.findField(registeredBean.getBeanClass(), this.fieldName);
			Assert.notNull(field,
					() -> "No field '" + this.fieldName + "' found on " + registeredBean.getBeanClass().getName());
			return field;
		}
	}


	/** 方法（setter）注入解析器。 */
	private static final class ResourceMethodResolver extends ResourceElementResolver {

		private final String methodName;

		private final Class<?> lookupType;

		private ResourceMethodResolver(String name, boolean defaultName, String methodName, Class<?> lookupType) {
			super(name, defaultName);
			this.methodName = methodName;
			this.lookupType = lookupType;
		}

		@Override
		public void resolveAndSet(RegisteredBean registeredBean, Object instance) {
			Assert.notNull(registeredBean, "'registeredBean' must not be null");
			Assert.notNull(instance, "'instance' must not be null");
			Method method = getMethod(registeredBean);
			Object resolved = resolve(registeredBean);
			ReflectionUtils.makeAccessible(method);
			ReflectionUtils.invokeMethod(method, instance, resolved);
		}

		@Override
		protected DependencyDescriptor createDependencyDescriptor(RegisteredBean registeredBean) {
			return new LookupDependencyDescriptor(
					getMethod(registeredBean), this.lookupType, isLazyLookup(registeredBean));
		}

		@Override
		protected Class<?> getLookupType(RegisteredBean bean) {
			return this.lookupType;
		}

		@Override
		protected AnnotatedElement getAnnotatedElement(RegisteredBean registeredBean) {
			return getMethod(registeredBean);
		}

		private Method getMethod(RegisteredBean registeredBean) {
			Method method = ReflectionUtils.findMethod(registeredBean.getBeanClass(), this.methodName, this.lookupType);
			Assert.notNull(method,
					() -> "Method '%s' with parameter type '%s' declared on %s could not be found.".formatted(
							this.methodName, this.lookupType.getName(), registeredBean.getBeanClass().getName()));
			return method;
		}
	}


	/**
	 * {@link DependencyDescriptor} 的扩展，用指定资源类型覆盖依赖类型。
	 */
	@SuppressWarnings("serial")
	static class LookupDependencyDescriptor extends DependencyDescriptor {

		private final Class<?> lookupType;

		private final boolean lazyLookup;

		public LookupDependencyDescriptor(Field field, Class<?> lookupType, boolean lazyLookup) {
			super(field, true);
			this.lookupType = lookupType;
			this.lazyLookup = lazyLookup;
		}

		public LookupDependencyDescriptor(Method method, Class<?> lookupType, boolean lazyLookup) {
			super(new MethodParameter(method, 0), true);
			this.lookupType = lookupType;
			this.lazyLookup = lazyLookup;
		}

		@Override
		public Class<?> getDependencyType() {
			return this.lookupType;
		}

		@Override
		public boolean supportsLazyResolution() {
			return !this.lazyLookup;
		}
	}

}
