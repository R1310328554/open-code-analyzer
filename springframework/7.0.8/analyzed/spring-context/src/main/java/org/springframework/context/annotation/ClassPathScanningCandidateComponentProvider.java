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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.index.CandidateComponentsIndex;
import org.springframework.context.index.CandidateComponentsIndexLoader;
import org.springframework.core.SpringProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.ClassFormatException;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 从指定基础包起扫描候选组件的提供者。
 * <p>若存在 {@linkplain CandidateComponentsIndex 组件索引}则优先使用索引，否则回退到 classpath 扫描。
 *
 * <p>通过 include/exclude 过滤器识别候选组件。
 * 对标注了 {@link Indexed} 的注解或目标类型，支持 {@link AnnotationTypeFilter} 与
 * {@link AssignableTypeFilter} 作为 include 过滤器；若配置了其他 include 过滤器，
 * 则忽略索引并改用 classpath 扫描。
 *
 * <p>实现基于 Spring 的 {@link org.springframework.core.type.classreading.MetadataReader MetadataReader}，
 * 底层由 ASM {@link org.springframework.asm.ClassReader ClassReader} 支撑。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author Chris Beams
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 2.5
 * @see org.springframework.core.type.classreading.MetadataReaderFactory
 * @see org.springframework.core.type.AnnotationMetadata
 * @see ScannedGenericBeanDefinition
 * @see CandidateComponentsIndex
 */
public class ClassPathScanningCandidateComponentProvider implements EnvironmentCapable, ResourceLoaderAware {

	/** 默认 classpath 资源匹配模式：所有 {@code .class} 文件。 */
	static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	/**
	 * 系统属性：classpath 扫描时忽略类格式异常（例如不支持的 class 文件版本）。
	 * 默认情况下类格式不匹配会导致扫描失败。
	 * @since 6.1.2
	 * @see ClassFormatException
	 */
	public static final String IGNORE_CLASSFORMAT_PROPERTY_NAME = "spring.classformat.ignore";

	/** 是否根据系统属性忽略类格式异常。 */
	private static final boolean shouldIgnoreClassFormatException =
			SpringProperties.getFlag(IGNORE_CLASSFORMAT_PROPERTY_NAME);


	protected final Log logger = LogFactory.getLog(getClass());

	/** 扫描时追加到每个基础包名后的资源模式。 */
	private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

	/** include 类型过滤器列表（按添加顺序匹配）。 */
	private final List<TypeFilter> includeFilters = new ArrayList<>();

	/** exclude 类型过滤器列表（新添加的过滤器优先匹配）。 */
	private final List<TypeFilter> excludeFilters = new ArrayList<>();

	private @Nullable Environment environment;

	private @Nullable ConditionEvaluator conditionEvaluator;

	private @Nullable ResourcePatternResolver resourcePatternResolver;

	private @Nullable MetadataReaderFactory metadataReaderFactory;

	/** 候选组件索引（若 classpath 上存在 {@code META-INF/spring.components}）。 */
	private @Nullable CandidateComponentsIndex componentsIndex;


	/**
	 * 受保护构造器，供子类灵活初始化。
	 * @since 4.3.6
	 */
	protected ClassPathScanningCandidateComponentProvider() {
	}

	/**
	 * 使用 {@link StandardEnvironment} 创建扫描提供者。
	 * @param useDefaultFilters 是否注册 {@link Component @Component}、
	 * {@link Repository @Repository}、{@link Service @Service}、
	 * {@link Controller @Controller} 等构造型注解的默认过滤器
	 * @see #registerDefaultFilters()
	 */
	public ClassPathScanningCandidateComponentProvider(boolean useDefaultFilters) {
		this(useDefaultFilters, new StandardEnvironment());
	}

	/**
	 * 使用给定 {@link Environment} 创建扫描提供者。
	 * @param useDefaultFilters 是否注册 {@link Component @Component}、
	 * {@link Repository @Repository}、{@link Service @Service}、
	 * {@link Controller @Controller} 等构造型注解的默认过滤器
	 * @param environment 使用的 Environment
	 * @see #registerDefaultFilters()
	 */
	public ClassPathScanningCandidateComponentProvider(boolean useDefaultFilters, Environment environment) {
		if (useDefaultFilters) {
			registerDefaultFilters();
		}
		setEnvironment(environment);
		setResourceLoader(null);
	}


	/**
	 * 设置 classpath 扫描使用的资源模式，会追加到每个基础包名之后。
	 * @see #findCandidateComponents(String)
	 * @see #DEFAULT_RESOURCE_PATTERN
	 */
	public void setResourcePattern(String resourcePattern) {
		Assert.notNull(resourcePattern, "'resourcePattern' must not be null");
		this.resourcePattern = resourcePattern;
	}

	/**
	 * 将 include 类型过滤器追加到 inclusion 列表末尾。
	 */
	public void addIncludeFilter(TypeFilter includeFilter) {
		this.includeFilters.add(includeFilter);
	}

	/**
	 * 将 exclude 类型过滤器插入 exclusion 列表头部。
	 */
	public void addExcludeFilter(TypeFilter excludeFilter) {
		this.excludeFilters.add(0, excludeFilter);
	}

	/**
	 * 重置已配置的类型过滤器。
	 * @param useDefaultFilters 是否重新注册 {@link Component @Component}、
	 * {@link Repository @Repository}、{@link Service @Service}、
	 * {@link Controller @Controller} 等构造型注解的默认过滤器
	 * @see #registerDefaultFilters()
	 */
	public void resetFilters(boolean useDefaultFilters) {
		this.includeFilters.clear();
		this.excludeFilters.clear();
		if (useDefaultFilters) {
			registerDefaultFilters();
		}
	}

	/**
	 * 注册 {@link Component @Component} 的默认过滤器。
	 * <p>会隐式注册所有带 {@link Component @Component} 元注解的注解，
	 * 包括 {@link Repository @Repository}、{@link Service @Service}、
	 * {@link Controller @Controller} 等构造型注解。
	 * <p>若可用，也支持 JSR-330 的 {@link jakarta.inject.Named} 注解。
	 */
	@SuppressWarnings("unchecked")
	protected void registerDefaultFilters() {
		this.includeFilters.add(new AnnotationTypeFilter(Component.class));
		ClassLoader cl = ClassPathScanningCandidateComponentProvider.class.getClassLoader();
		try {
			this.includeFilters.add(new AnnotationTypeFilter(
					((Class<? extends Annotation>) ClassUtils.forName("jakarta.inject.Named", cl)), false));
			logger.trace("JSR-330 'jakarta.inject.Named' annotation found and supported for component scanning");
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API (as included in Jakarta EE) not available - simply skip.
		}
	}

	/**
	 * 设置用于解析占位符及评估 {@link Conditional @Conditional} 组件类的 Environment。
	 * <p>默认为 {@link StandardEnvironment}。
	 * @param environment 使用的 Environment
	 */
	public void setEnvironment(Environment environment) {
		Assert.notNull(environment, "Environment must not be null");
		this.environment = environment;
		this.conditionEvaluator = null;
	}

	@Override
	public final Environment getEnvironment() {
		if (this.environment == null) {
			this.environment = new StandardEnvironment();
		}
		return this.environment;
	}

	/**
	 * 返回本扫描器使用的 {@link BeanDefinitionRegistry}（若有）。
	 */
	protected @Nullable BeanDefinitionRegistry getRegistry() {
		return null;
	}

	/**
	 * 设置用于定位资源的 {@link ResourceLoader}，通常为 {@link ResourcePatternResolver} 实现。
	 * <p>默认为 {@code PathMatchingResourcePatternResolver}，亦可通过
	 * {@code ResourcePatternResolver} 接口解析资源模式。
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 */
	@Override
	public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
		this.componentsIndex = CandidateComponentsIndexLoader.loadIndex(this.resourcePatternResolver.getClassLoader());
	}

	/**
	 * 返回本组件提供者使用的 ResourceLoader。
	 */
	public final ResourceLoader getResourceLoader() {
		return getResourcePatternResolver();
	}

	private ResourcePatternResolver getResourcePatternResolver() {
		if (this.resourcePatternResolver == null) {
			this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
		}
		return this.resourcePatternResolver;
	}

	/**
	 * 设置要使用的 {@link MetadataReaderFactory}。
	 * <p>默认为针对 {@linkplain #setResourceLoader 资源加载器} 的
	 * {@link CachingMetadataReaderFactory}。
	 * <p>若需覆盖默认工厂，应在 {@link #setResourceLoader} 之后调用本方法。
	 */
	public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
		this.metadataReaderFactory = metadataReaderFactory;
	}

	/**
	 * 返回本组件提供者使用的 MetadataReaderFactory。
	 */
	public final MetadataReaderFactory getMetadataReaderFactory() {
		if (this.metadataReaderFactory == null) {
			this.metadataReaderFactory = new CachingMetadataReaderFactory();
		}
		return this.metadataReaderFactory;
	}


	/**
	 * 扫描组件索引或 classpath 以查找候选组件。
	 * @param basePackage 待检查注解类的基础包
	 * @return 自动检测到的 BeanDefinition 集合
	 */
	public Set<BeanDefinition> findCandidateComponents(String basePackage) {
		// 1. 索引可用且过滤器兼容时，优先走索引路径
		if (this.componentsIndex != null && indexSupportsIncludeFilters()) {
			if (this.componentsIndex.hasScannedPackage(basePackage)) {
				return addCandidateComponentsFromIndex(this.componentsIndex, basePackage);
			}
			else {
				this.componentsIndex.registerScan(basePackage);
			}
		}
		// 2. 回退到 classpath 扫描
		return scanCandidateComponents(basePackage);
	}

	/**
	 * 判断本实例能否使用组件索引。
	 * @return 索引可用且当前配置被其支持时为 {@code true}，否则为 {@code false}
	 * @since 5.0
	 */
	private boolean indexSupportsIncludeFilters() {
		for (TypeFilter includeFilter : this.includeFilters) {
			if (!indexSupportsIncludeFilter(includeFilter)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断指定 include {@link TypeFilter} 是否被索引支持。
	 * @param filter 待检查的过滤器
	 * @return 索引是否支持该 include 过滤器
	 * @since 5.0
	 * @see #registerCandidateTypeForIncludeFilter(String, TypeFilter)
	 * @see #extractStereotype(TypeFilter)
	 */
	private boolean indexSupportsIncludeFilter(TypeFilter filter) {
		if (filter instanceof AnnotationTypeFilter annotationTypeFilter) {
			Class<? extends Annotation> annotationType = annotationTypeFilter.getAnnotationType();
			return isStereotypeAnnotationForIndex(annotationType);
		}
		if (filter instanceof AssignableTypeFilter assignableTypeFilter) {
			Class<?> target = assignableTypeFilter.getTargetType();
			return AnnotationUtils.isAnnotationDeclaredLocally(Indexed.class, target);
		}
		return false;
	}

	/**
	 * 将给定类注册为运行时填充索引中的候选类型（若有索引）。
	 * @param className 候选类型的全限定类名
	 * @param filter 用于提取关联构造型注解的 include 过滤器
	 */
	private void registerCandidateTypeForIncludeFilter(String className, TypeFilter filter) {
		if (this.componentsIndex != null) {
			if (filter instanceof AnnotationTypeFilter annotationTypeFilter) {
				Class<? extends Annotation> annotationType = annotationTypeFilter.getAnnotationType();
				if (isStereotypeAnnotationForIndex(annotationType)) {
					this.componentsIndex.registerCandidateType(className, annotationType.getName());
				}
			}
			else if (filter instanceof AssignableTypeFilter assignableTypeFilter) {
				Class<?> target = assignableTypeFilter.getTargetType();
				if (AnnotationUtils.isAnnotationDeclaredLocally(Indexed.class, target)) {
					this.componentsIndex.registerCandidateType(className, target.getName());
				}
			}
		}
	}

	/**
	 * 从兼容的过滤器中提取索引所用的构造型标识。
	 * @param filter 待处理的过滤器
	 * @return 索引中与该过滤器匹配的构造型名称
	 * @since 5.0
	 * @see #indexSupportsIncludeFilter(TypeFilter)
	 */
	private @Nullable String extractStereotype(TypeFilter filter) {
		if (filter instanceof AnnotationTypeFilter annotationTypeFilter) {
			return annotationTypeFilter.getAnnotationType().getName();
		}
		if (filter instanceof AssignableTypeFilter assignableTypeFilter) {
			return assignableTypeFilter.getTargetType().getName();
		}
		return null;
	}

	/** 判断注解类型是否可作为索引中的构造型键（{@link Indexed} 或 jakarta 命名空间）。 */
	private boolean isStereotypeAnnotationForIndex(Class<? extends Annotation> annotationType) {
		return (AnnotationUtils.isAnnotationDeclaredLocally(Indexed.class, annotationType) ||
				annotationType.getName().startsWith("jakarta."));
	}

	private Set<BeanDefinition> addCandidateComponentsFromIndex(CandidateComponentsIndex index, String basePackage) {
		Set<BeanDefinition> candidates = new LinkedHashSet<>();
		try {
			// 1. 按各 include 过滤器从索引收集候选类型名
			Set<String> types = new HashSet<>();
			for (TypeFilter filter : this.includeFilters) {
				String stereotype = extractStereotype(filter);
				if (stereotype == null) {
					throw new IllegalArgumentException("Failed to extract stereotype from " + filter);
				}
				types.addAll(index.getCandidateTypes(basePackage, stereotype));
			}
			boolean traceEnabled = logger.isTraceEnabled();
			boolean debugEnabled = logger.isDebugEnabled();
			// 2. 逐个读取元数据并应用过滤器与条件评估
			for (String type : types) {
				MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(type);
				if (isCandidateComponent(metadataReader)) {
					ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
					sbd.setSource(metadataReader.getResource());
					if (isCandidateComponent(sbd)) {
						if (debugEnabled) {
							logger.debug("Using candidate component class from index: " + type);
						}
						candidates.add(sbd);
					}
					else {
						if (debugEnabled) {
							logger.debug("Ignored because not a concrete top-level class: " + type);
						}
					}
				}
				else {
					if (traceEnabled) {
						logger.trace("Ignored because matching an exclude filter: " + type);
					}
				}
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}

	private Set<BeanDefinition> scanCandidateComponents(String basePackage) {
		Set<BeanDefinition> candidates = new LinkedHashSet<>();
		try {
			// 1. 构造包搜索路径并加载所有匹配资源
			String packageSearchPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					resolveBasePackage(basePackage) + '/' + this.resourcePattern;
			Resource[] resources = getResourcePatternResolver().getResources(packageSearchPattern);
			boolean traceEnabled = logger.isTraceEnabled();
			boolean debugEnabled = logger.isDebugEnabled();
			// 2. 遍历每个 .class 资源
			for (Resource resource : resources) {
				String filename = resource.getFilename();
				if (filename != null && filename.contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
					// Ignore CGLIB-generated classes in the classpath
					continue;
				}
				if (traceEnabled) {
					logger.trace("Scanning " + resource);
				}
				try {
					MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
					if (isCandidateComponent(metadataReader)) {
						ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
						sbd.setSource(resource);
						if (isCandidateComponent(sbd)) {
							if (debugEnabled) {
								logger.debug("Identified candidate component class: " + resource);
							}
							candidates.add(sbd);
						}
						else {
							if (debugEnabled) {
								logger.debug("Ignored because not a concrete top-level class: " + resource);
							}
						}
					}
					else {
						if (traceEnabled) {
							logger.trace("Ignored because not matching any filter: " + resource);
						}
					}
				}
				catch (FileNotFoundException ex) {
					if (traceEnabled) {
						logger.trace("Ignored non-readable " + resource + ": " + ex.getMessage());
					}
				}
				catch (ClassFormatException ex) {
					if (shouldIgnoreClassFormatException) {
						if (debugEnabled) {
							logger.debug("Ignored incompatible class format in " + resource + ": " + ex.getMessage());
						}
					}
					else {
						throw new BeanDefinitionStoreException("Incompatible class format in " + resource +
								": set system property 'spring.classformat.ignore' to 'true' " +
								"if you mean to ignore such files during classpath scanning", ex);
					}
				}
				catch (Throwable ex) {
					throw new BeanDefinitionStoreException("Failed to read candidate component class: " + resource, ex);
				}
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}


	/**
	 * 将基础包名解析为包搜索路径的模式规格。
	 * <p>默认实现先对系统属性解析占位符，再将 {@code .} 分隔的包路径转为 {@code /} 分隔的资源路径。
	 * @param basePackage 用户指定的基础包
	 * @return 用于包搜索的模式规格
	 */
	protected String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(getEnvironment().resolveRequiredPlaceholders(basePackage));
	}

	/**
	 * 判断给定类是否不匹配任何 exclude 过滤器且至少匹配一个 include 过滤器。
	 * @param metadataReader 该类的 ASM ClassReader
	 * @return 是否可作为候选组件
	 */
	protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
		// 1. exclude 过滤器优先：任一匹配则排除
		for (TypeFilter filter : this.excludeFilters) {
			if (filter.match(metadataReader, getMetadataReaderFactory())) {
				return false;
			}
		}
		// 2. include 过滤器：匹配后还需通过 @Conditional 评估
		for (TypeFilter filter : this.includeFilters) {
			if (filter.match(metadataReader, getMetadataReaderFactory())) {
				registerCandidateTypeForIncludeFilter(metadataReader.getClassMetadata().getClassName(), filter);
				return isConditionMatch(metadataReader);
			}
		}
		return false;
	}

	/**
	 * 根据 {@code @Conditional} 注解判断给定类是否可作为候选组件。
	 * @param metadataReader 该类的 ASM ClassReader
	 * @return 是否可作为候选组件
	 */
	private boolean isConditionMatch(MetadataReader metadataReader) {
		if (this.conditionEvaluator == null) {
			this.conditionEvaluator =
					new ConditionEvaluator(getRegistry(), this.environment, this.resourcePatternResolver);
		}
		return !this.conditionEvaluator.shouldSkip(metadataReader.getAnnotationMetadata());
	}

	/**
	 * 判断给定 BeanDefinition 是否可作为候选组件。
	 * <p>默认实现检查类是否不依赖外部类，且为具体类（非接口）或
	 * 带有 {@link Lookup @Lookup} 方法的抽象类。
	 * <p>子类可覆盖。
	 * @param beanDefinition 待检查的 BeanDefinition
	 * @return 是否可作为候选组件
	 */
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		AnnotationMetadata metadata = beanDefinition.getMetadata();
		return (metadata.isIndependent() && (metadata.isConcrete() ||
				(metadata.isAbstract() && metadata.hasAnnotatedMethods(Lookup.class.getName()))));
	}


	/**
	 * 清除本地元数据缓存（若有），移除所有已缓存的类元数据。
	 */
	public void clearCache() {
		if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory cmrf) {
			// Clear cache in externally provided MetadataReaderFactory; this is a no-op
			// for a shared cache since it'll be cleared by the ApplicationContext.
			cmrf.clearCache();
		}
	}

}
