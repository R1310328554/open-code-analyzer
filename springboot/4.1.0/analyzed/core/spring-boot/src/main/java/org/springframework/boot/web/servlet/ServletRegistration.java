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

import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebInitParam;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;

/**
 * 在 Servlet 3.0+ 容器中注册 {@link Servlet}。
 * 可作为 {@link ServletRegistrationBean} 的基于注解替代方案。
 *
 * @author Moritz Halbritter
 * @author Dmytro Danilenkov
 * @since 3.5.0
 * @see ServletRegistrationBean
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Order
public @interface ServletRegistration {

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
	 * 是否忽略注册失败。为 {@code true} 时仅记录日志；
	 * 为 {@code false} 时抛出 {@link IllegalStateException}。
	 *
	 * @return whether registration failures should be ignored 是否忽略注册失败
	 */
	boolean ignoreRegistrationFailure() default false;

	/**
	 * Servlet 的 URL 映射。未指定时默认为 {@code /}。
	 *
	 * @return the url mappings URL 映射数组
	 */
	String[] urlMappings() default {};

	/**
	 * {@code loadOnStartup} 优先级。
	 * 详见 {@link jakarta.servlet.ServletRegistration.Dynamic#setLoadOnStartup}。
	 *
	 * @return the {@code loadOnStartup} priority {@code loadOnStartup} 优先级
	 */
	int loadOnStartup() default -1;

	/**
	 * Servlet 使用的初始化参数。
	 *
	 * @return the init parameters 初始化参数
	 */
	WebInitParam[] initParameters() default {};

	/**
	 * 多部分（multipart）上传配置。
	 *
	 * @return the multipart configuration 多部分配置
	 */
	MultipartConfig multipartConfig() default @MultipartConfig;

}
