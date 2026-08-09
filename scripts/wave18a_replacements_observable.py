"""Replacement tuples for RxJava wave-18a observable operators [0:15]."""

OBSERVABLE_W18A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ObservableElementAtMaybe.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableElementAtMaybe<T> extends Maybe<T> implements FuseToObservable<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 取上游第 index 个（0-based）元素并以 {@link Maybe} 信号返回：\n"
            " * 命中时 onSuccess，上游完成而未命中时 onComplete。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableElementAtMaybe<T> extends Maybe<T> implements FuseToObservable<T> {",
        ),
        (
            "    public ObservableElementAtMaybe(ObservableSource<T> source, long index) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param index 目标元素索引（0-based）\n"
            "     */\n"
            "    public ObservableElementAtMaybe(ObservableSource<T> source, long index) {",
        ),
        (
            "    @Override\n    public void subscribeActual(MaybeObserver<? super T> t) {",
            "    /** 订阅 ElementAtObserver 并计数至 index。 */\n"
            "    @Override\n    public void subscribeActual(MaybeObserver<? super T> t) {",
        ),
        (
            "    @Override\n    public Observable<T> fuseToObservable() {",
            "    /** 融合为 {@link ObservableElementAt}（无默认值、元素不足时 onComplete）。 */\n"
            "    @Override\n    public Observable<T> fuseToObservable() {",
        ),
        (
            "    static final class ElementAtObserver<T> implements Observer<T>, Disposable {",
            "    /** 计数上游元素，count==index 时 onSuccess 并 dispose 上游。 */\n"
            "    static final class ElementAtObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** count 达 index 时 onSuccess 并终止上游。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 未命中 index 时 onComplete（空 Maybe）。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableElementAtSingle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableElementAtSingle<T> extends Single<T> implements FuseToObservable<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 取上游第 index 个（0-based）元素并以 {@link Single} 信号返回：\n"
            " * 命中时 onSuccess；元素不足时发 defaultValue 或 {@link NoSuchElementException}。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableElementAtSingle<T> extends Single<T> implements FuseToObservable<T> {",
        ),
        (
            "    public ObservableElementAtSingle(ObservableSource<T> source, long index, T defaultValue) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param index 目标元素索引（0-based）\n"
            "     * @param defaultValue 元素不足时的默认值（null 表示无默认值）\n"
            "     */\n"
            "    public ObservableElementAtSingle(ObservableSource<T> source, long index, T defaultValue) {",
        ),
        (
            "    @Override\n    public void subscribeActual(SingleObserver<? super T> t) {",
            "    /** 订阅 ElementAtObserver 并映射结果为 Single 信号。 */\n"
            "    @Override\n    public void subscribeActual(SingleObserver<? super T> t) {",
        ),
        (
            "    @Override\n    public Observable<T> fuseToObservable() {",
            "    /** 融合为 {@link ObservableElementAt}（带 defaultValue、errorOnFewer=true）。 */\n"
            "    @Override\n    public Observable<T> fuseToObservable() {",
        ),
        (
            "    static final class ElementAtObserver<T> implements Observer<T>, Disposable {",
            "    /** 计数至 index 时 onSuccess；完成时处理 defaultValue 逻辑。 */\n"
            "    static final class ElementAtObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** count==index 时 onSuccess 并 dispose 上游。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 未命中时 onSuccess(defaultValue) 或 onError(NoSuchElementException)。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableEmpty.java": [
        (
            "import io.reactivex.rxjava4.operators.ScalarSupplier;\n\npublic final class ObservableEmpty extends Observable<Object> implements ScalarSupplier<Object> {",
            "import io.reactivex.rxjava4.operators.ScalarSupplier;\n\n"
            "/**\n"
            " * 不发射任何元素的空 {@link Observable} 单例；\n"
            " * 订阅后立即 onComplete。实现 {@link ScalarSupplier} 时 null 标量表示空序列。\n"
            " */\n"
            "public final class ObservableEmpty extends Observable<Object> implements ScalarSupplier<Object> {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super Object> o) {",
            "    /** 经 {@link EmptyDisposable#complete} 立即完成下游。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super Object> o) {",
        ),
        (
            "    @Override\n    public Object get() {",
            "    /** 标量融合路径：null 表示空序列。 */\n"
            "    @Override\n    public Object get() {",
        ),
    ],
    "ObservableError.java": [
        (
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\npublic final class ObservableError<T> extends Observable<T> {",
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\n"
            "/**\n"
            " * 订阅时调用 {@link Supplier} 获取 Throwable 并立即 onError；\n"
            " * Supplier 异常同样经 {@link EmptyDisposable#error} 转发。\n"
            " *\n"
            " * @param <T> 元素类型（不会发射）\n"
            " */\n"
            "public final class ObservableError<T> extends Observable<T> {",
        ),
        (
            "    public ObservableError(Supplier<? extends Throwable> errorSupplier) {",
            "    /** @param errorSupplier 提供错误对象的 Supplier */\n"
            "    public ObservableError(Supplier<? extends Throwable> errorSupplier) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 求值 errorSupplier 并将结果或捕获异常作为 onError 信号。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
    ],
    "ObservableFilter.java": [
        (
            "import io.reactivex.rxjava4.internal.observers.BasicFuseableObserver;\n\npublic final class ObservableFilter<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.internal.observers.BasicFuseableObserver;\n\n"
            "/**\n"
            " * 按 {@link Predicate} 过滤上游元素，仅转发 test 为 true 的 onNext。\n"
            " * 支持 queue fusion 的 poll 路径同步过滤。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableFilter<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableFilter(ObservableSource<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param predicate 逐元素测试谓词\n"
            "     */\n"
            "    public ObservableFilter(ObservableSource<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 FilterObserver 并应用 predicate。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class FilterObserver<T> extends BasicFuseableObserver<T, T> {",
            "    /** 非 fusion 模式下 test 通过才 onNext；fusion 模式下 poll 循环过滤。 */\n"
            "    static final class FilterObserver<T> extends BasicFuseableObserver<T, T> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** predicate.test 为 true 时转发 t；fusion 模式发 null 占位。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Nullable\n        @Override\n        public T poll() throws Throwable {",
            "        /** 从队列 poll 直至元素通过 filter 或队列空。 */\n"
            "        @Nullable\n        @Override\n        public T poll() throws Throwable {",
        ),
    ],
    "ObservableFlatMapCompletable.java": [
        (
            "/**\n * Maps a sequence of values into CompletableSources and awaits their termination.\n * @param <T> the value type\n */",
            "/**\n"
            " * 将上游每个元素映射为 {@link CompletableSource} 并等待其全部终止；\n"
            " * 不向 downstream 发射元素。\n"
            " * @param <T> 上游元素类型\n"
            " */",
        ),
        (
            "    public ObservableFlatMapCompletable(ObservableSource<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param mapper 将元素映射为 CompletableSource 的函数\n"
            "     * @param delayErrors 为 true 时收集所有 inner 错误后再终止\n"
            "     */\n"
            "    public ObservableFlatMapCompletable(ObservableSource<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 并行订阅 inner Completable 并在全部完成后终止 downstream。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class FlatMapCompletableMainObserver<T> extends BasicIntQueueDisposable<T>\n    implements Observer<T> {",
            "    /** 维护 CompositeDisposable 与 active 计数，协调 inner 完成/错误。 */\n"
            "    static final class FlatMapCompletableMainObserver<T> extends BasicIntQueueDisposable<T>\n    implements Observer<T> {",
        ),
        (
            "        @Override\n        public void onNext(T value) {",
            "        /** 映射并订阅 inner Completable；null mapper 结果转 onError。 */\n"
            "        @Override\n        public void onNext(T value) {",
        ),
        (
            "        void innerComplete(InnerObserver inner) {",
            "        /** inner 完成时从 set 移除并递减 active 计数。 */\n"
            "        void innerComplete(InnerObserver inner) {",
        ),
        (
            "        final class InnerObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {",
            "        /** 订阅单个 inner Completable 并将完成/错误回传 main observer。 */\n"
            "        final class InnerObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {",
        ),
    ],
    "ObservableFlatMapCompletableCompletable.java": [
        (
            "/**\n * Maps a sequence of values into CompletableSources and awaits their termination.\n * @param <T> the value type\n */",
            "/**\n"
            " * {@link ObservableFlatMapCompletable} 的 {@link Completable} 变体：\n"
            " * 映射上游元素为 CompletableSource 并等待全部终止后 onComplete。\n"
            " * @param <T> 上游元素类型\n"
            " */",
        ),
        (
            "    public ObservableFlatMapCompletableCompletable(ObservableSource<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors, int bufferSize) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param mapper 将元素映射为 CompletableSource 的函数\n"
            "     * @param delayErrors 为 true 时延迟报告错误\n"
            "     * @param bufferSize 缓冲大小（保留参数，与 Observable 版语义对齐）\n"
            "     */\n"
            "    public ObservableFlatMapCompletableCompletable(ObservableSource<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors, int bufferSize) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 订阅 FlatMapCompletableMainObserver 并映射为 Completable 终止信号。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    @Override\n    public Observable<T> fuseToObservable() {",
            "    /** 融合为 {@link ObservableFlatMapCompletable} 实例。 */\n"
            "    @Override\n    public Observable<T> fuseToObservable() {",
        ),
        (
            "    static final class FlatMapCompletableMainObserver<T> extends AtomicInteger implements Disposable, Observer<T> {",
            "    /** Completable 版 main observer：active 归零时 tryTerminateConsumer 下游。 */\n"
            "    static final class FlatMapCompletableMainObserver<T> extends AtomicInteger implements Disposable, Observer<T> {",
        ),
        (
            "        @Override\n        public void onNext(T value) {",
            "        /** 映射并并行订阅 inner Completable。 */\n"
            "        @Override\n        public void onNext(T value) {",
        ),
        (
            "        final class InnerObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {",
            "        /** 单个 inner Completable 的 CompletableObserver 包装。 */\n"
            "        final class InnerObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {",
        ),
    ],
    "ObservableFlatMapMaybe.java": [
        (
            "/**\n * Maps upstream values into MaybeSources and merges their signals into one sequence.\n * @param <T> the source value type\n * @param <R> the result value type\n */",
            "/**\n"
            " * 将上游每个元素映射为 {@link MaybeSource} 并合并其 onSuccess 值为单一序列；\n"
            " * inner onComplete 被忽略，错误可延迟聚合。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 输出元素类型\n"
            " */",
        ),
        (
            "    public ObservableFlatMapMaybe(ObservableSource<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n            boolean delayError,\n            int bufferSize) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param mapper 将元素映射为 MaybeSource 的函数\n"
            "     * @param delayError 为 true 时延迟报告错误\n"
            "     * @param bufferSize inner 成功值缓冲队列容量\n"
            "     */\n"
            "    public ObservableFlatMapMaybe(ObservableSource<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n            boolean delayError,\n            int bufferSize) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 订阅 FlatMapMaybeObserver 并 drain 合并 inner 成功值。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class FlatMapMaybeObserver<T, R>\n    extends AtomicInteger\n    implements Observer<T>, Disposable {",
            "    /** 维护 active 计数、错误集合与 Spsc 队列，drain 合并 inner onSuccess。 */\n"
            "    static final class FlatMapMaybeObserver<T, R>\n    extends AtomicInteger\n    implements Observer<T>, Disposable {",
        ),
        (
            "        void innerSuccess(InnerObserver inner, R value) {",
            "        /** inner onSuccess 时直接 emit 或入队后 drainLoop。 */\n"
            "        void innerSuccess(InnerObserver inner, R value) {",
        ),
        (
            "        void drainLoop() {",
            "        /** 从队列 poll 并 onNext，active==0 且队列空时终止下游。 */\n"
            "        void drainLoop() {",
        ),
        (
            "        final class InnerObserver extends AtomicReference<Disposable>\n        implements MaybeObserver<R>, Disposable {",
            "        /** 订阅单个 inner Maybe 并将 onSuccess/onError/onComplete 回传 parent。 */\n"
            "        final class InnerObserver extends AtomicReference<Disposable>\n        implements MaybeObserver<R>, Disposable {",
        ),
    ],
    "ObservableFlatMapSingle.java": [
        (
            "/**\n * Maps upstream values into SingleSources and merges their signals into one sequence.\n * @param <T> the source value type\n * @param <R> the result value type\n */",
            "/**\n"
            " * 将上游每个元素映射为 {@link SingleSource} 并合并其 onSuccess 值为单一序列；\n"
            " * 错误可延迟聚合，并发 inner 成功值经 Spsc 队列缓冲。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 输出元素类型\n"
            " */",
        ),
        (
            "    public ObservableFlatMapSingle(ObservableSource<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayError,\n            int bufferSize) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param mapper 将元素映射为 SingleSource 的函数\n"
            "     * @param delayError 为 true 时延迟报告错误\n"
            "     * @param bufferSize inner 成功值缓冲队列容量\n"
            "     */\n"
            "    public ObservableFlatMapSingle(ObservableSource<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayError,\n            int bufferSize) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 订阅 FlatMapSingleObserver 并 drain 合并 inner 成功值。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class FlatMapSingleObserver<T, R>\n    extends AtomicInteger\n    implements Observer<T>, Disposable {",
            "    /** 维护 active 计数、错误集合与 Spsc 队列，drain 合并 inner onSuccess。 */\n"
            "    static final class FlatMapSingleObserver<T, R>\n    extends AtomicInteger\n    implements Observer<T>, Disposable {",
        ),
        (
            "        void innerSuccess(InnerObserver inner, R value) {",
            "        /** inner onSuccess 时直接 emit 或入队后 drainLoop。 */\n"
            "        void innerSuccess(InnerObserver inner, R value) {",
        ),
        (
            "        void drainLoop() {",
            "        /** 从队列 poll 并 onNext，active==0 且队列空时终止下游。 */\n"
            "        void drainLoop() {",
        ),
        (
            "        final class InnerObserver extends AtomicReference<Disposable>\n        implements SingleObserver<R>, Disposable {",
            "        /** 订阅单个 inner Single 并将 onSuccess/onError 回传 parent。 */\n"
            "        final class InnerObserver extends AtomicReference<Disposable>\n        implements SingleObserver<R>, Disposable {",
        ),
    ],
    "ObservableFlattenIterable.java": [
        (
            "/**\n * Maps a sequence into an Iterable and emits its values.\n *\n * @param <T> the input value type to map to Iterable\n * @param <R> the element type of the Iterable and the output\n */",
            "/**\n"
            " * 将上游每个元素经 mapper 转为 {@link Iterable}，\n"
            " * 逐元素展开为下游 onNext 序列。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> Iterable 元素及输出类型\n"
            " */",
        ),
        (
            "    public ObservableFlattenIterable(ObservableSource<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param mapper 将元素映射为 Iterable 的函数\n"
            "     */\n"
            "    public ObservableFlattenIterable(ObservableSource<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 订阅 FlattenIterableObserver 并展开 Iterable。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class FlattenIterableObserver<T, R> implements Observer<T>, Disposable {",
            "    /** 对每个上游值迭代 Iterable 并逐元素 onNext。 */\n"
            "    static final class FlattenIterableObserver<T, R> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T value) {",
            "        /** 展开 mapper(value) 的 iterator；null 元素或迭代异常转 onError。 */\n"
            "        @Override\n        public void onNext(T value) {",
        ),
    ],
    "ObservableFromAction.java": [
        (
            "/**\n * Executes an {@link Action} and signals its exception or completes normally.\n *\n * @param <T> the value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 执行 {@link Action} 并在无异常时 onComplete；\n"
            " * 异常经 onError 转发。实现 {@link Supplier} 时 get 返回 null 表示完成。\n"
            " *\n"
            " * @param <T> 元素类型（不会发射 onNext）\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public ObservableFromAction(Action action) {",
            "    /** @param action 订阅时执行的 Action */\n"
            "    public ObservableFromAction(Action action) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 经 CancellableQueueFuseable 订阅后 run action 并 onComplete/onError。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    @Override\n    public T get() throws Throwable {",
            "    /** 标量路径：run action 后返回 null 表示 onComplete。 */\n"
            "    @Override\n    public T get() throws Throwable {",
        ),
    ],
    "ObservableFromArray.java": [
        (
            "import java.util.Objects;\n\npublic final class ObservableFromArray<T> extends Observable<T> {",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 将数组元素依次作为 onNext 发射；支持 SYNC queue fusion。\n"
            " *\n"
            " * @param <T> 数组元素类型\n"
            " */\n"
            "public final class ObservableFromArray<T> extends Observable<T> {",
        ),
        (
            "    public ObservableFromArray(T[] array) {",
            "    /** @param array 源数组（元素 null 时 onError） */\n"
            "    public ObservableFromArray(T[] array) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 创建 FromArrayDisposable；非 fusion 模式下立即 run 发射。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class FromArrayDisposable<T> extends BasicQueueDisposable<T> {",
            "    /** 顺序 emit 数组元素或 fusion poll；null 元素触发 NPE onError。 */\n"
            "    static final class FromArrayDisposable<T> extends BasicQueueDisposable<T> {",
        ),
        (
            "        void run() {",
            "        /** 非 fusion 路径：遍历 array 逐元素 onNext 后 onComplete。 */\n"
            "        void run() {",
        ),
    ],
    "ObservableFromCallable.java": [
        (
            "/**\n * Calls a Callable and emits its resulting single value or signals its exception.\n * @param <T> the value type\n */",
            "/**\n"
            " * 调用 {@link Callable} 并将单个结果作为 onNext 后 onComplete；\n"
            " * 异常经 onError 转发。实现 {@link Supplier} 供标量融合。\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ObservableFromCallable(Callable<? extends T> callable) {",
            "    /** @param callable 订阅时调用的 Callable */\n"
            "    public ObservableFromCallable(Callable<? extends T> callable) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 经 DeferredScalarDisposable 调用 callable 并 complete 单值。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    @Override\n    public T get() throws Throwable {",
            "    /** 标量路径：直接 call callable 并 nullCheck 结果。 */\n"
            "    @Override\n    public T get() throws Throwable {",
        ),
    ],
    "ObservableFromCompletable.java": [
        (
            "/**\n * Wrap a Completable into an Observable.\n *\n * @param <T> the value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 将 {@link CompletableSource} 包装为不发射元素的 {@link Observable}：\n"
            " * Completable onComplete/onError 映射为 Observable 终止信号。\n"
            " *\n"
            " * @param <T> 元素类型（不会发射）\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public ObservableFromCompletable(CompletableSource source) {",
            "    /** @param source 上游 CompletableSource */\n"
            "    public ObservableFromCompletable(CompletableSource source) {",
        ),
        (
            "    @Override\n    public CompletableSource source() {",
            "    /** 返回包装的 CompletableSource。 */\n"
            "    @Override\n    public CompletableSource source() {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 FromCompletableObserver 并转发 Completable 信号。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    public static final class FromCompletableObserver<T>\n    extends AbstractEmptyQueueFuseable<T>\n    implements CompletableObserver {",
            "    /** 将 Completable onComplete/onError 映射为 Observable 终止信号。 */\n"
            "    public static final class FromCompletableObserver<T>\n    extends AbstractEmptyQueueFuseable<T>\n    implements CompletableObserver {",
        ),
    ],
    "ObservableFromFuture.java": [
        (
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\npublic final class ObservableFromFuture<T> extends Observable<T> {",
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\n"
            "/**\n"
            " * 阻塞调用 {@link Future#get}（可选超时）并将单个结果作为 onNext 后 onComplete；\n"
            " * 异常经 onError 转发。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableFromFuture<T> extends Observable<T> {",
        ),
        (
            "    public ObservableFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {",
            "    /**\n"
            "     * @param future 待求值的 Future\n"
            "     * @param timeout 超时量（unit 为 null 时忽略，使用无超时 get）\n"
            "     * @param unit 超时时间单位\n"
            "     */\n"
            "    public ObservableFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 经 DeferredScalarDisposable 阻塞 get future 并 complete 单值。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
    ],
}
