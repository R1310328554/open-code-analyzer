"""Chinese annotation replacements for Nacos 3.2.3 wave77a [0:15] auth utils + control + ds base."""

R: dict[str, list[tuple[str, str]]] = {}

# --- User (users package) ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/User.java"] = [
    (
        "/**\n * User information in authorization.\n *\n * @author nkorange\n * @author mai.jh\n * @since 1.2.0\n */",
        "/**\n * 鉴权模块中的用户信息模型。\n *\n * <p>轻量级用户标识，仅包含 {@code userName} 字段，"
        " 用于鉴权上下文传递与序列化，区别于持久化层 {@code persistence.User}。</p>\n *\n"
        " * @author nkorange\n * @author mai.jh\n * @since 1.2.0\n */",
    ),
    (
        "    /**\n     * Unique string representing user.\n     */",
        "    /** 唯一标识用户的登录名。 */",
    ),
    (
        "    public String getUserName() {",
        "    /** 获取用户名。 */\n    public String getUserName() {",
    ),
    (
        "    public void setUserName(String userName) {",
        "    /** 设置用户名。 */\n    public void setUserName(String userName) {",
    ),
]

# --- Base64Decode ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/utils/Base64Decode.java"] = [
    (
        "/**\n * Base64Decoder.\n *\n * @author xYohn\n * @date 2023/8/7\n */",
        "/**\n * Base64 解码工具类（无第三方依赖）。\n *\n * <p>支持标准 Base64 字母表、填充符 {@code =} 及 76 字符换行分隔；"
        " 非法字符将抛出 {@link IllegalArgumentException}。</p>\n *\n * @author xYohn\n * @date 2023/8/7\n */",
    ),
    (
        "    /**\n     * Decodes a Base64 encoded String into a newly-allocated byte array using the Base64 encoding scheme.\n     *\n     * @param input the string to decode\n     * @return a byte array containing binary data\n     */",
        "    /**\n     * 将 Base64 编码字符串解码为新分配的字节数组。\n     *\n     * @param input the string to decode\n     * @return a byte array containing binary data\n     */",
    ),
    (
        "        // Check special case",
        "        // 空输入直接返回空数组",
    ),
    (
        "        // Start and end index after trimming.",
        "        // 裁剪非法字符后的起止下标",
    ),
    (
        "        // Trim illegal chars from start",
        "        // 跳过首部非法 Base64 字符",
    ),
    (
        "        // Trim illegal chars from end",
        "        // 跳过尾部非法 Base64 字符",
    ),
    (
        "        // get the padding count (=) (0, 1 or 2)",
        "        // 统计末尾填充符 {@code =} 个数（0/1/2）",
    ),
    (
        "        // Count '=' at end.",
        "        // 根据末尾 {@code =} 判断填充长度",
    ),
    (
        "        // Content count including possible separators",
        "        // 有效字符数（含可能的换行分隔符）",
    ),
    (
        "        // The number of decoded bytes",
        "        // 计算解码后的字节长度",
    ),
    (
        "        // Preallocate byte[] of exact length",
        "        // 预分配精确长度的结果数组",
    ),
    (
        "        // Decode all but the last 0 - 2 bytes.",
        "        // 批量解码除最后 0～2 字节外的全部内容",
    ),
    (
        "            // Assemble three bytes into an int from four \"valid\" characters.",
        "            // 四个合法字符拼成一个 24 位整数",
    ),
    (
        "            // Add the bytes",
        "            // 拆出三个字节写入结果数组",
    ),
    (
        "            // If line separator, jump over it.",
        "            // 遇到 76 字符换行则跳过 \\r\\n",
    ),
    (
        "            // Decode last 1-3 bytes (incl '=') into 1-3 bytes",
        "            // 处理末尾带填充的最后 1～3 字节",
    ),
    (
        "    private static int ctoi(char c) {",
        "    /** 将 Base64 字符映射为 6 位索引值，非法字符抛异常。 */\n    private static int ctoi(char c) {",
    ),
]

# --- PasswordEncoderUtil ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/utils/PasswordEncoderUtil.java"] = [
    (
        "/**\n * Password encoder tool.\n *\n * @author nacos\n */",
        "/**\n * 密码编解码工具类。\n *\n * <p>委托 {@link SafeBcryptPasswordEncoder} 完成 BCrypt 哈希与校验；"
        " {@link #encode} 会校验明文长度不超过 {@link AuthConstants#MAX_PASSWORD_LENGTH}。</p>\n *\n * @author nacos\n */",
    ),
    (
        "    public static Boolean matches(String raw, String encoded) {",
        "    /** 校验明文密码是否与已编码哈希匹配。 */\n    public static Boolean matches(String raw, String encoded) {",
    ),
    (
        "    /**\n     * Encode password.\n     *\n     * @param raw password\n     * @return encoded password\n     */",
        "    /**\n     * 对明文密码进行 BCrypt 编码。\n     *\n     * @param raw password\n     * @return encoded password\n     */",
    ),
]

# --- PasswordGeneratorUtil ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/utils/PasswordGeneratorUtil.java"] = [
    (
        "/**\n * RandomPasswordGenerator .\n *\n * @author : huangtianhui\n */",
        "/**\n * 随机密码生成器。\n *\n * <p>固定长度 8 位，强制包含小写、大写、数字与特殊字符各至少一个，"
        " 最终打乱顺序以避免可预测模式。</p>\n *\n * @author : huangtianhui\n */",
    ),
    (
        "    /**\n     * generateRandomPassword.\n     * @return\n     */",
        "    /**\n     * 生成符合复杂度要求的随机密码。\n     * @return 8 位随机密码字符串\n     */",
    ),
    (
        "        // Fill the rest of the password with random characters from all categories",
        "        // 从全部字符集中随机填充剩余位数",
    ),
    (
        "        // Shuffle to avoid predictable order",
        "        // 打乱顺序，避免类型位置固定",
    ),
    (
        "        // Build the final password string",
        "        // 拼接为最终密码字符串",
    ),
]

# --- RemoteServerUtil ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/utils/RemoteServerUtil.java"] = [
    (
        "/**\n * Nacos auth plugin remote nacos server util.\n *\n * @author xiweng.yy\n */",
        "/**\n * 远程 Nacos 集群地址与 HTTP 辅助工具。\n *\n * <p>从 {@code cluster.conf} 读取节点列表并监听变更，"
        " 提供轮询选取、上下文路径及 {@link AuthConfigs} 身份头构造。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private static final String DEFAULT_REMOTE_SERVER_CONTEXT_PATH = \"/nacos\";",
        "    /** 远程控制台默认上下文路径。 */\n    private static final String DEFAULT_REMOTE_SERVER_CONTEXT_PATH = \"/nacos\";",
    ),
    (
        "    private static List<String> serverAddresses = new LinkedList<>();",
        "    /** 集群节点地址列表（可变，由文件监听刷新）。 */\n    private static List<String> serverAddresses = new LinkedList<>();",
    ),
    (
        "    private static AtomicInteger index = new AtomicInteger();",
        "    /** 轮询下标，用于 {@link #getOneNacosServerAddress()}。 */\n    private static AtomicInteger index = new AtomicInteger();",
    ),
    (
        "    private static void initRemoteServerContextPath() {",
        "    /** 从环境配置读取远程控制台 context-path。 */\n    private static void initRemoteServerContextPath() {",
    ),
    (
        "    private static void registerWatcher() {",
        "    /** 注册 cluster.conf 文件变更监听器。 */\n    private static void registerWatcher() {",
    ),
    (
        "    /**\n     * Read nacos server address from cluster.conf.\n     */",
        "    /** 从 cluster.conf 重新加载集群节点地址。 */",
    ),
    (
        "    public static List<String> getServerAddresses() {",
        "    /** 返回集群地址列表的副本。 */\n    public static List<String> getServerAddresses() {",
    ),
    (
        "    public static String getOneNacosServerAddress() {",
        "    /** 轮询返回一个集群节点地址。 */\n    public static String getOneNacosServerAddress() {",
    ),
    (
        "    public static String getRemoteServerContextPath() {",
        "    /** 获取远程控制台上下文路径。 */\n    public static String getRemoteServerContextPath() {",
    ),
    (
        "    /**\n     * Single check http result, if not success, wrapper result as Nacos exception.\n     *\n     * @param result http execute result\n     * @throws NacosException wrapper result as NacosException\n     */",
        "    /**\n     * 校验 HTTP 响应，失败时封装为 {@link NacosException} 抛出。\n     *\n     * @param result http execute result\n     * @throws NacosException wrapper result as NacosException\n     */",
    ),
    (
        "    /**\n     * According input {@link AuthConfigs} to build remote server identity header.\n     *\n     * @param authConfigs authConfigs\n     * @return remote server identity header\n     */",
        "    /**\n     * 根据 {@link AuthConfigs} 构造远程服务端身份认证请求头。\n     *\n     * @param authConfigs authConfigs\n     * @return remote server identity header\n     */",
    ),
]

# --- NacosConnectionControlManager ---

R["plugin-default-impl/nacos-default-control-plugin/src/main/java/com/alibaba/nacos/plugin/control/impl/NacosConnectionControlManager.java"] = [
    (
        "/**\n * Nacos default control plugin implementation.\n *\n * @author shiyiyue\n */",
        "/**\n * Nacos 默认连接数管控管理器。\n *\n * <p>继承 {@link ConnectionControlManager}，按规则汇总各采集器的连接总数；"
        " {@code countLimit < 0} 表示不限制。</p>\n *\n * @author shiyiyue\n */",
    ),
    (
        "    @Override\n    public String getName() {",
        "    /** 返回插件标识 {@code nacos}。 */\n    @Override\n    public String getName() {",
    ),
    (
        "    public NacosConnectionControlManager() {",
        "    /** 构造默认连接管控管理器。 */\n    public NacosConnectionControlManager() {",
    ),
    (
        "    @Override\n    public void applyConnectionLimitRule(ConnectionControlRule connectionControlRule) {",
        "    /** 更新连接数上限规则并记录日志（当前实现仅记录警告）。 */\n    @Override\n    public void applyConnectionLimitRule(ConnectionControlRule connectionControlRule) {",
    ),
    (
        "    @Override\n    public ConnectionCheckResponse check(ConnectionCheckRequest connectionCheckRequest) {",
        "    /** 校验当前总连接数是否超过规则上限。 */\n    @Override\n    public ConnectionCheckResponse check(ConnectionCheckRequest connectionCheckRequest) {",
    ),
    (
        "        // If totalCountLimit less than 0, no limit is applied.",
        "        // 上限小于 0 时不做连接数限制",
    ),
    (
        "        // Get total connection from metrics",
        "        // 汇总各指标采集器的连接总数",
    ),
]

# --- NacosControlManagerBuilder ---

R["plugin-default-impl/nacos-default-control-plugin/src/main/java/com/alibaba/nacos/plugin/control/impl/NacosControlManagerBuilder.java"] = [
    (
        "/**\n * Nacos default control plugin implementation.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 默认管控插件构建器。\n *\n * <p>实现 {@link ControlManagerBuilder} SPI，"
        " 分别创建连接管控与 TPS 管控管理器实例。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    @Override\n    public String getName() {",
        "    /** 返回构建器名称 {@code nacos}。 */\n    @Override\n    public String getName() {",
    ),
    (
        "    @Override\n    public ConnectionControlManager buildConnectionControlManager() {",
        "    /** 构建 {@link NacosConnectionControlManager} 实例。 */\n    @Override\n    public ConnectionControlManager buildConnectionControlManager() {",
    ),
    (
        "    @Override\n    public TpsControlManager buildTpsControlManager() {",
        "    /** 构建 {@link NacosTpsControlManager} 实例。 */\n    @Override\n    public TpsControlManager buildTpsControlManager() {",
    ),
]

# --- NacosTpsControlManager ---

R["plugin-default-impl/nacos-default-control-plugin/src/main/java/com/alibaba/nacos/plugin/control/impl/NacosTpsControlManager.java"] = [
    (
        "/**\n * Nacos default control plugin implementation.\n *\n * @author shiyiyue\n */",
        "/**\n * Nacos 默认 TPS 流控管理器。\n *\n * <p>维护 TPS 点位与 {@link TpsBarrier} 映射，支持规则注册/更新；"
        " 内置定时任务每 900ms 上报通过/拒绝计数。</p>\n *\n * @author shiyiyue\n */",
    ),
    (
        "    /**\n     * point name -> tps barrier.\n     */",
        "    /** TPS 点位名称到限流屏障的映射。 */",
    ),
    (
        "    /**\n     * point name -> tps control rule.\n     */",
        "    /** TPS 点位名称到流控规则的映射。 */",
    ),
    (
        "    protected ScheduledExecutorService executorService;",
        "    /** 定时上报 TPS 指标的调度线程池。 */\n    protected ScheduledExecutorService executorService;",
    ),
    (
        "    public NacosTpsControlManager() {",
        "    /** 初始化 TPS 管理器并启动指标上报任务。 */\n    public NacosTpsControlManager() {",
    ),
    (
        "    protected void startTpsReport() {",
        "    /** 启动固定延迟的 TPS 指标上报调度。 */\n    protected void startTpsReport() {",
    ),
    (
        "    /**\n     * apple tps rule.\n     *\n     * @param pointName pointName.\n     */",
        "    /**\n     * 注册 TPS 限流点位并初始化或应用已有规则。\n     *\n     * @param pointName pointName.\n     */",
    ),
    (
        "    /**\n     * apple tps rule.\n     *\n     * @param pointName pointName.\n     * @param rule      rule.\n     */",
        "    /**\n     * 更新指定点位的 TPS 流控规则。\n     *\n     * @param pointName pointName.\n     * @param rule      rule.\n     */",
    ),
    (
        "    public Map<String, TpsBarrier> getPoints() {",
        "    /** 返回全部 TPS 点位屏障映射。 */\n    public Map<String, TpsBarrier> getPoints() {",
    ),
    (
        "    public Map<String, TpsControlRule> getRules() {",
        "    /** 返回全部 TPS 流控规则映射。 */\n    public Map<String, TpsControlRule> getRules() {",
    ),
    (
        "    /**\n     * check tps result.\n     *\n     * @param tpsRequest TpsRequest.\n     * @return check current tps is allowed.\n     */",
        "    /**\n     * 对请求执行 TPS 校验，未注册点位则跳过。\n     *\n     * @param tpsRequest TpsRequest.\n     * @return check current tps is allowed.\n     */",
    ),
    (
        "    class TpsMetricsReporter implements Runnable {",
        "    /** 定时采集各点位 TPS 通过/拒绝计数并写入日志。 */\n    class TpsMetricsReporter implements Runnable {",
    ),
    (
        "        /**\n         * get format string \"2021-01-16 17:20:21\" of timestamp.\n         *\n         * @param timeStamp timestamp milliseconds.\n         * @return\n         */",
        "        /**\n         * 将毫秒时间戳格式化为 {@code yyyy-MM-dd HH:mm:ss} 字符串。\n         *\n         * @param timeStamp timestamp milliseconds.\n         * @return\n         */",
    ),
    (
        "                        //already reported.",
        "                        // 该秒指标已上报则跳过",
    ),
    (
        "    @Override\n    public String getName() {",
        "    /** 返回插件标识 {@code nacos}。 */\n    @Override\n    public String getName() {",
    ),
]

# --- BaseConfigInfoBetaMapper ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-base/src/main/java/com/alibaba/nacos/plugin/datasource/impl/base/BaseConfigInfoBetaMapper.java"] = [
    (
        "/**\n * The base implementation of ConfigInfoBetaMapper.\n *\n * @author Long Yu\n **/",
        "/**\n * {@link ConfigInfoBetaMapper} 抽象基类。\n *\n * <p>通过 {@link DatabaseDialect} 适配分页 SQL 与数据库函数，"
        " 子类仅需声明 {@link #getDataSource()} 数据源类型。</p>\n *\n * @author Long Yu\n **/",
    ),
    (
        "    private DatabaseDialect databaseDialect;",
        "    /** 当前数据源对应的数据库方言。 */\n    private DatabaseDialect databaseDialect;",
    ),
    (
        "    public BaseConfigInfoBetaMapper() {",
        "    /** 根据数据源类型初始化方言实例。 */\n    public BaseConfigInfoBetaMapper() {",
    ),
    (
        "    @Override\n    public String getTableName() {",
        "    /** 返回 Beta 配置表名 {@link TableConstant#CONFIG_INFO_BETA}。 */\n    @Override\n    public String getTableName() {",
    ),
    (
        "    public String getLimitPageSqlWithOffset(String sql, int startRow, int pageSize) {",
        "    /** 为 SQL 追加带偏移量的分页子句。 */\n    public String getLimitPageSqlWithOffset(String sql, int startRow, int pageSize) {",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoBetaForDumpAllFetchRows(MapperContext context) {",
        "    /** 分页拉取全部 Beta 配置用于全量 dump（子查询 + 关联）。 */\n    @Override\n    public MapperResult findAllConfigInfoBetaForDumpAllFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getFunction(String functionName) {",
        "    /** 委托方言解析数据库函数名。 */\n    @Override\n    public String getFunction(String functionName) {",
    ),
]

# --- BaseConfigInfoMapper ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-base/src/main/java/com/alibaba/nacos/plugin/datasource/impl/base/BaseConfigInfoMapper.java"] = [
    (
        "/**\n * The base implementation of ConfigInfoMapper.\n *\n * @author Long Yu\n **/",
        "/**\n * {@link ConfigInfoMapper} 抽象基类。\n *\n * <p>封装 config_info 表各类分页查询、模糊搜索与变更追踪 SQL，"
        " 分页语法由 {@link DatabaseDialect} 按 MySQL/Derby 等方言改写。</p>\n *\n * @author Long Yu\n **/",
    ),
    (
        "    private DatabaseDialect databaseDialect;",
        "    /** 当前数据源的数据库方言。 */\n    private DatabaseDialect databaseDialect;",
    ),
    (
        "    public BaseConfigInfoMapper() {",
        "    /** 初始化数据库方言。 */\n    public BaseConfigInfoMapper() {",
    ),
    (
        "    public String getLimitPageSqlWithOffset(String sql, int startOffset, int pageSize) {",
        "    /** 追加 OFFSET/LIMIT 风格分页子句。 */\n    public String getLimitPageSqlWithOffset(String sql, int startOffset, int pageSize) {",
    ),
    (
        "    public String getLimitPageSqlWithMark(String sql) {",
        "    /** 追加占位符风格的分页子句（参数由调用方绑定）。 */\n    public String getLimitPageSqlWithMark(String sql) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoByAppFetchRows(MapperContext context) {",
        "    /** 按租户与应用名分页查询配置。 */\n    @Override\n    public MapperResult findConfigInfoByAppFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult getTenantIdList(MapperContext context) {",
        "    /** 分页获取非空 tenant_id 去重列表。 */\n    @Override\n    public MapperResult getTenantIdList(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult getGroupIdList(MapperContext context) {",
        "    /** 分页获取默认命名空间下的 group_id 列表。 */\n    @Override\n    public MapperResult getGroupIdList(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigKey(MapperContext context) {",
        "    /** 分页查询指定租户下的配置键（dataId/groupId/appName）。 */\n    @Override\n    public MapperResult findAllConfigKey(MapperContext context) {",
    ),
    (
        "        // fix-bug 缺失括号",
        "        // 修复子查询括号缺失问题",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoBaseFetchRows(MapperContext context) {",
        "    /** 分页拉取默认命名空间配置基础字段。 */\n    @Override\n    public MapperResult findAllConfigInfoBaseFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoFragment(MapperContext context) {",
        "    /** 按 id 游标分页拉取配置片段（可选是否含 content）。 */\n    @Override\n    public MapperResult findAllConfigInfoFragment(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findChangeConfigFetchRows(MapperContext context) {",
        "    /** 按多条件与时间范围分页查询变更配置。 */\n    @Override\n    public MapperResult findChangeConfigFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult listGroupKeyMd5ByPageFetchRows(MapperContext context) {",
        "    /** 分页返回配置的 group 键及 md5 等元数据。 */\n    @Override\n    public MapperResult listGroupKeyMd5ByPageFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoBaseLikeFetchRows(MapperContext context) {",
        "    /** 默认命名空间下按 dataId/group/content 模糊分页查询。 */\n    @Override\n    public MapperResult findConfigInfoBaseLikeFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {",
        "    /** 精确条件分页查询租户配置列表。 */\n    @Override\n    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoBaseByGroupFetchRows(MapperContext context) {",
        "    /** 按 group 与 tenant 分页查询配置内容。 */\n    @Override\n    public MapperResult findConfigInfoBaseByGroupFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {",
        "    /** 租户下多字段模糊分页查询配置。 */\n    @Override\n    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoFetchRows(MapperContext context) {",
        "    /** 分页拉取指定租户全部配置（含 content/md5）。 */\n    @Override\n    public MapperResult findAllConfigInfoFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getTableName() {",
        "    /** 返回主配置表名 {@link TableConstant#CONFIG_INFO}。 */\n    @Override\n    public String getTableName() {",
    ),
    (
        "    @Override\n    public String getFunction(String functionName) {",
        "    /** 委托方言解析数据库函数。 */\n    @Override\n    public String getFunction(String functionName) {",
    ),
]

# --- BaseConfigInfoTagMapper ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-base/src/main/java/com/alibaba/nacos/plugin/datasource/impl/base/BaseConfigInfoTagMapper.java"] = [
    (
        "/**\n * The base implementation of ConfigTagsRelationMapper.\n *\n * @author Long Yu\n **/",
        "/**\n * {@link ConfigInfoTagMapper} 抽象基类。\n *\n * <p>操作 {@code config_info_tag} 表，提供全量 dump 分页查询；"
        " 分页 SQL 由 {@link DatabaseDialect} 生成。</p>\n *\n * @author Long Yu\n **/",
    ),
    (
        "    private DatabaseDialect databaseDialect;",
        "    /** 当前数据源的数据库方言。 */\n    private DatabaseDialect databaseDialect;",
    ),
    (
        "    public BaseConfigInfoTagMapper() {",
        "    /** 初始化数据库方言。 */\n    public BaseConfigInfoTagMapper() {",
    ),
    (
        "    @Override\n    public String getTableName() {",
        "    /** 返回标签配置表名 {@link TableConstant#CONFIG_INFO_TAG}。 */\n    @Override\n    public String getTableName() {",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoTagForDumpAllFetchRows(MapperContext context) {",
        "    /** 分页拉取全部标签配置用于全量 dump。 */\n    @Override\n    public MapperResult findAllConfigInfoTagForDumpAllFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getFunction(String functionName) {",
        "    /** 委托方言解析数据库函数。 */\n    @Override\n    public String getFunction(String functionName) {",
    ),
]

# --- BaseConfigTagsRelationMapper ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-base/src/main/java/com/alibaba/nacos/plugin/datasource/impl/base/BaseConfigTagsRelationMapper.java"] = [
    (
        "/**\n * The postgresql implementation of ConfigTagsRelationMapper.\n *\n * @author Long Yu\n **/",
        "/**\n * {@link ConfigTagsRelationMapper} 抽象基类。\n *\n * <p>通过 config_info 与 config_tags_relation 左连接，"
        " 支持按标签数组精确/模糊分页查询配置。</p>\n *\n * @author Long Yu\n **/",
    ),
    (
        "    private DatabaseDialect databaseDialect;",
        "    /** 当前数据源的数据库方言。 */\n    private DatabaseDialect databaseDialect;",
    ),
    (
        "    public BaseConfigTagsRelationMapper() {",
        "    /** 初始化数据库方言。 */\n    public BaseConfigTagsRelationMapper() {",
    ),
    (
        "    public String getLimitPageSqlWithOffset(String sql, int startOffset, int pageSize) {",
        "    /** 为 SQL 追加带偏移量的分页子句。 */\n    public String getLimitPageSqlWithOffset(String sql, int startOffset, int pageSize) {",
    ),
    (
        "    @Override\n    public String getTableName() {",
        "    /** 返回标签关联表名 {@link TableConstant#CONFIG_TAGS_RELATION}。 */\n    @Override\n    public String getTableName() {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {",
        "    /** 按租户、标签等精确条件分页查询配置。 */\n    @Override\n    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {",
        "    /** 按租户、标签等模糊条件分页查询配置。 */\n    @Override\n    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getFunction(String functionName) {",
        "    /** 委托方言解析数据库函数。 */\n    @Override\n    public String getFunction(String functionName) {",
    ),
]

# --- BaseGroupCapacityMapper ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-base/src/main/java/com/alibaba/nacos/plugin/datasource/impl/base/BaseGroupCapacityMapper.java"] = [
    (
        "/**\n * The base implementation of GroupCapacityMapper.\n *\n * @author Long Yu\n **/",
        "/**\n * {@link GroupCapacityMapper} 抽象基类。\n *\n * <p>操作 group_capacity 表，提供按 id 游标分页查询分组容量信息，"
        " Top-N SQL 由 {@link DatabaseDialect} 适配。</p>\n *\n * @author Long Yu\n **/",
    ),
    (
        "    private DatabaseDialect databaseDialect;",
        "    /** 当前数据源的数据库方言。 */\n    private DatabaseDialect databaseDialect;",
    ),
    (
        "    public BaseGroupCapacityMapper() {",
        "    /** 初始化数据库方言。 */\n    public BaseGroupCapacityMapper() {",
    ),
    (
        "    @Override\n    public MapperResult selectGroupInfoBySize(MapperContext context) {",
        "    /** 按 id 游标分页查询 group_id 列表（用于容量校正）。 */\n    @Override\n    public MapperResult selectGroupInfoBySize(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getFunction(String functionName) {",
        "    /** 委托方言解析数据库函数。 */\n    @Override\n    public String getFunction(String functionName) {",
    ),
]

# --- BaseTenantCapacityMapper ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-base/src/main/java/com/alibaba/nacos/plugin/datasource/impl/base/BaseTenantCapacityMapper.java"] = [
    (
        "/**\n * The base implementation of TenantCapacityMapper.\n *\n * @author Long Yu\n **/",
        "/**\n * {@link TenantCapacityMapper} 抽象基类。\n *\n * <p>操作 tenant_capacity 表，分页拉取租户容量记录用于用量校正；"
        " 分页语法委托 {@link DatabaseDialect}。</p>\n *\n * @author Long Yu\n **/",
    ),
    (
        "    private DatabaseDialect databaseDialect;",
        "    /** 当前数据源的数据库方言。 */\n    private DatabaseDialect databaseDialect;",
    ),
    (
        "    public BaseTenantCapacityMapper() {",
        "    /** 初始化数据库方言。 */\n    public BaseTenantCapacityMapper() {",
    ),
    (
        "    @Override\n    public MapperResult getCapacityList4CorrectUsage(MapperContext context) {",
        "    /** 按 id 游标分页查询租户容量列表（校正用量）。 */\n    @Override\n    public MapperResult getCapacityList4CorrectUsage(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getFunction(String functionName) {",
        "    /** 委托方言解析数据库函数。 */\n    @Override\n    public String getFunction(String functionName) {",
    ),
]

# --- BaseTenantInfoMapper ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-base/src/main/java/com/alibaba/nacos/plugin/datasource/impl/base/BaseTenantInfoMapper.java"] = [
    (
        "/**\n * The base implementation of TenantInfo.\n *\n * @author Long Yu\n **/",
        "/**\n * {@link TenantInfoMapper} 抽象基类。\n *\n * <p>租户信息 Mapper 的方言适配基类，"
        " 子类继承 {@link AbstractMapper} 并实现具体 SQL；本类仅封装 {@link DatabaseDialect} 函数解析。</p>\n *\n * @author Long Yu\n **/",
    ),
    (
        "    private DatabaseDialect databaseDialect;",
        "    /** 当前数据源的数据库方言。 */\n    private DatabaseDialect databaseDialect;",
    ),
    (
        "    public BaseTenantInfoMapper() {",
        "    /** 根据数据源类型初始化方言。 */\n    public BaseTenantInfoMapper() {",
    ),
    (
        "    @Override\n    public String getFunction(String functionName) {",
        "    /** 委托方言将逻辑函数名映射为数据库原生函数。 */\n    @Override\n    public String getFunction(String functionName) {",
    ),
]
