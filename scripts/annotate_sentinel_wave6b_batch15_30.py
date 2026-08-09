#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-6b block [15:30] (system/spi/util)."""
from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = Path("/tmp/sentinel_w6b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["SystemSlot.java"] = [
    (
        "/**\n * A {@link ProcessorSlot} that dedicates to {@link SystemRule} checking.\n *\n * @author jialiang.linjl\n * @author leyou\n */",
        "/**\n * 专用于 {@link SystemRule} 校验的 {@link ProcessorSlot}。\n *\n * @author jialiang.linjl\n * @author leyou\n */",
    ),
]

R["SystemStatusListener.java"] = [
    (
        "/**\n * @author jialiang.linjl\n */",
        "/**\n * 系统状态监听器，周期性采集系统负载与 CPU 使用率，供 {@link SystemRuleManager} 判定系统保护规则。\n *\n * @author jialiang.linjl\n */",
    ),
    (
        "            /*\n             * Java Doc copied from {@link OperatingSystemMXBean#getSystemCpuLoad()}:</br>\n             * Returns the \"recent cpu usage\" for the whole system. This value is a double in the [0.0,1.0] interval.\n             * A value of 0.0 means that all CPUs were idle during the recent period of time observed, while a value\n             * of 1.0 means that all CPUs were actively running 100% of the time during the recent period being\n             * observed. All values between 0.0 and 1.0 are possible depending of the activities going on in the\n             * system. If the system recent cpu usage is not available, the method returns a negative value.\n             */",
        "            /*\n             * 摘自 {@link OperatingSystemMXBean#getSystemCpuLoad()} 的 JavaDoc：</br>\n             * 返回整个系统\"近期 CPU 使用率\"，取值区间为 [0.0, 1.0] 的 double。\n             * 0.0 表示观测期内所有 CPU 均空闲；1.0 表示所有 CPU 在观测期内 100% 满负荷运行。\n             * 0.0 到 1.0 之间的值取决于系统当前活动。若无法获取近期 CPU 使用率，则返回负值。\n             */",
    ),
    (
        "            // calculate process cpu usage to support application running in container environment",
        "            // 计算进程 CPU 使用率，以支持容器环境中的应用",
    ),
]

R["Spi.java"] = [
    (
        "/**\n * Annotation for Provider class of SPI.\n *\n * @see SpiLoader\n * @author cdfive\n */",
        "/**\n * 标注 SPI Provider 实现类的注解。\n *\n * @see SpiLoader\n * @author cdfive\n */",
    ),
    (
        "    /**\n     * Alias name of Provider class\n     */",
        "    /**\n     * Provider 实现类的别名\n     */",
    ),
    (
        "    /**\n     * Whether create singleton instance\n     */",
        "    /**\n     * 是否创建单例实例\n     */",
    ),
    (
        "    /**\n     * Whether is the default Provider\n     */",
        "    /**\n     * 是否为默认 Provider\n     */",
    ),
    (
        "    /**\n     * Order priority of Provider class\n     */",
        "    /**\n     * Provider 实现类的排序优先级\n     */",
    ),
]

R["SpiLoaderException.java"] = [
    (
        "/**\n * Error thrown when something goes wrong while loading Provider via {@link SpiLoader}.\n *\n * @author cdfive\n */",
        "/**\n * 通过 {@link SpiLoader} 加载 Provider 时发生错误所抛出的异常。\n *\n * @author cdfive\n */",
    ),
]

R["AppNameUtil.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @author leyou\n */",
        "/**\n * 应用名工具类，从 {@link com.alibaba.csp.sentinel.config.SentinelConfig} 读取当前应用名。\n *\n * @author Eric Zhao\n * @author leyou\n */",
    ),
]

R["AssertUtil.java"] = [
    (
        "/**\n * Util class for checking arguments.\n *\n * @author Eric Zhao\n */",
        "/**\n * 参数校验工具类，不满足条件时抛出 {@link IllegalArgumentException} 或 {@link IllegalStateException}。\n *\n * @author Eric Zhao\n */",
    ),
]

R["ConfigUtil.java"] = [
    (
        "/**\n * <p>\n * Util class for loading configuration from file or command arguments.\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
        "/**\n * <p>\n * 从文件或命令行参数加载配置的工具类。\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
    ),
    (
        "    /**\n     * <p>Load the properties from provided file.</p>\n     * <p>Currently it supports reading from classpath file or local file.</p>\n     *\n     * @param fileName valid file path\n     * @return the retrieved properties from the file; null if the file not exist\n     */",
        "    /**\n     * <p>从指定文件加载 Properties。</p>\n     * <p>当前支持从 classpath 文件或本地文件读取。</p>\n     *\n     * @param fileName 有效的文件路径\n     * @return 从文件读取的 Properties；文件不存在时返回 null\n     */",
    ),
    (
        "        // avoid static loop dependencies: SentinelConfig -> SentinelConfigLoader -> ConfigUtil -> SentinelConfig\n        // so not use SentinelConfig.charset()",
        "        // 避免静态循环依赖：SentinelConfig -> SentinelConfigLoader -> ConfigUtil -> SentinelConfig\n        // 故不使用 SentinelConfig.charset()",
    ),
]

R["HostNameUtil.java"] = [
    (
        "/**\n * Get host name and ip of the host.\n *\n * @author leyou\n */",
        "/**\n * 获取本机主机名与 IP 地址的工具类。\n *\n * @author leyou\n */",
    ),
    (
        "            // Init the host information.",
        "            // 初始化主机信息。",
    ),
    (
        "            // find the first IPv4 Address that not loopback",
        "            // 查找第一个非回环的 IPv4 地址",
    ),
]

R["IdUtil.java"] = [
    (
        "/**\n * @author qinan.qn\n */",
        "/**\n * 资源 ID 处理工具类，用于截断并规范化资源标识字符串。\n *\n * @author qinan.qn\n */",
    ),
]

R["MethodUtil.java"] = [
    (
        "/***\n * Util class for processing {@link Method}.\n *\n * @author youji.zj\n */",
        "/***\n * 处理 {@link Method} 的工具类，解析并缓存方法签名。\n *\n * @author youji.zj\n */",
    ),
    (
        "    /**\n     * Parse and resolve the method name, then cache to the map.\n     *\n     * @param method method instance\n     * @return resolved method name\n     */",
        "    /**\n     * 解析并生成方法名，随后缓存到映射中。\n     *\n     * @param method 方法实例\n     * @return 解析后的方法名\n     */",
    ),
    (
        "    /**\n     * For test.\n     */",
        "    /**\n     * 供测试使用。\n     */",
    ),
]

R["PidUtil.java"] = [
    (
        "/**\n * Util class providing pid of current process.\n */",
        "/**\n * 提供当前进程 PID 的工具类。\n */",
    ),
    (
        "    /**\n     * Resolve and get current process ID.\n     *\n     * @return current process ID\n     */",
        "    /**\n     * 解析并获取当前进程 ID。\n     *\n     * @return 当前进程 ID\n     */",
    ),
    (
        "        // Note: this will trigger local host resolve, which might be slow.",
        "        // 注意：此操作会触发本机解析，可能较慢。",
    ),
]

R["StringUtil.java"] = [
    (
        "/***\n * Util class providing operations on {@link String}.\n *\n * @author youji.zj\n */",
        "/***\n * 提供 {@link String} 常用操作的工具类。\n *\n * @author youji.zj\n */",
    ),
    (
        "        // Extract these first so we detect NPEs the same as the java.lang.String version",
        "        // 先提取这些变量，以便与 java.lang.String 版本一样检测 NPE",
    ),
    (
        "        // Check for invalid parameters",
        "        // 校验非法参数",
    ),
    (
        "        // Check that the regions are long enough",
        "        // 校验区域长度是否足够",
    ),
    (
        "            // The same check as in String.regionMatches():",
        "            // 与 String.regionMatches() 相同的比较逻辑：",
    ),
]

R["TimeUtil.java"] = [
    (
        "/**\n * <p>Provides millisecond-level time of OS.</p>\n * <p>\n * Here we should see that not all the time TimeUtil should\n * keep looping 1_000 times every second (Actually about 800/s due to some losses).\n * <pre>\n * * In idle conditions it just acts as System.currentTimeMillis();\n * * In busy conditions (significantly more than 1_000/s) it keeps loop to reduce costs.\n * </pre>\n * For detail design and proposals please goto\n * <a href=\"https://github.com/alibaba/Sentinel/issues/1702#issuecomment-692151160\">https://github.com/alibaba/Sentinel/issues/1702</a>\n *\n * @author qinan.qn\n * @author jason\n */",
        "/**\n * <p>提供操作系统毫秒级时间戳。</p>\n * <p>\n * 并非始终需要每秒循环 1_000 次（实际约 800 次/秒，存在损耗）：\n * <pre>\n * * 空闲时行为等同 System.currentTimeMillis()；\n * * 高负载时（显著超过 1_000 次/秒）持续循环以降低开销。\n * </pre>\n * 详细设计与方案请参阅\n * <a href=\"https://github.com/alibaba/Sentinel/issues/1702#issuecomment-692151160\">https://github.com/alibaba/Sentinel/issues/1702</a>\n *\n * @author qinan.qn\n * @author jason\n */",
    ),
    (
        "    /**\n     * thread private variables\n     */",
        "    /**\n     * 线程私有变量\n     */",
    ),
    (
        "            // Mechanism optimized since 1.8.2",
        "            // 自 1.8.2 起优化的机制",
    ),
    (
        "    /**\n     * Current running state\n     *\n     * @return\n     */",
        "    /**\n     * 当前运行状态。\n     *\n     * @return 当前状态\n     */",
    ),
    (
        "    /**\n     * Current qps statistics (including reads and writes request)\n     * excluding current working time window for accurate result.\n     *\n     * @param now\n     * @return\n     */",
        "    /**\n     * 当前 QPS 统计（含读与写请求），\n     * 排除当前工作窗口以获得更准确的结果。\n     *\n     * @param now 当前时间戳\n     * @return 读/写 QPS 元组\n     */",
    ),
    (
        "    /**\n     * Check and operate the state if necessary.\n     * ATTENTION: It's called in daemon thread.\n     */",
        "    /**\n     * 按需检查并切换运行状态。\n     * 注意：在守护线程中调用。\n     */",
    ),
    (
        "        // every period",
        "        // 每个检查周期",
    ),
    (
        "    /**\n     * Current timestamp in milliseconds.\n     *\n     * @return\n     */",
        "    /**\n     * 当前毫秒时间戳。\n     *\n     * @return 毫秒时间戳\n     */",
    ),
]

R["VersionUtil.java"] = [
    (
        "/**\n * Get version of Sentinel from {@code MANIFEST.MF} file.\n * \n * @author jason\n * @since 0.2.1\n */",
        "/**\n * 从 {@code MANIFEST.MF} 读取 Sentinel 版本号的工具类。\n *\n * @author jason\n * @since 0.2.1\n */",
    ),
    (
        "    /**\n     * Convert version in string like x.y.z or x.y.z.b into number<br />\n     * Each segment has one byte space(unsigned)<br />\n     * eg.<br />\n     * <pre>\n     * 1.2.3.4 => 01 02 03 04\n     * 1.2.3   => 01 02 03 00\n     * 1.2     => 01 02 00 00\n     * 1       => 01 00 00 00\n     * </pre>\n     * \n     * @return\n     */",
        "    /**\n     * 将 x.y.z 或 x.y.z.b 格式的版本字符串转换为整数。<br />\n     * 每段占一字节（无符号）。<br />\n     * 例如：<br />\n     * <pre>\n     * 1.2.3.4 => 01 02 03 04\n     * 1.2.3   => 01 02 03 00\n     * 1.2     => 01 02 00 00\n     * 1       => 01 00 00 00\n     * </pre>\n     *\n     * @return 编码后的版本整数\n     */",
    ),
    (
        "                // More dots than \"x.y.z.b\" contains",
        "                // 分段数超过 \"x.y.z.b\" 格式允许的上限",
    ),
    (
        "                // Illegal format",
        "                // 非法格式",
    ),
    (
        "                // Out of range [0, 255]",
        "                // 超出 [0, 255] 范围",
    ),
]

R["BiConsumer.java"] = [
    (
        "/**\n * BiConsumer interface from JDK 8.\n */",
        "/**\n * JDK 8 中的 BiConsumer 函数式接口，接受两个参数且无返回值。\n */",
    ),
]


def apply_replacements(rel: str) -> None:
    name = Path(rel).name
    path = ANALYZED / rel
    text = path.read_text(encoding="utf-8")
    for old, new in R.get(name, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not re.search(r"[\u4e00-\u9fff]", text):
        raise SystemExit(f"No Chinese in {rel} after annotation")
    path.write_text(text, encoding="utf-8")


def update_batch_json() -> None:
    batch = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))
    remaining = [f for f in batch["files"] if f not in BATCH_LIST]
    batch["files"] = remaining
    batch["done"] = batch.get("done", 150) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 785) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run(
        [
            sys.executable,
            str(ROOT / "scripts/mark_batch_done.py"),
            "--project",
            "sentinel",
            "--version",
            "1.8.10",
            "--note",
            "wave6b system/util [15:30]",
            *BATCH_LIST,
        ],
        check=True,
    )
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
