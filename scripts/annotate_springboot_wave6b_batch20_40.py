#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-6b batch files [20:40]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][20:40]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "LoggingApplicationListener.java": [
        (
            "/**\n * An {@link ApplicationListener} that configures the {@link LoggingSystem}. If the\n * environment contains a {@code logging.config} property it will be used to bootstrap the\n * logging system, otherwise a default configuration is used. Regardless, logging levels\n * will be customized if the environment contains {@code logging.level.*} entries and\n * logging groups can be defined with {@code logging.group}.\n * <p>\n * Debug and trace logging for Spring, Tomcat, Jetty and Hibernate will be enabled when\n * the environment contains {@code debug} or {@code trace} properties that aren't set to\n * {@code \"false\"} (i.e. if you start your application using\n * {@literal java -jar myapp.jar [--debug | --trace]}). If you prefer to ignore these\n * properties you can set {@link #setParseArgs(boolean) parseArgs} to {@code false}.\n * <p>\n * By default, log output is only written to the console. If a log file is required, the\n * {@code logging.file.path} and {@code logging.file.name} properties can be used.\n * <p>\n * Some system properties may be set as side effects, and these can be useful if the\n * logging configuration supports placeholders (i.e. log4j or logback):\n * <ul>\n * <li>{@code LOG_FILE} is set to the value of path of the log file that should be written\n * (if any).</li>\n * <li>{@code PID} is set to the value of the current process ID if it can be determined.\n * </li>\n * </ul>\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Madhura Bhave\n * @author HaiTao Zhang\n * @since 2.0.0\n * @see LoggingSystem#get(ClassLoader)\n */",
            "/**\n * 配置 {@link LoggingSystem} 的 {@link ApplicationListener}。\n * 若环境中存在 {@code logging.config} 属性，则用它引导日志系统，否则使用默认配置。\n * 无论哪种情况，若环境包含 {@code logging.level.*} 条目则会定制日志级别，\n * 并可通过 {@code logging.group} 定义日志组。\n * <p>\n * 当环境中存在 {@code debug} 或 {@code trace} 属性且未设为 {@code \"false\"} 时\n * （例如使用 {@literal java -jar myapp.jar [--debug | --trace]} 启动应用），\n * 将为 Spring、Tomcat、Jetty 和 Hibernate 启用 debug/trace 日志。\n * 若要忽略这些属性，可将 {@link #setParseArgs(boolean) parseArgs} 设为 {@code false}。\n * <p>\n * 默认仅向控制台输出日志。若需日志文件，可使用 {@code logging.file.path}\n * 和 {@code logging.file.name} 属性。\n * <p>\n * 可能作为副作用设置一些系统属性，当日志配置支持占位符（如 log4j 或 logback）时很有用：\n * <ul>\n * <li>{@code LOG_FILE} 设为应写入的日志文件路径（若有）。</li>\n * <li>{@code PID} 设为当前进程 ID（若可确定）。</li>\n * </ul>\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Madhura Bhave\n * @author HaiTao Zhang\n * @since 2.0.0\n * @see LoggingSystem#get(ClassLoader)\n */",
        ),
        (
            "/**\n\t * The default order for the LoggingApplicationListener.\n\t */",
            "/**\n\t * LoggingApplicationListener 的默认顺序。\n\t */",
        ),
        (
            "/**\n\t * The name of the Spring property that contains a reference to the logging\n\t * configuration to load.\n\t */",
            "/**\n\t * 包含待加载日志配置引用的 Spring 属性名。\n\t */",
        ),
        (
            "/**\n\t * The name of the Spring property that controls the registration of a shutdown hook\n\t * to shut down the logging system when the JVM exits.\n\t * @see LoggingSystem#getShutdownHandler\n\t */",
            "/**\n\t * 控制是否在 JVM 退出时注册关闭钩子以关闭日志系统的 Spring 属性名。\n\t *\n\t * @see LoggingSystem#getShutdownHandler\n\t */",
        ),
        (
            "/**\n\t * The name of the {@link LoggingSystem} bean.\n\t */",
            "/**\n\t * {@link LoggingSystem} Bean 的名称。\n\t */",
        ),
        (
            "/**\n\t * The name of the {@link LogFile} bean.\n\t * @since 2.2.0\n\t */",
            "/**\n\t * {@link LogFile} Bean 的名称。\n\t *\n\t * @since 2.2.0\n\t */",
        ),
        (
            "/**\n\t * The name of the {@link LoggerGroups} bean.\n\t * @since 2.2.0\n\t */",
            "/**\n\t * {@link LoggerGroups} Bean 的名称。\n\t *\n\t * @since 2.2.0\n\t */",
        ),
        (
            "/**\n\t * The name of the {@link Lifecycle} bean used to handle cleanup.\n\t */",
            "/**\n\t * 用于处理清理的 {@link Lifecycle} Bean 的名称。\n\t */",
        ),
        (
            "/**\n\t * Initialize the logging system according to preferences expressed through the\n\t * {@link Environment} and the classpath.\n\t * @param environment the environment\n\t * @param classLoader the classloader\n\t */",
            "/**\n\t * 根据 {@link Environment} 和类路径表达的偏好初始化日志系统。\n\t *\n\t * @param environment 环境\n\t * @param classLoader 类加载器\n\t */",
        ),
        (
            "/**\n\t * Initialize loggers based on the {@link #setSpringBootLogging(LogLevel)\n\t * springBootLogging} setting. By default this implementation will pick an appropriate\n\t * set of loggers to configure based on the level.\n\t * @param system the logging system\n\t * @param springBootLogging the spring boot logging level requested\n\t * @since 2.2.0\n\t */",
            "/**\n\t * 根据 {@link #setSpringBootLogging(LogLevel) springBootLogging} 设置初始化日志记录器。\n\t * 默认实现会按级别选择合适的一组日志记录器进行配置。\n\t *\n\t * @param system 日志系统\n\t * @param springBootLogging 请求的 Spring Boot 日志级别\n\t * @since 2.2.0\n\t */",
        ),
        (
            "/**\n\t * Set logging levels based on relevant {@link Environment} properties.\n\t * @param system the logging system\n\t * @param environment the environment\n\t * @since 2.2.0\n\t */",
            "/**\n\t * 根据相关 {@link Environment} 属性设置日志级别。\n\t *\n\t * @param system 日志系统\n\t * @param environment 环境\n\t * @since 2.2.0\n\t */",
        ),
        (
            "/**\n\t * Sets a custom logging level to be used for Spring Boot and related libraries.\n\t * @param springBootLogging the logging level\n\t */",
            "/**\n\t * 设置用于 Spring Boot 及相关库的自定义日志级别。\n\t *\n\t * @param springBootLogging 日志级别\n\t */",
        ),
        (
            "/**\n\t * Sets if initialization arguments should be parsed for {@literal debug} and\n\t * {@literal trace} properties (usually defined from {@literal --debug} or\n\t * {@literal --trace} command line args). Defaults to {@code true}.\n\t * @param parseArgs if arguments should be parsed\n\t */",
            "/**\n\t * 设置是否解析初始化参数中的 {@literal debug} 和 {@literal trace} 属性\n\t * （通常来自 {@literal --debug} 或 {@literal --trace} 命令行参数）。默认为 {@code true}。\n\t *\n\t * @param parseArgs 是否解析参数\n\t */",
        ),
    ],
    "BufferedStartupStep.java": [
        (
            "/**\n * {@link StartupStep} implementation to be buffered by\n * {@link BufferingApplicationStartup}. Its processing time is recorded using\n * {@link System#nanoTime()}.\n *\n * @author Brian Clozel\n * @author Phillip Webb\n */",
            "/**\n * 由 {@link BufferingApplicationStartup} 缓冲的 {@link StartupStep} 实现。\n * 使用 {@link System#nanoTime()} 记录处理时间。\n *\n * @author Brian Clozel\n * @author Phillip Webb\n */",
        ),
    ],
    "BufferingApplicationStartup.java": [
        (
            "/**\n * {@link ApplicationStartup} implementation that buffers {@link StartupStep steps} and\n * records their timestamp as well as their processing time.\n * <p>\n * Once recording has been {@link #startRecording() started}, steps are buffered up until\n * the configured {@link #BufferingApplicationStartup(int) capacity}; after that, new\n * steps are not recorded.\n * <p>\n * There are several ways to keep the buffer size low:\n * <ul>\n * <li>configuring {@link #addFilter(Predicate) filters} to only record steps that are\n * relevant to us.\n * <li>{@link #drainBufferedTimeline() draining} the buffered steps.\n * </ul>\n *\n * @author Brian Clozel\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 缓冲 {@link StartupStep 启动步骤} 并记录时间戳与处理时间的 {@link ApplicationStartup} 实现。\n * <p>\n * {@link #startRecording() 开始录制} 后，步骤会缓冲至配置的\n * {@link #BufferingApplicationStartup(int) 容量}；超出后不再记录新步骤。\n * <p>\n * 保持缓冲区较小的方式：\n * <ul>\n * <li>配置 {@link #addFilter(Predicate) 过滤器}，仅记录相关步骤。</li>\n * <li>{@link #drainBufferedTimeline() 排空} 已缓冲步骤。</li>\n * </ul>\n *\n * @author Brian Clozel\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Create a new buffered {@link ApplicationStartup} with a limited capacity and starts\n\t * the recording of steps.\n\t * @param capacity the configured capacity; once reached, new steps are not recorded.\n\t */",
            "/**\n\t * 创建容量受限的缓冲 {@link ApplicationStartup} 并开始录制步骤。\n\t *\n\t * @param capacity 配置的容量；达到后不再记录新步骤\n\t */",
        ),
        (
            "/**\n\t * Start the recording of steps and mark the beginning of the {@link StartupTimeline}.\n\t * The class constructor already implicitly calls this, but it is possible to reset it\n\t * as long as steps have not been recorded already.\n\t * @throws IllegalStateException if called and {@link StartupStep} have been recorded\n\t * already.\n\t */",
            "/**\n\t * 开始录制步骤并标记 {@link StartupTimeline} 的起点。\n\t * 构造器已隐式调用此方法，但在尚未录制步骤时可重置。\n\t *\n\t * @throws IllegalStateException 若已录制 {@link StartupStep} 后再次调用\n\t */",
        ),
        (
            "/**\n\t * Add a predicate filter to the list of existing ones.\n\t * <p>\n\t * A {@link StartupStep step} that doesn't match all filters will not be recorded.\n\t * @param filter the predicate filter to add.\n\t */",
            "/**\n\t * 向现有过滤器列表添加谓词过滤器。\n\t * <p>\n\t * 不匹配所有过滤器的 {@link StartupStep 步骤} 不会被记录。\n\t *\n\t * @param filter 要添加的谓词过滤器\n\t */",
        ),
        (
            "/**\n\t * Return the {@link StartupTimeline timeline} as a snapshot of currently buffered\n\t * steps.\n\t * <p>\n\t * This will not remove steps from the buffer, see {@link #drainBufferedTimeline()}\n\t * for its counterpart.\n\t * @return a snapshot of currently buffered steps.\n\t */",
            "/**\n\t * 以当前缓冲步骤的快照形式返回 {@link StartupTimeline 时间线}。\n\t * <p>\n\t * 不会从缓冲区移除步骤；对应的可移除操作见 {@link #drainBufferedTimeline()}。\n\t *\n\t * @return 当前缓冲步骤的快照\n\t */",
        ),
        (
            "/**\n\t * Return the {@link StartupTimeline timeline} by pulling steps from the buffer.\n\t * <p>\n\t * This removes steps from the buffer, see {@link #getBufferedTimeline()} for its\n\t * read-only counterpart.\n\t * @return buffered steps drained from the buffer.\n\t */",
            "/**\n\t * 从缓冲区取出步骤并返回 {@link StartupTimeline 时间线}。\n\t * <p>\n\t * 会从缓冲区移除步骤；只读对应方法见 {@link #getBufferedTimeline()}。\n\t *\n\t * @return 从缓冲区排空后的步骤\n\t */",
        ),
    ],
    "StartupTimeline.java": [
        (
            "/**\n * Represent the timeline of {@link StartupStep steps} recorded by\n * {@link BufferingApplicationStartup}. Each {@link TimelineEvent} has a start and end\n * time as well as a duration measured with nanosecond precision.\n *\n * @author Brian Clozel\n * @since 2.4.0\n */",
            "/**\n * 表示 {@link BufferingApplicationStartup} 录制的 {@link StartupStep 步骤} 时间线。\n * 每个 {@link TimelineEvent} 具有起止时间及纳秒精度的持续时间。\n *\n * @author Brian Clozel\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Return the start time of this timeline.\n\t * @return the start time\n\t */",
            "/**\n\t * 返回此时间线的开始时间。\n\t *\n\t * @return 开始时间\n\t */",
        ),
        (
            "/**\n\t * Return the recorded events.\n\t * @return the events\n\t */",
            "/**\n\t * 返回已录制的事件。\n\t *\n\t * @return 事件列表\n\t */",
        ),
        (
            "/**\n\t * Event on the current {@link StartupTimeline}. Each event has a start/end time, a\n\t * precise duration and the complete {@link StartupStep} information associated with\n\t * it.\n\t */",
            "/**\n\t * 当前 {@link StartupTimeline} 上的事件。\n\t * 每个事件具有起止时间、精确持续时间及关联的完整 {@link StartupStep} 信息。\n\t */",
        ),
        (
            "/**\n\t\t * Return the start time of this event.\n\t\t * @return the start time\n\t\t */",
            "/**\n\t\t * 返回此事件的开始时间。\n\t\t *\n\t\t * @return 开始时间\n\t\t */",
        ),
        (
            "/**\n\t\t * Return the end time of this event.\n\t\t * @return the end time\n\t\t */",
            "/**\n\t\t * 返回此事件的结束时间。\n\t\t *\n\t\t * @return 结束时间\n\t\t */",
        ),
        (
            "/**\n\t\t * Return the duration of this event, i.e. the processing time of the associated\n\t\t * {@link StartupStep} with nanoseconds precision.\n\t\t * @return the event duration\n\t\t */",
            "/**\n\t\t * 返回此事件的持续时间，即关联 {@link StartupStep} 的处理时间（纳秒精度）。\n\t\t *\n\t\t * @return 事件持续时间\n\t\t */",
        ),
        (
            "/**\n\t\t * Return the {@link StartupStep} information for this event.\n\t\t * @return the step information.\n\t\t */",
            "/**\n\t\t * 返回此事件对应的 {@link StartupStep} 信息。\n\t\t *\n\t\t * @return 步骤信息\n\t\t */",
        ),
    ],
    "BindMethodAttribute.java": [
        (
            "/**\n * Allows a {@link BindMethod} value to be stored and retrieved from an\n * {@link AttributeAccessor}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 允许在 {@link AttributeAccessor} 中存储和读取 {@link BindMethod} 值。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "BoundConfigurationProperties.java": [
        (
            "/**\n * Bean to record and provide bound\n * {@link ConfigurationProperties @ConfigurationProperties}.\n *\n * @author Madhura Bhave\n * @since 2.3.0\n */",
            "/**\n * 记录并提供已绑定的 {@link ConfigurationProperties @ConfigurationProperties} 的 Bean。\n *\n * @author Madhura Bhave\n * @since 2.3.0\n */",
        ),
        (
            "/**\n\t * The bean name that this class is registered with.\n\t */",
            "/**\n\t * 注册此类的 Bean 名称。\n\t */",
        ),
        (
            "/**\n\t * Get the configuration property bound to the given name.\n\t * @param name the property name\n\t * @return the bound property or {@code null}\n\t */",
            "/**\n\t * 获取绑定到给定名称的配置属性。\n\t *\n\t * @param name 属性名\n\t * @return 已绑定属性，或 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Get all bound properties.\n\t * @return a map of all bound properties\n\t */",
            "/**\n\t * 获取所有已绑定属性。\n\t *\n\t * @return 所有已绑定属性的映射\n\t */",
        ),
        (
            "/**\n\t * Return the {@link BoundConfigurationProperties} from the given\n\t * {@link ApplicationContext} if it is available.\n\t * @param context the context to search\n\t * @return a {@link BoundConfigurationProperties} or {@code null}\n\t */",
            "/**\n\t * 若可用，从给定 {@link ApplicationContext} 返回 {@link BoundConfigurationProperties}。\n\t *\n\t * @param context 要搜索的上下文\n\t * @return {@link BoundConfigurationProperties} 实例，或 {@code null}\n\t */",
        ),
    ],
    "ConfigurationProperties.java": [
        (
            "/**\n * Annotation for externalized configuration. Add this to a class definition or a\n * {@code @Bean} method in a {@code @Configuration} class if you want to bind and validate\n * some external Properties (e.g. from a .properties file).\n * <p>\n * Binding is either performed by calling setters on the annotated class or, if\n * {@link ConstructorBinding @ConstructorBinding} is in use, by binding to the constructor\n * parameters.\n * <p>\n * Note that contrary to {@code @Value}, SpEL expressions are not evaluated since property\n * values are externalized.\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see ConfigurationPropertiesScan\n * @see ConstructorBinding\n * @see ConfigurationPropertiesBindingPostProcessor\n * @see EnableConfigurationProperties\n */",
            "/**\n * 外部化配置的注解。若要将外部属性（如 .properties 文件）绑定并校验到对象上，\n * 可将其添加到类定义或 {@code @Configuration} 类中的 {@code @Bean} 方法。\n * <p>\n * 绑定方式：调用被注解类的 setter；若使用 {@link ConstructorBinding @ConstructorBinding}，\n * 则绑定到构造器参数。\n * <p>\n * 与 {@code @Value} 不同，属性值已外部化，因此不会求值 SpEL 表达式。\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see ConfigurationPropertiesScan\n * @see ConstructorBinding\n * @see ConfigurationPropertiesBindingPostProcessor\n * @see EnableConfigurationProperties\n */",
        ),
        (
            "/**\n\t * The prefix of the properties that are valid to bind to this object. Synonym for\n\t * {@link #prefix()}. A valid prefix is defined by one or more words separated with\n\t * dots (e.g. {@code \"acme.system.feature\"}).\n\t * @return the prefix of the properties to bind\n\t */",
            "/**\n\t * 可绑定到此对象的有效属性前缀，{@link #prefix()} 的同义词。\n\t * 有效前缀由一个或多个以点分隔的单词组成（如 {@code \"acme.system.feature\"}）。\n\t *\n\t * @return 要绑定的属性前缀\n\t */",
        ),
        (
            "/**\n\t * The prefix of the properties that are valid to bind to this object. Synonym for\n\t * {@link #value()}. A valid prefix is defined by one or more words separated with\n\t * dots (e.g. {@code \"acme.system.feature\"}).\n\t * @return the prefix of the properties to bind\n\t */",
            "/**\n\t * 可绑定到此对象的有效属性前缀，{@link #value()} 的同义词。\n\t * 有效前缀由一个或多个以点分隔的单词组成（如 {@code \"acme.system.feature\"}）。\n\t *\n\t * @return 要绑定的属性前缀\n\t */",
        ),
        (
            "/**\n\t * Flag to indicate that when binding to this object invalid fields should be ignored.\n\t * Invalid means invalid according to the binder that is used, and usually this means\n\t * fields of the wrong type (or that cannot be coerced into the correct type).\n\t * @return the flag value (default false)\n\t */",
            "/**\n\t * 绑定到此对象时是否忽略无效字段。\n\t * “无效”指所用绑定器认为无效，通常指类型错误或无法强制转换为正确类型的字段。\n\t *\n\t * @return 标志值（默认 false）\n\t */",
        ),
        (
            "/**\n\t * Flag to indicate that when binding to this object unknown fields should be ignored.\n\t * An unknown field could be a sign of a mistake in the Properties.\n\t * @return the flag value (default true)\n\t */",
            "/**\n\t * 绑定到此对象时是否忽略未知字段。\n\t * 未知字段可能是 Properties 配置错误的信号。\n\t *\n\t * @return 标志值（默认 true）\n\t */",
        ),
    ],
    "ConfigurationPropertiesBean.java": [
        (
            "/**\n * Provides access to {@link ConfigurationProperties @ConfigurationProperties} bean\n * details, regardless of if the annotation was used directly or on a {@link Bean @Bean}\n * factory method. This class can be used to access {@link #getAll(ApplicationContext)\n * all} configuration properties beans in an ApplicationContext, or\n * {@link #get(ApplicationContext, Object, String) individual beans} on a case-by-case\n * basis (for example, in a {@link BeanPostProcessor}).\n *\n * @author Phillip Webb\n * @since 2.2.0\n * @see #getAll(ApplicationContext)\n * @see #get(ApplicationContext, Object, String)\n */",
            "/**\n * 提供对 {@link ConfigurationProperties @ConfigurationProperties} Bean 详情的访问，\n * 无论注解是直接标注在类上还是 {@link Bean @Bean} 工厂方法上。\n * 可用于访问 ApplicationContext 中 {@link #getAll(ApplicationContext) 全部}\n * 配置属性 Bean，或在 {@link BeanPostProcessor} 等场景下\n * {@link #get(ApplicationContext, Object, String) 逐个} 访问。\n *\n * @author Phillip Webb\n * @since 2.2.0\n * @see #getAll(ApplicationContext)\n * @see #get(ApplicationContext, Object, String)\n */",
        ),
        (
            "/**\n\t * Return the name of the Spring bean.\n\t * @return the bean name\n\t */",
            "/**\n\t * 返回 Spring Bean 的名称。\n\t *\n\t * @return Bean 名称\n\t */",
        ),
        (
            "/**\n\t * Return the actual Spring bean instance.\n\t * @return the bean instance\n\t */",
            "/**\n\t * 返回实际的 Spring Bean 实例。\n\t *\n\t * @return Bean 实例\n\t */",
        ),
        (
            "/**\n\t * Return the bean type.\n\t * @return the bean type\n\t */",
            "/**\n\t * 返回 Bean 类型。\n\t *\n\t * @return Bean 类型\n\t */",
        ),
        (
            "/**\n\t * Return the {@link ConfigurationProperties} annotation for the bean. The annotation\n\t * may be defined on the bean itself or from the factory method that create the bean\n\t * (usually a {@link Bean @Bean} method).\n\t * @return the configuration properties annotation\n\t */",
            "/**\n\t * 返回 Bean 的 {@link ConfigurationProperties} 注解。\n\t * 注解可能定义在 Bean 本身，或创建 Bean 的工厂方法（通常是 {@link Bean @Bean} 方法）上。\n\t *\n\t * @return 配置属性注解\n\t */",
        ),
        (
            "/**\n\t * Return a {@link Bindable} instance suitable that can be used as a target for the\n\t * {@link Binder}.\n\t * @return a bind target for use with the {@link Binder}\n\t */",
            "/**\n\t * 返回可用作 {@link Binder} 绑定目标的 {@link Bindable} 实例。\n\t *\n\t * @return 供 {@link Binder} 使用的绑定目标\n\t */",
        ),
        (
            "/**\n\t * Return all {@link ConfigurationProperties @ConfigurationProperties} beans contained\n\t * in the given application context. Both directly annotated beans, as well as beans\n\t * that have {@link ConfigurationProperties @ConfigurationProperties} annotated\n\t * factory methods are included.\n\t * @param applicationContext the source application context\n\t * @return a map of all configuration properties beans keyed by the bean name\n\t */",
            "/**\n\t * 返回给定应用上下文中所有 {@link ConfigurationProperties @ConfigurationProperties} Bean。\n\t * 包括直接标注的 Bean 以及工厂方法上标注 {@link ConfigurationProperties @ConfigurationProperties} 的 Bean。\n\t *\n\t * @param applicationContext 源应用上下文\n\t * @return 以 Bean 名称为键的所有配置属性 Bean 映射\n\t */",
        ),
        (
            "/**\n\t * Return a {@link ConfigurationPropertiesBean @ConfigurationPropertiesBean} instance\n\t * for the given bean details or {@code null} if the bean is not a\n\t * {@link ConfigurationProperties @ConfigurationProperties} object. Annotations are\n\t * considered both on the bean itself, as well as any factory method (for example a\n\t * {@link Bean @Bean} method).\n\t * @param applicationContext the source application context\n\t * @param bean the bean to consider\n\t * @param beanName the bean name\n\t * @return a configuration properties bean or {@code null} if the neither the bean nor\n\t * factory method are annotated with\n\t * {@link ConfigurationProperties @ConfigurationProperties}\n\t */",
            "/**\n\t * 为给定 Bean 详情返回 {@link ConfigurationPropertiesBean} 实例；\n\t * 若 Bean 不是 {@link ConfigurationProperties @ConfigurationProperties} 对象则返回 {@code null}。\n\t * 会检查 Bean 本身及工厂方法（如 {@link Bean @Bean} 方法）上的注解。\n\t *\n\t * @param applicationContext 源应用上下文\n\t * @param bean 待考虑的 Bean\n\t * @param beanName Bean 名称\n\t * @return 配置属性 Bean，若 Bean 与工厂方法均未标注\n\t * {@link ConfigurationProperties @ConfigurationProperties} 则为 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Deduce the {@code BindMethod} that should be used for the given type.\n\t * @param type the source type\n\t * @return the bind method to use\n\t */",
            "/**\n\t * 推断给定类型应使用的 {@code BindMethod}。\n\t *\n\t * @param type 源类型\n\t * @return 要使用的绑定方法\n\t */",
        ),
        (
            "/**\n\t * Deduce the {@code BindMethod} that should be used for the given {@link Bindable}.\n\t * @param bindable the source bindable\n\t * @return the bind method to use\n\t */",
            "/**\n\t * 推断给定 {@link Bindable} 应使用的 {@code BindMethod}。\n\t *\n\t * @param bindable 源 Bindable\n\t * @return 要使用的绑定方法\n\t */",
        ),
    ],
    "ConfigurationPropertiesBeanFactoryInitializationAotProcessor.java": [
        (
            "/**\n * {@link BeanFactoryInitializationAotProcessor} that contributes runtime hints for\n * configuration properties-annotated beans.\n *\n * @author Stephane Nicoll\n * @author Christoph Strobl\n * @author Sebastien Deleuze\n * @author Andy Wilkinson\n */",
            "/**\n * 为标注配置属性的 Bean 贡献运行时提示的 {@link BeanFactoryInitializationAotProcessor}。\n *\n * @author Stephane Nicoll\n * @author Christoph Strobl\n * @author Sebastien Deleuze\n * @author Andy Wilkinson\n */",
        ),
    ],
    "ConfigurationPropertiesBeanRegistrar.java": [
        (
            "/**\n * Delegate used by {@link EnableConfigurationPropertiesRegistrar} and\n * {@link ConfigurationPropertiesScanRegistrar} to register a bean definition for a\n * {@link ConfigurationProperties @ConfigurationProperties} class.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @author Yanming Zhou\n */",
            "/**\n * 由 {@link EnableConfigurationPropertiesRegistrar} 和 {@link ConfigurationPropertiesScanRegistrar}\n * 使用的委托，为 {@link ConfigurationProperties @ConfigurationProperties} 类注册 Bean 定义。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @author Yanming Zhou\n */",
        ),
    ],
    "ConfigurationPropertiesBeanRegistrationAotProcessor.java": [
        (
            "/**\n * {@link BeanRegistrationAotProcessor} for immutable configuration properties.\n *\n * @author Stephane Nicoll\n * @see ConstructorBound\n */",
            "/**\n * 用于不可变配置属性的 {@link BeanRegistrationAotProcessor}。\n *\n * @author Stephane Nicoll\n * @see ConstructorBound\n */",
        ),
    ],
    "ConfigurationPropertiesBindException.java": [
        (
            "/**\n * Exception thrown when {@link ConfigurationProperties @ConfigurationProperties} binding\n * fails.\n *\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @since 2.0.0\n */",
            "/**\n * {@link ConfigurationProperties @ConfigurationProperties} 绑定失败时抛出的异常。\n *\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * Return the bean type that was being bound.\n\t * @return the bean type\n\t */",
            "/**\n\t * 返回正在绑定的 Bean 类型。\n\t *\n\t * @return Bean 类型\n\t */",
        ),
        (
            "/**\n\t * Return the configuration properties annotation that triggered the binding.\n\t * @return the configuration properties annotation\n\t */",
            "/**\n\t * 返回触发绑定的配置属性注解。\n\t *\n\t * @return 配置属性注解\n\t */",
        ),
    ],
    "ConfigurationPropertiesBindHandlerAdvisor.java": [
        (
            "/**\n * Allows additional functionality to be applied to the {@link BindHandler} used by the\n * {@link ConfigurationPropertiesBindingPostProcessor}.\n *\n * @author Phillip Webb\n * @since 2.1.0\n * @see AbstractBindHandler\n */",
            "/**\n * 允许对 {@link ConfigurationPropertiesBindingPostProcessor} 使用的 {@link BindHandler}\n * 应用额外功能。\n *\n * @author Phillip Webb\n * @since 2.1.0\n * @see AbstractBindHandler\n */",
        ),
        (
            "/**\n\t * Apply additional functionality to the source bind handler.\n\t * @param bindHandler the source bind handler\n\t * @return a replacement bind handler that delegates to the source and provides\n\t * additional functionality\n\t */",
            "/**\n\t * 对源绑定处理器应用额外功能。\n\t *\n\t * @param bindHandler 源绑定处理器\n\t * @return 委托源处理器并提供额外功能的替换绑定处理器\n\t */",
        ),
    ],
    "ConfigurationPropertiesBinder.java": [
        (
            "/**\n * Internal class used by the {@link ConfigurationPropertiesBindingPostProcessor} to\n * handle the actual {@link ConfigurationProperties @ConfigurationProperties} binding.\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n */",
            "/**\n * {@link ConfigurationPropertiesBindingPostProcessor} 用于执行实际\n * {@link ConfigurationProperties @ConfigurationProperties} 绑定的内部类。\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n */",
        ),
        (
            "/**\n\t * {@link BindHandler} to deal with\n\t * {@link ConfigurationProperties @ConfigurationProperties} concerns.\n\t */",
            "/**\n\t * 处理 {@link ConfigurationProperties @ConfigurationProperties} 相关问题的 {@link BindHandler}。\n\t */",
        ),
        (
            "/**\n\t * {@link FactoryBean} to create the {@link ConfigurationPropertiesBinder}.\n\t */",
            "/**\n\t * 创建 {@link ConfigurationPropertiesBinder} 的 {@link FactoryBean}。\n\t */",
        ),
        (
            "/**\n\t * A {@code Validator} for a constructor-bound {@code Bindable} where the type being\n\t * bound is itself a {@code Validator} implementation.\n\t */",
            "/**\n\t * 用于构造器绑定 {@code Bindable} 的 {@code Validator}，\n\t * 其中被绑定类型本身实现了 {@code Validator}。\n\t */",
        ),
    ],
    "ConfigurationPropertiesBinding.java": [
        (
            "/**\n * Qualifier for beans that are needed to configure the binding of\n * {@link ConfigurationProperties @ConfigurationProperties} (e.g. Converters).\n * <p>\n * {@link Bean @Bean} methods that declare a {@code @ConfigurationPropertiesBinding} bean\n * should be {@code static} to ensure that \"bean is not eligible for getting processed by\n * all BeanPostProcessors\" warnings are not produced.\n *\n * @author Dave Syer\n * @since 1.3.0\n */",
            "/**\n * 用于配置 {@link ConfigurationProperties @ConfigurationProperties} 绑定所需 Bean\n * （如 Converter）的限定符。\n * <p>\n * 声明 {@code @ConfigurationPropertiesBinding} Bean 的 {@link Bean @Bean} 方法应为 {@code static}，\n * 以避免产生“bean is not eligible for getting processed by all BeanPostProcessors”警告。\n *\n * @author Dave Syer\n * @since 1.3.0\n */",
        ),
        (
            "/**\n\t * Concrete value for the {@link Qualifier @Qualifier}.\n\t */",
            "/**\n\t * {@link Qualifier @Qualifier} 的具体值。\n\t */",
        ),
    ],
    "ConfigurationPropertiesBindingPostProcessor.java": [
        (
            "/**\n * {@link BeanPostProcessor} to bind {@link PropertySources} to beans annotated with\n * {@link ConfigurationProperties @ConfigurationProperties}.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Christian Dupuis\n * @author Stephane Nicoll\n * @author Madhura Bhave\n * @since 1.0.0\n */",
            "/**\n * 将 {@link PropertySources} 绑定到标注 {@link ConfigurationProperties @ConfigurationProperties}\n * 的 Bean 的 {@link BeanPostProcessor}。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Christian Dupuis\n * @author Stephane Nicoll\n * @author Madhura Bhave\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * The bean name that this post-processor is registered with.\n\t */",
            "/**\n\t * 注册此后置处理器的 Bean 名称。\n\t */",
        ),
        (
            "/**\n\t * Register a {@link ConfigurationPropertiesBindingPostProcessor} bean if one is not\n\t * already registered.\n\t * @param registry the bean definition registry\n\t * @since 2.2.0\n\t */",
            "/**\n\t * 若尚未注册，则注册 {@link ConfigurationPropertiesBindingPostProcessor} Bean。\n\t *\n\t * @param registry Bean 定义注册表\n\t * @since 2.2.0\n\t */",
        ),
    ],
    "ConfigurationPropertiesCharSequenceToObjectConverter.java": [
        (
            "/**\n * Copy of package-private\n * {@code org.springframework.boot.convert.CharSequenceToObjectConverter}, renamed for\n * differentiation.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n */",
            "/**\n * 包私有类 {@code org.springframework.boot.convert.CharSequenceToObjectConverter} 的副本，\n * 重命名以作区分。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n */",
        ),
        (
            "/**\n\t * Return if String based conversion is better based on the target type. This is\n\t * required when ObjectTo... conversion produces incorrect results.\n\t * @param sourceType the source type to test\n\t * @param targetType the target type to test\n\t * @return if string conversion is better\n\t */",
            "/**\n\t * 根据目标类型判断是否基于 String 的转换更优。\n\t * 当 ObjectTo... 转换产生错误结果时需要此判断。\n\t *\n\t * @param sourceType 待测试的源类型\n\t * @param targetType 待测试的目标类型\n\t * @return 若 String 转换更优则为 {@code true}\n\t */",
        ),
    ],
    "ConfigurationPropertiesJsr303Validator.java": [
        (
            "/**\n * Validator that supports configuration classes annotated with\n * {@link Validated @Validated}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 支持标注 {@link Validated @Validated} 的配置类的校验器。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "ConfigurationPropertiesScan.java": [
        (
            "/**\n * Configures the base packages used when scanning for\n * {@link ConfigurationProperties @ConfigurationProperties} classes. One of\n * {@link #basePackageClasses()}, {@link #basePackages()} or its alias {@link #value()}\n * may be specified to define specific packages to scan. If specific packages are not\n * defined scanning will occur from the package of the class with this annotation.\n * <p>\n * Note: Classes annotated or meta-annotated with {@link Component @Component} will not be\n * picked up by this annotation.\n *\n * @author Madhura Bhave\n * @since 2.2.0\n */",
            "/**\n * 配置扫描 {@link ConfigurationProperties @ConfigurationProperties} 类时使用的基础包。\n * 可通过 {@link #basePackageClasses()}、{@link #basePackages()} 或其别名 {@link #value()}\n * 指定要扫描的包；未指定时从标注此注解的类所在包开始扫描。\n * <p>\n * 注意：标注或元标注 {@link Component @Component} 的类不会被此注解拾取。\n *\n * @author Madhura Bhave\n * @since 2.2.0\n */",
        ),
        (
            "/**\n\t * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation\n\t * declarations e.g.: {@code @ConfigurationPropertiesScan(\"org.my.pkg\")} instead of\n\t * {@code @ConfigurationPropertiesScan(basePackages=\"org.my.pkg\")}.\n\t * @return the base packages to scan\n\t */",
            "/**\n\t * {@link #basePackages()} 属性的别名，使注解声明更简洁，例如：\n\t * {@code @ConfigurationPropertiesScan(\"org.my.pkg\")} 而非\n\t * {@code @ConfigurationPropertiesScan(basePackages=\"org.my.pkg\")}。\n\t *\n\t * @return 要扫描的基础包\n\t */",
        ),
        (
            "/**\n\t * Base packages to scan for configuration properties. {@link #value()} is an alias\n\t * for (and mutually exclusive with) this attribute.\n\t * <p>\n\t * Use {@link #basePackageClasses()} for a type-safe alternative to String-based\n\t * package names.\n\t * @return the base packages to scan\n\t */",
            "/**\n\t * 扫描配置属性的基础包。{@link #value()} 是此属性的别名（且互斥）。\n\t * <p>\n\t * 可使用 {@link #basePackageClasses()} 作为基于字符串包名的类型安全替代。\n\t *\n\t * @return 要扫描的基础包\n\t */",
        ),
        (
            "/**\n\t * Type-safe alternative to {@link #basePackages()} for specifying the packages to\n\t * scan for configuration properties. The package of each class specified will be\n\t * scanned.\n\t * <p>\n\t * Consider creating a special no-op marker class or interface in each package that\n\t * serves no purpose other than being referenced by this attribute.\n\t * @return classes from the base packages to scan\n\t */",
            "/**\n\t * 指定扫描配置属性包的类型安全替代 {@link #basePackages()}。\n\t * 将扫描每个指定类所在的包。\n\t * <p>\n\t * 可在每个包中创建仅被此属性引用的无操作标记类或接口。\n\t *\n\t * @return 要扫描的基础包中的类\n\t */",
        ),
    ],
    "ConfigurationPropertiesScanRegistrar.java": [
        (
            "/**\n * {@link ImportBeanDefinitionRegistrar} for registering\n * {@link ConfigurationProperties @ConfigurationProperties} bean definitions through\n * scanning.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n */",
            "/**\n * 通过扫描注册 {@link ConfigurationProperties @ConfigurationProperties} Bean 定义的\n * {@link ImportBeanDefinitionRegistrar}。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n */",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:160]}...")
        text = text.replace(old, new, 1)
    return text


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def mark_queue_done(files: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    pending = [ln.strip() for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    pending_set = set(pending)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
        pending_set.discard(rel)
    done_path.write_text(("\n".join(done) + ("\n" if done else "")), encoding="utf-8")
    pending = [ln for ln in pending if ln in pending_set]
    pending_path.write_text(("\n".join(pending) + ("\n" if pending else "")), encoding="utf-8")
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["done"] = len(done)
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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
        if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
            shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            if has_chinese(text):
                cn_lines = len(re.findall(r"[\u4e00-\u9fff]", text))
                if cn_lines > 20:
                    ok += 1
                    print(f"SKIP(already CN) {rel}")
                    continue
            text = apply_replacements(text, reps)
            if not has_chinese(text):
                failures.append(f"NO_CHINESE_AFTER: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if not failures:
        mark_queue_done(BATCH_FILES)
        print("Marked 20 files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
