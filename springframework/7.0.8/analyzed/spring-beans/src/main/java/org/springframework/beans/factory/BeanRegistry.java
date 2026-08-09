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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

/**
 * 在 {@link BeanRegistrar#register(BeanRegistry, Environment)} 中使用，
 * 用于暴露编程式 bean 注册能力。
 *
 * @author Sebastien Deleuze
 * @since 7.0
 */
public interface BeanRegistry {

	/**
	 * 使用给定的 {@link BeanRegistrar} 注册 bean。
	 * @param registrar 将被调用以注册更多 bean 的注册器
	 */
	void register(BeanRegistrar registrar);

	/**
	 * 为给定名称注册一个别名。
	 * @param name 规范名称
	 * @param alias 要注册的别名
	 * @throws IllegalStateException 若该别名已在使用且不可覆盖
	 */
	void registerAlias(String name, String alias);

	/**
	 * 根据给定类注册 bean；若存在相关
	 * {@link BeanUtils#getResolvableConstructor 可解析构造器}，则用之实例化。
	 * <p>若要注册带泛型类型的 bean，请考虑
	 * {@link #registerBean(ParameterizedTypeReference)}。
	 * @param beanClass bean 的类
	 * @return 生成的 bean 名称
	 * @see #registerBean(Class)
	 */
	<T> String registerBean(Class<T> beanClass);

	/**
	 * 根据给定含泛型的类型注册 bean；若存在相关
	 * {@link BeanUtils#getResolvableConstructor 可解析构造器}，则用之实例化。
	 * @param beanType bean 的含泛型类型
	 * @return 生成的 bean 名称
	 */
	<T> String registerBean(ParameterizedTypeReference<T> beanType);

	/**
	 * 根据给定类注册 bean，并通过定制器回调进行定制。
	 * 将使用可在定制器中配置的 supplier 实例化；否则尝试使用其
	 * {@link BeanUtils#getResolvableConstructor 可解析构造器}。
	 * <p>若要注册带泛型类型的 bean，请考虑
	 * {@link #registerBean(ParameterizedTypeReference, Consumer)}。
	 * @param beanClass bean 的类
	 * @param customizer 用于定制名称以外其他 bean 属性的回调
	 * @return 生成的 bean 名称
	 */
	<T> String registerBean(Class<T> beanClass, Consumer<Spec<T>> customizer);

	/**
	 * 根据给定含泛型的类型注册 bean，并通过定制器回调进行定制。
	 * 将使用可在定制器中配置的 supplier 实例化；否则尝试使用其
	 * {@link BeanUtils#getResolvableConstructor 可解析构造器}。
	 * @param beanType bean 的含泛型类型
	 * @param customizer 用于定制名称以外其他 bean 属性的回调
	 * @return 生成的 bean 名称
	 */
	<T> String registerBean(ParameterizedTypeReference<T> beanType, Consumer<Spec<T>> customizer);

	/**
	 * 根据给定类注册 bean；若存在相关
	 * {@link BeanUtils#getResolvableConstructor 可解析构造器}，则用之实例化。
	 * <p>若要注册带泛型类型的 bean，请考虑
	 * {@link #registerBean(String, ParameterizedTypeReference)}。
	 * @param name bean 名称
	 * @param beanClass bean 的类
	 */
	<T> void registerBean(String name, Class<T> beanClass);

	/**
	 * 根据给定含泛型的类型注册 bean；若存在相关
	 * {@link BeanUtils#getResolvableConstructor 可解析构造器}，则用之实例化。
	 * @param name bean 名称
	 * @param beanType bean 的含泛型类型
	 */
	<T> void registerBean(String name, ParameterizedTypeReference<T> beanType);

	/**
	 * 根据给定类注册 bean，并通过定制器回调进行定制。
	 * 将使用可在定制器中配置的 supplier 实例化；否则尝试使用其
	 * {@link BeanUtils#getResolvableConstructor 可解析构造器}。
	 * <p>若要注册带泛型类型的 bean，请考虑
	 * {@link #registerBean(String, ParameterizedTypeReference, Consumer)}。
	 * @param name bean 名称
	 * @param beanClass bean 的类
	 * @param customizer 用于定制名称以外其他 bean 属性的回调
	 */
	<T> void registerBean(String name, Class<T> beanClass, Consumer<Spec<T>> customizer);

	/**
	 * 根据给定含泛型的类型注册 bean，并通过定制器回调进行定制。
	 * 将使用可在定制器中配置的 supplier 实例化；否则尝试使用其
	 * {@link BeanUtils#getResolvableConstructor 可解析构造器}。
	 * @param name bean 名称
	 * @param beanType bean 的含泛型类型
	 * @param customizer 用于定制名称以外其他 bean 属性的回调
	 */
	<T> void registerBean(String name, ParameterizedTypeReference<T> beanType, Consumer<Spec<T>> customizer);


	/**
	 * 用于定制 bean 的规格说明。
	 * @param <T> bean 类型
	 */
	interface Spec<T> {

		/**
		 * 允许在后台线程中实例化此 bean。
		 * @see AbstractBeanDefinition#setBackgroundInit(boolean)
		 */
		Spec<T> backgroundInit();

		/**
		 * 设置此 bean 的可读描述。
		 * @see BeanDefinition#setDescription(String)
		 */
		Spec<T> description(String description);

		/**
		 * 将此 bean 配置为回退自动装配候选。
		 * @see BeanDefinition#setFallback(boolean)
		 * @see #primary
		 */
		Spec<T> fallback();

		/**
		 * 提示此 bean 扮演基础设施角色，即对最终用户无直接意义。
		 * @see BeanDefinition#setRole(int)
		 * @see BeanDefinition#ROLE_INFRASTRUCTURE
		 */
		Spec<T> infrastructure();

		/**
		 * 将此 bean 配置为惰性初始化。
		 * @see BeanDefinition#setLazyInit(boolean)
		 */
		Spec<T> lazyInit();

		/**
		 * 将此 bean 配置为不可作为自动装配进其他 bean 的候选。
		 * @see BeanDefinition#setAutowireCandidate(boolean)
		 */
		Spec<T> notAutowirable();

		/**
		 * 此 bean 的排序顺序。类似于 {@code @Order} 注解。
		 * @see AbstractBeanDefinition#ORDER_ATTRIBUTE
		 */
		Spec<T> order(int order);

		/**
		 * 将此 bean 配置为首选自动装配候选。
		 * @see BeanDefinition#setPrimary(boolean)
		 * @see #fallback
		 */
		Spec<T> primary();

		/**
		 * 将此 bean 配置为原型作用域。
		 * @see BeanDefinition#setScope(String)
		 * @see BeanDefinition#SCOPE_PROTOTYPE
		 */
		Spec<T> prototype();

		/**
		 * 为此 bean 配置自定义作用域。
		 * @since 7.0.4
		 * @see BeanDefinition#setScope(String)
		 */
		Spec<T> scope(String scope);

		/**
		 * 设置用于构造 bean 实例的 supplier。
		 * @see AbstractBeanDefinition#setInstanceSupplier(Supplier)
		 */
		Spec<T> supplier(Function<SupplierContext, T> supplier);
	}


	/**
	 * bean 实例 supplier 可用的上下文，用于访问 bean 依赖。
	 */
	interface SupplierContext {

		/**
		 * 返回唯一匹配给定类型的 bean 实例（若存在）。
		 * @param beanClass bean 必须匹配的类型；可为接口或超类
		 * @return 匹配该类型的唯一 bean 实例
		 * @see BeanFactory#getBean(String)
		 */
		<T> T bean(Class<T> beanClass) throws BeansException;

		/**
		 * 返回唯一匹配给定含泛型类型的 bean 实例（若存在）。
		 * @param beanType bean 必须匹配的含泛型类型；可为接口或超类
		 * @return 匹配该类型的唯一 bean 实例
		 * @see BeanFactory#getBean(String)
		 */
		<T> T bean(ParameterizedTypeReference<T> beanType) throws BeansException;

		/**
		 * 返回指定 bean 的实例，可能是共享的，也可能是独立的。
		 * @param name 要获取的 bean 名称
		 * @param beanClass bean 必须匹配的类型；可为接口或超类
		 * @return bean 实例
		 * @see BeanFactory#getBean(String, Class)
		 */
		<T> T bean(String name, Class<T> beanClass) throws BeansException;

		/**
		 * 返回指定 bean 的提供者，支持按需惰性获取实例，
		 * 并提供可用性与唯一性相关选项。
		 * <p>若要匹配泛型类型，请考虑 {@link #beanProvider(ParameterizedTypeReference)}。
		 * @param beanClass bean 必须匹配的类型；可为接口或超类
		 * @return 对应的提供者句柄
		 * @see BeanFactory#getBeanProvider(Class)
		 */
		<T> ObjectProvider<T> beanProvider(Class<T> beanClass);

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
		 * 再调用 {@link #beanProvider(Class)} 并传入原始类型作为第二步。
		 * @param beanType bean 必须匹配的含泛型类型；可为接口或超类
		 * @return 对应的提供者句柄
		 * @see BeanFactory#getBeanProvider(ResolvableType)
		 */
		<T> ObjectProvider<T> beanProvider(ParameterizedTypeReference<T> beanType);
	}

}
