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
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.FilterRegistration.Dynamic;
import jakarta.servlet.ServletContext;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 在 Servlet 3.0+ 容器中注册 {@link Filter} 的抽象基类 {@link ServletContextInitializer}。
 *
 * @param <T> the type of {@link Filter} to register 待注册的 {@link Filter} 类型
 * @author Phillip Webb
 * @author Brian Clozel
 * @since 1.5.22
 */
public abstract class AbstractFilterRegistrationBean<T extends Filter> extends DynamicRegistrationBean<Dynamic> {

	private static final String[] DEFAULT_URL_MAPPINGS = { "/*" };

	private Set<ServletRegistrationBean<?>> servletRegistrationBeans = new LinkedHashSet<>();

	private Set<String> servletNames = new LinkedHashSet<>();

	private Set<String> urlPatterns = new LinkedHashSet<>();

	private @Nullable EnumSet<DispatcherType> dispatcherTypes;

	private boolean matchAfter;

	/**
	 * 创建新实例，与指定 {@link ServletRegistrationBean} 关联注册。
	 *
	 * @param servletRegistrationBeans associate {@link ServletRegistrationBean}s 关联的 {@link ServletRegistrationBean}
	 */
	AbstractFilterRegistrationBean(ServletRegistrationBean<?>... servletRegistrationBeans) {
		Assert.notNull(servletRegistrationBeans, "'servletRegistrationBeans' must not be null");
		Collections.addAll(this.servletRegistrationBeans, servletRegistrationBeans);
	}

	/**
	 * 设置过滤器将注册到的 {@link ServletRegistrationBean}。
	 *
	 * @param servletRegistrationBeans the Servlet registration beans Servlet 注册 Bean
	 */
	public void setServletRegistrationBeans(Collection<? extends ServletRegistrationBean<?>> servletRegistrationBeans) {
		Assert.notNull(servletRegistrationBeans, "'servletRegistrationBeans' must not be null");
		this.servletRegistrationBeans = new LinkedHashSet<>(servletRegistrationBeans);
	}

	/**
	 * 返回过滤器将注册到的 {@link ServletRegistrationBean} 可变集合。
	 *
	 * @return the Servlet registration beans Servlet 注册 Bean 集合
	 * @see #setServletNames
	 * @see #setUrlPatterns
	 */
	public Collection<ServletRegistrationBean<?>> getServletRegistrationBeans() {
		return this.servletRegistrationBeans;
	}

	/**
	 * 为过滤器添加 {@link ServletRegistrationBean}。
	 *
	 * @param servletRegistrationBeans the servlet registration beans to add 待添加的 Servlet 注册 Bean
	 * @see #setServletRegistrationBeans
	 */
	public void addServletRegistrationBeans(ServletRegistrationBean<?>... servletRegistrationBeans) {
		Assert.notNull(servletRegistrationBeans, "'servletRegistrationBeans' must not be null");
		Collections.addAll(this.servletRegistrationBeans, servletRegistrationBeans);
	}

	/**
	 * 设置过滤器将注册到的 Servlet 名称，会替换先前指定的 Servlet 名称。
	 *
	 * @param servletNames the servlet names Servlet 名称
	 * @see #setServletRegistrationBeans
	 * @see #setUrlPatterns
	 */
	public void setServletNames(Collection<String> servletNames) {
		Assert.notNull(servletNames, "'servletNames' must not be null");
		this.servletNames = new LinkedHashSet<>(servletNames);
	}

	/**
	 * 返回过滤器将注册到的 Servlet 名称可变集合。
	 *
	 * @return the servlet names Servlet 名称
	 */
	public Collection<String> getServletNames() {
		return this.servletNames;
	}

	/**
	 * 为过滤器添加 Servlet 名称。
	 *
	 * @param servletNames the servlet names to add 待添加的 Servlet 名称
	 */
	public void addServletNames(String... servletNames) {
		Assert.notNull(servletNames, "'servletNames' must not be null");
		this.servletNames.addAll(Arrays.asList(servletNames));
	}

	/**
	 * 设置过滤器将注册到的 URL 模式，会替换先前指定的 URL 模式。
	 *
	 * @param urlPatterns the URL patterns URL 模式
	 * @see #setServletRegistrationBeans
	 * @see #setServletNames
	 */
	public void setUrlPatterns(Collection<String> urlPatterns) {
		Assert.notNull(urlPatterns, "'urlPatterns' must not be null");
		this.urlPatterns = new LinkedHashSet<>(urlPatterns);
	}

	/**
	 * 返回过滤器将注册到的 URL 模式可变集合（按 Servlet 规范定义）。
	 *
	 * @return the URL patterns URL 模式
	 */
	public Collection<String> getUrlPatterns() {
		return this.urlPatterns;
	}

	/**
	 * 添加过滤器将注册到的 URL 模式（按 Servlet 规范定义）。
	 *
	 * @param urlPatterns the URL patterns URL 模式
	 */
	public void addUrlPatterns(String... urlPatterns) {
		Assert.notNull(urlPatterns, "'urlPatterns' must not be null");
		Collections.addAll(this.urlPatterns, urlPatterns);
	}

	/**
	 * 确定过滤器应注册的 {@link DispatcherType 分发类型}。
	 * 若未配置则根据所注册过滤器类型应用默认值。对返回的 {@link EnumSet} 的修改不会影响注册。
	 *
	 * @return the dispatcher types, never {@code null} 分发类型，永不为 {@code null}
	 * @since 3.2.0
	 */
	public EnumSet<DispatcherType> determineDispatcherTypes() {
		if (CollectionUtils.isEmpty(this.dispatcherTypes)) {
			T filter = getFilter();
			Assert.state(filter != null, "'filter' must not be null");
			if (ClassUtils.isPresent("org.springframework.web.filter.OncePerRequestFilter",
					filter.getClass().getClassLoader()) && filter instanceof OncePerRequestFilter) {
				return EnumSet.allOf(DispatcherType.class);
			}
			else {
				return EnumSet.of(DispatcherType.REQUEST);
			}
		}
		return EnumSet.copyOf(this.dispatcherTypes);
	}

	/**
	 * 使用指定元素 {@link #setDispatcherTypes(EnumSet) 设置分发类型} 的便捷方法。
	 *
	 * @param first the first dispatcher type 第一个分发类型
	 * @param rest additional dispatcher types 其余分发类型
	 */
	public void setDispatcherTypes(DispatcherType first, DispatcherType... rest) {
		this.dispatcherTypes = EnumSet.of(first, rest);
	}

	/**
	 * 设置注册应使用的分发类型。
	 *
	 * @param dispatcherTypes the dispatcher types 分发类型
	 */
	public void setDispatcherTypes(@Nullable EnumSet<DispatcherType> dispatcherTypes) {
		this.dispatcherTypes = dispatcherTypes;
	}

	/**
	 * 设置过滤器映射是否应在 ServletContext 已声明的过滤器映射之后匹配。
	 * 默认为 {@code false}，表示过滤器应在 ServletContext 已声明的过滤器映射之前匹配。
	 *
	 * @param matchAfter if filter mappings are matched after 是否在之后匹配
	 */
	public void setMatchAfter(boolean matchAfter) {
		this.matchAfter = matchAfter;
	}

	/**
	 * 返回过滤器映射是否应在 ServletContext 已声明的 Filter 映射之后匹配。
	 *
	 * @return if filter mappings are matched after 是否在之后匹配
	 */
	public boolean isMatchAfter() {
		return this.matchAfter;
	}

	@Override
	protected String getDescription() {
		Filter filter = getFilter();
		Assert.notNull(filter, "'filter' must not be null");
		return "filter " + getOrDeduceName(filter);
	}

	@Override
	protected Dynamic addRegistration(String description, ServletContext servletContext) {
		Filter filter = getFilter();
		return servletContext.addFilter(getOrDeduceName(filter), filter);
	}

	/**
	 * 配置注册设置。子类可按需覆盖此方法以执行额外配置。
	 *
	 * @param registration the registration 注册对象
	 */
	@Override
	protected void configure(FilterRegistration.Dynamic registration) {
		super.configure(registration);
		EnumSet<DispatcherType> dispatcherTypes = determineDispatcherTypes();
		Set<String> servletNames = new LinkedHashSet<>();
		for (ServletRegistrationBean<?> servletRegistrationBean : this.servletRegistrationBeans) {
			servletNames.add(servletRegistrationBean.getServletName());
		}
		servletNames.addAll(this.servletNames);
		if (servletNames.isEmpty() && this.urlPatterns.isEmpty()) {
			registration.addMappingForUrlPatterns(dispatcherTypes, this.matchAfter, DEFAULT_URL_MAPPINGS);
		}
		else {
			if (!servletNames.isEmpty()) {
				registration.addMappingForServletNames(dispatcherTypes, this.matchAfter,
						StringUtils.toStringArray(servletNames));
			}
			if (!this.urlPatterns.isEmpty()) {
				registration.addMappingForUrlPatterns(dispatcherTypes, this.matchAfter,
						StringUtils.toStringArray(this.urlPatterns));
			}
		}
	}

	/**
	 * 返回待注册的 {@link Filter}。
	 *
	 * @return the filter 过滤器
	 */
	public abstract @Nullable T getFilter();

	/**
	 * 返回将要注册的过滤器名称。
	 *
	 * @return the filter name 过滤器名称
	 * @since 3.2.0
	 */
	public String getFilterName() {
		return getOrDeduceName(getFilter());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getOrDeduceName(this));
		if (this.servletNames.isEmpty() && this.urlPatterns.isEmpty()) {
			builder.append(" urls=").append(Arrays.toString(DEFAULT_URL_MAPPINGS));
		}
		else {
			if (!this.servletNames.isEmpty()) {
				builder.append(" servlets=").append(this.servletNames);
			}
			if (!this.urlPatterns.isEmpty()) {
				builder.append(" urls=").append(this.urlPatterns);
			}
		}
		builder.append(" order=").append(getOrder());
		return builder.toString();
	}

}
