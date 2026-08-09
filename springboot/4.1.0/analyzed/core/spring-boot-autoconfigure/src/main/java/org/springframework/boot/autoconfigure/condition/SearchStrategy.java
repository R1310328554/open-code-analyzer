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

package org.springframework.boot.autoconfigure.condition;

/**
 * Bean 工厂层次结构中搜索 Bean 的命名策略。
 * <p>
 * 用于 {@link ConditionalOnBean}、{@link ConditionalOnMissingBean} 等条件注解，
 * 控制是否在父上下文或整个层次结构中查找 Bean。
 *
 * @author Dave Syer
 * @since 1.0.0
 */
public enum SearchStrategy {

	/**
	 * 仅在当前上下文中搜索。
	 */
	CURRENT,

	/**
	 * 搜索所有祖先上下文，但不包括当前上下文。
	 */
	ANCESTORS,

	/**
	 * 搜索整个层次结构。
	 */
	ALL

}
