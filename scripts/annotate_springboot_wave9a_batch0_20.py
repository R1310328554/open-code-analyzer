#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-9a slice [0:20] (convert.*)."""
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
    "CollectionToDelimitedStringConverter.java": [
        (
            "/**\n * Converts a Collection to a delimited String.\n *\n * @author Phillip Webb\n */",
            "/**\n * 将 Collection 转换为带分隔符的 String。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "DataSizeUnit.java": [
        (
            "/**\n * Annotation that can be used to change the default unit used when converting a\n * {@link DataSize}.\n *\n * @author Stephane Nicoll\n * @since 2.1.0\n */",
            "/**\n * 可用于更改转换 {@link DataSize} 时所用默认单位的注解。\n *\n * @author Stephane Nicoll\n * @since 2.1.0\n */",
        ),
        (
            "/**\n\t * The {@link DataUnit} to use if one is not specified.\n\t * @return the data unit\n\t */",
            "/**\n\t * 未指定单位时使用的 {@link DataUnit}。\n\t *\n\t * @return the data unit 数据单位\n\t */",
        ),
    ],
    "DelimitedStringToArrayConverter.java": [
        (
            "/**\n * Converts a {@link Delimiter delimited} String to an Array.\n *\n * @author Phillip Webb\n */",
            "/**\n * 将 {@link Delimiter 带分隔符} 的 String 转换为数组。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "DelimitedStringToCollectionConverter.java": [
        (
            "/**\n * Converts a {@link Delimiter delimited} String to a Collection.\n *\n * @author Phillip Webb\n */",
            "/**\n * 将 {@link Delimiter 带分隔符} 的 String 转换为 Collection。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "Delimiter.java": [
        (
            "/**\n * Declares a field or method parameter should be converted to collection using the\n * specified delimiter.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 声明字段或方法参数应使用指定分隔符转换为集合。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * A delimiter value used to indicate that no delimiter is required and the result\n\t * should be a single element containing the entire string.\n\t */",
            "/**\n\t * 表示不需要分隔符、整个字符串应作为单个元素的 delimiter 值。\n\t */",
        ),
        (
            "/**\n\t * The delimiter to use or {@code NONE} if the entire contents should be treated as a\n\t * single element.\n\t * @return the delimiter\n\t */",
            "/**\n\t * 要使用的分隔符；若整个内容应视为单个元素则使用 {@code NONE}。\n\t *\n\t * @return the delimiter 分隔符\n\t */",
        ),
    ],
    "DurationFormat.java": [
        (
            "/**\n * Annotation that can be used to indicate the format to use when converting a\n * {@link Duration}.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 可用于指定转换 {@link Duration} 时所使用格式的注解。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * The duration format style.\n\t * @return the duration format style.\n\t */",
            "/**\n\t * Duration 格式风格。\n\t *\n\t * @return the duration format style Duration 格式风格\n\t */",
        ),
    ],
    "DurationStyle.java": [
        (
            "/**\n * Duration format styles.\n *\n * @author Phillip Webb\n * @author Valentine Wu\n * @since 2.0.0\n */",
            "/**\n * Duration 格式风格。\n *\n * @author Phillip Webb\n * @author Valentine Wu\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Simple formatting, for example '1s'.\n\t */",
            "\t/**\n\t * 简单格式，例如 {@code 1s}。\n\t */",
        ),
        (
            "\t/**\n\t * ISO-8601 formatting.\n\t */",
            "\t/**\n\t * ISO-8601 格式。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value to a duration.\n\t * @param value the value to parse\n\t * @return a duration\n\t */",
            "\t/**\n\t * 将给定值解析为 Duration。\n\t *\n\t * @param value 要解析的值\n\t * @return a duration Duration 实例\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value to a duration.\n\t * @param value the value to parse\n\t * @param unit the duration unit to use if the value doesn't specify one ({@code null}\n\t * will default to ms)\n\t * @return a duration\n\t */",
            "\t/**\n\t * 将给定值解析为 Duration。\n\t *\n\t * @param value 要解析的值\n\t * @param unit 值未指定单位时使用的 Duration 单位（{@code null} 默认为毫秒）\n\t * @return a duration Duration 实例\n\t */",
        ),
        (
            "\t/**\n\t * Print the specified duration.\n\t * @param value the value to print\n\t * @return the printed result\n\t */",
            "\t/**\n\t * 打印指定的 Duration。\n\t *\n\t * @param value 要打印的值\n\t * @return the printed result 打印结果\n\t */",
        ),
        (
            "\t/**\n\t * Print the specified duration using the given unit.\n\t * @param value the value to print\n\t * @param unit the value to use for printing\n\t * @return the printed result\n\t */",
            "\t/**\n\t * 使用给定单位打印指定的 Duration。\n\t *\n\t * @param value 要打印的值\n\t * @param unit 打印时使用的单位\n\t * @return the printed result 打印结果\n\t */",
        ),
        (
            "\t/**\n\t * Detect the style then parse the value to return a duration.\n\t * @param value the value to parse\n\t * @return the parsed duration\n\t * @throws IllegalArgumentException if the value is not a known style or cannot be\n\t * parsed\n\t */",
            "\t/**\n\t * 检测格式风格并将值解析为 Duration。\n\t *\n\t * @param value 要解析的值\n\t * @return the parsed duration 解析后的 Duration\n\t * @throws IllegalArgumentException if the value is not a known style or cannot be parsed 值不是已知风格或无法解析时\n\t */",
        ),
        (
            "\t/**\n\t * Detect the style then parse the value to return a duration.\n\t * @param value the value to parse\n\t * @param unit the duration unit to use if the value doesn't specify one ({@code null}\n\t * will default to ms)\n\t * @return the parsed duration\n\t * @throws IllegalArgumentException if the value is not a known style or cannot be\n\t * parsed\n\t */",
            "\t/**\n\t * 检测格式风格并将值解析为 Duration。\n\t *\n\t * @param value 要解析的值\n\t * @param unit 值未指定单位时使用的 Duration 单位（{@code null} 默认为毫秒）\n\t * @return the parsed duration 解析后的 Duration\n\t * @throws IllegalArgumentException if the value is not a known style or cannot be parsed 值不是已知风格或无法解析时\n\t */",
        ),
        (
            "\t/**\n\t * Detect the style from the given source value.\n\t * @param value the source value\n\t * @return the duration style\n\t * @throws IllegalArgumentException if the value is not a known style\n\t */",
            "\t/**\n\t * 从给定源值检测 Duration 格式风格。\n\t *\n\t * @param value 源值\n\t * @return the duration style Duration 格式风格\n\t * @throws IllegalArgumentException if the value is not a known style 值不是已知风格时\n\t */",
        ),
        (
            "\t/**\n\t * Units that we support.\n\t */",
            "\t/**\n\t * 支持的时间单位。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Nanoseconds.\n\t\t */",
            "\t\t/**\n\t\t * 纳秒。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Microseconds.\n\t\t */",
            "\t\t/**\n\t\t * 微秒。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Milliseconds.\n\t\t */",
            "\t\t/**\n\t\t * 毫秒。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Seconds.\n\t\t */",
            "\t\t/**\n\t\t * 秒。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Minutes.\n\t\t */",
            "\t\t/**\n\t\t * 分钟。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Hours.\n\t\t */",
            "\t\t/**\n\t\t * 小时。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Days.\n\t\t */",
            "\t\t/**\n\t\t * 天。\n\t\t */",
        ),
    ],
    "DurationToNumberConverter.java": [
        (
            "/**\n * {@link Converter} to convert from a {@link Duration} to a {@link Number}.\n *\n * @author Phillip Webb\n * @see DurationFormat\n * @see DurationUnit\n */",
            "/**\n * 将 {@link Duration} 转换为 {@link Number} 的 {@link Converter}。\n *\n * @author Phillip Webb\n * @see DurationFormat\n * @see DurationUnit\n */",
        ),
    ],
    "DurationToStringConverter.java": [
        (
            "/**\n * {@link Converter} to convert from a {@link Duration} to a {@link String}.\n *\n * @author Phillip Webb\n * @see DurationFormat\n * @see DurationUnit\n */",
            "/**\n * 将 {@link Duration} 转换为 {@link String} 的 {@link Converter}。\n *\n * @author Phillip Webb\n * @see DurationFormat\n * @see DurationUnit\n */",
        ),
    ],
    "DurationUnit.java": [
        (
            "/**\n * Annotation that can be used to change the default unit used when converting a\n * {@link Duration}.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 可用于更改转换 {@link Duration} 时所用默认单位的注解。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * The duration unit to use if one is not specified.\n\t * @return the duration unit\n\t */",
            "/**\n\t * 未指定单位时使用的 Duration 单位。\n\t *\n\t * @return the duration unit Duration 单位\n\t */",
        ),
    ],
    "InetAddressFormatter.java": [
        (
            "/**\n * {@link Formatter} for {@link InetAddress}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 用于 {@link InetAddress} 的 {@link Formatter}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "InputStreamSourceToByteArrayConverter.java": [
        (
            "/**\n * {@link Converter} to convert from an {@link InputStreamSource} to a {@code byte[]}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 将 {@link InputStreamSource} 转换为 {@code byte[]} 的 {@link Converter}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "IsoOffsetFormatter.java": [
        (
            "/**\n * A {@link Formatter} for {@link OffsetDateTime} that uses\n * {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME ISO offset formatting}.\n *\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @author Phillip Webb\n */",
            "/**\n * 对 {@link OffsetDateTime} 使用\n * {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME ISO 偏移格式} 的 {@link Formatter}。\n *\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @author Phillip Webb\n */",
        ),
    ],
    "LenientBooleanToEnumConverterFactory.java": [
        (
            "/**\n * Converter to support mapping of YAML style {@code \"false\"} and {@code \"true\"} to enums\n * {@code ON} and {@code OFF}.\n *\n * @author Madhura Bhave\n */",
            "/**\n * 支持将 YAML 风格的 {@code \"false\"} 与 {@code \"true\"} 映射到枚举 {@code ON} 与 {@code OFF} 的转换器。\n *\n * @author Madhura Bhave\n */",
        ),
    ],
    "LenientObjectToEnumConverterFactory.java": [
        (
            "/**\n * Abstract base class for converting from a type to a {@link java.lang.Enum}.\n *\n * @param <T> the source type\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 将某类型转换为 {@link java.lang.Enum} 的抽象基类。\n *\n * @param <T> 源类型\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
    ],
    "LenientStringToEnumConverterFactory.java": [
        (
            "/**\n * Converts from a String to a {@link java.lang.Enum} with lenient conversion rules.\n * Specifically:\n * <ul>\n * <li>Uses a case insensitive search</li>\n * <li>Does not consider {@code '_'}, {@code '$'} or other special characters</li>\n * <li>Allows mapping of {@code \"false\"} and {@code \"true\"} to enums {@code ON} and\n * {@code OFF}</li>\n * </ul>\n *\n * @author Phillip Webb\n */",
            "/**\n * 使用宽松规则将 String 转换为 {@link java.lang.Enum}。具体规则：\n * <ul>\n * <li>大小写不敏感匹配</li>\n * <li>忽略 {@code '_'}、{@code '$'} 及其他特殊字符</li>\n * <li>允许将 {@code \"false\"} 与 {@code \"true\"} 映射到枚举 {@code ON} 与 {@code OFF}</li>\n * </ul>\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "NumberToDataSizeConverter.java": [
        (
            "/**\n * {@link Converter} to convert from a {@link Number} to a {@link DataSize}.\n *\n * @author Stephane Nicoll\n * @see DataSizeUnit\n */",
            "/**\n * 将 {@link Number} 转换为 {@link DataSize} 的 {@link Converter}。\n *\n * @author Stephane Nicoll\n * @see DataSizeUnit\n */",
        ),
    ],
    "NumberToDurationConverter.java": [
        (
            "/**\n * {@link Converter} to convert from a {@link Number} to a {@link Duration}. Supports\n * {@link Duration#parse(CharSequence)} as well a more readable {@code 10s} form.\n *\n * @author Phillip Webb\n * @see DurationFormat\n * @see DurationUnit\n */",
            "/**\n * 将 {@link Number} 转换为 {@link Duration} 的 {@link Converter}。\n * 支持 {@link Duration#parse(CharSequence)} 以及更易读的 {@code 10s} 形式。\n *\n * @author Phillip Webb\n * @see DurationFormat\n * @see DurationUnit\n */",
        ),
    ],
    "NumberToPeriodConverter.java": [
        (
            "/**\n * {@link Converter} to convert from a {@link Number} to a {@link Period}. Supports\n * {@link Period#parse(CharSequence)} as well a more readable {@code 10m} form.\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @see PeriodFormat\n * @see PeriodUnit\n */",
            "/**\n * 将 {@link Number} 转换为 {@link Period} 的 {@link Converter}。\n * 支持 {@link Period#parse(CharSequence)} 以及更易读的 {@code 10m} 形式。\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @see PeriodFormat\n * @see PeriodUnit\n */",
        ),
    ],
    "PeriodFormat.java": [
        (
            "/**\n * Annotation that can be used to indicate the format to use when converting a\n * {@link Period}.\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @since 2.3.0\n */",
            "/**\n * 可用于指定转换 {@link Period} 时所使用格式的注解。\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @since 2.3.0\n */",
        ),
        (
            "/**\n\t * The {@link Period} format style.\n\t * @return the period format style.\n\t */",
            "/**\n\t * {@link Period} 格式风格。\n\t *\n\t * @return the period format style Period 格式风格\n\t */",
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
