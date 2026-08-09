"""Chinese annotation replacements for Redisson 4.7.0 wave-36a spring-data-41/tx [0:15]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parent


def _load(module_file: str, attr: str) -> dict[str, list[tuple[str, str]]]:
    spec = importlib.util.spec_from_file_location(module_file, SCRIPTS / module_file)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return getattr(mod, attr)


W11A = _load("wave11a_replacements_redisson.py", "W11A_REPLACEMENTS")
W21B = _load("wave21b_replacements_redisson.py", "W21B_REPLACEMENTS")
W27B = _load("wave27b_replacements_redisson.py", "W27B_REPLACEMENTS")
W31A = _load("wave31a_replacements_redisson.py", "W31A_REPLACEMENTS")

_SD41 = "redisson-spring/redisson-spring-data/redisson-spring-data-41/src/main/java/org/redisson/spring/data/connection/"
_TX = "redisson-spring/redisson-spring-transaction/src/main/java/org/redisson/spring/transaction/"

W36A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-41: subscription/connection identical to spring-data-40/34.
for _name in (
    "RedissonReactiveSubscription.java",
    "RedissonSentinelConnection.java",
):
    W36A_REPLACEMENTS[_name] = W31A[_name]
    W36A_REPLACEMENTS[_SD41 + _name] = W31A[_name]

W36A_REPLACEMENTS["RedissonSubscription.java"] = W27B["RedissonSubscription.java"]
W36A_REPLACEMENTS[_SD41 + "RedissonSubscription.java"] = W27B["RedissonSubscription.java"]

# spring-data-41: ZSET replay decoders identical to spring-data-40/34.
for _name in (
    "ScoredSortedListReplayDecoder.java",
    "ScoredSortedSetBlockingReplayDecoder.java",
    "ScoredSortedSetReplayDecoder.java",
    "ScoredSortedSetReplayDecoderV2.java",
    "ScoredSortedSingleBlockingReplayDecoder.java",
    "ScoredSortedSingleReplayDecoder.java",
):
    W36A_REPLACEMENTS[_name] = W31A[_name]
    W36A_REPLACEMENTS[_SD41 + _name] = W31A[_name]

# spring-data-41: convertors/decoders identical to spring-data-40/34.
for _name, _src in (
    ("SecondsConvertor.java", W11A),
    ("SetReplayDecoder.java", W11A),
    ("SingleMapEntryDecoder.java", W21B),
):
    W36A_REPLACEMENTS[_name] = _src[_name]
    W36A_REPLACEMENTS[_SD41 + _name] = _src[_name]

# spring-tx: reactive Redisson transaction helpers.
W36A_REPLACEMENTS["ReactiveRedissonResourceHolder.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 响应式 Redisson 事务资源持有者：在 {@link org.springframework.transaction.reactive.TransactionSynchronizationManager}\n"
        " * 中以 {@link org.redisson.api.RedissonReactiveClient} 为键绑定 {@link org.redisson.api.RTransactionReactive}。\n"
        " * <p>继承 {@link org.springframework.transaction.support.ResourceHolderSupport} 以支持 rollback-only 标记。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private RTransactionReactive transaction;",
        "    /** 当前 Reactor 上下文绑定的 Redisson 响应式事务。 */\n"
        "    private RTransactionReactive transaction;",
    ),
    (
        "    public RTransactionReactive getTransaction() {",
        "    /** 返回绑定的 {@link RTransactionReactive}。 */\n"
        "    public RTransactionReactive getTransaction() {",
    ),
    (
        "    public void setTransaction(RTransactionReactive transaction) {",
        "    /** 设置或清空绑定的 {@link RTransactionReactive}（完成清理时使用）。 */\n"
        "    public void setTransaction(RTransactionReactive transaction) {",
    ),
]
W36A_REPLACEMENTS[_TX + "ReactiveRedissonResourceHolder.java"] = W36A_REPLACEMENTS[
    "ReactiveRedissonResourceHolder.java"
]

W36A_REPLACEMENTS["ReactiveRedissonTransactionObject.java"] = [
    (
        "/**\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * 响应式 Redisson 事务对象：实现 {@link org.springframework.transaction.support.SmartTransactionObject}，\n"
        " * 供 {@link ReactiveRedissonTransactionManager} 在事务生命周期中持有\n"
        " * {@link ReactiveRedissonResourceHolder}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    private ReactiveRedissonResourceHolder resourceHolder;",
        "    /** 绑定的 Redisson 响应式资源持有者。 */\n"
        "    private ReactiveRedissonResourceHolder resourceHolder;",
    ),
    (
        "    public ReactiveRedissonResourceHolder getResourceHolder() {",
        "    /** 返回当前资源持有者。 */\n"
        "    public ReactiveRedissonResourceHolder getResourceHolder() {",
    ),
    (
        "    public void setResourceHolder(ReactiveRedissonResourceHolder resourceHolder) {",
        "    /** 设置或清空资源持有者（挂起/恢复时使用）。 */\n"
        "    public void setResourceHolder(ReactiveRedissonResourceHolder resourceHolder) {",
    ),
    (
        "    @Override\n    public boolean isRollbackOnly() {",
        "    /** 若资源持有者存在则读取其 rollback-only 状态。 */\n"
        "    @Override\n"
        "    public boolean isRollbackOnly() {",
    ),
    (
        "    @Override\n    public void flush() {",
        "    /** Redisson 响应式事务无需 flush，空实现。 */\n"
        "    @Override\n"
        "    public void flush() {",
    ),
    (
        "        // skip",
        "        // Redisson 响应式事务不支持 flush，跳过。",
    ),
]
W36A_REPLACEMENTS[_TX + "ReactiveRedissonTransactionObject.java"] = W36A_REPLACEMENTS[
    "ReactiveRedissonTransactionObject.java"
]

W36A_REPLACEMENTS["ReactiveRedissonTransactionManager.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 {@link org.redisson.api.RedissonReactiveClient} 的 Spring 响应式事务管理器。\n"
        " * <p>继承 {@link org.springframework.transaction.reactive.AbstractReactiveTransactionManager}，\n"
        " * 将 {@link TransactionDefinition} 映射为 {@link TransactionOptions} 并绑定\n"
        " * {@link RTransactionReactive} 到 {@link TransactionSynchronizationManager}。\n"
        " * <p>提交/回滚以 Reactor {@link Mono} 返回，错误包装为 {@link TransactionSystemException}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final RedissonReactiveClient redissonClient;",
        "    /** Redisson 响应式客户端，兼作事务资源键。 */\n"
        "    private final RedissonReactiveClient redissonClient;",
    ),
    (
        "    public ReactiveRedissonTransactionManager(RedissonReactiveClient redissonClient) {",
        "    /** 指定提供 {@link RTransactionReactive} 的 Redisson 响应式客户端。 */\n"
        "    public ReactiveRedissonTransactionManager(RedissonReactiveClient redissonClient) {",
    ),
    (
        "    public Mono<RTransactionReactive> getCurrentTransaction() {",
        "    /** 从当前 Reactor 事务上下文获取绑定的 {@link RTransactionReactive}。 */\n"
        "    public Mono<RTransactionReactive> getCurrentTransaction() {",
    ),
    (
        "    @Override\n    protected Object doGetTransaction(TransactionSynchronizationManager synchronizationManager) throws TransactionException {",
        "    /** 创建事务对象并读取已绑定的 {@link ReactiveRedissonResourceHolder}（若有）。 */\n"
        "    @Override\n"
        "    protected Object doGetTransaction(TransactionSynchronizationManager synchronizationManager) throws TransactionException {",
    ),
    (
        "    @Override\n    protected Mono<Void> doBegin(TransactionSynchronizationManager synchronizationManager, Object transaction, TransactionDefinition definition) throws TransactionException {",
        "    /** 按 {@link TransactionDefinition} 超时创建 {@link RTransactionReactive} 并绑定资源。 */\n"
        "    @Override\n"
        "    protected Mono<Void> doBegin(TransactionSynchronizationManager synchronizationManager, Object transaction, TransactionDefinition definition) throws TransactionException {",
    ),
    (
        "        if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {",
        "        // 将 Spring 事务超时（秒）写入 Redisson TransactionOptions。\n"
        "        if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {",
    ),
    (
        "    @Override\n    protected Mono<Void> doCommit(TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction status) throws TransactionException {",
        "    /** 提交绑定的 {@link RTransactionReactive}；失败映射为 {@link TransactionSystemException}。 */\n"
        "    @Override\n"
        "    protected Mono<Void> doCommit(TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction status) throws TransactionException {",
    ),
    (
        "    @Override\n    protected Mono<Void> doRollback(TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction status) throws TransactionException {",
        "    /** 回滚绑定的 {@link RTransactionReactive}；失败映射为 {@link TransactionSystemException}。 */\n"
        "    @Override\n"
        "    protected Mono<Void> doRollback(TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction status) throws TransactionException {",
    ),
    (
        "    @Override\n    protected Mono<Object> doSuspend(TransactionSynchronizationManager synchronizationManager, Object transaction) throws TransactionException {",
        "    /** 挂起当前事务：解绑资源并返回供后续 resume 的 suspendedResources。 */\n"
        "    @Override\n"
        "    protected Mono<Object> doSuspend(TransactionSynchronizationManager synchronizationManager, Object transaction) throws TransactionException {",
    ),
    (
        "    @Override\n    protected Mono<Void> doResume(TransactionSynchronizationManager synchronizationManager, Object transaction, Object suspendedResources) throws TransactionException {",
        "    /** 恢复挂起的事务资源到 {@link TransactionSynchronizationManager}。 */\n"
        "    @Override\n"
        "    protected Mono<Void> doResume(TransactionSynchronizationManager synchronizationManager, Object transaction, Object suspendedResources) throws TransactionException {",
    ),
    (
        "    @Override\n    protected Mono<Void> doSetRollbackOnly(TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction status) throws TransactionException {",
        "    /** 将资源持有者标记为 rollback-only。 */\n"
        "    @Override\n"
        "    protected Mono<Void> doSetRollbackOnly(TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction status) throws TransactionException {",
    ),
    (
        "    @Override\n    protected Mono<Void> doCleanupAfterCompletion(TransactionSynchronizationManager synchronizationManager, Object transaction) {",
        "    /** 事务完成后解绑资源并清空 {@link RTransactionReactive} 引用。 */\n"
        "    @Override\n"
        "    protected Mono<Void> doCleanupAfterCompletion(TransactionSynchronizationManager synchronizationManager, Object transaction) {",
    ),
    (
        "    @Override\n    protected boolean isExistingTransaction(Object transaction) throws TransactionException {",
        "    /** 若事务对象已持有资源则视为存在活动事务。 */\n"
        "    @Override\n"
        "    protected boolean isExistingTransaction(Object transaction) throws TransactionException {",
    ),
]
W36A_REPLACEMENTS[_TX + "ReactiveRedissonTransactionManager.java"] = W36A_REPLACEMENTS[
    "ReactiveRedissonTransactionManager.java"
]
