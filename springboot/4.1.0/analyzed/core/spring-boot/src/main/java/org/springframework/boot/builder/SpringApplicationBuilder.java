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

package org.springframework.boot.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.BootstrapRegistryInitializer;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 用于构建 {@link SpringApplication} 与 {@link ApplicationContext} 实例的构建器，
 * 提供流式 API 与上下文层次结构支持。上下文层次结构示例：
 *
 * <pre class="code">
 * new SpringApplicationBuilder(ParentConfig.class).child(ChildConfig.class).run(args);
 * </pre>
 *
 * 另一常见用法是设置激活的 profile 与默认属性以配置应用环境：
 *
 * <pre class="code">
 * new SpringApplicationBuilder(Application.class).profiles(&quot;server&quot;)
 * 		.properties(&quot;transport=local&quot;).run(args);
 * </pre>
 *
 * <p>
 * 若需求较简单，可考虑直接使用 SpringApplication 的静态便捷方法。
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 * @since 1.0.0
 * @see SpringApplication
 */
public class SpringApplicationBuilder {

	private final SpringApplication application;

	private volatile @Nullable ConfigurableApplicationContext context;

	private @Nullable SpringApplicationBuilder parent;

	private final AtomicBoolean running = new AtomicBoolean();

	private final Set<Class<?>> sources = new LinkedHashSet<>();

	private final Map<String, Object> defaultProperties = new LinkedHashMap<>();

	private @Nullable ConfigurableEnvironment environment;

	private Set<String> additionalProfiles = new LinkedHashSet<>();

	private boolean registerShutdownHookApplied;

	private boolean configuredAsChild;

	public SpringApplicationBuilder(Class<?>... sources) {
		this(null, sources);
	}

	public SpringApplicationBuilder(@Nullable ResourceLoader resourceLoader, Class<?>... sources) {
		this.application = createSpringApplication(resourceLoader, sources);
	}

	/**
	 * 使用给定 {@link ResourceLoader} 从指定源创建新的 {@link SpringApplication} 实例。
	 * 子类可覆盖以提供自定义 {@link SpringApplication} 子类。
	 * @param resourceLoader 资源加载器，或 {@code null}
	 * @param sources 配置源
	 * @return {@link SpringApplication} 实例
	 * @since 2.6.0
	 */
	protected SpringApplication createSpringApplication(@Nullable ResourceLoader resourceLoader, Class<?>... sources) {
		return new SpringApplication(resourceLoader, sources);
	}

	/**
	 * 访问当前应用上下文。
	 * @return 当前应用上下文（尚未运行时为 {@code null}）
	 */
	public @Nullable ConfigurableApplicationContext context() {
		return this.context;
	}

	/**
	 * 访问当前应用。
	 * @return 当前应用（永不为 null）
	 */
	public SpringApplication application() {
		return this.application;
	}

	/**
	 * 使用提供的命令行参数创建应用上下文（及指定的父上下文）。若父上下文尚未启动，
	 * 将先以相同参数运行父上下文。
	 * @param args 命令行参数
	 * @return 根据当前状态创建的应用上下文
	 */
	public ConfigurableApplicationContext run(String... args) {
		if (this.running.get()) {
			ConfigurableApplicationContext context = this.context;
			Assert.state(context != null, "No context set");
			// If already created we just return the existing context
			return context;
		}
		configureAsChildIfNecessary(args);
		if (this.running.compareAndSet(false, true)) {
			// If not already running copy the sources over and then run.
			this.context = build().run(args);
		}
		ConfigurableApplicationContext context = this.context;
		Assert.state(context != null, "No context set");
		return context;
	}

	private void configureAsChildIfNecessary(String... args) {
		if (this.parent != null && !this.configuredAsChild) {
			this.configuredAsChild = true;
			if (!this.registerShutdownHookApplied) {
				this.application.setRegisterShutdownHook(false);
			}
			initializers(new ParentContextApplicationContextInitializer(this.parent.run(args)));
		}
	}

	/**
	 * 返回已完全配置、可运行的 {@link SpringApplication}。
	 * @return 已完全配置的 {@link SpringApplication}
	 */
	public SpringApplication build() {
		return build(new String[0]);
	}

	/**
	 * 返回已完全配置、可运行的 {@link SpringApplication}。已配置的父上下文将使用
	 * 给定 {@code args} 运行。
	 * @param args 父上下文的参数
	 * @return the fully configured {@link SpringApplication}.
	 */
	public SpringApplication build(String... args) {
		configureAsChildIfNecessary(args);
		this.application.addPrimarySources(this.sources);
		return this.application;
	}

	/**
	 * 使用提供的源创建子应用。默认参数与环境会复制到子应用，其余为全新配置。
	 * @param sources 应用的配置源（Spring 配置类）
	 * @return 子应用构建器
	 */
	public SpringApplicationBuilder child(Class<?>... sources) {
		SpringApplicationBuilder child = new SpringApplicationBuilder();
		child.sources(sources);

		// Copy environment stuff from parent to child
		child.properties(this.defaultProperties)
			.environment(this.environment)
			.additionalProfiles(this.additionalProfiles);
		child.parent = this;

		// It's not possible if embedded web server are enabled to support web contexts as
		// parents because the servlets cannot be initialized at the right point in
		// lifecycle.
		web(WebApplicationType.NONE);

		// Probably not interested in multiple banners
		bannerMode(Banner.Mode.OFF);

		// Make sure sources get copied over
		this.application.addPrimarySources(this.sources);

		return child;
	}

	/**
	 * 使用提供的源添加父应用。默认参数与环境会复制到父应用，其余为全新配置。
	 * @param sources the sources for the application (Spring configuration)
	 * @return 父构建器
	 */
	public SpringApplicationBuilder parent(Class<?>... sources) {
		if (this.parent == null) {
			this.parent = new SpringApplicationBuilder(sources).web(WebApplicationType.NONE)
				.properties(this.defaultProperties)
				.environment(this.environment);
		}
		else {
			this.parent.sources(sources);
		}
		return this.parent;
	}

	private SpringApplicationBuilder runAndExtractParent(String... args) {
		if (this.context == null) {
			run(args);
		}
		if (this.parent != null) {
			return this.parent;
		}
		throw new IllegalStateException(
				"No parent defined yet (please use the other overloaded parent methods to set one)");
	}

	/**
	 * 向现有应用添加已在运行的父上下文。
	 * @param parent 父上下文
	 * @return 当前构建器（非父构建器）
	 */
	public SpringApplicationBuilder parent(ConfigurableApplicationContext parent) {
		this.parent = new SpringApplicationBuilder();
		this.parent.context = parent;
		this.parent.running.set(true);
		return this;
	}

	/**
	 * 创建兄弟应用（拥有相同父上下文）。调用此方法时，若当前应用（及其父上下文）
	 * 尚未运行，将不带参数启动。若需传入参数，请改用
	 * {@link #sibling(Class[], String...)}。
	 * @param sources the sources for the application (Spring configuration)
	 * @return 新的兄弟构建器
	 */
	public SpringApplicationBuilder sibling(Class<?>... sources) {
		return runAndExtractParent().child(sources);
	}

	/**
	 * 创建兄弟应用（拥有相同父上下文）。调用此方法时，若当前应用（及其父上下文）
	 * 尚未运行，将启动它们。
	 * @param sources the sources for the application (Spring configuration)
	 * @param args 启动当前应用及其父上下文时使用的命令行参数
	 * @return the new sibling builder
	 */
	public SpringApplicationBuilder sibling(Class<?>[] sources, String... args) {
		return runAndExtractParent(args).child(sources);
	}

	/**
	 * 显式设置用于创建应用上下文的工厂。
	 * @param factory 要使用的工厂
	 * @return 当前构建器
	 * @since 2.4.0
	 */
	public SpringApplicationBuilder contextFactory(ApplicationContextFactory factory) {
		this.application.setApplicationContextFactory(factory);
		return this;
	}

	/**
	 * 向此应用添加更多源（配置类与组件）。
	 * @param sources 要添加的源
	 * @return the current builder
	 */
	public SpringApplicationBuilder sources(Class<?>... sources) {
		this.sources.addAll(new LinkedHashSet<>(Arrays.asList(sources)));
		return this;
	}

	/**
	 * 显式指定 Web 应用类型。未设置时根据类路径自动检测。
	 * @param webApplicationType Web 应用类型
	 * @return the current builder
	 * @since 2.0.0
	 */
	public SpringApplicationBuilder web(WebApplicationType webApplicationType) {
		this.application.setWebApplicationType(webApplicationType);
		return this;
	}

	/**
	 * 是否记录启动信息。
	 * @param logStartupInfo 标志位，默认为 true
	 * @return the current builder
	 */
	public SpringApplicationBuilder logStartupInfo(boolean logStartupInfo) {
		this.application.setLogStartupInfo(logStartupInfo);
		return this;
	}

	/**
	 * 设置在未提供静态 banner 文件时用于打印 banner 的 {@link Banner} 实例。
	 * @param banner 要使用的 banner
	 * @return the current builder
	 */
	public SpringApplicationBuilder banner(Banner banner) {
		this.application.setBanner(banner);
		return this;
	}

	public SpringApplicationBuilder bannerMode(Banner.Mode bannerMode) {
		this.application.setBannerMode(bannerMode);
		return this;
	}

	/**
	 * 设置应用是否为无头模式（不实例化 AWT）。默认为 {@code true}，
	 * 以避免出现 Java 图标。
	 * @param headless 是否为无头模式
	 * @return the current builder
	 */
	public SpringApplicationBuilder headless(boolean headless) {
		this.application.setHeadless(headless);
		return this;
	}

	/**
	 * 设置创建的 {@link ApplicationContext} 是否注册关闭钩子。
	 * @param registerShutdownHook 是否注册关闭钩子
	 * @return the current builder
	 */
	public SpringApplicationBuilder registerShutdownHook(boolean registerShutdownHook) {
		this.registerShutdownHookApplied = true;
		this.application.setRegisterShutdownHook(registerShutdownHook);
		return this;
	}

	/**
	 * 固定用于锚定启动消息的主应用类。
	 * @param mainApplicationClass 要使用的类
	 * @return the current builder
	 */
	public SpringApplicationBuilder main(Class<?> mainApplicationClass) {
		this.application.setMainApplicationClass(mainApplicationClass);
		return this;
	}

	/**
	 * 是否将命令行参数添加到环境中。
	 * @param addCommandLineProperties 标志位，默认为 true
	 * @return the current builder
	 */
	public SpringApplicationBuilder addCommandLineProperties(boolean addCommandLineProperties) {
		this.application.setAddCommandLineProperties(addCommandLineProperties);
		return this;
	}

	/**
	 * 是否将 {@link ApplicationConversionService} 添加到应用上下文的
	 * {@link Environment} 中。
	 * @param addConversionService 是否添加转换服务
	 * @return the current builder
	 * @since 2.1.0
	 */
	public SpringApplicationBuilder setAddConversionService(boolean addConversionService) {
		this.application.setAddConversionService(addConversionService);
		return this;
	}

	/**
	 * 添加用于初始化 {@link BootstrapRegistry} 的
	 * {@link BootstrapRegistryInitializer} 实例。
	 * @param bootstrapRegistryInitializer 要添加的引导注册表初始化器
	 * @return the current builder
	 * @since 2.4.5
	 */
	public SpringApplicationBuilder addBootstrapRegistryInitializer(
			BootstrapRegistryInitializer bootstrapRegistryInitializer) {
		this.application.addBootstrapRegistryInitializer(bootstrapRegistryInitializer);
		return this;
	}

	/**
	 * 控制应用是否延迟初始化。
	 * @param lazyInitialization 标志位，默认为 false
	 * @return the current builder
	 * @since 2.2
	 */
	public SpringApplicationBuilder lazyInitialization(boolean lazyInitialization) {
		this.application.setLazyInitialization(lazyInitialization);
		return this;
	}

	/**
	 * 以 {@code key=value} 或 {@code key:value} 形式设置环境默认属性。
	 * 多次调用会累积，不会清除先前设置的属性。
	 * @param defaultProperties 要设置的属性
	 * @return the current builder
	 * @see SpringApplicationBuilder#properties(Properties)
	 * @see SpringApplicationBuilder#properties(Map)
	 */
	public SpringApplicationBuilder properties(String... defaultProperties) {
		return properties(getMapFromKeyValuePairs(defaultProperties));
	}

	private Map<String, Object> getMapFromKeyValuePairs(String[] properties) {
		Map<String, Object> map = new HashMap<>();
		for (String property : properties) {
			int index = lowestIndexOf(property, ":", "=");
			String key = (index > 0) ? property.substring(0, index) : property;
			String value = (index > 0) ? property.substring(index + 1) : "";
			map.put(key, value);
		}
		return map;
	}

	private int lowestIndexOf(String property, String... candidates) {
		int index = -1;
		for (String candidate : candidates) {
			int candidateIndex = property.indexOf(candidate);
			if (candidateIndex > 0) {
				index = (index != -1) ? Math.min(index, candidateIndex) : candidateIndex;
			}
		}
		return index;
	}

	/**
	 * 设置环境的默认属性。多次调用会累积，不会清除先前设置的属性。
	 * @param defaultProperties the properties to set.
	 * @return the current builder
	 * @see SpringApplicationBuilder#properties(String...)
	 * @see SpringApplicationBuilder#properties(Map)
	 */
	public SpringApplicationBuilder properties(Properties defaultProperties) {
		return properties(getMapFromProperties(defaultProperties));
	}

	private Map<String, Object> getMapFromProperties(Properties properties) {
		Map<String, Object> map = new HashMap<>();
		for (Object key : Collections.list(properties.propertyNames())) {
			map.put((String) key, properties.get(key));
		}
		return map;
	}

	/**
	 * 设置环境的默认属性。多次调用会累积，不会清除先前设置的属性。
	 * @param defaults 默认属性
	 * @return the current builder
	 * @see SpringApplicationBuilder#properties(String...)
	 * @see SpringApplicationBuilder#properties(Properties)
	 */
	public SpringApplicationBuilder properties(Map<String, Object> defaults) {
		this.defaultProperties.putAll(defaults);
		this.application.setDefaultProperties(this.defaultProperties);
		if (this.parent != null) {
			this.parent.properties(this.defaultProperties);
			this.parent.environment(this.environment);
		}
		return this;
	}

	/**
	 * 为此应用（及其父、子应用）添加激活的 Spring profile。
	 * @param profiles 要添加的 profile
	 * @return the current builder
	 */
	public SpringApplicationBuilder profiles(String... profiles) {
		this.additionalProfiles.addAll(Arrays.asList(profiles));
		this.application.setAdditionalProfiles(StringUtils.toStringArray(this.additionalProfiles));
		return this;
	}

	private SpringApplicationBuilder additionalProfiles(Collection<String> additionalProfiles) {
		this.additionalProfiles = new LinkedHashSet<>(additionalProfiles);
		this.application.setAdditionalProfiles(StringUtils.toStringArray(this.additionalProfiles));
		return this;
	}

	/**
	 * 应用上下文中自动生成 Bean 名称的生成器。
	 * @param beanNameGenerator 要设置的生成器
	 * @return the current builder
	 */
	public SpringApplicationBuilder beanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.application.setBeanNameGenerator(beanNameGenerator);
		return this;
	}

	/**
	 * 应用上下文的环境。
	 * @param environment 要设置的环境
	 * @return the current builder
	 */
	public SpringApplicationBuilder environment(@Nullable ConfigurableEnvironment environment) {
		this.application.setEnvironment(environment);
		this.environment = environment;
		return this;
	}

	/**
	 * 从系统环境获取配置属性时使用的前缀。
	 * @param environmentPrefix 要设置的环境属性前缀
	 * @return the current builder
	 * @since 2.5.0
	 */
	public SpringApplicationBuilder environmentPrefix(String environmentPrefix) {
		this.application.setEnvironmentPrefix(environmentPrefix);
		return this;
	}

	/**
	 * 应用上下文的 {@link ResourceLoader}。如需自定义类加载器，在此设置。
	 * @param resourceLoader 要设置的资源加载器
	 * @return the current builder
	 */
	public SpringApplicationBuilder resourceLoader(ResourceLoader resourceLoader) {
		this.application.setResourceLoader(resourceLoader);
		return this;
	}

	/**
	 * 向应用添加初始化器（在加载任何 Bean 定义前应用于 {@link ApplicationContext}）。
	 * @param initializers 要添加的初始化器
	 * @return the current builder
	 */
	public SpringApplicationBuilder initializers(ApplicationContextInitializer<?>... initializers) {
		this.application.addInitializers(initializers);
		return this;
	}

	/**
	 * 向应用添加监听器（监听 SpringApplication 事件及上下文运行后的常规 Spring 事件）。
	 * 同时实现 {@link ApplicationContextInitializer} 的监听器会自动加入
	 * {@link #initializers(ApplicationContextInitializer...) 初始化器}。
	 * @param listeners 要添加的监听器
	 * @return the current builder
	 */
	public SpringApplicationBuilder listeners(ApplicationListener<?>... listeners) {
		this.application.addListeners(listeners);
		return this;
	}

	/**
	 * 配置与 {@link ApplicationContext} 配合使用的 {@link ApplicationStartup}，
	 * 用于收集启动指标。
	 * @param applicationStartup 要使用的应用启动追踪器
	 * @return the current builder
	 * @since 2.4.0
	 */
	public SpringApplicationBuilder applicationStartup(ApplicationStartup applicationStartup) {
		this.application.setApplicationStartup(applicationStartup);
		return this;
	}

	/**
	 * 是否允许 Bean 之间的循环引用并自动尝试解析。
	 * @param allowCircularReferences 是否允许循环引用
	 * @return the current builder
	 * @since 2.6.0
	 * @see AbstractAutowireCapableBeanFactory#setAllowCircularReferences(boolean)
	 */
	public SpringApplicationBuilder allowCircularReferences(boolean allowCircularReferences) {
		this.application.setAllowCircularReferences(allowCircularReferences);
		return this;
	}

}
