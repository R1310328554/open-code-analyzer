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

package org.springframework.beans.support;

/**
 * 按 Bean 属性对 Bean 实例进行排序的定义。
 *
 * @author Juergen Hoeller
 * @since 26.05.2003
 * @deprecated as severely outdated and superseded by more modern solutions,
 * for example in Spring Data Commons
 */
@Deprecated(since = "7.0.3", forRemoval = true)
public interface SortDefinition {

	/**
	 * 返回用于比较的 Bean 属性名。
	 * 也可以是嵌套的 Bean 属性路径。
	 */
	String getProperty();

	/**
	 * 返回比较 String 值时是否忽略大小写。
	 */
	boolean isIgnoreCase();

	/**
	 * 返回是否升序排序（{@code true}）或降序排序（{@code false}）。
	 */
	boolean isAscending();

}
