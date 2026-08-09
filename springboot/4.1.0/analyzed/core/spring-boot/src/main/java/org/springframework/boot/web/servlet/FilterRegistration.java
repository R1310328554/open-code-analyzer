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

package org.springframework.boot.web.servlet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.annotation.WebInitParam;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;

/**
 * 在 Servlet 3.0+ 容器中注册 {@link Filter}。
 * 可作为 {@link FilterRegistrationBean} 的基于注解替代方案。
 *
 * @author Moritz Halbritter
 * @author Daeho Kwon
 * @since 3.5.0
 * @see FilterRegistrationBean
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Order
public @interface FilterRegistration {

	/**
	 * 此注册是否启用。
	 *
	 * @return whether this registration is enabled 是否启用此注册
	 */
	boolean enabled() default true;

	/**
	 * 注册 Bean 的执行顺序。
	 *
	 * @return the order of the registration bean 注册 Bean 的顺序值
	 */
	@AliasFor(annotation = Order.class, attribute = "value")
	int order() default Ordered.LOWEST_PRECEDENCE;

	/**
	 * 此注册的名称。未指定时将使用 Bean 名称。
	 *
	 * @return the name 注册名称
	 */
	String name() default "";

	/**
	 * 此注册是否支持异步操作。
	 *
	 * @return whether asynchronous operations are supported 是否支持异步操作
	 */
	boolean asyncSupported() default true;

	/**
	 * 与此注册配合使用的分发器类型（DispatcherType）。
	 *
	 * @return the dispatcher types 分发器类型数组
	 */
	DispatcherType[] dispatcherTypes() default {};

	/**
	 * 是否忽略注册失败。为 {@code true} 时仅记录日志；
	 * 为 {@code false} 时抛出 {@link IllegalStateException}。
	 *
	 * @return whether registration failures should be ignored 是否忽略注册失败
	 */
	boolean ignoreRegistrationFailure() default false;

	/**
	 * 过滤器使用的初始化参数。
	 *
	 * @return the init parameters 初始化参数
	 */
	WebInitParam[] initParameters() default {};

	/**
	 * 过滤器映射是否应在 ServletContext 中已声明的 Filter 映射之后匹配。
	 *
	 * @return whether the filter mappings should be matched after any declared Filter
	 * mappings of the ServletContext 是否在 ServletContext 已声明映射之后匹配
	 */
	boolean matchAfter() default false;

	/**
	 * 过滤器将注册到的 Servlet 名称。
	 *
	 * @return the servlet names Servlet 名称数组
	 */
	String[] servletNames() default {};

	/**
	 * 过滤器将注册到的 Servlet 类。
	 *
	 * @return the servlet classes Servlet 类数组
	 */
	Class<?>[] servletClasses() default {};

	/**
	 * 按 Servlet 规范定义的、过滤器将注册到的 URL 模式。
	 *
	 * @return the url patterns URL 模式数组
	 */
	String[] urlPatterns() default {};

}
