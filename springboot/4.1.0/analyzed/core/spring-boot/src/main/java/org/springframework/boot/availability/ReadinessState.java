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

package org.springframework.boot.availability;

/**
 * 应用的「就绪（Readiness）」状态。
 * <p>
 * 当应用 {@link LivenessState 存活} 且愿意接收流量时视为就绪。「就绪」失败表示
 * 应用无法接收流量，基础设施应停止向其路由请求。
 *
 * @author Brian Clozel
 * @since 2.3.0
 */
public enum ReadinessState implements AvailabilityState {

	/**
	 * 应用已就绪，可接收流量。
	 */
	ACCEPTING_TRAFFIC,

	/**
	 * 应用拒绝接收流量。
	 */
	REFUSING_TRAFFIC

}
