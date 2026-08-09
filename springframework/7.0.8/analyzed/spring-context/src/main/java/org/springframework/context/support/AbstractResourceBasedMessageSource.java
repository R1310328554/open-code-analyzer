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

import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 基于资源包约定的 {@code MessageSource} 实现的抽象基类，
 * 如 {@link ResourceBundleMessageSource} 和 {@link ReloadableResourceBundleMessageSource}。
 * 提供通用配置方法及对应的语义定义。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.3
 * @see ResourceBundleMessageSource
 * @see ReloadableResourceBundleMessageSource
 */
public abstract class AbstractResourceBasedMessageSource extends AbstractMessageSource {

	/** 已注册的资源包基础名集合，保持注册顺序。 */
	private final Set<String> basenameSet = new LinkedHashSet<>(4);

	/** 解析 properties 文件的默认字符集。 */
	private @Nullable Charset defaultCharset;

	/** 找不到特定 Locale 文件时是否回退到系统 Locale。 */
	private boolean fallbackToSystemLocale = true;

	/** 回退用的默认 Locale（替代系统 Locale）。 */
	private @Nullable Locale defaultLocale;

	/** 已加载 properties 文件的缓存时长（毫秒）。 */
	private long cacheMillis = -1;


	/**
	 * 设置单个基础名，遵循 ResourceBundle 惯例，不指定文件扩展名或语言代码。
	 * 资源位置格式由具体 {@code MessageSource} 实现决定。
	 * <p>支持常规与 XML properties 文件：例如 {@code messages} 可找到
	 * {@code messages.properties}、{@code messages_en.properties} 等，
	 * 以及 {@code messages.xml}、{@code messages_en.xml} 等。
	 * @param basename 单个基础名
	 * @see #setBasenames
	 * @see org.springframework.core.io.ResourceEditor
	 * @see java.util.ResourceBundle
	 */
	public void setBasename(String basename) {
		setBasenames(basename);
	}

	/**
	 * 设置基础名数组，每个遵循 ResourceBundle 惯例，不指定文件扩展名或语言代码。
	 * 资源位置格式由具体 {@code MessageSource} 实现决定。
	 * <p>支持常规与 XML properties 文件：例如 {@code messages} 可找到
	 * {@code messages.properties}、{@code messages_en.properties} 等，
	 * 以及 {@code messages.xml}、{@code messages_en.xml} 等。
	 * <p>解析消息代码时将按顺序检查关联的资源包。注意：<i>较早</i>资源包中的
	 * 消息定义会覆盖较晚包中的定义，因为采用顺序查找。
	 * <p>注意：与 {@link #addBasenames} 不同，本方法用给定名称替换现有条目，
	 * 因此也可用于重置配置。
	 * @param basenames 基础名数组
	 * @see #setBasename
	 * @see java.util.ResourceBundle
	 */
	public void setBasenames(String... basenames) {
		this.basenameSet.clear();
		addBasenames(basenames);
	}

	/**
	 * 将指定基础名添加到现有基础名配置。
	 * <p>注意：若给定基础名已存在，其条目位置保持原集合中的位置。
	 * 新条目添加到列表末尾，在已有基础名之后搜索。
	 * @since 4.3
	 * @see #setBasenames
	 * @see java.util.ResourceBundle
	 */
	public void addBasenames(String... basenames) {
		if (!ObjectUtils.isEmpty(basenames)) {
			for (String basename : basenames) {
				Assert.hasText(basename, "Basename must not be empty");
				this.basenameSet.add(basename.trim());
			}
		}
	}

	/**
	 * 返回本 {@code MessageSource} 的基础名集合，按注册顺序排列。
	 * <p>调用代码可内省此集合并添加或删除条目。
	 * @since 4.3
	 * @see #setBasenames
	 * @see #addBasenames
	 */
	public Set<String> getBasenameSet() {
		return this.basenameSet;
	}

	/**
	 * 设置解析 properties 文件的默认字符集。
	 * <p>在未为文件指定专用字符集时使用。
	 * <p>有效默认为 {@code java.util.Properties} 默认编码 ISO-8859-1。
	 * {@code null} 表示平台默认编码。
	 * <p>仅适用于经典 properties 文件，不适用于 XML 文件。
	 * @param defaultEncoding 默认字符集
	 * @see #setDefaultCharset(Charset)
	 */
	public void setDefaultEncoding(@Nullable String defaultEncoding) {
		this.defaultCharset = (defaultEncoding != null ? Charset.forName(defaultEncoding) : null);
	}

	/**
	 * 返回解析 properties 文件的默认字符集（若有）。
	 * @since 4.3
	 * @see #getDefaultCharset()
	 */
	protected @Nullable String getDefaultEncoding() {
		return (this.defaultCharset != null ? this.defaultCharset.name() : null);
	}

	/**
	 * 设置解析 properties 文件的默认 {@link Charset}。
	 * <p>在未为文件指定专用字符集时使用。
	 * <p>有效默认为 {@code java.util.Properties} 默认编码 ISO-8859-1。
	 * {@code null} 表示平台默认编码。
	 * <p>仅适用于经典 properties 文件，不适用于 XML 文件。
	 * @param defaultCharset 默认字符集
	 * @since 7.0.6
	 * @see #setDefaultEncoding(String)
	 */
	public void setDefaultCharset(@Nullable Charset defaultCharset) {
		this.defaultCharset = defaultCharset;
	}

	/**
	 * 返回解析 properties 文件的默认字符集（若有）。
	 * @since 7.0.6
	 * @see #setDefaultCharset(Charset)
	 */
	protected @Nullable Charset getDefaultCharset() {
		return this.defaultCharset;
	}


	/**
	 * 设置找不到特定 Locale 文件时是否回退到系统 Locale。默认为 {@code true}；
	 * 关闭后唯一回退为默认文件（例如基础名 {@code messages} 对应 {@code messages.properties}）。
	 * <p>回退到系统 Locale 是 {@code java.util.ResourceBundle} 的默认行为。
	 * 但在应用服务器环境中，系统 Locale 往往与应用无关：
	 * 此类场景请将本标志设为 {@code false}。
	 * @see #setDefaultLocale
	 */
	public void setFallbackToSystemLocale(boolean fallbackToSystemLocale) {
		this.fallbackToSystemLocale = fallbackToSystemLocale;
	}

	/**
	 * 返回找不到特定 Locale 文件时是否回退到系统 Locale。
	 * @since 4.3
	 * @deprecated 请改用 {@link #getDefaultLocale()}
	 */
	@Deprecated(since = "5.2.2")
	protected boolean isFallbackToSystemLocale() {
		return this.fallbackToSystemLocale;
	}

	/**
	 * 指定回退用的默认 Locale，作为回退到系统 Locale 的替代方案。
	 * <p>默认回退到系统 Locale。可在此覆盖为本地指定的默认 Locale，
	 * 或通过禁用 {@link #setFallbackToSystemLocale "fallbackToSystemLocale"} 强制不回退。
	 * @since 5.2.2
	 * @see #setFallbackToSystemLocale
	 * @see #getDefaultLocale()
	 */
	public void setDefaultLocale(@Nullable Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	/**
	 * 确定回退用的默认 Locale：本地指定的默认 Locale、系统 Locale，
	 * 或 {@code null} 表示完全不回退。
	 * @since 5.2.2
	 * @see #setDefaultLocale
	 * @see #setFallbackToSystemLocale
	 * @see Locale#getDefault()
	 */
	protected @Nullable Locale getDefaultLocale() {
		if (this.defaultLocale != null) {
			return this.defaultLocale;
		}
		if (this.fallbackToSystemLocale) {
			return Locale.getDefault();
		}
		return null;
	}

	/**
	 * 设置已加载 properties 文件的缓存秒数。
	 * <ul>
	 * <li>默认为 {@code -1}，表示永久缓存（与 {@code java.util.ResourceBundle} 默认行为一致）。
	 * 注意此常量遵循 Spring 约定，而非 {@link java.util.ResourceBundle.Control#getTimeToLive}。
	 * <li>正数表示缓存给定秒数，实质为刷新检查间隔。
	 * 注意刷新会先检查文件最后修改时间再实际重载；若文件未变，可将间隔设得较低。
	 * <li>{@code 0} 表示每次访问消息都检查文件最后修改时间。<b>生产环境请勿使用！</b>
	 * </ul>
	 * <p><b>注意：取决于 ClassLoader，过期可能不可靠，
	 * 因为 ClassLoader 可能持有资源包文件的缓存版本。</b>
	 * 此类场景请优先使用 {@link ReloadableResourceBundleMessageSource}
	 * 而非 {@link ResourceBundleMessageSource}，并配合非类路径位置。
	 */
	public void setCacheSeconds(int cacheSeconds) {
		this.cacheMillis = cacheSeconds * 1000L;
	}

	/**
	 * 设置已加载 properties 文件的缓存毫秒数。
	 * 注意通常以秒为单位设置：{@link #setCacheSeconds}。
	 * <ul>
	 * <li>默认为 {@code -1}，表示永久缓存（与 {@code java.util.ResourceBundle} 默认行为一致）。
	 * 注意此常量遵循 Spring 约定，而非 {@link java.util.ResourceBundle.Control#getTimeToLive}。
	 * <li>正数表示缓存给定毫秒数，实质为刷新检查间隔。
	 * 注意刷新会先检查文件最后修改时间再实际重载；若文件未变，可将间隔设得较低。
	 * <li>{@code 0} 表示每次访问消息都检查文件最后修改时间。<b>生产环境请勿使用！</b>
	 * </ul>
	 * @since 4.3
	 * @see #setCacheSeconds
	 */
	public void setCacheMillis(long cacheMillis) {
		this.cacheMillis = cacheMillis;
	}

	/**
	 * 返回已加载 properties 文件的缓存毫秒数。
	 * @since 4.3
	 */
	protected long getCacheMillis() {
		return this.cacheMillis;
	}

}
