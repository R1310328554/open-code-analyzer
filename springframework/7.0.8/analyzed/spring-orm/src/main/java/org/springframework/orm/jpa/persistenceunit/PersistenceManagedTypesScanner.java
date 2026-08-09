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

package org.springframework.orm.jpa.persistenceunit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Converter;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PersistenceException;
import org.jspecify.annotations.Nullable;

import org.springframework.context.index.CandidateComponentsIndex;
import org.springframework.context.index.CandidateComponentsIndexLoader;
import org.springframework.core.SpringProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.ClassFormatException;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

/* ===== [OCA 中文解析] =====
class PersistenceManagedTypesScanner — 意图说明

class `PersistenceManagedTypesScanner`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-orm/src/main/java/org/springframework/orm/jpa/persistenceunit/PersistenceManagedTypesScanner.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Scanner of {@link PersistenceManagedTypes}.
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public final class PersistenceManagedTypesScanner {

	// [OCA] 字段 `CLASS_RESOURCE_PATTERN`：类成员状态。
	private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";

	// [OCA] 字段 `IGNORE_CLASSFORMAT_PROPERTY_NAME`：类成员状态。
	private static final String IGNORE_CLASSFORMAT_PROPERTY_NAME = "spring.classformat.ignore";

	// [OCA] 字段 `shouldIgnoreClassFormatException`：类成员状态。
	private static final boolean shouldIgnoreClassFormatException =
			SpringProperties.getFlag(IGNORE_CLASSFORMAT_PROPERTY_NAME);

	// [OCA] 字段 `entityTypeFilters`：类成员状态。
	private static final Set<AnnotationTypeFilter> entityTypeFilters = CollectionUtils.newLinkedHashSet(4);

	static {
		entityTypeFilters.add(new AnnotationTypeFilter(Entity.class, false));
		entityTypeFilters.add(new AnnotationTypeFilter(Embeddable.class, false));
		entityTypeFilters.add(new AnnotationTypeFilter(MappedSuperclass.class, false));
		entityTypeFilters.add(new AnnotationTypeFilter(Converter.class, false));
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Annotation> discoverable = (Class<? extends Annotation>) ClassUtils.forName(
					"jakarta.persistence.spi.Discoverable", PersistenceManagedTypesScanner.class.getClassLoader());
			entityTypeFilters.add(new AnnotationTypeFilter(discoverable, true));
		}
		catch (ClassNotFoundException ex) {
			// JPA 4.0 API not present - simply skip.
		}
	}

	// [OCA] 字段 `resourcePatternResolver`：类成员状态。
	private final ResourcePatternResolver resourcePatternResolver;

	private final @Nullable CandidateComponentsIndex componentsIndex;

	// [OCA] 字段 `managedClassNameFilter`：类成员状态。
	private final ManagedClassNameFilter managedClassNameFilter;


	/**
	 * Create a new {@code PersistenceManagedTypesScanner} for the given resource loader.
	 * @param resourceLoader the {@code ResourceLoader} to use
	 */
	public PersistenceManagedTypesScanner(ResourceLoader resourceLoader) {
		this(resourceLoader, null);
	}

	/**
	 * Create a new {@code PersistenceManagedTypesScanner} for the given resource loader.
	 * @param resourceLoader the {@code ResourceLoader} to use
	 * @param managedClassNameFilter an optional predicate to filter entity classes
	 * @since 6.1.4
	 */
	public PersistenceManagedTypesScanner(ResourceLoader resourceLoader,
			@Nullable ManagedClassNameFilter managedClassNameFilter) {

		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		this.componentsIndex = CandidateComponentsIndexLoader.loadIndex(resourceLoader.getClassLoader());
		this.managedClassNameFilter = (managedClassNameFilter != null ? managedClassNameFilter : className -> true);
	}


	/**
	 * Scan the specified packages and return a {@link PersistenceManagedTypes} that
	 * represents the result of the scanning.
	 * @param packagesToScan the packages to scan
	 * @return the {@link PersistenceManagedTypes} instance
	 */
	public PersistenceManagedTypes scan(String... packagesToScan) {
		ScanResult scanResult = new ScanResult();
		for (String pkg : packagesToScan) {
			scanPackage(pkg, scanResult);
		}
		return scanResult.toJpaManagedTypes();
	}

	/* ===== [OCA 中文解析] =====
方法 scanPackage — 意图与阅读要点

方法 `scanPackage` 复杂度较高（CCN≈14, NLOC≈49）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。

	===== [OCA 中文解析结束] ===== */

	private void scanPackage(String pkg, ScanResult scanResult) {
		if (this.componentsIndex != null) {
			Set<String> candidates = new HashSet<>();
			for (AnnotationTypeFilter filter : entityTypeFilters) {
				candidates.addAll(this.componentsIndex.getCandidateTypes(pkg, filter.getAnnotationType().getName()));
			}
			scanResult.managedClassNames.addAll(candidates.stream().filter(this.managedClassNameFilter::matches).toList());
			scanResult.managedPackages.addAll(this.componentsIndex.getCandidateTypes(pkg, "package-info"));
			return;
		}

		try {
			String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					ClassUtils.convertClassNameToResourcePath(pkg) + CLASS_RESOURCE_PATTERN;
			Resource[] resources = this.resourcePatternResolver.getResources(pattern);
			MetadataReaderFactory factory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
			for (Resource resource : resources) {
				try {
					MetadataReader reader = factory.getMetadataReader(resource);
					String className = reader.getClassMetadata().getClassName();
					if (className.endsWith(ClassUtils.PACKAGE_INFO_SUFFIX)) {
						scanResult.managedPackages.add(className.substring(0,
								className.length() - ClassUtils.PACKAGE_INFO_SUFFIX.length()));
					}
					else if (matchesEntityTypeFilter(reader, factory) && this.managedClassNameFilter.matches(className)) {
						scanResult.managedClassNames.add(className);
						if (scanResult.persistenceUnitRootUrl == null) {
							URL url = resource.getURL();
							if (ResourceUtils.isJarURL(url)) {
								scanResult.persistenceUnitRootUrl = ResourceUtils.extractJarFileURL(url);
							}
						}
					}
				}
				catch (FileNotFoundException ex) {
					// Ignore non-readable resource
				}
				catch (ClassFormatException ex) {
					if (!shouldIgnoreClassFormatException) {
						throw new PersistenceException("Incompatible class format in " + resource, ex);
					}
				}
				catch (Throwable ex) {
					throw new PersistenceException("Failed to read candidate entity class: " + resource, ex);
				}
			}
		}
		catch (IOException ex) {
			throw new PersistenceException("Failed to scan classpath for unlisted entity classes", ex);
		}
	}

	/**
	 * Check whether any of the configured entity type filters matches
	 * the current class descriptor contained in the metadata reader.
	 */
	private boolean matchesEntityTypeFilter(MetadataReader reader, MetadataReaderFactory factory) throws IOException {
		for (TypeFilter filter : entityTypeFilters) {
			if (filter.match(reader, factory)) {
				return true;
			}
		}
		return false;
	}


	/* ===== [OCA 中文解析] =====
class ScanResult — 意图说明

class `ScanResult`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-orm/src/main/java/org/springframework/orm/jpa/persistenceunit/PersistenceManagedTypesScanner.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）


	===== [OCA 中文解析结束] ===== */


	private static class ScanResult {

		private final List<String> managedClassNames = new ArrayList<>();

		private final List<String> managedPackages = new ArrayList<>();

		private @Nullable URL persistenceUnitRootUrl;

		PersistenceManagedTypes toJpaManagedTypes() {
			return new SimplePersistenceManagedTypes(this.managedClassNames,
					this.managedPackages, this.persistenceUnitRootUrl);
		}
	}

}
