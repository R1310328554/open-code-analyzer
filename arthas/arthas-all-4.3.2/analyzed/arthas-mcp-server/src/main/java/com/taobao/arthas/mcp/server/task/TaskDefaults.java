/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

import java.time.Duration;

/**
 * Task 子系统的默认配置常量。
 * <p>
 * 涵盖 TTL、轮询间隔、容量上限、side-channel 超时及工具输入 schema 等默认值。
 *
 * @author Yeaury
 */
public final class TaskDefaults {

    /** Task 默认存活时间：10 分钟（毫秒）。 */
    public static final long DEFAULT_TTL_MS = 10 * 60 * 1000L;

    /** 客户端轮询 Task 状态的默认间隔：1 秒。 */
    public static final long DEFAULT_POLL_INTERVAL_MS = 1000L;

    /** tasks/list 分页默认每页条数。 */
    public static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * task 记录存储容量上限（内存兜底）。
     * 统计的是 tasks Map 中所有状态的 entry 总数（包含已完成但未被 TTL 清理的），
     */
    public static final int DEFAULT_MAX_TASKS = 10_000;

    /**
     * 并发 task session 上限（资源保护）。
     * 统计的是正在执行中的 Arthas session 数量，保护目标 JVM 不被过多 session 拖垮。
     *
     * <p>取值 5 的依据：
     * <ul>
     *   <li>每个 task 会触发 {@code Instrumentation.retransformClasses}，引发一次 JVM STW 暂停，
     *       并使目标方法的 JIT 编译结果失效（去优化）。N 个并发 task 意味着 N 次串行 STW。</li>
     *   <li>AdviceListenerManager 按 (ClassLoader, 类, 方法签名) 存储 {@code List<AdviceListener>}，
     *       没有 adviceId 维度。同一热点方法被 N 个 task 监听时，每次方法调用要顺序回调 N 次，
     *       开销线性叠加。以 1000 QPS 为例：5 个 listener × 约 50μs/次 ≈ 单核 25% 开销，
     *       是业务延迟开始可感知的经验阈值；10 个时翻倍至 50%。</li>
     *   <li>当前支持 task 模式的流式工具恰好有 5 个（watch / trace / stack / tt / monitor），
     *       上限设为 5 对应"每种工具最多同时运行一个 task"，语义直观。</li>
     *   <li>与 Arthas 社区实践对齐：官方建议生产环境同时活跃的 trace/watch 不超过 5 个
     *       （参见 alibaba/arthas#44）。</li>
     * </ul>
     */
    public static final int DEFAULT_MAX_CONCURRENT_TASK_SESSIONS = 5;

    /** 自动轮询模式（无 Task 元数据）的默认超时：10 分钟。 */
    public static final long DEFAULT_AUTOMATIC_POLLING_TIMEOUT_MS = 600000L;

    /** side-channel 等待客户端响应的默认超时：5 分钟。 */
    public static final int DEFAULT_SIDE_CHANNEL_TIMEOUT_MINUTES = 5;

    /** Task TTL 上限：24 小时。 */
    public static final long MAX_TTL_MS = 24 * 60 * 60 * 1000L;

    /** 允许的最小轮询间隔：100 毫秒。 */
    public static final long MIN_POLL_INTERVAL_MS = 100L;

    /** 允许的最大轮询间隔：1 小时。 */
    public static final long MAX_POLL_INTERVAL_MS = 60 * 60 * 1000L;

    /** 过期 Task 后台清理任务的执行间隔：1 分钟。 */
    public static final long CLEANUP_INTERVAL_MINUTES = 1L;

    /** 消息队列 shutdown 时的清理等待上限：1 秒。 */
    public static final long MESSAGE_QUEUE_CLEANUP_TIMEOUT_MS = 1_000L;

    /** waitForResponse 内部轮询间隔：50 毫秒。 */
    public static final long RESPONSE_POLL_INTERVAL_MS = 50L;

    /** TaskStore 关闭时的等待超时：5 秒。 */
    public static final long TASK_STORE_SHUTDOWN_TIMEOUT_SECONDS = 5L;

    /** 按轮询间隔推算超时时的默认最大轮询次数。 */
    public static final int DEFAULT_MAX_POLL_ATTEMPTS = 60;

    /** 轮询超时上限：1 小时。 */
    public static final long MAX_TIMEOUT_MS = 3_600_000L;

    /** watch 类 Task 单次结果中允许的最大更新条数。 */
    public static final int MAX_WATCH_UPDATES = 100;

    /** 无输入参数工具的默认 JSON Schema（空 object）。 */
    public static final McpSchema.JsonSchema EMPTY_INPUT_SCHEMA =
            new McpSchema.JsonSchema("object", null, null, null);

    /**
     * 按轮询间隔与 {@link #DEFAULT_MAX_POLL_ATTEMPTS} 推算超时，上限为 {@link #MAX_TIMEOUT_MS}。
     */
    public static Duration calculateTimeout(Long pollInterval) {
        long interval = pollInterval != null ? pollInterval : DEFAULT_POLL_INTERVAL_MS;
        long calculatedMs = interval * DEFAULT_MAX_POLL_ATTEMPTS;
        return Duration.ofMillis(Math.min(calculatedMs, MAX_TIMEOUT_MS));
    }

    /**
     * 校验：注册了 Task 感知工具时必须同时配置 {@link TaskStore}。
     */
    public static void validateTaskConfiguration(boolean hasTaskTools, boolean hasTaskStore) {
        if (hasTaskTools && !hasTaskStore) {
            throw new IllegalStateException(
                    "Task-aware tools registered but no TaskStore configured. " +
                    "Add a TaskStore via .taskStore(store) or remove task tools.");
        }
    }

    private TaskDefaults() {
        throw new UnsupportedOperationException("Utility class");
    }
}
