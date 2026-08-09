#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-9b block [15:30] (jax-rs + motan)."""
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
BATCH_LIST = Path("/tmp/sentinel_w9b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["SentinelJaxRsProviderFilter.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * JAX-RS 服务端 Provider 过滤器，在请求入口创建 Sentinel 资源并执行流控。\n *\n * @author sea\n */",
    ),
    (
        "                // Parse the request origin using registered origin parser.",
        "                // 使用已注册的来源解析器解析请求来源。",
    ),
]

R["SentinelJaxRsConfig.java"] = [
    (
        "/**\n *  @author sea\n */",
        "/**\n * JAX-RS 适配器全局配置，管理资源名解析器、来源解析器与降级处理器。\n *\n * @author sea\n */",
    ),
]

R["DefaultExceptionMapper.java"] = [
    (
        "/**\n * sentinel jax-rs adapter provide this exception mapper\n * in case of user throw exception which is not {@link javax.ws.rs.WebApplicationException} and not matched by any ExceptionMapper\n * this exception mapper convert exception to Response let ContainerResponseFilter to be called to exit sentinel entry\n * user can add custom ExceptionMapper and config with {@link javax.annotation.Priority} with lower value\n * @author sea\n */",
        "/**\n * Sentinel JAX-RS 适配器提供的异常映射器。\n * 当用户抛出非 {@link javax.ws.rs.WebApplicationException} 且未被其他 ExceptionMapper 捕获的异常时，\n * 本映射器将异常转换为 Response，以便触发 ContainerResponseFilter 退出 Sentinel Entry。\n * 用户可添加自定义 ExceptionMapper，并通过 {@link javax.annotation.Priority} 设置更小的优先级值。\n *\n * @author sea\n */",
    ),
]

R["DefaultSentinelJaxRsFallback.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * JAX-RS 适配器默认降级实现，流控触发时返回 HTTP 429 响应。\n *\n * @author sea\n */",
    ),
]

R["SentinelJaxRsFallback.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * JAX-RS 适配器降级处理器接口。\n *\n * @author sea\n */",
    ),
    (
        "    /**\n     * Provides a fallback response based on the cause of the failed execution.\n     *\n     * @param route The route the fallback is for\n     * @param cause cause of the main method failure, may be <code>null</code>\n     * @return the fallback response\n     */",
        "    /**\n     * 根据执行失败原因提供降级响应。\n     *\n     * @param route 降级对应的路由\n     * @param cause 主方法失败原因，可能为 <code>null</code>\n     * @return 降级响应\n     */",
    ),
    (
        "    /**\n     * Provides a fallback response future based on the cause of the failed execution.\n     *\n     * @param route The route the fallback is for\n     * @param cause cause of the main method failure, may be <code>null</code>\n     * @return the fallback response future\n     */",
        "    /**\n     * 根据执行失败原因提供异步降级响应 Future。\n     *\n     * @param route 降级对应的路由\n     * @param cause 主方法失败原因，可能为 <code>null</code>\n     * @return 降级响应 Future\n     */",
    ),
]

R["FutureWrapper.java"] = [
    (
        "/**\n * wrap Future to ensure entry exit\n * @author sea\n */",
        "/**\n * 包装 {@link Future}，确保 {@link AsyncEntry} 在异步调用完成或取消时正确退出。\n *\n * @author sea\n */",
    ),
]

R["DefaultRequestOriginParser.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * 默认请求来源解析器，始终返回空字符串。\n *\n * @author sea\n */",
    ),
]

R["DefaultResourceNameParser.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * 默认资源名解析器，格式为 HTTP 方法 + 类路径 + 方法路径。\n *\n * @author sea\n */",
    ),
]

R["RequestOriginParser.java"] = [
    (
        "/**\n * The origin parser parses request origin (e.g. IP, user, appName) from HTTP request.\n *\n * @author sea\n */",
        "/**\n * 请求来源解析器，从 HTTP 请求中解析来源标识（如 IP、用户、应用名）。\n *\n * @author sea\n */",
    ),
    (
        "    /**\n     * Parse the origin from given HTTP request.\n     *\n     * @param request HTTP request\n     * @return parsed origin\n     */",
        "    /**\n     * 从给定 HTTP 请求中解析来源标识。\n     *\n     * @param request HTTP 请求\n     * @return 解析出的来源标识\n     */",
    ),
]

R["ResourceNameParser.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * 资源名解析器，从 JAX-RS 请求上下文解析 Sentinel 资源名。\n *\n * @author sea\n */",
    ),
    (
        "    String parse(ContainerRequestContext containerRequestContext, ResourceInfo resourceInfo);",
        "    /**\n     * 解析 Sentinel 资源名。\n     *\n     * @param containerRequestContext 请求上下文\n     * @param resourceInfo 资源信息\n     * @return 资源名\n     */\n    String parse(ContainerRequestContext containerRequestContext, ResourceInfo resourceInfo);",
    ),
]

R["MotanUtils.java"] = [
    (
        "/**\n * @author zhangxn8\n */",
        "/**\n * Motan 适配器工具类，用于构建接口与方法级 Sentinel 资源名。\n *\n * @author zhangxn8\n */",
    ),
    (
        "    public static String getMethodResourceName(Caller<?> caller, Request request){\n        return getMethodResourceName(caller, request, false);\n    }",
        "    /**\n     * 获取 Motan RPC 方法资源名（不使用 group/version）。\n     *\n     * @param caller Motan 调用方\n     * @param request Motan 请求\n     * @return 方法资源名\n     */\n    public static String getMethodResourceName(Caller<?> caller, Request request){\n        return getMethodResourceName(caller, request, false);\n    }",
    ),
    (
        "    public static String getMethodResourceName(Caller<?> caller, Request request, Boolean useGroupAndVersion) {",
        "    /**\n     * 获取 Motan RPC 方法资源名。\n     *\n     * @param caller Motan 调用方\n     * @param request Motan 请求\n     * @param useGroupAndVersion 是否在接口名中使用 group 与 version\n     * @return 方法资源名\n     */\n    public static String getMethodResourceName(Caller<?> caller, Request request, Boolean useGroupAndVersion) {",
    ),
    (
        "    public static String getMethodResourceName(Caller<?> caller, Request request, String prefix) {",
        "    /**\n     * 获取带前缀的 Motan RPC 方法资源名。\n     *\n     * @param caller Motan 调用方\n     * @param request Motan 请求\n     * @param prefix 资源名前缀\n     * @return 方法资源名\n     */\n    public static String getMethodResourceName(Caller<?> caller, Request request, String prefix) {",
    ),
    (
        "    public static String getInterfaceName(Caller<?> caller) {\n        return getInterfaceName(caller, false);\n    }",
        "    /**\n     * 获取 Motan RPC 接口资源名（不使用 group/version）。\n     *\n     * @param caller Motan 调用方\n     * @return 接口资源名\n     */\n    public static String getInterfaceName(Caller<?> caller) {\n        return getInterfaceName(caller, false);\n    }",
    ),
    (
        "    public static String getInterfaceName(Caller<?> caller, Boolean useGroupAndVersion) {",
        "    /**\n     * 获取 Motan RPC 接口资源名。\n     *\n     * @param caller Motan 调用方\n     * @param useGroupAndVersion 是否在接口名中使用 group 与 version\n     * @return 接口资源名\n     */\n    public static String getInterfaceName(Caller<?> caller, Boolean useGroupAndVersion) {",
    ),
    (
        "    public static String getInterfaceName(Caller<?> caller, String prefix) {",
        "    /**\n     * 获取带前缀的 Motan RPC 接口资源名。\n     *\n     * @param caller Motan 调用方\n     * @param prefix 资源名前缀\n     * @return 接口资源名\n     */\n    public static String getInterfaceName(Caller<?> caller, String prefix) {",
    ),
]

R["SentinelMotanConsumerFilter.java"] = [
    (
        "/**\n * @author zhangxn8\n */",
        "/**\n * Sentinel 集成的 Motan 服务 Consumer 过滤器，对出站 RPC 调用执行流控。\n *\n * @author zhangxn8\n */",
    ),
]

R["SentinelMotanProviderFilter.java"] = [
    (
        "/**\n * @author zhangxn8\n */",
        "/**\n * Sentinel 集成的 Motan 服务 Provider 过滤器，对入站 RPC 调用执行流控。\n *\n * @author zhangxn8\n */",
    ),
]

R["MotanAdapterGlobalConfig.java"] = [
    (
        "/**\n * @author zhangxn8\n */",
        "/**\n * Motan 适配器全局配置，管理资源名前缀、接口 group/version 开关与降级处理器。\n *\n * @author zhangxn8\n */",
    ),
    (
        "    public static boolean isUsePrefix() {",
        "    /**\n     * 是否启用资源名前缀。\n     *\n     * @return 启用时返回 true\n     */\n    public static boolean isUsePrefix() {",
    ),
    (
        "    public static String getMotanProviderPrefix() {",
        "    /**\n     * 获取 Provider 侧资源名前缀。\n     *\n     * @return 前缀字符串，未启用时返回 null\n     */\n    public static String getMotanProviderPrefix() {",
    ),
    (
        "    public static String getMotanConsumerPrefix() {",
        "    /**\n     * 获取 Consumer 侧资源名前缀。\n     *\n     * @return 前缀字符串，未启用时返回 null\n     */\n    public static String getMotanConsumerPrefix() {",
    ),
    (
        "    public static Boolean getMotanInterfaceGroupAndVersionEnabled() {",
        "    /**\n     * 是否在接口资源名中使用 group 与 version。\n     *\n     * @return 启用时返回 true\n     */\n    public static Boolean getMotanInterfaceGroupAndVersionEnabled() {",
    ),
    (
        "    public static MotanFallback getConsumerFallback() {",
        "    /**\n     * 获取 Consumer 侧降级处理器。\n     *\n     * @return Consumer 降级处理器\n     */\n    public static MotanFallback getConsumerFallback() {",
    ),
    (
        "    public static void setConsumerFallback(MotanFallback consumerFallback) {",
        "    /**\n     * 设置 Consumer 侧降级处理器。\n     *\n     * @param consumerFallback Consumer 降级处理器\n     */\n    public static void setConsumerFallback(MotanFallback consumerFallback) {",
    ),
    (
        "    public static MotanFallback getProviderFallback() {",
        "    /**\n     * 获取 Provider 侧降级处理器。\n     *\n     * @return Provider 降级处理器\n     */\n    public static MotanFallback getProviderFallback() {",
    ),
    (
        "    public static void setProviderFallback(MotanFallback providerFallback) {",
        "    /**\n     * 设置 Provider 侧降级处理器。\n     *\n     * @param providerFallback Provider 降级处理器\n     */\n    public static void setProviderFallback(MotanFallback providerFallback) {",
    ),
]

R["DefaultMotanFallback.java"] = [
    (
        "/**\n * @author zhangxn8\n */",
        "/**\n * Motan 适配器默认降级实现，将 {@link BlockException} 包装为运行时异常返回。\n *\n * @author zhangxn8\n */",
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
    batch["done"] = batch.get("done", 240) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 695) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave9b jaxrs/motan [15:30]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
