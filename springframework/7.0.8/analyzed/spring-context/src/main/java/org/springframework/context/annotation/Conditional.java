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

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指示组件仅当所有 {@linkplain #value 指定条件}均匹配时才具备注册资格。
 *
 * <p><em>条件</em>指在 Bean 定义即将注册之前可通过编程方式确定的任意状态
 * （详见 {@link Condition}）。
 *
 * <p>{@code @Conditional} 注解可按以下方式使用：
 * <ul>
 * <li>作为类型级注解，标注于直接或间接带有 {@code @Component} 的任意类上，
 * 包括 {@link Configuration @Configuration} 类</li>
 * <li>作为元注解，用于组合自定义构造型注解</li>
 * <li>作为方法级注解，标注于任意 {@link Bean @Bean} 方法上</li>
 * </ul>
 *
 * <p>若 {@code @Configuration} 类带有 {@code @Conditional}，
 * 则该类关联的所有 {@code @Bean} 方法、{@link Import @Import} 注解以及
 * {@link ComponentScan @ComponentScan} 注解均受相应条件约束。
 *
 * <p><strong>注意</strong>：不支持 {@code @Conditional} 注解的继承；
 * 来自超类或重写方法的条件不会被考虑。为强制执行此语义，
 * {@code @Conditional} 本身未声明为
 * {@link java.lang.annotation.Inherited @Inherited}；此外，任何以
 * {@code @Conditional} 作为元注解的自定义<em>组合注解</em>也不得声明为 {@code @Inherited}。
 *
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 4.0
 * @see Condition
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {

	/**
	 * 组件注册前必须全部 {@linkplain Condition#matches 匹配}的 {@link Condition} 类。
	 */
	Class<? extends Condition>[] value();

}
