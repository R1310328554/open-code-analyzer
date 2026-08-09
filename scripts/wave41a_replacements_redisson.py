"""Chinese annotation replacements for Redisson 4.7.0 wave-41a core [0:15]."""
from __future__ import annotations

_R = "redisson/src/main/java/org/redisson/"
_API = "redisson/src/main/java/org/redisson/api/"
_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

W41A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}


def _add(rel: str, reps: list[tuple[str, str]]) -> None:
    W41A_REPLACEMENTS[rel] = reps
    W41A_REPLACEMENTS[rel.split("/")[-1]] = reps


def _jcache_methods(iface: str, contains_link: str, put_link: str) -> list[tuple[str, str]]:
    def _sub(s: str) -> str:
        return s.replace("__IFACE__", iface).replace("__CONTAINS__", contains_link).replace(
            "__PUT__", put_link
        )

    pairs: list[tuple[str, str]] = [
        (
            "    /**\n    * This method retrieves an entry from the cache.\n    *\n    * If the cache uses the read-through pattern, and the method would return null\n    * because the entry is not present in the cache, then the cache's {@link CacheLoader}\n    * will try to load the entry.\n    *\n    * @param key the key whose value should be returned\n    * @return the element, or null if the entry does not exist.\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws NullPointerException if the key is null\n    * @throws CacheException if there is a problem retrieving the entry from the cache\n    */",
            "    /**\n    * 从缓存中获取指定键的条目。\n    *\n    * 若启用 read-through 且缓存中不存在该键，\n    * 则通过 {@link CacheLoader} 尝试加载。\n    *\n    * @param key 要返回值的键\n    * @return 对应元素；不存在时返回 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws NullPointerException 键为 {@code null}\n    * @throws CacheException 读取条目时出错\n    */",
        ),
        (
            "    /**\n    * This method accepts a set of requested keys and retrieves a collection of entries from the\n    * {@link __IFACE__}, returning them as a {@link Map} of the associated values.\n    *\n    * If the cache uses the read-through pattern, and the method would return null for a key\n    * because an entry is not present in the cache, the Cache's {@link CacheLoader} will try to\n    * load the entry. If a key's entry cannot be loaded, the key will not appear in the Map.\n    *\n    * @param keys The keys whose values should be returned.\n    * @return A Map of entries associated with the given keys. If a key is not found\n    * in the cache, it will not be in the Map.\n    * @throws NullPointerException if keys is null or contains a null\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem retrieving the entries from the cache\n    */",
            "    /**\n    * 根据给定键集合批量从 {@link __IFACE__} 获取条目，\n    * 以 {@link Map} 形式返回键值映射。\n    *\n    * 若启用 read-through 且某键不在缓存中，\n    * 将通过 {@link CacheLoader} 尝试加载；加载失败的键不会出现在结果 Map 中。\n    *\n    * @param keys 要返回值的键集合\n    * @return 与给定键关联的条目 Map；未找到的键不在 Map 中\n    * @throws NullPointerException {@code keys} 为 {@code null} 或包含 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 批量读取条目时出错\n    */",
        ),
        (
            "    /**\n    * This method returns a Boolean true/false value, depending on whether the\n    * {@link __IFACE__} has a mapping for a key k such that key.equals(k).\n    *\n    *\n    * @param key the key with a possible mapping in the cache.\n    * @return true if such a mapping exists\n    * @throws NullPointerException if key is null\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 判断 {@link __IFACE__} 是否包含与给定键相等的映射。\n    *\n    * @param key 待检查的键\n    * @return 存在映射时返回 {@code true}\n    * @throws NullPointerException 键为 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 访问缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method places the given value V in the cache and associates it with the given key K.\n    *\n    * If the {@link __IFACE__} already has a mapping for the key, the previous\n    * value is replaced by the given value V.\n    * This occurs if and only if __CONTAINS__\n    * would return true.)\n    *\n    * @param key the key to place in the cache\n    * @param value the value to associate with the given key\n    * @return void\n    * @throws NullPointerException if the key or value is null\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 将给定值写入缓存并与键关联。\n    *\n    * 若键已存在映射，则用新值替换旧值\n    * （当且仅当 __CONTAINS__ 返回 {@code true} 时）。\n    *\n    * @param key 要写入的键\n    * @param value 与键关联的值\n    * @return void\n    * @throws NullPointerException 键或值为 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 写入缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method places the given key and value in the cache.\n    * Any value already in the cache is returned and replaced by the new given value.\n    * This occurs if and only if __CONTAINS__\n    * would return true.)\n    * If there was no value already in the cache, the method returns null.\n    *\n    * @param key the key to place in the cache\n    * @param value the value to associate with the given key\n    * @return the previous value in the cache, or null if none already existed\n    * @throws NullPointerException if the key or value is null\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 写入键值并返回被替换的旧值。\n    *\n    * 若键已存在映射则返回旧值并替换（当且仅当 __CONTAINS__ 返回 {@code true}）；\n    * 若原先不存在映射则返回 {@code null}。\n    *\n    * @param key 要写入的键\n    * @param value 与键关联的值\n    * @return 被替换的旧值；原先不存在时返回 {@code null}\n    * @throws NullPointerException 键或值为 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 写入缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method copies all of the entries from the given Map to the {@link __IFACE__}.\n    *\n    * This method is equivalent to calling\n    * __PUT__\n    * from key k to value v in the given Map.\n    *\n    * Individual puts may occur in any order.\n    *\n    * If entries in the cache corresponding to entries in the Map, or the Map itself, is\n    * changed or removed during this operation, then the behavior of this method is\n    * not defined.\n    *\n    * If default consistency mode is enabled, then each put is atomic but not\n    * the entire putAll operation. Listeners can observe individual updates.\n    *\n    * @param map the Map that contains the entries to be copied to the cache\n    * @return void\n    * @throws NullPointerException if the map is null or contains null keys or values.\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache.\n    */",
            "    /**\n    * 将给定 Map 中的全部条目复制到 {@link __IFACE__}。\n    *\n    * 等价于对 Map 中每个键值对调用一次 __PUT__；\n    * 各次写入顺序未定义。\n    *\n    * 若操作期间缓存或 Map 被并发修改，行为未定义。\n    * 默认一致性模式下每次 put 原子，但整体 putAll 不原子，监听器可观察到单次更新。\n    *\n    * @param map 要复制到缓存的条目 Map\n    * @return void\n    * @throws NullPointerException Map 为 {@code null} 或含 {@code null} 键/值\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 写入缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method places the given key and value in the cache atomically, if the key is\n    * not already associated with a value in the cache.\n    *\n    * @param key the key to place in the cache\n    * @param value the value to associate with the given key\n    * @return true if the value was successfully placed in the cache\n    * @throws NullPointerException if the key or value is null\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 若键尚未关联值，则原子写入键值。\n    *\n    * @param key 要写入的键\n    * @param value 与键关联的值\n    * @return 成功写入时返回 {@code true}\n    * @throws NullPointerException 键或值为 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 写入缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method deletes the mapping for a given key from the cache, if it is present.\n    *\n    * This occurs if and only if there is a mapping from key k to\n    * value v such that\n    * (key==null ? k==null : key.equals(k)).\n    *\n    *\n    This method returns true if the removal was successful,\n    * or false if there was no such mapping.\n    *\n    *\n    * @param key the key whose mapping will be deleted\n    * @return returns true if successful, or false if there was no mapping\n    * @throws NullPointerException if the key is null\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 若存在映射则删除指定键的条目。\n    *\n    * 当且仅当存在键 k 满足 {@code key==null ? k==null : key.equals(k)} 时删除；\n    * 删除成功返回 {@code true}，无映射时返回 {@code false}。\n    *\n    * @param key 要删除映射的键\n    * @return 删除成功返回 {@code true}，否则 {@code false}\n    * @throws NullPointerException 键为 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 访问缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method atomically removes a key's mapping only if it is currently mapped to the\n    * provided value.\n    *\n    * @param key the key whose mapping will be deleted\n    * @param oldValue the value that should be mapped to the given key\n    * @return returns true if successful, or false if there was no such mapping\n    * @throws NullPointerException if the key is null\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 仅当键当前映射为给定值时，原子删除该映射。\n    *\n    * @param key 要删除映射的键\n    * @param oldValue 期望与键关联的旧值\n    * @return 删除成功返回 {@code true}，否则 {@code false}\n    * @throws NullPointerException 键为 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 访问缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method atomically removes the entry for a key only if it is currently mapped to some\n    * value.\n    *\n    * @param key the given key\n    * @return the value if it existed, or null if it did not\n    * @throws NullPointerException if the key is null.\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 若键当前有映射，则原子删除并返回其值。\n    *\n    * @param key 给定键\n    * @return 存在映射时返回值，否则 {@code null}\n    * @throws NullPointerException 键为 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 访问缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method atomically replaces an entry only if the key is currently mapped to a\n    * given value.\n    *\n    * @param key the key associated with the given oldValue\n    * @param oldValue the value that should be associated with the key\n    * @param newValue the value that will be associated with the key\n    * @return true if the value was replaced, or false if not\n    * @throws NullPointerException if the key or values are null\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 仅当键当前映射为 {@code oldValue} 时，原子替换为 {@code newValue}。\n    *\n    * @param key 与旧值关联的键\n    * @param oldValue 期望的旧值\n    * @param newValue 替换后的新值\n    * @return 替换成功返回 {@code true}，否则 {@code false}\n    * @throws NullPointerException 键或值为 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 访问缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method atomically replaces an entry only if the key is currently mapped to some\n    * value.\n    *\n    * @param key the key mapped to the given value\n    * @param value the value mapped to the given key\n    * @return true if the value was replaced, or false if not\n    * @throws NullPointerException if the key or value is null\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 仅当键当前已有映射时，原子替换为新值。\n    *\n    * @param key 给定键\n    * @param value 替换后的新值\n    * @return 替换成功返回 {@code true}，否则 {@code false}\n    * @throws NullPointerException 键或值为 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 访问缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method atomically replaces a given key's value if and only if the key is currently\n    * mapped to a value.\n    *\n    * @param key the key associated with the given value\n    * @param value the value associated with the given key\n    * @return the previous value mapped to the given key, or\n    * null if there was no such mapping.\n    * @throws NullPointerException if the key or value is null\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 若键当前有映射，则原子替换并返回旧值。\n    *\n    * @param key 给定键\n    * @param value 替换后的新值\n    * @return 被替换的旧值；原先无映射时返回 {@code null}\n    * @throws NullPointerException 键或值为 {@code null}\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 访问缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method deletes the entries for the given keys.\n    *\n    * The order in which the individual entries are removed is undefined.\n    *\n    * For every entry in the key set, the following are called:\n    *\n    •   any registered {@link CacheEntryRemovedListener}s\n    •   if the cache is a write-through cache, the {@link CacheWriter}\n    * If the key set is empty, the {@link CacheWriter} is not called.\n    *\n    * @param keys the keys to remove\n    * @return void\n    * @throws NullPointerException if keys is null or if it contains a null key\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 删除给定键集合对应的条目。\n    *\n    * 各条目删除顺序未定义。对每个键会触发已注册的\n    * {@link CacheEntryRemovedListener}；若为 write-through 缓存还会调用\n    * {@link CacheWriter}。键集合为空时不调用 {@link CacheWriter}。\n    *\n    * @param keys 要删除的键集合\n    * @return void\n    * @throws NullPointerException {@code keys} 为 {@code null} 或含 {@code null} 键\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 访问缓存时出错\n    */",
        ),
        (
            "    /**\n    * This method empties the cache's contents, without notifying listeners or\n    * {@link CacheWriter}s.\n    *\n    * @return void\n    * @throws IllegalStateException if the cache is in a closed state\n    * @throws CacheException if there is a problem with the cache\n    */",
            "    /**\n    * 清空缓存内容，不通知监听器或 {@link CacheWriter}。\n    *\n    * @return void\n    * @throws IllegalStateException 缓存已关闭\n    * @throws CacheException 访问缓存时出错\n    */",
        ),
    ]
    return [(_sub(old), _sub(new)) for old, new in pairs]


# --- ScanIterator ---

_add(
    f"{_R}ScanIterator.java",
    [
        (
            _EMPTY_JDOC,
            "/**\n"
            " * Redis {@code SCAN} 族命令的迭代器 SPI。\n"
            " * <p>由各类集合实现提供同步/异步扫描能力，并支持在迭代过程中删除元素。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " */",
        ),
    ],
)

# --- ScanResult ---

_add(
    f"{_R}ScanResult.java",
    [
        (
            _EMPTY_JDOC,
            "/**\n"
            " * {@code SCAN} 单次迭代返回的结果容器。\n"
            " * <p>包含游标位置、本次扫描到的值集合及执行扫描的 Redis 客户端引用。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " * @param <R> 扫描值元素类型\n"
            " */",
        ),
    ],
)

# --- SlotCallback ---

_add(
    f"{_R}SlotCallback.java",
    [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n * @param <T> type of batch result\n * @param <R> type of result\n */",
            "/**\n"
            " * 集群槽位批量执行回调。\n"
            " * <p>按槽分组后构造 Redis 命令与参数，并在各分片结果返回后聚合为最终值。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " * @param <T> 单条 Redis 命令的批量结果类型\n"
            " * @param <R> 聚合后的最终结果类型\n"
            " */",
        ),
    ],
)

# --- Version ---

_add(
    f"{_R}Version.java",
    [
        (
            "import org.slf4j.LoggerFactory;\n\npublic class Version {",
            "import org.slf4j.LoggerFactory;\n\n"
            "/**\n"
            " * Redisson 版本信息工具。\n"
            " * <p>从 classpath 中的 {@code META-INF/MANIFEST.MF} 读取 Bundle 版本并写入日志。\n"
            " */\n"
            "public class Version {",
        ),
        (
            "            // skip it",
            "            // 忽略异常",
        ),
    ],
)

# --- VoidSlotCallback ---

_add(
    f"{_R}VoidSlotCallback.java",
    [
        (
            "/**\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n"
            " * 无返回值的 {@link SlotCallback} 实现。\n"
            " * <p>用于仅需执行副作用、不关心聚合结果的批量槽位操作。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " */",
        ),
    ],
)

# --- WriteBehindService ---

_add(
    f"{_R}WriteBehindService.java",
    [
        (
            _EMPTY_JDOC,
            "/**\n"
            " * Map Write-Behind 异步刷盘任务管理器。\n"
            " * <p>按 map 名称维护 {@link MapWriteBehindTask}，在 {@link MapOptions} 启用\n"
            " * write-behind 时启动后台批量写入。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " */",
        ),
    ],
)

# --- AsyncIterator ---

_add(
    f"{_API}AsyncIterator.java",
    [
        (
            "/**\n * Asynchronous iterator\n *\n * @author Nikita Koksharov\n *\n * @param <V> value type\n */",
            "/**\n"
            " * 异步迭代器接口。\n"
            " * <p>通过 {@link java.util.concurrent.CompletionStage} 非阻塞地判断是否有下一元素并取值。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " * @param <V> 元素类型\n"
            " */",
        ),
        (
            "    /**\n     * Returns <code>true</code> if more elements are available.\n     * <p>\n     * NOTE: each invocation returns a new instance of CompletionStage\n     *\n     * @return <code>true</code> if more elements are available, otherwise <code>false</code>\n     */",
            "    /**\n"
            "     * 判断是否还有更多元素。\n"
            "     * <p>注意：每次调用均返回新的 {@link CompletionStage} 实例。\n"
            "     *\n"
            "     * @return 有更多元素时返回 {@code true}，否则 {@code false}\n"
            "     */",
        ),
        (
            "    /**\n     * Returns next element or NoSuchElementException if no more elements available.\n     * <p>\n     * NOTE: each invocation returns a new instance of CompletionStage\n     *\n     * @return next element or NoSuchElementException\n     */",
            "    /**\n"
            "     * 返回下一元素；无更多元素时完成异常为 {@code NoSuchElementException}。\n"
            "     * <p>注意：每次调用均返回新的 {@link CompletionStage} 实例。\n"
            "     *\n"
            "     * @return 下一元素，或 {@code NoSuchElementException}\n"
            "     */",
        ),
    ],
)

# --- BaseSyncParams ---

_add(
    f"{_API}BaseSyncParams.java",
    [
        (
            "import java.time.Duration;\n\npublic abstract class BaseSyncParams<T> implements SyncArgs<T> {",
            "import java.time.Duration;\n\n"
            "/**\n"
            " * {@link SyncArgs} 同步参数的抽象基类。\n"
            " * <p>持有同步模式、失败处理策略与超时时间，供各类配置对象继承。\n"
            " *\n"
            " * @param <T> 链式调用返回的具体配置类型\n"
            " */\n"
            "public abstract class BaseSyncParams<T> implements SyncArgs<T> {",
        ),
    ],
)

# --- BatchOptions ---

_batch_options = [
    (
        "/**\n * Configuration for Batch object.\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RBatch} 批量操作的配置项。\n"
        " * <p>控制执行模式、响应超时、重试策略及主从/AOF 同步等选项。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "        /**\n         * Store batched invocations in Redis and execute them atomically as a single command.\n         * <p>\n         * Please note, that in cluster mode all objects should be on the same cluster slot.\n         * https://github.com/antirez/redis/issues/3682 \n         * \n         */\n        REDIS_READ_ATOMIC,",
        "        /**\n"
        "         * 将批量调用存入 Redis 并以单条命令原子执行（读路径）。\n"
        "         * <p>集群模式下所有键须位于同一 slot。\n"
        "         * https://github.com/antirez/redis/issues/3682\n"
        "         */\n"
        "        REDIS_READ_ATOMIC,",
    ),
    (
        "        /**\n         * Store batched invocations in Redis and execute them atomically as a single command.\n         * <p>\n         * Please note, that in cluster mode all objects should be on the same cluster slot.\n         * https://github.com/antirez/redis/issues/3682 \n         * \n         */\n        REDIS_WRITE_ATOMIC,",
        "        /**\n"
        "         * 将批量调用存入 Redis 并以单条命令原子执行（写路径）。\n"
        "         * <p>集群模式下所有键须位于同一 slot。\n"
        "         * https://github.com/antirez/redis/issues/3682\n"
        "         */\n"
        "        REDIS_WRITE_ATOMIC,",
    ),
    (
        "        /**\n         * Store batched invocations in memory on Redisson side and execute them on Redis.\n         * <p>\n         * Default mode\n         * \n         */\n        IN_MEMORY,",
        "        /**\n"
        "         * 在 Redisson 客户端内存中缓存批量调用，再逐条发往 Redis。\n"
        "         * <p>默认模式。\n"
        "         */\n"
        "        IN_MEMORY,",
    ),
    (
        "        /**\n         * Store batched invocations on Redisson side and executes them atomically on Redis as a single command.\n         * <p>\n         * Please note, that in cluster mode all objects should be on the same cluster slot.\n         * https://github.com/antirez/redis/issues/3682 \n         * \n         */\n        IN_MEMORY_ATOMIC,",
        "        /**\n"
        "         * 在 Redisson 端缓存批量调用，再以单条 Redis 命令原子执行。\n"
        "         * <p>集群模式下所有键须位于同一 slot。\n"
        "         * https://github.com/antirez/redis/issues/3682\n"
        "         */\n"
        "        IN_MEMORY_ATOMIC,",
    ),
    (
        "    /**\n     * Defines timeout for Redis response. \n     * Starts to countdown when Redis command has been successfully sent.\n     * <p>\n     * Default is <code>{@link BaseConfig#getTimeout()}</code>\n     *\n     * @param timeout value\n     * @param unit value\n     * @return self instance\n     */",
        "    /**\n"
        "     * 设置 Redis 响应超时。\n"
        "     * <p>自命令成功发送后开始计时；默认取 {@link BaseConfig#getTimeout()}。\n"
        "     *\n"
        "     * @param timeout 超时数值\n"
        "     * @param unit 时间单位\n"
        "     * @return 当前实例\n"
        "     */",
    ),
    (
        "    /**\n     * Defines attempts amount to send Redis commands batch\n     * if it hasn't been sent already.\n     * <p>\n     * Default is <code>{@link BaseConfig#getRetryAttempts()}</code>\n     * \n     * @param retryAttempts value\n     * @return self instance\n     */",
        "    /**\n"
        "     * 设置批量命令尚未成功发送时的重试次数。\n"
        "     * <p>默认取 {@link BaseConfig#getRetryAttempts()}。\n"
        "     *\n"
        "     * @param retryAttempts 重试次数\n"
        "     * @return 当前实例\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #retryDelay(DelayStrategy)} instead\n     * \n     * @param retryInterval time interval\n     * @param retryIntervalUnit time interval unit\n     * @return self instance\n     */",
        "    /**\n"
        "     * 请改用 {@link #retryDelay(DelayStrategy)}。\n"
        "     *\n"
        "     * @param retryInterval 重试间隔\n"
        "     * @param retryIntervalUnit 间隔时间单位\n"
        "     * @return 当前实例\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #sync(int, Duration)} instead\n     */",
        "    /**\n     * 请改用 {@link #sync(int, Duration)}。\n"
        "     */",
    ),
    (
        "    /**\n     * Synchronize write operations execution within defined timeout\n     * across specified amount of Redis slave nodes.\n     * <p>\n     * NOTE: Redis 3.0+ required\n     *\n     * @param slaves slaves amount for synchronization\n     * @param timeout synchronization timeout\n     * @return self instance\n     */",
        "    /**\n"
        "     * 在指定超时内，将写操作同步到给定数量的 Redis 从节点。\n"
        "     * <p>需要 Redis 3.0+。\n"
        "     *\n"
        "     * @param slaves 参与同步的从节点数量\n"
        "     * @param timeout 同步超时\n"
        "     * @return 当前实例\n"
        "     */",
    ),
    (
        "    /**\n     * Inform Redis not to send reply. This allows to save network traffic for commands with batch with big response.\n     * <p>\n     * NOTE: Redis 3.2+ required\n     *\n     * @return self instance\n     */",
        "    /**\n"
        "     * 告知 Redis 不返回应答，可节省大批量响应的网络流量。\n"
        "     * <p>需要 Redis 3.2+。\n"
        "     *\n"
        "     * @return 当前实例\n"
        "     */",
    ),
    (
        "    /**\n     * Synchronize write operations to the AOF within defined timeout\n     * across specified amount of Redis slave nodes and local Redis.\n     * <p>\n     * NOTE: Redis 7.2+ required\n     *\n     * @param localNum local Redis amount for synchronization\n     * @param slaves slaves amount for synchronization\n     * @param timeout synchronization timeout\n     * @return self instance\n     */",
        "    /**\n"
        "     * 在指定超时内，将写操作同步到 AOF 及给定数量的从节点与本地 Redis。\n"
        "     * <p>需要 Redis 7.2+。\n"
        "     *\n"
        "     * @param localNum 参与同步的本地 Redis 数量\n"
        "     * @param slaves 参与同步的从节点数量\n"
        "     * @param timeout 同步超时\n"
        "     * @return 当前实例\n"
        "     */",
    ),
    (
        "    /**\n     * Sets execution mode.\n     * \n     * @see ExecutionMode\n     * \n     * @param executionMode batch execution mode\n     * @return self instance\n     */",
        "    /**\n"
        "     * 设置批量执行模式。\n"
        "     *\n"
        "     * @see ExecutionMode\n"
        "     * @param executionMode 批量执行模式\n"
        "     * @return 当前实例\n"
        "     */",
    ),
    (
        "    /**\n     * Defines the delay strategy for a new attempt to send a batch.\n     * <p>\n     * Default is <code>{@link BaseConfig#getRetryDelay()}}</code>\n     *\n     * @see DecorrelatedJitterDelay\n     * @see EqualJitterDelay\n     * @see FullJitterDelay\n     * @see ConstantDelay\n     *\n     * @param retryDelay delay strategy implementation\n     * @return options instance\n     */",
        "    /**\n"
        "     * 设置批量发送失败后的重试延迟策略。\n"
        "     * <p>默认取 {@link BaseConfig#getRetryDelay()}。\n"
        "     *\n"
        "     * @see DecorrelatedJitterDelay\n"
        "     * @see EqualJitterDelay\n"
        "     * @see FullJitterDelay\n"
        "     * @see ConstantDelay\n"
        "     *\n"
        "     * @param retryDelay 延迟策略实现\n"
        "     * @return 当前实例\n"
        "     */",
    ),
]
_add(f"{_API}BatchOptions.java", _batch_options)

# --- BatchResult ---

_add(
    f"{_API}BatchResult.java",
    [
        (
            _EMPTY_JDOC,
            "/**\n"
            " * 批量命令执行结果。\n"
            " * <p>包含各子命令的响应列表及成功同步的从节点数量。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " * @param <E> 单条命令响应类型\n"
            " */",
        ),
        (
            "    /**\n     * Returns list of result objects for each command\n     * \n     * @return list of objects\n     */",
            "    /**\n"
            "     * 返回各子命令的结果对象列表。\n"
            "     *\n"
            "     * @return 结果对象列表\n"
            "     */",
        ),
        (
            "    /**\n     * Returns amount of successfully synchronized slaves during batch execution\n     * \n     * @return slaves amount\n     */",
            "    /**\n"
            "     * 返回批量执行期间成功同步的从节点数量。\n"
            "     *\n"
            "     * @return 从节点数量\n"
            "     */",
        ),
    ],
)

# --- CacheAsync ---

_cache_async = [
    (
        "/**\n * Asynchronous interface for JCache\n *\n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
        "/**\n"
        " * JCache（JSR-107）的异步 API 接口。\n"
        " * <p>各方法返回 {@link RFuture}，支持非阻塞缓存读写。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */",
    ),
] + _jcache_methods(
    "CacheAsync",
    "{@link #containsKeyAsync(Object) c.containsKey(k)}",
    "{@link #putAsync(Object, Object)} on this cache one time for each mapping",
)
_add(f"{_API}CacheAsync.java", _cache_async)

# --- CacheReactive ---

_cache_reactive = [
    (
        "/**\n * Reactive interface for JCache\n *\n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
        "/**\n"
        " * JCache（JSR-107）的 Reactive 风格 API 接口。\n"
        " * <p>各方法返回 Project Reactor 的 {@link Mono}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */",
    ),
] + _jcache_methods(
    "CacheReactive",
    "{@link #containsKey(Object) c.containsKey(k)}",
    "{@link #put(Object, Object) put(k, v)} on this cache one time for each mapping",
)
_add(f"{_API}CacheReactive.java", _cache_reactive)

# --- CacheRx ---

_cache_rx = [
    (
        "/**\n * RxJava2 interface for JCache\n *\n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
        "/**\n"
        " * JCache（JSR-107）的 RxJava 风格 API 接口。\n"
        " * <p>各方法返回 RxJava3 的 {@link Single}、{@link Maybe} 或 {@link Completable}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */",
    ),
] + _jcache_methods(
    "CacheRx",
    "{@link #containsKey(Object) c.containsKey(k)}",
    "{@link #put(Object, Object) put(k, v)} on this cache one time for each mapping",
)
_add(f"{_API}CacheRx.java", _cache_rx)

# --- CronSchedule ---

_cron = [
    (
        "/**\n * Cron expression object used in {@link RScheduledExecutorService}.\n * Fully compatible with quartz cron expression.\n * \n * @see RScheduledExecutorService#schedule(Runnable, CronSchedule)\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 用于 {@link RScheduledExecutorService} 的 Cron 表达式对象。\n"
        " * <p>与 Quartz Cron 表达式完全兼容。\n"
        " *\n"
        " * @see RScheduledExecutorService#schedule(Runnable, CronSchedule)\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Creates cron expression object with defined expression string\n     * \n     * @param expression of cron\n     * @return object\n     * @throws IllegalArgumentException\n     *             wrapping a ParseException if the expression is invalid\n     */",
        "    /**\n"
        "     * 根据 Cron 表达式字符串创建实例（使用系统默认时区）。\n"
        "     *\n"
        "     * @param expression Cron 表达式\n"
        "     * @return CronSchedule 实例\n"
        "     * @throws IllegalArgumentException 表达式无效时包装 {@code ParseException}\n"
        "     */",
    ),
    (
        "    /**\n     * Creates cron expression object with defined expression string and time-zone ID\n     *\n     * @param expression of cron\n     * @param zoneId id of zone\n     * @return object\n     * @throws IllegalArgumentException\n     *             wrapping a ParseException if the expression is invalid\n     */",
        "    /**\n"
        "     * 根据 Cron 表达式与时区 ID 创建实例。\n"
        "     *\n"
        "     * @param expression Cron 表达式\n"
        "     * @param zoneId 时区 ID\n"
        "     * @return CronSchedule 实例\n"
        "     * @throws IllegalArgumentException 表达式无效时包装 {@code ParseException}\n"
        "     */",
    ),
    (
        "    /**\n     * Creates cron expression which schedule task execution\n     * every day at the given time \n     * \n     * @param hour of schedule\n     * @param minute of schedule\n     * @return object\n     * @throws IllegalArgumentException\n     *             wrapping a ParseException if the expression is invalid\n     */",
        "    /**\n"
        "     * 创建每天在指定时刻执行任务的 Cron 表达式（系统默认时区）。\n"
        "     *\n"
        "     * @param hour 小时（0–23）\n"
        "     * @param minute 分钟（0–59）\n"
        "     * @return CronSchedule 实例\n"
        "     * @throws IllegalArgumentException 表达式无效时包装 {@code ParseException}\n"
        "     */",
    ),
    (
        "    /**\n     * Creates cron expression which schedule task execution\n     * every day at the given time in specified time-zone ID\n     *\n     * @param hour of schedule\n     * @param minute of schedule\n     * @param zoneId id of zone\n     * @return object\n     * @throws IllegalArgumentException\n     *             wrapping a ParseException if the expression is invalid\n     */",
        "    /**\n"
        "     * 创建每天在指定时刻、指定时区执行任务的 Cron 表达式。\n"
        "     *\n"
        "     * @param hour 小时（0–23）\n"
        "     * @param minute 分钟（0–59）\n"
        "     * @param zoneId 时区 ID\n"
        "     * @return CronSchedule 实例\n"
        "     * @throws IllegalArgumentException 表达式无效时包装 {@code ParseException}\n"
        "     */",
    ),
    (
        "    /**\n     * Creates cron expression which schedule task execution\n     * every given days of the week at the given time.\n     * Use Calendar object constants to define day.\n     * \n     * @param hour of schedule\n     * @param minute of schedule\n     * @param daysOfWeek - Calendar object constants\n     * @return object\n     */",
        "    /**\n"
        "     * 创建在指定星期几、指定时刻执行任务的 Cron 表达式（系统默认时区）。\n"
        "     * <p>使用 {@link java.util.Calendar} 常量表示星期。\n"
        "     *\n"
        "     * @param hour 小时（0–23）\n"
        "     * @param minute 分钟（0–59）\n"
        "     * @param daysOfWeek {@link java.util.Calendar} 星期常量\n"
        "     * @return CronSchedule 实例\n"
        "     */",
    ),
    (
        "    /**\n     * Creates cron expression which schedule task execution\n     * every given days of the week at the given time in specified time-zone ID.\n     * Use Calendar object constants to define day.\n     *\n     * @param hour of schedule\n     * @param minute of schedule\n     * @param zoneId id of zone\n     * @param daysOfWeek - Calendar object constants\n     * @return object\n     */",
        "    /**\n"
        "     * 创建在指定星期几、指定时刻与指定时区执行任务的 Cron 表达式。\n"
        "     * <p>使用 {@link java.util.Calendar} 常量表示星期。\n"
        "     *\n"
        "     * @param hour 小时（0–23）\n"
        "     * @param minute 分钟（0–59）\n"
        "     * @param zoneId 时区 ID\n"
        "     * @param daysOfWeek {@link java.util.Calendar} 星期常量\n"
        "     * @return CronSchedule 实例\n"
        "     */",
    ),
    (
        "    /**\n     * Creates cron expression which schedule task execution\n     * every given day of the month at the given time\n     * \n     * @param hour of schedule\n     * @param minute of schedule\n     * @param dayOfMonth of schedule\n     * @return object\n     */",
        "    /**\n"
        "     * 创建在每月指定日期、指定时刻执行任务的 Cron 表达式（系统默认时区）。\n"
        "     *\n"
        "     * @param dayOfMonth 月中日期（1–31）\n"
        "     * @param hour 小时（0–23）\n"
        "     * @param minute 分钟（0–59）\n"
        "     * @return CronSchedule 实例\n"
        "     */",
    ),
    (
        "    /**\n     * Creates cron expression which schedule task execution\n     * every given day of the month at the given time in specified time-zone ID.\n     *\n     * @param hour of schedule\n     * @param minute of schedule\n     * @param dayOfMonth of schedule\n     * @param zoneId id of zone\n     * @return object\n     */",
        "    /**\n"
        "     * 创建在每月指定日期、指定时刻与指定时区执行任务的 Cron 表达式。\n"
        "     *\n"
        "     * @param dayOfMonth 月中日期（1–31）\n"
        "     * @param hour 小时（0–23）\n"
        "     * @param minute 分钟（0–59）\n"
        "     * @param zoneId 时区 ID\n"
        "     * @return CronSchedule 实例\n"
        "     */",
    ),
]
_add(f"{_API}CronSchedule.java", _cron)

# --- CuckooFilterInfo ---

_cuckoo = [
    (
        "/**\n * Cuckoo filter information returned by the {@code CF.INFO} command.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@code CF.INFO} 命令返回的布谷鸟过滤器统计信息。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Creates instance from the raw list returned by {@code CF.INFO}.\n     * <p>\n     * The response is a flat list of alternating field names and values:\n     * {@code [field1, value1, field2, value2, ...]}.\n     *\n     * @param info raw response list\n     */",
        "    /**\n"
        "     * 从 {@code CF.INFO} 返回的原始列表构造实例。\n"
        "     * <p>响应为字段名与值交替的扁平列表：\n"
        "     * {@code [field1, value1, field2, value2, ...]}。\n"
        "     *\n"
        "     * @param info 原始响应列表\n"
        "     */",
    ),
    (
        "    /**\n     * Returns the memory size in bytes.\n     *\n     * @return size in bytes\n     */",
        "    /**\n     * 返回过滤器占用的内存字节数。\n     *\n     * @return 字节数\n     */",
    ),
    (
        "    /**\n     * Returns the number of buckets in the filter.\n     *\n     * @return number of buckets\n     */",
        "    /**\n     * 返回过滤器中的桶数量。\n     *\n     * @return 桶数量\n     */",
    ),
    (
        "    /**\n     * Returns the number of sub-filters.\n     *\n     * @return number of filters\n     */",
        "    /**\n     * 返回子过滤器数量。\n     *\n     * @return 子过滤器数量\n     */",
    ),
    (
        "    /**\n     * Returns the number of items inserted into the filter.\n     *\n     * @return number of inserted items\n     */",
        "    /**\n     * 返回已插入的元素数量。\n     *\n     * @return 已插入元素数\n     */",
    ),
    (
        "    /**\n     * Returns the number of items deleted from the filter.\n     *\n     * @return number of deleted items\n     */",
        "    /**\n     * 返回已删除的元素数量。\n     *\n     * @return 已删除元素数\n     */",
    ),
    (
        "    /**\n     * Returns the number of items each bucket can hold.\n     *\n     * @return bucket size\n     */",
        "    /**\n     * 返回每个桶可容纳的元素数量。\n     *\n     * @return 桶容量\n     */",
    ),
    (
        "    /**\n     * Returns the expansion rate.\n     *\n     * @return expansion rate\n     */",
        "    /**\n     * 返回扩容倍率。\n     *\n     * @return 扩容倍率\n     */",
    ),
    (
        "    /**\n     * Returns the maximum number of swap attempts\n     * before declaring the filter full.\n     *\n     * @return max iterations\n     */",
        "    /**\n     * 返回判定过滤器已满前的最大交换尝试次数。\n     *\n     * @return 最大迭代次数\n     */",
    ),
]
_add(f"{_API}CuckooFilterInfo.java", _cuckoo)
