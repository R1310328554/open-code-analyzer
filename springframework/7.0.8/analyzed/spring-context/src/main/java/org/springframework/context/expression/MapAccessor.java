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

package org.springframework.context.expression;

import org.springframework.expression.PropertyAccessor;

/**
 * 能够访问标准 {@link java.util.Map} 键的 SpEL {@link PropertyAccessor}。
 *
 * @author Juergen Hoeller
 * @author Andy Clement
 * @since 3.0
 * @deprecated 自 Spring Framework 7.0 起已弃用，请改用
 * {@link org.springframework.expression.spel.support.MapAccessor}。
 */
@Deprecated(since = "7.0", forRemoval = true)
public class MapAccessor extends org.springframework.expression.spel.support.MapAccessor {

	/**
	 * 创建支持读写的 {@code MapAccessor}。
	 * @see #MapAccessor(boolean)
	 */
	public MapAccessor() {
		this(true);
	}

	/**
	 * 创建支持读取、并可选择是否支持写入的 {@code MapAccessor}。
	 * @param allowWrite 是否允许对目标实例执行写操作
	 * @since 6.2
	 * @see #canWrite
	 */
	public MapAccessor(boolean allowWrite) {
		super(allowWrite);
	}

}
