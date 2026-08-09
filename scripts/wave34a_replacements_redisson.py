"""Chinese annotation replacements for Redisson 4.7.0 wave-34a spring-data-40 [0:15]."""
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


W27B = _load("wave27b_replacements_redisson.py", "W27B_REPLACEMENTS")
W30B = _load("wave30b_replacements_redisson.py", "W30B_REPLACEMENTS")
W31A = _load("wave31a_replacements_redisson.py", "W31A_REPLACEMENTS")

_SD40 = "redisson-spring/redisson-spring-data/redisson-spring-data-40/src/main/java/org/redisson/spring/data/connection/"

W34A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-40: cluster string/zset/geo identical to spring-data-34/35 (wave-30b/27a).
for _name in (
    "RedissonReactiveClusterStringCommands.java",
    "RedissonReactiveClusterZSetCommands.java",
    "RedissonReactiveGeoCommands.java",
):
    W34A_REPLACEMENTS[_name] = W30B[_name]
    W34A_REPLACEMENTS[_SD40 + _name] = W30B[_name]

# spring-data-40: hyperloglog/list/number/pubsub identical to spring-data-34/35 (wave-30b/27b).
for _name in (
    "RedissonReactiveHyperLogLogCommands.java",
    "RedissonReactiveListCommands.java",
    "RedissonReactiveNumberCommands.java",
    "RedissonReactivePubSubCommands.java",
):
    W34A_REPLACEMENTS[_name] = W30B[_name]
    W34A_REPLACEMENTS[_SD40 + _name] = W30B[_name]

# spring-data-40: reactive connection/commands identical to spring-data-34/35 (wave-31a/27b).
for _name in (
    "RedissonReactiveRedisClusterConnection.java",
    "RedissonReactiveRedisConnection.java",
    "RedissonReactiveScriptingCommands.java",
    "RedissonReactiveServerCommands.java",
    "RedissonReactiveSubscription.java",
    "RedissonSentinelConnection.java",
):
    W34A_REPLACEMENTS[_name] = W31A[_name]
    W34A_REPLACEMENTS[_SD40 + _name] = W31A[_name]

# spring-data-40: RedissonSubscription javadoc without trailing space (use W27B directly).
W34A_REPLACEMENTS["RedissonSubscription.java"] = W27B["RedissonSubscription.java"]
W34A_REPLACEMENTS[_SD40 + "RedissonSubscription.java"] = W27B["RedissonSubscription.java"]

# spring-data-40: RedissonReactiveSetCommands adds sInterCard (SINTERCARD).
_SD40_SET_EXTRA: list[tuple[str, str]] = [
    (
        '    private static final RedisStrictCommand<Long> SINTERCARD = new RedisStrictCommand<>("SINTERCARD");',
        "    /** SINTERCARD 命令：仅返回交集基数，不物化结果集。 */\n"
        '    private static final RedisStrictCommand<Long> SINTERCARD = new RedisStrictCommand<>("SINTERCARD");',
    ),
    (
        "    @Override\n    public Flux<NumericResponse<SInterCardCommand, Long>> sInterCard(Publisher<SInterCardCommand> commands) {",
        "    /** SINTERCARD：返回多个集合交集的元素个数。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<SInterCardCommand, Long>> sInterCard(Publisher<SInterCardCommand> commands) {",
    ),
]

W34A_REPLACEMENTS["RedissonReactiveSetCommands.java"] = list(
    W31A["RedissonReactiveSetCommands.java"]
) + _SD40_SET_EXTRA
W34A_REPLACEMENTS[_SD40 + "RedissonReactiveSetCommands.java"] = W34A_REPLACEMENTS[
    "RedissonReactiveSetCommands.java"
]
