#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-8b batch files [20:40]."""
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
    "DefaultPropertyMapper.java": [
        (
            "/**\n * Default {@link PropertyMapper} implementation. Names are mapped by removing invalid\n * characters and converting to lower case. For example \"{@code my.server_name.PORT}\" is\n * mapped to \"{@code my.servername.port}\".\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see PropertyMapper\n * @see SpringConfigurationPropertySource\n */",
            "/**\n * 默认的 {@link PropertyMapper} 实现。通过移除无效字符并转为小写来映射名称。\n * 例如 \"{@code my.server_name.PORT}\" 映射为 \"{@code my.servername.port}\"。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see PropertyMapper\n * @see SpringConfigurationPropertySource\n */",
        ),
    ],
    "FilteredConfigurationPropertiesSource.java": [
        (
            "/**\n * A filtered {@link ConfigurationPropertySource}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 经过过滤的 {@link ConfigurationPropertySource}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
    ],
    "FilteredIterableConfigurationPropertiesSource.java": [
        (
            "/**\n * A filtered {@link IterableConfigurationPropertySource}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 经过过滤的 {@link IterableConfigurationPropertySource}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
    ],
    "InvalidConfigurationPropertyNameException.java": [
        (
            "/**\n * Exception thrown when {@link ConfigurationPropertyName} has invalid characters.\n *\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 当 {@link ConfigurationPropertyName} 包含无效字符时抛出的异常。\n *\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
    ],
    "InvalidConfigurationPropertyValueException.java": [
        (
            "/**\n * Exception thrown when a configuration property value is invalid.\n *\n * @author Stephane Nicoll\n * @since 2.0.0\n */",
            "/**\n * 当配置属性值无效时抛出的异常。\n *\n * @author Stephane Nicoll\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Creates a new instance for the specified property {@code name} and {@code value},\n\t * including a {@code reason} why the value is invalid.\n\t * @param name the name of the property in canonical format\n\t * @param value the value of the property, can be {@code null}\n\t * @param reason a human-readable text that describes why the value is invalid. Starts\n\t * with an upper-case character and ends with a dot. Several sentences and lines are\n\t * allowed.\n\t */",
            "\t/**\n\t * 为指定属性 {@code name} 与 {@code value} 创建新实例，并包含说明值无效的 {@code reason}。\n\t *\n\t * @param name 规范格式的属性名\n\t * @param value 属性值，可为 {@code null}\n\t * @param reason 描述值无效的可读文本。以大写字母开头并以句号结尾。允许多句与多行。\n\t */",
        ),
        (
            "\t/**\n\t * Creates a new instance for the specified property {@code name} and {@code value},\n\t * including a {@code reason} why the value is invalid.\n\t * @param name the name of the property in canonical format\n\t * @param value the value of the property, can be {@code null}\n\t * @param reason a human-readable text that describes why the value is invalid. Starts\n\t * with an upper-case character and ends with a dot. Several sentences and lines are\n\t * allowed.\n\t * @param cause the cause of the exception or {@code null}\n\t * @since 4.1.0\n\t */",
            "\t/**\n\t * 为指定属性 {@code name} 与 {@code value} 创建新实例，并包含说明值无效的 {@code reason}。\n\t *\n\t * @param name 规范格式的属性名\n\t * @param value 属性值，可为 {@code null}\n\t * @param reason 描述值无效的可读文本。以大写字母开头并以句号结尾。允许多句与多行。\n\t * @param cause 异常原因或 {@code null}\n\t * @since 4.1.0\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the property.\n\t * @return the property name\n\t */",
            "\t/**\n\t * 返回属性名。\n\t *\n\t * @return 属性名\n\t */",
        ),
        (
            "\t/**\n\t * Return the invalid value, can be {@code null}.\n\t * @return the invalid value\n\t */",
            "\t/**\n\t * 返回无效值，可为 {@code null}。\n\t *\n\t * @return 无效值\n\t */",
        ),
        (
            "\t/**\n\t * Return the reason why the value is invalid.\n\t * @return the reason\n\t */",
            "\t/**\n\t * 返回值无效的原因。\n\t *\n\t * @return 原因\n\t */",
        ),
    ],
    "IterableConfigurationPropertySource.java": [
        (
            "/**\n * A {@link ConfigurationPropertySource} with a fully {@link Iterable} set of entries.\n * Implementations of this interface <strong>must</strong> be able to iterate over all\n * contained configuration properties. Any {@code non-null} result from\n * {@link #getConfigurationProperty(ConfigurationPropertyName)} must also have an\n * equivalent entry in the {@link #iterator() iterator}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see ConfigurationPropertyName\n * @see OriginTrackedValue\n * @see #getConfigurationProperty(ConfigurationPropertyName)\n * @see #iterator()\n * @see #stream()\n */",
            "/**\n * 具有完整 {@link Iterable} 条目集的 {@link ConfigurationPropertySource}。\n * 此接口的实现<strong>必须</strong>能够遍历所有包含的配置属性。\n * {@link #getConfigurationProperty(ConfigurationPropertyName)} 返回的任何 {@code non-null} 结果\n * 也必须在 {@link #iterator() 迭代器} 中有对应条目。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see ConfigurationPropertyName\n * @see OriginTrackedValue\n * @see #getConfigurationProperty(ConfigurationPropertyName)\n * @see #iterator()\n * @see #stream()\n */",
        ),
        (
            "\t/**\n\t * Return an iterator for the {@link ConfigurationPropertyName names} managed by this\n\t * source.\n\t * @return an iterator (never {@code null})\n\t */",
            "\t/**\n\t * 返回此源所管理的 {@link ConfigurationPropertyName 名称} 的迭代器。\n\t *\n\t * @return 迭代器（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Returns a sequential {@code Stream} for the {@link ConfigurationPropertyName names}\n\t * managed by this source.\n\t * @return a stream of names (never {@code null})\n\t */",
            "\t/**\n\t * 返回此源所管理的 {@link ConfigurationPropertyName 名称} 的顺序 {@code Stream}。\n\t *\n\t * @return 名称流（永不为 {@code null}）\n\t */",
        ),
    ],
    "MapConfigurationPropertySource.java": [
        (
            "/**\n * A {@link ConfigurationPropertySource} backed by a {@link Map} and using standard name\n * mapping rules.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 由 {@link Map} 支持并使用标准名称映射规则的 {@link ConfigurationPropertySource}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Create a new empty {@link MapConfigurationPropertySource} instance.\n\t */",
            "\t/**\n\t * 创建新的空 {@link MapConfigurationPropertySource} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link MapConfigurationPropertySource} instance with entries copies\n\t * from the specified map.\n\t * @param map the source map\n\t */",
            "\t/**\n\t * 从指定 Map 复制条目，创建新的 {@link MapConfigurationPropertySource} 实例。\n\t *\n\t * @param map 源 Map\n\t */",
        ),
        (
            "\t/**\n\t * Add all entries from the specified map.\n\t * @param map the source map\n\t */",
            "\t/**\n\t * 添加指定 Map 中的所有条目。\n\t *\n\t * @param map 源 Map\n\t */",
        ),
        (
            "\t/**\n\t * Add an individual entry.\n\t * @param name the name\n\t * @param value the value\n\t */",
            "\t/**\n\t * 添加单个条目。\n\t *\n\t * @param name 名称\n\t * @param value 值\n\t */",
        ),
    ],
    "MutuallyExclusiveConfigurationPropertiesException.java": [
        (
            "/**\n * Exception thrown when more than one mutually exclusive configuration property has been\n * configured.\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 2.6.0\n */",
            "/**\n * 当配置了多个互斥配置属性时抛出的异常。\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 2.6.0\n */",
        ),
        (
            "\t/**\n\t * Creates a new instance for mutually exclusive configuration properties when two or\n\t * more of those properties have been configured.\n\t * @param configuredNames the names of the properties that have been configured\n\t * @param mutuallyExclusiveNames the names of the properties that are mutually\n\t * exclusive\n\t */",
            "\t/**\n\t * 当两个或多个互斥配置属性已被配置时，创建新实例。\n\t *\n\t * @param configuredNames 已配置属性的名称\n\t * @param mutuallyExclusiveNames 互斥属性的名称\n\t */",
        ),
        (
            "\t/**\n\t * Return the names of the properties that have been configured.\n\t * @return the names of the configured properties\n\t */",
            "\t/**\n\t * 返回已配置属性的名称。\n\t *\n\t * @return 已配置属性的名称\n\t */",
        ),
        (
            "\t/**\n\t * Return the names of the properties that are mutually exclusive.\n\t * @return the names of the mutually exclusive properties\n\t */",
            "\t/**\n\t * 返回互斥属性的名称。\n\t *\n\t * @return 互斥属性的名称\n\t */",
        ),
        (
            "\t/**\n\t * Throw a new {@link MutuallyExclusiveConfigurationPropertiesException} if multiple\n\t * non-null values are defined in a set of entries.\n\t * @param entries a consumer used to populate the entries to check\n\t */",
            "\t/**\n\t * 若一组条目中定义了多个非 null 值，则抛出新的 {@link MutuallyExclusiveConfigurationPropertiesException}。\n\t *\n\t * @param entries 用于填充待检查条目的 consumer\n\t */",
        ),
        (
            "\t/**\n\t * Throw a new {@link MutuallyExclusiveConfigurationPropertiesException} if multiple\n\t * values are defined in a set of entries that match the given predicate.\n\t * @param <V> the value type\n\t * @param entries a consumer used to populate the entries to check\n\t * @param predicate the predicate used to check for matching values\n\t * @since 3.3.7\n\t */",
            "\t/**\n\t * 若一组条目中存在多个匹配给定谓词的值，则抛出新的 {@link MutuallyExclusiveConfigurationPropertiesException}。\n\t *\n\t * @param <V> 值类型\n\t * @param entries 用于填充待检查条目的 consumer\n\t * @param predicate 用于检查匹配值的谓词\n\t * @since 3.3.7\n\t */",
        ),
    ],
    "PrefixedConfigurationPropertySource.java": [
        (
            "/**\n * A {@link ConfigurationPropertySource} supporting a prefix.\n *\n * @author Madhura Bhave\n */",
            "/**\n * 支持前缀的 {@link ConfigurationPropertySource}。\n *\n * @author Madhura Bhave\n */",
        ),
    ],
    "PrefixedIterableConfigurationPropertySource.java": [
        (
            "/**\n * An iterable {@link PrefixedConfigurationPropertySource}.\n *\n * @author Madhura Bhave\n */",
            "/**\n * 可迭代的 {@link PrefixedConfigurationPropertySource}。\n *\n * @author Madhura Bhave\n */",
        ),
    ],
    "PropertyMapper.java": [
        (
            "/**\n * Strategy used to provide a mapping between a {@link PropertySource} and a\n * {@link ConfigurationPropertySource}.\n * <p>\n * Mappings should be provided for both {@link ConfigurationPropertyName\n * ConfigurationPropertyName} types and {@code String} based names. This allows the\n * {@link SpringConfigurationPropertySource} to first attempt any direct mappings (i.e.\n * map the {@link ConfigurationPropertyName} directly to the {@link PropertySource} name)\n * before falling back to {@link EnumerablePropertySource enumerating} property names,\n * mapping them to a {@link ConfigurationPropertyName} and checking for applicability. See\n * {@link SpringConfigurationPropertySource} for more details.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see SpringConfigurationPropertySource\n */",
            "/**\n * 用于在 {@link PropertySource} 与 {@link ConfigurationPropertySource} 之间提供映射的策略。\n * <p>\n * 应同时为 {@link ConfigurationPropertyName ConfigurationPropertyName} 类型\n * 与基于 {@code String} 的名称提供映射。这使 {@link SpringConfigurationPropertySource}\n * 可先尝试直接映射（即将 {@link ConfigurationPropertyName} 直接映射到 {@link PropertySource} 名称），\n * 再回退到 {@link EnumerablePropertySource 枚举}属性名、将其映射为 {@link ConfigurationPropertyName}\n * 并检查是否适用。详见 {@link SpringConfigurationPropertySource}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see SpringConfigurationPropertySource\n */",
        ),
        (
            "\t/**\n\t * The default ancestor of check.\n\t */",
            "\t/**\n\t * 默认的祖先关系检查。\n\t */",
        ),
        (
            "\t/**\n\t * Provide mappings from a {@link ConfigurationPropertySource}\n\t * {@link ConfigurationPropertyName}.\n\t * @param configurationPropertyName the name to map\n\t * @return the mapped names or an empty list\n\t */",
            "\t/**\n\t * 从 {@link ConfigurationPropertySource} 的 {@link ConfigurationPropertyName} 提供映射。\n\t *\n\t * @param configurationPropertyName 待映射的名称\n\t * @return 映射后的名称或空列表\n\t */",
        ),
        (
            "\t/**\n\t * Provide mappings from a {@link PropertySource} property name.\n\t * @param propertySourceName the name to map\n\t * @return the mapped configuration property name or\n\t * {@link ConfigurationPropertyName#EMPTY}\n\t */",
            "\t/**\n\t * 从 {@link PropertySource} 属性名提供映射。\n\t *\n\t * @param propertySourceName 待映射的名称\n\t * @return 映射后的配置属性名或 {@link ConfigurationPropertyName#EMPTY}\n\t */",
        ),
        (
            "\t/**\n\t * Returns a {@link BiPredicate} that can be used to check if one name is an ancestor\n\t * of another when considering the mapping rules.\n\t * @return a predicate that can be used to check if one name is an ancestor of another\n\t */",
            "\t/**\n\t * 返回可用于在考虑映射规则时检查一个名称是否为另一个名称祖先的 {@link BiPredicate}。\n\t *\n\t * @return 用于检查祖先关系的谓词\n\t */",
        ),
    ],
    "SoftReferenceConfigurationPropertyCache.java": [
        (
            "/**\n * Simple cache that uses a {@link SoftReference} to cache a value for as long as\n * possible.\n *\n * @param <T> the value type\n * @author Phillip Webb\n */",
            "/**\n * 使用 {@link SoftReference} 尽可能长时间缓存值的简单缓存。\n *\n * @param <T> 值类型\n * @author Phillip Webb\n */",
        ),
        (
            "\t/**\n\t * Get a value from the cache, creating it if necessary.\n\t * @param factory a factory used to create the item if there is no reference to it.\n\t * @param refreshAction action called to refresh the value if it has expired\n\t * @return the value from the cache\n\t */",
            "\t/**\n\t * 从缓存获取值，必要时创建。\n\t *\n\t * @param factory 无引用时用于创建项的工厂\n\t * @param refreshAction 值过期时用于刷新的操作\n\t * @return 缓存中的值\n\t */",
        ),
        (
            "\t/**\n\t * An active {@link CacheOverride} with a stored time-to-live.\n\t */",
            "\t/**\n\t * 带有存储 TTL 的活动 {@link CacheOverride}。\n\t */",
        ),
    ],
    "SpringConfigurationPropertySource.java": [
        (
            "/**\n * {@link ConfigurationPropertySource} backed by a non-enumerable Spring\n * {@link PropertySource} or a restricted {@link EnumerablePropertySource} implementation\n * (such as a security restricted {@code systemEnvironment} source). A\n * {@link PropertySource} is adapted with the help of a {@link PropertyMapper} which\n * provides the mapping rules for individual properties.\n * <p>\n * Each {@link ConfigurationPropertySource#getConfigurationProperty\n * getConfigurationProperty} call attempts to\n * {@link PropertyMapper#map(ConfigurationPropertyName) map} the\n * {@link ConfigurationPropertyName} to one or more {@code String} based names. This\n * allows fast property resolution for well-formed property sources.\n * <p>\n * When possible the {@link SpringIterableConfigurationPropertySource} will be used in\n * preference to this implementation since it supports full \"relaxed\" style resolution.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see #from(PropertySource)\n * @see PropertyMapper\n * @see SpringIterableConfigurationPropertySource\n */",
            "/**\n * 由不可枚举的 Spring {@link PropertySource} 或受限的 {@link EnumerablePropertySource}\n * 实现（例如安全受限的 {@code systemEnvironment} 源）支持的 {@link ConfigurationPropertySource}。\n * 借助 {@link PropertyMapper} 适配 {@link PropertySource}，后者提供各属性的映射规则。\n * <p>\n * 每次 {@link ConfigurationPropertySource#getConfigurationProperty getConfigurationProperty}\n * 调用都会尝试将 {@link ConfigurationPropertyName}\n * {@link PropertyMapper#map(ConfigurationPropertyName) 映射}为一个或多个基于 {@code String} 的名称。\n * 这使结构良好的属性源可快速解析属性。\n * <p>\n * 若可能，优先使用 {@link SpringIterableConfigurationPropertySource}，\n * 因其支持完整的“宽松”风格解析。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see #from(PropertySource)\n * @see PropertyMapper\n * @see SpringIterableConfigurationPropertySource\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link SpringConfigurationPropertySource} implementation.\n\t * @param propertySource the source property source\n\t * @param systemEnvironmentSource if the source is from the system environment\n\t * @param mappers the property mappers\n\t */",
            "\t/**\n\t * 创建新的 {@link SpringConfigurationPropertySource} 实现。\n\t *\n\t * @param propertySource 源 PropertySource\n\t * @param systemEnvironmentSource 源是否来自系统环境\n\t * @param mappers 属性映射器\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link SpringConfigurationPropertySource} for the specified\n\t * {@link PropertySource}.\n\t * @param source the source Spring {@link PropertySource}\n\t * @return a {@link SpringConfigurationPropertySource} or\n\t * {@link SpringIterableConfigurationPropertySource} instance\n\t */",
            "\t/**\n\t * 为指定 {@link PropertySource} 创建新的 {@link SpringConfigurationPropertySource}。\n\t *\n\t * @param source 源 Spring {@link PropertySource}\n\t * @return {@link SpringConfigurationPropertySource} 或\n\t * {@link SpringIterableConfigurationPropertySource} 实例\n\t */",
        ),
    ],
    "SpringConfigurationPropertySources.java": [
        (
            "/**\n * Adapter to convert Spring's {@link MutablePropertySources} to\n * {@link ConfigurationPropertySource ConfigurationPropertySources}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 将 Spring 的 {@link MutablePropertySources} 转换为\n * {@link ConfigurationPropertySource ConfigurationPropertySources} 的适配器。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "SpringIterableConfigurationPropertySource.java": [
        (
            "/**\n * {@link ConfigurationPropertySource} backed by an {@link EnumerablePropertySource}.\n * Extends {@link SpringConfigurationPropertySource} with full \"relaxed\" mapping support.\n * In order to use this adapter the underlying {@link PropertySource} must be fully\n * enumerable. A security restricted {@link SystemEnvironmentPropertySource} cannot be\n * adapted.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see PropertyMapper\n */",
            "/**\n * 由 {@link EnumerablePropertySource} 支持的 {@link ConfigurationPropertySource}。\n * 扩展 {@link SpringConfigurationPropertySource}，提供完整的“宽松”映射支持。\n * 要使用此适配器，底层 {@link PropertySource} 必须完全可枚举。\n * 安全受限的 {@link SystemEnvironmentPropertySource} 无法适配。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see PropertyMapper\n */",
        ),
        (
            "\t/**\n\t * ConfigurationPropertyNames iterator backed by an array.\n\t */",
            "\t/**\n\t * 由数组支持的 ConfigurationPropertyNames 迭代器。\n\t */",
        ),
    ],
    "SystemEnvironmentPropertyMapper.java": [
        (
            "/**\n * {@link PropertyMapper} for system environment variables. Names are mapped by removing\n * invalid characters, converting to lower case and replacing \"{@code _}\" with\n * \"{@code .}\". For example, \"{@code SERVER_PORT}\" is mapped to \"{@code server.port}\". In\n * addition, numeric elements are mapped to indexes (e.g. \"{@code HOST_0}\" is mapped to\n * \"{@code host[0]}\").\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see PropertyMapper\n * @see SpringConfigurationPropertySource\n */",
            "/**\n * 用于系统环境变量的 {@link PropertyMapper}。通过移除无效字符、转为小写并将 \"{@code _}\" 替换为\n * \"{@code .}\" 来映射名称。例如 \"{@code SERVER_PORT}\" 映射为 \"{@code server.port}\"。\n * 此外，数字元素映射为索引（例如 \"{@code HOST_0}\" 映射为 \"{@code host[0]}\"）。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see PropertyMapper\n * @see SpringConfigurationPropertySource\n */",
        ),
    ],
    "UnboundElementsSourceFilter.java": [
        (
            "/**\n * Function used to determine if a {@link ConfigurationPropertySource} should be included\n * when determining unbound elements. If the underlying {@link PropertySource} is a\n * systemEnvironment or systemProperties property source, it will not be considered for\n * unbound element failures.\n *\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 用于确定在判定未绑定元素时是否应包含某 {@link ConfigurationPropertySource} 的函数。\n * 若底层 {@link PropertySource} 为 systemEnvironment 或 systemProperties 属性源，\n * 则不会将其计入未绑定元素失败。\n *\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
    ],
    "ArrayToDelimitedStringConverter.java": [
        (
            "/**\n * Converts an array to a delimited String.\n *\n * @author Phillip Webb\n */",
            "/**\n * 将数组转换为带分隔符的 String。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "CharArrayFormatter.java": [
        (
            "/**\n * {@link Formatter} for {@code char[]}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 用于 {@code char[]} 的 {@link Formatter}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "CharSequenceToObjectConverter.java": [
        (
            "/**\n * {@link ConditionalGenericConverter} to convert {@link CharSequence} type by delegating\n * to existing {@link String} converters.\n *\n * @author Phillip Webb\n */",
            "/**\n * 通过委托现有 {@link String} 转换器来转换 {@link CharSequence} 类型的\n * {@link ConditionalGenericConverter}。\n *\n * @author Phillip Webb\n */",
        ),
        (
            "\t/**\n\t * Return if String based conversion is better based on the target type. This is\n\t * required when ObjectTo... conversion produces incorrect results.\n\t * @param sourceType the source type to test\n\t * @param targetType the target type to test\n\t * @return if string conversion is better\n\t */",
            "\t/**\n\t * 根据目标类型判断是否基于 String 的转换更优。当 ObjectTo... 转换产生错误结果时需要此方法。\n\t *\n\t * @param sourceType 待测试的源类型\n\t * @param targetType 待测试的目标类型\n\t * @return 基于 String 的转换是否更优\n\t */",
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
