"""RocketMQ 5.5.0 wave18a container/controller [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["container/src/main/java/org/apache/rocketmq/container/BrokerContainerProcessor.java"] = [
    (
        "public class BrokerContainerProcessor implements NettyRequestProcessor {",
        "/**\n * Broker 容器远程请求处理器：处理动态增删 Broker、读取与更新容器级配置。\n * 实现 {@link NettyRequestProcessor}，响应 ADD/REMOVE/GET/UPDATE 等请求码。\n */\npublic class BrokerContainerProcessor implements NettyRequestProcessor {",
    ),
    (
        "    protected static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);",
        "    /** 容器模块日志记录器。 */\n    protected static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);",
    ),
    (
        "    protected final Set<String> configBlackList = new HashSet<>();",
        "    /** 禁止通过远程接口修改的配置项黑名单。 */\n    protected final Set<String> configBlackList = new HashSet<>();",
    ),
    (
        "    public BrokerContainerProcessor(BrokerContainer brokerContainer) {",
        "    /** 绑定所属 {@link BrokerContainer} 并初始化配置黑名单。 */\n    public BrokerContainerProcessor(BrokerContainer brokerContainer) {",
    ),
    (
        "    private void initConfigBlackList() {",
        "    /** 合并内置与配置文件中声明的黑名单项。 */\n    private void initConfigBlackList() {",
    ),
    (
        "    @Override\n    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
        "    /** 按请求码分发至增删 Broker 或配置读写处理逻辑。 */\n    @Override\n    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
    ),
    (
        "    protected synchronized RemotingCommand addBroker(ChannelHandlerContext ctx,\n        RemotingCommand request) throws Exception {",
        "    /**\n     * 加载配置文件、校验副本参数并启动容器内新 Broker。\n     * 启动失败时回滚已注册的 Broker 实例。\n     */\n    protected synchronized RemotingCommand addBroker(ChannelHandlerContext ctx,\n        RemotingCommand request) throws Exception {",
    ),
    (
        "    protected synchronized RemotingCommand removeBroker(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
        "    /** 按 {@link BrokerIdentity} 从容器中移除指定 Broker。 */\n    protected synchronized RemotingCommand removeBroker(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
    ),
    (
        "    public void registerBrokerBootHook(List<BrokerBootHook> brokerBootHookList) {",
        "    /** 注册 Broker 启动前后执行的钩子列表。 */\n    public void registerBrokerBootHook(List<BrokerBootHook> brokerBootHookList) {",
    ),
    (
        "    private RemotingCommand updateBrokerConfig(ChannelHandlerContext ctx, RemotingCommand request) {",
        "    /** 更新容器共享配置，拒绝修改黑名单中的键。 */\n    private RemotingCommand updateBrokerConfig(ChannelHandlerContext ctx, RemotingCommand request) {",
    ),
    (
        "    private boolean validateBlackListConfigExist(Properties properties) {",
        "    /** 检查待更新属性是否包含黑名单配置项。 */\n    private boolean validateBlackListConfigExist(Properties properties) {",
    ),
    (
        "    private RemotingCommand getBrokerConfig(ChannelHandlerContext ctx, RemotingCommand request) {",
        "    /** 返回容器全部配置文本及数据版本号。 */\n    private RemotingCommand getBrokerConfig(ChannelHandlerContext ctx, RemotingCommand request) {",
    ),
]

R["container/src/main/java/org/apache/rocketmq/container/ContainerClientHouseKeepingService.java"] = [
    (
        "public class ContainerClientHouseKeepingService implements ChannelEventListener {",
        "/**\n * 容器级客户端连接 housekeeping：将 Netty 通道事件\n * 广播给容器内所有主从 Broker，维护生产者/消费者连接统计。\n */\npublic class ContainerClientHouseKeepingService implements ChannelEventListener {",
    ),
    (
        "    public ContainerClientHouseKeepingService(final IBrokerContainer brokerContainer) {",
        "    /** @param brokerContainer 托管多个 Broker 的容器实例 */\n    public ContainerClientHouseKeepingService(final IBrokerContainer brokerContainer) {",
    ),
    (
        "    private void onChannelOperation(CallbackCode callbackCode, String remoteAddr, Channel channel) {",
        "    /** 遍历主从 Broker 并统一分发通道事件。 */\n    private void onChannelOperation(CallbackCode callbackCode, String remoteAddr, Channel channel) {",
    ),
    (
        "    private void brokerOperation(BrokerController brokerController, CallbackCode callbackCode, String remoteAddr,\n        Channel channel) {",
        "    /** 更新连接计数或清理生产者/消费者上的失效通道。 */\n    private void brokerOperation(BrokerController brokerController, CallbackCode callbackCode, String remoteAddr,\n        Channel channel) {",
    ),
    (
        "    public enum CallbackCode {\n        /**\n         * onChannelConnect\n         */\n        CONNECT,",
        "    /** 通道事件类型枚举。 */\n    public enum CallbackCode {\n        /** 新连接建立。 */\n        CONNECT,",
    ),
    (
        "        /**\n         * onChannelClose\n         */\n        CLOSE,",
        "        /** 连接正常关闭。 */\n        CLOSE,",
    ),
    (
        "        /**\n         * onChannelException\n         */\n        EXCEPTION,",
        "        /** 连接发生异常。 */\n        EXCEPTION,",
    ),
    (
        "        /**\n         * onChannelIdle\n         */\n        IDLE,",
        "        /** 连接空闲超时。 */\n        IDLE,",
    ),
    (
        "        /**\n         * onChannelActive\n         */\n        ACTIVE",
        "        /** 通道变为活跃状态。 */\n        ACTIVE",
    ),
]

R["container/src/main/java/org/apache/rocketmq/container/IBrokerContainer.java"] = [
    (
        "/**\n * An interface for broker container to hold multiple master and slave brokers.\n */\npublic interface IBrokerContainer {",
        "/**\n * Broker 容器接口：在同一 JVM 进程中托管多个主从 Broker，\n * 共享 Remoting 服务器与 {@link BrokerOuterAPI} 等基础设施。\n */\npublic interface IBrokerContainer {",
    ),
    (
        "    /**\n     * Start broker container\n     */\n    void start() throws Exception;",
        "    /** 启动容器及共享网络组件。 */\n    void start() throws Exception;",
    ),
    (
        "    /**\n     * Shutdown broker container and all the brokers inside.\n     */\n    void shutdown();",
        "    /** 关闭容器内全部 Broker 并释放共享资源。 */\n    void shutdown();",
    ),
    (
        "    /**\n     * Add a broker to this container with specific broker config.\n     *\n     * @param configContext the specified config context\n     * @return the added BrokerController or null if the broker already exists\n     * @throws Exception when initialize broker\n     */\n    BrokerController addBroker(ConfigContext configContext) throws Exception;",
        "    /**\n     * 按配置上下文向容器动态添加 Broker。\n     *\n     * @param configContext 包含 Broker/Store/Auth 等配置\n     * @return 新建的控制器；已存在时返回 null\n     * @throws Exception 初始化 Broker 失败时抛出\n     */\n    BrokerController addBroker(ConfigContext configContext) throws Exception;",
    ),
    (
        "    /**\n     * Remove the broker from this container associated with the specific broker identity\n     *\n     * @param brokerIdentity the specific broker identity\n     * @return the removed BrokerController or null if the broker doesn't exists\n     */\n    BrokerController removeBroker(BrokerIdentity brokerIdentity) throws Exception;",
        "    /**\n     * 按身份标识从容器移除 Broker。\n     *\n     * @param brokerIdentity 集群名、Broker 名与 ID 组合\n     * @return 被移除的控制器；不存在时返回 null\n     */\n    BrokerController removeBroker(BrokerIdentity brokerIdentity) throws Exception;",
    ),
    (
        "    /**\n     * Return the broker controller associated with the specific broker identity\n     *\n     * @param brokerIdentity the specific broker identity\n     * @return the associated messaging broker or null\n     */\n    BrokerController getBroker(BrokerIdentity brokerIdentity);",
        "    /**\n     * 按身份查找已注册的 Broker 控制器。\n     *\n     * @param brokerIdentity 目标 Broker 身份\n     * @return 匹配的控制器或 null\n     */\n    BrokerController getBroker(BrokerIdentity brokerIdentity);",
    ),
    (
        "    /**\n     * Return all the master brokers belong to this container\n     *\n     * @return the master broker list\n     */\n    Collection<InnerBrokerController> getMasterBrokers();",
        "    /** 返回容器内全部 Master Broker。 */\n    Collection<InnerBrokerController> getMasterBrokers();",
    ),
    (
        "    /**\n     * Return all the slave brokers belong to this container\n     *\n     * @return the slave broker list\n     */\n    Collection<InnerSalveBrokerController> getSlaveBrokers();",
        "    /** 返回容器内全部 Slave Broker。 */\n    Collection<InnerSalveBrokerController> getSlaveBrokers();",
    ),
    (
        "    /**\n     * Return all broker controller in this container\n     *\n     * @return all broker controller\n     */\n    List<BrokerController> getBrokerControllers();",
        "    /** 返回容器内所有 Broker 控制器列表。 */\n    List<BrokerController> getBrokerControllers();",
    ),
    (
        "    /**\n     * Return the address of broker container.\n     *\n     * @return broker container address.\n     */\n    String getBrokerContainerAddr();",
        "    /** 返回容器对外暴露的监听地址。 */\n    String getBrokerContainerAddr();",
    ),
    (
        "    /**\n     * Peek the first master broker in container.\n     *\n     * @return the first master broker in container\n     */\n    BrokerController peekMasterBroker();",
        "    /** 获取容器中第一个 Master Broker（用于默认路由）。 */\n    BrokerController peekMasterBroker();",
    ),
    (
        "    /**\n     * Return the config of the broker container\n     *\n     * @return the broker container config\n     */\n    BrokerContainerConfig getBrokerContainerConfig();",
        "    /** 返回容器级配置对象。 */\n    BrokerContainerConfig getBrokerContainerConfig();",
    ),
    (
        "    /**\n     * Get netty server config.\n     *\n     * @return netty server config\n     */\n    NettyServerConfig getNettyServerConfig();",
        "    /** 返回共享 Netty 服务端配置。 */\n    NettyServerConfig getNettyServerConfig();",
    ),
    (
        "    /**\n     * Get netty client config.\n     *\n     * @return netty client config\n     */\n    NettyClientConfig getNettyClientConfig();",
        "    /** 返回共享 Netty 客户端配置。 */\n    NettyClientConfig getNettyClientConfig();",
    ),
    (
        "    /**\n     * Return the shared BrokerOuterAPI\n     *\n     * @return the shared BrokerOuterAPI\n     */\n    BrokerOuterAPI getBrokerOuterAPI();",
        "    /** 返回容器内 Broker 共用的对外 RPC 客户端。 */\n    BrokerOuterAPI getBrokerOuterAPI();",
    ),
    (
        "    /**\n     * Return the shared RemotingServer\n     *\n     * @return the shared RemotingServer\n     */\n    RemotingServer getRemotingServer();",
        "    /** 返回容器共享的 Remoting 服务端。 */\n    RemotingServer getRemotingServer();",
    ),
]

R["container/src/main/java/org/apache/rocketmq/container/InnerBrokerController.java"] = [
    (
        "public class InnerBrokerController extends BrokerController {",
        "/**\n * 容器内 Master Broker 控制器：复用容器共享的 Remoting 与 OuterAPI，\n * 按容器策略注册 NameServer 并调度心跳任务。\n */\npublic class InnerBrokerController extends BrokerController {",
    ),
    (
        "    protected BrokerContainer brokerContainer;",
        "    /** 所属 Broker 容器引用。 */\n    protected BrokerContainer brokerContainer;",
    ),
    (
        "    @Override\n    protected void initializeRemotingServer() {",
        "    /** 从容器 Remoting 工厂创建主/快通道服务端并绑定指标。 */\n    @Override\n    protected void initializeRemotingServer() {",
    ),
    (
        "    @Override\n    protected void initializeScheduledTasks() {",
        "    /** 仅初始化 Broker 自身定时任务（容器模式不重复注册全局任务）。 */\n    @Override\n    protected void initializeScheduledTasks() {",
    ),
    (
        "    @Override\n    public void start() throws Exception {",
        "    /**\n     * 启动基础服务并按配置向 NameServer 注册；\n     * 支持从节点代主、Controller 模式心跳及成员组同步。\n     */\n    @Override\n    public void start() throws Exception {",
    ),
    (
        "    @Override\n    public void shutdown() {",
        "    /** 关闭基础服务、取消定时任务并从容器 Remoting 注销端口。 */\n    @Override\n    public void shutdown() {",
    ),
    (
        "    @Override\n    public String getBrokerAddr() {",
        "    /** 返回 Broker 对外服务地址（IP1:listenPort）。 */\n    @Override\n    public String getBrokerAddr() {",
    ),
    (
        "    @Override\n    public String getHAServerAddr() {",
        "    /** 返回 HA 复制监听地址（IP2:haListenPort）。 */\n    @Override\n    public String getHAServerAddr() {",
    ),
    (
        "    public MessageStore getMessageStoreByBrokerName(String brokerName) {",
        "    /** 按 Broker 名查找本容器内对应的消息存储实例。 */\n    public MessageStore getMessageStoreByBrokerName(String brokerName) {",
    ),
    (
        "    @Override\n    public BrokerController peekMasterBroker() {",
        "    /** 若自身为 Master 则返回 this，否则委托容器查找 Master。 */\n    @Override\n    public BrokerController peekMasterBroker() {",
    ),
]

R["container/src/main/java/org/apache/rocketmq/container/InnerSalveBrokerController.java"] = [
    (
        "public class InnerSalveBrokerController extends InnerBrokerController {",
        "/**\n * 容器内 Slave Broker 控制器：继承 {@link InnerBrokerController}，\n * 构造时校验从节点 ID 必须非 Master。\n */\npublic class InnerSalveBrokerController extends InnerBrokerController {",
    ),
    (
        "        // Check configs\n        checkSlaveBrokerConfig();",
        "        // 校验从节点必备配置\n        checkSlaveBrokerConfig();",
    ),
    (
        "    private void checkSlaveBrokerConfig() {",
        "    /** 断言集群名、Broker 名非空且 brokerId 不为 Master ID。 */\n    private void checkSlaveBrokerConfig() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/BrokerHeartbeatManager.java"] = [
    (
        "public interface BrokerHeartbeatManager {",
        "/**\n * Broker 心跳管理器：维护 Broker 存活状态、处理心跳与通道关闭，\n * 支持 DLedger 与 JRaft 两种 Controller 实现。\n */\npublic interface BrokerHeartbeatManager {",
    ),
    (
        "    static BrokerHeartbeatManager newBrokerHeartbeatManager(ControllerConfig controllerConfig) {",
        "    /** 按 Controller 类型创建对应的心跳管理器实例。 */\n    static BrokerHeartbeatManager newBrokerHeartbeatManager(ControllerConfig controllerConfig) {",
    ),
    (
        "    /**\n     * Initialize the resources\n     */\n    void initialize();",
        "    /** 初始化心跳检测所需资源。 */\n    void initialize();",
    ),
    (
        "    /**\n     * Broker new heartbeat.\n     */\n    void onBrokerHeartbeat(final String clusterName, final String brokerName, final String brokerAddr,",
        "    /** 处理 Broker 上报的心跳并更新存活信息。 */\n    void onBrokerHeartbeat(final String clusterName, final String brokerName, final String brokerAddr,",
    ),
    (
        "    /**\n     * Start heartbeat manager.\n     */\n    void start();",
        "    /** 启动心跳超时扫描等后台任务。 */\n    void start();",
    ),
    (
        "    /**\n     * Shutdown heartbeat manager.\n     */\n    void shutdown();",
        "    /** 关闭心跳管理器并释放资源。 */\n    void shutdown();",
    ),
    (
        "    /**\n     * Add BrokerLifecycleListener.\n     */\n    void registerBrokerLifecycleListener(final BrokerLifecycleListener listener);",
        "    /** 注册 Broker 下线等生命周期监听器。 */\n    void registerBrokerLifecycleListener(final BrokerLifecycleListener listener);",
    ),
    (
        "    /**\n     * Broker channel close\n     */\n    void onBrokerChannelClose(final Channel channel);",
        "    /** 通道关闭时清理对应 Broker 存活记录。 */\n    void onBrokerChannelClose(final Channel channel);",
    ),
    (
        "    /**\n     * Get broker live information by clusterName and brokerAddr\n     *\n     * @return broker live information or null if not found\n     */\n    BrokerLiveInfo getBrokerLiveInfo(String clusterName, String brokerName, Long brokerId);",
        "    /**\n     * 按集群与 Broker 身份查询存活详情。\n     *\n     * @return 存活信息；未找到时返回 null\n     */\n    BrokerLiveInfo getBrokerLiveInfo(String clusterName, String brokerName, Long brokerId);",
    ),
    (
        "    /**\n     * Check whether broker active\n     */\n    boolean isBrokerActive(final String clusterName, final String brokerName, final Long brokerId);",
        "    /** 判断指定 Broker 副本当前是否存活。 */\n    boolean isBrokerActive(final String clusterName, final String brokerName, final Long brokerId);",
    ),
    (
        "    /**\n     * Count the number of active brokers in each broker-set of each cluster\n     *\n     * @return active brokers count\n     */\n    Map<String/*cluster*/, Map<String/*broker-set*/, Integer/*active broker num*/>> getActiveBrokersNum();",
        "    /**\n     * 统计各集群各 Broker 组内活跃副本数量。\n     *\n     * @return 嵌套映射：集群 → Broker 组 → 活跃数\n     */\n    Map<String/*cluster*/, Map<String/*broker-set*/, Integer/*active broker num*/>> getActiveBrokersNum();",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/BrokerHousekeepingService.java"] = [
    (
        "public class BrokerHousekeepingService implements ChannelEventListener {",
        "/**\n * Controller 侧 Broker 连接 housekeeping：在通道关闭、异常或空闲时\n * 通知 {@link BrokerHeartbeatManager} 清理失效 Broker 记录。\n */\npublic class BrokerHousekeepingService implements ChannelEventListener {",
    ),
    (
        "    public BrokerHousekeepingService(ControllerManager controllerManager) {",
        "    /** @param controllerManager 持有心跳管理器的 Controller 管理器 */\n    public BrokerHousekeepingService(ControllerManager controllerManager) {",
    ),
    (
        "    @Override\n    public void onChannelClose(String remoteAddr, Channel channel) {",
        "    /** 连接关闭时触发 Broker 通道清理。 */\n    @Override\n    public void onChannelClose(String remoteAddr, Channel channel) {",
    ),
    (
        "    @Override\n    public void onChannelException(String remoteAddr, Channel channel) {",
        "    /** 连接异常时同样清理 Broker 心跳状态。 */\n    @Override\n    public void onChannelException(String remoteAddr, Channel channel) {",
    ),
    (
        "    @Override\n    public void onChannelIdle(String remoteAddr, Channel channel) {",
        "    /** 空闲超时时清理对应 Broker 通道。 */\n    @Override\n    public void onChannelIdle(String remoteAddr, Channel channel) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/Controller.java"] = [
    (
        "/**\n * The api for controller\n */\npublic interface Controller {",
        "/**\n * Controller 核心 API：主从选举、副本集变更、Broker 注册及元数据查询。\n * Leader 节点负责调度副本状态同步相关事件。\n */\npublic interface Controller {",
    ),
    (
        "    /**\n     * Startup controller\n     */\n    void startup();",
        "    /** 启动 Controller 进程与共识组件。 */\n    void startup();",
    ),
    (
        "    /**\n     * Shutdown controller\n     */\n    void shutdown();",
        "    /** 关闭 Controller 并释放共识与网络资源。 */\n    void shutdown();",
    ),
    (
        "    /**\n     * Start scheduling controller events, this function only will be triggered when the controller becomes leader.\n     */\n    void startScheduling();",
        "    /** 成为 Leader 后启动副本状态调度任务。 */\n    void startScheduling();",
    ),
    (
        "    /**\n     * Stop scheduling controller events, this function only will be triggered when the controller lose leadership.\n     */\n    void stopScheduling();",
        "    /** 失去 Leader 身份后停止调度任务。 */\n    void stopScheduling();",
    ),
    (
        "    /**\n     * Whether this controller is in leader state.\n     */\n    boolean isLeaderState();",
        "    /** 当前节点是否为 Controller Leader。 */\n    boolean isLeaderState();",
    ),
    (
        "    /**\n     * Alter SyncStateSet of broker replicas.\n     *\n     * @param request AlterSyncStateSetRequestHeader\n     * @return RemotingCommand(AlterSyncStateSetResponseHeader)\n     */",
        "    /**\n     * 修改 Broker 副本集的 SyncStateSet（同步副本集合）。\n     *\n     * @param request AlterSyncStateSetRequestHeader\n     * @return RemotingCommand(AlterSyncStateSetResponseHeader)\n     */",
    ),
    (
        "    /**\n     * Elect new master for a broker.\n     *\n     * @param request ElectMasterRequest\n     * @return RemotingCommand(ElectMasterResponseHeader)\n     */",
        "    /**\n     * 为指定 Broker 组选举新 Master。\n     *\n     * @param request ElectMasterRequest\n     * @return RemotingCommand(ElectMasterResponseHeader)\n     */",
    ),
    (
        "    CompletableFuture<RemotingCommand> getNextBrokerId(final GetNextBrokerIdRequestHeader request);",
        "    /** 申请 Broker 组内下一个可用 brokerId。 */\n    CompletableFuture<RemotingCommand> getNextBrokerId(final GetNextBrokerIdRequestHeader request);",
    ),
    (
        "    CompletableFuture<RemotingCommand> applyBrokerId(final ApplyBrokerIdRequestHeader request);",
        "    /** 向 Controller 申请并预留指定 brokerId。 */\n    CompletableFuture<RemotingCommand> applyBrokerId(final ApplyBrokerIdRequestHeader request);",
    ),
    (
        "    /**\n     * Register broker with unique brokerId and now broker address\n     *\n     * @param request RegisterBrokerToControllerRequest\n     * @return RemotingCommand(RegisterBrokerToControllerResponseHeader)\n     */",
        "    /**\n     * 注册 Broker 及其唯一 brokerId 与当前地址。\n     *\n     * @param request RegisterBrokerToControllerRequest\n     * @return RemotingCommand(RegisterBrokerToControllerResponseHeader)\n     */",
    ),
    (
        "    /**\n     * Get the Replica Info for a target broker.\n     *\n     * @param request GetRouteInfoRequest\n     * @return RemotingCommand(GetReplicaInfoResponseHeader)\n     */",
        "    /**\n     * 查询目标 Broker 组的副本与 Master 信息。\n     *\n     * @param request GetRouteInfoRequest\n     * @return RemotingCommand(GetReplicaInfoResponseHeader)\n     */",
    ),
    (
        "    /**\n     * Get Metadata of controller\n     *\n     * @return RemotingCommand(GetControllerMetadataResponseHeader)\n     */\n    RemotingCommand getControllerMetadata();",
        "    /** 返回 Controller 集群元数据（成员、Leader 等）。 */\n    RemotingCommand getControllerMetadata();",
    ),
    (
        "    /**\n     * Get SyncStateData for target brokers, this api is used for admin tools.\n     */",
        "    /** 批量查询 Broker 同步状态，供管理工具使用。 */",
    ),
    (
        "    /**\n     * Add broker's lifecycle listener\n     * @param listener listener\n     */",
        "    /**\n     * 注册 Broker 生命周期监听器。\n     * @param listener 监听器实例\n     */",
    ),
    (
        "    /**\n     * Get the remotingServer used by the controller, the upper layer will reuse this remotingServer.\n     */",
        "    /** 返回 Controller 使用的 RemotingServer，供上层复用注册处理器。 */",
    ),
    (
        "    /**\n     * Clean controller broker data\n     *\n     */",
        "    /** 清理 Controller 中指定 Broker 的持久化元数据。 */",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/ControllerManager.java"] = [
    (
        "public class ControllerManager {",
        "/**\n * Controller 进程总控：组装心跳、选举、通知与请求处理器，\n * 在 Broker 下线时触发 Master 选举并通知角色变更。\n */\npublic class ControllerManager {",
    ),
    (
        "    public ControllerManager(ControllerConfig controllerConfig, NettyServerConfig nettyServerConfig,\n        NettyClientConfig nettyClientConfig) {",
        "    /** 根据配置创建 DLedger 或 JRaft 版 Controller 及配套组件。 */\n    public ControllerManager(ControllerConfig controllerConfig, NettyServerConfig nettyServerConfig,\n        NettyClientConfig nettyClientConfig) {",
    ),
    (
        "    public boolean initialize() {",
        "    /** 初始化线程池、Controller 实现、心跳监听与 RPC 处理器注册。 */\n    public boolean initialize() {",
    ),
    (
        "    /**\n     * When the heartbeatManager detects the \"Broker is not active\", we call this method to elect a master and do\n     * something else.\n     *\n     * @param clusterName The cluster name of this inactive broker\n     * @param brokerName  The inactive broker name\n     * @param brokerId    The inactive broker id, null means that the election forced to be triggered\n     */",
        "    /**\n     * 心跳检测到 Broker 不活跃时的回调：若为 Master 则触发选主。\n     *\n     * @param clusterName 集群名\n     * @param brokerName  Broker 组名\n     * @param brokerId    下线副本 ID；null 表示强制选举\n     */",
    ),
    (
        "    private CompletableFuture<Boolean> triggerElectMaster0(String brokerName) {",
        "    /** 异步调用 Controller 选主并可选通知角色变更。 */\n    private CompletableFuture<Boolean> triggerElectMaster0(String brokerName) {",
    ),
    (
        "    private void triggerElectMaster(String brokerName) {",
        "    /** 带重试的同步选主入口。 */\n    private void triggerElectMaster(String brokerName) {",
    ),
    (
        "    /**\n     * Notify master and all slaves for a broker that the master role changed.\n     */",
        "    /** 向 Broker 组内全部活跃副本通知 Master 角色变更。 */",
    ),
    (
        "    /**\n     * Notify broker that there are roles-changing in controller\n     *\n     * @param brokerAddr target broker's address to notify\n     * @param entry      role change entry\n     */",
        "    /**\n     * 向单个 Broker 地址发送角色变更通知。\n     *\n     * @param brokerAddr 目标 Broker 地址\n     * @param entry      角色变更详情\n     */",
    ),
    (
        "    public void registerProcessor() {",
        "    /** 向 RemotingServer 注册 Controller 相关请求处理器。 */\n    public void registerProcessor() {",
    ),
    (
        "    public void start() {",
        "    /** 启动 Controller、心跳管理器与 Remoting 客户端。 */\n    public void start() {",
    ),
    (
        "    public void shutdown() {",
        "    /** 依次关闭心跳、线程池、通知服务与 Controller。 */\n    public void shutdown() {",
    ),
    (
        "    class NotifyService {",
        "    /** 异步通知 Broker 角色变更，按 masterEpoch 取消过期任务。 */\n    class NotifyService {",
    ),
    (
        "        public void notifyBroker(String brokerAddress, RoleChangeNotifyEntry entry) {",
        "        /** 提交通知任务；若存在更旧 epoch 的未完成通知则取消。 */\n        public void notifyBroker(String brokerAddress, RoleChangeNotifyEntry entry) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/ControllerStartup.java"] = [
    (
        "public class ControllerStartup {",
        "/**\n * Controller 进程启动入口：解析命令行与配置文件，\n * 创建 {@link ControllerManager} 并注册 JVM 关闭钩子。\n */\npublic class ControllerStartup {",
    ),
    (
        "    public static void main(String[] args) {",
        "    /** JVM 主入口，启动失败时以非零码退出。 */\n    public static void main(String[] args) {",
    ),
    (
        "    public static ControllerManager main0(String[] args) {",
        "    /** 创建、启动 Controller 并打印序列化类型等启动信息。 */\n    public static ControllerManager main0(String[] args) {",
    ),
    (
        "    public static ControllerManager createControllerManager(String[] args) throws IOException {",
        "    /**\n     * 解析 -c 配置文件与 -p 打印选项，加载 Controller/Netty 配置。\n     * 校验 ROCKETMQ_HOME 后构造 {@link ControllerManager}。\n     */\n    public static ControllerManager createControllerManager(String[] args) throws IOException {",
    ),
    (
        "    public static ControllerManager start(final ControllerManager controller) throws Exception {",
        "    /** 初始化 Controller、注册 shutdown hook 并启动服务。 */\n    public static ControllerManager start(final ControllerManager controller) throws Exception {",
    ),
    (
        "    public static void shutdown(final ControllerManager controller) {",
        "    /** 显式关闭 Controller 管理器。 */\n    public static void shutdown(final ControllerManager controller) {",
    ),
    (
        "    public static Options buildCommandlineOptions(final Options options) {",
        "    /** 构建 -c（配置文件）与 -p（打印配置）命令行选项。 */\n    public static Options buildCommandlineOptions(final Options options) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/elect/ElectPolicy.java"] = [
    (
        "public interface ElectPolicy {",
        "/**\n * Master 选举策略接口：根据同步副本集与全部副本\n * 在 Controller 触发选主时返回新 Master 的 brokerId。\n */\npublic interface ElectPolicy {",
    ),
    (
        "    /**\n     * elect a master\n     *\n     * @param clusterName       the broker group belongs to\n     * @param brokerName        the broker group name\n     * @param syncStateBrokers  all broker replicas in syncStateSet\n     * @param allReplicaBrokers all broker replicas\n     * @param oldMaster         old master\n     * @param brokerId          broker id(can be used as prefer or assigned in some elect policy)\n     * @return new master's broker id\n     */",
        "    /**\n     * 执行 Master 选举。\n     *\n     * @param clusterName       Broker 所属集群名\n     * @param brokerName        Broker 组名\n     * @param syncStateBrokers  SyncStateSet 内副本 ID 集合\n     * @param allReplicaBrokers 全部注册副本 ID 集合\n     * @param oldMaster         原 Master 的 brokerId\n     * @param brokerId          优先或指定的 brokerId\n     * @return 新 Master 的 brokerId；无法选出时返回 null\n     */",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/elect/impl/DefaultElectPolicy.java"] = [
    (
        "public class DefaultElectPolicy implements ElectPolicy {",
        "/**\n * 默认选主策略：先在 SyncStateSet 内、再在全副本中\n * 按存活过滤、优先保留旧 Master 或指定 ID，否则按 epoch/offset 排序。\n */\npublic class DefaultElectPolicy implements ElectPolicy {",
    ),
    (
        "    // <clusterName, brokerName, brokerAddr>, Used to judge whether a broker\n    // has preliminary qualification to be selected as master\n    private BrokerValidPredicate validPredicate;",
        "    /** 判断副本是否具备被选为 Master 的存活资格。 */\n    private BrokerValidPredicate validPredicate;",
    ),
    (
        "    // <clusterName, brokerName, brokerAddr, BrokerLiveInfo>, Used to obtain the BrokerLiveInfo information of a broker\n    private BrokerLiveInfoGetter brokerLiveInfoGetter;",
        "    /** 获取副本心跳详情用于排序比较。 */\n    private BrokerLiveInfoGetter brokerLiveInfoGetter;",
    ),
    (
        "    // Sort in descending order according to<epoch, offset>, and sort in ascending order according to priority\n    private final Comparator<BrokerLiveInfo> comparator = (o1, o2) -> {",
        "    /** 按 epoch 降序、maxOffset 降序、electionPriority 升序比较副本。 */\n    private final Comparator<BrokerLiveInfo> comparator = (o1, o2) -> {",
    ),
    (
        "    /**\n     * We will try to select a new master from syncStateBrokers and allReplicaBrokers in turn.\n     * The strategies are as follows:\n     *    - Filter alive brokers by 'validPredicate'.\n     *    - Check whether the old master is still valid.\n     *    - If preferBrokerAddr is not empty and valid, select it as master.\n     *    - Otherwise, we will sort the array of 'brokerLiveInfo' according to (epoch, offset, electionPriority), and select the best candidate as the new master.\n     *\n     * @param clusterName       the brokerGroup belongs\n     * @param syncStateBrokers  all broker replicas in syncStateSet\n     * @param allReplicaBrokers all broker replicas\n     * @param oldMaster         old master's broker id\n     * @param preferBrokerId    the broker id prefer to be elected\n     * @return master elected by our own policy\n     */",
        "    /**\n     * 依次在 SyncStateSet 与全副本中尝试选主：\n     * 1. 用 validPredicate 过滤存活副本；\n     * 2. 旧 Master 仍有效且未指定其他优先 ID 则保留；\n     * 3. 否则按 epoch/offset/优先级排序取最优，或随机取一。\n     *\n     * @param clusterName       集群名\n     * @param syncStateBrokers  同步副本 ID 集合\n     * @param allReplicaBrokers 全部副本 ID 集合\n     * @param oldMaster         原 Master brokerId\n     * @param preferBrokerId    优先选举的 brokerId\n     * @return 选出的新 Master brokerId\n     */",
    ),
    (
        "    private Long tryElect(String clusterName, String brokerName, Set<Long> brokers, Long oldMaster,\n        Long preferBrokerId) {",
        "    /** 在给定副本 ID 集合内执行一轮选主逻辑。 */\n    private Long tryElect(String clusterName, String brokerName, Set<Long> brokers, Long oldMaster,\n        Long preferBrokerId) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/helper/BrokerLifecycleListener.java"] = [
    (
        "public interface BrokerLifecycleListener {",
        "/**\n * Broker 生命周期监听器：在 Controller 检测到\n * Broker 不活跃（心跳超时或通道断开）时触发回调。\n */\npublic interface BrokerLifecycleListener {",
    ),
    (
        "    /**\n     * Trigger when broker inactive.\n     */\n    void onBrokerInactive(final String clusterName, final String brokerName, final Long brokerId);",
        "    /**\n     * Broker 变为不活跃时调用。\n     *\n     * @param clusterName 集群名\n     * @param brokerName  Broker 组名\n     * @param brokerId    下线副本 ID，强制选举时可为 null\n     */\n    void onBrokerInactive(final String clusterName, final String brokerName, final Long brokerId);",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/helper/BrokerLiveInfoGetter.java"] = [
    (
        "public interface BrokerLiveInfoGetter {",
        "/**\n * Broker 存活信息查询器：供选主策略获取\n * epoch、maxOffset、electionPriority 等排序字段。\n */\npublic interface BrokerLiveInfoGetter {",
    ),
    (
        "    BrokerLiveInfo get(String clusterName, String brokerName, Long brokerId);",
        "    /**\n     * 按集群与 Broker 身份获取存活详情。\n     *\n     * @param clusterName 集群名\n     * @param brokerName  Broker 组名\n     * @param brokerId    副本 ID\n     * @return 对应 {@link BrokerLiveInfo}\n     */\n    BrokerLiveInfo get(String clusterName, String brokerName, Long brokerId);",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/helper/BrokerValidPredicate.java"] = [
    (
        "public interface BrokerValidPredicate {",
        "/**\n * Broker 选主资格谓词：判断指定副本当前是否\n * 满足被选为 Master 的基本存活条件。\n */\npublic interface BrokerValidPredicate {",
    ),
    (
        "    boolean check(String clusterName, String brokerName, Long brokerId);",
        "    /**\n     * 检查副本是否具备选主资格。\n     *\n     * @param clusterName 集群名\n     * @param brokerName  Broker 组名\n     * @param brokerId    副本 ID\n     * @return 有资格返回 true\n     */\n    boolean check(String clusterName, String brokerName, Long brokerId);",
    ),
]
