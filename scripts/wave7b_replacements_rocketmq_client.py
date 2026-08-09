"""RocketMQ 5.5.0 wave7b client acl/common + client root/admin/common [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["client/src/main/java/org/apache/rocketmq/acl/common/AclSigner.java"] = [
    (
        "public class AclSigner {",
        "/**\n * ACL 请求签名工具：使用 HMAC 算法对请求内容计算签名并 Base64 编码，\n * 供客户端 {@link AclClientRPCHook} 与服务端鉴权链校验。\n */\npublic class AclSigner {",
    ),
    (
        "    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;",
        "    /** 默认字符集 UTF-8。 */\n    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;",
    ),
    (
        "    public static final SigningAlgorithm DEFAULT_ALGORITHM = SigningAlgorithm.HmacSHA1;",
        "    /** 默认签名算法 HmacSHA1。 */\n    public static final SigningAlgorithm DEFAULT_ALGORITHM = SigningAlgorithm.HmacSHA1;",
    ),
    (
        "    public static String calSignature(String data, String key) throws AclException {",
        "    /** 使用默认算法与 UTF-8 对字符串数据签名。 */\n    public static String calSignature(String data, String key) throws AclException {",
    ),
    (
        "    public static String calSignature(String data, String key, SigningAlgorithm algorithm,\n        Charset charset) throws AclException {",
        "    /** 指定算法与字符集对字符串数据签名。 */\n    public static String calSignature(String data, String key, SigningAlgorithm algorithm,\n        Charset charset) throws AclException {",
    ),
    (
        "    private static String signAndBase64Encode(String data, String key, SigningAlgorithm algorithm, Charset charset)",
        "    /** 字符串路径：HMAC 签名后 Base64 编码。 */\n    private static String signAndBase64Encode(String data, String key, SigningAlgorithm algorithm, Charset charset)",
    ),
    (
        "    private static byte[] sign(byte[] data, byte[] key, SigningAlgorithm algorithm) throws AclException {",
        "    /** 底层 HMAC 计算；失败时抛出 {@link AclException}。 */\n    private static byte[] sign(byte[] data, byte[] key, SigningAlgorithm algorithm) throws AclException {",
    ),
    (
        "    public static String calSignature(byte[] data, String key) throws AclException {",
        "    /** 使用默认算法与 UTF-8 对字节数组签名。 */\n    public static String calSignature(byte[] data, String key) throws AclException {",
    ),
    (
        "    public static String calSignature(byte[] data, String key, SigningAlgorithm algorithm,\n        Charset charset) throws AclException {",
        "    /** 指定算法与字符集对字节数组签名。 */\n    public static String calSignature(byte[] data, String key, SigningAlgorithm algorithm,\n        Charset charset) throws AclException {",
    ),
    (
        "    private static String signAndBase64Encode(byte[] data, String key, SigningAlgorithm algorithm, Charset charset)",
        "    /** 字节数组路径：HMAC 签名后 Base64 编码。 */\n    private static String signAndBase64Encode(byte[] data, String key, SigningAlgorithm algorithm, Charset charset)",
    ),
]

R["client/src/main/java/org/apache/rocketmq/acl/common/AclUtils.java"] = [
    (
        "public class AclUtils {",
        "/**\n * ACL 辅助工具：组装 Remoting 待签名字节、IPv4/IPv6 网段校验与展开、\n * 以及从 YAML 配置构建 {@link AclClientRPCHook}。\n */\npublic class AclUtils {",
    ),
    (
        "    public static byte[] combineRequestContent(RemotingCommand request, SortedMap<String, String> fieldsMap) {",
        "    /** 拼接扩展字段值（跳过 Signature）与请求体，作为签名输入。 */\n    public static byte[] combineRequestContent(RemotingCommand request, SortedMap<String, String> fieldsMap) {",
    ),
    (
        "    public static byte[] combineBytes(byte[] b1, byte[] b2) {",
        "    /** 顺序合并两个字节数组；任一方为空则返回另一方。 */\n    public static byte[] combineBytes(byte[] b1, byte[] b2) {",
    ),
    (
        "    public static String calSignature(byte[] data, String secretKey) {",
        "    /** 委托 {@link AclSigner} 计算签名。 */\n    public static String calSignature(byte[] data, String secretKey) {",
    ),
    (
        "    public static void IPv6AddressCheck(String netAddress) {",
        "    /** 校验 IPv6 网段表达式中 * 与 - 的合法位置。 */\n    public static void IPv6AddressCheck(String netAddress) {",
    ),
    (
        "            // '*' must be the end of netAddress if it exists",
        "            // 通配符 * 若存在，必须位于网段末尾",
    ),
    (
        "            // format like \"2::ac5:78:1-200:*\" or \"2::ac5:78:1-200\" is legal",
        "            // 合法格式示例：2::ac5:78:1-200:* 或 2::ac5:78:1-200",
    ),
    (
        "    public static String v6ipProcess(String netAddress) {",
        "    /** 按 * / - 组合将 IPv6 缩写展开为 8 段标准形式。 */\n    public static String v6ipProcess(String netAddress) {",
    ),
    (
        "    public static void verify(String netAddress, int index) {",
        "    /** 校验网段前 index 段数值是否在合法范围内。 */\n    public static void verify(String netAddress, int index) {",
    ),
    (
        "    public static String[] getAddresses(String netAddress, String partialAddress) {",
        "    /** 解析 {a,b,c} 形式的 IPv6 部分地址列表。 */\n    public static String[] getAddresses(String netAddress, String partialAddress) {",
    ),
    (
        "    public static boolean isScope(String netAddress, int index) {",
        "    /** 判断 IPv4 或 IPv6 网段前 index 段是否在合法范围。 */\n    public static boolean isScope(String netAddress, int index) {",
    ),
    (
        "        // IPv6 Address",
        "        // IPv6 地址分支",
    ),
    (
        "    public static boolean isScope(String[] num, int index) {",
        "    /** IPv4 分段范围校验。 */\n    public static boolean isScope(String[] num, int index) {",
    ),
    (
        "    public static boolean isColon(String netAddress) {",
        "    /** 是否包含冒号（IPv6 特征）。 */\n    public static boolean isColon(String netAddress) {",
    ),
    (
        "    public static boolean isScope(String num) {",
        "    /** 单段 IPv4 十进制是否在 0–255。 */\n    public static boolean isScope(String num) {",
    ),
    (
        "    public static boolean isScope(int num) {",
        "    /** 整数是否在 IPv4 单段合法范围 0–255。 */\n    public static boolean isScope(int num) {",
    ),
    (
        "    public static boolean isAsterisk(String asterisk) {",
        "    /** 是否包含通配符 *。 */\n    public static boolean isAsterisk(String asterisk) {",
    ),
    (
        "    public static boolean isComma(String colon) {",
        "    /** 是否包含逗号（多地址枚举）。 */\n    public static boolean isComma(String colon) {",
    ),
    (
        "    public static boolean isMinus(String minus) {",
        "    /** 是否包含连字符 -（范围表示）。 */\n    public static boolean isMinus(String minus) {",
    ),
    (
        "    public static boolean isIPv6Scope(String[] num, int index) {",
        "    /** IPv6 十六进制分段范围校验。 */\n    public static boolean isIPv6Scope(String[] num, int index) {",
    ),
    (
        "    public static boolean isIPv6Scope(int num) {",
        "    /** 单段 IPv6 十六进制值是否在 0–ffff。 */\n    public static boolean isIPv6Scope(int num) {",
    ),
    (
        "    public static String expandIP(String netAddress, int part) {",
        "    /** 将 :: 缩写 IPv6 展开并左补零至每段 4 位十六进制。 */\n    public static String expandIP(String netAddress, int part) {",
    ),
    (
        "        // expand netAddress",
        "        // 展开 :: 省略段",
    ),
    (
        "        // pad netAddress",
        "        // 各段左补零",
    ),
    (
        "        // output",
        "        // 拼接输出",
    ),
    (
        "    public static <T> T getYamlDataObject(String path, Class<T> clazz) {",
        "    /** 从文件路径加载 YAML 并反序列化为指定类型；文件不存在返回 null。 */\n    public static <T> T getYamlDataObject(String path, Class<T> clazz) {",
    ),
    (
        "    public static <T> T getYamlDataObject(InputStream fis, Class<T> clazz) {",
        "    /** 从输入流加载 YAML 并反序列化。 */\n    public static <T> T getYamlDataObject(InputStream fis, Class<T> clazz) {",
    ),
    (
        "    public static RPCHook getAclRPCHook(String fileName) {",
        "    /** 从 YAML 文件路径读取 accessKey/secretKey 并构建 ACL RPC Hook。 */\n    public static RPCHook getAclRPCHook(String fileName) {",
    ),
    (
        "    public static RPCHook getAclRPCHook(InputStream inputStream) {",
        "    /** 从 YAML 输入流构建 ACL RPC Hook。 */\n    public static RPCHook getAclRPCHook(InputStream inputStream) {",
    ),
    (
        "    private static RPCHook buildRpcHook(JSONObject yamlDataObject) {",
        "    /** 解析 accessKey/secretKey，非空时包装为 {@link AclClientRPCHook}。 */\n    private static RPCHook buildRpcHook(JSONObject yamlDataObject) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/acl/common/Permission.java"] = [
    (
        "public class Permission {",
        "/**\n * ACL 权限位掩码：发布、订阅、拒绝等权限以位运算组合。\n */\npublic class Permission {",
    ),
    (
        "    public static final byte DENY = 1;",
        "    /** 拒绝访问。 */\n    public static final byte DENY = 1;",
    ),
    (
        "    public static final byte ANY = 1 << 1;",
        "    /** 任意权限占位（保留位）。 */\n    public static final byte ANY = 1 << 1;",
    ),
    (
        "    public static final byte PUB = 1 << 2;",
        "    /** 发布（写）权限。 */\n    public static final byte PUB = 1 << 2;",
    ),
    (
        "    public static final byte SUB = 1 << 3;",
        "    /** 订阅（读）权限。 */\n    public static final byte SUB = 1 << 3;",
    ),
    (
        "    public static byte parsePermFromString(String permString) {",
        "    /** 将 PUB/SUB/PUB|SUB/DENY 等字符串解析为权限位掩码。 */\n    public static byte parsePermFromString(String permString) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/acl/common/SessionCredentials.java"] = [
    (
        "public class SessionCredentials {",
        "/**\n * ACL 会话凭证：承载 AccessKey、SecretKey、SecurityToken 与请求签名，\n * 默认可从 {@link #KEY_FILE} 指定的本地 key 文件加载。\n */\npublic class SessionCredentials {",
    ),
    (
        "    public static final Charset CHARSET = StandardCharsets.UTF_8;",
        "    /** 凭证与签名使用的字符集。 */\n    public static final Charset CHARSET = StandardCharsets.UTF_8;",
    ),
    (
        "    public static final String ACCESS_KEY = \"AccessKey\";",
        "    /** Remoting 扩展字段：AccessKey 键名。 */\n    public static final String ACCESS_KEY = \"AccessKey\";",
    ),
    (
        "    public static final String SECRET_KEY = \"SecretKey\";",
        "    /** Remoting 扩展字段：SecretKey 键名。 */\n    public static final String SECRET_KEY = \"SecretKey\";",
    ),
    (
        "    public static final String SIGNATURE = \"Signature\";",
        "    /** Remoting 扩展字段：Signature 键名。 */\n    public static final String SIGNATURE = \"Signature\";",
    ),
    (
        "    public static final String SECURITY_TOKEN = \"SecurityToken\";",
        "    /** 临时安全令牌字段名（STS 场景）。 */\n    public static final String SECURITY_TOKEN = \"SecurityToken\";",
    ),
    (
        "    public static final String KEY_FILE = System.getProperty(\"rocketmq.client.keyFile\",",
        "    /** 本地 key 文件路径，可通过系统属性 rocketmq.client.keyFile 覆盖。 */\n    public static final String KEY_FILE = System.getProperty(\"rocketmq.client.keyFile\",",
    ),
    (
        "    public SessionCredentials() {",
        "    /** 无参构造：尝试从 {@link #KEY_FILE} 加载 AccessKey/SecretKey。 */\n    public SessionCredentials() {",
    ),
    (
        "    public SessionCredentials(String accessKey, String secretKey) {",
        "    /** 使用 AccessKey 与 SecretKey 构造凭证。 */\n    public SessionCredentials(String accessKey, String secretKey) {",
    ),
    (
        "    public SessionCredentials(String accessKey, String secretKey, String securityToken) {",
        "    /** 构造带临时 SecurityToken 的凭证。 */\n    public SessionCredentials(String accessKey, String secretKey, String securityToken) {",
    ),
    (
        "    public void updateContent(Properties prop) {",
        "    /** 从 Properties 更新 AccessKey、SecretKey 与 SecurityToken。 */\n    public void updateContent(Properties prop) {",
    ),
    (
        "    public String getAccessKey() {",
        "    /** 返回 AccessKey。 */\n    public String getAccessKey() {",
    ),
    (
        "    public void setAccessKey(String accessKey) {",
        "    /** 设置 AccessKey。 */\n    public void setAccessKey(String accessKey) {",
    ),
    (
        "    public String getSecretKey() {",
        "    /** 返回 SecretKey。 */\n    public String getSecretKey() {",
    ),
    (
        "    public void setSecretKey(String secretKey) {",
        "    /** 设置 SecretKey。 */\n    public void setSecretKey(String secretKey) {",
    ),
    (
        "    public String getSignature() {",
        "    /** 返回当前请求签名。 */\n    public String getSignature() {",
    ),
    (
        "    public void setSignature(String signature) {",
        "    /** 设置请求签名。 */\n    public void setSignature(String signature) {",
    ),
    (
        "    public String getSecurityToken() {",
        "    /** 返回 SecurityToken。 */\n    public String getSecurityToken() {",
    ),
    (
        "    public void setSecurityToken(final String securityToken) {",
        "    /** 设置 SecurityToken。 */\n    public void setSecurityToken(final String securityToken) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/acl/common/SigningAlgorithm.java"] = [
    (
        "public enum SigningAlgorithm {",
        "/**\n * ACL 请求签名支持的 HMAC 算法。\n */\npublic enum SigningAlgorithm {",
    ),
    (
        "    HmacSHA1,",
        "    /** HMAC-SHA1（默认）。 */\n    HmacSHA1,",
    ),
    (
        "    HmacSHA256,",
        "    /** HMAC-SHA256。 */\n    HmacSHA256,",
    ),
    (
        "    HmacMD5;",
        "    /** HMAC-MD5。 */\n    HmacMD5;",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/AccessChannel.java"] = [
    (
        "/**\n * Used for set access channel, if need migrate the rocketmq service to cloud, it is We recommend set the value with\n * \"CLOUD\". otherwise set with \"LOCAL\", especially used the message trace feature.\n */",
        "/**\n * 访问通道枚举：迁移至云服务时建议设为 CLOUD，自建 IDC 或启用消息轨迹时设为 LOCAL。\n */",
    ),
    (
        "    /**\n     * Means connect to private IDC cluster.\n     */",
        "    /** 连接自建 IDC 集群。 */",
    ),
    (
        "    /**\n     * Means connect to Cloud service.\n     */",
        "    /** 连接 RocketMQ 云服务。 */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/MQAdmin.java"] = [
    (
        "/**\n * Base interface for MQ management\n */",
        "/**\n * MQ 管理基础接口：Topic 创建、队列 offset 查询与按 key 检索消息等。\n */",
    ),
    (
        "    /**\n     * Creates a topic\n     *  @param key accessKey\n     * @param newTopic topic name\n     * @param queueNum topic's queue number\n     * @param attributes\n     */",
        "    /**\n     * 创建 Topic。\n     *\n     * @param key accessKey\n     * @param newTopic Topic 名称\n     * @param queueNum 队列数\n     * @param attributes 扩展属性\n     */",
    ),
    (
        "    /**\n     * Creates a topic\n     *  @param key accessKey\n     * @param newTopic topic name\n     * @param queueNum topic's queue number\n     * @param topicSysFlag topic system flag\n     * @param attributes\n     */",
        "    /**\n     * 创建 Topic（指定系统标志位）。\n     *\n     * @param topicSysFlag Topic 系统标志\n     */",
    ),
    (
        "    /**\n     * Gets the message queue offset according to some time in milliseconds<br>\n     * be cautious to call because of more IO overhead\n     *\n     * @param mq Instance of MessageQueue\n     * @param timestamp from when in milliseconds.\n     * @return offset\n     */",
        "    /**\n     * 按时间戳查找队列消费位点；涉及 Broker IO，调用需谨慎。\n     */",
    ),
    (
        "    /**\n     * Gets the max offset\n     *\n     * @param mq Instance of MessageQueue\n     * @return the max offset\n     */",
        "    /** 获取队列最大 offset。 */",
    ),
    (
        "    /**\n     * Gets the minimum offset\n     *\n     * @param mq Instance of MessageQueue\n     * @return the minimum offset\n     */",
        "    /** 获取队列最小 offset。 */",
    ),
    (
        "    /**\n     * Gets the earliest stored message time\n     *\n     * @param mq Instance of MessageQueue\n     * @return the time in microseconds\n     */",
        "    /** 获取队列最早消息存储时间（微秒）。 */",
    ),
    (
        "    /**\n     * Query messages\n     *\n     * @param topic message topic\n     * @param key message key index word\n     * @param maxNum max message number\n     * @param begin from when\n     * @param end to when\n     * @return Instance of QueryResult\n     */",
        "    /** 按 Topic 与 key 在时间范围内索引查询消息。 */",
    ),
    (
        "    /**\n     * @return The {@code MessageExt} of given msgId\n     */",
        "    /** 按 msgId 查看单条消息详情。 */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/MQHelper.java"] = [
    (
        "public class MQHelper {",
        "/**\n * MQ 运维辅助工具：提供按时间戳重置消费位点等便捷方法。\n */\npublic class MQHelper {",
    ),
    (
        "    @Deprecated\n    public static void resetOffsetByTimestamp(",
        "    /** @deprecated 请使用带 instanceName 的重载方法。 */\n    @Deprecated\n    public static void resetOffsetByTimestamp(",
    ),
    (
        "    /**\n     * Reset consumer topic offset according to time\n     *\n     * @param messageModel  which model\n     * @param instanceName  which instance\n     * @param consumerGroup consumer group\n     * @param topic         topic\n     * @param timestamp     time\n     */",
        "    /**\n     * 按时间戳重置指定消费组在各队列上的消费位点。\n     *\n     * @param messageModel 集群/广播消费模式\n     * @param instanceName 客户端实例名\n     * @param consumerGroup 消费组\n     * @param topic Topic\n     * @param timestamp 目标时间戳（毫秒）\n     */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/MqClientAdmin.java"] = [
    (
        "public interface MqClientAdmin {",
        "/**\n * 客户端管理 RPC 门面：以 {@link CompletableFuture} 异步调用 Broker/NameServer\n * 的管理接口（Topic、订阅组、消费统计、直接消费等）。\n */\npublic interface MqClientAdmin {",
    ),
    (
        "    CompletableFuture<List<MessageExt>> queryMessage(String address, boolean uniqueKeyFlag, boolean decompressBody,\n        QueryMessageRequestHeader requestHeader, long timeoutMillis);",
        "    /** 异步按索引查询消息。 */\n    CompletableFuture<List<MessageExt>> queryMessage(String address, boolean uniqueKeyFlag, boolean decompressBody,\n        QueryMessageRequestHeader requestHeader, long timeoutMillis);",
    ),
    (
        "    CompletableFuture<TopicStatsTable> getTopicStatsInfo(String address,\n        GetTopicStatsInfoRequestHeader requestHeader, long timeoutMillis);",
        "    /** 异步获取 Topic 收发统计。 */\n    CompletableFuture<TopicStatsTable> getTopicStatsInfo(String address,\n        GetTopicStatsInfoRequestHeader requestHeader, long timeoutMillis);",
    ),
    (
        "    CompletableFuture<List<QueueTimeSpan>> queryConsumeTimeSpan(String address,\n        QueryConsumeTimeSpanRequestHeader requestHeader, long timeoutMillis);",
        "    /** 异步查询各队列消息时间跨度。 */\n    CompletableFuture<List<QueueTimeSpan>> queryConsumeTimeSpan(String address,\n        QueryConsumeTimeSpanRequestHeader requestHeader, long timeoutMillis);",
    ),
    (
        "    CompletableFuture<Void> updateOrCreateTopic(String address, CreateTopicRequestHeader requestHeader,\n        long timeoutMillis);",
        "    /** 异步创建或更新 Topic。 */\n    CompletableFuture<Void> updateOrCreateTopic(String address, CreateTopicRequestHeader requestHeader,\n        long timeoutMillis);",
    ),
    (
        "    CompletableFuture<Void> updateOrCreateSubscriptionGroup(String address, SubscriptionGroupConfig config,\n        long timeoutMillis);",
        "    /** 异步创建或更新订阅组配置。 */\n    CompletableFuture<Void> updateOrCreateSubscriptionGroup(String address, SubscriptionGroupConfig config,\n        long timeoutMillis);",
    ),
    (
        "    CompletableFuture<Void> deleteTopicInBroker(String address, DeleteTopicRequestHeader requestHeader,\n        long timeoutMillis);",
        "    /** 异步在 Broker 上删除 Topic。 */\n    CompletableFuture<Void> deleteTopicInBroker(String address, DeleteTopicRequestHeader requestHeader,\n        long timeoutMillis);",
    ),
    (
        "    CompletableFuture<Void> deleteTopicInNameserver(String address, DeleteTopicFromNamesrvRequestHeader requestHeader,\n        long timeoutMillis);",
        "    /** 异步在 NameServer 路由表中删除 Topic。 */\n    CompletableFuture<Void> deleteTopicInNameserver(String address, DeleteTopicFromNamesrvRequestHeader requestHeader,\n        long timeoutMillis);",
    ),
    (
        "    CompletableFuture<Void> deleteKvConfig(String address, DeleteKVConfigRequestHeader requestHeader,\n        long timeoutMillis);",
        "    /** 异步删除 NameServer KV 配置。 */\n    CompletableFuture<Void> deleteKvConfig(String address, DeleteKVConfigRequestHeader requestHeader,\n        long timeoutMillis);",
    ),
    (
        "    CompletableFuture<Void> deleteSubscriptionGroup(String address, DeleteSubscriptionGroupRequestHeader requestHeader,\n        long timeoutMillis);",
        "    /** 异步删除订阅组。 */\n    CompletableFuture<Void> deleteSubscriptionGroup(String address, DeleteSubscriptionGroupRequestHeader requestHeader,\n        long timeoutMillis);",
    ),
    (
        "    CompletableFuture<Map<MessageQueue, Long>> invokeBrokerToResetOffset(String address,\n        ResetOffsetRequestHeader requestHeader, long timeoutMillis);",
        "    /** 异步请求 Broker 重置消费位点。 */\n    CompletableFuture<Map<MessageQueue, Long>> invokeBrokerToResetOffset(String address,\n        ResetOffsetRequestHeader requestHeader, long timeoutMillis);",
    ),
    (
        "    CompletableFuture<MessageExt> viewMessage(String address, ViewMessageRequestHeader requestHeader,\n        long timeoutMillis);",
        "    /** 异步按物理 offset 查看单条消息。 */\n    CompletableFuture<MessageExt> viewMessage(String address, ViewMessageRequestHeader requestHeader,\n        long timeoutMillis);",
    ),
    (
        "    CompletableFuture<ClusterInfo> getBrokerClusterInfo(String address, long timeoutMillis);",
        "    /** 异步获取 Broker 集群信息。 */\n    CompletableFuture<ClusterInfo> getBrokerClusterInfo(String address, long timeoutMillis);",
    ),
    (
        "    CompletableFuture<ConsumerConnection> getConsumerConnectionList(String address,\n        GetConsumerConnectionListRequestHeader requestHeader, long timeoutMillis);",
        "    /** 异步查询消费组在线连接。 */\n    CompletableFuture<ConsumerConnection> getConsumerConnectionList(String address,\n        GetConsumerConnectionListRequestHeader requestHeader, long timeoutMillis);",
    ),
    (
        "    CompletableFuture<TopicList> queryTopicsByConsumer(String address,\n        QueryTopicsByConsumerRequestHeader requestHeader, long timeoutMillis);",
        "    /** 异步查询消费组订阅的 Topic 列表。 */\n    CompletableFuture<TopicList> queryTopicsByConsumer(String address,\n        QueryTopicsByConsumerRequestHeader requestHeader, long timeoutMillis);",
    ),
    (
        "    CompletableFuture<SubscriptionData> querySubscriptionByConsumer(String address,\n        QuerySubscriptionByConsumerRequestHeader requestHeader, long timeoutMillis);",
        "    /** 异步查询消费组对指定 Topic 的订阅详情。 */\n    CompletableFuture<SubscriptionData> querySubscriptionByConsumer(String address,\n        QuerySubscriptionByConsumerRequestHeader requestHeader, long timeoutMillis);",
    ),
    (
        "    CompletableFuture<ConsumeStats> getConsumeStats(String address, GetConsumeStatsRequestHeader requestHeader,\n        long timeoutMillis);",
        "    /** 异步获取消费进度统计（TPS、堆积等）。 */\n    CompletableFuture<ConsumeStats> getConsumeStats(String address, GetConsumeStatsRequestHeader requestHeader,\n        long timeoutMillis);",
    ),
    (
        "    CompletableFuture<GroupList> queryTopicConsumeByWho(String address,\n        QueryTopicConsumeByWhoRequestHeader requestHeader, long timeoutMillis);",
        "    /** 异步查询订阅指定 Topic 的消费组列表。 */\n    CompletableFuture<GroupList> queryTopicConsumeByWho(String address,\n        QueryTopicConsumeByWhoRequestHeader requestHeader, long timeoutMillis);",
    ),
    (
        "    CompletableFuture<ConsumerRunningInfo> getConsumerRunningInfo(String address,\n        GetConsumerRunningInfoRequestHeader requestHeader, long timeoutMillis);",
        "    /** 异步获取消费端运行时信息（订阅、位点、JStack 等）。 */\n    CompletableFuture<ConsumerRunningInfo> getConsumerRunningInfo(String address,\n        GetConsumerRunningInfoRequestHeader requestHeader, long timeoutMillis);",
    ),
    (
        "    CompletableFuture<ConsumeMessageDirectlyResult> consumeMessageDirectly(String address,\n        ConsumeMessageDirectlyResultRequestHeader requestHeader, long timeoutMillis);",
        "    /** 异步在 Broker 侧直接触发一次消费（运维调试）。 */\n    CompletableFuture<ConsumeMessageDirectlyResult> consumeMessageDirectly(String address,\n        ConsumeMessageDirectlyResultRequestHeader requestHeader, long timeoutMillis);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/QueryResult.java"] = [
    (
        "public class QueryResult {",
        "/**\n * 消息索引查询结果：包含索引最后更新时间戳与匹配的消息列表。\n */\npublic class QueryResult {",
    ),
    (
        "    public QueryResult(long indexLastUpdateTimestamp, List<MessageExt> messageList) {",
        "    /** 构造查询结果。 */\n    public QueryResult(long indexLastUpdateTimestamp, List<MessageExt> messageList) {",
    ),
    (
        "    public long getIndexLastUpdateTimestamp() {",
        "    /** 返回索引最后更新时间戳。 */\n    public long getIndexLastUpdateTimestamp() {",
    ),
    (
        "    public List<MessageExt> getMessageList() {",
        "    /** 返回匹配的消息列表。 */\n    public List<MessageExt> getMessageList() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/Validators.java"] = [
    (
        "/**\n * Common Validator\n */",
        "/**\n * 客户端通用校验器：校验 Group、Topic、Message 与 Broker/Topic 权限配置。\n */",
    ),
    (
        "    public static final int CHARACTER_MAX_LENGTH = 255;",
        "    /** 通用字符串最大长度。 */\n    public static final int CHARACTER_MAX_LENGTH = 255;",
    ),
    (
        "    public static final int TOPIC_MAX_LENGTH = 127;",
        "    /** Topic 名称最大长度。 */\n    public static final int TOPIC_MAX_LENGTH = 127;",
    ),
    (
        "    /*\n     * Group name max length is 120, for it will be used to make up retry and DLQ topic,\n     * like pull retry: %RETRY%group_topic and pop retry: %RETRY%group_topic.\n     */",
        "    /*\n     * 消费组名最长 120：需预留 %RETRY%group_topic 等重试 Topic 拼接空间。\n     */",
    ),
    (
        "    public static final int GROUP_MAX_LENGTH = 120;",
        "    /** 消费组名称最大长度。 */\n    public static final int GROUP_MAX_LENGTH = 120;",
    ),
    (
        "    /**\n     * Validate group\n     */",
        "    /** 校验消费组名非空、长度与字符合法性。 */",
    ),
    (
        "    public static void checkMessage(Message msg, DefaultMQProducer defaultMQProducer) throws MQClientException {",
        "    /** 校验消息 Topic、Body 非空且不超过 Producer 最大消息大小。 */\n    public static void checkMessage(Message msg, DefaultMQProducer defaultMQProducer) throws MQClientException {",
    ),
    (
        "        // topic",
        "        // 校验 Topic",
    ),
    (
        "        // body",
        "        // 校验消息体",
    ),
    (
        "    public static void checkTopic(String topic) throws MQClientException {",
        "    /** 校验 Topic 非空、长度与字符合法性。 */\n    public static void checkTopic(String topic) throws MQClientException {",
    ),
    (
        "    public static void isSystemTopic(String topic) throws MQClientException {",
        "    /** 禁止用户使用系统保留 Topic。 */\n    public static void isSystemTopic(String topic) throws MQClientException {",
    ),
    (
        "    public static void isNotAllowedSendTopic(String topic) throws MQClientException {",
        "    /** 禁止向不允许发送的 Topic 投递消息。 */\n    public static void isNotAllowedSendTopic(String topic) throws MQClientException {",
    ),
    (
        "    public static void checkTopicConfig(final TopicConfig topicConfig) throws MQClientException {",
        "    /** 校验 Topic 权限位是否合法。 */\n    public static void checkTopicConfig(final TopicConfig topicConfig) throws MQClientException {",
    ),
    (
        "    public static void checkBrokerConfig(final Properties brokerConfig) throws MQClientException {",
        "    /** 校验 Broker 配置中的 brokerPermission 是否合法。 */\n    public static void checkBrokerConfig(final Properties brokerConfig) throws MQClientException {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/admin/MQAdminExtInner.java"] = [
    (
        "public interface MQAdminExtInner {",
        "/**\n * MQAdmin 扩展内部标记接口：供 {@code MQClientInstance} 识别管理端实现。\n */\npublic interface MQAdminExtInner {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/common/ClientErrorCode.java"] = [
    (
        "public class ClientErrorCode {",
        "/**\n * 客户端本地错误码常量：连接 Broker、NameServer 超时与 Topic 不存在等场景。\n */\npublic class ClientErrorCode {",
    ),
    (
        "    public static final int CONNECT_BROKER_EXCEPTION = 10001;",
        "    /** 连接 Broker 失败。 */\n    public static final int CONNECT_BROKER_EXCEPTION = 10001;",
    ),
    (
        "    public static final int ACCESS_BROKER_TIMEOUT = 10002;",
        "    /** 访问 Broker 超时。 */\n    public static final int ACCESS_BROKER_TIMEOUT = 10002;",
    ),
    (
        "    public static final int BROKER_NOT_EXIST_EXCEPTION = 10003;",
        "    /** Broker 不存在。 */\n    public static final int BROKER_NOT_EXIST_EXCEPTION = 10003;",
    ),
    (
        "    public static final int NO_NAME_SERVER_EXCEPTION = 10004;",
        "    /** 未配置或无法连接 NameServer。 */\n    public static final int NO_NAME_SERVER_EXCEPTION = 10004;",
    ),
    (
        "    public static final int NOT_FOUND_TOPIC_EXCEPTION = 10005;",
        "    /** Topic 路由未找到。 */\n    public static final int NOT_FOUND_TOPIC_EXCEPTION = 10005;",
    ),
    (
        "    public static final int REQUEST_TIMEOUT_EXCEPTION = 10006;",
        "    /** 请求超时。 */\n    public static final int REQUEST_TIMEOUT_EXCEPTION = 10006;",
    ),
    (
        "    public static final int CREATE_REPLY_MESSAGE_EXCEPTION = 10007;",
        "    /** 创建 Reply 消息失败。 */\n    public static final int CREATE_REPLY_MESSAGE_EXCEPTION = 10007;",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/common/NameserverAccessConfig.java"] = [
    (
        "public class NameserverAccessConfig {",
        "/**\n * NameServer 访问配置：支持直连地址或域名/subgroup 解析方式。\n */\npublic class NameserverAccessConfig {",
    ),
    (
        "    public NameserverAccessConfig(String namesrvAddr, String namesrvDomain, String namesrvDomainSubgroup) {",
        "    /** 构造 NameServer 访问配置。 */\n    public NameserverAccessConfig(String namesrvAddr, String namesrvDomain, String namesrvDomainSubgroup) {",
    ),
    (
        "    public String getNamesrvAddr() {",
        "    /** 返回 NameServer 地址列表（分号分隔）。 */\n    public String getNamesrvAddr() {",
    ),
    (
        "    public String getNamesrvDomain() {",
        "    /** 返回 NameServer 域名（HTTP 寻址）。 */\n    public String getNamesrvDomain() {",
    ),
    (
        "    public String getNamesrvDomainSubgroup() {",
        "    /** 返回域名寻址子组标识。 */\n    public String getNamesrvDomainSubgroup() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/common/ThreadLocalIndex.java"] = [
    (
        "public class ThreadLocalIndex {",
        "/**\n * 线程本地递增索引：为每线程维护独立计数器，用于客户端实例名等场景。\n */\npublic class ThreadLocalIndex {",
    ),
    (
        "    public int incrementAndGet() {",
        "    /** 线程内递增并返回非负索引值。 */\n    public int incrementAndGet() {",
    ),
    (
        "    public void reset() {",
        "    /** 用随机正整数重置当前线程索引。 */\n    public void reset() {",
    ),
]
