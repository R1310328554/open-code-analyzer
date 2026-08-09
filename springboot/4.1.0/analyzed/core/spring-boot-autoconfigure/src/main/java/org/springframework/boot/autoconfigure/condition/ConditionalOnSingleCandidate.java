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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * 仅当 {@link BeanFactory} 中已包含指定类的 Bean 且能确定唯一候选时才匹配的
 * {@link Conditional @Conditional}。
 * <p>
 * 若 {@link BeanFactory} 中已包含多个匹配实例但已定义主候选，条件也会匹配；
 * 本质上，当按定义类型自动装配 Bean 能够成功时条件即匹配。
 * <p>
 * 该条件只能匹配应用上下文迄今已处理的 Bean 定义，因此强烈建议仅在自动配置类上使用。
 * 若候选 Bean 可能由其他自动配置创建，请确保使用此条件的配置在其之后运行。
 *
 * @author Stephane Nicoll
 * @since 1.3.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnSingleCandidate {

	/**
	 * 要检查的 Bean 类类型。当 {@link BeanFactory} 中包含指定类的 Bean 且
	 * 存在多个实例时有主候选，条件即匹配。非自动装配候选、非默认候选或回退候选的 Bean 会被忽略。
	 * <p>
	 * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，
	 * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。
	 * 若要将该注解作为元注解使用，请仅使用 {@link #type} 属性。
	 * <p>
	 * 该属性<strong>不得</strong>与 {@link #type()} 同时使用，但可作为 {@link #type()} 的替代。
	 * @return 要检查的 Bean 类类型
	 * @see Bean#autowireCandidate()
	 * @see BeanDefinition#isAutowireCandidate
	 * @see Bean#defaultCandidate()
	 * @see AbstractBeanDefinition#isDefaultCandidate
	 */
	Class<?> value() default Object.class;

	/**
	 * 要检查的 Bean 类类型名。当 {@link BeanFactory} 中包含指定类的 Bean 且
	 * 存在多个实例时有主候选，条件即匹配。非自动装配候选、非默认候选或回退候选的 Bean 会被忽略。
	 * <p>
	 * 该属性<strong>不得</strong>与 {@link #value()} 同时使用，但可作为 {@link #value()} 的替代。
	 * @return 要检查的 Bean 类类型名
	 * @see Bean#autowireCandidate()
	 * @see BeanDefinition#isAutowireCandidate
	 * @see Bean#defaultCandidate()
	 * @see AbstractBeanDefinition#isDefaultCandidate
	 */
	String type() default "";

	/**
	 * 决定是否应考虑应用上下文层次结构（父上下文）的策略。
	 * @return 搜索策略
	 */
	SearchStrategy search() default SearchStrategy.ALL;

}
