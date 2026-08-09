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

package org.springframework.boot.web.servlet.support;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import reactor.core.scheduler.Schedulers;

import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.ParentContextApplicationContextInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.web.context.servlet.AnnotationConfigServletWebApplicationContext;
import org.springframework.boot.web.context.servlet.ApplicationServletEnvironment;
import org.springframework.boot.web.context.servlet.WebApplicationContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * 用于在传统 WAR 部署中运行 {@link SpringApplication} 的约定式 {@link WebApplicationInitializer}。
 * 将应用上下文中的 {@link Servlet}、{@link Filter} 与
 * {@link ServletContextInitializer} Bean 绑定到服务器。
 * <p>
 * 配置应用时可覆盖 {@link #configure(SpringApplicationBuilder)} 方法
 * （调用 {@link SpringApplicationBuilder#sources(Class...)}），
 * 或将初始化器本身声明为 {@code @Configuration}。
 * 若将 {@link SpringBootServletInitializer} 与其他
 * {@link WebApplicationInitializer WebApplicationInitializers} 组合使用，
 * 还可添加 {@code @Ordered} 注解以指定启动顺序。
 * <p>
 * 注意：仅当构建 WAR 并部署时才需要 WebApplicationInitializer；
 * 若使用嵌入式 Web 服务器则完全不需要此类。
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Brian Clozel
 * @since 2.0.0
 * @see #configure(SpringApplicationBuilder)
 */
public abstract class SpringBootServletInitializer implements WebApplicationInitializer {

	private static final boolean REACTOR_PRESENT = ClassUtils.isPresent("reactor.core.scheduler.Schedulers",
			SpringBootServletInitializer.class.getClassLoader());

	protected @Nullable Log logger; // 不要过早初始化

	private boolean registerErrorPageFilter = true;

	/**
	 * 设置是否注册 {@link ErrorPageFilter}。
	 * 若错误页映射应由服务器而非 Spring Boot 处理，则设为 {@code false}。
	 *
	 * @param registerErrorPageFilter if the {@link ErrorPageFilter} should be registered 是否注册 {@link ErrorPageFilter}
	 */
	protected final void setRegisterErrorPageFilter(boolean registerErrorPageFilter) {
		this.registerErrorPageFilter = registerErrorPageFilter;
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		servletContext.setAttribute(LoggingApplicationListener.REGISTER_SHUTDOWN_HOOK_PROPERTY, false);
		// 延迟初始化 Logger，以防使用了带顺序的 LogServletContextInitializer
		this.logger = LogFactory.getLog(getClass());
		WebApplicationContext rootApplicationContext = createRootApplicationContext(servletContext);
		if (rootApplicationContext != null) {
			servletContext.addListener(new SpringBootContextLoaderListener(rootApplicationContext, servletContext));
		}
		else {
			this.logger.debug("No ContextLoaderListener registered, as createRootApplicationContext() did not "
					+ "return an application context");
		}
	}

	/**
	 * 注销由给定 {@code servletContext} 所代表应用注册的 JDBC 驱动。
	 * 默认实现会 {@link DriverManager#deregisterDriver(Driver) 注销}
	 * 由 {@link ServletContext#getClassLoader Web 应用类加载器} 加载的每个 {@link Driver}。
	 *
	 * @param servletContext the web application's servlet context Web 应用的 Servlet 上下文
	 * @since 2.3.0
	 */
	protected void deregisterJdbcDrivers(ServletContext servletContext) {
		for (Driver driver : Collections.list(DriverManager.getDrivers())) {
			if (driver.getClass().getClassLoader() == servletContext.getClassLoader()) {
				try {
					DriverManager.deregisterDriver(driver);
				}
				catch (SQLException ex) {
					// 继续
				}
			}
		}
	}

	/**
	 * 关闭由 {@code Schedulers.boundedElastic()}（或类似方法）初始化的 Reactor {@link Schedulers}。
	 * 默认实现会在调度器由此 Web 应用类加载器初始化时调用 {@link Schedulers#shutdownNow()}。
	 *
	 * @param servletContext the web application's servlet context Web 应用的 Servlet 上下文
	 * @since 3.4.0
	 */
	protected void shutDownSharedReactorSchedulers(ServletContext servletContext) {
		if (Schedulers.class.getClassLoader() == servletContext.getClassLoader()) {
			Schedulers.shutdownNow();
		}
	}

	protected @Nullable WebApplicationContext createRootApplicationContext(ServletContext servletContext) {
		SpringApplicationBuilder builder = createSpringApplicationBuilder();
		builder.main(getClass());
		ApplicationContext parent = getExistingRootWebApplicationContext(servletContext);
		if (parent != null) {
			getLogger().info("Root context already created (using as parent).");
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, null);
			builder.initializers(new ParentContextApplicationContextInitializer(parent));
		}
		builder.initializers(new ServletContextApplicationContextInitializer(servletContext));
		builder.contextFactory(new WarDeploymentApplicationContextFactory(servletContext));
		builder = configure(builder);
		builder.listeners(new WebEnvironmentPropertySourceInitializer(servletContext));
		SpringApplication application = builder.build();
		if (application.getAllSources().isEmpty()
				&& MergedAnnotations.from(getClass(), SearchStrategy.TYPE_HIERARCHY).isPresent(Configuration.class)) {
			application.addPrimarySources(Collections.singleton(getClass()));
		}
		Assert.state(!application.getAllSources().isEmpty(),
				"No SpringApplication sources have been defined. Either override the "
						+ "configure method or add an @Configuration annotation");
		// 确保注册错误页
		if (this.registerErrorPageFilter) {
			application.addPrimarySources(Collections.singleton(ErrorPageFilterConfiguration.class));
		}
		application.setRegisterShutdownHook(false);
		return run(application);
	}

	private Log getLogger() {
		Assert.state(this.logger != null, "Logger not set");
		return this.logger;
	}

	/**
	 * 返回用于配置并创建 {@link SpringApplication} 的 {@code SpringApplicationBuilder}。
	 * 默认实现返回处于默认状态的新 {@code SpringApplicationBuilder}。
	 *
	 * @return the {@code SpringApplicationBuilder} SpringApplicationBuilder 实例
	 */
	protected SpringApplicationBuilder createSpringApplicationBuilder() {
		return new SpringApplicationBuilder();
	}

	/**
	 * 运行已完全配置的 {@link SpringApplication}。
	 *
	 * @param application the application to run 待运行的应用
	 * @return the {@link WebApplicationContext} Web 应用上下文
	 */
	protected @Nullable WebApplicationContext run(SpringApplication application) {
		return (WebApplicationContext) application.run();
	}

	private @Nullable ApplicationContext getExistingRootWebApplicationContext(ServletContext servletContext) {
		Object context = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (context instanceof ApplicationContext applicationContext) {
			return applicationContext;
		}
		return null;
	}

	/**
	 * 配置应用。通常只需添加 sources（例如配置类），其他设置已有合理默认值。
	 * 也可选择添加默认命令行参数或设置激活的 Spring profile 等。
	 *
	 * @param builder a builder for the application context 应用上下文构建器
	 * @return the application builder 应用构建器
	 * @see SpringApplicationBuilder
	 */
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder;
	}

	/**
	 * 触发 {@link ConfigurableWebEnvironment#initPropertySources(ServletContext, jakarta.servlet.ServletConfig)} 的
	 * {@link ApplicationListener}。
	 */
	private static final class WebEnvironmentPropertySourceInitializer
			implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

		private final ServletContext servletContext;

		private WebEnvironmentPropertySourceInitializer(ServletContext servletContext) {
			this.servletContext = servletContext;
		}

		@Override
		public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
			ConfigurableEnvironment environment = event.getEnvironment();
			if (environment instanceof ConfigurableWebEnvironment configurableWebEnvironment) {
				configurableWebEnvironment.initPropertySources(this.servletContext, null);
			}
		}

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE;
		}

	}

	/**
	 * 用于已初始化上下文的 {@link ContextLoaderListener}。
	 */
	private class SpringBootContextLoaderListener extends ContextLoaderListener {

		private final ServletContext servletContext;

		SpringBootContextLoaderListener(WebApplicationContext applicationContext, ServletContext servletContext) {
			super(applicationContext);
			this.servletContext = servletContext;
		}

		@Override
		public void contextInitialized(ServletContextEvent event) {
			// 无操作，因为应用上下文已初始化
		}

		@Override
		public void contextDestroyed(ServletContextEvent event) {
			try {
				super.contextDestroyed(event);
			}
			finally {
				// 使用原始上下文以便访问类加载器
				deregisterJdbcDrivers(this.servletContext);
				// 关闭与此类加载器绑定的共享 Reactor 调度器
				if (REACTOR_PRESENT) {
					shutDownSharedReactorSchedulers(this.servletContext);
				}
			}
		}

	}

	private static final class WarDeploymentApplicationContextFactory implements ApplicationContextFactory {

		private final ServletContext servletContext;

		private WarDeploymentApplicationContextFactory(ServletContext servletContext) {
			this.servletContext = servletContext;
		}

		@Override
		public @Nullable Class<? extends ConfigurableEnvironment> getEnvironmentType(
				@Nullable WebApplicationType webApplicationType) {
			return (webApplicationType != WebApplicationType.SERVLET) ? null : ApplicationServletEnvironment.class;
		}

		@Override
		public ConfigurableEnvironment createEnvironment(@Nullable WebApplicationType webApplicationType) {
			return new ApplicationServletEnvironment();
		}

		@Override
		public ConfigurableApplicationContext create(@Nullable WebApplicationType webApplicationType) {
			return new AnnotationConfigServletWebApplicationContext() {

				@Override
				protected void onRefresh() {
					super.onRefresh();
					try {
						new WebApplicationContextInitializer(this)
							.initialize(WarDeploymentApplicationContextFactory.this.servletContext);
					}
					catch (ServletException ex) {
						throw new ApplicationContextException("Cannot initialize servlet context", ex);
					}
				}

			};
		}

	}

}
