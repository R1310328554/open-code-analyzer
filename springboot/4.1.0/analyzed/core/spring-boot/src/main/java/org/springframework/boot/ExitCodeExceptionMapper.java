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

package org.springframework.boot;

/**
 * 提供异常与退出码之间映射的策略接口。
 *
 * @author Phillip Webb
 * @since 1.3.2
 */
@FunctionalInterface
public interface ExitCodeExceptionMapper {

	/**
	 * 返回应用应返回的退出码。
	 *
	 * @param exception 导致应用退出的异常
	 * @return 退出码，或 {@code 0}
	 */
	int getExitCode(Throwable exception);

}
