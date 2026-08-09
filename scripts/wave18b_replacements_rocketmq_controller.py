"""RocketMQ 5.5.0 wave18b controller impl/event/heartbeat [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["controller/src/main/java/org/apache/rocketmq/controller/impl/DLedgerControllerStateMachine.java"] = [
    (
        "/**\n * The state machine implementation of the dledger controller\n */",
        "/**\n * 基于 DLedger 的控制器状态机：将已提交日志反序列化为 {@link EventMessage} 并应用到副本状态。\n */",
    ),
    (
        "    private final ReplicasInfoManager replicasInfoManager;",
        "    /** 副本信息管理器，负责将事件写入内存元数据。 */\n    private final ReplicasInfoManager replicasInfoManager;",
    ),
    (
        "    private final EventSerializer eventSerializer;",
        "    /** 控制器事件序列化与反序列化工具。 */\n    private final EventSerializer eventSerializer;",
    ),
    (
        "    private final String dLedgerId;",
        "    /** 本节点绑定的 DLedger 标识（groupId#selfId）。 */\n    private final String dLedgerId;",
    ),
    (
        "    public DLedgerControllerStateMachine(final ReplicasInfoManager replicasInfoManager,",
        "    /** 构造 DLedger 控制器状态机并生成节点标识。 */\n    public DLedgerControllerStateMachine(final ReplicasInfoManager replicasInfoManager,",
    ),
    (
        "    public void onApply(CommittedEntryIterator iterator) {",
        "    /** 批量应用已提交的 DLedger 日志条目到副本状态。 */\n    public void onApply(CommittedEntryIterator iterator) {",
    ),
    (
        "    public boolean onSnapshotSave(SnapshotWriter writer) {",
        "    /** 保存快照（当前实现直接返回 true，未持久化状态）。 */\n    public boolean onSnapshotSave(SnapshotWriter writer) {",
    ),
    (
        "    public boolean onSnapshotLoad(SnapshotReader reader) {",
        "    /** 加载快照（当前实现返回 false，不从快照恢复）。 */\n    public boolean onSnapshotLoad(SnapshotReader reader) {",
    ),
    (
        "    public void onShutdown() {",
        "    /** 状态机关闭回调，记录日志。 */\n    public void onShutdown() {",
    ),
    (
        "    public void onError(DLedgerException exception) {",
        "    /** DLedger 运行异常回调，记录错误并提示排查节点。 */\n    public void onError(DLedgerException exception) {",
    ),
    (
        "    public String getBindDLedgerId() {",
        "    /** 返回绑定的 DLedger 节点 ID。 */\n    public String getBindDLedgerId() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/JRaftController.java"] = [
    (
        "public class JRaftController implements Controller {",
        "/**\n * 基于 SOFA JRaft 的 RocketMQ 控制器：通过 Raft 共识管理 Broker 元数据与主从选举。\n */\npublic class JRaftController implements Controller {",
    ),
    (
        "    private final RaftGroupService raftGroupService;",
        "    /** JRaft Raft 组服务，负责节点启动与关闭。 */\n    private final RaftGroupService raftGroupService;",
    ),
    (
        "    private Node node;",
        "    /** 当前 JRaft 节点实例。 */\n    private Node node;",
    ),
    (
        "    private final JRaftControllerStateMachine stateMachine;",
        "    /** 控制器状态机，处理已提交的 Remoting 请求。 */\n    private final JRaftControllerStateMachine stateMachine;",
    ),
    (
        "    private final ControllerConfig controllerConfig;",
        "    /** 控制器运行时配置（选举超时、存储路径等）。 */\n    private final ControllerConfig controllerConfig;",
    ),
    (
        "    private final List<BrokerLifecycleListener> brokerLifecycleListeners;",
        "    /** Broker 生命周期监听器列表。 */\n    private final List<BrokerLifecycleListener> brokerLifecycleListeners;",
    ),
    (
        "    private final NettyRemotingServer remotingServer;",
        "    /** 对外提供 Controller RPC 的 Netty 服务端。 */\n    private final NettyRemotingServer remotingServer;",
    ),
    (
        "    public JRaftController(ControllerConfig controllerConfig,",
        "    /** 初始化 JRaft 节点、状态机与 Remoting 服务。 */\n    public JRaftController(ControllerConfig controllerConfig,",
    ),
    (
        "    private void initPeerIdMap() {",
        "    /** 解析初始集群配置，建立 PeerId 到 RPC 地址的映射。 */\n    private void initPeerIdMap() {",
    ),
    (
        "    public void startup() {",
        "    /** 启动 Remoting 服务与 JRaft 节点。 */\n    public void startup() {",
    ),
    (
        "    public void shutdown() {",
        "    /** 停止调度、关闭 Raft 组与 Remoting 服务。 */\n    public void shutdown() {",
    ),
    (
        "    public boolean isLeaderState() {",
        "    /** 判断当前节点是否为 Raft Leader。 */\n    public boolean isLeaderState() {",
    ),
    (
        "    private <T extends CommandCustomHeader> CompletableFuture<RemotingCommand> applyToJRaft(RemotingCommand request) {",
        "    /** 将 Remoting 请求封装为 JRaft Task 并提交到 Leader 节点。 */\n    private <T extends CommandCustomHeader> CompletableFuture<RemotingCommand> applyToJRaft(RemotingCommand request) {",
    ),
    (
        "    public void onLeaderStart(long term) {",
        "    /** Leader 任期开始时的回调。 */\n    public void onLeaderStart(long term) {",
    ),
    (
        "    public void onLeaderStop(Status status) {",
        "    /** Leader 任期结束时的回调，停止调度任务。 */\n    public void onLeaderStop(Status status) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/JRaftControllerStateMachine.java"] = [
    (
        "public class JRaftControllerStateMachine implements StateMachine {",
        "/**\n * JRaft 控制器状态机：解码 Remoting 请求、执行业务逻辑并生成 {@link ControllerResult}。\n */\npublic class JRaftControllerStateMachine implements StateMachine {",
    ),
    (
        "    private final List<Consumer<Long>> onLeaderStartCallbacks;",
        "    /** Leader 启动回调列表。 */\n    private final List<Consumer<Long>> onLeaderStartCallbacks;",
    ),
    (
        "    private final List<Consumer<Status>> onLeaderStopCallbacks;",
        "    /** Leader 停止回调列表。 */\n    private final List<Consumer<Status>> onLeaderStopCallbacks;",
    ),
    (
        "    private final RaftReplicasInfoManager replicasInfoManager;",
        "    /** Raft 模式下的副本信息管理器。 */\n    private final RaftReplicasInfoManager replicasInfoManager;",
    ),
    (
        "    private final NodeId nodeId;",
        "    /** 本 JRaft 节点的唯一标识。 */\n    private final NodeId nodeId;",
    ),
    (
        "    public void onApply(Iterator iter) {",
        "    /** 逐条应用已提交的 Raft 日志到状态机。 */\n    public void onApply(Iterator iter) {",
    ),
    (
        "    private void processEvent(ControllerClosure controllerClosure, byte[] data, long term, long index) {",
        "    /** 按请求码分发处理 Controller 事件并回填 Closure 结果。 */\n    private void processEvent(ControllerClosure controllerClosure, byte[] data, long term, long index) {",
    ),
    (
        "    private ControllerResult<ElectMasterResponseHeader> electMaster(ElectMasterRequestHeader request) {",
        "    /** 执行主 Broker 选举并记录选举指标。 */\n    private ControllerResult<ElectMasterResponseHeader> electMaster(ElectMasterRequestHeader request) {",
    ),
    (
        "    public void onSnapshotSave(SnapshotWriter writer, Closure done) {",
        "    /** 异步序列化副本状态并写入快照文件。 */\n    public void onSnapshotSave(SnapshotWriter writer, Closure done) {",
    ),
    (
        "    public boolean onSnapshotLoad(SnapshotReader reader) {",
        "    /** 从快照文件反序列化并恢复副本状态。 */\n    public boolean onSnapshotLoad(SnapshotReader reader) {",
    ),
    (
        "    public void registerOnLeaderStart(Consumer<Long> callback) {",
        "    /** 注册 Leader 启动回调。 */\n    public void registerOnLeaderStart(Consumer<Long> callback) {",
    ),
    (
        "    public void registerOnLeaderStop(Consumer<Status> callback) {",
        "    /** 注册 Leader 停止回调。 */\n    public void registerOnLeaderStop(Consumer<Status> callback) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/closure/ControllerClosure.java"] = [
    (
        "public class ControllerClosure implements Closure {",
        "/**\n * JRaft 请求闭包：将 Remoting 请求提交到 Raft 并在提交完成后返回响应。\n */\npublic class ControllerClosure implements Closure {",
    ),
    (
        "    private final RemotingCommand requestEvent;",
        "    /** 待提交的 Remoting 请求。 */\n    private final RemotingCommand requestEvent;",
    ),
    (
        "    private final CompletableFuture<RemotingCommand> future;",
        "    /** 异步响应 Future，供调用方等待结果。 */\n    private final CompletableFuture<RemotingCommand> future;",
    ),
    (
        "    private ControllerResult<?> controllerResult;",
        "    /** 状态机处理完成后填充的业务结果。 */\n    private ControllerResult<?> controllerResult;",
    ),
    (
        "    private Task task;",
        "    /** 关联的 JRaft Task 对象。 */\n    private Task task;",
    ),
    (
        "    public ControllerClosure(RemotingCommand requestEvent) {",
        "    /** 构造闭包并初始化异步 Future。 */\n    public ControllerClosure(RemotingCommand requestEvent) {",
    ),
    (
        "    public CompletableFuture<RemotingCommand> getFuture() {",
        "    /** 返回等待 Raft 提交完成的 Future。 */\n    public CompletableFuture<RemotingCommand> getFuture() {",
    ),
    (
        "    public void setControllerResult(ControllerResult<?> controllerResult) {",
        "    /** 设置状态机处理结果，供 run 时构建响应。 */\n    public void setControllerResult(ControllerResult<?> controllerResult) {",
    ),
    (
        "    public void run(Status status) {",
        "    /** Raft 提交完成回调：成功则封装响应，失败则返回内部错误码。 */\n    public void run(Status status) {",
    ),
    (
        "    public Task taskWithThisClosure() {",
        "    /** 构建携带本闭包的 JRaft Task（编码请求体为日志数据）。 */\n    public Task taskWithThisClosure() {",
    ),
    (
        "    public RemotingCommand getRequestEvent() {",
        "    /** 返回原始 Remoting 请求。 */\n    public RemotingCommand getRequestEvent() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/event/AlterSyncStateSetEvent.java"] = [
    (
        "/**\n * The event alters the syncStateSet of target broker.\n * Triggered by the AlterSyncStateSetApi.\n */",
        "/**\n * 修改目标 Broker 同步副本集（syncStateSet）的控制器事件。\n * 由 AlterSyncStateSet API 触发。\n */",
    ),
    (
        "    private final String brokerName;",
        "    /** 目标 Broker 名称。 */\n    private final String brokerName;",
    ),
    (
        "    private final Set<Long/*BrokerId*/> newSyncStateSet;",
        "    /** 新的同步副本 BrokerId 集合。 */\n    private final Set<Long/*BrokerId*/> newSyncStateSet;",
    ),
    (
        "    public AlterSyncStateSetEvent(String brokerName, Set<Long> newSyncStateSet) {",
        "    /** 构造同步副本集变更事件（防御性拷贝集合）。 */\n    public AlterSyncStateSetEvent(String brokerName, Set<Long> newSyncStateSet) {",
    ),
    (
        "    public String getBrokerName() {",
        "    /** 返回目标 Broker 名称。 */\n    public String getBrokerName() {",
    ),
    (
        "    public Set<Long> getNewSyncStateSet() {",
        "    /** 返回新的同步副本集副本（返回拷贝）。 */\n    public Set<Long> getNewSyncStateSet() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/event/ApplyBrokerIdEvent.java"] = [
    (
        "/**\n * The event trys to apply a new id for a new broker.\n * Triggered by the RegisterBrokerApi.\n */",
        "/**\n * 为新 Broker 申请 BrokerId 的控制器事件。\n * 由 RegisterBroker API 触发。\n */",
    ),
    (
        "    private final String clusterName;",
        "    /** 集群名称。 */\n    private final String clusterName;",
    ),
    (
        "    private final String brokerName;",
        "    /** Broker 名称。 */\n    private final String brokerName;",
    ),
    (
        "    private final String brokerAddress;",
        "    /** Broker 注册地址。 */\n    private final String brokerAddress;",
    ),
    (
        "    private final String registerCheckCode;",
        "    /** 注册校验码，用于防重放。 */\n    private final String registerCheckCode;",
    ),
    (
        "    private final long newBrokerId;",
        "    /** 待分配的新 BrokerId。 */\n    private final long newBrokerId;",
    ),
    (
        "    public ApplyBrokerIdEvent(String clusterName, String brokerName, String brokerAddress, long newBrokerId,",
        "    /** 构造 BrokerId 申请事件。 */\n    public ApplyBrokerIdEvent(String clusterName, String brokerName, String brokerAddress, long newBrokerId,",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/event/CleanBrokerDataEvent.java"] = [
    (
        "public class CleanBrokerDataEvent implements EventMessage {",
        "/**\n * 清理指定 Broker 元数据的控制器事件，用于下线或故障恢复场景。\n */\npublic class CleanBrokerDataEvent implements EventMessage {",
    ),
    (
        "    private String brokerName;",
        "    /** 待清理的 Broker 名称。 */\n    private String brokerName;",
    ),
    (
        "    private Set<Long> brokerIdSetToClean;",
        "    /** 需要清理的 BrokerId 集合。 */\n    private Set<Long> brokerIdSetToClean;",
    ),
    (
        "    public CleanBrokerDataEvent(String brokerName, Set<Long> brokerIdSetToClean) {",
        "    /** 构造 Broker 数据清理事件。 */\n    public CleanBrokerDataEvent(String brokerName, Set<Long> brokerIdSetToClean) {",
    ),
    (
        "    /**\n     * Returns the event type of this message\n     */",
        "    /** 返回事件类型 {@link EventType#CLEAN_BROKER_DATA_EVENT}。 */",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/event/ControllerResult.java"] = [
    (
        "public class ControllerResult<T> {",
        "/**\n * 控制器请求处理结果：包含待应用的 {@link EventMessage} 列表与 RPC 响应头/体。\n */\npublic class ControllerResult<T> {",
    ),
    (
        "    private final List<EventMessage> events;",
        "    /** 需写入 Raft 日志并应用到状态机的事件列表。 */\n    private final List<EventMessage> events;",
    ),
    (
        "    private final T response;",
        "    /** RPC 响应头对象。 */\n    private final T response;",
    ),
    (
        "    private byte[] body;",
        "    /** 可选的响应体字节数组。 */\n    private byte[] body;",
    ),
    (
        "    private int responseCode = ResponseCode.SUCCESS;",
        "    /** Remoting 响应码，默认成功。 */\n    private int responseCode = ResponseCode.SUCCESS;",
    ),
    (
        "    private String remark;",
        "    /** 响应备注信息。 */\n    private String remark;",
    ),
    (
        "    public static <T> ControllerResult<T> of(List<EventMessage> events, T response) {",
        "    /** 工厂方法：由事件列表与响应头构造结果。 */\n    public static <T> ControllerResult<T> of(List<EventMessage> events, T response) {",
    ),
    (
        "    public void setCodeAndRemark(int responseCode, String remark) {",
        "    /** 设置响应码与备注。 */\n    public void setCodeAndRemark(int responseCode, String remark) {",
    ),
    (
        "    public void addEvent(EventMessage event) {",
        "    /** 追加待应用的控制器事件。 */\n    public void addEvent(EventMessage event) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/event/ElectMasterEvent.java"] = [
    (
        "/**\n * The event trys to elect a new master for target broker.\n * Triggered by the ElectMasterApi.\n */",
        "/**\n * 为目标 Broker 选举新 Master 的控制器事件。\n * 由 ElectMaster API 触发。\n */",
    ),
    (
        "    // Mark whether a new master was elected.",
        "    // 标记是否选举出了新的 Master。",
    ),
    (
        "    private final boolean newMasterElected;",
        "    /** 是否成功选举新 Master。 */\n    private final boolean newMasterElected;",
    ),
    (
        "    private final String brokerName;",
        "    /** 目标 Broker 名称。 */\n    private final String brokerName;",
    ),
    (
        "    private final Long newMasterBrokerId;",
        "    /** 新 Master 的 BrokerId（未选举时为 null）。 */\n    private final Long newMasterBrokerId;",
    ),
    (
        "    public ElectMasterEvent(boolean newMasterElected, String brokerName, Long newMasterBrokerId) {",
        "    /** 构造主从选举事件。 */\n    public ElectMasterEvent(boolean newMasterElected, String brokerName, Long newMasterBrokerId) {",
    ),
    (
        "    public boolean getNewMasterElected() {",
        "    /** 返回是否选举了新 Master。 */\n    public boolean getNewMasterElected() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/event/EventMessage.java"] = [
    (
        "/**\n * The parent class of Event, the subclass needs to indicate eventType.\n */",
        "/**\n * 控制器事件消息接口，具体子类需通过 {@link #getEventType()} 声明事件类型。\n */",
    ),
    (
        "    /**\n     * Returns the event type of this message\n     */",
        "    /**\n     * 返回本消息的事件类型。\n     */",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/event/EventSerializer.java"] = [
    (
        "/**\n * EventMessage serializer\n */",
        "/**\n * 单条 {@link EventMessage} 的序列化器：前缀 2 字节事件类型 ID + JSON 体。\n */",
    ),
    (
        "    private final FastJsonSerializer serializer;",
        "    /** 底层 FastJson 序列化实现。 */\n    private final FastJsonSerializer serializer;",
    ),
    (
        "    private void putShort(byte[] memory, int index, int value) {",
        "    /** 向字节数组写入 big-endian short。 */\n    private void putShort(byte[] memory, int index, int value) {",
    ),
    (
        "    private short getShort(byte[] memory, int index) {",
        "    /** 从字节数组读取 big-endian short。 */\n    private short getShort(byte[] memory, int index) {",
    ),
    (
        "    public byte[] serialize(EventMessage message) throws SerializationException {",
        "    /** 序列化事件：2 字节类型 ID + JSON 数据。 */\n    public byte[] serialize(EventMessage message) throws SerializationException {",
    ),
    (
        "    public EventMessage deserialize(byte[] bytes) throws SerializationException {",
        "    /** 反序列化事件：解析类型 ID 后按类型分发到具体事件类。 */\n    public EventMessage deserialize(byte[] bytes) throws SerializationException {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/event/EventType.java"] = [
    (
        "/**\n * Event type (name, id);\n */",
        "/**\n * 控制器事件类型枚举：定义事件名称与 DLedger/JRaft 日志中的类型 ID。\n */",
    ),
    (
        "    ALTER_SYNC_STATE_SET_EVENT(\"AlterSyncStateSetEvent\", (short) 1),",
        "    /** 修改同步副本集事件。 */\n    ALTER_SYNC_STATE_SET_EVENT(\"AlterSyncStateSetEvent\", (short) 1),",
    ),
    (
        "    APPLY_BROKER_ID_EVENT(\"ApplyBrokerIdEvent\", (short) 2),",
        "    /** 申请 BrokerId 事件。 */\n    APPLY_BROKER_ID_EVENT(\"ApplyBrokerIdEvent\", (short) 2),",
    ),
    (
        "    ELECT_MASTER_EVENT(\"ElectMasterEvent\", (short) 3),",
        "    /** 主 Broker 选举事件。 */\n    ELECT_MASTER_EVENT(\"ElectMasterEvent\", (short) 3),",
    ),
    (
        "    READ_EVENT(\"ReadEvent\", (short) 4),",
        "    /** 只读占位事件（预留）。 */\n    READ_EVENT(\"ReadEvent\", (short) 4),",
    ),
    (
        "    CLEAN_BROKER_DATA_EVENT(\"CleanBrokerDataEvent\", (short) 5),",
        "    /** 清理 Broker 元数据事件。 */\n    CLEAN_BROKER_DATA_EVENT(\"CleanBrokerDataEvent\", (short) 5),",
    ),
    (
        "    UPDATE_BROKER_ADDRESS(\"UpdateBrokerAddressEvent\", (short) 6);",
        "    /** 更新 Broker 地址事件。 */\n    UPDATE_BROKER_ADDRESS(\"UpdateBrokerAddressEvent\", (short) 6);",
    ),
    (
        "    public static EventType from(short id) {",
        "    /** 按类型 ID 解析枚举值，未知 ID 返回 null。 */\n    public static EventType from(short id) {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/event/ListEventSerializer.java"] = [
    (
        "public class ListEventSerializer {",
        "/**\n * 批量 {@link EventMessage} 序列化器：每条记录为 type(2B) + length(4B) + JSON 体。\n */\npublic class ListEventSerializer {",
    ),
    (
        "    private static final Serializer SERIALIZER = new FastJsonSerializer();",
        "    /** 共享 FastJson 序列化器实例。 */\n    private static final Serializer SERIALIZER = new FastJsonSerializer();",
    ),
    (
        "    public static byte[] serialize(List<EventMessage> message, Logger log) throws SerializationException {",
        "    /** 将事件列表序列化为连续字节流，单条失败则记录日志并跳过。 */\n    public static byte[] serialize(List<EventMessage> message, Logger log) throws SerializationException {",
    ),
    (
        "    public static List<EventMessage> deserialize(byte[] bytes, Logger log) throws SerializationException {",
        "    /** 从字节流逐条解析事件，格式错误或未知类型则记录日志。 */\n    public static List<EventMessage> deserialize(byte[] bytes, Logger log) throws SerializationException {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/event/UpdateBrokerAddressEvent.java"] = [
    (
        "public class UpdateBrokerAddressEvent implements EventMessage {",
        "/**\n * 更新 Broker 注册地址的控制器事件，Broker 重启或迁移 IP 时触发。\n */\npublic class UpdateBrokerAddressEvent implements EventMessage {",
    ),
    (
        "    private String clusterName;",
        "    /** 集群名称。 */\n    private String clusterName;",
    ),
    (
        "    private String brokerName;",
        "    /** Broker 名称。 */\n    private String brokerName;",
    ),
    (
        "    private String brokerAddress;",
        "    /** 新的 Broker 地址。 */\n    private String brokerAddress;",
    ),
    (
        "    private Long brokerId;",
        "    /** 目标 BrokerId。 */\n    private Long brokerId;",
    ),
    (
        "    public UpdateBrokerAddressEvent(String clusterName, String brokerName, String brokerAddress, Long brokerId) {",
        "    /** 构造 Broker 地址更新事件。 */\n    public UpdateBrokerAddressEvent(String clusterName, String brokerName, String brokerAddress, Long brokerId) {",
    ),
    (
        "    public EventType getEventType() {",
        "    /** 返回 {@link EventType#UPDATE_BROKER_ADDRESS}。 */\n    public EventType getEventType() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/impl/heartbeat/BrokerIdentityInfo.java"] = [
    (
        "public class BrokerIdentityInfo implements Serializable {",
        "/**\n * Broker 身份标识：由集群名、Broker 名与 BrokerId 唯一确定一个副本。\n */\npublic class BrokerIdentityInfo implements Serializable {",
    ),
    (
        "    private final String clusterName;",
        "    /** 集群名称。 */\n    private final String clusterName;",
    ),
    (
        "    private final String brokerName;",
        "    /** Broker 名称（Broker Set）。 */\n    private final String brokerName;",
    ),
    (
        "    private final Long brokerId;",
        "    /** Broker 副本 ID。 */\n    private final Long brokerId;",
    ),
    (
        "    public BrokerIdentityInfo(String clusterName, String brokerName, Long brokerId) {",
        "    /** 构造不可变 Broker 身份三元组。 */\n    public BrokerIdentityInfo(String clusterName, String brokerName, Long brokerId) {",
    ),
    (
        "    public boolean isEmpty() {",
        "    /** 判断集群名、Broker 名与 ID 是否均为空。 */\n    public boolean isEmpty() {",
    ),
    (
        "    public boolean equals(Object obj) {",
        "    /** 按 clusterName、brokerName、brokerId 三元组判等。 */\n    public boolean equals(Object obj) {",
    ),
]
