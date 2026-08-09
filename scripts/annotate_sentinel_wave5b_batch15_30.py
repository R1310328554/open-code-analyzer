#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-5b block [15:30]."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = Path("/tmp/sentinel_w5b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}


def _first_class_javadoc(path: Path) -> str:
    text = path.read_text(encoding="utf-8")
    m = re.search(r"/\*\*.*?\*/", text, re.DOTALL)
    if not m:
        raise SystemExit(f"No class javadoc in {path}")
    return m.group(0)


def _inject_large_docs() -> None:
    flow_slot = ANALYZED / "sentinel-core/src/main/java/com/alibaba/csp/sentinel/slots/block/flow/FlowSlot.java"
    warm_up = ANALYZED / "sentinel-core/src/main/java/com/alibaba/csp/sentinel/slots/block/flow/controller/WarmUpController.java"
    R["FlowSlot.java"] = [
        (
            _first_class_javadoc(flow_slot),
            "/**\n * <p>\n * 结合前序 Slot（{@link com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot}、\n * {@link com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot} 与\n * {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot}）采集的运行时统计信息，\n * FlowSlot 依据预设规则判定入站请求是否应被阻断。\n * </p>\n *\n * <p>\n * 若任意规则被触发，{@code SphU.entry(resourceName)} 将抛出 {@code FlowException}。\n * 用户可通过捕获 {@code FlowException} 自定义处理逻辑。\n * </p>\n *\n * <p>\n * 一个资源可配置多条流控规则。FlowSlot 依次遍历这些规则，直到某条被触发或全部遍历完毕。\n * </p>\n *\n * <p>\n * 每条 {@link FlowRule} 主要由 grade、strategy、path 等因素组成，\n * 可组合这些因素实现不同的流控效果。\n * </p>\n *\n * <p>\n * grade 由 {@link FlowRule} 的 {@code grade} 字段定义：0 表示线程隔离，1 表示请求数整形（QPS）。\n * 线程数与请求数均在运行时实时采集，可通过以下命令查看统计：\n * </p>\n *\n * <pre>\n * curl http://localhost:8719/tree\n *\n * idx id    thread pass  blocked   success total aRt   1m-pass   1m-block   1m-all   exception\n * 2   abc647 0      460    46          46   1    27      630       276        897      0\n * </pre>\n *\n * <ul>\n * <li>{@code thread}：当前正在处理该资源的线程数</li>\n * <li>{@code pass}：一秒内通过的请求数</li>\n * <li>{@code blocked}：一秒内被阻断的请求数</li>\n * <li>{@code success}：一秒内由 Sentinel 成功处理的请求数</li>\n * <li>{@code RT}：一秒内请求的平均响应时间</li>\n * <li>{@code total}：一秒内入站请求与被阻断请求的总和</li>\n * <li>{@code 1m-pass}：一分钟内通过的请求数</li>\n * <li>{@code 1m-block}：一分钟内被阻断的请求数</li>\n * <li>{@code 1m-all}：一分钟内入站与被阻断请求的总和</li>\n * <li>{@code exception}：一秒内业务（自定义）异常数</li>\n * </ul>\n *\n * 该阶段通常用于防止资源被长时间占用。若资源处理耗时较长，线程会持续占用；\n * 响应越慢，占用的线程越多。\n *\n * 除计数器外，也可使用线程池或信号量实现并发控制：\n *\n * - 线程池：为资源分配专用线程池；池中无空闲线程时直接拒绝，不影响其他资源。\n *\n * - 信号量：用信号量控制该资源的并发线程数。\n *\n * 线程池的优势是超时可优雅退出，但会带来上下文切换与额外线程开销。\n * 若入站请求已在独立线程中处理（如 Servlet HTTP 请求），使用线程池几乎会使线程数翻倍。\n *\n * <h3>流量整形</h3>\n * <p>\n * 当 QPS 超过阈值时，Sentinel 将按流控规则中的 {@code controlBehavior} 字段对入站请求采取控制动作。\n * </p>\n * <ol>\n * <li>直接拒绝（{@code RuleConstant.CONTROL_BEHAVIOR_DEFAULT}）</li>\n * <p>\n * 默认行为：超限请求立即被拒绝并抛出 FlowException。\n * </p>\n *\n * <li>预热（{@code RuleConstant.CONTROL_BEHAVIOR_WARM_UP}）</li>\n * <p>\n * 若系统长期低负载后突然涌入大量请求，可能无法一次性全部处理。\n * 通过逐步增加入站请求，系统可预热并最终承载全部流量。\n * 预热时长可通过流控规则中的 {@code warmUpPeriodSec} 配置。\n * </p>\n *\n * <li>匀速排队（{@code RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER}）</li>\n * <p>\n * 该策略严格控制请求间隔，以稳定、均匀的速率放行请求。\n * </p>\n * <img src=\"https://raw.githubusercontent.com/wiki/alibaba/Sentinel/image/uniform-speed-queue.png\" style=\"max-width:\n * 60%;\"/>\n * <p>\n * 该策略是<a href=\"https://en.wikipedia.org/wiki/Leaky_bucket\">漏桶算法</a>的实现，\n * 以恒定速率处理请求，常用于突发流量（如消息处理）场景。\n * 当超出系统容量的大量请求同时到达时，系统以固定速率处理，\n * 直至全部完成或超时。\n * </p>\n * </ol>\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        ),
        (
            "    /**\n     * Package-private for test.\n     *\n     * @param checker flow rule checker\n     * @since 1.6.1\n     */",
            "    /**\n     * 包内可见，供测试使用。\n     *\n     * @param checker 流控规则校验器\n     * @since 1.6.1\n     */",
        ),
    ]
    R["WarmUpController.java"] = [
        (
            _first_class_javadoc(warm_up),
            "/**\n * <p>\n * 核心思想源自 Guava，但 Guava 基于速率计算，需将速率换算为 QPS。\n * </p>\n *\n * <p>\n * 脉冲式到达的请求可能拖垮长期空闲的系统，尽管其在稳定期具备更大处理能力。\n * 常见于需要额外初始化时间的场景，如数据库建连、连接远程服务等，因此需要\"预热\"。\n * </p>\n *\n * <p>\n * Sentinel 的预热实现基于 Guava 算法，但 Guava 侧重调整请求间隔（类似漏桶），\n * 而 Sentinel 更关注每秒入站请求数的控制（不计算间隔），更接近令牌桶算法。\n * </p>\n *\n * <p>\n * 桶中剩余令牌用于衡量系统利用率。假设系统每秒可处理 b 个请求，\n * 每秒向桶中补充 b 个令牌直至满桶；处理请求时从桶中取令牌。\n * 剩余令牌越多，系统利用率越低；令牌数超过某阈值时称为\"饱和\"状态。\n * </p>\n *\n * <p>\n * 基于 Guava 理论，可用线性方程 y = m * x + b 描述，\n * 其中 y（即 y(x) 或 qps(q)）为饱和期内的期望 QPS，\n * m 为从冷启动（最低）速率到稳定（最高）速率的变化率，\n * x（或 q）为已占用的令牌数。\n * </p>\n *\n * @author jialiang.linjl\n */",
        ),
    ]


R["PriorityWaitException.java"] = [
    (
        "/**\n * An exception that marks previous prioritized request has been waiting till now, then should pass.\n *\n * @author jialiang.linjl\n * @since 1.5.0\n */",
        "/**\n * 标记先前优先级请求已等待至此刻、应当放行的异常。\n *\n * @author jialiang.linjl\n * @since 1.5.0\n */",
    ),
]

R["TrafficShapingController.java"] = [
    (
        "/**\n * A universal interface for traffic shaping controller.\n *\n * @author jialiang.linjl\n */",
        "/**\n * 流量整形控制器的通用接口。\n *\n * @author jialiang.linjl\n */",
    ),
    (
        "    /**\n     * Check whether given resource entry can pass with provided count.\n     *\n     * @param node resource node\n     * @param acquireCount count to acquire\n     * @param prioritized whether the request is prioritized\n     * @return true if the resource entry can pass; false if it should be blocked\n     */",
        "    /**\n     * 校验给定资源入口在指定数量下是否可通过。\n     *\n     * @param node 资源节点\n     * @param acquireCount 要获取的配额数\n     * @param prioritized 是否为优先级请求\n     * @return 可通过返回 true；应被阻断返回 false\n     */",
    ),
    (
        "    /**\n     * Check whether given resource entry can pass with provided count.\n     *\n     * @param node resource node\n     * @param acquireCount count to acquire\n     * @return true if the resource entry can pass; false if it should be blocked\n     */",
        "    /**\n     * 校验给定资源入口在指定数量下是否可通过。\n     *\n     * @param node 资源节点\n     * @param acquireCount 要获取的配额数\n     * @return 可通过返回 true；应被阻断返回 false\n     */",
    ),
]

R["DefaultController.java"] = [
    (
        "/**\n * Default throttling controller (immediately reject strategy).\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * 默认流控控制器（直接拒绝策略）。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
]

R["ThrottlingController.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @author jialiang.linjl\n * @since 2.0\n */",
        "/**\n * 匀速排队流控控制器，严格限制请求间隔以实现均匀放行。\n * 由 Sentinel 1.x 的 RateLimitController 重构而来。\n *\n * @author Eric Zhao\n * @author jialiang.linjl\n * @since 2.0\n */",
    ),
]

R["WarmUpRateLimiterController.java"] = [
    (
        "/**\n * @author jialiang.linjl\n * @since 1.4.0\n */",
        "/**\n * 预热与匀速排队结合的流控控制器。\n *\n * @author jialiang.linjl\n * @since 1.4.0\n */",
    ),
]

R["AbstractTokenBucket.java"] = [
    (
        "/**\n * @author LearningGp\n */",
        "/**\n * 令牌桶算法的抽象基类，负责按时间间隔补充令牌并支持消费。\n *\n * @author LearningGp\n */",
    ),
    (
        "    /**\n     * Number of tokens left in the bucket\n     */",
        "    /**\n     * 桶中剩余令牌数\n     */",
    ),
    (
        "    /**\n     * Time of next production token\n     */",
        "    /**\n     * 下次生产令牌的时间戳\n     */",
    ),
    (
        "    /**\n     * Number of tokens produced per unit of time\n     */",
        "    /**\n     * 每个时间单位生产的令牌数\n     */",
    ),
    (
        "    /**\n     * Maximum number of tokens stored in the bucket\n     */",
        "    /**\n     * 桶中可存储的最大令牌数\n     */",
    ),
]

R["DefaultTokenBucket.java"] = [
    (
        "/**\n * @author LearningGp\n */",
        "/**\n * 默认令牌桶实现，首次请求到达时才填充初始令牌。\n *\n * @author LearningGp\n */",
    ),
]

R["StrictTokenBucket.java"] = [
    (
        "/**\n * @author LearningGp\n */",
        "/**\n * 严格令牌桶实现，通过锁保证刷新与消费操作的线程安全。\n *\n * @author LearningGp\n */",
    ),
]

R["TokenBucket.java"] = [
    (
        "/**\n * @author LearningGp\n */",
        "/**\n * 令牌桶接口，定义令牌消费与刷新操作。\n *\n * @author LearningGp\n */",
    ),
]

R["ClusterBuilderSlot.java"] = [
    (
        "/**\n * <p>\n * This slot maintains resource running statistics (response time, qps, thread\n * count, exception), and a list of callers as well which is marked by\n * {@link ContextUtil#enter(String origin)}\n * </p>\n * <p>\n * One resource has only one cluster node, while one resource can have multiple\n * default nodes.\n * </p>\n *\n * @author jialiang.linjl\n */",
        "/**\n * <p>\n * 该 Slot 维护资源运行统计（响应时间、QPS、线程数、异常），\n * 以及由 {@link ContextUtil#enter(String origin)} 标记的调用方列表。\n * </p>\n * <p>\n * 一个资源仅对应一个集群节点（ClusterNode），但可对应多个默认节点（DefaultNode）。\n * </p>\n *\n * @author jialiang.linjl\n */",
    ),
    (
        "    /**\n     * <p>\n     * Remember that same resource({@link ResourceWrapper#equals(Object)}) will share\n     * the same {@link ProcessorSlotChain} globally, no matter in which context. So if\n     * code goes into {@link #entry(Context, ResourceWrapper, DefaultNode, int, boolean, Object...)},\n     * the resource name must be same but context name may not.\n     * </p>\n     * <p>\n     * To get total statistics of the same resource in different context, same resource\n     * shares the same {@link ClusterNode} globally. All {@link ClusterNode}s are cached\n     * in this map.\n     * </p>\n     * <p>\n     * The longer the application runs, the more stable this mapping will\n     * become. so we don't concurrent map but a lock. as this lock only happens\n     * at the very beginning while concurrent map will hold the lock all the time.\n     * </p>\n     */",
        "    /**\n     * <p>\n     * 注意：相同资源（{@link ResourceWrapper#equals(Object)}）无论在哪个上下文中，\n     * 全局共享同一 {@link ProcessorSlotChain}。因此进入\n     * {@link #entry(Context, ResourceWrapper, DefaultNode, int, boolean, Object...)} 时，\n     * 资源名必须相同，但上下文名可能不同。\n     * </p>\n     * <p>\n     * 为汇总同一资源在不同上下文中的总统计，相同资源全局共享同一 {@link ClusterNode}。\n     * 所有 {@link ClusterNode} 缓存在此映射中。\n     * </p>\n     * <p>\n     * 应用运行越久，该映射越稳定，故使用锁而非并发 Map——\n     * 锁仅在启动初期出现，而并发 Map 会持续持有锁。\n     * </p>\n     */",
    ),
    (
        "    /**\n     * Get {@link ClusterNode} of the resource of the specific type.\n     *\n     * @param id   resource name.\n     * @param type invoke type.\n     * @return the {@link ClusterNode}\n     */",
        "    /**\n     * 获取指定类型资源的 {@link ClusterNode}。\n     *\n     * @param id   资源名\n     * @param type 调用类型\n     * @return 对应的 {@link ClusterNode}\n     */",
    ),
    (
        "    /**\n     * Get {@link ClusterNode} of the resource name.\n     *\n     * @param id resource name.\n     * @return the {@link ClusterNode}.\n     */",
        "    /**\n     * 按资源名获取 {@link ClusterNode}。\n     *\n     * @param id 资源名\n     * @return 对应的 {@link ClusterNode}\n     */",
    ),
    (
        "    /**\n     * Get {@link ClusterNode}s map, this map holds all {@link ClusterNode}s, it's key is resource name,\n     * value is the related {@link ClusterNode}. <br/>\n     * DO NOT MODIFY the map returned.\n     *\n     * @return all {@link ClusterNode}s\n     */",
        "    /**\n     * 获取 {@link ClusterNode} 映射，键为资源名，值为对应的 {@link ClusterNode}。<br/>\n     * 请勿修改返回的映射。\n     *\n     * @return 全部 {@link ClusterNode}\n     */",
    ),
    (
        "    /**\n     * Reset all {@link ClusterNode}s. Reset is needed when {@link IntervalProperty#INTERVAL} or\n     * {@link SampleCountProperty#SAMPLE_COUNT} is changed.\n     */",
        "    /**\n     * 重置全部 {@link ClusterNode}。\n     * 当 {@link IntervalProperty#INTERVAL} 或 {@link SampleCountProperty#SAMPLE_COUNT} 变更时需调用。\n     */",
    ),
]

R["EagleEyeLogUtil.java"] = [
    (
        "public class EagleEyeLogUtil {",
        "/**\n * 阻断异常日志工具类，将流控/降级等阻断事件写入 sentinel-block.log。\n */\npublic class EagleEyeLogUtil {",
    ),
    (
        "    public static void log(String resource, String exceptionName, String ruleLimitApp, String origin, Long ruleId, int count) {",
        "    /**\n     * 记录一次阻断事件。\n     *\n     * @param resource      资源名\n     * @param exceptionName 异常类名\n     * @param ruleLimitApp  规则限流应用\n     * @param origin        调用来源\n     * @param ruleId        规则 ID\n     * @param count         阻断数量\n     */\n    public static void log(String resource, String exceptionName, String ruleLimitApp, String origin, Long ruleId, int count) {",
    ),
]

R["LogSlot.java"] = [
    (
        "/**\n * A {@link com.alibaba.csp.sentinel.slotchain.ProcessorSlot} that is response for logging block exceptions\n * to provide concrete logs for troubleshooting.\n */",
        "/**\n * 负责记录阻断异常的 {@link com.alibaba.csp.sentinel.slotchain.ProcessorSlot}，\n * 输出具体日志以便排查问题。\n */",
    ),
]

R["NodeSelectorSlot.java"] = [
    (
        "/**\n * </p>\n * This class will try to build the calling traces via\n * <ol>\n * <li>adding a new {@link DefaultNode} if needed as the last child in the context.\n * The context's last node is the current node or the parent node of the context. </li>\n * <li>setting itself to the context current node.</li>\n * </ol>\n * </p>\n *\n * <p>It works as follow:</p>",
        "/**\n * <p>\n * 该类通过以下方式构建调用链路：\n * <ol>\n * <li>在上下文的最后一个节点（当前节点或父节点）下按需添加新的 {@link DefaultNode} 作为末级子节点。</li>\n * <li>将自身设为上下文的当前节点。</li>\n * </ol>\n * </p>\n *\n * <p>工作流程如下：</p>",
    ),
    (
        " * Above code will generate the following invocation structure in memory:",
        " * 上述代码将在内存中生成如下调用结构：",
    ),
    (
        " * Here the {@link EntranceNode} represents \"entrance1\" given by\n * {@code ContextUtil.enter(\"entrance1\", \"appA\")}.",
        " * 此处 {@link EntranceNode} 表示 {@code ContextUtil.enter(\"entrance1\", \"appA\")} 指定的 \"entrance1\"。",
    ),
    (
        " * Both DefaultNode(nodeA) and ClusterNode(nodeA) holds statistics of \"nodeA\", which is given\n * by {@code SphU.entry(\"nodeA\")}",
        " * DefaultNode(nodeA) 与 ClusterNode(nodeA) 均持有 \"nodeA\" 的统计信息，\n * \"nodeA\" 由 {@code SphU.entry(\"nodeA\")} 指定",
    ),
    (
        " * The {@link ClusterNode} is uniquely identified by the ResourceId; the {@link DefaultNode}\n * is identified by both the resource id and {@link Context}. In other words, one resource\n * id will generate multiple {@link DefaultNode} for each distinct context, but only one\n * {@link ClusterNode}.",
        " * {@link ClusterNode} 由 ResourceId 唯一标识；{@link DefaultNode} 由资源 id 与 {@link Context} 共同标识。\n * 换言之，同一资源 id 在不同上下文中会生成多个 {@link DefaultNode}，但仅对应一个 {@link ClusterNode}。",
    ),
    (
        " * the following code shows one resource id in two different context:",
        " * 以下代码展示同一资源 id 在两个不同上下文中的情况：",
    ),
    (
        " * As we can see, two {@link DefaultNode} are created for \"nodeA\" in two context, but only one\n * {@link ClusterNode} is created.",
        " * 可见 \"nodeA\" 在两个上下文中各创建一个 {@link DefaultNode}，但仅创建一个 {@link ClusterNode}。",
    ),
    (
        " * We can also check this structure by calling: <br/>\n * {@code curl http://localhost:8719/tree?type=root}",
        " * 也可通过以下命令查看该结构：<br/>\n * {@code curl http://localhost:8719/tree?type=root}",
    ),
    (
        "    /**\n     * {@link DefaultNode}s of the same resource in different context.\n     */",
        "    /**\n     * 同一资源在不同上下文中的 {@link DefaultNode} 映射。\n     */",
    ),
]


def apply_replacements(rel: str) -> None:
    name = Path(rel).name
    path = ANALYZED / rel
    text = path.read_text(encoding="utf-8")
    for old, new in R.get(name, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not re.search(r"[\u4e00-\u9fff]", text):
        raise SystemExit(f"No Chinese in {rel} after annotation")
    path.write_text(text, encoding="utf-8")


def update_tracking() -> None:
    done_path = QUEUE / "done.txt"
    done = done_path.read_text(encoding="utf-8").rstrip("\n").split("\n")
    done_set = set(done)
    for rel in BATCH_LIST:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
    done_path.write_text("\n".join(done) + "\n", encoding="utf-8")

    batch = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))
    remaining = [f for f in batch["files"] if f not in BATCH_LIST]
    batch["files"] = remaining
    batch["done"] = batch.get("done", 120) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 815) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    _inject_large_docs()
    for rel in BATCH_LIST:
        apply_replacements(rel)
    update_tracking()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
