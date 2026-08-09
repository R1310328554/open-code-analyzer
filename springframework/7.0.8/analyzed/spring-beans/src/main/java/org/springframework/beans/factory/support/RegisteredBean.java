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

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.ResolvableType;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@code RegisteredBean} 表示已向 {@link BeanFactory} 注册但未必已实例化的 Bean。
 * 它提供对包含该 Bean 的 Bean 工厂以及 Bean 名称的访问。
 * 对于内部 Bean，名称可能为自动生成。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 6.0
 */
public final class RegisteredBean {

	/** 包含该 Bean 的可配置列表 Bean 工厂。 */
	private final ConfigurableListableBeanFactory beanFactory;

	/** Bean 名称供应器（内部 Bean 可能延迟解析名称）。 */
	private final Supplier<String> beanName;

	/** Bean 名称是否为自动生成。 */
	private final boolean generatedBeanName;

	/** 已合并 Bean 定义供应器。 */
	private final Supplier<RootBeanDefinition> mergedBeanDefinition;

	/** 父级 RegisteredBean（内部 Bean 时非 null）。 */
	private final @Nullable RegisteredBean parent;


	private RegisteredBean(ConfigurableListableBeanFactory beanFactory, Supplier<String> beanName,
			boolean generatedBeanName, Supplier<RootBeanDefinition> mergedBeanDefinition,
			@Nullable RegisteredBean parent) {

		this.beanFactory = beanFactory;
		this.beanName = beanName;
		this.generatedBeanName = generatedBeanName;
		this.mergedBeanDefinition = mergedBeanDefinition;
		this.parent = parent;
	}


	/**
	 * 为普通 Bean 创建新的 {@link RegisteredBean} 实例。
	 * @param beanFactory 源 Bean 工厂
	 * @param beanName Bean 名称
	 * @return 新的 {@link RegisteredBean} 实例
	 */
	public static RegisteredBean of(ConfigurableListableBeanFactory beanFactory, String beanName) {
		Assert.notNull(beanFactory, "'beanFactory' must not be null");
		Assert.hasLength(beanName, "'beanName' must not be empty");
		return new RegisteredBean(beanFactory, () -> beanName, false,
				() -> (RootBeanDefinition) beanFactory.getMergedBeanDefinition(beanName),
				null);
	}

	/**
	 * 为普通 Bean 创建新的 {@link RegisteredBean} 实例。
	 * @param beanFactory 源 Bean 工厂
	 * @param beanName Bean 名称
	 * @param mbd 预先确定的已合并 Bean 定义
	 * @return 新的 {@link RegisteredBean} 实例
	 * @since 6.0.7
	 */
	static RegisteredBean of(ConfigurableListableBeanFactory beanFactory, String beanName, RootBeanDefinition mbd) {
		return new RegisteredBean(beanFactory, () -> beanName, false, () -> mbd, null);
	}

	/**
	 * 为内部 Bean 创建新的 {@link RegisteredBean} 实例。
	 * @param parent 内部 Bean 的父级
	 * @param innerBean 内部 Bean 的 {@link BeanDefinitionHolder}
	 * @return 新的 {@link RegisteredBean} 实例
	 */
	public static RegisteredBean ofInnerBean(RegisteredBean parent, BeanDefinitionHolder innerBean) {
		Assert.notNull(innerBean, "'innerBean' must not be null");
		return ofInnerBean(parent, innerBean.getBeanName(), innerBean.getBeanDefinition());
	}

	/**
	 * 为内部 Bean 创建新的 {@link RegisteredBean} 实例。
	 * @param parent 内部 Bean 的父级
	 * @param innerBeanDefinition 内部 Bean 定义
	 * @return 新的 {@link RegisteredBean} 实例
	 */
	public static RegisteredBean ofInnerBean(RegisteredBean parent, BeanDefinition innerBeanDefinition) {
		return ofInnerBean(parent, null, innerBeanDefinition);
	}

	/**
	 * 为内部 Bean 创建新的 {@link RegisteredBean} 实例。
	 * @param parent 内部 Bean 的父级
	 * @param innerBeanName 内部 Bean 名称，或 {@code null} 以自动生成名称
	 * @param innerBeanDefinition 内部 Bean 定义
	 * @return 新的 {@link RegisteredBean} 实例
	 */
	public static RegisteredBean ofInnerBean(RegisteredBean parent,
			@Nullable String innerBeanName, BeanDefinition innerBeanDefinition) {

		Assert.notNull(parent, "'parent' must not be null");
		Assert.notNull(innerBeanDefinition, "'innerBeanDefinition' must not be null");
		InnerBeanResolver resolver = new InnerBeanResolver(parent, innerBeanName, innerBeanDefinition);
		Supplier<String> beanName = (StringUtils.hasLength(innerBeanName) ?
				() -> innerBeanName : resolver::resolveBeanName);
		return new RegisteredBean(parent.getBeanFactory(), beanName,
				innerBeanName == null, resolver::resolveMergedBeanDefinition, parent);
	}


	/**
	 * 返回 Bean 名称。
	 * @return beanName Bean 名称
	 */
	public String getBeanName() {
		return this.beanName.get();
	}

	/**
	 * 返回 Bean 名称是否为自动生成。
	 * @return 若名称由系统生成则返回 {@code true}
	 */
	public boolean isGeneratedBeanName() {
		return this.generatedBeanName;
	}

	/**
	 * 返回包含该 Bean 的 Bean 工厂。
	 * @return Bean 工厂
	 */
	public ConfigurableListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * 返回 Bean 的用户定义类。
	 * @return Bean 类
	 */
	public Class<?> getBeanClass() {
		return ClassUtils.getUserClass(getBeanType().toClass());
	}

	/**
	 * 返回 Bean 的 {@link ResolvableType}。
	 * @return Bean 类型
	 */
	public ResolvableType getBeanType() {
		return getMergedBeanDefinition().getResolvableType();
	}

	/**
	 * 返回 Bean 的已合并 Bean 定义。
	 * @return 已合并 Bean 定义
	 * @see ConfigurableListableBeanFactory#getMergedBeanDefinition(String)
	 */
	public RootBeanDefinition getMergedBeanDefinition() {
		return this.mergedBeanDefinition.get();
	}

	/**
	 * 返回本实例是否表示内部 Bean。
	 * @return 若为内部 Bean 则返回 true
	 */
	public boolean isInnerBean() {
		return this.parent != null;
	}

	/**
	 * 返回本实例的父级；若非内部 Bean 则返回 {@code null}。
	 * @return 父级
	 */
	public @Nullable RegisteredBean getParent() {
		return this.parent;
	}

	/**
	 * 解析用于本 Bean 的构造器或工厂方法。
	 * @return {@link java.lang.reflect.Constructor} 或 {@link java.lang.reflect.Method}
	 * @deprecated 请改用 {@link #resolveInstantiationDescriptor()}
	 */
	@Deprecated(since = "6.1.7")
	public Executable resolveConstructorOrFactoryMethod() {
		return new ConstructorResolver((AbstractAutowireCapableBeanFactory) getBeanFactory())
				.resolveConstructorOrFactoryMethod(getBeanName(), getMergedBeanDefinition());
	}

	/**
	 * 解析用于实例化本 Bean 的 {@linkplain InstantiationDescriptor 描述符}。
	 * 它定义要使用的 {@link java.lang.reflect.Constructor}
	 * 或 {@link java.lang.reflect.Method}，以及附加元数据。
	 * @since 6.1.7
	 */
	public InstantiationDescriptor resolveInstantiationDescriptor() {
		Executable executable = resolveConstructorOrFactoryMethod();
		if (executable instanceof Method method && !Modifier.isStatic(method.getModifiers())) {
			String factoryBeanName = getMergedBeanDefinition().getFactoryBeanName();
			if (factoryBeanName != null && this.beanFactory.containsBean(factoryBeanName)) {
				return new InstantiationDescriptor(executable,
						this.beanFactory.getMergedBeanDefinition(factoryBeanName).getResolvableType().toClass());
			}
		}
		return new InstantiationDescriptor(executable, executable.getDeclaringClass());
	}

	/**
	 * 解析自动装配参数。
	 * @param descriptor 依赖描述符（字段/方法/构造器）
	 * @param typeConverter 用于填充数组与集合的 TypeConverter
	 * @param autowiredBeanNames 应将所有自动装配 Bean 名称（用于解析给定依赖）加入的集合
	 * @return 解析后的对象，若未找到则返回 {@code null}
	 * @since 6.0.9
	 */
	public @Nullable Object resolveAutowiredArgument(
			DependencyDescriptor descriptor, TypeConverter typeConverter, Set<String> autowiredBeanNames) {

		return new ConstructorResolver((AbstractAutowireCapableBeanFactory) getBeanFactory())
				.resolveAutowiredArgument(descriptor, descriptor.getDependencyType(),
						getBeanName(), autowiredBeanNames, typeConverter, true);
	}


	@Override
	public String toString() {
		return new ToStringCreator(this).append("beanName", getBeanName())
				.append("mergedBeanDefinition", getMergedBeanDefinition()).toString();
	}


	/**
	 * 描述 Bean 应如何实例化。{@code targetClass} 通常与 {@code executable}
	 * 的声明类相同（构造器或本地声明的工厂方法），但在某些情况下需保留实际具体类
	 * （例如继承的工厂方法）。
	 * @since 6.1.7
	 * @param executable 要调用的 {@link Executable}（{@link java.lang.reflect.Constructor}
	 * 或 {@link java.lang.reflect.Method}）
	 * @param targetClass 可执行对象的 target {@link Class}
	 */
	public record InstantiationDescriptor(Executable executable, Class<?> targetClass) {

		public InstantiationDescriptor(Executable executable) {
			this(executable, executable.getDeclaringClass());
		}
	}


	/**
	 * 用于获取内部 Bean 详情的解析器。
	 */
	private static class InnerBeanResolver {

		private final RegisteredBean parent;

		private final @Nullable String innerBeanName;

		private final BeanDefinition innerBeanDefinition;

		private volatile @Nullable String resolvedBeanName;

		InnerBeanResolver(RegisteredBean parent, @Nullable String innerBeanName, BeanDefinition innerBeanDefinition) {
			Assert.isInstanceOf(AbstractAutowireCapableBeanFactory.class, parent.getBeanFactory());
			this.parent = parent;
			this.innerBeanName = innerBeanName;
			this.innerBeanDefinition = innerBeanDefinition;
		}

		String resolveBeanName() {
			String resolvedBeanName = this.resolvedBeanName;
			if (resolvedBeanName != null) {
				return resolvedBeanName;
			}
			resolvedBeanName = resolveInnerBean((beanName, mergedBeanDefinition) -> beanName);
			this.resolvedBeanName = resolvedBeanName;
			return resolvedBeanName;
		}

		RootBeanDefinition resolveMergedBeanDefinition() {
			return resolveInnerBean((beanName, mergedBeanDefinition) -> mergedBeanDefinition);
		}

		private <T> T resolveInnerBean(BiFunction<String, RootBeanDefinition, T> resolver) {
			// 父级已合并 Bean 定义可能已变更，故始终使用新的 BeanDefinitionValueResolver
			BeanDefinitionValueResolver beanDefinitionValueResolver = new BeanDefinitionValueResolver(
					(AbstractAutowireCapableBeanFactory) this.parent.getBeanFactory(),
					this.parent.getBeanName(), this.parent.getMergedBeanDefinition());
			return beanDefinitionValueResolver.resolveInnerBean(this.innerBeanName, this.innerBeanDefinition, resolver);
		}
	}

}
