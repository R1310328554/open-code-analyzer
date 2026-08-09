#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-8a slice [0:20]."""
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
    "ValueObjectBinder.java": [
        (
            "/**\n * {@link DataObjectBinder} for immutable value objects.\n *\n * @author Madhura Bhave\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @author Scott Frederick\n * @author Ondřej Světlík\n */",
            "/**\n * 用于不可变值对象的 {@link DataObjectBinder}。\n *\n * @author Madhura Bhave\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @author Scott Frederick\n * @author Ondřej Světlík\n */",
        ),
        (
            "/**\n\t * The value object being bound.\n\t *\n\t * @param <T> the value object type\n\t */",
            "/**\n\t * 正在绑定的值对象。\n\t *\n\t * @param <T> 值对象类型\n\t */",
        ),
        (
            "/**\n\t * A {@link ValueObject} implementation that is aware of Kotlin specific constructs.\n\t */",
            "/**\n\t * 了解 Kotlin 特有构造的 {@link ValueObject} 实现。\n\t */",
        ),
        (
            "/**\n\t * A default {@link ValueObject} implementation that uses only standard Java\n\t * reflection calls.\n\t */",
            "/**\n\t * 仅使用标准 Java 反射调用的默认 {@link ValueObject} 实现。\n\t */",
        ),
        (
            "/**\n\t * A constructor parameter being bound.\n\t */",
            "/**\n\t * 正在绑定的构造器参数。\n\t */",
        ),
        (
            "/**\n\t * {@link ParameterNameDiscoverer} used for value data object binding.\n\t */",
            "/**\n\t * 用于值对象绑定的 {@link ParameterNameDiscoverer}。\n\t */",
        ),
    ],
    "IgnoreErrorsBindHandler.java": [
        (
            "/**\n * {@link BindHandler} that can be used to ignore binding errors.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 可用于忽略绑定错误的 {@link BindHandler}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
    ],
    "IgnoreTopLevelConverterNotFoundBindHandler.java": [
        (
            "/**\n * {@link BindHandler} that can be used to ignore top-level\n * {@link ConverterNotFoundException}s.\n *\n * @author Madhura Bhave\n * @since 2.0.1\n */",
            "/**\n * 可用于忽略顶层 {@link ConverterNotFoundException} 的 {@link BindHandler}。\n *\n * @author Madhura Bhave\n * @since 2.0.1\n */",
        ),
        (
            "/**\n\t * Create a new {@link IgnoreTopLevelConverterNotFoundBindHandler} instance.\n\t */",
            "/**\n\t * 创建新的 {@link IgnoreTopLevelConverterNotFoundBindHandler} 实例。\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link IgnoreTopLevelConverterNotFoundBindHandler} instance with a\n\t * specific parent.\n\t * @param parent the parent handler\n\t */",
            "/**\n\t * 使用指定父处理器创建新的 {@link IgnoreTopLevelConverterNotFoundBindHandler} 实例。\n\t *\n\t * @param parent 父处理器\n\t */",
        ),
    ],
    "NoUnboundElementsBindHandler.java": [
        (
            "/**\n * {@link BindHandler} to enforce that all configuration properties under the root name\n * have been bound.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 强制要求根名称下所有配置属性均已绑定的 {@link BindHandler}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
    ],
    "BindValidationException.java": [
        (
            "/**\n * Error thrown when validation fails during a bind operation.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see ValidationErrors\n * @see ValidationBindHandler\n */",
            "/**\n * 绑定操作期间校验失败时抛出的错误。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see ValidationErrors\n * @see ValidationBindHandler\n */",
        ),
        (
            "/**\n\t * Return the validation errors that caused the exception.\n\t * @return the validationErrors the validation errors\n\t */",
            "/**\n\t * 返回导致此异常的校验错误。\n\t *\n\t * @return the validationErrors 校验错误\n\t */",
        ),
    ],
    "OriginTrackedFieldError.java": [
        (
            "/**\n * {@link FieldError} implementation that tracks the source {@link Origin}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 跟踪来源 {@link Origin} 的 {@link FieldError} 实现。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
    ],
    "ValidationBindHandler.java": [
        (
            "/**\n * {@link BindHandler} to apply {@link Validator Validators} to bound results.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 对绑定结果应用 {@link Validator 校验器} 的 {@link BindHandler}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * {@link AbstractBindingResult} implementation backed by the bound properties.\n\t */",
            "/**\n\t * 由已绑定属性支持的 {@link AbstractBindingResult} 实现。\n\t */",
        ),
    ],
    "ValidationErrors.java": [
        (
            "/**\n * A collection of {@link ObjectError ObjectErrors} caused by bind validation failures.\n * Where possible, included {@link FieldError FieldErrors} will be OriginProvider.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 由绑定校验失败产生的 {@link ObjectError ObjectError} 集合。\n * 在可能的情况下，包含的 {@link FieldError FieldError} 将实现 OriginProvider。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * Return the name of the item that was being validated.\n\t * @return the name of the item\n\t */",
            "/**\n\t * 返回正在校验的项的名称。\n\t *\n\t * @return the name of the item 项的名称\n\t */",
        ),
        (
            "/**\n\t * Return the properties that were bound before validation failed.\n\t * @return the boundProperties\n\t */",
            "/**\n\t * 返回校验失败前已绑定的属性。\n\t *\n\t * @return the boundProperties 已绑定的属性\n\t */",
        ),
        (
            "/**\n\t * Return the list of all validation errors.\n\t * @return the errors\n\t */",
            "/**\n\t * 返回所有校验错误的列表。\n\t *\n\t * @return the errors 错误列表\n\t */",
        ),
    ],
    "AliasedConfigurationPropertySource.java": [
        (
            "/**\n * A {@link ConfigurationPropertySource} supporting name aliases.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 支持名称别名的 {@link ConfigurationPropertySource}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
    ],
    "AliasedIterableConfigurationPropertySource.java": [
        (
            "/**\n * A {@link IterableConfigurationPropertySource} supporting name aliases.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 支持名称别名的 {@link IterableConfigurationPropertySource}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
    ],
    "CachingConfigurationPropertySource.java": [
        (
            "/**\n * Interface used to indicate that a {@link ConfigurationPropertySource} supports\n * {@link ConfigurationPropertyCaching}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 用于标识 {@link ConfigurationPropertySource} 支持 {@link ConfigurationPropertyCaching} 的接口。\n *\n * @author Phillip Webb\n */",
        ),
        (
            "/**\n\t * Return {@link ConfigurationPropertyCaching} for this source.\n\t * @return source caching\n\t */",
            "/**\n\t * 返回此属性源的 {@link ConfigurationPropertyCaching}。\n\t *\n\t * @return source caching 属性源缓存\n\t */",
        ),
        (
            "/**\n\t * Find {@link ConfigurationPropertyCaching} for the given source.\n\t * @param source the configuration property source\n\t * @return a {@link ConfigurationPropertyCaching} instance or {@code null} if the\n\t * source does not support caching.\n\t */",
            "/**\n\t * 查找给定属性源对应的 {@link ConfigurationPropertyCaching}。\n\t *\n\t * @param source 配置属性源\n\t * @return {@link ConfigurationPropertyCaching} 实例；若属性源不支持缓存则返回 {@code null}\n\t */",
        ),
    ],
    "ConfigurationProperty.java": [
        (
            "/**\n * A single configuration property obtained from a {@link ConfigurationPropertySource}\n * consisting of a {@link #getName() name}, {@link #getValue() value} and optional\n * {@link #getOrigin() origin}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 从 {@link ConfigurationPropertySource} 获取的单个配置属性，\n * 包含 {@link #getName() 名称}、{@link #getValue() 值} 以及可选的 {@link #getOrigin() 来源}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * Return the {@link ConfigurationPropertySource} that provided the property or\n\t * {@code null} if the source is unknown.\n\t * @return the configuration property source\n\t * @since 2.6.0\n\t */",
            "/**\n\t * 返回提供该属性的 {@link ConfigurationPropertySource}；来源未知时返回 {@code null}。\n\t *\n\t * @return the configuration property source 配置属性源\n\t * @since 2.6.0\n\t */",
        ),
        (
            "/**\n\t * Return the name of the configuration property.\n\t * @return the configuration property name\n\t */",
            "/**\n\t * 返回配置属性的名称。\n\t *\n\t * @return the configuration property name 配置属性名\n\t */",
        ),
        (
            "/**\n\t * Return the value of the configuration property.\n\t * @return the configuration property value\n\t */",
            "/**\n\t * 返回配置属性的值。\n\t *\n\t * @return the configuration property value 配置属性值\n\t */",
        ),
    ],
    "ConfigurationPropertyCaching.java": [
        (
            "/**\n * Interface that can be used to control configuration property source caches.\n *\n * @author Phillip Webb\n * @since 2.3.0\n */",
            "/**\n * 用于控制配置属性源缓存的接口。\n *\n * @author Phillip Webb\n * @since 2.3.0\n */",
        ),
        (
            "/**\n\t * Enable caching with an unlimited time-to-live.\n\t */",
            "/**\n\t * 启用缓存，生存时间不限。\n\t */",
        ),
        (
            "/**\n\t * Disable caching.\n\t */",
            "/**\n\t * 禁用缓存。\n\t */",
        ),
        (
            "/**\n\t * Set amount of time that an item can live in the cache. Calling this method will\n\t * also enable the cache.\n\t * @param timeToLive the time to live value.\n\t */",
            "/**\n\t * 设置缓存项的生存时间。调用此方法也会启用缓存。\n\t *\n\t * @param timeToLive 生存时间\n\t */",
        ),
        (
            "/**\n\t * Clear the cache and force it to be reloaded on next access.\n\t */",
            "/**\n\t * 清空缓存，并在下次访问时强制重新加载。\n\t */",
        ),
        (
            "/**\n\t * Override caching to temporarily enable it. Once caching is no longer needed the\n\t * returned {@link CacheOverride} should be closed to restore previous cache settings.\n\t * @return a {@link CacheOverride}\n\t * @since 3.5.0\n\t */",
            "/**\n\t * 临时覆盖并启用缓存。不再需要缓存时应关闭返回的 {@link CacheOverride} 以恢复先前设置。\n\t *\n\t * @return a {@link CacheOverride} 缓存覆盖句柄\n\t * @since 3.5.0\n\t */",
        ),
        (
            "/**\n\t * Get for all configuration property sources in the environment.\n\t * @param environment the spring environment\n\t * @return a caching instance that controls all sources in the environment\n\t */",
            "/**\n\t * 获取环境中所有配置属性源的缓存控制器。\n\t *\n\t * @param environment Spring 环境\n\t * @return 控制环境中所有属性源的缓存实例\n\t */",
        ),
        (
            "/**\n\t * Get for a specific configuration property source in the environment.\n\t * @param environment the spring environment\n\t * @param underlyingSource the\n\t * {@link ConfigurationPropertySource#getUnderlyingSource() underlying source} that\n\t * must match\n\t * @return a caching instance that controls the matching source\n\t */",
            "/**\n\t * 获取环境中特定配置属性源的缓存控制器。\n\t *\n\t * @param environment Spring 环境\n\t * @param underlyingSource 必须匹配的\n\t * {@link ConfigurationPropertySource#getUnderlyingSource() 底层源}\n\t * @return 控制匹配属性源的缓存实例\n\t */",
        ),
        (
            "/**\n\t * Get for all specified configuration property sources.\n\t * @param sources the configuration property sources\n\t * @return a caching instance that controls the sources\n\t */",
            "/**\n\t * 获取指定配置属性源集合的缓存控制器。\n\t *\n\t * @param sources 配置属性源\n\t * @return 控制这些属性源的缓存实例\n\t */",
        ),
        (
            "/**\n\t * Get for a specific configuration property source in the specified configuration\n\t * property sources.\n\t * @param sources the configuration property sources\n\t * @param underlyingSource the\n\t * {@link ConfigurationPropertySource#getUnderlyingSource() underlying source} that\n\t * must match\n\t * @return a caching instance that controls the matching source\n\t */",
            "/**\n\t * 获取指定配置属性源集合中特定属性源的缓存控制器。\n\t *\n\t * @param sources 配置属性源\n\t * @param underlyingSource 必须匹配的\n\t * {@link ConfigurationPropertySource#getUnderlyingSource() 底层源}\n\t * @return 控制匹配属性源的缓存实例\n\t */",
        ),
        (
            "/**\n\t * {@link AutoCloseable} used to control a\n\t * {@link ConfigurationPropertyCaching#override() cache override}.\n\t *\n\t * @since 3.5.0\n\t */",
            "/**\n\t * 用于控制 {@link ConfigurationPropertyCaching#override() 缓存覆盖} 的 {@link AutoCloseable}。\n\t *\n\t * @since 3.5.0\n\t */",
        ),
    ],
    "ConfigurationPropertyNameAliases.java": [
        (
            "/**\n * Maintains a mapping of {@link ConfigurationPropertyName} aliases.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see ConfigurationPropertySource#withAliases(ConfigurationPropertyNameAliases)\n */",
            "/**\n * 维护 {@link ConfigurationPropertyName} 别名映射。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see ConfigurationPropertySource#withAliases(ConfigurationPropertyNameAliases)\n */",
        ),
    ],
    "ConfigurationPropertySource.java": [
        (
            "/**\n * A source of {@link ConfigurationProperty ConfigurationProperties}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see ConfigurationPropertyName\n * @see OriginTrackedValue\n * @see #getConfigurationProperty(ConfigurationPropertyName)\n */",
            "/**\n * {@link ConfigurationProperty 配置属性} 的来源。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see ConfigurationPropertyName\n * @see OriginTrackedValue\n * @see #getConfigurationProperty(ConfigurationPropertyName)\n */",
        ),
        (
            "/**\n\t * Return a single {@link ConfigurationProperty} from the source or {@code null} if no\n\t * property can be found.\n\t * @param name the name of the property\n\t * @return the associated object or {@code null}.\n\t */",
            "/**\n\t * 从属性源返回单个 {@link ConfigurationProperty}；找不到属性时返回 {@code null}。\n\t *\n\t * @param name 属性名称\n\t * @return the associated object or {@code null} 关联对象，或 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Returns if the source contains any descendants of the specified name. May return\n\t * {@link ConfigurationPropertyState#PRESENT} or\n\t * {@link ConfigurationPropertyState#ABSENT} if an answer can be determined or\n\t * {@link ConfigurationPropertyState#UNKNOWN} if it's not possible to determine a\n\t * definitive answer.\n\t * @param name the name to check\n\t * @return if the source contains any descendants\n\t */",
            "/**\n\t * 判断属性源是否包含指定名称的任意后代。若能确定则返回\n\t * {@link ConfigurationPropertyState#PRESENT} 或 {@link ConfigurationPropertyState#ABSENT}；\n\t * 无法确定时返回 {@link ConfigurationPropertyState#UNKNOWN}。\n\t *\n\t * @param name 要检查的名称\n\t * @return if the source contains any descendants 是否包含后代\n\t */",
        ),
        (
            "/**\n\t * Return a filtered variant of this source, containing only names that match the\n\t * given {@link Predicate}.\n\t * @param filter the filter to match\n\t * @return a filtered {@link ConfigurationPropertySource} instance\n\t */",
            "/**\n\t * 返回此属性源的过滤变体，仅包含匹配给定 {@link Predicate} 的名称。\n\t *\n\t * @param filter 匹配过滤器\n\t * @return a filtered {@link ConfigurationPropertySource} instance 过滤后的属性源实例\n\t */",
        ),
        (
            "/**\n\t * Return a variant of this source that supports name aliases.\n\t * @param aliases a function that returns a stream of aliases for any given name\n\t * @return a {@link ConfigurationPropertySource} instance supporting name aliases\n\t */",
            "/**\n\t * 返回支持名称别名的属性源变体。\n\t *\n\t * @param aliases 为给定名称返回别名流的函数\n\t * @return a {@link ConfigurationPropertySource} instance supporting name aliases 支持名称别名的属性源实例\n\t */",
        ),
        (
            "/**\n\t * Return a variant of this source that supports a prefix.\n\t * @param prefix the prefix for properties in the source\n\t * @return a {@link ConfigurationPropertySource} instance supporting a prefix\n\t * @since 2.5.0\n\t */",
            "/**\n\t * 返回支持前缀的属性源变体。\n\t *\n\t * @param prefix 属性源中属性的前缀\n\t * @return a {@link ConfigurationPropertySource} instance supporting a prefix 支持前缀的属性源实例\n\t * @since 2.5.0\n\t */",
        ),
        (
            "/**\n\t * Return the underlying source that is actually providing the properties.\n\t * @return the underlying property source or {@code null}.\n\t */",
            "/**\n\t * 返回实际提供属性的底层源。\n\t *\n\t * @return the underlying property source or {@code null} 底层属性源，或 {@code null}\n\t */",
        ),
        (
            "/**\n\t * Return a single new {@link ConfigurationPropertySource} adapted from the given\n\t * Spring {@link PropertySource} or {@code null} if the source cannot be adapted.\n\t * @param source the Spring property source to adapt\n\t * @return an adapted source or {@code null} {@link SpringConfigurationPropertySource}\n\t * @since 2.4.0\n\t */",
            "/**\n\t * 从给定 Spring {@link PropertySource} 适配出新的 {@link ConfigurationPropertySource}；\n\t * 无法适配时返回 {@code null}。\n\t *\n\t * @param source 要适配的 Spring 属性源\n\t * @return an adapted source or {@code null} {@link SpringConfigurationPropertySource} 适配后的属性源，或 {@code null}\n\t * @since 2.4.0\n\t */",
        ),
    ],
    "ConfigurationPropertySources.java": [
        (
            "/**\n * Provides access to {@link ConfigurationPropertySource ConfigurationPropertySources}.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 提供对 {@link ConfigurationPropertySource 配置属性源} 的访问。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * The name of the {@link PropertySource} {@link #attach(Environment) adapter}.\n\t */",
            "/**\n\t * {@link #attach(Environment) 适配器} 所用 {@link PropertySource} 的名称。\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link PropertyResolver} that resolves property values against an\n\t * underlying set of {@link PropertySources}. Provides an\n\t * {@link ConfigurationPropertySource} aware and optimized alternative to\n\t * {@link PropertySourcesPropertyResolver}.\n\t * @param propertySources the set of {@link PropertySource} objects to use\n\t * @return a {@link ConfigurablePropertyResolver} implementation\n\t * @since 2.5.0\n\t */",
            "/**\n\t * 创建新的 {@link PropertyResolver}，基于底层 {@link PropertySources} 解析属性值。\n\t * 提供感知 {@link ConfigurationPropertySource} 且优于 {@link PropertySourcesPropertyResolver} 的实现。\n\t *\n\t * @param propertySources 要使用的 {@link PropertySource} 集合\n\t * @return a {@link ConfigurablePropertyResolver} implementation 可配置属性解析器实现\n\t * @since 2.5.0\n\t */",
        ),
        (
            "/**\n\t * Determines if the specific {@link PropertySource} is the\n\t * {@link ConfigurationPropertySource} that was {@link #attach(Environment) attached}\n\t * to the {@link Environment}.\n\t * @param propertySource the property source to test\n\t * @return {@code true} if this is the attached {@link ConfigurationPropertySource}\n\t */",
            "/**\n\t * 判断给定 {@link PropertySource} 是否为已 {@link #attach(Environment) 附加} 到\n\t * {@link Environment} 的 {@link ConfigurationPropertySource}。\n\t *\n\t * @param propertySource 要检测的属性源\n\t * @return {@code true} if this is the attached {@link ConfigurationPropertySource} 若为已附加的配置属性源则为 {@code true}\n\t */",
        ),
        (
            "/**\n\t * Attach a {@link ConfigurationPropertySource} support to the specified\n\t * {@link Environment}. Adapts each {@link PropertySource} managed by the environment\n\t * to a {@link ConfigurationPropertySource} and allows classic\n\t * {@link PropertySourcesPropertyResolver} calls to resolve using\n\t * {@link ConfigurationPropertyName configuration property names}.\n\t * <p>\n\t * The attached resolver will dynamically track any additions or removals from the\n\t * underlying {@link Environment} property sources.\n\t * @param environment the source environment (must be an instance of\n\t * {@link ConfigurableEnvironment})\n\t * @see #get(Environment)\n\t */",
            "/**\n\t * 为指定 {@link Environment} 附加 {@link ConfigurationPropertySource} 支持。\n\t * 将环境管理的每个 {@link PropertySource} 适配为 {@link ConfigurationPropertySource}，\n\t * 使经典 {@link PropertySourcesPropertyResolver} 调用可使用\n\t * {@link ConfigurationPropertyName 配置属性名} 解析。\n\t * <p>\n\t * 附加的解析器会动态跟踪底层 {@link Environment} 属性源的增删。\n\t *\n\t * @param environment 源环境（必须是 {@link ConfigurableEnvironment} 实例）\n\t * @see #get(Environment)\n\t */",
        ),
        (
            "/**\n\t * Return a set of {@link ConfigurationPropertySource} instances that have previously\n\t * been {@link #attach(Environment) attached} to the {@link Environment}.\n\t * @param environment the source environment (must be an instance of\n\t * {@link ConfigurableEnvironment})\n\t * @return an iterable set of configuration property sources\n\t * @throws IllegalStateException if not configuration property sources have been\n\t * attached\n\t */",
            "/**\n\t * 返回先前已 {@link #attach(Environment) 附加} 到 {@link Environment} 的\n\t * {@link ConfigurationPropertySource} 实例集合。\n\t *\n\t * @param environment 源环境（必须是 {@link ConfigurableEnvironment} 实例）\n\t * @return an iterable set of configuration property sources 可迭代的配置属性源集合\n\t * @throws IllegalStateException if not configuration property sources have been attached 若尚未附加配置属性源\n\t */",
        ),
        (
            "/**\n\t * Return {@link Iterable} containing a single new {@link ConfigurationPropertySource}\n\t * adapted from the given Spring {@link PropertySource}. The single element can be\n\t * {@code null} if the source cannot be adapted.\n\t * @param source the Spring property source to adapt\n\t * @return an {@link Iterable} containing a single newly adapted\n\t * {@link SpringConfigurationPropertySource}\n\t */",
            "/**\n\t * 返回包含单个新 {@link ConfigurationPropertySource} 的 {@link Iterable}，\n\t * 该实例由给定 Spring {@link PropertySource} 适配而来；无法适配时该元素可为 {@code null}。\n\t *\n\t * @param source 要适配的 Spring 属性源\n\t * @return an {@link Iterable} containing a single newly adapted {@link SpringConfigurationPropertySource} 包含单个新适配实例的可迭代对象\n\t */",
        ),
        (
            "/**\n\t * Return {@link Iterable} containing new {@link ConfigurationPropertySource}\n\t * instances adapted from the given Spring {@link PropertySource PropertySources}.\n\t * <p>\n\t * This method will flatten any nested property sources and will filter all\n\t * {@link StubPropertySource stub property sources}. Updates to the underlying source,\n\t * identified by changes in the sources returned by its iterator, will be\n\t * automatically tracked. The underlying source should be thread safe, for example a\n\t * {@link MutablePropertySources}\n\t * @param sources the Spring property sources to adapt\n\t * @return an {@link Iterable} containing newly adapted\n\t * {@link SpringConfigurationPropertySource} instances\n\t */",
            "/**\n\t * 返回包含由给定 Spring {@link PropertySource PropertySources} 适配的新\n\t * {@link ConfigurationPropertySource} 实例的 {@link Iterable}。\n\t * <p>\n\t * 此方法会扁平化嵌套属性源，并过滤所有 {@link StubPropertySource 桩属性源}。\n\t * 底层源通过迭代器返回的属性源变化自动跟踪更新；底层源应线程安全，例如 {@link MutablePropertySources}。\n\t *\n\t * @param sources 要适配的 Spring 属性源\n\t * @return an {@link Iterable} containing newly adapted {@link SpringConfigurationPropertySource} instances 包含新适配实例的可迭代对象\n\t */",
        ),
    ],
    "ConfigurationPropertySourcesCaching.java": [
        (
            "/**\n * {@link ConfigurationPropertyCaching} for an {@link Iterable iterable} set of\n * {@link ConfigurationPropertySource} instances.\n *\n * @author Phillip Webb\n */",
            "/**\n * 针对 {@link Iterable 可迭代} {@link ConfigurationPropertySource} 实例集合的\n * {@link ConfigurationPropertyCaching}。\n *\n * @author Phillip Webb\n */",
        ),
        (
            "/**\n\t * Composite {@link CacheOverride}.\n\t */",
            "/**\n\t * 复合 {@link CacheOverride}。\n\t */",
        ),
    ],
    "ConfigurationPropertySourcesPropertyResolver.java": [
        (
            "/**\n * Alternative {@link PropertySourcesPropertyResolver} implementation that recognizes\n * {@link ConfigurationPropertySourcesPropertySource} and saves duplicate calls to the\n * underlying sources if the name is a value {@link ConfigurationPropertyName}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 可识别 {@link ConfigurationPropertySourcesPropertySource} 的\n * {@link PropertySourcesPropertyResolver} 替代实现；当名称为有效 {@link ConfigurationPropertyName} 时\n * 可避免对底层属性源的重复调用。\n *\n * @author Phillip Webb\n */",
        ),
        (
            "/**\n\t * Default {@link PropertySourcesPropertyResolver} used if\n\t * {@link ConfigurationPropertySources} is not attached.\n\t */",
            "/**\n\t * 未附加 {@link ConfigurationPropertySources} 时使用的默认 {@link PropertySourcesPropertyResolver}。\n\t */",
        ),
    ],
    "ConfigurationPropertySourcesPropertySource.java": [
        (
            "/**\n * {@link PropertySource} that exposes {@link ConfigurationPropertySource} instances so\n * that they can be used with a {@link PropertyResolver} or added to the\n * {@link Environment}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 暴露 {@link ConfigurationPropertySource} 实例的 {@link PropertySource}，\n * 以便与 {@link PropertyResolver} 配合使用或添加到 {@link Environment}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
    ],
    "ConfigurationPropertyState.java": [
        (
            "/**\n * The state of content from a {@link ConfigurationPropertySource}.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * {@link ConfigurationPropertySource} 中内容的状态。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * The {@link ConfigurationPropertySource} has at least one matching\n\t * {@link ConfigurationProperty}.\n\t */",
            "/**\n\t * {@link ConfigurationPropertySource} 至少有一个匹配的 {@link ConfigurationProperty}。\n\t */",
        ),
        (
            "/**\n\t * The {@link ConfigurationPropertySource} has no matching\n\t * {@link ConfigurationProperty ConfigurationProperties}.\n\t */",
            "/**\n\t * {@link ConfigurationPropertySource} 没有匹配的 {@link ConfigurationProperty 配置属性}。\n\t */",
        ),
        (
            "/**\n\t * It's not possible to determine if {@link ConfigurationPropertySource} has matching\n\t * {@link ConfigurationProperty ConfigurationProperties} or not.\n\t */",
            "/**\n\t * 无法确定 {@link ConfigurationPropertySource} 是否存在匹配的\n\t * {@link ConfigurationProperty 配置属性}。\n\t */",
        ),
        (
            "/**\n\t * Search the given iterable using a predicate to determine if content is\n\t * {@link #PRESENT} or {@link #ABSENT}.\n\t * @param <T> the data type\n\t * @param source the source iterable to search\n\t * @param predicate the predicate used to test for presence\n\t * @return {@link #PRESENT} if the iterable contains a matching item, otherwise\n\t * {@link #ABSENT}.\n\t */",
            "/**\n\t * 使用谓词搜索给定可迭代对象，判断内容是 {@link #PRESENT} 还是 {@link #ABSENT}。\n\t *\n\t * @param <T> 数据类型\n\t * @param source 要搜索的可迭代源\n\t * @param predicate 用于检测是否存在的谓词\n\t * @return {@link #PRESENT} if the iterable contains a matching item, otherwise {@link #ABSENT} 若存在匹配项则为 {@link #PRESENT}，否则为 {@link #ABSENT}\n\t */",
        ),
        (
            "/**\n\t * Search the given iterable using a predicate to determine if content is\n\t * {@link #PRESENT} or {@link #ABSENT}.\n\t * @param <T> the data type\n\t * @param source the source iterable to search\n\t * @param startInclusive the first index to cover\n\t * @param endExclusive index immediately past the last index to cover\n\t * @param predicate the predicate used to test for presence\n\t * @return {@link #PRESENT} if the iterable contains a matching item, otherwise\n\t * {@link #ABSENT}.\n\t */",
            "/**\n\t * 使用谓词搜索给定数组区间，判断内容是 {@link #PRESENT} 还是 {@link #ABSENT}。\n\t *\n\t * @param <T> 数据类型\n\t * @param source 要搜索的数组源\n\t * @param startInclusive 起始索引（含）\n\t * @param endExclusive 结束索引（不含）\n\t * @param predicate 用于检测是否存在的谓词\n\t * @return {@link #PRESENT} if the iterable contains a matching item, otherwise {@link #ABSENT} 若存在匹配项则为 {@link #PRESENT}，否则为 {@link #ABSENT}\n\t */",
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
        if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
            shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            if has_chinese(text):
                cn_chars = len(re.findall(r"[\u4e00-\u9fff]", text))
                if cn_chars > 20:
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
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
