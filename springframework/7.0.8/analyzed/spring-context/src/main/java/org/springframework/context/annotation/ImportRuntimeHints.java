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

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * 指示应处理一个或多个 {@link RuntimeHintsRegistrar} 实现。
 *
 * <p>与通过 {@code META-INF/spring/aot.factories} 声明 {@link RuntimeHintsRegistrar} 不同，
 * 本注解允许更灵活的注册：仅当带注解的组件或 Bean 方法实际在 Bean 工厂中注册时才处理。
 * 以下示例说明了此行为：
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyConfiguration {
 *
 *     &#064;Bean
 *     &#064;ImportRuntimeHints(MyHints.class)
 *     &#064;Conditional(MyCondition.class)
 *     public MyService myService() {
 *         return new MyService();
 *     }
 * }</pre>
 *
 * <p>若上述配置类被处理，仅当 {@code MyCondition} 匹配时才会贡献 {@code MyHints}。
 * 若条件不匹配，{@code MyService} 不会定义为 Bean，hints 也不会被处理。
 *
 * <p>{@code @ImportRuntimeHints} 也可应用于使用<em>Spring TestContext Framework</em>
 * 加载 {@code ApplicationContext} 的任意测试类。
 *
 * <p>若多个组件或测试类引用同一 {@link RuntimeHintsRegistrar} 实现，
 * 对于给定的 Bean 工厂处理或测试套件，该注册器仅调用一次。
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @since 6.0
 * @see RuntimeHints
 * @see ReflectiveScan @ReflectiveScan
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ImportRuntimeHints {

	/**
	 * 要处理的 {@link RuntimeHintsRegistrar} 实现。
	 */
	Class<? extends RuntimeHintsRegistrar>[] value();

}
