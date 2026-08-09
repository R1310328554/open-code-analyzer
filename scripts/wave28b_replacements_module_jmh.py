"""Chinese JavaDoc replacements for RxJava wave28b module-info and JMH perf classes."""

MODULE_JMH_W28B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "module-info.java": [
        (
            "module io.reactivex.rxjava4 {",
            "/**\n * RxJava 4 模块描述符：导出 core、operators、plugins、subscribers 等公共 API 包。\n */\n"
            "module io.reactivex.rxjava4 {",
        ),
    ],
    "BinaryFlatMapPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Single/Maybe/Completable 经 flatMap/flattenAs/andThen\n"
            " * 展开为 Flowable/Observable 的吞吐对比（含 hide 变体）。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 times 构造各类型 flatMap 链路与 hide 变体。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void singleFlatMapPublisher(Blackhole bh) {",
            "    /** Single.flatMapPublisher 基准。 */\n"
            "    @Benchmark\n    public void singleFlatMapPublisher(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void completableFlattenAsObservable(Blackhole bh) {",
            "    /** Completable.andThen Observable 基准。 */\n"
            "    @Benchmark\n    public void completableFlattenAsObservable(Blackhole bh) {",
        ),
    ],
    "BlockingGetPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：各响应式类型 blockingFirst/blockingLast/blockingGet/blockingAwait 吞吐。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造 just/complete 单元素源。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowableBlockingFirst() {",
            "    /** Flowable.blockingFirst 基准。 */\n"
            "    @Benchmark\n    public Object flowableBlockingFirst() {",
        ),
        (
            "    @Benchmark\n    public void completable() {",
            "    /** Completable.blockingAwait 基准。 */\n"
            "    @Benchmark\n    public void completable() {",
        ),
    ],
    "BlockingPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：大数组 Flowable/Observable 的 blockingFirst/blockingLast 吞吐。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 times 填充数组并构造 fromArray 源。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowableBlockingLast() {",
            "    /** Flowable.blockingLast 基准。 */\n"
            "    @Benchmark\n    public Object flowableBlockingLast() {",
        ),
        (
            "    @Benchmark\n    public Object observableBlockingFirst() {",
            "    /** Observable.blockingFirst 基准。 */\n"
            "    @Benchmark\n    public Object observableBlockingFirst() {",
        ),
    ],
    "CallableAsyncPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：fromCallable 经 subscribeOn/observeOn 管道在各类型上的异步吞吐。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造 subscribeOn/observeOn/双调度 pipeline 链路。 */\n"
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
    "EachTypeFlatMapPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：range/flatMap(just|range) 在 Flowable、Observable、Single 上的吞吐。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造 range 与 flatMap just/range 变体。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void bpRangeMapJust(Blackhole bh) {",
            "    /** Flowable range.flatMap(just) 基准。 */\n"
            "    @Benchmark\n    public void bpRangeMapJust(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void singleJustMapJust(Blackhole bh) {",
            "    /** Single.flatMap(just) 基准。 */\n"
            "    @Benchmark\n    public void singleJustMapJust(Blackhole bh) {",
        ),
    ],
    "FlatMapJustPerf.java": [
        (
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：fromArray.flatMap(just) 在 Flowable 与 Observable 上的吞吐。\n"
            " */\n"
            "@SuppressWarnings(\"exports\")\n@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 times 构造 flatMap(just) 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public void flowable(Blackhole bh) {",
            "    /** Flowable flatMap just 基准。 */\n"
            "    @Benchmark\n    public void flowable(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public void observable(Blackhole bh) {",
            "    /** Observable flatMap just 基准。 */\n"
            "    @Benchmark\n    public void observable(Blackhole bh) {",
        ),
    ],
}
