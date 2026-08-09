"""Chinese JavaDoc replacements for RocketMQ wave36a remoting route/statictopic/subscription [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/route/QueueData.java": [
        (
            "public class QueueData implements Comparable<QueueData> {",
            "/**\n * Topic 在某 Broker 上的队列元数据：读写队列数、权限位与系统标志。\n * 按 brokerName 字典序实现 {@link Comparable}。\n */\npublic class QueueData implements Comparable<QueueData> {",
        ),
        (
            "    private String brokerName;",
            "    /** 承载该 Topic 队列的 Broker 名称。 */\n    private String brokerName;",
        ),
        (
            "    private int readQueueNums;",
            "    /** 可读队列数量。 */\n    private int readQueueNums;",
        ),
        (
            "    private int writeQueueNums;",
            "    /** 可写队列数量。 */\n    private int writeQueueNums;",
        ),
        (
            "    private int perm;",
            "    /** Topic 在该 Broker 上的权限位（读/写/继承等）。 */\n    private int perm;",
        ),
        (
            "    private int topicSysFlag;",
            "    /** Topic 系统标志位。 */\n    private int topicSysFlag;",
        ),
        (
            "    // Deep copy QueueData\n    public QueueData(QueueData queueData) {",
            "    /** 深拷贝构造：复制源 QueueData 的全部字段。 */\n    public QueueData(QueueData queueData) {",
        ),
        (
            "    public int getReadQueueNums() {",
            "    /** 返回可读队列数。 */\n    public int getReadQueueNums() {",
        ),
        (
            "    public void setReadQueueNums(int readQueueNums) {",
            "    /** 设置可读队列数。 */\n    public void setReadQueueNums(int readQueueNums) {",
        ),
        (
            "    public int getWriteQueueNums() {",
            "    /** 返回可写队列数。 */\n    public int getWriteQueueNums() {",
        ),
        (
            "    public void setWriteQueueNums(int writeQueueNums) {",
            "    /** 设置可写队列数。 */\n    public void setWriteQueueNums(int writeQueueNums) {",
        ),
        (
            "    public int getPerm() {",
            "    /** 返回权限位。 */\n    public int getPerm() {",
        ),
        (
            "    public void setPerm(int perm) {",
            "    /** 设置权限位。 */\n    public void setPerm(int perm) {",
        ),
        (
            "    public int getTopicSysFlag() {",
            "    /** 返回 Topic 系统标志。 */\n    public int getTopicSysFlag() {",
        ),
        (
            "    public void setTopicSysFlag(int topicSysFlag) {",
            "    /** 设置 Topic 系统标志。 */\n    public void setTopicSysFlag(int topicSysFlag) {",
        ),
        (
            "    public int compareTo(QueueData o) {",
            "    /** 按 brokerName 字典序比较。 */\n    public int compareTo(QueueData o) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/route/TopicRouteData.java": [
        (
            "public class TopicRouteData extends RemotingSerializable {",
            "/**\n * Topic 路由数据：队列分布、Broker 地址、Filter Server 与静态 Topic 队列映射。\n * 可序列化并通过 NameServer 下发给客户端。\n */\npublic class TopicRouteData extends RemotingSerializable {",
        ),
        (
            "    private String orderTopicConf;",
            "    /** 顺序 Topic 配置串（可为空）。 */\n    private String orderTopicConf;",
        ),
        (
            "    private List<QueueData> queueDatas;",
            "    /** 各 Broker 上的队列元数据列表。 */\n    private List<QueueData> queueDatas;",
        ),
        (
            "    private List<BrokerData> brokerDatas;",
            "    /** Broker 地址与角色信息列表。 */\n    private List<BrokerData> brokerDatas;",
        ),
        (
            "    private HashMap<String/* brokerAddr */, List<String>/* Filter Server */> filterServerTable;",
            "    /** Broker 地址到 Filter Server 列表的映射。 */\n    private HashMap<String/* brokerAddr */, List<String>/* Filter Server */> filterServerTable;",
        ),
        (
            "    //It could be null or empty\n    private Map<String/*brokerName*/, TopicQueueMappingInfo> topicQueueMappingByBroker;",
            "    /** 按 Broker 名称索引的静态 Topic 队列映射（可为 null 或空）。 */\n    private Map<String/*brokerName*/, TopicQueueMappingInfo> topicQueueMappingByBroker;",
        ),
        (
            "    public TopicRouteData cloneTopicRouteData() {",
            "    /** 浅克隆：复制集合引用与映射表，不递归克隆元素对象。 */\n    public TopicRouteData cloneTopicRouteData() {",
        ),
        (
            "    public TopicRouteData deepCloneTopicRouteData() {",
            "    /** 深克隆：递归复制 QueueData、BrokerData、Filter Server 与队列映射。 */\n    public TopicRouteData deepCloneTopicRouteData() {",
        ),
        (
            "    public boolean topicRouteDataChanged(TopicRouteData oldData) {",
            "    /** 比较与旧路由是否发生变化（排序后比较队列与 Broker 列表）。 */\n    public boolean topicRouteDataChanged(TopicRouteData oldData) {",
        ),
        (
            "    public List<QueueData> getQueueDatas() {",
            "    /** 返回队列元数据列表。 */\n    public List<QueueData> getQueueDatas() {",
        ),
        (
            "    public void setQueueDatas(List<QueueData> queueDatas) {",
            "    /** 设置队列元数据列表。 */\n    public void setQueueDatas(List<QueueData> queueDatas) {",
        ),
        (
            "    public List<BrokerData> getBrokerDatas() {",
            "    /** 返回 Broker 数据列表。 */\n    public List<BrokerData> getBrokerDatas() {",
        ),
        (
            "    public void setBrokerDatas(List<BrokerData> brokerDatas) {",
            "    /** 设置 Broker 数据列表。 */\n    public void setBrokerDatas(List<BrokerData> brokerDatas) {",
        ),
        (
            "    public HashMap<String, List<String>> getFilterServerTable() {",
            "    /** 返回 Filter Server 映射表。 */\n    public HashMap<String, List<String>> getFilterServerTable() {",
        ),
        (
            "    public void setFilterServerTable(HashMap<String, List<String>> filterServerTable) {",
            "    /** 设置 Filter Server 映射表。 */\n    public void setFilterServerTable(HashMap<String, List<String>> filterServerTable) {",
        ),
        (
            "    public String getOrderTopicConf() {",
            "    /** 返回顺序 Topic 配置。 */\n    public String getOrderTopicConf() {",
        ),
        (
            "    public void setOrderTopicConf(String orderTopicConf) {",
            "    /** 设置顺序 Topic 配置。 */\n    public void setOrderTopicConf(String orderTopicConf) {",
        ),
        (
            "    public Map<String, TopicQueueMappingInfo> getTopicQueueMappingByBroker() {",
            "    /** 返回按 Broker 索引的静态队列映射。 */\n    public Map<String, TopicQueueMappingInfo> getTopicQueueMappingByBroker() {",
        ),
        (
            "    public void setTopicQueueMappingByBroker(Map<String, TopicQueueMappingInfo> topicQueueMappingByBroker) {",
            "    /** 设置按 Broker 索引的静态队列映射。 */\n    public void setTopicQueueMappingByBroker(Map<String, TopicQueueMappingInfo> topicQueueMappingByBroker) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/statictopic/LogicQueueMappingItem.java": [
        (
            "public class LogicQueueMappingItem extends RemotingSerializable {",
            "/**\n * 静态 Topic 逻辑队列映射项：描述逻辑偏移与物理偏移的对应区间。\n * 用于逻辑队列 ID 到物理 Broker 队列的偏移换算。\n */\npublic class LogicQueueMappingItem extends RemotingSerializable {",
        ),
        (
            "    private int gen; // immutable",
            "    /** 映射代数（不可变）。 */\n    private int gen; // immutable",
        ),
        (
            "    private int queueId; //, immutable",
            "    /** 物理队列 ID（不可变）。 */\n    private int queueId; //, immutable",
        ),
        (
            "    private String bname; //important, immutable",
            "    /** 承载 Broker 名称（重要，不可变）。 */\n    private String bname; //important, immutable",
        ),
        (
            "    private long logicOffset; // the start of the logic offset, important, can be changed by command only once",
            "    /** 逻辑偏移起点（重要，仅可通过命令修改一次）。 */\n    private long logicOffset; // the start of the logic offset, important, can be changed by command only once",
        ),
        (
            "    private long startOffset; // the start of the physical offset, should always be 0, immutable",
            "    /** 物理偏移起点（通常恒为 0，不可变）。 */\n    private long startOffset; // the start of the physical offset, should always be 0, immutable",
        ),
        (
            "    private long endOffset = -1; // the end of the physical offset, excluded, revered -1, mutable",
            "    /** 物理偏移终点（不含，-1 表示未定，可变）。 */\n    private long endOffset = -1; // the end of the physical offset, excluded, revered -1, mutable",
        ),
        (
            "    //should only be user in sendMessage and getMinOffset\n    public long computeStaticQueueOffsetLoosely(long physicalQueueOffset) {",
            "    /** 宽松计算静态队列偏移（sendMessage/getMinOffset 场景，考虑未闭合区间）。 */\n    public long computeStaticQueueOffsetLoosely(long physicalQueueOffset) {",
        ),
        (
            "    public long computeStaticQueueOffsetStrictly(long physicalQueueOffset) {",
            "    /** 严格计算静态队列偏移（要求 logicOffset 已确定）。 */\n    public long computeStaticQueueOffsetStrictly(long physicalQueueOffset) {",
        ),
        (
            "    public long computePhysicalQueueOffset(long staticQueueOffset) {",
            "    /** 由静态队列偏移反算物理队列偏移。 */\n    public long computePhysicalQueueOffset(long staticQueueOffset) {",
        ),
        (
            "    public long computeMaxStaticQueueOffset() {",
            "    /** 计算本映射项覆盖的最大静态队列偏移。 */\n    public long computeMaxStaticQueueOffset() {",
        ),
        (
            "    public boolean checkIfEndOffsetDecided() {",
            "    /** 判断物理 endOffset 是否已确定（endOffset > startOffset）。 */\n    public boolean checkIfEndOffsetDecided() {",
        ),
        (
            "    public boolean checkIfLogicoffsetDecided() {",
            "    /** 判断逻辑偏移是否已确定（logicOffset >= 0）。 */\n    public boolean checkIfLogicoffsetDecided() {",
        ),
        (
            "    public long computeOffsetDelta() {",
            "    /** 返回逻辑偏移与物理起点的差值。 */\n    public long computeOffsetDelta() {",
        ),
        (
            "    public int getGen() {",
            "    /** 返回映射代数。 */\n    public int getGen() {",
        ),
        (
            "    public int getQueueId() {",
            "    /** 返回物理队列 ID。 */\n    public int getQueueId() {",
        ),
        (
            "    public String getBname() {",
            "    /** 返回 Broker 名称。 */\n    public String getBname() {",
        ),
        (
            "    public long getLogicOffset() {",
            "    /** 返回逻辑偏移起点。 */\n    public long getLogicOffset() {",
        ),
        (
            "    public void setLogicOffset(long logicOffset) {",
            "    /** 设置逻辑偏移起点。 */\n    public void setLogicOffset(long logicOffset) {",
        ),
        (
            "    public void setEndOffset(long endOffset) {",
            "    /** 设置物理偏移终点。 */\n    public void setEndOffset(long endOffset) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/statictopic/TopicConfigAndQueueMapping.java": [
        (
            "public class TopicConfigAndQueueMapping extends TopicConfig {",
            "/**\n * Topic 配置与静态队列映射的组合体：继承 {@link TopicConfig} 并附加映射详情。\n */\npublic class TopicConfigAndQueueMapping extends TopicConfig {",
        ),
        (
            "    private TopicQueueMappingDetail mappingDetail;",
            "    /** 静态 Topic 队列映射详情。 */\n    private TopicQueueMappingDetail mappingDetail;",
        ),
        (
            "    public TopicConfigAndQueueMapping(TopicConfig topicConfig, TopicQueueMappingDetail mappingDetail) {",
            "    /** 由 Topic 配置与映射详情构造。 */\n    public TopicConfigAndQueueMapping(TopicConfig topicConfig, TopicQueueMappingDetail mappingDetail) {",
        ),
        (
            "    public TopicQueueMappingDetail getMappingDetail() {",
            "    /** 返回队列映射详情。 */\n    public TopicQueueMappingDetail getMappingDetail() {",
        ),
        (
            "    public void setMappingDetail(TopicQueueMappingDetail mappingDetail) {",
            "    /** 设置队列映射详情。 */\n    public void setMappingDetail(TopicQueueMappingDetail mappingDetail) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/statictopic/TopicQueueMappingContext.java": [
        (
            "public class TopicQueueMappingContext  {",
            "/**\n * 静态 Topic 队列映射上下文：聚合 Topic、全局 ID、映射详情与映射项列表。\n * 用于 Leader 判定与当前映射项定位。\n */\npublic class TopicQueueMappingContext  {",
        ),
        (
            "    private String topic;",
            "    /** Topic 名称。 */\n    private String topic;",
        ),
        (
            "    private Integer globalId;",
            "    /** 逻辑队列全局 ID。 */\n    private Integer globalId;",
        ),
        (
            "    private TopicQueueMappingDetail mappingDetail;",
            "    /** 当前 Broker 上的映射详情。 */\n    private TopicQueueMappingDetail mappingDetail;",
        ),
        (
            "    private List<LogicQueueMappingItem> mappingItemList;",
            "    /** 逻辑队列映射项列表。 */\n    private List<LogicQueueMappingItem> mappingItemList;",
        ),
        (
            "    private LogicQueueMappingItem leaderItem;",
            "    /** 当前 Leader 映射项。 */\n    private LogicQueueMappingItem leaderItem;",
        ),
        (
            "    private LogicQueueMappingItem currentItem;",
            "    /** 当前正在使用的映射项。 */\n    private LogicQueueMappingItem currentItem;",
        ),
        (
            "    public boolean isLeader() {",
            "    /** 判断当前 Broker 是否为该逻辑队列的 Leader。 */\n    public boolean isLeader() {",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public Integer getGlobalId() {",
            "    /** 返回全局逻辑队列 ID。 */\n    public Integer getGlobalId() {",
        ),
        (
            "    public TopicQueueMappingDetail getMappingDetail() {",
            "    /** 返回映射详情。 */\n    public TopicQueueMappingDetail getMappingDetail() {",
        ),
        (
            "    public List<LogicQueueMappingItem> getMappingItemList() {",
            "    /** 返回映射项列表。 */\n    public List<LogicQueueMappingItem> getMappingItemList() {",
        ),
        (
            "    public LogicQueueMappingItem getLeaderItem() {",
            "    /** 返回 Leader 映射项。 */\n    public LogicQueueMappingItem getLeaderItem() {",
        ),
        (
            "    public LogicQueueMappingItem getCurrentItem() {",
            "    /** 返回当前映射项。 */\n    public LogicQueueMappingItem getCurrentItem() {",
        ),
        (
            "    public void setCurrentItem(LogicQueueMappingItem currentItem) {",
            "    /** 设置当前映射项。 */\n    public void setCurrentItem(LogicQueueMappingItem currentItem) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/statictopic/TopicQueueMappingDetail.java": [
        (
            "public class TopicQueueMappingDetail extends TopicQueueMappingInfo {",
            "/**\n * 静态 Topic 队列映射详情：在 {@link TopicQueueMappingInfo} 基础上\n * 持有当前 Broker 托管的逻辑队列映射项（不注册到 NameServer）。\n */\npublic class TopicQueueMappingDetail extends TopicQueueMappingInfo {",
        ),
        (
            "    // the mapping info in current broker, do not register to nameserver\n    // make sure this value is not null\n    private ConcurrentMap<Integer/*global id*/, List<LogicQueueMappingItem>> hostedQueues = new ConcurrentHashMap<>();",
            "    /** 当前 Broker 托管的全局 ID 到映射项列表（不注册 NameServer，非 null）。 */\n    private ConcurrentMap<Integer/*global id*/, List<LogicQueueMappingItem>> hostedQueues = new ConcurrentHashMap<>();",
        ),
        (
            "    public static boolean putMappingInfo(TopicQueueMappingDetail mappingDetail, Integer globalId, List<LogicQueueMappingItem> mappingInfo) {",
            "    /** 向映射详情写入指定 globalId 的映射项列表。 */\n    public static boolean putMappingInfo(TopicQueueMappingDetail mappingDetail, Integer globalId, List<LogicQueueMappingItem> mappingInfo) {",
        ),
        (
            "    public static List<LogicQueueMappingItem> getMappingInfo(TopicQueueMappingDetail mappingDetail, Integer globalId) {",
            "    /** 读取指定 globalId 的映射项列表。 */\n    public static List<LogicQueueMappingItem> getMappingInfo(TopicQueueMappingDetail mappingDetail, Integer globalId) {",
        ),
        (
            "    public static ConcurrentMap<Integer, Integer> buildIdMap(TopicQueueMappingDetail mappingDetail, int level) {",
            "    /** 构建 globalId 到物理 queueId 的映射（level 0 表示当前 Leader）。 */\n    public static ConcurrentMap<Integer, Integer> buildIdMap(TopicQueueMappingDetail mappingDetail, int level) {",
        ),
        (
            "    public static long computeMaxOffsetFromMapping(TopicQueueMappingDetail mappingDetail, Integer globalId) {",
            "    /** 根据映射计算指定 globalId 的最大静态队列偏移。 */\n    public static long computeMaxOffsetFromMapping(TopicQueueMappingDetail mappingDetail, Integer globalId) {",
        ),
        (
            "    public static TopicQueueMappingInfo cloneAsMappingInfo(TopicQueueMappingDetail mappingDetail) {",
            "    /** 克隆为可注册到 NameServer 的 {@link TopicQueueMappingInfo}。 */\n    public static TopicQueueMappingInfo cloneAsMappingInfo(TopicQueueMappingDetail mappingDetail) {",
        ),
        (
            "    public static boolean checkIfAsPhysical(TopicQueueMappingDetail mappingDetail, Integer globalId) {",
            "    /** 判断该 globalId 是否可按物理队列处理（无映射或 logicOffset 为 0）。 */\n    public static boolean checkIfAsPhysical(TopicQueueMappingDetail mappingDetail, Integer globalId) {",
        ),
        (
            "    public ConcurrentMap<Integer, List<LogicQueueMappingItem>> getHostedQueues() {",
            "    /** 返回托管队列映射表。 */\n    public ConcurrentMap<Integer, List<LogicQueueMappingItem>> getHostedQueues() {",
        ),
        (
            "    public void setHostedQueues(ConcurrentMap<Integer, List<LogicQueueMappingItem>> hostedQueues) {",
            "    /** 设置托管队列映射表。 */\n    public void setHostedQueues(ConcurrentMap<Integer, List<LogicQueueMappingItem>> hostedQueues) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/statictopic/TopicQueueMappingInfo.java": [
        (
            "public class TopicQueueMappingInfo extends RemotingSerializable {",
            "/**\n * 静态 Topic 队列映射摘要：注册到 Broker/NameServer 用于构建路由。\n * 含 epoch 防脏读、逻辑到物理 queueId 映射等元数据。\n */\npublic class TopicQueueMappingInfo extends RemotingSerializable {",
        ),
        (
            "    public static final int LEVEL_0 = 0;",
            "    /** 映射层级 0：当前 Broker 上的 Leader。 */\n    public static final int LEVEL_0 = 0;",
        ),
        (
            "    String topic; // redundant field",
            "    /** Topic 名称（冗余字段）。 */\n    String topic; // redundant field",
        ),
        (
            "    String scope = MixAll.METADATA_SCOPE_GLOBAL;",
            "    /** 元数据作用域，默认全局。 */\n    String scope = MixAll.METADATA_SCOPE_GLOBAL;",
        ),
        (
            "    int totalQueues;",
            "    /** 逻辑队列总数。 */\n    int totalQueues;",
        ),
        (
            "    String bname;  //identify the hosted broker name",
            "    /** 托管 Broker 名称。 */\n    String bname;  //identify the hosted broker name",
        ),
        (
            "    long epoch; //important to fence the old dirty data",
            "    /** 映射 epoch，用于隔离旧脏数据。 */\n    long epoch; //important to fence the old dirty data",
        ),
        (
            "    boolean dirty; //indicate if the data is dirty",
            "    /** 是否为脏数据标记。 */\n    boolean dirty; //indicate if the data is dirty",
        ),
        (
            "    //register to broker to construct the route\n    protected ConcurrentMap<Integer/*logicId*/, Integer/*physicalId*/> currIdMap = new ConcurrentHashMap<>();",
            "    /** 逻辑 queueId 到物理 queueId 的当前映射（用于构建路由）。 */\n    protected ConcurrentMap<Integer/*logicId*/, Integer/*physicalId*/> currIdMap = new ConcurrentHashMap<>();",
        ),
        (
            "    public boolean isDirty() {",
            "    /** 返回是否为脏数据。 */\n    public boolean isDirty() {",
        ),
        (
            "    public void setDirty(boolean dirty) {",
            "    /** 设置脏数据标记。 */\n    public void setDirty(boolean dirty) {",
        ),
        (
            "    public int getTotalQueues() {",
            "    /** 返回逻辑队列总数。 */\n    public int getTotalQueues() {",
        ),
        (
            "    public String getBname() {",
            "    /** 返回托管 Broker 名称。 */\n    public String getBname() {",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public long getEpoch() {",
            "    /** 返回映射 epoch。 */\n    public long getEpoch() {",
        ),
        (
            "    public ConcurrentMap<Integer, Integer> getCurrIdMap() {",
            "    /** 返回逻辑到物理 queueId 映射。 */\n    public ConcurrentMap<Integer, Integer> getCurrIdMap() {",
        ),
        (
            "    public String getScope() {",
            "    /** 返回元数据作用域。 */\n    public String getScope() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/statictopic/TopicQueueMappingOne.java": [
        (
            "public class TopicQueueMappingOne extends RemotingSerializable {",
            "/**\n * 单个逻辑队列的静态映射视图：绑定 Topic、Broker、globalId 与映射项链。\n */\npublic class TopicQueueMappingOne extends RemotingSerializable {",
        ),
        (
            "    String topic; // redundant field",
            "    /** Topic 名称（冗余字段）。 */\n    String topic; // redundant field",
        ),
        (
            "    String bname;  //identify the hosted broker name",
            "    /** 托管 Broker 名称。 */\n    String bname;  //identify the hosted broker name",
        ),
        (
            "    Integer globalId;",
            "    /** 逻辑队列全局 ID。 */\n    Integer globalId;",
        ),
        (
            "    List<LogicQueueMappingItem> items;",
            "    /** 该逻辑队列的映射项链。 */\n    List<LogicQueueMappingItem> items;",
        ),
        (
            "    TopicQueueMappingDetail mappingDetail;",
            "    /** 所属映射详情。 */\n    TopicQueueMappingDetail mappingDetail;",
        ),
        (
            "    public TopicQueueMappingOne(TopicQueueMappingDetail mappingDetail, String topic, String bname, Integer globalId, List<LogicQueueMappingItem> items) {",
            "    /** 构造单队列映射视图。 */\n    public TopicQueueMappingOne(TopicQueueMappingDetail mappingDetail, String topic, String bname, Integer globalId, List<LogicQueueMappingItem> items) {",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public String getBname() {",
            "    /** 返回 Broker 名称。 */\n    public String getBname() {",
        ),
        (
            "    public Integer getGlobalId() {",
            "    /** 返回全局逻辑队列 ID。 */\n    public Integer getGlobalId() {",
        ),
        (
            "    public List<LogicQueueMappingItem> getItems() {",
            "    /** 返回映射项列表。 */\n    public List<LogicQueueMappingItem> getItems() {",
        ),
        (
            "    public TopicQueueMappingDetail getMappingDetail() {",
            "    /** 返回映射详情。 */\n    public TopicQueueMappingDetail getMappingDetail() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/statictopic/TopicRemappingDetailWrapper.java": [
        (
            "public class TopicRemappingDetailWrapper extends RemotingSerializable {",
            "/**\n * Topic 重映射操作包装：携带 epoch、Broker 配置变更与迁入/迁出 Broker 集合。\n * 支持 CREATE_OR_UPDATE 与 REMAPPING 两种操作类型。\n */\npublic class TopicRemappingDetailWrapper extends RemotingSerializable {",
        ),
        (
            "    public static final String TYPE_CREATE_OR_UPDATE = \"CREATE_OR_UPDATE\";",
            "    /** 操作类型：创建或更新映射。 */\n    public static final String TYPE_CREATE_OR_UPDATE = \"CREATE_OR_UPDATE\";",
        ),
        (
            "    public static final String TYPE_REMAPPING = \"REMAPPING\";",
            "    /** 操作类型：重映射（迁移逻辑队列）。 */\n    public static final String TYPE_REMAPPING = \"REMAPPING\";",
        ),
        (
            "    public static final String SUFFIX_BEFORE = \".before\";",
            "    /** 重映射前配置键后缀。 */\n    public static final String SUFFIX_BEFORE = \".before\";",
        ),
        (
            "    public static final String SUFFIX_AFTER = \".after\";",
            "    /** 重映射后配置键后缀。 */\n    public static final String SUFFIX_AFTER = \".after\";",
        ),
        (
            "    private String topic;",
            "    /** Topic 名称。 */\n    private String topic;",
        ),
        (
            "    private String type;",
            "    /** 操作类型（CREATE_OR_UPDATE 或 REMAPPING）。 */\n    private String type;",
        ),
        (
            "    private long epoch;",
            "    /** 映射 epoch，用于版本隔离。 */\n    private long epoch;",
        ),
        (
            "    private Map<String, TopicConfigAndQueueMapping> brokerConfigMap = new HashMap<>();",
            "    /** Broker 名称到 Topic 配置与映射的组合映射。 */\n    private Map<String, TopicConfigAndQueueMapping> brokerConfigMap = new HashMap<>();",
        ),
        (
            "    private Set<String> brokerToMapIn = new HashSet<>();",
            "    /** 需要迁入映射的 Broker 集合。 */\n    private Set<String> brokerToMapIn = new HashSet<>();",
        ),
        (
            "    private Set<String> brokerToMapOut = new HashSet<>();",
            "    /** 需要迁出映射的 Broker 集合。 */\n    private Set<String> brokerToMapOut = new HashSet<>();",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public String getType() {",
            "    /** 返回操作类型。 */\n    public String getType() {",
        ),
        (
            "    public long getEpoch() {",
            "    /** 返回映射 epoch。 */\n    public long getEpoch() {",
        ),
        (
            "    public Map<String, TopicConfigAndQueueMapping> getBrokerConfigMap() {",
            "    /** 返回 Broker 配置映射。 */\n    public Map<String, TopicConfigAndQueueMapping> getBrokerConfigMap() {",
        ),
        (
            "    public Set<String> getBrokerToMapIn() {",
            "    /** 返回迁入 Broker 集合。 */\n    public Set<String> getBrokerToMapIn() {",
        ),
        (
            "    public Set<String> getBrokerToMapOut() {",
            "    /** 返回迁出 Broker 集合。 */\n    public Set<String> getBrokerToMapOut() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/subscription/CustomizedRetryPolicy.java": [
        (
            "/**\n * CustomizedRetryPolicy is aim to make group's behavior compatible with messageDelayLevel\n *\n * @see <a href=\"https://github.com/apache/rocketmq/blob/3bd4b2b2f61a824196f19b03146e2c929c62777b/store/src/main/java/org/apache/rocketmq/store/config/MessageStoreConfig.java#L137\">org.apache.rocketmq.store.config.MessageStoreConfig</a>\n */",
            "/**\n * 自定义重试策略：与 messageDelayLevel 延迟级别表兼容，供消费组按固定阶梯延迟重投。\n *\n * @see <a href=\"https://github.com/apache/rocketmq/blob/3bd4b2b2f61a824196f19b03146e2c929c62777b/store/src/main/java/org/apache/rocketmq/store/config/MessageStoreConfig.java#L137\">org.apache.rocketmq.store.config.MessageStoreConfig</a>\n */",
        ),
        (
            "    // 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h\n    private long[] next = new long[] {",
            "    /** 默认延迟阶梯（毫秒）：1s 5s 10s … 2h，与 messageDelayLevel 对齐。 */\n    private long[] next = new long[] {",
        ),
        (
            "    public CustomizedRetryPolicy(long[] next) {",
            "    /** 使用自定义延迟数组构造。 */\n    public CustomizedRetryPolicy(long[] next) {",
        ),
        (
            "    public long[] getNext() {",
            "    /** 返回延迟阶梯数组。 */\n    public long[] getNext() {",
        ),
        (
            "    public void setNext(long[] next) {",
            "    /** 设置延迟阶梯数组。 */\n    public void setNext(long[] next) {",
        ),
        (
            "    /**\n     * Index = reconsumeTimes + 2 is compatible logic, cause old delayLevelTable starts from index 1,\n     * and old index is reconsumeTime + 3\n     *\n     * @param reconsumeTimes Message reconsumeTimes {@link org.apache.rocketmq.common.message.MessageExt#getReconsumeTimes}\n     * @see <a href=\"https://github.com/apache/rocketmq/blob/3bddd514646826253a239f95959c14840a87034a/broker/src/main/java/org/apache/rocketmq/broker/processor/AbstractSendMessageProcessor.java#L210\">org.apache.rocketmq.broker.processor.AbstractSendMessageProcessor</a>\n     * @see <a href=\"https://github.com/apache/rocketmq/blob/3bddd514646826253a239f95959c14840a87034a/store/src/main/java/org/apache/rocketmq/store/DefaultMessageStore.java#L242\">org.apache.rocketmq.store.DefaultMessageStore</a>\n     */",
            "    /**\n     * 按重试次数计算下次延迟：index = reconsumeTimes + 2，与旧 delayLevelTable 索引兼容。\n     *\n     * @param reconsumeTimes Message reconsumeTimes {@link org.apache.rocketmq.common.message.MessageExt#getReconsumeTimes}\n     * @see <a href=\"https://github.com/apache/rocketmq/blob/3bddd514646826253a239f95959c14840a87034a/broker/src/main/java/org/apache/rocketmq/broker/processor/AbstractSendMessageProcessor.java#L210\">org.apache.rocketmq.broker.processor.AbstractSendMessageProcessor</a>\n     * @see <a href=\"https://github.com/apache/rocketmq/blob/3bddd514646826253a239f95959c14840a87034a/store/src/main/java/org/apache/rocketmq/store/DefaultMessageStore.java#L242\">org.apache.rocketmq.store.DefaultMessageStore</a>\n     */",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/subscription/ExponentialRetryPolicy.java": [
        (
            "public class ExponentialRetryPolicy implements RetryPolicy {",
            "/**\n * 指数退避重试策略：初始延迟按 multiplier 指数增长，上限为 max。\n * 实现 {@link RetryPolicy} 供消费组配置。\n */\npublic class ExponentialRetryPolicy implements RetryPolicy {",
        ),
        (
            "    private long initial = TimeUnit.SECONDS.toMillis(5);",
            "    /** 首次重试延迟（毫秒），默认 5 秒。 */\n    private long initial = TimeUnit.SECONDS.toMillis(5);",
        ),
        (
            "    private long max = TimeUnit.HOURS.toMillis(2);",
            "    /** 最大重试延迟（毫秒），默认 2 小时。 */\n    private long max = TimeUnit.HOURS.toMillis(2);",
        ),
        (
            "    private long multiplier = 2;",
            "    /** 指数乘数，默认 2。 */\n    private long multiplier = 2;",
        ),
        (
            "    public ExponentialRetryPolicy(long initial, long max, long multiplier) {",
            "    /** 指定初始延迟、上限与乘数构造。 */\n    public ExponentialRetryPolicy(long initial, long max, long multiplier) {",
        ),
        (
            "    public long getInitial() {",
            "    /** 返回初始延迟。 */\n    public long getInitial() {",
        ),
        (
            "    public void setInitial(long initial) {",
            "    /** 设置初始延迟。 */\n    public void setInitial(long initial) {",
        ),
        (
            "    public long getMax() {",
            "    /** 返回最大延迟。 */\n    public long getMax() {",
        ),
        (
            "    public void setMax(long max) {",
            "    /** 设置最大延迟。 */\n    public void setMax(long max) {",
        ),
        (
            "    public long getMultiplier() {",
            "    /** 返回指数乘数。 */\n    public long getMultiplier() {",
        ),
        (
            "    public void setMultiplier(long multiplier) {",
            "    /** 设置指数乘数。 */\n    public void setMultiplier(long multiplier) {",
        ),
        (
            "    public long nextDelayDuration(int reconsumeTimes) {",
            "    /** 按重试次数计算下次延迟：min(max, initial * multiplier^reconsumeTimes)。 */\n    public long nextDelayDuration(int reconsumeTimes) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/subscription/GroupForbidden.java": [
        (
            "/**\n *\n */",
            "/**\n * 消费组禁读配置：标记某 group 对指定 topic 是否允许拉取消息。\n */",
        ),
        (
            "    private String  topic;",
            "    /** 受限 Topic 名称。 */\n    private String  topic;",
        ),
        (
            "    private String  group;",
            "    /** 消费组名称。 */\n    private String  group;",
        ),
        (
            "    private Boolean readable;",
            "    /** 是否可读（false 表示禁止消费该 Topic）。 */\n    private Boolean readable;",
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
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/subscription/GroupRetryPolicy.java": [
        (
            "public class GroupRetryPolicy {",
            "/**\n * 消费组重试策略配置：按 {@link GroupRetryPolicyType} 选择\n * 自定义阶梯或指数退避 {@link RetryPolicy} 实现。\n */\npublic class GroupRetryPolicy {",
        ),
        (
            "    private final static RetryPolicy DEFAULT_RETRY_POLICY = new CustomizedRetryPolicy();",
            "    /** 默认重试策略（CustomizedRetryPolicy）。 */\n    private final static RetryPolicy DEFAULT_RETRY_POLICY = new CustomizedRetryPolicy();",
        ),
        (
            "    private GroupRetryPolicyType type = GroupRetryPolicyType.CUSTOMIZED;",
            "    /** 重试策略类型，默认 CUSTOMIZED。 */\n    private GroupRetryPolicyType type = GroupRetryPolicyType.CUSTOMIZED;",
        ),
        (
            "    private ExponentialRetryPolicy exponentialRetryPolicy;",
            "    /** 指数退避策略参数（type 为 EXPONENTIAL 时使用）。 */\n    private ExponentialRetryPolicy exponentialRetryPolicy;",
        ),
        (
            "    private CustomizedRetryPolicy customizedRetryPolicy;",
            "    /** 自定义阶梯策略参数（type 为 CUSTOMIZED 时使用）。 */\n    private CustomizedRetryPolicy customizedRetryPolicy;",
        ),
        (
            "    public GroupRetryPolicyType getType() {",
            "    /** 返回重试策略类型。 */\n    public GroupRetryPolicyType getType() {",
        ),
        (
            "    public void setType(GroupRetryPolicyType type) {",
            "    /** 设置重试策略类型。 */\n    public void setType(GroupRetryPolicyType type) {",
        ),
        (
            "    public ExponentialRetryPolicy getExponentialRetryPolicy() {",
            "    /** 返回指数退避策略配置。 */\n    public ExponentialRetryPolicy getExponentialRetryPolicy() {",
        ),
        (
            "    public CustomizedRetryPolicy getCustomizedRetryPolicy() {",
            "    /** 返回自定义阶梯策略配置。 */\n    public CustomizedRetryPolicy getCustomizedRetryPolicy() {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public RetryPolicy getRetryPolicy() {",
            "    /** 按 type 解析并返回实际 {@link RetryPolicy}（缺省回退 DEFAULT）。 */\n    @JSONField(serialize = false, deserialize = false)\n    public RetryPolicy getRetryPolicy() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/subscription/GroupRetryPolicyType.java": [
        (
            "public enum GroupRetryPolicyType {",
            "/** 消费组重试策略类型枚举。 */\npublic enum GroupRetryPolicyType {",
        ),
        (
            "    EXPONENTIAL,",
            "    /** 指数退避重试。 */\n    EXPONENTIAL,",
        ),
        (
            "    CUSTOMIZED",
            "    /** 自定义延迟阶梯重试。 */\n    CUSTOMIZED",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/subscription/RetryPolicy.java": [
        (
            "    /**\n     * Compute message's next delay duration by specify reconsumeTimes\n     *\n     * @param reconsumeTimes Message reconsumeTimes\n     * @return Message's nextDelayDuration in milliseconds\n     */",
            "    /**\n     * 根据已重试次数计算消息下次投递前的延迟时长。\n     *\n     * @param reconsumeTimes Message reconsumeTimes\n     * @return Message's nextDelayDuration in milliseconds\n     */",
        ),
        (
            "public interface RetryPolicy {",
            "/** 消费重试延迟策略接口。 */\npublic interface RetryPolicy {",
        ),
    ],
}
