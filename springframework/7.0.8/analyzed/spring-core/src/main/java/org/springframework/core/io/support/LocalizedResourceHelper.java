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

package org.springframework.core.io.support;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

/* ===== [OCA 中文解析] =====
class LocalizedResourceHelper — 意图说明

class `LocalizedResourceHelper`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-core/src/main/java/org/springframework/core/io/support/LocalizedResourceHelper.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Helper class for loading a localized resource,
 * specified through name, extension and current locale.
 *
 * @author Juergen Hoeller
 * @since 1.2.5
 */
public class LocalizedResourceHelper {

	// [OCA] 字段 `DEFAULT_SEPARATOR`：类成员状态。
	/** The default separator to use in-between file name parts: an underscore. */
	public static final String DEFAULT_SEPARATOR = "_";


	// [OCA] 字段 `resourceLoader`：类成员状态。
	private final ResourceLoader resourceLoader;

	// [OCA] 字段 `separator`：类成员状态。
	private String separator = DEFAULT_SEPARATOR;


	/**
	 * Create a new LocalizedResourceHelper with a DefaultResourceLoader.
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public LocalizedResourceHelper() {
		this.resourceLoader = new DefaultResourceLoader();
	}

	/**
	 * Create a new LocalizedResourceHelper with the given ResourceLoader.
	 * @param resourceLoader the ResourceLoader to use
	 */
	public LocalizedResourceHelper(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Set the separator to use in-between file name parts.
	 * Default is an underscore ("_").
	 */
	public void setSeparator(@Nullable String separator) {
		this.separator = (separator != null ? separator : DEFAULT_SEPARATOR);
	}


	/* ===== [OCA 中文解析] =====
方法 findLocalizedResource — 意图与阅读要点

方法 `findLocalizedResource` 复杂度较高（CCN≈11, NLOC≈28）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Find the most specific localized resource for the given name,
	 * extension and locale:
	 * <p>The file will be searched with locations in the following order,
	 * similar to {@code java.util.ResourceBundle}'s search order:
	 * <ul>
	 * <li>[name]_[language]_[country]_[variant][extension]
	 * <li>[name]_[language]_[country][extension]
	 * <li>[name]_[language][extension]
	 * <li>[name][extension]
	 * </ul>
	 * <p>If none of the specific files can be found, a resource
	 * descriptor for the default location will be returned.
	 * @param name the name of the file, without localization part nor extension
	 * @param extension the file extension (for example, ".xls")
	 * @param locale the current locale (may be {@code null})
	 * @return the most specific localized resource found
	 * @see java.util.ResourceBundle
	 */
	public Resource findLocalizedResource(String name, String extension, @Nullable Locale locale) {
		Assert.notNull(name, "Name must not be null");
		Assert.notNull(extension, "Extension must not be null");

		Resource resource = null;

		if (locale != null) {
			String lang = locale.getLanguage();
			String country = locale.getCountry();
			String variant = locale.getVariant();

			// Check for file with language, country and variant localization.
			if (variant.length() > 0) {
				String location =
						name + this.separator + lang + this.separator + country + this.separator + variant + extension;
				resource = this.resourceLoader.getResource(location);
			}

			// Check for file with language and country localization.
			if ((resource == null || !resource.exists()) && country.length() > 0) {
				String location = name + this.separator + lang + this.separator + country + extension;
				resource = this.resourceLoader.getResource(location);
			}

			// Check for document with language localization.
			if ((resource == null || !resource.exists()) && lang.length() > 0) {
				String location = name + this.separator + lang + extension;
				resource = this.resourceLoader.getResource(location);
			}
		}

		// Check for document without localization.
		if (resource == null || !resource.exists()) {
			String location = name + extension;
			resource = this.resourceLoader.getResource(location);
		}

		return resource;
	}

}
