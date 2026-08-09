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

import org.jspecify.annotations.Nullable;

/**
 * 低级钩子，用于将 {@link SpringApplicationRunListener} 附加到
 * {@link SpringApplication}，以观察或修改其行为。
 * <p>
 * Hook 按线程管理，在并行执行多个应用时提供隔离。
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @since 3.0.0
 * @see SpringApplication#withHook
 */
@FunctionalInterface
public interface SpringApplicationHook {

	/**
	 * 返回应挂接到给定 {@link SpringApplication} 的
	 * {@link SpringApplicationRunListener}。
	 *
	 * @param springApplication 源 {@link SpringApplication} 实例
	 * @return 要附加的 {@link SpringApplicationRunListener}，或 {@code null}
	 */
	@Nullable SpringApplicationRunListener getRunListener(SpringApplication springApplication);

}
