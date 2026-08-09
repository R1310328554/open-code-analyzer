#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-13b Maybe operators [0:15]."""
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
WAVE13B_FILE = Path("/tmp/rxjava_w13b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE13B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "MaybeConcatArray.java": [
        (
            "/**\n * Concatenate values of each MaybeSource provided in an array.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 依次串联数组中每个 {@link MaybeSource} 发射的值，\n"
            " * 以 {@link Flowable} 形式向下游输出并支持背压。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeConcatArray(MaybeSource<? extends T>[] sources) {",
            "    /** @param sources Maybe 源数组 */\n"
            "    public MaybeConcatArray(MaybeSource<? extends T>[] sources) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 创建 ConcatMaybeObserver 并启动 drain 串联。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class ConcatMaybeObserver<T>\n    extends AtomicInteger\n    implements MaybeObserver<T>, Subscription {",
            "    /** 逐个订阅 Maybe 源，将 onSuccess 值经背压检查后转发为 onNext。 */\n"
            "    static final class ConcatMaybeObserver<T>\n    extends AtomicInteger\n    implements MaybeObserver<T>, Subscription {",
        ),
        (
            "            this.current = new AtomicReference<>(NotificationLite.COMPLETE); // as if a previous completed",
            "            this.current = new AtomicReference<>(NotificationLite.COMPLETE); // 模拟前一个 Maybe 已完成",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 缓存成功值并触发 drain。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @SuppressWarnings(\"unchecked\")\n        void drain() {",
            "        /** 背压感知地发射缓存值并订阅下一个 Maybe 源。 */\n"
            "        @SuppressWarnings(\"unchecked\")\n        void drain() {",
        ),
    ],
    "MaybeConcatArrayDelayError.java": [
        (
            "/**\n * Concatenate values of each MaybeSource provided in an array and delays\n * any errors till the very end.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 依次串联数组中每个 {@link MaybeSource} 的值，\n"
            " * 并将错误延迟到全部源处理完毕后再终止。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeConcatArrayDelayError(MaybeSource<? extends T>[] sources) {",
            "    /** @param sources Maybe 源数组 */\n"
            "    public MaybeConcatArrayDelayError(MaybeSource<? extends T>[] sources) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 创建带 AtomicThrowable 的 ConcatMaybeObserver 并启动 drain。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class ConcatMaybeObserver<T>\n    extends AtomicInteger\n    implements MaybeObserver<T>, Subscription {",
            "    /** 延迟错误版串联：onError 收集至 errors，全部完成后统一终止。 */\n"
            "    static final class ConcatMaybeObserver<T>\n    extends AtomicInteger\n    implements MaybeObserver<T>, Subscription {",
        ),
        (
            "            this.current = new AtomicReference<>(NotificationLite.COMPLETE); // as if a previous completed",
            "            this.current = new AtomicReference<>(NotificationLite.COMPLETE); // 模拟前一个 Maybe 已完成",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 记录错误并继续 drain 下一个源。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        @SuppressWarnings(\"unchecked\")\n        void drain() {",
            "        /** 发射缓存值；全部源结束后通过 errors 终止下游。 */\n"
            "        @SuppressWarnings(\"unchecked\")\n        void drain() {",
        ),
    ],
    "MaybeConcatIterable.java": [
        (
            "/**\n * Concatenate values of each MaybeSource provided by an Iterable.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 依次串联 {@link Iterable} 提供的每个 {@link MaybeSource} 发射的值，\n"
            " * 以 {@link Flowable} 形式向下游输出并支持背压。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeConcatIterable(Iterable<? extends MaybeSource<? extends T>> sources) {",
            "    /** @param sources Maybe 源 Iterable */\n"
            "    public MaybeConcatIterable(Iterable<? extends MaybeSource<? extends T>> sources) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 获取 Iterator 后创建 ConcatMaybeObserver 并启动 drain。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class ConcatMaybeObserver<T>\n    extends AtomicInteger\n    implements MaybeObserver<T>, Subscription {",
            "    /** 基于 Iterator 逐个订阅 Maybe 源并背压转发 onSuccess 值。 */\n"
            "    static final class ConcatMaybeObserver<T>\n    extends AtomicInteger\n    implements MaybeObserver<T>, Subscription {",
        ),
        (
            "            this.current = new AtomicReference<>(NotificationLite.COMPLETE); // as if a previous completed",
            "            this.current = new AtomicReference<>(NotificationLite.COMPLETE); // 模拟前一个 Maybe 已完成",
        ),
        (
            "        @SuppressWarnings(\"unchecked\")\n        void drain() {",
            "        /** 背压感知地发射缓存值并迭代订阅下一个 Maybe 源。 */\n"
            "        @SuppressWarnings(\"unchecked\")\n        void drain() {",
        ),
    ],
    "MaybeContains.java": [
        (
            "/**\n * Signals true if the source signals a value that is object-equals with the provided\n * value, false otherwise or for empty sources.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 若上游发射的值与给定值 {@link Objects#equals} 相等则发射 true，\n"
            " * 否则或上游为空时发射 false。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeContains(MaybeSource<T> source, Object value) {",
            "    /**\n"
            "     * @param source 上游 Maybe\n"
            "     * @param value 待比较的目标值\n"
            "     */\n"
            "    public MaybeContains(MaybeSource<T> source, Object value) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> observer) {",
            "    /** 订阅上游并在 onSuccess 时比较 equals。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> observer) {",
        ),
        (
            "    static final class ContainsMaybeObserver implements MaybeObserver<Object>, Disposable {",
            "    /** onSuccess 比较 equals；onComplete 发射 false。 */\n"
            "    static final class ContainsMaybeObserver implements MaybeObserver<Object>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(Object value) {",
            "        /** 比较上游值与目标值并发射 Boolean 结果。 */\n"
            "        @Override\n        public void onSuccess(Object value) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 空源视为未包含，发射 false。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "MaybeCount.java": [
        (
            "/**\n * Signals 1L if the source signaled an item or 0L if the source is empty.\n *\n * @param <T> the source value type\n */",
            "/**\n"
            " * 上游有值时发射 1L，空源（onComplete）时发射 0L。\n"
            " *\n * @param <T> 上游元素类型\n"
            " */",
        ),
        (
            "    public MaybeCount(MaybeSource<T> source) {",
            "    /** @param source 上游 Maybe */\n"
            "    public MaybeCount(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Long> observer) {",
            "    /** 订阅上游并将有无值转换为 0/1 计数。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Long> observer) {",
        ),
        (
            "    static final class CountMaybeObserver implements MaybeObserver<Object>, Disposable {",
            "    /** onSuccess 发射 1L；onComplete 发射 0L。 */\n"
            "    static final class CountMaybeObserver implements MaybeObserver<Object>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(Object value) {",
            "        /** 有值，计数为 1。 */\n"
            "        @Override\n        public void onSuccess(Object value) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 空源，计数为 0。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "MaybeCreate.java": [
        (
            "/**\n * Provides an API over MaybeObserver that serializes calls to onXXX and manages cancellation\n * in a safe manner.\n *\n * @param <T> the value type emitted\n */",
            "/**\n"
            " * 为 {@link MaybeOnSubscribe} 提供 {@link MaybeEmitter} API，\n"
            " * 序列化 onXXX 调用并安全管理取消。\n"
            " *\n * @param <T> 发射的元素类型\n"
            " */",
        ),
        (
            "    public MaybeCreate(MaybeOnSubscribe<T> source) {",
            "    /** @param source 自定义订阅逻辑 */\n"
            "    public MaybeCreate(MaybeOnSubscribe<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 创建 Emitter 并调用 source.subscribe。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class Emitter<T>\n    extends AtomicReference<Disposable>\n    implements MaybeEmitter<T>, Disposable {",
            "    /** 线程安全地转发 onSuccess/onError/onComplete 并处理 dispose。 */\n"
            "    static final class Emitter<T>\n    extends AtomicReference<Disposable>\n    implements MaybeEmitter<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 校验非 dispose 后转发 onSuccess 并 dispose 关联资源。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @Override\n        public boolean tryOnError(Throwable t) {",
            "        /** 尝试转发 onError；已 dispose 时返回 false。 */\n"
            "        @Override\n        public boolean tryOnError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 校验非 dispose 后转发 onComplete 并 dispose 关联资源。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "MaybeDefer.java": [
        (
            "/**\n * Defers the creation of the actual Maybe the incoming MaybeObserver is subscribed to.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 延迟创建实际 {@link MaybeSource}，直到 {@link MaybeObserver} 订阅时才调用 Supplier。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeDefer(Supplier<? extends MaybeSource<? extends T>> maybeSupplier) {",
            "    /** @param maybeSupplier 延迟提供 MaybeSource 的 Supplier */\n"
            "    public MaybeDefer(Supplier<? extends MaybeSource<? extends T>> maybeSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 调用 Supplier 获取 MaybeSource 并订阅 observer。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
    ],
    "MaybeDelay.java": [
        (
            "/**\n * Delays all signal types by the given amount and re-emits them on the given scheduler.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将所有信号类型延迟指定时长，\n"
            " * 并在给定 {@link Scheduler} 上重新发射。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeDelay(MaybeSource<T> source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {",
            "    /**\n"
            "     * @param source 上游 Maybe\n"
            "     * @param delay 延迟时长\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度延迟任务的 Scheduler\n"
            "     * @param delayError true 时 onError 也延迟 delay 时长\n"
            "     */\n"
            "    public MaybeDelay(MaybeSource<T> source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 DelayMaybeObserver 并在 Scheduler 上调度信号。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class DelayMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable, Runnable {",
            "    /** 缓存信号后在 Scheduler 上 scheduleDirect 延迟转发。 */\n"
            "    static final class DelayMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 延迟到期后转发缓存的 onSuccess/onError/onComplete。 */\n"
            "        @Override\n        public void run() {",
        ),
        (
            "        void schedule(long delay) {",
            "        /** 在 scheduler 上 scheduleDirect 延迟执行 run。 */\n"
            "        void schedule(long delay) {",
        ),
    ],
    "MaybeDelayOtherPublisher.java": [
        (
            "/**\n * Delay the emission of the main signal until the other signals an item or completes.\n * \n * @param <T> the main value type\n * @param <U> the other value type\n */",
            "/**\n"
            " * 延迟主 {@link Maybe} 信号的发射，\n"
            " * 直到 other {@link Publisher} 发射元素或完成。\n"
            " *\n * @param <T> 主 Maybe 元素类型\n"
            " * @param <U> other Publisher 元素类型\n"
            " */",
        ),
        (
            "    public MaybeDelayOtherPublisher(MaybeSource<T> source, Publisher<U> other) {",
            "    /**\n"
            "     * @param source 主 Maybe\n"
            "     * @param other 触发转发时机的 Publisher\n"
            "     */\n"
            "    public MaybeDelayOtherPublisher(MaybeSource<T> source, Publisher<U> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 先订阅主源缓存信号，other 就绪后再转发。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class DelayMaybeObserver<T, U>\n    implements MaybeObserver<T>, Disposable {",
            "    /** 缓存主源信号后订阅 other Publisher。 */\n"
            "    static final class DelayMaybeObserver<T, U>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "    static final class OtherSubscriber<T> extends\n    AtomicReference<Subscription>\n    implements FlowableSubscriber<Object> {",
            "    /** other 首项或 onComplete 时转发缓存的主源信号。 */\n"
            "    static final class OtherSubscriber<T> extends\n    AtomicReference<Subscription>\n    implements FlowableSubscriber<Object> {",
        ),
        (
            "        @Override\n        public void onNext(Object t) {",
            "        /** 收到 other 首项即 cancel 并触发 onComplete 转发。 */\n"
            "        @Override\n        public void onNext(Object t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** other 完成时转发缓存的 onSuccess/onError/onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "MaybeDelaySubscriptionOtherPublisher.java": [
        (
            "/**\n * Delay the subscription to the main Maybe until the other signals an item or completes.\n * \n * @param <T> the main value type\n * @param <U> the other value type\n */",
            "/**\n"
            " * 延迟对主 {@link Maybe} 的订阅，\n"
            " * 直到 other {@link Publisher} 发射元素或完成。\n"
            " *\n * @param <T> 主 Maybe 元素类型\n"
            " * @param <U> other Publisher 元素类型\n"
            " */",
        ),
        (
            "    public MaybeDelaySubscriptionOtherPublisher(MaybeSource<T> source, Publisher<U> other) {",
            "    /**\n"
            "     * @param source 主 Maybe\n"
            "     * @param other 触发订阅时机的 Publisher\n"
            "     */\n"
            "    public MaybeDelaySubscriptionOtherPublisher(MaybeSource<T> source, Publisher<U> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 先订阅 other，就绪后再订阅主 Maybe。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class OtherSubscriber<T> implements FlowableSubscriber<Object>, Disposable {",
            "    /** 等待 other 首项或 onComplete 后订阅主 Maybe。 */\n"
            "    static final class OtherSubscriber<T> implements FlowableSubscriber<Object>, Disposable {",
        ),
        (
            "        void subscribeNext() {",
            "        /** other 就绪后订阅主 MaybeSource。 */\n"
            "        void subscribeNext() {",
        ),
        (
            "    static final class DelayMaybeObserver<T> extends AtomicReference<Disposable>\n    implements MaybeObserver<T> {",
            "    /** 透传主 Maybe 的 onSuccess/onError/onComplete。 */\n"
            "    static final class DelayMaybeObserver<T> extends AtomicReference<Disposable>\n    implements MaybeObserver<T> {",
        ),
    ],
    "MaybeDelayWithCompletable.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class MaybeDelayWithCompletable",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 等待 {@link CompletableSource} 完成后再订阅主 {@link MaybeSource}。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class MaybeDelayWithCompletable",
        ),
        (
            "    public MaybeDelayWithCompletable(MaybeSource<T> source, CompletableSource other) {",
            "    /**\n"
            "     * @param source 主 Maybe\n"
            "     * @param other 需先完成的 Completable\n"
            "     */\n"
            "    public MaybeDelayWithCompletable(MaybeSource<T> source, CompletableSource other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 先订阅 other Completable，onComplete 后订阅主源。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class OtherObserver<T>\n    extends AtomicReference<Disposable>\n    implements CompletableObserver, Disposable {",
            "    /** Completable 完成后订阅主 Maybe。 */\n"
            "    static final class OtherObserver<T>\n    extends AtomicReference<Disposable>\n    implements CompletableObserver, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** other 完成，订阅主 MaybeSource。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "    record DelayWithMainObserver<T>(AtomicReference<Disposable> parent,\n                                    MaybeObserver<? super T> downstream) implements MaybeObserver<T> {",
            "    /** 透传主 Maybe 信号并更新 parent 的 Disposable。 */\n"
            "    record DelayWithMainObserver<T>(AtomicReference<Disposable> parent,\n                                    MaybeObserver<? super T> downstream) implements MaybeObserver<T> {",
        ),
    ],
    "MaybeDematerialize.java": [
        (
            "/**\n * Maps the success value of the source to a Notification, then\n * maps it back to the corresponding signal type.\n * <p>History: 2.2.4 - experimental\n * @param <T> the element type of the source\n * @param <R> the element type of the Notification and result\n * @since 3.0.0\n */",
            "/**\n"
            " * 将上游 onSuccess 值映射为 {@link Notification}，\n"
            " * 再还原为对应的 onSuccess/onError/onComplete 信号。\n"
            " * <p>History: 2.2.4 - experimental\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> Notification 及结果元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public MaybeDematerialize(Maybe<T> source, Function<? super T, Notification<R>> selector) {",
            "    /**\n"
            "     * @param source 上游 Maybe\n"
            "     * @param selector 将成功值映射为 Notification 的函数\n"
            "     */\n"
            "    public MaybeDematerialize(Maybe<T> source, Function<? super T, Notification<R>> selector) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
            "    /** 订阅 DematerializeObserver 并还原 Notification 信号。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
        ),
        (
            "    static final class DematerializeObserver<T, R> implements MaybeObserver<T>, Disposable {",
            "    /** onSuccess 时应用 selector 并按 Notification 类型转发。 */\n"
            "    static final class DematerializeObserver<T, R> implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** 按 Notification 的 onNext/onComplete/onError 分支转发。 */\n"
            "        @Override\n        public void onSuccess(T t) {",
        ),
    ],
    "MaybeDetach.java": [
        (
            "/**\n * Breaks the references between the upstream and downstream when the Maybe terminates.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * {@link Maybe} 终止时断开上游与下游之间的引用，\n"
            " * 避免长期持有 observer 导致内存泄漏。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeDetach(MaybeSource<T> source) {",
            "    /** @param source 上游 Maybe */\n"
            "    public MaybeDetach(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 DetachMaybeObserver 并在终止后清空引用。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class DetachMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** 终止事件后将 downstream 置 null 断开引用。 */\n"
            "    static final class DetachMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 清空 downstream 并 dispose 上游。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 转发后清空 downstream 引用。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
    ],
    "MaybeDoAfterSuccess.java": [
        (
            "/**\n * Calls a consumer after pushing the current item to the downstream.\n * <p>History: 2.0.1 - experimental\n * @param <T> the value type\n * @since 2.1\n */",
            "/**\n"
            " * 向下游推送当前项后调用 {@link Consumer} 副作用。\n"
            " * <p>History: 2.0.1 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public MaybeDoAfterSuccess(MaybeSource<T> source, Consumer<? super T> onAfterSuccess) {",
            "    /**\n"
            "     * @param source 上游 Maybe\n"
            "     * @param onAfterSuccess onSuccess 转发后调用的 Consumer\n"
            "     */\n"
            "    public MaybeDoAfterSuccess(MaybeSource<T> source, Consumer<? super T> onAfterSuccess) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 DoAfterObserver 并在 onSuccess 后执行副作用。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class DoAfterObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** 先转发 onSuccess 再调用 onAfterSuccess。 */\n"
            "    static final class DoAfterObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
        (
            "                // remember, onSuccess is a terminal event and we can't call onError",
            "                // onSuccess 为终止事件，异常只能通过 RxJavaPlugins 上报",
        ),
    ],
    "MaybeDoFinally.java": [
        (
            "/**\n * Execute an action after an onSuccess, onError, onComplete or a dispose event.\n * <p>History: 2.0.1 - experimental\n * @param <T> the value type\n * @since 2.1\n */",
            "/**\n"
            " * 在 onSuccess、onError、onComplete 或 dispose 后执行 {@link Action}。\n"
            " * <p>History: 2.0.1 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public MaybeDoFinally(MaybeSource<T> source, Action onFinally) {",
            "    /**\n"
            "     * @param source 上游 Maybe\n"
            "     * @param onFinally 终止或 dispose 后执行的 Action\n"
            "     */\n"
            "    public MaybeDoFinally(MaybeSource<T> source, Action onFinally) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 DoFinallyObserver 并在任一终止路径后执行 onFinally。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class DoFinallyObserver<T> extends AtomicInteger implements MaybeObserver<T>, Disposable {",
            "    /** CAS 保证 onFinally 至多执行一次。 */\n"
            "    static final class DoFinallyObserver<T> extends AtomicInteger implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        void runFinally() {",
            "        /** compareAndSet(0,1) 后执行 onFinally，异常上报 RxJavaPlugins。 */\n"
            "        void runFinally() {",
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
            "wave13b Maybe* [15:30]",
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
        print(f"Marked {ok} files done in queue (note=wave13b)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
