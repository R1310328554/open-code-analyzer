"""Chinese JavaDoc replacements for RxJava wave30a JMH perf classes [0:14]."""

JMH_W30A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "src/jmh/java/io/reactivex/rxjava4/streamable/StreamableLastAsSinglePerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Streamable.range.lastOrError 经 blockingGet 取末元素的吞吐。\n"
            " * 文件内 /// 注释记录 whenComplete/wip 与同步消费循环等优化实验数据。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 times 构造 range.lastOrError 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object benchmark() {",
            "    /** lastOrError blockingGet 基准。 */\n"
            "    @Benchmark\n    public Object benchmark() {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/streamable/StreamableMapOptionalPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Streamable.mapOptional 与 collect(maxBy) 融合路径吞吐\n"
            " *（range 源、indexed 与 enumerated 源对比）。\n"
            " * 文件内 /// 注释含状态机与 Deferred/EnumerableSource 优化数据。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造 mapOptional 奇数过滤及 indexed/enumerated maxBy 三条链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object basic() {",
            "    /** range.mapOptional blockingLast 基准。 */\n"
            "    @Benchmark\n    public Object basic() {",
        ),
        (
            "    @Benchmark\n    public Object indexed() {",
            "    /** range 源 mapOptional+maxBy blockingLast 基准。 */\n"
            "    @Benchmark\n    public Object indexed() {",
        ),
        (
            "    @Benchmark\n    public Object enumerated() {",
            "    /** fromIterable 源 mapOptional+maxBy blockingLast 基准。 */\n"
            "    @Benchmark\n    public Object enumerated() {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/streamable/StreamableMapPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Streamable.map 与 collect(maxBy) 融合及算子融合优化对比。\n"
            " * 文件内 /// 注释记录 whenComplete、indexable/enumerable 融合等实验数据。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造 map(v+1) 及 indexed/enumerated maxBy 三条链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object basic() {",
            "    /** range.map blockingLast 基准。 */\n"
            "    @Benchmark\n    public Object basic() {",
        ),
        (
            "    @Benchmark\n    public Object indexed() {",
            "    /** range 源 map+maxBy blockingLast 基准。 */\n"
            "    @Benchmark\n    public Object indexed() {",
        ),
        (
            "    @Benchmark\n    public Object enumerated() {",
            "    /** fromIterable 源 map+maxBy blockingLast 基准。 */\n"
            "    @Benchmark\n    public Object enumerated() {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/streamable/StreamableSkipPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Streamable.skip 在 range 源及 indexed/enumerable 路径上的吞吐。\n"
            " * 文件内 /// 注释含 whenComplete、wip 与同步偏置优化实验数据。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 构造 skip(times/2) 及 indexed/enumerable collect 变体。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object benchmark() {",
            "    /** range.skip blockingLast 基准。 */\n"
            "    @Benchmark\n    public Object benchmark() {",
        ),
        (
            "    @Benchmark\n    public Object indexed() {",
            "    /** skip+collect(toList) indexed 路径 blockingLast 基准。 */\n"
            "    @Benchmark\n    public Object indexed() {",
        ),
        (
            "    @Benchmark\n    public Object enumerable() {",
            "    /** filter+skip+collect enumerable 路径 blockingLast 基准。 */\n"
            "    @Benchmark\n    public Object enumerable() {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/xmapz/FlowableConcatMapCompletablePerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable concatMap 空流、concatMap(Completable.toFlowable)\n"
            " * 与 concatMapCompletable 三种 Completable 映射路径的吞吐对比。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充数组并构造 plain/convert/dedicated 三条链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
            "    /** concatMap 映射 Flowable.empty 基准。 */\n"
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
            "    /** concatMap(Completable.complete().toFlowable) 转换路径基准。 */\n"
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
            "    /** concatMapCompletable 专用算子基准。 */\n"
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/xmapz/FlowableConcatMapMaybeEmptyPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable concatMap 空流、concatMap(Maybe.empty().toFlowable)\n"
            " * 与 concatMapMaybe(Maybe.empty) 三种空 Maybe 映射路径的吞吐对比。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充数组并构造 plain/convert/dedicated 三条空 Maybe 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
            "    /** concatMap 映射 Flowable.empty 基准。 */\n"
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
            "    /** concatMap(Maybe.empty().toFlowable) 转换路径基准。 */\n"
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
            "    /** concatMapMaybe(Maybe.empty) 专用算子基准。 */\n"
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/xmapz/FlowableConcatMapMaybePerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable concatMap(Flowable::just)、concatMap(Maybe.just.toFlowable)\n"
            " * 与 concatMapMaybe(Maybe::just) 三种 Maybe 映射路径的吞吐对比。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充数组并构造 plain/convert/dedicated 三条 Maybe.just 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
            "    /** concatMap(Flowable::just) 基准。 */\n"
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
            "    /** concatMap(Maybe.just.toFlowable) 转换路径基准。 */\n"
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
            "    /** concatMapMaybe(Maybe::just) 专用算子基准。 */\n"
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/xmapz/FlowableConcatMapSinglePerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable concatMap(Flowable::just)、concatMap(Single.just.toFlowable)\n"
            " * 与 concatMapSingle(Single::just) 三种 Single 映射路径的吞吐对比。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充数组并构造 plain/convert/dedicated 三条 Single.just 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
            "    /** concatMap(Flowable::just) 基准。 */\n"
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
            "    /** concatMap(Single.just.toFlowable) 转换路径基准。 */\n"
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
            "    /** concatMapSingle(Single::just) 专用算子基准。 */\n"
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/xmapz/FlowableFlatMapCompletablePerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable flatMap 空流、flatMap(Completable.toFlowable)\n"
            " * 与 flatMapCompletable 三种 Completable 映射路径的吞吐对比。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充数组并构造 plain/convert/dedicated 三条链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
            "    /** flatMap 映射 Flowable.empty 基准。 */\n"
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
            "    /** flatMap(Completable.complete().toFlowable) 转换路径基准。 */\n"
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
            "    /** flatMapCompletable 专用算子基准。 */\n"
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/xmapz/FlowableFlatMapMaybeEmptyPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable flatMap 空流、flatMap(Maybe.empty().toFlowable)\n"
            " * 与 flatMapMaybe(Maybe.empty) 三种空 Maybe 映射路径的吞吐对比。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充数组并构造 plain/convert/dedicated 三条空 Maybe 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
            "    /** flatMap 映射 Flowable.empty 基准。 */\n"
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
            "    /** flatMap(Maybe.empty().toFlowable) 转换路径基准。 */\n"
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
            "    /** flatMapMaybe(Maybe.empty) 专用算子基准。 */\n"
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/xmapz/FlowableFlatMapMaybePerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable flatMap(Flowable::just)、flatMap(Maybe.just.toFlowable)\n"
            " * 与 flatMapMaybe(Maybe::just) 三种 Maybe 映射路径的吞吐对比。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充数组并构造 plain/convert/dedicated 三条 Maybe.just 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
            "    /** flatMap(Flowable::just) 基准。 */\n"
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
            "    /** flatMap(Maybe.just.toFlowable) 转换路径基准。 */\n"
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
            "    /** flatMapMaybe(Maybe::just) 专用算子基准。 */\n"
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/xmapz/FlowableFlatMapSinglePerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable flatMap(Flowable::just)、flatMap(Single.just.toFlowable)\n"
            " * 与 flatMapSingle(Single::just) 三种 Single 映射路径的吞吐对比。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充数组并构造 plain/convert/dedicated 三条 Single.just 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
            "    /** flatMap(Flowable::just) 基准。 */\n"
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
            "    /** flatMap(Single.just.toFlowable) 转换路径基准。 */\n"
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
            "    /** flatMapSingle(Single::just) 专用算子基准。 */\n"
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/xmapz/FlowableSwitchMapCompletablePerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable switchMap 空流、switchMap(Completable.toFlowable)\n"
            " * 与 switchMapCompletable 三种 Completable 映射路径的吞吐对比。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充数组并构造 plain/convert/dedicated 三条链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
            "    /** switchMap 映射 Flowable.empty 基准。 */\n"
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
            "    /** switchMap(Completable.complete().toFlowable) 转换路径基准。 */\n"
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
            "    /** switchMapCompletable 专用算子基准。 */\n"
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
        ),
    ],
    "src/jmh/java/io/reactivex/rxjava4/xmapz/FlowableSwitchMapMaybeEmptyPerf.java": [
        (
            "@BenchmarkMode(Mode.Throughput)",
            "/**\n"
            " * JMH 基准：Flowable switchMap 空流、switchMap(Maybe.empty().toFlowable)\n"
            " * 与 switchMapMaybe(Maybe.empty) 三种空 Maybe 映射路径的吞吐对比。\n"
            " */\n"
            "@BenchmarkMode(Mode.Throughput)",
        ),
        (
            "    @Setup\n    public void setup() {",
            "    /** 按 count 填充数组并构造 plain/convert/dedicated 三条空 Maybe 链路。 */\n"
            "    @Setup\n    public void setup() {",
        ),
        (
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
            "    /** switchMap 映射 Flowable.empty 基准。 */\n"
            "    @Benchmark\n    public Object flowablePlain(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
            "    /** switchMap(Maybe.empty().toFlowable) 转换路径基准。 */\n"
            "    @Benchmark\n    public Object flowableConvert(Blackhole bh) {",
        ),
        (
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
            "    /** switchMapMaybe(Maybe.empty) 专用算子基准。 */\n"
            "    @Benchmark\n    public Object flowableDedicated(Blackhole bh) {",
        ),
    ],
}
