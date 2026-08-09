"""Chinese annotation replacements for Redisson 4.7.0 wave-14b spring-data-22 reactive [15:30]."""
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
W13A = _load("wave13a_replacements_redisson.py", "W13A_REPLACEMENTS")

W14B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- spring-data-22: reuse wave-13a (sources identical to spring-data-21) ---
for _key in (
    "RedissonReactiveClusterHyperLogLogCommands.java",
    "RedissonReactiveClusterKeyCommands.java",
    "RedissonReactiveClusterListCommands.java",
    "RedissonReactiveClusterNumberCommands.java",
    "RedissonReactiveClusterServerCommands.java",
    "RedissonReactiveClusterSetCommands.java",
    "RedissonReactiveClusterStringCommands.java",
    "RedissonReactiveClusterZSetCommands.java",
    "RedissonReactiveGeoCommands.java",
    "RedissonReactiveHashCommands.java",
    "RedissonReactiveHyperLogLogCommands.java",
    "RedissonReactiveKeyCommands.java",
    "RedissonReactiveListCommands.java",
):
    W14B_REPLACEMENTS[_key] = W13A[_key]

W14B_REPLACEMENTS["RedissonReactiveNumberCommands.java"] = W12A["RedissonReactiveNumberCommands.java"]

# --- spring-data-22 only: cluster Stream adapter ---
W14B_REPLACEMENTS["RedissonReactiveClusterStreamCommands.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集群模式下 Spring Data Redis 响应式 Stream 命令适配器。\n"
        " * <p>继承 {@link RedissonReactiveStreamCommands} 并实现 {@link ReactiveClusterStreamCommands}，\n"
        "在集群拓扑下复用单机响应式命令实现。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveClusterStreamCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveClusterStreamCommands(CommandReactiveExecutor executorService) {",
    ),
]
