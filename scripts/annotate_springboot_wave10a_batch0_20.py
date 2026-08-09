#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-10a slice [0:20] (diagnostics.analyzer + env)."""
from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = Path("/tmp/springboot_w10a.txt").read_text(encoding="utf-8").strip().splitlines()

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "BindValidationFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractFailureAnalyzer} that performs analysis of any bind validation\n * failures caused by {@link BindValidationException} or\n * {@link org.springframework.validation.BindException}.\n *\n * @author Madhura Bhave\n */",
            "/**\n * 分析由 {@link BindValidationException} 或\n * {@link org.springframework.validation.BindException} 引起的绑定校验失败的 {@link AbstractFailureAnalyzer}。\n * 汇总字段级错误、拒绝值及配置来源。\n *\n * @author Madhura Bhave\n */",
        ),
    ],
    "InvalidConfigurationPropertyNameFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractFailureAnalyzer} that performs analysis of failures caused by\n * {@link InvalidConfigurationPropertyNameException}.\n *\n * @author Madhura Bhave\n */",
            "/**\n * 分析由 {@link InvalidConfigurationPropertyNameException} 引起失败的 {@link AbstractFailureAnalyzer}。\n * 帮助识别配置属性名中的非法字符并给出规范命名建议。\n *\n * @author Madhura Bhave\n */",
        ),
    ],
    "InvalidConfigurationPropertyValueFailureAnalyzer.java": [
        (
            "/**\n * A {@link FailureAnalyzer} that performs analysis of failures caused by an\n * {@link InvalidConfigurationPropertyValueException}.\n *\n * @author Stephane Nicoll\n * @author Scott Frederick\n */",
            "/**\n * 分析由 {@link InvalidConfigurationPropertyValueException} 引起失败的 {@link FailureAnalyzer}。\n * 会列出无效值、来源位置及冲突的多处配置。\n *\n * @author Stephane Nicoll\n * @author Scott Frederick\n */",
        ),
    ],
    "MissingParameterNamesFailureAnalyzer.java": [
        (
            "/**\n * {@link FailureAnalyzer} for exceptions caused by missing parameter names. This analyzer\n * is ordered last, if other analyzers wish to also report parameter actions they can use\n * the {@link #analyzeForMissingParameters(Throwable)} static method.\n *\n * @author Phillip Webb\n */",
            "/**\n * 针对缺少参数名异常的 {@link FailureAnalyzer}。\n * 此分析器排序最后；其他分析器若也要报告参数相关问题，可使用\n * {@link #analyzeForMissingParameters(Throwable)} 静态方法。\n *\n * @author Phillip Webb\n */",
        ),
        (
            "\t/**\n\t * Analyze the given failure for missing parameter name exceptions.\n\t * @param failure the failure to analyze\n\t * @return a failure analysis or {@code null}\n\t */",
            "\t/**\n\t * 分析给定失败是否由缺少参数名异常引起。\n\t *\n\t * @param failure 要分析的失败\n\t * @return a failure analysis or {@code null} 失败分析或 {@code null}\n\t */",
        ),
    ],
    "MutuallyExclusiveConfigurationPropertiesFailureAnalyzer.java": [
        (
            "/**\n * A {@link FailureAnalyzer} that performs analysis of failures caused by a\n * {@link MutuallyExclusiveConfigurationPropertiesException}.\n *\n * @author Andy Wilkinson\n * @author Scott Frederick\n */",
            "/**\n * 分析由 {@link MutuallyExclusiveConfigurationPropertiesException} 引起失败的 {@link FailureAnalyzer}。\n * 指出互斥配置属性同时被设置的位置与来源。\n *\n * @author Andy Wilkinson\n * @author Scott Frederick\n */",
        ),
    ],
    "NoSuchMethodFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractFailureAnalyzer} that analyzes {@link NoSuchMethodError\n * NoSuchMethodErrors}.\n *\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @author Scott Frederick\n */",
            "/**\n * 分析 {@link NoSuchMethodError NoSuchMethodErrors} 的 {@link AbstractFailureAnalyzer}。\n * 诊断调用方与被调用方法所在类路径及版本冲突。\n *\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @author Scott Frederick\n */",
        ),
    ],
    "NoUniqueBeanDefinitionFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractInjectionFailureAnalyzer} that performs analysis of failures caused\n * by a {@link NoUniqueBeanDefinitionException}.\n *\n * @author Andy Wilkinson\n * @author Scott Frederick\n */",
            "/**\n * 分析由 {@link NoUniqueBeanDefinitionException} 引起失败的 {@link AbstractInjectionFailureAnalyzer}。\n * 列出候选 Bean 定义并提示使用 {@code @Primary} 或 {@code @Qualifier}。\n *\n * @author Andy Wilkinson\n * @author Scott Frederick\n */",
        ),
    ],
    "PatternParseFailureAnalyzer.java": [
        (
            "/**\n * A {@code FailureAnalyzer} that performs analysis of failures caused by a\n * {@code PatternParseException}.\n *\n * @author Brian Clozel\n */",
            "/**\n * 分析由 {@code PatternParseException} 引起失败的 {@code FailureAnalyzer}。\n * 针对 Spring MVC 路径模式解析错误给出修复建议。\n *\n * @author Brian Clozel\n */",
        ),
    ],
    "UnboundConfigurationPropertyFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractFailureAnalyzer} that performs analysis of failures caused by any\n * {@link UnboundConfigurationPropertiesException}.\n *\n * @author Madhura Bhave\n */",
            "/**\n * 分析由 {@link UnboundConfigurationPropertiesException} 引起失败的 {@link AbstractFailureAnalyzer}。\n * 报告无法绑定到目标对象的未知配置属性。\n *\n * @author Madhura Bhave\n */",
        ),
    ],
    "ValidationExceptionFailureAnalyzer.java": [
        (
            "/**\n * A {@link FailureAnalyzer} that performs analysis of failures caused by a\n * {@link ValidationException}.\n *\n * @author Andy Wilkinson\n */",
            "/**\n * 分析由 {@link ValidationException} 引起失败的 {@link FailureAnalyzer}。\n * 检测类路径缺少 Bean Validation 实现的情况。\n *\n * @author Andy Wilkinson\n */",
        ),
    ],
    "ConfigTreePropertySource.java": [
        (
            "/**\n * {@link PropertySource} backed by a directory tree that contains files for each value.\n * The {@link PropertySource} will recursively scan a given source directory and expose a\n * property for each file found. The property name will be the filename, and the property\n * value will be the contents of the file.\n * <p>\n * Directories are only scanned when the source is first created. The directory is not\n * monitored for updates, so files should not be added or removed. However, the contents\n * of a file can be updated as long as the property source was created with a\n * {@link Option#ALWAYS_READ} option. Nested directories are included in the source, but\n * with a {@code '.'} rather than {@code '/'} used as the path separator.\n * <p>\n * Property values are returned as {@link Value} instances which allows them to be treated\n * either as an {@link InputStreamSource} or as a {@link CharSequence}. In addition, if\n * used with an {@link Environment} configured with an\n * {@link ApplicationConversionService}, property values can be converted to a\n * {@code String} or {@code byte[]}.\n * <p>\n * This property source is typically used to read Kubernetes {@code configMap} volume\n * mounts.\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 由目录树支持的 {@link PropertySource}，每个值对应一个文件。\n * 该 {@link PropertySource} 会递归扫描给定源目录，并为每个文件暴露一个属性；\n * 属性名为文件名，属性值为文件内容。\n * <p>\n * 目录仅在源首次创建时扫描，不会监控更新，因此不应添加或删除文件。\n * 但若属性源以 {@link Option#ALWAYS_READ} 选项创建，文件内容仍可更新。\n * 嵌套目录会包含在源中，但路径分隔符使用 {@code '.'} 而非 {@code '/'}。\n * <p>\n * 属性值以 {@link Value} 实例返回，可视为 {@link InputStreamSource} 或 {@link CharSequence}。\n * 若与配置了 {@link ApplicationConversionService} 的 {@link Environment} 一起使用，\n * 属性值可转换为 {@code String} 或 {@code byte[]}。\n * <p>\n * 此属性源通常用于读取 Kubernetes {@code configMap} 卷挂载。\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link ConfigTreePropertySource} instance.\n\t * @param name the name of the property source\n\t * @param sourceDirectory the underlying source directory\n\t */",
            "\t/**\n\t * 创建新的 {@link ConfigTreePropertySource} 实例。\n\t *\n\t * @param name 属性源名称\n\t * @param sourceDirectory 底层源目录\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link ConfigTreePropertySource} instance.\n\t * @param name the name of the property source\n\t * @param sourceDirectory the underlying source directory\n\t * @param options the property source options\n\t */",
            "\t/**\n\t * 创建新的 {@link ConfigTreePropertySource} 实例。\n\t *\n\t * @param name 属性源名称\n\t * @param sourceDirectory 底层源目录\n\t * @param options 属性源选项\n\t */",
        ),
        (
            "\t/**\n\t * Property source options.\n\t */",
            "\t/**\n\t * 属性源选项。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Always read the value of the file when accessing the property value. When this\n\t\t * option is not set the property source will cache the value when it's first\n\t\t * read.\n\t\t */",
            "\t\t/**\n\t\t * 访问属性值时始终读取文件内容。\n\t\t * 未设置此选项时，属性源会在首次读取时缓存值。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Convert file and directory names to lowercase.\n\t\t */",
            "\t\t/**\n\t\t * 将文件与目录名转为小写。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Automatically attempt trim trailing new-line characters.\n\t\t */",
            "\t\t/**\n\t\t * 自动尝试去除末尾换行符。\n\t\t */",
        ),
        (
            "\t/**\n\t * A value returned from the property source which exposes the contents of the\n\t * property file. Values can either be treated as {@link CharSequence} or as an\n\t * {@link InputStreamSource}.\n\t */",
            "\t/**\n\t * 属性源返回的值，暴露属性文件内容。\n\t * 值可视为 {@link CharSequence} 或 {@link InputStreamSource}。\n\t */",
        ),
        (
            "\t/**\n\t * A single property file that was found when the source was created.\n\t */",
            "\t/**\n\t * 创建源时发现的单个属性文件。\n\t */",
        ),
        (
            "\t/**\n\t * The contents of a found property file.\n\t */",
            "\t/**\n\t * 已发现属性文件的内容。\n\t */",
        ),
    ],
    "DefaultPropertiesPropertySource.java": [
        (
            "/**\n * {@link MapPropertySource} containing default properties contributed directly to a\n * {@code SpringApplication}. By convention, the {@link DefaultPropertiesPropertySource}\n * is always the last property source in the {@link Environment}.\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 包含直接贡献给 {@code SpringApplication} 的默认属性的 {@link MapPropertySource}。\n * 按约定，{@link DefaultPropertiesPropertySource} 始终是 {@link Environment} 中最后一个属性源。\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "\t/**\n\t * The name of the 'default properties' property source.\n\t */",
            "\t/**\n\t * {@code defaultProperties} 属性源的名称。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link DefaultPropertiesPropertySource} with the given {@code Map}\n\t * source.\n\t * @param source the source map\n\t */",
            "\t/**\n\t * 使用给定 {@code Map} 源创建新的 {@link DefaultPropertiesPropertySource}。\n\t *\n\t * @param source 源 Map\n\t */",
        ),
        (
            "\t/**\n\t * Return {@code true} if the given source is named 'defaultProperties'.\n\t * @param propertySource the property source to check\n\t * @return {@code true} if the name matches\n\t */",
            "\t/**\n\t * 若给定源名为 {@code defaultProperties} 则返回 {@code true}。\n\t *\n\t * @param propertySource 要检查的属性源\n\t * @return {@code true} if the name matches 名称匹配时为 {@code true}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link DefaultPropertiesPropertySource} instance if the provided\n\t * source is not empty.\n\t * @param source the {@code Map} source\n\t * @param action the action used to consume the\n\t * {@link DefaultPropertiesPropertySource}\n\t */",
            "\t/**\n\t * 若提供的源非空，则创建新的 {@link DefaultPropertiesPropertySource} 实例。\n\t *\n\t * @param source {@code Map} 源\n\t * @param action 消费 {@link DefaultPropertiesPropertySource} 的操作\n\t */",
        ),
        (
            "\t/**\n\t * Add a new {@link DefaultPropertiesPropertySource} or merge with an existing one.\n\t * @param source the {@code Map} source\n\t * @param sources the existing sources\n\t * @since 2.4.4\n\t */",
            "\t/**\n\t * 添加新的 {@link DefaultPropertiesPropertySource} 或与现有实例合并。\n\t *\n\t * @param source {@code Map} 源\n\t * @param sources 现有属性源\n\t * @since 2.4.4\n\t */",
        ),
        (
            "\t/**\n\t * Move the 'defaultProperties' property source so that it's the last source in the\n\t * given {@link ConfigurableEnvironment}.\n\t * @param environment the environment to update\n\t */",
            "\t/**\n\t * 将 {@code defaultProperties} 属性源移至给定 {@link ConfigurableEnvironment} 的最后。\n\t *\n\t * @param environment 要更新的环境\n\t */",
        ),
        (
            "\t/**\n\t * Move the 'defaultProperties' property source so that it's the last source in the\n\t * given {@link MutablePropertySources}.\n\t * @param propertySources the property sources to update\n\t */",
            "\t/**\n\t * 将 {@code defaultProperties} 属性源移至给定 {@link MutablePropertySources} 的最后。\n\t *\n\t * @param propertySources 要更新的属性源\n\t */",
        ),
    ],
    "EnvironmentPostProcessor.java": [
        (
            "/**\n * Allows for customization of the application's {@link Environment} prior to the\n * application context being refreshed.\n * <p>\n * EnvironmentPostProcessor implementations have to be registered in\n * {@code META-INF/spring.factories}, using the fully qualified name of this class as the\n * key. Implementations may implement the {@link org.springframework.core.Ordered Ordered}\n * interface or use an {@link org.springframework.core.annotation.Order @Order} annotation\n * if they wish to be invoked in specific order.\n * <p>\n * Since Spring Boot 2.4, {@code EnvironmentPostProcessor} implementations may optionally\n * take the following constructor parameters:\n * <ul>\n * <li>{@link DeferredLogFactory} - A factory that can be used to create loggers with\n * output deferred until the application has been fully prepared (allowing the environment\n * itself to configure logging levels).</li>\n * <li>{@link ConfigurableBootstrapContext} - A bootstrap context that can be used to\n * store objects that may be expensive to create, or need to be shared\n * ({@link BootstrapContext} or {@link BootstrapRegistry} may also be used).</li>\n * </ul>\n *\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @since 1.3.0\n * @deprecated since 4.0.0 for removal in 4.2.0 in favor of\n * {@link org.springframework.boot.EnvironmentPostProcessor}\n */",
            "/**\n * 允许在应用上下文刷新之前定制应用的 {@link Environment}。\n * <p>\n * {@code EnvironmentPostProcessor} 实现须在 {@code META-INF/spring.factories} 中注册，\n * 以本类全限定名作为键。若需按特定顺序调用，可实现 {@link org.springframework.core.Ordered Ordered}\n * 接口或使用 {@link org.springframework.core.annotation.Order @Order} 注解。\n * <p>\n * 自 Spring Boot 2.4 起，{@code EnvironmentPostProcessor} 实现可选接受以下构造参数：\n * <ul>\n * <li>{@link DeferredLogFactory} — 可创建日志输出延迟到应用完全就绪后的 Logger 的工厂\n * （允许环境本身配置日志级别）。</li>\n * <li>{@link ConfigurableBootstrapContext} — 可用于存储创建成本较高或需共享的对象的引导上下文\n * （也可使用 {@link BootstrapContext} 或 {@link BootstrapRegistry}）。</li>\n * </ul>\n *\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @since 1.3.0\n * @deprecated since 4.0.0 for removal in 4.2.0 in favor of\n * {@link org.springframework.boot.EnvironmentPostProcessor}\n */",
        ),
        (
            "\t/**\n\t * Post-process the given {@code environment}.\n\t * @param environment the environment to post-process\n\t * @param application the application to which the environment belongs\n\t */",
            "\t/**\n\t * 后处理给定 {@code environment}。\n\t *\n\t * @param environment 要后处理的环境\n\t * @param application 环境所属的应用\n\t */",
        ),
    ],
    "OriginTrackedMapPropertySource.java": [
        (
            "/**\n * {@link OriginLookup} backed by a {@link Map} containing {@link OriginTrackedValue\n * OriginTrackedValues}.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.0.0\n * @see OriginTrackedValue\n */",
            "/**\n * 由包含 {@link OriginTrackedValue OriginTrackedValues} 的 {@link Map} 支持的 {@link OriginLookup}。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.0.0\n * @see OriginTrackedValue\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link OriginTrackedMapPropertySource} instance.\n\t * @param name the property source name\n\t * @param source the underlying map source\n\t */",
            "\t/**\n\t * 创建新的 {@link OriginTrackedMapPropertySource} 实例。\n\t *\n\t * @param name 属性源名称\n\t * @param source 底层 Map 源\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link OriginTrackedMapPropertySource} instance.\n\t * @param name the property source name\n\t * @param source the underlying map source\n\t * @param immutable if the underlying source is immutable and guaranteed not to change\n\t * @since 2.2.0\n\t */",
            "\t/**\n\t * 创建新的 {@link OriginTrackedMapPropertySource} 实例。\n\t *\n\t * @param name 属性源名称\n\t * @param source 底层 Map 源\n\t * @param immutable 底层源是否不可变且保证不会改变\n\t * @since 2.2.0\n\t */",
        ),
    ],
    "OriginTrackedPropertiesLoader.java": [
        (
            "/**\n * Class to load {@code .properties} files into a map of {@code String} -&gt;\n * {@link OriginTrackedValue}. Also supports expansion of {@code name[]=a,b,c} list style\n * values.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @author Thiago Hirata\n * @author Guirong Hu\n * @author Moritz Halbritter\n */",
            "/**\n * 将 {@code .properties} 文件加载为 {@code String} -&gt; {@link OriginTrackedValue} 映射的类。\n * 也支持展开 {@code name[]=a,b,c} 列表风格值。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @author Thiago Hirata\n * @author Guirong Hu\n * @author Moritz Halbritter\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link OriginTrackedPropertiesLoader} instance.\n\t * @param resource the resource of the {@code .properties} data\n\t */",
            "\t/**\n\t * 创建新的 {@link OriginTrackedPropertiesLoader} 实例。\n\t *\n\t * @param resource {@code .properties} 数据的资源\n\t */",
        ),
        (
            "\t/**\n\t * Load {@code .properties} data and return a list of documents.\n\t * @param encoding the resource encoding. Uses ISO-8859-1 if {@code null}\n\t * @return the loaded properties\n\t * @throws IOException on read error\n\t */",
            "\t/**\n\t * 加载 {@code .properties} 数据并返回文档列表。\n\t *\n\t * @param encoding 资源编码；{@code null} 时使用 ISO-8859-1\n\t * @return the loaded properties 已加载的属性\n\t * @throws IOException on read error 读取错误时\n\t */",
        ),
        (
            "\t/**\n\t * Load {@code .properties} data and return a map of {@code String} ->\n\t * {@link OriginTrackedValue}.\n\t * @param encoding the resource encoding. Uses ISO-8859-1 if {@code null}\n\t * @param expandLists if list {@code name[]=a,b,c} shortcuts should be expanded\n\t * @return the loaded properties\n\t * @throws IOException on read error\n\t */",
            "\t/**\n\t * 加载 {@code .properties} 数据并返回 {@code String} -> {@link OriginTrackedValue} 映射。\n\t *\n\t * @param encoding 资源编码；{@code null} 时使用 ISO-8859-1\n\t * @param expandLists 是否展开 {@code name[]=a,b,c} 列表快捷写法\n\t * @return the loaded properties 已加载的属性\n\t * @throws IOException on read error 读取错误时\n\t */",
        ),
        (
            "\t/**\n\t * Reads characters from the source resource, taking care of skipping comments,\n\t * handling multi-line values and tracking {@code '\\'} escapes.\n\t */",
            "\t/**\n\t * 从源资源读取字符，负责跳过注释、处理多行值并跟踪 {@code '\\'} 转义。\n\t */",
        ),
        (
            "\t/**\n\t * A single document within the properties file.\n\t */",
            "\t/**\n\t * properties 文件中的单个文档。\n\t */",
        ),
    ],
    "OriginTrackedYamlLoader.java": [
        (
            "/**\n * Class to load {@code .yml} files into a map of {@code String} to\n * {@link OriginTrackedValue}.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n */",
            "/**\n * 将 {@code .yml} 文件加载为 {@code String} 到 {@link OriginTrackedValue} 映射的类。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n */",
        ),
        (
            "\t/**\n\t * {@link Constructor} that tracks property origins.\n\t */",
            "\t/**\n\t * 跟踪属性来源的 {@link Constructor}。\n\t */",
        ),
        (
            "\t/**\n\t * {@link ScalarNode} that replaces the key node in a {@link NodeTuple}.\n\t */",
            "\t/**\n\t * 替换 {@link NodeTuple} 中键节点的 {@link ScalarNode}。\n\t */",
        ),
        (
            "\t/**\n\t * {@link Resolver} that limits {@link Tag#TIMESTAMP} tags.\n\t */",
            "\t/**\n\t * 限制 {@link Tag#TIMESTAMP} 标签的 {@link Resolver}。\n\t */",
        ),
    ],
    "PropertiesPropertySourceLoader.java": [
        (
            "/**\n * Strategy to load '.properties' files into a {@link PropertySource}.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 1.0.0\n */",
            "/**\n * 将 {@code .properties} 与 {@code .xml} 文件加载到 {@link PropertySource} 的策略。\n * 支持多文档 properties 并保留属性来源信息。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 1.0.0\n */",
        ),
    ],
    "PropertySourceInfo.java": [
        (
            "/**\n * Interface that can be optionally implemented by a {@link PropertySource} to provide\n * additional information.\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
            "/**\n * {@link PropertySource} 可选实现的接口，用于提供附加信息。\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
        ),
        (
            "\t/**\n\t * Return {@code true} if this lookup is immutable and has contents that will never\n\t * change.\n\t * @return if the lookup is immutable\n\t */",
            "\t/**\n\t * 若此查找不可变且内容永不变更则返回 {@code true}。\n\t *\n\t * @return if the lookup is immutable 查找是否不可变\n\t */",
        ),
        (
            "\t/**\n\t * Return the implicit prefix that is applied when performing a lookup or {@code null}\n\t * if no prefix is used. Prefixes can be used to disambiguate keys that would\n\t * otherwise clash. For example, if multiple applications are running on the same\n\t * machine a different prefix can be set on each application to ensure that different\n\t * environment variables are used.\n\t * @return the prefix applied by the lookup class or {@code null}.\n\t */",
            "\t/**\n\t * 返回查找时应用的隐式前缀；未使用前缀时返回 {@code null}。\n\t * 前缀可用于消歧可能冲突的键。例如同一机器上运行多个应用时，\n\t * 可为每个应用设置不同前缀以确保使用不同环境变量。\n\t *\n\t * @return the prefix applied by the lookup class or {@code null} 查找类应用的前缀或 {@code null}\n\t */",
        ),
    ],
    "PropertySourceLoader.java": [
        (
            "/**\n * Strategy interface located through {@link SpringFactoriesLoader} and used to load a\n * {@link PropertySource}.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @since 1.0.0\n */",
            "/**\n * 通过 {@link SpringFactoriesLoader} 定位、用于加载 {@link PropertySource} 的策略接口。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @since 1.0.0\n */",
        ),
        (
            "\t/**\n\t * Returns the file extensions that the loader supports (excluding the '.').\n\t * @return the file extensions\n\t */",
            "\t/**\n\t * 返回加载器支持的文件扩展名（不含 {@code .}）。\n\t *\n\t * @return the file extensions 文件扩展名\n\t */",
        ),
        (
            "\t/**\n\t * Load the resource into one or more property sources. Implementations may either\n\t * return a list containing a single source, or in the case of a multi-document format\n\t * such as yaml a source for each document in the resource.\n\t * @param name the root name of the property source. If multiple documents are loaded\n\t * an additional suffix should be added to the name for each source loaded.\n\t * @param resource the resource to load\n\t * @return a list property sources\n\t * @throws IOException if the source cannot be loaded\n\t */",
            "\t/**\n\t * 将资源加载为一个或多个属性源。实现可返回仅含单个源的列表，\n\t * 对于 yaml 等多文档格式，可为资源中每个文档返回一个源。\n\t *\n\t * @param name 属性源的根名称；加载多个文档时，每个源应在名称上追加后缀\n\t * @param resource 要加载的资源\n\t * @return a list property sources 属性源列表\n\t * @throws IOException if the source cannot be loaded 无法加载源时\n\t */",
        ),
        (
            "\t/**\n\t * Load the resource into one or more property sources. Implementations may either\n\t * return a list containing a single source, or in the case of a multi-document format\n\t * such as yaml a source for each document in the resource.\n\t * @param name the root name of the property source. If multiple documents are loaded\n\t * an additional suffix should be added to the name for each source loaded.\n\t * @param resource the resource to load\n\t * @param encoding encoding of the resource\n\t * @return a list property sources\n\t * @throws IOException if the source cannot be loaded\n\t * @since 4.1.0\n\t */",
            "\t/**\n\t * 将资源加载为一个或多个属性源。实现可返回仅含单个源的列表，\n\t * 对于 yaml 等多文档格式，可为资源中每个文档返回一个源。\n\t *\n\t * @param name 属性源的根名称；加载多个文档时，每个源应在名称上追加后缀\n\t * @param resource 要加载的资源\n\t * @param encoding 资源编码\n\t * @return a list property sources 属性源列表\n\t * @throws IOException if the source cannot be loaded 无法加载源时\n\t * @since 4.1.0\n\t */",
        ),
    ],
    "PropertySourceRuntimeHints.java": [
        (
            "/**\n * {@link RuntimeHintsRegistrar} implementation for property source support.\n *\n * @author Stephane Nicoll\n */",
            "/**\n * 属性源支持的 {@link RuntimeHintsRegistrar} 实现。\n * 为 AOT 原生镜像注册 SnakeYAML 反射提示。\n *\n * @author Stephane Nicoll\n */",
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


def update_batch_counts() -> None:
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
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
            if cn < 10 or not lic or not has_chinese(text):
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if ok == len(BATCH_FILES) and not failures:
        subprocess.run(
            [
                sys.executable,
                str(ROOT / "scripts/mark_batch_done.py"),
                "--project",
                "springboot",
                "--version",
                "4.1.0",
                "--note",
                "wave10a [0:20]",
                *BATCH_FILES,
            ],
            check=True,
        )
        update_batch_counts()
        print(f"Marked {ok} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
