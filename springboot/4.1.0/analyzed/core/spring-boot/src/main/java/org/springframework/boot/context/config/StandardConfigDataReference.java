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

import java.nio.charset.Charset;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.util.StringUtils;

/**
 * 由原始 {@link ConfigDataLocation} 展开、最终可解析为一个或多个
 * {@link StandardConfigDataResource 资源} 的引用。
 *
 * @author Phillip Webb
 * @author Moritz Halbritter
 */
class StandardConfigDataReference {

	private final ConfigDataLocation configDataLocation;

	private final String resourceLocation;

	private final @Nullable String directory;

	private final @Nullable String profile;

	private final PropertySourceLoader propertySourceLoader;

	private final @Nullable Charset encoding;

	/**
	 * 创建新的 {@link StandardConfigDataReference} 实例。
	 *
	 * @param configDataLocation 传给解析器的原始位置
	 * @param directory 资源目录；引用指向文件时为 {@code null}
	 * @param root 资源位置根路径
	 * @param profile 正在加载的 profile
	 * @param extension 资源文件扩展名
	 * @param propertySourceLoader 本引用应使用的属性源加载器
	 * @param encoding 资源编码
	 */
	StandardConfigDataReference(ConfigDataLocation configDataLocation, @Nullable String directory, String root,
			@Nullable String profile, @Nullable String extension, PropertySourceLoader propertySourceLoader,
			@Nullable Charset encoding) {
		this.configDataLocation = configDataLocation;
		String profileSuffix = (StringUtils.hasText(profile)) ? "-" + profile : "";
		this.resourceLocation = root + profileSuffix + ((extension != null) ? "." + extension : "");
		this.directory = directory;
		this.profile = profile;
		this.propertySourceLoader = propertySourceLoader;
		this.encoding = encoding;
	}

	ConfigDataLocation getConfigDataLocation() {
		return this.configDataLocation;
	}

	String getResourceLocation() {
		return this.resourceLocation;
	}

	boolean isMandatoryDirectory() {
		return !this.configDataLocation.isOptional() && this.directory != null;
	}

	@Nullable String getDirectory() {
		return this.directory;
	}

	@Nullable String getProfile() {
		return this.profile;
	}

	@Nullable Charset getEncoding() {
		return this.encoding;
	}

	boolean isSkippable() {
		return this.configDataLocation.isOptional() || this.directory != null || this.profile != null;
	}

	PropertySourceLoader getPropertySourceLoader() {
		return this.propertySourceLoader;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		StandardConfigDataReference other = (StandardConfigDataReference) obj;
		return this.resourceLocation.equals(other.resourceLocation);
	}

	@Override
	public int hashCode() {
		return this.resourceLocation.hashCode();
	}

	@Override
	public String toString() {
		return this.resourceLocation;
	}

}
