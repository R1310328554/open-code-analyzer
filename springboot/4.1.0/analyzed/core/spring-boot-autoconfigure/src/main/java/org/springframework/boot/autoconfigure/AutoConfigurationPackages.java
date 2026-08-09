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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.annotation.DeterminableImports;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 存储自动配置包名，供后续引用（例如 JPA 实体扫描器）。
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Oliver Gierke
 * @since 1.0.0
 */
public abstract class AutoConfigurationPackages {

	private static final Log logger = LogFactory.getLog(AutoConfigurationPackages.class);

	/** 存储基础包信息的内部 bean 名称。 */
	private static final String BEAN = AutoConfigurationPackages.class.getName();

	/**
	 * 判断给定 bean 工厂是否已注册自动配置基础包。
	 * @param beanFactory 源 bean 工厂
	 * @return 若存在可用的自动配置包则返回 {@code true}
	 */
	public static boolean has(BeanFactory beanFactory) {
		return beanFactory.containsBean(BEAN) && !get(beanFactory).isEmpty();
	}

	/**
	 * 返回给定 bean 工厂的自动配置基础包列表。
	 * @param beanFactory 源 bean 工厂
	 * @return 自动配置包列表
	 * @throws IllegalStateException 若未启用自动配置
	 */
	public static List<String> get(BeanFactory beanFactory) {
		try {
			return beanFactory.getBean(BEAN, BasePackages.class).get();
		}
		catch (NoSuchBeanDefinitionException ex) {
			throw new IllegalStateException("Unable to retrieve @EnableAutoConfiguration base packages");
		}
	}

	/**
	 * 以编程方式注册自动配置包名。后续调用会将给定包名追加到已注册的包名之上。
	 * 可用于为给定的 {@link BeanDefinitionRegistry} 手动定义基础包。
	 * 通常不建议直接调用本方法，而应依赖默认约定——包名由
	 * {@code @EnableAutoConfiguration} 配置类确定。
	 * @param registry bean 定义注册表
	 * @param packageNames 要设置的包名
	 */
	public static void register(BeanDefinitionRegistry registry, String... packageNames) {
		if (registry.containsBeanDefinition(BEAN)) {
			addBasePackages(registry.getBeanDefinition(BEAN), packageNames);
		}
		else {
			RootBeanDefinition beanDefinition = new RootBeanDefinition(BasePackages.class);
			beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			addBasePackages(beanDefinition, packageNames);
			registry.registerBeanDefinition(BEAN, beanDefinition);
		}
	}

	/** 将额外包名合并到已有构造参数中（去重）。 */
	private static void addBasePackages(BeanDefinition beanDefinition, String[] additionalBasePackages) {
		ConstructorArgumentValues constructorArgumentValues = beanDefinition.getConstructorArgumentValues();
		if (constructorArgumentValues.hasIndexedArgumentValue(0)) {
			ValueHolder indexedArgumentValue = constructorArgumentValues.getIndexedArgumentValue(0, String[].class);
			Assert.state(indexedArgumentValue != null, "'indexedArgumentValue' must not be null");
			String[] existingPackages = (String[]) indexedArgumentValue.getValue();
			Stream<String> existingPackagesStream = (existingPackages != null) ? Stream.of(existingPackages)
					: Stream.empty();
			constructorArgumentValues.addIndexedArgumentValue(0,
					Stream.concat(existingPackagesStream, Stream.of(additionalBasePackages))
						.distinct()
						.toArray(String[]::new));
		}
		else {
			constructorArgumentValues.addIndexedArgumentValue(0, additionalBasePackages);
		}
	}

	/**
	 * 用于存储导入配置类基础包的 {@link ImportBeanDefinitionRegistrar}。
	 */
	static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {

		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
			register(registry, new PackageImports(metadata).getPackageNames().toArray(new String[0]));
		}

		@Override
		public Set<Object> determineImports(AnnotationMetadata metadata) {
			return Collections.singleton(new PackageImports(metadata));
		}

	}

	/**
	 * 包导入信息的包装类。
	 */
	private static final class PackageImports {

		/** 解析得到的包名列表。 */
		private final List<String> packageNames;

		PackageImports(AnnotationMetadata metadata) {
			AnnotationAttributes attributes = AnnotationAttributes
				.fromMap(metadata.getAnnotationAttributes(AutoConfigurationPackage.class.getName(), false));
			Assert.state(attributes != null, "'attributes' must not be null");
			List<String> packageNames = new ArrayList<>(Arrays.asList(attributes.getStringArray("basePackages")));
			for (Class<?> basePackageClass : attributes.getClassArray("basePackageClasses")) {
				packageNames.add(basePackageClass.getPackage().getName());
			}
			// 未显式指定时，使用被注解类所在包
			if (packageNames.isEmpty()) {
				packageNames.add(ClassUtils.getPackageName(metadata.getClassName()));
			}
			this.packageNames = Collections.unmodifiableList(packageNames);
		}

		List<String> getPackageNames() {
			return this.packageNames;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return this.packageNames.equals(((PackageImports) obj).packageNames);
		}

		@Override
		public int hashCode() {
			return this.packageNames.hashCode();
		}

		@Override
		public String toString() {
			return "Package Imports " + this.packageNames;
		}

	}

	/**
	 * 基础包名的持有者（空列表表示不进行扫描）。
	 */
	static final class BasePackages {

		/** 非空的基础包名列表。 */
		private final List<String> packages;

		/** 是否已记录基础包日志信息。 */
		private boolean loggedBasePackageInfo;

		BasePackages(String... names) {
			List<String> packages = new ArrayList<>();
			for (String name : names) {
				if (StringUtils.hasText(name)) {
					packages.add(name);
				}
			}
			this.packages = packages;
		}

		List<String> get() {
			if (!this.loggedBasePackageInfo) {
				if (this.packages.isEmpty()) {
					if (logger.isWarnEnabled()) {
						logger.warn("@EnableAutoConfiguration was declared on a class "
								+ "in the default package. Automatic @Repository and "
								+ "@Entity scanning is not enabled.");
					}
				}
				else {
					if (logger.isDebugEnabled()) {
						String packageNames = StringUtils.collectionToCommaDelimitedString(this.packages);
						logger.debug("@EnableAutoConfiguration was declared on a class in the package '" + packageNames
								+ "'. Automatic @Repository and @Entity scanning is enabled.");
					}
				}
				this.loggedBasePackageInfo = true;
			}
			return this.packages;
		}

	}

}
