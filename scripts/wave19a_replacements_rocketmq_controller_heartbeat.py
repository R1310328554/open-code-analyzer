"""RocketMQ 5.5.0 wave19a controller heartbeat/manager/task [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["controller/src/main/java/org/apache/rocketmq/controller/impl/heartbeat/BrokerLiveInfo.java"] = [
    (
        "public class BrokerLiveInfo implements Serializable {",
        "/**\n * Broker 存活快照：记录心跳地址、超时、Netty 通道、副本 epoch 与消费位点等运行时状态。\n * 供 Controller 心跳管理器维护在线 Broker 表并在 Raft 模式下序列化复制。\n */\npublic class BrokerLiveInfo implements Serializable {",
    ),
    (
        "    private final String brokerName;\n\n    private String brokerAddr;",
        "    /** Broker 逻辑名称。 */\n    private final String brokerName;\n\n    /** Broker 对外服务地址。 */\n    private String brokerAddr;",
    ),
    (
        "    private long heartbeatTimeoutMillis;\n    private Channel channel;",
        "    /** 心跳超时毫秒数，超过则视为离线。 */\n    private long heartbeatTimeoutMillis;\n    /** 与 Broker 建立的 Netty 通道。 */\n    private Channel channel;",
    ),
    (
        "    private long brokerId;\n    private long lastUpdateTimestamp;\n    private int epoch;",
        "    /** 副本 Broker ID。 */\n    private long brokerId;\n    /** 最近一次收到心跳的时间戳。 */\n    private long lastUpdateTimestamp;\n    /** 当前副本 epoch，用于主从切换一致性判断。 */\n    private int epoch;",
    ),
    (
        "    private long maxOffset;\n    private long confirmOffset;\n    private Integer electionPriority;",
        "    /** 已写入 CommitLog 的最大偏移量。 */\n    private long maxOffset;\n    /** 已确认同步的偏移量。 */\n    private long confirmOffset;\n    /** 参与选主的优先级，数值越小优先级越高。 */\n    private Integer electionPriority;",
    ),
    (
        "    public BrokerLiveInfo(String brokerName, String brokerAddr, long brokerId, long lastUpdateTimestamp,\n        long heartbeatTimeoutMillis, Channel channel, int epoch, long maxOffset, Integer electionPriority) {",
        "    /** 构造不含 confirmOffset 的存活信息（首次注册场景）。 */\n    public BrokerLiveInfo(String brokerName, String brokerAddr, long brokerId, long lastUpdateTimestamp,\n        long heartbeatTimeoutMillis, Channel channel, int epoch, long maxOffset, Integer electionPriority) {",
    ),
    (
        "    public String getBrokerName() {\n        return brokerName;\n    }",
        "    /** 返回 Broker 名称。 */\n    public String getBrokerName() {\n        return brokerName;\n    }",
    ),
    (
        "    public long getConfirmOffset() {\n        return confirmOffset;\n    }",
        "    /** 返回已确认同步偏移量。 */\n    public long getConfirmOffset() {\n        return confirmOffset;\n    }",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/heartbeat/DefaultBrokerHeartbeatManager.java"] = [
    (
        "public class DefaultBrokerHeartbeatManager implements BrokerHeartbeatManager {",
        "/**\n * 单机 Controller 模式下的 Broker 心跳管理器：\n * 维护 {@link BrokerLiveInfo} 在线表，定时扫描超时 Broker 并通知生命周期监听器。\n */\npublic class DefaultBrokerHeartbeatManager implements BrokerHeartbeatManager {",
    ),
    (
        "    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONTROLLER_LOGGER_NAME);",
        "    /** Controller 模块日志记录器。 */\n    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONTROLLER_LOGGER_NAME);",
    ),
    (
        "    private final Map<BrokerIdentityInfo/* brokerIdentity*/, BrokerLiveInfo> brokerLiveTable;",
        "    /** 按 Broker 身份索引的存活信息表。 */\n    private final Map<BrokerIdentityInfo/* brokerIdentity*/, BrokerLiveInfo> brokerLiveTable;",
    ),
    (
        "    public DefaultBrokerHeartbeatManager(final ControllerConfig controllerConfig) {",
        "    /** @param controllerConfig Controller 配置，含扫描间隔等参数 */\n    public DefaultBrokerHeartbeatManager(final ControllerConfig controllerConfig) {",
    ),
    (
        "    @Override\n    public void start() {",
        "    /** 启动定时任务，周期性扫描非活跃 Broker。 */\n    @Override\n    public void start() {",
    ),
    (
        "    public void scanNotActiveBroker() {",
        "    /** 遍历在线表，移除超时 Broker 并关闭 Netty 通道、触发 inactive 回调。 */\n    public void scanNotActiveBroker() {",
    ),
    (
        "    private void notifyBrokerInActive(String clusterName, String brokerName, Long brokerId) {",
        "    /** 通知所有已注册的 {@link BrokerLifecycleListener} Broker 已离线。 */\n    private void notifyBrokerInActive(String clusterName, String brokerName, Long brokerId) {",
    ),
    (
        "    @Override\n    public void onBrokerHeartbeat(String clusterName, String brokerName, String brokerAddr, Long brokerId,",
        "    /**\n     * 处理 Broker 心跳：新 Broker 写入在线表，已有记录则刷新时间戳与 epoch/offset。\n     * 空字段使用默认值（如默认超时、最大选主优先级）。\n     */\n    @Override\n    public void onBrokerHeartbeat(String clusterName, String brokerName, String brokerAddr, Long brokerId,",
    ),
    (
        "    @Override\n    public void onBrokerChannelClose(Channel channel) {",
        "    /** Netty 通道关闭时，查找对应 Broker 并触发 inactive 通知。 */\n    @Override\n    public void onBrokerChannelClose(Channel channel) {",
    ),
    (
        "    @Override\n    public boolean isBrokerActive(String clusterName, String brokerName, Long brokerId) {",
        "    /** 根据最后心跳时间与超时阈值判断 Broker 是否仍在线。 */\n    @Override\n    public boolean isBrokerActive(String clusterName, String brokerName, Long brokerId) {",
    ),
    (
        "    @Override\n    public Map<String, Map<String, Integer>> getActiveBrokersNum() {",
        "    /** 统计各集群各 Broker 名称下当前活跃副本数量。 */\n    @Override\n    public Map<String, Map<String, Integer>> getActiveBrokersNum() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/heartbeat/RaftBrokerHeartBeatManager.java"] = [
    (
        "public class RaftBrokerHeartBeatManager implements BrokerHeartbeatManager {",
        "/**\n * Raft Controller 模式下的 Broker 心跳管理器：\n * 心跳与通道事件通过 JRaft 复制到 Leader 的 {@link RaftReplicasInfoManager}。\n */\npublic class RaftBrokerHeartBeatManager implements BrokerHeartbeatManager {",
    ),
    (
        "    private final Map<Channel, BrokerIdentityInfo> brokerChannelIdentityInfoMap = new HashMap<>();",
        "    /** Netty 通道到 Broker 身份的本地映射，用于通道关闭时定位副本。 */\n    private final Map<Channel, BrokerIdentityInfo> brokerChannelIdentityInfoMap = new HashMap<>();",
    ),
    (
        "    // resolve the scene\n    // when controller all down and startup again, we wait for some time to avoid electing a new leader,which is not necessary\n    private volatile long firstReceivedHeartbeatTime = -1;",
        "    // 解决 Controller 全宕重启场景：等待一段时间再扫描，避免不必要的选主\n    /** 首次收到 Broker 心跳的时间戳，用于启动宽限期判断。 */\n    private volatile long firstReceivedHeartbeatTime = -1;",
    ),
    (
        "    public void setController(JRaftController controller) {",
        "    /** 注入 JRaft Controller，用于提交心跳类 Raft 任务。 */\n    public void setController(JRaftController controller) {",
    ),
    (
        "    @Override\n    public void onBrokerHeartbeat(String clusterName, String brokerName, String brokerAddr, Long brokerId,",
        "    /** 将心跳事件封装为 {@link RaftBrokerHeartBeatEventRequest} 并同步提交至 Raft Leader。 */\n    @Override\n    public void onBrokerHeartbeat(String clusterName, String brokerName, String brokerAddr, Long brokerId,",
    ),
    (
        "    @Override\n    public void onBrokerChannelClose(Channel channel) {",
        "    /** 通道关闭时通过 Raft 提交 {@link BrokerCloseChannelRequest} 并通知监听器。 */\n    @Override\n    public void onBrokerChannelClose(Channel channel) {",
    ),
    (
        "    /**\n     * @param brokerIdentityInfo null means get broker live info of all brokers\n     */\n    private Map<BrokerIdentityInfo, BrokerLiveInfo> getBrokerLiveInfo(BrokerIdentityInfo brokerIdentityInfo) {",
        "    /**\n     * 经 Raft 查询 Broker 存活信息。\n     * @param brokerIdentityInfo 为 null 时返回全部 Broker 的存活表\n     */\n    private Map<BrokerIdentityInfo, BrokerLiveInfo> getBrokerLiveInfo(BrokerIdentityInfo brokerIdentityInfo) {",
    ),
    (
        "    private void scanNotActiveBroker() {",
        "    /** Leader 节点定时提交 {@link CheckNotActiveBrokerRequest}，清理超时 Broker 并关闭本地通道。 */\n    private void scanNotActiveBroker() {",
    ),
    (
        "        // if has not received any heartbeat from broker, we do not need to scan",
        "        // 尚未收到任何 Broker 心跳时跳过扫描，避免冷启动误报",
    ),
    (
        "    private void notifyBrokerInActive(String clusterName, String brokerName, Long brokerId) {",
        "    /** 广播 Broker 离线事件给生命周期监听器。 */\n    private void notifyBrokerInActive(String clusterName, String brokerName, Long brokerId) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/manager/BrokerReplicaInfo.java"] = [
    (
        "/**\n * Broker replicas info, mapping from brokerAddress to {brokerId, brokerHaAddress}.\n */\npublic class BrokerReplicaInfo implements Serializable {",
        "/**\n * Broker 副本注册信息：维护 brokerId 到 IP 地址与注册校验码的映射，\n * 并分配递增的下一个可用 brokerId。\n */\npublic class BrokerReplicaInfo implements Serializable {",
    ),
    (
        "    // Start from 1\n    private final AtomicLong nextAssignBrokerId;",
        "    /** 下一个待分配的 brokerId，从 {@link MixAll#FIRST_BROKER_CONTROLLER_ID} 起递增。 */\n    private final AtomicLong nextAssignBrokerId;",
    ),
    (
        "    private final Map<Long/*brokerId*/, Pair<String/*ipAddress*/, String/*registerCheckCode*/>> brokerIdInfo;",
        "    /** brokerId 到（IP 地址, 注册校验码）的并发安全映射。 */\n    private final Map<Long/*brokerId*/, Pair<String/*ipAddress*/, String/*registerCheckCode*/>> brokerIdInfo;",
    ),
    (
        "    public void addBroker(final Long brokerId, final String ipAddress, final String registerCheckCode) {",
        "    /** 注册新副本并递增 nextAssignBrokerId。 */\n    public void addBroker(final Long brokerId, final String ipAddress, final String registerCheckCode) {",
    ),
    (
        "    public boolean isBrokerExist(final Long brokerId) {",
        "    /** 判断指定 brokerId 是否已注册。 */\n    public boolean isBrokerExist(final Long brokerId) {",
    ),
    (
        "    public Map<Long, String> getBrokerIdTable() {",
        "    /** 返回 brokerId 到 IP 地址的快照表。 */\n    public Map<Long, String> getBrokerIdTable() {",
    ),
    (
        "    public String getBrokerRegisterCheckCode(final Long brokerId) {",
        "    /** 返回副本注册时生成的校验码，用于防重复注册。 */\n    public String getBrokerRegisterCheckCode(final Long brokerId) {",
    ),
    (
        "    public void updateBrokerAddress(final Long brokerId, final String brokerAddress) {",
        "    /** 更新已注册副本的 IP 地址，保留原校验码。 */\n    public void updateBrokerAddress(final Long brokerId, final String brokerAddress) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/manager/RaftReplicasInfoManager.java"] = [
    (
        "public class RaftReplicasInfoManager extends ReplicasInfoManager {",
        "/**\n * Raft 模式副本信息管理器：在父类同步状态集基础上维护 Broker 存活表，\n * 处理心跳、通道关闭与非活跃扫描等 Controller 状态机事件。\n */\npublic class RaftReplicasInfoManager extends ReplicasInfoManager {",
    ),
    (
        "    private final Map<BrokerIdentityInfo/* brokerIdentity*/, BrokerLiveInfo> brokerLiveTable = new ConcurrentHashMap<>(256);",
        "    /** Raft 状态机内持久化的 Broker 存活信息表。 */\n    private final Map<BrokerIdentityInfo/* brokerIdentity*/, BrokerLiveInfo> brokerLiveTable = new ConcurrentHashMap<>(256);",
    ),
    (
        "    public ControllerResult<GetBrokerLiveInfoResponse> getBrokerLiveInfo(final GetBrokerLiveInfoRequest request) {",
        "    /** 按请求中的 Broker 身份返回存活信息，空身份则返回全表 JSON 序列化结果。 */\n    public ControllerResult<GetBrokerLiveInfoResponse> getBrokerLiveInfo(final GetBrokerLiveInfoRequest request) {",
    ),
    (
        "    public ControllerResult<RaftBrokerHeartBeatEventResponse> onBrokerHeartBeat(\n        RaftBrokerHeartBeatEventRequest request) {",
        "    /** 应用 Broker 心跳：更新或插入存活记录，按 epoch/offset 规则合并副本进度。 */\n    public ControllerResult<RaftBrokerHeartBeatEventResponse> onBrokerHeartBeat(\n        RaftBrokerHeartBeatEventRequest request) {",
    ),
    (
        "    public ControllerResult<BrokerCloseChannelResponse> onBrokerCloseChannel(BrokerCloseChannelRequest request) {",
        "    /** Broker 通道关闭时从存活表移除对应身份。 */\n    public ControllerResult<BrokerCloseChannelResponse> onBrokerCloseChannel(BrokerCloseChannelRequest request) {",
    ),
    (
        "    public ControllerResult<CheckNotActiveBrokerResponse> checkNotActiveBroker(CheckNotActiveBrokerRequest request) {",
        "    /** 扫描超时 Broker，合并需重选主的 Broker 名并返回身份列表。 */\n    public ControllerResult<CheckNotActiveBrokerResponse> checkNotActiveBroker(CheckNotActiveBrokerRequest request) {",
    ),
    (
        "        // avoid to duplicate report, filter by name,\n        // because BrokerIdentityInfo in needReElectBrokerNames does not have brokerId or clusterName",
        "        // 按 brokerName 去重，needReElect 列表中的身份可能缺少 clusterName/brokerId",
    ),
    (
        "    public boolean isBrokerActive(String clusterName, String brokerName, Long brokerId, long invokeTime) {",
        "    /** 以指定调用时刻判断 Broker 心跳是否仍有效。 */\n    public boolean isBrokerActive(String clusterName, String brokerName, Long brokerId, long invokeTime) {",
    ),
    (
        "    @Override\n    public byte[] serialize() throws Throwable {",
        "    /** 序列化父类状态及 brokerLiveTable（Hessian 编码各条目）。 */\n    @Override\n    public byte[] serialize() throws Throwable {",
    ),
    (
        "    @Override\n    public void deserializeFrom(byte[] data) throws Throwable {",
        "    /** 从字节流反序列化父类数据与 brokerLiveTable。 */\n    @Override\n    public void deserializeFrom(byte[] data) throws Throwable {",
    ),
    (
        "    public static class BrokerValidPredicateWithInvokeTime implements BrokerValidPredicate {",
        "    /** 带调用时刻的 Broker 活跃性谓词，供扫描需重选主副本时使用。 */\n    public static class BrokerValidPredicateWithInvokeTime implements BrokerValidPredicate {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/manager/SyncStateInfo.java"] = [
    (
        "/**\n * Manages the syncStateSet of broker replicas.\n */\npublic class SyncStateInfo implements Serializable {",
        "/**\n * Broker 副本同步状态集（syncStateSet）管理：\n * 跟踪 Master brokerId、masterEpoch 及参与同步的副本 ID 集合。\n */\npublic class SyncStateInfo implements Serializable {",
    ),
    (
        "    private final AtomicInteger masterEpoch;\n    private final AtomicInteger syncStateSetEpoch;",
        "    /** Master 变更代数，每次换主递增。 */\n    private final AtomicInteger masterEpoch;\n    /** syncStateSet 变更代数，集合更新时递增。 */\n    private final AtomicInteger syncStateSetEpoch;",
    ),
    (
        "    private Set<Long/*brokerId*/> syncStateSet;\n\n    private Long masterBrokerId;",
        "    /** 当前处于同步状态的副本 brokerId 集合。 */\n    private Set<Long/*brokerId*/> syncStateSet;\n\n    /** 当前 Master 副本的 brokerId。 */\n    private Long masterBrokerId;",
    ),
    (
        "    public void updateMasterInfo(Long masterBrokerId) {",
        "    /** 记录新 Master 并递增 masterEpoch。 */\n    public void updateMasterInfo(Long masterBrokerId) {",
    ),
    (
        "    public void updateSyncStateSetInfo(Set<Long> newSyncStateSet) {",
        "    /** 替换 syncStateSet 并递增 syncStateSetEpoch。 */\n    public void updateSyncStateSetInfo(Set<Long> newSyncStateSet) {",
    ),
    (
        "    public boolean isFirstTimeForElect() {",
        "    /** 是否尚未完成首次选主（masterEpoch 为 0）。 */\n    public boolean isFirstTimeForElect() {",
    ),
    (
        "    public boolean isMasterExist() {",
        "    /** 当前是否已存在 Master 副本。 */\n    public boolean isMasterExist() {",
    ),
    (
        "    public void removeFromSyncState(final Long brokerId) {",
        "    /** 从 syncStateSet 中移除指定副本。 */\n    public void removeFromSyncState(final Long brokerId) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/task/BrokerCloseChannelRequest.java"] = [
    (
        "public class BrokerCloseChannelRequest implements CommandCustomHeader {",
        "/** Raft 通道关闭事件请求头：携带待移除的 {@link BrokerIdentityInfo}。 */\npublic class BrokerCloseChannelRequest implements CommandCustomHeader {",
    ),
    (
        "    @CFNullable\n    private String clusterName;",
        "    /** 集群名，可为空。 */\n    @CFNullable\n    private String clusterName;",
    ),
    (
        "    public BrokerCloseChannelRequest(BrokerIdentityInfo brokerIdentityInfo) {",
        "    /** 从 Broker 身份对象填充请求字段。 */\n    public BrokerCloseChannelRequest(BrokerIdentityInfo brokerIdentityInfo) {",
    ),
    (
        "    public BrokerIdentityInfo getBrokerIdentityInfo() {",
        "    /** 组装并返回 Broker 身份三元组。 */\n    public BrokerIdentityInfo getBrokerIdentityInfo() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/task/BrokerCloseChannelResponse.java"] = [
    (
        "public class BrokerCloseChannelResponse implements CommandCustomHeader {",
        "/** Broker 通道关闭 Raft 任务的空响应头，表示 Leader 已处理移除请求。 */\npublic class BrokerCloseChannelResponse implements CommandCustomHeader {",
    ),
    (
        "    public BrokerCloseChannelResponse() {",
        "    /** 默认无参构造。 */\n    public BrokerCloseChannelResponse() {",
    ),
    (
        "    @Override\n    public void checkFields() throws RemotingCommandException {",
        "    /** 本响应无额外字段需校验。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/task/CheckNotActiveBrokerRequest.java"] = [
    (
        "public class CheckNotActiveBrokerRequest implements CommandCustomHeader {",
        "/** 扫描非活跃 Broker 的 Raft 请求：携带发起扫描时的毫秒时间戳作为判定基准。 */\npublic class CheckNotActiveBrokerRequest implements CommandCustomHeader {",
    ),
    (
        "    private final Long checkTimeMillis = System.currentTimeMillis();",
        "    /** 扫描基准时刻，用于与最后心跳时间比较。 */\n    private final Long checkTimeMillis = System.currentTimeMillis();",
    ),
    (
        "    public Long getCheckTimeMillis() {",
        "    /** 返回扫描基准时间戳。 */\n    public Long getCheckTimeMillis() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/task/CheckNotActiveBrokerResponse.java"] = [
    (
        "public class CheckNotActiveBrokerResponse implements CommandCustomHeader {",
        "/** 非活跃 Broker 扫描响应头；具体身份列表在 Remoting 消息体 JSON 中返回。 */\npublic class CheckNotActiveBrokerResponse implements CommandCustomHeader {",
    ),
    (
        "    public CheckNotActiveBrokerResponse() {",
        "    /** 默认无参构造。 */\n    public CheckNotActiveBrokerResponse() {",
    ),
    (
        "    @Override\n    public void checkFields() throws RemotingCommandException {",
        "    /** 本响应无额外字段需校验。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/task/GetBrokerLiveInfoRequest.java"] = [
    (
        "public class GetBrokerLiveInfoRequest implements CommandCustomHeader {",
        "/** 查询 Broker 存活信息的 Raft 请求头，可按身份过滤或查询全表。 */\npublic class GetBrokerLiveInfoRequest implements CommandCustomHeader {",
    ),
    (
        "    /**\n     * @param brokerIdentity The BrokerIdentityInfo that needs to be queried, if it is null, it means obtaining BrokerLiveInfo for all brokers\n     */",
        "    /**\n     * @param brokerIdentity 待查询的 Broker 身份；为 null 时表示获取全部 Broker 存活信息\n     */",
    ),
    (
        "    public BrokerIdentityInfo getBrokerIdentity() {",
        "    /** 从请求字段组装 {@link BrokerIdentityInfo}。 */\n    public BrokerIdentityInfo getBrokerIdentity() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/task/GetBrokerLiveInfoResponse.java"] = [
    (
        "public class GetBrokerLiveInfoResponse implements CommandCustomHeader {",
        "/** 查询 Broker 存活信息的 Raft 响应头；存活表 JSON 在消息体中。 */\npublic class GetBrokerLiveInfoResponse implements CommandCustomHeader {",
    ),
    (
        "    public GetBrokerLiveInfoResponse() {",
        "    /** 默认无参构造。 */\n    public GetBrokerLiveInfoResponse() {",
    ),
    (
        "    @Override\n    public void checkFields() throws RemotingCommandException {",
        "    /** 本响应无额外字段需校验。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/task/GetSyncStateDataRequest.java"] = [
    (
        "public class GetSyncStateDataRequest implements CommandCustomHeader {",
        "/** 获取同步状态集数据的 Raft 请求，记录发起调用的毫秒时间戳。 */\npublic class GetSyncStateDataRequest implements CommandCustomHeader {",
    ),
    (
        "    private final Long invokeTime = System.currentTimeMillis();",
        "    /** 请求发起时刻，供状态机按时间点判断 Broker 活跃性。 */\n    private final Long invokeTime = System.currentTimeMillis();",
    ),
    (
        "    public Long getInvokeTime() {",
        "    /** 返回请求发起时间戳。 */\n    public Long getInvokeTime() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/task/RaftBrokerHeartBeatEventRequest.java"] = [
    (
        "public class RaftBrokerHeartBeatEventRequest implements CommandCustomHeader {",
        "/** Broker 心跳 Raft 事件请求：同时携带身份与 {@link BrokerLiveInfo} 快照字段。 */\npublic class RaftBrokerHeartBeatEventRequest implements CommandCustomHeader {",
    ),
    (
        "    // brokerIdentityInfo\n    private String clusterNameIdentityInfo;",
        "    // --- Broker 身份字段 ---\n    /** 集群名。 */\n    private String clusterNameIdentityInfo;",
    ),
    (
        "    // brokerLiveInfo\n    private String brokerName;",
        "    // --- BrokerLiveInfo 快照字段 ---\n    /** Broker 名称。 */\n    private String brokerName;",
    ),
    (
        "    public RaftBrokerHeartBeatEventRequest(BrokerIdentityInfo brokerIdentityInfo, BrokerLiveInfo brokerLiveInfo) {",
        "    /** 从身份与存活对象拷贝全部序列化字段。 */\n    public RaftBrokerHeartBeatEventRequest(BrokerIdentityInfo brokerIdentityInfo, BrokerLiveInfo brokerLiveInfo) {",
    ),
    (
        "    public BrokerLiveInfo getBrokerLiveInfo() {",
        "    /** 由请求字段重建 {@link BrokerLiveInfo}（通道字段为 null）。 */\n    public BrokerLiveInfo getBrokerLiveInfo() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/task/RaftBrokerHeartBeatEventResponse.java"] = [
    (
        "public class RaftBrokerHeartBeatEventResponse implements CommandCustomHeader {",
        "/** Broker 心跳 Raft 事件响应头，表示 Leader 已更新存活表。 */\npublic class RaftBrokerHeartBeatEventResponse implements CommandCustomHeader {",
    ),
    (
        "    public RaftBrokerHeartBeatEventResponse() {",
        "    /** 默认无参构造。 */\n    public RaftBrokerHeartBeatEventResponse() {",
    ),
    (
        "    @Override\n    public void checkFields() throws RemotingCommandException {",
        "    /** 本响应无额外字段需校验。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
    ),
]
