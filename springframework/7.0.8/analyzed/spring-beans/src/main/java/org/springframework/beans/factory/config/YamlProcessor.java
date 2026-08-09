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

package org.springframework.beans.factory.config;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.ComposerException;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

import org.springframework.core.CollectionFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * YAML 工厂的基类。
 *
 * <p>需要 SnakeYAML 2.0 或更高版本。
 *
 * @author Dave Syer
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Brian Clozel
 * @since 4.1
 */
public abstract class YamlProcessor {

	private final Log logger = LogFactory.getLog(getClass());

	/** 资源解析方法，默认为 OVERRIDE */
	private ResolutionMethod resolutionMethod = ResolutionMethod.OVERRIDE;

	/** 要加载的 YAML 资源数组 */
	private Resource[] resources = new Resource[0];

	/** 文档匹配器列表，用于选择性加载 YAML 资源中的部分文档 */
	private List<DocumentMatcher> documentMatchers = Collections.emptyList();

	/** 所有文档匹配器均弃权时，文档是否仍应匹配，默认为 true */
	private boolean matchDefault = true;

	/** 可从 YAML 文档加载的支持类型集合 */
	private Set<String> supportedTypes = Collections.emptySet();


	/**
	 * 设置文档匹配器映射，允许调用方仅选择性使用 YAML 资源中的部分文档。
	 * 在 YAML 中，文档以 {@code ---} 行分隔，每个文档在匹配前
	 * 会先转换为 properties。例如：
	 * <pre class="code">
	 * environment: dev
	 * url: https://dev.bar.com
	 * name: Developer Setup
	 * ---
	 * environment: prod
	 * url:https://foo.bar.com
	 * name: My Cool App
	 * </pre>
	 * 配合以下匹配器：
	 * <pre class="code">
	 * setDocumentMatchers(properties -&gt;
	 *     ("prod".equals(properties.getProperty("environment")) ? MatchStatus.FOUND : MatchStatus.NOT_FOUND));
	 * </pre>
	 * 将得到：
	 * <pre class="code">
	 * environment=prod
	 * url=https://foo.bar.com
	 * name=My Cool App
	 * </pre>
	 */
	public void setDocumentMatchers(DocumentMatcher... matchers) {
		this.documentMatchers = List.of(matchers);
	}

	/**
	 * 指示所有 {@link #setDocumentMatchers(DocumentMatcher...) 文档匹配器}均弃权时，
	 * 文档是否仍应匹配。默认为 {@code true}。
	 */
	public void setMatchDefault(boolean matchDefault) {
		this.matchDefault = matchDefault;
	}

	/**
	 * 设置资源解析方法。每个资源将转换为 Map，
	 * 此属性用于决定本工厂最终输出中保留哪些 Map 条目。
	 * 默认为 {@link ResolutionMethod#OVERRIDE}。
	 */
	public void setResolutionMethod(ResolutionMethod resolutionMethod) {
		Assert.notNull(resolutionMethod, "ResolutionMethod must not be null");
		this.resolutionMethod = resolutionMethod;
	}

	/**
	 * 设置要加载的 YAML {@link Resource 资源}位置。
	 * @see ResolutionMethod
	 */
	public void setResources(Resource... resources) {
		this.resources = resources;
	}

	/**
	 * 设置可从 YAML 文档加载的支持类型。
	 * <p>若未配置支持类型，则仅支持 YAML 文档中遇到的 Java 标准类
	 * （定义于 {@link org.yaml.snakeyaml.constructor.SafeConstructor}）。
	 * 若遇到不支持的类型，处理对应 YAML 节点时将抛出 {@link ComposerException}。
	 * @param supportedTypes 支持类型，或传入空数组以清除支持类型
	 * @since 5.1.16
	 * @see #createYaml()
	 */
	public void setSupportedTypes(Class<?>... supportedTypes) {
		if (ObjectUtils.isEmpty(supportedTypes)) {
			this.supportedTypes = Collections.emptySet();
		}
		else {
			Assert.noNullElements(supportedTypes, "'supportedTypes' must not contain null elements");
			this.supportedTypes = Arrays.stream(supportedTypes).map(Class::getName)
					.collect(Collectors.toUnmodifiableSet());
		}
	}

	/**
	 * 为子类提供处理从所供资源解析出的 YAML 的机会。
	 * 依次解析每个资源，并根据 {@link #setDocumentMatchers(DocumentMatcher...) 匹配器}
	 * 检查其中的文档。若文档匹配，则连同其 Properties 表示一起传入回调。
	 * 根据 {@link #setResolutionMethod(ResolutionMethod)}，并非所有文档都会被解析。
	 * @param callback 找到匹配文档后委托的回调
	 * @see #createYaml()
	 */
	protected void process(MatchCallback callback) {
		Yaml yaml = createYaml();
		for (Resource resource : this.resources) {
			boolean found = process(callback, yaml, resource);
			if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND && found) {
				return;
			}
		}
	}

	/**
	 * 创建要使用的 {@link Yaml} 实例。
	 * <p>默认实现将 "allowDuplicateKeys" 标志设为 {@code false}，
	 * 启用内置的重复键处理。
	 * <p>若已配置自定义 {@linkplain #setSupportedTypes 支持类型}，
	 * 默认实现将创建过滤 YAML 文档中不支持类型的 {@code Yaml} 实例。
	 * 若遇到不支持的类型，处理节点时将抛出 {@link ComposerException}。
	 * @see LoaderOptions#setAllowDuplicateKeys(boolean)
	 */
	protected Yaml createYaml() {
		LoaderOptions loaderOptions = new LoaderOptions();
		loaderOptions.setAllowDuplicateKeys(false);
		loaderOptions.setTagInspector(new SupportedTagInspector());
		DumperOptions dumperOptions = new DumperOptions();
		return new Yaml(new Constructor(loaderOptions), new Representer(dumperOptions),
				dumperOptions, loaderOptions);
	}

	private boolean process(MatchCallback callback, Yaml yaml, Resource resource) {
		int count = 0;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Loading from YAML: " + resource);
			}
			try (Reader reader = new UnicodeReader(resource.getInputStream())) {
				for (Object object : yaml.loadAll(reader)) {
					if (object != null && process(asMap(object), callback)) {
						count++;
						if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND) {
							break;
						}
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Loaded " + count + " document" + (count > 1 ? "s" : "") +
							" from YAML resource: " + resource);
				}
			}
		}
		catch (IOException ex) {
			handleProcessError(resource, ex);
		}
		return (count > 0);
	}

	private void handleProcessError(Resource resource, IOException ex) {
		if (this.resolutionMethod != ResolutionMethod.FIRST_FOUND &&
				this.resolutionMethod != ResolutionMethod.OVERRIDE_AND_IGNORE) {
			throw new IllegalStateException(ex);
		}
		if (logger.isWarnEnabled()) {
			logger.warn("Could not load map from " + resource + ": " + ex.getMessage());
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Map<String, Object> asMap(Object object) {
		// YAML 的键可以是数字
		Map<String, Object> result = new LinkedHashMap<>();
		if (!(object instanceof Map map)) {
			// 文档可以是文本字面量
			result.put("document", object);
			return result;
		}

		map.forEach((key, value) -> {
			if (value instanceof Map) {
				value = asMap(value);
			}
			if (key instanceof CharSequence) {
				result.put(key.toString(), value);
			}
			else {
				// 此时键必须是 Map 键
				result.put("[" + key + "]", value);
			}
		});
		return result;
	}

	private boolean process(Map<String, Object> map, MatchCallback callback) {
		Properties properties = CollectionFactory.createStringAdaptingProperties();
		properties.putAll(getFlattenedMap(map));

		if (this.documentMatchers.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Merging document (no matchers set): " + map);
			}
			callback.process(properties, map);
			return true;
		}

		MatchStatus result = MatchStatus.ABSTAIN;
		for (DocumentMatcher matcher : this.documentMatchers) {
			MatchStatus match = matcher.matches(properties);
			result = MatchStatus.getMostSpecific(match, result);
			if (match == MatchStatus.FOUND) {
				if (logger.isDebugEnabled()) {
					logger.debug("Matched document with document matcher: " + properties);
				}
				callback.process(properties, map);
				return true;
			}
		}

		if (result == MatchStatus.ABSTAIN && this.matchDefault) {
			if (logger.isDebugEnabled()) {
				logger.debug("Matched document with default matcher: " + map);
			}
			callback.process(properties, map);
			return true;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Unmatched document: " + map);
		}
		return false;
	}

	/**
	 * 返回给定 Map 的扁平化版本，递归遍历所有嵌套 Map 或 Collection 值。
	 * 结果 Map 中的条目保持与源相同的顺序。对 {@link MatchCallback} 的 Map 调用时，
	 * 结果将包含与 {@link MatchCallback} Properties 相同的值。
	 * @param source 源 Map
	 * @return 扁平化后的 Map
	 * @since 4.1.3
	 */
	protected final Map<String, Object> getFlattenedMap(Map<String, Object> source) {
		return getFlattenedMap(source, false, null);
	}

	/**
	 * 返回给定 Map 的扁平化版本，递归遍历所有嵌套 Map 或 Collection 值。
	 * 结果 Map 中的条目保持与源相同的顺序。对 {@link MatchCallback} 的 Map 调用时，
	 * 结果将包含与 {@link MatchCallback} Properties 相同的值。
	 * @param source 源 Map
	 * @param includeEmpty 是否在结果中包含空条目
	 * @param emptyValue 表示空条目的值，例如 {@code null} 或空 {@code String}
	 * @return 扁平化后的 Map
	 * @since 7.0.4
	 */
	protected final Map<String, Object> getFlattenedMap(Map<String, Object> source, boolean includeEmpty,
			@Nullable Object emptyValue) {

		Map<String, Object> result = new LinkedHashMap<>();
		buildFlattenedMap(result, source, null, includeEmpty, emptyValue);
		return result;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, @Nullable String path,
			boolean includeEmpty, @Nullable Object emptyValue) {

		if (includeEmpty && source.isEmpty()) {
			result.put(path, emptyValue);
			return;
		}
		source.forEach((key, value) -> {
			if (StringUtils.hasText(path)) {
				if (key.startsWith("[")) {
					key = path + key;
				}
				else {
					key = path + '.' + key;
				}
			}
			if (value instanceof String) {
				result.put(key, value);
			}
			else if (value instanceof Map map) {
				// 需要复合键
				buildFlattenedMap(result, map, key, includeEmpty, emptyValue);
			}
			else if (value instanceof Collection collection) {
				// 需要复合键
				if (collection.isEmpty()) {
					result.put(key, "");
				}
				else {
					int count = 0;
					for (Object object : collection) {
						buildFlattenedMap(result, Collections.singletonMap(
								"[" + (count++) + "]", object), key, includeEmpty, emptyValue);
					}
				}
			}
			else {
				result.put(key, (value != null ? value : (includeEmpty ? emptyValue : "")));
			}
		});
	}


	/**
	 * 用于处理 YAML 解析结果的回调接口。
	 */
	@FunctionalInterface
	public interface MatchCallback {

		/**
		 * 处理给定的解析结果表示。
		 * @param properties 要处理的属性（扁平化表示，集合或 Map 时使用索引键）
		 * @param map 结果 Map（保留 YAML 文档中的原始值结构）
		 */
		void process(Properties properties, Map<String, Object> map);
	}


	/**
	 * 用于测试属性是否匹配的策略接口。
	 */
	@FunctionalInterface
	public interface DocumentMatcher {

		/**
		 * 测试给定属性是否匹配。
		 * @param properties 要测试的属性
		 * @return 匹配状态
		 */
		MatchStatus matches(Properties properties);
	}


	/**
	 * {@link DocumentMatcher#matches(java.util.Properties)} 返回的状态。
	 */
	public enum MatchStatus {

		/**
		 * 找到匹配。
		 */
		FOUND,

		/**
		 * 未找到匹配。
		 */
		NOT_FOUND,

		/**
		 * 匹配器不应被考虑。
		 */
		ABSTAIN;

		/**
		 * 比较两个 {@link MatchStatus} 项，返回更具体的状态。
		 */
		public static MatchStatus getMostSpecific(MatchStatus a, MatchStatus b) {
			return (a.ordinal() < b.ordinal() ? a : b);
		}
	}


	/**
	 * 资源解析方法。
	 */
	public enum ResolutionMethod {

		/**
		 * 用列表中靠后的值替换靠前的值。
		 */
		OVERRIDE,

		/**
		 * 用列表中靠后的值替换靠前的值，忽略任何失败。
		 */
		OVERRIDE_AND_IGNORE,

		/**
		 * 取列表中第一个存在的资源，仅使用该资源。
		 */
		FIRST_FOUND
	}

	/** 过滤 YAML 标签的检查器，仅允许配置的支持类型 */
	private class SupportedTagInspector implements TagInspector {

		@Override
		public boolean isGlobalTagAllowed(Tag tag) {
			return supportedTypes.contains(tag.getClassName());
		}
	}

}
