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

package org.springframework.boot.autoconfigure.data;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * 仅当已启用特定类型的 Spring Data Repository 时才匹配的
 * {@link Conditional @Conditional}。
 * <p>
 * 通过 {@code spring.data.<store>.repositories.type} 属性
 * 与注解指定的 {@link RepositoryType} 进行比较。
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnRepositoryTypeCondition.class)
public @interface ConditionalOnRepositoryType {

	/**
	 * 支撑 Repository 的存储名称（如 {@code mongodb}、{@code redis}）。
	 *
	 * @return 存储名称
	 */
	String store();

	/**
	 * 所需的 Repository 类型。
	 *
	 * @return 所需的 Repository 类型
	 */
	RepositoryType type();

}
