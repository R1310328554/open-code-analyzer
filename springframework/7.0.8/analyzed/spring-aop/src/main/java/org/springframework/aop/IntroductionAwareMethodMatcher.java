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

package org.springframework.aop;

import java.lang.reflect.Method;

/**
 * 一种特殊类型的 {@link MethodMatcher}，在匹配方法时考虑引入。例如，如果没有对目标类的介绍，方法匹配器可能能够更有效地优化匹配。
 * @author Adrian Colyer
 * @since 2.0
 */
public interface IntroductionAwareMethodMatcher extends MethodMatcher {

	/**
	 * 执行静态检查给定方法是否匹配。如果调用者支持扩展的IntroductionAwareMethodMatcher 接口，则可以调用此方法而不是 2-arg {@link #mat
	 * c hes(java.lang.reflect.Method, Class)} 方法。
	 * @param method 候选方法
	 * @param targetClass 目标类别
	 * @param hasIntroductions {@code true} 如果我们所代表的对象是一个或多个介绍的主题； {@code false} 否则
	 * @return
	 */
	boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions);

}
