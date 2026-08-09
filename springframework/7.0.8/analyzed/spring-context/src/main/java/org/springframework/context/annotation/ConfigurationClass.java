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

package org.springframework.context.annotation;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 表示用户定义的 {@link Configuration @Configuration} 类。
 * <p>包含一组 {@link Bean} 方法，以“扁平化”方式涵盖该类继承层次中定义的全部此类方法。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @since 3.0
 * @see BeanMethod
 * @see ConfigurationClassParser
 */
final class ConfigurationClass {

	/** 配置类的注解元数据。 */
	private final AnnotationMetadata metadata;

	/** 配置类对应的资源（类文件或描述性资源）。 */
	private final Resource resource;

	/** 配置类在容器中的 Bean 名称；导入类可能暂无名称。 */
	private @Nullable String beanName;

	/** 是否通过组件扫描注册。 */
	private boolean scanned = false;

	/** 导入本配置类的其他配置类集合。 */
	private final Set<ConfigurationClass> importedBy = new LinkedHashSet<>(1);

	/** 本配置类中声明的 {@link Bean} 方法。 */
	private final Set<BeanMethod> beanMethods = new LinkedHashSet<>();

	/** {@link ImportResource} 导入的资源路径及其对应读取器类型。 */
	private final Map<String, Class<? extends BeanDefinitionReader>> importedResources =
			new LinkedHashMap<>();

	/** 源类名到 {@link BeanRegistrar} 的映射。 */
	private final MultiValueMap<String, BeanRegistrar> beanRegistrars = new LinkedMultiValueMap<>();

	/** {@link ImportBeanDefinitionRegistrar} 及其导入方元数据。 */
	private final Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> importBeanDefinitionRegistrars =
			new LinkedHashMap<>();

	/** 因条件不匹配等原因跳过的 Bean 方法名集合。 */
	final Set<String> skippedBeanMethods = new HashSet<>();


	/**
	 * 创建带指定名称的新 {@link ConfigurationClass}。
	 * @param metadataReader 用于解析底层 {@link Class} 的读取器
	 * @param beanName 不得为 {@code null}
	 */
	ConfigurationClass(MetadataReader metadataReader, String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.metadata = metadataReader.getAnnotationMetadata();
		this.resource = metadataReader.getResource();
		this.beanName = beanName;
	}

	/**
	 * 创建表示通过 {@link Import} 导入或作为嵌套配置类自动处理的新 {@link ConfigurationClass}
	 *（当 importedBy 非 {@code null} 时）。
	 * @param metadataReader 用于解析底层 {@link Class} 的读取器
	 * @param importedBy 导入本类的配置类
	 * @since 3.1.1
	 */
	ConfigurationClass(MetadataReader metadataReader, ConfigurationClass importedBy) {
		this.metadata = metadataReader.getAnnotationMetadata();
		this.resource = metadataReader.getResource();
		this.importedBy.add(importedBy);
	}

	/**
	 * 创建带指定名称的新 {@link ConfigurationClass}。
	 * @param clazz 要表示的底层 {@link Class}
	 * @param beanName {@code @Configuration} 类 Bean 的名称
	 */
	ConfigurationClass(Class<?> clazz, String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.metadata = AnnotationMetadata.introspect(clazz);
		this.resource = new DescriptiveResource(clazz.getName());
		this.beanName = beanName;
	}

	/**
	 * 创建表示通过 {@link Import} 导入或作为嵌套配置类自动处理的新 {@link ConfigurationClass}
	 *（当 imported 为 {@code true} 时）。
	 * @param clazz 要表示的底层 {@link Class}
	 * @param importedBy 导入本类的配置类
	 * @since 3.1.1
	 */
	ConfigurationClass(Class<?> clazz, ConfigurationClass importedBy) {
		this.metadata = AnnotationMetadata.introspect(clazz);
		this.resource = new DescriptiveResource(clazz.getName());
		this.importedBy.add(importedBy);
	}

	/**
	 * 创建带指定名称的新 {@link ConfigurationClass}。
	 * @param metadata 要表示的底层类的元数据
	 * @param beanName {@code @Configuration} 类 Bean 的名称
	 * @param scanned 底层类是否已通过扫描注册
	 * @since 6.2
	 */
	ConfigurationClass(AnnotationMetadata metadata, String beanName, boolean scanned) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.metadata = metadata;
		this.resource = new DescriptiveResource(metadata.getClassName());
		this.beanName = beanName;
		this.scanned = scanned;
	}


	/** 返回注解元数据。 */
	AnnotationMetadata getMetadata() {
		return this.metadata;
	}

	/** 返回关联资源。 */
	Resource getResource() {
		return this.resource;
	}

	/** 返回配置类的简短类名。 */
	String getSimpleName() {
		return ClassUtils.getShortName(getMetadata().getClassName());
	}

	/** 设置 Bean 名称。 */
	void setBeanName(@Nullable String beanName) {
		this.beanName = beanName;
	}

	/** 返回 Bean 名称。 */
	@Nullable String getBeanName() {
		return this.beanName;
	}

	/**
	 * 返回本配置类是否已通过扫描注册。
	 * @since 6.2
	 */
	boolean isScanned() {
		return this.scanned;
	}

	/**
	 * 返回本配置类是否通过 @{@link Import} 注册，或因嵌套于其他配置类而自动注册。
	 * @since 3.1.1
	 * @see #getImportedBy()
	 */
	boolean isImported() {
		return !this.importedBy.isEmpty();
	}

	/**
	 * 将另一配置类的 imported-by 声明合并到本类。
	 * @since 4.0.5
	 */
	void mergeImportedBy(ConfigurationClass otherConfigClass) {
		this.importedBy.addAll(otherConfigClass.importedBy);
	}

	/**
	 * 返回导入本类的配置类集合；若未被导入则返回空 Set。
	 * @since 4.0.5
	 * @see #isImported()
	 */
	Set<ConfigurationClass> getImportedBy() {
		return this.importedBy;
	}

	/** 登记一个 Bean 方法。 */
	void addBeanMethod(BeanMethod method) {
		this.beanMethods.add(method);
	}

	/** 返回全部 Bean 方法。 */
	Set<BeanMethod> getBeanMethods() {
		return this.beanMethods;
	}

	/** 是否存在非静态 Bean 方法。 */
	boolean hasNonStaticBeanMethods() {
		for (BeanMethod beanMethod : this.beanMethods) {
			if (!beanMethod.getMetadata().isStatic()) {
				return true;
			}
		}
		return false;
	}

	/** 登记导入的资源及其 BeanDefinitionReader 类型。 */
	void addImportedResource(String importedResource, Class<? extends BeanDefinitionReader> readerClass) {
		this.importedResources.put(importedResource, readerClass);
	}

	/** 返回导入的资源映射。 */
	Map<String, Class<? extends BeanDefinitionReader>> getImportedResources() {
		return this.importedResources;
	}

	/** 登记 BeanRegistrar 及其源类名。 */
	void addBeanRegistrar(String sourceClassName, BeanRegistrar beanRegistrar) {
		this.beanRegistrars.add(sourceClassName, beanRegistrar);
	}

	/** 返回 BeanRegistrar 映射。 */
	public MultiValueMap<String, BeanRegistrar> getBeanRegistrars() {
		return this.beanRegistrars;
	}

	/** 登记 ImportBeanDefinitionRegistrar 及其导入方元数据。 */
	void addImportBeanDefinitionRegistrar(ImportBeanDefinitionRegistrar registrar, AnnotationMetadata importingClassMetadata) {
		this.importBeanDefinitionRegistrars.put(registrar, importingClassMetadata);
	}

	/** 返回 ImportBeanDefinitionRegistrar 映射。 */
	Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> getImportBeanDefinitionRegistrars() {
		return this.importBeanDefinitionRegistrars;
	}

	/** 校验配置类及其 Bean 方法的合法性，通过 problemReporter 报告问题。 */
	@SuppressWarnings("NullAway") // Reflection
	void validate(ProblemReporter problemReporter) {
		Map<String, @Nullable Object> attributes = this.metadata.getAnnotationAttributes(Configuration.class.getName());

		// 配置类不得为 final（CGLIB 限制），除非无需代理 Bean 方法
		if (attributes != null && (Boolean) attributes.get("proxyBeanMethods") && hasNonStaticBeanMethods() &&
				this.metadata.isFinal()) {
			problemReporter.error(new FinalConfigurationProblem());
		}

		for (BeanMethod beanMethod : this.beanMethods) {
			beanMethod.validate(problemReporter);
		}

		// 除非 enforceUniqueMethods=false，配置类不得包含重载 Bean 方法
		if (attributes != null && (Boolean) attributes.get("enforceUniqueMethods")) {
			Map<String, MethodMetadata> beanMethodsByName = new LinkedHashMap<>();
			for (BeanMethod beanMethod : this.beanMethods) {
				MethodMetadata current = beanMethod.getMetadata();
				MethodMetadata existing = beanMethodsByName.put(current.getMethodName(), current);
				if (existing != null && existing.getDeclaringClassName().equals(current.getDeclaringClassName())) {
					problemReporter.error(new BeanMethodOverloadingProblem(existing.getMethodName()));
				}
			}
		}
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof ConfigurationClass that &&
				getMetadata().getClassName().equals(that.getMetadata().getClassName())));
	}

	@Override
	public int hashCode() {
		return getMetadata().getClassName().hashCode();
	}

	@Override
	public String toString() {
		return "ConfigurationClass: beanName '" + this.beanName + "', " + this.resource;
	}


	/**
	 * 配置类必须为 non-final，以支持 CGLIB 子类化。
	 */
	private class FinalConfigurationProblem extends Problem {

		FinalConfigurationProblem() {
			super(String.format("@Configuration class '%s' may not be final. Remove the final modifier to continue.",
					getSimpleName()), new Location(getResource(), getMetadata()));
		}
	}


	/**
	 * 默认情况下（自 6.0 起）配置类不允许包含重载 Bean 方法。
	 */
	private class BeanMethodOverloadingProblem extends Problem {

		BeanMethodOverloadingProblem(String methodName) {
			super(String.format("@Configuration class '%s' contains overloaded @Bean methods with name '%s'. Use " +
							"unique method names for separate bean definitions (with individual conditions etc) " +
							"or switch '@Configuration.enforceUniqueMethods' to 'false'.",
					getSimpleName(), methodName), new Location(getResource(), getMetadata()));
		}
	}

}
