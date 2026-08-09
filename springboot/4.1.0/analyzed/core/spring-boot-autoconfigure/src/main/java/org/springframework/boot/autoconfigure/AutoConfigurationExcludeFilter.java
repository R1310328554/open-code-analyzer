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

import java.io.IOException;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * 匹配已注册自动配置类的 {@link TypeFilter} 实现。
 *
 * @author Stephane Nicoll
 * @author Scott Frederick
 * @since 1.5.0
 */
public class AutoConfigurationExcludeFilter implements TypeFilter, BeanClassLoaderAware {

	/** 用于加载自动配置候选类的类加载器。 */
	@SuppressWarnings("NullAway.Init")
	private ClassLoader beanClassLoader;

	/** 缓存的自动配置类全限定名列表。 */
	private volatile @Nullable List<String> autoConfigurations;

	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	@Override
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
			throws IOException {
		return isConfiguration(metadataReader) && isAutoConfiguration(metadataReader);
	}

	/** 判断元数据是否标注了 {@link Configuration}。 */
	private boolean isConfiguration(MetadataReader metadataReader) {
		return metadataReader.getAnnotationMetadata().isAnnotated(Configuration.class.getName());
	}

	/** 判断是否为自动配置类（直接标注或出现在候选列表中）。 */
	private boolean isAutoConfiguration(MetadataReader metadataReader) {
		boolean annotatedWithAutoConfiguration = metadataReader.getAnnotationMetadata()
			.isAnnotated(AutoConfiguration.class.getName());
		return annotatedWithAutoConfiguration
				|| getAutoConfigurations().contains(metadataReader.getClassMetadata().getClassName());
	}

	/** 懒加载并返回所有自动配置类名称。 */
	protected List<String> getAutoConfigurations() {
		List<String> autoConfigurations = this.autoConfigurations;
		if (autoConfigurations == null) {
			ImportCandidates importCandidates = ImportCandidates.load(AutoConfiguration.class, this.beanClassLoader);
			autoConfigurations = importCandidates.getCandidates();
			this.autoConfigurations = autoConfigurations;
		}
		return autoConfigurations;
	}

}
