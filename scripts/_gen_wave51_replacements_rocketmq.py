#!/usr/bin/env python3
"""Generate wave51_replacements_rocketmq.py — IndexStoreService + MQAdminExt batch [0:4]."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "rocketmq/rocketmq-all-5.5.0/original"
OUT = ROOT / "scripts/wave51_replacements_rocketmq.py"
BATCH = Path("/tmp/rmq51_all.txt")

CLASS_JDOC: dict[str, str] = {
    "IndexStoreService": (
        "分层存储索引文件管理服务：维护按时间戳排序的 {@link IndexFile} 表，"
        "负责本地/远程恢复、键写入、异步查询、压缩上传与过期清理。"
    ),
    "MQAdminExt": (
        "RocketMQ 管理扩展接口：在 {@link org.apache.rocketmq.client.MQAdmin} 基础上"
        "提供 Broker/Topic/消费组运维、集群元数据、偏移重置、ACL/用户管理及 Controller 操作等 API。"
    ),
    "DefaultMQAdminExt": (
        "MQ 管理扩展默认实现：继承 {@link ClientConfig}，将 {@link MQAdminExt} 全部方法"
        "委托给 {@link DefaultMQAdminExtImpl}，供 mqadmin 命令行与运维工具使用。"
    ),
    "DefaultMQAdminExtImpl": (
        "MQ 管理扩展核心实现：封装 {@link MQClientInstance} 与 Remoting API，"
        "完成 Topic/订阅组配置、消费统计、消息轨迹、偏移管理、NameServer/Controller 配置及并发批量运维。"
    ),
}

METHOD_CN: dict[str, str] = {
    "start": "启动管理客户端：注册 AdminExt、初始化 MQClient 与并发线程池。",
    "shutdown": "关闭管理客户端并释放 MQClient 与线程池资源。",
    "addBrokerToContainer": "向 Broker 容器动态添加 Broker 实例。",
    "removeBrokerFromContainer": "从 Broker 容器移除指定 Broker。",
    "updateBrokerConfig": "更新 Broker 运行时配置项。",
    "getBrokerConfig": "拉取 Broker 当前配置 Properties。",
    "createAndUpdateTopicConfig": "在指定 Broker 创建或更新 Topic 配置。",
    "createAndUpdateTopicConfigList": "批量创建或更新 Topic 配置列表。",
    "createAndUpdateSubscriptionGroupConfig": "创建或更新消费组订阅配置。",
    "createAndUpdateSubscriptionGroupConfigList": "批量创建或更新消费组订阅配置。",
    "examineSubscriptionGroupConfig": "查询指定 Broker 上某消费组的订阅配置。",
    "examineTopicConfig": "查询指定 Broker 上某 Topic 的配置。",
    "examineTopicStats": "查询 Topic 在各 Broker 上的统计（min/max offset、TPS 等）。",
    "examineTopicStatsConcurrent": "并发查询 Topic 统计并封装为 {@link AdminToolResult}。",
    "fetchAllTopicList": "从 NameServer 拉取全部 Topic 列表。",
    "fetchTopicsByCLuster": "按集群名拉取 Topic 列表。",
    "fetchBrokerRuntimeStats": "拉取 Broker 运行时 KV 指标。",
    "examineConsumeStats": "查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。",
    "checkRocksdbCqWriteProgress": "检查 RocksDB ConsumeQueue 写入进度。",
    "examineConsumeStatsConcurrent": "并发汇总消费组消费统计。",
    "examineBrokerClusterInfo": "从 NameServer 获取集群 Broker 拓扑信息。",
    "examineTopicRouteInfo": "查询 Topic 路由（Broker 与队列分布）。",
    "viewMessage": "按 Topic 与 msgId 查看单条消息。",
    "queryMessage": "按 Topic/Key 或集群条件索引查询消息。",
    "examineConsumerConnectionInfo": "查询消费组在线连接与订阅关系。",
    "examineProducerConnectionInfo": "查询指定 Topic 下生产者连接信息。",
    "getAllProducerInfo": "拉取 Broker 上全部生产者连接表。",
    "getNameServerAddressList": "返回当前配置的 NameServer 地址列表。",
    "wipeWritePermOfBroker": "在 NameServer 上撤销 Broker 写权限。",
    "addWritePermOfBroker": "在 NameServer 上恢复 Broker 写权限。",
    "putKVConfig": "本地写入 KV 配置（不经过 Broker）。",
    "getKVConfig": "从 NameServer 读取 KV 配置值。",
    "getKVListByNamespace": "按命名空间拉取 KV 配置表。",
    "deleteTopic": "从集群 Broker 与 NameServer 删除 Topic。",
    "deleteTopicInBroker": "从指定 Broker 集合删除 Topic。",
    "deleteTopicInBrokerConcurrent": "并发从多个 Broker 删除 Topic。",
    "deleteTopicInNameServer": "从 NameServer 删除 Topic 路由元数据。",
    "deleteSubscriptionGroup": "删除 Broker 上指定消费组配置。",
    "createAndUpdateKvConfig": "在 Broker/NameServer 创建或更新 KV 配置。",
    "deleteKvConfig": "删除 KV 配置项。",
    "resetOffsetByTimestampOld": "按时间戳重置消费位点（旧版 API，返回 RollbackStats）。",
    "resetOffsetByTimestamp": "按时间戳重置消费位点到各队列。",
    "resetOffsetNew": "新版按时间戳重置消费位点。",
    "resetOffsetNewConcurrent": "并发向各 Broker 执行新版位点重置。",
    "getConsumeStatus": "查询消费组各客户端队列消费状态。",
    "createOrUpdateOrderConf": "创建或更新顺序消息全局配置。",
    "queryTopicConsumeByWho": "查询订阅指定 Topic 的消费组列表。",
    "queryTopicsByConsumer": "查询某消费组订阅的全部 Topic。",
    "queryTopicsByConsumerConcurrent": "并发查询消费组订阅 Topic 列表。",
    "querySubscription": "查询消费组对某 Topic 的订阅表达式。",
    "queryConsumeTimeSpan": "查询消费组在各队列上的消费时间跨度。",
    "queryConsumeTimeSpanConcurrent": "并发查询消费时间跨度。",
    "cleanExpiredConsumerQueue": "清理集群内过期消费进度。",
    "cleanExpiredConsumerQueueByAddr": "清理指定 Broker 上过期消费进度。",
    "cleanExpiredConsumerQueueByCluster": "遍历集群全部 Broker 清理过期消费进度。",
    "deleteExpiredCommitLog": "触发集群删除过期 CommitLog。",
    "deleteExpiredCommitLogByAddr": "触发指定 Broker 删除过期 CommitLog。",
    "deleteExpiredCommitLogByCluster": "遍历集群触发 CommitLog 过期删除。",
    "cleanUnusedTopic": "清理集群未使用 Topic。",
    "cleanUnusedTopicByAddr": "清理指定 Broker 未使用 Topic。",
    "cleanUnusedTopicByCluster": "遍历集群清理未使用 Topic。",
    "getConsumerRunningInfo": "拉取消费端运行态（订阅、ProcessQueue、可选 jstack/metrics）。",
    "consumeMessageDirectly": "向指定消费端直接投递并消费一条消息（运维调试）。",
    "messageTrackDetail": "查询消息在各消费组的投递/消费轨迹。",
    "messageTrackDetailConcurrent": "并发查询消息轨迹详情。",
    "consumed": "判断消息是否已被指定消费组消费（同步）。",
    "consumedConcurrent": "并发判断消息消费状态。",
    "cloneGroupOffset": "将源消费组位点克隆到目标消费组。",
    "viewBrokerStatsData": "查看 Broker 指定统计项明细。",
    "getClusterList": "返回 Topic 所在集群名集合。",
    "fetchConsumeStatsInBroker": "拉取 Broker 上全部消费组统计。",
    "getTopicClusterList": "返回 Topic 关联的集群列表。",
    "getAllSubscriptionGroup": "拉取 Broker 全部订阅组配置。",
    "getUserSubscriptionGroup": "拉取 Broker 用户订阅组配置（不含系统组）。",
    "getAllTopicConfig": "拉取 Broker 全部 Topic 配置。",
    "getUserTopicConfig": "拉取 Broker 用户 Topic 配置。",
    "createTopic": "创建 Topic（指定队列数与属性）。",
    "createStaticTopic": "创建静态 Topic 并写入队列映射。",
    "searchOffset": "按时间戳在队列中查找 offset。",
    "maxOffset": "返回队列最大逻辑 offset。",
    "minOffset": "返回队列最小逻辑 offset。",
    "earliestMsgStoreTime": "返回队列最早消息存储时间。",
    "updateConsumeOffset": "更新消费组在指定队列上的 commit offset。",
    "updateNameServerConfig": "更新 NameServer 配置（可指定 NS 列表）。",
    "getNameServerConfig": "拉取 NameServer 配置。",
    "queryConsumeQueue": "分页查询 ConsumeQueue 条目。",
    "exportRocksDBConfigToJson": "导出 Broker RocksDB 配置为 JSON。",
    "resumeCheckHalfMessage": "恢复半消息事务检查（事务消息运维）。",
    "setMessageRequestMode": "设置 Topic 消费组的 POP/Pull 请求模式。",
    "resetOffsetByQueueId": "重置指定队列的消费位点。",
    "getBrokerHAStatus": "查询 Broker 主从复制 HA 状态。",
    "getInSyncStateData": "从 Controller 查询副本 InSync 状态。",
    "getBrokerEpochCache": "拉取 Broker Epoch 缓存（Controller 协议）。",
    "getControllerMetaData": "获取 Controller 集群元数据。",
    "resetMasterFlushOffset": "在 Slave 上重置 Master flush offset。",
    "getControllerConfig": "拉取 Controller 配置。",
    "updateControllerConfig": "更新 Controller 配置。",
    "electMaster": "手动触发 Controller 选举 Broker Master。",
    "cleanControllerBrokerData": "清理 Controller 中 Broker 元数据。",
    "updateColdDataFlowCtrGroupConfig": "更新冷读流控消费组配置。",
    "removeColdDataFlowCtrGroupConfig": "移除冷读流控消费组配置。",
    "getColdDataFlowCtrInfo": "查询冷读流控配置 JSON。",
    "setCommitLogReadAheadMode": "设置 CommitLog 预读模式。",
    "createUser": "在 Broker 创建用户账号。",
    "updateUser": "更新 Broker 用户账号。",
    "deleteUser": "删除 Broker 用户。",
    "getUser": "查询 Broker 用户信息。",
    "listUser": "列出 Broker 用户列表。",
    "createAcl": "在 Broker 创建 ACL 规则。",
    "updateAcl": "更新 Broker ACL 规则。",
    "deleteAcl": "删除 Broker ACL 规则。",
    "getAcl": "查询 Broker ACL 规则。",
    "listAcl": "列出 Broker ACL 规则。",
    "exportPopRecords": "导出 Broker POP 消费记录。",
    "switchTimerEngine": "切换 Broker 定时消息引擎。",
    "getBrokerLiteInfo": "查询 Broker Lite 模式信息。",
    "getParentTopicInfo": "查询 Lite 父 Topic 信息。",
    "getLiteTopicInfo": "查询 Lite 子 Topic 信息。",
    "getLiteClientInfo": "查询 Lite 消费端信息。",
    "getLiteGroupInfo": "查询 Lite 消费组 TopK 信息。",
    "triggerLiteDispatch": "触发 Lite 消费端消息分发。",
    "updateAndGetGroupReadForbidden": "更新并返回消费组 Topic 读禁止状态。",
    "adminToolExecute": "统一执行 {@link AdminToolHandler} 并映射异常为 {@link AdminToolResult}。",
    "searchLowerBoundaryOffset": "按时间戳查找队列下界 offset。",
    "searchUpperBoundaryOffset": "按时间戳查找队列上界 offset。",
    "queryMessageByUniqKey": "按 UniqKey 查询消息。",
    "doExecute": "执行管理工具回调逻辑。",
    "run": "后台线程：过期清理、压缩上传与优雅关闭。",
    "recover": "从本地与远程恢复索引文件表。",
    "createNewIndexFile": "创建新的未封存索引文件。",
    "putKey": "将消息键写入当前索引文件。",
    "queryAsync": "异步按 Topic/Key 与时间范围查询索引项。",
    "forceUpload": "强制压缩并上传全部待处理索引文件。",
    "doCompactThenUploadFile": "压缩单个索引文件并上传至远程存储。",
    "destroyExpiredFile": "删除过期且已上传的索引文件。",
    "destroy": "销毁本地未上传与远程索引资源。",
    "getServiceName": "返回索引服务线程名。",
    "setCompactTimestamp": "更新压缩进度时间戳游标。",
    "getNextSealedFile": "获取下一个待压缩的已封存索引文件。",
    "forceShutdown": "强制关闭索引服务（不等待上传完成）。",
    "doConvertOldFormatFile": "将旧版固定文件名索引转换为时间戳命名。",
    "getTimeStoreTable": "返回时间戳→索引文件映射表（测试用）。",
    "resetOffsetConsumeOffset": "向 Broker 发送单队列位点重置请求。",
}

INDEX_FIELD_PAIRS: list[tuple[str, str]] = [
    (
        "    public static final String FILE_DIRECTORY_NAME = \"tiered_index_file\";",
        "    /** 分层索引文件根目录名。 */\n    public static final String FILE_DIRECTORY_NAME = \"tiered_index_file\";",
    ),
    (
        "    public static final String FILE_COMPACTED_DIRECTORY_NAME = \"compacting\";",
        "    /** 索引压缩过程中的临时目录名。 */\n    public static final String FILE_COMPACTED_DIRECTORY_NAME = \"compacting\";",
    ),
    (
        "     * File status in table example:\n     * upload, upload, upload, sealed, sealed, unsealed\n     */",
        "     * 索引文件状态示例（timeStoreTable 中）：upload, sealed, unsealed。\n     */",
    ),
    (
        "    private final MessageStoreConfig storeConfig;",
        "    /** 分层存储配置。 */\n    private final MessageStoreConfig storeConfig;",
    ),
    (
        "    private final ConcurrentSkipListMap<Long /* timestamp */, IndexFile> timeStoreTable;",
        "    /** 按起始时间戳索引的 {@link IndexFile} 有序表。 */\n    private final ConcurrentSkipListMap<Long /* timestamp */, IndexFile> timeStoreTable;",
    ),
    (
        "    private final ReadWriteLock readWriteLock;",
        "    /** 索引表读写锁，保护查询与压缩/upload 互斥。 */\n    private final ReadWriteLock readWriteLock;",
    ),
    (
        "    private final AtomicLong compactTimestamp;",
        "    /** 已完成压缩/upload 的最大时间戳游标。 */\n    private final AtomicLong compactTimestamp;",
    ),
    (
        "    private final String filePath;",
        "    /** 远程索引 FlatFile 路径标识。 */\n    private final String filePath;",
    ),
    (
        "    private final FlatFileFactory fileAllocator;",
        "    /** 分层 FlatFile 分配器。 */\n    private final FlatFileFactory fileAllocator;",
    ),
    (
        "    private final boolean autoCreateNewFile;",
        "    /** 恢复时若表为空是否自动创建新索引文件。 */\n    private final boolean autoCreateNewFile;",
    ),
    (
        "    private volatile IndexFile currentWriteFile;",
        "    /** 当前写入中的索引文件。 */\n    private volatile IndexFile currentWriteFile;",
    ),
    (
        "    private volatile FlatAppendFile flatAppendFile;",
        "    /** 远程索引追加文件句柄。 */\n    private volatile FlatAppendFile flatAppendFile;",
    ),
]

INDEX_INLINE: list[tuple[str, str]] = [
    ("        // delete compact file directory", "        // 删除压缩临时目录"),
    ("        // recover local", "        // 恢复本地索引文件"),
    ("        // recover remote", "        // 恢复远程已上传索引段"),
    ("                // use current time to ensure the order of file", "                // 用当前时间创建新文件以保证时间序"),
    ("                    // Try to return the query results as much as possible here", "                    // 尽量返回部分查询结果"),
    ("                    // rather than directly throwing exceptions", "                    // 而非直接抛出异常中断"),
    ("                    // The total number of files will not too much, prevent io too fast.", "                    // 文件总数有限，短暂 sleep 避免 IO 过快"),
    ("        // delete file in time store table", "        // 从 timeStoreTable 删除过期条目"),
    ("            // delete local store file", "            // 销毁本地未上传索引文件"),
    ("            // delete remote", "            // 销毁远程 FlatAppendFile"),
    ("        // Wait index service upload then clear time store table", "        // 等待上传完成后清空索引表"),
]

ADMIN_EXT_FIELD_PAIRS: list[tuple[str, str]] = [
    (
        "    private final DefaultMQAdminExtImpl defaultMQAdminExtImpl;",
        "    /** 管理扩展核心实现，封装 Remoting 调用。 */\n    private final DefaultMQAdminExtImpl defaultMQAdminExtImpl;",
    ),
    (
        "    private String adminExtGroup = \"admin_ext_group\";",
        "    /** AdminExt 在 MQClient 中注册的分组名。 */\n    private String adminExtGroup = \"admin_ext_group\";",
    ),
    (
        "    private String createTopicKey = TopicValidator.AUTO_CREATE_TOPIC_KEY_TOPIC;",
        "    /** 自动创建 Topic 时使用的系统 Key。 */\n    private String createTopicKey = TopicValidator.AUTO_CREATE_TOPIC_KEY_TOPIC;",
    ),
    (
        "    private long timeoutMillis = 5000;",
        "    /** 默认 Remoting 请求超时（毫秒）。 */\n    private long timeoutMillis = 5000;",
    ),
]

IMPL_FIELD_PAIRS: list[tuple[str, str]] = [
    (
        "    private static final String SOCKS_PROXY_JSON = \"socksProxyJson\";",
        "    /** 环境变量名：SOCKS 代理 JSON 配置。 */\n    private static final String SOCKS_PROXY_JSON = \"socksProxyJson\";",
    ),
    (
        "    private final Logger logger = LoggerFactory.getLogger(DefaultMQAdminExtImpl.class);",
        "    /** 管理扩展实现日志。 */\n    private final Logger logger = LoggerFactory.getLogger(DefaultMQAdminExtImpl.class);",
    ),
    (
        "    private final DefaultMQAdminExt defaultMQAdminExt;",
        "    /** 关联的 {@link DefaultMQAdminExt} 配置与分组信息。 */\n    private final DefaultMQAdminExt defaultMQAdminExt;",
    ),
    (
        "    private ServiceState serviceState = ServiceState.CREATE_JUST;",
        "    /** AdminExt 生命周期状态。 */\n    private ServiceState serviceState = ServiceState.CREATE_JUST;",
    ),
    (
        "    private MQClientInstance mqClientInstance;",
        "    /** 底层 MQ 客户端实例，负责 Remoting 通信。 */\n    private MQClientInstance mqClientInstance;",
    ),
    (
        "    private RPCHook rpcHook;",
        "    /** 可选 RPC 钩子（鉴权等）。 */\n    private RPCHook rpcHook;",
    ),
    (
        "    private long timeoutMillis = 20000;",
        "    /** Remoting 默认超时（毫秒）。 */\n    private long timeoutMillis = 20000;",
    ),
    (
        "    private Random random = new Random();",
        "    /** 随机数，用于负载均衡选 Broker 等。 */\n    private Random random = new Random();",
    ),
    (
        "    protected final List<String> kvNamespaceToDeleteList = Arrays.asList(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG);",
        "    /** shutdown 时需清理的 KV 命名空间列表。 */\n    protected final List<String> kvNamespaceToDeleteList = Arrays.asList(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG);",
    ),
    (
        "    protected ThreadPoolExecutor threadPoolExecutor;",
        "    /** 并发管理操作线程池（Topic 统计、位点重置等）。 */\n    protected ThreadPoolExecutor threadPoolExecutor;",
    ),
]

IMPL_INLINE: list[tuple[str, str]] = [
    ("        //Get the static stats", "        // 合并静态 Topic 逻辑队列统计"),
    ("        // Use clusterName topic to get topic route for lmq or rmq_sys_wheel_timer", "        // LMQ/定时 Topic 用 clusterName 作为路由键查询"),
    ("            // for topic, we put the physical stats, how about group?", "            // Topic 维度保留物理统计；消费组维度另行转换"),
    ("            // staticResult.getOffsetTable().putAll(result.getOffsetTable());", "            // 静态 Topic 需单独转换逻辑 offset 表"),
    ("                // broadcast", "                // 广播模式无法精确追踪，标记 CONSUMED_BUT_FILTERED"),
    ("                // offline", "                // 消费端离线"),
    ("                // pull", "                // Pull 模式处理中"),
    ("                // not consume", "                // 尚未消费"),
    ("                // consumed", "                // 已正常消费"),
    ("                // filter", "                // 已投递但被过滤"),
    ("        // name servers", "        // 遍历 NameServer 地址"),
    ("        // delete kv config", "        // shutdown 时清理顺序 Topic KV 配置"),
]

EN_JDOC_TRANSLATIONS: list[tuple[str, str]] = [
    (
        "    /**\n     * Update name server config.\n     * <br>\n     * Command Code : RequestCode.UPDATE_NAMESRV_CONFIG\n     *\n     * <br> If param(nameServers) is null or empty, will use name servers from ns!\n     */",
        "    /**\n     * 更新 NameServer 配置。\n     * <br>\n     * 命令码：RequestCode.UPDATE_NAMESRV_CONFIG\n     *\n     * <br> nameServers 为空时使用客户端已配置的 NameServer 列表。\n     */",
    ),
    (
        "    /**\n     * Get name server config.\n     * <br>\n     * Command Code : RequestCode.GET_NAMESRV_CONFIG\n     * <br> If param(nameServers) is null or empty, will use name servers from ns!\n     *\n     * @return The fetched name server config\n     */",
        "    /**\n     * 获取 NameServer 配置。\n     * <br>\n     * 命令码：RequestCode.GET_NAMESRV_CONFIG\n     * <br> nameServers 为空时使用客户端已配置的 NameServer 列表。\n     *\n     * @return NameServer 配置映射\n     */",
    ),
    (
        "    /**\n     * query consume queue data\n     *\n     * @param brokerAddr    broker ip address\n     * @param topic         topic\n     * @param queueId       id of queue\n     * @param index         start offset\n     * @param count         how many\n     * @param consumerGroup group\n     */",
        "    /**\n     * 分页查询 ConsumeQueue 数据。\n     *\n     * @param brokerAddr Broker 地址\n     * @param topic Topic 名称\n     * @param queueId 队列 ID\n     * @param index 起始逻辑 offset\n     * @param count 拉取条数\n     * @param consumerGroup 消费组（可选过滤）\n     */",
    ),
    (
        "    /**\n     * Reset master flush offset in slave\n     *\n     * @param brokerAddr        slave broker address\n     * @param masterFlushOffset master flush offset\n     */",
        "    /**\n     * 在 Slave Broker 上重置 Master flush offset。\n     *\n     * @param brokerAddr Slave Broker 地址\n     * @param masterFlushOffset Master flush offset\n     */",
    ),
    (
        "    /**\n     * Get controller config.\n     * <br>\n     * Command Code : RequestCode.GET_CONTROLLER_CONFIG\n     *\n     * @return The fetched controller config\n     */",
        "    /**\n     * 获取 Controller 配置。\n     * <br>\n     * 命令码：RequestCode.GET_CONTROLLER_CONFIG\n     *\n     * @return Controller 配置映射\n     */",
    ),
    (
        "    /**\n     * Update controller config.\n     * <br>\n     * Command Code : RequestCode.UPDATE_CONTROLLER_CONFIG\n     */",
        "    /**\n     * 更新 Controller 配置。\n     * <br>\n     * 命令码：RequestCode.UPDATE_CONTROLLER_CONFIG\n     */",
    ),
    (
        "    /**\n     * manual trigger broker elect master\n     *\n     * @param controllerAddr controller address\n     * @param clusterName    cluster name\n     * @param brokerName     broker name\n     * @param brokerId     broker id\n     * @return\n     * @throws RemotingException\n     * @throws InterruptedException\n     * @throws MQBrokerException\n     */",
        "    /**\n     * 手动触发 Controller 选举 Broker Master。\n     *\n     * @param controllerAddr Controller 地址\n     * @param clusterName 集群名\n     * @param brokerName Broker 名\n     * @param brokerId Broker ID\n     * @return 选举响应与 Broker 成员组\n     */",
    ),
    (
        "    /**\n     * clean controller broker meta data\n     */",
        "    /** 清理 Controller 中指定 Broker 元数据。 */",
    ),
]


def apply_pairs(text: str, pairs: list[tuple[str, str]]) -> str:
    for old, new in pairs:
        if old in text:
            text = text.replace(old, new, 1)
    return text


def add_class_javadoc(text: str, class_name: str) -> str:
    desc = CLASS_JDOC.get(class_name)
    if not desc:
        return text
    marker = f"public class {class_name}"
    iface = f"public interface {class_name}"
    if marker in text:
        return text.replace(marker, f"/**\n * {desc}\n */\n{marker}", 1)
    if iface in text:
        return text.replace(iface, f"/**\n * {desc}\n */\n{iface}", 1)
    return text


def method_desc(name: str, class_name: str) -> str:
    if name in METHOD_CN:
        return METHOD_CN[name]
    if class_name == "DefaultMQAdminExt":
        return f"委托 {{@link DefaultMQAdminExtImpl}}#{name}。"
    return f"MQ 管理操作：{name}。"


def has_javadoc_before(text: str, pos: int) -> bool:
    window = text[max(0, pos - 600) : pos]
    return bool(re.search(r"/\*\*[\s\S]*?\*/\s*$", window))


def add_method_javadocs(text: str, class_name: str, with_body: bool = True) -> str:
    if with_body:
        pattern = re.compile(
            r"(?P<indent>^[ \t]*)"
            r"(?:(?:@\w+(?:\([^)]*\))?\s*\n\s*)*)"
            r"(?P<sig>(?:public|protected|private)\s+[\w<>,\?\[\]\s]+\s+(?P<name>\w+)\s*\([^;]*\)\s*(?:throws[^{]+)?\{)",
            re.MULTILINE,
        )
    else:
        pattern = re.compile(
            r"(?P<indent>^[ \t]*)"
            r"(?P<sig>(?:void|[\w<>,\?\[\]\s]+)\s+(?P<name>\w+)\s*\([^)]*\)\s*(?:throws[^;]*)?;)\s*$",
            re.MULTILINE,
        )

    def repl(m: re.Match[str]) -> str:
        if has_javadoc_before(text, m.start()):
            return m.group(0)
        name = m.group("name")
        desc = method_desc(name, class_name)
        indent = m.group("indent")
        prefix = m.group(0)
        if prefix.lstrip().startswith("@"):
            return m.group(0)
        if with_body:
            return f"{indent}/** {desc} */\n{prefix}"
        return f"{indent}/** {desc} */\n{indent}{m.group('sig')}"

    return pattern.sub(repl, text)


def add_override_javadocs(text: str, class_name: str) -> str:
    pattern = re.compile(
        r"(?P<indent>^[ \t]*)(@Override\s*\n\s*)"
        r"(?P<sig>(?:public|protected)\s+[\w<>,\?\[\]\s]+\s+(?P<name>\w+)\s*\([^;]*\)\s*(?:throws[^{]+)?\{)",
        re.MULTILINE,
    )

    def repl(m: re.Match[str]) -> str:
        if has_javadoc_before(text, m.start()):
            return m.group(0)
        name = m.group("name")
        desc = method_desc(name, class_name)
        indent = m.group("indent")
        return f"{indent}/** {desc} */\n{indent}{m.group(2)}{m.group('sig')}"

    return pattern.sub(repl, text)


def annotate_text(rel: str, text: str) -> str:
    class_name = Path(rel).stem
    text = add_class_javadoc(text, class_name)

    if class_name == "IndexStoreService":
        text = apply_pairs(text, INDEX_FIELD_PAIRS)
        text = apply_pairs(text, INDEX_INLINE)
        text = add_method_javadocs(text, class_name, with_body=True)
    elif class_name == "MQAdminExt":
        text = apply_pairs(text, EN_JDOC_TRANSLATIONS)
        text = add_method_javadocs(text, class_name, with_body=False)
    elif class_name == "DefaultMQAdminExt":
        text = apply_pairs(text, ADMIN_EXT_FIELD_PAIRS)
        text = add_override_javadocs(text, class_name)
        text = add_method_javadocs(text, class_name, with_body=True)
    elif class_name == "DefaultMQAdminExtImpl":
        text = apply_pairs(text, IMPL_FIELD_PAIRS)
        text = apply_pairs(text, IMPL_INLINE)
        text = apply_pairs(text, EN_JDOC_TRANSLATIONS)
        text = add_override_javadocs(text, class_name)
        text = add_method_javadocs(text, class_name, with_body=True)

    return text


def build_replacements() -> dict[str, list[tuple[str, str]]]:
    reps: dict[str, list[tuple[str, str]]] = {}
    for rel in BATCH.read_text(encoding="utf-8").splitlines():
        rel = rel.strip()
        if not rel:
            continue
        orig = (ORIG / rel).read_text(encoding="utf-8")
        annotated = annotate_text(rel, orig)
        if orig == annotated:
            raise ValueError(f"no changes for {rel}")
        cn = len(re.findall(r"[\u4e00-\u9fff]", annotated))
        if cn < 10:
            raise ValueError(f"insufficient CJK cn={cn} for {rel}")
        reps[rel] = [(orig, annotated)]
        print(f"OK {rel} cjk={cn}")
    return reps


def write_replacements_file(reps: dict[str, list[tuple[str, str]]]) -> None:
    lines = [
        '"""Chinese annotation replacements for RocketMQ 5.5.0 wave51 [0:4]."""',
        "from __future__ import annotations",
        "",
        "R: dict[str, list[tuple[str, str]]] = {",
    ]
    for rel in BATCH.read_text(encoding="utf-8").splitlines():
        rel = rel.strip()
        if not rel:
            continue
        old, new = reps[rel][0]
        lines.append(f"    {rel!r}: [")
        lines.append(f"        ({old!r},")
        lines.append(f"         {new!r}),")
        lines.append("    ],")
    lines.append("}")
    lines.append("")
    OUT.write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    reps = build_replacements()
    write_replacements_file(reps)
    print(f"Wrote {OUT} ({len(reps)} entries)")


if __name__ == "__main__":
    main()
