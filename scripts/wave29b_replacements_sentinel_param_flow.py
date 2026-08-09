"""Chinese JavaDoc replacements for Sentinel 1.8.10 wave29b parameter flow control classes."""

PARAM_FLOW_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ModifyParamFlowRulesCommandHandler.java": [
        (
            "/**\n * @author Eric Zhao\n * @since 0.2.0\n */",
            "/**\n * 命令处理器：接收并全量替换热点参数流控规则，可选同步写入可写数据源。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        ),
        (
            "        RecordLog.info(\"[API Server] Receiving rule change (type:parameter flow rule): {}\", data);",
            "        RecordLog.info(\"[API Server] 收到规则变更（类型：热点参数流控）: {}\", data);",
        ),
        (
            "    /**\n     * Write target value to given data source.\n     *\n     * @param dataSource writable data source\n     * @param value target value to save\n     * @param <T> value type\n     * @return true if write successful or data source is empty; false if error occurs\n     */",
            "    /**\n     * 将规则列表写入可写数据源持久化。\n     *\n     * @param dataSource 可写数据源\n     * @param value 待保存的规则列表\n     * @param <T> 值类型\n     * @return 写入成功或数据源为空时返回 true；异常时返回 false\n     */",
        ),
    ],
    "ParamFlowStatisticSlotCallbackInit.java": [
        (
            "/**\n * Init function for adding callbacks to {@link StatisticSlotCallbackRegistry} to record metrics\n * for frequent parameters in {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot}.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
            "/**\n * 初始化函数：向 {@link StatisticSlotCallbackRegistry} 注册热点参数统计回调，\n * 在 {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot} 中记录参数级指标。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        ),
    ],
    "HotParamSlotChainBuilder.java": [
        (
            "/**\n * @author Eric Zhao\n * @since 0.2.0\n *\n * @deprecated since 1.7.2, we can use @Spi(order = -3000) to adjust the order of {@link ParamFlowSlot},\n * this class is reserved for compatibility with older versions.\n *\n * @see ParamFlowSlot\n * @see DefaultSlotChainBuilder\n */",
            "/**\n * @author Eric Zhao\n * @since 0.2.0\n *\n * @deprecated 自 1.7.2 起已废弃；可通过 {@link ParamFlowSlot} 上的 @Spi(order = -3000) 调整槽位顺序，\n * 本类仅为兼容旧版本保留。\n *\n * @see ParamFlowSlot\n * @see DefaultSlotChainBuilder\n */",
        ),
    ],
    "ParamFlowArgument.java": [
        (
            "/**\n * ParamFlowArgument\n */",
            "/**\n * 热点参数流控参数接口：实现类可自定义用于限流的键值。\n */",
        ),
        (
            "    /**\n     * @return the object as a key of param flow limit\n     */",
            "    /**\n     * @return 作为热点参数流控键的对象\n     */",
        ),
    ],
    "ParamFlowChecker.java": [
        (
            "/**\n * Rule checker for parameter flow control.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
            "/**\n * 热点参数流控规则校验器：支持本地 QPS/线程数限流与集群模式。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        ),
        (
            "        // Get parameter value.",
            "        // 提取参数值",
        ),
        (
            "        // Assign value with the result of paramFlowKey method",
            "        // 若参数实现 ParamFlowArgument，则使用 paramFlowKey() 作为实际限流键",
        ),
        (
            "        // If value is null, then pass",
            "        // 参数值为 null 时直接放行",
        ),
        (
            "        // Calculate max token count (threshold)",
            "        // 计算最大令牌数（阈值），含例外项单独阈值",
        ),
        (
            "            // Calculate the time duration since last token was added.",
            "            // 计算距上次补充令牌的时间间隔",
        ),
        (
            "            // A simplified token bucket algorithm that will replenish the tokens only when statistic window has passed.",
            "            // 简化令牌桶：仅在统计窗口过期后补充令牌",
        ),
        (
            "                // Token never added, just replenish the tokens and consume {@code acquireCount} immediately.",
            "                // 首次访问：初始化令牌并立即消耗 acquireCount",
        ),
        (
            "                // No available cluster client or server, fallback to local or\n                // pass in need.",
            "                // 无可用集群客户端/服务端，按配置降级本地校验或直接放行",
        ),
        (
            "            // The rule won't be activated, just pass.",
            "            // 未启用降级本地校验时，集群失败则直接放行",
        ),
    ],
    "ParamFlowClusterConfig.java": [
        (
            "/**\n * Parameter flow rule config in cluster mode.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
            "/**\n * 热点参数流控在集群模式下的配置项。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        ),
        (
            "    /**\n     * Global unique ID.\n     */",
            "    /** 集群流控规则的全局唯一 ID。 */",
        ),
        (
            "    /**\n     * Threshold type (average by local value or global value).\n     */",
            "    /** 阈值类型（按本地均值或全局均值统计）。 */",
        ),
        (
            "    /**\n     * The time interval length of the statistic sliding window (in milliseconds)\n     */",
            "    /** 统计滑动窗口的时间长度（毫秒）。 */",
        ),
    ],
    "ParamFlowException.java": [
        (
            "/**\n * Block exception for frequent (\"hot-spot\") parameter flow control.\n *\n * @author jialiang.linjl\n * @since 0.2.0\n */",
            "/**\n * 热点参数流控触发时抛出的阻塞异常。\n *\n * @author jialiang.linjl\n * @since 0.2.0\n */",
        ),
        (
            "    /**\n     * Get the parameter value that triggered the parameter flow control.\n     *\n     * @return the parameter value\n     * @since 1.4.2\n     */",
            "    /**\n     * 获取触发参数流控的参数值。\n     *\n     * @return 参数值字符串\n     * @since 1.4.2\n     */",
        ),
        (
            "    /**\n     * Get triggered rule.\n     * Note: the rule result is a reference to rule map and SHOULD NOT be modified.\n     *\n     * @return triggered rule\n     * @since 1.4.2\n     */",
            "    /**\n     * 获取触发的规则。\n     * 注意：返回的规则引用自内部规则映射，请勿修改。\n     *\n     * @return 触发的 ParamFlowRule\n     * @since 1.4.2\n     */",
        ),
    ],
    "ParamFlowItem.java": [
        (
            "/**\n * A flow control item for a specific parameter value.\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 0.2.0\n */",
            "/**\n * 针对特定参数值的流控例外项（单独阈值）。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 0.2.0\n */",
        ),
        (
            "    public static <T> ParamFlowItem newItem(T object, Integer count) {",
            "    /** 根据运行时对象构造例外项，自动记录 classType。 */\n    public static <T> ParamFlowItem newItem(T object, Integer count) {",
        ),
    ],
    "ParamFlowRule.java": [
        (
            "/**\n * Rules for \"hot-spot\" frequent parameter flow control.\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 0.2.0\n */",
            "/**\n * 热点参数流控规则：按方法参数索引对高频参数值进行 QPS 或并发线程数限流。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 0.2.0\n */",
        ),
        (
            "    /**\n     * The threshold type of flow control (0: thread count, 1: QPS).\n     */",
            "    /** 流控阈值类型（0：并发线程数，1：QPS）。 */",
        ),
        (
            "    /**\n     * Parameter index.\n     */",
            "    /** 待限流的方法参数索引（可为负数表示倒数）。 */",
        ),
        (
            "    /**\n     * The threshold count.\n     */",
            "    /** 默认阈值（QPS 或线程数）。 */",
        ),
        (
            "    /**\n     * Traffic shaping behavior (since 1.6.0).\n     */",
            "    /** 流量整形行为（自 1.6.0 起，如匀速排队）。 */",
        ),
        (
            "    /**\n     * Original exclusion items of parameters.\n     */",
            "    /** 参数例外项原始列表（JSON 序列化用）。 */",
        ),
        (
            "    /**\n     * Parsed exclusion items of parameters. Only for internal use.\n     */",
            "    /** 解析后的参数例外项映射，仅供内部使用。 */",
        ),
        (
            "    /**\n     * Indicating whether the rule is for cluster mode.\n     */",
            "    /** 是否启用集群流控模式。 */",
        ),
        (
            "    /**\n     * Cluster mode specific config for parameter flow rule.\n     */",
            "    /** 集群模式下的专用配置。 */",
        ),
    ],
    "ParamFlowRuleManager.java": [
        (
            "/**\n * Manager for frequent (\"hot-spot\") parameter flow rules.\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 0.2.0\n */",
            "/**\n * 热点参数流控规则管理器：加载、监听与按资源聚合规则。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 0.2.0\n */",
        ),
        (
            "    /**\n     * Load parameter flow rules. Former rules will be replaced.\n     *\n     * @param rules new rules to load.\n     */",
            "    /**\n     * 加载热点参数流控规则，全量替换已有规则。\n     *\n     * @param rules 新规则列表\n     */",
        ),
        (
            "    /**\n     * Listen to the {@link SentinelProperty} for {@link ParamFlowRule}s. The\n     * property is the source of {@link ParamFlowRule}s. Parameter flow rules\n     * can also be set by {@link #loadRules(List)} directly.\n     *\n     * @param property the property to listen\n     */",
            "    /**\n     * 注册 {@link SentinelProperty} 作为规则来源并监听变更；\n     * 也可直接调用 {@link #loadRules(List)} 设置规则。\n     *\n     * @param property 动态属性\n     */",
        ),
        (
            "    /**\n     * Get a copy of the rules.\n     *\n     * @return a new copy of the rules.\n     */",
            "    /**\n     * 获取全部规则的副本。\n     *\n     * @return 规则列表副本\n     */",
        ),
        (
            "                // No parameter flow rules, so clear all the metrics.",
            "                // 无参数流控规则时清空全部参数指标",
        ),
        (
            "            // Clear unused parameter metrics.",
            "            // 清理已移除资源或规则的参数指标",
        ),
    ],
    "ParamFlowRuleUtil.java": [
        (
            "/**\n * @author Eric Zhao\n */",
            "/**\n * 热点参数流控规则工具类：校验、解析例外项与构建资源映射。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    /**\n     * Check whether the provided rule is valid.\n     *\n     * @param rule any parameter rule\n     * @return true if valid, otherwise false\n     */",
            "    /**\n     * 校验参数流控规则是否合法。\n     *\n     * @param rule 待校验规则\n     * @return 合法返回 true，否则 false\n     */",
        ),
        (
            "    /**\n     * Fill the parameter rule with parsed items.\n     *\n     * @param rule valid parameter rule\n     */",
            "    /**\n     * 解析并填充规则的例外项映射。\n     *\n     * @param rule 合法规则\n     */",
        ),
        (
            "    /**\n     * Build the flow rule map from raw list of flow rules, grouping by resource name.\n     *\n     * @param list raw list of flow rules\n     * @return constructed new flow rule map; empty map if list is null or empty, or no valid rules\n     * @since 1.6.1\n     */",
            "    /**\n     * 将原始规则列表按资源名聚合为映射。\n     *\n     * @param list 原始规则列表\n     * @return 资源名 → 规则列表；无有效规则时返回空映射\n     * @since 1.6.1\n     */",
        ),
        (
            "            // Value should not be null.",
            "            // 例外项参数值不可为 null",
        ),
        (
            "            // If the class type is not provided, then treat it as string.",
            "            // 未指定类型时按字符串处理",
        ),
        (
            "        // Handle primitive type.",
            "        // 解析基本类型及包装类",
        ),
    ],
    "ParamFlowSlot.java": [
        (
            "/**\n * A processor slot that is responsible for flow control by frequent (\"hot spot\") parameters.\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 0.2.0\n */",
            "/**\n * 热点参数流控处理器槽：按方法参数值执行 QPS/线程数限流。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 0.2.0\n */",
        ),
        (
            "                // Illegal index, give it a illegal positive value, latter rule checking will pass.",
            "                // 非法索引转为正数占位，后续校验将放行",
        ),
        (
            "            // Initialize the parameter metrics.",
            "            // 初始化该规则对应的参数指标",
        ),
        (
            "                    // Assign actual value with the result of paramFlowKey method",
            "                    // 使用 paramFlowKey() 获取实际触发限流的参数值",
        ),
    ],
    "ParameterMetric.java": [
        (
            "/**\n * Metrics for frequent (\"hot spot\") parameters.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
            "/**\n * 热点参数指标：维护令牌桶、匀速排队计时器与线程计数。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        ),
        (
            "    /**\n     * Format: (rule, (value, timeRecorder))\n     *\n     * @since 1.6.0\n     */",
            "    /** 结构：(规则, (参数值, 上次通过时间))，用于匀速排队。 @since 1.6.0 */",
        ),
        (
            "    /**\n     * Format: (rule, (value, tokenCounter))\n     *\n     * @since 1.6.0\n     */",
            "    /** 结构：(规则, (参数值, 令牌状态))，用于 QPS 令牌桶。 @since 1.6.0 */",
        ),
        (
            "    /**\n     * Get the token counter for given parameter rule.\n     *\n     * @param rule valid parameter rule\n     * @return the associated token counter\n     * @since 1.8.8\n     */",
            "    /**\n     * 获取指定规则的令牌计数器映射。\n     *\n     * @param rule 合法参数规则\n     * @return 参数值 → 令牌状态\n     * @since 1.8.8\n     */",
        ),
        (
            "    /**\n     * Get the time record counter for given parameter rule.\n     *\n     * @param rule valid parameter rule\n     * @return the associated time counter\n     * @since 1.6.0\n     */",
            "    /**\n     * 获取指定规则的匀速排队时间记录器。\n     *\n     * @param rule 合法参数规则\n     * @return 参数值 → 上次通过时间\n     * @since 1.6.0\n     */",
        ),
        (
            "    /**\n     * Get the token counter map. Package-private for test.\n     *\n     * @return the token counter map\n     */",
            "    /**\n     * 获取令牌计数器映射（包级可见，供测试使用）。\n     *\n     * @return 规则 → 令牌映射\n     */",
        ),
    ],
    "ParameterMetricStorage.java": [
        (
            "/**\n * @author Eric Zhao\n * @since 1.6.1\n */",
            "/**\n * 热点参数指标存储：按资源名维护 {@link ParameterMetric} 实例。\n *\n * @author Eric Zhao\n * @since 1.6.1\n */",
        ),
        (
            "    /**\n     * Lock for a specific resource.\n     */",
            "    /** 创建指标时的全局锁。 */",
        ),
        (
            "    /**\n     * Init the parameter metric and index map for given resource.\n     * Package-private for test.\n     *\n     * @param resourceWrapper resource to init\n     * @param rule            relevant rule\n     */",
            "    /**\n     * 为指定资源初始化参数指标及规则相关计数器。\n     *\n     * @param resourceWrapper 资源包装\n     * @param rule 关联规则\n     */",
        ),
        (
            "        // Assume that the resource is valid.",
            "        // 假定资源名合法，按需懒创建 ParameterMetric",
        ),
    ],
    "RollingParamEvent.java": [
        (
            "    /**\n     * Indicates that the request successfully passed the slot chain (entry).\n     */",
            "    /** 请求已成功通过槽链（entry）。 */",
        ),
        (
            "    /**\n     * Indicates that the request is blocked by a specific slot.\n     */",
            "    /** 请求被某槽位阻塞。 */",
        ),
    ],
}
