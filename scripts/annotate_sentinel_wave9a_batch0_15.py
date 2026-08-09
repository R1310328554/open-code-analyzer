#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-9a gateway/dubbo/grpc/jaxrs [0:15]."""
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
BATCH_LIST = Path("/tmp/sentinel_w9a.txt").read_text(encoding="utf-8").strip().split("\n")[:15]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-adapter/sentinel-api-gateway-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/common/slot/GatewaySlotChainBuilder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.1\n *\n * @deprecated since 1.7.2, we can use @Spi(order = -4000) to adjust the order of {@link GatewayFlowSlot},\n * this class is reserved for compatibility with older versions.\n *\n * @see GatewayFlowSlot\n * @see DefaultSlotChainBuilder\n */",
        "/**\n * 网关 Slot 链构建器，继承 {@link DefaultSlotChainBuilder}。\n *\n * @author Eric Zhao\n * @since 1.6.1\n *\n * @deprecated since 1.7.2, we can use @Spi(order = -4000) to adjust the order of {@link GatewayFlowSlot},\n * 自 1.7.2 起已废弃，可通过 @Spi(order = -4000) 调整 {@link GatewayFlowSlot} 顺序；\n * 保留此类仅为兼容旧版本。\n *\n * @see GatewayFlowSlot\n * @see DefaultSlotChainBuilder\n */",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/AbstractDubboFilter.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * Dubbo 旧版适配器 Filter 抽象基类，提供接口与方法级 Sentinel 资源名构建。\n *\n * @author leyou\n */",
    ),
    (
        "    protected String getMethodResourceName(Invoker<?> invoker, Invocation invocation) {",
        "    /** 构建方法级资源名：接口名:方法名(参数类型列表)。 */\n    protected String getMethodResourceName(Invoker<?> invoker, Invocation invocation) {",
    ),
    (
        "    protected String getMethodResourceName(Invoker<?> invoker, Invocation invocation, String prefix) {",
        "    /** 构建带前缀的方法级资源名。 */\n    protected String getMethodResourceName(Invoker<?> invoker, Invocation invocation, String prefix) {",
    ),
    (
        "    protected String getInterfaceName(Invoker<?> invoker) {",
        "    /** 获取 Dubbo 接口全限定名作为资源名。 */\n    protected String getInterfaceName(Invoker<?> invoker) {",
    ),
    (
        "    protected String getInterfaceName(Invoker<?> invoker, String prefix) {",
        "    /** 获取带前缀的接口级资源名。 */\n    protected String getInterfaceName(Invoker<?> invoker, String prefix) {",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/DubboAdapterGlobalConfig.java"] = [
    (
        "/**\n * <p>Global config and callback registry of Dubbo legacy adapter.</p>\n *\n * @author lianglin\n * @author Eric Zhao\n * @since 1.7.0\n */",
        "/**\n * <p>Dubbo 旧版适配器的全局配置与回调注册中心。</p>\n * <p>管理资源名前缀、消费者/提供者降级处理器及调用来源解析器。</p>\n *\n * @author lianglin\n * @author Eric Zhao\n * @since 1.7.0\n */",
    ),
    (
        "    public static final String DUBBO_RES_NAME_WITH_PREFIX_KEY = \"csp.sentinel.dubbo.resource.use.prefix\";",
        "    /** 是否在资源名中使用前缀的配置键。 */\n    public static final String DUBBO_RES_NAME_WITH_PREFIX_KEY = \"csp.sentinel.dubbo.resource.use.prefix\";",
    ),
    (
        "    public static final String DUBBO_PROVIDER_RES_NAME_PREFIX_KEY = \"csp.sentinel.dubbo.resource.provider.prefix\";",
        "    /** Provider 端资源名前缀配置键。 */\n    public static final String DUBBO_PROVIDER_RES_NAME_PREFIX_KEY = \"csp.sentinel.dubbo.resource.provider.prefix\";",
    ),
    (
        "    public static final String DUBBO_CONSUMER_RES_NAME_PREFIX_KEY = \"csp.sentinel.dubbo.resource.consumer.prefix\";",
        "    /** Consumer 端资源名前缀配置键。 */\n    public static final String DUBBO_CONSUMER_RES_NAME_PREFIX_KEY = \"csp.sentinel.dubbo.resource.consumer.prefix\";",
    ),
    (
        "    public static boolean isUsePrefix() {",
        "    /** 是否启用资源名前缀。 */\n    public static boolean isUsePrefix() {",
    ),
    (
        "    public static String getDubboProviderPrefix() {",
        "    /** 获取 Provider 端资源名前缀，未启用时返回 null。 */\n    public static String getDubboProviderPrefix() {",
    ),
    (
        "    public static String getDubboConsumerPrefix() {",
        "    /** 获取 Consumer 端资源名前缀，未启用时返回 null。 */\n    public static String getDubboConsumerPrefix() {",
    ),
    (
        "    public static DubboFallback getConsumerFallback() {",
        "    /** 获取消费者端降级处理器。 */\n    public static DubboFallback getConsumerFallback() {",
    ),
    (
        "    public static void setConsumerFallback(DubboFallback consumerFallback) {",
        "    /** 设置消费者端降级处理器。 */\n    public static void setConsumerFallback(DubboFallback consumerFallback) {",
    ),
    (
        "    public static DubboFallback getProviderFallback() {",
        "    /** 获取提供者端降级处理器。 */\n    public static DubboFallback getProviderFallback() {",
    ),
    (
        "    public static void setProviderFallback(DubboFallback providerFallback) {",
        "    /** 设置提供者端降级处理器。 */\n    public static void setProviderFallback(DubboFallback providerFallback) {",
    ),
    (
        "    /**\n     * Get the origin parser of Dubbo adapter.\n     *\n     * @return the origin parser\n     * @since 1.8.0\n     */",
        "    /**\n     * 获取 Dubbo 适配器的调用来源解析器。\n     *\n     * @return the origin parser\n     * @since 1.8.0\n     */",
    ),
    (
        "    /**\n     * Set the origin parser of Dubbo adapter.\n     *\n     * @param originParser the origin parser\n     * @since 1.8.0\n     */",
        "    /**\n     * 设置 Dubbo 适配器的调用来源解析器。\n     *\n     * @param originParser the origin parser\n     * @since 1.8.0\n     */",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/DubboAppContextFilter.java"] = [
    (
        "/**\n * Puts current consumer's application name in the attachment of each invocation.\n *\n * @author Eric Zhao\n */",
        "/**\n * 将当前消费者的应用名写入每次调用的 attachment，供 Provider 端解析调用来源。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Override\n    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {",
        "    /** 在调用前将消费者应用名写入 RpcContext attachment。 */\n    @Override\n    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/DubboUtils.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 旧版适配器工具类，用于读取调用方应用名。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public static final String DUBBO_APPLICATION_KEY = \"dubboApplication\";",
        "    /** Dubbo attachment 中传递调用方应用名的键。 */\n    public static final String DUBBO_APPLICATION_KEY = \"dubboApplication\";",
    ),
    (
        "    public static String getApplication(Invocation invocation, String defaultValue) {",
        "    /** 从 invocation attachment 读取调用方应用名。 */\n    public static String getApplication(Invocation invocation, String defaultValue) {",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/SentinelDubboConsumerFilter.java"] = [
    (
        "/**\n * <p>Dubbo service consumer filter for Sentinel. Auto activated by default.</p>\n *\n * If you want to disable the consumer filter, you can configure:\n * <pre>\n * &lt;dubbo:consumer filter=\"-sentinel.dubbo.consumer.filter\"/&gt;\n * </pre>\n *\n * @author leyou\n * @author Eric Zhao\n */",
        "/**\n * <p>Sentinel 集成的 Dubbo 消费者 Filter，默认自动激活。</p>\n * <p>对接口与方法资源分别进行流控，出站流量标记为 {@link EntryType#OUT}。</p>\n * <p>\n * 如需禁用消费者 Filter，可配置：\n * <pre>\n * &lt;dubbo:consumer filter=\"-sentinel.dubbo.consumer.filter\"/&gt;\n * </pre>\n *\n * @author leyou\n * @author Eric Zhao\n */",
    ),
    (
        "    public SentinelDubboConsumerFilter() {",
        "    /** 初始化消费者 Filter 并记录日志。 */\n    public SentinelDubboConsumerFilter() {",
    ),
    (
        "    @Override\n    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {",
        "    /** 对接口与方法资源 entry/exit，阻断时调用消费者降级处理器。 */\n    @Override\n    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {",
    ),
    (
        "                // Record common exception.",
        "                // 记录业务异常到 Sentinel 统计。",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/SentinelDubboProviderFilter.java"] = [
    (
        "/**\n * <p>Dubbo service provider filter for Sentinel. Auto activated by default.</p>\n *\n * If you want to disable the provider filter, you can configure:\n * <pre>\n * &lt;dubbo:provider filter=\"-sentinel.dubbo.provider.filter\"/&gt;\n * </pre>\n *\n * @author leyou\n * @author Eric Zhao\n */",
        "/**\n * <p>Sentinel 集成的 Dubbo 提供者 Filter，默认自动激活。</p>\n * <p>解析调用来源并创建入口 Context，入站流量标记为 {@link EntryType#IN}。</p>\n * <p>\n * 如需禁用提供者 Filter，可配置：\n * <pre>\n * &lt;dubbo:provider filter=\"-sentinel.dubbo.provider.filter\"/&gt;\n * </pre>\n *\n * @author leyou\n * @author Eric Zhao\n */",
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
        "                // Record common exception.",
        "                // 记录业务异常到 Sentinel 统计。",
    ),
    (
        "    @Override\n    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {",
        "    /** 解析 origin、创建 Context，对接口与方法资源 entry/exit。 */\n    @Override\n    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/fallback/DefaultDubboFallback.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 默认降级实现，将 {@link BlockException} 包装为 {@link SentinelRpcException} 并写入 Result。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Just wrap the exception. edit by wzg923 2020/9/23",
        "        // 将阻断异常包装为 SentinelRpcException 并设置到 Result。",
    ),
    (
        "    @Override\n    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {",
        "    /** 包装阻断异常并返回带异常的 RpcResult。 */\n    @Override\n    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/fallback/DubboFallback.java"] = [
    (
        "/**\n * Fallback handler for Dubbo services.\n *\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 服务被 Sentinel 阻断时的降级处理器接口。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Handle the block exception and provide fallback result.\n     *\n     * @param invoker Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @param ex block exception\n     * @return fallback result\n     */",
        "    /**\n     * 处理阻断异常并返回降级结果。\n     *\n     * @param invoker Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @param ex block exception\n     * @return fallback result\n     */",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/fallback/DubboFallbackRegistry.java"] = [
    (
        "/**\n * <p>Global fallback registry for Dubbo.</p>\n *\n * @author Eric Zhao\n * @deprecated use {@link DubboAdapterGlobalConfig} instead.\n */",
        "/**\n * <p>Dubbo 全局降级处理器注册表（已废弃）。</p>\n * <p>委托 {@link DubboAdapterGlobalConfig} 读写消费者/提供者降级处理器。</p>\n *\n * @author Eric Zhao\n * @deprecated use {@link DubboAdapterGlobalConfig} instead.\n */",
    ),
    (
        "    public static DubboFallback getConsumerFallback() {",
        "    /** 获取消费者端降级处理器。 */\n    public static DubboFallback getConsumerFallback() {",
    ),
    (
        "    public static void setConsumerFallback(DubboFallback consumerFallback) {",
        "    /** 设置消费者端降级处理器。 */\n    public static void setConsumerFallback(DubboFallback consumerFallback) {",
    ),
    (
        "    public static DubboFallback getProviderFallback() {",
        "    /** 获取提供者端降级处理器。 */\n    public static DubboFallback getProviderFallback() {",
    ),
    (
        "    public static void setProviderFallback(DubboFallback providerFallback) {",
        "    /** 设置提供者端降级处理器。 */\n    public static void setProviderFallback(DubboFallback providerFallback) {",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/origin/DefaultDubboOriginParser.java"] = [
    (
        "/**\n * Default Dubbo origin parser.\n *\n * @author tiecheng\n * @since 1.8.0\n */",
        "/**\n * Dubbo 默认调用来源解析器，从 invocation attachment 读取消费者应用名。\n *\n * @author tiecheng\n * @since 1.8.0\n */",
    ),
    (
        "    @Override\n    public String parse(Invoker<?> invoker, Invocation invocation) {",
        "    /** 从 attachment 读取 dubboApplication 作为 origin。 */\n    @Override\n    public String parse(Invoker<?> invoker, Invocation invocation) {",
    ),
]

R["sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/origin/DubboOriginParser.java"] = [
    (
        "/**\n * Customized origin parser for Dubbo provider filter.\n *\n * @author tiecheng\n * @since 1.8.0\n */",
        "/**\n * Dubbo Provider Filter 的自定义调用来源解析器接口。\n *\n * @author tiecheng\n * @since 1.8.0\n */",
    ),
    (
        "    /**\n     * Parses the origin (caller) from Dubbo invocation.\n     *\n     * @param invoker    Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @return the parsed origin\n     */",
        "    /**\n     * 从 Dubbo 调用中解析调用来源（caller）。\n     *\n     * @param invoker    Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @return the parsed origin\n     */",
    ),
]

R["sentinel-adapter/sentinel-grpc-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/grpc/SentinelGrpcClientInterceptor.java"] = [
    (
        "/**\n * <p>gRPC client interceptor for Sentinel. Currently it only works with unary methods.</p>\n * <p>\n * Example code:\n * <pre>\n * public class ServiceClient {\n *\n *     private final ManagedChannel channel;\n *\n *     ServiceClient(String host, int port) {\n *         this.channel = ManagedChannelBuilder.forAddress(host, port)\n *             .intercept(new SentinelGrpcClientInterceptor()) // Add the client interceptor.\n *             .build();\n *         // Init your stub here.\n *     }\n *\n * }\n * </pre>\n * <p>\n * For server interceptor, see {@link SentinelGrpcServerInterceptor}.\n *\n * @author Eric Zhao\n */",
        "/**\n * <p>Sentinel 集成的 gRPC 客户端拦截器，目前仅支持 unary 方法。</p>\n * <p>\n * Example code:\n * <pre>\n * public class ServiceClient {\n *\n *     private final ManagedChannel channel;\n *\n *     ServiceClient(String host, int port) {\n *         this.channel = ManagedChannelBuilder.forAddress(host, port)\n *             .intercept(new SentinelGrpcClientInterceptor()) // Add the client interceptor.\n *             .build();\n *         // Init your stub here.\n *     }\n *\n * }\n * </pre>\n * <p>\n * 服务端拦截器见 {@link SentinelGrpcServerInterceptor}。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "            // Allow access, forward the call.",
        "            // 通过流控检查，转发调用。",
    ),
    (
        "                                // Record the exception metrics.",
        "                                // 记录异常指标。",
    ),
    (
        "                /**\n                 * Some Exceptions will only call cancel.\n                 */",
        "                /**\n                 * 部分异常只会触发 cancel 而不会调用 onClose。\n                 */",
    ),
    (
        "                    // Some Exceptions will call onClose and cancel.",
        "                    // 部分异常会同时触发 onClose 与 cancel。",
    ),
    (
        "                        // Record the exception metrics.",
        "                        // 记录异常指标。",
    ),
    (
        "            // Flow control threshold exceeded, block the call.",
        "            // 超过流控阈值，阻断调用。",
    ),
    (
        "            // Catch the RuntimeException newCall throws, entry is guaranteed to exit.",
        "            // 捕获 newCall 抛出的 RuntimeException，确保 entry 退出。",
    ),
    (
        "    @Override\n    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,\n                                                               CallOptions callOptions, Channel channel) {",
        "    /** 以 gRPC 全方法名作为资源名进行 asyncEntry，在 onClose/cancel 时 exit。 */\n    @Override\n    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,\n                                                               CallOptions callOptions, Channel channel) {",
    ),
]

R["sentinel-adapter/sentinel-grpc-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/grpc/SentinelGrpcServerInterceptor.java"] = [
    (
        "/**\n * <p>gRPC server interceptor for Sentinel. Currently it only works with unary methods.</p>\n * <p>\n * Example code:\n * <pre>\n * Server server = ServerBuilder.forPort(port)\n *      .addService(new MyServiceImpl()) // Add your service.\n *      .intercept(new SentinelGrpcServerInterceptor()) // Add the server interceptor.\n *      .build();\n * </pre>\n * <p>\n * For client interceptor, see {@link SentinelGrpcClientInterceptor}.\n *\n * @author Eric Zhao\n */",
        "/**\n * <p>Sentinel 集成的 gRPC 服务端拦截器，目前仅支持 unary 方法。</p>\n * <p>\n * Example code:\n * <pre>\n * Server server = ServerBuilder.forPort(port)\n *      .addService(new MyServiceImpl()) // Add your service.\n *      .intercept(new SentinelGrpcServerInterceptor()) // Add the server interceptor.\n *      .build();\n * </pre>\n * <p>\n * 客户端拦截器见 {@link SentinelGrpcClientInterceptor}。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Remote address: serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);",
        "        // 远程地址：serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);",
    ),
    (
        "            // Allow access, forward the call.",
        "            // 通过流控检查，转发调用。",
    ),
    (
        "                                        // Record the exception metrics.",
        "                                        // 记录异常指标。",
    ),
    (
        "                                        //entry exit when the call be closed",
        "                                        // 调用关闭时 exit entry",
    ),
    (
        "                /**\n                 * If call was canceled, onCancel will be called. and the close will not be called\n                 * so the server is encouraged to abort processing to save resources by onCancel\n                 * @see ServerCall.Listener#onCancel()\n                 */",
        "                /**\n                 * 调用被取消时会触发 onCancel 而非 close，服务端应在此中止处理以节省资源。\n                 * @see ServerCall.Listener#onCancel()\n                 */",
    ),
    (
        "            // Catch the RuntimeException startCall throws, entry is guaranteed to exit.",
        "            // 捕获 startCall 抛出的 RuntimeException，确保 entry 退出。",
    ),
    (
        "    @Override\n    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {",
        "    /** 以 gRPC 全方法名作为资源名进行 asyncEntry，在 close/onCancel 时 exit。 */\n    @Override\n    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {",
    ),
]

R["sentinel-adapter/sentinel-jax-rs-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/jaxrs/SentinelJaxRsClientTemplate.java"] = [
    (
        "/**\n * wrap jax-rs client execution with sentinel\n * <pre>\n *         Response response = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {\n *\n *             @Override\n *             public Response get() {\n *                 return client.target(host).path(url).request()\n *                         .get();\n *             }\n *         });\n * </pre>\n * @author sea\n */",
        "/**\n * 使用 Sentinel 包装 JAX-RS 客户端调用的模板类。\n * <pre>\n *         Response response = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {\n *\n *             @Override\n *             public Response get() {\n *                 return client.target(host).path(url).request()\n *                         .get();\n *             }\n *         });\n * </pre>\n * @author sea\n */",
    ),
    (
        "    /**\n     * execute supplier with sentinel\n     * @param resourceName\n     * @param supplier\n     * @return\n     */\n    public static Response execute(String resourceName, Supplier<Response> supplier) {",
        "    /**\n     * 同步执行 JAX-RS 客户端调用，以 Sentinel 保护指定资源。\n     * @param resourceName\n     * @param supplier\n     * @return\n     */\n    public static Response execute(String resourceName, Supplier<Response> supplier) {",
    ),
    (
        "    /**\n     * execute supplier with sentinel\n     * @param resourceName\n     * @param supplier\n     * @return\n     */\n    public static Future<Response> executeAsync(String resourceName, Supplier<Future<Response>> supplier) {",
        "    /**\n     * 异步执行 JAX-RS 客户端调用，以 Sentinel 保护指定资源。\n     * @param resourceName\n     * @param supplier\n     * @return\n     */\n    public static Future<Response> executeAsync(String resourceName, Supplier<Future<Response>> supplier) {",
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
    path = ensure_analyzed(rel)
    text = path.read_text(encoding="utf-8")
    if len(re.findall(r"[\u4e00-\u9fff]", text)) >= 10:
        return
    for old, new in R.get(rel, []):
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
    batch["done"] = batch.get("done", 240) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 695) - len(BATCH_LIST)
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
            "wave9a gateway/dubbo/grpc/jaxrs [0:15]",
            *BATCH_LIST,
        ],
        check=True,
    )
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
