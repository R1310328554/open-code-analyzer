"""RocketMQ 5.5.0 wave19b controller metrics/processor + filter [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["controller/src/main/java/org/apache/rocketmq/controller/metrics/ControllerMetricsConstant.java"] = [
    (
        "public class ControllerMetricsConstant {",
        "/**\n * 控制器 OpenTelemetry 指标常量：定义 Meter 名称、标签键、指标名及枚举映射。\n */\npublic class ControllerMetricsConstant {",
    ),
    (
        "    public static final String LABEL_ADDRESS = \"address\";",
        "    /** 指标标签：控制器节点地址。 */\n    public static final String LABEL_ADDRESS = \"address\";",
    ),
    (
        "    public static final String OPEN_TELEMETRY_METER_NAME = \"controller\";",
        "    /** OpenTelemetry Meter 名称，用于标识控制器指标域。 */\n    public static final String OPEN_TELEMETRY_METER_NAME = \"controller\";",
    ),
    (
        "    public static final String GAUGE_ROLE = \"role\";",
        "    /** Gauge 指标名：当前节点 Raft/DLedger 角色。 */\n    public static final String GAUGE_ROLE = \"role\";",
    ),
    (
        "    // unit: B\n    public static final String GAUGE_DLEDGER_DISK_USAGE = \"dledger_disk_usage\";",
        "    /** Gauge 指标名：DLedger 存储目录磁盘占用（单位：字节）。 */\n    public static final String GAUGE_DLEDGER_DISK_USAGE = \"dledger_disk_usage\";",
    ),
    (
        "    public static final String COUNTER_REQUEST_TOTAL = \"request_total\";",
        "    /** Counter 指标名：控制器 RPC 请求总数。 */\n    public static final String COUNTER_REQUEST_TOTAL = \"request_total\";",
    ),
    (
        "    // unit: us\n    public static final String HISTOGRAM_REQUEST_LATENCY = \"request_latency\";",
        "    /** Histogram 指标名：请求处理延迟（单位：微秒）。 */\n    public static final String HISTOGRAM_REQUEST_LATENCY = \"request_latency\";",
    ),
    (
        "    public enum RequestType {",
        "    /** 控制器请求类型，与 {@link RequestCode} 一一对应。 */\n    public enum RequestType {",
    ),
    (
        "        private final int code;\n\n        RequestType(int code) {",
        "        /** 对应的 Remoting 请求码。 */\n        private final int code;\n\n        /** 绑定请求码枚举常量。 */\n        RequestType(int code) {",
    ),
    (
        "        public static String getLowerCaseNameByCode(int code) {",
        "        /** 按请求码返回枚举名的小写形式，未匹配时返回 null。 */\n        public static String getLowerCaseNameByCode(int code) {",
    ),
    (
        "    public enum RequestHandleStatus {",
        "    /** 控制器请求处理结果：成功、失败或超时。 */\n    public enum RequestHandleStatus {",
    ),
    (
        "    public enum ElectionResult {",
        "    /** 主节点选举结果枚举，用于选举指标标签。 */\n    public enum ElectionResult {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/metrics/ControllerMetricsManager.java"] = [
    (
        "public class ControllerMetricsManager {",
        "/**\n * 控制器指标管理器：基于 OpenTelemetry 注册并导出角色、磁盘、请求与选举等指标。\n */\npublic class ControllerMetricsManager {",
    ),
    (
        "    private static volatile ControllerMetricsManager instance;",
        "    /** 单例实例，双重检查锁懒加载。 */\n    private static volatile ControllerMetricsManager instance;",
    ),
    (
        "    // metrics about node status\n    public static LongUpDownCounter role = new NopLongUpDownCounter();",
        "    /** 节点角色 UpDownCounter（候选/跟随/领导者等）。 */\n    public static LongUpDownCounter role = new NopLongUpDownCounter();",
    ),
    (
        "    // metrics about latency\n    public static LongHistogram requestLatency = new NopLongHistogram();",
        "    /** 请求延迟直方图（微秒级分桶）。 */\n    public static LongHistogram requestLatency = new NopLongHistogram();",
    ),
    (
        "    public static ControllerMetricsManager getInstance(ControllerManager controllerManager) {",
        "    /** 获取指标管理器单例，首次调用时绑定 {@link ControllerManager}。 */\n    public static ControllerMetricsManager getInstance(ControllerManager controllerManager) {",
    ),
    (
        "    public static AttributesBuilder newAttributesBuilder() {",
        "    /** 创建带全局标签（地址、组、节点 ID）的 OpenTelemetry 属性构建器。 */\n    public static AttributesBuilder newAttributesBuilder() {",
    ),
    (
        "    public static void recordRole(MemberState.Role newRole, MemberState.Role oldRole) {",
        "    /** 记录角色变更：按新旧角色差值更新 role 指标。 */\n    public static void recordRole(MemberState.Role newRole, MemberState.Role oldRole) {",
    ),
    (
        "    private boolean checkConfig() {",
        "    /** 校验指标导出配置（导出类型、端点等）是否可用。 */\n    private boolean checkConfig() {",
    ),
    (
        "    private void registerMetricsView(SdkMeterProviderBuilder providerBuilder) {",
        "    /** 为请求与 DLedger 操作延迟直方图注册显式分桶视图。 */\n    private void registerMetricsView(SdkMeterProviderBuilder providerBuilder) {",
    ),
    (
        "        // define latency bucket",
        "        // 延迟分桶边界：微秒到秒级",
    ),
    (
        "    private void initMetric(Meter meter) {",
        "    /** 在 Meter 上注册角色、磁盘、活跃 Broker、计数器与延迟等指标。 */\n    private void initMetric(Meter meter) {",
    ),
    (
        "    public void init() {",
        "    /** 按配置初始化 OpenTelemetry SDK 与指标导出器（OTLP/Prom/Log）。 */\n    public void init() {",
    ),
]

R["controller/src/main/java/org/apache/rocketmq/controller/processor/ControllerRequestProcessor.java"] = [
    (
        "/**\n * Processor for controller request\n */",
        "/**\n * 控制器 Remoting 请求处理器：路由各类 Controller RPC 并上报指标。\n */",
    ),
    (
        "    private static final int WAIT_TIMEOUT_OUT = 5;",
        "    /** 异步 Controller 操作等待超时（秒）。 */\n    private static final int WAIT_TIMEOUT_OUT = 5;",
    ),
    (
        "    public ControllerRequestProcessor(final ControllerManager controllerManager) {",
        "    /** 构造处理器并初始化配置黑名单。 */\n    public ControllerRequestProcessor(final ControllerManager controllerManager) {",
    ),
    (
        "    private void initConfigBlackList() {",
        "    /** 加载禁止通过 RPC 修改的配置项黑名单。 */\n    private void initConfigBlackList() {",
    ),
    (
        "    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
        "    /** 处理入站请求：分发业务逻辑并记录成功/失败/超时指标。 */\n    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
    ),
    (
        "    private RemotingCommand handleRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
        "    /** 按 {@link RemotingCommand#getCode()} 将请求路由到具体处理方法。 */\n    private RemotingCommand handleRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
    ),
    (
        "    private RemotingCommand handleAlterSyncStateSet(ChannelHandlerContext ctx,",
        "    /** 处理修改同步状态集（SyncStateSet）请求。 */\n    private RemotingCommand handleAlterSyncStateSet(ChannelHandlerContext ctx,",
    ),
    (
        "    private RemotingCommand handleControllerElectMaster(ChannelHandlerContext ctx,",
        "    /** 处理选举 Master 请求，成功时可通知 Broker 角色变更。 */\n    private RemotingCommand handleControllerElectMaster(ChannelHandlerContext ctx,",
    ),
    (
        "    private RemotingCommand handleBrokerHeartbeat(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
        "    /** 处理 Broker 心跳，更新心跳管理器中的存活信息。 */\n    private RemotingCommand handleBrokerHeartbeat(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
    ),
    (
        "    private RemotingCommand handleUpdateControllerConfig(ChannelHandlerContext ctx, RemotingCommand request) {",
        "    /** 动态更新控制器配置，拦截黑名单中的敏感项。 */\n    private RemotingCommand handleUpdateControllerConfig(ChannelHandlerContext ctx, RemotingCommand request) {",
    ),
    (
        "    public boolean rejectRequest() {",
        "    /** 是否拒绝新请求（当前恒为 false）。 */\n    public boolean rejectRequest() {",
    ),
    (
        "    private boolean validateBlackListConfigExist(Properties properties) {",
        "    /** 检查待更新配置是否包含黑名单键。 */\n    private boolean validateBlackListConfigExist(Properties properties) {",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/FilterFactory.java"] = [
    (
        "/**\n * Filter factory: support other filter to register.\n */",
        "/**\n * 消息过滤器工厂：支持注册多种 {@link FilterSpi} 实现（默认注册 SQL92）。\n */",
    ),
    (
        "    public static final FilterFactory INSTANCE = new FilterFactory();",
        "    /** 全局单例工厂实例。 */\n    public static final FilterFactory INSTANCE = new FilterFactory();",
    ),
    (
        "    protected static final Map<String, FilterSpi> FILTER_SPI_HOLDER = new HashMap<>(4);",
        "    /** 过滤器类型到 SPI 实现的映射表。 */\n    protected static final Map<String, FilterSpi> FILTER_SPI_HOLDER = new HashMap<>(4);",
    ),
    (
        "    static {\n        FilterFactory.INSTANCE.register(new SqlFilter());",
        "    /** 静态块：默认注册 SQL92 过滤器。 */\n    static {\n        FilterFactory.INSTANCE.register(new SqlFilter());",
    ),
    (
        "     * Register a filter.\n     * <br>\n     * Note:\n     * <li>1. Filter registered will be used in broker server, so take care of it's reliability and performance.</li>\n     */",
        "     * 注册自定义过滤器。\n     * <br>\n     * 注意：过滤器在 Broker 侧运行，需保证可靠性与性能。\n     */",
    ),
    (
        "    public void register(FilterSpi filterSpi) {",
        "    /** 注册过滤器，同类型重复注册将抛出异常。 */\n    public void register(FilterSpi filterSpi) {",
    ),
    (
        "     * Un register a filter.\n     */",
        "     * 注销指定类型的过滤器。\n     */",
    ),
    (
        "     * Get a filter registered, null if none exist.\n     */",
        "     * 按类型获取已注册过滤器，不存在时返回 null。\n     */",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/FilterSpi.java"] = [
    (
        "/**\n * Filter spi interface.\n */",
        "/**\n * 消息过滤器 SPI 接口：编译表达式并声明过滤器类型。\n */",
    ),
    (
        "     * Compile.\n     */",
        "     * 将字符串表达式编译为可执行的 {@link Expression}。\n     * @param expr 过滤器表达式字符串\n     */",
    ),
    (
        "    Expression compile(final String expr) throws MQFilterException;",
        "    /** 编译入口，失败时抛出 {@link MQFilterException}。 */\n    Expression compile(final String expr) throws MQFilterException;",
    ),
    (
        "     * Which type.\n     */",
        "     * 返回过滤器类型标识（如 SQL92）。\n     */",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/SqlFilter.java"] = [
    (
        "/**\n * SQL92 Filter, just a wrapper of {@link org.apache.rocketmq.filter.parser.SelectorParser}.\n * <p/>\n * <p>\n * Do not use this filter directly.Use {@link FilterFactory#get} to select a filter.\n * </p>\n */",
        "/**\n * SQL92 消息过滤器：封装 {@link org.apache.rocketmq.filter.parser.SelectorParser} 解析逻辑。\n * <p>\n * 请勿直接使用，应通过 {@link FilterFactory#get} 获取过滤器实例。\n * </p>\n */",
    ),
    (
        "    public Expression compile(final String expr) throws MQFilterException {",
        "    /** 调用 SelectorParser 将 SQL92 表达式编译为表达式树。 */\n    public Expression compile(final String expr) throws MQFilterException {",
    ),
    (
        "    public String ofType() {",
        "    /** 返回 {@link ExpressionType#SQL92} 类型标识。 */\n    public String ofType() {",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/constant/UnaryType.java"] = [
    (
        "public enum UnaryType {",
        "/**\n * 一元运算类型枚举，用于 SQL 选择器解析与求值。\n */\npublic enum UnaryType {",
    ),
    (
        "    NEGATE,",
        "    /** 取负（-x）。 */\n    NEGATE,",
    ),
    (
        "    IN,",
        "    /** IN 集合成员判断。 */\n    IN,",
    ),
    (
        "    NOT,",
        "    /** 逻辑非（NOT）。 */\n    NOT,",
    ),
    (
        "    BOOLEANCAST,",
        "    /** 布尔类型强制转换。 */\n    BOOLEANCAST,",
    ),
    (
        "    LIKE",
        "    /** 字符串 LIKE 模式匹配。 */\n    LIKE",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/expression/BinaryExpression.java"] = [
    (
        "/**\n * An expression which performs an operation on two expression values.\n * <p>\n * This class was taken from ActiveMQ org.apache.activemq.filter.BinaryExpression,\n * </p>\n */",
        "/**\n * 二元表达式抽象基类：对左右两个子表达式执行运算。\n * <p>\n * 源自 ActiveMQ {@code org.apache.activemq.filter.BinaryExpression}。\n * </p>\n */",
    ),
    (
        "    protected Expression left;\n    protected Expression right;",
        "    /** 左操作数表达式。 */\n    protected Expression left;\n    /** 右操作数表达式。 */\n    protected Expression right;",
    ),
    (
        "    public BinaryExpression(Expression left, Expression right) {",
        "    /** 构造二元表达式并绑定左右子树。 */\n    public BinaryExpression(Expression left, Expression right) {",
    ),
    (
        "     * Returns the symbol that represents this binary expression.  For example, addition is\n     * represented by \"+\"\n     */",
        "     * 返回表示该二元运算的符号，例如加法为 \"+\"。\n     */",
    ),
    (
        "    public abstract String getExpressionSymbol();",
        "    /** 子类实现：返回运算符字符串。 */\n    public abstract String getExpressionSymbol();",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/expression/BooleanConstantExpression.java"] = [
    (
        "/**\n * BooleanConstantExpression\n */",
        "/**\n * 布尔常量表达式：表示 TRUE、FALSE 或 NULL 布尔值。\n */",
    ),
    (
        "    public static final BooleanConstantExpression NULL = new BooleanConstantExpression(null);",
        "    /** 布尔 NULL 常量。 */\n    public static final BooleanConstantExpression NULL = new BooleanConstantExpression(null);",
    ),
    (
        "    public static final BooleanConstantExpression TRUE = new BooleanConstantExpression(Boolean.TRUE);",
        "    /** 布尔 TRUE 常量。 */\n    public static final BooleanConstantExpression TRUE = new BooleanConstantExpression(Boolean.TRUE);",
    ),
    (
        "    public static final BooleanConstantExpression FALSE = new BooleanConstantExpression(Boolean.FALSE);",
        "    /** 布尔 FALSE 常量。 */\n    public static final BooleanConstantExpression FALSE = new BooleanConstantExpression(Boolean.FALSE);",
    ),
    (
        "    public boolean matches(EvaluationContext context) throws Exception {",
        "    /** 判断求值结果是否为 {@link Boolean#TRUE}。 */\n    public boolean matches(EvaluationContext context) throws Exception {",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/expression/BooleanExpression.java"] = [
    (
        "/**\n * A BooleanExpression is an expression that always\n * produces a Boolean result.\n * <p>\n * This class was taken from ActiveMQ org.apache.activemq.filter.BooleanExpression,\n * but the parameter is changed to an interface.\n * </p>\n *\n * @see org.apache.rocketmq.filter.expression.EvaluationContext\n */",
        "/**\n * 布尔表达式接口：求值结果恒为布尔类型。\n * <p>\n * 源自 ActiveMQ，求值上下文改为 {@link EvaluationContext} 接口。\n * </p>\n *\n * @see org.apache.rocketmq.filter.expression.EvaluationContext\n */",
    ),
    (
        "     * @return true if the expression evaluates to Boolean.TRUE.\n     */",
        "     * 在给定上下文中求值，结果为 {@link Boolean#TRUE} 时返回 true。\n     * @param context 表达式求值上下文\n     */",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/expression/ConstantExpression.java"] = [
    (
        "/**\n * Represents a constant expression\n * <p>\n * This class was taken from ActiveMQ org.apache.activemq.filter.ConstantExpression,\n * but:\n * 1. For long type constant, the range bound by java Long type;\n * 2. For float type constant, the range bound by java Double type;\n * 3. Remove Hex and Octal expression;\n * 4. Add now expression to support to get current time.\n * </p>\n */",
        "/**\n * 常量表达式：封装字面量值（数字、字符串、布尔、当前时间等）。\n * <p>\n * 相较 ActiveMQ 版本：Long/Double 范围受限，移除八/十六进制，新增 now 表达式。\n * </p>\n */",
    ),
    (
        "    public static ConstantExpression createFromDecimal(String text) {",
        "    /** 从十进制文本创建整数常量，自动收窄为 int 或 long。 */\n    public static ConstantExpression createFromDecimal(String text) {",
    ),
    (
        "        // Strip off the 'l' or 'L' if needed.",
        "        // 去除 Long 后缀 l/L",
    ),
    (
        "        // only support Long.MIN_VALUE ~ Long.MAX_VALUE",
        "        // 仅支持 Java Long 范围内的整数",
    ),
    (
        "    public static ConstantExpression createFloat(String text) {",
        "    /** 从浮点文本创建 Double 常量，超出范围则抛异常。 */\n    public static ConstantExpression createFloat(String text) {",
    ),
    (
        "    public static ConstantExpression createNow() {",
        "    /** 创建表示当前时间的 now 表达式。 */\n    public static ConstantExpression createNow() {",
    ),
    (
        "    public Object evaluate(EvaluationContext context) throws Exception {",
        "    /** 直接返回内部常量值，与上下文无关。 */\n    public Object evaluate(EvaluationContext context) throws Exception {",
    ),
    (
        "     * Encodes the value of string so that it looks like it would look like when\n     * it was provided in a selector.\n     */",
        "     * 将字符串编码为选择器字面量形式（单引号转义）。\n     */",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/expression/EmptyEvaluationContext.java"] = [
    (
        "/**\n * Empty context.\n */",
        "/**\n * 空求值上下文：不含任何变量，{@link #get} 恒返回 null。\n */",
    ),
    (
        "    public Object get(String name) {",
        "    /** 空实现，任意名称均返回 null。 */\n    public Object get(String name) {",
    ),
    (
        "    public Map<String, Object> keyValues() {",
        "    /** 返回 null，表示无上下文变量映射。 */\n    public Map<String, Object> keyValues() {",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/expression/EvaluationContext.java"] = [
    (
        "/**\n * Context of evaluate expression.\n *\n * Compare to org.apache.activemq.filter.MessageEvaluationContext, this is just an interface.\n */",
        "/**\n * 表达式求值上下文：提供按名称读取变量与全量键值映射。\n *\n * 对应 ActiveMQ 的 MessageEvaluationContext，此处抽象为接口。\n */",
    ),
    (
        "     * Get value by name from context\n     */",
        "     * 按变量名从上下文取值。\n     * @param name 属性或用户属性名\n     */",
    ),
    (
        "     * Context variables.\n     */",
        "     * 返回上下文全部变量键值对。\n     */",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/expression/Expression.java"] = [
    (
        "/**\n * Interface of expression.\n * <p>\n * This class was taken from ActiveMQ org.apache.activemq.filter.Expression,\n * but the parameter is changed to an interface.\n * </p>\n *\n * @see org.apache.rocketmq.filter.expression.EvaluationContext\n */",
        "/**\n * 表达式根接口：在给定上下文中求值并返回结果对象。\n * <p>\n * 源自 ActiveMQ，求值参数改为 {@link EvaluationContext} 接口。\n * </p>\n *\n * @see org.apache.rocketmq.filter.expression.EvaluationContext\n */",
    ),
    (
        "     * Calculate express result with context.\n     *\n     * @param context context of evaluation\n     * @return the value of this expression\n     */",
        "     * 结合上下文计算表达式结果。\n     *\n     * @param context 求值上下文\n     * @return 表达式求值结果\n     */",
    ),
]

R["filter/src/main/java/org/apache/rocketmq/filter/expression/LogicExpression.java"] = [
    (
        "/**\n * A filter performing a comparison of two objects\n * <p>\n * This class was taken from ActiveMQ org.apache.activemq.filter.LogicExpression,\n * </p>\n */",
        "/**\n * 逻辑表达式抽象类：对两个布尔子表达式执行 AND/OR 短路求值。\n * <p>\n * 源自 ActiveMQ {@code org.apache.activemq.filter.LogicExpression}。\n * </p>\n */",
    ),
    (
        "    public LogicExpression(BooleanExpression left, BooleanExpression right) {",
        "    /** 构造逻辑表达式，左右操作数均为布尔表达式。 */\n    public LogicExpression(BooleanExpression left, BooleanExpression right) {",
    ),
    (
        "    public static BooleanExpression createOR(BooleanExpression lvalue, BooleanExpression rvalue) {",
        "    /** 创建逻辑或（||）表达式，左真短路。 */\n    public static BooleanExpression createOR(BooleanExpression lvalue, BooleanExpression rvalue) {",
    ),
    (
        "    public static BooleanExpression createAND(BooleanExpression lvalue, BooleanExpression rvalue) {",
        "    /** 创建逻辑与（&&）表达式，左假短路。 */\n    public static BooleanExpression createAND(BooleanExpression lvalue, BooleanExpression rvalue) {",
    ),
    (
        "    public boolean matches(EvaluationContext context) throws Exception {",
        "    /** 求值后判断是否为 {@link Boolean#TRUE}。 */\n    public boolean matches(EvaluationContext context) throws Exception {",
    ),
]
