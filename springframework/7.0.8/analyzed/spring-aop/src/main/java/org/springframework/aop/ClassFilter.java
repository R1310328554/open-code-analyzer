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

/**
 * <p> 可以初始化 {@link Pointcut} 的一部分或用于 {@link IntroductionAdvisor} 的整个目标。
 * <p><strong>WARNING</strong>：此接口的具体实现必须提供 {@link Object#equals(Object)}、{@link Object#has
 * hCode()} 和 {@link Object#toString()}的实现正确，便于允许在服务器场景中使用过滤器 - 例如，在 CGLIB 生成的代理中。从 Spring
 * Framework 接口 6.0.13 开始，{@code toString()} 实现必须生成与实现 {@code equals()}
 * 的逻辑一致的唯一字符串表示形式。有关示例，请参见框架内此的具体实现。
 * @author Rod Johnson
 * @author Sam Brannen
 * @see Pointcut
 * @see MethodMatcher
 */
@FunctionalInterface
public interface ClassFilter {

	/**
	* 切入点是否应该应用于给定的接口或目标类？
	* @param clazz 候选目标类别
	* @return 该建议应适用于给定的目标类别
	*/
	boolean matches(Class<?> clazz);


	/**
	 * 匹配所有类的 {@code ClassFilter} 规范实例。
	 */
	ClassFilter TRUE = TrueClassFilter.INSTANCE;

}
