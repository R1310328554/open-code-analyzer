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
 * 简单策略，允许工具控制如何将来源元数据附加到 Bean 定义元数据。
 *
 * <p>配置解析器<strong>可以</strong>在解析阶段提供附加来源元数据的能力。
 * 它们会以通用格式提供该元数据，可由 {@link SourceExtractor} 进一步修改，
 * 再附加到 Bean 定义元数据。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.BeanMetadataElement#getSource()
 * @see org.springframework.beans.factory.config.BeanDefinition
 */
@FunctionalInterface
public interface SourceExtractor {

	/**
	 * 从配置解析器提供的候选对象中提取来源元数据。
	 * @param sourceCandidate 原始来源元数据（永不为 {@code null}）
	 * @param definingResource 定义给定来源对象的资源（可为 {@code null}）
	 * @return 要存储的来源元数据对象（可为 {@code null}）
	 */
	@Nullable Object extractSource(Object sourceCandidate, @Nullable Resource definingResource);

}
