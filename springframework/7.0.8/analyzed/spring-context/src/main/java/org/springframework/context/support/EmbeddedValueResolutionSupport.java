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

import org.jspecify.annotations.Nullable;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;

/**
 * 需要嵌入式值解析（即 {@link org.springframework.context.EmbeddedValueResolverAware} 消费者）
 * 的组件的便捷基类。
 *
 * @author Juergen Hoeller
 * @since 4.1
 */
public class EmbeddedValueResolutionSupport implements EmbeddedValueResolverAware {

	private @Nullable StringValueResolver embeddedValueResolver;


	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}

	/**
	 * 通过本实例的 {@link StringValueResolver} 解析给定的嵌入式值。
	 * @param value 要解析的值
	 * @return 解析后的值；若无可用解析器则始终返回原始值
	 * @see #setEmbeddedValueResolver
	 */
	protected @Nullable String resolveEmbeddedValue(String value) {
		return (this.embeddedValueResolver != null ? this.embeddedValueResolver.resolveStringValue(value) : value);
	}


}
