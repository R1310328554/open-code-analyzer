"""Chinese annotation replacements for Redisson 4.7.0 wave-6b [15:30]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

_spec5b = importlib.util.spec_from_file_location(
    "wave5b_replacements_redisson",
    Path(__file__).with_name("wave5b_replacements_redisson.py"),
)
_w5b = importlib.util.module_from_spec(_spec5b)
assert _spec5b.loader is not None
_spec5b.loader.exec_module(_w5b)

_spec4b = importlib.util.spec_from_file_location(
    "wave4b_replacements_redisson",
    Path(__file__).with_name("wave4b_replacements_redisson.py"),
)
_w4b = importlib.util.module_from_spec(_spec4b)
assert _spec4b.loader is not None
_spec4b.loader.exec_module(_w4b)


def _m5(text: str) -> str:
    return (
        text.replace("Micronaut 4.x", "Micronaut 5.x")
        .replace("Micronaut 3.x", "Micronaut 5.x")
        .replace("Micronaut 2.x", "Micronaut 5.x")
    )


def _from_w5b(name: str) -> list[tuple[str, str]]:
    return [(old, _m5(new)) for old, new in _w5b.W5B_REPLACEMENTS[name]]


def _from_w4b(name: str) -> list[tuple[str, str]]:
    return [(old, _m5(new)) for old, new in _w4b.W4B_REPLACEMENTS[name]]


W6B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

W6B_REPLACEMENTS["RedissonSyncCache.java"] = _from_w5b("RedissonSyncCache.java")
W6B_REPLACEMENTS["AttributeMessage.java"] = _from_w5b("AttributeMessage.java")
W6B_REPLACEMENTS["AttributeRemoveMessage.java"] = _from_w4b("AttributeRemoveMessage.java")
W6B_REPLACEMENTS["AttributeUpdateMessage.java"] = _from_w4b("AttributeUpdateMessage.java")
W6B_REPLACEMENTS["AttributesClearMessage.java"] = _from_w5b("AttributesClearMessage.java")
W6B_REPLACEMENTS["AttributesPutAllMessage.java"] = _from_w5b("AttributesPutAllMessage.java")
W6B_REPLACEMENTS["SessionCreatedMessage.java"] = _from_w5b("SessionCreatedMessage.java")
W6B_REPLACEMENTS["SessionDestroyedMessage.java"] = _from_w5b("SessionDestroyedMessage.java")
W6B_REPLACEMENTS["RedissonHttpSessionConfiguration.java"] = _from_w5b(
    "RedissonHttpSessionConfiguration.java"
)
W6B_REPLACEMENTS["RedissonSession.java"] = _from_w5b("RedissonSession.java")
W6B_REPLACEMENTS["RedissonSessionStore.java"] = _from_w5b("RedissonSessionStore.java")

W6B_REPLACEMENTS["RedissonCache.java"] = [
    (
        "/**\n * MyBatis cache implementation\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson {@link RMapCache} 的 MyBatis 二级缓存实现。\n"
        " * <p>通过 {@link #setRedissonConfig(String)} 从 classpath YAML 加载 Redisson 配置；\n"
        " * 支持写入 TTL、访问 max-idle 与 LRU 容量上限。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonCache(String id) {",
        "    /** @param id MyBatis 缓存命名空间 ID，同时作为 Redis Map 名称 */\n"
        "    public RedissonCache(String id) {",
    ),
    (
        "    @Override\n    public void putObject(Object o, Object o1) {",
        "    /** 写入缓存条目；MapCache 模式下附带 TTL 与 max-idle。 */\n"
        "    @Override\n"
        "    public void putObject(Object o, Object o1) {",
    ),
    (
        "    @Override\n    public Object getObject(Object o) {",
        "    /** 读取缓存；无 max-idle/容量限制时使用 {@link RMapCache#getWithTTLOnly}。 */\n"
        "    @Override\n"
        "    public Object getObject(Object o) {",
    ),
    (
        "    @Override\n    public Object removeObject(Object o) {",
        "    /** 移除单个键并返回先前值。 */\n"
        "    @Override\n"
        "    public Object removeObject(Object o) {",
    ),
    (
        "    @Override\n    public void clear() {",
        "    /** 清空整个 MapCache。 */\n"
        "    @Override\n"
        "    public void clear() {",
    ),
    (
        "    @Override\n    public int getSize() {",
        "    /** 返回当前缓存条目数。 */\n"
        "    @Override\n"
        "    public int getSize() {",
    ),
    (
        "    public void setTimeToLive(long timeToLive) {",
        "    /** 设置写入后 TTL（毫秒）。 */\n    public void setTimeToLive(long timeToLive) {",
    ),
    (
        "    public void setMaxIdleTime(long maxIdleTime) {",
        "    /** 设置访问后 max-idle（毫秒）。 */\n    public void setMaxIdleTime(long maxIdleTime) {",
    ),
    (
        "    public void setMaxSize(int maxSize) {",
        "    /** 设置 LRU 容量上限；{@code > 0} 时在 {@link #setRedissonConfig} 中生效。 */\n"
        "    public void setMaxSize(int maxSize) {",
    ),
    (
        "    public ReadWriteLock getReadWriteLock() {",
        "    /** MyBatis 并发锁；Redisson 实现返回 {@code null}（依赖 Redis 原子操作）。 */\n"
        "    public ReadWriteLock getReadWriteLock() {",
    ),
    (
        "    public void setRedissonConfig(String config) {",
        "    /** 从 classpath 加载 Redisson YAML 并初始化 {@link RMapCache}。\n"
        "     *  @param config 配置文件路径（如 {@code redisson.yaml}）\n"
        "     */\n"
        "    public void setRedissonConfig(String config) {",
    ),
    (
        "    protected RMapCache<Object, Object> getMapCache(String id, RedissonClient redisson) {",
        "    /** 子类可覆盖以使用 Native Map 等变体；默认 {@link RedissonClient#getMapCache}。 */\n"
        "    protected RMapCache<Object, Object> getMapCache(String id, RedissonClient redisson) {",
    ),
    (
        "    private void check() {",
        "    /** 确保已通过 {@link #setRedissonConfig} 初始化 MapCache。 */\n    private void check() {",
    ),
]

W6B_REPLACEMENTS["RedissonCacheNative.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redis 7+ {@link RMapCacheNative} 的 MyBatis 缓存实现。\n"
        " * <p>通过 {@link MapCacheNativeWrapper} 适配标准 {@link RMapCache} 接口；\n"
        " * 不支持 maxIdleTime 与 maxSize 配置。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonCacheNative(String id) {",
        "    /** @param id MyBatis 缓存命名空间 ID */\n    public RedissonCacheNative(String id) {",
    ),
    (
        "    @Override\n    public void setMaxIdleTime(long maxIdleTime) {",
        "    /** Native Map 不支持 max-idle，调用时抛出 {@link IllegalArgumentException}。 */\n"
        "    @Override\n"
        "    public void setMaxIdleTime(long maxIdleTime) {",
    ),
    (
        "    @Override\n    public void setMaxSize(int maxSize) {",
        "    /** Native Map 不支持 LRU 容量限制，调用时抛出 {@link IllegalArgumentException}。 */\n"
        "    @Override\n"
        "    public void setMaxSize(int maxSize) {",
    ),
    (
        "    @Override\n    protected RMapCache<Object, Object> getMapCache(String id, RedissonClient redisson) {",
        "    /** 使用 {@link RedissonClient#getMapCacheNative} 并包装为 {@link RMapCache}。 */\n"
        "    @Override\n"
        "    protected RMapCache<Object, Object> getMapCache(String id, RedissonClient redisson) {",
    ),
]

W6B_REPLACEMENTS["QuarkusRedissonClientProcessor.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus 1.6 扩展部署处理器：注册 Redisson 客户端 CDI 生产者与 Native Image 反射配置。\n"
        " * <p>构建阶段加载 {@code redisson.yaml}、注册 {@link Kryo5Codec} 与配置类反射，\n"
        " * 运行时通过 {@link RedissonClientRecorder} 创建 {@link RedissonClientProducer}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @BuildStep\n    FeatureBuildItem feature() {",
        "    /** 向 Quarkus 注册 {@code redisson} 扩展特性名。 */\n"
        "    @BuildStep\n"
        "    FeatureBuildItem feature() {",
    ),
    (
        "    @BuildStep\n    ExtensionSslNativeSupportBuildItem sslNativeSupport() {",
        "    /** 声明扩展支持 Native Image 下的 SSL/TLS。 */\n"
        "    @BuildStep\n"
        "    ExtensionSslNativeSupportBuildItem sslNativeSupport() {",
    ),
    (
        "    @BuildStep\n    AdditionalBeanBuildItem addProducer() {",
        "    /** 将 {@link RedissonClientProducer} 注册为不可移除的 CDI Bean。 */\n"
        "    @BuildStep\n"
        "    AdditionalBeanBuildItem addProducer() {",
    ),
    (
        "    @BuildStep\n    void addConfig(BuildProducer<NativeImageResourceBuildItem> nativeResources,",
        "    /** 配置 Native Image 资源、热部署监听文件与 GraalVM 反射类列表。 */\n"
        "    @BuildStep\n"
        "    void addConfig(BuildProducer<NativeImageResourceBuildItem> nativeResources,",
    ),
    (
        "        nativeResources.produce(new NativeImageResourceBuildItem(\"redisson.yaml\"));",
        "        // 将 redisson.yaml 与 JBoss Marshalling SPI 描述符打包进 Native Image。\n"
        "        nativeResources.produce(new NativeImageResourceBuildItem(\"redisson.yaml\"));",
    ),
    (
        "        watchedFiles.produce(new HotDeploymentWatchedFileBuildItem(\"redisson.yaml\"));",
        "        // 开发模式下监听 redisson.yaml 变更以触发热重载。\n"
        "        watchedFiles.produce(new HotDeploymentWatchedFileBuildItem(\"redisson.yaml\"));",
    ),
    (
        "        reflectiveItems.produce(ReflectiveClassBuildItem.builder(Kryo5Codec.class)",
        "        // Kryo5 编解码器仅需类注册（无方法/字段反射）。\n"
        "        reflectiveItems.produce(ReflectiveClassBuildItem.builder(Kryo5Codec.class)",
    ),
    (
        "        reflectiveItems.produce(ReflectiveClassBuildItem.builder(\n"
        "                        RemoteExecutorService.class,\n"
        "                        RemoteExecutorServiceAsync.class)",
        "        // 远程执行服务接口需方法反射以支持 RMI 代理。\n"
        "        reflectiveItems.produce(ReflectiveClassBuildItem.builder(\n"
        "                        RemoteExecutorService.class,\n"
        "                        RemoteExecutorServiceAsync.class)",
    ),
    (
        "        reflectiveItems.produce(ReflectiveClassBuildItem.builder(\n"
        "                        Config.class,\n"
        "                        BaseConfig.class,\n"
        "                        BaseMasterSlaveServersConfig.class,\n"
        "                        SingleServerConfig.class,\n"
        "                        ReplicatedServersConfig.class,\n"
        "                        SentinelServersConfig.class,\n"
        "                        ClusterServersConfig.class)",
        "        // Redisson 配置层次结构需完整反射以支持 YAML/Properties 绑定。\n"
        "        reflectiveItems.produce(ReflectiveClassBuildItem.builder(\n"
        "                        Config.class,\n"
        "                        BaseConfig.class,\n"
        "                        BaseMasterSlaveServersConfig.class,\n"
        "                        SingleServerConfig.class,\n"
        "                        ReplicatedServersConfig.class,\n"
        "                        SentinelServersConfig.class,\n"
        "                        ClusterServersConfig.class)",
    ),
    (
        "        reflectiveItems.produce(ReflectiveClassBuildItem.builder(\n"
        "                        RBucket.class,\n"
        "                        RedissonBucket.class,\n"
        "                        RedissonObject.class,\n"
        "                        RedissonMultimap.class)",
        "        // 常用 Redis 对象 API 需方法与字段反射。\n"
        "        reflectiveItems.produce(ReflectiveClassBuildItem.builder(\n"
        "                        RBucket.class,\n"
        "                        RedissonBucket.class,\n"
        "                        RedissonObject.class,\n"
        "                        RedissonMultimap.class)",
    ),
    (
        "        reflectiveItems.produce(ReflectiveClassBuildItem.builder(\n"
        "                        RObjectReactive.class,\n"
        "                        RExpirable.class,\n"
        "                        RObject.class)",
        "        // 响应式与通用对象接口需方法反射。\n"
        "        reflectiveItems.produce(ReflectiveClassBuildItem.builder(\n"
        "                        RObjectReactive.class,\n"
        "                        RExpirable.class,\n"
        "                        RObject.class)",
    ),
    (
        "    @BuildStep\n    @Record(ExecutionTime.RUNTIME_INIT)\n    RedissonClientItemBuild build(RedissonClientRecorder recorder) throws IOException {",
        "    /** 运行时初始化：调用 Recorder 创建 CDI 生产者并返回构建标记项。 */\n"
        "    @BuildStep\n"
        "    @Record(ExecutionTime.RUNTIME_INIT)\n"
        "    RedissonClientItemBuild build(RedissonClientRecorder recorder) throws IOException {",
    ),
]

W6B_REPLACEMENTS["RedissonClientItemBuild.java"] = [
    (
        "/**\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * Redisson 客户端扩展的 Quarkus 构建标记项（{@link SimpleBuildItem}）。\n"
        " * <p>表示 {@link QuarkusRedissonClientProcessor#build} 步骤已完成，\n"
        " * 供其他扩展在构建图中依赖 Redisson 客户端就绪状态。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
