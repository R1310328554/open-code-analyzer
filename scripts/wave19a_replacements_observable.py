"""Replacement tuples for RxJava wave-19a observable operators [0:15]."""

OBSERVABLE_W19A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ObservableLastMaybe.java": [
        (
            "/**\n"
            " * Consumes the source ObservableSource and emits its last item, the defaultItem\n"
            " * if empty or a NoSuchElementException if even the defaultItem is null.\n"
            " * \n"
            " * @param <T> the value type\n"
            " */",
            "/**\n"
            " * 消费上游 {@link ObservableSource} 并发射最后一个元素：\n"
            " * 有元素时 onSuccess，上游为空时 onComplete（无默认值路径）。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " */",
        ),
        (
            "    public ObservableLastMaybe(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableLastMaybe(ObservableSource<T> source) {",
        ),
        (
            "    // TODO fuse back to Observable\n",
            "    // TODO 融合回 Observable\n",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 LastObserver，在 onComplete 时将最后一项映射为 Maybe 信号。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class LastObserver<T> implements Observer<T>, Disposable {",
            "    /** 缓存上游最后一项，完成时 onSuccess 或 onComplete。 */\n"
            "    static final class LastObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 覆盖保存最后一项，不立即向下游发射。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 有缓存项时 onSuccess，否则 onComplete（空 Maybe）。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableLastSingle.java": [
        (
            "/**\n"
            " * Consumes the source ObservableSource and emits its last item, the defaultItem\n"
            " * if empty or a NoSuchElementException if even the defaultItem is null.\n"
            " * \n"
            " * @param <T> the value type\n"
            " */",
            "/**\n"
            " * 消费上游 {@link ObservableSource} 并发射最后一个元素：\n"
            " * 有元素时 onSuccess；为空时发 defaultItem 或 {@link NoSuchElementException}。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " */",
        ),
        (
            "    public ObservableLastSingle(ObservableSource<T> source, T defaultItem) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param defaultItem 上游为空时的默认值（null 表示无默认值）\n"
            "     */\n"
            "    public ObservableLastSingle(ObservableSource<T> source, T defaultItem) {",
        ),
        (
            "    // TODO fuse back to Observable\n",
            "    // TODO 融合回 Observable\n",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 LastObserver 并将最后一项映射为 Single 信号。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class LastObserver<T> implements Observer<T>, Disposable {",
            "    /** 缓存最后一项，完成时 onSuccess 或处理 defaultItem 逻辑。 */\n"
            "    static final class LastObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 覆盖保存最后一项。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 有缓存项或 defaultItem 时 onSuccess，否则 onError(NoSuchElementException)。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableLift.java": [
        (
            "/**\n"
            " * Allows lifting operators into a chain of Observables.\n"
            " *\n"
            " * <p>By having a concrete ObservableSource as lift, operator fusing can now identify\n"
            " * both the source and the operation inside it via casting, unlike the lambda version of this.\n"
            " *\n"
            " * @param <T> the upstream value type\n"
            " * @param <R> the downstream parameter type\n"
            " */",
            "/**\n"
            " * 将 {@link ObservableOperator} 提升（lift）进 Observable 链。\n"
            " *\n"
            " * <p>相比 lambda 版 lift，具体 {@link ObservableSource} 包装使 operator fusion\n"
            " * 可通过类型转换同时识别上游与内部操作。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 下游元素类型\n"
            " */",
        ),
        (
            "    /** The actual operator. */",
            "    /** 实际应用的 {@link ObservableOperator}。 */",
        ),
        (
            "    public ObservableLift(ObservableSource<T> source, ObservableOperator<? extends R, ? super T> operator) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param operator 对下游 Observer 进行变换的算子\n"
            "     */\n"
            "    public ObservableLift(ObservableSource<T> source, ObservableOperator<? extends R, ? super T> operator) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super R> observer) {",
            "    /** 调用 operator.apply 得到 lifted Observer 后订阅上游。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "        } catch (NullPointerException e) { // NOPMD",
            "        } catch (NullPointerException e) { // NOPMD — operator 返回 null Observer",
        ),
        (
            "            // can't call onError because no way to know if a Disposable has been set or not\n"
            "            // can't call onSubscribe because the call might have set a Disposable already",
            "            // 无法 onError：尚不确定 Disposable 是否已设置\n"
            "            // 无法 onSubscribe：apply 可能已设置 Disposable",
        ),
    ],
    "ObservableMap.java": [
        (
            "import java.util.Objects;\n\npublic final class ObservableMap<T, U> extends AbstractObservableWithUpstream<T, U> {",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 按 {@link Function} 将上游每个 onNext 元素映射为下游类型并转发。\n"
            " * 支持 queue fusion 的 poll 路径同步映射。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 映射后元素类型\n"
            " */\n"
            "public final class ObservableMap<T, U> extends AbstractObservableWithUpstream<T, U> {",
        ),
        (
            "    public ObservableMap(ObservableSource<T> source, Function<? super T, ? extends U> function) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param function 元素映射函数（返回值不可为 null）\n"
            "     */\n"
            "    public ObservableMap(ObservableSource<T> source, Function<? super T, ? extends U> function) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super U> t) {",
            "    /** 订阅 MapObserver 并在 onNext/poll 路径应用 mapper。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super U> t) {",
        ),
        (
            "    static final class MapObserver<T, U> extends BasicFuseableObserver<T, U> {",
            "    /** 融合感知映射 Observer：onNext 与 poll 均经 mapper 变换。 */\n"
            "    static final class MapObserver<T, U> extends BasicFuseableObserver<T, U> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 非 fusion 路径：apply mapper 后 onNext，null 或异常走 fail。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Nullable\n        @Override\n        public U poll() throws Throwable {",
            "        /** fusion poll 路径：对 poll 结果同步映射。 */\n"
            "        @Nullable\n        @Override\n        public U poll() throws Throwable {",
        ),
    ],
    "ObservableMapNotification.java": [
        (
            "import java.util.Objects;\n\npublic final class ObservableMapNotification<T, R> extends AbstractObservableWithUpstream<T, ObservableSource<? extends R>> {",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 将上游 onNext/onError/onComplete 分别映射为新的 {@link ObservableSource} 并作为\n"
            " * 下游 onNext 发射（onError 映射后还会 onComplete）。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 映射后 ObservableSource 的元素类型\n"
            " */\n"
            "public final class ObservableMapNotification<T, R> extends AbstractObservableWithUpstream<T, ObservableSource<? extends R>> {",
        ),
        (
            "    public ObservableMapNotification(\n"
            "            ObservableSource<T> source,\n"
            "            Function<? super T, ? extends ObservableSource<? extends R>> onNextMapper,\n"
            "            Function<? super Throwable, ? extends ObservableSource<? extends R>> onErrorMapper,\n"
            "            Supplier<? extends ObservableSource<? extends R>> onCompleteSupplier) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param onNextMapper onNext 时的 ObservableSource 映射\n"
            "     * @param onErrorMapper onError 时的 ObservableSource 映射\n"
            "     * @param onCompleteSupplier onComplete 时提供 ObservableSource 的 Supplier\n"
            "     */\n"
            "    public ObservableMapNotification(\n"
            "            ObservableSource<T> source,\n"
            "            Function<? super T, ? extends ObservableSource<? extends R>> onNextMapper,\n"
            "            Function<? super Throwable, ? extends ObservableSource<? extends R>> onErrorMapper,\n"
            "            Supplier<? extends ObservableSource<? extends R>> onCompleteSupplier) {",
        ),
        (
            "    static final class MapNotificationObserver<T, R>\n"
            "    implements Observer<T>, Disposable {",
            "    /** 按事件类型调用对应 mapper 并将结果 ObservableSource 作为 onNext 转发。 */\n"
            "    static final class MapNotificationObserver<T, R>\n"
            "    implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** onNextMapper 求值后以 ObservableSource 形式 onNext。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** onErrorMapper 求值后 onNext 映射源并 onComplete；mapper 异常则 CompositeException。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** onCompleteSupplier 求值后 onNext 映射源并 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableMaterialize.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class ObservableMaterialize<T> extends AbstractObservableWithUpstream<T, Notification<T>> {",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 将上游 onNext/onError/onComplete 包装为 {@link Notification} 并以 onNext 发射；\n"
            " * 终端事件（onError/onComplete）之后仍 onComplete 下游。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableMaterialize<T> extends AbstractObservableWithUpstream<T, Notification<T>> {",
        ),
        (
            "    public ObservableMaterialize(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableMaterialize(ObservableSource<T> source) {",
        ),
        (
            "    static final class MaterializeObserver<T> implements Observer<T>, Disposable {",
            "    /** 将每个上游信号转为对应 Notification 并转发。 */\n"
            "    static final class MaterializeObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 发射 {@link Notification#createOnNext}。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 发射 {@link Notification#createOnError} 后 onComplete。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 发射 {@link Notification#createOnComplete} 后 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableMergeWithCompletable.java": [
        (
            "/**\n"
            " * Merges an Observable and a Completable by emitting the items of the Observable and waiting until\n"
            " * both the Observable and Completable complete normally.\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> the element type of the Observable\n"
            " * @since 2.2\n"
            " */",
            "/**\n"
            " * 合并 {@link Observable} 与 {@link Completable}：转发 Observable 的 onNext，\n"
            " * 且两者均正常完成时才向下游 onComplete。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> Observable 元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ObservableMergeWithCompletable(Observable<T> source, CompletableSource other) {",
            "    /**\n"
            "     * @param source 主 Observable 上游\n"
            "     * @param other 与之合并的 Completable\n"
            "     */\n"
            "    public ObservableMergeWithCompletable(Observable<T> source, CompletableSource other) {",
        ),
        (
            "    static final class MergeWithObserver<T> extends AtomicInteger\n"
            "    implements Observer<T>, Disposable {",
            "    /** 协调主序列与 Completable：双端均完成时经 {@link HalfSerializer} 终止。 */\n"
            "    static final class MergeWithObserver<T> extends AtomicInteger\n"
            "    implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 主序列完成；若 other 已完成则向下游 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        void otherComplete() {",
            "        /** Completable 完成；若主序列已完成则向下游 onComplete。 */\n"
            "        void otherComplete() {",
        ),
        (
            "        static final class OtherObserver extends AtomicReference<Disposable>\n"
            "        implements CompletableObserver {",
            "        /** Completable 侧 Observer，完成/错误回调至 parent。 */\n"
            "        static final class OtherObserver extends AtomicReference<Disposable>\n"
            "        implements CompletableObserver {",
        ),
    ],
    "ObservableMergeWithMaybe.java": [
        (
            "/**\n"
            " * Merges an Observable and a Maybe by emitting the items of the Observable and the success\n"
            " * value of the Maybe and waiting until both the Observable and Maybe terminate normally.\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> the element type of the Observable\n"
            " * @since 2.2\n"
            " */",
            "/**\n"
            " * 合并 {@link Observable} 与 {@link Maybe}：交错发射 Observable 元素与 Maybe 的 onSuccess 值，\n"
            " * 两者均正常终止后才 onComplete。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> Observable 元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "        static final int OTHER_STATE_HAS_VALUE = 1;",
            "        /** Maybe 已成功且值尚未消费。 */\n"
            "        static final int OTHER_STATE_HAS_VALUE = 1;",
        ),
        (
            "        static final int OTHER_STATE_CONSUMED_OR_EMPTY = 2;",
            "        /** Maybe 已消费或为空（onComplete）。 */\n"
            "        static final int OTHER_STATE_CONSUMED_OR_EMPTY = 2;",
        ),
        (
            "        void otherSuccess(T value) {",
            "        /** Maybe onSuccess：立即或入队后 drain 发射。 */\n"
            "        void otherSuccess(T value) {",
        ),
        (
            "        void drainLoop() {",
            "        /** 串行 drain：先消费 Maybe 缓存值，再 poll 主队列，双端完成时 onComplete。 */\n"
            "        void drainLoop() {",
        ),
        (
            "        static final class OtherObserver<T> extends AtomicReference<Disposable>\n"
            "        implements MaybeObserver<T> {",
            "        /** Maybe 侧 Observer，成功/完成/错误回调至 parent。 */\n"
            "        static final class OtherObserver<T> extends AtomicReference<Disposable>\n"
            "        implements MaybeObserver<T> {",
        ),
    ],
    "ObservableMergeWithSingle.java": [
        (
            "/**\n"
            " * Merges an Observable and a Single by emitting the items of the Observable and the success\n"
            " * value of the Single and waiting until both the Observable and Single terminate normally.\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> the element type of the Observable\n"
            " * @since 2.2\n"
            " */",
            "/**\n"
            " * 合并 {@link Observable} 与 {@link Single}：交错发射 Observable 元素与 Single 的 onSuccess 值，\n"
            " * 主序列完成且 Single 已成功后才 onComplete。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> Observable 元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "        static final int OTHER_STATE_HAS_VALUE = 1;",
            "        /** Single 已成功且值尚未消费。 */\n"
            "        static final int OTHER_STATE_HAS_VALUE = 1;",
        ),
        (
            "        static final int OTHER_STATE_CONSUMED_OR_EMPTY = 2;",
            "        /** Single 值已消费（Single 无 onComplete）。 */\n"
            "        static final int OTHER_STATE_CONSUMED_OR_EMPTY = 2;",
        ),
        (
            "        void otherSuccess(T value) {",
            "        /** Single onSuccess：立即或入队后 drain 发射。 */\n"
            "        void otherSuccess(T value) {",
        ),
        (
            "        void drainLoop() {",
            "        /** 串行 drain：消费 Single 缓存值与主队列，双端就绪时 onComplete。 */\n"
            "        void drainLoop() {",
        ),
        (
            "        static final class OtherObserver<T> extends AtomicReference<Disposable>\n"
            "        implements SingleObserver<T> {",
            "        /** Single 侧 Observer，成功/错误回调至 parent。 */\n"
            "        static final class OtherObserver<T> extends AtomicReference<Disposable>\n"
            "        implements SingleObserver<T> {",
        ),
    ],
    "ObservableNever.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;\n\npublic final class ObservableNever extends Observable<Object> {",
            "import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;\n\n"
            "/**\n"
            " * 永不发射元素且永不终止的 {@link Observable} 单例；\n"
            " * 订阅时仅 onSubscribe({@link EmptyDisposable#NEVER})。\n"
            " */\n"
            "public final class ObservableNever extends Observable<Object> {",
        ),
        (
            "    public static final Observable<Object> INSTANCE = new ObservableNever();",
            "    /** 全局单例实例。 */\n"
            "    public static final Observable<Object> INSTANCE = new ObservableNever();",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super Object> o) {",
            "    /** 仅设置 NEVER Disposable，不发射任何事件。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super Object> o) {",
        ),
    ],
    "ObservableObserveOn.java": [
        (
            "import java.io.Serial;\n\npublic final class ObservableObserveOn<T> extends AbstractObservableWithUpstream<T, T> {",
            "import java.io.Serial;\n\n"
            "/**\n"
            " * 将上游事件调度到指定 {@link Scheduler} 的 Worker 线程执行；\n"
            " * 经队列缓冲并支持 SYNC/ASYNC queue fusion。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableObserveOn<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableObserveOn(ObservableSource<T> source, Scheduler scheduler, boolean delayError, int bufferSize) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param scheduler 目标调度器\n"
            "     * @param delayError true 时先 drain 队列再 onError\n"
            "     * @param bufferSize 非 fusion 时的队列容量\n"
            "     */\n"
            "    public ObservableObserveOn(ObservableSource<T> source, Scheduler scheduler, boolean delayError, int bufferSize) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** TrampolineScheduler 直接订阅；否则创建 Worker 并包装 ObserveOnObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class ObserveOnObserver<T> extends BasicIntQueueDisposable<T>\n"
            "    implements Observer<T>, Runnable {",
            "    /** 在 Worker 上 drain 队列：normal 路径逐元素，fused 路径 poll null 推进。 */\n"
            "    static final class ObserveOnObserver<T> extends BasicIntQueueDisposable<T>\n"
            "    implements Observer<T>, Runnable {",
        ),
        (
            "        void schedule() {",
            "        /** 通过 worker.schedule(this) 触发 drain。 */\n"
            "        void schedule() {",
        ),
        (
            "        void drainNormal() {",
            "        /** 非 fusion：从 queue poll 并在 Worker 线程 onNext。 */\n"
            "        void drainNormal() {",
        ),
        (
            "        void drainFused() {",
            "        /** ASYNC fusion：onNext(null) 驱动上游 poll。 */\n"
            "        void drainFused() {",
        ),
        (
            "        boolean checkTerminated(boolean d, boolean empty, Observer<? super T> a) {",
            "        /** 根据 delayError 策略在 done/empty 时 onComplete 或 onError。 */\n"
            "        boolean checkTerminated(boolean d, boolean empty, Observer<? super T> a) {",
        ),
    ],
    "ObservableOnErrorComplete.java": [
        (
            "/**\n"
            " * Emits an onComplete if the source emits an onError and the predicate returns true for\n"
            " * that Throwable.\n"
            " * \n"
            " * @param <T> the value type\n"
            " * @since 3.0.0\n"
            " */",
            "/**\n"
            " * 上游 onError 时若 {@link Predicate} 对 Throwable 返回 true，则转为 onComplete；\n"
            " * 否则原样转发 onError。predicate 异常则 {@link CompositeException}。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public ObservableOnErrorComplete(ObservableSource<T> source,\n"
            "            Predicate<? super Throwable> predicate) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param predicate 判定是否吞掉错误的谓词\n"
            "     */\n"
            "    public ObservableOnErrorComplete(ObservableSource<T> source,\n"
            "            Predicate<? super Throwable> predicate) {",
        ),
        (
            "    public static final class OnErrorCompleteObserver<T>\n"
            "    implements Observer<T>, Disposable {",
            "    /** onNext/onComplete 直通；onError 经 predicate 决定 onComplete 或 onError。 */\n"
            "    public static final class OnErrorCompleteObserver<T>\n"
            "    implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** predicate.test 为 true 时 onComplete，否则转发原错误。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "ObservableOnErrorNext.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableOnErrorNext<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 上游 onError 时由 {@link Function} 提供备用 {@link ObservableSource} 并订阅续流；\n"
            " * 仅首次 onError 触发切换，后续错误按 once/done 标志处理。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableOnErrorNext<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableOnErrorNext(ObservableSource<T> source,\n"
            "                                 Function<? super Throwable, ? extends ObservableSource<? extends T>> nextSupplier) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param nextSupplier 由 Throwable 映射备用 ObservableSource 的函数\n"
            "     */\n"
            "    public ObservableOnErrorNext(ObservableSource<T> source,\n"
            "                                 Function<? super Throwable, ? extends ObservableSource<? extends T>> nextSupplier) {",
        ),
        (
            "    static final class OnErrorNextObserver<T> implements Observer<T> {",
            "    /** 经 {@link SequentialDisposable} 仲裁上游与备用源订阅。 */\n"
            "    static final class OnErrorNextObserver<T> implements Observer<T> {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 首次 onError 时 nextSupplier 求值并 subscribe 备用源；null 则 NPE。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
    ],
    "ObservableOnErrorReturn.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class ObservableOnErrorReturn<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 上游 onError 时由 {@link Function} 提供替代值，以 onNext 发射后 onComplete；\n"
            " * valueSupplier 异常则 {@link CompositeException}，返回 null 则 NPE。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableOnErrorReturn<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableOnErrorReturn(ObservableSource<T> source, Function<? super Throwable, ? extends T> valueSupplier) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param valueSupplier 由 Throwable 映射替代值的函数\n"
            "     */\n"
            "    public ObservableOnErrorReturn(ObservableSource<T> source, Function<? super Throwable, ? extends T> valueSupplier) {",
        ),
        (
            "    static final class OnErrorReturnObserver<T> implements Observer<T>, Disposable {",
            "    /** onNext/onComplete 直通；onError 经 valueSupplier 转为 onNext+onComplete。 */\n"
            "    static final class OnErrorReturnObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** valueSupplier 求值后 onNext 替代值并 onComplete。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
    ],
    "ObservablePublish.java": [
        (
            "/**\n"
            " * Shares a single underlying connection to the upstream ObservableSource\n"
            " * and multicasts events to all subscribed observers until the upstream\n"
            " * completes or the connection is disposed.\n"
            " * <p>\n"
            " * The difference to ObservablePublish is that when the upstream terminates,\n"
            " * late observers will receive that terminal event until the connection is\n"
            " * disposed and the ConnectableObservable is reset to its fresh state.\n"
            " *\n"
            " * @param <T> the element type\n"
            " * @since 2.2.10\n"
            " */",
            "/**\n"
            " * 共享单一上游连接并向所有订阅者多播事件，直至上游终止或连接 dispose。\n"
            " * <p>\n"
            " * 上游终止后，迟到的订阅者仍会收到该终端事件，直到连接 dispose 且\n"
            " * {@link ConnectableObservable} 被 reset 至初始状态。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " * @since 2.2.10\n"
            " */",
        ),
        (
            "    @Override\n    public void connect(Consumer<? super Disposable> connection) {",
            "    /** 获取或创建 PublishConnection，首次 connect 时订阅上游。 */\n"
            "    @Override\n    public void connect(Consumer<? super Disposable> connection) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 将 observer 加入当前连接；已终止连接则直接转发终端事件。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "            // we don't create a fresh connection if the current is terminated",
            "            // 当前连接已终止时不创建新连接",
        ),
        (
            "        // Late observers will be simply terminated",
            "        // 迟到订阅者直接收到已缓存的终端事件",
        ),
        (
            "    static final class PublishConnection<T>\n"
            "    extends AtomicReference<InnerDisposable<T>[]>\n"
            "    implements Observer<T>, Disposable {",
            "    /** 多播连接：维护 InnerDisposable 数组并向全部下游转发事件。 */\n"
            "    static final class PublishConnection<T>\n"
            "    extends AtomicReference<InnerDisposable<T>[]>\n"
            "    implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 向所有活跃 InnerDisposable 的 downstream 广播 onNext。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "    /**\n"
            "     * Intercepts the dispose signal from the downstream and\n"
            "     * removes itself from the connection's observers array\n"
            "     * at most once.\n"
            "     * @param <T> the element type\n"
            "     */",
            "    /**\n"
            "     * 拦截下游 dispose 信号，至多一次从连接的 observers 数组中移除自身。\n"
            "     * @param <T> 元素类型\n"
            "     */",
        ),
    ],
}
