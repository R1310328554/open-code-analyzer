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

package org.springframework.boot.context.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.support.FilePatternResourceHintsRegistrar;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ResourceUtils;

/**
 * 应用配置的 {@link RuntimeHintsRegistrar} 实现。
 *
 * @author Stephane Nicoll
 * @see FilePatternResourceHintsRegistrar
 */
class ConfigDataLocationRuntimeHints implements RuntimeHintsRegistrar {

	private static final Log logger = LogFactory.getLog(ConfigDataLocationRuntimeHints.class);

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
		List<String> fileNames = getFileNames(classLoader);
		List<String> locations = getLocations(classLoader);
		List<String> extensions = getExtensions(classLoader);
		if (logger.isDebugEnabled()) {
			logger.debug("Registering application configuration hints for " + fileNames + "(" + extensions + ") at "
					+ locations);
		}
		FilePatternResourceHintsRegistrar.forClassPathLocations(locations)
			.withFilePrefixes(fileNames)
			.withFileExtensions(extensions)
			.registerHints(hints.resources(), classLoader);
	}

	/**
	 * 获取需考虑的应用文件名。
	 *
	 * @param classLoader 要使用的类加载器
	 * @return 配置文件名列表
	 */
	protected List<String> getFileNames(@Nullable ClassLoader classLoader) {
		return Arrays.asList(StandardConfigDataLocationResolver.DEFAULT_CONFIG_NAMES);
	}

	/**
	 * 获取需考虑的位置。位置为类路径位置，可使用或不使用标准 {@code classpath:} 前缀。
	 *
	 * @param classLoader 要使用的类加载器
	 * @return 配置文件位置列表
	 */
	protected List<String> getLocations(@Nullable ClassLoader classLoader) {
		List<String> classpathLocations = new ArrayList<>();
		for (ConfigDataLocation candidate : ConfigDataEnvironment.DEFAULT_SEARCH_LOCATIONS) {
			for (ConfigDataLocation configDataLocation : candidate.split()) {
				String location = configDataLocation.getValue();
				if (location.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
					classpathLocations.add(location);
				}
			}
		}
		return classpathLocations;
	}

	/**
	 * 获取需考虑的应用文件扩展名。有效扩展名以点号开头。
	 *
	 * @param classLoader 要使用的类加载器
	 * @return 配置文件扩展名列表
	 */
	protected List<String> getExtensions(@Nullable ClassLoader classLoader) {
		List<String> extensions = new ArrayList<>();
		List<PropertySourceLoader> propertySourceLoaders = getSpringFactoriesLoader(classLoader)
			.load(PropertySourceLoader.class);
		for (PropertySourceLoader propertySourceLoader : propertySourceLoaders) {
			for (String fileExtension : propertySourceLoader.getFileExtensions()) {
				String candidate = "." + fileExtension;
				if (!extensions.contains(candidate)) {
					extensions.add(candidate);
				}
			}
		}
		return extensions;
	}

	protected SpringFactoriesLoader getSpringFactoriesLoader(@Nullable ClassLoader classLoader) {
		return SpringFactoriesLoader.forDefaultResourceLocation(classLoader);
	}

}
