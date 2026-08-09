"""Chinese annotation replacements for Redisson 4.7.0 wave-61b api [15:30]."""
from __future__ import annotations

_K = "redisson/src/main/java/org/redisson/api/keys"
_L = "redisson/src/main/java/org/redisson/api/listener"

W61B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    f"{_K}/DatabaseMigrateArgs.java": [
        (
            "/**\n * DatabaseMigrateArgs\n * @author lyrric\n */",
            "/**\n * 键迁移参数链中的「目标数据库」配置步骤。\n * <p>\n * 在 {@link MigrateArgs#keys(String...)} 指定待迁移键后，\n * 依次设置 host、port、database 与 timeout 等参数。\n *\n * @author lyrric\n */",
        ),
        (
            "/**\n     * Defines database of destination instance\n     *\n     * @param database database, should be greater than or eq 0\n     * @return migrate conditions object\n     */",
            "/**\n     * 指定目标 Redis 实例的逻辑数据库编号。\n     * <p>\n     * 数据库索引应大于等于 0。\n     *\n     * @param database 目标数据库编号，应 ≥ 0\n     * @return 迁移条件构建器，可继续设置超时等参数\n     */",
        ),
    ],
    f"{_K}/HostMigrateArgs.java": [
        (
            "/**\n * DatabaseMigrateArgs\n *\n * @author lyrric\n */",
            "/**\n * 键迁移参数链中的「目标主机」配置步骤。\n * <p>\n * 由 {@link MigrateArgs#keys(String...)} 返回，用于指定远程 Redis 地址。\n *\n * @author lyrric\n */",
        ),
        (
            "/**\n     * Defines host of destination instance\n     *\n     * @param host host\n     * @return migrate conditions object\n     */",
            "/**\n     * 设置目标 Redis 实例的主机名或 IP 地址。\n     *\n     * @param host 目标主机\n     * @return 迁移条件构建器，可继续设置端口\n     */",
        ),
    ],
    f"{_K}/MigrateArgs.java": [
        (
            "/**\n * MigrateArgs\n *\n * @author lyrric\n */",
            "/**\n * {@link org.redisson.api.RKeys#migrate(MigrateArgs)} 的键迁移参数入口。\n * <p>\n * 通过静态工厂 {@link #keys(String...)} 创建链式构建器，\n * 逐步配置目标实例连接信息与可选认证参数。\n *\n * @author lyrric\n */",
        ),
        (
            "/**\n     * Defines keys to transfer\n     * Redis version >= 3.0.6\n     *\n     * @param keys keys to migrate，not empty\n     * @return migrate conditions object\n     */",
            "/**\n     * 指定待迁移的键名列表。\n     * <p>\n     * 需要 Redis 3.0.6 及以上版本；键数组不可为空。\n     *\n     * @param keys 待迁移的键，不可为空\n     * @return 迁移条件构建器，可继续设置 host\n     */",
        ),
    ],
    f"{_K}/MigrateParams.java": [
        (
            "/**\n * Arguments objects for RKeys.migrate()\n *\n * @author lyrric\n */",
            "/**\n * {@link org.redisson.api.RKeys#migrate(MigrateArgs)} 的参数实现类。\n * <p>\n * 聚合待迁移键、目标连接信息、超时、迁移模式及 ACL 认证等字段，\n * 实现迁移参数链上的全部接口。\n *\n * @author lyrric\n */",
        ),
        (
            "    /**\n     * keys to transfer Redis version >= 3.0.6\n     */",
            "    /** 待迁移的键名数组，需 Redis 3.0.6+。 */",
        ),
        (
            "    /**\n     * destination host\n     */",
            "    /** 目标 Redis 主机地址。 */",
        ),
        (
            "    /**\n     * destination port\n     */",
            "    /** 目标 Redis 端口。 */",
        ),
        (
            "    /**\n     * destination database\n     */",
            "    /** 目标逻辑数据库编号。 */",
        ),
        (
            "    /**\n     * maximum idle time in any moment of the communication with the destination instance in milliseconds\n     */",
            "    /** 与目标实例通信过程中允许的最大空闲时间（毫秒）。 */",
        ),
        (
            "    /**\n     * migration mode\n     */",
            "    /** 迁移模式，默认为 {@link org.redisson.api.MigrateMode#MIGRATE}。 */",
        ),
        (
            "    /**\n     * destination username Redis version >= 6.0.0\n     */",
            "    /** 目标实例 ACL 用户名，需 Redis 6.0+。 */",
        ),
        (
            "    /**\n     * destination password Redis version >= 4.0.7\n     */",
            "    /** 目标实例密码，需 Redis 4.0.7+。 */",
        ),
        (
            "    public MigrateParams(String[] keys) {",
            "    /** 以待迁移键数组构造参数对象。 */\n    public MigrateParams(String[] keys) {",
        ),
        (
            "    @Override\n    public PortMigrateArgs host(String host) {",
            "    /** 设置目标主机并返回自身以继续链式调用。 */\n    @Override\n    public PortMigrateArgs host(String host) {",
        ),
        (
            "    @Override\n    public DatabaseMigrateArgs port(int port) {",
            "    /** 设置目标端口。 */\n    @Override\n    public DatabaseMigrateArgs port(int port) {",
        ),
        (
            "    @Override\n    public TimeoutMigrateArgs database(int database) {",
            "    /** 设置目标数据库编号。 */\n    @Override\n    public TimeoutMigrateArgs database(int database) {",
        ),
        (
            "    @Override\n    public OptionalMigrateArgs timeout(long timeout) {",
            "    /** 设置通信超时（毫秒）。 */\n    @Override\n    public OptionalMigrateArgs timeout(long timeout) {",
        ),
        (
            "    @Override\n    public OptionalMigrateArgs mode(MigrateMode mode) {",
            "    /** 设置迁移模式（迁移或复制）。 */\n    @Override\n    public OptionalMigrateArgs mode(MigrateMode mode) {",
        ),
        (
            "    @Override\n    public OptionalMigrateArgs username(String username) {",
            "    /** 设置目标实例 ACL 用户名。 */\n    @Override\n    public OptionalMigrateArgs username(String username) {",
        ),
        (
            "    @Override\n    public OptionalMigrateArgs password(String password) {",
            "    /** 设置目标实例密码。 */\n    @Override\n    public OptionalMigrateArgs password(String password) {",
        ),
        (
            "    public String[] getKeys() {",
            "    /** 返回待迁移键数组。 */\n    public String[] getKeys() {",
        ),
        (
            "    public String getHost() {",
            "    /** 返回目标主机。 */\n    public String getHost() {",
        ),
        (
            "    public int getPort() {",
            "    /** 返回目标端口。 */\n    public int getPort() {",
        ),
        (
            "    public int getDatabase() {",
            "    /** 返回目标数据库编号。 */\n    public int getDatabase() {",
        ),
        (
            "    public long getTimeout() {",
            "    /** 返回通信超时毫秒数。 */\n    public long getTimeout() {",
        ),
        (
            "    public MigrateMode getMode() {",
            "    /** 返回迁移模式。 */\n    public MigrateMode getMode() {",
        ),
        (
            "    public String getUsername() {",
            "    /** 返回目标 ACL 用户名。 */\n    public String getUsername() {",
        ),
        (
            "    public String getPassword() {",
            "    /** 返回目标密码。 */\n    public String getPassword() {",
        ),
    ],
    f"{_K}/OptionalMigrateArgs.java": [
        (
            "/**\n * OptionalMigrateArgs\n *\n * @author lyrric\n */",
            "/**\n * 键迁移参数链中的可选配置步骤。\n * <p>\n * 在设置 timeout 之后，可进一步指定迁移模式与目标实例认证信息。\n *\n * @author lyrric\n */",
        ),
        (
            "/**\n     * Defines migrate mode\n     * @see org.redisson.api.MigrateMode\n     *\n     * @param mode migrate mode\n     * @return migrate conditions object\n     */",
            "/**\n     * 设置键迁移模式（迁移或复制）。\n     * @see org.redisson.api.MigrateMode\n     *\n     * @param mode 迁移模式\n     * @return 迁移条件构建器\n     */",
        ),
        (
            "/**\n     * Defines username of destination instance\n     * <p>\n     * Authenticate with the given username to the remote instance.\n     * <p>\n     * if username is set, then password should be set too.\n     * <p>\n     * Redis 6 or greater ACL auth style\n     *\n     * @param username distinction username\n     * @return migrate conditions object\n     */",
            "/**\n     * 设置目标 Redis 实例的 ACL 用户名。\n     * <p>\n     * 使用给定用户名对远程实例进行认证；若设置用户名则通常需同时设置密码。\n     * <p>\n     * 适用于 Redis 6+ 的 ACL 认证方式。\n     *\n     * @param username 目标实例用户名\n     * @return 迁移条件构建器\n     */",
        ),
        (
            "/**\n     * Defines password of destination instance\n     * <p>\n     * Authenticate with the given password to the remote instance.\n     *\n     * @param password distinction password\n     * @return migrate conditions object\n     */",
            "/**\n     * 设置目标 Redis 实例的访问密码。\n     * <p>\n     * 使用给定密码对远程实例进行认证。\n     *\n     * @param password 目标实例密码\n     * @return 迁移条件构建器\n     */",
        ),
    ],
    f"{_K}/PortMigrateArgs.java": [
        (
            "/**\n * PortMigrateArgs\n * @author lyrric\n */",
            "/**\n * 键迁移参数链中的「目标端口」配置步骤。\n * <p>\n * 在设置 host 之后调用，用于指定远程 Redis 监听端口。\n *\n * @author lyrric\n */",
        ),
        (
            "/**\n     * Defines port of destination instance\n     * @param port port\n     * @return migrate conditions object\n     */",
            "/**\n     * 设置目标 Redis 实例的 TCP 端口。\n     *\n     * @param port 目标端口\n     * @return 迁移条件构建器，可继续设置数据库\n     */",
        ),
    ],
    f"{_K}/TimeoutMigrateArgs.java": [
        (
            "/**\n * TimeoutMigrateArgs\n *\n * @author lyrric\n */",
            "/**\n * 键迁移参数链中的「通信超时」配置步骤。\n * <p>\n * 在设置 database 之后调用，用于限制与目标实例通信的空闲时间。\n *\n * @author lyrric\n */",
        ),
        (
            "/**\n     * Defines maximum idle time in any moment of the communication with the destination instance in milliseconds\n     *\n     * @param timeout timeout\n     * @return migrate conditions object\n     */",
            "/**\n     * 设置与目标实例通信过程中允许的最大空闲时间（毫秒）。\n     * <p>\n     * 超过该时间未收到响应则中断迁移操作。\n     *\n     * @param timeout 超时毫秒数\n     * @return 迁移条件构建器，可继续设置可选参数\n     */",
        ),
    ],
    f"{_L}/BasePatternStatusListener.java": [
        (
            "/**\n * Base status listener for Redis PubSub channel status changes\n *\n * @author Nikita Koksharov\n *\n * @see org.redisson.api.RTopic\n */",
            "/**\n * Redis Pub/Sub 模式订阅（pattern subscribe）状态变更的基类监听器。\n * <p>\n * 提供 {@link PatternStatusListener} 的空实现，子类可按需覆盖\n * {@link #onPSubscribe(String)} 与 {@link #onPUnsubscribe(String)}。\n *\n * @author Nikita Koksharov\n *\n * @see org.redisson.api.RTopic\n */",
        ),
        (
            "    @Override\n    public void onPSubscribe(String channel) {",
            "    /** 模式订阅成功时的回调，默认空实现。 */\n    @Override\n    public void onPSubscribe(String channel) {",
        ),
        (
            "    @Override\n    public void onPUnsubscribe(String channel) {",
            "    /** 取消模式订阅时的回调，默认空实现。 */\n    @Override\n    public void onPUnsubscribe(String channel) {",
        ),
    ],
    f"{_L}/BaseStatusListener.java": [
        (
            "/**\n * Base status listener for Redis PubSub channel status changes\n *\n * @author Nikita Koksharov\n *\n * @see org.redisson.api.RTopic\n */",
            "/**\n * Redis Pub/Sub 频道订阅状态变更的基类监听器。\n * <p>\n * 提供 {@link StatusListener} 的空实现，子类可按需覆盖\n * {@link #onSubscribe(String)} 与 {@link #onUnsubscribe(String)}。\n *\n * @author Nikita Koksharov\n *\n * @see org.redisson.api.RTopic\n */",
        ),
        (
            "    @Override\n    public void onSubscribe(String channel) {",
            "    /** 频道订阅成功时的回调，默认空实现。 */\n    @Override\n    public void onSubscribe(String channel) {",
        ),
        (
            "    @Override\n    public void onUnsubscribe(String channel) {",
            "    /** 取消频道订阅时的回调，默认空实现。 */\n    @Override\n    public void onUnsubscribe(String channel) {",
        ),
    ],
    f"{_L}/DequeAddFirstListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>lpush</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain El letters\n *\n * @author nhancdt\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的双端队列<b>头部插入</b>（lpush）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code l} 字母。\n *\n * @author nhancdt\n */",
        ),
        (
            "/**\n     * Invoked when elements added to deque head\n     *\n     * @param name object name\n     */",
            "/**\n     * 当元素被添加到双端队列头部时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/DequeAddLastListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>rpush</b> event published by Valkey or Redis..\n * <p>\n * Redis notify-keyspace-events setting should contain El letters\n *\n * @author nhancdt\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的双端队列<b>尾部插入</b>（rpush）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code l} 字母。\n *\n * @author nhancdt\n */",
        ),
        (
            "/**\n     * Invoked when elements added to deque tail\n     *\n     * @param name object name\n     */",
            "/**\n     * 当元素被添加到双端队列尾部时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/FlushListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>flush</b> event published by Valkey or Redis.\n * <p>\n * Requires Redis 6.0+\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的<b>清空数据库</b>（flushdb/flushall）键空间事件。\n * <p>\n * 需要 Redis 6.0 及以上版本。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked when `flushdb` or `flushall` command\n     * was executed on Redis node.\n     *\n     * @param address\n     */",
            "/**\n     * 当 Redis 节点执行 {@code flushdb} 或 {@code flushall} 命令时触发。\n     *\n     * @param address 执行清空命令的节点地址\n     */",
        ),
    ],
    f"{_L}/IncrByListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>incrby</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Ex letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的<b>计数器递增</b>（incrby）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code x} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked on counter change event\n     *\n     * @param name - name of object\n     */",
            "/**\n     * 计数器数值发生变化时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/ListAddListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>rpush</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain El letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的列表<b>尾部追加</b>（rpush）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code l} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked on event of adding element to list\n     *\n     * @param name - name of object\n     */",
            "/**\n     * 当元素被添加到列表尾部时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
    f"{_L}/ListInsertListener.java": [
        (
            "/**\n * Redisson Object Event listener for <b>linsert</b> event published by Valkey or Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain El letters\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 监听 Valkey 或 Redis 发布的列表<b>中间插入</b>（linsert）键空间事件。\n * <p>\n * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code l} 字母。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked on event of setting element to list\n     *\n     * @param name - name of object\n     */",
            "/**\n     * 当元素被插入到列表指定位置时触发。\n     *\n     * @param name 对象名称（键名）\n     */",
        ),
    ],
}
