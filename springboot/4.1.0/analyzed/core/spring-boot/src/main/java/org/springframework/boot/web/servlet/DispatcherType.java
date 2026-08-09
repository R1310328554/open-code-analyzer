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

package org.springframework.boot.web.servlet;

/**
 * 过滤器分发类型枚举，与 {@link jakarta.servlet.DispatcherType} 一致，
 * 用于配置场景（Servlet API 可能不存在）。
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
public enum DispatcherType {

	/**
	 * 在 "RequestDispatcher.forward()" 调用时应用过滤器。
	 */
	FORWARD,

	/**
	 * 在 "RequestDispatcher.include()" 调用时应用过滤器。
	 */
	INCLUDE,

	/**
	 * 在普通客户端请求时应用过滤器。
	 */
	REQUEST,

	/**
	 * 在从 AsyncContext 分发的调用中应用过滤器。
	 */
	ASYNC,

	/**
	 * 在处理错误时应用过滤器。
	 */
	ERROR

}
