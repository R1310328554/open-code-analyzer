"""Chinese annotation replacements for Nacos 3.2.3 wave72b [15:30] persistence utils + prometheus/server."""

R: dict[str, list[tuple[str, str]]] = {}

# --- DerbyUtils ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/utils/DerbyUtils.java"] = [
    (
        "/**\n * Derby util.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * Derby 嵌入式数据库 SQL 工具类。\n *\n * <p>Derby 表名默认大写，外部 MySQL 等库的 INSERT 语句导入 Derby 时需做大小写与反引号转换。</p>\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    private static final String INSERT_INTO_VALUES = \"(INSERT INTO .+? VALUES)\";",
        "    /** 匹配 INSERT INTO ... VALUES 片段的正则表达式。 */\n    private static final String INSERT_INTO_VALUES = \"(INSERT INTO .+? VALUES)\";",
    ),
    (
        "    private static final Pattern INSERT_INTO_PATTERN = Pattern.compile(INSERT_INTO_VALUES);",
        "    /** 预编译的 INSERT 语句匹配模式。 */\n    private static final Pattern INSERT_INTO_PATTERN = Pattern.compile(INSERT_INTO_VALUES);",
    ),
    (
        "    /**\n     * Because Derby's database table name is uppercase, you need to do a conversion to the insert statement that was\n     * inserted.\n     *\n     * @param sql external database insert sql\n     * @return derby insert sql\n     */",
        "    /**\n     * 将外部库的 INSERT 语句转换为 Derby 兼容格式。\n     *\n     * <p>将 INSERT INTO 段转为大写并去除反引号，同时去掉末尾分号。</p>\n     *\n     * @param sql 外部数据库原始 INSERT SQL\n     * @return 适配 Derby 的 INSERT SQL\n     */",
    ),
    (
        "    public static String insertStatementCorrection(String sql) {",
        "    /** 执行 INSERT 语句校正，无匹配时原样返回。 */\n    public static String insertStatementCorrection(String sql) {",
    ),
]

# --- PersistenceExecutor ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/utils/PersistenceExecutor.java"] = [
    (
        "/**\n * Persistence async task executors.\n *\n * @author xiweng.yy\n */",
        "/**\n * 持久化模块异步任务线程池封装。\n *\n * <p>提供定时任务、嵌入式数据 dump 与快照三类专用 {@link Executor}，"
        " 由 {@link ExecutorFactory.Managed} 统一管理生命周期。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private static final ScheduledExecutorService TIMER_EXECUTOR = ExecutorFactory.Managed",
        "    /** 持久化定时任务调度器（双线程）。 */\n    private static final ScheduledExecutorService TIMER_EXECUTOR = ExecutorFactory.Managed",
    ),
    (
        "    private static final Executor DUMP_EXECUTOR = ExecutorFactory.Managed",
        "    /** 嵌入式存储 dump 单线程执行器。 */\n    private static final Executor DUMP_EXECUTOR = ExecutorFactory.Managed",
    ),
    (
        "    private static final ExecutorService EMBEDDED_SNAPSHOT_EXECUTOR = ExecutorFactory.Managed",
        "    /** 嵌入式快照写入单线程执行器。 */\n    private static final ExecutorService EMBEDDED_SNAPSHOT_EXECUTOR = ExecutorFactory.Managed",
    ),
    (
        "    public static void scheduleTask(Runnable command, long initialDelay, long delay,\n        TimeUnit unit) {",
        "    /** 以固定延迟周期调度持久化定时任务。 */\n    public static void scheduleTask(Runnable command, long initialDelay, long delay,\n        TimeUnit unit) {",
    ),
    (
        "    public static void executeEmbeddedDump(Runnable runnable) {",
        "    /** 在 dump 线程池中异步执行嵌入式数据导出任务。 */\n    public static void executeEmbeddedDump(Runnable runnable) {",
    ),
    (
        "    public static void executeSnapshot(Runnable runnable) {",
        "    /** 在快照线程池中异步执行嵌入式存储快照任务。 */\n    public static void executeSnapshot(Runnable runnable) {",
    ),
]

# --- PrometheusApp ---

R["prometheus/src/main/java/com/alibaba/nacos/prometheus/PrometheusApp.java"] = [
    (
        "/**\n * PrometheusApp starter.\n *\n * @author karsonto\n */",
        "/**\n * Prometheus 指标模块独立启动入口。\n *\n * <p>扫描 {@code com.alibaba.nacos} 包并启用定时任务，"
        " 用于单独部署 Prometheus 服务发现 HTTP 端点。</p>\n *\n * @author karsonto\n */",
    ),
    (
        "    public static void main(String[] args) {",
        "    /** 启动 Prometheus 模块 Spring Boot 应用。 */\n    public static void main(String[] args) {",
    ),
]

# --- ApiConstants ---

R["prometheus/src/main/java/com/alibaba/nacos/prometheus/api/ApiConstants.java"] = [
    (
        "/**\n * Api Constants.\n *\n * @author karsonto\n */",
        "/**\n * Prometheus 服务发现 REST API 路径常量。\n *\n * <p>定义全局、按命名空间、按服务三种 metrics 拉取 URL 模板，"
        " 供 {@link com.alibaba.nacos.prometheus.controller.PrometheusController} 与安全配置引用。</p>\n *\n * @author karsonto\n */",
    ),
    (
        "    public static final String PROMETHEUS_CONTROLLER_PATH = \"/prometheus\";",
        "    /** 全量实例 Prometheus SD 接口根路径。 */\n    public static final String PROMETHEUS_CONTROLLER_PATH = \"/prometheus\";",
    ),
    (
        "    public static final String PROMETHEUS_CONTROLLER_NAMESPACE_PATH =",
        "    /** 按命名空间过滤的 metrics 路径模板。 */\n    public static final String PROMETHEUS_CONTROLLER_NAMESPACE_PATH =",
    ),
    (
        "    public static final String PROMETHEUS_CONTROLLER_SERVICE_PATH =",
        "    /** 按命名空间与服务名过滤的 metrics 路径模板。 */\n    public static final String PROMETHEUS_CONTROLLER_SERVICE_PATH =",
    ),
]

# --- PrometheusSecurityConfiguration ---

R["prometheus/src/main/java/com/alibaba/nacos/prometheus/conf/PrometheusSecurityConfiguration.java"] = [
    (
        "/**\n * prometheus auth configuration, avoid spring security configuration override.\n *\n * @author vividfish\n */",
        "/**\n * Prometheus 端点 Spring Security 配置。\n *\n * <p>在未启用 Nacos 认证插件时，将 Prometheus SD 路径设为 {@code permitAll}，"
        " 避免全局 Security 拦截 metrics 拉取；启用认证时由 {@link com.alibaba.nacos.prometheus.filter.PrometheusAuthFilter} 处理。</p>\n *\n * @author vividfish\n */",
    ),
    (
        "    @Bean\n    @Conditional(ConditionOnNoAuthPluginType.class)\n    public SecurityFilterChain prometheusSecurityFilterChain(HttpSecurity http) throws Exception {",
        "    /** 无认证插件时对 Prometheus 路径放行。 */\n    @Bean\n    @Conditional(ConditionOnNoAuthPluginType.class)\n    public SecurityFilterChain prometheusSecurityFilterChain(HttpSecurity http) throws Exception {",
    ),
    (
        "    private static class ConditionOnNoAuthPluginType implements Condition {",
        "    /** 当 {@code nacos.core.auth.system.type} 为空时匹配（未配置认证插件）。 */\n    private static class ConditionOnNoAuthPluginType implements Condition {",
    ),
    (
        "        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {",
        "        /** 检测环境是否未指定 Nacos 认证系统类型。 */\n        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {",
    ),
]

# --- PrometheusController ---

R["prometheus/src/main/java/com/alibaba/nacos/prometheus/controller/PrometheusController.java"] = [
    (
        "/**\n * Support Prometheus SD Controller.\n *\n * @author karsonto\n */",
        "/**\n * Prometheus 服务发现（HTTP SD）REST 控制器。\n *\n * <p>暴露 JSON 格式的 targets/labels 列表，供 Prometheus 抓取 Nacos 注册实例；"
        " 支持全量、按命名空间、按服务三种粒度查询。</p>\n *\n * @author karsonto\n */",
    ),
    (
        "    @Autowired\n    private InstanceOperatorClientImpl instanceServiceV2;",
        "    /** 命名服务实例查询客户端（V2）。 */\n    @Autowired\n    private InstanceOperatorClientImpl instanceServiceV2;",
    ),
    (
        "    private final ServiceManager serviceManager;",
        "    /** 命名空间与服务元数据管理器。 */\n    private final ServiceManager serviceManager;",
    ),
    (
        "    public PrometheusController() {",
        "    /** 初始化并绑定 {@link ServiceManager} 单例。 */\n    public PrometheusController() {",
    ),
    (
        "    /**\n     * Get all service instances.\n     *\n     * @throws NacosException NacosException.\n     */",
        "    /**\n     * 返回所有命名空间下全部健康实例的 Prometheus SD JSON。\n     *\n     * @throws NacosException 实例列表查询失败时抛出\n     */",
    ),
    (
        "    public ResponseEntity<String> metric() throws NacosException {",
        "    /** 遍历全部命名空间与服务，组装 targets 数组。 */\n    public ResponseEntity<String> metric() throws NacosException {",
    ),
    (
        "    /**\n     * Get service instances from designated namespace.\n     *\n     * @throws NacosException NacosException.\n     */",
        "    /**\n     * 返回指定命名空间内全部实例的 Prometheus SD JSON。\n     *\n     * @throws NacosException 实例列表查询失败时抛出\n     */",
    ),
    (
        "    public ResponseEntity<String> metricNamespace(@PathVariable(\"namespaceId\") String namespaceId)",
        "    /** 按 namespaceId 过滤服务后返回 SD JSON。 */\n    public ResponseEntity<String> metricNamespace(@PathVariable(\"namespaceId\") String namespaceId)",
    ),
    (
        "    /**\n     * Get service instances from designated namespace and service.\n     *\n     * @throws NacosException NacosException.\n     */",
        "    /**\n     * 返回指定命名空间与单个服务下实例的 Prometheus SD JSON。\n     *\n     * @throws NacosException 实例列表查询失败时抛出\n     */",
    ),
    (
        "    public ResponseEntity<String> metricNamespaceService(",
        "    /** 按 namespaceId 与 service 名精确过滤实例。 */\n    public ResponseEntity<String> metricNamespaceService(",
    ),
    (
        "    private ArrayNode getServiceArrayNode(String namespaceId, Predicate<Service> serviceFilter)",
        "    /** 内部方法：按命名空间与服务谓词组装 SD 数组节点。 */\n    private ArrayNode getServiceArrayNode(String namespaceId, Predicate<Service> serviceFilter)",
    ),
]

# --- PrometheusApiExceptionHandler ---

R["prometheus/src/main/java/com/alibaba/nacos/prometheus/exception/PrometheusApiExceptionHandler.java"] = [
    (
        "/**\n * Exception Handler for Prometheus API.\n *\n * @author karsonto\n * @date 2023/02/01\n */",
        "/**\n * Prometheus REST API 全局异常处理器。\n *\n * <p>仅作用于 {@code com.alibaba.nacos.prometheus.controller} 包，"
        " 将 {@link NacosException} 与 {@link NacosRuntimeException} 转为统一 {@link Result} JSON 响应。</p>\n *\n * @author karsonto\n * @date 2023/02/01\n */",
    ),
    (
        "    private static final Logger LOGGER =",
        "    /** 异常日志记录器。 */\n    private static final Logger LOGGER =",
    ),
    (
        "    @ExceptionHandler(NacosException.class)\n    public ResponseEntity<Result<String>> handleNacosException(NacosException e) {",
        "    /** 处理受检 NacosException，返回 500 与错误消息。 */\n    @ExceptionHandler(NacosException.class)\n    public ResponseEntity<Result<String>> handleNacosException(NacosException e) {",
    ),
    (
        "    @ExceptionHandler(NacosRuntimeException.class)\n    public ResponseEntity<Result<String>> handleNacosRuntimeException(NacosRuntimeException e) {",
        "    /** 处理运行时 NacosRuntimeException，按错误码返回 HTTP 状态。 */\n    @ExceptionHandler(NacosRuntimeException.class)\n    public ResponseEntity<Result<String>> handleNacosRuntimeException(NacosRuntimeException e) {",
    ),
]

# --- PrometheusAuthFilter ---

R["prometheus/src/main/java/com/alibaba/nacos/prometheus/filter/PrometheusAuthFilter.java"] = [
    (
        "/**\n * prometheus auth configuration.\n *\n * @author vividfish\n */",
        "/**\n * Prometheus 端点 HTTP Basic 认证过滤器配置。\n *\n * <p>在 {@code nacos.core.auth.enabled=true} 且存在 {@link PrometheusController} 时注册"
        " Basic、匿名、授权与异常转换过滤器链，仅作用于 {@code /prometheus} 路径。</p>\n *\n * @author vividfish\n */",
    ),
    (
        "    @Bean\n    public AuthenticationManager authenticationManager(HttpSecurity http,",
        "    /** 构建基于 UserDetailsService 的 AuthenticationManager。 */\n    @Bean\n    public AuthenticationManager authenticationManager(HttpSecurity http,",
    ),
    (
        "    @Bean\n    public FilterRegistrationBean<BasicAuthenticationFilter> basicAuthenticationFilter(",
        "    /** 注册 Basic 认证过滤器，顺序为 2。 */\n    @Bean\n    public FilterRegistrationBean<BasicAuthenticationFilter> basicAuthenticationFilter(",
    ),
    (
        "    @Bean\n    public FilterRegistrationBean<AnonymousAuthenticationFilter> anonymousAuthenticationFilter() {",
        "    /** 注册匿名认证过滤器，顺序为 3。 */\n    @Bean\n    public FilterRegistrationBean<AnonymousAuthenticationFilter> anonymousAuthenticationFilter() {",
    ),
    (
        "    @Bean\n    public FilterRegistrationBean<AuthorizationFilter> authorizationFilter() {",
        "    /** 注册已认证用户授权过滤器，顺序为 4。 */\n    @Bean\n    public FilterRegistrationBean<AuthorizationFilter> authorizationFilter() {",
    ),
    (
        "    @Bean\n    public FilterRegistrationBean<ExceptionTranslationFilter> exceptionTranslationFilter() {",
        "    /** 注册认证/授权异常转 403 的过滤器，顺序为 1。 */\n    @Bean\n    public FilterRegistrationBean<ExceptionTranslationFilter> exceptionTranslationFilter() {",
    ),
]

# --- PrometheusUtils ---

R["prometheus/src/main/java/com/alibaba/nacos/prometheus/utils/PrometheusUtils.java"] = [
    (
        "/**\n * prometheus common utils.\n *\n * @author Joey777210\n */",
        "/**\n * Prometheus 服务发现 JSON 组装工具。\n *\n * <p>将 Nacos {@link Instance} 集合转为 Prometheus HTTP SD 所需的"
        " {@code targets} 与 {@code labels} 结构，并规范化 metadata 标签名。</p>\n *\n * @author Joey777210\n */",
    ),
    (
        "    /**\n     * Assemble arrayNodes for prometheus sd api.\n     */",
        "    /**\n     * 按集群名分组实例并追加到 SD 数组节点。\n     */",
    ),
    (
        "    public static void assembleArrayNodes(Set<Instance> targetSet, ArrayNode arrayNode) {",
        "    /** 遍历分组后的实例，逐个写入 arrayNode。 */\n    public static void assembleArrayNodes(Set<Instance> targetSet, ArrayNode arrayNode) {",
    ),
    (
        "    /**\n     * assemble instance to json node, and export metadata to label.\n     *\n     * @param clusterName the cluster name\n     * @param instance    instance info\n     */",
        "    /**\n     * 将单个实例转为 Prometheus SD 条目 JSON。\n     *\n     * <p>targets 为 ip:port，labels 含集群名与实例 metadata（键名中 . 与 - 转为 _）。</p>\n     *\n     * @param clusterName 集群名称\n     * @param instance 实例信息\n     */",
    ),
    (
        "        //mark cluster name",
        "        // 写入 __meta_clusterName 标签",
    ),
    (
        "        //export metadata",
        "        // 导出实例 metadata 为 Prometheus labels",
    ),
    (
        "        // auto convert label names contain with \".\" and \"-\" to \"_\"",
        "        // 标签名中的点与横线自动替换为下划线",
    ),
]

# --- NacosServerBasicApplication ---

R["server/src/main/java/com/alibaba/nacos/NacosServerBasicApplication.java"] = [
    (
        "/**\n * Nacos Server basic starter class, which load common non-web container beans.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos Server 基础进程启动类（非 Web 容器 Bean）。\n *\n * <p>通过 {@link NacosWebBeanTypeFilter} 排除 Web 相关 Bean，"
        " 加载 core、naming、config 等后台服务组件；与 {@link NacosServerWebApplication} 拆分部署时使用。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    public static void main(String[] args) {",
        "    /** 启动 Nacos 基础 Spring Boot 应用。 */\n    public static void main(String[] args) {",
    ),
]

# --- NacosServerWebApplication ---

R["server/src/main/java/com/alibaba/nacos/NacosServerWebApplication.java"] = [
    (
        "/**\n * Nacos Server web starter class, which load non-console web container beans.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos Server Web 进程启动类（非 Console Web Bean）。\n *\n * <p>通过 {@link NacosNormalBeanTypeFilter} 排除普通业务 Bean，"
        " 仅加载 REST API、Prometheus 等 Web 端点；加载 {@code nacos-server.properties} 配置。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    public static void main(String[] args) {",
        "    /** 启动 Nacos Web Spring Boot 应用。 */\n    public static void main(String[] args) {",
    ),
]

# --- AbstractNacosWebBeanTypeFilter ---

R["server/src/main/java/com/alibaba/nacos/server/AbstractNacosWebBeanTypeFilter.java"] = [
    (
        "/**\n * Abstract TypeFilter to filter Nacos Web Bean or not.\n *\n * @author xiweng.yy\n */",
        "/**\n * 判断类是否为 Nacos Web Bean 的抽象 {@link TypeFilter}。\n *\n * <p>识别 {@link RestController}、{@link ControllerAdvice}、{@link Controller} 与"
        " {@link NacosWebBean} 注解，供 Basic/Web 双进程组件扫描互斥使用。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private static final Set<String> WEB_BEAN_ANNOTATIONS = new HashSet<>();",
        "    /** Web Bean 判定所用的注解类全名集合。 */\n    private static final Set<String> WEB_BEAN_ANNOTATIONS = new HashSet<>();",
    ),
    (
        "    static {",
        "    /** 静态初始化 Web 相关注解类名。 */\n    static {",
    ),
    (
        "    protected boolean isWebBean(MetadataReader metadataReader,",
        "    /** 若类携带任一 Web 注解则返回 true。 */\n    protected boolean isWebBean(MetadataReader metadataReader,",
    ),
]

# --- NacosNormalBeanTypeFilter ---

R["server/src/main/java/com/alibaba/nacos/server/NacosNormalBeanTypeFilter.java"] = [
    (
        "/**\n * TypeFilter to filter beans which is Nacos Web.\n *\n * @author xiweng.yy\n */",
        "/**\n * 排除 Web Bean 的组件扫描过滤器。\n *\n * <p>用于 {@link NacosServerWebApplication}：匹配非 Web Bean（{@code !isWebBean}），"
        " 使 Web 进程不加载后台-only 组件。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)",
        "    /** 非 Web Bean 时返回 true，允许扫描注册。 */\n    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)",
    ),
]

# --- NacosWebBeanPostProcessorConfiguration ---

R["server/src/main/java/com/alibaba/nacos/server/NacosWebBeanPostProcessorConfiguration.java"] = [
    (
        "/**\n * Bean Post Processor Configuration for nacos web server.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos Web 进程 Bean 后处理器配置。\n *\n * <p>在启用 duplicate bean 增强时注册 Spring Bean 与 Configuration 重复定义检测后处理器，"
        " 避免 Basic/Web 双进程同 classpath 下 Bean 冲突。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    @Bean\n    public InstantiationAwareBeanPostProcessor nacosDuplicateSpringBeanPostProcessor(",
        "    /** 注册普通 Spring Bean 重复定义检测后处理器。 */\n    @Bean\n    public InstantiationAwareBeanPostProcessor nacosDuplicateSpringBeanPostProcessor(",
    ),
    (
        "    @Bean\n    public InstantiationAwareBeanPostProcessor nacosDuplicateConfigurationBeanPostProcessor(",
        "    /** 注册 {@code @Configuration} 类重复定义检测后处理器。 */\n    @Bean\n    public InstantiationAwareBeanPostProcessor nacosDuplicateConfigurationBeanPostProcessor(",
    ),
]

# --- NacosWebBeanTypeFilter ---

R["server/src/main/java/com/alibaba/nacos/server/NacosWebBeanTypeFilter.java"] = [
    (
        "/**\n * TypeFilter to filter beans which is Nacos Web.\n *\n * @author xiweng.yy\n */",
        "/**\n * 仅保留 Web Bean 的组件扫描过滤器。\n *\n * <p>用于 {@link NacosServerBasicApplication}：匹配 Web Bean（{@code isWebBean}），"
        " 在 excludeFilters 中排除，使基础进程不加载 REST 控制器等 Web 组件。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)",
        "    /** Web Bean 时返回 true，配合 excludeFilter 排除注册。 */\n    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)",
    ),
]
