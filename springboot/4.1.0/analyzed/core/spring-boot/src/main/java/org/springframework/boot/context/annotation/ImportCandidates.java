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

package org.springframework.boot.context.annotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;

/**
 * 包含 {@code @Configuration} 导入候选（通常为自动配置类）。
 * <p>
 * 可通过 {@link #load(Class, ClassLoader)} 发现导入候选。
 *
 * @author Moritz Halbritter
 * @author Scott Frederick
 * @since 2.7.0
 */
public final class ImportCandidates implements Iterable<String> {

	private static final String LOCATION = "META-INF/spring/%s.imports";

	private static final String COMMENT_START = "#";

	private final List<String> candidates;

	private ImportCandidates(List<String> candidates) {
		Assert.notNull(candidates, "'candidates' must not be null");
		this.candidates = Collections.unmodifiableList(candidates);
	}

	@Override
	public Iterator<String> iterator() {
		return this.candidates.iterator();
	}

	/**
	 * 返回已加载的导入候选列表。
	 *
	 * @return 导入候选列表
	 */
	public List<String> getCandidates() {
		return this.candidates;
	}

	/**
	 * 从类路径加载导入候选类名。候选名存储于
	 * {@code META-INF/spring/全限定注解名.imports} 文件中，每行一个候选类全限定名，
	 * 支持以 {@code #} 开头的注释。
	 *
	 * @param annotation 要加载的注解
	 * @param classLoader 用于加载的类加载器
	 * @return 候选类名列表
	 */
	public static ImportCandidates load(Class<?> annotation, @Nullable ClassLoader classLoader) {
		Assert.notNull(annotation, "'annotation' must not be null");
		ClassLoader classLoaderToUse = decideClassloader(classLoader);
		String location = String.format(LOCATION, annotation.getName());
		Enumeration<URL> urls = findUrlsInClasspath(classLoaderToUse, location);
		List<String> importCandidates = new ArrayList<>();
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			importCandidates.addAll(readCandidateConfigurations(url));
		}
		return new ImportCandidates(importCandidates);
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

	private static List<String> readCandidateConfigurations(URL url) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new UrlResource(url).getInputStream(), StandardCharsets.UTF_8))) {
			List<String> candidates = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				line = stripComment(line);
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				candidates.add(line);
			}
			return candidates;
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load configurations from location [" + url + "]", ex);
		}
	}

	private static String stripComment(String line) {
		int commentStart = line.indexOf(COMMENT_START);
		if (commentStart == -1) {
			return line;
		}
		return line.substring(0, commentStart);
	}

}
