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

package org.springframework.boot.web.context.reactive;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.io.ApplicationResourceLoader;
import org.springframework.boot.io.ApplicationResourceLoader.FilePathResolver;
import org.springframework.core.io.Resource;

/**
 * 面向 {@link FilteredReactiveWebContextResource} 的 {@link FilePathResolver} 实现。
 * 当资源为 {@link FilteredReactiveWebContextResource} 实例时返回原始 location，否则返回 {@code null}。
 *
 * @author Dmytro Nosan
 */
class FilteredReactiveWebContextResourceFilePathResolver implements ApplicationResourceLoader.FilePathResolver {

	@Override
	public @Nullable String resolveFilePath(String location, Resource resource) {
		return (resource instanceof FilteredReactiveWebContextResource) ? location : null;
	}

}
