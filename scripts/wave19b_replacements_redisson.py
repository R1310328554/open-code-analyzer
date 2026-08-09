"""Chinese annotation replacements for Redisson 4.7.0 wave-19b spring-data-25/26 [15:30]."""
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


W11A = _load("wave11a_replacements_redisson.py", "W11A_REPLACEMENTS")
W12B = _load("wave12b_replacements_redisson.py", "W12B_REPLACEMENTS")
W15A = _load("wave15a_replacements_redisson.py", "W15A_REPLACEMENTS")
W17A = _load("wave17a_replacements_redisson.py", "W17A_REPLACEMENTS")

W19B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-25: reuse wave-15a (sources identical to spring-data-22/23/24 for these files).
for _key in (
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
    W19B_REPLACEMENTS[_key] = list(W15A[_key])

# spring-data-25: TIME with TimeUnit conversion (added vs spring-data-24).
W19B_REPLACEMENTS["RedissonReactiveServerCommands.java"].append(
    (
        "    @Override\n    public Mono<Long> time(TimeUnit timeUnit) {",
        "    /** TIME：读取服务器时间并按 {@link TimeUnit} 转换（毫秒基准）。 */\n"
        "    @Override\n"
        "    public Mono<Long> time(TimeUnit timeUnit) {",
    )
)

# spring-data-26: identical to spring-data-22/24/25 counterparts.
W19B_REPLACEMENTS["BinaryConvertor.java"] = W11A["BinaryConvertor.java"]
W19B_REPLACEMENTS["ByteBufferGeoResultsDecoder.java"] = W12B["ByteBufferGeoResultsDecoder.java"]
W19B_REPLACEMENTS["DataTypeConvertor.java"] = W17A["DataTypeConvertor.java"]
