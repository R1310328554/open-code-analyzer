"""RocketMQ 5.5.0 wave14b common filter/future/help/hook/lite/logging/message [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["common/src/main/java/org/apache/rocketmq/common/filter/impl/PolishExpr.java"] = [
    (
        "public class PolishExpr {",
        "/**\n * 逆波兰表达式（后缀表达式）工具：对过滤表达式分词并应用调度场算法（Shunting-yard）转换。\n */\npublic class PolishExpr {",
    ),
    (
        "    public static List<Op> reversePolish(String expression) {",
        "    /** 将表达式字符串分词后转换为逆波兰序列。 */\n    public static List<Op> reversePolish(String expression) {",
    ),
    (
        "    /**\n     * Shunting-yard algorithm <br/>\n     * http://en.wikipedia.org/wiki/Shunting_yard_algorithm\n     *\n     * @return the compute result of Shunting-yard algorithm\n     */",
        "    /**\n     * 调度场算法（Shunting-yard），将中缀 token 序列转为后缀序列。\n     * 参见 http://en.wikipedia.org/wiki/Shunting_yard_algorithm\n     *\n     * @param tokens 分词后的操作数/运算符 token 列表\n     * @return 逆波兰序列\n     */",
    ),
    (
        "    /**\n     * @param expression\n     * @return\n     * @throws Exception\n     */",
        "    /**\n     * 按字符扫描表达式，切分为操作数、运算符与括号 token。\n     *\n     * @param expression 原始表达式字符串\n     * @return token 列表\n     */",
    ),
    (
        "    public static boolean isOperand(Op token) {",
        "    /** 判断 token 是否为操作数。 */\n    public static boolean isOperand(Op token) {",
    ),
    (
        "    public static boolean isLeftParenthesis(Op token) {",
        "    /** 判断 token 是否为左括号。 */\n    public static boolean isLeftParenthesis(Op token) {",
    ),
    (
        "    public static boolean isRightParenthesis(Op token) {",
        "    /** 判断 token 是否为右括号。 */\n    public static boolean isRightParenthesis(Op token) {",
    ),
    (
        "    public static boolean isOperator(Op token) {",
        "    /** 判断 token 是否为运算符。 */\n    public static boolean isOperator(Op token) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/filter/impl/Type.java"] = [
    (
        "public enum Type {",
        "/**\n * 表达式分词过程中当前字符的语法类别。\n */\npublic enum Type {",
    ),
    (
        "    NULL,",
        "    /** 尚未识别任何字符。 */\n    NULL,",
    ),
    (
        "    OPERAND,",
        "    /** 操作数（标识符/字面量片段）。 */\n    OPERAND,",
    ),
    (
        "    OPERATOR,",
        "    /** 逻辑运算符（如 AND/OR）。 */\n    OPERATOR,",
    ),
    (
        "    PARENTHESIS,",
        "    /** 括号。 */\n    PARENTHESIS,",
    ),
    (
        "    SEPAERATOR;",
        "    /** 空白分隔符（空格/制表符）。 */\n    SEPAERATOR;",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/future/FutureTaskExt.java"] = [
    (
        "public class FutureTaskExt<V> extends FutureTask<V> {",
        "/**\n * 扩展 {@link FutureTask}，额外保留底层 {@link Runnable} 引用以便外部获取。\n *\n * @param <V> 异步任务结果类型\n */\npublic class FutureTaskExt<V> extends FutureTask<V> {",
    ),
    (
        "    private final Runnable runnable;",
        "    /** 构造时传入的 Runnable；Callable 构造路径下为 null。 */\n    private final Runnable runnable;",
    ),
    (
        "    public FutureTaskExt(final Callable<V> callable) {",
        "    /** 基于 Callable 创建 FutureTask 扩展。 */\n    public FutureTaskExt(final Callable<V> callable) {",
    ),
    (
        "    public FutureTaskExt(final Runnable runnable, final V result) {",
        "    /** 基于 Runnable 与预设结果值创建 FutureTask 扩展。 */\n    public FutureTaskExt(final Runnable runnable, final V result) {",
    ),
    (
        "    public Runnable getRunnable() {",
        "    /** 返回关联的 Runnable，Callable 路径下为 null。 */\n    public Runnable getRunnable() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/help/FAQUrl.java"] = [
    (
        "public class FAQUrl {",
        "/**\n * RocketMQ 常见错误对应的 FAQ 文档 URL 及错误信息拼接工具。\n */\npublic class FAQUrl {",
    ),
    (
        "    public static final String DEFAULT_FAQ_URL = \"https://rocketmq.apache.org/docs/bestPractice/06FAQ\";",
        "    /** 默认 FAQ 文档地址。 */\n    public static final String DEFAULT_FAQ_URL = \"https://rocketmq.apache.org/docs/bestPractice/06FAQ\";",
    ),
    (
        "    public static final String APPLY_TOPIC_URL = DEFAULT_FAQ_URL;",
        "    /** 申请 Topic 相关 FAQ。 */\n    public static final String APPLY_TOPIC_URL = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String NAME_SERVER_ADDR_NOT_EXIST_URL = DEFAULT_FAQ_URL;",
        "    /** NameServer 地址不存在 FAQ。 */\n    public static final String NAME_SERVER_ADDR_NOT_EXIST_URL = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String GROUP_NAME_DUPLICATE_URL = DEFAULT_FAQ_URL;",
        "    /** 消费组名重复 FAQ。 */\n    public static final String GROUP_NAME_DUPLICATE_URL = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String CLIENT_PARAMETER_CHECK_URL = DEFAULT_FAQ_URL;",
        "    /** 客户端参数校验失败 FAQ。 */\n    public static final String CLIENT_PARAMETER_CHECK_URL = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String SUBSCRIPTION_GROUP_NOT_EXIST = DEFAULT_FAQ_URL;",
        "    /** 订阅组不存在 FAQ。 */\n    public static final String SUBSCRIPTION_GROUP_NOT_EXIST = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String CLIENT_SERVICE_NOT_OK = DEFAULT_FAQ_URL;",
        "    /** 客户端服务不可用 FAQ。 */\n    public static final String CLIENT_SERVICE_NOT_OK = DEFAULT_FAQ_URL;",
    ),
    (
        "    // FAQ: No route info of this topic, TopicABC",
        "    // FAQ：无此 Topic 路由信息，例如 TopicABC",
    ),
    (
        "    public static final String NO_TOPIC_ROUTE_INFO = DEFAULT_FAQ_URL;",
        "    /** 无 Topic 路由信息 FAQ。 */\n    public static final String NO_TOPIC_ROUTE_INFO = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String LOAD_JSON_EXCEPTION = DEFAULT_FAQ_URL;",
        "    /** JSON 加载异常 FAQ。 */\n    public static final String LOAD_JSON_EXCEPTION = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String SAME_GROUP_DIFFERENT_TOPIC = DEFAULT_FAQ_URL;",
        "    /** 同组不同 Topic 订阅 FAQ。 */\n    public static final String SAME_GROUP_DIFFERENT_TOPIC = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String MQLIST_NOT_EXIST = DEFAULT_FAQ_URL;",
        "    /** 消息队列列表不存在 FAQ。 */\n    public static final String MQLIST_NOT_EXIST = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String UNEXPECTED_EXCEPTION_URL = DEFAULT_FAQ_URL;",
        "    /** 未预期异常默认 FAQ。 */\n    public static final String UNEXPECTED_EXCEPTION_URL = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String SEND_MSG_FAILED = DEFAULT_FAQ_URL;",
        "    /** 发送消息失败 FAQ。 */\n    public static final String SEND_MSG_FAILED = DEFAULT_FAQ_URL;",
    ),
    (
        "    public static final String UNKNOWN_HOST_EXCEPTION = DEFAULT_FAQ_URL;",
        "    /** 未知主机异常 FAQ。 */\n    public static final String UNKNOWN_HOST_EXCEPTION = DEFAULT_FAQ_URL;",
    ),
    (
        "    private static final String TIP_STRING_BEGIN = \"\\nSee \";",
        "    /** FAQ 提示前缀（英文，拼接在错误信息后）。 */\n    private static final String TIP_STRING_BEGIN = \"\\nSee \";",
    ),
    (
        "    private static final String TIP_STRING_END = \" for further details.\";",
        "    /** FAQ 提示后缀。 */\n    private static final String TIP_STRING_END = \" for further details.\";",
    ),
    (
        "    private static final String MORE_INFORMATION = \"For more information, please visit the url, \";",
        "    /** 附加默认 URL 时的引导语。 */\n    private static final String MORE_INFORMATION = \"For more information, please visit the url, \";",
    ),
    (
        "    public static String suggestTodo(final String url) {",
        "    /** 在错误信息后追加 \"See {url} for further details.\" 提示。 */\n    public static String suggestTodo(final String url) {",
    ),
    (
        "    public static String attachDefaultURL(final String errorMessage) {",
        "    /** 若错误信息尚未包含 FAQ 提示，则附加默认 FAQ URL。 */\n    public static String attachDefaultURL(final String errorMessage) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/hook/FilterCheckHook.java"] = [
    (
        "public interface FilterCheckHook {",
        "/**\n * 消息过滤校验钩子：在消费端对消息体进行额外匹配检查。\n */\npublic interface FilterCheckHook {",
    ),
    (
        "    String hookName();",
        "    /** 返回钩子名称，用于注册与日志标识。 */\n    String hookName();",
    ),
    (
        "    boolean isFilterMatched(final boolean isUnitMode, final ByteBuffer byteBuffer);",
        "    /**\n     * 判断消息体是否通过过滤条件。\n     *\n     * @param isUnitMode 是否单元化模式\n     * @param byteBuffer 消息体字节缓冲\n     * @return 匹配返回 true\n     */\n    boolean isFilterMatched(final boolean isUnitMode, final ByteBuffer byteBuffer);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/lite/LiteLagInfo.java"] = [
    (
        "public class LiteLagInfo {",
        "/**\n * Lite Topic 消费堆积信息：堆积条数与最早未消费消息时间戳。\n */\npublic class LiteLagInfo {",
    ),
    (
        "    private String liteTopic;",
        "    /** Lite Topic 名称。 */\n    private String liteTopic;",
    ),
    (
        "    private long lagCount;",
        "    /** 堆积消息条数。 */\n    private long lagCount;",
    ),
    (
        "    // earliest unconsumed timestamp",
        "    // 最早未消费消息的时间戳（毫秒），-1 表示未知",
    ),
    (
        "    private long earliestUnconsumedTimestamp = -1;",
        "    /** 最早未消费消息时间戳，默认 -1。 */\n    private long earliestUnconsumedTimestamp = -1;",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/lite/LiteSubscription.java"] = [
    (
        "public class LiteSubscription {",
        "/**\n * 消费组对某 Topic 下 Lite Topic 集合的订阅关系，线程安全维护 liteTopicSet。\n */\npublic class LiteSubscription {",
    ),
    (
        "    private String group;",
        "    /** 消费组名。 */\n    private String group;",
    ),
    (
        "    private String topic;",
        "    /** 父 Topic 名。 */\n    private String topic;",
    ),
    (
        "    private final Set<String> liteTopicSet = ConcurrentHashMap.newKeySet();",
        "    /** 已订阅的 Lite Topic 名称集合。 */\n    private final Set<String> liteTopicSet = ConcurrentHashMap.newKeySet();",
    ),
    (
        "    private volatile long updateTime = System.currentTimeMillis();",
        "    /** 最近一次变更时间戳（毫秒）。 */\n    private volatile long updateTime = System.currentTimeMillis();",
    ),
    (
        "    public boolean addLiteTopic(String liteTopic) {",
        "    /** 添加单个 Lite Topic 订阅并刷新 updateTime。 */\n    public boolean addLiteTopic(String liteTopic) {",
    ),
    (
        "    public void addLiteTopic(Collection<String> set) {",
        "    /** 批量添加 Lite Topic 订阅。 */\n    public void addLiteTopic(Collection<String> set) {",
    ),
    (
        "    public boolean removeLiteTopic(String liteTopic) {",
        "    /** 移除单个 Lite Topic 订阅。 */\n    public boolean removeLiteTopic(String liteTopic) {",
    ),
    (
        "    public void removeLiteTopic(Collection<String> set) {",
        "    /** 批量移除 Lite Topic 订阅。 */\n    public void removeLiteTopic(Collection<String> set) {",
    ),
    (
        "    private void updateTime() {",
        "    /** 将 updateTime 设为当前时间。 */\n    private void updateTime() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/lite/LiteSubscriptionAction.java"] = [
    (
        "public enum LiteSubscriptionAction {",
        "/**\n * Lite Topic 订阅变更动作类型。\n */\npublic enum LiteSubscriptionAction {",
    ),
    (
        "    PARTIAL_ADD,",
        "    /** 增量添加部分 Lite Topic。 */\n    PARTIAL_ADD,",
    ),
    (
        "    PARTIAL_REMOVE,",
        "    /** 增量移除部分 Lite Topic。 */\n    PARTIAL_REMOVE,",
    ),
    (
        "    COMPLETE_ADD,",
        "    /** 全量覆盖添加（替换整个订阅集合）。 */\n    COMPLETE_ADD,",
    ),
    (
        "    COMPLETE_REMOVE",
        "    /** 全量移除订阅。 */\n    COMPLETE_REMOVE",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/lite/LiteSubscriptionDTO.java"] = [
    (
        "public class LiteSubscriptionDTO {",
        "/**\n * Lite Topic 订阅变更传输对象：携带客户端、组、Topic、动作与偏移策略。\n */\npublic class LiteSubscriptionDTO {",
    ),
    (
        "    private LiteSubscriptionAction action;",
        "    /** 订阅变更动作。 */\n    private LiteSubscriptionAction action;",
    ),
    (
        "    private String clientId;",
        "    /** 发起变更的客户端 ID。 */\n    private String clientId;",
    ),
    (
        "    private String group;",
        "    /** 消费组名。 */\n    private String group;",
    ),
    (
        "    private String topic;",
        "    /** 父 Topic 名。 */\n    private String topic;",
    ),
    (
        "    private Set<String> liteTopicSet;",
        "    /** 涉及的 Lite Topic 集合。 */\n    private Set<String> liteTopicSet;",
    ),
    (
        "    private OffsetOption offsetOption;",
        "    /** 消费起始偏移策略。 */\n    private OffsetOption offsetOption;",
    ),
    (
        "    private long version;",
        "    /** 订阅版本号，用于并发控制。 */\n    private long version;",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/lite/LiteUtil.java"] = [
    (
        "public class LiteUtil {",
        "/**\n * Lite Topic 与 LMQ 队列名之间的编解码工具。\n * 命名模式：{@code %LMQ%$parentTopic$liteTopic}，{@code $} 为分隔符。\n */\npublic class LiteUtil {",
    ),
    (
        "    public static final char SEPARATOR = '$';",
        "    /** 父 Topic 与 Lite Topic 之间的分隔符。 */\n    public static final char SEPARATOR = '$';",
    ),
    (
        "    public static final String LITE_TOPIC_PREFIX = MixAll.LMQ_PREFIX + SEPARATOR;",
        "    /** Lite Topic 对应 LMQ 名前缀：{@code %LMQ%$}。 */\n    public static final String LITE_TOPIC_PREFIX = MixAll.LMQ_PREFIX + SEPARATOR;",
    ),
    (
        "    /**\n     * Lite Topic: A specific type of message topic implemented based on LMQ, which has no retry topic.\n     * A lite topic's underlying storage is a lmq (Light Message Queue),\n     * but the reverse is not true: lmq is not necessarily a lite topic,\n     * we use \"$\" as a separator to achieve the distinction and assume \"$\" is not allowed for topic name.\n     * pattern like: %LMQ%$parentTopic$liteTopic\n     *\n     * @param parentTopic act as namespace\n     * @param liteTopic here means child topic string\n     * @return lmqName",
        "    /**\n     * 将父 Topic 与 Lite Topic 编码为 LMQ 队列名。\n     * Lite Topic 基于 LMQ 实现且无重试 Topic；并非所有 LMQ 都是 Lite Topic。\n     * 格式示例：{@code %LMQ%$parentTopic$liteTopic}\n     *\n     * @param parentTopic 父 Topic，作命名空间\n     * @param liteTopic 子 Topic 字符串\n     * @return LMQ 队列名，参数为空时返回 null",
    ),
    (
        "    /**\n     * whether lmqName is queue of a lite topic, here we only check the prefix.\n     * @param lmqName\n     * @return\n     */",
        "    /**\n     * 判断 lmqName 是否为 Lite Topic 队列（仅检查前缀）。\n     *\n     * @param lmqName LMQ 队列名\n     * @return 是 Lite Topic 队列返回 true\n     */",
    ),
    (
        "    public static String getParentTopic(String lmqName) {",
        "    /** 从 LMQ 名解析父 Topic；格式不合法时返回 null。 */\n    public static String getParentTopic(String lmqName) {",
    ),
    (
        "    public static String getLiteTopic(String lmqName) {",
        "    /** 从 LMQ 名解析 Lite Topic 子名；格式不合法时返回 null。 */\n    public static String getLiteTopic(String lmqName) {",
    ),
    (
        "    /**\n     * %LMQ%${parentTopic}${liteTopic}\n     * parse parent topic and child topic from lmqName\n     * @param lmqName\n     * @return\n     */",
        "    /**\n     * 从 {@code %LMQ%$parentTopic$liteTopic} 解析父 Topic 与 Lite Topic。\n     *\n     * @param lmqName LMQ 队列名\n     * @return Pair(父 Topic, Lite Topic)，解析失败返回 null\n     */",
    ),
    (
        "    /**\n     * whether lmqName is queue of a lite topic and belongs to the specified parent,\n     * here we only check the prefix.\n     * @param lmqName\n     * @param parentTopic\n     * @return\n     */",
        "    /**\n     * 判断 lmqName 是否为指定父 Topic 下的 Lite Topic 队列（前缀匹配）。\n     *\n     * @param lmqName LMQ 队列名\n     * @param parentTopic 父 Topic\n     * @return 属于该父 Topic 返回 true\n     */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/lite/OffsetOption.java"] = [
    (
        "public class OffsetOption {",
        "/**\n * Lite 消费起始偏移选项：支持策略、绝对偏移、尾部 N 条与时间戳四种类型。\n */\npublic class OffsetOption {",
    ),
    (
        "    public static final long POLICY_LAST_VALUE = 0L;",
        "    /** 策略值：从最新位置消费。 */\n    public static final long POLICY_LAST_VALUE = 0L;",
    ),
    (
        "    public static final long POLICY_MIN_VALUE = 1L;",
        "    /** 策略值：从最早位置消费。 */\n    public static final long POLICY_MIN_VALUE = 1L;",
    ),
    (
        "    public static final long POLICY_MAX_VALUE = 2L;",
        "    /** 策略值：从最大偏移消费。 */\n    public static final long POLICY_MAX_VALUE = 2L;",
    ),
    (
        "    private Type type;",
        "    /** 偏移类型。 */\n    private Type type;",
    ),
    (
        "    private long value;",
        "    /** 偏移值或策略枚举值，含义取决于 type。 */\n    private long value;",
    ),
    (
        "    public OffsetOption(Type type, long value) {",
        "    /** 指定类型与数值构造偏移选项。 */\n    public OffsetOption(Type type, long value) {",
    ),
    (
        "    public enum Type {",
        "    /** 偏移选项类型。 */\n    public enum Type {",
    ),
    (
        "        POLICY,",
        "        /** 预定义策略（LAST/MIN/MAX）。 */\n        POLICY,",
    ),
    (
        "        OFFSET,",
        "        /** 绝对逻辑偏移。 */\n        OFFSET,",
    ),
    (
        "        TAIL_N,",
        "        /** 从队列尾部向前 N 条。 */\n        TAIL_N,",
    ),
    (
        "        TIMESTAMP",
        "        /** 按时间戳定位起始消费位点。 */\n        TIMESTAMP",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/logging/DefaultJoranConfiguratorExt.java"] = [
    (
        "public class DefaultJoranConfiguratorExt extends DefaultJoranConfigurator {",
        "/**\n * RocketMQ 专用 Logback 自动配置：按优先级搜索 rmq.*.logback.xml 配置文件。\n */\npublic class DefaultJoranConfiguratorExt extends DefaultJoranConfigurator {",
    ),
    (
        "    final public static String TEST_AUTOCONFIG_FILE = \"rmq.logback-test.xml\";",
        "    /** 测试环境 Logback 配置文件名。 */\n    final public static String TEST_AUTOCONFIG_FILE = \"rmq.logback-test.xml\";",
    ),
    (
        "    final public static String AUTOCONFIG_FILE = \"rmq.logback.xml\";",
        "    /** 通用 Logback 配置文件名。 */\n    final public static String AUTOCONFIG_FILE = \"rmq.logback.xml\";",
    ),
    (
        "    final public static String PROXY_AUTOCONFIG_FILE = \"rmq.proxy.logback.xml\";",
        "    /** Proxy 组件 Logback 配置。 */\n    final public static String PROXY_AUTOCONFIG_FILE = \"rmq.proxy.logback.xml\";",
    ),
    (
        "    final public static String BROKER_AUTOCONFIG_FILE = \"rmq.broker.logback.xml\";",
        "    /** Broker 组件 Logback 配置。 */\n    final public static String BROKER_AUTOCONFIG_FILE = \"rmq.broker.logback.xml\";",
    ),
    (
        "    final public static String NAMESRV_AUTOCONFIG_FILE = \"rmq.namesrv.logback.xml\";",
        "    /** NameServer 组件 Logback 配置。 */\n    final public static String NAMESRV_AUTOCONFIG_FILE = \"rmq.namesrv.logback.xml\";",
    ),
    (
        "    final public static String CONTROLLER_AUTOCONFIG_FILE = \"rmq.controller.logback.xml\";",
        "    /** Controller 组件 Logback 配置。 */\n    final public static String CONTROLLER_AUTOCONFIG_FILE = \"rmq.controller.logback.xml\";",
    ),
    (
        "    final public static String TOOLS_AUTOCONFIG_FILE = \"rmq.tools.logback.xml\";",
        "    /** 工具类 Logback 配置。 */\n    final public static String TOOLS_AUTOCONFIG_FILE = \"rmq.tools.logback.xml\";",
    ),
    (
        "    final public static String CLIENT_AUTOCONFIG_FILE = \"rmq.client.logback.xml\";",
        "    /** 客户端 Logback 配置。 */\n    final public static String CLIENT_AUTOCONFIG_FILE = \"rmq.client.logback.xml\";",
    ),
    (
        "    private final List<String> configFiles;",
        "    /** 按搜索顺序排列的配置文件名列表。 */\n    private final List<String> configFiles;",
    ),
    (
        "        // skip other configurator on purpose.",
        "        // 有意跳过后续 configurator 链。",
    ),
    (
        "    public void configureByResource(URL url) throws JoranException {",
        "    /** 加载 XML 资源并通过 {@link JoranConfiguratorExt} 完成 Logback 配置。 */\n    public void configureByResource(URL url) throws JoranException {",
    ),
    (
        "    public URL findURLOfDefaultConfigurationFile(boolean updateStatus) {",
        "    /**\n     * 查找默认 Logback 配置 URL：先系统属性，再按 configFiles 顺序搜索类路径。\n     *\n     * @param updateStatus 是否向 StatusManager 报告搜索过程\n     * @return 找到的配置 URL，未找到返回 null\n     */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/logging/JoranConfiguratorExt.java"] = [
    (
        "public class JoranConfiguratorExt extends JoranConfigurator {",
        "/**\n * Logback Joran 配置扩展：加载 XML 时将包名重映射为 RocketMQ 内置 logback 包。\n */\npublic class JoranConfiguratorExt extends JoranConfigurator {",
    ),
    (
        "    private InputStream transformXml(InputStream in) throws IOException {",
        "    /** 读取 XML 并将 {@code ch.qos.logback} 替换为 RocketMQ 重定位包名。 */\n    private InputStream transformXml(InputStream in) throws IOException {",
    ),
    (
        "    public final void doConfigure0(URL url) throws JoranException {",
        "    /** 打开 URL、转换 XML 包名后执行 Logback 配置。 */\n    public final void doConfigure0(URL url) throws JoranException {",
    ),
    (
        "            // per http://jira.qos.ch/browse/LBCORE-105\n            // per http://jira.qos.ch/browse/LBCORE-127",
        "            // 参见 LBCORE-105 / LBCORE-127：禁用 URL 连接缓存",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/Message.java"] = [
    (
        "public class Message implements Serializable {",
        "/**\n * RocketMQ 消息体：Topic、标签、键、用户属性、消息体字节及事务 ID 等。\n * 系统属性通过 {@link MessageConst} 键写入 properties Map。\n */\npublic class Message implements Serializable {",
    ),
    (
        "    private String topic;",
        "    /** 消息所属 Topic。 */\n    private String topic;",
    ),
    (
        "    private int flag;",
        "    /** 消息标志位（系统/业务自定义）。 */\n    private int flag;",
    ),
    (
        "    private Map<String, String> properties;",
        "    /** 系统属性与用户属性 Map。 */\n    private Map<String, String> properties;",
    ),
    (
        "    private byte[] body;",
        "    /** 消息体字节数组。 */\n    private byte[] body;",
    ),
    (
        "    private String transactionId;",
        "    /** 事务消息 ID。 */\n    private String transactionId;",
    ),
    (
        "    public Message(String topic, String tags, String keys, int flag, byte[] body, boolean waitStoreMsgOK) {",
        "    /**\n     * 完整构造：设置 Topic、标签、键、标志、消息体及是否等待存储确认。\n     */\n    public Message(String topic, String tags, String keys, int flag, byte[] body, boolean waitStoreMsgOK) {",
    ),
    (
        "    void putProperty(final String name, final String value) {",
        "    /** 写入系统/内部属性（懒初始化 properties）。 */\n    void putProperty(final String name, final String value) {",
    ),
    (
        "    void clearProperty(final String name) {",
        "    /** 移除指定属性键。 */\n    void clearProperty(final String name) {",
    ),
    (
        "    public void putUserProperty(final String name, final String value) {",
        "    /**\n     * 设置用户自定义属性；名称不得与 {@link MessageConst} 系统键冲突。\n     *\n     * @throws RuntimeException 属性名被系统占用\n     * @throws IllegalArgumentException 名或值为 null/空白\n     */",
    ),
    (
        "    public String getUserProperty(final String name) {",
        "    /** 获取用户属性，等价于 {@link #getProperty(String)}。 */\n    public String getUserProperty(final String name) {",
    ),
    (
        "    public int getDelayTimeLevel() {",
        "    /** 返回延迟级别（未设置时为 0）。 */\n    public int getDelayTimeLevel() {",
    ),
    (
        "    public void setDelayTimeLevel(int level) {",
        "    /** 设置延迟消息级别。 */\n    public void setDelayTimeLevel(int level) {",
    ),
    (
        "    public void setPriority(int priority) {",
        "    /** 设置消息优先级，须 >= 0。 */\n    public void setPriority(int priority) {",
    ),
    (
        "    public boolean isWaitStoreMsgOK() {",
        "    /** 发送时是否等待 Broker 存储确认，默认 true。 */\n    public boolean isWaitStoreMsgOK() {",
    ),
    (
        "    public void setInstanceId(String instanceId) {",
        "    /** 设置实例 ID（多实例隔离）。 */\n    public void setInstanceId(String instanceId) {",
    ),
    (
        "    void setProperties(Map<String, String> properties) {",
        "    /** 包内可见：整体替换 properties Map。 */\n    void setProperties(Map<String, String> properties) {",
    ),
    (
        "    public void setDelayTimeSec(long sec) {",
        "    /** 设置定时消息延迟秒数。 */\n    public void setDelayTimeSec(long sec) {",
    ),
    (
        "    public long getDelayTimeSec() {",
        "    /** 获取定时消息延迟秒数，未设置时为 0。 */\n    public long getDelayTimeSec() {",
    ),
    (
        "    public void setDelayTimeMs(long timeMs) {",
        "    /** 设置定时消息延迟毫秒数。 */\n    public void setDelayTimeMs(long timeMs) {",
    ),
    (
        "    public long getDelayTimeMs() {",
        "    /** 获取定时消息延迟毫秒数，未设置时为 0。 */\n    public long getDelayTimeMs() {",
    ),
    (
        "    public void setDeliverTimeMs(long timeMs) {",
        "    /** 设置绝对投递时间戳（毫秒）。 */\n    public void setDeliverTimeMs(long timeMs) {",
    ),
    (
        "    public long getDeliverTimeMs() {",
        "    /** 获取绝对投递时间戳，未设置时为 0。 */\n    public long getDeliverTimeMs() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageAccessor.java"] = [
    (
        "public class MessageAccessor {",
        "/**\n * {@link Message} 属性访问工具：读写系统属性、克隆消息及深拷贝 properties。\n * 供框架内部在不暴露 Message 包级方法时操作消息属性。\n */\npublic class MessageAccessor {",
    ),
    (
        "    public static void clearProperty(final Message msg, final String name) {",
        "    /** 清除消息指定属性。 */\n    public static void clearProperty(final Message msg, final String name) {",
    ),
    (
        "    public static void setProperties(final Message msg, Map<String, String> properties) {",
        "    /** 整体设置消息 properties Map。 */\n    public static void setProperties(final Message msg, Map<String, String> properties) {",
    ),
    (
        "    public static void setTransferFlag(final Message msg, String unit) {",
        "    /** 设置单元化迁移标志。 */\n    public static void setTransferFlag(final Message msg, String unit) {",
    ),
    (
        "    public static void putProperty(final Message msg, final String name, final String value) {",
        "    /** 写入任意系统/内部属性。 */\n    public static void putProperty(final Message msg, final String name, final String value) {",
    ),
    (
        "    public static String getTransferFlag(final Message msg) {",
        "    /** 读取单元化迁移标志。 */\n    public static String getTransferFlag(final Message msg) {",
    ),
    (
        "    public static void setCorrectionFlag(final Message msg, String unit) {",
        "    /** 设置消息纠错标志。 */\n    public static void setCorrectionFlag(final Message msg, String unit) {",
    ),
    (
        "    public static String getCorrectionFlag(final Message msg) {",
        "    /** 读取消息纠错标志。 */\n    public static String getCorrectionFlag(final Message msg) {",
    ),
    (
        "    public static void setOriginMessageId(final Message msg, String originMessageId) {",
        "    /** 设置原始消息 ID（重试/转发链路）。 */\n    public static void setOriginMessageId(final Message msg, String originMessageId) {",
    ),
    (
        "    public static String getOriginMessageId(final Message msg) {",
        "    /** 读取原始消息 ID。 */\n    public static String getOriginMessageId(final Message msg) {",
    ),
    (
        "    public static void setMQ2Flag(final Message msg, String flag) {",
        "    /** 设置 MQ2 兼容标志。 */\n    public static void setMQ2Flag(final Message msg, String flag) {",
    ),
    (
        "    public static String getMQ2Flag(final Message msg) {",
        "    /** 读取 MQ2 兼容标志。 */\n    public static String getMQ2Flag(final Message msg) {",
    ),
    (
        "    public static void setReconsumeTime(final Message msg, String reconsumeTimes) {",
        "    /** 设置当前重试消费次数。 */\n    public static void setReconsumeTime(final Message msg, String reconsumeTimes) {",
    ),
    (
        "    public static String getReconsumeTime(final Message msg) {",
        "    /** 读取当前重试消费次数。 */\n    public static String getReconsumeTime(final Message msg) {",
    ),
    (
        "    public static void setMaxReconsumeTimes(final Message msg, String maxReconsumeTimes) {",
        "    /** 设置最大重试消费次数。 */\n    public static void setMaxReconsumeTimes(final Message msg, String maxReconsumeTimes) {",
    ),
    (
        "    public static String getMaxReconsumeTimes(final Message msg) {",
        "    /** 读取最大重试消费次数。 */\n    public static String getMaxReconsumeTimes(final Message msg) {",
    ),
    (
        "    public static void setConsumeStartTimeStamp(final Message msg, String propertyConsumeStartTimeStamp) {",
        "    /** 设置消费开始时间戳属性。 */\n    public static void setConsumeStartTimeStamp(final Message msg, String propertyConsumeStartTimeStamp) {",
    ),
    (
        "    public static String getConsumeStartTimeStamp(final Message msg) {",
        "    /** 读取消费开始时间戳属性。 */\n    public static String getConsumeStartTimeStamp(final Message msg) {",
    ),
    (
        "    public static void setLiteTopic(final Message msg, String liteTopic) {",
        "    /** 设置消息关联的 Lite Topic 名。 */\n    public static void setLiteTopic(final Message msg, String liteTopic) {",
    ),
    (
        "    public static Message cloneMessage(final Message msg) {",
        "    /** 浅克隆消息：复制 Topic、body、flag 与 properties。 */\n    public static Message cloneMessage(final Message msg) {",
    ),
    (
        "    public static Map<String, String> deepCopyProperties(Map<String, String> properties) {",
        "    /** 深拷贝 properties Map；入参为 null 时返回 null。 */\n    public static Map<String, String> deepCopyProperties(Map<String, String> properties) {",
    ),
]
