#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-7a slice [0:20]."""
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
    "ConfigurationPropertiesSource.java": [
        (
            "/**\n * Indicates that the annotated type is a source of configuration properties metadata.\n * <p>\n * This annotation has no effect on the actual binding process, but serves as a hint to\n * the {@code spring-boot-configuration-processor} to generate full metadata for the type.\n * <p>\n * Typically, this annotation is only required for types located in a different module\n * than the {@code @ConfigurationProperties} class that references them. When both types\n * are in the same module, the annotation processor can automatically discover full\n * metadata as long as the source is available.\n * <p>\n * Use this annotation when metadata for types located outside the module is needed:\n * <ol>\n * <li>Nested types annotated by {@code @NestedConfigurationProperty}</li>\n * <li>Base classes that a {@code @ConfigurationProperties}-annotated type extends\n * from</li>\n * </ol>\n * <p>\n * In the example below, {@code ServerProperties} is located in module \"A\" and\n * {@code Host} in module \"B\":<pre><code class=\"java\">\n * &#064;ConfigurationProperties(\"example.server\")\n * class ServerProperties {\n *\n *     &#064;NestedConfigurationProperty\n *     private final Host host = new Host();\n *\n *     public Host getHost() { ... }\n *\n *     // Other properties, getter, setter.\n *\n * }</code></pre>\n * <p>\n * Properties from {@code Host} are detected as they are based on the type, but\n * description and default value are not. To fix this, add the\n * {@code spring-boot-configuration-processor} to module \"B\" if it is not present already\n * and update {@code Host} as follows::<pre><code class=\"java\">\n * &#064;ConfigurationPropertiesSource\n * class Host {\n *\n *     /**\n *      * URL to use.\n *      *&#47;\n *     private String url = \"https://example.com\";\n *\n *     // Other properties, getter, setter.\n *\n * }</code></pre>\n * <p>\n * Similarly the metadata of a base class that a\n * {@code @ConfigurationProperties}-annotated type extends from can also be detected.\n * Consider the following example:<pre><code class=\"java\">\n * &#064;ConfigurationProperties(\"example.client.github\")\n * class GitHubClientProperties extends AbstractClientProperties {\n *\n *     // Additional properties, getter, setter.\n *\n * }</code></pre>\n * <p>\n * As with nested types, adding {@code @ConfigurationPropertiesSource} to\n * {@code AbstractClientProperties} and the {@code spring-boot-configuration-processor} to\n * its module ensures full metadata generation.\n *\n * @author Stephane Nicoll\n * @since 4.0.0\n */",
            "/**\n * 标记被注解的类型为配置属性元数据的来源。\n * <p>\n * 此注解对实际绑定过程无影响，但会向 {@code spring-boot-configuration-processor}\n * 发出提示，以便为该类型生成完整元数据。\n * <p>\n * 通常，仅当被引用类型与引用它的 {@code @ConfigurationProperties} 类\n * 位于不同模块时才需要此注解。当两者在同一模块且源码可用时，\n * 注解处理器可自动发现完整元数据。\n * <p>\n * 当需要为模块外类型的元数据生成时使用此注解：\n * <ol>\n * <li>由 {@code @NestedConfigurationProperty} 标注的嵌套类型</li>\n * <li>{@code @ConfigurationProperties} 标注类型所继承的基类</li>\n * </ol>\n * <p>\n * 下例中，{@code ServerProperties} 位于模块 \"A\"，{@code Host} 位于模块 \"B\"：<pre><code class=\"java\">\n * &#064;ConfigurationProperties(\"example.server\")\n * class ServerProperties {\n *\n *     &#064;NestedConfigurationProperty\n *     private final Host host = new Host();\n *\n *     public Host getHost() { ... }\n *\n *     // Other properties, getter, setter.\n *\n * }</code></pre>\n * <p>\n * 基于类型可检测到 {@code Host} 的属性，但缺少描述与默认值。\n * 为此，若模块 \"B\" 尚未引入 {@code spring-boot-configuration-processor}，\n * 请添加该依赖并按如下方式更新 {@code Host}：<pre><code class=\"java\">\n * &#064;ConfigurationPropertiesSource\n * class Host {\n *\n *     /**\n *      * 要使用的 URL。\n *      *&#47;\n *     private String url = \"https://example.com\";\n *\n *     // Other properties, getter, setter.\n *\n * }</code></pre>\n * <p>\n * 类似地，{@code @ConfigurationProperties} 标注类型所继承的基类元数据也可被检测。\n * 示例：<pre><code class=\"java\">\n * &#064;ConfigurationProperties(\"example.client.github\")\n * class GitHubClientProperties extends AbstractClientProperties {\n *\n *     // Additional properties, getter, setter.\n *\n * }</code></pre>\n * <p>\n * 与嵌套类型相同，在 {@code AbstractClientProperties} 上添加\n * {@code @ConfigurationPropertiesSource}，并在其模块中引入\n * {@code spring-boot-configuration-processor}，即可确保生成完整元数据。\n *\n * @author Stephane Nicoll\n * @since 4.0.0\n */",
        ),
    ],
    "ConstructorBound.java": [
        (
            "/**\n * Helper class to programmatically bind configuration properties that use constructor\n * injection.\n *\n * @author Stephane Nicoll\n * @since 3.0.0\n * @see ConstructorBinding\n */",
            "/**\n * 用于以编程方式绑定采用构造器注入的配置属性的辅助类。\n *\n * @author Stephane Nicoll\n * @since 3.0.0\n * @see ConstructorBinding\n */",
        ),
        (
            "/**\n\t * Create an immutable {@link ConfigurationProperties} instance for the specified\n\t * {@code beanName} and {@code beanType} using the specified {@link BeanFactory}.\n\t * @param beanFactory the bean factory to use\n\t * @param beanName the name of the bean\n\t * @param beanType the type of the bean\n\t * @return an instance from the specified bean\n\t */",
            "/**\n\t * 使用指定 {@link BeanFactory} 为给定 {@code beanName} 与 {@code beanType}\n\t * 创建不可变的 {@link ConfigurationProperties} 实例。\n\t *\n\t * @param beanFactory 要使用的 Bean 工厂\n\t * @param beanName Bean 名称\n\t * @param beanType Bean 类型\n\t * @return 指定 Bean 的实例\n\t */",
        ),
    ],
    "ConversionServiceDeducer.java": [
        (
            "/**\n * Utility to deduce the {@link ConversionService} to use for configuration properties\n * binding.\n *\n * @author Phillip Webb\n */",
            "/**\n * 推断配置属性绑定应使用的 {@link ConversionService} 的工具类。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "DeprecatedConfigurationProperty.java": [
        (
            "/**\n * Indicates that a getter in a {@link ConfigurationProperties @ConfigurationProperties}\n * object is deprecated. This annotation has no bearing on the actual binding processes,\n * but it is used by the {@code spring-boot-configuration-processor} to add deprecation\n * meta-data.\n * <p>\n * This annotation <strong>must</strong> be used on the getter of the deprecated element.\n *\n * @author Phillip Webb\n * @author Scott Frederick\n * @since 1.3.0\n */",
            "/**\n * 标记 {@link ConfigurationProperties @ConfigurationProperties} 对象中某个 getter 已弃用。\n * 此注解不影响实际绑定过程，但供 {@code spring-boot-configuration-processor}\n * 添加弃用元数据。\n * <p>\n * 此注解<strong>必须</strong>标注在已弃用元素的 getter 上。\n *\n * @author Phillip Webb\n * @author Scott Frederick\n * @since 1.3.0\n */",
        ),
        (
            "/**\n\t * The reason for the deprecation.\n\t * @return the deprecation reason\n\t */",
            "/**\n\t * 弃用原因。\n\t *\n\t * @return 弃用原因\n\t */",
        ),
        (
            "/**\n\t * The field that should be used instead (if any).\n\t * @return the replacement field\n\t */",
            "/**\n\t * 应改用的字段（若有）。\n\t *\n\t * @return 替代字段\n\t */",
        ),
        (
            "/**\n\t * The version in which the property became deprecated.\n\t * @return the version\n\t */",
            "/**\n\t * 属性开始弃用的版本。\n\t *\n\t * @return 版本号\n\t */",
        ),
    ],
    "EnableConfigurationProperties.java": [
        (
            "/**\n * Enable support for {@link ConfigurationProperties @ConfigurationProperties} annotated\n * beans. {@code @ConfigurationProperties} beans can be registered in the standard way\n * (for example using {@link Bean @Bean} methods) or, for convenience, can be specified\n * directly on this annotation.\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
            "/**\n * 启用对 {@link ConfigurationProperties @ConfigurationProperties} 标注 Bean 的支持。\n * {@code @ConfigurationProperties} Bean 可按常规方式注册（例如通过 {@link Bean @Bean} 方法），\n * 也可为方便起见直接在此注解上指定。\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * The bean name of the configuration properties validator.\n\t * @since 2.2.0\n\t */",
            "/**\n\t * 配置属性校验器的 Bean 名称。\n\t *\n\t * @since 2.2.0\n\t */",
        ),
        (
            "/**\n\t * Convenient way to quickly register\n\t * {@link ConfigurationProperties @ConfigurationProperties} annotated beans with\n\t * Spring. Standard Spring Beans will also be scanned regardless of this value.\n\t * @return {@code @ConfigurationProperties} annotated beans to register\n\t */",
            "/**\n\t * 快速向 Spring 注册 {@link ConfigurationProperties @ConfigurationProperties}\n\t * 标注 Bean 的便捷方式。无论此值如何，标准 Spring Bean 仍会被扫描。\n\t *\n\t * @return 要注册的 {@code @ConfigurationProperties} 标注 Bean\n\t */",
        ),
    ],
    "EnableConfigurationPropertiesRegistrar.java": [
        (
            "/**\n * {@link ImportBeanDefinitionRegistrar} for\n * {@link EnableConfigurationProperties @EnableConfigurationProperties}.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n */",
            "/**\n * 用于 {@link EnableConfigurationProperties @EnableConfigurationProperties} 的\n * {@link ImportBeanDefinitionRegistrar}。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n */",
        ),
    ],
    "IncompatibleConfigurationException.java": [
        (
            "/**\n * Exception thrown when the application has configured an incompatible set of\n * {@link ConfigurationProperties} keys.\n *\n * @author Brian Clozel\n * @since 2.4.0\n */",
            "/**\n * 当应用配置了互不兼容的 {@link ConfigurationProperties} 键集合时抛出的异常。\n *\n * @author Brian Clozel\n * @since 2.4.0\n */",
        ),
    ],
    "IncompatibleConfigurationFailureAnalyzer.java": [
        (
            "/**\n * A {@code FailureAnalyzer} that performs analysis of failures caused by a\n * {@code IncompatibleConfigurationException}.\n *\n * @author Brian Clozel\n */",
            "/**\n * 分析由 {@code IncompatibleConfigurationException} 导致失败的 {@code FailureAnalyzer}。\n *\n * @author Brian Clozel\n */",
        ),
    ],
    "NestedConfigurationProperty.java": [
        (
            "/**\n * Indicates that a property in a {@link ConfigurationProperties @ConfigurationProperties}\n * object should be treated as if it were a nested type. This annotation has no bearing on\n * the actual binding processes, but it is used by the\n * {@code spring-boot-configuration-processor} as a hint that a property is not bound as a\n * single value. When this is specified, a nested group is created for the property and\n * its type is harvested.\n * <p>\n * In the example below, {@code Host} is flagged as a nested property using its field and\n * an {@code example.server.host} nested group is created with any property that\n * {@code Host} defines:<pre><code class=\"java\">\n * &#064;ConfigurationProperties(\"example.server\")\n * class ServerProperties {\n *\n *     &#064;NestedConfigurationProperty\n *     private final Host host = new Host();\n *\n *     public Host getHost() { ... }\n *\n *     // Other properties, getter, setter.\n *\n * }</code></pre>\n * <p>\n * The annotation can also be specified on a getter method. If you use records, you can\n * annotate the record component.\n * <p>\n * This has no effect on collections and maps as these types are automatically identified.\n * Also, the annotation is not necessary if the target type is an inner class of the\n * {@link ConfigurationProperties @ConfigurationProperties} object. In the example below,\n * {@code Host} is detected as a nested type as it is defined as an inner class:\n * <pre><code class=\"java\">\n * &#064;ConfigurationProperties(\"example.server\")\n * class ServerProperties {\n *\n *     private final Host host = new Host();\n *\n *     public Host getHost() { ... }\n *\n *     // Other properties, getter, setter.\n *\n *     public static class Host {\n *\n *         // properties, getter, setter.\n *\n *     }\n *\n * }</code></pre>\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @author Jared Bates\n * @since 1.2.0\n */",
            "/**\n * 标记 {@link ConfigurationProperties @ConfigurationProperties} 对象中的属性\n * 应视为嵌套类型。此注解不影响实际绑定过程，但供\n * {@code spring-boot-configuration-processor} 提示该属性并非作为单个值绑定。\n * 指定后会为该属性创建嵌套组并收集其类型信息。\n * <p>\n * 下例中，{@code Host} 通过字段标记为嵌套属性，并为 {@code Host} 定义的属性\n * 创建 {@code example.server.host} 嵌套组：<pre><code class=\"java\">\n * &#064;ConfigurationProperties(\"example.server\")\n * class ServerProperties {\n *\n *     &#064;NestedConfigurationProperty\n *     private final Host host = new Host();\n *\n *     public Host getHost() { ... }\n *\n *     // Other properties, getter, setter.\n *\n * }</code></pre>\n * <p>\n * 也可标注在 getter 方法上；使用 record 时可标注 record 组件。\n * <p>\n * 对集合与 Map 无效，这些类型会自动识别。若目标类型是\n * {@link ConfigurationProperties @ConfigurationProperties} 对象的内部类，\n * 则无需此注解。下例中 {@code Host} 作为内部类定义，会被自动识别为嵌套类型：\n * <pre><code class=\"java\">\n * &#064;ConfigurationProperties(\"example.server\")\n * class ServerProperties {\n *\n *     private final Host host = new Host();\n *\n *     public Host getHost() { ... }\n *\n *     // Other properties, getter, setter.\n *\n *     public static class Host {\n *\n *         // properties, getter, setter.\n *\n *     }\n *\n * }</code></pre>\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @author Jared Bates\n * @since 1.2.0\n */",
        ),
    ],
    "NotConstructorBoundInjectionFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractInjectionFailureAnalyzer} for\n * {@link ConfigurationProperties @ConfigurationProperties} that are intended to use\n * {@link ConstructorBinding constructor binding} but did not.\n *\n * @author Andy Wilkinson\n */",
            "/**\n * 针对本应使用 {@link ConstructorBinding 构造器绑定} 但未正确配置的\n * {@link ConfigurationProperties @ConfigurationProperties} 的\n * {@link AbstractInjectionFailureAnalyzer}。\n *\n * @author Andy Wilkinson\n */",
        ),
    ],
    "PropertySourcesDeducer.java": [
        (
            "/**\n * Utility to deduce the {@link PropertySources} to use for configuration binding.\n *\n * @author Phillip Webb\n */",
            "/**\n * 推断配置绑定应使用的 {@link PropertySources} 的工具类。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "AbstractBindHandler.java": [
        (
            "/**\n * Abstract base class for {@link BindHandler} implementations.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * {@link BindHandler} 实现的抽象基类。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * Create a new binding handler instance.\n\t */",
            "/**\n\t * 创建新的绑定处理器实例。\n\t */",
        ),
        (
            "/**\n\t * Create a new binding handler instance with a specific parent.\n\t * @param parent the parent handler\n\t */",
            "/**\n\t * 使用指定父处理器创建新的绑定处理器实例。\n\t *\n\t * @param parent 父处理器\n\t */",
        ),
    ],
    "AggregateBinder.java": [
        (
            "/**\n * Internal strategy used by {@link Binder} to bind aggregates (Maps, Lists, Arrays).\n *\n * @param <T> the type being bound\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * {@link Binder} 用于绑定聚合类型（Map、List、数组）的内部策略。\n *\n * @param <T> 被绑定的类型\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
        (
            "/**\n\t * Determine if recursive binding is supported.\n\t * @param source the configuration property source or {@code null} for all sources.\n\t * @return if recursive binding is supported\n\t */",
            "/**\n\t * 判断是否支持递归绑定。\n\t *\n\t * @param source 配置属性源，或 {@code null} 表示所有源\n\t * @return 是否支持递归绑定\n\t */",
        ),
        (
            "/**\n\t * Perform binding for the aggregate.\n\t * @param name the configuration property name to bind\n\t * @param target the target to bind\n\t * @param elementBinder an element binder\n\t * @return the bound aggregate or null\n\t */",
            "/**\n\t * 执行聚合类型的绑定。\n\t *\n\t * @param name 要绑定的配置属性名\n\t * @param target 绑定目标\n\t * @param elementBinder 元素绑定器\n\t * @return 绑定后的聚合对象，或 null\n\t */",
        ),
        (
            "/**\n\t * Perform the actual aggregate binding.\n\t * @param name the configuration property name to bind\n\t * @param target the target to bind\n\t * @param elementBinder an element binder\n\t * @return the bound result\n\t */",
            "/**\n\t * 执行实际的聚合绑定。\n\t *\n\t * @param name 要绑定的配置属性名\n\t * @param target 绑定目标\n\t * @param elementBinder 元素绑定器\n\t * @return 绑定结果\n\t */",
        ),
        (
            "/**\n\t * Merge any additional elements into the existing aggregate.\n\t * @param existing the supplier for the existing value\n\t * @param additional the additional elements to merge\n\t * @return the merged result\n\t */",
            "/**\n\t * 将额外元素合并到现有聚合中。\n\t *\n\t * @param existing 现有值的供应器\n\t * @param additional 要合并的额外元素\n\t * @return 合并后的结果\n\t */",
        ),
        (
            "/**\n\t * Return the context being used by this binder.\n\t * @return the context\n\t */",
            "/**\n\t * 返回此绑定器使用的上下文。\n\t *\n\t * @return 上下文\n\t */",
        ),
        (
            "/**\n\t * Internal class used to supply the aggregate and cache the value.\n\t *\n\t * @param <T> the aggregate type\n\t */",
            "/**\n\t * 用于提供聚合对象并缓存值的内部类。\n\t *\n\t * @param <T> 聚合类型\n\t */",
        ),
    ],
    "AggregateElementBinder.java": [
        (
            "/**\n * Binder that can be used by {@link AggregateBinder} implementations to recursively bind\n * elements.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 供 {@link AggregateBinder} 实现递归绑定元素时使用的绑定器。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
        (
            "/**\n\t * Bind the given name to a target bindable.\n\t * @param name the name to bind\n\t * @param target the target bindable\n\t * @return a bound object or {@code null}\n\t */",
            "/**\n\t * 将给定名称绑定到目标 Bindable。\n\t *\n\t * @param name 要绑定的名称\n\t * @param target 目标 Bindable\n\t * @return 绑定后的对象，或 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Bind the given name to a target bindable using optionally limited to a single\n\t * source.\n\t * @param name the name to bind\n\t * @param target the target bindable\n\t * @param source the source of the elements or {@code null} to use all sources\n\t * @return a bound object or {@code null}\n\t */",
            "/**\n\t * 将给定名称绑定到目标 Bindable，可限定为单一属性源。\n\t *\n\t * @param name 要绑定的名称\n\t * @param target 目标 Bindable\n\t * @param source 元素来源，或 {@code null} 表示使用所有源\n\t * @return 绑定后的对象，或 {@code null}\n\t */",
        ),
    ],
    "ArrayBinder.java": [
        (
            "/**\n * {@link AggregateBinder} for arrays.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 用于数组的 {@link AggregateBinder}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
    ],
    "BindConstructorProvider.java": [
        (
            "/**\n * Strategy interface used to determine a specific constructor to use when binding.\n *\n * @author Madhura Bhave\n * @since 2.2.1\n */",
            "/**\n * 用于确定绑定时使用哪个构造器的策略接口。\n *\n * @author Madhura Bhave\n * @since 2.2.1\n */",
        ),
        (
            "/**\n\t * Default {@link BindConstructorProvider} implementation that only returns a value\n\t * when there's a single constructor and when the bindable has no existing value.\n\t */",
            "/**\n\t * 默认 {@link BindConstructorProvider} 实现：仅当存在唯一构造器且\n\t * Bindable 尚无现有值时才返回构造器。\n\t */",
        ),
        (
            "/**\n\t * Return the bind constructor to use for the given type, or {@code null} if\n\t * constructor binding is not supported.\n\t * @param type the type to check\n\t * @param isNestedConstructorBinding if this binding is nested within a constructor\n\t * binding\n\t * @return the bind constructor or {@code null}\n\t * @since 3.0.0\n\t */",
            "/**\n\t * 返回给定类型应使用的绑定构造器；不支持构造器绑定时返回 {@code null}。\n\t *\n\t * @param type 要检查的类型\n\t * @param isNestedConstructorBinding 是否为构造器绑定内的嵌套绑定\n\t * @return 绑定构造器，或 {@code null}\n\t * @since 3.0.0\n\t */",
        ),
        (
            "/**\n\t * Return the bind constructor to use for the given bindable, or {@code null} if\n\t * constructor binding is not supported.\n\t * @param bindable the bindable to check\n\t * @param isNestedConstructorBinding if this binding is nested within a constructor\n\t * binding\n\t * @return the bind constructor or {@code null}\n\t */",
            "/**\n\t * 返回给定 Bindable 应使用的绑定构造器；不支持构造器绑定时返回 {@code null}。\n\t *\n\t * @param bindable 要检查的 Bindable\n\t * @param isNestedConstructorBinding 是否为构造器绑定内的嵌套绑定\n\t * @return 绑定构造器，或 {@code null}\n\t */",
        ),
    ],
    "BindContext.java": [
        (
            "/**\n * Context information for use by {@link BindHandler BindHandlers}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 供 {@link BindHandler BindHandler} 使用的上下文信息。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * Return the source binder that is performing the bind operation.\n\t * @return the source binder\n\t */",
            "/**\n\t * 返回正在执行绑定操作的源 Binder。\n\t *\n\t * @return 源 Binder\n\t */",
        ),
        (
            "/**\n\t * Return the current depth of the binding. Root binding starts with a depth of\n\t * {@code 0}. Each subsequent property binding increases the depth by {@code 1}.\n\t * @return the depth of the current binding\n\t */",
            "/**\n\t * 返回当前绑定深度。根绑定从 {@code 0} 开始，每绑定一层属性深度加 {@code 1}。\n\t *\n\t * @return 当前绑定深度\n\t */",
        ),
        (
            "/**\n\t * Return an {@link Iterable} of the {@link ConfigurationPropertySource sources} being\n\t * used by the {@link Binder}.\n\t * @return the sources\n\t */",
            "/**\n\t * 返回 {@link Binder} 正在使用的 {@link ConfigurationPropertySource 属性源} 集合。\n\t *\n\t * @return 属性源\n\t */",
        ),
        (
            "/**\n\t * Return the {@link ConfigurationProperty} actually being bound or {@code null} if\n\t * the property has not yet been determined.\n\t * @return the configuration property (may be {@code null}).\n\t */",
            "/**\n\t * 返回实际正在绑定的 {@link ConfigurationProperty}；属性尚未确定时返回 {@code null}。\n\t *\n\t * @return 配置属性（可能为 {@code null}）\n\t */",
        ),
    ],
    "BindConverter.java": [
        (
            "/**\n * Utility to handle any conversion needed during binding. This class is not thread-safe\n * and so a new instance is created for each top-level bind.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n */",
            "/**\n * 处理绑定过程中所需类型转换的工具类。此类非线程安全，\n * 因此每次顶层绑定都会创建新实例。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n */",
        ),
        (
            "/**\n\t * A {@link TypeDescriptor} backed by a {@link ResolvableType}.\n\t */",
            "/**\n\t * 由 {@link ResolvableType} 支持的 {@link TypeDescriptor}。\n\t */",
        ),
        (
            "/**\n\t * A {@link ConversionService} implementation that delegates to a\n\t * {@link SimpleTypeConverter}. Allows {@link PropertyEditor} based conversion for\n\t * simple types, arrays and collections.\n\t */",
            "/**\n\t * 委托给 {@link SimpleTypeConverter} 的 {@link ConversionService} 实现。\n\t * 支持简单类型、数组与集合的 {@link PropertyEditor} 转换。\n\t */",
        ),
        (
            "/**\n\t * {@link ConditionalGenericConverter} that delegates to {@link SimpleTypeConverter}.\n\t */",
            "/**\n\t * 委托给 {@link SimpleTypeConverter} 的 {@link ConditionalGenericConverter}。\n\t */",
        ),
    ],
    "BindException.java": [
        (
            "/**\n * Exception thrown when binding fails.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 绑定失败时抛出的异常。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * Return the name of the configuration property being bound.\n\t * @return the configuration property name\n\t */",
            "/**\n\t * 返回正在绑定的配置属性名。\n\t *\n\t * @return 配置属性名\n\t */",
        ),
        (
            "/**\n\t * Return the target being bound.\n\t * @return the bind target\n\t */",
            "/**\n\t * 返回绑定目标。\n\t *\n\t * @return 绑定目标\n\t */",
        ),
        (
            "/**\n\t * Return the configuration property name of the item that was being bound.\n\t * @return the configuration property name\n\t */",
            "/**\n\t * 返回正在绑定项的配置属性。\n\t *\n\t * @return 配置属性\n\t */",
        ),
    ],
    "BindHandler.java": [
        (
            "/**\n * Callback interface that can be used to handle additional logic during element\n * {@link Binder binding}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 在元素 {@link Binder 绑定} 过程中处理额外逻辑的回调接口。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * Default no-op bind handler.\n\t */",
            "/**\n\t * 默认的空操作绑定处理器。\n\t */",
        ),
        (
            "/**\n\t * Called when binding of an element starts but before any result has been determined.\n\t * @param <T> the bindable source type\n\t * @param name the name of the element being bound\n\t * @param target the item being bound\n\t * @param context the bind context\n\t * @return the actual item that should be used for binding (may be {@code null})\n\t */",
            "/**\n\t * 元素绑定开始时、尚未确定结果前调用。\n\t *\n\t * @param <T> Bindable 源类型\n\t * @param name 正在绑定的元素名称\n\t * @param target 正在绑定的项\n\t * @param context 绑定上下文\n\t * @return 实际用于绑定的项（可能为 {@code null}）\n\t */",
        ),
        (
            "/**\n\t * Called when binding of an element ends with a successful result. Implementations\n\t * may change the ultimately returned result or perform addition validation.\n\t * @param name the name of the element being bound\n\t * @param target the item being bound\n\t * @param context the bind context\n\t * @param result the bound result (never {@code null})\n\t * @return the actual result that should be used (may be {@code null})\n\t */",
            "/**\n\t * 元素绑定成功结束时调用。实现可修改最终返回结果或执行额外校验。\n\t *\n\t * @param name 正在绑定的元素名称\n\t * @param target 正在绑定的项\n\t * @param context 绑定上下文\n\t * @param result 绑定结果（永不为 {@code null}）\n\t * @return 实际应使用的结果（可能为 {@code null}）\n\t */",
        ),
        (
            "/**\n\t * Called when binding of an element ends with an unbound result and a newly created\n\t * instance is about to be returned. Implementations may change the ultimately\n\t * returned result or perform addition validation.\n\t * @param name the name of the element being bound\n\t * @param target the item being bound\n\t * @param context the bind context\n\t * @param result the newly created instance (never {@code null})\n\t * @return the actual result that should be used (must not be {@code null})\n\t * @since 2.2.2\n\t */",
            "/**\n\t * 元素绑定未产生绑定值、即将返回新创建实例时调用。实现可修改最终返回结果或执行额外校验。\n\t *\n\t * @param name 正在绑定的元素名称\n\t * @param target 正在绑定的项\n\t * @param context 绑定上下文\n\t * @param result 新创建的实例（永不为 {@code null}）\n\t * @return 实际应使用的结果（不得为 {@code null}）\n\t * @since 2.2.2\n\t */",
        ),
        (
            "/**\n\t * Called when binding fails for any reason (including failures from\n\t * {@link #onSuccess} or {@link #onCreate} calls). Implementations may choose to\n\t * swallow exceptions and return an alternative result.\n\t * @param name the name of the element being bound\n\t * @param target the item being bound\n\t * @param context the bind context\n\t * @param error the cause of the error (if the exception stands it may be re-thrown)\n\t * @return the actual result that should be used (may be {@code null}).\n\t * @throws Exception if the binding isn't valid\n\t */",
            "/**\n\t * 绑定因任何原因失败时调用（包括 {@link #onSuccess} 或 {@link #onCreate} 中的失败）。\n\t * 实现可选择吞掉异常并返回替代结果。\n\t *\n\t * @param name 正在绑定的元素名称\n\t * @param target 正在绑定的项\n\t * @param context 绑定上下文\n\t * @param error 错误原因（若异常未被处理则可能被重新抛出）\n\t * @return 实际应使用的结果（可能为 {@code null}）\n\t * @throws Exception 若绑定无效\n\t */",
        ),
        (
            "/**\n\t * Called when binding finishes with either bound or unbound result. This method will\n\t * not be called when binding failed, even if a handler returns a result from\n\t * {@link #onFailure}.\n\t * @param name the name of the element being bound\n\t * @param target the item being bound\n\t * @param context the bind context\n\t * @param result the bound result (may be {@code null})\n\t * @throws Exception if the binding isn't valid\n\t */",
            "/**\n\t * 绑定完成时调用（无论是否产生绑定值）。绑定失败时不调用此方法，\n\t * 即使处理器从 {@link #onFailure} 返回了结果。\n\t *\n\t * @param name 正在绑定的元素名称\n\t * @param target 正在绑定的项\n\t * @param context 绑定上下文\n\t * @param result 绑定结果（可能为 {@code null}）\n\t * @throws Exception 若绑定无效\n\t */",
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
