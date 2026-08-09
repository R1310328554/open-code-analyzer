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
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.context.MessageSource} implementation that
 * accesses resource bundles using specified basenames. This class relies
 * on the underlying JDK's {@link java.util.ResourceBundle} implementation,
 * in combination with the JDK's standard message parsing provided by
 * {@link java.text.MessageFormat}.
 *
 * <p>This MessageSource caches both the accessed ResourceBundle instances and
 * the generated MessageFormats for each message. It also implements rendering of
 * no-arg messages without MessageFormat, as supported by the AbstractMessageSource
 * base class. The caching provided by this MessageSource is significantly faster
 * than the built-in caching of the {@code java.util.ResourceBundle} class.
 *
 * <p>The basenames follow {@link java.util.ResourceBundle} conventions: essentially,
 * a fully-qualified classpath location. If it doesn't contain a package qualifier
 * (such as {@code org.mypackage}), it will be resolved from the classpath root.
 * Note that the JDK's standard ResourceBundle treats dots as package separators:
 * This means that "test.theme" is effectively equivalent to "test/theme".
 *
 * <p>On the classpath, bundle resources will be read with the locally configured
 * {@link #setDefaultCharset Charset}: by default, ISO-8859-1; consider switching
 * this to UTF-8, or to {@code null} for the platform default encoding. On the JDK 9+
 * module path where locally provided {@code ResourceBundle.Control} handles are not
 * supported, this MessageSource always falls back to {@link ResourceBundle#getBundle}
 * retrieval with the platform default encoding: UTF-8 with an ISO-8859-1 fallback on
 * JDK 9+ (configurable through the "java.util.PropertyResourceBundle.encoding" system
 * property). Note that {@link #loadBundle(Reader)}/{@link #loadBundle(InputStream)}
 * won't be called in this case either, effectively ignoring overrides in subclasses.
 * Consider implementing a JDK 9 {@code java.util.spi.ResourceBundleProvider} instead.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Qimiao Chen
 * @author Sam Brannen
 * @see #setBasenames
 * @see ReloadableResourceBundleMessageSource
 * @see java.util.ResourceBundle
 * @see java.text.MessageFormat
 */
public class ResourceBundleMessageSource extends AbstractResourceBasedMessageSource implements BeanClassLoaderAware {

	private @Nullable ClassLoader bundleClassLoader;

	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/**
	 * Cache to hold loaded ResourceBundles.
	 * <p>This Map is keyed with the bundle basename, which holds a Map that is
	 * keyed with the Locale and in turn holds the ResourceBundle instances.
	 * This allows for very efficient hash lookups, significantly faster
	 * than the ResourceBundle class's own cache.
	 */
	private final Map<String, Map<Locale, ResourceBundle>> cachedResourceBundles =
			new ConcurrentHashMap<>();

	/**
	 * Cache to hold already generated MessageFormats.
	 * <p>This Map is keyed with the ResourceBundle, which holds a Map that is
	 * keyed with the message code, which in turn holds a Map that is keyed
	 * with the Locale and holds the MessageFormat values. This allows for
	 * very efficient hash lookups without concatenated keys.
	 * @see #getMessageFormat
	 */
	private final Map<ResourceBundle, Map<String, Map<Locale, MessageFormat>>> cachedBundleMessageFormats =
			new ConcurrentHashMap<>();

	private volatile @Nullable MessageSourceControl control = new MessageSourceControl();


	public ResourceBundleMessageSource() {
		setDefaultCharset(StandardCharsets.ISO_8859_1);
	}


	/**
	 * Set the ClassLoader to load resource bundles with.
	 * <p>Default is the containing BeanFactory's
	 * {@link org.springframework.beans.factory.BeanClassLoaderAware bean ClassLoader},
	 * or the default ClassLoader determined by
	 * {@link org.springframework.util.ClassUtils#getDefaultClassLoader()}
	 * if not running within a BeanFactory.
	 */
	public void setBundleClassLoader(ClassLoader classLoader) {
		this.bundleClassLoader = classLoader;
	}

	/**
	 * Return the ClassLoader to load resource bundles with.
	 * <p>Default is the containing BeanFactory's bean ClassLoader.
	 * @see #setBundleClassLoader
	 */
	protected @Nullable ClassLoader getBundleClassLoader() {
		return (this.bundleClassLoader != null ? this.bundleClassLoader : this.beanClassLoader);
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}


	/**
	 * Resolves the given message code as key in the registered resource bundles,
	 * returning the value found in the bundle as-is (without MessageFormat parsing).
	 */
	@Override
	protected @Nullable String resolveCodeWithoutArguments(String code, Locale locale) {
		Set<String> basenames = getBasenameSet();
		for (String basename : basenames) {
			ResourceBundle bundle = getResourceBundle(basename, locale);
			if (bundle != null) {
				String result = getStringOrNull(bundle, code);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * Resolves the given message code as key in the registered resource bundles,
	 * using a cached MessageFormat instance per message code.
	 */
	@Override
	protected @Nullable MessageFormat resolveCode(String code, Locale locale) {
		Set<String> basenames = getBasenameSet();
		for (String basename : basenames) {
			ResourceBundle bundle = getResourceBundle(basename, locale);
			if (bundle != null) {
				MessageFormat messageFormat = getMessageFormat(bundle, code, locale);
				if (messageFormat != null) {
					return messageFormat;
				}
			}
		}
		return null;
	}


	/**
	 * Return a ResourceBundle for the given basename and Locale,
	 * fetching already generated ResourceBundle from the cache.
	 * @param basename the basename of the ResourceBundle
	 * @param locale the Locale to find the ResourceBundle for
	 * @return the resulting ResourceBundle, or {@code null} if none
	 * found for the given basename and Locale
	 */
	protected @Nullable ResourceBundle getResourceBundle(String basename, Locale locale) {
		if (getCacheMillis() >= 0) {
			// Fresh ResourceBundle.getBundle call in order to let ResourceBundle
			// do its native caching, at the expense of more extensive lookup steps.
			return doGetBundle(basename, locale);
		}
		else {
			// Cache forever: prefer locale cache over repeated getBundle calls.
			Map<Locale, ResourceBundle> localeMap = this.cachedResourceBundles.get(basename);
			if (localeMap != null) {
				ResourceBundle bundle = localeMap.get(locale);
				if (bundle != null) {
					return bundle;
				}
			}
			try {
				ResourceBundle bundle = doGetBundle(basename, locale);
				if (localeMap == null) {
					localeMap = this.cachedResourceBundles.computeIfAbsent(basename, bn -> new ConcurrentHashMap<>());
				}
				localeMap.put(locale, bundle);
				return bundle;
			}
			catch (MissingResourceException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("ResourceBundle [" + basename + "] not found for MessageSource: " + ex.getMessage());
				}
				// Assume bundle not found
				// -> do NOT throw the exception to allow for checking parent message source.
				return null;
			}
		}
	}

	/**
	 * Obtain the resource bundle for the given basename and Locale.
	 * @param basename the basename to look for
	 * @param locale the Locale to look for
	 * @return the corresponding ResourceBundle
	 * @throws MissingResourceException if no matching bundle could be found
	 * @see java.util.ResourceBundle#getBundle(String, Locale, ClassLoader)
	 * @see #getBundleClassLoader()
	 */
	protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
		ClassLoader classLoader = getBundleClassLoader();
		Assert.state(classLoader != null, "No bundle ClassLoader set");

		MessageSourceControl control = this.control;
		if (control != null) {
			try {
				return ResourceBundle.getBundle(basename, locale, classLoader, control);
			}
			catch (UnsupportedOperationException ex) {
				// Probably in a Java Module System environment on JDK 9+
				this.control = null;
				Charset charset = getDefaultCharset();
				if (charset != null && logger.isInfoEnabled()) {
					logger.info("ResourceBundleMessageSource is configured to read resources with encoding '" +
							charset + "' but ResourceBundle.Control is not supported in current system environment: " +
							ex.getMessage() + " - falling back to plain ResourceBundle.getBundle retrieval with the " +
							"platform default encoding. Consider setting the 'defaultCharset' property to 'null' " +
							"for participating in the platform default and therefore avoiding this log message.");
				}
			}
		}

		// Fallback: plain getBundle lookup without Control handle
		return ResourceBundle.getBundle(basename, locale, classLoader);
	}

	/**
	 * 从给定 Reader 加载基于属性的资源包。
	 * <p>在设置了 {@linkplain #setDefaultCharset "defaultCharset"} 时调用，
	 * 包括 ResourceBundleMessageSource 默认的 ISO-8859-1 编码。
	 * 注意此方法仅能在有 {@code ResourceBundle.Control} 时调用：
	 * 在 JDK 9+ 模块路径上不支持此类控制句柄时，自定义子类中的覆盖实际上被忽略。
	 * <p>默认实现返回 {@link PropertyResourceBundle}。
	 * @param reader 目标资源的 Reader
	 * @return 完全加载的 bundle
	 * @throws IOException I/O 失败时
	 * @since 4.2
	 * @see #loadBundle(InputStream)
	 * @see PropertyResourceBundle#PropertyResourceBundle(Reader)
	 */
	protected ResourceBundle loadBundle(Reader reader) throws IOException {
		return new PropertyResourceBundle(reader);
	}

	/**
	 * 从给定输入流加载基于属性的资源包，在 JDK 9+ 上使用默认 properties 编码。
	 * <p>仅在 {@linkplain #setDefaultCharset "defaultCharset"} 设为 {@code null} 时调用，
	 * 显式强制平台默认编码（JDK 9+ 上为 UTF-8 并回退 ISO-8859-1，
	 * 可通过 "java.util.PropertyResourceBundle.encoding" 系统属性配置）。
	 * 注意此方法仅能在有 {@code ResourceBundle.Control} 时调用：
	 * 在 JDK 9+ 模块路径上不支持此类控制句柄时，自定义子类中的覆盖实际上被忽略。
	 * <p>默认实现返回 {@link PropertyResourceBundle}。
	 * @param inputStream 目标资源的输入流
	 * @return 完全加载的 bundle
	 * @throws IOException I/O 失败时
	 * @since 5.1
	 * @see #loadBundle(Reader)
	 * @see PropertyResourceBundle#PropertyResourceBundle(InputStream)
	 */
	protected ResourceBundle loadBundle(InputStream inputStream) throws IOException {
		return new PropertyResourceBundle(inputStream);
	}

	/**
	 * 返回给定 bundle 与代码的 MessageFormat，从缓存获取已生成的实例。
	 * @param bundle 要操作的 ResourceBundle
	 * @param code 要检索的消息代码
	 * @param locale 构建 MessageFormat 使用的区域
	 * @return 得到的 MessageFormat，给定代码未定义消息则为 {@code null}
	 * @throws MissingResourceException 若 ResourceBundle 抛出
	 */
	protected @Nullable MessageFormat getMessageFormat(ResourceBundle bundle, String code, Locale locale)
			throws MissingResourceException {

		Map<String, Map<Locale, MessageFormat>> codeMap = this.cachedBundleMessageFormats.get(bundle);
		Map<Locale, MessageFormat> localeMap = null;
		if (codeMap != null) {
			localeMap = codeMap.get(code);
			if (localeMap != null) {
				MessageFormat result = localeMap.get(locale);
				if (result != null) {
					return result;
				}
			}
		}

		String msg = getStringOrNull(bundle, code);
		if (msg != null) {
			if (codeMap == null) {
				codeMap = this.cachedBundleMessageFormats.computeIfAbsent(bundle, b -> new ConcurrentHashMap<>());
			}
			if (localeMap == null) {
				localeMap = codeMap.computeIfAbsent(code, c -> new ConcurrentHashMap<>());
			}
			MessageFormat result = createMessageFormat(msg, locale);
			localeMap.put(locale, result);
			return result;
		}

		return null;
	}

	/**
	 * 高效检索指定键的字符串值，未找到则返回 {@code null}。
	 * <p>自 4.2 起，默认实现先调用 {@code containsKey} 再调用 {@code getString}
	 * （否则需捕获键未找到的 {@code MissingResourceException}）。
	 * <p>子类可覆盖。
	 * @param bundle 执行查找的 ResourceBundle
	 * @param key 要查找的键
	 * @return 关联值，无则为 {@code null}
	 * @since 4.2
	 * @see ResourceBundle#getString(String)
	 * @see ResourceBundle#containsKey(String)
	 */
	protected @Nullable String getStringOrNull(ResourceBundle bundle, String key) {
		if (bundle.containsKey(key)) {
			try {
				return bundle.getString(key);
			}
			catch (MissingResourceException ex) {
				// 假定因其他原因未找到键，不抛异常以便检查父消息源
			}
		}
		return null;
	}

	/**
	 * 显示本 MessageSource 的配置信息。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": basenames=" + getBasenameSet();
	}


	/**
	 * {@code ResourceBundle.Control} 的自定义实现，增加自定义文件编码支持、
	 * 禁用回退到系统区域，并按需激活 ResourceBundle 原生缓存。
	 */
	private class MessageSourceControl extends ResourceBundle.Control {

		@Override
		public @Nullable ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException {

			// 默认编码的特殊处理
			if (format.equals("java.properties")) {
				String bundleName = toBundleName(baseName, locale);
				final String resourceName = toResourceName(bundleName, "properties");
				final ClassLoader classLoader = loader;
				final boolean reloadFlag = reload;
				InputStream inputStream = null;
				if (reloadFlag) {
					URL url = classLoader.getResource(resourceName);
					if (url != null) {
						URLConnection connection = url.openConnection();
						if (connection != null) {
							connection.setUseCaches(false);
							inputStream = connection.getInputStream();
						}
					}
				}
				else {
					inputStream = classLoader.getResourceAsStream(resourceName);
				}
				if (inputStream != null) {
					Charset charset = getDefaultCharset();
					if (charset != null) {
						try (InputStreamReader bundleReader = new InputStreamReader(inputStream, charset)) {
							return loadBundle(bundleReader);
						}
					}
					else {
						try (InputStream bundleStream = inputStream) {
							return loadBundle(bundleStream);
						}
					}
				}
				else {
					return null;
				}
			}
			else {
				// 将 "java.class" 格式委托给标准 Control
				return super.newBundle(baseName, locale, format, loader, reload);
			}
		}

		@Override
		public @Nullable Locale getFallbackLocale(String baseName, Locale locale) {
			Locale defaultLocale = getDefaultLocale();
			return (defaultLocale != null && !defaultLocale.equals(locale) ? defaultLocale : null);
		}

		@Override
		public long getTimeToLive(String baseName, Locale locale) {
			long cacheMillis = getCacheMillis();
			return (cacheMillis >= 0 ? cacheMillis : super.getTimeToLive(baseName, locale));
		}

		@Override
		public boolean needsReload(
				String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {

			if (super.needsReload(baseName, locale, format, loader, bundle, loadTime)) {
				cachedBundleMessageFormats.remove(bundle);
				return true;
			}
			else {
				return false;
			}
		}
	}

}
