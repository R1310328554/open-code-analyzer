#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-3b [15:30]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"

BATCH_FILES = [
    "src/main/java/io/reactivex/rxjava4/disposables/Disposable.java",
    "src/main/java/io/reactivex/rxjava4/disposables/DisposableContainer.java",
    "src/main/java/io/reactivex/rxjava4/disposables/DisposableStreamerCancellation.java",
    "src/main/java/io/reactivex/rxjava4/disposables/FutureDisposable.java",
    "src/main/java/io/reactivex/rxjava4/disposables/ReferenceDisposable.java",
    "src/main/java/io/reactivex/rxjava4/disposables/RunnableDisposable.java",
    "src/main/java/io/reactivex/rxjava4/disposables/SerialDisposable.java",
    "src/main/java/io/reactivex/rxjava4/disposables/StreamerCancellation.java",
    "src/main/java/io/reactivex/rxjava4/disposables/SubscriptionDisposable.java",
    "src/main/java/io/reactivex/rxjava4/disposables/package-info.java",
    "src/main/java/io/reactivex/rxjava4/exceptions/CompositeException.java",
    "src/main/java/io/reactivex/rxjava4/exceptions/Exceptions.java",
    "src/main/java/io/reactivex/rxjava4/exceptions/MissingBackpressureException.java",
    "src/main/java/io/reactivex/rxjava4/exceptions/OnErrorNotImplementedException.java",
    "src/main/java/io/reactivex/rxjava4/exceptions/ProtocolViolationException.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str] | tuple[str, str, bool]]] = {
    "Disposable.java": [
        ("/**\n * Represents a disposable resource or ongoing task.\n */", "/**\n * 表示可 dispose 的资源或进行中的任务。\n */"),
        ("    /**\n     * Dispose the resource, the operation should be idempotent.\n     */", "    /**\n     * dispose 资源，此操作应为幂等的。\n     */"),
        ("    /**\n     * Returns true if this resource has been disposed.\n     * @return true if this resource has been disposed\n     */", "    /**\n     * 若本资源已被 dispose 则返回 true。\n     * @return 若本资源已被 dispose 则为 true\n     */"),
        ("    /**\n     * Construct a {@code Disposable} by wrapping a {@link Runnable} that is\n     * executed exactly once when the {@code Disposable} is disposed.\n     * @param run the {@code Runnable} to wrap\n     * @return the new {@code Disposable} instance\n     * @throws NullPointerException if {@code run} is {@code null}\n     * @since 3.0.0\n     */", "    /**\n     * 通过包装 {@link Runnable} 构造 {@code Disposable}，在 dispose 时恰好执行一次。\n     * @param run 要包装的 {@code Runnable}\n     * @return 新的 {@code Disposable} 实例\n     * @throws NullPointerException 若 {@code run} 为 {@code null}\n     * @since 3.0.0\n     */"),
        ("    /**\n     * Construct a {@code Disposable} by wrapping a {@link Action} that is\n     * executed exactly once when the {@code Disposable} is disposed.\n     * @param action the {@code Action} to wrap\n     * @return the new {@code Disposable} instance\n     * @throws NullPointerException if {@code action} is {@code null}\n     * @since 3.0.0\n     */", "    /**\n     * 通过包装 {@link Action} 构造 {@code Disposable}，在 dispose 时恰好执行一次。\n     * @param action 要包装的 {@code Action}\n     * @return 新的 {@code Disposable} 实例\n     * @throws NullPointerException 若 {@code action} 为 {@code null}\n     * @since 3.0.0\n     */"),
        ("    /**\n     * Construct a {@code Disposable} by wrapping a {@link Future} that is\n     * cancelled exactly once when the {@code Disposable} is disposed.\n     * <p>\n     * The {@code Future} is cancelled with {@code mayInterruptIfRunning == true}.\n     * @param future the {@code Future} to wrap\n     * @return the new {@code Disposable} instance\n     * @throws NullPointerException if {@code future} is {@code null}\n     * @see #fromFuture(Future, boolean)\n     * @since 3.0.0\n     */", "    /**\n     * 通过包装 {@link Future} 构造 {@code Disposable}，在 dispose 时恰好取消一次。\n     * <p>\n     * {@code Future} 以 {@code mayInterruptIfRunning == true} 取消。\n     * @param future 要包装的 {@code Future}\n     * @return 新的 {@code Disposable} 实例\n     * @throws NullPointerException 若 {@code future} 为 {@code null}\n     * @see #fromFuture(Future, boolean)\n     * @since 3.0.0\n     */"),
        ("    /**\n     * Construct a {@code Disposable} by wrapping a {@link Future} that is\n     * cancelled exactly once when the {@code Disposable} is disposed.\n     * @param future the {@code Future} to wrap\n     * @param allowInterrupt if true, the future cancel happens via {@code Future.cancel(true)}\n     * @return the new {@code Disposable} instance\n     * @throws NullPointerException if {@code future} is {@code null}\n     * @since 3.0.0\n     */", "    /**\n     * 通过包装 {@link Future} 构造 {@code Disposable}，在 dispose 时恰好取消一次。\n     * @param future 要包装的 {@code Future}\n     * @param allowInterrupt 若为 true，则通过 {@code Future.cancel(true)} 取消 future\n     * @return 新的 {@code Disposable} 实例\n     * @throws NullPointerException 若 {@code future} 为 {@code null}\n     * @since 3.0.0\n     */"),
        ("    /**\n     * Construct a {@code Disposable} by wrapping a {@link Subscription} that is\n     * cancelled exactly once when the {@code Disposable} is disposed.\n     * @param subscription the {@code Runnable} to wrap\n     * @return the new {@code Disposable} instance\n     * @throws NullPointerException if {@code subscription} is {@code null}\n     * @since 3.0.0\n     */", "    /**\n     * 通过包装 {@link Subscription} 构造 {@code Disposable}，在 dispose 时恰好取消一次。\n     * @param subscription 要包装的 {@code Subscription}\n     * @return 新的 {@code Disposable} 实例\n     * @throws NullPointerException 若 {@code subscription} 为 {@code null}\n     * @since 3.0.0\n     */"),
        ("    /**\n     * Construct a {@code Disposable} by wrapping an {@link AutoCloseable} that is\n     * closed exactly once when the {@code Disposable} is disposed.\n     * @param autoCloseable the {@code AutoCloseable} to wrap\n     * @return the new {@code Disposable} instance\n     * @throws NullPointerException if {@code autoCloseable} is {@code null}\n     * @since 3.0.0\n     */", "    /**\n     * 通过包装 {@link AutoCloseable} 构造 {@code Disposable}，在 dispose 时恰好关闭一次。\n     * @param autoCloseable 要包装的 {@code AutoCloseable}\n     * @return 新的 {@code Disposable} 实例\n     * @throws NullPointerException 若 {@code autoCloseable} 为 {@code null}\n     * @since 3.0.0\n     */"),
        ("    /**\n     * Construct an {@link AutoCloseable} by wrapping a {@code Disposable} that is\n     * disposed when the returned {@code AutoCloseable} is closed.\n     * @param disposable the {@code Disposable} instance\n     * @return the new {@code AutoCloseable} instance\n     * @throws NullPointerException if {@code disposable} is {@code null}\n     * @since 3.0.0\n     */", "    /**\n     * 通过包装 {@code Disposable} 构造 {@link AutoCloseable}，在关闭返回的 {@code AutoCloseable} 时 dispose。\n     * @param disposable {@code Disposable} 实例\n     * @return 新的 {@code AutoCloseable} 实例\n     * @throws NullPointerException 若 {@code disposable} 为 {@code null}\n     * @since 3.0.0\n     */"),
        ("    /**\n     * Wraps this {@code Disposable} into an {@link AutoCloseable} instance\n     * that can be used with try-with-resources constructs.\n     * @return the new {@code AutoCloseable} instance\n     * @since 4.0.0\n     */", "    /**\n     * 将本 {@code Disposable} 包装为可用于 try-with-resources 的 {@link AutoCloseable} 实例。\n     * @return 新的 {@code AutoCloseable} 实例\n     * @since 4.0.0\n     */"),
        ("    /**\n     * Returns a new, non-disposed {@code Disposable} instance.\n     * @return a new, non-disposed {@code Disposable} instance\n     * @since 3.0.0\n     */", "    /**\n     * 返回新的、未 dispose 的 {@code Disposable} 实例。\n     * @return 新的、未 dispose 的 {@code Disposable} 实例\n     * @since 3.0.0\n     */"),
        ("    /**\n     * Returns a shared, disposed {@code Disposable} instance.\n     * @return a shared, disposed {@code Disposable} instance\n     * @since 3.0.0\n     */", "    /**\n     * 返回共享的、已 dispose 的 {@code Disposable} 实例。\n     * @return 共享的、已 dispose 的 {@code Disposable} 实例\n     * @since 3.0.0\n     */"),
    ],
    "DisposableContainer.java": [
        ("/**\n * Common interface to add and remove {@link Disposable}s from a container.\n * @since 2.0\n */", "/**\n * 向容器添加与移除 {@link Disposable} 的通用接口。\n * @since 2.0\n */"),
        ("    /**\n     * Adds a disposable to this container or disposes it if the\n     * container has been disposed.\n     * @param d the disposable to add, not null\n     * @return true if successful, false if this container has been disposed\n     */", "    /**\n     * 向本容器添加 disposable；若容器已被 dispose 则直接 dispose 该 disposable。\n     * @param d 要添加的 disposable，不可为 null\n     * @return 成功则为 true；若本容器已被 dispose 则为 false\n     */"),
        ("    /**\n     * Removes and disposes the given disposable if it is part of this\n     * container.\n     * @param d the disposable to remove and dispose, not null\n     * @return true if the operation was successful\n     */", "    /**\n     * 若给定 disposable 属于本容器，则移除并 dispose 它。\n     * @param d 要移除并 dispose 的 disposable，不可为 null\n     * @return 若操作成功则为 true\n     */"),
        ("    /**\n     * Removes but does not dispose the given disposable if it is part of this\n     * container.\n     * @param d the disposable to remove, not null\n     * @return true if the operation was successful\n     */", "    /**\n     * 若给定 disposable 属于本容器，则移除但不 dispose 它。\n     * @param d 要移除的 disposable，不可为 null\n     * @return 若操作成功则为 true\n     */"),
        ("    /**\n     * Removes all contained {@link Disposable}s without disposing them, making this\n     * container fresh.\n     * @since 4.0.0\n     */", "    /**\n     * 移除所有包含的 {@link Disposable} 但不 dispose 它们，使本容器恢复为空。\n     * @since 4.0.0\n     */"),
        ("    /**\n     * Removes and disposes all contained {@link Disposable}s, making this container fresh\n     * without disposing the entire container.\n     */", "    /**\n     * 移除并 dispose 所有包含的 {@link Disposable}，使本容器恢复为空，但不 dispose 整个容器。\n     */"),
    ],
    "DisposableStreamerCancellation.java": [
        ("/**\n * Represents the full, disposable cancellation interface for {@code Streamer}\n * operations.\n * <p>\n * It was decided to have a separate set of interfaces instead of trying to\n * retrofit {@link DisposableContainer}.\n * @since 4.0.0\n */", "/**\n * 表示 {@code Streamer} 操作的完整、可 dispose 的取消接口。\n * <p>\n * 决定采用独立接口集，而非改造 {@link DisposableContainer}。\n * @since 4.0.0\n */"),
        ("    /**\n     * Returns a constant instance which does nothing, cannot be disposed and\n     * accepts any incoming Disposable without registering it or handling it in any form,\n     * because this {@code never} instance cannot be disposed to begin with.\n     * @return the shared constant no-op instance\n     */", "    /**\n     * 返回常量实例：不执行任何操作、不可 dispose，且接受任何传入的 Disposable 但不注册或处理，\n     * 因为该 {@code never} 实例本身就无法 dispose。\n     * @return 共享的无操作常量实例\n     */"),
    ],
    "FutureDisposable.java": [
        ("/**\n * A Disposable container that cancels a {@link Future} instance.\n */", "/**\n * 取消 {@link Future} 实例的 Disposable 容器。\n */"),
        ("    FutureDisposable(Future<?> run, boolean allowInterrupt) {", "    /** @param run 要管理的 Future；@param allowInterrupt 取消时是否允许中断运行中的线程 */\n    FutureDisposable(Future<?> run, boolean allowInterrupt) {"),
        ("    @Override\n    public boolean isDisposed() {", "    /** 若 Future 为 null 或已完成则视为已 dispose。 */\n    @Override\n    public boolean isDisposed() {"),
        ("    @Override\n    public void dispose() {", "    /** 原子取出 Future 并取消，仅执行一次。 */\n    @Override\n    public void dispose() {"),
    ],
    "ReferenceDisposable.java": [
        ("/**\n * Base class for Disposable containers that manage some other type that\n * has to be run when the container is disposed.\n *\n * @param <T> the type contained\n */", "/**\n * Disposable 容器基类，管理在 dispose 时需执行操作的其它类型。\n *\n * @param <T> 包含的类型\n */"),
    ],
    "RunnableDisposable.java": [
        ("/**\n * A disposable container that manages a {@link Runnable} instance.\n */", "/**\n * 管理 {@link Runnable} 实例的 disposable 容器。\n */"),
        ("    RunnableDisposable(Runnable value) {", "    /** @param value 要在 dispose 时执行的 Runnable */\n    RunnableDisposable(Runnable value) {"),
        ("    @Override\n    protected void onDisposed(@NonNull Runnable value) {", "    /** dispose 时运行所持有的 Runnable。 */\n    @Override\n    protected void onDisposed(@NonNull Runnable value) {"),
        ("    @Override\n    public String toString() {", "    /** 返回包含 disposed 状态与 Runnable 的调试字符串。 */\n    @Override\n    public String toString() {"),
    ],
    "SerialDisposable.java": [
        ("/**\n * A Disposable container that allows atomically updating/replacing the contained\n * Disposable with another Disposable, disposing the old one when updating plus\n * handling the disposition when the container itself is disposed.\n */", "/**\n * Disposable 容器，允许原子地更新/替换所包含的 Disposable；\n * 更新时 dispose 旧实例，并在容器自身 dispose 时处理内部资源。\n */"),
        ("    /**\n     * Constructs an empty SerialDisposable.\n     */", "    /**\n     * 构造空的 SerialDisposable。\n     */"),
        ("    /**\n     * Constructs a SerialDisposable with the given initial Disposable instance.\n     * @param initialDisposable the initial Disposable instance to use, null allowed\n     */", "    /**\n     * 使用给定初始 Disposable 实例构造 SerialDisposable。\n     * @param initialDisposable 要使用的初始 Disposable 实例，允许为 null\n     */"),
        ("    /**\n     * Atomically: set the next disposable on this container and dispose the previous\n     * one (if any) or dispose next if the container has been disposed.\n     * @param next the Disposable to set, may be null\n     * @return true if the operation succeeded, false if the container has been disposed\n     * @see #replace(Disposable)\n     */", "    /**\n     * 原子操作：在本容器上设置下一个 disposable 并 dispose 前一个（若有）；\n     * 若容器已被 dispose 则 dispose next。\n     * @param next 要设置的 Disposable，可为 null\n     * @return 操作成功则为 true；若容器已被 dispose 则为 false\n     * @see #replace(Disposable)\n     */"),
        ("    /**\n     * Atomically: set the next disposable on this container but don't dispose the previous\n     * one (if any) or dispose next if the container has been disposed.\n     * @param next the Disposable to set, may be null\n     * @return true if the operation succeeded, false if the container has been disposed\n     * @see #set(Disposable)\n     */", "    /**\n     * 原子操作：在本容器上设置下一个 disposable 但不 dispose 前一个（若有）；\n     * 若容器已被 dispose 则 dispose next。\n     * @param next 要设置的 Disposable，可为 null\n     * @return 操作成功则为 true；若容器已被 dispose 则为 false\n     * @see #set(Disposable)\n     */"),
        ("    /**\n     * Returns the currently contained Disposable or null if this container is empty.\n     * @return the current Disposable, may be null\n     */", "    /**\n     * 返回当前包含的 Disposable；若本容器为空则返回 null。\n     * @return 当前 Disposable，可为 null\n     */"),
    ],
    "StreamerCancellation.java": [
        ("/**\n * Represents non-disposable view of a {@link DisposableStreamerCancellation}\n * that allows synchronous testing for disposed state as well as allow\n * adding and removing {@link Disposable} resources to be\n * cleaned up when the full container is disposed.\n * <p>\n * This view is provided to prevent calling {@link DisposableStreamerCancellation#dispose()}\n * in {@link Streamable#stream(StreamerCancellation)} implementations because\n * disposing a stream is the privilege of the caller/downstream.\n * <p>\n * Use the {@link #derive()} to create a sub-container with full disposability access.\n * <p>\n * This interface doesn't support {@link DisposableContainer#reset()} nor\n * {@link DisposableContainer#clear()} because it would allow accidentally removing another\n * operator's added/registered {@code Disposable}s.\n * @since 4.0.0\n */", "/**\n * {@link DisposableStreamerCancellation} 的不可 dispose 视图，\n * 允许同步检测 disposed 状态，并允许添加/移除在完整容器 dispose 时需清理的 {@link Disposable} 资源。\n * <p>\n * 提供此视图是为了防止在 {@link Streamable#stream(StreamerCancellation)} 实现中调用\n * {@link DisposableStreamerCancellation#dispose()}，因为 dispose 流是调用方/下游的特权。\n * <p>\n * 使用 {@link #derive()} 创建具有完整 dispose 能力的子容器。\n * <p>\n * 本接口不支持 {@link DisposableContainer#reset()} 与 {@link DisposableContainer#clear()}，\n * 否则会误删其它算子添加/注册的 {@code Disposable}。\n * @since 4.0.0\n */"),
        ("    /**\n     * Returns true if this resource has been disposed.\n     * @return true if this resource has been disposed\n     */", "    /**\n     * 若本资源已被 dispose 则返回 true。\n     * @return 若本资源已被 dispose 则为 true\n     */"),
        ("    /**\n     * Adds a disposable to this container or disposes it if the\n     * container has been disposed.\n     * @param d the disposable to add, not null\n     * @return true if successful, false if this container has been disposed\n     */", "    /**\n     * 向本容器添加 disposable；若容器已被 dispose 则直接 dispose 该 disposable。\n     * @param d 要添加的 disposable，不可为 null\n     * @return 成功则为 true；若本容器已被 dispose 则为 false\n     */"),
        ("    /**\n     * Removes and disposes the given disposable if it is part of this\n     * container.\n     * @param d the disposable to remove and dispose, not null\n     * @return true if the operation was successful\n     */", "    /**\n     * 若给定 disposable 属于本容器，则移除并 dispose 它。\n     * @param d 要移除并 dispose 的 disposable，不可为 null\n     * @return 若操作成功则为 true\n     */"),
        ("    /**\n     * Removes but does not dispose the given disposable if it is part of this\n     * container.\n     * @param d the disposable to remove, not null\n     * @return true if the operation was successful\n     */", "    /**\n     * 若给定 disposable 属于本容器，则移除但不 dispose 它。\n     * @param d 要移除的 disposable，不可为 null\n     * @return 若操作成功则为 true\n     */"),
        ("    /**\n     * Create a derived sub-cancellation management interface that can get disposed\n     * by this cancellation management object but disposing the sub-cancellation\n     * manager does not dispose this current {@code StreamerCancellation} instance..\n     * @return the derived sub-cancellation management object\n     */", "    /**\n     * 创建派生子取消管理接口：可由本取消管理对象 dispose，\n     * 但 dispose 子取消管理器不会 dispose 当前 {@code StreamerCancellation} 实例。\n     * @return 派生的子取消管理对象\n     */"),
    ],
    "SubscriptionDisposable.java": [
        ("/**\n * A Disposable container that handles a {@link Subscription}.\n */", "/**\n * 处理 {@link Subscription} 的 Disposable 容器。\n */"),
        ("    SubscriptionDisposable(Subscription value) {", "    /** @param value 要在 dispose 时 cancel 的 Subscription */\n    SubscriptionDisposable(Subscription value) {"),
        ("    @Override\n    protected void onDisposed(@NonNull Subscription value) {", "    /** dispose 时调用 Subscription.cancel()。 */\n    @Override\n    protected void onDisposed(@NonNull Subscription value) {"),
    ],
    "package-info.java": [
        ("/**\n * Default implementations for {@link io.reactivex.rxjava4.disposables.Disposable Disposable}-based resource management\n * ({@code Disposable} container types) and utility classes to construct\n * {@link io.reactivex.rxjava4.disposables.Disposable Disposables} from callbacks and other types.\n */", "/**\n * 基于 {@link io.reactivex.rxjava4.disposables.Disposable Disposable} 的资源管理\n *（{@code Disposable} 容器类型）的默认实现，以及从回调与其它类型构造\n * {@link io.reactivex.rxjava4.disposables.Disposable Disposables} 的工具类。\n */"),
    ],
    "CompositeException.java": [
        ("/**\n * Represents an exception that is a composite of one or more other exceptions. A {@code CompositeException}\n * does not modify the structure of any exception it wraps, but at print-time it iterates through the list of\n * Throwables contained in the composite in order to print them all.\n *\n * Its invariant is to contain an immutable, ordered (by insertion order), unique list of non-composite\n * exceptions. You can retrieve individual exceptions in this list with {@link #getExceptions()}.\n *\n * The {@link #printStackTrace()} implementation handles the StackTrace in a customized way instead of using\n * {@code getCause()} so that it can avoid circular references.\n *\n * If you invoke {@link #getCause()}, it will lazily create the causal chain but will stop if it finds any\n * Throwable in the chain that it has already seen.\n */", "/**\n * 表示由一个或多个其它异常组成的复合异常。{@code CompositeException}\n * 不修改其所包装异常的结构，但在打印时会遍历复合体中包含的 Throwable 列表并依次打印。\n *\n * 其不变量为：包含不可变、有序（按插入顺序）、唯一的非复合异常列表。\n * 可通过 {@link #getExceptions()} 获取列表中的各个异常。\n *\n * {@link #printStackTrace()} 实现以定制方式处理 StackTrace，而非使用 {@code getCause()}，\n * 以避免循环引用。\n *\n * 若调用 {@link #getCause()}，将惰性创建因果链，但若在链中发现已见过的 Throwable 则停止。\n */"),
        ("    /**\n     * Constructs a CompositeException with the given array of Throwables as the\n     * list of suppressed exceptions.\n     * @param exceptions the Throwables to have as initially suppressed exceptions\n     *\n     * @throws IllegalArgumentException if <code>exceptions</code> is empty.\n     */", "    /**\n     * 使用给定 Throwable 数组作为初始 suppressed 异常列表构造 CompositeException。\n     * @param exceptions 作为初始 suppressed 异常的 Throwable\n     *\n     * @throws IllegalArgumentException 若 <code>exceptions</code> 为空。\n     */"),
        ("    /**\n     * Constructs a CompositeException with the given array of Throwables as the\n     * list of suppressed exceptions.\n     * @param errors the Throwables to have as initially suppressed exceptions\n     *\n     * @throws IllegalArgumentException if <code>errors</code> is empty.\n     */", "    /**\n     * 使用给定 Throwable 集合作为初始 suppressed 异常列表构造 CompositeException。\n     * @param errors 作为初始 suppressed 异常的 Throwable\n     *\n     * @throws IllegalArgumentException 若 <code>errors</code> 为空。\n     */"),
        ("    /**\n     * Retrieves the list of exceptions that make up the {@code CompositeException}.\n     *\n     * @return the exceptions that make up the {@code CompositeException}, as a {@link List} of {@link Throwable}s\n     */", "    /**\n     * 获取构成 {@code CompositeException} 的异常列表。\n     *\n     * @return 构成 {@code CompositeException} 的异常，以 {@link List}{@code <}{@link Throwable}{@code >} 形式返回\n     */"),
        ("    /**\n     * All of the following {@code printStackTrace} functionality is derived from JDK {@link Throwable}\n     * {@code printStackTrace}. In particular, the {@code PrintStreamOrWriter} abstraction is copied wholesale.\n     *\n     * Changes from the official JDK implementation:<ul>\n     * <li>no infinite loop detection</li>\n     * <li>smaller critical section holding {@link PrintStream} lock</li>\n     * <li>explicit knowledge about the exceptions {@link List} that this loops through</li>\n     * </ul>\n     */", "    /**\n     * 以下 {@code printStackTrace} 功能源自 JDK {@link Throwable} 的 {@code printStackTrace}。\n     * 尤其是 {@code PrintStreamOrWriter} 抽象被完整复制。\n     *\n     * 与官方 JDK 实现的差异：<ul>\n     * <li>无无限循环检测</li>\n     * <li>持有 {@link PrintStream} 锁的临界区更小</li>\n     * <li>显式知晓所遍历的异常 {@link List}</li>\n     * </ul>\n     */"),
        ("    /**\n     * Special handling for printing out a {@code CompositeException}.\n     * Loops through all inner exceptions and prints them out.\n     *\n     * @param output\n     *            stream to print to\n     */", "    /**\n     * 打印 {@code CompositeException} 的特殊处理。\n     * 遍历所有内部异常并打印。\n     *\n     * @param output\n     *            要打印到的流\n     */"),
        ("        /**\n         * Prints the object's string representation via the underlying PrintStream or PrintWriter.\n         * @param o the object to print\n         * @return this\n         */", "        /**\n         * 通过底层 PrintStream 或 PrintWriter 打印对象的字符串表示。\n         * @param o 要打印的对象\n         * @return this\n         */"),
        ("    /**\n     * Same abstraction and implementation as in JDK to allow PrintStream and PrintWriter to share implementation.\n     */", "    /**\n     * 与 JDK 中相同的抽象与实现，使 PrintStream 与 PrintWriter 可共享实现。\n     */", True),
        ("    /**\n     * Contains a formatted message with a simplified representation of the exception graph\n     * contained within the CompositeException.\n     */", "    /**\n     * 包含格式化消息，以简化形式表示 CompositeException 内的异常图。\n     */"),
        ("    /**\n     * Returns the number of suppressed exceptions.\n     * @return the number of suppressed exceptions\n     */", "    /**\n     * 返回 suppressed 异常的数量。\n     * @return suppressed 异常的数量\n     */"),
    ],
    "Exceptions.java": [
        ("/**\n * Utility class to help propagate checked exceptions and rethrow exceptions\n * designated as fatal.\n */", "/**\n * 帮助传播 checked 异常并重新抛出被标记为致命的异常的工具类。\n */"),
        ("    /** Utility class. */", "    /** 工具类。 */"),
        ("    /**\n     * Convenience method to throw a {@code RuntimeException} and {@code Error} directly\n     * or wrap any other exception type into a {@link CompletionException}.\n     * @param t the exception to throw directly or wrapped\n     * @return because {@code propagate} itself throws an exception or error, this is a sort of phantom return\n     *         value; {@code propagate} does not actually return anything\n     */", "    /**\n     * 便捷方法：直接抛出 {@code RuntimeException} 与 {@code Error}，\n     * 或将其它异常类型包装为 {@link CompletionException}。\n     * @param t 要直接抛出或包装的异常\n     * @return 由于 {@code propagate} 本身会抛出异常或错误，此为一种“幻影”返回值；\n     *         {@code propagate} 实际上不会返回任何值\n     */"),
        ("        /*\n         * The return type of RuntimeException is a trick for code to be like this:\n         *\n         * throw Exceptions.propagate(e);\n         *\n         * Even though nothing will return and throw via that 'throw', it allows the code to look like it\n         * so it's easy to read and understand that it will always result in a throw.\n         */", "        /*\n         * 返回类型 RuntimeException 是一种技巧，使代码可写成：\n         *\n         * throw Exceptions.propagate(e);\n         *\n         * 尽管该 throw 不会真正返回，但代码形式更易读，\n         * 便于理解其始终会导致抛出。\n         */"),
        ("    /**\n     * Throws a particular {@code Throwable} only if it belongs to a set of \"fatal\" error varieties. These\n     * varieties are as follows:\n     * <ul>\n     * <li>{@code VirtualMachineError}</li>\n     * <li>{@code ThreadDeath}</li>\n     * <li>{@code LinkageError}</li>\n     * </ul>\n     * This can be useful if you are writing an operator that calls user-supplied code, and you want to\n     * notify subscribers of errors encountered in that code by calling their {@code onError} methods, but only\n     * if the errors are not so catastrophic that such a call would be futile, in which case you simply want to\n     * rethrow the error.\n     *\n     * @param t\n     *         the {@code Throwable} to test and perhaps throw\n     * @see <a href=\"https://github.com/ReactiveX/RxJava/issues/748#issuecomment-32471495\">RxJava: StackOverflowError is swallowed (Issue #748)</a>\n     */", "    /**\n     * 仅当特定 {@code Throwable} 属于一组“致命”错误类型时才抛出。这些类型包括：\n     * <ul>\n     * <li>{@code VirtualMachineError}</li>\n     * <li>{@code ThreadDeath}</li>\n     * <li>{@code LinkageError}</li>\n     * </ul>\n     * 若编写调用用户代码的算子，并希望通过调用订阅者的 {@code onError} 方法通知其中遇到的错误，\n     * 但仅当错误尚未严重到此类调用无意义时，本方法很有用；否则应直接重新抛出错误。\n     *\n     * @param t\n     *         要测试并可能抛出的 {@code Throwable}\n     * @see <a href=\"https://github.com/ReactiveX/RxJava/issues/748#issuecomment-32471495\">RxJava: StackOverflowError is swallowed (Issue #748)</a>\n     */"),
    ],
    "MissingBackpressureException.java": [
        ("/**\n * Indicates that an operator attempted to emit a value but the downstream wasn't ready for it.\n */", "/**\n * 表示算子尝试发射值，但下游尚未准备好接收。\n */"),
        ("    /**\n     * The default error message.\n     * <p>\n     * This can happen if the downstream doesn't call {@link java.util.concurrent.Flow.Subscription#request(long)}\n     * in time or at all.\n     * @since 3.1.6\n     */", "    /**\n     * 默认错误消息。\n     * <p>\n     * 若下游未及时或未调用 {@link java.util.concurrent.Flow.Subscription#request(long)} 可能发生此情况。\n     * @since 3.1.6\n     */"),
        ("    /**\n     * Constructs a MissingBackpressureException without message or cause.\n     */", "    /**\n     * 构造无消息与 cause 的 MissingBackpressureException。\n     */"),
        ("    /**\n     * Constructs a MissingBackpressureException with the given message but no cause.\n     * @param message the error message\n     */", "    /**\n     * 使用给定消息、无 cause 构造 MissingBackpressureException。\n     * @param message 错误消息\n     */"),
        ("    /**\n     * Constructs a new {@code MissingBackpressureException} with the\n     * default message {@value #DEFAULT_MESSAGE}.\n     * @return the new {@code MissingBackpressureException} instance.\n     * @since 3.1.6\n     */", "    /**\n     * 使用默认消息 {@value #DEFAULT_MESSAGE} 构造新的 {@code MissingBackpressureException}。\n     * @return 新的 {@code MissingBackpressureException} 实例。\n     * @since 3.1.6\n     */"),
    ],
    "OnErrorNotImplementedException.java": [
        ("/**\n * Represents an exception used to signal to the {@code RxJavaPlugins.onError()} that a\n * callback-based subscribe() method on a base reactive type didn't specify\n * an onError handler.\n * <p>History: 2.0.6 - experimental; 2.1 - beta\n * @since 2.2\n */", "/**\n * 表示用于向 {@code RxJavaPlugins.onError()} 发出信号的异常：\n * 基础响应式类型的基于回调的 subscribe() 方法未指定 onError 处理器。\n * <p>History: 2.0.6 - experimental; 2.1 - beta\n * @since 2.2\n */"),
        ("    /**\n     * Customizes the {@code Throwable} with a custom message and wraps it before it\n     * is signaled to the {@code RxJavaPlugins.onError()} handler as {@code OnErrorNotImplementedException}.\n     *\n     * @param message\n     *          the message to assign to the {@code Throwable} to signal\n     * @param e\n     *          the {@code Throwable} to signal; if null, a NullPointerException is constructed\n     */", "    /**\n     * 使用自定义消息定制 {@code Throwable} 并包装，\n     * 再以 {@code OnErrorNotImplementedException} 形式通知 {@code RxJavaPlugins.onError()} 处理器。\n     *\n     * @param message\n     *          要赋给待通知 {@code Throwable} 的消息\n     * @param e\n     *          要通知的 {@code Throwable}；若为 null 则构造 NullPointerException\n     */"),
        ("    /**\n     * Wraps the {@code Throwable} before it\n     * is signaled to the {@code RxJavaPlugins.onError()}\n     * handler as {@code OnErrorNotImplementedException}.\n     *\n     * @param e\n     *          the {@code Throwable} to signal; if null, a NullPointerException is constructed\n     */", "    /**\n     * 在将 {@code Throwable} 以 {@code OnErrorNotImplementedException} 形式\n     * 通知 {@code RxJavaPlugins.onError()} 处理器之前进行包装。\n     *\n     * @param e\n     *          要通知的 {@code Throwable}；若为 null 则构造 NullPointerException\n     */"),
    ],
    "ProtocolViolationException.java": [
        ("/**\n * Explicitly named exception to indicate a Reactive-Streams\n * protocol violation.\n * <p>History: 2.0.6 - experimental; 2.1 - beta\n * @since 2.2\n */", "/**\n * 显式命名的异常，表示 Reactive-Streams 协议违规。\n * <p>History: 2.0.6 - experimental; 2.1 - beta\n * @since 2.2\n */"),
        ("    /**\n     * Creates an instance with the given message.\n     * @param message the message\n     */", "    /**\n     * 使用给定消息创建实例。\n     * @param message 消息\n     */"),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str] | tuple[str, str, bool]]) -> str:
    for item in replacements:
        if len(item) == 3:
            old, new, replace_all = item
        else:
            old, new = item
            replace_all = False
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new) if replace_all else text.replace(old, new, 1)
    return text


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        reps = FILE_REPLACEMENTS.get(name, [])
        if not src.exists() or not reps:
            failures.append(rel)
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        try:
            text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
            if len(re.findall(r"[\u4e00-\u9fff]", text)) < 10:
                failures.append(f"cn<{rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
        except Exception as e:
            failures.append(f"{rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
