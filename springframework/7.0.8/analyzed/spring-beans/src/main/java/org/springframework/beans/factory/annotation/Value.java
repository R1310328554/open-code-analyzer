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

package org.springframework.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可用于字段或方法/构造器参数，为被标注元素指定默认值表达式。
 *
 * <p>通常用于表达式驱动或属性驱动的依赖注入；也支持在处理器方法参数上动态解析，
 * 例如在 Spring MVC 中。
 *
 * <p>常见用法是通过 SpEL（Spring 表达式语言）注入值，例如
 * <code>#{systemProperties.myProp}</code>；也可使用
 * <code>${my.app.myProp}</code> 风格的属性占位符。
 *
 * <p>注意：{@code @Value} 的实际处理由
 * {@link org.springframework.beans.factory.config.BeanPostProcessor
 * BeanPostProcessor} 完成，因此<em>不能</em>在
 * {@link org.springframework.beans.factory.config.BeanPostProcessor
 * BeanPostProcessor} 或
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor}
 * 类型中使用 {@code @Value}。请参阅 {@link AutowiredAnnotationBeanPostProcessor}
 * 的文档（默认会检测此注解是否存在）。
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see AutowiredAnnotationBeanPostProcessor
 * @see Autowired
 * @see org.springframework.beans.factory.config.BeanExpressionResolver
 * @see org.springframework.beans.factory.support.AutowireCandidateResolver#getSuggestedValue
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {

	/**
	 * 实际值表达式，例如 <code>#{systemProperties.myProp}</code>，
	 * 或属性占位符，例如 <code>${my.app.myProp}</code>。
	 */
	String value();

}
