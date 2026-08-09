"""Replacement tuples for RxJava wave-23a single operators [0:15]."""

SINGLE_W23A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SingleFromUnsafeSource.java": [
        (
            "import io.reactivex.rxjava4.core.*;\n\npublic final class SingleFromUnsafeSource",
            "import io.reactivex.rxjava4.core.*;\n\n"
            "/**\n"
            " * 将 {@link SingleSource} 包装为 {@link Single}，不做线程安全或生命周期校验。\n"
            " * 直接透传 subscribe 调用，供内部 unsafe 场景使用。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleFromUnsafeSource",
        ),
        (
            "    public SingleFromUnsafeSource(SingleSource<T> source) {",
            "    /** @param source 待包装的上游 SingleSource */\n"
            "    public SingleFromUnsafeSource(SingleSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 直接调用 source.subscribe，无额外拦截或调度。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
    ],
    "SingleHide.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class SingleHide",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 隐藏上游 Disposable：downstream 的 onSubscribe 收到 HideSingleObserver 自身。\n"
            " * dispose 时转发至 upstream，防止下游直接操作上游订阅。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleHide",
        ),
        (
            "    public SingleHide(SingleSource<? extends T> source) {",
            "    /** @param source 上游 SingleSource */\n"
            "    public SingleHide(SingleSource<? extends T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 用 HideSingleObserver 包装 downstream 后订阅 source。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class HideSingleObserver<T> implements SingleObserver<T>, Disposable {",
            "    /** 拦截 onSubscribe 将自身作为 Disposable 暴露给 downstream。 */\n"
            "    static final class HideSingleObserver<T> implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 转发 dispose 至 upstream。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        @Override\n        public void onSubscribe(Disposable d) {",
            "        /** validate 成功后保存 upstream 并向 downstream 传递 this。 */\n"
            "        @Override\n        public void onSubscribe(Disposable d) {",
        ),
    ],
    "SingleInternalHelper.java": [
        (
            "/**\n * Helper utility class to support Single with inner classes.\n */\npublic final class SingleInternalHelper {",
            "/**\n"
            " * Single 内部工具类：提供 emptyThrower、toFlowable 及 iterableToFlowable 等辅助。\n"
            " * 不可实例化。\n"
            " */\n"
            "public final class SingleInternalHelper {",
        ),
        (
            "    /** Utility class. */\n    private SingleInternalHelper() {",
            "    /** 工具类私有构造，禁止实例化。 */\n"
            "    private SingleInternalHelper() {",
        ),
        (
            "    enum NoSuchElementSupplier implements Supplier<NoSuchElementException> {",
            "    /** 单例 Supplier，每次 get 返回新的 NoSuchElementException。 */\n"
            "    enum NoSuchElementSupplier implements Supplier<NoSuchElementException> {",
        ),
        (
            "    public static Supplier<NoSuchElementException> emptyThrower() {",
            "    /** 返回用于 empty 场景抛出 NoSuchElementException 的 Supplier 单例。 */\n"
            "    public static Supplier<NoSuchElementException> emptyThrower() {",
        ),
        (
            "    enum ToFlowable implements Function<SingleSource, Publisher> {",
            "    /** 将 SingleSource 映射为 SingleToFlowable 的 Function 单例。 */\n"
            "    enum ToFlowable implements Function<SingleSource, Publisher> {",
        ),
        (
            "    public static <T> Function<SingleSource<? extends T>, Publisher<? extends T>> toFlowable() {",
            "    /** 返回将 SingleSource 转为 Publisher 的泛型 Function。 */\n"
            "    public static <T> Function<SingleSource<? extends T>, Publisher<? extends T>> toFlowable() {",
        ),
        (
            "    static final class ToFlowableIterator<T> implements Iterator<Flowable<T>> {",
            "    /** 遍历 SingleSource 迭代器，next 时包装为 SingleToFlowable。 */\n"
            "    static final class ToFlowableIterator<T> implements Iterator<Flowable<T>> {",
        ),
        (
            "        @Override\n        public void remove() {",
            "        /** 不支持 remove，调用即抛 UnsupportedOperationException。 */\n"
            "        @Override\n        public void remove() {",
        ),
        (
            "    static final class ToFlowableIterable<T> implements Iterable<Flowable<T>> {",
            "    /** 将 Iterable&lt;SingleSource&gt; 转为 Iterable&lt;Flowable&gt; 的包装。 */\n"
            "    static final class ToFlowableIterable<T> implements Iterable<Flowable<T>> {",
        ),
        (
            "    public static <T> Iterable<? extends Flowable<T>> iterableToFlowable(final Iterable<? extends SingleSource<? extends T>> sources) {",
            "    /** 将 sources 包装为 ToFlowableIterable，逐项转为 Flowable。 */\n"
            "    public static <T> Iterable<? extends Flowable<T>> iterableToFlowable(final Iterable<? extends SingleSource<? extends T>> sources) {",
        ),
    ],
    "SingleJust.java": [
        (
            "import io.reactivex.rxjava4.disposables.Disposable;\n\npublic final class SingleJust",
            "import io.reactivex.rxjava4.disposables.Disposable;\n\n"
            "/**\n"
            " * 立即向订阅者发射常量 value 的 Single。\n"
            " * onSubscribe 使用已 disposed 的 Disposable，随后同步 onSuccess。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleJust",
        ),
        (
            "    public SingleJust(T value) {",
            "    /** @param value 要发射的常量值 */\n"
            "    public SingleJust(T value) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 先 onSubscribe(Disposable.disposed())，再同步 onSuccess(value)。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
    ],
    "SingleLift.java": [
        (
            "import java.util.Objects;\n\npublic final class SingleLift",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 通过 {@link SingleOperator} 变换下游 SingleObserver 后订阅上游。\n"
            " * onLift 返回 null 或抛异常时以 EmptyDisposable.error 终止。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 下游元素类型\n"
            " */\n"
            "public final class SingleLift",
        ),
        (
            "    public SingleLift(SingleSource<T> source, SingleOperator<? extends R, ? super T> onLift) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onLift 将 downstream Observer 变换为 upstream Observer 的算子\n"
            "     */\n"
            "    public SingleLift(SingleSource<T> source, SingleOperator<? extends R, ? super T> onLift) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
            "    /** 应用 onLift 得到 sr，成功则 source.subscribe(sr)。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
        ),
    ],
    "SingleMap.java": [
        (
            "import java.util.Objects;\n\npublic final class SingleMap",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 对上游 onSuccess 值应用 mapper 后转发给 downstream。\n"
            " * mapper 返回 null 或抛异常时走 onError 路径。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 映射后元素类型\n"
            " */\n"
            "public final class SingleMap",
        ),
        (
            "    public SingleMap(SingleSource<? extends T> source, Function<? super T, ? extends R> mapper) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param mapper 成功值的映射函数\n"
            "     */\n"
            "    public SingleMap(SingleSource<? extends T> source, Function<? super T, ? extends R> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super R> t) {",
            "    /** 订阅 MapSingleObserver 拦截 onSuccess 并应用 mapper。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super R> t) {",
        ),
        (
            "    record MapSingleObserver<T, R>(SingleObserver<? super R> t,\n                                   Function<? super T, ? extends R> mapper) implements SingleObserver<T> {",
            "    /** 成功时 mapper.apply；null 或异常转 onError；错误直接转发。 */\n"
            "    record MapSingleObserver<T, R>(SingleObserver<? super R> t,\n                                   Function<? super T, ? extends R> mapper) implements SingleObserver<T> {",
        ),
        (
            "            public void onSuccess(T value) {",
            "            /** 应用 mapper，非 null 结果则 t.onSuccess(v)。 */\n"
            "            public void onSuccess(T value) {",
        ),
    ],
    "SingleMaterialize.java": [
        (
            "/**\n * Turn the signal types of a Single source into a single Notification of\n * equal kind.\n * <p>History: 2.2.4 - experimental\n *\n * @param <T> the element type of the source\n * @since 3.0.0\n */\npublic final class SingleMaterialize",
            "/**\n"
            " * 将上游 Single 的终端信号封装为 {@link Notification} 后发射。\n"
            " * 成功 → Notification.createOnNext；错误 → Notification.createOnError。\n"
            " * @param <T> 上游元素类型\n"
            " * @since 3.0.0\n"
            " */\n"
            "public final class SingleMaterialize",
        ),
        (
            "    public SingleMaterialize(Single<T> source) {",
            "    /** @param source 待物化的上游 Single */\n"
            "    public SingleMaterialize(Single<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Notification<T>> observer) {",
            "    /** 用 MaterializeSingleObserver 将 onSuccess/onError 转为 Notification。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Notification<T>> observer) {",
        ),
    ],
    "SingleNever.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;\n\npublic final class SingleNever",
            "import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;\n\n"
            "/**\n"
            " * 永不发射 onSuccess/onError 的 Single 单例。\n"
            " * 订阅时仅 onSubscribe(EmptyDisposable.NEVER)。\n"
            " */\n"
            "public final class SingleNever",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Object> observer) {",
            "    /** 向 observer 传递 EmptyDisposable.NEVER，不触发终端事件。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Object> observer) {",
        ),
    ],
    "SingleObserveOn.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class SingleObserveOn",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 在指定 {@link Scheduler} 上转发上游 onSuccess/onError。\n"
            " * 收到终端事件后 scheduleDirect(this) 异步通知 downstream。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleObserveOn",
        ),
        (
            "    public SingleObserveOn(SingleSource<T> source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param scheduler 执行下游回调的调度器\n"
            "     */\n"
            "    public SingleObserveOn(SingleSource<T> source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 ObserveOnSingleObserver 缓存事件后调度转发。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class ObserveOnSingleObserver<T> extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable, Runnable {",
            "    /** 缓存 value/error，run 时在 scheduler 线程转发给 downstream。 */\n"
            "    static final class ObserveOnSingleObserver<T> extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 保存 value 并 scheduleDirect(this) 替换当前 Disposable。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 保存 error 并 scheduleDirect(this) 替换当前 Disposable。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 有 error 则 onError，否则 onSuccess(value)。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "SingleOnErrorComplete.java": [
        (
            "/**\n * Emits an onComplete if the source emits an onError and the predicate returns true for\n * that Throwable.\n * \n * @param <T> the value type\n * @since 3.0.0\n */\npublic final class SingleOnErrorComplete",
            "/**\n"
            " * 上游 onError 且 predicate 对 Throwable 返回 true 时转为 Maybe 的 onComplete。\n"
            " * predicate 为 false 时正常转发 onError；成功仍 onSuccess。\n"
            " * @param <T> 元素类型\n"
            " * @since 3.0.0\n"
            " */\n"
            "public final class SingleOnErrorComplete",
        ),
        (
            "    public SingleOnErrorComplete(Single<T> source,\n            Predicate<? super Throwable> predicate) {",
            "    /**\n"
            "     * @param source 上游 Single\n"
            "     * @param predicate 判定是否吞掉错误并 onComplete 的谓词\n"
            "     */\n"
            "    public SingleOnErrorComplete(Single<T> source,\n            Predicate<? super Throwable> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 复用 MaybeOnErrorComplete.OnErrorCompleteMultiObserver 处理错误完成逻辑。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
    ],
    "SingleOnErrorReturn.java": [
        (
            "import io.reactivex.rxjava4.functions.Function;\n\npublic final class SingleOnErrorReturn",
            "import io.reactivex.rxjava4.functions.Function;\n\n"
            "/**\n"
            " * 上游 onError 时用 valueSupplier 或固定 value 转为 onSuccess。\n"
            " * valueSupplier 异常合并 CompositeException；结果为 null 则 NPE 链式转发。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleOnErrorReturn",
        ),
        (
            "    public SingleOnErrorReturn(SingleSource<? extends T> source,\n            Function<? super Throwable, ? extends T> valueSupplier, T value) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param valueSupplier 按错误计算替代值的函数，可为 null\n"
            "     * @param value valueSupplier 为 null 时使用的固定替代值\n"
            "     */\n"
            "    public SingleOnErrorReturn(SingleSource<? extends T> source,\n            Function<? super Throwable, ? extends T> valueSupplier, T value) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 OnErrorReturn 拦截 onError 并转为 onSuccess。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    final class OnErrorReturn implements SingleObserver<T> {",
            "    /** onError 时计算替代值；成功/onSubscribe 直接透传。 */\n"
            "    final class OnErrorReturn implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** valueSupplier 或固定 value；null 则 NPE.initCause(e) 转发。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "SingleResumeNext.java": [
        (
            "import io.reactivex.rxjava4.internal.observers.ResumeSingleObserver;\n\npublic final class SingleResumeNext",
            "import io.reactivex.rxjava4.internal.observers.ResumeSingleObserver;\n\n"
            "/**\n"
            " * 上游 onError 时由 nextFunction 返回备用 SingleSource 并订阅恢复。\n"
            " * nextFunction 异常则 CompositeException 合并转发。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleResumeNext",
        ),
        (
            "    public SingleResumeNext(SingleSource<? extends T> source,\n            Function<? super Throwable, ? extends SingleSource<? extends T>> nextFunction) {",
            "    /**\n"
            "     * @param source 主 SingleSource\n"
            "     * @param nextFunction 错误时选择备用 SingleSource 的函数\n"
            "     */\n"
            "    public SingleResumeNext(SingleSource<? extends T> source,\n            Function<? super Throwable, ? extends SingleSource<? extends T>> nextFunction) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 ResumeMainSingleObserver 在 onError 时切换备用源。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class ResumeMainSingleObserver<T> extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable {",
            "    /** onError 时 apply nextFunction 并用 ResumeSingleObserver 订阅备用源。 */\n"
            "    static final class ResumeMainSingleObserver<T> extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** nextFunction 得 source 后 ResumeSingleObserver 桥接订阅。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "SingleSubscribeOn.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.*;\n\npublic final class SingleSubscribeOn",
            "import io.reactivex.rxjava4.internal.disposables.*;\n\n"
            "/**\n"
            " * 在指定 {@link Scheduler} 上异步订阅上游 SingleSource。\n"
            " * 先向 downstream 传递 SubscribeOnObserver，再 scheduleDirect 执行 source.subscribe。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleSubscribeOn",
        ),
        (
            "    public SingleSubscribeOn(SingleSource<? extends T> source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param scheduler 执行订阅操作的调度器\n"
            "     */\n"
            "    public SingleSubscribeOn(SingleSource<? extends T> source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 创建 SubscribeOnObserver，onSubscribe 后 scheduleDirect(parent) 异步订阅 source。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class SubscribeOnObserver<T>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable, Runnable {",
            "    /** run 中 source.subscribe(this)；task 管理调度 Disposable。 */\n"
            "    static final class SubscribeOnObserver<T>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 在 scheduler 线程调用 source.subscribe(this)。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "SingleTakeUntil.java": [
        (
            "/**\n * Signals the events of the source Single or signals a CancellationException if the\n * other Publisher signaled first.\n * @param <T> the main value type\n * @param <U> the other value type\n */\npublic final class SingleTakeUntil",
            "/**\n"
            " * 竞速上游 Single 与 other {@link Publisher}：other 先发出或完成则 CancellationException。\n"
            " * source 先成功则 dispose other 并 onSuccess；任一侧错误按规则转发。\n"
            " * @param <T> 主 Single 元素类型\n"
            " * @param <U> other Publisher 元素类型\n"
            " */\n"
            "public final class SingleTakeUntil",
        ),
        (
            "    public SingleTakeUntil(SingleSource<T> source, Publisher<U> other) {",
            "    /**\n"
            "     * @param source 主 SingleSource\n"
            "     * @param other 用于竞速取消的 Publisher\n"
            "     */\n"
            "    public SingleTakeUntil(SingleSource<T> source, Publisher<U> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 同时订阅 TakeUntilMainObserver 与 other 的 TakeUntilOtherSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class TakeUntilMainObserver<T>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable {",
            "    /** 主 Single 观察者：成功时 dispose other；错误/other 先动则取消或转发。 */\n"
            "    static final class TakeUntilMainObserver<T>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** dispose other 后若未 DISPOSED 则 downstream.onSuccess。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        void otherError(Throwable e) {",
            "        /** other 触发错误/取消：dispose 主源并以 e 终止 downstream。 */\n"
            "        void otherError(Throwable e) {",
        ),
        (
            "    static final class TakeUntilOtherSubscriber\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<Object> {",
            "    /** other 侧订阅者：onNext/onComplete 取消并 parent.otherError(CancellationException)。 */\n"
            "    static final class TakeUntilOtherSubscriber\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<Object> {",
        ),
        (
            "        @Override\n        public void onNext(Object t) {",
            "        /** cancel 成功后 parent.otherError(CancellationException)。 */\n"
            "        @Override\n        public void onNext(Object t) {",
        ),
    ],
    "SingleTimeInterval.java": [
        (
            "/**\n * Measures the time between subscription and the success item emission\n * from the upstream and emits this as a {@link Timed} success value.\n * @param <T> the element type of the sequence\n * @since 3.0.0\n */\npublic final class SingleTimeInterval",
            "/**\n"
            " * 测量订阅到上游 onSuccess 的耗时，封装为 {@link Timed} 发射。\n"
            " * start 为 true 时在 onSubscribe 时记录起点；否则从 0 起算。\n"
            " * @param <T> 上游元素类型\n"
            " * @since 3.0.0\n"
            " */\n"
            "public final class SingleTimeInterval",
        ),
        (
            "    public SingleTimeInterval(SingleSource<T> source, TimeUnit unit, Scheduler scheduler, boolean start) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 提供 now(unit) 的调度器\n"
            "     * @param start 是否在 onSubscribe 时记录 startTime\n"
            "     */\n"
            "    public SingleTimeInterval(SingleSource<T> source, TimeUnit unit, Scheduler scheduler, boolean start) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(@NonNull SingleObserver<? super @NonNull Timed<T>> observer) {",
            "    /** 订阅 TimeIntervalSingleObserver 在 onSuccess 时包装 Timed。 */\n"
            "    @Override\n    protected void subscribeActual(@NonNull SingleObserver<? super @NonNull Timed<T>> observer) {",
        ),
        (
            "    static final class TimeIntervalSingleObserver<T> implements SingleObserver<T>, Disposable {",
            "    /** onSuccess 时发射 Timed(value, now-startTime, unit)。 */\n"
            "    static final class TimeIntervalSingleObserver<T> implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(@NonNull T t) {",
            "        /** 计算 elapsed 并 downstream.onSuccess(new Timed<>(t, elapsed, unit))。 */\n"
            "        @Override\n        public void onSuccess(@NonNull T t) {",
        ),
    ],
}
