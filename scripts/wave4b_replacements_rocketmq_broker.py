"""RocketMQ 5.5.0 wave4b broker lite/loadbalance/longpolling [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["broker/src/main/java/org/apache/rocketmq/broker/lite/AbstractLiteLifecycleManager.java"] = [
    (
        "/**\n * Abstract class of lite lifecycle manager, which is used to manage the TTL of lite topics\n * and the validity of subscription. The subclasses provide file CQ and rocksdb CQ implementations.\n */",
        "/**\n * Lite 主题生命周期管理抽象基类：负责 lite topic 的 TTL 过期清理与订阅有效性判定。\n * 子类分别基于文件 ConsumeQueue 与 RocksDB ConsumeQueue 实现具体扫描逻辑。\n */",
    ),
    (
        "    private static final int MAX_INVALID_SCAN_COUNT = 5;",
        "    /** maxOffset 异常时连续扫描超过该次数才判定过期，避免并发读写 transient 状态。 */\n    private static final int MAX_INVALID_SCAN_COUNT = 5;",
    ),
    (
        "    public AbstractLiteLifecycleManager(BrokerController brokerController, LiteSharding liteSharding) {",
        "    /** 绑定 Broker 控制器与 lite 分片策略。 */\n    public AbstractLiteLifecycleManager(BrokerController brokerController, LiteSharding liteSharding) {",
    ),
    (
        "    public boolean init() {",
        "    /** 初始化 MessageStore 引用，启动前必须调用。 */\n    public boolean init() {",
    ),
    (
        "    /**\n     * This method actually returns NEXT slot index to use, starting from 0\n     */",
        "    /**\n     * 返回指定 LMQ 队列下一个可写 slot 索引（从 0 起算）。\n     */",
    ),
    (
        "    /**\n     * Collect expired LMQ of lite topic, and also attach its parent topic name\n     * return Pair of parent topic and lmq name, not null\n     */",
        "    /**\n     * 收集已过期的 lite LMQ，附带父 topic 名；返回 (parentTopic, lmqName) 列表，非 null。\n     */",
    ),
    (
        "    /**\n     * Collect LMQ by parent topic\n     * return lmq name list, not null\n     */",
        "    /**\n     * 按父 topic 收集其下所有 LMQ 名称；返回列表非 null。\n     */",
    ),
    (
        "    /**\n     * Iterator of lite topic, for high frequency iteration\n     * Triple<lmqName, maxOffsetInQueue, lastStoreTimestamp>, lastStoreTimestamp is null for now\n     * return true to continue, false to break.\n     *\n     * @param function consumer func\n     */",
        "    /**\n     * 高频遍历 lite topic；Triple 为 (lmqName, maxOffset, lastStoreTimestamp)，后者暂为 null。\n     * 回调返回 true 继续，false 中断。\n     *\n     * @param function 遍历回调\n     */",
    ),
    (
        "    /**\n     * Check if the subscription for the given LMQ is active.\n     * A subscription is considered active if either:\n     * - the current broker is responsible for this LMQ according to the sharding strategy\n     * - the LMQ exists (has messages) in the message store\n     */",
        "    /**\n     * 判断给定 LMQ 的订阅是否仍有效：当前 broker 负责该 LMQ，或 MessageStore 中仍有消息。\n     */",
    ),
    (
        "    public int getLiteTopicCount(String parentTopic) {",
        "    /** 统计父 topic 下 lite LMQ 数量；非 lite 类型 topic 返回 0。 */\n    public int getLiteTopicCount(String parentTopic) {",
    ),
    (
        "    public boolean isLmqExist(String lmqName) {",
        "    /** LMQ 队列 maxOffset > 0 即视为存在。 */\n    public boolean isLmqExist(String lmqName) {",
    ),
    (
        "    public void cleanExpiredLiteTopic() {",
        "    /** 刷新元数据后扫描并删除所有 TTL 过期的 lite LMQ。 */\n    public void cleanExpiredLiteTopic() {",
    ),
    (
        "    public void cleanByParentTopic(String parentTopic) {",
        "    /** 按父 topic 批量清理其下全部 lite LMQ。 */\n    public void cleanByParentTopic(String parentTopic) {",
    ),
    (
        "    public void updateMetadata() {",
        "    /** 从 Topic/Subscription 配置刷新 TTL 映射与订阅 group 映射。 */\n    public void updateMetadata() {",
    ),
    (
        "    public boolean isLiteTopicExpired(String parentTopic, String lmqName, long maxOffset) {",
        "    /** 综合 maxOffset、最后写入时间与 topic TTL 判定 LMQ 是否过期。 */\n    public boolean isLiteTopicExpired(String parentTopic, String lmqName, long maxOffset) {",
    ),
    (
        "    public void deleteLmq(String parentTopic, String lmqName) {",
        "    /** 删除 LMQ：清理消费位点、订阅注册及 MessageStore 中的 topic 数据。 */\n    public void deleteLmq(String parentTopic, String lmqName) {",
    ),
    (
        "    /**\n     * Maybe we can check all subscriber groups, but currently consumer lag checking is not performed.\n     * Only inactive time of message sending is considered for TTL expiration.\n     */",
        "    /**\n     * 当前未做消费滞后检查，TTL 过期仅依据消息发送静默时长。\n     */",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/lite/LiteCtlListener.java"] = [
    (
        "public interface LiteCtlListener {",
        "/**\n * Lite 控制面事件监听器：订阅/退订 LMQ 时回调。\n */\npublic interface LiteCtlListener {",
    ),
    (
        "    void onRegister(String clientId, String group, String lmqName);",
        "    /** 客户端注册对指定 LMQ 的 lite 订阅。 */\n    void onRegister(String clientId, String group, String lmqName);",
    ),
    (
        "    void onUnregister(String clientId, String group, String lmqName);",
        "    /** 客户端取消对指定 LMQ 的 lite 订阅。 */\n    void onUnregister(String clientId, String group, String lmqName);",
    ),
    (
        "    void onRemoveAll(String clientId, String group);",
        "    /** 客户端断开或 group 下全部 lite 订阅被移除。 */\n    void onRemoveAll(String clientId, String group);",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/lite/LiteLifecycleManager.java"] = [
    (
        "public class LiteLifecycleManager extends AbstractLiteLifecycleManager {",
        "/**\n * 基于内存 ConsumeQueue 表的 lite 生命周期管理器：扫描、过期判定与遍历均直接访问 MessageStore 队列索引。\n */\npublic class LiteLifecycleManager extends AbstractLiteLifecycleManager {",
    ),
    (
        "    public LiteLifecycleManager(BrokerController brokerController, LiteSharding liteSharding) {",
        "    /** 绑定 Broker 与 lite 分片组件。 */\n    public LiteLifecycleManager(BrokerController brokerController, LiteSharding liteSharding) {",
    ),
    (
        "    public long getMaxOffsetInQueue(String lmqName) {",
        "    /** 从 ConsumeQueue 表读取 LMQ queueId=0 的 maxOffset。 */\n    public long getMaxOffsetInQueue(String lmqName) {",
    ),
    (
        "    public List<String> collectByParentTopic(String parentTopic) {",
        "    /** 遍历 ConsumeQueue 表，筛选属于 parentTopic 的 LMQ 名。 */\n    public List<String> collectByParentTopic(String parentTopic) {",
    ),
    (
        "    public List<Pair<String, String>> collectExpiredLiteTopic() {",
        "    /** 全表扫描 ConsumeQueue，收集 TTL 已过期的 (parentTopic, lmqName) 对。 */\n    public List<Pair<String, String>> collectExpiredLiteTopic() {",
    ),
    (
        "    public void forEachLiteTopic(Function<Triple<String, Long, Long>, Boolean> function) {",
        "    /** 遍历所有 lite LMQ 并回调 (lmqName, maxOffset, null)。 */\n    public void forEachLiteTopic(Function<Triple<String, Long, Long>, Boolean> function) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/lite/LiteMetadataUtil.java"] = [
    (
        "public class LiteMetadataUtil {",
        "/**\n * Lite 元数据工具：从 Topic/Subscription 配置读取 lite 类型、绑定 topic、TTL 及订阅 group 映射。\n */\npublic class LiteMetadataUtil {",
    ),
    (
        "    public static boolean isConsumeEnable(String group, BrokerController brokerController) {",
        "    /** 判断消费组是否允许消费。 */\n    public static boolean isConsumeEnable(String group, BrokerController brokerController) {",
    ),
    (
        "    public static boolean isLiteMessageType(String parentTopic, BrokerController brokerController) {",
        "    /** 父 topic 的 {@link TopicMessageType} 是否为 LITE。 */\n    public static boolean isLiteMessageType(String parentTopic, BrokerController brokerController) {",
    ),
    (
        "    public static boolean isLiteGroupType(String group, BrokerController brokerController) {",
        "    /** 消费组是否配置了 liteBindTopic（lite 订阅组）。 */\n    public static boolean isLiteGroupType(String group, BrokerController brokerController) {",
    ),
    (
        "    public static String getLiteBindTopic(String group, BrokerController brokerController) {",
        "    /** 返回消费组绑定的 lite 父 topic；未配置则 null。 */\n    public static String getLiteBindTopic(String group, BrokerController brokerController) {",
    ),
    (
        "    public static boolean isSubLiteExclusive(String group, BrokerController brokerController) {",
        "    /** 消费组是否为 lite 独占订阅模式。 */\n    public static boolean isSubLiteExclusive(String group, BrokerController brokerController) {",
    ),
    (
        "    public static boolean isResetOffsetInExclusiveMode(String group, BrokerController brokerController) {",
        "    /** 独占模式下是否在重置位点时清空 offset。 */\n    public static boolean isResetOffsetInExclusiveMode(String group, BrokerController brokerController) {",
    ),
    (
        "    public static boolean isResetOffsetOnUnsubscribe(String group, BrokerController brokerController) {",
        "    /** 退订时是否重置消费位点。 */\n    public static boolean isResetOffsetOnUnsubscribe(String group, BrokerController brokerController) {",
    ),
    (
        "    public static int getMaxClientEventCount(String group, BrokerController brokerController) {",
        "    /** 返回 group 级 maxClientEventCount，未配置则用 broker 默认值。 */\n    public static int getMaxClientEventCount(String group, BrokerController brokerController) {",
    ),
    (
        "    public static boolean isWildcardGroup(String group, BrokerController brokerController) {",
        "    /** 是否为 wildcard lite 消费组（通配订阅父 topic 下全部 LMQ）。 */\n    public static boolean isWildcardGroup(String group, BrokerController brokerController) {",
    ),
    (
        "    public static Map<String, Integer> getTopicTtlMap(BrokerController brokerController) {",
        "    /** 构建 lite topic 名 → 过期分钟数 的 TTL 映射。 */\n    public static Map<String, Integer> getTopicTtlMap(BrokerController brokerController) {",
    ),
    (
        "    public static Map<String, Set<String>> getSubscriberGroupMap(BrokerController brokerController) {",
        "    /** 构建 lite 父 topic → 订阅该 topic 的 group 集合 映射。 */\n    public static Map<String, Set<String>> getSubscriberGroupMap(BrokerController brokerController) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/lite/LiteQuotaException.java"] = [
    (
        "public class LiteQuotaException extends RuntimeException {",
        "/**\n * Lite 配额超限异常：客户端 lite 订阅数或事件数超过配置上限时抛出。\n */\npublic class LiteQuotaException extends RuntimeException {",
    ),
    (
        "    public LiteQuotaException(String message) {",
        "    /** 携带描述信息的 lite 配额异常。 */\n    public LiteQuotaException(String message) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/lite/LiteSharding.java"] = [
    (
        "public interface LiteSharding {",
        "/**\n * Lite LMQ 分片策略：根据 parentTopic 与 lmqName 计算负责该 LMQ 的 broker 名。\n */\npublic interface LiteSharding {",
    ),
    (
        "    String shardingByLmqName(String parentTopic, String lmqName);",
        "    /** 返回应承载该 LMQ 的 brokerName。 */\n    String shardingByLmqName(String parentTopic, String lmqName);",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/lite/LiteShardingImpl.java"] = [
    (
        "public class LiteShardingImpl implements LiteSharding {",
        "/**\n * 一致性哈希 lite 分片：按 liteTopic 哈希值映射到父 topic 路由中的 MessageQueue 所属 broker。\n */\npublic class LiteShardingImpl implements LiteSharding {",
    ),
    (
        "    public LiteShardingImpl(BrokerController brokerController, TopicRouteInfoManager topicRouteInfoManager) {",
        "    /** 注入 Broker 与 topic 路由管理器。 */\n    public LiteShardingImpl(BrokerController brokerController, TopicRouteInfoManager topicRouteInfoManager) {",
    ),
    (
        "    public String shardingByLmqName(String parentTopic, String lmqName) {",
        "    /** 对 liteTopic 做 consistentHash，选取 writeQueue 对应 broker；路由缺失时回退当前 broker。 */\n    public String shardingByLmqName(String parentTopic, String lmqName) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/lite/LiteSubscriptionRegistry.java"] = [
    (
        "public interface LiteSubscriptionRegistry {",
        "/**\n * Lite 订阅注册表：维护 clientId 到 LMQ 订阅关系，支持部分/全量订阅及 wildcard 查询。\n */\npublic interface LiteSubscriptionRegistry {",
    ),
    (
        "    void updateClientChannel(String clientId, Channel channel);",
        "    /** 更新 clientId 对应的 Netty 通道。 */\n    void updateClientChannel(String clientId, Channel channel);",
    ),
    (
        "    LiteSubscription getLiteSubscription(String clientId);",
        "    /** 获取 clientId 的 lite 订阅快照。 */\n    LiteSubscription getLiteSubscription(String clientId);",
    ),
    (
        "    int getActiveSubscriptionNum();",
        "    /** 当前活跃 lite 订阅总数。 */\n    int getActiveSubscriptionNum();",
    ),
    (
        "    void addPartialSubscription(String clientId, String group, String topic, Set<String> lmqNameSet, OffsetOption offsetOption);",
        "    /** 增量添加 client 对指定 LMQ 集合的部分订阅。 */\n    void addPartialSubscription(String clientId, String group, String topic, Set<String> lmqNameSet, OffsetOption offsetOption);",
    ),
    (
        "    void removePartialSubscription(String clientId, String group, String topic, Set<String> lmqNameSet);",
        "    /** 移除 client 对部分 LMQ 的订阅。 */\n    void removePartialSubscription(String clientId, String group, String topic, Set<String> lmqNameSet);",
    ),
    (
        "    void addCompleteSubscription(String clientId, String group, String topic, Set<String> newLmqNameSet, long version);",
        "    /** 全量替换 client 在某 topic 下的 LMQ 订阅集合并携带版本号。 */\n    void addCompleteSubscription(String clientId, String group, String topic, Set<String> newLmqNameSet, long version);",
    ),
    (
        "    void removeCompleteSubscription(String clientId);",
        "    /** 移除 client 的全部 lite 订阅。 */\n    void removeCompleteSubscription(String clientId);",
    ),
    (
        "    void addListener(LiteCtlListener listener);",
        "    /** 注册 lite 控制面事件监听器。 */\n    void addListener(LiteCtlListener listener);",
    ),
    (
        "    SubscriberWrapper getAllSubscriber(String group, String lmqName);",
        "    /** 查询订阅指定 LMQ 的全部 client（按 group 聚合）。 */\n    SubscriberWrapper getAllSubscriber(String group, String lmqName);",
    ),
    (
        "    SubscriberWrapper.ListWrapper getWildcardSubscriber(String group, String parentTopic);",
        "    /** 查询 wildcard group 对父 topic 的通配订阅 client 列表。 */\n    SubscriberWrapper.ListWrapper getWildcardSubscriber(String group, String parentTopic);",
    ),
    (
        "    List<String> getAllClientIdByGroup(String group);",
        "    /** 返回指定 group 下所有已注册 lite clientId。 */\n    List<String> getAllClientIdByGroup(String group);",
    ),
    (
        "    void cleanSubscription(String lmqName, boolean notifyClient);",
        "    /** LMQ 删除时清理相关订阅，可选是否通知客户端。 */\n    void cleanSubscription(String lmqName, boolean notifyClient);",
    ),
    (
        "    void start();",
        "    /** 启动订阅注册表后台任务。 */\n    void start();",
    ),
    (
        "    void shutdown();",
        "    /** 关闭并释放订阅注册表资源。 */\n    void shutdown();",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/lite/RocksDBLiteLifecycleManager.java"] = [
    (
        "public class RocksDBLiteLifecycleManager extends AbstractLiteLifecycleManager {",
        "/**\n * 基于 RocksDB ConsumeQueue offset 表的 lite 生命周期管理器，适用于 defaultRocksDB 或 CombineCQ 双写场景。\n */\npublic class RocksDBLiteLifecycleManager extends AbstractLiteLifecycleManager {",
    ),
    (
        "    public RocksDBLiteLifecycleManager(BrokerController brokerController, LiteSharding liteSharding) {",
        "    /** 绑定 Broker 与 lite 分片组件。 */\n    public RocksDBLiteLifecycleManager(BrokerController brokerController, LiteSharding liteSharding) {",
    ),
    (
        "    public long getMaxOffsetInQueue(String lmqName) {",
        "    /** 从 RocksDB maxCqOffsetTable 读取 lmqName-0 的 next offset。 */\n    public long getMaxOffsetInQueue(String lmqName) {",
    ),
    (
        "    public List<String> collectByParentTopic(String parentTopic) {",
        "    /** 遍历 maxCqOffsetTable，筛选属于 parentTopic 的 LMQ。 */\n    public List<String> collectByParentTopic(String parentTopic) {",
    ),
    (
        "    public List<Pair<String, String>> collectExpiredLiteTopic() {",
        "    /** 扫描 offset 表，收集 TTL 过期的 (parentTopic, lmqName)。 */\n    public List<Pair<String, String>> collectExpiredLiteTopic() {",
    ),
    (
        "    public boolean init() {",
        "    /** 解析 Tiered/Combine/RocksDB 队列存储并挂载 maxCqOffsetTable；失败则中止启动。 */\n    public boolean init() {",
    ),
    (
        "    public void forEachLiteTopic(Function<Triple<String, Long, Long>, Boolean> function) {",
        "    /** 遍历 offset 表中所有 lite LMQ 并回调。 */\n    public void forEachLiteTopic(Function<Triple<String, Long, Long>, Boolean> function) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/lite/SubscriberWrapper.java"] = [
    (
        "public abstract class SubscriberWrapper {",
        "/**\n * Lite 订阅者查询结果包装：ListWrapper 为 flat 列表，MapWrapper 按 group 分组。\n */\npublic abstract class SubscriberWrapper {",
    ),
    (
        "    public static class ListWrapper extends SubscriberWrapper {",
        "    /** 扁平 {@link ClientGroup} 列表包装。 */\n    public static class ListWrapper extends SubscriberWrapper {",
    ),
    (
        "        public ListWrapper() {",
        "        /** 创建空 client 列表。 */\n        public ListWrapper() {",
    ),
    (
        "        public ListWrapper(List<ClientGroup> clients) {",
        "        /** 用已有 client 列表初始化。 */\n        public ListWrapper(List<ClientGroup> clients) {",
    ),
    (
        "        public List<ClientGroup> getClients() {",
        "        /** 返回订阅 client 列表。 */\n        public List<ClientGroup> getClients() {",
    ),
    (
        "    public static class MapWrapper extends SubscriberWrapper {",
        "    /** group → client 列表 的分组包装。 */\n    public static class MapWrapper extends SubscriberWrapper {",
    ),
    (
        "        public Map<String, List<ClientGroup>> getGroupMap() {",
        "        /** 返回按 group 分组的 client 映射。 */\n        public Map<String, List<ClientGroup>> getGroupMap() {",
    ),
    (
        "    public ListWrapper asListWrapper() {",
        "    /** 若当前实例为 ListWrapper 则强转，否则 null。 */\n    public ListWrapper asListWrapper() {",
    ),
    (
        "    public MapWrapper asMapWrapper() {",
        "    /** 若当前实例为 MapWrapper 则强转，否则 null。 */\n    public MapWrapper asMapWrapper() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/loadbalance/MessageRequestModeManager.java"] = [
    (
        "public class MessageRequestModeManager extends ConfigManager {",
        "/**\n * 消息拉取模式配置管理器：持久化 topic × consumerGroup 的 {@link SetMessageRequestModeRequestBody} 映射。\n */\npublic class MessageRequestModeManager extends ConfigManager {",
    ),
    (
        "    public MessageRequestModeManager() {",
        "    /** 空构造，供 JSON 反序列化使用。 */\n    public MessageRequestModeManager() {",
    ),
    (
        "    public MessageRequestModeManager(BrokerController brokerController) {",
        "    /** 绑定 Broker 以解析配置文件路径。 */\n    public MessageRequestModeManager(BrokerController brokerController) {",
    ),
    (
        "    public void setMessageRequestMode(String topic, String consumerGroup, SetMessageRequestModeRequestBody requestBody) {",
        "    /** 设置指定 topic 与 consumerGroup 的消息请求模式（POP/PULL 等）。 */\n    public void setMessageRequestMode(String topic, String consumerGroup, SetMessageRequestModeRequestBody requestBody) {",
    ),
    (
        "    public SetMessageRequestModeRequestBody getMessageRequestMode(String topic, String consumerGroup) {",
        "    /** 查询 topic+group 的消息请求模式；未配置则 null。 */\n    public SetMessageRequestModeRequestBody getMessageRequestMode(String topic, String consumerGroup) {",
    ),
    (
        "    public ConcurrentHashMap<String, ConcurrentHashMap<String, SetMessageRequestModeRequestBody>> getMessageRequestModeMap() {",
        "    /** 返回完整的 topic → group → 模式 映射表。 */\n    public ConcurrentHashMap<String, ConcurrentHashMap<String, SetMessageRequestModeRequestBody>> getMessageRequestModeMap() {",
    ),
    (
        "    public String configFilePath() {",
        "    /** 返回 messageRequestMode 持久化 JSON 文件路径。 */\n    public String configFilePath() {",
    ),
    (
        "    public void decode(String jsonString) {",
        "    /** 从 JSON 恢复 messageRequestModeMap。 */\n    public void decode(String jsonString) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/LmqPullRequestHoldService.java"] = [
    (
        "public class LmqPullRequestHoldService extends PullRequestHoldService {",
        "/**\n * LMQ 长轮询挂起服务：继承 PullRequestHoldService，针对 lite 队列优化 hold 表清理逻辑。\n */\npublic class LmqPullRequestHoldService extends PullRequestHoldService {",
    ),
    (
        "    public LmqPullRequestHoldService(BrokerController brokerController) {",
        "    /** 绑定 Broker 控制器。 */\n    public LmqPullRequestHoldService(BrokerController brokerController) {",
    ),
    (
        "    public String getServiceName() {",
        "    /** Broker 容器模式下附加 broker 标识前缀。 */\n    public String getServiceName() {",
    ),
    (
        "    public void checkHoldRequest() {",
        "    /** 扫描挂起 pull 请求，有新消息则唤醒；LMQ 队列为空时移除 hold 条目。 */\n    public void checkHoldRequest() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/ManyPullRequest.java"] = [
    (
        "public class ManyPullRequest {",
        "/**\n * 同一 topic-queue 上聚合的多个长轮询 PullRequest，供 LMQ 等多订阅场景批量唤醒。\n */\npublic class ManyPullRequest {",
    ),
    (
        "    public synchronized void addPullRequest(final PullRequest pullRequest) {",
        "    /** 追加单个挂起 pull 请求。 */\n    public synchronized void addPullRequest(final PullRequest pullRequest) {",
    ),
    (
        "    public synchronized void addPullRequest(final List<PullRequest> many) {",
        "    /** 批量追加挂起 pull 请求。 */\n    public synchronized void addPullRequest(final List<PullRequest> many) {",
    ),
    (
        "    public synchronized List<PullRequest> cloneListAndClear() {",
        "    /** 克隆当前列表并清空内部容器，供唤醒线程无锁处理。 */\n    public synchronized List<PullRequest> cloneListAndClear() {",
    ),
    (
        "    public ArrayList<PullRequest> getPullRequestList() {",
        "    /** 返回内部 pull 请求列表（非副本）。 */\n    public ArrayList<PullRequest> getPullRequestList() {",
    ),
    (
        "    public synchronized boolean isEmpty() {",
        "    /** 是否无任何挂起请求。 */\n    public synchronized boolean isEmpty() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/NotificationRequest.java"] = [
    (
        "public class NotificationRequest {",
        "/**\n * POP 通知长轮询挂起请求：持有 RemotingCommand 与 Channel，支持超时与一次性完成标记。\n */\npublic class NotificationRequest {",
    ),
    (
        "    public NotificationRequest(RemotingCommand remotingCommand, Channel channel, long expired) {",
        "    /** 构造挂起请求，expired 为绝对过期时间戳（毫秒）。 */\n    public NotificationRequest(RemotingCommand remotingCommand, Channel channel, long expired) {",
    ),
    (
        "    public Channel getChannel() {",
        "    /** 返回客户端 Netty 通道。 */\n    public Channel getChannel() {",
    ),
    (
        "    public RemotingCommand getRemotingCommand() {",
        "    /** 返回原始 POP 通知 Remoting 请求。 */\n    public RemotingCommand getRemotingCommand() {",
    ),
    (
        "    public boolean isTimeout() {",
        "    /** 当前时间是否已超过过期时间（预留 500ms 缓冲）。 */\n    public boolean isTimeout() {",
    ),
    (
        "    public boolean complete() {",
        "    /** CAS 标记请求已完成，防止重复响应。 */\n    public boolean complete() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/NotifyMessageArrivingListener.java"] = [
    (
        "public class NotifyMessageArrivingListener implements MessageArrivingListener {",
        "/**\n * 消息到达监听器：lite LMQ 走 {@link LiteEventDispatcher}，普通 topic 唤醒 pull/pop/notification 长轮询。\n */\npublic class NotifyMessageArrivingListener implements MessageArrivingListener {",
    ),
    (
        "    public NotifyMessageArrivingListener(final PullRequestHoldService pullRequestHoldService, final PopMessageProcessor popMessageProcessor, final NotificationProcessor notificationProcessor, final LiteEventDispatcher liteEventDispatcher) {",
        "    /** 注入 pull 挂起、POP、通知处理器及 lite 事件分发器。 */\n    public NotifyMessageArrivingListener(final PullRequestHoldService pullRequestHoldService, final PopMessageProcessor popMessageProcessor, final NotificationProcessor notificationProcessor, final LiteEventDispatcher liteEventDispatcher) {",
    ),
    (
        "    public void arriving(String topic, int queueId, long logicOffset, long tagsCode,\n                         long msgStoreTime, byte[] filterBitMap, Map<String, String> properties) {",
        "    /** 新消息写入 CommitLog 后回调：lite 队列分发事件，否则通知三类长轮询挂起请求。 */\n    public void arriving(String topic, int queueId, long logicOffset, long tagsCode,\n                         long msgStoreTime, byte[] filterBitMap, Map<String, String> properties) {",
    ),
]
