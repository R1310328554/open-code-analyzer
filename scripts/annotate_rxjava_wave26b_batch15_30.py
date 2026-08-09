#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-26b internal util/virtual/observers [15:30]."""
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
WAVE26B_FILE = Path("/tmp/rxjava_w26b.txt")
SCRIPT_NAME = "annotate_rxjava_wave26b_batch15_30.py"
MARK_NOTE = "wave26b [15:30]"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE26B_FILE.read_text(encoding="utf-8").splitlines()
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
    "NotificationLite.java": [
        (
            "/**\n * Lightweight notification handling utility class.\n */",
            "/**\n"
            " * 轻量级通知编码工具：将 onNext/onError/onComplete/onSubscribe\n"
            " * 统一表示为 {@link Object}，供队列与序列化场景复用。\n"
            " */",
        ),
        (
            "    /**\n         * Wraps a Throwable.\n         */",
            "    /** 包装 Throwable 的错误通知 record。 */",
        ),
        (
            "    /**\n         * Wraps a Subscription.\n         */",
            "    /** 包装 {@link Subscription} 的订阅通知 record。 */",
        ),
        (
            "    /**\n         * Wraps a Disposable.\n         */",
            "    /** 包装 {@link Disposable} 的订阅通知 record。 */",
        ),
        (
            "    /**\n     * Converts a value into a notification value.\n     * @param <T> the actual value type\n     * @param value the value to convert\n     * @return the notification representing the value\n     */",
            "    /**\n"
            "     * 将普通值编码为 onNext 通知（即原值本身）。\n"
            "     * @param <T> 元素类型\n"
            "     * @param value 待编码值\n"
            "     * @return 表示 onNext 的通知对象\n"
            "     */",
        ),
        (
            "    /**\n     * Returns a complete notification.\n     * @return a complete notification\n     */",
            "    /** @return {@link #COMPLETE} 单例，表示 onComplete */",
        ),
        (
            "    /**\n     * Converts a Throwable into a notification value.\n     * @param e the Throwable to convert\n     * @return the notification representing the Throwable\n     */",
            "    /**\n"
            "     * 将 Throwable 包装为 {@link ErrorNotification}。\n"
            "     * @param e 错误\n"
            "     * @return 错误通知对象\n"
            "     */",
        ),
        (
            "    /**\n     * Converts a Subscription into a notification value.\n     * @param s the Subscription to convert\n     * @return the notification representing the Subscription\n     */",
            "    /**\n"
            "     * 将 Subscription 包装为 {@link SubscriptionNotification}。\n"
            "     * @param s 上游 Subscription\n"
            "     * @return 订阅通知对象\n"
            "     */",
        ),
        (
            "    /**\n     * Converts a Disposable into a notification value.\n     * @param d the disposable to convert\n     * @return the notification representing the Disposable\n     */",
            "    /**\n"
            "     * 将 Disposable 包装为 {@link DisposableNotification}。\n"
            "     * @param d 上游 Disposable\n"
            "     * @return 订阅通知对象\n"
            "     */",
        ),
        (
            "    /**\n     * Checks if the given object represents a complete notification.\n     * @param o the object to check\n     * @return true if the object represents a complete notification\n     */",
            "    /** 判断 o 是否为 COMPLETE 单例。 */",
        ),
        (
            "    /**\n     * Checks if the given object represents an error notification.\n     * @param o the object to check\n     * @return true if the object represents an error notification\n     */",
            "    /** 判断 o 是否为 ErrorNotification。 */",
        ),
        (
            "    /**\n     * Checks if the given object represents a subscription notification.\n     * @param o the object to check\n     * @return true if the object represents a subscription notification\n     */",
            "    /** 判断 o 是否为 SubscriptionNotification。 */",
        ),
        (
            "    public static boolean isDisposable(Object o) {",
            "    /** 判断 o 是否为 DisposableNotification。 */\n"
            "    public static boolean isDisposable(Object o) {",
        ),
        (
            "    /**\n     * Extracts the value from the notification object.\n     * @param <T> the expected value type when unwrapped\n     * @param o the notification object\n     * @return the extracted value\n     */",
            "    /**\n"
            "     * 从 onNext 通知中取出原值（强转）。\n"
            "     * @param <T> 期望类型\n"
            "     * @param o 通知对象\n"
            "     * @return 元素值\n"
            "     */",
        ),
        (
            "    /**\n     * Extracts the Throwable from the notification object.\n     * @param o the notification object\n     * @return the extracted Throwable\n     */",
            "    /** 从 ErrorNotification 中取出 Throwable。 */",
        ),
        (
            "    /**\n     * Extracts the Subscription from the notification object.\n     * @param o the notification object\n     * @return the extracted Subscription\n     */",
            "    /** 从 SubscriptionNotification 中取出 Subscription。 */",
        ),
        (
            "    public static Disposable getDisposable(Object o) {",
            "    /** 从 DisposableNotification 中取出 Disposable。 */\n"
            "    public static Disposable getDisposable(Object o) {",
        ),
        (
            "    /**\n     * Calls the appropriate Subscriber method based on the type of the notification.\n     * <p>Does not check for a subscription notification, see {@link #acceptFull(Object, Subscriber)}.\n     * @param <T> the expected value type when unwrapped\n     * @param o the notification object\n     * @param s the subscriber to call methods on\n     * @return true if the notification was a terminal event (i.e., complete or error)\n     * @see #acceptFull(Object, Subscriber)\n     */",
            "    /**\n"
            "     * 按通知类型调用 Subscriber 的 onComplete/onError/onNext。\n"
            "     * 不处理 Subscription 通知，见 {@link #acceptFull(Object, Subscriber)}。\n"
            "     * @return 若为终止事件（complete 或 error）则 true\n"
            "     * @see #acceptFull(Object, Subscriber)\n"
            "     */",
        ),
        (
            "    /**\n     * Calls the appropriate Observer method based on the type of the notification.\n     * <p>Does not check for a subscription notification.\n     * @param <T> the expected value type when unwrapped\n     * @param o the notification object\n     * @param observer the Observer to call methods on\n     * @return true if the notification was a terminal event (i.e., complete or error)\n     */",
            "    /**\n"
            "     * 按通知类型调用 Observer 的 onComplete/onError/onNext。\n"
            "     * 不处理 Disposable 订阅通知。\n"
            "     * @return 若为终止事件则 true\n"
            "     */",
        ),
        (
            "    /**\n     * Calls the appropriate Subscriber method based on the type of the notification.\n     * @param <T> the expected value type when unwrapped\n     * @param o the notification object\n     * @param s the subscriber to call methods on\n     * @return true if the notification was a terminal event (i.e., complete or error)\n     * @see #accept(Object, Subscriber)\n     */",
            "    /**\n"
            "     * 完整版 accept：含 onSubscribe(Subscription) 分支。\n"
            "     * @return 若为 complete 或 error 则 true\n"
            "     * @see #accept(Object, Subscriber)\n"
            "     */",
        ),
        (
            "    /**\n     * Calls the appropriate Observer method based on the type of the notification.\n     * @param <T> the expected value type when unwrapped\n     * @param o the notification object\n     * @param observer the subscriber to call methods on\n     * @return true if the notification was a terminal event (i.e., complete or error)\n     * @see #accept(Object, Observer)\n     */",
            "    /**\n"
            "     * 完整版 accept：含 onSubscribe(Disposable) 分支。\n"
            "     * @return 若为 complete 或 error 则 true\n"
            "     * @see #accept(Object, Observer)\n"
            "     */",
        ),
    ],
    "ObservableQueueDrain.java": [
        (
            "import io.reactivex.rxjava4.core.Observer;\n\npublic interface ObservableQueueDrain<T, U> {",
            "import io.reactivex.rxjava4.core.Observer;\n\n"
            "/**\n"
            " * Observable 侧队列排空契约：查询取消/完成/错误状态，\n"
            " * 管理 wip 计数并向 Observer 转发元素。\n"
            " *\n"
            " * @param <T> 队列/上游元素类型\n"
            " * @param <U> 下游接收类型\n"
            " */\n"
            "public interface ObservableQueueDrain<T, U> {",
        ),
        (
            "    boolean cancelled();",
            "    /** 是否已取消订阅。 */\n"
            "    boolean cancelled();",
        ),
        (
            "    boolean done();",
            "    /** 上游是否已完成（正常或错误）。 */\n"
            "    boolean done();",
        ),
        (
            "    Throwable error();",
            "    /** 上游错误（未完成时为 null）。 */\n"
            "    Throwable error();",
        ),
        (
            "    boolean enter();",
            "    /** CAS 进入 drain 临界区（wip 0→1）。 */\n"
            "    boolean enter();",
        ),
        (
            "    /**\n     * Adds m to the wip counter.\n     * @param m the value to add\n     * @return the wip value after adding the value\n     */",
            "    /**\n"
            "     * 将 wip 计数增加 m（通常 drain 结束时减回）。\n"
            "     * @param m 增量\n"
            "     * @return 增加后的 wip 值\n"
            "     */",
        ),
        (
            "    /**\n     * Accept the value and return true if forwarded.\n     * @param observer the subscriber to deliver values to\n     * @param value the value to deliver\n     */",
            "    /**\n"
            "     * 向 Observer 发射 value（实现类决定背压/丢弃策略）。\n"
            "     * @param observer 下游 Observer\n"
            "     * @param value 待发射值\n"
            "     */",
        ),
    ],
    "OpenHashSet.java": [
        (
            "/**\n * A simple open hash set with add, remove and clear capabilities only.\n * <p>Doesn't support nor checks for {@code null}s.\n *\n * @param <T> the element type\n */",
            "/**\n"
            " * 开放寻址哈希集合：仅支持 add/remove，不支持 null。\n"
            " * 借鉴 fastutil OpenHashSet，负载因子默认 0.75。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public OpenHashSet() {",
            "    /** 默认容量 16、负载因子 0.75。 */\n"
            "    public OpenHashSet() {",
        ),
        (
            "    /**\n     * Creates an OpenHashSet with the initial capacity and load factor of 0.75f.\n     * @param capacity the initial capacity\n     */",
            "    /**\n"
            "     * 指定初始容量，负载因子 0.75。\n"
            "     * @param capacity 初始容量（会 round 到 2 的幂）\n"
            "     */",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    public OpenHashSet(int capacity, float loadFactor) {",
            "    /**\n"
            "     * @param capacity 初始容量\n"
            "     * @param loadFactor 负载因子，决定 rehash 阈值\n"
            "     */\n"
            "    @SuppressWarnings(\"unchecked\")\n    public OpenHashSet(int capacity, float loadFactor) {",
        ),
        (
            "    public boolean add(T value) {",
            "    /** 线性探测插入；已存在则 false；超 maxSize 时 rehash。 */\n"
            "    public boolean add(T value) {",
        ),
        (
            "    public boolean remove(T value) {",
            "    /** 线性探测删除；不存在则 false。 */\n"
            "    public boolean remove(T value) {",
        ),
        (
            "    boolean removeEntry(int pos, T[] a, int m) {",
            "    /** 删除 pos 处元素并回填后续可前移项（Knuth 算法 6.4R）。 */\n"
            "    boolean removeEntry(int pos, T[] a, int m) {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    void rehash() {",
            "    /** 容量翻倍并重新分布所有非 null 键。 */\n"
            "    @SuppressWarnings(\"unchecked\")\n    void rehash() {",
        ),
        (
            "    static int mix(int x) {",
            "    /** 用 INT_PHI 乘法与高位异或打散 hashCode。 */\n"
            "    static int mix(int x) {",
        ),
        (
            "    public Object[] keys() {",
            "    /** 返回内部 keys 数组（仅供内部迭代，勿修改）。 */\n"
            "    public Object[] keys() {",
        ),
        (
            "    public int size() {",
            "    /** 当前元素个数。 */\n"
            "    public int size() {",
        ),
    ],
    "Pow2.java": [
        (
            "public final class Pow2 {",
            "/**\n"
            " * 2 的幂次工具（源自 JCTools Pow2）。\n"
            " * 供队列/哈希表容量对齐使用。\n"
            " */\n"
            "public final class Pow2 {",
        ),
        (
            "    private Pow2() {",
            "    /** 工具类禁止实例化。 */\n"
            "    private Pow2() {",
        ),
        (
            "    /**\n     * Find the next larger positive power of two value up from the given value. If value is a power of two then\n     * this value will be returned.\n     *\n     * @param value from which next positive power of two will be found.\n     * @return the next positive power of 2 or this value if it is a power of 2.\n     */",
            "    /**\n"
            "     * 返回不小于 value 的最小 2 的幂（value 本身为 2 的幂则原样返回）。\n"
            "     * @param value 输入值\n"
            "     * @return 2 的幂\n"
            "     */",
        ),
        (
            "    /**\n     * Is this value a power of two.\n     *\n     * @param value to be tested to see if it is a power of two.\n     * @return true if the value is a power of 2 otherwise false.\n     */",
            "    /**\n"
            "     * 判断 value 是否为 2 的幂。\n"
            "     * @param value 待测值\n"
            "     * @return 是 2 的幂则 true\n"
            "     */",
        ),
    ],
    "QueueDrain.java": [
        (
            "import static java.util.concurrent.Flow.*;\n\npublic interface QueueDrain<T, U> {",
            "import static java.util.concurrent.Flow.*;\n\n"
            "/**\n"
            " * Flowable 侧队列排空契约：除 Observable 版状态外，\n"
            " * 还跟踪 requested/produced 背压计数。\n"
            " *\n"
            " * @param <T> 队列元素类型\n"
            " * @param <U> 下游接收类型\n"
            " */\n"
            "public interface QueueDrain<T, U> {",
        ),
        (
            "    boolean cancelled();",
            "    /** 是否已取消。 */\n"
            "    boolean cancelled();",
        ),
        (
            "    boolean done();",
            "    /** 上游是否结束。 */\n"
            "    boolean done();",
        ),
        (
            "    Throwable error();",
            "    /** 上游错误。 */\n"
            "    Throwable error();",
        ),
        (
            "    boolean enter();",
            "    /** 尝试进入 drain（wip CAS）。 */\n"
            "    boolean enter();",
        ),
        (
            "    long requested();",
            "    /** 当前累计 request 量。 */\n"
            "    long requested();",
        ),
        (
            "    long produced(long n);",
            "    /** 记录已向下游发射 n 个元素并返回新的 produced 计数。 */\n"
            "    long produced(long n);",
        ),
        (
            "    /**\n     * Adds m to the wip counter.\n     * @param m the value to add\n     * @return the current value after adding m\n     */",
            "    /**\n"
            "     * wip 计数增加 m。\n"
            "     * @param m 增量\n"
            "     * @return 增加后的 wip\n"
            "     */",
        ),
        (
            "    /**\n     * Accept the value and return true if forwarded.\n     * @param subscriber the subscriber\n     * @param value the value\n     * @return true if the value was delivered\n     */",
            "    /**\n"
            "     * 向 Subscriber 发射 value。\n"
            "     * @param subscriber 下游 Subscriber\n"
            "     * @param value 待发射值\n"
            "     * @return 成功转发则 true\n"
            "     */",
        ),
    ],
    "SorterFunction.java": [
        (
            "import io.reactivex.rxjava4.functions.Function;\n\npublic final class SorterFunction<T> implements Function<List<T>, List<T>> {",
            "import io.reactivex.rxjava4.functions.Function;\n\n"
            "/**\n"
            " * 对 List 原地排序的 {@link Function}，供 toSortedList 等算子使用。\n"
            " *\n"
            " * @param <T> 列表元素类型\n"
            " */\n"
            "public final class SorterFunction<T> implements Function<List<T>, List<T>> {",
        ),
        (
            "    final Comparator<? super T> comparator;",
            "    /** 排序比较器。 */\n"
            "    final Comparator<? super T> comparator;",
        ),
        (
            "    public SorterFunction(Comparator<? super T> comparator) {",
            "    /** @param comparator 用于 List.sort 的比较器 */\n"
            "    public SorterFunction(Comparator<? super T> comparator) {",
        ),
        (
            "    @Override\n    public List<T> apply(List<T> t) {",
            "    /** 原地 sort 后返回同一 List 引用。 */\n"
            "    @Override\n    public List<T> apply(List<T> t) {",
        ),
    ],
    "SuppressAnimalSniffer.java": [
        (
            "/**\n * Suppress errors by the AnimalSniffer plugin.\n */",
            "/**\n"
            " * 标记方法/构造器/类型以抑制 AnimalSniffer 对\n"
            " * Android/API 兼容性检查的误报。\n"
            " */",
        ),
    ],
    "VolatileSizeArrayList.java": [
        (
            "/**\n * Tracks the current underlying array size in a volatile field.\n *\n * @param <T> the element type\n * @since 2.0.7\n */",
            "/**\n"
            " * 用 {@link AtomicInteger} 以 lazySet 维护 List 大小的包装器：\n"
            " * {@link #size()} 读 volatile 计数，修改操作后更新计数，便于无锁观测长度。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " * @since 2.0.7\n"
            " */",
        ),
        (
            "    final ArrayList<T> list;",
            "    /** 实际存储的 ArrayList。 */\n"
            "    final ArrayList<T> list;",
        ),
        (
            "    public VolatileSizeArrayList() {",
            "    /** 空列表，初始 size 为 0。 */\n"
            "    public VolatileSizeArrayList() {",
        ),
        (
            "    public VolatileSizeArrayList(int initialCapacity) {",
            "    /** @param initialCapacity 底层 ArrayList 初始容量 */\n"
            "    public VolatileSizeArrayList(int initialCapacity) {",
        ),
        (
            "    @Override\n    public int size() {",
            "    /** 读 AtomicInteger 中的 volatile size，非 list.size() 同步读。 */\n"
            "    @Override\n    public int size() {",
        ),
        (
            "    @Override\n    public boolean add(T e) {",
            "    /** add 成功后 lazySet 新 size。 */\n"
            "    @Override\n    public boolean add(T e) {",
        ),
        (
            "    @Override\n    public void clear() {",
            "    /** 清空 list 并将 size lazySet 为 0。 */\n"
            "    @Override\n    public void clear() {",
        ),
    ],
    "FlowableVirtualCreateExecutor.java": [
        (
            "/**\n * Runs a generator callback on a virtual thread backed by a Worker of the given scheduler\n * and signals events emitted by the generator considering any downstream backpressure.\n *\n * @param <T> the element type of the flow\n * @since 4.0.0\n */",
            "/**\n"
            " * 在虚拟线程（或 ExecutorService）上运行 {@link VirtualGenerator}，\n"
            " * 通过 {@link VirtualResumable} 协调下游背压与 emit。\n"
            " *\n"
            " * @param <T> 流元素类型\n"
            " * @since 4.0.0\n"
            " */",
        ),
        (
            "    public FlowableVirtualCreateExecutor(VirtualGenerator<T> generator, ExecutorService executor, Scheduler scheduler) {",
            "    /**\n"
            "     * @param generator 在虚拟线程中执行的生成器\n"
            "     * @param executor 非 null 时用其 submit；否则用 scheduler Worker\n"
            "     * @param scheduler 无 executor 时的调度源\n"
            "     */\n"
            "    public FlowableVirtualCreateExecutor(VirtualGenerator<T> generator, ExecutorService executor, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 创建 Subscription、onSubscribe，再 submit/schedule 生成任务。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "        static final Throwable STOP = new Throwable(\"Downstream cancelled\");",
            "        /** 下游 cancel 时 emit 抛出的哨兵异常。 */\n"
            "        static final Throwable STOP = new Throwable(\"Downstream cancelled\");",
        ),
        (
            "        @Override\n        public Void call() {",
            "        /** 执行 generator.generate；正常结束 onComplete，finally 释放 worker。 */\n"
            "        @Override\n        public Void call() {",
        ),
        (
            "        @Override\n        public void request(long n) {",
            "        /** 累加背压计数并 resume 生成线程。 */\n"
            "        @Override\n        public void request(long n) {",
        ),
        (
            "        @Override\n        public void cancel() {",
            "        /** 置 cancelled、dispose canceller，request(1) 唤醒 await。 */\n"
            "        @Override\n        public void cancel() {",
        ),
        (
            "        @Override\n        public void emit(T item) throws Throwable {",
            "        /** 等待 requested>produced 后 onNext；cancel 则抛 STOP。 */\n"
            "        @Override\n        public void emit(T item) throws Throwable {",
        ),
    ],
    "FlowableVirtualTransformExecutor.java": [
        (
            "public final class FlowableVirtualTransformExecutor<T, R> extends Flowable<R> {",
            "/**\n"
            " * 在虚拟线程上对上游 Flowable 逐元素调用 {@link VirtualTransformer}，\n"
            " * 用 SPSC 队列缓冲上游，双 VirtualResumable 协调生产/消费背压。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 下游元素类型\n"
            " */\n"
            "public final class FlowableVirtualTransformExecutor<T, R> extends Flowable<R> {",
        ),
        (
            "    public FlowableVirtualTransformExecutor(Flowable<T> source,\n            VirtualTransformer<T, R> transformer,\n            ExecutorService executor,\n            Scheduler scheduler,\n            int prefetch) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param transformer 逐元素虚拟变换\n"
            "     * @param executor 可选执行器\n"
            "     * @param scheduler 无 executor 时的 Worker 来源\n"
            "     * @param prefetch 上游预取与队列容量\n"
            "     */\n"
            "    public FlowableVirtualTransformExecutor(Flowable<T> source,\n            VirtualTransformer<T, R> transformer,\n            ExecutorService executor,\n            Scheduler scheduler,\n            int prefetch) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 订阅上游并 submit/schedule 变换循环。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "        static final Throwable STOP = new Throwable(\"Downstream cancelled\");",
            "        /** cancel 时 emit 抛出的哨兵。 */\n"
            "        static final Throwable STOP = new Throwable(\"Downstream cancelled\");",
        ),
        (
            "        @Override\n        public void onSubscribe(Subscription s) {",
            "        /** 保存 upstream、向下游 onSubscribe(this)、request(prefetch)。 */\n"
            "        @Override\n        public void onSubscribe(Subscription s) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 入队 SpscArrayQueue，首次 wip 时 resume 生产线程。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void emit(R item) throws Throwable {",
            "        /** 等待 requested>produced 后 downstream.onNext。 */\n"
            "        @Override\n        public void emit(R item) throws Throwable {",
        ),
        (
            "        @Override\n        public Void call() {",
            "        /** 主循环：poll 上游元素、transformer.transform、75% prefetch 时再 request。 */\n"
            "        @Override\n        public Void call() {",
        ),
        (
            "        @Override\n        public void cancel() {",
            "        /** cancel 上游、dispose canceller/worker，双端 resume 唤醒 park。 */\n"
            "        @Override\n        public void cancel() {",
        ),
    ],
    "VirtualResumable.java": [
        (
            "/**\n * Fundamental primitive for suspending and resuming a Thread.\n * @since 4.0.0\n */",
            "/**\n"
            " * 虚拟线程挂起/恢复原语：await 时 park，resume 时 unpark 或置 READY。\n"
            " * 供 FlowableVirtual* 算子协调背压。\n"
            " * @since 4.0.0\n"
            " */",
        ),
        (
            "    /**\n     * Indicates the {@link #await()} can resume without parking the virtual thread.\n     */",
            "    /** 表示 await 可直接通过，无需 park。 */",
        ),
        (
            "    /**\n     * Wait for a resumption by a {@link #resume()} call.\n     * This method won't suspend the current virtual thread if there was already\n     * a resume indication.\n     */",
            "    /**\n"
            "     * 等待 resume：无 READY 时 CAS 登记当前线程并 LockSupport.park。\n"
            "     * 若已 READY 则立即返回；退出时 clear 状态。\n"
            "     */",
        ),
        (
            "    /**\n     * Clears any resumption/ready object from this VirtualResumable.\n     */",
            "    /** getAndSet(null)，清除 READY 或 parked 线程引用。 */",
        ),
        (
            "    /**\n     * Trigger a resumption of a virtual thread suspended in {@link #await()}.\n     * This method can be called from multiple threads and multiple times.\n     * Note that this method is not guaranteed to act as a full memory barrier\n     * if there was a resume() call previously and the suspend side didn't suspend yet.\n     */",
            "    /**\n"
            "     * 触发恢复：置 READY 并对已 park 的线程 unpark。\n"
            "     * 可多次、多线程调用；与 await 之间不保证完整内存屏障。\n"
            "     */",
        ),
    ],
    "DefaultObserver.java": [
        (
            "/**\n * Abstract base implementation of an {@link io.reactivex.rxjava4.core.Observer Observer} with support for cancelling a\n * subscription via {@code #cancel()} (synchronously) and calls {@code #onStart()}\n * when the subscription happens.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>Use the protected {@code #cancel()} to dispose the sequence from within an\n * {@code onNext} implementation.\n *\n * <p>Like all other consumers, {@code DefaultObserver} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onNext(Object)}, {@link #onError(Throwable)}\n * and {@link #onComplete()} are not allowed to throw any unchecked exceptions.\n * If for some reason this can't be avoided, use {@link io.reactivex.rxjava4.core.Observable#safeSubscribe(io.reactivex.rxjava4.core.Observer)}\n * instead of the standard {@code subscribe()} method.\n *\n * <p>Example<pre><code>\n * Observable.range(1, 5)\n *     .subscribe(new DefaultObserver&lt;Integer&gt;() {\n *         &#64;Override public void onStart() {\n *             System.out.println(\"Start!\");\n *         }\n *         &#64;Override public void onNext(Integer t) {\n *             if (t == 3) {\n *                 cancel();\n *             }\n *             System.out.println(t);\n *         }\n *         &#64;Override public void onError(Throwable t) {\n *             t.printStackTrace();\n *         }\n *         &#64;Override public void onComplete() {\n *             System.out.println(\"Done!\");\n *         }\n *     });\n * </code></pre>\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 带同步 {@link #cancel()} 的 Observer 抽象基类：\n"
            " * onSubscribe 校验单次订阅后调用 {@link #onStart()}。\n"
            " *\n"
            " * <p>预置 final 方法线程安全；仅允许订阅一次。\n"
            " *\n"
            " * <p>在 onNext 中可调用 protected {@link #cancel()} 取消上游。\n"
            " *\n"
            " * <p>onStart/onNext/onError/onComplete 不应抛出未检查异常；\n"
            " * 否则请用 {@link io.reactivex.rxjava4.core.Observable#safeSubscribe(io.reactivex.rxjava4.core.Observer)}。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
            "    /** EndConsumerHelper.validate 通过后保存 upstream 并 onStart()。 */\n"
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    /**\n     * Cancels the upstream's disposable.\n     */",
            "    /** 同步 dispose 上游并将 upstream 置 DISPOSED。 */",
        ),
        (
            "    /**\n     * Called once the subscription has been set on this observer; override this\n     * to perform initialization.\n     */",
            "    /** 订阅建立后回调，子类可覆写做初始化。 */",
        ),
    ],
    "DisposableCompletableObserver.java": [
        (
            "/**\n * An abstract {@link CompletableObserver} that allows asynchronous cancellation by implementing Disposable.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>Like all other consumers, {@code DisposableCompletableObserver} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onError(Throwable)} and\n * {@link #onComplete()} are not allowed to throw any unchecked exceptions.\n *\n * <p>Example<pre><code>\n * Disposable d =\n *     Completable.complete().delay(1, TimeUnit.SECONDS)\n *     .subscribeWith(new DisposableMaybeObserver&lt;Integer&gt;() {\n *         &#64;Override public void onStart() {\n *             System.out.println(\"Start!\");\n *         }\n *         &#64;Override public void onError(Throwable t) {\n *             t.printStackTrace();\n *         }\n *         &#64;Override public void onComplete() {\n *             System.out.println(\"Done!\");\n *         }\n *     });\n * // ...\n * d.dispose();\n * </code></pre>\n */",
            "/**\n"
            " * 实现 {@link Disposable} 的 {@link CompletableObserver} 抽象基类：\n"
            " * AtomicReference 持有上游，支持异步 dispose。\n"
            " *\n"
            " * <p>仅允许单次订阅；final 方法线程安全。\n"
            " *\n"
            " * <p>onStart/onError/onComplete 不应抛出未检查异常。\n"
            " */",
        ),
        (
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
            "    /** setOnce 成功后调用 onStart()。 */\n"
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    /**\n     * Called once the single upstream {@link Disposable} is set via {@link #onSubscribe(Disposable)}.\n     */",
            "    /** 上游 Disposable 设置成功后回调。 */",
        ),
        (
            "    @Override\n    public final boolean isDisposed() {",
            "    /** upstream 是否为 DISPOSED。 */\n"
            "    @Override\n    public final boolean isDisposed() {",
        ),
        (
            "    @Override\n    public final void dispose() {",
            "    /** DisposableHelper.dispose(upstream)。 */\n"
            "    @Override\n    public final void dispose() {",
        ),
    ],
    "DisposableMaybeObserver.java": [
        (
            "/**\n * An abstract {@link MaybeObserver} that allows asynchronous cancellation by implementing {@link Disposable}.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>Note that {@link #onSuccess(Object)}, {@link #onError(Throwable)} and {@link #onComplete()} are\n * exclusive to each other, unlike a regular {@link io.reactivex.rxjava4.core.Observer Observer}, and\n * {@code onComplete()} is never called after an {@code onSuccess()}.\n *\n * <p>Like all other consumers, {@code DisposableMaybeObserver} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onSuccess(Object)}, {@link #onError(Throwable)} and\n * {@link #onComplete()} are not allowed to throw any unchecked exceptions.\n *\n * <p>Example<pre><code>\n * Disposable d =\n *     Maybe.just(1).delay(1, TimeUnit.SECONDS)\n *     .subscribeWith(new DisposableMaybeObserver&lt;Integer&gt;() {\n *         &#64;Override public void onStart() {\n *             System.out.println(\"Start!\");\n *         }\n *         &#64;Override public void onSuccess(Integer t) {\n *             System.out.println(t);\n *         }\n *         &#64;Override public void onError(Throwable t) {\n *             t.printStackTrace();\n *         }\n *         &#64;Override public void onComplete() {\n *             System.out.println(\"Done!\");\n *         }\n *     });\n * // ...\n * d.dispose();\n * </code></pre>\n *\n * @param <T> the received value type\n */",
            "/**\n"
            " * 实现 {@link Disposable} 的 {@link MaybeObserver} 抽象基类。\n"
            " *\n"
            " * <p>onSuccess/onError/onComplete 互斥，onSuccess 后不会 onComplete。\n"
            " *\n"
            " * <p>仅允许单次订阅；final 方法线程安全。\n"
            " *\n"
            " * @param <T> 接收值类型\n"
            " */",
        ),
        (
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
            "    /** setOnce 成功后 onStart()。 */\n"
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    /**\n     * Called once the single upstream {@link Disposable} is set via {@link #onSubscribe(Disposable)}.\n     */",
            "    /** 上游 Disposable 就绪后回调。 */",
        ),
        (
            "    @Override\n    public final boolean isDisposed() {",
            "    /** 是否已 dispose。 */\n"
            "    @Override\n    public final boolean isDisposed() {",
        ),
        (
            "    @Override\n    public final void dispose() {",
            "    /** 取消上游订阅。 */\n"
            "    @Override\n    public final void dispose() {",
        ),
    ],
    "DisposableObserver.java": [
        (
            "/**\n * An abstract {@link Observer} that allows asynchronous cancellation by implementing {@link Disposable}.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>Use the public {@link #dispose()} method to dispose the sequence from within an\n * {@code onNext} implementation.\n *\n * <p>Like all other consumers, {@code DisposableObserver} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onNext(Object)}, {@link #onError(Throwable)}\n * and {@link #onComplete()} are not allowed to throw any unchecked exceptions.\n * If for some reason this can't be avoided, use {@link io.reactivex.rxjava4.core.Observable#safeSubscribe(io.reactivex.rxjava4.core.Observer)}\n * instead of the standard {@code subscribe()} method.\n *\n * <p>Example<pre><code>\n * Disposable d =\n *     Observable.range(1, 5)\n *     .subscribeWith(new DisposableObserver&lt;Integer&gt;() {\n *         &#64;Override public void onStart() {\n *             System.out.println(\"Start!\");\n *         }\n *         &#64;Override public void onNext(Integer t) {\n *             if (t == 3) {\n *                 dispose();\n *             }\n *             System.out.println(t);\n *         }\n *         &#64;Override public void onError(Throwable t) {\n *             t.printStackTrace();\n *         }\n *         &#64;Override public void onComplete() {\n *             System.out.println(\"Done!\");\n *         }\n *     });\n * // ...\n * d.dispose();\n * </code></pre>\n *\n * @param <T> the received value type\n */",
            "/**\n"
            " * 实现 {@link Disposable} 的 {@link Observer} 抽象基类：\n"
            " * 在 onNext 中可调用 {@link #dispose()} 异步取消。\n"
            " *\n"
            " * <p>仅允许单次订阅；final 方法线程安全。\n"
            " *\n"
            " * <p>回调不应抛出未检查异常，否则用 safeSubscribe。\n"
            " *\n"
            " * @param <T> 接收值类型\n"
            " */",
        ),
        (
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
            "    /** setOnce 后 onStart()。 */\n"
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    /**\n     * Called once the single upstream Disposable is set via onSubscribe.\n     */",
            "    /** 上游 Disposable 设置成功后调用。 */",
        ),
        (
            "    @Override\n    public final boolean isDisposed() {",
            "    /** upstream 是否为 DISPOSED。 */\n"
            "    @Override\n    public final boolean isDisposed() {",
        ),
        (
            "    @Override\n    public final void dispose() {",
            "    /** DisposableHelper.dispose(upstream)。 */\n"
            "    @Override\n    public final void dispose() {",
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
    index_file = Path("/tmp/git-index-rxjava-w26b")
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
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 26b [15:30]",
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
        "queue: mark rxjava 4.0.0-alpha-21 wave26b done",
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
