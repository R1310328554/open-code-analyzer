#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-14b slice [20:40] (reactive/servlet context, error pages, filters)."""
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
BATCH_FILES = Path("/tmp/springboot_w14b.txt").read_text(encoding="utf-8").strip().splitlines()

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ConfigurableReactiveWebEnvironment.java": [
        (
            "/**\n * Specialization of {@link ConfigurableEnvironment} for reactive application contexts.\n *\n * @author Phillip Webb\n * @since 2.0.0\n * @see ConfigurableReactiveWebApplicationContext#getEnvironment()\n */",
            "/**\n * 面向响应式应用上下文的 {@link ConfigurableEnvironment} 特化接口。\n *\n * @author Phillip Webb\n * @since 2.0.0\n * @see ConfigurableReactiveWebApplicationContext#getEnvironment()\n */",
        ),
    ],
    "FilteredReactiveWebContextResource.java": [
        (
            "/**\n * Resource implementation that replaces the\n * {@link org.springframework.web.context.support.ServletContextResource} in a reactive\n * web application.\n * <p>\n * {@link #exists()} always returns {@code false} in order to avoid exposing the whole\n * classpath in a non-servlet environment.\n *\n * @author Brian Clozel\n */",
            "/**\n * 在响应式 Web 应用中替代\n * {@link org.springframework.web.context.support.ServletContextResource} 的资源实现。\n * <p>\n * {@link #exists()} 始终返回 {@code false}，以避免在非 Servlet 环境中暴露整个类路径。\n *\n * @author Brian Clozel\n */",
        ),
    ],
    "FilteredReactiveWebContextResourceFilePathResolver.java": [
        (
            "/**\n * {@link FilePathResolver} for {@link FilteredReactiveWebContextResource}.\n *\n * @author Dmytro Nosan\n */",
            "/**\n * 面向 {@link FilteredReactiveWebContextResource} 的 {@link FilePathResolver} 实现。\n * 当资源为 {@link FilteredReactiveWebContextResource} 实例时返回原始 location，否则返回 {@code null}。\n *\n * @author Dmytro Nosan\n */",
        ),
    ],
    "GenericReactiveWebApplicationContext.java": [
        (
            "/**\n * Subclass of {@link GenericApplicationContext}, suitable for reactive web environments.\n *\n * @author Stephane Nicoll\n * @author Brian Clozel\n * @since 2.0.0\n */",
            "/**\n * 适用于响应式 Web 环境的 {@link GenericApplicationContext} 子类。\n *\n * @author Stephane Nicoll\n * @author Brian Clozel\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link GenericReactiveWebApplicationContext}.\n\t * @see #registerBeanDefinition\n\t * @see #refresh\n\t */",
            "\t/**\n\t * 创建新的 {@link GenericReactiveWebApplicationContext}。\n\t * @see #registerBeanDefinition\n\t * @see #refresh\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link GenericReactiveWebApplicationContext} with the given\n\t * DefaultListableBeanFactory.\n\t * @param beanFactory the DefaultListableBeanFactory instance to use for this context\n\t * @see #registerBeanDefinition\n\t * @see #refresh\n\t */",
            "\t/**\n\t * 使用给定 DefaultListableBeanFactory 创建新的 {@link GenericReactiveWebApplicationContext}。\n\t *\n\t * @param beanFactory the DefaultListableBeanFactory instance to use for this context 此上下文使用的 DefaultListableBeanFactory 实例\n\t * @see #registerBeanDefinition\n\t * @see #refresh\n\t */",
        ),
        (
            "\t\t// We must be careful not to expose classpath resources",
            "\t\t// 必须小心避免暴露类路径资源",
        ),
    ],
    "ReactiveWebApplicationContext.java": [
        (
            "/**\n * Interface to provide configuration for a reactive web application.\n *\n * @author Stephane Nicoll\n * @since 2.0.0\n */",
            "/**\n * 为响应式 Web 应用提供配置的接口。\n *\n * @author Stephane Nicoll\n * @since 2.0.0\n */",
        ),
    ],
    "StandardReactiveWebEnvironment.java": [
        (
            "/**\n * {@link Environment} implementation to be used by {@code Reactive}-based web\n * applications. All web-related (reactive-based) {@code ApplicationContext} classes\n * initialize an instance by default.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 供基于 {@code Reactive} 的 Web 应用使用的 {@link Environment} 实现。\n * 所有与 Web 相关的（基于 Reactive 的）{@code ApplicationContext} 类默认都会初始化此实例。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
    ],
    "AnnotationConfigServletWebApplicationContext.java": [
        (
            "/**\n * {@link GenericWebApplicationContext} that accepts annotated classes as input - in\n * particular {@link Configuration @Configuration}-annotated classes, but also plain\n * {@link Component @Component} classes and JSR-330 compliant classes using\n * {@code javax.inject} annotations. Allows for registering classes one by one (specifying\n * class names as config location) as well as for classpath scanning (specifying base\n * packages as config location).\n * <p>\n * Note: In case of multiple {@code @Configuration} classes, later {@code @Bean}\n * definitions will override ones defined in earlier loaded files. This can be leveraged\n * to deliberately override certain bean definitions through an extra Configuration class.\n *\n * @author Stephane Nicoll\n * @since 2.2.0\n * @see #register(Class...)\n * @see #scan(String...)\n */",
            "/**\n * 接受带注解类作为输入的 {@link GenericWebApplicationContext}——\n * 尤其是 {@link Configuration @Configuration} 注解类，也包括普通\n * {@link Component @Component} 类以及使用 {@code javax.inject} 注解的 JSR-330 兼容类。\n * 支持逐个注册类（以类名作为配置位置），也支持类路径扫描（以基础包作为配置位置）。\n * <p>\n * 注意：存在多个 {@code @Configuration} 类时，后加载文件中的 {@code @Bean}\n * 定义会覆盖先加载的定义。可通过额外的 Configuration 类有意覆盖某些 Bean 定义。\n *\n * @author Stephane Nicoll\n * @since 2.2.0\n * @see #register(Class...)\n * @see #scan(String...)\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link AnnotationConfigServletWebApplicationContext} that needs to be\n\t * populated through {@link #register} calls and then manually {@linkplain #refresh\n\t * refreshed}.\n\t */",
            "\t/**\n\t * 创建新的 {@link AnnotationConfigServletWebApplicationContext}，\n\t * 需通过 {@link #register} 调用填充后手动 {@linkplain #refresh 刷新}。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link AnnotationConfigServletWebApplicationContext} with the given\n\t * {@code DefaultListableBeanFactory}. The context needs to be populated through\n\t * {@link #register} calls and then manually {@linkplain #refresh refreshed}.\n\t * @param beanFactory the DefaultListableBeanFactory instance to use for this context\n\t */",
            "\t/**\n\t * 使用给定 {@code DefaultListableBeanFactory} 创建新的 {@link AnnotationConfigServletWebApplicationContext}。\n\t * 需通过 {@link #register} 调用填充后手动 {@linkplain #refresh 刷新}。\n\t *\n\t * @param beanFactory the DefaultListableBeanFactory instance to use for this context 此上下文使用的 DefaultListableBeanFactory 实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link AnnotationConfigServletWebApplicationContext}, deriving bean\n\t * definitions from the given annotated classes and automatically refreshing the\n\t * context.\n\t * @param annotatedClasses one or more annotated classes, e.g. {@code @Configuration}\n\t * classes\n\t */",
            "\t/**\n\t * 创建新的 {@link AnnotationConfigServletWebApplicationContext}，\n\t * 从给定带注解类派生 Bean 定义并自动刷新上下文。\n\t *\n\t * @param annotatedClasses one or more annotated classes, e.g. {@code @Configuration}\n\t * classes 一个或多个带注解类，例如 {@code @Configuration} 类\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link AnnotationConfigServletWebApplicationContext}, scanning for\n\t * bean definitions in the given packages and automatically refreshing the context.\n\t * @param basePackages the packages to check for annotated classes\n\t */",
            "\t/**\n\t * 创建新的 {@link AnnotationConfigServletWebApplicationContext}，\n\t * 扫描给定包中的 Bean 定义并自动刷新上下文。\n\t *\n\t * @param basePackages the packages to check for annotated classes 待扫描带注解类的基础包\n\t */",
        ),
        (
            "\t/**\n\t * {@inheritDoc}\n\t * <p>\n\t * Delegates given environment to underlying {@link AnnotatedBeanDefinitionReader} and\n\t * {@link ClassPathBeanDefinitionScanner} members.\n\t */",
            "\t/**\n\t * {@inheritDoc}\n\t * <p>\n\t * 将给定环境委托给底层的 {@link AnnotatedBeanDefinitionReader} 与\n\t * {@link ClassPathBeanDefinitionScanner} 成员。\n\t */",
        ),
        (
            "\t/**\n\t * Provide a custom {@link BeanNameGenerator} for use with\n\t * {@link AnnotatedBeanDefinitionReader} and/or\n\t * {@link ClassPathBeanDefinitionScanner}, if any.\n\t * <p>\n\t * Default is\n\t * {@link org.springframework.context.annotation.AnnotationBeanNameGenerator}.\n\t * <p>\n\t * Any call to this method must occur prior to calls to {@link #register(Class...)}\n\t * and/or {@link #scan(String...)}.\n\t * @param beanNameGenerator the bean name generator\n\t * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator\n\t * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator\n\t */",
            "\t/**\n\t * 为 {@link AnnotatedBeanDefinitionReader} 和/或\n\t * {@link ClassPathBeanDefinitionScanner} 提供自定义 {@link BeanNameGenerator}（若有）。\n\t * <p>\n\t * 默认为 {@link org.springframework.context.annotation.AnnotationBeanNameGenerator}。\n\t * <p>\n\t * 必须在调用 {@link #register(Class...)} 和/或 {@link #scan(String...)} 之前调用此方法。\n\t *\n\t * @param beanNameGenerator the bean name generator Bean 名称生成器\n\t * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator\n\t * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link ScopeMetadataResolver} to use for detected bean classes.\n\t * <p>\n\t * The default is an {@link AnnotationScopeMetadataResolver}.\n\t * <p>\n\t * Any call to this method must occur prior to calls to {@link #register(Class...)}\n\t * and/or {@link #scan(String...)}.\n\t * @param scopeMetadataResolver the scope metadata resolver\n\t */",
            "\t/**\n\t * 设置用于检测到的 Bean 类的 {@link ScopeMetadataResolver}。\n\t * <p>\n\t * 默认为 {@link AnnotationScopeMetadataResolver}。\n\t * <p>\n\t * 必须在调用 {@link #register(Class...)} 和/或 {@link #scan(String...)} 之前调用此方法。\n\t *\n\t * @param scopeMetadataResolver the scope metadata resolver 作用域元数据解析器\n\t */",
        ),
        (
            "\t/**\n\t * Register one or more annotated classes to be processed. Note that\n\t * {@link #refresh()} must be called in order for the context to fully process the new\n\t * class.\n\t * <p>\n\t * Calls to {@code #register} are idempotent; adding the same annotated class more\n\t * than once has no additional effect.\n\t * @param annotatedClasses one or more annotated classes, e.g. {@code @Configuration}\n\t * classes\n\t * @see #scan(String...)\n\t * @see #refresh()\n\t */",
            "\t/**\n\t * 注册一个或多个待处理的带注解类。注意必须调用 {@link #refresh()} 上下文才能完全处理新类。\n\t * <p>\n\t * 对 {@code #register} 的调用是幂等的；重复添加同一带注解类不会产生额外效果。\n\t *\n\t * @param annotatedClasses one or more annotated classes, e.g. {@code @Configuration}\n\t * classes 一个或多个带注解类，例如 {@code @Configuration} 类\n\t * @see #scan(String...)\n\t * @see #refresh()\n\t */",
        ),
        (
            "\t/**\n\t * Perform a scan within the specified base packages. Note that {@link #refresh()}\n\t * must be called in order for the context to fully process the new class.\n\t * @param basePackages the packages to check for annotated classes\n\t * @see #register(Class...)\n\t * @see #refresh()\n\t */",
            "\t/**\n\t * 在指定基础包内执行扫描。注意必须调用 {@link #refresh()} 上下文才能完全处理新类。\n\t *\n\t * @param basePackages the packages to check for annotated classes 待扫描带注解类的基础包\n\t * @see #register(Class...)\n\t * @see #refresh()\n\t */",
        ),
    ],
    "ApplicationServletEnvironment.java": [
        (
            "/**\n * {@link StandardServletEnvironment} for typical use in a typical\n * {@link SpringApplication}.\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
            "/**\n * 在典型 {@link SpringApplication} 中常用的 {@link StandardServletEnvironment} 实现。\n * 使用 {@link ConfigurationPropertySources} 创建属性解析器，\n * 并禁用通过系统属性设置 active/default profiles。\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
        ),
    ],
    "ServletContextResourceFilePathResolver.java": [
        (
            "/**\n * {@link FilePathResolver} for {@link ServletContextResource}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 面向 {@link ServletContextResource} 的 {@link FilePathResolver} 实现。\n * 当资源为 {@link ServletContextResource} 实例时返回原始 location，否则返回 {@code null}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "WebApplicationContextInitializer.java": [
        (
            "/**\n * Common initialization logic for Servlet web applications.\n *\n * @author Andy Wilkinson\n * @since 4.0.0\n */",
            "/**\n * Servlet Web 应用的通用初始化逻辑。\n *\n * @author Andy Wilkinson\n * @since 4.0.0\n */",
        ),
        (
            "\t\t// Register as ServletContext attribute, for ContextCleanupListener to detect it.",
            "\t\t// 注册为 ServletContext 属性，供 ContextCleanupListener 检测。",
        ),
    ],
    "Error.java": [
        (
            "/**\n * A wrapper class for {@link MessageSourceResolvable} errors that is safe for JSON\n * serialization.\n *\n * @author Yongjun Hong\n * @author Phillip Webb\n * @since 3.5.0\n */",
            "/**\n * 对 {@link MessageSourceResolvable} 错误进行包装、且可安全进行 JSON 序列化的类。\n *\n * @author Yongjun Hong\n * @author Phillip Webb\n * @since 3.5.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code Error} instance with the specified cause.\n\t * @param cause the error cause (must not be {@code null})\n\t */",
            "\t/**\n\t * 使用指定原因创建新的 {@code Error} 实例。\n\t *\n\t * @param cause the error cause (must not be {@code null}) 错误原因（不得为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Return the original cause of the error.\n\t * @return the error cause\n\t */",
            "\t/**\n\t * 返回错误的原始原因。\n\t *\n\t * @return the error cause 错误原因\n\t */",
        ),
        (
            "\t/**\n\t * Wrap the given errors, if necessary, such that they are suitable for serialization\n\t * to JSON. {@link MessageSourceResolvable} implementations that are known to be\n\t * suitable are not wrapped.\n\t * @param errors the errors to wrap\n\t * @return a new Error list\n\t * @since 3.5.4\n\t */",
            "\t/**\n\t * 如有必要，包装给定错误使其适合 JSON 序列化。\n\t * 已知适合序列化的 {@link MessageSourceResolvable} 实现不会被包装。\n\t *\n\t * @param errors the errors to wrap 待包装的错误\n\t * @return a new Error list 新的 Error 列表\n\t * @since 3.5.4\n\t */",
        ),
    ],
    "ErrorAttributeOptions.java": [
        (
            "/**\n * Options controlling the contents of {@code ErrorAttributes}.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 2.3.0\n */",
            "/**\n * 控制 {@code ErrorAttributes} 内容的选项。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 2.3.0\n */",
        ),
        (
            "\t/**\n\t * Get the option for including the specified attribute in the error response.\n\t * @param include error attribute to get\n\t * @return {@code true} if the {@code Include} attribute is included in the error\n\t * response, {@code false} otherwise\n\t */",
            "\t/**\n\t * 获取是否在错误响应中包含指定属性的选项。\n\t *\n\t * @param include error attribute to get 待查询的错误属性\n\t * @return {@code true} if the {@code Include} attribute is included in the error\n\t * response, {@code false} otherwise 若错误响应包含该 {@code Include} 属性则为 {@code true}，否则为 {@code false}\n\t */",
        ),
        (
            "\t/**\n\t * Get all options for including attributes in the error response.\n\t * @return the options\n\t */",
            "\t/**\n\t * 获取错误响应中包含属性的全部选项。\n\t *\n\t * @return the options 选项集合\n\t */",
        ),
        (
            "\t/**\n\t * Return an {@code ErrorAttributeOptions} that includes the specified attribute\n\t * {@link Include} options.\n\t * @param includes error attributes to include\n\t * @return an {@code ErrorAttributeOptions}\n\t */",
            "\t/**\n\t * 返回包含指定 {@link Include} 选项的 {@code ErrorAttributeOptions}。\n\t *\n\t * @param includes error attributes to include 要包含的错误属性\n\t * @return an {@code ErrorAttributeOptions} {@code ErrorAttributeOptions} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Return an {@code ErrorAttributeOptions} that excludes the specified attribute\n\t * {@link Include} options.\n\t * @param excludes error attributes to exclude\n\t * @return an {@code ErrorAttributeOptions}\n\t */",
            "\t/**\n\t * 返回排除指定 {@link Include} 选项的 {@code ErrorAttributeOptions}。\n\t *\n\t * @param excludes error attributes to exclude 要排除的错误属性\n\t * @return an {@code ErrorAttributeOptions} {@code ErrorAttributeOptions} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Remove elements from the given map if they are not included in this set of options.\n\t * @param map the map to update\n\t * @since 3.2.7\n\t */",
            "\t/**\n\t * 从此选项集合中未包含的项会从给定 map 中移除。\n\t *\n\t * @param map the map to update 待更新的 map\n\t * @since 3.2.7\n\t */",
        ),
        (
            "\t/**\n\t * Create an {@code ErrorAttributeOptions} with defaults.\n\t * @return an {@code ErrorAttributeOptions}\n\t */",
            "\t/**\n\t * 创建带默认值的 {@code ErrorAttributeOptions}。\n\t *\n\t * @return an {@code ErrorAttributeOptions} {@code ErrorAttributeOptions} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Create an {@code ErrorAttributeOptions} that includes the specified attribute\n\t * {@link Include} options.\n\t * @param includes error attributes to include\n\t * @return an {@code ErrorAttributeOptions}\n\t */",
            "\t/**\n\t * 创建包含指定 {@link Include} 选项的 {@code ErrorAttributeOptions}。\n\t *\n\t * @param includes error attributes to include 要包含的错误属性\n\t * @return an {@code ErrorAttributeOptions} {@code ErrorAttributeOptions} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Create an {@code ErrorAttributeOptions} that includes the specified attribute\n\t * {@link Include} options.\n\t * @param includes error attributes to include\n\t * @return an {@code ErrorAttributeOptions}\n\t */",
            "\t/**\n\t * 创建包含指定 {@link Include} 选项的 {@code ErrorAttributeOptions}。\n\t *\n\t * @param includes error attributes to include 要包含的错误属性\n\t * @return an {@code ErrorAttributeOptions} {@code ErrorAttributeOptions} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Error attributes that can be included in an error response.\n\t */",
            "\t/**\n\t * 可包含在错误响应中的错误属性。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Include the exception class name attribute.\n\t\t */",
            "\t\t/**\n\t\t * 包含异常类名属性。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Include the stack trace attribute.\n\t\t */",
            "\t\t/**\n\t\t * 包含堆栈跟踪属性。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Include the message attribute.\n\t\t */",
            "\t\t/**\n\t\t * 包含消息属性。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Include the binding errors attribute.\n\t\t */",
            "\t\t/**\n\t\t * 包含绑定错误属性。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Include the HTTP status code.\n\t\t * @since 3.2.7\n\t\t */",
            "\t\t/**\n\t\t * 包含 HTTP 状态码。\n\t\t * @since 3.2.7\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Include the HTTP status code.\n\t\t * @since 3.2.7\n\t\t */",
            "\t\t/**\n\t\t * 包含 HTTP 错误描述。\n\t\t * @since 3.2.7\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Include the request path.\n\t\t * @since 3.3.0\n\t\t */",
            "\t\t/**\n\t\t * 包含请求路径。\n\t\t * @since 3.3.0\n\t\t */",
        ),
    ],
    "ErrorPage.java": [
        (
            "/**\n * Simple server-independent abstraction for error pages. Roughly equivalent to the\n * {@literal &lt;error-page&gt;} element traditionally found in web.xml.\n *\n * @author Dave Syer\n * @since 4.0.0\n */",
            "/**\n * 与服务器无关的错误页面简单抽象，大致等价于 web.xml 中传统的\n * {@literal &lt;error-page&gt;} 元素。\n *\n * @author Dave Syer\n * @since 4.0.0\n */",
        ),
        (
            "\t/**\n\t * The path to render (usually implemented as a forward), starting with \"/\". A custom\n\t * controller or servlet path can be used, or if the server supports it, a template\n\t * path (e.g. \"/error.jsp\").\n\t * @return the path that will be rendered for this error\n\t */",
            "\t/**\n\t * 用于渲染的路径（通常实现为 forward），以 \"/\" 开头。\n\t * 可使用自定义控制器或 Servlet 路径，若服务器支持也可使用模板路径（例如 \"/error.jsp\"）。\n\t *\n\t * @return the path that will be rendered for this error 此错误将渲染的路径\n\t */",
        ),
        (
            "\t/**\n\t * Returns the exception type (or {@code null} for a page that matches by status).\n\t * @return the exception type or {@code null}\n\t */",
            "\t/**\n\t * 返回异常类型（按状态码匹配的页面则返回 {@code null}）。\n\t *\n\t * @return the exception type or {@code null} 异常类型或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * The HTTP status value that this error page matches (or {@code null} for a page that\n\t * matches by exception).\n\t * @return the status or {@code null}\n\t */",
            "\t/**\n\t * 此错误页面匹配的 HTTP 状态值（按异常匹配的页面则返回 {@code null}）。\n\t *\n\t * @return the status or {@code null} 状态或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * The HTTP status value that this error page matches.\n\t * @return the status value (or 0 for a page that matches any status)\n\t */",
            "\t/**\n\t * 此错误页面匹配的 HTTP 状态值。\n\t *\n\t * @return the status value (or 0 for a page that matches any status) 状态值（匹配任意状态的页面返回 0）\n\t */",
        ),
        (
            "\t/**\n\t * The exception type name.\n\t * @return the exception type name (or {@code null} if there is none)\n\t */",
            "\t/**\n\t * 异常类型名称。\n\t *\n\t * @return the exception type name (or {@code null} if there is none) 异常类型名称（若无则为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Return if this error page is a global one (matches all unmatched status and\n\t * exception types).\n\t * @return if this is a global error page\n\t */",
            "\t/**\n\t * 返回此错误页面是否为全局页面（匹配所有未匹配的状态码与异常类型）。\n\t *\n\t * @return if this is a global error page 是否为全局错误页面\n\t */",
        ),
    ],
    "ErrorPageRegistrar.java": [
        (
            "/**\n * Interface to be implemented by types that register {@link ErrorPage ErrorPages}.\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
            "/**\n * 由注册 {@link ErrorPage 错误页面} 的类型实现的接口。\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
        ),
        (
            "\t/**\n\t * Register pages as required with the given registry.\n\t * @param registry the error page registry\n\t */",
            "\t/**\n\t * 按需在给定注册表中注册错误页面。\n\t *\n\t * @param registry the error page registry 错误页面注册表\n\t */",
        ),
    ],
    "ErrorPageRegistrarBeanPostProcessor.java": [
        (
            "/**\n * {@link BeanPostProcessor} that applies all {@link ErrorPageRegistrar}s from the bean\n * factory to {@link ErrorPageRegistry} beans.\n *\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @since 4.0.0\n */",
            "/**\n * 将 Bean 工厂中所有 {@link ErrorPageRegistrar} 应用到 {@link ErrorPageRegistry} Bean 的\n * {@link BeanPostProcessor}。\n *\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @since 4.0.0\n */",
        ),
        (
            "\t\t\t// Look up does not include the parent context",
            "\t\t\t// 查找不包含父上下文",
        ),
    ],
    "ErrorPageRegistry.java": [
        (
            "/**\n * Interface for a registry that holds {@link ErrorPage ErrorPages}.\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
            "/**\n * 持有 {@link ErrorPage 错误页面} 的注册表接口。\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
        ),
        (
            "\t/**\n\t * Adds error pages that will be used when handling exceptions.\n\t * @param errorPages the error pages\n\t */",
            "\t/**\n\t * 添加处理异常时使用的错误页面。\n\t *\n\t * @param errorPages the error pages 错误页面\n\t */",
        ),
    ],
    "AbstractFilterRegistrationBean.java": [
        (
            "/**\n * Abstract base {@link ServletContextInitializer} to register {@link Filter}s in a\n * Servlet 3.0+ container.\n *\n * @param <T> the type of {@link Filter} to register\n * @author Phillip Webb\n * @author Brian Clozel\n * @since 1.5.22\n */",
            "/**\n * 在 Servlet 3.0+ 容器中注册 {@link Filter} 的抽象基类 {@link ServletContextInitializer}。\n *\n * @param <T> the type of {@link Filter} to register 待注册的 {@link Filter} 类型\n * @author Phillip Webb\n * @author Brian Clozel\n * @since 1.5.22\n */",
        ),
        (
            "\t/**\n\t * Create a new instance to be registered with the specified\n\t * {@link ServletRegistrationBean}s.\n\t * @param servletRegistrationBeans associate {@link ServletRegistrationBean}s\n\t */",
            "\t/**\n\t * 创建新实例，与指定 {@link ServletRegistrationBean} 关联注册。\n\t *\n\t * @param servletRegistrationBeans associate {@link ServletRegistrationBean}s 关联的 {@link ServletRegistrationBean}\n\t */",
        ),
        (
            "\t/**\n\t * Set {@link ServletRegistrationBean}s that the filter will be registered against.\n\t * @param servletRegistrationBeans the Servlet registration beans\n\t */",
            "\t/**\n\t * 设置过滤器将注册到的 {@link ServletRegistrationBean}。\n\t *\n\t * @param servletRegistrationBeans the Servlet registration beans Servlet 注册 Bean\n\t */",
        ),
        (
            "\t/**\n\t * Return a mutable collection of the {@link ServletRegistrationBean} that the filter\n\t * will be registered against. {@link ServletRegistrationBean}s.\n\t * @return the Servlet registration beans\n\t * @see #setServletNames\n\t * @see #setUrlPatterns\n\t */",
            "\t/**\n\t * 返回过滤器将注册到的 {@link ServletRegistrationBean} 可变集合。\n\t *\n\t * @return the Servlet registration beans Servlet 注册 Bean 集合\n\t * @see #setServletNames\n\t * @see #setUrlPatterns\n\t */",
        ),
        (
            "\t/**\n\t * Add {@link ServletRegistrationBean}s for the filter.\n\t * @param servletRegistrationBeans the servlet registration beans to add\n\t * @see #setServletRegistrationBeans\n\t */",
            "\t/**\n\t * 为过滤器添加 {@link ServletRegistrationBean}。\n\t *\n\t * @param servletRegistrationBeans the servlet registration beans to add 待添加的 Servlet 注册 Bean\n\t * @see #setServletRegistrationBeans\n\t */",
        ),
        (
            "\t/**\n\t * Set servlet names that the filter will be registered against. This will replace any\n\t * previously specified servlet names.\n\t * @param servletNames the servlet names\n\t * @see #setServletRegistrationBeans\n\t * @see #setUrlPatterns\n\t */",
            "\t/**\n\t * 设置过滤器将注册到的 Servlet 名称，会替换先前指定的 Servlet 名称。\n\t *\n\t * @param servletNames the servlet names Servlet 名称\n\t * @see #setServletRegistrationBeans\n\t * @see #setUrlPatterns\n\t */",
        ),
        (
            "\t/**\n\t * Return a mutable collection of servlet names that the filter will be registered\n\t * against.\n\t * @return the servlet names\n\t */",
            "\t/**\n\t * 返回过滤器将注册到的 Servlet 名称可变集合。\n\t *\n\t * @return the servlet names Servlet 名称\n\t */",
        ),
        (
            "\t/**\n\t * Add servlet names for the filter.\n\t * @param servletNames the servlet names to add\n\t */",
            "\t/**\n\t * 为过滤器添加 Servlet 名称。\n\t *\n\t * @param servletNames the servlet names to add 待添加的 Servlet 名称\n\t */",
        ),
        (
            "\t/**\n\t * Set the URL patterns that the filter will be registered against. This will replace\n\t * any previously specified URL patterns.\n\t * @param urlPatterns the URL patterns\n\t * @see #setServletRegistrationBeans\n\t * @see #setServletNames\n\t */",
            "\t/**\n\t * 设置过滤器将注册到的 URL 模式，会替换先前指定的 URL 模式。\n\t *\n\t * @param urlPatterns the URL patterns URL 模式\n\t * @see #setServletRegistrationBeans\n\t * @see #setServletNames\n\t */",
        ),
        (
            "\t/**\n\t * Return a mutable collection of URL patterns, as defined in the Servlet\n\t * specification, that the filter will be registered against.\n\t * @return the URL patterns\n\t */",
            "\t/**\n\t * 返回过滤器将注册到的 URL 模式可变集合（按 Servlet 规范定义）。\n\t *\n\t * @return the URL patterns URL 模式\n\t */",
        ),
        (
            "\t/**\n\t * Add URL patterns, as defined in the Servlet specification, that the filter will be\n\t * registered against.\n\t * @param urlPatterns the URL patterns\n\t */",
            "\t/**\n\t * 添加过滤器将注册到的 URL 模式（按 Servlet 规范定义）。\n\t *\n\t * @param urlPatterns the URL patterns URL 模式\n\t */",
        ),
        (
            "\t/**\n\t * Determines the {@link DispatcherType dispatcher types} for which the filter should\n\t * be registered. Applies defaults based on the type of filter being registered if\n\t * none have been configured. Modifications to the returned {@link EnumSet} will have\n\t * no effect on the registration.\n\t * @return the dispatcher types, never {@code null}\n\t * @since 3.2.0\n\t */",
            "\t/**\n\t * 确定过滤器应注册的 {@link DispatcherType 分发类型}。\n\t * 若未配置则根据所注册过滤器类型应用默认值。对返回的 {@link EnumSet} 的修改不会影响注册。\n\t *\n\t * @return the dispatcher types, never {@code null} 分发类型，永不为 {@code null}\n\t * @since 3.2.0\n\t */",
        ),
        (
            "\t/**\n\t * Convenience method to {@link #setDispatcherTypes(EnumSet) set dispatcher types}\n\t * using the specified elements.\n\t * @param first the first dispatcher type\n\t * @param rest additional dispatcher types\n\t */",
            "\t/**\n\t * 使用指定元素 {@link #setDispatcherTypes(EnumSet) 设置分发类型} 的便捷方法。\n\t *\n\t * @param first the first dispatcher type 第一个分发类型\n\t * @param rest additional dispatcher types 其余分发类型\n\t */",
        ),
        (
            "\t/**\n\t * Sets the dispatcher types that should be used with the registration.\n\t * @param dispatcherTypes the dispatcher types\n\t */",
            "\t/**\n\t * 设置注册应使用的分发类型。\n\t *\n\t * @param dispatcherTypes the dispatcher types 分发类型\n\t */",
        ),
        (
            "\t/**\n\t * Set if the filter mappings should be matched after any declared filter mappings of\n\t * the ServletContext. Defaults to {@code false} indicating the filters are supposed\n\t * to be matched before any declared filter mappings of the ServletContext.\n\t * @param matchAfter if filter mappings are matched after\n\t */",
            "\t/**\n\t * 设置过滤器映射是否应在 ServletContext 已声明的过滤器映射之后匹配。\n\t * 默认为 {@code false}，表示过滤器应在 ServletContext 已声明的过滤器映射之前匹配。\n\t *\n\t * @param matchAfter if filter mappings are matched after 是否在之后匹配\n\t */",
        ),
        (
            "\t/**\n\t * Return if filter mappings should be matched after any declared Filter mappings of\n\t * the ServletContext.\n\t * @return if filter mappings are matched after\n\t */",
            "\t/**\n\t * 返回过滤器映射是否应在 ServletContext 已声明的 Filter 映射之后匹配。\n\t *\n\t * @return if filter mappings are matched after 是否在之后匹配\n\t */",
        ),
        (
            "\t/**\n\t * Configure registration settings. Subclasses can override this method to perform\n\t * additional configuration if required.\n\t * @param registration the registration\n\t */",
            "\t/**\n\t * 配置注册设置。子类可按需覆盖此方法以执行额外配置。\n\t *\n\t * @param registration the registration 注册对象\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@link Filter} to be registered.\n\t * @return the filter\n\t */",
            "\t/**\n\t * 返回待注册的 {@link Filter}。\n\t *\n\t * @return the filter 过滤器\n\t */",
        ),
        (
            "\t/**\n\t * Returns the filter name that will be registered.\n\t * @return the filter name\n\t * @since 3.2.0\n\t */",
            "\t/**\n\t * 返回将要注册的过滤器名称。\n\t *\n\t * @return the filter name 过滤器名称\n\t * @since 3.2.0\n\t */",
        ),
    ],
    "DelegatingFilterProxyRegistrationBean.java": [
        (
            "/**\n * A {@link ServletContextInitializer} to register {@link DelegatingFilterProxy}s in a\n * Servlet 3.0+ container. Similar to the {@link ServletContext#addFilter(String, Filter)\n * registration} features provided by {@link ServletContext} but with a Spring Bean\n * friendly design.\n * <p>\n * The bean name of the actual delegate {@link Filter} should be specified using the\n * {@code targetBeanName} constructor argument. Unlike the {@link FilterRegistrationBean},\n * referenced filters are not instantiated early. In fact, if the delegate filter bean is\n * marked {@code @Lazy} it won't be instantiated at all until the filter is called.\n * <p>\n * Registrations can be associated with {@link #setUrlPatterns URL patterns} and/or\n * servlets (either by {@link #setServletNames name} or through a\n * {@link #setServletRegistrationBeans ServletRegistrationBean}s). When no URL pattern or\n * servlets are specified the filter will be associated to '/*'. The targetBeanName will\n * be used as the filter name if not otherwise specified.\n *\n * @author Phillip Webb\n * @since 1.4.0\n * @see ServletContextInitializer\n * @see ServletContext#addFilter(String, Filter)\n * @see FilterRegistrationBean\n * @see DelegatingFilterProxy\n */",
            "/**\n * 在 Servlet 3.0+ 容器中注册 {@link DelegatingFilterProxy} 的 {@link ServletContextInitializer}。\n * 类似 {@link ServletContext} 提供的 {@link ServletContext#addFilter(String, Filter) 注册} 能力，\n * 但采用对 Spring Bean 更友好的设计。\n * <p>\n * 实际委托 {@link Filter} 的 Bean 名称应通过 {@code targetBeanName} 构造参数指定。\n * 与 {@link FilterRegistrationBean} 不同，引用的过滤器不会提前实例化；\n * 若委托过滤器 Bean 标记为 {@code @Lazy}，则在过滤器被调用前甚至不会实例化。\n * <p>\n * 注册可关联 {@link #setUrlPatterns URL 模式} 和/或 Servlet\n *（通过 {@link #setServletNames 名称} 或 {@link #setServletRegistrationBeans ServletRegistrationBean}）。\n * 未指定 URL 模式或 Servlet 时，过滤器将关联到 '/*'。\n * 若未另行指定，targetBeanName 将用作过滤器名称。\n *\n * @author Phillip Webb\n * @since 1.4.0\n * @see ServletContextInitializer\n * @see ServletContext#addFilter(String, Filter)\n * @see FilterRegistrationBean\n * @see DelegatingFilterProxy\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link DelegatingFilterProxyRegistrationBean} instance to be\n\t * registered with the specified {@link ServletRegistrationBean}s.\n\t * @param targetBeanName name of the target filter bean to look up in the Spring\n\t * application context (must not be {@code null}).\n\t * @param servletRegistrationBeans associate {@link ServletRegistrationBean}s\n\t */",
            "\t/**\n\t * 创建新的 {@link DelegatingFilterProxyRegistrationBean} 实例，\n\t * 与指定 {@link ServletRegistrationBean} 关联注册。\n\t *\n\t * @param targetBeanName name of the target filter bean to look up in the Spring\n\t * application context (must not be {@code null}). 在 Spring 应用上下文中查找的目标过滤器 Bean 名称（不得为 {@code null}）\n\t * @param servletRegistrationBeans associate {@link ServletRegistrationBean}s 关联的 {@link ServletRegistrationBean}\n\t */",
        ),
        (
            "\t\t\t\t// Don't initialize filter bean on init()",
            "\t\t\t\t// 不在 init() 时初始化过滤器 Bean",
        ),
    ],
    "DispatcherType.java": [
        (
            "/**\n * Enumeration of filter dispatcher types, identical to\n * {@link jakarta.servlet.DispatcherType} and used in configuration as the servlet API may\n * not be present.\n *\n * @author Stephane Nicoll\n * @since 2.0.0\n */",
            "/**\n * 过滤器分发类型枚举，与 {@link jakarta.servlet.DispatcherType} 一致，\n * 用于配置场景（Servlet API 可能不存在）。\n *\n * @author Stephane Nicoll\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Apply the filter on \"RequestDispatcher.forward()\" calls.\n\t */",
            "\t/**\n\t * 在 \"RequestDispatcher.forward()\" 调用时应用过滤器。\n\t */",
        ),
        (
            "\t/**\n\t * Apply the filter on \"RequestDispatcher.include()\" calls.\n\t */",
            "\t/**\n\t * 在 \"RequestDispatcher.include()\" 调用时应用过滤器。\n\t */",
        ),
        (
            "\t/**\n\t * Apply the filter on ordinary client calls.\n\t */",
            "\t/**\n\t * 在普通客户端请求时应用过滤器。\n\t */",
        ),
        (
            "\t/**\n\t * Apply the filter under calls dispatched from an AsyncContext.\n\t */",
            "\t/**\n\t * 在从 AsyncContext 分发的调用中应用过滤器。\n\t */",
        ),
        (
            "\t/**\n\t * Apply the filter when an error is handled.\n\t */",
            "\t/**\n\t * 在处理错误时应用过滤器。\n\t */",
        ),
    ],
    "DynamicRegistrationBean.java": [
        (
            "/**\n * Base class for Servlet 3.0+ {@link jakarta.servlet.Registration.Dynamic dynamic} based\n * registration beans.\n *\n * @param <D> the dynamic registration result\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 2.0.0\n */",
            "/**\n * 基于 Servlet 3.0+ {@link jakarta.servlet.Registration.Dynamic 动态} 注册的 Bean 基类。\n *\n * @param <D> the dynamic registration result 动态注册结果类型\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Set the name of this registration. If not specified the bean name will be used.\n\t * @param name the name of the registration\n\t */",
            "\t/**\n\t * 设置此注册的名称。若未指定则使用 Bean 名称。\n\t *\n\t * @param name the name of the registration 注册名称\n\t */",
        ),
        (
            "\t/**\n\t * Sets if asynchronous operations are supported for this registration. If not\n\t * specified defaults to {@code true}.\n\t * @param asyncSupported if async is supported\n\t */",
            "\t/**\n\t * 设置此注册是否支持异步操作。未指定时默认为 {@code true}。\n\t *\n\t * @param asyncSupported if async is supported 是否支持异步\n\t */",
        ),
        (
            "\t/**\n\t * Returns if asynchronous operations are supported for this registration.\n\t * @return if async is supported\n\t */",
            "\t/**\n\t * 返回此注册是否支持异步操作。\n\t *\n\t * @return if async is supported 是否支持异步\n\t */",
        ),
        (
            "\t/**\n\t * Set init-parameters for this registration. Calling this method will replace any\n\t * existing init-parameters.\n\t * @param initParameters the init parameters\n\t * @see #getInitParameters\n\t * @see #addInitParameter\n\t */",
            "\t/**\n\t * 设置此注册的 init 参数。调用此方法会替换现有 init 参数。\n\t *\n\t * @param initParameters the init parameters init 参数\n\t * @see #getInitParameters\n\t * @see #addInitParameter\n\t */",
        ),
        (
            "\t/**\n\t * Returns a mutable Map of the registration init-parameters.\n\t * @return the init parameters\n\t */",
            "\t/**\n\t * 返回注册 init 参数的可变 Map。\n\t *\n\t * @return the init parameters init 参数\n\t */",
        ),
        (
            "\t/**\n\t * Add a single init-parameter, replacing any existing parameter with the same name.\n\t * @param name the init-parameter name\n\t * @param value the init-parameter value\n\t */",
            "\t/**\n\t * 添加单个 init 参数，替换同名现有参数。\n\t *\n\t * @param name the init-parameter name init 参数名\n\t * @param value the init-parameter value init 参数值\n\t */",
        ),
        (
            "\t/**\n\t * Sets whether registration failures should be ignored. If set to true, a failure\n\t * will be logged. If set to false, an {@link IllegalStateException} will be thrown.\n\t * @param ignoreRegistrationFailure whether to ignore registration failures\n\t * @since 3.1.0\n\t */",
            "\t/**\n\t * 设置是否忽略注册失败。为 true 时记录日志；为 false 时抛出 {@link IllegalStateException}。\n\t *\n\t * @param ignoreRegistrationFailure whether to ignore registration failures 是否忽略注册失败\n\t * @since 3.1.0\n\t */",
        ),
        (
            "\t/**\n\t * Deduces the name for this registration. Will return user specified name or fallback\n\t * to the bean name. If the bean name is not available, convention based naming is\n\t * used.\n\t * @param value the object used for convention based names\n\t * @return the deduced name\n\t */",
            "\t/**\n\t * 推断此注册的名称。返回用户指定名称或回退到 Bean 名称；\n\t * 若 Bean 名称不可用则使用基于约定的命名。\n\t *\n\t * @param value the object used for convention based names 用于约定命名的对象\n\t * @return the deduced name 推断出的名称\n\t */",
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
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
