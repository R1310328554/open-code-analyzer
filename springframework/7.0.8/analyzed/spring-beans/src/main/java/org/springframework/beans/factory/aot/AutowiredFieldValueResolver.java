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

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

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
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.function.ThrowingConsumer;

/**
 * 支持字段自动装配的解析器。通常在 AOT 处理后的应用中作为
 * {@link org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 * AutowiredAnnotationBeanPostProcessor} 的定向替代方案使用。
 *
 * <p>在原生镜像中解析参数时，所用 {@link Field} 必须标记
 * {@link ExecutableMode#INTROSPECT 内省} 提示，以便读取字段注解。
 * 仅当使用本类的 {@link #resolveAndSet(RegisteredBean, Object)} 方法时
 * （通常用于支持私有字段），才需要完整的 {@link ExecutableMode#INVOKE 调用} 提示。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
public final class AutowiredFieldValueResolver extends AutowiredElementResolver {

	/** 字段名称。 */
	private final String fieldName;

	/** 是否必须注入。 */
	private final boolean required;

	/** 快捷 bean 名称（若有）。 */
	private final @Nullable String shortcutBeanName;


	private AutowiredFieldValueResolver(String fieldName, boolean required, @Nullable String shortcut) {
		Assert.hasText(fieldName, "'fieldName' must not be empty");
		this.fieldName = fieldName;
		this.required = required;
		this.shortcutBeanName = shortcut;
	}


	/**
	 * 为指定字段创建新的 {@link AutowiredFieldValueResolver}，注入为可选。
	 * @param fieldName 字段名
	 * @return 新的 {@link AutowiredFieldValueResolver} 实例
	 */
	public static AutowiredFieldValueResolver forField(String fieldName) {
		return new AutowiredFieldValueResolver(fieldName, false, null);
	}

	/**
	 * 为指定字段创建新的 {@link AutowiredFieldValueResolver}，注入为必须。
	 * @param fieldName 字段名
	 * @return 新的 {@link AutowiredFieldValueResolver} 实例
	 */
	public static AutowiredFieldValueResolver forRequiredField(String fieldName) {
		return new AutowiredFieldValueResolver(fieldName, true, null);
	}


	/**
	 * 返回使用直接 bean 名称注入快捷方式的新 {@link AutowiredFieldValueResolver} 实例。
	 * @param beanName 用作快捷方式的 bean 名称
	 * @return 使用给定快捷 bean 名称的新 {@link AutowiredFieldValueResolver} 实例
	 */
	public AutowiredFieldValueResolver withShortcut(String beanName) {
		return new AutowiredFieldValueResolver(this.fieldName, this.required, beanName);
	}

	/**
	 * 为指定已注册 bean 解析字段值，并将结果提供给给定操作。
	 * @param registeredBean 已注册 bean
	 * @param action 接收解析后字段值的操作
	 */
	public <T> void resolve(RegisteredBean registeredBean, ThrowingConsumer<T> action) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		Assert.notNull(action, "'action' must not be null");
		T resolved = resolve(registeredBean);
		if (resolved != null) {
			action.accept(resolved);
		}
	}

	/**
	 * 为指定已注册 bean 解析字段值。
	 * @param registeredBean 已注册 bean
	 * @param requiredType 要求的类型
	 * @return 解析后的字段值
	 */
	@SuppressWarnings("unchecked")
	public <T> @Nullable T resolve(RegisteredBean registeredBean, Class<T> requiredType) {
		Object value = resolveObject(registeredBean);
		Assert.isInstanceOf(requiredType, value);
		return (T) value;
	}

	/**
	 * 为指定已注册 bean 解析字段值。
	 * @param registeredBean 已注册 bean
	 * @return 解析后的字段值
	 */
	@SuppressWarnings("unchecked")
	public <T> @Nullable T resolve(RegisteredBean registeredBean) {
		return (T) resolveObject(registeredBean);
	}

	/**
	 * 为指定已注册 bean 解析字段值。
	 * @param registeredBean 已注册 bean
	 * @return 解析后的字段值
	 */
	public @Nullable Object resolveObject(RegisteredBean registeredBean) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		return resolveValue(registeredBean, getField(registeredBean));
	}

	/**
	 * 为指定已注册 bean 解析字段值，并通过反射设置到实例上。
	 * @param registeredBean 已注册 bean
	 * @param instance bean 实例
	 */
	public void resolveAndSet(RegisteredBean registeredBean, Object instance) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		Assert.notNull(instance, "'instance' must not be null");
		Field field = getField(registeredBean);
		Object resolved = resolveValue(registeredBean, field);
		if (resolved != null) {
			ReflectionUtils.makeAccessible(field);
			ReflectionUtils.setField(field, instance, resolved);
		}
	}

	private @Nullable Object resolveValue(RegisteredBean registeredBean, Field field) {
		String beanName = registeredBean.getBeanName();
		Class<?> beanClass = registeredBean.getBeanClass();
		ConfigurableBeanFactory beanFactory = registeredBean.getBeanFactory();
		DependencyDescriptor descriptor = new DependencyDescriptor(field, this.required);
		descriptor.setContainingClass(beanClass);
		if (this.shortcutBeanName != null) {
			descriptor = new ShortcutDependencyDescriptor(descriptor, this.shortcutBeanName);
		}
		Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
		TypeConverter typeConverter = beanFactory.getTypeConverter();
		try {
			Assert.isInstanceOf(AutowireCapableBeanFactory.class, beanFactory);
			Object value = ((AutowireCapableBeanFactory) beanFactory).resolveDependency(
					descriptor, beanName, autowiredBeanNames, typeConverter);
			registerDependentBeans(beanFactory, beanName, autowiredBeanNames);
			return value;
		}
		catch (BeansException ex) {
			throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(field), ex);
		}
	}

	private Field getField(RegisteredBean registeredBean) {
		Field field = ReflectionUtils.findField(registeredBean.getBeanClass(), this.fieldName);
		Assert.notNull(field, () -> "No field '" + this.fieldName + "' found on " +
				registeredBean.getBeanClass().getName());
		return field;
	}

}
