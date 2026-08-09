"""RocketMQ 5.5.0 wave11b client trace/hook/utils [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["client/src/main/java/org/apache/rocketmq/client/trace/TraceContext.java"] = [
    (
        "/**\n * The context of Trace\n */",
        "/**\n * 消息轨迹上下文：记录一次轨迹事件（发送/消费/事务等）的元数据与关联 {@link TraceBean} 列表。\n * 按时间戳排序，用于异步上报与轨迹查询。\n */",
    ),
    (
        "    private TraceType traceType;",
        "    /** 轨迹类型（发布、消费前/后、事务结束、撤回等）。 */\n    private TraceType traceType;",
    ),
    (
        "    private long timeStamp = System.currentTimeMillis();",
        "    /** 事件发生时间戳（毫秒）。 */\n    private long timeStamp = System.currentTimeMillis();",
    ),
    (
        "    private String regionId = \"\";",
        "    /** 轨迹所属区域 ID。 */\n    private String regionId = \"\";",
    ),
    (
        "    private String regionName = \"\";",
        "    /** 轨迹所属区域名称。 */\n    private String regionName = \"\";",
    ),
    (
        "    private String groupName = \"\";",
        "    /** Producer 或 Consumer 组名（已去除命名空间）。 */\n    private String groupName = \"\";",
    ),
    (
        "    private int costTime = 0;",
        "    /** 操作耗时（毫秒）。 */\n    private int costTime = 0;",
    ),
    (
        "    private boolean isSuccess = true;",
        "    /** 操作是否成功。 */\n    private boolean isSuccess = true;",
    ),
    (
        "    private String requestId = MessageClientIDSetter.createUniqID();",
        "    /** 消费前后轨迹关联用的请求 ID。 */\n    private String requestId = MessageClientIDSetter.createUniqID();",
    ),
    (
        "    private int contextCode = 0;",
        "    /** 消费返回类型编码（对应 {@link org.apache.rocketmq.client.consumer.listener.ConsumeReturnType}）。 */\n    private int contextCode = 0;",
    ),
    (
        "    private AccessChannel accessChannel;",
        "    /** 访问通道（本地/云等），影响轨迹编码格式。 */\n    private AccessChannel accessChannel;",
    ),
    (
        "    private List<TraceBean> traceBeans;",
        "    /** 本上下文关联的消息轨迹明细列表。 */\n    private List<TraceBean> traceBeans;",
    ),
    (
        "    public int compareTo(TraceContext o) {",
        "    /** 按时间戳升序比较，用于轨迹排序。 */\n    public int compareTo(TraceContext o) {",
    ),
    (
        "    public String toString() {",
        "    /** 拼接轨迹类型、组名、区域及消息摘要的调试字符串。 */\n    public String toString() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/TraceDataEncoder.java"] = [
    (
        "/**\n * Encode/decode for Trace Data\n */",
        "/**\n * 轨迹数据编解码器：在分隔符分隔的字符串与 {@link TraceContext} 列表之间互转。\n * 支持发布、消费前/后、事务结束、撤回等多种轨迹类型的序列化与兼容旧版格式。\n */",
    ),
    (
        "    /**\n     * Resolving traceContext list From trace data String\n     *\n     * @param traceData\n     * @return\n     */",
        "    /**\n     * 从轨迹数据字符串解析 {@link TraceContext} 列表。\n     *\n     * @param traceData 以 {@link TraceConstants#FIELD_SPLITOR} 分隔的轨迹载荷\n     * @return 解析出的轨迹上下文列表，空输入返回空列表\n     */",
    ),
    (
        "                // compatible with the old version",
        "                // 兼容旧版轨迹格式（含 clientHost 等扩展字段）",
    ),
    (
        "                    // add the context type",
        "                    // 追加消费返回类型编码",
    ),
    (
        "                // compatible with the old version\n                if (line.length >= 9) {",
        "                // 兼容旧版 SubAfter 格式（含 timeStamp 与 groupName）\n                if (line.length >= 9) {",
    ),
    (
        "    /**\n     * Encoding the trace context into data strings and keyset sets\n     *\n     * @param ctx\n     * @return\n     */",
        "    /**\n     * 将 {@link TraceContext} 编码为传输实体：分隔符拼接的 transData 与 msgId/keys 索引集合。\n     *\n     * @param ctx 待编码的轨迹上下文\n     * @return 轨迹传输 Bean，ctx 为 null 时返回 null\n     */",
    ),
    (
        "        //build message trace of the transferring entity content bean",
        "        // 按轨迹类型拼接 transData 字段",
    ),
    (
        "                //append the content of context and traceBean to transferBean's TransData",
        "                // 发布轨迹：写入 topic、msgId、耗时、成功标志等",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/TraceDispatcher.java"] = [
    (
        "/**\n * Interface of asynchronous transfer data\n */",
        "/**\n * 轨迹异步传输接口：负责启动、追加轨迹上下文、刷盘与关闭。\n * 由 {@link org.apache.rocketmq.client.trace.AsyncTraceDispatcher} 等实现。\n */",
    ),
    (
        "    enum Type {\n        PRODUCE,\n        CONSUME\n    }",
        "    /** 轨迹分发器角色：生产者侧或消费者侧。 */\n    enum Type {\n        PRODUCE,\n        CONSUME\n    }",
    ),
    (
        "    /**\n     * Initialize asynchronous transfer data module\n     */",
        "    /**\n     * 初始化异步轨迹传输模块，连接 NameServer 并注册客户端。\n     */",
    ),
    (
        "    /**\n     * Append the transferring data\n     * @param ctx data information\n     * @return\n     */",
        "    /**\n     * 追加一条轨迹上下文到发送队列。\n     * @param ctx 轨迹上下文（通常为 {@link TraceContext}）\n     * @return 是否成功入队\n     */",
    ),
    (
        "    /**\n     * Write flush action\n     *\n     * @throws IOException\n     */",
        "    /**\n     * 强制刷盘/发送缓冲中的轨迹数据。\n     *\n     * @throws IOException 网络或 IO 异常\n     */",
    ),
    (
        "    /**\n     * Close the trace Hook\n     */",
        "    /** 关闭轨迹分发器，释放线程与网络资源。 */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/TraceDispatcherType.java"] = [
    (
        "public enum TraceDispatcherType {",
        "/** 轨迹分发器类型：标识 Producer 或 Consumer 侧的轨迹上报通道。 */\npublic enum TraceDispatcherType {",
    ),
    (
        "    PRODUCER,",
        "    /** 生产者轨迹分发器。 */\n    PRODUCER,",
    ),
    (
        "    CONSUMER",
        "    /** 消费者轨迹分发器。 */\n    CONSUMER",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/TraceTransferBean.java"] = [
    (
        "/**\n * Trace transferring bean\n */",
        "/**\n * 轨迹传输实体：携带编码后的轨迹字符串与用于索引的 key 集合（msgId、业务 keys）。\n */",
    ),
    (
        "    private String transData;",
        "    /** 分隔符编码的轨迹数据正文。 */\n    private String transData;",
    ),
    (
        "    private Set<String> transKey = new HashSet<>();",
        "    /** 轨迹索引 key 集合，便于按 msgId 或业务 key 检索。 */\n    private Set<String> transKey = new HashSet<>();",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/TraceType.java"] = [
    (
        "public enum TraceType {",
        "/** 轨迹事件类型枚举，对应编解码与 Hook 中的不同阶段。 */\npublic enum TraceType {",
    ),
    (
        "    Pub,",
        "    /** 消息发布（发送）轨迹。 */\n    Pub,",
    ),
    (
        "    Recall,",
        "    /** 消息撤回轨迹。 */\n    Recall,",
    ),
    (
        "    SubBefore,",
        "    /** 消费开始前轨迹。 */\n    SubBefore,",
    ),
    (
        "    SubAfter,",
        "    /** 消费结束后轨迹。 */\n    SubAfter,",
    ),
    (
        "    EndTransaction,",
        "    /** 事务消息提交/回滚结束轨迹。 */\n    EndTransaction,",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/TraceView.java"] = [
    (
        "public class TraceView {",
        "/**\n * 轨迹视图：从轨迹 Topic 消息体解码出的单条轨迹展示模型，\n * 供控制台或查询工具按 msgId 过滤并展示发送/消费链路。\n */\npublic class TraceView {",
    ),
    (
        "    public static List<TraceView> decodeFromTraceTransData(String key, MessageExt messageExt) {",
        "    /**\n     * 从轨迹 Topic 的 {@link MessageExt} 中解码与指定 key 匹配的轨迹视图列表。\n     *\n     * @param key 目标 msgId\n     * @param messageExt 轨迹 Topic 消息\n     * @return 匹配的轨迹视图列表\n     */",
    ),
    (
        "    private String msgId;",
        "    /** 消息 ID。 */\n    private String msgId;",
    ),
    (
        "    private String status;",
        "    /** 轨迹状态（success / failed）。 */\n    private String status;",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/hook/ConsumeMessageOpenTracingHookImpl.java"] = [
    (
        "public class ConsumeMessageOpenTracingHookImpl implements ConsumeMessageHook {",
        "/**\n * 基于 OpenTracing 的消费消息 Hook：为每条消息创建 Consumer Span，\n * 从消息属性提取父上下文并注入 RocketMQ 相关标签。\n */\npublic class ConsumeMessageOpenTracingHookImpl implements ConsumeMessageHook {",
    ),
    (
        "    private Tracer tracer;",
        "    /** OpenTracing Tracer 实例。 */\n    private Tracer tracer;",
    ),
    (
        "    public ConsumeMessageOpenTracingHookImpl(Tracer tracer) {",
        "    /** 注入 Tracer 以创建与结束 Span。 */\n    public ConsumeMessageOpenTracingHookImpl(Tracer tracer) {",
    ),
    (
        "    public void consumeMessageBefore(ConsumeMessageContext context) {",
        "    /** 消费前：为每条消息启动 Span，写入 topic、msgId、重试次数等标签。 */\n    public void consumeMessageBefore(ConsumeMessageContext context) {",
    ),
    (
        "    public void consumeMessageAfter(ConsumeMessageContext context) {",
        "    /** 消费后：标记成功标志并 finish 所有 Span。 */\n    public void consumeMessageAfter(ConsumeMessageContext context) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/hook/ConsumeMessageTraceHookImpl.java"] = [
    (
        "public class ConsumeMessageTraceHookImpl implements ConsumeMessageHook {",
        "/**\n * RocketMQ 原生消费轨迹 Hook：消费前上报 SubBefore，消费后上报 SubAfter，\n * 经 {@link TraceDispatcher} 异步写入轨迹 Topic。\n */\npublic class ConsumeMessageTraceHookImpl implements ConsumeMessageHook {",
    ),
    (
        "    private TraceDispatcher localDispatcher;",
        "    /** 本地轨迹分发器（Consumer 侧）。 */\n    private TraceDispatcher localDispatcher;",
    ),
    (
        "    public ConsumeMessageTraceHookImpl(TraceDispatcher localDispatcher) {",
        "    /** 绑定 Consumer 侧 TraceDispatcher。 */\n    public ConsumeMessageTraceHookImpl(TraceDispatcher localDispatcher) {",
    ),
    (
        "    public void consumeMessageBefore(ConsumeMessageContext context) {",
        "    /** 消费前：构建 SubBefore 上下文并 append；trace 开关为 false 的消息跳过。 */\n    public void consumeMessageBefore(ConsumeMessageContext context) {",
    ),
    (
        "                // If trace switch is false ,skip it",
        "                // 消息级 trace 开关关闭则跳过",
    ),
    (
        "    public void consumeMessageAfter(ConsumeMessageContext context) {",
        "    /** 消费后：基于 SubBefore 计算耗时，构建 SubAfter 并 append。 */\n    public void consumeMessageAfter(ConsumeMessageContext context) {",
    ),
    (
        "            // If subBefore bean is null ,skip it",
        "            // 无有效 SubBefore 明细则跳过",
    ),
    (
        "        // Calculate the cost time for processing messages",
        "        // 按消息条数均摊计算单条处理耗时",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/hook/DefaultRecallMessageTraceHook.java"] = [
    (
        "public class DefaultRecallMessageTraceHook implements RPCHook {",
        "/**\n * 默认撤回消息轨迹 RPC Hook：在 RECALL_MESSAGE 响应成功后，\n * 可选地将 Recall 类型轨迹追加到 {@link TraceDispatcher}。\n * 通过系统属性 {@code com.rocketmq.recall.default.trace.enable} 控制开关。\n */\npublic class DefaultRecallMessageTraceHook implements RPCHook {",
    ),
    (
        "    private static final String RECALL_TRACE_ENABLE_KEY = \"com.rocketmq.recall.default.trace.enable\";",
        "    /** 是否启用默认撤回轨迹的系统属性键。 */\n    private static final String RECALL_TRACE_ENABLE_KEY = \"com.rocketmq.recall.default.trace.enable\";",
    ),
    (
        "    private boolean enableDefaultTrace = Boolean.parseBoolean(System.getProperty(RECALL_TRACE_ENABLE_KEY, \"false\"));",
        "    /** 是否记录撤回轨迹，默认 false。 */\n    private boolean enableDefaultTrace = Boolean.parseBoolean(System.getProperty(RECALL_TRACE_ENABLE_KEY, \"false\"));",
    ),
    (
        "    private TraceDispatcher traceDispatcher;",
        "    /** 轨迹分发器，用于 append Recall 上下文。 */\n    private TraceDispatcher traceDispatcher;",
    ),
    (
        "    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {",
        "    /** 请求前无操作。 */\n    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {",
    ),
    (
        "    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {",
        "    /** 撤回响应后：解析 handle 与 region，构建 Recall 轨迹并 append。 */\n    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/hook/EndTransactionOpenTracingHookImpl.java"] = [
    (
        "public class EndTransactionOpenTracingHookImpl implements EndTransactionHook {",
        "/**\n * 基于 OpenTracing 的事务结束 Hook：为 commit/rollback 创建 Producer Span，\n * 记录事务 ID、状态及是否来自回查等标签。\n */\npublic class EndTransactionOpenTracingHookImpl implements EndTransactionHook {",
    ),
    (
        "    private Tracer tracer;",
        "    /** OpenTracing Tracer 实例。 */\n    private Tracer tracer;",
    ),
    (
        "    public void endTransaction(EndTransactionContext context) {",
        "    /** 事务结束时创建并立即 finish 一条 EndTransaction Span。 */\n    public void endTransaction(EndTransactionContext context) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/hook/EndTransactionTraceHookImpl.java"] = [
    (
        "public class EndTransactionTraceHookImpl implements EndTransactionHook {",
        "/**\n * RocketMQ 原生事务结束轨迹 Hook：在 commit/rollback 时构建 EndTransaction 上下文\n * 并经由 {@link TraceDispatcher} 异步上报（跳过轨迹 Topic 自身消息）。\n */\npublic class EndTransactionTraceHookImpl implements EndTransactionHook {",
    ),
    (
        "    private TraceDispatcher localDispatcher;",
        "    /** 本地轨迹分发器。 */\n    private TraceDispatcher localDispatcher;",
    ),
    (
        "    public void endTransaction(EndTransactionContext context) {",
        "    /** 构建 EndTransaction 轨迹并 append；轨迹 Topic 消息不重复记录。 */\n    public void endTransaction(EndTransactionContext context) {",
    ),
    (
        "        //if it is message trace data,then it doesn't recorded",
        "        // 轨迹 Topic 自身消息不再写入轨迹，避免递归",
    ),
    (
        "        //build the context content of TuxeTraceContext",
        "        // 组装 EndTransaction 轨迹上下文",
    ),
    (
        "        //build the data bean object of message trace",
        "        // 填充消息与事务相关 TraceBean 字段",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/hook/SendMessageOpenTracingHookImpl.java"] = [
    (
        "public class SendMessageOpenTracingHookImpl implements SendMessageHook {",
        "/**\n * 基于 OpenTracing 的发送消息 Hook：发送前创建 Producer Span 并 inject 到消息属性，\n * 发送后写入 msgId、region 与成功标志后 finish。\n */\npublic class SendMessageOpenTracingHookImpl implements SendMessageHook {",
    ),
    (
        "    private Tracer tracer;",
        "    /** OpenTracing Tracer 实例。 */\n    private Tracer tracer;",
    ),
    (
        "    public void sendMessageBefore(SendMessageContext context) {",
        "    /** 发送前：启动 Span，inject 上下文到消息 properties。 */\n    public void sendMessageBefore(SendMessageContext context) {",
    ),
    (
        "    public void sendMessageAfter(SendMessageContext context) {",
        "    /** 发送后：补充 msgId、regionId、成功标志并 finish Span。 */\n    public void sendMessageAfter(SendMessageContext context) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/hook/SendMessageTraceHookImpl.java"] = [
    (
        "public class SendMessageTraceHookImpl implements SendMessageHook {",
        "/**\n * RocketMQ 原生发送轨迹 Hook：发送前准备 Pub 上下文，发送成功后 append 完整轨迹。\n * 轨迹 Topic 消息与 trace 开关关闭时不记录。\n */\npublic class SendMessageTraceHookImpl implements SendMessageHook {",
    ),
    (
        "    private TraceDispatcher localDispatcher;",
        "    /** 本地轨迹分发器（Producer 侧）。 */\n    private TraceDispatcher localDispatcher;",
    ),
    (
        "    public void sendMessageBefore(SendMessageContext context) {",
        "    /** 发送前：初始化 Pub 类型 TraceContext 与 TraceBean。 */\n    public void sendMessageBefore(SendMessageContext context) {",
    ),
    (
        "        //if it is message trace data,then it doesn't recorded",
        "        // 轨迹 Topic 自身消息不记录",
    ),
    (
        "        //build the context content of TraceContext",
        "        // 创建 Pub 轨迹上下文",
    ),
    (
        "        //build the data bean object of message trace",
        "        // 填充 topic、tags、body 长度等 TraceBean 字段",
    ),
    (
        "    public void sendMessageAfter(SendMessageContext context) {",
        "    /** 发送后：补充 msgId、耗时与成功标志，append 到 TraceDispatcher。 */\n    public void sendMessageAfter(SendMessageContext context) {",
    ),
    (
        "            // if switch is false,skip it",
        "            // region 为空或 trace 开关关闭则跳过",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/utils/MessageUtil.java"] = [
    (
        "public class MessageUtil {",
        "/** 消息工具类：构建 Request-Reply 模式的回复消息及读取 reply-to 客户端标识。 */\npublic class MessageUtil {",
    ),
    (
        "    public static Message createReplyMessage(final Message requestMessage, final byte[] body) throws MQClientException {",
        "    /**\n     * 根据请求消息创建回复消息：复制 cluster、correlationId、replyTo、TTL 等属性，\n     * 并设置 Reply Topic 与消息类型标记。\n     *\n     * @param requestMessage 原始请求消息\n     * @param body 回复体\n     * @return 配置完成的回复 Message\n     * @throws MQClientException cluster 缺失或 requestMessage 为 null\n     */",
    ),
    (
        "    public static String getReplyToClient(final Message msg) {",
        "    /** 读取消息属性中的 reply-to 客户端标识。 */\n    public static String getReplyToClient(final Message msg) {",
    ),
]
