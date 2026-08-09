"""Replacement tuples for RxJava wave-21a observable/parallel operators [0:15]."""

OBSERVABLE_W21A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ObservableToListSingle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableToListSingle",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 将 Observable 全部元素收集到 Collection，上游 onComplete 后以 Single 发射该集合。\n"
            " * 实现 {@link FuseToObservable}，可 fuse 回 {@link ObservableToList}。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 集合类型\n"
            " */\n"
            "public final class ObservableToListSingle",
        ),
        (
            "    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n    public ObservableToListSingle(ObservableSource<T> source, final int defaultCapacityHint) {",
            "    /**\n"
            "     * 使用默认 ArrayList 容量提示构造集合供应器。\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param defaultCapacityHint ArrayList 初始容量\n"
            "     */\n"
            "    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n    public ObservableToListSingle(ObservableSource<T> source, final int defaultCapacityHint) {",
        ),
        (
            "    public ObservableToListSingle(ObservableSource<T> source, Supplier<U> collectionSupplier) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param collectionSupplier 可变的集合供应器\n"
            "     */\n"
            "    public ObservableToListSingle(ObservableSource<T> source, Supplier<U> collectionSupplier) {",
        ),
        (
            "    @Override\n    public void subscribeActual(SingleObserver<? super U> t) {",
            "    /** 获取集合实例后订阅 ToListObserver。 */\n"
            "    @Override\n    public void subscribeActual(SingleObserver<? super U> t) {",
        ),
        (
            "    static final class ToListObserver<T, U extends Collection<? super T>> implements Observer<T>, Disposable {",
            "    /** 逐项 add 至 collection；onComplete 时 onSuccess 下发集合。 */\n"
            "    static final class ToListObserver<T, U extends Collection<? super T>> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 清空引用后 onSuccess 发射累积集合。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableUnsubscribeOn.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableUnsubscribeOn",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 在指定 {@link Scheduler} 上异步 dispose 上游，避免 dispose 与 onNext 同线程竞态。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableUnsubscribeOn",
        ),
        (
            "    public ObservableUnsubscribeOn(ObservableSource<T> source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param scheduler 执行 upstream.dispose 的调度器\n"
            "     */\n"
            "    public ObservableUnsubscribeOn(ObservableSource<T> source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
            "    /** 订阅 UnsubscribeObserver。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
        ),
        (
            "    static final class UnsubscribeObserver<T> extends AtomicBoolean implements Observer<T>, Disposable {",
            "    /** dispose 后忽略后续事件；在 scheduler 上 dispose 上游。 */\n"
            "    static final class UnsubscribeObserver<T> extends AtomicBoolean implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** CAS 置位后 scheduleDirect 执行 DisposeTask。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        final class DisposeTask implements Runnable {",
            "        /** 在 scheduler 线程上调用 upstream.dispose。 */\n"
            "        final class DisposeTask implements Runnable {",
        ),
    ],
    "ObservableUsing.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableUsing",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 资源管理模式：先获取资源 D，用其创建 ObservableSource，\n"
            " * 终止或 dispose 时通过 disposer 释放资源。\n"
            " * @param <T> 元素类型\n"
            " * @param <D> 资源类型\n"
            " */\n"
            "public final class ObservableUsing",
        ),
        (
            "    public ObservableUsing(Supplier<? extends D> resourceSupplier,\n            Function<? super D, ? extends ObservableSource<? extends T>> sourceSupplier,\n            Consumer<? super D> disposer,\n            boolean eager) {",
            "    /**\n"
            "     * @param resourceSupplier 资源供应器\n"
            "     * @param sourceSupplier 由资源创建 ObservableSource 的函数\n"
            "     * @param disposer 资源释放回调\n"
            "     * @param eager true 时在 onError/onComplete 前先释放资源\n"
            "     */\n"
            "    public ObservableUsing(Supplier<? extends D> resourceSupplier,\n            Function<? super D, ? extends ObservableSource<? extends T>> sourceSupplier,\n            Consumer<? super D> disposer,\n            boolean eager) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 获取资源、创建 source，失败时尝试 disposer 后 error。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class UsingObserver<T, D> extends AtomicBoolean implements Observer<T>, Disposable {",
            "    /** eager 模式在终止前释放资源；非 eager 在 terminate 后 disposeResource。 */\n"
            "    static final class UsingObserver<T, D> extends AtomicBoolean implements Observer<T>, Disposable {",
        ),
        (
            "        void disposeResource() {",
            "        /** CAS 保证 disposer 仅调用一次；异常走 RxJavaPlugins.onError。 */\n"
            "        void disposeResource() {",
        ),
    ],
    "ObservableWindow.java": [
        (
            "import io.reactivex.rxjava4.subjects.UnicastSubject;\n\npublic final class ObservableWindow",
            "import io.reactivex.rxjava4.subjects.UnicastSubject;\n\n"
            "/**\n"
            " * 将上游按 count/skip 切分为多个窗口 Observable。\n"
            " * count==skip 时用 WindowExactObserver；否则用滑动 WindowSkipObserver。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableWindow",
        ),
        (
            "    public ObservableWindow(ObservableSource<T> source, long count, long skip, int capacityHint) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param count 每个窗口最多元素数\n"
            "     * @param skip 窗口起始间隔（与 count 相等时为不重叠窗口）\n"
            "     * @param capacityHint UnicastSubject 容量提示\n"
            "     */\n"
            "    public ObservableWindow(ObservableSource<T> source, long count, long skip, int capacityHint) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super Observable<T>> t) {",
            "    /** count==skip 选 Exact，否则选 Skip 策略。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super Observable<T>> t) {",
        ),
        (
            "    static final class WindowExactObserver<T>\n    extends AtomicInteger\n    implements Observer<T>, Disposable, Runnable {",
            "    /** 固定大小不重叠窗口；refCount 归零时 dispose 上游。 */\n"
            "    static final class WindowExactObserver<T>\n    extends AtomicInteger\n    implements Observer<T>, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 懒创建 UnicastSubject 并下发 intercept；满 count 时 onComplete 当前窗口。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "    static final class WindowSkipObserver<T> extends AtomicInteger\n    implements Observer<T>, Disposable, Runnable {",
            "    /** 滑动窗口：ArrayDeque 维护多个 UnicastSubject，按 skip 开启新窗。 */\n"
            "    static final class WindowSkipObserver<T> extends AtomicInteger\n    implements Observer<T>, Disposable, Runnable {",
        ),
        (
            "        /** Counts how many elements were emitted to the very first window in windows. */",
            "        /** 记录已发射到 deque 首窗口的元素计数。 */",
        ),
    ],
    "ObservableWindowBoundary.java": [
        (
            "import io.reactivex.rxjava4.subjects.UnicastSubject;\n\npublic final class ObservableWindowBoundary",
            "import io.reactivex.rxjava4.subjects.UnicastSubject;\n\n"
            "/**\n"
            " * 由 other 流每个 onNext 作为边界，切分主序列成多个窗口 Observable。\n"
            " * @param <T> 主序列元素类型\n"
            " * @param <B> 边界信号类型\n"
            " */\n"
            "public final class ObservableWindowBoundary",
        ),
        (
            "    public ObservableWindowBoundary(ObservableSource<T> source, ObservableSource<B> other, int capacityHint) {",
            "    /**\n"
            "     * @param source 主序列 ObservableSource\n"
            "     * @param other 边界 ObservableSource\n"
            "     * @param capacityHint 每个 UnicastSubject 容量提示\n"
            "     */\n"
            "    public ObservableWindowBoundary(ObservableSource<T> source, ObservableSource<B> other, int capacityHint) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super Observable<T>> observer) {",
            "    /** 先 onSubscribe parent，再订阅 boundary 与主序列。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super Observable<T>> observer) {",
        ),
        (
            "    static final class WindowBoundaryMainObserver<T, B>\n    extends AtomicInteger\n    implements Observer<T>, Disposable, Runnable {",
            "    /** MpscLinkedQueue 串行 drain；NEXT_WINDOW 标记开启新窗。 */\n"
            "    static final class WindowBoundaryMainObserver<T, B>\n    extends AtomicInteger\n    implements Observer<T>, Disposable, Runnable {",
        ),
        (
            "        void drain() {",
            "        /** wip 门控：poll 元素入当前窗或 NEXT_WINDOW 时关闭旧窗开新窗。 */\n"
            "        void drain() {",
        ),
        (
            "    static final class WindowBoundaryInnerObserver<T, B> extends DisposableObserver<B> {",
            "    /** 边界 onNext 触发 parent.innerNext；完成/错误终止主序列。 */\n"
            "    static final class WindowBoundaryInnerObserver<T, B> extends DisposableObserver<B> {",
        ),
    ],
    "ObservableWindowSubscribeIntercept.java": [
        (
            "/**\n * Wrapper for a Subject that detects an incoming subscriber.\n * @param <T> the element type of the flow.\n * @since 3.0.0\n */",
            "/**\n * 包装 Subject 以检测是否有下游订阅。\n * 若窗口未被订阅，{@link #tryAbandon} 可提前关闭空窗。\n * @param <T> 流元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> s) {",
            "    /** 订阅底层 window 并标记 once。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> s) {",
        ),
        (
            "    boolean tryAbandon() {",
            "    /** 尚无订阅者时 CAS 置位，表示可放弃该空窗口。 */\n"
            "    boolean tryAbandon() {",
        ),
    ],
    "ObservableWithLatestFrom.java": [
        (
            "import io.reactivex.rxjava4.observers.SerializedObserver;\n\npublic final class ObservableWithLatestFrom",
            "import io.reactivex.rxjava4.observers.SerializedObserver;\n\n"
            "/**\n"
            " * 主序列每项与 other 最新值经 combiner 合并后发射。\n"
            " * other 尚未 emit 时主序列 onNext 被忽略。\n"
            " * @param <T> 主序列类型\n"
            " * @param <U> other 类型\n"
            " * @param <R> 合并结果类型\n"
            " */\n"
            "public final class ObservableWithLatestFrom",
        ),
        (
            "    public ObservableWithLatestFrom(ObservableSource<T> source,\n            BiFunction<? super T, ? super U, ? extends R> combiner, ObservableSource<? extends U> other) {",
            "    /**\n"
            "     * @param source 主序列 ObservableSource\n"
            "     * @param combiner (T, U) -> R 合并函数\n"
            "     * @param other 提供最新值的 ObservableSource\n"
            "     */\n"
            "    public ObservableWithLatestFrom(ObservableSource<T> source,\n            BiFunction<? super T, ? super U, ? extends R> combiner, ObservableSource<? extends U> other) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super R> t) {",
            "    /** SerializedObserver 包装下游；先订 other 再订主序列。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super R> t) {",
        ),
        (
            "    static final class WithLatestFromObserver<T, U, R> extends AtomicReference<U> implements Observer<T>, Disposable {",
            "    /** AtomicReference 存 other 最新值；主 onNext 时 combiner.apply。 */\n"
            "    static final class WithLatestFromObserver<T, U, R> extends AtomicReference<U> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** other 值非 null 时合并并 onNext；null combiner 结果或异常则 dispose+onError。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "    final class WithLatestFromOtherObserver implements Observer<U> {",
            "    /** other 侧 Observer：onNext 更新 parent.lazySet。 */\n"
            "    final class WithLatestFromOtherObserver implements Observer<U> {",
        ),
    ],
    "ObservableWithLatestFromMany.java": [
        (
            "/**\n * Combines a main sequence of values with the latest from multiple other sequences via\n * a selector function.\n *\n * @param <T> the main sequence's type\n * @param <R> the output type\n */",
            "/**\n * 主序列每项与多路 other 各自最新值组成数组，经 combiner 合并后发射。\n * 任一路 other 缺值时跳过该主序列项。\n *\n * @param <T> 主序列元素类型\n * @param <R> 输出类型\n */",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 无 other 时退化为 ObservableMap；否则订阅 WithLatestFromObserver 与各路 inner。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class WithLatestFromObserver<T, R>\n    extends AtomicInteger\n    implements Observer<T>, Disposable {",
            "    /** values 数组存各路最新值；主 onNext 凑齐后 combiner 并经 HalfSerializer 下发。 */\n"
            "    static final class WithLatestFromObserver<T, R>\n    extends AtomicInteger\n    implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 任一路 latest 为 null 则跳过；否则 combiner 合并并 HalfSerializer.onNext。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "    static final class WithLatestInnerObserver\n    extends AtomicReference<Disposable>\n    implements Observer<Object> {",
            "    /** 某路 other 的 Observer；onNext 写入 parent.values[index]。 */\n"
            "    static final class WithLatestInnerObserver\n    extends AtomicReference<Disposable>\n    implements Observer<Object> {",
        ),
    ],
    "ObservableZip.java": [
        (
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\npublic final class ObservableZip",
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\n"
            "/**\n"
            " * 多路 Observable 按索引对齐，每轮 zipper 合并一行后发射。\n"
            " * @param <T> 各路元素类型\n"
            " * @param <R> zipper 输出类型\n"
            " */\n"
            "public final class ObservableZip",
        ),
        (
            "    public ObservableZip(ObservableSource<? extends T>[] sources,\n            Iterable<? extends ObservableSource<? extends T>> sourcesIterable,\n            Function<? super Object[], ? extends R> zipper,\n            int bufferSize,\n            boolean delayError) {",
            "    /**\n"
            "     * @param sources 固定数组源（与 sourcesIterable 二选一）\n"
            "     * @param sourcesIterable 可迭代源\n"
            "     * @param zipper 数组合并函数\n"
            "     * @param bufferSize 每路 SpscLinkedArrayQueue 容量\n"
            "     * @param delayError 为 true 时延迟聚合错误\n"
            "     */\n"
            "    public ObservableZip(ObservableSource<? extends T>[] sources,\n            Iterable<? extends ObservableSource<? extends T>> sourcesIterable,\n            Function<? super Object[], ? extends R> zipper,\n            int bufferSize,\n            boolean delayError) {",
        ),
        (
            "    static final class ZipCoordinator<T, R> extends AtomicInteger implements Disposable {",
            "    /** 协调多路 ZipObserver；row 缓存当前轮各索引元素。 */\n"
            "    static final class ZipCoordinator<T, R> extends AtomicInteger implements Disposable {",
        ),
        (
            "        public void drain() {",
            "        /** wip 门控：各路 poll 凑齐一行后 zipper.apply 并 onNext。 */\n"
            "        public void drain() {",
        ),
        (
            "    static final class ZipObserver<T, R> implements Observer<T> {",
            "    /** onNext 入队 SpscLinkedArrayQueue 并 parent.drain。 */\n"
            "    static final class ZipObserver<T, R> implements Observer<T> {",
        ),
    ],
    "ObservableZipIterable.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableZipIterable",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 将 Observable 每项与 Iterable 迭代器当前项 zip 后发射；\n"
            " * Iterable 耗尽时 onComplete。\n"
            " * @param <T> Observable 元素类型\n"
            " * @param <U> Iterable 元素类型\n"
            " * @param <V> zipper 输出类型\n"
            " */\n"
            "public final class ObservableZipIterable",
        ),
        (
            "    public ObservableZipIterable(\n            Observable<? extends T> source,\n            Iterable<U> other, BiFunction<? super T, ? super U, ? extends V> zipper) {",
            "    /**\n"
            "     * @param source 主 Observable\n"
            "     * @param other 同步 Iterable\n"
            "     * @param zipper (T, U) -> V\n"
            "     */\n"
            "    public ObservableZipIterable(\n            Observable<? extends T> source,\n            Iterable<U> other, BiFunction<? super T, ? super U, ? extends V> zipper) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super V> t) {",
            "    /** 校验 iterator/hasNext；空 Iterable 直接 complete。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super V> t) {",
        ),
        (
            "    static final class ZipIterableObserver<T, U, V> implements Observer<T>, Disposable {",
            "    /** 每项 iterator.next 与 zipper 合并；无下一项时 dispose 并 onComplete。 */\n"
            "    static final class ZipIterableObserver<T, U, V> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** next+zipper；hasNext 为 false 时结束流。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObserverResourceWrapper.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class ObserverResourceWrapper",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 包装 Observer 并持有额外 Disposable 资源；\n"
            " * onError/onComplete/dispose 时一并释放 upstream 与 resource。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObserverResourceWrapper",
        ),
        (
            "    @Override\n    public void onError(Throwable t) {",
            "    /** dispose 后转发 onError。 */\n"
            "    @Override\n    public void onError(Throwable t) {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** 同时 dispose upstream 与本体 AtomicReference 中的 resource。 */\n"
            "    @Override\n    public void dispose() {",
        ),
        (
            "    public void setResource(Disposable resource) {",
            "    /** 设置需随 Observer 生命周期释放的额外 Disposable。 */\n"
            "    public void setResource(Disposable resource) {",
        ),
    ],
    "ParallelCollect.java": [
        (
            "/**\n * Reduce the sequence of values in each 'rail' to a single value.\n *\n * @param <T> the input value type\n * @param <C> the collection type\n */",
            "/**\n * 对 ParallelFlowable 每条 rail 累积收集为单个值（通常为集合）。\n * 各路 onComplete 时以 DeferredScalar 发射累积结果。\n *\n * @param <T> 输入元素类型\n * @param <C> 累积容器类型\n */",
        ),
        (
            "    public ParallelCollect(ParallelFlowable<? extends T> source,\n            Supplier<? extends C> initialCollection, BiConsumer<? super C, ? super T> collector) {",
            "    /**\n"
            "     * @param source 上游 ParallelFlowable\n"
            "     * @param initialCollection 每 rail 初始容器供应器\n"
            "     * @param collector 将元素并入容器的 BiConsumer\n"
            "     */\n"
            "    public ParallelCollect(ParallelFlowable<? extends T> source,\n            Supplier<? extends C> initialCollection, BiConsumer<? super C, ? super T> collector) {",
        ),
        (
            "    @Override\n    public void subscribe(Subscriber<? super C>[] subscribers) {",
            "    /** 为每 rail 创建 ParallelCollectSubscriber 并 request MAX。 */\n"
            "    @Override\n    public void subscribe(Subscriber<? super C>[] subscribers) {",
        ),
        (
            "    static final class ParallelCollectSubscriber<T, C> extends DeferredScalarSubscriber<T, C> {",
            "    /** onNext 调用 collector.accept；onComplete 时 complete(collection)。 */\n"
            "    static final class ParallelCollectSubscriber<T, C> extends DeferredScalarSubscriber<T, C> {",
        ),
    ],
    "ParallelConcatMap.java": [
        (
            "/**\n * Concatenates the generated Publishers on each rail.\n *\n * @param <T> the input value type\n * @param <R> the output value type\n */",
            "/**\n * 对 ParallelFlowable 每条 rail 做 concatMap：\n"
            " * 将元素映射为 Publisher 后顺序拼接发射。\n"
            " *\n * @param <T> 输入元素类型\n * @param <R> 输出元素类型\n */",
        ),
        (
            "    public ParallelConcatMap(\n            ParallelFlowable<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper,\n                    int prefetch, ErrorMode errorMode) {",
            "    /**\n"
            "     * @param source 上游 ParallelFlowable\n"
            "     * @param mapper 元素到 Publisher 的映射\n"
            "     * @param prefetch 预取缓冲\n"
            "     * @param errorMode 错误处理模式\n"
            "     */\n"
            "    public ParallelConcatMap(\n            ParallelFlowable<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper,\n                    int prefetch, ErrorMode errorMode) {",
        ),
        (
            "    @Override\n    public void subscribe(Subscriber<? super R>[] subscribers) {",
            "    /** 每 rail 用 FlowableConcatMap.subscribe 包装下游 Subscriber。 */\n"
            "    @Override\n    public void subscribe(Subscriber<? super R>[] subscribers) {",
        ),
    ],
    "ParallelDoOnNextTry.java": [
        (
            "/**\n * Calls a Consumer for each upstream value passing by\n * and handles any failure with a handler function.\n * <p>History: 2.0.8 - experimental\n * @param <T> the input value type\n * @since 2.2\n */",
            "/**\n * 对 ParallelFlowable 每条 rail 的 onNext 调用 Consumer；\n"
            " * 异常由 errorHandler 决定 RETRY/SKIP/STOP 等策略。\n"
            " * <p>History: 2.0.8 - experimental\n * @param <T> 输入元素类型\n * @since 2.2\n */",
        ),
        (
            "    @Override\n    public void subscribe(Subscriber<? super T>[] subscribers) {",
            "    /** ConditionalSubscriber 走条件分支，否则普通 ParallelDoOnNextSubscriber。 */\n"
            "    @Override\n    public void subscribe(Subscriber<? super T>[] subscribers) {",
        ),
        (
            "        @Override\n        public boolean tryOnNext(T t) {",
            "        /** 调用 onNext.accept；失败时按 ParallelFailureHandling 重试/跳过/终止。 */\n"
            "        @Override\n        public boolean tryOnNext(T t) {",
        ),
    ],
    "ParallelFilter.java": [
        (
            "/**\n * Filters each 'rail' of the source ParallelFlowable with a predicate function.\n *\n * @param <T> the input value type\n */",
            "/**\n * 对 ParallelFlowable 每条 rail 用 predicate 过滤元素。\n"
            " * 未通过时 request(1) 补拉。\n"
            " *\n * @param <T> 输入元素类型\n */",
        ),
        (
            "    public ParallelFilter(ParallelFlowable<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 ParallelFlowable\n"
            "     * @param predicate 过滤谓词\n"
            "     */\n"
            "    public ParallelFilter(ParallelFlowable<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    public void subscribe(Subscriber<? super T>[] subscribers) {",
            "    /** 按下游类型选择 Conditional 或普通 FilterSubscriber。 */\n"
            "    @Override\n    public void subscribe(Subscriber<? super T>[] subscribers) {",
        ),
        (
            "        @Override\n        public boolean tryOnNext(T t) {",
            "        /** predicate.test 为 true 时转发；异常则 cancel+onError。 */\n"
            "        @Override\n        public boolean tryOnNext(T t) {",
        ),
    ],
}
