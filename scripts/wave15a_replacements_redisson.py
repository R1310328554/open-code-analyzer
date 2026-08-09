"""Chinese annotation replacements for Redisson 4.7.0 wave-15a spring-data-22 [0:15]."""
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


W13B = _load("wave13b_replacements_redisson.py", "W13B_REPLACEMENTS")

W15A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- spring-data-22: reuse wave-13b (sources identical to spring-data-21) ---
for _key in (
    "RedissonReactivePubSubCommands.java",
    "RedissonReactiveRedisClusterConnection.java",
    "RedissonReactiveRedisConnection.java",
    "RedissonReactiveScriptingCommands.java",
    "RedissonReactiveServerCommands.java",
    "RedissonReactiveSetCommands.java",
    "RedissonReactiveSubscription.java",
    "RedissonSentinelConnection.java",
    "RedissonSubscription.java",
    "ScoredSortedListReplayDecoder.java",
    "ScoredSortedSetReplayDecoder.java",
    "ScoredSortedSetReplayDecoderV2.java",
    "SecondsConvertor.java",
    "SetReplayDecoder.java",
):
    W15A_REPLACEMENTS[_key] = W13B[_key]

# --- spring-data-22: reactive Stream commands ---
W15A_REPLACEMENTS["RedissonReactiveStreamCommands.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Data Redis 响应式 Stream 命令实现。\n"
        " * <p>封装 XADD/XACK/XDEL、XRANGE/XREVRANGE、XREAD/XREADGROUP、\n"
        "XGROUP 消费者组管理及 XTRIM 等 Redis Stream 操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveStreamCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveStreamCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    private static List<String> toStringList(List<RecordId> recordIds) {",
        "    /** 将 {@link RecordId} 列表转为 Redis 命令所需的字符串 ID 列表。 */\n"
        "    private static List<String> toStringList(List<RecordId> recordIds) {",
    ),
    (
        "    @Override\n    public Flux<ReactiveRedisConnection.NumericResponse<AcknowledgeCommand, Long>> xAck(Publisher<AcknowledgeCommand> publisher) {",
        "    /** XACK：确认消费组已处理指定消息 ID。 */\n"
        "    @Override\n"
        "    public Flux<ReactiveRedisConnection.NumericResponse<AcknowledgeCommand, Long>> xAck(Publisher<AcknowledgeCommand> publisher) {",
    ),
    (
        "    @Override\n    public Flux<ReactiveRedisConnection.CommandResponse<AddStreamRecord, RecordId>> xAdd(Publisher<AddStreamRecord> publisher) {",
        "    /** XADD：向 Stream 追加一条记录，支持自动生成或指定 ID。 */\n"
        "    @Override\n"
        "    public Flux<ReactiveRedisConnection.CommandResponse<AddStreamRecord, RecordId>> xAdd(Publisher<AddStreamRecord> publisher) {",
    ),
    (
        "    @Override\n    public Flux<ReactiveRedisConnection.CommandResponse<DeleteCommand, Long>> xDel(Publisher<DeleteCommand> publisher) {",
        "    /** XDEL：按 ID 删除 Stream 中的消息。 */\n"
        "    @Override\n"
        "    public Flux<ReactiveRedisConnection.CommandResponse<DeleteCommand, Long>> xDel(Publisher<DeleteCommand> publisher) {",
    ),
    (
        "    @Override\n    public Flux<ReactiveRedisConnection.NumericResponse<ReactiveRedisConnection.KeyCommand, Long>> xLen(Publisher<ReactiveRedisConnection.KeyCommand> publisher) {",
        "    /** XLEN：返回 Stream 当前长度。 */\n"
        "    @Override\n"
        "    public Flux<ReactiveRedisConnection.NumericResponse<ReactiveRedisConnection.KeyCommand, Long>> xLen(Publisher<ReactiveRedisConnection.KeyCommand> publisher) {",
    ),
    (
        "    @Override\n    public Flux<ReactiveRedisConnection.CommandResponse<RangeCommand, Flux<ByteBufferRecord>>> xRange(Publisher<RangeCommand> publisher) {",
        "    /** XRANGE：按 ID 范围正序读取 Stream 记录。 */\n"
        "    @Override\n"
        "    public Flux<ReactiveRedisConnection.CommandResponse<RangeCommand, Flux<ByteBufferRecord>>> xRange(Publisher<RangeCommand> publisher) {",
    ),
    (
        "    private Flux<ReactiveRedisConnection.CommandResponse<RangeCommand, Flux<ByteBufferRecord>>> range(RedisCommand<?> rangeCommand, Publisher<RangeCommand> publisher) {",
        "    /** XRANGE/XREVRANGE 共用实现：组装边界、COUNT 参数并解码为 {@link ByteBufferRecord} 流。 */\n"
        "    private Flux<ReactiveRedisConnection.CommandResponse<RangeCommand, Flux<ByteBufferRecord>>> range(RedisCommand<?> rangeCommand, Publisher<RangeCommand> publisher) {",
    ),
    (
        "    String toLowerBound(Range range) {",
        "    /** 将 Spring {@link Range} 下界转为 Redis Stream ID 字符串（含开区间前缀）。 */\n"
        "    String toLowerBound(Range range) {",
    ),
    (
        "    String toUpperBound(Range range) {",
        "    /** 将 Spring {@link Range} 上界转为 Redis Stream ID 字符串（含开区间前缀）。 */\n"
        "    String toUpperBound(Range range) {",
    ),
    (
        "    @Override\n    public Flux<ReactiveRedisConnection.CommandResponse<ReadCommand, Flux<ByteBufferRecord>>> read(Publisher<ReadCommand> publisher) {",
        "    /** XREAD/XREADGROUP：阻塞或非阻塞读取一个或多个 Stream，支持 COUNT/BLOCK/NOACK。 */\n"
        "    @Override\n"
        "    public Flux<ReactiveRedisConnection.CommandResponse<ReadCommand, Flux<ByteBufferRecord>>> read(Publisher<ReadCommand> publisher) {",
    ),
    (
        "    @Override\n    public Flux<ReactiveRedisConnection.CommandResponse<GroupCommand, String>> xGroup(Publisher<GroupCommand> publisher) {",
        "    /** XGROUP：创建消费组、删除消费者或销毁消费组。 */\n"
        "    @Override\n"
        "    public Flux<ReactiveRedisConnection.CommandResponse<GroupCommand, String>> xGroup(Publisher<GroupCommand> publisher) {",
    ),
    (
        "            throw new IllegalArgumentException(\"unknown command \" + command.getAction());",
        "            // 未知 XGROUP 子命令。\n"
        "            throw new IllegalArgumentException(\"unknown command \" + command.getAction());",
    ),
    (
        "    @Override\n    public Flux<ReactiveRedisConnection.CommandResponse<RangeCommand, Flux<ByteBufferRecord>>> xRevRange(Publisher<RangeCommand> publisher) {",
        "    /** XREVRANGE：按 ID 范围逆序读取 Stream 记录。 */\n"
        "    @Override\n"
        "    public Flux<ReactiveRedisConnection.CommandResponse<RangeCommand, Flux<ByteBufferRecord>>> xRevRange(Publisher<RangeCommand> publisher) {",
    ),
    (
        "    @Override\n    public Flux<ReactiveRedisConnection.NumericResponse<ReactiveRedisConnection.KeyCommand, Long>> xTrim(Publisher<TrimCommand> publisher) {",
        "    /** XTRIM MAXLEN：按最大长度裁剪 Stream。 */\n"
        "    @Override\n"
        "    public Flux<ReactiveRedisConnection.NumericResponse<ReactiveRedisConnection.KeyCommand, Long>> xTrim(Publisher<TrimCommand> publisher) {",
    ),
]
