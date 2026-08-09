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

package org.springframework.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSR-303 {@link jakarta.validation.Valid} 的变体，支持指定校验分组。
 * 便于与 Spring 的 JSR-303 支持配合使用，但并非 JSR-303 专用。
 *
 * <p>例如可用于 Spring MVC 处理器方法参数。
 * 通过 {@link org.springframework.validation.SmartValidator} 的校验提示概念支持，
 * 校验分组类作为提示对象。
 *
 * <p>也可用于方法级校验，表示特定类应在方法级进行校验
 *（作为对应校验拦截器的切点），并可选择指定被注解类的方法级校验分组。
 * 在方法级应用本注解可覆盖特定方法的校验分组，但不作为切点；
 * 要触发特定 Bean 的方法校验，仍需要类级注解。
 * 还可用作自定义构造型注解或自定义分组校验注解的元注解。
 *
 * <p>本注解可用作<em>元注解</em>以创建自定义<em>组合注解</em>。
 *
 * @author Juergen Hoeller
 * @since 3.1
 * @see jakarta.validation.Validator#validate(Object, Class[])
 * @see org.springframework.validation.SmartValidator#validate(Object, org.springframework.validation.Errors, Object...)
 * @see org.springframework.validation.beanvalidation.SpringValidatorAdapter
 * @see org.springframework.validation.beanvalidation.MethodValidationPostProcessor
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Validated {

	/**
	 * 指定本注解触发的校验步骤所应用的校验分组。
	 * <p>JSR-303 将校验分组定义为应用声明的自定义注解，
	 * 专门用作类型安全的分组参数，如 {@link org.springframework.validation.beanvalidation.SpringValidatorAdapter} 中的实现。
	 * <p>其他 {@link org.springframework.validation.SmartValidator} 实现也可能以其他方式支持类参数。
	 */
	Class<?>[] value() default {};

}
