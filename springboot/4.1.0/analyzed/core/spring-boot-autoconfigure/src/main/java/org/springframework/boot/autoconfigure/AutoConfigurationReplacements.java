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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;

/**
 * 包含自动配置替换映射，用于处理已弃用或已迁移、但仍可能被
 * {@link AutoConfigureBefore @AutoConfigureBefore}、
 * {@link AutoConfigureAfter @AutoConfigureAfter} 或排除项引用的自动配置类。
 *
 * @author Phillip Webb
 */
final class AutoConfigurationReplacements {

	/** 替换映射文件在类路径上的位置模板。 */
	private static final String LOCATION = "META-INF/spring/%s.replacements";

	/** 旧类名到新类名的不可变映射。 */
	private final Map<String, String> replacements;

	private AutoConfigurationReplacements(Map<String, String> replacements) {
		this.replacements = Map.copyOf(replacements);
	}

	/** 批量替换集合中的类名。 */
	Set<String> replaceAll(Set<String> classNames) {
		Set<String> replaced = new LinkedHashSet<>(classNames.size());
		for (String className : classNames) {
			replaced.add(replace(className));
		}
		return replaced;
	}

	/** 将单个类名替换为映射后的新名称（无映射则原样返回）。 */
	String replace(String className) {
		return this.replacements.getOrDefault(className, className);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return this.replacements.equals(((AutoConfigurationReplacements) obj).replacements);
	}

	@Override
	public int hashCode() {
		return this.replacements.hashCode();
	}

	/**
	 * 从类路径加载替换映射。映射文件位于
	 * {@code META-INF/spring/全限定注解名.replacements}，使用
	 * {@link Properties#load(java.io.InputStream)} 加载，
	 * 键为原自动配置类名，值为替换后的类名。
	 * @param annotation 要加载的注解类型
	 * @param classLoader 用于加载的类加载器
	 * @return 替换映射实例
	 */
	static AutoConfigurationReplacements load(Class<?> annotation, @Nullable ClassLoader classLoader) {
		Assert.notNull(annotation, "'annotation' must not be null");
		ClassLoader classLoaderToUse = decideClassloader(classLoader);
		String location = String.format(LOCATION, annotation.getName());
		Enumeration<URL> urls = findUrlsInClasspath(classLoaderToUse, location);
		Map<String, String> replacements = new HashMap<>();
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			replacements.putAll(readReplacements(url));
		}
		return new AutoConfigurationReplacements(replacements);
	}

	private static ClassLoader decideClassloader(@Nullable ClassLoader classLoader) {
		if (classLoader == null) {
			return ImportCandidates.class.getClassLoader();
		}
		return classLoader;
	}

	private static Enumeration<URL> findUrlsInClasspath(ClassLoader classLoader, String location) {
		try {
			return classLoader.getResources(location);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Failed to load configurations from location [" + location + "]", ex);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, String> readReplacements(URL url) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new UrlResource(url).getInputStream(), StandardCharsets.UTF_8))) {
			Properties properties = new Properties();
			properties.load(reader);
			return (Map) properties;
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load replacements from location [" + url + "]", ex);
		}
	}

}
