"""Chinese annotation replacements for Redisson 4.7.0 wave-42a api [0:15]."""
from __future__ import annotations

_API = "redisson/src/main/java/org/redisson/api/"

W42A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}


def _add(rel: str, reps: list[tuple[str, str]]) -> None:
    W42A_REPLACEMENTS[rel] = reps
    W42A_REPLACEMENTS[rel.split("/")[-1]] = reps


def _jdoc(*body: str, params: list[tuple[str, str]] | None = None, ret: str | None = None) -> str:
    lines = ["    /**", *[f"     * {line}" for line in body]]
    if params:
        lines.append("     *")
        for name, desc in params:
            lines.append(f"     * @param {name} {desc}")
    if ret is not None:
        lines.append(f"     * @return {ret}")
    lines.append("     */")
    return "\n".join(lines)


def _pair(
    desc_en: str,
    desc_cn: str,
    params: list[tuple[str, str, str]] | None = None,
    ret_en: str | None = None,
    ret_cn: str | None = None,
    indent: str = "    ",
) -> tuple[str, str]:
    """Build (old, new) javadoc pair. params: (name, en_desc, cn_desc)."""
    star = indent + " *"
    old_lines = [indent + "/**", f"{star} {desc_en}"]
    new_lines = [indent + "/**", f"{star} {desc_cn}"]
    if params or ret_en is not None:
        old_lines.append(star)
        new_lines.append(star)
    if params:
        for name, en_d, cn_d in params:
            old_lines.append(f"{star} @param {name} {en_d}")
            new_lines.append(f"{star} @param {name} {cn_d}")
    if ret_en is not None:
        old_lines.append(f"{star} @return {ret_en}")
        new_lines.append(f"{star} @return {ret_cn}")
    old_lines.append(indent + " */")
    new_lines.append(indent + " */")
    return "\n".join(old_lines), "\n".join(new_lines)


# --- shared array method javadoc pairs ---

_ARRAY_COMMON: list[tuple[str, str, list[tuple[str, str, str]] | None, str | None, str | None]] = [
    (
        "Returns value stored at the specified array index.",
        "返回指定数组下标处存储的值。",
        [("index", "array index", "数组下标")],
        "value stored at the specified array index",
        "指定下标处存储的值",
    ),
    (
        "Returns {@code true} if a value is stored at the specified array index.",
        "若指定数组下标处已存储值则返回 {@code true}。",
        [("index", "array index", "数组下标")],
        "{@code true} if a value is stored at the specified array index, {@code false} otherwise",
        "已存储值时返回 {@code true}，否则 {@code false}",
    ),
    (
        "Returns values stored at the specified array indexes.",
        "返回指定多个数组下标处的值。",
        [("indexes", "array indexes", "数组下标集合")],
        "values stored at the specified array indexes",
        "指定下标处的值列表",
    ),
    (
        "Sets value at the specified array index.",
        "在指定数组下标处设置值。",
        [("index", "array index", "数组下标"), ("value", "value to set", "要设置的值")],
        "number of values set",
        "成功设置的值数量",
    ),
    (
        "Sets values starting at the specified array index.",
        "从指定起始下标起连续设置多个值。",
        [
            ("index", "start array index", "起始数组下标"),
            ("values", "values to set", "要设置的值"),
        ],
        "number of values set",
        "成功设置的值数量",
    ),
    (
        "Sets values at the specified array indexes.",
        "在指定多个数组下标处设置值。",
        [("entries", "map of array indexes and values", "数组下标与值的映射")],
        "number of values set",
        "成功设置的值数量",
    ),
    (
        "Deletes values stored at the specified array indexes.",
        "删除指定数组下标处的值。",
        [("indexes", "array indexes", "数组下标集合")],
        "number of deleted values",
        "删除的值数量",
    ),
    (
        "Deletes values stored in the specified array index range.",
        "删除指定下标区间内的值。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
        ],
        "number of deleted values",
        "删除的值数量",
    ),
    (
        "Deletes values stored in the specified array index ranges.\n     * Arguments should contain start and end index pairs.",
        "删除多个下标区间内的值；参数为成对的起始与结束下标。",
        [("startEndIndexes", "start and end array index pairs", "成对的起始与结束下标")],
        "number of deleted values",
        "删除的值数量",
    ),
    (
        "Returns number of values stored in this array.",
        "返回本数组中已存储值的数量。",
        None,
        "number of values",
        "值的数量",
    ),
    (
        "Returns number of values stored in the specified array index range.",
        "返回指定下标区间内已存储值的数量。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
        ],
        "number of values",
        "值的数量",
    ),
    (
        "Returns number of values equal to the specified value in the specified array index range.",
        "返回指定下标区间内与给定值相等的元素个数。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
            ("value", "value to match", "待匹配的值"),
        ],
        "number of matching values",
        "匹配的元素个数",
    ),
    (
        "Returns array length.",
        "返回数组长度。",
        None,
        "array length",
        "数组长度",
    ),
    (
        "Returns values stored in the specified array index range.",
        "返回指定下标区间内的值列表。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
        ],
        "values stored in the specified array index range",
        "指定区间内的值列表",
    ),
    (
        "Returns entries stored in the specified array index range.",
        "返回指定下标区间内的条目列表。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
        ],
        "entries stored in the specified array index range",
        "指定区间内的条目列表",
    ),
    (
        "Returns entries stored in the specified array index range.",
        "返回指定下标区间内的条目列表（最多返回 limit 条）。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
            ("limit", "maximum number of entries", "返回条目数量上限"),
        ],
        "entries stored in the specified array index range",
        "指定区间内的条目列表",
    ),
    (
        "Inserts values at consecutive indexes starting at the current insert index.",
        "从当前插入游标起，在连续下标处插入多个值。",
        [("values", "values to insert", "要插入的值")],
        "array index where the last value was inserted",
        "最后一个值被插入的数组下标",
    ),
    (
        "Inserts values into a ring buffer with the specified size.\n     * Values are written at consecutive ring positions and wrap around as needed.",
        "将值写入指定大小的环形缓冲区，按环形位置连续写入并在必要时回绕。",
        [
            ("size", "ring buffer size", "环形缓冲区大小"),
            ("values", "values to insert", "要插入的值"),
        ],
        "array index where the last value was inserted",
        "最后一个值被插入的数组下标",
    ),
    (
        "Sets current insert index.",
        "设置当前插入游标下标。",
        [("index", "array index", "数组下标")],
        "{@code true} if index was set, {@code false} otherwise",
        "设置成功返回 {@code true}，否则 {@code false}",
    ),
    (
        "Returns last inserted values.",
        "返回最近插入的值。",
        [("count", "values amount", "值数量")],
        "last inserted values",
        "最近插入的值列表",
    ),
    (
        "Returns last inserted values in reverse order.",
        "按逆序返回最近插入的值。",
        [("count", "values amount", "值数量")],
        "last inserted values in reverse order",
        "逆序的最近插入值列表",
    ),
    (
        "Returns array information.",
        "返回数组基本信息。",
        None,
        "array information",
        "数组基本信息",
    ),
    (
        "Returns full array information including extended statistics.",
        "返回包含扩展统计信息的完整数组信息。",
        None,
        "full array information",
        "完整数组信息",
    ),
    (
        "Returns indexes of values matching the specified arguments.",
        "返回匹配 grep 条件的值所在下标。",
        [("args", "grep arguments", "grep 参数")],
        "indexes of matching values",
        "匹配值的下标列表",
    ),
    (
        "Returns indexes of values matching the specified arguments in the specified array index range.",
        "返回指定下标区间内匹配 grep 条件的值所在下标。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
            ("args", "grep arguments", "grep 参数"),
        ],
        "indexes of matching values",
        "匹配值的下标列表",
    ),
    (
        "Returns entries matching the specified arguments.",
        "返回匹配 grep 条件的条目。",
        [("args", "grep arguments", "grep 参数")],
        "matching entries",
        "匹配的条目列表",
    ),
    (
        "Returns entries matching the specified arguments in the specified array index range.",
        "返回指定下标区间内匹配 grep 条件的条目。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
            ("args", "grep arguments", "grep 参数"),
        ],
        "matching entries",
        "匹配的条目列表",
    ),
    (
        "Returns sum of numeric values in the specified array index range.",
        "返回指定下标区间内数值元素之和。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
        ],
        "sum of values",
        "数值之和",
    ),
    (
        "Returns minimum numeric value in the specified array index range.",
        "返回指定下标区间内的最小数值。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
        ],
        "minimum value",
        "最小值",
    ),
    (
        "Returns maximum numeric value in the specified array index range.",
        "返回指定下标区间内的最大数值。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
        ],
        "maximum value",
        "最大值",
    ),
    (
        "Returns bitwise AND result for numeric values in the specified array index range.",
        "返回指定下标区间内数值的按位与结果。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
        ],
        "bitwise AND result",
        "按位与结果",
    ),
    (
        "Returns bitwise OR result for numeric values in the specified array index range.",
        "返回指定下标区间内数值的按位或结果。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
        ],
        "bitwise OR result",
        "按位或结果",
    ),
    (
        "Returns bitwise XOR result for numeric values in the specified array index range.",
        "返回指定下标区间内数值的按位异或结果。",
        [
            ("startIndex", "start array index", "起始数组下标"),
            ("endIndex", "end array index", "结束数组下标"),
        ],
        "bitwise XOR result",
        "按位异或结果",
    ),
]


def _array_common_pairs() -> list[tuple[str, str]]:
    return [_pair(*item) for item in _ARRAY_COMMON]


# --- LockOptions ---

_add(
    f"{_API}LockOptions.java",
    [
        (
            "/**\n * Configuration for Lock object.\n *\n * @author Danila Varatyntsev\n */",
            "/**\n"
            " * 分布式锁对象的退避（back-off）配置。\n"
            " * <p>供 {@linkplain org.redisson.RedissonSpinLock} 等自旋锁在获取失败时\n"
            " * 按策略休眠后重试。\n"
            " *\n"
            " * @author Danila Varatyntsev\n"
            " */",
        ),
        (
            "    /**\n     * Factory for {@linkplain BackOffPolicy} class.\n     */",
            "    /**\n     * {@linkplain BackOffPolicy} 工厂接口。\n     */",
        ),
        (
            "    /**\n     * Generator of sleep period values for {@linkplain org.redisson.RedissonSpinLock} back off\n     */",
            "    /**\n     * 为 {@linkplain org.redisson.RedissonSpinLock} 退避算法生成休眠时长。\n     */",
        ),
        (
            "        /**\n         * Generates and returns next sleep period\n         *\n         * @return next sleep period\n         */",
            "        /**\n         * 生成并返回下一次休眠时长\n         *\n         * @return 下一次休眠时长（毫秒）\n         */",
        ),
        (
            "    /**\n     * Back off algorithm, where sleep period starts with {@linkplain #initialDelay}, each time increases\n     * {@linkplain #multiplier} times but doesn't exceed {@linkplain #maxDelay}\n     */",
            "    /**\n"
            "     * 指数退避算法：休眠从 {@linkplain #initialDelay} 起，每次乘以\n"
            "     * {@linkplain #multiplier}，且不超过 {@linkplain #maxDelay}。\n"
            "     */",
        ),
        (
            "        /**\n         * Sets max back off delay.\n         * <p>\n         * Default is <code>128</code>\n         *\n         * @param maxDelay - max sleep period. Has to be positive\n         * @return ExponentialBackOffOptions instance\n         */",
            "        /**\n"
            "         * 设置最大退避延迟。\n"
            "         * <p>\n"
            "         * 默认值为 <code>128</code>。\n"
            "         *\n"
            "         * @param maxDelay 最大休眠时长，须为正数\n"
            "         * @return ExponentialBackOff 实例\n"
            "         */",
        ),
        (
            "        /**\n         * Sets initial back off delay.\n         * <p>\n         * Default is <code>1</code>\n         *\n         * @param initialDelay - initial sleep period. Has to be positive\n         * @return ExponentialBackOffOptions instance\n         */",
            "        /**\n"
            "         * 设置初始退避延迟。\n"
            "         * <p>\n"
            "         * 默认值为 <code>1</code>。\n"
            "         *\n"
            "         * @param initialDelay 初始休眠时长，须为正数\n"
            "         * @return ExponentialBackOff 实例\n"
            "         */",
        ),
        (
            "        /**\n         * Sets back off delay multiplier.\n         * <p>\n         * Default is <code>2</code>\n         *\n         * @param multiplier - sleep period multiplier. Has to be positive\n         * @return ExponentialBackOffOptions instance\n         */",
            "        /**\n"
            "         * 设置退避延迟倍数。\n"
            "         * <p>\n"
            "         * 默认值为 <code>2</code>。\n"
            "         *\n"
            "         * @param multiplier 休眠时长倍数，须为正数\n"
            "         * @return ExponentialBackOff 实例\n"
            "         */",
        ),
        (
            "    /**\n     * Back off algorithm, where sleep period time increases exponentially. To prevent\n     */",
            "    /** 指数退避策略实现：休眠时长指数增长并加入随机抖动。 */\n",
        ),
        (
            "    /**\n     * Back off algorithm, where sleep period is constant and is defined by {@linkplain #delay}.\n     * To reduce possible negative effects of many threads simultaneously sending requests, a small random value is\n     * added to all sleep periods.\n     */",
            "    /**\n"
            "     * 固定退避算法：休眠时长由 {@linkplain #delay} 决定。\n"
            "     * <p>为减轻多线程同时重试的惊群效应，可在各次休眠上叠加小幅随机值。\n"
            "     */",
        ),
        (
            "        /**\n         * Sets back off delay value.\n         * <p>\n         * Default is <code>64</code>\n         *\n         * @param delay - sleep period value. Has to be positive\n         * @return ConstantBackOffOptions instance\n         */",
            "        /**\n"
            "         * 设置固定退避延迟。\n"
            "         * <p>\n"
            "         * 默认值为 <code>64</code>。\n"
            "         *\n"
            "         * @param delay 休眠时长，须为正数\n"
            "         * @return ConstantBackOff 实例\n"
            "         */",
        ),
        (
            "    /**\n     * Back off policy, where sleep period is constant and is defined by {@linkplain #delay}\n     */",
            "    /** 固定休眠时长的退避策略实现。 */\n",
        ),
        (
            "    /**\n     * Creates a new instance of ExponentialBackOffOptions with default options.\n     *\n     * @return BackOffOptions instance\n     */",
            "    /**\n     * 创建默认配置的指数退避工厂。\n     *\n     * @return BackOff 实例\n     */",
        ),
    ],
)

# --- MapCacheOptions ---

_add(
    f"{_API}MapCacheOptions.java",
    [
        (
            "/**\n * Configuration for RMapCache object.\n *\n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
            "/**\n"
            " * {@link RMapCache} 的配置选项（已废弃）。\n"
            " * <p>扩展 {@link MapOptions}，支持在条目驱逐完成后移除空的驱逐任务。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " * @param <K> 键类型\n"
            " * @param <V> 值类型\n"
            " */",
        ),
        (
            "    /**\n     * Removes eviction task from memory if map is empty\n     * upon entries eviction process completion.\n     *\n     * @return MapOptions instance\n     */",
            "    /**\n"
            "     * 条目驱逐完成后，若 map 已空则从内存中移除驱逐任务。\n"
            "     *\n"
            "     * @return MapCacheOptions 实例\n"
            "     */",
        ),
    ],
)

# --- MapOptions ---

_add(
    f"{_API}MapOptions.java",
    [
        (
            "/**\n * Use org.redisson.api.options.MapOptions instead\n * \n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
            "/**\n"
            " * 分布式 Map 读写策略配置（已废弃，请改用 org.redisson.api.options.MapOptions）。\n"
            " * <p>可配置 {@link MapLoader}/{@link MapWriter}、write-through/write-behind\n"
            " * 及写入重试参数。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " * @param <K> 键类型\n"
            " * @param <V> 值类型\n"
            " */",
        ),
        (
            "        /**\n         * In write behind mode all data written in map object \n         * also written using MapWriter in asynchronous mode.\n         */",
            "        /** write-behind 模式：map 写入异步批量落库至 {@link MapWriter}。 */\n",
        ),
        (
            "        /**\n         * In write through mode all write operations for map object \n         * are synchronized with MapWriter write operations.\n         * If MapWriter throws an error then it will be re-thrown to Map operation caller.\n         */",
            "        /**\n"
            "         * write-through 模式：map 写操作与 {@link MapWriter} 同步；\n"
            "         * 若 {@link MapWriter} 抛错则原样传递给调用方。\n"
            "         */",
        ),
        (
            "    /**\n     * Creates a new instance of MapOptions with default options.\n     * <p>\n     * This is equivalent to:\n     * <pre>\n     *     new MapOptions()\n     *      .writer(null, null).loader(null);\n     * </pre>\n     * \n     * @param <K> key type\n     * @param <V> value type\n     * \n     * @return MapOptions instance\n     * \n     */",
            "    /**\n"
            "     * 创建默认 {@link MapOptions} 实例。\n"
            "     * <p>\n"
            "     * 等价于：\n"
            "     * <pre>\n"
            "     *     new MapOptions()\n"
            "     *      .writer(null, null).loader(null);\n"
            "     * </pre>\n"
            "     *\n"
            "     * @param <K> 键类型\n"
            "     * @param <V> 值类型\n"
            "     * @return MapOptions 实例\n"
            "     */",
        ),
        (
            "    /**\n     * Defines {@link MapWriter} object which is invoked during write operation.\n     * \n     * @param writer object\n     * @return MapOptions instance\n     */",
            "    /**\n     * 设置写操作调用的 {@link MapWriter}。\n     *\n     * @param writer MapWriter 实例\n     * @return MapOptions 实例\n     */",
        ),
        (
            "    /**\n     * Defines {@link MapWriterAsync} object which is invoked during write operation.\n     *\n     * @param writer object\n     * @return MapOptions instance\n     */",
            "    /**\n     * 设置写操作调用的 {@link MapWriterAsync}。\n     *\n     * @param writer MapWriterAsync 实例\n     * @return MapOptions 实例\n     */",
        ),
        (
            "    /**\n     * Sets write behind tasks batch size. \n     * All updates accumulated into a batch of specified size and written with {@link MapWriter}.\n     * <p>\n     * Default is <code>50</code>\n     * \n     * @param writeBehindBatchSize - size of batch\n     * @return MapOptions instance\n     */",
            "    /**\n"
            "     * 设置 write-behind 批量大小；更新累积到指定批次后通过 {@link MapWriter} 写入。\n"
            "     * <p>\n"
            "     * 默认值为 <code>50</code>。\n"
            "     *\n"
            "     * @param writeBehindBatchSize 批次大小\n"
            "     * @return MapOptions 实例\n"
            "     */",
        ),
        (
            "    /**\n     * Sets write behind tasks execution delay.\n     * All updates written with {@link MapWriter} and lag not more than specified delay.\n     * <p>\n     * Default is <code>1000</code> milliseconds\n     * \n     * @param writeBehindDelay - delay in milliseconds\n     * @return MapOptions instance\n     */",
            "    /**\n"
            "     * 设置 write-behind 任务执行延迟；更新通过 {@link MapWriter} 写入且滞后不超过该延迟。\n"
            "     * <p>\n"
            "     * 默认值为 <code>1000</code> 毫秒。\n"
            "     *\n"
            "     * @param writeBehindDelay 延迟（毫秒）\n"
            "     * @return MapOptions 实例\n"
            "     */",
        ),
        (
            "    /**\n     * Sets write mode. \n     * <p>\n     * Default is <code>{@link WriteMode#WRITE_THROUGH}</code>\n     * \n     * @param writeMode - write mode\n     * @return MapOptions instance\n     */",
            "    /**\n"
            "     * 设置写入模式。\n"
            "     * <p>\n"
            "     * 默认值为 <code>{@link WriteMode#WRITE_THROUGH}</code>。\n"
            "     *\n"
            "     * @param writeMode 写入模式\n"
            "     * @return MapOptions 实例\n"
            "     */",
        ),
        (
            "    /**\n     * Sets max retry attempts for {@link RetryableMapWriter} or {@link RetryableMapWriterAsync}\n     *\n     * @param writerRetryAttempts object\n     * @return MapOptions instance\n     */",
            "    /**\n"
            "     * 设置 {@link RetryableMapWriter} 或 {@link RetryableMapWriterAsync} 的最大重试次数。\n"
            "     *\n"
            "     * @param writerRetryAttempts 最大重试次数\n"
            "     * @return MapOptions 实例\n"
            "     */",
        ),
        (
            "    /**\n     * Sets retry interval for {@link RetryableMapWriter} or {@link RetryableMapWriterAsync}\n     * \n     * @param writerRetryInterval {@link Duration}\n     * @return MapOptions instance\n     */",
            "    /**\n"
            "     * 设置 {@link RetryableMapWriter} 或 {@link RetryableMapWriterAsync} 的重试间隔。\n"
            "     *\n"
            "     * @param writerRetryInterval 重试间隔 {@link Duration}\n"
            "     * @return MapOptions 实例\n"
            "     */",
        ),
        (
            "    /**\n     * Sets {@link MapLoader} object.\n     * \n     * @param loader object\n     * @return MapOptions instance\n     */",
            "    /**\n     * 设置 {@link MapLoader}。\n     *\n     * @param loader MapLoader 实例\n     * @return MapOptions 实例\n     */",
        ),
        (
            "    /**\n     * Sets {@link MapLoaderAsync} object.\n     *\n     * @param loaderAsync object\n     * @return MapOptions instance\n     */",
            "    /**\n     * 设置 {@link MapLoaderAsync}。\n     *\n     * @param loaderAsync MapLoaderAsync 实例\n     * @return MapOptions 实例\n     */",
        ),
    ],
)

# --- Message ---

_add(
    f"{_API}Message.java",
    [
        (
            "/**\n * Message object.\n *\n * @author Nikita Koksharov\n * @param <V> type\n *\n */",
            "/**\n"
            " * 队列消息不可变值对象。\n"
            " * <p>包含消息 ID、载荷及只读 headers 元数据。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " * @param <V> 载荷类型\n"
            " */",
        ),
    ],
)

# --- MessageArgs ---

_add(
    f"{_API}MessageArgs.java",
    [
        (
            "/**\n * Fluent API interface defining parameters for a message in a queue.\n *\n * @author Nikita Koksharov\n * @param <V> type\n *\n */",
            "/**\n"
            " * 队列消息的流式参数配置接口。\n"
            " * <p>支持优先级、延迟投递、去重、TTL、投递次数限制及自定义 headers。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " * @param <V> 载荷类型\n"
            " */",
        ),
        (
            "    /**\n     * Sets the priority level for the message.\n     * Defined as a number between <code>0</code> and <code>9</code>\n     * <p>\n     * <code>0</code> is the lowest priority level.\n     * <p>\n     * <code>9</code> is the highest priority level.\n     * <p>\n     * Default value is <code>0</code>.\n     *\n     * @param priority the priority level\n     * @return arguments object\n     */",
            "    /**\n"
            "     * 设置消息优先级，取值 <code>0</code>～<code>9</code>。\n"
            "     * <p>\n"
            "     * <code>0</code> 为最低，<code>9</code> 为最高；默认 <code>0</code>。\n"
            "     *\n"
            "     * @param priority 优先级\n"
            "     * @return 参数构建器\n"
            "     */",
        ),
        (
            "    /**\n     * Sets a delay interval before the message becomes available for processing.\n     * <p>\n     * <code>0</code> value means delay duration is not applied.\n     * If not defined, the queue's delay setting value is used.\n     * If queue's delay setting is also not set, the default value is <code>0</code>.\n     *\n     * @param interval the time duration to delay message delivery\n     * @return arguments object\n     */",
            "    /**\n"
            "     * 设置消息可消费前的延迟时长。\n"
            "     * <p>\n"
            "     * <code>0</code> 表示不延迟；未设置时使用队列级 delay，队列也未设置则默认为 <code>0</code>。\n"
            "     *\n"
            "     * @param interval 延迟时长\n"
            "     * @return 参数构建器\n"
            "     */",
        ),
        (
            "    /**\n     * Enables deduplication based on the payload hash for the specified interval.\n     * <p>\n     * During the specified interval, messages with the same hash will be considered duplicates\n     * and won't be added to the queue.\n     * </p>\n     *\n     * @param interval the time duration\n     * @return arguments object\n     */",
            "    /**\n"
            "     * 在指定时间窗口内按载荷哈希去重。\n"
            "     * <p>\n"
            "     * 窗口内相同哈希的消息视为重复，不会入队。\n"
            "     * </p>\n"
            "     *\n"
            "     * @param interval 去重时间窗口\n"
            "     * @return 参数构建器\n"
            "     */",
        ),
        (
            "    /**\n     * Enables deduplication based on a custom ID for the specified interval.\n     * <p>\n     * During the specified interval, messages with the same ID will be considered duplicates\n     * and won't be added to the queue.\n     * </p>\n     *\n     * @param id the custom identifier\n     * @param interval the time duration\n     * @return arguments object\n     */",
            "    /**\n"
            "     * 在指定时间窗口内按自定义 ID 去重。\n"
            "     * <p>\n"
            "     * 窗口内相同 ID 的消息视为重复，不会入队。\n"
            "     * </p>\n"
            "     *\n"
            "     * @param id 自定义标识\n"
            "     * @param interval 去重时间窗口\n"
            "     * @return 参数构建器\n"
            "     */",
        ),
        (
            "    /**\n     * Sets the time-to-live duration for the message.\n     * <p>\n     * After this duration has elapsed, the message is removed from the queue\n     * if it hasn't been processed.\n     * <p>\n     * <code>0</code> value means expiration is not applied.\n     * If not defined, the queue's timeToLive setting value is used.\n     * If queue's timeToLive setting is also not set, the default value is <code>0</code>.\n     *\n     * @param value the time duration\n     * @return arguments object\n     */",
            "    /**\n"
            "     * 设置消息存活时间（TTL）。\n"
            "     * <p>\n"
            "     * 超时且未被消费时从队列移除。\n"
            "     * <p>\n"
            "     * <code>0</code> 表示不过期；未设置时使用队列 timeToLive，队列也未设置则默认为 <code>0</code>。\n"
            "     *\n"
            "     * @param value 存活时长\n"
            "     * @return 参数构建器\n"
            "     */",
        ),
        (
            "    /**\n     * Sets the maximum number of delivery attempts for the message.\n     * <p>\n     * If processing the message fails, it may be redelivered up to the specified count.\n     * </p>\n     * The minimum value is <code>1</code>. If not defined, the queue's deliveryLimit setting value is used.\n     * If queue's deliveryLimit setting is also not set, the default value is <code>10</code>.\n     *\n     * @param count the maximum number of delivery attempts\n     * @return arguments object\n     */",
            "    /**\n"
            "     * 设置消息最大投递次数。\n"
            "     * <p>\n"
            "     * 处理失败时可重投，最多达到指定次数。\n"
            "     * </p>\n"
            "     * 最小值为 <code>1</code>；未设置时使用队列 deliveryLimit，队列也未设置则默认为 <code>10</code>。\n"
            "     *\n"
            "     * @param count 最大投递次数\n"
            "     * @return 参数构建器\n"
            "     */",
        ),
        (
            "    /**\n     * Adds a single header entry to the message.\n     *\n     * @param key the header key\n     * @param value the header value\n     * @return arguments object\n     */",
            "    /**\n     * 添加单条消息 header。\n     *\n     * @param key header 键\n     * @param value header 值\n     * @return 参数构建器\n     */",
        ),
        (
            "    /**\n     * Adds multiple header entries to the message at once.\n     *\n     * @param entries a map containing header key-value pairs\n     * @return arguments object\n     */",
            "    /**\n     * 批量添加消息 headers。\n     *\n     * @param entries header 键值对映射\n     * @return 参数构建器\n     */",
        ),
        (
            "    /**\n     * Defines the payload to include in the message\n     *\n     * @param value the payload to include\n     * @return arguments object\n     */",
            "    /**\n     * 指定消息载荷并创建参数构建器。\n     *\n     * @param value 消息载荷\n     * @return 参数构建器\n     */",
        ),
    ],
)

# --- MessageParams ---

_add(
    f"{_API}MessageParams.java",
    [
        (
            "/**\n *\n * @author Nikita Koksharov\n * @param <V> type\n *\n */",
            "/**\n"
            " * {@link MessageArgs} 的可变参数实现。\n"
            " * <p>由 {@link MessageArgs#payload(Object)} 创建，供队列客户端组装投递参数。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " * @param <V> 载荷类型\n"
            " */",
        ),
    ],
)

# --- MigrateMode ---

_add(
    f"{_API}MigrateMode.java",
    [
        (
            "/**\n * migrate mode\n *\n * @author lyrric\n */",
            "/**\n"
            " * Redis 键迁移模式，对应 {@code MIGRATE} 命令的行为变体。\n"
            " *\n"
            " * @author lyrric\n"
            " */",
        ),
        (
            "    /**\n     * Default migrate\n     */",
            "    /** 默认迁移：迁移后删除源节点上的键。 */\n",
        ),
        (
            "    /**\n     * Do not remove the key from the local instance.\n     */",
            "    /** 复制模式：不删除源节点上的键。 */\n",
        ),
        (
            "    /**\n     * Replace existing key on the remote instance.\n     */",
            "    /** 替换模式：覆盖目标节点上已存在的同名键。 */\n",
        ),
        (
            "    /**\n     * Do not remove the key from the local instance and replace existing key on the remote instance.\n     */",
            "    /** 复制并替换：保留源键且覆盖目标节点上的同名键。 */\n",
        ),
    ],
)

# --- NodeType ---

_add(
    f"{_API}NodeType.java",
    [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n"
            " * Redis 集群节点类型。\n"
            " * <p>用于区分主节点、从节点与 Sentinel 节点。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " */",
        ),
    ],
)

# --- ObjectEncoding ---

_add(
    f"{_API}ObjectEncoding.java",
    [
        (
            "/**\n * enum type from https://redis.io/docs/latest/commands/object-encoding/\n *\n * @author seakider\n */",
            "/**\n"
            " * Redis {@code OBJECT ENCODING} 返回值枚举。\n"
            " * <p>对应命令文档：https://redis.io/docs/latest/commands/object-encoding/\n"
            " *\n"
            " * @author seakider\n"
            " */",
        ),
        (
            "    /**\n     * Normal string encoding.\n     */",
            "    /** 普通字符串编码（raw）。 */\n",
        ),
        (
            "    /**\n     * Strings representing integers in a 64-bit signed interval.\n     */",
            "    /** 64 位有符号整数区间内的整数字符串编码。 */\n",
        ),
        (
            "    /**\n     * Strings with lengths up to the hardcoded limit of OBJ_ENCODING_EMBSTR_SIZE_LIMIT or 44 bytes.\n     */",
            "    /** 长度不超过 OBJ_ENCODING_EMBSTR_SIZE_LIMIT（44 字节）的嵌入式字符串编码。 */\n",
        ),
        (
            "    /**\n     * An old list encoding.\n     * No longer used.\n     */",
            "    /** 旧版 list 编码，已不再使用。 */\n",
        ),
        (
            "    /**\n     * A space-efficient encoding used for small lists.\n     * Redis <= 6.2\n     */",
            "    /** 小 list 的空间优化编码（Redis &lt;= 6.2）。 */\n",
        ),
        (
            "    /**\n     * A space-efficient encoding used for small lists.\n     * Redis >= 7.0\n     */",
            "    /** 小 list 的空间优化编码（Redis &gt;= 7.0）。 */\n",
        ),
        (
            "    /**\n     * Encoded as linkedlist of ziplists or listpacks.\n     */",
            "    /** quicklist：由 ziplist 或 listpack 组成的链表。 */\n",
        ),
        (
            "    /**\n     * Normal set encoding.\n     */",
            "    /** 普通 set 哈希表编码。 */\n",
        ),
        (
            "    /**\n     * Small sets composed solely of integers encoding.\n     */",
            "    /** 仅含整数的小 set 专用编码。 */\n",
        ),
        (
            "    /**\n     * An old hash encoding.\n     * No longer used\n     */",
            "    /** 旧版 hash 编码，已不再使用。 */\n",
        ),
        (
            "    /**\n     * Normal sorted set encoding\n     */",
            "    /** 普通 sorted set 跳表编码。 */\n",
        ),
        (
            "    /**\n     * Encoded as a radix tree of listpacks\n     */",
            "    /** stream：由 listpack 构成的 radix tree 编码。 */\n",
        ),
        (
            "    /**\n     * Key is not exist.\n     */",
            "    /** 键不存在。 */\n",
        ),
        (
            "    /**\n     * This means redis support new type and this Enum not defined.\n     */",
            "    /** Redis 新增编码类型，当前枚举尚未定义。 */\n",
        ),
    ],
)

# --- ObjectListener ---

_add(
    f"{_API}ObjectListener.java",
    [
        (
            "/**\n * Redisson Object Event listener for Expired or Deleted event.\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n"
            " * Redisson 对象事件监听器标记接口。\n"
            " * <p>用于过期（{@link ExpiredObjectListener}）或删除（{@link DeletedObjectListener}）等事件回调。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " */",
        ),
    ],
)

# --- RArray ---

_rarray = [
    (
        "/**\n * Array object.\n * <p>\n * Stores values by sparse non-negative array index.\n * <p>\n * Requires <b>Redis 8.8 or higher.</b>\n *\n * @param <V> value type\n *\n * @author lamnt2008\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis Array 对象同步 API。\n"
        " * <p>以稀疏非负数组下标存储值；需要 <b>Redis 8.8 及以上</b>。\n"
        " *\n"
        " * @param <V> 值类型\n"
        " * @author lamnt2008\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _pair(
        "Returns an iterator over the entries stored in this array.\n     * Entries are returned in ascending array index order.",
        "返回本数组条目的迭代器，按数组下标升序返回。",
        None,
        "entries iterator",
        "条目迭代器",
    ),
    _pair(
        "Returns an iterator over the entries stored in this array.\n     * Entries are returned in ascending array index order.\n     * Entries are fetched in batches with the specified page size.",
        "返回本数组条目的迭代器，按升序返回；以指定页面大小分批拉取。",
        [("count", "page size hint, maps to the {@code ARSCAN COUNT} option", "分页大小提示，对应 {@code ARSCAN COUNT} 选项")],
        "entries iterator",
        "条目迭代器",
    ),
    _pair(
        "Returns a sequential stream of the entries stored in this array.\n     * Entries are returned in ascending array index order.",
        "返回本数组条目的顺序 Stream，按数组下标升序。",
        None,
        "entries stream",
        "条目 Stream",
    ),
    _pair(
        "Returns the next array index used by {@code insert(...)} or {@code ring(...)}.",
        "返回 {@code insert(...)} 或 {@code ring(...)} 使用的下一个插入下标。",
        None,
        "next insert index, or {@code null} if insert cursor is exhausted",
        "下一个插入下标；游标耗尽时返回 {@code null}",
    ),
    *_array_common_pairs(),
]
_add(f"{_API}RArray.java", _rarray)

# --- RArrayAsync ---

_rarray_async = [
    (
        "/**\n * Async interface for Array object.\n *\n * @param <V> value type\n *\n * @author lamnt2008\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis Array 对象异步 API。\n"
        " * <p>方法返回 {@link RFuture}，适用于非阻塞调用。\n"
        " *\n"
        " * @param <V> 值类型\n"
        " * @author lamnt2008\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _pair(
        "Returns an async iterator over the entries stored in this array.\n     * Entries are returned in ascending array index order.",
        "返回本数组条目的异步迭代器，按数组下标升序返回。",
        None,
        "entries iterator",
        "异步条目迭代器",
    ),
    _pair(
        "Returns an async iterator over the entries stored in this array.\n     * Entries are returned in ascending array index order.\n     * Entries are fetched in batches with the specified page size.",
        "返回本数组条目的异步迭代器，按升序分批拉取。",
        [("count", "page size hint, maps to the {@code ARSCAN COUNT} option", "分页大小提示，对应 {@code ARSCAN COUNT} 选项")],
        "entries iterator",
        "异步条目迭代器",
    ),
    _pair(
        "Returns the next array index used by {@code insertAsync(...)} or {@code ringAsync(...)}.",
        "返回 {@code insertAsync(...)} 或 {@code ringAsync(...)} 使用的下一个插入下标。",
        None,
        "next insert index, or {@code null} if insert cursor is exhausted",
        "下一个插入下标；游标耗尽时返回 {@code null}",
    ),
    *_array_common_pairs(),
]
_add(f"{_API}RArrayAsync.java", _rarray_async)

# --- RArrayReactive ---

_rarray_reactive = [
    (
        "/**\n * Reactive interface for Array object.\n *\n * @param <V> value type\n *\n * @author lamnt2008\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis Array 对象 Project Reactor 响应式 API。\n"
        " * <p>读写操作返回 {@link Mono} 或 {@link Flux}。\n"
        " *\n"
        " * @param <V> 值类型\n"
        " * @author lamnt2008\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _pair(
        "Returns a stream of the entries stored in this array.\n     * Entries are emitted in ascending array index order.",
        "返回本数组条目的响应式流，按数组下标升序发射。",
        None,
        "entries flux",
        "条目 Flux",
    ),
    _pair(
        "Returns the next array index used by {@code insert(...)} or {@code ring(...)}.",
        "返回 {@code insert(...)} 或 {@code ring(...)} 使用的下一个插入下标。",
        None,
        "next insert index, or empty {@link Mono} if insert cursor is exhausted",
        "下一个插入下标；游标耗尽时返回空 {@link Mono}",
    ),
    *_array_common_pairs(),
]
_add(f"{_API}RArrayReactive.java", _rarray_reactive)

# --- RArrayRx ---

_rarray_rx = [
    (
        "/**\n * RxJava3 interface for Array object.\n *\n * @param <V> value type\n *\n * @author lamnt2008\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis Array 对象 RxJava 3 响应式 API。\n"
        " * <p>读写操作返回 {@link Single}、{@link Maybe} 或 {@link Flowable}。\n"
        " *\n"
        " * @param <V> 值类型\n"
        " * @author lamnt2008\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _pair(
        "Returns a stream of the entries stored in this array.\n     * Entries are emitted in ascending array index order.",
        "返回本数组条目的响应式流，按数组下标升序发射。",
        None,
        "entries flowable",
        "条目 Flowable",
    ),
    _pair(
        "Returns the next array index used by {@code insert(...)} or {@code ring(...)}.",
        "返回 {@code insert(...)} 或 {@code ring(...)} 使用的下一个插入下标。",
        None,
        "next insert index, or empty {@link Maybe} if insert cursor is exhausted",
        "下一个插入下标；游标耗尽时返回空 {@link Maybe}",
    ),
    *_array_common_pairs(),
]
_add(f"{_API}RArrayRx.java", _rarray_rx)

# --- RAtomicDouble ---

_add(
    f"{_API}RAtomicDouble.java",
    [
        (
            "/**\n * Distributed implementation to the AtomicDouble\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n"
            " * 分布式双精度原子变量 {@link java.util.concurrent.atomic.AtomicDouble} 的 Redis 实现。\n"
            " * <p>基于 {@code INCRBYFLOAT} 等命令提供原子读写与 CAS 操作。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " */",
        ),
        _pair(
            "Atomically deletes the value if it satisfies the condition\n     * defined by the specified arguments.",
            "若当前值满足 {@link CompareAndDeleteArgs} 定义的条件，则原子删除。",
            [("args", "compare and delete arguments", "比较并删除参数")],
            "{@code true} if deleted, {@code false} otherwise",
            "删除成功返回 {@code true}，否则 {@code false}",
        ),
        _pair(
            "Atomically decrements by one the current value.",
            "将当前值原子减一并返回旧值。",
            None,
            "the previous value",
            "减一前的值",
        ),
        _pair(
            "Atomically adds the given value to the current value.",
            "将给定增量原子加到当前值上。",
            [("delta", "the value to add", "要增加的增量")],
            "the updated value",
            "更新后的值",
        ),
        (
            "    /**\n     * Atomically sets the value to the given updated value\n     * only if the current value {@code ==} the expected value.\n     *\n     * @param expect the expected value\n     * @param update the new value\n     * @return true if successful; or false if the actual value\n     *         was not equal to the expected value.\n     */",
            "    /**\n     * 仅当当前值等于期望值时，原子设置为新值。\n     *\n     * @param expect 期望值\n     * @param update 新值\n     * @return 成功返回 {@code true}；实际值与期望值不等时返回 {@code false}\n     */",
        ),
        _pair(
            "Atomically decrements the current value by one.",
            "将当前值原子减一并返回新值。",
            None,
            "the updated value",
            "减一后的值",
        ),
        _pair(
            "Returns current value.",
            "返回当前值。",
            None,
            "current value",
            "当前值",
        ),
        (
            "    /**\n     * Returns and deletes object\n     * \n     * @return the current value\n     */",
            "    /**\n     * 读取当前值并删除该对象。\n     *\n     * @return 删除前的当前值\n     */",
        ),
        (
            "    /**\n     * Atomically adds the given value to the current value.\n     *\n     * @param delta the value to add\n     * @return the old value before the add\n     */",
            "    /**\n     * 将给定增量原子加到当前值上并返回旧值。\n     *\n     * @param delta 要增加的增量\n     * @return 加算前的旧值\n     */",
        ),
        _pair(
            "Atomically sets the given value and returns the old value.",
            "原子设置为新值并返回旧值。",
            [("newValue", "the new value", "新值")],
            "the old value",
            "旧值",
        ),
        _pair(
            "Atomically increments the current value by one.",
            "将当前值原子加一。",
            None,
            "the updated value",
            "加一后的值",
        ),
        (
            "    /**\n     * Atomically increments the current value by one.\n     *\n     * @return the old value\n     */",
            "    /**\n     * 将当前值原子加一并返回旧值。\n     *\n     * @return 加一前的旧值\n     */",
        ),
        _pair(
            "Atomically increments the current value according to the specified arguments.",
            "按 {@link DoubleIncrementArgs} 指定规则原子递增当前值。",
            [("args", "increment arguments", "递增参数")],
            "the updated value",
            "递增后的值",
        ),
        _pair(
            "Atomically sets the given value.",
            "原子设置给定值。",
            [("newValue", "the new value", "新值")],
            None,
            None,
        ),
        (
            "    /**\n     * Atomically sets the given value if current value is less than\n     * the special value\n     *\n     * @param less  compare value\n     * @param value newValue\n     * @return true when the value update is successful\n     */",
            "    /**\n     * 仅当当前值小于给定阈值时，原子设置为新值。\n     *\n     * @param less  比较阈值\n     * @param value 新值\n     * @return 更新成功返回 {@code true}\n     */",
        ),
        (
            "    /**\n     * Atomically sets the given value if current value is greater than\n     * the special value\n     *\n     * @param greater  compare value\n     * @param value newValue\n     * @return true when the value update is successful\n     */",
            "    /**\n     * 仅当当前值大于给定阈值时，原子设置为新值。\n     *\n     * @param greater  比较阈值\n     * @param value 新值\n     * @return 更新成功返回 {@code true}\n     */",
        ),
        (
            "    /**\n     * Adds object event listener\n     *\n     * @see org.redisson.api.listener.IncrByListener\n     * @see org.redisson.api.ExpiredObjectListener\n     * @see org.redisson.api.DeletedObjectListener\n     *\n     * @param listener object event listener\n     * @return listener id\n     */",
            "    /**\n"
            "     * 注册对象事件监听器。\n"
            "     *\n"
            "     * @see org.redisson.api.listener.IncrByListener\n"
            "     * @see org.redisson.api.ExpiredObjectListener\n"
            "     * @see org.redisson.api.DeletedObjectListener\n"
            "     *\n"
            "     * @param listener 对象事件监听器\n"
            "     * @return 监听器 ID\n"
            "     */",
        ),
    ],
)
