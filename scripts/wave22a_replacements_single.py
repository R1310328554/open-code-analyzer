"""Replacement tuples for RxJava wave-22a single operators [0:15]."""

SINGLE_W22A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SingleDelay.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\npublic final class SingleDelay",
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\n"
            "/**\n"
            " * 在指定 {@link Scheduler} 上延迟转发上游 Single 的 onSuccess/onError。\n"
            " * delayError 为 true 时错误同样延迟；否则错误立即转发。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleDelay",
        ),
        (
            "    public SingleDelay(SingleSource<? extends T> source, long time, TimeUnit unit, Scheduler scheduler, boolean delayError) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param time 延迟时长\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 执行延迟任务的调度器\n"
            "     * @param delayError 是否在 onError 时也应用延迟\n"
            "     */\n"
            "    public SingleDelay(SingleSource<? extends T> source, long time, TimeUnit unit, Scheduler scheduler, boolean delayError) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 用 SequentialDisposable 串联上游与调度任务。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    final class Delay implements SingleObserver<T> {",
            "    /** 接收上游事件后 scheduleDirect 延迟 Runnable 转发。 */\n"
            "    final class Delay implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(final T value) {",
            "        /** 调度 OnSuccess 在 time/unit 后 downstream.onSuccess。 */\n"
            "        @Override\n        public void onSuccess(final T value) {",
        ),
        (
            "        @Override\n        public void onError(final Throwable e) {",
            "        /** delayError 时延迟转发；否则 time=0 立即 onError。 */\n"
            "        @Override\n        public void onError(final Throwable e) {",
        ),
        (
            "        final class OnSuccess implements Runnable {",
            "        /** 延迟任务：向 downstream 发射成功值。 */\n"
            "        final class OnSuccess implements Runnable {",
        ),
        (
            "        final class OnError implements Runnable {",
            "        /** 延迟任务：向 downstream 转发错误。 */\n"
            "        final class OnError implements Runnable {",
        ),
    ],
    "SingleDelayWithCompletable.java": [
        (
            "import io.reactivex.rxjava4.internal.observers.ResumeSingleObserver;\n\npublic final class SingleDelayWithCompletable",
            "import io.reactivex.rxjava4.internal.observers.ResumeSingleObserver;\n\n"
            "/**\n"
            " * 先订阅 other {@link CompletableSource}，其 onComplete 后再订阅 source Single。\n"
            " * other 出错则直接 onError，不订阅 source。\n"
            " * @param <T> 目标 Single 元素类型\n"
            " */\n"
            "public final class SingleDelayWithCompletable",
        ),
        (
            "    public SingleDelayWithCompletable(SingleSource<T> source, CompletableSource other) {",
            "    /**\n"
            "     * @param source 待延迟订阅的 SingleSource\n"
            "     * @param other 必须先完成的 CompletableSource\n"
            "     */\n"
            "    public SingleDelayWithCompletable(SingleSource<T> source, CompletableSource other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 other，完成后再 ResumeSingleObserver 订阅 source。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class OtherObserver<T>\n    extends AtomicReference<Disposable>\n    implements CompletableObserver, Disposable {",
            "    /** Completable 观察者：onComplete 时 ResumeSingleObserver 订阅 source。 */\n"
            "    static final class OtherObserver<T>\n    extends AtomicReference<Disposable>\n    implements CompletableObserver, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** other 完成后用 ResumeSingleObserver 桥接 source。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "SingleDelayWithObservable.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleDelayWithObservable",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 先订阅 other {@link ObservableSource}，收到首个 onNext 或 onComplete 后再订阅 source Single。\n"
            " * other 出错则转发 onError；done 门控避免重复订阅 source。\n"
            " * @param <T> 目标 Single 元素类型\n"
            " * @param <U> other Observable 元素类型\n"
            " */\n"
            "public final class SingleDelayWithObservable",
        ),
        (
            "    public SingleDelayWithObservable(SingleSource<T> source, ObservableSource<U> other) {",
            "    /**\n"
            "     * @param source 待延迟订阅的 SingleSource\n"
            "     * @param other 必须先发出或完成的 ObservableSource\n"
            "     */\n"
            "    public SingleDelayWithObservable(SingleSource<T> source, ObservableSource<U> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 OtherSubscriber 监听 other 的首事件或完成。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class OtherSubscriber<T, U>\n    extends AtomicReference<Disposable>\n    implements Observer<U>, Disposable {",
            "    /** other 的 Observer：onNext 时 dispose 并触发 onComplete 逻辑。 */\n"
            "    static final class OtherSubscriber<T, U>\n    extends AtomicReference<Disposable>\n    implements Observer<U>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(U value) {",
            "        /** 收到首元素后 dispose 自身并走 onComplete 路径订阅 source。 */\n"
            "        @Override\n        public void onNext(U value) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** done 门控后 ResumeSingleObserver 订阅 source。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "SingleDelayWithPublisher.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleDelayWithPublisher",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 先订阅 other {@link Publisher}，收到首个 onNext 或 onComplete 后再订阅 source Single。\n"
            " * 通过 request(Long.MAX_VALUE) 无界拉取 other；dispose 时 cancel 上游。\n"
            " * @param <T> 目标 Single 元素类型\n"
            " * @param <U> other Publisher 元素类型\n"
            " */\n"
            "public final class SingleDelayWithPublisher",
        ),
        (
            "    public SingleDelayWithPublisher(SingleSource<T> source, Publisher<U> other) {",
            "    /**\n"
            "     * @param source 待延迟订阅的 SingleSource\n"
            "     * @param other 必须先发出或完成的 Publisher\n"
            "     */\n"
            "    public SingleDelayWithPublisher(SingleSource<T> source, Publisher<U> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 OtherSubscriber 并 request 无界等待 other 首事件。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class OtherSubscriber<T, U>\n    extends AtomicReference<Disposable>\n    implements FlowableSubscriber<U>, Disposable {",
            "    /** other 的 FlowableSubscriber：onNext 时 cancel 并触发完成逻辑。 */\n"
            "    static final class OtherSubscriber<T, U>\n    extends AtomicReference<Disposable>\n    implements FlowableSubscriber<U>, Disposable {",
        ),
        (
            "        @Override\n        public void onSubscribe(Subscription s) {",
            "        /** 校验 Subscription 后 request(Long.MAX_VALUE) 无界拉取。 */\n"
            "        @Override\n        public void onSubscribe(Subscription s) {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** cancel 上游 Subscription 并 dispose 自身 Disposable。 */\n"
            "        @Override\n        public void dispose() {",
        ),
    ],
    "SingleDelayWithSingle.java": [
        (
            "import io.reactivex.rxjava4.internal.observers.ResumeSingleObserver;\n\npublic final class SingleDelayWithSingle",
            "import io.reactivex.rxjava4.internal.observers.ResumeSingleObserver;\n\n"
            "/**\n"
            " * 先订阅 other {@link SingleSource}，其 onSuccess 后再订阅 source Single。\n"
            " * other 出错则直接 onError，不订阅 source。\n"
            " * @param <T> 目标 Single 元素类型\n"
            " * @param <U> other Single 元素类型\n"
            " */\n"
            "public final class SingleDelayWithSingle",
        ),
        (
            "    public SingleDelayWithSingle(SingleSource<T> source, SingleSource<U> other) {",
            "    /**\n"
            "     * @param source 待延迟订阅的 SingleSource\n"
            "     * @param other 必须先成功的 SingleSource\n"
            "     */\n"
            "    public SingleDelayWithSingle(SingleSource<T> source, SingleSource<U> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 OtherObserver，other 成功后 ResumeSingleObserver 订阅 source。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class OtherObserver<T, U>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<U>, Disposable {",
            "    /** other 的 SingleObserver：onSuccess 时桥接订阅 source。 */\n"
            "    static final class OtherObserver<T, U>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<U>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(U value) {",
            "        /** other 成功后 ResumeSingleObserver 订阅 source（忽略 U 值）。 */\n"
            "        @Override\n        public void onSuccess(U value) {",
        ),
    ],
    "SingleDematerialize.java": [
        (
            "/**\n * Maps the success value of the source to a Notification, then\n * maps it back to the corresponding signal type.\n * <p>History: 2.2.4 - experimental\n * @param <T> the element type of the source\n * @param <R> the element type of the Notification and result\n * @since 3.0.0\n */\npublic final class SingleDematerialize",
            "/**\n"
            " * 将上游 Single 成功值经 selector 映射为 {@link Notification}，\n"
            " * 再按 Notification 类型转为 Maybe 的 onSuccess/onComplete/onError。\n"
            " * <p>History: 2.2.4 - experimental\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> Notification 与结果元素类型\n"
            " * @since 3.0.0\n"
            " */\n"
            "public final class SingleDematerialize",
        ),
        (
            "    public SingleDematerialize(Single<T> source, Function<? super T, Notification<R>> selector) {",
            "    /**\n"
            "     * @param source 上游 Single\n"
            "     * @param selector 将成功值映射为 Notification 的函数\n"
            "     */\n"
            "    public SingleDematerialize(Single<T> source, Function<? super T, Notification<R>> selector) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
            "    /** 订阅 DematerializeObserver 处理 Notification 分支。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
        ),
        (
            "    static final class DematerializeObserver<T, R> implements SingleObserver<T>, Disposable {",
            "    /** 按 Notification.isOnNext/isOnComplete/isOnError 转发 Maybe 信号。 */\n"
            "    static final class DematerializeObserver<T, R> implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** selector 映射 Notification 后分支转发 onSuccess/onComplete/onError。 */\n"
            "        @Override\n        public void onSuccess(T t) {",
        ),
    ],
    "SingleDetach.java": [
        (
            "/**\n * Breaks the references between the upstream and downstream when the Maybe terminates.\n * <p>History: 2.1.5 - experimental\n * @param <T> the value type\n * @since 2.2\n */\npublic final class SingleDetach",
            "/**\n"
            " * 终止时断开 upstream 与 downstream 的引用，便于 GC 回收观察者链。\n"
            " * onSuccess/onError 后将 downstream 置 null，upstream 置 DISPOSED。\n"
            " * <p>History: 2.1.5 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.2\n"
            " */\n"
            "public final class SingleDetach",
        ),
        (
            "    public SingleDetach(SingleSource<T> source) {",
            "    /** @param source 上游 SingleSource */\n"
            "    public SingleDetach(SingleSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 DetachSingleObserver 在终止时清空引用。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class DetachSingleObserver<T> implements SingleObserver<T>, Disposable {",
            "    /** dispose 时清空 downstream；终止后 upstream 置 DISPOSED。 */\n"
            "    static final class DetachSingleObserver<T> implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 清空引用后转发 onSuccess，避免持有 downstream。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 清空引用后转发 onError。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "SingleDoAfterSuccess.java": [
        (
            "/**\n * Calls a consumer after pushing the current item to the downstream.\n * <p>History: 2.0.1 - experimental\n * @param <T> the value type\n * @since 2.1\n */\npublic final class SingleDoAfterSuccess",
            "/**\n"
            " * 先向 downstream 转发 onSuccess，再调用 onAfterSuccess 消费成功值。\n"
            " * onAfterSuccess 异常无法转 onError（已终止），走 RxJavaPlugins.onError。\n"
            " * <p>History: 2.0.1 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.1\n"
            " */\n"
            "public final class SingleDoAfterSuccess",
        ),
        (
            "    public SingleDoAfterSuccess(SingleSource<T> source, Consumer<? super T> onAfterSuccess) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onAfterSuccess 成功值下发后执行的 Consumer\n"
            "     */\n"
            "    public SingleDoAfterSuccess(SingleSource<T> source, Consumer<? super T> onAfterSuccess) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 DoAfterObserver 在 onSuccess 后触发回调。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class DoAfterObserver<T> implements SingleObserver<T>, Disposable {",
            "    /** onSuccess 顺序：downstream.onSuccess 再 onAfterSuccess.accept。 */\n"
            "    static final class DoAfterObserver<T> implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** 先转发成功值；回调异常走 RxJavaPlugins（不可 onError）。 */\n"
            "        @Override\n        public void onSuccess(T t) {",
        ),
    ],
    "SingleDoAfterTerminate.java": [
        (
            "/**\n * Calls an action after pushing the current item or an error to the downstream.\n * <p>History: 2.0.6 - experimental\n * @param <T> the value type\n * @since 2.1\n */\npublic final class SingleDoAfterTerminate",
            "/**\n"
            " * 在 downstream 收到 onSuccess 或 onError 之后调用 onAfterTerminate。\n"
            " * 回调异常走 RxJavaPlugins.onError，不影响已转发的终端事件。\n"
            " * <p>History: 2.0.6 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.1\n"
            " */\n"
            "public final class SingleDoAfterTerminate",
        ),
        (
            "    public SingleDoAfterTerminate(SingleSource<T> source, Action onAfterTerminate) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onAfterTerminate 终端事件转发后执行的 Action\n"
            "     */\n"
            "    public SingleDoAfterTerminate(SingleSource<T> source, Action onAfterTerminate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 DoAfterTerminateObserver 在成功或错误后触发 Action。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class DoAfterTerminateObserver<T> implements SingleObserver<T>, Disposable {",
            "    /** onSuccess/onError 均先转发再 onAfterTerminate()。 */\n"
            "    static final class DoAfterTerminateObserver<T> implements SingleObserver<T>, Disposable {",
        ),
        (
            "        private void onAfterTerminate() {",
            "        /** 执行 Action；异常经 RxJavaPlugins 上报。 */\n"
            "        private void onAfterTerminate() {",
        ),
    ],
    "SingleDoFinally.java": [
        (
            "/**\n * Execute an action after an onSuccess, onError or a dispose event.\n * <p>History: 2.0.1 - experimental\n * @param <T> the value type\n * @since 2.1\n */\npublic final class SingleDoFinally",
            "/**\n"
            " * 在 onSuccess、onError 或 dispose 后执行 onFinally，且仅执行一次。\n"
            " * AtomicInteger CAS 门控保证 runFinally 不重复调用。\n"
            " * <p>History: 2.0.1 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.1\n"
            " */\n"
            "public final class SingleDoFinally",
        ),
        (
            "    public SingleDoFinally(SingleSource<T> source, Action onFinally) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onFinally 终止或 dispose 后执行的 Action\n"
            "     */\n"
            "    public SingleDoFinally(SingleSource<T> source, Action onFinally) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 DoFinallyObserver 在三种路径触发 runFinally。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class DoFinallyObserver<T> extends AtomicInteger implements SingleObserver<T>, Disposable {",
            "    /** compareAndSet(0,1) 保证 onFinally 仅执行一次。 */\n"
            "    static final class DoFinallyObserver<T> extends AtomicInteger implements SingleObserver<T>, Disposable {",
        ),
        (
            "        void runFinally() {",
            "        /** CAS 成功后 run onFinally；异常走 RxJavaPlugins。 */\n"
            "        void runFinally() {",
        ),
    ],
    "SingleDoOnDispose.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleDoOnDispose",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 下游 dispose 时先执行 onDispose Action，再 dispose 上游。\n"
            " * AtomicReference 存 Action，getAndSet(null) 保证仅调用一次。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleDoOnDispose",
        ),
        (
            "    public SingleDoOnDispose(SingleSource<T> source, Action onDispose) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onDispose dispose 时执行的 Action\n"
            "     */\n"
            "    public SingleDoOnDispose(SingleSource<T> source, Action onDispose) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 DoOnDisposeObserver 拦截 dispose 路径。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class DoOnDisposeObserver<T>\n    extends AtomicReference<Action>\n    implements SingleObserver<T>, Disposable {",
            "    /** dispose 时 getAndSet Action 并 run，再 upstream.dispose。 */\n"
            "    static final class DoOnDisposeObserver<T>\n    extends AtomicReference<Action>\n    implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 取并清空 Action 后 run；再 dispose 上游。 */\n"
            "        @Override\n        public void dispose() {",
        ),
    ],
    "SingleDoOnError.java": [
        (
            "import io.reactivex.rxjava4.functions.Consumer;\n\npublic final class SingleDoOnError",
            "import io.reactivex.rxjava4.functions.Consumer;\n\n"
            "/**\n"
            " * 上游 onError 时先调用 onError Consumer，再转发错误给 downstream。\n"
            " * Consumer 抛异常则合并为 CompositeException。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleDoOnError",
        ),
        (
            "    public SingleDoOnError(SingleSource<T> source, Consumer<? super Throwable> onError) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onError 错误发生时的 Consumer\n"
            "     */\n"
            "    public SingleDoOnError(SingleSource<T> source, Consumer<? super Throwable> onError) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 DoOnError 拦截 onError 路径。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    final class DoOnError implements SingleObserver<T> {",
            "    /** onError 时 accept 后 downstream.onError；回调异常合并异常。 */\n"
            "    final class DoOnError implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 调用 onError.accept；异常则 CompositeException 合并转发。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "SingleDoOnEvent.java": [
        (
            "import io.reactivex.rxjava4.functions.BiConsumer;\n\npublic final class SingleDoOnEvent",
            "import io.reactivex.rxjava4.functions.BiConsumer;\n\n"
            "/**\n"
            " * 在转发终端事件前调用 onEvent：成功时 (value, null)，错误时 (null, error)。\n"
            " * 回调异常：成功路径转 onError；错误路径合并 CompositeException。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleDoOnEvent",
        ),
        (
            "    public SingleDoOnEvent(SingleSource<T> source, BiConsumer<? super T, ? super Throwable> onEvent) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onEvent 终端事件回调 (value, error)\n"
            "     */\n"
            "    public SingleDoOnEvent(SingleSource<T> source, BiConsumer<? super T, ? super Throwable> onEvent) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 DoOnEvent 在 onSuccess/onError 前触发 BiConsumer。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    final class DoOnEvent implements SingleObserver<T> {",
            "    /** onSuccess/onError 均先 onEvent.accept 再转发。 */\n"
            "    final class DoOnEvent implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** accept(value, null) 成功后 downstream.onSuccess。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** accept(null, e) 后转发；回调异常合并 CompositeException。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "SingleDoOnLifecycle.java": [
        (
            "/**\n * Invokes callbacks upon {@code onSubscribe} from upstream and\n * {@code dispose} from downstream.\n *\n * @param <T> the element type of the flow\n * @since 3.0.0\n */\npublic final class SingleDoOnLifecycle",
            "/**\n"
            " * 在上游 onSubscribe 与下游 dispose 时分别调用 onSubscribe Consumer 与 onDispose Action。\n"
            " * onSubscribe 回调异常则 dispose 上游并以 EmptyDisposable.error 终止。\n"
            " * @param <T> 元素类型\n"
            " * @since 3.0.0\n"
            " */\n"
            "public final class SingleDoOnLifecycle",
        ),
        (
            "    public SingleDoOnLifecycle(Single<T> upstream, Consumer<? super Disposable> onSubscribe,\n            Action onDispose) {",
            "    /**\n"
            "     * @param upstream 上游 Single\n"
            "     * @param onSubscribe 上游 onSubscribe 时接收 Disposable 的 Consumer\n"
            "     * @param onDispose 下游 dispose 时执行的 Action\n"
            "     */\n"
            "    public SingleDoOnLifecycle(Single<T> upstream, Consumer<? super Disposable> onSubscribe,\n            Action onDispose) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 SingleLifecycleObserver 桥接生命周期回调。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class SingleLifecycleObserver<T> implements SingleObserver<T>, Disposable {",
            "    /** onSubscribe 先 accept 再 validate；dispose 先 onDispose 再 upstream.dispose。 */\n"
            "    static final class SingleLifecycleObserver<T> implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSubscribe(@NonNull Disposable d) {",
            "        /** 先 onSubscribe.accept；异常则 dispose 并以 error 终止订阅。 */\n"
            "        @Override\n        public void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 先 onDispose.run 再 upstream.dispose 并置 DISPOSED。 */\n"
            "        @Override\n        public void dispose() {",
        ),
    ],
    "SingleDoOnSubscribe.java": [
        (
            "/**\n * Calls a callback when the upstream calls onSubscribe with a disposable.\n *\n * @param <T> the value type\n */\npublic final class SingleDoOnSubscribe",
            "/**\n"
            " * 上游调用 onSubscribe 时先执行 onSubscribe Consumer，再转发 Disposable 给 downstream。\n"
            " * 回调异常则 dispose 上游并以 EmptyDisposable.error 终止；done 门控忽略后续事件。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleDoOnSubscribe",
        ),
        (
            "    public SingleDoOnSubscribe(SingleSource<T> source, Consumer<? super Disposable> onSubscribe) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onSubscribe 收到 Disposable 时执行的 Consumer\n"
            "     */\n"
            "    public SingleDoOnSubscribe(SingleSource<T> source, Consumer<? super Disposable> onSubscribe) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 DoOnSubscribeSingleObserver 拦截 onSubscribe。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class DoOnSubscribeSingleObserver<T> implements SingleObserver<T> {",
            "    /** done 标志：onSubscribe 失败后忽略 onSuccess/onError。 */\n"
            "    static final class DoOnSubscribeSingleObserver<T> implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSubscribe(Disposable d) {",
            "        /** accept 成功则 downstream.onSubscribe；异常则 done 并 error 终止。 */\n"
            "        @Override\n        public void onSubscribe(Disposable d) {",
        ),
    ],
}
