#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-6a slice [0:20]."""
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
    "LocationResourceLoader.java": [
        (
            "/**\n * Strategy interface for loading resources from a location. Supports single resource and\n * simple wildcard directory patterns.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 从位置加载资源的策略接口，支持单个资源及简单通配符目录模式。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
        (
            "/**\n\t * Create a new {@link LocationResourceLoader} instance.\n\t * @param resourceLoader the underlying resource loader\n\t */",
            "/**\n\t * 创建新的 {@link LocationResourceLoader} 实例。\n\t *\n\t * @param resourceLoader 底层资源加载器\n\t */",
        ),
        (
            "/**\n\t * Returns if the location contains a pattern.\n\t * @param location the location to check\n\t * @return if the location is a pattern\n\t */",
            "/**\n\t * 判断位置是否包含通配符模式。\n\t *\n\t * @param location 要检查的位置\n\t * @return 位置是否为模式\n\t */",
        ),
        (
            "/**\n\t * Get a single resource from a non-pattern location.\n\t * @param location the location\n\t * @return the resource\n\t * @see #isPattern(String)\n\t */",
            "/**\n\t * 从非模式位置获取单个资源。\n\t *\n\t * @param location 位置\n\t * @return 资源\n\t * @see #isPattern(String)\n\t */",
        ),
        (
            "/**\n\t * Get a multiple resources from a location pattern.\n\t * @param location the location pattern\n\t * @param type the type of resource to return\n\t * @return the resources\n\t * @see #isPattern(String)\n\t */",
            "/**\n\t * 从位置模式获取多个资源。\n\t *\n\t * @param location 位置模式\n\t * @param type 要返回的资源类型\n\t * @return 资源数组\n\t * @see #isPattern(String)\n\t */",
        ),
        (
            "/**\n\t * Resource types that can be returned.\n\t */",
            "/**\n\t * 可返回的资源类型。\n\t */",
        ),
        (
            "/**\n\t\t * Return file resources.\n\t\t */",
            "/**\n\t\t * 返回文件资源。\n\t\t */",
        ),
        (
            "/**\n\t\t * Return directory resources.\n\t\t */",
            "/**\n\t\t * 返回目录资源。\n\t\t */",
        ),
    ],
    "Profiles.java": [
        (
            "/**\n * Provides access to environment profiles that have either been set directly on the\n * {@link Environment} or will be set based on configuration data property values.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
            "/**\n * 提供对环境 profile 的访问，这些 profile 可直接设置在 {@link Environment} 上，\n * 或基于配置数据属性值确定。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Name of property to set to specify additionally included active profiles.\n\t */",
            "/**\n\t * 用于指定额外包含的活动 profile 的属性名。\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link Profiles} instance based on the {@link Environment} and\n\t * {@link Binder}.\n\t * @param environment the source environment\n\t * @param binder the binder for profile properties\n\t * @param additionalProfiles any additional active profiles\n\t */",
            "/**\n\t * 基于 {@link Environment} 与 {@link Binder} 创建新的 {@link Profiles} 实例。\n\t *\n\t * @param environment 源环境\n\t * @param binder 用于 profile 属性的绑定器\n\t * @param additionalProfiles 额外的活动 profile\n\t */",
        ),
        (
            "/**\n\t * Return an iterator for all {@link #getAccepted() accepted profiles}.\n\t */",
            "/**\n\t * 返回所有 {@link #getAccepted() 已接受 profile} 的迭代器。\n\t */",
        ),
        (
            "/**\n\t * Return the active profiles.\n\t * @return the active profiles\n\t */",
            "/**\n\t * 返回活动 profile。\n\t *\n\t * @return 活动 profile\n\t */",
        ),
        (
            "/**\n\t * Return the default profiles.\n\t * @return the active profiles\n\t */",
            "/**\n\t * 返回默认 profile。\n\t *\n\t * @return 默认 profile\n\t */",
        ),
        (
            "/**\n\t * Return the accepted profiles.\n\t * @return the accepted profiles\n\t */",
            "/**\n\t * 返回已接受的 profile。\n\t *\n\t * @return 已接受的 profile\n\t */",
        ),
        (
            "/**\n\t * Return if the given profile is active.\n\t * @param profile the profile to test\n\t * @return if the profile is active\n\t */",
            "/**\n\t * 判断给定 profile 是否已接受。\n\t *\n\t * @param profile 要测试的 profile\n\t * @return profile 是否已接受\n\t */",
        ),
        (
            "/**\n\t * A profiles type that can be obtained.\n\t */",
            "/**\n\t * 可获取的 profile 类型。\n\t */",
        ),
    ],
    "ProfilesValidator.java": [
        (
            "/**\n * {@link BindHandler} that validates profile names.\n *\n * @author Sijun Yang\n * @author Phillip Webb\n */",
            "/**\n * 校验 profile 名称的 {@link BindHandler}。\n *\n * @author Sijun Yang\n * @author Phillip Webb\n */",
        ),
    ],
    "StandardConfigDataLoader.java": [
        (
            "/**\n * {@link ConfigDataLoader} for {@link Resource} backed locations.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
            "/**\n * 用于 {@link Resource} 支持位置的 {@link ConfigDataLoader}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
        ),
    ],
    "StandardConfigDataLocationResolver.java": [
        (
            "/**\n * {@link ConfigDataLocationResolver} for standard locations.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @author Scott Frederick\n * @author Sijun Yang\n * @since 2.4.0\n */",
            "/**\n * 用于标准位置的 {@link ConfigDataLocationResolver}。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @author Scott Frederick\n * @author Sijun Yang\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link StandardConfigDataLocationResolver} instance.\n\t * @param logFactory the factory for loggers to use\n\t * @param binder a binder backed by the initial {@link Environment}\n\t * @param resourceLoader a {@link ResourceLoader} used to load resources\n\t */",
            "/**\n\t * 创建新的 {@link StandardConfigDataLocationResolver} 实例。\n\t *\n\t * @param logFactory 日志工厂\n\t * @param binder 由初始 {@link Environment} 支持的绑定器\n\t * @param resourceLoader 用于加载资源的 {@link ResourceLoader}\n\t */",
        ),
    ],
    "StandardConfigDataReference.java": [
        (
            "/**\n * A reference expanded from the original {@link ConfigDataLocation} that can ultimately\n * be resolved to one or more {@link StandardConfigDataResource resources}.\n *\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
            "/**\n * 由原始 {@link ConfigDataLocation} 展开、最终可解析为一个或多个\n * {@link StandardConfigDataResource 资源} 的引用。\n *\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
        ),
        (
            "/**\n\t * Create a new {@link StandardConfigDataReference} instance.\n\t * @param configDataLocation the original location passed to the resolver\n\t * @param directory the directory of the resource or {@code null} if the reference is\n\t * to a file\n\t * @param root the root of the resource location\n\t * @param profile the profile being loaded\n\t * @param extension the file extension for the resource\n\t * @param propertySourceLoader the property source loader that should be used for this\n\t * reference\n\t * @param encoding the encoding of the resource\n\t */",
            "/**\n\t * 创建新的 {@link StandardConfigDataReference} 实例。\n\t *\n\t * @param configDataLocation 传给解析器的原始位置\n\t * @param directory 资源目录；引用指向文件时为 {@code null}\n\t * @param root 资源位置根路径\n\t * @param profile 正在加载的 profile\n\t * @param extension 资源文件扩展名\n\t * @param propertySourceLoader 本引用应使用的属性源加载器\n\t * @param encoding 资源编码\n\t */",
        ),
    ],
    "StandardConfigDataResource.java": [
        (
            "/**\n * {@link ConfigDataResource} backed by a {@link Resource}.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 由 {@link Resource} 支持的 {@link ConfigDataResource}。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link StandardConfigDataResource} instance.\n\t * @param reference the resource reference\n\t * @param resource the underlying resource\n\t */",
            "/**\n\t * 创建新的 {@link StandardConfigDataResource} 实例。\n\t *\n\t * @param reference 资源引用\n\t * @param resource 底层资源\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link StandardConfigDataResource} instance.\n\t * @param reference the resource reference\n\t * @param resource the underlying resource\n\t * @param emptyDirectory if the resource is an empty directory that we know exists\n\t */",
            "/**\n\t * 创建新的 {@link StandardConfigDataResource} 实例。\n\t *\n\t * @param reference 资源引用\n\t * @param resource 底层资源\n\t * @param emptyDirectory 资源是否为已知存在的空目录\n\t */",
        ),
        (
            "/**\n\t * Return the underlying Spring {@link Resource} being loaded.\n\t * @return the underlying resource\n\t * @since 2.4.2\n\t */",
            "/**\n\t * 返回正在加载的底层 Spring {@link Resource}。\n\t *\n\t * @return 底层资源\n\t * @since 2.4.2\n\t */",
        ),
        (
            "/**\n\t * Return the profile or {@code null} if the resource is not profile specific.\n\t * @return the profile or {@code null}\n\t * @since 2.4.6\n\t */",
            "/**\n\t * 返回 profile；资源非 profile 特定时为 {@code null}。\n\t *\n\t * @return profile，或 {@code null}\n\t * @since 2.4.6\n\t */",
        ),
    ],
    "SystemEnvironmentConfigDataLoader.java": [
        (
            "/**\n * {@link ConfigDataLoader} to load data from system environment variables.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * 从系统环境变量加载数据的 {@link ConfigDataLoader}。\n *\n * @author Moritz Halbritter\n */",
        ),
    ],
    "SystemEnvironmentConfigDataLocationResolver.java": [
        (
            "/**\n * {@link ConfigDataLocationResolver} to resolve {@code env:} locations.\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
            "/**\n * 解析 {@code env:} 位置的 {@link ConfigDataLocationResolver}。\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
        ),
    ],
    "SystemEnvironmentConfigDataResource.java": [
        (
            "/**\n * {@link ConfigDataResource} used by {@link SystemEnvironmentConfigDataLoader}.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * {@link SystemEnvironmentConfigDataLoader} 使用的 {@link ConfigDataResource}。\n *\n * @author Moritz Halbritter\n */",
        ),
    ],
    "UnsupportedConfigDataLocationException.java": [
        (
            "/**\n * Exception throw if a {@link ConfigDataLocation} is not supported.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
            "/**\n * 当 {@link ConfigDataLocation} 不受支持时抛出的异常。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link UnsupportedConfigDataLocationException} instance.\n\t * @param location the unsupported location\n\t */",
            "/**\n\t * 创建新的 {@link UnsupportedConfigDataLocationException} 实例。\n\t *\n\t * @param location 不受支持的位置\n\t */",
        ),
        (
            "/**\n\t * Return the unsupported location reference.\n\t * @return the unsupported location reference\n\t */",
            "/**\n\t * 返回不受支持的位置引用。\n\t *\n\t * @return 不受支持的位置引用\n\t */",
        ),
    ],
    "ApplicationContextInitializedEvent.java": [
        (
            "/**\n * Event published when a {@link SpringApplication} is starting up and the\n * {@link ApplicationContext} is prepared and ApplicationContextInitializers have been\n * called but before any bean definitions are loaded.\n *\n * @author Artsiom Yudovin\n * @since 2.1.0\n */",
            "/**\n * 在 {@link SpringApplication} 启动过程中，{@link ApplicationContext} 已准备就绪且\n * ApplicationContextInitializer 已调用、但尚未加载任何 Bean 定义时发布的事件。\n *\n * @author Artsiom Yudovin\n * @since 2.1.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link ApplicationContextInitializedEvent} instance.\n\t * @param application the current application\n\t * @param args the arguments the application is running with\n\t * @param context the context that has been initialized\n\t */",
            "/**\n\t * 创建新的 {@link ApplicationContextInitializedEvent} 实例。\n\t *\n\t * @param application 当前应用\n\t * @param args 应用运行参数\n\t * @param context 已初始化的上下文\n\t */",
        ),
        (
            "/**\n\t * Return the application context.\n\t * @return the context\n\t */",
            "/**\n\t * 返回应用上下文。\n\t *\n\t * @return 上下文\n\t */",
        ),
    ],
    "ApplicationEnvironmentPreparedEvent.java": [
        (
            "/**\n * Event published when a {@link SpringApplication} is starting up and the\n * {@link Environment} is first available for inspection and modification.\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
            "/**\n * 在 {@link SpringApplication} 启动过程中，{@link Environment} 首次可供检查与修改时发布的事件。\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link ApplicationEnvironmentPreparedEvent} instance.\n\t * @param bootstrapContext the bootstrap context\n\t * @param application the current application\n\t * @param args the arguments the application is running with\n\t * @param environment the environment that was just created\n\t */",
            "/**\n\t * 创建新的 {@link ApplicationEnvironmentPreparedEvent} 实例。\n\t *\n\t * @param bootstrapContext 引导上下文\n\t * @param application 当前应用\n\t * @param args 应用运行参数\n\t * @param environment 刚创建的环境\n\t */",
        ),
        (
            "/**\n\t * Return the bootstrap context.\n\t * @return the bootstrap context\n\t * @since 2.4.0\n\t */",
            "/**\n\t * 返回引导上下文。\n\t *\n\t * @return 引导上下文\n\t * @since 2.4.0\n\t */",
        ),
        (
            "/**\n\t * Return the environment.\n\t * @return the environment\n\t */",
            "/**\n\t * 返回环境。\n\t *\n\t * @return 环境\n\t */",
        ),
    ],
    "ApplicationFailedEvent.java": [
        (
            "/**\n * Event published by a {@link SpringApplication} when it fails to start.\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see ApplicationReadyEvent\n */",
            "/**\n * {@link SpringApplication} 启动失败时发布的事件。\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see ApplicationReadyEvent\n */",
        ),
        (
            "/**\n\t * Create a new {@link ApplicationFailedEvent} instance.\n\t * @param application the current application\n\t * @param args the arguments the application was running with\n\t * @param context the context that was being created (maybe null)\n\t * @param exception the exception that caused the error\n\t */",
            "/**\n\t * 创建新的 {@link ApplicationFailedEvent} 实例。\n\t *\n\t * @param application 当前应用\n\t * @param args 应用运行参数\n\t * @param context 正在创建的上下文（可能为 null）\n\t * @param exception 导致失败的异常\n\t */",
        ),
        (
            "/**\n\t * Return the application context.\n\t * @return the context or {@code null}\n\t */",
            "/**\n\t * 返回应用上下文。\n\t *\n\t * @return 上下文，或 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Return the exception that caused the failure.\n\t * @return the exception\n\t */",
            "/**\n\t * 返回导致失败的异常。\n\t *\n\t * @return 异常\n\t */",
        ),
    ],
    "ApplicationPreparedEvent.java": [
        (
            "/**\n * Event published as when a {@link SpringApplication} is starting up and the\n * {@link ApplicationContext} is fully prepared but not refreshed. The bean definitions\n * will be loaded and the {@link Environment} is ready for use at this stage.\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
            "/**\n * 在 {@link SpringApplication} 启动过程中，{@link ApplicationContext} 已完全准备就绪但尚未刷新时发布的事件。\n * 此阶段 Bean 定义已加载，{@link Environment} 可供使用。\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link ApplicationPreparedEvent} instance.\n\t * @param application the current application\n\t * @param args the arguments the application is running with\n\t * @param context the ApplicationContext about to be refreshed\n\t */",
            "/**\n\t * 创建新的 {@link ApplicationPreparedEvent} 实例。\n\t *\n\t * @param application 当前应用\n\t * @param args 应用运行参数\n\t * @param context 即将刷新的 ApplicationContext\n\t */",
        ),
        (
            "/**\n\t * Return the application context.\n\t * @return the context\n\t */",
            "/**\n\t * 返回应用上下文。\n\t *\n\t * @return 上下文\n\t */",
        ),
    ],
    "ApplicationReadyEvent.java": [
        (
            "/**\n * Event published as late as conceivably possible to indicate that the application is\n * ready to service requests. The source of the event is the {@link SpringApplication}\n * itself, but beware of modifying its internal state since all initialization steps will\n * have been completed by then.\n *\n * @author Stephane Nicoll\n * @author Chris Bono\n * @since 1.3.0\n * @see ApplicationFailedEvent\n */",
            "/**\n * 尽可能晚地发布、表示应用已准备好处理请求的事件。\n * 事件源为 {@link SpringApplication} 本身，但此时所有初始化步骤均已完成，\n * 应避免修改其内部状态。\n *\n * @author Stephane Nicoll\n * @author Chris Bono\n * @since 1.3.0\n * @see ApplicationFailedEvent\n */",
        ),
        (
            "/**\n\t * Create a new {@link ApplicationReadyEvent} instance.\n\t * @param application the current application\n\t * @param args the arguments the application is running with\n\t * @param context the context that was being created\n\t * @param timeTaken the time taken to get the application ready to service requests\n\t * @since 2.6.0\n\t */",
            "/**\n\t * 创建新的 {@link ApplicationReadyEvent} 实例。\n\t *\n\t * @param application 当前应用\n\t * @param args 应用运行参数\n\t * @param context 正在创建的上下文\n\t * @param timeTaken 应用就绪所耗时间\n\t * @since 2.6.0\n\t */",
        ),
        (
            "/**\n\t * Return the application context.\n\t * @return the context\n\t */",
            "/**\n\t * 返回应用上下文。\n\t *\n\t * @return 上下文\n\t */",
        ),
        (
            "/**\n\t * Return the time taken for the application to be ready to service requests, or\n\t * {@code null} if unknown.\n\t * @return the time taken to be ready to service requests\n\t * @since 2.6.0\n\t */",
            "/**\n\t * 返回应用就绪所耗时间；未知时为 {@code null}。\n\t *\n\t * @return 就绪所耗时间\n\t * @since 2.6.0\n\t */",
        ),
    ],
    "ApplicationStartedEvent.java": [
        (
            "/**\n * Event published once the application context has been refreshed but before any\n * {@link ApplicationRunner application} and {@link CommandLineRunner command line}\n * runners have been called.\n *\n * @author Andy Wilkinson\n * @since 2.0.0\n */",
            "/**\n * 应用上下文刷新后、任何 {@link ApplicationRunner 应用} 与 {@link CommandLineRunner 命令行}\n * Runner 调用前发布的事件。\n *\n * @author Andy Wilkinson\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link ApplicationStartedEvent} instance.\n\t * @param application the current application\n\t * @param args the arguments the application is running with\n\t * @param context the context that was being created\n\t * @param timeTaken the time taken to start the application\n\t * @since 2.6.0\n\t */",
            "/**\n\t * 创建新的 {@link ApplicationStartedEvent} 实例。\n\t *\n\t * @param application 当前应用\n\t * @param args 应用运行参数\n\t * @param context 正在创建的上下文\n\t * @param timeTaken 应用启动所耗时间\n\t * @since 2.6.0\n\t */",
        ),
        (
            "/**\n\t * Return the application context.\n\t * @return the context\n\t */",
            "/**\n\t * 返回应用上下文。\n\t *\n\t * @return 上下文\n\t */",
        ),
        (
            "/**\n\t * Return the time taken to start the application, or {@code null} if unknown.\n\t * @return the startup time\n\t * @since 2.6.0\n\t */",
            "/**\n\t * 返回应用启动所耗时间；未知时为 {@code null}。\n\t *\n\t * @return 启动耗时\n\t * @since 2.6.0\n\t */",
        ),
    ],
    "ApplicationStartingEvent.java": [
        (
            "/**\n * Event published as early as conceivably possible as soon as a {@link SpringApplication}\n * has been started - before the {@link Environment} or {@link ApplicationContext} is\n * available, but after the {@link ApplicationListener}s have been registered. The source\n * of the event is the {@link SpringApplication} itself, but beware of using its internal\n * state too much at this early stage since it might be modified later in the lifecycle.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 1.5.0\n */",
            "/**\n * {@link SpringApplication} 启动后尽可能早地发布的事件——此时 {@link Environment} 与\n * {@link ApplicationContext} 尚不可用，但 {@link ApplicationListener} 已注册。\n * 事件源为 {@link SpringApplication} 本身；此早期阶段其内部状态仍可能被后续生命周期修改，\n * 不宜过度依赖。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 1.5.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link ApplicationStartingEvent} instance.\n\t * @param bootstrapContext the bootstrap context\n\t * @param application the current application\n\t * @param args the arguments the application is running with\n\t */",
            "/**\n\t * 创建新的 {@link ApplicationStartingEvent} 实例。\n\t *\n\t * @param bootstrapContext 引导上下文\n\t * @param application 当前应用\n\t * @param args 应用运行参数\n\t */",
        ),
        (
            "/**\n\t * Return the bootstrap context.\n\t * @return the bootstrap context\n\t * @since 2.4.0\n\t */",
            "/**\n\t * 返回引导上下文。\n\t *\n\t * @return 引导上下文\n\t * @since 2.4.0\n\t */",
        ),
    ],
    "EventPublishingRunListener.java": [
        (
            "/**\n * {@link SpringApplicationRunListener} to publish {@link SpringApplicationEvent}s.\n * <p>\n * Uses an internal {@link ApplicationEventMulticaster} for the events that are fired\n * before the context is actually refreshed.\n *\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @author Andy Wilkinson\n * @author Artsiom Yudovin\n * @author Brian Clozel\n * @author Chris Bono\n */",
            "/**\n * 发布 {@link SpringApplicationEvent} 的 {@link SpringApplicationRunListener}。\n * <p>\n * 对上下文实际刷新前触发的事件，使用内部 {@link ApplicationEventMulticaster}。\n *\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @author Andy Wilkinson\n * @author Artsiom Yudovin\n * @author Brian Clozel\n * @author Chris Bono\n */",
        ),
    ],
    "SpringApplicationEvent.java": [
        (
            "/**\n * Base class for {@link ApplicationEvent} related to a {@link SpringApplication}.\n *\n * @author Phillip Webb\n * @since 1.0.0\n */",
            "/**\n * 与 {@link SpringApplication} 相关的 {@link ApplicationEvent} 基类。\n *\n * @author Phillip Webb\n * @since 1.0.0\n */",
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
