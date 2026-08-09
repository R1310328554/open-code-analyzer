#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-3b node/property [15:30]."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))["files"][15:30]

R: dict[str, list[tuple[str, str]]] = {}

R["Node.java"] = [
    (
        "/**\n * Holds real-time statistics for resources.\n *\n * @author qinan.qn\n * @author leyou\n * @author Eric Zhao\n */",
        "/**\n * 保存资源的实时统计数据。\n *\n * @author qinan.qn\n * @author leyou\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Get incoming request per minute ({@code pass + block}).\n     *\n     * @return total request count per minute\n     */",
        "    /**\n     * 获取每分钟入站请求数（{@code pass + block}）。\n     *\n     * @return 每分钟总请求数\n     */",
    ),
    (
        "    /**\n     * Get pass count per minute.\n     *\n     * @return total passed request count per minute\n     * @since 1.5.0\n     */",
        "    /**\n     * 获取每分钟通过请求数。\n     *\n     * @return 每分钟通过请求总数\n     * @since 1.5.0\n     */",
    ),
    (
        "    /**\n     * Get {@link Entry#exit()} count per minute.\n     *\n     * @return total completed request count per minute\n     */",
        "    /**\n     * 获取每分钟 {@link Entry#exit()} 次数。\n     *\n     * @return 每分钟完成请求总数\n     */",
    ),
    (
        "    /**\n     * Get blocked request count per minute (totalBlockRequest).\n     *\n     * @return total blocked request count per minute\n     */",
        "    /**\n     * 获取每分钟被阻断的请求数（totalBlockRequest）。\n     *\n     * @return 每分钟被阻断请求总数\n     */",
    ),
    (
        "    /**\n     * Get exception count per minute.\n     *\n     * @return total business exception count per minute\n     */",
        "    /**\n     * 获取每分钟业务异常数。\n     *\n     * @return 每分钟业务异常总数\n     */",
    ),
    (
        "    /**\n     * Get pass request per second.\n     *\n     * @return QPS of passed requests\n     */",
        "    /**\n     * 获取每秒通过请求 QPS。\n     *\n     * @return 通过请求的 QPS\n     */",
    ),
    (
        "    /**\n     * Get block request per second.\n     *\n     * @return QPS of blocked requests\n     */",
        "    /**\n     * 获取每秒阻断请求 QPS。\n     *\n     * @return 阻断请求的 QPS\n     */",
    ),
    (
        "    /**\n     * Get {@link #passQps()} + {@link #blockQps()} request per second.\n     *\n     * @return QPS of passed and blocked requests\n     */",
        "    /**\n     * 获取 {@link #passQps()} + {@link #blockQps()} 的每秒请求 QPS。\n     *\n     * @return 通过与阻断请求的总 QPS\n     */",
    ),
    (
        "    /**\n     * Get {@link Entry#exit()} request per second.\n     *\n     * @return QPS of completed requests\n     */",
        "    /**\n     * 获取每秒 {@link Entry#exit()} 请求 QPS。\n     *\n     * @return 完成请求的 QPS\n     */",
    ),
    (
        "    /**\n     * Get estimated max success QPS till now.\n     *\n     * @return max completed QPS\n     */",
        "    /**\n     * 获取截至目前估计的最大成功 QPS。\n     *\n     * @return 最大完成 QPS\n     */",
    ),
    (
        "    /**\n     * Get exception count per second.\n     *\n     * @return QPS of exception occurs\n     */",
        "    /**\n     * 获取每秒异常 QPS。\n     *\n     * @return 异常发生的 QPS\n     */",
    ),
    (
        "    /**\n     * Get average rt per second.\n     *\n     * @return average response time per second\n     */",
        "    /**\n     * 获取每秒平均响应时间（RT）。\n     *\n     * @return 每秒平均响应时间\n     */",
    ),
    (
        "    /**\n     * Get minimal response time.\n     *\n     * @return recorded minimal response time\n     */",
        "    /**\n     * 获取最小响应时间。\n     *\n     * @return 已记录的最小响应时间\n     */",
    ),
    (
        "    /**\n     * Get current active thread count.\n     *\n     * @return current active thread count\n     */",
        "    /**\n     * 获取当前活跃线程数。\n     *\n     * @return 当前活跃线程数\n     */",
    ),
    (
        "    /**\n     * Get last second block QPS.\n     */",
        "    /**\n     * 获取上一秒的阻断 QPS。\n     */",
    ),
    (
        "    /**\n     * Last window QPS.\n     */",
        "    /**\n     * 上一时间窗口的通过 QPS。\n     */",
    ),
    (
        "    /**\n     * Fetch all valid metric nodes of resources.\n     *\n     * @return valid metric nodes of resources\n     */",
        "    /**\n     * 获取资源的所有有效指标节点。\n     *\n     * @return 资源的有效指标节点\n     */",
    ),
    (
        "    /**\n     * Fetch all raw metric items that satisfies the time predicate.\n     *\n     * @param timePredicate time predicate\n     * @return raw metric items that satisfies the time predicate\n     * @since 1.7.0\n     */",
        "    /**\n     * 获取满足时间谓词的所有原始指标项。\n     *\n     * @param timePredicate 时间谓词\n     * @return 满足时间谓词的原始指标项\n     * @since 1.7.0\n     */",
    ),
    (
        "    /**\n     * Add pass count.\n     *\n     * @param count count to add pass\n     */",
        "    /**\n     * 增加通过计数。\n     *\n     * @param count 要增加的通过数\n     */",
    ),
    (
        "    /**\n     * Add rt and success count.\n     *\n     * @param rt      response time\n     * @param success success count to add\n     */",
        "    /**\n     * 增加 RT 与成功计数。\n     *\n     * @param rt      响应时间\n     * @param success 要增加的成功数\n     */",
    ),
    (
        "    /**\n     * Increase the block count.\n     *\n     * @param count count to add\n     */",
        "    /**\n     * 增加阻断计数。\n     *\n     * @param count 要增加的阻断数\n     */",
    ),
    (
        "    /**\n     * Add the biz exception count.\n     *\n     * @param count count to add\n     */",
        "    /**\n     * 增加业务异常计数。\n     *\n     * @param count 要增加的异常数\n     */",
    ),
    (
        "    /**\n     * Increase current thread count.\n     */",
        "    /**\n     * 增加当前线程计数。\n     */",
    ),
    (
        "    /**\n     * Decrease current thread count.\n     */",
        "    /**\n     * 减少当前线程计数。\n     */",
    ),
    (
        "    /**\n     * Reset the internal counter. Reset is needed when {@link IntervalProperty#INTERVAL} or\n     * {@link SampleCountProperty#SAMPLE_COUNT} is changed.\n     */",
        "    /**\n     * 重置内部计数器。当 {@link IntervalProperty#INTERVAL} 或\n     * {@link SampleCountProperty#SAMPLE_COUNT} 变更时需要重置。\n     */",
    ),
]

R["NodeBuilder.java"] = [
    (
        "/**\n * Builds new {@link DefaultNode} and {@link ClusterNode}.\n *\n * @author qinan.qn\n */",
        "/**\n * 构建新的 {@link DefaultNode} 与 {@link ClusterNode}。\n *\n * @author qinan.qn\n */",
    ),
    (
        "    /**\n     * Create a new {@link DefaultNode} as tree node.\n     *\n     * @param id resource\n     * @param clusterNode the cluster node of the provided resource\n     * @return new created tree node\n     */",
        "    /**\n     * 创建新的 {@link DefaultNode} 作为调用树节点。\n     *\n     * @param id 资源\n     * @param clusterNode 该资源对应的集群节点\n     * @return 新创建的树节点\n     */",
    ),
    (
        "    /**\n     * Create a new {@link ClusterNode} as universal statistic node for a single resource.\n     *\n     * @return new created cluster node\n     */",
        "    /**\n     * 为单个资源创建新的 {@link ClusterNode} 作为全局统计节点。\n     *\n     * @return 新创建的集群节点\n     */",
    ),
]

R["OccupySupport.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * 支持借用未来时间窗口令牌以实现优先级等待的接口。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
    (
        "    /**\n     * Try to occupy latter time windows' tokens. If occupy success, a value less than\n     * {@code occupyTimeout} in {@link OccupyTimeoutProperty} will be return.\n     *\n     * <p>\n     * Each time we occupy tokens of the future window, current thread should sleep for the\n     * corresponding time for smoothing QPS. We can't occupy tokens of the future with unlimited,\n     * the sleep time limit is {@code occupyTimeout} in {@link OccupyTimeoutProperty}.\n     * </p>\n     *\n     * @param currentTime  current time millis.\n     * @param acquireCount tokens count to acquire.\n     * @param threshold    qps threshold.\n     * @return time should sleep. Time >= {@code occupyTimeout} in {@link OccupyTimeoutProperty} means\n     * occupy fail, in this case, the request should be rejected immediately.\n     */",
        "    /**\n     * 尝试占用后续时间窗口的令牌。占用成功时返回小于 {@link OccupyTimeoutProperty} 中\n     * {@code occupyTimeout} 的值。\n     *\n     * <p>\n     * 每次占用未来窗口令牌时，当前线程应睡眠相应时长以平滑 QPS。\n     * 占用时长受 {@link OccupyTimeoutProperty} 中 {@code occupyTimeout} 限制。\n     * </p>\n     *\n     * @param currentTime  当前时间（毫秒）\n     * @param acquireCount 要获取的令牌数\n     * @param threshold    QPS 阈值\n     * @return 应睡眠的毫秒数。若 >= {@link OccupyTimeoutProperty} 中的 {@code occupyTimeout} 表示占用失败，\n     * 此时应直接拒绝请求\n     */",
    ),
    (
        "    /**\n     * Get current waiting amount. Useful for debug.\n     *\n     * @return current waiting amount\n     */",
        "    /**\n     * 获取当前等待中的令牌量，便于调试。\n     *\n     * @return 当前等待量\n     */",
    ),
    (
        "    /**\n     * Add request that occupied.\n     *\n     * @param futureTime   future timestamp that the acquireCount should be added on.\n     * @param acquireCount tokens count.\n     */",
        "    /**\n     * 添加已占用令牌的等待请求。\n     *\n     * @param futureTime   应累加 acquireCount 的未来时间戳\n     * @param acquireCount 令牌数\n     */",
    ),
    (
        "    /**\n     * Add occupied pass request, which represents pass requests that borrow the latter windows' token.\n     *\n     * @param acquireCount tokens count.\n     */",
        "    /**\n     * 增加占用通过请求数，表示借用后续窗口令牌的通过请求。\n     *\n     * @param acquireCount 令牌数\n     */",
    ),
    (
        "    /**\n     * Get current occupied pass QPS.\n     *\n     * @return current occupied pass QPS\n     */",
        "    /**\n     * 获取当前占用通过 QPS。\n     *\n     * @return 当前占用通过 QPS\n     */",
    ),
]

R["OccupyTimeoutProperty.java"] = [
    (
        "/**\n * @author jialiang.linjl\n * @author Carpenter Lee\n * @since 1.5.0\n */",
        "/**\n * 优先级占用未来统计窗口令牌的最大等待超时配置。\n *\n * @author jialiang.linjl\n * @author Carpenter Lee\n * @since 1.5.0\n */",
    ),
    (
        "    /**\n     * <p>\n     * Max occupy timeout in milliseconds. Requests with priority can occupy tokens of the future statistic\n     * window, and {@code occupyTimeout} limit the max time length that can be occupied.\n     * </p>\n     * <p>\n     * Note that the timeout value should never be greeter than {@link IntervalProperty#INTERVAL}.\n     * </p>\n     * DO NOT MODIFY this value directly, use {@link #updateTimeout(int)},\n     * otherwise the modification will not take effect.\n     */",
        "    /**\n     * <p>\n     * 最大占用超时（毫秒）。带优先级的请求可占用未来统计窗口的令牌，\n     * {@code occupyTimeout} 限制可占用的最大时长。\n     * </p>\n     * <p>\n     * 注意：超时值不应大于 {@link IntervalProperty#INTERVAL}。\n     * </p>\n     * 请勿直接修改此值，应使用 {@link #updateTimeout(int)}，\n     * 否则修改不会生效。\n     */",
    ),
    (
        "    /**\n     * Update the timeout value.</br>\n     * Note that the time out should never greeter than {@link IntervalProperty#INTERVAL},\n     * or it will be ignored.\n     *\n     * @param newInterval new value.\n     */",
        "    /**\n     * 更新超时值。</br>\n     * 注意：超时值不应大于 {@link IntervalProperty#INTERVAL}，\n     * 否则将被忽略。\n     *\n     * @param newInterval 新超时值\n     */",
    ),
]

R["SampleCountProperty.java"] = [
    (
        "/**\n * Holds statistic buckets count per second.\n *\n * @author jialiang.linjl\n * @author CarpenterLee\n */",
        "/**\n * 保存每秒统计桶（采样窗口）数量配置。\n *\n * @author jialiang.linjl\n * @author CarpenterLee\n */",
    ),
    (
        "    /**\n     * <p>\n     * Statistic buckets count per second. This variable determines sensitivity of the QPS calculation.\n     * DO NOT MODIFY this value directly, use {@link #updateSampleCount(int)}, otherwise the modification will not\n     * take effect.\n     * </p>\n     * Node that this value must be divisor of 1000.\n     */",
        "    /**\n     * <p>\n     * 每秒统计桶数量，决定 QPS 计算的灵敏度。\n     * 请勿直接修改此值，应使用 {@link #updateSampleCount(int)}，否则修改不会生效。\n     * </p>\n     * 注意：该值必须是 1000 的约数。\n     */",
    ),
    (
        "    /**\n     * Update the {@link #SAMPLE_COUNT}. All {@link ClusterNode}s will be reset if newSampleCount\n     * is different from {@link #SAMPLE_COUNT}.\n     *\n     * @param newSampleCount New sample count to set. This value must be divisor of 1000.\n     */",
        "    /**\n     * 更新 {@link #SAMPLE_COUNT}。若 newSampleCount 与当前值不同，\n     * 将重置所有 {@link ClusterNode}。\n     *\n     * @param newSampleCount 新的采样数，必须是 1000 的约数\n     */",
    ),
]

R["StatisticNode.java"] = [
    (
        "/**\n * <p>The statistic node keep three kinds of real-time statistics metrics:</p>\n * <ol>\n * <li>metrics in second level ({@code rollingCounterInSecond})</li>\n * <li>metrics in minute level ({@code rollingCounterInMinute})</li>\n * <li>thread count</li>\n * </ol>\n *\n * <p>\n * Sentinel use sliding window to record and count the resource statistics in real-time.\n * The sliding window infrastructure behind the {@link ArrayMetric} is {@code LeapArray}.\n * </p>\n *\n * <p>\n * case 1: When the first request comes in, Sentinel will create a new window bucket of\n * a specified time-span to store running statics, such as total response time(rt),\n * incoming request(QPS), block request(bq), etc. And the time-span is defined by sample count.\n * </p>\n * <pre>\n * \t0      100ms\n *  +-------+--→ Sliding Windows\n * \t    ^\n * \t    |\n * \t  request\n * </pre>\n * <p>\n * Sentinel use the statics of the valid buckets to decide whether this request can be passed.\n * For example, if a rule defines that only 100 requests can be passed,\n * it will sum all qps in valid buckets, and compare it to the threshold defined in rule.\n * </p>\n *\n * <p>case 2: continuous requests</p>\n * <pre>\n *  0    100ms    200ms    300ms\n *  +-------+-------+-------+-----→ Sliding Windows\n *                      ^\n *                      |\n *                   request\n * </pre>\n *\n * <p>case 3: requests keeps coming, and previous buckets become invalid</p>\n * <pre>\n *  0    100ms    200ms\t  800ms\t   900ms  1000ms    1300ms\n *  +-------+-------+ ...... +-------+-------+ ...... +-------+-----→ Sliding Windows\n *                                                      ^\n *                                                      |\n *                                                    request\n * </pre>\n *\n * <p>The sliding window should become:</p>\n * <pre>\n * 300ms     800ms  900ms  1000ms  1300ms\n *  + ...... +-------+ ...... +-------+-----→ Sliding Windows\n *                                                      ^\n *                                                      |\n *                                                    request\n * </pre>\n *\n * @author qinan.qn\n * @author jialiang.linjl\n */",
        "/**\n * <p>统计节点维护三类实时统计指标：</p>\n * <ol>\n * <li>秒级指标（{@code rollingCounterInSecond}）</li>\n * <li>分钟级指标（{@code rollingCounterInMinute}）</li>\n * <li>线程数</li>\n * </ol>\n *\n * <p>\n * Sentinel 使用滑动窗口实时记录并统计资源指标。\n * {@link ArrayMetric} 背后的滑动窗口基础设施为 {@code LeapArray}。\n * </p>\n *\n * <p>\n * 场景 1：首个请求到达时，Sentinel 会创建新的时间窗口桶，\n * 用于存储运行中的统计量（如总 RT、入站 QPS、阻断 QPS 等），\n * 窗口跨度由采样数决定。\n * </p>\n * <pre>\n * \t0      100ms\n *  +-------+--→ 滑动窗口\n * \t    ^\n * \t    |\n * \t  request\n * </pre>\n * <p>\n * Sentinel 汇总有效桶的统计量来判断请求是否可通过。\n * 例如规则限定最多 100 次通过，则累加有效桶中的 QPS 并与阈值比较。\n * </p>\n *\n * <p>场景 2：连续请求</p>\n * <pre>\n *  0    100ms    200ms    300ms\n *  +-------+-------+-------+-----→ 滑动窗口\n *                      ^\n *                      |\n *                   request\n * </pre>\n *\n * <p>场景 3：请求持续到达，旧桶失效</p>\n * <pre>\n *  0    100ms    200ms\t  800ms\t   900ms  1000ms    1300ms\n *  +-------+-------+ ...... +-------+-------+ ...... +-------+-----→ 滑动窗口\n *                                                      ^\n *                                                      |\n *                                                    request\n * </pre>\n *\n * <p>滑动窗口应变为：</p>\n * <pre>\n * 300ms     800ms  900ms  1000ms  1300ms\n *  + ...... +-------+ ...... +-------+-----→ 滑动窗口\n *                                                      ^\n *                                                      |\n *                                                    request\n * </pre>\n *\n * @author qinan.qn\n * @author jialiang.linjl\n */",
    ),
    (
        "    /**\n     * Holds statistics of the recent {@code INTERVAL} milliseconds. The {@code INTERVAL} is divided into time spans\n     * by given {@code sampleCount}.\n     */",
        "    /**\n     * 保存最近 {@code INTERVAL} 毫秒内的统计量。\n     * {@code INTERVAL} 按 {@code sampleCount} 划分为多个时间片。\n     */",
    ),
    (
        "    /**\n     * Holds statistics of the recent 60 seconds. The windowLengthInMs is deliberately set to 1000 milliseconds,\n     * meaning each bucket per second, in this way we can get accurate statistics of each second.\n     */",
        "    /**\n     * 保存最近 60 秒的统计量。窗口长度刻意设为 1000 毫秒，\n     * 即每秒一个桶，以便精确统计每一秒的数据。\n     */",
    ),
    (
        "    /**\n     * The counter for thread count.\n     */",
        "    /**\n     * 线程数计数器。\n     */",
    ),
    (
        "    /**\n     * The last timestamp when metrics were fetched.\n     */",
        "    /**\n     * 上次拉取指标时的时间戳。\n     */",
    ),
    (
        "        // The fetch operation is thread-safe under a single-thread scheduler pool.",
        "        // 在单线程调度池下，拉取操作是线程安全的。",
    ),
    (
        "        // Iterate metrics of all resources, filter valid metrics (not-empty and up-to-date).",
        "        // 遍历所有资源的指标，过滤有效（非空且最新）的指标。",
    ),
    (
        "         * Note: here {@code currentPass} may be less than it really is NOW, because time difference\n         * since call rollingCounterInSecond.pass(). So in high concurrency, the following code may\n         * lead more tokens be borrowed.",
        "         * 注意：此处 {@code currentPass} 可能小于当前真实值，\n         * 因为从调用 rollingCounterInSecond.pass() 起已有时间差。\n         * 高并发下以下代码可能导致更多令牌被借用。",
    ),
]

R["MetricNode.java"] = [
    (
        "/**\n * Metrics data for a specific resource at given {@code timestamp}.\n *\n * @author jialiang.linjl\n * @author Carpenter Lee\n */",
        "/**\n * 给定 {@code timestamp} 下某资源的指标数据。\n *\n * @author jialiang.linjl\n * @author Carpenter Lee\n */",
    ),
    (
        "    /**\n     * Resource classification (e.g. SQL or RPC)\n     * @since 1.7.0\n     */",
        "    /**\n     * 资源分类（例如 SQL 或 RPC）\n     * @since 1.7.0\n     */",
    ),
    (
        "    /**\n     * To formatting string. All \"|\" in {@link #resource} will be replaced with\n     * \"_\", format is: <br/>\n     * <code>\n     * timestamp|resource|passQps|blockQps|successQps|exceptionQps|rt|occupiedPassQps\n     * </code>\n     *\n     * @return string format of this.\n     */",
        "    /**\n     * 格式化为紧凑字符串。{@link #resource} 中的 \"|\" 将替换为 \"_\"，格式为：<br/>\n     * <code>\n     * timestamp|resource|passQps|blockQps|successQps|exceptionQps|rt|occupiedPassQps\n     * </code>\n     *\n     * @return 紧凑字符串表示\n     */",
    ),
    (
        "    /**\n     * Parse {@link MetricNode} from thin string, see {@link #toThinString()}\n     *\n     * @param line\n     * @return\n     */",
        "    /**\n     * 从紧凑字符串解析 {@link MetricNode}，见 {@link #toThinString()}。\n     *\n     * @param line 紧凑格式行\n     * @return 解析得到的 MetricNode\n     */",
    ),
    (
        "    /**\n     * To formatting string. All \"|\" in {@link MetricNode#resource} will be\n     * replaced with \"_\", format is: <br/>\n     * <code>\n     * timestamp|yyyy-MM-dd HH:mm:ss|resource|passQps|blockQps|successQps|exceptionQps|rt|occupiedPassQps\\n\n     * </code>\n     *\n     * @return string format of this.\n     */",
        "    /**\n     * 格式化为完整字符串。{@link MetricNode#resource} 中的 \"|\" 将替换为 \"_\"，格式为：<br/>\n     * <code>\n     * timestamp|yyyy-MM-dd HH:mm:ss|resource|passQps|blockQps|successQps|exceptionQps|rt|occupiedPassQps\\n\n     * </code>\n     *\n     * @return 完整字符串表示\n     */",
    ),
    (
        "    /**\n     * Parse {@link MetricNode} from fat string, see {@link #toFatString()}\n     *\n     * @param line\n     * @return the {@link MetricNode} parsed.\n     */",
        "    /**\n     * 从完整字符串解析 {@link MetricNode}，见 {@link #toFatString()}。\n     *\n     * @param line 完整格式行\n     * @return 解析得到的 MetricNode\n     */",
    ),
]

R["MetricSearcher.java"] = [
    (
        "    /**\n     * Find metric between [beginTimeMs, endTimeMs], both side inclusive.\n     * When identity is null, all metric between the time intervalMs will be read, otherwise, only the specific\n     * identity will be read.\n     */",
        "    /**\n     * 在 [beginTimeMs, endTimeMs] 区间内检索指标（两端均含）。\n     * identity 为 null 时读取区间内全部指标，否则仅读取指定资源。\n     */",
    ),
    (
        "    /**\n     * The position we cached is useful only when {@code beginTimeMs} is >= {@code lastPosition.second}\n     * and the index file exists and the second we cached is same as in the index file.\n     */",
        "    /**\n     * 仅当 {@code beginTimeMs} >= {@code lastPosition.second}、\n     * 索引文件存在且缓存的秒数与索引文件中一致时，缓存位置才有效。\n     */",
    ),
    (
        "        // index file dose not exits",
        "        // 索引文件不存在",
    ),
    (
        "            // timestamp(second) in the specific position == that we cached",
        "            // 指定位置的秒级时间戳与缓存一致",
    ),
]

R["MetricTimerListener.java"] = [
    (
        "/**\n * @author jialiang.linjl\n */",
        "/**\n * 定时聚合各资源 {@link MetricNode} 并写入指标日志文件的监听器。\n *\n * @author jialiang.linjl\n */",
    ),
]

R["MetricsReader.java"] = [
    (
        "/**\n * Reads metrics data from log file.\n */",
        "/**\n * 从指标日志文件读取 {@link MetricNode} 数据。\n */",
    ),
    (
        "    /**\n     * Avoid OOM in any cases.\n     */",
        "    /**\n     * 避免任何情况下 OOM。\n     */",
    ),
    (
        "    /**\n     * @return if should continue read, return true, else false.\n     */",
        "    /**\n     * @return 若应继续读取返回 true，否则 false\n     */",
    ),
    (
        "                // currentSecond should >= beginSecond, otherwise a wrong metric file must occur",
        "                // currentSecond 应 >= beginSecond，否则说明指标文件异常",
    ),
    (
        "                    // read all",
        "                    // 读取全部",
    ),
    (
        "    /**\n     * When identity is null, all metric between the time intervalMs will be read, otherwise, only the specific\n     * identity will be read.\n     */",
        "    /**\n     * identity 为 null 时读取时间区间内全部指标，否则仅读取指定资源。\n     */",
    ),
]

R["DynamicSentinelProperty.java"] = [
    (
        "import java.util.concurrent.CopyOnWriteArraySet;\n\npublic class DynamicSentinelProperty",
        "import java.util.concurrent.CopyOnWriteArraySet;\n\n/**\n * 可动态更新并通知 {@link PropertyListener} 的 {@link SentinelProperty} 实现。\n * <p>使用 {@link CopyOnWriteArraySet} 保存监听器，值变更时回调 configUpdate。</p>\n *\n * @param <T> 配置值类型\n */\npublic class DynamicSentinelProperty",
    ),
    (
        "    public void close() {",
        "    /**\n     * 清空所有监听器。\n     */\n    public void close() {",
    ),
]

R["NoOpSentinelProperty.java"] = [
    (
        "/**\n * A {@link SentinelProperty} that will never inform the {@link PropertyListener} on it.\n *\n * @author leyou\n */",
        "/**\n * 空操作的 {@link SentinelProperty}，永远不会通知其上的 {@link PropertyListener}。\n *\n * @author leyou\n */",
    ),
]

R["PropertyListener.java"] = [
    (
        "/**\n * This class holds callback method when {@link SentinelProperty#updateValue(Object)} need inform the listener\n *\n * @author jialiang.linjl\n */",
        "/**\n * {@link SentinelProperty#updateValue(Object)} 需要通知监听器时的回调接口。\n *\n * @author jialiang.linjl\n */",
    ),
    (
        "    /**\n     * Callback method when {@link SentinelProperty#updateValue(Object)} need inform the listener.\n     *\n     * @param value updated value.\n     */",
        "    /**\n     * 配置更新时的回调。\n     *\n     * @param value 更新后的值\n     */",
    ),
    (
        "    /**\n     * The first time of the {@code value}'s load.\n     *\n     * @param value the value loaded.\n     */",
        "    /**\n     * 配置值首次加载时的回调。\n     *\n     * @param value 加载的值\n     */",
    ),
]

R["SentinelProperty.java"] = [
    (
        "/**\n * <p>\n * This class holds current value of the config, and is responsible for informing all {@link PropertyListener}s\n * added on this when the config is updated.\n * </p>\n * <p>\n * Note that not every {@link #updateValue(Object newValue)} invocation should inform the listeners, only when\n * {@code newValue} is not Equals to the old value, informing is needed.\n * </p>\n *\n * @param <T> the target type.\n * @author Carpenter Lee\n */",
        "/**\n * <p>\n * 保存配置的当前值，并在配置更新时通知所有已注册的 {@link PropertyListener}。\n * </p>\n * <p>\n * 注意：并非每次 {@link #updateValue(Object newValue)} 都需通知监听器，\n * 仅当 {@code newValue} 与旧值不相等时才通知。\n * </p>\n *\n * @param <T> 配置值类型\n * @author Carpenter Lee\n */",
    ),
    (
        "    /**\n     * <p>\n     * Add a {@link PropertyListener} to this {@link SentinelProperty}. After the listener is added,\n     * {@link #updateValue(Object)} will inform the listener if needed.\n     * </p>\n     * <p>\n     * This method can invoke multi times to add more than one listeners.\n     * </p>\n     *\n     * @param listener listener to add.\n     */",
        "    /**\n     * <p>\n     * 向本 {@link SentinelProperty} 添加 {@link PropertyListener}。\n     * 添加后，{@link #updateValue(Object)} 在需要时会通知该监听器。\n     * </p>\n     * <p>\n     * 可多次调用以添加多个监听器。\n     * </p>\n     *\n     * @param listener 要添加的监听器\n     */",
    ),
    (
        "    /**\n     * Remove the {@link PropertyListener} on this. After removing, {@link #updateValue(Object)}\n     * will not inform the listener.\n     *\n     * @param listener the listener to remove.\n     */",
        "    /**\n     * 移除本属性上的 {@link PropertyListener}。移除后 {@link #updateValue(Object)}\n     * 将不再通知该监听器。\n     *\n     * @param listener 要移除的监听器\n     */",
    ),
    (
        "    /**\n     * Update the {@code newValue} as the current value of this property and inform all {@link PropertyListener}s\n     * added on this only when new {@code newValue} is not Equals to the old value.\n     *\n     * @param newValue the new value.\n     * @return true if the value in property has been updated, otherwise false\n     */",
        "    /**\n     * 将 {@code newValue} 更新为当前值；仅当新值与旧值不相等时通知所有 {@link PropertyListener}。\n     *\n     * @param newValue 新值\n     * @return 若属性值已更新返回 true，否则 false\n     */",
    ),
]

R["SimplePropertyListener.java"] = [
    (
        "package com.alibaba.csp.sentinel.property;\n\npublic abstract class SimplePropertyListener",
        "package com.alibaba.csp.sentinel.property;\n\n/**\n * {@link PropertyListener} 的简化抽象实现。\n * <p>configLoad 默认委托给 configUpdate，便于只关心更新逻辑。</p>\n *\n * @param <T> 配置值类型\n */\npublic abstract class SimplePropertyListener",
    ),
]


def apply(text: str, reps: list[tuple[str, str]]) -> str:
    for old, new in reps:
        if old in text:
            text = text.replace(old, new)
    return text


def mark_queue_done(files: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    pending = [ln.strip() for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    pending_set = set(pending)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
        pending_set.discard(rel)
    done_path.write_text(("\n".join(done) + ("\n" if done else "")), encoding="utf-8")
    pending = [ln for ln in pending if ln in pending_set]
    pending_path.write_text(("\n".join(pending) + ("\n" if pending else "")), encoding="utf-8")
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["done"] = len(done)
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        dst = ANALYZED / rel
        name = Path(rel).name
        text = dst.read_text(encoding="utf-8")
        text = apply(text, R.get(name, []))
        cn = len(re.findall(r"[\u4e00-\u9fff]", text))
        if cn < 10 or "Licensed under the Apache License" not in text:
            failures.append(f"VALIDATION cn={cn}: {rel}")
            print(f"FAIL cn={cn} {rel}")
            continue
        dst.write_text(text, encoding="utf-8")
        ok += 1
        print(f"OK cn={cn} {rel}")
    if not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {len(BATCH_FILES)} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
