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

import jakarta.servlet.Filter;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;

/**
 * 仅当 {@link BeanFactory} 中不包含指定类型的 {@link Filter} Bean 时才匹配的
 * {@link Conditional @Conditional}。该条件会检测直接注册的 {@link Filter} Bean，
 * 以及通过 {@link FilterRegistrationBean} 注册的 Bean。
 * <p>
 * 当标注在 {@code @Bean} 方法上时，Bean 类默认为工厂方法的返回类型，
 * 若 Bean 为 {@link FilterRegistrationBean} 则默认为其中的 {@link Filter} 类型：
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyAutoConfiguration {
 *
 *     &#064;ConditionalOnMissingFilterBean
 *     &#064;Bean
 *     public MyFilter myFilter() {
 *         ...
 *     }
 *
 * }</pre>
 * <p>
 * 上例中，若 {@link BeanFactory} 中尚不存在类型为 {@code MyFilter} 或
 * {@code FilterRegistrationBean<MyFilter>} 的 Bean，条件即匹配。
 *
 * @author Phillip Webb
 * @since 2.1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnMissingBean(parameterizedContainer = FilterRegistrationBean.class)
public @interface ConditionalOnMissingFilterBean {

	/**
	 * 必须不存在的过滤器 Bean 类型。
	 * @return Bean 类型
	 */
	@AliasFor(annotation = ConditionalOnMissingBean.class)
	Class<? extends Filter>[] value() default {};

}
