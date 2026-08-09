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

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.core.OrderComparator;

/**
 * {@link ObjectFactory} 的变体，专为注入点设计，
 * 支持以编程方式处理可选依赖，以及对「不唯一」情况采取宽松处理。
 *
 * <p>在 {@link BeanFactory} 环境中，从工厂获得的每一个 {@code ObjectProvider}
 * 都会绑定到其 {@code BeanFactory} 并对应特定 Bean 类型，
 * 所有提供者调用都会对照工厂已注册的 Bean 定义进行匹配。
 * 注意：这些调用都是动态地作用于底层工厂状态，每次都会重新解析所请求的目标对象。
 *
 * <p>自 5.1 起，本接口扩展了 {@link Iterable}，并提供 {@link Stream} 支持。
 * 因此可用于 {@code for} 循环，提供 {@link #forEach} 迭代，
 * 并允许以集合风格通过 {@link #stream} 访问。
 *
 * <p>自 6.2 起，本接口为所有方法声明了默认实现。
 * 这使自定义实现更方便，例如用于单元测试。
 * 典型做法是实现 {@link #stream()} 以启用其余全部方法。
 * 也可以只实现调用方实际需要的方法，例如仅 {@link #getObject()} 或 {@link #getIfAvailable()}。
 *
 * <p>注意：{@link #getObject()} 永不返回 {@code null}——找不到时会抛出
 * {@link NoSuchBeanDefinitionException}——而 {@link #getIfAvailable()}
 * 在完全没有匹配 Bean 时会返回 {@code null}。不过，若找到多个匹配 Bean
 * 且没有明确的唯一胜出者（见下文），两个方法都会抛出
 * {@link NoUniqueBeanDefinitionException}。最后，{@link #getIfUnique()}
 * 在找不到匹配 Bean，以及找到多个匹配但没有唯一胜出者时，都会返回 {@code null}。
 *
 * <p>唯一性一般取决于容器的候选解析算法，但始终会尊重 "primary" 标志
 * （候选 Bean 中仅有一个标记为 primary）以及 "fallback" 标志
 * （候选 Bean 中仅有一个未标记为 fallback）。即使对于非基于注解的注入点，
 * default-candidate 标志也会被一致地纳入考虑：
 * 在没有明确 primary/fallback 指示时，由单个 default candidate 胜出。
 *
 * @author Juergen Hoeller
 * @since 4.3
 * @param <T> 对象类型
 * @see BeanFactory#getBeanProvider
 * @see org.springframework.beans.factory.annotation.Autowired
 */
public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T> {

	/**
	 * 未过滤类型匹配的谓词：包含非 default 候选，
	 * 但在注入点上使用时仍会排除非自动装配候选。
	 * @since 6.2.3
	 * @see #stream(Predicate)
	 * @see #orderedStream(Predicate)
	 * @see org.springframework.beans.factory.config.BeanDefinition#isAutowireCandidate()
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#isDefaultCandidate()
	 */
	Predicate<Class<?>> UNFILTERED = (clazz -> true);


	@Override
	default T getObject() throws BeansException {
		Iterator<T> it = iterator();
		if (!it.hasNext()) {
			throw new NoSuchBeanDefinitionException(Object.class);
		}
		T result = it.next();
		if (it.hasNext()) {
			throw new NoUniqueBeanDefinitionException(Object.class, 2, "more than 1 matching bean");
		}
		return result;
	}

	/**
	 * 返回本工厂所管理对象的一个实例（可能是共享的，也可能是独立的）。
	 * <p>允许显式指定构造参数，用法类似于
	 * {@link BeanFactory#getBean(String, Object...)}。
	 * @param args 创建对应实例时使用的参数
	 * @return Bean 实例
	 * @throws BeansException 创建出错时
	 * @see #getObject()
	 */
	default T getObject(@Nullable Object... args) throws BeansException {
		throw new UnsupportedOperationException("Retrieval with arguments not supported -" +
				"for custom ObjectProvider classes, implement getObject(Object...) for your purposes");
	}

	/**
	 * 返回本工厂所管理对象的一个实例（可能是共享的，也可能是独立的）。
	 * @return Bean 实例；不可用则为 {@code null}
	 * @throws BeansException 创建出错时
	 * @see #getObject()
	 */
	default @Nullable T getIfAvailable() throws BeansException {
		try {
			return getObject();
		}
		catch (NoUniqueBeanDefinitionException ex) {
			throw ex;
		}
		catch (NoSuchBeanDefinitionException ex) {
			return null;
		}
	}

	/**
	 * 返回本工厂所管理对象的一个实例（可能是共享的，也可能是独立的）。
	 * @param defaultSupplier 当工厂中不存在对应对象时，用于提供默认对象的回调
	 * @return Bean 实例；若无可用 Bean 则返回所提供的默认对象
	 * @throws BeansException 创建出错时
	 * @since 5.0
	 * @see #getIfAvailable()
	 */
	default T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfAvailable();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * 若可用，则消费本工厂所管理对象的一个实例（可能是共享的，也可能是独立的）。
	 * @param dependencyConsumer 在目标对象可用时用于处理它的回调（否则不调用）
	 * @throws BeansException 创建出错时
	 * @since 5.0
	 * @see #getIfAvailable()
	 */
	default void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfAvailable();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * 返回本工厂所管理对象的一个实例（可能是共享的，也可能是独立的）。
	 * @return Bean 实例；不可用或不唯一时（即找到多个候选且无一标记为 primary）
	 * 则为 {@code null}
	 * @throws BeansException 创建出错时
	 * @see #getObject()
	 */
	default @Nullable T getIfUnique() throws BeansException {
		try {
			return getObject();
		}
		catch (NoSuchBeanDefinitionException ex) {
			return null;
		}
	}

	/**
	 * 返回本工厂所管理对象的一个实例（可能是共享的，也可能是独立的）。
	 * @param defaultSupplier 当工厂中不存在唯一候选时，用于提供默认对象的回调
	 * @return Bean 实例；若无可用 Bean，或工厂中不唯一
	 * （即找到多个候选且无一标记为 primary），则返回所提供的默认对象
	 * @throws BeansException 创建出错时
	 * @since 5.0
	 * @see #getIfUnique()
	 */
	default T getIfUnique(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfUnique();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * 若唯一，则消费本工厂所管理对象的一个实例（可能是共享的，也可能是独立的）。
	 * @param dependencyConsumer 在目标对象唯一时用于处理它的回调（否则不调用）
	 * @throws BeansException 创建出错时
	 * @since 5.0
	 * @see #getIfUnique()
	 */
	default void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfUnique();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * 返回覆盖所有匹配对象实例的 {@link Iterator}，
	 * 不保证特定顺序（但通常为注册顺序）。
	 * @since 5.1
	 * @see #stream()
	 */
	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	/**
	 * 返回覆盖所有匹配对象实例的顺序 {@link Stream}，
	 * 不保证特定顺序（但通常为注册顺序）。
	 * <p>注意：结果默认可能按注入点上的限定符与目标 Bean、
	 * 以及匹配 Bean 的一般自动装配候选状态进行过滤。
	 * 若要对类型匹配的候选做自定义过滤，请改用 {@link #stream(Predicate)}
	 * （必要时配合 {@link #UNFILTERED}）。
	 * @since 5.1
	 * @see #iterator()
	 * @see #orderedStream()
	 */
	default Stream<T> stream() {
		throw new UnsupportedOperationException("Element access not supported - " +
				"for custom ObjectProvider classes, implement stream() to enable all other methods");
	}

	/**
	 * 返回覆盖所有匹配对象实例的顺序 {@link Stream}，
	 * 并按工厂的通用顺序比较器预先排序。
	 * <p>在标准 Spring 应用上下文中，会按
	 * {@link org.springframework.core.Ordered} 约定排序；
	 * 在基于注解的配置中还会考虑
	 * {@link org.springframework.core.annotation.Order} 注解，
	 * 类似于 list/array 类型的多元素注入点。
	 * <p>默认方法对 {@link #stream()} 应用 {@link OrderComparator}。
	 * 必要时可覆盖本方法以应用
	 * {@link org.springframework.core.annotation.AnnotationAwareOrderComparator}。
	 * <p>注意：结果默认可能按注入点上的限定符与目标 Bean、
	 * 以及匹配 Bean 的一般自动装配候选状态进行过滤。
	 * 若要对类型匹配的候选做自定义过滤，请改用 {@link #stream(Predicate)}
	 * （必要时配合 {@link #UNFILTERED}）。
	 * @since 5.1
	 * @see #stream()
	 * @see org.springframework.core.OrderComparator
	 */
	default Stream<T> orderedStream() {
		return stream().sorted(OrderComparator.INSTANCE);
	}

	/**
	 * 返回经自定义过滤的、覆盖所有匹配对象实例的 {@link Stream}，
	 * 不保证特定顺序（但通常为注册顺序）。
	 * @param customFilter 在原始 Bean 类型匹配结果中选择 Bean 的自定义类型过滤器
	 * （或使用 {@link #UNFILTERED} 获取全部原始类型匹配且不做任何默认过滤）
	 * @since 6.2.3
	 * @see #stream()
	 * @see #orderedStream(Predicate)
	 */
	default Stream<T> stream(Predicate<Class<?>> customFilter) {
		return stream(customFilter, true);
	}

	/**
	 * 返回经自定义过滤的、覆盖所有匹配对象实例的 {@link Stream}，
	 * 并按工厂的通用顺序比较器预先排序。
	 * @param customFilter 在原始 Bean 类型匹配结果中选择 Bean 的自定义类型过滤器
	 * （或使用 {@link #UNFILTERED} 获取全部原始类型匹配且不做任何默认过滤）
	 * @since 6.2.3
	 * @see #orderedStream()
	 * @see #stream(Predicate)
	 */
	default Stream<T> orderedStream(Predicate<Class<?>> customFilter) {
		return orderedStream(customFilter, true);
	}

	/**
	 * 返回经自定义过滤的、覆盖所有匹配对象实例的 {@link Stream}，
	 * 不保证特定顺序（但通常为注册顺序）。
	 * @param customFilter 在原始 Bean 类型匹配结果中选择 Bean 的自定义类型过滤器
	 * （或使用 {@link #UNFILTERED} 获取全部原始类型匹配且不做任何默认过滤）
	 * @param includeNonSingletons 是否也包含原型或作用域 Bean，
	 * 还是仅包含单例（同样适用于 FactoryBean）
	 * @since 6.2.5
	 * @see #stream(Predicate)
	 * @see #orderedStream(Predicate, boolean)
	 */
	default Stream<T> stream(Predicate<Class<?>> customFilter, boolean includeNonSingletons) {
		if (!includeNonSingletons) {
			throw new UnsupportedOperationException("Only supports includeNonSingletons=true by default");
		}
		return stream().filter(obj -> customFilter.test(obj.getClass()));
	}

	/**
	 * 返回经自定义过滤的、覆盖所有匹配对象实例的 {@link Stream}，
	 * 并按工厂的通用顺序比较器预先排序。
	 * @param customFilter 在原始 Bean 类型匹配结果中选择 Bean 的自定义类型过滤器
	 * （或使用 {@link #UNFILTERED} 获取全部原始类型匹配且不做任何默认过滤）
	 * @param includeNonSingletons 是否也包含原型或作用域 Bean，
	 * 还是仅包含单例（同样适用于 FactoryBean）
	 * @since 6.2.5
	 * @see #orderedStream()
	 * @see #stream(Predicate)
	 */
	default Stream<T> orderedStream(Predicate<Class<?>> customFilter, boolean includeNonSingletons) {
		if (!includeNonSingletons) {
			throw new UnsupportedOperationException("Only supports includeNonSingletons=true by default");
		}
		return orderedStream().filter(obj -> customFilter.test(obj.getClass()));
	}

}
