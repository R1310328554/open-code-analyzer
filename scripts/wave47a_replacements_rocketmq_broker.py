"""Chinese JavaDoc replacements for RocketMQ wave47a broker/auth [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "auth/src/main/java/org/apache/rocketmq/auth/authorization/builder/DefaultAuthorizationContextBuilder.java": [
        (
            "public class DefaultAuthorizationContextBuilder implements AuthorizationContextBuilder {",
            "/**\n * 默认授权上下文构建器：从 gRPC 或 Remoting 请求解析 Topic、ConsumerGroup 与所需 {@link Action}。\n * <p>覆盖发送、拉取、心跳、位点管理等常见请求的权限资源提取。\n */\npublic class DefaultAuthorizationContextBuilder implements AuthorizationContextBuilder {",
        ),
        (
            "    public DefaultAuthorizationContextBuilder(AuthConfig authConfig) {",
            "    /** 绑定认证配置并初始化请求头注册表。 */\n    public DefaultAuthorizationContextBuilder(AuthConfig authConfig) {",
        ),
        (
            "    @Override\n    public List<DefaultAuthorizationContext> build(Metadata metadata, GeneratedMessageV3 message) {",
            "    /** 从 gRPC {@link Metadata} 与 Protobuf 消息构建授权上下文列表。 */\n    @Override\n    public List<DefaultAuthorizationContext> build(Metadata metadata, GeneratedMessageV3 message) {",
        ),
        (
            "    @Override\n    public List<DefaultAuthorizationContext> build(ChannelHandlerContext context, RemotingCommand command) {",
            "    /** 从 Netty 通道与 Remoting 命令按 RequestCode 分支构建授权上下文。 */\n    @Override\n    public List<DefaultAuthorizationContext> build(ChannelHandlerContext context, RemotingCommand command) {",
        ),
        (
            "            switch (command.getCode()) {",
            "            // 按请求码解析 Topic/Group 与 PUB/SUB/GET 权限\n            switch (command.getCode()) {",
        ),
        (
            "                case RequestCode.SEND_MESSAGE:",
            "                // 发送消息：重试 Topic 映射为 Group 订阅权限\n                case RequestCode.SEND_MESSAGE:",
        ),
        (
            "                case RequestCode.PULL_MESSAGE:",
            "                // 拉取消息：Topic 与 ConsumerGroup 均需 SUB 权限\n                case RequestCode.PULL_MESSAGE:",
        ),
        (
            "                case RequestCode.HEART_BEAT:",
            "                // 心跳：遍历 ConsumerData 与订阅 Topic 列表\n                case RequestCode.HEART_BEAT:",
        ),
        (
            "                            if (NamespaceUtil.isRetryTopic(subscriptionData.getTopic())) {",
            "                            // 重试 Topic 已在 Group 维度授权，跳过\n                            if (NamespaceUtil.isRetryTopic(subscriptionData.getTopic())) {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/BrokerController.java": [
        (
            "public class BrokerController {",
            "/**\n * Broker 核心控制器：聚合消息存储、客户端管理、Remoting 服务与各类 Processor。\n * <p>负责初始化元数据、注册请求处理器、启动/关闭 Broker 生命周期及 Controller 模式副本管理。\n */\npublic class BrokerController {",
        ),
        (
            "    public boolean initializeMetadata() {",
            "    /** 加载 Topic、订阅组、消费位点、过滤规则等 Broker 元数据。 */\n    public boolean initializeMetadata() {",
        ),
        (
            "    public boolean initializeMessageStore() {",
            "    /** 创建 {@link MessageStore}（含 RocksDB/DLedger/定时/事务扩展）并注册分发钩子。 */\n    public boolean initializeMessageStore() {",
        ),
        (
            "            // Load store plugin",
            "            // 加载 MessageStore 插件链",
        ),
        (
            "    public boolean initialize() throws CloneNotSupportedException {",
            "    /** 依次初始化元数据、消息存储并恢复服务组件。 */\n    public boolean initialize() throws CloneNotSupportedException {",
        ),
        (
            "    public boolean recoverAndInitService() throws CloneNotSupportedException {",
            "    /** 恢复存储、启动副本管理器、注册 Processor 并初始化 Lite/Pop 等扩展服务。 */\n    public boolean recoverAndInitService() throws CloneNotSupportedException {",
        ),
        (
            "    public void registerProcessor() {",
            "    /** 向 TCP/FAST Remoting 服务器注册各 RequestCode 对应的 Processor 与线程池。 */\n    public void registerProcessor() {",
        ),
        (
            "        /*\n         * SendMessageProcessor\n         */",
            "        /* 发送/批量/回退/召回消息处理器 */",
        ),
        (
            "        /**\n         * PullMessageProcessor\n         */",
            "        /** 拉取与 Lite 拉取消息处理器 */",
        ),
        (
            "        /**\n         * PopMessageProcessor\n         */",
            "        /** Pop 与 Pop-Lite 消息处理器 */",
        ),
        (
            "        /**\n         * AckMessageProcessor\n         */",
            "        /** Pop 确认与批量确认处理器 */",
        ),
        (
            "    public void protectBroker() {",
            "    /** 检测消费落后超过阈值的 Group 并禁用其消费。 */\n    public void protectBroker() {",
        ),
        (
            "    public long headSlowTimeMills(BlockingQueue<Runnable> q) {",
            "    /** 返回线程池队列队首任务等待时间（毫秒），用于流控与水位监控。 */\n    public long headSlowTimeMills(BlockingQueue<Runnable> q) {",
        ),
        (
            "    public void shutdown() {",
            "    /** 关闭 Remoting、存储、长轮询、Pop、事务及定时任务等全部 Broker 服务。 */\n    public void shutdown() {",
        ),
        (
            "    public void start() throws Exception {",
            "    /** 启动 Broker：注册 NameServer、调度心跳与同步任务并启动各后台服务。 */\n    public void start() throws Exception {",
        ),
        (
            "    public MessageStore getMessageStore() {",
            "    /** 返回消息存储实例。 */\n    public MessageStore getMessageStore() {",
        ),
        (
            "    public ConsumerManager getConsumerManager() {",
            "    /** 返回 Consumer 客户端管理器。 */\n    public ConsumerManager getConsumerManager() {",
        ),
        (
            "    public PopConsumerService getPopConsumerService() {",
            "    /** 返回 Pop 消费服务。 */\n    public PopConsumerService getPopConsumerService() {",
        ),
        (
            "        // Set RemotingMetricsManager on both remoting servers",
            "        // 为 TCP 与 FAST 两套 Remoting 服务器设置指标管理器",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/client/ConsumerManager.java": [
        (
            "public class ConsumerManager {",
            "/**\n * Consumer 客户端管理器：维护消费组与 Netty 通道映射、订阅关系及补偿表。\n * <p>负责注册/注销 Consumer、扫描过期通道并通知 {@link ConsumerIdsChangeListener}。\n */\npublic class ConsumerManager {",
        ),
        (
            "    public ConsumerManager(final ConsumerIdsChangeListener consumerIdsChangeListener, long expiredTimeout) {",
            "    /** 测试用构造：仅绑定 ID 变更监听器与过期超时。 */\n    public ConsumerManager(final ConsumerIdsChangeListener consumerIdsChangeListener, long expiredTimeout) {",
        ),
        (
            "    public ClientChannelInfo findChannel(final String group, final String clientId) {",
            "    /** 按消费组与 clientId 查找客户端通道信息。 */\n    public ClientChannelInfo findChannel(final String group, final String clientId) {",
        ),
        (
            "    public SubscriptionData findSubscriptionData(final String group, final String topic) {",
            "    /** 查询消费组对 Topic 的订阅数据（含补偿表回退）。 */\n    public SubscriptionData findSubscriptionData(final String group, final String topic) {",
        ),
        (
            "    public boolean registerConsumer(final String group, final ClientChannelInfo clientChannelInfo,",
            "    /** 注册 Consumer 客户端及其订阅关系，并更新 Topic-Group 索引。 */\n    public boolean registerConsumer(final String group, final ClientChannelInfo clientChannelInfo,",
        ),
        (
            "    public void unregisterConsumer(final String group, final ClientChannelInfo clientChannelInfo,",
            "    /** 注销 Consumer 客户端并清理空消费组。 */\n    public void unregisterConsumer(final String group, final ClientChannelInfo clientChannelInfo,",
        ),
        (
            "    public void removeExpireConsumerGroupInfo() {",
            "    /** 移除长时间无活跃通道的消费组信息。 */\n    public void removeExpireConsumerGroupInfo() {",
        ),
        (
            "    public void scanNotActiveChannel() {",
            "    /** 扫描并关闭超时的非活跃 Consumer 通道。 */\n    public void scanNotActiveChannel() {",
        ),
        (
            "    public HashSet<String> queryTopicConsumeByWho(final String topic) {",
            "    /** 查询订阅指定 Topic 的全部消费组名称。 */\n    public HashSet<String> queryTopicConsumeByWho(final String topic) {",
        ),
        (
            "    public void compensateBasicConsumerInfo(String group, ConsumeType consumeType, MessageModel messageModel) {",
            "    /** 向补偿表写入消费组基础信息（消费类型与消息模型）。 */\n    public void compensateBasicConsumerInfo(String group, ConsumeType consumeType, MessageModel messageModel) {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/config/v1/RocksDBSubscriptionGroupManager.java": [
        (
            "public class RocksDBSubscriptionGroupManager extends SubscriptionGroupManager {",
            "/**\n * 基于 RocksDB 的订阅组配置管理器（v1 配置存储）。\n * <p>持久化 {@link SubscriptionGroupConfig}、禁止消费位图及数据版本。\n */\npublic class RocksDBSubscriptionGroupManager extends SubscriptionGroupManager {",
        ),
        (
            "    public RocksDBSubscriptionGroupManager(BrokerController brokerController, boolean useSingleRocksDB,",
            "    /** 指定是否共用单 RocksDB 实例及存储根目录。 */\n    public RocksDBSubscriptionGroupManager(BrokerController brokerController, boolean useSingleRocksDB,",
        ),
        (
            "    public boolean load() {",
            "    /** 加载数据版本、订阅组配置与禁止消费规则。 */\n    public boolean load() {",
        ),
        (
            "    public boolean loadSubscriptionGroupAndForbidden() {",
            "    /** 扫描 RocksDB 列族加载全部订阅组与禁止消费条目。 */\n    public boolean loadSubscriptionGroupAndForbidden() {",
        ),
        (
            "    public SubscriptionGroupConfig putSubscriptionGroupConfig(SubscriptionGroupConfig subscriptionGroupConfig) {",
            "    /** 写入或更新订阅组配置到 RocksDB 并刷新内存表。 */\n    public SubscriptionGroupConfig putSubscriptionGroupConfig(SubscriptionGroupConfig subscriptionGroupConfig) {",
        ),
        (
            "    public synchronized void persist() {",
            "    /** 强制刷盘 RocksDB WAL，保证元数据持久化。 */\n    public synchronized void persist() {",
        ),
        (
            "    public synchronized void exportToJson() {",
            "    /** 将订阅组配置导出为 JSON 快照文件。 */\n    public synchronized void exportToJson() {",
        ),
        (
            "    public void updateForbidden(String group, String topic, int forbiddenIndex, boolean setOrClear) {",
            "    /** 设置或清除指定 Group 对 Topic 某队列的禁止消费位。 */\n    public void updateForbidden(String group, String topic, int forbiddenIndex, boolean setOrClear) {",
        ),
        (
            "    public void setForbidden(String group, String topic, int forbiddenIndex) {",
            "    /** 禁止消费组对 Topic 指定队列的消费。 */\n    public void setForbidden(String group, String topic, int forbiddenIndex) {",
        ),
        (
            "    public void clearForbidden(String group, String topic, int forbiddenIndex) {",
            "    /** 清除禁止消费位，恢复该队列消费。 */\n    public void clearForbidden(String group, String topic, int forbiddenIndex) {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/config/v2/ConsumerOffsetManagerV2.java": [
        (
            "/**\n * <p>\n * Layout of consumer offset key:\n * [table-prefix, 1 byte][table-id, 2 bytes][record-prefix, 1 byte][group-len, 2 bytes][group bytes][CTRL_1, 1 byte]\n * [topic-len, 2 bytes][topic bytes][CTRL_1, 1 byte][queue-id, 4 bytes]\n * </p>\n *\n * <p>\n * Layout of consumer offset value: [offset, 8 bytes]\n * </p>\n */",
            "/**\n * 基于 RocksDB {@link ConfigStorage} 的消费位点管理器（v2 配置存储）。\n * <p>\n * 位点键布局：表前缀(1) + 表 ID(2) + 记录前缀(1) + group 长度(2) + group + 分隔符(1)\n * + topic 长度(2) + topic + 分隔符(1) + queueId(4)\n * </p>\n * <p>\n * 位点值布局：offset(8 字节)\n * </p>\n */",
        ),
        (
            "    public ConsumerOffsetManagerV2(BrokerController brokerController, ConfigStorage configStorage) {",
            "    /** 绑定 Broker 控制器与 v2 配置存储。 */\n    public ConsumerOffsetManagerV2(BrokerController brokerController, ConfigStorage configStorage) {",
        ),
        (
            "    public void commitOffset(String clientHost, String group, String topic, int queueId, long offset) {",
            "    /** 提交 Push 消费位点到 RocksDB 并更新内存缓存。 */\n    public void commitOffset(String clientHost, String group, String topic, int queueId, long offset) {",
        ),
        (
            "    public long queryOffset(String group, String topic, int queueId) {",
            "    /** 查询 Push 消费位点，不存在时返回 -1。 */\n    public long queryOffset(String group, String topic, int queueId) {",
        ),
        (
            "    public void commitPullOffset(String clientHost, String group, String topic, int queueId, long offset) {",
            "    /** 提交 Pull 消费位点。 */\n    public void commitPullOffset(String clientHost, String group, String topic, int queueId, long offset) {",
        ),
        (
            "    public long queryPullOffset(String group, String topic, int queueId) {",
            "    /** 查询 Pull 消费位点。 */\n    public long queryPullOffset(String group, String topic, int queueId) {",
        ),
        (
            "    public boolean load() {",
            "    /** 从 RocksDB 加载数据版本与全部消费位点。 */\n    public boolean load() {",
        ),
        (
            "    public synchronized void persist() {",
            "    /** 强制刷 WAL，持久化位点变更。 */\n    public synchronized void persist() {",
        ),
        (
            "    public void assignResetOffset(String topic, String group, int queueId, long offset) {",
            "    /** 管理员指定重置位点（写入 reset 表）。 */\n    public void assignResetOffset(String topic, String group, int queueId, long offset) {",
        ),
        (
            "    public void removeOffset(String group) {",
            "    /** 删除指定消费组的全部位点记录。 */\n    public void removeOffset(String group) {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/controller/ReplicasManager.java": [
        (
            "public class ReplicasManager {",
            "/**\n * Controller 模式副本管理器：Broker 与 Controller 集群交互，处理主从角色切换与同步状态集。\n * <p>负责注册、心跳、epoch 变更及 Master/Slave 角色迁移。\n */\npublic class ReplicasManager {",
        ),
        (
            "    public ReplicasManager(final BrokerController brokerController) {",
            "    /** 绑定 Broker 控制器并初始化 HA 服务与元数据路径。 */\n    public ReplicasManager(final BrokerController brokerController) {",
        ),
        (
            "    public void start() {",
            "    /** 启动副本管理：同步 Controller 元数据、注册 Broker 并调度心跳。 */\n    public void start() {",
        ),
        (
            "    public void shutdown() {",
            "    /** 关闭调度与执行线程池，标记状态为 SHUTDOWN。 */\n    public void shutdown() {",
        ),
        (
            "    public synchronized void changeBrokerRole(final Long newMasterBrokerId, final String newMasterAddress,",
            "    /** Controller 通知角色变更：切换为 Master 或 Slave 并重新注册。 */\n    public synchronized void changeBrokerRole(final Long newMasterBrokerId, final String newMasterAddress,",
        ),
        (
            "    public void changeToMaster(final int newMasterEpoch, final int syncStateSetEpoch, final Set<Long> syncStateSet) throws Exception {",
            "    /** 切换为 Master：更新 epoch、同步状态集并启动 Master 服务。 */\n    public void changeToMaster(final int newMasterEpoch, final int syncStateSetEpoch, final Set<Long> syncStateSet) throws Exception {",
        ),
        (
            "    public void changeToSlave(final String newMasterAddress, final int newMasterEpoch, Long newMasterBrokerId) {",
            "    /** 切换为 Slave：连接新 Master 并停止 Master 专属服务。 */\n    public void changeToSlave(final String newMasterAddress, final int newMasterEpoch, Long newMasterBrokerId) {",
        ),
        (
            "    public void registerBrokerWhenRoleChange() {",
            "    /** 角色变更后向 NameServer 重新注册 Broker 路由信息。 */\n    public void registerBrokerWhenRoleChange() {",
        ),
        (
            "    public void sendHeartbeatToController() {",
            "    /** 向 Controller 发送心跳并上报同步状态。 */\n    public void sendHeartbeatToController() {",
        ),
        (
            "    public boolean isMasterState() {",
            "    /** 当前 Broker 是否处于 Master 角色。 */\n    public boolean isMasterState() {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/filter/ConsumerFilterManager.java": [
        (
            "/**\n * Consumer filter data manager.Just manage the consumers use expression filter.\n */",
            "/**\n * Consumer 表达式过滤数据管理器：维护使用 SQL92/属性过滤的订阅 Bloom 位图与表达式。\n * <p>仅管理非 Tag 类型的表达式过滤 Consumer。\n */",
        ),
        (
            "    public ConsumerFilterManager(BrokerController brokerController) {",
            "    /** 绑定 Broker 并按配置初始化 Bloom 过滤器参数。 */\n    public ConsumerFilterManager(BrokerController brokerController) {",
        ),
        (
            "    /**\n     * Build consumer filter data.Be care, bloom filter data is not included.\n     *\n     * @return maybe null\n     */",
            "    /**\n     * 构建 Consumer 过滤数据（不含 Bloom 位图）。\n     *\n     * @return 过滤数据，Tag 类型或表达式无效时可能为 null\n     */",
        ),
        (
            "    public void register(final String consumerGroup, final Collection<SubscriptionData> subList) {",
            "    /** 批量注册消费组的表达式订阅过滤数据。 */\n    public void register(final String consumerGroup, final Collection<SubscriptionData> subList) {",
        ),
        (
            "    public boolean register(final String topic, final String consumerGroup, final String expression,",
            "    /** 注册单个 Topic 的表达式过滤并更新 Bloom 位图。 */\n    public boolean register(final String topic, final String consumerGroup, final String expression,",
        ),
        (
            "    public void unRegister(final String consumerGroup) {",
            "    /** 注销消费组的全部过滤数据。 */\n    public void unRegister(final String consumerGroup) {",
        ),
        (
            "    public ConsumerFilterData get(final String topic, final String consumerGroup) {",
            "    /** 查询指定 Topic 与消费组的过滤数据。 */\n    public ConsumerFilterData get(final String topic, final String consumerGroup) {",
        ),
        (
            "    public BloomFilter getBloomFilter() {",
            "    /** 返回 CommitLog 扩展位图使用的 Bloom 过滤器。 */\n    public BloomFilter getBloomFilter() {",
        ),
        (
            "    public void clean() {",
            "    /** 清理过期（24 小时未更新）的过滤数据。 */\n    public void clean() {",
        ),
        (
            "        // just for test",
            "        // 仅用于单元测试",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/lite/LiteEventDispatcher.java": [
        (
            "public class LiteEventDispatcher extends ServiceThread {",
            "/**\n * Lite 事件分发器：消息到达时将 LMQ 事件推送给订阅客户端。\n * <p>支持共享消费、通配符 Group 全量分发及客户端黑名单防抖。\n */\npublic class LiteEventDispatcher extends ServiceThread {",
        ),
        (
            "    public LiteEventDispatcher(BrokerController brokerController,",
            "    /** 绑定 Broker、Lite 订阅注册表与生命周期管理器。 */\n    public LiteEventDispatcher(BrokerController brokerController,",
        ),
        (
            "    public void init() {",
            "    /** 注册 Lite 订阅变更监听器。 */\n    public void init() {",
        ),
        (
            "    /**\n     * If event mode is enabled, try to dispatch event to one client when message arriving or available.\n     * In most cases, there is only one subscriber for a LMQ under a consumer group,\n     * but also supports multiple clients consuming in share mode.\n     * When group is null, dispatch to all subscribers regardless of their group,\n     * when group is specified, only dispatch to subscribers belonging to this group.\n     * <p>\n     * If the expected number of subscriptions by each client is small, disabling event mode can be a choice.\n     */",
            "    /**\n     * 启用事件模式时，消息到达或可用后将事件分发给一个客户端。\n     * <p>通常每个 LMQ 在消费组下仅一个订阅者，也支持多客户端共享消费。\n     * group 为 null 时分发给全部订阅者；指定 group 时仅分发给该组成员。\n     * <p>客户端订阅数较少时可关闭事件模式以简化路径。\n     */",
        ),
        (
            "    public boolean selectAndDispatch(String lmqName, List<ClientGroup> clients, String excludeClientId) {",
            "    /** 从候选客户端中选取一个并尝试分发事件（可排除指定 clientId）。 */\n    public boolean selectAndDispatch(String lmqName, List<ClientGroup> clients, String excludeClientId) {",
        ),
        (
            "    public boolean tryDispatchToClient(String lmqName, String clientId, String group, boolean scheduleFullDispatchIfFull) {",
            "    /** 向指定客户端投递 LMQ 事件；队列满时可调度全量重分发。 */\n    public boolean tryDispatchToClient(String lmqName, String clientId, String group, boolean scheduleFullDispatchIfFull) {",
        ),
        (
            "    public void doFullDispatchForClient(String clientId, String group) {",
            "    /** 对指定客户端执行全量 LMQ 事件重分发。 */\n    public void doFullDispatchForClient(String clientId, String group) {",
        ),
        (
            "    public void doFullDispatchForWildcardGroup(String group) {",
            "    /** 对通配符订阅的消费组执行全量事件重分发。 */\n    public void doFullDispatchForWildcardGroup(String group) {",
        ),
        (
            "    public void scan() {",
            "    /** 扫描非活跃客户端并触发延迟全量分发。 */\n    public void scan() {",
        ),
        (
            "    private static final long CLIENT_INACTIVE_INTERVAL = 10 * 1000; // inactive time when it has unprocessed events",
            "    private static final long CLIENT_INACTIVE_INTERVAL = 10 * 1000; // 存在未处理事件时的非活跃判定间隔",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/lite/LiteSubscriptionRegistryImpl.java": [
        (
            "public class LiteSubscriptionRegistryImpl extends ServiceThread implements LiteSubscriptionRegistry {",
            "/**\n * Lite 订阅注册表实现：维护 clientId 与 LMQ/Topic 订阅关系及 Netty 通道映射。\n * <p>支持部分/完整订阅增删、通配符 Group 缓存与取消订阅通知。\n */\npublic class LiteSubscriptionRegistryImpl extends ServiceThread implements LiteSubscriptionRegistry {",
        ),
        (
            "    // Number of active liteTopic references.\n    // [(client1, liteTopic1), (client2, liteTopic1)] counts as two active references.",
            "    // 活跃 LiteTopic 引用计数；同一 Topic 被多客户端订阅时分别计数",
        ),
        (
            "    public void updateClientChannel(String clientId, Channel channel) {",
            "    /** 更新客户端 Netty 通道映射。 */\n    public void updateClientChannel(String clientId, Channel channel) {",
        ),
        (
            "    public void addPartialSubscription(String clientId, String group, String topic, Set<String> lmqNameSet,",
            "    /** 增量添加客户端对 Topic 下部分 LMQ 的订阅。 */\n    public void addPartialSubscription(String clientId, String group, String topic, Set<String> lmqNameSet,",
        ),
        (
            "    public void addCompleteSubscription(String clientId, String group, String topic, Set<String> lmqNameAll, long version) {",
            "    /** 添加客户端对 Topic 下全部 LMQ 的完整订阅。 */\n    public void addCompleteSubscription(String clientId, String group, String topic, Set<String> lmqNameAll, long version) {",
        ),
        (
            "    public void removeCompleteSubscription(String clientId) {",
            "    /** 移除客户端的全部 Lite 订阅并清理索引。 */\n    public void removeCompleteSubscription(String clientId) {",
        ),
        (
            "    public SubscriberWrapper getAllSubscriber(String group, String lmqName) {",
            "    /** 查询订阅指定 LMQ 的全部客户端（含通配符 Group）。 */\n    public SubscriberWrapper getAllSubscriber(String group, String lmqName) {",
        ),
        (
            "    public void cleanSubscription(String lmqName, boolean notifyClient) {",
            "    /** 清理 LMQ 的全部订阅，可选通知客户端取消订阅。 */\n    public void cleanSubscription(String lmqName, boolean notifyClient) {",
        ),
        (
            "    public LiteSubscription getLiteSubscription(String clientId) {",
            "    /** 返回客户端的 Lite 订阅快照。 */\n    public LiteSubscription getLiteSubscription(String clientId) {",
        ),
        (
            "    public int getActiveSubscriptionNum() {",
            "    /** 返回当前活跃 LiteTopic 引用总数。 */\n    public int getActiveSubscriptionNum() {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/longpolling/PopLongPollingService.java": [
        (
            "public class PopLongPollingService extends ServiceThread {",
            "/**\n * Pop 长轮询服务：挂起 Pop 请求直至消息到达或超时。\n * <p>按 topic+cid+queueId 维护 polling 队列，支持 Caffeine LRU 过期清理。\n */\npublic class PopLongPollingService extends ServiceThread {",
        ),
        (
            "    public PopLongPollingService(BrokerController brokerController, NettyRequestProcessor processor,",
            "    /** 初始化 polling 映射与 topic-cid 索引缓存。 */\n    public PopLongPollingService(BrokerController brokerController, NettyRequestProcessor processor,",
        ),
        (
            "        // 100000 topic default,  100000 lru topic + cid + qid",
            "        // 默认约 10 万 Topic 容量；LRU 缓存 topic+cid+queueId 组合",
        ),
        (
            "    public void notifyMessageArriving(final String topic, final int queueId, long offset,",
            "    /** 消息到达时唤醒匹配的长轮询 Pop 请求。 */\n    public void notifyMessageArriving(final String topic, final int queueId, long offset,",
        ),
        (
            "    public boolean wakeUp(final PopRequest request, CommandCallback callback) {",
            "    /** 唤醒单个 Pop 长轮询请求并回调响应。 */\n    public boolean wakeUp(final PopRequest request, CommandCallback callback) {",
        ),
        (
            "    public PollingResult polling(final ChannelHandlerContext ctx, RemotingCommand remotingCommand,",
            "    /** 处理 Pop 长轮询请求：立即返回或挂起等待消息。 */\n    public PollingResult polling(final ChannelHandlerContext ctx, RemotingCommand remotingCommand,",
        ),
        (
            "    public Cache<String, ConcurrentSkipListSet<PopRequest>> getPollingMap() {",
            "    /** 返回 topic 键到 Pop 请求集合的 polling 映射。 */\n    public Cache<String, ConcurrentSkipListSet<PopRequest>> getPollingMap() {",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 后台循环：清理过期 polling 条目并打印统计。 */\n    @Override\n    public void run() {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/metrics/BatchSplittingMetricExporter.java": [
        (
            "/**\n * A {@link MetricExporter} decorator that splits large\n * metric batches into smaller sub-batches before delegating\n * to the underlying exporter.\n *\n * <p>This addresses the gRPC 32MB payload size limit when\n * exporting OTLP metrics. High-cardinality metrics (e.g.,\n * consumer lag with consumer_group x topic combinations)\n * can produce payloads exceeding this limit, causing all\n * metrics to fail to export.\n *\n * <p>Splitting is based on the total number of data points\n * (not the number of MetricData objects), because a single\n * MetricData can contain thousands of data points. When the\n * total data point count is within the configured threshold,\n * the batch is passed through directly (fast path).\n *\n * <p>When a single MetricData contains more data points\n * than the batch limit, its internal points are split into\n * multiple smaller MetricData objects, each preserving the\n * original resource, scope, name, description, unit, and\n * type metadata.\n */",
            "/**\n * {@link MetricExporter} 装饰器：将大批量指标拆分为小子批次再委托底层导出器。\n *\n * <p>解决 OTLP 经 gRPC 导出时的 32MB 载荷限制。高基数指标（如 consumer_group × topic\n * 的消费滞后）可能使单次导出超限导致全部失败。\n *\n * <p>拆分依据为数据点总数（非 MetricData 对象数），单对象可含数千点。\n * 未超阈值时走快速路径直接透传。\n *\n * <p>单 MetricData 超限时将其内部点拆成多个较小对象，保留原 resource/scope/名称等元数据。\n */",
        ),
        (
            "    /** Logger. */",
            "    /** 日志记录器。 */",
        ),
        (
            "    /** The underlying exporter to delegate to. */",
            "    /** 委托的底层指标导出器。 */",
        ),
        (
            "    /** Supplies the max data points per batch at runtime. */",
            "    /** 运行时提供每批最大数据点数的供应器。 */",
        ),
        (
            "    /**\n     * Creates a new BatchSplittingMetricExporter.\n     *\n     * @param metricExporter the underlying MetricExporter\n     * @param batchSizeSupplier supplies the max number\n     *     of data points per batch; must return &gt; 0\n     */",
            "    /**\n     * 创建批量拆分指标导出器。\n     *\n     * @param metricExporter 底层 MetricExporter\n     * @param batchSizeSupplier 每批最大数据点数供应器，返回值须 &gt; 0\n     */",
        ),
        (
            "        // Snapshot to avoid concurrent-modification AIOOBE\n        // in OTel SDK marshaling (see NumberDataPointMarshaler)",
            "        // 快照避免 OTel SDK 序列化时并发修改 AIOOBE",
        ),
        (
            "    @Override\n    public CompletableResultCode export(",
            "    /** 按数据点阈值拆分批次后委托底层导出。 */\n    @Override\n    public CompletableResultCode export(",
        ),
        (
            "    @Override\n    public CompletableResultCode flush() {",
            "    /** 委托底层导出器 flush。 */\n    @Override\n    public CompletableResultCode flush() {",
        ),
        (
            "    @Override\n    public CompletableResultCode shutdown() {",
            "    /** 委托底层导出器 shutdown。 */\n    @Override\n    public CompletableResultCode shutdown() {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/metrics/BrokerMetricsManager.java": [
        (
            "public class BrokerMetricsManager {",
            "/**\n * Broker OpenTelemetry 指标管理器：注册消息吞吐、事务、Pop、Lite 滞后等 Meter。\n * <p>支持 OTLP、Prometheus 等多种导出后端及批量拆分装饰。\n */\npublic class BrokerMetricsManager {",
        ),
        (
            "    public BrokerMetricsManager(BrokerController brokerController) {",
            "    /** 初始化 SdkMeterProvider、Exporter 与各 Counter/Histogram/Gauge。 */\n    public BrokerMetricsManager(BrokerController brokerController) {",
        ),
        (
            "    public AttributesBuilder newAttributesBuilder() {",
            "    /** 创建带 Broker 集群/名称/地址等默认标签的 Attributes 构建器。 */\n    public AttributesBuilder newAttributesBuilder() {",
        ),
        (
            "    public static boolean isRetryOrDlqTopic(String topic) {",
            "    /** 判断 Topic 是否为重试或死信队列 Topic。 */\n    public static boolean isRetryOrDlqTopic(String topic) {",
        ),
        (
            "    public static boolean isSystemGroup(String group) {",
            "    /** 判断消费组是否为系统内置 Group（如工具组前缀）。 */\n    public static boolean isSystemGroup(String group) {",
        ),
        (
            "    public Meter getBrokerMeter() {",
            "    /** 返回 Broker 级 OpenTelemetry Meter。 */\n    public Meter getBrokerMeter() {",
        ),
        (
            "    public LongCounter getMessagesInTotal() {",
            "    /** 返回消息写入总量 Counter。 */\n    public LongCounter getMessagesInTotal() {",
        ),
        (
            "    public LongCounter getMessagesOutTotal() {",
            "    /** 返回消息消费总量 Counter。 */\n    public LongCounter getMessagesOutTotal() {",
        ),
        (
            "    public void shutdown() {",
            "    /** 关闭 MeterProvider 与 PeriodicMetricReader。 */\n    public void shutdown() {",
        ),
        (
            "    public LiteConsumerLagCalculator getLiteConsumerLagCalculator() {",
            "    /** 返回 Lite 消费滞后计算器。 */\n    public LiteConsumerLagCalculator getLiteConsumerLagCalculator() {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/metrics/ConsumerLagCalculator.java": [
        (
            "public class ConsumerLagCalculator {",
            "/**\n * 消费滞后计算器：统计 Push/Pull/Pop 模式下 Group×Topic 的 lag、in-flight 与可用消息数。\n * <p>供 Broker 指标导出与运维监控使用。\n */\npublic class ConsumerLagCalculator {",
        ),
        (
            "    public ConsumerLagCalculator(BrokerController brokerController) {",
            "    /** 绑定 Broker 各管理器与 Pop 相关服务。 */\n    public ConsumerLagCalculator(BrokerController brokerController) {",
        ),
        (
            "    public static class ProcessGroupInfo {",
            "    /** 待计算的 Group×Topic 处理上下文（含 Pop/重试 Topic 标识）。 */\n    public static class ProcessGroupInfo {",
        ),
        (
            "    public static class CalculateLagResult extends BaseCalculateResult {",
            "    /** 消费滞后计算结果：lag 条数与最早未消费消息时间戳。 */\n    public static class CalculateLagResult extends BaseCalculateResult {",
        ),
        (
            "        public long getLagLatency() {",
            "        /** 返回滞后延迟（当前时间减最早未消费时间戳）。 */\n        public long getLagLatency() {",
        ),
        (
            "    public void calculateLag(Consumer<CalculateLagResult> lagRecorder) {",
            "    /** 遍历全部在线消费组并计算各 Topic 消费滞后。 */\n    public void calculateLag(Consumer<CalculateLagResult> lagRecorder) {",
        ),
        (
            "    public void calculateInflight(Consumer<CalculateInflightResult> inflightRecorder) {",
            "    /** 计算 Pop 模式下已 Pop 未 Ack 的在途消息数。 */\n    public void calculateInflight(Consumer<CalculateInflightResult> inflightRecorder) {",
        ),
        (
            "    public void calculateAvailable(Consumer<CalculateAvailableResult> availableRecorder) {",
            "    /** 计算可立即消费的消息数量（含过滤与 Pop 缓冲）。 */\n    public void calculateAvailable(Consumer<CalculateAvailableResult> availableRecorder) {",
        ),
        (
            "    public Pair<Long, Long> getConsumerLagStats(String group, String topic, boolean isPop) throws ConsumeQueueException {",
            "    /** 查询指定 Group×Topic 的滞后条数与最早未消费时间戳。 */\n    public Pair<Long, Long> getConsumerLagStats(String group, String topic, boolean isPop) throws ConsumeQueueException {",
        ),
        (
            "    public long getAvailableMsgCount(String group, String topic, boolean isPop) throws ConsumeQueueException {",
            "    /** 返回指定 Group×Topic 的可消费消息总数。 */\n    public long getAvailableMsgCount(String group, String topic, boolean isPop) throws ConsumeQueueException {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/offset/ConsumerOffsetManager.java": [
        (
            "public class ConsumerOffsetManager extends ConfigManager {",
            "/**\n * 消费位点管理器（JSON 文件持久化）：维护 Push/Pull 位点表与重置位点。\n * <p>键格式为 topic@group，值为 queueId→offset 映射。\n */\npublic class ConsumerOffsetManager extends ConfigManager {",
        ),
        (
            "    public static final String TOPIC_GROUP_SEPARATOR = \"@\";",
            "    /** Topic 与 Group 在位点键中的分隔符。 */\n    public static final String TOPIC_GROUP_SEPARATOR = \"@\";",
        ),
        (
            "    public void commitOffset(final String clientHost, final String group, final String topic, final int queueId,",
            "    /** 提交 Push 消费位点并持久化到配置文件。 */\n    public void commitOffset(final String clientHost, final String group, final String topic, final int queueId,",
        ),
        (
            "    public long queryOffset(final String group, final String topic, final int queueId) {",
            "    /** 查询 Push 消费位点，不存在时返回 -1。 */\n    public long queryOffset(final String group, final String topic, final int queueId) {",
        ),
        (
            "    public void commitPullOffset(final String clientHost, final String group, final String topic, final int queueId,",
            "    /** 提交 Pull 消费位点。 */\n    public void commitPullOffset(final String clientHost, final String group, final String topic, final int queueId,",
        ),
        (
            "    public Set<String> whichTopicByConsumer(final String group) {",
            "    /** 返回指定消费组已提交位点的全部 Topic。 */\n    public Set<String> whichTopicByConsumer(final String group) {",
        ),
        (
            "    public void scanUnsubscribedTopic() {",
            "    /** 扫描并清理已无在线 Consumer 的 Topic 位点。 */\n    public void scanUnsubscribedTopic() {",
        ),
        (
            "    public void assignResetOffset(String topic, String group, int queueId, long offset) {",
            "    /** 管理员指定重置位点。 */\n    public void assignResetOffset(String topic, String group, int queueId, long offset) {",
        ),
        (
            "    public Long queryThenEraseResetOffset(String topic, String group, Integer queueId) {",
            "    /** 查询并清除一次性重置位点（Consumer 拉取时应用）。 */\n    public Long queryThenEraseResetOffset(String topic, String group, Integer queueId) {",
        ),
        (
            "    public void cloneOffset(final String srcGroup, final String destGroup, final String topic) {",
            "    /** 将源消费组在 Topic 上的位点克隆到目标组。 */\n    public void cloneOffset(final String srcGroup, final String destGroup, final String topic) {",
        ),
    ],
    "broker/src/main/java/org/apache/rocketmq/broker/pop/PopConsumerService.java": [
        (
            "public class PopConsumerService extends ServiceThread {",
            "/**\n * Pop 消费核心服务：异步 Pop/Ack、不可见时间变更、消息复活与重试 Topic 管理。\n * <p>维护 Pop 位点 KV 存储、FIFO 阻塞状态与 CheckPoint 持久化。\n */\npublic class PopConsumerService extends ServiceThread {",
        ),
        (
            "    public PopConsumerService(BrokerController brokerController) {",
            "    /** 初始化 Pop 缓存、KV 存储、锁服务与请求计数表。 */\n    public PopConsumerService(BrokerController brokerController) {",
        ),
        (
            "    public boolean isPopShouldStop(String group, String topic, int queueId) {",
            "    /** 判断指定队列 Pop 是否应暂停（如订阅被禁止）。 */\n    public boolean isPopShouldStop(String group, String topic, int queueId) {",
        ),
        (
            "    public long getPopOffset(String groupId, String topicId, int queueId, int initMode, boolean fifo) {",
            "    /** 获取或初始化 Pop 消费位点（支持 FIFO 与多种 init 模式）。 */\n    public long getPopOffset(String groupId, String topicId, int queueId, int initMode, boolean fifo) {",
        ),
        (
            "    public CompletableFuture<GetMessageResult> getMessageAsync(String clientHost,",
            "    /** 异步从 CommitLog 读取 Pop 消息并应用过滤器。 */\n    public CompletableFuture<GetMessageResult> getMessageAsync(String clientHost,",
        ),
        (
            "    public CompletableFuture<PopConsumerContext> popAsync(String clientHost, long popTime, long invisibleTime,",
            "    /** 异步 Pop 入口：拉取消息、设置不可见时间并返回上下文。 */\n    public CompletableFuture<PopConsumerContext> popAsync(String clientHost, long popTime, long invisibleTime,",
        ),
        (
            "    public CompletableFuture<Boolean> ackAsync(",
            "    /** 异步确认 Pop 消息，更新位点并触发复活检查。 */\n    public CompletableFuture<Boolean> ackAsync(",
        ),
        (
            "    public void changeInvisibilityDuration(long popTime, long invisibleTime, long changedPopTime,",
            "    /** 变更已 Pop 消息的不可见时长（ChangeInvisibleTime）。 */\n    public void changeInvisibilityDuration(long popTime, long invisibleTime, long changedPopTime,",
        ),
        (
            "    public CompletableFuture<Boolean> revive(PopConsumerRecord record) {",
            "    /** 将超时未 Ack 的 Pop 消息复活回队列或重试 Topic。 */\n    public CompletableFuture<Boolean> revive(PopConsumerRecord record) {",
        ),
        (
            "    public long revive(AtomicLong currentTime, int maxCount) {",
            "    /** 批量扫描并复活超时 Pop 消息，返回本次处理条数。 */\n    public long revive(AtomicLong currentTime, int maxCount) {",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 后台循环：CheckPoint 刷盘、锁清理与 Pop 复活调度。 */\n    @Override\n    public void run() {",
        ),
    ],
}
