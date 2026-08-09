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

import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * 支持自定义报告 {@link SpringApplication} 启动错误的回调接口。
 * <p>
 * {@link SpringBootExceptionReporter} 通过 {@link SpringFactoriesLoader} 加载，
 * 须声明接受单个 {@link ConfigurableApplicationContext} 参数的 public 构造器。
 *
 * @author Phillip Webb
 * @since 2.0.0
 * @see ApplicationContextAware
 */
@FunctionalInterface
public interface SpringBootExceptionReporter {

	/**
	 * 向用户报告启动失败。
	 *
	 * @param failure 失败原因
	 * @return 若已报告失败为 {@code true}，否则为 {@code false} 以使用默认报告
	 */
	boolean reportException(Throwable failure);

}
