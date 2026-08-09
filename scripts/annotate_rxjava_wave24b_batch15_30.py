#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-24b internal schedulers [15:30]."""
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
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
SCRIPTS = ROOT / "scripts"
WAVE24B_FILE = Path("/tmp/rxjava_w24b.txt")
SCRIPT_NAME = "annotate_rxjava_wave24b_batch15_30.py"
MARK_NOTE = "wave24b [15:30]"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE24B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ComputationScheduler.java": [
        (
            "/**\n * Holds a fixed pool of worker threads and assigns them\n * to requested Scheduler.Workers in a round-robin fashion.\n */",
            "/**\n"
            " * 固定大小的工作线程池 Scheduler：以轮询方式为\n"
            " * 请求的 {@link Scheduler.Worker} 分配底层 PoolWorker。\n"
            " * 支持 {@link SchedulerMultiWorkerSupport} 批量创建 Worker。\n"
            " */",
        ),
        (
            "    /** This will indicate no pool is active. */",
            "    /** 表示当前无活动线程池（已 shutdown）。 */",
        ),
        (
            "    /** Manages a fixed number of workers. */",
            "    /** 管理固定数量的 PoolWorker 事件循环。 */",
        ),
        (
            "    /**\n     * Key to setting the maximum number of computation scheduler threads.\n     * Zero or less is interpreted as use available. Capped by available.\n     */",
            "    /**\n"
            "     * 系统属性键：设置 computation 调度器最大线程数。\n"
            "     * 0 或更小表示使用可用 CPU 数，且不超过 availableProcessors。\n"
            "     */",
        ),
        (
            "    /** The maximum number of computation scheduler threads. */",
            "    /** computation 调度器线程池上限。 */",
        ),
        (
            "    /** The name of the system property for setting the thread priority for this Scheduler. */",
            "    /** 设置本 Scheduler 线程优先级的系统属性键。 */",
        ),
        (
            "    static int cap(int cpuCount, int paramThreads) {",
            "    /** 将配置线程数限制在 (0, cpuCount] 范围内。 */\n"
            "    static int cap(int cpuCount, int paramThreads) {",
        ),
        (
            "        public PoolWorker getEventLoop() {",
            "        /** 轮询返回下一个 PoolWorker；cores==0 时返回 SHUTDOWN_WORKER。 */\n"
            "        public PoolWorker getEventLoop() {",
        ),
        (
            "        public void shutdown() {",
            "        /** 依次 dispose 所有 eventLoop Worker。 */\n"
            "        public void shutdown() {",
        ),
        (
            "    /**\n     * Create a scheduler with pool size equal to the available processor\n     * count and using least-recent worker selection policy.\n     */",
            "    /**\n"
            "     * 使用默认 THREAD_FACTORY 创建 Scheduler，\n"
            "     * 池大小等于可用处理器数。\n"
            "     */",
        ),
        (
            "    /**\n     * Create a scheduler with pool size equal to the available processor\n     * count and using least-recent worker selection policy.\n     *\n     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any\n     *                      system properties for configuring new thread creation. Cannot be null.\n     */",
            "    /**\n"
            "     * 使用指定 ThreadFactory 创建 Scheduler，池大小等于可用处理器数。\n"
            "     *\n"
            "     * @param threadFactory 创建工作线程的 ThreadFactory，优先于系统属性配置，不可为 null\n"
            "     */",
        ),
        (
            "    @NonNull\n    @Override\n    public Worker createWorker() {",
            "    /** 创建绑定单个 PoolWorker 的 EventLoopWorker。 */\n"
            "    @NonNull\n    @Override\n    public Worker createWorker() {",
        ),
        (
            "    @Override\n    public void start() {",
            "    /** CAS 将 NONE 替换为新 FixedSchedulerPool；失败则 shutdown 新建池。 */\n"
            "    @Override\n    public void start() {",
        ),
        (
            "    @Override\n    public void shutdown() {",
            "    /** getAndSet(NONE) 并 shutdown 当前 FixedSchedulerPool。 */\n"
            "    @Override\n    public void shutdown() {",
        ),
        (
            "    static final class EventLoopWorker extends Scheduler.Worker {",
            "    /** 包装单个 PoolWorker：serial/timed 分别追踪即时与延迟任务。 */\n"
            "    static final class EventLoopWorker extends Scheduler.Worker {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 置 disposed 并 dispose serial+timed 容器。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        @NonNull\n        @Override\n        public Disposable schedule(@NonNull Runnable action) {",
            "        /** 无延迟 schedule：委托 poolWorker.scheduleActual(..., serial)。 */\n"
            "        @NonNull\n        @Override\n        public Disposable schedule(@NonNull Runnable action) {",
        ),
        (
            "        @NonNull\n        @Override\n        public Disposable schedule(@NonNull Runnable action, long delayTime, @NonNull TimeUnit unit) {",
            "        /** 延迟 schedule：委托 poolWorker.scheduleActual(..., timed)。 */\n"
            "        @NonNull\n        @Override\n        public Disposable schedule(@NonNull Runnable action, long delayTime, @NonNull TimeUnit unit) {",
        ),
        (
            "    static final class PoolWorker extends NewThreadWorker {",
            "    /** 基于 NewThreadWorker 的池内工作线程。 */\n"
            "    static final class PoolWorker extends NewThreadWorker {",
        ),
    ],
    "DisposeOnCancel.java": [
        (
            "/**\n * Implements the Future interface and calls dispose() on cancel() but\n * the other methods are not implemented.\n */",
            "/**\n"
            " * 实现 {@link Future}：{@link #cancel} 时调用 upstream.dispose()，\n"
            " * 其余 Future 方法为占位实现（始终未完成/未取消）。\n"
            " */",
        ),
        (
            "record DisposeOnCancel(Disposable upstream) implements Future<Object> {",
            "/** @param upstream cancel 时要 dispose 的 Disposable */\n"
            "record DisposeOnCancel(Disposable upstream) implements Future<Object> {",
        ),
        (
            "    @Override\n    public boolean cancel(boolean mayInterruptIfRunning) {",
            "    /** 调用 upstream.dispose()；Future 语义上仍返回 false。 */\n"
            "    @Override\n    public boolean cancel(boolean mayInterruptIfRunning) {",
        ),
    ],
    "ImmediateThinScheduler.java": [
        (
            "/**\n * A Scheduler partially implementing the API by allowing only non-delayed, non-periodic\n * task execution on the current thread immediately.\n * <p>\n * Note that this doesn't support recursive scheduling and disposing the returned Disposable\n * has no effect (because when the schedule() method returns, the task has been already run).\n */",
            "/**\n"
            " * 轻量即时 Scheduler：仅支持当前线程立即执行、无延迟/无周期任务。\n"
            " * <p>\n"
            " * 不支持递归调度；返回的 Disposable dispose 无效（任务在 schedule 返回前已执行）。\n"
            " */",
        ),
        (
            "    /**\n     * The singleton instance of the immediate (thin) scheduler.\n     */",
            "    /**\n     * 即时（thin）Scheduler 单例。\n     */",
        ),
        (
            "    @NonNull\n    @Override\n    public Disposable scheduleDirect(@NonNull Runnable run) {",
            "    /** 同步 run.run() 并返回已 dispose 的 DISPOSED。 */\n"
            "    @NonNull\n    @Override\n    public Disposable scheduleDirect(@NonNull Runnable run) {",
        ),
        (
            "    @NonNull\n    @Override\n    public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {",
            "    /** 不支持延迟执行。 */\n"
            "    @NonNull\n    @Override\n    public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {",
        ),
        (
            "    static final class ImmediateThinWorker extends Worker {",
            "    /** 无状态 Worker：schedule 立即执行，dispose 无效果。 */\n"
            "    static final class ImmediateThinWorker extends Worker {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 无操作：本 Worker 不追踪任务。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        @NonNull\n        @Override\n        public Disposable schedule(@NonNull Runnable run) {",
            "        /** 当前线程同步执行 run 并返回 DISPOSED。 */\n"
            "        @NonNull\n        @Override\n        public Disposable schedule(@NonNull Runnable run) {",
        ),
    ],
    "InstantPeriodicTask.java": [
        (
            "/**\n * Wrapper for a regular task that gets immediately rescheduled when the task completed.\n */",
            "/**\n"
            " * 即时重调度周期任务包装：每次 run 完成后立即 submit 下一次，\n"
            " * 用于 period<=0 时的“连续执行”语义。\n"
            " */",
        ),
        (
            "    @Override\n    public Void call() {",
            "    /** 执行 task 后在 executor 上 submit(this) 实现链式重调度。 */\n"
            "    @Override\n    public Void call() {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** 将 first/rest Future 置为 CANCELLED 并 cancel 运行中任务。 */\n"
            "    @Override\n    public void dispose() {",
        ),
        (
            "    void setFirst(Future<?> f) {",
            "    /** CAS 设置首次 Future；已 CANCELLED 则 cancel 新 Future。 */\n"
            "    void setFirst(Future<?> f) {",
        ),
        (
            "    void setRest(Future<?> f) {",
            "    /** CAS 设置后续 Future；已 CANCELLED 则 cancel 新 Future。 */\n"
            "    void setRest(Future<?> f) {",
        ),
    ],
    "NewThreadScheduler.java": [
        (
            "/**\n * Schedules work on a new thread.\n */",
            "/**\n"
            " * 每次 createWorker 创建独立 NewThreadWorker（新线程池）的 Scheduler。\n"
            " */",
        ),
        (
            "    /** The name of the system property for setting the thread priority for this Scheduler. */",
            "    /** 设置本 Scheduler 线程优先级的系统属性键。 */",
        ),
        (
            "    public NewThreadScheduler(ThreadFactory threadFactory) {",
            "    /** @param threadFactory 创建 NewThreadWorker 底层线程的工厂 */\n"
            "    public NewThreadScheduler(ThreadFactory threadFactory) {",
        ),
        (
            "    @NonNull\n    @Override\n    public Worker createWorker() {",
            "    /** 返回基于 threadFactory 的新 NewThreadWorker。 */\n"
            "    @NonNull\n    @Override\n    public Worker createWorker() {",
        ),
    ],
    "NewThreadWorker.java": [
        (
            "/**\n * Base class that manages a single-threaded ScheduledExecutorService as a\n * worker but doesn't perform task-tracking operations.\n *\n */",
            "/**\n"
            " * 管理单线程 {@link ScheduledExecutorService} 的 Worker 基类，\n"
            " * 提供 scheduleDirect/schedulePeriodicallyDirect/scheduleActual 等底层调度。\n"
            " */",
        ),
        (
            "    public NewThreadWorker(ThreadFactory threadFactory) {",
            "    /** 通过 {@link SchedulerPoolFactory#create} 创建单线程 executor。 */\n"
            "    public NewThreadWorker(ThreadFactory threadFactory) {",
        ),
        (
            "    /**\n     * Schedules the given runnable on the underlying executor directly and\n     * returns its future wrapped into a Disposable.\n     * @param run the Runnable to execute in a delayed fashion\n     * @param delayTime the delay amount\n     * @param unit the delay time unit\n     * @return the ScheduledRunnable instance\n     */",
            "    /**\n"
            "     * 直接在底层 executor 调度 Runnable，返回 Disposable 包装的 Future。\n"
            "     * @param run 待执行的 Runnable\n"
            "     * @param delayTime 延迟时长\n"
            "     * @param unit 时间单位\n"
            "     * @return ScheduledDirectTask 实例\n"
            "     */",
        ),
        (
            "    /**\n     * Schedules the given runnable periodically on the underlying executor directly\n     * and returns its future wrapped into a Disposable.\n     * @param run the Runnable to execute in a periodic fashion\n     * @param initialDelay the initial delay amount\n     * @param period the repeat period amount\n     * @param unit the time unit for both the initialDelay and period\n     * @return the ScheduledRunnable instance\n     */",
            "    /**\n"
            "     * 直接在底层 executor 周期调度 Runnable。\n"
            "     * period<=0 时用 {@link InstantPeriodicTask} 链式重调度；\n"
            "     * 否则 scheduleAtFixedRate + {@link ScheduledDirectPeriodicTask}。\n"
            "     * @param run 周期执行的 Runnable\n"
            "     * @param initialDelay 初始延迟\n"
            "     * @param period 周期间隔\n"
            "     * @param unit 时间单位\n"
            "     * @return Disposable 包装的任务\n"
            "     */",
        ),
        (
            "    /**\n     * Wraps and returns the given runnable into a ScheduledRunnable and schedules it\n     * on the underlying ScheduledExecutorService.\n     * @param run the runnable instance\n     * @param delayTime the time to delay the execution\n     * @param unit the time unit\n     * @param parent the optional tracker parent to add the created ScheduledRunnable instance to before it gets scheduled\n     * @return the ScheduledRunnable instance\n     */",
            "    /**\n"
            "     * 包装为 {@link ScheduledRunnable} 并在 executor 上调度；\n"
            "     * parent 非 null 时先加入追踪容器。\n"
            "     * @param run Runnable 实例\n"
            "     * @param delayTime 延迟时长\n"
            "     * @param unit 时间单位\n"
            "     * @param parent 可选的任务追踪父容器\n"
            "     * @return ScheduledRunnable 实例\n"
            "     */",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** shutdownNow 并置 disposed。 */\n"
            "    @Override\n    public void dispose() {",
        ),
        (
            "    /**\n     * Shuts down the underlying executor in a non-interrupting fashion.\n     */",
            "    /**\n     * 非中断方式 shutdown 底层 executor。\n"
            "     */",
        ),
    ],
    "NonBlockingThread.java": [
        (
            "/**\n * Marker interface to indicate blocking is not recommended while running\n * on a Scheduler with a thread type implementing it.\n */",
            "/**\n"
            " * 标记接口：实现它的 Scheduler 线程上不推荐阻塞操作。\n"
            " */",
        ),
    ],
    "RxThreadFactory.java": [
        (
            "/**\n * A ThreadFactory that counts how many threads have been created and given a prefix,\n * sets the created Thread's name to {@code prefix-count}.\n */",
            "/**\n"
            " * 带计数前缀的 ThreadFactory：线程名为 {@code prefix-count}，\n"
            " * nonBlocking 为 true 时使用实现 {@link NonBlockingThread} 的 RxCustomThread。\n"
            " */",
        ),
        (
            "    @Override\n    public Thread newThread(@NonNull Runnable r) {",
            "    /** 创建 daemon 线程并设置 priority；nonBlocking 时用 RxCustomThread。 */\n"
            "    @Override\n    public Thread newThread(@NonNull Runnable r) {",
        ),
        (
            "    static final class RxCustomThread extends Thread implements NonBlockingThread {",
            "    /** 标记为非阻塞 Scheduler 线程。 */\n"
            "    static final class RxCustomThread extends Thread implements NonBlockingThread {",
        ),
    ],
    "ScheduledDirectPeriodicTask.java": [
        (
            "/**\n * A Callable to be submitted to an ExecutorService that runs a Runnable\n * action periodically and manages completion/cancellation.\n * @since 2.0.8\n */",
            "/**\n"
            " * 提交给 ExecutorService 的周期 Runnable 包装，\n"
            " * 管理完成/取消与 runner 线程中断策略。\n"
            " * @since 2.0.8\n"
            " */",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 执行 runnable；异常时 dispose 并通过 RxJavaPlugins 上报。 */\n"
            "    @Override\n    public void run() {",
        ),
    ],
    "ScheduledDirectTask.java": [
        (
            "/**\n * A Callable to be submitted to an ExecutorService that runs a Runnable\n * action and manages completion/cancellation.\n * @since 2.0.8\n */",
            "/**\n"
            " * 提交给 ExecutorService 的单次 Runnable 包装（Callable 形态），\n"
            " * 管理完成/取消状态。\n"
            " * @since 2.0.8\n"
            " */",
        ),
        (
            "    @Override\n    public Void call() {",
            "    /** 记录 runner、执行 runnable，finally 中 lazySet(FINISHED)。 */\n"
            "    @Override\n    public Void call() {",
        ),
    ],
    "ScheduledRunnable.java": [
        (
            "public final class ScheduledRunnable extends AtomicReferenceArray<Object>\nimplements Runnable, Callable<Object>, Disposable, SchedulerRunnableIntrospection {",
            "/**\n"
            " * 可追踪的调度任务：AtomicReferenceArray 存 parent/future/thread，\n"
            " * 支持同步/异步 dispose 与 {@link SchedulerRunnableIntrospection}。\n"
            " */\n"
            "public final class ScheduledRunnable extends AtomicReferenceArray<Object>\nimplements Runnable, Callable<Object>, Disposable, SchedulerRunnableIntrospection {",
        ),
        (
            "    /** Indicates that the parent tracking this task has been notified about its completion. */",
            "    /** 父追踪容器已收到本任务完成通知。 */",
        ),
        (
            "    /** Indicates dispose() was called from within the run/call method. */",
            "    /** dispose() 在 run/call 内部同线程调用。 */",
        ),
        (
            "    /** Indicates dispose() was called from another thread. */",
            "    /** dispose() 从其他线程异步调用。 */",
        ),
        (
            "    /**\n     * Creates a ScheduledRunnable by wrapping the given action and setting\n     * up the optional parent.\n     * The underlying future will be interrupted if the task is disposed asynchronously.\n     * @param actual the runnable to wrap, not-null (not verified)\n     * @param parent the parent tracking container or null if none\n     */",
            "    /**\n"
            "     * 包装 Runnable 并设置可选 parent；默认 interruptOnCancel=true。\n"
            "     * @param actual 待包装的 Runnable（未校验非 null）\n"
            "     * @param parent 任务追踪容器，可为 null\n"
            "     */",
        ),
        (
            "    /**\n     * Creates a ScheduledRunnable by wrapping the given action and setting\n     * up the optional parent.\n     * @param actual the runnable to wrap, not-null (not verified)\n     * @param parent the parent tracking container or null if none\n     * @param interruptOnCancel if true, the underlying future will be interrupted when disposing\n     *                          this task from a different thread than it is running on.\n     */",
            "    /**\n"
            "     * 包装 Runnable 并设置 parent 与 interruptOnCancel 策略。\n"
            "     * @param actual 待包装的 Runnable（未校验非 null）\n"
            "     * @param parent 任务追踪容器，可为 null\n"
            "     * @param interruptOnCancel 异步 dispose 时是否 interrupt 底层 Future\n"
            "     */",
        ),
        (
            "    @Override\n    public Object call() {",
            "    /** Callable 入口：调用 run() 以节省 ThreadPoolExecutor 分配。 */\n"
            "    @Override\n    public Object call() {",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 执行 actual.run()，finally 中通知 parent 并清理 future/thread 槽位。 */\n"
            "    @Override\n    public void run() {",
        ),
        (
            "    public void setFuture(Future<?> f) {",
            "    /** CAS 设置 Future；已 dispose 则按策略 cancel。 */\n"
            "    public void setFuture(Future<?> f) {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** 标记 SYNC/ASYNC_DISPOSED 并 cancel Future；通知 parent 删除本任务。 */\n"
            "    @Override\n    public void dispose() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回 Waiting/Running/Finished/Disposed 等状态摘要。 */\n"
            "    @Override\n    public String toString() {",
        ),
    ],
    "SchedulerMultiWorkerSupport.java": [
        (
            "/**\n * Allows retrieving multiple workers from the implementing\n * {@link io.reactivex.rxjava4.core.Scheduler} in a way that when asking for\n * at most the parallelism level of the Scheduler, those\n * {@link io.reactivex.rxjava4.core.Scheduler.Worker} instances will be running\n * with different backing threads.\n * <p>History: 2.1.8 - experimental\n * @since 2.2\n */",
            "/**\n"
            " * 允许从实现类一次性获取多个 {@link Scheduler.Worker}：\n"
            " * 请求数不超过并行度时，各 Worker 绑定不同底层线程。\n"
            " * <p>History: 2.1.8 - experimental\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    /**\n     * Creates the given number of {@link io.reactivex.rxjava4.core.Scheduler.Worker} instances\n     * that are possibly backed by distinct threads\n     * and calls the specified {@code Consumer} with them.\n     * @param number the number of workers to create, positive\n     * @param callback the callback to send worker instances to\n     */",
            "    /**\n"
            "     * 创建 number 个可能由不同线程支撑的 Worker，\n"
            "     * 通过 callback 逐个回调。\n"
            "     * @param number 要创建的 Worker 数量，须为正数\n"
            "     * @param callback 接收 Worker 实例的回调\n"
            "     */",
        ),
        (
            "    /**\n     * The callback interface for the {@link SchedulerMultiWorkerSupport#createWorkers(int, WorkerCallback)}\n     * method.\n     */",
            "    /**\n"
            "     * {@link #createWorkers(int, WorkerCallback)} 的回调接口。\n"
            "     */",
        ),
        (
            "        /**\n         * Called with the Worker index and instance.\n         * @param index the worker index, zero-based\n         * @param worker the worker instance\n         */",
            "        /**\n"
            "         * 回调 Worker 索引与实例。\n"
            "         * @param index Worker 索引，从 0 开始\n"
            "         * @param worker Worker 实例\n"
            "         */",
        ),
    ],
    "SchedulerPoolFactory.java": [
        (
            "/**\n * Manages the creating of ScheduledExecutorServices and sets up purging.\n */",
            "/**\n"
            " * 创建 {@link ScheduledExecutorService} 并配置 removeOnCancelPolicy（purge）。\n"
            " */",
        ),
        (
            "    /** Utility class. */",
            "    /** 工具类，不可实例化。 */",
        ),
        (
            "    /**\n     * Creates a ScheduledExecutorService with the given factory.\n     * @param factory the thread factory\n     * @return the ScheduledExecutorService\n     */",
            "    /**\n"
            "     * 使用给定 ThreadFactory 创建单线程 ScheduledThreadPoolExecutor。\n"
            "     * @param factory 线程工厂\n"
            "     * @return ScheduledExecutorService\n"
            "     */",
        ),
    ],
    "SchedulerToExecutorService.java": [
        (
            "/**\n * Represents the state for a Scheduler -&gt; ExecutorService interface.\n * @param scheduler the scheduler to use\n * @param workerStore hosts the worker state\n * @since 4.0.0\n */",
            "/**\n"
            " * 将 {@link Scheduler} 适配为 {@link ExecutorService}：\n"
            " * workerStore 有 Worker 时走 w.schedule，否则 scheduleDirect。\n"
            " * @param scheduler 底层 Scheduler\n"
            " * @param workerStore 持有 Worker 状态的原子引用\n"
            " * @since 4.0.0\n"
            " */",
        ),
        (
            "    @Override\n    public void execute(Runnable command) {",
            "    /** Worker 可用则 schedule，否则 scheduleDirect。 */\n"
            "    @Override\n    public void execute(Runnable command) {",
        ),
        (
            "    @Override\n    public void shutdown() {",
            "    /** dispose Worker 或 getAndSet(SHUTDOWN) 后 dispose。 */\n"
            "    @Override\n    public void shutdown() {",
        ),
        (
            "    @Override\n    public List<Runnable> shutdownNow() {",
            "    /** 同 shutdown，返回空列表。 */\n"
            "    @Override\n    public List<Runnable> shutdownNow() {",
        ),
        (
            "    @Override\n    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {",
            "    /** 轮询 isTerminated 直至超时（Rx 场景下被动等待）。 */\n"
            "    @Override\n    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {",
        ),
        (
            "    @Override\n    public <T> Future<T> submit(Callable<T> task) {",
            "    /** 通过 CompletableFuture.supplyAsync 在 Scheduler 上执行 Callable。 */\n"
            "    @Override\n    public <T> Future<T> submit(Callable<T> task) {",
        ),
        (
            "    @Override\n    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {",
            "    /** 逐个 submit 并阻塞 get 等待全部完成。 */\n"
            "    @Override\n    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {",
        ),
        (
            "    @Override\n    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {",
            "    /** 并行 submit，首个 complete 的 CompletableFuture 胜出并 cancel 其余。 */\n"
            "    @Override\n    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {",
        ),
        (
            "    record CompletedIndexValue<T>(int index, T value) {",
            "    /** invokeAny 中记录获胜任务索引与返回值。 */\n"
            "    record CompletedIndexValue<T>(int index, T value) {",
        ),
        (
            "    static final class CompletionSignaller extends AtomicInteger {",
            "    /** 计数 invokeAny/invokeAll 中未完成任务数，归零时 complete signal。 */\n"
            "    static final class CompletionSignaller extends AtomicInteger {",
        ),
    ],
    "SharedScheduler.java": [
        (
            "/**\n * A Scheduler implementation that uses one of the Workers from another Scheduler\n * and shares the access to it through its own Workers.\n * <p>\n * Disposing a worker doesn't dispose the underlying shared worker so other\n * workers of this class can continue their work; use {@link #shutdown()} to release\n * the underlying shared worker.\n * <p>\n * This scheduler doesn't support {@link #start()} (it's a no-op) and once {@link #shutdown()}\n * it can't be revived.\n * @since 4.0.0\n */",
            "/**\n"
            " * 共享底层 Worker 的 Scheduler：多个 SharedWorker 共用同一 worker，\n"
            " * dispose SharedWorker 不会释放共享 worker，需 {@link #shutdown()}。\n"
            " * 不支持 {@link #start()}，shutdown 后不可恢复。\n"
            " * @since 4.0.0\n"
            " */",
        ),
        (
            "    /**\n     * Constructs a SharedScheduler and uses the Worker instance provided.\n     * @param worker the worker to use, not null\n     */",
            "    /**\n"
            "     * 使用给定 Worker 构造 SharedScheduler。\n"
            "     * @param worker 共享的 Worker，不可为 null\n"
            "     */",
        ),
        (
            "    @Override\n    public void shutdown() {",
            "    /** dispose 共享 worker。 */\n"
            "    @Override\n    public void shutdown() {",
        ),
        (
            "    @Override\n    public Worker createWorker() {",
            "    /** 创建绑定同一 worker 的 SharedWorker。 */\n"
            "    @Override\n    public Worker createWorker() {",
        ),
        (
            "    static final class SharedWorker extends Worker {",
            "    /** 在共享 worker 上调度任务，tasks 容器追踪本 Worker 的 SharedAction。 */\n"
            "    static final class SharedWorker extends Worker {",
        ),
        (
            "        @Override\n        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {",
            "        /** 创建 SharedAction 加入 tasks，委托 worker.schedule 并 setFuture。 */\n"
            "        @Override\n        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {",
        ),
        (
            "        static final class SharedAction\n        extends AtomicReference<DisposableContainer>\n        implements Runnable, Disposable, SchedulerRunnableIntrospection {",
            "        /** 可取消的共享任务：run 后 complete 从 parent 移除。 */\n"
            "        static final class SharedAction\n        extends AtomicReference<DisposableContainer>\n        implements Runnable, Disposable, SchedulerRunnableIntrospection {",
        ),
        (
            "            @Override\n            public void run() {",
            "            /** 执行 actual.run()，finally 调用 complete()。 */\n"
            "            @Override\n            public void run() {",
        ),
        (
            "            void complete() {",
            "            /** 从 parent 删除自身并将 future 置为 this。 */\n"
            "            void complete() {",
        ),
        (
            "            @Override\n            public void dispose() {",
            "            /** 从 parent 移除并 DisposableHelper.dispose(future)。 */\n"
            "            @Override\n            public void dispose() {",
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(
        subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines()
    )
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


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def annotate_file(rel: str) -> None:
    name = Path(rel).name
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    reps = FILE_REPLACEMENTS.get(name, [])
    if not reps:
        raise ValueError(f"NO_REPLACEMENTS: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
    cn = len(re.findall(r"[\u4e00-\u9fff]", text))
    if cn < 10 or "Licensed under the Apache License" not in text:
        raise ValueError(f"VALIDATION cn={cn}: {rel}")
    dst.write_text(text, encoding="utf-8")


def isolated_index_commit(
    message: str, paths: list[str], base_ref: str = "origin/main"
) -> tuple[str, int]:
    index_file = Path("/tmp/git-index-rxjava-w24b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True, env=env
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
    tree = subprocess.check_output(
        ["git", "-C", str(ROOT), "write-tree"], env=env, text=True
    ).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
        env=env,
    ).strip()
    subprocess.run(
        ["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True
    )
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
            subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)
            time.sleep(4 * (2**attempt))
    raise subprocess.CalledProcessError(r.returncode, r.args, r.stdout, r.stderr)


def confirm_chinese_on_origin() -> dict[str, bool]:
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    result: dict[str, bool] = {}
    for rel in BATCH_FILES:
        blob = subprocess.check_output(
            [
                "git",
                "-C",
                str(ROOT),
                "show",
                f"origin/main:rxjava/4.0.0-alpha-21/analyzed/{rel}",
            ],
            text=True,
        )
        result[rel] = has_chinese(blob)
    return result


def main() -> int:
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "checkout", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)

    failures: list[str] = []
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        print(json.dumps({"ok": 0, "failures": failures}, ensure_ascii=False, indent=2))
        return 1

    analyzed_paths = [f"rxjava/4.0.0-alpha-21/analyzed/{rel}" for rel in BATCH_FILES]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 24b [15:30]",
        [*analyzed_paths, script_path],
    )
    push_main()

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "rxjava",
            "--version",
            "4.0.0-alpha-21",
            "--note",
            MARK_NOTE,
            *BATCH_FILES,
        ],
        check=True,
    )
    queue_paths = [
        "rxjava/4.0.0-alpha-21/_reports/class-queue/done.txt",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/pending.txt",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/batch.json",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark rxjava 4.0.0-alpha-21 wave24b done",
        queue_paths,
        base_ref="HEAD",
    )
    push_main()

    done_total = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    pending_total = len(
        [
            ln
            for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines()
            if ln.strip()
        ]
    )
    chinese = confirm_chinese_on_origin()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese,
                "all_chinese": all(chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(chinese.values()) else 1


if __name__ == "__main__":
    raise SystemExit(main())
