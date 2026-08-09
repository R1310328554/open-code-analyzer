"""Replacement tuples for RxJava wave-20a observable operators [0:15]."""

OBSERVABLE_W20A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ObservableScalarXMap.java": [
        (
            "/**\n"
            " * Utility classes to work with scalar-sourced XMap operators (where X == { flat, concat, switch }).\n"
            " */",
            "/**\n"
            " * 处理标量（Supplier）来源的 XMap 算子工具类（X 为 flat、concat、switch）。\n"
            " * 若上游已是标量，可短路映射并订阅，避免完整 Observable 链开销。\n"
            " */",
        ),
        (
            "    /** Utility class. */",
            "    /** 工具类，禁止实例化。 */",
        ),
        (
            "    /**\n"
            "     * Tries to subscribe to a possible Supplier source's mapped ObservableSource.\n"
            "     * @param <T> the input value type\n"
            "     * @param <R> the output value type\n"
            "     * @param source the source ObservableSource\n"
            "     * @param observer the subscriber\n"
            "     * @param mapper the function mapping a scalar value into an ObservableSource\n"
            "     * @return true if successful, false if the caller should continue with the regular path.\n"
            "     */",
            "    /**\n"
            "     * 尝试对可能为 {@link Supplier} 的上游做标量 XMap 订阅。\n"
            "     * @param <T> 输入标量类型\n"
            "     * @param <R> 映射后输出类型\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param observer 下游 Observer\n"
            "     * @param mapper 将标量映射为 ObservableSource 的函数\n"
            "     * @return 已走标量快路径时为 true；否则调用方应继续常规路径\n"
            "     */",
        ),
        (
            "    /**\n"
            "     * Maps a scalar value into an Observable and emits its values.\n"
            "     *\n"
            "     * @param <T> the scalar value type\n"
            "     * @param <U> the output value type\n"
            "     * @param value the scalar value to map\n"
            "     * @param mapper the function that gets the scalar value and should return\n"
            "     * an ObservableSource that gets streamed\n"
            "     * @return the new Observable instance\n"
            "     */",
            "    /**\n"
            "     * 将标量值映射为 Observable 并发射其元素。\n"
            "     *\n"
            "     * @param <T> 标量类型\n"
            "     * @param <U> 输出元素类型\n"
            "     * @param value 待映射标量\n"
            "     * @param mapper 接收标量并返回待订阅 ObservableSource 的函数\n"
            "     * @return 包装 {@link ScalarXMapObservable} 的新 Observable\n"
            "     */",
        ),
        (
            "    /**\n"
            "     * Maps a scalar value to an ObservableSource and subscribes to it.\n"
            "     *\n"
            "     * @param <T> the scalar value type\n"
            "     * @param <R> the mapped ObservableSource's element type.\n"
            "     */",
            "    /**\n"
            "     * 将标量映射为 ObservableSource 并订阅；映射结果仍为 Supplier 时走 {@link ScalarDisposable}。\n"
            "     *\n"
            "     * @param <T> 标量类型\n"
            "     * @param <R> 映射后 ObservableSource 元素类型\n"
            "     */",
        ),
        (
            "    /**\n"
            "     * Represents a Disposable that signals one onNext followed by an onComplete.\n"
            "     *\n"
            "     * @param <T> the value type\n"
            "     */",
            "    /**\n"
            "     * 表示仅发射一次 onNext 后 onComplete 的 Disposable，支持同步融合。\n"
            "     *\n"
            "     * @param <T> 标量元素类型\n"
            "     */",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 在 START 状态下 CAS 到 ON_NEXT，发射标量后 onComplete。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "ObservableScan.java": [
        (
            "public final class ObservableScan<T> extends AbstractObservableWithUpstream<T, T> {",
            "/**\n"
            " * 对上游序列做 scan（累积）：首项原样发射，之后每项与累积值经 {@link BiFunction} 合并后发射。\n"
            " *\n"
            " * @param <T> 上下游元素类型\n"
            " */\n"
            "public final class ObservableScan<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableScan(ObservableSource<T> source, BiFunction<T, T, T> accumulator) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param accumulator 累积函数 (accumulator, incoming) -> newValue\n"
            "     */\n"
            "    public ObservableScan(ObservableSource<T> source, BiFunction<T, T, T> accumulator) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
            "    /** 订阅 {@link ScanObserver}，在 onNext 中维护累积状态。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
        ),
        (
            "    static final class ScanObserver<T> implements Observer<T>, Disposable {",
            "    /** 持有当前累积值；首项直接缓存并下发，后续项经 accumulator 合并。 */\n"
            "    static final class ScanObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 首项直接发射；否则 apply 累积，null 结果触发 onError。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObservableScanSeed.java": [
        (
            "public final class ObservableScanSeed<T, R> extends AbstractObservableWithUpstream<T, R> {",
            "/**\n"
            " * 带初始种子值的 scan：订阅时先发射 seed，之后每项与累积值合并为 R 类型并发射。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 累积结果类型\n"
            " */\n"
            "public final class ObservableScanSeed<T, R> extends AbstractObservableWithUpstream<T, R> {",
        ),
        (
            "    public ObservableScanSeed(ObservableSource<T> source, Supplier<R> seedSupplier, BiFunction<R, ? super T, R> accumulator) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param seedSupplier 初始累积值供应器\n"
            "     * @param accumulator (accumulator, incoming) -> newValue\n"
            "     */\n"
            "    public ObservableScanSeed(ObservableSource<T> source, Supplier<R> seedSupplier, BiFunction<R, ? super T, R> accumulator) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super R> t) {",
            "    /** 获取 seed 后立即 onNext(seed)，再订阅 ScanSeedObserver。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super R> t) {",
        ),
        (
            "    static final class ScanSeedObserver<T, R> implements Observer<T>, Disposable {",
            "    /** onSubscribe 时向下游发射初始 seed；onNext 用 accumulator 更新并发射。 */\n"
            "    static final class ScanSeedObserver<T, R> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSubscribe(Disposable d) {",
            "        /** 校验 Disposable 后 onSubscribe，并立即 onNext 当前 seed。 */\n"
            "        @Override\n        public void onSubscribe(Disposable d) {",
        ),
    ],
    "ObservableSequenceEqual.java": [
        (
            "public final class ObservableSequenceEqual<T> extends Observable<Boolean> {",
            "/**\n"
            " * 逐元素比较两个 Observable 序列是否相等，结果以 {@code Observable<Boolean>} 发射。\n"
            " * 两路均完成且逐对匹配时发射 true；长度不等或某对不匹配时发射 false。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSequenceEqual<T> extends Observable<Boolean> {",
        ),
        (
            "    public ObservableSequenceEqual(ObservableSource<? extends T> first, ObservableSource<? extends T> second,\n"
            "                                   BiPredicate<? super T, ? super T> comparer, int bufferSize) {",
            "    /**\n"
            "     * @param first 第一路 ObservableSource\n"
            "     * @param second 第二路 ObservableSource\n"
            "     * @param comparer 逐元素相等判定\n"
            "     * @param bufferSize 每路 SPSC 队列容量\n"
            "     */\n"
            "    public ObservableSequenceEqual(ObservableSource<? extends T> first, ObservableSource<? extends T> second,\n"
            "                                   BiPredicate<? super T, ? super T> comparer, int bufferSize) {",
        ),
        (
            "    static final class EqualCoordinator<T> extends AtomicInteger implements Disposable {",
            "    /** 协调两路 EqualObserver，在 drain 中成对 poll 并 comparer.test。 */\n"
            "    static final class EqualCoordinator<T> extends AtomicInteger implements Disposable {",
        ),
        (
            "        void drain() {",
            "        /** 成对取元素比较；任一路出错或不等则取消并下发结果/错误。 */\n"
            "        void drain() {",
        ),
        (
            "    static final class EqualObserver<T> implements Observer<T> {",
            "    /** 将 onNext 入队 SpscLinkedArrayQueue，并触发 parent.drain。 */\n"
            "    static final class EqualObserver<T> implements Observer<T> {",
        ),
    ],
    "ObservableSequenceEqualSingle.java": [
        (
            "public final class ObservableSequenceEqualSingle<T> extends Single<Boolean> implements FuseToObservable<Boolean> {",
            "/**\n"
            " * 与 {@link ObservableSequenceEqual} 相同逻辑，结果以 {@link Single} 的 onSuccess 交付。\n"
            " * 实现 {@link FuseToObservable} 可融合回 Observable 形式。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSequenceEqualSingle<T> extends Single<Boolean> implements FuseToObservable<Boolean> {",
        ),
        (
            "    @Override\n    public Observable<Boolean> fuseToObservable() {",
            "    /** 融合为 {@link ObservableSequenceEqual} 以便 operator fusion。 */\n"
            "    @Override\n    public Observable<Boolean> fuseToObservable() {",
        ),
        (
            "    static final class EqualCoordinator<T> extends AtomicInteger implements Disposable {",
            "    /** 与 Observable 版类似，完成时调用 downstream.onSuccess 而非 onNext+onComplete。 */\n"
            "    static final class EqualCoordinator<T> extends AtomicInteger implements Disposable {",
        ),
        (
            "                    if (d1 && d2 && e1 && e2) {\n"
            "                        downstream.onSuccess(true);\n"
            "                        return;\n"
            "                    }",
            "                    /** 两路均完成且队列均空：序列相等。 */\n"
            "                    if (d1 && d2 && e1 && e2) {\n"
            "                        downstream.onSuccess(true);\n"
            "                        return;\n"
            "                    }",
        ),
    ],
    "ObservableSerialized.java": [
        (
            "public final class ObservableSerialized<T> extends AbstractObservableWithUpstream<T, T> {",
            "/**\n"
            " * 用 {@link SerializedObserver} 包装下游，保证 onNext/onError/onComplete 串行、不并发重入。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSerialized<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableSerialized(Observable<T> upstream) {",
            "    /** @param upstream 待序列化回调的上游 Observable */\n"
            "    public ObservableSerialized(Observable<T> upstream) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅上游并将事件经 SerializedObserver 转发给下游。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
    ],
    "ObservableSingleMaybe.java": [
        (
            "public final class ObservableSingleMaybe<T> extends Maybe<T> {",
            "/**\n"
            " * 消费上游 Observable，恰有一个元素时 onSuccess，零个时 onComplete，多于一个时 onError。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSingleMaybe<T> extends Maybe<T> {",
        ),
        (
            "    public ObservableSingleMaybe(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableSingleMaybe(ObservableSource<T> source) {",
        ),
        (
            "    static final class SingleElementObserver<T> implements Observer<T>, Disposable {",
            "    /** 缓存首个 onNext；第二个 onNext 触发 IllegalArgumentException。 */\n"
            "    static final class SingleElementObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 无缓存元素时 onComplete；否则 onSuccess 唯一元素。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableSingleSingle.java": [
        (
            "public final class ObservableSingleSingle<T> extends Single<T> {",
            "/**\n"
            " * 消费上游 Observable 并转为 Single：恰一个元素时 onSuccess；\n"
            " * 零个时用 defaultValue 或 {@link NoSuchElementException}；多于一个时 onError。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSingleSingle<T> extends Single<T> {",
        ),
        (
            "    public ObservableSingleSingle(ObservableSource<? extends T> source, T defaultValue) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param defaultValue 上游为空时的默认值（null 表示无默认）\n"
            "     */\n"
            "    public ObservableSingleSingle(ObservableSource<? extends T> source, T defaultValue) {",
        ),
        (
            "    static final class SingleElementObserver<T> implements Observer<T>, Disposable {",
            "    /** 与 Maybe 版类似，onComplete 时应用 defaultValue 或 NoSuchElementException。 */\n"
            "    static final class SingleElementObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 有唯一元素或 defaultValue 时 onSuccess，否则 onError(NoSuchElementException)。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableSkip.java": [
        (
            "public final class ObservableSkip<T> extends AbstractObservableWithUpstream<T, T> {",
            "/**\n"
            " * 跳过前 n 个 onNext，之后原样转发；onError/onComplete 不受影响。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSkip<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableSkip(ObservableSource<T> source, long n) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param n 要跳过的元素个数\n"
            "     */\n"
            "    public ObservableSkip(ObservableSource<T> source, long n) {",
        ),
        (
            "    static final class SkipObserver<T> implements Observer<T>, Disposable {",
            "    /** remaining 递减至 0 后开始向下游 onNext。 */\n"
            "    static final class SkipObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** remaining > 0 时仅递减；否则转发 t。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObservableSkipLast.java": [
        (
            "public final class ObservableSkipLast<T> extends AbstractObservableWithUpstream<T, T> {",
            "/**\n"
            " * 滑动窗口跳过最后 skip 个元素：用固定容量 {@link ArrayDeque} 缓冲，\n"
            " * 队列满后再 onNext 时先 poll 队首再 offer 新项。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSkipLast<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    static final class SkipLastObserver<T> extends ArrayDeque<T> implements Observer<T>, Disposable {",
            "    /** 自身即容量为 skip 的 deque；满员后 emit poll() 再 offer 新值。 */\n"
            "    static final class SkipLastObserver<T> extends ArrayDeque<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** size()==skip 时先下发队首，再将 t 入队。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObservableSkipLastTimed.java": [
        (
            "public final class ObservableSkipLastTimed<T> extends AbstractObservableWithUpstream<T, T> {",
            "/**\n"
            " * 按时间窗口跳过「最近 time 内」的元素：队列存 (timestamp, value)，\n"
            " * 仅当队首时间戳早于 now-time 时才 poll 并发射。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSkipLastTimed<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableSkipLastTimed(ObservableSource<T> source,\n"
            "            long time, TimeUnit unit, Scheduler scheduler, int bufferSize, boolean delayError) {",
            "    /**\n"
            "     * @param source 上游\n"
            "     * @param time 时间窗口长度\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 提供 now() 的调度器\n"
            "     * @param bufferSize SPSC 队列容量\n"
            "     * @param delayError true 时等队列排空再 onError\n"
            "     */\n"
            "    public ObservableSkipLastTimed(ObservableSource<T> source,\n"
            "            long time, TimeUnit unit, Scheduler scheduler, int bufferSize, boolean delayError) {",
        ),
        (
            "    static final class SkipLastTimedObserver<T> extends AtomicInteger implements Observer<T>, Disposable {",
            "    /** onNext 带时间戳入队；drain 循环释放已超出窗口的元素。 */\n"
            "    static final class SkipLastTimedObserver<T> extends AtomicInteger implements Observer<T>, Disposable {",
        ),
        (
            "        void drain() {",
            "        /** 队首时间戳 <= now-time 时 poll 时间戳与值并 onNext。 */\n"
            "        void drain() {",
        ),
    ],
    "ObservableSkipUntil.java": [
        (
            "public final class ObservableSkipUntil<T, U> extends AbstractObservableWithUpstream<T, T> {",
            "/**\n"
            " * 在 other 发出首个信号（onNext 或 onComplete）之前丢弃主序列 onNext；\n"
            " * 之后原样转发。other 的 onError 会终止整个流。\n"
            " *\n"
            " * @param <T> 主序列元素类型\n"
            " * @param <U> 门控序列元素类型\n"
            " */\n"
            "public final class ObservableSkipUntil<T, U> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> child) {",
            "    /** 用 SerializedObserver 包装下游，ArrayCompositeDisposable 管理双路订阅。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> child) {",
        ),
        (
            "    static final class SkipUntilObserver<T> implements Observer<T> {",
            "    /** notSkipping 由 other 置 true；notSkippingLocal 保证首个放行项也下发。 */\n"
            "    static final class SkipUntilObserver<T> implements Observer<T> {",
        ),
        (
            "    final class SkipUntil implements Observer<U> {",
            "    /** other 的 onNext 后 dispose 自身并打开主序列；onComplete 仅打开不 dispose。 */\n"
            "    final class SkipUntil implements Observer<U> {",
        ),
    ],
    "ObservableSkipWhile.java": [
        (
            "public final class ObservableSkipWhile<T> extends AbstractObservableWithUpstream<T, T> {",
            "/**\n"
            " * 在 predicate 对某元素返回 true 期间丢弃 onNext；\n"
            " * 首次返回 false 的元素及之后所有元素原样转发。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSkipWhile<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableSkipWhile(ObservableSource<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param predicate 返回 true 时继续跳过\n"
            "     */\n"
            "    public ObservableSkipWhile(ObservableSource<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    static final class SkipWhileObserver<T> implements Observer<T>, Disposable {",
            "    /** notSkipping 为 false 时对每项 test；首次 false 后永久转发。 */\n"
            "    static final class SkipWhileObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 已放行则直接 onNext；否则 predicate.test，false 时开启转发并 emit t。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObservableSubscribeOn.java": [
        (
            "public final class ObservableSubscribeOn<T> extends AbstractObservableWithUpstream<T, T> {",
            "/**\n"
            " * 在指定 {@link Scheduler} 上执行对上游的 subscribe，\n"
            " * 使订阅动作与下游调用线程分离；事件仍由上游所在线程发射。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSubscribeOn<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    @Override\n    public void subscribeActual(final Observer<? super T> observer) {",
            "    /** 先 onSubscribe(parent)，再 scheduleDirect(SubscribeTask) 在 scheduler 上订阅上游。 */\n"
            "    @Override\n    public void subscribeActual(final Observer<? super T> observer) {",
        ),
        (
            "    static final class SubscribeOnObserver<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {",
            "    /** upstream 存上游 Disposable；自身 AtomicReference 存 scheduleDirect 返回的 Disposable。 */\n"
            "    static final class SubscribeOnObserver<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {",
        ),
        (
            "    final class SubscribeTask implements Runnable {",
            "    /** 在 scheduler 线程执行 source.subscribe(parent)。 */\n"
            "    final class SubscribeTask implements Runnable {",
        ),
    ],
    "ObservableSwitchIfEmpty.java": [
        (
            "public final class ObservableSwitchIfEmpty<T> extends AbstractObservableWithUpstream<T, T> {",
            "/**\n"
            " * 若主序列在 onComplete 前未发射任何 onNext，则订阅 other 作为替代；\n"
            " * 否则行为与主序列相同。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableSwitchIfEmpty<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableSwitchIfEmpty(ObservableSource<T> source, ObservableSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 主 ObservableSource\n"
            "     * @param other 主序列空完成时的备用源\n"
            "     */\n"
            "    public ObservableSwitchIfEmpty(ObservableSource<T> source, ObservableSource<? extends T> other) {",
        ),
        (
            "    static final class SwitchIfEmptyObserver<T> implements Observer<T> {",
            "    /** empty 初始 true；首个 onNext 置 false；空完成时 other.subscribe(this) 复用同一 Observer。 */\n"
            "    static final class SwitchIfEmptyObserver<T> implements Observer<T> {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** empty 仍为 true 时切换订阅 other；否则正常 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
}
