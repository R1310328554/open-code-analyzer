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

package org.springframework.boot.task;

import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * 用于自定义 {@link SimpleAsyncTaskExecutor} 的回调接口。
 *
 * @author Stephane Nicoll
 * @author Moritz Halbritter
 * @since 3.2.0
 * @see SimpleAsyncTaskExecutorBuilder
 */
@FunctionalInterface
public interface SimpleAsyncTaskExecutorCustomizer {

	/**
	 * 自定义 {@link SimpleAsyncTaskExecutor} 实例的回调。
	 *
	 * @param taskExecutor the task executor to customize 待自定义的任务执行器
	 */
	void customize(SimpleAsyncTaskExecutor taskExecutor);

}
