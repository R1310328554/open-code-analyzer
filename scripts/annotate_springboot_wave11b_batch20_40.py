#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-11b batch files [20:40] (logging/java/log4j2)."""
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
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][20:40]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "StackTracePrinter.java": [
        (
            "/**\n * Interface that can be used to print the stack trace of a {@link Throwable}.\n *\n * @author Phillip Webb\n * @since 3.5.0\n * @see StandardStackTracePrinter\n */",
            "/**\n * 可用于打印 {@link Throwable} 堆栈跟踪的接口。\n *\n * @author Phillip Webb\n * @since 3.5.0\n * @see StandardStackTracePrinter\n */",
        ),
        (
            "\t/**\n\t * Return a {@link String} containing the printed stack trace for a given\n\t * {@link Throwable}.\n\t * @param throwable the throwable that should have its stack trace printed\n\t * @return the stack trace string\n\t */",
            "\t/**\n\t * 返回给定 {@link Throwable} 的堆栈跟踪字符串。\n\t *\n\t * @param throwable 需要打印堆栈跟踪的 throwable\n\t * @return 堆栈跟踪字符串\n\t */",
        ),
        (
            "\t/**\n\t * Prints a stack trace for the given {@link Throwable}.\n\t * @param throwable the throwable that should have its stack trace printed\n\t * @param out the destination to write output\n\t * @throws IOException on IO error\n\t */",
            "\t/**\n\t * 打印给定 {@link Throwable} 的堆栈跟踪。\n\t *\n\t * @param throwable 需要打印堆栈跟踪的 throwable\n\t * @param out 输出目标\n\t * @throws IOException IO 错误时抛出\n\t */",
        ),
    ],
    "JavaLoggingSystem.java": [
        (
            "/**\n * {@link LoggingSystem} for {@link Logger java.util.logging}.\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Andy Wilkinson\n * @author Ben Hale\n * @since 1.0.0\n */",
            "/**\n * 针对 {@link Logger java.util.logging} 的 {@link LoggingSystem} 实现。\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Andy Wilkinson\n * @author Ben Hale\n * @since 1.0.0\n */",
        ),
        (
            "\t/**\n\t * {@link LoggingSystemFactory} that returns {@link JavaLoggingSystem} if possible.\n\t */",
            "\t/**\n\t * 在可用时返回 {@link JavaLoggingSystem} 的 {@link LoggingSystemFactory}。\n\t */",
        ),
    ],
    "JavaLoggingSystemRuntimeHints.java": [
        (
            "/**\n * {@link RuntimeHintsRegistrar} implementation for {@link JavaLoggingSystem}.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * {@link JavaLoggingSystem} 的 {@link RuntimeHintsRegistrar} 实现。\n * 在 AOT 处理阶段注册 Java Logging 默认配置文件资源提示，\n * 包括 {@code logging.properties} 与 {@code logging-file.properties}。\n *\n * @author Moritz Halbritter\n */",
        ),
    ],
    "SimpleFormatter.java": [
        (
            "/**\n * Simple 'Java Logging' {@link Formatter}.\n *\n * @author Phillip Webb\n * @since 1.0.0\n */",
            "/**\n * 简单的 Java Logging {@link Formatter} 实现。\n * 使用环境变量或系统属性 {@code LOG_FORMAT} 与 {@code PID} 格式化 {@link LogRecord}，\n * 默认格式与 Spring Boot 控制台日志风格一致。\n *\n * @author Phillip Webb\n * @since 1.0.0\n */",
        ),
    ],
    "ColorConverter.java": [
        (
            "/**\n * Log4j2 {@link LogEventPatternConverter} to color output using the {@link AnsiOutput}\n * class. One or more styling options can be provided to the converter, or if not\n * specified color styling will be picked based on the logging level. Supported options\n * include foreground colors (e.g. {@code red}, {@code bright_blue}), background colors\n * (e.g. {@code bg_red}, {@code bg_bright_green}), and text styles (e.g. {@code bold},\n * {@code underline}, {@code reverse}).\n *\n * @author Vladimir Tsanev\n * @since 1.3.0\n */",
            "/**\n * 使用 {@link AnsiOutput} 为输出着色的 Log4j2 {@link LogEventPatternConverter}。\n * 可为转换器提供一个或多个样式选项；未指定时按日志级别选择颜色样式。\n * 支持的选项包括前景色（如 {@code red}、{@code bright_blue}）、\n * 背景色（如 {@code bg_red}、{@code bg_bright_green}）以及文本样式\n * （如 {@code bold}、{@code underline}、{@code reverse}）。\n *\n * @author Vladimir Tsanev\n * @since 1.3.0\n */",
        ),
        (
            "\t/**\n\t * Creates a new instance of the class. Required by Log4J2.\n\t * @param config the configuration\n\t * @param options the options\n\t * @return a new instance, or {@code null} if the options are invalid\n\t */",
            "\t/**\n\t * 创建类的新实例。Log4J2 要求提供此方法。\n\t *\n\t * @param config 配置\n\t * @param options 选项\n\t * @return 新实例；若选项无效则返回 {@code null}\n\t */",
        ),
    ],
    "CorrelationIdConverter.java": [
        (
            "/**\n * Log4j2 {@link LogEventPatternConverter} to convert a {@link CorrelationIdFormatter}\n * pattern into formatted output using data from the {@link LogEvent#getContextData()\n * MDC}.\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see MdcPatternConverter\n */",
            "/**\n * 使用 {@link LogEvent#getContextData() MDC} 数据将 {@link CorrelationIdFormatter}\n * 模式转换为格式化输出的 Log4j2 {@link LogEventPatternConverter}。\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see MdcPatternConverter\n */",
        ),
        (
            "\t/**\n\t * Factory method to create a new {@link CorrelationIdConverter}.\n\t * @param options options, may be null or first element contains name of property to\n\t * format.\n\t * @return instance of PropertiesPatternConverter.\n\t */",
            "\t/**\n\t * 创建新 {@link CorrelationIdConverter} 的工厂方法。\n\t *\n\t * @param options 选项，可为 null，或首元素为要格式化的属性名\n\t * @return PropertiesPatternConverter 实例\n\t */",
        ),
    ],
    "ElasticCommonSchemaStructuredLogFormatter.java": [
        (
            "/**\n * Log4j2 {@link StructuredLogFormatter} for\n * {@link CommonStructuredLogFormat#ELASTIC_COMMON_SCHEMA}.\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
            "/**\n * 用于 {@link CommonStructuredLogFormat#ELASTIC_COMMON_SCHEMA} 的\n * Log4j2 {@link StructuredLogFormatter} 实现。\n * 将 {@link LogEvent} 格式化为符合 Elastic Common Schema（ECS）8.11 的 JSON 日志。\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
        ),
    ],
    "EnclosedInSquareBracketsConverter.java": [
        (
            "/**\n * Log4j2 {@link LogEventPatternConverter} used to help format optional values that should\n * be shown enclosed in square brackets.\n *\n * @author Phillip Webb\n * @since 3.4.0\n */",
            "/**\n * 用于格式化应显示在方括号内的可选值的 Log4j2 {@link LogEventPatternConverter}。\n *\n * @author Phillip Webb\n * @since 3.4.0\n */",
        ),
        (
            "\t/**\n\t * Creates a new instance of the class. Required by Log4J2.\n\t * @param config the configuration\n\t * @param options the options\n\t * @return a new instance, or {@code null} if the options are invalid\n\t */",
            "\t/**\n\t * 创建类的新实例。Log4J2 要求提供此方法。\n\t *\n\t * @param config 配置\n\t * @param options 选项\n\t * @return 新实例；若选项无效则返回 {@code null}\n\t */",
        ),
    ],
    "ExtendedWhitespaceThrowablePatternConverter.java": [
        (
            "/**\n * {@link ThrowablePatternConverter} that adds some additional whitespace around the stack\n * trace.\n *\n * @author Vladimir Tsanev\n * @author Phillip Webb\n * @since 1.3.0\n */",
            "/**\n * 在堆栈跟踪周围添加额外空白字符的 {@link ThrowablePatternConverter}。\n *\n * @author Vladimir Tsanev\n * @author Phillip Webb\n * @since 1.3.0\n */",
        ),
        (
            "\t/**\n\t * Creates a new instance of the class. Required by Log4J2.\n\t * @param configuration current configuration\n\t * @param options pattern options, may be null. If first element is \"short\", only the\n\t * first line of the throwable will be formatted.\n\t * @return a new {@code WhitespaceThrowablePatternConverter}\n\t */",
            "\t/**\n\t * 创建类的新实例。Log4J2 要求提供此方法。\n\t *\n\t * @param configuration 当前配置\n\t * @param options 模式选项，可为 null；若首元素为 \"short\" 则仅格式化 throwable 首行\n\t * @return 新的 {@code WhitespaceThrowablePatternConverter}\n\t */",
        ),
    ],
    "Extractor.java": [
        (
            "/**\n * Functions to extract items from {@link LoggingEvent}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 从 {@link LoggingEvent} 提取条目的工具函数。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "GraylogExtendedLogFormatStructuredLogFormatter.java": [
        (
            "/**\n * Log4j2 {@link StructuredLogFormatter} for\n * {@link CommonStructuredLogFormat#GRAYLOG_EXTENDED_LOG_FORMAT}. Supports GELF version\n * 1.1.\n *\n * @author Samuel Lissner\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
            "/**\n * 用于 {@link CommonStructuredLogFormat#GRAYLOG_EXTENDED_LOG_FORMAT} 的\n * Log4j2 {@link StructuredLogFormatter} 实现，支持 GELF 1.1 版本。\n *\n * @author Samuel Lissner\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
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
            "\t/**\n\t * GELF requires \"seconds since UNIX epoch with optional <b>decimal places for\n\t * milliseconds</b>\". To comply with this requirement, we format a POSIX timestamp\n\t * with millisecond precision as e.g. \"1725459730385\" -> \"1725459730.385\"\n\t * @param timeStamp the timestamp of the log message.\n\t * @return the timestamp formatted as string with millisecond precision\n\t */",
            "\t/**\n\t * GELF 要求 \"自 UNIX epoch 起的秒数，可选<b>毫秒小数位</b>\"。\n\t * 为满足该要求，将毫秒精度的 POSIX 时间戳格式化为字符串，\n\t * 例如 \"1725459730385\" -> \"1725459730.385\"。\n\t *\n\t * @param timeStamp 日志消息的时间戳\n\t * @return 毫秒精度的格式化时间戳字符串\n\t */",
        ),
        (
            "\t/**\n\t * Converts the log4j2 event level to the Syslog event level code.\n\t * @param event the log event\n\t * @return an integer representing the syslog log level code\n\t * @see Severity class from Log4j2 which contains the conversion logic\n\t */",
            "\t/**\n\t * 将 log4j2 事件级别转换为 Syslog 事件级别代码。\n\t *\n\t * @param event 日志事件\n\t * @return 表示 syslog 日志级别代码的整数\n\t * @see Log4j2 的 Severity 类，其中包含转换逻辑\n\t */",
        ),
    ],
    "Log4J2RuntimeHints.java": [
        (
            "/**\n * {@link RuntimeHintsRegistrar} implementation for {@link Log4J2LoggingSystem}.\n *\n * @author Piotr P. Karwasz\n * @author Stephane Nicoll\n */",
            "/**\n * {@link Log4J2LoggingSystem} 的 {@link RuntimeHintsRegistrar} 实现。\n * 在 AOT 处理阶段注册 Log4j2 配置资源与反射类型提示，\n * 包括默认 {@code log4j2.xml} 与 {@code log4j2-file.xml} 配置文件。\n *\n * @author Piotr P. Karwasz\n * @author Stephane Nicoll\n */",
        ),
    ],
    "Log4j2LoggingSystemProperties.java": [
        (
            "/**\n * {@link LoggingSystemProperties} for Log4j2.\n *\n * @author HoJoo Moon\n * @since 4.1.0\n * @see RollingPolicySystemProperty\n */",
            "/**\n * Log4j2 的 {@link LoggingSystemProperties} 实现。\n *\n * @author HoJoo Moon\n * @since 4.1.0\n * @see RollingPolicySystemProperty\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link Log4j2LoggingSystemProperties} instance.\n\t * @param environment the source environment\n\t * @param defaultValueResolver function used to resolve default values or {@code null}\n\t * @param setter setter used to apply the property or {@code null} for system\n\t * properties\n\t */",
            "\t/**\n\t * 创建新的 {@link Log4j2LoggingSystemProperties} 实例。\n\t *\n\t * @param environment 源环境\n\t * @param defaultValueResolver 用于解析默认值的函数，或 {@code null}\n\t * @param setter 用于应用属性的 setter，或 {@code null} 表示使用 system properties\n\t */",
        ),
    ],
    "LogstashStructuredLogFormatter.java": [
        (
            "/**\n * Log4j2 {@link StructuredLogFormatter} for {@link CommonStructuredLogFormat#LOGSTASH}.\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
            "/**\n * 用于 {@link CommonStructuredLogFormat#LOGSTASH} 的\n * Log4j2 {@link StructuredLogFormatter} 实现。\n * 将 {@link LogEvent} 格式化为 Logstash 兼容的 JSON 结构化日志。\n *\n * @author Moritz Halbritter\n * @author Phillip Webb\n */",
        ),
    ],
    "RollingPolicyStrategy.java": [
        (
            "/**\n * Available rolling policy strategies.\n *\n * @author Stephane Nicoll\n * @since 4.1.0\n */",
            "/**\n * 可用的滚动策略（rolling policy strategy）。\n *\n * @author Stephane Nicoll\n * @since 4.1.0\n */",
        ),
        (
            "\t/**\n\t * Roll a file over based on its size.\n\t */",
            "\t/**\n\t * 基于文件大小滚动。\n\t */",
        ),
        (
            "\t/**\n\t * Roll a file over based on time.\n\t */",
            "\t/**\n\t * 基于时间滚动。\n\t */",
        ),
        (
            "\t/**\n\t * Roll a file over based on its size and time.\n\t */",
            "\t/**\n\t * 基于文件大小与时间滚动。\n\t */",
        ),
        (
            "\t/**\n\t * Roll a file over based on a cron schedule.\n\t */",
            "\t/**\n\t * 基于 cron 调度滚动。\n\t */",
        ),
    ],
    "RollingPolicySystemProperty.java": [
        (
            "/**\n * Log4j2 rolling policy system properties that can later be used by log configuration\n * files.\n *\n * @author HoJoo Moon\n * @author Stephane Nicoll\n * @since 4.1.0\n * @see Log4j2LoggingSystemProperties\n */",
            "/**\n * 可供日志配置文件后续使用的 Log4j2 滚动策略 system properties。\n *\n * @author HoJoo Moon\n * @author Stephane Nicoll\n * @since 4.1.0\n * @see Log4j2LoggingSystemProperties\n */",
        ),
        (
            "\t/**\n\t * Logging system property for the rolled-over log file name pattern.\n\t */",
            "\t/**\n\t * 滚动后日志文件名模式的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the file log max size.\n\t */",
            "\t/**\n\t * 文件日志最大大小的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the file log max history.\n\t */",
            "\t/**\n\t * 文件日志最大保留历史的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the {@linkplain RollingPolicyStrategy rolling policy\n\t * strategy}.\n\t */",
            "\t/**\n\t * {@linkplain RollingPolicyStrategy 滚动策略} 的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the rolling policy time interval.\n\t */",
            "\t/**\n\t * 滚动策略时间间隔的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the rolling policy time modulate flag.\n\t */",
            "\t/**\n\t * 滚动策略 time modulate 标志的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Logging system property for the cron based schedule.\n\t */",
            "\t/**\n\t * 基于 cron 调度的 logging system property。\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of environment variable that can be used to access this property.\n\t * @return the environment variable name\n\t */",
            "\t/**\n\t * 返回可用于访问此属性的环境变量名。\n\t *\n\t * @return 环境变量名\n\t */",
        ),
    ],
    "SpringBootConfigurationFactory.java": [
        (
            "/**\n * Spring Boot {@link ConfigurationFactory} that customizes Log4J2's default configuration\n * to:\n *\n * <ol>\n * <li>Prevent logger warnings from being printed when the application first starts.\n * </ol>\n *\n * This factory is ordered last and is triggered by a {@code log4j2.springboot} classpath\n * resource (which is bundled in this jar). If the {@link Log4J2LoggingSystem} is active,\n * a {@link DefaultConfiguration} is returned with the expectation that the system will\n * later re-initialize Log4J2 with the correct configuration file.\n *\n * @author Phillip Webb\n * @since 1.5.0\n */",
            "/**\n * 定制 Log4J2 默认配置的 Spring Boot {@link ConfigurationFactory}，用于：\n *\n * <ol>\n * <li>防止应用首次启动时打印 logger 警告。\n * </ol>\n *\n * 此工厂排序最后，由 classpath 资源 {@code log4j2.springboot}（打包在本 jar 中）触发。\n * 若 {@link Log4J2LoggingSystem} 处于活动状态，则返回 {@link DefaultConfiguration}，\n * 期望系统稍后使用正确的配置文件重新初始化 Log4J2。\n *\n * @author Phillip Webb\n * @since 1.5.0\n */",
        ),
    ],
    "SpringBootPropertySource.java": [
        (
            "/**\n * Spring Boot {@link PropertySource} that disables Log4j2's shutdown hook.\n *\n * @author Andy Wilkinson\n * @since 2.5.2\n */",
            "/**\n * 禁用 Log4j2 shutdown hook 的 Spring Boot {@link PropertySource}。\n * 将 {@code log4j.shutdownHookEnabled} 设为 {@code false}，\n * 由 Spring Boot 生命周期管理 Log4j2 关闭。\n *\n * @author Andy Wilkinson\n * @since 2.5.2\n */",
        ),
    ],
    "SpringBootTriggeringPolicy.java": [
        (
            "/**\n * Factory for creating a standard Log4j2 {@link TriggeringPolicy} based on configuration\n * attributes. The supported strategies are {@code size}, {@code time},\n * {@code size-and-time}, and {@code cron}.\n *\n * @author HoJoo Moon\n * @author Stephane Nicoll\n * @since 4.1.0\n */",
            "/**\n * 基于配置属性创建标准 Log4j2 {@link TriggeringPolicy} 的工厂。\n * 支持的策略为 {@code size}、{@code time}、{@code size-and-time} 与 {@code cron}。\n *\n * @author HoJoo Moon\n * @author Stephane Nicoll\n * @since 4.1.0\n */",
        ),
        (
            "\t/**\n\t * Builder for creating a {@link TriggeringPolicy}.\n\t */",
            "\t/**\n\t * 用于创建 {@link TriggeringPolicy} 的 Builder。\n\t */",
        ),
    ],
    "SpringEnvironmentLookup.java": [
        (
            "/**\n * Lookup for Spring properties.\n *\n * @author Ralph Goers\n * @author Phillip Webb\n * @author Dmytro Nosan\n */",
            "/**\n * 用于在 Log4j2 配置中查找 Spring 环境属性的 {@link StrLookup} 实现。\n * 通过 {@link LoggerContext} 获取 {@link Environment}，\n * 配置文件名需以 {@code -spring} 结尾（如 {@code log4j2-spring.xml}）。\n *\n * @author Ralph Goers\n * @author Phillip Webb\n * @author Dmytro Nosan\n */",
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
                "wave11b logging/java/log4j2 [20:40]",
                *BATCH_FILES,
            ],
            check=True,
        )
        batch_path = QUEUE / "batch.json"
        batch = json.loads(batch_path.read_text(encoding="utf-8"))
        done_path = QUEUE / "done.txt"
        pending_path = QUEUE / "pending.txt"
        batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
        batch["remaining_pending"] = len(
            [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
        )
        batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"Marked {ok} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
