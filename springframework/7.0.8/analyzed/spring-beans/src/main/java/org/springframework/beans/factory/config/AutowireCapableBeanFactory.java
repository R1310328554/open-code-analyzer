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

import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;

/**
 * {@link org.springframework.beans.factory.BeanFactory} 接口的扩展，
 * 由能够自动装配且希望为现有 bean 实例暴露此功能的 bean 工厂实现。
 *
 * <p>此 BeanFactory 子接口不适用于普通应用代码：典型场景请使用
 * {@link org.springframework.beans.factory.BeanFactory} 或
 * {@link org.springframework.beans.factory.ListableBeanFactory}。
 *
 * <p>其他框架的集成代码可利用此接口来装配和填充 Spring 不控制生命周期的现有 bean 实例。
 * 例如 WebWork Action 和 Tapestry Page 对象。
 *
 * <p>注意此接口不由 {@link org.springframework.context.ApplicationContext} 门面实现，
 * 因为应用代码几乎不会使用它。不过也可从应用上下文访问，
 * 通过 ApplicationContext 的
 * {@link org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()} 方法。
 *
 * <p>也可实现 {@link org.springframework.beans.factory.BeanFactoryAware} 接口，
 * 在 ApplicationContext 中运行时暴露内部 BeanFactory，以访问 AutowireCapableBeanFactory：
 * 只需将传入的 BeanFactory 强制转换为 AutowireCapableBeanFactory。
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
 */
public interface AutowireCapableBeanFactory extends BeanFactory {

	/**
	 * 表示无外部定义的自动装配的常量。
	 * 注意 BeanFactoryAware 等和注解驱动的注入仍会应用。
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_NO = 0;

	/**
	 * 表示按名称自动装配 bean 属性的常量（应用于所有 bean 属性 setter）。
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_NAME = 1;

	/**
	 * 表示按类型自动装配 bean 属性的常量（应用于所有 bean 属性 setter）。
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_TYPE = 2;

	/**
	 * 表示自动装配可满足的最贪婪构造器的常量（涉及解析合适的构造器）。
	 * @see #autowire
	 */
	int AUTOWIRE_CONSTRUCTOR = 3;

	/**
	 * 表示通过内省 bean 类确定合适自动装配策略的常量。
	 * @see #autowire
	 * @deprecated 若使用混合自动装配策略，建议使用基于注解的自动装配以更清晰地标示自动装配需求。
	 */
	@Deprecated(since = "3.0")
	int AUTOWIRE_AUTODETECT = 4;

	/**
	 * 初始化现有 bean 实例时"原始实例"约定的后缀：追加到完全限定 bean 类名后，
	 * 例如 "com.mypackage.MyClass.ORIGINAL"，以强制返回给定实例（即不使用代理等）。
	 * @since 5.1
	 * @see #initializeBean(Object, String)
	 * @see #applyBeanPostProcessorsBeforeInitialization(Object, String)
	 * @see #applyBeanPostProcessorsAfterInitialization(Object, String)
	 */
	String ORIGINAL_INSTANCE_SUFFIX = ".ORIGINAL";


	//-------------------------------------------------------------------------
	// 创建和填充外部 bean 实例的常用方法
	//-------------------------------------------------------------------------

	/**
	 * 完全创建给定类的新 bean 实例。
	 * <p>执行 bean 的完整初始化，包括所有适用的
	 * {@link BeanPostProcessor BeanPostProcessor}。
	 * <p>注意：用于创建新实例，填充注解字段和方法，并应用所有标准 bean 初始化回调。
	 * 构造器解析基于 Kotlin 主构造器 / 单个 public / 单个 non-public，
	 * 在模糊场景下回退到默认构造器，也受
	 * {@link SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors}
	 * 影响（例如注解驱动的构造器选择）。
	 * @param beanClass 要创建的 bean 类
	 * @return 新的 bean 实例
	 * @throws BeansException 若实例化或装配失败
	 */
	<T> T createBean(Class<T> beanClass) throws BeansException;

	/**
	 * 通过应用实例化后回调和 bean 属性后处理（例如注解驱动注入）
	 * 来填充给定 bean 实例。
	 * <p>注意：本质上用于（重新）填充注解字段和方法，适用于新实例或反序列化实例。
	 * 它<i>不</i>意味着传统的按名称或按类型属性自动装配；
	 * 那些用途请使用 {@link #autowireBeanProperties}。
	 * @param existingBean 现有 bean 实例
	 * @throws BeansException 若装配失败
	 */
	void autowireBean(Object existingBean) throws BeansException;

	/**
	 * 配置给定的原始 bean：自动装配 bean 属性、应用 bean 属性值、
	 * 应用工厂回调（如 {@code setBeanName} 和 {@code setBeanFactory}），
	 * 并应用所有 bean 后处理器（包括可能包装给定原始 bean 的处理器）。
	 * <p>这实际上是 {@link #initializeBean} 提供的功能的超集，
	 * 完全应用相应 bean 定义指定的配置。
	 * <b>注意：此方法需要给定名称的 bean 定义！</b>
	 * @param existingBean 现有 bean 实例
	 * @param beanName bean 的名称，必要时传递给 bean
	 * （必须有该名称的 bean 定义）
	 * @return 要使用的 bean 实例，可以是原始实例或包装后的实例
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * 若不存在给定名称的 bean 定义
	 * @throws BeansException 若初始化失败
	 * @see #initializeBean
	 */
	Object configureBean(Object existingBean, String beanName) throws BeansException;


	//-------------------------------------------------------------------------
	// 精细控制 bean 生命周期的专用方法
	//-------------------------------------------------------------------------

	/**
	 * 使用指定自动装配策略完全创建给定类的新 bean 实例。
	 * 支持本接口中定义的所有常量。
	 * <p>执行 bean 的完整初始化，包括所有适用的
	 * {@link BeanPostProcessor BeanPostProcessors}。这实际上是
	 * {@link #autowire} 提供的功能的超集，增加了 {@link #initializeBean} 行为。
	 * @param beanClass 要创建的 bean 类
	 * @param autowireMode 按名称或按类型，使用本接口中的常量
	 * @param dependencyCheck 是否对对象执行依赖检查
	 * （不适用于构造器自动装配，因此在那里被忽略）
	 * @return 新的 bean 实例
	 * @throws BeansException 若实例化或装配失败
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @deprecated 推荐使用 {@link #createBean(Class)}
	 */
	@Deprecated(since = "6.1")
	Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * 使用指定自动装配策略实例化给定类的新 bean 实例。
	 * 支持本接口中定义的所有常量。
	 * 也可使用 {@code AUTOWIRE_NO} 调用，仅应用实例化前回调（例如注解驱动注入）。
	 * <p><i>不</i>应用标准 {@link BeanPostProcessor BeanPostProcessors} 回调，
	 * 也不执行 bean 的进一步初始化。此接口为这些目的提供独立的精细操作，
	 * 例如 {@link #initializeBean}。不过，若适用于实例构造，
	 * 会应用 {@link InstantiationAwareBeanPostProcessor} 回调。
	 * @param beanClass 要实例化的 bean 类
	 * @param autowireMode 按名称或按类型，使用本接口中的常量
	 * @param dependencyCheck 是否对 bean 实例中的对象引用执行依赖检查
	 * （不适用于构造器自动装配，因此在那里被忽略）
	 * @return 新的 bean 实例
	 * @throws BeansException 若实例化或装配失败
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #initializeBean
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * 按名称或类型自动装配给定 bean 实例的 bean 属性。
	 * 也可使用 {@code AUTOWIRE_NO} 调用，仅应用实例化后回调（例如注解驱动注入）。
	 * <p><i>不</i>应用标准 {@link BeanPostProcessor BeanPostProcessors} 回调，
	 * 也不执行 bean 的进一步初始化。此接口为这些目的提供独立的精细操作，
	 * 例如 {@link #initializeBean}。不过，若适用于实例配置，
	 * 会应用 {@link InstantiationAwareBeanPostProcessor} 回调。
	 * @param existingBean 现有 bean 实例
	 * @param autowireMode 按名称或按类型，使用本接口中的常量
	 * @param dependencyCheck 是否对 bean 实例中的对象引用执行依赖检查
	 * @throws BeansException 若装配失败
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_NO
	 */
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * 将给定名称的 bean 定义的属性值应用于给定 bean 实例。
	 * bean 定义可定义完全自包含的 bean（复用其属性值），
	 * 或仅定义用于现有 bean 实例的属性值。
	 * <p>此方法<i>不</i>自动装配 bean 属性；仅应用显式定义的属性值。
	 * 使用 {@link #autowireBeanProperties} 方法自动装配现有 bean 实例。
	 * <b>注意：此方法需要给定名称的 bean 定义！</b>
	 * <p><i>不</i>应用标准 {@link BeanPostProcessor BeanPostProcessors} 回调，
	 * 也不执行 bean 的进一步初始化。此接口为这些目的提供独立的精细操作，
	 * 例如 {@link #initializeBean}。不过，若适用于实例配置，
	 * 会应用 {@link InstantiationAwareBeanPostProcessor} 回调。
	 * @param existingBean 现有 bean 实例
	 * @param beanName bean 工厂中 bean 定义的名称
	 * （必须有该名称的 bean 定义）
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * 若不存在给定名称的 bean 定义
	 * @throws BeansException 若应用属性值失败
	 * @see #autowireBeanProperties
	 */
	void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

	/**
	 * 初始化给定的原始 bean，应用工厂回调（如 {@code setBeanName} 和 {@code setBeanFactory}），
	 * 并应用所有 bean 后处理器（包括可能包装给定原始 bean 的处理器）。
	 * <p>注意 bean 工厂中不必存在给定名称的 bean 定义。
	 * 传入的 bean 名称仅用于回调，不会对照已注册的 bean 定义进行检查。
	 * @param existingBean 现有 bean 实例
	 * @param beanName bean 的名称，必要时传递给 bean
	 * （仅传递给 {@link BeanPostProcessor BeanPostProcessors}；
	 * 可遵循 {@link #ORIGINAL_INSTANCE_SUFFIX} 约定以强制返回给定实例，即不使用代理等）
	 * @return 要使用的 bean 实例，可以是原始实例或包装后的实例
	 * @throws BeansException 若初始化失败
	 * @see #ORIGINAL_INSTANCE_SUFFIX
	 */
	Object initializeBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * 将 {@link BeanPostProcessor BeanPostProcessors} 应用于给定现有 bean 实例，
	 * 调用其 {@code postProcessBeforeInitialization} 方法。
	 * 返回的 bean 实例可能是原始实例的包装。
	 * @param existingBean 现有 bean 实例
	 * @param beanName bean 的名称，必要时传递给 bean
	 * （仅传递给 {@link BeanPostProcessor BeanPostProcessors}；
	 * 可遵循 {@link #ORIGINAL_INSTANCE_SUFFIX} 约定以强制返回给定实例，即不使用代理等）
	 * @return 要使用的 bean 实例，可以是原始实例或包装后的实例
	 * @throws BeansException 若任何后处理失败
	 * @see BeanPostProcessor#postProcessBeforeInitialization
	 * @see #ORIGINAL_INSTANCE_SUFFIX
	 * @deprecated 自 6.1 起，推荐使用通过 {@link #initializeBean(Object, String)} 的隐式后处理
	 */
	@Deprecated(since = "6.1")
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * 将 {@link BeanPostProcessor BeanPostProcessors} 应用于给定现有 bean 实例，
	 * 调用其 {@code postProcessAfterInitialization} 方法。
	 * 返回的 bean 实例可能是原始实例的包装。
	 * @param existingBean 现有 bean 实例
	 * @param beanName bean 的名称，必要时传递给 bean
	 * （仅传递给 {@link BeanPostProcessor BeanPostProcessors}；
	 * 可遵循 {@link #ORIGINAL_INSTANCE_SUFFIX} 约定以强制返回给定实例，即不使用代理等）
	 * @return 要使用的 bean 实例，可以是原始实例或包装后的实例
	 * @throws BeansException 若任何后处理失败
	 * @see BeanPostProcessor#postProcessAfterInitialization
	 * @see #ORIGINAL_INSTANCE_SUFFIX
	 * @deprecated 自 6.1 起，推荐使用通过 {@link #initializeBean(Object, String)} 的隐式后处理
	 */
	@Deprecated(since = "6.1")
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * 销毁给定 bean 实例（通常来自 {@link #createBean(Class)}），
	 * 应用 {@link org.springframework.beans.factory.DisposableBean} 契约以及
	 * 已注册的 {@link DestructionAwareBeanPostProcessor DestructionAwareBeanPostProcessors}。
	 * <p>销毁过程中产生的任何异常应被捕获并记录，而非传播给此方法的调用者。
	 * @param existingBean 要销毁的 bean 实例
	 */
	void destroyBean(Object existingBean);


	//-------------------------------------------------------------------------
	// 解析注入点的委托方法
	//-------------------------------------------------------------------------

	/**
	 * 解析唯一匹配给定对象类型的 bean 实例（如有），包括其 bean 名称。
	 * <p>这实际上是 {@link #getBean(Class)} 的变体，保留了匹配实例的 bean 名称。
	 * @param requiredType bean 必须匹配的类型；可以是接口或超类
	 * @return bean 名称加 bean 实例
	 * @throws NoSuchBeanDefinitionException 若未找到匹配的 bean
	 * @throws NoUniqueBeanDefinitionException 若找到多个匹配的 bean
	 * @throws BeansException 若 bean 无法创建
	 * @since 4.3.3
	 * @see #getBean(Class)
	 */
	<T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException;

	/**
	 * 为给定 bean 名称解析 bean 实例，提供依赖描述符以暴露给目标工厂方法。
	 * <p>这实际上是 {@link #getBean(String, Class)} 的变体，支持带有
	 * {@link org.springframework.beans.factory.InjectionPoint} 参数的工厂方法。
	 * @param name 要查找的 bean 名称
	 * @param descriptor 请求注入点的依赖描述符
	 * @return 对应的 bean 实例
	 * @throws NoSuchBeanDefinitionException 若不存在指定名称的 bean
	 * @throws BeansException 若 bean 无法创建
	 * @since 5.1.5
	 * @see #getBean(String, Class)
	 */
	Object resolveBeanByName(String name, DependencyDescriptor descriptor) throws BeansException;

	/**
	 * 针对此工厂中定义的 bean 解析指定依赖。
	 * @param descriptor 依赖描述符（字段/方法/构造器）
	 * @param requestingBeanName 声明给定依赖的 bean 名称
	 * @return 解析后的对象，未找到则为 {@code null}
	 * @throws NoSuchBeanDefinitionException 若未找到匹配的 bean
	 * @throws NoUniqueBeanDefinitionException 若找到多个匹配的 bean
	 * @throws BeansException 若因其他原因依赖解析失败
	 * @since 2.5
	 * @see #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)
	 */
	@Nullable Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException;

	/**
	 * 针对此工厂中定义的 bean 解析指定依赖。
	 * @param descriptor 依赖描述符（字段/方法/构造器）
	 * @param requestingBeanName 声明给定依赖的 bean 名称
	 * @param autowiredBeanNames 所有自动装配 bean 的名称集合（用于解析给定依赖），
	 * 解析出的名称应添加到此集合
	 * @param typeConverter 用于填充数组和集合的 TypeConverter
	 * @return 解析后的对象，未找到则为 {@code null}
	 * @throws NoSuchBeanDefinitionException 若未找到匹配的 bean
	 * @throws NoUniqueBeanDefinitionException 若找到多个匹配的 bean
	 * @throws BeansException 若因其他原因依赖解析失败
	 * @since 2.5
	 * @see DependencyDescriptor
	 */
	@Nullable Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException;

}
