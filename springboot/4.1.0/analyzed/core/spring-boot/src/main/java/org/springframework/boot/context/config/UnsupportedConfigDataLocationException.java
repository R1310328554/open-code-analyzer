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

/**
 * 当 {@link ConfigDataLocation} 不受支持时抛出的异常。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public class UnsupportedConfigDataLocationException extends ConfigDataException {

	private final ConfigDataLocation location;

	/**
	 * 创建新的 {@link UnsupportedConfigDataLocationException} 实例。
	 *
	 * @param location 不受支持的位置
	 */
	UnsupportedConfigDataLocationException(ConfigDataLocation location) {
		super("Unsupported config data location '" + location + "'", null);
		this.location = location;
	}

	/**
	 * 返回不受支持的位置引用。
	 *
	 * @return 不受支持的位置引用
	 */
	public ConfigDataLocation getLocation() {
		return this.location;
	}

}
