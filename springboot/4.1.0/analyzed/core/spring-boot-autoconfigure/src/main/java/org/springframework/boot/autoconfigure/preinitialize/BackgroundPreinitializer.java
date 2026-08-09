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

package org.springframework.boot.autoconfigure.preinitialize;

/**
 * 用于在后台预初始化代码的接口；这些代码若首次调用时初始化可能导致延迟。
 * 实现类应在 {@code spring.factories} 中注册。
 *
 * @author Phillip Webb
 * @since 4.0.0
 */
@FunctionalInterface
public interface BackgroundPreinitializer {

	/**
	 * 执行所需的预初始化。
	 * @throws Exception 初始化出错时
	 */
	void preinitialize() throws Exception;

}
