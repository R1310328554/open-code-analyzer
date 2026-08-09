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

package org.springframework.context.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.StringUtils;

/* ===== [OCA 中文解析] =====
class ReloadableResourceBundleMessageSource — 意图说明

class `ReloadableResourceBundleMessageSource`：可热刷新的国际化 MessageSource，按 basename 加载 properties/XML 并缓存。；源文件: `spring-context/src/main/java/org/springframework/context/support/ReloadableResourceBundleMessageSource.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Spring 专用的 {@link org.springframework.context.MessageSource} 实现，
 * 通过 basename 访问资源 bundle，并参与 Spring {@link org.springframework.context.ResourceLoaderAware} 资源加载。
 *
 * <p>与 {@link ResourceBundleMessageSource} 的 "basenames" 类似但不完全相同：
 * 遵循 ResourceBundle 不写扩展名/语言码的规则，但可指向任意 Spring 资源位置。
 *
 * <p>典型 Web 应用可将消息文件放在 {@code WEB-INF} 下，例如 basename "WEB-INF/messages"。
 * 先找到的 bundle 中定义会覆盖后找到的（顺序查找）。
 *
 * <p>可在 {@link org.springframework.context.ApplicationContext} 外使用；
 * 默认 {@link org.springframework.core.io.DefaultResourceLoader}，在上下文中会被覆盖。
 *
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @author Sam Brannen
 * @see #setCacheSeconds
 * @see #setBasenames
 * @see #setDefaultCharset
 * @see #setFileEncodings
 * @see #setPropertiesPersister
 * @see #setResourceLoader
 * @see org.springframework.core.io.DefaultResourceLoader
 * @see ResourceBundleMessageSource
 * @see java.util.ResourceBundle
 */
public class ReloadableResourceBundleMessageSource extends AbstractResourceBasedMessageSource
		implements ResourceLoaderAware {

	// [OCA] 字段 `PROPERTIES_EXTENSION`：类成员状态。
	private static final String PROPERTIES_EXTENSION = ".properties";

	// [OCA] 字段 `XML_EXTENSION`：类成员状态。
	private static final String XML_EXTENSION = ".xml";


	// [OCA] 字段 `fileExtensions`：类成员状态。
	private List<String> fileExtensions = List.of(PROPERTIES_EXTENSION, XML_EXTENSION);

	private @Nullable Properties fileEncodings;

	// [OCA] 字段 `concurrentRefresh`：类成员状态。
	private boolean concurrentRefresh = true;

	// [OCA] 字段 `propertiesPersister`：类成员状态。
	private PropertiesPersister propertiesPersister = DefaultPropertiesPersister.INSTANCE;

	// [OCA] 字段 `resourceLoader`：类成员状态。
	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	// Cache to hold filename lists per Locale
	// [OCA] 字段 `cachedFilenames`：类成员状态。
	private final ConcurrentMap<String, Map<Locale, List<String>>> cachedFilenames = new ConcurrentHashMap<>();

	// Cache to hold already loaded properties per filename
	// [OCA] 字段 `cachedProperties`：类成员状态。
	private final ConcurrentMap<String, PropertiesHolder> cachedProperties = new ConcurrentHashMap<>();

	// Cache to hold already loaded properties per filename
	// [OCA] 字段 `cachedMergedProperties`：类成员状态。
	private final ConcurrentMap<Locale, PropertiesHolder> cachedMergedProperties = new ConcurrentHashMap<>();


	/**
	 * Set the list of supported file extensions.
	 * <p>The default is a list containing {@code .properties} and {@code .xml}.
	 * @param fileExtensions the file extensions (starts with a dot)
	 * @since 6.1
	 */
	public void setFileExtensions(List<String> fileExtensions) {
		Assert.isTrue(!CollectionUtils.isEmpty(fileExtensions), "At least one file extension is required");
		for (String extension : fileExtensions) {
			if (!extension.startsWith(".")) {
				throw new IllegalArgumentException("File extension '" + extension + "' should start with '.'");
			}
		}
		this.fileExtensions = Collections.unmodifiableList(fileExtensions);
	}

	/**
	 * Set per-file charsets to use for parsing properties files.
	 * <p>Only applies to classic properties files, not to XML files.
	 * @param fileEncodings a Properties object with filenames as keys and charset
	 * names as values. Filenames have to match the basename syntax,
	 * with optional locale-specific components: for example, "WEB-INF/messages"
	 * or "WEB-INF/messages_en".
	 * @see #setBasenames
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setFileEncodings(Properties fileEncodings) {
		this.fileEncodings = fileEncodings;
	}

	/**
	 * Specify whether to allow for concurrent refresh behavior, i.e. one thread
	 * locked in a refresh attempt for a specific cached properties file whereas
	 * other threads keep returning the old properties for the time being, until
	 * the refresh attempt has completed.
	 * <p>Default is "true", minimizing contention between threads. If you prefer
	 * the old behavior, i.e. to fully block on refresh, switch this flag to "false".
	 * @since 4.1
	 * @see #setCacheSeconds
	 */
	public void setConcurrentRefresh(boolean concurrentRefresh) {
		this.concurrentRefresh = concurrentRefresh;
	}

	/**
	 * Set the PropertiesPersister to use for parsing properties files.
	 * <p>The default is {@code DefaultPropertiesPersister}.
	 * @see DefaultPropertiesPersister#INSTANCE
	 */
	public void setPropertiesPersister(@Nullable PropertiesPersister propertiesPersister) {
		this.propertiesPersister =
				(propertiesPersister != null ? propertiesPersister : DefaultPropertiesPersister.INSTANCE);
	}

	/**
	 * Set the ResourceLoader to use for loading bundle properties files.
	 * <p>The default is a DefaultResourceLoader. Will get overridden by the
	 * ApplicationContext if running in a context, as it implements the
	 * ResourceLoaderAware interface. Can be manually overridden when
	 * running outside an ApplicationContext.
	 * @see org.springframework.core.io.DefaultResourceLoader
	 * @see org.springframework.context.ResourceLoaderAware
	 */
	@Override
	public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
	}


	/**
	 * Resolves the given message code as key in the retrieved bundle files,
	 * returning the value found in the bundle as-is (without MessageFormat parsing).
	 */
	@Override
	protected @Nullable String resolveCodeWithoutArguments(String code, Locale locale) {
		if (getCacheMillis() < 0) {
			PropertiesHolder propHolder = getMergedProperties(locale);
			String result = propHolder.getProperty(code);
			if (result != null) {
				return result;
			}
		}
		else {
			for (String basename : getBasenameSet()) {
				List<String> filenames = calculateAllFilenames(basename, locale);
				for (String filename : filenames) {
					PropertiesHolder propHolder = getProperties(filename);
					String result = propHolder.getProperty(code);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Resolves the given message code as key in the retrieved bundle files,
	 * using a cached MessageFormat instance per message code.
	 */
	@Override
	protected @Nullable MessageFormat resolveCode(String code, Locale locale) {
		if (getCacheMillis() < 0) {
			PropertiesHolder propHolder = getMergedProperties(locale);
			MessageFormat result = propHolder.getMessageFormat(code, locale);
			if (result != null) {
				return result;
			}
		}
		else {
			for (String basename : getBasenameSet()) {
				List<String> filenames = calculateAllFilenames(basename, locale);
				for (String filename : filenames) {
					PropertiesHolder propHolder = getProperties(filename);
					MessageFormat result = propHolder.getMessageFormat(code, locale);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}


	/**
	 * Get a PropertiesHolder that contains the actually visible properties
	 * for a Locale, after merging all specified resource bundles.
	 * Either fetches the holder from the cache or freshly loads it.
	 * <p>Only used when caching resource bundle contents forever, i.e.
	 * with cacheSeconds &lt; 0. Therefore, merged properties are always
	 * cached forever.
	 * @see #collectPropertiesToMerge
	 * @see #mergeProperties
	 */
	protected PropertiesHolder getMergedProperties(Locale locale) {
		PropertiesHolder mergedHolder = this.cachedMergedProperties.get(locale);
		if (mergedHolder != null) {
			return mergedHolder;
		}
		mergedHolder = mergeProperties(collectPropertiesToMerge(locale));
		PropertiesHolder existing = this.cachedMergedProperties.putIfAbsent(locale, mergedHolder);
		if (existing != null) {
			mergedHolder = existing;
		}
		return mergedHolder;
	}

	/**
	 * Determine the properties to merge based on the specified basenames.
	 * @param locale the locale
	 * @return the list of properties holders
	 * @since 6.1.4
	 * @see #getBasenameSet()
	 * @see #calculateAllFilenames
	 * @see #mergeProperties
	 */
	protected List<PropertiesHolder> collectPropertiesToMerge(Locale locale) {
		String[] basenames = StringUtils.toStringArray(getBasenameSet());
		List<PropertiesHolder> holders = new ArrayList<>(basenames.length);
		for (int i = basenames.length - 1; i >= 0; i--) {
			List<String> filenames = calculateAllFilenames(basenames[i], locale);
			for (int j = filenames.size() - 1; j >= 0; j--) {
				String filename = filenames.get(j);
				PropertiesHolder propHolder = getProperties(filename);
				if (propHolder.getProperties() != null) {
					holders.add(propHolder);
				}
			}
		}
		return holders;
	}

	/**
	 * Merge the given properties holders into a single holder.
	 * @param holders the list of properties holders
	 * @return a single merged properties holder
	 * @since 6.1.4
	 * @see #newProperties()
	 * @see #getMergedProperties
	 * @see #collectPropertiesToMerge
	 */
	protected PropertiesHolder mergeProperties(List<PropertiesHolder> holders) {
		Properties mergedProps = newProperties();
		long latestTimestamp = -1;
		for (PropertiesHolder holder : holders) {
			mergedProps.putAll(holder.getProperties());
			if (holder.getFileTimestamp() > latestTimestamp) {
				latestTimestamp = holder.getFileTimestamp();
			}
		}
		return new PropertiesHolder(mergedProps, latestTimestamp);
	}

	/* ===== [OCA 中文解析] =====
方法 calculateAllFilenames — 意图与阅读要点

方法 `calculateAllFilenames` 复杂度较高（CCN≈9, NLOC≈30）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Calculate all filenames for the given bundle basename and Locale.
	 * Will calculate filenames for the given Locale, the system Locale
	 * (if applicable), and the default file.
	 * @param basename the basename of the bundle
	 * @param locale the locale
	 * @return the List of filenames to check
	 * @see #setFallbackToSystemLocale
	 * @see #calculateFilenamesForLocale
	 */
	protected List<String> calculateAllFilenames(String basename, Locale locale) {
		Map<Locale, List<String>> localeMap = this.cachedFilenames.get(basename);
		if (localeMap != null) {
			List<String> filenames = localeMap.get(locale);
			if (filenames != null) {
				return filenames;
			}
		}

		// Filenames for given Locale
		List<String> filenames = new ArrayList<>(7);
		filenames.addAll(calculateFilenamesForLocale(basename, locale));

		// Filenames for default Locale, if any
		Locale defaultLocale = getDefaultLocale();
		if (defaultLocale != null && !defaultLocale.equals(locale)) {
			List<String> fallbackFilenames = calculateFilenamesForLocale(basename, defaultLocale);
			for (String fallbackFilename : fallbackFilenames) {
				if (!filenames.contains(fallbackFilename)) {
					// Entry for fallback locale that isn't already in filenames list.
					filenames.add(fallbackFilename);
				}
			}
		}

		// Filename for default bundle file
		filenames.add(basename);

		if (localeMap == null) {
			localeMap = new ConcurrentHashMap<>();
			Map<Locale, List<String>> existing = this.cachedFilenames.putIfAbsent(basename, localeMap);
			if (existing != null) {
				localeMap = existing;
			}
		}
		localeMap.put(locale, filenames);
		return filenames;
	}

	/**
	 * Calculate the filenames for the given bundle basename and Locale,
	 * appending language code, country code, and variant code.
	 * <p>For example, basename "messages", Locale "de_AT_oo" &rarr; "messages_de_AT_OO",
	 * "messages_de_AT", "messages_de".
	 * <p>Follows the rules defined by {@link java.util.Locale#toString()}.
	 * @param basename the basename of the bundle
	 * @param locale the locale
	 * @return the List of filenames to check
	 */
	protected List<String> calculateFilenamesForLocale(String basename, Locale locale) {
		List<String> result = new ArrayList<>(3);
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		StringBuilder temp = new StringBuilder(basename);

		temp.append('_');
		if (!language.isEmpty()) {
			temp.append(language);
			result.add(0, temp.toString());
		}

		temp.append('_');
		if (!country.isEmpty()) {
			temp.append(country);
			result.add(0, temp.toString());
		}

		if (!variant.isEmpty() && (!language.isEmpty() || !country.isEmpty())) {
			temp.append('_').append(variant);
			result.add(0, temp.toString());
		}

		return result;
	}


	/* ===== [OCA 中文解析] =====
方法 getProperties — 意图与阅读要点

方法 `getProperties` 复杂度较高（CCN≈10, NLOC≈35）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Get a PropertiesHolder for the given filename, either from the
	 * cache or freshly loaded.
	 * @param filename the bundle filename (basename + Locale)
	 * @return the current PropertiesHolder for the bundle
	 */
	protected PropertiesHolder getProperties(String filename) {
		PropertiesHolder propHolder = this.cachedProperties.get(filename);
		long originalTimestamp = -2;

		if (propHolder != null) {
			originalTimestamp = propHolder.getRefreshTimestamp();
			if (originalTimestamp == -1 || originalTimestamp > System.currentTimeMillis() - getCacheMillis()) {
				// Up to date
				return propHolder;
			}
		}
		else {
			propHolder = new PropertiesHolder();
			PropertiesHolder existingHolder = this.cachedProperties.putIfAbsent(filename, propHolder);
			if (existingHolder != null) {
				propHolder = existingHolder;
			}
		}

		// At this point, we need to refresh...
		if (this.concurrentRefresh && propHolder.getRefreshTimestamp() >= 0) {
			// A populated but stale holder -> could keep using it.
			if (!propHolder.refreshLock.tryLock()) {
				// Getting refreshed by another thread already ->
				// let's return the existing properties for the time being.
				return propHolder;
			}
		}
		else {
			propHolder.refreshLock.lock();
		}
		try {
			PropertiesHolder existingHolder = this.cachedProperties.get(filename);
			if (existingHolder != null && existingHolder.getRefreshTimestamp() > originalTimestamp) {
				return existingHolder;
			}
			return refreshProperties(filename, propHolder);
		}
		finally {
			propHolder.refreshLock.unlock();
		}
	}

	/* ===== [OCA 中文解析] =====
方法 refreshProperties — 意图与阅读要点

方法 `refreshProperties` 复杂度较高（CCN≈12, NLOC≈44）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Refresh the PropertiesHolder for the given bundle filename.
	 * <p>The holder can be {@code null} if not cached before, or a timed-out cache entry
	 * (potentially getting re-validated against the current last-modified timestamp).
	 * @param filename the bundle filename (basename + Locale)
	 * @param propHolder the current PropertiesHolder for the bundle
	 * @see #resolveResource(String)
	 */
	protected PropertiesHolder refreshProperties(String filename, @Nullable PropertiesHolder propHolder) {
		long refreshTimestamp = (getCacheMillis() < 0 ? -1 : System.currentTimeMillis());

		Resource resource = resolveResource(filename);
		if (resource != null) {
			long fileTimestamp = -1;
			if (getCacheMillis() >= 0) {
				// Last-modified timestamp of file will just be read if caching with timeout.
				try {
					fileTimestamp = resource.lastModified();
					if (propHolder != null && propHolder.getFileTimestamp() == fileTimestamp) {
						if (logger.isDebugEnabled()) {
							logger.debug("Re-caching properties for filename [" + filename + "] - file hasn't been modified");
						}
						propHolder.setRefreshTimestamp(refreshTimestamp);
						return propHolder;
					}
				}
				catch (IOException ex) {
					// Probably a class path resource: cache it forever.
					if (logger.isDebugEnabled()) {
						logger.debug(resource + " could not be resolved in the file system - assuming that it hasn't changed", ex);
					}
					fileTimestamp = -1;
				}
			}
			try {
				Properties props = loadProperties(resource, filename);
				propHolder = new PropertiesHolder(props, fileTimestamp);
			}
			catch (IOException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Could not parse properties file [" + resource.getFilename() + "]", ex);
				}
				// Empty holder representing "not valid".
				propHolder = new PropertiesHolder();
			}
		}

		else {
			// Resource does not exist.
			if (logger.isDebugEnabled()) {
				logger.debug("No properties file found for [" + filename + "]");
			}
			// Empty holder representing "not found".
			propHolder = new PropertiesHolder();
		}

		propHolder.setRefreshTimestamp(refreshTimestamp);
		this.cachedProperties.put(filename, propHolder);
		return propHolder;
	}

	/**
	 * Resolve the specified bundle {@code filename} into a concrete {@link Resource},
	 * potentially checking multiple sources or file extensions.
	 * <p>If no suitable concrete {@code Resource} can be resolved, this method
	 * returns a {@code Resource} for which {@link Resource#exists()} returns
	 * {@code false}, which gets subsequently ignored.
	 * <p>This can be leveraged to check the last modification timestamp or to load
	 * properties from alternative sources &mdash; for example, from an XML BLOB
	 * in a database, or from properties serialized using a custom format such as
	 * JSON.
	 * <p>The default implementation delegates to the configured
	 * {@link #setResourceLoader(ResourceLoader) ResourceLoader} to resolve
	 * resources, checking in order for existing {@code Resource} with extensions defined
	 * by {@link #setFileExtensions(List)} ({@code .properties} and {@code .xml}
	 * by default).
	 * <p>When overriding this method, {@link #loadProperties(Resource, String)}
	 * <strong>must</strong> be capable of loading properties from any type of
	 * {@code Resource} returned by this method. As a consequence, implementors
	 * are strongly encouraged to also override {@code loadProperties()}.
	 * <p>As an alternative to overriding this method, you can configure a
	 * {@link #setPropertiesPersister(PropertiesPersister) PropertiesPersister}
	 * that is capable of dealing with all resources returned by this method.
	 * Please note, however, that the default {@code loadProperties()} implementation
	 * uses {@link PropertiesPersister#loadFromXml(Properties, InputStream) loadFromXml}
	 * for XML resources and otherwise uses the two
	 * {@link PropertiesPersister#load(Properties, InputStream) load} methods
	 * for other types of resources.
	 * @param filename the bundle filename (basename + Locale)
	 * @return the {@code Resource} to use, or {@code null} if none found
	 * @since 6.1
	 */
	protected @Nullable Resource resolveResource(String filename) {
		for (String fileExtension : this.fileExtensions) {
			Resource resource = this.resourceLoader.getResource(filename + fileExtension);
			if (resource.exists()) {
				return resource;
			}
		}
		return null;
	}

	/* ===== [OCA 中文解析] =====
方法 loadProperties — 意图与阅读要点

方法 `loadProperties` 复杂度较高（CCN≈10, NLOC≈37）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Load the properties from the given resource.
	 * @param resource the resource to load from
	 * @param filename the original bundle filename (basename + Locale)
	 * @return the populated Properties instance
	 * @throws IOException if properties loading failed
	 */
	protected Properties loadProperties(Resource resource, String filename) throws IOException {
		Properties props = newProperties();
		try (InputStream inputStream = resource.getInputStream()) {
			String resourceFilename = resource.getFilename();
			if (resourceFilename != null && resourceFilename.endsWith(XML_EXTENSION)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Loading properties [" + resource.getFilename() + "]");
				}
				this.propertiesPersister.loadFromXml(props, inputStream);
			}
			else {
				Charset charset = null;
				if (this.fileEncodings != null) {
					String charsetName = this.fileEncodings.getProperty(filename);
					if (charsetName != null) {
						charset = Charset.forName(charsetName);
					}
				}
				if (charset == null) {
					charset = getDefaultCharset();
				}
				if (charset != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Loading properties [" + resource.getFilename() + "] with encoding '" + charset + "'");
					}
					this.propertiesPersister.load(props, new InputStreamReader(inputStream, charset));
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Loading properties [" + resource.getFilename() + "]");
					}
					this.propertiesPersister.load(props, inputStream);
				}
			}
			return props;
		}
	}

	/**
	 * Template method for creating a plain new {@link Properties} instance.
	 * The default implementation simply calls {@link Properties#Properties()}.
	 * <p>Allows for returning a custom {@link Properties} extension in subclasses.
	 * Overriding methods should just instantiate a custom {@link Properties} subclass,
	 * with no further initialization or population to be performed at that point.
	 * @return a plain Properties instance
	 * @since 4.2
	 */
	protected Properties newProperties() {
		return new Properties();
	}


	/**
	 * Clear the resource bundle cache.
	 * Subsequent resolve calls will lead to reloading of the properties files.
	 */
	public void clearCache() {
		logger.debug("Clearing entire resource bundle cache");
		this.cachedProperties.clear();
		this.cachedMergedProperties.clear();
	}

	/**
	 * Clear the resource bundle caches of this MessageSource and all its ancestors.
	 * @see #clearCache
	 */
	public void clearCacheIncludingAncestors() {
		clearCache();
		if (getParentMessageSource() instanceof ReloadableResourceBundleMessageSource reloadableMsgSrc) {
			reloadableMsgSrc.clearCacheIncludingAncestors();
		}
	}


	@Override
	public String toString() {
		return getClass().getName() + ": basenames=" + getBasenameSet();
	}


	/* ===== [OCA 中文解析] =====
class PropertiesHolder — 意图说明

class `PropertiesHolder`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-context/src/main/java/org/springframework/context/support/ReloadableResourceBundleMessageSource.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
	===== [OCA 中文解析结束] ===== */
	/**
 * 用于缓存的 PropertiesHolder：记录源文件最后修改时间以高效检测变更，
 * 以及上次刷新尝试的时间戳（每次重新验证缓存条目时更新）。
 */
	protected class PropertiesHolder {

		private final @Nullable Properties properties;

		private final long fileTimestamp;

		private volatile long refreshTimestamp = -2;

		private final Lock refreshLock = new ReentrantLock();

		/** 缓存已生成的 MessageFormat（按消息代码）。 */
		private final ConcurrentMap<String, Map<Locale, MessageFormat>> cachedMessageFormats =
				new ConcurrentHashMap<>();

		public PropertiesHolder() {
			this.properties = null;
			this.fileTimestamp = -1;
		}

		public PropertiesHolder(Properties properties, long fileTimestamp) {
			this.properties = properties;
			this.fileTimestamp = fileTimestamp;
		}

		public @Nullable Properties getProperties() {
			return this.properties;
		}

		public long getFileTimestamp() {
			return this.fileTimestamp;
		}

		public void setRefreshTimestamp(long refreshTimestamp) {
			this.refreshTimestamp = refreshTimestamp;
		}

		public long getRefreshTimestamp() {
			return this.refreshTimestamp;
		}

		public @Nullable String getProperty(String code) {
			if (this.properties == null) {
				return null;
			}
			return this.properties.getProperty(code);
		}

		public @Nullable MessageFormat getMessageFormat(String code, Locale locale) {
			if (this.properties == null) {
				return null;
			}
			Map<Locale, MessageFormat> localeMap = this.cachedMessageFormats.get(code);
			if (localeMap != null) {
				MessageFormat result = localeMap.get(locale);
				if (result != null) {
					return result;
				}
			}
			String msg = this.properties.getProperty(code);
			if (msg != null) {
				if (localeMap == null) {
					localeMap = new ConcurrentHashMap<>();
					Map<Locale, MessageFormat> existing = this.cachedMessageFormats.putIfAbsent(code, localeMap);
					if (existing != null) {
						localeMap = existing;
					}
				}
				MessageFormat result = createMessageFormat(msg, locale);
				localeMap.put(locale, result);
				return result;
			}
			return null;
		}
	}

}
