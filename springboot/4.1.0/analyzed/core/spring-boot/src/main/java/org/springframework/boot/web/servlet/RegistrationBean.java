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

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

/**
 * 基于 Servlet 3.0+ 的注册 Bean 抽象基类。
 * 实现 {@link ServletContextInitializer} 与 {@link Ordered}，
 * 在 {@link #onStartup(ServletContext)} 中按启用状态与顺序执行注册。
 *
 * @author Phillip Webb
 * @since 1.4.0
 * @see ServletRegistrationBean
 * @see FilterRegistrationBean
 * @see DelegatingFilterProxyRegistrationBean
 * @see ServletListenerRegistrationBean
 */
public abstract class RegistrationBean implements ServletContextInitializer, Ordered {

	private static final Log logger = LogFactory.getLog(RegistrationBean.class);

	private int order = Ordered.LOWEST_PRECEDENCE;

	private boolean enabled = true;

	@Override
	public final void onStartup(ServletContext servletContext) throws ServletException {
		String description = getDescription();
		if (!isEnabled()) {
			logger.info(StringUtils.capitalize(description) + " was not registered (disabled)");
			return;
		}
		register(description, servletContext);
	}

	/**
	 * 返回注册的描述信息，例如 "Servlet resourceServlet"。
	 *
	 * @return a description of the registration 注册描述
	 */
	protected abstract String getDescription();

	/**
	 * 将此 Bean 注册到 Servlet 上下文。
	 *
	 * @param description a description of the item being registered 待注册项的描述
	 * @param servletContext the servlet context Servlet 上下文
	 */
	protected abstract void register(String description, ServletContext servletContext);

	/**
	 * 设置注册是否启用。
	 *
	 * @param enabled the enabled to set 是否启用
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * 返回注册是否启用。
	 *
	 * @return if enabled (default {@code true}) 是否启用（默认 {@code true}）
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * 设置注册 Bean 的执行顺序。
	 *
	 * @param order the order 顺序值
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * 获取注册 Bean 的执行顺序。
	 *
	 * @return the order 顺序值
	 */
	@Override
	public int getOrder() {
		return this.order;
	}

}
