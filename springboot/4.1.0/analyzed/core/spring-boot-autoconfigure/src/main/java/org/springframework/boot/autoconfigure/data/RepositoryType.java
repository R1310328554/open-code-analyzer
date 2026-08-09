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

package org.springframework.boot.autoconfigure.data;

/**
 * 要启用的 Spring Data Repository 类型。
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public enum RepositoryType {

	/**
	 * 根据可用性自动启用所有 Repository 类型。
	 */
	AUTO,

	/**
	 * 启用命令式（imperative）Repository。
	 */
	IMPERATIVE,

	/**
	 * 不启用任何 Repository。
	 */
	NONE,

	/**
	 * 启用响应式（reactive）Repository。
	 */
	REACTIVE

}
