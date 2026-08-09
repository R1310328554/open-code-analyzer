/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.autoconfigure.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * 依赖于 SpEL 表达式值的条件元素的配置注解。
 * <p>
 * 在表达式中引用 Bean 会导致该 Bean 在上下文刷新处理的极早阶段被初始化。
 * 因此该 Bean 将无法进行后处理（例如配置属性绑定），其状态可能不完整。
 *
 * @author Dave Syer
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnExpressionCondition.class)
public @interface ConditionalOnExpression {

	/**
	 * 要评估的 SpEL 表达式。表达式应返回 {@code true} 表示条件通过，{@code false} 表示失败。
	 * @return SpEL 表达式
	 */
	String value() default "true";

}
