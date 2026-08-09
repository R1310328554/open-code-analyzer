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

package org.springframework.boot.autoconfigure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.springframework.boot.context.annotation.Configurations;
import org.springframework.core.Ordered;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * 表示自动配置 {@code @Configuration} 类的 {@link Configurations}。
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
public class AutoConfigurations extends Configurations implements Ordered {

	private static final SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();

	private static final int ORDER = AutoConfigurationImportSelector.ORDER;

	/** 全局自动配置类替换映射。 */
	static final AutoConfigurationReplacements replacements = AutoConfigurationReplacements
		.load(AutoConfiguration.class, null);

	/** 类名替换函数。 */
	private final UnaryOperator<String> replacementMapper;

	protected AutoConfigurations(Collection<Class<?>> classes) {
		this(replacements::replace, classes);
	}

	AutoConfigurations(UnaryOperator<String> replacementMapper, Collection<Class<?>> classes) {
		super(sorter(replacementMapper), classes, Class::getName);
		this.replacementMapper = replacementMapper;
	}

	/** 构建按优先级排序的配置类合并函数。 */
	private static UnaryOperator<Collection<Class<?>>> sorter(UnaryOperator<String> replacementMapper) {
		AutoConfigurationSorter sorter = new AutoConfigurationSorter(metadataReaderFactory, null, replacementMapper);
		return (classes) -> {
			List<String> names = classes.stream().map(Class::getName).map(replacementMapper).toList();
			List<String> sorted = sorter.getInPriorityOrder(names);
			return sorted.stream()
				.map((className) -> ClassUtils.resolveClassName(className, null))
				.collect(Collectors.toCollection(ArrayList::new));
		};
	}

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	protected AutoConfigurations merge(Set<Class<?>> mergedClasses) {
		return new AutoConfigurations(this.replacementMapper, mergedClasses);
	}

	/** 由给定配置类创建 {@link AutoConfigurations} 实例。 */
	public static AutoConfigurations of(Class<?>... classes) {
		return new AutoConfigurations(Arrays.asList(classes));
	}

}
