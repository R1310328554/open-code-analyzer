"""RocketMQ 5.5.0 wave9b client exception/hook/impl [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["client/src/main/java/org/apache/rocketmq/client/exception/RequestTimeoutException.java"] = [
    (
        "public class RequestTimeoutException extends Exception {",
        "/**\n * 请求超时异常：客户端向 Broker/NameServer 发起 Remoting 请求未在时限内收到响应时抛出。\n * 携带响应码与错误描述，便于定位网络或 Broker 侧超时。\n */\npublic class RequestTimeoutException extends Exception {",
    ),
    (
        "    private int responseCode;",
        "    /** Remoting 响应码，未知时为 -1。 */\n    private int responseCode;",
    ),
    (
        "    private String errorMessage;",
        "    /** 错误描述信息。 */\n    private String errorMessage;",
    ),
    (
        "    public RequestTimeoutException(String errorMessage, Throwable cause) {",
        "    /** 以自定义消息与根因构造超时异常，响应码默认为 -1。 */\n    public RequestTimeoutException(String errorMessage, Throwable cause) {",
    ),
    (
        "    public RequestTimeoutException(int responseCode, String errorMessage) {",
        "    /** 以响应码与错误描述构造；异常消息格式为 CODE/DESC。 */\n    public RequestTimeoutException(int responseCode, String errorMessage) {",
    ),
    (
        "    public int getResponseCode() {",
        "    /** 返回 Remoting 响应码。 */\n    public int getResponseCode() {",
    ),
    (
        "    public RequestTimeoutException setResponseCode(final int responseCode) {",
        "    /** 设置响应码并返回自身，便于链式调用。 */\n    public RequestTimeoutException setResponseCode(final int responseCode) {",
    ),
    (
        "    public String getErrorMessage() {",
        "    /** 返回错误描述。 */\n    public String getErrorMessage() {",
    ),
    (
        "    public void setErrorMessage(final String errorMessage) {",
        "    /** 设置错误描述。 */\n    public void setErrorMessage(final String errorMessage) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/hook/CheckForbiddenContext.java"] = [
    (
        "public class CheckForbiddenContext {",
        "/**\n * 发送禁发校验钩子上下文：在消息真正发往 Broker 前，向 {@link CheckForbiddenHook}\n * 传递 NameServer 地址、Producer 组、目标队列及通信模式等信息。\n */\npublic class CheckForbiddenContext {",
    ),
    (
        "    private String nameSrvAddr;",
        "    /** NameServer 地址。 */\n    private String nameSrvAddr;",
    ),
    (
        "    private String group;",
        "    /** Producer 组名。 */\n    private String group;",
    ),
    (
        "    private Message message;",
        "    /** 待发送消息。 */\n    private Message message;",
    ),
    (
        "    private MessageQueue mq;",
        "    /** 目标消息队列。 */\n    private MessageQueue mq;",
    ),
    (
        "    private String brokerAddr;",
        "    /** 目标 Broker 地址。 */\n    private String brokerAddr;",
    ),
    (
        "    private CommunicationMode communicationMode;",
        "    /** 发送通信模式（同步/异步/单向）。 */\n    private CommunicationMode communicationMode;",
    ),
    (
        "    private SendResult sendResult;",
        "    /** 发送结果（钩子执行后可能回填）。 */\n    private SendResult sendResult;",
    ),
    (
        "    private Exception exception;",
        "    /** 发送过程异常（若有）。 */\n    private Exception exception;",
    ),
    (
        "    private Object arg;",
        "    /** 用户自定义扩展参数。 */\n    private Object arg;",
    ),
    (
        "    private boolean unitMode = false;",
        "    /** 是否单元化部署模式。 */\n    private boolean unitMode = false;",
    ),
    (
        "    public String getGroup() {",
        "    /** 返回 Producer 组名。 */\n    public String getGroup() {",
    ),
    (
        "    public void setGroup(String group) {",
        "    /** 设置 Producer 组名。 */\n    public void setGroup(String group) {",
    ),
    (
        "    public Message getMessage() {",
        "    /** 返回待发送消息。 */\n    public Message getMessage() {",
    ),
    (
        "    public void setMessage(Message message) {",
        "    /** 设置待发送消息。 */\n    public void setMessage(Message message) {",
    ),
    (
        "    public MessageQueue getMq() {",
        "    /** 返回目标队列。 */\n    public MessageQueue getMq() {",
    ),
    (
        "    public void setMq(MessageQueue mq) {",
        "    /** 设置目标队列。 */\n    public void setMq(MessageQueue mq) {",
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
        "    public CommunicationMode getCommunicationMode() {",
        "    /** 返回通信模式。 */\n    public CommunicationMode getCommunicationMode() {",
    ),
    (
        "    public void setCommunicationMode(CommunicationMode communicationMode) {",
        "    /** 设置通信模式。 */\n    public void setCommunicationMode(CommunicationMode communicationMode) {",
    ),
    (
        "    public SendResult getSendResult() {",
        "    /** 返回发送结果。 */\n    public SendResult getSendResult() {",
    ),
    (
        "    public void setSendResult(SendResult sendResult) {",
        "    /** 设置发送结果。 */\n    public void setSendResult(SendResult sendResult) {",
    ),
    (
        "    public Exception getException() {",
        "    /** 返回发送异常。 */\n    public Exception getException() {",
    ),
    (
        "    public void setException(Exception exception) {",
        "    /** 设置发送异常。 */\n    public void setException(Exception exception) {",
    ),
    (
        "    public Object getArg() {",
        "    /** 返回扩展参数。 */\n    public Object getArg() {",
    ),
    (
        "    public void setArg(Object arg) {",
        "    /** 设置扩展参数。 */\n    public void setArg(Object arg) {",
    ),
    (
        "    public boolean isUnitMode() {",
        "    /** 是否单元化模式。 */\n    public boolean isUnitMode() {",
    ),
    (
        "    public void setUnitMode(boolean isUnitMode) {",
        "    /** 设置是否单元化模式。 */\n    public void setUnitMode(boolean isUnitMode) {",
    ),
    (
        "    public String getNameSrvAddr() {",
        "    /** 返回 NameServer 地址。 */\n    public String getNameSrvAddr() {",
    ),
    (
        "    public void setNameSrvAddr(String nameSrvAddr) {",
        "    /** 设置 NameServer 地址。 */\n    public void setNameSrvAddr(String nameSrvAddr) {",
    ),
    (
        "    @Override\n    public String toString() {",
        "    /** 返回便于日志排查的字符串表示。 */\n    @Override\n    public String toString() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/hook/CheckForbiddenHook.java"] = [
    (
        "public interface CheckForbiddenHook {",
        "/**\n * 发送禁发校验钩子：在 Producer 发送消息前检查是否允许发往指定 Topic/队列，\n * 不满足策略时抛出 {@link MQClientException} 阻断发送。\n */\npublic interface CheckForbiddenHook {",
    ),
    (
        "    String hookName();",
        "    /** 返回钩子唯一名称，用于注册与排查。 */\n    String hookName();",
    ),
    (
        "    void checkForbidden(final CheckForbiddenContext context) throws MQClientException;",
        "    /** 执行禁发校验；不允许发送时抛出 {@link MQClientException}。 */\n    void checkForbidden(final CheckForbiddenContext context) throws MQClientException;",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/hook/ConsumeMessageContext.java"] = [
    (
        "public class ConsumeMessageContext {",
        "/**\n * 消费消息钩子上下文：记录一次消费批次的消息列表、队列、成功标志及\n * 链路追踪/命名空间等元数据，供 {@link ConsumeMessageHook} 前后回调使用。\n */\npublic class ConsumeMessageContext {",
    ),
    (
        "    private String consumerGroup;",
        "    /** 消费者组名。 */\n    private String consumerGroup;",
    ),
    (
        "    private List<MessageExt> msgList;",
        "    /** 本批次待消费或已消费的消息列表。 */\n    private List<MessageExt> msgList;",
    ),
    (
        "    private MessageQueue mq;",
        "    /** 消息来源队列。 */\n    private MessageQueue mq;",
    ),
    (
        "    private boolean success;",
        "    /** 消费是否成功（After 钩子中回填）。 */\n    private boolean success;",
    ),
    (
        "    private String status;",
        "    /** 消费状态描述（如 RECONSUME_LATER）。 */\n    private String status;",
    ),
    (
        "    private Object mqTraceContext;",
        "    /** 消息轨迹追踪上下文。 */\n    private Object mqTraceContext;",
    ),
    (
        "    private Map<String, String> props;",
        "    /** 扩展属性键值对。 */\n    private Map<String, String> props;",
    ),
    (
        "    private String namespace;",
        "    /** 命名空间（多租户隔离）。 */\n    private String namespace;",
    ),
    (
        "    private AccessChannel accessChannel;",
        "    /** 接入通道类型（LOCAL/CLOUD 等）。 */\n    private AccessChannel accessChannel;",
    ),
    (
        "    public String getConsumerGroup() {",
        "    /** 返回消费者组。 */\n    public String getConsumerGroup() {",
    ),
    (
        "    public void setConsumerGroup(String consumerGroup) {",
        "    /** 设置消费者组。 */\n    public void setConsumerGroup(String consumerGroup) {",
    ),
    (
        "    public List<MessageExt> getMsgList() {",
        "    /** 返回消息列表。 */\n    public List<MessageExt> getMsgList() {",
    ),
    (
        "    public void setMsgList(List<MessageExt> msgList) {",
        "    /** 设置消息列表。 */\n    public void setMsgList(List<MessageExt> msgList) {",
    ),
    (
        "    public MessageQueue getMq() {",
        "    /** 返回消息队列。 */\n    public MessageQueue getMq() {",
    ),
    (
        "    public void setMq(MessageQueue mq) {",
        "    /** 设置消息队列。 */\n    public void setMq(MessageQueue mq) {",
    ),
    (
        "    public boolean isSuccess() {",
        "    /** 消费是否成功。 */\n    public boolean isSuccess() {",
    ),
    (
        "    public void setSuccess(boolean success) {",
        "    /** 设置消费成功标志。 */\n    public void setSuccess(boolean success) {",
    ),
    (
        "    public Object getMqTraceContext() {",
        "    /** 返回轨迹追踪上下文。 */\n    public Object getMqTraceContext() {",
    ),
    (
        "    public void setMqTraceContext(Object mqTraceContext) {",
        "    /** 设置轨迹追踪上下文。 */\n    public void setMqTraceContext(Object mqTraceContext) {",
    ),
    (
        "    public Map<String, String> getProps() {",
        "    /** 返回扩展属性。 */\n    public Map<String, String> getProps() {",
    ),
    (
        "    public void setProps(Map<String, String> props) {",
        "    /** 设置扩展属性。 */\n    public void setProps(Map<String, String> props) {",
    ),
    (
        "    public String getStatus() {",
        "    /** 返回消费状态。 */\n    public String getStatus() {",
    ),
    (
        "    public void setStatus(String status) {",
        "    /** 设置消费状态。 */\n    public void setStatus(String status) {",
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
        "    public AccessChannel getAccessChannel() {",
        "    /** 返回接入通道。 */\n    public AccessChannel getAccessChannel() {",
    ),
    (
        "    public void setAccessChannel(AccessChannel accessChannel) {",
        "    /** 设置接入通道。 */\n    public void setAccessChannel(AccessChannel accessChannel) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/hook/ConsumeMessageHook.java"] = [
    (
        "public interface ConsumeMessageHook {",
        "/**\n * 消费消息钩子：在 Push/Pull 消费者执行业务监听器前后插入自定义逻辑，\n * 常用于监控、审计与消息轨迹上报。\n */\npublic interface ConsumeMessageHook {",
    ),
    (
        "    String hookName();",
        "    /** 返回钩子唯一名称。 */\n    String hookName();",
    ),
    (
        "    void consumeMessageBefore(final ConsumeMessageContext context);",
        "    /** 消费监听器执行前回调。 */\n    void consumeMessageBefore(final ConsumeMessageContext context);",
    ),
    (
        "    void consumeMessageAfter(final ConsumeMessageContext context);",
        "    /** 消费监听器执行后回调（含成功/失败状态）。 */\n    void consumeMessageAfter(final ConsumeMessageContext context);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/hook/EndTransactionContext.java"] = [
    (
        "public class EndTransactionContext {",
        "/**\n * 事务消息结束钩子上下文：记录半消息提交/回滚时的 Producer 组、消息体、\n * Broker 地址及本地事务状态，供 {@link EndTransactionHook} 使用。\n */\npublic class EndTransactionContext {",
    ),
    (
        "    private String producerGroup;",
        "    /** 事务 Producer 组名。 */\n    private String producerGroup;",
    ),
    (
        "    private Message message;",
        "    /** 半消息或事务消息体。 */\n    private Message message;",
    ),
    (
        "    private String brokerAddr;",
        "    /** 处理事务的 Broker 地址。 */\n    private String brokerAddr;",
    ),
    (
        "    private String msgId;",
        "    /** 消息 ID。 */\n    private String msgId;",
    ),
    (
        "    private String transactionId;",
        "    /** 事务 ID。 */\n    private String transactionId;",
    ),
    (
        "    private LocalTransactionState transactionState;",
        "    /** 本地事务最终状态（COMMIT/ROLLBACK/UNKNOWN）。 */\n    private LocalTransactionState transactionState;",
    ),
    (
        "    private boolean fromTransactionCheck;",
        "    /** 是否由 Broker 事务回查触发（而非 Producer 主动提交）。 */\n    private boolean fromTransactionCheck;",
    ),
    (
        "    public String getProducerGroup() {",
        "    /** 返回 Producer 组。 */\n    public String getProducerGroup() {",
    ),
    (
        "    public void setProducerGroup(String producerGroup) {",
        "    /** 设置 Producer 组。 */\n    public void setProducerGroup(String producerGroup) {",
    ),
    (
        "    public Message getMessage() {",
        "    /** 返回消息体。 */\n    public Message getMessage() {",
    ),
    (
        "    public void setMessage(Message message) {",
        "    /** 设置消息体。 */\n    public void setMessage(Message message) {",
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
        "    public String getMsgId() {",
        "    /** 返回消息 ID。 */\n    public String getMsgId() {",
    ),
    (
        "    public void setMsgId(String msgId) {",
        "    /** 设置消息 ID。 */\n    public void setMsgId(String msgId) {",
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
        "    public LocalTransactionState getTransactionState() {",
        "    /** 返回本地事务状态。 */\n    public LocalTransactionState getTransactionState() {",
    ),
    (
        "    public void setTransactionState(LocalTransactionState transactionState) {",
        "    /** 设置本地事务状态。 */\n    public void setTransactionState(LocalTransactionState transactionState) {",
    ),
    (
        "    public boolean isFromTransactionCheck() {",
        "    /** 是否来自 Broker 事务回查。 */\n    public boolean isFromTransactionCheck() {",
    ),
    (
        "    public void setFromTransactionCheck(boolean fromTransactionCheck) {",
        "    /** 设置是否来自事务回查。 */\n    public void setFromTransactionCheck(boolean fromTransactionCheck) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/hook/EndTransactionHook.java"] = [
    (
        "public interface EndTransactionHook {",
        "/**\n * 事务消息结束钩子：在 Producer 向 Broker 提交或回滚半消息后触发，\n * 用于监控、审计事务消息生命周期。\n */\npublic interface EndTransactionHook {",
    ),
    (
        "    String hookName();",
        "    /** 返回钩子唯一名称。 */\n    String hookName();",
    ),
    (
        "    void endTransaction(final EndTransactionContext context);",
        "    /** 事务结束（提交/回滚）时回调。 */\n    void endTransaction(final EndTransactionContext context);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/hook/FilterMessageContext.java"] = [
    (
        "public class FilterMessageContext {",
        "/**\n * 消息过滤钩子上下文：Pull 消费在业务监听器之前，向 {@link FilterMessageHook}\n * 传递待过滤的消息批次与队列信息，支持自定义二次过滤逻辑。\n */\npublic class FilterMessageContext {",
    ),
    (
        "    private String consumerGroup;",
        "    /** 消费者组名。 */\n    private String consumerGroup;",
    ),
    (
        "    private List<MessageExt> msgList;",
        "    /** 待过滤的消息列表（可被钩子修改）。 */\n    private List<MessageExt> msgList;",
    ),
    (
        "    private MessageQueue mq;",
        "    /** 消息来源队列。 */\n    private MessageQueue mq;",
    ),
    (
        "    private Object arg;",
        "    /** 用户自定义扩展参数。 */\n    private Object arg;",
    ),
    (
        "    private boolean unitMode;",
        "    /** 是否单元化部署模式。 */\n    private boolean unitMode;",
    ),
    (
        "    public String getConsumerGroup() {",
        "    /** 返回消费者组。 */\n    public String getConsumerGroup() {",
    ),
    (
        "    public void setConsumerGroup(String consumerGroup) {",
        "    /** 设置消费者组。 */\n    public void setConsumerGroup(String consumerGroup) {",
    ),
    (
        "    public List<MessageExt> getMsgList() {",
        "    /** 返回消息列表。 */\n    public List<MessageExt> getMsgList() {",
    ),
    (
        "    public void setMsgList(List<MessageExt> msgList) {",
        "    /** 设置消息列表。 */\n    public void setMsgList(List<MessageExt> msgList) {",
    ),
    (
        "    public MessageQueue getMq() {",
        "    /** 返回消息队列。 */\n    public MessageQueue getMq() {",
    ),
    (
        "    public void setMq(MessageQueue mq) {",
        "    /** 设置消息队列。 */\n    public void setMq(MessageQueue mq) {",
    ),
    (
        "    public Object getArg() {",
        "    /** 返回扩展参数。 */\n    public Object getArg() {",
    ),
    (
        "    public void setArg(Object arg) {",
        "    /** 设置扩展参数。 */\n    public void setArg(Object arg) {",
    ),
    (
        "    public boolean isUnitMode() {",
        "    /** 是否单元化模式。 */\n    public boolean isUnitMode() {",
    ),
    (
        "    public void setUnitMode(boolean isUnitMode) {",
        "    /** 设置是否单元化模式。 */\n    public void setUnitMode(boolean isUnitMode) {",
    ),
    (
        "    @Override\n    public String toString() {",
        "    /** 返回便于日志排查的字符串表示。 */\n    @Override\n    public String toString() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/hook/FilterMessageHook.java"] = [
    (
        "public interface FilterMessageHook {",
        "/**\n * 消息过滤钩子：在消费监听器之前对拉取到的消息批次做二次过滤或改写，\n * 常用于灰度、租户隔离等场景。\n */\npublic interface FilterMessageHook {",
    ),
    (
        "    String hookName();",
        "    /** 返回钩子唯一名称。 */\n    String hookName();",
    ),
    (
        "    void filterMessage(final FilterMessageContext context);",
        "    /** 执行过滤逻辑，可修改 context 中的 msgList。 */\n    void filterMessage(final FilterMessageContext context);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/hook/SendMessageContext.java"] = [
    (
        "public class SendMessageContext {",
        "/**\n * 发送消息钩子上下文：记录 Producer 单次 send 的目标队列、Broker 地址、\n * 通信模式、发送结果及轨迹信息，供 {@link SendMessageHook} 前后回调。\n */\npublic class SendMessageContext {",
    ),
    (
        "    private String producerGroup;",
        "    /** Producer 组名。 */\n    private String producerGroup;",
    ),
    (
        "    private Message message;",
        "    /** 待发送消息。 */\n    private Message message;",
    ),
    (
        "    private MessageQueue mq;",
        "    /** 目标消息队列。 */\n    private MessageQueue mq;",
    ),
    (
        "    private String brokerAddr;",
        "    /** 目标 Broker 地址。 */\n    private String brokerAddr;",
    ),
    (
        "    private String bornHost;",
        "    /** 消息产生主机地址。 */\n    private String bornHost;",
    ),
    (
        "    private CommunicationMode communicationMode;",
        "    /** 发送通信模式。 */\n    private CommunicationMode communicationMode;",
    ),
    (
        "    private SendResult sendResult;",
        "    /** 发送结果（After 钩子中回填）。 */\n    private SendResult sendResult;",
    ),
    (
        "    private Exception exception;",
        "    /** 发送异常（若有）。 */\n    private Exception exception;",
    ),
    (
        "    private Object mqTraceContext;",
        "    /** 消息轨迹追踪上下文。 */\n    private Object mqTraceContext;",
    ),
    (
        "    private Map<String, String> props;",
        "    /** 扩展属性键值对。 */\n    private Map<String, String> props;",
    ),
    (
        "    private DefaultMQProducerImpl producer;",
        "    /** 关联的 Producer 实现实例。 */\n    private DefaultMQProducerImpl producer;",
    ),
    (
        "    private MessageType msgType = MessageType.Normal_Msg;",
        "    /** 消息类型（普通/事务/延迟等）。 */\n    private MessageType msgType = MessageType.Normal_Msg;",
    ),
    (
        "    private String namespace;",
        "    /** 命名空间。 */\n    private String namespace;",
    ),
    (
        "    public MessageType getMsgType() {",
        "    /** 返回消息类型。 */\n    public MessageType getMsgType() {",
    ),
    (
        "    public void setMsgType(final MessageType msgType) {",
        "    /** 设置消息类型。 */\n    public void setMsgType(final MessageType msgType) {",
    ),
    (
        "    public DefaultMQProducerImpl getProducer() {",
        "    /** 返回 Producer 实现。 */\n    public DefaultMQProducerImpl getProducer() {",
    ),
    (
        "    public void setProducer(final DefaultMQProducerImpl producer) {",
        "    /** 设置 Producer 实现。 */\n    public void setProducer(final DefaultMQProducerImpl producer) {",
    ),
    (
        "    public String getProducerGroup() {",
        "    /** 返回 Producer 组。 */\n    public String getProducerGroup() {",
    ),
    (
        "    public void setProducerGroup(String producerGroup) {",
        "    /** 设置 Producer 组。 */\n    public void setProducerGroup(String producerGroup) {",
    ),
    (
        "    public Message getMessage() {",
        "    /** 返回待发送消息。 */\n    public Message getMessage() {",
    ),
    (
        "    public void setMessage(Message message) {",
        "    /** 设置待发送消息。 */\n    public void setMessage(Message message) {",
    ),
    (
        "    public MessageQueue getMq() {",
        "    /** 返回目标队列。 */\n    public MessageQueue getMq() {",
    ),
    (
        "    public void setMq(MessageQueue mq) {",
        "    /** 设置目标队列。 */\n    public void setMq(MessageQueue mq) {",
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
        "    public CommunicationMode getCommunicationMode() {",
        "    /** 返回通信模式。 */\n    public CommunicationMode getCommunicationMode() {",
    ),
    (
        "    public void setCommunicationMode(CommunicationMode communicationMode) {",
        "    /** 设置通信模式。 */\n    public void setCommunicationMode(CommunicationMode communicationMode) {",
    ),
    (
        "    public SendResult getSendResult() {",
        "    /** 返回发送结果。 */\n    public SendResult getSendResult() {",
    ),
    (
        "    public void setSendResult(SendResult sendResult) {",
        "    /** 设置发送结果。 */\n    public void setSendResult(SendResult sendResult) {",
    ),
    (
        "    public Exception getException() {",
        "    /** 返回发送异常。 */\n    public Exception getException() {",
    ),
    (
        "    public void setException(Exception exception) {",
        "    /** 设置发送异常。 */\n    public void setException(Exception exception) {",
    ),
    (
        "    public Object getMqTraceContext() {",
        "    /** 返回轨迹追踪上下文。 */\n    public Object getMqTraceContext() {",
    ),
    (
        "    public void setMqTraceContext(Object mqTraceContext) {",
        "    /** 设置轨迹追踪上下文。 */\n    public void setMqTraceContext(Object mqTraceContext) {",
    ),
    (
        "    public Map<String, String> getProps() {",
        "    /** 返回扩展属性。 */\n    public Map<String, String> getProps() {",
    ),
    (
        "    public void setProps(Map<String, String> props) {",
        "    /** 设置扩展属性。 */\n    public void setProps(Map<String, String> props) {",
    ),
    (
        "    public String getBornHost() {",
        "    /** 返回消息产生主机。 */\n    public String getBornHost() {",
    ),
    (
        "    public void setBornHost(String bornHost) {",
        "    /** 设置消息产生主机。 */\n    public void setBornHost(String bornHost) {",
    ),
    (
        "    public String getNamespace() {",
        "    /** 返回命名空间。 */\n    public String getNamespace() {",
    ),
    (
        "    public void setNamespace(String namespace) {",
        "    /** 设置命名空间。 */\n    public void setNamespace(String namespace) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/hook/SendMessageHook.java"] = [
    (
        "public interface SendMessageHook {",
        "/**\n * 发送消息钩子：在 Producer 向 Broker 发送消息前后插入自定义逻辑，\n * 常用于监控、限流、消息轨迹与审计。\n */\npublic interface SendMessageHook {",
    ),
    (
        "    String hookName();",
        "    /** 返回钩子唯一名称。 */\n    String hookName();",
    ),
    (
        "    void sendMessageBefore(final SendMessageContext context);",
        "    /** 发送请求发出前回调。 */\n    void sendMessageBefore(final SendMessageContext context);",
    ),
    (
        "    void sendMessageAfter(final SendMessageContext context);",
        "    /** 发送完成后回调（含结果或异常）。 */\n    void sendMessageAfter(final SendMessageContext context);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/ClientRemotingProcessor.java"] = [
    (
        "public class ClientRemotingProcessor implements NettyRequestProcessor {",
        "/**\n * 客户端 Remoting 入站处理器：处理 Broker 主动下发的请求，\n * 包括事务回查、消费组变更通知、Offset 重置、Request-Reply 响应等。\n */\npublic class ClientRemotingProcessor implements NettyRequestProcessor {",
    ),
    (
        "    private final Logger logger = LoggerFactory.getLogger(ClientRemotingProcessor.class);",
        "    /** 日志记录器。 */\n    private final Logger logger = LoggerFactory.getLogger(ClientRemotingProcessor.class);",
    ),
    (
        "    private final MQClientInstance mqClientFactory;",
        "    /** 所属 {@link MQClientInstance} 工厂。 */\n    private final MQClientInstance mqClientFactory;",
    ),
    (
        "    public ClientRemotingProcessor(final MQClientInstance mqClientFactory) {",
        "    /** 绑定客户端实例。 */\n    public ClientRemotingProcessor(final MQClientInstance mqClientFactory) {",
    ),
    (
        "    @Override\n    public RemotingCommand processRequest(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
        "    /** 按请求码分发到对应处理方法。 */\n    @Override\n    public RemotingCommand processRequest(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
    ),
    (
        "            case RequestCode.CHECK_TRANSACTION_STATE:",
        "            case RequestCode.CHECK_TRANSACTION_STATE: // 事务状态回查",
    ),
    (
        "            case RequestCode.NOTIFY_CONSUMER_IDS_CHANGED:",
        "            case RequestCode.NOTIFY_CONSUMER_IDS_CHANGED: // 消费实例列表变更",
    ),
    (
        "            case RequestCode.RESET_CONSUMER_CLIENT_OFFSET:",
        "            case RequestCode.RESET_CONSUMER_CLIENT_OFFSET: // 重置消费位点",
    ),
    (
        "            case RequestCode.GET_CONSUMER_STATUS_FROM_CLIENT:",
        "            case RequestCode.GET_CONSUMER_STATUS_FROM_CLIENT: // 查询消费进度（已废弃）",
    ),
    (
        "            case RequestCode.GET_CONSUMER_RUNNING_INFO:",
        "            case RequestCode.GET_CONSUMER_RUNNING_INFO: // 获取消费者运行信息",
    ),
    (
        "            case RequestCode.CONSUME_MESSAGE_DIRECTLY:",
        "            case RequestCode.CONSUME_MESSAGE_DIRECTLY: // 直接消费单条消息（运维）",
    ),
    (
        "            case RequestCode.PUSH_REPLY_MESSAGE_TO_CLIENT:",
        "            case RequestCode.PUSH_REPLY_MESSAGE_TO_CLIENT: // Request-Reply 响应推送",
    ),
    (
        "    @Override\n    public boolean rejectRequest() {",
        "    /** 是否拒绝处理新请求；客户端始终返回 false。 */\n    @Override\n    public boolean rejectRequest() {",
    ),
    (
        "    public RemotingCommand checkTransactionState(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
        "    /** 处理 Broker 发起的事务状态回查，委托对应 Producer 执行本地事务检查。 */\n    public RemotingCommand checkTransactionState(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
    ),
    (
        "    public RemotingCommand notifyConsumerIdsChanged(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
        "    /** 消费组在线实例变更时触发立即 Rebalance。 */\n    public RemotingCommand notifyConsumerIdsChanged(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
    ),
    (
        "    public RemotingCommand resetOffset(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
        "    /** 按 Broker 指令重置指定 Topic/Group 的消费位点。 */\n    public RemotingCommand resetOffset(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
    ),
    (
        "    @Deprecated\n    public RemotingCommand getConsumeStatus(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
        "    /** 查询消费进度（已废弃，保留兼容）。 */\n    @Deprecated\n    public RemotingCommand getConsumeStatus(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
    ),
    (
        "    private RemotingCommand getConsumerRunningInfo(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
        "    /** 返回消费者运行快照，可选附带 JVM 线程栈。 */\n    private RemotingCommand getConsumerRunningInfo(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
    ),
    (
        "    private RemotingCommand consumeMessageDirectly(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
        "    /** 运维场景：绕过正常拉取流程直接消费单条消息。 */\n    private RemotingCommand consumeMessageDirectly(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
    ),
    (
        "    private RemotingCommand receiveReplyMessage(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
        "    /** 接收 Broker 推送的 Request-Reply 响应消息并匹配等待中的 Future。 */\n    private RemotingCommand receiveReplyMessage(ChannelHandlerContext ctx,\n        RemotingCommand request) throws RemotingCommandException {",
    ),
    (
        "    private void processReplyMessage(MessageExt replyMsg) {",
        "    /** 按 correlationId 将回复消息交给对应的 {@link RequestResponseFuture}。 */\n    private void processReplyMessage(MessageExt replyMsg) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/CommunicationMode.java"] = [
    (
        "public enum CommunicationMode {",
        "/**\n * Producer 发送消息的 Remoting 通信模式。\n */\npublic enum CommunicationMode {",
    ),
    (
        "    SYNC,",
        "    /** 同步发送：阻塞等待 Broker 响应。 */\n    SYNC,",
    ),
    (
        "    ASYNC,",
        "    /** 异步发送：通过回调通知结果。 */\n    ASYNC,",
    ),
    (
        "    ONEWAY,",
        "    /** 单向发送：不等待也不关心响应。 */\n    ONEWAY,",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/FindBrokerResult.java"] = [
    (
        "public class FindBrokerResult {",
        "/**\n * 路由查找 Broker 的结果：包含 Broker 地址、是否从节点及版本号。\n */\npublic class FindBrokerResult {",
    ),
    (
        "    private final String brokerAddr;",
        "    /** Broker 地址（ip:port）。 */\n    private final String brokerAddr;",
    ),
    (
        "    private final boolean slave;",
        "    /** 是否为 Broker 从节点。 */\n    private final boolean slave;",
    ),
    (
        "    private final int brokerVersion;",
        "    /** Broker 版本号，用于特性兼容判断。 */\n    private final int brokerVersion;",
    ),
    (
        "    public FindBrokerResult(String brokerAddr, boolean slave) {",
        "    /** 构造结果，版本号默认为 0。 */\n    public FindBrokerResult(String brokerAddr, boolean slave) {",
    ),
    (
        "    public FindBrokerResult(String brokerAddr, boolean slave, int brokerVersion) {",
        "    /** 构造结果并指定 Broker 版本。 */\n    public FindBrokerResult(String brokerAddr, boolean slave, int brokerVersion) {",
    ),
    (
        "    public String getBrokerAddr() {",
        "    /** 返回 Broker 地址。 */\n    public String getBrokerAddr() {",
    ),
    (
        "    public boolean isSlave() {",
        "    /** 是否从节点。 */\n    public boolean isSlave() {",
    ),
    (
        "    public int getBrokerVersion() {",
        "    /** 返回 Broker 版本号。 */\n    public int getBrokerVersion() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/MQClientManager.java"] = [
    (
        "public class MQClientManager {",
        "/**\n * 客户端实例管理器（单例）：按 clientId 复用 {@link MQClientInstance} 与\n * {@link ProduceAccumulator}，避免同一 JVM 内重复创建网络连接与后台线程。\n */\npublic class MQClientManager {",
    ),
    (
        "    private final static Logger log = LoggerFactory.getLogger(MQClientManager.class);",
        "    /** 日志记录器。 */\n    private final static Logger log = LoggerFactory.getLogger(MQClientManager.class);",
    ),
    (
        "    private static MQClientManager instance = new MQClientManager();",
        "    /** 全局单例。 */\n    private static MQClientManager instance = new MQClientManager();",
    ),
    (
        "    private AtomicInteger factoryIndexGenerator = new AtomicInteger();",
        "    /** 新建 MQClientInstance 时的递增序号。 */\n    private AtomicInteger factoryIndexGenerator = new AtomicInteger();",
    ),
    (
        "    private ConcurrentMap<String/* clientId */, MQClientInstance> factoryTable =\n        new ConcurrentHashMap<>();",
        "    /** clientId → MQClientInstance 映射表。 */\n    private ConcurrentMap<String/* clientId */, MQClientInstance> factoryTable =\n        new ConcurrentHashMap<>();",
    ),
    (
        "    private ConcurrentMap<String/* clientId */, ProduceAccumulator> accumulatorTable =\n        new ConcurrentHashMap<String, ProduceAccumulator>();",
        "    /** clientId → 发送累加器映射表。 */\n    private ConcurrentMap<String/* clientId */, ProduceAccumulator> accumulatorTable =\n        new ConcurrentHashMap<String, ProduceAccumulator>();",
    ),
    (
        "    private MQClientManager() {",
        "    /** 私有构造，禁止外部实例化。 */\n    private MQClientManager() {",
    ),
    (
        "    public static MQClientManager getInstance() {",
        "    /** 返回全局单例。 */\n    public static MQClientManager getInstance() {",
    ),
    (
        "    public MQClientInstance getOrCreateMQClientInstance(final ClientConfig clientConfig) {",
        "    /** 按配置获取或创建 MQClientInstance（无 RPC Hook）。 */\n    public MQClientInstance getOrCreateMQClientInstance(final ClientConfig clientConfig) {",
    ),
    (
        "    public MQClientInstance getOrCreateMQClientInstance(final ClientConfig clientConfig, RPCHook rpcHook) {",
        "    /** 按配置与 RPC Hook 获取或创建 MQClientInstance；同 clientId 复用已有实例。 */\n    public MQClientInstance getOrCreateMQClientInstance(final ClientConfig clientConfig, RPCHook rpcHook) {",
    ),
    (
        "    public ProduceAccumulator getOrCreateProduceAccumulator(final ClientConfig clientConfig) {",
        "    /** 按 clientId 获取或创建发送累加器。 */\n    public ProduceAccumulator getOrCreateProduceAccumulator(final ClientConfig clientConfig) {",
    ),
    (
        "    public void removeClientFactory(final String clientId) {",
        "    /** 从工厂表移除指定 clientId 的实例（关闭后清理）。 */\n    public void removeClientFactory(final String clientId) {",
    ),
    (
        "    public ConcurrentMap<String, MQClientInstance> getFactoryTable() {",
        "    /** 返回 MQClientInstance 工厂表（测试或监控用）。 */\n    public ConcurrentMap<String, MQClientInstance> getFactoryTable() {",
    ),
]
