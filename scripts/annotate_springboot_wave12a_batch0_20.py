#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-12a slice [0:20] (log4j2 + logback logging)."""
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
BATCH_FILES = Path("/tmp/springboot_w12a.txt").read_text(encoding="utf-8").strip().splitlines()

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SpringEnvironmentPropertySource.java": [
        (
            "/**\n * Returns properties from Spring.\n *\n * @author Ralph Goers\n */",
            "/**\n * 从 Spring 环境返回属性的 {@link PropertySource} 实现。\n * 供 Log4j2 在解析配置时读取 Spring {@link Environment} 中的属性值。\n *\n * @author Ralph Goers\n */",
        ),
        (
            "\t/**\n\t * System properties take precedence followed by properties in Log4j properties files.\n\t */",
            "\t/**\n\t * 优先级：系统属性优先，其次为 Log4j 属性文件中的属性。\n\t */",
        ),
    ],
    "SpringProfileArbiter.java": [
        (
            "/**\n * An Arbiter that uses the active Spring profile to determine if configuration should be\n * included.\n *\n * @author Ralph Goers\n */",
            "/**\n * 根据活动 Spring Profile 决定是否包含 Log4j2 配置的 {@link Arbiter}。\n * 当 {@link Environment} 接受指定 {@link Profiles} 时条件成立。\n *\n * @author Ralph Goers\n */",
        ),
        (
            "\t/**\n\t * Standard Builder to create the Arbiter.\n\t */",
            "\t/**\n\t * 创建 {@link SpringProfileArbiter} 的标准 Builder。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Sets the profile name or expression.\n\t\t * @param name the profile name or expression\n\t\t * @return this\n\t\t * @see Profiles#of(String...)\n\t\t */",
            "\t\t/**\n\t\t * 设置 Profile 名称或表达式。\n\t\t *\n\t\t * @param name the profile name or expression Profile 名称或表达式\n\t\t * @return this 当前 Builder\n\t\t * @see Profiles#of(String...)\n\t\t */",
        ),
    ],
    "StructuredLogLayout.java": [
        (
            "/**\n * {@link Layout Log4j2 Layout} for structured logging.\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n * @see StructuredLogFormatter\n */",
            "/**\n * 用于结构化日志的 Log4j2 {@link Layout}。\n * 通过 {@link StructuredLogFormatterFactory} 按格式名称创建 ECS、GELF 或 Logstash 格式化器。\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n * @see StructuredLogFormatter\n */",
        ),
    ],
    "StructuredMessage.java": [
        (
            "/**\n * Helper used to adapt {@link Message} for structured writing.\n *\n * @author Phillip Webb\n */",
            "/**\n * 将 {@link Message} 适配为结构化写入格式的辅助类。\n * 若消息支持 JSON 格式则返回 {@link WritableJson}，否则返回格式化字符串。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "WhitespaceThrowablePatternConverter.java": [
        (
            "/**\n * {@link ThrowablePatternConverter} that adds some additional whitespace around the stack\n * trace.\n *\n * @author Vladimir Tsanev\n * @since 1.3.0\n */",
            "/**\n * 在堆栈跟踪周围添加额外空白字符的 {@link ThrowablePatternConverter}。\n * 通过嵌套 {@code %ex} 模式实现，支持 {@code wEx}、{@code wThrowable}、{@code wException} 键。\n *\n * @author Vladimir Tsanev\n * @since 1.3.0\n */",
        ),
    ],
    "ColorConverter.java": [
        (
            "/**\n * Logback {@link CompositeConverter} to color output using the {@link AnsiOutput} class.\n * One or more styling options can be provided to the converter, or if not specified color\n * will be picked based on the logging level. Supported options include foreground colors\n * (e.g. {@code red}, {@code bright_blue}), background colors (e.g. {@code bg_red},\n * {@code bg_bright_green}), and text styles (e.g. {@code bold}, {@code underline},\n * {@code reverse}).\n *\n * @author Phillip Webb\n * @since 1.0.0\n */",
            "/**\n * 使用 {@link AnsiOutput} 为输出着色的 Logback {@link CompositeConverter}。\n * 可为转换器提供一个或多个样式选项；未指定时按日志级别选择颜色样式。\n * 支持的选项包括前景色（如 {@code red}、{@code bright_blue}）、\n * 背景色（如 {@code bg_red}、{@code bg_bright_green}）以及文本样式\n * （如 {@code bold}、{@code underline}、{@code reverse}）。\n *\n * @author Phillip Webb\n * @since 1.0.0\n */",
        ),
    ],
    "CorrelationIdConverter.java": [
        (
            "/**\n * Logback {@link DynamicConverter} to convert a {@link CorrelationIdFormatter} pattern\n * into formatted output using data from the {@link ILoggingEvent#getMDCPropertyMap() MDC}\n * and {@link Environment}.\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see MDCConverter\n */",
            "/**\n * 使用 {@link ILoggingEvent#getMDCPropertyMap() MDC} 与 {@link Environment} 数据\n * 将 {@link CorrelationIdFormatter} 模式转换为格式化输出的 Logback {@link DynamicConverter}。\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see MDCConverter\n */",
        ),
    ],
    "DebugLogbackConfigurator.java": [
        (
            "/**\n * Custom {@link LogbackConfigurator} used to add {@link Status Statuses} when Logback\n * debugging is enabled.\n *\n * @author Andy Wilkinson\n */",
            "/**\n * 在启用 Logback 调试时向 {@link StatusManager} 添加 {@link Status} 的\n * 自定义 {@link LogbackConfigurator}。\n * 记录转换规则、Appender、Logger 及生命周期组件的配置信息。\n *\n * @author Andy Wilkinson\n */",
        ),
    ],
    "DefaultLogbackConfiguration.java": [
        (
            "/**\n * Default logback configuration used by Spring Boot. Uses {@link LogbackConfigurator} to\n * improve startup time. See also the {@code base.xml}, {@code defaults.xml},\n * {@code console-appender.xml} and {@code file-appender.xml} files provided for classic\n * {@code logback.xml} use.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Vedran Pavic\n * @author Robert Thornton\n * @author Scott Frederick\n * @author Jonatan Ivanov\n * @author Moritz Halbritter\n */",
            "/**\n * Spring Boot 使用的默认 Logback 配置。\n * 通过 {@link LogbackConfigurator} 以编程方式配置以提升启动速度。\n * 另见经典 {@code logback.xml} 使用的 {@code base.xml}、{@code defaults.xml}、\n * {@code console-appender.xml} 与 {@code file-appender.xml} 文件。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Vedran Pavic\n * @author Robert Thornton\n * @author Scott Frederick\n * @author Jonatan Ivanov\n * @author Moritz Halbritter\n */",
        ),
    ],
    "ElasticCommonSchemaStructuredLogFormatter.java": [
        (
            "/**\n * Logback {@link StructuredLogFormatter} for\n * {@link CommonStructuredLogFormat#ELASTIC_COMMON_SCHEMA}.\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
            "/**\n * 用于 {@link CommonStructuredLogFormat#ELASTIC_COMMON_SCHEMA} 的\n * Logback {@link StructuredLogFormatter} 实现。\n * 将 {@link ILoggingEvent} 格式化为符合 Elastic Common Schema（ECS）8.11 的 JSON 日志。\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
        ),
    ],
    "EnclosedInSquareBracketsConverter.java": [
        (
            "/**\n * Logback {@link CompositeConverter} used to help format optional values that should be\n * shown enclosed in square brackets.\n *\n * @author Phillip Webb\n * @since 3.4.0\n */",
            "/**\n * 用于格式化应显示在方括号内的可选值的 Logback {@link CompositeConverter}。\n * 若输入为空则尝试从首个选项对应的 Logger 上下文或系统属性解析。\n *\n * @author Phillip Webb\n * @since 3.4.0\n */",
        ),
    ],
    "ExtendedWhitespaceThrowableProxyConverter.java": [
        (
            "/**\n * {@link ExtendedThrowableProxyConverter} that adds some additional whitespace around the\n * stack trace.\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
            "/**\n * 在堆栈跟踪前后添加换行空白的 {@link ExtendedThrowableProxyConverter}。\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
        ),
    ],
    "Extractor.java": [
        (
            "/**\n * Functions to extract items from {@link ILoggingEvent}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 从 {@link ILoggingEvent} 提取消息与堆栈跟踪等条目的工具类。\n * 优先使用 {@link StackTracePrinter}，否则回退到 {@link ThrowableProxyConverter}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "GraylogExtendedLogFormatStructuredLogFormatter.java": [
        (
            "/**\n * Logback {@link StructuredLogFormatter} for\n * {@link CommonStructuredLogFormat#GRAYLOG_EXTENDED_LOG_FORMAT}. Supports GELF version\n * 1.1.\n *\n * @author Samuel Lissner\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
            "/**\n * 用于 {@link CommonStructuredLogFormat#GRAYLOG_EXTENDED_LOG_FORMAT} 的\n * Logback {@link StructuredLogFormatter} 实现，支持 GELF 1.1 版本。\n *\n * @author Samuel Lissner\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
        ),
        (
            "\t/**\n\t * Allowed characters in field names are any word character (letter, number,\n\t * underscore), dashes and dots.\n\t */",
            "\t/**\n\t * 字段名允许的字符为任意 word 字符（字母、数字、下划线）、连字符与点号。\n\t */",
        ),
        (
            "\t/**\n\t * Libraries SHOULD not allow to send id as additional field (\"_id\"). Graylog server\n\t * nodes omit this field automatically.\n\t */",
            "\t/**\n\t * 库不应允许将 id 作为附加字段（\"_id\"）发送；Graylog 服务端会自动省略该字段。\n\t */",
        ),
        (
            "\t/**\n\t * GELF requires \"seconds since UNIX epoch with optional <b>decimal places for\n\t * milliseconds</b>\". To comply with this requirement, we format a POSIX timestamp\n\t * with millisecond precision as e.g. \"1725459730385\" -> \"1725459730.385\"\n\t * @param timeStamp the timestamp of the log message\n\t * @return the timestamp formatted as string with millisecond precision\n\t */",
            "\t/**\n\t * GELF 要求 \"自 UNIX epoch 起的秒数，可选<b>毫秒小数位</b>\"。\n\t * 为满足该要求，将毫秒精度的 POSIX 时间戳格式化为字符串，\n\t * 例如 \"1725459730385\" -> \"1725459730.385\"。\n\t *\n\t * @param timeStamp the timestamp of the log message 日志消息的时间戳\n\t * @return the timestamp formatted as string with millisecond precision 毫秒精度的格式化时间戳字符串\n\t */",
        ),
    ],
    "LogbackConfigurator.java": [
        (
            "/**\n * Allows programmatic configuration of logback which is usually faster than parsing XML.\n *\n * @author Phillip Webb\n */",
            "/**\n * 允许以编程方式配置 Logback，通常比解析 XML 更快。\n * 提供转换规则、Appender、Logger 及 root Logger 的配置便捷方法。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "LogbackLoggingSystemProperties.java": [
        (
            "/**\n * {@link LoggingSystemProperties} for Logback.\n *\n * @author Phillip Webb\n * @since 2.4.0\n * @see RollingPolicySystemProperty\n */",
            "/**\n * Logback 的 {@link LoggingSystemProperties} 实现。\n * 除通用日志属性外，还应用 JBoss Logging 与滚动策略相关系统属性。\n *\n * @author Phillip Webb\n * @since 2.4.0\n * @see RollingPolicySystemProperty\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link LogbackLoggingSystemProperties} instance.\n\t * @param environment the source environment\n\t * @param setter setter used to apply the property\n\t * @since 2.4.3\n\t */",
            "\t/**\n\t * 创建新的 {@link LogbackLoggingSystemProperties} 实例。\n\t *\n\t * @param environment the source environment 源环境\n\t * @param setter setter used to apply the property 应用属性的 setter\n\t * @since 2.4.3\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link LoggingSystemProperties} instance.\n\t * @param environment the source environment\n\t * @param defaultValueResolver function used to resolve default values or {@code null}\n\t * @param setter setter used to apply the property or {@code null} for system\n\t * properties\n\t * @since 3.2.0\n\t */",
            "\t/**\n\t * 创建新的 {@link LogbackLoggingSystemProperties} 实例。\n\t *\n\t * @param environment the source environment 源环境\n\t * @param defaultValueResolver function used to resolve default values or {@code null} 解析默认值的函数\n\t * @param setter setter used to apply the property or {@code null} for system\n\t * properties 应用属性的 setter，{@code null} 时使用系统属性\n\t * @since 3.2.0\n\t */",
        ),
    ],
    "LogbackRuntimeHints.java": [
        (
            "/**\n * {@link RuntimeHintsRegistrar} for Logback.\n *\n * @author Andy Wilkinson\n */",
            "/**\n * Logback 的 {@link RuntimeHintsRegistrar} 实现。\n * 在 AOT 处理阶段注册 Logback 内置与 Spring Boot 自定义转换器的反射提示。\n *\n * @author Andy Wilkinson\n */",
        ),
    ],
    "LogstashStructuredLogFormatter.java": [
        (
            "/**\n * Logback {@link StructuredLogFormatter} for {@link CommonStructuredLogFormat#LOGSTASH}.\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
            "/**\n * 用于 {@link CommonStructuredLogFormat#LOGSTASH} 的\n * Logback {@link StructuredLogFormatter} 实现。\n * 将 {@link ILoggingEvent} 格式化为 Logstash 兼容的 JSON 结构化日志。\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
        ),
    ],
    "RollingPolicySystemProperty.java": [
        (
            "/**\n * Logback rolling policy system properties that can later be used by log configuration\n * files.\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see LogbackLoggingSystemProperties\n */",
            "/**\n * 可供日志配置文件后续使用的 Logback 滚动策略系统属性枚举。\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see LogbackLoggingSystemProperties\n */",
        ),
        (
            "\t/**\n\t * Logging system property for the rolled-over log file name pattern.\n\t */",
            "\t/**\n\t * 滚动后日志文件名模式的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the clean history on start flag.\n\t */",
            "\t/**\n\t * 启动时清理历史日志标志的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the file log max size.\n\t */",
            "\t/**\n\t * 文件日志最大大小的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the file total size cap.\n\t */",
            "\t/**\n\t * 文件日志总大小上限的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the file log max history.\n\t */",
            "\t/**\n\t * 文件日志最大保留历史的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of environment variable that can be used to access this property.\n\t * @return the environment variable name\n\t */",
            "\t/**\n\t * 返回可用于访问此属性的环境变量名。\n\t *\n\t * @return the environment variable name 环境变量名\n\t */",
        ),
    ],
    "RootLogLevelConfigurator.java": [
        (
            "/**\n * Logback {@link Configurator}, registered through {@code META-INF/services}, that sets\n * the root log level to {@link Level#INFO}.\n *\n * @author Andy Wilkinson\n * @since 3.1.0\n */",
            "/**\n * 通过 {@code META-INF/services} 注册的 Logback {@link Configurator}，\n * 将 root Logger 级别设为 {@link Level#INFO}。\n *\n * @author Andy Wilkinson\n * @since 3.1.0\n */",
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
                "wave12a [0:20]",
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
