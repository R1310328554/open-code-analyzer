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

import java.lang.annotation.Annotation;
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
 * 仅当 {@link BeanFactory} 中已包含满足所有指定要求的 Bean 时才匹配的
 * {@link Conditional @Conditional}。所有要求都必须满足，但不必由同一个 Bean 满足。
 * <p>
 * 当标注在 {@link Bean @Bean} 方法上且未指定 {@link #value}、{@link #type}、
 * {@link #name} 或 {@link #annotation} 时，要匹配的 Bean 类型默认为
 * {@code @Bean} 方法的返回类型：
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyAutoConfiguration {
 *
 *     &#064;ConditionalOnBean
 *     &#064;Bean
 *     public MyService myService() {
 *         ...
 *     }
 *
 * }</pre>
 * <p>
 * 上例中，若 {@link BeanFactory} 中已包含类型为 {@code MyService} 的 Bean，条件即匹配。
 * <p>
 * 该条件只能匹配应用上下文迄今已处理的 Bean 定义，因此强烈建议仅在自动配置类上使用。
 * 若候选 Bean 可能由其他自动配置创建，请确保使用此条件的配置在其之后运行。
 *
 * @author Phillip Webb
 * @since 1.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnBean {

	/**
	 * 要检查的 Bean 类类型。当 {@link BeanFactory} 中包含所有指定类的 Bean 时条件匹配。
	 * 非自动装配候选或非默认候选的 Bean 会被忽略。
	 * <p>
	 * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，
	 * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。
	 * 若要将该注解作为元注解使用，请仅使用 {@link #type} 属性。
	 * @return 要检查的 Bean 类类型
	 * @see Bean#autowireCandidate()
	 * @see BeanDefinition#isAutowireCandidate
	 * @see Bean#defaultCandidate()
	 * @see AbstractBeanDefinition#isDefaultCandidate
	 */
	Class<?>[] value() default {};

	/**
	 * 要检查的 Bean 类类型名。当 {@link BeanFactory} 中包含所有指定类的 Bean 时条件匹配。
	 * 非自动装配候选或非默认候选的 Bean 会被忽略。
	 * @return 要检查的 Bean 类类型名
	 * @see Bean#autowireCandidate()
	 * @see BeanDefinition#isAutowireCandidate
	 * @see Bean#defaultCandidate()
	 * @see AbstractBeanDefinition#isDefaultCandidate
	 */
	String[] type() default {};

	/**
	 * 要检查的、标注在 Bean 上的注解类型。当 {@link BeanFactory} 中所有指定注解
	 * 均已定义在 Bean 上时条件匹配。非自动装配候选或非默认候选的 Bean 会被忽略。
	 * <p>
	 * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，
	 * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。
	 * @return 要检查的类级别注解类型
	 * @see Bean#autowireCandidate()
	 * @see BeanDefinition#isAutowireCandidate
	 * @see Bean#defaultCandidate()
	 * @see AbstractBeanDefinition#isDefaultCandidate
	 */
	Class<? extends Annotation>[] annotation() default {};

	/**
	 * 要检查的 Bean 名称。当 {@link BeanFactory} 中包含所有指定名称时条件匹配。
	 * @return 要检查的 Bean 名称
	 */
	String[] name() default {};

	/**
	 * 决定是否应考虑应用上下文层次结构（父上下文）的策略。
	 * @return 搜索策略
	 */
	SearchStrategy search() default SearchStrategy.ALL;

	/**
	 * 其泛型参数中可能包含指定 Bean 类型的附加类。例如，注解声明
	 * {@code value=Name.class} 和 {@code parameterizedContainer=NameRegistration.class}
	 * 将同时检测 {@code Name} 和 {@code NameRegistration<Name>}。
	 * <p>
	 * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，
	 * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。
	 * @return 容器类型
	 * @since 2.1.0
	 */
	Class<?>[] parameterizedContainer() default {};

}
