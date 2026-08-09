"""Replacement tuples for RxJava wave-27a observers and operators [0:15]."""

OBSERVERS_OPERATORS_W27A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DisposableSingleObserver.java": [
        (
            "/**\n * An abstract {@link SingleObserver} that allows asynchronous cancellation by implementing {@link Disposable}.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>Like all other consumers, {@code DisposableSingleObserver} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onSuccess(Object)} and {@link #onError(Throwable)}\n * are not allowed to throw any unchecked exceptions.\n *\n * <p>Example<pre><code>\n * Disposable d =\n *     Single.just(1).delay(1, TimeUnit.SECONDS)\n *     .subscribeWith(new DisposableSingleObserver&lt;Integer&gt;() {\n *         &#64;Override public void onStart() {\n *             System.out.println(\"Start!\");\n *         }\n *         &#64;Override public void onSuccess(Integer t) {\n *             System.out.println(t);\n *         }\n *         &#64;Override public void onError(Throwable t) {\n *             t.printStackTrace();\n *         }\n *     });\n * // ...\n * d.dispose();\n * </code></pre>\n *\n * @param <T> the received value type\n */",
            "/**\n"
            " * 实现 {@link Disposable} 的 {@link SingleObserver} 抽象基类，支持异步取消订阅。\n"
            " *\n"
            " * <p>所有预实现的 final 方法均为线程安全。\n"
            " *\n"
            " * <p>与其他 consumer 一样，{@code DisposableSingleObserver} 仅允许订阅一次；\n"
            " * 再次订阅将抛出 {@link IllegalStateException}。\n"
            " *\n"
            " * <p>{@code #onStart()}、{@link #onSuccess(Object)} 与 {@link #onError(Throwable)}\n"
            " * 的实现不得抛出未检查异常。\n"
            " *\n"
            " * @param <T> 接收值类型\n"
            " */",
        ),
        (
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
            "    /** setOnce 成功后调用 {@link #onStart()}。 */\n"
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    /**\n     * Called once the single upstream {@link Disposable} is set via {@link #onSubscribe(Disposable)}.\n     */",
            "    /** 上游 {@link Disposable} 通过 {@link #onSubscribe(Disposable)} 设置成功后调用。 */",
        ),
        (
            "    @Override\n    public final boolean isDisposed() {",
            "    /** 判断 upstream 是否已为 DISPOSED。 */\n"
            "    @Override\n    public final boolean isDisposed() {",
        ),
        (
            "    @Override\n    public final void dispose() {",
            "    /** 通过 {@link DisposableHelper#dispose} 取消上游订阅。 */\n"
            "    @Override\n    public final void dispose() {",
        ),
    ],
    "LambdaConsumerIntrospection.java": [
        (
            "/**\n * An interface that indicates that the implementing type is composed of individual components and exposes information\n * about their behavior.\n *\n * <p><em>NOTE:</em> This is considered a read-only public API and is not intended to be implemented externally.\n * <p>History: 2.1.4 - experimental\n * @since 2.2\n */",
            "/**\n"
            " * 表明实现类型由多个组件构成，并暴露其行为的内省接口。\n"
            " *\n"
            " * <p><em>NOTE:</em> 此为只读公共 API，不建议外部实现。\n"
            " * <p>History: 2.1.4 - experimental\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    /**\n     * Returns {@code true} or {@code false} if a custom {@code onError} consumer has been provided.\n     * @return {@code true} if a custom {@code onError} consumer implementation was supplied. Returns {@code false} if the\n     * implementation is missing an error consumer and thus using a throwing default implementation.\n     */",
            "    /**\n"
            "     * 判断是否提供了自定义 {@code onError} consumer。\n"
            "     * @return 若提供了自定义 {@code onError} 实现则为 {@code true}；\n"
            "     *         若缺少错误 consumer 而使用会抛异常的默认实现则为 {@code false}\n"
            "     */",
        ),
    ],
    "ResourceCompletableObserver.java": [
        (
            "/**\n * An abstract {@link CompletableObserver} that allows asynchronous cancellation of its subscription and associated resources.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>Override the protected {@code #onStart()} to perform initialization when this\n * {@code ResourceCompletableObserver} is subscribed to a source.\n *\n * <p>Use the public {@link #dispose()} method to dispose the sequence externally and release\n * all resources.\n *\n * <p>To release the associated resources, one has to call {@link #dispose()}\n * in {@code onError()} and {@code onComplete()} explicitly.\n *\n * <p>Use {@link #add(Disposable)} to associate resources (as {@link io.reactivex.rxjava4.disposables.Disposable Disposable}s)\n * with this {@code ResourceCompletableObserver} that will be cleaned up when {@link #dispose()} is called.\n * Removing previously associated resources is not possible but one can create a\n * {@link io.reactivex.rxjava4.disposables.CompositeDisposable CompositeDisposable}, associate it with this\n * {@code ResourceCompletableObserver} and then add/remove resources to/from the {@code CompositeDisposable}\n * freely.\n *\n * <p>Like all other consumers, {@code ResourceCompletableObserver} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onError(Throwable)}\n * and {@link #onComplete()} are not allowed to throw any unchecked exceptions.\n *\n * <p>Example<pre><code>\n * Disposable d =\n *     Completable.complete().delay(1, TimeUnit.SECONDS)\n *     .subscribeWith(new ResourceCompletableObserver() {\n *         &#64;Override public void onStart() {\n *             add(Schedulers.single()\n *                 .scheduleDirect(() -&gt; System.out.println(\"Time!\"),\n *                     2, TimeUnit.SECONDS));\n *         }\n *         &#64;Override public void onError(Throwable t) {\n *             t.printStackTrace();\n *             dispose();\n *         }\n *         &#64;Override public void onComplete() {\n *             System.out.println(\"Done!\");\n *             dispose();\n *         }\n *     });\n * // ...\n * d.dispose();\n * </code></pre>\n */",
            "/**\n"
            " * 支持异步取消订阅并管理关联资源的 {@link CompletableObserver} 抽象基类。\n"
            " *\n"
            " * <p>所有预实现的 final 方法均为线程安全。\n"
            " *\n"
            " * <p>订阅时可覆盖 {@code #onStart()} 做初始化；\n"
            " * 在 {@code onError()} 与 {@code onComplete()} 中应显式调用 {@link #dispose()} 释放资源。\n"
            " *\n"
            " * <p>通过 {@link #add(Disposable)} 关联 {@link io.reactivex.rxjava4.disposables.Disposable Disposable}，\n"
            " * 在 {@link #dispose()} 时一并清理；已关联资源不可单独移除，\n"
            " * 可改用 {@link io.reactivex.rxjava4.disposables.CompositeDisposable CompositeDisposable} 灵活增删。\n"
            " *\n"
            " * <p>仅允许单次订阅；回调不得抛出未检查异常。\n"
            " */",
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
            "    /**\n     * Adds a resource to this {@code ResourceCompletableObserver}.\n     *\n     * @param resource the resource to add\n     *\n     * @throws NullPointerException if resource is {@code null}\n     */",
            "    /**\n"
            "     * 向本 {@code ResourceCompletableObserver} 添加资源。\n"
            "     *\n     * @param resource 要添加的资源\n"
            "     *\n     * @throws NullPointerException 若 resource 为 {@code null}\n"
            "     */",
        ),
        (
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
            "    /** setOnce 成功后调用 {@link #onStart()}。 */\n"
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    /**\n     * Called once the upstream sets a {@link Disposable} on this {@code ResourceCompletableObserver}.\n     *\n     * <p>You can perform initialization at this moment. The default\n     * implementation does nothing.\n     */",
            "    /**\n"
            "     * 上游在本 observer 上设置 {@link Disposable} 后调用。\n"
            "     * <p>可在此做初始化；默认实现为空。\n"
            "     */",
        ),
        (
            "    /**\n     * Cancels the main disposable (if any) and disposes the resources associated with\n     * this {@code ResourceCompletableObserver} (if any).\n     *\n     * <p>This method can be called before the upstream calls {@link #onSubscribe(Disposable)} at which\n     * case the main {@link Disposable} will be immediately disposed.\n     */",
            "    /**\n"
            "     * 取消主 disposable（若有）并 dispose 本 observer 关联的所有资源。\n"
            "     * <p>可在上游 {@link #onSubscribe(Disposable)} 之前调用，此时主 {@link Disposable} 会立即被 dispose。\n"
            "     */",
        ),
        (
            "    /**\n     * Returns true if this {@code ResourceCompletableObserver} has been disposed/cancelled.\n     * @return true if this {@code ResourceCompletableObserver} has been disposed/cancelled\n     */",
            "    /**\n"
            "     * 判断本 {@code ResourceCompletableObserver} 是否已 dispose/取消。\n"
            "     * @return 若已 dispose/取消则为 true\n"
            "     */",
        ),
    ],
    "ResourceMaybeObserver.java": [
        (
            "/**\n * An abstract {@link MaybeObserver} that allows asynchronous cancellation of its subscription and associated resources.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>Note that {@link #onSuccess(Object)}, {@link #onError(Throwable)} and {@link #onComplete()} are\n * exclusive to each other, unlike a regular {@link io.reactivex.rxjava4.core.Observer Observer}, and\n * {@code onComplete()} is never called after an {@code onSuccess()}.\n *\n * <p>Override the protected {@code #onStart()} to perform initialization when this\n * {@code ResourceMaybeObserver} is subscribed to a source.\n *\n * <p>Use the public {@link #dispose()} method to dispose the sequence externally and release\n * all resources.\n *\n * <p>To release the associated resources, one has to call {@link #dispose()}\n * in {@code onSuccess()}, {@code onError()} and {@code onComplete()} explicitly.\n *\n * <p>Use {@link #add(Disposable)} to associate resources (as {@link io.reactivex.rxjava4.disposables.Disposable Disposable}s)\n * with this {@code ResourceMaybeObserver} that will be cleaned up when {@link #dispose()} is called.\n * Removing previously associated resources is not possible but one can create a\n * {@link io.reactivex.rxjava4.disposables.CompositeDisposable CompositeDisposable}, associate it with this\n * {@code ResourceMaybeObserver} and then add/remove resources to/from the {@code CompositeDisposable}\n * freely.\n *\n * <p>Like all other consumers, {@code ResourceMaybeObserver} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onSuccess(Object)}, {@link #onError(Throwable)}\n * and {@link #onComplete()} are not allowed to throw any unchecked exceptions.\n *\n * <p>Example<pre><code>\n * Disposable d =\n *     Maybe.just(1).delay(1, TimeUnit.SECONDS)\n *     .subscribeWith(new ResourceMaybeObserver&lt;Integer&gt;() {\n *         &#64;Override public void onStart() {\n *             add(Schedulers.single()\n *                 .scheduleDirect(() -&gt; System.out.println(\"Time!\"),\n *                     2, TimeUnit.SECONDS));\n *         }\n *         &#64;Override public void onSuccess(Integer t) {\n *             System.out.println(t);\n *             dispose();\n *         }\n *         &#64;Override public void onError(Throwable t) {\n *             t.printStackTrace();\n *             dispose();\n *         }\n *         &#64;Override public void onComplete() {\n *             System.out.println(\"Done!\");\n *             dispose();\n *         }\n *     });\n * // ...\n * d.dispose();\n * </code></pre>\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 支持异步取消订阅并管理关联资源的 {@link MaybeObserver} 抽象基类。\n"
            " *\n"
            " * <p>所有预实现的 final 方法均为线程安全。\n"
            " *\n"
            " * <p>{@link #onSuccess(Object)}、{@link #onError(Throwable)} 与 {@link #onComplete()} 互斥，\n"
            " * 且 {@code onSuccess()} 之后不会调用 {@code onComplete()}。\n"
            " *\n"
            " * <p>在 {@code onSuccess()}、{@code onError()} 与 {@code onComplete()} 中应显式调用 {@link #dispose()}；\n"
            " * 通过 {@link #add(Disposable)} 关联资源，{@link #dispose()} 时一并清理。\n"
            " *\n"
            " * <p>仅允许单次订阅；回调不得抛出未检查异常。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
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
            "    /**\n     * Adds a resource to this {@code ResourceMaybeObserver}.\n     *\n     * @param resource the resource to add\n     *\n     * @throws NullPointerException if resource is {@code null}\n     */",
            "    /**\n"
            "     * 向本 {@code ResourceMaybeObserver} 添加资源。\n"
            "     *\n     * @param resource 要添加的资源\n"
            "     *\n     * @throws NullPointerException 若 resource 为 {@code null}\n"
            "     */",
        ),
        (
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
            "    /** setOnce 成功后调用 {@link #onStart()}。 */\n"
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    /**\n     * Called once the upstream sets a {@link Disposable} on this {@code ResourceMaybeObserver}.\n     *\n     * <p>You can perform initialization at this moment. The default\n     * implementation does nothing.\n     */",
            "    /**\n"
            "     * 上游在本 observer 上设置 {@link Disposable} 后调用。\n"
            "     * <p>可在此做初始化；默认实现为空。\n"
            "     */",
        ),
        (
            "    /**\n     * Cancels the main disposable (if any) and disposes the resources associated with\n     * this {@code ResourceMaybeObserver} (if any).\n     *\n     * <p>This method can be called before the upstream calls {@link #onSubscribe(Disposable)} at which\n     * case the main {@link Disposable} will be immediately disposed.\n     */",
            "    /**\n"
            "     * 取消主 disposable（若有）并 dispose 本 observer 关联的所有资源。\n"
            "     * <p>可在上游 {@link #onSubscribe(Disposable)} 之前调用，此时主 {@link Disposable} 会立即被 dispose。\n"
            "     */",
        ),
        (
            "    /**\n     * Returns true if this {@code ResourceMaybeObserver} has been disposed/cancelled.\n     * @return true if this {@code ResourceMaybeObserver} has been disposed/cancelled\n     */",
            "    /**\n"
            "     * 判断本 {@code ResourceMaybeObserver} 是否已 dispose/取消。\n"
            "     * @return 若已 dispose/取消则为 true\n"
            "     */",
        ),
    ],
    "ResourceObserver.java": [
        (
            "/**\n * An abstract {@link Observer} that allows asynchronous cancellation of its subscription and associated resources.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>To release the associated resources, one has to call {@link #dispose()}\n * in {@code onError()} and {@code onComplete()} explicitly.\n *\n * <p>Use {@link #add(Disposable)} to associate resources (as {@link io.reactivex.rxjava4.disposables.Disposable Disposable}s)\n * with this {@code ResourceObserver} that will be cleaned up when {@link #dispose()} is called.\n * Removing previously associated resources is not possible but one can create a\n * {@link io.reactivex.rxjava4.disposables.CompositeDisposable CompositeDisposable}, associate it with this\n * {@code ResourceObserver} and then add/remove resources to/from the {@code CompositeDisposable}\n * freely.\n *\n * <p>Use the {@link #dispose()} to dispose the sequence from within an\n * {@code onNext} implementation.\n *\n * <p>Like all other consumers, {@code ResourceObserver} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onNext(Object)}, {@link #onError(Throwable)}\n * and {@link #onComplete()} are not allowed to throw any unchecked exceptions.\n * If for some reason this can't be avoided, use {@link io.reactivex.rxjava4.core.Observable#safeSubscribe(io.reactivex.rxjava4.core.Observer)}\n * instead of the standard {@code subscribe()} method.\n *\n * <p>Example<pre><code>\n * Disposable d =\n *     Observable.range(1, 5)\n *     .subscribeWith(new ResourceObserver&lt;Integer&gt;() {\n *         &#64;Override public void onStart() {\n *             add(Schedulers.single()\n *                 .scheduleDirect(() -&gt; System.out.println(\"Time!\"),\n *                     2, TimeUnit.SECONDS));\n *             request(1);\n *         }\n *         &#64;Override public void onNext(Integer t) {\n *             if (t == 3) {\n *                 dispose();\n *             }\n *             System.out.println(t);\n *         }\n *         &#64;Override public void onError(Throwable t) {\n *             t.printStackTrace();\n *             dispose();\n *         }\n *         &#64;Override public void onComplete() {\n *             System.out.println(\"Done!\");\n *             dispose();\n *         }\n *     });\n * // ...\n * d.dispose();\n * </code></pre>\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 支持异步取消订阅并管理关联资源的 {@link Observer} 抽象基类。\n"
            " *\n"
            " * <p>所有预实现的 final 方法均为线程安全。\n"
            " *\n"
            " * <p>在 {@code onError()} 与 {@code onComplete()} 中应显式调用 {@link #dispose()}；\n"
            " * 亦可在 {@code onNext} 内调用 {@link #dispose()} 提前终止。\n"
            " *\n"
            " * <p>通过 {@link #add(Disposable)} 关联资源；仅允许单次订阅。\n"
            " * 若回调可能抛出未检查异常，请改用 {@link io.reactivex.rxjava4.core.Observable#safeSubscribe(io.reactivex.rxjava4.core.Observer)}。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
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
            "    /**\n     * Adds a resource to this {@code ResourceObserver}.\n     *\n     * @param resource the resource to add\n     *\n     * @throws NullPointerException if resource is {@code null}\n     */",
            "    /**\n"
            "     * 向本 {@code ResourceObserver} 添加资源。\n"
            "     *\n     * @param resource 要添加的资源\n"
            "     *\n     * @throws NullPointerException 若 resource 为 {@code null}\n"
            "     */",
        ),
        (
            "    @Override\n    public final void onSubscribe(Disposable d) {",
            "    /** setOnce 成功后调用 {@link #onStart()}。 */\n"
            "    @Override\n    public final void onSubscribe(Disposable d) {",
        ),
        (
            "    /**\n     * Called once the upstream sets a {@link Disposable} on this {@code ResourceObserver}.\n     *\n     * <p>You can perform initialization at this moment. The default\n     * implementation does nothing.\n     */",
            "    /**\n"
            "     * 上游在本 observer 上设置 {@link Disposable} 后调用。\n"
            "     * <p>可在此做初始化；默认实现为空。\n"
            "     */",
        ),
        (
            "    /**\n     * Cancels the main disposable (if any) and disposes the resources associated with\n     * this {@code ResourceObserver} (if any).\n     *\n     * <p>This method can be called before the upstream calls {@link #onSubscribe(Disposable)} at which\n     * case the main {@link Disposable} will be immediately disposed.\n     */",
            "    /**\n"
            "     * 取消主 disposable（若有）并 dispose 本 observer 关联的所有资源。\n"
            "     * <p>可在上游 {@link #onSubscribe(Disposable)} 之前调用，此时主 {@link Disposable} 会立即被 dispose。\n"
            "     */",
        ),
        (
            "    /**\n     * Returns true if this {@code ResourceObserver} has been disposed/cancelled.\n     * @return true if this {@code ResourceObserver} has been disposed/cancelled\n     */",
            "    /**\n"
            "     * 判断本 {@code ResourceObserver} 是否已 dispose/取消。\n"
            "     * @return 若已 dispose/取消则为 true\n"
            "     */",
        ),
    ],
    "ResourceSingleObserver.java": [
        (
            "/**\n * An abstract {@link SingleObserver} that allows asynchronous cancellation of its subscription\n * and the associated resources.\n *\n * <p>All pre-implemented final methods are thread-safe.\n *\n * <p>Override the protected {@code #onStart()} to perform initialization when this\n * {@code ResourceSingleObserver} is subscribed to a source.\n *\n * <p>Use the public {@link #dispose()} method to dispose the sequence externally and release\n * all resources.\n *\n * <p>To release the associated resources, one has to call {@link #dispose()}\n * in {@code onSuccess()} and {@code onError()} explicitly.\n *\n * <p>Use {@link #add(Disposable)} to associate resources (as {@link io.reactivex.rxjava4.disposables.Disposable Disposable}s)\n * with this {@code ResourceSingleObserver} that will be cleaned up when {@link #dispose()} is called.\n * Removing previously associated resources is not possible but one can create a\n * {@link io.reactivex.rxjava4.disposables.CompositeDisposable CompositeDisposable}, associate it with this\n * {@code ResourceSingleObserver} and then add/remove resources to/from the {@code CompositeDisposable}\n * freely.\n *\n * <p>Like all other consumers, {@code ResourceSingleObserver} can be subscribed only once.\n * Any subsequent attempt to subscribe it to a new source will yield an\n * {@link IllegalStateException} with message {@code \"It is not allowed to subscribe with a(n) <class name> multiple times.\"}.\n *\n * <p>Implementation of {@code #onStart()}, {@link #onSuccess(Object)} and {@link #onError(Throwable)}\n * are not allowed to throw any unchecked exceptions.\n *\n * <p>Example<pre><code>\n * Disposable d =\n *     Single.just(1).delay(1, TimeUnit.SECONDS)\n *     .subscribeWith(new ResourceSingleObserver&lt;Integer&gt;() {\n *         &#64;Override public void onStart() {\n *             add(Schedulers.single()\n *                 .scheduleDirect(() -&gt; System.out.println(\"Time!\"),\n *                     2, TimeUnit.SECONDS));\n *         }\n *         &#64;Override public void onSuccess(Integer t) {\n *             System.out.println(t);\n *             dispose();\n *         }\n *         &#64;Override public void onError(Throwable t) {\n *             t.printStackTrace();\n *             dispose();\n *         }\n *     });\n * // ...\n * d.dispose();\n * </code></pre>\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 支持异步取消订阅并管理关联资源的 {@link SingleObserver} 抽象基类。\n"
            " *\n"
            " * <p>所有预实现的 final 方法均为线程安全。\n"
            " *\n"
            " * <p>在 {@code onSuccess()} 与 {@code onError()} 中应显式调用 {@link #dispose()}；\n"
            " * 通过 {@link #add(Disposable)} 关联资源，{@link #dispose()} 时一并清理。\n"
            " *\n"
            " * <p>仅允许单次订阅；回调不得抛出未检查异常。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
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
            "    /**\n     * Adds a resource to this {@code ResourceSingleObserver}.\n     *\n     * @param resource the resource to add\n     *\n     * @throws NullPointerException if resource is {@code null}\n     */",
            "    /**\n"
            "     * 向本 {@code ResourceSingleObserver} 添加资源。\n"
            "     *\n     * @param resource 要添加的资源\n"
            "     *\n     * @throws NullPointerException 若 resource 为 {@code null}\n"
            "     */",
        ),
        (
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
            "    /** setOnce 成功后调用 {@link #onStart()}。 */\n"
            "    @Override\n    public final void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    /**\n     * Called once the upstream sets a {@link Disposable} on this {@code ResourceSingleObserver}.\n     *\n     * <p>You can perform initialization at this moment. The default\n     * implementation does nothing.\n     */",
            "    /**\n"
            "     * 上游在本 observer 上设置 {@link Disposable} 后调用。\n"
            "     * <p>可在此做初始化；默认实现为空。\n"
            "     */",
        ),
        (
            "    /**\n     * Cancels the main disposable (if any) and disposes the resources associated with\n     * this {@code ResourceSingleObserver} (if any).\n     *\n     * <p>This method can be called before the upstream calls {@link #onSubscribe(Disposable)} at which\n     * case the main {@link Disposable} will be immediately disposed.\n     */",
            "    /**\n"
            "     * 取消主 disposable（若有）并 dispose 本 observer 关联的所有资源。\n"
            "     * <p>可在上游 {@link #onSubscribe(Disposable)} 之前调用，此时主 {@link Disposable} 会立即被 dispose。\n"
            "     */",
        ),
        (
            "    /**\n     * Returns true if this {@code ResourceSingleObserver} has been disposed/cancelled.\n     * @return true if this {@code ResourceSingleObserver} has been disposed/cancelled\n     */",
            "    /**\n"
            "     * 判断本 {@code ResourceSingleObserver} 是否已 dispose/取消。\n"
            "     * @return 若已 dispose/取消则为 true\n"
            "     */",
        ),
    ],
    "SafeObserver.java": [
        (
            "/**\n * Wraps another {@link Observer} and ensures all {@code onXXX} methods conform the protocol\n * (except the requirement for serialized access).\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 包装另一个 {@link Observer}，确保所有 {@code onXXX} 方法符合协议\n"
            " * （序列化访问要求除外）。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
        ),
        (
            "    /** The actual Subscriber. */",
            "    /** 实际下游 Observer。 */",
        ),
        (
            "    /** The subscription. */",
            "    /** 上游订阅。 */",
        ),
        (
            "    /** Indicates a terminal state. */",
            "    /** 是否已进入终止状态。 */",
        ),
        (
            "    /**\n     * Constructs a {@code SafeObserver} by wrapping the given actual {@link Observer}.\n     * @param downstream the actual {@code Observer} to wrap, not {@code null} (not validated)\n     */",
            "    /**\n"
            "     * 通过包装给定 {@link Observer} 构造 {@code SafeObserver}。\n"
            "     * @param downstream 要包装的实际 {@code Observer}，非 {@code null}（未校验）\n"
            "     */",
        ),
        (
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
            "    /** 校验并设置 upstream，安全转发 onSubscribe；异常时 dispose 并上报 RxJavaPlugins。 */\n"
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** dispose 上游订阅。 */\n"
            "    @Override\n    public void dispose() {",
        ),
        (
            "    @Override\n    public boolean isDisposed() {",
            "    /** 委托 upstream.isDisposed()。 */\n"
            "    @Override\n    public boolean isDisposed() {",
        ),
        (
            "    @Override\n    public void onNext(@NonNull T t) {",
            "    /** 校验 null 与订阅状态后安全转发 onNext；异常时 dispose 并 onError。 */\n"
            "    @Override\n    public void onNext(@NonNull T t) {",
        ),
        (
            "    void onNextNoSubscription() {",
            "    /** 未设置订阅时以 EmptyDisposable 订阅并向下游发出错误。 */\n"
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
            "    /** 未设置订阅时以 EmptyDisposable 订阅并向下游发出错误。 */\n"
            "    void onCompleteNoSubscription() {",
        ),
    ],
    "SerializedObserver.java": [
        (
            "/**\n * Serializes access to the {@link Observer#onNext(Object)}, {@link Observer#onError(Throwable)} and\n * {@link Observer#onComplete()} methods of another {@link Observer}.\n *\n * <p>Note that {@link #onSubscribe(Disposable)} is not serialized in respect of the other methods so\n * make sure the {@code onSubscribe()} is called with a non-null {@link Disposable}\n * before any of the other methods are called.\n *\n * <p>The implementation assumes that the actual {@code Observer}'s methods don't throw.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 对另一个 {@link Observer} 的 {@link Observer#onNext(Object)}、\n"
            " * {@link Observer#onError(Throwable)} 与 {@link Observer#onComplete()} 进行串行化访问。\n"
            " *\n"
            " * <p>{@link #onSubscribe(Disposable)} 相对其他方法未串行化，\n"
            " * 须在其他方法之前以非 null {@link Disposable} 完成 onSubscribe。\n"
            " *\n"
            " * <p>假定实际 {@code Observer} 的方法不会抛出异常。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
        ),
        (
            "    /**\n     * Construct a {@code SerializedObserver} by wrapping the given actual {@link Observer}.\n     * @param downstream the actual {@code Observer}, not {@code null} (not verified)\n     */",
            "    /**\n"
            "     * 通过包装给定 {@link Observer} 构造 {@code SerializedObserver}。\n"
            "     * @param downstream 实际 {@code Observer}，非 {@code null}（未校验）\n"
            "     */",
        ),
        (
            "    /**\n     * Construct a SerializedObserver by wrapping the given actual {@link Observer} and\n     * optionally delaying the errors till all regular values have been emitted\n     * from the internal buffer.\n     * @param actual the actual {@code Observer}, not {@code null} (not verified)\n     * @param delayError if {@code true}, errors are emitted after regular values have been emitted\n     */",
            "    /**\n"
            "     * 包装给定 {@link Observer} 并可选择将错误延迟到内部缓冲中的常规值全部发出后再发出。\n"
            "     * @param actual 实际 {@code Observer}，非 {@code null}（未校验）\n"
            "     * @param delayError 若为 {@code true}，错误在所有常规值发出后再发出\n"
            "     */",
        ),
        (
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
            "    /** 校验 upstream 后将其设为自身并转发 onSubscribe。 */\n"
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** 标记 done 并 dispose 上游。 */\n"
            "    @Override\n    public void dispose() {",
        ),
        (
            "    @Override\n    public void onNext(@NonNull T t) {",
            "    /** 串行化转发 onNext；若正在 emitting 则入队。 */\n"
            "    @Override\n    public void onNext(@NonNull T t) {",
        ),
        (
            "    @Override\n    public void onError(@NonNull Throwable t) {",
            "    /** 串行化转发 onError；delayError 模式下错误入队。 */\n"
            "    @Override\n    public void onError(@NonNull Throwable t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 串行化转发 onComplete；若正在 emitting 则入队 complete 标记。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
        (
            "    void emitLoop() {",
            "    /** 排空内部队列并继续向下游发出事件。 */\n"
            "    void emitLoop() {",
        ),
    ],
    "TestObserver.java": [
        (
            "/**\n * An {@link Observer}, {@link MaybeObserver}, {@link SingleObserver} and\n * {@link CompletableObserver} composite that can record events from\n * {@link Observable}s, {@link Maybe}s, {@link Single}s and {@link Completable}s\n *  and allows making assertions about them.\n *\n * <p>You can override the {@link #onSubscribe(Disposable)}, {@link #onNext(Object)}, {@link #onError(Throwable)},\n * {@link #onComplete()} and {@link #onSuccess(Object)} methods but not the others (this is by design).\n *\n * <p>Since 4.0.0, the {@code TestObserver} does implements {@link Disposable}\n * anymore. Use {@link #asDisposable()} to create a wrapper that calls the {@link #dispose()}.\n * <strong>Implementation note</strong><br>\n * Avoids all the resource warnings because {@code Disposable} implements {@link AutoCloseable} now.\n *\n * @param <T> the value type\n * @see io.reactivex.rxjava4.subscribers.TestSubscriber\n */",
            "/**\n"
            " * 组合 {@link Observer}、{@link MaybeObserver}、{@link SingleObserver} 与\n"
            " * {@link CompletableObserver}，记录 {@link Observable}、{@link Maybe}、\n"
            " * {@link Single} 与 {@link Completable} 的事件并支持断言。\n"
            " *\n"
            " * <p>可覆盖 {@link #onSubscribe(Disposable)}、{@link #onNext(Object)}、\n"
            " * {@link #onError(Throwable)}、{@link #onComplete()} 与 {@link #onSuccess(Object)}，\n"
            " * 其余方法不可覆盖（设计如此）。\n"
            " *\n"
            " * <p>自 4.0.0 起 {@code TestObserver} 不再直接实现 {@link Disposable}；\n"
            " * 请用 {@link #asDisposable()} 获取会调用 {@link #dispose()} 的包装。\n"
            " * <strong>Implementation note</strong><br>\n"
            " * {@code Disposable} 现实现 {@link AutoCloseable}，可避免资源警告。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " * @see io.reactivex.rxjava4.subscribers.TestSubscriber\n"
            " */",
        ),
        (
            "    /** The actual observer to forward events to. */",
            "    /** 转发事件的实际 observer。 */",
        ),
        (
            "    /** Holds the current subscription if any. */",
            "    /** 保存当前订阅（若有）。 */",
        ),
        (
            "    /**\n     * Constructs a non-forwarding {@code TestObserver}.\n     * @param <T> the value type received\n     * @return the new {@code TestObserver} instance\n     */",
            "    /**\n"
            "     * 构造不转发事件的 {@code TestObserver}。\n"
            "     * @param <T> 接收的值类型\n"
            "     * @return 新的 {@code TestObserver} 实例\n"
            "     */",
        ),
        (
            "    /**\n     * Constructs a forwarding {@code TestObserver}.\n     * @param <T> the value type received\n     * @param delegate the actual {@link Observer} to forward events to\n     * @return the new {@code TestObserver} instance\n     */",
            "    /**\n"
            "     * 构造转发事件的 {@code TestObserver}。\n"
            "     * @param <T> 接收的值类型\n"
            "     * @param delegate 转发目标 {@link Observer}\n"
            "     * @return 新的 {@code TestObserver} 实例\n"
            "     */",
        ),
        (
            "    /**\n     * Constructs a non-forwarding TestObserver.\n     */",
            "    /** 构造不转发事件的 TestObserver（使用 EmptyObserver）。 */",
        ),
        (
            "    /**\n     * Constructs a forwarding {@code TestObserver}.\n     * @param downstream the actual {@link Observer} to forward events to\n     */",
            "    /**\n"
            "     * 构造转发事件的 {@code TestObserver}。\n"
            "     * @param downstream 转发目标 {@link Observer}\n"
            "     */",
        ),
        (
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
            "    /** 记录订阅线程与 upstream，校验重复订阅后转发 downstream。 */\n"
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    @Override\n    public void onNext(@NonNull T t) {",
            "    /** 记录值与线程，校验订阅顺序后转发 downstream。 */\n"
            "    @Override\n    public void onNext(@NonNull T t) {",
        ),
        (
            "    @Override\n    public void onError(@NonNull Throwable t) {",
            "    /** 记录错误与线程，转发 downstream 并 countDown done。 */\n"
            "    @Override\n    public void onError(@NonNull Throwable t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 递增 completions，转发 downstream 并 countDown done。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
        (
            "    @Override\n    public final void dispose() {",
            "    /** 通过 DisposableHelper 取消 upstream。 */\n"
            "    @Override\n    public final void dispose() {",
        ),
        (
            "    /**\n     * Expose this {@code TestObserver} as a {@link Disposable} object.\n     * @return the {@code Disposable} view of this {@code TestObserver}\n     * @since 4.0.0\n     */",
            "    /**\n"
            "     * 将本 {@code TestObserver} 暴露为 {@link Disposable} 视图。\n"
            "     * @return 本 {@code TestObserver} 的 {@code Disposable} 包装\n"
            "     * @since 4.0.0\n"
            "     */",
        ),
        (
            "    /**\n     * Returns true if this {@code TestObserver} received a subscription.\n     * @return true if this {@code TestObserver} received a subscription\n     */",
            "    /**\n"
            "     * 判断本 {@code TestObserver} 是否已收到订阅。\n"
            "     * @return 若已收到订阅则为 true\n"
            "     */",
        ),
        (
            "    /**\n     * Assert that the {@link #onSubscribe(Disposable)} method was called exactly once.\n     * @return this\n     */",
            "    /**\n"
            "     * 断言 {@link #onSubscribe(Disposable)} 恰好被调用一次。\n"
            "     * @return this\n"
            "     */",
        ),
        (
            "    @Override\n    public void onSuccess(@NonNull T value) {",
            "    /** Single/Maybe 成功：记录 onNext 后 onComplete。 */\n"
            "    @Override\n    public void onSuccess(@NonNull T value) {",
        ),
        (
            "    /**\n     * An observer that ignores all events and does not report errors.\n     */",
            "    /** 忽略所有事件且不上报错误的 observer。 */",
        ),
    ],
    "package-info.java": [
        (
            "/**\n * Default wrappers and implementations for observer-based consumer classes and interfaces,\n * including disposable and resource-tracking variants and\n * the {@link io.reactivex.rxjava4.observers.TestObserver TestObserver} that allows unit testing\n * {@link io.reactivex.rxjava4.core.Observable Observable}-, {@link io.reactivex.rxjava4.core.Single Single}-,\n * {@link io.reactivex.rxjava4.core.Maybe Maybe}- and {@link io.reactivex.rxjava4.core.Completable Completable}-based flows.\n * <p>\n * Available observer variants\n * <br>\n * <table border=\"1\" style=\"border-collapse: collapse;\">\n * <caption>The available observer types.</caption>",
            "/**\n"
            " * 基于 observer 的 consumer 类与接口的默认包装与实现，\n"
            " * 含 disposable 与 resource 跟踪变体，以及用于单元测试\n"
            " * {@link io.reactivex.rxjava4.core.Observable Observable}、\n"
            " * {@link io.reactivex.rxjava4.core.Single Single}、\n"
            " * {@link io.reactivex.rxjava4.core.Maybe Maybe} 与\n"
            " * {@link io.reactivex.rxjava4.core.Completable Completable} 流的\n"
            " * {@link io.reactivex.rxjava4.observers.TestObserver TestObserver}。\n"
            " * <p>\n"
            " * 可用的 observer 变体\n"
            " * <br>\n"
            " * <table border=\"1\" style=\"border-collapse: collapse;\">\n"
            " * <caption>可用的 observer 类型。</caption>",
        ),
        (
            " * <tr><td><b>Reactive type</b></td><td><b>Base interface</b></td><td><b>Simple</b></td><td><b>Disposable</b></td><td><b>Resource</b></td></tr>",
            " * <tr><td><b>响应式类型</b></td><td><b>基础接口</b></td><td><b>Simple</b></td><td><b>Disposable</b></td><td><b>Resource</b></td></tr>",
        ),
    ],
    "ConditionalSubscriber.java": [
        (
            "/**\n * A {@link FlowableSubscriber} with an additional {@link #tryOnNext(Object)} method that\n * tells the caller the specified value has been accepted or not.\n *\n * <p>This allows certain queue-drain or source-drain operators\n * to avoid requesting 1 on behalf of a dropped value.\n *\n * @param <T> the value type\n * @since 3.1.1\n */",
            "/**\n"
            " * 在 {@link FlowableSubscriber} 基础上增加 {@link #tryOnNext(Object)}，\n"
            " * 告知调用方指定值是否已被接受。\n"
            " *\n"
            " * <p>某些 queue-drain 或 source-drain 算子可据此避免为丢弃的值额外 request(1)。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " * @since 3.1.1\n"
            " */",
        ),
        (
            "    /**\n     * Conditionally takes the value.\n     * @param t the value to deliver\n     * @return true if the value has been accepted, false if the value has been rejected\n     * and the next value can be sent immediately\n     */",
            "    /**\n"
            "     * 有条件地接收值。\n"
            "     * @param t 要传递的值\n"
            "     * @return 若值已被接受则为 true；若被拒绝且可立即发送下一值则为 false\n"
            "     */",
        ),
    ],
    "DeferredEnumerableSource.java": [
        (
            "/// Represents an unknown length, deferred iterable source which can be moved forward synchronously\n///  and obtain the current item via a simple call once it reports said iterable elements are ready\n/// to be consumed.\n/// <p>\n/// No {@code hasNext} and {@code next} duplication. C# IEnumerator is way better in this regard.\n/// @param <T> the element type of the source\n/// @see IndexableSource\n/// @since 4.0.0",
            "/// 表示长度未知、可延迟就绪的可迭代源：可同步前移，\n"
            "/// 并在报告元素可消费后通过简单调用获取当前项。\n"
            "/// <p>\n"
            "/// 避免 {@code hasNext} 与 {@code next} 的重复调用；C# IEnumerator 在此方面更优。\n"
            "/// @param <T> 源元素类型\n"
            "/// @see IndexableSource\n"
            "/// @since 4.0.0",
        ),
        (
            "    /**\n     * Returns true if the source is ready to be consumed via its\n     * {@link EnumerableSource#nextSync()} and {@link EnumerableSource#current()}\n     * methods.\n     * @return the completion stage that indicates an empty {@code false} or a non-empty\n     *         {@code true} enumerable source is now available\n     */",
            "    /**\n"
            "     * 返回源是否可通过 {@link EnumerableSource#nextSync()} 与\n"
            "     * {@link EnumerableSource#current()} 消费。\n"
            "     * @return 完成阶段：空源为 {@code false}，非空可迭代源就绪为 {@code true}\n"
            "     */",
        ),
    ],
    "EnumerableSource.java": [
        (
            "/// Represents an unknown length iterable source which can be moved forward synchronously and obtain the current\n/// item via a simple call.\n/// <p>\n/// No {@code hasNext} and {@code next} duplication. C# IEnumerator is way better in this regard.\n/// @param <T> the element type of the source\n/// @see IndexableSource\n/// @since 4.0.0",
            "/// 表示长度未知的可迭代源：可同步前移并通过简单调用获取当前项。\n"
            "/// <p>\n"
            "/// 避免 {@code hasNext} 与 {@code next} 的重复调用；C# IEnumerator 在此方面更优。\n"
            "/// @param <T> 源元素类型\n"
            "/// @see IndexableSource\n"
            "/// @since 4.0.0",
        ),
        (
            "    /**\n     * Synchronously obtains the next item or returns {@code false} if no\n     * more items.\n     * @return {@code true} it there is an item available which can be obtained via {@link #current()},\n     *         {@code false} if no more items are available\n     * @throws Throwable if there is a (processing) error while going to the next item synchronously\n     */",
            "    /**\n"
            "     * 同步获取下一项；若无更多项则返回 {@code false}。\n"
            "     * @return 若有可用项（可通过 {@link #current()} 获取）则为 {@code true}，\n"
            "     *         否则为 {@code false}\n"
            "     * @throws Throwable 同步前进时发生（处理）错误\n"
            "     */",
        ),
        (
            "    /**\n     * Returns the current item if {@link #nextSync()} returned {@code true} the previous call.\n     * <p>\n     * Calling before the first or after exhaustion of the source is an undefined behavior\n     * @return the current item\n     */",
            "    /**\n"
            "     * 若上次 {@link #nextSync()} 返回 {@code true}，返回当前项。\n"
            "     * <p>\n"
            "     * 在首次调用前或源耗尽后调用行为未定义。\n"
            "     * @return 当前项\n"
            "     */",
        ),
    ],
    "IndexableSource.java": [
        (
            "/// Represents a source which can be accessed via a zero-based index synchronously,\n/// without going through the usual [Streamer#next()] calls to obtain the next item.\n/// @param <T> the element type of the source\n/// @since 4.0.0",
            "/// 表示可通过从零开始的索引同步访问的源，\n"
            "/// 无需像 [Streamer#next()] 那样逐步获取下一项。\n"
            "/// @param <T> 源元素类型\n"
            "/// @since 4.0.0",
        ),
        (
            "    /**\n     * Obtain an element from the given index.\n     * Make sure you read only up to {@link #limit()}\n     * @param index the index\n     * @return the element at the specified index\n     * @throws Throwable if the indexed access involves computation that can throw\n     */",
            "    /**\n"
            "     * 按给定索引获取元素；请仅读取不超过 {@link #limit()} 的范围。\n"
            "     * @param index 索引\n"
            "     * @return 指定索引处的元素\n"
            "     * @throws Throwable 若索引访问涉及可能抛出的计算\n"
            "     */",
        ),
        (
            "    /**\n     * Returns the limit of how many items can be obtained via [{@link #elementAt(long)}.\n     * @return the index limit, exclusive\n     */",
            "    /**\n"
            "     * 返回可通过 {@link #elementAt(long)} 获取的元素数量上限。\n"
            "     * @return 索引上限（不含）\n"
            "     */",
        ),
    ],
    "QueueDisposable.java": [
        (
            "/**\n * An interface extending {@link SimpleQueue} and {@link Disposable} and allows negotiating\n * the fusion mode between subsequent operators of the {@link io.reactivex.rxjava4.core.Observable Observable} base reactive type.\n * <p>\n * The negotiation happens in subscription time when the upstream\n * calls the {@code onSubscribe} with an instance of this interface. The\n * downstream has then the obligation to call {@link #requestFusion(int)}\n * with the appropriate mode before calling {@code request()}.\n * <p>\n * In <b>synchronous fusion</b>, all upstream values are either already available or is generated\n * when {@link #poll()} is called synchronously. When the {@link #poll()} returns {@code null},\n * that is the indication if a terminated stream. In this mode, the upstream won't call the onXXX methods.\n * <p>\n * In <b>asynchronous fusion</b>, upstream values may become available to {@link #poll()} eventually.\n * Upstream signals {@code onError()} and {@code onComplete()} as usual, however,\n * {@code onNext} will be called with {@code null} instead of the actual value.\n * Downstream should treat such onNext as indication that {@link #poll()} can be called.\n * <p>\n * The general rules for consuming the {@link SimpleQueue} interface:\n * <ul>\n * <li> {@link #poll()} and {@link #clear()} has to be called sequentially (from within a serializing drain-loop).</li>\n * <li>In addition, callers of {@link #poll()} should be prepared to catch exceptions.</li>\n * <li>Due to how computation attaches to the {@link #poll()}, {@link #poll()} may return\n * {@code null} even if a preceding {@link #isEmpty()} returned false.</li>\n * </ul>\n * <p>\n * Implementations should only allow calling the following methods and the rest of the\n * {@link SimpleQueue} interface methods should throw {@link UnsupportedOperationException}:\n * <ul>\n * <li>{@link #poll()}</li>\n * <li>{@link #isEmpty()}</li>\n * <li>{@link #clear()}</li>\n * </ul>\n * @param <T> the value type transmitted through the queue\n * @see QueueSubscription\n * @since 3.1.1\n */",
            "/**\n"
            " * 扩展 {@link SimpleQueue} 与 {@link Disposable}，用于协商\n"
            " * {@link io.reactivex.rxjava4.core.Observable Observable} 链上相邻算子之间的融合模式。\n"
            " * <p>\n"
            " * 协商发生在订阅时：上游以本接口实例调用 {@code onSubscribe}，\n"
            " * 下游须在 {@code request()} 前调用 {@link #requestFusion(int)} 指定模式。\n"
            " * <p>\n"
            " * <b>同步融合</b>：上游值已就绪或在 {@link #poll()} 同步调用时生成；\n"
            " * {@link #poll()} 返回 {@code null} 表示流已终止，上游不再调用 onXXX。\n"
            " * <p>\n"
            " * <b>异步融合</b>：值可能稍后通过 {@link #poll()} 可用；\n"
            " * onError/onComplete 照常，但 onNext 以 {@code null} 代替实际值，\n"
            " * 下游应将其视为可调用 {@link #poll()} 的信号。\n"
            " * <p>\n"
            " * 消费 {@link SimpleQueue} 的一般规则：\n"
            " * <ul>\n"
            " * <li>{@link #poll()} 与 {@link #clear()} 须在串行 drain-loop 中顺序调用。</li>\n"
            " * <li>{@link #poll()} 调用方应准备捕获异常。</li>\n"
            " * <li>因计算附着于 {@link #poll()}，即使 {@link #isEmpty()} 曾为 false，{@link #poll()} 仍可能返回 {@code null}。</li>\n"
            " * </ul>\n"
            " * <p>\n"
            " * 实现应仅允许调用 {@link #poll()}、{@link #isEmpty()}、{@link #clear()}，\n"
            " * 其余 {@link SimpleQueue} 方法应抛出 {@link UnsupportedOperationException}。\n"
            " * @param <T> 经队列传递的值类型\n"
            " * @see QueueSubscription\n"
            " * @since 3.1.1\n"
            " */",
        ),
    ],
}
