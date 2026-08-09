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

import java.util.LinkedHashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PatternMatchUtils;

/**
 * 在类路径上检测 Bean 候选并注册相应 Bean 定义到给定注册表（{@code BeanFactory}
 * 或 {@code ApplicationContext}）的 Bean 定义扫描器。
 *
 * <p>候选类通过可配置的类型过滤器检测。默认过滤器包括标注或元标注了 Spring
 * {@link org.springframework.stereotype.Component @Component} 注解的类，
 * 例如 {@link org.springframework.stereotype.Repository @Repository}、
 * {@link org.springframework.stereotype.Service @Service} 和
 * {@link org.springframework.stereotype.Controller @Controller} 构造型。
 *
 * <p>若可用，也支持 JSR-330 的 {@link jakarta.inject.Named} 注解。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see AnnotationConfigApplicationContext#scan
 * @see org.springframework.stereotype.Component
 * @see org.springframework.stereotype.Repository
 * @see org.springframework.stereotype.Service
 * @see org.springframework.stereotype.Controller
 */
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

	/** 本扫描器操作的 Bean 定义注册表。 */
	private final BeanDefinitionRegistry registry;

	/** 检测到的 Bean 的默认值。 */
	private BeanDefinitionDefaults beanDefinitionDefaults = new BeanDefinitionDefaults();

	/** 用于确定自动装配候选的名称匹配模式。 */
	private String @Nullable [] autowireCandidatePatterns;

	/** Bean 名称生成器。 */
	private BeanNameGenerator beanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;

	/** 作用域元数据解析器。 */
	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	/** 是否注册注解配置后处理器。 */
	private boolean includeAnnotationConfig = true;


	/**
	 * 为给定 Bean 工厂创建新的 {@code ClassPathBeanDefinitionScanner}。
	 * @param registry 以 {@code BeanDefinitionRegistry} 形式加载 Bean 定义的 {@code BeanFactory}
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		this(registry, true);
	}

	/**
	 * 为给定 Bean 工厂创建新的 {@code ClassPathBeanDefinitionScanner}。
	 * <p>若传入的 Bean 工厂不仅实现 {@code BeanDefinitionRegistry} 接口，
	 * 还实现 {@code ResourceLoader} 接口，也将用作默认 {@code ResourceLoader}。
	 * 这通常适用于 {@link org.springframework.context.ApplicationContext} 实现。
	 * <p>若给定普通 {@code BeanDefinitionRegistry}，默认 {@code ResourceLoader}
	 * 将为 {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}。
	 * <p>若传入的 Bean 工厂还实现 {@link EnvironmentCapable}，本 reader 将使用其环境。
	 * 否则，reader 将初始化并使用 {@link org.springframework.core.env.StandardEnvironment}。
	 * 所有 {@code ApplicationContext} 实现均为 {@code EnvironmentCapable}，
	 * 而普通 {@code BeanFactory} 实现则不是。
	 * @param registry 以 {@code BeanDefinitionRegistry} 形式加载 Bean 定义的 {@code BeanFactory}
	 * @param useDefaultFilters 是否包含
	 * {@link org.springframework.stereotype.Component @Component}、
	 * {@link org.springframework.stereotype.Repository @Repository}、
	 * {@link org.springframework.stereotype.Service @Service} 和
	 * {@link org.springframework.stereotype.Controller @Controller} 构造型注解的默认过滤器
	 * @see #setResourceLoader
	 * @see #setEnvironment
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
		this(registry, useDefaultFilters, getOrCreateEnvironment(registry));
	}

	/**
	 * 为给定 Bean 工厂创建新的 {@code ClassPathBeanDefinitionScanner}，
	 * 评估 Bean 定义 profile 元数据时使用给定 {@link Environment}。
	 * <p>若传入的 Bean 工厂不仅实现 {@code BeanDefinitionRegistry} 接口，
	 * 还实现 {@link ResourceLoader} 接口，也将用作默认 {@code ResourceLoader}。
	 * 这通常适用于 {@link org.springframework.context.ApplicationContext} 实现。
	 * <p>若给定普通 {@code BeanDefinitionRegistry}，默认 {@code ResourceLoader}
	 * 将为 {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}。
	 * @param registry 以 {@code BeanDefinitionRegistry} 形式加载 Bean 定义的 {@code BeanFactory}
	 * @param useDefaultFilters 是否包含
	 * {@link org.springframework.stereotype.Component @Component}、
	 * {@link org.springframework.stereotype.Repository @Repository}、
	 * {@link org.springframework.stereotype.Service @Service} 和
	 * {@link org.springframework.stereotype.Controller @Controller} 构造型注解的默认过滤器
	 * @param environment 评估 Bean 定义 profile 元数据时使用的 Spring {@link Environment}
	 * @since 3.1
	 * @see #setResourceLoader
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
			Environment environment) {

		this(registry, useDefaultFilters, environment,
				(registry instanceof ResourceLoader resourceLoader ? resourceLoader : null));
	}

	/**
	 * 为给定 Bean 工厂创建新的 {@code ClassPathBeanDefinitionScanner}，
	 * 评估 Bean 定义 profile 元数据时使用给定 {@link Environment}。
	 * @param registry 以 {@code BeanDefinitionRegistry} 形式加载 Bean 定义的 {@code BeanFactory}
	 * @param useDefaultFilters 是否包含
	 * {@link org.springframework.stereotype.Component @Component}、
	 * {@link org.springframework.stereotype.Repository @Repository}、
	 * {@link org.springframework.stereotype.Service @Service} 和
	 * {@link org.springframework.stereotype.Controller @Controller} 构造型注解的默认过滤器
	 * @param environment 评估 Bean 定义 profile 元数据时使用的 Spring {@link Environment}
	 * @param resourceLoader 要使用的 {@link ResourceLoader}
	 * @since 4.3.6
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
			Environment environment, @Nullable ResourceLoader resourceLoader) {

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		if (useDefaultFilters) {
			registerDefaultFilters();
		}
		setEnvironment(environment);
		setResourceLoader(resourceLoader);
	}


	/**
	 * 返回本扫描器操作的 BeanDefinitionRegistry。
	 */
	@Override
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * 设置检测到的 Bean 的默认值。
	 * @see BeanDefinitionDefaults
	 */
	public void setBeanDefinitionDefaults(@Nullable BeanDefinitionDefaults beanDefinitionDefaults) {
		this.beanDefinitionDefaults =
				(beanDefinitionDefaults != null ? beanDefinitionDefaults : new BeanDefinitionDefaults());
	}

	/**
	 * 返回检测到的 Bean 的默认值（永不为 {@code null}）。
	 * @since 4.1
	 */
	public BeanDefinitionDefaults getBeanDefinitionDefaults() {
		return this.beanDefinitionDefaults;
	}

	/**
	 * 设置用于确定自动装配候选的名称匹配模式。
	 * @param autowireCandidatePatterns 要匹配的模式
	 */
	public void setAutowireCandidatePatterns(String @Nullable ... autowireCandidatePatterns) {
		this.autowireCandidatePatterns = autowireCandidatePatterns;
	}

	/**
	 * 设置用于检测到的 Bean 类的 {@link BeanNameGenerator}。
	 * <p>默认为 {@code AnnotationBeanNameGenerator}。
	 * @see AnnotationBeanNameGenerator
	 * @see FullyQualifiedAnnotationBeanNameGenerator
	 * @see FullyQualifiedConfigurationBeanNameGenerator
	 */
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator =
				(beanNameGenerator != null ? beanNameGenerator : AnnotationBeanNameGenerator.INSTANCE);
	}

	/**
	 * 设置用于检测到的 Bean 类的 ScopeMetadataResolver。
	 * 注意：这将覆盖任何自定义的 {@code scopedProxyMode} 设置。
	 * <p>默认为 {@link AnnotationScopeMetadataResolver}。
	 * @see #setScopedProxyMode
	 */
	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver =
				(scopeMetadataResolver != null ? scopeMetadataResolver : new AnnotationScopeMetadataResolver());
	}

	/**
	 * 指定非单例作用域 Bean 的代理行为。
	 * 注意：这将覆盖任何自定义的 {@code scopeMetadataResolver} 设置。
	 * <p>默认为 {@link ScopedProxyMode#NO}。
	 * @see #setScopeMetadataResolver
	 */
	public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
		this.scopeMetadataResolver = new AnnotationScopeMetadataResolver(scopedProxyMode);
	}

	/**
	 * 指定是否注册注解配置后处理器。
	 * <p>默认注册后处理器。关闭此选项可忽略注解或以不同方式处理。
	 */
	public void setIncludeAnnotationConfig(boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
	}


	/**
	 * 在指定基础包内执行扫描。
	 * @param basePackages 要检查带注解类的包
	 * @return 注册的 Bean 数量
	 */
	public int scan(String... basePackages) {
		int beanCountAtScanStart = this.registry.getBeanDefinitionCount();

		doScan(basePackages);

		// 必要时注册注解配置处理器
		if (this.includeAnnotationConfig) {
			AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
		}

		return (this.registry.getBeanDefinitionCount() - beanCountAtScanStart);
	}

	/**
	 * 在指定基础包内执行扫描，返回已注册的 Bean 定义。
	 * <p>本方法<em>不</em>注册注解配置处理器，而是留给调用方处理。
	 * @param basePackages 要检查带注解类的包
	 * @return 为工具注册目的而注册的 Bean 集合（永不为 {@code null}）
	 */
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
		for (String basePackage : basePackages) {
			// 在基础包中查找候选组件
			Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
			for (BeanDefinition candidate : candidates) {
				// 解析作用域元数据并应用到候选定义
				ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
				candidate.setScope(scopeMetadata.getScopeName());
				// 生成 Bean 名称
				String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);
				if (candidate instanceof AbstractBeanDefinition abstractBeanDefinition) {
					postProcessBeanDefinition(abstractBeanDefinition, beanName);
				}
				if (candidate instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
					AnnotationConfigUtils.processCommonDefinitionAnnotations(annotatedBeanDefinition);
				}
				// 检查候选是否可注册，并应用作用域代理
				if (checkCandidate(beanName, candidate)) {
					BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
					definitionHolder =
							AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
					beanDefinitions.add(definitionHolder);
					registerBeanDefinition(definitionHolder, this.registry);
				}
			}
		}
		return beanDefinitions;
	}

	/**
	 * 对给定 Bean 定义应用扫描组件类检索内容之外的进一步设置。
	 * @param beanDefinition 扫描得到的 Bean 定义
	 * @param beanName 给定 Bean 的生成名称
	 */
	protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
		beanDefinition.applyDefaults(this.beanDefinitionDefaults);
		if (this.autowireCandidatePatterns != null) {
			beanDefinition.setAutowireCandidate(PatternMatchUtils.simpleMatch(this.autowireCandidatePatterns, beanName));
		}
	}

	/**
	 * 使用给定注册表注册指定 Bean。
	 * <p>子类可覆盖，例如适配注册流程或为每个扫描 Bean 注册更多 Bean 定义。
	 * @param definitionHolder Bean 定义及其名称
	 * @param registry 要注册 Bean 的 BeanDefinitionRegistry
	 */
	protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}


	/**
	 * 检查给定候选的 Bean 名称，判断相应 Bean 定义是否需要注册，
	 * 或与已有定义冲突。
	 * @param beanName 建议的 Bean 名称
	 * @param beanDefinition 相应的 Bean 定义
	 * @return 若 Bean 可按原样注册则为 {@code true}；
	 * 若因指定名称已存在兼容 Bean 定义而应跳过则为 {@code false}
	 * @throws IllegalStateException 若发现指定名称的已有、不兼容 Bean 定义
	 */
	protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
		if (!this.registry.containsBeanDefinition(beanName)) {
			return true;
		}

		BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
		BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
		if (originatingDef != null) {
			existingDef = originatingDef;
		}

		// 显式注册的覆盖 Bean？
		if (!(existingDef instanceof ScannedGenericBeanDefinition) &&
				(this.registry.isBeanDefinitionOverridable(beanName) || ObjectUtils.nullSafeEquals(
						beanDefinition.getBeanClassName(), existingDef.getBeanClassName()))) {
			return false;
		}

		// 同一文件或等价类被扫描两次？
		if (isCompatible(beanDefinition, existingDef)) {
			return false;
		}

		throw new ConflictingBeanDefinitionException("Annotation-specified bean name '" + beanName +
				"' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " +
				"non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
	}

	/**
	 * 判断给定新 Bean 定义是否与已有 Bean 定义兼容。
	 * <p>默认实现认为：若已有 Bean 定义来自相同来源或来自非扫描来源，则视为兼容。
	 * @param newDef 来自扫描的新 Bean 定义
	 * @param existingDef 已有 Bean 定义，可能是显式定义或先前扫描生成
	 * @return 定义是否视为兼容，新定义应让位于已有定义
	 */
	protected boolean isCompatible(BeanDefinition newDef, BeanDefinition existingDef) {
		return ((newDef.getSource() != null && newDef.getSource().equals(existingDef.getSource())) ||
				newDef.equals(existingDef));
	}


	/**
	 * 若可能，从给定注册表获取 Environment；否则返回新的 StandardEnvironment。
	 */
	private static Environment getOrCreateEnvironment(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		if (registry instanceof EnvironmentCapable environmentCapable) {
			return environmentCapable.getEnvironment();
		}
		return new StandardEnvironment();
	}

}
