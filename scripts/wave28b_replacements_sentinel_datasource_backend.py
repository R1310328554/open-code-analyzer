"""Chinese JavaDoc replacements for Sentinel 1.8.10 wave28b datasource backend integrations."""

BACKEND_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ConsulDataSource.java": [
        (
            "/**\n * <p>\n * A read-only {@code DataSource} with Consul backend.\n * <p>\n * <p>\n * The data source first initial rules from a Consul during initialization.\n * Then it start a watcher to observe the updates of rule date and update to memory.\n *\n * Consul do not provide http api to watch the update of KV，so it use a long polling and\n * <a href=\"https://www.consul.io/api/features/blocking.html\">blocking queries</a> of the Consul's feature\n * to watch and update value easily.When Querying data by index will blocking until change or timeout. If\n * the index of the current query is larger than before, it means that the data has changed.\n * </p>\n *\n * @author wavesZh\n * @author Zhiguo.Chen\n */",
            "/**\n * <p>\n * 基于 Consul KV 的只读数据源。\n * </p>\n * <p>\n * 初始化时从 Consul 加载规则，随后启动后台 watcher 监听 KV 变更并更新内存。\n * Consul 无原生 KV 变更推送 HTTP API，故采用\n * <a href=\"https://www.consul.io/api/features/blocking.html\">blocking queries</a>\n * 长轮询：按 index 查询会阻塞至变更或超时；若返回 index 大于上次，表示数据已更新。\n * </p>\n *\n * @author wavesZh\n * @author Zhiguo.Chen\n */",
        ),
        (
            "    /**\n     * Request of query will hang until timeout (in second) or get updated value.\n     */",
            "    /** 长轮询超时时间（秒），超时或无变更则返回。 */",
        ),
        (
            "    /**\n     * Record the data's index in Consul to watch the change.\n     * If lastIndex is smaller than the index of next query, it means that rule data has updated.\n     */",
            "    /** 记录 Consul 返回的 index，用于 blocking query 增量监听。 */",
        ),
        (
            "    /**\n     * Constructor of {@code ConsulDataSource}.\n     *\n     * @param parser       customized data parser, cannot be empty\n     * @param host         consul agent host\n     * @param port         consul agent port\n     * @param ruleKey      data key in Consul\n     * @param watchTimeout request for querying data will be blocked until new data or timeout. The unit is second (s)\n     */",
            "    /**\n     * 构造 Consul 数据源（默认端口 8500）。\n     *\n     * @param parser       自定义配置解析器，不可为空\n     * @param host         Consul Agent 主机\n     * @param port         Consul Agent 端口\n     * @param ruleKey      Consul KV 键\n     * @param watchTimeout 长轮询超时（秒）\n     */",
        ),
        (
            "    /**\n     * Constructor of {@code ConsulDataSource}.\n     *\n     * @param parser       customized data parser, cannot be empty\n     * @param host         consul agent host\n     * @param port         consul agent port\n     * @param token     consul agent acl token\n     * @param ruleKey      data key in Consul\n     * @param watchTimeout request for querying data will be blocked until new data or timeout. The unit is second (s)\n     */",
            "    /**\n     * 构造带 ACL Token 的 Consul 数据源。\n     *\n     * @param parser       自定义配置解析器\n     * @param host         Consul Agent 主机\n     * @param port         Consul Agent 端口\n     * @param token        ACL Token（可为 null）\n     * @param ruleKey      Consul KV 键\n     * @param watchTimeout 长轮询超时（秒）\n     */",
        ),
        (
            "                // It will be blocked until watchTimeout(s) if rule data has no update.",
            "                // 无变更时将阻塞最长 watchTimeout 秒",
        ),
        (
            "                        // In case of parsing error.",
            "                        // 解析失败时记录日志，不中断 watcher",
        ),
        (
            "    /**\n     * Get data from Consul immediately.\n     *\n     * @param key data key in Consul\n     * @return the value associated to the key, or null if error occurs\n     */",
            "    /**\n     * 非阻塞方式立即读取 Consul KV。\n     *\n     * @param key Consul KV 键\n     * @return 键对应值，失败返回 null\n     */",
        ),
        (
            "    /**\n     * Get data from Consul (blocking).\n     *\n     * @param key      data key in Consul\n     * @param index    the index of data in Consul.\n     * @param waitTime time(second) for waiting get updated value.\n     * @return the value associated to the key, or null if error occurs\n     */",
            "    /**\n     * 阻塞方式读取 Consul KV（blocking query）。\n     *\n     * @param key      Consul KV 键\n     * @param index    上次已知 index\n     * @param waitTime 最长等待秒数\n     * @return 键对应值，失败返回 null\n     */",
        ),
    ],
    "EtcdConfig.java": [
        (
            "/**\n * Etcd connection configuration.\n *\n * @author lianglin\n * @since 1.7.0\n */",
            "/**\n * Etcd 连接配置项，从 {@link com.alibaba.csp.sentinel.config.SentinelConfig} 读取。\n *\n * @author lianglin\n * @since 1.7.0\n */",
        ),
        (
            "    public static String getEndPoints() {",
            "    /** 获取 Etcd 端点列表（逗号分隔）。 */\n    public static String getEndPoints() {",
        ),
        (
            "    public static String getUser() {",
            "    /** 获取 Etcd 认证用户名。 */\n    public static String getUser() {",
        ),
        (
            "    public static String getPassword() {",
            "    /** 获取 Etcd 认证密码。 */\n    public static String getPassword() {",
        ),
        (
            "    public static String getCharset() {",
            "    /** 获取 Etcd 通信字符集，未配置则回退 Sentinel 全局 charset。 */\n    public static String getCharset() {",
        ),
        (
            "    public static boolean isAuthEnable() {",
            "    /** 是否启用 Etcd 用户名密码认证。 */\n    public static boolean isAuthEnable() {",
        ),
        (
            "    public static String getAuthority() {",
            "    /** 获取 Etcd authority 头（TLS/SNI 场景）。 */\n    public static String getAuthority() {",
        ),
    ],
    "EtcdDataSource.java": [
        (
            "/**\n * A read-only {@code DataSource} with Etcd backend. When the data in Etcd backend has been modified,\n * Etcd will automatically push the new value so that the dynamic configuration can be real-time.\n *\n * @author lianglin\n * @since 1.7.0\n */",
            "/**\n * 基于 Etcd 的只读数据源：Etcd 键值变更时通过 Watch 推送，实现规则实时更新。\n *\n * @author lianglin\n * @since 1.7.0\n */",
        ),
        (
            "    /**\n     * Create an etcd data-source. The connection configuration will be retrieved from {@link EtcdConfig}.\n     *\n     * @param key    config key\n     * @param parser data parser\n     */",
            "    /**\n     * 创建 Etcd 数据源，连接参数从 {@link EtcdConfig} 读取。\n     *\n     * @param key    配置键\n     * @param parser 配置解析器\n     */",
        ),
        (
            "    private void loadInitialConfig() {",
            "    /** 启动时加载初始配置并写入 property。 */\n    private void loadInitialConfig() {",
        ),
        (
            "    private void initWatcher() {",
            "    /** 注册 Etcd Watch，处理 PUT/DELETE 事件。 */\n    private void initWatcher() {",
        ),
    ],
    "EurekaDataSource.java": [
        (
            "/**\n * <p>\n * A {@link ReadableDataSource} based on Eureka. This class will automatically\n * fetches the metadata of the instance every period.\n * </p>\n * <p>\n * Limitations: Default refresh interval is 10s. Because there is synchronization between eureka servers,\n * it may take longer to take effect.\n * </p>\n *\n * @author liyang\n * @since 1.8.0\n */",
            "/**\n * <p>\n * 基于 Eureka 实例元数据的自动刷新数据源，周期性拉取规则配置。\n * </p>\n * <p>\n * 限制：默认刷新间隔 10 秒；Eureka 集群同步可能导致生效延迟更长。\n * </p>\n *\n * @author liyang\n * @since 1.8.0\n */",
        ),
        (
            "    /**\n     * Default connect timeout: 3s\n     */",
            "    /** 默认连接超时：3 秒 */",
        ),
        (
            "    /**\n     * Default read timeout: 30s\n     */",
            "    /** 默认读取超时：30 秒 */",
        ),
        (
            "    /**\n     * Eureka instance app ID.\n     */",
            "    /** Eureka 应用 ID（app name）。 */",
        ),
        (
            "    /**\n     * Eureka instance id.\n     */",
            "    /** Eureka 实例 ID。 */",
        ),
        (
            "    /**\n     * Eureka server URL list.\n     */",
            "    /** Eureka Server URL 列表（可多个，失败时随机重试）。 */",
        ),
        (
            "    /**\n     * Metadata key of the rule source.\n     */",
            "    /** 实例 metadata 中存放规则的键名。 */",
        ),
        (
            "                    //ignore",
            "                    // 忽略解析主机地址失败",
        ),
    ],
    "NacosDataSource.java": [
        (
            "/**\n * A read-only {@code DataSource} with Nacos backend. When the data in Nacos backend has been modified,\n * Nacos will automatically push the new value so that the dynamic configuration can be real-time.\n *\n * @author Eric Zhao\n */",
            "/**\n * 基于 Nacos 的只读数据源：配置变更时 Nacos 主动推送，实现规则实时更新。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    /**\n     * Single-thread pool. Once the thread pool is blocked, we throw up the old task.\n     */",
            "    /** 单线程池；队列满时丢弃最旧任务以保证最新配置优先处理。 */",
        ),
        (
            "    /**\n     * Note: The Nacos config might be null if its initialization failed.\n     */",
            "    /** 注意：Nacos 初始化失败时 configService 可能为 null。 */",
        ),
        (
            "    /**\n     * Constructs an read-only DataSource with Nacos backend.\n     *\n     * @param serverAddr server address of Nacos, cannot be empty\n     * @param groupId    group ID, cannot be empty\n     * @param dataId     data ID, cannot be empty\n     * @param parser     customized data parser, cannot be empty\n     */",
            "    /**\n     * 使用 Nacos 服务地址构造只读数据源。\n     *\n     * @param serverAddr Nacos 服务地址\n     * @param groupId    配置分组\n     * @param dataId     配置 dataId\n     * @param parser     配置解析器\n     */",
        ),
        (
            "                // Update the new value to the property.",
            "                // 将新配置写入 SentinelProperty",
        ),
        (
            "            // Add config listener.",
            "            // 注册 Nacos 配置变更监听器",
        ),
    ],
    "RedisDataSource.java": [
        (
            "/**\n * <p>\n * A read-only {@code DataSource} with Redis backend.\n * </p>\n * <p>\n * The data source first loads initial rules from a Redis String during initialization.\n * Then the data source subscribe from specific channel. When new rules is published to the channel,\n * the data source will observe the change in realtime and update to memory.\n * </p>\n * <p>\n * Note that for consistency, users should publish the value and save the value to the ruleKey simultaneously\n * like this (using Redis transaction):\n * <pre>\n *  MULTI\n *  SET ruleKey value\n *  PUBLISH channel value\n *  EXEC\n * </pre>\n * </p>\n *\n * @author tiger\n */",
            "/**\n * <p>\n * 基于 Redis 的只读数据源。\n * </p>\n * <p>\n * 初始化时从 Redis String（ruleKey）加载规则，并订阅指定 channel；\n * 发布新规则时实时更新内存中的 {@link SentinelProperty}。\n * </p>\n * <p>\n * 为保证一致性，建议用 Redis 事务同时 SET ruleKey 与 PUBLISH channel：\n * <pre>\n *  MULTI\n *  SET ruleKey value\n *  PUBLISH channel value\n *  EXEC\n * </pre>\n * </p>\n *\n * @author tiger\n */",
        ),
        (
            "    /**\n     * Constructor of {@code RedisDataSource}.\n     *\n     * @param connectionConfig Redis connection config\n     * @param ruleKey          data key in Redis\n     * @param channel          channel to subscribe in Redis\n     * @param parser           customized data parser, cannot be empty\n     */",
            "    /**\n     * 构造 Redis 数据源（支持单机、Sentinel、Cluster）。\n     *\n     * @param connectionConfig Redis 连接配置\n     * @param ruleKey          规则 String 键\n     * @param channel          Pub/Sub 频道\n     * @param parser           配置解析器\n     */",
        ),
        (
            "    /**\n     * init SslOptions, support jks or pem format\n     *\n     * @param connectionConfig Redis connection config\n     * @return a new SslOptions\n     */",
            "    /**\n     * 初始化 SSL 选项，支持 JKS 或 PEM 格式证书。\n     *\n     * @param connectionConfig Redis 连接配置\n     * @return SslOptions 实例，未启用 SSL 时返回 null\n     */",
        ),
        (
            "                // if the value is end with .jks，think it is java key store format，to invoke truststore method",
            "                // .jks 后缀视为 Java KeyStore，调用 truststore",
        ),
        (
            "                // if the value is not end with .jks，think it is pem format，to invoke trustManager method",
            "                // 非 .jks 视为 PEM，调用 trustManager",
        ),
        (
            "    /**\n     * Build Redis client fromm {@code RedisConnectionConfig}.\n     *\n     * @return a new {@link RedisClient}\n     */",
            "    /**\n     * 根据 {@link RedisConnectionConfig} 构建 Lettuce {@link RedisClient}。\n     *\n     * @return 新的 RedisClient\n     */",
        ),
        (
            "        //If any uri is successful for connection, the others are not tried anymore",
            "        // 任一 URI 连接成功即停止尝试其余节点",
        ),
        (
            "            //All redis nodes must have same password",
            "            // 集群各节点须使用相同密码",
        ),
        (
            "    private void subscribeFromChannel(String channel) {",
            "    /** 订阅 Redis Pub/Sub 频道，收到消息时更新 property。 */\n    private void subscribeFromChannel(String channel) {",
        ),
        (
            "    private void loadInitialConfig() {",
            "    /** 启动时从 ruleKey 读取初始规则。 */\n    private void loadInitialConfig() {",
        ),
    ],
}
