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

package org.springframework.boot.origin;

import org.jspecify.annotations.Nullable;

/**
 * 提供对某项来源访问能力的接口。
 *
 * @author Phillip Webb
 * @since 2.0.0
 * @see Origin
 */
@FunctionalInterface
public interface OriginProvider {

	/**
	 * 返回源来源；若来源未知则返回 {@code null}。
	 *
	 * @return the origin or {@code null} 来源或 {@code null}
	 */
	@Nullable Origin getOrigin();

}
