#!/usr/bin/env python3
"""Generate wave53a_replacements_redisson.py — Spring Data connection mega batch."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave53a_replacements_redisson.py"
BATCH = Path("/tmp/re53a.txt")

# --- RedisConnection / cluster / reactive method descriptions ---
METHOD_CN: dict[str, str] = {
    # lifecycle & pipeline
    "close": "关闭连接；若处于 MULTI/管道模式则先 discard。",
    "isClosed": "返回连接是否已关闭。",
    "getNativeConnection": "返回底层 {@link Redisson} 客户端实例。",
    "isQueueing": "是否处于 MULTI 事务排队（REDIS_WRITE_ATOMIC 批处理）模式。",
    "isPipelined": "是否处于管道（IN_MEMORY）批处理模式。",
    "isPipelinedAtomic": "是否处于原子管道（IN_MEMORY_ATOMIC）模式。",
    "openPipeline": "开启管道批处理。",
    "closePipeline": "执行管道并返回各命令结果列表。",
    "multi": "开启 MULTI 事务。",
    "exec": "执行 MULTI 队列中的命令。",
    "discard": "放弃 MULTI 队列中的命令。",
    "watch": "监视给定 key，用于乐观事务。",
    "unwatch": "取消对所有 key 的监视。",
    "resetConnection": "重置连接状态（退出事务/管道）。",
    "execute": "执行原始 Redis 命令或集群节点命令。",
    "sync": "同步等待异步命令完成。",
    "transform": "将 Redisson 异步结果转为 Spring Data 期望类型。",
    "convert": "节点地址/host:port 字符串转换工具。",
    "toCommand": "将 Spring Data 命令对象映射为 Redisson 协议命令。",
    "indexCommand": "按索引从批处理结果中取命令响应。",
    "filterResults": "过滤管道/MULTI 批处理结果。",
    "checkSubscription": "校验 Pub/Sub 订阅状态。",
    # key commands
    "exists": "判断 key 是否存在。",
    "del": "删除一个或多个 key。",
    "unlink": "异步删除 key（UNLINK）。",
    "type": "返回 key 的 {@link DataType}。",
    "keys": "按模式匹配返回 key 集合（慎用）。",
    "scan": "增量 SCAN 迭代 key。",
    "randomKey": "随机返回一个 key。",
    "rename": "重命名 key。",
    "renameNX": "仅当新 key 不存在时重命名。",
    "move": "将 key 移动到另一数据库。",
    "migrate": "跨节点迁移 key。",
    "touch": "更新 key 最后访问时间。",
    "dump": "序列化 key 值（DUMP）。",
    "restore": "从 DUMP 数据恢复 key。",
    "sort": "对列表/set 排序或 store 结果。",
    # expire
    "expire": "设置 key 过期时间（秒）。",
    "expireAt": "设置 key 过期 Unix 时间戳（秒）。",
    "pExpire": "设置 key 过期时间（毫秒）。",
    "pExpireAt": "设置 key 过期 Unix 时间戳（毫秒）。",
    "persist": "移除 key 的过期时间。",
    "ttl": "返回 key 剩余生存时间（秒）。",
    "pTtl": "返回 key 剩余生存时间（毫秒）。",
    "idletime": "返回 key 空闲时间（OBJECT IDLETIME）。",
    "refcount": "返回 key 引用计数（OBJECT REFCOUNT）。",
    "encodingOf": "返回 key 内部编码（OBJECT ENCODING）。",
    # string
    "get": "获取字符串值。",
    "getSet": "设置新值并返回旧值。",
    "set": "设置字符串值。",
    "setNX": "仅当 key 不存在时设置。",
    "setEx": "设置带秒级 TTL 的字符串。",
    "pSetEx": "设置带毫秒级 TTL 的字符串。",
    "mGet": "批量 GET。",
    "mSet": "批量 SET。",
    "mSetNX": "批量 SET NX。",
    "append": "追加字符串。",
    "getRange": "获取子串（GETRANGE）。",
    "setRange": "覆盖子串（SETRANGE）。",
    "strLen": "返回字符串长度。",
    "getBit": "获取位值。",
    "setBit": "设置位值。",
    "bitCount": "统计位为 1 的数量。",
    "bitOp": "对多个 key 执行位运算。",
    "bitPos": "查找第一个指定位值的位置。",
    "bitField": "执行 BITFIELD 子命令。",
    "incr": "字符串值自增 1。",
    "incrBy": "字符串值按增量自增。",
    "decr": "字符串值自减 1。",
    "decrBy": "字符串值按增量自减。",
    # hash
    "hSet": "设置 hash 字段。",
    "hSetNX": "仅当字段不存在时设置 hash 字段。",
    "hGet": "获取 hash 字段值。",
    "hMSet": "批量设置 hash 字段。",
    "hMGet": "批量获取 hash 字段值。",
    "hExists": "判断 hash 字段是否存在。",
    "hDel": "删除 hash 字段。",
    "hLen": "返回 hash 字段数量。",
    "hKeys": "返回 hash 全部字段名。",
    "hVals": "返回 hash 全部字段值。",
    "hGetAll": "返回 hash 全部字段映射。",
    "hIncrBy": "hash 字段按整数增量自增。",
    "hStrLen": "返回 hash 字段值长度。",
    "hScan": "增量 SCAN hash 字段。",
    # list
    "lPush": "从列表左侧入队。",
    "lPushX": "仅当列表存在时从左侧入队。",
    "rPush": "从列表右侧入队。",
    "rPushX": "仅当列表存在时从右侧入队。",
    "lPop": "从列表左侧出队。",
    "rPop": "从列表右侧出队。",
    "bLPop": "阻塞式左侧出队。",
    "bRPop": "阻塞式右侧出队。",
    "rPopLPush": "从源列表右侧弹出并推入目标列表左侧。",
    "bRPopLPush": "阻塞式 RPOPLPUSH。",
    "lLen": "返回列表长度。",
    "lRange": "返回列表指定区间元素。",
    "lIndex": "按索引获取列表元素。",
    "lSet": "按索引设置列表元素。",
    "lInsert": "在 pivot 前/后插入元素。",
    "lRem": "删除列表中等于 value 的元素。",
    "lTrim": "保留列表指定区间，其余删除。",
    # set
    "sAdd": "向集合添加成员。",
    "sRem": "从集合移除成员。",
    "sPop": "随机弹出集合成员。",
    "sMembers": "返回集合全部成员。",
    "sIsMember": "判断元素是否为集合成员。",
    "sMove": "将成员从源集合移动到目标集合。",
    "sCard": "返回集合基数。",
    "sRandMember": "随机返回集合成员。",
    "sScan": "增量 SCAN 集合成员。",
    "sDiff": "返回集合差集。",
    "sDiffStore": "计算差集并存储。",
    "sInter": "返回集合交集。",
    "sInterStore": "计算交集并存储。",
    "sUnion": "返回集合并集。",
    "sUnionStore": "计算并集并存储。",
    # zset
    "zAdd": "向有序集合添加成员及分数。",
    "zRem": "从有序集合移除成员。",
    "zIncrBy": "有序集合成员分数增量更新。",
    "zRank": "返回成员升序排名。",
    "zRevRank": "返回成员降序排名。",
    "zRange": "按排名区间返回成员。",
    "zRevRange": "按排名区间降序返回成员。",
    "zRangeWithScores": "按排名区间返回成员及分数。",
    "zRevRangeWithScores": "按排名区间降序返回成员及分数。",
    "zRangeByScore": "按分数区间返回成员。",
    "zRevRangeByScore": "按分数区间降序返回成员。",
    "zRangeByScoreWithScores": "按分数区间返回成员及分数。",
    "zRevRangeByScoreWithScores": "按分数区间降序返回成员及分数。",
    "zRangeByLex": "按字典序区间返回成员。",
    "zCount": "统计分数区间内成员数。",
    "zCard": "返回有序集合基数。",
    "zScore": "返回成员分数。",
    "zRemRange": "按排名区间删除成员。",
    "zRemRangeByScore": "按分数区间删除成员。",
    "zRemRangeByRank": "按排名区间删除成员（响应式）。",
    "zInterStore": "计算有序集合交集并存储。",
    "zUnionStore": "计算有序集合并集并存储。",
    "zScan": "增量 SCAN 有序集合成员。",
    # hyperloglog
    "pfAdd": "向 HyperLogLog 添加元素。",
    "pfCount": "返回 HyperLogLog 基数估计。",
    "pfMerge": "合并多个 HyperLogLog。",
    # geo
    "geoAdd": "添加 GEO 位置。",
    "geoDist": "计算两点距离。",
    "geoHash": "返回 GEO 成员 geohash。",
    "geoPos": "返回 GEO 成员坐标。",
    "geoRadius": "按圆心半径查询附近成员。",
    "geoRadiusByMember": "以成员为圆心查询附近成员。",
    "geoRemove": "从 GEO 移除成员。",
    # pubsub
    "publish": "向频道发布消息。",
    "getSubscription": "返回 Pub/Sub 订阅句柄。",
    "isSubscribed": "是否处于订阅模式。",
    "subscribe": "订阅频道。",
    "pSubscribe": "按模式订阅频道。",
    # server / connection mgmt
    "echo": "ECHO 测试。",
    "ping": "PING 测试连通性。",
    "select": "切换数据库索引。",
    "flushDb": "清空当前数据库。",
    "flushAll": "清空全部数据库。",
    "dbSize": "返回当前库 key 数量。",
    "info": "返回 INFO 信息。",
    "lastSave": "返回上次 RDB 持久化时间。",
    "save": "同步 SAVE。",
    "bgSave": "后台 BGSAVE。",
    "bgReWriteAof": "后台 AOF 重写。",
    "bgWriteAof": "触发 AOF 写入。",
    "shutdown": "关闭 Redis 服务。",
    "time": "返回服务器时间。",
    "killClient": "终止客户端连接。",
    "getClientList": "返回 CLIENT LIST 信息。",
    "getClientName": "获取当前连接客户端名。",
    "setClientName": "设置当前连接客户端名。",
    "getConfig": "读取 CONFIG GET。",
    "setConfig": "写入 CONFIG SET。",
    "resetConfigStats": "重置 CONFIG 统计。",
    "slaveOf": "配置从节点复制主节点。",
    "slaveOfNoOne": "将从节点提升为独立主节点。",
    # scripting
    "eval": "执行 Lua 脚本。",
    "evalSha": "按 SHA1 执行已加载 Lua 脚本。",
    "scriptLoad": "加载 Lua 脚本并返回 SHA1。",
    "scriptExists": "检查脚本 SHA1 是否已加载。",
    "scriptFlush": "清空脚本缓存。",
    "scriptKill": "终止正在执行的脚本。",
    # cluster
    "clusterGetNodes": "返回 CLUSTER NODES 解析结果。",
    "clusterGetSlaves": "返回指定 master 的 slave 节点。",
    "clusterGetMasterSlaveMap": "返回 master 到 slaves 的映射。",
    "clusterGetClusterInfo": "返回 CLUSTER INFO 解析结果。",
    "clusterGetNodeForKey": "返回 key 所在槽位对应节点。",
    "clusterGetNodeForSlot": "返回槽位对应节点。",
    "clusterGetSlotForKey": "返回 key 的 CRC16 槽位。",
    "clusterGetKeysInSlot": "返回槽位内 key 列表。",
    "clusterCountKeysInSlot": "返回槽位内 key 数量。",
    "clusterAddSlots": "为当前节点分配槽位。",
    "clusterDeleteSlots": "删除当前节点槽位。",
    "clusterDeleteSlotsInRange": "删除槽位区间。",
    "clusterMeet": "CLUSTER MEET 加入节点。",
    "clusterForget": "CLUSTER FORGET 移除节点。",
    "clusterReplicate": "配置节点复制指定 master。",
    "clusterSetSlot": "设置槽位 IMPORTING/MIGRATING/STABLE/...",
    "getEntry": "根据 {@link RedisClusterNode} 解析底层 {@link MasterSlaveEntry}。",
    "serverCommands": "返回集群服务端命令适配器。",
    "doScan": "集群模式 SCAN 实现。",
    "getKey": "从命令参数中提取 key 字节数组。",
    "getAbbreviation": "返回命令缩写名。",
    "value": "从响应对象提取值。",
    "streamCommands": "返回 Stream 命令适配器。",
    # reactive zset/string
    "scanIterator": "将 SCAN 结果包装为响应式迭代器。",
    "pSetEX": "响应式 SET key 带毫秒 TTL。",
    "setEX": "响应式 SET key 带秒级 TTL。",
    # auto configuration
    "redisTemplate": "注册默认 {@link RedisTemplate}（若未自定义）。",
    "stringRedisTemplate": "注册默认 {@link StringRedisTemplate}。",
    "redissonConnectionFactory": "将 {@link RedissonClient} 包装为 {@link RedisConnectionFactory}。",
    "redissonReactive": "暴露 {@link RedissonReactiveClient} Bean。",
    "redissonRxJava": "暴露 {@link RedissonRxClient} Bean。",
    "redisson": "创建 {@link RedissonClient}：优先 YAML/文件，否则从 Spring Redis 属性推导。",
    "hasConnectionDetails": "检测 classpath 是否存在 Spring Boot 3 {@link RedisConnectionDetails}。",
    "initSSL": "若配置了 SSL bundle，注入信任库与密钥库到 {@link Config}。",
    "getPrefix": "根据 SSL 配置返回 redis:// 或 rediss:// 地址前缀。",
    "convertNodes": "通过 MethodHandle 读取 host/port 并拼接节点地址。",
    "getConfigStream": "从 Spring {@link Resource} 加载 Redisson 配置文件流。",
}

CLASS_JDOC: dict[str, str] = {
    "RedissonAutoConfiguration": (
        "Spring Boot 2.6 及以下版本的 Redisson 自动配置。\n"
        " * <p>注册 {@link RedisTemplate}、{@link RedissonConnectionFactory} 与\n"
        " * {@link RedissonClient}；从 {@link RedisProperties} 或 {@link RedissonProperties}\n"
        " * 推导单机/哨兵/集群 {@link Config}，并支持 {@link RedissonAutoConfigurationCustomizer} 回调。"
    ),
    "RedissonConnection": (
        "基于 Redisson 的 Spring Data Redis {@link RedisConnection} 实现。\n"
        " * <p>将 Spring Data 键值/集合/事务/管道 API 映射为 Redisson 异步命令；\n"
        " * 支持 MULTI/管道批处理、Pub/Sub 订阅与 Lua 脚本执行。"
    ),
    "RedissonClusterConnection": (
        "Redisson 集群模式 {@link RedisClusterConnection} 实现。\n"
        " * <p>扩展 {@link RedissonConnection}，提供 CLUSTER 管理命令、\n"
        " * 槽位/key 路由及按节点执行服务端命令的能力。"
    ),
    "RedissonReactiveZSetCommands": (
        "Spring Data Redis 响应式有序集合（ZSet）命令实现。\n"
        " * <p>继承 {@link RedissonBaseReactive}，将 {@link ReactiveZSetCommands}\n"
        " * 命令流转换为 Redisson 响应式写/读调用。"
    ),
    "RedissonReactiveStringCommands": (
        "Spring Data Redis 响应式字符串命令实现。\n"
        " * <p>继承 {@link RedissonBaseReactive}，封装 GET/SET/MGET 及位操作等\n"
        " * {@link ReactiveStringCommands} 的 Reactor 适配。"
    ),
}

FIELD_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "    private boolean closed;\n    protected final Redisson redisson;",
        "    /** 连接是否已关闭。 */\n"
        "    private boolean closed;\n"
        "    /** 底层 Redisson 客户端。 */\n"
        "    protected final Redisson redisson;",
    ),
    (
        "    CommandAsyncExecutor executorService;\n    private RedissonSubscription subscription;",
        "    /** 异步命令执行器（事务/管道时切换为 {@link CommandBatchService}）。 */\n"
        "    CommandAsyncExecutor executorService;\n"
        "    /** Pub/Sub 订阅管理器。 */\n"
        "    private RedissonSubscription subscription;",
    ),
    (
        "    @Autowired(required = false)\n    private List<RedissonAutoConfigurationCustomizer> redissonAutoConfigurationCustomizers;",
        "    /** 可选的 Redisson 配置定制器列表。 */\n"
        "    @Autowired(required = false)\n"
        "    private List<RedissonAutoConfigurationCustomizer> redissonAutoConfigurationCustomizers;",
    ),
    (
        "    @Autowired\n    private RedissonProperties redissonProperties;",
        "    /** {@code spring.redis.redisson.*} 扩展属性。 */\n"
        "    @Autowired\n    private RedissonProperties redissonProperties;",
    ),
    (
        "    @Autowired\n    private RedisProperties redisProperties;",
        "    /** Spring Boot 标准 {@code spring.redis.*} 属性。 */\n"
        "    @Autowired\n    private RedisProperties redisProperties;",
    ),
    (
        "    @Autowired\n    private ApplicationContext ctx;",
        "    /** Spring 应用上下文，用于加载配置 Resource 与 SSL bundle。 */\n"
        "    @Autowired\n    private ApplicationContext ctx;",
    ),
    (
        "    private static final RedisCommand<Double> ZADD_FLOAT = new RedisCommand<>(\"ZADD\", new DoubleNullSafeReplayConvertor());",
        "    /** ZADD INCR 模式使用的浮点结果解码命令。 */\n"
        "    private static final RedisCommand<Double> ZADD_FLOAT = new RedisCommand<>(\"ZADD\", new DoubleNullSafeReplayConvertor());",
    ),
    (
        "    RedissonReactiveZSetCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入 Redisson 响应式命令执行器。 */\n"
        "    RedissonReactiveZSetCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    RedissonReactiveStringCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入 Redisson 响应式命令执行器。 */\n"
        "    RedissonReactiveStringCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    public RedissonClusterConnection(RedissonClient redisson) {",
        "    /** 绑定 Redisson 客户端并初始化集群连接。 */\n"
        "    public RedissonClusterConnection(RedissonClient redisson) {",
    ),
    (
        "    public RedissonConnection(RedissonClient redisson) {",
        "    /** 绑定 Redisson 客户端并获取默认命令执行器。 */\n"
        "    public RedissonConnection(RedissonClient redisson) {",
    ),
]

AUTO_CONFIG_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "/**\n * Spring configuration used with Spring Boot 2.6 and lower\n *\n * @author Nikita Koksharov\n * @author Nikos Kakavas (https://github.com/nikakis)\n * @author AnJia (https://anjia0532.github.io/)\n *\n */",
        "/**\n * Spring Boot 2.6 及以下版本的 Redisson 自动配置。\n * <p>注册 {@link RedisTemplate}、{@link RedissonConnectionFactory} 与\n * {@link RedissonClient}；从 {@link RedisProperties} 或 {@link RedissonProperties}\n * 推导单机/哨兵/集群 {@link Config}，并支持 {@link RedissonAutoConfigurationCustomizer} 回调。\n *\n * @author Nikita Koksharov\n * @author Nikos Kakavas (https://github.com/nikakis)\n * @author AnJia (https://anjia0532.github.io/)\n *\n */",
    ),
    (
        "    @Bean\n    @ConditionalOnMissingBean(name = \"redisTemplate\")\n    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {",
        "    /** 注册默认 {@link RedisTemplate}（若应用未自定义）。 */\n"
        "    @Bean\n    @ConditionalOnMissingBean(name = \"redisTemplate\")\n    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {",
    ),
    (
        "    @Bean\n    @ConditionalOnMissingBean(StringRedisTemplate.class)\n    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {",
        "    /** 注册默认 {@link StringRedisTemplate}。 */\n"
        "    @Bean\n    @ConditionalOnMissingBean(StringRedisTemplate.class)\n    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {",
    ),
    (
        "    @Bean\n    @ConditionalOnMissingBean(RedisConnectionFactory.class)\n    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {",
        "    /** 将 {@link RedissonClient} 包装为 Spring Data {@link RedisConnectionFactory}。 */\n"
        "    @Bean\n    @ConditionalOnMissingBean(RedisConnectionFactory.class)\n    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {",
    ),
    (
        "    @Bean\n    @Lazy\n    @ConditionalOnMissingBean(RedissonReactiveClient.class)\n    public RedissonReactiveClient redissonReactive(RedissonClient redisson) {",
        "    /** 暴露 {@link RedissonReactiveClient}（懒加载）。 */\n"
        "    @Bean\n    @Lazy\n    @ConditionalOnMissingBean(RedissonReactiveClient.class)\n    public RedissonReactiveClient redissonReactive(RedissonClient redisson) {",
    ),
    (
        "    @Bean\n    @Lazy\n    @ConditionalOnMissingBean(RedissonRxClient.class)\n    public RedissonRxClient redissonRxJava(RedissonClient redisson) {",
        "    /** 暴露 {@link RedissonRxClient}（懒加载）。 */\n"
        "    @Bean\n    @Lazy\n    @ConditionalOnMissingBean(RedissonRxClient.class)\n    public RedissonRxClient redissonRxJava(RedissonClient redisson) {",
    ),
    (
        "    @SuppressWarnings(\"MethodLength\")\n    @Bean(destroyMethod = \"shutdown\")\n    @ConditionalOnMissingBean(RedissonClient.class)\n    public RedissonClient redisson() throws IOException {",
        "    /**\n     * 创建 {@link RedissonClient}：优先 YAML/文件配置，否则从 Spring Redis 属性推导。\n     * <p>支持单机、哨兵、集群；创建前应用全部 {@link RedissonAutoConfigurationCustomizer}。\n     */\n"
        "    @SuppressWarnings(\"MethodLength\")\n    @Bean(destroyMethod = \"shutdown\")\n    @ConditionalOnMissingBean(RedissonClient.class)\n    public RedissonClient redisson() throws IOException {",
    ),
    (
        "        } else if (redisProperties.getSentinel() != null || isSentinel) {",
        "        // 哨兵模式：从 RedisProperties 或 RedisConnectionDetails 构建 SentinelServersConfig。\n"
        "        } else if (redisProperties.getSentinel() != null || isSentinel) {",
    ),
    (
        "        } else if ((clusterMethod != null && ReflectionUtils.invokeMethod(clusterMethod, redisProperties) != null)\n                    || isCluster) {",
        "        // 集群模式：解析节点列表并构建 ClusterServersConfig。\n"
        "        } else if ((clusterMethod != null && ReflectionUtils.invokeMethod(clusterMethod, redisProperties) != null)\n                    || isCluster) {",
    ),
    (
        "        } else {\n            config = new Config()",
        "        // 单机模式：使用 host:port 或 RedisConnectionDetails.standalone。\n"
        "        } else {\n            config = new Config()",
    ),
    (
        "        if (redissonAutoConfigurationCustomizers != null) {",
        "        // 依次应用用户注册的 RedissonAutoConfigurationCustomizer。\n"
        "        if (redissonAutoConfigurationCustomizers != null) {",
    ),
    (
        "    private void initSSL(Config config) {",
        "    /** 若配置了 SSL bundle，则注入信任库与密钥库到 {@link Config}。 */\n"
        "    private void initSSL(Config config) {",
    ),
    (
        "    private String getPrefix() {",
        "    /** 根据 SSL 配置返回 {@code redis://} 或 {@code rediss://} 地址前缀。 */\n"
        "    private String getPrefix() {",
    ),
    (
        "    @SuppressWarnings(\"IllegalCatch\")\n    private String[] convertNodes(String prefix, List<?> nodesObject) {",
        "    /** 通过 MethodHandle 读取 host/port（兼容 JDK 8 record 编译产物）。 */\n"
        "    @SuppressWarnings(\"IllegalCatch\")\n    private String[] convertNodes(String prefix, List<?> nodesObject) {",
    ),
    (
        "    private String[] convert(String prefix, List<String> nodesObject) {",
        "    /** 为节点地址补全协议前缀（若尚未包含）。 */\n"
        "    private String[] convert(String prefix, List<String> nodesObject) {",
    ),
    (
        "    private InputStream getConfigStream() throws IOException {",
        "    /** 从 Spring {@link Resource} 加载 Redisson 配置文件流。 */\n"
        "    private InputStream getConfigStream() throws IOException {",
    ),
    (
        "    private boolean hasConnectionDetails() {",
        "    /** 检测 classpath 是否存在 Spring Boot 3 {@link RedisConnectionDetails}。 */\n"
        "    private boolean hasConnectionDetails() {",
    ),
]

CLUSTER_INLINE: list[tuple[str, str]] = [
    (
        "        if (masterNode == null) {",
        "        // 未在 CLUSTER NODES 结果中找到匹配的 master 节点。\n"
        "        if (masterNode == null) {",
    ),
    (
        "        if (node == null) {",
        "        // 节点不存在时抛出 InvalidDataAccessResourceUsageException。\n"
        "        if (node == null) {",
    ),
    (
        "//    @Override\n//    public String ping(RedisClusterNode node) {",
        "//    集群节点 PING 暂未启用（历史注释保留）。\n//    @Override\n//    public String ping(RedisClusterNode node) {",
    ),
]

EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"
CONN_CLASS_JDOC = "/**\n * Redisson connection\n * \n * @author Nikita Koksharov\n *\n */"


def replace_class_javadoc(text: str, class_name: str) -> str:
    desc = CLASS_JDOC.get(class_name)
    if not desc:
        return text
    new_block = f"/**\n * {desc}\n *\n * @author Nikita Koksharov\n *\n */"
    if EMPTY_JDOC in text:
        return text.replace(EMPTY_JDOC, new_block, 1)
    if CONN_CLASS_JDOC in text:
        return text.replace(CONN_CLASS_JDOC, new_block, 1)
    return text


def add_override_javadocs(text: str) -> str:
    pattern = re.compile(
        r"(?P<indent>^[ \t]*)(@Override\s*\n\s*)"
        r"(?P<sig>(?:public|protected)\s+[\w<>,\?\[\]\s]+\s+(?P<name>\w+)\s*\([^;]*\)\s*(?:throws[^{]+)?\{)",
        re.MULTILINE,
    )

    def repl(m: re.Match[str]) -> str:
        start = m.start()
        window = text[max(0, start - 500) : start]
        if re.search(r"/\*\*[\s\S]*?\*/\s*$", window):
            return m.group(0)
        name = m.group("name")
        desc = METHOD_CN.get(name, f"实现 Spring Data Redis 的 {name} 操作。")
        indent = m.group("indent")
        return f"{indent}/** {desc} */\n{indent}{m.group(2)}{m.group('sig')}"

    return pattern.sub(repl, text)


def add_private_method_javadocs(text: str, class_name: str) -> str:
    if class_name != "RedissonAutoConfiguration":
        return text
    priv_pattern = re.compile(
        r"(?P<indent>^[ \t]*)((?:private|protected)\s+[\w<>,\?\[\]\s]+\s+(?P<name>\w+)\s*\([^;]*\)\s*(?:throws[^{]+)?\{)",
        re.MULTILINE,
    )

    def repl(m: re.Match[str]) -> str:
        start = m.start()
        window = text[max(0, start - 300) : start]
        if re.search(r"/\*\*[\s\S]*?\*/\s*$", window) or "@Bean" in window[-120:]:
            return m.group(0)
        name = m.group("name")
        desc = METHOD_CN.get(name)
        if not desc:
            return m.group(0)
        indent = m.group("indent")
        return f"{indent}/** {desc} */\n{indent}{m.group(2)}"

    return priv_pattern.sub(repl, text)


def apply_pairs(text: str, pairs: list[tuple[str, str]]) -> str:
    for old, new in pairs:
        if old in text:
            text = text.replace(old, new, 1)
    return text


def annotate_text(rel: str, text: str) -> str:
    class_name = Path(rel).stem

    if class_name == "RedissonAutoConfiguration":
        text = apply_pairs(text, AUTO_CONFIG_REPLACEMENTS)
    else:
        text = replace_class_javadoc(text, class_name)

    text = apply_pairs(text, FIELD_REPLACEMENTS)

    if "RedissonClusterConnection" in class_name:
        text = apply_pairs(text, CLUSTER_INLINE)

    text = add_override_javadocs(text)
    text = add_private_method_javadocs(text, class_name)

    # Section marker for large connection class
    if class_name == "RedissonConnection" and "// --- Redis key 命令 ---" not in text:
        text = text.replace(
            "    @Override\n    public Boolean exists(byte[] key) {",
            "    // --- Redis key 命令 ---\n\n"
            "    @Override\n    public Boolean exists(byte[] key) {",
            1,
        )

    return text


def build_replacements() -> dict[str, list[tuple[str, str]]]:
    reps: dict[str, list[tuple[str, str]]] = {}
    for rel in BATCH.read_text(encoding="utf-8").splitlines():
        rel = rel.strip()
        if not rel:
            continue
        orig = (ORIG / rel).read_text(encoding="utf-8")
        annotated = annotate_text(rel, orig)
        if orig == annotated:
            raise ValueError(f"no changes for {rel}")
        cn = len(re.findall(r"[\u4e00-\u9fff]", annotated))
        if cn < 10:
            raise ValueError(f"insufficient CJK cn={cn} for {rel}")
        name = Path(rel).name
        reps[rel] = [(orig, annotated)]
        if name != rel:
            reps[name] = [(orig, annotated)]
        print(f"OK {rel} cjk={cn}")
    return reps


def write_replacements_file(reps: dict[str, list[tuple[str, str]]]) -> None:
    # dedupe by rel path only for output keys
    seen: set[str] = set()
    lines = [
        '"""Chinese annotation replacements for Redisson 4.7.0 wave-53a spring-data [0:15]."""',
        "from __future__ import annotations",
        "",
        "W53A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
    ]
    for rel in BATCH.read_text(encoding="utf-8").splitlines():
        rel = rel.strip()
        if not rel or rel in seen:
            continue
        seen.add(rel)
        pairs = reps[rel]
        short = Path(rel).name
        lines.append(f"    {rel!r}: [")
        for old, new in pairs:
            lines.append(f"        ({old!r},")
            lines.append(f"         {new!r}),")
        lines.append("    ],")
        if short != rel:
            lines.append(f"    {short!r}: [")
            for old, new in pairs:
                lines.append(f"        ({old!r},")
                lines.append(f"         {new!r}),")
            lines.append("    ],")
    lines.append("}")
    lines.append("")
    OUT.write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    reps = build_replacements()
    write_replacements_file(reps)
    print(f"Wrote {OUT} ({len(reps)} entries)")


if __name__ == "__main__":
    main()
