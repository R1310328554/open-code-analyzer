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
 * 由能提供按 AspectJ 优先级规则排序 advice/advisor 所需信息的类型实现。
 *
 * @author Adrian Colyer
 * @since 2.0
 * @see org.springframework.aop.aspectj.autoproxy.AspectJPrecedenceComparator
 */
public interface AspectJPrecedenceInformation extends Ordered {

	// 实现说明：
	// 需要本接口提供的间接层，否则 AspectJPrecedenceComparator
	// 在所有情况下都须向 Advisor 索取 Advice 才能排序。
	// 这会导致 InstantiationModelAwarePointcutAdvisor 出现问题，
	// 该 Advisor 须为非单例实例化模型的切面延迟创建 advice。

	/**
	 * 返回声明该通知的切面（bean）名称。
	 */
	String getAspectName();

	/**
	 * 返回通知成员在切面内的声明顺序。
	 */
	int getDeclarationOrder();

	/**
	 * 返回是否为 before 通知。
	 */
	boolean isBeforeAdvice();

	/**
	 * 返回是否为 after 通知。
	 */
	boolean isAfterAdvice();

}
