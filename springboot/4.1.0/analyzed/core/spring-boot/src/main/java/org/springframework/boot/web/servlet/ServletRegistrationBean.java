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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 在 Servlet 3.0+ 容器中注册 {@link Servlet} 的 {@link ServletContextInitializer}。
 * 功能类似 {@link ServletContext} 提供的 {@link ServletContext#addServlet(String, Servlet)
 * 注册} 能力，但采用对 Spring Bean 更友好的设计。
 * <p>
 * 调用 {@link #onStartup} 前必须先通过 {@link #setServlet(Servlet) setServlet} 指定 Servlet。
 * 可通过 {@link #setUrlMappings} 配置 URL 映射；若省略映射则默认映射到 {@code /*}
 * （除非 {@link #ServletRegistrationBean(Servlet, boolean, String...) alwaysMapUrl}
 * 设为 {@code false}）。未指定名称时会自动推断 Servlet 名称。
 *
 * @param <T> the type of the {@link Servlet} to register 待注册 {@link Servlet} 的类型
 * @author Phillip Webb
 * @since 1.4.0
 * @see ServletContextInitializer
 * @see ServletContext#addServlet(String, Servlet)
 * @see org.springframework.boot.web.servlet.ServletRegistration
 */
public class ServletRegistrationBean<T extends Servlet> extends DynamicRegistrationBean<ServletRegistration.Dynamic> {

	private static final String[] DEFAULT_MAPPINGS = { "/*" };

	private @Nullable T servlet;

	private Set<String> urlMappings = new LinkedHashSet<>();

	private boolean alwaysMapUrl = true;

	private int loadOnStartup = -1;

	private @Nullable MultipartConfigElement multipartConfig;

	/**
	 * 创建新的 {@link ServletRegistrationBean} 实例。
	 */
	public ServletRegistrationBean() {
	}

	/**
	 * 使用指定 {@link Servlet} 与 URL 映射创建新的 {@link ServletRegistrationBean} 实例。
	 *
	 * @param servlet the servlet being mapped 待映射的 Servlet
	 * @param urlMappings the URLs being mapped 待映射的 URL
	 */
	public ServletRegistrationBean(T servlet, String... urlMappings) {
		this(servlet, true, urlMappings);
	}

	/**
	 * 使用指定 {@link Servlet} 与 URL 映射创建新的 {@link ServletRegistrationBean} 实例。
	 *
	 * @param servlet the servlet being mapped 待映射的 Servlet
	 * @param alwaysMapUrl if omitted URL mappings should be replaced with '/*' 省略 URL 映射时是否替换为 {@code /*}
	 * @param urlMappings the URLs being mapped 待映射的 URL
	 */
	public ServletRegistrationBean(T servlet, boolean alwaysMapUrl, String... urlMappings) {
		Assert.notNull(servlet, "'servlet' must not be null");
		Assert.notNull(urlMappings, "'urlMappings' must not be null");
		this.servlet = servlet;
		this.alwaysMapUrl = alwaysMapUrl;
		this.urlMappings.addAll(Arrays.asList(urlMappings));
	}

	/**
	 * 设置待注册的 Servlet。
	 *
	 * @param servlet the servlet 待注册的 Servlet
	 */
	public void setServlet(T servlet) {
		Assert.notNull(servlet, "'servlet' must not be null");
		this.servlet = servlet;
	}

	/**
	 * 返回待注册的 Servlet。
	 *
	 * @return the servlet 待注册的 Servlet
	 */
	public @Nullable T getServlet() {
		return this.servlet;
	}

	/**
	 * 设置 Servlet 的 URL 映射。未指定时默认映射到 {@code /}。
	 * 调用此方法会替换此前已指定的所有映射。
	 *
	 * @param urlMappings the mappings to set 待设置的映射
	 * @see #addUrlMappings(String...)
	 */
	public void setUrlMappings(Collection<String> urlMappings) {
		Assert.notNull(urlMappings, "'urlMappings' must not be null");
		this.urlMappings = new LinkedHashSet<>(urlMappings);
	}

	/**
	 * 返回 Servlet 规范定义的、用于此 Servlet 的可变 URL 映射集合。
	 *
	 * @return the urlMappings URL 映射
	 */
	public Collection<String> getUrlMappings() {
		return this.urlMappings;
	}

	/**
	 * 按 Servlet 规范为 Servlet 追加 URL 映射。
	 *
	 * @param urlMappings the mappings to add 待追加的映射
	 * @see #setUrlMappings(Collection)
	 */
	public void addUrlMappings(String... urlMappings) {
		Assert.notNull(urlMappings, "'urlMappings' must not be null");
		this.urlMappings.addAll(Arrays.asList(urlMappings));
	}

	/**
	 * 设置 {@code loadOnStartup} 优先级。详见
	 * {@link ServletRegistration.Dynamic#setLoadOnStartup}。
	 *
	 * @param loadOnStartup if load on startup is enabled 是否启用启动时加载
	 */
	public void setLoadOnStartup(int loadOnStartup) {
		this.loadOnStartup = loadOnStartup;
	}

	/**
	 * 设置 {@link MultipartConfigElement 多部分（multipart）配置}。
	 *
	 * @param multipartConfig the multipart configuration to set or {@code null} 待设置的多部分配置，或 {@code null}
	 */
	public void setMultipartConfig(@Nullable MultipartConfigElement multipartConfig) {
		this.multipartConfig = multipartConfig;
	}

	/**
	 * 返回将要应用的 {@link MultipartConfigElement 多部分配置}，或 {@code null}。
	 *
	 * @return the multipart config 多部分配置
	 */
	public @Nullable MultipartConfigElement getMultipartConfig() {
		return this.multipartConfig;
	}

	@Override
	protected String getDescription() {
		Assert.state(this.servlet != null, "Unable to return description for null servlet");
		return "servlet " + getServletName();
	}

	@Override
	protected ServletRegistration.Dynamic addRegistration(String description, ServletContext servletContext) {
		String name = getServletName();
		return servletContext.addServlet(name, this.servlet);
	}

	/**
	 * 配置注册设置。子类可按需覆盖此方法以执行额外配置。
	 *
	 * @param registration the registration 注册对象
	 */
	@Override
	protected void configure(ServletRegistration.Dynamic registration) {
		super.configure(registration);
		String[] urlMapping = StringUtils.toStringArray(this.urlMappings);
		if (urlMapping.length == 0 && this.alwaysMapUrl) {
			urlMapping = DEFAULT_MAPPINGS;
		}
		if (!ObjectUtils.isEmpty(urlMapping)) {
			registration.addMapping(urlMapping);
		}
		registration.setLoadOnStartup(this.loadOnStartup);
		if (this.multipartConfig != null) {
			registration.setMultipartConfig(this.multipartConfig);
		}
	}

	/**
	 * 返回将要注册的 Servlet 名称。
	 *
	 * @return the servlet name Servlet 名称
	 */
	public String getServletName() {
		return getOrDeduceName(this.servlet);
	}

	@Override
	public String toString() {
		return getServletName() + " urls=" + getUrlMappings();
	}

}
