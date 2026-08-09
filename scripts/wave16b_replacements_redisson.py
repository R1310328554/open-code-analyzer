"""Chinese annotation replacements for Redisson 4.7.0 wave-16b spring-data-23 [15:30]."""
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
W15A = _load("wave15a_replacements_redisson.py", "W15A_REPLACEMENTS")

W16B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

# spring-data-23: reuse wave-15a (sources identical to spring-data-22 for these files).
for _key in (
    "RedissonReactivePubSubCommands.java",
    "RedissonReactiveRedisConnection.java",
    "RedissonReactiveScriptingCommands.java",
    "RedissonReactiveServerCommands.java",
    "RedissonReactiveSetCommands.java",
    "RedissonReactiveSubscription.java",
    "RedissonSentinelConnection.java",
    "RedissonSubscription.java",
    "ScoredSortedListReplayDecoder.java",
    "ScoredSortedSetReplayDecoder.java",
    "ScoredSortedSetReplayDecoderV2.java",
    "SecondsConvertor.java",
    "SetReplayDecoder.java",
):
    W16B_REPLACEMENTS[_key] = W15A[_key]

# spring-data-24 BinaryConvertor identical to spring-data-22.
W16B_REPLACEMENTS["BinaryConvertor.java"] = W11A["BinaryConvertor.java"]

# spring-data-23: reactive cluster connection (extended cluster admin API).
W16B_REPLACEMENTS["RedissonReactiveRedisClusterConnection.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 集群模式响应式连接门面。\n"
        " * <p>继承 {@link RedissonReactiveRedisConnection} 并实现 {@link ReactiveRedisClusterConnection}；\n"
        "各 {@code *Commands()} 返回集群专用命令适配器，并封装 CLUSTER 拓扑/槽位管理命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public RedissonReactiveRedisClusterConnection(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    public RedissonReactiveRedisClusterConnection(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public ReactiveClusterKeyCommands keyCommands() {",
        "    /** 返回集群 Key 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterKeyCommands keyCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterStringCommands stringCommands() {",
        "    /** 返回集群 String 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterStringCommands stringCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterNumberCommands numberCommands() {",
        "    /** 返回集群数值命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterNumberCommands numberCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterListCommands listCommands() {",
        "    /** 返回集群 List 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterListCommands listCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterSetCommands setCommands() {",
        "    /** 返回集群 Set 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterSetCommands setCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterZSetCommands zSetCommands() {",
        "    /** 返回集群 ZSet 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterZSetCommands zSetCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterHashCommands hashCommands() {",
        "    /** 返回集群 Hash 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterHashCommands hashCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterGeoCommands geoCommands() {",
        "    /** 返回集群 Geo 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterGeoCommands geoCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterHyperLogLogCommands hyperLogLogCommands() {",
        "    /** 返回集群 HyperLogLog 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterHyperLogLogCommands hyperLogLogCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterServerCommands serverCommands() {",
        "    /** 返回集群 Server 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterServerCommands serverCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterStreamCommands streamCommands() {",
        "    /** 返回集群 Stream 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterStreamCommands streamCommands() {",
    ),
    (
        "    @Override\n    public Mono<String> ping(RedisClusterNode node) {",
        "    /** 对指定集群节点执行 PING。 */\n"
        "    @Override\n"
        "    public Mono<String> ping(RedisClusterNode node) {",
    ),
    (
        "    @Override\n    public Flux<RedisClusterNode> clusterGetNodes() {",
        "    /** CLUSTER NODES：获取集群全部节点拓扑。 */\n"
        "    @Override\n"
        "    public Flux<RedisClusterNode> clusterGetNodes() {",
    ),
    (
        "    @Override\n    public Flux<RedisClusterNode> clusterGetSlaves(RedisClusterNode redisClusterNode) {",
        "    /** 按主节点 host/port 查找其从节点列表。 */\n"
        "    @Override\n"
        "    public Flux<RedisClusterNode> clusterGetSlaves(RedisClusterNode redisClusterNode) {",
    ),
    (
        "    @Override\n    public Mono<Map<RedisClusterNode, Collection<RedisClusterNode>>> clusterGetMasterSlaveMap() {",
        "    /** 构建主节点到从节点集合的映射。 */\n"
        "    @Override\n"
        "    public Mono<Map<RedisClusterNode, Collection<RedisClusterNode>>> clusterGetMasterSlaveMap() {",
    ),
    (
        "    @Override\n    public Mono<Integer> clusterGetSlotForKey(ByteBuffer byteBuffer) {",
        "    /** KEYSLOT：计算 key 对应的哈希槽编号。 */\n"
        "    @Override\n"
        "    public Mono<Integer> clusterGetSlotForKey(ByteBuffer byteBuffer) {",
    ),
    (
        "    @Override\n    public Mono<RedisClusterNode> clusterGetNodeForSlot(int slot) {",
        "    /** 查找负责指定槽的主节点。 */\n"
        "    @Override\n"
        "    public Mono<RedisClusterNode> clusterGetNodeForSlot(int slot) {",
    ),
    (
        "    @Override\n    public Mono<RedisClusterNode> clusterGetNodeForKey(ByteBuffer byteBuffer) {",
        "    /** 按 key 计算槽位并返回负责该槽的主节点。 */\n"
        "    @Override\n"
        "    public Mono<RedisClusterNode> clusterGetNodeForKey(ByteBuffer byteBuffer) {",
    ),
    (
        "    @Override\n    public Mono<ClusterInfo> clusterGetClusterInfo() {",
        "    /** CLUSTER INFO：获取集群状态信息。 */\n"
        "    @Override\n"
        "    public Mono<ClusterInfo> clusterGetClusterInfo() {",
    ),
    (
        "    @Override\n    public Mono<Void> clusterAddSlots(RedisClusterNode redisClusterNode, int... ints) {",
        "    /** CLUSTER ADDSLOTS：向节点分配一个或多个槽。 */\n"
        "    @Override\n"
        "    public Mono<Void> clusterAddSlots(RedisClusterNode redisClusterNode, int... ints) {",
    ),
    (
        "    private List<Integer> convert(int... slots) {",
        "    /** 将槽位数组转为 Redis 命令参数列表。 */\n"
        "    private List<Integer> convert(int... slots) {",
    ),
    (
        "    @Override\n    public Mono<Void> clusterAddSlots(RedisClusterNode redisClusterNode, RedisClusterNode.SlotRange slotRange) {",
        "    /** CLUSTER ADDSLOTS：按 {@link SlotRange} 批量分配槽。 */\n"
        "    @Override\n"
        "    public Mono<Void> clusterAddSlots(RedisClusterNode redisClusterNode, RedisClusterNode.SlotRange slotRange) {",
    ),
    (
        "    @Override\n    public Mono<Long> clusterCountKeysInSlot(int slot) {",
        "    /** CLUSTER COUNTKEYSINSLOT：统计槽内 key 数量。 */\n"
        "    @Override\n"
        "    public Mono<Long> clusterCountKeysInSlot(int slot) {",
    ),
    (
        "    @Override\n    public Mono<Void> clusterDeleteSlots(RedisClusterNode redisClusterNode, int... ints) {",
        "    /** CLUSTER DELSLOTS：从节点移除一个或多个槽。 */\n"
        "    @Override\n"
        "    public Mono<Void> clusterDeleteSlots(RedisClusterNode redisClusterNode, int... ints) {",
    ),
    (
        "    @Override\n    public Mono<Void> clusterDeleteSlotsInRange(RedisClusterNode redisClusterNode, RedisClusterNode.SlotRange slotRange) {",
        "    /** CLUSTER DELSLOTS：按 {@link SlotRange} 批量移除槽。 */\n"
        "    @Override\n"
        "    public Mono<Void> clusterDeleteSlotsInRange(RedisClusterNode redisClusterNode, RedisClusterNode.SlotRange slotRange) {",
    ),
    (
        "    @Override\n    public Mono<Void> clusterForget(RedisClusterNode redisClusterNode) {",
        "    /** CLUSTER FORGET：从集群视图中移除指定节点。 */\n"
        "    @Override\n"
        "    public Mono<Void> clusterForget(RedisClusterNode redisClusterNode) {",
    ),
    (
        "    @Override\n    public Mono<Void> clusterMeet(RedisClusterNode redisClusterNode) {",
        "    /** CLUSTER MEET：将新节点加入集群。 */\n"
        "    @Override\n"
        "    public Mono<Void> clusterMeet(RedisClusterNode redisClusterNode) {",
    ),
    (
        "    @Override\n    public Mono<Void> clusterSetSlot(RedisClusterNode redisClusterNode, int slot, AddSlots addSlots) {",
        "    /** CLUSTER SETSLOT：设置槽的导入/迁移/稳定状态。 */\n"
        "    @Override\n"
        "    public Mono<Void> clusterSetSlot(RedisClusterNode redisClusterNode, int slot, AddSlots addSlots) {",
    ),
    (
        "    private static final RedisStrictCommand<List<String>> CLUSTER_GETKEYSINSLOT = new RedisStrictCommand<List<String>>(\"CLUSTER\", \"GETKEYSINSLOT\", new ObjectListReplayDecoder<String>());",
        "    /** CLUSTER GETKEYSINSLOT 命令定义。 */\n"
        "    private static final RedisStrictCommand<List<String>> CLUSTER_GETKEYSINSLOT = new RedisStrictCommand<List<String>>(\"CLUSTER\", \"GETKEYSINSLOT\", new ObjectListReplayDecoder<String>());",
    ),
    (
        "    @Override\n    public Flux<ByteBuffer> clusterGetKeysInSlot(int slot, int count) {",
        "    /** CLUSTER GETKEYSINSLOT：返回槽内最多 count 个 key。 */\n"
        "    @Override\n"
        "    public Flux<ByteBuffer> clusterGetKeysInSlot(int slot, int count) {",
    ),
    (
        "    @Override\n    public Mono<Void> clusterReplicate(RedisClusterNode redisClusterNode, RedisClusterNode slave) {",
        "    /** CLUSTER REPLICATE：将节点配置为指定主节点的从节点。 */\n"
        "    @Override\n"
        "    public Mono<Void> clusterReplicate(RedisClusterNode redisClusterNode, RedisClusterNode slave) {",
    ),
]
