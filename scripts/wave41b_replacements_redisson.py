"""Chinese annotation replacements for Redisson 4.7.0 wave-41b api [15:30]."""
from __future__ import annotations

_A = "redisson/src/main/java/org/redisson/api/"
_EMPTY_JDOC = "/**\n *\n * @author Nikita Koksharov\n *\n */"

W41B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- DeletedObjectListener ---

_deleted_listener = [
    (
        "/**\n * Redisson Object Event listener for <b>deleted</b> event published by Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Eg letters\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis 发布<b>删除</b>键空间事件时触发的 Redisson 对象监听器。\n"
        " * <p>\n"
        " * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code g} 字母。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Invoked on deleted event\n     * \n     * @param name - name of object\n     */",
        "    /**\n"
        "     * 对象被删除时回调。\n"
        "     *\n"
        "     * @param name 被删除对象的 Redis 键名\n"
        "     */",
    ),
]
W41B_REPLACEMENTS[f"{_A}DeletedObjectListener.java"] = _deleted_listener
W41B_REPLACEMENTS["DeletedObjectListener.java"] = _deleted_listener

# --- Entry ---

_entry = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 简单的键值对容器，用于 API 层传递 {@code Map.Entry} 风格数据。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */",
    ),
    (
        "    public Entry() {",
        "    /** 无参构造，供序列化框架使用。 */\n"
        "    public Entry() {",
    ),
    (
        "    public Entry(K key, V  value) {",
        "    /** @param key 键\n"
        "     *  @param value 值 */\n"
        "    public Entry(K key, V  value) {",
    ),
    (
        "    public V getValue() {",
        "    /** @return 值 */\n"
        "    public V getValue() {",
    ),
    (
        "    public K getKey() {",
        "    /** @return 键 */\n"
        "    public K getKey() {",
    ),
]
W41B_REPLACEMENTS[f"{_A}Entry.java"] = _entry
W41B_REPLACEMENTS["Entry.java"] = _entry

# --- EvictionMode ---

_eviction_mode = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 缓存驱逐策略枚举，用于 {@link org.redisson.api.RMapCache} 等带容量限制的集合。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "        /**\n         * Least Recently Used eviction algorithm.\n         */",
        "        /** 最近最少使用（LRU）驱逐算法。 */",
    ),
    (
        "        /**\n         * Least Frequently Used eviction algorithm.\n         */",
        "        /** 最不经常使用（LFU）驱逐算法。 */",
    ),
]
W41B_REPLACEMENTS[f"{_A}EvictionMode.java"] = _eviction_mode
W41B_REPLACEMENTS["EvictionMode.java"] = _eviction_mode

# --- ExecutorOptions ---

_executor_options = [
    (
        "/**\n * Configuration for ExecutorService.\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link org.redisson.api.RExecutorService} 的可选配置。\n"
        " * <p>\n"
        " * 控制任务重试间隔与任务标识生成策略。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static ExecutorOptions defaults() {",
        "    /** @return 使用默认参数的 {@link ExecutorOptions} 实例 */\n"
        "    public static ExecutorOptions defaults() {",
    ),
    (
        "    public long getTaskRetryInterval() {",
        "    /** @return 任务重试间隔（毫秒） */\n"
        "    public long getTaskRetryInterval() {",
    ),
    (
        "    /**\n     * Defines task retry interval at the end of which task\n     * is executed again by ExecutorService worker.\n     * <p>\n     * Counted from the task start moment.\n     * Applied only if the task was in progress but for some reason\n     * wasn't marked as completed (successful or unsuccessful).\n     * <p>\n     * Set <code>0</code> to disable.\n     * <p>\n     * Default is <code>5 minutes</code>\n     * \n     * @param timeout value\n     * @param unit value\n     * @return self instance\n     */",
        "    /**\n"
        "     * 设置任务重试间隔：自任务开始起若仍未标记完成（成功或失败），\n"
        "     * 则 Worker 在该间隔后重新执行。\n"
        "     * <p>\n"
        "     * 设为 {@code 0} 禁用重试；默认 {@code 5} 分钟。\n"
        "     *\n"
        "     * @param timeout 间隔数值\n"
        "     * @param unit 时间单位\n"
        "     * @return 当前实例（链式调用）\n"
        "     */",
    ),
    (
        "    public IdGenerator getIdGenerator() {",
        "    /** @return 任务标识生成器 */\n"
        "    public IdGenerator getIdGenerator() {",
    ),
    (
        "    /**\n     * Defines identifier generator\n     *\n     * @param idGenerator identifier generator\n     * @return self instance\n     */",
        "    /**\n"
        "     * 设置任务标识生成器。\n"
        "     *\n"
        "     * @param idGenerator 标识生成器实现\n"
        "     * @return 当前实例（链式调用）\n"
        "     */",
    ),
]
W41B_REPLACEMENTS[f"{_A}ExecutorOptions.java"] = _executor_options
W41B_REPLACEMENTS["ExecutorOptions.java"] = _executor_options

# --- ExpiredObjectListener ---

_expired_listener = [
    (
        "/**\n * Redisson Object Event listener for <b>expired</b> event published by Redis.\n * <p>\n * Redis notify-keyspace-events setting should contain Ex letters\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis 发布<b>过期</b>键空间事件时触发的 Redisson 对象监听器。\n"
        " * <p>\n"
        " * 需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code x} 字母。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Invoked on expired event\n     * \n     * @param name - name of object\n     */",
        "    /**\n"
        "     * 对象 TTL 到期时回调。\n"
        "     *\n"
        "     * @param name 过期对象的 Redis 键名\n"
        "     */",
    ),
]
W41B_REPLACEMENTS[f"{_A}ExpiredObjectListener.java"] = _expired_listener
W41B_REPLACEMENTS["ExpiredObjectListener.java"] = _expired_listener

# --- FPHAType ---

_fpha_type = [
    (
        "/**\n * Floating-point homogeneous array precision type for JSON.SET FPHA argument.\n * Requires <b>Redis 8.8.0 or higher.</b>\n *\n * @author Triet Nguyen\n */",
        "/**\n"
        " * {@code JSON.SET} 命令 {@code FPHA} 参数的浮点同质数组精度类型。\n"
        " * <p>\n"
        " * 需要 <b>Redis 8.8.0 或更高版本</b>。\n"
        " *\n"
        " * @author Triet Nguyen\n"
        " */",
    ),
    (
        "    /** Brain Float 16-bit precision. */",
        "    /** Brain Float 16 位精度（BF16）。 */",
    ),
    (
        "    /** 16-bit floating-point precision. */",
        "    /** 16 位浮点精度（FP16）。 */",
    ),
    (
        "    /** 32-bit floating-point precision. */",
        "    /** 32 位浮点精度（FP32）。 */",
    ),
    (
        "    /** 64-bit floating-point precision. */",
        "    /** 64 位浮点精度（FP64）。 */",
    ),
]
W41B_REPLACEMENTS[f"{_A}FPHAType.java"] = _fpha_type
W41B_REPLACEMENTS["FPHAType.java"] = _fpha_type

# --- FunctionLibrary ---

_function_library = [
    (
        "/**\n * Encapsulates information about Redis functions library.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 封装 Redis {@code FUNCTION LIST} 返回的函数库元数据。\n"
        " * <p>\n"
        " * 包含库名、引擎、源码及库内各函数的名称、描述与标志位。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public enum Flag {NO_WRITES, ALLOW_OOM, ALLOW_STALE, NO_CLUSTER}",
        "    /** 函数标志：禁止写、允许 OOM、允许 stale 读、禁止集群执行等。 */\n"
        "    public enum Flag {NO_WRITES, ALLOW_OOM, ALLOW_STALE, NO_CLUSTER}",
    ),
    (
        "    public static class Function {",
        "    /** 函数库内单个函数的元数据。 */\n"
        "    public static class Function {",
    ),
    (
        "        public Function(String name, String description, List<Flag> flags) {",
        "        /** @param name 函数名\n"
        "         *  @param description 描述\n"
        "         *  @param flags 标志位列表 */\n"
        "        public Function(String name, String description, List<Flag> flags) {",
    ),
    (
        "        public List<Flag> getFlags() {",
        "        /** @return 函数标志位列表 */\n"
        "        public List<Flag> getFlags() {",
    ),
    (
        "        public String getDescription() {",
        "        /** @return 函数描述 */\n"
        "        public String getDescription() {",
    ),
    (
        "        public String getName() {",
        "        /** @return 函数名 */\n"
        "        public String getName() {",
    ),
    (
        "    public FunctionLibrary(String name, String engine, String code, List<Function> functions) {",
        "    /** @param name 库名\n"
        "     *  @param engine 引擎（如 {@code lua}）\n"
        "     *  @param code 库源码\n"
        "     *  @param functions 库内函数列表 */\n"
        "    public FunctionLibrary(String name, String engine, String code, List<Function> functions) {",
    ),
    (
        "    public String getName() {",
        "    /** @return 函数库名称 */\n"
        "    public String getName() {",
    ),
    (
        "    public String getEngine() {",
        "    /** @return 执行引擎名称 */\n"
        "    public String getEngine() {",
    ),
    (
        "    public String getCode() {",
        "    /** @return 函数库 Lua 源码 */\n"
        "    public String getCode() {",
    ),
    (
        "    public List<Function> getFunctions() {",
        "    /** @return 库内函数元数据列表 */\n"
        "    public List<Function> getFunctions() {",
    ),
]
W41B_REPLACEMENTS[f"{_A}FunctionLibrary.java"] = _function_library
W41B_REPLACEMENTS["FunctionLibrary.java"] = _function_library

# --- FunctionMode ---

_function_mode = [
    (
        "/**\n * Function execution mode.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis 函数 {@code FCALL} 的执行模式，映射为只读或读写命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Execute function as read operation\n     */",
        "    /** 以只读操作执行函数（{@code FCALL_RO}）。 */",
    ),
    (
        "    /**\n     * Execute function as read operation\n     */",
        "    /** 以写操作执行函数（{@code FCALL}）。 */",
    ),
]
W41B_REPLACEMENTS[f"{_A}FunctionMode.java"] = _function_mode
W41B_REPLACEMENTS["FunctionMode.java"] = _function_mode

# --- FunctionResult ---

_function_result = [
    (
        "/**\n * Function result type.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@code FCALL} 返回值类型，决定使用的 Redis 命令与解码方式。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Result is a value of Boolean type\n     */",
        "    /** 返回 {@code Boolean} 类型。 */",
    ),
    (
        "    /**\n     * Result is a value of Long type\n     */",
        "    /** 返回 {@code Long} 类型。 */",
    ),
    (
        "    /**\n     * Result is a value of List type\n     */",
        "    /** 返回 {@code List} 类型。 */",
    ),
    (
        "    /**\n     * Result is a value of plain String type\n     */",
        "    /** 返回普通 {@code String} 类型。 */",
    ),
    (
        "    /**\n     * Result is a value of user defined type\n     */",
        "    /** 返回用户自定义类型（由 {@link org.redisson.client.codec.Codec} 解码）。 */",
    ),
    (
        "    /**\n     * Result is a value of Map Value type. Codec.getMapValueDecoder() and Codec.getMapValueEncoder()\n     * methods are used for data deserialization or serialization.\n     */",
        "    /** 返回 Map 值类型；使用 {@code Codec.getMapValueDecoder/Encoder()} 编解码。 */",
    ),
    (
        "    /**\n     * Result is a value of List type, which consists of objects of Map Value type.\n     * Codec.getMapValueDecoder() and Codec.getMapValueEncoder()\n     * methods are used for data deserialization or serialization.\n     */",
        "    /** 返回 Map 值类型的 {@code List}；使用 {@code Codec.getMapValueDecoder/Encoder()} 编解码。 */",
    ),
    (
        "    public RedisCommand<?> getCommand() {",
        "    /** @return 对应的 {@code FCALL_*} Redis 命令 */\n"
        "    public RedisCommand<?> getCommand() {",
    ),
]
W41B_REPLACEMENTS[f"{_A}FunctionResult.java"] = _function_result
W41B_REPLACEMENTS["FunctionResult.java"] = _function_result

# --- FunctionStats ---

_function_stats = [
    (
        "/**\n * Encapsulates information about currently running\n * Redis function and available execution engines.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 封装 {@code FUNCTION STATS} 返回的运行中函数与各引擎统计信息。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static class Engine {",
        "    /** 单个函数引擎（如 {@code lua}）的库与函数计数。 */\n"
        "    public static class Engine {",
    ),
    (
        "        public Engine(Long libraries, Long functions) {",
        "        /** @param libraries 已加载库数量\n"
        "         *  @param functions 已注册函数数量 */\n"
        "        public Engine(Long libraries, Long functions) {",
    ),
    (
        "        /**\n         * Returns libraries amount\n         *\n         * @return libraries amount\n         */",
        "        /** @return 已加载函数库数量 */\n",
    ),
    (
        "        /**\n         * Returns functions amount\n         *\n         * @return functions amount\n         */",
        "        /** @return 已注册函数数量 */\n",
    ),
    (
        "    public static class RunningFunction {",
        "    /** 当前正在执行的函数及其调用参数与运行时长。 */\n"
        "    public static class RunningFunction {",
    ),
    (
        "        public RunningFunction(String name, List<Object> command, Duration duration) {",
        "        /** @param name 函数名\n"
        "         *  @param command FCALL 命令参数列表\n"
        "         *  @param duration 已运行时长 */\n"
        "        public RunningFunction(String name, List<Object> command, Duration duration) {",
    ),
    (
        "        /**\n         * Returns name of running function\n         *\n         * @return name\n         */",
        "        /** @return 正在执行的函数名 */\n",
    ),
    (
        "        /**\n         * Returns arguments of running function\n         *\n         * @return arguments\n         */",
        "        /** @return FCALL 命令参数列表 */\n",
    ),
    (
        "        /**\n         * Returns runtime duration of running function\n         *\n         * @return runtime duration\n         */",
        "        /** @return 函数已运行时长 */\n",
    ),
    (
        "    public FunctionStats(RunningFunction runningFunction, Map<String, Engine> engines) {",
        "    /** @param runningFunction 当前运行中的函数；无则为 {@code null}\n"
        "     *  @param engines 按引擎名索引的统计信息 */\n"
        "    public FunctionStats(RunningFunction runningFunction, Map<String, Engine> engines) {",
    ),
    (
        "    /**\n     * Returns currently running fuction otherwise {@code null}\n     *\n     * @return running function\n     */",
        "    /** @return 当前运行中的函数；无则 {@code null} */\n",
    ),
    (
        "    /**\n     * Returns engine objects mapped by function engine name\n     *\n     * @return engine objects\n     */",
        "    /** @return 按引擎名映射的 {@link Engine} 统计信息 */\n",
    ),
]
W41B_REPLACEMENTS[f"{_A}FunctionStats.java"] = _function_stats
W41B_REPLACEMENTS["FunctionStats.java"] = _function_stats

# --- GcraConfig ---

_gcra_config = [
    (
        "/**\n * Rate configuration of {@link RGcra} object.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RGcra} 分布式 GCRA 限流器的速率配置快照。\n"
        " * <p>\n"
        " * 由 {@link RGcra#trySetRate} 或 {@link RGcra#setRate} 设置。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public GcraConfig(long maxBurst, long tokensPerPeriod, Duration period) {",
        "    /** @param maxBurst 最大突发令牌数\n"
        "     *  @param tokensPerPeriod 每周期补充令牌数\n"
        "     *  @param period 补充周期 */\n"
        "    public GcraConfig(long maxBurst, long tokensPerPeriod, Duration period) {",
    ),
    (
        "    /**\n     * Returns maximum burst size set through\n     * {@link RGcra#trySetRate(long, long, Duration)} or {@link RGcra#setRate(long, long, Duration)} method.\n     *\n     * @return maximum burst size\n     */",
        "    /** @return 最大突发令牌容量 */\n",
    ),
    (
        "    /**\n     * Returns token replenishment rate per period set through\n     * {@link RGcra#trySetRate(long, long, Duration)} or {@link RGcra#setRate(long, long, Duration)} method.\n     *\n     * @return token amount replenished per period\n     */",
        "    /** @return 每个补充周期恢复的令牌数 */\n",
    ),
    (
        "    /**\n     * Returns replenishment period set through\n     * {@link RGcra#trySetRate(long, long, Duration)} or {@link RGcra#setRate(long, long, Duration)} method.\n     *\n     * @return replenishment period\n     */",
        "    /** @return 令牌补充周期 */\n",
    ),
]
W41B_REPLACEMENTS[f"{_A}GcraConfig.java"] = _gcra_config
W41B_REPLACEMENTS["GcraConfig.java"] = _gcra_config

# --- GcraResult ---

_gcra_result = [
    (
        "/**\n * Result returned by Redis {@code GCRA} command.\n *\n * @author Su Ko\n *\n */",
        "/**\n"
        " * Redis {@code GCRA} 命令的限流判定结果。\n"
        " * <p>\n"
        " * 表示请求令牌是否被限流，以及当前可用令牌与重试等待时间。\n"
        " *\n"
        " * @author Su Ko\n"
        " */",
    ),
    (
        "    public GcraResult(boolean limited, long maxTokens, long availableTokens,\n                      long retryAfterSeconds, long fullBurstAfterSeconds) {",
        "    /** @param limited 是否被限流\n"
        "     *  @param maxTokens 最大令牌容量\n"
        "     *  @param availableTokens 当前可用令牌数\n"
        "     *  @param retryAfterSeconds 获取所需令牌需等待的秒数\n"
        "     *  @param fullBurstAfterSeconds 恢复满突发容量需等待的秒数 */\n"
        "    public GcraResult(boolean limited, long maxTokens, long availableTokens,\n"
        "                      long retryAfterSeconds, long fullBurstAfterSeconds) {",
    ),
    (
        "    /**\n     * Returns {@code true} if the requested tokens can't be acquired.\n     *\n     * @return {@code true} if rate limit has been exceeded\n     */",
        "    /** @return 若请求令牌无法获取则为 {@code true}（已触发限流） */\n",
    ),
    (
        "    /**\n     * Returns maximum token amount available for burst.\n     *\n     * @return maximum token amount\n     */",
        "    /** @return 最大突发令牌容量 */\n",
    ),
    (
        "    /**\n     * Returns token amount currently available.\n     *\n     * @return available token amount\n     */",
        "    /** @return 当前可用令牌数 */\n",
    ),
    (
        "    /**\n     * Returns number of seconds to wait before the requested tokens can be acquired.\n     *\n     * @return retry interval in seconds\n     */",
        "    /** @return 获取所需令牌前需等待的秒数 */\n",
    ),
    (
        "    /**\n     * Returns number of seconds to wait before the full burst capacity is restored.\n     *\n     * @return full burst restore interval in seconds\n     */",
        "    /** @return 恢复满突发容量前需等待的秒数 */\n",
    ),
]
W41B_REPLACEMENTS[f"{_A}GcraResult.java"] = _gcra_result
W41B_REPLACEMENTS["GcraResult.java"] = _gcra_result

# --- IdGenerator ---

_id_generator = [
    (
        "/**\n * Identifier generator\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 分布式任务标识生成器，供 {@link ExecutorOptions} 等组件使用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Generates identifier\n     *\n     * @return identifier\n     */",
        "    /**\n"
        "     * 生成唯一任务标识。\n"
        "     *\n"
        "     * @return 任务标识字符串\n"
        "     */",
    ),
    (
        "    /**\n     * Returns random identifier generator. Used by default.\n     *\n     * @return random identifier generator\n     */",
        "    /**\n"
        "     * 返回随机标识生成器（默认实现）。\n"
        "     *\n"
        "     * @return 随机 {@link IdGenerator} 实例\n"
        "     */",
    ),
]
W41B_REPLACEMENTS[f"{_A}IdGenerator.java"] = _id_generator
W41B_REPLACEMENTS["IdGenerator.java"] = _id_generator

# --- JsonType ---

_json_type = [
    (
        "/**\n * Json data type\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * RedisJSON 文档节点类型，对应 {@code JSON.TYPE} 返回值。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    BOOLEAN,",
        "    /** 布尔类型。 */\n"
        "    BOOLEAN,",
    ),
    (
        "    STRING,",
        "    /** 字符串类型。 */\n"
        "    STRING,",
    ),
    (
        "    NUMBER,",
        "    /** 浮点数值类型。 */\n"
        "    NUMBER,",
    ),
    (
        "    INTEGER,",
        "    /** 整数类型。 */\n"
        "    INTEGER,",
    ),
    (
        "    OBJECT,",
        "    /** JSON 对象类型。 */\n"
        "    OBJECT,",
    ),
    (
        "    ARRAY",
        "    /** JSON 数组类型。 */\n"
        "    ARRAY",
    ),
]
W41B_REPLACEMENTS[f"{_A}JsonType.java"] = _json_type
W41B_REPLACEMENTS["JsonType.java"] = _json_type

# --- LeaseGetResult ---

_lease_get_result = [
    (
        "/**\n * Result returned by {@link RLeasedMap#getWithLease(Object, java.time.Duration)} method.\n * <p>\n * If the entry is present in cache then {@link #getValue()} returns the value and {@link #getLeaseToken()} is {@code null}.\n * If the entry is absent then {@link #getValue()} is {@code null} and {@link #getLeaseToken()} returns the lease token,\n * or {@code null} if no lease information is available.\n *\n * @author nhancdt2602\n *\n * @param <V> value type\n */",
        "/**\n"
        " * {@link RLeasedMap#getWithLease(Object, java.time.Duration)} 的查询结果。\n"
        " * <p>\n"
        " * 缓存命中时 {@link #getValue()} 返回值且 {@link #getLeaseToken()} 为 {@code null}；\n"
        " * 未命中时 {@link #getValue()} 为 {@code null}，{@link #getLeaseToken()} 返回租约令牌\n"
        "（无租约信息时亦为 {@code null}）。\n"
        " *\n"
        " * @author nhancdt2602\n"
        " * @param <V> 值类型\n"
        " */",
    ),
    (
        "    public LeaseGetResult(V value, boolean leaseAcquired, String leaseToken) {",
        "    /** @param value 缓存值；未命中为 {@code null}\n"
        "     *  @param leaseAcquired 是否在未命中时成功获取租约\n"
        "     *  @param leaseToken 租约令牌；命中或无租约时为 {@code null} */\n"
        "    public LeaseGetResult(V value, boolean leaseAcquired, String leaseToken) {",
    ),
    (
        "    /**\n     * Returns cached value or {@code null} if cache miss happened.\n     *\n     * @return value or {@code null}\n     */",
        "    /** @return 缓存值；未命中时 {@code null} */\n",
    ),
    (
        "    /**\n     * Returns {@code true} if there was no cached value for this lookup ({@link #getValue()} is {@code null}).\n     *\n     * @return {@code true} on cache miss, {@code false} if a value was present\n     */",
        "    /** @return 缓存未命中（{@link #getValue()} 为 {@code null}）时为 {@code true} */\n",
    ),
    (
        "    /**\n     * Returns {@code true} if lease has been acquired on cache miss.\n     * <p>\n     * If {@link #getValue()} is not {@code null} then this method always returns {@code false}.\n     *\n     * @return {@code true} if acquired, otherwise {@code false}\n     */",
        "    /** @return 未命中且成功获取租约时为 {@code true}；命中时恒为 {@code false} */\n",
    ),
    (
        "    /**\n     * Returns lease token if cache miss happened, otherwise {@code null}.\n     *\n     * @return lease token or {@code null}\n     */",
        "    /** @return 未命中时的租约令牌；命中或无租约信息时为 {@code null} */\n",
    ),
]
W41B_REPLACEMENTS[f"{_A}LeaseGetResult.java"] = _lease_get_result
W41B_REPLACEMENTS["LeaseGetResult.java"] = _lease_get_result
