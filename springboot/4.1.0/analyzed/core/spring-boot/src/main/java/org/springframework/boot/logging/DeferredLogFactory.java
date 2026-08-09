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

import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 可创建多个在适当时机切换的 {@link DeferredLog} 实例的工厂。
 *
 * @author Phillip Webb
 * @since 2.4.0
 * @see DeferredLogs
 */
@FunctionalInterface
public interface DeferredLogFactory {

	/**
	 * 为给定目标创建新的 {@link DeferredLog}。
	 *
	 * @param destination 最终日志目标类
	 * @return a deferred log instance that will switch to the destination when
	 * appropriate 适当时切换的延迟日志实例
	 */
	default Log getLog(Class<?> destination) {
		return getLog(() -> LogFactory.getLog(destination));
	}

	/**
	 * 为给定目标创建新的 {@link DeferredLog}。
	 *
	 * @param destination 最终日志目标
	 * @return a deferred log instance that will switch to the destination when
	 * appropriate 适当时切换的延迟日志实例
	 */
	default Log getLog(Log destination) {
		return getLog(() -> destination);
	}

	/**
	 * 为给定目标创建新的 {@link DeferredLog}。
	 *
	 * @param destination 最终日志目标供应器
	 * @return a deferred log instance that will switch to the destination when
	 * appropriate 适当时切换的延迟日志实例
	 */
	Log getLog(Supplier<Log> destination);

}
