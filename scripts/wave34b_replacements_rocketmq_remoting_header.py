"""Chinese JavaDoc replacements for RocketMQ wave34b remoting protocol header [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ViewBrokerStatsDataRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.VIEW_BROKER_STATS_DATA, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class ViewBrokerStatsDataRequestHeader implements CommandCustomHeader {",
            "/**\n * 查看 Broker 统计数据的请求头：按 statsName 与 statsKey 查询集群级监控指标。\n */\n@RocketMQAction(value = RequestCode.VIEW_BROKER_STATS_DATA, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class ViewBrokerStatsDataRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String statsName;",
            "    /** 统计项名称。 */\n    @CFNotNull\n    private String statsName;",
        ),
        (
            "    @CFNotNull\n    private String statsKey;",
            "    /** 统计项键值（维度标识）。 */\n    @CFNotNull\n    private String statsKey;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getStatsName() {",
            "    /** 返回统计项名称。 */\n    public String getStatsName() {",
        ),
        (
            "    public void setStatsName(String statsName) {",
            "    /** 设置统计项名称。 */\n    public void setStatsName(String statsName) {",
        ),
        (
            "    public String getStatsKey() {",
            "    /** 返回统计项键值。 */\n    public String getStatsKey() {",
        ),
        (
            "    public void setStatsKey(String statsKey) {",
            "    /** 设置统计项键值。 */\n    public void setStatsKey(String statsKey) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ViewMessageRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.VIEW_MESSAGE_BY_ID, action = Action.GET)\npublic class ViewMessageRequestHeader implements CommandCustomHeader {",
            "/**\n * 按消息 ID 查看消息内容的请求头：指定 Topic 与物理 offset 定位单条消息。\n */\n@RocketMQAction(value = RequestCode.VIEW_MESSAGE_BY_ID, action = Action.GET)\npublic class ViewMessageRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private Long offset;",
            "    /** 消息在 CommitLog 中的物理 offset。 */\n    @CFNotNull\n    private Long offset;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    public void setTopic(String topic) {",
        ),
        (
            "    public Long getOffset() {",
            "    /** 返回物理 offset。 */\n    public Long getOffset() {",
        ),
        (
            "    public void setOffset(Long offset) {",
            "    /** 设置物理 offset。 */\n    public void setOffset(Long offset) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ViewMessageResponseHeader.java": [
        (
            "public class ViewMessageResponseHeader implements CommandCustomHeader {",
            "/**\n * 查看消息内容的响应头：消息体由 Remoting 响应体承载，本头无额外字段。\n */\npublic class ViewMessageResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/AlterSyncStateSetRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CONTROLLER_ALTER_SYNC_STATE_SET, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class AlterSyncStateSetRequestHeader implements CommandCustomHeader {",
            "/**\n * Controller 修改同步副本集（SyncStateSet）的请求头：携带 Broker 组名、Master brokerId 与 epoch。\n */\n@RocketMQAction(value = RequestCode.CONTROLLER_ALTER_SYNC_STATE_SET, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class AlterSyncStateSetRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String brokerName;",
            "    /** 目标 Broker 组名称。 */\n    private String brokerName;",
        ),
        (
            "    private Long masterBrokerId;",
            "    /** 当前 Master 的 brokerId。 */\n    private Long masterBrokerId;",
        ),
        (
            "    private Integer masterEpoch;",
            "    /** Master 的 epoch 版本号。 */\n    private Integer masterEpoch;",
        ),
        (
            "    private long invokeTime = System.currentTimeMillis();",
            "    /** 请求发起时间戳（毫秒）。 */\n    private long invokeTime = System.currentTimeMillis();",
        ),
        (
            "    public AlterSyncStateSetRequestHeader() {",
            "    /** 默认构造。 */\n    public AlterSyncStateSetRequestHeader() {",
        ),
        (
            "    public AlterSyncStateSetRequestHeader(String brokerName, Long masterBrokerId, Integer masterEpoch) {",
            "    /** 指定 Broker 组、Master brokerId 与 epoch 的构造。 */\n    public AlterSyncStateSetRequestHeader(String brokerName, Long masterBrokerId, Integer masterEpoch) {",
        ),
        (
            "    public long getInvokeTime() {",
            "    /** 返回请求发起时间戳。 */\n    public long getInvokeTime() {",
        ),
        (
            "    public void setInvokeTime(long invokeTime) {",
            "    /** 设置请求发起时间戳。 */\n    public void setInvokeTime(long invokeTime) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
        (
            "    public Long getMasterBrokerId() {",
            "    /** 返回 Master brokerId。 */\n    public Long getMasterBrokerId() {",
        ),
        (
            "    public void setMasterBrokerId(Long masterBrokerId) {",
            "    /** 设置 Master brokerId。 */\n    public void setMasterBrokerId(Long masterBrokerId) {",
        ),
        (
            "    public Integer getMasterEpoch() {",
            "    /** 返回 Master epoch。 */\n    public Integer getMasterEpoch() {",
        ),
        (
            "    public void setMasterEpoch(Integer masterEpoch) {",
            "    /** 设置 Master epoch。 */\n    public void setMasterEpoch(Integer masterEpoch) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Broker 组、Master brokerId 与 epoch 的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/AlterSyncStateSetResponseHeader.java": [
        (
            "public class AlterSyncStateSetResponseHeader implements CommandCustomHeader {",
            "/**\n * Controller 修改 SyncStateSet 的响应头：返回更新后的 syncStateSet epoch。\n */\npublic class AlterSyncStateSetResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    private int newSyncStateSetEpoch;",
            "    /** 变更后的 SyncStateSet epoch 版本号。 */\n    private int newSyncStateSetEpoch;",
        ),
        (
            "    public AlterSyncStateSetResponseHeader() {",
            "    /** 默认构造。 */\n    public AlterSyncStateSetResponseHeader() {",
        ),
        (
            "    public int getNewSyncStateSetEpoch() {",
            "    /** 返回新的 SyncStateSet epoch。 */\n    public int getNewSyncStateSetEpoch() {",
        ),
        (
            "    public void setNewSyncStateSetEpoch(int newSyncStateSetEpoch) {",
            "    /** 设置新的 SyncStateSet epoch。 */\n    public void setNewSyncStateSetEpoch(int newSyncStateSetEpoch) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含新 epoch 的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/ElectMasterRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CONTROLLER_ELECT_MASTER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class ElectMasterRequestHeader implements CommandCustomHeader {",
            "/**\n * Controller 选举 Master 的请求头：支持 Broker 触发、Controller 触发与管理员指定选举。\n */\n@RocketMQAction(value = RequestCode.CONTROLLER_ELECT_MASTER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class ElectMasterRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName = \"\";",
            "    /** 目标集群名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName = \"\";",
        ),
        (
            "    @CFNotNull\n    private String brokerName = \"\";",
            "    /** 目标 Broker 组名称。 */\n    @CFNotNull\n    private String brokerName = \"\";",
        ),
        (
            "    /**\n     * brokerId\n     * for brokerTrigger electMaster: this brokerId will be elected as a master when it is the first time to elect\n     * in this broker-set\n     * for adminTrigger electMaster: this brokerId is also named assignedBrokerId, which means we must prefer to elect\n     * it as a new master when this broker is valid.\n     */",
            "    /**\n     * 候选 brokerId。\n     * Broker 触发选主：Broker 组首次选举时，该 brokerId 将被选为 Master。\n     * 管理员触发选主：即 assignedBrokerId，Broker 有效时优先将其选为新 Master。\n     */",
        ),
        (
            "    @CFNotNull\n    private Boolean designateElect = false;",
            "    /** 是否为指定选举（管理员触发时为 true）。 */\n    @CFNotNull\n    private Boolean designateElect = false;",
        ),
        (
            "    private Long invokeTime = System.currentTimeMillis();",
            "    /** 请求发起时间戳（毫秒）。 */\n    private Long invokeTime = System.currentTimeMillis();",
        ),
        (
            "    public ElectMasterRequestHeader() {",
            "    /** 默认构造。 */\n    public ElectMasterRequestHeader() {",
        ),
        (
            "    public ElectMasterRequestHeader(String brokerName) {",
            "    /** 仅指定 Broker 组名的构造（Controller 触发）。 */\n    public ElectMasterRequestHeader(String brokerName) {",
        ),
        (
            "    public ElectMasterRequestHeader(String clusterName, String brokerName, Long brokerId) {",
            "    /** 指定集群、Broker 组与 brokerId 的构造。 */\n    public ElectMasterRequestHeader(String clusterName, String brokerName, Long brokerId) {",
        ),
        (
            "    public ElectMasterRequestHeader(String clusterName, String brokerName, Long brokerId, boolean designateElect) {",
            "    /** 指定集群、Broker 组、brokerId 及是否指定选举的构造。 */\n    public ElectMasterRequestHeader(String clusterName, String brokerName, Long brokerId, boolean designateElect) {",
        ),
        (
            "    public static ElectMasterRequestHeader ofBrokerTrigger(String clusterName, String brokerName,\n        Long brokerId) {",
            "    /** 创建 Broker 触发的选主请求头。 */\n    public static ElectMasterRequestHeader ofBrokerTrigger(String clusterName, String brokerName,\n        Long brokerId) {",
        ),
        (
            "    public static ElectMasterRequestHeader ofControllerTrigger(String brokerName) {",
            "    /** 创建 Controller 触发的选主请求头。 */\n    public static ElectMasterRequestHeader ofControllerTrigger(String brokerName) {",
        ),
        (
            "    public static ElectMasterRequestHeader ofAdminTrigger(String clusterName, String brokerName, Long brokerId) {",
            "    /** 创建管理员指定选举的请求头。 */\n    public static ElectMasterRequestHeader ofAdminTrigger(String clusterName, String brokerName, Long brokerId) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
        (
            "    public Long getBrokerId() {",
            "    /** 返回候选 brokerId。 */\n    public Long getBrokerId() {",
        ),
        (
            "    public void setBrokerId(Long brokerId) {",
            "    /** 设置候选 brokerId。 */\n    public void setBrokerId(Long brokerId) {",
        ),
        (
            "    public String getClusterName() {",
            "    /** 返回集群名称。 */\n    public String getClusterName() {",
        ),
        (
            "    public void setClusterName(String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(String clusterName) {",
        ),
        (
            "    public boolean getDesignateElect() {",
            "    /** 返回是否为指定选举。 */\n    public boolean getDesignateElect() {",
        ),
        (
            "    public Long getInvokeTime() {",
            "    /** 返回请求发起时间戳。 */\n    public Long getInvokeTime() {",
        ),
        (
            "    public void setInvokeTime(Long invokeTime) {",
            "    /** 设置请求发起时间戳。 */\n    public void setInvokeTime(Long invokeTime) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含集群、Broker 组、brokerId 与指定选举标志的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/ElectMasterResponseHeader.java": [
        (
            "public class ElectMasterResponseHeader implements CommandCustomHeader {",
            "/**\n * Controller 选举 Master 的响应头：返回新 Master 的 brokerId、地址、epoch 及 SyncStateSet epoch。\n */\npublic class ElectMasterResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    private Long masterBrokerId;",
            "    /** 新 Master 的 brokerId。 */\n    private Long masterBrokerId;",
        ),
        (
            "    private String masterAddress;",
            "    /** 新 Master 的网络地址。 */\n    private String masterAddress;",
        ),
        (
            "    private Integer masterEpoch;",
            "    /** 新 Master 的 epoch 版本号。 */\n    private Integer masterEpoch;",
        ),
        (
            "    private Integer syncStateSetEpoch;",
            "    /** 当前 SyncStateSet 的 epoch 版本号。 */\n    private Integer syncStateSetEpoch;",
        ),
        (
            "    public ElectMasterResponseHeader() {",
            "    /** 默认构造。 */\n    public ElectMasterResponseHeader() {",
        ),
        (
            "    public ElectMasterResponseHeader(Long masterBrokerId, String masterAddress, Integer masterEpoch, Integer syncStateSetEpoch) {",
            "    /** 指定 Master brokerId、地址、epoch 与 SyncStateSet epoch 的构造。 */\n    public ElectMasterResponseHeader(Long masterBrokerId, String masterAddress, Integer masterEpoch, Integer syncStateSetEpoch) {",
        ),
        (
            "    public String getMasterAddress() {",
            "    /** 返回 Master 地址。 */\n    public String getMasterAddress() {",
        ),
        (
            "    public void setMasterAddress(String masterAddress) {",
            "    /** 设置 Master 地址。 */\n    public void setMasterAddress(String masterAddress) {",
        ),
        (
            "    public Integer getMasterEpoch() {",
            "    /** 返回 Master epoch。 */\n    public Integer getMasterEpoch() {",
        ),
        (
            "    public void setMasterEpoch(Integer masterEpoch) {",
            "    /** 设置 Master epoch。 */\n    public void setMasterEpoch(Integer masterEpoch) {",
        ),
        (
            "    public Integer getSyncStateSetEpoch() {",
            "    /** 返回 SyncStateSet epoch。 */\n    public Integer getSyncStateSetEpoch() {",
        ),
        (
            "    public void setSyncStateSetEpoch(Integer syncStateSetEpoch) {",
            "    /** 设置 SyncStateSet epoch。 */\n    public void setSyncStateSetEpoch(Integer syncStateSetEpoch) {",
        ),
        (
            "    public void setMasterBrokerId(Long masterBrokerId) {",
            "    /** 设置 Master brokerId。 */\n    public void setMasterBrokerId(Long masterBrokerId) {",
        ),
        (
            "    public Long getMasterBrokerId() {",
            "    /** 返回 Master brokerId。 */\n    public Long getMasterBrokerId() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Master 信息与 epoch 的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/GetMetaDataResponseHeader.java": [
        (
            "public class GetMetaDataResponseHeader implements CommandCustomHeader {",
            "/**\n * Controller 元数据查询响应头：返回 Controller 组、Leader 信息、本节点是否为 Leader 及 peers 列表。\n */\npublic class GetMetaDataResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    private String group;",
            "    /** Controller 组名称。 */\n    private String group;",
        ),
        (
            "    private String controllerLeaderId;",
            "    /** 当前 Controller Leader 的节点 ID。 */\n    private String controllerLeaderId;",
        ),
        (
            "    private String controllerLeaderAddress;",
            "    /** 当前 Controller Leader 的网络地址。 */\n    private String controllerLeaderAddress;",
        ),
        (
            "    private boolean isLeader;",
            "    /** 本节点是否为 Controller Leader。 */\n    private boolean isLeader;",
        ),
        (
            "    private String peers;",
            "    /** Controller 集群 peers 信息（序列化字符串）。 */\n    private String peers;",
        ),
        (
            "    public GetMetaDataResponseHeader() {",
            "    /** 默认构造。 */\n    public GetMetaDataResponseHeader() {",
        ),
        (
            "    public GetMetaDataResponseHeader(String group, String controllerLeaderId, String controllerLeaderAddress, boolean isLeader, String peers) {",
            "    /** 指定组、Leader 信息、本节点角色与 peers 的构造。 */\n    public GetMetaDataResponseHeader(String group, String controllerLeaderId, String controllerLeaderAddress, boolean isLeader, String peers) {",
        ),
        (
            "    public String getGroup() {",
            "    /** 返回 Controller 组名称。 */\n    public String getGroup() {",
        ),
        (
            "    public void setGroup(String group) {",
            "    /** 设置 Controller 组名称。 */\n    public void setGroup(String group) {",
        ),
        (
            "    public String getControllerLeaderId() {",
            "    /** 返回 Controller Leader 节点 ID。 */\n    public String getControllerLeaderId() {",
        ),
        (
            "    public void setControllerLeaderId(String controllerLeaderId) {",
            "    /** 设置 Controller Leader 节点 ID。 */\n    public void setControllerLeaderId(String controllerLeaderId) {",
        ),
        (
            "    public String getControllerLeaderAddress() {",
            "    /** 返回 Controller Leader 地址。 */\n    public String getControllerLeaderAddress() {",
        ),
        (
            "    public void setControllerLeaderAddress(String controllerLeaderAddress) {",
            "    /** 设置 Controller Leader 地址。 */\n    public void setControllerLeaderAddress(String controllerLeaderAddress) {",
        ),
        (
            "    public boolean isLeader() {",
            "    /** 返回本节点是否为 Leader。 */\n    public boolean isLeader() {",
        ),
        (
            "    public void setIsLeader(boolean leader) {",
            "    /** 设置本节点是否为 Leader。 */\n    public void setIsLeader(boolean leader) {",
        ),
        (
            "    public String getPeers() {",
            "    /** 返回 peers 信息字符串。 */\n    public String getPeers() {",
        ),
        (
            "    public void setPeers(String peers) {",
            "    /** 设置 peers 信息字符串。 */\n    public void setPeers(String peers) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含组、Leader 与 peers 的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/GetReplicaInfoRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CONTROLLER_GET_REPLICA_INFO, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetReplicaInfoRequestHeader implements CommandCustomHeader {",
            "/**\n * Controller 查询副本信息的请求头：按 Broker 组名获取 Master 与副本状态。\n */\n@RocketMQAction(value = RequestCode.CONTROLLER_GET_REPLICA_INFO, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetReplicaInfoRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String brokerName;",
            "    /** 目标 Broker 组名称。 */\n    private String brokerName;",
        ),
        (
            "    public GetReplicaInfoRequestHeader() {",
            "    /** 默认构造。 */\n    public GetReplicaInfoRequestHeader() {",
        ),
        (
            "    public GetReplicaInfoRequestHeader(String brokerName) {",
            "    /** 指定 Broker 组名的构造。 */\n    public GetReplicaInfoRequestHeader(String brokerName) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Broker 组名的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/GetReplicaInfoResponseHeader.java": [
        (
            "public class GetReplicaInfoResponseHeader implements CommandCustomHeader {",
            "/**\n * Controller 查询副本信息的响应头：返回 Master brokerId、地址与 epoch。\n */\npublic class GetReplicaInfoResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    private Long masterBrokerId;",
            "    /** Master 的 brokerId。 */\n    private Long masterBrokerId;",
        ),
        (
            "    private String masterAddress;",
            "    /** Master 的网络地址。 */\n    private String masterAddress;",
        ),
        (
            "    private Integer masterEpoch;",
            "    /** Master 的 epoch 版本号。 */\n    private Integer masterEpoch;",
        ),
        (
            "    public GetReplicaInfoResponseHeader() {",
            "    /** 默认构造。 */\n    public GetReplicaInfoResponseHeader() {",
        ),
        (
            "    public String getMasterAddress() {",
            "    /** 返回 Master 地址。 */\n    public String getMasterAddress() {",
        ),
        (
            "    public void setMasterAddress(String masterAddress) {",
            "    /** 设置 Master 地址。 */\n    public void setMasterAddress(String masterAddress) {",
        ),
        (
            "    public Integer getMasterEpoch() {",
            "    /** 返回 Master epoch。 */\n    public Integer getMasterEpoch() {",
        ),
        (
            "    public void setMasterEpoch(Integer masterEpoch) {",
            "    /** 设置 Master epoch。 */\n    public void setMasterEpoch(Integer masterEpoch) {",
        ),
        (
            "    public Long getMasterBrokerId() {",
            "    /** 返回 Master brokerId。 */\n    public Long getMasterBrokerId() {",
        ),
        (
            "    public void setMasterBrokerId(Long masterBrokerId) {",
            "    /** 设置 Master brokerId。 */\n    public void setMasterBrokerId(Long masterBrokerId) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Master 信息的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/admin/CleanControllerBrokerDataRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CLEAN_BROKER_DATA, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class CleanControllerBrokerDataRequestHeader implements CommandCustomHeader {",
            "/**\n * 清理 Controller 侧 Broker 元数据的请求头：可指定待清理的 brokerId 集合及是否清理存活 Broker。\n */\n@RocketMQAction(value = RequestCode.CLEAN_BROKER_DATA, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class CleanControllerBrokerDataRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNullable\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
            "    /** 目标集群名称，可为空。 */\n    @CFNullable\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
        ),
        (
            "    @CFNotNull\n    private String brokerName;",
            "    /** 目标 Broker 组名称。 */\n    @CFNotNull\n    private String brokerName;",
        ),
        (
            "    @CFNullable\n    private String brokerControllerIdsToClean;",
            "    /** 待清理的 Controller brokerId 集合（字符串形式），可为空。 */\n    @CFNullable\n    private String brokerControllerIdsToClean;",
        ),
        (
            "    private boolean isCleanLivingBroker = false;",
            "    /** 是否同时清理仍存活的 Broker 数据。 */\n    private boolean isCleanLivingBroker = false;",
        ),
        (
            "    private long invokeTime = System.currentTimeMillis();",
            "    /** 请求发起时间戳（毫秒）。 */\n    private long invokeTime = System.currentTimeMillis();",
        ),
        (
            "    public CleanControllerBrokerDataRequestHeader() {",
            "    /** 默认构造。 */\n    public CleanControllerBrokerDataRequestHeader() {",
        ),
        (
            "    public CleanControllerBrokerDataRequestHeader(String clusterName, String brokerName, String brokerIdSetToClean,\n        boolean isCleanLivingBroker) {",
            "    /** 指定集群、Broker 组、待清理 brokerId 集合及存活清理标志的构造。 */\n    public CleanControllerBrokerDataRequestHeader(String clusterName, String brokerName, String brokerIdSetToClean,\n        boolean isCleanLivingBroker) {",
        ),
        (
            "    public CleanControllerBrokerDataRequestHeader(String clusterName, String brokerName, String brokerIdSetToClean) {",
            "    /** 指定集群、Broker 组与待清理 brokerId 集合的构造（默认不清理存活 Broker）。 */\n    public CleanControllerBrokerDataRequestHeader(String clusterName, String brokerName, String brokerIdSetToClean) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public long getInvokeTime() {",
            "    /** 返回请求发起时间戳。 */\n    public long getInvokeTime() {",
        ),
        (
            "    public void setInvokeTime(long invokeTime) {",
            "    /** 设置请求发起时间戳。 */\n    public void setInvokeTime(long invokeTime) {",
        ),
        (
            "    public String getClusterName() {",
            "    /** 返回集群名称。 */\n    public String getClusterName() {",
        ),
        (
            "    public void setClusterName(String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(String clusterName) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
        (
            "    public String getBrokerControllerIdsToClean() {",
            "    /** 返回待清理的 Controller brokerId 集合。 */\n    public String getBrokerControllerIdsToClean() {",
        ),
        (
            "    public void setBrokerControllerIdsToClean(String brokerIdSetToClean) {",
            "    /** 设置待清理的 Controller brokerId 集合。 */\n    public void setBrokerControllerIdsToClean(String brokerIdSetToClean) {",
        ),
        (
            "    public boolean isCleanLivingBroker() {",
            "    /** 返回是否清理存活 Broker 数据。 */\n    public boolean isCleanLivingBroker() {",
        ),
        (
            "    public void setCleanLivingBroker(boolean cleanLivingBroker) {",
            "    /** 设置是否清理存活 Broker 数据。 */\n    public void setCleanLivingBroker(boolean cleanLivingBroker) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含集群、Broker 组与清理参数的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/register/ApplyBrokerIdRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CONTROLLER_APPLY_BROKER_ID, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class ApplyBrokerIdRequestHeader implements CommandCustomHeader {",
            "/**\n * Broker 向 Controller 申请 brokerId 的请求头：携带集群、Broker 组名、申请的 brokerId 与注册校验码。\n */\n@RocketMQAction(value = RequestCode.CONTROLLER_APPLY_BROKER_ID, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class ApplyBrokerIdRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
            "    /** 目标集群名称。 */\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
        ),
        (
            "    private String brokerName;",
            "    /** 目标 Broker 组名称。 */\n    private String brokerName;",
        ),
        (
            "    private Long appliedBrokerId;",
            "    /** 申请的 brokerId。 */\n    private Long appliedBrokerId;",
        ),
        (
            "    private String registerCheckCode;",
            "    /** 注册校验码，用于防重放。 */\n    private String registerCheckCode;",
        ),
        (
            "    public ApplyBrokerIdRequestHeader() {",
            "    /** 默认构造。 */\n    public ApplyBrokerIdRequestHeader() {",
        ),
        (
            "    public ApplyBrokerIdRequestHeader(String clusterName, String brokerName, Long appliedBrokerId, String registerCheckCode) {",
            "    /** 指定集群、Broker 组、申请 brokerId 与校验码的构造。 */\n    public ApplyBrokerIdRequestHeader(String clusterName, String brokerName, Long appliedBrokerId, String registerCheckCode) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getClusterName() {",
            "    /** 返回集群名称。 */\n    public String getClusterName() {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public Long getAppliedBrokerId() {",
            "    /** 返回申请的 brokerId。 */\n    public Long getAppliedBrokerId() {",
        ),
        (
            "    public String getRegisterCheckCode() {",
            "    /** 返回注册校验码。 */\n    public String getRegisterCheckCode() {",
        ),
        (
            "    public void setClusterName(String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(String clusterName) {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
        (
            "    public void setAppliedBrokerId(Long appliedBrokerId) {",
            "    /** 设置申请的 brokerId。 */\n    public void setAppliedBrokerId(Long appliedBrokerId) {",
        ),
        (
            "    public void setRegisterCheckCode(String registerCheckCode) {",
            "    /** 设置注册校验码。 */\n    public void setRegisterCheckCode(String registerCheckCode) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/register/ApplyBrokerIdResponseHeader.java": [
        (
            "public class ApplyBrokerIdResponseHeader implements CommandCustomHeader {",
            "/**\n * Broker 申请 brokerId 的响应头：确认集群与 Broker 组名称。\n */\npublic class ApplyBrokerIdResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    private String clusterName;",
            "    /** 集群名称。 */\n    private String clusterName;",
        ),
        (
            "    private String brokerName;",
            "    /** Broker 组名称。 */\n    private String brokerName;",
        ),
        (
            "    public ApplyBrokerIdResponseHeader() {",
            "    /** 默认构造。 */\n    public ApplyBrokerIdResponseHeader() {",
        ),
        (
            "    public ApplyBrokerIdResponseHeader(String clusterName, String brokerName) {",
            "    /** 指定集群与 Broker 组名的构造。 */\n    public ApplyBrokerIdResponseHeader(String clusterName, String brokerName) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含集群与 Broker 组名的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getClusterName() {",
            "    /** 返回集群名称。 */\n    public String getClusterName() {",
        ),
        (
            "    public void setClusterName(String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(String clusterName) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/register/GetNextBrokerIdRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CONTROLLER_GET_NEXT_BROKER_ID, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetNextBrokerIdRequestHeader implements CommandCustomHeader {",
            "/**\n * 向 Controller 查询下一个可用 brokerId 的请求头：指定集群与 Broker 组名。\n */\n@RocketMQAction(value = RequestCode.CONTROLLER_GET_NEXT_BROKER_ID, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetNextBrokerIdRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
            "    /** 目标集群名称。 */\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
        ),
        (
            "    private String brokerName;",
            "    /** 目标 Broker 组名称。 */\n    private String brokerName;",
        ),
        (
            "    public GetNextBrokerIdRequestHeader() {",
            "    /** 默认构造。 */\n    public GetNextBrokerIdRequestHeader() {",
        ),
        (
            "    public GetNextBrokerIdRequestHeader(String clusterName, String brokerName) {",
            "    /** 指定集群与 Broker 组名的构造。 */\n    public GetNextBrokerIdRequestHeader(String clusterName, String brokerName) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含集群与 Broker 组名的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getClusterName() {",
            "    /** 返回集群名称。 */\n    public String getClusterName() {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
        (
            "    public void setClusterName(String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(String clusterName) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/register/GetNextBrokerIdResponseHeader.java": [
        (
            "public class GetNextBrokerIdResponseHeader implements CommandCustomHeader {",
            "/**\n * 查询下一个可用 brokerId 的响应头：返回集群、Broker 组名与分配的 nextBrokerId。\n */\npublic class GetNextBrokerIdResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    private String clusterName;",
            "    /** 集群名称。 */\n    private String clusterName;",
        ),
        (
            "    private String brokerName;",
            "    /** Broker 组名称。 */\n    private String brokerName;",
        ),
        (
            "    private Long nextBrokerId;",
            "    /** Controller 分配的下一个 brokerId。 */\n    private Long nextBrokerId;",
        ),
        (
            "    public GetNextBrokerIdResponseHeader() {",
            "    /** 默认构造。 */\n    public GetNextBrokerIdResponseHeader() {",
        ),
        (
            "    public GetNextBrokerIdResponseHeader(String clusterName, String brokerName) {",
            "    /** 指定集群与 Broker 组名的构造（nextBrokerId 为空）。 */\n    public GetNextBrokerIdResponseHeader(String clusterName, String brokerName) {",
        ),
        (
            "    public GetNextBrokerIdResponseHeader(String clusterName, String brokerName, Long nextBrokerId) {",
            "    /** 指定集群、Broker 组名与 nextBrokerId 的构造。 */\n    public GetNextBrokerIdResponseHeader(String clusterName, String brokerName, Long nextBrokerId) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含集群、Broker 组名与 nextBrokerId 的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public void setNextBrokerId(Long nextBrokerId) {",
            "    /** 设置 nextBrokerId。 */\n    public void setNextBrokerId(Long nextBrokerId) {",
        ),
        (
            "    public Long getNextBrokerId() {",
            "    /** 返回 nextBrokerId。 */\n    public Long getNextBrokerId() {",
        ),
        (
            "    public String getClusterName() {",
            "    /** 返回集群名称。 */\n    public String getClusterName() {",
        ),
        (
            "    public void setClusterName(String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(String clusterName) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
    ],
}
