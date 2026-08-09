#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-9b batch files [20:40] (convert + diagnostics)."""
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
    "PeriodStyle.java": [
        (
            "/**\n * A standard set of {@link Period} units.\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @author Valentine Wu\n * @since 2.3.0\n * @see Period\n */",
            "/**\n * 标准的 {@link Period} 单位集合。\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @author Valentine Wu\n * @since 2.3.0\n * @see Period\n */",
        ),
        (
            "\t/**\n\t * Simple formatting, for example '1d'.\n\t */",
            "\t/**\n\t * 简单格式，例如 '1d'。\n\t */",
        ),
        (
            "\t/**\n\t * ISO-8601 formatting.\n\t */",
            "\t/**\n\t * ISO-8601 格式。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value to a Period.\n\t * @param value the value to parse\n\t * @return a period\n\t */",
            "\t/**\n\t * 将给定值解析为 Period。\n\t *\n\t * @param value 待解析的值\n\t * @return 解析得到的 period\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value to a period.\n\t * @param value the value to parse\n\t * @param unit the period unit to use if the value doesn't specify one ({@code null}\n\t * will default to d)\n\t * @return a period\n\t */",
            "\t/**\n\t * 将给定值解析为 period。\n\t *\n\t * @param value 待解析的值\n\t * @param unit 值未指定单位时使用的 period 单位（{@code null} 默认为 d）\n\t * @return 解析得到的 period\n\t */",
        ),
        (
            "\t/**\n\t * Print the specified period.\n\t * @param value the value to print\n\t * @return the printed result\n\t */",
            "\t/**\n\t * 打印指定的 period。\n\t *\n\t * @param value 待打印的值\n\t * @return 打印结果\n\t */",
        ),
        (
            "\t/**\n\t * Print the specified period using the given unit.\n\t * @param value the value to print\n\t * @param unit the value to use for printing\n\t * @return the printed result\n\t */",
            "\t/**\n\t * 使用给定单位打印指定的 period。\n\t *\n\t * @param value 待打印的值\n\t * @param unit 用于打印的单位\n\t * @return 打印结果\n\t */",
        ),
        (
            "\t/**\n\t * Detect the style then parse the value to return a period.\n\t * @param value the value to parse\n\t * @return the parsed period\n\t * @throws IllegalArgumentException if the value is not a known style or cannot be\n\t * parsed\n\t */",
            "\t/**\n\t * 检测格式后解析值并返回 period。\n\t *\n\t * @param value 待解析的值\n\t * @return 解析得到的 period\n\t * @throws IllegalArgumentException 若值不是已知格式或无法解析\n\t */",
        ),
        (
            "\t/**\n\t * Detect the style then parse the value to return a period.\n\t * @param value the value to parse\n\t * @param unit the period unit to use if the value doesn't specify one ({@code null}\n\t * will default to ms)\n\t * @return the parsed period\n\t * @throws IllegalArgumentException if the value is not a known style or cannot be\n\t * parsed\n\t */",
            "\t/**\n\t * 检测格式后解析值并返回 period。\n\t *\n\t * @param value 待解析的值\n\t * @param unit 值未指定单位时使用的 period 单位（{@code null} 默认为 ms）\n\t * @return 解析得到的 period\n\t * @throws IllegalArgumentException 若值不是已知格式或无法解析\n\t */",
        ),
        (
            "\t/**\n\t * Detect the style from the given source value.\n\t * @param value the source value\n\t * @return the period style\n\t * @throws IllegalArgumentException if the value is not a known style\n\t */",
            "\t/**\n\t * 从给定源值检测 period 格式。\n\t *\n\t * @param value 源值\n\t * @return period 格式\n\t * @throws IllegalArgumentException 若值不是已知格式\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Days, represented by suffix {@code d}.\n\t\t */",
            "\t\t/**\n\t\t * 天，后缀为 {@code d}。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Weeks, represented by suffix {@code w}.\n\t\t */",
            "\t\t/**\n\t\t * 周，后缀为 {@code w}。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Months, represented by suffix {@code m}.\n\t\t */",
            "\t\t/**\n\t\t * 月，后缀为 {@code m}。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Years, represented by suffix {@code y}.\n\t\t */",
            "\t\t/**\n\t\t * 年，后缀为 {@code y}。\n\t\t */",
        ),
    ],
    "PeriodToStringConverter.java": [
        (
            "/**\n * {@link Converter} to convert from a {@link Period} to a {@link String}.\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @see PeriodFormat\n * @see PeriodUnit\n */",
            "/**\n * 将 {@link Period} 转换为 {@link String} 的 {@link Converter}。\n * 可通过 {@link PeriodFormat} 与 {@link PeriodUnit} 注解指定输出格式与默认单位。\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @see PeriodFormat\n * @see PeriodUnit\n */",
        ),
    ],
    "PeriodUnit.java": [
        (
            "/**\n * Annotation that can be used to change the default unit used when converting a\n * {@link Period}.\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @since 2.3.0\n */",
            "/**\n * 可用于更改转换 {@link Period} 时默认单位的注解。\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @since 2.3.0\n */",
        ),
        (
            "\t/**\n\t * The Period unit to use if one is not specified.\n\t * @return the Period unit\n\t */",
            "\t/**\n\t * 未指定单位时使用的 Period 单位。\n\t *\n\t * @return Period 单位\n\t */",
        ),
    ],
    "StringToDataSizeConverter.java": [
        (
            "/**\n * {@link Converter} to convert from a {@link String} to a {@link DataSize}. Supports\n * {@link DataSize#parse(CharSequence)}.\n *\n * @author Stephane Nicoll\n * @see DataSizeUnit\n */",
            "/**\n * 将 {@link String} 转换为 {@link DataSize} 的 {@link Converter}。\n * 内部调用 {@link DataSize#parse(CharSequence)} 完成解析。\n * 可通过 {@link DataSizeUnit} 注解指定默认数据单位。\n *\n * @author Stephane Nicoll\n * @see DataSizeUnit\n */",
        ),
    ],
    "StringToDurationConverter.java": [
        (
            "/**\n * {@link Converter} to convert from a {@link String} to a {@link Duration}. Supports\n * {@link Duration#parse(CharSequence)} as well a more readable {@code 10s} form.\n *\n * @author Phillip Webb\n * @see DurationFormat\n * @see DurationUnit\n */",
            "/**\n * 将 {@link String} 转换为 {@link Duration} 的 {@link Converter}。\n * 支持 {@link Duration#parse(CharSequence)} 以及更易读的 {@code 10s} 形式。\n *\n * @author Phillip Webb\n * @see DurationFormat\n * @see DurationUnit\n */",
        ),
    ],
    "StringToFileConverter.java": [
        (
            "/**\n * {@link Converter} to convert from a {@link String} to a {@link File}. Supports basic\n * file conversion as well as file URLs.\n *\n * @author Phillip Webb\n * @author Scott Frederick\n */",
            "/**\n * 将 {@link String} 转换为 {@link File} 的 {@link Converter}。\n * 支持基本文件转换以及文件 URL。\n *\n * @author Phillip Webb\n * @author Scott Frederick\n */",
        ),
    ],
    "StringToPeriodConverter.java": [
        (
            "/**\n * {@link Converter} to convert from a {@link String} to a {@link Period}. Supports\n * {@link Period#parse(CharSequence)} as well a more readable form.\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @see PeriodFormat\n * @see PeriodUnit\n */",
            "/**\n * 将 {@link String} 转换为 {@link Period} 的 {@link Converter}。\n * 支持 {@link Period#parse(CharSequence)} 以及更易读的形式。\n *\n * @author Eddú Meléndez\n * @author Edson Chávez\n * @see PeriodFormat\n * @see PeriodUnit\n */",
        ),
    ],
    "AbstractFailureAnalyzer.java": [
        (
            "/**\n * Abstract base class for most {@code FailureAnalyzer} implementations.\n *\n * @param <T> the type of exception to analyze\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 1.4.0\n */",
            "/**\n * 大多数 {@code FailureAnalyzer} 实现的抽象基类。\n *\n * @param <T> 待分析异常的类型\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 1.4.0\n */",
        ),
        (
            "\t/**\n\t * Returns an analysis of the given {@code rootFailure}, or {@code null} if no\n\t * analysis was possible.\n\t * @param rootFailure the root failure passed to the analyzer\n\t * @param cause the actual found cause\n\t * @return the analysis or {@code null}\n\t */",
            "\t/**\n\t * 返回对给定 {@code rootFailure} 的分析，若无法分析则返回 {@code null}。\n\t *\n\t * @param rootFailure 传入分析器的根失败\n\t * @param cause 实际找到的 cause\n\t * @return 分析结果或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Return the cause type being handled by the analyzer. By default the class generic\n\t * is used.\n\t * @return the cause type\n\t */",
            "\t/**\n\t * 返回分析器处理的 cause 类型。默认使用类的泛型参数。\n\t *\n\t * @return cause 类型\n\t */",
        ),
    ],
    "FailureAnalysis.java": [
        (
            "/**\n * The result of analyzing a failure.\n *\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
            "/**\n * 失败分析的结果。\n *\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
        ),
        (
            "\t/**\n\t * Creates a new {@code FailureAnalysis} with the given {@code description} and\n\t * {@code action}, if any, that the user should take to address the problem. The\n\t * failure had the given underlying {@code cause}.\n\t * @param description the description\n\t * @param action the action\n\t * @param cause the cause\n\t */",
            "\t/**\n\t * 使用给定 {@code description} 与 {@code action}（若有）创建新的 {@code FailureAnalysis}，\n\t * 供用户采取以解决问题。失败具有给定的底层 {@code cause}。\n\t *\n\t * @param description 描述\n\t * @param action 建议操作\n\t * @param cause 原因\n\t */",
        ),
        (
            "\t/**\n\t * Returns a description of the failure.\n\t * @return the description\n\t */",
            "\t/**\n\t * 返回失败描述。\n\t *\n\t * @return 描述\n\t */",
        ),
        (
            "\t/**\n\t * Returns the action, if any, to be taken to address the failure.\n\t * @return the action or {@code null}\n\t */",
            "\t/**\n\t * 返回为解决失败建议采取的操作（若有）。\n\t *\n\t * @return 操作或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Returns the cause of the failure.\n\t * @return the cause\n\t */",
            "\t/**\n\t * 返回失败原因。\n\t *\n\t * @return 原因\n\t */",
        ),
    ],
    "FailureAnalysisReporter.java": [
        (
            "/**\n * Reports a {@code FailureAnalysis} to the user.\n *\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
            "/**\n * 向用户报告 {@code FailureAnalysis}。\n *\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
        ),
        (
            "\t/**\n\t * Reports the given {@code failureAnalysis} to the user.\n\t * @param analysis the analysis\n\t */",
            "\t/**\n\t * 向用户报告给定的 {@code failureAnalysis}。\n\t *\n\t * @param analysis 分析结果\n\t */",
        ),
    ],
    "FailureAnalyzedException.java": [
        (
            "/**\n * {@link RuntimeException} that includes a {@link FailureAnalysis}.\n *\n * @author Phillip Webb\n * @since 4.1.0\n */",
            "/**\n * 包含 {@link FailureAnalysis} 的 {@link RuntimeException}。\n *\n * @author Phillip Webb\n * @since 4.1.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link FailureAnalyzedException} instance.\n\t * @param description the {@link FailureAnalysis} description\n\t * @param action the {@link FailureAnalysis} action\n\t */",
            "\t/**\n\t * 创建新的 {@link FailureAnalyzedException} 实例。\n\t *\n\t * @param description {@link FailureAnalysis} 描述\n\t * @param action {@link FailureAnalysis} 建议操作\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link FailureAnalyzedException} instance.\n\t * @param description the {@link FailureAnalysis} description\n\t * @param action the {@link FailureAnalysis} action\n\t * @param cause the cause of the exception\n\t */",
            "\t/**\n\t * 创建新的 {@link FailureAnalyzedException} 实例。\n\t *\n\t * @param description {@link FailureAnalysis} 描述\n\t * @param action {@link FailureAnalysis} 建议操作\n\t * @param cause 异常原因\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@link FailureAnalysis} to use for this exception.\n\t * @return the failure analysis\n\t */",
            "\t/**\n\t * 返回此异常对应的 {@link FailureAnalysis}。\n\t *\n\t * @return 失败分析\n\t */",
        ),
    ],
    "FailureAnalyzer.java": [
        (
            "/**\n * A {@code FailureAnalyzer} is used to analyze a failure and provide diagnostic\n * information that can be displayed to the user.\n *\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
            "/**\n * {@code FailureAnalyzer} 用于分析失败并提供可展示给用户的诊断信息。\n *\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
        ),
        (
            "\t/**\n\t * Returns an analysis of the given {@code failure}, or {@code null} if no analysis\n\t * was possible.\n\t * @param failure the failure\n\t * @return the analysis or {@code null}\n\t */",
            "\t/**\n\t * 返回对给定 {@code failure} 的分析，若无法分析则返回 {@code null}。\n\t *\n\t * @param failure 失败\n\t * @return 分析结果或 {@code null}\n\t */",
        ),
    ],
    "FailureAnalyzers.java": [
        (
            "/**\n * Utility to trigger {@link FailureAnalyzer} and {@link FailureAnalysisReporter}\n * instances loaded from {@code spring.factories}.\n * <p>\n * A {@code FailureAnalyzer} that requires access to the {@link BeanFactory} or\n * {@link Environment} in order to perform its analysis can implement a constructor that\n * accepts arguments of one or both of these types.\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @author Scott Frederick\n */",
            "/**\n * 触发从 {@code spring.factories} 加载的 {@link FailureAnalyzer} 与\n * {@link FailureAnalysisReporter} 实例的工具类。\n * <p>\n * 若 {@code FailureAnalyzer} 需要访问 {@link BeanFactory} 或 {@link Environment} 才能执行分析，\n * 可实现接受上述一种或两种类型参数的构造器。\n *\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @author Scott Frederick\n */",
        ),
    ],
    "LoggingFailureAnalysisReporter.java": [
        (
            "/**\n * {@link FailureAnalysisReporter} that logs the failure analysis.\n *\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
            "/**\n * 将失败分析记录到日志的 {@link FailureAnalysisReporter}。\n *\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
        ),
    ],
    "AbstractInjectionFailureAnalyzer.java": [
        (
            "/**\n * Abstract base class for a {@link FailureAnalyzer} that handles some kind of injection\n * failure.\n *\n * @param <T> the type of exception to analyze\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @since 1.4.1\n */",
            "/**\n * 处理某种注入失败的 {@link FailureAnalyzer} 抽象基类。\n *\n * @param <T> 待分析异常的类型\n * @author Andy Wilkinson\n * @author Stephane Nicoll\n * @since 1.4.1\n */",
        ),
        (
            "\t/**\n\t * Returns an analysis of the given {@code rootFailure}, or {@code null} if no\n\t * analysis was possible.\n\t * @param rootFailure the root failure passed to the analyzer\n\t * @param cause the actual found cause\n\t * @param description the description of the injection point or {@code null}\n\t * @return the analysis or {@code null}\n\t */",
            "\t/**\n\t * 返回对给定 {@code rootFailure} 的分析，若无法分析则返回 {@code null}。\n\t *\n\t * @param rootFailure 传入分析器的根失败\n\t * @param cause 实际找到的 cause\n\t * @param description 注入点描述或 {@code null}\n\t * @return 分析结果或 {@code null}\n\t */",
        ),
    ],
    "AotInitializerNotFoundFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractFailureAnalyzer} that performs analysis of failures caused by a\n * {@link AotInitializerNotFoundException}.\n *\n * @author Moritz Halbritter\n */",
            "/**\n * 分析由 {@link AotInitializerNotFoundException} 引起启动失败的 {@link AbstractFailureAnalyzer}。\n * 通常与 AOT 处理未启用或主类配置错误有关。\n *\n * @author Moritz Halbritter\n */",
        ),
    ],
    "BeanCurrentlyInCreationFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractFailureAnalyzer} that performs analysis of failures caused by a\n * {@link BeanCurrentlyInCreationException}.\n *\n * @author Andy Wilkinson\n * @author Scott Frederick\n */",
            "/**\n * 分析由 {@link BeanCurrentlyInCreationException} 引起失败的 {@link AbstractFailureAnalyzer}。\n * 用于检测并可视化 Bean 之间的循环依赖关系。\n *\n * @author Andy Wilkinson\n * @author Scott Frederick\n */",
        ),
    ],
    "BeanDefinitionOverrideFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractFailureAnalyzer} that performs analysis of failures caused by a\n * {@link BeanDefinitionOverrideException}.\n *\n * @author Andy Wilkinson\n */",
            "/**\n * 分析由 {@link BeanDefinitionOverrideException} 引起失败的 {@link AbstractFailureAnalyzer}。\n * 当同名 Bean 定义冲突且未启用覆盖时触发。\n *\n * @author Andy Wilkinson\n */",
        ),
    ],
    "BeanNotOfRequiredTypeFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractFailureAnalyzer} that performs analysis of failures caused by a\n * {@link BeanNotOfRequiredTypeException}.\n *\n * @author Andy Wilkinson\n * @author Scott Frederick\n * @since 1.4.0\n */",
            "/**\n * 分析由 {@link BeanNotOfRequiredTypeException} 引起失败的 {@link AbstractFailureAnalyzer}。\n * 常见于 JDK 动态代理 Bean 与所需接口类型不匹配的场景。\n *\n * @author Andy Wilkinson\n * @author Scott Frederick\n * @since 1.4.0\n */",
        ),
    ],
    "BindFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractFailureAnalyzer} that performs analysis of failures caused by a\n * {@link BindException} excluding {@link BindValidationException} and\n * {@link UnboundConfigurationPropertiesException}.\n *\n * @author Andy Wilkinson\n * @author Madhura Bhave\n * @author Phillip Webb\n */",
            "/**\n * 分析由 {@link BindException} 引起失败的 {@link AbstractFailureAnalyzer}，\n * 排除 {@link BindValidationException} 与 {@link UnboundConfigurationPropertiesException}。\n *\n * @author Andy Wilkinson\n * @author Madhura Bhave\n * @author Phillip Webb\n */",
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
                "wave9b convert/diagnostics [20:40]",
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
