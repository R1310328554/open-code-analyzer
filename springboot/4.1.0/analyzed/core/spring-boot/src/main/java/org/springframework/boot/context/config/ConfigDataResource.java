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
 * 可从中加载 {@link ConfigData} 的单个资源。实现类必须提供有效的
 * {@link #equals(Object) equals}、{@link #hashCode() hashCode} 与 {@link #toString() toString} 方法。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public abstract class ConfigDataResource {

	private final boolean optional;

	/**
	 * 创建新的非可选 {@link ConfigDataResource} 实例。
	 */
	public ConfigDataResource() {
		this(false);
	}

	/**
	 * 创建新的 {@link ConfigDataResource} 实例。
	 *
	 * @param optional 资源是否可选
	 * @since 2.4.6
	 */
	protected ConfigDataResource(boolean optional) {
		this.optional = optional;
	}

	boolean isOptional() {
		return this.optional;
	}

}
