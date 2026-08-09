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

package org.springframework.boot.logging.log4j2;

/**
 * 可用的滚动策略（rolling policy strategy）。
 *
 * @author Stephane Nicoll
 * @since 4.1.0
 */
public enum RollingPolicyStrategy {

	/**
	 * 基于文件大小滚动。
	 */
	SIZE,

	/**
	 * 基于时间滚动。
	 */
	TIME,

	/**
	 * 基于文件大小与时间滚动。
	 */
	SIZE_AND_TIME,

	/**
	 * 基于 cron 调度滚动。
	 */
	CRON

}
