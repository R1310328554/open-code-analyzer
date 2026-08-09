#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-4 slice [0:20]."""
from __future__ import annotations

import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][:20]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "OnBeanCondition.java": [
        (
            "/**\n * {@link Condition} that checks for the presence or absence of specific beans.\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Jakub Kubrynski\n * @author Stephane Nicoll\n * @author Andy Wilkinson\n * @author Uladzislau Seuruk\n * @see ConditionalOnBean\n * @see ConditionalOnMissingBean\n * @see ConditionalOnSingleCandidate\n */",
            "/**\n * 检查特定 Bean 是否存在或不存在的 {@link Condition}。\n * <p>\n * 支持 {@link ConditionalOnBean}、{@link ConditionalOnMissingBean} 与\n * {@link ConditionalOnSingleCandidate}，按类型、名称或注解在 BeanFactory 中检索匹配。\n * 在 {@link ConfigurationPhase#REGISTER_BEAN} 阶段评估，可安全加载 {@code @Bean} 方法返回类型。\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Jakub Kubrynski\n * @author Stephane Nicoll\n * @author Andy Wilkinson\n * @author Uladzislau Seuruk\n * @see ConditionalOnBean\n * @see ConditionalOnMissingBean\n * @see ConditionalOnSingleCandidate\n */",
        ),
        (
            "\t\tcatch (ClassNotFoundException ex) {\n\t\t\t// Continue\n\t\t}",
            "\t\tcatch (ClassNotFoundException ex) {\n\t\t\t// 注解类未找到，忽略并继续\n\t\t}",
        ),
        (
            "/**\n\t * A search specification extracted from the underlying annotation.\n\t */",
            "/**\n\t * 从底层条件注解提取的 Bean 检索规格。\n\t */",
        ),
        (
            "\t\t\t// Safe to load at this point since we are in the REGISTER_BEAN phase",
            "\t\t\t// 处于 REGISTER_BEAN 阶段，此时可安全加载类字节码",
        ),
        (
            "/**\n\t * Specialized {@link Spec specification} for\n\t * {@link ConditionalOnSingleCandidate @ConditionalOnSingleCandidate}.\n\t */",
            "/**\n\t * {@link ConditionalOnSingleCandidate @ConditionalOnSingleCandidate} 专用的\n\t * {@link Spec 检索规格}。\n\t */",
        ),
        (
            "/**\n\t * Results collected during the condition evaluation.\n\t */",
            "/**\n\t * 条件评估过程中收集的匹配结果。\n\t */",
        ),
        (
            "/**\n\t * Exception thrown when the bean type cannot be deduced.\n\t */",
            "/**\n\t * Bean 类型无法推断时抛出的异常。\n\t */",
        ),
    ],
    "SpringApplicationRunListener.java": [
        (
            "/**\n * Listener for the {@link SpringApplication} {@code run} method.\n * {@link SpringApplicationRunListener}s are loaded through the\n * {@link SpringFactoriesLoader} and should declare a public constructor that accepts a\n * {@link SpringApplication} instance and a {@code String[]} of arguments. A new\n * {@link SpringApplicationRunListener} instance will be created for each run.\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Andy Wilkinson\n * @author Chris Bono\n * @since 1.0.0\n */",
            "/**\n * {@link SpringApplication#run} 方法的监听器。\n * <p>\n * {@link SpringApplicationRunListener} 通过 {@link SpringFactoriesLoader} 加载，\n * 须声明接受 {@link SpringApplication} 实例与 {@code String[]} 参数的 public 构造器。\n * 每次 {@code run} 调用都会创建新实例。\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Andy Wilkinson\n * @author Chris Bono\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * Called immediately when the run method has first started. Can be used for very\n\t * early initialization.\n\t * @param bootstrapContext the bootstrap context\n\t */",
            "/**\n\t * {@code run} 方法刚启动时立即调用，可用于极早期初始化。\n\t *\n\t * @param bootstrapContext 引导上下文\n\t */",
        ),
        (
            "/**\n\t * Called once the environment has been prepared, but before the\n\t * {@link ApplicationContext} has been created.\n\t * @param bootstrapContext the bootstrap context\n\t * @param environment the environment\n\t */",
            "/**\n\t * 环境准备完成后、{@link ApplicationContext} 创建前调用。\n\t *\n\t * @param bootstrapContext 引导上下文\n\t * @param environment 环境\n\t */",
        ),
        (
            "/**\n\t * Called once the {@link ApplicationContext} has been created and prepared, but\n\t * before sources have been loaded.\n\t * @param context the application context\n\t */",
            "/**\n\t * {@link ApplicationContext} 已创建并准备就绪、但尚未加载源时调用。\n\t *\n\t * @param context 应用上下文\n\t */",
        ),
        (
            "/**\n\t * Called once the application context has been loaded but before it has been\n\t * refreshed.\n\t * @param context the application context\n\t */",
            "/**\n\t * 应用上下文已加载但尚未刷新时调用。\n\t *\n\t * @param context 应用上下文\n\t */",
        ),
        (
            "/**\n\t * The context has been refreshed and the application has started but\n\t * {@link CommandLineRunner CommandLineRunners} and {@link ApplicationRunner\n\t * ApplicationRunners} have not been called.\n\t * @param context the application context.\n\t * @param timeTaken the time taken to start the application or {@code null} if unknown\n\t * @since 2.6.0\n\t */",
            "/**\n\t * 上下文已刷新且应用已启动，但尚未调用\n\t * {@link CommandLineRunner CommandLineRunners} 与 {@link ApplicationRunner ApplicationRunners}。\n\t *\n\t * @param context 应用上下文\n\t * @param timeTaken 启动耗时，未知时为 {@code null}\n\t * @since 2.6.0\n\t */",
        ),
        (
            "/**\n\t * Called immediately before the run method finishes, when the application context has\n\t * been refreshed and all {@link CommandLineRunner CommandLineRunners} and\n\t * {@link ApplicationRunner ApplicationRunners} have been called.\n\t * @param context the application context.\n\t * @param timeTaken the time taken for the application to be ready or {@code null} if\n\t * unknown\n\t * @since 2.6.0\n\t */",
            "/**\n\t * {@code run} 方法即将结束前调用；此时上下文已刷新且所有\n\t * {@link CommandLineRunner CommandLineRunners} 与 {@link ApplicationRunner ApplicationRunners}\n\t * 均已执行完毕。\n\t *\n\t * @param context 应用上下文\n\t * @param timeTaken 应用就绪耗时，未知时为 {@code null}\n\t * @since 2.6.0\n\t */",
        ),
        (
            "/**\n\t * Called when a failure occurs when running the application.\n\t * @param context the application context or {@code null} if a failure occurred before\n\t * the context was created\n\t * @param exception the failure\n\t * @since 2.0.0\n\t */",
            "/**\n\t * 应用运行失败时调用。\n\t *\n\t * @param context 应用上下文；若在上下文创建前失败则为 {@code null}\n\t * @param exception 失败原因\n\t * @since 2.0.0\n\t */",
        ),
    ],
    "SpringApplicationRunListeners.java": [
        (
            "/**\n * A collection of {@link SpringApplicationRunListener}.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Chris Bono\n */",
            "/**\n * {@link SpringApplicationRunListener} 集合，统一分发启动生命周期回调并记录 {@link ApplicationStartup} 指标。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Chris Bono\n */",
        ),
    ],
    "SpringApplicationShutdownHandlers.java": [
        (
            "/**\n * Interface that can be used to add or remove code that should run when the JVM is\n * shutdown. Shutdown handlers are similar to JVM {@link Runtime#addShutdownHook(Thread)\n * shutdown hooks} except that they run sequentially rather than concurrently.\n * <p>\n * Shutdown handlers are guaranteed to be called only after registered\n * {@link ApplicationContext} instances have been closed and are no longer active.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 2.5.1\n * @see SpringApplication#getShutdownHandlers()\n * @see SpringApplication#setRegisterShutdownHook(boolean)\n */",
            "/**\n * 用于添加或移除 JVM 关闭时应执行代码的接口。\n * <p>\n * 关闭处理器类似 JVM {@link Runtime#addShutdownHook(Thread) 关闭钩子}，\n * 但以顺序方式执行而非并发。\n * <p>\n * 保证仅在已注册的 {@link ApplicationContext} 实例全部关闭且不再活跃后才调用。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 2.5.1\n * @see SpringApplication#getShutdownHandlers()\n * @see SpringApplication#setRegisterShutdownHook(boolean)\n */",
        ),
        (
            "/**\n\t * Add an action to the handlers that will be run when the JVM exits.\n\t * @param action the action to add\n\t */",
            "/**\n\t * 向 JVM 退出时将执行的处理列表添加动作。\n\t *\n\t * @param action 要添加的动作\n\t */",
        ),
        (
            "/**\n\t * Remove a previously added an action so that it no longer runs when the JVM exits.\n\t * @param action the action to remove\n\t */",
            "/**\n\t * 移除先前添加的动作，使其在 JVM 退出时不再执行。\n\t *\n\t * @param action 要移除的动作\n\t */",
        ),
    ],
    "SpringApplicationShutdownHook.java": [
        (
            "/**\n * A {@link Runnable} to be used as a {@link Runtime#addShutdownHook(Thread) shutdown\n * hook} to perform graceful shutdown of Spring Boot applications. This hook tracks\n * registered application contexts as well as any actions registered via\n * {@link SpringApplication#getShutdownHandlers()}.\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @author Brian Clozel\n */",
            "/**\n * 作为 {@link Runtime#addShutdownHook(Thread) 关闭钩子} 使用的 {@link Runnable}，\n * 用于 Spring Boot 应用的优雅关闭。\n * <p>\n * 跟踪已注册的应用上下文，以及通过 {@link SpringApplication#getShutdownHandlers()}\n * 注册的动作；关闭时依次关闭上下文并逆序执行处理器。\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @author Brian Clozel\n */",
        ),
        (
            "/**\n\t * Call {@link ConfigurableApplicationContext#close()} and wait until the context\n\t * becomes inactive. We can't assume that just because the close method returns that\n\t * the context is actually inactive. It could be that another thread is still in the\n\t * process of disposing beans.\n\t * @param context the context to clean\n\t */",
            "/**\n\t * 调用 {@link ConfigurableApplicationContext#close()} 并等待上下文变为非活跃。\n\t * <p>\n\t * {@code close()} 返回并不保证上下文已完全非活跃——其他线程可能仍在销毁 Bean。\n\t *\n\t * @param context 要关闭的上下文\n\t */",
        ),
        (
            "/**\n\t * The handler actions for this shutdown hook.\n\t */",
            "/**\n\t * 此关闭钩子的处理器动作集合。\n\t */",
        ),
        (
            "/**\n\t * A single handler that uses object identity for {@link #equals(Object)} and\n\t * {@link #hashCode()}.\n\t *\n\t * @param runnable the handler runner\n\t */",
            "/**\n\t * 单个处理器；{@link #equals(Object)} 与 {@link #hashCode()} 基于对象身份。\n\t *\n\t * @param runnable 处理器执行体\n\t */",
        ),
        (
            "/**\n\t * {@link ApplicationListener} to track closed contexts.\n\t */",
            "/**\n\t * 跟踪已关闭上下文的 {@link ApplicationListener}。\n\t */",
        ),
        (
            "\t\t\t// The ContextClosedEvent is fired at the start of a call to {@code close()}\n\t\t\t// and if that happens in a different thread then the context may still be\n\t\t\t// active. Rather than just removing the context, we add it to a {@code\n\t\t\t// closedContexts} set. This is weak set so that the context can be GC'd once\n\t\t\t// the {@code close()} method returns.",
            "\t\t\t// ContextClosedEvent 在 {@code close()} 调用开始时触发；\n\t\t\t// 若在其他线程关闭，上下文可能仍活跃。因此不直接移除，\n\t\t\t// 而是加入 {@code closedContexts} 弱引用集合，\n\t\t\t// 以便 {@code close()} 返回后上下文可被 GC 回收。",
        ),
    ],
    "SpringBootBanner.java": [
        (
            "/**\n * Default Banner implementation which writes the 'Spring' banner.\n *\n * @author Phillip Webb\n */",
            "/**\n * 输出经典 Spring ASCII Banner 的默认 {@link Banner} 实现。\n * <p>\n * 使用 {@link AnsiOutput} 为版本号着色。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "SpringBootConfiguration.java": [
        (
            "/**\n * Indicates that a class provides Spring Boot application\n * {@link Configuration @Configuration}. Can be used as an alternative to the Spring's\n * standard {@code @Configuration} annotation so that configuration can be found\n * automatically (for example in tests).\n * <p>\n * Application should only ever include <em>one</em> {@code @SpringBootConfiguration} and\n * most idiomatic Spring Boot applications will inherit it from\n * {@code @SpringBootApplication}.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
            "/**\n * 标识类提供 Spring Boot 应用的 {@link Configuration @Configuration}。\n * <p>\n * 可作为 Spring 标准 {@code @Configuration} 的替代，便于自动发现配置（例如测试中）。\n * 应用应仅包含<em>一个</em> {@code @SpringBootConfiguration}，\n * 惯用的 Spring Boot 应用通常通过 {@code @SpringBootApplication} 继承它。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
        ),
        (
            "/**\n\t * Specify whether {@link Bean @Bean} methods should get proxied in order to enforce\n\t * bean lifecycle behavior, e.g. to return shared singleton bean instances even in\n\t * case of direct {@code @Bean} method calls in user code. This feature requires\n\t * method interception, implemented through a runtime-generated CGLIB subclass which\n\t * comes with limitations such as the configuration class and its methods not being\n\t * allowed to declare {@code final}.\n\t * <p>\n\t * The default is {@code true}, allowing for 'inter-bean references' within the\n\t * configuration class as well as for external calls to this configuration's\n\t * {@code @Bean} methods, e.g. from another configuration class. If this is not needed\n\t * since each of this particular configuration's {@code @Bean} methods is\n\t * self-contained and designed as a plain factory method for container use, switch\n\t * this flag to {@code false} in order to avoid CGLIB subclass processing.\n\t * <p>\n\t * Turning off bean method interception effectively processes {@code @Bean} methods\n\t * individually like when declared on non-{@code @Configuration} classes, a.k.a.\n\t * \"@Bean Lite Mode\" (see {@link Bean @Bean's javadoc}). It is therefore behaviorally\n\t * equivalent to removing the {@code @Configuration} stereotype.\n\t * @return whether to proxy {@code @Bean} methods\n\t * @since 2.2\n\t */",
            "/**\n\t * 是否代理 {@link Bean @Bean} 方法以强制 Bean 生命周期行为\n\t * （例如用户代码直接调用 {@code @Bean} 方法时仍返回共享单例）。\n\t * <p>\n\t * 此特性通过运行时生成的 CGLIB 子类实现方法拦截，\n\t * 配置类及其方法不能声明为 {@code final} 等限制适用。\n\t * 默认为 {@code true}，允许配置类内部 Bean 互引及外部调用本配置的 {@code @Bean} 方法。\n\t * 若各 {@code @Bean} 方法自洽且仅作容器工厂方法，可设为 {@code false} 以避免 CGLIB 处理。\n\t * <p>\n\t * 关闭代理等效于在非 {@code @Configuration} 类上声明 {@code @Bean} 的 Lite 模式\n\t * （见 {@link Bean @Bean} 文档），行为上等效于移除 {@code @Configuration} 元注解。\n\t *\n\t * @return 是否代理 {@code @Bean} 方法\n\t * @since 2.2\n\t */",
        ),
    ],
    "SpringBootExceptionHandler.java": [
        (
            "/**\n * {@link UncaughtExceptionHandler} to suppress handling already logged exceptions and\n * dealing with system exit.\n *\n * @author Phillip Webb\n */",
            "/**\n * {@link UncaughtExceptionHandler}，用于抑制已记录异常的重复处理，并在需要时触发系统退出。\n * <p>\n * 通过线程本地附加到当前线程，跟踪 {@link SpringApplication} 已记录的异常与退出码。\n *\n * @author Phillip Webb\n */",
        ),
        (
            "/**\n\t * Check if the exception is a log configuration message, i.e. the log call might not\n\t * have actually output anything.\n\t * @param ex the source exception\n\t * @return {@code true} if the exception contains a log configuration message\n\t */",
            "/**\n\t * 判断异常是否为日志配置错误消息（日志调用可能未实际输出任何内容）。\n\t *\n\t * @param ex 源异常\n\t * @return 若异常包含日志配置消息则为 {@code true}\n\t */",
        ),
        (
            "/**\n\t * Thread local used to attach and track handlers.\n\t */",
            "/**\n\t * 用于附加并跟踪处理器的线程本地变量。\n\t */",
        ),
    ],
    "SpringBootExceptionReporter.java": [
        (
            "/**\n * Callback interface used to support custom reporting of {@link SpringApplication}\n * startup errors. {@link SpringBootExceptionReporter reporters} are loaded through the\n * {@link SpringFactoriesLoader} and must declare a public constructor with a single\n * {@link ConfigurableApplicationContext} parameter.\n *\n * @author Phillip Webb\n * @since 2.0.0\n * @see ApplicationContextAware\n */",
            "/**\n * 支持自定义报告 {@link SpringApplication} 启动错误的回调接口。\n * <p>\n * {@link SpringBootExceptionReporter} 通过 {@link SpringFactoriesLoader} 加载，\n * 须声明接受单个 {@link ConfigurableApplicationContext} 参数的 public 构造器。\n *\n * @author Phillip Webb\n * @since 2.0.0\n * @see ApplicationContextAware\n */",
        ),
        (
            "/**\n\t * Report a startup failure to the user.\n\t * @param failure the source failure\n\t * @return {@code true} if the failure was reported or {@code false} if default\n\t * reporting should occur.\n\t */",
            "/**\n\t * 向用户报告启动失败。\n\t *\n\t * @param failure 失败原因\n\t * @return 若已报告失败为 {@code true}，否则为 {@code false} 以使用默认报告\n\t */",
        ),
    ],
    "StartupInfoLogger.java": [
        (
            "/**\n * Logs application information on startup.\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Moritz Halbritter\n */",
            "/**\n * 在应用启动时记录应用信息（Starting/Started 日志）。\n * <p>\n * 组装应用名、版本、Java 版本、PID、启动路径及 Spring Boot/Spring 版本等信息。\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Moritz Halbritter\n */",
        ),
    ],
    "WebApplicationType.java": [
        (
            "/**\n * An enumeration of possible types of web application.\n *\n * @author Andy Wilkinson\n * @author Brian Clozel\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * Web 应用类型的枚举。\n * <p>\n * 可通过 {@link #deduce()} 从类路径推断，或由 {@link SpringApplication} 显式指定。\n *\n * @author Andy Wilkinson\n * @author Brian Clozel\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * The application should not run as a web application and should not start an\n\t * embedded web server.\n\t */",
            "/**\n\t * 非 Web 应用，不启动嵌入式 Web 服务器。\n\t */",
        ),
        (
            "/**\n\t * The application should run as a servlet-based web application and should start an\n\t * embedded servlet web server.\n\t */",
            "/**\n\t * Servlet 风格 Web 应用，启动嵌入式 Servlet Web 服务器。\n\t */",
        ),
        (
            "/**\n\t * The application should run as a reactive web application and should start an\n\t * embedded reactive web server.\n\t */",
            "/**\n\t * 响应式 Web 应用，启动嵌入式响应式 Web 服务器。\n\t */",
        ),
        (
            "/**\n\t * Deduce the {@link WebApplicationType} from the current classpath.\n\t * @return the deduced web application\n\t * @since 4.0.1\n\t */",
            "/**\n\t * 从当前类路径推断 {@link WebApplicationType}。\n\t *\n\t * @return 推断出的 Web 应用类型\n\t * @since 4.0.1\n\t */",
        ),
        (
            "/**\n\t * Strategy that may be implemented by a module that can deduce the\n\t * {@link WebApplicationType}.\n\t *\n\t * @since 4.0.1\n\t */",
            "/**\n\t * 可由模块实现的策略，用于推断 {@link WebApplicationType}。\n\t *\n\t * @since 4.0.1\n\t */",
        ),
        (
            "/**\n\t\t * Deduce the web application type.\n\t\t * @return the deduced web application type or {@code null}\n\t\t */",
            "/**\n\t\t * 推断 Web 应用类型。\n\t\t *\n\t\t * @return 推断出的类型，或 {@code null}\n\t\t */",
        ),
    ],
    "SpringApplicationAdminMXBean.java": [
        (
            "/**\n * An MBean contract to control and monitor a running {@code SpringApplication} over JMX.\n * Intended for internal use only.\n *\n * @author Stephane Nicoll\n * @since 1.3.0\n */",
            "/**\n * 通过 JMX 控制与监控运行中 {@code SpringApplication} 的 MBean 契约。\n * <p>\n * <strong>仅供内部使用。</strong>\n *\n * @author Stephane Nicoll\n * @since 1.3.0\n */",
        ),
        (
            "/**\n\t * Specify if the application has fully started and is now ready.\n\t * @return {@code true} if the application is ready\n\t * @see org.springframework.boot.context.event.ApplicationReadyEvent\n\t */",
            "/**\n\t * 应用是否已完全启动并就绪。\n\t *\n\t * @return 应用就绪时为 {@code true}\n\t * @see org.springframework.boot.context.event.ApplicationReadyEvent\n\t */",
        ),
        (
            "/**\n\t * Specify if the application runs in an embedded web container. Return {@code false}\n\t * on a web application that hasn't fully started yet, so it is preferable to wait for\n\t * the application to be {@link #isReady() ready}.\n\t * @return {@code true} if the application runs in an embedded web container\n\t * @see #isReady()\n\t */",
            "/**\n\t * 应用是否运行在嵌入式 Web 容器中。\n\t * <p>\n\t * Web 应用未完全启动时可能返回 {@code false}，建议先等待 {@link #isReady() 就绪}。\n\t *\n\t * @return 运行在嵌入式 Web 容器时为 {@code true}\n\t * @see #isReady()\n\t */",
        ),
        (
            "/**\n\t * Return the value of the specified key from the application\n\t * {@link org.springframework.core.env.Environment Environment}.\n\t * @param key the property key\n\t * @return the property value or {@code null} if it does not exist\n\t */",
            "/**\n\t * 从应用 {@link org.springframework.core.env.Environment Environment} 返回指定键的值。\n\t *\n\t * @param key 属性键\n\t * @return 属性值，不存在时为 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Shutdown the application.\n\t * @see org.springframework.context.ConfigurableApplicationContext#close()\n\t */",
            "/**\n\t * 关闭应用。\n\t *\n\t * @see org.springframework.context.ConfigurableApplicationContext#close()\n\t */",
        ),
    ],
    "SpringApplicationAdminMXBeanRegistrar.java": [
        (
            "/**\n * Register a {@link SpringApplicationAdminMXBean} implementation to the platform\n * {@link MBeanServer}.\n *\n * @author Stephane Nicoll\n * @author Andy Wilkinson\n * @since 1.3.0\n */",
            "/**\n * 将 {@link SpringApplicationAdminMXBean} 实现注册到平台 {@link MBeanServer}。\n * <p>\n * 在 {@link ApplicationReadyEvent} 后将 {@code ready} 置为 {@code true}；\n * 通过 {@code server.ports} 属性源判断是否为嵌入式 Web 应用。\n *\n * @author Stephane Nicoll\n * @author Andy Wilkinson\n * @since 1.3.0\n */",
        ),
    ],
    "Ansi8BitColor.java": [
        (
            "/**\n * {@link AnsiElement} implementation for ANSI 8-bit foreground or background color codes.\n *\n * @author Toshiaki Maki\n * @author Phillip Webb\n * @since 2.2.0\n * @see #foreground(int)\n * @see #background(int)\n */",
            "/**\n * ANSI 8 位前景或背景色码的 {@link AnsiElement} 实现。\n *\n * @author Toshiaki Maki\n * @author Phillip Webb\n * @since 2.2.0\n * @see #foreground(int)\n * @see #background(int)\n */",
        ),
        (
            "/**\n\t * Create a new {@link Ansi8BitColor} instance.\n\t * @param prefix the prefix escape chars\n\t * @param code color code (must be 0-255)\n\t * @throws IllegalArgumentException if color code is not between 0 and 255.\n\t */",
            "/**\n\t * 创建新的 {@link Ansi8BitColor} 实例。\n\t *\n\t * @param prefix 转义前缀字符\n\t * @param code 颜色码（须为 0–255）\n\t * @throws IllegalArgumentException 颜色码不在 0–255 范围内\n\t */",
        ),
        (
            "/**\n\t * Return a foreground ANSI color code instance for the given code.\n\t * @param code the color code\n\t * @return an ANSI color code instance\n\t */",
            "/**\n\t * 返回给定颜色码的前景 ANSI 颜色实例。\n\t *\n\t * @param code 颜色码\n\t * @return ANSI 颜色实例\n\t */",
        ),
        (
            "/**\n\t * Return a background ANSI color code instance for the given code.\n\t * @param code the color code\n\t * @return an ANSI color code instance\n\t */",
            "/**\n\t * 返回给定颜色码的背景 ANSI 颜色实例。\n\t *\n\t * @param code 颜色码\n\t * @return ANSI 颜色实例\n\t */",
        ),
    ],
    "AnsiBackground.java": [
        (
            "/**\n * {@link AnsiElement Ansi} background colors.\n *\n * @author Phillip Webb\n * @author Geoffrey Chandler\n * @since 1.3.0\n */",
            "/**\n * {@link AnsiElement Ansi} 背景色。\n *\n * @author Phillip Webb\n * @author Geoffrey Chandler\n * @since 1.3.0\n */",
        ),
    ],
    "AnsiColor.java": [
        (
            "/**\n * {@link AnsiElement Ansi} colors.\n *\n * @author Phillip Webb\n * @author Geoffrey Chandler\n * @since 1.3.0\n */",
            "/**\n * {@link AnsiElement Ansi} 前景色。\n *\n * @author Phillip Webb\n * @author Geoffrey Chandler\n * @since 1.3.0\n */",
        ),
    ],
    "AnsiElement.java": [
        (
            "/**\n * An ANSI encodable element.\n *\n * @author Phillip Webb\n * @since 1.0.0\n */",
            "/**\n * 可 ANSI 编码的元素。\n *\n * @author Phillip Webb\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * @return the ANSI escape code\n\t */",
            "/**\n\t * @return ANSI 转义码\n\t */",
        ),
    ],
    "AnsiOutput.java": [
        (
            "/**\n * Generates ANSI encoded output, automatically attempting to detect if the terminal\n * supports ANSI.\n *\n * @author Phillip Webb\n * @author Yong-Hyun Kim\n * @author Philemon Hilscher\n * @since 1.0.0\n */",
            "/**\n * 生成 ANSI 编码输出，并自动检测终端是否支持 ANSI。\n * <p>\n * 可通过 {@link Enabled} 强制启用/禁用，或在 Windows 上检测控制台与 OS 版本。\n *\n * @author Phillip Webb\n * @author Yong-Hyun Kim\n * @author Philemon Hilscher\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * Sets if ANSI output is enabled.\n\t * @param enabled if ANSI is enabled, disabled or detected\n\t */",
            "/**\n\t * 设置 ANSI 输出是否启用。\n\t *\n\t * @param enabled 启用、禁用或自动检测\n\t */",
        ),
        (
            "/**\n\t * Returns if ANSI output is enabled\n\t * @return if ANSI enabled, disabled or detected\n\t */",
            "/**\n\t * 返回 ANSI 输出启用状态。\n\t *\n\t * @return 启用、禁用或自动检测\n\t */",
        ),
        (
            "/**\n\t * Sets if the System.console() is known to be available.\n\t * @param consoleAvailable if the console is known to be available or {@code null} to\n\t * use standard detection logic.\n\t */",
            "/**\n\t * 设置 {@code System.console()} 是否已知可用。\n\t *\n\t * @param consoleAvailable 控制台是否可用，或 {@code null} 使用标准检测逻辑\n\t */",
        ),
        (
            "/**\n\t * Encode a single {@link AnsiElement} if output is enabled.\n\t * @param element the element to encode\n\t * @return the encoded element or an empty string\n\t */",
            "/**\n\t * 若输出已启用，编码单个 {@link AnsiElement}。\n\t *\n\t * @param element 要编码的元素\n\t * @return 编码后的字符串，未启用时返回空字符串\n\t */",
        ),
        (
            "/**\n\t * Create a new ANSI string from the specified elements. Any {@link AnsiElement}s will\n\t * be encoded as required.\n\t * @param elements the elements to encode\n\t * @return a string of the encoded elements\n\t */",
            "/**\n\t * 由指定元素创建 ANSI 字符串；{@link AnsiElement} 会按需编码。\n\t *\n\t * @param elements 要编码的元素\n\t * @return 编码后的字符串\n\t */",
        ),
        (
            "/**\n\t * Possible values to pass to {@link AnsiOutput#setEnabled}. Determines when to output\n\t * ANSI escape sequences for coloring application output.\n\t */",
            "/**\n\t * 传给 {@link AnsiOutput#setEnabled} 的取值，决定何时输出 ANSI 转义序列以着色应用输出。\n\t */",
        ),
        (
            "/**\n\t\t * Try to detect whether ANSI coloring capabilities are available. The default\n\t\t * value for {@link AnsiOutput}.\n\t\t */",
            "/**\n\t\t * 尝试检测终端是否支持 ANSI 着色；{@link AnsiOutput} 的默认值。\n\t\t */",
        ),
        (
            "/**\n\t\t * Enable ANSI-colored output.\n\t\t */",
            "/**\n\t\t * 始终启用 ANSI 着色输出。\n\t\t */",
        ),
        (
            "/**\n\t\t * Disable ANSI-colored output.\n\t\t */",
            "/**\n\t\t * 始终禁用 ANSI 着色输出。\n\t\t */",
        ),
    ],
    "AnsiPropertySource.java": [
        (
            "/**\n * {@link PropertyResolver} for {@link AnsiStyle}, {@link AnsiColor},\n * {@link AnsiBackground} and {@link Ansi8BitColor} elements. Supports properties of the\n * form {@code AnsiStyle.BOLD}, {@code AnsiColor.RED} or {@code AnsiBackground.GREEN}.\n * Also supports a prefix of {@code Ansi.} which is an aggregation of everything (with\n * background colors prefixed {@code BG_}).\n * <p>\n * ANSI 8-bit color codes can be used with {@code AnsiColor} and {@code AnsiBackground}.\n * For example, {@code AnsiColor.208} will render orange text.\n * <a href=\"https://en.wikipedia.org/wiki/ANSI_escape_code\">Wikipedia</a> has a complete\n * list of the 8-bit color codes that can be used.\n *\n * @author Phillip Webb\n * @author Toshiaki Maki\n * @since 1.3.0\n */",
            "/**\n * 解析 {@link AnsiStyle}、{@link AnsiColor}、{@link AnsiBackground} 与\n * {@link Ansi8BitColor} 元素的 {@link PropertyResolver}。\n * <p>\n * 支持 {@code AnsiStyle.BOLD}、{@code AnsiColor.RED}、{@code AnsiBackground.GREEN} 等形式；\n * {@code Ansi.} 前缀聚合全部（背景色以 {@code BG_} 为前缀）。\n * <p>\n * {@code AnsiColor} 与 {@code AnsiBackground} 也支持 8 位色码，例如 {@code AnsiColor.208} 渲染橙色文本。\n * 完整 8 位色码列表见\n * <a href=\"https://en.wikipedia.org/wiki/ANSI_escape_code\">Wikipedia</a>。\n *\n * @author Phillip Webb\n * @author Toshiaki Maki\n * @since 1.3.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link AnsiPropertySource} instance.\n\t * @param name the name of the property source\n\t * @param encode if the output should be encoded\n\t */",
            "/**\n\t * 创建新的 {@link AnsiPropertySource} 实例。\n\t *\n\t * @param name 属性源名称\n\t * @param encode 是否对输出进行 ANSI 编码\n\t */",
        ),
        (
            "/**\n\t * Mapping between a name and the pseudo property source.\n\t */",
            "/**\n\t * 名称与伪属性源之间的映射。\n\t */",
        ),
        (
            "/**\n\t * {@link Mapping} for {@link AnsiElement} enums.\n\t */",
            "/**\n\t * {@link AnsiElement} 枚举的 {@link Mapping}。\n\t */",
        ),
        (
            "/**\n\t * {@link Mapping} for {@link Ansi8BitColor}.\n\t */",
            "/**\n\t * {@link Ansi8BitColor} 的 {@link Mapping}。\n\t */",
        ),
    ],
    "AnsiStyle.java": [
        (
            "/**\n * {@link AnsiElement Ansi} styles.\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
            "/**\n * {@link AnsiElement Ansi} 文本样式。\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
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
