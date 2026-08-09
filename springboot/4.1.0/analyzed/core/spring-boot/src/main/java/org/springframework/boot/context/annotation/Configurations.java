/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.context.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 可在 {@link ApplicationContext} 中注册的 {@link Configuration @Configuration} 类集合。
 * 可通过 {@link #getClasses(Configurations[])} 从一个或多个 {@link Configurations} 实例返回类数组，
 * 排序遵循 {@link ApplicationContext} 及自定义 {@link ImportSelector} 的常规规则。
 * <p>
 * 主要用于需要指定配置类但无法使用
 * {@link org.springframework.test.context.junit4.SpringRunner} 的测试场景。
 * <p>
 * 实现类应标注 {@link Order @Order} 或实现 {@link Ordered}。
 *
 * @author Phillip Webb
 * @since 2.0.0
 * @see UserConfigurations
 */
public abstract class Configurations {

	private static final Comparator<Object> COMPARATOR = OrderComparator.INSTANCE
		.thenComparing((other) -> other.getClass().getName());

	private final @Nullable UnaryOperator<Collection<Class<?>>> sorter;

	private final Set<Class<?>> classes;

	private final @Nullable Function<Class<?>, String> beanNameGenerator;

	/**
	 * 创建新的 {@link Configurations} 实例。
	 *
	 * @param classes 配置类
	 */
	protected Configurations(Collection<Class<?>> classes) {
		Assert.notNull(classes, "'classes' must not be null");
		this.sorter = null;
		this.classes = Collections.unmodifiableSet(new LinkedHashSet<>(classes));
		this.beanNameGenerator = null;
	}

	/**
	 * 创建新的 {@link Configurations} 实例。
	 *
	 * @param sorter 用于排序配置的 {@link UnaryOperator}
	 * @param classes 配置类
	 * @param beanNameGenerator 可选的 Bean 名称生成函数
	 * @since 3.4.0
	 */
	protected Configurations(@Nullable UnaryOperator<Collection<Class<?>>> sorter, Collection<Class<?>> classes,
			@Nullable Function<Class<?>, String> beanNameGenerator) {
		Assert.notNull(classes, "'classes' must not be null");
		this.sorter = (sorter != null) ? sorter : UnaryOperator.identity();
		Collection<Class<?>> sorted = this.sorter.apply(classes);
		this.classes = Collections.unmodifiableSet(new LinkedHashSet<>(sorted));
		this.beanNameGenerator = beanNameGenerator;
	}

	protected final Set<Class<?>> getClasses() {
		return this.classes;
	}

	/**
	 * 合并同类型来源的配置。
	 *
	 * @param other 另一个 {@link Configurations}（须与本实例同类型）
	 * @return 新的配置实例（须与本实例同类型）
	 */
	protected Configurations merge(Configurations other) {
		Set<Class<?>> mergedClasses = new LinkedHashSet<>(getClasses());
		mergedClasses.addAll(other.getClasses());
		if (this.sorter != null) {
			mergedClasses = new LinkedHashSet<>(this.sorter.apply(mergedClasses));
		}
		return merge(mergedClasses);
	}

	/**
	 * 合并配置类。
	 *
	 * @param mergedClasses 合并后的类集合
	 * @return 新的配置实例（须与本实例同类型）
	 */
	protected abstract Configurations merge(Set<Class<?>> mergedClasses);

	/**
	 * 返回给定配置类应使用的 Bean 名称，或 {@code null} 使用默认名称。
	 *
	 * @param beanClass Bean 类
	 * @return Bean 名称
	 * @since 3.4.0
	 */
	public @Nullable String getBeanName(Class<?> beanClass) {
		return (this.beanNameGenerator != null) ? this.beanNameGenerator.apply(beanClass) : null;
	}

	/**
	 * 按注册顺序返回所有指定配置中的类。
	 *
	 * @param configurations 源配置
	 * @return 按注册顺序排列的配置类
	 */
	public static Class<?>[] getClasses(Configurations... configurations) {
		return getClasses(Arrays.asList(configurations));
	}

	/**
	 * 按注册顺序返回所有指定配置中的类。
	 *
	 * @param configurations 源配置
	 * @return 按注册顺序排列的配置类
	 */
	public static Class<?>[] getClasses(Collection<Configurations> configurations) {
		List<Configurations> collated = collate(configurations);
		LinkedHashSet<Class<?>> classes = collated.stream()
			.flatMap(Configurations::streamClasses)
			.collect(Collectors.toCollection(LinkedHashSet::new));
		return ClassUtils.toClassArray(classes);
	}

	/**
	 * 对给定配置排序并合并。
	 *
	 * @param configurations 源配置
	 * @return 整理后的配置
	 * @since 3.4.0
	 */
	public static List<Configurations> collate(Collection<Configurations> configurations) {
		LinkedList<Configurations> collated = new LinkedList<>();
		for (Configurations configuration : sortConfigurations(configurations)) {
			if (collated.isEmpty() || collated.getLast().getClass() != configuration.getClass()) {
				collated.add(configuration);
			}
			else {
				collated.set(collated.size() - 1, collated.getLast().merge(configuration));
			}
		}
		return collated;
	}

	private static List<Configurations> sortConfigurations(Collection<Configurations> configurations) {
		List<Configurations> sorted = new ArrayList<>(configurations);
		sorted.sort(COMPARATOR);
		return sorted;
	}

	private static Stream<Class<?>> streamClasses(Configurations configurations) {
		return configurations.getClasses().stream();
	}

}
