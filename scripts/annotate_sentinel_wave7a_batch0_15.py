#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-7a util/dubbo [0:15]."""
from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
ORIGINAL = VER / "original"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = Path("/tmp/sentinel_w7a.txt").read_text(encoding="utf-8").strip().split("\n")[:15]

R: dict[str, list[tuple[str, str]]] = {}

R["Consumer.java"] = [
    (
        "/**\n * Consumer interface from JDK 8.\n */",
        "/**\n * JDK 8 中的 Consumer 函数式接口，接受一个参数且无返回值。\n */",
    ),
    (
        "    /**\n     * Performs this operation on the given argument.\n     *\n     * @param t the input argument\n     */",
        "    /**\n     * 对给定参数执行此操作。\n     *\n     * @param t the input argument\n     */",
    ),
]

R["Function.java"] = [
    (
        "/**\n * Function functional interface from JDK 8.\n */",
        "/**\n * JDK 8 中的 Function 函数式接口，接受一个参数并返回结果。\n */",
    ),
    (
        "    /**\n     * Applies this function to the given argument.\n     *\n     * @param t the function argument\n     * @return the function result\n     */",
        "    /**\n     * 将此函数应用于给定参数。\n     *\n     * @param t the function argument\n     * @return the function result\n     */",
    ),
    (
        "    /**\n     * Returns a function that always returns its input argument.\n     *\n     * @param <T> the type of the input and output objects to the function\n     * @return a function that always returns its input argument\n     */",
        "    /**\n     * 返回恒等函数，始终返回其输入参数。\n     *\n     * @param <T> the type of the input and output objects to the function\n     * @return a function that always returns its input argument\n     */",
    ),
]

R["Predicate.java"] = [
    (
        "/**\n * Predicate functional interface from JDK 8.\n */",
        "/**\n * JDK 8 中的 Predicate 函数式接口，对给定参数进行布尔判定。\n */",
    ),
    (
        "    /**\n     * Evaluates this predicate on the given argument.\n     *\n     * @param t the input argument\n     * @return {@code true} if the input argument matches the predicate,\n     * otherwise {@code false}\n     */",
        "    /**\n     * 对给定参数求值此谓词。\n     *\n     * @param t the input argument\n     * @return {@code true} if the input argument matches the predicate,\n     * otherwise {@code false}\n     */",
    ),
]

R["Supplier.java"] = [
    (
        "/**\n * Supplier functional interface from JDK 8.\n */",
        "/**\n * JDK 8 中的 Supplier 函数式接口，无参并返回一个结果。\n */",
    ),
    (
        "    /**\n     * Gets a result.\n     *\n     * @return a result\n     */",
        "    /**\n     * 获取结果。\n     *\n     * @return a result\n     */",
    ),
]

R["Tuple2.java"] = [
    (
        "/**\n * A tuple of 2 elements.\n */",
        "/**\n * 包含两个元素的元组。\n */",
    ),
    (
        "    public Tuple2(R1 r1, R2 r2) {",
        "    /** 使用两个元素构造元组。 */\n    public Tuple2(R1 r1, R2 r2) {",
    ),
    (
        "    /**\n     * Factory method for creating a Tuple.\n     *\n     * @return new Tuple\n     */",
        "    /**\n     * 创建元组的工厂方法。\n     *\n     * @return new Tuple\n     */",
    ),
    (
        "    /**\n     * Swaps the element of this Tuple.\n     *\n     * @return a new Tuple where the first element is the second element of this Tuple and the second element is the first element of this Tuple.\n     */",
        "    /**\n     * 交换本元组两个元素的位置。\n     *\n     * @return a new Tuple where the first element is the second element of this Tuple and the second element is the first element of this Tuple.\n     */",
    ),
]

R["BaseSentinelDubboFilter.java"] = [
    (
        "/**\n * Base class of the {@link SentinelDubboProviderFilter} and {@link SentinelDubboConsumerFilter}.\n *\n * @author Zechao Zheng\n */",
        "/**\n * {@link SentinelDubboProviderFilter} 与 {@link SentinelDubboConsumerFilter} 的抽象基类。\n *\n * @author Zechao Zheng\n */",
    ),
    (
        "    /**\n     * Get method name of dubbo rpc\n     *\n     * @param invoker\n     * @param invocation\n     * @return\n     */",
        "    /**\n     * 获取 Dubbo RPC 方法资源名。\n     *\n     * @param invoker\n     * @param invocation\n     * @return\n     */",
    ),
    (
        "    /**\n     * Get interface name of dubbo rpc\n     *\n     * @param invoker\n     * @return\n     */",
        "    /**\n     * 获取 Dubbo RPC 接口资源名。\n     *\n     * @param invoker\n     * @return\n     */",
    ),
]

R["DubboAppContextFilter.java"] = [
    (
        "/**\n * Puts current consumer's application name in the attachment of each invocation.\n *\n * @author Eric Zhao\n */",
        "/**\n * 将当前消费者的应用名写入每次调用的 attachment，供 Provider 端解析调用来源。\n *\n * @author Eric Zhao\n */",
    ),
]

R["DubboUtils.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 适配器工具类，用于构建资源名与读取调用方应用名。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public static final String SENTINEL_DUBBO_APPLICATION_KEY = \"dubboApplication\";",
        "    /** Sentinel 在 Dubbo attachment 中传递应用名的键。 */\n    public static final String SENTINEL_DUBBO_APPLICATION_KEY = \"dubboApplication\";",
    ),
    (
        "    public static String getApplication(Invocation invocation, String defaultValue) {",
        "    /** 从 invocation attachment 读取调用方应用名。 */\n    public static String getApplication(Invocation invocation, String defaultValue) {",
    ),
    (
        "    public static String getMethodResourceName(Invoker<?> invoker, Invocation invocation){",
        "    /** 构建方法级 Sentinel 资源名（不含前缀）。 */\n    public static String getMethodResourceName(Invoker<?> invoker, Invocation invocation){",
    ),
    (
        "    public static String getMethodResourceName(Invoker<?> invoker, Invocation invocation, Boolean useGroupAndVersion) {",
        "    /** 构建方法级 Sentinel 资源名，可选包含 group 与 version。 */\n    public static String getMethodResourceName(Invoker<?> invoker, Invocation invocation, Boolean useGroupAndVersion) {",
    ),
    (
        "    public static String getMethodResourceName(Invoker<?> invoker, Invocation invocation, String prefix) {",
        "    /** 构建带前缀的方法级 Sentinel 资源名。 */\n    public static String getMethodResourceName(Invoker<?> invoker, Invocation invocation, String prefix) {",
    ),
    (
        "    public static String getInterfaceName(Invoker invoker) {",
        "    /** 构建接口级 Sentinel 资源名（不含前缀）。 */\n    public static String getInterfaceName(Invoker invoker) {",
    ),
    (
        "    public static String getInterfaceName(Invoker<?> invoker, Boolean useGroupAndVersion) {",
        "    /** 构建接口级 Sentinel 资源名，可选包含 group 与 version。 */\n    public static String getInterfaceName(Invoker<?> invoker, Boolean useGroupAndVersion) {",
    ),
    (
        "    public static String getInterfaceName(Invoker<?> invoker, String prefix) {",
        "    /** 构建带前缀的接口级 Sentinel 资源名。 */\n    public static String getInterfaceName(Invoker<?> invoker, String prefix) {",
    ),
]

R["SentinelDubboConsumerFilter.java"] = [
    (
        "/**\n * <p>Dubbo service consumer filter for Sentinel. Auto activated by default.</p>\n * <p>\n * If you want to disable the consumer filter, you can configure:\n * <pre>\n * &lt;dubbo:consumer filter=\"-sentinel.dubbo.consumer.filter\"/&gt;\n * </pre>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n * @author Lin Liang\n */",
        "/**\n * <p>Sentinel 集成的 Dubbo 消费者 Filter，默认自动激活。</p>\n * <p>\n * 如需禁用消费者 Filter，可配置：\n * <pre>\n * &lt;dubbo:consumer filter=\"-sentinel.dubbo.consumer.filter\"/&gt;\n * </pre>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n * @author Lin Liang\n */",
    ),
    (
        "    public SentinelDubboConsumerFilter() {",
        "    /** 初始化消费者 Filter 并记录日志。 */\n    public SentinelDubboConsumerFilter() {",
    ),
    (
        "    private Result syncInvoke(Invoker<?> invoker, Invocation invocation) {",
        "    /** 同步调用路径：对接口与方法资源分别 entry/exit。 */\n    private Result syncInvoke(Invoker<?> invoker, Invocation invocation) {",
    ),
    (
        "    private Result asyncInvoke(Invoker<?> invoker, Invocation invocation) {",
        "    /** 异步调用路径：使用 asyncEntry 并在 whenComplete 中 exit。 */\n    private Result asyncInvoke(Invoker<?> invoker, Invocation invocation) {",
    ),
    (
        "    static class EntryHolder {",
        "    /** 异步调用时暂存 Entry 与参数，供完成回调 exit。 */\n    static class EntryHolder {",
    ),
    (
        "    private void exitEntry(EntryHolder holder) {",
        "    /** 按是否携带参数退出 Entry。 */\n    private void exitEntry(EntryHolder holder) {",
    ),
]

R["SentinelDubboProviderFilter.java"] = [
    (
        "/**\n * <p>Apache Dubbo service provider filter that enables integration with Sentinel. Auto activated by default.</p>\n * <p>Note: this only works for Apache Dubbo 2.7.x or above version.</p>\n * <p>\n * If you want to disable the provider filter, you can configure:\n * <pre>\n * &lt;dubbo:provider filter=\"-sentinel.dubbo.provider.filter\"/&gt;\n * </pre>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
        "/**\n * <p>与 Sentinel 集成的 Apache Dubbo 提供者 Filter，默认自动激活。</p>\n * <p>注意：仅适用于 Apache Dubbo 2.7.x 及以上版本。</p>\n * <p>\n * 如需禁用提供者 Filter，可配置：\n * <pre>\n * &lt;dubbo:provider filter=\"-sentinel.dubbo.provider.filter\"/&gt;\n * </pre>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
    ),
    (
        "    public SentinelDubboProviderFilter() {",
        "    /** 初始化提供者 Filter 并记录日志。 */\n    public SentinelDubboProviderFilter() {",
    ),
    (
        "        // Get origin caller.",
        "        // 解析调用来源（origin）。",
    ),
    (
        "            // Only need to create entrance context at provider side, as context will take effect",
        "            // 仅在 Provider 端创建入口 Context，Context 仅对入站调用链入口生效",
    ),
    (
        "            // at entrance of invocation chain only (for inbound traffic).",
        "            // （针对入站流量）。",
    ),
]

R["DubboAdapterGlobalConfig.java"] = [
    (
        "/**\n * <p>\n * Responsible for dubbo service provider, consumer attribute configuration\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
        "/**\n * <p>\n * Dubbo 适配器全局配置，管理 Provider/Consumer 资源名前缀、降级与来源解析等。\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
    ),
    (
        "    public static final String DUBBO_RES_NAME_WITH_PREFIX_KEY = \"csp.sentinel.dubbo.resource.use.prefix\";",
        "    /** 是否在资源名中使用前缀的配置键。 */\n    public static final String DUBBO_RES_NAME_WITH_PREFIX_KEY = \"csp.sentinel.dubbo.resource.use.prefix\";",
    ),
    (
        "    public static final String DUBBO_PROVIDER_RES_NAME_PREFIX_KEY = \"csp.sentinel.dubbo.resource.provider.prefix\";",
        "    /** Provider 资源名前缀配置键。 */\n    public static final String DUBBO_PROVIDER_RES_NAME_PREFIX_KEY = \"csp.sentinel.dubbo.resource.provider.prefix\";",
    ),
    (
        "    public static final String DUBBO_CONSUMER_RES_NAME_PREFIX_KEY = \"csp.sentinel.dubbo.resource.consumer.prefix\";",
        "    /** Consumer 资源名前缀配置键。 */\n    public static final String DUBBO_CONSUMER_RES_NAME_PREFIX_KEY = \"csp.sentinel.dubbo.resource.consumer.prefix\";",
    ),
    (
        "    public static final String DUBBO_INTERFACE_GROUP_VERSION_ENABLED = \"csp.sentinel.dubbo.interface.group.version.enabled\";",
        "    /** 是否在资源名中包含接口 group 与 version 的配置键。 */\n    public static final String DUBBO_INTERFACE_GROUP_VERSION_ENABLED = \"csp.sentinel.dubbo.interface.group.version.enabled\";",
    ),
    (
        "    public static boolean isUsePrefix() {",
        "    /** 是否启用资源名前缀。 */\n    public static boolean isUsePrefix() {",
    ),
    (
        "    public static String getDubboProviderResNamePrefixKey() {",
        "    /** 获取 Provider 侧资源名前缀，未启用时返回 null。 */\n    public static String getDubboProviderResNamePrefixKey() {",
    ),
    (
        "    public static String getDubboConsumerResNamePrefixKey() {",
        "    /** 获取 Consumer 侧资源名前缀，未启用时返回 null。 */\n    public static String getDubboConsumerResNamePrefixKey() {",
    ),
    (
        "    public static Boolean getDubboInterfaceGroupAndVersionEnabled() {",
        "    /** 是否在资源名中包含 group 与 version。 */\n    public static Boolean getDubboInterfaceGroupAndVersionEnabled() {",
    ),
    (
        "    public static DubboFallback getConsumerFallback() {",
        "    /** 获取 Consumer 侧降级处理器。 */\n    public static DubboFallback getConsumerFallback() {",
    ),
    (
        "    public static void setConsumerFallback(DubboFallback consumerFallback) {",
        "    /** 设置 Consumer 侧降级处理器。 */\n    public static void setConsumerFallback(DubboFallback consumerFallback) {",
    ),
    (
        "    public static DubboFallback getProviderFallback() {",
        "    /** 获取 Provider 侧降级处理器。 */\n    public static DubboFallback getProviderFallback() {",
    ),
    (
        "    public static void setProviderFallback(DubboFallback providerFallback) {",
        "    /** 设置 Provider 侧降级处理器。 */\n    public static void setProviderFallback(DubboFallback providerFallback) {",
    ),
    (
        "    /**\n     * Get the origin parser of Dubbo adapter.\n     *\n     * @return the origin parser\n     * @since 1.8.0\n     */",
        "    /**\n     * 获取 Dubbo 适配器的来源解析器。\n     *\n     * @return the origin parser\n     * @since 1.8.0\n     */",
    ),
    (
        "    /**\n     * Set the origin parser of Dubbo adapter.\n     *\n     * @param originParser the origin parser\n     * @since 1.8.0\n     */",
        "    /**\n     * 设置 Dubbo 适配器的来源解析器。\n     *\n     * @param originParser the origin parser\n     * @since 1.8.0\n     */",
    ),
]

R["DefaultDubboFallback.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 默认降级实现，将 {@link BlockException} 包装为运行时异常返回。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Just wrap the exception.",
        "        // 将阻断异常包装为运行时异常。",
    ),
]

R["DubboFallback.java"] = [
    (
        "/**\n * Fallback handler for Dubbo services.\n *\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 服务被 Sentinel 阻断时的降级处理器。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Handle the block exception and provide fallback result.\n     *\n     * @param invoker Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @param ex block exception\n     * @return fallback result\n     */",
        "    /**\n     * 处理阻断异常并返回降级结果。\n     *\n     * @param invoker Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @param ex block exception\n     * @return fallback result\n     */",
    ),
]

R["DubboFallbackRegistry.java"] = [
    (
        "/**\n * <p>Global fallback registry for Dubbo.</p>\n *\n * @author Eric Zhao\n * @deprecated use {@link DubboAdapterGlobalConfig} instead since 1.8.0.\n */",
        "/**\n * <p>Dubbo 全局降级注册表（已废弃）。</p>\n *\n * @author Eric Zhao\n * @deprecated use {@link DubboAdapterGlobalConfig} instead since 1.8.0.\n */",
    ),
    (
        "    public static DubboFallback getConsumerFallback() {",
        "    /** 获取 Consumer 降级处理器（委托 {@link DubboAdapterGlobalConfig}）。 */\n    public static DubboFallback getConsumerFallback() {",
    ),
    (
        "    public static void setConsumerFallback(DubboFallback consumerFallback) {",
        "    /** 设置 Consumer 降级处理器。 */\n    public static void setConsumerFallback(DubboFallback consumerFallback) {",
    ),
    (
        "    public static DubboFallback getProviderFallback() {",
        "    /** 获取 Provider 降级处理器。 */\n    public static DubboFallback getProviderFallback() {",
    ),
    (
        "    public static void setProviderFallback(DubboFallback providerFallback) {",
        "    /** 设置 Provider 降级处理器。 */\n    public static void setProviderFallback(DubboFallback providerFallback) {",
    ),
]

R["DefaultDubboOriginParser.java"] = [
    (
        "/**\n * Default Dubbo origin parser.\n *\n * @author jingzian\n */",
        "/**\n * 默认 Dubbo 来源解析器，从 attachment 读取消费者应用名作为 origin。\n *\n * @author jingzian\n */",
    ),
]


def ensure_analyzed(rel: str) -> Path:
    dst = ANALYZED / rel
    if not dst.exists():
        src = ORIGINAL / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")
    return dst


def apply_replacements(rel: str) -> None:
    name = Path(rel).name
    path = ensure_analyzed(rel)
    text = path.read_text(encoding="utf-8")
    if len(re.findall(r"[\u4e00-\u9fff]", text)) >= 10:
        return
    for old, new in R.get(name, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    cn = len(re.findall(r"[\u4e00-\u9fff]", text))
    if cn < 10:
        raise SystemExit(f"Insufficient Chinese (cn={cn}) in {rel}")
    path.write_text(text, encoding="utf-8")


def update_batch_json() -> None:
    batch = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))
    remaining = [f for f in batch["files"] if f not in BATCH_LIST]
    batch["files"] = remaining
    batch["done"] = batch.get("done", 195) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 740) - len(BATCH_LIST)
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
            "wave7a util/dubbo [0:15]",
            *BATCH_LIST,
        ],
        check=True,
    )
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
