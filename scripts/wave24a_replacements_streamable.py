"""Replacement tuples for RxJava wave-24a Streamable operators, queue and schedulers [0:15]."""

STREAMABLE_W24A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "StreamableRepeat.java": [
        (
            "import io.reactivex.rxjava4.internal.fuseable.HasUpstreamStreamableSource;\n\npublic record StreamableRepeat<T>(",
            "import io.reactivex.rxjava4.internal.fuseable.HasUpstreamStreamableSource;\n\n"
            "/**\n"
            " * 在上游正常结束后，按 {@code whenFunction} 决定是否重新订阅源 {@link Streamable}。\n"
            " * 每完成一轮消费调用 {@code whenFunction(completionCount)}，返回 true 则再次订阅。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public record StreamableRepeat<T>(",
        ),
        (
            "    static final class RepeatStreamer<T>\n    implements Streamer<T>, BiConsumer<Object, Throwable> {",
            "    /** 三阶段状态机：拉取上游 → finish → 调用 whenFunction 决定是否 repeat。 */\n"
            "    static final class RepeatStreamer<T>\n    implements Streamer<T>, BiConsumer<Object, Throwable> {",
        ),
        (
            "        void retrySource() {",
            "        /** wip 串行保护下重新订阅 source 并启动 next 回调链。 */\n"
            "        void retrySource() {",
        ),
        (
            "                // FIXME some operators don't clean up their StreamerCancellations so we hand out clean ones for now",
            "                // FIXME：部分算子未清理 StreamerCancellation，暂为每次重订阅派生新的 cancellation",
        ),
        (
            "            } else { // stage 3",
            "            } else { // 阶段 3：whenFunction 完成，true 则重试，false 则结束",
        ),
    ],
    "StreamableRetry.java": [
        (
            "import io.reactivex.rxjava4.internal.fuseable.HasUpstreamStreamableSource;\n\npublic record StreamableRetry<T>(",
            "import io.reactivex.rxjava4.internal.fuseable.HasUpstreamStreamableSource;\n\n"
            "/**\n"
            " * 上游出错时按 {@code whenFunction(failureCount, error)} 决定是否重新订阅。\n"
            " * 先 finish 当前 Streamer 再询问 whenFunction，true 则 retrySource。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public record StreamableRetry<T>(",
        ),
        (
            "    static final class RetryStreamer<T>\n    implements Streamer<T>, BiConsumer<Object, Throwable> {",
            "    /** 错误路径：next 失败 → finish → whenFunction 决定是否重试。 */\n"
            "    static final class RetryStreamer<T>\n    implements Streamer<T>, BiConsumer<Object, Throwable> {",
        ),
        (
            "        void retrySource() {",
            "        /** wip 串行保护下重新订阅 source。 */\n"
            "        void retrySource() {",
        ),
        (
            "                // FIXME some operators don't clean up their StreamerCancellations so we hand out clean ones for now",
            "                // FIXME：部分算子未清理 StreamerCancellation，暂为每次重订阅派生新的 cancellation",
        ),
        (
            "            } else { // stage 3",
            "            } else { // 阶段 3：whenFunction 完成，true 则重试，false 则向下游传播终止",
        ),
    ],
    "StreamableSingleFlattenAs.java": [
        (
            "import io.reactivex.rxjava4.operators.DeferredEnumerableSource;\n\npublic record StreamableSingleFlattenAs<T, U>(",
            "import io.reactivex.rxjava4.operators.DeferredEnumerableSource;\n\n"
            "/**\n"
            " * 订阅 {@link SingleSource}，将 onSuccess 值经 mapper 转为 {@link Iterable} 后作为 Streamable 逐元素发射。\n"
            " * 实现 {@link DeferredEnumerableSource} 以支持同步 nextSync 路径。\n"
            " * @param <T> Single 元素类型\n"
            " * @param <U> Iterable 元素类型\n"
            " */\n"
            "public record StreamableSingleFlattenAs<T, U>(",
        ),
        (
            "    static final class FlattenAsSingleObserver<T, U>\n    extends AtomicInteger\n    implements SingleObserver<T>, Streamer<U>, DisposableOnly,\n            DeferredEnumerableSource<U> {",
            "    /** onSuccess 时拉取 Iterable 首元素并缓存 Iterator；next 迭代剩余元素。 */\n"
            "    static final class FlattenAsSingleObserver<T, U>\n    extends AtomicInteger\n    implements SingleObserver<T>, Streamer<U>, DisposableOnly,\n            DeferredEnumerableSource<U> {",
        ),
        (
            "        @Override\n        public CompletionStage<Boolean> enumerableReady() {",
            "        /** 首个元素就绪前 next 返回 iteratorReady。 */\n"
            "        @Override\n        public CompletionStage<Boolean> enumerableReady() {",
        ),
        (
            "            // because onSuccess will pull out the first item",
            "            // onSuccess 已预取首元素，首次 nextSync 直接返回 true",
        ),
    ],
    "StreamableSkip.java": [
        (
            "import io.reactivex.rxjava4.operators.*;\n\npublic record StreamableSkip<T>(Streamable<T> source, long count) implements Streamable<T> {",
            "import io.reactivex.rxjava4.operators.*;\n\n"
            "/**\n"
            " * 跳过前 {@code count} 个元素后转发上游 Streamable。\n"
            " * 若上游实现 {@link IndexableSource}、{@link EnumerableSource} 或\n"
            " * {@link DeferredEnumerableSource}，则选用对应优化实现。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public record StreamableSkip<T>(Streamable<T> source, long count) implements Streamable<T> {",
        ),
        (
            "    static abstract class SkipStreamer<T> extends AtomicInteger implements Streamer<T>, BiConsumer<Boolean, Throwable> {",
            "    /** remaining>0 时 drain 跳过；remaining<=0 后直接转发 upstream.next。 */\n"
            "    static abstract class SkipStreamer<T> extends AtomicInteger implements Streamer<T>, BiConsumer<Boolean, Throwable> {",
        ),
        (
            "        void drain() {",
            "        /** 同步或异步拉取 upstream 直至跳过足够元素或上游结束。 */\n"
            "        void drain() {",
        ),
        (
            "                        // still skipping, try the next upstream value",
            "                        // 仍在跳过阶段，继续拉取上游下一项",
        ),
        (
            "    static final class SkipStreamerIndexed<T> extends SkipStreamer<T>\n    implements IndexableSource<T> {",
            "    /** 索引访问时 elementAt 偏移 count；limit 减去已跳过数量。 */\n"
            "    static final class SkipStreamerIndexed<T> extends SkipStreamer<T>\n    implements IndexableSource<T> {",
        ),
        (
            "    static final class SkipStreamerEnumerable<T> extends SkipStreamer<T>\n    implements EnumerableSource<T> {",
            "    /** 同步路径：先 nextSync 跳过 count 次再转发。 */\n"
            "    static final class SkipStreamerEnumerable<T> extends SkipStreamer<T>\n    implements EnumerableSource<T> {",
        ),
        (
            "    static final class SkipStreamerDeferredEnumerable<T> extends SkipStreamer<T>\n    implements DeferredEnumerableSource<T> {",
            "    /** 延迟可枚举路径：跳过后转发 enumerableReady 与 nextSync。 */\n"
            "    static final class SkipStreamerDeferredEnumerable<T> extends SkipStreamer<T>\n    implements DeferredEnumerableSource<T> {",
        ),
    ],
    "StreamableTake.java": [
        (
            "import io.reactivex.rxjava4.operators.*;\n\npublic record StreamableTake<T>(Streamable<T> source, long count)\nimplements Streamable<T>, HasUpstreamStreamableSource<T> {",
            "import io.reactivex.rxjava4.operators.*;\n\n"
            "/**\n"
            " * 仅转发前 {@code count} 个元素，之后 next 返回 false。\n"
            " * 上游为 {@link IndexableSource} 等时可选用优化 Streamer 实现。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public record StreamableTake<T>(Streamable<T> source, long count)\nimplements Streamable<T>, HasUpstreamStreamableSource<T> {",
        ),
        (
            "    static abstract class TakeStreamerBase<T> implements Streamer<T> {",
            "    /** remaining 递减至 0 后 next 固定返回 NEXT_FALSE。 */\n"
            "    static abstract class TakeStreamerBase<T> implements Streamer<T> {",
        ),
        (
            "    static final class TakeStreamerIndexable<T> extends TakeStreamerBase<T>\n    implements IndexableSource<T> {",
            "    /** limit 为 min(count, 上游 limit)。 */\n"
            "    static final class TakeStreamerIndexable<T> extends TakeStreamerBase<T>\n    implements IndexableSource<T> {",
        ),
        (
            "    static final class TakeStreamerEnumerable<T> extends TakeStreamerBase<T>\n    implements EnumerableSource<T> {",
            "    /** 同步路径：最多 nextSync count 次。 */\n"
            "    static final class TakeStreamerEnumerable<T> extends TakeStreamerBase<T>\n    implements EnumerableSource<T> {",
        ),
        (
            "    static final class TakeStreamerDeferredEnumerable<T> extends TakeStreamerBase<T>\n    implements DeferredEnumerableSource<T> {",
            "    /** 延迟可枚举 take：转发 enumerableReady。 */\n"
            "    static final class TakeStreamerDeferredEnumerable<T> extends TakeStreamerBase<T>\n    implements DeferredEnumerableSource<T> {",
        ),
    ],
    "StreamableTakeUntil.java": [
        (
            "import io.reactivex.rxjava4.internal.fuseable.HasUpstreamStreamableSource;\n\npublic record StreamableTakeUntil<T, U>(",
            "import io.reactivex.rxjava4.internal.fuseable.HasUpstreamStreamableSource;\n\n"
            "/**\n"
            " * 转发主源元素直至 other 发出首个元素或主源结束；other 先完成则取消主源。\n"
            " * 使用 {@link StreamableHelper#race} 竞速 mainNext 与 otherNext。\n"
            " * @param <T> 主源元素类型\n"
            " * @param <U> other 源元素类型\n"
            " */\n"
            "public record StreamableTakeUntil<T, U>(",
        ),
        (
            "    static final class TakeUntilMainStreamer<T, U> implements Streamer<T> {",
            "    /** 并行订阅 main 与 other；other 获胜时 dispose 主 cancellation。 */\n"
            "    static final class TakeUntilMainStreamer<T, U> implements Streamer<T> {",
        ),
        (
            "        public TakeUntilMainStreamer(",
            "        /**\n"
            "         * @param upstream 主 Streamer\n"
            "         * @param otherStreamer other 的 Streamer\n"
            "         * @param mainCancellation 主源 cancellation\n"
            "         * @param otherCancellation other cancellation\n"
            "         * @param otherNext 已启动的 other.next（抑制值并绑定取消）\n"
            "         */\n"
            "        public TakeUntilMainStreamer(",
        ),
    ],
    "StreamableTakeWhile.java": [
        (
            "import io.reactivex.rxjava4.internal.fuseable.HasUpstreamStreamableSource;\n\npublic record StreamableTakeWhile<T>(Streamable<T> source, Predicate<? super T> predicate)\nimplements Streamable<T>, HasUpstreamStreamableSource<T> {",
            "import io.reactivex.rxjava4.internal.fuseable.HasUpstreamStreamableSource;\n\n"
            "/**\n"
            " * 转发元素直至 predicate 对 current() 返回 false 或上游结束。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public record StreamableTakeWhile<T>(Streamable<T> source, Predicate<? super T> predicate)\nimplements Streamable<T>, HasUpstreamStreamableSource<T> {",
        ),
        (
            "    static final class TakeWhileStreamer<T>\n    implements Streamer<T>, java.util.function.Function<Boolean, CompletionStage<Boolean>> {",
            "    /** upstream.next 成功后以 predicate 检验 current，false 则终止序列。 */\n"
            "    static final class TakeWhileStreamer<T>\n    implements Streamer<T>, java.util.function.Function<Boolean, CompletionStage<Boolean>> {",
        ),
    ],
    "StreamableTimeout.java": [
        (
            "import io.reactivex.rxjava4.disposables.*;\n\npublic record StreamableTimeout<T>(",
            "import io.reactivex.rxjava4.disposables.*;\n\n"
            "/**\n"
            " * 每次 next 与调度器超时竞速；超时则 dispose 主源并切换至 fallback Streamable。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public record StreamableTimeout<T>(",
        ),
        (
            "    static final class TimeoutStreamer<T> implements Streamer<T> {",
            "    /** mainNext 与 worker 定时任务 whenEither；超时后订阅 fallback。 */\n"
            "    static final class TimeoutStreamer<T> implements Streamer<T> {",
        ),
        (
            "        TimeoutStreamer(",
            "        /**\n"
            "         * @param mainStreamer 主 Streamer\n"
            "         * @param mainDisposable 主源 disposable\n"
            "         * @param downstreamDisposable 下游 cancellation\n"
            "         * @param timeout 超时时长\n"
            "         * @param unit 时间单位\n"
            "         * @param worker 执行定时任务的 Scheduler.Worker\n"
            "         * @param fallback 超时后的备用 Streamable\n"
            "         */\n"
            "        TimeoutStreamer(",
        ),
    ],
    "StreamableTimer.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic record StreamableTimer(long delay, @NonNull TimeUnit unit,",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 延迟后发射单个 {@code 0L}，随后 next 返回 false。\n"
            " * 可通过 {@link Scheduler} 或 {@link ExecutorService} 调度。\n"
            " */\n"
            "public record StreamableTimer(long delay, @NonNull TimeUnit unit,",
        ),
        (
            "    public StreamableTimer {",
            "    /** scheduler 与 executor 不可同时为 null。 */\n"
            "    public StreamableTimer {",
        ),
        (
            "    static final class TimerStreamer extends AtomicReference<Disposable> implements Streamer<Long>,  Runnable, Disposable {",
            "    /** run 完成 waiter；dispose 时以 CancellationException 结束 waiter。 */\n"
            "    static final class TimerStreamer extends AtomicReference<Disposable> implements Streamer<Long>,  Runnable, Disposable {",
        ),
        (
            "        void interrupedSleep(InterruptedException ex) {",
            "        /** 非 dispose 状态下 sleep 被中断则 exceptional 完成 waiter。 */\n"
            "        void interrupedSleep(InterruptedException ex) {",
        ),
    ],
    "StreamableToObservable.java": [
        (
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\npublic final class StreamableToObservable<T> extends Observable<T>\nimplements HasUpstreamStreamableSource<T> {",
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\n"
            "/**\n"
            " * 将 {@link Streamable} 桥接为 {@link Observable}：循环 next 映射为 onNext，\n"
            " * 结束或错误时 onComplete/onError。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class StreamableToObservable<T> extends Observable<T>\nimplements HasUpstreamStreamableSource<T> {",
        ),
        (
            "    public StreamableToObservable(Streamable<T> source) {",
            "    /** @param source 上游 Streamable */\n"
            "    public StreamableToObservable(Streamable<T> source) {",
        ),
        (
            "    record StreamToObserver<T>(Streamer<T> streamer,",
            "    /** wip 串行 drain：未完成时 next，done 后 finish 并通知 Observer。 */\n"
            "    record StreamToObserver<T>(Streamer<T> streamer,",
        ),
        (
            "        void drain() {",
            "        /** 自旋 drain next/finish 直至 wip 归零。 */\n"
            "        void drain() {",
        ),
    ],
    "StreamableUsing.java": [
        (
            "import io.reactivex.rxjava4.functions.*;\n\npublic record StreamableUsing<T, R>(",
            "import io.reactivex.rxjava4.functions.*;\n\n"
            "/**\n"
            " * 资源管理模式：resourceSupplier 获取资源，resourceMapper 创建 Streamable，\n"
            " * finish 时 resourceCleaner 释放资源。\n"
            " * @param <T> 元素类型\n"
            " * @param <R> 资源类型\n"
            " */\n"
            "public record StreamableUsing<T, R>(",
        ),
        (
            "    static final class UsingStreamer<T, R> implements Streamer<T> {",
            "    /** finish 时先 upstream.finish 再 run cleanup（仅一次）。 */\n"
            "    static final class UsingStreamer<T, R> implements Streamer<T> {",
        ),
    ],
    "StreamableZip.java": [
        (
            "import io.reactivex.rxjava4.internal.util.AtomicThrowable;\n\npublic record StreamableZip<T>(",
            "import io.reactivex.rxjava4.internal.util.AtomicThrowable;\n\n"
            "/**\n"
            " * 并行拉取多个 Streamable 的下一项，全部成功则 current 为 {@link List} 快照。\n"
            " * 任一源结束或出错则协调取消并汇总异常。\n"
            " * @param <T> 各源元素类型\n"
            " */\n"
            "public record StreamableZip<T>(",
        ),
        (
            "    static final class ZipStreamer<T>\n    implements Streamer<List<T>>, BiConsumer<Object, Throwable> {",
            "    /** wip 计数各 inner next；working 数组暂存 current 值后组装 List.of。 */\n"
            "    static final class ZipStreamer<T>\n    implements Streamer<List<T>>, BiConsumer<Object, Throwable> {",
        ),
        (
            "        void whenComplete(int index, Boolean b, Throwable t) {",
            "        /** 单路 next 完成：错误则 dispose 并唤醒其余 waiter；全部 true 则 complete nextReady。 */\n"
            "        void whenComplete(int index, Boolean b, Throwable t) {",
        ),
    ],
    "MpscLinkedQueue.java": [
        (
            "/*\n * The code was inspired by the similarly named JCTools class:\n * https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/queues/atomic\n */",
            "/*\n * 实现思路参考 JCTools 同名类：\n * https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/queues/atomic\n */",
        ),
        (
            "/**\n * A multi-producer single consumer unbounded queue.\n * @param <T> the contained value type\n */",
            "/**\n * 多生产者、单消费者无界链表队列。\n * @param <T> 元素类型\n */",
        ),
        (
            "        xchgProducerNode(node); // this ensures correct construction: StoreLoad",
            "        xchgProducerNode(node); // StoreLoad 保证构造可见性",
        ),
        (
            "    /**\n     * {@inheritDoc} <br>\n     * <p>\n     * IMPLEMENTATION NOTES:<br>\n     * Offer is allowed from multiple threads.<br>\n     * Offer allocates a new node and:\n     * <ol>\n     * <li>Swaps it atomically with current producer node (only one producer 'wins')\n     * <li>Sets the new node as the node following from the swapped producer node\n     * </ol>\n     * This works because each producer is guaranteed to 'plant' a new node and link the old node. No 2 producers can\n     * get the same producer node as part of XCHG guarantee.\n     *\n     * @see java.util.Queue#offer(java.lang.Object)\n     */",
            "    /**\n     * {@inheritDoc} <br>\n     * <p>\n     * 实现说明：<br>\n     * 允许多线程 offer。<br>\n     * 分配新节点并：\n     * <ol>\n     * <li>与当前 producer 节点原子交换（仅一个生产者“获胜”）\n     * <li>将新节点链接到被换出的 producer 节点之后\n     * </ol>\n     * 每个生产者都会植入新节点并链接旧节点；XCHG 保证无两个生产者取得同一 producer 节点。\n     *\n     * @see java.util.Queue#offer(java.lang.Object)\n     */",
        ),
        (
            "        // Should a producer thread get interrupted here the chain WILL be broken until that thread is resumed\n        // and completes the store in prev.next.",
            "        // 若生产者线程在此被中断，链会断裂直至恢复并完成 prev.next 写入",
        ),
        (
            "        prevProducerNode.soNext(nextNode); // StoreStore",
            "        prevProducerNode.soNext(nextNode); // StoreStore 发布链接",
        ),
        (
            "    /**\n     * {@inheritDoc} <br>\n     * <p>\n     * IMPLEMENTATION NOTES:<br>\n     * Poll is allowed from a SINGLE thread.<br>\n     * Poll reads the next node from the consumerNode and:\n     * <ol>\n     * <li>If it is null, the queue is assumed empty (though it might not be).\n     * <li>If it is not null set it as the consumer node and return it's now evacuated value.\n     * </ol>\n     * This means the consumerNode.value is always null, which is also the starting point for the queue. Because null\n     * values are not allowed to be offered this is the only node with its value set to null at any one time.\n     *\n     * @see java.util.Queue#poll()\n     */",
            "    /**\n     * {@inheritDoc} <br>\n     * <p>\n     * 实现说明：<br>\n     * 仅允许单线程 poll。<br>\n     * 从 consumerNode 读取下一节点：\n     * <ol>\n     * <li>为 null 则视为空（可能仍有生产者在链接）\n     * <li>非 null 则设为 consumer 节点并返回已取出的值\n     * </ol>\n     * consumerNode.value 恒为 null（队列哨兵）；禁止 offer null，故任意时刻仅该节点 value 可为 null。\n     *\n     * @see java.util.Queue#poll()\n     */",
        ),
        (
            "        LinkedQueueNode<T> currConsumerNode = lpConsumerNode(); // don't load twice, it's alright",
            "        LinkedQueueNode<T> currConsumerNode = lpConsumerNode(); // 本地加载一次即可",
        ),
        (
            "            // we have to null out the value because we are going to hang on to the node",
            "            // 取出值后清空节点 value，因 consumer 将持有该节点",
        ),
        (
            "            // unlink previous consumer to help gc",
            "            // 断开前一 consumer 节点链接以利于 GC",
        ),
        (
            "            // spin, we are no longer wait free",
            "            // 自旋等待链接完成，此路径非 wait-free",
        ),
        (
            "            // got the next node...",
            "            // 已取得下一节点",
        ),
        (
            "    /**\n     * {@inheritDoc} <br>\n     * <p>\n     * IMPLEMENTATION NOTES:<br>\n     * Queue is empty when producerNode is the same as consumerNode. An alternative implementation would be to observe\n     * the producerNode.value is null, which also means an empty queue because only the consumerNode.value is allowed to\n     * be null.\n     */",
            "    /**\n     * {@inheritDoc} <br>\n     * <p>\n     * 实现说明：<br>\n     * producerNode 与 consumerNode 相同时队列为空。\n     * 亦可观察 producerNode.value 是否为 null（仅 consumer 哨兵允许 null 值）。\n     */",
        ),
        (
            "        /**\n         * Gets the current value and nulls out the reference to it from this node.\n         *\n         * @return value\n         */",
            "        /**\n         * 读取当前 value 并将节点内引用置 null。\n         *\n         * @return 元素值\n         */",
        ),
    ],
    "AbstractDirectTask.java": [
        (
            "/**\n * Base functionality for direct tasks that manage a runnable and cancellation/completion.\n * @since 2.0.8\n */",
            "/**\n * 直接调度任务基类：管理 Runnable、取消与完成状态。\n * @since 2.0.8\n */",
        ),
        (
            "    @Override\n    public final void dispose() {",
            "    /** CAS 将 Future 置为 DISPOSED 并按策略 cancelFuture。 */\n"
            "    @Override\n    public final void dispose() {",
        ),
        (
            "    public final void setFuture(Future<?> future) {",
            "    /** 循环 CAS 绑定 Future；若已 FINISHED/DISPOSED 则取消新 Future。 */\n"
            "    public final void setFuture(Future<?> future) {",
        ),
        (
            "    private void cancelFuture(Future<?> future) {",
            "    /** 执行线程为 runner 时用 cancel(false)，否则按 interruptOnCancel。 */\n"
            "    private void cancelFuture(Future<?> future) {",
        ),
    ],
    "CachedScheduler.java": [
        (
            "/**\n * Scheduler that creates and caches a set of thread pools and reuses them if possible.\n */",
            "/**\n * 创建并缓存线程池 Worker，空闲时复用以降低线程创建开销。\n */",
        ),
        (
            "    /** The name of the system property for setting the keep-alive time (in seconds) for this Scheduler workers. */",
            "    /** 系统属性名：CachedScheduler Worker 空闲保活时间（秒）。 */",
        ),
        (
            "    /** The name of the system property for setting the thread priority for this Scheduler. */",
            "    /** 系统属性名：CachedScheduler 线程优先级。 */",
        ),
        (
            "    /** The name of the system property for setting the release behaviour for this Scheduler. */",
            "    /** 系统属性名：Worker dispose 时是否延迟归还线程池。 */",
        ),
        (
            "    static final class CachedWorkerPool implements Runnable {",
            "    /** 线程池：get 复用或新建 ThreadWorker；evictor 定期清理过期 Worker。 */\n"
            "    static final class CachedWorkerPool implements Runnable {",
        ),
        (
            "            // No cached worker found, so create a new one.",
            "            // 无可用缓存 Worker，创建新实例",
        ),
        (
            "            // Refresh expire time before putting worker back in pool",
            "            // 归还池前刷新过期时间戳",
        ),
        (
            "                        // Queue is ordered with the worker that will expire first in the beginning, so when we\n                        // find a non-expired worker we can stop evicting.",
            "                        // 队列按过期时间排序；遇到未过期 Worker 即可停止驱逐",
        ),
        (
            "    /**\n     * Constructs an CachedScheduler with the given thread factory and starts the pool of workers.\n     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any\n     *                      system properties for configuring new thread creation. Cannot be null.\n     */",
            "    /**\n     * 使用给定 ThreadFactory 构造并启动 Worker 池。\n"
            "     * @param threadFactory 创建 Worker 线程的工厂；优先于相关系统属性；不可为 null\n"
            "     */",
        ),
        (
            "    static final class EventLoopWorker extends Scheduler.Worker implements Runnable {",
            "    /** 绑定单个 ThreadWorker；dispose 后 release 回池（或 scheduleActual 延迟 release）。 */\n"
            "    static final class EventLoopWorker extends Scheduler.Worker implements Runnable {",
        ),
        (
            "                    // releasing the pool should be the last action",
            "                    // 直接归还 Worker 至池（无延迟 release）",
        ),
        (
            "                // don't schedule, we are unsubscribed",
            "                // tasks 已 dispose，不再调度",
        ),
        (
            "    static final class ThreadWorker extends NewThreadWorker {",
            "    /** 带 expirationTime 的池化 Worker，供 evictor 判定是否过期。 */\n"
            "    static final class ThreadWorker extends NewThreadWorker {",
        ),
    ],
}
