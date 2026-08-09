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

package org.springframework.stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 表示被注解的类是“Repository”，最初由领域驱动设计（Evans, 2003）定义为
 * “封装存储、检索和搜索行为、模拟对象集合的机制”。
 *
 * <p>实现传统 Jakarta EE 模式（如“Data Access Object”）的团队
 * 也可将此构造型应用于 DAO 类，但在此之前应理解
 * Data Access Object 与 DDD 风格仓储之间的区别。
 * 本注解是通用构造型，各团队可按需收窄语义与用法。
 *
 * <p>如此注解的类在与 {@link
 * org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
 * PersistenceExceptionTranslationPostProcessor} 配合使用时，
 * 有资格进行 Spring {@link org.springframework.dao.DataAccessException DataAccessException} 转换。
 * 被注解类在整体应用架构中的角色也会为工具、切面等目的而明确。
 *
 * <p>本注解也是 {@link Component @Component} 的特化，
 * 允许实现类通过类路径扫描自动检测。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see Component
 * @see Service
 * @see org.springframework.dao.DataAccessException
 * @see org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Repository {

	/**
	 * {@link Component#value} 的别名。
	 */
	@AliasFor(annotation = Component.class)
	String value() default "";

}
