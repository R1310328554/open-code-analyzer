#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-3 slice [0:20]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][:20]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AutoConfigurationImportSelector.java": [
        (
            "/**\n * {@link DeferredImportSelector} to handle {@link EnableAutoConfiguration\n * auto-configuration}. This class can also be subclassed if a custom variant of\n * {@link EnableAutoConfiguration @EnableAutoConfiguration} is needed.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @author Madhura Bhave\n * @author Moritz Halbritter\n * @author Scott Frederick\n * @since 1.3.0\n * @see EnableAutoConfiguration\n */",
            "/**\n * 用于处理 {@link EnableAutoConfiguration 自动配置} 的 {@link DeferredImportSelector}。\n * 若需要自定义 {@link EnableAutoConfiguration @EnableAutoConfiguration} 变体，也可子类化此类。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @author Madhura Bhave\n * @author Moritz Halbritter\n * @author Scott Frederick\n * @since 1.3.0\n * @see EnableAutoConfiguration\n */",
        ),
        (
            "/**\n\t * Return the {@link AutoConfigurationEntry} based on the {@link AnnotationMetadata}\n\t * of the importing {@link Configuration @Configuration} class.\n\t * @param annotationMetadata the annotation metadata of the configuration class\n\t * @return the auto-configurations that should be imported\n\t */",
            "/**\n\t * 根据导入的 {@link Configuration @Configuration} 类的 {@link AnnotationMetadata}\n\t * 返回 {@link AutoConfigurationEntry}。\n\t * @param annotationMetadata 配置类的注解元数据\n\t * @return 应导入的自动配置\n\t */",
        ),
        (
            "/**\n\t * Return the appropriate {@link AnnotationAttributes} from the\n\t * {@link AnnotationMetadata}. By default this method will return attributes for\n\t * {@link #getAnnotationClass()}.\n\t * @param metadata the annotation metadata\n\t * @return annotation attributes\n\t */",
            "/**\n\t * 从 {@link AnnotationMetadata} 返回相应的 {@link AnnotationAttributes}。\n\t * 默认情况下返回 {@link #getAnnotationClass()} 对应的属性。\n\t * @param metadata 注解元数据\n\t * @return 注解属性\n\t */",
        ),
        (
            "/**\n\t * Return the source annotation class used by the selector.\n\t * @return the annotation class\n\t */",
            "/**\n\t * 返回选择器使用的源注解类。\n\t * @return 注解类\n\t */",
        ),
        (
            "/**\n\t * Return the auto-configuration class names that should be considered. By default,\n\t * this method will load candidates using {@link ImportCandidates}.\n\t * @param metadata the source metadata\n\t * @param attributes the {@link #getAttributes(AnnotationMetadata) annotation\n\t * attributes}\n\t * @return a list of candidate configurations\n\t */",
            "/**\n\t * 返回应考虑的自动配置类名。默认通过 {@link ImportCandidates} 加载候选类。\n\t * @param metadata 源元数据\n\t * @param attributes {@link #getAttributes(AnnotationMetadata) 注解属性}\n\t * @return 候选配置列表\n\t */",
        ),
        (
            "/**\n\t * Handle any invalid excludes that have been specified.\n\t * @param invalidExcludes the list of invalid excludes (will always have at least one\n\t * element)\n\t */",
            "/**\n\t * 处理指定的无效排除项。\n\t * @param invalidExcludes 无效排除项列表（至少包含一个元素）\n\t */",
        ),
        (
            "/**\n\t * Return any exclusions that limit the candidate configurations.\n\t * @param metadata the source metadata\n\t * @param attributes the {@link #getAttributes(AnnotationMetadata) annotation\n\t * attributes}\n\t * @return exclusions or an empty set\n\t */",
            "/**\n\t * 返回限制候选配置的排除项。\n\t * @param metadata 源元数据\n\t * @param attributes {@link #getAttributes(AnnotationMetadata) 注解属性}\n\t * @return 排除项或空集合\n\t */",
        ),
        (
            "/**\n\t * Returns the auto-configurations excluded by the\n\t * {@code spring.autoconfigure.exclude} property.\n\t * @return excluded auto-configurations\n\t * @since 2.3.2\n\t */",
            "/**\n\t * 返回由 {@code spring.autoconfigure.exclude} 属性排除的自动配置。\n\t * @return 被排除的自动配置\n\t * @since 2.3.2\n\t */",
        ),
        (
            "/**\n\t\t * Create an entry with the configurations that were contributed and their\n\t\t * exclusions.\n\t\t * @param configurations the configurations that should be imported\n\t\t * @param exclusions the exclusions that were applied to the original list\n\t\t */",
            "/**\n\t\t * 使用已贡献的配置及其排除项创建条目。\n\t\t * @param configurations 应导入的配置\n\t\t * @param exclusions 应用于原始列表的排除项\n\t\t */",
        ),
    ],
    "TaskSchedulingProperties.java": [
        (
            "/**\n * Configuration properties for task scheduling.\n *\n * @author Stephane Nicoll\n * @since 2.1.0\n */",
            "/**\n * 任务调度的配置属性。\n *\n * @author Stephane Nicoll\n * @since 2.1.0\n */",
        ),
        (
            "\t/**\n\t * Prefix to use for the names of newly created threads.\n\t */",
            "\t/**\n\t * 新创建线程名称使用的前缀。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Maximum allowed number of threads. Doesn't have an effect if virtual threads\n\t\t * are enabled.\n\t\t */",
            "\t\t/**\n\t\t * 允许的最大线程数。启用虚拟线程时无效。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Set the maximum number of parallel accesses allowed. -1 indicates no\n\t\t * concurrency limit at all.\n\t\t */",
            "\t\t/**\n\t\t * 设置允许的最大并行访问数。{@code -1} 表示完全不限制并发。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Whether the executor should wait for scheduled tasks to complete on shutdown.\n\t\t */",
            "\t\t/**\n\t\t * 关闭时执行器是否应等待已调度任务完成。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Maximum time the executor should wait for remaining tasks to complete.\n\t\t */",
            "\t\t/**\n\t\t * 执行器等待剩余任务完成的最长时间。\n\t\t */",
        ),
    ],
    "PathBasedTemplateAvailabilityProvider.java": [
        (
            "/**\n * Abstract base class for {@link TemplateAvailabilityProvider} implementations that find\n * templates from paths.\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 1.4.6\n */",
            "/**\n * 从路径查找模板的 {@link TemplateAvailabilityProvider} 实现抽象基类。\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 1.4.6\n */",
        ),
    ],
    "TemplateAvailabilityProvider.java": [
        (
            "/**\n * Indicates the availability of view templates for a particular templating engine such as\n * FreeMarker or Thymeleaf.\n *\n * @author Andy Wilkinson\n * @since 1.1.0\n */",
            "/**\n * 指示特定模板引擎（如 FreeMarker 或 Thymeleaf）的视图模板是否可用。\n *\n * @author Andy Wilkinson\n * @since 1.1.0\n */",
        ),
        (
            "\t/**\n\t * Returns {@code true} if a template is available for the given {@code view}.\n\t * @param view the view name\n\t * @param environment the environment\n\t * @param classLoader the class loader\n\t * @param resourceLoader the resource loader\n\t * @return if the template is available\n\t */",
            "\t/**\n\t * 若给定 {@code view} 的模板可用则返回 {@code true}。\n\t * @param view 视图名\n\t * @param environment 环境\n\t * @param classLoader 类加载器\n\t * @param resourceLoader 资源加载器\n\t * @return 模板是否可用\n\t */",
        ),
    ],
    "TemplateAvailabilityProviders.java": [
        (
            "/**\n * Collection of {@link TemplateAvailabilityProvider} beans that can be used to check\n * which (if any) templating engine supports a given view. Caches responses unless the\n * {@code spring.template.provider.cache} property is set to {@code false}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 1.4.0\n */",
            "/**\n * {@link TemplateAvailabilityProvider} Bean 集合，用于检查哪个（若有）模板引擎支持给定视图。\n * 除非 {@code spring.template.provider.cache} 设为 {@code false}，否则缓存响应。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 1.4.0\n */",
        ),
        (
            "\t/**\n\t * Resolved template views, returning already cached instances without a global lock.\n\t */",
            "\t/**\n\t * 已解析的模板视图，无需全局锁即可返回已缓存实例。\n\t */",
        ),
        (
            "\t/**\n\t * Map from view name resolve template view, synchronized when accessed.\n\t */",
            "\t/**\n\t * 从视图名到已解析模板提供者的映射，访问时同步。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link TemplateAvailabilityProviders} instance.\n\t * @param applicationContext the source application context\n\t */",
            "\t/**\n\t * 创建新的 {@link TemplateAvailabilityProviders} 实例。\n\t * @param applicationContext 源应用上下文\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link TemplateAvailabilityProviders} instance.\n\t * @param classLoader the source class loader\n\t */",
            "\t/**\n\t * 创建新的 {@link TemplateAvailabilityProviders} 实例。\n\t * @param classLoader 源类加载器\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link TemplateAvailabilityProviders} instance.\n\t * @param providers the underlying providers\n\t */",
            "\t/**\n\t * 创建新的 {@link TemplateAvailabilityProviders} 实例。\n\t * @param providers 底层提供者\n\t */",
        ),
        (
            "\t/**\n\t * Return the underlying providers being used.\n\t * @return the providers being used\n\t */",
            "\t/**\n\t * 返回正在使用的底层提供者。\n\t * @return 正在使用的提供者\n\t */",
        ),
        (
            "\t/**\n\t * Get the provider that can be used to render the given view.\n\t * @param view the view to render\n\t * @param applicationContext the application context\n\t * @return a {@link TemplateAvailabilityProvider} or null\n\t */",
            "\t/**\n\t * 获取可用于渲染给定视图的提供者。\n\t * @param view 要渲染的视图\n\t * @param applicationContext 应用上下文\n\t * @return {@link TemplateAvailabilityProvider} 或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Get the provider that can be used to render the given view.\n\t * @param view the view to render\n\t * @param environment the environment\n\t * @param classLoader the class loader\n\t * @param resourceLoader the resource loader\n\t * @return a {@link TemplateAvailabilityProvider} or null\n\t */",
            "\t/**\n\t * 获取可用于渲染给定视图的提供者。\n\t * @param view 要渲染的视图\n\t * @param environment 环境\n\t * @param classLoader 类加载器\n\t * @param resourceLoader 资源加载器\n\t * @return {@link TemplateAvailabilityProvider} 或 {@code null}\n\t */",
        ),
    ],
    "TemplateLocation.java": [
        (
            "/**\n * Contains a location that templates can be loaded from.\n *\n * @author Phillip Webb\n * @since 1.2.1\n */",
            "/**\n * 包含可从中加载模板的位置。\n *\n * @author Phillip Webb\n * @since 1.2.1\n */",
        ),
        (
            "\t/**\n\t * Determine if this template location exists using the specified\n\t * {@link ResourcePatternResolver}.\n\t * @param resolver the resolver used to test if the location exists\n\t * @return {@code true} if the location exists.\n\t */",
            "\t/**\n\t * 使用指定的 {@link ResourcePatternResolver} 判断此模板位置是否存在。\n\t * @param resolver 用于测试位置是否存在的解析器\n\t * @return 位置存在时返回 {@code true}\n\t */",
        ),
    ],
    "TemplateRuntimeHints.java": [
        (
            "/**\n * {@link RuntimeHintsRegistrar} for default template location.\n *\n * @author Stephane Nicoll\n */",
            "/**\n * 默认模板位置的 {@link RuntimeHintsRegistrar}。\n *\n * @author Stephane Nicoll\n */",
        ),
    ],
    "ConditionalOnEnabledResourceChain.java": [
        (
            "/**\n * {@link Conditional @Conditional} that checks whether the Spring resource handling chain\n * is enabled. Matches if {@link WebProperties.Resources.Chain#getEnabled()} is\n * {@code true} or if one of {@code \"org.webjars:webjars-locator-core\"},\n * {@code \"org.webjars:webjars-locator-lite\"} is on the classpath.\n * <p>\n * Note that support for {@code \"org.webjars:webjars-locator-core\"} is deprecated.\n *\n * @author Stephane Nicoll\n * @since 1.3.0\n */",
            "/**\n * 检查 Spring 资源处理链是否启用的 {@link Conditional @Conditional}。\n * 若 {@link WebProperties.Resources.Chain#getEnabled()} 为 {@code true}，\n * 或类路径上存在 {@code \"org.webjars:webjars-locator-core\"}、\n * {@code \"org.webjars:webjars-locator-lite\"} 之一则匹配。\n * <p>\n * 注意：对 {@code \"org.webjars:webjars-locator-core\"} 的支持已弃用。\n *\n * @author Stephane Nicoll\n * @since 1.3.0\n */",
        ),
    ],
    "ErrorProperties.java": [
        (
            "/**\n * Configuration properties for web error handling.\n *\n * @author Michael Stummvoll\n * @author Stephane Nicoll\n * @author Vedran Pavic\n * @author Scott Frederick\n * @since 1.3.0\n */",
            "/**\n * Web 错误处理的配置属性。\n *\n * @author Michael Stummvoll\n * @author Stephane Nicoll\n * @author Vedran Pavic\n * @author Scott Frederick\n * @since 1.3.0\n */",
        ),
        (
            "\t/**\n\t * Path of the error controller.\n\t */",
            "\t/**\n\t * 错误控制器的路径。\n\t */",
        ),
        (
            "\t/**\n\t * Include the \"exception\" attribute.\n\t */",
            "\t/**\n\t * 是否包含 {@code exception} 属性。\n\t */",
        ),
        (
            "\t/**\n\t * When to include the \"trace\" attribute.\n\t */",
            "\t/**\n\t * 何时包含 {@code trace} 属性。\n\t */",
        ),
        (
            "\t/**\n\t * When to include \"message\" attribute.\n\t */",
            "\t/**\n\t * 何时包含 {@code message} 属性。\n\t */",
        ),
        (
            "\t/**\n\t * When to include \"errors\" attribute.\n\t */",
            "\t/**\n\t * 何时包含 {@code errors} 属性。\n\t */",
        ),
        (
            "\t/**\n\t * When to include \"path\" attribute.\n\t */",
            "\t/**\n\t * 何时包含 {@code path} 属性。\n\t */",
        ),
        (
            "\t/**\n\t * Include error attributes options.\n\t */",
            "\t/**\n\t * 错误属性包含选项。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Never add error attribute.\n\t\t */",
            "\t\t/**\n\t\t * 从不添加错误属性。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Always add error attribute.\n\t\t */",
            "\t\t/**\n\t\t * 始终添加错误属性。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Add error attribute when the appropriate request parameter is not \"false\".\n\t\t */",
            "\t\t/**\n\t\t * 当相应请求参数不为 {@code false} 时添加错误属性。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Whether to enable the default error page displayed in browsers in case of a\n\t\t * server error.\n\t\t */",
            "\t\t/**\n\t\t * 是否在发生服务器错误时在浏览器中启用默认错误页。\n\t\t */",
        ),
    ],
    "OnEnabledResourceChainCondition.java": [
        (
            "/**\n * {@link Condition} that checks whether the Spring resource handling chain is enabled.\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Brian Clozel\n * @see ConditionalOnEnabledResourceChain\n */",
            "/**\n * 检查 Spring 资源处理链是否启用的 {@link Condition}。\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Brian Clozel\n * @see ConditionalOnEnabledResourceChain\n */",
        ),
    ],
    "WebProperties.java": [
        (
            "/**\n * {@link ConfigurationProperties Configuration properties} for general web concerns.\n *\n * @author Andy Wilkinson\n * @since 2.4.0\n */",
            "/**\n * 通用 Web 相关事项的 {@link ConfigurationProperties 配置属性}。\n *\n * @author Andy Wilkinson\n * @since 2.4.0\n */",
        ),
        (
            "\t/**\n\t * Locale to use. By default, this locale is overridden by the \"Accept-Language\"\n\t * header.\n\t */",
            "\t/**\n\t * 要使用的区域设置。默认情况下会被 {@code Accept-Language} 请求头覆盖。\n\t */",
        ),
        (
            "\t/**\n\t * Define how the locale should be resolved.\n\t */",
            "\t/**\n\t * 定义应如何解析区域设置。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Always use the configured locale.\n\t\t */",
            "\t\t/**\n\t\t * 始终使用配置的区域设置。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Use the \"Accept-Language\" header or the configured locale if the header is not\n\t\t * set.\n\t\t */",
            "\t\t/**\n\t\t * 使用 {@code Accept-Language} 请求头；若未设置请求头则使用配置的区域设置。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Locations of static resources. Defaults to classpath:[/META-INF/resources/,\n\t\t * /resources/, /static/, /public/].\n\t\t */",
            "\t\t/**\n\t\t * 静态资源位置。默认为 classpath:[/META-INF/resources/、/resources/、/static/、/public/]。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Whether to enable default resource handling.\n\t\t */",
            "\t\t/**\n\t\t * 是否启用默认资源处理。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Configuration for the Spring Resource Handling chain.\n\t\t */",
            "\t\t/**\n\t\t * Spring 资源处理链的配置。\n\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * Whether to enable the Spring Resource Handling chain. By default, disabled\n\t\t\t * unless at least one strategy has been enabled.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 是否启用 Spring 资源处理链。默认禁用，除非至少启用一种策略。\n\t\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * Whether to enable caching in the Resource chain.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 是否在资源链中启用缓存。\n\t\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * Whether to enable resolution of already compressed resources (gzip,\n\t\t\t * brotli). Checks for a resource name with the '.gz' or '.br' file\n\t\t\t * extensions.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 是否启用对已压缩资源（gzip、brotli）的解析。\n\t\t\t * 检查带有 {@code .gz} 或 {@code .br} 扩展名的资源名。\n\t\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * Return whether the resource chain is enabled. Return {@code null} if no\n\t\t\t * specific settings are present.\n\t\t\t * @return whether the resource chain is enabled or {@code null} if no\n\t\t\t * specified settings are present.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 返回资源链是否启用。若无特定设置则返回 {@code null}。\n\t\t\t * @return 资源链是否启用；无指定设置时返回 {@code null}\n\t\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * Strategies for extracting and embedding a resource version in its URL path.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 在资源 URL 路径中提取并嵌入版本号的策略。\n\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Version Strategy based on content hashing.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 基于内容哈希的版本策略。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t\t/**\n\t\t\t\t\t * Whether to enable the content Version Strategy.\n\t\t\t\t\t */",
            "\t\t\t\t\t/**\n\t\t\t\t\t * 是否启用内容版本策略。\n\t\t\t\t\t */",
        ),
        (
            "\t\t\t\t\t/**\n\t\t\t\t\t * List of patterns to apply to the content Version Strategy.\n\t\t\t\t\t */",
            "\t\t\t\t\t/**\n\t\t\t\t\t * 应用于内容版本策略的模式列表。\n\t\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Version Strategy based on a fixed version string.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 基于固定版本字符串的版本策略。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t\t/**\n\t\t\t\t\t * Whether to enable the fixed Version Strategy.\n\t\t\t\t\t */",
            "\t\t\t\t\t/**\n\t\t\t\t\t * 是否启用固定版本策略。\n\t\t\t\t\t */",
        ),
        (
            "\t\t\t\t\t/**\n\t\t\t\t\t * List of patterns to apply to the fixed Version Strategy.\n\t\t\t\t\t */",
            "\t\t\t\t\t/**\n\t\t\t\t\t * 应用于固定版本策略的模式列表。\n\t\t\t\t\t */",
        ),
        (
            "\t\t\t\t\t/**\n\t\t\t\t\t * Version string to use for the fixed Version Strategy.\n\t\t\t\t\t */",
            "\t\t\t\t\t/**\n\t\t\t\t\t * 固定版本策略使用的版本字符串。\n\t\t\t\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Cache configuration.\n\t\t */",
            "\t\t/**\n\t\t * 缓存配置。\n\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * Cache period for the resources served by the resource handler. If a\n\t\t\t * duration suffix is not specified, seconds will be used. Can be overridden\n\t\t\t * by the 'spring.web.resources.cache.cachecontrol' properties.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 资源处理器所提供资源的缓存周期。未指定持续时间后缀时使用秒。\n\t\t\t * 可被 {@code spring.web.resources.cache.cachecontrol} 属性覆盖。\n\t\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * Cache control HTTP headers, only allows valid directive combinations.\n\t\t\t * Overrides the 'spring.web.resources.cache.period' property.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 缓存控制 HTTP 头，仅允许有效的指令组合。\n\t\t\t * 覆盖 {@code spring.web.resources.cache.period} 属性。\n\t\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * Whether we should use the \"lastModified\" metadata of the files in HTTP\n\t\t\t * caching headers.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 是否在 HTTP 缓存头中使用文件的 {@code lastModified} 元数据。\n\t\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * Cache Control HTTP header configuration.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 缓存控制 HTTP 头配置。\n\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Maximum time the response should be cached, in seconds if no duration\n\t\t\t\t * suffix is not specified.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 响应应被缓存的最长时间；未指定持续时间后缀时使用秒。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Indicate that the cached response can be reused only if re-validated\n\t\t\t\t * with the server.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 指示缓存响应仅在与服务器重新验证后才可复用。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Indicate to not cache the response in any case.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 指示在任何情况下都不缓存响应。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Indicate that once it has become stale, a cache must not use the\n\t\t\t\t * response without re-validating it with the server.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 指示缓存过期后，未经与服务器重新验证不得使用该响应。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Indicate intermediaries (caches and others) that they should not\n\t\t\t\t * transform the response content.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 指示中间节点（缓存等）不得转换响应内容。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Indicate that any cache may store the response.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 指示任何缓存均可存储该响应。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Indicate that the response message is intended for a single user and\n\t\t\t\t * must not be stored by a shared cache.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 指示响应消息仅供单个用户使用，共享缓存不得存储。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Same meaning as the \"must-revalidate\" directive, except that it does\n\t\t\t\t * not apply to private caches.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 与 {@code must-revalidate} 指令含义相同，但不适用于私有缓存。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Maximum time the response can be served after it becomes stale, in\n\t\t\t\t * seconds if no duration suffix is not specified.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 响应过期后仍可提供的最大时间；未指定持续时间后缀时使用秒。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Maximum time the response may be used when errors are encountered, in\n\t\t\t\t * seconds if no duration suffix is not specified.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 遇到错误时响应仍可使用的最大时间；未指定持续时间后缀时使用秒。\n\t\t\t\t */",
        ),
        (
            "\t\t\t\t/**\n\t\t\t\t * Maximum time the response should be cached by shared caches, in seconds\n\t\t\t\t * if no duration suffix is not specified.\n\t\t\t\t */",
            "\t\t\t\t/**\n\t\t\t\t * 共享缓存应缓存响应的最长时间；未指定持续时间后缀时使用秒。\n\t\t\t\t */",
        ),
    ],
    "WebResourcesRuntimeHints.java": [
        (
            "/**\n * {@link RuntimeHintsRegistrar} for default locations of web resources.\n *\n * @author Stephane Nicoll\n * @since 3.0.0\n */",
            "/**\n * 默认 Web 资源位置的 {@link RuntimeHintsRegistrar}。\n *\n * @author Stephane Nicoll\n * @since 3.0.0\n */",
        ),
    ],
    "DateTimeFormatters.java": [
        (
            "/**\n * {@link DateTimeFormatter Formatters} for dates, times, and date-times.\n *\n * @author Andy Wilkinson\n * @author Gaurav Pareek\n * @since 2.3.0\n */",
            "/**\n * 日期、时间与日期时间的 {@link DateTimeFormatter 格式化器}。\n *\n * @author Andy Wilkinson\n * @author Gaurav Pareek\n * @since 2.3.0\n */",
        ),
        (
            "\t/**\n\t * Configures the date format using the given {@code pattern}.\n\t * @param pattern the pattern for formatting dates\n\t * @return {@code this} for chained method invocation\n\t */",
            "\t/**\n\t * 使用给定 {@code pattern} 配置日期格式。\n\t * @param pattern 日期格式化模式\n\t * @return {@code this}，支持链式调用\n\t */",
        ),
        (
            "\t/**\n\t * Configures the time format using the given {@code pattern}.\n\t * @param pattern the pattern for formatting times\n\t * @return {@code this} for chained method invocation\n\t */",
            "\t/**\n\t * 使用给定 {@code pattern} 配置时间格式。\n\t * @param pattern 时间格式化模式\n\t * @return {@code this}，支持链式调用\n\t */",
        ),
        (
            "\t/**\n\t * Configures the date-time format using the given {@code pattern}.\n\t * @param pattern the pattern for formatting date-times\n\t * @return {@code this} for chained method invocation\n\t */",
            "\t/**\n\t * 使用给定 {@code pattern} 配置日期时间格式。\n\t * @param pattern 日期时间格式化模式\n\t * @return {@code this}，支持链式调用\n\t */",
        ),
    ],
    "WebConversionService.java": [
        (
            "/**\n * {@link org.springframework.format.support.FormattingConversionService} dedicated to web\n * applications for formatting and converting values to/from the web.\n *\n * @author Brian Clozel\n * @since 2.0.0\n */",
            "/**\n * 专用于 Web 应用程序、负责与 Web 之间格式化与转换值的\n * {@link org.springframework.format.support.FormattingConversionService}。\n *\n * @author Brian Clozel\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Create a new WebConversionService that configures formatters with the provided\n\t * date, time, and date-time formats, or registers the default if no custom format is\n\t * provided.\n\t * @param dateTimeFormatters the formatters to use for date, time, and date-time\n\t * formatting\n\t * @since 2.3.0\n\t */",
            "\t/**\n\t * 创建新的 WebConversionService，使用提供的日期、时间与日期时间格式配置格式化器；\n\t * 若未提供自定义格式则注册默认值。\n\t * @param dateTimeFormatters 用于日期、时间与日期时间格式化的格式化器\n\t * @since 2.3.0\n\t */",
        ),
        (
            "\t/**\n\t * Create a new WebConversionService that configures formatters with the provided\n\t * date, time, and date-time formats, or registers the default if no custom format is\n\t * provided. The given {@code embeddedValueResolver} is used to resolve embedded\n\t * values such as property placeholders in\n\t * {@link org.springframework.format.annotation.DateTimeFormat#pattern()} patterns.\n\t * @param embeddedValueResolver the embedded value resolver to use, or {@code null}\n\t * @param dateTimeFormatters the formatters to use for date, time, and date-time\n\t * formatting\n\t * @since 4.1.0\n\t */",
            "\t/**\n\t * 创建新的 WebConversionService，使用提供的日期、时间与日期时间格式配置格式化器；\n\t * 若未提供自定义格式则注册默认值。\n\t * 给定 {@code embeddedValueResolver} 用于解析嵌入值，\n\t * 例如 {@link org.springframework.format.annotation.DateTimeFormat#pattern()} 模式中的属性占位符。\n\t * @param embeddedValueResolver 要使用的嵌入值解析器，或 {@code null}\n\t * @param dateTimeFormatters 用于日期、时间与日期时间格式化的格式化器\n\t * @since 4.1.0\n\t */",
        ),
    ],
    "AotInitializerNotFoundException.java": [
        (
            "/**\n * Exception thrown when the AOT initializer couldn't be found.\n *\n * @author Moritz Halbritter\n * @since 3.2.6\n */",
            "/**\n * 找不到 AOT 初始化器时抛出的异常。\n *\n * @author Moritz Halbritter\n * @since 3.2.6\n */",
        ),
    ],
    "ApplicationArguments.java": [
        (
            "/**\n * Provides access to the arguments that were used to run a {@link SpringApplication}.\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
            "/**\n * 提供对运行 {@link SpringApplication} 时所用参数的访问。\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
        ),
        (
            "\t/**\n\t * Return the raw unprocessed arguments that were passed to the application.\n\t * @return the arguments\n\t */",
            "\t/**\n\t * 返回传递给应用的原始未处理参数。\n\t * @return 参数\n\t */",
        ),
        (
            "\t/**\n\t * Return the names of all option arguments. For example, if the arguments were\n\t * \"--foo=bar --debug\" would return the values {@code [\"foo\", \"debug\"]}.\n\t * @return the option names or an empty set\n\t */",
            "\t/**\n\t * 返回所有选项参数的名称。例如参数为 {@code --foo=bar --debug} 时\n\t * 返回 {@code [\"foo\", \"debug\"]}。\n\t * @return 选项名称或空集合\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the set of option arguments parsed from the arguments contains an\n\t * option with the given name.\n\t * @param name the name to check\n\t * @return {@code true} if the arguments contain an option with the given name\n\t */",
            "\t/**\n\t * 返回从参数解析出的选项集合是否包含给定名称的选项。\n\t * @param name 要检查的名称\n\t * @return 参数包含该名称的选项时返回 {@code true}\n\t */",
        ),
        (
            "\t/**\n\t * Return the collection of values associated with the arguments option having the\n\t * given name.\n\t * <ul>\n\t * <li>if the option is present and has no argument (e.g.: \"--foo\"), return an empty\n\t * collection ({@code []})</li>\n\t * <li>if the option is present and has a single value (e.g. \"--foo=bar\"), return a\n\t * collection having one element ({@code [\"bar\"]})</li>\n\t * <li>if the option is present and has multiple values (e.g. \"--foo=bar --foo=baz\"),\n\t * return a collection having elements for each value ({@code [\"bar\", \"baz\"]})</li>\n\t * <li>if the option is not present, return {@code null}</li>\n\t * </ul>\n\t * @param name the name of the option\n\t * @return a list of option values for the given name\n\t */",
            "\t/**\n\t * 返回与给定名称选项关联的值集合。\n\t * <ul>\n\t * <li>若选项存在且无参数（如 {@code --foo}），返回空集合（{@code []}）</li>\n\t * <li>若选项存在且有一个值（如 {@code --foo=bar}），返回单元素集合（{@code [\"bar\"]}）</li>\n\t * <li>若选项存在且有多个值（如 {@code --foo=bar --foo=baz}），\n\t * 返回包含各值的集合（{@code [\"bar\", \"baz\"]}）</li>\n\t * <li>若选项不存在，返回 {@code null}</li>\n\t * </ul>\n\t * @param name 选项名称\n\t * @return 给定名称的选项值列表\n\t */",
        ),
        (
            "\t/**\n\t * Return the collection of non-option arguments parsed.\n\t * @return the non-option arguments or an empty list\n\t */",
            "\t/**\n\t * 返回解析出的非选项参数集合。\n\t * @return 非选项参数或空列表\n\t */",
        ),
    ],
    "ApplicationContextFactory.java": [
        (
            "/**\n * Strategy interface for creating the {@link ConfigurableApplicationContext} used by a\n * {@link SpringApplication}. Created contexts should be returned in their default form,\n * with the {@code SpringApplication} responsible for configuring and refreshing the\n * context.\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 为 {@link SpringApplication} 创建所用 {@link ConfigurableApplicationContext} 的策略接口。\n * 创建的上下文应以默认形式返回，由 {@code SpringApplication} 负责配置与刷新上下文。\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "\t/**\n\t * A default {@link ApplicationContextFactory} implementation that will create an\n\t * appropriate context for the {@link WebApplicationType}.\n\t */",
            "\t/**\n\t * 默认 {@link ApplicationContextFactory} 实现，将根据 {@link WebApplicationType} 创建合适的上下文。\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@link Environment} type expected to be set on the\n\t * {@link #create(WebApplicationType) created} application context. The result of this\n\t * method can be used to convert an existing environment instance to the correct type.\n\t * @param webApplicationType the web application type or {@code null}\n\t * @return the expected application context type or {@code null} to use the default\n\t * @since 2.6.14\n\t */",
            "\t/**\n\t * 返回应设置在 {@link #create(WebApplicationType) 创建的} 应用上下文上的\n\t * {@link Environment} 类型。此方法的结果可用于将现有环境实例转换为正确类型。\n\t * @param webApplicationType Web 应用类型，或 {@code null}\n\t * @return 期望的应用上下文类型，或 {@code null} 以使用默认值\n\t * @since 2.6.14\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link Environment} to be set on the\n\t * {@link #create(WebApplicationType) created} application context. The result of this\n\t * method must match the type returned by\n\t * {@link #getEnvironmentType(WebApplicationType)}.\n\t * @param webApplicationType the web application type or {@code null}\n\t * @return an environment instance or {@code null} to use the default\n\t * @since 2.6.14\n\t */",
            "\t/**\n\t * 创建应设置在 {@link #create(WebApplicationType) 创建的} 应用上下文上的新 {@link Environment}。\n\t * 此方法的结果必须与 {@link #getEnvironmentType(WebApplicationType)} 返回的类型一致。\n\t * @param webApplicationType Web 应用类型，或 {@code null}\n\t * @return 环境实例，或 {@code null} 以使用默认值\n\t * @since 2.6.14\n\t */",
        ),
        (
            "\t/**\n\t * Creates the {@link ConfigurableApplicationContext application context} for a\n\t * {@link SpringApplication}, respecting the given {@code webApplicationType}.\n\t * @param webApplicationType the web application type\n\t * @return the newly created application context\n\t */",
            "\t/**\n\t * 为 {@link SpringApplication} 创建 {@link ConfigurableApplicationContext 应用上下文}，\n\t * 并尊重给定的 {@code webApplicationType}。\n\t * @param webApplicationType Web 应用类型\n\t * @return 新创建的应用上下文\n\t */",
        ),
        (
            "\t/**\n\t * Creates an {@code ApplicationContextFactory} that will create contexts by\n\t * instantiating the given {@code contextClass} through its primary constructor.\n\t * @param contextClass the context class\n\t * @return the factory that will instantiate the context class\n\t * @see BeanUtils#instantiateClass(Class)\n\t */",
            "\t/**\n\t * 创建 {@code ApplicationContextFactory}，通过主构造函数实例化给定 {@code contextClass} 来创建上下文。\n\t * @param contextClass 上下文类\n\t * @return 将实例化上下文类的工厂\n\t * @see BeanUtils#instantiateClass(Class)\n\t */",
        ),
        (
            "\t/**\n\t * Creates an {@code ApplicationContextFactory} that will create contexts by calling\n\t * the given {@link Supplier}.\n\t * @param supplier the context supplier, for example\n\t * {@code AnnotationConfigApplicationContext::new}\n\t * @return the factory that will instantiate the context class\n\t */",
            "\t/**\n\t * 创建 {@code ApplicationContextFactory}，通过调用给定 {@link Supplier} 来创建上下文。\n\t * @param supplier 上下文供应者，例如 {@code AnnotationConfigApplicationContext::new}\n\t * @return 将实例化上下文类的工厂\n\t */",
        ),
    ],
    "ApplicationEnvironment.java": [
        (
            "/**\n * {@link StandardEnvironment} for typical use in a typical {@link SpringApplication}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 在典型 {@link SpringApplication} 中常规使用的 {@link StandardEnvironment}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "ApplicationInfoPropertySource.java": [
        (
            "/**\n * {@link PropertySource} which provides information about the application, like the\n * process ID (PID) or the version.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * 提供应用信息（如进程 ID（PID）或版本）的 {@link PropertySource}。\n *\n * @author Moritz Halbritter\n */",
        ),
        (
            "\t/**\n\t * Moves the {@link ApplicationInfoPropertySource} to the end of the environment's\n\t * property sources.\n\t * @param environment the environment\n\t */",
            "\t/**\n\t * 将 {@link ApplicationInfoPropertySource} 移至环境属性源的末尾。\n\t * @param environment 环境\n\t */",
        ),
    ],
    "ApplicationProperties.java": [
        (
            "/**\n * Spring application properties.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * Spring 应用属性。\n *\n * @author Moritz Halbritter\n */",
        ),
        (
            "\t/**\n\t * Whether bean definition overriding, by registering a definition with the same name\n\t * as an existing definition, is allowed.\n\t */",
            "\t/**\n\t * 是否允许 Bean 定义覆盖（注册与现有定义同名的定义）。\n\t */",
        ),
        (
            "\t/**\n\t * Whether to allow circular references between beans and automatically try to resolve\n\t * them.\n\t */",
            "\t/**\n\t * 是否允许 Bean 之间的循环引用并自动尝试解析。\n\t */",
        ),
        (
            "\t/**\n\t * Mode used to display the banner when the application runs.\n\t */",
            "\t/**\n\t * 应用运行时显示 Banner 的模式。\n\t */",
        ),
        (
            "\t/**\n\t * Whether to keep the application alive even if there are no more non-daemon threads.\n\t */",
            "\t/**\n\t * 即使没有更多非守护线程时是否仍保持应用存活。\n\t */",
        ),
        (
            "\t/**\n\t * Whether initialization should be performed lazily.\n\t */",
            "\t/**\n\t * 是否应延迟执行初始化。\n\t */",
        ),
        (
            "\t/**\n\t * Whether to log information about the application when it starts.\n\t */",
            "\t/**\n\t * 应用启动时是否记录应用信息。\n\t */",
        ),
        (
            "\t/**\n\t * Whether the application should have a shutdown hook registered.\n\t */",
            "\t/**\n\t * 应用是否应注册关闭钩子。\n\t */",
        ),
        (
            "\t/**\n\t * Sources (class names, package names, or XML resource locations) to include in the\n\t * ApplicationContext.\n\t */",
            "\t/**\n\t * 要包含在 ApplicationContext 中的源（类名、包名或 XML 资源位置）。\n\t */",
        ),
        (
            "\t/**\n\t * Flag to explicitly request a specific type of web application. If not set,\n\t * auto-detected based on the classpath.\n\t */",
            "\t/**\n\t * 显式请求特定 Web 应用类型的标志。若未设置，则根据类路径自动检测。\n\t */",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not dst.exists():
            if src.exists():
                dst.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(src, dst)
            else:
                failures.append(f"MISSING analyzed: {rel}")
                continue
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            if has_chinese(text):
                # Already annotated — skip but count as ok
                ok += 1
                print(f"SKIP(already CN) {rel}")
                continue
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
