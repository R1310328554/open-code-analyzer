/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.aop.target.dynamic;

/**
 * 动态目标对象应实现的接口，
 * 支持重新加载并可选择轮询更新。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0
 */
public interface Refreshable {

	/**
	 * 刷新底层目标对象。
	 */
	void refresh();

	/**
	 * 返回启动以来的实际刷新次数。
	 */
	long getRefreshCount();

	/**
	 * 返回上次实际刷新的时间（时间戳）。
	 */
	long getLastRefreshTime();

}
