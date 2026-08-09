"""Chinese annotation replacements for Redisson 4.7.0 wave-47a api [0:15]."""
from __future__ import annotations

_A = "redisson/src/main/java/org/redisson/api/"

# --- shared multimap method javadocs ---

_MM_SIZE = (
    "/**\n     * Returns the number of key-value pairs in this multimap.\n     *\n     * @return size of multimap\n     */",
    "/**\n     * 返回 multimap 中键值对总数。\n     *\n     * @return multimap 大小\n     */",
)
_MM_CONTAINS_KEY = (
    "/**\n     * Returns {@code true} if this multimap contains at least one key-value pair\n     * with the key {@code key}.\n     * \n     * @param key - map key\n     * @return <code>true</code> if contains a key\n     */",
    "/**\n     * 若 multimap 中存在键 {@code key} 的至少一个键值对则返回 {@code true}。\n     * \n     * @param key 映射键\n     * @return 包含该键时为 {@code true}\n     */",
)
_MM_CONTAINS_VALUE = (
    "/**\n     * Returns {@code true} if this multimap contains at least one key-value pair\n     * with the value {@code value}.\n     * \n     * @param value - map value\n     * @return <code>true</code> if contains a value\n     */",
    "/**\n     * 若 multimap 中存在值 {@code value} 的至少一个键值对则返回 {@code true}。\n     * \n     * @param value 映射值\n     * @return 包含该值时为 {@code true}\n     */",
)
_MM_CONTAINS_ENTRY = (
    "/**\n     * Returns {@code true} if this multimap contains at least one key-value pair\n     * with the key {@code key} and the value {@code value}.\n     * \n     * @param key - map key\n     * @param value - map value\n     * @return <code>true</code> if contains an entry\n     */",
    "/**\n     * 若 multimap 中存在键 {@code key} 且值 {@code value} 的键值对则返回 {@code true}。\n     * \n     * @param key 映射键\n     * @param value 映射值\n     * @return 包含该条目时为 {@code true}\n     */",
)
_MM_PUT = (
    "/**\n     * Stores a key-value pair in this multimap.\n     *\n     * <p>Some multimap implementations allow duplicate key-value pairs, in which\n     * case {@code put} always adds a new key-value pair and increases the\n     * multimap size by 1. Other implementations prohibit duplicates, and storing\n     * a key-value pair that's already in the multimap has no effect.\n     *\n     * @param key - map key\n     * @param value - map value\n     * @return {@code true} if the method increased the size of the multimap, or\n     *     {@code false} if the multimap already contained the key-value pair and\n     *     doesn't allow duplicates\n     */",
    "/**\n     * 向 multimap 存入一个键值对。\n     *\n     * <p>部分实现允许重复键值对，此时 {@code put} 总是新增并令大小加 1；\n     * 其他实现禁止重复，已存在的键值对再次写入无效。\n     *\n     * @param key 映射键\n     * @param value 映射值\n     * @return 若 multimap 大小增加则为 {@code true}；\n     *     若已存在且不允许重复则为 {@code false}\n     */",
)
_MM_REMOVE = (
    "/**\n     * Removes a single key-value pair with the key {@code key} and the value\n     * {@code value} from this multimap, if such exists. If multiple key-value\n     * pairs in the multimap fit this description, which one is removed is\n     * unspecified.\n     *\n     * @param key - map key\n     * @param value - map value\n     * @return {@code true} if the multimap changed\n     */",
    "/**\n     * 移除键 {@code key} 且值 {@code value} 的一个键值对（若存在）。\n     * 若存在多个匹配项，移除哪一个未定义。\n     *\n     * @param key 映射键\n     * @param value 映射值\n     * @return multimap 发生变化时为 {@code true}\n     */",
)
_MM_PUTALL = (
    "/**\n     * Stores a key-value pair in this multimap for each of {@code values}, all\n     * using the same key, {@code key}. Equivalent to (but expected to be more\n     * efficient than): <pre>   {@code\n     *\n     *   for (V value : values) {\n     *     put(key, value);\n     *   }}</pre>\n     *\n     * <p>In particular, this is a no-op if {@code values} is empty.\n     * \n     * @param key - map key\n     * @param values - map values\n     * @return {@code true} if the multimap changed\n     */",
    "/**\n     * 将 {@code values} 中每个值以同一键 {@code key} 写入 multimap。\n     * 等价于循环调用 {@code put(key, value)}，但通常更高效。\n     *\n     * <p>若 {@code values} 为空则为空操作。\n     * \n     * @param key 映射键\n     * @param values 映射值集合\n     * @return multimap 发生变化时为 {@code true}\n     */",
)
_MM_PUTALL_ASYNC = (
    "/**\n     * Stores a key-value pair in this multimap for each of {@code values}, all\n     * using the same key, {@code key}. Equivalent to (but expected to be more\n     * efficient than): <pre>   {@code\n     *\n     *   for (V value : values) {\n     *     put(key, value);\n     *   }}</pre>\n     *\n     * <p>In particular, this is a no-op if {@code values} is empty.\n     *\n     * @param key - map key\n     * @param values - map values\n     * @return {@code true} if the multimap changed\n     */",
    "/**\n     * 将 {@code values} 中每个值以同一键 {@code key} 写入 multimap。\n     * 等价于循环调用 {@code put(key, value)}，但通常更高效。\n     *\n     * <p>若 {@code values} 为空则为空操作。\n     *\n     * @param key 映射键\n     * @param values 映射值集合\n     * @return multimap 发生变化时为 {@code true}\n     */",
)
_MM_KEYSIZE = (
    "/**\n     * Returns the number of key-value pairs in this multimap.\n     *\n     * @return keys amount\n     */",
    "/**\n     * 返回 multimap 中不重复键的数量。\n     *\n     * @return 键数量\n     */",
)
_MM_FAST_REPLACE = (
    "/**\n     * Stores a collection of values with the same key, replacing any existing\n     * values for that key. Is faster by not returning the values.\n     *\n     * @param key - map key\n     * @param values - map values\n     */",
    "/**\n     * 用 {@code values} 替换指定键的全部已有值（快速版，不返回旧值）。\n     *\n     * @param key 映射键\n     * @param values 新值集合\n     */",
)
_MM_FAST_REMOVE = (
    "/**\n     * Removes <code>keys</code> from map by one operation\n     *\n     * Works faster than <code>RMultimap.remove</code> but not returning\n     * the value associated with <code>key</code>\n     *\n     * @param keys - map keys\n     * @return the number of keys that were removed from the hash, not including specified but non existing keys\n     */",
    "/**\n     * 一次操作移除多个键及其全部关联值。\n     *\n     * 比 {@code RMultimap.remove} 更快，但不返回被移除的值。\n     *\n     * @param keys 待移除的映射键\n     * @return 实际从 hash 中移除的键数量（不含不存在的键）\n     */",
)
_MM_FAST_REMOVE_VAL = (
    "/**\n     * Removes <code>values</code> from map by one operation\n     *\n     * @param values map values\n     * @return the number of values that were removed from the map\n     */",
    "/**\n     * 一次操作从 multimap 中移除多个值。\n     *\n     * @param values 待移除的映射值\n     * @return 实际移除的值数量\n     */",
)
_MM_READ_KEYS = (
    "/**\n     * Read all keys at once\n     *\n     * @return keys\n     */",
    "/**\n     * 一次性读取全部键。\n     *\n     * @return 键集合\n     */",
)
_MM_EXPIRE_KEY = (
    "/**\n     * Set a timeout for key. After the timeout has expired,\n     * the key and its values will automatically be deleted.\n     * \n     * @param key - map key\n     * @param timeToLive - timeout before key will be deleted\n     * @param timeUnit - timeout time unit\n     * @return <code>true</code> if key exists and the timeout was set and <code>false</code> if key not exists\n     */",
    "/**\n     * 为指定键设置过期时间；到期后键及其全部值自动删除。\n     * \n     * @param key 映射键\n     * @param timeToLive 存活时间\n     * @param timeUnit 时间单位\n     * @return 键存在且设置成功时为 {@code true}；键不存在时为 {@code false}\n     */",
)
_MM_EXPIRE_KEY_ASYNC = (
    "/**\n     * Set a timeout for key in async mode. After the timeout has expired,\n     * the key and its values will automatically be deleted.\n     * \n     * @param key - map key\n     * @param timeToLive - timeout before key will be deleted\n     * @param timeUnit - timeout time unit\n     * @return <code>true</code> if key exists and the timeout was set and <code>false</code> if key not exists\n     */",
    "/**\n     * 异步为指定键设置过期时间；到期后键及其全部值自动删除。\n     * \n     * @param key 映射键\n     * @param timeToLive 存活时间\n     * @param timeUnit 时间单位\n     * @return 键存在且设置成功时为 {@code true}；键不存在时为 {@code false}\n     */",
)
_MM_EXPIRE_KEY_REACTIVE = (
    "/**\n     * Set a timeout for key. After the timeout has expired, the key and its values will automatically be deleted.\n     *\n     * @param key - map key\n     * @param timeToLive - timeout before key will be deleted\n     * @param timeUnit - timeout time unit\n     * @return A Single that will emit <code>true</code> if key exists and the timeout was set and <code>false</code>\n     * if key not exists\n     */",
    "/**\n     * 为指定键设置过期时间；到期后键及其全部值自动删除。\n     *\n     * @param key 映射键\n     * @param timeToLive 存活时间\n     * @param timeUnit 时间单位\n     * @return 发出 {@code true}（键存在且设置成功）或 {@code false}（键不存在）的 Mono/Single\n     */",
)

# --- RMaps family ---

_MAPS_ASYNC = [
    (
        "/**\n * Async interface for mass operations with Map objects.\n *\n * @author Nikita Koksharov\n *\n * @param <K> field type\n * @param <V> value type\n */",
        "/**\n * Map 批量操作的异步 API。\n * <p>各方法返回 {@link RFuture}；用于一次性或分批写入多个 Redis Hash。\n *\n * @author Nikita Koksharov\n * @param <K> 字段类型\n * @param <V> 值类型\n */",
    ),
    (
        "/**\n     * Stores Map objects mapped by name. Each object replaces\n     * the whole Map object stored under the same name.\n     *\n     * @param maps Map objects mapped by name\n     * @return void\n     */",
        "/**\n     * 按名称批量写入 Map 对象，每个对象替换同名 Redis Map 的全部内容。\n     *\n     * @param maps Map 对象映射（键名为 Redis 对象名）\n     * @return void\n     */",
    ),
    (
        "/**\n     * Stores Map objects mapped by name. Each object replaces\n     * the whole Map object stored under the same name.\n     * <p>\n     * Objects are written in portions defined by <code>batchSize</code>.\n     *\n     * @param maps Map objects mapped by name\n     * @param batchSize amount of Map objects written per portion\n     * @return void\n     */",
        "/**\n     * 按名称批量写入 Map 对象，每个对象替换同名 Redis Map 的全部内容。\n     * <p>\n     * 按 {@code batchSize} 分批写入，降低单次内存与网络压力。\n     *\n     * @param maps Map 对象映射（键名为 Redis 对象名）\n     * @param batchSize 每批写入的 Map 数量\n     * @return void\n     */",
    ),
    (
        "/**\n     * Returns import object for Map objects sharing the field names defined in <code>args</code>.\n     * <p>\n     * Suits for importing an amount of objects which doesn't fit in memory,\n     * as objects are added one by one instead of being collected in a Map.\n     *\n     * @param args import arguments object\n     * @return import object\n     */",
        "/**\n     * 返回共享 {@code args} 中字段名的 Map 批量导入对象。\n     * <p>\n     * 适合无法一次性载入内存的大量对象导入，\n     * 逐个添加而非先收集到 Map。\n     *\n     * @param args 导入参数\n     * @return 导入对象\n     */",
    ),
]

_MAPS_IMPORT = [
    (
        "/**\n * Import session for Map objects sharing the same field names.\n * <p>\n * Buffered Map objects are written to Redis when the buffer reaches the configured\n * batch size and when {@link #flush()} is called. Objects added but not flushed are\n * never written, so {@link #flush()} has to be called before the import object is discarded.\n * <p>\n * Each imported Map object replaces the whole object stored under the same name.\n * <p>\n * Map objects can be added from several threads.\n *\n * @author Nikita Koksharov\n *\n * @param <K> field type\n * @param <V> value type\n */",
        "/**\n * 共享相同字段名的 Map 批量导入会话。\n * <p>\n * 缓冲达到配置的 batch 大小或调用 {@link #flush()} 时写入 Redis；\n * 未 flush 的数据不会写入，丢弃导入对象前必须调用 {@link #flush()}。\n * <p>\n * 每个导入的 Map 替换同名 Redis 对象的全部内容。\n * <p>\n * 支持多线程并发添加。\n *\n * @author Nikita Koksharov\n * @param <K> 字段类型\n * @param <V> 值类型\n */",
    ),
    (
        "/**\n     * Adds Map object stored under the specified <code>name</code>.\n     * <p>\n     * Values are matched to the field names defined for this import object by position,\n     * so the amount of values has to be equal to the amount of field names.\n     * Values are encoded immediately.\n     *\n     * @param name name of object\n     * @param values values ordered as the defined field names\n     */",
        "/**\n     * 添加存储于 {@code name} 下的 Map 对象（变参形式）。\n     * <p>\n     * 值按位置与导入对象定义的字段名一一对应，数量须与字段数相等；\n     * 值会立即编码。\n     *\n     * @param name Redis 对象名\n     * @param values 与字段名顺序对应的值\n     */",
    ),
    (
        "/**\n     * Adds Map object stored under the specified <code>name</code>.\n     * <p>\n     * Values are matched to the field names defined for this import object by position,\n     * so the amount of values has to be equal to the amount of field names.\n     * Values are encoded immediately.\n     *\n     * @param name name of object\n     * @param values values ordered as the defined field names\n     */",
        "/**\n     * 添加存储于 {@code name} 下的 Map 对象（List 形式）。\n     * <p>\n     * 值按位置与导入对象定义的字段名一一对应，数量须与字段数相等；\n     * 值会立即编码。\n     *\n     * @param name Redis 对象名\n     * @param values 与字段名顺序对应的值\n     */",
    ),
    (
        "/**\n     * Writes all buffered Map objects.\n     */",
        "/**\n     * 将缓冲中的全部 Map 对象写入 Redis。\n     */",
    ),
    (
        "/**\n     * Returns the amount of Map objects written by this import object.\n     *\n     * @return amount of Map objects\n     */",
        "/**\n     * 返回本导入对象已成功写入 Redis 的 Map 数量。\n     *\n     * @return 已导入的 Map 数量\n     */",
    ),
]

_MAPS_IMPORT_ASYNC = [
    (
        "/**\n * Async import session for Map objects sharing the same field names.\n *\n * @author Nikita Koksharov\n *\n * @param <K> field type\n * @param <V> value type\n */",
        "/**\n * 共享相同字段名的 Map 批量导入异步 API。\n * <p>各方法返回 {@link RFuture}；缓冲满或 flush 时写入 Redis。\n *\n * @author Nikita Koksharov\n * @param <K> 字段类型\n * @param <V> 值类型\n */",
    ),
    (
        "/**\n     * Adds Map object stored under the specified <code>name</code> in async mode.\n     * <p>\n     * Values are matched to the field names defined for this import object by position,\n     * so the amount of values has to be equal to the amount of field names.\n     * Values are encoded immediately.\n     *\n     * @param name name of object\n     * @param values values ordered as the defined field names\n     * @return void\n     */",
        "/**\n     * 异步添加存储于 {@code name} 下的 Map 对象（变参形式）。\n     * <p>\n     * 值按位置与字段名一一对应，数量须与字段数相等；值会立即编码。\n     *\n     * @param name Redis 对象名\n     * @param values 与字段名顺序对应的值\n     * @return void\n     */",
    ),
    (
        "/**\n     * Adds Map object stored under the specified <code>name</code> in async mode.\n     * <p>\n     * Values are matched to the field names defined for this import object by position,\n     * so the amount of values has to be equal to the amount of field names.\n     * Values are encoded immediately.\n     *\n     * @param name name of object\n     * @param values values ordered as the defined field names\n     * @return void\n     */",
        "/**\n     * 异步添加存储于 {@code name} 下的 Map 对象（List 形式）。\n     * <p>\n     * 值按位置与字段名一一对应，数量须与字段数相等；值会立即编码。\n     *\n     * @param name Redis 对象名\n     * @param values 与字段名顺序对应的值\n     * @return void\n     */",
    ),
    (
        "/**\n     * Writes all buffered Map objects in async mode.\n     *\n     * @return void\n     */",
        "/**\n     * 异步将缓冲中的全部 Map 对象写入 Redis。\n     *\n     * @return void\n     */",
    ),
]

_MAPS_IMPORT_REACTIVE = [
    (
        "/**\n * Reactive import session for Map objects sharing the same field names.\n * <p>\n * Buffered Map objects are written when the buffer reaches the configured batch size\n * and when {@link #flush()} is called. Objects added but not flushed are never written.\n *\n * @author Nikita Koksharov\n *\n * @param <K> field type\n * @param <V> value type\n */",
        "/**\n * 共享相同字段名的 Map 批量导入 Reactor API。\n * <p>\n * 缓冲达到 batch 大小或调用 {@link #flush()} 时写入；未 flush 的数据不会写入。\n *\n * @author Nikita Koksharov\n * @param <K> 字段类型\n * @param <V> 值类型\n */",
    ),
    (
        "/**\n     * Adds Map object stored under the specified <code>name</code>.\n     * <p>\n     * Values are matched to the field names defined for this import object by position.\n     *\n     * @param name name of object\n     * @param values values ordered as the defined field names\n     * @return void\n     */",
        "/**\n     * 添加存储于 {@code name} 下的 Map 对象（变参形式）。\n     * <p>\n     * 值按位置与导入对象定义的字段名一一对应。\n     *\n     * @param name Redis 对象名\n     * @param values 与字段名顺序对应的值\n     * @return void\n     */",
    ),
    (
        "/**\n     * Adds Map object stored under the specified <code>name</code>.\n     * <p>\n     * Values are matched to the field names defined for this import object by position.\n     *\n     * @param name name of object\n     * @param values values ordered as the defined field names\n     * @return void\n     */",
        "/**\n     * 添加存储于 {@code name} 下的 Map 对象（List 形式）。\n     * <p>\n     * 值按位置与导入对象定义的字段名一一对应。\n     *\n     * @param name Redis 对象名\n     * @param values 与字段名顺序对应的值\n     * @return void\n     */",
    ),
    (
        "/**\n     * Writes all buffered Map objects.\n     *\n     * @return void\n     */",
        "/**\n     * 将缓冲中的全部 Map 对象写入 Redis。\n     *\n     * @return void\n     */",
    ),
    (
        "/**\n     * Returns the amount of Map objects written by this import object.\n     *\n     * @return amount of Map objects\n     */",
        "/**\n     * 返回本导入对象已成功写入 Redis 的 Map 数量。\n     *\n     * @return 已导入的 Map 数量\n     */",
    ),
]

_MAPS_IMPORT_RX = [
    (
        "/**\n * Rx import session for Map objects sharing the same field names.\n * <p>\n * Buffered Map objects are written when the buffer reaches the configured batch size\n * and when {@link #flush()} is called. Objects added but not flushed are never written.\n *\n * @author Nikita Koksharov\n *\n * @param <K> field type\n * @param <V> value type\n */",
        "/**\n * 共享相同字段名的 Map 批量导入 RxJava API。\n * <p>\n * 缓冲达到 batch 大小或调用 {@link #flush()} 时写入；未 flush 的数据不会写入。\n *\n * @author Nikita Koksharov\n * @param <K> 字段类型\n * @param <V> 值类型\n */",
    ),
    (
        "/**\n     * Adds Map object stored under the specified <code>name</code>.\n     * <p>\n     * Values are matched to the field names defined for this import object by position.\n     *\n     * @param name name of object\n     * @param values values ordered as the defined field names\n     * @return void\n     */",
        "/**\n     * 添加存储于 {@code name} 下的 Map 对象（变参形式）。\n     * <p>\n     * 值按位置与导入对象定义的字段名一一对应。\n     *\n     * @param name Redis 对象名\n     * @param values 与字段名顺序对应的值\n     * @return void\n     */",
    ),
    (
        "/**\n     * Adds Map object stored under the specified <code>name</code>.\n     * <p>\n     * Values are matched to the field names defined for this import object by position.\n     *\n     * @param name name of object\n     * @param values values ordered as the defined field names\n     * @return void\n     */",
        "/**\n     * 添加存储于 {@code name} 下的 Map 对象（List 形式）。\n     * <p>\n     * 值按位置与导入对象定义的字段名一一对应。\n     *\n     * @param name Redis 对象名\n     * @param values 与字段名顺序对应的值\n     * @return void\n     */",
    ),
    (
        "/**\n     * Writes all buffered Map objects.\n     *\n     * @return void\n     */",
        "/**\n     * 将缓冲中的全部 Map 对象写入 Redis。\n     *\n     * @return void\n     */",
    ),
    (
        "/**\n     * Returns the amount of Map objects written by this import object.\n     *\n     * @return amount of Map objects\n     */",
        "/**\n     * 返回本导入对象已成功写入 Redis 的 Map 数量。\n     *\n     * @return 已导入的 Map 数量\n     */",
    ),
]

_MAPS_REACTIVE = [
    (
        "/**\n * Reactive interface for mass operations with Map objects.\n *\n * @author Nikita Koksharov\n *\n * @param <K> field type\n * @param <V> value type\n */",
        "/**\n * Map 批量操作的 Reactor API。\n * <p>各方法返回 {@link Mono}；用于一次性或分批写入多个 Redis Hash。\n *\n * @author Nikita Koksharov\n * @param <K> 字段类型\n * @param <V> 值类型\n */",
    ),
    (
        "/**\n     * Stores Map objects mapped by name. Each object replaces\n     * the whole Map object stored under the same name.\n     *\n     * @param maps Map objects mapped by name\n     * @return void\n     */",
        "/**\n     * 按名称批量写入 Map 对象，每个对象替换同名 Redis Map 的全部内容。\n     *\n     * @param maps Map 对象映射（键名为 Redis 对象名）\n     * @return void\n     */",
    ),
    (
        "/**\n     * Stores Map objects mapped by name. Each object replaces\n     * the whole Map object stored under the same name.\n     * <p>\n     * Objects are written in portions defined by <code>batchSize</code>.\n     *\n     * @param maps Map objects mapped by name\n     * @param batchSize amount of Map objects written per portion\n     * @return void\n     */",
        "/**\n     * 按名称批量写入 Map 对象，每个对象替换同名 Redis Map 的全部内容。\n     * <p>\n     * 按 {@code batchSize} 分批写入，降低单次内存与网络压力。\n     *\n     * @param maps Map 对象映射（键名为 Redis 对象名）\n     * @param batchSize 每批写入的 Map 数量\n     * @return void\n     */",
    ),
    (
        "/**\n     * Returns import object for Map objects sharing the field names defined in <code>args</code>.\n     *\n     * @param args import arguments object\n     * @return import object\n     */",
        "/**\n     * 返回共享 {@code args} 中字段名的 Map 批量导入对象。\n     *\n     * @param args 导入参数\n     * @return 导入对象\n     */",
    ),
]

_MAPS_RX = [
    (
        "/**\n * Rx interface for mass operations with Map objects.\n *\n * @author Nikita Koksharov\n *\n * @param <K> field type\n * @param <V> value type\n */",
        "/**\n * Map 批量操作的 RxJava API。\n * <p>各方法返回 {@link Completable}；用于一次性或分批写入多个 Redis Hash。\n *\n * @author Nikita Koksharov\n * @param <K> 字段类型\n * @param <V> 值类型\n */",
    ),
    (
        "/**\n     * Stores Map objects mapped by name. Each object replaces\n     * the whole Map object stored under the same name.\n     *\n     * @param maps Map objects mapped by name\n     * @return void\n     */",
        "/**\n     * 按名称批量写入 Map 对象，每个对象替换同名 Redis Map 的全部内容。\n     *\n     * @param maps Map 对象映射（键名为 Redis 对象名）\n     * @return void\n     */",
    ),
    (
        "/**\n     * Stores Map objects mapped by name. Each object replaces\n     * the whole Map object stored under the same name.\n     * <p>\n     * Objects are written in portions defined by <code>batchSize</code>.\n     *\n     * @param maps Map objects mapped by name\n     * @param batchSize amount of Map objects written per portion\n     * @return void\n     */",
        "/**\n     * 按名称批量写入 Map 对象，每个对象替换同名 Redis Map 的全部内容。\n     * <p>\n     * 按 {@code batchSize} 分批写入，降低单次内存与网络压力。\n     *\n     * @param maps Map 对象映射（键名为 Redis 对象名）\n     * @param batchSize 每批写入的 Map 数量\n     * @return void\n     */",
    ),
    (
        "/**\n     * Returns import object for Map objects sharing the field names defined in <code>args</code>.\n     *\n     * @param args import arguments object\n     * @return import object\n     */",
        "/**\n     * 返回共享 {@code args} 中字段名的 Map 批量导入对象。\n     *\n     * @param args 导入参数\n     * @return 导入对象\n     */",
    ),
]

# --- RMultimap family ---

_MULTimap = [
    (
        "/**\n * Base Multimap interface. Allows to map multiple values per key.\n *\n * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
        "/**\n * Multimap 基础接口，允许一个键映射多个值。\n * <p>基于 Redis Hash 结构，键对应一组值集合；\n * 具体实现可为 ListMultimap 或 SetMultimap。\n *\n * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
    ),
    (
        "/**\n     * Returns <code>RCountDownLatch</code> instance associated with key\n     * \n     * @param key - map key\n     * @return countdownlatch\n     */",
        "/**\n     * 返回与 {@code key} 关联的 {@link RCountDownLatch} 实例。\n     * \n     * @param key 映射键\n     * @return 倒计时门闩\n     */",
    ),
    (
        "/**\n     * Returns <code>RPermitExpirableSemaphore</code> instance associated with key\n     * \n     * @param key - map key\n     * @return permitExpirableSemaphore\n     */",
        "/**\n     * 返回与 {@code key} 关联的 {@link RPermitExpirableSemaphore} 实例。\n     * \n     * @param key 映射键\n     * @return 可过期许可信号量\n     */",
    ),
    (
        "/**\n     * Returns <code>RSemaphore</code> instance associated with key\n     * \n     * @param key - map key\n     * @return semaphore\n     */",
        "/**\n     * 返回与 {@code key} 关联的 {@link RSemaphore} 实例。\n     * \n     * @param key 映射键\n     * @return 信号量\n     */",
    ),
    (
        "/**\n     * Returns <code>RLock</code> instance associated with key\n     * \n     * @param key - map key\n     * @return fairlock\n     */",
        "/**\n     * 返回与 {@code key} 关联的公平 {@link RLock} 实例。\n     * \n     * @param key 映射键\n     * @return 公平锁\n     */",
    ),
    (
        "/**\n     * Returns <code>RReadWriteLock</code> instance associated with key\n     * \n     * @param key - map key\n     * @return readWriteLock\n     */",
        "/**\n     * 返回与 {@code key} 关联的 {@link RReadWriteLock} 实例。\n     * \n     * @param key 映射键\n     * @return 读写锁\n     */",
    ),
    (
        "/**\n     * Returns <code>RLock</code> instance associated with key\n     * \n     * @param key - map key\n     * @return lock\n     */",
        "/**\n     * 返回与 {@code key} 关联的 {@link RLock} 实例。\n     * \n     * @param key 映射键\n     * @return 分布式锁\n     */",
    ),
    _MM_SIZE,
    (
        "/**\n     * Check is map empty\n     *\n     * @return <code>true</code> if empty\n     */",
        "/**\n     * 检查 multimap 是否为空。\n     *\n     * @return 为空时返回 {@code true}\n     */",
    ),
    _MM_CONTAINS_KEY,
    _MM_CONTAINS_VALUE,
    _MM_CONTAINS_ENTRY,
    _MM_PUT,
    _MM_REMOVE,
    _MM_PUTALL,
    (
        "/**\n     * Stores a collection of values with the same key, replacing any existing\n     * values for that key.\n     *\n     * <p>If {@code values} is empty, this is equivalent to\n     * {@link #removeAll(Object) removeAll(key)}.\n     *\n     * @param key - map key\n     * @param values - map values\n     * @return the collection of replaced values, or an empty collection if no\n     *     values were previously associated with the key. The collection\n     *     <i>may</i> be modifiable, but updating it will have no effect on the\n     *     multimap.\n     */",
        "/**\n     * 用 {@code values} 替换指定键的全部已有值。\n     *\n     * <p>若 {@code values} 为空，等价于 {@link #removeAll(Object) removeAll(key)}。\n     *\n     * @param key 映射键\n     * @param values 新值集合\n     * @return 被替换的旧值集合；修改返回集合不影响 multimap。\n     */",
    ),
    (
        "/**\n     * Stores a collection of values with the same key, replacing any existing\n     * values for that key. Is faster than {@link #replaceValues} by not returning\n     * the values.\n     *\n     * <p>If {@code values} is empty, this is equivalent to\n     * {@link #removeAll(Object) removeAll(key)}.\n     *\n     * @param key - map key\n     * @param values - map values\n     */",
        "/**\n     * 用 {@code values} 替换指定键的全部已有值（快速版，不返回旧值）。\n     * 比 {@link #replaceValues} 更快，但不返回被替换的值。\n     *\n     * <p>若 {@code values} 为空，等价于 {@link #removeAll(Object) removeAll(key)}。\n     *\n     * @param key 映射键\n     * @param values 新值集合\n     */",
    ),
    (
        "/**\n     * Removes all values associated with the key {@code key}.\n     *\n     * <p>Once this method returns, {@code key} will not be mapped to any values\n     * <p>Use {@link RMultimap#fastRemove} if values are not needed.</p>\n     * \n     * @param key - map key\n     * @return the values that were removed (possibly empty). The returned\n     *     collection <i>may</i> be modifiable, but updating it will have no\n     *     effect on the multimap.\n     */",
        "/**\n     * 移除与 {@code key} 关联的全部值。\n     *\n     * <p>方法返回后 {@code key} 不再映射任何值。\n     * <p>若不需要返回值，可使用 {@link RMultimap#fastRemove}。</p>\n     * \n     * @param key 映射键\n     * @return 被移除的值集合（可能为空）；修改返回集合不影响 multimap。\n     */",
    ),
    (
        "/**\n     * Removes all key-value pairs from the multimap, leaving it {@linkplain\n     * #isEmpty empty}.\n     */",
        "/**\n     * 清空 multimap 的全部键值对，使其 {@linkplain #isEmpty 为空}。\n     */",
    ),
    (
        "/**\n     * Returns a view collection of the values associated with {@code key} in this\n     * multimap, if any. Note that when {@code containsKey(key)} is false, this\n     * returns an empty collection, not {@code null}.\n     *\n     * <p>Changes to the returned collection will update the underlying multimap,\n     * and vice versa.\n     * \n     * @param key - map key\n     * @return collection of values\n     */",
        "/**\n     * 返回与 {@code key} 关联的值集合视图。\n     * 当 {@code containsKey(key)} 为 false 时返回空集合而非 {@code null}。\n     *\n     * <p>对返回集合的修改会反映到底层 multimap，反之亦然。\n     * \n     * @param key 映射键\n     * @return 值集合\n     */",
    ),
    (
        "/**\n     * Returns all elements at once. Result collection is <b>NOT</b> backed by map,\n     * so changes are not reflected in map.\n     *\n     * @param key - map key\n     * @return collection of values \n     */",
        "/**\n     * 一次性返回指定键的全部元素。\n     * 结果集合<b>不</b>与底层 map 绑定，修改结果不会影响 multimap。\n     *\n     * @param key 映射键\n     * @return 值集合\n     */",
    ),
    (
        "/**\n     * Returns a view collection of all <i>distinct</i> keys contained in this\n     * multimap. Note that the key set contains a key if and only if this multimap\n     * maps that key to at least one value.\n     *\n     * <p>Changes to the returned set will update the underlying multimap, and\n     * vice versa. However, <i>adding</i> to the returned set is not possible.\n     * \n     * @return set of keys\n     */",
        "/**\n     * 返回 multimap 中全部不重复键的集合视图。\n     * 仅当键至少映射一个值时才会出现在键集中。\n     *\n     * <p>对返回集合的修改会反映到底层 multimap，反之亦然；\n     * 但不支持向返回集合添加键。\n     * \n     * @return 键集合\n     */",
    ),
    (
        "/**\n     * Returns a view collection of all distinct keys contained in this multimap.\n     * Keys are loaded in batches; batch size is defined by the <code>count</code> parameter.\n     * Larger values reduce the number of HSCAN round-trips when iterating large multimaps.\n     *\n     * <p>Changes to the returned set will update the underlying multimap, and\n     * vice versa. However, <i>adding</i> to the returned set is not possible.\n     *\n     * @param count - size of the keys batch\n     * @return set of keys\n     */",
        "/**\n     * 返回 multimap 中全部不重复键的集合视图（分批加载）。\n     * {@code count} 定义每批键数量；较大值可减少大 multimap 迭代时的 HSCAN 往返次数。\n     *\n     * <p>对返回集合的修改会反映到底层 multimap，反之亦然；\n     * 但不支持向返回集合添加键。\n     *\n     * @param count 每批键数量\n     * @return 键集合\n     */",
    ),
    (
        "/**\n     *  Returns the count of distinct keys in this multimap.\n     *  \n     *  @return keys amount\n     */",
        "/**\n     * 返回 multimap 中不重复键的数量。\n     *  \n     * @return 键数量\n     */",
    ),
    (
        "/**\n     * Returns a view collection containing the <i>value</i> from each key-value\n     * pair contained in this multimap, without collapsing duplicates (so {@code\n     * values().size() == size()}).\n     *\n     * <p>Changes to the returned collection will update the underlying multimap,\n     * and vice versa. However, <i>adding</i> to the returned collection is not\n     * possible.\n     * \n     * @return collection of values\n     */",
        "/**\n     * 返回 multimap 中每个键值对的值集合视图（不合并重复）。\n     * 因此 {@code values().size() == size()}。\n     *\n     * <p>对返回集合的修改会反映到底层 multimap，反之亦然；\n     * 但不支持向返回集合添加值。\n     * \n     * @return 值集合\n     */",
    ),
    (
        "/**\n     * Returns a view collection containing the <i>value</i> from each key-value\n     * pair contained in this multimap, without collapsing duplicates.\n     * Values are loaded in batches; batch size is defined by the <code>count</code> parameter.\n     * Larger values reduce the number of HSCAN round-trips on the key dimension when\n     * iterating large multimaps, and (for {@code RSetMultimap}) the number of SSCAN\n     * round-trips on the value dimension as well.\n     *\n     * @param count size of the iteration batch\n     * @return collection of values\n     */",
        "/**\n     * 返回 multimap 中每个键值对的值集合视图（分批加载，不合并重复）。\n     * {@code count} 定义每批数量；较大值可减少 HSCAN/SSCAN 往返次数。\n     *\n     * @param count 每批迭代数量\n     * @return 值集合\n     */",
    ),
    (
        "/**\n     * Returns a view collection of all key-value pairs contained in this\n     * multimap, as {@link Map.Entry} instances.\n     *\n     * <p>Changes to the returned collection or the entries it contains will\n     * update the underlying multimap, and vice versa. However, <i>adding</i> to\n     * the returned collection is not possible.\n     * \n     * @return collection of entries\n     */",
        "/**\n     * 返回 multimap 中全部键值对的集合视图（{@link Map.Entry} 形式）。\n     *\n     * <p>对返回集合或其条目的修改会反映到底层 multimap，反之亦然；\n     * 但不支持向返回集合添加条目。\n     * \n     * @return 条目集合\n     */",
    ),
    (
        "/**\n     * Returns a view collection of all key-value pairs contained in this multimap,\n     * as {@link Map.Entry} instances.\n     * Entries are loaded in batches; batch size is defined by the <code>count</code> parameter.\n     * Larger values reduce the number of HSCAN round-trips on the key dimension when\n     * iterating large multimaps, and (for {@code RSetMultimap}) the number of SSCAN\n     * round-trips on the value dimension as well.\n     *\n     * @param count size of the iteration batch\n     * @return collection of entries\n     */",
        "/**\n     * 返回 multimap 中全部键值对的集合视图（分批加载，{@link Map.Entry} 形式）。\n     * {@code count} 定义每批数量；较大值可减少 HSCAN/SSCAN 往返次数。\n     *\n     * @param count 每批迭代数量\n     * @return 条目集合\n     */",
    ),
    _MM_FAST_REMOVE,
    _MM_FAST_REMOVE_VAL,
    _MM_READ_KEYS,
]

_MULTimap_ASYNC = [
    (
        "/**\n * Base asynchronous MultiMap interface. A collection that maps multiple values per one key.\n *\n * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
        "/**\n * Multimap 基础异步 API，一个键可映射多个值。\n * <p>各方法返回 {@link RFuture}；基于 Redis Hash 结构。\n *\n * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
    ),
    _MM_SIZE,
    _MM_CONTAINS_KEY,
    _MM_CONTAINS_VALUE,
    _MM_CONTAINS_ENTRY,
    _MM_PUT,
    _MM_REMOVE,
    _MM_PUTALL_ASYNC,
    (
        "/**\n     * Stores a collection of values with the same key, replacing any existing\n     * values for that key.\n     *\n     * <p>If {@code values} is empty, this is equivalent to\n     * {@link #removeAllAsync(Object)}.\n     *\n     * @param key - map key\n     * @param values - map values\n     * @return the collection of replaced values, or an empty collection if no\n     *     values were previously associated with the key. The collection\n     *     <i>may</i> be modifiable, but updating it will have no effect on the\n     *     multimap.\n     */",
        "/**\n     * 用 {@code values} 替换指定键的全部已有值。\n     *\n     * <p>若 {@code values} 为空，等价于 {@link #removeAllAsync(Object)}。\n     *\n     * @param key 映射键\n     * @param values 新值集合\n     * @return 被替换的旧值集合；修改返回集合不影响 multimap。\n     */",
    ),
    (
        "/**\n     * Stores a collection of values with the same key, replacing any existing\n     * values for that key. Is faster than {@link #replaceValuesAsync(Object, Iterable)}\n     * by not returning the values.\n     *\n     * <p>If {@code values} is empty, this is equivalent to\n     * {@link #removeAllAsync(Object)}.\n     *\n     * @param key - map key\n     * @param values - map values\n     */",
        "/**\n     * 用 {@code values} 替换指定键的全部已有值（快速版，不返回旧值）。\n     * 比 {@link #replaceValuesAsync(Object, Iterable)} 更快。\n     *\n     * <p>若 {@code values} 为空，等价于 {@link #removeAllAsync(Object)}。\n     *\n     * @param key 映射键\n     * @param values 新值集合\n     */",
    ),
    (
        "/**\n     * Removes all values associated with the key {@code key}.\n     *\n     * <p>Once this method returns, {@code key} will not be mapped to any values.\n     *\n     * @param key - map key\n     * @return the values that were removed (possibly empty). The returned\n     *     collection <i>may</i> be modifiable, but updating it will have no\n     *     effect on the multimap.\n     */",
        "/**\n     * 移除与 {@code key} 关联的全部值。\n     *\n     * <p>方法返回后 {@code key} 不再映射任何值。\n     *\n     * @param key 映射键\n     * @return 被移除的值集合（可能为空）；修改返回集合不影响 multimap。\n     */",
    ),
    (
        "    RFuture<Collection<V>> getAllAsync(K key);",
        "    /** 异步一次性返回指定键的全部元素（结果不与底层 map 绑定）。 */\n    RFuture<Collection<V>> getAllAsync(K key);",
    ),
    _MM_KEYSIZE,
    _MM_FAST_REMOVE,
    _MM_FAST_REMOVE_VAL,
    _MM_READ_KEYS,
]

_MULTimap_CACHE = [
    (
        "/**\n * Base Multimap interface. Allows to map multiple values per key and define expiration per key.\n *\n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
        "/**\n * 带 per-key TTL 的 Multimap 基础接口，一个键可映射多个值。\n * <p>继承 {@link RMultimap} 全部能力，并支持为单个键设置过期时间。\n *\n * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
    ),
    _MM_EXPIRE_KEY,
]

_MULTimap_CACHE_ASYNC = [
    (
        "/**\n * Base asynchronous Multimap interface. Allows to map multiple values per key and define expiration per key.\n *\n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
        "/**\n * 带 per-key TTL 的 Multimap 异步 API。\n * <p>继承 {@link RMultimapAsync} 全部能力，并支持为单个键设置过期时间。\n *\n * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
    ),
    _MM_EXPIRE_KEY_ASYNC,
]

_MULTimap_CACHE_REACTIVE = [
    (
        "/**\n * Reactive interface of {@link RMultimapCache} object.\n *\n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
        "/**\n * {@link RMultimapCache} 的 Reactor API。\n * <p>各方法返回 {@link Mono}；支持为单个键设置过期时间。\n *\n * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
    ),
    _MM_EXPIRE_KEY_REACTIVE,
]

_MULTimap_CACHE_RX = [
    (
        "/**\n * Rx-ified version of {@link RMultimapCache}.\n *\n * @author Marnix Kammer\n *\n * @param <K> key type\n * @param <V> value type\n */",
        "/**\n * {@link RMultimapCache} 的 RxJava API。\n * <p>各方法返回 {@link Single}；支持为单个键设置过期时间。\n *\n * @author Marnix Kammer\n * @param <K> 键类型\n * @param <V> 值类型\n */",
    ),
    _MM_EXPIRE_KEY_REACTIVE,
]

_MULTimap_REACTIVE = [
    (
        "/**\n * Base Reactive interface for Multimap object\n * \n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
        "/**\n * Multimap 基础 Reactor API，一个键可映射多个值。\n * <p>各方法返回 {@link Mono}；基于 Redis Hash 结构。\n *\n * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
    ),
    _MM_SIZE,
    _MM_CONTAINS_KEY,
    _MM_CONTAINS_VALUE,
    _MM_CONTAINS_ENTRY,
    _MM_PUT,
    _MM_REMOVE,
    _MM_PUTALL_ASYNC,
    _MM_KEYSIZE,
    _MM_FAST_REPLACE,
    _MM_FAST_REMOVE,
    _MM_FAST_REMOVE_VAL,
    _MM_READ_KEYS,
]

_MULTimap_RX = [
    (
        "/**\n * Base RxJava2 interface for Multimap object\n * \n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
        "/**\n * Multimap 基础 RxJava API，一个键可映射多个值。\n * <p>各方法返回 {@link Single}；基于 Redis Hash 结构。\n *\n * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
    ),
    _MM_SIZE,
    _MM_CONTAINS_KEY,
    _MM_CONTAINS_VALUE,
    _MM_CONTAINS_ENTRY,
    _MM_PUT,
    _MM_REMOVE,
    _MM_PUTALL_ASYNC,
    _MM_KEYSIZE,
    _MM_FAST_REPLACE,
    _MM_FAST_REMOVE,
    _MM_FAST_REMOVE_VAL,
    _MM_READ_KEYS,
]


def _dup(full_path: str, short: str, reps: list) -> None:
    W47A_REPLACEMENTS[full_path] = reps
    W47A_REPLACEMENTS[short] = reps


W47A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

_dup(f"{_A}RMapsAsync.java", "RMapsAsync.java", _MAPS_ASYNC)
_dup(f"{_A}RMapsImport.java", "RMapsImport.java", _MAPS_IMPORT)
_dup(f"{_A}RMapsImportAsync.java", "RMapsImportAsync.java", _MAPS_IMPORT_ASYNC)
_dup(f"{_A}RMapsImportReactive.java", "RMapsImportReactive.java", _MAPS_IMPORT_REACTIVE)
_dup(f"{_A}RMapsImportRx.java", "RMapsImportRx.java", _MAPS_IMPORT_RX)
_dup(f"{_A}RMapsReactive.java", "RMapsReactive.java", _MAPS_REACTIVE)
_dup(f"{_A}RMapsRx.java", "RMapsRx.java", _MAPS_RX)
_dup(f"{_A}RMultimap.java", "RMultimap.java", _MULTimap)
_dup(f"{_A}RMultimapAsync.java", "RMultimapAsync.java", _MULTimap_ASYNC)
_dup(f"{_A}RMultimapCache.java", "RMultimapCache.java", _MULTimap_CACHE)
_dup(f"{_A}RMultimapCacheAsync.java", "RMultimapCacheAsync.java", _MULTimap_CACHE_ASYNC)
_dup(f"{_A}RMultimapCacheReactive.java", "RMultimapCacheReactive.java", _MULTimap_CACHE_REACTIVE)
_dup(f"{_A}RMultimapCacheRx.java", "RMultimapCacheRx.java", _MULTimap_CACHE_RX)
_dup(f"{_A}RMultimapReactive.java", "RMultimapReactive.java", _MULTimap_REACTIVE)
_dup(f"{_A}RMultimapRx.java", "RMultimapRx.java", _MULTimap_RX)
