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

package org.springframework.boot.logging;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * 供 {@link LoggingSystem#get(ClassLoader)} 查找实际实现的工厂类。
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public interface LoggingSystemFactory {

	/**
	 * 返回日志系统实现；无可用实现时返回 {@code null}。
	 *
	 * @param classLoader 要使用的类加载器
	 * @return a logging system 日志系统
	 */
	@Nullable LoggingSystem getLoggingSystem(ClassLoader classLoader);

	/**
	 * 返回由 {@code spring.factories} 支持的 {@link LoggingSystemFactory}。
	 *
	 * @return a {@link LoggingSystemFactory} instance 工厂实例
	 */
	static LoggingSystemFactory fromSpringFactories() {
		return new DelegatingLoggingSystemFactory(
				(classLoader) -> SpringFactoriesLoader.loadFactories(LoggingSystemFactory.class, classLoader));
	}

}
