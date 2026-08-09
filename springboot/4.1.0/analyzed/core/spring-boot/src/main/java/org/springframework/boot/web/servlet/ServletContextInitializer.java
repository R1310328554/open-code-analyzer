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

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.WebApplicationInitializer;

/**
 * 以编程方式配置 Servlet 3.0+ {@link ServletContext context} 的接口。
 * 与 {@link WebApplicationInitializer} 不同，仅实现本接口（且未实现 {@link WebApplicationInitializer}）的类
 * <b>不会</b>被 {@link SpringServletContainerInitializer} 检测到，
 * 因此不会被 Servlet 容器自动引导。
 * <p>
 * 本接口的设计意图类似 {@link ServletContainerInitializer}，
 * 但生命周期由 Spring 管理而非 Servlet 容器。
 * <p>
 * 配置示例参见 {@link WebApplicationInitializer}。
 *
 * @author Phillip Webb
 * @since 4.0.0
 * @see WebApplicationInitializer
 */
@FunctionalInterface
public interface ServletContextInitializer {

	/**
	 * 为给定 {@link ServletContext} 配置初始化所需的 Servlet、过滤器、监听器、
	 * 上下文参数及属性。
	 *
	 * @param servletContext the {@code ServletContext} to initialize 待初始化的 {@code ServletContext}
	 * @throws ServletException if any call against the given {@code ServletContext}
	 * throws a {@code ServletException} 对给定 {@code ServletContext} 的调用抛出 {@code ServletException} 时
	 */
	void onStartup(ServletContext servletContext) throws ServletException;

}
