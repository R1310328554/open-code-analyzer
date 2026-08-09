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
 * 应用的「存活（Liveness）」状态。
 * <p>
 * 当应用正在运行且内部状态正确时视为存活。「存活」失败表示应用内部状态已损坏且
 * 无法恢复，平台应重启应用。
 *
 * @author Brian Clozel
 * @since 2.3.0
 */
public enum LivenessState implements AvailabilityState {

	/**
	 * 应用正在运行且内部状态正确。
	 */
	CORRECT,

	/**
	 * 应用正在运行但内部状态已损坏。
	 */
	BROKEN

}
