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

import org.springframework.boot.system.JavaVersion;
import org.springframework.context.annotation.Conditional;

/**
 * 根据应用程序运行的 JVM 版本进行匹配的 {@link Conditional @Conditional}。
 *
 * @author Oliver Gierke
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnJavaCondition.class)
public @interface ConditionalOnJava {

	/**
	 * 配置 {@link #value()} 中配置的值应视为上界（不含）还是下界（含）。
	 * 默认为 {@link Range#EQUAL_OR_NEWER}。
	 * @return 范围
	 */
	Range range() default Range.EQUAL_OR_NEWER;

	/**
	 * 要检查的 {@link JavaVersion}。使用 {@link #range()} 指定配置的值是上界（不含）还是下界（含）。
	 * @return Java 版本
	 */
	JavaVersion value();

	/**
	 * 范围选项。
	 */
	enum Range {

		/**
		 * 等于或高于指定的 {@link JavaVersion}。
		 */
		EQUAL_OR_NEWER,

		/**
		 * 低于指定的 {@link JavaVersion}。
		 */
		OLDER_THAN

	}

}
