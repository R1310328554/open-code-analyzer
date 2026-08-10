"""Chinese annotation replacements for Nacos 3.2.3 wave41a [0:15] config listener/query."""

R: dict[str, list[tuple[str, str]]] = {
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/task/DumpAllTagTask.java": [
        (
            "/**\n * Dump all tag task.\n *\n * @author Nacos\n * @date 2020/7/5 12:19 PM\n */",
            "/**\n * 全量标签配置 Dump 延迟任务：触发将持久层中全部带标签（Tag）的配置\n"
            " * 同步到本地内存缓存，供配置服务快速读取。\n"
            " * Dump all tag task.\n *\n * @author Nacos\n * @date 2020/7/5 12:19 PM\n */",
        ),
        (
            "    @Override\n    public void merge(AbstractDelayTask task) {",
            "    /** 合并同类型延迟任务；本任务无需合并逻辑，空实现。 */\n"
            "    @Override\n    public void merge(AbstractDelayTask task) {",
        ),
        (
            "    public static final String TASK_ID = \"dumpAllTagConfigTask\";",
            "    /** 任务队列唯一标识，用于 {@link com.alibaba.nacos.common.task.NacosTaskProcessor} 路由。 */\n"
            "    public static final String TASK_ID = \"dumpAllTagConfigTask\";",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/task/DumpAllTask.java": [
        (
            "/**\n * Dump all task.\n *\n * @author Nacos\n * @date 2020/7/5 12:17 PM\n */",
            "/**\n * 全量配置 Dump 延迟任务：将数据库中全部正式配置加载至本地缓存，\n"
            " * 通常在节点启动或运维触发时使用；{@code startUp} 标记是否为启动阶段执行。\n"
            " * Dump all task.\n *\n * @author Nacos\n * @date 2020/7/5 12:17 PM\n */",
        ),
        (
            "    private boolean startUp;",
            "    /** 是否为启动阶段触发的全量 Dump，用于区分冷启动与运维手动刷新。 */\n"
            "    private boolean startUp;",
        ),
        (
            "    public DumpAllTask(boolean startUp) {",
            "    /**\n"
            "     * 构造全量 Dump 任务并指定是否启动场景。\n"
            "     *\n"
            "     * @param startUp 启动阶段为 true，否则为 false\n"
            "     */\n"
            "    public DumpAllTask(boolean startUp) {",
        ),
        (
            "    public boolean isStartUp() {",
            "    /** 返回当前任务是否由节点启动流程触发。 */\n"
            "    public boolean isStartUp() {",
        ),
        (
            "    public static final String TASK_ID = \"dumpAllConfigTask\";",
            "    /** 全量 Dump 任务在延迟队列中的唯一 ID。 */\n"
            "    public static final String TASK_ID = \"dumpAllConfigTask\";",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/task/DumpTask.java": [
        (
            "/**\n * Dump data task.\n *\n * @author Nacos\n */",
            "/**\n * 单条配置增量 Dump 延迟任务：按 groupKey、灰度名与最后修改时间\n"
            " * 将一条配置从持久层同步到本地缓存；失败时按 1 秒间隔重试。\n"
            " * Dump data task.\n *\n * @author Nacos\n */",
        ),
        (
            "    public DumpTask(String groupKey, String grayName, long lastModified, String handleIp) {",
            "    /**\n"
            "     * 构造单条 Dump 任务。\n"
            "     *\n"
            "     * @param groupKey     配置 groupKey（dataId+group+tenant）\n"
            "     * @param grayName     灰度规则名，正式配置为空\n"
            "     * @param lastModified 配置最后修改时间戳\n"
            "     * @param handleIp     触发 Dump 的节点 IP\n"
            "     */\n"
            "    public DumpTask(String groupKey, String grayName, long lastModified, String handleIp) {",
        ),
        (
            "        //retry interval: 1s",
            "        // 重试间隔 1 秒，避免持久层短暂不可用时频繁打满",
        ),
        (
            "    final String groupKey;",
            "    /** 目标配置的 groupKey，Dump 路由主键。 */\n"
            "    final String groupKey;",
        ),
        (
            "    final String grayName;",
            "    /** 灰度规则标识；正式配置该字段为空字符串。 */\n"
            "    final String grayName;",
        ),
        (
            "    public String getHandleIp() {",
            "    /** 返回发起 Dump 请求的节点 IP，用于链路追踪。 */\n"
            "    public String getHandleIp() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/listener/ConfigListenerStateDelegate.java": [
        (
            "/**\n * Delegate for Config Listener State Service.\n *\n * @author xiweng.yy\n */",
            "/**\n * 配置监听状态查询委托：聚合本节点 {@link LocalConfigListenerStateServiceImpl}\n"
            " * 与集群其他节点 {@link RemoteConfigListenerStateServiceImpl} 的监听快照，\n"
            " * 供 OpenAPI 与控制台展示客户端订阅分布。\n"
            " * Delegate for Config Listener State Service.\n *\n * @author xiweng.yy\n */",
        ),
        (
            "    public ConfigListenerStateDelegate(LocalConfigListenerStateServiceImpl localService,",
            "    /**\n"
            "     * 注入本地与远程监听状态服务实现。\n"
            "     *\n"
            "     * @param localService  本节点长轮询与 gRPC 监听采样\n"
            "     * @param remoteService 集群其他节点 HTTP 聚合查询\n"
            "     */\n"
            "    public ConfigListenerStateDelegate(LocalConfigListenerStateServiceImpl localService,",
        ),
        (
            "    public ConfigListenerInfo getListenerState(String dataId, String groupName, String namespaceId,",
            "    /**\n"
            "     * 按 dataId/group/namespace 查询监听状态；{@code aggregation=true} 时合并远程节点结果。\n"
            "     *\n"
            "     * @param dataId       配置 dataId\n"
            "     * @param groupName    配置 group\n"
            "     * @param namespaceId  命名空间 ID\n"
            "     * @param aggregation  是否聚合集群其他成员\n"
            "     * @return 客户端 IP 与 MD5 映射\n"
            "     */\n"
            "    public ConfigListenerInfo getListenerState(String dataId, String groupName, String namespaceId,",
        ),
        (
            "        if (aggregation) {",
            "        // aggregation 开启时合并各远程节点的 listenersStatus",
        ),
        (
            "    public ConfigListenerInfo getListenerStateByIp(String ip, boolean aggregation) {",
            "    /**\n"
            "     * 按客户端 IP 反查其监听的 groupKey 与 MD5；可选聚合远程节点。\n"
            "     *\n"
            "     * @param ip          客户端 IP\n"
            "     * @param aggregation 是否合并集群其他成员\n"
            "     * @return 该 IP 的监听详情\n"
            "     */\n"
            "    public ConfigListenerInfo getListenerStateByIp(String ip, boolean aggregation) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/listener/ConfigListenerStateService.java": [
        (
            "/**\n * Nacos config listener statues service.\n *\n * @author xiweng.yy\n */",
            "/**\n * 配置监听状态查询接口：按配置三元组或客户端 IP 返回当前订阅客户端\n"
            " * 及其本地 MD5，供运维排查与一致性校验。\n"
            " * Nacos config listener statues service.\n *\n * @author xiweng.yy\n */",
        ),
        (
            "    /**\n     * Get config listener state by dataId, groupName, namespaceId.\n     *\n     * @param dataId        data id of config\n     * @param groupName     group name of config\n     * @param namespaceId   namespace id of config\n     * @return              listener state, include listener ip and config md5\n     */",
            "    /**\n"
            "     * 按 dataId、groupName、namespaceId 查询该配置的监听客户端列表。\n"
            "     *\n"
            "     * @param dataId        data id of config\n"
            "     * @param groupName     group name of config\n"
            "     * @param namespaceId   namespace id of config\n"
            "     * @return              listener state, include listener ip and config md5\n"
            "     */",
        ),
        (
            "    /**\n     * Get config listener state by listener ip.\n     *\n     * @param ip    listener ip\n     * @return      listener config information, include dataId, groupName, namespaceId and config md5\n     */",
            "    /**\n"
            "     * 按客户端 IP 反查其当前监听的配置及 MD5。\n"
            "     *\n"
            "     * @param ip    listener ip\n"
            "     * @return      listener config information, include dataId, groupName, namespaceId and config md5\n"
            "     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/listener/LocalConfigListenerStateServiceImpl.java": [
        (
            "/**\n * Local implementation for Config listener state service.\n *\n * @author xiweng.yy\n */",
            "/**\n * 本节点配置监听状态实现：合并 1.x 长轮询采样与 2.x+ gRPC 连接监听上下文，\n"
            " * 返回客户端 IP 与 MD5 映射。\n"
            " * Local implementation for Config listener state service.\n *\n * @author xiweng.yy\n */",
        ),
        (
            "        // long polling listeners for 1.x client TODO removed after 3.x not support 1.x client.",
            "        // 1.x 客户端走长轮询采样（3.x 移除 1.x 支持后待删除）",
        ),
        (
            "        // rpc listeners for upper 2.x client.",
            "        // 2.x 及以上客户端通过 gRPC 连接与 ConfigChangeListenContext 维护监听",
        ),
        (
            "        String groupKey = GroupKey2.getKey(dataId, groupName, namespaceId);",
            "        // 组装 groupKey 以便从监听上下文查找订阅连接",
        ),
        (
            "    private ConfigListenerInfo buildActualResult(SampleResult sampleResult, String type) {",
            "    /**\n"
            "     * 将内部 {@link SampleResult} 转为 API 层 {@link ConfigListenerInfo}。\n"
            "     *\n"
            "     * @param sampleResult 长轮询与 RPC 合并后的采样结果\n"
            "     * @param type         查询类型（按配置或按 IP）\n"
            "     * @return 对外暴露的监听状态对象\n"
            "     */\n"
            "    private ConfigListenerInfo buildActualResult(SampleResult sampleResult, String type) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/listener/RemoteConfigListenerStateServiceImpl.java": [
        (
            "/**\n * Local implementation for Config listener state service.\n *\n * @author xiweng.yy\n */",
            "/**\n * 远程集群成员监听状态查询：向除本节点外的各 Member 发起 HTTP 请求，\n"
            " * 聚合返回的 {@link ConfigListenerInfo}，用于集群级监听分布视图。\n"
            " * Local implementation for Config listener state service.\n *\n * @author xiweng.yy\n */",
        ),
        (
            "    private static final String CONFIG_LISTENER_STATE_URL =",
            "    /** 集群内查询单配置监听状态的 Admin V3 相对路径。 */\n"
            "    private static final String CONFIG_LISTENER_STATE_URL =",
        ),
        (
            "        Query query =\n            Query.newInstance().addParam(\"dataId\", dataId).addParam(\"groupName\", groupName)",
            "        // 构造查询参数，aggregation=false 避免远程节点再次递归聚合",
        ),
        (
            "        for (Member each : memberManager.allMembersWithoutSelf()) {",
            "        // 遍历集群成员（不含本节点）并发 HTTP 拉取监听快照",
        ),
        (
            "    private ConfigListenerInfo invokeUrl(String url, Query query, Header header) {",
            "    /**\n"
            "     * 调用远程 URL 获取监听状态；失败时记录日志并返回空结果占位。\n"
            "     *\n"
            "     * @param url    完整 HTTP 地址\n"
            "     * @param query  查询参数\n"
            "     * @param header 含鉴权与编码的请求头\n"
            "     * @return 解析后的监听信息或空对象\n"
            "     */\n"
            "    private ConfigListenerInfo invokeUrl(String url, Query query, Header header) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/notify/AsyncNotifyService.java": [
        (
            "/**\n * Async notify service.\n *\n * @author Nacos\n */",
            "/**\n * 配置变更异步集群通知：订阅 {@link ConfigDataChangeEvent}，通过 gRPC\n"
            " * 向各健康成员推送 {@link ConfigChangeClusterSyncRequest}，失败时指数退避重试。\n"
            " * Async notify service.\n *\n * @author Nacos\n */",
        ),
        (
            "        // Register ConfigDataChangeEvent to NotifyCenter.",
            "        // 向 NotifyCenter 注册配置变更事件发布器",
        ),
        (
            "        // Register A Subscriber to subscribe ConfigDataChangeEvent.",
            "        // 订阅 ConfigDataChangeEvent，在 onEvent 中触发集群同步",
        ),
        (
            "                // Generate ConfigDataChangeEvent concurrently",
            "                // 并发场景下由 NotifyCenter 回调处理变更事件",
        ),
        (
            "            // In fact, any type of queue here can be",
            "            // 此处使用 LinkedList 作为待通知 RPC 任务队列",
        ),
        (
            "                // grpc report data change only",
            "                // 仅通过 gRPC 上报配置变更（HTTP 通知已废弃）",
        ),
        (
            "            // old server should set beta or tag flag",
            "            // 兼容不支持灰度模型的旧节点：映射 beta/tag 标志",
        ),
        (
            "        // compatible with gray model",
            "        // 灰度兼容模式下构造 NotifySingleRpcTask",
        ),
        (
            "                // start the health check and there are ips that are not monitored, put them directly in the notification queue, otherwise notify",
            "                // 目标不健康则延迟重试，健康则立即发起 gRPC 同步",
        ),
        (
            "                //No nothing if  member has offline.",
            "                // 成员已离线则跳过，不做无效通知",
        ),
        (
            "            // Perform merge, but do nothing, tasks with the same dataId and group, later will replace the previous",
            "            // 同 dataId/group 任务后者覆盖前者，merge 保持空实现",
        ),
        (
            "    /**\n     * get delayTime and also set failCount to task; The failure time index increases, so as not to retry invalid tasks\n     * in the offline scene, which affects the normal synchronization.\n     *\n     * @param task notify task\n     * @return delay\n     */",
            "    /**\n"
            "     * 按失败次数计算退避延迟并递增 failCount，避免离线节点拖慢正常同步。\n"
            "     *\n"
            "     * @param task notify task\n"
            "     * @return delay 毫秒级重试间隔\n"
            "     */",
        ),
        (
            "                //get delay time and set fail count to the task",
            "                // 失败或异常时按退避策略调度重试",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/notify/HttpClientManager.java": [
        (
            "/**\n * http client manager.\n *\n * @author mai.jh\n */",
            "/**\n * 配置服务端 HTTP 客户端管理器：维护同步与异步 {@link NacosRestTemplate} 单例，\n"
            " * 供集群内通知、监听状态查询等模块复用，并在 JVM 关闭时优雅释放连接池。\n"
            " * http client manager.\n *\n * @author mai.jh\n */",
        ),
        (
            "    /**\n     * Connection timeout and socket timeout with other servers.\n     */",
            "    /**\n     * 与其他 Nacos 节点通信的默认连接与读超时（毫秒）。\n     */",
        ),
        (
            "        // build nacos rest template",
            "        // 初始化同步/异步 RestTemplate，通知模块使用可配置更长超时",
        ),
        (
            "    public static NacosRestTemplate getNacosRestTemplate() {",
            "    /** 返回配置模块共享的同步 HTTP 客户端（500ms 超时）。 */\n"
            "    public static NacosRestTemplate getNacosRestTemplate() {",
        ),
        (
            "    public static NacosAsyncRestTemplate getNacosAsyncRestTemplate() {",
            "    /** 返回异步 HTTP 客户端，超时取自 {@link PropertyUtil} 通知配置。 */\n"
            "    public static NacosAsyncRestTemplate getNacosAsyncRestTemplate() {",
        ),
        (
            "    /**\n     * http client factory.\n     */",
            "    /**\n     * 内部 HTTP 客户端工厂，按构造参数定制连接与读超时。\n     */",
        ),
        (
            "        @Override\n        protected HttpClientConfig buildHttpClientConfig() {",
            "        /** 构建带连接/读超时的 {@link HttpClientConfig}。 */\n"
            "        @Override\n        protected HttpClientConfig buildHttpClientConfig() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/ConfigChainRequestExtractorService.java": [
        (
            "/**\n * Service class for initializing and retrieving the configuration query request extractor.\n *\n * @author Nacos\n */",
            "/**\n * 配置查询链请求提取器门面：通过 SPI 加载 {@link ConfigQueryChainRequestExtractor}，\n"
            " * 由 {@code nacos.config.query.chain.request.extractor} 指定实现名（默认 nacos）。\n"
            " * Service class for initializing and retrieving the configuration query request extractor.\n *\n * @author Nacos\n */",
        ),
        (
            "        String curExtractor =\n            EnvUtil.getProperty(\"nacos.config.query.chain.request.extractor\", \"nacos\");",
            "        // 从环境读取提取器名称，默认使用 nacos 内置实现",
        ),
        (
            "    public static ConfigQueryChainRequestExtractor getExtractor() {",
            "    /** 返回已初始化的请求提取器单例，供 HTTP/gRPC 入口统一调用。 */\n"
            "    public static ConfigQueryChainRequestExtractor getExtractor() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/ConfigQueryChainRequestExtractor.java": [
        (
            "/**\n * Interface for extracting configuration query chain requests from different sources.\n *\n * @author Nacos\n */",
            "/**\n * 配置查询链请求提取接口：将 HTTP Servlet 或 gRPC {@link ConfigQueryRequest}\n"
            " * 统一转换为 {@link ConfigQueryChainRequest}，供责任链处理。\n"
            " * Interface for extracting configuration query chain requests from different sources.\n *\n * @author Nacos\n */",
        ),
        (
            "    /**\n     * Gets the name of the current implementation.\n     *\n     * @return the name of the current implementation\n     */",
            "    /**\n     * 返回当前 SPI 实现名称，与配置项 nacos.config.query.chain.request.extractor 对应。\n"
            "     *\n     * @return the name of the current implementation\n     */",
        ),
        (
            "    /**\n     * Extracts a configuration query chain request from an HTTP request.\n     *\n     * @param request the HTTP request object\n     * @return the extracted configuration query chain request\n     */",
            "    /**\n     * 从 HTTP 请求解析 dataId、group、tenant、标签与客户端 IP 等查询参数。\n"
            "     *\n     * @param request the HTTP request object\n     * @return the extracted configuration query chain request\n     */",
        ),
        (
            "    /**\n     * Extracts a configuration query chain request from a configuration query request object.\n     *\n     * @param request      the configuration query request object\n     * @param requestMeta  the request metadata\n     * @return the extracted configuration query chain request\n     */",
            "    /**\n     * 从 gRPC ConfigQueryRequest 与 RequestMeta 构建链式查询请求。\n"
            "     *\n     * @param request      the configuration query request object\n     * @param requestMeta  the request metadata\n     * @return the extracted configuration query chain request\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/ConfigQueryChainService.java": [
        (
            "/**\n * Service class for initializing and retrieving the configuration query chain builder.\n *\n * @author Nacos\n */",
            "/**\n * 配置查询责任链入口服务：SPI 加载 {@link ConfigQueryHandlerChainBuilder} 并构建\n"
            " * 处理链，对外提供 {@link #handle} 统一查询入口。\n"
            " * Service class for initializing and retrieving the configuration query chain builder.\n *\n * @author Nacos\n */",
        ),
        (
            "        String curChain = EnvUtil.getProperty(\"nacos.config.query.chain.builder\", \"nacos\");",
            "        // 读取责任链构建器名称，默认 nacos 内置链",
        ),
        (
            "    /**\n     * Handles the configuration query request.\n     *\n     * @param request the configuration query request object\n     * @return the configuration query response object\n     */",
            "    /**\n"
            "     * 执行配置查询责任链；异常时记录日志并返回 FAIL 响应。\n"
            "     *\n"
            "     * @param request the configuration query request object\n"
            "     * @return the configuration query response object\n"
            "     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/ConfigQueryHandlerChain.java": [
        (
            "/**\n * ConfigQueryHandlerChain.\n * @author Nacos\n */",
            "/**\n * 配置查询责任链：按添加顺序串联多个 {@link ConfigQueryHandler}，\n"
            " * 由头节点依次处理 {@link ConfigQueryChainRequest} 并返回响应。\n"
            " * ConfigQueryHandlerChain.\n * @author Nacos\n */",
        ),
        (
            "    /**\n     * Adds a new configuration query handler to the chain.\n     *\n     * @param handler the configuration query handler to be added\n     * @return the current configuration query handler chain object, supporting method chaining\n     */",
            "    /**\n"
            "     * 向链尾追加处理器，支持链式调用；null 处理器将被忽略并打 warn 日志。\n"
            "     *\n"
            "     * @param handler the configuration query handler to be added\n"
            "     * @return the current configuration query handler chain object, supporting method chaining\n"
            "     */",
        ),
        (
            "    public ConfigQueryChainResponse handle(ConfigQueryChainRequest request) throws IOException {",
            "    /**\n"
            "     * 从头节点开始执行责任链处理逻辑。\n"
            "     *\n"
            "     * @param request 统一查询请求\n"
            "     * @return 链处理结果\n"
            "     * @throws IOException IO 异常向上抛出\n"
            "     */\n"
            "    public ConfigQueryChainResponse handle(ConfigQueryChainRequest request) throws IOException {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/ConfigQueryHandlerChainBuilder.java": [
        (
            "/**\n * ConfigQueryHandlerChainBuilder.\n *\n * @author Nacos\n */",
            "/**\n * 配置查询责任链构建器 SPI：按业务场景组装 Handler 顺序，\n"
            " * 由 {@code nacos.config.query.chain.builder} 选择具体实现。\n"
            " * ConfigQueryHandlerChainBuilder.\n *\n * @author Nacos\n */",
        ),
        (
            "    /**\n     * Builds the configuration query handler chain.\n     *\n     * @return the configuration query handler chain\n     */",
            "    /**\n     * 构建并返回完整的配置查询 Handler 责任链。\n"
            "     *\n     * @return the configuration query handler chain\n     */",
        ),
        (
            "    /**\n     * Gets the name of the builder.\n     *\n     * @return the name of the builder\n     */",
            "    /**\n     * 返回构建器 SPI 名称，供 {@link ConfigQueryChainService} 过滤加载。\n"
            "     *\n     * @return the name of the builder\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/DefaultChainRequestExtractor.java": [
        (
            "/**\n * DefaultChainRequestExtractor.\n *\n * @author Nacos\n */",
            "/**\n * 默认配置查询请求提取器（SPI 名 nacos）：从 HTTP 或 gRPC 请求解析\n"
            " * dataId、group、tenant、灰度标签与客户端 IP 标签，供责任链灰度路由使用。\n"
            " * DefaultChainRequestExtractor.\n *\n * @author Nacos\n */",
        ),
        (
            "    @Override\n    public String getName() {",
            "    /** SPI 实现名，固定返回 {@code nacos}。 */\n"
            "    @Override\n    public String getName() {",
        ),
        (
            "        String tenant = request.getParameter(\"namespaceId\") != null",
            "        // namespaceId 与 tenant 参数二选一，空则归一化为空串",
        ),
        (
            "        String autoTag = request.getHeader(VIPSERVER_TAG);",
            "        // 读取 VIPServer 自动标签头，用于无显式 tag 时的灰度匹配",
        ),
        (
            "        appLabels.put(BetaGrayRule.CLIENT_IP_LABEL, clientIp);",
            "        // 写入客户端 IP 标签，供 Beta 灰度规则匹配",
        ),
        (
            "    @Override\n    public ConfigQueryChainRequest extract(ConfigQueryRequest request, RequestMeta requestMeta) {",
            "    /**\n"
            "     * 从 gRPC 请求与元数据构建链式查询对象，合并 appLabels 供灰度链使用。\n"
            "     *\n"
            "     * @param request     RPC 配置查询请求\n"
            "     * @param requestMeta 含 clientIp 与 appLabels 的元数据\n"
            "     * @return 责任链统一请求模型\n"
            "     */\n"
            "    @Override\n    public ConfigQueryChainRequest extract(ConfigQueryRequest request, RequestMeta requestMeta) {",
        ),
    ],
}
