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

import jakarta.servlet.Filter;
import jakarta.servlet.ServletContext;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 在 Servlet 3.0+ 容器中注册 {@link Filter} 的 {@link ServletContextInitializer}。
 * 功能类似 {@link ServletContext} 提供的 {@link ServletContext#addFilter(String, Filter) 注册}能力，
 * 但采用对 Spring Bean 更友好的设计。
 * <p>
 * 调用 {@link #onStartup(ServletContext)} 前必须先通过 {@link #setFilter(Filter) setFilter} 指定过滤器。
 * 注册可与 {@link #setUrlPatterns URL 模式} 和/或 Servlet 关联
 *（通过 {@link #setServletNames 名称} 或 {@link #setServletRegistrationBeans ServletRegistrationBean}）。
 * 未指定 URL 模式或 Servlet 时，过滤器将关联到 {@code /*}。
 * 未指定名称时将自动推断过滤器名称。
 *
 * @param <T> the type of {@link Filter} to register 待注册的 {@link Filter} 类型
 * @author Phillip Webb
 * @since 1.4.0
 * @see ServletContextInitializer
 * @see ServletContext#addFilter(String, Filter)
 * @see DelegatingFilterProxyRegistrationBean
 * @see FilterRegistration
 */
public class FilterRegistrationBean<T extends Filter> extends AbstractFilterRegistrationBean<T> {

	private @Nullable T filter;

	/**
	 * 创建新的 {@link FilterRegistrationBean} 实例。
	 */
	public FilterRegistrationBean() {
	}

	/**
	 * 创建新的 {@link FilterRegistrationBean} 实例，并与指定 {@link ServletRegistrationBean} 关联注册。
	 *
	 * @param filter the filter to register 待注册的过滤器
	 * @param servletRegistrationBeans associate {@link ServletRegistrationBean}s 关联的 {@link ServletRegistrationBean}
	 */
	public FilterRegistrationBean(T filter, ServletRegistrationBean<?>... servletRegistrationBeans) {
		super(servletRegistrationBeans);
		Assert.notNull(filter, "'filter' must not be null");
		this.filter = filter;
	}

	@Override
	public @Nullable T getFilter() {
		return this.filter;
	}

	/**
	 * 设置待注册的过滤器。
	 *
	 * @param filter the filter 过滤器实例
	 */
	public void setFilter(T filter) {
		Assert.notNull(filter, "'filter' must not be null");
		this.filter = filter;
	}

}
