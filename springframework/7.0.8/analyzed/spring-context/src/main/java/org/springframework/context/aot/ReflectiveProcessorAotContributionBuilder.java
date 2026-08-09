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

package org.springframework.context.aot;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.aot.hint.annotation.ReflectiveProcessor;
import org.springframework.aot.hint.annotation.ReflectiveRuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflection;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.util.ClassUtils;

/**
 * 用于构建 {@linkplain BeanFactoryInitializationAotContribution AOT 贡献} 的构建器：
 * 检测带注解元素上是否存在 {@link Reflective @Reflective}，并调用底层
 * {@link ReflectiveProcessor} 实现。
 *
 * <p>候选类可显式提供，也可通过扫描类路径发现。
 *
 * @author Stephane Nicoll
 * @since 6.2
 * @see Reflective
 * @see RegisterReflection
 */
public class ReflectiveProcessorAotContributionBuilder {

	private static final ReflectiveRuntimeHintsRegistrar registrar = new ReflectiveRuntimeHintsRegistrar();

	private final Set<Class<?>> classes = new LinkedHashSet<>();


	/**
	 * 处理给定类，筛选使用 {@link Reflective} 的类。
	 * <p>若类直接使用 {@link Reflective} 或通过元注解间接使用，则视为候选。
	 * 会检查类型、字段、构造器、方法及嵌套类型。
	 * @param classes 待检查的类
	 */
	public ReflectiveProcessorAotContributionBuilder withClasses(Iterable<Class<?>> classes) {
		this.classes.addAll(StreamSupport.stream(classes.spliterator(), false)
				.filter(registrar::isCandidate).toList());
		return this;
	}

	/**
	 * 处理给定类，筛选使用 {@link Reflective} 的类。
	 * <p>若类直接使用 {@link Reflective} 或通过元注解间接使用，则视为候选。
	 * 会检查类型、字段、构造器、方法及嵌套类型。
	 * @param classes 待检查的类
	 */
	public ReflectiveProcessorAotContributionBuilder withClasses(Class<?>[] classes) {
		return withClasses(Arrays.asList(classes));
	}

	/**
	 * 扫描给定 {@code packageNames} 及其子包，查找使用 {@link Reflective} 的类。
	 * <p>通过加载指定包中的每个类进行“深度扫描”，在类型、构造器、方法和字段上
	 * 查找 {@link Reflective}；嵌套类同样视为候选。加载失败的类将被忽略。
	 * @param classLoader 使用的类加载器
	 * @param packageNames 要扫描的包名
	 */
	public ReflectiveProcessorAotContributionBuilder scan(@Nullable ClassLoader classLoader, String... packageNames) {
		ReflectiveClassPathScanner scanner = new ReflectiveClassPathScanner(classLoader);
		return withClasses(scanner.scan(packageNames));
	}

	public @Nullable BeanFactoryInitializationAotContribution build() {
		return (!this.classes.isEmpty() ? new AotContribution(this.classes) : null);
	}


	private static class AotContribution implements BeanFactoryInitializationAotContribution {

		private final Class<?>[] classes;

		public AotContribution(Set<Class<?>> classes) {
			this.classes = classes.toArray(Class<?>[]::new);
		}

		@Override
		public void applyTo(GenerationContext generationContext, BeanFactoryInitializationCode beanFactoryInitializationCode) {
			RuntimeHints runtimeHints = generationContext.getRuntimeHints();
			registrar.registerRuntimeHints(runtimeHints, this.classes);
		}
	}


	private static class ReflectiveClassPathScanner extends ClassPathScanningCandidateComponentProvider {

		private final @Nullable ClassLoader classLoader;

		ReflectiveClassPathScanner(@Nullable ClassLoader classLoader) {
			super(false);
			this.classLoader = classLoader;
			addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
		}

		Class<?>[] scan(String... packageNames) {
			if (logger.isDebugEnabled()) {
				logger.debug("Scanning all types for reflective usage from " + Arrays.toString(packageNames));
			}
			Set<BeanDefinition> candidates = new HashSet<>();
			for (String packageName : packageNames) {
				candidates.addAll(findCandidateComponents(packageName));
			}
			return candidates.stream().map(c -> (Class<?>) c.getAttribute("type")).toArray(Class<?>[]::new);
		}

		@Override
		protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
			String className = beanDefinition.getBeanClassName();
			if (className != null) {
				try {
					Class<?> type = ClassUtils.forName(className, this.classLoader);
					beanDefinition.setAttribute("type", type);
					return registrar.isCandidate(type);
				}
				catch (Exception ex) {
					if (logger.isTraceEnabled()) {
						logger.trace("Ignoring '%s' for reflective usage: %s".formatted(className, ex.getMessage()));
					}
				}
			}
			return false;
		}
	}

}
