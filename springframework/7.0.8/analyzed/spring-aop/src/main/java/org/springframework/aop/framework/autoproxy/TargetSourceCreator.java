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

package org.springframework.aop.framework.autoproxy;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;

/**
 * 实现可以为特定的 bean 创建特殊的目标源，例如池化目标源。例如，他们的选择可能基于目标类的属性，例如池属性。
 * <p>AbstractAutoProxyCreator可以支持多个TargetSourceCreators，这些TargetSourceCreators将按顺序应用。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@FunctionalInterface
public interface TargetSourceCreator {

	/**
	 * 为给定的 bean 创建一个特殊的 TargetSource（如果有）。
	 * @param beanClass 要为其创建 TargetSource 的 bean 类
	 * @param beanName 豆子的名字
	 * @return 特殊 TargetSource 或 {@code null}（如果此 TargetSourceCreator 对特定 bean 不感兴趣）
	 */
	@Nullable TargetSource getTargetSource(Class<?> beanClass, String beanName);

}
