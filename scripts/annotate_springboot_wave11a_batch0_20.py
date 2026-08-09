#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-11a slice [0:20] (json + logging)."""
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
BATCH_FILES = Path("/tmp/springboot_w11a.txt").read_text(encoding="utf-8").strip().splitlines()

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "JsonParserFactory.java": [
        (
            "/**\n * Factory to create a {@link JsonParser}.\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see JacksonJsonParser\n * @see GsonJsonParser\n * @see BasicJsonParser\n */",
            "/**\n * 创建 {@link JsonParser} 的工厂。\n * 按类路径可用性依次尝试 Jackson、Gson，最后回退到 {@link BasicJsonParser}。\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see JacksonJsonParser\n * @see GsonJsonParser\n * @see BasicJsonParser\n */",
        ),
        (
            "\t/**\n\t * Static factory for the \"best\" JSON parser available on the classpath. Tries\n\t * Jackson, then Gson, and then falls back to the {@link BasicJsonParser}.\n\t * @return a {@link JsonParser}\n\t */",
            "\t/**\n\t * 类路径上“最佳” JSON 解析器的静态工厂。\n\t * 依次尝试 Jackson、Gson，最后回退到 {@link BasicJsonParser}。\n\t *\n\t * @return a {@link JsonParser} JSON 解析器\n\t */",
        ),
    ],
    "JsonWriterFiltersAndProcessors.java": [
        (
            "/**\n * Internal record used to hold {@link NameProcessor} and {@link ValueProcessor}\n * instances.\n *\n * @author Phillip Webb\n * @param pathFilters the path filters\n * @param nameProcessors the name processors\n * @param valueProcessors the value processors\n */",
            "/**\n * 内部 record，用于持有 {@link NameProcessor} 与 {@link ValueProcessor} 实例。\n *\n * @author Phillip Webb\n * @param pathFilters the path filters 路径过滤器\n * @param nameProcessors the name processors 名称处理器\n * @param valueProcessors the value processors 值处理器\n */",
        ),
    ],
    "WritableJson.java": [
        (
            "/**\n * JSON content that can be written out.\n *\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 3.4.0\n * @see JsonWriter\n */",
            "/**\n * 可写出 JSON 内容的接口。\n * 提供写入 {@link Appendable}、字符串、字节数组及资源等多种输出方式。\n *\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 3.4.0\n * @see JsonWriter\n */",
        ),
        (
            "\t/**\n\t * Write the JSON to the provided {@link Appendable}.\n\t * @param out the {@link Appendable} to receive the JSON\n\t * @throws IOException on IO error\n\t */",
            "\t/**\n\t * 将 JSON 写入给定 {@link Appendable}。\n\t *\n\t * @param out 接收 JSON 的 {@link Appendable}\n\t * @throws IOException on IO error IO 错误时\n\t */",
        ),
        (
            "\t/**\n\t * Write the JSON to a {@link String}.\n\t * @return the JSON string\n\t */",
            "\t/**\n\t * 将 JSON 写入 {@link String}。\n\t *\n\t * @return the JSON string JSON 字符串\n\t */",
        ),
        (
            "\t/**\n\t * Write the JSON to a UTF-8 encoded byte array.\n\t * @return the JSON bytes\n\t */",
            "\t/**\n\t * 将 JSON 写入 UTF-8 编码字节数组。\n\t *\n\t * @return the JSON bytes JSON 字节\n\t */",
        ),
        (
            "\t/**\n\t * Write the JSON to a byte array.\n\t * @param charset the charset\n\t * @return the JSON bytes\n\t */",
            "\t/**\n\t * 将 JSON 写入字节数组。\n\t *\n\t * @param charset 字符集\n\t * @return the JSON bytes JSON 字节\n\t */",
        ),
        (
            "\t/**\n\t * Write the JSON to the provided {@link WritableResource} using\n\t * {@link StandardCharsets#UTF_8 UTF8} encoding.\n\t * @param out the {@link OutputStream} to receive the JSON\n\t * @throws IOException on IO error\n\t */",
            "\t/**\n\t * 使用 {@link StandardCharsets#UTF_8 UTF8} 编码将 JSON 写入给定 {@link WritableResource}。\n\t *\n\t * @param out 接收 JSON 的 {@link OutputStream}\n\t * @throws IOException on IO error IO 错误时\n\t */",
        ),
        (
            "\t/**\n\t * Write the JSON to the provided {@link WritableResource} using the given\n\t * {@link Charset}.\n\t * @param out the {@link OutputStream} to receive the JSON\n\t * @param charset the charset to use\n\t * @throws IOException on IO error\n\t */",
            "\t/**\n\t * 使用给定 {@link Charset} 将 JSON 写入 {@link WritableResource}。\n\t *\n\t * @param out 接收 JSON 的 {@link OutputStream}\n\t * @param charset 要使用的字符集\n\t * @throws IOException on IO error IO 错误时\n\t */",
        ),
        (
            "\t/**\n\t * Write the JSON to the provided {@link OutputStream} using\n\t * {@link StandardCharsets#UTF_8 UTF8} encoding. The output stream will not be closed.\n\t * @param out the {@link OutputStream} to receive the JSON\n\t * @throws IOException on IO error\n\t * @see #toOutputStream(OutputStream, Charset)\n\t */",
            "\t/**\n\t * 使用 {@link StandardCharsets#UTF_8 UTF8} 编码将 JSON 写入 {@link OutputStream}。\n\t * 输出流不会被关闭。\n\t *\n\t * @param out 接收 JSON 的 {@link OutputStream}\n\t * @throws IOException on IO error IO 错误时\n\t * @see #toOutputStream(OutputStream, Charset)\n\t */",
        ),
        (
            "\t/**\n\t * Write the JSON to the provided {@link OutputStream} using the given\n\t * {@link Charset}. The output stream will not be closed.\n\t * @param out the {@link OutputStream} to receive the JSON\n\t * @param charset the charset to use\n\t * @throws IOException on IO error\n\t */",
            "\t/**\n\t * 使用给定 {@link Charset} 将 JSON 写入 {@link OutputStream}。\n\t * 输出流不会被关闭。\n\t *\n\t * @param out 接收 JSON 的 {@link OutputStream}\n\t * @param charset 要使用的字符集\n\t * @throws IOException on IO error IO 错误时\n\t */",
        ),
        (
            "\t/**\n\t * Write the JSON to the provided {@link Writer}. The writer will be flushed but not\n\t * closed.\n\t * @param out the {@link Writer} to receive the JSON\n\t * @throws IOException on IO error\n\t * @see #toOutputStream(OutputStream, Charset)\n\t */",
            "\t/**\n\t * 将 JSON 写入给定 {@link Writer}。\n\t * Writer 会被刷新但不会关闭。\n\t *\n\t * @param out 接收 JSON 的 {@link Writer}\n\t * @throws IOException on IO error IO 错误时\n\t * @see #toOutputStream(OutputStream, Charset)\n\t */",
        ),
        (
            "\t/**\n\t * Factory method used to create a {@link WritableJson} with a sensible\n\t * {@link Object#toString()} that delegate to {@link WritableJson#toJsonString()}.\n\t * @param writableJson the source {@link WritableJson}\n\t * @return a new {@link WritableJson} with a sensible {@link Object#toString()}.\n\t */",
            "\t/**\n\t * 创建 {@link WritableJson} 的工厂方法，其 {@link Object#toString()} 委托给\n\t * {@link WritableJson#toJsonString()}。\n\t *\n\t * @param writableJson 源 {@link WritableJson}\n\t * @return a new {@link WritableJson} with a sensible {@link Object#toString()} 带合理 toString 的新实例\n\t */",
        ),
    ],
    "AbstractLoggingSystem.java": [
        (
            "/**\n * Abstract base class for {@link LoggingSystem} implementations.\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @since 1.0.0\n */",
            "/**\n * {@link LoggingSystem} 实现的抽象基类。\n * 提供基于约定或显式配置位置的初始化流程。\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @since 1.0.0\n */",
        ),
        (
            "\t/**\n\t * Return any self initialization config that has been applied. By default this method\n\t * checks {@link #getStandardConfigLocations()} and assumes that any file that exists\n\t * will have been applied.\n\t * @return the self initialization config or {@code null}\n\t */",
            "\t/**\n\t * 返回已应用的自初始化配置。\n\t * 默认检查 {@link #getStandardConfigLocations()}，假定存在的文件已被应用。\n\t *\n\t * @return the self initialization config or {@code null} 自初始化配置或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Return any spring specific initialization config that should be applied. By default\n\t * this method checks {@link #getSpringConfigLocations()}.\n\t * @return the spring initialization config or {@code null}\n\t */",
            "\t/**\n\t * 返回应应用的 Spring 专用初始化配置。\n\t * 默认检查 {@link #getSpringConfigLocations()}。\n\t *\n\t * @return the spring initialization config or {@code null} Spring 初始化配置或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Return the standard config locations for this system.\n\t * @return the standard config locations\n\t * @see #getSelfInitializationConfig()\n\t */",
            "\t/**\n\t * 返回此系统的标准配置位置。\n\t *\n\t * @return the standard config locations 标准配置位置\n\t * @see #getSelfInitializationConfig()\n\t */",
        ),
        (
            "\t/**\n\t * Return the spring config locations for this system. By default this method returns\n\t * a set of locations based on {@link #getStandardConfigLocations()}.\n\t * @return the spring config locations\n\t * @see #getSpringInitializationConfig()\n\t */",
            "\t/**\n\t * 返回此系统的 Spring 配置位置。\n\t * 默认基于 {@link #getStandardConfigLocations()} 生成带 {@code -spring} 后缀的位置。\n\t *\n\t * @return the spring config locations Spring 配置位置\n\t * @see #getSpringInitializationConfig()\n\t */",
        ),
        (
            "\t/**\n\t * Load sensible defaults for the logging system.\n\t * @param initializationContext the logging initialization context\n\t * @param logFile the file to load or {@code null} if no log file is to be written\n\t */",
            "\t/**\n\t * 加载日志系统的合理默认配置。\n\t *\n\t * @param initializationContext 日志初始化上下文\n\t * @param logFile 要写入的日志文件，不写入时为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Load a specific configuration.\n\t * @param initializationContext the logging initialization context\n\t * @param location the location of the configuration to load (never {@code null})\n\t * @param logFile the file to load or {@code null} if no log file is to be written\n\t */",
            "\t/**\n\t * 加载指定配置。\n\t *\n\t * @param initializationContext 日志初始化上下文\n\t * @param location 要加载的配置位置（永不为 {@code null}）\n\t * @param logFile 要写入的日志文件，不写入时为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Reinitialize the logging system if required. Called when\n\t * {@link #getSelfInitializationConfig()} is used and the log file hasn't changed. May\n\t * be used to reload configuration (for example to pick up additional System\n\t * properties).\n\t * @param initializationContext the logging initialization context\n\t */",
            "\t/**\n\t * 必要时重新初始化日志系统。\n\t * 在已使用 {@link #getSelfInitializationConfig()} 且日志文件未变更时调用，\n\t * 可用于重载配置（例如拾取新增系统属性）。\n\t *\n\t * @param initializationContext 日志初始化上下文\n\t */",
        ),
        (
            "\t/**\n\t * Return the default value resolver to use when resolving system properties.\n\t * @param environment the environment\n\t * @return the default value resolver\n\t * @since 3.2.0\n\t */",
            "\t/**\n\t * 解析系统属性时使用的默认值解析器。\n\t *\n\t * @param environment 环境\n\t * @return the default value resolver 默认值解析器\n\t * @since 3.2.0\n\t */",
        ),
        (
            "\t/**\n\t * Return the default log correlation pattern or {@code null} if log correlation\n\t * patterns are not supported.\n\t * @return the default log correlation pattern\n\t * @since 3.2.0\n\t */",
            "\t/**\n\t * 返回默认日志关联 ID 模式；不支持时返回 {@code null}。\n\t *\n\t * @return the default log correlation pattern 默认关联 ID 模式\n\t * @since 3.2.0\n\t */",
        ),
        (
            "\t/**\n\t * Maintains a mapping between native levels and {@link LogLevel}.\n\t *\n\t * @param <T> the native level type\n\t */",
            "\t/**\n\t * 维护原生日志级别与 {@link LogLevel} 之间的映射。\n\t *\n\t * @param <T> the native level type 原生级别类型\n\t */",
        ),
    ],
    "CorrelationIdFormatter.java": [
        (
            "/**\n * Utility class that can be used to format a correlation identifier for logging based on\n * <a href=\n * \"https://www.w3.org/TR/trace-context/#examples-of-http-traceparent-headers\">W3C</a>\n * recommendations.\n * <p>\n * The formatter can be configured with a comma-separated list of names and the expected\n * length of their resolved value. Each item should be specified in the form\n * {@code \"<name>(length)\"}. For example, {@code \"traceId(32),spanId(16)\"} specifies the\n * names {@code \"traceId\"} and {@code \"spanId\"} with expected lengths of {@code 32} and\n * {@code 16} respectively.\n * <p>\n * Correlation IDs are formatted as dash separated strings surrounded in square brackets.\n * Formatted output is always of a fixed width and with trailing space. Dashes are omitted\n * if none of the named items can be resolved.\n * <p>\n * The following example would return a formatted result of\n * {@code \"[01234567890123456789012345678901-0123456789012345] \"}: <pre class=\"code\">\n * CorrelationIdFormatter formatter = CorrelationIdFormatter.of(\"traceId(32),spanId(16)\");\n * Map&lt;String, String&gt; mdc = Map.of(\"traceId\", \"01234567890123456789012345678901\", \"spanId\", \"0123456789012345\");\n * return formatter.format(mdc::get);\n * </pre>\n * <p>\n * If {@link #of(String)} is called with an empty spec the {@link #DEFAULT} formatter will\n * be used.\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see #of(String)\n * @see #of(Collection)\n */",
            "/**\n * 基于 <a href=\n * \"https://www.w3.org/TR/trace-context/#examples-of-http-traceparent-headers\">W3C</a>\n * 建议格式化日志关联 ID 的工具类。\n * <p>\n * 可通过逗号分隔的名称列表及期望解析长度进行配置，每项格式为 {@code \"<name>(length)\"}。\n * 例如 {@code \"traceId(32),spanId(16)\"} 指定 {@code traceId} 与 {@code spanId}，长度分别为 32 与 16。\n * <p>\n * 关联 ID 格式化为方括号内以短横线分隔的固定宽度字符串，末尾带空格；\n * 若所有命名项均无法解析则省略短横线。\n * <p>\n * 若 {@link #of(String)} 传入空规格，则使用 {@link #DEFAULT} 格式化器。\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see #of(String)\n * @see #of(Collection)\n */",
        ),
        (
            "\t/**\n\t * Default {@link CorrelationIdFormatter}.\n\t */",
            "\t/**\n\t * 默认 {@link CorrelationIdFormatter}。\n\t */",
        ),
        (
            "\t/**\n\t * Format a correlation from the values in the given resolver.\n\t * @param resolver the resolver used to resolve named values\n\t * @return a formatted correlation id\n\t */",
            "\t/**\n\t * 根据解析器中的值格式化关联 ID。\n\t *\n\t * @param resolver 用于解析命名值的解析器\n\t * @return a formatted correlation id 格式化后的关联 ID\n\t */",
        ),
        (
            "\t/**\n\t * Format a correlation from the values in the given resolver and append it to the\n\t * given {@link Appendable}.\n\t * @param resolver the resolver used to resolve named values\n\t * @param appendable the appendable for the formatted correlation id\n\t */",
            "\t/**\n\t * 格式化关联 ID 并追加到给定 {@link Appendable}。\n\t *\n\t * @param resolver 用于解析命名值的解析器\n\t * @param appendable 接收格式化关联 ID 的可追加对象\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link CorrelationIdFormatter} instance from the given specification.\n\t * @param spec a comma-separated specification\n\t * @return a new {@link CorrelationIdFormatter} instance\n\t */",
            "\t/**\n\t * 根据给定规格创建新的 {@link CorrelationIdFormatter} 实例。\n\t *\n\t * @param spec 逗号分隔的规格字符串\n\t * @return a new {@link CorrelationIdFormatter} instance 新实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link CorrelationIdFormatter} instance from the given specification.\n\t * @param spec a pre-separated specification\n\t * @return a new {@link CorrelationIdFormatter} instance\n\t */",
            "\t/**\n\t * 根据预分割规格创建新的 {@link CorrelationIdFormatter} 实例。\n\t *\n\t * @param spec 预分割规格\n\t * @return a new {@link CorrelationIdFormatter} instance 新实例\n\t */",
        ),
        (
            "\t/**\n\t * A part of the correlation id.\n\t *\n\t * @param name the name of the correlation part\n\t * @param length the expected length of the correlation part\n\t */",
            "\t/**\n\t * 关联 ID 的一个组成部分。\n\t *\n\t * @param name the name of the correlation part 关联部分名称\n\t * @param length the expected length of the correlation part 期望长度\n\t */",
        ),
    ],
    "DeferredLog.java": [
        (
            "/**\n * Deferred {@link Log} that can be used to store messages that shouldn't be written until\n * the logging system is fully initialized.\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
            "/**\n * 延迟 {@link Log}，用于暂存日志系统完全初始化前不应输出的消息。\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link DeferredLog} instance.\n\t */",
            "\t/**\n\t * 创建新的 {@link DeferredLog} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link DeferredLog} instance managed by a {@link DeferredLogFactory}.\n\t * @param destination the switch-over destination\n\t * @param lines the lines backing all related deferred logs\n\t * @since 2.4.0\n\t */",
            "\t/**\n\t * 由 {@link DeferredLogFactory} 管理的 {@link DeferredLog} 实例。\n\t *\n\t * @param destination 切换后的目标日志\n\t * @param lines 所有相关延迟日志共享的行缓冲\n\t * @since 2.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Switch from deferred logging to immediate logging to the specified destination.\n\t * @param destination the new log destination\n\t * @since 2.1.0\n\t */",
            "\t/**\n\t * 从延迟日志切换到指定目标的即时日志。\n\t *\n\t * @param destination 新的日志目标类\n\t * @since 2.1.0\n\t */",
        ),
        (
            "\t/**\n\t * Switch from deferred logging to immediate logging to the specified destination.\n\t * @param destination the new log destination\n\t * @since 2.1.0\n\t */",
            "\t/**\n\t * 从延迟日志切换到指定目标的即时日志。\n\t *\n\t * @param destination 新的日志目标\n\t * @since 2.1.0\n\t */",
        ),
        (
            "\t/**\n\t * Replay deferred logging to the specified destination.\n\t * @param destination the destination for the deferred log messages\n\t */",
            "\t/**\n\t * 将延迟日志重放到指定目标。\n\t *\n\t * @param destination 延迟消息的目标日志类\n\t */",
        ),
        (
            "\t/**\n\t * Replay deferred logging to the specified destination.\n\t * @param destination the destination for the deferred log messages\n\t */",
            "\t/**\n\t * 将延迟日志重放到指定目标。\n\t *\n\t * @param destination 延迟消息的目标日志\n\t */",
        ),
        (
            "\t/**\n\t * Replay from a source log to a destination log when the source is deferred.\n\t * @param source the source logger\n\t * @param destination the destination logger class\n\t * @return the destination\n\t */",
            "\t/**\n\t * 当源为延迟日志时，将消息重放到目标日志。\n\t *\n\t * @param source 源 Logger\n\t * @param destination 目标 Logger 类\n\t * @return the destination 目标 Logger\n\t */",
        ),
        (
            "\t/**\n\t * Replay from a source log to a destination log when the source is deferred.\n\t * @param source the source logger\n\t * @param destination the destination logger\n\t * @return the destination\n\t */",
            "\t/**\n\t * 当源为延迟日志时，将消息重放到目标日志。\n\t *\n\t * @param source 源 Logger\n\t * @param destination 目标 Logger\n\t * @return the destination 目标 Logger\n\t */",
        ),
    ],
    "DeferredLogFactory.java": [
        (
            "/**\n * Factory that can be used to create multiple {@link DeferredLog} instances that will\n * switch over when appropriate.\n *\n * @author Phillip Webb\n * @since 2.4.0\n * @see DeferredLogs\n */",
            "/**\n * 可创建多个在适当时机切换的 {@link DeferredLog} 实例的工厂。\n *\n * @author Phillip Webb\n * @since 2.4.0\n * @see DeferredLogs\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link DeferredLog} for the given destination.\n\t * @param destination the ultimate log destination\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate.\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@link DeferredLog}。\n\t *\n\t * @param destination 最终日志目标类\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate 适当时切换的延迟日志实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link DeferredLog} for the given destination.\n\t * @param destination the ultimate log destination\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate.\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@link DeferredLog}。\n\t *\n\t * @param destination 最终日志目标\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate 适当时切换的延迟日志实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link DeferredLog} for the given destination.\n\t * @param destination the ultimate log destination\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate.\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@link DeferredLog}。\n\t *\n\t * @param destination 最终日志目标供应器\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate 适当时切换的延迟日志实例\n\t */",
        ),
    ],
    "DeferredLogs.java": [
        (
            "/**\n * A {@link DeferredLogFactory} implementation that manages a collection\n * {@link DeferredLog} instances.\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 管理一组 {@link DeferredLog} 实例的 {@link DeferredLogFactory} 实现。\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link DeferredLog} for the given destination.\n\t * @param destination the ultimate log destination\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate.\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@link DeferredLog}。\n\t *\n\t * @param destination 最终日志目标类\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate 适当时切换的延迟日志实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link DeferredLog} for the given destination.\n\t * @param destination the ultimate log destination\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate.\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@link DeferredLog}。\n\t *\n\t * @param destination 最终日志目标\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate 适当时切换的延迟日志实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link DeferredLog} for the given destination.\n\t * @param destination the ultimate log destination\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate.\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@link DeferredLog}。\n\t *\n\t * @param destination 最终日志目标供应器\n\t * @return a deferred log instance that will switch to the destination when\n\t * appropriate 适当时切换的延迟日志实例\n\t */",
        ),
        (
            "\t/**\n\t * Switch over all deferred logs to their supplied destination.\n\t */",
            "\t/**\n\t * 将所有延迟日志切换到各自的目标。\n\t */",
        ),
    ],
    "DelegatingLoggingSystemFactory.java": [
        (
            "/**\n * {@link LoggingSystemFactory} that delegates to other factories.\n *\n * @author Phillip Webb\n */",
            "/**\n * 委托给其他工厂的 {@link LoggingSystemFactory}。\n * 依次尝试各委托工厂直至返回非空 {@link LoggingSystem}。\n *\n * @author Phillip Webb\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link DelegatingLoggingSystemFactory} instance.\n\t * @param delegates a function that provides the delegates\n\t */",
            "\t/**\n\t * 创建新的 {@link DelegatingLoggingSystemFactory} 实例。\n\t *\n\t * @param delegates 提供委托工厂列表的函数\n\t */",
        ),
    ],
    "LogFile.java": [
        (
            "/**\n * A reference to a log output file. Log output files are specified using\n * {@code logging.file.name} or {@code logging.file.path} {@link Environment} properties.\n * If the {@code logging.file.name} property is not specified {@code \"spring.log\"} will be\n * written in the {@code logging.file.path} directory.\n *\n * @author Phillip Webb\n * @author Christian Carriere-Tisseur\n * @since 1.2.1\n * @see #get(PropertyResolver)\n */",
            "/**\n * 日志输出文件的引用。\n * 通过 {@code logging.file.name} 或 {@code logging.file.path} {@link Environment} 属性指定；\n * 未指定 {@code logging.file.name} 时，在 {@code logging.file.path} 目录写入 {@code spring.log}。\n *\n * @author Phillip Webb\n * @author Christian Carriere-Tisseur\n * @since 1.2.1\n * @see #get(PropertyResolver)\n */",
        ),
        (
            "\t/**\n\t * The name of the Spring property that contains the name of the log file. Names can\n\t * be an exact location or relative to the current directory.\n\t * @since 2.2.0\n\t */",
            "\t/**\n\t * 包含日志文件名的 Spring 属性名。\n\t * 可为绝对路径或相对当前目录的路径。\n\t *\n\t * @since 2.2.0\n\t */",
        ),
        (
            "\t/**\n\t * The name of the Spring property that contains the directory where log files are\n\t * written.\n\t * @since 2.2.0\n\t */",
            "\t/**\n\t * 包含日志文件写入目录的 Spring 属性名。\n\t *\n\t * @since 2.2.0\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link LogFile} instance.\n\t * @param file a reference to the file to write\n\t */",
            "\t/**\n\t * 创建新的 {@link LogFile} 实例。\n\t *\n\t * @param file 要写入的文件引用\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link LogFile} instance.\n\t * @param file a reference to the file to write\n\t * @param path a reference to the logging path to use if {@code file} is not specified\n\t */",
            "\t/**\n\t * 创建新的 {@link LogFile} 实例。\n\t *\n\t * @param file 要写入的文件引用\n\t * @param path 未指定 {@code file} 时使用的日志目录\n\t */",
        ),
        (
            "\t/**\n\t * Apply log file details to {@code LOG_PATH} and {@code LOG_FILE} system properties.\n\t */",
            "\t/**\n\t * 将日志文件详情应用到 {@code LOG_PATH} 与 {@code LOG_FILE} 系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Apply log file details to {@code LOG_PATH} and {@code LOG_FILE} map entries.\n\t * @param properties the properties to apply to\n\t */",
            "\t/**\n\t * 将日志文件详情应用到 {@code LOG_PATH} 与 {@code LOG_FILE} 映射项。\n\t *\n\t * @param properties 要应用到的属性\n\t */",
        ),
        (
            "\t/**\n\t * Get a {@link LogFile} from the given Spring {@link Environment}.\n\t * @param propertyResolver the {@link PropertyResolver} used to obtain the logging\n\t * properties\n\t * @return a {@link LogFile} or {@code null} if the environment didn't contain any\n\t * suitable properties\n\t */",
            "\t/**\n\t * 从给定 Spring {@link Environment} 获取 {@link LogFile}。\n\t *\n\t * @param propertyResolver 用于获取日志属性的 {@link PropertyResolver}\n\t * @return a {@link LogFile} or {@code null} LogFile 实例，无合适属性时为 {@code null}\n\t */",
        ),
    ],
    "LogLevel.java": [
        (
            "/**\n * Logging levels supported by a {@link LoggingSystem}.\n *\n * @author Phillip Webb\n * @since 1.0.0\n */",
            "/**\n * {@link LoggingSystem} 支持的日志级别。\n *\n * @author Phillip Webb\n * @since 1.0.0\n */",
        ),
        (
            "\t/**\n\t * Log a message to the given logger at this level.\n\t * @param logger the logger\n\t * @param message the message to log\n\t * @since 3.1.0\n\t */",
            "\t/**\n\t * 以此级别向给定 Logger 记录消息。\n\t *\n\t * @param logger Logger\n\t * @param message 要记录的消息\n\t * @since 3.1.0\n\t */",
        ),
        (
            "\t/**\n\t * Log a message to the given logger at this level.\n\t * @param logger the logger\n\t * @param message the message to log\n\t * @param cause the cause to log\n\t * @since 3.1.0\n\t */",
            "\t/**\n\t * 以此级别向给定 Logger 记录消息及原因。\n\t *\n\t * @param logger Logger\n\t * @param message 要记录的消息\n\t * @param cause 要记录的原因\n\t * @since 3.1.0\n\t */",
        ),
    ],
    "LoggerConfiguration.java": [
        (
            "/**\n * Immutable class that represents the configuration of a {@link LoggingSystem}'s logger.\n *\n * @author Ben Hale\n * @author Phillip Webb\n * @since 1.5.0\n */",
            "/**\n * 表示 {@link LoggingSystem} 中某个 Logger 配置的不变类。\n *\n * @author Ben Hale\n * @author Phillip Webb\n * @since 1.5.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link LoggerConfiguration instance}.\n\t * @param name the name of the logger\n\t * @param configuredLevel the configured level of the logger\n\t * @param effectiveLevel the effective level of the logger\n\t */",
            "\t/**\n\t * 创建新的 {@link LoggerConfiguration} 实例。\n\t *\n\t * @param name Logger 名称\n\t * @param configuredLevel 已配置的级别\n\t * @param effectiveLevel 有效级别\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link LoggerConfiguration instance}.\n\t * @param name the name of the logger\n\t * @param levelConfiguration the level configuration\n\t * @param inheritedLevelConfiguration the inherited level configuration\n\t * @since 2.7.13\n\t */",
            "\t/**\n\t * 创建新的 {@link LoggerConfiguration} 实例。\n\t *\n\t * @param name Logger 名称\n\t * @param levelConfiguration 级别配置\n\t * @param inheritedLevelConfiguration 继承的级别配置\n\t * @since 2.7.13\n\t */",
        ),
        (
            "\t/**\n\t * Returns the name of the logger.\n\t * @return the name of the logger\n\t */",
            "\t/**\n\t * 返回 Logger 名称。\n\t *\n\t * @return the name of the logger Logger 名称\n\t */",
        ),
        (
            "\t/**\n\t * Returns the configured level of the logger.\n\t * @return the configured level of the logger\n\t * @see #getLevelConfiguration(ConfigurationScope)\n\t */",
            "\t/**\n\t * 返回 Logger 已配置的级别。\n\t *\n\t * @return the configured level of the logger 已配置级别\n\t * @see #getLevelConfiguration(ConfigurationScope)\n\t */",
        ),
        (
            "\t/**\n\t * Returns the effective level of the logger.\n\t * @return the effective level of the logger\n\t * @see #getLevelConfiguration(ConfigurationScope)\n\t */",
            "\t/**\n\t * 返回 Logger 的有效级别。\n\t *\n\t * @return the effective level of the logger 有效级别\n\t * @see #getLevelConfiguration(ConfigurationScope)\n\t */",
        ),
        (
            "\t/**\n\t * Return the level configuration, considering inherited loggers.\n\t * @return the level configuration\n\t * @since 2.7.13\n\t */",
            "\t/**\n\t * 返回考虑继承 Logger 后的级别配置。\n\t *\n\t * @return the level configuration 级别配置\n\t * @since 2.7.13\n\t */",
        ),
        (
            "\t/**\n\t * Return the level configuration for the given scope.\n\t * @param scope the configuration scope\n\t * @return the level configuration or {@code null} for\n\t * {@link ConfigurationScope#DIRECT direct scope} results without applied\n\t * configuration\n\t * @since 2.7.13\n\t */",
            "\t/**\n\t * 返回给定作用域的级别配置。\n\t *\n\t * @param scope 配置作用域\n\t * @return the level configuration or {@code null} 级别配置；\n\t * {@link ConfigurationScope#DIRECT} 且无直接配置时为 {@code null}\n\t * @since 2.7.13\n\t */",
        ),
        (
            "\t/**\n\t * Supported logger configuration scopes.\n\t *\n\t * @since 2.7.13\n\t */",
            "\t/**\n\t * 支持的 Logger 配置作用域。\n\t *\n\t * @since 2.7.13\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Only return configuration that has been applied directly. Often referred to as\n\t\t * 'configured' or 'assigned' configuration.\n\t\t */",
            "\t\t/**\n\t\t * 仅返回直接应用的配置，常称为“已配置”或“已分配”配置。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * May return configuration that has been applied to a parent logger. Often\n\t\t * referred to as 'effective' configuration.\n\t\t */",
            "\t\t/**\n\t\t * 可能返回父 Logger 应用的配置，常称为“有效”配置。\n\t\t */",
        ),
        (
            "\t/**\n\t * Logger level configuration.\n\t *\n\t * @since 2.7.13\n\t */",
            "\t/**\n\t * Logger 级别配置。\n\t *\n\t * @since 2.7.13\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Return the name of the level.\n\t\t * @return the level name\n\t\t */",
            "\t\t/**\n\t\t * 返回级别名称。\n\t\t *\n\t\t * @return the level name 级别名称\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Return the actual level value if possible.\n\t\t * @return the level value\n\t\t * @throws IllegalStateException if this is a {@link #isCustom() custom} level\n\t\t */",
            "\t\t/**\n\t\t * 若可能则返回实际级别值。\n\t\t *\n\t\t * @return the level value 级别值\n\t\t * @throws IllegalStateException if this is a {@link #isCustom() custom} level 自定义级别时\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Return if this is a custom level and cannot be represented by {@link LogLevel}.\n\t\t * @return if this is a custom level\n\t\t */",
            "\t\t/**\n\t\t * 返回是否为无法用 {@link LogLevel} 表示的自定义级别。\n\t\t *\n\t\t * @return if this is a custom level 是否为自定义级别\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Create a new {@link LevelConfiguration} instance of the given {@link LogLevel}.\n\t\t * @param logLevel the log level\n\t\t * @return a new {@link LevelConfiguration} instance\n\t\t */",
            "\t\t/**\n\t\t * 根据给定 {@link LogLevel} 创建新的 {@link LevelConfiguration} 实例。\n\t\t *\n\t\t * @param logLevel 日志级别\n\t\t * @return a new {@link LevelConfiguration} instance 新实例\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Create a new {@link LevelConfiguration} instance for a custom level name.\n\t\t * @param name the log level name\n\t\t * @return a new {@link LevelConfiguration} instance\n\t\t */",
            "\t\t/**\n\t\t * 为自定义级别名创建新的 {@link LevelConfiguration} 实例。\n\t\t *\n\t\t * @param name 日志级别名称\n\t\t * @return a new {@link LevelConfiguration} instance 新实例\n\t\t */",
        ),
    ],
    "LoggerConfigurationComparator.java": [
        (
            "/**\n * An implementation of {@link Comparator} for comparing {@link LoggerConfiguration}s.\n * Sorts the \"root\" logger as the first logger and then lexically by name after that.\n *\n * @author Ben Hale\n */",
            "/**\n * 比较 {@link LoggerConfiguration} 的 {@link Comparator} 实现。\n * 将 root Logger 排在首位，其余按名称字典序排序。\n *\n * @author Ben Hale\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link LoggerConfigurationComparator} instance.\n\t * @param rootLoggerName the name of the \"root\" logger\n\t */",
            "\t/**\n\t * 创建新的 {@link LoggerConfigurationComparator} 实例。\n\t *\n\t * @param rootLoggerName root Logger 的名称\n\t */",
        ),
    ],
    "LoggerGroup.java": [
        (
            "/**\n * A single logger group.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.2.0\n */",
            "/**\n * 单个 Logger 组。\n * 将多个 Logger 名称聚合为一组，便于统一配置日志级别。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 2.2.0\n */",
        ),
    ],
    "LoggerGroups.java": [
        (
            "/**\n * Logger groups configured through the Spring Environment.\n *\n * @author HaiTao Zhang\n * @author Phillip Webb\n * @since 2.2.0\n * @see LoggerGroup\n */",
            "/**\n * 通过 Spring Environment 配置的 Logger 组集合。\n * 支持按名称注册、批量添加及迭代访问各 {@link LoggerGroup}。\n *\n * @author HaiTao Zhang\n * @author Phillip Webb\n * @since 2.2.0\n * @see LoggerGroup\n */",
        ),
    ],
    "LoggingInitializationContext.java": [
        (
            "/**\n * Context passed to the {@link LoggingSystem} during initialization.\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
            "/**\n * 日志系统初始化期间传递给 {@link LoggingSystem} 的上下文。\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link LoggingInitializationContext} instance.\n\t * @param environment the Spring environment.\n\t */",
            "\t/**\n\t * 创建新的 {@link LoggingInitializationContext} 实例。\n\t *\n\t * @param environment Spring 环境\n\t */",
        ),
        (
            "\t/**\n\t * Return the Spring environment if available.\n\t * @return the {@link Environment} or {@code null}\n\t */",
            "\t/**\n\t * 若可用则返回 Spring 环境。\n\t *\n\t * @return the {@link Environment} or {@code null} 环境或 {@code null}\n\t */",
        ),
    ],
    "LoggingSystem.java": [
        (
            "/**\n * Common abstraction over logging systems.\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Andy Wilkinson\n * @author Ben Hale\n * @since 1.0.0\n */",
            "/**\n * 日志系统的通用抽象。\n * 定义初始化、级别设置、配置查询及工厂发现等核心能力。\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Andy Wilkinson\n * @author Ben Hale\n * @since 1.0.0\n */",
        ),
        (
            "\t/**\n\t * A System property that can be used to indicate the {@link LoggingSystem} to use.\n\t */",
            "\t/**\n\t * 用于指定要使用的 {@link LoggingSystem} 的系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * The value of the {@link #SYSTEM_PROPERTY} that can be used to indicate that no\n\t * {@link LoggingSystem} should be used.\n\t */",
            "\t/**\n\t * {@link #SYSTEM_PROPERTY} 的值，表示不使用任何 {@link LoggingSystem}。\n\t */",
        ),
        (
            "\t/**\n\t * The name used for the root logger. LoggingSystem implementations should ensure that\n\t * this is the name used to represent the root logger, regardless of the underlying\n\t * implementation.\n\t */",
            "\t/**\n\t * root Logger 使用的名称。\n\t * 各 {@link LoggingSystem} 实现应统一用此名称表示 root Logger。\n\t */",
        ),
        (
            "\t/**\n\t * The name of an {@link Environment} property used to indicate that a correlation ID\n\t * is expected to be logged at some point.\n\t * @since 3.2.0\n\t */",
            "\t/**\n\t * 表示期望在日志中输出关联 ID 的 {@link Environment} 属性名。\n\t *\n\t * @since 3.2.0\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@link LoggingSystemProperties} that should be applied.\n\t * @param environment the {@link ConfigurableEnvironment} used to obtain value\n\t * @return the {@link LoggingSystemProperties} to apply\n\t * @since 2.4.0\n\t */",
            "\t/**\n\t * 返回应应用的 {@link LoggingSystemProperties}。\n\t *\n\t * @param environment 用于获取值的 {@link ConfigurableEnvironment}\n\t * @return the {@link LoggingSystemProperties} to apply 要应用的属性集\n\t * @since 2.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Reset the logging system to be limit output. This method may be called before\n\t * {@link #initialize(LoggingInitializationContext, String, LogFile)} to reduce\n\t * logging noise until the system has been fully initialized.\n\t */",
            "\t/**\n\t * 重置日志系统以限制输出。\n\t * 可在 {@link #initialize(LoggingInitializationContext, String, LogFile)} 之前调用，\n\t * 在系统完全初始化前减少日志噪音。\n\t */",
        ),
        (
            "\t/**\n\t * Fully initialize the logging system.\n\t * @param initializationContext the logging initialization context\n\t * @param configLocation a log configuration location or {@code null} if default\n\t * initialization is required\n\t * @param logFile the log output file that should be written or {@code null} for\n\t * console only output\n\t */",
            "\t/**\n\t * 完全初始化日志系统。\n\t *\n\t * @param initializationContext 日志初始化上下文\n\t * @param configLocation 日志配置位置，默认初始化时为 {@code null}\n\t * @param logFile 要写入的日志文件，仅控制台输出时为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Clean up the logging system. The default implementation does nothing. Subclasses\n\t * should override this method to perform any logging system-specific cleanup.\n\t */",
            "\t/**\n\t * 清理日志系统。默认实现为空，子类应覆盖以执行特定清理。\n\t */",
        ),
        (
            "\t/**\n\t * Returns a {@link Runnable} that can handle shutdown of this logging system when the\n\t * JVM exits. The default implementation returns {@code null}, indicating that no\n\t * shutdown is required.\n\t * @return the shutdown handler, or {@code null}\n\t */",
            "\t/**\n\t * 返回 JVM 退出时关闭此日志系统的 {@link Runnable}。\n\t * 默认返回 {@code null}，表示无需关闭处理。\n\t *\n\t * @return the shutdown handler, or {@code null} 关闭处理器或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Returns a set of the {@link LogLevel LogLevels} that are actually supported by the\n\t * logging system.\n\t * @return the supported levels\n\t */",
            "\t/**\n\t * 返回日志系统实际支持的 {@link LogLevel} 集合。\n\t *\n\t * @return the supported levels 支持的级别\n\t */",
        ),
        (
            "\t/**\n\t * Sets the logging level for a given logger.\n\t * @param loggerName the name of the logger to set ({@code null} can be used for the\n\t * root logger).\n\t * @param level the log level ({@code null} can be used to remove any custom level for\n\t * the logger and use the default configuration instead)\n\t */",
            "\t/**\n\t * 设置指定 Logger 的日志级别。\n\t *\n\t * @param loggerName 要设置的 Logger 名称（{@code null} 表示 root Logger）\n\t * @param level 日志级别（{@code null} 移除自定义级别并恢复默认配置）\n\t */",
        ),
        (
            "\t/**\n\t * Returns a collection of the current configuration for all a {@link LoggingSystem}'s\n\t * loggers.\n\t * @return the current configurations\n\t * @since 1.5.0\n\t */",
            "\t/**\n\t * 返回 {@link LoggingSystem} 所有 Logger 的当前配置。\n\t *\n\t * @return the current configurations 当前配置集合\n\t * @since 1.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Returns the current configuration for a {@link LoggingSystem}'s logger.\n\t * @param loggerName the name of the logger\n\t * @return the current configuration\n\t * @since 1.5.0\n\t */",
            "\t/**\n\t * 返回指定 Logger 的当前配置。\n\t *\n\t * @param loggerName Logger 名称\n\t * @return the current configuration 当前配置\n\t * @since 1.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Detect and return the logging system in use. Supports Logback and Java Logging.\n\t * @param classLoader the classloader\n\t * @return the logging system\n\t */",
            "\t/**\n\t * 检测并返回正在使用的日志系统。\n\t * 支持 Logback 与 Java Logging 等实现。\n\t *\n\t * @param classLoader 类加载器\n\t * @return the logging system 日志系统\n\t */",
        ),
        (
            "\t/**\n\t * {@link LoggingSystem} that does nothing.\n\t */",
            "\t/**\n\t * 空操作的 {@link LoggingSystem}。\n\t */",
        ),
    ],
    "LoggingSystemFactory.java": [
        (
            "/**\n * Factory class used by {@link LoggingSystem#get(ClassLoader)} to find an actual\n * implementation.\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 供 {@link LoggingSystem#get(ClassLoader)} 查找实际实现的工厂类。\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "\t/**\n\t * Return a logging system implementation or {@code null} if no logging system is\n\t * available.\n\t * @param classLoader the class loader to use\n\t * @return a logging system\n\t */",
            "\t/**\n\t * 返回日志系统实现；无可用实现时返回 {@code null}。\n\t *\n\t * @param classLoader 要使用的类加载器\n\t * @return a logging system 日志系统\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link LoggingSystemFactory} backed by {@code spring.factories}.\n\t * @return a {@link LoggingSystemFactory} instance\n\t */",
            "\t/**\n\t * 返回由 {@code spring.factories} 支持的 {@link LoggingSystemFactory}。\n\t *\n\t * @return a {@link LoggingSystemFactory} instance 工厂实例\n\t */",
        ),
    ],
    "LoggingSystemProperties.java": [
        (
            "/**\n * Utility to set system properties that can later be used by log configuration files.\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Vedran Pavic\n * @author Robert Thornton\n * @author Eddú Meléndez\n * @author Jonatan Ivanov\n * @since 2.0.0\n * @see LoggingSystemProperty\n */",
            "/**\n * 设置日志配置文件可引用的系统属性的工具类。\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Vedran Pavic\n * @author Robert Thornton\n * @author Eddú Meléndez\n * @author Jonatan Ivanov\n * @since 2.0.0\n * @see LoggingSystemProperty\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link LoggingSystemProperties} instance.\n\t * @param environment the source environment\n\t */",
            "\t/**\n\t * 创建新的 {@link LoggingSystemProperties} 实例。\n\t *\n\t * @param environment 源环境\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link LoggingSystemProperties} instance.\n\t * @param environment the source environment\n\t * @param setter setter used to apply the property or {@code null} for system\n\t * properties\n\t * @since 2.4.2\n\t */",
            "\t/**\n\t * 创建新的 {@link LoggingSystemProperties} 实例。\n\t *\n\t * @param environment 源环境\n\t * @param setter 应用属性的 setter，{@code null} 时使用系统属性\n\t * @since 2.4.2\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link LoggingSystemProperties} instance.\n\t * @param environment the source environment\n\t * @param defaultValueResolver function used to resolve default values or {@code null}\n\t * @param setter setter used to apply the property or {@code null} for system\n\t * properties\n\t * @since 3.2.0\n\t */",
            "\t/**\n\t * 创建新的 {@link LoggingSystemProperties} 实例。\n\t *\n\t * @param environment 源环境\n\t * @param defaultValueResolver 解析默认值的函数，可为 {@code null}\n\t * @param setter 应用属性的 setter，{@code null} 时使用系统属性\n\t * @since 3.2.0\n\t */",
        ),
        (
            "\t/**\n\t * Returns the {@link Console} to use.\n\t * @return the {@link Console} to use\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 返回要使用的 {@link Console}。\n\t *\n\t * @return the {@link Console} to use 控制台\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Returns the default console charset.\n\t * @return the default console charset\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 返回默认控制台字符集。\n\t *\n\t * @return the default console charset 默认控制台字符集\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Returns the default file charset.\n\t * @return the default file charset\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 返回默认文件字符集。\n\t *\n\t * @return the default file charset 默认文件字符集\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Set a system property.\n\t * @param name the property name\n\t * @param value the value\n\t */",
            "\t/**\n\t * 设置系统属性。\n\t *\n\t * @param name 属性名\n\t * @param value 属性值\n\t */",
        ),
    ],
    "LoggingSystemProperty.java": [
        (
            "/**\n * Logging system properties that can later be used by log configuration files.\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see LoggingSystemProperties\n */",
            "/**\n * 日志配置文件可引用的日志系统属性枚举。\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see LoggingSystemProperties\n */",
        ),
        (
            "\t/**\n\t * Logging system property for the application name that should be logged.\n\t */",
            "\t/**\n\t * 应写入日志的应用名称系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the application group that should be logged.\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 应写入日志的应用组系统属性。\n\t *\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the process ID.\n\t */",
            "\t/**\n\t * 进程 ID 的日志系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the log file.\n\t */",
            "\t/**\n\t * 日志文件的日志系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the log path.\n\t */",
            "\t/**\n\t * 日志路径的日志系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the console log charset.\n\t */",
            "\t/**\n\t * 控制台日志字符集的系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the file log charset.\n\t */",
            "\t/**\n\t * 文件日志字符集的系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the console log.\n\t */",
            "\t/**\n\t * 控制台日志阈值的系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the file log.\n\t */",
            "\t/**\n\t * 文件日志阈值的系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the exception conversion word.\n\t */",
            "\t/**\n\t * 异常转换字的日志系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the console log pattern.\n\t */",
            "\t/**\n\t * 控制台日志模式的系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the file log pattern.\n\t */",
            "\t/**\n\t * 文件日志模式的系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the console structured logging format.\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 控制台结构化日志格式的系统属性。\n\t *\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the file structured logging format.\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 文件结构化日志格式的系统属性。\n\t *\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the log level pattern.\n\t */",
            "\t/**\n\t * 日志级别模式的系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the date-format pattern.\n\t */",
            "\t/**\n\t * 日期格式模式的日志系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the correlation pattern.\n\t */",
            "\t/**\n\t * 关联 ID 模式的日志系统属性。\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of environment variable that can be used to access this property.\n\t * @return the environment variable name\n\t */",
            "\t/**\n\t * 返回访问此属性的环境变量名。\n\t *\n\t * @return the environment variable name 环境变量名\n\t */",
        ),
        (
            "\t/**\n\t * Return the application property name that can be used to set this property.\n\t * @return the application property name\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 返回设置此属性的应用属性名。\n\t *\n\t * @return the application property name 应用属性名\n\t * @since 3.4.0\n\t */",
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
                "wave11a [0:20]",
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
