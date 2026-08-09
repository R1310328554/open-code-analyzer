"""Chinese annotation replacements for Redisson 4.7.0 wave-62b listener [15:30]."""
from __future__ import annotations

_L = "redisson/src/main/java/org/redisson/api/listener"

W62B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    f"{_L}/ScoredSortedSetDiffStoreListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>zdiffstore</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Ez letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的有序集合<b>差集存储</b>（zdiffstore）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code z} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked when destination RScoredSortedSet object is created or updated\n     * as a result of a ZDIFFSTORE operation.\n     *\n     * @param name - name of object\n     */",
            "/**\n     * 当 ZDIFFSTORE 操作创建或更新目标 {@link org.redisson.api.RScoredSortedSet} 对象时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/ScoredSortedSetIncrListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>zincr</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Ez letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的有序集合<b>分数递增</b>（zincr）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code z} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked when entry score incremented in RScoredSortedSet object\n     *\n     * @param name - name of object\n     */",
            "/**\n     * 当 {@link org.redisson.api.RScoredSortedSet} 中成员分数被递增时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/ScoredSortedSetInterStoreListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>zinterstore</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Ez letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的有序集合<b>交集存储</b>（zinterstore）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code z} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked when destination RScoredSortedSet object is created or updated\n     * as a result of a ZINTERSTORE operation.\n     *\n     * @param name - name of object\n     */",
            "/**\n     * 当 ZINTERSTORE 操作创建或更新目标 {@link org.redisson.api.RScoredSortedSet} 对象时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/ScoredSortedSetRemoveListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>zrem</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Ez letters\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的有序集合<b>成员移除</b>（zrem）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code z} 字母。\n * \n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked when entry removed from RScoredSortedSet object\n     * \n     * @param name - name of object\n     */",
            "/**\n     * 当成员从 {@link org.redisson.api.RScoredSortedSet} 中移除时触发。\n     * \n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/ScoredSortedSetUnionStoreListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>zunionstore</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Ez letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的有序集合<b>并集存储</b>（zunionstore）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code z} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked when destination RScoredSortedSet object is created or updated\n     * as a result of a ZUNIONSTORE operation.\n     *\n     * @param name - name of object\n     */",
            "/**\n     * 当 ZUNIONSTORE 操作创建或更新目标 {@link org.redisson.api.RScoredSortedSet} 对象时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/SetAddListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>sadd</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Es letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的集合<b>添加成员</b>（sadd）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code s} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "/**\n     * Invoked when value added to RSet object\n     *\n     * @param name object name\n     */",
            "/**\n     * 当成员被添加到 {@link org.redisson.api.RSet} 对象时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/SetDiffStoreListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>sdiffstore</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Es letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的集合<b>差集存储</b>（sdiffstore）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code s} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "/**\n     * Invoked when destination RSet object is created or updated\n     * as a result of a SDIFFSTORE operation.\n     *\n     * @param name object name\n     */",
            "/**\n     * 当 SDIFFSTORE 操作创建或更新目标 {@link org.redisson.api.RSet} 对象时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/SetExpiredListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>expired</b> event of {@link org.redisson.api.RSetCache} value.\n * <p>\n * Triggered when a value stored with a time to live is removed by the eviction process.\n *\n * @author Nikita Koksharov\n *\n * @param <V> value type\n */",
            "/**\n * 监听 {@link org.redisson.api.RSetCache} 中带 TTL 的集合成员<b>过期</b>（expired）键空间事件。\n * <p>\n * 当设置了生存时间的成员被 Redis 淘汰进程移除时触发。\n *\n * @author Nikita Koksharov\n *\n * @param <V> 集合元素类型\n */",
        ),
        (
            "/**\n     * Invoked when value stored in RSetCache object expires.\n     *\n     * @param value expired value\n     */",
            "/**\n     * 当 {@link org.redisson.api.RSetCache} 中的成员因 TTL 到期而被移除时触发。\n     *\n     * @param value 已过期的成员值\n     */",
        ),
    ],
    f"{_L}/SetInterStoreListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>sinterstore</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Es letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的集合<b>交集存储</b>（sinterstore）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code s} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "/**\n     * Invoked when destination RSet object is created or updated\n     * as a result of a SINTERSTORE operation.\n     *\n     * @param name object name\n     */",
            "/**\n     * 当 SINTERSTORE 操作创建或更新目标 {@link org.redisson.api.RSet} 对象时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/SetObjectListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>set</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain E$ letters\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的集合<b>整体赋值</b>（set）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code $} 字母。\n * \n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked on set object event\n     * \n     * @param name - name of object\n     */",
            "/**\n     * 当集合对象被整体赋值或覆盖时触发。\n     * \n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/SetRemoveListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>srem</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Es letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的集合<b>移除成员</b>（srem）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code s} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "/**\n     * Invoked when value removed from RSet object\n     *\n     * @param name object name\n     */",
            "/**\n     * 当成员从 {@link org.redisson.api.RSet} 对象中移除时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/SetRemoveRandomListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>spop</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Es letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的集合<b>随机弹出</b>（spop）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code s} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "/**\n     * Invoked when value randomly removed from RSet object\n     *\n     * @param name object name\n     */",
            "/**\n     * 当成员被随机从 {@link org.redisson.api.RSet} 对象中弹出时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/SetUnionStoreListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>sunionstore</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Es letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的集合<b>并集存储</b>（sunionstore）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code s} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "/**\n     * Invoked when destination RSet object is created or updated\n     * as a result of a SUNIONSTORE operation.\n     *\n     * @param name object name\n     */",
            "/**\n     * 当 SUNIONSTORE 操作创建或更新目标 {@link org.redisson.api.RSet} 对象时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/StatusListener.java": [
        (
            "/**\n * Listener for Valkey or Redis PubSub channel status changes\n *\n * @author Nikita Koksharov\n *\n * @see org.redisson.api.RTopic\n */",
            "/**\n * 监听 Valkey 或 Redis Pub/Sub 频道订阅状态变更。\n * <p>\n * 在订阅成功、取消订阅以及重连或故障转移过程中触发回调。\n *\n * @author Nikita Koksharov\n *\n * @see org.redisson.api.RTopic\n */",
        ),
        (
            "/**\n     * Executes then Redisson successfully subscribed to channel.\n     * Invoked during re-connection or failover process\n     * \n     * @param channel to subscribe\n     */",
            "/**\n     * Redisson 成功订阅频道时调用。\n     * <p>\n     * 在重连或故障转移过程中也会触发。\n     * \n     * @param channel 已订阅的频道名称\n     */",
        ),
        (
            "/**\n     * Executes then Redisson successfully unsubscribed from channel.\n     * \n     * @param channel to unsubscribe\n     */",
            "/**\n     * Redisson 成功取消频道订阅时调用。\n     * \n     * @param channel 已取消订阅的频道名称\n     */",
        ),
    ],
    f"{_L}/StreamAddListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>xadd</b> event\n * published by Valkey or Redis when an element added into Stream.\n * <p>\n * Redis notify-keyspace-events setting should contain Et letters\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的流<b>追加条目</b>（xadd）键空间事件。\n * <p>\n * 当新元素被写入 Stream 时触发；需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code t} 字母。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "/**\n     * Invoked when a new entry is added to RStream object\n     *\n     * @param name object name\n     */",
            "/**\n     * 当新条目被添加到 {@link org.redisson.api.RStream} 对象时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
}
