#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-16a mixed operators [0:15]."""
from __future__ import annotations

import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
WAVE16A_FILE = Path("/tmp/rxjava_w16a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE16A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FlowableConcatMapCompletable.java": [
        (
            "/**\n * Maps the upstream items into {@link CompletableSource}s and subscribes to them one after the\n * other completes or terminates (in error-delaying mode).\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream value type\n * @since 2.2\n */",
            "/**\n * 将上游元素映射为 {@link CompletableSource}，\n * 在前一个完成或终止后再串行订阅下一个（错误延迟模式下可延后终止）。\n * <p>History: 2.1.11 - experimental\n * @param <T> 上游元素类型\n * @since 2.2\n */",
        ),
        (
            "    public FlowableConcatMapCompletable(Flowable<T> source,\n            Function<? super T, ? extends CompletableSource> mapper,\n            ErrorMode errorMode,\n            int prefetch) {",
            "    /**\n     * @param source 上游 Flowable\n     * @param mapper 由 T 映射 CompletableSource 的函数\n     * @param errorMode 错误处理模式\n     * @param prefetch 预取队列容量\n     */\n    public FlowableConcatMapCompletable(Flowable<T> source,\n            Function<? super T, ? extends CompletableSource> mapper,\n            ErrorMode errorMode,\n            int prefetch) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 订阅 ConcatMapCompletableObserver 串行执行 inner Completable。 */\n    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class ConcatMapCompletableObserver<T>\n    extends ConcatMapXMainSubscriber<T>\n    implements Disposable {",
            "    /** 管理队列、背压与 inner Completable 串行 drain。 */\n    static final class ConcatMapCompletableObserver<T>\n    extends ConcatMapXMainSubscriber<T>\n    implements Disposable {",
        ),
        (
            "        void innerError(Throwable ex) {",
            "        /** inner onError：IMMEDIATE 立即终止，否则 active=false 继续 drain。 */\n        void innerError(Throwable ex) {",
        ),
        (
            "        void innerComplete() {",
            "        /** inner onComplete 后 active=false 并继续 drain 下一项。 */\n        void innerComplete() {",
        ),
        (
            "        static final class ConcatMapInnerObserver extends AtomicReference<Disposable>\n        implements CompletableObserver {",
            "        /** 订阅单个 inner Completable 并将信号 relay 到 parent。 */\n        static final class ConcatMapInnerObserver extends AtomicReference<Disposable>\n        implements CompletableObserver {",
        ),
    ],
    "FlowableConcatMapMaybe.java": [
        (
            "/**\n * Maps each upstream item into a {@link MaybeSource}, subscribes to them one after the other terminates\n * and relays their success values, optionally delaying any errors till the main and inner sources\n * terminate.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream element type\n * @param <R> the output element type\n * @since 2.2\n */",
            "/**\n * 将上游各元素映射为 {@link MaybeSource}，串行订阅并在 inner 终止后\n * 转发 onSuccess 值；可选将错误延迟到主流与 inner 均终止后再上报。\n * <p>History: 2.1.11 - experimental\n * @param <T> 上游元素类型\n * @param <R> 下游元素类型\n * @since 2.2\n */",
        ),
        (
            "    public FlowableConcatMapMaybe(Flowable<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
            "    /**\n     * @param source 上游 Flowable\n     * @param mapper 由 T 映射 MaybeSource 的函数\n     * @param errorMode 错误处理模式\n     * @param prefetch 预取队列容量\n     */\n    public FlowableConcatMapMaybe(Flowable<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 订阅 ConcatMapMaybeSubscriber 串行映射 Maybe 并背压发射。 */\n    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "        /** No inner MaybeSource is running. */",
            "        /** 无 inner MaybeSource 在运行。 */",
        ),
        (
            "        /** An inner MaybeSource is running but there are no results yet. */",
            "        /** inner MaybeSource 运行中但尚无结果。 */",
        ),
        (
            "        /** The inner MaybeSource succeeded with a value in {@link #item}. */",
            "        /** inner MaybeSource 已成功，值缓存在 {@link #item}。 */",
        ),
        (
            "    static final class ConcatMapMaybeSubscriber<T, R>\n    extends ConcatMapXMainSubscriber<T> implements Subscription {",
            "    /** 串行 inner Maybe、管理 STATE 与背压 request。 */\n    static final class ConcatMapMaybeSubscriber<T, R>\n    extends ConcatMapXMainSubscriber<T> implements Subscription {",
        ),
        (
            "        void innerSuccess(R item) {",
            "        /** inner onSuccess：缓存 item 并置 STATE_RESULT_VALUE。 */\n        void innerSuccess(R item) {",
        ),
        (
            "        void innerComplete() {",
            "        /** inner onComplete：重置为 STATE_INACTIVE 并 drain。 */\n        void innerComplete() {",
        ),
        (
            "        void innerError(Throwable ex) {",
            "        /** inner onError：按 errorMode 取消上游或继续 drain。 */\n        void innerError(Throwable ex) {",
        ),
        (
            "        static final class ConcatMapMaybeObserver<R>\n        extends AtomicReference<Disposable>\n        implements MaybeObserver<R> {",
            "        /** 订阅单个 inner Maybe 并将信号 relay 到 parent。 */\n        static final class ConcatMapMaybeObserver<R>\n        extends AtomicReference<Disposable>\n        implements MaybeObserver<R> {",
        ),
    ],
    "FlowableConcatMapMaybePublisher.java": [
        (
            "/**\n * Maps each upstream item into a {@link MaybeSource}, subscribes to them one after the other terminates\n * and relays their success values, optionally delaying any errors till the main and inner sources\n * terminate.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream element type\n * @param <R> the output element type\n * @since 2.2\n */",
            "/**\n * 将 {@link Publisher} 各元素映射为 {@link MaybeSource} 并串行订阅，\n * 转发 inner onSuccess 值；复用 {@link FlowableConcatMapMaybe} 的 Subscriber 实现。\n * <p>History: 2.1.11 - experimental\n * @param <T> 上游元素类型\n * @param <R> 下游元素类型\n * @since 2.2\n */",
        ),
        (
            "    public FlowableConcatMapMaybePublisher(Publisher<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
            "    /**\n     * @param source 上游 Publisher\n     * @param mapper 由 T 映射 MaybeSource 的函数\n     * @param errorMode 错误处理模式\n     * @param prefetch 预取队列容量\n     */\n    public FlowableConcatMapMaybePublisher(Publisher<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 复用 ConcatMapMaybeSubscriber 订阅任意 Publisher。 */\n    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
    ],
    "FlowableConcatMapSingle.java": [
        (
            "/**\n * Maps each upstream item into a {@link SingleSource}, subscribes to them one after the other terminates\n * and relays their success values, optionally delaying any errors till the main and inner sources\n * terminate.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream element type\n * @param <R> the output element type\n * @since 2.2\n */",
            "/**\n * 将上游各元素映射为 {@link SingleSource}，串行订阅并在 inner 终止后\n * 转发 onSuccess 值；可选将错误延迟到主流与 inner 均终止后再上报。\n * <p>History: 2.1.11 - experimental\n * @param <T> 上游元素类型\n * @param <R> 下游元素类型\n * @since 2.2\n */",
        ),
        (
            "    public FlowableConcatMapSingle(Flowable<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
            "    /**\n     * @param source 上游 Flowable\n     * @param mapper 由 T 映射 SingleSource 的函数\n     * @param errorMode 错误处理模式\n     * @param prefetch 预取队列容量\n     */\n    public FlowableConcatMapSingle(Flowable<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 订阅 ConcatMapSingleSubscriber 串行映射 Single 并背压发射。 */\n    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "        /** No inner SingleSource is running. */",
            "        /** 无 inner SingleSource 在运行。 */",
        ),
        (
            "        /** An inner SingleSource is running but there are no results yet. */",
            "        /** inner SingleSource 运行中但尚无结果。 */",
        ),
        (
            "        /** The inner SingleSource succeeded with a value in {@link #item}. */",
            "        /** inner SingleSource 已成功，值缓存在 {@link #item}。 */",
        ),
        (
            "    static final class ConcatMapSingleSubscriber<T, R>\n    extends ConcatMapXMainSubscriber<T> implements Subscription {",
            "    /** 串行 inner Single、管理 STATE 与背压 request。 */\n    static final class ConcatMapSingleSubscriber<T, R>\n    extends ConcatMapXMainSubscriber<T> implements Subscription {",
        ),
        (
            "        void innerSuccess(R item) {",
            "        /** inner onSuccess：缓存 item 并置 STATE_RESULT_VALUE。 */\n        void innerSuccess(R item) {",
        ),
        (
            "        void innerError(Throwable ex) {",
            "        /** inner onError：按 errorMode 取消上游或继续 drain。 */\n        void innerError(Throwable ex) {",
        ),
        (
            "        static final class ConcatMapSingleObserver<R>\n        extends AtomicReference<Disposable>\n        implements SingleObserver<R> {",
            "        /** 订阅单个 inner Single 并将信号 relay 到 parent。 */\n        static final class ConcatMapSingleObserver<R>\n        extends AtomicReference<Disposable>\n        implements SingleObserver<R> {",
        ),
    ],
    "FlowableConcatMapSinglePublisher.java": [
        (
            "/**\n * Maps each upstream item into a {@link SingleSource}, subscribes to them one after the other terminates\n * and relays their success values, optionally delaying any errors till the main and inner sources\n * terminate.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream element type\n * @param <R> the output element type\n * @since 2.2\n */",
            "/**\n * 将 {@link Publisher} 各元素映射为 {@link SingleSource} 并串行订阅，\n * 转发 inner onSuccess 值；复用 {@link FlowableConcatMapSingle} 的 Subscriber 实现。\n * <p>History: 2.1.11 - experimental\n * @param <T> 上游元素类型\n * @param <R> 下游元素类型\n * @since 2.2\n */",
        ),
        (
            "    public FlowableConcatMapSinglePublisher(Publisher<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
            "    /**\n     * @param source 上游 Publisher\n     * @param mapper 由 T 映射 SingleSource 的函数\n     * @param errorMode 错误处理模式\n     * @param prefetch 预取队列容量\n     */\n    public FlowableConcatMapSinglePublisher(Publisher<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 复用 ConcatMapSingleSubscriber 订阅任意 Publisher。 */\n    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
    ],
    "FlowableSwitchMapCompletable.java": [
        (
            "/**\n * Maps the upstream values into {@link CompletableSource}s, subscribes to the newer one while\n * disposing the subscription to the previous {@code CompletableSource}, thus keeping at most one\n * active {@code CompletableSource} running.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream value type\n * @since 2.2\n */",
            "/**\n * 将上游元素映射为 {@link CompletableSource}，\n * 新元素到达时 dispose 旧 inner 并订阅新 inner，至多保持一个 active inner。\n * <p>History: 2.1.11 - experimental\n * @param <T> 上游元素类型\n * @since 2.2\n */",
        ),
        (
            "    public FlowableSwitchMapCompletable(Flowable<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {",
            "    /**\n     * @param source 上游 Flowable\n     * @param mapper 由 T 映射 CompletableSource 的函数\n     * @param delayErrors 是否延迟合并 inner 错误\n     */\n    public FlowableSwitchMapCompletable(Flowable<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 订阅 SwitchMapCompletableObserver，onNext 时切换 inner Completable。 */\n    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class SwitchMapCompletableObserver<T> implements FlowableSubscriber<T>, Disposable {",
            "    /** 管理 inner 切换、delayErrors 与主流终止逻辑。 */\n    static final class SwitchMapCompletableObserver<T> implements FlowableSubscriber<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 映射并 CAS 切换 inner；旧 inner dispose 后订阅新 Completable。 */\n        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void innerError(SwitchMapInnerObserver sender, Throwable error) {",
            "        /** inner onError：delayErrors 时等主流完成，否则 cancel 并终止。 */\n        void innerError(SwitchMapInnerObserver sender, Throwable error) {",
        ),
        (
            "        void innerComplete(SwitchMapInnerObserver sender) {",
            "        /** inner onComplete：若主流已 done 则向下游 onComplete/onError。 */\n        void innerComplete(SwitchMapInnerObserver sender) {",
        ),
        (
            "        static final class SwitchMapInnerObserver extends AtomicReference<Disposable>\n        implements CompletableObserver {",
            "        /** 单个 inner Completable 的 Observer，信号 relay 到 parent。 */\n        static final class SwitchMapInnerObserver extends AtomicReference<Disposable>\n        implements CompletableObserver {",
        ),
    ],
    "FlowableSwitchMapCompletablePublisher.java": [
        (
            "/**\n * Switch between subsequent {@link CompletableSource}s emitted by a {@link Publisher}.\n * Reuses {@link FlowableSwitchMapCompletable} internals.\n * @param <T> the upstream value type\n * @since 3.0.0\n */",
            "/**\n * 对 {@link Publisher} 发出的各元素映射并切换 {@link CompletableSource}。\n * 复用 {@link FlowableSwitchMapCompletable} 内部实现。\n * @param <T> 上游元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public FlowableSwitchMapCompletablePublisher(Publisher<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {",
            "    /**\n     * @param source 上游 Publisher\n     * @param mapper 由 T 映射 CompletableSource 的函数\n     * @param delayErrors 是否延迟合并 inner 错误\n     */\n    public FlowableSwitchMapCompletablePublisher(Publisher<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 复用 SwitchMapCompletableObserver 订阅任意 Publisher。 */\n    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "FlowableSwitchMapMaybe.java": [
        (
            "/**\n * Maps the upstream items into {@link MaybeSource}s and switches (subscribes) to the newer ones\n * while disposing the older ones and emits the latest success value if available, optionally delaying\n * errors from the main source or the inner sources.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n * @since 2.2\n */",
            "/**\n * 将上游元素映射为 {@link MaybeSource} 并 switch 到最新 inner，\n * dispose 旧 inner 后发射最新 onSuccess 值；可选延迟主流或 inner 错误。\n * <p>History: 2.1.11 - experimental\n * @param <T> 上游元素类型\n * @param <R> 下游元素类型\n * @since 2.2\n */",
        ),
        (
            "    public FlowableSwitchMapMaybe(Flowable<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n            boolean delayErrors) {",
            "    /**\n     * @param source 上游 Flowable\n     * @param mapper 由 T 映射 MaybeSource 的函数\n     * @param delayErrors 是否延迟合并错误\n     */\n    public FlowableSwitchMapMaybe(Flowable<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n            boolean delayErrors) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 订阅 SwitchMapMaybeSubscriber，背压下发射 inner onSuccess。 */\n    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "    static final class SwitchMapMaybeSubscriber<T, R> extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 切换 inner Maybe、背压 drain 发射 item。 */\n    static final class SwitchMapMaybeSubscriber<T, R> extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n        public void onNext(T t) {",
            "        /** dispose 当前 inner，映射并 CAS 订阅新 MaybeSource。 */\n        @Override\n        @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n        public void onNext(T t) {",
        ),
        (
            "        void innerError(SwitchMapMaybeObserver<R> sender, Throwable ex) {",
            "        /** inner onError：按 delayErrors 决定 cancel 或继续 drain。 */\n        void innerError(SwitchMapMaybeObserver<R> sender, Throwable ex) {",
        ),
        (
            "        void drain() {",
            "        /** 背压下从 inner.item 取值的 missed-drain 循环。 */\n        void drain() {",
        ),
        (
            "        static final class SwitchMapMaybeObserver<R>\n        extends AtomicReference<Disposable> implements MaybeObserver<R> {",
            "        /** 缓存 onSuccess 的 item 并触发 parent.drain。 */\n        static final class SwitchMapMaybeObserver<R>\n        extends AtomicReference<Disposable> implements MaybeObserver<R> {",
        ),
    ],
    "FlowableSwitchMapMaybePublisher.java": [
        (
            "/**\n * Switch between subsequent {@link MaybeSource}s emitted by a {@link Publisher}.\n * Reuses {@link FlowableSwitchMapMaybe} internals.\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n * @since 3.0.0\n */",
            "/**\n * 对 {@link Publisher} 各元素映射并切换 {@link MaybeSource}。\n * 复用 {@link FlowableSwitchMapMaybe} 内部实现。\n * @param <T> 上游元素类型\n * @param <R> 下游元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public FlowableSwitchMapMaybePublisher(Publisher<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n            boolean delayErrors) {",
            "    /**\n     * @param source 上游 Publisher\n     * @param mapper 由 T 映射 MaybeSource 的函数\n     * @param delayErrors 是否延迟合并错误\n     */\n    public FlowableSwitchMapMaybePublisher(Publisher<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n            boolean delayErrors) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 复用 SwitchMapMaybeSubscriber 订阅任意 Publisher。 */\n    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
    ],
    "FlowableSwitchMapSingle.java": [
        (
            "/**\n * Maps the upstream items into {@link SingleSource}s and switches (subscribes) to the newer ones\n * while disposing the older ones and emits the latest success value, optionally delaying\n * errors from the main source or the inner sources.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n * @since 2.2\n */",
            "/**\n * 将上游元素映射为 {@link SingleSource} 并 switch 到最新 inner，\n * dispose 旧 inner 后发射最新 onSuccess 值；可选延迟主流或 inner 错误。\n * <p>History: 2.1.11 - experimental\n * @param <T> 上游元素类型\n * @param <R> 下游元素类型\n * @since 2.2\n */",
        ),
        (
            "    public FlowableSwitchMapSingle(Flowable<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayErrors) {",
            "    /**\n     * @param source 上游 Flowable\n     * @param mapper 由 T 映射 SingleSource 的函数\n     * @param delayErrors 是否延迟合并错误\n     */\n    public FlowableSwitchMapSingle(Flowable<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayErrors) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 订阅 SwitchMapSingleSubscriber，背压下发射 inner onSuccess。 */\n    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "    static final class SwitchMapSingleSubscriber<T, R> extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 切换 inner Single、背压 drain 发射 item。 */\n    static final class SwitchMapSingleSubscriber<T, R> extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n        public void onNext(T t) {",
            "        /** dispose 当前 inner，映射并 CAS 订阅新 SingleSource。 */\n        @Override\n        @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n        public void onNext(T t) {",
        ),
        (
            "        void innerError(SwitchMapSingleObserver<R> sender, Throwable ex) {",
            "        /** inner onError：按 delayErrors 决定 cancel 或继续 drain。 */\n        void innerError(SwitchMapSingleObserver<R> sender, Throwable ex) {",
        ),
        (
            "        void drain() {",
            "        /** 背压下从 inner.item 取值的 missed-drain 循环。 */\n        void drain() {",
        ),
        (
            "        static final class SwitchMapSingleObserver<R>\n        extends AtomicReference<Disposable> implements SingleObserver<R> {",
            "        /** 缓存 onSuccess 的 item 并触发 parent.drain。 */\n        static final class SwitchMapSingleObserver<R>\n        extends AtomicReference<Disposable> implements SingleObserver<R> {",
        ),
    ],
    "FlowableSwitchMapSinglePublisher.java": [
        (
            "/**\n * Switch between subsequent {@link SingleSource}s emitted by a {@link Publisher}.\n * Reuses {@link FlowableSwitchMapSingle} internals.\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n * @since 3.0.0\n */",
            "/**\n * 对 {@link Publisher} 各元素映射并切换 {@link SingleSource}。\n * 复用 {@link FlowableSwitchMapSingle} 内部实现。\n * @param <T> 上游元素类型\n * @param <R> 下游元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public FlowableSwitchMapSinglePublisher(Publisher<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayErrors) {",
            "    /**\n     * @param source 上游 Publisher\n     * @param mapper 由 T 映射 SingleSource 的函数\n     * @param delayErrors 是否延迟合并错误\n     */\n    public FlowableSwitchMapSinglePublisher(Publisher<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayErrors) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 复用 SwitchMapSingleSubscriber 订阅任意 Publisher。 */\n    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
    ],
    "MaterializeSingleObserver.java": [
        (
            "/**\n * A consumer that implements the consumer types of Maybe, Single and Completable\n * and turns their signals into Notifications for a SingleObserver.\n * <p>History: 2.2.4 - experimental\n * @param <T> the element type of the source\n * @since 3.0.0\n */",
            "/**\n * 同时实现 Maybe、Single、Completable 的 Observer 接口，\n * 将各信号转为 {@link Notification} 并以 {@link SingleObserver#onSuccess} 向下游发射。\n * <p>History: 2.2.4 - experimental\n * @param <T> 源元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public MaterializeSingleObserver(SingleObserver<? super Notification<T>> downstream) {",
            "    /** @param downstream 接收 Notification 的 SingleObserver */\n    public MaterializeSingleObserver(SingleObserver<? super Notification<T>> downstream) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** onComplete 转为 Notification.createOnComplete 并 onSuccess。 */\n    @Override\n    public void onComplete() {",
        ),
        (
            "    @Override\n    public void onSuccess(T t) {",
            "    /** onSuccess 转为 Notification.createOnNext 并 onSuccess。 */\n    @Override\n    public void onSuccess(T t) {",
        ),
        (
            "    @Override\n    public void onError(Throwable e) {",
            "    /** onError 转为 Notification.createOnError 并 onSuccess。 */\n    @Override\n    public void onError(Throwable e) {",
        ),
    ],
    "MaybeFlatMapObservable.java": [
        (
            "/**\n * Maps the success value of a Maybe onto an ObservableSource and\n * relays its signals to the downstream observer.\n *\n * @param <T> the success value type of the Maybe source\n * @param <R> the result type of the ObservableSource and this operator\n * @since 2.1.15\n */",
            "/**\n * Maybe onSuccess 时将值映射为 {@link ObservableSource} 并订阅，\n * 将其信号 relay 到下游 {@link Observer}。\n * @param <T> Maybe 成功值类型\n * @param <R> ObservableSource 及本算子结果类型\n * @since 2.1.15\n */",
        ),
        (
            "    public MaybeFlatMapObservable(MaybeSource<T> source,\n            Function<? super T, ? extends ObservableSource<? extends R>> mapper) {",
            "    /**\n     * @param source 上游 MaybeSource\n     * @param mapper 由成功值映射 ObservableSource 的函数\n     */\n    public MaybeFlatMapObservable(MaybeSource<T> source,\n            Function<? super T, ? extends ObservableSource<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 先 onSubscribe FlatMapObserver，再订阅 Maybe。 */\n    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class FlatMapObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements Observer<R>, MaybeObserver<T>, Disposable {",
            "    /** onSuccess 时 flatMap 订阅 inner Observable 并 relay 信号。 */\n    static final class FlatMapObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements Observer<R>, MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** apply mapper 得 ObservableSource 并 subscribe(this)。 */\n        @Override\n        public void onSuccess(T t) {",
        ),
    ],
    "MaybeFlatMapPublisher.java": [
        (
            "/**\n * Maps the success value of a Maybe onto a Publisher and\n * relays its signals to the downstream subscriber.\n *\n * @param <T> the success value type of the Maybe source\n * @param <R> the result type of the Publisher and this operator\n * @since 2.1.15\n */",
            "/**\n * Maybe onSuccess 时将值映射为 {@link Publisher} 并订阅，\n * 将其信号 relay 到下游 {@link Subscriber}（支持背压）。\n * @param <T> Maybe 成功值类型\n * @param <R> Publisher 及本算子结果类型\n * @since 2.1.15\n */",
        ),
        (
            "    public MaybeFlatMapPublisher(MaybeSource<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper) {",
            "    /**\n     * @param source 上游 MaybeSource\n     * @param mapper 由成功值映射 Publisher 的函数\n     */\n    public MaybeFlatMapPublisher(MaybeSource<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 订阅 FlatMapPublisherSubscriber 并在 onSuccess 时切换 Publisher。 */\n    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "    static final class FlatMapPublisherSubscriber<T, R>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<R>, MaybeObserver<T>, Subscription {",
            "    /** 先作为 MaybeObserver 等待 onSuccess，再作为 Subscriber 转发 inner Publisher。 */\n    static final class FlatMapPublisherSubscriber<T, R>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<R>, MaybeObserver<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** apply mapper 得 Publisher 并 subscribe(this)。 */\n        @Override\n        public void onSuccess(T t) {",
        ),
        (
            "        @Override\n        public void onSubscribe(Subscription s) {",
            "        /** inner Publisher onSubscribe：deferredSetOnce 处理背压 request。 */\n        @Override\n        public void onSubscribe(Subscription s) {",
        ),
    ],
    "ObservableConcatMapCompletable.java": [
        (
            "/**\n * Maps the upstream items into {@link CompletableSource}s and subscribes to them one after the\n * other completes or terminates (in error-delaying mode).\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream value type\n * @since 2.2\n */",
            "/**\n * 将上游 Observable 元素映射为 {@link CompletableSource}，\n * 在前一个完成或终止后再串行订阅下一个（错误延迟模式下可延后终止）。\n * <p>History: 2.1.11 - experimental\n * @param <T> 上游元素类型\n * @since 2.2\n */",
        ),
        (
            "    public ObservableConcatMapCompletable(Observable<T> source,\n            Function<? super T, ? extends CompletableSource> mapper,\n            ErrorMode errorMode,\n            int prefetch) {",
            "    /**\n     * @param source 上游 Observable\n     * @param mapper 由 T 映射 CompletableSource 的函数\n     * @param errorMode 错误处理模式\n     * @param prefetch 预取队列容量\n     */\n    public ObservableConcatMapCompletable(Observable<T> source,\n            Function<? super T, ? extends CompletableSource> mapper,\n            ErrorMode errorMode,\n            int prefetch) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 标量优化失败时订阅 ConcatMapCompletableObserver。 */\n    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class ConcatMapCompletableObserver<T>\n    extends ConcatMapXMainObserver<T> {",
            "    /** 管理队列与 inner Completable 串行 drain（Observable 版）。 */\n    static final class ConcatMapCompletableObserver<T>\n    extends ConcatMapXMainObserver<T> {",
        ),
        (
            "        void innerError(Throwable ex) {",
            "        /** inner onError：非 END 模式 dispose 上游并继续 drain。 */\n        void innerError(Throwable ex) {",
        ),
        (
            "        void innerComplete() {",
            "        /** inner onComplete 后 active=false 并继续 drain 下一项。 */\n        void innerComplete() {",
        ),
        (
            "        static final class ConcatMapInnerObserver extends AtomicReference<Disposable>\n        implements CompletableObserver {",
            "        /** 订阅单个 inner Completable 并将信号 relay 到 parent。 */\n        static final class ConcatMapInnerObserver extends AtomicReference<Disposable>\n        implements CompletableObserver {",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


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
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
