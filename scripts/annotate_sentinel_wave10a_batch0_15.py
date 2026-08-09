#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-10a block [0:15] (motan/okhttp/quarkus/reactor)."""
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
BATCH_LIST = Path("/tmp/sentinel_w10a.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["MotanFallback.java"] = [
    (
        "/**\n * @author zhangxn8\n */",
        "/**\n * Motan 适配器降级处理器接口，流控触发时由过滤器回调。\n *\n * @author zhangxn8\n */",
    ),
    (
        "    /**\n     * Handle the block exception and provide fallback result.\n     * @param caller\n     * @param request\n     * @param ex\n     * @return\n     */",
        "    /**\n     * 处理流控异常并返回降级结果。\n     *\n     * @param caller Motan 调用方\n     * @param request RPC 请求\n     * @param ex 流控异常\n     * @return 降级响应\n     */",
    ),
]

R["SentinelOkHttpConfig.java"] = [
    (
        "/**\n * @author zhaoyuguang\n * @author Eric Zhao\n */",
        "/**\n * OkHttp 适配器配置，管理资源名前缀、资源提取器与降级处理器。\n *\n * @author zhaoyuguang\n * @author Eric Zhao\n */",
    ),
]

R["SentinelOkHttpInterceptor.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * OkHttp 出站拦截器，在 HTTP 请求发出前创建 Sentinel 资源并执行流控。\n *\n * @author zhaoyuguang\n */",
    ),
]

R["DefaultOkHttpResourceExtractor.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * 默认 OkHttp 资源名提取器，格式为 {@code method:url}。\n *\n * @author zhaoyuguang\n */",
    ),
]

R["OkHttpResourceExtractor.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * OkHttp 资源名提取器接口。\n *\n * @author zhaoyuguang\n */",
    ),
    (
        "    /**\n     * Extracts the resource name from the HTTP request.\n     *\n     * @param request    HTTP request entity\n     * @param connection HTTP connection\n     * @return the resource name of current request\n     */",
        "    /**\n     * 从 HTTP 请求中提取 Sentinel 资源名。\n     *\n     * @param request    HTTP 请求实体\n     * @param connection HTTP 连接\n     * @return 当前请求的资源名\n     */",
    ),
]

R["DefaultOkHttpFallback.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * OkHttp 适配器默认降级实现，将 {@link BlockException} 包装为 {@link SentinelRpcException} 抛出。\n *\n * @author zhaoyuguang\n */",
    ),
    (
        "        // Just wrap and throw the exception.",
        "        // 直接包装并抛出异常。",
    ),
]

R["OkHttpFallback.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * OkHttp 适配器降级处理器接口。\n *\n * @author zhaoyuguang\n */",
    ),
    (
        "    Response handle(Request request, Connection connection, BlockException e);",
        "    /**\n     * 流控触发时的降级处理。\n     *\n     * @param request    HTTP 请求\n     * @param connection HTTP 连接\n     * @param e          流控异常\n     * @return 降级响应\n     */\n    Response handle(Request request, Connection connection, BlockException e);",
    ),
]

R["SentinelAnnotationQuarkusAdapterProcessor.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * Quarkus 部署阶段处理器，注册 Sentinel 注解适配器特性与 CDI 拦截器 Bean。\n *\n * @author sea\n */",
    ),
]

R["SentinelJaxRsQuarkusAdapterProcessor.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * Quarkus 部署阶段处理器，注册 Sentinel JAX-RS 适配器特性。\n *\n * @author sea\n */",
    ),
]

R["SentinelNativeImageProcessor.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * Quarkus Native Image 部署处理器，配置 Sentinel 在 GraalVM 原生镜像下的反射与运行时初始化类。\n *\n * @author sea\n */",
    ),
]

R["SentinelRecorder.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * Quarkus 运行时 Recorder，在静态初始化阶段预热 Fastjson 序列化/反序列化配置。\n *\n * @author sea\n */",
    ),
    (
        "    /**\n     * register fastjson serializer deserializer class info\n     */",
        "    /**\n     * 注册 Fastjson 序列化与反序列化所需的类元信息，供 Native Image 使用。\n     */",
    ),
]

R["ContextConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Reactor 适配器 Sentinel 上下文配置，包含 contextName 与 origin。\n *\n * @author Eric Zhao\n */",
    ),
]

R["EntryConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * Reactor 适配器 Sentinel Entry 配置，描述资源名、Entry 类型、许可数与上下文等参数。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
    (
        "        // Constructed ContextConfig should be valid here. Null is allowed here.",
        "        // 此处传入的 ContextConfig 应已校验有效；允许为 null。",
    ),
]

R["FluxSentinelOperator.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * 为 {@link Flux} 提供 Sentinel 流控的算子，订阅时委托 {@link SentinelReactorSubscriber} 执行 Entry 生命周期管理。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
]

R["InheritableBaseSubscriber.java"] = [
    (
        "/**\n * <p>\n * Copied from {@link reactor.core.publisher.BaseSubscriber} of reactor-core,\n * but allow sub-classes to override {@code onSubscribe}, {@code onNext},\n * {@code onError} and {@code onComplete} method for customization.\n * </p>\n * <p>This base subscriber also provides predicate for {@code onErrorDropped} hook as a workaround for Sentinel.</p>\n */",
        "/**\n * <p>\n * 自 reactor-core 的 {@link reactor.core.publisher.BaseSubscriber} 复制而来，\n * 允许子类重写 {@code onSubscribe}、{@code onNext}、{@code onError} 与 {@code onComplete} 以定制行为。\n * </p>\n * <p>该基类还为 {@code onErrorDropped} 钩子提供谓词，作为 Sentinel 流控场景的变通方案。</p>\n */",
    ),
    (
        "    /**\n     * Return current {@link Subscription}\n     *\n     * @return current {@link Subscription}\n     */",
        "    /**\n     * 返回当前上游 {@link Subscription}。\n     *\n     * @return 当前 {@link Subscription}\n     */",
    ),
    (
        "    /**\n     * {@link Disposable#dispose() Dispose} the {@link Subscription} by\n     * {@link Subscription#cancel() cancelling} it.\n     */",
        "    /**\n     * 通过 {@link Subscription#cancel()} 取消订阅，等效于 {@link Disposable#dispose()}。\n     */",
    ),
    (
        "    /**\n     * Hook for further processing of onSubscribe's Subscription. Implement this method\n     * to call {@link #request(long)} as an initial request. Values other than the\n     * unbounded {@code Long.MAX_VALUE} imply that you'll also call request in\n     * {@link #hookOnNext(Object)}.\n     * <p> Defaults to request unbounded Long.MAX_VALUE as in {@link #requestUnbounded()}\n     *\n     * @param subscription the subscription to optionally process\n     */",
        "    /**\n     * {@code onSubscribe} 时进一步处理上游 {@link Subscription} 的钩子。\n     * 可在此调用 {@link #request(long)} 作为初始请求；若初始请求不是无界的 {@code Long.MAX_VALUE}，\n     * 则通常还需在 {@link #hookOnNext(Object)} 中继续 request。\n     * <p>默认行为同 {@link #requestUnbounded()}，请求无界 {@code Long.MAX_VALUE}。\n     *\n     * @param subscription 待处理的上游订阅\n     */",
    ),
    (
        "    /**\n     * Hook for processing of onNext values. You can call {@link #request(long)} here\n     * to further request data from the source {@code org.reactivestreams.Publisher} if\n     * the {@link #hookOnSubscribe(Subscription) initial request} wasn't unbounded.\n     * <p>Defaults to doing nothing.\n     *\n     * @param value the emitted value to process\n     */",
        "    /**\n     * 处理 {@code onNext} 值的钩子。若 {@link #hookOnSubscribe(Subscription) 初始请求} 非无界，\n     * 可在此调用 {@link #request(long)} 向上游 {@code org.reactivestreams.Publisher} 继续拉取数据。\n     * <p>默认为空实现。\n     *\n     * @param value 下游收到的元素\n     */",
    ),
    (
        "    /**\n     * Optional hook for completion processing. Defaults to doing nothing.\n     */",
        "    /**\n     * 可选的完成处理钩子，默认为空实现。\n     */",
    ),
    (
        "    /**\n     * Optional hook for error processing. Default is to call\n     * {@link Exceptions#errorCallbackNotImplemented(Throwable)}.\n     *\n     * @param throwable the error to process\n     */",
        "    /**\n     * 可选的错误处理钩子，默认调用 {@link Exceptions#errorCallbackNotImplemented(Throwable)}。\n     *\n     * @param throwable 待处理的错误\n     */",
    ),
    (
        "    /**\n     * Optional hook executed when the subscription is cancelled by calling this\n     * Subscriber's {@link #cancel()} method. Defaults to doing nothing.\n     */",
        "    /**\n     * 调用本 Subscriber 的 {@link #cancel()} 取消订阅时执行的可选钩子，默认为空实现。\n     */",
    ),
    (
        "    /**\n     * Optional hook executed after any of the termination events (onError, onComplete,\n     * cancel). The hook is executed in addition to and after {@link #hookOnError(Throwable)},\n     * {@link #hookOnComplete()} and {@link #hookOnCancel()} hooks, even if these callbacks\n     * fail. Defaults to doing nothing. A failure of the callback will be caught by\n     * {@code Operators#onErrorDropped(Throwable, reactor.util.context.Context)}.\n     *\n     * @param type the type of termination event that triggered the hook\n     *             ({@link SignalType#ON_ERROR}, {@link SignalType#ON_COMPLETE} or\n     *             {@link SignalType#CANCEL})\n     */",
        "    /**\n     * 任意终止事件（onError、onComplete、cancel）之后执行的可选钩子。\n     * 该钩子在 {@link #hookOnError(Throwable)}、{@link #hookOnComplete()} 与 {@link #hookOnCancel()} 之后执行，\n     * 即使上述回调失败也会调用。默认为空实现；若钩子自身失败，将由\n     * {@code Operators#onErrorDropped(Throwable, reactor.util.context.Context)} 捕获。\n     *\n     * @param type 触发该钩子的终止事件类型\n     *             （{@link SignalType#ON_ERROR}、{@link SignalType#ON_COMPLETE} 或 {@link SignalType#CANCEL}）\n     */",
    ),
    (
        "        // NO-OP",
        "        // 空操作",
    ),
    (
        "        //NO-OP",
        "        // 空操作",
    ),
    (
        "    protected void hookOnComplete() {\n        // NO-OP\n    }",
        "    protected void hookOnComplete() {\n        // 空操作\n    }",
    ),
    (
        "    protected void hookFinally(SignalType type) {\n        //NO-OP\n    }",
        "    protected void hookFinally(SignalType type) {\n        // 空操作\n    }",
    ),
    (
        "            // Already cancelled concurrently",
        "            // 订阅已被并发取消",
    ),
    (
        "            // Workaround for Sentinel BlockException:\n            // Here we add a predicate method to decide whether exception should be dropped implicitly\n            // or call the {@code onErrorDropped} hook.",
        "            // Sentinel BlockException 变通处理：\n            // 通过谓词方法决定异常是被静默丢弃，还是调用 {@code onErrorDropped} 钩子。",
    ),
    (
        "            //we're sure it has not been concurrently cancelled",
        "            // 确认未被并发取消",
    ),
    (
        "                //onError itself will short-circuit due to the CancelledSubscription being push above",
        "                // 上方已置为 CancelledSubscription，hookOnError 将短路",
    ),
    (
        "    /**\n     * {@link #request(long) Request} an unbounded amount.\n     */",
        "    /**\n     * 以无界方式 {@link #request(long) 请求} 上游数据。\n     */",
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
    batch["done"] = batch.get("done", 270) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 665) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave10a motan/okhttp/quarkus/reactor [0:15]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
