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

package org.springframework.beans.factory;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;

/**
 * 访问 Spring bean 容器的根接口。
 *
 * <p>这是 bean 容器的基本客户端视图；
 * 另有 {@link ListableBeanFactory}、
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}
 * 等接口面向特定用途。
 *
 * <p>由持有若干 bean 定义的对象实现，每个定义以唯一的 String 名称标识。
 * 根据 bean 定义不同，工厂会返回所含对象的独立实例（原型 / Prototype 模式），
 * 或返回共享的单一实例（相对经典单例模式更优的做法：单例范围限定在工厂内）。
 * 返回何种实例取决于工厂配置，API 本身相同。自 Spring 2.0 起，
 * 具体应用上下文还可提供更多作用域（例如 Web 环境下的 {@code request}、{@code session}）。
 *
 * <p>这一做法的要点在于：BeanFactory 是应用组件的中央注册表，
 * 并集中管理组件配置（例如不再需要各个对象自行读取属性文件）。
 * 关于其收益，可参见《Expert One-on-One J2EE Design and Development》第 4、11 章。
 *
 * <p>通常更宜依赖依赖注入（“推”式配置），通过 setter 或构造器配置应用对象，
 * 而不是用 BeanFactory 查找这类“拉”式配置。Spring 的依赖注入能力
 * 正是基于本接口及其子接口实现的。
 *
 * <p>BeanFactory 一般会从配置源（如 XML 文档）加载 bean 定义，
 * 并用 {@code org.springframework.beans} 包完成配置。不过实现也可以
 * 在 Java 代码中按需直接创建并返回对象。定义的存储方式不受限：
 * LDAP、RDBMS、XML、属性文件等均可。鼓励实现支持 bean 之间的引用（依赖注入）。
 *
 * <p>与 {@link ListableBeanFactory} 中的方法不同，本接口上的操作在工厂为
 * {@link HierarchicalBeanFactory} 时还会向上查找父工厂。
 * 若本工厂实例找不到 bean，会询问直接父工厂。
 * 本工厂中的同名 bean 应覆盖父工厂中的定义。
 *
 * <p>BeanFactory 实现应尽可能支持标准 bean 生命周期接口。
 * 完整的初始化方法及其标准顺序为：
 * <ol>
 * <li>BeanNameAware 的 {@code setBeanName}
 * <li>BeanClassLoaderAware 的 {@code setBeanClassLoader}
 * <li>BeanFactoryAware 的 {@code setBeanFactory}
 * <li>EnvironmentAware 的 {@code setEnvironment}
 * <li>EmbeddedValueResolverAware 的 {@code setEmbeddedValueResolver}
 * <li>ResourceLoaderAware 的 {@code setResourceLoader}
 * （仅在应用上下文中运行时适用）
 * <li>ApplicationEventPublisherAware 的 {@code setApplicationEventPublisher}
 * （仅在应用上下文中运行时适用）
 * <li>MessageSourceAware 的 {@code setMessageSource}
 * （仅在应用上下文中运行时适用）
 * <li>ApplicationContextAware 的 {@code setApplicationContext}
 * （仅在应用上下文中运行时适用）
 * <li>ServletContextAware 的 {@code setServletContext}
 * （仅在 Web 应用上下文中运行时适用）
 * <li>BeanPostProcessor 的 {@code postProcessBeforeInitialization} 方法
 * <li>InitializingBean 的 {@code afterPropertiesSet}
 * <li>自定义 {@code init-method} 定义
 * <li>BeanPostProcessor 的 {@code postProcessAfterInitialization} 方法
 * </ol>
 *
 * <p>BeanFactory 关闭时适用的生命周期方法：
 * <ol>
 * <li>DestructionAwareBeanPostProcessor 的 {@code postProcessBeforeDestruction} 方法
 * <li>DisposableBean 的 {@code destroy}
 * <li>自定义 {@code destroy-method} 定义
 * </ol>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 13 April 2001
 * @see BeanNameAware#setBeanName
 * @see BeanClassLoaderAware#setBeanClassLoader
 * @see BeanFactoryAware#setBeanFactory
 * @see org.springframework.context.EnvironmentAware#setEnvironment
 * @see org.springframework.context.EmbeddedValueResolverAware#setEmbeddedValueResolver
 * @see org.springframework.context.ResourceLoaderAware#setResourceLoader
 * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher
 * @see org.springframework.context.MessageSourceAware#setMessageSource
 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
 * @see org.springframework.web.context.ServletContextAware#setServletContext
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization
 * @see InitializingBean#afterPropertiesSet
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization
 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#postProcessBeforeDestruction
 * @see DisposableBean#destroy
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName
 */
public interface BeanFactory {

	/**
	 * 用于解引用 {@link FactoryBean} 实例本身，以区别于由该 FactoryBean
	 * <i>创建</i> 的 bean。例如，若名为 {@code myJndiObject} 的 bean 是 FactoryBean，
	 * 获取 {@code &myJndiObject} 将返回工厂本身，而非工厂创建的实例。
	 * @see #FACTORY_BEAN_PREFIX_CHAR
	 */
	String FACTORY_BEAN_PREFIX = "&";

	/**
	 * {@link #FACTORY_BEAN_PREFIX} 的字符形式。
	 * @since 6.2.6
	 */
	char FACTORY_BEAN_PREFIX_CHAR = '&';


	/**
	 * 返回指定 bean 的实例，可能是共享的，也可能是独立的。
	 * <p>该方法使 Spring BeanFactory 可作为单例或原型设计模式的替代。
	 * 对于单例 bean，调用方可保留对返回对象的引用。
	 * <p>会将别名还原为对应的规范 bean 名称。
	 * <p>若在本工厂实例中找不到该 bean，会询问父工厂。
	 * @param name 要获取的 bean 名称
	 * @return bean 实例。
	 * 注意：返回值永不为 {@code null}，但可能是工厂方法返回 {@code null} 时的占位对象，
	 * 需通过 {@code equals(null)} 判断。可选依赖请考虑使用 {@link #getBeanProvider(Class)}。
	 * @throws NoSuchBeanDefinitionException 不存在指定名称的 bean
	 * @throws BeansException 无法获取该 bean
	 */
	Object getBean(String name) throws BeansException;

	/**
	 * 返回指定 bean 的实例，可能是共享的，也可能是独立的。
	 * <p>行为与 {@link #getBean(String)} 相同，但通过在类型不符时抛出
	 * {@code BeanNotOfRequiredTypeException} 提供一定类型安全，
	 * 从而避免像 {@link #getBean(String)} 那样在正确转型时仍可能抛出 ClassCastException。
	 * <p>会将别名还原为对应的规范 bean 名称。
	 * <p>若在本工厂实例中找不到该 bean，会询问父工厂。
	 * @param name 要获取的 bean 名称
	 * @param requiredType bean 必须匹配的类型；可为接口或超类
	 * @return bean 实例。
	 * 注意：返回值永不为 {@code null}。若请求的 bean 解析为工厂方法返回 {@code null}
	 * 的 NullBean 占位，则会针对该占位抛出 {@code BeanNotOfRequiredTypeException}。
	 * 可选依赖请考虑使用 {@link #getBeanProvider(Class)}。
	 * @throws NoSuchBeanDefinitionException 不存在该 bean 定义
	 * @throws BeanNotOfRequiredTypeException bean 类型不符合要求
	 * @throws BeansException 无法创建该 bean
	 */
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;

	/**
	 * 返回指定 bean 的实例，可能是共享的，也可能是独立的。
	 * <p>允许显式指定构造器参数 / 工厂方法参数，覆盖 bean 定义中的默认参数（若有）。
	 * 注意：所提供参数须按声明顺序匹配某个候选构造器 / 工厂方法。
	 * @param name 要获取的 bean 名称
	 * @param args 使用显式参数创建 bean 实例时所用的参数
	 * （仅在创建新实例而非获取已有实例时生效）
	 * @return bean 实例
	 * @throws NoSuchBeanDefinitionException 不存在该 bean 定义
	 * @throws BeanDefinitionStoreException 已给出参数但目标 bean 不是原型
	 * @throws BeansException 无法创建该 bean
	 * @since 2.5
	 */
	Object getBean(String name, @Nullable Object @Nullable ... args) throws BeansException;

	/**
	 * 返回唯一匹配给定对象类型的 bean 实例（若存在）。
	 * <p>该方法进入 {@link ListableBeanFactory} 的按类型查找领域，
	 * 也可能按给定类型的名称转为常规按名查找。若需在多组 bean 上做更广泛检索，
	 * 请使用 {@link ListableBeanFactory} 和/或 {@link BeanFactoryUtils}。
	 * @param requiredType bean 必须匹配的类型；可为接口或超类
	 * @return 匹配所需类型的唯一 bean 实例
	 * @throws NoSuchBeanDefinitionException 未找到该类型的 bean
	 * @throws NoUniqueBeanDefinitionException 找到多个该类型的 bean
	 * @throws BeansException 无法创建该 bean
	 * @since 3.0
	 * @see ListableBeanFactory
	 */
	<T> T getBean(Class<T> requiredType) throws BeansException;

	/**
	 * 返回指定 bean 的实例，可能是共享的，也可能是独立的。
	 * <p>允许显式指定构造器参数 / 工厂方法参数，覆盖 bean 定义中的默认参数（若有）。
	 * 注意：所提供参数须按声明顺序匹配某个候选构造器 / 工厂方法。
	 * <p>该方法进入 {@link ListableBeanFactory} 的按类型查找领域，
	 * 也可能按给定类型的名称转为常规按名查找。若需在多组 bean 上做更广泛检索，
	 * 请使用 {@link ListableBeanFactory} 和/或 {@link BeanFactoryUtils}。
	 * @param requiredType bean 必须匹配的类型；可为接口或超类
	 * @param args 使用显式参数创建 bean 实例时所用的参数
	 * （仅在创建新实例而非获取已有实例时生效）
	 * @return bean 实例
	 * @throws NoSuchBeanDefinitionException 不存在该 bean 定义
	 * @throws BeanDefinitionStoreException 已给出参数但目标 bean 不是原型
	 * @throws BeansException 无法创建该 bean
	 * @since 4.1
	 */
	<T> T getBean(Class<T> requiredType, @Nullable Object @Nullable ... args) throws BeansException;

	/**
	 * 返回指定 bean 的提供者，支持按需惰性获取实例，
	 * 并提供可用性与唯一性相关选项。
	 * <p>若要匹配泛型类型，请考虑 {@link #getBeanProvider(ResolvableType)}。
	 * @param requiredType bean 必须匹配的类型；可为接口或超类
	 * @return 对应的提供者句柄
	 * @since 5.1
	 * @see #getBeanProvider(ResolvableType)
	 */
	<T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);

	/**
	 * 返回指定 bean 的提供者，支持按需惰性获取实例，
	 * 并提供可用性与唯一性相关选项。本重载允许指定要匹配的泛型类型，
	 * 类似于方法/构造器参数上带泛型声明的反射注入点。
	 * <p>注意：与反射注入点不同，此处不支持 bean 集合。若要以编程方式
	 * 获取匹配某类型的 bean 列表，请在此传入实际 bean 类型，再使用
	 * {@link ObjectProvider#orderedStream()} 或其惰性流式/迭代选项。
	 * <p>此外，此处的泛型匹配遵循 Java 赋值规则，较为严格。
	 * 若需类似 Java 编译器 {@code unchecked} 警告的宽松回退匹配，
	 * 可在本变体无法 {@link ObjectProvider#getIfAvailable() 可用} 完整泛型匹配时，
	 * 再调用 {@link #getBeanProvider(Class)} 并传入原始类型作为第二步。
	 * @return 对应的提供者句柄
	 * @param requiredType bean 必须匹配的类型；可为泛型类型声明
	 * @since 5.1
	 * @see ObjectProvider#iterator()
	 * @see ObjectProvider#stream()
	 * @see ObjectProvider#orderedStream()
	 */
	<T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType);

	/**
	 * 返回指定 bean 的提供者，支持按需惰性获取实例，
	 * 并提供可用性与唯一性相关选项。本重载允许指定要匹配的泛型类型，
	 * 类似于方法/构造器参数上带泛型声明的反射注入点。
	 * <p>这是 {@link #getBeanProvider(ResolvableType)} 的变体，通过捕获泛型类型
	 * 实现类型安全获取，通常以内联方式使用：
	 * {@code getBeanProvider(new ParameterizedTypeReference<>() {})}，
	 * 效果等同于 {@code getBeanProvider(ResolvableType.forType(...))}。
	 * @return 对应的提供者句柄
	 * @param requiredType bean 必须匹配的捕获泛型类型
	 * @since 7.0
	 * @see #getBeanProvider(ResolvableType)
	 */
	<T> ObjectProvider<T> getBeanProvider(ParameterizedTypeReference<T> requiredType);

	/**
	 * 本 BeanFactory 是否包含给定名称的 bean 定义或外部注册的单例实例？
	 * <p>若给定名称是别名，会还原为对应的规范 bean 名称。
	 * <p>若本工厂是层次结构，在本工厂找不到时会询问任一父工厂。
	 * <p>只要找到匹配给定名称的 bean 定义或单例实例，本方法即返回 {@code true}，
	 * 无论该定义是具体还是抽象、懒还是急、是否在作用域内。因此，
	 * 本方法返回 {@code true} 并不必然表示 {@link #getBean} 能为同名 bean 取得实例。
	 * @param name 要查询的 bean 名称
	 * @return 是否存在给定名称的 bean
	 */
	boolean containsBean(String name);

	/**
	 * 该 bean 是否为共享单例？亦即 {@link #getBean} 是否总是返回同一实例？
	 * <p>注意：本方法返回 {@code false} 并不明确表示是独立实例。
	 * 它只表示非单例，也可能对应作用域 bean。要显式检查独立实例，请使用 {@link #isPrototype}。
	 * <p>会将别名还原为对应的规范 bean 名称。
	 * <p>若在本工厂实例中找不到该 bean，会询问父工厂。
	 * @param name 要查询的 bean 名称
	 * @return 该 bean 是否对应单例实例
	 * @throws NoSuchBeanDefinitionException 不存在给定名称的 bean
	 * @see #getBean
	 * @see #isPrototype
	 */
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 该 bean 是否为原型？亦即 {@link #getBean} 是否总是返回独立实例？
	 * <p>注意：本方法返回 {@code false} 并不明确表示是单例对象。
	 * 它只表示非独立实例，也可能对应作用域 bean。要显式检查共享单例，请使用 {@link #isSingleton}。
	 * <p>会将别名还原为对应的规范 bean 名称。
	 * <p>若在本工厂实例中找不到该 bean，会询问父工厂。
	 * @param name 要查询的 bean 名称
	 * @return 该 bean 是否总是提供独立实例
	 * @throws NoSuchBeanDefinitionException 不存在给定名称的 bean
	 * @since 2.0.3
	 * @see #getBean
	 * @see #isSingleton
	 */
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 检查给定名称的 bean 是否匹配指定类型。
	 * 更具体地说，检查对该名称调用 {@link #getBean} 返回的对象
	 * 是否可赋值给指定目标类型。
	 * <p>会将别名还原为对应的规范 bean 名称。
	 * <p>若在本工厂实例中找不到该 bean，会询问父工厂。
	 * @param name 要查询的 bean 名称
	 * @param typeToMatch 要匹配的类型（{@code ResolvableType}）
	 * @return 若 bean 类型匹配则为 {@code true}；
	 * 不匹配或尚无法确定则为 {@code false}
	 * @throws NoSuchBeanDefinitionException 不存在给定名称的 bean
	 * @since 4.2
	 * @see #getBean
	 * @see #getType
	 */
	boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;

	/**
	 * 检查给定名称的 bean 是否匹配指定类型。
	 * 更具体地说，检查对该名称调用 {@link #getBean} 返回的对象
	 * 是否可赋值给指定目标类型。
	 * <p>会将别名还原为对应的规范 bean 名称。
	 * <p>若在本工厂实例中找不到该 bean，会询问父工厂。
	 * @param name 要查询的 bean 名称
	 * @param typeToMatch 要匹配的类型（{@code Class}）
	 * @return 若 bean 类型匹配则为 {@code true}；
	 * 不匹配或尚无法确定则为 {@code false}
	 * @throws NoSuchBeanDefinitionException 不存在给定名称的 bean
	 * @since 2.0.1
	 * @see #getBean
	 * @see #getType
	 */
	boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

	/**
	 * 确定给定名称 bean 的类型。更具体地说，
	 * 确定对该名称调用 {@link #getBean} 将返回的对象类型。
	 * <p>对于 {@link FactoryBean}，返回其创建的对象类型
	 * （由 {@link FactoryBean#getObjectType()} 暴露）。这可能导致先前未初始化的
	 * {@code FactoryBean} 被初始化（参见 {@link #getType(String, boolean)}）。
	 * <p>会将别名还原为对应的规范 bean 名称。
	 * <p>若在本工厂实例中找不到该 bean，会询问父工厂。
	 * @param name 要查询的 bean 名称
	 * @return bean 的类型；无法确定则为 {@code null}
	 * @throws NoSuchBeanDefinitionException 不存在给定名称的 bean
	 * @since 1.1.2
	 * @see #getBean
	 * @see #isTypeMatch
	 */
	@Nullable Class<?> getType(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 确定给定名称 bean 的类型。更具体地说，
	 * 确定对该名称调用 {@link #getBean} 将返回的对象类型。
	 * <p>对于 {@link FactoryBean}，返回其创建的对象类型
	 * （由 {@link FactoryBean#getObjectType()} 暴露）。取决于
	 * {@code allowFactoryBeanInit} 标志，在尚无早期类型信息时，
	 * 可能导致先前未初始化的 {@code FactoryBean} 仅为确定对象类型而被初始化。
	 * <p>会将别名还原为对应的规范 bean 名称。
	 * <p>若在本工厂实例中找不到该 bean，会询问父工厂。
	 * @param name 要查询的 bean 名称
	 * @param allowFactoryBeanInit 是否允许仅为确定对象类型而初始化 {@code FactoryBean}
	 * @return bean 的类型；无法确定则为 {@code null}
	 * @throws NoSuchBeanDefinitionException 不存在给定名称的 bean
	 * @since 5.2
	 * @see #getBean
	 * @see #isTypeMatch
	 */
	@Nullable Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException;

	/**
	 * 返回给定 bean 名称的别名（若有）。
	 * <p>在 {@link #getBean} 调用中，这些别名都指向同一 bean。
	 * <p>若给定名称本身是别名，将返回对应的原始 bean 名称及其他别名（若有），
	 * 且原始 bean 名称为数组的第一个元素。
	 * <p>若在本工厂实例中找不到该 bean，会询问父工厂。
	 * @param name 要检查别名的 bean 名称
	 * @return 别名数组；若无则为空数组
	 * @see #getBean
	 */
	String[] getAliases(String name);

}
