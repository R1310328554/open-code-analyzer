"""Large replacement blocks for RxJava wave-4b."""

LARGE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DisposableHelper.java": [
        (
            "/**\n * Utility methods for working with Disposables atomically.\n */",
            "/**\n * 原子操作 Disposable 的工具方法。\n */",
        ),
        (
            "    /**\n     * The singleton instance representing a terminal, disposed state, don't leak it.\n     */",
            "    /**\n     * 表示终态、已 dispose 状态的单例实例，请勿泄漏。\n     */",
        ),
        (
            "    /**\n     * Checks if the given Disposable is the common {@link #DISPOSED} enum value.\n     * @param d the disposable to check\n     * @return true if d is {@link #DISPOSED}\n     */",
            "    /**\n     * 检查给定 Disposable 是否为公共 {@link #DISPOSED} 枚举值。\n     * @param d 要检查的 disposable\n     * @return 若 d 为 {@link #DISPOSED} 则为 true\n     */",
        ),
        (
            "    /**\n     * Atomically sets the field and disposes the old contents.\n     * @param field the target field\n     * @param d the new Disposable to set\n     * @return true if successful, false if the field contains the {@link #DISPOSED} instance.\n     */",
            "    /**\n     * 原子设置字段并 dispose 旧内容。\n     * @param field 目标字段\n     * @param d 要设置的新 Disposable\n     * @return 成功则为 true；若字段包含 {@link #DISPOSED} 实例则为 false\n     */",
        ),
        (
            "    /**\n     * Atomically sets the field to the given non-null Disposable and returns true\n     * or returns false if the field is non-null.\n     * If the target field contains the common DISPOSED instance, the supplied disposable\n     * is disposed. If the field contains other non-null Disposable, an IllegalStateException\n     * is signaled to the RxJavaPlugins.onError hook.\n     * \n     * @param field the target field\n     * @param d the disposable to set, not null\n     * @return true if the operation succeeded, false\n     */",
            "    /**\n     * 原子地将字段设置为给定非 null Disposable 并返回 true，\n     * 若字段非 null 则返回 false。\n     * 若目标字段包含公共 DISPOSED 实例，则 dispose 提供的 disposable。\n     * 若字段包含其它非 null Disposable，则向 RxJavaPlugins.onError 钩子报告 IllegalStateException。\n     *\n     * @param field 目标字段\n     * @param d 要设置的 disposable，不可为 null\n     * @return 操作成功则为 true，否则为 false\n     */",
        ),
        (
            "    /**\n     * Atomically replaces the Disposable in the field with the given new Disposable\n     * but does not dispose the old one.\n     * @param field the target field to change\n     * @param d the new disposable, null allowed\n     * @return true if the operation succeeded, false if the target field contained\n     * the common DISPOSED instance and the given disposable (if not null) is disposed.\n     */",
            "    /**\n     * 原子地将字段中的 Disposable 替换为给定新 Disposable，但不 dispose 旧实例。\n     * @param field 要更改的目标字段\n     * @param d 新 disposable，允许为 null\n     * @return 操作成功则为 true；若目标字段包含公共 DISPOSED 实例且给定 disposable（若非 null）已被 dispose 则为 false\n     */",
        ),
        (
            "    /**\n     * Atomically disposes the Disposable in the field if not already disposed.\n     * @param field the target field\n     * @return true if the current thread managed to dispose the Disposable\n     */",
            "    /**\n     * 若字段中的 Disposable 尚未 dispose，则原子 dispose 它。\n     * @param field 目标字段\n     * @return 若当前线程成功 dispose Disposable 则为 true\n     */",
        ),
        (
            "    /**\n     * Verifies that current is null, next is not null, otherwise signals errors\n     * to the RxJavaPlugins and returns false.\n     * @param current the current Disposable, expected to be null\n     * @param next the next Disposable, expected to be non-null\n     * @return true if the validation succeeded\n     */",
            "    /**\n     * 验证 current 为 null 且 next 非 null，否则向 RxJavaPlugins 报告错误并返回 false。\n     * @param current 当前 Disposable，期望为 null\n     * @param next 下一个 Disposable，期望非 null\n     * @return 验证成功则为 true\n     */",
        ),
        (
            "    /**\n     * Reports that the disposable is already set to the RxJavaPlugins error handler.\n     */",
            "    /**\n     * 向 RxJavaPlugins 错误处理器报告 disposable 已被设置。\n     */",
        ),
        (
            "    /**\n     * Atomically tries to set the given Disposable on the field if it is null or disposes it if\n     * the field contains {@link #DISPOSED}.\n     * @param field the target field\n     * @param d the disposable to set\n     * @return true if successful, false otherwise\n     */",
            "    /**\n     * 若字段为 null 则原子尝试设置给定 Disposable；若字段包含 {@link #DISPOSED} 则 dispose 它。\n     * @param field 目标字段\n     * @param d 要设置的 disposable\n     * @return 成功则为 true，否则为 false\n     */",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** 故意无操作。 */\n    @Override\n    public void dispose() {",
        ),
        (
            "    @Override\n    public boolean isDisposed() {",
            "    /** 始终返回 true，表示已 dispose。 */\n    @Override\n    public boolean isDisposed() {",
        ),
    ],
    "EmptyDisposable.java": [
        (
            "/**\n * Represents a stateless empty Disposable that reports being always\n * empty and disposed.\n * <p>It is also async-fuseable but empty all the time.\n * <p>Since EmptyDisposable implements QueueDisposable and is empty,\n * don't use it in tests and then signal onNext with it;\n * use Disposables.empty() instead.\n */",
            "/**\n * 表示无状态的空 Disposable，始终报告为空且已 dispose。\n * <p>它也是 async-fuseable，但始终为空。\n * <p>由于 EmptyDisposable 实现 QueueDisposable 且为空，\n * 不要在测试中用它再 signal onNext；请改用 Disposables.empty()。\n */",
        ),
        (
            "    /**\n     * Since EmptyDisposable implements QueueDisposable and is empty,\n     * don't use it in tests and then signal onNext with it;\n     * use Disposables.empty() instead.\n     */",
            "    /**\n     * 由于 EmptyDisposable 实现 QueueDisposable 且为空，\n     * 不要在测试中用它再 signal onNext；请改用 Disposables.empty()。\n     */",
        ),
        (
            "    /**\n     * An empty disposable that returns false for isDisposed.\n     */",
            "    /**\n     * 对 isDisposed 返回 false 的空 disposable。\n     */",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** 无操作。 */\n    @Override\n    public void dispose() {",
        ),
        (
            "    @Override\n    public boolean isDisposed() {",
            "    /** 仅 INSTANCE 常量视为已 dispose。 */\n    @Override\n    public boolean isDisposed() {",
        ),
        (
            "    public static void complete(Observer<?> observer) {",
            "    /** 订阅 INSTANCE 并向 Observer 发送 onComplete。 */\n    public static void complete(Observer<?> observer) {",
        ),
        (
            "    public static void complete(MaybeObserver<?> observer) {",
            "    /** 订阅 INSTANCE 并向 MaybeObserver 发送 onComplete。 */\n    public static void complete(MaybeObserver<?> observer) {",
        ),
        (
            "    public static void error(Throwable e, Observer<?> observer) {",
            "    /** 订阅 INSTANCE 并向 Observer 发送 onError。 */\n    public static void error(Throwable e, Observer<?> observer) {",
        ),
        (
            "    public static void complete(CompletableObserver observer) {",
            "    /** 订阅 INSTANCE 并向 CompletableObserver 发送 onComplete。 */\n    public static void complete(CompletableObserver observer) {",
        ),
        (
            "    public static void error(Throwable e, CompletableObserver observer) {",
            "    /** 订阅 INSTANCE 并向 CompletableObserver 发送 onError。 */\n    public static void error(Throwable e, CompletableObserver observer) {",
        ),
        (
            "    public static void error(Throwable e, SingleObserver<?> observer) {",
            "    /** 订阅 INSTANCE 并向 SingleObserver 发送 onError。 */\n    public static void error(Throwable e, SingleObserver<?> observer) {",
        ),
        (
            "    public static void error(Throwable e, MaybeObserver<?> observer) {",
            "    /** 订阅 INSTANCE 并向 MaybeObserver 发送 onError。 */\n    public static void error(Throwable e, MaybeObserver<?> observer) {",
        ),
        (
            "    @Override\n    public boolean offer(Object value) {",
            "    /** 不应被调用。 */\n    @Override\n    public boolean offer(Object value) {",
        ),
        (
            "    @Override\n    public boolean offer(Object v1, Object v2) {",
            "    /** 不应被调用。 */\n    @Override\n    public boolean offer(Object v1, Object v2) {",
        ),
        (
            "    @Nullable\n    @Override\n    public Object poll() {",
            "    /** 始终为空，返回 null。 */\n    @Nullable\n    @Override\n    public Object poll() {",
        ),
        (
            "    @Override\n    public boolean isEmpty() {",
            "    /** 始终为空。 */\n    @Override\n    public boolean isEmpty() {",
        ),
        (
            "    @Override\n    public void clear() {",
            "    /** 无内容可清。 */\n    @Override\n    public void clear() {",
        ),
        (
            "    @Override\n    public int requestFusion(int mode) {",
            "    /** 返回 async 融合模式。 */\n    @Override\n    public int requestFusion(int mode) {",
        ),
    ],
    "ListCompositeDisposable.java": [
        (
            "/**\n * A disposable container that can hold onto multiple other disposables.\n */",
            "/**\n * 可持有多个其它 disposable 的 disposable 容器。\n */",
        ),
        (
            "    public ListCompositeDisposable() {",
            "    /** 构造空的 ListCompositeDisposable。 */\n    public ListCompositeDisposable() {",
        ),
        (
            "    public ListCompositeDisposable(Disposable... resources) {",
            "    /** @param resources 初始 disposable 数组 */\n    public ListCompositeDisposable(Disposable... resources) {",
        ),
        (
            "    public ListCompositeDisposable(Iterable<? extends Disposable> resources) {",
            "    /** @param resources 初始 disposable 集合 */\n    public ListCompositeDisposable(Iterable<? extends Disposable> resources) {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** dispose 所有持有的 disposable 并标记容器为已 dispose。 */\n    @Override\n    public void dispose() {",
        ),
        (
            "    @Override\n    public boolean isDisposed() {",
            "    /** 若容器已被 dispose 则返回 true。 */\n    @Override\n    public boolean isDisposed() {",
        ),
        (
            "    @Override\n    public boolean add(Disposable d) {",
            "    /** 添加 disposable；若容器已 dispose 则直接 dispose 该 disposable。 */\n    @Override\n    public boolean add(Disposable d) {",
        ),
        (
            "    public boolean addAll(Disposable... ds) {",
            "    /** 批量添加 disposable；若容器已 dispose 则 dispose 所有参数。 */\n    public boolean addAll(Disposable... ds) {",
        ),
        (
            "    @Override\n    public boolean remove(Disposable d) {",
            "    /** 移除并 dispose 给定 disposable。 */\n    @Override\n    public boolean remove(Disposable d) {",
        ),
        (
            "    @Override\n    public boolean delete(Disposable d) {",
            "    /** 移除但不 dispose 给定 disposable。 */\n    @Override\n    public boolean delete(Disposable d) {",
        ),
        (
            "    public void clear() {",
            "    /** 移除并 dispose 所有持有的 disposable。 */\n    public void clear() {",
        ),
        (
            "    void dispose(List<Disposable> set) {",
            "    /** 依次 dispose 列表中的 disposable，聚合异常为 CompositeException。 */\n    void dispose(List<Disposable> set) {",
        ),
        (
            "    @Override\n    public void reset() {",
            "    /** 清空持有的 disposable 但不 dispose 它们。 */\n    @Override\n    public void reset() {",
        ),
        (
            "    @Override\n    public DisposableStreamerCancellation derive() {",
            "    /** 派生新的 ListCompositeDisposable 并注册自清理逻辑。 */\n    @Override\n    public DisposableStreamerCancellation derive() {",
        ),
    ],
}
