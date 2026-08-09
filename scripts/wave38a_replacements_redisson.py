"""Chinese annotation replacements for Redisson 4.7.0 wave-38a tomcat-8/9 + core [0:15]."""
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


W37B = _load("wave37b_replacements_redisson.py", "W37B_REPLACEMENTS")

_T7 = "redisson-tomcat/redisson-tomcat-7/src/main/java/org/redisson/tomcat/"
_T8 = "redisson-tomcat/redisson-tomcat-8/src/main/java/org/redisson/tomcat/"
_T9 = "redisson-tomcat/redisson-tomcat-9/src/main/java/org/redisson/tomcat/"

W38A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# tomcat-8: remaining valves (javax.servlet; same API as tomcat-7).
for _name in ("UpdateValve.java", "UsageValve.java"):
    W38A_REPLACEMENTS[f"{_T8}{_name}"] = W37B[f"{_T7}{_name}"]

# tomcat-9: session cluster messages, JNDI manager, SSO (identical sources to tomcat-8).
for _name in (
    "AttributeMessage.java",
    "AttributeRemoveMessage.java",
    "AttributeUpdateMessage.java",
    "AttributesClearMessage.java",
    "AttributesPutAllMessage.java",
    "JndiRedissonSessionManager.java",
    "RedissonSingleSignOn.java",
    "SessionCreatedMessage.java",
    "SessionDestroyedMessage.java",
):
    W38A_REPLACEMENTS[f"{_T9}{_name}"] = W37B[f"{_T8}{_name}"]

# tomcat-9 valves: javax.servlet (same API as tomcat-7/8).
for _name in ("UpdateValve.java", "UsageValve.java"):
    W38A_REPLACEMENTS[f"{_T9}{_name}"] = W37B[f"{_T7}{_name}"]

# core redisson: slot callback + elements subscribe helpers.
W38A_REPLACEMENTS["BooleanSlotCallback.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集群分槽批量命令的 {@link SlotCallback} 实现：合并各槽 {@code Boolean} 结果为逻辑或。\n"
        " * <p>用于 {@link org.redisson.command.CommandAsyncService} 的 read/writeBatched 等 API，\n"
        " * 任一分片返回 {@code true} 则整体结果为 {@code true}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public BooleanSlotCallback() {",
        "    /** 使用默认 {@link SlotCallback#createParams} 构造命令参数。 */\n    public BooleanSlotCallback() {",
    ),
    (
        "    public BooleanSlotCallback(Object[] params) {",
        "    /** @param params 固定命令参数数组；非 null 时覆盖默认 createParams 行为 */\n    public BooleanSlotCallback(Object[] params) {",
    ),
    (
        "    @Override\n    public Boolean onResult(Collection<Boolean> res) {",
        "    /** 合并各槽结果：包含 {@code true} 则返回 {@code true}。 */\n    @Override\n    public Boolean onResult(Collection<Boolean> res) {",
    ),
    (
        "    @Override\n    public Object[] createParams(List<Object> params) {",
        "    /** 若构造时指定了固定 params 则直接返回，否则委托接口默认实现。 */\n    @Override\n    public Object[] createParams(List<Object> params) {",
    ),
]

W38A_REPLACEMENTS["ElementsSubscribeService.java"] = [
    (
        "/**\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * 阻塞队列等元素订阅的循环调度服务。\n"
        " * <p>由 {@link org.redisson.connection.ServiceManager} 持有，供 {@link RedissonBlockingQueue} 等\n"
        " * 反复调用异步取元素 API 并将结果交给消费者；失败时在非关闭状态下延迟重试。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public ElementsSubscribeService(ServiceManager serviceManager) {",
        "    /** @param serviceManager 提供超时调度与关闭状态检测 */\n    public ElementsSubscribeService(ServiceManager serviceManager) {",
    ),
    (
        "    public <V> int subscribeOnElements(Supplier<CompletionStage<V>> func, Function<V, CompletionStage<Void>> consumer) {",
        "    /**\n"
        "     * 注册异步元素订阅循环（推荐）。\n"
        "     * <p>{@code func} 持续发起取元素请求，{@code consumer} 处理每个元素并可返回后续 {@link CompletionStage}。\n"
        "     *\n"
        "     * @param func 异步取元素供应函数（如 {@code takeAsync}）\n"
        "     * @param consumer 元素处理函数，勿在其中调用阻塞 API\n"
        "     * @return 监听器 ID，用于 {@link #unsubscribe(int)}\n"
        "     */\n"
        "    public <V> int subscribeOnElements(Supplier<CompletionStage<V>> func, Function<V, CompletionStage<Void>> consumer) {",
    ),
    (
        "    @Deprecated\n    public <V> int subscribeOnElements(Supplier<CompletionStage<V>> func, Consumer<V> consumer) {",
        "    /** @deprecated 请改用 {@link #subscribeOnElements(Supplier, Function)} */\n    @Deprecated\n    public <V> int subscribeOnElements(Supplier<CompletionStage<V>> func, Consumer<V> consumer) {",
    ),
    (
        "    public void unsubscribe(int listenerId) {",
        "    /** 取消订阅并中断进行中的异步取元素循环。\n     * @param listenerId {@link #subscribeOnElements} 返回的 ID */\n    public void unsubscribe(int listenerId) {",
    ),
]
