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
 * 在匹配方法时考虑引介的 {@link MethodMatcher} 特化类型。
 * 例如，若目标类上没有引介，方法匹配器可能更高效地优化匹配。
 *
 * @author Adrian Colyer
 * @since 2.0
 */
public interface IntroductionAwareMethodMatcher extends MethodMatcher {

	/**
	 * 静态检查给定方法是否匹配。若调用方支持扩展的 IntroductionAwareMethodMatcher 接口，
	 * 可调用本方法替代两参数 {@link #matches(java.lang.reflect.Method, Class)}。
	 * @param method 候选方法
	 * @param targetClass 目标类
	 * @param hasIntroductions 若被询问对象是一个或多个引介的主体则为 {@code true}，否则为 {@code false}
	 * @return 该方法是否静态匹配
	 */
	boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions);

}
