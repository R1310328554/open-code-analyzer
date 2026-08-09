#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-5b batch files [20:40]."""
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
    "ConfigDataLocationBindHandler.java": [
        (
            "/**\n * {@link BindHandler} to set the {@link Origin} of bound {@link ConfigDataLocation}\n * objects.\n *\n * @author Phillip Webb\n * @author Scott Frederick\n */",
            "/**\n * 为绑定的 {@link ConfigDataLocation} 对象设置 {@link Origin} 的 {@link BindHandler}。\n *\n * @author Phillip Webb\n * @author Scott Frederick\n */",
        ),
    ],
    "ConfigDataLocationNotFoundException.java": [
        (
            "/**\n * {@link ConfigDataNotFoundException} thrown when a {@link ConfigDataLocation} cannot be\n * found.\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 找不到 {@link ConfigDataLocation} 时抛出的 {@link ConfigDataNotFoundException}。\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataLocationNotFoundException} instance.\n\t * @param location the location that could not be found\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataLocationNotFoundException} 实例。\n\t *\n\t * @param location 找不到的配置位置\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataLocationNotFoundException} instance.\n\t * @param location the location that could not be found\n\t * @param cause the exception cause\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataLocationNotFoundException} 实例。\n\t *\n\t * @param location 找不到的配置位置\n\t * @param cause 异常原因\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataLocationNotFoundException} instance.\n\t * @param location the location that could not be found\n\t * @param message the exception message\n\t * @param cause the exception cause\n\t * @since 2.4.7\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataLocationNotFoundException} 实例。\n\t *\n\t * @param location 找不到的配置位置\n\t * @param message 异常消息\n\t * @param cause 异常原因\n\t * @since 2.4.7\n\t */",
        ),
        (
            "/**\n\t * Return the location that could not be found.\n\t * @return the location\n\t */",
            "/**\n\t * 返回找不到的配置位置。\n\t *\n\t * @return 配置位置\n\t */",
        ),
    ],
    "ConfigDataLocationResolver.java": [
        (
            "/**\n * Strategy interface used to resolve {@link ConfigDataLocation locations} into one or\n * more {@link ConfigDataResource resources}. Implementations should be added as a\n * {@code spring.factories} entries. The following constructor parameter types are\n * supported:\n * <ul>\n * <li>{@link DeferredLogFactory} - if the resolver needs deferred logging</li>\n * <li>{@link Binder} - if the resolver needs to obtain values from the initial\n * {@link Environment}</li>\n * <li>{@link ResourceLoader} - if the resolver needs a resource loader</li>\n * <li>{@link ConfigurableBootstrapContext} - A bootstrap context that can be used to\n * store objects that may be expensive to create, or need to be shared\n * ({@link BootstrapContext} or {@link BootstrapRegistry} may also be used).</li>\n * </ul>\n * <p>\n * Resolvers may implement {@link Ordered} or use the {@link Order @Order} annotation. The\n * first resolver that supports the given location will be used.\n *\n * @param <R> the location type\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
            "/**\n * 将 {@link ConfigDataLocation 配置位置} 解析为一个或多个 {@link ConfigDataResource 资源} 的策略接口。\n * 实现类应作为 {@code spring.factories} 条目注册。支持以下构造器参数类型：\n * <ul>\n * <li>{@link DeferredLogFactory} — 解析器需要延迟日志时使用</li>\n * <li>{@link Binder} — 解析器需从初始 {@link Environment} 获取值时使用</li>\n * <li>{@link ResourceLoader} — 解析器需要资源加载器时使用</li>\n * <li>{@link ConfigurableBootstrapContext} — 可用于存储创建成本较高或需共享的对象\n * （也可使用 {@link BootstrapContext} 或 {@link BootstrapRegistry}）</li>\n * </ul>\n * <p>\n * 解析器可实现 {@link Ordered} 或使用 {@link Order @Order} 注解。\n * 将使用第一个支持给定位置的解析器。\n *\n * @param <R> 资源类型\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Returns if the specified location address can be resolved by this resolver.\n\t * @param context the location resolver context\n\t * @param location the location to check.\n\t * @return if the location is supported by this resolver\n\t */",
            "/**\n\t * 返回此解析器是否能解析指定位置地址。\n\t *\n\t * @param context 位置解析器上下文\n\t * @param location 待检查的位置\n\t * @return 此解析器是否支持该位置\n\t */",
        ),
        (
            "/**\n\t * Resolve a {@link ConfigDataLocation} into one or more {@link ConfigDataResource}\n\t * instances.\n\t * @param context the location resolver context\n\t * @param location the location that should be resolved\n\t * @return a list of {@link ConfigDataResource resources} in ascending priority order.\n\t * @throws ConfigDataLocationNotFoundException on a non-optional location that cannot\n\t * be found\n\t * @throws ConfigDataResourceNotFoundException if a resolved resource cannot be found\n\t */",
            "/**\n\t * 将 {@link ConfigDataLocation} 解析为一个或多个 {@link ConfigDataResource} 实例。\n\t *\n\t * @param context 位置解析器上下文\n\t * @param location 待解析的位置\n\t * @return 按优先级升序排列的 {@link ConfigDataResource 资源} 列表\n\t * @throws ConfigDataLocationNotFoundException 非可选位置找不到时抛出\n\t * @throws ConfigDataResourceNotFoundException 解析出的资源找不到时抛出\n\t */",
        ),
        (
            "/**\n\t * Resolve a {@link ConfigDataLocation} into one or more {@link ConfigDataResource}\n\t * instances based on available profiles. This method is called once profiles have\n\t * been deduced from the contributed values. By default this method returns an empty\n\t * list.\n\t * @param context the location resolver context\n\t * @param location the location that should be resolved\n\t * @param profiles profile information\n\t * @return a list of resolved locations in ascending priority order.\n\t * @throws ConfigDataLocationNotFoundException on a non-optional location that cannot\n\t * be found\n\t */",
            "/**\n\t * 根据可用 profile 将 {@link ConfigDataLocation} 解析为一个或多个 {@link ConfigDataResource} 实例。\n\t * 在从已贡献值推断出 profile 后调用。默认返回空列表。\n\t *\n\t * @param context 位置解析器上下文\n\t * @param location 待解析的位置\n\t * @param profiles profile 信息\n\t * @return 按优先级升序排列的已解析位置列表\n\t * @throws ConfigDataLocationNotFoundException 非可选位置找不到时抛出\n\t */",
        ),
    ],
    "ConfigDataLocationResolverContext.java": [
        (
            "/**\n * Context provided to {@link ConfigDataLocationResolver} methods.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
            "/**\n * 提供给 {@link ConfigDataLocationResolver} 方法的上下文。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Provides access to a binder that can be used to obtain previously contributed\n\t * values.\n\t * @return a binder instance\n\t */",
            "/**\n\t * 提供可用于获取先前已贡献值的绑定器。\n\t *\n\t * @return 绑定器实例\n\t */",
        ),
        (
            "/**\n\t * Provides access to the parent {@link ConfigDataResource} that triggered the resolve\n\t * or {@code null} if there is no available parent.\n\t * @return the parent location\n\t */",
            "/**\n\t * 提供触发本次解析的父 {@link ConfigDataResource}；若无可用父资源则返回 {@code null}。\n\t *\n\t * @return 父资源\n\t */",
        ),
        (
            "/**\n\t * Provides access to the {@link ConfigurableBootstrapContext} shared across all\n\t * {@link EnvironmentPostProcessor EnvironmentPostProcessors}.\n\t * @return the bootstrap context\n\t */",
            "/**\n\t * 提供所有 {@link EnvironmentPostProcessor EnvironmentPostProcessor} 共享的\n\t * {@link ConfigurableBootstrapContext}。\n\t *\n\t * @return 引导上下文\n\t */",
        ),
    ],
    "ConfigDataLocationResolvers.java": [
        (
            "/**\n * A collection of {@link ConfigDataLocationResolver} instances loaded via\n * {@code spring.factories}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 通过 {@code spring.factories} 加载的 {@link ConfigDataLocationResolver} 实例集合。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataLocationResolvers} instance.\n\t * @param logFactory a {@link DeferredLogFactory} used to inject {@link Log} instances\n\t * @param bootstrapContext the bootstrap context\n\t * @param binder a binder providing values from the initial {@link Environment}\n\t * @param resourceLoader {@link ResourceLoader} to load resource locations\n\t * @param springFactoriesLoader to load {@link ConfigDataLocationResolver} instances\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataLocationResolvers} 实例。\n\t *\n\t * @param logFactory 用于注入 {@link Log} 实例的 {@link DeferredLogFactory}\n\t * @param bootstrapContext 引导上下文\n\t * @param binder 从初始 {@link Environment} 提供值的绑定器\n\t * @param resourceLoader 加载资源位置的 {@link ResourceLoader}\n\t * @param springFactoriesLoader 加载 {@link ConfigDataLocationResolver} 实例的加载器\n\t */",
        ),
        (
            "/**\n\t * Return the resolvers managed by this object.\n\t * @return the resolvers\n\t */",
            "/**\n\t * 返回此对象管理的解析器。\n\t *\n\t * @return 解析器列表\n\t */",
        ),
    ],
    "ConfigDataLocationRuntimeHints.java": [
        (
            "/**\n * {@link RuntimeHintsRegistrar} implementation for application configuration.\n *\n * @author Stephane Nicoll\n * @see FilePatternResourceHintsRegistrar\n */",
            "/**\n * 应用配置的 {@link RuntimeHintsRegistrar} 实现。\n *\n * @author Stephane Nicoll\n * @see FilePatternResourceHintsRegistrar\n */",
        ),
        (
            "/**\n\t * Get the application file names to consider.\n\t * @param classLoader the classloader to use\n\t * @return the configuration file names\n\t */",
            "/**\n\t * 获取需考虑的应用文件名。\n\t *\n\t * @param classLoader 要使用的类加载器\n\t * @return 配置文件名列表\n\t */",
        ),
        (
            "/**\n\t * Get the locations to consider. A location is a classpath location that may or may\n\t * not use the standard {@code classpath:} prefix.\n\t * @param classLoader the classloader to use\n\t * @return the configuration file locations\n\t */",
            "/**\n\t * 获取需考虑的位置。位置为类路径位置，可使用或不使用标准 {@code classpath:} 前缀。\n\t *\n\t * @param classLoader 要使用的类加载器\n\t * @return 配置文件位置列表\n\t */",
        ),
        (
            "/**\n\t * Get the application file extensions to consider. A valid extension starts with a\n\t * dot.\n\t * @param classLoader the classloader to use\n\t * @return the configuration file extensions\n\t */",
            "/**\n\t * 获取需考虑的应用文件扩展名。有效扩展名以点号开头。\n\t *\n\t * @param classLoader 要使用的类加载器\n\t * @return 配置文件扩展名列表\n\t */",
        ),
    ],
    "ConfigDataNotFoundAction.java": [
        (
            "/**\n * Action to take when an uncaught {@link ConfigDataNotFoundException} is thrown.\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 未捕获 {@link ConfigDataNotFoundException} 时采取的操作。\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Throw the exception to fail startup.\n\t */",
            "/**\n\t * 抛出异常以使启动失败。\n\t */",
        ),
        (
            "/**\n\t * Ignore the exception and continue processing the remaining locations.\n\t */",
            "/**\n\t * 忽略异常并继续处理剩余位置。\n\t */",
        ),
        (
            "/**\n\t * Handle the given exception.\n\t * @param logger the logger used for output {@code ConfigDataLocation})\n\t * @param ex the exception to handle\n\t */",
            "/**\n\t * 处理给定异常。\n\t *\n\t * @param logger 用于输出的日志记录器\n\t * @param ex 待处理的异常\n\t */",
        ),
    ],
    "ConfigDataNotFoundException.java": [
        (
            "/**\n * {@link ConfigDataNotFoundException} thrown when a {@link ConfigData} cannot be found.\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 找不到 {@link ConfigData} 时抛出的 {@link ConfigDataNotFoundException}。\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataNotFoundException} instance.\n\t * @param message the exception message\n\t * @param cause the exception cause\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataNotFoundException} 实例。\n\t *\n\t * @param message 异常消息\n\t * @param cause 异常原因\n\t */",
        ),
        (
            "/**\n\t * Return a description of actual referenced item that could not be found.\n\t * @return a description of the referenced items\n\t */",
            "/**\n\t * 返回找不到的实际引用项的描述。\n\t *\n\t * @return 引用项描述\n\t */",
        ),
    ],
    "ConfigDataNotFoundFailureAnalyzer.java": [
        (
            "/**\n * An implementation of {@link AbstractFailureAnalyzer} to analyze failures caused by\n * {@link ConfigDataNotFoundException}.\n *\n * @author Michal Mlak\n * @author Phillip Webb\n */",
            "/**\n * 分析 {@link ConfigDataNotFoundException} 导致失败的 {@link AbstractFailureAnalyzer} 实现。\n *\n * @author Michal Mlak\n * @author Phillip Webb\n */",
        ),
    ],
    "ConfigDataProperties.java": [
        (
            "/**\n * Bound properties used when working with {@link ConfigData}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Yanming Zhou\n */",
            "/**\n * 处理 {@link ConfigData} 时使用的绑定属性。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Yanming Zhou\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataProperties} instance.\n\t * @param imports the imports requested\n\t * @param activate the activate properties\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataProperties} 实例。\n\t *\n\t * @param imports 请求的导入项\n\t * @param activate 激活属性\n\t */",
        ),
        (
            "/**\n\t * Return any additional imports requested.\n\t * @return the requested imports\n\t */",
            "/**\n\t * 返回请求的额外导入项。\n\t *\n\t * @return 请求的导入项\n\t */",
        ),
        (
            "/**\n\t * Return {@code true} if the properties indicate that the config data property source\n\t * is active for the given activation context.\n\t * @param activationContext the activation context\n\t * @return {@code true} if the config data property source is active\n\t */",
            "/**\n\t * 若属性表明配置数据属性源在给定激活上下文中处于活动状态则返回 {@code true}。\n\t *\n\t * @param activationContext 激活上下文\n\t * @return 配置数据属性源是否活动\n\t */",
        ),
        (
            "/**\n\t * Return a new variant of these properties without any imports.\n\t * @return a new {@link ConfigDataProperties} instance\n\t */",
            "/**\n\t * 返回不含任何导入项的此属性新副本。\n\t *\n\t * @return 新的 {@link ConfigDataProperties} 实例\n\t */",
        ),
        (
            "/**\n\t * Factory method used to create {@link ConfigDataProperties} from the given\n\t * {@link Binder}.\n\t * @param binder the binder used to bind the properties\n\t * @return a {@link ConfigDataProperties} instance or {@code null}\n\t */",
            "/**\n\t * 从给定 {@link Binder} 创建 {@link ConfigDataProperties} 的工厂方法。\n\t *\n\t * @param binder 用于绑定属性的绑定器\n\t * @return {@link ConfigDataProperties} 实例，或 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Activate properties used to determine when a config data property source is active.\n\t */",
            "/**\n\t * 用于判定配置数据属性源何时激活的激活属性。\n\t */",
        ),
        (
            "/**\n\t\t * Create a new {@link Activate} instance.\n\t\t * @param onCloudPlatform the cloud platform required for activation\n\t\t * @param onProfile the profile expression required for activation\n\t\t */",
            "/**\n\t\t * 创建新的 {@link Activate} 实例。\n\t\t *\n\t\t * @param onCloudPlatform 激活所需的云平台\n\t\t * @param onProfile 激活所需的 profile 表达式\n\t\t */",
        ),
        (
            "/**\n\t\t * Return {@code true} if the properties indicate that the config data property\n\t\t * source is active for the given activation context.\n\t\t * @param activationContext the activation context\n\t\t * @return {@code true} if the config data property source is active\n\t\t */",
            "/**\n\t\t * 若属性表明配置数据属性源在给定激活上下文中处于活动状态则返回 {@code true}。\n\t\t *\n\t\t * @param activationContext 激活上下文\n\t\t * @return 配置数据属性源是否活动\n\t\t */",
        ),
    ],
    "ConfigDataPropertiesRuntimeHints.java": [
        (
            "/**\n * {@link RuntimeHintsRegistrar} for {@link ConfigDataProperties}.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * {@link ConfigDataProperties} 的 {@link RuntimeHintsRegistrar}。\n *\n * @author Moritz Halbritter\n */",
        ),
    ],
    "ConfigDataResolutionResult.java": [
        (
            "/**\n * Result returned from {@link ConfigDataLocationResolvers} containing both the\n * {@link ConfigDataResource} and the original {@link ConfigDataLocation}.\n *\n * @author Phillip Webb\n */",
            "/**\n * {@link ConfigDataLocationResolvers} 返回的结果，包含 {@link ConfigDataResource}\n * 与原始 {@link ConfigDataLocation}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "ConfigDataResource.java": [
        (
            "/**\n * A single resource from which {@link ConfigData} can be loaded. Implementations must\n * implement a valid {@link #equals(Object) equals}, {@link #hashCode() hashCode} and\n * {@link #toString() toString} methods.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
            "/**\n * 可从中加载 {@link ConfigData} 的单个资源。实现类必须提供有效的\n * {@link #equals(Object) equals}、{@link #hashCode() hashCode} 与 {@link #toString() toString} 方法。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Create a new non-optional {@link ConfigDataResource} instance.\n\t */",
            "/**\n\t * 创建新的非可选 {@link ConfigDataResource} 实例。\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataResource} instance.\n\t * @param optional if the resource is optional\n\t * @since 2.4.6\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataResource} 实例。\n\t *\n\t * @param optional 资源是否可选\n\t * @since 2.4.6\n\t */",
        ),
    ],
    "ConfigDataResourceNotFoundException.java": [
        (
            "/**\n * {@link ConfigDataNotFoundException} thrown when a {@link ConfigDataResource} cannot be\n * found.\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 找不到 {@link ConfigDataResource} 时抛出的 {@link ConfigDataNotFoundException}。\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataResourceNotFoundException} instance.\n\t * @param resource the resource that could not be found\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataResourceNotFoundException} 实例。\n\t *\n\t * @param resource 找不到的配置资源\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataResourceNotFoundException} instance.\n\t * @param resource the resource that could not be found\n\t * @param cause the exception cause\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataResourceNotFoundException} 实例。\n\t *\n\t * @param resource 找不到的配置资源\n\t * @param cause 异常原因\n\t */",
        ),
        (
            "/**\n\t * Return the resource that could not be found.\n\t * @return the resource\n\t */",
            "/**\n\t * 返回找不到的配置资源。\n\t *\n\t * @return 配置资源\n\t */",
        ),
        (
            "/**\n\t * Return the original location that was resolved to determine the resource.\n\t * @return the location or {@code null} if no location is available\n\t */",
            "/**\n\t * 返回解析以确定该资源的原始位置。\n\t *\n\t * @return 配置位置；若无可用位置则为 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataResourceNotFoundException} instance with a location.\n\t * @param location the location to set\n\t * @return a new {@link ConfigDataResourceNotFoundException} instance\n\t */",
            "/**\n\t * 创建带位置信息的新 {@link ConfigDataResourceNotFoundException} 实例。\n\t *\n\t * @param location 要设置的位置\n\t * @return 新的 {@link ConfigDataResourceNotFoundException} 实例\n\t */",
        ),
        (
            "/**\n\t * Throw a {@link ConfigDataNotFoundException} if the specified {@link Path} does not\n\t * exist.\n\t * @param resource the config data resource\n\t * @param pathToCheck the path to check\n\t */",
            "/**\n\t * 若指定 {@link Path} 不存在则抛出 {@link ConfigDataNotFoundException}。\n\t *\n\t * @param resource 配置数据资源\n\t * @param pathToCheck 待检查的路径\n\t */",
        ),
        (
            "/**\n\t * Throw a {@link ConfigDataNotFoundException} if the specified {@link File} does not\n\t * exist.\n\t * @param resource the config data resource\n\t * @param fileToCheck the file to check\n\t */",
            "/**\n\t * 若指定 {@link File} 不存在则抛出 {@link ConfigDataNotFoundException}。\n\t *\n\t * @param resource 配置数据资源\n\t * @param fileToCheck 待检查的文件\n\t */",
        ),
        (
            "/**\n\t * Throw a {@link ConfigDataNotFoundException} if the specified {@link Resource} does\n\t * not exist.\n\t * @param resource the config data resource\n\t * @param resourceToCheck the resource to check\n\t */",
            "/**\n\t * 若指定 {@link Resource} 不存在则抛出 {@link ConfigDataNotFoundException}。\n\t *\n\t * @param resource 配置数据资源\n\t * @param resourceToCheck 待检查的资源\n\t */",
        ),
    ],
    "ConfigTreeConfigDataLoader.java": [
        (
            "/**\n * {@link ConfigDataLoader} for config tree locations.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 用于 config tree 位置的 {@link ConfigDataLoader}。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
    ],
    "ConfigTreeConfigDataLocationResolver.java": [
        (
            "/**\n * {@link ConfigDataLocationResolver} for config tree locations.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 用于 config tree 位置的 {@link ConfigDataLocationResolver}。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
    ],
    "ConfigTreeConfigDataResource.java": [
        (
            "/**\n * {@link ConfigDataResource} backed by a config tree directory.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.4.0\n * @see ConfigTreePropertySource\n */",
            "/**\n * 由 config tree 目录支持的 {@link ConfigDataResource}。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.4.0\n * @see ConfigTreePropertySource\n */",
        ),
    ],
    "FileHint.java": [
        (
            "/**\n * User-provided hint for file attributes, like extension or encoding.\n *\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
            "/**\n * 用户提供的文件属性提示，如扩展名或编码。\n *\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
        ),
        (
            "/**\n\t * Return {@code true} if the hint is present.\n\t * @return if the hint is present\n\t */",
            "/**\n\t * 若存在提示则返回 {@code true}。\n\t *\n\t * @return 是否存在提示\n\t */",
        ),
        (
            "/**\n\t * Returns the extension.\n\t * @return the extension\n\t */",
            "/**\n\t * 返回扩展名。\n\t *\n\t * @return 扩展名\n\t */",
        ),
        (
            "/**\n\t * Return the extension from the hint or return the {@code fallback} if the extension\n\t * is not present.\n\t * @param fallback the fallback extension\n\t * @return the extension either from the hint or fallback\n\t */",
            "/**\n\t * 返回提示中的扩展名；若不存在则返回 {@code fallback}。\n\t *\n\t * @param fallback 回退扩展名\n\t * @return 提示或回退扩展名\n\t */",
        ),
        (
            "/**\n\t * Returns the encoding.\n\t * @return the encoding\n\t */",
            "/**\n\t * 返回编码。\n\t *\n\t * @return 编码\n\t */",
        ),
        (
            "/**\n\t * Returns the encoding as a {@link Charset}.\n\t * @return the encoding as a {@link Charset}\n\t */",
            "/**\n\t * 以 {@link Charset} 形式返回编码。\n\t *\n\t * @return 编码对应的 {@link Charset}\n\t */",
        ),
        (
            "/**\n\t * Return the {@link FileHint} from the given value.\n\t * @param value the source value\n\t * @return the {@link FileHint}\n\t */",
            "/**\n\t * 从给定值解析 {@link FileHint}。\n\t *\n\t * @param value 源值\n\t * @return {@link FileHint}\n\t */",
        ),
        (
            "/**\n\t * Remove any hint from the given value.\n\t * @param value the source value\n\t * @return the value without any hint\n\t */",
            "/**\n\t * 从给定值中移除所有提示。\n\t *\n\t * @param value 源值\n\t * @return 不含提示的值\n\t */",
        ),
    ],
    "InactiveConfigDataAccessException.java": [
        (
            "/**\n * Exception thrown when an attempt is made to resolve a property against an inactive\n * {@link ConfigData} property source. Used to ensure that a user doesn't accidentally\n * attempt to specify a properties that can never be resolved.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
            "/**\n * 尝试针对非活动的 {@link ConfigData} 属性源解析属性时抛出的异常。\n * 用于防止用户误指定永远无法解析的属性。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link InactiveConfigDataAccessException} instance.\n\t * @param propertySource the inactive property source\n\t * @param location the {@link ConfigDataResource} of the property source or\n\t * {@code null} if the source was not loaded from {@link ConfigData}.\n\t * @param propertyName the name of the property\n\t * @param origin the origin or the property or {@code null}\n\t */",
            "/**\n\t * 创建新的 {@link InactiveConfigDataAccessException} 实例。\n\t *\n\t * @param propertySource 非活动的属性源\n\t * @param location 属性源的 {@link ConfigDataResource}；若源非来自 {@link ConfigData} 则为 {@code null}\n\t * @param propertyName 属性名\n\t * @param origin 属性来源，或 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Return the inactive property source that contained the property.\n\t * @return the property source\n\t */",
            "/**\n\t * 返回包含该属性的非活动属性源。\n\t *\n\t * @return 属性源\n\t */",
        ),
        (
            "/**\n\t * Return the {@link ConfigDataResource} of the property source or {@code null} if the\n\t * source was not loaded from {@link ConfigData}.\n\t * @return the config data location or {@code null}\n\t */",
            "/**\n\t * 返回属性源的 {@link ConfigDataResource}；若源非来自 {@link ConfigData} 则为 {@code null}。\n\t *\n\t * @return 配置数据位置，或 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Return the name of the property.\n\t * @return the property name\n\t */",
            "/**\n\t * 返回属性名。\n\t *\n\t * @return 属性名\n\t */",
        ),
        (
            "/**\n\t * Return the origin or the property or {@code null}.\n\t * @return the property origin\n\t */",
            "/**\n\t * 返回属性来源，或 {@code null}。\n\t *\n\t * @return 属性来源\n\t */",
        ),
        (
            "/**\n\t * Throw an {@link InactiveConfigDataAccessException} if the given\n\t * {@link ConfigDataEnvironmentContributor} contains the property.\n\t * @param contributor the contributor to check\n\t * @param name the name to check\n\t */",
            "/**\n\t * 若给定 {@link ConfigDataEnvironmentContributor} 包含该属性则抛出\n\t * {@link InactiveConfigDataAccessException}。\n\t *\n\t * @param contributor 待检查的 contributor\n\t * @param name 待检查的属性名\n\t */",
        ),
    ],
    "InvalidConfigDataPropertyException.java": [
        (
            "/**\n * Exception thrown if an invalid property is found when processing config data.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
            "/**\n * 处理配置数据时发现无效属性时抛出的异常。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Return source property that caused the exception.\n\t * @return the invalid property\n\t */",
            "/**\n\t * 返回导致异常的源属性。\n\t *\n\t * @return 无效属性\n\t */",
        ),
        (
            "/**\n\t * Return the {@link ConfigDataResource} of the invalid property or {@code null} if\n\t * the source was not loaded from {@link ConfigData}.\n\t * @return the config data location or {@code null}\n\t */",
            "/**\n\t * 返回无效属性的 {@link ConfigDataResource}；若源非来自 {@link ConfigData} 则为 {@code null}。\n\t *\n\t * @return 配置数据位置，或 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Return the replacement property that should be used instead or {@code null} if not\n\t * replacement is available.\n\t * @return the replacement property name\n\t */",
            "/**\n\t * 返回应使用的替代属性名；若无可用替代项则为 {@code null}。\n\t *\n\t * @return 替代属性名\n\t */",
        ),
        (
            "/**\n\t * Throw an {@link InvalidConfigDataPropertyException} if the given\n\t * {@link ConfigDataEnvironmentContributor} contains any invalid property.\n\t * @param contributor the contributor to check\n\t */",
            "/**\n\t * 若给定 {@link ConfigDataEnvironmentContributor} 包含任何无效属性则抛出\n\t * {@link InvalidConfigDataPropertyException}。\n\t *\n\t * @param contributor 待检查的 contributor\n\t */",
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
