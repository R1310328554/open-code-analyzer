"""Chinese annotation replacements for Redisson 4.7.0 wave-32a spring-data-35 [0:15]."""
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


W30A = _load("wave30a_replacements_redisson.py", "W30A_REPLACEMENTS")
W30B = _load("wave30b_replacements_redisson.py", "W30B_REPLACEMENTS")

_SD35 = "redisson-spring/redisson-spring-data/redisson-spring-data-35/src/main/java/org/redisson/spring/data/connection/"

W32A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-35: connection layer identical to spring-data-34.
for _name in (
    "RedissonConnectionFactory.java",
    "RedissonExceptionConverter.java",
    "RedissonReactiveClusterGeoCommands.java",
    "RedissonReactiveClusterHashCommands.java",
    "RedissonReactiveClusterHyperLogLogCommands.java",
):
    W32A_REPLACEMENTS[_name] = W30A[_name]
    W32A_REPLACEMENTS[_SD35 + _name] = W30A[_name]

# spring-data-35: cluster reactive adapters identical to spring-data-34.
for _name in (
    "RedissonReactiveClusterKeyCommands.java",
    "RedissonReactiveClusterListCommands.java",
    "RedissonReactiveClusterNumberCommands.java",
    "RedissonReactiveClusterServerCommands.java",
    "RedissonReactiveClusterSetCommands.java",
    "RedissonReactiveClusterStreamCommands.java",
    "RedissonReactiveClusterStringCommands.java",
    "RedissonReactiveClusterZSetCommands.java",
    "RedissonReactiveGeoCommands.java",
):
    W32A_REPLACEMENTS[_name] = W30B[_name]
    W32A_REPLACEMENTS[_SD35 + _name] = W30B[_name]

# spring-data-35: Hash adds field-level expiration/TTL commands (HEXPIRE/HPERSIST/HTTL/HPTTL).
_SD35_HASH_EXTRA: list[tuple[str, str]] = [
    (
        "    private static final RedisCommand<List<Long>> HEXPIRE = new RedisCommand<>(\"HEXPIRE\", new ObjectListReplayDecoder<>());",
        "    /** HEXPIRE 命令：为 hash 字段设置过期时间（秒）。 */\n"
        "    private static final RedisCommand<List<Long>> HEXPIRE = new RedisCommand<>(\"HEXPIRE\", new ObjectListReplayDecoder<>());",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<HashExpireCommand, Long>> applyHashFieldExpiration(Publisher<HashExpireCommand> commands) {",
        "    /** 批量为 hash 字段设置过期时间，支持 {@link ExpirationOptions.Condition} 条件。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<HashExpireCommand, Long>> applyHashFieldExpiration(Publisher<HashExpireCommand> commands) {",
    ),
    (
        "    private static final RedisStrictCommand<List<Long>> HPERSIST = new RedisStrictCommand<>(\"HPERSIST\", new ObjectListReplayDecoder<>());",
        "    /** HPERSIST 命令：移除 hash 字段的过期时间，使其持久化。 */\n"
        "    private static final RedisStrictCommand<List<Long>> HPERSIST = new RedisStrictCommand<>(\"HPERSIST\", new ObjectListReplayDecoder<>());",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<HashFieldsCommand, Long>> hPersist(Publisher<HashFieldsCommand> commands) {",
        "    /** HPERSIST：移除指定 hash 字段的 TTL，返回各字段操作结果码。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<HashFieldsCommand, Long>> hPersist(Publisher<HashFieldsCommand> commands) {",
    ),
    (
        "    private static final RedisCommand<List<Long>> HTTL = new RedisCommand<>(\"HTTL\", new ObjectListReplayDecoder<>());",
        "    /** HTTL 命令：查询 hash 字段剩余过期时间（秒）。 */\n"
        "    private static final RedisCommand<List<Long>> HTTL = new RedisCommand<>(\"HTTL\", new ObjectListReplayDecoder<>());",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<HashFieldsCommand, Long>> hTtl(Publisher<HashFieldsCommand> commands) {",
        "    /** HTTL：返回各 hash 字段剩余 TTL（秒），-1 表示无过期，-2 表示字段不存在。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<HashFieldsCommand, Long>> hTtl(Publisher<HashFieldsCommand> commands) {",
    ),
    (
        "    private static final RedisCommand<List<Long>> HPTTL = new RedisCommand<>(\"HPTTL\", new ObjectListReplayDecoder<>());",
        "    /** HPTTL 命令：查询 hash 字段剩余过期时间（毫秒）。 */\n"
        "    private static final RedisCommand<List<Long>> HPTTL = new RedisCommand<>(\"HPTTL\", new ObjectListReplayDecoder<>());",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<HashFieldsCommand, Long>> hpTtl(Publisher<HashFieldsCommand> commands) {",
        "    /** HPTTL：返回各 hash 字段剩余 TTL（毫秒）。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<HashFieldsCommand, Long>> hpTtl(Publisher<HashFieldsCommand> commands) {",
    ),
]

W32A_REPLACEMENTS["RedissonReactiveHashCommands.java"] = list(
    W30B["RedissonReactiveHashCommands.java"]
) + _SD35_HASH_EXTRA
W32A_REPLACEMENTS[_SD35 + "RedissonReactiveHashCommands.java"] = W32A_REPLACEMENTS[
    "RedissonReactiveHashCommands.java"
]
