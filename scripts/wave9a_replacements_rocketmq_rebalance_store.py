"""RocketMQ 5.5.0 wave9a rebalance/store/exception [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R['client/src/main/java/org/apache/rocketmq/client/consumer/rebalance/AllocateMachineRoomNearby.java'] = [
    (
        '/**\n * An allocate strategy proxy for based on machine room nearside priority. An actual allocate strategy can be\n * specified.\n *\n * If any consumer is alive in a machine room, the message queue of the broker which is deployed in the same machine\n * should only be allocated to those. Otherwise, those message queues can be shared along all consumers since there are\n * no alive consumer to monopolize them.\n */',
        '/**\n * 基于机房就近优先级的队列分配策略代理，可指定底层实际分配算法。\n *\n * 若某机房内有存活消费者，则部署在同一机房的 Broker 消息队列仅分配给该机房消费者；\n * 若某机房无存活消费者，其队列可由全部消费者共享。\n */',
    ),
    (
        '    private final AllocateMessageQueueStrategy allocateMessageQueueStrategy;//actual allocate strategy',
        '    /** 底层实际分配策略。 */\n    private final AllocateMessageQueueStrategy allocateMessageQueueStrategy;',
    ),
    (
        '    private final MachineRoomResolver machineRoomResolver;',
        '    /** 机房解析器，用于判定 Broker/消费者所属机房。 */\n    private final MachineRoomResolver machineRoomResolver;',
    ),
    (
        '        //group mq by machine room',
        '        // 按机房分组消息队列',
    ),
    (
        '        //group consumer by machine room',
        '        // 按机房分组消费者',
    ),
    (
        '        //1.allocate the mq that deploy in the same machine room with the current consumer',
        '        // 1. 优先分配与当前消费者同机房的队列',
    ),
    (
        '            if (!mr2c.containsKey(machineRoomEntry.getKey())) { // no alive consumer in the corresponding machine room, so all consumers share these queues',
        '            if (!mr2c.containsKey(machineRoomEntry.getKey())) { // 对应机房无存活消费者，队列由全部消费者共享',
    ),
    (
        '        //2.allocate the rest mq to each machine room if there are no consumer alive in that machine room',
        '        // 2. 对无存活消费者的机房，将其剩余队列分配给全部消费者',
    ),
    (
        '    /**\n     * A resolver object to determine which machine room do the message queues or clients are deployed in.\n     *\n     * AllocateMachineRoomNearby will use the results to group the message queues and clients by machine room.\n     *\n     * The result returned from the implemented method CANNOT be null.\n     */',
        '    /**\n     * 解析消息队列与消费者所属机房的接口。\n     *\n     * {@link AllocateMachineRoomNearby} 据此按机房分组队列与消费者。\n     *\n     * 实现方法返回值不可为 null。\n     */',
    ),
    (
        '        String brokerDeployIn(MessageQueue messageQueue);',
        '        /** 返回消息队列所在 Broker 的机房标识。 */\n        String brokerDeployIn(MessageQueue messageQueue);',
    ),
    (
        '        String consumerDeployIn(String clientID);',
        '        /** 返回消费者 clientID 所在机房标识。 */\n        String consumerDeployIn(String clientID);',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/rebalance/AllocateMessageQueueAveragely.java'] = [
    (
        '/**\n * Average Hashing queue algorithm\n */',
        '/**\n * 平均分配消息队列算法：在消费组内按消费者数量均分队列。\n */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/rebalance/AllocateMessageQueueAveragelyByCircle.java'] = [
    (
        '/**\n * Cycle average Hashing queue algorithm\n */',
        '/**\n * 环形平均分配算法：按消费者索引轮询分配消息队列。\n */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/rebalance/AllocateMessageQueueByConfig.java'] = [
    (
        'public class AllocateMessageQueueByConfig extends AbstractAllocateMessageQueueStrategy {',
        '/**\n * 按配置固定返回指定消息队列列表的分配策略。\n */\npublic class AllocateMessageQueueByConfig extends AbstractAllocateMessageQueueStrategy {',
    ),
    (
        '    private List<MessageQueue> messageQueueList;',
        '    /** 预配置的消息队列列表。 */\n    private List<MessageQueue> messageQueueList;',
    ),
    (
        '    public List<MessageQueue> getMessageQueueList() {',
        '    /** 获取配置的消息队列列表。 */\n    public List<MessageQueue> getMessageQueueList() {',
    ),
    (
        '    public void setMessageQueueList(List<MessageQueue> messageQueueList) {',
        '    /** 设置配置的消息队列列表。 */\n    public void setMessageQueueList(List<MessageQueue> messageQueueList) {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/rebalance/AllocateMessageQueueByMachineRoom.java'] = [
    (
        '/**\n * Computer room Hashing queue algorithm, such as Alipay logic room\n */',
        '/**\n * 按机房（逻辑机房/IDC）分配消息队列算法，如支付宝逻辑机房场景。\n */',
    ),
    (
        '    private Set<String> consumeridcs;',
        '    /** 消费者所在机房（IDC）标识集合。 */\n    private Set<String> consumeridcs;',
    ),
    (
        '    public Set<String> getConsumeridcs() {',
        '    /** 获取消费者 IDC 集合。 */\n    public Set<String> getConsumeridcs() {',
    ),
    (
        '    public void setConsumeridcs(Set<String> consumeridcs) {',
        '    /** 设置消费者 IDC 集合。 */\n    public void setConsumeridcs(Set<String> consumeridcs) {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/rebalance/AllocateMessageQueueConsistentHash.java'] = [
    (
        '/**\n * Consistent Hashing queue algorithm\n */',
        '/**\n * 一致性哈希消息队列分配算法：队列经哈希环路由到对应消费者。\n */',
    ),
    (
        '    private final int virtualNodeCnt;',
        '    /** 每个消费者的虚拟节点数量。 */\n    private final int virtualNodeCnt;',
    ),
    (
        '    private final HashFunction customHashFunction;',
        '    /** 自定义哈希函数，null 时使用默认实现。 */\n    private final HashFunction customHashFunction;',
    ),
    (
        '        final ConsistentHashRouter<ClientNode> router; //for building hash ring',
        '        final ConsistentHashRouter<ClientNode> router; // 构建一致性哈希环',
    ),
    (
        '    private static class ClientNode implements Node {',
        '    /** 一致性哈希环上的消费者节点。 */\n    private static class ClientNode implements Node {',
    ),
    (
        '        private final String clientID;',
        '        /** 消费者 clientID。 */\n        private final String clientID;',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/store/ControllableOffset.java'] = [
    (
        '/**\n * The ControllableOffset class encapsulates a thread-safe offset value that can be\n * updated atomically. Additionally, this class allows for the offset to be "frozen,"\n * which prevents further updates after the freeze operation has been performed.\n * <p>\n * Concurrency Scenarios:\n * If {@code updateAndFreeze} is called before any {@code update} operations, it sets\n * {@code allowToUpdate} to false and updates the offset to the target value specified.\n * After this operation, further invocations of {@code update} will not affect the offset,\n * as it is considered frozen.\n * <p>\n * If {@code update} is in progress while {@code updateAndFreeze} is invoked concurrently,\n * the final outcome depends on the sequence of operations:\n * 1. If {@code update}\'s atomic update operation completes before {@code updateAndFreeze},\n * the latter will overwrite the offset and set {@code allowToUpdate} to false,\n * preventing any further updates.\n * 2. If {@code updateAndFreeze} executes before the {@code update} finalizes its operation,\n * the ongoing {@code update} will not proceed with its changes. The {@link AtomicLong#getAndUpdate}\n * method used in both operations ensures atomicity and respects the final state imposed by\n * {@code updateAndFreeze}, even if the {@code update} function has already begun.\n * <p>\n * In essence, once the {@code updateAndFreeze} operation is executed, the offset value remains\n * immutable to any subsequent {@code update} calls due to the immediate visibility of the\n * {@code allowToUpdate} state change, courtesy of its volatile nature.\n * <p>\n * The combination of an AtomicLong for the offset value and a volatile boolean flag for update\n * control provides a reliable mechanism for managing offset values in concurrent environments.\n */',
        '/**\n * 线程安全的可控消费偏移量：支持原子更新与冻结（freeze），冻结后不可再更新。\n * <p>\n * 并发场景说明：\n * 若在任意 {@code update} 之前调用 {@code updateAndFreeze}，会将 {@code allowToUpdate} 置为 false\n * 并将偏移量设为目标值，此后 {@code update} 不再生效。\n * <p>\n * 若 {@code update} 与 {@code updateAndFreeze} 并发执行，最终结果取决于操作顺序：\n * 1. 若 {@code update} 的原子更新先完成，{@code updateAndFreeze} 会覆盖偏移量并禁止后续更新；\n * 2. 若 {@code updateAndFreeze} 先执行，进行中的 {@code update} 不会生效。\n * {@link AtomicLong#getAndUpdate} 保证原子性并尊重 {@code updateAndFreeze} 的最终状态。\n * <p>\n * 一旦执行 {@code updateAndFreeze}，由于 {@code allowToUpdate} 的 volatile 可见性，\n * 后续 {@code update} 调用均无法改变偏移量。\n */',
    ),
    (
        '    // Holds the current offset value in an atomic way.',
        '    // 原子方式保存当前偏移量',
    ),
    (
        '    // Controls whether updates to the offset are allowed.',
        '    // 控制是否允许更新偏移量',
    ),
    (
        '    /**\n     * Attempts to update the offset to the target value. If increaseOnly is true,\n     * the offset will not be decreased. The update operation is atomic and thread-safe.\n     * The operation will respect the current allowToUpdate state, and if the offset\n     * has been frozen by a previous call to {@link #updateAndFreeze(long)},\n     * this method will not update the offset.\n     *\n     * @param target       the new target offset value.\n     * @param increaseOnly if true, the offset will only be updated if the target value\n     *                     is greater than the current value.\n     */',
        '    /**\n     * 尝试将偏移量更新为目标值。{@code increaseOnly} 为 true 时仅允许增大。\n     * 操作原子且线程安全；若偏移量已被 {@link #updateAndFreeze(long)} 冻结则不再更新。\n     *\n     * @param target 目标偏移量\n     * @param increaseOnly 为 true 时仅当目标值大于当前值才更新\n     */',
    ),
    (
        '    /**\n     * Overloaded method for updating the offset value unconditionally.\n     *\n     * @param target The new target value for the offset.\n     */',
        '    /**\n     * 无条件更新偏移量。\n     *\n     * @param target 目标偏移量\n     */',
    ),
    (
        '    /**\n     * Freezes the offset at the target value provided. Once frozen, the offset\n     * cannot be updated by subsequent calls to {@link #update(long, boolean)}.\n     * This method will set allowToUpdate to false and then update the offset,\n     * ensuring the new value is the final state of the offset.\n     *\n     * @param target the new target offset value to freeze at.\n     */',
        '    /**\n     * 将偏移量冻结为目标值；冻结后 {@link #update(long, boolean)} 无法再修改。\n     * 先将 allowToUpdate 置为 false 再更新偏移量，确保为最终状态。\n     *\n     * @param target 冻结时的目标偏移量\n     */',
    ),
    (
        '    public long getOffset() {',
        '    /** 获取当前偏移量。 */\n    public long getOffset() {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/store/LocalFileOffsetStore.java'] = [
    (
        '/**\n * Local storage implementation\n */',
        '/**\n * 消费偏移量本地文件存储实现：将偏移量持久化到客户端本地 JSON 文件。\n */',
    ),
    (
        '    public final static String LOCAL_OFFSET_STORE_DIR = System.getProperty(',
        '    /** 本地偏移量存储根目录，可通过系统属性 rocketmq.client.localOffsetStoreDir 配置。 */\n    public final static String LOCAL_OFFSET_STORE_DIR = System.getProperty(',
    ),
    (
        '    private final MQClientInstance mQClientFactory;',
        '    /** MQ 客户端实例。 */\n    private final MQClientInstance mQClientFactory;',
    ),
    (
        '    private final String groupName;',
        '    /** 消费组名称。 */\n    private final String groupName;',
    ),
    (
        '    private final String storePath;',
        '    /** 偏移量 JSON 文件路径。 */\n    private final String storePath;',
    ),
    (
        '    private ConcurrentMap<MessageQueue, ControllableOffset> offsetTable =',
        '    /** 内存中的队列偏移量表。 */\n    private ConcurrentMap<MessageQueue, ControllableOffset> offsetTable =',
    ),
    (
        '    private OffsetSerializeWrapper readLocalOffset() throws MQClientException {',
        '    /** 从本地文件读取偏移量，失败时尝试 .bak 备份文件。 */\n    private OffsetSerializeWrapper readLocalOffset() throws MQClientException {',
    ),
    (
        '    private OffsetSerializeWrapper readLocalOffsetBak() throws MQClientException {',
        '    /** 从 .bak 备份文件读取偏移量。 */\n    private OffsetSerializeWrapper readLocalOffsetBak() throws MQClientException {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/store/OffsetSerializeWrapper.java'] = [
    (
        '/**\n * Wrapper class for offset serialization\n */',
        '/**\n * 消费偏移量序列化包装类，用于 JSON 持久化。\n */',
    ),
    (
        '    private ConcurrentMap<MessageQueue, AtomicLong> offsetTable =',
        '    /** 消息队列到偏移量的映射表。 */\n    private ConcurrentMap<MessageQueue, AtomicLong> offsetTable =',
    ),
    (
        '    public ConcurrentMap<MessageQueue, AtomicLong> getOffsetTable() {',
        '    /** 获取偏移量映射表。 */\n    public ConcurrentMap<MessageQueue, AtomicLong> getOffsetTable() {',
    ),
    (
        '    public void setOffsetTable(ConcurrentMap<MessageQueue, AtomicLong> offsetTable) {',
        '    /** 设置偏移量映射表。 */\n    public void setOffsetTable(ConcurrentMap<MessageQueue, AtomicLong> offsetTable) {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/store/OffsetStore.java'] = [
    (
        '/**\n * Offset store interface\n */',
        '/**\n * 消费偏移量存储接口：定义加载、更新、读取、持久化等操作。\n */',
    ),
    (
        '    /**\n     * Load\n     */',
        '    /** 加载偏移量（从本地或远程）。 */',
    ),
    (
        '    /**\n     * Update the offset,store it in memory\n     */',
        '    /** 更新偏移量并写入内存。 */',
    ),
    (
        '    /**\n     * Update and freeze the message queue to prevent concurrent update action\n     *\n     * @param mq target message queue\n     * @param offset expect update offset\n     */',
        '    /**\n     * 更新并冻结指定队列偏移量，防止并发更新。\n     *\n     * @param mq 目标消息队列\n     * @param offset 期望更新的偏移量\n     */',
    ),
    (
        '    /**\n     * Get offset from local storage\n     *\n     * @return The fetched offset\n     */',
        '    /**\n     * 按指定方式读取偏移量。\n     *\n     * @return 读取到的偏移量，未找到时返回负值\n     */',
    ),
    (
        '    /**\n     * Persist all offsets,may be in local storage or remote name server\n     */',
        '    /** 批量持久化指定队列的偏移量（本地文件或 Broker）。 */',
    ),
    (
        '    /**\n     * Persist the offset,may be in local storage or remote name server\n     */',
        '    /** 持久化单个队列的偏移量。 */',
    ),
    (
        '    /**\n     * Remove offset\n     */',
        '    /** 移除指定队列的偏移量记录。 */',
    ),
    (
        '    /**\n     * @return The cloned offset table of given topic\n     */',
        '    /**\n     * 克隆指定 Topic 的偏移量表副本。\n     *\n     * @return 偏移量映射副本\n     */',
    ),
    (
        '    /**\n     * @param mq\n     * @param offset\n     * @param isOneway\n     */',
        '    /**\n     * 将消费偏移量同步到 Broker。\n     *\n     * @param mq 消息队列\n     * @param offset 偏移量\n     * @param isOneway 是否单向发送（不等待响应）\n     */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/store/ReadOffsetType.java'] = [
    (
        'public enum ReadOffsetType {',
        '/** 读取消费偏移量的数据来源策略。 */\npublic enum ReadOffsetType {',
    ),
    (
        '    /**\n     * From memory\n     */',
        '    /** 仅从内存读取。 */',
    ),
    (
        '    /**\n     * From storage\n     */',
        '    /** 仅从存储（本地文件或 Broker）读取。 */',
    ),
    (
        '    /**\n     * From memory,then from storage\n     */',
        '    /** 先读内存，未命中再读存储。 */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/store/RemoteBrokerOffsetStore.java'] = [
    (
        '/**\n * Remote storage implementation\n */',
        '/**\n * 消费偏移量远程 Broker 存储实现：偏移量持久化在 Broker 端。\n */',
    ),
    (
        '    private final MQClientInstance mQClientFactory;',
        '    /** MQ 客户端实例。 */\n    private final MQClientInstance mQClientFactory;',
    ),
    (
        '    private final String groupName;',
        '    /** 消费组名称。 */\n    private final String groupName;',
    ),
    (
        '    private ConcurrentMap<MessageQueue, ControllableOffset> offsetTable =',
        '    /** 内存中的队列偏移量表。 */\n    private ConcurrentMap<MessageQueue, ControllableOffset> offsetTable =',
    ),
    (
        '                    // No offset in broker',
        '                    // Broker 上无偏移量记录',
    ),
    (
        '                    //Other exceptions',
        '                    // 其他异常',
    ),
    (
        '    /**\n     * Update the Consumer Offset in one way, once the Master is off, updated to Slave, here need to be optimized.\n     */',
        '    /** 单向更新 Broker 消费偏移量（Master 不可用时可能写入 Slave，待优化）。 */',
    ),
    (
        '    /**\n     * Update the Consumer Offset synchronously, once the Master is off, updated to Slave, here need to be optimized.\n     */',
        '    /**\n     * 同步更新 Broker 消费偏移量（Master 不可用时可能写入 Slave，待优化）。\n     *\n     * @param mq 消息队列\n     * @param offset 偏移量\n     * @param isOneway 是否单向发送\n     */',
    ),
    (
        '    private long fetchConsumeOffsetFromBroker(MessageQueue mq) throws RemotingException, MQBrokerException,',
        '    /** 从 Broker 查询消费偏移量。 */\n    private long fetchConsumeOffsetFromBroker(MessageQueue mq) throws RemotingException, MQBrokerException,',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/exception/MQBrokerException.java'] = [
    (
        'public class MQBrokerException extends Exception {',
        '/**\n * Broker 端返回错误时的客户端异常，携带响应码与 Broker 地址等信息。\n */\npublic class MQBrokerException extends Exception {',
    ),
    (
        '    private final int responseCode;',
        '    /** Broker 响应码。 */\n    private final int responseCode;',
    ),
    (
        '    private final String errorMessage;',
        '    /** 错误描述信息。 */\n    private final String errorMessage;',
    ),
    (
        '    private final String brokerAddr;',
        '    /** 发生错误的 Broker 地址。 */\n    private final String brokerAddr;',
    ),
    (
        '    public int getResponseCode() {',
        '    /** 获取响应码。 */\n    public int getResponseCode() {',
    ),
    (
        '    public String getErrorMessage() {',
        '    /** 获取错误描述。 */\n    public String getErrorMessage() {',
    ),
    (
        '    public String getBrokerAddr() {',
        '    /** 获取 Broker 地址。 */\n    public String getBrokerAddr() {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/exception/MQClientException.java'] = [
    (
        'public class MQClientException extends Exception {',
        '/**\n * RocketMQ 客户端通用异常，封装响应码与错误信息。\n */\npublic class MQClientException extends Exception {',
    ),
    (
        '    private int responseCode;',
        '    /** 响应码，-1 表示未设置。 */\n    private int responseCode;',
    ),
    (
        '    private String errorMessage;',
        '    /** 错误描述信息。 */\n    private String errorMessage;',
    ),
    (
        '    public int getResponseCode() {',
        '    /** 获取响应码。 */\n    public int getResponseCode() {',
    ),
    (
        '    public MQClientException setResponseCode(final int responseCode) {',
        '    /** 设置响应码并返回自身（链式调用）。 */\n    public MQClientException setResponseCode(final int responseCode) {',
    ),
    (
        '    public String getErrorMessage() {',
        '    /** 获取错误描述。 */\n    public String getErrorMessage() {',
    ),
    (
        '    public void setErrorMessage(final String errorMessage) {',
        '    /** 设置错误描述。 */\n    public void setErrorMessage(final String errorMessage) {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/exception/OffsetNotFoundException.java'] = [
    (
        'public class OffsetNotFoundException extends MQBrokerException {',
        '/**\n * Broker 上未找到消费偏移量时抛出的异常（如首次消费）。\n */\npublic class OffsetNotFoundException extends MQBrokerException {',
    ),
]
