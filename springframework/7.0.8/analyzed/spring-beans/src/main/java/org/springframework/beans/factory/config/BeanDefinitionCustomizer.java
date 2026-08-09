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

package org.springframework.beans.factory.config;

/**
 * 用于自定义给定 bean 定义的回调接口。
 * 设计为与 lambda 表达式或方法引用配合使用。
 *
 * @author Juergen Hoeller
 * @since 5.0
 * @see org.springframework.beans.factory.support.BeanDefinitionBuilder#applyCustomizers
 */
@FunctionalInterface
public interface BeanDefinitionCustomizer {

	/**
	 * 自定义给定的 bean 定义。
	 * @param bd 待自定义的 bean 定义
	 */
	void customize(BeanDefinition bd);

}
