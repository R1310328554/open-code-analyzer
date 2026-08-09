"""Chinese JavaDoc replacements for RocketMQ wave31b remoting protocol header [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/EndTransactionResponseHeader.java": [
        (
            "public class EndTransactionResponseHeader implements CommandCustomHeader {",
            "/**\n * 结束事务响应头：Broker 处理 {@link EndTransactionRequestHeader} 后返回，无附加字段。\n */\npublic class EndTransactionResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验响应头字段（本类无字段，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ExchangeHAInfoRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.EXCHANGE_BROKER_HA_INFO,resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class ExchangeHAInfoRequestHeader implements CommandCustomHeader {",
            "/**\n * 交换 Broker 主从 HA 信息请求头：同步 Master HA 地址、刷盘位点及 Broker 地址。\n */\n@RocketMQAction(value = RequestCode.EXCHANGE_BROKER_HA_INFO,resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class ExchangeHAInfoRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNullable\n    public String masterHaAddress;",
            "    /** Master HA 服务地址，可为空。 */\n    @CFNullable\n    public String masterHaAddress;",
        ),
        (
            "    @CFNullable\n    public Long masterFlushOffset;",
            "    /** Master 已刷盘位点，可为空。 */\n    @CFNullable\n    public Long masterFlushOffset;",
        ),
        (
            "    @CFNullable\n    public String masterAddress;",
            "    /** Master Broker 地址，可为空。 */\n    @CFNullable\n    public String masterAddress;",
        ),
        (
            "    public String getMasterHaAddress() {",
            "    /** 返回 Master HA 地址。 */\n    public String getMasterHaAddress() {",
        ),
        (
            "    public void setMasterHaAddress(String masterHaAddress) {",
            "    /** 设置 Master HA 地址。 */\n    public void setMasterHaAddress(String masterHaAddress) {",
        ),
        (
            "    public Long getMasterFlushOffset() {",
            "    /** 返回 Master 刷盘位点。 */\n    public Long getMasterFlushOffset() {",
        ),
        (
            "    public void setMasterFlushOffset(Long masterFlushOffset) {",
            "    /** 设置 Master 刷盘位点。 */\n    public void setMasterFlushOffset(Long masterFlushOffset) {",
        ),
        (
            "    public String getMasterAddress() {",
            "    /** 返回 Master Broker 地址。 */\n    public String getMasterAddress() {",
        ),
        (
            "    public void setMasterAddress(String masterAddress) {",
            "    /** 设置 Master Broker 地址。 */\n    public void setMasterAddress(String masterAddress) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ExchangeHAInfoResponseHeader.java": [
        (
            "public class ExchangeHAInfoResponseHeader implements CommandCustomHeader {",
            "/**\n * 交换 Broker HA 信息响应头：回传 Master HA 地址、刷盘位点及 Broker 地址。\n */\npublic class ExchangeHAInfoResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNullable\n    public String masterHaAddress;",
            "    /** Master HA 服务地址，可为空。 */\n    @CFNullable\n    public String masterHaAddress;",
        ),
        (
            "    @CFNullable\n    public Long masterFlushOffset;",
            "    /** Master 已刷盘位点，可为空。 */\n    @CFNullable\n    public Long masterFlushOffset;",
        ),
        (
            "    @CFNullable\n    public String masterAddress;",
            "    /** Master Broker 地址，可为空。 */\n    @CFNullable\n    public String masterAddress;",
        ),
        (
            "    public String getMasterHaAddress() {",
            "    /** 返回 Master HA 地址。 */\n    public String getMasterHaAddress() {",
        ),
        (
            "    public void setMasterHaAddress(String masterHaAddress) {",
            "    /** 设置 Master HA 地址。 */\n    public void setMasterHaAddress(String masterHaAddress) {",
        ),
        (
            "    public Long getMasterFlushOffset() {",
            "    /** 返回 Master 刷盘位点。 */\n    public Long getMasterFlushOffset() {",
        ),
        (
            "    public void setMasterFlushOffset(Long masterFlushOffset) {",
            "    /** 设置 Master 刷盘位点。 */\n    public void setMasterFlushOffset(Long masterFlushOffset) {",
        ),
        (
            "    public String getMasterAddress() {",
            "    /** 返回 Master Broker 地址。 */\n    public String getMasterAddress() {",
        ),
        (
            "    public void setMasterAddress(String masterAddress) {",
            "    /** 设置 Master Broker 地址。 */\n    public void setMasterAddress(String masterAddress) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ExportRocksDBConfigToJsonRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.EXPORT_ROCKSDB_CONFIG_TO_JSON, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class ExportRocksDBConfigToJsonRequestHeader implements CommandCustomHeader {",
            "/**\n * 将 RocksDB 持久化配置导出为 JSON 的请求头：指定 Topic、订阅组或消费位点等配置类型。\n */\n@RocketMQAction(value = RequestCode.EXPORT_ROCKSDB_CONFIG_TO_JSON, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class ExportRocksDBConfigToJsonRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    public enum ConfigType {",
            "    /** RocksDB 可导出的配置类型枚举。 */\n    public enum ConfigType {",
        ),
        (
            "        TOPICS(\"topics\"),\n        SUBSCRIPTION_GROUPS(\"subscriptionGroups\"),\n        CONSUMER_OFFSETS(\"consumerOffsets\");",
            "        /** Topic 配置。 */\n        TOPICS(\"topics\"),\n        /** 订阅组配置。 */\n        SUBSCRIPTION_GROUPS(\"subscriptionGroups\"),\n        /** 消费位点配置。 */\n        CONSUMER_OFFSETS(\"consumerOffsets\");",
        ),
        (
            "        public static ConfigType getConfigTypeByName(String typeName) {",
            "        /** 按名称（忽略大小写）解析配置类型。 */\n        public static ConfigType getConfigTypeByName(String typeName) {",
        ),
        (
            "        public static List<ConfigType> fromString(String ordinal) {",
            "        /** 从分号分隔的类型名字符串解析配置类型列表。 */\n        public static List<ConfigType> fromString(String ordinal) {",
        ),
        (
            "        public static String toString(List<ConfigType> configTypes) {",
            "        /** 将配置类型列表序列化为分号分隔字符串。 */\n        public static String toString(List<ConfigType> configTypes) {",
        ),
        (
            "        public String getTypeName() {",
            "        /** 返回配置类型名称。 */\n        public String getTypeName() {",
        ),
        (
            "    @CFNotNull\n    private String configType;",
            "    /** 待导出的配置类型，分号分隔。 */\n    @CFNotNull\n    private String configType;",
        ),
        (
            "    public List<ConfigType> fetchConfigType() {",
            "    /** 解析并返回配置类型列表。 */\n    public List<ConfigType> fetchConfigType() {",
        ),
        (
            "    public void updateConfigType(List<ConfigType> configType) {",
            "    /** 以类型列表更新 configType 字符串。 */\n    public void updateConfigType(List<ConfigType> configType) {",
        ),
        (
            "    public String getConfigType() {",
            "    /** 返回配置类型字符串。 */\n    public String getConfigType() {",
        ),
        (
            "    public void setConfigType(String configType) {",
            "    /** 设置配置类型字符串。 */\n    public void setConfigType(String configType) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ExtraInfoUtil.java": [
        (
            "public class ExtraInfoUtil {",
            "/**\n * Pop 消费 extraInfo 编解码工具：解析/构建 Pop 附加串中的位点、时间、重试 Topic 等字段。\n * extraInfo 各段以 {@link MessageConst#KEY_SEPARATOR} 分隔。\n */\npublic class ExtraInfoUtil {",
        ),
        (
            "    private static final String NORMAL_TOPIC = \"0\";",
            "    /** 普通 Topic 标识。 */\n    private static final String NORMAL_TOPIC = \"0\";",
        ),
        (
            "    private static final String RETRY_TOPIC = \"1\";",
            "    /** Pop 重试 Topic V1 标识。 */\n    private static final String RETRY_TOPIC = \"1\";",
        ),
        (
            "    private static final String RETRY_TOPIC_V2 = \"2\";",
            "    /** Pop 重试 Topic V2 标识。 */\n    private static final String RETRY_TOPIC_V2 = \"2\";",
        ),
        (
            "    private static final String QUEUE_OFFSET = \"qo\";",
            "    /** 队列位点键前缀（Lite 顺序消费）。 */\n    private static final String QUEUE_OFFSET = \"qo\";",
        ),
        (
            "    public static String[] split(String extraInfo) {",
            "    /** 按分隔符拆分 extraInfo 字符串。 */\n    public static String[] split(String extraInfo) {",
        ),
        (
            "    public static Long getCkQueueOffset(String[] extraInfoStrs) {",
            "    /** 从拆分结果取 CheckPoint 队列位点（第 0 段）。 */\n    public static Long getCkQueueOffset(String[] extraInfoStrs) {",
        ),
        (
            "    public static Long getPopTime(String[] extraInfoStrs) {",
            "    /** 从拆分结果取 Pop 时间戳（第 1 段）。 */\n    public static Long getPopTime(String[] extraInfoStrs) {",
        ),
        (
            "    public static Long getInvisibleTime(String[] extraInfoStrs) {",
            "    /** 从拆分结果取不可见时长（第 2 段）。 */\n    public static Long getInvisibleTime(String[] extraInfoStrs) {",
        ),
        (
            "    public static int getReviveQid(String[] extraInfoStrs) {",
            "    /** 从拆分结果取 Revive 队列 ID（第 3 段）。 */\n    public static int getReviveQid(String[] extraInfoStrs) {",
        ),
        (
            "    public static String getRealTopic(String[] extraInfoStrs, String topic, String cid) {",
            "    /** 根据重试标志解析真实 Topic（含 Pop 重试 Topic 构建）。 */\n    public static String getRealTopic(String[] extraInfoStrs, String topic, String cid) {",
        ),
        (
            "    public static String getRealTopic(String topic, String cid, String retry) {",
            "    /** 按重试标志字符串解析真实 Topic。 */\n    public static String getRealTopic(String topic, String cid, String retry) {",
        ),
        (
            "    public static String getRetry(String[] extraInfoStrs) {",
            "    /** 从拆分结果取重试 Topic 类型标志（第 4 段）。 */\n    public static String getRetry(String[] extraInfoStrs) {",
        ),
        (
            "    public static String getBrokerName(String[] extraInfoStrs) {",
            "    /** 从拆分结果取 Broker 名称（第 5 段）。 */\n    public static String getBrokerName(String[] extraInfoStrs) {",
        ),
        (
            "    public static int getQueueId(String[] extraInfoStrs) {",
            "    /** 从拆分结果取队列 ID（第 6 段）。 */\n    public static int getQueueId(String[] extraInfoStrs) {",
        ),
        (
            "    public static long getQueueOffset(String[] extraInfoStrs) {",
            "    /** 从拆分结果取消息队列位点（第 7 段）。 */\n    public static long getQueueOffset(String[] extraInfoStrs) {",
        ),
        (
            "    public static String buildExtraInfo(long ckQueueOffset, long popTime, long invisibleTime, int reviveQid, String topic, String brokerName, int queueId) {",
            "    /** 构建标准 Pop extraInfo 串（8 段）。 */\n    public static String buildExtraInfo(long ckQueueOffset, long popTime, long invisibleTime, int reviveQid, String topic, String brokerName, int queueId) {",
        ),
        (
            "    public static String buildExtraInfo(long ckQueueOffset, long popTime, long invisibleTime, int reviveQid, String topic, String brokerName, int queueId,\n                                        long msgQueueOffset) {",
            "    /** 构建含消息队列位点的 Pop extraInfo 串（9 段）。 */\n    public static String buildExtraInfo(long ckQueueOffset, long popTime, long invisibleTime, int reviveQid, String topic, String brokerName, int queueId,\n                                        long msgQueueOffset) {",
        ),
        (
            "    public static void buildStartOffsetInfo(StringBuilder stringBuilder, String topic, int queueId, long startOffset) {",
            "    /** 向 StringBuilder 追加起始位点信息段。 */\n    public static void buildStartOffsetInfo(StringBuilder stringBuilder, String topic, int queueId, long startOffset) {",
        ),
        (
            "    public static void buildQueueIdOrderCountInfo(StringBuilder stringBuilder, String topic, int queueId, int orderCount) {",
            "    /** 向 StringBuilder 追加队列 ID 与顺序计数信息。 */\n    public static void buildQueueIdOrderCountInfo(StringBuilder stringBuilder, String topic, int queueId, int orderCount) {",
        ),
        (
            "    public static void buildQueueOffsetOrderCountInfo(StringBuilder stringBuilder, String topic, long queueId, long queueOffset, int orderCount) {",
            "    /** 向 StringBuilder 追加队列位点与顺序计数信息。 */\n    public static void buildQueueOffsetOrderCountInfo(StringBuilder stringBuilder, String topic, long queueId, long queueOffset, int orderCount) {",
        ),
        (
            "    public static void buildMsgOffsetInfo(StringBuilder stringBuilder, String topic, int queueId, List<Long> msgOffsets) {",
            "    /** 向 StringBuilder 追加消息位点列表信息。 */\n    public static void buildMsgOffsetInfo(StringBuilder stringBuilder, String topic, int queueId, List<Long> msgOffsets) {",
        ),
        (
            "    public static Map<String, List<Long>> parseMsgOffsetInfo(String msgOffsetInfo) {",
            "    /** 解析消息位点信息串为 retry@queueId → 位点列表映射。 */\n    public static Map<String, List<Long>> parseMsgOffsetInfo(String msgOffsetInfo) {",
        ),
        (
            "    public static Map<String, Long> parseStartOffsetInfo(String startOffsetInfo) {",
            "    /** 解析起始位点信息串为 retry@queueId → 位点映射。 */\n    public static Map<String, Long> parseStartOffsetInfo(String startOffsetInfo) {",
        ),
        (
            "    public static Map<String, Integer> parseOrderCountInfo(String orderCountInfo) {",
            "    /** 解析顺序计数信息串为 retry@queueId → 计数映射。 */\n    public static Map<String, Integer> parseOrderCountInfo(String orderCountInfo) {",
        ),
        (
            "    public static List<Integer> parseLiteOrderCountInfo(String orderCountInfo, int msgCount) {",
            "    /** 解析 Lite 顺序计数信息，段数须与 msgCount 一致。 */\n    public static List<Integer> parseLiteOrderCountInfo(String orderCountInfo, int msgCount) {",
        ),
        (
            "    private static int parseLiteOrderCount(String info) {",
            "    /** 解析单段 Lite 顺序计数值。 */\n    private static int parseLiteOrderCount(String info) {",
        ),
        (
            "    public static String getStartOffsetInfoMapKey(String topic, long key) {",
            "    /** 构建起始位点 Map 键（retry@key）。 */\n    public static String getStartOffsetInfoMapKey(String topic, long key) {",
        ),
        (
            "    public static String getStartOffsetInfoMapKey(String topic, String popCk, long key) {",
            "    /** 基于 popCk 或 Topic 构建起始位点 Map 键。 */\n    public static String getStartOffsetInfoMapKey(String topic, String popCk, long key) {",
        ),
        (
            "    public static String getQueueOffsetKeyValueKey(long queueId, long queueOffset) {",
            "    /** 构建队列位点键值（qo{queueId}%{queueOffset}）。 */\n    public static String getQueueOffsetKeyValueKey(long queueId, long queueOffset) {",
        ),
        (
            "    public static String getQueueOffsetMapKey(String topic, long queueId, long queueOffset) {",
            "    /** 构建队列位点 Map 键（retry@qo...）。 */\n    public static String getQueueOffsetMapKey(String topic, long queueId, long queueOffset) {",
        ),
        (
            "    public static boolean isOrder(String[] extraInfo) {",
            "    /** 判断 extraInfo 是否对应顺序消费（Revive 队列 ID 为 POP_ORDER_REVIVE_QUEUE）。 */\n    public static boolean isOrder(String[] extraInfo) {",
        ),
        (
            "    private static String getRetry(String topic) {",
            "    /** 根据 Topic 名推断重试类型标志。 */\n    private static String getRetry(String topic) {",
        ),
        (
            "    private static String getRetry(String topic, String popCk) {",
            "    /** 优先从 popCk 解析重试类型，否则按 Topic 推断。 */\n    private static String getRetry(String topic, String popCk) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetAclRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.AUTH_GET_ACL, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetAclRequestHeader implements CommandCustomHeader {",
            "/**\n * 查询 ACL 权限配置的请求头：按 subject（用户/资源主体）检索访问控制规则。\n */\n@RocketMQAction(value = RequestCode.AUTH_GET_ACL, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetAclRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String subject;",
            "    /** ACL 主体标识（用户名或资源主体）。 */\n    private String subject;",
        ),
        (
            "    public GetAclRequestHeader() {",
            "    /** 无参构造。 */\n    public GetAclRequestHeader() {",
        ),
        (
            "    public GetAclRequestHeader(String subject) {",
            "    /** 以 subject 构造。 */\n    public GetAclRequestHeader(String subject) {",
        ),
        (
            "    public String getSubject() {",
            "    /** 返回 ACL 主体。 */\n    public String getSubject() {",
        ),
        (
            "    public void setSubject(String subject) {",
            "    /** 设置 ACL 主体。 */\n    public void setSubject(String subject) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetAllProducerInfoRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_ALL_PRODUCER_INFO, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetAllProducerInfoRequestHeader implements CommandCustomHeader {",
            "/**\n * 获取集群全部生产者连接信息的请求头：无附加参数。\n */\n@RocketMQAction(value = RequestCode.GET_ALL_PRODUCER_INFO, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetAllProducerInfoRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n        // To change body of implemented methods use File | Settings | File\n        // Templates.\n    }",
            "    /** 校验请求头字段（本类无字段，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n        // To change body of implemented methods use File | Settings | File\n        // Templates.\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetAllSubscriptionGroupRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_ALL_SUBSCRIPTIONGROUP_CONFIG, resource = ResourceType.GROUP, action = Action.GET)\npublic class GetAllSubscriptionGroupRequestHeader implements CommandCustomHeader {",
            "/**\n * 分页拉取全部订阅组配置的请求头：含序号、数据版本及单次最大条数。\n */\n@RocketMQAction(value = RequestCode.GET_ALL_SUBSCRIPTIONGROUP_CONFIG, resource = ResourceType.GROUP, action = Action.GET)\npublic class GetAllSubscriptionGroupRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Integer groupSeq;",
            "    /** 当前分页序号，从 0 起。 */\n    @CFNotNull\n    private Integer groupSeq;",
        ),
        (
            "    private String dataVersion;",
            "    /** 客户端已知的数据版本，用于增量同步。 */\n    private String dataVersion;",
        ),
        (
            "    private Integer maxGroupNum;",
            "    /** 单次返回的最大订阅组数量。 */\n    private Integer maxGroupNum;",
        ),
        (
            "    public Integer getGroupSeq() {",
            "    /** 返回分页序号。 */\n    public Integer getGroupSeq() {",
        ),
        (
            "    public void setGroupSeq(Integer groupSeq) {",
            "    /** 设置分页序号。 */\n    public void setGroupSeq(Integer groupSeq) {",
        ),
        (
            "    public String getDataVersion() {",
            "    /** 返回数据版本。 */\n    public String getDataVersion() {",
        ),
        (
            "    public void setDataVersion(String dataVersion) {",
            "    /** 设置数据版本。 */\n    public void setDataVersion(String dataVersion) {",
        ),
        (
            "    public Integer getMaxGroupNum() {",
            "    /** 返回单次最大条数。 */\n    public Integer getMaxGroupNum() {",
        ),
        (
            "    public void setMaxGroupNum(Integer maxGroupNum) {",
            "    /** 设置单次最大条数。 */\n    public void setMaxGroupNum(Integer maxGroupNum) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetAllSubscriptionGroupResponseHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_ALL_SUBSCRIPTIONGROUP_CONFIG, resource = ResourceType.GROUP, action = Action.LIST)\npublic class GetAllSubscriptionGroupResponseHeader implements CommandCustomHeader {",
            "/**\n * 分页拉取订阅组配置的响应头：返回集群订阅组总数。\n */\n@RocketMQAction(value = RequestCode.GET_ALL_SUBSCRIPTIONGROUP_CONFIG, resource = ResourceType.GROUP, action = Action.LIST)\npublic class GetAllSubscriptionGroupResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Integer totalGroupNum;",
            "    /** 集群订阅组总数。 */\n    @CFNotNull\n    private Integer totalGroupNum;",
        ),
        (
            "    public Integer getTotalGroupNum() {",
            "    /** 返回订阅组总数。 */\n    public Integer getTotalGroupNum() {",
        ),
        (
            "    public void setTotalGroupNum(Integer totalGroupNum) {",
            "    /** 设置订阅组总数。 */\n    public void setTotalGroupNum(Integer totalGroupNum) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetAllTopicConfigRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_ALL_TOPIC_CONFIG, resource = ResourceType.TOPIC, action = Action.GET)\npublic class GetAllTopicConfigRequestHeader implements CommandCustomHeader {",
            "/**\n * 分页拉取全部 Topic 配置的请求头：含序号、数据版本及单次最大条数。\n */\n@RocketMQAction(value = RequestCode.GET_ALL_TOPIC_CONFIG, resource = ResourceType.TOPIC, action = Action.GET)\npublic class GetAllTopicConfigRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Integer topicSeq;",
            "    /** 当前分页序号，从 0 起。 */\n    @CFNotNull\n    private Integer topicSeq;",
        ),
        (
            "    private String dataVersion;",
            "    /** 客户端已知的数据版本，用于增量同步。 */\n    private String dataVersion;",
        ),
        (
            "    private Integer maxTopicNum;",
            "    /** 单次返回的最大 Topic 数量。 */\n    private Integer maxTopicNum;",
        ),
        (
            "    public Integer getTopicSeq() {",
            "    /** 返回分页序号。 */\n    public Integer getTopicSeq() {",
        ),
        (
            "    public void setTopicSeq(Integer topicSeq) {",
            "    /** 设置分页序号。 */\n    public void setTopicSeq(Integer topicSeq) {",
        ),
        (
            "    public String getDataVersion() {",
            "    /** 返回数据版本。 */\n    public String getDataVersion() {",
        ),
        (
            "    public void setDataVersion(String dataVersion) {",
            "    /** 设置数据版本。 */\n    public void setDataVersion(String dataVersion) {",
        ),
        (
            "    public Integer getMaxTopicNum() {",
            "    /** 返回单次最大条数。 */\n    public Integer getMaxTopicNum() {",
        ),
        (
            "    public void setMaxTopicNum(Integer maxTopicNum) {",
            "    /** 设置单次最大条数。 */\n    public void setMaxTopicNum(Integer maxTopicNum) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetAllTopicConfigResponseHeader.java": [
        (
            "public class GetAllTopicConfigResponseHeader implements CommandCustomHeader {",
            "/**\n * 分页拉取 Topic 配置的响应头：返回 Broker 上 Topic 总数。\n */\npublic class GetAllTopicConfigResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    private Integer totalTopicNum;",
            "    /** Broker 上 Topic 总数。 */\n    private Integer totalTopicNum;",
        ),
        (
            "    public Integer getTotalTopicNum() {",
            "    /** 返回 Topic 总数。 */\n    public Integer getTotalTopicNum() {",
        ),
        (
            "    public void setTotalTopicNum(Integer totalTopicNum) {",
            "    /** 设置 Topic 总数。 */\n    public void setTotalTopicNum(Integer totalTopicNum) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetBrokerConfigResponseHeader.java": [
        (
            "public class GetBrokerConfigResponseHeader implements CommandCustomHeader {",
            "/**\n * 获取 Broker 配置的响应头：返回配置数据版本号字符串。\n */\npublic class GetBrokerConfigResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String version;",
            "    /** Broker 配置数据版本号。 */\n    @CFNotNull\n    private String version;",
        ),
        (
            "    public String getVersion() {",
            "    /** 返回配置版本号。 */\n    public String getVersion() {",
        ),
        (
            "    public void setVersion(String version) {",
            "    /** 设置配置版本号。 */\n    public void setVersion(String version) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetBrokerMemberGroupRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_BROKER_MEMBER_GROUP, action = Action.GET)\npublic class GetBrokerMemberGroupRequestHeader implements CommandCustomHeader {",
            "/**\n * 查询 Broker 成员组（Master/Slave 副本集）的请求头：指定集群与 Broker 名。\n */\n@RocketMQAction(value = RequestCode.GET_BROKER_MEMBER_GROUP, action = Action.GET)\npublic class GetBrokerMemberGroupRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
            "    /** 集群名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
        ),
        (
            "    @CFNotNull\n    private String brokerName;",
            "    /** Broker 逻辑名称。 */\n    @CFNotNull\n    private String brokerName;",
        ),
        (
            "    public String getClusterName() {",
            "    /** 返回集群名称。 */\n    public String getClusterName() {",
        ),
        (
            "    public void setClusterName(final String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(final String clusterName) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(final String brokerName) {",
            "    /** 设置 Broker 名称。 */\n    public void setBrokerName(final String brokerName) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetConsumeStatsInBrokerHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_BROKER_CONSUME_STATS, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetConsumeStatsInBrokerHeader implements CommandCustomHeader {",
            "/**\n * 获取 Broker 级消费统计的请求头：指定是否仅统计顺序消费。\n */\n@RocketMQAction(value = RequestCode.GET_BROKER_CONSUME_STATS, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetConsumeStatsInBrokerHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private boolean isOrder;",
            "    /** 是否仅统计顺序消费，true 表示顺序。 */\n    @CFNotNull\n    private boolean isOrder;",
        ),
        (
            "    public boolean isOrder() {",
            "    /** 返回是否顺序消费统计。 */\n    public boolean isOrder() {",
        ),
        (
            "    public void setIsOrder(boolean isOrder) {",
            "    /** 设置是否顺序消费统计。 */\n    public void setIsOrder(boolean isOrder) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetConsumeStatsRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_CONSUME_STATS, action = Action.GET)\npublic class GetConsumeStatsRequestHeader extends TopicRequestHeader {",
            "/**\n * 获取消费进度统计的请求头：指定消费组及单个或多个 Topic。\n * 若提供 topicList 则忽略 topic 单字段。\n */\n@RocketMQAction(value = RequestCode.GET_CONSUME_STATS, action = Action.GET)\npublic class GetConsumeStatsRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 单个目标 Topic（topicList 为空时生效）。 */\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    // if topicList is provided, topic will be ignored\n    @RocketMQResource(value = ResourceType.TOPIC, splitter = TOPIC_NAME_SEPARATOR)\n    private String topicList;",
            "    /** 多个 Topic，分号分隔；非空时忽略 topic 字段。 */\n    @RocketMQResource(value = ResourceType.TOPIC, splitter = TOPIC_NAME_SEPARATOR)\n    private String topicList;",
        ),
        (
            "    public List<String> fetchTopicList() {",
            "    /** 解析 topicList 为 Topic 名称列表，空则返回空列表。 */\n    public List<String> fetchTopicList() {",
        ),
        (
            "    public void updateTopicList(List<String> topicList) {",
            "    /** 以 Topic 列表更新 topicList 字符串。 */\n    public void updateTopicList(List<String> topicList) {",
        ),
        (
            "    public String getTopicList() {",
            "    /** 返回 Topic 列表字符串。 */\n    public String getTopicList() {",
        ),
        (
            "    public void setTopicList(String topicList) {",
            "    /** 设置 Topic 列表字符串。 */\n    public void setTopicList(String topicList) {",
        ),
        (
            "    public String getConsumerGroup() {",
            "    /** 返回消费组名称。 */\n    public String getConsumerGroup() {",
        ),
        (
            "    public void setConsumerGroup(String consumerGroup) {",
            "    /** 设置消费组名称。 */\n    public void setConsumerGroup(String consumerGroup) {",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回单个 Topic。 */\n    public String getTopic() {",
        ),
        (
            "    public void setTopic(String topic) {",
            "    /** 设置单个 Topic。 */\n    public void setTopic(String topic) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含消费组与 Topic 的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
}
