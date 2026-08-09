"""Chinese annotation replacements for Redisson 4.7.0 wave-20b spring-data-26 reactive [15:30]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parent


def _load(module_file: str, attr: str) -> dict[str, list[tuple[str, str]]]:
    spec = importlib.util.spec_from_file_location(module_file, SCRIPTS / module_file)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return getattr(mod, attr)


W14B = _load("wave14b_replacements_redisson.py", "W14B_REPLACEMENTS")
W18B = _load("wave18b_replacements_redisson.py", "W18B_REPLACEMENTS")

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

W20B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-26: identical to spring-data-25 for these cluster reactive adapters.
for _key in (
    "RedissonReactiveClusterListCommands.java",
    "RedissonReactiveClusterNumberCommands.java",
    "RedissonReactiveClusterServerCommands.java",
    "RedissonReactiveClusterSetCommands.java",
    "RedissonReactiveClusterStreamCommands.java",
    "RedissonReactiveClusterStringCommands.java",
    "RedissonReactiveClusterZSetCommands.java",
    "RedissonReactiveHyperLogLogCommands.java",
    "RedissonReactiveNumberCommands.java",
):
    W20B_REPLACEMENTS[_key] = W14B[_key]

W20B_REPLACEMENTS["RedissonReactiveClusterKeyCommands.java"] = W18B[
    "RedissonReactiveClusterKeyCommands.java"
]

# spring-data-26: Hash/Key/List reuse wave-19a base plus sd-26-only API.
W20B_REPLACEMENTS["RedissonReactiveHashCommands.java"] = list(
    W14B["RedissonReactiveHashCommands.java"]
) + [
    (
        "    @Override\n    public Flux<CommandResponse<HRandFieldCommand, Flux<ByteBuffer>>> hRandField(Publisher<HRandFieldCommand> commands) {",
        "    /** HRANDFIELD：随机返回 hash 字段名（可选 COUNT）。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<HRandFieldCommand, Flux<ByteBuffer>>> hRandField(Publisher<HRandFieldCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<HRandFieldCommand, Flux<Entry<ByteBuffer, ByteBuffer>>>> hRandFieldWithValues(Publisher<HRandFieldCommand> commands) {",
        "    /** HRANDFIELD WITHVALUES：随机返回字段名与值的键值对。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<HRandFieldCommand, Flux<Entry<ByteBuffer, ByteBuffer>>>> hRandFieldWithValues(Publisher<HRandFieldCommand> commands) {",
    ),
]

W20B_REPLACEMENTS["RedissonReactiveKeyCommands.java"] = list(
    W14B["RedissonReactiveKeyCommands.java"]
) + [
    (
        "    @Override\n    public Flux<BooleanResponse<CopyCommand>> copy(Publisher<CopyCommand> commands) {",
        "    /** COPY：将 key 复制到目标 key，可选 DB 参数切换库。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<CopyCommand>> copy(Publisher<CopyCommand> commands) {",
    ),
]

W20B_REPLACEMENTS["RedissonReactiveListCommands.java"] = list(
    W14B["RedissonReactiveListCommands.java"]
) + [
    (
        "    @Override\n    public Flux<ByteBufferResponse<LMoveCommand>> lMove(Publisher<? extends LMoveCommand> commands) {",
        "    /** LMOVE：原子地从源列表弹出并推入目标列表。 */\n"
        "    @Override\n"
        "    public Flux<ByteBufferResponse<LMoveCommand>> lMove(Publisher<? extends LMoveCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<ByteBufferResponse<BLMoveCommand>> bLMove(Publisher<BLMoveCommand> commands) {",
        "    /** BLMOVE：阻塞版 LMOVE，在超时内等待可移动元素。 */\n"
        "    @Override\n"
        "    public Flux<ByteBufferResponse<BLMoveCommand>> bLMove(Publisher<BLMoveCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<PopCommand, Flux<ByteBuffer>>> popList(Publisher<PopCommand> commands) {",
        "    /** LPOP/RPOP：按方向弹出单个或多个列表元素。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<PopCommand, Flux<ByteBuffer>>> popList(Publisher<PopCommand> commands) {",
    ),
]

# spring-data-26: GEO uses ByteBuffer member type and adds GEOSEARCH/GEOSEARCHSTORE.
W20B_REPLACEMENTS["RedissonReactiveGeoCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 GEO 命令实现。\n"
        " * <p>封装 GEOADD、GEODIST、GEOHASH、GEOPOS、GEORADIUS、GEOSEARCH 等命令，\n"
        "通过 {@link RedissonBaseReactive#write} / {@link RedissonBaseReactive#read} 路由。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveGeoCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveGeoCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<GeoAddCommand, Long>> geoAdd(Publisher<GeoAddCommand> commands) {",
        "    /** 批量 GEOADD：经度、纬度与 member 依次写入命令参数。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<GeoAddCommand, Long>> geoAdd(Publisher<GeoAddCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<GeoDistCommand, Distance>> geoDist(Publisher<GeoDistCommand> commands) {",
        "    /** GEODIST：可选 {@link Metric} 经 {@link DistanceConvertor} 解码。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<GeoDistCommand, Distance>> geoDist(Publisher<GeoDistCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<MultiValueResponse<GeoHashCommand, String>> geoHash(Publisher<GeoHashCommand> commands) {",
        "    /** GEOHASH：返回各 member 的 geohash 字符串列表。 */\n"
        "    @Override\n"
        "    public Flux<MultiValueResponse<GeoHashCommand, String>> geoHash(Publisher<GeoHashCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<MultiValueResponse<GeoPosCommand, Point>> geoPos(Publisher<GeoPosCommand> commands) {",
        "    /** GEOPOS：经 {@code geoDecoder} 解码经纬度 {@link Point}。 */\n"
        "    @Override\n"
        "    public Flux<MultiValueResponse<GeoPosCommand, Point>> geoPos(Publisher<GeoPosCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<GeoRadiusCommand, Flux<GeoResult<GeoLocation<ByteBuffer>>>>> geoRadius(",
        "    /** GEORADIUS_RO：以坐标为圆心按半径查询附近 member。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<GeoRadiusCommand, Flux<GeoResult<GeoLocation<ByteBuffer>>>>> geoRadius(",
    ),
    (
        "            if (args.getFlags().contains(GeoRadiusCommandArgs.Flag.WITHCOORD)) {",
        "            // WITHCOORD：返回 member 与坐标。\n"
        "            if (args.getFlags().contains(GeoRadiusCommandArgs.Flag.WITHCOORD)) {",
    ),
    (
        "                cmd = new RedisCommand<>(\"GEORADIUS_RO\", distanceDecoder);",
        "                // WITHDIST：返回 member 与距离。\n"
        "                cmd = new RedisCommand<>(\"GEORADIUS_RO\", distanceDecoder);",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<GeoRadiusByMemberCommand, Flux<GeoResult<GeoLocation<ByteBuffer>>>>> geoRadiusByMember(",
        "    /** GEORADIUSBYMEMBER_RO：以 member 为圆心按半径查询。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<GeoRadiusByMemberCommand, Flux<GeoResult<GeoLocation<ByteBuffer>>>>> geoRadiusByMember(",
    ),
    (
        "    private String convert(double longitude) {",
        "    /** 将经度/纬度 double 转为 Redis 命令所需的 plain string。 */\n"
        "    private String convert(double longitude) {",
    ),
    (
        "    private ByteBuf encode(Object value) {",
        "    /** 经 {@link ByteArrayCodec} 编码 member 等命令参数。 */\n"
        "    private ByteBuf encode(Object value) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<GeoSearchCommand, Flux<GeoResult<GeoLocation<ByteBuffer>>>>> geoSearch(Publisher<GeoSearchCommand> commands) {",
        "    /** GEOSEARCH：按坐标/member 参考点与圆/矩形范围搜索附近 member。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<GeoSearchCommand, Flux<GeoResult<GeoLocation<ByteBuffer>>>>> geoSearch(Publisher<GeoSearchCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<GeoSearchStoreCommand, Long>> geoSearchStore(Publisher<GeoSearchStoreCommand> commands) {",
        "    /** GEOSEARCHSTORE：将 GEOSEARCH 结果写入目标 sorted set，可选 STOREDIST。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<GeoSearchStoreCommand, Long>> geoSearchStore(Publisher<GeoSearchStoreCommand> commands) {",
    ),
    (
        "    private Metric convert(Metric metric) {",
        "    /** 将 {@link Metrics#NEUTRAL} 映射为默认米制单位。 */\n"
        "    private Metric convert(Metric metric) {",
    ),
]

# spring-data-26: createSubscription 接受 SubscriptionListener。
W20B_REPLACEMENTS["RedissonReactivePubSubCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 Pub/Sub 命令实现。\n"
        " * <p>通过 {@link #createSubscription(SubscriptionListener)} 创建\n"
        " {@link RedissonReactiveSubscription} 管理订阅；{@link #publish} 向频道发布消息。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactivePubSubCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactivePubSubCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Flux<Long> publish(Publisher<ChannelMessage<ByteBuffer, ByteBuffer>> messageStream) {",
        "    /** PUBLISH：向指定频道发布消息，返回接收订阅者数量。 */\n"
        "    @Override\n"
        "    public Flux<Long> publish(Publisher<ChannelMessage<ByteBuffer, ByteBuffer>> messageStream) {",
    ),
    (
        "    @Override\n    public Mono<Void> subscribe(ByteBuffer... channels) {",
        "    /** 直接订阅未实现，须通过 {@link #createSubscription} 获取订阅对象后操作。 */\n"
        "    @Override\n"
        "    public Mono<Void> subscribe(ByteBuffer... channels) {",
    ),
    (
        "    @Override\n    public Mono<Void> pSubscribe(ByteBuffer... patterns) {",
        "    /** 直接模式订阅未实现，须通过 {@link #createSubscription} 获取订阅对象后操作。 */\n"
        "    @Override\n"
        "    public Mono<Void> pSubscribe(ByteBuffer... patterns) {",
    ),
    (
        "    @Override\n    public Mono<ReactiveSubscription> createSubscription(SubscriptionListener subscriptionListener) {",
        "    /** 创建带 {@link SubscriptionListener} 的响应式订阅对象。 */\n"
        "    @Override\n"
        "    public Mono<ReactiveSubscription> createSubscription(SubscriptionListener subscriptionListener) {",
    ),
]
