"""Chinese JavaDoc replacements for RocketMQ wave34a remoting protocol header [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/SearchOffsetRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.SEARCH_OFFSET_BY_TIMESTAMP, action = Action.GET)\npublic class SearchOffsetRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 按时间戳搜索队列 offset 的请求头：在指定 Topic/队列上查找最接近给定时间的消费位点。\n * boundaryType 控制取不大于（LOWER）或不小于（UPPER）目标时间的 offset。\n */\n@RocketMQAction(value = RequestCode.SEARCH_OFFSET_BY_TIMESTAMP, action = Action.GET)\npublic class SearchOffsetRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    private String liteTopic;",
            "    /** Lite Topic 名称，可为空。 */\n    private String liteTopic;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 消息队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    @CFNotNull\n    private Long timestamp;",
            "    /** 目标时间戳（毫秒）。 */\n    @CFNotNull\n    private Long timestamp;",
        ),
        (
            "    private BoundaryType boundaryType;",
            "    /** 边界类型：LOWER 取不大于目标时间的 offset，UPPER 取不小于的 offset。 */\n    private BoundaryType boundaryType;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    @Override\n    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    @Override\n    public String getTopic() {",
        ),
        (
            "    @Override\n    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    @Override\n    public void setTopic(String topic) {",
        ),
        (
            "    public String getLiteTopic() {",
            "    /** 返回 Lite Topic 名称。 */\n    public String getLiteTopic() {",
        ),
        (
            "    public void setLiteTopic(String liteTopic) {",
            "    /** 设置 Lite Topic 名称。 */\n    public void setLiteTopic(String liteTopic) {",
        ),
        (
            "    @Override\n    public Integer getQueueId() {",
            "    /** 返回队列 ID。 */\n    @Override\n    public Integer getQueueId() {",
        ),
        (
            "    @Override\n    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID。 */\n    @Override\n    public void setQueueId(Integer queueId) {",
        ),
        (
            "    public Long getTimestamp() {",
            "    /** 返回目标时间戳。 */\n    public Long getTimestamp() {",
        ),
        (
            "    public void setTimestamp(Long timestamp) {",
            "    /** 设置目标时间戳。 */\n    public void setTimestamp(Long timestamp) {",
        ),
        (
            "    public BoundaryType getBoundaryType() {\n        // default return LOWER",
            "    /** 返回边界类型，未设置时默认 LOWER。 */\n    public BoundaryType getBoundaryType() {\n        // 默认返回 LOWER",
        ),
        (
            "    public void setBoundaryType(BoundaryType boundaryType) {",
            "    /** 设置边界类型。 */\n    public void setBoundaryType(BoundaryType boundaryType) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Topic、队列、时间戳及边界类型的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/SearchOffsetResponseHeader.java": [
        (
            "public class SearchOffsetResponseHeader implements CommandCustomHeader {",
            "/**\n * 按时间戳搜索 offset 的响应头：返回匹配到的队列消费位点。\n */\npublic class SearchOffsetResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Long offset;",
            "    /** 搜索到的逻辑 offset。 */\n    @CFNotNull\n    private Long offset;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public Long getOffset() {",
            "    /** 返回搜索到的 offset。 */\n    public Long getOffset() {",
        ),
        (
            "    public void setOffset(Long offset) {",
            "    /** 设置搜索到的 offset。 */\n    public void setOffset(Long offset) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/SendMessageRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.SEND_MESSAGE, action = Action.PUB)\npublic class SendMessageRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 发送单条/批量消息的请求头：携带生产者组、Topic、队列及消息属性等元数据。\n * parseRequestHeader 可兼容 V1/V2 及批量发送请求码。\n */\n@RocketMQAction(value = RequestCode.SEND_MESSAGE, action = Action.PUB)\npublic class SendMessageRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private String producerGroup;",
            "    /** 生产者组名称。 */\n    @CFNotNull\n    private String producerGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private String defaultTopic;",
            "    /** 自动创建 Topic 时使用的默认 Topic 名。 */\n    @CFNotNull\n    private String defaultTopic;",
        ),
        (
            "    @CFNotNull\n    private Integer defaultTopicQueueNums;",
            "    /** 自动创建 Topic 时的默认队列数。 */\n    @CFNotNull\n    private Integer defaultTopicQueueNums;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 目标消息队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    @CFNotNull\n    private Integer sysFlag;",
            "    /** 系统标志位，编码消息压缩、事务等特性。 */\n    @CFNotNull\n    private Integer sysFlag;",
        ),
        (
            "    @CFNotNull\n    private Long bornTimestamp;",
            "    /** 消息 born 时间戳（毫秒）。 */\n    @CFNotNull\n    private Long bornTimestamp;",
        ),
        (
            "    @CFNotNull\n    private Integer flag;",
            "    /** 消息 flag，用于过滤表达式匹配。 */\n    @CFNotNull\n    private Integer flag;",
        ),
        (
            "    @CFNullable\n    private String properties;",
            "    /** 用户自定义属性键值对字符串，可为空。 */\n    @CFNullable\n    private String properties;",
        ),
        (
            "    @CFNullable\n    private Integer reconsumeTimes;",
            "    /** 当前重试消费次数，可为空（默认 0）。 */\n    @CFNullable\n    private Integer reconsumeTimes;",
        ),
        (
            "    @CFNullable\n    private Boolean unitMode;",
            "    /** 是否单元化模式，可为空（默认 false）。 */\n    @CFNullable\n    private Boolean unitMode;",
        ),
        (
            "    @CFNullable\n    private Boolean batch;",
            "    /** 是否为批量发送，可为空（默认 false）。 */\n    @CFNullable\n    private Boolean batch;",
        ),
        (
            "    private Integer maxReconsumeTimes;",
            "    /** 最大允许重试消费次数，可为空。 */\n    private Integer maxReconsumeTimes;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getProducerGroup() {",
            "    /** 返回生产者组名称。 */\n    public String getProducerGroup() {",
        ),
        (
            "    public void setProducerGroup(String producerGroup) {",
            "    /** 设置生产者组名称。 */\n    public void setProducerGroup(String producerGroup) {",
        ),
        (
            "    @Override\n    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    @Override\n    public String getTopic() {",
        ),
        (
            "    @Override\n    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    @Override\n    public void setTopic(String topic) {",
        ),
        (
            "    public String getDefaultTopic() {",
            "    /** 返回默认 Topic 名。 */\n    public String getDefaultTopic() {",
        ),
        (
            "    public void setDefaultTopic(String defaultTopic) {",
            "    /** 设置默认 Topic 名。 */\n    public void setDefaultTopic(String defaultTopic) {",
        ),
        (
            "    public Integer getDefaultTopicQueueNums() {",
            "    /** 返回默认队列数。 */\n    public Integer getDefaultTopicQueueNums() {",
        ),
        (
            "    public void setDefaultTopicQueueNums(Integer defaultTopicQueueNums) {",
            "    /** 设置默认队列数。 */\n    public void setDefaultTopicQueueNums(Integer defaultTopicQueueNums) {",
        ),
        (
            "    @Override\n    public Integer getQueueId() {",
            "    /** 返回队列 ID。 */\n    @Override\n    public Integer getQueueId() {",
        ),
        (
            "    @Override\n    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID。 */\n    @Override\n    public void setQueueId(Integer queueId) {",
        ),
        (
            "    public Integer getSysFlag() {",
            "    /** 返回系统标志位。 */\n    public Integer getSysFlag() {",
        ),
        (
            "    public void setSysFlag(Integer sysFlag) {",
            "    /** 设置系统标志位。 */\n    public void setSysFlag(Integer sysFlag) {",
        ),
        (
            "    public Long getBornTimestamp() {",
            "    /** 返回 born 时间戳。 */\n    public Long getBornTimestamp() {",
        ),
        (
            "    public void setBornTimestamp(Long bornTimestamp) {",
            "    /** 设置 born 时间戳。 */\n    public void setBornTimestamp(Long bornTimestamp) {",
        ),
        (
            "    public Integer getFlag() {",
            "    /** 返回消息 flag。 */\n    public Integer getFlag() {",
        ),
        (
            "    public void setFlag(Integer flag) {",
            "    /** 设置消息 flag。 */\n    public void setFlag(Integer flag) {",
        ),
        (
            "    public String getProperties() {",
            "    /** 返回用户属性字符串。 */\n    public String getProperties() {",
        ),
        (
            "    public void setProperties(String properties) {",
            "    /** 设置用户属性字符串。 */\n    public void setProperties(String properties) {",
        ),
        (
            "    public Integer getReconsumeTimes() {",
            "    /** 返回重试消费次数，空时返回 0。 */\n    public Integer getReconsumeTimes() {",
        ),
        (
            "    public void setReconsumeTimes(Integer reconsumeTimes) {",
            "    /** 设置重试消费次数。 */\n    public void setReconsumeTimes(Integer reconsumeTimes) {",
        ),
        (
            "    public boolean isUnitMode() {",
            "    /** 是否单元化模式，空时返回 false。 */\n    public boolean isUnitMode() {",
        ),
        (
            "    public void setUnitMode(Boolean isUnitMode) {",
            "    /** 设置是否单元化模式。 */\n    public void setUnitMode(Boolean isUnitMode) {",
        ),
        (
            "    public Integer getMaxReconsumeTimes() {",
            "    /** 返回最大重试消费次数。 */\n    public Integer getMaxReconsumeTimes() {",
        ),
        (
            "    public void setMaxReconsumeTimes(final Integer maxReconsumeTimes) {",
            "    /** 设置最大重试消费次数。 */\n    public void setMaxReconsumeTimes(final Integer maxReconsumeTimes) {",
        ),
        (
            "    public boolean isBatch() {",
            "    /** 是否批量发送，空时返回 false。 */\n    public boolean isBatch() {",
        ),
        (
            "    public void setBatch(Boolean batch) {",
            "    /** 设置是否批量发送。 */\n    public void setBatch(Boolean batch) {",
        ),
        (
            "    public static SendMessageRequestHeader parseRequestHeader(RemotingCommand request) throws RemotingCommandException {",
            "    /** 从 RemotingCommand 解析发送消息请求头，兼容 V1/V2 及批量码。 */\n    public static SendMessageRequestHeader parseRequestHeader(RemotingCommand request) throws RemotingCommandException {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含生产者、Topic、队列及消息属性的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/SendMessageRequestHeaderV2.java": [
        (
            "/**\n * Use short variable name to speed up FastJson deserialization process.\n */",
            "/**\n * 发送消息请求头 V2：字段名缩短以加速 FastJson 反序列化。\n * 字段 a~n 分别对应 V1 中的 producerGroup、topic、defaultTopic 等。\n */",
        ),
        (
            "    @CFNotNull\n    private String a; // producerGroup;",
            "    /** 生产者组（字段 a，对应 V1 producerGroup）。 */\n    @CFNotNull\n    private String a; // producerGroup",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String b; // topic;",
            "    /** Topic 名称（字段 b）。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String b; // topic",
        ),
        (
            "    @CFNotNull\n    private String c; // defaultTopic;",
            "    /** 默认 Topic 名（字段 c）。 */\n    @CFNotNull\n    private String c; // defaultTopic",
        ),
        (
            "    @CFNotNull\n    private Integer d; // defaultTopicQueueNums;",
            "    /** 默认队列数（字段 d）。 */\n    @CFNotNull\n    private Integer d; // defaultTopicQueueNums",
        ),
        (
            "    @CFNotNull\n    private Integer e; // queueId;",
            "    /** 队列 ID（字段 e）。 */\n    @CFNotNull\n    private Integer e; // queueId",
        ),
        (
            "    @CFNotNull\n    private Integer f; // sysFlag;",
            "    /** 系统标志位（字段 f）。 */\n    @CFNotNull\n    private Integer f; // sysFlag",
        ),
        (
            "    @CFNotNull\n    private Long g; // bornTimestamp;",
            "    /** born 时间戳（字段 g）。 */\n    @CFNotNull\n    private Long g; // bornTimestamp",
        ),
        (
            "    @CFNotNull\n    private Integer h; // flag;",
            "    /** 消息 flag（字段 h）。 */\n    @CFNotNull\n    private Integer h; // flag",
        ),
        (
            "    @CFNullable\n    private String i; // properties;",
            "    /** 用户属性（字段 i），可为空。 */\n    @CFNullable\n    private String i; // properties",
        ),
        (
            "    @CFNullable\n    private Integer j; // reconsumeTimes;",
            "    /** 重试消费次数（字段 j），可为空。 */\n    @CFNullable\n    private Integer j; // reconsumeTimes",
        ),
        (
            "    @CFNullable\n    private Boolean k; // unitMode;",
            "    /** 单元化模式（字段 k），可为空。 */\n    @CFNullable\n    private Boolean k; // unitMode",
        ),
        (
            "    private Integer l; // consumeRetryTimes",
            "    /** 最大重试次数（字段 l，对应 V1 maxReconsumeTimes）。 */\n    private Integer l; // consumeRetryTimes",
        ),
        (
            "    @CFNullable\n    private Boolean m; //batch",
            "    /** 是否批量（字段 m），可为空。 */\n    @CFNullable\n    private Boolean m; // batch",
        ),
        (
            "    @CFNullable\n    private String n; // brokerName",
            "    /** Broker 名称（字段 n），可为空。 */\n    @CFNullable\n    private String n; // brokerName",
        ),
        (
            "    public static SendMessageRequestHeader createSendMessageRequestHeaderV1(final SendMessageRequestHeaderV2 v2) {",
            "    /** 将 V2 请求头转换为 V1 格式。 */\n    public static SendMessageRequestHeader createSendMessageRequestHeaderV1(final SendMessageRequestHeaderV2 v2) {",
        ),
        (
            "    public static SendMessageRequestHeaderV2 createSendMessageRequestHeaderV2(final SendMessageRequestHeader v1) {",
            "    /** 将 V1 请求头转换为 V2 格式。 */\n    public static SendMessageRequestHeaderV2 createSendMessageRequestHeaderV2(final SendMessageRequestHeader v1) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    @Override\n    public void encode(ByteBuf out) {",
            "    /** 将短字段名键值对编码写入 ByteBuf。 */\n    @Override\n    public void encode(ByteBuf out) {",
        ),
        (
            "    @Override\n    public void decode(HashMap<String, String> fields) throws RemotingCommandException {",
            "    /** 从字段映射解码并填充各短名字段。 */\n    @Override\n    public void decode(HashMap<String, String> fields) throws RemotingCommandException {",
        ),
        (
            "    public String getA() {",
            "    /** 返回字段 a（producerGroup）。 */\n    public String getA() {",
        ),
        (
            "    public void setA(String a) {",
            "    /** 设置字段 a。 */\n    public void setA(String a) {",
        ),
        (
            "    public String getB() {",
            "    /** 返回字段 b（topic）。 */\n    public String getB() {",
        ),
        (
            "    public void setB(String b) {",
            "    /** 设置字段 b。 */\n    public void setB(String b) {",
        ),
        (
            "    public String getC() {",
            "    /** 返回字段 c（defaultTopic）。 */\n    public String getC() {",
        ),
        (
            "    public void setC(String c) {",
            "    /** 设置字段 c。 */\n    public void setC(String c) {",
        ),
        (
            "    public Integer getD() {",
            "    /** 返回字段 d（defaultTopicQueueNums）。 */\n    public Integer getD() {",
        ),
        (
            "    public void setD(Integer d) {",
            "    /** 设置字段 d。 */\n    public void setD(Integer d) {",
        ),
        (
            "    public Integer getE() {",
            "    /** 返回字段 e（queueId）。 */\n    public Integer getE() {",
        ),
        (
            "    public void setE(Integer e) {",
            "    /** 设置字段 e。 */\n    public void setE(Integer e) {",
        ),
        (
            "    public Integer getF() {",
            "    /** 返回字段 f（sysFlag）。 */\n    public Integer getF() {",
        ),
        (
            "    public void setF(Integer f) {",
            "    /** 设置字段 f。 */\n    public void setF(Integer f) {",
        ),
        (
            "    public Long getG() {",
            "    /** 返回字段 g（bornTimestamp）。 */\n    public Long getG() {",
        ),
        (
            "    public void setG(Long g) {",
            "    /** 设置字段 g。 */\n    public void setG(Long g) {",
        ),
        (
            "    public Integer getH() {",
            "    /** 返回字段 h（flag）。 */\n    public Integer getH() {",
        ),
        (
            "    public void setH(Integer h) {",
            "    /** 设置字段 h。 */\n    public void setH(Integer h) {",
        ),
        (
            "    public String getI() {",
            "    /** 返回字段 i（properties）。 */\n    public String getI() {",
        ),
        (
            "    public void setI(String i) {",
            "    /** 设置字段 i。 */\n    public void setI(String i) {",
        ),
        (
            "    public Integer getJ() {",
            "    /** 返回字段 j（reconsumeTimes）。 */\n    public Integer getJ() {",
        ),
        (
            "    public void setJ(Integer j) {",
            "    /** 设置字段 j。 */\n    public void setJ(Integer j) {",
        ),
        (
            "    public Boolean isK() {",
            "    /** 返回字段 k（unitMode）。 */\n    public Boolean isK() {",
        ),
        (
            "    public void setK(Boolean k) {",
            "    /** 设置字段 k。 */\n    public void setK(Boolean k) {",
        ),
        (
            "    public Integer getL() {",
            "    /** 返回字段 l（maxReconsumeTimes）。 */\n    public Integer getL() {",
        ),
        (
            "    public void setL(final Integer l) {",
            "    /** 设置字段 l。 */\n    public void setL(final Integer l) {",
        ),
        (
            "    public Boolean isM() {",
            "    /** 返回字段 m（batch）。 */\n    public Boolean isM() {",
        ),
        (
            "    public void setM(Boolean m) {",
            "    /** 设置字段 m。 */\n    public void setM(Boolean m) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含全部短字段的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public Integer getQueueId() {",
            "    /** 返回队列 ID（字段 e）。 */\n    @Override\n    public Integer getQueueId() {",
        ),
        (
            "    @Override\n    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID（写入字段 e）。 */\n    @Override\n    public void setQueueId(Integer queueId) {",
        ),
        (
            "    @Override\n    public String getTopic() {",
            "    /** 返回 Topic 名称（字段 b）。 */\n    @Override\n    public String getTopic() {",
        ),
        (
            "    @Override\n    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称（写入字段 b）。 */\n    @Override\n    public void setTopic(String topic) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/SendMessageResponseHeader.java": [
        (
            "public class SendMessageResponseHeader implements CommandCustomHeader, FastCodesHeader {",
            "/**\n * 发送消息的响应头：返回 msgId、队列位点及事务/批量/撤回相关标识。\n * 实现 FastCodesHeader 以支持高效编解码。\n */\npublic class SendMessageResponseHeader implements CommandCustomHeader, FastCodesHeader {",
        ),
        (
            "    @CFNotNull\n    private String msgId;",
            "    /** 服务端分配的消息唯一 ID。 */\n    @CFNotNull\n    private String msgId;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 消息写入的队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    @CFNotNull\n    private Long queueOffset;",
            "    /** 消息在队列中的逻辑 offset。 */\n    @CFNotNull\n    private Long queueOffset;",
        ),
        (
            "    private String transactionId;",
            "    /** 事务消息 ID，非事务消息可为空。 */\n    private String transactionId;",
        ),
        (
            "    private String batchUniqId;",
            "    /** 批量消息唯一标识，可为空。 */\n    private String batchUniqId;",
        ),
        (
            "    private String recallHandle;",
            "    /** 消息撤回句柄，用于后续 recall 操作，可为空。 */\n    private String recallHandle;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    @Override\n    public void encode(ByteBuf out) {",
            "    /** 将响应字段编码写入 ByteBuf。 */\n    @Override\n    public void encode(ByteBuf out) {",
        ),
        (
            "    @Override\n    public void decode(HashMap<String, String> fields) throws RemotingCommandException {",
            "    /** 从字段映射解码响应头各字段。 */\n    @Override\n    public void decode(HashMap<String, String> fields) throws RemotingCommandException {",
        ),
        (
            "    public String getMsgId() {",
            "    /** 返回消息 ID。 */\n    public String getMsgId() {",
        ),
        (
            "    public void setMsgId(String msgId) {",
            "    /** 设置消息 ID。 */\n    public void setMsgId(String msgId) {",
        ),
        (
            "    public Integer getQueueId() {",
            "    /** 返回队列 ID。 */\n    public Integer getQueueId() {",
        ),
        (
            "    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID。 */\n    public void setQueueId(Integer queueId) {",
        ),
        (
            "    public Long getQueueOffset() {",
            "    /** 返回队列 offset。 */\n    public Long getQueueOffset() {",
        ),
        (
            "    public void setQueueOffset(Long queueOffset) {",
            "    /** 设置队列 offset。 */\n    public void setQueueOffset(Long queueOffset) {",
        ),
        (
            "    public String getTransactionId() {",
            "    /** 返回事务 ID。 */\n    public String getTransactionId() {",
        ),
        (
            "    public void setTransactionId(String transactionId) {",
            "    /** 设置事务 ID。 */\n    public void setTransactionId(String transactionId) {",
        ),
        (
            "    public String getBatchUniqId() {",
            "    /** 返回批量唯一 ID。 */\n    public String getBatchUniqId() {",
        ),
        (
            "    public void setBatchUniqId(String batchUniqId) {",
            "    /** 设置批量唯一 ID。 */\n    public void setBatchUniqId(String batchUniqId) {",
        ),
        (
            "    public String getRecallHandle() {",
            "    /** 返回撤回句柄。 */\n    public String getRecallHandle() {",
        ),
        (
            "    public void setRecallHandle(String recallHandle) {",
            "    /** 设置撤回句柄。 */\n    public void setRecallHandle(String recallHandle) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/StatisticsMessagesRequestHeader.java": [
        (
            "public class StatisticsMessagesRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 统计消息数量的请求头：按消费组、Topic、队列及时间区间统计消息条数。\n * queueId 小于 0 时在 getter 中归一化为 -1（表示全部队列）。\n */\npublic class StatisticsMessagesRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private String consumerGroup;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    private String consumerGroup;",
        ),
        (
            "    @CFNotNull\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private int queueId;",
            "    /** 队列 ID，小于 0 表示全部队列。 */\n    @CFNotNull\n    private int queueId;",
        ),
        (
            "    private long fromTime;",
            "    /** 统计起始时间（毫秒）。 */\n    private long fromTime;",
        ),
        (
            "    private long toTime;",
            "    /** 统计结束时间（毫秒）。 */\n    private long toTime;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
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
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    public void setTopic(String topic) {",
        ),
        (
            "    public Integer getQueueId() {",
            "    /** 返回队列 ID，内部值小于 0 时返回 -1。 */\n    public Integer getQueueId() {",
        ),
        (
            "    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID。 */\n    public void setQueueId(Integer queueId) {",
        ),
        (
            "    public long getFromTime() {",
            "    /** 返回统计起始时间。 */\n    public long getFromTime() {",
        ),
        (
            "    public void setFromTime(long fromTime) {",
            "    /** 设置统计起始时间。 */\n    public void setFromTime(long fromTime) {",
        ),
        (
            "    public long getToTime() {",
            "    /** 返回统计结束时间。 */\n    public long getToTime() {",
        ),
        (
            "    public void setToTime(long toTime) {",
            "    /** 设置统计结束时间。 */\n    public void setToTime(long toTime) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/TriggerLiteDispatchRequestHeader.java": [
        (
            "public class TriggerLiteDispatchRequestHeader implements CommandCustomHeader {",
            "/**\n * 触发 Lite 消息派发的请求头：指定消费组及可选客户端 ID。\n * Broker 据此向 Lite 消费端推送待消费消息。\n */\npublic class TriggerLiteDispatchRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
        ),
        (
            "    private String clientId;",
            "    /** 指定客户端 ID，为空则对该组全部客户端触发。 */\n    private String clientId;",
        ),
        (
            "    public String getGroup() {",
            "    /** 返回消费组名称。 */\n    public String getGroup() {",
        ),
        (
            "    public void setGroup(String group) {",
            "    /** 设置消费组名称。 */\n    public void setGroup(String group) {",
        ),
        (
            "    public String getClientId() {",
            "    /** 返回客户端 ID。 */\n    public String getClientId() {",
        ),
        (
            "    public void setClientId(String clientId) {",
            "    /** 设置客户端 ID。 */\n    public void setClientId(String clientId) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/UnlockBatchMqRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.UNLOCK_BATCH_MQ, action = Action.SUB)\npublic class UnlockBatchMqRequestHeader extends RpcRequestHeader {",
            "/**\n * 批量解锁消息队列的请求头：消费端批量 ACK 后通知 Broker 释放队列锁。\n * 具体待解锁队列列表由 Rpc 上下文 body 携带，本头仅作权限校验。\n */\n@RocketMQAction(value = RequestCode.UNLOCK_BATCH_MQ, action = Action.SUB)\npublic class UnlockBatchMqRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/UnregisterClientRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.UNREGISTER_CLIENT, action = {Action.PUB, Action.SUB})\npublic class UnregisterClientRequestHeader extends RpcRequestHeader {",
            "/**\n * 注销客户端的请求头：Broker 移除指定 clientID 的生产者/消费者注册信息。\n * producerGroup 与 consumerGroup 可选，用于区分注销范围。\n */\n@RocketMQAction(value = RequestCode.UNREGISTER_CLIENT, action = {Action.PUB, Action.SUB})\npublic class UnregisterClientRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private String clientID;",
            "    /** 待注销的客户端唯一 ID。 */\n    @CFNotNull\n    private String clientID;",
        ),
        (
            "    @CFNullable\n    private String producerGroup;",
            "    /** 生产者组，为空则不限定生产端。 */\n    @CFNullable\n    private String producerGroup;",
        ),
        (
            "    @CFNullable\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 消费组，为空则不限定消费端。 */\n    @CFNullable\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    public String getClientID() {",
            "    /** 返回客户端 ID。 */\n    public String getClientID() {",
        ),
        (
            "    public void setClientID(String clientID) {",
            "    /** 设置客户端 ID。 */\n    public void setClientID(String clientID) {",
        ),
        (
            "    public String getProducerGroup() {",
            "    /** 返回生产者组。 */\n    public String getProducerGroup() {",
        ),
        (
            "    public void setProducerGroup(String producerGroup) {",
            "    /** 设置生产者组。 */\n    public void setProducerGroup(String producerGroup) {",
        ),
        (
            "    public String getConsumerGroup() {",
            "    /** 返回消费组。 */\n    public String getConsumerGroup() {",
        ),
        (
            "    public void setConsumerGroup(String consumerGroup) {",
            "    /** 设置消费组。 */\n    public void setConsumerGroup(String consumerGroup) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/UnregisterClientResponseHeader.java": [
        (
            "public class UnregisterClientResponseHeader implements CommandCustomHeader {",
            "/**\n * 注销客户端的响应头：无额外字段，仅表示操作完成。\n */\npublic class UnregisterClientResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/UpdateAclRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.AUTH_UPDATE_ACL, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class UpdateAclRequestHeader implements CommandCustomHeader {",
            "/**\n * 更新 ACL 配置的请求头：指定待更新的 ACL 主体（subject）。\n * 具体 ACL 规则内容由请求 body 携带。\n */\n@RocketMQAction(value = RequestCode.AUTH_UPDATE_ACL, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class UpdateAclRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String subject;",
            "    /** ACL 主体标识（如用户或资源名）。 */\n    private String subject;",
        ),
        (
            "    public UpdateAclRequestHeader() {",
            "    /** 无参构造。 */\n    public UpdateAclRequestHeader() {",
        ),
        (
            "    public UpdateAclRequestHeader(String subject) {",
            "    /** 按 ACL 主体构造。 */\n    public UpdateAclRequestHeader(String subject) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
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
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/UpdateConsumerOffsetRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.UPDATE_CONSUMER_OFFSET, action = Action.SUB)\npublic class UpdateConsumerOffsetRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 更新消费位点的请求头：消费端主动提交某队列的消费进度至 Broker。\n */\n@RocketMQAction(value = RequestCode.UPDATE_CONSUMER_OFFSET, action = Action.SUB)\npublic class UpdateConsumerOffsetRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 消息队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    @CFNotNull\n    private Long commitOffset;",
            "    /** 待提交的消费位点（逻辑 offset）。 */\n    @CFNotNull\n    private Long commitOffset;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
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
            "    @Override\n    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    @Override\n    public String getTopic() {",
        ),
        (
            "    @Override\n    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    @Override\n    public void setTopic(String topic) {",
        ),
        (
            "    @Override\n    public Integer getQueueId() {",
            "    /** 返回队列 ID。 */\n    @Override\n    public Integer getQueueId() {",
        ),
        (
            "    @Override\n    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID。 */\n    @Override\n    public void setQueueId(Integer queueId) {",
        ),
        (
            "    public Long getCommitOffset() {",
            "    /** 返回待提交位点。 */\n    public Long getCommitOffset() {",
        ),
        (
            "    public void setCommitOffset(Long commitOffset) {",
            "    /** 设置待提交位点。 */\n    public void setCommitOffset(Long commitOffset) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含消费组、Topic、队列及位点的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/UpdateConsumerOffsetResponseHeader.java": [
        (
            "public class UpdateConsumerOffsetResponseHeader implements CommandCustomHeader {",
            "/**\n * 更新消费位点的响应头：无额外字段，表示 Broker 已接受位点提交。\n */\npublic class UpdateConsumerOffsetResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/UpdateGroupForbiddenRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.UPDATE_AND_GET_GROUP_FORBIDDEN, action = Action.UPDATE)\npublic class UpdateGroupForbiddenRequestHeader extends TopicRequestHeader {",
            "/**\n * 更新并查询消费组禁读状态的请求头：设置某消费组对 Topic 的可读权限。\n * readable 为 false 时禁止该组消费指定 Topic。\n */\n@RocketMQAction(value = RequestCode.UPDATE_AND_GET_GROUP_FORBIDDEN, action = Action.UPDATE)\npublic class UpdateGroupForbiddenRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String  group;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String  group;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String  topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String  topic;",
        ),
        (
            "    private Boolean readable;",
            "    /** 是否允许读取，false 表示禁读，可为空。 */\n    private Boolean readable;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
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
            "    public String getGroup() {",
            "    /** 返回消费组名称。 */\n    public String getGroup() {",
        ),
        (
            "    public void setGroup(String group) {",
            "    /** 设置消费组名称。 */\n    public void setGroup(String group) {",
        ),
        (
            "    public Boolean getReadable() {",
            "    /** 返回是否可读。 */\n    public Boolean getReadable() {",
        ),
        (
            "    public void setReadable(Boolean readable) {",
            "    /** 设置是否可读。 */\n    public void setReadable(Boolean readable) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/UpdateUserRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.AUTH_UPDATE_USER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class UpdateUserRequestHeader implements CommandCustomHeader {",
            "/**\n * 更新用户信息的请求头：指定待更新的用户名。\n * 具体用户凭证与权限由请求 body 携带。\n */\n@RocketMQAction(value = RequestCode.AUTH_UPDATE_USER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class UpdateUserRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String username;",
            "    /** 待更新的用户名。 */\n    private String username;",
        ),
        (
            "    public UpdateUserRequestHeader() {",
            "    /** 无参构造。 */\n    public UpdateUserRequestHeader() {",
        ),
        (
            "    public UpdateUserRequestHeader(String username) {",
            "    /** 按用户名构造。 */\n    public UpdateUserRequestHeader(String username) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getUsername() {",
            "    /** 返回用户名。 */\n    public String getUsername() {",
        ),
        (
            "    public void setUsername(String username) {",
            "    /** 设置用户名。 */\n    public void setUsername(String username) {",
        ),
    ],
}
