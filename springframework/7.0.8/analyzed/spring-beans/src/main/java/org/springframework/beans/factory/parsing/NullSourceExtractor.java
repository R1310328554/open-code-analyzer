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

package org.springframework.beans.factory.parsing;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.Resource;

/**
 * {@link SourceExtractor} 的简单实现，始终将来源元数据返回为 {@code null}。
 *
 * <p>这是默认实现，可防止在正常（非工具化）运行时占用过多内存来保存元数据。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class NullSourceExtractor implements SourceExtractor {

	/**
	 * 本实现对任何输入均返回 {@code null}。
	 */
	@Override
	public @Nullable Object extractSource(Object sourceCandidate, @Nullable Resource definitionResource) {
		return null;
	}

}
