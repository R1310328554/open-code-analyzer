"""Chinese annotation replacements for Redisson 4.7.0 wave-13a spring-data-21 reactive [0:15]."""
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


W11B = _load("wave11b_replacements_redisson.py", "W11B_REPLACEMENTS")
W12A = _load("wave12a_replacements_redisson.py", "W12A_REPLACEMENTS")

W13A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

# --- cluster reactive adapters + GEO/Hash/HyperLogLog cluster key/server (spring-data-21 identical) ---
for _key in (
    "RedissonReactiveClusterGeoCommands.java",
    "RedissonReactiveClusterHashCommands.java",
    "RedissonReactiveClusterHyperLogLogCommands.java",
    "RedissonReactiveClusterKeyCommands.java",
    "RedissonReactiveClusterListCommands.java",
    "RedissonReactiveClusterNumberCommands.java",
    "RedissonReactiveClusterServerCommands.java",
    "RedissonReactiveClusterSetCommands.java",
    "RedissonReactiveClusterStringCommands.java",
    "RedissonReactiveClusterZSetCommands.java",
    "RedissonReactiveGeoCommands.java",
):
    W13A_REPLACEMENTS[_key] = W11B[_key]

W13A_REPLACEMENTS["RedissonReactiveHyperLogLogCommands.java"] = W12A[
    "RedissonReactiveHyperLogLogCommands.java"
]
W13A_REPLACEMENTS["RedissonReactiveListCommands.java"] = W12A["RedissonReactiveListCommands.java"]

# --- spring-data-21: reactive Hash (wave-11b + HSCAN/HSTRLEN) ---
W13A_REPLACEMENTS["RedissonReactiveHashCommands.java"] = list(
    W11B["RedissonReactiveHashCommands.java"]
) + [
    (
        "    @Override\n    public Flux<CommandResponse<KeyCommand, Flux<Entry<ByteBuffer, ByteBuffer>>>> hScan(",
        "    /** HSCAN：按 {@link KeyScanCommand} 选项增量扫描 hash 字段。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<KeyCommand, Flux<Entry<ByteBuffer, ByteBuffer>>>> hScan(",
    ),
    (
        "                public RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {",
        "                /** 分页拉取 HSCAN 游标，支持 MATCH/COUNT 选项。 */\n"
        "                public RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<HStrLenCommand, Long>> hStrLen(Publisher<HStrLenCommand> commands) {",
        "    /** HSTRLEN：返回 hash 字段值的字节长度。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<HStrLenCommand, Long>> hStrLen(Publisher<HStrLenCommand> commands) {",
    ),
]

# --- spring-data-21: reactive Key (wave-12a + touch/scan/unlink/expire variants/OBJECT) ---
_KEY_JDOC_OLD = (
    "/**\n"
    " * Spring Data Redis 响应式 Key 命令实现。\n"
    " * <p>封装 EXISTS、TYPE、KEYS、RENAME、DEL、EXPIRE、TTL、MOVE 等通用 key 操作。\n"
    " *\n"
    " * @author Nikita Koksharov\n"
    " *\n"
    " */"
)
_KEY_JDOC_NEW = (
    "/**\n"
    " * Spring Data Redis 响应式 Key 命令实现。\n"
    " * <p>封装 EXISTS、TYPE、KEYS、SCAN、RENAME、DEL、UNLINK、EXPIRE、TTL、MOVE 及 OBJECT 等通用 key 操作。\n"
    " *\n"
    " * @author Nikita Koksharov\n"
    " *\n"
    " */"
)

_key_reps = []
for old, new in W12A["RedissonReactiveKeyCommands.java"]:
    if new == _KEY_JDOC_OLD:
        _key_reps.append((old, _KEY_JDOC_NEW))
    else:
        _key_reps.append((old, new))

_key_reps.extend(
    [
        (
            "    @Override\n    public Flux<NumericResponse<Collection<ByteBuffer>, Long>> touch(Publisher<Collection<ByteBuffer>> keys) {",
            "    /** TOUCH：刷新一个或多个 key 的最后访问时间。 */\n"
            "    @Override\n"
            "    public Flux<NumericResponse<Collection<ByteBuffer>, Long>> touch(Publisher<Collection<ByteBuffer>> keys) {",
        ),
        (
            "    @Override\n    public Flux<ByteBuffer> scan(ScanOptions options) {",
            "    /** SCAN：经 {@link RedissonKeysReactive} 按模式增量迭代 key。 */\n"
            "    @Override\n"
            "    public Flux<ByteBuffer> scan(ScanOptions options) {",
        ),
        (
            "    @Override\n    public Flux<BooleanResponse<RenameCommand>> renameNX(Publisher<RenameCommand> commands) {",
            "    /** RENAMENX：仅当新 key 不存在时重命名。 */\n"
            "    @Override\n"
            "    public Flux<BooleanResponse<RenameCommand>> renameNX(Publisher<RenameCommand> commands) {",
        ),
        (
            "    @Override\n    public Flux<NumericResponse<KeyCommand, Long>> unlink(Publisher<KeyCommand> keys) {",
            "    /** UNLINK：异步删除单个 key 并返回删除数量。 */\n"
            "    @Override\n"
            "    public Flux<NumericResponse<KeyCommand, Long>> unlink(Publisher<KeyCommand> keys) {",
        ),
        (
            "    @Override\n    public Flux<NumericResponse<List<ByteBuffer>, Long>> mUnlink(Publisher<List<ByteBuffer>> keys) {",
            "    /** 批量 UNLINK：一次异步删除多个 key。 */\n"
            "    @Override\n"
            "    public Flux<NumericResponse<List<ByteBuffer>, Long>> mUnlink(Publisher<List<ByteBuffer>> keys) {",
        ),
        (
            "    @Override\n    public Flux<BooleanResponse<ExpireCommand>> pExpire(Publisher<ExpireCommand> commands) {",
            "    /** PEXPIRE：以毫秒为单位设置 key 过期时间。 */\n"
            "    @Override\n"
            "    public Flux<BooleanResponse<ExpireCommand>> pExpire(Publisher<ExpireCommand> commands) {",
        ),
        (
            "    @Override\n    public Flux<BooleanResponse<ExpireAtCommand>> expireAt(Publisher<ExpireAtCommand> commands) {",
            "    /** EXPIREAT：按 Unix 秒时间戳设置过期。 */\n"
            "    @Override\n"
            "    public Flux<BooleanResponse<ExpireAtCommand>> expireAt(Publisher<ExpireAtCommand> commands) {",
        ),
        (
            "    @Override\n    public Flux<BooleanResponse<ExpireAtCommand>> pExpireAt(Publisher<ExpireAtCommand> commands) {",
            "    /** PEXPIREAT：按 Unix 毫秒时间戳设置过期。 */\n"
            "    @Override\n"
            "    public Flux<BooleanResponse<ExpireAtCommand>> pExpireAt(Publisher<ExpireAtCommand> commands) {",
        ),
        (
            "    @Override\n    public Flux<BooleanResponse<KeyCommand>> persist(Publisher<KeyCommand> commands) {",
            "    /** PERSIST：移除 key 的过期时间使其持久化。 */\n"
            "    @Override\n"
            "    public Flux<BooleanResponse<KeyCommand>> persist(Publisher<KeyCommand> commands) {",
        ),
        (
            "    @Override\n    public Flux<NumericResponse<KeyCommand, Long>> pTtl(Publisher<KeyCommand> commands) {",
            "    /** PTTL：返回 key 剩余存活毫秒数。 */\n"
            "    @Override\n"
            "    public Flux<NumericResponse<KeyCommand, Long>> pTtl(Publisher<KeyCommand> commands) {",
        ),
        (
            "    @Override\n    public Mono<ValueEncoding> encodingOf(ByteBuffer key) {",
            "    /** OBJECT ENCODING：返回 key 内部编码类型。 */\n"
            "    @Override\n"
            "    public Mono<ValueEncoding> encodingOf(ByteBuffer key) {",
        ),
        (
            "    @Override\n    public Mono<Duration> idletime(ByteBuffer key) {",
            "    /** OBJECT IDLETIME：返回 key 空闲秒数并包装为 {@link Duration}。 */\n"
            "    @Override\n"
            "    public Mono<Duration> idletime(ByteBuffer key) {",
        ),
        (
            "    @Override\n    public Mono<Long> refcount(ByteBuffer key) {",
            "    /** OBJECT REFCOUNT：返回 key 的引用计数。 */\n"
            "    @Override\n"
            "    public Mono<Long> refcount(ByteBuffer key) {",
        ),
    ]
)

W13A_REPLACEMENTS["RedissonReactiveKeyCommands.java"] = _key_reps
