"""Chinese JavaDoc replacements for RxJava wave29a JMH perf classes [0:15]."""

JMH_W29A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "src/jmh/java/io/reactivex/rxjava4/core/FlattenCrossMapPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：fromArray.flatMapIterable 展开大 Iterable 时\n"
            " * Flowable 与 Observable 的吞吐对比（交叉映射场景）。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 times 构造外层数组与内层 Iterable，并建立 flatMapIterable 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void flowable(Blackhole bh) {",
            "    /** Flowable flatMapIterable 基准。 */\n"
            "    @Benchmark\n    public void flowable(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void observable(Blackhole bh) {",
            "    /** Observable flatMapIterable 基准。 */\n"
            "    @Benchmark\n    public void observable(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/FlattenJustPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：fromArray.flatMapIterable 映射为单元素 Iterable 时\n"
            " * Flowable 与 Observable 的吞吐对比。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 times 填充数组并构造 flatMapIterable(singletonList) 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void flowable(Blackhole bh) {",
            "    /** Flowable flatMapIterable 基准。 */\n"
            "    @Benchmark\n    public void flowable(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void observable(Blackhole bh) {",
            "    /** Observable flatMapIterable 基准。 */\n"
            "    @Benchmark\n    public void observable(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/FlattenRangePerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：fromArray.flatMapIterable 展开固定小 Iterable（1、2）时\n"
            " * Flowable 与 Observable 的吞吐对比。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 times 构造 flatMapIterable(1,2) 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void flowable(Blackhole bh) {",
            "    /** Flowable flatMapIterable 基准。 */\n"
            "    @Benchmark\n    public void flowable(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void observable(Blackhole bh) {",
            "    /** Observable flatMapIterable 基准。 */\n"
            "    @Benchmark\n    public void observable(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/FlowableFlatMapCompletableAsyncPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable flatMapCompletable 与 flatMap(Completable.toFlowable)\n"
            " * 在 subscribeOn 异步 Completable 下的吞吐对比。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** Action 回调：模拟 inner Completable 的 CPU 工作量。 */\n"
            "    @Override\n    public void run() {",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 items/maxConcurrency 构造异步 flatMapCompletable 与 flatMap 变体。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flatMapCompletable(Blackhole bh) {",
            "    /** flatMapCompletable 异步基准。 */\n"
            "    @Benchmark\n    public Object flatMapCompletable(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/FlowableFlatMapCompletableSyncPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable flatMapCompletable 与 flatMap(Completable.toFlowable)\n"
            " * 在同步 Completable.complete 下的吞吐对比。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 items/maxConcurrency 构造同步 flatMapCompletable 与 flatMap 变体。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flatMap(Blackhole bh) {",
            "    /** flatMap(Completable.toFlowable) 同步基准。 */\n"
            "    @Benchmark\n    public Object flatMap(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flatMapCompletable(Blackhole bh) {",
            "    /** flatMapCompletable 同步基准。 */\n"
            "    @Benchmark\n    public Object flatMapCompletable(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/InputWithIncrementingInteger.java": [
        (
            "/**\n * Exposes an Observable and Observer that increments n Integers and consumes them in a Blackhole.\n */",
            "/**\n"
            " * JMH 输入基类：提供递增整数 Iterable、Flowable 与 firehose Publisher，\n"
            " * 供 flatMap/merge 等算子基准复用。\n"
            " */",
        ),
        (
            "    final class DefaultSubscriberImpl extends DefaultSubscriber<Integer> {",
            "    /** 将 onNext 整数消费到 Blackhole 的默认订阅者。 */\n"
            "    final class DefaultSubscriberImpl extends DefaultSubscriber<Integer> {",
        ),
        (
            "    record IncrementingIterable(int size) implements Iterable<Integer> {",
            "    /** 按需生成 0..size-1 递增整数的 Iterable。 */\n"
            "    record IncrementingIterable(int size) implements Iterable<Integer> {",
        ),
        (
            "    record IncrementingPublisher(int size) implements Publisher<Integer> {",
            "    /** 同步推送 0..size-1 的 Reactive Streams Publisher。 */\n"
            "    record IncrementingPublisher(int size) implements Publisher<Integer> {",
        ),
        (
            "    @Setup\n    public void setup(final Blackhole bh) {",
            "    /** 按 getSize() 初始化 range、unsafeCreate 与 iterable 输入源。 */\n"
            "    @Setup\n    public void setup(final Blackhole bh) {",
        ),
        (
            "    public PerfSubscriber newLatchedObserver() {",
            "    /** 创建带 CountDownLatch 的 PerfSubscriber。 */\n"
            "    public PerfSubscriber newLatchedObserver() {",
        ),
        (
            "    public FlowableSubscriber<Integer> newSubscriber() {",
            "    /** 创建消费到 Blackhole 的 FlowableSubscriber。 */\n"
            "    public FlowableSubscriber<Integer> newSubscriber() {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/JustAsyncPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：just(1) 经 subscribeOn/observeOn/双调度管道\n"
            " * 在 Flowable、Observable、Single、Completable、Maybe 上的异步吞吐。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 为各响应式类型构造 subscribeOn、observeOn 与 pipeline 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void subscribeOnFlowable(Blackhole bh) {",
            "    /** Flowable subscribeOn 基准。 */\n"
            "    @Benchmark\n    public void subscribeOnFlowable(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void pipelineMaybe(Blackhole bh) {",
            "    /** Maybe subscribeOn+observeOn 管道基准。 */\n"
            "    @Benchmark\n    public void pipelineMaybe(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/LatchedSingleObserver.java": [
        (
            "@SuppressWarnings(\"exports\")\npublic final class LatchedSingleObserver<T> implements SingleObserver<T> {",
            "/**\n"
            " * 带 CountDownLatch 的 SingleObserver：终止时将结果写入 Blackhole 并释放 latch。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\npublic final class LatchedSingleObserver<T> implements SingleObserver<T> {",
        ),
        (
            "    public LatchedSingleObserver(Blackhole bh) {",
            "    /** @param bh JMH Blackhole，用于消费 onSuccess 值。 */\n"
            "    public LatchedSingleObserver(Blackhole bh) {",
        ),
        (
            "    @Override\n    public void onSuccess(T value) {",
            "    /** 消费成功值并 countDown latch。 */\n"
            "    @Override\n    public void onSuccess(T value) {",
        ),
        (
            "    @Override\n    public void onError(Throwable e) {",
            "    /** 打印错误并 countDown latch。 */\n"
            "    @Override\n    public void onError(Throwable e) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/MemoryPerf.java": [
        (
            "/**\n * Measure various prepared flows about their memory usage and print the result\n * in a JMH compatible format; run {@link #main(String[])}.\n */",
            "/**\n"
            " * 内存占用探测：实例化各类 Flowable/Observable 链路并估算堆增量，\n"
            " * 以 JMH 兼容格式输出；运行 {@link #main(String[])}。\n"
            " */",
        ),
        (
            "    static long memoryUse() {",
            "    /** 读取当前堆已用字节数。 */\n"
            "    static long memoryUse() {",
        ),
        (
            "    static final class MyRx2Subscriber implements FlowableSubscriber<Object> {",
            "    /** 最小 FlowableSubscriber，仅保存 Subscription 引用。 */\n"
            "    static final class MyRx2Subscriber implements FlowableSubscriber<Object> {",
        ),
        (
            "    static final class MyRx2Observer implements io.reactivex.rxjava4.core.Observer<Object>, io.reactivex.rxjava4.core.SingleObserver<Object>,",
            "    /** 多类型 Observer 桩：保存 Disposable 以测量订阅对象内存。 */\n"
            "    static final class MyRx2Observer implements io.reactivex.rxjava4.core.Observer<Object>, io.reactivex.rxjava4.core.SingleObserver<Object>,",
        ),
        (
            "    static <U> void checkMemory(Callable<U> item, String name, String typeLib, int n) throws Exception {",
            "    /** 批量创建 n 个实例并打印 name/typeLib 对应的 MB 增量。 */\n"
            "    static <U> void checkMemory(Callable<U> item, String name, String typeLib, int n) throws Exception {",
        ),
        (
            "    public static void main(String[] args) throws Exception {",
            "    /** 遍历 Observable/Flowable 工厂、算子与 Subject/Processor 的内存场景。 */\n"
            "    public static void main(String[] args) throws Exception {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/ObservableFlatMapPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Observable fromArray.flatMap 嵌套内层 fromArray 的吞吐。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 构造外层与内层数组并建立 flatMap 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void flatMapXRange(Blackhole bh) {",
            "    /** flatMap 嵌套 range 基准。 */\n"
            "    @Benchmark\n    public void flatMapXRange(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/OperatorFlatMapPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable flatMap 同步/异步透传、双层嵌套等算子场景吞吐。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @State(Scope.Thread)\n    public static class Input extends InputWithIncrementingInteger {",
            "    /** JMH 状态：继承递增整数输入，size 参数化元素个数。 */\n"
            "    @State(Scope.Thread)\n    public static class Input extends InputWithIncrementingInteger {",
        ),
        (
            "    @Benchmark\n    public void flatMapIntPassthruSync(Input input) {",
            "    /** 同步 flatMap(Flowable::just) 透传基准。 */\n"
            "    @Benchmark\n    public void flatMapIntPassthruSync(Input input) {",
        ),
        (
            "    @Benchmark\n    public void flatMapIntPassthruAsync(Input input) throws InterruptedException {",
            "    /** 异步 flatMap(subscribeOn) 透传基准。 */\n"
            "    @Benchmark\n    public void flatMapIntPassthruAsync(Input input) throws InterruptedException {",
        ),
        (
            "    @Benchmark\n    public void flatMapTwoNestedSync(final Input input) {",
            "    /** 双层 flatMap 嵌套同步基准。 */\n"
            "    @Benchmark\n    public void flatMapTwoNestedSync(final Input input) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/OperatorMergePerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable.merge / mergeArray 在多种嵌套与并发配置下的吞吐。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Benchmark\n    public void oneStreamOfNthatMergesIn1(final InputMillion input) throws InterruptedException {",
            "    /** range.flatMap(just) 后 merge 单元素流基准。 */\n"
            "    @Benchmark\n    public void oneStreamOfNthatMergesIn1(final InputMillion input) throws InterruptedException {",
        ),
        (
            "    @Benchmark\n    public void mergeNAsyncStreamsOfN(final InputThousand input) throws InterruptedException {",
            "    /** N 路异步 range 流 merge 基准。 */\n"
            "    @Benchmark\n    public void mergeNAsyncStreamsOfN(final InputThousand input) throws InterruptedException {",
        ),
        (
            "    @State(Scope.Thread)\n    public static class InputForMergeN {",
            "    /** merge 输入：预构造 size 个 Flowable.just 列表。 */\n"
            "    @State(Scope.Thread)\n    public static class InputForMergeN {",
        ),
        (
            "    @State(Scope.Thread)\n    public static class InputMillion extends InputWithIncrementingInteger {",
            "    /** 百万级递增整数输入状态。 */\n"
            "    @State(Scope.Thread)\n    public static class InputMillion extends InputWithIncrementingInteger {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/PerfAsyncConsumer.java": [
        (
            "/**\n * A multi-type asynchronous consumer.\n */",
            "/**\n"
            " * 多类型异步消费者：实现 Flowable/Observer/Single/Completable/Maybe 接口，\n"
            " * 终止时 countDown CountDownLatch。\n"
            " */",
        ),
        (
            "    public PerfAsyncConsumer(Blackhole bh) {",
            "    /** @param bh JMH Blackhole，消费 onNext/onSuccess 值。 */\n"
            "    public PerfAsyncConsumer(Blackhole bh) {",
        ),
        (
            "    /**\n     * Wait for the terminal signal.\n     * @param count if less than 1001, a spin-wait is used\n     * @return this\n     */",
            "    /**\n"
            "     * 等待终止信号。\n"
            "     * @param count 小于 1001 时使用自旋等待\n"
            "     * @return this\n"
            "     */",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/PerfBoundedSubscriber.java": [
        (
            "/**\n * Performance subscriber with a one-time request from the upstream.\n */",
            "/**\n"
            " * 带有限 request 的性能订阅者：onSubscribe 时仅向上游请求指定数量。\n"
            " */",
        ),
        (
            "    public PerfBoundedSubscriber(Blackhole bh, long request) {",
            "    /** @param bh Blackhole；@param request 首次 request 数量。 */\n"
            "    public PerfBoundedSubscriber(Blackhole bh, long request) {",
        ),
        (
            "    @Override\n    public void onSubscribe(Subscription s) {",
            "    /** 向上游请求有限数量。 */\n"
            "    @Override\n    public void onSubscribe(Subscription s) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/core/PerfConsumer.java": [
        (
            "/**\n * A multi-type synchronous consumer.\n */",
            "/**\n"
            " * 多类型同步消费者：实现 Flowable/Observer/Single/Completable/Maybe 接口，\n"
            " * 立即 request(Long.MAX_VALUE) 并消费事件到 Blackhole。\n"
            " */",
        ),
        (
            "    public PerfConsumer(Blackhole bh) {",
            "    /** @param bh JMH Blackhole。 */\n"
            "    public PerfConsumer(Blackhole bh) {",
        ),
        (
            "    @Override\n    public void onSubscribe(Subscription s) {",
            "    /** 请求无界背压。 */\n"
            "    @Override\n    public void onSubscribe(Subscription s) {",
        ),
    ],
}
