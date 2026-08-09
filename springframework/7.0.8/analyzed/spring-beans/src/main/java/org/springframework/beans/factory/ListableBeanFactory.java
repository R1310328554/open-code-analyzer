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

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;

/**
 * {@link BeanFactory} 接口的扩展，由能够枚举其全部 Bean 实例的 BeanFactory 实现，
 * 而不是像客户端请求时那样按名称逐个查找。会预加载全部 Bean 定义的 BeanFactory
 * 实现（例如基于 XML 的工厂）可以实现本接口。
 *
 * <p>若同时是 {@link HierarchicalBeanFactory}，则返回值<i>不会</i>考虑任何
 * BeanFactory 层次结构，而只与当前工厂中定义的 Bean 相关。
 * 若需同时考虑祖先工厂中的 Bean，请使用辅助类 {@link BeanFactoryUtils}。
 *
 * <p>本接口中的方法只尊重本工厂的 Bean 定义。
 * 它们会忽略通过其他方式注册的单例 Bean，例如
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory} 的
 * {@code registerSingleton} 方法；但 {@code getBeanNamesForType} 与
 * {@code getBeansOfType} 例外，它们也会检查这类手动注册的单例。
 * 当然，BeanFactory 的 {@code getBean} 也同样允许透明访问这类特殊 Bean。
 * 不过在典型场景下，所有 Bean 本来都由外部 Bean 定义声明，
 * 因此多数应用不必关心这一区别。
 *
 * <p><b>注意：</b>除 {@code getBeanDefinitionCount} 与
 * {@code containsBeanDefinition} 外，本接口中的方法并非为频繁调用而设计。
 * 实现可能较慢。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16 April 2001
 * @see HierarchicalBeanFactory
 * @see BeanFactoryUtils
 */
public interface ListableBeanFactory extends BeanFactory {

	/**
	 * 检查本 BeanFactory 是否包含给定名称的 Bean 定义。
	 * <p>不考虑本工厂可能参与的任何层次结构，
	 * 并忽略非通过 Bean 定义注册的单例 Bean。
	 * @param beanName 要查找的 Bean 名称
	 * @return 本 BeanFactory 是否包含该名称的 Bean 定义
	 * @see #containsBean
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * 返回工厂中定义的 Bean 数量。
	 * <p>不考虑本工厂可能参与的任何层次结构，
	 * 并忽略非通过 Bean 定义注册的单例 Bean。
	 * @return 工厂中定义的 Bean 数量
	 */
	int getBeanDefinitionCount();

	/**
	 * 返回本工厂中定义的所有 Bean 的名称。
	 * <p>不考虑本工厂可能参与的任何层次结构，
	 * 并忽略非通过 Bean 定义注册的单例 Bean。
	 * @return 本工厂中定义的所有 Bean 名称；若无定义则返回空数组
	 */
	String[] getBeanDefinitionNames();

	/**
	 * 返回指定 Bean 的提供者，支持按需惰性获取实例，
	 * 并提供可用性与唯一性相关选项。
	 * @param requiredType Bean 必须匹配的类型；可为接口或超类
	 * @param allowEagerInit 流式访问是否可内省 <i>lazy-init 单例</i>
	 * 以及 <i>由 FactoryBean 创建的对象</i>（或带有 "factory-bean" 引用的工厂方法）
	 * 以进行类型检查。注意：FactoryBean 需要急切初始化才能确定其类型，
	 * 因此传入 {@code true} 会初始化 FactoryBean 与 "factory-bean" 引用。
	 * 只会执行类型检查所必需的初始化；仍会尽可能避免构造器与方法调用。
	 * @return 对应的提供者句柄
	 * @since 5.3
	 * @see #getBeanProvider(ResolvableType, boolean)
	 * @see #getBeanProvider(Class)
	 * @see #getBeansOfType(Class, boolean, boolean)
	 * @see #getBeanNamesForType(Class, boolean, boolean)
	 */
	<T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit);

	/**
	 * 返回指定 Bean 的提供者，支持按需惰性获取实例，
	 * 并提供可用性与唯一性相关选项。
	 * @param requiredType Bean 必须匹配的类型；可为泛型类型声明。
	 * 注意：与反射注入点不同，此处不支持集合类型。
	 * 若要以编程方式获取匹配特定类型的 Bean 列表，请在此传入实际 Bean 类型，
	 * 再使用 {@link ObjectProvider#orderedStream()} 或其惰性流式/迭代选项。
	 * @param allowEagerInit 流式访问是否可内省 <i>lazy-init 单例</i>
	 * 以及 <i>由 FactoryBean 创建的对象</i>（或带有 "factory-bean" 引用的工厂方法）
	 * 以进行类型检查。注意：FactoryBean 需要急切初始化才能确定其类型，
	 * 因此传入 {@code true} 会初始化 FactoryBean 与 "factory-bean" 引用。
	 * 只会执行类型检查所必需的初始化；仍会尽可能避免构造器与方法调用。
	 * @return 对应的提供者句柄
	 * @since 5.3
	 * @see #getBeanProvider(ResolvableType)
	 * @see ObjectProvider#iterator()
	 * @see ObjectProvider#stream()
	 * @see ObjectProvider#orderedStream()
	 * @see #getBeanNamesForType(ResolvableType, boolean, boolean)
	 */
	<T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit);

	/**
	 * 返回匹配给定类型（含其子类）的 Bean 名称，
	 * 依据 Bean 定义，或在 FactoryBean 情况下依据 {@code getObjectType} 的返回值判断。
	 * <p><b>注意：本方法仅内省顶层 Bean。</b>它<i>不会</i>
	 * 检查可能也匹配指定类型的嵌套 Bean。
	 * <p>会考虑由 FactoryBean 创建的对象，这意味着 FactoryBean 会被初始化。
	 * 若 FactoryBean 创建的对象不匹配，则会用原始 FactoryBean 本身与类型进行匹配。
	 * <p>不考虑本工厂可能参与的任何层次结构。
	 * 若需包含祖先工厂中的 Bean，请使用 BeanFactoryUtils 的
	 * {@code beanNamesForTypeIncludingAncestors}。
	 * <p>本版本的 {@code getBeanNamesForType} 会匹配各类 Bean，
	 * 无论是单例、原型还是 FactoryBean。在多数实现中，
	 * 结果与 {@code getBeanNamesForType(type, true, true)} 相同。
	 * <p>本方法返回的 Bean 名称应尽可能始终按后端配置中的
	 * <i>定义顺序</i>排列。
	 * @param type 要匹配的泛型类或接口
	 * @return 匹配给定对象类型（含子类）的 Bean（或由 FactoryBean 创建的对象）名称；
	 * 若无匹配则返回空数组
	 * @since 4.2
	 * @see #isTypeMatch(String, ResolvableType)
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, ResolvableType)
	 */
	String[] getBeanNamesForType(ResolvableType type);

	/**
	 * 返回匹配给定类型（含其子类）的 Bean 名称，
	 * 依据 Bean 定义，或在 FactoryBean 情况下依据 {@code getObjectType} 的返回值判断。
	 * <p><b>注意：本方法仅内省顶层 Bean。</b>它<i>不会</i>
	 * 检查可能也匹配指定类型的嵌套 Bean。
	 * <p>若设置了 "allowEagerInit" 标志，则会考虑由 FactoryBean 创建的对象，
	 * 这意味着 FactoryBean 会被初始化。若 FactoryBean 创建的对象不匹配，
	 * 则会用原始 FactoryBean 本身与类型进行匹配。
	 * 若未设置 "allowEagerInit"，则只检查原始 FactoryBean
	 * （无需初始化每个 FactoryBean）。
	 * <p>不考虑本工厂可能参与的任何层次结构。
	 * 若需包含祖先工厂中的 Bean，请使用 BeanFactoryUtils 的
	 * {@code beanNamesForTypeIncludingAncestors}。
	 * <p>本方法返回的 Bean 名称应尽可能始终按后端配置中的
	 * <i>定义顺序</i>排列。
	 * @param type 要匹配的泛型类或接口
	 * @param includeNonSingletons 是否也包含原型或作用域 Bean，
	 * 还是仅包含单例（同样适用于 FactoryBean）
	 * @param allowEagerInit 是否内省 <i>lazy-init 单例</i>
	 * 以及 <i>由 FactoryBean 创建的对象</i>（或带有 "factory-bean" 引用的工厂方法）
	 * 以进行类型检查。注意：FactoryBean 需要急切初始化才能确定其类型，
	 * 因此传入 {@code true} 会初始化 FactoryBean 与 "factory-bean" 引用。
	 * 只会执行类型检查所必需的初始化；仍会尽可能避免构造器与方法调用。
	 * @return 匹配给定对象类型（含子类）的 Bean（或由 FactoryBean 创建的对象）名称；
	 * 若无匹配则返回空数组
	 * @since 5.2
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, ResolvableType, boolean, boolean)
	 */
	String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit);

	/**
	 * 返回匹配给定类型（含其子类）的 Bean 名称，
	 * 依据 Bean 定义，或在 FactoryBean 情况下依据 {@code getObjectType} 的返回值判断。
	 * <p><b>注意：本方法仅内省顶层 Bean。</b>它<i>不会</i>
	 * 检查可能也匹配指定类型的嵌套 Bean。
	 * <p>会考虑由 FactoryBean 创建的对象，这意味着 FactoryBean 会被初始化。
	 * 若 FactoryBean 创建的对象不匹配，则会用原始 FactoryBean 本身与类型进行匹配。
	 * <p>不考虑本工厂可能参与的任何层次结构。
	 * 若需包含祖先工厂中的 Bean，请使用 BeanFactoryUtils 的
	 * {@code beanNamesForTypeIncludingAncestors}。
	 * <p>本版本的 {@code getBeanNamesForType} 会匹配各类 Bean，
	 * 无论是单例、原型还是 FactoryBean。在多数实现中，
	 * 结果与 {@code getBeanNamesForType(type, true, true)} 相同。
	 * <p>本方法返回的 Bean 名称应尽可能始终按后端配置中的
	 * <i>定义顺序</i>排列。
	 * @param type 要匹配的类或接口；{@code null} 表示所有 Bean 名称
	 * @return 匹配给定对象类型（含子类）的 Bean（或由 FactoryBean 创建的对象）名称；
	 * 若无匹配则返回空数组
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	String[] getBeanNamesForType(@Nullable Class<?> type);

	/**
	 * 返回匹配给定类型（含其子类）的 Bean 名称，
	 * 依据 Bean 定义，或在 FactoryBean 情况下依据 {@code getObjectType} 的返回值判断。
	 * <p><b>注意：本方法仅内省顶层 Bean。</b>它<i>不会</i>
	 * 检查可能也匹配指定类型的嵌套 Bean。
	 * <p>若设置了 "allowEagerInit" 标志，则会考虑由 FactoryBean 创建的对象，
	 * 这意味着 FactoryBean 会被初始化。若 FactoryBean 创建的对象不匹配，
	 * 则会用原始 FactoryBean 本身与类型进行匹配。
	 * 若未设置 "allowEagerInit"，则只检查原始 FactoryBean
	 * （无需初始化每个 FactoryBean）。
	 * <p>不考虑本工厂可能参与的任何层次结构。
	 * 若需包含祖先工厂中的 Bean，请使用 BeanFactoryUtils 的
	 * {@code beanNamesForTypeIncludingAncestors}。
	 * <p>本方法返回的 Bean 名称应尽可能始终按后端配置中的
	 * <i>定义顺序</i>排列。
	 * @param type 要匹配的类或接口；{@code null} 表示所有 Bean 名称
	 * @param includeNonSingletons 是否也包含原型或作用域 Bean，
	 * 还是仅包含单例（同样适用于 FactoryBean）
	 * @param allowEagerInit 是否内省 <i>lazy-init 单例</i>
	 * 以及 <i>由 FactoryBean 创建的对象</i>（或带有 "factory-bean" 引用的工厂方法）
	 * 以进行类型检查。注意：FactoryBean 需要急切初始化才能确定其类型，
	 * 因此传入 {@code true} 会初始化 FactoryBean 与 "factory-bean" 引用。
	 * 只会执行类型检查所必需的初始化；仍会尽可能避免构造器与方法调用。
	 * @return 匹配给定对象类型（含子类）的 Bean（或由 FactoryBean 创建的对象）名称；
	 * 若无匹配则返回空数组
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

	/**
	 * 返回匹配给定对象类型（含其子类）的 Bean 实例，
	 * 依据 Bean 定义，或在 FactoryBean 情况下依据 {@code getObjectType} 的返回值判断。
	 * <p><b>注意：本方法仅内省顶层 Bean。</b>它<i>不会</i>
	 * 检查可能也匹配指定类型的嵌套 Bean。此外，它会
	 * <b>抑制循环引用场景下当前正在创建中的 Bean 所抛出的异常：</b>
	 * 典型情况是指回本方法调用方的引用。
	 * <p>会考虑由 FactoryBean 创建的对象，这意味着 FactoryBean 会被初始化。
	 * 若 FactoryBean 创建的对象不匹配，则会用原始 FactoryBean 本身与类型进行匹配。
	 * <p>不考虑本工厂可能参与的任何层次结构。
	 * 若需包含祖先工厂中的 Bean，请使用 BeanFactoryUtils 的
	 * {@code beansOfTypeIncludingAncestors}。
	 * <p>本版本的 getBeansOfType 会匹配各类 Bean，
	 * 无论是单例、原型还是 FactoryBean。在多数实现中，
	 * 结果与 {@code getBeansOfType(type, true, true)} 相同。
	 * <p>本方法返回的 Map 应尽可能始终按后端配置中的
	 * <i>定义顺序</i>包含 Bean 名称及对应实例。
	 * <p><b>建议优先使用 {@link #getBeanNamesForType(Class)}，再对特定 Bean 名称
	 * 选择性调用 {@link #getBean}，而不是使用这种基于 Map 的检索方式。</b>
	 * 除惰性实例化带来的好处外，这样也能避免任何异常抑制。
	 * @param type 要匹配的类或接口；{@code null} 表示所有具体 Bean
	 * @return 匹配 Bean 的 Map，键为 Bean 名称，值为对应 Bean 实例
	 * @throws BeansException 若无法创建某个 Bean
	 * @since 1.1.2
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException;

	/**
	 * 返回匹配给定对象类型（含其子类）的 Bean 实例，
	 * 依据 Bean 定义，或在 FactoryBean 情况下依据 {@code getObjectType} 的返回值判断。
	 * <p><b>注意：本方法仅内省顶层 Bean。</b>它<i>不会</i>
	 * 检查可能也匹配指定类型的嵌套 Bean。此外，它会
	 * <b>抑制循环引用场景下当前正在创建中的 Bean 所抛出的异常：</b>
	 * 典型情况是指回本方法调用方的引用。
	 * <p>若设置了 "allowEagerInit" 标志，则会考虑由 FactoryBean 创建的对象，
	 * 这意味着 FactoryBean 会被初始化。若 FactoryBean 创建的对象不匹配，
	 * 则会用原始 FactoryBean 本身与类型进行匹配。
	 * 若未设置 "allowEagerInit"，则只检查原始 FactoryBean
	 * （无需初始化每个 FactoryBean）。
	 * <p>不考虑本工厂可能参与的任何层次结构。
	 * 若需包含祖先工厂中的 Bean，请使用 BeanFactoryUtils 的
	 * {@code beansOfTypeIncludingAncestors}。
	 * <p>本方法返回的 Map 应尽可能始终按后端配置中的
	 * <i>定义顺序</i>包含 Bean 名称及对应实例。
	 * <p><b>建议优先使用 {@link #getBeanNamesForType(Class)}，再对特定 Bean 名称
	 * 选择性调用 {@link #getBean}，而不是使用这种基于 Map 的检索方式。</b>
	 * 除惰性实例化带来的好处外，这样也能避免任何异常抑制。
	 * @param type 要匹配的类或接口；{@code null} 表示所有具体 Bean
	 * @param includeNonSingletons 是否也包含原型或作用域 Bean，
	 * 还是仅包含单例（同样适用于 FactoryBean）
	 * @param allowEagerInit 是否内省 <i>lazy-init 单例</i>
	 * 以及 <i>由 FactoryBean 创建的对象</i>（或带有 "factory-bean" 引用的工厂方法）
	 * 以进行类型检查。注意：FactoryBean 需要急切初始化才能确定其类型，
	 * 因此传入 {@code true} 会初始化 FactoryBean 与 "factory-bean" 引用。
	 * 只会执行类型检查所必需的初始化；仍会尽可能避免构造器与方法调用。
	 * @return 匹配 Bean 的 Map，键为 Bean 名称，值为对应 Bean 实例
	 * @throws BeansException 若无法创建某个 Bean
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException;

	/**
	 * 查找所有标注了给定 {@link Annotation} 类型的 Bean 名称，
	 * 此时尚不创建对应的 Bean 实例。
	 * <p>注意：本方法会考虑由 FactoryBean 创建的对象，
	 * 这意味着为了确定对象类型，FactoryBean 会被初始化。
	 * @param annotationType 要查找的注解类型
	 * （位于指定 Bean 的类、接口或工厂方法级别）
	 * @return 所有匹配 Bean 的名称
	 * @since 4.0
	 * @see #getBeansWithAnnotation(Class)
	 * @see #findAnnotationOnBean(String, Class)
	 */
	String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

	/**
	 * 查找所有标注了给定 {@link Annotation} 类型的 Bean，
	 * 返回 Bean 名称到对应 Bean 实例的 Map。
	 * <p>注意：本方法会考虑由 FactoryBean 创建的对象，
	 * 这意味着为了确定对象类型，FactoryBean 会被初始化。
	 * @param annotationType 要查找的注解类型
	 * （位于指定 Bean 的类、接口或工厂方法级别）
	 * @return 匹配 Bean 的 Map，键为 Bean 名称，值为对应 Bean 实例
	 * @throws BeansException 若无法创建某个 Bean
	 * @since 3.0
	 * @see #findAnnotationOnBean(String, Class)
	 * @see #findAnnotationOnBean(String, Class, boolean)
	 * @see #findAllAnnotationsOnBean(String, Class, boolean)
	 */
	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

	/**
	 * 在指定 Bean 上查找 {@code annotationType} 类型的 {@link Annotation}：
	 * 若给定类本身找不到，则遍历其接口与超类；
	 * 同时也会检查该 Bean 的工厂方法（若有）。
	 * @param beanName 要查找注解的 Bean 名称
	 * @param annotationType 要查找的注解类型
	 * （位于指定 Bean 的类、接口或工厂方法级别）
	 * @return 找到则返回该类型的注解，否则返回 {@code null}
	 * @throws NoSuchBeanDefinitionException 若不存在给定名称的 Bean
	 * @since 3.0
	 * @see #findAnnotationOnBean(String, Class, boolean)
	 * @see #findAllAnnotationsOnBean(String, Class, boolean)
	 * @see #getBeanNamesForAnnotation(Class)
	 * @see #getBeansWithAnnotation(Class)
	 * @see #getType(String)
	 */
	<A extends Annotation> @Nullable A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException;

	/**
	 * 在指定 Bean 上查找 {@code annotationType} 类型的 {@link Annotation}：
	 * 若给定类本身找不到，则遍历其接口与超类；
	 * 同时也会检查该 Bean 的工厂方法（若有）。
	 * @param beanName 要查找注解的 Bean 名称
	 * @param annotationType 要查找的注解类型
	 * （位于指定 Bean 的类、接口或工厂方法级别）
	 * @param allowFactoryBeanInit 是否允许仅为确定对象类型而初始化 {@code FactoryBean}
	 * @return 找到则返回该类型的注解，否则返回 {@code null}
	 * @throws NoSuchBeanDefinitionException 若不存在给定名称的 Bean
	 * @since 5.3.14
	 * @see #findAnnotationOnBean(String, Class)
	 * @see #findAllAnnotationsOnBean(String, Class, boolean)
	 * @see #getBeanNamesForAnnotation(Class)
	 * @see #getBeansWithAnnotation(Class)
	 * @see #getType(String, boolean)
	 */
	<A extends Annotation> @Nullable A findAnnotationOnBean(
			String beanName, Class<A> annotationType, boolean allowFactoryBeanInit)
			throws NoSuchBeanDefinitionException;

	/**
	 * 在指定 Bean 上查找所有 {@code annotationType} 类型的 {@link Annotation} 实例：
	 * 若给定类本身找不到，则遍历其接口与超类；
	 * 同时也会检查该 Bean 的工厂方法（若有）。
	 * @param beanName 要查找注解的 Bean 名称
	 * @param annotationType 要查找的注解类型
	 * （位于指定 Bean 的类、接口或工厂方法级别）
	 * @param allowFactoryBeanInit 是否允许仅为确定对象类型而初始化 {@code FactoryBean}
	 * @return 找到的该类型注解集合（可能为空）
	 * @throws NoSuchBeanDefinitionException 若不存在给定名称的 Bean
	 * @since 6.0
	 * @see #getBeanNamesForAnnotation(Class)
	 * @see #findAnnotationOnBean(String, Class, boolean)
	 * @see #getType(String, boolean)
	 */
	<A extends Annotation> Set<A> findAllAnnotationsOnBean(
			String beanName, Class<A> annotationType, boolean allowFactoryBeanInit)
			throws NoSuchBeanDefinitionException;

}
