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
 * 表示被注解的类是“Service”，最初由领域驱动设计（Evans, 2003）定义为
 * “作为接口提供的、在模型中独立存在且无封装状态的操作”。
 *
 * <p>也可表示类是“Business Service Facade”（Core J2EE 模式意义下）或类似角色。
 * 本注解是通用构造型，各团队可按需收窄语义与用法。
 *
 * <p>本注解是 {@link Component @Component} 的特化，
 * 允许实现类通过类路径扫描自动检测。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see Component
 * @see Repository
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {

	/**
	 * {@link Component#value} 的别名。
	 */
	@AliasFor(annotation = Component.class)
	String value() default "";

}
