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

package org.springframework.context.annotation;

/**
 * 与 {@code @Configuration} 配合使用时提供更细粒度控制的 {@link Condition}。
 * 允许某些条件根据配置阶段在匹配时自适应。例如，检查某 Bean 是否已注册的条件
 * 可选择在 {@link ConfigurationPhase#REGISTER_BEAN REGISTER_BEAN}
 * {@link ConfigurationPhase} 阶段才进行评估。
 *
 * @author Phillip Webb
 * @since 4.0
 * @see Configuration
 */
public interface ConfigurationCondition extends Condition {

	/**
	 * 返回条件应被评估的 {@link ConfigurationPhase}。
	 */
	ConfigurationPhase getConfigurationPhase();


	/**
	 * 条件可被评估的各个配置阶段。
	 */
	enum ConfigurationPhase {

		/**
		 * 在解析 {@code @Configuration} 类时评估 {@link Condition}。
		 * <p>若此阶段条件不匹配，该 {@code @Configuration} 类将不会被加入。
		 */
		PARSE_CONFIGURATION,

		/**
		 * 在添加普通（非 {@code @Configuration}）Bean 时评估 {@link Condition}。
		 * 该条件不会阻止 {@code @Configuration} 类被加入。
		 * <p>评估条件时，所有 {@code @Configuration} 类均已解析完毕。
		 */
		REGISTER_BEAN
	}

}
