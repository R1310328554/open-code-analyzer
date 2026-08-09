#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-11b Flowable operators [15:30]."""
from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
WAVE11B_FILE = Path("/tmp/rxjava_w11b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE11B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FlowableOnErrorNext.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableOnErrorNext",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 上游 onError 时，根据 {@link Function} 切换到备用 {@link Publisher} 继续发射。\n"
            " * 使用 {@link SubscriptionArbiter} 协调主序列与备用序列的背压。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableOnErrorNext",
        ),
        (
            "    public FlowableOnErrorNext(Flowable<T> source,\n            Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param nextSupplier 接收错误并返回备用 Publisher 的函数\n"
            "     */\n"
            "    public FlowableOnErrorNext(Flowable<T> source,\n            Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 安装 OnErrorNextSubscriber 并订阅上游。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class OnErrorNextSubscriber<T>\n    extends SubscriptionArbiter\n    implements FlowableSubscriber<T> {",
            "    /** 首次 onError 时切换备用 Publisher；后续错误直接传播。 */\n"
            "    static final class OnErrorNextSubscriber<T>\n    extends SubscriptionArbiter\n    implements FlowableSubscriber<T> {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 调用 nextSupplier 订阅备用流；supplier 异常则 CompositeException。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
    ],
    "FlowableOnErrorReturn.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableOnErrorReturn",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 上游 onError 时，用 {@link Function} 计算替代值发射后 onComplete，\n"
            " * 而非向下游传播错误。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableOnErrorReturn",
        ),
        (
            "    public FlowableOnErrorReturn(Flowable<T> source, Function<? super Throwable, ? extends T> valueSupplier) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param valueSupplier 根据错误计算替代值的函数\n"
            "     */\n"
            "    public FlowableOnErrorReturn(Flowable<T> source, Function<? super Throwable, ? extends T> valueSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅 OnErrorReturnSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class OnErrorReturnSubscriber<T>\n    extends SinglePostCompleteSubscriber<T, T> {",
            "    /** onError 时发射 valueSupplier 返回值并完成。 */\n"
            "    static final class OnErrorReturnSubscriber<T>\n    extends SinglePostCompleteSubscriber<T, T> {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 调用 valueSupplier；成功则 complete(v)，失败则 CompositeException。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
    ],
    "FlowableRange.java": [
        (
            "/**\n * Emits a range of integer values.\n */",
            "/**\n * 按背压发射从 start 起连续 count 个 int 值（含 start，不含 start+count）。\n"
            " * 支持 {@link ConditionalSubscriber} 融合路径。\n"
            " */",
        ),
        (
            "    public FlowableRange(int start, int count) {",
            "    /**\n"
            "     * @param start 起始值（含）\n"
            "     * @param count 发射元素个数\n"
            "     */\n"
            "    public FlowableRange(int start, int count) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super Integer> s) {",
            "    /** 按下游类型选择 RangeSubscription 或 RangeConditionalSubscription。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super Integer> s) {",
        ),
        (
            "    abstract static class BaseRangeSubscription extends BasicQueueSubscription<Integer> {",
            "    /** 同步融合队列 Subscription，实现 poll/request 背压逻辑。 */\n"
            "    abstract static class BaseRangeSubscription extends BasicQueueSubscription<Integer> {",
        ),
        (
            "        abstract void fastPath();\n\n        abstract void slowPath(long r);",
            "        /** 无背压限制时一次性发射剩余元素。 */\n"
            "        abstract void fastPath();\n\n"
            "        /** 按请求量 r 逐批发射。 */\n"
            "        abstract void slowPath(long r);",
        ),
        (
            "    static final class RangeSubscription extends BaseRangeSubscription {",
            "    /** 标准 Subscriber 的 range 发射实现。 */\n"
            "    static final class RangeSubscription extends BaseRangeSubscription {",
        ),
        (
            "    static final class RangeConditionalSubscription extends BaseRangeSubscription {",
            "    /** {@link ConditionalSubscriber} 的 range 发射实现。 */\n"
            "    static final class RangeConditionalSubscription extends BaseRangeSubscription {",
        ),
    ],
    "FlowableRangeLong.java": [
        (
            "/**\n * Emits a range of long values.\n */",
            "/**\n * 按背压发射从 start 起连续 count 个 long 值（含 start，不含 start+count）。\n"
            " * 支持 {@link ConditionalSubscriber} 融合路径。\n"
            " */",
        ),
        (
            "    public FlowableRangeLong(long start, long count) {",
            "    /**\n"
            "     * @param start 起始值（含）\n"
            "     * @param count 发射元素个数\n"
            "     */\n"
            "    public FlowableRangeLong(long start, long count) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super Long> s) {",
            "    /** 按下游类型选择 RangeSubscription 或 RangeConditionalSubscription。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super Long> s) {",
        ),
        (
            "    abstract static class BaseRangeSubscription extends BasicQueueSubscription<Long> {",
            "    /** 同步融合队列 Subscription，实现 poll/request 背压逻辑。 */\n"
            "    abstract static class BaseRangeSubscription extends BasicQueueSubscription<Long> {",
        ),
        (
            "        abstract void fastPath();\n\n        abstract void slowPath(long r);",
            "        /** 无背压限制时一次性发射剩余元素。 */\n"
            "        abstract void fastPath();\n\n"
            "        /** 按请求量 r 逐批发射。 */\n"
            "        abstract void slowPath(long r);",
        ),
        (
            "    static final class RangeSubscription extends BaseRangeSubscription {",
            "    /** 标准 Subscriber 的 long range 发射实现。 */\n"
            "    static final class RangeSubscription extends BaseRangeSubscription {",
        ),
        (
            "    static final class RangeConditionalSubscription extends BaseRangeSubscription {",
            "    /** {@link ConditionalSubscriber} 的 long range 发射实现。 */\n"
            "    static final class RangeConditionalSubscription extends BaseRangeSubscription {",
        ),
    ],
    "FlowableReduce.java": [
        (
            "/**\n * Reduces a sequence via a function into a single value or signals NoSuchElementException for\n * an empty source.\n *\n * @param <T> the value type\n */",
            "/**\n * 用 {@link BiFunction} 将序列归约为单个值；空源 onComplete 时不发射值。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public FlowableReduce(Flowable<T> source, BiFunction<T, T, T> reducer) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param reducer 两两归约函数（返回值不可为 null）\n"
            "     */\n"
            "    public FlowableReduce(Flowable<T> source, BiFunction<T, T, T> reducer) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅 ReduceSubscriber 并请求 Long.MAX_VALUE。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class ReduceSubscriber<T> extends DeferredScalarSubscription<T> implements FlowableSubscriber<T> {",
            "    /** 累积归约结果，onComplete 时发射最终值。 */\n"
            "    static final class ReduceSubscriber<T> extends DeferredScalarSubscription<T> implements FlowableSubscriber<T> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 首元素缓存；后续元素与 accumulator 归约。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 有累积值则 complete(v)，否则直接 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableReduceMaybe.java": [
        (
            "/**\n * Reduce a Flowable into a single value exposed as Single or signal NoSuchElementException.\n *\n * @param <T> the value type\n */",
            "/**\n * 将 {@link Flowable} 归约为单个值并以 {@link Maybe} 暴露；\n"
            " * 空源 onComplete，有值 onSuccess。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public FlowableReduceMaybe(Flowable<T> source, BiFunction<T, T, T> reducer) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param reducer 两两归约函数\n"
            "     */\n"
            "    public FlowableReduceMaybe(Flowable<T> source, BiFunction<T, T, T> reducer) {",
        ),
        (
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableReduce}。 */\n"
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
        ),
        (
            "    static final class ReduceSubscriber<T> implements FlowableSubscriber<T>, Disposable {",
            "    /** 归约订阅者，实现 {@link Disposable} 以支持取消。 */\n"
            "    static final class ReduceSubscriber<T> implements FlowableSubscriber<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 有累积值 onSuccess，否则 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "//                value = null;",
            "//                value = null; // 保留注释占位",
        ),
    ],
    "FlowableReduceSeedSingle.java": [
        (
            "/**\n * Reduce a sequence of values, starting from a seed value and by using\n * an accumulator function and return the last accumulated value.\n *\n * @param <T> the source value type\n * @param <R> the accumulated result type\n */",
            "/**\n"
            " * 从 seed 起始，用累加器 {@link BiFunction} 归约序列为最终值并以 {@link Single} 发射。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <R> 累加结果类型\n"
            " */",
        ),
        (
            "    public FlowableReduceSeedSingle(Publisher<T> source, R seed, BiFunction<R, ? super T, R> reducer) {",
            "    /**\n"
            "     * @param source 上游 Publisher\n"
            "     * @param seed 初始累加值\n"
            "     * @param reducer 累加器函数\n"
            "     */\n"
            "    public FlowableReduceSeedSingle(Publisher<T> source, R seed, BiFunction<R, ? super T, R> reducer) {",
        ),
        (
            "    static final class ReduceSeedObserver<T, R> implements FlowableSubscriber<T>, Disposable {",
            "    /** 持 seed 累加，onComplete 时 onSuccess 最终值。 */\n"
            "    static final class ReduceSeedObserver<T, R> implements FlowableSubscriber<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T value) {",
            "        /** 将当前元素累入 value 字段。 */\n"
            "        @Override\n        public void onNext(T value) {",
        ),
    ],
    "FlowableReduceWithSingle.java": [
        (
            "/**\n * Reduce a sequence of values, starting from a generated seed value and by using\n * an accumulator function and return the last accumulated value.\n *\n * @param <T> the source value type\n * @param <R> the accumulated result type\n */",
            "/**\n"
            " * 订阅时由 {@link Supplier} 生成 seed，再用累加器归约并以 {@link Single} 发射。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <R> 累加结果类型\n"
            " */",
        ),
        (
            "    public FlowableReduceWithSingle(Publisher<T> source, Supplier<R> seedSupplier, BiFunction<R, ? super T, R> reducer) {",
            "    /**\n"
            "     * @param source 上游 Publisher\n"
            "     * @param seedSupplier 初始累加值 Supplier\n"
            "     * @param reducer 累加器函数\n"
            "     */\n"
            "    public FlowableReduceWithSingle(Publisher<T> source, Supplier<R> seedSupplier, BiFunction<R, ? super T, R> reducer) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
            "    /** 获取 seed 后委托 {@link ReduceSeedObserver} 订阅上游。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
        ),
    ],
    "FlowableRefCount.java": [
        (
            "/**\n * Returns an observable sequence that stays connected to the source as long as\n * there is at least one subscription to the observable sequence.\n *\n * @param <T>\n *            the value type\n */",
            "/**\n"
            " * 将 {@link ConnectableFlowable} 转为普通 {@link Flowable}：\n"
            " * 只要有订阅者就保持连接，订阅数归零后断开（可配置延迟）。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public FlowableRefCount(ConnectableFlowable<T> source) {",
            "    /** @param source 待 refCount 的 ConnectableFlowable */\n"
            "    public FlowableRefCount(ConnectableFlowable<T> source) {",
        ),
        (
            "    public FlowableRefCount(ConnectableFlowable<T> source, int n, long timeout, TimeUnit unit,\n            Scheduler scheduler) {",
            "    /**\n"
            "     * @param source ConnectableFlowable 源\n"
            "     * @param n 达到该订阅数时触发 connect\n"
            "     * @param timeout 末位订阅者取消后的断开延迟\n"
            "     * @param unit timeout 时间单位\n"
            "     * @param scheduler 调度 timeout 的 Scheduler\n"
            "     */\n"
            "    public FlowableRefCount(ConnectableFlowable<T> source, int n, long timeout, TimeUnit unit,\n            Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 维护 RefConnection 订阅计数，必要时 connect 上游。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    void cancel(RefConnection rc) {",
            "    /** 订阅者取消时递减计数；归零后调度或立即 timeout 断开。 */\n"
            "    void cancel(RefConnection rc) {",
        ),
        (
            "    void terminated(RefConnection rc) {",
            "    /** 上游终止时清理 timer 并在计数归零时 reset 源。 */\n"
            "    void terminated(RefConnection rc) {",
        ),
        (
            "    void timeout(RefConnection rc) {",
            "    /** 延迟到期且无订阅者时 reset ConnectableFlowable。 */\n"
            "    void timeout(RefConnection rc) {",
        ),
        (
            "    static final class RefConnection extends AtomicReference<Disposable>\n    implements Runnable, Consumer<Disposable> {",
            "    /** 共享连接状态：订阅计数、connect 标志与断开 timer。 */\n"
            "    static final class RefConnection extends AtomicReference<Disposable>\n    implements Runnable, Consumer<Disposable> {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** timer 到期回调，触发 parent.timeout。 */\n"
            "        @Override\n        public void run() {",
        ),
        (
            "        @Override\n        public void accept(Disposable t) {",
            "        /** connect 回调：保存 Disposable；若已 early disconnect 则 reset。 */\n"
            "        @Override\n        public void accept(Disposable t) {",
        ),
        (
            "    static final class RefCountSubscriber<T>\n    extends AtomicBoolean implements FlowableSubscriber<T>, Subscription {",
            "    /** 包装下游 Subscription，cancel/终止时通知 parent 更新连接状态。 */\n"
            "    static final class RefCountSubscriber<T>\n    extends AtomicBoolean implements FlowableSubscriber<T>, Subscription {",
        ),
    ],
    "FlowableRepeat.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\npublic final class FlowableRepeat",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\n"
            "/**\n"
            " * 上游 onComplete 后重新订阅，共重复 count 次（含首次订阅）。\n"
            " * count 为 {@code Long.MAX_VALUE} 时无限重复。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableRepeat",
        ),
        (
            "    public FlowableRepeat(Flowable<T> source, long count) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param count 总订阅次数（含首次）\n"
            "     */\n"
            "    public FlowableRepeat(Flowable<T> source, long count) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 安装 SubscriptionArbiter 并启动 RepeatSubscriber。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class RepeatSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {",
            "    /** onComplete 时若 remaining>0 则 trampolining 重新订阅。 */\n"
            "    static final class RepeatSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {",
        ),
        (
            "        /**\n         * Subscribes to the source again via trampolining.\n         */",
            "        /** 通过 trampolining 再次订阅上游。 */",
        ),
    ],
    "FlowableRepeatUntil.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\npublic final class FlowableRepeatUntil",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\n"
            "/**\n"
            " * 上游 onComplete 后若 {@link BooleanSupplier} 为 false 则重新订阅；\n"
            " * 为 true 时向下游 onComplete。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableRepeatUntil",
        ),
        (
            "    public FlowableRepeatUntil(Flowable<T> source, BooleanSupplier until) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param until 每次 onComplete 后判断是否停止重复\n"
            "     */\n"
            "    public FlowableRepeatUntil(Flowable<T> source, BooleanSupplier until) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 安装 SubscriptionArbiter 并启动 RepeatSubscriber。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class RepeatSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {",
            "    /** onComplete 时评估 stop；false 则重新订阅。 */\n"
            "    static final class RepeatSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {",
        ),
        (
            "        /**\n         * Subscribes to the source again via trampolining.\n         */",
            "        /** 通过 trampolining 再次订阅上游。 */",
        ),
    ],
    "FlowableRepeatWhen.java": [
        (
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\npublic final class FlowableRepeatWhen",
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\n"
            "/**\n"
            " * 上游 onComplete 时向 handler 提供的 {@link FlowableProcessor} 发信号，\n"
            " * 由 handler 返回的 Publisher 决定是否重新订阅。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableRepeatWhen",
        ),
        (
            "    public FlowableRepeatWhen(Flowable<T> source,\n            Function<? super Flowable<Object>, ? extends Publisher<?>> handler) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param handler 接收重复信号流并返回控制 Publisher\n"
            "     */\n"
            "    public FlowableRepeatWhen(Flowable<T> source,\n            Function<? super Flowable<Object>, ? extends Publisher<?>> handler) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 创建 processor/handler 链并触发首次订阅。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class WhenReceiver<T, U>\n    extends AtomicInteger\n    implements FlowableSubscriber<Object>, Subscription {",
            "    /** 接收 handler Publisher 信号并 trampolining 重新订阅 source。 */\n"
            "    static final class WhenReceiver<T, U>\n    extends AtomicInteger\n    implements FlowableSubscriber<Object>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(Object t) {",
            "        /** 收到重复信号时重新订阅 source。 */\n"
            "        @Override\n        public void onNext(Object t) {",
        ),
        (
            "    abstract static class WhenSourceSubscriber<T, U> extends SubscriptionArbiter implements FlowableSubscriber<T> {",
            "    /** 主序列 subscriber：转发 onNext，again 时重置 Subscription 并通知 processor。 */\n"
            "    abstract static class WhenSourceSubscriber<T, U> extends SubscriptionArbiter implements FlowableSubscriber<T> {",
        ),
        (
            "        protected final void again(U signal) {",
            "        /** 重置上游 Subscription、上报 produced 并向 processor 发重复信号。 */\n"
            "        protected final void again(U signal) {",
        ),
        (
            "    static final class RepeatWhenSubscriber<T> extends WhenSourceSubscriber<T, Object> {",
            "    /** onComplete 触发 again(0) 请求重复。 */\n"
            "    static final class RepeatWhenSubscriber<T> extends WhenSourceSubscriber<T, Object> {",
        ),
    ],
    "FlowableRetryBiPredicate.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\npublic final class FlowableRetryBiPredicate",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\n"
            "/**\n"
            " * 上游 onError 时由 {@link BiPredicate} 根据重试次数与错误决定是否重新订阅。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableRetryBiPredicate",
        ),
        (
            "    public FlowableRetryBiPredicate(\n            Flowable<T> source,\n            BiPredicate<? super Integer, ? super Throwable> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param predicate (重试次数, 错误) -> 是否继续重试\n"
            "     */\n"
            "    public FlowableRetryBiPredicate(\n            Flowable<T> source,\n            BiPredicate<? super Integer, ? super Throwable> predicate) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 安装 SubscriptionArbiter 并启动 RetryBiSubscriber。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class RetryBiSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {",
            "    /** predicate 返回 true 时 trampolining 重新订阅。 */\n"
            "    static final class RetryBiSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {",
        ),
        (
            "        /**\n         * Subscribes to the source again via trampolining.\n         */",
            "        /** 通过 trampolining 再次订阅上游。 */",
        ),
    ],
    "FlowableRetryPredicate.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\npublic final class FlowableRetryPredicate",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\n"
            "/**\n"
            " * 上游 onError 时在剩余次数内且 {@link Predicate} 为 true 则重新订阅。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableRetryPredicate",
        ),
        (
            "    public FlowableRetryPredicate(Flowable<T> source,\n            long count,\n            Predicate<? super Throwable> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param count 最大重试次数\n"
            "     * @param predicate 按错误判断是否重试\n"
            "     */\n"
            "    public FlowableRetryPredicate(Flowable<T> source,\n            long count,\n            Predicate<? super Throwable> predicate) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 安装 SubscriptionArbiter 并启动 RetrySubscriber。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class RetrySubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {",
            "    /** remaining 递减；predicate 通过则重新订阅。 */\n"
            "    static final class RetrySubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {",
        ),
        (
            "        /**\n         * Subscribes to the source again via trampolining.\n         */",
            "        /** 通过 trampolining 再次订阅上游。 */",
        ),
    ],
    "FlowableRetryWhen.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableRetryWhen",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 上游 onError 时将错误推入 handler 的 {@link FlowableProcessor}，\n"
            " * 由 handler 返回的 Publisher 决定是否重新订阅。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableRetryWhen",
        ),
        (
            "    public FlowableRetryWhen(Flowable<T> source,\n            Function<? super Flowable<Throwable>, ? extends Publisher<?>> handler) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param handler 接收错误流并返回控制 Publisher\n"
            "     */\n"
            "    public FlowableRetryWhen(Flowable<T> source,\n            Function<? super Flowable<Throwable>, ? extends Publisher<?>> handler) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 复用 {@link FlowableRepeatWhen} 的 WhenReceiver/WhenSourceSubscriber 机制。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class RetryWhenSubscriber<T> extends WhenSourceSubscriber<T, Throwable> {",
            "    /** onError 时 again(t) 请求重试；onComplete 正常结束。 */\n"
            "    static final class RetryWhenSubscriber<T> extends WhenSourceSubscriber<T, Throwable> {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 将错误作为重复信号交给 handler 链。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def mark_queue_done(files: list[str]) -> None:
    subprocess.run(
        [
            sys.executable,
            str(ROOT / "scripts/mark_batch_done.py"),
            "--project",
            "rxjava",
            "--version",
            "4.0.0-alpha-21",
            "--note",
            "wave11b Flowable* [15:30]",
            *files,
        ],
        check=True,
    )
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    file_set = set(files)
    batch["files"] = [f for f in batch.get("files", []) if f not in file_set]
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        try:
            text = dst.read_text(encoding="utf-8")
            text = apply_replacements(text, reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            if cn < 10 or not lic:
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if ok == len(BATCH_FILES) and not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {ok} files done in queue (note=wave11b)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
