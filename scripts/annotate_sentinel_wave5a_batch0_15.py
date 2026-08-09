#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-5a circuitbreaker/flow [0:15]."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
ORIGINAL = VER / "original"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))["files"][:15]

R: dict[str, list[tuple[str, str]]] = {}

R["AbstractCircuitBreaker.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.8.0\n */",
        "/**\n * 熔断器抽象基类，实现 {@link CircuitBreaker} 的状态机与通用逻辑。\n * <p>管理 CLOSED / OPEN / HALF_OPEN 三态转换，并通知 {@link CircuitBreakerStateChangeObserver}。</p>\n *\n * @author Eric Zhao\n * @since 1.8.0\n */",
    ),
    (
        "    public AbstractCircuitBreaker(DegradeRule rule) {",
        "    /** 使用默认 {@link EventObserverRegistry} 实例构造熔断器。 */\n    public AbstractCircuitBreaker(DegradeRule rule) {",
    ),
    (
        "    AbstractCircuitBreaker(DegradeRule rule, EventObserverRegistry observerRegistry) {",
        "    /** 指定状态变更观察者注册表构造熔断器（包内可见，便于测试）。 */\n    AbstractCircuitBreaker(DegradeRule rule, EventObserverRegistry observerRegistry) {",
    ),
    (
        "    @Override\n    public DegradeRule getRule() {",
        "    /** 返回关联的降级规则。 */\n    @Override\n    public DegradeRule getRule() {",
    ),
    (
        "    @Override\n    public State currentState() {",
        "    /** 返回当前熔断器状态。 */\n    @Override\n    public State currentState() {",
    ),
    (
        "    @Override\n    public boolean tryPass(Context context) {",
        "    /** 尝试获取调用许可：CLOSED 直接放行；OPEN 在恢复超时后转入 HALF_OPEN 并允许探测请求。 */\n    @Override\n    public boolean tryPass(Context context) {",
    ),
    (
        "    /**\n     * Reset the statistic data.\n     */",
        "    /**\n     * 重置统计数据。\n     */",
    ),
    (
        "    protected boolean retryTimeoutArrived() {",
        "    /** 判断是否已到达下次重试时间点。 */\n    protected boolean retryTimeoutArrived() {",
    ),
    (
        "    protected void updateNextRetryTimestamp() {",
        "    /** 根据恢复超时时间更新下次重试时间戳。 */\n    protected void updateNextRetryTimestamp() {",
    ),
    (
        "    protected boolean fromCloseToOpen(double snapshotValue) {",
        "    /** 从 CLOSED 转换到 OPEN，并通知观察者。 */\n    protected boolean fromCloseToOpen(double snapshotValue) {",
    ),
    (
        "    protected boolean fromOpenToHalfOpen(Context context) {",
        "    /** 从 OPEN 转换到 HALF_OPEN，注册 Entry 终止钩子以处理被后续规则阻断的探测请求。 */\n    protected boolean fromOpenToHalfOpen(Context context) {",
    ),
    (
        "    protected boolean fromHalfOpenToOpen(double snapshotValue) {",
        "    /** 从 HALF_OPEN 回退到 OPEN。 */\n    protected boolean fromHalfOpenToOpen(double snapshotValue) {",
    ),
    (
        "    protected boolean fromHalfOpenToClose() {",
        "    /** 从 HALF_OPEN 恢复到 CLOSED，并重置统计。 */\n    protected boolean fromHalfOpenToClose() {",
    ),
    (
        "    protected void transformToOpen(double triggerValue) {",
        "    /** 根据当前状态将熔断器转换到 OPEN。 */\n    protected void transformToOpen(double triggerValue) {",
    ),
]

R["CircuitBreaker.java"] = [
    (
        "/**\n * <p>Basic <a href=\"https://martinfowler.com/bliki/CircuitBreaker.html\">circuit breaker</a> interface.</p>\n *\n * @author Eric Zhao\n */",
        "/**\n * <p>基础<a href=\"https://martinfowler.com/bliki/CircuitBreaker.html\">熔断器</a>接口。</p>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Get the associated circuit breaking rule.\n     *\n     * @return associated circuit breaking rule\n     */",
        "    /**\n     * 获取关联的熔断规则。\n     *\n     * @return 关联的熔断规则\n     */",
    ),
    (
        "    /**\n     * Acquires permission of an invocation only if it is available at the time of invoking.\n     *\n     * @param context context of current invocation\n     * @return {@code true} if permission was acquired and {@code false} otherwise\n     */",
        "    /**\n     * 仅在调用时刻可用时获取一次调用的许可。\n     *\n     * @param context 当前调用上下文\n     * @return 获取成功返回 {@code true}，否则返回 {@code false}\n     */",
    ),
    (
        "    /**\n     * Get current state of the circuit breaker.\n     *\n     * @return current state of the circuit breaker\n     */",
        "    /**\n     * 获取熔断器当前状态。\n     *\n     * @return 熔断器当前状态\n     */",
    ),
    (
        "    /**\n     * <p>Record a completed request with the context and handle state transformation of the circuit breaker.</p>\n     * <p>Called when a <strong>passed</strong> invocation finished.</p>\n     *\n     * @param context context of current invocation\n     */",
        "    /**\n     * <p>记录一次已完成请求并处理熔断器状态转换。</p>\n     * <p>在<strong>已通过</strong>的调用结束时调用。</p>\n     *\n     * @param context 当前调用上下文\n     */",
    ),
    (
        "    /**\n     * Circuit breaker state.\n     */",
        "    /**\n     * 熔断器状态枚举。\n     */",
    ),
    (
        "        /**\n         * In {@code OPEN} state, all requests will be rejected until the next recovery time point.\n         */",
        "        /** {@code OPEN} 状态下，所有请求将被拒绝，直至到达下次恢复时间点。 */",
    ),
    (
        "        /**\n         * In {@code HALF_OPEN} state, the circuit breaker will allow a \"probe\" invocation.\n         * If the invocation is abnormal according to the strategy (e.g. it's slow), the circuit breaker\n         * will re-transform to the {@code OPEN} state and wait for the next recovery time point;\n         * otherwise the resource will be regarded as \"recovered\" and the circuit breaker\n         * will cease cutting off requests and transform to {@code CLOSED} state.\n         */",
        "        /**\n         * {@code HALF_OPEN} 状态下允许一次“探测”调用。\n         * 若调用按策略判定为异常（如慢调用），则重新转为 {@code OPEN} 并等待下次恢复；\n         * 否则视为资源已恢复，停止熔断并转为 {@code CLOSED}。\n         */",
    ),
    (
        "        /**\n         * In {@code CLOSED} state, all requests are permitted. When current metric value exceeds the threshold,\n         * the circuit breaker will transform to {@code OPEN} state.\n         */",
        "        /** {@code CLOSED} 状态下允许所有请求；当指标超过阈值时转为 {@code OPEN}。 */",
    ),
]

R["CircuitBreakerStateChangeObserver.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.8.0\n */",
        "/**\n * 熔断器状态变更观察者接口。\n *\n * @author Eric Zhao\n * @since 1.8.0\n */",
    ),
    (
        "    /**\n     * <p>Observer method triggered when circuit breaker state changed. The transformation could be:</p>\n     * <ul>\n     * <li>From {@code CLOSED} to {@code OPEN} (with the triggered metric)</li>\n     * <li>From {@code OPEN} to {@code HALF_OPEN}</li>\n     * <li>From {@code OPEN} to {@code CLOSED}</li>\n     * <li>From {@code HALF_OPEN} to {@code OPEN} (with the triggered metric)</li>\n     * </ul>\n     *\n     * @param prevState     previous state of the circuit breaker\n     * @param newState      new state of the circuit breaker\n     * @param rule          associated rule\n     * @param snapshotValue triggered value on circuit breaker opens (null if the new state is CLOSED or HALF_OPEN)\n     */",
        "    /**\n     * <p>熔断器状态变更时触发的回调。可能的转换包括：</p>\n     * <ul>\n     * <li>{@code CLOSED} → {@code OPEN}（携带触发指标）</li>\n     * <li>{@code OPEN} → {@code HALF_OPEN}</li>\n     * <li>{@code OPEN} → {@code CLOSED}</li>\n     * <li>{@code HALF_OPEN} → {@code OPEN}（携带触发指标）</li>\n     * </ul>\n     *\n     * @param prevState     熔断器先前状态\n     * @param newState      熔断器新状态\n     * @param rule          关联规则\n     * @param snapshotValue 熔断打开时的触发值（新状态为 CLOSED 或 HALF_OPEN 时为 null）\n     */",
    ),
]

R["CircuitBreakerStrategy.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.8.0\n */",
        "/**\n * 熔断策略枚举，定义触发熔断的指标类型。\n *\n * @author Eric Zhao\n * @since 1.8.0\n */",
    ),
    (
        "    /**\n     * Circuit breaker opens (cuts off) when slow request ratio exceeds the threshold.\n     */",
        "    /** 慢调用比例超过阈值时打开熔断（切断请求）。 */",
    ),
    (
        "    /**\n     * Circuit breaker opens (cuts off) when error ratio exceeds the threshold.\n     */",
        "    /** 异常比例超过阈值时打开熔断（切断请求）。 */",
    ),
    (
        "    /**\n     * Circuit breaker opens (cuts off) when error count exceeds the threshold.\n     */",
        "    /** 异常数超过阈值时打开熔断（切断请求）。 */",
    ),
    (
        "    public int getType() {",
        "    /** 返回策略对应的数值类型标识。 */\n    public int getType() {",
    ),
]

R["EventObserverRegistry.java"] = [
    (
        "/**\n * <p>Registry for circuit breaker event observers.</p>\n *\n * @author Eric Zhao\n * @since 1.8.0\n */",
        "/**\n * <p>熔断器事件观察者的注册表（单例）。</p>\n *\n * @author Eric Zhao\n * @since 1.8.0\n */",
    ),
    (
        "    /**\n     * Register a circuit breaker state change observer.\n     *\n     * @param name observer name\n     * @param observer a valid observer\n     */",
        "    /**\n     * 注册熔断器状态变更观察者。\n     *\n     * @param name 观察者名称\n     * @param observer 有效的观察者实例\n     */",
    ),
    (
        "    public boolean removeStateChangeObserver(String name) {",
        "    /** 按名称移除状态变更观察者。 */\n    public boolean removeStateChangeObserver(String name) {",
    ),
    (
        "    /**\n     * Get all registered state chane observers.\n     *\n     * @return all registered state chane observers\n     */",
        "    /**\n     * 获取所有已注册的状态变更观察者。\n     *\n     * @return 所有已注册的状态变更观察者\n     */",
    ),
    (
        "    public static EventObserverRegistry getInstance() {",
        "    /** 返回全局单例注册表。 */\n    public static EventObserverRegistry getInstance() {",
    ),
]

R["ExceptionCircuitBreaker.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.8.0\n */",
        "/**\n * 基于异常指标（异常比例或异常数）的熔断器实现。\n *\n * @author Eric Zhao\n * @since 1.8.0\n */",
    ),
    (
        "    public ExceptionCircuitBreaker(DegradeRule rule) {",
        "    /** 根据降级规则构造异常熔断器。 */\n    public ExceptionCircuitBreaker(DegradeRule rule) {",
    ),
    (
        "    ExceptionCircuitBreaker(DegradeRule rule, LeapArray<SimpleErrorCounter> stat) {",
        "    /** 指定统计窗口构造异常熔断器（包内可见，便于测试）。 */\n    ExceptionCircuitBreaker(DegradeRule rule, LeapArray<SimpleErrorCounter> stat) {",
    ),
    (
        "    @Override\n    protected void resetStat() {",
        "    /** 重置当前桶的异常计数。 */\n    @Override\n    protected void resetStat() {",
    ),
    (
        "    @Override\n    public void onRequestComplete(Context context) {",
        "    /** 请求完成后更新异常/总数统计，并检查是否触发熔断。 */\n    @Override\n    public void onRequestComplete(Context context) {",
    ),
    (
        "    static class SimpleErrorCounter {",
        "    /** 滑动窗口内的简单异常计数器。 */\n    static class SimpleErrorCounter {",
    ),
    (
        "        public SimpleErrorCounter reset() {",
        "        /** 重置异常数与总数。 */\n        public SimpleErrorCounter reset() {",
    ),
    (
        "    static class SimpleErrorCounterLeapArray extends LeapArray<SimpleErrorCounter> {",
        "    /** 异常计数的滑动窗口数组。 */\n    static class SimpleErrorCounterLeapArray extends LeapArray<SimpleErrorCounter> {",
    ),
    (
        "        @Override\n        public SimpleErrorCounter newEmptyBucket(long timeMillis) {",
        "        /** 创建空的异常计数桶。 */\n        @Override\n        public SimpleErrorCounter newEmptyBucket(long timeMillis) {",
    ),
    (
        "        @Override\n        protected WindowWrap<SimpleErrorCounter> resetWindowTo(WindowWrap<SimpleErrorCounter> w, long startTime) {",
        "        /** 重置窗口起始时间并清空计数。 */\n        @Override\n        protected WindowWrap<SimpleErrorCounter> resetWindowTo(WindowWrap<SimpleErrorCounter> w, long startTime) {",
    ),
]

R["ResponseTimeCircuitBreaker.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.8.0\n */",
        "/**\n * 基于响应时间（慢调用比例）的熔断器实现。\n *\n * @author Eric Zhao\n * @since 1.8.0\n */",
    ),
    (
        "    public ResponseTimeCircuitBreaker(DegradeRule rule) {",
        "    /** 根据降级规则构造 RT 熔断器。 */\n    public ResponseTimeCircuitBreaker(DegradeRule rule) {",
    ),
    (
        "    ResponseTimeCircuitBreaker(DegradeRule rule, LeapArray<SlowRequestCounter> stat) {",
        "    /** 指定统计窗口构造 RT 熔断器（包内可见，便于测试）。 */\n    ResponseTimeCircuitBreaker(DegradeRule rule, LeapArray<SlowRequestCounter> stat) {",
    ),
    (
        "    @Override\n    public void resetStat() {",
        "    /** 重置当前桶的慢调用计数。 */\n    @Override\n    public void resetStat() {",
    ),
    (
        "    @Override\n    public void onRequestComplete(Context context) {",
        "    /** 请求完成后统计 RT，更新慢调用比例并检查是否触发熔断。 */\n    @Override\n    public void onRequestComplete(Context context) {",
    ),
    (
        "    static class SlowRequestCounter {",
        "    /** 滑动窗口内的慢调用计数器。 */\n    static class SlowRequestCounter {",
    ),
    (
        "        public SlowRequestCounter reset() {",
        "        /** 重置慢调用数与总数。 */\n        public SlowRequestCounter reset() {",
    ),
    (
        "    static class SlowRequestLeapArray extends LeapArray<SlowRequestCounter> {",
        "    /** 慢调用计数的滑动窗口数组。 */\n    static class SlowRequestLeapArray extends LeapArray<SlowRequestCounter> {",
    ),
    (
        "        @Override\n        public SlowRequestCounter newEmptyBucket(long timeMillis) {",
        "        /** 创建空的慢调用计数桶。 */\n        @Override\n        public SlowRequestCounter newEmptyBucket(long timeMillis) {",
    ),
    (
        "        @Override\n        protected WindowWrap<SlowRequestCounter> resetWindowTo(WindowWrap<SlowRequestCounter> w, long startTime) {",
        "        /** 重置窗口起始时间并清空计数。 */\n        @Override\n        protected WindowWrap<SlowRequestCounter> resetWindowTo(WindowWrap<SlowRequestCounter> w, long startTime) {",
    ),
]

R["ClusterFlowConfig.java"] = [
    (
        "/**\n * Flow rule config in cluster mode.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群模式下的流控规则配置。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Global unique ID.\n     */",
        "    /** 全局唯一流控 ID。 */",
    ),
    (
        "    /**\n     * Threshold type (average by local value or global value).\n     */",
        "    /** 阈值类型（按本地均分或全局阈值）。 */",
    ),
    (
        "    /**\n     * 0: normal.\n     */",
        "    /** 集群策略：0 表示普通模式。 */",
    ),
    (
        "    /**\n     * The time interval length of the statistic sliding window (in milliseconds)\n     */",
        "    /** 统计滑动窗口的时间长度（毫秒）。 */",
    ),
    (
        "    /**\n     * if the client keep the token for more than resourceTimeout,resourceTimeoutStrategy will work.\n     */",
        "    /** 客户端持有令牌超过 resourceTimeout 时，resourceTimeoutStrategy 生效。 */",
    ),
    (
        "    /**\n     * 0:ignore,1:release the token.\n     */",
        "    /** 令牌超时策略：0 忽略，1 释放令牌。 */",
    ),
    (
        "    /**\n     * if the request(prioritized=true) is block,acquireRefuseStrategy will work..\n     * 0:ignore and block.\n     * 1:try again .\n     * 2:try until success.\n     */",
        "    /**\n     * 优先级请求（prioritized=true）被阻断时，acquireRefuseStrategy 生效：\n     * 0 忽略并阻断；1 重试一次；2 重试直至成功。\n     */",
    ),
    (
        "    /**\n     * if a client is offline,the server will delete all the token the client holds after clientOfflineTime.\n     */",
        "    /** 客户端离线后，服务端在 clientOfflineTime 之后删除其持有的全部令牌。 */",
    ),
]

R["ColdFactorProperty.java"] = [
    (
        "/**\n * @author jialiang.linjl\n */",
        "/**\n * 预热流控的冷启动因子配置，默认取自 {@link com.alibaba.csp.sentinel.config.SentinelConfig}。\n *\n * @author jialiang.linjl\n */",
    ),
    (
        "    public static volatile int coldFactor = SentinelConfig.coldFactor();",
        "    /** 冷启动因子，可在运行时调整。 */\n    public static volatile int coldFactor = SentinelConfig.coldFactor();",
    ),
]

R["FlowException.java"] = [
    (
        "/***\n * @author youji.zj\n */",
        "/**\n * 流控阻断异常，表示请求被流控规则拦截。\n *\n * @author youji.zj\n */",
    ),
    (
        "    public FlowException(String ruleLimitApp) {",
        "    /** 指定限流来源应用构造异常。 */\n    public FlowException(String ruleLimitApp) {",
    ),
    (
        "    public FlowException(String ruleLimitApp, FlowRule rule) {",
        "    /** 指定限流来源与触发的流控规则构造异常。 */\n    public FlowException(String ruleLimitApp, FlowRule rule) {",
    ),
    (
        "    public FlowException(String message, Throwable cause) {",
        "    /** 指定消息与原因构造异常。 */\n    public FlowException(String message, Throwable cause) {",
    ),
    (
        "    public FlowException(String ruleLimitApp, String message) {",
        "    /** 指定限流来源与消息构造异常。 */\n    public FlowException(String ruleLimitApp, String message) {",
    ),
    (
        "    @Override\n    public Throwable fillInStackTrace() {",
        "    /** 不填充堆栈，降低阻断异常开销。 */\n    @Override\n    public Throwable fillInStackTrace() {",
    ),
    (
        "    /**\n     * Get triggered rule.\n     * Note: the rule result is a reference to rule map and SHOULD NOT be modified.\n     *\n     * @return triggered rule\n     * @since 1.4.2\n     */",
        "    /**\n     * 获取触发的流控规则。\n     * 注意：返回的是规则映射中的引用，不应修改。\n     *\n     * @return 触发的流控规则\n     * @since 1.4.2\n     */",
    ),
]

R["FlowRule.java"] = [
    (
        "/**\n * <p>\n * Each flow rule is mainly composed of three factors: <strong>grade</strong>,\n * <strong>strategy</strong> and <strong>controlBehavior</strong>:\n * </p>\n * <ul>\n *     <li>The {@link #grade} represents the threshold type of flow control (by QPS or thread count).</li>\n *     <li>The {@link #strategy} represents the strategy based on invocation relation.</li>\n *     <li>The {@link #controlBehavior} represents the QPS shaping behavior (actions on incoming request when QPS\n *     exceeds the threshold).</li>\n * </ul>\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * <p>\n * 每条流控规则主要由三个要素组成：<strong>grade</strong>、<strong>strategy</strong> 与 <strong>controlBehavior</strong>：\n * </p>\n * <ul>\n *     <li>{@link #grade} 表示流控阈值类型（QPS 或线程数）。</li>\n *     <li>{@link #strategy} 表示基于调用关系的流控策略。</li>\n *     <li>{@link #controlBehavior} 表示 QPS 整形行为（超过阈值时对入站请求的处理方式）。</li>\n * </ul>\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * The threshold type of flow control (0: thread count, 1: QPS).\n     */",
        "    /** 流控阈值类型（0：线程数，1：QPS）。 */",
    ),
    (
        "    /**\n     * Flow control threshold count.\n     */",
        "    /** 流控阈值。 */",
    ),
    (
        "    /**\n     * Flow control strategy based on invocation chain.\n     *\n     * {@link RuleConstant#STRATEGY_DIRECT} for direct flow control (by origin);\n     * {@link RuleConstant#STRATEGY_RELATE} for relevant flow control (with relevant resource);\n     * {@link RuleConstant#STRATEGY_CHAIN} for chain flow control (by entrance resource).\n     */",
        "    /**\n     * 基于调用链的流控策略。\n     *\n     * {@link RuleConstant#STRATEGY_DIRECT} 直接流控（按来源）；\n     * {@link RuleConstant#STRATEGY_RELATE} 关联流控（关联资源）；\n     * {@link RuleConstant#STRATEGY_CHAIN} 链路流控（按入口资源）。\n     */",
    ),
    (
        "    /**\n     * Reference resource in flow control with relevant resource or context.\n     */",
        "    /** 关联流控或链路流控中的参考资源名。 */",
    ),
    (
        "    /**\n     * Rate limiter control behavior.\n     * 0. default(reject directly), 1. warm up, 2. rate limiter, 3. warm up + rate limiter\n     */",
        "    /**\n     * 流量整形控制行为。\n     * 0 默认（直接拒绝），1 预热，2 匀速排队，3 预热 + 匀速排队。\n     */",
    ),
    (
        "    /**\n     * Max queueing time in rate limiter behavior.\n     */",
        "    /** 匀速排队模式下的最大排队时间（毫秒）。 */",
    ),
    (
        "    /**\n     * Flow rule config for cluster mode.\n     */",
        "    /** 集群模式下的流控配置。 */",
    ),
    (
        "    /**\n     * The traffic shaping (throttling) controller.\n     */",
        "    /** 流量整形（节流）控制器。 */",
    ),
    (
        "    FlowRule setRater(TrafficShapingController rater) {",
        "    /** 设置流量整形控制器（包内可见）。 */\n    FlowRule setRater(TrafficShapingController rater) {",
    ),
    (
        "    TrafficShapingController getRater() {",
        "    /** 获取流量整形控制器（包内可见）。 */\n    TrafficShapingController getRater() {",
    ),
]

R["FlowRuleChecker.java"] = [
    (
        "/**\n * Rule checker for flow control rules.\n *\n * @author Eric Zhao\n */",
        "/**\n * 流控规则检查器，负责本地与集群模式的令牌校验。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public void checkFlow(Function<String, Collection<FlowRule>> ruleProvider, ResourceWrapper resource,\n                          Context context, DefaultNode node, int count, boolean prioritized) throws BlockException {",
        "    /** 对资源应用全部流控规则，任一不通过则抛出 {@link FlowException}。 */\n    public void checkFlow(Function<String, Collection<FlowRule>> ruleProvider, ResourceWrapper resource,\n                          Context context, DefaultNode node, int count, boolean prioritized) throws BlockException {",
    ),
    (
        "    public boolean canPassCheck(/*@NonNull*/ FlowRule rule, Context context, DefaultNode node,\n                                                    int acquireCount) {",
        "    /** 检查单条规则是否允许通过（非优先级请求）。 */\n    public boolean canPassCheck(/*@NonNull*/ FlowRule rule, Context context, DefaultNode node,\n                                                    int acquireCount) {",
    ),
    (
        "    public boolean canPassCheck(/*@NonNull*/ FlowRule rule, Context context, DefaultNode node, int acquireCount,\n                                                    boolean prioritized) {",
        "    /** 检查单条规则是否允许通过，支持优先级请求。 */\n    public boolean canPassCheck(/*@NonNull*/ FlowRule rule, Context context, DefaultNode node, int acquireCount,\n                                                    boolean prioritized) {",
    ),
    (
        "    static Node selectReferenceNode(FlowRule rule, Context context, DefaultNode node) {",
        "    /** 按策略选择关联或链路流控的统计节点。 */\n    static Node selectReferenceNode(FlowRule rule, Context context, DefaultNode node) {",
    ),
    (
        "    static Node selectNodeByRequesterAndStrategy(/*@NonNull*/ FlowRule rule, Context context, DefaultNode node) {",
        "    /** 根据调用来源与流控策略选择用于计数的节点。 */\n    static Node selectNodeByRequesterAndStrategy(/*@NonNull*/ FlowRule rule, Context context, DefaultNode node) {",
    ),
]

R["FlowRuleComparator.java"] = [
    (
        "/**\n * Comparator for flow rules.\n *\n * @author jialiang.linjl\n */",
        "/**\n * 流控规则比较器，用于规则加载时的排序。\n * <p>集群模式规则排在末尾；默认来源（default）规则优先级最低。</p>\n *\n * @author jialiang.linjl\n */",
    ),
    (
        "    @Override\n    public int compare(FlowRule o1, FlowRule o2) {",
        "    /** 比较两条流控规则的优先级顺序。 */\n    @Override\n    public int compare(FlowRule o1, FlowRule o2) {",
    ),
]

R["FlowRuleManager.java"] = [
    (
        "/**\n * <p>\n * One resources can have multiple rules. And these rules take effects in the following order:\n * <ol>\n * <li>requests from specified caller</li>\n * <li>no specified caller</li>\n * </ol>\n * </p>\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @author Weihua\n */",
        "/**\n * <p>\n * 一个资源可配置多条流控规则，生效顺序如下：\n * <ol>\n * <li>指定调用来源的规则</li>\n * <li>未指定调用来源的规则</li>\n * </ol>\n * </p>\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @author Weihua\n */",
    ),
    (
        "    /**\n     * <p> Start the MetricTimerListener\n     * <ol>\n     *     <li>If the flushInterval more than 0,\n     * the timer will run with the flushInterval as the rate </li>.\n     *      <li>If the flushInterval less than 0(include) or value is not valid,\n     * then means the timer will not be started </li>\n     * <ol></p>\n     */",
        "    /**\n     * <p>启动 {@link MetricTimerListener} 定时任务：</p>\n     * <ol>\n     *     <li>flushInterval &gt; 0 时，按该间隔周期性运行；</li>\n     *     <li>flushInterval ≤ 0 或配置无效时，不启动定时器。</li>\n     * </ol>\n     */",
    ),
    (
        "    /**\n     * Listen to the {@link SentinelProperty} for {@link FlowRule}s. The property is the source of {@link FlowRule}s.\n     * Flow rules can also be set by {@link #loadRules(List)} directly.\n     *\n     * @param property the property to listen.\n     */",
        "    /**\n     * 监听 {@link FlowRule} 的动态配置源 {@link SentinelProperty}。\n     * 也可通过 {@link #loadRules(List)} 直接加载规则。\n     *\n     * @param property 待监听的配置属性\n     */",
    ),
    (
        "    /**\n     * Get a copy of the rules.\n     *\n     * @return a new copy of the rules.\n     */",
        "    /**\n     * 获取全部流控规则的副本。\n     *\n     * @return 规则列表副本\n     */",
    ),
    (
        "    /**\n     * Load {@link FlowRule}s, former rules will be replaced.\n     *\n     * @param rules new rules to load.\n     */",
        "    /**\n     * 加载 {@link FlowRule} 列表，替换原有规则。\n     *\n     * @param rules 新规则列表\n     */",
    ),
    (
        "    static List<FlowRule> getFlowRules(String resource) {",
        "    /** 获取指定资源名下的流控规则（包内可见）。 */\n    static List<FlowRule> getFlowRules(String resource) {",
    ),
    (
        "    public static boolean hasConfig(String resource) {",
        "    /** 判断资源是否已配置流控规则。 */\n    public static boolean hasConfig(String resource) {",
    ),
    (
        "    public static boolean isOtherOrigin(String origin, String resourceName) {",
        "    /** 判断来源是否为该资源的“其他”来源（未单独配置的 caller）。 */\n    public static boolean isOtherOrigin(String origin, String resourceName) {",
    ),
]

R["FlowRuleUtil.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 流控规则工具类：构建规则映射、校验规则合法性并生成流量整形控制器。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Build the flow rule map from raw list of flow rules, grouping by resource name.\n     *\n     * @param list raw list of flow rules\n     * @return constructed new flow rule map; empty map if list is null or empty, or no valid rules\n     */",
        "    /**\n     * 从原始流控规则列表按资源名分组构建规则映射。\n     *\n     * @param list 原始流控规则列表\n     * @return 新构建的规则映射；列表为空或无有效规则时返回空映射\n     */",
    ),
    (
        "    /**\n     * Build the flow rule map from raw list of flow rules, grouping by resource name.\n     *\n     * @param list   raw list of flow rules\n     * @param filter rule filter\n     * @return constructed new flow rule map; empty map if list is null or empty, or no wanted rules\n     */",
        "    /**\n     * 从原始流控规则列表按资源名分组构建规则映射，支持过滤器。\n     *\n     * @param list   原始流控规则列表\n     * @param filter 规则过滤器\n     * @return 新构建的规则映射；无匹配规则时返回空映射\n     */",
    ),
    (
        "    /**\n     * Build the flow rule map from raw list of flow rules, grouping by resource name.\n     *\n     * @param list       raw list of flow rules\n     * @param filter     rule filter\n     * @param shouldSort whether the rules should be sorted\n     * @return constructed new flow rule map; empty map if list is null or empty, or no wanted rules\n     */",
        "    /**\n     * 从原始流控规则列表按资源名分组构建规则映射，可选排序。\n     *\n     * @param list       原始流控规则列表\n     * @param filter     规则过滤器\n     * @param shouldSort 是否对规则排序\n     * @return 新构建的规则映射\n     */",
    ),
    (
        "    /**\n     * Build the flow rule map from raw list of flow rules, grouping by provided group function.\n     *\n     * @param list          raw list of flow rules\n     * @param groupFunction grouping function of the map (by key)\n     * @param filter        rule filter\n     * @param shouldSort    whether the rules should be sorted\n     * @param <K>           type of key\n     * @return constructed new flow rule map; empty map if list is null or empty, or no wanted rules\n     */",
        "    /**\n     * 从原始流控规则列表按自定义分组函数构建规则映射。\n     *\n     * @param list          原始流控规则列表\n     * @param groupFunction 分组键提取函数\n     * @param filter        规则过滤器\n     * @param shouldSort    是否对规则排序\n     * @param <K>           映射键类型\n     * @return 新构建的规则映射\n     */",
    ),
    (
        "    /**\n     * Check whether provided ID can be a valid cluster flow ID.\n     *\n     * @param id flow ID to check\n     * @return true if valid, otherwise false\n     */",
        "    /**\n     * 校验集群流控 ID 是否有效。\n     *\n     * @param id 待校验的流控 ID\n     * @return 有效返回 true，否则 false\n     */",
    ),
    (
        "    /**\n     * Check whether provided flow rule is valid.\n     *\n     * @param rule flow rule to check\n     * @return true if valid, otherwise false\n     */",
        "    /**\n     * 校验流控规则是否合法。\n     *\n     * @param rule 待校验的流控规则\n     * @return 合法返回 true，否则 false\n     */",
    ),
    (
        "    public static boolean checkClusterConcurrentField(/*@NonNull*/ FlowRule rule) {",
        "    /** 校验线程数流控的集群配置字段。 */\n    public static boolean checkClusterConcurrentField(/*@NonNull*/ FlowRule rule) {",
    ),
    (
        "    public static boolean isWindowConfigValid(int sampleCount, int windowIntervalMs) {",
        "    /** 校验滑动窗口采样数与时间间隔是否合法（间隔须能被采样数整除）。 */\n    public static boolean isWindowConfigValid(int sampleCount, int windowIntervalMs) {",
    ),
]


def apply(text: str, reps: list[tuple[str, str]]) -> str:
    for old, new in reps:
        if old in text:
            text = text.replace(old, new)
    return text


def ensure_analyzed(rel: str) -> Path:
    dst = ANALYZED / rel
    if not dst.exists():
        src = ORIGINAL / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")
    return dst


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        dst = ensure_analyzed(rel)
        name = Path(rel).name
        text = dst.read_text(encoding="utf-8")
        if len(re.findall(r"[\u4e00-\u9fff]", text)) >= 10:
            ok += 1
            print(f"SKIP {rel}")
            continue
        text = apply(text, R.get(name, []))
        cn = len(re.findall(r"[\u4e00-\u9fff]", text))
        if cn < 10 or "Licensed under the Apache License" not in text:
            failures.append(f"VALIDATION cn={cn}: {rel}")
            print(f"FAIL cn={cn} {rel}")
            continue
        dst.write_text(text, encoding="utf-8")
        ok += 1
        print(f"OK cn={cn} {rel}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
