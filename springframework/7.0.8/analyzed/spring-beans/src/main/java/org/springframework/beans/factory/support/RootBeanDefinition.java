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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * 根 Bean 定义表示在运行时支撑 Spring BeanFactory 中特定 Bean 的
 * <b>合并后 Bean 定义</b>。它可能由多个相互继承的原始 Bean 定义创建，
 * 例如来自 XML 声明的 {@link GenericBeanDefinition GenericBeanDefinitions}。
 * 根 Bean 定义本质上是运行时的"统一" Bean 定义视图。
 *
 * <p>根 Bean 定义也可用于<b>在配置阶段注册单个 Bean 定义</b>。
 * 这尤其适用于从工厂方法（例如 {@code @Bean} 方法）和实例供应器
 * （例如 lambda 表达式）派生的程序化定义，它们带有额外类型元数据
 * （参见 {@link #setTargetType(ResolvableType)}/{@link #setResolvedFactoryMethod(Method)}）。
 *
 * <p>注意：对于来自声明式源（例如 XML 定义）的 Bean 定义，
 * 首选灵活的 {@link GenericBeanDefinition} 变体。
 * GenericBeanDefinition 的优势在于允许动态定义父依赖，
 * 而非将角色"硬编码"为根 Bean 定义，甚至支持在 Bean 后处理器阶段更改父关系。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see GenericBeanDefinition
 * @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public class RootBeanDefinition extends AbstractBeanDefinition {

	/** 本定义所装饰的目标定义。 */
	private @Nullable BeanDefinitionHolder decoratedDefinition;

	/** 定义限定符的 {@link AnnotatedElement}。 */
	private @Nullable AnnotatedElement qualifiedElement;

	/** 标记定义是否需要重新合并。 */
	volatile boolean stale;

	/** 是否允许缓存合并结果。 */
	boolean allowCaching = true;

	/** 工厂方法是否唯一（非重载）。 */
	boolean isFactoryMethodUnique;

	/** 目标类型（含泛型信息）。 */
	volatile @Nullable ResolvableType targetType;

	/** 缓存的已确定目标 Class。 */
	volatile @Nullable Class<?> resolvedTargetType;

	/** 缓存的 Bean 是否为 FactoryBean。 */
	volatile @Nullable Boolean isFactoryBean;

	/** 缓存的泛型工厂方法返回类型。 */
	volatile @Nullable ResolvableType factoryMethodReturnType;

	/** 缓存的唯一工厂方法候选（用于内省）。 */
	volatile @Nullable Method factoryMethodToIntrospect;

	/** 缓存的已解析销毁方法名（含推断结果）。 */
	volatile @Nullable String resolvedDestroyMethodName;

	/** 以下四个构造函数相关字段的公共锁。 */
	final Object constructorArgumentLock = new Object();

	/** 缓存的已解析构造函数或工厂方法。 */
	@Nullable Executable resolvedConstructorOrFactoryMethod;

	/** 标记构造函数参数已解析。 */
	boolean constructorArgumentsResolved = false;

	/** 缓存的完全解析的构造函数参数。 */
	@Nullable Object @Nullable [] resolvedConstructorArguments;

	/** 缓存的部分准备的构造函数参数。 */
	@Nullable Object @Nullable [] preparedConstructorArguments;

	/** 以下两个后处理字段的公共锁。 */
	final Object postProcessingLock = new Object();

	/** 标记 MergedBeanDefinitionPostProcessor 已应用。 */
	boolean postProcessed = false;

	/** 标记实例化前后处理器已介入。 */
	volatile @Nullable Boolean beforeInstantiationResolved;

	/** 外部管理的配置成员（方法或字段）。 */
	private @Nullable Set<Member> externallyManagedConfigMembers;

	/** 外部管理的初始化方法名集合。 */
	private @Nullable Set<String> externallyManagedInitMethods;

	/** 外部管理的销毁方法名集合。 */
	private @Nullable Set<String> externallyManagedDestroyMethods;


	/**
	 * 创建新的 RootBeanDefinition，通过 Bean 属性和配置方法进行配置。
	 * @see #setBeanClass
	 * @see #setScope
	 * @see #setConstructorArgumentValues
	 * @see #setPropertyValues
	 */
	public RootBeanDefinition() {
	}

	/**
	 * 为单例创建新的 RootBeanDefinition。
	 * @param beanClass 要实例化的 Bean 类
	 * @see #setBeanClass
	 */
	public RootBeanDefinition(@Nullable Class<?> beanClass) {
		setBeanClass(beanClass);
	}

	/**
	 * 为单例创建新的 RootBeanDefinition。
	 * @param beanType 要实例化的 Bean 类型
	 * @since 6.0
	 * @see #setTargetType(ResolvableType)
	 * @deprecated 自 6.0.11 起，建议额外调用 {@link #setTargetType(ResolvableType)}
	 */
	@Deprecated(since = "6.0.11")
	public RootBeanDefinition(@Nullable ResolvableType beanType) {
		setTargetType(beanType);
	}

	/**
	 * 为单例 Bean 创建新的 RootBeanDefinition，通过调用给定供应器构造每个实例
	 * （可能是 lambda 或方法引用）。
	 * @param beanClass 要实例化的 Bean 类
	 * @param instanceSupplier 构造 Bean 实例的供应器，作为声明式工厂方法的替代
	 * @since 5.0
	 * @see #setInstanceSupplier
	 */
	public <T> RootBeanDefinition(@Nullable Class<T> beanClass, @Nullable Supplier<T> instanceSupplier) {
		setBeanClass(beanClass);
		setInstanceSupplier(instanceSupplier);
	}

	/**
	 * 为作用域 Bean 创建新的 RootBeanDefinition，通过调用给定供应器构造每个实例
	 * （可能是 lambda 或方法引用）。
	 * @param beanClass 要实例化的 Bean 类
	 * @param scope 对应作用域的名称
	 * @param instanceSupplier 构造 Bean 实例的供应器，作为声明式工厂方法的替代
	 * @since 5.0
	 * @see #setInstanceSupplier
	 */
	public <T> RootBeanDefinition(@Nullable Class<T> beanClass, String scope, @Nullable Supplier<T> instanceSupplier) {
		setBeanClass(beanClass);
		setScope(scope);
		setInstanceSupplier(instanceSupplier);
	}

	/**
	 * 为单例创建新的 RootBeanDefinition，使用给定自动装配模式。
	 * @param beanClass 要实例化的 Bean 类
	 * @param autowireMode 按名称或类型，使用本接口中的常量
	 * @param dependencyCheck 是否对对象执行依赖检查
	 * （不适用于构造函数自动装配，因此在构造函数处被忽略）
	 */
	public RootBeanDefinition(@Nullable Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
		if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
			setDependencyCheck(DEPENDENCY_CHECK_OBJECTS);
		}
	}

	/**
	 * 为单例创建新的 RootBeanDefinition，提供构造函数参数和属性值。
	 * @param beanClass 要实例化的 Bean 类
	 * @param cargs 要应用的构造函数参数值
	 * @param pvs 要应用的属性值
	 */
	public RootBeanDefinition(@Nullable Class<?> beanClass, @Nullable ConstructorArgumentValues cargs,
			@Nullable MutablePropertyValues pvs) {

		super(cargs, pvs);
		setBeanClass(beanClass);
	}

	/**
	 * 为单例创建新的 RootBeanDefinition，提供构造函数参数和属性值。
	 * <p>使用 Bean 类名以避免过早加载 Bean 类。
	 * @param beanClassName 要实例化的类名
	 */
	public RootBeanDefinition(String beanClassName) {
		setBeanClassName(beanClassName);
	}

	/**
	 * 为单例创建新的 RootBeanDefinition，提供构造函数参数和属性值。
	 * <p>使用 Bean 类名以避免过早加载 Bean 类。
	 * @param beanClassName 要实例化的类名
	 * @param cargs 要应用的构造函数参数值
	 * @param pvs 要应用的属性值
	 */
	public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClassName(beanClassName);
	}

	/**
	 * 将给定 Bean 定义深拷贝为新的 RootBeanDefinition。
	 * @param original 要拷贝的原始 Bean 定义
	 */
	public RootBeanDefinition(RootBeanDefinition original) {
		super(original);
		this.decoratedDefinition = original.decoratedDefinition;
		this.qualifiedElement = original.qualifiedElement;
		this.allowCaching = original.allowCaching;
		this.isFactoryMethodUnique = original.isFactoryMethodUnique;
		this.targetType = original.targetType;
		this.factoryMethodToIntrospect = original.factoryMethodToIntrospect;
	}

	/**
	 * 将给定 Bean 定义深拷贝为新的 RootBeanDefinition。
	 * @param original 要拷贝的原始 Bean 定义
	 */
	RootBeanDefinition(BeanDefinition original) {
		super(original);
	}


	@Override
	public @Nullable String getParentName() {
		return null;
	}

	@Override
	public void setParentName(@Nullable String parentName) {
		if (parentName != null) {
			throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
		}
	}

	/**
	 * 注册本 Bean 定义所装饰的目标定义。
	 */
	public void setDecoratedDefinition(@Nullable BeanDefinitionHolder decoratedDefinition) {
		this.decoratedDefinition = decoratedDefinition;
	}

	/**
	 * 返回本 Bean 定义所装饰的目标定义（若有）。
	 */
	public @Nullable BeanDefinitionHolder getDecoratedDefinition() {
		return this.decoratedDefinition;
	}

	/**
	 * 指定定义限定符的 {@link AnnotatedElement}，
	 * 用于替代目标类或工厂方法。
	 * @since 4.3.3
	 * @see #setTargetType(ResolvableType)
	 * @see #getResolvedFactoryMethod()
	 */
	public void setQualifiedElement(@Nullable AnnotatedElement qualifiedElement) {
		this.qualifiedElement = qualifiedElement;
	}

	/**
	 * 返回定义限定符的 {@link AnnotatedElement}（若有）。
	 * 否则将检查工厂方法和目标类。
	 * @since 4.3.3
	 */
	public @Nullable AnnotatedElement getQualifiedElement() {
		return this.qualifiedElement;
	}

	/**
	 * 指定本 Bean 定义的泛型目标类型（若预先已知）。
	 * @since 4.3.3
	 */
	public void setTargetType(@Nullable ResolvableType targetType) {
		this.targetType = targetType;
	}

	/**
	 * 指定本 Bean 定义的目标类型（若预先已知）。
	 * @since 3.2.2
	 */
	public void setTargetType(@Nullable Class<?> targetType) {
		this.targetType = (targetType != null ? ResolvableType.forClass(targetType) : null);
	}

	/**
	 * 返回本 Bean 定义的目标类型（若已知，可预先指定或在首次实例化时解析）。
	 * @since 3.2.2
	 */
	public @Nullable Class<?> getTargetType() {
		if (this.resolvedTargetType != null) {
			return this.resolvedTargetType;
		}
		ResolvableType targetType = this.targetType;
		return (targetType != null ? targetType.resolve() : null);
	}

	/**
	 * 返回本 Bean 定义的 {@link ResolvableType}，
	 * 来自运行时缓存的类型信息或配置时的
	 * {@link #setTargetType(ResolvableType)} 或 {@link #setBeanClass(Class)}，
	 * 同时考虑已解析的工厂方法定义。
	 * @since 5.1
	 * @see #setTargetType(ResolvableType)
	 * @see #setBeanClass(Class)
	 * @see #setResolvedFactoryMethod(Method)
	 */
	@Override
	public ResolvableType getResolvableType() {
		ResolvableType targetType = this.targetType;
		if (targetType != null) {
			return targetType;
		}
		ResolvableType returnType = this.factoryMethodReturnType;
		if (returnType != null) {
			return returnType;
		}
		Method factoryMethod = getResolvedFactoryMethod();
		if (factoryMethod != null) {
			return ResolvableType.forMethodReturnType(factoryMethod);
		}
		return super.getResolvableType();
	}

	/**
	 * 确定用于默认构造的首选构造函数（若有）。
	 * 必要时将自动装配构造函数参数。
	 * <p>自 6.1 起，默认实现会考虑
	 * {@link #PREFERRED_CONSTRUCTORS_ATTRIBUTE} 属性。
	 * 子类应通过 {@code super} 调用保留此行为，
	 * 可在自身首选构造函数确定之前或之后调用。
	 * @return 一个或多个首选构造函数，或 {@code null}（将调用常规无参默认构造函数）
	 * @since 5.1
	 */
	public Constructor<?> @Nullable [] getPreferredConstructors() {
		Object attribute = getAttribute(PREFERRED_CONSTRUCTORS_ATTRIBUTE);
		if (attribute == null) {
			return null;
		}
		if (attribute instanceof Constructor<?> constructor) {
			return new Constructor<?>[] {constructor};
		}
		if (attribute instanceof Constructor<?>[] constructors) {
			return constructors;
		}
		throw new IllegalArgumentException("Invalid value type for attribute '" +
				PREFERRED_CONSTRUCTORS_ATTRIBUTE + "': " + attribute.getClass().getName());
	}

	/**
	 * 指定引用非重载方法的工厂方法名。
	 */
	public void setUniqueFactoryMethodName(String name) {
		Assert.hasText(name, "Factory method name must not be empty");
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = true;
	}

	/**
	 * 指定引用重载方法的工厂方法名。
	 * @since 5.2
	 */
	public void setNonUniqueFactoryMethodName(String name) {
		Assert.hasText(name, "Factory method name must not be empty");
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = false;
	}

	/**
	 * 检查给定候选是否合格为工厂方法。
	 */
	public boolean isFactoryMethod(Method candidate) {
		return candidate.getName().equals(getFactoryMethodName());
	}

	/**
	 * 设置本 Bean 定义上已解析的 Java 工厂方法。
	 * @param method 已解析的工厂方法，或 {@code null} 以重置
	 * @since 5.2
	 */
	public void setResolvedFactoryMethod(@Nullable Method method) {
		this.factoryMethodToIntrospect = method;
		if (method != null) {
			setUniqueFactoryMethodName(method.getName());
		}
	}

	/**
	 * 返回已解析的工厂方法 Java Method 对象（若可用）。
	 * @return 工厂方法，或 {@code null}（未找到或尚未解析）
	 */
	public @Nullable Method getResolvedFactoryMethod() {
		Method factoryMethod = this.factoryMethodToIntrospect;
		if (factoryMethod == null &&
				getInstanceSupplier() instanceof InstanceSupplier<?> instanceSupplier) {
			factoryMethod = instanceSupplier.getFactoryMethod();
		}
		return factoryMethod;
	}

	/**
	 * 标记本 Bean 定义已后处理，
	 * 即已由 {@link MergedBeanDefinitionPostProcessor} 处理。
	 * @since 6.0
	 */
	public void markAsPostProcessed() {
		synchronized (this.postProcessingLock) {
			this.postProcessed = true;
		}
	}

	/**
	 * 注册外部管理的配置方法或字段。
	 */
	public void registerExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedConfigMembers == null) {
				this.externallyManagedConfigMembers = new LinkedHashSet<>(1);
			}
			this.externallyManagedConfigMembers.add(configMember);
		}
	}

	/**
	 * 判断给定方法或字段是否为外部管理的配置成员。
	 */
	public boolean isExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedConfigMembers != null &&
					this.externallyManagedConfigMembers.contains(configMember));
		}
	}

	/**
	 * 获取所有外部管理的配置方法和字段（不可变 Set）。
	 * @since 5.3.11
	 */
	public Set<Member> getExternallyManagedConfigMembers() {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedConfigMembers != null ?
					Collections.unmodifiableSet(new LinkedHashSet<>(this.externallyManagedConfigMembers)) :
					Collections.emptySet());
		}
	}

	/**
	 * 注册外部管理的配置初始化方法 &mdash;
	 * 例如带有 Jakarta {@link jakarta.annotation.PostConstruct} 注解的方法。
	 * <p>所供 {@code initMethod} 可以是
	 * {@linkplain Method#getName() 简单方法名}或
	 * {@linkplain org.springframework.util.ClassUtils#getQualifiedMethodName(Method)
	 * 限定方法名}（用于包私有和 {@code private} 方法）。
	 * 限定名对于包私有和 {@code private} 方法是必要的，
	 * 以便在类型层次结构中区分同名方法。
	 */
	public void registerExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedInitMethods == null) {
				this.externallyManagedInitMethods = new LinkedHashSet<>(1);
			}
			this.externallyManagedInitMethods.add(initMethod);
		}
	}

	/**
	 * 判断给定方法名是否表示外部管理的初始化方法。
	 * <p>有关所供 {@code initMethod} 的格式，参见 {@link #registerExternallyManagedInitMethod}。
	 */
	public boolean isExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedInitMethods != null &&
					this.externallyManagedInitMethods.contains(initMethod));
		}
	}

	/**
	 * 判断给定方法名是否表示外部管理的初始化方法，不考虑方法可见性。
	 * <p>与 {@link #isExternallyManagedInitMethod(String)} 不同，
	 * 若存在使用限定方法名（而非简单方法名）
	 * {@linkplain #registerExternallyManagedInitMethod(String) 注册}的
	 * {@code private} 外部管理初始化方法，本方法也返回 {@code true}。
	 * @since 5.3.17
	 */
	boolean hasAnyExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			if (isExternallyManagedInitMethod(initMethod)) {
				return true;
			}
			return hasAnyExternallyManagedMethod(this.externallyManagedInitMethods, initMethod);
		}
	}

	/**
	 * 获取所有外部管理的初始化方法（不可变 Set）。
	 * <p>有关返回集合中初始化方法的格式，参见 {@link #registerExternallyManagedInitMethod}。
	 * @since 5.3.11
	 */
	public Set<String> getExternallyManagedInitMethods() {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedInitMethods != null ?
					Collections.unmodifiableSet(new LinkedHashSet<>(this.externallyManagedInitMethods)) :
					Collections.emptySet());
		}
	}

	/**
	 * 必要时解析推断的销毁方法。
	 * @since 6.0
	 */
	public void resolveDestroyMethodIfNecessary() {
		setDestroyMethodNames(DisposableBeanAdapter
				.inferDestroyMethodsIfNecessary(getResolvableType().toClass(), this));
	}

	/**
	 * 注册外部管理的配置销毁方法 &mdash;
	 * 例如带有 JSR-250 {@link jakarta.annotation.PreDestroy} 注解的方法。
	 * <p>所供 {@code destroyMethod} 对于非 private 方法可以是
	 * {@linkplain Method#getName() 简单方法名}，对于 {@code private} 方法可以是
	 * {@linkplain org.springframework.util.ClassUtils#getQualifiedMethodName(Method)
	 * 限定方法名}。限定名对于 {@code private} 方法是必要的，
	 * 以便在类层次结构中区分同名 private 方法。
	 */
	public void registerExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedDestroyMethods == null) {
				this.externallyManagedDestroyMethods = new LinkedHashSet<>(1);
			}
			this.externallyManagedDestroyMethods.add(destroyMethod);
		}
	}

	/**
	 * 判断给定方法名是否表示外部管理的销毁方法。
	 * <p>有关所供 {@code destroyMethod} 的格式，参见 {@link #registerExternallyManagedDestroyMethod}。
	 */
	public boolean isExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedDestroyMethods != null &&
					this.externallyManagedDestroyMethods.contains(destroyMethod));
		}
	}

	/**
	 * 判断给定方法名是否表示外部管理的销毁方法，不考虑方法可见性。
	 * <p>与 {@link #isExternallyManagedDestroyMethod(String)} 不同，
	 * 若存在使用限定方法名（而非简单方法名）
	 * {@linkplain #registerExternallyManagedDestroyMethod(String) 注册}的
	 * {@code private} 外部管理销毁方法，本方法也返回 {@code true}。
	 * @since 5.3.17
	 */
	boolean hasAnyExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			if (isExternallyManagedDestroyMethod(destroyMethod)) {
				return true;
			}
			return hasAnyExternallyManagedMethod(this.externallyManagedDestroyMethods, destroyMethod);
		}
	}

	/** 检查候选集合中是否存在与给定方法名匹配的限定方法名。 */
	private static boolean hasAnyExternallyManagedMethod(@Nullable Set<String> candidates, String methodName) {
		if (candidates != null) {
			for (String candidate : candidates) {
				int indexOfDot = candidate.lastIndexOf('.');
				if (indexOfDot > 0) {
					String candidateMethodName = candidate.substring(indexOfDot + 1);
					if (candidateMethodName.equals(methodName)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 获取所有外部管理的销毁方法（不可变 Set）。
	 * <p>有关返回集合中销毁方法的格式，参见 {@link #registerExternallyManagedDestroyMethod}。
	 * @since 5.3.11
	 */
	public Set<String> getExternallyManagedDestroyMethods() {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedDestroyMethods != null ?
					Collections.unmodifiableSet(new LinkedHashSet<>(this.externallyManagedDestroyMethods)) :
					Collections.emptySet());
		}
	}


	@Override
	public RootBeanDefinition cloneBeanDefinition() {
		return new RootBeanDefinition(this);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
	}

	@Override
	public String toString() {
		return "Root bean: " + super.toString();
	}

}
