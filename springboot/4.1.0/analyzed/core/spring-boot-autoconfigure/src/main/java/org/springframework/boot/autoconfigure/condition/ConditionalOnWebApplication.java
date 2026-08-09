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
 * 当应用程序为 Web 应用程序时匹配的 {@link Conditional @Conditional}。
 * 默认情况下任何 Web 应用程序均匹配，但可通过 {@link #type()} 属性缩小范围。
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 * @since 1.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnWebApplicationCondition.class)
public @interface ConditionalOnWebApplication {

	/**
	 * 所需的 Web 应用程序类型。
	 * @return 所需的 Web 应用程序类型
	 */
	Type type() default Type.ANY;

	/**
	 * 可用的应用程序类型。
	 */
	enum Type {

		/**
		 * 任何 Web 应用程序均匹配。
		 */
		ANY,

		/**
		 * 仅基于 Servlet 的 Web 应用程序匹配。
		 */
		SERVLET,

		/**
		 * 仅基于响应式的 Web 应用程序匹配。
		 */
		REACTIVE

	}

}
