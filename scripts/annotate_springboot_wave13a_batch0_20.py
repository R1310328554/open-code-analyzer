#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-13a slice [0:20] (origin tracking + SSL bundles)."""
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
BATCH_FILES = Path("/tmp/springboot_w13a.txt").read_text(encoding="utf-8").strip().splitlines()

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "StructuredLoggingJsonPropertiesJsonMembersCustomizer.java": [
        (
            "/**\n * {@link StructuredLoggingJsonMembersCustomizer} to apply\n * {@link StructuredLoggingJsonProperties}.\n *\n * @author Phillip Webb\n * @author Yanming Zhou\n */",
            "/**\n * 应用 {@link StructuredLoggingJsonProperties} 的 {@link StructuredLoggingJsonMembersCustomizer} 实现。\n * 支持 JSON 成员路径过滤、重命名、追加字段及嵌套自定义器。\n *\n * @author Phillip Webb\n * @author Yanming Zhou\n */",
        ),
    ],
    "JarUri.java": [
        (
            "/**\n * Simple class that understands Jar URLs and can provide short descriptions.\n *\n * @author Phillip Webb\n */",
            "/**\n * 解析 Jar URL 并提供简短描述的工具类。\n * 用于在 {@link TextResourceOrigin} 中生成类路径资源的可读来源说明。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "Origin.java": [
        (
            "/**\n * Interface that uniquely represents the origin of an item. For example, an item loaded\n * from a {@link File} may have an origin made up of the file name along with line/column\n * numbers.\n * <p>\n * Implementations must provide sensible {@code hashCode()}, {@code equals(...)} and\n * {@code #toString()} implementations.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.0.0\n * @see OriginProvider\n * @see TextResourceOrigin\n */",
            "/**\n * 唯一表示某项来源的接口。例如，从 {@link File} 加载的项\n * 其来源可能由文件名以及行号/列号组成。\n * <p>\n * 实现类必须提供合理的 {@code hashCode()}、{@code equals(...)} 与\n * {@code #toString()} 实现。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.0.0\n * @see OriginProvider\n * @see TextResourceOrigin\n */",
        ),
        (
            "\t/**\n\t * Return the parent origin for this instance if there is one. The parent origin\n\t * provides the origin of the item that created this one.\n\t * @return the parent origin or {@code null}\n\t * @since 2.4.0\n\t * @see Origin#parentsFrom(Object)\n\t */",
            "\t/**\n\t * 若存在则返回此实例的父来源。父来源表示创建当前项的上级项的来源。\n\t *\n\t * @return the parent origin or {@code null} 父来源，或 {@code null}\n\t * @since 2.4.0\n\t * @see Origin#parentsFrom(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Find the {@link Origin} that an object originated from. Checks if the source object\n\t * is an {@link Origin} or {@link OriginProvider} and also searches exception stacks.\n\t * @param source the source object or {@code null}\n\t * @return an {@link Origin} or {@code null}\n\t */",
            "\t/**\n\t * 查找对象所对应的 {@link Origin}。若源对象是 {@link Origin} 或\n\t * {@link OriginProvider} 则直接解析，并会沿异常堆栈继续查找。\n\t *\n\t * @param source the source object or {@code null} 源对象或 {@code null}\n\t * @return an {@link Origin} or {@code null} {@link Origin} 或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Find the parents of the {@link Origin} that an object originated from. Checks if\n\t * the source object is an {@link Origin} or {@link OriginProvider} and also searches\n\t * exception stacks. Provides a list of all parents up to root {@link Origin},\n\t * starting with the most immediate parent.\n\t * @param source the source object or {@code null}\n\t * @return a list of parents or an empty list if the source is {@code null}, has no\n\t * origin, or no parent\n\t * @since 2.4.0\n\t */",
            "\t/**\n\t * 查找对象所对应 {@link Origin} 的全部父来源。若源对象是 {@link Origin} 或\n\t * {@link OriginProvider} 则解析，并会沿异常堆栈继续查找。\n\t * 返回从最近父来源到根 {@link Origin} 的列表。\n\t *\n\t * @param source the source object or {@code null} 源对象或 {@code null}\n\t * @return a list of parents or an empty list if the source is {@code null}, has no\n\t * origin, or no parent 父来源列表；若源对象为 {@code null}、无来源或无父来源则返回空列表\n\t * @since 2.4.0\n\t */",
        ),
    ],
    "OriginLookup.java": [
        (
            "/**\n * An interface that may be implemented by an object that can lookup {@link Origin}\n * information from a given key. Can be used to add origin support to existing classes.\n *\n * @param <K> the lookup key type\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 可根据给定键查找 {@link Origin} 信息的接口。\n * 可用于为现有类添加来源追踪支持。\n *\n * @param <K> the lookup key type 查找键类型\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Return the origin of the given key or {@code null} if the origin cannot be\n\t * determined.\n\t * @param key the key to lookup\n\t * @return the origin of the key or {@code null}\n\t */",
            "\t/**\n\t * 返回给定键的来源；若无法确定则返回 {@code null}。\n\t *\n\t * @param key the key to lookup 待查找的键\n\t * @return the origin of the key or {@code null} 键的来源或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Attempt to look up the origin from the given source. If the source is not a\n\t * {@link OriginLookup} or if an exception occurs during lookup then {@code null} is\n\t * returned.\n\t * @param source the source object\n\t * @param key the key to lookup\n\t * @param <K> the key type\n\t * @return an {@link Origin} or {@code null}\n\t */",
            "\t/**\n\t * 尝试从给定源对象查找来源。若源对象不是 {@link OriginLookup}，\n\t * 或查找过程中抛出异常，则返回 {@code null}。\n\t *\n\t * @param source the source object 源对象\n\t * @param key the key to lookup 待查找的键\n\t * @param <K> the key type 键类型\n\t * @return an {@link Origin} or {@code null} {@link Origin} 或 {@code null}\n\t */",
        ),
    ],
    "OriginProvider.java": [
        (
            "/**\n * Interface to provide access to the origin of an item.\n *\n * @author Phillip Webb\n * @since 2.0.0\n * @see Origin\n */",
            "/**\n * 提供对某项来源访问能力的接口。\n *\n * @author Phillip Webb\n * @since 2.0.0\n * @see Origin\n */",
        ),
        (
            "\t/**\n\t * Return the source origin or {@code null} if the origin is not known.\n\t * @return the origin or {@code null}\n\t */",
            "\t/**\n\t * 返回源来源；若来源未知则返回 {@code null}。\n\t *\n\t * @return the origin or {@code null} 来源或 {@code null}\n\t */",
        ),
    ],
    "OriginTrackedResource.java": [
        (
            "/**\n * Decorator that can be used to add {@link Origin} information to a {@link Resource} or\n * {@link WritableResource}.\n *\n * @author Phillip Webb\n * @since 2.4.0\n * @see #of(Resource, Origin)\n * @see #of(WritableResource, Origin)\n * @see OriginProvider\n */",
            "/**\n * 可为 {@link Resource} 或 {@link WritableResource} 附加 {@link Origin} 信息的装饰器。\n *\n * @author Phillip Webb\n * @since 2.4.0\n * @see #of(Resource, Origin)\n * @see #of(WritableResource, Origin)\n * @see OriginProvider\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link OriginTrackedResource} instance.\n\t * @param resource the resource to track\n\t * @param origin the origin of the resource\n\t */",
            "\t/**\n\t * 创建新的 {@link OriginTrackedResource} 实例。\n\t *\n\t * @param resource the resource to track 待追踪的资源\n\t * @param origin the origin of the resource 资源的来源\n\t */",
        ),
        (
            "\t/**\n\t * Return a new {@link OriginProvider origin tracked} version the given\n\t * {@link WritableResource}.\n\t * @param resource the tracked resource\n\t * @param origin the origin of the resource\n\t * @return an {@link OriginTrackedWritableResource} instance\n\t */",
            "\t/**\n\t * 返回给定 {@link WritableResource} 的 {@link OriginProvider 带来源追踪} 版本。\n\t *\n\t * @param resource the tracked resource 待追踪的资源\n\t * @param origin the origin of the resource 资源的来源\n\t * @return an {@link OriginTrackedWritableResource} instance {@link OriginTrackedWritableResource} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Return a new {@link OriginProvider origin tracked} version the given\n\t * {@link Resource}.\n\t * @param resource the tracked resource\n\t * @param origin the origin of the resource\n\t * @return an {@link OriginTrackedResource} instance\n\t */",
            "\t/**\n\t * 返回给定 {@link Resource} 的 {@link OriginProvider 带来源追踪} 版本。\n\t *\n\t * @param resource the tracked resource 待追踪的资源\n\t * @param origin the origin of the resource 资源的来源\n\t * @return an {@link OriginTrackedResource} instance {@link OriginTrackedResource} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Variant of {@link OriginTrackedResource} for {@link WritableResource} instances.\n\t */",
            "\t/**\n\t * 面向 {@link WritableResource} 实例的 {@link OriginTrackedResource} 变体。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Create a new {@link OriginTrackedWritableResource} instance.\n\t\t * @param resource the resource to track\n\t\t * @param origin the origin of the resource\n\t\t */",
            "\t\t/**\n\t\t * 创建新的 {@link OriginTrackedWritableResource} 实例。\n\t\t *\n\t\t * @param resource the resource to track 待追踪的资源\n\t\t * @param origin the origin of the resource 资源的来源\n\t\t */",
        ),
    ],
    "OriginTrackedValue.java": [
        (
            "/**\n * A wrapper for an {@link Object} value and {@link Origin}.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.0.0\n * @see #of(Object)\n * @see #of(Object, Origin)\n */",
            "/**\n * 包装 {@link Object} 值与 {@link Origin} 的容器类。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.0.0\n * @see #of(Object)\n * @see #of(Object, Origin)\n */",
        ),
        (
            "\t/**\n\t * Return the tracked value.\n\t * @return the tracked value\n\t */",
            "\t/**\n\t * 返回被追踪的值。\n\t *\n\t * @return the tracked value 被追踪的值\n\t */",
        ),
        (
            "\t/**\n\t * Create an {@link OriginTrackedValue} containing the specified {@code value} and\n\t * {@code origin}. If the source value implements {@link CharSequence} then so will\n\t * the resulting {@link OriginTrackedValue}.\n\t * @param value the source value\n\t * @param origin the origin\n\t * @return an {@link OriginTrackedValue} or {@code null} if the source value was\n\t * {@code null}.\n\t */",
            "\t/**\n\t * 创建包含指定 {@code value} 与 {@code origin} 的 {@link OriginTrackedValue}。\n\t * 若源值实现 {@link CharSequence}，则结果同样实现 {@link CharSequence}。\n\t *\n\t * @param value the source value 源值\n\t * @param origin the origin 来源\n\t * @return an {@link OriginTrackedValue} or {@code null} if the source value was\n\t * {@code null}. {@link OriginTrackedValue}；若源值为 {@code null} 则返回 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * {@link OriginTrackedValue} for a {@link CharSequence}.\n\t */",
            "\t/**\n\t * 面向 {@link CharSequence} 的 {@link OriginTrackedValue} 实现。\n\t */",
        ),
    ],
    "PropertySourceOrigin.java": [
        (
            "/**\n * {@link Origin} from a {@link PropertySource}.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 来自 {@link PropertySource} 的 {@link Origin} 实现。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link PropertySourceOrigin} instance.\n\t * @param propertySource the property source\n\t * @param propertyName the name from the property source\n\t */",
            "\t/**\n\t * 创建新的 {@link PropertySourceOrigin} 实例。\n\t *\n\t * @param propertySource the property source 属性源\n\t * @param propertyName the name from the property source 属性源中的属性名\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link PropertySourceOrigin} instance.\n\t * @param propertySource the property source\n\t * @param propertyName the name from the property source\n\t * @param origin the actual origin for the source if known\n\t * @since 3.2.8\n\t */",
            "\t/**\n\t * 创建新的 {@link PropertySourceOrigin} 实例。\n\t *\n\t * @param propertySource the property source 属性源\n\t * @param propertyName the name from the property source 属性源中的属性名\n\t * @param origin the actual origin for the source if known 若已知则为属性源的实际来源\n\t * @since 3.2.8\n\t */",
        ),
        (
            "\t/**\n\t * Return the origin {@link PropertySource}.\n\t * @return the origin property source\n\t */",
            "\t/**\n\t * 返回来源 {@link PropertySource}。\n\t *\n\t * @return the origin property source 来源属性源\n\t */",
        ),
        (
            "\t/**\n\t * Return the property name that was used when obtaining the original value from the\n\t * {@link #getPropertySource() property source}.\n\t * @return the origin property name\n\t */",
            "\t/**\n\t * 返回从 {@link #getPropertySource() 属性源} 获取原始值时使用的属性名。\n\t *\n\t * @return the origin property name 来源属性名\n\t */",
        ),
        (
            "\t/**\n\t * Return the actual origin for the source if known.\n\t * @return the actual source origin\n\t * @since 3.2.8\n\t */",
            "\t/**\n\t * 若已知则返回属性源的实际来源。\n\t *\n\t * @return the actual source origin 实际来源\n\t * @since 3.2.8\n\t */",
        ),
        (
            "\t/**\n\t * Get an {@link Origin} for the given {@link PropertySource} and\n\t * {@code propertyName}. Will either return an {@link OriginLookup} result or a\n\t * {@link PropertySourceOrigin}.\n\t * @param propertySource the origin property source\n\t * @param name the property name\n\t * @return the property origin\n\t */",
            "\t/**\n\t * 获取给定 {@link PropertySource} 与 {@code propertyName} 对应的 {@link Origin}。\n\t * 优先返回 {@link OriginLookup} 的结果，否则返回 {@link PropertySourceOrigin}。\n\t *\n\t * @param propertySource the origin property source 来源属性源\n\t * @param name the property name 属性名\n\t * @return the property origin 属性来源\n\t */",
        ),
    ],
    "SystemEnvironmentOrigin.java": [
        (
            "/**\n * {@link Origin} for an item loaded from the system environment. Provides access to the\n * original property name.\n *\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 表示从系统环境加载的项的 {@link Origin}。\n * 提供对原始环境变量/系统属性名的访问。\n *\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
    ],
    "TextResourceOrigin.java": [
        (
            "/**\n * {@link Origin} for an item loaded from a text resource. Provides access to the original\n * {@link Resource} that loaded the text and a {@link Location} within it. If the provided\n * resource provides an {@link Origin} (e.g. it is an {@link OriginTrackedResource}), then\n * it will be used as the {@link Origin#getParent() origin parent}.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.0.0\n * @see OriginTrackedResource\n */",
            "/**\n * 表示从文本资源加载的项的 {@link Origin}。\n * 提供加载文本的原始 {@link Resource} 及其内部 {@link Location} 位置信息。\n * 若资源本身提供 {@link Origin}（例如 {@link OriginTrackedResource}），\n * 则将其作为 {@link Origin#getParent() 父来源}。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.0.0\n * @see OriginTrackedResource\n */",
        ),
        (
            "\t/**\n\t * Return the resource where the property originated.\n\t * @return the text resource or {@code null}\n\t */",
            "\t/**\n\t * 返回属性来源所在的文本资源。\n\t *\n\t * @return the text resource or {@code null} 文本资源或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Return the location of the property within the source (if known).\n\t * @return the location or {@code null}\n\t */",
            "\t/**\n\t * 返回属性在源文本中的位置（若已知）。\n\t *\n\t * @return the location or {@code null} 位置或 {@code null}\n\t */",
        ),
        (
            "\t\t// Ignore",
            "\t\t// 忽略",
        ),
        (
            "\t/**\n\t * A location (line and column number) within the resource.\n\t */",
            "\t/**\n\t * 资源内的位置（行号与列号）。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Create a new {@link Location} instance.\n\t\t * @param line the line number (zero indexed)\n\t\t * @param column the column number (zero indexed)\n\t\t */",
            "\t\t/**\n\t\t * 创建新的 {@link Location} 实例。\n\t\t *\n\t\t * @param line the line number (zero indexed) 行号（从 0 开始）\n\t\t * @param column the column number (zero indexed) 列号（从 0 开始）\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Return the line of the text resource where the property originated.\n\t\t * @return the line number (zero indexed)\n\t\t */",
            "\t\t/**\n\t\t * 返回属性来源在文本资源中的行号。\n\t\t *\n\t\t * @return the line number (zero indexed) 行号（从 0 开始）\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Return the column of the text resource where the property originated.\n\t\t * @return the column number (zero indexed)\n\t\t */",
            "\t\t/**\n\t\t * 返回属性来源在文本资源中的列号。\n\t\t *\n\t\t * @return the column number (zero indexed) 列号（从 0 开始）\n\t\t */",
        ),
    ],
    "RetryPolicySettings.java": [
        (
            "/**\n * Settings for a {@link RetryPolicy}.\n *\n * @author Stephane Nicoll\n * @since 4.0.0\n */",
            "/**\n * {@link RetryPolicy} 的配置项。\n * 通过 {@link PropertyMapper} 映射到 {@link RetryPolicy.Builder} 并构建重试策略。\n *\n * @author Stephane Nicoll\n * @since 4.0.0\n */",
        ),
        (
            "\t/**\n\t * Default number of retry attempts.\n\t */",
            "\t/**\n\t * 默认最大重试次数。\n\t */",
        ),
        (
            "\t/**\n\t * Default initial delay.\n\t */",
            "\t/**\n\t * 默认初始延迟。\n\t */",
        ),
        (
            "\t/**\n\t * Default multiplier, uses a fixed delay.\n\t */",
            "\t/**\n\t * 默认乘数；值为 1.0 时等效于固定延迟。\n\t */",
        ),
        (
            "\t/**\n\t * Default maximum delay (infinite).\n\t */",
            "\t/**\n\t * 默认最大延迟（无上限）。\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@link RetryPolicy} based on the state of this instance.\n\t * @return a {@link RetryPolicy}\n\t */",
            "\t/**\n\t * 根据当前实例状态创建 {@link RetryPolicy}。\n\t *\n\t * @return a {@link RetryPolicy} {@link RetryPolicy} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Return the applicable exception types to attempt a retry for.\n\t * <p>\n\t * The default is empty, leading to a retry attempt for any exception.\n\t * @return the applicable exception types\n\t */",
            "\t/**\n\t * 返回应触发重试的异常类型列表。\n\t * <p>\n\t * 默认为空，表示任意异常都会尝试重试。\n\t *\n\t * @return the applicable exception types 适用异常类型\n\t */",
        ),
        (
            "\t/**\n\t * Replace the applicable exception types to attempt a retry for by the given\n\t * {@code includes}. Alternatively consider using {@link #getExceptionIncludes()} to\n\t * mutate the existing list.\n\t * @param includes the applicable exception types\n\t */",
            "\t/**\n\t * 用给定 {@code includes} 替换应触发重试的异常类型列表。\n\t * 也可通过 {@link #getExceptionIncludes()} 直接修改现有列表。\n\t *\n\t * @param includes the applicable exception types 适用异常类型\n\t */",
        ),
        (
            "\t/**\n\t * Return the non-applicable exception types to avoid a retry for.\n\t * <p>\n\t * The default is empty, leading to a retry attempt for any exception.\n\t * @return the non-applicable exception types\n\t */",
            "\t/**\n\t * 返回不应触发重试的异常类型列表。\n\t * <p>\n\t * 默认为空，表示任意异常都会尝试重试。\n\t *\n\t * @return the non-applicable exception types 不适用异常类型\n\t */",
        ),
        (
            "\t/**\n\t * Replace the non-applicable exception types to attempt a retry for by the given\n\t * {@code excludes}. Alternatively consider using {@link #getExceptionExcludes()} to\n\t * mutate the existing list.\n\t * @param excludes the non-applicable types\n\t */",
            "\t/**\n\t * 用给定 {@code excludes} 替换不应触发重试的异常类型列表。\n\t * 也可通过 {@link #getExceptionExcludes()} 直接修改现有列表。\n\t *\n\t * @param excludes the non-applicable types 不适用异常类型\n\t */",
        ),
        (
            "\t/**\n\t * Return the predicate to use to determine whether to retry a failed operation based\n\t * on a given {@link Throwable}.\n\t * @return the predicate to use\n\t */",
            "\t/**\n\t * 返回用于根据 {@link Throwable} 判断是否重试的谓词。\n\t *\n\t * @return the predicate to use 判定谓词\n\t */",
        ),
        (
            "\t/**\n\t * Set the predicate to use to determine whether to retry a failed operation based on\n\t * a given {@link Throwable}.\n\t * @param exceptionPredicate the predicate to use\n\t */",
            "\t/**\n\t * 设置用于根据 {@link Throwable} 判断是否重试的谓词。\n\t *\n\t * @param exceptionPredicate the predicate to use 判定谓词\n\t */",
        ),
        (
            "\t/**\n\t * Return the maximum number of retry attempts.\n\t * @return the maximum number of retry attempts\n\t * @see #DEFAULT_MAX_RETRIES\n\t */",
            "\t/**\n\t * 返回最大重试次数。\n\t *\n\t * @return the maximum number of retry attempts 最大重试次数\n\t * @see #DEFAULT_MAX_RETRIES\n\t */",
        ),
        (
            "\t/**\n\t * Specify the maximum number of retry attempts.\n\t * @param maxRetries the maximum number of retry attempts (must be equal or greater\n\t * than zero)\n\t */",
            "\t/**\n\t * 设置最大重试次数。\n\t *\n\t * @param maxRetries the maximum number of retry attempts (must be equal or greater\n\t * than zero) 最大重试次数（必须大于等于 0）\n\t */",
        ),
        (
            "\t/**\n\t * Return the base delay after the initial invocation.\n\t * @return the base delay\n\t * @see #DEFAULT_DELAY\n\t */",
            "\t/**\n\t * 返回首次调用后的基础延迟。\n\t *\n\t * @return the base delay 基础延迟\n\t * @see #DEFAULT_DELAY\n\t */",
        ),
        (
            "\t/**\n\t * Specify the base delay after the initial invocation.\n\t * <p>\n\t * If a {@linkplain #getMultiplier() multiplier} is specified, this serves as the\n\t * initial delay to multiply from.\n\t * @param delay the base delay (must be greater than or equal to zero)\n\t */",
            "\t/**\n\t * 设置首次调用后的基础延迟。\n\t * <p>\n\t * 若指定了 {@linkplain #getMultiplier() 乘数}，此值作为后续倍增的初始延迟。\n\t *\n\t * @param delay the base delay (must be greater than or equal to zero) 基础延迟（必须大于等于 0）\n\t */",
        ),
        (
            "\t/**\n\t * Return the jitter period to enable random retry attempts.\n\t * @return the jitter value\n\t */",
            "\t/**\n\t * 返回用于随机化重试间隔的抖动周期。\n\t *\n\t * @return the jitter value 抖动值\n\t */",
        ),
        (
            "\t/**\n\t * Specify a jitter period for the base retry attempt, randomly subtracted or added to\n\t * the calculated delay, resulting in a value between {@code delay - jitter} and\n\t * {@code delay + jitter} but never below the {@linkplain #getDelay() base delay} or\n\t * above the {@linkplain #getMaxDelay() max delay}.\n\t * <p>\n\t * If a {@linkplain #getMultiplier() multiplier} is specified, it is applied to the\n\t * jitter value as well.\n\t * @param jitter the jitter value (must be positive)\n\t */",
            "\t/**\n\t * 设置基础重试尝试的抖动周期，会在计算出的延迟上随机加减，\n\t * 结果介于 {@code delay - jitter} 与 {@code delay + jitter} 之间，\n\t * 但不会低于 {@linkplain #getDelay() 基础延迟} 或高于 {@linkplain #getMaxDelay() 最大延迟}。\n\t * <p>\n\t * 若指定了 {@linkplain #getMultiplier() 乘数}，抖动值也会相应倍增。\n\t *\n\t * @param jitter the jitter value (must be positive) 抖动值（必须为正数）\n\t */",
        ),
        (
            "\t/**\n\t * Return the value to multiply the current interval by for each attempt. The default\n\t * value, {@code 1.0}, effectively results in a fixed delay.\n\t * @return the value to multiply the current interval by for each attempt\n\t * @see #DEFAULT_MULTIPLIER\n\t */",
            "\t/**\n\t * 返回每次重试将当前间隔乘以的系数。默认值 {@code 1.0} 等效于固定延迟。\n\t *\n\t * @return the value to multiply the current interval by for each attempt 间隔乘数\n\t * @see #DEFAULT_MULTIPLIER\n\t */",
        ),
        (
            "\t/**\n\t * Specify a multiplier for a delay for the next retry attempt.\n\t * @param multiplier value to multiply the current interval by for each attempt (must\n\t * be greater than or equal to 1)\n\t */",
            "\t/**\n\t * 设置下次重试延迟的乘数。\n\t *\n\t * @param multiplier value to multiply the current interval by for each attempt (must\n\t * be greater than or equal to 1) 间隔乘数（必须大于等于 1）\n\t */",
        ),
        (
            "\t/**\n\t * Return the maximum delay for any retry attempt.\n\t * @return the maximum delay\n\t */",
            "\t/**\n\t * 返回任意重试尝试的最大延迟。\n\t *\n\t * @return the maximum delay 最大延迟\n\t */",
        ),
        (
            "\t/**\n\t * Specify the maximum delay for any retry attempt, limiting how far\n\t * {@linkplain #getJitter() jitter} and the {@linkplain #getMultiplier() multiplier}\n\t * can increase the {@linkplain #getDelay() delay}.\n\t * <p>\n\t * The default is unlimited.\n\t * @param maxDelay the maximum delay (must be positive)\n\t * @see #DEFAULT_MAX_DELAY\n\t */",
            "\t/**\n\t * 设置任意重试尝试的最大延迟，限制 {@linkplain #getJitter() 抖动} 与\n\t * {@linkplain #getMultiplier() 乘数} 可将 {@linkplain #getDelay() 延迟} 放大的上限。\n\t * <p>\n\t * 默认无上限。\n\t *\n\t * @param maxDelay the maximum delay (must be positive) 最大延迟（必须为正数）\n\t * @see #DEFAULT_MAX_DELAY\n\t */",
        ),
        (
            "\t/**\n\t * Set the factory to use to create the {@link RetryPolicy}, or {@code null} to use\n\t * the default. The function takes a {@link Builder RetryPolicy.Builder} initialized\n\t * with the state of this instance that can be further configured, or ignored to\n\t * restart from scratch.\n\t * @param factory a factory to customize the retry policy.\n\t */",
            "\t/**\n\t * 设置用于创建 {@link RetryPolicy} 的工厂；为 {@code null} 时使用默认构建方式。\n\t * 工厂接收已按当前实例状态初始化的 {@link Builder RetryPolicy.Builder}，\n\t * 可进一步配置，也可忽略后从头构建。\n\t *\n\t * @param factory a factory to customize the retry policy. 自定义重试策略的工厂\n\t */",
        ),
    ],
    "AliasKeyManagerFactory.java": [
        (
            "/**\n * {@link KeyManagerFactory} that allows a configurable key alias to be used. Due to the\n * fact that the actual calls to retrieve the key by alias are done at request time the\n * approach is to wrap the actual key managers with a {@link AliasX509ExtendedKeyManager}.\n * The actual SPI has to be wrapped as well due to the fact that\n * {@link KeyManagerFactory#getKeyManagers()} is final.\n *\n * @author Scott Frederick\n */",
            "/**\n * 支持使用可配置密钥别名的 {@link KeyManagerFactory}。\n * 由于按别名获取密钥的实际调用发生在请求时，因此用 {@link AliasX509ExtendedKeyManager}\n * 包装底层 KeyManager。由于 {@link KeyManagerFactory#getKeyManagers()} 为 final，\n * SPI 也需要一并包装。\n *\n * @author Scott Frederick\n */",
        ),
        (
            "\t/**\n\t * {@link KeyManagerFactorySpi} that allows a configurable key alias to be used.\n\t */",
            "\t/**\n\t * 支持使用可配置密钥别名的 {@link KeyManagerFactorySpi}。\n\t */",
        ),
        (
            "\t/**\n\t * {@link X509ExtendedKeyManager} that allows a configurable key alias to be used.\n\t */",
            "\t/**\n\t * 支持使用可配置密钥别名的 {@link X509ExtendedKeyManager}。\n\t */",
        ),
    ],
    "DefaultSslBundleRegistry.java": [
        (
            "/**\n * Default {@link SslBundleRegistry} implementation.\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @author Phillip Webb\n * @author Jonatan Ivanov\n * @since 3.1.0\n */",
            "/**\n * {@link SslBundleRegistry} 的默认实现，同时实现 {@link SslBundles}。\n * 管理命名 SSL 束的注册、更新与事件处理器。\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @author Phillip Webb\n * @author Jonatan Ivanov\n * @since 3.1.0\n */",
        ),
    ],
    "DefaultSslManagerBundle.java": [
        (
            "/**\n * Default implementation of {@link SslManagerBundle}.\n *\n * @author Scott Frederick\n * @see SslManagerBundle#from(SslStoreBundle, SslBundleKey)\n */",
            "/**\n * {@link SslManagerBundle} 的默认实现。\n * 根据 {@link SslStoreBundle} 与 {@link SslBundleKey} 创建 KeyManagerFactory 与 TrustManagerFactory。\n *\n * @author Scott Frederick\n * @see SslManagerBundle#from(SslStoreBundle, SslBundleKey)\n */",
        ),
    ],
    "FixedTrustManagerFactory.java": [
        (
            "/**\n * {@link TrustManagerFactory} which uses a fixed set of {@link TrustManager\n * TrustManagers}.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * 使用固定 {@link TrustManager TrustManagers} 集合的 {@link TrustManagerFactory}。\n * 用于在 SSL 束更新时替换信任管理器而不重新初始化底层工厂。\n *\n * @author Moritz Halbritter\n */",
        ),
    ],
    "NoSuchSslBundleException.java": [
        (
            "/**\n * Exception indicating that an {@link SslBundle} was referenced with a name that does not\n * match any registered bundle.\n *\n * @author Scott Frederick\n * @since 3.1.0\n */",
            "/**\n * 表示引用了不存在于注册表中的 {@link SslBundle} 名称时抛出的异常。\n *\n * @author Scott Frederick\n * @since 3.1.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code SslBundleNotFoundException} instance.\n\t * @param bundleName the name of the bundle that could not be found\n\t * @param message the exception message\n\t */",
            "\t/**\n\t * 创建新的 {@code NoSuchSslBundleException} 实例。\n\t *\n\t * @param bundleName the name of the bundle that could not be found 未找到的束名称\n\t * @param message the exception message 异常消息\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SslBundleNotFoundException} instance.\n\t * @param bundleName the name of the bundle that could not be found\n\t * @param message the exception message\n\t * @param cause the exception cause\n\t */",
            "\t/**\n\t * 创建新的 {@code NoSuchSslBundleException} 实例。\n\t *\n\t * @param bundleName the name of the bundle that could not be found 未找到的束名称\n\t * @param message the exception message 异常消息\n\t * @param cause the exception cause 异常原因\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the bundle that was not found.\n\t * @return the bundle name\n\t */",
            "\t/**\n\t * 返回未找到的 SSL 束名称。\n\t *\n\t * @return the bundle name SSL 束名称\n\t */",
        ),
    ],
    "SslBundle.java": [
        (
            "/**\n * A bundle of trust material that can be used to establish an SSL connection.\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @since 3.1.0\n */",
            "/**\n * 可用于建立 SSL 连接的信任材料束。\n * 聚合密钥库、密钥引用、SSL 选项与管理器工厂。\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @since 3.1.0\n */",
        ),
        (
            "\t/**\n\t * The default protocol to use.\n\t */",
            "\t/**\n\t * 默认使用的 SSL 协议。\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@link SslStoreBundle} that can be used to access this bundle's key and\n\t * trust stores.\n\t * @return the {@code SslStoreBundle} instance for this bundle\n\t */",
            "\t/**\n\t * 返回用于访问此束密钥库与信任库的 {@link SslStoreBundle}。\n\t *\n\t * @return the {@code SslStoreBundle} instance for this bundle 此束的 {@code SslStoreBundle} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Return a reference to the key that should be used for this bundle or\n\t * {@link SslBundleKey#NONE}.\n\t * @return a reference to the SSL key that should be used\n\t */",
            "\t/**\n\t * 返回此束应使用的密钥引用，或 {@link SslBundleKey#NONE}。\n\t *\n\t * @return a reference to the SSL key that should be used 应使用的 SSL 密钥引用\n\t */",
        ),
        (
            "\t/**\n\t * Return {@link SslOptions} that should be applied when establishing the SSL\n\t * connection.\n\t * @return the options that should be applied\n\t */",
            "\t/**\n\t * 返回建立 SSL 连接时应应用的 {@link SslOptions}。\n\t *\n\t * @return the options that should be applied 应应用的选项\n\t */",
        ),
        (
            "\t/**\n\t * Return the protocol to use when establishing the connection. Values should be\n\t * supported by {@link SSLContext#getInstance(String)}.\n\t * @return the SSL protocol\n\t * @see SSLContext#getInstance(String)\n\t */",
            "\t/**\n\t * 返回建立连接时使用的协议。取值应被 {@link SSLContext#getInstance(String)} 支持。\n\t *\n\t * @return the SSL protocol SSL 协议\n\t * @see SSLContext#getInstance(String)\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@link SslManagerBundle} that can be used to access this bundle's\n\t * {@link KeyManager key} and {@link TrustManager trust} managers.\n\t * @return the {@code SslManagerBundle} instance for this bundle\n\t */",
            "\t/**\n\t * 返回用于访问此束 {@link KeyManager 密钥} 与 {@link TrustManager 信任} 管理器的\n\t * {@link SslManagerBundle}。\n\t *\n\t * @return the {@code SslManagerBundle} instance for this bundle 此束的 {@code SslManagerBundle} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a new {@link SSLContext} for this bundle.\n\t * @return a new {@link SSLContext} instance\n\t */",
            "\t/**\n\t * 工厂方法：为此束创建新的 {@link SSLContext}。\n\t *\n\t * @return a new {@link SSLContext} instance 新的 {@link SSLContext} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a new {@link SslBundle} instance.\n\t * @param stores the stores or {@code null}\n\t * @return a new {@link SslBundle} instance\n\t */",
            "\t/**\n\t * 工厂方法：创建新的 {@link SslBundle} 实例。\n\t *\n\t * @param stores the stores or {@code null} 存储束或 {@code null}\n\t * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a new {@link SslBundle} instance.\n\t * @param stores the stores or {@code null}\n\t * @param key the key or {@code null}\n\t * @return a new {@link SslBundle} instance\n\t */",
            "\t/**\n\t * 工厂方法：创建新的 {@link SslBundle} 实例。\n\t *\n\t * @param stores the stores or {@code null} 存储束或 {@code null}\n\t * @param key the key or {@code null} 密钥或 {@code null}\n\t * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a new {@link SslBundle} instance.\n\t * @param stores the stores or {@code null}\n\t * @param key the key or {@code null}\n\t * @param options the options or {@code null}\n\t * @return a new {@link SslBundle} instance\n\t */",
            "\t/**\n\t * 工厂方法：创建新的 {@link SslBundle} 实例。\n\t *\n\t * @param stores the stores or {@code null} 存储束或 {@code null}\n\t * @param key the key or {@code null} 密钥或 {@code null}\n\t * @param options the options or {@code null} 选项或 {@code null}\n\t * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a new {@link SslBundle} instance.\n\t * @param stores the stores or {@code null}\n\t * @param key the key or {@code null}\n\t * @param options the options or {@code null}\n\t * @param protocol the protocol or {@code null}\n\t * @return a new {@link SslBundle} instance\n\t */",
            "\t/**\n\t * 工厂方法：创建新的 {@link SslBundle} 实例。\n\t *\n\t * @param stores the stores or {@code null} 存储束或 {@code null}\n\t * @param key the key or {@code null} 密钥或 {@code null}\n\t * @param options the options or {@code null} 选项或 {@code null}\n\t * @param protocol the protocol or {@code null} 协议或 {@code null}\n\t * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a new {@link SslBundle} instance.\n\t * @param stores the stores or {@code null}\n\t * @param key the key or {@code null}\n\t * @param options the options or {@code null}\n\t * @param protocol the protocol or {@code null}\n\t * @param managers the managers or {@code null}\n\t * @return a new {@link SslBundle} instance\n\t */",
            "\t/**\n\t * 工厂方法：创建新的 {@link SslBundle} 实例。\n\t *\n\t * @param stores the stores or {@code null} 存储束或 {@code null}\n\t * @param key the key or {@code null} 密钥或 {@code null}\n\t * @param options the options or {@code null} 选项或 {@code null}\n\t * @param protocol the protocol or {@code null} 协议或 {@code null}\n\t * @param managers the managers or {@code null} 管理器束或 {@code null}\n\t * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a new {@link SslBundle} which uses the system defaults.\n\t * @return a new {@link SslBundle} instance\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 工厂方法：创建使用系统默认 SSL 配置的 {@link SslBundle}。\n\t *\n\t * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例\n\t * @since 3.5.0\n\t */",
        ),
    ],
    "SslBundleKey.java": [
        (
            "/**\n * A reference to a single key obtained via {@link SslBundle}.\n *\n * @author Phillip Webb\n * @since 3.1.0\n */",
            "/**\n * 通过 {@link SslBundle} 获取的单个密钥引用。\n * 包含密钥密码与别名信息。\n *\n * @author Phillip Webb\n * @since 3.1.0\n */",
        ),
        (
            "\t/**\n\t * {@link SslBundleKey} that returns no values.\n\t */",
            "\t/**\n\t * 不返回任何值的 {@link SslBundleKey} 常量。\n\t */",
        ),
        (
            "\t/**\n\t * Return the password that should be used to access the key or {@code null} if no\n\t * password is required.\n\t * @return the key password\n\t */",
            "\t/**\n\t * 返回访问密钥应使用的密码；若无需密码则返回 {@code null}。\n\t *\n\t * @return the key password 密钥密码\n\t */",
        ),
        (
            "\t/**\n\t * Return the alias of the key or {@code null} if the key has no alias.\n\t * @return the key alias\n\t */",
            "\t/**\n\t * 返回密钥别名；若密钥无别名则返回 {@code null}。\n\t *\n\t * @return the key alias 密钥别名\n\t */",
        ),
        (
            "\t/**\n\t * Assert that the alias is contained in the given keystore.\n\t * @param keyStore the keystore to check\n\t */",
            "\t/**\n\t * 断言给定密钥库包含此别名。\n\t *\n\t * @param keyStore the keystore to check 待检查的密钥库\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a new {@link SslBundleKey} instance.\n\t * @param password the password used to access the key\n\t * @return a new {@link SslBundleKey} instance\n\t */",
            "\t/**\n\t * 工厂方法：创建新的 {@link SslBundleKey} 实例。\n\t *\n\t * @param password the password used to access the key 访问密钥的密码\n\t * @return a new {@link SslBundleKey} instance 新的 {@link SslBundleKey} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a new {@link SslBundleKey} instance.\n\t * @param password the password used to access the key\n\t * @param alias the alias of the key\n\t * @return a new {@link SslBundleKey} instance\n\t */",
            "\t/**\n\t * 工厂方法：创建新的 {@link SslBundleKey} 实例。\n\t *\n\t * @param password the password used to access the key 访问密钥的密码\n\t * @param alias the alias of the key 密钥别名\n\t * @return a new {@link SslBundleKey} instance 新的 {@link SslBundleKey} 实例\n\t */",
        ),
    ],
    "SslBundleRegistry.java": [
        (
            "/**\n * Interface that can be used to register an {@link SslBundle} for a given name.\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @since 3.1.0\n */",
            "/**\n * 可为指定名称注册 {@link SslBundle} 的接口。\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @since 3.1.0\n */",
        ),
        (
            "\t/**\n\t * Register a named {@link SslBundle}.\n\t * @param name the bundle name\n\t * @param bundle the bundle\n\t */",
            "\t/**\n\t * 注册具名 {@link SslBundle}。\n\t *\n\t * @param name the bundle name 束名称\n\t * @param bundle the bundle SSL 束\n\t */",
        ),
        (
            "\t/**\n\t * Updates an {@link SslBundle}.\n\t * @param name the bundle name\n\t * @param updatedBundle the updated bundle\n\t * @throws NoSuchSslBundleException if the bundle cannot be found\n\t * @since 3.2.0\n\t */",
            "\t/**\n\t * 更新 {@link SslBundle}。\n\t *\n\t * @param name the bundle name 束名称\n\t * @param updatedBundle the updated bundle 更新后的 SSL 束\n\t * @throws NoSuchSslBundleException if the bundle cannot be found 若找不到束则抛出\n\t * @since 3.2.0\n\t */",
        ),
    ],
    "SslBundles.java": [
        (
            "/**\n * A managed set of {@link SslBundle} instances that can be retrieved by name.\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @author Jonatan Ivanov\n * @since 3.1.0\n */",
            "/**\n * 可按名称检索的受管 {@link SslBundle} 实例集合。\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @author Jonatan Ivanov\n * @since 3.1.0\n */",
        ),
        (
            "\t/**\n\t * Return an {@link SslBundle} with the provided name.\n\t * @param name the bundle name\n\t * @return the bundle\n\t * @throws NoSuchSslBundleException if a bundle with the provided name does not exist\n\t */",
            "\t/**\n\t * 返回指定名称的 {@link SslBundle}。\n\t *\n\t * @param name the bundle name 束名称\n\t * @return the bundle SSL 束\n\t * @throws NoSuchSslBundleException if a bundle with the provided name does not exist 若不存在则抛出\n\t */",
        ),
        (
            "\t/**\n\t * Add a handler that will be called each time the named bundle is updated.\n\t * @param name the bundle name\n\t * @param updateHandler the handler that should be called\n\t * @throws NoSuchSslBundleException if a bundle with the provided name does not exist\n\t * @since 3.2.0\n\t */",
            "\t/**\n\t * 添加在指定束每次更新时调用的处理器。\n\t *\n\t * @param name the bundle name 束名称\n\t * @param updateHandler the handler that should be called 更新处理器\n\t * @throws NoSuchSslBundleException if a bundle with the provided name does not exist 若不存在则抛出\n\t * @since 3.2.0\n\t */",
        ),
        (
            "\t/**\n\t * Add a handler that will be called each time a bundle is registered. The handler\n\t * will be called with the bundle name and the bundle.\n\t * @param registerHandler the handler that should be called\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 添加在每次注册束时调用的处理器。\n\t * 处理器接收束名称与束实例作为参数。\n\t *\n\t * @param registerHandler the handler that should be called 注册处理器\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Return the names of all bundles managed by this instance.\n\t * @return the bundle names\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 返回此实例管理的全部束名称。\n\t *\n\t * @return the bundle names 束名称列表\n\t * @since 3.4.0\n\t */",
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
                "wave13a origin/ssl [0:20]",
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
