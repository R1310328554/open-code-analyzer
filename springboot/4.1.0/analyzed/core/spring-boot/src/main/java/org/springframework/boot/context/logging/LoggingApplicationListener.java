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

package org.springframework.boot.context.logging;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerGroup;
import org.springframework.boot.logging.LoggerGroups;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.LoggingSystemProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.log.LogMessage;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 配置 {@link LoggingSystem} 的 {@link ApplicationListener}。
 * 若环境中存在 {@code logging.config} 属性，则用它引导日志系统，否则使用默认配置。
 * 无论哪种情况，若环境包含 {@code logging.level.*} 条目则会定制日志级别，
 * 并可通过 {@code logging.group} 定义日志组。
 * <p>
 * 当环境中存在 {@code debug} 或 {@code trace} 属性且未设为 {@code "false"} 时
 * （例如使用 {@literal java -jar myapp.jar [--debug | --trace]} 启动应用），
 * 将为 Spring、Tomcat、Jetty 和 Hibernate 启用 debug/trace 日志。
 * 若要忽略这些属性，可将 {@link #setParseArgs(boolean) parseArgs} 设为 {@code false}。
 * <p>
 * 默认仅向控制台输出日志。若需日志文件，可使用 {@code logging.file.path}
 * 和 {@code logging.file.name} 属性。
 * <p>
 * 可能作为副作用设置一些系统属性，当日志配置支持占位符（如 log4j 或 logback）时很有用：
 * <ul>
 * <li>{@code LOG_FILE} 设为应写入的日志文件路径（若有）。</li>
 * <li>{@code PID} 设为当前进程 ID（若可确定）。</li>
 * </ul>
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Madhura Bhave
 * @author HaiTao Zhang
 * @since 2.0.0
 * @see LoggingSystem#get(ClassLoader)
 */
public class LoggingApplicationListener implements GenericApplicationListener {

	private static final ConfigurationPropertyName LOGGING_LEVEL = ConfigurationPropertyName.of("logging.level");

	private static final ConfigurationPropertyName LOGGING_GROUP = ConfigurationPropertyName.of("logging.group");

	private static final Bindable<Map<String, LogLevel>> STRING_LOGLEVEL_MAP = Bindable.mapOf(String.class,
			LogLevel.class);

	private static final Bindable<Map<String, List<String>>> STRING_STRINGS_MAP = Bindable
		.of(ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, String.class).asMap());

	/**
	 * LoggingApplicationListener 的默认顺序。
	 */
	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 20;

	/**
	 * 包含待加载日志配置引用的 Spring 属性名。
	 */
	public static final String CONFIG_PROPERTY = "logging.config";

	/**
	 * 控制是否在 JVM 退出时注册关闭钩子以关闭日志系统的 Spring 属性名。
	 *
	 * @see LoggingSystem#getShutdownHandler
	 */
	public static final String REGISTER_SHUTDOWN_HOOK_PROPERTY = "logging.register-shutdown-hook";

	/**
	 * {@link LoggingSystem} Bean 的名称。
	 */
	public static final String LOGGING_SYSTEM_BEAN_NAME = "springBootLoggingSystem";

	/**
	 * {@link LogFile} Bean 的名称。
	 *
	 * @since 2.2.0
	 */
	public static final String LOG_FILE_BEAN_NAME = "springBootLogFile";

	/**
	 * {@link LoggerGroups} Bean 的名称。
	 *
	 * @since 2.2.0
	 */
	public static final String LOGGER_GROUPS_BEAN_NAME = "springBootLoggerGroups";

	/**
	 * 用于处理清理的 {@link Lifecycle} Bean 的名称。
	 */
	private static final String LOGGING_LIFECYCLE_BEAN_NAME = "springBootLoggingLifecycle";

	private static final Map<String, List<String>> DEFAULT_GROUP_LOGGERS;
	static {
		MultiValueMap<String, String> loggers = new LinkedMultiValueMap<>();
		loggers.add("web", "org.springframework.core.codec");
		loggers.add("web", "org.springframework.http");
		loggers.add("web", "org.springframework.web");
		loggers.add("web", "org.springframework.boot.actuate.endpoint.web");
		loggers.add("web", "org.springframework.boot.web.servlet.ServletContextInitializerBeans");
		loggers.add("sql", "org.springframework.jdbc.core");
		loggers.add("sql", "org.hibernate.SQL");
		loggers.add("sql", "org.jooq.tools.LoggerListener");
		DEFAULT_GROUP_LOGGERS = Collections.unmodifiableMap(loggers);
	}

	private static final Map<LogLevel, List<String>> SPRING_BOOT_LOGGING_LOGGERS;
	static {
		MultiValueMap<LogLevel, String> loggers = new LinkedMultiValueMap<>();
		loggers.add(LogLevel.DEBUG, "sql");
		loggers.add(LogLevel.DEBUG, "web");
		loggers.add(LogLevel.DEBUG, "org.springframework.boot");
		loggers.add(LogLevel.TRACE, "org.springframework");
		loggers.add(LogLevel.TRACE, "org.apache.tomcat");
		loggers.add(LogLevel.TRACE, "org.apache.catalina");
		loggers.add(LogLevel.TRACE, "org.eclipse.jetty");
		loggers.add(LogLevel.TRACE, "org.hibernate.tool.hbm2ddl");
		SPRING_BOOT_LOGGING_LOGGERS = Collections.unmodifiableMap(loggers);
	}

	private static final Class<?>[] EVENT_TYPES = { ApplicationStartingEvent.class,
			ApplicationEnvironmentPreparedEvent.class, ApplicationPreparedEvent.class, ContextClosedEvent.class,
			ApplicationFailedEvent.class };

	private static final Class<?>[] SOURCE_TYPES = { SpringApplication.class, ApplicationContext.class };

	private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean();

	private final Log logger = LogFactory.getLog(getClass());

	private @Nullable LoggingSystem loggingSystem;

	private @Nullable LogFile logFile;

	private @Nullable LoggerGroups loggerGroups;

	private int order = DEFAULT_ORDER;

	private boolean parseArgs = true;

	private @Nullable LogLevel springBootLogging;

	@Override
	public boolean supportsEventType(ResolvableType resolvableType) {
		return isAssignableFrom(resolvableType.getRawClass(), EVENT_TYPES);
	}

	@Override
	public boolean supportsSourceType(@Nullable Class<?> sourceType) {
		return isAssignableFrom(sourceType, SOURCE_TYPES);
	}

	private boolean isAssignableFrom(@Nullable Class<?> type, Class<?>... supportedTypes) {
		if (type != null) {
			for (Class<?> supportedType : supportedTypes) {
				if (supportedType.isAssignableFrom(type)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationStartingEvent startingEvent) {
			onApplicationStartingEvent(startingEvent);
		}
		else if (event instanceof ApplicationEnvironmentPreparedEvent environmentPreparedEvent) {
			onApplicationEnvironmentPreparedEvent(environmentPreparedEvent);
		}
		else if (event instanceof ApplicationPreparedEvent preparedEvent) {
			onApplicationPreparedEvent(preparedEvent);
		}
		else if (event instanceof ContextClosedEvent contextClosedEvent) {
			onContextClosedEvent(contextClosedEvent);
		}
		else if (event instanceof ApplicationFailedEvent) {
			onApplicationFailedEvent();
		}
	}

	private void onApplicationStartingEvent(ApplicationStartingEvent event) {
		this.loggingSystem = LoggingSystem.get(event.getSpringApplication().getClassLoader());
		this.loggingSystem.beforeInitialize();
	}

	private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
		SpringApplication springApplication = event.getSpringApplication();
		if (this.loggingSystem == null) {
			this.loggingSystem = LoggingSystem.get(springApplication.getClassLoader());
		}
		initialize(event.getEnvironment(), springApplication.getClassLoader());
	}

	private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
		ConfigurableApplicationContext applicationContext = event.getApplicationContext();
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		if (!beanFactory.containsBean(LOGGING_SYSTEM_BEAN_NAME)) {
			Assert.state(this.loggingSystem != null, "loggingSystem is not set");
			beanFactory.registerSingleton(LOGGING_SYSTEM_BEAN_NAME, this.loggingSystem);
		}
		if (this.logFile != null && !beanFactory.containsBean(LOG_FILE_BEAN_NAME)) {
			beanFactory.registerSingleton(LOG_FILE_BEAN_NAME, this.logFile);
		}
		if (this.loggerGroups != null && !beanFactory.containsBean(LOGGER_GROUPS_BEAN_NAME)) {
			beanFactory.registerSingleton(LOGGER_GROUPS_BEAN_NAME, this.loggerGroups);
		}
		if (!beanFactory.containsBean(LOGGING_LIFECYCLE_BEAN_NAME) && applicationContext.getParent() == null) {
			beanFactory.registerSingleton(LOGGING_LIFECYCLE_BEAN_NAME, new Lifecycle());
		}
	}

	private void onContextClosedEvent(ContextClosedEvent event) {
		ApplicationContext applicationContext = event.getApplicationContext();
		if (applicationContext.getParent() != null || applicationContext.containsBean(LOGGING_LIFECYCLE_BEAN_NAME)) {
			return;
		}
		cleanupLoggingSystem();
	}

	void cleanupLoggingSystem() {
		if (this.loggingSystem != null) {
			this.loggingSystem.cleanUp();
		}
	}

	private void onApplicationFailedEvent() {
		cleanupLoggingSystem();
	}

	/**
	 * 根据 {@link Environment} 和类路径表达的偏好初始化日志系统。
	 *
	 * @param environment 环境
	 * @param classLoader 类加载器
	 */
	protected void initialize(ConfigurableEnvironment environment, ClassLoader classLoader) {
		getLoggingSystemProperties(environment).apply();
		this.logFile = LogFile.get(environment);
		if (this.logFile != null) {
			this.logFile.applyToSystemProperties();
		}
		this.loggerGroups = new LoggerGroups(DEFAULT_GROUP_LOGGERS);
		initializeEarlyLoggingLevel(environment);
		Assert.state(this.loggingSystem != null, "loggingSystem is not set");
		initializeSystem(environment, this.loggingSystem, this.logFile);
		initializeFinalLoggingLevels(environment, this.loggingSystem);
		registerShutdownHookIfNecessary(environment, this.loggingSystem);
	}

	private LoggingSystemProperties getLoggingSystemProperties(ConfigurableEnvironment environment) {
		return (this.loggingSystem != null) ? this.loggingSystem.getSystemProperties(environment)
				: new LoggingSystemProperties(environment);
	}

	private void initializeEarlyLoggingLevel(ConfigurableEnvironment environment) {
		if (this.parseArgs && this.springBootLogging == null) {
			if (isSet(environment, "debug")) {
				this.springBootLogging = LogLevel.DEBUG;
			}
			if (isSet(environment, "trace")) {
				this.springBootLogging = LogLevel.TRACE;
			}
		}
	}

	private boolean isSet(ConfigurableEnvironment environment, String property) {
		String value = environment.getProperty(property);
		return (value != null && !value.equals("false"));
	}

	private void initializeSystem(ConfigurableEnvironment environment, LoggingSystem system,
			@Nullable LogFile logFile) {
		String logConfig = environment.getProperty(CONFIG_PROPERTY);
		if (StringUtils.hasLength(logConfig)) {
			logConfig = logConfig.strip();
		}
		try {
			LoggingInitializationContext initializationContext = new LoggingInitializationContext(environment);
			if (ignoreLogConfig(logConfig)) {
				system.initialize(initializationContext, null, logFile);
			}
			else {
				system.initialize(initializationContext, logConfig, logFile);
			}
		}
		catch (Throwable ex) {
			Throwable exceptionToReport = ex;
			while (exceptionToReport != null && !(exceptionToReport instanceof FileNotFoundException)) {
				exceptionToReport = exceptionToReport.getCause();
			}
			exceptionToReport = (exceptionToReport != null) ? exceptionToReport : ex;
			// NOTE: We can't use the logger here to report the problem
			System.err.println("Logging system failed to initialize using configuration from '" + logConfig + "'");
			exceptionToReport.printStackTrace(System.err);
			throw new IllegalStateException(ex);
		}
	}

	private boolean ignoreLogConfig(@Nullable String logConfig) {
		return !StringUtils.hasLength(logConfig) || logConfig.startsWith("-D");
	}

	private void initializeFinalLoggingLevels(ConfigurableEnvironment environment, LoggingSystem system) {
		bindLoggerGroups(environment);
		if (this.springBootLogging != null) {
			initializeSpringBootLogging(system, this.springBootLogging);
		}
		setLogLevels(system, environment);
	}

	private void bindLoggerGroups(ConfigurableEnvironment environment) {
		if (this.loggerGroups != null) {
			Binder binder = Binder.get(environment);
			binder.bind(LOGGING_GROUP, STRING_STRINGS_MAP).ifBound(this.loggerGroups::putAll);
		}
	}

	/**
	 * 根据 {@link #setSpringBootLogging(LogLevel) springBootLogging} 设置初始化日志记录器。
	 * 默认实现会按级别选择合适的一组日志记录器进行配置。
	 *
	 * @param system 日志系统
	 * @param springBootLogging 请求的 Spring Boot 日志级别
	 * @since 2.2.0
	 */
	protected void initializeSpringBootLogging(LoggingSystem system, LogLevel springBootLogging) {
		BiConsumer<String, @Nullable LogLevel> configurer = getLogLevelConfigurer(system);
		SPRING_BOOT_LOGGING_LOGGERS.getOrDefault(springBootLogging, Collections.emptyList())
			.forEach((name) -> configureLogLevel(name, springBootLogging, configurer));
	}

	/**
	 * 根据相关 {@link Environment} 属性设置日志级别。
	 *
	 * @param system 日志系统
	 * @param environment 环境
	 * @since 2.2.0
	 */
	protected void setLogLevels(LoggingSystem system, ConfigurableEnvironment environment) {
		BiConsumer<String, @Nullable LogLevel> customizer = getLogLevelConfigurer(system);
		Binder binder = Binder.get(environment);
		Map<String, LogLevel> levels = binder.bind(LOGGING_LEVEL, STRING_LOGLEVEL_MAP).orElseGet(Collections::emptyMap);
		levels.forEach((name, level) -> configureLogLevel(name, level, customizer));
	}

	private void configureLogLevel(String name, LogLevel level, BiConsumer<String, @Nullable LogLevel> configurer) {
		if (this.loggerGroups != null) {
			LoggerGroup group = this.loggerGroups.get(name);
			if (group != null && group.hasMembers()) {
				group.configureLogLevel(level, configurer);
				return;
			}
		}
		configurer.accept(name, level);
	}

	private BiConsumer<String, @Nullable LogLevel> getLogLevelConfigurer(LoggingSystem system) {
		return (name, level) -> {
			try {
				name = name.equalsIgnoreCase(LoggingSystem.ROOT_LOGGER_NAME) ? null : name;
				system.setLogLevel(name, level);
			}
			catch (RuntimeException ex) {
				this.logger.error(LogMessage.format("Cannot set level '%s' for '%s'", level, name));
			}
		};
	}

	private void registerShutdownHookIfNecessary(Environment environment, LoggingSystem loggingSystem) {
		if (environment.getProperty(REGISTER_SHUTDOWN_HOOK_PROPERTY, Boolean.class, true)) {
			Runnable shutdownHandler = loggingSystem.getShutdownHandler();
			if (shutdownHandler != null && shutdownHookRegistered.compareAndSet(false, true)) {
				registerShutdownHook(shutdownHandler);
			}
		}
	}

	void registerShutdownHook(Runnable shutdownHandler) {
		SpringApplication.getShutdownHandlers().add(shutdownHandler);
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * 设置用于 Spring Boot 及相关库的自定义日志级别。
	 *
	 * @param springBootLogging 日志级别
	 */
	public void setSpringBootLogging(LogLevel springBootLogging) {
		this.springBootLogging = springBootLogging;
	}

	/**
	 * 设置是否解析初始化参数中的 {@literal debug} 和 {@literal trace} 属性
	 * （通常来自 {@literal --debug} 或 {@literal --trace} 命令行参数）。默认为 {@code true}。
	 *
	 * @param parseArgs 是否解析参数
	 */
	public void setParseArgs(boolean parseArgs) {
		this.parseArgs = parseArgs;
	}

	private final class Lifecycle implements SmartLifecycle {

		private volatile boolean running;

		@Override
		public void start() {
			this.running = true;
		}

		@Override
		public void stop() {
			this.running = false;
			cleanupLoggingSystem();
		}

		@Override
		public boolean isRunning() {
			return this.running;
		}

		@Override
		public int getPhase() {
			// Shutdown late and always after WebServerStartStopLifecycle
			return Integer.MIN_VALUE + 1;
		}

	}

}
