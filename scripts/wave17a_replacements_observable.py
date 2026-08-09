"""Replacement tuples for RxJava wave-17a observable operators [0:15]."""

OBSERVABLE_W17A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ObservableAny.java": [
        (
            "public final class ObservableAny<T> extends AbstractObservableWithUpstream<T, Boolean> {",
            "/**\n"
            " * 判断上游是否存在任一元素满足 {@link Predicate}；\n"
            " * 存在则 onNext(true) 后 onComplete，上游正常结束且无匹配时 onNext(false) 后 onComplete。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableAny<T> extends AbstractObservableWithUpstream<T, Boolean> {",
        ),
        (
            "    public ObservableAny(ObservableSource<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param predicate 逐元素测试谓词\n"
            "     */\n"
            "    public ObservableAny(ObservableSource<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super Boolean> t) {",
            "    /** 订阅 AnyObserver 并在首个匹配或完成时终止。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super Boolean> t) {",
        ),
        (
            "    static final class AnyObserver<T> implements Observer<T>, Disposable {",
            "    /** 逐元素测试 predicate，首个 true 或完成/错误时终止。 */\n"
            "    static final class AnyObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** predicate 为 true 时 dispose 上游并 emit true 后 onComplete。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 上游完成且无匹配时 emit false 后 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableAnySingle.java": [
        (
            "public final class ObservableAnySingle<T> extends Single<Boolean> implements FuseToObservable<Boolean> {",
            "/**\n"
            " * {@link ObservableAny} 的 Single 变体：\n"
            " * 存在满足 predicate 的元素时 onSuccess(true)，否则 onSuccess(false)。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableAnySingle<T> extends Single<Boolean> implements FuseToObservable<Boolean> {",
        ),
        (
            "    public ObservableAnySingle(ObservableSource<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param predicate 逐元素测试谓词\n"
            "     */\n"
            "    public ObservableAnySingle(ObservableSource<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> t) {",
            "    /** 订阅 AnyObserver 并映射结果为 Single 信号。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> t) {",
        ),
        (
            "    @Override\n    public Observable<Boolean> fuseToObservable() {",
            "    /** 融合为 {@link ObservableAny} 实例。 */\n"
            "    @Override\n    public Observable<Boolean> fuseToObservable() {",
        ),
        (
            "    static final class AnyObserver<T> implements Observer<T>, Disposable {",
            "    /** 逐元素测试 predicate，首个 true 或完成/错误时以 onSuccess 终止。 */\n"
            "    static final class AnyObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** predicate 为 true 时 dispose 上游并 onSuccess(true)。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 上游完成且无匹配时 onSuccess(false)。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableAutoConnect.java": [
        (
            "/**\n * Wraps a ConnectableObservable and calls its connect() method once\n * the specified number of Observers have subscribed.\n *\n * @param <T> the value type of the chain\n */",
            "/**\n"
            " * 包装 {@link ConnectableObservable}，在订阅者数量达到阈值时调用 {@code connect()}。\n"
            " *\n"
            " * @param <T> 链上元素类型\n"
            " */",
        ),
        (
            "    public ObservableAutoConnect(ConnectableObservable<? extends T> source,\n            int numberOfObservers,\n            Consumer<? super Disposable> connection) {",
            "    /**\n"
            "     * @param source 可连接的 ConnectableObservable\n"
            "     * @param numberOfObservers 触发 connect 所需的 Observer 数量\n"
            "     * @param connection connect 时接收 Disposable 的回调\n"
            "     */\n"
            "    public ObservableAutoConnect(ConnectableObservable<? extends T> source,\n            int numberOfObservers,\n            Consumer<? super Disposable> connection) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> child) {",
            "    /** 订阅上游并在 clients 达到 numberOfObservers 时 connect。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> child) {",
        ),
    ],
    "ObservableBlockingSubscribe.java": [
        (
            "/**\n * Utility methods to consume an Observable in a blocking manner with callbacks or Observer.\n */",
            "/**\n"
            " * 以阻塞方式消费 {@link ObservableSource} 的工具方法（Observer 或回调形式）。\n"
            " */",
        ),
        (
            "    /** Utility class. */",
            "    /** 工具类，禁止实例化。 */",
        ),
        (
            "    /**\n     * Subscribes to the source and calls the Observer methods on the current thread.\n     * <p>\n     * @param o the source ObservableSource\n     * The call to dispose() is composed through.\n     * @param observer the subscriber to forward events and calls to in the current thread\n     * @param <T> the value type\n     * @throws NullPointerException if {@code observer} is {@code null}\n     */",
            "    /**\n"
            "     * 订阅上游并在当前线程调用 Observer 方法。\n"
            "     * <p>\n"
            "     * @param o 上游 ObservableSource\n"
            "     * dispose() 调用会透传。\n"
            "     * @param observer 在当前线程接收事件的 Observer\n"
            "     * @param <T> 元素类型\n"
            "     * @throws NullPointerException 若 {@code observer} 为 {@code null}\n"
            "     */",
        ),
        (
            "    /**\n     * Runs the source observable to a terminal event, ignoring any values and rethrowing any exception.\n     * @param o the source ObservableSource\n     * @param <T> the value type\n     */",
            "    /**\n"
            "     * 阻塞运行上游至终止事件，忽略所有 onNext 并在有异常时重新抛出。\n"
            "     * @param o 上游 ObservableSource\n"
            "     * @param <T> 元素类型\n"
            "     */",
        ),
        (
            "    /**\n     * Subscribes to the source and calls the given actions on the current thread.\n     * @param o the source ObservableSource\n     * @param onNext the callback action for each source value\n     * @param onError the callback action for an error event\n     * @param onComplete the callback action for the completion event.\n     * @param <T> the value type\n     */",
            "    /**\n"
            "     * 订阅上游并在当前线程调用给定回调。\n"
            "     * @param o 上游 ObservableSource\n"
            "     * @param onNext 每个元素的回调\n"
            "     * @param onError 错误事件回调\n"
            "     * @param onComplete 完成事件回调\n"
            "     * @param <T> 元素类型\n"
            "     */",
        ),
    ],
    "ObservableBuffer.java": [
        (
            "public final class ObservableBuffer<T, U extends Collection<? super T>> extends AbstractObservableWithUpstream<T, U> {",
            "/**\n"
            " * 将上游元素按 count/skip 规则收集到 {@link Collection} 中并下发。\n"
            " * count==skip 时使用非重叠窗口；否则使用滑动/跳跃缓冲。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 缓冲区集合类型\n"
            " */\n"
            "public final class ObservableBuffer<T, U extends Collection<? super T>> extends AbstractObservableWithUpstream<T, U> {",
        ),
        (
            "    public ObservableBuffer(ObservableSource<T> source, int count, int skip, Supplier<U> bufferSupplier) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param count 每个缓冲区的目标元素数\n"
            "     * @param skip 启动新缓冲区的步长\n"
            "     * @param bufferSupplier 创建空缓冲区的工厂\n"
            "     */\n"
            "    public ObservableBuffer(ObservableSource<T> source, int count, int skip, Supplier<U> bufferSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super U> t) {",
            "    /** count==skip 时用 BufferExactObserver，否则用 BufferSkipObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super U> t) {",
        ),
        (
            "    static final class BufferExactObserver<T, U extends Collection<? super T>> implements Observer<T>, Disposable {",
            "    /** 非重叠固定大小缓冲：满 count 即 emit 并新建缓冲。 */\n"
            "    static final class BufferExactObserver<T, U extends Collection<? super T>> implements Observer<T>, Disposable {",
        ),
        (
            "        boolean createBuffer() {",
            "        /** 通过 bufferSupplier 创建新缓冲；失败时 error 下游。 */\n"
            "        boolean createBuffer() {",
        ),
        (
            "    static final class BufferSkipObserver<T, U extends Collection<? super T>>\n    extends AtomicBoolean implements Observer<T>, Disposable {",
            "    /** 按 skip 步长启动新缓冲，并在各活跃缓冲中累积元素。 */\n"
            "    static final class BufferSkipObserver<T, U extends Collection<? super T>>\n    extends AtomicBoolean implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {\n            while (!buffers.isEmpty()) {",
            "        /** 排空 deque 中剩余缓冲后 onComplete。 */\n"
            "        @Override\n        public void onComplete() {\n            while (!buffers.isEmpty()) {",
        ),
    ],
    "ObservableBufferBoundary.java": [
        (
            "public final class ObservableBufferBoundary<T, U extends Collection<? super T>, Open, Close>\nextends AbstractObservableWithUpstream<T, U> {",
            "/**\n"
            " * 由 open/close 边界 Observable 控制缓冲窗口：\n"
            " * open 信号开启新缓冲，close 信号关闭对应缓冲并下发。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 缓冲区集合类型\n"
            " * @param <Open> open 边界信号类型\n"
            " * @param <Close> close 边界信号类型\n"
            " */\n"
            "public final class ObservableBufferBoundary<T, U extends Collection<? super T>, Open, Close>\n"
            "extends AbstractObservableWithUpstream<T, U> {",
        ),
        (
            "    public ObservableBufferBoundary(ObservableSource<T> source, ObservableSource<? extends Open> bufferOpen,\n                                    Function<? super Open, ? extends ObservableSource<? extends Close>> bufferClose, Supplier<U> bufferSupplier) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param bufferOpen 发出 open 信号的 ObservableSource\n"
            "     * @param bufferClose 由 open 令牌映射 close ObservableSource 的函数\n"
            "     * @param bufferSupplier 创建空缓冲区的工厂\n"
            "     */\n"
            "    public ObservableBufferBoundary(ObservableSource<T> source, ObservableSource<? extends Open> bufferOpen,\n                                    Function<? super Open, ? extends ObservableSource<? extends Close>> bufferClose, Supplier<U> bufferSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super U> t) {",
            "    /** 先 onSubscribe parent，再订阅上游 BufferBoundaryObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super U> t) {",
        ),
        (
            "    static final class BufferBoundaryObserver<T, C extends Collection<? super T>, Open, Close>\n    extends AtomicInteger implements Observer<T>, Disposable {",
            "    /** 管理多缓冲窗口、边界订阅与 drain 队列。 */\n"
            "    static final class BufferBoundaryObserver<T, C extends Collection<? super T>, Open, Close>\n    extends AtomicInteger implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 将元素加入所有活跃缓冲。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void open(Open token) {",
            "        /** open 信号：创建缓冲并订阅对应 close ObservableSource。 */\n"
            "        void open(Open token) {",
        ),
        (
            "        void close(BufferCloseObserver<T, C> closer, long idx) {",
            "        /** close 信号：移除索引缓冲并入队 drain。 */\n"
            "        void close(BufferCloseObserver<T, C> closer, long idx) {",
        ),
        (
            "        void drain() {",
            "        /** 从 queue 取出完整缓冲并 onNext，处理 done/错误。 */\n"
            "        void drain() {",
        ),
        (
            "        static final class BufferOpenObserver<Open>\n        extends AtomicReference<Disposable>\n        implements Observer<Open>, Disposable {",
            "        /** 订阅 bufferOpen 并将 open 令牌 relay 到 parent。 */\n"
            "        static final class BufferOpenObserver<Open>\n        extends AtomicReference<Disposable>\n        implements Observer<Open>, Disposable {",
        ),
        (
            "    static final class BufferCloseObserver<T, C extends Collection<? super T>>\n    extends AtomicReference<Disposable>\n    implements Observer<Object>, Disposable {",
            "    /** 订阅 close 源；onNext/onComplete 时关闭对应索引缓冲。 */\n"
            "    static final class BufferCloseObserver<T, C extends Collection<? super T>>\n    extends AtomicReference<Disposable>\n    implements Observer<Object>, Disposable {",
        ),
    ],
    "ObservableBufferExactBoundary.java": [
        (
            "public final class ObservableBufferExactBoundary<T, U extends Collection<? super T>, B>\nextends AbstractObservableWithUpstream<T, U> {",
            "/**\n"
            " * 按 {@link ObservableSource} 边界信号将上游元素收集到 {@link Collection}；\n"
            " * 每次 boundary 发出信号时刷新并下发当前缓冲区。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 缓冲区类型\n"
            " * @param <B> 边界信号类型\n"
            " */\n"
            "public final class ObservableBufferExactBoundary<T, U extends Collection<? super T>, B>\n"
            "extends AbstractObservableWithUpstream<T, U> {",
        ),
        (
            "    public ObservableBufferExactBoundary(ObservableSource<T> source, ObservableSource<B> boundary, Supplier<U> bufferSupplier) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param boundary 边界信号 ObservableSource\n"
            "     * @param bufferSupplier 创建空缓冲区的工厂\n"
            "     */\n"
            "    public ObservableBufferExactBoundary(ObservableSource<T> source, ObservableSource<B> boundary, Supplier<U> bufferSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super U> t) {",
            "    /** 用 SerializedObserver 包装下游并订阅 BufferExactBoundaryObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super U> t) {",
        ),
        (
            "    static final class BufferExactBoundaryObserver<T, U extends Collection<? super T>, B>\n    extends QueueDrainObserver<T, U, U> implements Disposable {",
            "    /** 维护当前 buffer，boundary 信号触发 next() 切换缓冲。 */\n"
            "    static final class BufferExactBoundaryObserver<T, U extends Collection<? super T>, B>\n    extends QueueDrainObserver<T, U, U> implements Disposable {",
        ),
        (
            "        void next() {",
            "        /** 边界信号：emit 旧缓冲并创建新缓冲。 */\n"
            "        void next() {",
        ),
        (
            "    static final class BufferBoundaryObserver<T, U extends Collection<? super T>, B>\n    extends DisposableObserver<B> {",
            "    /** 订阅 boundary 并将信号 relay 到 parent.next()。 */\n"
            "    static final class BufferBoundaryObserver<T, U extends Collection<? super T>, B>\n    extends DisposableObserver<B> {",
        ),
    ],
    "ObservableCollect.java": [
        (
            "public final class ObservableCollect<T, U> extends AbstractObservableWithUpstream<T, U> {",
            "/**\n"
            " * 用 {@link Supplier} 创建可变容器，经 {@link BiConsumer} 逐元素累积，\n"
            " * 上游完成时下发累积结果。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 累积容器类型\n"
            " */\n"
            "public final class ObservableCollect<T, U> extends AbstractObservableWithUpstream<T, U> {",
        ),
        (
            "    public ObservableCollect(ObservableSource<T> source,\n            Supplier<? extends U> initialSupplier, BiConsumer<? super U, ? super T> collector) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param initialSupplier 创建初始累积容器的工厂\n"
            "     * @param collector 将元素并入容器的函数\n"
            "     */\n"
            "    public ObservableCollect(ObservableSource<T> source,\n            Supplier<? extends U> initialSupplier, BiConsumer<? super U, ? super T> collector) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super U> t) {",
            "    /** 创建初始容器并订阅 CollectObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super U> t) {",
        ),
        (
            "    static final class CollectObserver<T, U> implements Observer<T>, Disposable {",
            "    /** 逐元素 collector.accept，完成时 onNext 累积结果。 */\n"
            "    static final class CollectObserver<T, U> implements Observer<T>, Disposable {",
        ),
    ],
    "ObservableCollectSingle.java": [
        (
            "public final class ObservableCollectSingle<T, U> extends Single<U> implements FuseToObservable<U> {",
            "/**\n"
            " * {@link ObservableCollect} 的 Single 变体：\n"
            " * 上游完成时以 onSuccess 下发累积结果。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 累积容器类型\n"
            " */\n"
            "public final class ObservableCollectSingle<T, U> extends Single<U> implements FuseToObservable<U> {",
        ),
        (
            "    public ObservableCollectSingle(ObservableSource<T> source,\n            Supplier<? extends U> initialSupplier, BiConsumer<? super U, ? super T> collector) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param initialSupplier 创建初始累积容器的工厂\n"
            "     * @param collector 将元素并入容器的函数\n"
            "     */\n"
            "    public ObservableCollectSingle(ObservableSource<T> source,\n            Supplier<? extends U> initialSupplier, BiConsumer<? super U, ? super T> collector) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super U> t) {",
            "    /** 创建初始容器并订阅 CollectObserver。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super U> t) {",
        ),
        (
            "    @Override\n    public Observable<U> fuseToObservable() {",
            "    /** 融合为 {@link ObservableCollect} 实例。 */\n"
            "    @Override\n    public Observable<U> fuseToObservable() {",
        ),
        (
            "    static final class CollectObserver<T, U> implements Observer<T>, Disposable {",
            "    /** 逐元素 collector.accept，完成时 onSuccess 累积结果。 */\n"
            "    static final class CollectObserver<T, U> implements Observer<T>, Disposable {",
        ),
    ],
    "ObservableCombineLatest.java": [
        (
            "public final class ObservableCombineLatest<T, R> extends Observable<R> {",
            "/**\n"
            " * 组合多个 Observable 的最新值：各源均至少 emit 一次后才调用 combiner，\n"
            " * 之后任一源 emit 都会触发新的组合结果。\n"
            " * @param <T> 各源元素类型\n"
            " * @param <R> 组合结果类型\n"
            " */\n"
            "public final class ObservableCombineLatest<T, R> extends Observable<R> {",
        ),
        (
            "    public ObservableCombineLatest(ObservableSource<? extends T>[] sources,\n            Iterable<? extends ObservableSource<? extends T>> sourcesIterable,\n            Function<? super Object[], ? extends R> combiner, int bufferSize,\n            boolean delayError) {",
            "    /**\n"
            "     * @param sources Observable 数组（可为 null，则用 sourcesIterable）\n"
            "     * @param sourcesIterable 可迭代的 Observable 源\n"
            "     * @param combiner 组合 latest 数组的函数\n"
            "     * @param bufferSize 组合结果队列容量\n"
            "     * @param delayError 是否延迟错误至所有源终止\n"
            "     */\n"
            "    public ObservableCombineLatest(ObservableSource<? extends T>[] sources,\n            Iterable<? extends ObservableSource<? extends T>> sourcesIterable,\n            Function<? super Object[], ? extends R> combiner, int bufferSize,\n            boolean delayError) {",
        ),
        (
            "    @Override\n    @SuppressWarnings(\"unchecked\")\n    public void subscribeActual(Observer<? super R> observer) {",
            "    /** 解析源列表并订阅 LatestCoordinator。 */\n"
            "    @Override\n    @SuppressWarnings(\"unchecked\")\n    public void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class LatestCoordinator<T, R> extends AtomicInteger implements Disposable {",
            "    /** 维护各源 latest 数组、组合队列与 drain 循环。 */\n"
            "    static final class LatestCoordinator<T, R> extends AtomicInteger implements Disposable {",
        ),
        (
            "        void drain() {",
            "        /** 从 queue 取 latest 快照、apply combiner 并 onNext。 */\n"
            "        void drain() {",
        ),
        (
            "        void innerNext(int index, T item) {",
            "        /** 更新 index 处 latest；全部就绪时 clone 入队并 drain。 */\n"
            "        void innerNext(int index, T item) {",
        ),
        (
            "    static final class CombinerObserver<T, R> extends AtomicReference<Disposable> implements Observer<T> {",
            "    /** 订阅单个源并将信号 relay 到 LatestCoordinator。 */\n"
            "    static final class CombinerObserver<T, R> extends AtomicReference<Disposable> implements Observer<T> {",
        ),
    ],
    "ObservableConcatWithCompletable.java": [
        (
            "/**\n * Subscribe to a main Observable first, then when it completes normally, subscribe to a Single,\n * signal its success value followed by a completion or signal its error as is.\n * <p>History: 2.1.10 - experimental\n * @param <T> the element type of the main source and output type\n * @since 2.2\n */",
            "/**\n"
            " * 先订阅主流 {@link Observable}，正常完成后订阅 {@link CompletableSource}，\n"
            " * 再 relay Completable 的 onComplete/onError。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> 主流及输出元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ObservableConcatWithCompletable(Observable<T> source, CompletableSource other) {",
            "    /**\n"
            "     * @param source 主流 Observable\n"
            "     * @param other 主流完成后串行订阅的 CompletableSource\n"
            "     */\n"
            "    public ObservableConcatWithCompletable(Observable<T> source, CompletableSource other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 ConcatWithObserver 串行 relay 主流与 Completable。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class ConcatWithObserver<T>\n    extends AtomicReference<Disposable>\n    implements Observer<T>, CompletableObserver, Disposable {",
            "    /** 主流完成后切换为 CompletableObserver。 */\n"
            "    static final class ConcatWithObserver<T>\n    extends AtomicReference<Disposable>\n    implements Observer<T>, CompletableObserver, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 主流完成时订阅 other Completable；inner 完成时 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableConcatWithMaybe.java": [
        (
            "/**\n * Subscribe to a main Observable first, then when it completes normally, subscribe to a Maybe,\n * signal its success value followed by a completion or signal its error or completion signal as is.\n * <p>History: 2.1.10 - experimental\n * @param <T> the element type of the main source and output type\n * @since 2.2\n */",
            "/**\n"
            " * 先订阅主流 {@link Observable}，正常完成后订阅 {@link MaybeSource}，\n"
            " * onSuccess 时转发元素后 onComplete，或 relay onError/onComplete。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> 主流及输出元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ObservableConcatWithMaybe(Observable<T> source, MaybeSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 主流 Observable\n"
            "     * @param other 主流完成后串行订阅的 MaybeSource\n"
            "     */\n"
            "    public ObservableConcatWithMaybe(Observable<T> source, MaybeSource<? extends T> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 ConcatWithObserver 串行 relay 主流与 Maybe。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class ConcatWithObserver<T>\n    extends AtomicReference<Disposable>\n    implements Observer<T>, MaybeObserver<T>, Disposable {",
            "    /** 主流完成后切换为 MaybeObserver。 */\n"
            "    static final class ConcatWithObserver<T>\n    extends AtomicReference<Disposable>\n    implements Observer<T>, MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** Maybe onSuccess：onNext 元素后 onComplete。 */\n"
            "        @Override\n        public void onSuccess(T t) {",
        ),
    ],
    "ObservableConcatWithSingle.java": [
        (
            "/**\n * Subscribe to a main Observable first, then when it completes normally, subscribe to a Single,\n * signal its success value followed by a completion or signal its error as is.\n * <p>History: 2.1.10 - experimental\n * @param <T> the element type of the main source and output type\n * @since 2.2\n */",
            "/**\n"
            " * 先订阅主流 {@link Observable}，正常完成后订阅 {@link SingleSource}，\n"
            " * onSuccess 时转发元素后 onComplete，或 relay onError。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> 主流及输出元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ObservableConcatWithSingle(Observable<T> source, SingleSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 主流 Observable\n"
            "     * @param other 主流完成后串行订阅的 SingleSource\n"
            "     */\n"
            "    public ObservableConcatWithSingle(Observable<T> source, SingleSource<? extends T> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 ConcatWithObserver 串行 relay 主流与 Single。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class ConcatWithObserver<T>\n    extends AtomicReference<Disposable>\n    implements Observer<T>, SingleObserver<T>, Disposable {",
            "    /** 主流完成后切换为 SingleObserver。 */\n"
            "    static final class ConcatWithObserver<T>\n    extends AtomicReference<Disposable>\n    implements Observer<T>, SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** Single onSuccess：onNext 元素后 onComplete。 */\n"
            "        @Override\n        public void onSuccess(T t) {",
        ),
    ],
    "ObservableCount.java": [
        (
            "public final class ObservableCount<T> extends AbstractObservableWithUpstream<T, Long> {",
            "/**\n"
            " * 统计上游 emit 的元素个数，完成时 onNext(count) 后 onComplete。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableCount<T> extends AbstractObservableWithUpstream<T, Long> {",
        ),
        (
            "    public ObservableCount(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableCount(ObservableSource<T> source) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super Long> t) {",
            "    /** 订阅 CountObserver 递增计数。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super Long> t) {",
        ),
        (
            "    static final class CountObserver implements Observer<Object>, Disposable {",
            "    /** 忽略元素类型，仅递增 count。 */\n"
            "    static final class CountObserver implements Observer<Object>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 完成时 emit 累计 count。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableCountSingle.java": [
        (
            "public final class ObservableCountSingle<T> extends Single<Long> implements FuseToObservable<Long> {",
            "/**\n"
            " * {@link ObservableCount} 的 Single 变体：\n"
            " * 上游完成时 onSuccess(count)。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableCountSingle<T> extends Single<Long> implements FuseToObservable<Long> {",
        ),
        (
            "    public ObservableCountSingle(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableCountSingle(ObservableSource<T> source) {",
        ),
        (
            "    @Override\n    public void subscribeActual(SingleObserver<? super Long> t) {",
            "    /** 订阅 CountObserver 并以 onSuccess 返回计数。 */\n"
            "    @Override\n    public void subscribeActual(SingleObserver<? super Long> t) {",
        ),
        (
            "    @Override\n    public Observable<Long> fuseToObservable() {",
            "    /** 融合为 {@link ObservableCount} 实例。 */\n"
            "    @Override\n    public Observable<Long> fuseToObservable() {",
        ),
        (
            "    static final class CountObserver implements Observer<Object>, Disposable {",
            "    /** 忽略元素类型，完成时 onSuccess(count)。 */\n"
            "    static final class CountObserver implements Observer<Object>, Disposable {",
        ),
    ],
}
