"""RocketMQ 5.5.0 wave3a broker ConfigContext/client/auth [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["broker/src/main/java/org/apache/rocketmq/broker/ConfigContext.java"] = [
    (
        "public class ConfigContext {",
        "/**\n * Broker 启动配置上下文：聚合配置文件路径、原始 Properties 以及\n * {@link BrokerConfig}、Netty 与 {@link MessageStoreConfig}、{@link AuthConfig} 等运行时配置对象。\n */\npublic class ConfigContext {",
    ),
    (
        "    private ConfigContext(Builder builder) {",
        "    /** 通过 {@link Builder} 组装不可变配置快照。 */\n    private ConfigContext(Builder builder) {",
    ),
    (
        "    public String getConfigFilePath() {",
        "    /** 返回 broker 配置文件路径。 */\n    public String getConfigFilePath() {",
    ),
    (
        "    public Properties getProperties() {",
        "    /** 返回从配置文件解析出的原始属性集合。 */\n    public Properties getProperties() {",
    ),
    (
        "    public BrokerConfig getBrokerConfig() {",
        "    /** 返回 broker 核心运行参数。 */\n    public BrokerConfig getBrokerConfig() {",
    ),
    (
        "    public NettyServerConfig getNettyServerConfig() {",
        "    /** 返回 Netty 服务端监听与线程池配置。 */\n    public NettyServerConfig getNettyServerConfig() {",
    ),
    (
        "    public NettyClientConfig getNettyClientConfig() {",
        "    /** 返回 Netty 客户端连接与超时配置。 */\n    public NettyClientConfig getNettyClientConfig() {",
    ),
    (
        "    public MessageStoreConfig getMessageStoreConfig() {",
        "    /** 返回消息存储层（CommitLog/ConsumeQueue）配置。 */\n    public MessageStoreConfig getMessageStoreConfig() {",
    ),
    (
        "    public AuthConfig getAuthConfig() {",
        "    /** 返回认证与授权相关配置。 */\n    public AuthConfig getAuthConfig() {",
    ),
    (
        "    public static class Builder {",
        "    /** 流式构建 {@link ConfigContext} 的建造者。 */\n    public static class Builder {",
    ),
    (
        "        public Builder configFilePath(String configFilePath) {",
        "        /** 设置配置文件路径。 */\n        public Builder configFilePath(String configFilePath) {",
    ),
    (
        "        public Builder properties(Properties properties) {",
        "        /** 设置原始配置属性。 */\n        public Builder properties(Properties properties) {",
    ),
    (
        "        public Builder brokerConfig(BrokerConfig brokerConfig) {",
        "        /** 设置 {@link BrokerConfig}。 */\n        public Builder brokerConfig(BrokerConfig brokerConfig) {",
    ),
    (
        "        public Builder nettyServerConfig(NettyServerConfig nettyServerConfig) {",
        "        /** 设置 Netty 服务端配置。 */\n        public Builder nettyServerConfig(NettyServerConfig nettyServerConfig) {",
    ),
    (
        "        public Builder nettyClientConfig(NettyClientConfig nettyClientConfig) {",
        "        /** 设置 Netty 客户端配置。 */\n        public Builder nettyClientConfig(NettyClientConfig nettyClientConfig) {",
    ),
    (
        "        public Builder messageStoreConfig(MessageStoreConfig messageStoreConfig) {",
        "        /** 设置消息存储配置。 */\n        public Builder messageStoreConfig(MessageStoreConfig messageStoreConfig) {",
    ),
    (
        "        public Builder authConfig(AuthConfig authConfig) {",
        "        /** 设置认证授权配置。 */\n        public Builder authConfig(AuthConfig authConfig) {",
    ),
    (
        "        public ConfigContext build() {",
        "        /** 构建不可变 {@link ConfigContext} 实例。 */\n        public ConfigContext build() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/ShutdownHook.java"] = [
    (
        "public interface ShutdownHook {",
        "/**\n * Broker 关闭钩子：在控制器停机流程中插入自定义清理逻辑。\n */\npublic interface ShutdownHook {",
    ),
    (
        "    /**\n     * Code to execute before broker shutdown.\n     *\n     * @param controller broker to shutdown\n     */",
        "    /**\n     * Broker 关闭前执行的钩子逻辑。\n     *\n     * @param controller 待关闭的 broker 控制器\n     */",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/auth/converter/AclConverter.java"] = [
    (
        "public class AclConverter {",
        "/**\n * ACL 模型转换器：在 Remoting 协议体 {@link AclInfo} 与授权域模型 {@link Acl} 之间双向转换。\n */\npublic class AclConverter {",
    ),
    (
        "    public static Acl convertAcl(AclInfo aclInfo) {",
        "    /** 将 {@link AclInfo} 转为内存 {@link Acl}；入参为 null 时返回 null。 */\n    public static Acl convertAcl(AclInfo aclInfo) {",
    ),
    (
        "    public static List<AclInfo> convertAcls(List<Acl> acls) {",
        "    /** 批量将 {@link Acl} 列表转为 {@link AclInfo} 列表。 */\n    public static List<AclInfo> convertAcls(List<Acl> acls) {",
    ),
    (
        "    public static AclInfo convertAcl(Acl acl) {",
        "    /** 将 {@link Acl} 转为 {@link AclInfo}；入参为 null 时返回 null。 */\n    public static AclInfo convertAcl(Acl acl) {",
    ),
    (
        "    private static AclInfo.PolicyInfo convertPolicy(Policy policy) {",
        "    /** 将 {@link Policy} 转为 {@link AclInfo.PolicyInfo}。 */\n    private static AclInfo.PolicyInfo convertPolicy(Policy policy) {",
    ),
    (
        "    private static AclInfo.PolicyEntryInfo convertPolicyEntry(PolicyEntry entry) {",
        "    /** 将 {@link PolicyEntry} 转为 {@link AclInfo.PolicyEntryInfo}。 */\n    private static AclInfo.PolicyEntryInfo convertPolicyEntry(PolicyEntry entry) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/auth/converter/UserConverter.java"] = [
    (
        "public class UserConverter {",
        "/**\n * 用户模型转换器：在 {@link User} 域对象与 Remoting {@link UserInfo} 之间互转。\n */\npublic class UserConverter {",
    ),
    (
        "    public static List<UserInfo> convertUsers(List<User> users) {",
        "    /** 批量将 {@link User} 转为 {@link UserInfo}。 */\n    public static List<UserInfo> convertUsers(List<User> users) {",
    ),
    (
        "    public static UserInfo convertUser(User user) {",
        "    /** 将 {@link User} 转为 {@link UserInfo}，枚举字段输出为名称字符串。 */\n    public static UserInfo convertUser(User user) {",
    ),
    (
        "    public static User convertUser(UserInfo userInfo) {",
        "    /** 将 {@link UserInfo} 还原为 {@link User} 域对象。 */\n    public static User convertUser(UserInfo userInfo) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/auth/pipeline/AuthenticationPipeline.java"] = [
    (
        "public class AuthenticationPipeline implements RequestPipeline {",
        "/**\n * 认证请求管道：在 Remoting 请求进入业务处理器前调用 {@link AuthenticationEvaluator} 校验身份。\n */\npublic class AuthenticationPipeline implements RequestPipeline {",
    ),
    (
        "    public AuthenticationPipeline(AuthConfig authConfig) {",
        "    /** 按 {@link AuthConfig} 创建认证评估器。 */\n    public AuthenticationPipeline(AuthConfig authConfig) {",
    ),
    (
        "    @Override\n    public void execute(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
        "    /** 若启用认证则构建上下文并评估；失败时抛出 {@link AbortProcessException}。 */\n    @Override\n    public void execute(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
    ),
    (
        "    protected AuthenticationContext newContext(ChannelHandlerContext ctx, RemotingCommand request) {",
        "    /** 为当前 Netty 通道与请求构造 {@link AuthenticationContext}。 */\n    protected AuthenticationContext newContext(ChannelHandlerContext ctx, RemotingCommand request) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/auth/pipeline/AuthorizationPipeline.java"] = [
    (
        "public class AuthorizationPipeline implements RequestPipeline {",
        "/**\n * 授权请求管道：在认证通过后对请求资源执行 ACL 策略评估。\n */\npublic class AuthorizationPipeline implements RequestPipeline {",
    ),
    (
        "    public AuthorizationPipeline(AuthConfig authConfig) {",
        "    /** 按 {@link AuthConfig} 创建授权评估器。 */\n    public AuthorizationPipeline(AuthConfig authConfig) {",
    ),
    (
        "    @Override\n    public void execute(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
        "    /** 若启用授权则构建上下文列表并评估；权限不足时抛出 {@link AbortProcessException}。 */\n    @Override\n    public void execute(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
    ),
    (
        "    protected List<AuthorizationContext> newContexts(ChannelHandlerContext ctx, RemotingCommand request) {",
        "    /** 为当前请求解析出待评估的 {@link AuthorizationContext} 列表。 */\n    protected List<AuthorizationContext> newContexts(ChannelHandlerContext ctx, RemotingCommand request) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/ClientChannelAttributeHelper.java"] = [
    (
        "public class ClientChannelAttributeHelper {",
        "/**\n * Netty 通道属性辅助类：在同一 {@link Channel} 上记录该连接关联的生产者/消费者组名。\n * 组名以 {@code |} 分隔存储，便于单连接多组场景。\n */\npublic class ClientChannelAttributeHelper {",
    ),
    (
        "    public static void addProducerGroup(Channel channel, String group) {",
        "    /** 向通道追加生产者组名（去重）。 */\n    public static void addProducerGroup(Channel channel, String group) {",
    ),
    (
        "    public static void addConsumerGroup(Channel channel, String group) {",
        "    /** 向通道追加消费者组名（去重）。 */\n    public static void addConsumerGroup(Channel channel, String group) {",
    ),
    (
        "    public static List<String> getProducerGroups(Channel channel) {",
        "    /** 读取通道上已登记的生产者组列表。 */\n    public static List<String> getProducerGroups(Channel channel) {",
    ),
    (
        "    public static List<String> getConsumerGroups(Channel channel) {",
        "    /** 读取通道上已登记的消费者组列表。 */\n    public static List<String> getConsumerGroups(Channel channel) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/ClientChannelInfo.java"] = [
    (
        "public class ClientChannelInfo {",
        "/**\n * 客户端通道元数据：绑定 Netty {@link Channel} 与 clientId、语言、协议版本及心跳时间戳。\n */\npublic class ClientChannelInfo {",
    ),
    (
        "    public ClientChannelInfo(Channel channel) {",
        "    /** 仅绑定通道，其余字段使用默认值。 */\n    public ClientChannelInfo(Channel channel) {",
    ),
    (
        "    public ClientChannelInfo(Channel channel, String clientId, LanguageCode language, int version) {",
        "    /** 构造完整客户端通道描述。 */\n    public ClientChannelInfo(Channel channel, String clientId, LanguageCode language, int version) {",
    ),
    (
        "    public Channel getChannel() {",
        "    /** 返回底层 Netty 通道。 */\n    public Channel getChannel() {",
    ),
    (
        "    public String getClientId() {",
        "    /** 返回客户端唯一标识。 */\n    public String getClientId() {",
    ),
    (
        "    public LanguageCode getLanguage() {",
        "    /** 返回客户端 SDK 语言类型。 */\n    public LanguageCode getLanguage() {",
    ),
    (
        "    public int getVersion() {",
        "    /** 返回 Remoting 协议版本号。 */\n    public int getVersion() {",
    ),
    (
        "    public long getLastUpdateTimestamp() {",
        "    /** 返回最近一次心跳或注册更新时间戳（毫秒）。 */\n    public long getLastUpdateTimestamp() {",
    ),
    (
        "    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {",
        "    /** 更新最近活跃时间戳。 */\n    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/ClientHousekeepingService.java"] = [
    (
        "public class ClientHousekeepingService implements ChannelEventListener {",
        "/**\n * 客户端连接 housekeeping：定时扫描非活跃通道，并在 Netty 连接生命周期事件中清理生产者/消费者状态。\n */\npublic class ClientHousekeepingService implements ChannelEventListener {",
    ),
    (
        "    public ClientHousekeepingService(final BrokerController brokerController) {",
        "    /** 绑定 {@link BrokerController} 并创建定时扫描线程池。 */\n    public ClientHousekeepingService(final BrokerController brokerController) {",
    ),
    (
        "    public void start() {",
        "    /** 启动定时任务，每 10 秒扫描一次异常/非活跃客户端通道。 */\n    public void start() {",
    ),
    (
        "    private void scanExceptionChannel() {",
        "    /** 委托生产者与消费者管理器扫描并清理非活跃通道。 */\n    private void scanExceptionChannel() {",
    ),
    (
        "    public void shutdown() {",
        "    /** 关闭定时线程池。 */\n    public void shutdown() {",
    ),
    (
        "    @Override\n    public void onChannelConnect(String remoteAddr, Channel channel) {",
        "    /** 新连接建立时递增连接统计。 */\n    @Override\n    public void onChannelConnect(String remoteAddr, Channel channel) {",
    ),
    (
        "    @Override\n    public void onChannelClose(String remoteAddr, Channel channel) {",
        "    /** 连接正常关闭时清理客户端注册并更新统计。 */\n    @Override\n    public void onChannelClose(String remoteAddr, Channel channel) {",
    ),
    (
        "    @Override\n    public void onChannelException(String remoteAddr, Channel channel) {",
        "    /** 连接异常时按关闭流程清理客户端状态。 */\n    @Override\n    public void onChannelException(String remoteAddr, Channel channel) {",
    ),
    (
        "    @Override\n    public void onChannelIdle(String remoteAddr, Channel channel) {",
        "    /** 连接空闲超时时清理客户端状态。 */\n    @Override\n    public void onChannelIdle(String remoteAddr, Channel channel) {",
    ),
    (
        "    @Override\n    public void onChannelActive(String remoteAddr, Channel channel) {",
        "    /** 通道变为 active 时的占位回调（当前无额外逻辑）。 */\n    @Override\n    public void onChannelActive(String remoteAddr, Channel channel) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/ConsumerGroupEvent.java"] = [
    (
        "public enum ConsumerGroupEvent {",
        "/**\n * 消费者组生命周期事件：供 {@link ConsumerIdsChangeListener} 等组件响应组内成员变化。\n */\npublic enum ConsumerGroupEvent {",
    ),
    (
        "    /**\n     * Some consumers in the group are changed.\n     */",
        "    /** 组内消费者成员或订阅发生变化。 */",
    ),
    (
        "    /**\n     * The group of consumer is unregistered.\n     */",
        "    /** 整个消费者组被注销。 */",
    ),
    (
        "    /**\n     * The group of consumer is registered.\n     */",
        "    /** 消费者组完成注册。 */",
    ),
    (
        "    /**\n     * The client of this consumer is new registered.\n     */",
        "    /** 组内某个客户端新注册上线。 */",
    ),
    (
        "    /**\n     * The client of this consumer is unregistered.\n     */",
        "    /** 组内某个客户端下线注销。 */",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/ConsumerGroupInfo.java"] = [
    (
        "public class ConsumerGroupInfo {",
        "/**\n * 单个消费者组的运行时视图：维护组内通道、订阅关系及消费模式等元数据。\n */\npublic class ConsumerGroupInfo {",
    ),
    (
        "    public ConsumerGroupInfo(String groupName, ConsumeType consumeType, MessageModel messageModel,\n        ConsumeFromWhere consumeFromWhere) {",
        "    /** 创建带完整消费参数的消费者组信息。 */\n    public ConsumerGroupInfo(String groupName, ConsumeType consumeType, MessageModel messageModel,\n        ConsumeFromWhere consumeFromWhere) {",
    ),
    (
        "    public ConsumerGroupInfo(String groupName) {",
        "    /** 仅指定组名，消费参数后续由 {@link #updateChannel} 填充。 */\n    public ConsumerGroupInfo(String groupName) {",
    ),
    (
        "    public ClientChannelInfo findChannel(final String clientId) {",
        "    /** 按 clientId 查找组内通道信息。 */\n    public ClientChannelInfo findChannel(final String clientId) {",
    ),
    (
        "    public ConcurrentMap<String, SubscriptionData> getSubscriptionTable() {",
        "    /** 返回 topic → {@link SubscriptionData} 订阅表。 */\n    public ConcurrentMap<String, SubscriptionData> getSubscriptionTable() {",
    ),
    (
        "    public ClientChannelInfo findChannel(final Channel channel) {",
        "    /** 按 Netty 通道查找客户端信息。 */\n    public ClientChannelInfo findChannel(final Channel channel) {",
    ),
    (
        "    public ConcurrentMap<Channel, ClientChannelInfo> getChannelInfoTable() {",
        "    /** 返回通道 → 客户端信息的并发映射表。 */\n    public ConcurrentMap<Channel, ClientChannelInfo> getChannelInfoTable() {",
    ),
    (
        "    public List<Channel> getAllChannel() {",
        "    /** 返回组内全部 Netty 通道列表。 */\n    public List<Channel> getAllChannel() {",
    ),
    (
        "    public List<String> getAllClientId() {",
        "    /** 返回组内全部 clientId 列表。 */\n    public List<String> getAllClientId() {",
    ),
    (
        "    public boolean unregisterChannel(final ClientChannelInfo clientChannelInfo) {",
        "    /** 主动注销指定客户端通道；成功移除时返回 true。 */\n    public boolean unregisterChannel(final ClientChannelInfo clientChannelInfo) {",
    ),
    (
        "    public ClientChannelInfo doChannelCloseEvent(final String remoteAddr, final Channel channel) {",
        "    /** Netty 连接关闭/异常时移除通道并记录日志。 */\n    public ClientChannelInfo doChannelCloseEvent(final String remoteAddr, final Channel channel) {",
    ),
    (
        "    /**\n     * Update {@link #channelInfoTable} in {@link ConsumerGroupInfo}\n     *\n     * @param infoNew Channel info of new client.\n     * @param consumeType consume type of new client.\n     * @param messageModel message consuming model (CLUSTERING/BROADCASTING) of new client.\n     * @param consumeFromWhere indicate the position when the client consume message firstly.\n     * @return the result that if new connector is connected or not.\n     */",
        "    /**\n     * 更新 {@link #channelInfoTable} 中的客户端通道，并同步组级消费参数。\n     *\n     * @param infoNew 新客户端的通道信息\n     * @param consumeType 新客户端的消费类型\n     * @param messageModel 新客户端的消息模式（CLUSTERING/BROADCASTING）\n     * @param consumeFromWhere 客户端首次消费时的起始位点策略\n     * @return 是否新增了连接（true 表示有新客户端接入）\n     */",
    ),
    (
        "    /**\n     * Update subscription.\n     *\n     * @param subList set of {@link SubscriptionData}\n     * @return the boolean indicates the subscription has changed or not.\n     */",
        "    /**\n     * 批量更新订阅关系：新增、升级版本或移除不再订阅的 topic。\n     *\n     * @param subList {@link SubscriptionData} 集合\n     * @return 订阅表是否发生变更\n     */",
    ),
    (
        "    public Set<String> getSubscribeTopics() {",
        "    /** 返回当前已订阅的全部 topic 名称。 */\n    public Set<String> getSubscribeTopics() {",
    ),
    (
        "    public SubscriptionData findSubscriptionData(final String topic) {",
        "    /** 按 topic 查找订阅详情。 */\n    public SubscriptionData findSubscriptionData(final String topic) {",
    ),
    (
        "    public ConsumeType getConsumeType() {",
        "    /** 返回组级消费类型。 */\n    public ConsumeType getConsumeType() {",
    ),
    (
        "    public void setConsumeType(ConsumeType consumeType) {",
        "    /** 设置组级消费类型。 */\n    public void setConsumeType(ConsumeType consumeType) {",
    ),
    (
        "    public MessageModel getMessageModel() {",
        "    /** 返回组级消息模式。 */\n    public MessageModel getMessageModel() {",
    ),
    (
        "    public void setMessageModel(MessageModel messageModel) {",
        "    /** 设置组级消息模式。 */\n    public void setMessageModel(MessageModel messageModel) {",
    ),
    (
        "    public String getGroupName() {",
        "    /** 返回消费者组名。 */\n    public String getGroupName() {",
    ),
    (
        "    public long getLastUpdateTimestamp() {",
        "    /** 返回组信息最近更新时间戳。 */\n    public long getLastUpdateTimestamp() {",
    ),
    (
        "    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {",
        "    /** 设置组信息最近更新时间戳。 */\n    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {",
    ),
    (
        "    public ConsumeFromWhere getConsumeFromWhere() {",
        "    /** 返回首次消费起始位点策略。 */\n    public ConsumeFromWhere getConsumeFromWhere() {",
    ),
    (
        "    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {",
        "    /** 设置首次消费起始位点策略。 */\n    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/ConsumerIdsChangeListener.java"] = [
    (
        "public interface ConsumerIdsChangeListener {",
        "/**\n * 消费者 ID 变更监听器：响应 {@link ConsumerGroupEvent} 以同步过滤规则或通知客户端重平衡。\n */\npublic interface ConsumerIdsChangeListener {",
    ),
    (
        "    void handle(ConsumerGroupEvent event, String group, Object... args);",
        "    /** 处理指定组的事件；args 含义随 event 类型变化。 */\n    void handle(ConsumerGroupEvent event, String group, Object... args);",
    ),
    (
        "    void shutdown();",
        "    /** 释放监听器持有的后台资源。 */\n    void shutdown();",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/DefaultConsumerIdsChangeListener.java"] = [
    (
        "public class DefaultConsumerIdsChangeListener implements ConsumerIdsChangeListener {",
        "/**\n * {@link ConsumerIdsChangeListener} 默认实现：维护消费者组变更通知队列，\n * 支持实时推送或定时批量通知客户端 consumerId 变化。\n */\npublic class DefaultConsumerIdsChangeListener implements ConsumerIdsChangeListener {",
    ),
    (
        "    public DefaultConsumerIdsChangeListener(BrokerController brokerController) {",
        "    /** 绑定 broker 并启动定时通知任务（初始延迟 30s，间隔 15s）。 */\n    public DefaultConsumerIdsChangeListener(BrokerController brokerController) {",
    ),
    (
        "    @Override\n    public void handle(ConsumerGroupEvent event, String group, Object... args) {",
        "    /** 按事件类型更新过滤管理器或向组内通道推送 consumerId 变更。 */\n    @Override\n    public void handle(ConsumerGroupEvent event, String group, Object... args) {",
    ),
    (
        "    private void notifyConsumerChange() {",
        "    /** 定时任务：冲刷缓存的组→通道映射并调用 {@code notifyConsumerIdsChanged}。 */\n    private void notifyConsumerChange() {",
    ),
    (
        "    @Override\n    public void shutdown() {",
        "    /** 关闭定时通知线程池。 */\n    @Override\n    public void shutdown() {",
    ),
    (
        "    private static class NotifyTaskControl {",
        "    /** 实时通知任务控制块：支持被新任务中断以避免重复推送。 */\n    private static class NotifyTaskControl {",
    ),
    (
        "        public NotifyTaskControl(List<Channel> channels) {",
        "        /** 绑定待通知的通道列表。 */\n        public NotifyTaskControl(List<Channel> channels) {",
    ),
    (
        "        public boolean isInterrupted() {",
        "        /** 当前通知任务是否已被更新的任务取代。 */\n        public boolean isInterrupted() {",
    ),
    (
        "        public void interrupt() {",
        "        /** 标记任务为已中断。 */\n        public void interrupt() {",
    ),
    (
        "        public List<Channel> getChannels() {",
        "        /** 返回待通知通道列表。 */\n        public List<Channel> getChannels() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/ProducerChangeListener.java"] = [
    (
        "/**\n * producer manager will call this listener when something happen\n * <p>\n * event type: {@link ProducerGroupEvent}\n */",
        "/**\n * 生产者变更监听器：{@code ProducerManager} 在组或客户端状态变化时回调。\n * <p>\n * 事件类型见 {@link ProducerGroupEvent}。\n */",
    ),
    (
        "    void handle(ProducerGroupEvent event, String group, ClientChannelInfo clientChannelInfo);",
        "    /** 处理指定生产者组事件及关联客户端通道信息。 */\n    void handle(ProducerGroupEvent event, String group, ClientChannelInfo clientChannelInfo);",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/ProducerGroupEvent.java"] = [
    (
        "public enum ProducerGroupEvent {",
        "/**\n * 生产者组生命周期事件：供 {@link ProducerChangeListener} 响应组注销或客户端下线。\n */\npublic enum ProducerGroupEvent {",
    ),
    (
        "    /**\n     * The group of producer is unregistered.\n     */",
        "    /** 整个生产者组被注销。 */",
    ),
    (
        "    /**\n     * The client of this producer is unregistered.\n     */",
        "    /** 组内某个生产者客户端下线注销。 */",
    ),
]
