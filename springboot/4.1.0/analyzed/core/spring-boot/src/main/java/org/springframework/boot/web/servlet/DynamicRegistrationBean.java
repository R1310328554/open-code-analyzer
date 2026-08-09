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

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.Registration;
import jakarta.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 基于 Servlet 3.0+ {@link jakarta.servlet.Registration.Dynamic 动态} 注册的 Bean 基类。
 *
 * @param <D> the dynamic registration result 动态注册结果类型
 * @author Phillip Webb
 * @author Moritz Halbritter
 * @since 2.0.0
 */
public abstract class DynamicRegistrationBean<D extends Registration.Dynamic> extends RegistrationBean
		implements BeanNameAware {

	private static final Log logger = LogFactory.getLog(RegistrationBean.class);

	private @Nullable String name;

	private boolean asyncSupported = true;

	private Map<String, String> initParameters = new LinkedHashMap<>();

	private @Nullable String beanName;

	private boolean ignoreRegistrationFailure;

	/**
	 * 设置此注册的名称。若未指定则使用 Bean 名称。
	 *
	 * @param name the name of the registration 注册名称
	 */
	public void setName(String name) {
		Assert.hasLength(name, "'name' must not be empty");
		this.name = name;
	}

	/**
	 * 设置此注册是否支持异步操作。未指定时默认为 {@code true}。
	 *
	 * @param asyncSupported if async is supported 是否支持异步
	 */
	public void setAsyncSupported(boolean asyncSupported) {
		this.asyncSupported = asyncSupported;
	}

	/**
	 * 返回此注册是否支持异步操作。
	 *
	 * @return if async is supported 是否支持异步
	 */
	public boolean isAsyncSupported() {
		return this.asyncSupported;
	}

	/**
	 * 设置此注册的 init 参数。调用此方法会替换现有 init 参数。
	 *
	 * @param initParameters the init parameters init 参数
	 * @see #getInitParameters
	 * @see #addInitParameter
	 */
	public void setInitParameters(Map<String, String> initParameters) {
		Assert.notNull(initParameters, "'initParameters' must not be null");
		this.initParameters = new LinkedHashMap<>(initParameters);
	}

	/**
	 * 返回注册 init 参数的可变 Map。
	 *
	 * @return the init parameters init 参数
	 */
	public Map<String, String> getInitParameters() {
		return this.initParameters;
	}

	/**
	 * 添加单个 init 参数，替换同名现有参数。
	 *
	 * @param name the init-parameter name init 参数名
	 * @param value the init-parameter value init 参数值
	 */
	public void addInitParameter(String name, String value) {
		Assert.notNull(name, "'name' must not be null");
		this.initParameters.put(name, value);
	}

	@Override
	protected final void register(String description, ServletContext servletContext) {
		D registration = addRegistration(description, servletContext);
		if (registration == null) {
			if (this.ignoreRegistrationFailure) {
				logger.info(StringUtils.capitalize(description) + " was not registered (possibly already registered?)");
				return;
			}
			throw new IllegalStateException(
					"Failed to register '%s' on the servlet context. Possibly already registered?"
						.formatted(description));
		}
		configure(registration);
	}

	/**
	 * 设置是否忽略注册失败。为 true 时记录日志；为 false 时抛出 {@link IllegalStateException}。
	 *
	 * @param ignoreRegistrationFailure whether to ignore registration failures 是否忽略注册失败
	 * @since 3.1.0
	 */
	public void setIgnoreRegistrationFailure(boolean ignoreRegistrationFailure) {
		this.ignoreRegistrationFailure = ignoreRegistrationFailure;
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	protected abstract @Nullable D addRegistration(String description, ServletContext servletContext);

	protected void configure(D registration) {
		registration.setAsyncSupported(this.asyncSupported);
		if (!this.initParameters.isEmpty()) {
			registration.setInitParameters(this.initParameters);
		}
	}

	/**
	 * 推断此注册的名称。返回用户指定名称或回退到 Bean 名称；
	 * 若 Bean 名称不可用则使用基于约定的命名。
	 *
	 * @param value the object used for convention based names 用于约定命名的对象
	 * @return the deduced name 推断出的名称
	 */
	protected final String getOrDeduceName(@Nullable Object value) {
		if (this.name != null) {
			return this.name;
		}
		if (this.beanName != null) {
			return this.beanName;
		}
		if (value == null) {
			return "null";
		}
		return Conventions.getVariableName(value);
	}

}
