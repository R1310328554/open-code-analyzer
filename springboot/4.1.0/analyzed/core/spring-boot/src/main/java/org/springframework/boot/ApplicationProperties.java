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

package org.springframework.boot;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.context.properties.bind.BindableRuntimeHintsRegistrar;
import org.springframework.boot.logging.LoggingSystemProperty;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * Spring 应用属性。
 *
 * @author Moritz Halbritter
 */
class ApplicationProperties {

	/**
	 * 是否允许 Bean 定义覆盖（注册与现有定义同名的定义）。
	 */
	private boolean allowBeanDefinitionOverriding;

	/**
	 * 是否允许 Bean 之间的循环引用并自动尝试解析。
	 */
	private boolean allowCircularReferences;

	/**
	 * 应用运行时显示 Banner 的模式。
	 */
	private Banner.@Nullable Mode bannerMode;

	/**
	 * 即使没有更多非守护线程时是否仍保持应用存活。
	 */
	private boolean keepAlive;

	/**
	 * 是否应延迟执行初始化。
	 */
	private boolean lazyInitialization;

	/**
	 * 应用启动时是否记录应用信息。
	 */
	private boolean logStartupInfo = true;

	/**
	 * 应用是否应注册关闭钩子。
	 */
	private boolean registerShutdownHook = true;

	/**
	 * 要包含在 ApplicationContext 中的源（类名、包名或 XML 资源位置）。
	 */
	private Set<String> sources = new LinkedHashSet<>();

	/**
	 * 显式请求特定 Web 应用类型的标志。若未设置，则根据类路径自动检测。
	 */
	private @Nullable WebApplicationType webApplicationType;

	boolean isAllowBeanDefinitionOverriding() {
		return this.allowBeanDefinitionOverriding;
	}

	void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	boolean isAllowCircularReferences() {
		return this.allowCircularReferences;
	}

	void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}

	Mode getBannerMode(Environment environment) {
		if (this.bannerMode != null) {
			return this.bannerMode;
		}
		String applicationPropertyName = LoggingSystemProperty.CONSOLE_STRUCTURED_FORMAT.getApplicationPropertyName();
		Assert.state(applicationPropertyName != null, "applicationPropertyName must not be null");
		boolean structuredLoggingEnabled = environment.containsProperty(applicationPropertyName);
		return (structuredLoggingEnabled) ? Mode.OFF : Banner.Mode.CONSOLE;
	}

	void setBannerMode(@Nullable Mode bannerMode) {
		this.bannerMode = bannerMode;
	}

	boolean isKeepAlive() {
		return this.keepAlive;
	}

	void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	boolean isLazyInitialization() {
		return this.lazyInitialization;
	}

	void setLazyInitialization(boolean lazyInitialization) {
		this.lazyInitialization = lazyInitialization;
	}

	boolean isLogStartupInfo() {
		return this.logStartupInfo;
	}

	void setLogStartupInfo(boolean logStartupInfo) {
		this.logStartupInfo = logStartupInfo;
	}

	boolean isRegisterShutdownHook() {
		return this.registerShutdownHook;
	}

	void setRegisterShutdownHook(boolean registerShutdownHook) {
		this.registerShutdownHook = registerShutdownHook;
	}

	Set<String> getSources() {
		return this.sources;
	}

	void setSources(Set<String> sources) {
		this.sources = new LinkedHashSet<>(sources);
	}

	@Nullable WebApplicationType getWebApplicationType() {
		return this.webApplicationType;
	}

	void setWebApplicationType(@Nullable WebApplicationType webApplicationType) {
		this.webApplicationType = webApplicationType;
	}

	static class ApplicationPropertiesRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
			BindableRuntimeHintsRegistrar.forTypes(ApplicationProperties.class).registerHints(hints, classLoader);
		}

	}

}
