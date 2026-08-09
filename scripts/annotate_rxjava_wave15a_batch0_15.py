#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-15a Maybe operators [0:15]."""
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
WAVE15A_FILE = Path("/tmp/rxjava_w15a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE15A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
# wave15b = next batch slice from current batch.json (files 16–30)
WAVE15B_FILES = [
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeTimeoutPublisher.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeTimer.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeToFlowable.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeToObservable.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeToPublisher.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeToSingle.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeUnsafeCreate.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeUnsubscribeOn.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeUsing.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeZipArray.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/maybe/MaybeZipIterable.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/mixed/CompletableAndThenObservable.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/mixed/CompletableAndThenPublisher.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/mixed/ConcatMapXMainObserver.java",
    "src/main/java/io/reactivex/rxjava4/internal/operators/mixed/ConcatMapXMainSubscriber.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "MaybeMap.java": [
        (
            "/**\n * Maps the upstream success value into some other value.\n *\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n */",
            "/**\n"
            " * 将上游 onSuccess 值经 {@link Function} 映射为另一类型并向下游发射。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 下游元素类型\n"
            " */",
        ),
        (
            "    public MaybeMap(MaybeSource<T> source, Function<? super T, ? extends R> mapper) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param mapper 由 T 映射为 R 的函数\n"
            "     */\n"
            "    public MaybeMap(MaybeSource<T> source, Function<? super T, ? extends R> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
            "    /** 包装为 MapMaybeObserver 并订阅上游。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
        ),
        (
            "    static final class MapMaybeObserver<T, R> implements MaybeObserver<T>, Disposable {",
            "    /** onSuccess 时 apply mapper；null 或异常转为 onError。 */\n"
            "    static final class MapMaybeObserver<T, R> implements MaybeObserver<T>, Disposable {",
        ),
    ],
    "MaybeMaterialize.java": [
        (
            "/**\n * Turn the signal types of a Maybe source into a single Notification of\n * equal kind.\n * <p>History: 2.2.4 - experimental\n *\n * @param <T> the element type of the source\n * @since 3.0.0\n */",
            "/**\n"
            " * 将 Maybe 的 onSuccess/onError/onComplete 信号\n"
            " * 封装为单个 {@link Notification} 并以 {@link Single} 形式发射。\n"
            " * <p>History: 2.2.4 - experimental\n"
            " *\n"
            " * @param <T> 源元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public MaybeMaterialize(Maybe<T> source) {",
            "    /** @param source 待物化的 Maybe */\n"
            "    public MaybeMaterialize(Maybe<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Notification<T>> observer) {",
            "    /** 通过 MaterializeSingleObserver 将 Maybe 信号转为 Notification。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Notification<T>> observer) {",
        ),
    ],
    "MaybeNever.java": [
        (
            "/**\n * Doesn't signal any event other than onSubscribe.\n */",
            "/**\n"
            " * 仅发出 onSubscribe（{@link EmptyDisposable#NEVER}），\n"
            " * 永不触发 onSuccess/onError/onComplete。\n"
            " */",
        ),
        (
            "    public static final MaybeNever INSTANCE = new MaybeNever();",
            "    /** 单例永不终止 Maybe 实例。 */\n"
            "    public static final MaybeNever INSTANCE = new MaybeNever();",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super Object> observer) {",
            "    /** 仅 onSubscribe(EmptyDisposable.NEVER)。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super Object> observer) {",
        ),
    ],
    "MaybeObserveOn.java": [
        (
            "/**\n * Signals the onSuccess, onError or onComplete events on the specific scheduler.\n *\n * @param <T> the value type delivered\n */",
            "/**\n"
            " * 在指定 {@link Scheduler} 上调度并向下游转发\n"
            " * onSuccess、onError 或 onComplete 事件。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeObserveOn(MaybeSource<T> source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param scheduler 事件调度器\n"
            "     */\n"
            "    public MaybeObserveOn(MaybeSource<T> source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 包装为 ObserveOnMaybeObserver，在 scheduler 上 run 转发。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class ObserveOnMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable, Runnable {",
            "    /** 缓存 value/error 后 scheduleDirect(this) 在目标线程转发。 */\n"
            "    static final class ObserveOnMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 按 error → success → complete 顺序向下游 relay。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "MaybeOnErrorComplete.java": [
        (
            "/**\n * Emits an onComplete if the source emits an onError and the predicate returns true for\n * that Throwable.\n * \n * @param <T> the value type\n */",
            "/**\n"
            " * 上游 onError 时若 {@link Predicate} 对异常返回 true，\n"
            " * 则向下游 onComplete 而非 onError。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeOnErrorComplete(MaybeSource<T> source,\n            Predicate<? super Throwable> predicate) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param predicate 判定是否吞掉异常\n"
            "     */\n"
            "    public MaybeOnErrorComplete(MaybeSource<T> source,\n            Predicate<? super Throwable> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 包装为 OnErrorCompleteMultiObserver。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    public static final class OnErrorCompleteMultiObserver<T>\n    implements MaybeObserver<T>, SingleObserver<T>, Disposable {",
            "    /** onError 时 test predicate；true 则 onComplete。 */\n"
            "    public static final class OnErrorCompleteMultiObserver<T>\n    implements MaybeObserver<T>, SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** predicate 异常则 CompositeException；否则按 test 结果转发。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "MaybeOnErrorNext.java": [
        (
            "/**\n * Subscribes to the MaybeSource returned by a function if the main source signals an onError.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 主源 onError 时调用 {@link Function} 获取备用 {@link MaybeSource} 并订阅。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeOnErrorNext(MaybeSource<T> source,\n            Function<? super Throwable, ? extends MaybeSource<? extends T>> resumeFunction) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param resumeFunction 由异常映射备用 MaybeSource\n"
            "     */\n"
            "    public MaybeOnErrorNext(MaybeSource<T> source,\n            Function<? super Throwable, ? extends MaybeSource<? extends T>> resumeFunction) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 包装为 OnErrorNextMaybeObserver。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class OnErrorNextMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
            "    /** onError 时 resumeFunction 得新源并由 NextMaybeObserver 订阅。 */\n"
            "    static final class OnErrorNextMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** apply resumeFunction；null 或异常则 CompositeException。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        record NextMaybeObserver<T>(MaybeObserver<? super T> downstream,\n                                    AtomicReference<Disposable> upstream) implements MaybeObserver<T> {",
            "        /** 备用 Maybe 信号直接 relay 到 downstream。 */\n"
            "        record NextMaybeObserver<T>(MaybeObserver<? super T> downstream,\n                                    AtomicReference<Disposable> upstream) implements MaybeObserver<T> {",
        ),
    ],
    "MaybeOnErrorReturn.java": [
        (
            "/**\n * Returns a value generated via a function if the main source signals an onError.\n * @param <T> the value type\n */",
            "/**\n"
            " * 主源 onError 时调用 {@link Function} 生成替代值并以 onSuccess 向下游发射。\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeOnErrorReturn(MaybeSource<T> source,\n            Function<? super Throwable, ? extends T> itemSupplier) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param itemSupplier 由异常生成替代 T\n"
            "     */\n"
            "    public MaybeOnErrorReturn(MaybeSource<T> source,\n            Function<? super Throwable, ? extends T> itemSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 包装为 OnErrorReturnMaybeObserver。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class OnErrorReturnMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** onError 时 itemSupplier.apply 得值并 onSuccess。 */\n"
            "    static final class OnErrorReturnMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** null 或 supplier 异常则 CompositeException。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "MaybePeek.java": [
        (
            "/**\n * Peeks into the lifecycle of a Maybe and MaybeObserver.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 在 Maybe 生命周期各阶段插入副作用回调（peek），\n"
            " * 不改变事件语义地观察 onSubscribe/onSuccess/onError/onComplete/dispose。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybePeek(MaybeSource<T> source, Consumer<? super Disposable> onSubscribeCall,\n            Consumer<? super T> onSuccessCall, Consumer<? super Throwable> onErrorCall, Action onCompleteCall,\n            Action onAfterTerminate, Action onDispose) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param onSubscribeCall onSubscribe 时调用\n"
            "     * @param onSuccessCall onSuccess 时调用\n"
            "     * @param onErrorCall onError 时调用\n"
            "     * @param onCompleteCall onComplete 时调用\n"
            "     * @param onAfterTerminate 终止事件转发后调用\n"
            "     * @param onDispose dispose 时调用\n"
            "     */\n"
            "    public MaybePeek(MaybeSource<T> source, Consumer<? super Disposable> onSubscribeCall,\n            Consumer<? super T> onSuccessCall, Consumer<? super Throwable> onErrorCall, Action onCompleteCall,\n            Action onAfterTerminate, Action onDispose) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 包装为 MaybePeekObserver。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class MaybePeekObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** 各回调先于或伴随下游转发；回调异常会中断正常流程。 */\n"
            "    static final class MaybePeekObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 先 onDisposeCall 再 dispose 上游。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        void onErrorInner(Throwable e) {",
            "        /** onErrorCall 后 downstream.onError 并 onAfterTerminate。 */\n"
            "        void onErrorInner(Throwable e) {",
        ),
        (
            "        void onAfterTerminate() {",
            "        /** 终止后 run onAfterTerminate；异常上报 RxJavaPlugins。 */\n"
            "        void onAfterTerminate() {",
        ),
    ],
    "MaybeSubscribeOn.java": [
        (
            "/**\n * Subscribes to the upstream MaybeSource on the specified scheduler.\n *\n * @param <T> the value type delivered\n */",
            "/**\n"
            " * 在指定 {@link Scheduler} 上异步订阅上游 {@link MaybeSource}。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeSubscribeOn(MaybeSource<T> source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param scheduler 订阅调度器\n"
            "     */\n"
            "    public MaybeSubscribeOn(MaybeSource<T> source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 先 onSubscribe(parent)，再 scheduleDirect 订阅上游。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    record SubscribeTask<T>(MaybeObserver<? super T> observer, MaybeSource<T> source) implements Runnable {",
            "    /** 在 scheduler 线程执行 source.subscribe(observer)。 */\n"
            "    record SubscribeTask<T>(MaybeObserver<? super T> observer, MaybeSource<T> source) implements Runnable {",
        ),
        (
            "    static final class SubscribeOnMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
            "    /** task 持有 schedule 的 Disposable；事件 relay 到 downstream。 */\n"
            "    static final class SubscribeOnMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
        ),
    ],
    "MaybeSwitchIfEmpty.java": [
        (
            "/**\n * Subscribes to the other source if the main source is empty.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 主源 onComplete（空）时订阅备用 {@link MaybeSource}；\n"
            " * onSuccess/onError 直接转发。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeSwitchIfEmpty(MaybeSource<T> source, MaybeSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 主 MaybeSource\n"
            "     * @param other 主源为空时的备用 MaybeSource\n"
            "     */\n"
            "    public MaybeSwitchIfEmpty(MaybeSource<T> source, MaybeSource<? extends T> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 包装为 SwitchIfEmptyMaybeObserver。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class SwitchIfEmptyMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
            "    /** onComplete 且未 dispose 时 CAS 清空并订阅 other。 */\n"
            "    static final class SwitchIfEmptyMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** compareAndSet 成功后 other.subscribe(OtherMaybeObserver)。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        record OtherMaybeObserver<T>(MaybeObserver<? super T> downstream,\n                                     AtomicReference<Disposable> parent) implements MaybeObserver<T> {",
            "        /** 备用 Maybe 信号 relay 到 downstream。 */\n"
            "        record OtherMaybeObserver<T>(MaybeObserver<? super T> downstream,\n                                     AtomicReference<Disposable> parent) implements MaybeObserver<T> {",
        ),
    ],
    "MaybeSwitchIfEmptySingle.java": [
        (
            "/**\n * Subscribes to the other source if the main source is empty.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 主 Maybe 为空（onComplete）时订阅备用 {@link SingleSource}，\n"
            " * 以 {@link Single} 形式保证必有 onSuccess/onError。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeSwitchIfEmptySingle(MaybeSource<T> source, SingleSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 主 MaybeSource\n"
            "     * @param other 主源为空时的备用 SingleSource\n"
            "     */\n"
            "    public MaybeSwitchIfEmptySingle(MaybeSource<T> source, SingleSource<? extends T> other) {",
        ),
        (
            "    @Override\n    public MaybeSource<T> source() {",
            "    /** 返回上游 MaybeSource。 */\n"
            "    @Override\n    public MaybeSource<T> source() {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** SwitchIfEmptyMaybeObserver 在 onComplete 时切换 Single。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class SwitchIfEmptyMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
            "    /** onComplete 时 CAS 并订阅 other SingleSource。 */\n"
            "    static final class SwitchIfEmptyMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        record OtherSingleObserver<T>(SingleObserver<? super T> downstream,\n                                      AtomicReference<Disposable> parent) implements SingleObserver<T> {",
            "        /** 备用 Single 结果 relay 到 downstream。 */\n"
            "        record OtherSingleObserver<T>(SingleObserver<? super T> downstream,\n                                      AtomicReference<Disposable> parent) implements SingleObserver<T> {",
        ),
    ],
    "MaybeTakeUntilMaybe.java": [
        (
            "/**\n * Relays the main source's event unless the other Maybe signals an item first or just completes\n * at which point the resulting Maybe is completed.\n *\n * @param <T> the value type\n * @param <U> the other's value type\n */",
            "/**\n"
            " * 转发主源事件，除非 other {@link MaybeSource} 先 onSuccess 或 onComplete，\n"
            " * 此时取消主源并向下游 onComplete。\n"
            " *\n"
            " * @param <T> 主源元素类型\n"
            " * @param <U> other 元素类型\n"
            " */",
        ),
        (
            "    public MaybeTakeUntilMaybe(MaybeSource<T> source, MaybeSource<U> other) {",
            "    /**\n"
            "     * @param source 主 MaybeSource\n"
            "     * @param other 触发截断的 MaybeSource\n"
            "     */\n"
            "    public MaybeTakeUntilMaybe(MaybeSource<T> source, MaybeSource<U> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 同时订阅 main 与 other；other 先终止则截断 main。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class TakeUntilMainMaybeObserver<T, U>\n    extends AtomicReference<Disposable> implements MaybeObserver<T>, Disposable {",
            "    /** 主源事件前 dispose other；other 先完成则 otherComplete。 */\n"
            "    static final class TakeUntilMainMaybeObserver<T, U>\n    extends AtomicReference<Disposable> implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        void otherComplete() {",
            "        /** other 成功或完成时 dispose 主源并 onComplete。 */\n"
            "        void otherComplete() {",
        ),
        (
            "        static final class TakeUntilOtherMaybeObserver<U>\n        extends AtomicReference<Disposable> implements MaybeObserver<U> {",
            "        /** other 任意终止事件通知 parent.otherComplete/otherError。 */\n"
            "        static final class TakeUntilOtherMaybeObserver<U>\n        extends AtomicReference<Disposable> implements MaybeObserver<U> {",
        ),
    ],
    "MaybeTakeUntilPublisher.java": [
        (
            "/**\n * Relays the main source's event unless the other Publisher signals an item first or just completes\n * at which point the resulting Maybe is completed.\n *\n * @param <T> the value type\n * @param <U> the other's value type\n */",
            "/**\n"
            " * 转发主源事件，除非 other {@link Publisher} 先 onNext 或 onComplete，\n"
            " * 此时取消主源并向下游 onComplete。\n"
            " *\n"
            " * @param <T> 主源元素类型\n"
            " * @param <U> other 元素类型\n"
            " */",
        ),
        (
            "    public MaybeTakeUntilPublisher(MaybeSource<T> source, Publisher<U> other) {",
            "    /**\n"
            "     * @param source 主 MaybeSource\n"
            "     * @param other 触发截断的 Publisher\n"
            "     */\n"
            "    public MaybeTakeUntilPublisher(MaybeSource<T> source, Publisher<U> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 同时订阅 main Maybe 与 other Publisher。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class TakeUntilMainMaybeObserver<T, U>\n    extends AtomicReference<Disposable> implements MaybeObserver<T>, Disposable {",
            "    /** dispose 时 cancel other Subscription。 */\n"
            "    static final class TakeUntilMainMaybeObserver<T, U>\n    extends AtomicReference<Disposable> implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        static final class TakeUntilOtherMaybeObserver<U>\n        extends AtomicReference<Subscription> implements FlowableSubscriber<U> {",
            "        /** onNext 或 onComplete 时 cancel 并 parent.otherComplete。 */\n"
            "        static final class TakeUntilOtherMaybeObserver<U>\n        extends AtomicReference<Subscription> implements FlowableSubscriber<U> {",
        ),
    ],
    "MaybeTimeInterval.java": [
        (
            "/**\n * Measures the time between subscription and the success item emission\n * from the upstream and emits this as a {@link Timed} success value.\n * @param <T> the element type of the sequence\n * @since 3.0.0\n */",
            "/**\n"
            " * 测量订阅到上游 onSuccess 的时间间隔，\n"
            " * 以 {@link Timed} 包装值并向下游 onSuccess 发射。\n"
            " * @param <T> 上游元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public MaybeTimeInterval(MaybeSource<T> source, TimeUnit unit, Scheduler scheduler, boolean start) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 计时用 Scheduler\n"
            "     * @param start true 时在 onSubscribe 记录起点\n"
            "     */\n"
            "    public MaybeTimeInterval(MaybeSource<T> source, TimeUnit unit, Scheduler scheduler, boolean start) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(@NonNull MaybeObserver<? super @NonNull Timed<T>> observer) {",
            "    /** 包装为 TimeIntervalMaybeObserver。 */\n"
            "    @Override\n    protected void subscribeActual(@NonNull MaybeObserver<? super @NonNull Timed<T>> observer) {",
        ),
        (
            "    static final class TimeIntervalMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** onSuccess 时发射 Timed(t, now-startTime, unit)。 */\n"
            "    static final class TimeIntervalMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
    ],
    "MaybeTimeoutMaybe.java": [
        (
            "/**\n * Switches to the fallback Maybe if the other MaybeSource signals a success or completes, or\n * signals TimeoutException if fallback is null.\n * \n * @param <T> the main value type\n * @param <U> the other value type\n */",
            "/**\n"
            " * other {@link MaybeSource} 先 onSuccess 或 onComplete 时切换到 fallback Maybe；\n"
            " * fallback 为 null 则向下游 onError({@link TimeoutException})。\n"
            " *\n"
            " * @param <T> 主源元素类型\n"
            " * @param <U> other 元素类型\n"
            " */",
        ),
        (
            "    public MaybeTimeoutMaybe(MaybeSource<T> source, MaybeSource<U> other, MaybeSource<? extends T> fallback) {",
            "    /**\n"
            "     * @param source 主 MaybeSource\n"
            "     * @param other 超时触发源\n"
            "     * @param fallback 超时后的备用 MaybeSource（可为 null）\n"
            "     */\n"
            "    public MaybeTimeoutMaybe(MaybeSource<T> source, MaybeSource<U> other, MaybeSource<? extends T> fallback) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 同时订阅 main 与 other；other 先终止则 timeout 逻辑。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class TimeoutMainMaybeObserver<T, U>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
            "    /** 主源正常终止时 dispose other；other 先终止则 otherComplete。 */\n"
            "    static final class TimeoutMainMaybeObserver<T, U>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        public void otherComplete() {",
            "        /** fallback 非 null 则 subscribe otherObserver；否则 TimeoutException。 */\n"
            "        public void otherComplete() {",
        ),
        (
            "    static final class TimeoutOtherMaybeObserver<T, U>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<Object> {",
            "    /** other 任意终止事件通知 parent.otherComplete/otherError。 */\n"
            "    static final class TimeoutOtherMaybeObserver<T, U>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<Object> {",
        ),
        (
            "    static final class TimeoutFallbackMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T> {",
            "    /** fallback Maybe 信号 relay 到 downstream。 */\n"
            "    static final class TimeoutFallbackMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T> {",
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
            "wave15a",
            *files,
        ],
        check=True,
    )
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["files"] = WAVE15B_FILES
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
        print(f"Marked {ok} files done in queue (note=wave15a)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
