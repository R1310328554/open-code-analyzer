"""Chinese annotation replacements for Redisson 4.7.0 wave-31a spring-data-34 [0:15]."""
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
W23A = _load("wave23a_replacements_redisson.py", "W23A_REPLACEMENTS")
W27B = _load("wave27b_replacements_redisson.py", "W27B_REPLACEMENTS")
W28A = _load("wave28a_replacements_redisson.py", "W28A_REPLACEMENTS")

_SD34 = "redisson-spring/redisson-spring-data/redisson-spring-data-34/src/main/java/org/redisson/spring/data/connection/"

W31A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-34: identical to spring-data-33 for reactive connection/commands.
for _name in (
    "RedissonReactiveRedisClusterConnection.java",
    "RedissonReactiveRedisConnection.java",
    "RedissonReactiveScriptingCommands.java",
    "RedissonReactiveServerCommands.java",
    "RedissonReactiveSetCommands.java",
    "RedissonReactiveSubscription.java",
    "RedissonSentinelConnection.java",
    "RedissonSubscription.java",
    "ScoredSortedListReplayDecoder.java",
    "ScoredSortedSetBlockingReplayDecoder.java",
):
    W31A_REPLACEMENTS[_name] = W27B[_name]
    W31A_REPLACEMENTS[_SD34 + _name] = W27B[_name]

# spring-data-34: ZSET replay decoders (zset.Tuple package since spring-data-30).
W31A_REPLACEMENTS["ScoredSortedSetReplayDecoder.java"] = W23A["ScoredSortedSetReplayDecoder.java"]
W31A_REPLACEMENTS[_SD34 + "ScoredSortedSetReplayDecoder.java"] = W23A["ScoredSortedSetReplayDecoder.java"]
W31A_REPLACEMENTS["ScoredSortedSingleReplayDecoder.java"] = W23A["ScoredSortedSingleReplayDecoder.java"]
W31A_REPLACEMENTS[_SD34 + "ScoredSortedSingleReplayDecoder.java"] = W23A["ScoredSortedSingleReplayDecoder.java"]
W31A_REPLACEMENTS["ScoredSortedSetReplayDecoderV2.java"] = W28A["ScoredSortedSetReplayDecoderV2.java"]
W31A_REPLACEMENTS[_SD34 + "ScoredSortedSetReplayDecoderV2.java"] = W28A["ScoredSortedSetReplayDecoderV2.java"]
W31A_REPLACEMENTS["ScoredSortedSingleBlockingReplayDecoder.java"] = W28A[
    "ScoredSortedSingleBlockingReplayDecoder.java"
]
W31A_REPLACEMENTS[_SD34 + "ScoredSortedSingleBlockingReplayDecoder.java"] = W28A[
    "ScoredSortedSingleBlockingReplayDecoder.java"
]

# spring-data-34: convertors identical to spring-data-26/33.
W31A_REPLACEMENTS["SecondsConvertor.java"] = W11A["SecondsConvertor.java"]
W31A_REPLACEMENTS[_SD34 + "SecondsConvertor.java"] = W11A["SecondsConvertor.java"]
