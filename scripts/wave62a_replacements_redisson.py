"""Chinese annotation replacements for Redisson 4.7.0 wave-62a listener [0:15]."""
from __future__ import annotations

_L = "redisson/src/main/java/org/redisson/api/listener"

W62A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    f"{_L}/ListRemoveListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>lrem</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain El letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的列表<b>元素删除</b>（lrem）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code l} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on event of removing element from list\n     *\n     * @param name - name of object\n     */",
            "    /**\n     * 当元素从列表中被移除时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    "ListRemoveListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>lrem</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain El letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的列表<b>元素删除</b>（lrem）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code l} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on event of removing element from list\n     *\n     * @param name - name of object\n     */",
            "    /**\n     * 当元素从列表中被移除时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/ListSetListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>lset</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain El letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的列表<b>索引赋值</b>（lset）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code l} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on event of setting element to list\n     *\n     * @param name - name of object\n     */",
            "    /**\n     * 当列表指定索引处的元素被更新时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    "ListSetListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>lset</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain El letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的列表<b>索引赋值</b>（lset）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code l} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on event of setting element to list\n     *\n     * @param name - name of object\n     */",
            "    /**\n     * 当列表指定索引处的元素被更新时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/ListTrimListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>ltrim</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain El letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的列表<b>裁剪</b>（ltrim）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code l} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on list trimming event\n     *\n     * @param name - name of object\n     */",
            "    /**\n     * 当列表被 ltrim 命令裁剪长度时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    "ListTrimListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>ltrim</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain El letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的列表<b>裁剪</b>（ltrim）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code l} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on list trimming event\n     *\n     * @param name - name of object\n     */",
            "    /**\n     * 当列表被 ltrim 命令裁剪长度时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/LocalCacheInvalidateListener.java": [
        (
            "/**\n * Redisson Object Event listener for local cache invalidation event published by Valkey or Redis.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的<b>本地缓存失效</b>事件。\n * <p>\n * 当远程 Map 条目变更导致本地缓存条目需失效时回调，\n * 适用于带本地缓存的 {@link org.redisson.api.RLocalCachedMap} 等结构。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on event of map entry invalidation\n     *\n     * @param key key to remove\n     * @param value value to remove\n     */",
            "    /**\n     * 当 Map 条目从本地缓存中失效时触发。\n     *\n     * @param key 待移除的键\n     * @param value 待移除的值\n     */",
        ),
    ],
    "LocalCacheInvalidateListener.java": [
        (
            "/**\n * Redisson Object Event listener for local cache invalidation event published by Valkey or Redis.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的<b>本地缓存失效</b>事件。\n * <p>\n * 当远程 Map 条目变更导致本地缓存条目需失效时回调，\n * 适用于带本地缓存的 {@link org.redisson.api.RLocalCachedMap} 等结构。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on event of map entry invalidation\n     *\n     * @param key key to remove\n     * @param value value to remove\n     */",
            "    /**\n     * 当 Map 条目从本地缓存中失效时触发。\n     *\n     * @param key 待移除的键\n     * @param value 待移除的值\n     */",
        ),
    ],
    f"{_L}/LocalCacheUpdateListener.java": [
        (
            "/**\n * Redisson Object Event listener for local cache update event published by Valkey or Redis.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的<b>本地缓存更新</b>事件。\n * <p>\n * 当远程 Map 条目变更需同步到本地缓存时回调，\n * 适用于带本地缓存的 {@link org.redisson.api.RLocalCachedMap} 等结构。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on event of map entry udpate\n     *\n     * @param key key to update\n     * @param value new value\n     */",
            "    /**\n     * 当 Map 条目在本地缓存中被更新时触发。\n     *\n     * @param key 待更新的键\n     * @param value 新的值\n     */",
        ),
    ],
    "LocalCacheUpdateListener.java": [
        (
            "/**\n * Redisson Object Event listener for local cache update event published by Valkey or Redis.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的<b>本地缓存更新</b>事件。\n * <p>\n * 当远程 Map 条目变更需同步到本地缓存时回调，\n * 适用于带本地缓存的 {@link org.redisson.api.RLocalCachedMap} 等结构。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on event of map entry udpate\n     *\n     * @param key key to update\n     * @param value new value\n     */",
            "    /**\n     * 当 Map 条目在本地缓存中被更新时触发。\n     *\n     * @param key 待更新的键\n     * @param value 新的值\n     */",
        ),
    ],
    f"{_L}/MapClearExpireListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>hpersist</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eh or Th letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的 Hash<b>清除过期时间</b>（hpersist）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when entry's expiration is cleared\n     *\n     * @param name object name\n     * @param fieldName map entry field name. Can be null for keyevent notification.\n     */",
            "    /**\n     * 当 Hash 条目的过期时间被清除时触发。\n     *\n     * @param name 对象名称（键名）\n     * @param fieldName Map 字段名；键事件通知时可为 null\n     */",
        ),
    ],
    "MapClearExpireListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>hpersist</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eh or Th letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的 Hash<b>清除过期时间</b>（hpersist）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when entry's expiration is cleared\n     *\n     * @param name object name\n     * @param fieldName map entry field name. Can be null for keyevent notification.\n     */",
            "    /**\n     * 当 Hash 条目的过期时间被清除时触发。\n     *\n     * @param name 对象名称（键名）\n     * @param fieldName Map 字段名；键事件通知时可为 null\n     */",
        ),
    ],
    f"{_L}/MapExpiredListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>hexpired</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eh or Th letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的 Hash<b>字段过期</b>（hexpired）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when entry expired\n     *\n     * @param name object name\n     * @param fieldName map entry field name. Can be null for keyevent notification.\n     */",
            "    /**\n     * 当 Hash 字段过期被删除时触发。\n     *\n     * @param name 对象名称（键名）\n     * @param fieldName Map 字段名；键事件通知时可为 null\n     */",
        ),
    ],
    "MapExpiredListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>hexpired</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eh or Th letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的 Hash<b>字段过期</b>（hexpired）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when entry expired\n     *\n     * @param name object name\n     * @param fieldName map entry field name. Can be null for keyevent notification.\n     */",
            "    /**\n     * 当 Hash 字段过期被删除时触发。\n     *\n     * @param name 对象名称（键名）\n     * @param fieldName Map 字段名；键事件通知时可为 null\n     */",
        ),
    ],
    f"{_L}/MapIncrListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>hincrbyfloat</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eh or Th letters\n *\n * @author nhancdt2602\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的 Hash<b>浮点递增</b>（hincrbyfloat）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。\n *\n * @author nhancdt2602\n */",
        ),
        (
            "    /**\n     * Invoked when entry incremented\n     *\n     * @param name object name\n     * @param fieldName map entry field name. Can be null for keyevent notification.\n     */",
            "    /**\n     * 当 Hash 字段数值被递增时触发。\n     *\n     * @param name 对象名称（键名）\n     * @param fieldName Map 字段名；键事件通知时可为 null\n     */",
        ),
    ],
    "MapIncrListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>hincrbyfloat</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eh or Th letters\n *\n * @author nhancdt2602\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的 Hash<b>浮点递增</b>（hincrbyfloat）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。\n *\n * @author nhancdt2602\n */",
        ),
        (
            "    /**\n     * Invoked when entry incremented\n     *\n     * @param name object name\n     * @param fieldName map entry field name. Can be null for keyevent notification.\n     */",
            "    /**\n     * 当 Hash 字段数值被递增时触发。\n     *\n     * @param name 对象名称（键名）\n     * @param fieldName Map 字段名；键事件通知时可为 null\n     */",
        ),
    ],
    f"{_L}/MapPutListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>hset</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eh or Th letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的 Hash<b>写入</b>（hset）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when entry added to RMap object\n     *\n     * @param name object name\n     * @param fieldName map entry field name. Can be null for keyevent notification.\n     */",
            "    /**\n     * 当条目被写入 {@link org.redisson.api.RMap} 时触发。\n     *\n     * @param name 对象名称（键名）\n     * @param fieldName Map 字段名；键事件通知时可为 null\n     */",
        ),
    ],
    "MapPutListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>hset</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eh or Th letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的 Hash<b>写入</b>（hset）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when entry added to RMap object\n     *\n     * @param name object name\n     * @param fieldName map entry field name. Can be null for keyevent notification.\n     */",
            "    /**\n     * 当条目被写入 {@link org.redisson.api.RMap} 时触发。\n     *\n     * @param name 对象名称（键名）\n     * @param fieldName Map 字段名；键事件通知时可为 null\n     */",
        ),
    ],
    f"{_L}/MapRemoveListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>hdel</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eh or Th letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的 Hash<b>字段删除</b>（hdel）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when entry removed from RMap object\n     *\n     * @param name object name\n     * @param fieldName map entry field name. Can be null for keyevent notification.\n     */",
            "    /**\n     * 当条目从 {@link org.redisson.api.RMap} 中被删除时触发。\n     *\n     * @param name 对象名称（键名）\n     * @param fieldName Map 字段名；键事件通知时可为 null\n     */",
        ),
    ],
    "MapRemoveListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>hdel</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eh or Th letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的 Hash<b>字段删除</b>（hdel）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code h} 或 {@code T} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when entry removed from RMap object\n     *\n     * @param name object name\n     * @param fieldName map entry field name. Can be null for keyevent notification.\n     */",
            "    /**\n     * 当条目从 {@link org.redisson.api.RMap} 中被删除时触发。\n     *\n     * @param name 对象名称（键名）\n     * @param fieldName Map 字段名；键事件通知时可为 null\n     */",
        ),
    ],
    f"{_L}/MessageListener.java": [
        (
            "/**\n * Listener for Valkey or Redis messages published via RTopic Redisson object\n *\n * @author Nikita Koksharov\n *\n * @param <M> message\n *\n * @see org.redisson.api.RTopic\n */",
            "/**\n * 监听通过 {@link org.redisson.api.RTopic} 发布的 Valkey 或 Redis 消息。\n * <p>\n * 订阅指定频道后，每条 Pub/Sub 消息都会回调 {@link #onMessage(CharSequence, Object)}。\n *\n * @author Nikita Koksharov\n *\n * @param <M> 消息体类型\n *\n * @see org.redisson.api.RTopic\n */",
        ),
        (
            "    /**\n     * Invokes on every message in topic\n     *\n     * @param channel of topic\n     * @param msg topic message\n     */",
            "    /**\n     * 收到主题消息时调用。\n     *\n     * @param channel 主题频道名称\n     * @param msg 主题消息内容\n     */",
        ),
    ],
    "MessageListener.java": [
        (
            "/**\n * Listener for Valkey or Redis messages published via RTopic Redisson object\n *\n * @author Nikita Koksharov\n *\n * @param <M> message\n *\n * @see org.redisson.api.RTopic\n */",
            "/**\n * 监听通过 {@link org.redisson.api.RTopic} 发布的 Valkey 或 Redis 消息。\n * <p>\n * 订阅指定频道后，每条 Pub/Sub 消息都会回调 {@link #onMessage(CharSequence, Object)}。\n *\n * @author Nikita Koksharov\n *\n * @param <M> 消息体类型\n *\n * @see org.redisson.api.RTopic\n */",
        ),
        (
            "    /**\n     * Invokes on every message in topic\n     *\n     * @param channel of topic\n     * @param msg topic message\n     */",
            "    /**\n     * 收到主题消息时调用。\n     *\n     * @param channel 主题频道名称\n     * @param msg 主题消息内容\n     */",
        ),
    ],
    f"{_L}/NewObjectListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>new object</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain En letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的<b>新键创建</b>键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code n} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on new object event\n     *\n     * @param name - name of object\n     */",
            "    /**\n     * 当 Redis 中创建新键时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    "NewObjectListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>new object</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain En letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的<b>新键创建</b>键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code n} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked on new object event\n     *\n     * @param name - name of object\n     */",
            "    /**\n     * 当 Redis 中创建新键时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/PatternMessageListener.java": [
        (
            "/**\n * Listener for Valkey or Redis messages published via RTopic Redisson object\n *\n * @author Nikita Koksharov\n *\n * @param <M> message\n *\n * @see org.redisson.api.RTopic\n */",
            "/**\n * 监听通过 {@link org.redisson.api.RTopic} 以<b>模式订阅</b>接收的 Valkey 或 Redis 消息。\n * <p>\n * 与 {@link MessageListener} 不同，回调中包含匹配的模式与具体频道名称。\n *\n * @author Nikita Koksharov\n *\n * @param <M> 消息体类型\n *\n * @see org.redisson.api.RTopic\n */",
        ),
        (
            "    /**\n     * Invokes on every message in topic\n     *\n     * @param pattern of channel name\n     * @param channel of topic\n     * @param msg topic message\n     */",
            "    /**\n     * 收到模式匹配的主题消息时调用。\n     *\n     * @param pattern 订阅的模式表达式\n     * @param channel 实际消息来源频道\n     * @param msg 主题消息内容\n     */",
        ),
    ],
    "PatternMessageListener.java": [
        (
            "/**\n * Listener for Valkey or Redis messages published via RTopic Redisson object\n *\n * @author Nikita Koksharov\n *\n * @param <M> message\n *\n * @see org.redisson.api.RTopic\n */",
            "/**\n * 监听通过 {@link org.redisson.api.RTopic} 以<b>模式订阅</b>接收的 Valkey 或 Redis 消息。\n * <p>\n * 与 {@link MessageListener} 不同，回调中包含匹配的模式与具体频道名称。\n *\n * @author Nikita Koksharov\n *\n * @param <M> 消息体类型\n *\n * @see org.redisson.api.RTopic\n */",
        ),
        (
            "    /**\n     * Invokes on every message in topic\n     *\n     * @param pattern of channel name\n     * @param channel of topic\n     * @param msg topic message\n     */",
            "    /**\n     * 收到模式匹配的主题消息时调用。\n     *\n     * @param pattern 订阅的模式表达式\n     * @param channel 实际消息来源频道\n     * @param msg 主题消息内容\n     */",
        ),
    ],
    f"{_L}/PatternStatusListener.java": [
        (
            "/**\n * Listener for Valkey or Redis PubSub channel status changes\n *\n * @author Nikita Koksharov\n *\n * @see org.redisson.api.RTopic\n */",
            "/**\n * 监听 Valkey 或 Redis Pub/Sub <b>模式订阅</b>状态变更。\n * <p>\n * 在客户端成功订阅或取消模式（pattern）时回调，\n * 可配合 {@link org.redisson.api.RTopic} 跟踪连接状态。\n *\n * @author Nikita Koksharov\n *\n * @see org.redisson.api.RTopic\n */",
        ),
        (
            "    void onPSubscribe(String pattern);",
            "    /**\n     * 模式订阅成功时调用。\n     *\n     * @param pattern 已订阅的模式表达式\n     */\n    void onPSubscribe(String pattern);",
        ),
        (
            "    void onPUnsubscribe(String pattern);",
            "    /**\n     * 取消模式订阅时调用。\n     *\n     * @param pattern 已取消订阅的模式表达式\n     */\n    void onPUnsubscribe(String pattern);",
        ),
    ],
    "PatternStatusListener.java": [
        (
            "/**\n * Listener for Valkey or Redis PubSub channel status changes\n *\n * @author Nikita Koksharov\n *\n * @see org.redisson.api.RTopic\n */",
            "/**\n * 监听 Valkey 或 Redis Pub/Sub <b>模式订阅</b>状态变更。\n * <p>\n * 在客户端成功订阅或取消模式（pattern）时回调，\n * 可配合 {@link org.redisson.api.RTopic} 跟踪连接状态。\n *\n * @author Nikita Koksharov\n *\n * @see org.redisson.api.RTopic\n */",
        ),
        (
            "    void onPSubscribe(String pattern);",
            "    /**\n     * 模式订阅成功时调用。\n     *\n     * @param pattern 已订阅的模式表达式\n     */\n    void onPSubscribe(String pattern);",
        ),
        (
            "    void onPUnsubscribe(String pattern);",
            "    /**\n     * 取消模式订阅时调用。\n     *\n     * @param pattern 已取消订阅的模式表达式\n     */\n    void onPUnsubscribe(String pattern);",
        ),
    ],
    f"{_L}/ScoredSortedSetAddListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>zadd</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Ez letters\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的有序集合<b>成员添加</b>（zadd）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code z} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked when entry added to RScoredSortedSet object\n     * \n     * @param name - name of object\n     */",
            "    /**\n     * 当成员被添加到 {@link org.redisson.api.RScoredSortedSet} 时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    "ScoredSortedSetAddListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>zadd</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Ez letters\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的有序集合<b>成员添加</b>（zadd）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code z} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    /**\n     * Invoked when entry added to RScoredSortedSet object\n     * \n     * @param name - name of object\n     */",
            "    /**\n     * 当成员被添加到 {@link org.redisson.api.RScoredSortedSet} 时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
}
