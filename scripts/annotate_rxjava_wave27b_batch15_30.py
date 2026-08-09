#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-27b operators/parallel/plugins/processors [15:30]."""
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
WAVE27B_FILE = Path("/tmp/rxjava_w27b.txt")
SCRIPT_NAME = "annotate_rxjava_wave27b_batch15_30.py"
MARK_NOTE = "wave27b [15:30]"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE27B_FILE.read_text(encoding="utf-8").splitlines()
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
    "QueueFuseable.java": [
        (
            "/**\n * Represents a {@link SimpleQueue} plus the means and constants for requesting a fusion mode.\n * @param <T> the value type returned by the SimpleQueue.poll()\n * @since 3.1.1\n */",
            "/**\n"
            " * 扩展 {@link SimpleQueue}，定义算子融合（fusion）模式常量与协商方法。\n"
            " * 上游/下游通过 {@link #requestFusion(int)} 决定是否用 poll 替代 onNext 链。\n"
            " *\n"
            " * @param <T> poll 返回的元素类型\n"
            " * @since 3.1.1\n"
            " */",
        ),
        (
            "    /**\n     * Returned by the {@link #requestFusion(int)} if the upstream doesn't support\n     * the requested mode.\n     */",
            "    /** 上游不支持所请求融合模式时 {@link #requestFusion(int)} 的返回值。 */",
        ),
        (
            "    /**\n     * Request a synchronous fusion mode and can be returned by {@link #requestFusion(int)}\n     * for an accepted mode.\n     * <p>\n     * In synchronous fusion, all upstream values are either already available or is generated\n     * when {@link #poll()} is called synchronously. When the {@link #poll()} returns null,\n     * that is the indication if a terminated stream.\n     * In this mode, the upstream won't call the onXXX methods and callers of\n     * {@link #poll()} should be prepared to catch exceptions. Note that {@link #poll()} has\n     * to be called sequentially (from within a serializing drain-loop).\n     */",
            "    /**\n"
            "     * 同步融合：值在 poll 时同步产生或已就绪；poll 返回 null 表示终止。\n"
            "     * 此模式下上游不调用 onXXX，poll 须在串行 drain-loop 中调用并捕获异常。\n"
            "     */",
        ),
        (
            "    /**\n     * Request an asynchronous fusion mode and can be returned by {@link #requestFusion(int)}\n     * for an accepted mode.\n     * <p>\n     * In asynchronous fusion, upstream values may become available to {@link #poll()} eventually.\n     * Upstream signals onError() and onComplete() as usual but onNext may not actually contain\n     * the upstream value but have {@code null} instead. Downstream should treat such onNext as indication\n     * that {@link #poll()} can be called. Note that {@link #poll()} has to be called sequentially\n     * (from within a serializing drain-loop). In addition, callers of {@link #poll()} should be\n     * prepared to catch exceptions.\n     */",
            "    /**\n"
            "     * 异步融合：上游值最终 经 poll 可用；onNext(null) 提示可 poll。\n"
            "     * onError/onComplete 仍正常；poll 须串行调用并捕获异常。\n"
            "     */",
        ),
        (
            "    /**\n     * Request any of the {@link #SYNC} or {@link #ASYNC} modes.\n     */",
            "    /** 请求 SYNC 或 ASYNC 任一可接受模式（位或）。 */",
        ),
        (
            "    /**\n     * Used in binary or combination with the other constants as an input to {@link #requestFusion(int)}\n     * indicating that the {@link #poll()} will be called behind an asynchronous boundary and thus\n     * may change the non-trivial computation locations attached to the {@link #poll()} chain of\n     * fused operators.\n     * <p>\n     * For example, fusing map() and observeOn() may move the computation of the map's function over to\n     * the thread run after the observeOn(), which is generally unexpected.\n     */",
            "    /**\n"
            "     * 与 SYNC/ASYNC 组合使用：poll 将在异步边界之后调用，\n"
            "     * 可能改变融合链上计算所在线程（如 map+observeOn 时 map 跑到 observeOn 线程）。\n"
            "     */",
        ),
        (
            "    /**\n     * Request a fusion mode from the upstream.\n     * <p>\n     * This should be called before {@code onSubscribe} returns.\n     * <p>\n     * Calling this method multiple times or after {@code onSubscribe} finished is not allowed\n     * and may result in undefined behavior.\n     * <p>\n     * @param mode the requested fusion mode, allowed values are {@link #SYNC}, {@link #ASYNC},\n     * {@link #ANY} combined with {@link #BOUNDARY} (e.g., {@code requestFusion(SYNC | BOUNDARY)}).\n     * @return the established fusion mode: {@link #NONE}, {@link #SYNC}, {@link #ASYNC}.\n     */",
            "    /**\n"
            "     * 在 onSubscribe 返回前向 upstream 请求融合模式。\n"
            "     * 不可重复调用或于 onSubscribe 之后调用。\n"
            "     *\n"
            "     * @param mode SYNC、ASYNC、ANY，可与 BOUNDARY 组合\n"
            "     * @return 实际建立的 NONE、SYNC 或 ASYNC\n"
            "     */",
        ),
    ],
    "QueueSubscription.java": [
        (
            "/**\n * An interface extending {@link SimpleQueue} and {@link Subscription} and allows negotiating\n * the fusion mode between subsequent operators of the {@link io.reactivex.rxjava4.core.Flowable Flowable} base reactive type.\n * <p>\n * The negotiation happens in subscription time when the upstream\n * calls the {@code onSubscribe} with an instance of this interface. The\n * downstream has then the obligation to call {@link #requestFusion(int)}\n * with the appropriate mode before calling {@code request()}.\n * <p>\n * In <b>synchronous fusion</b>, all upstream values are either already available or is generated\n * when {@link #poll()} is called synchronously. When the {@link #poll()} returns null,\n * that is the indication if a terminated stream. Downstream should not call {@link #request(long)}\n * in this mode. In this mode, the upstream won't call the onXXX methods.\n * <p>\n * In <b>asynchronous fusion</b>, upstream values may become available to {@link #poll()} eventually.\n * Upstream signals {@code onError()} and {@code onComplete()} as usual, however,\n * {@code onNext} will be called with {@code null} instead of the actual value.\n * Downstream should treat such onNext as indication that {@link #poll()} can be called.\n * In this mode, the downstream still has to call {@link #request(long)}\n * to indicate it is prepared to receive more values.\n * <p>\n * The general rules for consuming the {@link SimpleQueue} interface:\n * <ul>\n * <li> {@link #poll()} and {@link #clear()} has to be called sequentially (from within a serializing drain-loop).</li>\n * <li>In addition, callers of {@link #poll()} should be prepared to catch exceptions.</li>\n * <li>Due to how computation attaches to the {@link #poll()}, {@link #poll()} may return\n * {@code null} even if a preceding {@link #isEmpty()} returned false.</li>\n * </ul>\n * <p>\n * Implementations should only allow calling the following methods and the rest of the\n * {@link SimpleQueue} interface methods should throw {@link UnsupportedOperationException}:\n * <ul>\n * <li>{@link #poll()}</li>\n * <li>{@link #isEmpty()}</li>\n * <li>{@link #clear()}</li>\n * </ul>\n * @param <T> the value type transmitted through the queue\n * @see QueueDisposable\n * @since 3.1.1\n */",
            "/**\n"
            " * Flowable 侧融合队列：同时是 {@link SimpleQueue} 与 {@link Subscription}。\n"
            " * 订阅时 upstream 以本接口 onSubscribe，下游须在 request 前调用 {@link #requestFusion(int)}。\n"
            " *\n"
            " * <p><b>同步融合</b>：poll 同步取数，null 表终止；不应再 request。\n"
            " * <p><b>异步融合</b>：onNext(null) 提示可 poll，仍需 request 背压。\n"
            " *\n"
            " * <p>消费规则：poll/clear 须串行 drain-loop；poll 可能抛异常；\n"
            " * isEmpty 为 false 时 poll 仍可能因融合函数返回 null。\n"
            " *\n"
            " * <p>融合实现通常仅支持 poll、isEmpty、clear，其余抛 UnsupportedOperationException。\n"
            " *\n"
            " * @param <T> 队列元素类型\n"
            " * @see QueueDisposable\n"
            " * @since 3.1.1\n"
            " */",
        ),
    ],
    "ScalarSupplier.java": [
        (
            "/**\n * A marker interface indicating that a scalar, constant value\n * is held by the implementing reactive type which can be\n * safely extracted during assembly time can be used for\n * optimization.\n * <p>\n * Implementors of {@link #get()} should not throw any exception.\n * <p>\n * Design note: the interface extends {@link Supplier} because if a scalar\n * is safe to extract during assembly time, it is also safe to extract at\n * subscription time or later. This allows optimizations to deal with such\n * single-element sources uniformly.\n * <p>\n * @param <T> the scalar value type held by the implementing reactive type\n * @since 3.1.1\n */",
            "/**\n"
            " * 标记接口：实现类持有可在装配期安全提取的标量常量，供算子优化。\n"
            " *\n"
            " * <p>{@link #get()} 不应抛异常。\n"
            " * <p>继承 {@link Supplier}：装配期可提取则订阅期亦可，便于统一处理单元素源。\n"
            " *\n"
            " * @param <T> 标量值类型\n"
            " * @since 3.1.1\n"
            " */",
        ),
        (
            "    // overridden to remove the throws Throwable",
            "    /** 覆写以去掉 throws Throwable。 */",
        ),
    ],
    "SimplePlainQueue.java": [
        (
            "/**\n * Override of the {@link SimpleQueue} interface with no {@code throws Throwable} on {@code poll()}.\n *\n * @param <T> the value type to offer and poll, not null\n * @since 3.1.1\n */",
            "/**\n"
            " * {@link SimpleQueue} 变体：{@code poll()} 不声明 throws Throwable。\n"
            " * 供 SPSC 队列等不通过融合函数抛错的实现使用。\n"
            " *\n"
            " * @param <T> offer/poll 的元素类型，非 null\n"
            " * @since 3.1.1\n"
            " */",
        ),
    ],
    "SimpleQueue.java": [
        (
            "/**\n * A simplified interface for offering, polling and clearing a queue.\n * <p>\n * This interface does not define most of the {@link java.util.Collection}\n * or {@link java.util.Queue} methods as the intended usage of {@code SimpleQueue}\n * does not require support for iteration or introspection.\n *\n * @param <T> the value type to offer and poll, not null\n * @since 3.1.1\n */",
            "/**\n"
            " * 精简队列接口：仅 offer、poll、isEmpty、clear。\n"
            " * 不含 Collection/Queue 的迭代与内省方法，供算子内部缓冲使用。\n"
            " *\n"
            " * @param <T> 元素类型，非 null\n"
            " * @since 3.1.1\n"
            " */",
        ),
        (
            "    /**\n     * Atomically enqueue a single value.\n     * @param value the value to enqueue, not null\n     * @return true if successful, false if the value was not enqueued\n     * likely due to reaching the queue capacity)\n     */",
            "    /**\n"
            "     * 原子入队单个元素。\n"
            "     * @param value 非 null 元素\n"
            "     * @return 成功 true；容量满等则 false\n"
            "     */",
        ),
        (
            "    /**\n     * Atomically enqueue two values.\n     * @param v1 the first value to enqueue, not null\n     * @param v2 the second value to enqueue, not null\n     * @return true if successful, false if the value was not enqueued\n     * likely due to reaching the queue capacity)\n     */",
            "    /**\n"
            "     * 原子入队两个元素（成对 poll 时第二项保证非 null）。\n"
            "     * @return 成功 true；满则 false\n"
            "     */",
        ),
        (
            "    /**\n     * Tries to dequeue a value (non-null) or returns null if\n     * the queue is empty.\n     * <p>\n     * If the producer uses {@link #offer(Object, Object)} and\n     * when polling in pairs, if the first poll() returns a non-null\n     * item, the second poll() is guaranteed to return a non-null item\n     * as well.\n     * @return the item or null to indicate an empty queue\n     * @throws Throwable if some pre-processing of the dequeued\n     * item (usually through fused functions) throws.\n     */",
            "    /**\n"
            "     * 出队非 null 元素，空队列返回 null。\n"
            "     * 成对 offer 时，若第一次 poll 非 null，第二次 poll 也保证非 null。\n"
            "     * @throws Throwable 融合函数预处理可能抛出\n"
            "     */",
        ),
        (
            "    /**\n     * Returns true if the queue is empty.\n     * <p>\n     * Note however that due to potential fused functions in {@link #poll()}\n     * it is possible this method returns false but then poll() returns null\n     * because the fused function swallowed the available item(s).\n     * @return true if the queue is empty\n     */",
            "    /**\n"
            "     * 队列是否为空。\n"
            "     * 融合 poll 可能使 isEmpty 为 false 而 poll 仍返回 null。\n"
            "     */",
        ),
        (
            "    /**\n     * Removes all enqueued items from this queue.\n     */",
            "    /** 清空队列中所有已入队元素。 */",
        ),
    ],
    "SpscArrayQueue.java": [
        (
            "/**\n * A Single-Producer-Single-Consumer queue backed by a pre-allocated buffer.\n * <p>\n * This implementation is a mashup of the <a href=\"http://sourceforge.net/projects/mc-fastflow/\">Fast Flow</a>\n * algorithm with an optimization of the offer method taken from the <a\n * href=\"http://staff.ustc.edu.cn/~bhua/publications/IJPP_draft.pdf\">BQueue</a> algorithm (a variation on Fast\n * Flow), and adjusted to comply with Queue.offer semantics regarding capacity.<br>\n * For convenience the relevant papers are available in the resources folder:<br>\n * <i>2010 - Pisa - SPSC Queues on Shared Cache Multi-Core Systems.pdf<br>\n * 2012 - Junchang- BQueue- Efficient and Practical Queuing.pdf <br>\n * </i> This implementation is wait free.\n *\n * @param <E> the element type of the queue\n * @since 3.1.1\n */",
            "/**\n"
            " * 预分配缓冲区的单生产者单消费者（SPSC）无锁队列（源自 JCTools）。\n"
            " * 融合 Fast Flow 与 BQueue 的 offer 优化，容量满时 offer 返回 false。\n"
            " * wait-free；容量向上取 2 的幂。\n"
            " *\n"
            " * @param <E> 元素类型\n"
            " * @since 3.1.1\n"
            " */",
        ),
        (
            "    /**\n     * Constructs an array-backed queue with the given capacity rounded\n     * up to the next power of 2 size.\n     * @param capacity the maximum number of elements the queue would hold,\n     *                 rounded up to the next power of 2\n     */",
            "    /**\n"
            "     * @param capacity 最大元素数（向上取 2 的幂作为数组长度）\n"
            "     */",
        ),
        (
            "    @Override\n    public boolean offer(E e) {",
            "    /** look-ahead 探测空槽；满则 false；lazySet 更新 producerIndex。 */\n"
            "    @Override\n    public boolean offer(E e) {",
        ),
        (
            "    @Override\n    public boolean offer(E v1, E v2) {",
            "    /** 连续两次 offer（FIXME：非原子双入队）。 */\n"
            "    @Override\n    public boolean offer(E v1, E v2) {",
        ),
        (
            "    @Nullable\n    @Override\n    public E poll() {",
            "    /** 读槽位非 null 则推进 consumerIndex 并清空槽。 */\n"
            "    @Nullable\n    @Override\n    public E poll() {",
        ),
        (
            "    @Override\n    public boolean isEmpty() {",
            "    /** producerIndex == consumerIndex。 */\n"
            "    @Override\n    public boolean isEmpty() {",
        ),
        (
            "    @Override\n    public void clear() {",
            "    /** 循环 poll 直至空（poll 弱保证需配合 isEmpty）。 */\n"
            "    @Override\n    public void clear() {",
        ),
    ],
    "SpscLinkedArrayQueue.java": [
        (
            "/**\n * A single-producer single-consumer array-backed queue which can allocate new arrays in case the consumer is slower\n * than the producer.\n * @param <T> the contained value type\n * @since 3.1.1\n */",
            "/**\n"
            " * SPSC 链表式数组队列：消费慢于生产时可分配新数组岛并链接。\n"
            " * 末槽 HAS_NEXT 指向下一岛；支持原子双元素 offer。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " * @since 3.1.1\n"
            " */",
        ),
        (
            "    /**\n     * Constructs a linked array-based queue instance with the given\n     * island size rounded up to the next power of 2.\n     * @param bufferSize the maximum number of elements per island\n     */",
            "    /**\n"
            "     * @param bufferSize 每个数组岛的最大元素数（向上取 2 的幂，至少 8）\n"
            "     */",
        ),
        (
            "    /**\n     * {@inheritDoc}\n     * <p>\n     * This implementation is correct for single producer thread use only.\n     */",
            "    /** {@inheritDoc} 仅单生产者线程安全。 */",
        ),
        (
            "    @Override\n    public boolean offer(final T e) {",
            "    /** look-ahead 或 resize 新岛后写入；仅单生产者。 */\n"
            "    @Override\n    public boolean offer(final T e) {",
        ),
        (
            "    /**\n     * {@inheritDoc}\n     * <p>\n     * This implementation is correct for single consumer thread use only.\n     */",
            "    /** {@inheritDoc} 仅单消费者线程安全。 */",
        ),
        (
            "    @Nullable\n    @SuppressWarnings(\"unchecked\")\n    @Override\n    public T poll() {",
            "    /** 遇 HAS_NEXT 则切换到下一岛再取；仅单消费者。 */\n"
            "    @Nullable\n    @SuppressWarnings(\"unchecked\")\n    @Override\n    public T poll() {",
        ),
        (
            "    /**\n     * Returns the next element in this queue without removing it or {@code null}\n     * if this queue is empty\n     * @return the next element or {@code null}\n     */",
            "    /** 窥视队首元素不移除；空则 null。 */",
        ),
        (
            "    /**\n     * Returns the number of elements in the queue.\n     * @return the number of elements in the queue\n     */",
            "    /** 估算队列长度（先读 consumer 再读 producer，可能高估）。 */",
        ),
        (
            "    /**\n     * Offer two elements at the same time.\n     * <p>Don't use the regular offer() with this at all!\n     * @param first the first value, not null\n     * @param second the second value, not null\n     * @return true if the queue accepted the two new values\n     */",
            "    /**\n"
            "     * 原子入队两个元素；勿与普通 offer 混用。\n"
            "     * 空间不足时扩容新岛并链接。\n"
            "     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/operators/package-info.java": [
        (
            "/**\n * Classes and interfaces for writing advanced operators within and outside RxJava.\n */",
            "/**\n"
            " * 编写 RxJava 内外高级算子所需的队列、融合等接口与实现。\n"
            " */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/parallel/package-info.java": [
        (
            "/**\n * Contains the base type {@link io.reactivex.rxjava4.parallel.ParallelFlowable},\n * a sub-DSL for working with {@link io.reactivex.rxjava4.core.Flowable} sequences in parallel.\n */",
            "/**\n"
            " * 并行流子 DSL：{@link io.reactivex.rxjava4.parallel.ParallelFlowable} 及\n"
            " * 在多条 rail 上并行处理 {@link io.reactivex.rxjava4.core.Flowable} 的相关类型。\n"
            " */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/plugins/package-info.java": [
        (
            "/**\n * Contains the central plugin handler {@link io.reactivex.rxjava4.plugins.RxJavaPlugins}\n * class to hook into the lifecycle of the base reactive types and schedulers.\n */",
            "/**\n"
            " * 全局插件入口 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins}：\n"
            " * 挂钩基础响应式类型与 Scheduler 的生命周期。\n"
            " */",
        ),
    ],
    "ParallelFailureHandling.java": [
        (
            "/**\n * Enumerations for handling failure within a parallel operator.\n * <p>History: 2.0.8 - experimental\n * @since 2.2\n */",
            "/**\n"
            " * 并行算子内错误处理策略枚举，同时作为 BiFunction 恒返回自身。\n"
            " * <p>History: 2.0.8 - experimental\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    /**\n     * The current rail is stopped and the error is dropped.\n     */",
            "    /** 停止当前 rail 并丢弃错误。 */",
        ),
        (
            "    /**\n     * The current rail is stopped and the error is signaled.\n     */",
            "    /** 停止当前 rail 并向下游传播错误。 */",
        ),
        (
            "    /**\n     * The current value and error is ignored and the rail resumes with the next item.\n     */",
            "    /** 跳过当前项与错误，继续处理下一项。 */",
        ),
        (
            "    /**\n     * Retry the current value.\n     */",
            "    /** 重试当前元素。 */",
        ),
    ],
    "ParallelFlowableConverter.java": [
        (
            "/**\n * Convenience interface and callback used by the {@link ParallelFlowable#to} operator to turn a ParallelFlowable into\n * another value fluently.\n * <p>History: 2.1.7 - experimental\n * @param <T> the upstream type\n * @param <R> the output type\n * @since 2.2\n */",
            "/**\n"
            " * {@link ParallelFlowable#to} 的回调：将 ParallelFlowable 转换为任意类型 R。\n"
            " * <p>History: 2.1.7 - experimental\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 转换结果类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    /**\n     * Applies a function to the upstream ParallelFlowable and returns a converted value of type {@code R}.\n     *\n     * @param upstream the upstream ParallelFlowable instance\n     * @return the converted value\n     */",
            "    /**\n"
            "     * 对 upstream 应用转换逻辑并返回 R。\n"
            "     * @param upstream 上游 ParallelFlowable\n"
            "     * @return 转换结果\n"
            "     */",
        ),
    ],
    "ParallelTransformer.java": [
        (
            "/**\n * Interface to compose ParallelFlowable.\n * <p>History: 2.0.8 - experimental\n * @param <Upstream> the upstream value type\n * @param <Downstream> the downstream value type\n * @since 2.2\n */",
            "/**\n"
            " * 组合 ParallelFlowable 的函数式接口（compose 参数）。\n"
            " * <p>History: 2.0.8 - experimental\n"
            " *\n"
            " * @param <Upstream> 上游元素类型\n"
            " * @param <Downstream> 下游元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    /**\n     * Applies a function to the upstream ParallelFlowable and returns a ParallelFlowable with\n     * optionally different element type.\n     * @param upstream the upstream ParallelFlowable instance\n     * @return the transformed ParallelFlowable instance\n     */",
            "    /**\n"
            "     * 变换 upstream 并返回（可能换型的）ParallelFlowable。\n"
            "     * @param upstream 上游实例\n"
            "     * @return 变换后的 ParallelFlowable\n"
            "     */",
        ),
    ],
    "AsyncProcessor.java": [
        (
            "/**\n * Processor that emits the very last value followed by a completion event or the received error\n * to {@link Subscriber}s.\n * <p>\n * <img width=\"640\" height=\"239\" src=\"https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/AsyncProcessor.png\" alt=\"\">\n * <p>\n * This processor does not have a public constructor by design; a new empty instance of this\n * {@code AsyncProcessor} can be created via the {@link #create()} method.\n * <p>\n * Since an {@code AsyncProcessor} is a Reactive Streams {@code Processor} type,\n * {@code null}s are not allowed (<a href=\"https://github.com/reactive-streams/reactive-streams-jvm#2.13\">Rule 2.13</a>)\n * as parameters to {@link #onNext(Object)} and {@link #onError(Throwable)}. Such calls will result in a\n * {@link NullPointerException} being thrown and the processor's state is not changed.\n * <p>\n * {@code AsyncProcessor} is a {@link io.reactivex.rxjava4.core.Flowable} as well as a {@link FlowableProcessor} and supports backpressure from the downstream but\n * its {@link Subscriber}-side consumes items in an unbounded manner.\n * <p>\n * When this {@code AsyncProcessor} is terminated via {@link #onError(Throwable)}, the\n * last observed item (if any) is cleared and late {@link Subscriber}s only receive\n * the {@code onError} event.\n * <p>\n * The {@code AsyncProcessor} caches the latest item internally and it emits this item only when {@code onComplete} is called.\n * Therefore, it is not recommended to use this {@code Processor} with infinite or never-completing sources.\n * <p>\n * Even though {@code AsyncProcessor} implements the {@link Subscriber} interface, calling\n * {@code onSubscribe} is not required (<a href=\"https://github.com/reactive-streams/reactive-streams-jvm#2.12\">Rule 2.12</a>)\n * if the processor is used as a standalone source. However, calling {@code onSubscribe}\n * after the {@code AsyncProcessor} reached its terminal state will result in the\n * given {@link Subscription} being cancelled immediately.\n * <p>\n * Calling {@link #onNext(Object)}, {@link #onError(Throwable)} and {@link #onComplete()}\n * is required to be serialized (called from the same thread or called non-overlappingly from different threads\n * through external means of serialization). The {@link #toSerialized()} method available to all {@code FlowableProcessor}s\n * provides such serialization and also protects against reentrance (i.e., when a downstream {@code Subscriber}\n * consuming this processor also wants to call {@link #onNext(Object)} on this processor recursively).\n * The implementation of {@code onXXX} methods are technically thread-safe but non-serialized calls\n * to them may lead to undefined state in the currently subscribed {@code Subscriber}s.\n * <p>\n * This {@code AsyncProcessor} supports the standard state-peeking methods {@link #hasComplete()}, {@link #hasThrowable()},\n * {@link #getThrowable()} and {@link #hasSubscribers()} as well as means to read the very last observed value -\n * after this {@code AsyncProcessor} has been completed - in a non-blocking and thread-safe\n * manner via {@link #hasValue()} or {@link #getValue()}.\n * <dl>\n *  <dt><b>Backpressure:</b></dt>\n *  <dd>The {@code AsyncProcessor} honors the backpressure of the downstream {@code Subscriber}s and won't emit\n *  its single value to a particular {@code Subscriber} until that {@code Subscriber} has requested an item.\n *  When the {@code AsyncProcessor} is subscribed to a {@link io.reactivex.rxjava4.core.Flowable}, the processor consumes this\n *  {@code Flowable} in an unbounded manner (requesting {@link Long#MAX_VALUE}) as only the very last upstream item is\n *  retained by it.\n *  </dd>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>{@code AsyncProcessor} does not operate by default on a particular {@link io.reactivex.rxjava4.core.Scheduler} and\n *  the {@code Subscriber}s get notified on the thread where the terminating {@code onError} or {@code onComplete}\n *  methods were invoked.</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>When the {@link #onError(Throwable)} is called, the {@code AsyncProcessor} enters into a terminal state\n *  and emits the same {@code Throwable} instance to the last set of {@code Subscriber}s. During this emission,\n *  if one or more {@code Subscriber}s dispose their respective {@code Subscription}s, the\n *  {@code Throwable} is delivered to the global error handler via\n *  {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} (multiple times if multiple {@code Subscriber}s\n *  cancel at once).\n *  If there were no {@code Subscriber}s subscribed to this {@code AsyncProcessor} when the {@code onError()}\n *  was called, the global error handler is not invoked.\n *  </dd>\n * </dl>\n * <p>\n * Example usage:\n * <pre><code>\n * AsyncProcessor&lt;Object&gt; processor = AsyncProcessor.create();\n * \n * TestSubscriber&lt;Object&gt; ts1 = processor.test();\n *\n * ts1.assertEmpty();\n *\n * processor.onNext(1);\n *\n * // AsyncProcessor only emits when onComplete was called.\n * ts1.assertEmpty();\n *\n * processor.onNext(2);\n * processor.onComplete();\n *\n * // onComplete triggers the emission of the last cached item and the onComplete event.\n * ts1.assertResult(2);\n *\n * TestSubscriber&lt;Object&gt; ts2 = processor.test();\n *\n * // late Subscribers receive the last cached item too\n * ts2.assertResult(2);\n * </code></pre>\n * @param <T> the value type\n */",
            "/**\n"
            " * 异步处理器：onComplete 时向 Subscriber 发射最后一个 onNext 值，\n"
            " * 或 onError 时发射错误；晚订阅者也能收到缓存的最后一项。\n"
            " *\n"
            " * <p>通过 {@link #create()} 创建；onNext/onError 禁止 null。\n"
            " * <p>内部只保留最新一项，onComplete 前不向下游发射；不适合无限流。\n"
            " * <p>onXXX 须串行调用，可用 {@link #toSerialized()}；作为上游时 request(Long.MAX_VALUE)。\n"
            " * <p>支持 hasComplete/hasThrowable/getValue 等状态查询。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    /** Write before updating subscribers, read after reading subscribers as TERMINATED. */",
            "    /** 在 subscribers 置 TERMINATED 前写入，读后可见。 */",
        ),
        (
            "    /**\n     * Creates a new AsyncProcessor.\n     * @param <T> the value type to be received and emitted\n     * @return the new AsyncProcessor instance\n     */",
            "    /** 创建空的 AsyncProcessor。 */",
        ),
        (
            "    /**\n     * Constructs an AsyncProcessor.\n     * @since 2.0\n     */",
            "    /** 包内构造：subscribers 初始为 EMPTY。 */",
        ),
        (
            "    @Override\n    public void onSubscribe(@NonNull Subscription s) {",
            "    /** 已终止则 cancel；否则无界 request(MAX_VALUE)。 */\n"
            "    @Override\n    public void onSubscribe(@NonNull Subscription s) {",
        ),
        (
            "    @Override\n    public void onNext(@NonNull T t) {",
            "    /** 非终止时覆盖缓存 value（不立即发射）。 */\n"
            "    @Override\n    public void onNext(@NonNull T t) {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    @Override\n    public void onError(@NonNull Throwable t) {",
            "    /** 清空 value、置 error，向所有订阅者 onError；已终止则 RxJavaPlugins.onError。 */\n"
            "    @SuppressWarnings(\"unchecked\")\n    @Override\n    public void onError(@NonNull Throwable t) {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    @Override\n    public void onComplete() {",
            "    /** 向订阅者 complete(最后一项) 或 onComplete（无值时）。 */\n"
            "    @SuppressWarnings(\"unchecked\")\n    @Override\n    public void onComplete() {",
        ),
        (
            "    @Override\n    protected void subscribeActual(@NonNull Subscriber<? super T> s) {",
            "    /** 注册 AsyncSubscription；已终止则 replay 错误或最后一项。 */\n"
            "    @Override\n    protected void subscribeActual(@NonNull Subscriber<? super T> s) {",
        ),
        (
            "    /**\n     * Tries to add the given subscriber to the subscribers array atomically\n     * or returns false if the processor has terminated.\n     * @param ps the subscriber to add\n     * @return true if successful, false if the processor has terminated\n     */",
            "    /** CAS 扩展 subscribers 数组；已 TERMINATED 则 false。 */",
        ),
        (
            "    /**\n     * Atomically removes the given subscriber if it is subscribed to this processor.\n     * @param ps the subscriber's subscription wrapper to remove\n     */",
            "    /** CAS 从 subscribers 移除指定订阅包装。 */",
        ),
        (
            "    /**\n     * Returns true if this processor has any value.\n     * <p>The method is thread-safe.\n     * @return true if this processor has any value\n     */",
            "    /** 已终止且 value 非 null 时 true。 */",
        ),
        (
            "    /**\n     * Returns a single value this processor currently has or null if no such value exists.\n     * <p>The method is thread-safe.\n     * @return a single value this processor currently has or null if no such value exists\n     */",
            "    /** 终止后返回缓存的最后一项，否则 null。 */",
        ),
    ],
    "DispatchStreamProcessor.java": [
        (
            "/**\n * Signals the various {@link #next(Object)} and {@link #finish(Throwable)} events to one or more\n * downstream {@link Streamer}s.\n * <p>\n * This is equivalent with to a {@link PublishProcessor} or {@link MulticastProcessor}, adapted to\n * the {@link Streamable} world.\n * @param <T> the element type of the input and output values\n * @since 4.0.0\n */",
            "/**\n"
            " * Streamable 版多播处理器：将 {@link #next(Object)} 与 {@link #finish(Throwable)}\n"
            " * 分发给多个 {@link Streamer}，语义类似 {@link PublishProcessor}。\n"
            " *\n"
            " * @param <T> 输入输出元素类型\n"
            " * @since 4.0.0\n"
            " */",
        ),
        (
            "    @Override\n    public @NonNull Streamer<@NonNull T> stream(@NonNull StreamerCancellation cancellation) {",
            "    /** 注册 DispatchStreamer；已终止则返回 failed/empty Streamer。 */\n"
            "    @Override\n    public @NonNull Streamer<@NonNull T> stream(@NonNull StreamerCancellation cancellation) {",
        ),
        (
            "    @Override\n    public CompletionStage<Boolean> next(@NonNull T item) {",
            "    /** 向所有 streamer 并发 send，awaitAllBoolean 合并结果。 */\n"
            "    @Override\n    public CompletionStage<Boolean> next(@NonNull T item) {",
        ),
        (
            "    @Override\n    public CompletionStage<Void> finish(@Nullable Throwable throwable) {",
            "    /** 置 terminalEvent，向所有 streamer error/finish 并清空列表。 */\n"
            "    @Override\n    public CompletionStage<Void> finish(@Nullable Throwable throwable) {",
        ),
        (
            "    boolean add(DispatchStreamer<T> streamer) {",
            "    /** CAS 追加 streamer；已 TERMINATED 则 false。 */\n"
            "    boolean add(DispatchStreamer<T> streamer) {",
        ),
        (
            "    boolean remove(DispatchStreamer<T> streamer) {",
            "    /** CAS 从 streamers 数组移除。 */\n"
            "    boolean remove(DispatchStreamer<T> streamer) {",
        ),
        (
            "    @Override\n    public boolean hasStreamers() {",
            "    /** 是否有活跃 streamer。 */\n"
            "    @Override\n    public boolean hasStreamers() {",
        ),
        (
            "    @Override\n    public int streamerCount() {",
            "    /** 当前 streamer 数量。 */\n"
            "    @Override\n    public int streamerCount() {",
        ),
        (
            "    @Override\n    public boolean hasComplete() {",
            "    /** terminalEvent 为 TERMINATED 哨兵。 */\n"
            "    @Override\n    public boolean hasComplete() {",
        ),
        (
            "    @Override\n    public boolean hasThrowable() {",
            "    /** terminalEvent 为非 TERMINATED 的 Throwable。 */\n"
            "    @Override\n    public boolean hasThrowable() {",
        ),
        (
            "    @Override\n    public @Nullable Throwable getThrowable() {",
            "    /** 返回终止错误，正常完成则 null。 */\n"
            "    @Override\n    public @Nullable Throwable getThrowable() {",
        ),
        (
            "        @Override\n        public @NonNull CompletionStage<Boolean> next() {",
            "        /** consumerReady 就绪后 await producerReady 取下一项。 */\n"
            "        @Override\n        public @NonNull CompletionStage<Boolean> next() {",
        ),
        (
            "        @Override\n        public @NonNull T current() {",
            "        /** 返回最近一次 apply 写入的 current。 */\n"
            "        @Override\n        public @NonNull T current() {",
        ),
        (
            "        @Override\n        public @NonNull CompletionStage<Void> finish() {",
            "        /** 移除自身并完成 consumerReady(false)。 */\n"
            "        @Override\n        public @NonNull CompletionStage<Void> finish() {",
        ),
        (
            "        CompletionStage<Boolean> send(T item) {",
            "        /** 等待 consumer 就绪后设置 incoming 并 resume producer。 */\n"
            "        CompletionStage<Boolean> send(T item) {",
        ),
        (
            "        CompletionStage<Void> error(Throwable t) {",
            "        /** finish 时 completeExceptionally 或 complete(false) producerReady。 */\n"
            "        CompletionStage<Void> error(Throwable t) {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 移除并以 CancellationException 结束 producerReady。 */\n"
            "        @Override\n        public void dispose() {",
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
    reps = FILE_REPLACEMENTS.get(rel) or FILE_REPLACEMENTS.get(name, [])
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
    index_file = Path("/tmp/git-index-rxjava-w27b")
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
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "HEAD"], check=True)
    return commit, tree_count


def push_main(retries: int = 4) -> None:
    r = subprocess.CompletedProcess([], 1)
    for attempt in range(retries):
        subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
        local = subprocess.check_output(
            ["git", "-C", str(ROOT), "rev-parse", "main"], text=True
        ).strip()
        remote = subprocess.check_output(
            ["git", "-C", str(ROOT), "rev-parse", "origin/main"], text=True
        ).strip()
        if local != remote:
            merge_base = subprocess.check_output(
                ["git", "-C", str(ROOT), "merge-base", local, remote], text=True
            ).strip()
            if merge_base != remote:
                raise RuntimeError(
                    f"main diverged from origin/main (local={local[:8]} remote={remote[:8]})"
                )
        r = subprocess.run(
            ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            return
        if attempt + 1 < retries:
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
    if len(BATCH_FILES) != 15:
        raise RuntimeError(f"batch guard failed: expected 15 files, got {len(BATCH_FILES)}")

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "checkout", "-f", "main"], check=True)
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
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 27b [15:30]",
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
        "queue: mark rxjava 4.0.0-alpha-21 wave27b done",
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
    verified = sum(chinese.values())
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
                "verified": f"{verified}/15",
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(chinese.values()) and verified == 15 else 1


if __name__ == "__main__":
    raise SystemExit(main())
