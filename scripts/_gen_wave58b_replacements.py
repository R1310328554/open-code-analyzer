#!/usr/bin/env python3
"""Generate wave58b_replacements_redisson.py for json/bitvector/keys/map api [15:30]."""
from __future__ import annotations

import importlib.util
import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave58b_replacements_redisson.py"
SCRIPTS = ROOT / "scripts"
_A = "redisson/src/main/java/org/redisson/api/"
FILES = [
    ln.strip()
    for ln in Path("/tmp/re58b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

CLASS_OVERRIDES: dict[str, list[tuple[str, str]]] = {
    f"{_A}RBitSetRx.java": [
        (
            "/**\n * RxJava2 interface for BitSet object\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * BitSet 分布式对象的 RxJava3 API {@link RBitSetRx}。\n"
            " * <p>基于 Redis 字符串位图，支持 GET/SET/位运算及 BITFIELD 子命令。\n"
            " *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RBitVectorStore.java": [
        (
            "/**\n * Distributed store of 64-bit vectors mapped by keys,\n"
            " * with bitmask-based filtering.\n * <p>\n * This object is thread-safe.\n *\n"
            " * @param <K> the type of keys identifying stored vectors\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 按键存储 64 位向量的分布式集合，支持位掩码过滤与匹配查询。\n"
            " * <p>线程安全。\n *\n * @param <K> 标识向量的键类型\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RBitVectorStoreAsync.java": [
        (
            "/**\n * Distributed store of 64-bit vectors mapped by keys,\n"
            " * with bitmask-based filtering.\n * <p>\n * This object is thread-safe.\n *\n"
            " * @param <K> the type of keys identifying stored vectors\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 64 位向量存储 {@link RBitVectorStore} 异步 API；各方法返回 {@link RFuture}。\n"
            " * <p>支持位掩码过滤、原子位更新与匹配迭代。\n *\n * @param <K> 标识向量的键类型\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RBitVectorStoreReactive.java": [
        (
            "/**\n * Distributed store of 64-bit vectors mapped by keys,\n"
            " * with bitmask-based filtering.\n * <p>\n * This object is thread-safe.\n *\n"
            " * @param <K> the type of keys identifying stored vectors\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 64 位向量存储 {@link RBitVectorStore} Reactor 响应式 API。\n"
            " * <p>支持位掩码过滤、原子位更新与匹配迭代。\n *\n * @param <K> 标识向量的键类型\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RBitVectorStoreRx.java": [
        (
            "/**\n * Distributed store of fixed-width 64-bit vectors mapped by keys,\n"
            " * with bitmask-based filtering.\n * <p>\n * This object is thread-safe.\n *\n"
            " * @param <K> the type of keys identifying stored vectors\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 64 位向量存储 {@link RBitVectorStore} RxJava3 API。\n"
            " * <p>支持位掩码过滤、原子位更新与匹配迭代。\n *\n * @param <K> 标识向量的键类型\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RJsonBucket.java": [
        (
            "/**\n * Redis JSON datatype holder. Data is stored as JSON object in Redis\n *\n"
            " * @author Nikita Koksharov\n * @param <V> the type of object\n */",
            "/**\n * Redis JSON 数据类型持有者 {@link RJsonBucket}；数据以 JSON 对象存储。\n"
            " * <p>支持 JSONPath 读写、数组/字符串操作与 CAS 更新。\n *\n"
            " * @author Nikita Koksharov\n * @param <V> 对象类型\n */",
        ),
    ],
    f"{_A}RJsonBucketAsync.java": [
        (
            "/**\n * Redis JSON datatype interface. Data is stored as JSON object in Redis\n *\n"
            " * @author Nikita Koksharov\n * @param <V> the type of object\n */",
            "/**\n * {@link RJsonBucket} 异步 API；各方法返回 {@link RFuture}。\n"
            " * <p>数据以 Redis JSON 对象存储，支持 JSONPath 部分更新。\n *\n"
            " * @author Nikita Koksharov\n * @param <V> 对象类型\n */",
        ),
    ],
    f"{_A}RJsonBucketReactive.java": [
        (
            "/**\n * Redis JSON datatype holder. Data is stored as JSON object in Redis\n *\n"
            " * @author Nikita Koksharov\n * @param <V> the type of object\n */",
            "/**\n * {@link RJsonBucket} Reactor 响应式 API。\n"
            " * <p>数据以 Redis JSON 对象存储，支持 JSONPath 部分更新。\n *\n"
            " * @author Nikita Koksharov\n * @param <V> 对象类型\n */",
        ),
    ],
    f"{_A}RJsonBucketRx.java": [
        (
            "/**\n * Redis JSON datatype holder. Data is stored as JSON object in Redis\n *\n"
            " * @author Nikita Koksharov\n * @param <V> the type of object\n */",
            "/**\n * {@link RJsonBucket} RxJava3 API。\n"
            " * <p>数据以 Redis JSON 对象存储，支持 JSONPath 部分更新。\n *\n"
            " * @author Nikita Koksharov\n * @param <V> 对象类型\n */",
        ),
    ],
    f"{_A}RJsonStore.java": [
        (
            "/**\n * JSON Store which stores each entry as key and value. Both are POJO objects.\n"
            " * Value is stored as JSON datatype in Redis.\n * <p>\n"
            " * The implementation is available in Redisson PRO only.\n *\n * @author Nikita Koksharov\n *\n"
            " * @param <K> the type of key\n * @param <V> the type of value\n *\n */",
            "/**\n * JSON Store {@link RJsonStore}：每条目以键值对存储，值以 Redis JSON 类型持久化。\n"
            " * <p>实现仅 Redisson PRO 提供；支持 JSONPath、TTL 与批量操作。\n *\n"
            " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RJsonStoreAsync.java": [
        (
            "/**\n * JSON Store which stores each entry as key and value. Both are POJO objects.\n"
            " * Value is stored as JSON datatype in Redis.\n * <p>\n"
            " * The implementation is available in Redisson PRO only.\n *\n * @author Nikita Koksharov\n *\n"
            " * @param <K> the type of key\n * @param <V> the type of value\n *\n */",
            "/**\n * {@link RJsonStore} 异步 API；各方法返回 {@link RFuture}。\n"
            " * <p>实现仅 Redisson PRO 提供。\n *\n"
            " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RJsonStoreReactive.java": [
        (
            "/**\n * JSON Store which stores each entry as key and value. Both are POJO objects.\n"
            " * Value is stored as JSON datatype in Redis.\n * <p>\n"
            " * The implementation is available in Redisson PRO only.\n *\n * @author Nikita Koksharov\n *\n"
            " * @param <K> the type of key\n * @param <V> the type of value\n *\n */",
            "/**\n * {@link RJsonStore} Reactor 响应式 API。\n"
            " * <p>实现仅 Redisson PRO 提供。\n *\n"
            " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RJsonStoreRx.java": [
        (
            "/**\n * JSON Store which stores each entry as key and value. Both are POJO objects.\n"
            " * Value is stored as JSON datatype in Redis.\n * <p>\n"
            " * The implementation is available in Redisson PRO only.\n *\n * @author Nikita Koksharov\n *\n"
            " * @param <K> the type of key\n * @param <V> the type of value\n *\n */",
            "/**\n * {@link RJsonStore} RxJava3 API。\n"
            " * <p>实现仅 Redisson PRO 提供。\n *\n"
            " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RKeys.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * Redis 键空间管理 API {@link RKeys}。\n"
            " * <p>封装 SCAN、过期/TTL、MIGRATE/COPY、跨库 MOVE 及键计数等操作。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RMap.java": [
        (
            "/**\n * Redis based implementation of {@link java.util.concurrent.ConcurrentMap}\n"
            " * and {@link java.util.Map}\n * <p>\n"
            " * This map uses serialized state of key instead of hashCode or equals methods.\n"
            " * This map doesn't allow to store <code>null</code> as key or value.\n *\n"
            " * @author Nikita Koksharov\n *\n * @param <K> map key\n * @param <V> value\n */",
            "/**\n * 基于 Redis 的 {@link java.util.concurrent.ConcurrentMap} 与 {@link java.util.Map} API。\n"
            " * <p>使用键序列化状态而非 hashCode/equals；不允许 {@code null} 键或值。\n"
            " * 支持 MapLoader/MapWriter、MapReduce 与 per-key 分布式锁。\n *\n"
            " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
}

PARAM_RETURN: dict[str, str] = {
    "@param <K> map key": "@param <K> 键类型",
    "@param <V> value": "@param <V> 值类型",
    "@param <V> the type of object": "@param <V> 对象类型",
    "@param <T> the type of object": "@param <T> 对象类型",
    "@param <K> the type of key": "@param <K> 键类型",
    "@param <V> the type of value": "@param <V> 值类型",
    "@param key - map key": "@param key 映射键",
    "@param key the key": "@param key 键",
    "@param value - map value": "@param value 映射值",
    "@param keys - map keys": "@param keys 键集合",
    "@param name of object": "@param name 对象名称",
    "@param limit - limit of keys amount": "@param limit 键数量上限",
    "@param pattern - match pattern": "@param pattern 匹配模式",
    "@param database - Redis database number": "@param database Redis 数据库编号",
    "@param host - destination host": "@param host 目标主机",
    "@param port - destination port": "@param port 目标端口",
    "@param timeout - maximum idle time": "@param timeout 通信最大空闲毫秒数",
    "@param timeToLive - timeout before object will be deleted": "@param timeToLive 过期时长",
    "@param timeUnit - timeout time unit": "@param timeUnit 时间单位",
    "@param listener object event listener": "@param listener 对象事件监听器",
    "@param listener - object event listener": "@param listener 对象事件监听器",
    "@return listener id": "@return 监听器 ID",
    "@return keys": "@return 键集合",
    "@return values": "@return 值集合",
    "@return map size": "@return 映射大小",
    "@return map entries": "@return 键值对集合",
    "@return previous associated value": "@return 先前关联的值",
    "@return void": "@return 无返回值",
    "@param id the key to test": "@param id 待检测的键",
    "@param id the key whose vector to retrieve": "@param id 待读取的键",
    "@param id     the key under which to store": "@param id 存储键",
    "@param vector the 64-bit vector value": "@param vector 64 位向量值",
    "@param id the key to remove": "@param id 待移除的键",
    "@param mask the bitmask to test against": "@param mask 位掩码",
    "@param mask   the bitmask selecting which bits to compare": "@param mask 参与比较的位掩码",
    "@param target the required bit pattern within the masked positions": "@param target 掩码位上的目标模式",
    "@param args the mask, target, and iteration parameters": "@param args 掩码、目标与迭代参数",
    "@param args the mask and iteration parameters": "@param args 掩码与迭代参数",
    "@param codec object codec": "@param codec 对象编解码器",
    "@param paths JSON paths": "@param paths JSON 路径",
    "@param path JSON path": "@param path JSON 路径",
    "@param key entry key": "@param key 条目键",
    "@param expect the expected value": "@param expect 期望值",
    "@param update the new value": "@param update 新值",
    "@return object": "@return 对象",
    "@return entry value": "@return 条目值",
    "@param chunkSize the number of keys to fetch per round-trip": "@param chunkSize 每轮拉取的键数量",
    "@param replaceExistingValues - <code>true</code> if existed values should be replaced, <code>false</code> otherwise.": "@param replaceExistingValues 是否替换已有值",
    "@param parallelism - parallelism level, used to increase speed of process execution": "@param parallelism 并行度",
    "@param bitIndex - index of bit": "@param bitIndex 位索引",
    "@param bitSetNames - name of stored bitsets": "@param bitSetNames 位集名称",
    "@param bitSetNames name of stored bitsets": "@param bitSetNames 位集名称",
    "@param size - size of signed number up to 64 bits": "@param size 有符号数位宽（最多 64 位）",
    "@param offset - offset of signed number": "@param offset 有符号数偏移",
    "@param value - value of signed number": "@param value 有符号数值",
    "@return signed number": "@return 有符号整数",
    "@return previous value of signed number": "@return 旧有符号整数",
    "@param increment - increment value": "@param increment 增量",
    "@return result value": "@return 结果值",
    "@param args - bitfield arguments": "@param args BITFIELD 参数",
    "@return result values": "@return 结果值列表",
    "@return number of bits": "@return 位数",
    "@return length in bytes of the destination key": "@return 目标键字节长度",
    "@param fromIndex inclusive": "@param fromIndex 起始索引（含）",
    "@param toIndex exclusive": "@param toIndex 结束索引（不含）",
    "@param value true = 1, false = 0": "@param value true=1，false=0",
    "@param bs - BitSet source": "@param bs 源 BitSet",
    "@param migrateArgs migrateArgs": "@param migrateArgs 迁移参数",
}

# Phrase-level translations (longest first at runtime)
TRANSLATIONS: dict[str, str] = {}


def t(en: str, cn: str) -> None:
    TRANSLATIONS[en.strip()] = cn.strip()


# --- shared phrases ---
t("Use {@link #getKeys(KeysScanOptions)} instead.", "请改用 {@link #getKeys(KeysScanOptions)}。")
t("Use {@link #expire(Duration, String...)} instead.", "请改用 {@link #expire(Duration, String...)}。")
t("Use {@link #expireAt(Instant, String...)} instead.", "请改用 {@link #expireAt(Instant, String...)}。")
t("Use {@link #setIfAbsent(String, Object)} instead", "请改用 {@link #setIfAbsent(String, Object)}")
t("Adds object event listener", "注册对象事件监听器。")
t("Read all keys at once", "一次性读取全部键。")
t("Read all values at once", "一次性读取全部值。")
t("Read all map entries at once", "一次性读取全部键值对。")
t("Returns key set of this map.", "返回本 Map 的键集合视图。")
t("Returns values of this map.", "返回本 Map 的值集合视图。")
t("Returns map entries set of this map.", "返回本 Map 的键值对集合视图。")
t("Returns size of this map.", "返回本 Map 的元素数量。")
t("Returns <code>true</code> if this map is empty", "本 Map 为空时返回 {@code true}。")
t("Returns <code>true</code> if this map contains map entry\n     * mapped by specified <code>key</code>, otherwise <code>false</code>", "若包含指定 {@code key} 的映射条目则返回 {@code true}，否则 {@code false}。")
t("Returns <code>RLock</code> instance associated with key", "返回与键关联的 {@link RLock}。")
t("Returns <code>RReadWriteLock</code> instance associated with key", "返回与键关联的 {@link RReadWriteLock}。")
t("Returns <code>RSemaphore</code> instance associated with key", "返回与键关联的 {@link RSemaphore}。")
t("Returns <code>RPermitExpirableSemaphore</code> instance associated with key", "返回与键关联的 {@link RPermitExpirableSemaphore}。")
t("Returns <code>RCountDownLatch</code> instance associated with key", "返回与键关联的 {@link RCountDownLatch}。")
t("Move object to another database", "将对象移动到另一 Redis 数据库。")
t("Transfer object from source Redis instance to destination Redis instance", "将对象从源 Redis 实例迁移到目标实例。")
t("Copy object from source Redis instance to destination Redis instance", "将对象从源 Redis 实例复制到目标实例。")
t("Returns number of keys", "返回键数量。")
t("Returns all keys matching pattern", "返回匹配模式的所有键。")
t("Returns {@code true} if a vector is stored under the given key.", "若给定键下已存储向量则返回 {@code true}。")
t("Returns the number of vectors currently stored.", "返回当前存储的向量数量。")
t("Returns the vector stored under the given key, or {@code null} if no vector\n     * is stored for that key.", "返回给定键下的向量；无向量时返回 {@code null}。")
t("Stores a vector under the given key, overwriting any previous value.", "在给定键下存储向量，覆盖已有值。")
t("Removes the vector stored under the given key.", "移除给定键下存储的向量。")
t("Get Json object/objects by JSONPath", "按 JSONPath 获取 JSON 对象。")
t("Sets Json object by JSONPath only if previous value is empty", "仅当 JSONPath 处原值为空时写入 JSON 对象。")
t("Sets Json object by JSONPath only if previous value is non-empty", "仅当 JSONPath 处原值非空时写入 JSON 对象。")
t("Atomically sets the value to the given updated value\n     * by given JSONPath, only if serialized state of\n     * the current value equals to serialized state of the expected value.", "仅当 JSONPath 处当前值序列化状态等于期望值时，原子写入新值。")
t("Loads all map entries to this Redis map using {@link org.redisson.api.map.MapLoader}.", "使用 {@link org.redisson.api.map.MapLoader} 加载全部映射条目。")
t("Loads map entries using {@link org.redisson.api.map.MapLoader} whose keys are listed in defined <code>keys</code> parameter.", "使用 {@link org.redisson.api.map.MapLoader} 加载指定 {@code keys} 的条目。")
t("Returns the value mapped by defined <code>key</code> or {@code null} if value is absent.", "返回 {@code key} 映射的值；不存在时返回 {@code null}。")
t("Stores the specified <code>value</code> mapped by specified <code>key</code>.\n     * Returns previous value if map entry with specified <code>key</code> already existed.", "存储 {@code key}-{@code value}；键已存在时返回旧值。")
t("Returns <code>RMapReduce</code> object associated with this map", "返回与本 Map 关联的 {@link RMapReduce}。")
t("Returns signed number at specified\n     * <code>offset</code> and <code>size</code>", "读取指定位域的有符号整数。")
t("Returns unsigned number at specified\n     * <code>offset</code> and <code>size</code>", "读取指定位域的无符号整数。")
t("Executes BITFIELD command with multiple subcommands\n     * and returns result list in the same order.", "执行 BITFIELD 多子命令并按相同顺序返回结果。")
t("Copy bits state of source BitSet object to this object", "将源 BitSet 的位状态复制到本对象。")
t("Executes NOT operation over all bits", "对全部位执行 NOT 运算。")
t("Returns number of set bits.", "返回置 1 的位数。")
t("Returns the number of bits set to one.", "返回值为 1 的位数（基数）。")
t("Set all bits to zero", "将全部位清零。")
t("Stores result into this object.", "结果写回本对象。")
t("Clears json container.", "清空 JSON 容器。")
t("Compatible only with enhanced syntax starting with '$' character.", "仅兼容以 {@code $} 开头的增强 JSONPath 语法。")
t("Requires <b>Redis 8.8.0 or higher.</b>", "需要 <b>Redis 8.8.0 及以上</b>。")
t("-1 means object not found.", "返回 {@code -1} 表示未找到。")


def load_bitset_reuse() -> dict[str, list[tuple[str, str]]]:
    spec = importlib.util.spec_from_file_location(
        "w58a", SCRIPTS / "wave58a_replacements_redisson.py"
    )
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return mod.W58A_REPLACEMENTS.get("RBitSet.java", [])


def translate_javadoc(block: str) -> str | None:
    if "Copyright" in block and "Licensed under the Apache License" in block:
        return None
    result = block
    changed = False
    for en, cn in sorted(TRANSLATIONS.items(), key=lambda x: -len(x[0])):
        if en in result:
            result = result.replace(en, cn, 1)
            changed = True
    for en, cn in PARAM_RETURN.items():
        if en in result:
            result = result.replace(en, cn)
            changed = True
    if re.search(r"[\u4e00-\u9fff]", result) and result != block:
        return result
    return result if changed else None


def collect_replacements(rel: str, bitset_reuse: list[tuple[str, str]]) -> list[tuple[str, str]]:
    text = (ORIG / rel).read_text(encoding="utf-8")
    reps: list[tuple[str, str]] = []
    seen: set[str] = set()
    override_olds = {o for o, _ in CLASS_OVERRIDES.get(rel, [])}

    for old, new in CLASS_OVERRIDES.get(rel, []):
        if old in text and old not in seen:
            reps.append((old, new))
            seen.add(old)

    if Path(rel).name == "RBitSetRx.java":
        for old, new in bitset_reuse:
            if old in override_olds or old in seen:
                continue
            if old in text:
                reps.append((old, new))
                seen.add(old)

    for m in re.finditer(r"/\*\*.*?\*/", text, re.DOTALL):
        old = m.group(0)
        if old in seen or old in override_olds:
            continue
        if "Copyright" in old and "Licensed under the Apache License" in old:
            continue
        new = translate_javadoc(old)
        if new and new != old:
            reps.append((old, new))
            seen.add(old)

    return reps


def emit() -> None:
    bitset_reuse = load_bitset_reuse()
    all_data: dict[str, list[tuple[str, str]]] = {}
    for rel in FILES:
        reps = collect_replacements(rel, bitset_reuse)
        if not reps:
            raise RuntimeError(f"No replacements for {rel}")
        preview = (ORIG / rel).read_text(encoding="utf-8")
        for old, new in reps:
            preview = preview.replace(old, new, 1)
        if len(re.findall(r"[\u4e00-\u9fff]", preview)) < 10:
            raise RuntimeError(f"CJK preview <10 for {rel}")
        all_data[rel] = reps

    lines = [
        '"""Chinese annotation replacements for Redisson 4.7.0 wave-58b api [15:30]."""',
        "from __future__ import annotations",
        "",
        "W58B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
    ]
    emitted_short: set[str] = set()
    for rel, reps in all_data.items():
        short = Path(rel).name
        lines.append(f"    {rel!r}: [")
        for old, new in reps:
            lines.append(f"        ({old!r}, {new!r}),")
        lines.append("    ],")
        if short not in emitted_short:
            same = all(
                all_data[r] == reps for r in all_data if Path(r).name == short and r != rel
            )
            if same:
                lines.append(f"    {short!r}: [")
                for old, new in reps:
                    lines.append(f"        ({old!r}, {new!r}),")
                lines.append("    ],")
                emitted_short.add(short)
    lines.append("}")
    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {OUT} ({len(all_data)} files)")
    for rel, reps in all_data.items():
        print(f"  {rel}: {len(reps)} replacements")


if __name__ == "__main__":
    emit()
