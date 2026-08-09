#!/usr/bin/env python3
"""Generate wave43b_replacements_redisson.py from originals with exact javadoc matching."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave43b_replacements_redisson.py"
_A = "redisson/src/main/java/org/redisson/api/"

FILES = [ln.strip() for ln in Path("/tmp/re43b.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]

# Map first meaningful description line -> Chinese replacement body (without /** */ wrapper)
# For multi-line keys use full normalized key from extractor

TRANSLATIONS: dict[str, str] = {}


def t(en: str, cn: str) -> None:
    TRANSLATIONS[en.strip()] = cn.strip()


# class-level and special full-block overrides keyed by unique substring
FULL_BLOCK: dict[str, tuple[str, str]] = {}


def full(sub: str, cn_block: str) -> None:
    FULL_BLOCK[sub] = (sub, cn_block)


def extract_javadocs(text: str) -> list[str]:
    return re.findall(r"/\*\*.*?\*/", text, re.DOTALL)


def main_desc(jdoc: str) -> str:
    for line in jdoc.splitlines():
        s = line.strip()
        if s.startswith("*") and not s.startswith("* @") and s not in ("*/", "/**", "*"):
            return s[1:].strip()
    return ""


def translate_jdoc(jdoc: str, rel: str) -> str | None:
    for sub, (_, cn) in FULL_BLOCK.items():
        if sub in jdoc:
            indent = re.match(r"(\s*)/\*\*", jdoc)
            ind = indent.group(1) if indent else "    "
            return f"{ind}/**{cn}\n{ind} */"

    desc = main_desc(jdoc)
    if desc in TRANSLATIONS:
        cn_desc = TRANSLATIONS[desc]
        lines = jdoc.splitlines()
        out = []
        replaced = False
        for line in lines:
            s = line.strip()
            if (
                not replaced
                and s.startswith("*")
                and not s.startswith("* @")
                and s not in ("*/", "/**", "*")
            ):
                indent = line[: len(line) - len(line.lstrip())]
                out.append(f"{indent}* {cn_desc}")
                replaced = True
            else:
                # translate @param / @return descriptions inline
                m = re.match(r"(\s*\* @param \S+)(.*)", line)
                if m:
                    param = line.split()[2]
                    pmap = PARAM_MAP.get(param)
                    if pmap:
                        out.append(f"{m.group(1)} {pmap}")
                        continue
                m2 = re.match(r"(\s*\* @return)(.*)", line)
                if m2 and desc in RETURN_MAP:
                    out.append(f"{m2.group(1)} {RETURN_MAP[desc]}")
                    continue
                out.append(line)
        if not replaced:
            return None
        return "\n".join(out)

    # try partial multi-line keys
    for key, cn in TRANSLATIONS.items():
        if "\n" in key and key in jdoc:
            return jdoc.replace(key, cn)

    return None


PARAM_MAP: dict[str, str] = {
    "- value to set": "待设置的值",
    "value to set": "待设置的值",
    "expiration duration": "过期时长",
    "- time to live interval": "存活时间",
    "- unit of time to live interval": "时间单位",
    "the expected value": "期望值",
    "the new value": "新值",
    "args": "参数",
    "comparison arguments": "比较参数",
    "- object event listener": "事件监听器",
    "listener - object event listener": "事件监听器",
    "second bucket": "另一 Bucket 的名称",
    "name second bucket": "另一 Bucket 的名称",
    "keys - keys": "Redis 键列表",
    "keys": "Redis 键列表",
    "buckets - map of buckets": "键到值的映射",
    "- args": "写入参数",
    "buffer capacity": "缓冲区容量",
    "value to add": "待追加的值",
    "values to add": "待追加的值集合",
    "ring size, becomes the new buffer capacity": "环大小（新容量）",
    "values to write, must contain at least one value": "待写入的值（至少一个）",
    "ring index": "环槽下标",
    "number of values to return": "返回数量",
    "start ring index": "起始环槽下标",
    "end ring index": "结束环槽下标",
    "value to match": "待匹配的值",
    "ring indexes": "环槽下标集合",
    "name name of object": "对象名称",
    "name of object": "对象名称",
    "codec codec for values": "值编解码器",
    "codec for values": "值编解码器",
    "name of stream": "Stream 名称",
    "codec codec for entry": "条目编解码器",
    "codec for entry": "条目编解码器",
    "codec codec for message": "消息编解码器",
    "codec for message": "消息编解码器",
    "name name of queue": "队列名称",
    "codec queue objects codec": "队列元素编解码器",
    "codec deque objects codec": "双端队列元素编解码器",
    "codec codec for value": "成员编解码器",
    "codec for value": "成员编解码器",
    "- element to add": "待添加元素",
    "- collection of elements to add": "待添加元素集合",
    "e - element to add": "待添加元素",
    "c - collection of elements to add": "待添加元素集合",
}

RETURN_MAP: dict[str, str] = {
    "Returns size of object in bytes": "对象字节大小",
    "Returns number of elements in this collection.": "元素数量",
    "Returns iterator over collection elements": "元素流",
}


# --- populate translations ---
t("Returns size of object in bytes", "返回对象序列化后的字节大小。")
t("Sets value only if object holder doesn't exist.", "仅当 Redis 键不存在时设置值（NX）。")
t("Sets value with defined duration only if object holder doesn't exist.", "仅当键不存在时设置值并指定过期时长。")
t("Use {@link #setIfAbsent(Object)} instead", "已废弃，请改用 {@link #setIfAbsent(Object)}。")
t("Use {@link #setIfAbsent(Object, Duration)} instead", "已废弃，请改用 {@link #setIfAbsent(Object, Duration)}。")
t("Sets value only if it's already exists.", "仅当键已存在时更新值（XX）。")
t("Use {@link #setIfExists(Object, Duration)} instead", "已废弃，请改用 {@link #setIfExists(Object, Duration)}。")
t(
    "Sets <code>value</code> with expiration <code>duration</code> only if object holder already exists.",
    "仅当键已存在时设置 {@code value} 并指定过期时长 {@code duration}。",
)
t(
    "Atomically sets the value to the given updated value\n     * only if serialized state of the current value equals \n     * to serialized state of the expected value.",
    "仅当当前值与 {@code expect} 序列化结果一致时，原子设置为 {@code update}（CAS）。",
)
t(
    "Retrieves current element in the holder and replaces it with <code>newValue</code>.",
    "读取当前值并以 {@code newValue} 替换，返回旧值。",
)
t("Use {@link #getAndSet(Object, Duration)} instead", "已废弃，请改用 {@link #getAndSet(Object, Duration)}。")
t(
    "Retrieves current element in the holder and replaces it\n     * with <code>value</code> with defined expiration <code>duration</code>.",
    "读取当前值并以 {@code value} 替换，同时设置过期时长 {@code duration}。",
)
t("Retrieves current element in the holder and sets an expiration duration for it.", "读取当前值并为其设置过期时长。")
t("Retrieves current element in the holder and sets an expiration date for it.", "读取当前值并设置绝对过期时刻。")
t("Retrieves current element in the holder and clears expiration date set before.", "读取当前值并清除已设置的过期时间。")
t("Retrieves element stored in the holder.", "返回容器中存储的值。")
t("Retrieves element in the holder and removes it.", "读取当前值并删除该 Redis 键。")
t("Stores element into the holder.", "将值写入容器。")
t("Use {@link #set(Object, Duration)} instead", "已废弃，请改用 {@link #set(Object, Duration)}。")
t(
    "Stores <code>value</code> into the holder with defined expiration <code>duration</code>.",
    "写入 {@code value} 并设置过期时长 {@code duration}。",
)
t("Set value and keep existing TTL.", "设置新值并保留原有 TTL。")
t("Adds object event listener", "注册对象事件监听器。")
t(
    "Returns the common part of the data stored in this bucket\n     * and a bucket defined by the <code>name</code>",
    "返回本 Bucket 与名为 {@code name} 的另一 Bucket 存储数据的公共前缀部分。",
)
t(
    "Returns the length of the common part of the data stored in this bucket\n     * and a bucket defined by the <code>name</code>",
    "返回本 Bucket 与名为 {@code name} 的另一 Bucket 公共前缀的字节长度。",
)

t(
    "Returns Redis object mapped by key. Result Map is not contains\n     * key-value entry for null values.",
    "按 Redis 键批量读取对象；结果 Map 不包含值为 null 的条目。",
)
t(
    "Try to save objects mapped by Redis key.\n     * If at least one of them is already exist then \n     * don't set none of them.",
    "尝试批量写入；若任一键已存在则全部不写入（原子性）。",
)
t("Saves objects mapped by Redis key.", "批量写入键值映射。")
t("Saves objects mapped by Redis key.\n     * If all of them is already exist", "仅当所有指定键均已存在时批量写入。")
t("Saves objects mapped by Redis key.\n     * If none of the specified keys exist", "仅当所有指定键均不存在时批量写入。")

t("Includes all cascade types.", "包含所有级联类型。")
t(
    "Cascade persist operation during {@link RLiveObjectService#persist} method invocation.",
    "在调用 {@link RLiveObjectService#persist} 时级联持久化关联对象。",
)
t(
    "Cascade detach operation during {@link RLiveObjectService#detach} method invocation.",
    "在调用 {@link RLiveObjectService#detach} 时级联分离关联对象。",
)
t(
    "Cascade merge operation during {@link RLiveObjectService#merge} method invocation.",
    "在调用 {@link RLiveObjectService#merge} 时级联合并关联对象。",
)
t(
    "Cascade delete operation during {@link RLiveObjectService#delete} method invocation.",
    "在调用 {@link RLiveObjectService#delete} 时级联删除关联对象。",
)

t("Sets capacity of this buffer only if it wasn't set before.", "仅当尚未设置容量时初始化缓冲区容量。")
t("Returns capacity of this buffer.", "返回缓冲区容量。")
t("Returns the remaining capacity of this buffer.", "返回剩余可写入容量（再写入多少元素才会开始淘汰最旧值）。")
t(
    "Returns the remaining capacity of this buffer, that is the number of\n     * values that can be added before the oldest values start being evicted.",
    "返回剩余可写入容量，即在开始淘汰最旧值之前还可追加的元素个数。",
)
t("Adds the specified value to the tail of this buffer.", "将指定值追加到缓冲区尾部。")
t("Adds the specified values to the tail of this buffer in iteration order.", "按迭代顺序批量追加值到尾部。")
t(
    "Writes the specified values into a ring of the given {@code size} and\n     * (re)configures this buffer capacity to {@code size}.",
    "将值写入指定 {@code size} 的环并（重新）设置缓冲区容量为 {@code size}。",
)
t("Returns the value stored at the specified ring index.", "返回指定环槽下标处的值。")
t("Returns the {@code count} most recently added values.", "返回最近追加的 {@code count} 个值。")
t("Returns values stored in the specified ring index range (inclusive).", "返回指定环槽下标区间（含端点）内的值。")
t("Returns all retained values in insertion order (oldest-first).", "按插入顺序（最旧在前）返回所有保留值。")
t("Returns the number of values currently stored in this buffer.", "返回当前存储的值数量。")
t("Returns the sum of the numeric values currently stored in this buffer.", "返回当前存储数值的总和。")
t("Returns the sum of the numeric values stored in the specified ring index range.", "返回指定环槽下标区间内数值的总和。")
t("Returns the minimum numeric value currently stored in this buffer.", "返回当前存储数值的最小值。")
t("Returns the minimum numeric value stored in the specified ring index range.", "返回指定环槽下标区间内的最小值。")
t("Returns the maximum numeric value currently stored in this buffer.", "返回当前存储数值的最大值。")
t("Returns the maximum numeric value stored in the specified ring index range.", "返回指定环槽下标区间内的最大值。")
t("Removes all values from this buffer while keeping the configured capacity.", "清空所有值但保留已配置的容量。")
t("Returns {@code true} if this buffer contains no values.", "若缓冲区无任何值则返回 {@code true}。")
t("Returns {@code true} if this buffer is full.", "若缓冲区已满则返回 {@code true}。")
t(
    "Returns {@code true} if this buffer is full, that is the next {@link #add(Object)}\n     * will overwrite the oldest value.",
    "若缓冲区已满（下次 {@link #add(Object)} 将覆盖最旧值）则返回 {@code true}。",
)
t(
    "Returns {@code true} if this buffer is full, that is the next {@code add}\n     * will overwrite the oldest value.",
    "若缓冲区已满（下次 add 将覆盖最旧值）则返回 {@code true}。",
)
t("Returns the most recently added value without removing it.", "返回最近追加的值但不移除。")
t("Returns the oldest retained value without removing it.", "返回最旧保留值但不移除。")
t(
    "Returns the oldest retained value without removing it, that is the value\n     * that will be overwritten next.",
    "返回最旧保留值（下次写入将被覆盖的值）但不移除。",
)
t("Returns the values stored at the specified ring indexes.", "返回指定多个环槽下标处的值。")
t(
    "Returns the number of values equal to the specified value currently stored in this buffer.",
    "返回当前存储中与指定值相等的元素个数。",
)
t("Returns {@code true} if this buffer contains the specified value.", "若缓冲区包含指定值则返回 {@code true}。")
t("Returns the average of the numeric values currently stored in this buffer.", "返回当前存储数值的平均值。")
for op, cn in [("AND", "按位与"), ("OR", "按位或"), ("XOR", "按位异或")]:
    t(
        f"Returns the bitwise {op} of the numeric values currently stored in this buffer.",
        f"返回当前存储数值的{cn}结果。",
    )
    t(
        f"Returns the bitwise {op} of the numeric values stored in the specified ring index range.",
        f"返回指定环槽下标区间内数值的{cn}结果。",
    )

t("Returns object holder instance by name.", "按名称获取 {@link RBucket} 实例（客户端侧缓存）。")
t(
    "Returns object holder instance by name\n     * using provided codec for object.",
    "按名称与指定 {@link Codec} 获取 {@link RBucket} 实例。",
)
t("Returns set instance by name.", "按名称获取 {@link RSet} 实例。")
t("Returns set instance by name\n     * using provided codec for set objects.", "按名称与指定 {@link Codec} 获取 {@link RSet} 实例。")
t(
    "Returns Redis Sorted Set instance by name.\n     * This sorted set sorts objects by object score.",
    "按名称获取 {@link RScoredSortedSet} 实例（按 score 排序）。",
)
t(
    "Returns Redis Sorted Set instance by name\n     * using provided codec for sorted set objects.\n     * This sorted set sorts objects by object score.",
    "按名称与指定 {@link Codec} 获取 {@link RScoredSortedSet} 实例。",
)
t("Returns list instance by name.", "按名称获取 {@link RList} 实例。")
t("Returns list instance by name\n     * using provided codec for list objects.", "按名称与指定 {@link Codec} 获取 {@link RList} 实例。")
t("Returns unbounded queue instance by name.", "按名称获取无界 {@link RQueue} 实例。")
t(
    "Returns unbounded queue instance by name\n     * using provided codec for queue objects.",
    "按名称与指定 {@link Codec} 获取无界 {@link RQueue} 实例。",
)
t("Returns unbounded deque instance by name.", "按名称获取无界 {@link RDeque} 实例。")
t(
    "Returns unbounded deque instance by name\n     * using provided codec for deque objects.",
    "按名称与指定 {@link Codec} 获取无界 {@link RDeque} 实例。",
)
t("Returns unbounded blocking queue instance by name.", "按名称获取无界 {@link RBlockingQueue} 实例。")
t(
    "Returns unbounded blocking queue instance by name\n     * using provided codec for queue objects.",
    "按名称与指定 {@link Codec} 获取无界 {@link RBlockingQueue} 实例。",
)
t("Returns unbounded blocking deque instance by name.", "按名称获取无界 {@link RBlockingDeque} 实例。")
t(
    "Returns unbounded blocking deque instance by name\n     * using provided codec for deque objects.",
    "按名称与指定 {@link Codec} 获取无界 {@link RBlockingDeque} 实例。",
)
t("Returns geospatial items holder instance by <code>name</code>.", "按名称获取 {@link RGeo} 地理空间容器实例。")
t(
    "Returns geospatial items holder instance by <code>name</code>\n     * using provided codec for geospatial members.",
    "按名称与指定 {@link Codec} 获取 {@link RGeo} 实例。",
)

t(
    "Retains only the elements in this collection that are contained in the\n     * specified collection.",
    "仅保留同时存在于指定集合中的元素（求交保留）。",
)
t(
    "Retains only the elements in this collection that are contained in the\n     * specified collection (optional operation).",
    "仅保留同时存在于指定集合中的元素（求交保留）。",
)
t(
    "Removes all of this collection's elements that are also contained in the\n     * specified collection.",
    "移除本集合中亦存在于指定集合的所有元素。",
)
t(
    "Removes all of this collection's elements that are also contained in the\n     * specified collection (optional operation).",
    "移除本集合中亦存在于指定集合的所有元素。",
)
t(
    "Returns <code>true</code> if this collection contains encoded state of the specified element.",
    "若本集合包含指定元素（按序列化状态比较）则返回 {@code true}。",
)
t(
    "Returns <code>true</code> if this collection contains all of the elements\n     * in the specified collection.",
    "若本集合包含指定集合的全部元素则返回 {@code true}。",
)
t(
    "Removes a single instance of the specified element from this\n     * collection, if it is present.",
    "若存在则从本集合移除指定元素的一个实例。",
)
t(
    "Removes a single instance of the specified element from this\n     * collection, if it is present (optional operation).",
    "若存在则从本集合移除指定元素的一个实例。",
)
t("Returns number of elements in this collection.", "返回集合元素数量。")
t("Adds element into this collection.", "向集合添加元素。")
t("Adds all elements contained in the specified collection", "批量添加指定集合中的全部元素。")
t("Returns iterator over collection elements", "返回集合元素的响应式迭代流。")

# Full block overrides for complex javadocs
CLASS_OVERRIDES: dict[str, list[tuple[str, str]]] = {
    f"{_A}RBucketReactive.java": [
        (
            "/**\n * Reactive implementation of object holder. Max size of object is 512MB\n *\n * @author Nikita Koksharov\n *\n * @param <V> - the type of object\n */",
            "/**\n * {@link RBucket} 的 Reactor 风格 API 接口。\n * <p>单个对象最大 512MB；各方法返回 {@link Mono}，用于非阻塞响应式编程。\n *\n * @author Nikita Koksharov\n * @param <V> 存储对象类型\n */",
        ),
    ],
    f"{_A}RBucketRx.java": [
        (
            "/**\n * Reactive implementation of object holder. Max size of object is 512MB\n *\n * @author Nikita Koksharov\n *\n * @param <V> - the type of object\n */",
            "/**\n * {@link RBucket} 的 RxJava 风格 API 接口。\n * <p>单个对象最大 512MB；各方法返回 {@link Single}、{@link Maybe} 或 {@link Completable}。\n *\n * @author Nikita Koksharov\n * @param <V> 存储对象类型\n */",
        ),
    ],
    f"{_A}RBuckets.java": [
        (
            "/**\n * Operations over multiple Bucket objects.\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 对多个 {@link RBucket} 的批量操作接口。\n * <p>支持 MGET/MSET 等 Redis 批量命令语义。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RBucketsAsync.java": [
        (
            "/**\n * Operations over multiple Bucket objects.\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 对多个 {@link RBucket} 的批量操作异步 API。\n * <p>各方法返回 {@link RFuture}。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RBucketsReactive.java": [
        (
            "/**\n * Operations over multiple Bucket objects.\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 对多个 {@link RBucket} 的批量操作 Reactor API。\n * <p>各方法返回 {@link Mono}。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RBucketsRx.java": [
        (
            "/**\n * Operations over multiple Bucket objects.\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 对多个 {@link RBucket} 的批量操作 RxJava API。\n * <p>各方法返回 {@link Single} 或 {@link Completable}。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RCascadeType.java": [
        (
            "/**\n * Live Object cascade type.\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * Live Object 级联操作类型。\n * <p>控制 {@link RLiveObjectService} 持久化、合并、分离、删除时的关联对象传播行为。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RClientSideCaching.java": [
        (
            "/**\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 客户端侧读缓存（Client-side caching）API。\n * <p>在 Redis 6 跟踪模式下本地缓存热点对象，减少网络往返。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RCollectionAsync.java": [
        (
            "/**\n * Common async interface for collection object\n * \n * @author Nikita Koksharov\n *\n * @param <V> type of value\n */",
            "/**\n * 分布式集合对象的通用异步 API 接口。\n * <p>各方法返回 {@link RFuture}。\n *\n * @author Nikita Koksharov\n * @param <V> 元素类型\n */",
        ),
    ],
    f"{_A}RCollectionReactive.java": [
        (
            "/**\n * Common reactive interface for collection object\n * \n * @author Nikita Koksharov\n *\n * @param <V> value\n */",
            "/**\n * 分布式集合对象的通用 Reactor 风格 API 接口。\n * <p>各方法返回 {@link Mono} 或 {@link Flux}。\n *\n * @author Nikita Koksharov\n * @param <V> 元素类型\n */",
        ),
    ],
    f"{_A}RCollectionRx.java": [
        (
            "/**\n * Common RxJava2 interface for collection object\n * \n * @author Nikita Koksharov\n *\n * @param <V> value\n */",
            "/**\n * 分布式集合对象的通用 RxJava 风格 API 接口。\n * <p>各方法返回 {@link Single} 或 {@link Flowable}。\n *\n * @author Nikita Koksharov\n * @param <V> 元素类型\n */",
        ),
    ],
}

# read originals and build per-file replacements
W43B: dict[str, list[tuple[str, str]]] = {}
failures: list[str] = []

CN_PARAM = {
    "value": "待设置的值",
    "duration": "过期时长",
    "timeToLive": "存活时间",
    "timeUnit": "时间单位",
    "expect": "期望值",
    "update": "新值",
    "args": "参数",
    "newValue": "新值",
    "listener": "事件监听器",
    "name": "名称",
    "keys": "Redis 键列表",
    "buckets": "键到值的映射",
    "capacity": "缓冲区容量",
    "value": "值",
    "values": "值集合",
    "size": "环大小",
    "index": "环槽下标",
    "count": "返回数量",
    "reverse": "排序方向",
    "startIndex": "起始环槽下标",
    "endIndex": "结束环槽下标",
    "indexes": "环槽下标集合",
    "codec": "编解码器",
    "c": "集合",
    "o": "元素",
    "e": "待添加元素",
}


def cn_param_line(line: str) -> str:
    m = re.match(r"(\s*\* @param(?:\s+<[^>]+>)?\s+)(\S+)(\s*)(.*)", line)
    if not m:
        return line
    name = m.group(2)
    rest = m.group(4).strip()
    cn = CN_PARAM.get(name)
    if cn:
        return f"{m.group(1)}{name} {cn}"
    if rest.startswith("- "):
        return f"{m.group(1)}{name} {rest[2:]}"
    return line


def cn_return_line(line: str, cn_desc: str) -> str:
    m = re.match(r"(\s*\* @return\s*)(.*)", line)
    if not m:
        return line
    en = m.group(2).strip()
    mapping = {
        "object size": "对象字节大小",
        "previous value": "替换前的旧值",
        "element": "当前值",
        "listener id": "监听器 ID",
        "common part of the data": "公共前缀数据",
        "void": None,
        "size of collection": "元素数量",
        "iterator": "元素流",
        "buffer capacity, or {@code 0} if capacity wasn't set": "缓冲区容量；未设置时为 {@code 0}",
        "remaining capacity": "剩余容量",
        "number of stored values": "已存储值数量",
        "all retained values": "全部保留值",
        "most recently added values": "最近追加的值列表",
        "values stored in the specified ring index range": "指定区间内的值列表",
        "values stored at the specified ring indexes": "指定下标处的值列表",
        "number of matching values": "匹配元素个数",
        "array index where the last value was written": "最后一个值写入的数组下标",
    }
    if en in mapping and mapping[en]:
        return f"{m.group(1)}{mapping[en]}"
    if "{@code true}" in en:
        return f"{m.group(1)}见方法说明"
    return line


def translate_block(jdoc: str) -> str | None:
    desc = main_desc(jdoc)
    if desc not in TRANSLATIONS:
        return None
    cn = TRANSLATIONS[desc]
    out = []
    first = True
    for line in jdoc.splitlines():
        s = line.strip()
        if first and s.startswith("*") and not s.startswith("* @") and s not in ("*/", "/**", "*"):
            indent = line[: len(line) - len(line.lstrip())]
            out.append(f"{indent}* {cn}")
            first = False
        elif s.startswith("* @param"):
            out.append(cn_param_line(line))
        elif s.startswith("* @return"):
            out.append(cn_return_line(line, cn))
        elif s.startswith("* Requires"):
            out.append(line.replace("Requires", "需要").replace("and higher", "及以上"))
        elif s.startswith("* NOTE:"):
            out.append(line.replace("NOTE:", "注意："))
        else:
            out.append(line)
    return "\n".join(out)


for rel in FILES:
    src = (ORIG / rel).read_text(encoding="utf-8")
    reps: list[tuple[str, str]] = []
    used: set[str] = set()

    for old, new in CLASS_OVERRIDES.get(rel, []):
        if old in src:
            reps.append((old, new))
            used.add(old)

    for jdoc in extract_javadocs(src):
        if jdoc in used:
            continue
        new = translate_block(jdoc)
        if new and new != jdoc:
            reps.append((jdoc, new))
            used.add(jdoc)
        elif main_desc(jdoc) and main_desc(jdoc) not in TRANSLATIONS:
            failures.append(f"{rel}: untranslated: {main_desc(jdoc)[:60]}")

    if not reps:
        failures.append(f"{rel}: no replacements")
    W43B[rel] = reps
    W43B[rel.split("/")[-1]] = reps

if failures:
    print("WARNINGS (non-fatal if cn>=10 after apply):")
    for f in failures[:30]:
        print(f)
    print(f"... total warnings: {len(failures)}")

lines = [
    '"""Chinese annotation replacements for Redisson 4.7.0 wave-43b api [15:30]."""',
    "from __future__ import annotations",
    "",
    f'_A = "{_A}"',
    "",
    "W43B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
]
for rel in FILES:
    reps = W43B[rel]
    lines.append(f"    {rel!r}: [")
    for old, new in reps:
        lines.append(f"        ({old!r}, {new!r}),")
    lines.append("    ],")
    name = rel.split("/")[-1]
    lines.append(f"    {name!r}: [")
    for old, new in reps:
        lines.append(f"        ({old!r}, {new!r}),")
    lines.append("    ],")
lines.append("}")
OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
print(f"Wrote {OUT} — {sum(len(W43B[r]) for r in FILES)} replacement pairs across {len(FILES)} files")
