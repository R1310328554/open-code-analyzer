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
import jakarta.servlet.ServletException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * 在 Servlet 3.0+ 容器中注册 {@link DelegatingFilterProxy} 的 {@link ServletContextInitializer}。
 * 类似 {@link ServletContext} 提供的 {@link ServletContext#addFilter(String, Filter) 注册} 能力，
 * 但采用对 Spring Bean 更友好的设计。
 * <p>
 * 实际委托 {@link Filter} 的 Bean 名称应通过 {@code targetBeanName} 构造参数指定。
 * 与 {@link FilterRegistrationBean} 不同，引用的过滤器不会提前实例化；
 * 若委托过滤器 Bean 标记为 {@code @Lazy}，则在过滤器被调用前甚至不会实例化。
 * <p>
 * 注册可关联 {@link #setUrlPatterns URL 模式} 和/或 Servlet
 *（通过 {@link #setServletNames 名称} 或 {@link #setServletRegistrationBeans ServletRegistrationBean}）。
 * 未指定 URL 模式或 Servlet 时，过滤器将关联到 '/*'。
 * 若未另行指定，targetBeanName 将用作过滤器名称。
 *
 * @author Phillip Webb
 * @since 1.4.0
 * @see ServletContextInitializer
 * @see ServletContext#addFilter(String, Filter)
 * @see FilterRegistrationBean
 * @see DelegatingFilterProxy
 */
public class DelegatingFilterProxyRegistrationBean extends AbstractFilterRegistrationBean<DelegatingFilterProxy>
		implements ApplicationContextAware {

	@SuppressWarnings("NullAway.Init")
	private ApplicationContext applicationContext;

	private final String targetBeanName;

	/**
	 * 创建新的 {@link DelegatingFilterProxyRegistrationBean} 实例，
	 * 与指定 {@link ServletRegistrationBean} 关联注册。
	 *
	 * @param targetBeanName name of the target filter bean to look up in the Spring
	 * application context (must not be {@code null}). 在 Spring 应用上下文中查找的目标过滤器 Bean 名称（不得为 {@code null}）
	 * @param servletRegistrationBeans associate {@link ServletRegistrationBean}s 关联的 {@link ServletRegistrationBean}
	 */
	public DelegatingFilterProxyRegistrationBean(String targetBeanName,
			ServletRegistrationBean<?>... servletRegistrationBeans) {
		super(servletRegistrationBeans);
		Assert.hasLength(targetBeanName, "'targetBeanName' must not be empty");
		this.targetBeanName = targetBeanName;
		setName(targetBeanName);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	protected String getTargetBeanName() {
		return this.targetBeanName;
	}

	@Override
	public DelegatingFilterProxy getFilter() {
		return new DelegatingFilterProxy(this.targetBeanName, getWebApplicationContext()) {

			@Override
			protected void initFilterBean() throws ServletException {
				// 不在 init() 时初始化过滤器 Bean
			}

		};
	}

	private WebApplicationContext getWebApplicationContext() {
		Assert.state(this.applicationContext != null, "ApplicationContext has not been injected");
		Assert.state(this.applicationContext instanceof WebApplicationContext,
				"Injected ApplicationContext is not a WebApplicationContext");
		return (WebApplicationContext) this.applicationContext;
	}

}
