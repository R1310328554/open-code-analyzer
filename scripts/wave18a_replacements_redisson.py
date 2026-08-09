"""Chinese annotation replacements for Redisson 4.7.0 wave-18a spring-data-24/25 [0:15]."""
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
W16B = _load("wave16b_replacements_redisson.py", "W16B_REPLACEMENTS")

W18A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-24 cluster connection: extended CLUSTER admin API (same as spring-data-23).
W18A_REPLACEMENTS["RedissonReactiveRedisClusterConnection.java"] = W16B[
    "RedissonReactiveRedisClusterConnection.java"
]

# spring-data-24: reuse wave-15a (sources identical to spring-data-22/23 for these files).
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
    W18A_REPLACEMENTS[_key] = W15A[_key]

# spring-data-25: identical to spring-data-22/24 counterparts.
W18A_REPLACEMENTS["BinaryConvertor.java"] = W11A["BinaryConvertor.java"]
W18A_REPLACEMENTS["ByteBufferGeoResultsDecoder.java"] = W12B["ByteBufferGeoResultsDecoder.java"]
