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

package org.springframework.boot.autoconfigure.ssl;

import java.nio.file.Path;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.ssl.pem.PemContent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 管理单个 SSL bundle 内容配置属性的辅助工具。
 * <p>
 * 属性值可能是 PEM 内联内容、资源位置或目录搜索模式。
 *
 * @param name 配置属性名（不含前缀）
 * @param value 配置属性值
 * @author Phillip Webb
 * @author Moritz Halbritter
 */
record BundleContentProperty(String name, @Nullable String value) {

	/**
	 * 判断属性值是否为 PEM 内联内容。
	 * @return 值为 PEM 内容时返回 {@code true}
	 */
	boolean isPemContent() {
		return PemContent.isPresentInText(this.value);
	}

	/**
	 * 判断属性值是否存在且非空。
	 * @return 存在有效值时返回 {@code true}
	 */
	boolean hasValue() {
		return StringUtils.hasText(this.value);
	}

	Path toWatchPath(ResourceLoader resourceLoader) {
		try {
			Assert.state(!isPemContent(), "Value contains PEM content");
			Assert.state(this.value != null, "Value must not be null");
			Resource resource = resourceLoader.getResource(this.value);
			if (!resource.isFile()) {
				throw new BundleContentNotWatchableException(this);
			}
			return Path.of(resource.getFile().getAbsolutePath());
		}
		catch (Exception ex) {
			if (ex instanceof BundleContentNotWatchableException bundleContentNotWatchableException) {
				throw bundleContentNotWatchableException;
			}
			throw new IllegalStateException("Unable to convert value of property '%s' to a path".formatted(this.name),
					ex);
		}
	}

}
