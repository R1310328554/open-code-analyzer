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

package org.springframework.aop.aspectj;

import org.springframework.aop.PointcutAdvisor;

/**
 * 由 Spring AOP Advisor 实现的接口，包装可能具有延迟初始化策略的 AspectJ 方面。例如，perThis 实例化模型意味着建议的延迟初始化。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public interface InstantiationModelAwarePointcutAdvisor extends PointcutAdvisor {

	/**
	 * 返回该顾问程序是否正在延迟初始化其基础建议。
	 */
	boolean isLazy();

	/**
	 * 返回该顾问是否已经实例化其建议。
	 */
	boolean isAdviceInstantiated();

}
