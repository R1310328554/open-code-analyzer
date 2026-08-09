#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-3b batch files [20:40]."""
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][20:40]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ApplicationRunner.java": [
        (
            "/**\n * Interface used to indicate that a bean should <em>run</em> when it is contained within\n * a {@link SpringApplication}. Multiple {@link ApplicationRunner} beans can be defined\n * within the same application context and can be ordered using the {@link Ordered}\n * interface or {@link Order @Order} annotation.\n *\n * @author Phillip Webb\n * @since 1.3.0\n * @see CommandLineRunner\n */",
            "/**\n * 标识 Bean 在 {@link SpringApplication} 中应被<em>执行</em>的接口。\n * <p>\n * 同一应用上下文中可定义多个 {@link ApplicationRunner} Bean，\n * 可通过 {@link Ordered} 接口或 {@link Order @Order} 注解排序。\n *\n * @author Phillip Webb\n * @since 1.3.0\n * @see CommandLineRunner\n */",
        ),
        (
            "/**\n\t * Callback used to run the bean.\n\t * @param args incoming application arguments\n\t * @throws Exception on error\n\t */",
            "/**\n\t * 执行 Bean 的回调方法。\n\t *\n\t * @param args 传入的应用参数\n\t * @throws Exception 发生错误时\n\t */",
        ),
    ],
    "Banner.java": [
        (
            "/**\n * Interface class for writing a banner programmatically.\n *\n * @author Phillip Webb\n * @author Michael Stummvoll\n * @author Jeremy Rickard\n * @since 1.2.0\n */",
            "/**\n * 以编程方式输出 Banner 的接口。\n *\n * @author Phillip Webb\n * @author Michael Stummvoll\n * @author Jeremy Rickard\n * @since 1.2.0\n */",
        ),
        (
            "/**\n\t * Print the banner to the specified print stream.\n\t * @param environment the spring environment\n\t * @param sourceClass the source class for the application or {@code null}\n\t * @param out the output print stream\n\t */",
            "/**\n\t * 将 Banner 输出到指定的打印流。\n\t *\n\t * @param environment Spring 环境\n\t * @param sourceClass 应用的源类，或 {@code null}\n\t * @param out 输出打印流\n\t */",
        ),
        (
            "/**\n\t * An enumeration of possible values for configuring the Banner.\n\t */",
            "/**\n\t * 配置 Banner 显示方式的枚举值。\n\t */",
        ),
        (
            "/**\n\t\t * Disable printing of the banner.\n\t\t */",
            "/**\n\t\t * 禁用 Banner 输出。\n\t\t */",
        ),
        (
            "/**\n\t\t * Print the banner to System.out.\n\t\t */",
            "/**\n\t\t * 将 Banner 输出到 System.out。\n\t\t */",
        ),
        (
            "/**\n\t\t * Print the banner to the log file.\n\t\t */",
            "/**\n\t\t * 将 Banner 输出到日志文件。\n\t\t */",
        ),
    ],
    "BeanDefinitionLoader.java": [
        (
            "/**\n * Loads bean definitions from underlying sources, including XML and JavaConfig. Acts as a\n * simple facade over {@link AnnotatedBeanDefinitionReader},\n * {@link XmlBeanDefinitionReader} and {@link ClassPathBeanDefinitionScanner}. See\n * {@link SpringApplication} for the types of sources that are supported.\n *\n * @author Phillip Webb\n * @author Vladislav Kisel\n * @author Sebastien Deleuze\n * @see #setBeanNameGenerator(BeanNameGenerator)\n */",
            "/**\n * 从底层源（包括 XML 和 JavaConfig）加载 Bean 定义。\n * <p>\n * 作为 {@link AnnotatedBeanDefinitionReader}、{@link XmlBeanDefinitionReader}\n * 和 {@link ClassPathBeanDefinitionScanner} 的简单门面。支持的源类型见\n * {@link SpringApplication}。\n *\n * @author Phillip Webb\n * @author Vladislav Kisel\n * @author Sebastien Deleuze\n * @see #setBeanNameGenerator(BeanNameGenerator)\n */",
        ),
        (
            "/**\n\t * Create a new {@link BeanDefinitionLoader} that will load beans into the specified\n\t * {@link BeanDefinitionRegistry}.\n\t * @param registry the bean definition registry that will contain the loaded beans\n\t * @param sources the bean sources\n\t */",
            "/**\n\t * 创建新的 {@link BeanDefinitionLoader}，将 Bean 加载到指定的\n\t * {@link BeanDefinitionRegistry} 中。\n\t *\n\t * @param registry 将包含已加载 Bean 的 Bean 定义注册表\n\t * @param sources Bean 源\n\t */",
        ),
        (
            "/**\n\t * Set the bean name generator to be used by the underlying readers and scanner.\n\t * @param beanNameGenerator the bean name generator\n\t */",
            "/**\n\t * 设置底层读取器和扫描器使用的 Bean 名称生成器。\n\t *\n\t * @param beanNameGenerator Bean 名称生成器\n\t */",
        ),
        (
            "/**\n\t * Set the resource loader to be used by the underlying readers and scanner.\n\t * @param resourceLoader the resource loader\n\t */",
            "/**\n\t * 设置底层读取器和扫描器使用的资源加载器。\n\t *\n\t * @param resourceLoader 资源加载器\n\t */",
        ),
        (
            "/**\n\t * Set the environment to be used by the underlying readers and scanner.\n\t * @param environment the environment\n\t */",
            "/**\n\t * 设置底层读取器和扫描器使用的环境。\n\t *\n\t * @param environment 环境\n\t */",
        ),
        (
            "/**\n\t * Load the sources into the reader.\n\t */",
            "/**\n\t * 将所有源加载到读取器中。\n\t */",
        ),
        (
            "/**\n\t * Check whether the bean is eligible for registration.\n\t * @param type candidate bean type\n\t * @return true if the given bean type is eligible for registration, i.e. not a groovy\n\t * closure nor an anonymous class\n\t */",
            "/**\n\t * 检查 Bean 是否有资格注册。\n\t *\n\t * @param type 候选 Bean 类型\n\t * @return 若给定 Bean 类型有资格注册（非 Groovy 闭包且非匿名类）则返回 {@code true}\n\t */",
        ),
        (
            "/**\n\t * Simple {@link TypeFilter} used to ensure that specified {@link Class} sources are\n\t * not accidentally re-added during scanning.\n\t */",
            "/**\n\t * 简单的 {@link TypeFilter}，确保扫描期间不会意外重新添加\n\t * 已指定的 {@link Class} 源。\n\t */",
        ),
        (
            "/**\n\t * Source for Bean definitions defined in Groovy.\n\t */",
            "/**\n\t * 在 Groovy 中定义的 Bean 定义源。\n\t */",
        ),
    ],
    "ClearCachesApplicationListener.java": [
        (
            "/**\n * {@link ApplicationListener} to cleanup caches once the context is loaded.\n *\n * @author Phillip Webb\n */",
            "/**\n * 上下文加载完成后清理缓存的 {@link ApplicationListener}。\n * <p>\n * 清理 {@link ReflectionUtils} 缓存及类加载器层次结构中的 {@code clearCache} 方法。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "CommandLineRunner.java": [
        (
            "/**\n * Interface used to indicate that a bean should <em>run</em> when it is contained within\n * a {@link SpringApplication}. Multiple {@link CommandLineRunner} beans can be defined\n * within the same application context and can be ordered using the {@link Ordered}\n * interface or {@link Order @Order} annotation.\n * <p>\n * If you need access to {@link ApplicationArguments} instead of the raw String array\n * consider using {@link ApplicationRunner}.\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see ApplicationRunner\n */",
            "/**\n * 标识 Bean 在 {@link SpringApplication} 中应被<em>执行</em>的接口。\n * <p>\n * 同一应用上下文中可定义多个 {@link CommandLineRunner} Bean，\n * 可通过 {@link Ordered} 接口或 {@link Order @Order} 注解排序。\n * 若需要 {@link ApplicationArguments} 而非原始字符串数组，请考虑使用 {@link ApplicationRunner}。\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see ApplicationRunner\n */",
        ),
        (
            "/**\n\t * Callback used to run the bean.\n\t * @param args incoming main method arguments\n\t * @throws Exception on error\n\t */",
            "/**\n\t * 执行 Bean 的回调方法。\n\t *\n\t * @param args 传入的 main 方法参数\n\t * @throws Exception 发生错误时\n\t */",
        ),
    ],
    "DefaultApplicationArguments.java": [
        (
            "/**\n * Default implementation of {@link ApplicationArguments}.\n *\n * @author Phillip Webb\n * @since 1.4.1\n */",
            "/**\n * {@link ApplicationArguments} 的默认实现。\n * <p>\n * 基于 {@link SimpleCommandLinePropertySource} 解析命令行参数，\n * 区分选项参数与非选项参数。\n *\n * @author Phillip Webb\n * @since 1.4.1\n */",
        ),
    ],
    "DefaultApplicationContextFactory.java": [
        (
            "/**\n * Default {@link ApplicationContextFactory} implementation that will create an\n * appropriate context for the {@link WebApplicationType}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 默认 {@link ApplicationContextFactory} 实现，\n * 将根据 {@link WebApplicationType} 创建合适的上下文。\n * <p>\n * 优先从 {@code META-INF/spring.factories} 加载自定义工厂；\n * 若无匹配结果，则创建 {@link AnnotationConfigApplicationContext}\n * 或 AOT 模式下的 {@link GenericApplicationContext}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "EnvironmentConverter.java": [
        (
            "/**\n * Utility class for converting one type of {@link Environment} to another.\n *\n * @author Ethan Rubinson\n * @author Andy Wilkinson\n * @author Madhura Bhave\n */",
            "/**\n * 将一种 {@link Environment} 类型转换为另一种的工具类。\n * <p>\n * 复制活动 Profile、转换服务及属性源，并在转换为 Servlet 环境时\n * 保留 Servlet 相关属性源。\n *\n * @author Ethan Rubinson\n * @author Andy Wilkinson\n * @author Madhura Bhave\n */",
        ),
        (
            "/**\n\t * Creates a new {@link EnvironmentConverter} that will use the given\n\t * {@code classLoader} during conversion.\n\t * @param classLoader the class loader to use\n\t */",
            "/**\n\t * 创建新的 {@link EnvironmentConverter}，转换过程中使用给定的 {@code classLoader}。\n\t *\n\t * @param classLoader 要使用的类加载器\n\t */",
        ),
        (
            "/**\n\t * Converts the given {@code environment} to the given {@link StandardEnvironment}\n\t * type. If the environment is already of the same type, no conversion is performed\n\t * and it is returned unchanged.\n\t * @param environment the Environment to convert\n\t * @param type the type to convert the Environment to\n\t * @return the converted Environment\n\t */",
            "/**\n\t * 将给定的 {@code environment} 转换为指定的 {@link StandardEnvironment} 类型。\n\t * 若环境已是相同类型，则不执行转换，直接返回原实例。\n\t *\n\t * @param environment 要转换的环境\n\t * @param type 目标环境类型\n\t * @return 转换后的环境\n\t */",
        ),
    ],
    "EnvironmentPostProcessor.java": [
        (
            "/**\n * Allows for customization of the application's {@link Environment} prior to the\n * application context being refreshed.\n * <p>\n * EnvironmentPostProcessor implementations have to be registered in\n * {@code META-INF/spring.factories}, using the fully qualified name of this class as the\n * key. Implementations may implement the {@link org.springframework.core.Ordered Ordered}\n * interface or use an {@link org.springframework.core.annotation.Order @Order} annotation\n * if they wish to be invoked in specific order.\n * <p>\n * {@code EnvironmentPostProcessor} implementations may optionally take the following\n * constructor parameters:\n * <ul>\n * <li>{@link DeferredLogFactory} - A factory that can be used to create loggers with\n * output deferred until the application has been fully prepared (allowing the environment\n * itself to configure logging levels).</li>\n * <li>{@link ConfigurableBootstrapContext} - A bootstrap context that can be used to\n * store objects that may be expensive to create, or need to be shared\n * ({@link BootstrapContext} or {@link BootstrapRegistry} may also be used).</li>\n * </ul>\n *\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @since 4.0.0\n */",
            "/**\n * 允许在应用上下文刷新之前自定义应用的 {@link Environment}。\n * <p>\n * {@code EnvironmentPostProcessor} 实现须在 {@code META-INF/spring.factories} 中注册，\n * 以本类全限定名作为键。若需按特定顺序调用，可实现\n * {@link org.springframework.core.Ordered Ordered} 接口或使用\n * {@link org.springframework.core.annotation.Order @Order} 注解。\n * <p>\n * 实现类可选地接受以下构造器参数：\n * <ul>\n * <li>{@link DeferredLogFactory} — 用于创建延迟输出日志的工厂，\n * 直到应用完全准备就绪后才输出（允许环境本身配置日志级别）。</li>\n * <li>{@link ConfigurableBootstrapContext} — 引导上下文，\n * 用于存储创建成本较高或需要共享的对象\n * （也可使用 {@link BootstrapContext} 或 {@link BootstrapRegistry}）。</li>\n * </ul>\n *\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @since 4.0.0\n */",
        ),
        (
            "/**\n\t * Post-process the given {@code environment}.\n\t * @param environment the environment to post-process\n\t * @param application the application to which the environment belongs\n\t */",
            "/**\n\t * 后处理给定的 {@code environment}。\n\t *\n\t * @param environment 要后处理的环境\n\t * @param application 所属的应用\n\t */",
        ),
    ],
    "ExitCodeEvent.java": [
        (
            "/**\n * Event fired when an application exit code has been determined from an\n * {@link ExitCodeGenerator}.\n *\n * @author Phillip Webb\n * @since 1.3.2\n */",
            "/**\n * 当从 {@link ExitCodeGenerator} 确定应用退出码时发布的事件。\n *\n * @author Phillip Webb\n * @since 1.3.2\n */",
        ),
        (
            "/**\n\t * Create a new {@link ExitCodeEvent} instance.\n\t * @param source the source of the event\n\t * @param exitCode the exit code\n\t */",
            "/**\n\t * 创建新的 {@link ExitCodeEvent} 实例。\n\t *\n\t * @param source 事件源\n\t * @param exitCode 退出码\n\t */",
        ),
        (
            "/**\n\t * Return the exit code that will be used to exit the JVM.\n\t * @return the exit code\n\t */",
            "/**\n\t * 返回将用于退出 JVM 的退出码。\n\t *\n\t * @return 退出码\n\t */",
        ),
    ],
    "ExitCodeExceptionMapper.java": [
        (
            "/**\n * Strategy interface that can be used to provide a mapping between exceptions and exit\n * codes.\n *\n * @author Phillip Webb\n * @since 1.3.2\n */",
            "/**\n * 提供异常与退出码之间映射的策略接口。\n *\n * @author Phillip Webb\n * @since 1.3.2\n */",
        ),
        (
            "/**\n\t * Returns the exit code that should be returned from the application.\n\t * @param exception the exception causing the application to exit\n\t * @return the exit code or {@code 0}.\n\t */",
            "/**\n\t * 返回应用应返回的退出码。\n\t *\n\t * @param exception 导致应用退出的异常\n\t * @return 退出码，或 {@code 0}\n\t */",
        ),
    ],
    "ExitCodeGenerator.java": [
        (
            "/**\n * Interface used to generate an 'exit code' from a running command line\n * {@link SpringApplication}. Can be used on exceptions as well as directly on beans.\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see SpringApplication#exit(org.springframework.context.ApplicationContext,\n * ExitCodeGenerator...)\n */",
            "/**\n * 从运行中的命令行 {@link SpringApplication} 生成「退出码」的接口。\n * 可用于异常处理，也可直接用于 Bean。\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see SpringApplication#exit(org.springframework.context.ApplicationContext,\n * ExitCodeGenerator...)\n */",
        ),
        (
            "/**\n\t * Returns the exit code that should be returned from the application.\n\t * @return the exit code.\n\t */",
            "/**\n\t * 返回应用应返回的退出码。\n\t *\n\t * @return 退出码\n\t */",
        ),
    ],
    "ExitCodeGenerators.java": [
        (
            "/**\n * Maintains an ordered collection of {@link ExitCodeGenerator} instances and allows the\n * final exit code to be calculated. Generators are ordered by {@link Order @Order} and\n * {@link Ordered}.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author GenKui Du\n * @see #getExitCode()\n * @see ExitCodeGenerator\n */",
            "/**\n * 维护有序的 {@link ExitCodeGenerator} 集合并计算最终退出码。\n * <p>\n * 生成器按 {@link Order @Order} 和 {@link Ordered} 排序。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author GenKui Du\n * @see #getExitCode()\n * @see ExitCodeGenerator\n */",
        ),
        (
            "/**\n\t * Get the final exit code that should be returned. The final exit code is the first\n\t * non-zero exit code that is {@link ExitCodeGenerator#getExitCode generated}.\n\t * @return the final exit code.\n\t */",
            "/**\n\t * 获取应返回的最终退出码。最终退出码为第一个\n\t * {@link ExitCodeGenerator#getExitCode 生成的} 非零退出码。\n\t *\n\t * @return 最终退出码\n\t */",
        ),
        (
            "/**\n\t * Adapts an {@link ExitCodeExceptionMapper} to an {@link ExitCodeGenerator}.\n\t */",
            "/**\n\t * 将 {@link ExitCodeExceptionMapper} 适配为 {@link ExitCodeGenerator}。\n\t */",
        ),
    ],
    "LazyInitializationBeanFactoryPostProcessor.java": [
        (
            "/**\n * {@link BeanFactoryPostProcessor} to set lazy-init on bean definitions that are not\n * {@link LazyInitializationExcludeFilter excluded} and have not already had a value\n * explicitly set.\n * <p>\n * Note that {@link SmartInitializingSingleton SmartInitializingSingletons} are\n * automatically excluded from lazy initialization to ensure that their\n * {@link SmartInitializingSingleton#afterSingletonsInstantiated() callback method} is\n * invoked.\n * <p>\n * Beans that are in the {@link BeanDefinition#ROLE_INFRASTRUCTURE infrastructure role}\n * are automatically excluded from lazy initialization, too.\n *\n * @author Andy Wilkinson\n * @author Madhura Bhave\n * @author Tyler Van Gorder\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 2.2.0\n * @see LazyInitializationExcludeFilter\n */",
            "/**\n * 对未被 {@link LazyInitializationExcludeFilter 排除} 且尚未显式设置值的\n * Bean 定义设置 {@code lazy-init} 的 {@link BeanFactoryPostProcessor}。\n * <p>\n * 注意：{@link SmartInitializingSingleton SmartInitializingSingletons} 会自动排除在\n * 懒加载之外，以确保其\n * {@link SmartInitializingSingleton#afterSingletonsInstantiated() 回调方法} 被调用。\n * <p>\n * 角色为 {@link BeanDefinition#ROLE_INFRASTRUCTURE 基础设施} 的 Bean\n * 也会自动排除在懒加载之外。\n *\n * @author Andy Wilkinson\n * @author Madhura Bhave\n * @author Tyler Van Gorder\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 2.2.0\n * @see LazyInitializationExcludeFilter\n */",
        ),
        (
            "/**\n\t * Excludes all {@link BeanDefinition bean definitions} which have the infrastructure\n\t * role from lazy initialization.\n\t */",
            "/**\n\t * 将所有具有基础设施角色的 {@link BeanDefinition Bean 定义}\n\t * 排除在懒加载之外。\n\t */",
        ),
    ],
    "LazyInitializationExcludeFilter.java": [
        (
            "/**\n * Filter that can be used to exclude beans definitions from having their\n * {@link AbstractBeanDefinition#setLazyInit(boolean) lazy-init} set by the\n * {@link LazyInitializationBeanFactoryPostProcessor}.\n * <p>\n * Primarily intended to allow downstream projects to deal with edge-cases in which it is\n * not easy to support lazy-loading (such as in DSLs that dynamically create additional\n * beans). Adding an instance of this filter to the application context can be used for\n * these edge cases.\n * <p>\n * A typical example would be something like this: <pre>\n * &#64;Bean\n * public static LazyInitializationExcludeFilter integrationLazyInitializationExcludeFilter() {\n *   return LazyInitializationExcludeFilter.forBeanTypes(IntegrationFlow.class);\n * }\n * </pre>\n * <p>\n * NOTE: Beans of this type will be instantiated very early in the spring application\n * lifecycle so they should generally be declared static and not have any dependencies.\n *\n * @author Tyler Van Gorder\n * @author Philip Webb\n * @since 2.2.0\n */",
            "/**\n * 用于将 Bean 定义排除在 {@link LazyInitializationBeanFactoryPostProcessor}\n * 自动设置 {@link AbstractBeanDefinition#setLazyInit(boolean) lazy-init} 之外的过滤器。\n * <p>\n * 主要供下游项目处理难以支持懒加载的边界情况\n * （例如在 DSL 中动态创建额外 Bean 的场景）。\n * 向应用上下文添加此过滤器的实例即可处理这些边界情况。\n * <p>\n * 典型示例如下： <pre>\n * &#64;Bean\n * public static LazyInitializationExcludeFilter integrationLazyInitializationExcludeFilter() {\n *   return LazyInitializationExcludeFilter.forBeanTypes(IntegrationFlow.class);\n * }\n * </pre>\n * <p>\n * 注意：此类型的 Bean 会在 Spring 应用生命周期极早期实例化，\n * 因此通常应声明为 static 且不依赖其他 Bean。\n *\n * @author Tyler Van Gorder\n * @author Philip Webb\n * @since 2.2.0\n */",
        ),
        (
            "/**\n\t * Returns {@code true} if the specified bean definition should be excluded from\n\t * having {@code lazy-init} automatically set.\n\t * @param beanName the bean name\n\t * @param beanDefinition the bean definition\n\t * @param beanType the bean type\n\t * @return {@code true} if {@code lazy-init} should not be automatically set\n\t */",
            "/**\n\t * 若指定的 Bean 定义应排除在自动设置 {@code lazy-init} 之外，则返回 {@code true}。\n\t *\n\t * @param beanName Bean 名称\n\t * @param beanDefinition Bean 定义\n\t * @param beanType Bean 类型\n\t * @return 若不应自动设置 {@code lazy-init} 则为 {@code true}\n\t */",
        ),
        (
            "/**\n\t * Factory method that creates a filter for the given bean types.\n\t * @param types the filtered types\n\t * @return a new filter instance\n\t */",
            "/**\n\t * 为给定 Bean 类型创建过滤器的工厂方法。\n\t *\n\t * @param types 要过滤的类型\n\t * @return 新的过滤器实例\n\t */",
        ),
    ],
    "ResourceBanner.java": [
        (
            "/**\n * Banner implementation that prints from a source text {@link Resource}.\n *\n * @author Phillip Webb\n * @author Vedran Pavic\n * @author Toshiaki Maki\n * @author Krzysztof Krason\n * @author Moritz Halbritter\n * @since 1.2.0\n */",
            "/**\n * 从文本 {@link Resource} 源输出 Banner 的实现。\n * <p>\n * 支持占位符解析（应用标题、版本、ANSI 颜色等），\n * 字符编码可通过 {@code spring.banner.charset} 配置。\n *\n * @author Phillip Webb\n * @author Vedran Pavic\n * @author Toshiaki Maki\n * @author Krzysztof Krason\n * @author Moritz Halbritter\n * @since 1.2.0\n */",
        ),
        (
            "/**\n\t * Return a mutable list of the {@link PropertyResolver} instances that will be used\n\t * to resolve placeholders.\n\t * @param environment the environment\n\t * @param sourceClass the source class\n\t * @return a mutable list of property resolvers\n\t */",
            "/**\n\t * 返回用于解析占位符的 {@link PropertyResolver} 实例的可变列表。\n\t *\n\t * @param environment 环境\n\t * @param sourceClass 源类\n\t * @return 属性解析器的可变列表\n\t */",
        ),
        (
            "/**\n\t * Return the application title that should be used for the source class. By default\n\t * will use {@link Package#getImplementationTitle()}.\n\t * @param sourceClass the source class\n\t * @return the application title\n\t */",
            "/**\n\t * 返回用于源类的应用标题。默认使用 {@link Package#getImplementationTitle()}。\n\t *\n\t * @param sourceClass 源类\n\t * @return 应用标题\n\t */",
        ),
        (
            "/**\n\t * Like {@link MapPropertySource}, but allows {@code null} as map values.\n\t */",
            "/**\n\t * 类似 {@link MapPropertySource}，但允许 map 值为 {@code null}。\n\t */",
        ),
    ],
    "Runner.java": [
        (
            "/**\n * Marker interface for runners.\n *\n * @author Tadaya Tsuyukubo\n * @see ApplicationRunner\n * @see CommandLineRunner\n */",
            "/**\n * Runner 的标记接口。\n *\n * @author Tadaya Tsuyukubo\n * @see ApplicationRunner\n * @see CommandLineRunner\n */",
        ),
    ],
    "SpringApplicationAotProcessor.java": [
        (
            "/**\n * Entry point for AOT processing of a {@link SpringApplication}.\n * <p>\n * <strong>For internal use only.</strong>\n *\n * @author Stephane Nicoll\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.0.0\n */",
            "/**\n * {@link SpringApplication} AOT 处理的入口点。\n * <p>\n * <strong>仅供内部使用。</strong>\n * <p>\n * 通过 {@link SpringApplicationHook} 调用应用的 {@code main} 方法，\n * 在上下文加载后立即捕获 {@link GenericApplicationContext} 并触发 AOT 代码生成。\n *\n * @author Stephane Nicoll\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.0.0\n */",
        ),
        (
            "/**\n\t * Create a new processor for the specified application and settings.\n\t * @param application the application main class\n\t * @param settings the general AOT processor settings\n\t * @param applicationArgs the arguments to provide to the main method\n\t */",
            "/**\n\t * 为指定应用和设置创建新的处理器。\n\t *\n\t * @param application 应用主类\n\t * @param settings 通用 AOT 处理器设置\n\t * @param applicationArgs 提供给 main 方法的参数\n\t */",
        ),
        (
            "/**\n\t * {@link SpringApplicationHook} used to capture the {@link ApplicationContext} and\n\t * trigger early exit of main method.\n\t */",
            "/**\n\t * 用于捕获 {@link ApplicationContext} 并触发 main 方法提前退出的\n\t * {@link SpringApplicationHook}。\n\t */",
        ),
    ],
    "SpringApplicationBannerPrinter.java": [
        (
            "/**\n * Class used by {@link SpringApplication} to print the application banner.\n *\n * @author Phillip Webb\n */",
            "/**\n * 供 {@link SpringApplication} 输出应用 Banner 的类。\n * <p>\n * 优先从 {@code spring.banner.location}（默认 {@code banner.txt}）加载文本 Banner，\n * 否则使用回退 Banner 或默认的 {@link SpringBootBanner}。\n *\n * @author Phillip Webb\n */",
        ),
        (
            "/**\n\t * Decorator that allows a {@link Banner} to be printed again without needing to\n\t * specify the source class.\n\t */",
            "/**\n\t * 装饰器，允许再次输出 {@link Banner} 而无需重新指定源类。\n\t */",
        ),
    ],
    "SpringApplicationHook.java": [
        (
            "/**\n * Low-level hook that can be used to attach a {@link SpringApplicationRunListener} to a\n * {@link SpringApplication} in order to observe or modify its behavior. Hooks are managed\n * on a per-thread basis providing isolation when multiple applications are executed in\n * parallel.\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.0.0\n * @see SpringApplication#withHook\n */",
            "/**\n * 低级钩子，用于将 {@link SpringApplicationRunListener} 附加到\n * {@link SpringApplication}，以观察或修改其行为。\n * <p>\n * Hook 按线程管理，在并行执行多个应用时提供隔离。\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.0.0\n * @see SpringApplication#withHook\n */",
        ),
        (
            "/**\n\t * Return the {@link SpringApplicationRunListener} that should be hooked into the\n\t * given {@link SpringApplication}.\n\t * @param springApplication the source {@link SpringApplication} instance\n\t * @return the {@link SpringApplicationRunListener} to attach or {@code null}\n\t */",
            "/**\n\t * 返回应挂接到给定 {@link SpringApplication} 的\n\t * {@link SpringApplicationRunListener}。\n\t *\n\t * @param springApplication 源 {@link SpringApplication} 实例\n\t * @return 要附加的 {@link SpringApplicationRunListener}，或 {@code null}\n\t */",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            text = apply_replacements(text, reps)
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
