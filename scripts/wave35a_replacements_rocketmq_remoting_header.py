"""Chinese JavaDoc replacements for RocketMQ wave35a remoting protocol header [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/register/RegisterBrokerToControllerRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CONTROLLER_REGISTER_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class RegisterBrokerToControllerRequestHeader implements CommandCustomHeader {",
            "/**\n * Broker 向 Controller 注册自身的请求头：携带集群、Broker 组名、brokerId 与访问地址。\n */\n@RocketMQAction(value = RequestCode.CONTROLLER_REGISTER_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class RegisterBrokerToControllerRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
            "    /** 集群名称。 */\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
        ),
        (
            "    private String brokerName;",
            "    /** Broker 组名称。 */\n    private String brokerName;",
        ),
        (
            "    private Long brokerId;",
            "    /** 本 Broker 的 brokerId。 */\n    private Long brokerId;",
        ),
        (
            "    private String brokerAddress;",
            "    /** Broker 对外服务地址。 */\n    private String brokerAddress;",
        ),
        (
            "    private long invokeTime;",
            "    /** 请求发起时间戳（毫秒）。 */\n    private long invokeTime;",
        ),
        (
            "    public RegisterBrokerToControllerRequestHeader() {",
            "    /** 默认构造。 */\n    public RegisterBrokerToControllerRequestHeader() {",
        ),
        (
            "    public RegisterBrokerToControllerRequestHeader(String clusterName, String brokerName, Long brokerId, String brokerAddress) {",
            "    /** 指定集群、Broker 组、brokerId 与地址的构造。 */\n    public RegisterBrokerToControllerRequestHeader(String clusterName, String brokerName, Long brokerId, String brokerAddress) {",
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
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public Long getBrokerId() {",
            "    /** 返回 brokerId。 */\n    public Long getBrokerId() {",
        ),
        (
            "    public String getBrokerAddress() {",
            "    /** 返回 Broker 地址。 */\n    public String getBrokerAddress() {",
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
            "    public void setBrokerId(Long brokerId) {",
            "    /** 设置 brokerId。 */\n    public void setBrokerId(Long brokerId) {",
        ),
        (
            "    public void setBrokerAddress(String brokerAddress) {",
            "    /** 设置 Broker 地址。 */\n    public void setBrokerAddress(String brokerAddress) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/controller/register/RegisterBrokerToControllerResponseHeader.java": [
        (
            "public class RegisterBrokerToControllerResponseHeader implements CommandCustomHeader {",
            "/**\n * Broker 向 Controller 注册的响应头：返回 Master 副本信息与同步副本集 epoch。\n */\npublic class RegisterBrokerToControllerResponseHeader implements CommandCustomHeader {",
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
            "    private Long masterBrokerId;",
            "    /** 当前 Master 的 brokerId。 */\n    private Long masterBrokerId;",
        ),
        (
            "    private String masterAddress;",
            "    /** Master 对外服务地址。 */\n    private String masterAddress;",
        ),
        (
            "    private Integer masterEpoch;",
            "    /** Master 的 epoch 版本号。 */\n    private Integer masterEpoch;",
        ),
        (
            "    private Integer syncStateSetEpoch;",
            "    /** 同步副本集（SyncStateSet）的 epoch。 */\n    private Integer syncStateSetEpoch;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public RegisterBrokerToControllerResponseHeader() {",
            "    /** 默认构造。 */\n    public RegisterBrokerToControllerResponseHeader() {",
        ),
        (
            "    public RegisterBrokerToControllerResponseHeader(String clusterName, String brokerName) {",
            "    /** 指定集群与 Broker 组名的构造。 */\n    public RegisterBrokerToControllerResponseHeader(String clusterName, String brokerName) {",
        ),
        (
            "    public void setMasterBrokerId(Long masterBrokerId) {",
            "    /** 设置 Master brokerId。 */\n    public void setMasterBrokerId(Long masterBrokerId) {",
        ),
        (
            "    public void setMasterAddress(String masterAddress) {",
            "    /** 设置 Master 地址。 */\n    public void setMasterAddress(String masterAddress) {",
        ),
        (
            "    public void setMasterEpoch(Integer masterEpoch) {",
            "    /** 设置 Master epoch。 */\n    public void setMasterEpoch(Integer masterEpoch) {",
        ),
        (
            "    public void setSyncStateSetEpoch(Integer syncStateSetEpoch) {",
            "    /** 设置同步副本集 epoch。 */\n    public void setSyncStateSetEpoch(Integer syncStateSetEpoch) {",
        ),
        (
            "    public Integer getMasterEpoch() {",
            "    /** 返回 Master epoch。 */\n    public Integer getMasterEpoch() {",
        ),
        (
            "    public Integer getSyncStateSetEpoch() {",
            "    /** 返回同步副本集 epoch。 */\n    public Integer getSyncStateSetEpoch() {",
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
            "    public Long getMasterBrokerId() {",
            "    /** 返回 Master brokerId。 */\n    public Long getMasterBrokerId() {",
        ),
        (
            "    public String getMasterAddress() {",
            "    /** 返回 Master 地址。 */\n    public String getMasterAddress() {",
        ),
        (
            "    public void setClusterName(String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(String clusterName) {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/AddWritePermOfBrokerRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.ADD_WRITE_PERM_OF_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class AddWritePermOfBrokerRequestHeader implements CommandCustomHeader {",
            "/**\n * 为 Broker 恢复写权限的请求头：NameServer 解除对该 Broker 的只读限制。\n */\n@RocketMQAction(value = RequestCode.ADD_WRITE_PERM_OF_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class AddWritePermOfBrokerRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String brokerName;",
            "    /** 目标 Broker 组名称。 */\n    @CFNotNull\n    private String brokerName;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
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
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/AddWritePermOfBrokerResponseHeader.java": [
        (
            "public class AddWritePermOfBrokerResponseHeader implements CommandCustomHeader {",
            "/**\n * 恢复 Broker 写权限的响应头：返回本次恢复写权限涉及的 Topic 数量。\n */\npublic class AddWritePermOfBrokerResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Integer addTopicCount;",
            "    /** 恢复写权限的 Topic 数量。 */\n    @CFNotNull\n    private Integer addTopicCount;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public Integer getAddTopicCount() {",
            "    /** 返回恢复写权限的 Topic 数量。 */\n    public Integer getAddTopicCount() {",
        ),
        (
            "    public void setAddTopicCount(Integer addTopicCount) {",
            "    /** 设置恢复写权限的 Topic 数量。 */\n    public void setAddTopicCount(Integer addTopicCount) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/BrokerHeartbeatRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.BROKER_HEARTBEAT, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class BrokerHeartbeatRequestHeader implements CommandCustomHeader {",
            "/**\n * Broker 向 NameServer 上报心跳的请求头：携带集群、地址、epoch 与消费位点等存活信息。\n */\n@RocketMQAction(value = RequestCode.BROKER_HEARTBEAT, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class BrokerHeartbeatRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
            "    /** 集群名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
        ),
        (
            "    @CFNotNull\n    private String brokerAddr;",
            "    /** Broker 对外服务地址。 */\n    @CFNotNull\n    private String brokerAddr;",
        ),
        (
            "    @CFNotNull\n    private String brokerName;",
            "    /** Broker 组名称。 */\n    @CFNotNull\n    private String brokerName;",
        ),
        (
            "    @CFNullable\n    private Long brokerId;",
            "    /** Broker 的 brokerId，可为空。 */\n    @CFNullable\n    private Long brokerId;",
        ),
        (
            "    @CFNullable\n    private Integer epoch;",
            "    /** Master epoch 版本号，可为空。 */\n    @CFNullable\n    private Integer epoch;",
        ),
        (
            "    @CFNullable\n    private Long maxOffset;",
            "    /** CommitLog 最大物理 offset，可为空。 */\n    @CFNullable\n    private Long maxOffset;",
        ),
        (
            "    @CFNullable\n    private Long confirmOffset;",
            "    /** 已确认刷盘 offset，可为空。 */\n    @CFNullable\n    private Long confirmOffset;",
        ),
        (
            "    @CFNullable\n    private Long heartbeatTimeoutMills;",
            "    /** 心跳超时时间（毫秒），可为空。 */\n    @CFNullable\n    private Long heartbeatTimeoutMills;",
        ),
        (
            "    @CFNullable\n    private Integer electionPriority;",
            "    /** 选主优先级，可为空。 */\n    @CFNullable\n    private Integer electionPriority;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getBrokerAddr() {",
            "    /** 返回 Broker 地址。 */\n    public String getBrokerAddr() {",
        ),
        (
            "    public void setBrokerAddr(String brokerAddr) {",
            "    /** 设置 Broker 地址。 */\n    public void setBrokerAddr(String brokerAddr) {",
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
            "    public Integer getEpoch() {",
            "    /** 返回 Master epoch。 */\n    public Integer getEpoch() {",
        ),
        (
            "    public void setEpoch(Integer epoch) {",
            "    /** 设置 Master epoch。 */\n    public void setEpoch(Integer epoch) {",
        ),
        (
            "    public Long getMaxOffset() {",
            "    /** 返回最大物理 offset。 */\n    public Long getMaxOffset() {",
        ),
        (
            "    public void setMaxOffset(Long maxOffset) {",
            "    /** 设置最大物理 offset。 */\n    public void setMaxOffset(Long maxOffset) {",
        ),
        (
            "    public Long getConfirmOffset() {",
            "    /** 返回已确认刷盘 offset。 */\n    public Long getConfirmOffset() {",
        ),
        (
            "    public void setConfirmOffset(Long confirmOffset) {",
            "    /** 设置已确认刷盘 offset。 */\n    public void setConfirmOffset(Long confirmOffset) {",
        ),
        (
            "    public Long getBrokerId() {",
            "    /** 返回 brokerId。 */\n    public Long getBrokerId() {",
        ),
        (
            "    public void setBrokerId(Long brokerId) {",
            "    /** 设置 brokerId。 */\n    public void setBrokerId(Long brokerId) {",
        ),
        (
            "    public Long getHeartbeatTimeoutMills() {",
            "    /** 返回心跳超时时间（毫秒）。 */\n    public Long getHeartbeatTimeoutMills() {",
        ),
        (
            "    public void setHeartbeatTimeoutMills(Long heartbeatTimeoutMills) {",
            "    /** 设置心跳超时时间（毫秒）。 */\n    public void setHeartbeatTimeoutMills(Long heartbeatTimeoutMills) {",
        ),
        (
            "    public Integer getElectionPriority() {",
            "    /** 返回选主优先级。 */\n    public Integer getElectionPriority() {",
        ),
        (
            "    public void setElectionPriority(Integer electionPriority) {",
            "    /** 设置选主优先级。 */\n    public void setElectionPriority(Integer electionPriority) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/DeleteKVConfigRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.DELETE_KV_CONFIG, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class DeleteKVConfigRequestHeader implements CommandCustomHeader {",
            "/**\n * 删除 NameServer KV 配置的请求头：按 namespace 与 key 移除键值对。\n */\n@RocketMQAction(value = RequestCode.DELETE_KV_CONFIG, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class DeleteKVConfigRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String namespace;",
            "    /** KV 配置命名空间。 */\n    @CFNotNull\n    private String namespace;",
        ),
        (
            "    @CFNotNull\n    private String key;",
            "    /** 待删除的配置键。 */\n    @CFNotNull\n    private String key;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getNamespace() {",
            "    /** 返回命名空间。 */\n    public String getNamespace() {",
        ),
        (
            "    public void setNamespace(String namespace) {",
            "    /** 设置命名空间。 */\n    public void setNamespace(String namespace) {",
        ),
        (
            "    public String getKey() {",
            "    /** 返回配置键。 */\n    public String getKey() {",
        ),
        (
            "    public void setKey(String key) {",
            "    /** 设置配置键。 */\n    public void setKey(String key) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/DeleteTopicFromNamesrvRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.DELETE_TOPIC_IN_NAMESRV, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class DeleteTopicFromNamesrvRequestHeader extends TopicRequestHeader {",
            "/**\n * 从 NameServer 路由表删除 Topic 的请求头：指定 Topic 与可选集群名。\n */\n@RocketMQAction(value = RequestCode.DELETE_TOPIC_IN_NAMESRV, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class DeleteTopicFromNamesrvRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private String topic;",
            "    /** 待删除的 Topic 名称。 */\n    @CFNotNull\n    private String topic;",
        ),
        (
            "    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
            "    /** 目标集群名称，可为空。 */\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
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
            "    public String getClusterName() {",
            "    /** 返回集群名称。 */\n    public String getClusterName() {",
        ),
        (
            "    public void setClusterName(String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(String clusterName) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/GetKVConfigRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_KV_CONFIG, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetKVConfigRequestHeader implements CommandCustomHeader {",
            "/**\n * 读取 NameServer KV 配置的请求头：按 namespace 与 key 查询单个键值。\n */\n@RocketMQAction(value = RequestCode.GET_KV_CONFIG, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetKVConfigRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String namespace;",
            "    /** KV 配置命名空间。 */\n    @CFNotNull\n    private String namespace;",
        ),
        (
            "    @CFNotNull\n    private String key;",
            "    /** 待查询的配置键。 */\n    @CFNotNull\n    private String key;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getNamespace() {",
            "    /** 返回命名空间。 */\n    public String getNamespace() {",
        ),
        (
            "    public void setNamespace(String namespace) {",
            "    /** 设置命名空间。 */\n    public void setNamespace(String namespace) {",
        ),
        (
            "    public String getKey() {",
            "    /** 返回配置键。 */\n    public String getKey() {",
        ),
        (
            "    public void setKey(String key) {",
            "    /** 设置配置键。 */\n    public void setKey(String key) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/GetKVConfigResponseHeader.java": [
        (
            "public class GetKVConfigResponseHeader implements CommandCustomHeader {",
            "/**\n * 读取 NameServer KV 配置的响应头：返回查询到的配置值。\n */\npublic class GetKVConfigResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNullable\n    private String value;",
            "    /** 配置值，不存在时可为空。 */\n    @CFNullable\n    private String value;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getValue() {",
            "    /** 返回配置值。 */\n    public String getValue() {",
        ),
        (
            "    public void setValue(String value) {",
            "    /** 设置配置值。 */\n    public void setValue(String value) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/GetKVListByNamespaceRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_KVLIST_BY_NAMESPACE, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetKVListByNamespaceRequestHeader implements CommandCustomHeader {",
            "/**\n * 按命名空间列出 NameServer KV 键的请求头：返回该 namespace 下全部 key。\n */\n@RocketMQAction(value = RequestCode.GET_KVLIST_BY_NAMESPACE, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetKVListByNamespaceRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String namespace;",
            "    /** KV 配置命名空间。 */\n    @CFNotNull\n    private String namespace;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getNamespace() {",
            "    /** 返回命名空间。 */\n    public String getNamespace() {",
        ),
        (
            "    public void setNamespace(String namespace) {",
            "    /** 设置命名空间。 */\n    public void setNamespace(String namespace) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/GetRouteInfoRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_ROUTEINFO_BY_TOPIC, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetRouteInfoRequestHeader extends TopicRequestHeader {",
            "/**\n * 按 Topic 查询路由信息的请求头：NameServer 返回 Broker 队列分布。\n * acceptStandardJsonOnly 控制是否仅接受标准 JSON 格式路由数据。\n */\n@RocketMQAction(value = RequestCode.GET_ROUTEINFO_BY_TOPIC, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetRouteInfoRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    private String topic;",
        ),
        (
            "    @CFNullable\n    private Boolean acceptStandardJsonOnly;",
            "    /** 是否仅接受标准 JSON 路由格式，可为空。 */\n    @CFNullable\n    private Boolean acceptStandardJsonOnly;",
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
            "    public Boolean getAcceptStandardJsonOnly() {",
            "    /** 返回是否仅接受标准 JSON 路由格式。 */\n    public Boolean getAcceptStandardJsonOnly() {",
        ),
        (
            "    public void setAcceptStandardJsonOnly(Boolean acceptStandardJsonOnly) {",
            "    /** 设置是否仅接受标准 JSON 路由格式。 */\n    public void setAcceptStandardJsonOnly(Boolean acceptStandardJsonOnly) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/PutKVConfigRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.PUT_KV_CONFIG, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class PutKVConfigRequestHeader implements CommandCustomHeader {",
            "/**\n * 写入 NameServer KV 配置的请求头：按 namespace、key 写入 value。\n */\n@RocketMQAction(value = RequestCode.PUT_KV_CONFIG, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class PutKVConfigRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String namespace;",
            "    /** KV 配置命名空间。 */\n    @CFNotNull\n    private String namespace;",
        ),
        (
            "    @CFNotNull\n    private String key;",
            "    /** 配置键。 */\n    @CFNotNull\n    private String key;",
        ),
        (
            "    @CFNotNull\n    private String value;",
            "    /** 配置值。 */\n    @CFNotNull\n    private String value;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getNamespace() {",
            "    /** 返回命名空间。 */\n    public String getNamespace() {",
        ),
        (
            "    public void setNamespace(String namespace) {",
            "    /** 设置命名空间。 */\n    public void setNamespace(String namespace) {",
        ),
        (
            "    public String getKey() {",
            "    /** 返回配置键。 */\n    public String getKey() {",
        ),
        (
            "    public void setKey(String key) {",
            "    /** 设置配置键。 */\n    public void setKey(String key) {",
        ),
        (
            "    public String getValue() {",
            "    /** 返回配置值。 */\n    public String getValue() {",
        ),
        (
            "    public void setValue(String value) {",
            "    /** 设置配置值。 */\n    public void setValue(String value) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/QueryDataVersionRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.QUERY_DATA_VERSION, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class QueryDataVersionRequestHeader implements CommandCustomHeader {",
            "/**\n * 查询 Broker 数据版本是否变更的请求头：NameServer 比对本地 Topic 配置版本。\n */\n@RocketMQAction(value = RequestCode.QUERY_DATA_VERSION, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class QueryDataVersionRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String brokerName;",
            "    /** Broker 组名称。 */\n    @CFNotNull\n    private String brokerName;",
        ),
        (
            "    @CFNotNull\n    private String brokerAddr;",
            "    /** Broker 对外服务地址。 */\n    @CFNotNull\n    private String brokerAddr;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
            "    /** 集群名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
        ),
        (
            "    @CFNotNull\n    private Long brokerId;",
            "    /** Broker 的 brokerId。 */\n    @CFNotNull\n    private Long brokerId;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
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
            "    public String getBrokerAddr() {",
            "    /** 返回 Broker 地址。 */\n    public String getBrokerAddr() {",
        ),
        (
            "    public void setBrokerAddr(String brokerAddr) {",
            "    /** 设置 Broker 地址。 */\n    public void setBrokerAddr(String brokerAddr) {",
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
            "    public Long getBrokerId() {",
            "    /** 返回 brokerId。 */\n    public Long getBrokerId() {",
        ),
        (
            "    public void setBrokerId(Long brokerId) {",
            "    /** 设置 brokerId。 */\n    public void setBrokerId(Long brokerId) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/QueryDataVersionResponseHeader.java": [
        (
            "public class QueryDataVersionResponseHeader implements CommandCustomHeader {",
            "/**\n * 查询 Broker 数据版本的响应头：changed 表示 NameServer 侧 Topic 配置是否已变更。\n */\npublic class QueryDataVersionResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Boolean changed;",
            "    /** 数据版本是否已变更。 */\n    @CFNotNull\n    private Boolean changed;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public Boolean getChanged() {",
            "    /** 返回数据版本是否已变更。 */\n    public Boolean getChanged() {",
        ),
        (
            "    public void setChanged(Boolean changed) {",
            "    /** 设置数据版本是否已变更。 */\n    public void setChanged(Boolean changed) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 changed 字段的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/RegisterBrokerRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.REGISTER_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class RegisterBrokerRequestHeader implements CommandCustomHeader {",
            "/**\n * Broker 向 NameServer 注册的请求头：上报集群、地址、HA 地址及 brokerId 等元数据。\n * 请求体可携带 Topic 配置列表，compressed 与 bodyCrc32 标识压缩与校验。\n */\n@RocketMQAction(value = RequestCode.REGISTER_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class RegisterBrokerRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String brokerName;",
            "    /** Broker 组名称。 */\n    @CFNotNull\n    private String brokerName;",
        ),
        (
            "    @CFNotNull\n    private String brokerAddr;",
            "    /** Broker 对外服务地址。 */\n    @CFNotNull\n    private String brokerAddr;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
            "    /** 集群名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
        ),
        (
            "    @CFNotNull\n    private String haServerAddr;",
            "    /** HA 服务地址，用于主从同步。 */\n    @CFNotNull\n    private String haServerAddr;",
        ),
        (
            "    @CFNotNull\n    private Long brokerId;",
            "    /** Broker 的 brokerId（0 表示 Master）。 */\n    @CFNotNull\n    private Long brokerId;",
        ),
        (
            "    @CFNullable\n    private Long heartbeatTimeoutMillis;",
            "    /** 心跳超时时间（毫秒），可为空。 */\n    @CFNullable\n    private Long heartbeatTimeoutMillis;",
        ),
        (
            "    @CFNullable\n    private Boolean enableActingMaster;",
            "    /** 是否允许 Acting Master 模式，可为空。 */\n    @CFNullable\n    private Boolean enableActingMaster;",
        ),
        (
            "    private boolean compressed;",
            "    /** 请求体是否压缩。 */\n    private boolean compressed;",
        ),
        (
            "    private Integer bodyCrc32 = 0;",
            "    /** 请求体 CRC32 校验值。 */\n    private Integer bodyCrc32 = 0;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
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
            "    public String getBrokerAddr() {",
            "    /** 返回 Broker 地址。 */\n    public String getBrokerAddr() {",
        ),
        (
            "    public void setBrokerAddr(String brokerAddr) {",
            "    /** 设置 Broker 地址。 */\n    public void setBrokerAddr(String brokerAddr) {",
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
            "    public String getHaServerAddr() {",
            "    /** 返回 HA 服务地址。 */\n    public String getHaServerAddr() {",
        ),
        (
            "    public void setHaServerAddr(String haServerAddr) {",
            "    /** 设置 HA 服务地址。 */\n    public void setHaServerAddr(String haServerAddr) {",
        ),
        (
            "    public Long getBrokerId() {",
            "    /** 返回 brokerId。 */\n    public Long getBrokerId() {",
        ),
        (
            "    public void setBrokerId(Long brokerId) {",
            "    /** 设置 brokerId。 */\n    public void setBrokerId(Long brokerId) {",
        ),
        (
            "    public Long getHeartbeatTimeoutMillis() {",
            "    /** 返回心跳超时时间（毫秒）。 */\n    public Long getHeartbeatTimeoutMillis() {",
        ),
        (
            "    public void setHeartbeatTimeoutMillis(Long heartbeatTimeoutMillis) {",
            "    /** 设置心跳超时时间（毫秒）。 */\n    public void setHeartbeatTimeoutMillis(Long heartbeatTimeoutMillis) {",
        ),
        (
            "    public boolean isCompressed() {",
            "    /** 返回请求体是否压缩。 */\n    public boolean isCompressed() {",
        ),
        (
            "    public void setCompressed(boolean compressed) {",
            "    /** 设置请求体是否压缩。 */\n    public void setCompressed(boolean compressed) {",
        ),
        (
            "    public Integer getBodyCrc32() {",
            "    /** 返回请求体 CRC32 校验值。 */\n    public Integer getBodyCrc32() {",
        ),
        (
            "    public void setBodyCrc32(Integer bodyCrc32) {",
            "    /** 设置请求体 CRC32 校验值。 */\n    public void setBodyCrc32(Integer bodyCrc32) {",
        ),
        (
            "    public Boolean getEnableActingMaster() {",
            "    /** 返回是否允许 Acting Master 模式。 */\n    public Boolean getEnableActingMaster() {",
        ),
        (
            "    public void setEnableActingMaster(Boolean enableActingMaster) {",
            "    /** 设置是否允许 Acting Master 模式。 */\n    public void setEnableActingMaster(Boolean enableActingMaster) {",
        ),
    ],
}
