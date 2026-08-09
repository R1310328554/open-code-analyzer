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

package org.springframework.boot.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 提示某个 {@link EnableAutoConfiguration 自动配置} 应在其他指定自动配置类之前应用。
 * <p>
 * 与标准 {@link Configuration @Configuration} 类一样，自动配置类的应用顺序
 * 仅影响其 bean 的定义顺序；这些 bean 的后续创建顺序不受影响，
 * 由各自的依赖关系及 {@link DependsOn @DependsOn} 关系决定。
 *
 * @author Phillip Webb
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface AutoConfigureBefore {

	/**
	 * 尚未应用的自动配置类。
	 * <p>
	 * 由于本注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，
	 * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合/元注解使用。
	 * 若要将本注解用作元注解，请仅使用 {@link #name} 属性。
	 * @return 自动配置类
	 */
	Class<?>[] value() default {};

	/**
	 * 尚未应用的自动配置类名称。
	 * 若自动配置类不是顶层类，类名应使用 {@code $} 分隔外部类，
	 * 例如 {@code com.example.Outer$NestedAutoConfiguration}。
	 * @return 类名
	 * @since 1.2.2
	 */
	String[] name() default {};

}
