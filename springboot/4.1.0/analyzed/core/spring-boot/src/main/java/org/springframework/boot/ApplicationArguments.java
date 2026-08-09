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

import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * 提供对运行 {@link SpringApplication} 时所用参数的访问。
 *
 * @author Phillip Webb
 * @since 1.3.0
 */
public interface ApplicationArguments {

	/**
	 * 返回传递给应用的原始未处理参数。
	 * @return 参数
	 */
	String[] getSourceArgs();

	/**
	 * 返回所有选项参数的名称。例如参数为 {@code --foo=bar --debug} 时
	 * 返回 {@code ["foo", "debug"]}。
	 * @return 选项名称或空集合
	 */
	Set<String> getOptionNames();

	/**
	 * 返回从参数解析出的选项集合是否包含给定名称的选项。
	 * @param name 要检查的名称
	 * @return 参数包含该名称的选项时返回 {@code true}
	 */
	boolean containsOption(String name);

	/**
	 * 返回与给定名称选项关联的值集合。
	 * <ul>
	 * <li>若选项存在且无参数（如 {@code --foo}），返回空集合（{@code []}）</li>
	 * <li>若选项存在且有一个值（如 {@code --foo=bar}），返回单元素集合（{@code ["bar"]}）</li>
	 * <li>若选项存在且有多个值（如 {@code --foo=bar --foo=baz}），
	 * 返回包含各值的集合（{@code ["bar", "baz"]}）</li>
	 * <li>若选项不存在，返回 {@code null}</li>
	 * </ul>
	 * @param name 选项名称
	 * @return 给定名称的选项值列表
	 */
	@Nullable List<String> getOptionValues(String name);

	/**
	 * 返回解析出的非选项参数集合。
	 * @return 非选项参数或空列表
	 */
	List<String> getNonOptionArgs();

}
