#!/usr/bin/env python3
"""Chinese-annotate Spring Framework 7.0.8 wave-11 batch [20:40]."""
from __future__ import annotations

import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springframework/7.0.8"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][20:40]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractAsyncConfiguration.java": [
        (
            "/**\n * Abstract base {@code Configuration} class providing common structure for enabling\n * Spring's asynchronous method execution capability.\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @since 3.1\n * @see EnableAsync\n */",
            "/**\n * 启用 Spring 异步方法执行能力的抽象 {@code Configuration} 基类，提供通用结构。\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @since 3.1\n * @see EnableAsync\n */",
        ),
        (
            "\t/**\n\t * Collect any {@link AsyncConfigurer} beans through autowiring.\n\t */",
            "\t/**\n\t * 通过自动装配收集所有 {@link AsyncConfigurer} Bean。\n\t */",
        ),
    ],
    "AnnotationAsyncExecutionInterceptor.java": [
        (
            "/**\n * Specialization of {@link AsyncExecutionInterceptor} that delegates method execution to\n * an {@code Executor} based on the {@link Async} annotation.\n *\n * <p>Specifically designed to support use of the {@link Async#value()} executor\n * qualifier mechanism.\n *\n * <p>Supports detecting qualifier metadata via {@code @Async} at the method or\n * declaring class level. See {@link #getExecutorQualifier(Method)} for details.\n *\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 3.1.2\n * @see org.springframework.scheduling.annotation.Async\n * @see org.springframework.scheduling.annotation.AsyncAnnotationAdvisor\n */",
            "/**\n * {@link AsyncExecutionInterceptor} 的特化实现，根据 {@link Async} 注解\n * 将方法执行委托给 {@code Executor}。\n *\n * <p>专门支持 {@link Async#value()} 执行器限定符机制。\n *\n * <p>支持在方法或声明类级别通过 {@code @Async} 检测限定符元数据。\n * 详见 {@link #getExecutorQualifier(Method)}。\n *\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 3.1.2\n * @see org.springframework.scheduling.annotation.Async\n * @see org.springframework.scheduling.annotation.AsyncAnnotationAdvisor\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code AnnotationAsyncExecutionInterceptor} with the given executor\n\t * and a simple {@link AsyncUncaughtExceptionHandler}.\n\t * @param defaultExecutor the executor to be used by default if no more specific\n\t * executor has been qualified at the method level using {@link Async#value()};\n\t * a local executor for this interceptor will be built otherwise\n\t */",
            "\t/**\n\t * 使用给定执行器及简单 {@link AsyncUncaughtExceptionHandler} 创建新的\n\t * {@code AnnotationAsyncExecutionInterceptor}。\n\t * @param defaultExecutor 当方法级 {@link Async#value()} 未指定更具体执行器时使用的默认执行器；\n\t * 否则将为本拦截器构建本地执行器\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code AnnotationAsyncExecutionInterceptor} with the given executor.\n\t * @param defaultExecutor the executor to be used by default if no more specific\n\t * executor has been qualified at the method level using {@link Async#value()};\n\t * a local executor for this interceptor will be built otherwise\n\t * @param exceptionHandler the {@link AsyncUncaughtExceptionHandler} to use to\n\t * handle exceptions thrown by asynchronous method executions with {@code void}\n\t * return type\n\t */",
            "\t/**\n\t * 使用给定执行器创建新的 {@code AnnotationAsyncExecutionInterceptor}。\n\t * @param defaultExecutor 当方法级 {@link Async#value()} 未指定更具体执行器时使用的默认执行器；\n\t * 否则将为本拦截器构建本地执行器\n\t * @param exceptionHandler 用于处理 {@code void} 返回类型异步方法执行\n\t * 所抛出异常的 {@link AsyncUncaughtExceptionHandler}\n\t */",
        ),
        (
            "\t/**\n\t * Return the qualifier or bean name of the executor to be used when executing the\n\t * given method, specified via {@link Async#value} at the method or declaring\n\t * class level. If {@code @Async} is specified at both the method and class level, the\n\t * method's {@code value} takes precedence (even if empty string, indicating that\n\t * the default executor should be used preferentially).\n\t * @param method the method to inspect for executor qualifier metadata\n\t * @return the qualifier if specified, otherwise an empty string indicating that the\n\t * {@linkplain #setExecutor(Executor) default executor} should be used\n\t * @see #determineAsyncExecutor(Method)\n\t */",
            "\t/**\n\t * 返回执行给定方法时使用的执行器限定符或 Bean 名称，\n\t * 由方法或声明类级别的 {@link Async#value} 指定。\n\t * 若方法与类级别均标注 {@code @Async}，方法上的 {@code value} 优先\n\t * （即使为空字符串，也表示优先使用默认执行器）。\n\t * @param method 待检查执行器限定符元数据的方法\n\t * @return 已指定时返回限定符，否则返回空字符串表示应使用\n\t * {@linkplain #setExecutor(Executor) 默认执行器}\n\t * @see #determineAsyncExecutor(Method)\n\t */",
        ),
    ],
    "Async.java": [
        (
            "/**\n * Annotation that marks a method as a candidate for <i>asynchronous</i> execution.\n *\n * <p>Can also be used at the type level, in which case all the type's methods are\n * considered as asynchronous. Note, however, that {@code @Async} is not supported\n * on methods declared within a\n * {@link org.springframework.context.annotation.Configuration @Configuration} class.\n *\n * <p>In terms of target method signatures, any parameter types are supported.\n * However, the return type is constrained to either {@code void} or\n * {@link java.util.concurrent.Future}. In the latter case, you may declare the\n * more specific {@link java.util.concurrent.CompletableFuture} type which allows\n * for richer interaction with the asynchronous task and for immediate composition\n * with further processing steps.\n *\n * <p>A {@code Future} handle returned from the proxy will be an actual asynchronous\n * {@code (Completable)Future} that can be used to track the result of the\n * asynchronous method execution. However, since the target method needs to implement\n * the same signature, it will have to return a temporary {@code Future} handle that\n * just passes a value after computation in the execution thread: typically through\n * {@link java.util.concurrent.CompletableFuture#completedFuture(Object)}. The\n * provided value will be exposed to the caller through the actual asynchronous\n * {@code Future} handle at runtime.\n *\n * @author Juergen Hoeller\n * @author Chris Beams\n * @since 3.0\n * @see AnnotationAsyncExecutionInterceptor\n * @see AsyncAnnotationAdvisor\n */",
            "/**\n * 标记方法为<i>异步</i>执行候选的注解。\n *\n * <p>也可用于类型级别，此时该类型的所有方法均视为异步。\n * 但 {@code @Async} 不支持\n * {@link org.springframework.context.annotation.Configuration @Configuration} 类中声明的方法。\n *\n * <p>目标方法签名支持任意参数类型，\n * 但返回类型须为 {@code void} 或 {@link java.util.concurrent.Future}。\n * 后者可声明更具体的 {@link java.util.concurrent.CompletableFuture}，\n * 以便与异步任务 richer 交互并立即组合后续处理步骤。\n *\n * <p>代理返回的 {@code Future} 句柄为真正的异步 {@code (Completable)Future}，\n * 可用于跟踪异步方法执行结果。由于目标方法须实现相同签名，\n * 它须返回在执行线程中计算后传递值的临时 {@code Future} 句柄：\n * 通常通过 {@link java.util.concurrent.CompletableFuture#completedFuture(Object)}。\n * 提供的值将在运行时通过真正的异步 {@code Future} 句柄暴露给调用方。\n *\n * @author Juergen Hoeller\n * @author Chris Beams\n * @since 3.0\n * @see AnnotationAsyncExecutionInterceptor\n * @see AsyncAnnotationAdvisor\n */",
        ),
        (
            "\t/**\n\t * A qualifier value for the specified asynchronous operation(s).\n\t * <p>May be used to determine the target executor to be used when executing\n\t * the asynchronous operation(s), matching the qualifier value (or the bean\n\t * name) of a specific {@link java.util.concurrent.Executor Executor} or\n\t * {@link org.springframework.core.task.TaskExecutor TaskExecutor}\n\t * bean definition.\n\t * <p>When specified in a class-level {@code @Async} annotation, indicates that the\n\t * given executor should be used for all methods within the class. Method-level use\n\t * of {@code Async#value} always overrides any qualifier value configured at\n\t * the class level.\n\t * <p>The qualifier value will be resolved dynamically if supplied as a SpEL\n\t * expression (for example, {@code \"#{environment['myExecutor']}\"}) or a\n\t * property placeholder (for example, {@code \"${my.app.myExecutor}\"}).\n\t * @since 3.1.2\n\t */",
            "\t/**\n\t * 指定异步操作的限定符值。\n\t * <p>可用于确定执行异步操作时使用的目标执行器，\n\t * 匹配特定 {@link java.util.concurrent.Executor Executor} 或\n\t * {@link org.springframework.core.task.TaskExecutor TaskExecutor}\n\t * Bean 定义的限定符值（或 Bean 名称）。\n\t * <p>在类级 {@code @Async} 中指定时，表示该类内所有方法均使用该执行器。\n\t * 方法级 {@code Async#value} 始终覆盖类级配置的限定符值。\n\t * <p>若限定符值为 SpEL 表达式（如 {@code \"#{environment['myExecutor']}\"}）\n\t * 或属性占位符（如 {@code \"${my.app.myExecutor}\"}），将动态解析。\n\t * @since 3.1.2\n\t */",
        ),
    ],
    "AsyncAnnotationAdvisor.java": [
        (
            "/**\n * Advisor that activates asynchronous method execution through the {@link Async}\n * annotation. This annotation can be used at the method and type level in\n * implementation classes as well as in service interfaces.\n *\n * <p>This advisor detects the EJB 3.1 {@code jakarta.ejb.Asynchronous}\n * annotation as well, treating it exactly like Spring's own {@code Async}.\n * Furthermore, a custom async annotation type may get specified through the\n * {@link #setAsyncAnnotationType \"asyncAnnotationType\"} property.\n *\n * @author Juergen Hoeller\n * @since 3.0\n * @see Async\n * @see AnnotationAsyncExecutionInterceptor\n */",
            "/**\n * 通过 {@link Async} 注解激活异步方法执行的 Advisor。\n * 该注解可用于实现类及服务接口的方法级和类型级。\n *\n * <p>本 Advisor 也会检测 EJB 3.1 {@code jakarta.ejb.Asynchronous} 注解，\n * 与 Spring 自有 {@code Async} 同等处理。\n * 此外可通过 {@link #setAsyncAnnotationType \"asyncAnnotationType\"} 属性\n * 指定自定义异步注解类型。\n *\n * @author Juergen Hoeller\n * @since 3.0\n * @see Async\n * @see AnnotationAsyncExecutionInterceptor\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code AsyncAnnotationAdvisor} for bean-style configuration.\n\t */",
            "\t/**\n\t * 为 Bean 风格配置创建新的 {@code AsyncAnnotationAdvisor}。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code AsyncAnnotationAdvisor} for the given task executor.\n\t * @param executor the task executor to use for asynchronous methods\n\t * (can be {@code null} to trigger default executor resolution)\n\t * @param exceptionHandler the {@link AsyncUncaughtExceptionHandler} to use to\n\t * handle unexpected exception thrown by asynchronous method executions\n\t * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)\n\t */",
            "\t/**\n\t * 为给定任务执行器创建新的 {@code AsyncAnnotationAdvisor}。\n\t * @param executor 异步方法使用的任务执行器\n\t * （可为 {@code null} 以触发默认执行器解析）\n\t * @param exceptionHandler 处理异步方法执行意外异常的\n\t * {@link AsyncUncaughtExceptionHandler}\n\t * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code AsyncAnnotationAdvisor} for the given task executor.\n\t * @param executor the task executor to use for asynchronous methods\n\t * (can be {@code null} to trigger default executor resolution)\n\t * @param exceptionHandler the {@link AsyncUncaughtExceptionHandler} to use to\n\t * handle unexpected exception thrown by asynchronous method executions\n\t * @since 5.1\n\t * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)\n\t */",
            "\t/**\n\t * 为给定任务执行器创建新的 {@code AsyncAnnotationAdvisor}。\n\t * @param executor 异步方法使用的任务执行器\n\t * （可为 {@code null} 以触发默认执行器解析）\n\t * @param exceptionHandler 处理异步方法执行意外异常的\n\t * {@link AsyncUncaughtExceptionHandler}\n\t * @since 5.1\n\t * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)\n\t */",
        ),
        (
            "\t/**\n\t * Set the 'async' annotation type.\n\t * <p>The default async annotation type is the {@link Async} annotation, as well\n\t * as the EJB 3.1 {@code jakarta.ejb.Asynchronous} annotation (if present).\n\t * <p>This setter property exists so that developers can provide their own\n\t * (non-Spring-specific) annotation type to indicate that a method is to\n\t * be executed asynchronously.\n\t * @param asyncAnnotationType the desired annotation type\n\t */",
            "\t/**\n\t * 设置“异步”注解类型。\n\t * <p>默认异步注解类型为 {@link Async} 注解，\n\t * 以及 EJB 3.1 {@code jakarta.ejb.Asynchronous} 注解（若存在）。\n\t * <p>此 setter 供开发者提供自定义（非 Spring 专有）注解类型，\n\t * 以指示方法应异步执行。\n\t * @param asyncAnnotationType 所需的注解类型\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@code BeanFactory} to be used when looking up executors by qualifier.\n\t */",
            "\t/**\n\t * 设置按限定符查找执行器时使用的 {@code BeanFactory}。\n\t */",
        ),
        (
            "\t/**\n\t * Calculate a pointcut for the given async annotation types, if any.\n\t * @param asyncAnnotationTypes the async annotation types to introspect\n\t * @return the applicable Pointcut object, or {@code null} if none\n\t */",
            "\t/**\n\t * 为给定异步注解类型（若有）计算切点。\n\t * @param asyncAnnotationTypes 待内省的异步注解类型\n\t * @return 适用的 Pointcut 对象，无则返回 {@code null}\n\t */",
        ),
    ],
    "AsyncAnnotationBeanPostProcessor.java": [
        (
            "/**\n * Bean post-processor that automatically applies asynchronous invocation\n * behavior to any bean that carries the {@link Async} annotation at class or\n * method-level by adding a corresponding {@link AsyncAnnotationAdvisor} to the\n * exposed proxy (either an existing AOP proxy or a newly generated proxy that\n * implements all the target's interfaces).\n *\n * <p>The {@link TaskExecutor} responsible for the asynchronous execution may\n * be provided as well as the annotation type that indicates a method should be\n * invoked asynchronously. If no annotation type is specified, this post-\n * processor will detect both Spring's {@link Async @Async} annotation as well\n * as the EJB 3.1 {@code jakarta.ejb.Asynchronous} annotation.\n *\n * <p>For methods having a {@code void} return type, any exception thrown\n * during the asynchronous method invocation cannot be accessed by the\n * caller. An {@link AsyncUncaughtExceptionHandler} can be specified to handle\n * these cases.\n *\n * <p>Note: The underlying async advisor applies before existing advisors by default,\n * in order to switch to async execution as early as possible in the invocation chain.\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @since 3.0\n * @see Async\n * @see AsyncAnnotationAdvisor\n * @see #setBeforeExistingAdvisors\n * @see ScheduledAnnotationBeanPostProcessor\n */",
            "/**\n * Bean 后处理器，为类或方法级携带 {@link Async} 注解的 Bean\n * 自动应用异步调用行为，向暴露的代理（现有 AOP 代理或新生成、\n * 实现目标全部接口的代理）添加对应的 {@link AsyncAnnotationAdvisor}。\n *\n * <p>可提供负责异步执行的 {@link TaskExecutor}，\n * 以及指示方法应异步调用的注解类型。\n * 若未指定注解类型，本后处理器将检测 Spring {@link Async @Async} 注解\n * 及 EJB 3.1 {@code jakarta.ejb.Asynchronous} 注解。\n *\n * <p>对于 {@code void} 返回类型的方法，异步调用期间抛出的异常\n * 无法被调用方访问。可指定 {@link AsyncUncaughtExceptionHandler} 处理此类情况。\n *\n * <p>注意：底层异步 Advisor 默认在现有 Advisor 之前应用，\n * 以便在调用链中尽早切换到异步执行。\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @since 3.0\n * @see Async\n * @see AsyncAnnotationAdvisor\n * @see #setBeforeExistingAdvisors\n * @see ScheduledAnnotationBeanPostProcessor\n */",
        ),
        (
            "\t/**\n\t * The default name of the {@link TaskExecutor} bean to pick up: \"taskExecutor\".\n\t * <p>Note that the initial lookup happens by type; this is just the fallback\n\t * in case of multiple executor beans found in the context.\n\t * @since 4.2\n\t * @see AnnotationAsyncExecutionInterceptor#DEFAULT_TASK_EXECUTOR_BEAN_NAME\n\t */",
            "\t/**\n\t * 要选取的 {@link TaskExecutor} Bean 默认名称：\"taskExecutor\"。\n\t * <p>初始查找按类型进行；此名称仅作为上下文中存在多个执行器 Bean 时的回退。\n\t * @since 4.2\n\t * @see AnnotationAsyncExecutionInterceptor#DEFAULT_TASK_EXECUTOR_BEAN_NAME\n\t */",
        ),
        (
            "\t/**\n\t * Configure this post-processor with the given executor and exception handler suppliers,\n\t * applying the corresponding default if a supplier is not resolvable.\n\t * @since 5.1\n\t */",
            "\t/**\n\t * 使用给定执行器与异常处理器 Supplier 配置本后处理器，\n\t * 若 Supplier 不可解析则应用对应默认值。\n\t * @since 5.1\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link Executor} to use when invoking methods asynchronously.\n\t * <p>If not specified, default executor resolution will apply: searching for a\n\t * unique {@link TaskExecutor} bean in the context, or for an {@link Executor}\n\t * bean named \"taskExecutor\" otherwise. If neither of the two is resolvable,\n\t * a local default executor will be created within the interceptor.\n\t * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)\n\t * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME\n\t */",
            "\t/**\n\t * 设置异步调用方法时使用的 {@link Executor}。\n\t * <p>若未指定，将应用默认执行器解析：在上下文中查找唯一 {@link TaskExecutor} Bean，\n\t * 否则查找名为 \"taskExecutor\" 的 {@link Executor} Bean。\n\t * 若两者均不可解析，将在拦截器内创建本地默认执行器。\n\t * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)\n\t * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link AsyncUncaughtExceptionHandler} to use to handle uncaught\n\t * exceptions thrown by asynchronous method executions.\n\t * @since 4.1\n\t */",
            "\t/**\n\t * 设置用于处理异步方法执行未捕获异常的\n\t * {@link AsyncUncaughtExceptionHandler}。\n\t * @since 4.1\n\t */",
        ),
        (
            "\t/**\n\t * Set the 'async' annotation type to be detected at either class or method\n\t * level. By default, both the {@link Async} annotation and the EJB 3.1\n\t * {@code jakarta.ejb.Asynchronous} annotation will be detected.\n\t * <p>This setter property exists so that developers can provide their own\n\t * (non-Spring-specific) annotation type to indicate that a method (or all\n\t * methods of a given class) should be invoked asynchronously.\n\t * @param asyncAnnotationType the desired annotation type\n\t */",
            "\t/**\n\t * 设置在类或方法级别检测的“异步”注解类型。\n\t * 默认检测 {@link Async} 注解及 EJB 3.1 {@code jakarta.ejb.Asynchronous} 注解。\n\t * <p>此 setter 供开发者提供自定义（非 Spring 专有）注解类型，\n\t * 以指示方法（或给定类的全部方法）应异步调用。\n\t * @param asyncAnnotationType 所需的注解类型\n\t */",
        ),
    ],
    "AsyncConfigurationSelector.java": [
        (
            "/**\n * Selects which implementation of {@link AbstractAsyncConfiguration} should\n * be used based on the value of {@link EnableAsync#mode} on the importing\n * {@code @Configuration} class.\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @since 3.1\n * @see EnableAsync\n * @see ProxyAsyncConfiguration\n */",
            "/**\n * 根据导入 {@code @Configuration} 类上 {@link EnableAsync#mode} 的值\n * 选择应使用的 {@link AbstractAsyncConfiguration} 实现。\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @since 3.1\n * @see EnableAsync\n * @see ProxyAsyncConfiguration\n */",
        ),
        (
            "\t/**\n\t * Returns {@link ProxyAsyncConfiguration} or {@code AspectJAsyncConfiguration}\n\t * for {@code PROXY} and {@code ASPECTJ} values of {@link EnableAsync#mode()},\n\t * respectively.\n\t */",
            "\t/**\n\t * 分别对 {@link EnableAsync#mode()} 的 {@code PROXY} 与 {@code ASPECTJ} 值\n\t * 返回 {@link ProxyAsyncConfiguration} 或 {@code AspectJAsyncConfiguration}。\n\t */",
        ),
    ],
    "AsyncConfigurer.java": [
        (
            "/**\n * Interface to be implemented for customizing the {@link Executor} instance used when\n * processing async method invocations or the {@link AsyncUncaughtExceptionHandler}\n * instance used to process exceptions thrown from async methods with a {@code void}\n * return type.\n *\n * <p>Typically implemented by @{@link org.springframework.context.annotation.Configuration\n * Configuration} classes annotated with @{@link EnableAsync}.\n * See the @{@link EnableAsync} javadoc for usage examples.\n *\n * <p><b>NOTE: An {@code AsyncConfigurer} will get initialized early.</b>\n * Do not inject common dependencies into autowired fields directly; instead, consider\n * declaring a lazy {@link org.springframework.beans.factory.ObjectProvider} for those.\n *\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 3.1\n * @see AbstractAsyncConfiguration\n * @see EnableAsync\n */",
            "/**\n * 用于自定义处理异步方法调用时使用的 {@link Executor} 实例，\n * 或处理 {@code void} 返回类型异步方法所抛出异常的\n * {@link AsyncUncaughtExceptionHandler} 实例的接口。\n *\n * <p>通常由标注 @{@link EnableAsync} 的\n * @{@link org.springframework.context.annotation.Configuration Configuration} 类实现。\n * 用法示例见 @{@link EnableAsync} 的 javadoc。\n *\n * <p><b>注意：{@code AsyncConfigurer} 会较早初始化。</b>\n * 请勿直接向自动装配字段注入常见依赖；\n * 可考虑为这些依赖声明惰性 {@link org.springframework.beans.factory.ObjectProvider}。\n *\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 3.1\n * @see AbstractAsyncConfiguration\n * @see EnableAsync\n */",
        ),
        (
            "\t/**\n\t * The {@link Executor} instance to be used when processing async\n\t * method invocations.\n\t */",
            "\t/**\n\t * 处理异步方法调用时使用的 {@link Executor} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * The {@link AsyncUncaughtExceptionHandler} instance to be used\n\t * when an exception is thrown during an asynchronous method execution\n\t * with {@code void} return type.\n\t */",
            "\t/**\n\t * 在 {@code void} 返回类型的异步方法执行抛出异常时使用的\n\t * {@link AsyncUncaughtExceptionHandler} 实例。\n\t */",
        ),
    ],
    "AsyncConfigurerSupport.java": [
        (
            "/**\n * A convenience {@link AsyncConfigurer} that implements all methods\n * so that the defaults are used. Provides a backward compatible alternative\n * of implementing {@link AsyncConfigurer} directly.\n *\n * @author Stephane Nicoll\n * @since 4.1\n * @deprecated as of 6.0 in favor of implementing {@link AsyncConfigurer} directly\n */",
            "/**\n * 便捷 {@link AsyncConfigurer}，实现全部方法以使用默认值。\n * 提供直接实现 {@link AsyncConfigurer} 的向后兼容替代方案。\n *\n * @author Stephane Nicoll\n * @since 4.1\n * @deprecated 自 6.0 起，请直接实现 {@link AsyncConfigurer}\n */",
        ),
    ],
    "AsyncResult.java": [
        (
            "/**\n * A pass-through {@code Future} handle that can be used for method signatures\n * which are declared with a {@code Future} return type for asynchronous execution.\n *\n * @author Juergen Hoeller\n * @author Rossen Stoyanchev\n * @since 3.0\n * @param <V> the value type\n * @see Async\n * @see #forValue(Object)\n * @see #forExecutionException(Throwable)\n * @deprecated as of 6.0, in favor of {@link CompletableFuture}\n */",
            "/**\n * 透传 {@code Future} 句柄，可用于声明 {@code Future} 返回类型\n * 以支持异步执行的方法签名。\n *\n * @author Juergen Hoeller\n * @author Rossen Stoyanchev\n * @since 3.0\n * @param <V> 值类型\n * @see Async\n * @see #forValue(Object)\n * @see #forExecutionException(Throwable)\n * @deprecated 自 6.0 起，请使用 {@link CompletableFuture}\n */",
        ),
        (
            "\t/**\n\t * Create a new AsyncResult holder.\n\t * @param value the value to pass through\n\t */",
            "\t/**\n\t * 创建新的 AsyncResult 持有者。\n\t * @param value 要透传的值\n\t */",
        ),
        (
            "\t/**\n\t * Create a new async result which exposes the given value from {@link Future#get()}.\n\t * @param value the value to expose\n\t * @since 4.2\n\t * @see Future#get()\n\t */",
            "\t/**\n\t * 创建新的异步结果，从 {@link Future#get()} 暴露给定值。\n\t * @param value 要暴露的值\n\t * @since 4.2\n\t * @see Future#get()\n\t */",
        ),
        (
            "\t/**\n\t * Create a new async result which exposes the given exception as an\n\t * {@link ExecutionException} from {@link Future#get()}.\n\t * @param ex the exception to expose (either an pre-built {@link ExecutionException}\n\t * or a cause to be wrapped in an {@link ExecutionException})\n\t * @since 4.2\n\t * @see ExecutionException\n\t */",
            "\t/**\n\t * 创建新的异步结果，从 {@link Future#get()} 将给定异常\n\t * 作为 {@link ExecutionException} 暴露。\n\t * @param ex 要暴露的异常（可为预构建的 {@link ExecutionException}\n\t * 或将被包装为 {@link ExecutionException} 的原因）\n\t * @since 4.2\n\t * @see ExecutionException\n\t */",
        ),
    ],
    "ProxyAsyncConfiguration.java": [
        (
            "/**\n * {@code @Configuration} class that registers the Spring infrastructure beans necessary\n * to enable proxy-based asynchronous method execution.\n *\n * @author Chris Beams\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @since 3.1\n * @see EnableAsync\n * @see AsyncConfigurationSelector\n */",
            "/**\n * 注册启用基于代理的异步方法执行所需的 Spring 基础设施 Bean 的\n * {@code @Configuration} 类。\n *\n * @author Chris Beams\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @since 3.1\n * @see EnableAsync\n * @see AsyncConfigurationSelector\n */",
        ),
    ],
    "Schedules.java": [
        (
            "/**\n * Container annotation that aggregates several {@link Scheduled} annotations.\n *\n * <p>Can be used natively, declaring several nested {@link Scheduled} annotations.\n * Can also be used in conjunction with Java's support for repeatable annotations,\n * where {@link Scheduled @Scheduled} can simply be declared several times on the\n * same method, implicitly generating this container annotation.\n *\n * <p>This annotation may be used as a <em>meta-annotation</em> to create custom\n * <em>composed annotations</em>.\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see Scheduled\n */",
            "/**\n * 聚合多个 {@link Scheduled} 注解的容器注解。\n *\n * <p>可原生使用，声明多个嵌套 {@link Scheduled} 注解。\n * 也可与 Java 可重复注解支持配合：在同一方法上多次声明\n * {@link Scheduled @Scheduled}，将隐式生成本容器注解。\n *\n * <p>本注解可作为<em>元注解</em>创建自定义<em>组合注解</em>。\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see Scheduled\n */",
        ),
    ],
    "SchedulingConfiguration.java": [
        (
            "/**\n * {@code @Configuration} class that registers a {@link ScheduledAnnotationBeanPostProcessor}\n * bean capable of processing Spring's @{@link Scheduled} annotation.\n *\n * <p>This configuration class is automatically imported when using the\n * {@link EnableScheduling @EnableScheduling} annotation. See\n * {@code @EnableScheduling}'s javadoc for complete usage details.\n *\n * @author Chris Beams\n * @since 3.1\n * @see EnableScheduling\n * @see ScheduledAnnotationBeanPostProcessor\n */",
            "/**\n * 注册可处理 Spring @{@link Scheduled} 注解的\n * {@link ScheduledAnnotationBeanPostProcessor} Bean 的 {@code @Configuration} 类。\n *\n * <p>使用 {@link EnableScheduling @EnableScheduling} 注解时自动导入本配置类。\n * 完整用法见 {@code @EnableScheduling} 的 javadoc。\n *\n * @author Chris Beams\n * @since 3.1\n * @see EnableScheduling\n * @see ScheduledAnnotationBeanPostProcessor\n */",
        ),
    ],
    "SchedulingConfigurer.java": [
        (
            "/**\n * Optional interface to be implemented by {@link\n * org.springframework.context.annotation.Configuration @Configuration} classes annotated\n * with {@link EnableScheduling @EnableScheduling}. Typically used for setting a specific\n * {@link org.springframework.scheduling.TaskScheduler TaskScheduler} bean to be used when\n * executing scheduled tasks or for registering scheduled tasks in a <em>programmatic</em>\n * fashion as opposed to the <em>declarative</em> approach of using the\n * {@link Scheduled @Scheduled} annotation. For example, this may be necessary\n * when implementing {@link org.springframework.scheduling.Trigger Trigger}-based\n * tasks, which are not supported by the {@code @Scheduled} annotation.\n *\n * <p>See {@link EnableScheduling @EnableScheduling} for detailed usage examples.\n *\n * @author Chris Beams\n * @since 3.1\n * @see EnableScheduling\n * @see ScheduledTaskRegistrar\n */",
            "/**\n * 由标注 {@link EnableScheduling @EnableScheduling} 的\n * {@link org.springframework.context.annotation.Configuration @Configuration} 类\n * 实现的可选接口。通常用于设置执行定时任务时使用的特定\n * {@link org.springframework.scheduling.TaskScheduler TaskScheduler} Bean，\n * 或以<em>编程</em>方式注册定时任务，而非使用 {@link Scheduled @Scheduled} 注解的\n * <em>声明式</em>方式。例如实现 {@link org.springframework.scheduling.Trigger Trigger}\n * 任务时可能需要，{@code @Scheduled} 不支持此类任务。\n *\n * <p>详细用法示例见 {@link EnableScheduling @EnableScheduling}。\n *\n * @author Chris Beams\n * @since 3.1\n * @see EnableScheduling\n * @see ScheduledTaskRegistrar\n */",
        ),
        (
            "\t/**\n\t * Callback allowing a {@link org.springframework.scheduling.TaskScheduler}\n\t * and specific {@link org.springframework.scheduling.config.Task} instances\n\t * to be registered against the given the {@link ScheduledTaskRegistrar}.\n\t * @param taskRegistrar the registrar to be configured\n\t */",
            "\t/**\n\t * 回调，允许向给定 {@link ScheduledTaskRegistrar} 注册\n\t * {@link org.springframework.scheduling.TaskScheduler} 及特定\n\t * {@link org.springframework.scheduling.config.Task} 实例。\n\t * @param taskRegistrar 待配置的任务注册器\n\t */",
        ),
    ],
    "CustomizableThreadFactory.java": [
        (
            "/**\n * Implementation of the {@link java.util.concurrent.ThreadFactory} interface,\n * allowing for customizing the created threads (name, priority, etc).\n *\n * <p>See the base class {@link org.springframework.util.CustomizableThreadCreator}\n * for details on the available configuration options.\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see #setThreadNamePrefix\n * @see #setThreadPriority\n */",
            "/**\n * {@link java.util.concurrent.ThreadFactory} 接口的实现，\n * 允许自定义所创建线程（名称、优先级等）。\n *\n * <p>可用配置选项详见基类 {@link org.springframework.util.CustomizableThreadCreator}。\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see #setThreadNamePrefix\n * @see #setThreadPriority\n */",
        ),
        (
            "\t/**\n\t * Create a new CustomizableThreadFactory with default thread name prefix.\n\t */",
            "\t/**\n\t * 使用默认线程名前缀创建新的 CustomizableThreadFactory。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new CustomizableThreadFactory with the given thread name prefix.\n\t * @param threadNamePrefix the prefix to use for the names of newly created threads\n\t */",
            "\t/**\n\t * 使用给定线程名前缀创建新的 CustomizableThreadFactory。\n\t * @param threadNamePrefix 新创建线程名称使用的前缀\n\t */",
        ),
    ],
    "DefaultManagedAwareThreadFactory.java": [
        (
            "/**\n * JNDI-based variant of {@link CustomizableThreadFactory}, performing a default lookup\n * for JSR-236's \"java:comp/DefaultManagedThreadFactory\" in a Jakarta EE environment,\n * falling back to the local {@link CustomizableThreadFactory} setup if not found.\n *\n * <p>This is a convenient way to use managed threads when running in a Jakarta EE\n * environment, simply using regular local threads otherwise - without conditional\n * setup (i.e. without profiles).\n *\n * <p>Note: This class is not strictly JSR-236 based; it can work with any regular\n * {@link java.util.concurrent.ThreadFactory} that can be found in JNDI. Therefore,\n * the default JNDI name \"java:comp/DefaultManagedThreadFactory\" can be customized\n * through the {@link #setJndiName \"jndiName\"} bean property.\n *\n * @author Juergen Hoeller\n * @since 4.0\n */",
            "/**\n * 基于 JNDI 的 {@link CustomizableThreadFactory} 变体，\n * 在 Jakarta EE 环境中默认查找 JSR-236 的 \"java:comp/DefaultManagedThreadFactory\"，\n * 未找到时回退到本地 {@link CustomizableThreadFactory} 配置。\n *\n * <p>这是在 Jakarta EE 环境中使用受管线程的便捷方式，\n * 否则直接使用常规本地线程，无需条件配置（即无需 profile）。\n *\n * <p>注意：本类并非严格基于 JSR-236；可与 JNDI 中找到的任何常规\n * {@link java.util.concurrent.ThreadFactory} 配合工作。\n * 因此可通过 {@link #setJndiName \"jndiName\"} Bean 属性自定义默认 JNDI 名称\n * \"java:comp/DefaultManagedThreadFactory\"。\n *\n * @author Juergen Hoeller\n * @since 4.0\n */",
        ),
        (
            "\t/**\n\t * Set the JNDI template to use for JNDI lookups.\n\t * @see org.springframework.jndi.JndiAccessor#setJndiTemplate\n\t */",
            "\t/**\n\t * 设置 JNDI 查找使用的 JNDI 模板。\n\t * @see org.springframework.jndi.JndiAccessor#setJndiTemplate\n\t */",
        ),
        (
            "\t/**\n\t * Set the JNDI environment to use for JNDI lookups.\n\t * @see org.springframework.jndi.JndiAccessor#setJndiEnvironment\n\t */",
            "\t/**\n\t * 设置 JNDI 查找使用的 JNDI 环境。\n\t * @see org.springframework.jndi.JndiAccessor#setJndiEnvironment\n\t */",
        ),
        (
            "\t/**\n\t * Set whether the lookup occurs in a Jakarta EE container, i.e. if the prefix\n\t * \"java:comp/env/\" needs to be added if the JNDI name doesn't already\n\t * contain it. PersistenceAnnotationBeanPostProcessor's default is \"true\".\n\t * @see org.springframework.jndi.JndiLocatorSupport#setResourceRef\n\t */",
            "\t/**\n\t * 设置查找是否发生在 Jakarta EE 容器中，\n\t * 即 JNDI 名称尚未包含时是否需添加前缀 \"java:comp/env/\"。\n\t * PersistenceAnnotationBeanPostProcessor 默认为 \"true\"。\n\t * @see org.springframework.jndi.JndiLocatorSupport#setResourceRef\n\t */",
        ),
        (
            "\t/**\n\t * Specify a JNDI name of the {@link java.util.concurrent.ThreadFactory} to delegate to,\n\t * replacing the default JNDI name \"java:comp/DefaultManagedThreadFactory\".\n\t * <p>This can either be a fully qualified JNDI name, or the JNDI name relative\n\t * to the current environment naming context if \"resourceRef\" is set to \"true\".\n\t * @see #setResourceRef\n\t */",
            "\t/**\n\t * 指定委托的 {@link java.util.concurrent.ThreadFactory} 的 JNDI 名称，\n\t * 替换默认 JNDI 名称 \"java:comp/DefaultManagedThreadFactory\"。\n\t * <p>可为完全限定 JNDI 名称，或在 \"resourceRef\" 为 \"true\" 时\n\t * 相对于当前环境命名上下文的 JNDI 名称。\n\t * @see #setResourceRef\n\t */",
        ),
    ],
}

# Merge large-file JavaDoc replacements
import importlib.util

_large_spec = importlib.util.spec_from_file_location(
    "wave11_large",
    ROOT / "scripts/annotate_springframework_wave11_large.py",
)
_large_mod = importlib.util.module_from_spec(_large_spec)
assert _large_spec.loader is not None
_large_spec.loader.exec_module(_large_mod)
FILE_REPLACEMENTS.update(_large_mod.LARGE_FILE_REPLACEMENTS)


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
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            if cn < 10 or not lic:
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
