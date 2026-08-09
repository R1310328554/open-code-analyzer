"""Chinese OCA + JavaDoc replacements for Spring Framework 7.0.8 wave30b mega batch [10:20]."""

from __future__ import annotations

# Each entry: (old, new) — applied in order after fresh copy from original.

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# ---------------------------------------------------------------------------
# AbstractReflectiveMBeanInfoAssembler.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["AbstractReflectiveMBeanInfoAssembler.java"] = [
    (
        "package org.springframework.jmx.export.assembler;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "JMX MBeanInfo 反射组装基类：扫描托管 Bean 的 getter/setter/方法，"
        "生成 ModelMBean 属性与操作元数据；具体暴露哪些成员由子类 includeXXX 投票决定。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.jmx.export.assembler;",
    ),
    (
        "/**\n * Builds on the {@link AbstractMBeanInfoAssembler} superclass to\n"
        " * add a basic algorithm for building metadata based on the\n"
        " * reflective metadata of the MBean class.\n"
        " *\n"
        " * <p>The logic for creating MBean metadata from the reflective metadata\n"
        " * is contained in this class, but this class makes no decisions as to\n"
        " * which methods and properties are to be exposed. Instead, it gives\n"
        " * subclasses a chance to 'vote' on each property or method through\n"
        " * the {@code includeXXX} methods.\n"
        " *\n"
        " * <p>Subclasses are also given the opportunity to populate attribute\n"
        " * and operation metadata with additional descriptors once the metadata\n"
        " * is assembled through the {@code populateXXXDescriptor} methods.\n"
        " *\n"
        " * @author Rob Harrop\n"
        " * @author Juergen Hoeller\n"
        " * @author David Boden\n"
        " * @since 1.2\n"
        " * @see #includeOperation\n"
        " * @see #includeReadAttribute\n"
        " * @see #includeWriteAttribute\n"
        " * @see #populateAttributeDescriptor\n"
        " * @see #populateOperationDescriptor\n"
        " */\n"
        "public abstract class AbstractReflectiveMBeanInfoAssembler",
        "/* ===== [OCA 中文解析] =====\n"
        "class AbstractReflectiveMBeanInfoAssembler — 意图说明\n\n"
        "反射驱动的 MBean 元数据工厂：把 JavaBean 属性与 public 方法映射为 JMX Attribute/Operation。"
        "子类通过 includeReadAttribute/includeWriteAttribute/includeOperation 过滤暴露面，"
        "populateXXXDescriptor 可补充 Descriptor 细节。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 在 {@link AbstractMBeanInfoAssembler} 之上，基于 MBean 类的反射元数据\n"
        " * 构建 ModelMBean 属性与操作信息的基础算法。\n"
        " *\n"
        " * <p>本类负责「如何从反射得到 MBean 元数据」，但不决定暴露哪些成员；"
        "子类通过 {@code includeXXX} 方法对每个属性/方法「投票」。\n"
        " *\n"
        " * <p>元数据组装完成后，子类还可通过 {@code populateXXXDescriptor} "
        "向 Descriptor 写入额外信息。\n"
        " *\n"
        " * @author Rob Harrop\n"
        " * @author Juergen Hoeller\n"
        " * @author David Boden\n"
        " * @since 1.2\n"
        " * @see #includeOperation\n"
        " * @see #includeReadAttribute\n"
        " * @see #includeWriteAttribute\n"
        " * @see #populateAttributeDescriptor\n"
        " * @see #populateOperationDescriptor\n"
        " */\n"
        "public abstract class AbstractReflectiveMBeanInfoAssembler",
    ),
    (
        "\t/**\n"
        "\t * Allows subclasses to vote on the inclusion of a particular attribute accessor.\n"
        "\t * @param method the accessor {@code Method}\n"
        "\t * @param beanKey the key associated with the MBean in the beans map\n"
        "\t * of the {@code MBeanExporter}\n"
        "\t * @return {@code true} if the accessor should be included in the management interface,\n"
        "\t * otherwise {@code false}\n"
        "\t */",
        "\t/**\n"
        "\t * 允许子类对是否纳入某属性 accessor（getter）投票。\n"
        "\t * @param method accessor {@code Method}\n"
        "\t * @param beanKey {@code MBeanExporter} beans 映射中的 MBean 键\n"
        "\t * @return 应纳入管理接口时返回 {@code true}，否则 {@code false}\n"
        "\t */",
    ),
    (
        "\t/**\n"
        "\t * Allows subclasses to vote on the inclusion of a particular operation.\n"
        "\t * @param method the operation method\n"
        "\t * @param beanKey the key associated with the MBean in the beans map\n"
        "\t * of the {@code MBeanExporter}\n"
        "\t * @return whether the operation should be included in the management interface\n"
        "\t */",
        "\t/**\n"
        "\t * 允许子类对是否纳入某操作方法投票。\n"
        "\t * @param method 操作方法\n"
        "\t * @param beanKey {@code MBeanExporter} beans 映射中的 MBean 键\n"
        "\t * @return 应纳入管理接口时返回 {@code true}\n"
        "\t */",
    ),
]

# ---------------------------------------------------------------------------
# ScheduledAnnotationBeanPostProcessor.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["ScheduledAnnotationBeanPostProcessor.java"] = [
    (
        "package org.springframework.scheduling.annotation;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "@Scheduled 注解的 Bean 后处理器：容器就绪后扫描带 @Scheduled 的方法，"
        "按 fixedRate/fixedDelay/cron 等表达式向 TaskScheduler 注册定时任务；"
        "与 @EnableScheduling / task:annotation-driven 配合使用。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.scheduling.annotation;",
    ),
    (
        "/**\n * Bean post-processor that registers methods annotated with\n"
        " * {@link Scheduled @Scheduled} to be invoked by a\n"
        " * {@link org.springframework.scheduling.TaskScheduler} according to the\n"
        ' * "fixedRate", "fixedDelay", or "cron" expression provided via the annotation.\n',
        "/* ===== [OCA 中文解析] =====\n"
        "class ScheduledAnnotationBeanPostProcessor — 意图说明\n\n"
        "定时任务注册器：在 afterSingletonsInstantiated 阶段汇总所有 @Scheduled 方法，"
        "解析 SpEL/占位符后创建 CronTask、FixedRateTask 等并交给 ScheduledTaskRegistrar。"
        "支持同步与异步（@Async）两种调度路径。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n * 将标注 {@link Scheduled @Scheduled} 的方法注册为由\n"
        " * {@link org.springframework.scheduling.TaskScheduler} 按注解中的\n"
        ' * "fixedRate"、"fixedDelay" 或 "cron" 表达式调度的 Bean 后处理器。\n',
    ),
    (
        "\tpublic void afterSingletonsInstantiated() {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 afterSingletonsInstantiated — 意图与阅读要点\n\n"
        "\t所有单例就绪后的入口：遍历已收集的 @Scheduled 任务并 schedule；"
        "同时发现 SchedulingConfigurer Bean 以定制 registrar。容器关闭时会取消已注册任务。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tpublic void afterSingletonsInstantiated() {",
    ),
    (
        "\tprotected void processScheduled(Scheduled scheduled, Method method, Object bean) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 processScheduled — 意图与阅读要点\n\n"
        "\t解析单个 @Scheduled：区分 sync/async，读取 cron/fixedDelay/fixedRate/initialDelay，"
        "支持 Duration 与 zone；最终构造 Runnable 并注册到 registrar。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tprotected void processScheduled(Scheduled scheduled, Method method, Object bean) {",
    ),
]

# ---------------------------------------------------------------------------
# ExecutorConfigurationSupport.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["ExecutorConfigurationSupport.java"] = [
    (
        "package org.springframework.scheduling.concurrent;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "线程池/ExecutorService 配置的抽象基类：统一线程命名、池大小、拒绝策略、"
        "优雅关闭与虚拟线程开关；ThreadPoolTaskExecutor 等均继承此类。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.scheduling.concurrent;",
    ),
    (
        "/**\n * Base class for setting up a {@link java.util.concurrent.ExecutorService}\n"
        " * (typically a {@link java.util.concurrent.ThreadPoolExecutor} or\n"
        " * {@link java.util.concurrent.ScheduledThreadPoolExecutor}).\n"
        " *\n"
        " * <p>Defines common configuration settings and common lifecycle handling,\n"
        " * inheriting thread customization options (name, priority, etc) from\n"
        " * {@link org.springframework.util.CustomizableThreadCreator}.\n"
        " *\n"
        " * @author Juergen Hoeller\n"
        " * @since 3.0\n"
        " * @see java.util.concurrent.ExecutorService\n"
        " * @see java.util.concurrent.Executors\n"
        " * @see java.util.concurrent.ThreadPoolExecutor\n"
        " * @see java.util.concurrent.ScheduledThreadPoolExecutor\n"
        " */\n"
        "@SuppressWarnings(\"serial\")\n"
        "public abstract class ExecutorConfigurationSupport",
        "/* ===== [OCA 中文解析] =====\n"
        "class ExecutorConfigurationSupport — 意图说明\n\n"
        "Executor 生命周期模板：afterPropertiesSet 创建线程池，ContextClosedEvent 触发分阶段 shutdown，"
        "SmartLifecycle 控制启动/停止相位。子类实现 initializeExecutor 提供具体 ExecutorService。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 配置 {@link java.util.concurrent.ExecutorService} 的基类\n"
        " * （通常为 {@link java.util.concurrent.ThreadPoolExecutor} 或\n"
        " * {@link java.util.concurrent.ScheduledThreadPoolExecutor}）。\n"
        " *\n"
        " * <p>定义通用配置与生命周期处理，线程命名/优先级等选项继承自\n"
        " * {@link org.springframework.util.CustomizableThreadCreator}。\n"
        " *\n"
        " * @author Juergen Hoeller\n"
        " * @since 3.0\n"
        " * @see java.util.concurrent.ExecutorService\n"
        " * @see java.util.concurrent.Executors\n"
        " * @see java.util.concurrent.ThreadPoolExecutor\n"
        " * @see java.util.concurrent.ScheduledThreadPoolExecutor\n"
        " */\n"
        "@SuppressWarnings(\"serial\")\n"
        "public abstract class ExecutorConfigurationSupport",
    ),
    (
        "\t/**\n"
        "\t * Specify whether to use virtual threads instead of platform threads.\n"
        "\t * This is off by default, setting up a traditional platform thread pool.\n"
        "\t * <p>Set this flag to {@code true} on Java 21 or higher for a tightly\n"
        "\t * managed thread pool setup with virtual threads. In contrast to\n"
        "\t * {@link SimpleAsyncTaskExecutor}, this is integrated with Spring's\n"
        "\t * lifecycle management for stopping and restarting execution threads,\n"
        "\t * including an early stop signal for a graceful shutdown arrangement.\n"
        "\t * <p>Specify either this or {@link #setThreadFactory}, not both.\n"
        "\t * @since 6.2\n"
        "\t * @see #setThreadFactory\n"
        "\t * @see VirtualThreadTaskExecutor#getVirtualThreadFactory()\n"
        "\t * @see SimpleAsyncTaskExecutor#setVirtualThreads\n"
        "\t */",
        "\t/**\n"
        "\t * 指定是否使用虚拟线程而非平台线程。默认关闭，创建传统平台线程池。\n"
        "\t * <p>在 Java 21+ 设为 {@code true} 可建立受管虚拟线程池；"
        "与 {@link SimpleAsyncTaskExecutor} 不同，本类集成 Spring 生命周期管理。\n"
        "\t * <p>须与 {@link #setThreadFactory} 二选一，不可同时设置。\n"
        "\t * @since 6.2\n"
        "\t * @see #setThreadFactory\n"
        "\t * @see VirtualThreadTaskExecutor#getVirtualThreadFactory()\n"
        "\t * @see SimpleAsyncTaskExecutor#setVirtualThreads\n"
        "\t */",
    ),
    (
        "\tpublic void afterPropertiesSet() {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 afterPropertiesSet — 意图与阅读要点\n\n"
        "\t初始化线程池：校验 poolSize/queueCapacity，按 virtualThreads 选择 VirtualThreadTaskExecutor "
        "或 ThreadPoolExecutor；设置 ThreadFactory 与 RejectedExecutionHandler。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tpublic void afterPropertiesSet() {",
    ),
    (
        "\tpublic void shutdown() {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 shutdown — 意图与阅读要点\n\n"
        "\t优雅关闭：先 shutdown 停止接收新任务，再 awaitTermination；超时后 shutdownNow。"
        "ContextClosedEvent 监听器会按 SmartLifecycle 相位协调多 Executor 关闭顺序。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tpublic void shutdown() {",
    ),
]

# ---------------------------------------------------------------------------
# ScheduledTaskRegistrar.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["ScheduledTaskRegistrar.java"] = [
    (
        "package org.springframework.scheduling.config;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "定时任务注册表：收集 TriggerTask/CronTask/FixedRateTask 等，"
        "在 scheduleTasks 时统一提交到 TaskScheduler 或 ScheduledExecutorService。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.scheduling.config;",
    ),
    (
        "/**\n * Helper bean for registering tasks with a {@link TaskScheduler}, typically using cron\n"
        " * expressions.\n"
        " *\n * <p>{@code ScheduledTaskRegistrar} has a more prominent user-facing role when used in\n"
        " * conjunction with the {@link\n"
        " * org.springframework.scheduling.annotation.EnableAsync @EnableAsync} annotation and its\n"
        " * {@link org.springframework.scheduling.annotation.SchedulingConfigurer\n"
        " * SchedulingConfigurer} callback interface.\n"
        " *\n * @author Juergen Hoeller\n"
        " * @author Chris Beams\n"
        " * @author Tobias Montagna-Hay\n"
        " * @author Sam Brannen\n"
        " * @author Arjen Poutsma\n"
        " * @author Brian Clozel\n"
        " * @since 3.0\n"
        " * @see org.springframework.scheduling.annotation.EnableAsync\n"
        " * @see org.springframework.scheduling.annotation.SchedulingConfigurer\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "class ScheduledTaskRegistrar — 意图说明\n\n"
        "任务注册中心：维护已注册 ScheduledTask 列表，scheduleTasks 时按 Trigger/Cron 表达式"
        "调度到 TaskScheduler；SchedulingConfigurer 可编程式添加任务。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 向 {@link TaskScheduler} 注册定时任务的辅助 Bean，通常使用 cron 表达式。\n"
        " *\n"
        " * <p>与 {@link org.springframework.scheduling.annotation.EnableAsync @EnableAsync} 及\n"
        " * {@link org.springframework.scheduling.annotation.SchedulingConfigurer} 回调配合时，"
        "面向用户的角色更突出。\n"
        " *\n"
        " * @author Juergen Hoeller\n"
        " * @author Chris Beams\n"
        " * @author Tobias Montagna-Hay\n"
        " * @author Sam Brannen\n"
        " * @author Arjen Poutsma\n"
        " * @author Brian Clozel\n"
        " * @since 3.0\n"
        " * @see org.springframework.scheduling.annotation.EnableAsync\n"
        " * @see org.springframework.scheduling.annotation.SchedulingConfigurer\n"
        " */",
    ),
    (
        "\t/**\n"
        "\t * Schedule all registered tasks against the underlying\n"
        "\t * {@linkplain #setTaskScheduler(TaskScheduler) task scheduler}.\n"
        "\t */",
        "\t/**\n"
        "\t * 将所有已注册任务提交到底层 {@linkplain #setTaskScheduler(TaskScheduler) 任务调度器}。\n"
        "\t */",
    ),
    (
        "\tprotected void scheduleTasks() {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 scheduleTasks — 意图与阅读要点\n\n"
        "\t遍历 cronTasks/triggerTasks/fixedDelayTasks/fixedRateTasks/oneTimeTasks，"
        "分别调用 TaskScheduler 的 schedule 方法；已调度任务记录在 scheduledTasks 以便 destroy 时取消。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tpublic void scheduleTasks() {",
    ),
]

# ---------------------------------------------------------------------------
# ScriptFactoryPostProcessor.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["ScriptFactoryPostProcessor.java"] = [
    (
        "package org.springframework.scripting.support;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "脚本 Bean 后处理器：把 ScriptFactory 定义替换为脚本编译后的 Java 对象；"
        "构造参数注入工厂，Bean 属性注入脚本实例——与 FactoryBean 类似但是扩展机制。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.scripting.support;",
    ),
    (
        "/**\n * {@link org.springframework.beans.factory.config.BeanPostProcessor} that\n"
        " * handles {@link org.springframework.scripting.ScriptFactory} definitions,\n"
        " * replacing each factory with the actual scripted Java object generated by it.\n",
        "/* ===== [OCA 中文解析] =====\n"
        "class ScriptFactoryPostProcessor — 意图说明\n\n"
        "在实例化前拦截 ScriptFactory：解析脚本源、确定接口，postProcessBeforeInstantiation "
        "返回脚本对象代理；Refreshable 脚本支持定时刷新。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 处理 {@link org.springframework.scripting.ScriptFactory} 定义的\n"
        " * {@link org.springframework.beans.factory.config.BeanPostProcessor}，\n"
        " * 将每个工厂替换为其生成的实际脚本 Java 对象。\n",
    ),
    (
        "\tpublic @Nullable Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 postProcessBeforeInstantiation — 意图与阅读要点\n\n"
        "\t若 Bean 类为 ScriptFactory：读取脚本源、编译/加载脚本类，"
        "用脚本对象替换容器中的 Bean；构造参数给 ScriptFactory，属性值给脚本实例。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tpublic @Nullable Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {",
    ),
]

# ---------------------------------------------------------------------------
# DataBinder.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["DataBinder.java"] = [
    (
        "package org.springframework.validation;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "Web 与通用场景的数据绑定核心：把请求参数/PropertyValues 写入目标对象（bind/construct），"
        "再经 Validator 校验并汇总到 BindingResult；allowedFields 等安全约束防 mass assignment。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.validation;",
    ),
    (
        "/**\n * Binder that allows applying property values to a target object via constructor\n"
        " * and setter injection, and also supports validation and binding result analysis.\n",
        "/* ===== [OCA 中文解析] =====\n"
        "class DataBinder — 意图说明\n\n"
        "绑定 + 校验门面：bind 走 setter/字段注入，construct 走构造器绑定（6.1+）；"
        "Errors 收集 FieldError/ObjectError，MessageCodesResolver 生成 i18n 消息码。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 数据绑定器：通过构造器与 setter 注入把属性值应用到目标对象，\n"
        " * 并支持校验与绑定结果分析。\n",
    ),
    (
        "\tpublic void bind(PropertyValues pvs) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 bind — 意图与阅读要点\n\n"
        "\t绑定主入口：过滤 allowed/disallowed 字段 → doBind 写入 PropertyAccessor → "
        "checkRequiredFields；类型转换经 TypeConverter/ConversionService，失败记入 BindingResult。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tpublic void bind(PropertyValues pvs) {",
    ),
    (
        "\tpublic void construct(ValueResolver valueResolver) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 construct — 意图与阅读要点\n\n"
        "\t构造器绑定：按构造参数顺序从 ValueResolver 取值，支持嵌套与 @Name 解析；"
        "实例化后可选 validateConstructorArgument 做参数级校验。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tpublic void construct(ValueResolver valueResolver) {",
    ),
    (
        "\tpublic void validate() {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 validate — 意图与阅读要点\n\n"
        "\t对当前 target 执行已注册 Validator，错误写入 BindingResult；"
        "Web 层通常在 bind 之后调用，@Valid 触发 Bean Validation 时也走类似路径。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tpublic void validate() {",
    ),
    (
        "\tprotected void doBind(MutablePropertyValues mpvs) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 doBind — 意图与阅读要点\n\n"
        "\t逐 PropertyValue 应用：嵌套路径 autoGrow、Editor/Formatter 转换、"
        "PropertyAccessException 经 BindingErrorProcessor 转为 FieldError。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tprotected void doBind(MutablePropertyValues mpvs) {",
    ),
]

# ---------------------------------------------------------------------------
# MethodValidationAdapter.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["MethodValidationAdapter.java"] = [
    (
        "package org.springframework.validation.beanvalidation;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "Spring 6.1+ 方法级 Bean Validation 适配器：对控制器/服务方法参数与返回值执行 "
        "jakarta.validation，把 ConstraintViolation 转为 MethodValidationResult。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.validation.beanvalidation;",
    ),
    (
        "/**\n * {@link MethodValidator} that uses a Bean Validation\n"
        " * {@link jakarta.validation.Validator} for validation, and adapts\n"
        " * {@link ConstraintViolation}s to {@link MethodValidationResult}.\n"
        " *\n"
        " * @author Rossen Stoyanchev\n"
        " * @since 6.1\n"
        " */\n"
        "public class MethodValidationAdapter",
        "/* ===== [OCA 中文解析] =====\n"
        "class MethodValidationAdapter — 意图说明\n\n"
        "ExecutableValidator 的 Spring 封装：validateArguments/validateReturnValue 驱动 "
        "JSR-380 方法约束，Violation 映射为 ParameterValidationResult 供 MVC/WebFlux 处理。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 使用 Bean Validation {@link jakarta.validation.Validator} 进行校验、\n"
        " * 并将 {@link ConstraintViolation} 适配为 {@link MethodValidationResult} 的\n"
        " * {@link MethodValidator} 实现。\n"
        " *\n"
        " * @author Rossen Stoyanchev\n"
        " * @since 6.1\n"
        " */\n"
        "public class MethodValidationAdapter",
    ),
    (
        "\tpublic final MethodValidationResult validateArguments(",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 validateArguments — 意图与阅读要点\n\n"
        "\t方法入参校验：解析 @Validated 分组，调用 ExecutableValidator.validateParameters，"
        "Violation 按参数索引分组为 ParameterValidationResult。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tpublic final MethodValidationResult validateArguments(",
    ),
    (
        "\tpublic final MethodValidationResult validateReturnValue(",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 validateReturnValue — 意图与阅读要点\n\n"
        "\t返回值校验：对 @Valid 等方法级约束调用 validateReturnValue；"
        "常用于 @Validated 服务层或控制器方法返回值约束。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tpublic final MethodValidationResult validateReturnValue(",
    ),
]

# ---------------------------------------------------------------------------
# SpringValidatorAdapter.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["SpringValidatorAdapter.java"] = [
    (
        "package org.springframework.validation.beanvalidation;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "JSR-303/380 Validator 与 Spring Validator 的双向适配器："
        "DataBinder/MVC 使用 Spring 接口，底层委托 jakarta.validation.Validator。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.validation.beanvalidation;",
    ),
    (
        "/**\n * Adapter that takes a JSR-303 {@code javax.validator.Validator} and\n"
        " * exposes it as a Spring {@link org.springframework.validation.Validator}\n"
        " * while also exposing the original JSR-303 Validator interface itself.\n"
        " *\n"
        " * <p>Can be used as a programmatic wrapper. Also serves as base class for\n"
        " * {@link CustomValidatorBean} and {@link LocalValidatorFactoryBean},\n"
        " * and as the primary implementation of the {@link SmartValidator} interface.\n"
        " *\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @since 3.0\n"
        " * @see SmartValidator\n"
        " * @see CustomValidatorBean\n"
        " * @see LocalValidatorFactoryBean\n"
        " */\n"
        "public class SpringValidatorAdapter",
        "/* ===== [OCA 中文解析] =====\n"
        "class SpringValidatorAdapter — 意图说明\n\n"
        "桥接两层校验 API：validate(Object, Errors) 走 Spring，ConstraintViolation 转为 "
        "FieldError/ObjectError；同时实现 jakarta.validation.Validator 供需要原生 API 的代码使用。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 将 JSR-303 {@code jakarta.validation.Validator} 适配为 Spring\n"
        " * {@link org.springframework.validation.Validator}，同时暴露原生 JSR-303 接口。\n"
        " *\n"
        " * <p>可作为编程式包装器；也是 {@link CustomValidatorBean}、\n"
        " * {@link LocalValidatorFactoryBean} 的基类，以及 {@link SmartValidator} 的主要实现。\n"
        " *\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @since 3.0\n"
        " * @see SmartValidator\n"
        " * @see CustomValidatorBean\n"
        " * @see LocalValidatorFactoryBean\n"
        " */\n"
        "public class SpringValidatorAdapter",
    ),
    (
        "\t@Override\n"
        "\tpublic void validate(Object target, Errors errors) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 validate — 意图与阅读要点\n\n"
        "\tSpring Validator 入口：调用 targetValidator.validate，"
        "processConstraintViolations 把每个 Violation 映射为 FieldError（含 message code 与参数）。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\t@Override\n"
        "\tpublic void validate(Object target, Errors errors) {",
    ),
    (
        "\t@Override\n"
        "\tpublic void validate(Object target, Errors errors, Object... validationHints) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 validate (带 hints) — 意图与阅读要点\n\n"
        "\tSmartValidator 扩展：hints 作为 Bean Validation 分组（Class<?>），"
        "支持 @Validated 的分组校验语义。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\t@Override\n"
        "\tpublic void validate(Object target, Errors errors, Object... validationHints) {",
    ),
]

# ---------------------------------------------------------------------------
# TransactionAspectSupport.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["TransactionAspectSupport.java"] = [
    (
        "package org.springframework.transaction.interceptor;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "事务切面核心支持类：决定创建/加入/挂起事务，并在成功提交或异常回滚。@Transactional 的真实引擎在这里。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.transaction.interceptor;",
    ),
    (
        "/**\n * Base class for transactional aspects, such as the {@link TransactionInterceptor}\n"
        " * or an AspectJ aspect.\n",
        "/* ===== [OCA 中文解析] =====\n"
        "class TransactionAspectSupport — 意图说明\n\n"
        "被 TransactionInterceptor / AspectJ 切面复用的事务边界控制逻辑："
        "解析 TransactionAttribute、选择 Platform/Reactive TransactionManager、"
        "维护 TransactionInfo 线程栈以支持嵌套与传播行为。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 事务切面基类，如 {@link TransactionInterceptor} 或 AspectJ 切面。\n",
    ),
    (
        "\tprotected @Nullable Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,\n"
        "\t\t\tfinal InvocationCallback invocation) throws Throwable {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 invokeWithinTransaction — 意图与阅读要点\n\n"
        "\t主路径：获取 TransactionAttribute → createTransactionIfNecessary → 调回调 → "
        "completeTransactionAfterThrowing 或 commitTransactionAfterReturning。"
        "重点看传播行为导致的「挂起当前事务」分支；Reactive/Kotlin 协程走 ReactiveTransactionSupport 旁路。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tprotected @Nullable Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,\n"
        "\t\t\tfinal InvocationCallback invocation) throws Throwable {",
    ),
    (
        "\tprotected TransactionInfo createTransactionIfNecessary(@Nullable PlatformTransactionManager tm,\n"
        "\t\t\t@Nullable TransactionAttribute txAttr, final String joinpointIdentification) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 createTransactionIfNecessary — 意图与阅读要点\n\n"
        "\t按 txAttr 调用 tm.getTransaction：REQUIRED 加入现有、REQUIRES_NEW 挂起并新建、"
        "NOT_SUPPORTED 挂起且不创建等；无名称时用 joinpointIdentification 作为事务名。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tprotected TransactionInfo createTransactionIfNecessary(@Nullable PlatformTransactionManager tm,\n"
        "\t\t\t@Nullable TransactionAttribute txAttr, final String joinpointIdentification) {",
    ),
    (
        "\tprotected void completeTransactionAfterThrowing(\n"
        "\t\t\t@Nullable TransactionInfo txInfo, InvocationCallback invocation, Throwable ex) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 completeTransactionAfterThrowing — 意图与阅读要点\n\n"
        "\t异常收尾：txAttr.rollbackOn(ex) 决定 rollback 还是 commit；"
        "rollback 失败时 TransactionSystemException 保留原始应用异常。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\tprotected void completeTransactionAfterThrowing(\n"
        "\t\t\t@Nullable TransactionInfo txInfo, InvocationCallback invocation, Throwable ex) {",
    ),
    (
        "\t/**\n"
        "\t * Opaque object used to hold transaction information. Subclasses\n"
        "\t * must pass it back to methods on this class, but not see its internals.\n"
        "\t */",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\tclass TransactionInfo — 意图说明\n\n"
        "\t当前连接点的事务上下文：持有 TransactionManager、TransactionAttribute、TransactionStatus；"
        "bind/cleanup 维护 ThreadLocal 栈以支持嵌套调用。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\t/**\n"
        "\t * 保存事务信息的不透明对象。子类须将其传回本类方法，但不应访问内部细节。\n"
        "\t */",
    ),
]

# ---------------------------------------------------------------------------
# JtaTransactionManager.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["JtaTransactionManager.java"] = [
    (
        "package org.springframework.transaction.jta;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "JTA 分布式事务 PlatformTransactionManager：委托 UserTransaction/TransactionManager "
        "处理跨资源事务；适合 EE 容器或嵌入式 JTA 提供者。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.transaction.jta;",
    ),
    (
        "/**\n * {@link org.springframework.transaction.PlatformTransactionManager} implementation\n"
        " * for JTA, delegating to a backend JTA provider. This is typically used to delegate\n"
        " * to a Jakarta EE server's transaction coordinator, but may also be configured with a\n"
        " * local JTA provider which is embedded within the application.\n",
        "/* ===== [OCA 中文解析] =====\n"
        "class JtaTransactionManager — 意图说明\n\n"
        "JTA 与 Spring 事务抽象的桥接：doBegin/doCommit/doRollback 映射到 UserTransaction；"
        "REQUIRES_NEW 等需 TransactionManager.suspend/resume；自动探测常见 JNDI 位置。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 面向 JTA 的 {@link org.springframework.transaction.PlatformTransactionManager} 实现，\n"
        " * 委托后端 JTA 提供者。通常用于 Jakarta EE 服务器事务协调器，\n"
        " * 也可配置应用内嵌的本地 JTA 提供者。\n",
    ),
    (
        "\t@Override\n"
        "\tprotected void doBegin(Object transaction, TransactionDefinition definition) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 doBegin — 意图与阅读要点\n\n"
        "\t开启 JTA 事务：设置 timeout/isolation（若支持），UserTransaction.begin；"
        "已有全局事务时按传播行为 join 或 suspend。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\t@Override\n"
        "\tprotected void doBegin(Object transaction, TransactionDefinition definition) {",
    ),
    (
        "\t@Override\n"
        "\tprotected void doCommit(DefaultTransactionStatus status) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 doCommit — 意图与阅读要点\n\n"
        "\t提交 JTA 事务：UserTransaction.commit；rollback-only 时改调 rollback；"
        "HeuristicMixed/HeuristicRollback 转为 UnexpectedRollbackException。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\t@Override\n"
        "\tprotected void doCommit(DefaultTransactionStatus status) {",
    ),
    (
        "\t@Override\n"
        "\tprotected void doRollback(DefaultTransactionStatus status) {",
        "\t/* ===== [OCA 中文解析] =====\n"
        "\t方法 doRollback — 意图与阅读要点\n\n"
        "\t回滚 JTA 事务：UserTransaction.rollback；无活动事务时吞掉 IllegalStateException（已完成场景）。\n"
        "\t===== [OCA 中文解析结束] ===== */\n"
        "\t@Override\n"
        "\tprotected void doRollback(DefaultTransactionStatus status) {",
    ),
]
