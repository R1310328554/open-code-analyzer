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

package org.springframework.context.index;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.SpringProperties;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * 框架内部使用的候选组件索引加载机制。
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 5.0
 */
public final class CandidateComponentsIndexLoader {

	/**
	 * 查找组件资源的位置。
	 * <p>可存在于多个 JAR 文件中。
	 */
	public static final String COMPONENTS_RESOURCE_LOCATION = "META-INF/spring.components";

	/**
	 * 指示 Spring 忽略组件索引的系统属性，即
	 * 使 {@link #loadIndex(ClassLoader)} 始终返回 {@code null}。
	 * <p>默认为 {@code false}，允许正常使用索引。将此标志设为 {@code true}
	 * 适用于部分库（或场景）有索引但整个应用无法构建索引的边角情况：
	 * 此时应用上下文将回退到常规类路径扫描（如同索引不存在）。
	 */
	public static final String IGNORE_INDEX = "spring.index.ignore";


	private static final boolean shouldIgnoreIndex = SpringProperties.getFlag(IGNORE_INDEX);

	private static final Log logger = LogFactory.getLog(CandidateComponentsIndexLoader.class);

	/** 按 ClassLoader 缓存已加载的索引。 */
	private static final ConcurrentMap<ClassLoader, CandidateComponentsIndex> cache =
			new ConcurrentReferenceHashMap<>();


	private CandidateComponentsIndexLoader() {
	}


	/**
	 * 从 {@value #COMPONENTS_RESOURCE_LOCATION} 加载并实例化
	 * {@link CandidateComponentsIndex}，使用给定类加载器。若无可用索引则返回 {@code null}。
	 * @param classLoader 用于加载的 ClassLoader（可为 {@code null} 以使用默认加载器）
	 * @return 要使用的索引，或 {@code null} 表示未找到索引
	 * @throws IllegalArgumentException 若任一模块索引无法加载，
	 * 或在创建 {@link CandidateComponentsIndex} 时发生错误
	 */
	public static @Nullable CandidateComponentsIndex loadIndex(@Nullable ClassLoader classLoader) {
		ClassLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			classLoaderToUse = CandidateComponentsIndexLoader.class.getClassLoader();
		}
		return cache.computeIfAbsent(classLoaderToUse, CandidateComponentsIndexLoader::doLoadIndex);
	}

	private static @Nullable CandidateComponentsIndex doLoadIndex(ClassLoader classLoader) {
		if (shouldIgnoreIndex) {
			return null;
		}

		try {
			Enumeration<URL> urls = classLoader.getResources(COMPONENTS_RESOURCE_LOCATION);
			if (!urls.hasMoreElements()) {
				return null;
			}
			List<Properties> result = new ArrayList<>();
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				Properties properties = PropertiesLoaderUtils.loadProperties(new UrlResource(url));
				result.add(properties);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + result.size() + " index(es)");
			}
			int totalCount = result.stream().mapToInt(Properties::size).sum();
			return (totalCount > 0 ? new CandidateComponentsIndex(result) : null);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Unable to load indexes from location [" +
					COMPONENTS_RESOURCE_LOCATION + "]", ex);
		}
	}


	/**
	 * 为给定 ClassLoader 以编程方式添加索引实例，
	 * 用编程式组装的索引替换由文件确定的索引。
	 * <p>索引实例通常预先填充，用于 AOT 运行时设置或
	 * 预配置运行时扫描结果的测试场景。也可为空索引，
	 * 在 AOT 处理或测试运行期间填充，供后续内省索引记录的候选类型。
	 * @param classLoader 要添加索引的 ClassLoader
	 * @param index 关联的 CandidateComponentsIndex 实例
	 * @since 7.0
	 */
	public static void addIndex(ClassLoader classLoader, CandidateComponentsIndex index) {
		cache.put(classLoader, index);
	}

	/**
	 * 清空运行时索引缓存。
	 * @since 7.0
	 */
	public static void clearCache() {
		cache.clear();
	}

}
