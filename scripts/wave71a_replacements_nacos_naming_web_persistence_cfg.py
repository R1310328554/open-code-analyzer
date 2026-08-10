"""Chinese annotation replacements for Nacos 3.2.3 wave71a [0:15] naming utils/web + persistence cfg."""

R: dict[str, list[tuple[str, str]]] = {}

# --- NamingRequestUtil ---

R["naming/src/main/java/com/alibaba/nacos/naming/utils/NamingRequestUtil.java"] = [
    (
        "/**\n * Naming request util.\n *\n * @author xiweng.yy\n */",
        "/**\n * 命名服务请求工具类。\n *\n"
        " * <p>从 {@link RequestContextHolder} 或 HTTP/gRPC 请求中提取客户端源 IP，"
        "并校验实例注册权重是否在合法区间内。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    /**\n     * Get source ip from request context.\n     *\n"
        "     * @return source ip, null if not found\n     */",
        "    /**\n     * 从请求上下文中获取客户端源 IP。\n     *\n"
        "     * <p>优先取 {@link AddressContext#getSourceIp()}，为空时回退到 remoteIp。</p>\n     *\n"
        "     * @return source ip, null if not found\n     */",
    ),
    (
        "    /**\n     * Get source ip from request context first, if it can't found, get from http request.\n     *\n"
        "     * @param httpServletRequest http request\n     * @return source ip, null if not found\n     */",
        "    /**\n     * 获取 HTTP 请求的客户端源 IP。\n     *\n"
        "     * <p>上下文无 IP 时通过 {@link WebUtils#getRemoteIp} 从 Servlet 请求解析。</p>\n     *\n"
        "     * @param httpServletRequest http request\n     * @return source ip, null if not found\n     */",
    ),
    (
        "        // If can't get from request context, get from http request.",
        "        // 上下文未携带 IP 时，从 HTTP 请求头/连接信息解析。",
    ),
    (
        "    /**\n     * Get source ip from request context first, if it can't found, get from http request.\n     *\n"
        "     * @param meta grpc request meta\n     * @return source ip, null if not found\n     */",
        "    /**\n     * 获取 gRPC 请求的客户端源 IP。\n     *\n"
        "     * <p>上下文无 IP 时使用 {@link RequestMeta#getClientIp()}。</p>\n     *\n"
        "     * @param meta grpc request meta\n     * @return source ip, null if not found\n     */",
    ),
    (
        "        // If can't get from request context, get from grpc request meta.",
        "        // 上下文未携带 IP 时，从 gRPC RequestMeta 读取 clientIp。",
    ),
    (
        "    /**\n     * Check request weight is validate.\n     *\n"
        "     * @param weight weight from request\n     * @throws NacosException if weight is invalid\n     */",
        "    /**\n     * 校验实例权重是否在允许范围内。\n     *\n"
        "     * <p>超出 {@link com.alibaba.nacos.naming.constants.Constants} 定义的最小/最大权重时抛出 "
        "{@link NacosApiException}。</p>\n     *\n"
        "     * @param weight weight from request\n     * @throws NacosException if weight is invalid\n     */",
    ),
]

# --- ServiceUtil ---

R["naming/src/main/java/com/alibaba/nacos/naming/utils/ServiceUtil.java"] = [
    (
        "/**\n * Service util.\n *\n * @author xiweng.yy\n */",
        "/**\n * 命名服务工具类。\n *\n"
        " * <p>提供服务详情转换、服务名分页、实例筛选及健康保护阈值逻辑，"
        "供订阅推送与 OpenAPI 查询复用。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    /**\n     * TODO removed after console controller and console-ui support use ServiceDetailInfo directly.\n     *\n"
        "     * @param serviceDetailInfo serviceDetailInfo\n     * @return old console ui custom result\n     */",
        "    /**\n     * 将 {@link ServiceDetailInfo} 转为旧版控制台 JSON 结构。\n     *\n"
        "     * <p>TODO：控制台全面迁移后可删除。</p>\n     *\n"
        "     * @param serviceDetailInfo serviceDetailInfo\n     * @return old console ui custom result\n     */",
    ),
    (
        "    /**\n     * Page service name.\n     *\n"
        "     * @param pageNo         page number\n     * @param pageSize       size per page\n"
        "     * @param serviceNameSet service name set\n     * @return service name list by paged\n     */",
        "    /**\n     * 对服务名集合分页并去掉 group@@ 前缀。\n     *\n"
        "     * @param pageNo         page number\n     * @param pageSize       size per page\n"
        "     * @param serviceNameSet service name set\n     * @return service name list by paged\n     */",
    ),
    (
        "    /**\n     * Select healthy instance of service info.\n     *\n"
        "     * @param serviceInfo original service info\n     * @return new service info\n     */",
        "    /** 仅保留健康实例，返回新的 {@link ServiceInfo} 副本。 */",
    ),
    (
        "    public static ServiceInfo selectEnabledInstances(ServiceInfo serviceInfo) {",
        "    /** 仅保留 enabled 实例。 */\n"
        "    public static ServiceInfo selectEnabledInstances(ServiceInfo serviceInfo) {",
    ),
    (
        "    public static ServiceInfo selectInstances(ServiceInfo serviceInfo, String cluster) {",
        "    /** 按集群名筛选实例。 */\n"
        "    public static ServiceInfo selectInstances(ServiceInfo serviceInfo, String cluster) {",
    ),
    (
        "            // filter ips using selector",
        "            // 使用 Selector 按订阅者 IP 进一步过滤实例列表",
    ),
    (
        "            // will re-compute healthCount",
        "            // 过滤后若实例数变化，需重新统计健康实例数",
    ),
    (
        "                                // set all to `healthy` state to protect",
        "                                // 健康保护：将不健康实例标记为 healthy 避免全部被摘除",
    ),
    (
        "        // The instance list won't be modified almost time.",
        "        // 返回副本中的 hosts 列表，通常不修改原始 ServiceInfo",
    ),
    (
        "        // The instance list of all filtered by cluster/enabled condition.",
        "        // 记录经 cluster/enabled 过滤后的全部实例，供健康保护计算比例",
    ),
    (
        "        /**\n         * Do customized filtering.\n         *\n"
        "         * @param filteredResult result with instances already been filtered cluster/enabled/healthy\n"
        "         * @param allInstances   all instances filtered by cluster/enabled\n"
        "         * @param healthyCount   healthy instances count filtered by cluster/enabled\n         */",
        "        /**\n         * 自定义实例过滤回调（如 Selector 与健康保护）。\n         *\n"
        "         * @param filteredResult result with instances already been filtered cluster/enabled/healthy\n"
        "         * @param allInstances   all instances filtered by cluster/enabled\n"
        "         * @param healthyCount   healthy instances count filtered by cluster/enabled\n         */",
    ),
]

# --- CanDistro ---

R["naming/src/main/java/com/alibaba/nacos/naming/web/CanDistro.java"] = [
    (
        "/**\n * Annotation to determine if method should be redirected.\n *\n * @author nkorange\n * @since 1.0.0\n */",
        "/**\n * 标记 Controller 方法是否参与 Distro 分区路由。\n *\n"
        " * <p>标注后由 {@link DistroFilter} 判断本节点是否负责该请求，"
        "否则代理到负责节点。</p>\n *\n * @author nkorange\n * @since 1.0.0\n */",
    ),
]

# --- ClientAttributesFilter ---

R["naming/src/main/java/com/alibaba/nacos/naming/web/ClientAttributesFilter.java"] = [
    (
        "/**\n * <p>\n * collect client attributes for 1.x.\n * </p>\n *\n * @author hujun\n */",
        "/**\n * 1.x 客户端属性采集过滤器。\n *\n"
        " * <p>在实例注册与心跳请求中收集版本、应用名、客户端 IP 等属性，"
        "写入 {@link RequestContextHolder} 或更新 {@link IpPortBasedClient}。</p>\n *\n"
        " * @author hujun\n */",
    ),
    (
        "    public static Optional<ClientAttributes> getCurrentClientAttributes() {",
        "    /** 从请求扩展上下文读取当前客户端属性。 */\n"
        "    public static Optional<ClientAttributes> getCurrentClientAttributes() {",
    ),
    (
        "                //register",
        "                // 注册实例：将客户端属性放入请求扩展上下文",
    ),
    (
        "                //beat",
        "                // 心跳：若客户端尚未记录版本等属性则补写",
    ),
    (
        "                    //update clientAttributes,when client version attributes is null,then update.",
        "                    // 仅当本地客户端缺少版本属性时才用本次请求属性更新",
    ),
    (
        "    private boolean isBeatUri(String uri, String httpMethod) {",
        "    /** 判断是否为 v1/v2 实例心跳 PUT 接口。 */\n"
        "    private boolean isBeatUri(String uri, String httpMethod) {",
    ),
    (
        "    private boolean isRegisterInstanceUri(String uri, String httpMethod) {",
        "    /** 判断是否为 v1/v2 实例注册 POST 接口。 */\n"
        "    private boolean isRegisterInstanceUri(String uri, String httpMethod) {",
    ),
    (
        "    private boolean canUpdateClientAttributes(IpPortBasedClient client,\n"
        "        ClientAttributes requestClientAttributes) {",
        "    /** 请求带版本且客户端尚未持久化版本时才允许更新属性。 */\n"
        "    private boolean canUpdateClientAttributes(IpPortBasedClient client,\n"
        "        ClientAttributes requestClientAttributes) {",
    ),
    (
        "    private ClientAttributes getClientAttributes() {",
        "    /** 从 BasicContext 组装 User-Agent、App、源 IP 等客户端属性。 */\n"
        "    private ClientAttributes getClientAttributes() {",
    ),
]

# --- DistroFilter ---

R["naming/src/main/java/com/alibaba/nacos/naming/web/DistroFilter.java"] = [
    (
        "/**\n * Distro filter.\n *\n * @author nacos\n */",
        "/**\n * Distro 一致性分区 HTTP 过滤器。\n *\n"
        " * <p>对标注 {@link CanDistro} 的接口按 {@link DistroTagGenerator} 计算责任标签，"
        "非本节点负责时将请求代理到 {@link DistroMapper} 映射的目标服务器。</p>\n *\n"
        " * @author nacos\n */",
    ),
    (
        "            // proxy request to other server if necessary:",
        "            // 本节点非责任方：将请求代理到集群内负责该 tag 的节点",
    ),
    (
        "                // This request is sent from peer server, should not be redirected again:",
        "                // 来自对等 Nacos 节点的重定向请求，禁止再次转发以防环路",
    ),
]

# --- DistroIpPortTagGenerator ---

R["naming/src/main/java/com/alibaba/nacos/naming/web/DistroIpPortTagGenerator.java"] = [
    (
        "/**\n * Distro IP and port tag generator.\n *\n * @author xiweng.yy\n */",
        "/**\n * 基于实例 IP:Port 的 Distro 责任标签生成器。\n *\n"
        " * <p>从请求参数 ip/port 或旧版 beat JSON 解析 {@link RsInfo}，"
        "生成 {@code ip:port} 形式的分区键。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "            // some old version clients using beat parameter",
        "            // 兼容旧客户端：ip 为空时从 beat 参数 JSON 解析 IP 与端口",
    ),
    (
        "    @Override\n    public String getResponsibleTag(ReuseHttpServletRequest request) {",
        "    /** 解析请求参数并返回 ip:port 责任标签，缺省端口为 0。 */\n"
        "    @Override\n    public String getResponsibleTag(ReuseHttpServletRequest request) {",
    ),
]

# --- DistroTagGenerator ---

R["naming/src/main/java/com/alibaba/nacos/naming/web/DistroTagGenerator.java"] = [
    (
        "/**\n * Distro tag generator.\n *\n * @author xiweng.yy\n */",
        "/**\n * Distro 责任标签生成器接口。\n *\n"
        " * <p>由 {@link DistroFilter} 调用，根据 HTTP 请求内容计算一致性哈希分区键。</p>\n *\n"
        " * @author xiweng.yy\n */",
    ),
    (
        "    /**\n     * Get responsible tag from http request.\n     *\n"
        "     * @param request http request.\n     * @return responsible tag for distro.\n     */",
        "    /**\n     * 从 HTTP 请求提取 Distro 责任标签。\n     *\n"
        "     * @param request http request.\n     * @return responsible tag for distro.\n     */",
    ),
]

# --- DistroTagGeneratorImpl ---

R["naming/src/main/java/com/alibaba/nacos/naming/web/DistroTagGeneratorImpl.java"] = [
    (
        "/**\n * Distro tag generator.\n *\n * @author xiweng.yy\n */",
        "/**\n * Distro 标签生成器 Spring 默认实现。\n *\n"
        " * <p>当前集群统一使用 {@link DistroIpPortTagGenerator}；"
        "预留按成员版本切换策略的扩展点。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    /**\n     * Get tag generator according to cluster member ability.\n     *\n     * <p>\n"
        "     * If all member is upper than 2.x. Using {@link DistroIpPortTagGenerator}.\n     * </p>\n     *\n"
        "     * @return actual tag generator\n     */",
        "    /**\n     * 按集群成员能力选择具体标签生成策略。\n     *\n"
        "     * <p>成员均为 2.x 及以上时使用 {@link DistroIpPortTagGenerator}。</p>\n     *\n"
        "     * @return actual tag generator\n     */",
    ),
]

# --- NamingConfig ---

R["naming/src/main/java/com/alibaba/nacos/naming/web/NamingConfig.java"] = [
    (
        "/**\n * Naming spring configuration.\n *\n * @author nkorange\n */",
        "/**\n * 命名模块 Web 层 Spring 配置。\n *\n"
        " * <p>注册 Distro、服务名校验、流量修订与客户端属性等 Servlet Filter，"
        "并预热 {@link ControllerMethodsCache}。</p>\n *\n * @author nkorange\n */",
    ),
    (
        "    @PostConstruct\n    public void init() {",
        "    /** 启动时扫描 naming controllers 包，缓存方法元数据供 DistroFilter 使用。 */\n"
        "    @PostConstruct\n    public void init() {",
    ),
    (
        "    @Bean\n    public FilterRegistrationBean<DistroFilter> distroFilterRegistration() {",
        "    /** 注册 Distro 过滤器，匹配 v1/v3 命名 API，顺序 7。 */\n"
        "    @Bean\n    public FilterRegistrationBean<DistroFilter> distroFilterRegistration() {",
    ),
    (
        "    @Bean\n    public FilterRegistrationBean<ServiceNameFilter> serviceNameFilterRegistration() {",
        "    /** 注册服务名兼容过滤器，顺序 5。 */\n"
        "    @Bean\n    public FilterRegistrationBean<ServiceNameFilter> serviceNameFilterRegistration() {",
    ),
    (
        "    @Bean\n    public FilterRegistrationBean<TrafficReviseFilter> trafficReviseFilterRegistration() {",
        "    /** 注册流量修订过滤器（限流与读写状态），顺序 1。 */\n"
        "    @Bean\n    public FilterRegistrationBean<TrafficReviseFilter> trafficReviseFilterRegistration() {",
    ),
    (
        "    @Bean\n    public FilterRegistrationBean<ClientAttributesFilter> clientAttributesFilterRegistration() {",
        "    /** 注册客户端属性采集过滤器，匹配 v1/v2，顺序 8。 */\n"
        "    @Bean\n    public FilterRegistrationBean<ClientAttributesFilter> clientAttributesFilterRegistration() {",
    ),
]

# --- ServiceNameFilter ---

R["naming/src/main/java/com/alibaba/nacos/naming/web/ServiceNameFilter.java"] = [
    (
        "/**\n * Service name filter. This class is created for adapting 1.x. client and old openAPI.\n * <p>\n"
        " * Because the old version will auto combined group and serviceName in old {@link DistroFilter}. So client and openAPI\n"
        " * can ignore group name.\n * </p>\n *\n * @author xiweng.yy\n */",
        "/**\n * 服务名兼容过滤器，适配 1.x 客户端与旧 OpenAPI。\n * <p>\n"
        " * 旧版 {@link DistroFilter} 会自动拼接 group 与服务名；本过滤器将 "
        "{@code groupName@@serviceName} 写回请求参数并校验格式。\n * </p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "            // use groupName@@serviceName as new service name:",
        "            // 将 group 与服务名合并为 groupName@@serviceName 标准格式",
    ),
]

# --- TrafficReviseFilter ---

R["naming/src/main/java/com/alibaba/nacos/naming/web/TrafficReviseFilter.java"] = [
    (
        "/**\n * Filter incoming traffic to refuse or revise unexpected requests.\n *\n * @author nkorange\n * @since 1.0.0\n */",
        "/**\n * 入站流量修订过滤器。\n *\n"
        " * <p>按 {@link SwitchDomain} 限流 URL、{@link ServerStatusManager} 节点状态"
        "（UP/READ_ONLY/WRITE_ONLY）决定放行或返回 503。</p>\n *\n"
        " * @author nkorange\n * @since 1.0.0\n */",
    ),
    (
        "        // request limit if exist:",
        "        // 若配置了 URL 前缀限流，直接返回指定 HTTP 状态码",
    ),
    (
        "        // if server is UP:",
        "        // 节点 UP 时全部放行",
    ),
    (
        "        // requests from peer server should be let pass:",
        "        // 来自集群对等节点的请求始终放行",
    ),
    (
        "        // write operation should be let pass in WRITE_ONLY status:",
        "        // WRITE_ONLY 状态下允许非 GET 写操作",
    ),
    (
        "        // read operation should be let pass in READ_ONLY status:",
        "        // READ_ONLY 状态下允许 GET 读操作",
    ),
]

# --- DatasourceConfiguration ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/configuration/DatasourceConfiguration.java"] = [
    (
        "/**\n * Configuration about datasource.\n *\n * @author xiweng.yy\n */",
        "/**\n * 持久化数据源配置初始化器。\n *\n"
        " * <p>在 Spring 上下文启动前根据 platform 与 standalone 模式决定使用外置 DB 还是内嵌存储，"
        "并设置 {@link #useExternalDb} 与 {@link #embeddedStorage} 静态标志。</p>\n *\n"
        " * @author xiweng.yy\n */",
    ),
    (
        "    /**\n     * Standalone mode uses DB.\n     */",
        "    /** 是否使用外置数据库（集群模式默认为 true）。 */",
    ),
    (
        "    /**\n     * Inline storage value = ${nacos.standalone}.\n     */",
        "    /** 是否启用内嵌存储，初始值取自 standalone 配置。 */",
    ),
    (
        "        // External data sources are used by default in cluster mode",
        "        // 集群模式默认走外置数据源；platform 非空且非 derby 即视为外置",
    ),
    (
        "        // must initialize after setUseExternalDb",
        "        // 须在 setUseExternalDb 之后设置 embeddedStorage",
    ),
    (
        "        // This value is true in stand-alone mode and false in cluster mode",
        "        // 单机通常为 true，集群为 false；集群强制 true 则开启分布式内嵌引擎",
    ),
    (
        "            // If the embedded data source storage is not turned on, it is automatically",
        "            // 未开启内嵌存储时自动升级到外置 DB，与历史行为一致",
    ),
    (
        "    @Override\n    public void initialize(final ConfigurableApplicationContext applicationContext) {",
        "    /** ApplicationContextInitializer 入口：加载并固化数据源类型配置。 */\n"
        "    @Override\n    public void initialize(final ConfigurableApplicationContext applicationContext) {",
    ),
]

# --- ConditionDistributedEmbedStorage ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/configuration/condition/ConditionDistributedEmbedStorage.java"] = [
    (
        "/**\n * when embeddedStorage==true and nacos.standalone=false\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 分布式内嵌存储条件：embeddedStorage 为 true 且非 standalone。\n *\n"
        " * <p>用于装配集群模式下 Derby 分布式存储相关 Bean。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    @Override\n    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {",
        "    /** 内嵌存储开启且运行在非单机模式时匹配。 */\n"
        "    @Override\n    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {",
    ),
]

# --- ConditionOnEmbeddedStorage ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/configuration/condition/ConditionOnEmbeddedStorage.java"] = [
    (
        "/**\n * Judge whether to user EmbeddedStorage by condition.\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 判断是否启用内嵌存储的 Spring {@link Condition}。\n *\n"
        " * <p>委托 {@link DatasourceConfiguration#isEmbeddedStorage()}。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    @Override\n    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {",
        "    /** embeddedStorage 为 true 时条件成立。 */\n"
        "    @Override\n    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {",
    ),
]

# --- ConditionOnExternalStorage ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/configuration/condition/ConditionOnExternalStorage.java"] = [
    (
        "/**\n * Judge whether to user ExternalStorage by condition.\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 判断是否使用外置存储的 Spring {@link Condition}。\n *\n"
        " * <p>与 {@link ConditionOnEmbeddedStorage} 互斥，即未启用内嵌存储时匹配。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    @Override\n    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {",
        "    /** 未启用内嵌存储（走外置 DB）时条件成立。 */\n"
        "    @Override\n    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {",
    ),
]
