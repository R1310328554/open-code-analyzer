"""Chinese JavaDoc replacements for RxJava 4.0.0-alpha-21 wave28b subscribers/subjects [15:30]."""

SUBSCRIBERS_W28B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "src/main/java/io/reactivex/rxjava4/subjects/package-info.java": [
        (
            "/**\n * Classes representing so-called hot sources, aka <strong>subjects</strong>, that implement a base reactive class and\n * the respective consumer type at once to allow forms of multicasting events to multiple\n * consumers as well as consuming another base reactive type of their kind.\n * <p>\n * Available subject classes with their respective base classes and consumer interfaces:\n * <br>\n * <table border=\"1\" style=\"border-collapse: collapse;\">\n * <caption>The available subject classes with their respective base classes and consumer interfaces.</caption>\n * <tr><td><b>Subject type</b></td><td><b>Base class</b></td><td><b>Consumer interface</b></td></tr>",
            "/**\n"
            " * 表示热源（<strong>Subject</strong>）的类型：同时实现基础响应式类与对应 consumer 接口，\n"
            " * 可向多个 consumer 多播事件，也可作为 consumer 订阅同类源流。\n"
            " * <p>\n"
            " * 各 Subject 与基础类、consumer 接口对应关系：\n"
            " * <br>\n"
            " * <table border=\"1\" style=\"border-collapse: collapse;\">\n"
            " * <caption>可用 Subject 类及其基础类与 consumer 接口。</caption>\n"
            " * <tr><td><b>Subject 类型</b></td><td><b>基础类</b></td><td><b>Consumer 接口</b></td></tr>",
        ),
        (
            " * The backpressure-aware variants of the {@code Subject} class are called\n * {@link java.util.concurrent.Flow.Processor}s and reside in the {@code io.reactivex.processors} package.\n * @see io.reactivex.rxjava4.processors\n */",
            " * 支持背压的 {@code Subject} 变体为 {@link java.util.concurrent.Flow.Processor}，\n"
            " * 位于 {@code io.reactivex.processors} 包。\n"
            " * @see io.reactivex.rxjava4.processors\n"
            " */",
        ),
    ],
    "DefaultSubscriber.java": [
        (
            "/**\n * Abstract base implementation of a {@link java.util.concurrent.Flow.Subscriber Subscriber} with\n * support for requesting via {@code #request(long)}, cancelling via\n * {@code #cancel()} (both synchronously) and calls {@code #onStart()}\n * when the subscription happens.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>The default {@code #onStart()} requests {@link Long#MAX_VALUE} by default. Override\n * the method to request a custom <em>positive</em> amount.\n *\n * <p>Note that calling {@code #request(long)} from {@code #onStart()} may trigger\n * an immediate, asynchronous emission of data to {@link #onNext(Object)}. Make sure\n * all initialization happens before the call to {@code request()} in {@code onStart()}.\n * Calling {@code #request(long)} inside {@link #onNext(Object)} can happen at any time\n * because by design, {@code onNext} calls from upstream are non-reentrant and non-overlapping.\n *\n * <p>Use the protected {@code #cancel()} to cancel the sequence from within an\n * {@code onNext} implementation.\n *\n * <p>Like all other consumers, {@code DefaultSubscriber} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onNext(Object)}, {@link #onError(Throwable)}\n * and {@link #onComplete()} are not allowed to throw any unchecked exceptions.\n * If for some reason this can't be avoided, use {@link io.reactivex.rxjava4.core.Flowable#safeSubscribe(java.util.concurrent.Flow.Subscriber)}\n * instead of the standard {@code subscribe()} method.\n * @param <T> the value type",
            "/**\n"
            " * {@link java.util.concurrent.Flow.Subscriber} 抽象基类：支持同步 {@link #request(long)} 与\n"
            " * {@link #cancel()}，订阅建立时调用 {@link #onStart()}。\n"
            " *\n"
            " * <p>预置 final 方法线程安全；默认 onStart 请求 {@link Long#MAX_VALUE}。\n"
            " *\n"
            " * <p>onStart 内 request 可能异步触发 onNext，须先完成初始化。\n"
            " * onNext 内 request 安全（上游 onNext 非重入）。\n"
            " *\n"
            " * <p>仅允许单次订阅；回调不应抛出未检查异常，否则用 safeSubscribe。\n"
            " *\n"
            " * @param <T> 元素类型",
        ),
        (
            "    @Override\n    public final void onSubscribe(Subscription s) {",
            "    /** validate 通过后保存 upstream 并调用 onStart()。 */\n"
            "    @Override\n    public final void onSubscribe(Subscription s) {",
        ),
        (
            "    /**\n     * Requests from the upstream {@link Subscription}.\n     * @param n the request amount, positive\n     */",
            "    /**\n"
            "     * 向上游 {@link Subscription} 请求元素。\n"
            "     * @param n 请求数量，须为正\n"
            "     */",
        ),
        (
            "    /**\n     * Cancels the upstream's {@link Subscription}.\n     */",
            "    /** 取消上游 {@link Subscription} 并将 upstream 置 CANCELLED。 */",
        ),
        (
            "    /**\n     * Called once the subscription has been set on this observer; override this\n     * to perform initialization or issue an initial request.\n     * <p>\n     * The default implementation requests {@link Long#MAX_VALUE}.\n     */",
            "    /**\n"
            "     * 订阅建立后回调，可覆写做初始化或发出初始 request。\n"
            "     * <p>默认请求 {@link Long#MAX_VALUE}。\n"
            "     */",
        ),
    ],
    "DisposableSubscriber.java": [
        (
            "/**\n * An abstract Subscriber that allows asynchronous, external cancellation by implementing {@link Disposable}.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>The default {@code #onStart()} requests {@link Long#MAX_VALUE} by default. Override\n * the method to request a custom <em>positive</em> amount. Use the protected {@code #request(long)}\n * to request more items and {@code #cancel()} to cancel the sequence from within an\n * {@code onNext} implementation.\n *\n * <p>Note that calling {@code #request(long)} from {@code #onStart()} may trigger\n * an immediate, asynchronous emission of data to {@link #onNext(Object)}. Make sure\n * all initialization happens before the call to {@code request()} in {@code onStart()}.\n * Calling {@code #request(long)} inside {@link #onNext(Object)} can happen at any time\n * because by design, {@code onNext} calls from upstream are non-reentrant and non-overlapping.\n *\n * <p>Like all other consumers, {@code DisposableSubscriber} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onNext(Object)}, {@link #onError(Throwable)}\n * and {@link #onComplete()} are not allowed to throw any unchecked exceptions.\n * If for some reason this can't be avoided, use {@link io.reactivex.rxjava4.core.Flowable#safeSubscribe(java.util.concurrent.Flow.Subscriber)}\n * instead of the standard {@code subscribe()} method.",
            "/**\n"
            " * 实现 {@link Disposable} 的 Subscriber 抽象基类，支持异步外部取消。\n"
            " *\n"
            " * <p>final 方法线程安全；默认 onStart 无界 request。\n"
            " * 在 onNext 中可用 {@link #request(long)} 与 {@link #cancel()}。\n"
            " *\n"
            " * <p>仅允许单次订阅；回调不应抛出未检查异常。\n",
        ),
        (
            " * @param <T> the received value type.\n */",
            " *\n * @param <T> 接收的元素类型\n */",
        ),
        (
            "    @Override\n    public final void onSubscribe(Subscription s) {",
            "    /** setOnce 成功后调用 onStart()。 */\n"
            "    @Override\n    public final void onSubscribe(Subscription s) {",
        ),
        (
            "    /**\n     * Called once the single upstream {@link Subscription} is set via {@link #onSubscribe(Subscription)}.\n     */",
            "    /** 上游 {@link Subscription} 通过 onSubscribe 设置成功后调用。 */",
        ),
        (
            "    /**\n     * Requests the specified amount from the upstream if its {@link Subscription} is set via\n     * onSubscribe already.\n     * <p>Note that calling this method before a {@link Subscription} is set via {@link #onSubscribe(Subscription)}\n     * leads to {@link NullPointerException} and meant to be called from inside {@link #onStart()} or\n     * {@link #onNext(Object)}.\n     * @param n the request amount, positive\n     */",
            "    /**\n"
            "     * 向上游请求指定数量（须已 onSubscribe）。\n"
            "     * <p>onSubscribe 前调用会 NPE；应在 onStart 或 onNext 内调用。\n"
            "     * @param n 请求数量，须为正\n"
            "     */",
        ),
        (
            "    /**\n     * Cancels the Subscription set via {@link #onSubscribe(Subscription)} or makes sure a\n     * {@link Subscription} set asynchronously (later) is cancelled immediately.\n     * <p>This method is thread-safe and can be exposed as a public API.\n     */",
            "    /** 取消订阅；线程安全，等价于 dispose()。 */",
        ),
        (
            "    @Override\n    public final boolean isDisposed() {",
            "    /** upstream 是否为 CANCELLED。 */\n"
            "    @Override\n    public final boolean isDisposed() {",
        ),
        (
            "    @Override\n    public final void dispose() {",
            "    /** SubscriptionHelper.cancel(upstream)。 */\n"
            "    @Override\n    public final void dispose() {",
        ),
    ],
    "ResourceSubscriber.java": [
        (
            "/**\n * An abstract Subscriber that allows asynchronous cancellation of its\n * subscription and associated resources.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>To release the associated resources, one has to call {@link #dispose()}\n * in {@code onError()} and {@code onComplete()} explicitly.\n *\n * <p>Use {@link #add(Disposable)} to associate resources (as {@link io.reactivex.rxjava4.disposables.Disposable Disposable}s)\n * with this {@code ResourceSubscriber} that will be cleaned up when {@link #dispose()} is called.\n * Removing previously associated resources is not possible but one can create a\n * {@link io.reactivex.rxjava4.disposables.CompositeDisposable CompositeDisposable}, associate it with this\n * {@code ResourceSubscriber} and then add/remove resources to/from the {@code CompositeDisposable}\n * freely.\n *\n * <p>The default {@code #onStart()} requests {@link Long#MAX_VALUE} by default. Override\n * the method to request a custom <em>positive</em> amount. Use the protected {@code #request(long)}\n * to request more items and {@link #dispose()} to cancel the sequence from within an\n * {@code onNext} implementation.\n *\n * <p>Note that calling {@code #request(long)} from {@code #onStart()} may trigger\n * an immediate, asynchronous emission of data to {@link #onNext(Object)}. Make sure\n * all initialization happens before the call to {@code request()} in {@code onStart()}.\n * Calling {@code #request(long)} inside {@link #onNext(Object)} can happen at any time\n * because by design, {@code onNext} calls from upstream are non-reentrant and non-overlapping.\n *\n * <p>Like all other consumers, {@code ResourceSubscriber} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onNext(Object)}, {@link #onError(Throwable)}\n * and {@link #onComplete()} are not allowed to throw any unchecked exceptions.\n * If for some reason this can't be avoided, use {@link io.reactivex.rxjava4.core.Flowable#safeSubscribe(java.util.concurrent.Flow.Subscriber)}\n * instead of the standard {@code subscribe()} method.",
            "/**\n"
            " * 支持异步取消订阅并管理关联资源的 Subscriber 抽象基类。\n"
            " *\n"
            " * <p>在 onError/onComplete 中应显式 {@link #dispose()}；\n"
            " * 通过 {@link #add(Disposable)} 关联资源，dispose 时一并清理。\n"
            " *\n"
            " * <p>默认 onStart 无界 request；request 支持 onSubscribe 前 deferred 累积。\n"
            " *\n"
            " * <p>仅允许单次订阅；回调不应抛出未检查异常。\n",
        ),
        (
            " * @param <T> the value type\n */",
            " *\n * @param <T> 元素类型\n */",
        ),
        (
            "    /** The active subscription. */",
            "    /** 当前活跃的上游订阅。 */",
        ),
        (
            "    /** The resource composite, can never be null. */",
            "    /** 资源复合容器，永不为 null。 */",
        ),
        (
            "    /** Remembers the request(n) counts until a subscription arrives. */",
            "    /** 在 Subscription 到达前缓存 request(n) 计数。 */",
        ),
        (
            "    /**\n     * Adds a resource to this {@code ResourceSubscriber}.\n     *\n     * @param resource the resource to add\n     *\n     * @throws NullPointerException if {@code resource} is {@code null}\n     */",
            "    /**\n"
            "     * 向本 {@code ResourceSubscriber} 添加资源。\n"
            "     * @param resource 要添加的资源\n"
            "     * @throws NullPointerException 若 resource 为 null\n"
            "     */",
        ),
        (
            "    @Override\n    public final void onSubscribe(Subscription s) {",
            "    /** setOnce 后 flush missedRequested 并 onStart()。 */\n"
            "    @Override\n    public final void onSubscribe(Subscription s) {",
        ),
        (
            "    /**\n     * Called once the upstream sets a {@link Subscription} on this {@code ResourceSubscriber}.\n     *\n     * <p>You can perform initialization at this moment. The default\n     * implementation requests {@link Long#MAX_VALUE} from upstream.\n     */",
            "    /**\n"
            "     * 上游设置 Subscription 后调用；默认 request(Long.MAX_VALUE)。\n"
            "     */",
        ),
        (
            "    /**\n     * Request the specified amount of elements from upstream.\n     *\n     * <p>This method can be called before the upstream calls {@link #onSubscribe(Subscription)}.\n     * When the subscription happens, all missed requests are requested.\n     *\n     * @param n the request amount, must be positive\n     */",
            "    /**\n"
            "     * 向上游请求元素；可在 onSubscribe 前调用，订阅时一并发出。\n"
            "     * @param n 请求数量，须为正\n"
            "     */",
        ),
        (
            "    /**\n     * Cancels the subscription (if any) and disposes the resources associated with\n     * this {@code ResourceSubscriber} (if any).\n     *\n     * <p>This method can be called before the upstream calls {@link #onSubscribe(Subscription)} at which\n     * case the {@link Subscription} will be immediately cancelled.\n     */",
            "    /** 取消订阅并 dispose 关联资源；可在 onSubscribe 前调用。 */",
        ),
        (
            "    /**\n     * Returns true if this {@code ResourceSubscriber} has been disposed/cancelled.\n     * @return true if this {@code ResourceSubscriber} has been disposed/cancelled\n     */",
            "    /**\n"
            "     * 是否已 dispose/取消。\n"
            "     * @return 已 dispose/取消则为 true\n"
            "     */",
        ),
    ],
    "SafeSubscriber.java": [
        (
            "/**\n * Wraps another {@link Subscriber} and ensures all {@code onXXX} methods conform the protocol\n * (except the requirement for serialized access).\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 包装另一个 {@link Subscriber}，确保 onXXX 符合协议（序列化要求除外）。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    /** The actual Subscriber. */",
            "    /** 实际下游 Subscriber。 */",
        ),
        (
            "    /** The subscription. */",
            "    /** 上游 Subscription。 */",
        ),
        (
            "    /** Indicates a terminal state. */",
            "    /** 是否已进入终止状态。 */",
        ),
        (
            "    /**\n     * Constructs a {@code SafeSubscriber} by wrapping the given actual {@link Subscriber}.\n     * @param downstream the actual {@code Subscriber} to wrap, not {@code null} (not validated)\n     */",
            "    /**\n"
            "     * 包装给定 {@link Subscriber} 构造 SafeSubscriber。\n"
            "     * @param downstream 实际 Subscriber（未校验非 null）\n"
            "     */",
        ),
        (
            "    @Override\n    public void onSubscribe(@NonNull Subscription s) {",
            "    /** 校验 upstream 后以自身转发 onSubscribe；异常时 cancel 并上报。 */\n"
            "    @Override\n    public void onSubscribe(@NonNull Subscription s) {",
        ),
        (
            "    @Override\n    public void onNext(@NonNull T t) {",
            "    /** 校验 null 与订阅状态后安全转发 onNext。 */\n"
            "    @Override\n    public void onNext(@NonNull T t) {",
        ),
        (
            "    void onNextNoSubscription() {",
            "    /** 未设置 Subscription 时以 EmptySubscription 订阅并向下游 onError。 */\n"
            "    void onNextNoSubscription() {",
        ),
        (
            "    @Override\n    public void onError(@NonNull Throwable t) {",
            "    /** 安全转发 onError；已终止或未订阅时上报 RxJavaPlugins。 */\n"
            "    @Override\n    public void onError(@NonNull Throwable t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 安全转发 onComplete；未订阅时走 onCompleteNoSubscription。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
        (
            "    void onCompleteNoSubscription() {",
            "    /** 未设置 Subscription 时以 EmptySubscription 订阅并向下游 onError。 */\n"
            "    void onCompleteNoSubscription() {",
        ),
        (
            "    @Override\n    public void request(long n) {",
            "    /** 安全转发 request；异常时 cancel 并上报。 */\n"
            "    @Override\n    public void request(long n) {",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** 安全转发 cancel；异常上报 RxJavaPlugins。 */\n"
            "    @Override\n    public void cancel() {",
        ),
    ],
    "SerializedSubscriber.java": [
        (
            "/**\n * Serializes access to the {@link Subscriber#onNext(Object)}, {@link Subscriber#onError(Throwable)} and\n * {@link Subscriber#onComplete()} methods of another {@link Subscriber}.\n *\n * <p>Note that {@link #onSubscribe(Subscription)} is not serialized in respect of the other methods so\n * make sure the {@code onSubscribe} is called with a non-{@code null} {@link Subscription}\n * before any of the other methods are called.\n *\n * <p>The implementation assumes that the actual {@code Subscriber}'s methods don't throw.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 对另一个 {@link Subscriber} 的 onNext/onError/onComplete 进行串行化访问。\n"
            " *\n"
            " * <p>onSubscribe 相对其他方法未串行化，须先以非 null Subscription 完成 onSubscribe。\n"
            " *\n"
            " * <p>假定实际 Subscriber 的方法不会抛出异常。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    /**\n     * Construct a {@code SerializedSubscriber} by wrapping the given actual {@link Subscriber}.\n     * @param downstream the actual {@code Subscriber}, not null (not verified)\n     */",
            "    /**\n"
            "     * 包装给定 {@link Subscriber} 构造 SerializedSubscriber。\n"
            "     * @param downstream 实际 Subscriber（未校验非 null）\n"
            "     */",
        ),
        (
            "    /**\n     * Construct a {@code SerializedSubscriber} by wrapping the given actual {@link Subscriber} and\n     * optionally delaying the errors till all regular values have been emitted\n     * from the internal buffer.\n     * @param actual the actual {@code Subscriber}, not {@code null} (not verified)\n     * @param delayError if {@code true}, errors are emitted after regular values have been emitted\n     */",
            "    /**\n"
            "     * 包装 Subscriber 并可选择将错误延迟到缓冲中常规值全部发出后。\n"
            "     * @param actual 实际 Subscriber\n"
            "     * @param delayError 为 true 时错误在常规值之后发出\n"
            "     */",
        ),
        (
            "    @Override\n    public void onSubscribe(@NonNull Subscription s) {",
            "    /** 校验 upstream 后以自身转发 onSubscribe。 */\n"
            "    @Override\n    public void onSubscribe(@NonNull Subscription s) {",
        ),
        (
            "    @Override\n    public void onNext(@NonNull T t) {",
            "    /** 串行化转发 onNext；emitting 时入队。 */\n"
            "    @Override\n    public void onNext(@NonNull T t) {",
        ),
        (
            "    @Override\n    public void onError(Throwable t) {",
            "    /** 串行化转发 onError；delayError 模式下错误入队。 */\n"
            "    @Override\n    public void onError(Throwable t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 串行化转发 onComplete；emitting 时入队 complete 标记。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
        (
            "    void emitLoop() {",
            "    /** 排空内部队列并继续向下游发出事件。 */\n"
            "    void emitLoop() {",
        ),
        (
            "    @Override\n    public void request(long n) {",
            "    /** 委托 upstream.request。 */\n"
            "    @Override\n    public void request(long n) {",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** 委托 upstream.cancel。 */\n"
            "    @Override\n    public void cancel() {",
        ),
    ],
    "TestSubscriber.java": [
        (
            "/**\n * A {@link Subscriber} implementation that records events and allows making assertions about them.\n *\n * <p>You can override the {@link #onSubscribe(Subscription)}, {@link #onNext(Object)}, {@link #onError(Throwable)} and\n * {@link #onComplete()} methods but not the others (this is by design).\n *\n * <p>When calling the default request method, you are requesting on behalf of the\n * wrapped actual {@link Subscriber} if any.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 记录事件的 {@link Subscriber} 实现，支持断言。\n"
            " *\n"
            " * <p>可覆盖 onSubscribe/onNext/onError/onComplete，其余方法不可覆盖。\n"
            " *\n"
            " * <p>request 代表被包装的实际 Subscriber 向上游请求。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    /** The actual subscriber to forward events to. */",
            "    /** 转发事件的实际 Subscriber。 */",
        ),
        (
            "    /** Makes sure the incoming Subscriptions get cancelled immediately. */",
            "    /** 标记是否已 cancel。 */",
        ),
        (
            "    /** Holds the current subscription if any. */",
            "    /** 当前 Subscription（若有）。 */",
        ),
        (
            "    /** Holds the requested amount until a subscription arrives. */",
            "    /** 在 Subscription 到达前缓存 request 数量。 */",
        ),
        (
            "    /**\n     * Creates a {@code TestSubscriber} with {@link Long#MAX_VALUE} initial request amount.\n     * @param <T> the value type\n     * @return the new {@code TestSubscriber} instance.\n     * @see #create(long)\n     */",
            "    /**\n"
            "     * 创建初始 request 为 {@link Long#MAX_VALUE} 的 TestSubscriber。\n"
            "     * @param <T> 元素类型\n"
            "     * @return 新实例\n"
            "     * @see #create(long)\n"
            "     */",
        ),
        (
            "    /**\n     * Creates a {@code TestSubscriber} with the given initial request amount.\n     * @param <T> the value type\n     * @param initialRequested the initial requested amount\n     * @return the new {@code TestSubscriber} instance.\n     */",
            "    /**\n"
            "     * 以给定初始 request 创建 TestSubscriber。\n"
            "     * @param initialRequested 初始请求量\n"
            "     * @return 新实例\n"
            "     */",
        ),
        (
            "    /**\n     * Constructs a forwarding {@code TestSubscriber}.\n     * @param <T> the value type received\n     * @param delegate the actual {@link Subscriber} to forward events to\n     * @return the new TestObserver instance\n     */",
            "    /**\n"
            "     * 创建转发事件的 TestSubscriber。\n"
            "     * @param delegate 转发目标 Subscriber\n"
            "     * @return 新实例\n"
            "     */",
        ),
        (
            "    /**\n     * Constructs a non-forwarding {@code TestSubscriber} with an initial request value of {@link Long#MAX_VALUE}.\n     */",
            "    /** 构造不转发、初始 request 为 MAX_VALUE 的 TestSubscriber。 */",
        ),
        (
            "    /**\n     * Constructs a non-forwarding {@code TestSubscriber} with the specified initial request value.\n     * <p>The {@code TestSubscriber} doesn't validate the {@code initialRequest} amount so one can\n     * test sources with invalid values as well.\n     * @param initialRequest the initial request amount\n     */",
            "    /**\n"
            "     * 构造不转发、指定初始 request 的 TestSubscriber（不校验 initialRequest）。\n"
            "     * @param initialRequest 初始请求量\n"
            "     */",
        ),
        (
            "    @Override\n    public void onSubscribe(@NonNull Subscription s) {",
            "    /** 记录线程与 upstream，flush missedRequested 后 onStart。 */\n"
            "    @Override\n    public void onSubscribe(@NonNull Subscription s) {",
        ),
        (
            "    /**\n     * Called after the onSubscribe is called and handled.\n     */",
            "    /** onSubscribe 处理完成后的钩子方法。 */",
        ),
        (
            "    @Override\n    public void onNext(@NonNull T t) {",
            "    /** 记录值与线程，校验订阅顺序后转发 downstream。 */\n"
            "    @Override\n    public void onNext(@NonNull T t) {",
        ),
        (
            "    @Override\n    public void onError(@NonNull Throwable t) {",
            "    /** 记录错误，转发 downstream 并 countDown done。 */\n"
            "    @Override\n    public void onError(@NonNull Throwable t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 递增 completions，转发 downstream 并 countDown done。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
        (
            "    @Override\n    public final void request(long n) {",
            "    /** deferredRequest 向上游请求。 */\n"
            "    @Override\n    public final void request(long n) {",
        ),
        (
            "    @Override\n    public final void cancel() {",
            "    /** 取消 upstream 并置 cancelled。 */\n"
            "    @Override\n    public final void cancel() {",
        ),
        (
            "    /**\n     * Returns true if this {@code TestSubscriber} has been cancelled.\n     * @return true if this {@code TestSubscriber} has been cancelled\n     */",
            "    /**\n"
            "     * 是否已 cancel。\n"
            "     * @return 已 cancel 则为 true\n"
            "     */",
        ),
        (
            "    /**\n     * Returns true if this {@code TestSubscriber} received a {@link Subscription} via {@link #onSubscribe(Subscription)}.\n     * @return true if this {@code TestSubscriber} received a {@link Subscription} via {@link #onSubscribe(Subscription)}\n     */",
            "    /**\n"
            "     * 是否已通过 onSubscribe 收到 Subscription。\n"
            "     * @return 已收到则为 true\n"
            "     */",
        ),
        (
            "    /**\n     * Assert that the {@link #onSubscribe(Subscription)} method was called exactly once.\n     * @return this\n     */",
            "    /**\n"
            "     * 断言 onSubscribe 恰好调用一次。\n"
            "     * @return this\n"
            "     */",
        ),
        (
            "    /**\n     * Calls {@link #request(long)} and returns this.\n     * <p>History: 2.0.1 - experimental\n     * @param n the request amount\n     * @return this\n     * @since 2.1\n     */",
            "    /**\n"
            "     * 调用 request(n) 并返回 this。\n"
            "     * @param n 请求数量\n"
            "     * @return this\n"
            "     */",
        ),
        (
            "    /**\n     * Expose this {@code TestSubscriber} as a {@link Disposable} object.\n     * @return the {@code Disposable} view of this {@code TestSubscriber}\n     * @since 4.0.0\n     */",
            "    /**\n"
            "     * 将本 TestSubscriber 暴露为 {@link Disposable} 视图。\n"
            "     * @return Disposable 包装\n"
            "     * @since 4.0.0\n"
            "     */",
        ),
        (
            "    /**\n     * A subscriber that ignores all events and does not report errors.\n     */",
            "    /** 忽略所有事件且不上报错误的 Subscriber。 */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/subscribers/package-info.java": [
        (
            "/**\n * Default wrappers and implementations for {@link java.util.concurrent.Flow.Subscriber Subscriber}-based consumer classes and interfaces,\n * including disposable ({@link io.reactivex.rxjava4.subscribers.DisposableSubscriber DisposableSubscriber}) and resource-tracking\n * ({@link io.reactivex.rxjava4.subscribers.ResourceSubscriber ResourceSubscriber})\n * variants and the {@link io.reactivex.rxjava4.subscribers.TestSubscriber TestSubscriber} that allows unit testing\n * {@link io.reactivex.rxjava4.core.Flowable Flowable}-based flows.\n */",
            "/**\n"
            " * 基于 {@link java.util.concurrent.Flow.Subscriber} 的 consumer 默认包装与实现，\n"
            " * 含 {@link io.reactivex.rxjava4.subscribers.DisposableSubscriber DisposableSubscriber}、\n"
            " * {@link io.reactivex.rxjava4.subscribers.ResourceSubscriber ResourceSubscriber} 与\n"
            " * 用于单元测试 {@link io.reactivex.rxjava4.core.Flowable Flowable} 流的\n"
            " * {@link io.reactivex.rxjava4.subscribers.TestSubscriber TestSubscriber}。\n"
            " */",
        ),
    ],
}
