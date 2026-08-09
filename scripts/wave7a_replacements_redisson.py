"""Chinese annotation replacements for Redisson 4.7.0 wave-7a quarkus-16/20 [0:15]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

_spec6b = importlib.util.spec_from_file_location(
    "wave6b_replacements_redisson",
    Path(__file__).with_name("wave6b_replacements_redisson.py"),
)
_w6b = importlib.util.module_from_spec(_spec6b)
assert _spec6b.loader is not None
_spec6b.loader.exec_module(_w6b)


def _adapt_q20(reps: list[tuple[str, str]]) -> list[tuple[str, str]]:
    return [(old, new.replace("Quarkus 1.6", "Quarkus 2.0")) for old, new in reps]


W7A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- integration test REST resource (quarkus-16 & quarkus-20 identical) ---
W7A_REPLACEMENTS["QuarkusRedissonClientResource.java"] = [
    (
        "@Path(\"/quarkus-redisson-client\")\npublic class QuarkusRedissonClientResource {",
        "/**\n"
        " * Quarkus Redisson 客户端集成测试 REST 资源。\n"
        " * <p>暴露 {@link RMap}、{@link RRemoteService}、Redis 节点 ping 与\n"
        " * {@link RScheduledExecutorService} 等典型用法的 HTTP 端点，供 IT 用例调用。\n"
        " */\n"
        "@Path(\"/quarkus-redisson-client\")\n"
        "public class QuarkusRedissonClientResource {",
    ),
    (
        "    @Inject\n    RedissonClient redisson;",
        "    /** 注入的 Redisson 客户端（由扩展 CDI 生产者提供）。 */\n"
        "    @Inject\n"
        "    RedissonClient redisson;",
    ),
    (
        "    @GET\n    @Path(\"/map\")\n    public String map() {",
        "    /** 测试 {@link RMap} 读写：写入键 {@code \"1\"} 并返回其值。 */\n"
        "    @GET\n"
        "    @Path(\"/map\")\n"
        "    public String map() {",
    ),
    (
        "    @GET\n    @Path(\"/remoteService\")\n    public String remoteService() {",
        "    /** 测试 {@link RRemoteService}：注册 {@link RemService} 实现并远程调用。 */\n"
        "    @GET\n"
        "    @Path(\"/remoteService\")\n"
        "    public String remoteService() {",
    ),
    (
        "    @GET\n    @Path(\"/pingAll\")\n    public String pingAll() {",
        "    /** 对单机 Redis 节点执行 {@code pingAll} 连通性检测。 */\n"
        "    @GET\n"
        "    @Path(\"/pingAll\")\n"
        "    public String pingAll() {",
    ),
    (
        "    @GET\n    @Path(\"/executeTask\")\n    public String executeTask() throws ExecutionException, InterruptedException {",
        "    /** 测试 {@link RScheduledExecutorService}：提交 {@link Task} 并同步等待结果。 */\n"
        "    @GET\n"
        "    @Path(\"/executeTask\")\n"
        "    public String executeTask() throws ExecutionException, InterruptedException {",
    ),
]

# --- remote service interface & impl ---
W7A_REPLACEMENTS["RemService.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 远程服务接口，供 {@link RRemoteService} 注册与动态代理调用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    String executeMe();",
        "    /** 远程方法：返回执行结果字符串。 */\n"
        "    String executeMe();",
    ),
]

W7A_REPLACEMENTS["RemoteServiceImpl.java"] = [
    (
        "/**\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * {@link RemService} 的本地实现，注册到 {@link RRemoteService} 后供远程调用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public String executeMe() {",
        "    /** 返回 {@code \"executed\"} 表示远程调用成功。 */\n"
        "    @Override\n"
        "    public String executeMe() {",
    ),
]

W7A_REPLACEMENTS["Task.java"] = [
    (
        "/**\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * 可序列化的 {@link java.util.concurrent.Callable} 任务，\n"
        " * 提交到 {@link RScheduledExecutorService} 在 Worker 节点执行。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public String call() throws Exception {",
        "    /** 返回 {@code \"hello\"} 作为分布式执行结果。 */\n"
        "    @Override\n"
        "    public String call() throws Exception {",
    ),
]

# --- quarkus-16 runtime (CDI producer / recorder / config) ---
W7A_REPLACEMENTS["RedissonClientProducer.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus CDI 生产者：从配置或 {@code redisson.yaml} 创建 {@link RedissonClient}。\n"
        " * <p>支持 MicroProfile Config 属性前缀 {@code quarkus.redisson.} 与 classpath 配置文件；\n"
        " * 应用关闭时按 {@link ShutdownConfig} 优雅停止客户端。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Produces\n    @Singleton\n    @DefaultBean\n    public RedissonClient create() throws IOException {",
        "    /** 加载 Redisson 配置并创建单例 {@link RedissonClient}。 */\n"
        "    @Produces\n"
        "    @Singleton\n"
        "    @DefaultBean\n"
        "    public RedissonClient create() throws IOException {",
    ),
    (
        "        Optional<String> configFile = ConfigProvider.getConfig().getOptionalValue(\"quarkus.redisson.file\", String.class);",
        "        // 优先从 quarkus.redisson.file 指定路径或默认 redisson.yaml 加载。\n"
        "        Optional<String> configFile = ConfigProvider.getConfig().getOptionalValue(\"quarkus.redisson.file\", String.class);",
    ),
    (
        "        if (config == null) {",
        "        // 无 YAML 文件时，将 quarkus.redisson.* 属性聚合为 YAML 字符串。\n"
        "        if (config == null) {",
    ),
    (
        "        if (config.trim().isEmpty()) {",
        "        // 配置为空时拒绝启动，避免静默连接失败。\n"
        "        if (config.trim().isEmpty()) {",
    ),
    (
        "    public void setConfig(org.eclipse.microprofile.config.Config config) {",
        "    /** 预留的配置注入钩子（当前为空实现）。 */\n"
        "    public void setConfig(org.eclipse.microprofile.config.Config config) {",
    ),
    (
        "    @PreDestroy\n    public void close() {",
        "    /** 容器销毁时关闭 Redisson 客户端；若配置了 shutdown timeout 则分阶段优雅退出。 */\n"
        "    @PreDestroy\n"
        "    public void close() {",
    ),
]

W7A_REPLACEMENTS["RedissonClientRecorder.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus {@link io.quarkus.runtime.annotations.Recorder}：\n"
        " * 在运行时初始化阶段触发 {@link RedissonClientProducer} 创建。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public void createProducer() {",
        "    /** 通过 Arc 容器获取生产者实例，完成 Redisson 客户端初始化。 */\n"
        "    public void createProducer() {",
    ),
]

W7A_REPLACEMENTS["RedissonConfig.java"] = [
    (
        "/**\n * Redisson config\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus 运行时 Redisson 配置映射（{@code quarkus.redisson.*}）。\n"
        " * <p>各方法返回对应部署模式（单机/集群/哨兵等）的配置键值对。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Common params\n     *\n     * @return params\n     */",
        "    /** 通用 Redisson 参数。\n     * @return params 配置键值对\n     */",
    ),
    (
        "    /**\n     * Single server params\n     *\n     * @return params\n     */",
        "    /** 单机模式参数。\n     * @return params 配置键值对\n     */",
    ),
    (
        "    /**\n     * Cluster servers params\n     *\n     * @return params\n     */",
        "    /** 集群模式参数。\n     * @return params 配置键值对\n     */",
    ),
    (
        "    /**\n     * Sentinel servers params\n     *\n     * @return params\n     */",
        "    /** 哨兵模式参数。\n     * @return params 配置键值对\n     */",
    ),
    (
        "    /**\n     * Replicated servers params\n     *\n     * @return params\n     */",
        "    /** 复制模式参数。\n     * @return params 配置键值对\n     */",
    ),
    (
        "    /**\n     * Master and slave servers params\n     *\n     * @return params\n     */",
        "    /** 主从模式参数。\n     * @return params 配置键值对\n     */",
    ),
]

# --- GraalVM Native Image substitutions ---
W7A_REPLACEMENTS["ByteBuddySubstitutions.java"] = [
    (
        "@TargetClass(className = \"net.bytebuddy.description.type.TypeDescription$Generic$AnnotationReader$ForTypeVariableBoundType$OfFormalTypeVariable\")\nfinal class OfFormalTypeVariableSubstitute {",
        "/**\n"
        " * GraalVM Native Image 替代：ByteBuddy 形式类型变量注解解析在 Native 模式下不可用。\n"
        " */\n"
        "@TargetClass(className = \"net.bytebuddy.description.type.TypeDescription$Generic$AnnotationReader$ForTypeVariableBoundType$OfFormalTypeVariable\")\n"
        "final class OfFormalTypeVariableSubstitute {",
    ),
    (
        "    @Substitute\n    public AnnotatedElement resolve() {",
        "    /** Native 模式下返回 {@code null}，跳过 ByteBuddy 注解读取。 */\n"
        "    @Substitute\n"
        "    public AnnotatedElement resolve() {",
    ),
    (
        "@TargetClass(className = \"net.bytebuddy.description.type.TypeDescription$Generic$AnnotationReader$ForTypeVariableBoundType\")\nfinal class ForTypeVariableBoundTypeSubstitute {",
        "/** GraalVM 替代：类型变量上界注解解析的占位实现。 */\n"
        "@TargetClass(className = \"net.bytebuddy.description.type.TypeDescription$Generic$AnnotationReader$ForTypeVariableBoundType\")\n"
        "final class ForTypeVariableBoundTypeSubstitute {",
    ),
    (
        "    @Substitute\n    protected AnnotatedElement resolve(AnnotatedElement annotatedElement) {",
        "    /** Native 模式下返回 {@code null}。 */\n"
        "    @Substitute\n"
        "    protected AnnotatedElement resolve(AnnotatedElement annotatedElement) {",
    ),
]

W7A_REPLACEMENTS["CodecsSubstitutions.java"] = [
    (
        "@TargetClass(className = \"org.redisson.connection.ServiceManager\")\nfinal class ServiceManagerSubstitute {",
        "/** GraalVM 替代：Native 模式下禁用 IOUring EventLoop 组创建。 */\n"
        "@TargetClass(className = \"org.redisson.connection.ServiceManager\")\n"
        "final class ServiceManagerSubstitute {",
    ),
    (
        "    @Substitute\n    private static EventLoopGroup createIOUringGroup(Config cfg) {",
        "    /** 抛出异常表明 IOUring 与 Native Image 不兼容。 */\n"
        "    @Substitute\n"
        "    private static EventLoopGroup createIOUringGroup(Config cfg) {",
    ),
    (
        "@TargetClass(className = \"org.redisson.codec.JsonJacksonCodec\")\nfinal class JsonJacksonCodecSubstitute {",
        "/** GraalVM 替代：跳过 JsonJacksonCodec 构建时 warmup（Native 下不可用）。 */\n"
        "@TargetClass(className = \"org.redisson.codec.JsonJacksonCodec\")\n"
        "final class JsonJacksonCodecSubstitute {",
    ),
    (
        "    @Substitute\n    private void warmup() {",
        "    /** Native 模式下空实现，避免反射预热失败。 */\n"
        "    @Substitute\n"
        "    private void warmup() {",
    ),
]

# --- quarkus-20 deployment (reuse wave-6b, adapt version label) ---
W7A_REPLACEMENTS["QuarkusRedissonClientProcessor.java"] = _adapt_q20(
    _w6b.W6B_REPLACEMENTS["QuarkusRedissonClientProcessor.java"]
)
W7A_REPLACEMENTS["RedissonClientItemBuild.java"] = _w6b.W6B_REPLACEMENTS[
    "RedissonClientItemBuild.java"
]
