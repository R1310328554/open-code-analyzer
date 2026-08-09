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
 * 实现类可为特定 Bean 创建特殊 TargetSource，
 * 例如池化 TargetSource。选择可基于目标类上的属性（如池化属性）。
 *
 * <p>AbstractAutoProxyCreator 可支持多个 TargetSourceCreator，
 * 按顺序应用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@FunctionalInterface
public interface TargetSourceCreator {

	/**
	 * 为给定 Bean 创建特殊 TargetSource（若有）。
	 * @param beanClass 要创建 TargetSource 的 Bean 类
	 * @param beanName Bean 名称
	 * @return 特殊 TargetSource；若本 TargetSourceCreator 对该 Bean 无兴趣则 {@code null}
	 */
	@Nullable TargetSource getTargetSource(Class<?> beanClass, String beanName);

}
