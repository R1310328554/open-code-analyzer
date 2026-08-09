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

import java.lang.StackWalker.StackFrame;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.crac.management.CRaCMXBean;
import org.jspecify.annotations.Nullable;

import org.springframework.aot.AotDetector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.BootstrapRegistryInitializer;
import org.springframework.boot.bootstrap.DefaultBootstrapContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.env.DefaultPropertiesPropertySource;
import org.springframework.boot.system.JavaVersion;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.aot.AotApplicationContextInitializer;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.NativeDetector;
import org.springframework.core.OrderComparator;
import org.springframework.core.OrderComparator.OrderSourceProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.io.support.SpringFactoriesLoader.ArgumentResolver;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.function.ThrowingConsumer;
import org.springframework.util.function.ThrowingSupplier;

/**
 * 可从 Java main 方法引导并启动 Spring 应用的类。默认情况下会执行以下步骤引导应用：
 *
 * <ul>
 * <li>创建合适的 {@link ApplicationContext} 实例（取决于 classpath）</li>
 * <li>注册 {@link CommandLinePropertySource}，将命令行参数暴露为 Spring 属性</li>
 * <li>刷新应用上下文，加载所有单例 Bean</li>
 * <li>触发所有 {@link CommandLineRunner} Bean</li>
 * </ul>
 *
 * 多数情况下可直接在 {@literal main} 方法中调用静态 {@link #run(Class, String[])} 方法启动应用：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAutoConfiguration
 * public class MyApplication  {
 *
 *   // ... Bean definitions
 *
 *   public static void main(String[] args) {
 *     SpringApplication.run(MyApplication.class, args);
 *   }
 * }
 * </pre>
 *
 * <p>
 * 高级配置可先创建 {@link SpringApplication} 实例，定制后再运行：
 *
 * <pre class="code">
 * public static void main(String[] args) {
 *   SpringApplication application = new SpringApplication(MyApplication.class);
 *   // ... customize application settings here
 *   application.run(args)
 * }
 * </pre>
 *
 * {@link SpringApplication} 可从多种来源读取 Bean。通常建议使用单个 {@code @Configuration}
 * 类引导应用，也可通过 {@link #getSources() sources} 设置：
 * <ul>
 * <li>由 {@link AnnotatedBeanDefinitionReader} 加载的全限定类名</li>
 * <li>由 {@link XmlBeanDefinitionReader} 加载的 XML 资源位置，或由
 * {@link GroovyBeanDefinitionReader} 加载的 Groovy 脚本</li>
 * <li>由 {@link ClassPathBeanDefinitionScanner} 扫描的包名</li>
 * </ul>
 *
 * 配置属性也会绑定到 {@link SpringApplication}，从而可动态设置属性，例如额外来源
 * （{@code spring.main.sources}，CSV 列表）、Web 环境标志
 * （{@code spring.main.web-application-type=none}）或关闭 Banner
 * （{@code spring.main.banner-mode=off}）。
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Christian Dupuis
 * @author Stephane Nicoll
 * @author Jeremy Rickard
 * @author Craig Burke
 * @author Michael Simons
 * @author Madhura Bhave
 * @author Brian Clozel
 * @author Ethan Rubinson
 * @author Chris Bono
 * @author Moritz Halbritter
 * @author Tadaya Tsuyukubo
 * @author Lasse Wulff
 * @author Yanming Zhou
 * @since 1.0.0
 * @see #run(Class, String[])
 * @see #run(Class[], String[])
 * @see #SpringApplication(Class...)
 */
public class SpringApplication {

	/**
	 * 默认 Banner 位置。
	 */
	public static final String BANNER_LOCATION_PROPERTY_VALUE = SpringApplicationBannerPrinter.DEFAULT_BANNER_LOCATION;

	/**
	 * Banner 位置属性键。
	 */
	public static final String BANNER_LOCATION_PROPERTY = SpringApplicationBannerPrinter.BANNER_LOCATION_PROPERTY;

	private static final String SYSTEM_PROPERTY_JAVA_AWT_HEADLESS = "java.awt.headless";

	private static final Log logger = LogFactory.getLog(SpringApplication.class);

	static final SpringApplicationShutdownHook shutdownHook = new SpringApplicationShutdownHook();

	private static final ThreadLocal<SpringApplicationHook> applicationHook = new ThreadLocal<>();

	private final Set<Class<?>> primarySources;

	private @Nullable Class<?> mainApplicationClass;

	private boolean addCommandLineProperties = true;

	private boolean addConversionService = true;

	private @Nullable Banner banner;

	private @Nullable ResourceLoader resourceLoader;

	private @Nullable BeanNameGenerator beanNameGenerator;

	private @Nullable ConfigurableEnvironment environment;

	private boolean headless = true;

	private List<ApplicationContextInitializer<?>> initializers = new ArrayList<>();

	private List<ApplicationListener<?>> listeners = new ArrayList<>();

	private @Nullable Map<String, Object> defaultProperties;

	private final List<BootstrapRegistryInitializer> bootstrapRegistryInitializers;

	private Set<String> additionalProfiles = Collections.emptySet();

	private boolean isCustomEnvironment;

	private @Nullable String environmentPrefix;

	private ApplicationContextFactory applicationContextFactory = ApplicationContextFactory.DEFAULT;

	private ApplicationStartup applicationStartup = ApplicationStartup.DEFAULT;

	final ApplicationProperties properties = new ApplicationProperties();

	/**
	 * 创建新的 {@link SpringApplication} 实例。应用上下文将从指定的主来源加载 Bean
	 * （详见 {@link SpringApplication 类级} 文档）。可在调用 {@link #run(String...)} 前定制实例。
	 * @param primarySources 主 Bean 来源
	 * @see #run(Class, String[])
	 * @see #SpringApplication(ResourceLoader, Class...)
	 * @see #setSources(Set)
	 */
	public SpringApplication(Class<?>... primarySources) {
		this(null, primarySources);
	}

	/**
	 * 创建新的 {@link SpringApplication} 实例。应用上下文将从指定的主来源加载 Bean
	 * （详见 {@link SpringApplication 类级} 文档）。可在调用 {@link #run(String...)} 前定制实例。
	 * @param resourceLoader 要使用的资源加载器
	 * @param primarySources 主 Bean 来源
	 * @see #run(Class, String[])
	 * @see #setSources(Set)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SpringApplication(@Nullable ResourceLoader resourceLoader, Class<?>... primarySources) {
		this.resourceLoader = resourceLoader;
		Assert.notNull(primarySources, "'primarySources' must not be null");
		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
		this.properties.setWebApplicationType(WebApplicationType.deduce());
		this.bootstrapRegistryInitializers = new ArrayList<>(
				getSpringFactoriesInstances(BootstrapRegistryInitializer.class));
		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
		this.mainApplicationClass = deduceMainApplicationClass();
	}

	private @Nullable Class<?> deduceMainApplicationClass() {
		return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
			.walk(this::findMainClass)
			.orElse(null);
	}

	private Optional<Class<?>> findMainClass(Stream<StackFrame> stack) {
		return stack.filter((frame) -> Objects.equals(frame.getMethodName(), "main"))
			.findFirst()
			.map(StackWalker.StackFrame::getDeclaringClass);
	}

	/**
	 * 运行 Spring 应用，创建并刷新新的 {@link ApplicationContext}。
	 * @param args 应用参数（通常来自 Java main 方法）
	 * @return 运行中的 {@link ApplicationContext}
	 */
	public ConfigurableApplicationContext run(String... args) {
		// 步骤 1：初始化启动计时与关闭钩子
		Startup startup = Startup.create();
		if (this.properties.isRegisterShutdownHook()) {
			SpringApplication.shutdownHook.enableShutdownHookAddition();
		}
		// 步骤 2：创建 Bootstrap 上下文并通知监听器 starting
		DefaultBootstrapContext bootstrapContext = createBootstrapContext();
		ConfigurableApplicationContext context = null;
		configureHeadlessProperty();
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting(bootstrapContext, this.mainApplicationClass);
		try {
			// 步骤 3：准备环境、打印 Banner、创建并准备 ApplicationContext
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
			ConfigurableEnvironment environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments);
			Banner printedBanner = printBanner(environment);
			context = createApplicationContext();
			context.setApplicationStartup(this.applicationStartup);
			prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
			// 步骤 4：刷新上下文并执行 Runner
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			Duration timeTakenToStarted = startup.started();
			if (this.properties.isLogStartupInfo()) {
				new StartupInfoLogger(this.mainApplicationClass, environment).logStarted(getApplicationLog(), startup);
			}
			listeners.started(context, timeTakenToStarted);
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			throw handleRunFailure(context, ex, listeners);
		}
		try {
			// 步骤 5：应用就绪后通知监听器 ready
			if (context.isRunning()) {
				listeners.ready(context, startup.ready());
			}
		}
		catch (Throwable ex) {
			throw handleRunFailure(context, ex, null);
		}
		return context;
	}

	private DefaultBootstrapContext createBootstrapContext() {
		DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext();
		this.bootstrapRegistryInitializers.forEach((initializer) -> initializer.initialize(bootstrapContext));
		return bootstrapContext;
	}

	private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
			DefaultBootstrapContext bootstrapContext, ApplicationArguments applicationArguments) {
		// 步骤：创建并配置 Environment
		ConfigurableEnvironment environment = getOrCreateEnvironment();
		configureEnvironment(environment, applicationArguments.getSourceArgs());
		ConfigurationPropertySources.attach(environment);
		listeners.environmentPrepared(bootstrapContext, environment);
		ApplicationInfoPropertySource.moveToEnd(environment);
		DefaultPropertiesPropertySource.moveToEnd(environment);
		Assert.state(!environment.containsProperty("spring.main.environment-prefix"),
				"Environment prefix cannot be set via properties.");
		bindToSpringApplication(environment);
		if (!this.isCustomEnvironment) {
			EnvironmentConverter environmentConverter = new EnvironmentConverter(getClassLoader());
			environment = environmentConverter.convertEnvironmentIfNecessary(environment, deduceEnvironmentClass());
		}
		ConfigurationPropertySources.attach(environment);
		return environment;
	}

	private Class<? extends ConfigurableEnvironment> deduceEnvironmentClass() {
		WebApplicationType webApplicationType = this.properties.getWebApplicationType();
		Class<? extends ConfigurableEnvironment> environmentType = this.applicationContextFactory
			.getEnvironmentType(webApplicationType);
		if (environmentType == null && this.applicationContextFactory != ApplicationContextFactory.DEFAULT) {
			environmentType = ApplicationContextFactory.DEFAULT.getEnvironmentType(webApplicationType);
		}
		return (environmentType != null) ? environmentType : ApplicationEnvironment.class;
	}

	private void prepareContext(DefaultBootstrapContext bootstrapContext, ConfigurableApplicationContext context,
			ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
			ApplicationArguments applicationArguments, @Nullable Banner printedBanner) {
		context.setEnvironment(environment);
		postProcessApplicationContext(context);
		addAotGeneratedInitializerIfNecessary(this.initializers);
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		if (beanFactory instanceof AbstractAutowireCapableBeanFactory autowireCapableBeanFactory) {
			autowireCapableBeanFactory.setAllowCircularReferences(this.properties.isAllowCircularReferences());
			if (beanFactory instanceof DefaultListableBeanFactory listableBeanFactory) {
				listableBeanFactory.setAllowBeanDefinitionOverriding(this.properties.isAllowBeanDefinitionOverriding());
			}
		}
		applyInitializers(context);
		listeners.contextPrepared(context);
		bootstrapContext.close(context);
		if (this.properties.isLogStartupInfo()) {
			logStartupInfo(context);
			logStartupProfileInfo(context);
		}
		// 步骤：注册 Boot 专用单例 Bean
		beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
		if (printedBanner != null) {
			beanFactory.registerSingleton("springBootBanner", printedBanner);
		}
		if (this.properties.isLazyInitialization()) {
			context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
		}
		if (this.properties.isKeepAlive()) {
			context.addApplicationListener(new KeepAlive());
		}
		context.addBeanFactoryPostProcessor(new PropertySourceOrderingBeanFactoryPostProcessor(context));
		if (!AotDetector.useGeneratedArtifacts()) {
			// 步骤：加载配置来源
			Set<Object> sources = getAllSources();
			Assert.state(!ObjectUtils.isEmpty(sources), "No sources defined");
			load(context, sources.toArray(new Object[0]));
		}
		listeners.contextLoaded(context);
	}

	private void addAotGeneratedInitializerIfNecessary(List<ApplicationContextInitializer<?>> initializers) {
		if (AotDetector.useGeneratedArtifacts()) {
			List<ApplicationContextInitializer<?>> aotInitializers = new ArrayList<>(
					initializers.stream().filter(AotApplicationContextInitializer.class::isInstance).toList());
			if (aotInitializers.isEmpty()) {
				Assert.state(this.mainApplicationClass != null, "No application main class found");
				String initializerClassName = this.mainApplicationClass.getName() + "__ApplicationContextInitializer";
				if (!ClassUtils.isPresent(initializerClassName, getClassLoader())) {
					throw new AotInitializerNotFoundException(this.mainApplicationClass, initializerClassName);
				}
				aotInitializers.add(AotApplicationContextInitializer.forInitializerClasses(initializerClassName));
			}
			initializers.removeAll(aotInitializers);
			initializers.addAll(0, aotInitializers);
		}
		if (NativeDetector.inNativeImage()) {
			NativeImageRequirementsException.throwIfNotMet();
		}
	}

	private void refreshContext(ConfigurableApplicationContext context) {
		// 步骤：注册关闭钩子后刷新 ApplicationContext
		if (this.properties.isRegisterShutdownHook()) {
			shutdownHook.registerApplicationContext(context);
		}
		refresh(context);
	}

	private void configureHeadlessProperty() {
		System.setProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS,
				System.getProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, Boolean.toString(this.headless)));
	}

	private SpringApplicationRunListeners getRunListeners(String[] args) {
		ArgumentResolver argumentResolver = ArgumentResolver.of(SpringApplication.class, this);
		argumentResolver = argumentResolver.and(String[].class, args);
		List<SpringApplicationRunListener> listeners = getSpringFactoriesInstances(SpringApplicationRunListener.class,
				argumentResolver);
		SpringApplicationHook hook = applicationHook.get();
		SpringApplicationRunListener hookListener = (hook != null) ? hook.getRunListener(this) : null;
		if (hookListener != null) {
			listeners = new ArrayList<>(listeners);
			listeners.add(hookListener);
		}
		return new SpringApplicationRunListeners(logger, listeners, this.applicationStartup);
	}

	private <T> List<T> getSpringFactoriesInstances(Class<T> type) {
		return getSpringFactoriesInstances(type, null);
	}

	private <T> List<T> getSpringFactoriesInstances(Class<T> type, @Nullable ArgumentResolver argumentResolver) {
		return SpringFactoriesLoader.forDefaultResourceLocation(getClassLoader()).load(type, argumentResolver);
	}

	private ConfigurableEnvironment getOrCreateEnvironment() {
		if (this.environment != null) {
			return this.environment;
		}
		WebApplicationType webApplicationType = this.properties.getWebApplicationType();
		ConfigurableEnvironment environment = this.applicationContextFactory.createEnvironment(webApplicationType);
		if (environment == null && this.applicationContextFactory != ApplicationContextFactory.DEFAULT) {
			environment = ApplicationContextFactory.DEFAULT.createEnvironment(webApplicationType);
		}
		return (environment != null) ? environment : new ApplicationEnvironment();
	}

	/**
	 * 模板方法，依次委托 {@link #configurePropertySources(ConfigurableEnvironment, String[])}
	 * 与 {@link #configureProfiles(ConfigurableEnvironment, String[])}。
	 * 覆盖此方法可完全控制 Environment 定制，或覆盖上述方法分别精细控制属性源或 Profile。
	 * @param environment 本应用的环境
	 * @param args 传递给 {@code run} 方法的参数
	 * @see #configureProfiles(ConfigurableEnvironment, String[])
	 * @see #configurePropertySources(ConfigurableEnvironment, String[])
	 */
	protected void configureEnvironment(ConfigurableEnvironment environment, String[] args) {
		if (this.addConversionService) {
			environment.setConversionService(new ApplicationConversionService());
		}
		configurePropertySources(environment, args);
		configureProfiles(environment, args);
	}

	/**
	 * 在本应用环境中添加、移除或重排 {@link PropertySource}。
	 * @param environment 本应用的环境
	 * @param args 传递给 {@code run} 方法的参数
	 * @see #configureEnvironment(ConfigurableEnvironment, String[])
	 */
	protected void configurePropertySources(ConfigurableEnvironment environment, String[] args) {
		MutablePropertySources sources = environment.getPropertySources();
		if (!CollectionUtils.isEmpty(this.defaultProperties)) {
			DefaultPropertiesPropertySource.addOrMerge(this.defaultProperties, sources);
		}
		if (this.addCommandLineProperties && args.length > 0) {
			String name = CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME;
			PropertySource<?> source = sources.get(name);
			if (source != null) {
				CompositePropertySource composite = new CompositePropertySource(name);
				composite
					.addPropertySource(new SimpleCommandLinePropertySource("springApplicationCommandLineArgs", args));
				composite.addPropertySource(source);
				sources.replace(name, composite);
			}
			else {
				sources.addFirst(new SimpleCommandLinePropertySource(args));
			}
		}
		environment.getPropertySources().addLast(new ApplicationInfoPropertySource(this.mainApplicationClass));
	}

	/**
	 * 配置本应用环境中激活（或默认激活）的 Profile。配置处理期间还可通过
	 * {@code spring.profiles.active} 属性激活额外 Profile。
	 * @param environment 本应用的环境
	 * @param args 传递给 {@code run} 方法的参数
	 * @see #configureEnvironment(ConfigurableEnvironment, String[])
	 */
	protected void configureProfiles(ConfigurableEnvironment environment, String[] args) {
	}

	/**
	 * 将环境绑定到 {@link ApplicationProperties}。
	 * @param environment 要绑定的环境
	 */
	protected void bindToSpringApplication(ConfigurableEnvironment environment) {
		try {
			Binder.get(environment).bind("spring.main", Bindable.ofInstance(this.properties));
		}
		catch (Exception ex) {
			throw new IllegalStateException("Cannot bind to SpringApplication", ex);
		}
	}

	private @Nullable Banner printBanner(ConfigurableEnvironment environment) {
		if (this.properties.getBannerMode(environment) == Banner.Mode.OFF) {
			return null;
		}
		ResourceLoader resourceLoader = (this.resourceLoader != null) ? this.resourceLoader
				: new DefaultResourceLoader(null);
		SpringApplicationBannerPrinter bannerPrinter = new SpringApplicationBannerPrinter(resourceLoader, this.banner);
		if (this.properties.getBannerMode(environment) == Mode.LOG) {
			return bannerPrinter.print(environment, this.mainApplicationClass, logger);
		}
		return bannerPrinter.print(environment, this.mainApplicationClass, System.out);
	}

	/**
	 * 创建 {@link ApplicationContext} 的策略方法。默认先尊重显式设置的上下文类或工厂，
	 * 再回退到合适的默认值。
	 * @return 应用上下文（尚未刷新）
	 * @see #setApplicationContextFactory(ApplicationContextFactory)
	 */
	protected ConfigurableApplicationContext createApplicationContext() {
		// 步骤：根据 Web 应用类型通过工厂创建 ApplicationContext
		ConfigurableApplicationContext context = this.applicationContextFactory
			.create(this.properties.getWebApplicationType());
		Assert.state(context != null, "ApplicationContextFactory created null context");
		return context;
	}

	/**
	 * 对 {@link ApplicationContext} 应用相关后处理。子类可按需添加额外处理。
	 * @param context 应用上下文
	 */
	protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
		if (this.beanNameGenerator != null) {
			context.getBeanFactory()
				.registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, this.beanNameGenerator);
		}
		if (this.resourceLoader != null) {
			if (context instanceof GenericApplicationContext genericApplicationContext) {
				genericApplicationContext.setResourceLoader(this.resourceLoader);
			}
			if (context instanceof DefaultResourceLoader defaultResourceLoader) {
				defaultResourceLoader.setClassLoader(this.resourceLoader.getClassLoader());
			}
		}
		if (this.addConversionService) {
			context.getBeanFactory().setConversionService(context.getEnvironment().getConversionService());
		}
	}

	/**
	 * 在上下文刷新前应用所有 {@link ApplicationContextInitializer}。
	 * @param context 已配置但未刷新的 ApplicationContext
	 * @see ConfigurableApplicationContext#refresh()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void applyInitializers(ConfigurableApplicationContext context) {
		for (ApplicationContextInitializer initializer : getInitializers()) {
			Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(initializer.getClass(),
					ApplicationContextInitializer.class);
			Assert.state(requiredType != null,
					() -> "No generic type found for initializr of type " + initializer.getClass());
			Assert.state(requiredType.isInstance(context), "Unable to call initializer");
			initializer.initialize(context);
		}
	}

	/**
	 * 记录启动信息，子类可覆盖以添加额外日志。
	 * @param context 应用上下文
	 * @since 3.4.0
	 */
	protected void logStartupInfo(ConfigurableApplicationContext context) {
		boolean isRoot = context.getParent() == null;
		if (isRoot) {
			new StartupInfoLogger(this.mainApplicationClass, context.getEnvironment()).logStarting(getApplicationLog());
		}
	}

	/**
	 * 记录激活 Profile 信息。
	 * @param context 应用上下文
	 */
	protected void logStartupProfileInfo(ConfigurableApplicationContext context) {
		Log log = getApplicationLog();
		if (log.isInfoEnabled()) {
			List<String> activeProfiles = quoteProfiles(context.getEnvironment().getActiveProfiles());
			if (ObjectUtils.isEmpty(activeProfiles)) {
				List<String> defaultProfiles = quoteProfiles(context.getEnvironment().getDefaultProfiles());
				String message = String.format("%s default %s: ", defaultProfiles.size(),
						(defaultProfiles.size() <= 1) ? "profile" : "profiles");
				log.info("No active profile set, falling back to " + message
						+ StringUtils.collectionToDelimitedString(defaultProfiles, ", "));
			}
			else {
				String message = (activeProfiles.size() == 1) ? "1 profile is active: "
						: activeProfiles.size() + " profiles are active: ";
				log.info("The following " + message + StringUtils.collectionToDelimitedString(activeProfiles, ", "));
			}
		}
	}

	private List<String> quoteProfiles(String[] profiles) {
		return Arrays.stream(profiles).map((profile) -> "\"" + profile + "\"").toList();
	}

	/**
	 * 返回应用的 {@link Log}，默认自动推断。
	 * @return 应用日志
	 */
	protected Log getApplicationLog() {
		if (this.mainApplicationClass == null) {
			return logger;
		}
		return LogFactory.getLog(this.mainApplicationClass);
	}

	/**
	 * 将 Bean 加载到应用上下文。
	 * @param context 要加载 Bean 的上下文
	 * @param sources 要加载的来源
	 */
	protected void load(ApplicationContext context, Object[] sources) {
		if (logger.isDebugEnabled()) {
			logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
		}
		BeanDefinitionLoader loader = createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources);
		if (this.beanNameGenerator != null) {
			loader.setBeanNameGenerator(this.beanNameGenerator);
		}
		if (this.resourceLoader != null) {
			loader.setResourceLoader(this.resourceLoader);
		}
		if (this.environment != null) {
			loader.setEnvironment(this.environment);
		}
		loader.load();
	}

	/**
	 * ApplicationContext 中使用的 ResourceLoader。
	 * @return ApplicationContext 使用的资源加载器（默认时为 {@code null}）
	 */
	public @Nullable ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	/**
	 * 返回 ApplicationContext 使用的 ClassLoader：若设置了
	 * {@link #setResourceLoader(ResourceLoader) resourceLoader} 则使用其 ClassLoader，
	 * 否则使用上下文类加载器（若非 null），再否则使用 Spring {@link ClassUtils} 的加载器。
	 * @return ClassLoader（永不为 null）
	 */
	public ClassLoader getClassLoader() {
		if (this.resourceLoader != null) {
			ClassLoader classLoader = this.resourceLoader.getClassLoader();
			Assert.state(classLoader != null, "No classloader found");
			return classLoader;
		}
		ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
		Assert.state(classLoader != null, "No classloader found");
		return classLoader;
	}

	/**
	 * 获取 Bean 定义注册表。
	 * @param context 应用上下文
	 * @return 可确定时的 BeanDefinitionRegistry
	 */
	private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
		if (context instanceof BeanDefinitionRegistry registry) {
			return registry;
		}
		if (context instanceof AbstractApplicationContext abstractApplicationContext) {
			return (BeanDefinitionRegistry) abstractApplicationContext.getBeanFactory();
		}
		throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
	}

	/**
	 * 创建 {@link BeanDefinitionLoader} 的工厂方法。
	 * @param registry Bean 定义注册表
	 * @param sources 要加载的来源
	 * @return 用于加载 Bean 的 {@link BeanDefinitionLoader}
	 */
	protected BeanDefinitionLoader createBeanDefinitionLoader(BeanDefinitionRegistry registry, Object[] sources) {
		return new BeanDefinitionLoader(registry, sources);
	}

	/**
	 * 刷新底层 {@link ApplicationContext}。
	 * @param applicationContext 要刷新的应用上下文
	 */
	protected void refresh(ConfigurableApplicationContext applicationContext) {
		// 步骤：委托 ApplicationContext.refresh() 完成容器初始化
		applicationContext.refresh();
	}

	/**
	 * 上下文刷新后调用。
	 * @param context 应用上下文
	 * @param args 应用参数
	 */
	protected void afterRefresh(ConfigurableApplicationContext context, ApplicationArguments args) {
	}

	private void callRunners(ConfigurableApplicationContext context, ApplicationArguments args) {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		String[] beanNames = beanFactory.getBeanNamesForType(Runner.class);
		Map<Runner, String> instancesToBeanNames = new IdentityHashMap<>();
		for (String beanName : beanNames) {
			instancesToBeanNames.put(beanFactory.getBean(beanName, Runner.class), beanName);
		}
		Comparator<Object> comparator = getOrderComparator(beanFactory)
			.withSourceProvider(new FactoryAwareOrderSourceProvider(beanFactory, instancesToBeanNames));
		instancesToBeanNames.keySet().stream().sorted(comparator).forEach((runner) -> callRunner(runner, args));
	}

	private OrderComparator getOrderComparator(ConfigurableListableBeanFactory beanFactory) {
		Comparator<?> dependencyComparator = (beanFactory instanceof DefaultListableBeanFactory defaultListableBeanFactory)
				? defaultListableBeanFactory.getDependencyComparator() : null;
		return (dependencyComparator instanceof OrderComparator orderComparator) ? orderComparator
				: AnnotationAwareOrderComparator.INSTANCE;
	}

	private void callRunner(Runner runner, ApplicationArguments args) {
		if (runner instanceof ApplicationRunner) {
			callRunner(ApplicationRunner.class, runner, (applicationRunner) -> applicationRunner.run(args));
		}
		if (runner instanceof CommandLineRunner) {
			callRunner(CommandLineRunner.class, runner,
					(commandLineRunner) -> commandLineRunner.run(args.getSourceArgs()));
		}
	}

	@SuppressWarnings("unchecked")
	private <R extends Runner> void callRunner(Class<R> type, Runner runner, ThrowingConsumer<R> call) {
		call.throwing(
				(message, ex) -> new IllegalStateException("Failed to execute " + ClassUtils.getShortName(type), ex))
			.accept((R) runner);
	}

	private RuntimeException handleRunFailure(@Nullable ConfigurableApplicationContext context, Throwable exception,
			@Nullable SpringApplicationRunListeners listeners) {
		if (exception instanceof AbandonedRunException abandonedRunException) {
			return abandonedRunException;
		}
		try {
			try {
				handleExitCode(context, exception);
				if (listeners != null) {
					listeners.failed(context, exception);
				}
			}
			finally {
				reportFailure(getExceptionReporters(context), exception);
				if (context != null) {
					context.close();
					shutdownHook.deregisterFailedApplicationContext(context);
				}
			}
		}
		catch (Exception ex) {
			logger.warn("Unable to close ApplicationContext", ex);
		}
		return (exception instanceof RuntimeException runtimeException) ? runtimeException
				: new IllegalStateException(exception);
	}

	private Collection<SpringBootExceptionReporter> getExceptionReporters(
			@Nullable ConfigurableApplicationContext context) {
		try {
			ArgumentResolver argumentResolver = (context != null)
					? ArgumentResolver.of(ConfigurableApplicationContext.class, context) : ArgumentResolver.none();
			return getSpringFactoriesInstances(SpringBootExceptionReporter.class, argumentResolver);
		}
		catch (Throwable ex) {
			return Collections.emptyList();
		}
	}

	private void reportFailure(Collection<SpringBootExceptionReporter> exceptionReporters, Throwable failure) {
		try {
			for (SpringBootExceptionReporter reporter : exceptionReporters) {
				if (reporter.reportException(failure)) {
					registerLoggedException(failure);
					return;
				}
			}
		}
		catch (Throwable ex) {
			// 继续按原失败流程处理
		}
		if (logger.isErrorEnabled()) {
			if (NativeDetector.inNativeImage()) {
				// 失败发生过早时 native image 中日志可能不可用，直接输出到 System.out
				System.out.println("Application run failed");
				failure.printStackTrace(System.out);
			}
			else {
				logger.error("Application run failed", failure);
			}
			registerLoggedException(failure);
		}
	}

	/**
	 * 注册给定异常已记录。默认在主线程运行时，将抑制额外堆栈跟踪输出。
	 * @param exception 已记录的异常
	 */
	protected void registerLoggedException(Throwable exception) {
		SpringBootExceptionHandler handler = getSpringBootExceptionHandler();
		if (handler != null) {
			handler.registerLoggedException(exception);
		}
	}

	private void handleExitCode(@Nullable ConfigurableApplicationContext context, Throwable exception) {
		int exitCode = getExitCodeFromException(context, exception);
		if (exitCode != 0) {
			if (context != null) {
				context.publishEvent(new ExitCodeEvent(context, exitCode));
			}
			SpringBootExceptionHandler handler = getSpringBootExceptionHandler();
			if (handler != null) {
				handler.registerExitCode(exitCode);
			}
		}
	}

	private int getExitCodeFromException(@Nullable ConfigurableApplicationContext context, Throwable exception) {
		int exitCode = getExitCodeFromMappedException(context, exception);
		if (exitCode == 0) {
			exitCode = getExitCodeFromExitCodeGeneratorException(exception);
		}
		return exitCode;
	}

	private int getExitCodeFromMappedException(@Nullable ConfigurableApplicationContext context, Throwable exception) {
		if (context == null || !context.isActive()) {
			return 0;
		}
		ExitCodeGenerators generators = new ExitCodeGenerators();
		Collection<ExitCodeExceptionMapper> beans = context.getBeansOfType(ExitCodeExceptionMapper.class).values();
		generators.addAll(exception, beans);
		return generators.getExitCode();
	}

	private int getExitCodeFromExitCodeGeneratorException(@Nullable Throwable exception) {
		if (exception == null) {
			return 0;
		}
		if (exception instanceof ExitCodeGenerator generator) {
			return generator.getExitCode();
		}
		return getExitCodeFromExitCodeGeneratorException(exception.getCause());
	}

	@Nullable SpringBootExceptionHandler getSpringBootExceptionHandler() {
		if (isMainThread(Thread.currentThread())) {
			return SpringBootExceptionHandler.forCurrentThread();
		}
		return null;
	}

	private boolean isMainThread(Thread currentThread) {
		return ("main".equals(currentThread.getName()) || "restartedMain".equals(currentThread.getName()))
				&& "main".equals(currentThread.getThreadGroup().getName());
	}

	/**
	 * 返回已推断或显式配置的主应用类。
	 * @return 主应用类或 {@code null}
	 */
	public @Nullable Class<?> getMainApplicationClass() {
		return this.mainApplicationClass;
	}

	/**
	 * 设置用作日志来源及获取版本信息的主应用类。默认自动推断。
	 * 若无显式应用类可设为 {@code null}。
	 * @param mainApplicationClass 要设置的主应用类或 {@code null}
	 */
	public void setMainApplicationClass(@Nullable Class<?> mainApplicationClass) {
		this.mainApplicationClass = mainApplicationClass;
	}

	/**
	 * 返回正在运行的 Web 应用类型。
	 * @return Web 应用类型
	 * @since 2.0.0
	 */
	public @Nullable WebApplicationType getWebApplicationType() {
		return this.properties.getWebApplicationType();
	}

	/**
	 * 设置要运行的 Web 应用类型。未显式设置时将根据 classpath 推断。
	 * @param webApplicationType Web 应用类型
	 * @since 2.0.0
	 */
	public void setWebApplicationType(WebApplicationType webApplicationType) {
		Assert.notNull(webApplicationType, "'webApplicationType' must not be null");
		this.properties.setWebApplicationType(webApplicationType);
	}

	/**
	 * 设置是否允许 Bean 定义覆盖（注册与已有定义同名的定义）。默认为 {@code false}。
	 * @param allowBeanDefinitionOverriding 是否允许覆盖
	 * @since 2.1.0
	 * @see DefaultListableBeanFactory#setAllowBeanDefinitionOverriding(boolean)
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.properties.setAllowBeanDefinitionOverriding(allowBeanDefinitionOverriding);
	}

	/**
	 * 设置是否允许 Bean 间循环引用并自动解析。默认为 {@code false}。
	 * @param allowCircularReferences 是否允许循环引用
	 * @since 2.6.0
	 * @see AbstractAutowireCapableBeanFactory#setAllowCircularReferences(boolean)
	 */
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.properties.setAllowCircularReferences(allowCircularReferences);
	}

	/**
	 * 设置 Bean 是否懒初始化。默认为 {@code false}。
	 * @param lazyInitialization 是否懒初始化
	 * @since 2.2
	 * @see BeanDefinition#setLazyInit(boolean)
	 */
	public void setLazyInitialization(boolean lazyInitialization) {
		this.properties.setLazyInitialization(lazyInitialization);
	}

	/**
	 * 设置应用是否为 headless（不实例化 AWT）。默认为 {@code true}，避免 Java 图标弹出。
	 * @param headless 是否为 headless 应用
	 */
	public void setHeadless(boolean headless) {
		this.headless = headless;
	}

	/**
	 * 设置是否为创建的 {@link ApplicationContext} 注册关闭钩子。默认为 {@code true}，
	 * 确保 JVM 关闭时优雅处理。
	 * @param registerShutdownHook 是否注册关闭钩子
	 * @see #getShutdownHandlers()
	 */
	public void setRegisterShutdownHook(boolean registerShutdownHook) {
		this.properties.setRegisterShutdownHook(registerShutdownHook);
	}

	/**
	 * 设置未提供静态 Banner 文件时用于打印 Banner 的 {@link Banner} 实例。
	 * @param banner 要使用的 Banner 实例
	 */
	public void setBanner(Banner banner) {
		this.banner = banner;
	}

	/**
	 * 设置应用运行时显示 Banner 的模式。默认为 {@code Banner.Mode.CONSOLE}。
	 * @param bannerMode Banner 显示模式
	 */
	public void setBannerMode(Banner.Mode bannerMode) {
		this.properties.setBannerMode(bannerMode);
	}

	/**
	 * 设置应用启动时是否记录应用信息。默认为 {@code true}。
	 * @param logStartupInfo 是否记录启动信息
	 */
	public void setLogStartupInfo(boolean logStartupInfo) {
		this.properties.setLogStartupInfo(logStartupInfo);
	}

	/**
	 * 设置是否向应用上下文添加 {@link CommandLinePropertySource} 以暴露参数。默认为 {@code true}。
	 * @param addCommandLineProperties 是否暴露命令行参数
	 */
	public void setAddCommandLineProperties(boolean addCommandLineProperties) {
		this.addCommandLineProperties = addCommandLineProperties;
	}

	/**
	 * 设置是否将 {@link ApplicationConversionService} 添加到应用上下文的 {@link Environment}。
	 * @param addConversionService 是否添加应用转换服务
	 * @since 2.1.0
	 */
	public void setAddConversionService(boolean addConversionService) {
		this.addConversionService = addConversionService;
	}

	/**
	 * 添加用于初始化 {@link BootstrapRegistry} 的 {@link BootstrapRegistryInitializer} 实例。
	 * @param bootstrapRegistryInitializer 要添加的引导注册表初始化器
	 * @since 2.4.5
	 */
	public void addBootstrapRegistryInitializer(BootstrapRegistryInitializer bootstrapRegistryInitializer) {
		Assert.notNull(bootstrapRegistryInitializer, "'bootstrapRegistryInitializer' must not be null");
		this.bootstrapRegistryInitializers.addAll(Arrays.asList(bootstrapRegistryInitializer));
	}

	/**
	 * 设置默认环境属性，与现有 {@link Environment} 中的属性合并使用。
	 * @param defaultProperties 要设置的额外属性
	 */
	public void setDefaultProperties(Map<String, Object> defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	/**
	 * {@link #setDefaultProperties(Map)} 的便捷替代方法。
	 * @param defaultProperties 一些 {@link Properties}
	 */
	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = new HashMap<>();
		for (Object key : Collections.list(defaultProperties.propertyNames())) {
			this.defaultProperties.put((String) key, defaultProperties.get(key));
		}
	}

	/**
	 * 设置要使用的额外 Profile（叠加系统或命令行属性中的 Profile）。
	 * @param profiles 要设置的额外 Profile
	 */
	public void setAdditionalProfiles(String... profiles) {
		this.additionalProfiles = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(profiles)));
	}

	/**
	 * 返回正在使用的额外 Profile 的不可变集合。
	 * @return 额外 Profile
	 */
	public Set<String> getAdditionalProfiles() {
		return this.additionalProfiles;
	}

	/**
	 * 设置生成 Bean 名称时使用的 Bean 名称生成器。
	 * @param beanNameGenerator Bean 名称生成器
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}

	/**
	 * 设置与创建的应用上下文一起使用的基础环境。
	 * @param environment 环境
	 */
	public void setEnvironment(@Nullable ConfigurableEnvironment environment) {
		this.isCustomEnvironment = true;
		this.environment = environment;
	}

	/**
	 * 向主来源添加额外项，在调用 {@link #run(String...)} 时加入 ApplicationContext。
	 * <p>
	 * 此处来源与构造器中设置的来源合并。多数用户应使用
	 * {@link #getSources()}/{@link #setSources(Set)} 而非直接调用本方法。
	 * @param additionalPrimarySources 要添加的额外主来源
	 * @see #SpringApplication(Class...)
	 * @see #getSources()
	 * @see #setSources(Set)
	 * @see #getAllSources()
	 */
	public void addPrimarySources(Collection<Class<?>> additionalPrimarySources) {
		this.primarySources.addAll(additionalPrimarySources);
	}

	/**
	 * 返回调用 {@link #run(String...)} 时将加入 ApplicationContext 的来源的可变集合。
	 * <p>
	 * 此处设置的来源与构造器中的主来源合并使用。
	 * @return 应用来源
	 * @see #SpringApplication(Class...)
	 * @see #getAllSources()
	 */
	public Set<String> getSources() {
		return this.properties.getSources();
	}

	/**
	 * 设置用于创建 ApplicationContext 的额外来源。来源可以是类名、包名或 XML 资源位置。
	 * <p>
	 * 此处设置的来源与构造器中的主来源合并使用。
	 * @param sources 要设置的应用来源
	 * @see #SpringApplication(Class...)
	 * @see #getAllSources()
	 */
	public void setSources(Set<String> sources) {
		Assert.notNull(sources, "'sources' must not be null");
		this.properties.setSources(sources);
	}

	/**
	 * 返回调用 {@link #run(String...)} 时将加入 ApplicationContext 的所有来源的不可变集合。
	 * 合并构造器中的主来源与 {@link #setSources(Set) 显式设置} 的额外来源。
	 * @return 所有来源的不可变集合
	 */
	public Set<Object> getAllSources() {
		Set<Object> allSources = new LinkedHashSet<>();
		if (!CollectionUtils.isEmpty(this.primarySources)) {
			allSources.addAll(this.primarySources);
		}
		if (!CollectionUtils.isEmpty(this.properties.getSources())) {
			allSources.addAll(this.properties.getSources());
		}
		return Collections.unmodifiableSet(allSources);
	}

	/**
	 * 设置加载资源时使用的 {@link ResourceLoader}。
	 * @param resourceLoader 资源加载器
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "'resourceLoader' must not be null");
		this.resourceLoader = resourceLoader;
	}

	/**
	 * 返回从系统环境获取配置属性时使用的前缀。
	 * @return 环境属性前缀
	 * @since 2.5.0
	 */
	public @Nullable String getEnvironmentPrefix() {
		return this.environmentPrefix;
	}

	/**
	 * 设置从系统环境获取配置属性时使用的前缀。
	 * @param environmentPrefix 要设置的环境属性前缀
	 * @since 2.5.0
	 */
	public void setEnvironmentPrefix(String environmentPrefix) {
		this.environmentPrefix = environmentPrefix;
	}

	/**
	 * 设置用于创建应用上下文的工厂。未设置时默认创建适合应用类型的上下文
	 * （响应式 Web、Servlet Web 或非 Web 应用）。
	 * @param applicationContextFactory 上下文工厂
	 * @since 2.4.0
	 */
	public void setApplicationContextFactory(@Nullable ApplicationContextFactory applicationContextFactory) {
		this.applicationContextFactory = (applicationContextFactory != null) ? applicationContextFactory
				: ApplicationContextFactory.DEFAULT;
	}

	/**
	 * 设置将应用于 Spring {@link ApplicationContext} 的 {@link ApplicationContextInitializer}。
	 * @param initializers 要设置的初始化器
	 */
	public void setInitializers(Collection<? extends ApplicationContextInitializer<?>> initializers) {
		this.initializers = new ArrayList<>(initializers);
	}

	/**
	 * 添加将应用于 Spring {@link ApplicationContext} 的 {@link ApplicationContextInitializer}。
	 * @param initializers 要添加的初始化器
	 */
	public void addInitializers(ApplicationContextInitializer<?>... initializers) {
		this.initializers.addAll(Arrays.asList(initializers));
	}

	/**
	 * 返回将应用于 Spring {@link ApplicationContext} 的 {@link ApplicationContextInitializer}
	 * 的只读有序集合。
	 * @return 初始化器
	 */
	public Set<ApplicationContextInitializer<?>> getInitializers() {
		return asUnmodifiableOrderedSet(this.initializers);
	}

	/**
	 * 设置将应用于 SpringApplication 并注册到 {@link ApplicationContext} 的
	 * {@link ApplicationListener}。
	 * @param listeners 要设置的监听器
	 */
	public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
		this.listeners = new ArrayList<>(listeners);
	}

	/**
	 * 添加将应用于 SpringApplication 并注册到 {@link ApplicationContext} 的
	 * {@link ApplicationListener}。
	 * @param listeners 要添加的监听器
	 */
	public void addListeners(ApplicationListener<?>... listeners) {
		this.listeners.addAll(Arrays.asList(listeners));
	}

	/**
	 * 返回将应用于 SpringApplication 并注册到 {@link ApplicationContext} 的
	 * {@link ApplicationListener} 的只读有序集合。
	 * @return 监听器
	 */
	public Set<ApplicationListener<?>> getListeners() {
		return asUnmodifiableOrderedSet(this.listeners);
	}

	/**
	 * 设置用于收集启动指标的 {@link ApplicationStartup}。
	 * @param applicationStartup 要使用的应用启动追踪器
	 * @since 2.4.0
	 */
	public void setApplicationStartup(ApplicationStartup applicationStartup) {
		this.applicationStartup = (applicationStartup != null) ? applicationStartup : ApplicationStartup.DEFAULT;
	}

	/**
	 * 返回用于收集启动指标的 {@link ApplicationStartup}。
	 * @return 应用启动追踪器
	 * @since 2.4.0
	 */
	public ApplicationStartup getApplicationStartup() {
		return this.applicationStartup;
	}

	/**
	 * 即使没有更多非守护线程，是否仍保持应用存活。
	 * @return 是否保持应用存活
	 * @since 3.2.0
	 */
	public boolean isKeepAlive() {
		return this.properties.isKeepAlive();
	}

	/**
	 * 设置即使没有更多非守护线程，是否仍保持应用存活。
	 * @param keepAlive 是否保持应用存活
	 * @since 3.2.0
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.properties.setKeepAlive(keepAlive);
	}

	/**
	 * 返回可用于添加或移除 JVM 关闭前执行操作的处理器的
	 * {@link SpringApplicationShutdownHandlers} 实例。
	 * @return {@link SpringApplicationShutdownHandlers} 实例
	 * @since 2.5.1
	 */
	public static SpringApplicationShutdownHandlers getShutdownHandlers() {
		return shutdownHook.getHandlers();
	}

	/**
	 * 使用默认设置从指定来源运行 {@link SpringApplication} 的静态辅助方法。
	 * @param primarySource 要加载的主来源
	 * @param args 应用参数（通常来自 Java main 方法）
	 * @return 运行中的 {@link ApplicationContext}
	 */
	public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
		return run(new Class<?>[] { primarySource }, args);
	}

	/**
	 * 使用默认设置和用户参数从指定来源运行 {@link SpringApplication} 的静态辅助方法。
	 * @param primarySources 要加载的主来源
	 * @param args 应用参数（通常来自 Java main 方法）
	 * @return 运行中的 {@link ApplicationContext}
	 */
	public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
		return new SpringApplication(primarySources).run(args);
	}

	/**
	 * 可用于启动应用的基础 main 方法。当通过 {@literal --spring.main.sources}
	 * 命令行参数定义应用来源时很有用。
	 * <p>
	 * 多数开发者应定义自己的 main 方法并调用 {@link #run(Class, String...) run} 方法。
	 * @param args 命令行参数
	 * @throws Exception 若应用无法启动
	 * @see SpringApplication#run(Class[], String[])
	 * @see SpringApplication#run(Class, String...)
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(new Class<?>[0], args);
	}

	/**
	 * 退出 {@link SpringApplication} 并获取表示成功（0）或其他结果的退出码的静态辅助方法。
	 * 不抛出异常，但会打印遇到的堆栈跟踪。除实现 {@link ExitCodeGenerator} 的 Spring Bean 外，
	 * 还应用指定的 {@link ExitCodeGenerator ExitCodeGenerators}。多个生成器可用时使用第一个非零退出码。
	 * 生成器按 {@link Ordered} 实现与 {@link Order @Order} 注解排序。
	 * @param context 尽可能要关闭的上下文
	 * @param exitCodeGenerators 退出码生成器
	 * @return 退出结果（成功为 0）
	 */
	public static int exit(ApplicationContext context, ExitCodeGenerator... exitCodeGenerators) {
		Assert.notNull(context, "'context' must not be null");
		int exitCode = 0;
		try {
			try {
				ExitCodeGenerators generators = new ExitCodeGenerators();
				Collection<ExitCodeGenerator> beans = context.getBeansOfType(ExitCodeGenerator.class).values();
				generators.addAll(exitCodeGenerators);
				generators.addAll(beans);
				exitCode = generators.getExitCode();
				if (exitCode != 0) {
					context.publishEvent(new ExitCodeEvent(context, exitCode));
				}
			}
			finally {
				close(context);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			exitCode = (exitCode != 0) ? exitCode : 1;
		}
		return exitCode;
	}

	/**
	 * 从现有 {@code main} 方法创建应用，可附加 {@code @Configuration} 或 Bean 类运行。
	 * 编写需要额外配置启动应用的测试框架时很有用。
	 * @param main 运行 {@link SpringApplication} 的 main 方法入口
	 * @return 可用于添加配置并运行应用的 {@link SpringApplication.Augmented} 实例
	 * @since 3.1.0
	 * @see #withHook(SpringApplicationHook, Runnable)
	 */
	public static SpringApplication.Augmented from(ThrowingConsumer<String[]> main) {
		Assert.notNull(main, "'main' must not be null");
		return new Augmented(main, Collections.emptySet(), Collections.emptySet());
	}

	/**
	 * 若给定动作触发 {@link SpringApplication#run(String...) 应用运行}，则附加
	 * {@link SpringApplicationHook} 后执行该动作。
	 * @param hook 要应用的钩子
	 * @param action 要执行的动作
	 * @since 3.0.0
	 * @see #withHook(SpringApplicationHook, ThrowingSupplier)
	 */
	public static void withHook(SpringApplicationHook hook, Runnable action) {
		withHook(hook, () -> {
			action.run();
			return Void.class;
		});
	}

	/**
	 * 若给定动作触发 {@link SpringApplication#run(String...) 应用运行}，则附加
	 * {@link SpringApplicationHook} 后执行该动作。
	 * @param <T> 结果类型
	 * @param hook 要应用的钩子
	 * @param action 要执行的动作
	 * @return 动作结果
	 * @since 3.0.0
	 * @see #withHook(SpringApplicationHook, Runnable)
	 */
	public static <T> T withHook(SpringApplicationHook hook, ThrowingSupplier<T> action) {
		applicationHook.set(hook);
		try {
			return action.get();
		}
		finally {
			applicationHook.remove();
		}
	}

	private static void close(ApplicationContext context) {
		if (context instanceof ConfigurableApplicationContext closable) {
			closable.close();
		}
	}

	private static <E> Set<E> asUnmodifiableOrderedSet(Collection<E> elements) {
		List<E> list = new ArrayList<>(elements);
		list.sort(AnnotationAwareOrderComparator.INSTANCE);
		return new LinkedHashSet<>(list);
	}

	/**
	 * 用于配置并运行需应用额外配置的增强型 {@link SpringApplication}。
	 *
	 * @since 3.1.0
	 */
	public static class Augmented {

		private final ThrowingConsumer<String[]> main;

		private final Set<Class<?>> sources;

		private final Set<String> additionalProfiles;

		Augmented(ThrowingConsumer<String[]> main, Set<Class<?>> sources, Set<String> additionalProfiles) {
			this.main = main;
			this.sources = Set.copyOf(sources);
			this.additionalProfiles = additionalProfiles;
		}

		/**
		 * 返回应用运行时应附加额外来源的新 {@link SpringApplication.Augmented} 实例。
		 * @param sources 要应用的来源
		 * @return 新的 {@link SpringApplication.Augmented} 实例
		 */
		public Augmented with(Class<?>... sources) {
			LinkedHashSet<Class<?>> merged = new LinkedHashSet<>(this.sources);
			merged.addAll(Arrays.asList(sources));
			return new Augmented(this.main, merged, this.additionalProfiles);
		}

		/**
		 * 返回应用运行时应附加额外 Profile 的新 {@link SpringApplication.Augmented} 实例。
		 * @param profiles 要应用的 Profile
		 * @return 新的 {@link SpringApplication.Augmented} 实例
		 * @since 3.4.0
		 */
		public Augmented withAdditionalProfiles(String... profiles) {
			Set<String> merged = new LinkedHashSet<>(this.additionalProfiles);
			merged.addAll(Arrays.asList(profiles));
			return new Augmented(this.main, this.sources, merged);
		}

		/**
		 * 使用给定参数运行应用。
		 * @param args main 方法参数
		 * @return 运行中的 {@link ApplicationContext}
		 */
		public SpringApplication.Running run(String... args) {
			RunListener runListener = new RunListener();
			SpringApplicationHook hook = new SingleUseSpringApplicationHook((springApplication) -> {
				springApplication.addPrimarySources(this.sources);
				springApplication.setAdditionalProfiles(this.additionalProfiles.toArray(String[]::new));
				return runListener;
			});
			withHook(hook, () -> this.main.accept(args));
			return runListener;
		}

		/**
		 * 用于捕获 {@link Running} 应用详情的 {@link SpringApplicationRunListener}。
		 */
		private static final class RunListener implements SpringApplicationRunListener, Running {

			private final List<ConfigurableApplicationContext> contexts = Collections
				.synchronizedList(new ArrayList<>());

			@Override
			public void contextLoaded(ConfigurableApplicationContext context) {
				this.contexts.add(context);
			}

			@Override
			public ConfigurableApplicationContext getApplicationContext() {
				List<ConfigurableApplicationContext> rootContexts = this.contexts.stream()
					.filter((context) -> context.getParent() == null)
					.toList();
				Assert.state(!rootContexts.isEmpty(), "No root application context located");
				Assert.state(rootContexts.size() == 1, "No unique root application context located");
				return rootContexts.get(0);
			}

		}

	}

	/**
	 * 提供通过 {@link Augmented#run(String...)} 运行的 {@link SpringApplication} 运行详情访问。
	 *
	 * @since 3.1.0
	 */
	public interface Running {

		/**
		 * 返回运行中应用的根 {@link ConfigurableApplicationContext}。
		 * @return 根应用上下文
		 */
		ConfigurableApplicationContext getApplicationContext();

	}

	/**
	 * {@link BeanFactoryPostProcessor}，将属性源重排到
	 * {@link ConfigurationClassPostProcessor} 添加的 {@code @PropertySource} 项之下。
	 */
	private static class PropertySourceOrderingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

		private final ConfigurableApplicationContext context;

		PropertySourceOrderingBeanFactoryPostProcessor(ConfigurableApplicationContext context) {
			this.context = context;
		}

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE;
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			DefaultPropertiesPropertySource.moveToEnd(this.context.getEnvironment());
		}

	}

	/**
	 * 可抛出以静默退出运行中 {@link SpringApplication} 且不触发运行失败处理的异常。
	 *
	 * @since 3.0.0
	 */
	public static class AbandonedRunException extends RuntimeException {

		private final @Nullable ConfigurableApplicationContext applicationContext;

		/**
		 * 创建新的 {@link AbandonedRunException} 实例。
		 */
		public AbandonedRunException() {
			this(null);
		}

		/**
		 * 使用给定应用上下文创建新的 {@link AbandonedRunException} 实例。
		 * @param applicationContext 放弃运行时可用的应用上下文
		 */
		public AbandonedRunException(@Nullable ConfigurableApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		/**
		 * 返回放弃运行时可用的应用上下文；若无可用上下文则为 {@code null}。
		 * @return 应用上下文
		 */
		public @Nullable ConfigurableApplicationContext getApplicationContext() {
			return this.applicationContext;
		}

	}

	/**
	 * GraalVM native-image 要求未满足时抛出的异常。
	 */
	static final class NativeImageRequirementsException extends RuntimeException {

		private static final JavaVersion MINIMUM_REQUIRED_JAVA_VERSION = JavaVersion.TWENTY_FIVE;

		private static final JavaVersion CURRENT_JAVA_VERSION = JavaVersion.getJavaVersion();

		NativeImageRequirementsException(String message) {
			super(message);
		}

		static void throwIfNotMet() {
			if (CURRENT_JAVA_VERSION.isOlderThan(MINIMUM_REQUIRED_JAVA_VERSION)) {
				throw new NativeImageRequirementsException("Native Image requirements not met. "
						+ "Native Image must support at least Java %s but Java %s was detected"
							.formatted(MINIMUM_REQUIRED_JAVA_VERSION, CURRENT_JAVA_VERSION));
			}
		}

	}

	/**
	 * 确保钩子仅使用一次的 {@link SpringApplicationHook} 装饰器。
	 */
	private static final class SingleUseSpringApplicationHook implements SpringApplicationHook {

		private final AtomicBoolean used = new AtomicBoolean();

		private final SpringApplicationHook delegate;

		private SingleUseSpringApplicationHook(SpringApplicationHook delegate) {
			this.delegate = delegate;
		}

		@Override
		public @Nullable SpringApplicationRunListener getRunListener(SpringApplication springApplication) {
			return this.used.compareAndSet(false, true) ? this.delegate.getRunListener(springApplication) : null;
		}

	}

	/**
	 * 在 {@link ContextRefreshedEvent} 时启动非守护线程保持 JVM 存活，
	 * 在 {@link ContextClosedEvent} 时停止该线程。
	 */
	private static final class KeepAlive implements ApplicationListener<ApplicationContextEvent> {

		private final AtomicReference<@Nullable Thread> thread = new AtomicReference<>();

		@Override
		public void onApplicationEvent(ApplicationContextEvent event) {
			if (event instanceof ContextRefreshedEvent) {
				startKeepAliveThread();
			}
			else if (event instanceof ContextClosedEvent) {
				stopKeepAliveThread();
			}
		}

		private void startKeepAliveThread() {
			Thread thread = new Thread(() -> {
				while (true) {
					try {
						Thread.sleep(Long.MAX_VALUE);
					}
					catch (InterruptedException ex) {
						break;
					}
				}
			});
			if (this.thread.compareAndSet(null, thread)) {
				thread.setDaemon(false);
				thread.setName("keep-alive");
				thread.start();
			}
		}

		private void stopKeepAliveThread() {
			Thread thread = this.thread.getAndSet(null);
			if (thread == null) {
				return;
			}
			thread.interrupt();
		}

	}

	/**
	 * 处理启动相关事项的策略。
	 */
	abstract static class Startup {

		private @Nullable Duration timeTakenToStarted;

		protected abstract long startTime();

		protected abstract @Nullable Long processUptime();

		protected abstract String action();

		final Duration started() {
			long now = System.currentTimeMillis();
			this.timeTakenToStarted = Duration.ofMillis(now - startTime());
			return this.timeTakenToStarted;
		}

		Duration timeTakenToStarted() {
			Assert.state(this.timeTakenToStarted != null,
					"timeTakenToStarted is not set. Make sure to call started() before this method");
			return this.timeTakenToStarted;
		}

		private Duration ready() {
			long now = System.currentTimeMillis();
			return Duration.ofMillis(now - startTime());
		}

		static Startup create() {
			ClassLoader classLoader = Startup.class.getClassLoader();
			return (ClassUtils.isPresent("jdk.crac.management.CRaCMXBean", classLoader)
					&& ClassUtils.isPresent("org.crac.management.CRaCMXBean", classLoader))
							? new CoordinatedRestoreAtCheckpointStartup() : new StandardStartup();
		}

	}

	/**
	 * 标准 {@link Startup} 实现。
	 */
	private static final class StandardStartup extends Startup {

		private final Long startTime = System.currentTimeMillis();

		@Override
		protected long startTime() {
			return this.startTime;
		}

		@Override
		protected @Nullable Long processUptime() {
			try {
				return ManagementFactory.getRuntimeMXBean().getUptime();
			}
			catch (Throwable ex) {
				return null;
			}
		}

		@Override
		protected String action() {
			return "Started";
		}

	}

	/**
	 * 协调检查点恢复（Coordinated-Restore-At-Checkpoint）{@link Startup} 实现。
	 */
	private static final class CoordinatedRestoreAtCheckpointStartup extends Startup {

		private final StandardStartup fallback = new StandardStartup();

		@Override
		protected @Nullable Long processUptime() {
			Long uptime = CRaCMXBean.getCRaCMXBean().getUptimeSinceRestore();
			return (uptime >= 0) ? uptime : this.fallback.processUptime();
		}

		@Override
		protected String action() {
			return (restoreTime() >= 0) ? "Restored" : this.fallback.action();
		}

		private long restoreTime() {
			return CRaCMXBean.getCRaCMXBean().getRestoreTime();
		}

		@Override
		protected long startTime() {
			long restoreTime = restoreTime();
			return (restoreTime >= 0) ? restoreTime : this.fallback.startTime();
		}

	}

	/**
	 * 用于获取工厂方法与目标类型排序来源的 {@link OrderSourceProvider}。
	 * 基于 {@link DefaultListableBeanFactory} 内部代码。
	 */
	private static class FactoryAwareOrderSourceProvider implements OrderSourceProvider {

		private final ConfigurableBeanFactory beanFactory;

		private final Map<?, String> instancesToBeanNames;

		FactoryAwareOrderSourceProvider(ConfigurableBeanFactory beanFactory, Map<?, String> instancesToBeanNames) {
			this.beanFactory = beanFactory;
			this.instancesToBeanNames = instancesToBeanNames;
		}

		@Override
		public @Nullable Object getOrderSource(Object obj) {
			String beanName = this.instancesToBeanNames.get(obj);
			return (beanName != null) ? getOrderSource(beanName, obj.getClass()) : null;
		}

		private @Nullable Object getOrderSource(String beanName, Class<?> instanceType) {
			try {
				RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory
					.getMergedBeanDefinition(beanName);
				Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
				Class<?> targetType = beanDefinition.getTargetType();
				targetType = (targetType != instanceType) ? targetType : null;
				return Stream.of(factoryMethod, targetType).filter(Objects::nonNull).toArray();
			}
			catch (NoSuchBeanDefinitionException ex) {
				return null;
			}
		}

	}

}
