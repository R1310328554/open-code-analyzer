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

import org.springframework.context.annotation.Conditional;

/**
 * 仅当指定类在类路径上时才匹配的 {@link Conditional @Conditional}。
 * <p>
 * 在 {@code @Configuration} 类上可安全指定 {@code Class} 类型的 {@link #value() value}，
 * 因为注解元数据在类加载前通过 ASM 解析。若无法使用类引用，可使用 {@link #name() name}
 * {@code String} 属性。
 * <p>
 * <b>注意：</b>在 {@code @Bean} 方法上使用 {@code @ConditionalOnClass} 时需格外小心，
 * 因为通常返回类型是条件的目标。在方法上的条件生效前，JVM 会加载该类并可能处理方法引用，
 * 若类不存在将导致失败。为应对此场景，应使用独立的 {@code @Configuration} 类隔离条件。例如：
 * <pre class="code">
 * &#064;AutoConfiguration
 * public class MyAutoConfiguration {
 *
 * 	&#64;Configuration(proxyBeanMethods = false)
 * 	&#64;ConditionalOnClass(SomeService.class)
 * 	public static class SomeServiceConfiguration {
 *
 * 		&#64;Bean
 * 		&#64;ConditionalOnMissingBean
 * 		public SomeService someService() {
 * 			return new SomeService();
 * 		}
 *
 * 	}
 *
 * }</pre>
 *
 * @author Phillip Webb
 * @since 1.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnClassCondition.class)
public @interface ConditionalOnClass {

	/**
	 * 必须存在的类。
	 * <p>
	 * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，
	 * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。
	 * 若要将该注解作为元注解使用，请仅使用 {@link #name} 属性。
	 * @return 必须存在的类
	 */
	Class<?>[] value() default {};

	/**
	 * 必须存在的类名。
	 * @return 必须存在的类名
	 */
	String[] name() default {};

}
