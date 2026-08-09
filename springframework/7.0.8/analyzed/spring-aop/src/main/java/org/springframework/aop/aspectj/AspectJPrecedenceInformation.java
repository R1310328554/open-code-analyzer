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

import org.springframework.core.Ordered;

/**
 * 由类型实现的接口，可以提供按 AspectJ 的优先级规则对建议/顾问进行排序所需的信息。
 * @author Adrian Colyer
 * @since 2.0
 * @see org.springframework.aop.aspectj.autoproxy.AspectJPrecedenceComparator
 */
public interface AspectJPrecedenceInformation extends Ordered {

	// 实施注意事项：
	// 我们需要该接口提供的间接级别，否则
	// 在所有情况下，AspectJPrecedenceComparator 都必须询问 Advisor 的建议
	// 以便对顾问进行排序。这会导致问题
	// 需要延迟创建的InstantiationModelAwarePointcutAdvisor
	// 它对非单例实例化模型方面的建议。

	/**
	 * 返回声明通知的方面（bean）的名称。
	 */
	String getAspectName();

	/**
	 * 返回方面内通知成员的声明顺序。
	 */
	int getDeclarationOrder();

	/**
	 * 返回这是否是之前的建议。
	 */
	boolean isBeforeAdvice();

	/**
	 * 返回这是否是事后建议。
	 */
	boolean isAfterAdvice();

}
