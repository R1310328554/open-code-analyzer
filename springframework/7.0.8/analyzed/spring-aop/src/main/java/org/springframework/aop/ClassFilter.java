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
 * 将切入点或引介的匹配范围限制在给定目标类集合上的过滤器。
 *
 * <p>可作为 {@link Pointcut} 的一部分，或用于 {@link IntroductionAdvisor} 的整体目标选择。
 *
 * <p><strong>警告</strong>：本接口的具体实现必须正确实现
 * {@link Object#equals(Object)}、{@link Object#hashCode()} 与 {@link Object#toString()}，
 * 以便在缓存场景（例如 CGLIB 生成的代理）中使用。
 * 自 Spring Framework 6.0.13 起，{@code toString()} 必须生成与 {@code equals()} 逻辑一致的唯一字符串表示。
 * 可参考框架内本接口的具体实现示例。
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @see Pointcut
 * @see MethodMatcher
 */
@FunctionalInterface
public interface ClassFilter {

	/**
	 * 切入点是否应作用于给定接口或目标类？
	 * @param clazz 候选目标类
	 * @return advice 是否应作用于该目标类
	 */
	boolean matches(Class<?> clazz);


	/**
	 * 匹配所有类的 {@code ClassFilter} 规范实例。
	 */
	ClassFilter TRUE = TrueClassFilter.INSTANCE;

}
