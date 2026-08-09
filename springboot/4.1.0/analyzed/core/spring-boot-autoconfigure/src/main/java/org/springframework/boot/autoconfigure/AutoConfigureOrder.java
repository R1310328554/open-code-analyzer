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

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Spring Framework {@link Order @Order} 注解的自动配置专用变体。
 * 允许自动配置类在彼此之间排序，而不影响传递给
 * {@link AnnotationConfigApplicationContext#register(Class...)} 的配置类顺序。
 * <p>
 * 与标准 {@link Configuration @Configuration} 类一样，自动配置类的应用顺序
 * 仅影响其 bean 的定义顺序；这些 bean 的后续创建顺序不受影响，
 * 由各自的依赖关系及 {@link DependsOn @DependsOn} 关系决定。
 *
 * @author Andy Wilkinson
 * @since 1.3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Documented
public @interface AutoConfigureOrder {

	/**
	 * 默认排序值。
	 */
	int DEFAULT_ORDER = 0;

	/**
	 * 排序值，默认为 {@code 0}。
	 * @see Ordered#getOrder()
	 * @return 排序值
	 */
	int value() default DEFAULT_ORDER;

}
