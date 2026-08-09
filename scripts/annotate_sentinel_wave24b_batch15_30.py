#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-24b block [15:30] (basic demos + cluster embedded start)."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = Path("/tmp/sentinel_w24b.txt").read_text(encoding="utf-8").strip().split("\n")
SCRIPT_NAME = "annotate_sentinel_wave24b_batch15_30.py"
MARK_NOTE = "wave24b [15:30]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-demo/sentinel-demo-apollo-datasource/src/main/java/com/alibaba/csp/sentinel/demo/datasource/apollo/FlowQpsRunner.java"] = [
    (
        "/**\n * Flow QPS runner.\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
        "/**\n * Apollo 动态数据源演示用的 QPS 流量压测 Runner：\n * 多线程对指定资源发起 {@link SphU#entry} 调用并每秒统计 pass/block 数量。\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
    ),
    (
        "    public FlowQpsRunner(String resourceName, int threadCount, int seconds) {",
        "    /** 构造压测 Runner：指定资源名、并发线程数与统计时长（秒）。 */\n    public FlowQpsRunner(String resourceName, int threadCount, int seconds) {",
    ),
    (
        "    public void simulateTraffic() {",
        "    /** 启动 {@link #threadCount} 个线程持续对资源发起 entry 请求。 */\n    public void simulateTraffic() {",
    ),
    (
        "    public void tick() {",
        "    /** 启动定时统计线程，每秒输出 pass/block QPS。 */\n    public void tick() {",
    ),
    (
        "                    // token acquired, means pass",
        "                    // 成功获取令牌，计为通过",
    ),
    (
        "                    // biz exception",
        "                    // 业务异常（本 demo 未模拟）",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/AsyncEntryDemo.java"] = [
    (
        "/**\n * An example for asynchronous entry in Sentinel.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * Sentinel 异步 {@link AsyncEntry} 用法示例：演示异步 entry 与同步 entry 嵌套、\n * {@link ContextUtil#runOnContext} 绑定异步上下文及调用链结构。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    private void invoke(String arg, Consumer<String> handler) {",
        "    /** 模拟异步 RPC：延迟 3 秒后在另一线程回调 handler。 */\n    private void invoke(String arg, Consumer<String> handler) {",
    ),
    (
        "            final AsyncEntry entry = SphU.asyncEntry(\"test-another-async\");",
        "            // 创建异步 entry，资源名 test-another-async\n            final AsyncEntry entry = SphU.asyncEntry(\"test-another-async\");",
    ),
    (
        "                        // Normal entry nested in asynchronous entry.",
        "                        // 在异步 entry 内嵌套同步 entry",
    ),
    (
        "                        // Ignore.",
        "                        // 忽略中断",
    ),
    (
        "                // If no nested entry later, we don't have to wrap in `ContextUtil.runOnContext()`.",
        "                // 若回调内无嵌套 entry，可不使用 ContextUtil.runOnContext()",
    ),
    (
        "                    // Here to handle the async result (without other entry).",
        "                    // 在此处理异步结果（无其他 entry）",
    ),
    (
        "                    // Exit the async entry.",
        "                    // 退出异步 entry",
    ),
    (
        "            // Request blocked, handle the exception.",
        "            // 请求被限流，处理 BlockException",
    ),
    (
        "            // First we call an asynchronous resource.",
        "            // 先调用异步资源 test-async",
    ),
    (
        "                // The thread is different from original caller thread for async entry.",
        "                // 回调线程与发起 asyncEntry 的线程不同",
    ),
    (
        "                // So we need to wrap in the async context so that nested invocation entry\n"
        "                // can be linked to the parent asynchronous entry.",
        "                // 须在异步上下文中执行，使嵌套 entry 挂到父异步 entry 调用链",
    ),
    (
        "                        // In the callback, we do another async invocation several times under the async context.",
        "                        // 在回调中多次触发 anotherAsync()",
    ),
    (
        "                        // Then we do a sync (normal) entry under current async context.",
        "                        // 再在异步上下文中做同步 entry",
    ),
    (
        "            // Then we call a sync resource.",
        "            // 随后调用同步资源 test-sync",
    ),
    (
        "        // Expected invocation chain:",
        "        // 预期调用链：",
    ),
    (
        "            System.out.println(\"Do something...\");",
        "            System.out.println(\"Do something...\"); // 触发异步+同步嵌套调用",
    ),
    (
        "        // Rule 1 won't take effect as the limitApp doesn't match.",
        "        // 规则 1 的 limitApp 为 originB，与当前 originA 不匹配，不生效",
    ),
    (
        "        // Rule 2 will take effect.",
        "        // 规则 2 对 test-another-async 限 QPS=5，会生效",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/authority/AuthorityDemo.java"] = [
    (
        "/**\n * Authority rule is designed for limiting by request origins. In blacklist mode,\n * requests will be blocked when blacklist contains current origin, otherwise will pass.\n * In whitelist mode, only requests from whitelist origin can pass.\n *\n * @author Eric Zhao\n */",
        "/**\n * 黑白名单（Authority）规则演示：按请求来源 origin 限流。\n * 黑名单模式：origin 在名单中则拒绝；白名单模式：仅名单内 origin 可通过。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        System.out.println(\"========Testing for black list========\");",
        "        System.out.println(\"======== 黑名单模式测试 ========\");",
    ),
    (
        "        System.out.println(\"========Testing for white list========\");",
        "        System.out.println(\"======== 白名单模式测试 ========\");",
    ),
    (
        "    private static void testFor(/*@NonNull*/ String resource, /*@NonNull*/ String origin) {",
        "    /** 以指定 origin 进入上下文并尝试 entry，打印通过或拦截结果。 */\n    private static void testFor(/*@NonNull*/ String resource, /*@NonNull*/ String origin) {",
    ),
    (
        "        rule.setStrategy(RuleConstant.AUTHORITY_WHITE);",
        "        rule.setStrategy(RuleConstant.AUTHORITY_WHITE); // 白名单",
    ),
    (
        "        rule.setStrategy(RuleConstant.AUTHORITY_BLACK);",
        "        rule.setStrategy(RuleConstant.AUTHORITY_BLACK); // 黑名单",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/degrade/ExceptionRatioCircuitBreakerDemo.java"] = [
    (
        "/**\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * 异常比例熔断演示：当窗口内异常占比超过阈值时打开熔断器，\n * 拒绝后续请求直至 {@link DegradeRule#getTimeWindow()} 秒后进入半开探测。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "                        // Error probability is 45%",
        "                        // 约 45% 概率抛出业务异常",
    ),
    (
        "                            // biz code raise an exception.",
        "                            // 模拟业务代码异常",
    ),
    (
        "                        // It's required to record exception here manually.",
        "                        // 须手动调用 Tracer 记录异常，供熔断统计",
    ),
    (
        "            // Set ratio threshold to 50%.",
        "            // 异常比例阈值 50%",
    ),
    (
        "            // Retry timeout (in second)",
        "            // 熔断打开后恢复探测间隔（秒）",
    ),
    (
        "            // ignore",
        "            // 忽略中断",
    ),
    (
        "            System.out.println(\"Begin to run! Go go go!\");",
        "            System.out.println(\"Begin to run! Go go go!\"); // 启动并发压测",
    ),
    (
        "            System.out.println(\"See corresponding metrics.log for accurate statistic data\");",
        "            System.out.println(\"See corresponding metrics.log for accurate statistic data\"); // 精确指标见 metrics.log",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/degrade/SlowRatioCircuitBreakerDemo.java"] = [
    (
        "/**\n * Run this demo, and the output will be like:",
        "/**\n * 慢调用比例熔断演示。运行后输出类似：",
    ),
    (
        " * 1529399829825,total:19179, pass:4, block:19176 // circuit breaker opens",
        " * 1529399829825,total:19179, pass:4, block:19176 // 熔断器打开",
    ),
    (
        " * 1529399839829,total:14494, pass:104, block:14390 // After 10 seconds, the system restored",
        " * 1529399839829,total:14494, pass:104, block:14390 // 10 秒后系统恢复",
    ),
    (
        "                        // RT: [40ms, 60ms)",
        "                        // 模拟 RT 在 [40ms, 60ms)",
    ),
    (
        "            // Max allowed response time",
        "            // 最大允许 RT（毫秒）",
    ),
    (
        "            // Retry timeout (in second)",
        "            // 熔断恢复探测间隔（秒）",
    ),
    (
        "            // Circuit breaker opens when slow request ratio > 60%",
        "            // 慢调用比例超过 60% 时打开熔断",
    ),
    (
        "            // ignore",
        "            // 忽略中断",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/flow/FlowQpsDemo.java"] = [
    (
        "/**\n * @author jialiang.linjl\n */",
        "/**\n * 基础 QPS 流控演示：32 线程压测，规则限制每秒仅 20 个请求通过。\n *\n * @author jialiang.linjl\n */",
    ),
    (
        "        // first make the system run on a very low condition",
        "        // 先启动压测线程",
    ),
    (
        "        System.out.println(\"===== begin to do flow control\");",
        "        System.out.println(\"===== 开始流控演示\");",
    ),
    (
        "        System.out.println(\"only 20 requests per second can pass\");",
        "        System.out.println(\"每秒仅允许 20 个请求通过\");",
    ),
    (
        "        // set limit qps to 20",
        "        // QPS 阈值设为 20",
    ),
    (
        "                    // token acquired, means pass",
        "                    // 获取令牌成功，计为通过",
    ),
    (
        "                    // biz exception",
        "                    // 业务异常",
    ),
    (
        "                    // ignore",
        "                    // 忽略中断",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/flow/FlowQpsRegexDemo.java"] = [
    (
        "public class FlowQpsRegexDemo {",
        "/** 正则资源名 QPS 流控演示：规则 {@code /A/.*} 匹配以 /A/ 开头的资源。 */\npublic class FlowQpsRegexDemo {",
    ),
    (
        "        // first make the system run on a very low condition",
        "        // 为各资源启动压测线程",
    ),
    (
        "        System.out.println(\"===== begin to do flow control\");",
        "        System.out.println(\"===== 开始正则流控演示\");",
    ),
    (
        "        System.out.println(\"Resources prefixed with /A/ can only pass 20 requests per second\");",
        "        System.out.println(\"以 /A/ 为前缀的资源每秒最多通过 20 个请求\");",
    ),
    (
        "        // set limit qps to 20",
        "        // QPS 阈值 20，regex=true 启用正则匹配",
    ),
    (
        "                    // token acquired, means pass",
        "                    // 获取令牌成功",
    ),
    (
        "                    // biz exception",
        "                    // 业务异常",
    ),
    (
        "                    // ignore",
        "                    // 忽略中断",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/flow/FlowThreadDemo.java"] = [
    (
        "/**\n * @author jialiang.linjl\n */",
        "/**\n * 并发线程数流控演示：限制 methodA 同时最多 20 个线程，\n * methodB 变慢后 methodA 占用线程时间缩短，通过量会上升。\n *\n * @author jialiang.linjl\n */",
    ),
    (
        "        System.out.println(\n            \"MethodA will call methodB. After running for a while, methodB becomes fast, \"\n                + \"which make methodA also become fast \");",
        "        System.out.println(\n            \"methodA 会调用 methodB；运行一段时间后 methodB 变快，methodA 也会更快释放线程 \");",
    ),
    (
        "        // set limit concurrent thread for 'methodA' to 20",
        "        // methodA 并发线程数上限 20",
    ),
    (
        "                            // biz exception",
        "                            // 业务异常",
    ),
    (
        "                    System.out.println(\"method B is running much faster; more requests are allowed to pass\");",
        "                    System.out.println(\"methodB 变快，更多 methodA 请求得以通过\");",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/flow/PaceFlowDemo.java"] = [
    (
        "/**\n * <p>\n * If {@link RuleConstant#CONTROL_BEHAVIOR_RATE_LIMITER} is set, incoming",
        "/**\n * <p>\n * 当流控行为为 {@link RuleConstant#CONTROL_BEHAVIOR_RATE_LIMITER}（匀速排队）时，",
    ),
    (
        " * requests are passing at regular interval. When a new request arrives, the\n * flow rule checks whether the interval between the new request and the\n * previous request. If the interval is less than the count set in the rule\n * first. If the interval is large, it will pass the request; otherwise,\n * sentinel will calculate the waiting time for this request. If the waiting\n * time is longer than the {@link FlowRule#maxQueueingTimeMs} set in the rule,\n * the request will be rejected immediately.\n *\n * This method is widely used for pulsed flow. When a large amount of flow\n * comes, we don't want to pass all these requests at once, which may drag the\n * system down. We can make the system handle these requests at a steady pace by\n * using this kind of rules.",
        " * 请求以固定间隔通过。新请求到达时检查与上一请求的间隔；\n * 间隔不足则排队等待，超过 {@link FlowRule#maxQueueingTimeMs} 则直接拒绝。\n * 适用于脉冲流量：避免瞬时洪峰拖垮系统，改为匀速放行。\n *",
    ),
    (
        " * This demo demonstrates how to use {@link RuleConstant#CONTROL_BEHAVIOR_RATE_LIMITER}.",
        " * 本 demo 演示 {@link RuleConstant#CONTROL_BEHAVIOR_RATE_LIMITER} 与默认拒绝行为对比。",
    ),
    (
        " * {@link #simulatePulseFlow()} simulates 100 requests that arrives at almost the\n * same time. All these 100 request are passed at a fixed interval.",
        " * {@link #simulatePulseFlow()} 模拟 100 个几乎同时到达的请求，匀速排队后全部通过。",
    ),
    (
        " * Then we invoke {@link #initDefaultFlowRule()} to set rules with default behavior, and only 10\n * requests will be allowed to pass, other requests will be rejected immediately.",
        " * 随后切换为 {@link #initDefaultFlowRule()} 默认行为，仅 10 个通过、其余立即拒绝。",
    ),
    (
        "        System.out.println(\"pace behavior\");",
        "        System.out.println(\"匀速排队行为（pace behavior）\");",
    ),
    (
        "        System.out.println(\"default behavior\");",
        "        System.out.println(\"默认快速失败行为（default behavior）\");",
    ),
    (
        "         * CONTROL_BEHAVIOR_RATE_LIMITER means requests more than threshold will be queueing in the queue,\n         * until the queueing time is more than {@link FlowRule#maxQueueingTimeMs}, the requests will be rejected.",
        "         * CONTROL_BEHAVIOR_RATE_LIMITER：超阈值请求进入队列排队，\n         * 排队时间超过 {@link FlowRule#maxQueueingTimeMs} 则拒绝。",
    ),
    (
        "        // CONTROL_BEHAVIOR_DEFAULT means requests more than threshold will be rejected immediately.",
        "        // CONTROL_BEHAVIOR_DEFAULT：超阈值立即拒绝",
    ),
    (
        "                    // biz exception",
        "                    // 业务异常",
    ),
    (
        "                        // ignore",
        "                        // 忽略中断",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/flow/WarmUpFlowDemo.java"] = [
    (
        "/**\n * When {@link FlowRule#controlBehavior} set to {@link RuleConstant#CONTROL_BEHAVIOR_WARM_UP}, real passed qps will\n * gradually increase to {@link FlowRule#count}, other than burst increasing.",
        "/**\n * 预热（Warm Up）流控：{@link RuleConstant#CONTROL_BEHAVIOR_WARM_UP} 下\n * 通过 QPS 在 {@link FlowRule#warmUpPeriodSec} 内渐增至 {@link FlowRule#count}，避免冷启动被冲垮。",
    ),
    (
        "        // trigger Sentinel internal init",
        "        // 触发 Sentinel 内部初始化",
    ),
    (
        "        //first make the system run on a very low condition",
        "        // 先以低 QPS 预热运行约 20 秒",
    ),
    (
        "         * Start more thread to simulate more qps. Since we use {@link RuleConstant.CONTROL_BEHAVIOR_WARM_UP} as\n         * {@link FlowRule#controlBehavior}, real passed qps will increase to {@link FlowRule#count} in\n         * {@link FlowRule#warmUpPeriodSec} seconds.",
        "         * 再启动大量线程模拟 QPS 突增；预热期内通过 QPS 在 warmUpPeriodSec 内渐增至 count。",
    ),
    (
        "                    // token acquired, means pass",
        "                    // 获取令牌，计为通过",
    ),
    (
        "                    // biz exception",
        "                    // 业务异常",
    ),
    (
        "                    // ignore",
        "                    // 忽略中断",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/flow/WarmUpRateLimiterFlowDemo.java"] = [
    (
        "/**\n * When {@link FlowRule#controlBehavior} set to {@link RuleConstant#CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER}, real passed\n * qps will gradually increase to {@link FlowRule#count}, other than burst increasing, and after the passed qps reaches\n * the threshold, the request will pass at a constant interval.",
        "/**\n * 预热匀速器流控：{@link RuleConstant#CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER} 结合\n * 预热与匀速排队，QPS 渐增到阈值后以固定间隔放行。\n * 等价于 {@link RuleConstant#CONTROL_BEHAVIOR_WARM_UP} + {@link RuleConstant#CONTROL_BEHAVIOR_RATE_LIMITER}。",
    ),
    (
        "        // trigger Sentinel internal init",
        "        // 触发 Sentinel 内部初始化",
    ),
    (
        "        //first make the system run on a very low condition",
        "        // 先低 QPS 运行 5 秒",
    ),
    (
        "        // request qps burst increase, warm up behavior triggered.",
        "        // 突发高 QPS，触发预热+匀速行为",
    ),
    (
        "                    // token acquired, means pass",
        "                    // 获取令牌，计为通过",
    ),
    (
        "                    // biz exception",
        "                    // 业务异常",
    ),
    (
        "                    // ignore",
        "                    // 忽略中断",
    ),
]

R["sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/system/SystemGuardDemo.java"] = [
    (
        "/**\n * @author jialiang.linjl\n */",
        "/**\n * 系统保护规则演示：对系统负载、CPU、平均 RT、入口 QPS、并发线程数设全局阈值。\n *\n * @author jialiang.linjl\n */",
    ),
    (
        "        // max load is 3",
        "        // 最高系统负载 3.0",
    ),
    (
        "        // max cpu usage is 60%",
        "        // 最高 CPU 使用率 60%",
    ),
    (
        "        // max avg rt of all request is 10 ms",
        "        // 全局平均 RT 上限 10 ms",
    ),
    (
        "        // max total qps is 20",
        "        // 入口总 QPS 上限 20",
    ),
    (
        "        // max parallel working thread is 10",
        "        // 最大并发工作线程 10",
    ),
    (
        "                            // ignore",
        "                            // 忽略中断",
    ),
    (
        "                            // biz exception",
        "                            // 业务异常",
    ),
]

R["sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-embedded/src/main/java/com/alibaba/csp/sentinel/demo/cluster/DemoConstants.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 集群流控嵌入式 Demo 的 Apollo/Nacos 等动态配置 Key 后缀常量。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public static final String FLOW_POSTFIX = \"-flow-rules\";",
        "    /** 流控规则配置 Key 后缀。 */\n    public static final String FLOW_POSTFIX = \"-flow-rules\";",
    ),
    (
        "    public static final String PARAM_FLOW_POSTFIX = \"-param-rules\";",
        "    /** 热点参数规则配置 Key 后缀。 */\n    public static final String PARAM_FLOW_POSTFIX = \"-param-rules\";",
    ),
    (
        "    public static final String SERVER_NAMESPACE_SET_POSTFIX = \"-cs-namespace-set\";",
        "    /** 集群服务端命名空间集合配置 Key 后缀。 */\n    public static final String SERVER_NAMESPACE_SET_POSTFIX = \"-cs-namespace-set\";",
    ),
    (
        "    public static final String CLIENT_CONFIG_POSTFIX = \"-cc-config\";",
        "    /** 集群客户端配置 Key 后缀。 */\n    public static final String CLIENT_CONFIG_POSTFIX = \"-cc-config\";",
    ),
    (
        "    public static final String CLUSTER_MAP_POSTFIX = \"-cluster-map\";",
        "    /** 集群 Token Server/Client 映射配置 Key 后缀。 */\n    public static final String CLUSTER_MAP_POSTFIX = \"-cluster-map\";",
    ),
]

R["sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-embedded/src/main/java/com/alibaba/csp/sentinel/demo/cluster/app/ClusterDemoApplication.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 嵌入式集群流控 Demo 的 Spring Boot 启动入口。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public static void main(String[] args) {",
        "    /** 启动 Spring Boot，加载集群 Token Server/Client 与 Sentinel 规则。 */\n    public static void main(String[] args) {",
    ),
]

R["sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-embedded/src/main/java/com/alibaba/csp/sentinel/demo/cluster/app/config/AopConfig.java"] = [
    (
        "/**\n * AOP config to enable annotation support for Sentinel.\n *\n * @author Eric Zhao\n */",
        "/**\n * Spring AOP 配置：注册 {@link SentinelResourceAspect}，启用 {@code @SentinelResource} 注解支持。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Bean\n    public SentinelResourceAspect sentinelResourceAspect() {",
        "    /** 声明 Sentinel 资源切面 Bean。 */\n    @Bean\n    public SentinelResourceAspect sentinelResourceAspect() {",
    ),
]


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines())
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if env is None:
            if not path.exists():
                raise RuntimeError(f"guard file missing: {path}")
            blob = path.read_text(encoding="utf-8")
        else:
            rel = path.relative_to(ROOT)
            blob = subprocess.check_output(
                ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
            )
        if not has_chinese(blob):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def isolated_index_commit(message: str, paths: list[str], base_ref: str = "origin/main") -> tuple[str, int]:
    index_file = Path("/tmp/git-index-sentinel-w24b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    tree_before = subprocess.check_output(
        ["git", "-C", str(ROOT), "write-tree"], env=env, text=True
    ).strip()
    tree_count = len(
        subprocess.check_output(
            ["git", "-C", str(ROOT), "ls-tree", "-r", "--name-only", tree_before],
            env=env,
            text=True,
        ).splitlines()
    )
    if tree_count < 50000:
        raise RuntimeError(f"read-tree guard failed: tree_count={tree_count} (expected >=50000)")
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_guard(env)
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def push_main(retries: int = 4) -> None:
    r = subprocess.CompletedProcess([], 1)
    for attempt in range(retries):
        r = subprocess.run(
            ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            return
        if attempt + 1 < retries:
            subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
            time.sleep(4 * (2**attempt))
    raise subprocess.CalledProcessError(r.returncode, r.args, r.stdout, r.stderr)


def apply_replacements(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"Missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    for old, new in R.get(rel, []):
        if old not in text:
            raise ValueError(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not has_chinese(text):
        raise ValueError(f"No Chinese in {rel} after annotation")
    dst.write_text(text, encoding="utf-8")


def update_batch_json() -> None:
    batch_path = QUEUE / "batch.json"
    if not batch_path.exists():
        return
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    if pending_path.exists():
        batch["remaining_pending"] = len(
            [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
        )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def confirm_chinese() -> dict[str, bool]:
    return {rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_LIST}


def verify_origin_main() -> dict[str, bool]:
    result: dict[str, bool] = {}
    for rel in BATCH_LIST:
        path = f"sentinel/1.8.10/analyzed/{rel}"
        blob = subprocess.check_output(
            ["git", "-C", str(ROOT), "show", f"origin/main:{path}"],
            text=True,
        )
        result[rel] = has_chinese(blob)
    return result


def main() -> int:
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    failures: list[str] = []
    for rel in BATCH_LIST:
        try:
            apply_replacements(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        print(json.dumps({"failures": failures}, ensure_ascii=False, indent=2))
        return 1

    analyzed_paths = [f"sentinel/1.8.10/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "sentinel 1.8.10: Chinese-annotate wave 24b [15:30]",
        [*analyzed_paths, script_path],
    )
    push_main()

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "sentinel",
            "--version",
            "1.8.10",
            "--note",
            MARK_NOTE,
            *BATCH_LIST,
        ],
        check=True,
    )
    update_batch_json()
    queue_paths = [
        "sentinel/1.8.10/_reports/class-queue/done.txt",
        "sentinel/1.8.10/_reports/class-queue/pending.txt",
        "sentinel/1.8.10/_reports/class-queue/batch.json",
        "sentinel/1.8.10/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark sentinel 1.8.10 wave24b done",
        queue_paths,
        base_ref="HEAD",
    )
    push_main()

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    origin_chinese = verify_origin_main()
    done_total = len([ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()])
    pending_total = len([ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()])
    chinese_confirmed = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese_confirmed,
                "origin_main_chinese": origin_chinese,
                "all_15_chinese": all(origin_chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(origin_chinese.values()) else 1


if __name__ == "__main__":
    raise SystemExit(main())
