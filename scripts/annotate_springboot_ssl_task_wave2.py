#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 ssl/task batch [20:40] wave2."""
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
    "BundleContentNotWatchableException.java": [
        (
            "/**\n * Thrown when a bundle content location is not watchable.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * 当 SSL bundle 内容位置不可监视时抛出。\n * <p>\n * 仅 {@code file:} 协议资源可被 {@link FileWatcher} 监视；\n * 使用 classpath、PEM 内联内容等不可监视配置时会触发此异常。\n *\n * @author Moritz Halbritter\n */",
        ),
    ],
    "BundleContentNotWatchableFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractFailureAnalyzer} that performs analysis of non-watchable bundle\n * content failures caused by {@link BundleContentNotWatchableException}.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * 分析由 {@link BundleContentNotWatchableException} 引起的\n * bundle 内容不可监视失败的 {@link AbstractFailureAnalyzer}。\n * <p>\n * 提示用户使用可监视资源，或在 bundle 上设置 {@code reload-on-update = false} 禁用热重载。\n *\n * @author Moritz Halbritter\n */",
        ),
    ],
    "BundleContentProperty.java": [
        (
            "/**\n * Helper utility to manage a single bundle content configuration property. May possibly\n * contain PEM content, a location or a directory search pattern.\n *\n * @param name the configuration property name (excluding any prefix)\n * @param value the configuration property value\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
            "/**\n * 管理单个 SSL bundle 内容配置属性的辅助工具。\n * <p>\n * 属性值可能是 PEM 内联内容、资源位置或目录搜索模式。\n *\n * @param name 配置属性名（不含前缀）\n * @param value 配置属性值\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
        ),
        (
            "/**\n\t * Return if the property value is PEM content.\n\t * @return if the value is PEM content\n\t */",
            "/**\n\t * 判断属性值是否为 PEM 内联内容。\n\t * @return 值为 PEM 内容时返回 {@code true}\n\t */",
        ),
        (
            "/**\n\t * Return if there is any property value present.\n\t * @return if the value is present\n\t */",
            "/**\n\t * 判断属性值是否存在且非空。\n\t * @return 存在有效值时返回 {@code true}\n\t */",
        ),
    ],
    "CertificateMatcher.java": [
        (
            "/**\n * Helper used to match certificates against a {@link PrivateKey}.\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
            "/**\n * 将证书与 {@link PrivateKey} 进行匹配的辅助类。\n * <p>\n * 使用私钥对固定数据签名，再用证书公钥验证签名，\n * 以确认 PEM 配置中私钥与证书链是否对应。\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
        ),
    ],
    "FileWatcher.java": [
        (
            "/**\n * Watches files and directories and triggers a callback on change.\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
            "/**\n * 监视文件与目录变更并在检测到变化后触发回调。\n * <p>\n * 基于 {@link WatchService} 实现，支持静默期（quiet period）防抖，\n * 用于 SSL bundle 证书/密钥文件的热重载。\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
        ),
        (
            "/**\n\t * Create a new {@link FileWatcher} instance.\n\t * @param quietPeriod the duration that no file changes should occur before triggering\n\t * actions\n\t */",
            "/**\n\t * 创建新的 {@link FileWatcher} 实例。\n\t * @param quietPeriod 触发动作前需无文件变更的静默时长\n\t */",
        ),
        (
            "/**\n\t * Watch the given files or directories for changes.\n\t * @param paths the files or directories to watch\n\t * @param action the action to take when changes are detected\n\t */",
            "/**\n\t * 监视给定文件或目录的变更。\n\t * @param paths 要监视的文件或目录\n\t * @param action 检测到变更时执行的动作\n\t */",
        ),
        (
            "/**\n\t * Retrieves all {@link Path Paths} that should be registered for the specified\n\t * {@link Path}. If the path is a symlink, changes to the symlink should be monitored,\n\t * not just the file it points to. For example, for the given {@code keystore.jks}\n\t * path in the following directory structure:<pre>\n\t * +- stores\n\t * |  +─ keystore.jks\n\t * +- <em>data</em> -&gt; stores\n\t * +─ <em>keystore.jks</em> -&gt; data/keystore.jks\n\t * </pre> the resulting paths would include:\n\t * <p>\n\t * <ul>\n\t * <li>{@code keystore.jks}</li>\n\t * <li>{@code data/keystore.jks}</li>\n\t * <li>{@code data}</li>\n\t * <li>{@code stores/keystore.jks}</li>\n\t * </ul>\n\t * @param paths the source paths\n\t * @return all possible {@link Path} instances to be registered\n\t * @throws IOException if an I/O error occurs\n\t */",
            "/**\n\t * 获取指定 {@link Path} 应注册监视的全部 {@link Path 路径}。\n\t * <p>\n\t * 若路径为符号链接，需监视链接本身及其解析链路上的相关路径，\n\t * 而不仅是链接最终指向的文件。例如下列目录结构中 {@code keystore.jks}：<pre>\n\t * +- stores\n\t * |  +─ keystore.jks\n\t * +- <em>data</em> -&gt; stores\n\t * +─ <em>keystore.jks</em> -&gt; data/keystore.jks\n\t * </pre> 结果路径包括：\n\t * <p>\n\t * <ul>\n\t * <li>{@code keystore.jks}</li>\n\t * <li>{@code data/keystore.jks}</li>\n\t * <li>{@code data}</li>\n\t * <li>{@code stores/keystore.jks}</li>\n\t * </ul>\n\t * @param paths 源路径集合\n\t * @return 应注册的全部 {@link Path} 实例\n\t * @throws IOException I/O 错误时抛出\n\t */",
        ),
        (
            "/**\n\t * The watcher thread used to check for changes.\n\t */",
            "/**\n\t * 用于检测文件变更的监视线程。\n\t */",
        ),
        (
            "/**\n\t * An individual watch registration.\n\t *\n\t * @param paths the paths being registered\n\t * @param action the action to take\n\t */",
            "/**\n\t * 单次监视注册。\n\t *\n\t * @param paths 已注册的路径\n\t * @param action 变更时执行的动作\n\t */",
        ),
    ],
    "JksSslBundleProperties.java": [
        (
            "/**\n * {@link SslBundleProperties} for Java keystores.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.1.0\n * @see JksSslStoreBundle\n */",
            "/**\n * Java 密钥库（JKS 等）格式的 {@link SslBundleProperties}。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.1.0\n * @see JksSslStoreBundle\n */",
        ),
        (
            "/**\n\t * Keystore properties.\n\t */",
            "/**\n\t * 密钥库（keystore）属性。\n\t */",
        ),
        (
            "/**\n\t * Truststore properties.\n\t */",
            "/**\n\t * 信任库（truststore）属性。\n\t */",
        ),
        (
            "/**\n\t * Store properties.\n\t */",
            "/**\n\t * 存储（store）属性。\n\t */",
        ),
        (
            "/**\n\t\t * Type of the store to create, e.g. JKS.\n\t\t */",
            "/**\n\t\t * 要创建的存储类型，例如 JKS。\n\t\t */",
        ),
        (
            "/**\n\t\t * Provider for the store.\n\t\t */",
            "/**\n\t\t * 存储的 Security Provider。\n\t\t */",
        ),
        (
            "/**\n\t\t * Location of the resource containing the store content.\n\t\t */",
            "/**\n\t\t * 包含存储内容的资源位置。\n\t\t */",
        ),
        (
            "/**\n\t\t * Password used to access the store.\n\t\t */",
            "/**\n\t\t * 访问存储所用的密码。\n\t\t */",
        ),
    ],
    "PemSslBundleProperties.java": [
        (
            "/**\n * {@link SslBundleProperties} for PEM-encoded certificates and private keys.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 3.1.0\n * @see PemSslStoreBundle\n */",
            "/**\n * PEM 编码证书与私钥的 {@link SslBundleProperties}。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 3.1.0\n * @see PemSslStoreBundle\n */",
        ),
        (
            "/**\n\t * Keystore properties.\n\t */",
            "/**\n\t * 密钥库（keystore）属性。\n\t */",
        ),
        (
            "/**\n\t * Truststore properties.\n\t */",
            "/**\n\t * 信任库（truststore）属性。\n\t */",
        ),
        (
            "/**\n\t * Store properties.\n\t */",
            "/**\n\t * 存储（store）属性。\n\t */",
        ),
        (
            "/**\n\t\t * Type of the store to create, e.g. JKS.\n\t\t */",
            "/**\n\t\t * 要创建的存储类型，例如 JKS。\n\t\t */",
        ),
        (
            "/**\n\t\t * Location or content of the certificate or certificate chain in PEM format.\n\t\t */",
            "/**\n\t\t * PEM 格式证书或证书链的位置或内联内容。\n\t\t */",
        ),
        (
            "/**\n\t\t * Location or content of the private key in PEM format.\n\t\t */",
            "/**\n\t\t * PEM 格式私钥的位置或内联内容。\n\t\t */",
        ),
        (
            "/**\n\t\t * Password used to decrypt an encrypted private key.\n\t\t */",
            "/**\n\t\t * 解密加密私钥所用的密码。\n\t\t */",
        ),
        (
            "/**\n\t\t * Whether to verify that the private key matches the public key.\n\t\t */",
            "/**\n\t\t * 是否验证私钥与公钥是否匹配。\n\t\t */",
        ),
    ],
    "PropertiesSslBundle.java": [
        (
            "/**\n * {@link SslBundle} backed by {@link JksSslBundleProperties} or\n * {@link PemSslBundleProperties}.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.1.0\n */",
            "/**\n * 由 {@link JksSslBundleProperties} 或 {@link PemSslBundleProperties}\n * 配置属性支撑的 {@link SslBundle} 实现。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.1.0\n */",
        ),
        (
            "/**\n\t * Get an {@link SslBundle} for the given {@link PemSslBundleProperties}.\n\t * @param properties the source properties\n\t * @return an {@link SslBundle} instance\n\t */",
            "/**\n\t * 根据给定 {@link PemSslBundleProperties} 获取 {@link SslBundle}。\n\t * @param properties 源配置属性\n\t * @return {@link SslBundle} 实例\n\t */",
        ),
        (
            "/**\n\t * Get an {@link SslBundle} for the given {@link PemSslBundleProperties}.\n\t * @param properties the source properties\n\t * @param resourceLoader the resource loader used to load content\n\t * @return an {@link SslBundle} instance\n\t * @since 3.3.5\n\t */",
            "/**\n\t * 根据给定 {@link PemSslBundleProperties} 获取 {@link SslBundle}。\n\t * @param properties 源配置属性\n\t * @param resourceLoader 用于加载内容的资源加载器\n\t * @return {@link SslBundle} 实例\n\t * @since 3.3.5\n\t */",
        ),
        (
            "/**\n\t * Get an {@link SslBundle} for the given {@link JksSslBundleProperties}.\n\t * @param properties the source properties\n\t * @return an {@link SslBundle} instance\n\t */",
            "/**\n\t * 根据给定 {@link JksSslBundleProperties} 获取 {@link SslBundle}。\n\t * @param properties 源配置属性\n\t * @return {@link SslBundle} 实例\n\t */",
        ),
        (
            "/**\n\t * Get an {@link SslBundle} for the given {@link JksSslBundleProperties}.\n\t * @param properties the source properties\n\t * @param resourceLoader the resource loader used to load content\n\t * @return an {@link SslBundle} instance\n\t * @since 3.3.5\n\t */",
            "/**\n\t * 根据给定 {@link JksSslBundleProperties} 获取 {@link SslBundle}。\n\t * @param properties 源配置属性\n\t * @param resourceLoader 用于加载内容的资源加载器\n\t * @return {@link SslBundle} 实例\n\t * @since 3.3.5\n\t */",
        ),
    ],
    "SslAutoConfiguration.java": [
        (
            "/**\n * {@link EnableAutoConfiguration Auto-configuration} for SSL.\n *\n * @author Scott Frederick\n * @since 3.1.0\n */",
            "/**\n * SSL 的 {@link EnableAutoConfiguration 自动配置}。\n * <p>\n * 注册 {@link FileWatcher}、{@link SslPropertiesBundleRegistrar}\n * 以及默认 {@link DefaultSslBundleRegistry}，\n * 将 {@code spring.ssl.bundle.*} 配置映射为 {@link SslBundle}。\n *\n * @author Scott Frederick\n * @since 3.1.0\n */",
        ),
    ],
    "SslBundleProperties.java": [
        (
            "/**\n * Base class for SSL Bundle properties.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.1.0\n * @see SslBundle\n */",
            "/**\n * SSL Bundle 配置属性的基类。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.1.0\n * @see SslBundle\n */",
        ),
        (
            "/**\n\t * Key details for the bundle.\n\t */",
            "/**\n\t * Bundle 的密钥详情。\n\t */",
        ),
        (
            "/**\n\t * Options for the SSL connection.\n\t */",
            "/**\n\t * SSL 连接选项。\n\t */",
        ),
        (
            "/**\n\t * SSL Protocol to use.\n\t */",
            "/**\n\t * 使用的 SSL 协议。\n\t */",
        ),
        (
            "/**\n\t * Whether to reload the SSL bundle.\n\t */",
            "/**\n\t * 是否在文件更新时重新加载 SSL bundle。\n\t */",
        ),
        (
            "/**\n\t\t * Supported SSL ciphers.\n\t\t */",
            "/**\n\t\t * 支持的 SSL 密码套件。\n\t\t */",
        ),
        (
            "/**\n\t\t * Enabled SSL protocols.\n\t\t */",
            "/**\n\t\t * 启用的 SSL 协议。\n\t\t */",
        ),
        (
            "/**\n\t\t * The password used to access the key in the key store.\n\t\t */",
            "/**\n\t\t * 访问密钥库中密钥所用的密码。\n\t\t */",
        ),
        (
            "/**\n\t\t * The alias that identifies the key in the key store.\n\t\t */",
            "/**\n\t\t * 标识密钥库中密钥的别名。\n\t\t */",
        ),
    ],
    "SslBundleRegistrar.java": [
        (
            "/**\n * Interface to be implemented by types that register {@link SslBundle} instances with an\n * {@link SslBundleRegistry}.\n *\n * @author Scott Frederick\n * @since 3.1.0\n */",
            "/**\n * 向 {@link SslBundleRegistry} 注册 {@link SslBundle} 实例的类型应实现的接口。\n *\n * @author Scott Frederick\n * @since 3.1.0\n */",
        ),
        (
            "/**\n\t * Callback method for registering {@link SslBundle}s with an\n\t * {@link SslBundleRegistry}.\n\t * @param registry the registry that accepts {@code SslBundle}s\n\t */",
            "/**\n\t * 向 {@link SslBundleRegistry} 注册 {@link SslBundle} 的回调方法。\n\t * @param registry 接受 {@code SslBundle} 的注册表\n\t */",
        ),
    ],
    "SslProperties.java": [
        (
            "/**\n * Properties for centralized SSL trust material configuration.\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @since 3.1.0\n */",
            "/**\n * 集中式 SSL 信任材料配置属性。\n * <p>\n * 绑定 {@code spring.ssl.bundle.*} 前缀，支持 PEM 与 JKS 两种 bundle 定义及文件监视配置。\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @since 3.1.0\n */",
        ),
        (
            "/**\n\t * SSL bundles.\n\t */",
            "/**\n\t * SSL bundle 集合。\n\t */",
        ),
        (
            "/**\n\t * Properties to define SSL Bundles.\n\t */",
            "/**\n\t * 定义 SSL Bundle 的属性。\n\t */",
        ),
        (
            "/**\n\t\t * PEM-encoded SSL trust material.\n\t\t */",
            "/**\n\t\t * PEM 编码的 SSL 信任材料。\n\t\t */",
        ),
        (
            "/**\n\t\t * Java keystore SSL trust material.\n\t\t */",
            "/**\n\t\t * Java 密钥库格式的 SSL 信任材料。\n\t\t */",
        ),
        (
            "/**\n\t\t * Trust material watching.\n\t\t */",
            "/**\n\t\t * 信任材料文件监视配置。\n\t\t */",
        ),
        (
            "/**\n\t\t\t * File watching.\n\t\t\t */",
            "/**\n\t\t\t * 文件监视配置。\n\t\t\t */",
        ),
        (
            "/**\n\t\t\t\t * Quiet period, after which changes are detected.\n\t\t\t\t */",
            "/**\n\t\t\t\t * 静默期，在此时间内无变更后才视为检测到变化。\n\t\t\t\t */",
        ),
    ],
    "SslPropertiesBundleRegistrar.java": [
        (
            "/**\n * A {@link SslBundleRegistrar} that registers SSL bundles based\n * {@link SslProperties#getBundle() configuration properties}.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
            "/**\n * 基于 {@link SslProperties#getBundle() 配置属性} 注册 SSL bundle 的\n * {@link SslBundleRegistrar} 实现。\n * <p>\n * 支持 {@code reload-on-update} 热重载：通过 {@link FileWatcher} 监视\n * keystore/truststore 或 PEM 文件变更并更新注册表。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
        ),
    ],
    "DefaultTaskSchedulerConfiguration.java": [
        (
            "/**\n * Configuration that can be imported to expose a standard {@link TaskScheduler} if the\n * user has not enabled task scheduling explicitly. A {@link SimpleAsyncTaskScheduler} is\n * exposed if the user enables virtual threads via\n * {@code spring.threads.virtual.enabled=true}, otherwise {@link ThreadPoolTaskScheduler}.\n * <p>\n * Configurations importing this one should be ordered after\n * {@link TaskSchedulingAutoConfiguration}.\n *\n * @author Phillip Webb\n * @since 4.1.0\n */",
            "/**\n * 在用户未显式启用任务调度时，可导入以暴露标准 {@link TaskScheduler} 的配置。\n * <p>\n * 若通过 {@code spring.threads.virtual.enabled=true} 启用虚拟线程，\n * 则暴露 {@link SimpleAsyncTaskScheduler}；否则暴露 {@link ThreadPoolTaskScheduler}。\n * <p>\n * 导入此配置的类应排在 {@link TaskSchedulingAutoConfiguration} 之后。\n *\n * @author Phillip Webb\n * @since 4.1.0\n */",
        ),
        (
            "/**\n\t * The bean name of the default task scheduler.\n\t */",
            "/**\n\t * 默认任务调度器的 Bean 名称。\n\t */",
        ),
    ],
    "ScheduledBeanLazyInitializationExcludeFilter.java": [
        (
            "/**\n * A {@link LazyInitializationExcludeFilter} that detects bean methods annotated with\n * {@link Scheduled} or {@link Schedules}.\n *\n * @author Stephane Nicoll\n */",
            "/**\n * 检测标注 {@link Scheduled} 或 {@link Schedules} 的 Bean 方法的\n * {@link LazyInitializationExcludeFilter}。\n * <p>\n * 在启用懒初始化时，含 {@code @Scheduled} 方法的 Bean 需立即初始化，\n * 以便调度注解处理器能正确注册定时任务。\n *\n * @author Stephane Nicoll\n */",
        ),
    ],
    "TaskExecutionAutoConfiguration.java": [
        (
            "/**\n * {@link EnableAutoConfiguration Auto-configuration} for {@link TaskExecutor}.\n *\n * @author Stephane Nicoll\n * @author Camille Vienot\n * @author Moritz Halbritter\n * @since 2.1.0\n */",
            "/**\n * {@link TaskExecutor} 的 {@link EnableAutoConfiguration 自动配置}。\n * <p>\n * 导入 {@link TaskExecutorConfigurations} 各子配置，\n * 注册应用级 {@code applicationTaskExecutor} 及异步执行相关 Bean。\n *\n * @author Stephane Nicoll\n * @author Camille Vienot\n * @author Moritz Halbritter\n * @since 2.1.0\n */",
        ),
        (
            "/**\n\t * Bean name of the application {@link TaskExecutor}.\n\t */",
            "/**\n\t * 应用级 {@link TaskExecutor} 的 Bean 名称。\n\t */",
        ),
    ],
    "TaskExecutionProperties.java": [
        (
            "/**\n * Configuration properties for task execution.\n *\n * @author Stephane Nicoll\n * @author Filip Hrisafov\n * @author Yanming Zhou\n * @since 2.1.0\n */",
            "/**\n * 任务执行（Task Execution）的配置属性。\n * <p>\n * 绑定 {@code spring.task.execution.*}，控制线程池、简单异步执行器及关闭行为。\n *\n * @author Stephane Nicoll\n * @author Filip Hrisafov\n * @author Yanming Zhou\n * @since 2.1.0\n */",
        ),
        (
            "/**\n\t * Determine when the task executor is to be created.\n\t */",
            "/**\n\t * 决定何时创建任务执行器。\n\t */",
        ),
        (
            "/**\n\t * Whether to propagate the current context to task executions.\n\t */",
            "/**\n\t * 是否将当前上下文传播到任务执行中。\n\t */",
        ),
        (
            "/**\n\t * Prefix to use for the names of newly created threads.\n\t */",
            "/**\n\t * 新创建线程名称的前缀。\n\t */",
        ),
        (
            "/**\n\t\t * Whether to cancel remaining tasks on close. Only recommended if threads are\n\t\t * commonly expected to be stuck.\n\t\t */",
            "/**\n\t\t * 关闭时是否取消剩余任务。仅在线程可能长期阻塞时建议使用。\n\t\t */",
        ),
        (
            "/**\n\t\t * Whether to reject tasks when the concurrency limit has been reached.\n\t\t */",
            "/**\n\t\t * 达到并发上限时是否拒绝新任务。\n\t\t */",
        ),
        (
            "/**\n\t\t * Set the maximum number of parallel accesses allowed. -1 indicates no\n\t\t * concurrency limit at all.\n\t\t */",
            "/**\n\t\t * 允许的最大并行访问数；{@code -1} 表示无并发限制。\n\t\t */",
        ),
        (
            "/**\n\t\t * Queue capacity. An unbounded capacity does not increase the pool and therefore\n\t\t * ignores the \"max-size\" property. Doesn't have an effect if virtual threads are\n\t\t * enabled.\n\t\t */",
            "/**\n\t\t * 队列容量。无界队列不会扩展线程池，因此忽略 {@code max-size}。\n\t\t * 启用虚拟线程时无效。\n\t\t */",
        ),
        (
            "/**\n\t\t * Core number of threads. Doesn't have an effect if virtual threads are enabled.\n\t\t */",
            "/**\n\t\t * 核心线程数。启用虚拟线程时无效。\n\t\t */",
        ),
        (
            "/**\n\t\t * Maximum allowed number of threads. If tasks are filling up the queue, the pool\n\t\t * can expand up to that size to accommodate the load. Ignored if the queue is\n\t\t * unbounded. Doesn't have an effect if virtual threads are enabled.\n\t\t */",
            "/**\n\t\t * 允许的最大线程数。队列积压时线程池可扩展至此规模。\n\t\t * 无界队列时忽略；启用虚拟线程时无效。\n\t\t */",
        ),
        (
            "/**\n\t\t * Whether core threads are allowed to time out. This enables dynamic growing and\n\t\t * shrinking of the pool. Doesn't have an effect if virtual threads are enabled.\n\t\t */",
            "/**\n\t\t * 是否允许核心线程超时退出，以实现线程池动态伸缩。\n\t\t * 启用虚拟线程时无效。\n\t\t */",
        ),
        (
            "/**\n\t\t * Time limit for which threads may remain idle before being terminated. Doesn't\n\t\t * have an effect if virtual threads are enabled.\n\t\t */",
            "/**\n\t\t * 线程空闲后被终止前的最长等待时间。\n\t\t * 启用虚拟线程时无效。\n\t\t */",
        ),
        (
            "/**\n\t\t\t * Whether to accept further tasks after the application context close phase\n\t\t\t * has begun.\n\t\t\t */",
            "/**\n\t\t\t * 应用上下文关闭阶段开始后是否仍接受新任务。\n\t\t\t */",
        ),
        (
            "/**\n\t\t * Whether the executor should wait for scheduled tasks to complete on shutdown.\n\t\t */",
            "/**\n\t\t * 关闭时执行器是否等待已调度任务完成。\n\t\t */",
        ),
        (
            "/**\n\t\t * Maximum time the executor should wait for remaining tasks to complete.\n\t\t */",
            "/**\n\t\t * 执行器等待剩余任务完成的最长时间。\n\t\t */",
        ),
        (
            "/**\n\t * Determine when the task executor is to be created.\n\t *\n\t * @since 3.5.0\n\t */",
            "/**\n\t * 决定何时创建任务执行器。\n\t *\n\t * @since 3.5.0\n\t */",
        ),
        (
            "/**\n\t\t * Create the task executor if no user-defined executor is present.\n\t\t */",
            "/**\n\t\t * 若不存在用户自定义执行器则创建任务执行器。\n\t\t */",
        ),
        (
            "/**\n\t\t * Create the task executor even if a user-defined executor is present.\n\t\t */",
            "/**\n\t\t * 即使存在用户自定义执行器也创建任务执行器。\n\t\t */",
        ),
    ],
    "TaskExecutorConfigurations.java": [
        (
            "/**\n * {@link TaskExecutor} configurations to be imported by\n * {@link TaskExecutionAutoConfiguration} in a specific order.\n *\n * @author Andy Wilkinson\n * @author Moritz Halbritter\n * @author Yanming Zhou\n */",
            "/**\n * 由 {@link TaskExecutionAutoConfiguration} 按特定顺序导入的\n * {@link TaskExecutor} 配置集合。\n * <p>\n * 包含 Builder、应用级执行器、上下文传播、{@link AsyncConfigurer} 包装及\n * bootstrap 执行器别名等子配置。\n *\n * @author Andy Wilkinson\n * @author Moritz Halbritter\n * @author Yanming Zhou\n */",
        ),
        (
            "/**\n\t * {@link AsyncConfigurer} implementation that delegates to the user-defined\n\t * {@link AsyncConfigurer} instance, if any. Consistently use the executor named\n\t * {@value TaskExecutionAutoConfiguration#APPLICATION_TASK_EXECUTOR_BEAN_NAME} in the\n\t * absence of a custom executor.\n\t */",
            "/**\n\t * 委托给用户自定义 {@link AsyncConfigurer}（若存在）的 {@link AsyncConfigurer} 实现。\n\t * 无自定义执行器时，统一使用名为\n\t * {@value TaskExecutionAutoConfiguration#APPLICATION_TASK_EXECUTOR_BEAN_NAME} 的执行器。\n\t */",
        ),
    ],
    "TaskSchedulingAutoConfiguration.java": [
        (
            "/**\n * {@link EnableAutoConfiguration Auto-configuration} for {@link TaskScheduler}.\n *\n * @author Stephane Nicoll\n * @author Moritz Halbritter\n * @since 2.1.0\n */",
            "/**\n * {@link TaskScheduler} 的 {@link EnableAutoConfiguration 自动配置}。\n * <p>\n * 导入 {@link TaskSchedulingConfigurations} 并注册\n * {@link ScheduledBeanLazyInitializationExcludeFilter}，\n * 在启用 {@code @Scheduled} 且缺少用户自定义调度器时提供默认 {@link TaskScheduler}。\n *\n * @author Stephane Nicoll\n * @author Moritz Halbritter\n * @since 2.1.0\n */",
        ),
    ],
    "TaskSchedulingConfigurations.java": [
        (
            "/**\n * {@link TaskScheduler} configurations to be imported by\n * {@link TaskSchedulingAutoConfiguration} in a specific order.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * 由 {@link TaskSchedulingAutoConfiguration} 按特定顺序导入的\n * {@link TaskScheduler} 配置集合。\n * <p>\n * 包含 {@link ThreadPoolTaskSchedulerBuilder}、\n * {@link SimpleAsyncTaskSchedulerBuilder} 及默认调度器导入配置。\n *\n * @author Moritz Halbritter\n */",
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
