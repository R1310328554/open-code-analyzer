"""Chinese JavaDoc replacements for RxJava 4.0.0-alpha-21 wave29b JMH perf classes [15:30]."""

JMH_W29B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "PerfInteropConsumer.java": [
        (
            "/**\n * A multi-type synchronous consumer that doesn't implement FlowableSubscriber and\n * thus should be treated by Flowable as a candidate for strict interop.\n */",
            "/**\n"
            " * 多类型同步 consumer：未实现 FlowableSubscriber，\n"
            " * Flowable 将其视为 strict interop 互操作候选。\n"
            " */",
        ),
        (
            "    public PerfInteropConsumer(Blackhole bh) {",
            "    /** @param bh JMH 黑洞，消费元素以防死代码消除 */\n"
            "    public PerfInteropConsumer(Blackhole bh) {",
        ),
        (
            "    @Override\n    public void onSubscribe(Subscription s) {",
            "    /** Flow Subscriber：无界 request。 */\n"
            "    @Override\n    public void onSubscribe(Subscription s) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 完成时向 Blackhole 写入哨兵值。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
    ],
    "PerfObserver.java": [
        (
            "@SuppressWarnings(\"exports\")\npublic final class PerfObserver implements Observer<Object> {",
            "/**\n"
            " * JMH 用 Observable Observer：Latch 等待终止，Blackhole 消费 onNext。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\npublic final class PerfObserver implements Observer<Object> {",
        ),
        (
            "    public PerfObserver(Blackhole bh) {",
            "    /** @param bh JMH 黑洞 */\n    public PerfObserver(Blackhole bh) {",
        ),
        (
            "    @Override\n    public void onNext(Object value) {",
            "    /** 将元素送入 Blackhole。 */\n"
            "    @Override\n    public void onNext(Object value) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 释放 Latch。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
    ],
    "PerfSubscriber.java": [
        (
            "@SuppressWarnings(\"exports\")\npublic class PerfSubscriber implements FlowableSubscriber<Object> {",
            "/**\n"
            " * JMH 用 FlowableSubscriber：无界 request，Latch 同步，Blackhole 消费元素。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\npublic class PerfSubscriber implements FlowableSubscriber<Object> {",
        ),
        (
            "    @Override\n    public void onSubscribe(Subscription s) {",
            "    /** 无界 request 以拉取全序列。 */\n"
            "    @Override\n    public void onSubscribe(Subscription s) {",
        ),
        (
            "    @Override\n    public void onNext(Object t) {",
            "    /** 将元素送入 Blackhole。 */\n"
            "    @Override\n    public void onNext(Object t) {",
        ),
    ],
    "PublishProcessorPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：PublishProcessor/PublishSubject onNext 吞吐（无界与有界背压订阅）。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup(Blackhole bh) {",
            "    /** 构造无界/有界 Processor 与 Subject 并预订阅 consumer。 */\n"
            "    @Setup\n    public void setup(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void unbounded1m() {",
            "    /** 无界 Processor 连续 100 万次 onNext。 */\n"
            "    @Benchmark\n    public void unbounded1m() {",
        ),
        (
            "    @Benchmark\n    public void bounded1k() {",
            "    /** 有界背压 Processor 连续 1000 次 onNext。 */\n"
            "    @Benchmark\n    public void bounded1k() {",
        ),
        (
            "    @Benchmark\n    public void subject1m() {",
            "    /** PublishSubject 连续 100 万次 onNext。 */\n"
            "    @Benchmark\n    public void subject1m() {",
        ),
    ],
    "RangePerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable.range 同步订阅吞吐（含 observeOn/subscribeOn 变体，部分已注释）。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 times 构造 range 及异步/管道变体。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object rangeSync(Blackhole bh) {",
            "    /** 同步 subscribe PerfSubscriber。 */\n"
            "    @Benchmark\n    public Object rangeSync(Blackhole bh) {",
        ),
    ],
    "ReducePerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Observable/Flowable fromArray.reduce 在 Single/Maybe 上的吞吐。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Override\n    public Integer apply(Integer t1, Integer t2) {",
            "    /** reduce 累加函数。 */\n"
            "    @Override\n    public Integer apply(Integer t1, Integer t2) {",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 填充数组并构造四种 reduce 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void obsSingle(Blackhole bh) {",
            "    /** Observable reduce 带初始值 → Single。 */\n"
            "    @Benchmark\n    public void obsSingle(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void flowMaybe(Blackhole bh) {",
            "    /** Flowable reduce 无初始值 → Maybe。 */\n"
            "    @Benchmark\n    public void flowMaybe(Blackhole bh) {",
        ),
    ],
    "RxVsStreamPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable 与 Observable range/flatMap 订阅吞吐对比。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造 range 与 flatMap(just|range) 的 Flowable/Observable 变体。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void range(Blackhole bh) {",
            "    /** Flowable.range 同步订阅。 */\n"
            "    @Benchmark\n    public void range(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void rangeObservableFlatMap(Blackhole bh) {",
            "    /** Observable range.flatMap(range) 订阅。 */\n"
            "    @Benchmark\n    public void rangeObservableFlatMap(Blackhole bh) {",
        ),
    ],
    "StrictPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：FlowableSubscriber 与 java.util.concurrent.Flow.Subscriber 严格互操作开销。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充 fromArray 源。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void internal(Blackhole bh) {",
            "    /** 使用 FlowableSubscriber（内部协议）。 */\n"
            "    @Benchmark\n    public void internal(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void external(Blackhole bh) {",
            "    /** 使用 Flow.Subscriber（外部互操作路径）。 */\n"
            "    @Benchmark\n    public void external(Blackhole bh) {",
        ),
        (
            "    record InternalConsumer(Blackhole bh, int cycles) implements FlowableSubscriber<Object> {",
            "    /** 内部 FlowableSubscriber，onNext 中 consumeCPU。 */\n"
            "    record InternalConsumer(Blackhole bh, int cycles) implements FlowableSubscriber<Object> {",
        ),
    ],
    "TakeUntilPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable/Observable takeUntil 与异步终止信号的竞争吞吐。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造百万 range 与 subscribeOn 的 takeUntil 终止源。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void flowable() {",
            "    /** Flowable takeUntil 自旋等待完成。 */\n"
            "    @Benchmark\n    public void flowable() {",
        ),
        (
            "    @Benchmark\n    public void observable() {",
            "    /** Observable takeUntil 自旋等待完成。 */\n"
            "    @Benchmark\n    public void observable() {",
        ),
    ],
    "ToFlowablePerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Maybe/Observable reduce 经 toFlowable/toObservable 与 blocking 取值的吞吐。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造 reduce 与 concatMap 嵌套 reduce 的 Maybe/Flowable/Observable 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowable() {",
            "    /** Maybe reduce blockingGet。 */\n"
            "    @Benchmark\n    public Object flowable() {",
        ),
        (
            "    @Benchmark\n    public Object observableInner() {",
            "    /** Observable concatMap 内层 reduce blockingLast。 */\n"
            "    @Benchmark\n    public Object observableInner() {",
        ),
    ],
    "XMapYPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable/Observable 各类 flatMap（Publisher/Single/Maybe/Completable/Iterable）吞吐矩阵。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 times 填充数组并构造 flatMap 全矩阵（含 asFlow/asObs 变体）。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void flowFlatMapFlowable1(Blackhole bh) {",
            "    /** Flowable flatMap(Flowable::just)。 */\n"
            "    @Benchmark\n    public void flowFlatMapFlowable1(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void flowFlatMapCompletable0(Blackhole bh) {",
            "    /** Flowable flatMapCompletable(complete)。 */\n"
            "    @Benchmark\n    public void flowFlatMapCompletable0(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void obsFlatMapObservable1(Blackhole bh) {",
            "    /** Observable flatMap(Observable::just)。 */\n"
            "    @Benchmark\n    public void obsFlatMapObservable1(Blackhole bh) {",
        ),
    ],
    "ParallelPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：flatMap/subscribeOn、groupBy+observeOn 与 Parallel.runOn 并行映射吞吐。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造 flatMap、groupBy 与 parallel().runOn().sequential() 三条链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    void subscribe(Flowable<Integer> f, Blackhole bh) {",
            "    /** 异步订阅 PerfAsyncConsumer 并 await 指定计数。 */\n"
            "    void subscribe(Flowable<Integer> f, Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void parallel(Blackhole bh) {",
            "    /** Parallel.runOn + map + sequential 基准。 */\n"
            "    @Benchmark\n    public void parallel(Blackhole bh) {",
        ),
    ],
    "StreamableCollectPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Streamable.range.collect(maxBy) 的 trampoline-reducer 开销与优化迭代对比。\n"
            " * 文件内 /// 注释含各优化阶段的实测数据。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造带/不带 hide 的 maxBy collect 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object benchmark() {",
            "    /** 标准 collect 路径 blockingFirst。 */\n"
            "    @Benchmark\n    public Object benchmark() {",
        ),
        (
            "    @Benchmark\n    public Object benchmarkHidden() {",
            "    /** hide 后 collect，隔离 range 融合优化。 */\n"
            "    @Benchmark\n    public Object benchmarkHidden() {",
        ),
    ],
    "StreamableConcatIterablePerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Streamable.concat(Iterable) 连接两段 range 的吞吐与分配优化对比。\n"
            " * 文件内 /// 注释记录 whenComplete/wip 等优化实验数据。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 连接两段等长 range Streamable。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object benchmark() {",
            "    /** concat 全序列 blockingLast。 */\n"
            "    @Benchmark\n    public Object benchmark() {",
        ),
    ],
    "StreamableFilterPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Streamable.filter 与 collect(maxBy) 融合路径吞吐（range/Iterable 源对比）。\n"
            " * 文件内 /// 注释含同步偏置与 EnumerableSource 优化数据。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造 filter 奇数、indexed max 与 enumerated max 三条链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object basic() {",
            "    /** range.filter 直接 blockingLast。 */\n"
            "    @Benchmark\n    public Object basic() {",
        ),
        (
            "    @Benchmark\n    public Object enumerated() {",
            "    /** fromIterable 源 filter+collect max blockingLast。 */\n"
            "    @Benchmark\n    public Object enumerated() {",
        ),
    ],
}
