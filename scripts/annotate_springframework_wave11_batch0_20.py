#!/usr/bin/env python3
"""Chinese-annotate Spring Framework 7.0.8 wave-11 batch [0:20]."""
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
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][:20]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SimpleJndiBeanFactory.java": [
        (
            "/**\n * Simple JNDI-based implementation of Spring's\n * {@link org.springframework.beans.factory.BeanFactory} interface.\n * Does not support enumerating bean definitions, hence doesn't implement\n * the {@link org.springframework.beans.factory.ListableBeanFactory} interface.\n *\n * <p>This factory resolves given bean names as JNDI names within the\n * Jakarta EE application's \"java:comp/env/\" namespace. It caches the resolved\n * types for all obtained objects, and optionally also caches shareable\n * objects (if they are explicitly marked as\n * {@link #addShareableResource shareable resource}).\n *\n * <p>The main intent of this factory is usage in combination with Spring's\n * {@link org.springframework.context.annotation.CommonAnnotationBeanPostProcessor},\n * configured as \"resourceFactory\" for resolving {@code @Resource}\n * annotations as JNDI objects without intermediate bean definitions.\n * It may be used for similar lookup scenarios as well, of course,\n * in particular if BeanFactory-style type checking is required.\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see org.springframework.beans.factory.support.DefaultListableBeanFactory\n * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor\n */",
            "/**\n * 基于 JNDI 的 Spring {@link org.springframework.beans.factory.BeanFactory} 接口简单实现。\n * 不支持枚举 Bean 定义，因此未实现\n * {@link org.springframework.beans.factory.ListableBeanFactory} 接口。\n *\n * <p>本工厂将给定 Bean 名称解析为 Jakarta EE 应用\n * \"java:comp/env/\" 命名空间内的 JNDI 名称。它缓存所有已获取对象的解析类型，\n * 并可选择缓存可共享对象（若显式标记为\n * {@link #addShareableResource 可共享资源}）。\n *\n * <p>本工厂的主要用途是与 Spring 的\n * {@link org.springframework.context.annotation.CommonAnnotationBeanPostProcessor} 配合，\n * 配置为 \"resourceFactory\"，以便将 {@code @Resource}\n * 注解直接解析为 JNDI 对象，无需中间 Bean 定义。\n * 当然也可用于类似查找场景，\n * 尤其当需要 BeanFactory 风格的类型检查时。\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see org.springframework.beans.factory.support.DefaultListableBeanFactory\n * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor\n */",
        ),
        (
            "\t/** JNDI names of resources that are known to be shareable, i.e. can be cached */",
            "\t/** 已知可共享、即允许缓存的 JNDI 资源名称 */",
        ),
        (
            "\t/** Cache of shareable singleton objects: bean name to bean instance. */",
            "\t/** 可共享单例对象缓存：Bean 名称到 Bean 实例。 */",
        ),
        (
            "\t/** Cache of the types of nonshareable resources: bean name to bean type. */",
            "\t/** 不可共享资源类型缓存：Bean 名称到 Bean 类型。 */",
        ),
        (
            "\t/**\n\t * Add the name of a shareable JNDI resource,\n\t * which this factory is allowed to cache once obtained.\n\t * @param shareableResource the JNDI name\n\t * (typically within the \"java:comp/env/\" namespace)\n\t */",
            "\t/**\n\t * 添加可共享 JNDI 资源名称，\n\t * 本工厂在获取后允许缓存该资源。\n\t * @param shareableResource JNDI 名称\n\t *（通常位于 \"java:comp/env/\" 命名空间内）\n\t */",
        ),
        (
            "\t/**\n\t * Set a list of names of shareable JNDI resources,\n\t * which this factory is allowed to cache once obtained.\n\t * @param shareableResources the JNDI names\n\t * (typically within the \"java:comp/env/\" namespace)\n\t */",
            "\t/**\n\t * 设置可共享 JNDI 资源名称列表，\n\t * 本工厂在获取后允许缓存这些资源。\n\t * @param shareableResources JNDI 名称列表\n\t *（通常位于 \"java:comp/env/\" 命名空间内）\n\t */",
        ),
    ],
    "InvocationRejectedException.java": [
        (
            "/**\n * Exception thrown when a target will not get invoked due to a resilience policy,\n * such as the concurrency limit having been reached for a class/method annotated with\n * {@link org.springframework.resilience.annotation.ConcurrencyLimit @ConcurrencyLimit}.\n *\n * <p>Extends {@link RejectedExecutionException} as a common base class\n * with {@link org.springframework.core.task.TaskRejectedException},\n * allowing for custom catch blocks to cover both Spring scenarios and\n * {@link java.util.concurrent.ExecutorService} rejection exceptions.\n *\n * @author Juergen Hoeller\n * @since 7.0.3\n * @see org.springframework.resilience.annotation.ConcurrencyLimit.ThrottlePolicy#REJECT\n * @see org.springframework.core.task.TaskRejectedException\n */",
            "/**\n * 因弹性策略导致目标不会被调用时抛出的异常，\n * 例如带 {@link org.springframework.resilience.annotation.ConcurrencyLimit @ConcurrencyLimit}\n * 注解的类/方法已达到并发上限。\n *\n * <p>继承 {@link RejectedExecutionException}，与\n * {@link org.springframework.core.task.TaskRejectedException} 共用基类，\n * 便于自定义 catch 块同时覆盖 Spring 场景与\n * {@link java.util.concurrent.ExecutorService} 拒绝异常。\n *\n * @author Juergen Hoeller\n * @since 7.0.3\n * @see org.springframework.resilience.annotation.ConcurrencyLimit.ThrottlePolicy#REJECT\n * @see org.springframework.core.task.TaskRejectedException\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code InvocationRejectedException}\n\t * with the specified detail message and target instance.\n\t * @param msg the detail message\n\t * @param target the target instance that was about to be invoked\n\t */",
            "\t/**\n\t * 使用指定详细消息和目标实例创建新的 {@code InvocationRejectedException}。\n\t * @param msg 详细消息\n\t * @param target 即将被调用的目标实例\n\t */",
        ),
        (
            "\t/**\n\t * Return the target instance that was about to be invoked.\n\t */",
            "\t/**\n\t * 返回即将被调用的目标实例。\n\t */",
        ),
    ],
    "ConcurrencyLimit.java": [
        (
            "/**\n * A common annotation specifying a concurrency limit for an individual method,\n * or for all proxy-invoked methods in a given class hierarchy if annotated at\n * the type level. The default behavior is to block further method invocations\n * when the limit has been reached. Alternatively, further invocations can be\n * rejected through configuring {@link #policy()} as {@code policy = REJECT}.\n *\n * <p>In the type-level case, all methods inheriting the concurrency limit\n * from the type level share a common concurrency throttle, with any mix\n * of such method invocations contributing to the shared concurrency limit.\n * Whereas for a locally annotated method, a local throttle with the specified\n * limit is going to be applied to invocations of that particular method only.\n *\n * <p>This is particularly useful with Virtual Threads where there is generally\n * no thread pool limit in place. For asynchronous tasks, this can be constrained\n * on {@link org.springframework.core.task.SimpleAsyncTaskExecutor}. For\n * synchronous invocations, this annotation provides equivalent behavior through\n * {@link org.springframework.aop.interceptor.ConcurrencyThrottleInterceptor}.\n * Alternatively, consider {@link org.springframework.core.task.SyncTaskExecutor}\n * and its inherited concurrency throttling support (new as of 7.0) for\n * programmatic use.\n *\n * @author Juergen Hoeller\n * @author Hyunsang Han\n * @author Sam Brannen\n * @since 7.0\n * @see EnableResilientMethods\n * @see ConcurrencyLimitBeanPostProcessor\n * @see org.springframework.aop.interceptor.ConcurrencyThrottleInterceptor\n * @see org.springframework.core.task.SyncTaskExecutor#setConcurrencyLimit\n * @see org.springframework.core.task.SimpleAsyncTaskExecutor#setConcurrencyLimit\n */",
            "/**\n * 为单个方法指定并发上限的通用注解；\n * 若在类型级别标注，则对给定类层次结构中所有经代理调用的方法生效。\n * 默认行为是在达到上限时阻塞后续方法调用。\n * 也可通过将 {@link #policy()} 配置为 {@code policy = REJECT} 来拒绝后续调用。\n *\n * <p>类型级别场景下，从类型继承并发上限的所有方法共享同一并发节流器，\n * 任意此类方法调用均计入共享并发上限。\n * 而局部标注的方法则仅对该方法调用应用具有指定上限的局部节流器。\n *\n * <p>在虚拟线程场景下尤其有用，因为通常不存在线程池上限。\n * 异步任务可在 {@link org.springframework.core.task.SimpleAsyncTaskExecutor} 上约束。\n * 同步调用时，本注解通过\n * {@link org.springframework.aop.interceptor.ConcurrencyThrottleInterceptor} 提供等价行为。\n * 编程式用法也可考虑 {@link org.springframework.core.task.SyncTaskExecutor}\n * 及其继承的并发节流支持（7.0 新增）。\n *\n * @author Juergen Hoeller\n * @author Hyunsang Han\n * @author Sam Brannen\n * @since 7.0\n * @see EnableResilientMethods\n * @see ConcurrencyLimitBeanPostProcessor\n * @see org.springframework.aop.interceptor.ConcurrencyThrottleInterceptor\n * @see org.springframework.core.task.SyncTaskExecutor#setConcurrencyLimit\n * @see org.springframework.core.task.SimpleAsyncTaskExecutor#setConcurrencyLimit\n */",
        ),
        (
            "\t/**\n\t * Alias for {@link #limit()}.\n\t * <p>Intended to be used when no other attributes are needed &mdash; for\n\t * example, {@code @ConcurrencyLimit(5)}.\n\t * @see #limitString()\n\t */",
            "\t/**\n\t * {@link #limit()} 的别名。\n\t * <p>在无需其他属性时使用 &mdash; 例如 {@code @ConcurrencyLimit(5)}。\n\t * @see #limitString()\n\t */",
        ),
        (
            "\t/**\n\t * The concurrency limit.\n\t * <p>Specify {@code 1} to effectively lock the target instance for each method\n\t * invocation.\n\t * <p>Specify a limit greater than {@code 1} for pool-like throttling, constraining\n\t * the number of concurrent invocations similar to the upper bound of a pool.\n\t * <p>Specify {@code -1} for unbounded concurrency.\n\t * @see #value()\n\t * @see #limitString()\n\t * @see org.springframework.util.ConcurrencyThrottleSupport#UNBOUNDED_CONCURRENCY\n\t */",
            "\t/**\n\t * 并发上限。\n\t * <p>指定 {@code 1} 可有效地在每次方法调用时锁定目标实例。\n\t * <p>指定大于 {@code 1} 的上限可实现类池化节流，\n\t * 限制并发调用数，类似线程池上限。\n\t * <p>指定 {@code -1} 表示无界并发。\n\t * @see #value()\n\t * @see #limitString()\n\t * @see org.springframework.util.ConcurrencyThrottleSupport#UNBOUNDED_CONCURRENCY\n\t */",
        ),
        (
            "\t/**\n\t * The concurrency limit, as a configurable String.\n\t * <p>A non-empty value specified here overrides the {@link #limit()} and\n\t * {@link #value()} attributes.\n\t * <p>This supports Spring-style \"${...}\" placeholders as well as SpEL expressions.\n\t * <p>See the Javadoc for {@link #limit()} for details on supported values.\n\t * @see #limit()\n\t * @see org.springframework.util.ConcurrencyThrottleSupport#UNBOUNDED_CONCURRENCY\n\t */",
            "\t/**\n\t * 可配置字符串形式的并发上限。\n\t * <p>此处指定非空值将覆盖 {@link #limit()} 与 {@link #value()} 属性。\n\t * <p>支持 Spring 风格 \"${...}\" 占位符及 SpEL 表达式。\n\t * <p>支持的值详见 {@link #limit()} 的 Javadoc。\n\t * @see #limit()\n\t * @see org.springframework.util.ConcurrencyThrottleSupport#UNBOUNDED_CONCURRENCY\n\t */",
        ),
        (
            "\t/**\n\t * The policy for throttling method invocations when the limit has been reached.\n\t * <p>The default behavior is to block further concurrent invocations once the\n\t * specified limit has been reached: {@link ThrottlePolicy#BLOCK}.\n\t * <p>Switch this policy to {@code REJECT} for rejecting further invocations instead,\n\t * throwing {@link org.springframework.resilience.InvocationRejectedException}\n\t * (which extends the common {@link java.util.concurrent.RejectedExecutionException})\n\t * on any further concurrent invocation attempts: {@link ThrottlePolicy#REJECT}.\n\t * @since 7.0.3\n\t */",
            "\t/**\n\t * 达到并发上限时对方法调用施加节流的策略。\n\t * <p>默认行为是在达到指定上限后阻塞后续并发调用：{@link ThrottlePolicy#BLOCK}。\n\t * <p>将策略切换为 {@code REJECT} 可改为拒绝后续调用，\n\t * 在进一步并发调用尝试时抛出 {@link org.springframework.resilience.InvocationRejectedException}\n\t *（继承通用 {@link java.util.concurrent.RejectedExecutionException}）：\n\t * {@link ThrottlePolicy#REJECT}。\n\t * @since 7.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Policy to apply for throttling method invocations when the limit has been reached.\n\t * @since 7.0.3\n\t */",
            "\t/**\n\t * 达到并发上限时对方法调用施加节流所应用的策略。\n\t * @since 7.0.3\n\t */",
        ),
        (
            "\t\t/**\n\t\t * The default: block until we can invoke the method within the configured limit.\n\t\t */",
            "\t\t/**\n\t\t * 默认策略：阻塞直至能在配置上限内调用方法。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Alternative: reject further method invocations once the limit has been reached.\n\t\t * @see org.springframework.resilience.InvocationRejectedException\n\t\t */",
            "\t\t/**\n\t\t * 备选策略：达到上限后拒绝后续方法调用。\n\t\t * @see org.springframework.resilience.InvocationRejectedException\n\t\t */",
        ),
    ],
    "ConcurrencyLimitBeanPostProcessor.java": [
        (
            "/**\n * A convenient {@link org.springframework.beans.factory.config.BeanPostProcessor\n * BeanPostProcessor} that applies a concurrency interceptor to all bean methods\n * annotated with {@link ConcurrencyLimit @ConcurrencyLimit}.\n *\n * @author Juergen Hoeller\n * @author Hyunsang Han\n * @since 7.0\n */",
            "/**\n * 便捷的 {@link org.springframework.beans.factory.config.BeanPostProcessor\n * BeanPostProcessor}，为所有带 {@link ConcurrencyLimit @ConcurrencyLimit} 注解的\n * Bean 方法应用并发拦截器。\n *\n * @author Juergen Hoeller\n * @author Hyunsang Han\n * @since 7.0\n */",
        ),
    ],
    "EnableResilientMethods.java": [
        (
            "/**\n * Enables Spring's core resilience features for method invocations:\n * {@link Retryable @Retryable} as well as {@link ConcurrencyLimit @ConcurrencyLimit}.\n *\n * <p>These annotations can also be individually enabled by\n * defining a {@link RetryAnnotationBeanPostProcessor} or a\n * {@link ConcurrencyLimitBeanPostProcessor}.\n *\n * @author Juergen Hoeller\n * @since 7.0\n * @see RetryAnnotationBeanPostProcessor\n * @see ConcurrencyLimitBeanPostProcessor\n */",
            "/**\n * 启用 Spring 方法调用的核心弹性特性：\n * {@link Retryable @Retryable} 与 {@link ConcurrencyLimit @ConcurrencyLimit}。\n *\n * <p>也可通过分别定义 {@link RetryAnnotationBeanPostProcessor} 或\n * {@link ConcurrencyLimitBeanPostProcessor} 单独启用这些注解。\n *\n * @author Juergen Hoeller\n * @since 7.0\n * @see RetryAnnotationBeanPostProcessor\n * @see ConcurrencyLimitBeanPostProcessor\n */",
        ),
        (
            "\t/**\n\t * Indicate whether subclass-based (CGLIB) proxies are to be created as opposed\n\t * to standard Java interface-based proxies.\n\t * <p>The default is {@code false}.\n\t * <p>Note that setting this attribute to {@code true} will only affect\n\t * {@link RetryAnnotationBeanPostProcessor} and\n\t * {@link ConcurrencyLimitBeanPostProcessor}.\n\t * <p>It is usually recommendable to rely on a global default proxy configuration\n\t * instead, with specific proxy requirements for certain beans expressed through\n\t * a {@link org.springframework.context.annotation.Proxyable} annotation on\n\t * the affected bean classes.\n\t * @see org.springframework.aop.config.AopConfigUtils#forceAutoProxyCreatorToUseClassProxying\n\t */",
            "\t/**\n\t * 指示是否创建基于子类（CGLIB）的代理，而非标准 Java 接口代理。\n\t * <p>默认为 {@code false}。\n\t * <p>注意：将此属性设为 {@code true} 仅影响\n\t * {@link RetryAnnotationBeanPostProcessor} 与\n\t * {@link ConcurrencyLimitBeanPostProcessor}。\n\t * <p>通常建议依赖全局默认代理配置，\n\t * 对特定 Bean 的代理需求通过受影响 Bean 类上的\n\t * {@link org.springframework.context.annotation.Proxyable} 注解表达。\n\t * @see org.springframework.aop.config.AopConfigUtils#forceAutoProxyCreatorToUseClassProxying\n\t */",
        ),
        (
            "\t/**\n\t * Indicate the order in which the {@link RetryAnnotationBeanPostProcessor}\n\t * and {@link ConcurrencyLimitBeanPostProcessor} should be applied.\n\t * <p>The default is {@link Ordered#LOWEST_PRECEDENCE - 1} in order to run\n\t * after all common post-processors, except for {@code @EnableAsync}.\n\t * @see org.springframework.scheduling.annotation.EnableAsync#order()\n\t */",
            "\t/**\n\t * 指示 {@link RetryAnnotationBeanPostProcessor} 与\n\t * {@link ConcurrencyLimitBeanPostProcessor} 的应用顺序。\n\t * <p>默认为 {@link Ordered#LOWEST_PRECEDENCE - 1}，以便在除 {@code @EnableAsync}\n\t * 外的所有常见后处理器之后运行。\n\t * @see org.springframework.scheduling.annotation.EnableAsync#order()\n\t */",
        ),
    ],
    "ResilientMethodsConfiguration.java": [
        (
            "/**\n * {@code @Configuration} class that registers the Spring infrastructure beans necessary\n * to enable proxy-based method invocations with retry and concurrency limit behavior.\n *\n * @author Juergen Hoeller\n * @since 7.0\n * @see EnableResilientMethods\n * @see RetryAnnotationBeanPostProcessor\n * @see ConcurrencyLimitBeanPostProcessor\n */",
            "/**\n * 注册启用基于代理的方法调用（含重试与并发上限行为）所需 Spring 基础设施 Bean 的\n * {@code @Configuration} 类。\n *\n * @author Juergen Hoeller\n * @since 7.0\n * @see EnableResilientMethods\n * @see RetryAnnotationBeanPostProcessor\n * @see ConcurrencyLimitBeanPostProcessor\n */",
        ),
    ],
    "RetryAnnotationBeanPostProcessor.java": [
        (
            "/**\n * A convenient {@link org.springframework.beans.factory.config.BeanPostProcessor\n * BeanPostProcessor} that applies a retry interceptor to all bean methods\n * annotated with {@link Retryable @Retryable}.\n *\n * @author Juergen Hoeller\n * @since 7.0\n */",
            "/**\n * 便捷的 {@link org.springframework.beans.factory.config.BeanPostProcessor\n * BeanPostProcessor}，为所有带 {@link Retryable @Retryable} 注解的\n * Bean 方法应用重试拦截器。\n *\n * @author Juergen Hoeller\n * @since 7.0\n */",
        ),
    ],
    "MethodRetryPredicate.java": [
        (
            "/**\n * Predicate for retrying a {@link Throwable} from a specific {@link Method}.\n *\n * @author Juergen Hoeller\n * @since 7.0\n * @see MethodRetrySpec#predicate()\n */",
            "/**\n * 针对特定 {@link Method} 抛出的 {@link Throwable} 判断是否重试的谓词。\n *\n * @author Juergen Hoeller\n * @since 7.0\n * @see MethodRetrySpec#predicate()\n */",
        ),
        (
            "\t/**\n\t * Determine whether the given {@code Method} should be retried after\n\t * throwing the given {@code Throwable}.\n\t * @param method the method to potentially retry\n\t * @param throwable the exception encountered\n\t */",
            "\t/**\n\t * 判断给定 {@code Method} 在抛出给定 {@code Throwable} 后是否应重试。\n\t * @param method 可能重试的方法\n\t * @param throwable 遇到的异常\n\t */",
        ),
        (
            "\t/**\n\t * Build a {@code Predicate} for testing exceptions from a given method.\n\t * @param method the method to build a predicate for\n\t */",
            "\t/**\n\t * 为给定方法构建用于测试异常的 {@code Predicate}。\n\t * @param method 要构建谓词的方法\n\t */",
        ),
    ],
    "MethodRetrySpec.java": [
        (
            "/**\n * A specification for retry attempts on a given method, combining common\n * retry characteristics. This roughly matches the annotation attributes\n * on {@link org.springframework.resilience.annotation.Retryable}.\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 7.0\n * @param includes applicable exception types to attempt a retry for\n * @param excludes non-applicable exception types to avoid a retry for\n * @param predicate a predicate for filtering exceptions from applicable methods\n * @param maxRetries the maximum number of retry attempts\n * @param timeout the maximum amount of elapsed time allowed for the initial\n * invocation and any subsequent retry attempts, including delays\n * @param delay the base delay after the initial invocation\n * @param jitter a jitter value for the next retry attempt\n * @param multiplier a multiplier for a delay for the next retry attempt\n * @param maxDelay the maximum delay for any retry attempt\n * @see AbstractRetryInterceptor#getRetrySpec\n * @see SimpleRetryInterceptor#SimpleRetryInterceptor(MethodRetrySpec)\n * @see org.springframework.resilience.annotation.Retryable\n */",
            "/**\n * 给定方法重试尝试的规范，组合常见重试特征。\n * 大致对应 {@link org.springframework.resilience.annotation.Retryable} 的注解属性。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 7.0\n * @param includes 应尝试重试的适用异常类型\n * @param excludes 应避免重试的不适用异常类型\n * @param predicate 过滤适用方法异常的谓词\n * @param maxRetries 最大重试次数\n * @param timeout 初始调用及后续重试（含延迟）允许的最大耗时\n * @param delay 初始调用后的基础延迟\n * @param jitter 下次重试的抖动值\n * @param multiplier 下次重试延迟的乘数\n * @param maxDelay 任意重试尝试的最大延迟\n * @see AbstractRetryInterceptor#getRetrySpec\n * @see SimpleRetryInterceptor#SimpleRetryInterceptor(MethodRetrySpec)\n * @see org.springframework.resilience.annotation.Retryable\n */",
        ),
        (
            "\t/**\n\t * Construct a new {@code MethodRetryPredicate} with the supplied arguments.\n\t */\n\tpublic MethodRetrySpec(MethodRetryPredicate predicate, long maxRetries, Duration delay) {",
            "\t/**\n\t * 使用给定参数构造新的 {@code MethodRetrySpec}。\n\t */\n\tpublic MethodRetrySpec(MethodRetryPredicate predicate, long maxRetries, Duration delay) {",
        ),
        (
            "\t/**\n\t * Construct a new {@code MethodRetryPredicate} with the supplied arguments.\n\t */\n\tpublic MethodRetrySpec(MethodRetryPredicate predicate, long maxRetries, Duration delay,\n\t\t\tDuration jitter, double multiplier, Duration maxDelay) {",
            "\t/**\n\t * 使用给定参数构造新的 {@code MethodRetrySpec}。\n\t */\n\tpublic MethodRetrySpec(MethodRetryPredicate predicate, long maxRetries, Duration delay,\n\t\t\tDuration jitter, double multiplier, Duration maxDelay) {",
        ),
        (
            "\t/**\n\t * Construct a new {@code MethodRetryPredicate} with the supplied arguments.\n\t * @deprecated as of Spring Framework 7.0.2, in favor of\n\t * {@link #MethodRetrySpec(Collection, Collection, MethodRetryPredicate, long, Duration, Duration, Duration, double, Duration)}\n\t */",
            "\t/**\n\t * 使用给定参数构造新的 {@code MethodRetrySpec}。\n\t * @deprecated 自 Spring Framework 7.0.2 起，请改用\n\t * {@link #MethodRetrySpec(Collection, Collection, MethodRetryPredicate, long, Duration, Duration, Duration, double, Duration)}\n\t */",
        ),
    ],
    "SimpleRetryInterceptor.java": [
        (
            "/**\n * A simple concrete retry interceptor based on a given {@link MethodRetrySpec}.\n *\n * @author Juergen Hoeller\n * @since 7.0\n */",
            "/**\n * 基于给定 {@link MethodRetrySpec} 的简单具体重试拦截器。\n *\n * @author Juergen Hoeller\n * @since 7.0\n */",
        ),
        (
            "\t/**\n\t * Create a {@code SimpleRetryInterceptor} for the given {@link MethodRetrySpec}.\n\t * @param retrySpec the specification to use for all method invocations\n\t */",
            "\t/**\n\t * 为给定 {@link MethodRetrySpec} 创建 {@code SimpleRetryInterceptor}。\n\t * @param retrySpec 用于所有方法调用的规范\n\t */",
        ),
    ],
    "SchedulingAwareRunnable.java": [
        (
            "/**\n * Extension of the {@link Runnable} interface, adding special callbacks\n * for long-running operations.\n *\n * <p>Scheduling-capable TaskExecutors are encouraged to check a submitted\n * Runnable, detecting whether this interface is implemented and reacting\n * as appropriately as they are able to.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.core.task.TaskExecutor\n * @see SchedulingTaskExecutor\n */",
            "/**\n * {@link Runnable} 接口的扩展，为长时间运行操作添加特殊回调。\n *\n * <p>建议具备调度能力的 TaskExecutor 检查提交的 Runnable，\n * 检测是否实现本接口并尽可能作出相应处理。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.core.task.TaskExecutor\n * @see SchedulingTaskExecutor\n */",
        ),
        (
            "\t/**\n\t * Return whether the Runnable's operation is long-lived\n\t * ({@code true}) versus short-lived ({@code false}).\n\t * <p>In the former case, the task will not allocate a thread from the thread\n\t * pool (if any) but rather be considered as long-running background thread.\n\t * <p>This should be considered a hint. Of course TaskExecutor implementations\n\t * are free to ignore this flag and the SchedulingAwareRunnable interface overall.\n\t * <p>The default implementation returns {@code false}, as of 6.1.\n\t */",
            "\t/**\n\t * 返回 Runnable 的操作是否为长生命周期（{@code true}）而非短生命周期（{@code false}）。\n\t * <p>前者情况下，任务不会从线程池（若有）分配线程，\n\t * 而是视为长时间运行的后台线程。\n\t * <p>这应视为提示。TaskExecutor 实现当然可忽略此标志及 SchedulingAwareRunnable 接口本身。\n\t * <p>默认实现返回 {@code false}（自 6.1 起）。\n\t */",
        ),
        (
            "\t/**\n\t * Return a qualifier associated with this Runnable.\n\t * <p>The default implementation returns {@code null}.\n\t * <p>May be used for custom purposes depending on the scheduler implementation.\n\t * {@link org.springframework.scheduling.config.TaskSchedulerRouter} introspects\n\t * this qualifier in order to determine the target scheduler to be used\n\t * for a given Runnable, matching the qualifier value (or the bean name)\n\t * of a specific {@link org.springframework.scheduling.TaskScheduler} or\n\t * {@link java.util.concurrent.ScheduledExecutorService} bean definition.\n\t * @since 6.1\n\t * @see org.springframework.scheduling.annotation.Scheduled#scheduler()\n\t */",
            "\t/**\n\t * 返回与此 Runnable 关联的限定符。\n\t * <p>默认实现返回 {@code null}。\n\t * <p>可根据调度器实现用于自定义目的。\n\t * {@link org.springframework.scheduling.config.TaskSchedulerRouter} 内省此限定符，\n\t * 以确定给定 Runnable 应使用的目标调度器，\n\t * 匹配特定 {@link org.springframework.scheduling.TaskScheduler} 或\n\t * {@link java.util.concurrent.ScheduledExecutorService} Bean 定义的限定符值（或 Bean 名称）。\n\t * @since 6.1\n\t * @see org.springframework.scheduling.annotation.Scheduled#scheduler()\n\t */",
        ),
    ],
    "SchedulingException.java": [
        (
            "/**\n * General exception to be thrown on scheduling failures,\n * such as the scheduler already having shut down.\n * Unchecked since scheduling failures are usually fatal.\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 调度失败时抛出的通用异常，\n * 例如调度器已关闭。\n * 为 unchecked 异常，因为调度失败通常不可恢复。\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Constructor for SchedulingException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * {@code SchedulingException} 构造器。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for SchedulingException.\n\t * @param msg the detail message\n\t * @param cause the root cause (usually from using an underlying\n\t * scheduling API such as Quartz)\n\t */",
            "\t/**\n\t * {@code SchedulingException} 构造器。\n\t * @param msg 详细消息\n\t * @param cause 根本原因（通常来自底层调度 API，如 Quartz）\n\t */",
        ),
    ],
    "SchedulingTaskExecutor.java": [
        (
            "/**\n * A {@link org.springframework.core.task.TaskExecutor} extension exposing\n * scheduling characteristics that are relevant to potential task submitters.\n *\n * <p>Scheduling clients are encouraged to submit\n * {@link Runnable Runnables} that match the exposed preferences\n * of the {@code TaskExecutor} implementation in use.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see SchedulingAwareRunnable\n * @see org.springframework.core.task.TaskExecutor\n */",
            "/**\n * 暴露与潜在任务提交者相关的调度特征的\n * {@link org.springframework.core.task.TaskExecutor} 扩展。\n *\n * <p>建议调度客户端提交与所用 {@code TaskExecutor} 实现\n * 所暴露偏好相匹配的 {@link Runnable Runnables}。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see SchedulingAwareRunnable\n * @see org.springframework.core.task.TaskExecutor\n */",
        ),
        (
            "\t/**\n\t * Does this {@code TaskExecutor} prefer short-lived tasks over long-lived tasks?\n\t * <p>A {@code SchedulingTaskExecutor} implementation can indicate whether it\n\t * prefers submitted tasks to perform as little work as it can within a single\n\t * task execution. For example, submitted tasks might break a repeated loop into\n\t * individual subtasks which submit a follow-up task afterwards (if feasible).\n\t * <p>This should be considered a hint. Of course {@code TaskExecutor} clients\n\t * are free to ignore this flag and hence the {@code SchedulingTaskExecutor}\n\t * interface overall. However, thread pools will usually indicate a preference\n\t * for short-lived tasks, allowing for more fine-grained scheduling.\n\t * @return {@code true} if this executor prefers short-lived tasks (the default),\n\t * {@code false} otherwise (for treatment like a regular {@code TaskExecutor})\n\t */",
            "\t/**\n\t * 此 {@code TaskExecutor} 是否偏好短生命周期任务而非长生命周期任务？\n\t * <p>{@code SchedulingTaskExecutor} 实现可指示是否偏好提交的任务\n\t * 在单次任务执行中尽可能少做工作。例如，提交的任务可将重复循环拆分为\n\t * 独立子任务，随后再提交后续任务（若可行）。\n\t * <p>这应视为提示。{@code TaskExecutor} 客户端当然可忽略此标志及\n\t * {@code SchedulingTaskExecutor} 接口本身。不过线程池通常会表明\n\t * 偏好短生命周期任务，以实现更细粒度的调度。\n\t * @return 若此执行器偏好短生命周期任务（默认）则返回 {@code true}，\n\t * 否则返回 {@code false}（按常规 {@code TaskExecutor} 处理）\n\t */",
        ),
    ],
    "Trigger.java": [
        (
            "/**\n * Common interface for trigger objects that determine the next execution time\n * of a task that they get associated with.\n *\n * @author Juergen Hoeller\n * @since 3.0\n * @see TaskScheduler#schedule(Runnable, Trigger)\n * @see org.springframework.scheduling.support.CronTrigger\n */",
            "/**\n * 确定所关联任务下次执行时间的触发器对象通用接口。\n *\n * @author Juergen Hoeller\n * @since 3.0\n * @see TaskScheduler#schedule(Runnable, Trigger)\n * @see org.springframework.scheduling.support.CronTrigger\n */",
        ),
        (
            "\t/**\n\t * Determine the next execution time according to the given trigger context.\n\t * <p>The default implementation delegates to {@link #nextExecution(TriggerContext)}.\n\t * @param triggerContext context object encapsulating last execution times\n\t * and last completion time\n\t * @return the next execution time as defined by the trigger,\n\t * or {@code null} if the trigger won't fire anymore\n\t * @deprecated as of 6.0, in favor of {@link #nextExecution(TriggerContext)}\n\t */",
            "\t/**\n\t * 根据给定触发器上下文确定下次执行时间。\n\t * <p>默认实现委托给 {@link #nextExecution(TriggerContext)}。\n\t * @param triggerContext 封装上次执行时间与上次完成时间的上下文对象\n\t * @return 触发器定义的下次执行时间，\n\t * 若触发器不再触发则返回 {@code null}\n\t * @deprecated 自 6.0 起，请改用 {@link #nextExecution(TriggerContext)}\n\t */",
        ),
        (
            "\t/**\n\t * Determine the next execution time according to the given trigger context.\n\t * @param triggerContext context object encapsulating last execution times\n\t * and last completion time\n\t * @return the next execution time as defined by the trigger,\n\t * or {@code null} if the trigger won't fire anymore\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 根据给定触发器上下文确定下次执行时间。\n\t * @param triggerContext 封装上次执行时间与上次完成时间的上下文对象\n\t * @return 触发器定义的下次执行时间，\n\t * 若触发器不再触发则返回 {@code null}\n\t * @since 6.0\n\t */",
        ),
    ],
    "AbstractAsyncConfiguration.java": [
        (
            "/**\n * Abstract base {@code Configuration} class providing common structure for enabling\n * Spring's asynchronous method execution capability.\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @since 3.1\n * @see EnableAsync\n */",
            "/**\n * 启用 Spring 异步方法执行能力的抽象基类 {@code Configuration}，\n * 提供通用结构。\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @since 3.1\n * @see EnableAsync\n */",
        ),
        (
            "\t/**\n\t * Collect any {@link AsyncConfigurer} beans through autowiring.\n\t */",
            "\t/**\n\t * 通过自动装配收集所有 {@link AsyncConfigurer} Bean。\n\t */",
        ),
    ],
}


# Large files: load replacements from companion data embedded below
def _load_large_replacements() -> dict[str, list[tuple[str, str]]]:
    from pathlib import Path as P
    data_path = P(__file__).with_name("annotate_springframework_wave11_batch0_20_large.py")
    if data_path.exists():
        ns: dict = {}
        exec(data_path.read_text(encoding="utf-8"), ns)  # noqa: S102
        return ns.get("LARGE_REPLACEMENTS", {})
    return {}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def mark_batch_done(batch: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    for rel in batch:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
    done_path.write_text("\n".join(done) + "\n", encoding="utf-8")


def main() -> int:
    large = _load_large_replacements()
    all_replacements = {**FILE_REPLACEMENTS, **large}
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
        reps = all_replacements.get(name, [])
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
    if ok == len(BATCH_FILES) and not failures:
        mark_batch_done(BATCH_FILES)
        print(f"Marked {ok} files done")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
