"""Chinese annotation replacements for Redisson 4.7.0 wave-28a spring-data-32/33 [0:15]."""
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


W23A = _load("wave23a_replacements_redisson.py", "W23A_REPLACEMENTS")

W28A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = dict(W23A)

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

# spring-data-32/33: ZSET tuple type is org.springframework.data.redis.connection.zset.Tuple.
W28A_REPLACEMENTS["ScoredSortedSetReplayDecoderV2.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 单条 member/score 对解码为 {@link Tuple}（V2 接口）。\n"
        " * <p>适用于仅含一对元素的 ZSET 命令响应；奇数下标以 {@link DoubleCodec} 解析 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);",
        "        // 偶数下标 member 走默认 Codec 解码。\n"
        "        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);",
    ),
    (
        "    @Override\n    public Tuple decode(List<Object> parts, State state) {",
        "    /** 从两元素列表构造 {@link DefaultTuple}。 */\n"
        "    @Override\n"
        "    public Tuple decode(List<Object> parts, State state) {",
    ),
]

W28A_REPLACEMENTS["ScoredSortedSingleBlockingReplayDecoder.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 单条阻塞有序集合弹出解码器：将 BZPOPMIN/BZPOPMAX 等单 key 响应\n"
        " * （key、member、score 三元素）解析为一条 {@link Tuple}。\n"
        " * <p>{@code paramNum == 2} 时以 {@link DoubleCodec} 解析 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        if (paramNum == 2) {",
        "        // 响应第三项为 score，使用 DoubleCodec。\n"
        "        if (paramNum == 2) {",
    ),
    (
        "    @Override\n    public Tuple decode(List<Object> parts, State state) {",
        "    /** 从 parts[1] member 与 parts[2] score 构造 {@link DefaultTuple}（parts[0] 为 key）。 */\n"
        "    @Override\n"
        "    public Tuple decode(List<Object> parts, State state) {",
    ),
]
