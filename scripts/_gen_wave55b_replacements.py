#!/usr/bin/env python3
"""Generate wave55b_replacements_redisson.py for spring-data-34/35/40 connection classes."""
from __future__ import annotations

import importlib.util
import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave55b_replacements_redisson.py"
SCRIPTS = ROOT / "scripts"
FILES = [
    ln.strip()
    for ln in Path("/tmp/re55b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"
_EMPTY_JDOC2 = "/**\n *\n * @author Nikita Koksharov\n *\n */"
_CONN_JDOC = "/**\n * Redisson connection\n * \n * @author Nikita Koksharov\n *\n */"

CLASS_JDOC: dict[str, str] = {
    "RedissonConnection": (
        "/**\n"
        " * Spring Data Redis {@link RedisConnection} 的 Redisson 同步实现。\n"
        " * <p>封装 String/Hash/List/Set/ZSet/Geo/Stream 等命令，\n"
        "支持管道、事务、Pub/Sub 与 Lua 脚本执行。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */"
    ),
    "RedissonClusterConnection": (
        "/**\n"
        " * Spring Data Redis 集群模式同步连接。\n"
        " * <p>继承 {@link RedissonConnection} 并实现 {@link RedisClusterConnection}；\n"
        "封装 CLUSTER 拓扑查询、槽位分配与节点管理命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */"
    ),
    "RedissonStreamCommands": (
        "/**\n"
        " * Spring Data Redis Stream 命令同步实现。\n"
        " * <p>封装 XADD/XACK/XDEL、XRANGE/XREVRANGE、XREAD/XREADGROUP、\n"
        "XGROUP 消费者组管理及 XTRIM 等 Redis Stream 操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */"
    ),
    "RedissonReactiveStreamCommands": (
        "/**\n"
        " * Spring Data Redis 响应式 Stream 命令实现。\n"
        " * <p>封装 XADD/XACK/XDEL、XRANGE/XREVRANGE、XREAD/XREADGROUP、\n"
        "XCLAIM/XPENDING、XINFO 及 XGROUP 消费者组管理。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */"
    ),
    "RedissonReactiveZSetCommands": (
        "/**\n"
        " * Spring Data Redis 响应式 ZSet 命令实现。\n"
        " * <p>封装 ZADD/ZREM、ZRANGE/ZREVRANGE、ZSCAN、ZUNIONSTORE/ZINTERSTORE\n"
        "等有序集合操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */"
    ),
    "RedissonReactiveStringCommands": (
        "/**\n"
        " * Spring Data Redis 响应式 String 命令实现。\n"
        " * <p>封装 SET/GET、MGET/MSET、APPEND、GETRANGE、BITCOUNT/BITOP 等字符串与位图操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */"
    ),
    "RedissonReactiveKeyCommands": (
        "/**\n"
        " * Spring Data Redis 响应式 Key 命令实现。\n"
        " * <p>封装 EXISTS、TYPE、KEYS、RENAME、DEL、EXPIRE、TTL、MOVE 等通用 key 操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */"
    ),
    "RedissonReactiveHashCommands": (
        "/**\n"
        " * Spring Data Redis 响应式 Hash 命令实现。\n"
        " * <p>封装 HSET/HMSET、HMGET、HEXISTS、HDEL、HLEN、HKEYS、HVALS、HGETALL 等命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */"
    ),
}

METHOD_CN: dict[str, str] = {
    "close": "关闭连接并释放 Redisson 资源。",
    "isClosed": "返回连接是否已关闭。",
    "getNativeConnection": "返回底层 Redisson 原生连接对象。",
    "isQueueing": "是否处于 MULTI 事务排队模式。",
    "isPipelined": "是否已开启管道模式。",
    "isPipelinedAtomic": "管道是否以原子方式执行。",
    "openPipeline": "开启管道，后续命令批量发送。",
    "closePipeline": "关闭管道并返回各命令结果列表。",
    "execute": "执行任意 Redis 命令（反射或字符串形式）。",
    "exists": "EXISTS：判断 key 是否存在。",
    "del": "DEL：删除一个或多个 key。",
    "unlink": "UNLINK：异步删除 key。",
    "type": "TYPE：返回 key 的数据类型。",
    "keys": "KEYS：按模式匹配返回 key 集合。",
    "scan": "SCAN：增量迭代 key 空间。",
    "randomKey": "RANDOMKEY：随机返回一个 key。",
    "rename": "RENAME：重命名 key。",
    "renameNX": "RENAMENX：仅当新 key 不存在时重命名。",
    "expire": "EXPIRE：以秒为单位设置过期时间。",
    "pExpire": "PEXPIRE：以毫秒为单位设置过期时间。",
    "expireAt": "EXPIREAT：按 Unix 秒时间戳设置过期。",
    "pExpireAt": "PEXPIREAT：按 Unix 毫秒时间戳设置过期。",
    "persist": "PERSIST：移除 key 的过期时间。",
    "move": "MOVE：将 key 迁移到指定数据库。",
    "ttl": "TTL：返回 key 剩余存活秒数。",
    "pTtl": "PTTL：返回 key 剩余存活毫秒数。",
    "sort": "SORT：对列表/集合/有序集合排序，可选 STORE。",
    "dump": "DUMP：序列化 key 的值。",
    "restore": "RESTORE：用 DUMP 数据恢复 key 并设置 TTL。",
    "get": "GET：读取字符串值。",
    "getSet": "GETSET：设置新值并返回旧值。",
    "mGet": "MGET：批量读取多个 key。",
    "set": "SET：写入字符串值，支持过期与 NX/XX 选项。",
    "setNX": "SETNX：仅当 key 不存在时写入。",
    "setEx": "SETEX：写入并设置秒级过期。",
    "pSetEx": "PSETEX：写入并设置毫秒级过期。",
    "mSet": "MSET：批量写入 key-value。",
    "mSetNX": "MSETNX：批量写入，全部 key 均不存在时才成功。",
    "incr": "INCR：字符串值自增 1。",
    "incrBy": "INCRBY：字符串值按整数增量自增。",
    "decr": "DECR：字符串值自减 1。",
    "decrBy": "DECRBY：字符串值按整数减量自减。",
    "append": "APPEND：在字符串末尾追加内容。",
    "getRange": "GETRANGE：按字节区间读取子串。",
    "setRange": "SETRANGE：从指定偏移覆写子串。",
    "getBit": "GETBIT：读取指定偏移处的位值。",
    "setBit": "SETBIT：设置指定偏移处的位值。",
    "bitCount": "BITCOUNT：统计字符串中置 1 的位数。",
    "bitOp": "BITOP：对多个字符串执行 AND/OR/XOR/NOT 位运算。",
    "bitPos": "BITPOS：查找第一个指定 bit 值的偏移。",
    "bitField": "BITFIELD：读写/增减字符串位域。",
    "strLen": "STRLEN：返回字符串字节长度。",
    "hDel": "HDEL：删除 hash 字段。",
    "hExists": "HEXISTS：判断 hash 字段是否存在。",
    "hGet": "HGET：读取 hash 单个字段。",
    "hSet": "HSET：写入 hash 字段。",
    "hSetNX": "HSETNX：仅当字段不存在时写入 hash。",
    "hMGet": "HMGET：批量读取 hash 字段。",
    "hMSet": "HMSET：批量写入 hash 字段。",
    "hKeys": "HKEYS：返回 hash 全部字段名。",
    "hVals": "HVALS：返回 hash 全部字段值。",
    "hLen": "HLEN：返回 hash 字段数量。",
    "hIncrBy": "HINCRBY：hash 字段按整数自增。",
    "hStrLen": "HSTRLEN：返回 hash 字段值字节长度。",
    "lPush": "LPUSH：从列表左侧入队。",
    "lPushX": "LPUSHX：仅当列表存在时从左侧入队。",
    "rPush": "RPUSH：从列表右侧入队。",
    "rPushX": "RPUSHX：仅当列表存在时从右侧入队。",
    "lPop": "LPOP：从列表左侧弹出元素。",
    "rPop": "RPOP：从列表右侧弹出元素。",
    "bLPop": "BLPOP：阻塞式从左侧弹出。",
    "bRPop": "BRPOP：阻塞式从右侧弹出。",
    "lLen": "LLEN：返回列表长度。",
    "lRange": "LRANGE：按闭区间下标返回列表片段。",
    "lTrim": "LTRIM：裁剪列表到指定区间。",
    "lIndex": "LINDEX：按下标读取列表元素。",
    "lInsert": "LINSERT：在 pivot 前/后插入元素。",
    "lSet": "LSET：按下标覆写列表元素。",
    "lRem": "LREM：删除列表中指定值的元素。",
    "rPopLPush": "RPOPLPUSH：从源列表弹出并推入目标列表。",
    "bRPopLPush": "BRPOPLPUSH：阻塞式 RPOPLPUSH。",
    "lPos": "LPOS：查找列表中匹配元素的下标。",
    "sAdd": "SADD：向集合添加 member。",
    "sRem": "SREM：从集合移除 member。",
    "sPop": "SPOP：随机弹出集合 member。",
    "sMembers": "SMEMBERS：返回集合全部 member。",
    "sIsMember": "SISMEMBER：判断 member 是否在集合中。",
    "sCard": "SCARD：返回集合基数。",
    "sMove": "SMOVE：将 member 从源集合移动到目标集合。",
    "sRandMember": "SRANDMEMBER：随机返回集合 member。",
    "sDiff": "SDIFF：返回集合差集。",
    "sDiffStore": "SDIFFSTORE：将差集写入目标 key。",
    "sInter": "SINTER：返回集合交集。",
    "sInterStore": "SINTERSTORE：将交集写入目标 key。",
    "sUnion": "SUNION：返回集合并集。",
    "sUnionStore": "SUNIONSTORE：将并集写入目标 key。",
    "sScan": "SSCAN：增量迭代集合 member。",
    "zAdd": "ZADD：向有序集合添加 member 及 score。",
    "zRem": "ZREM：从有序集合移除 member。",
    "zIncrBy": "ZINCRBY：有序集合 member 的 score 自增。",
    "zRank": "ZRANK：返回 member 正序排名（0 起）。",
    "zRevRank": "ZREVRANK：返回 member 逆序排名。",
    "zRange": "ZRANGE：按 rank 区间正序返回 member。",
    "zRevRange": "ZREVRANGE：按 rank 区间逆序返回 member。",
    "zRangeWithScores": "ZRANGE WITHSCORES：正序返回 member 与 score。",
    "zRevRangeWithScores": "ZREVRANGE WITHSCORES：逆序返回 member 与 score。",
    "zRangeByScore": "ZRANGEBYSCORE：按 score 区间正序返回 member。",
    "zRevRangeByScore": "ZREVRANGEBYSCORE：按 score 区间逆序返回 member。",
    "zRangeByScoreWithScores": "ZRANGEBYSCORE WITHSCORES：正序返回 member 与 score。",
    "zRevRangeByScoreWithScores": "ZREVRANGEBYSCORE WITHSCORES：逆序返回 member 与 score。",
    "zRangeByLex": "ZRANGEBYLEX：按字典序区间返回 member。",
    "zRevRangeByLex": "ZREVRANGEBYLEX：按字典序区间逆序返回 member。",
    "zCount": "ZCOUNT：统计 score 区间内 member 数量。",
    "zLexCount": "ZLEXCOUNT：统计字典序区间内 member 数量。",
    "zCard": "ZCARD：返回有序集合基数。",
    "zScore": "ZSCORE：返回 member 的 score。",
    "zRemRange": "ZREMRANGEBYRANK：按 rank 区间删除 member。",
    "zRemRangeByScore": "ZREMRANGEBYSCORE：按 score 区间删除 member。",
    "zUnionStore": "ZUNIONSTORE：将多个有序集合并集写入目标 key。",
    "zInterStore": "ZINTERSTORE：将多个有序集合交集写入目标 key。",
    "zScan": "ZSCAN：增量迭代有序集合 member。",
    "pfAdd": "PFADD：向 HyperLogLog 追加元素。",
    "pfCount": "PFCOUNT：估算 HyperLogLog 基数。",
    "pfMerge": "PFMERGE：合并多个 HyperLogLog。",
    "geoAdd": "GEOADD：向 geo 集合添加坐标点。",
    "geoDist": "GEODIST：计算两点间距离。",
    "geoHash": "GEOHASH：返回坐标的 geohash 字符串。",
    "geoPos": "GEOPOS：返回 member 的经纬度。",
    "geoRadius": "GEORADIUS：按圆心半径查询附近 member。",
    "geoRadiusByMember": "GEORADIUSBYMEMBER：以 member 为圆心查询附近点。",
    "geoRemove": "ZREM：从 geo 集合移除 member。",
    "publish": "PUBLISH：向频道发布消息。",
    "subscribe": "SUBSCRIBE：订阅频道。",
    "pSubscribe": "PSUBSCRIBE：按模式订阅频道。",
    "getSubscription": "返回 Pub/Sub 订阅对象。",
    "isSubscribed": "是否处于 Pub/Sub 订阅状态。",
    "multi": "MULTI：开启事务。",
    "exec": "EXEC：执行事务命令队列。",
    "discard": "DISCARD：放弃事务。",
    "watch": "WATCH：监视 key 以支持乐观事务。",
    "unwatch": "UNWATCH：取消全部 WATCH。",
    "select": "SELECT：切换数据库编号。",
    "ping": "PING：测试连接可用性。",
    "echo": "ECHO：回显字符串。",
    "flushDb": "FLUSHDB：清空当前数据库。",
    "flushAll": "FLUSHALL：清空全部数据库。",
    "bgSave": "BGSAVE：后台异步保存 RDB 快照。",
    "save": "SAVE：同步保存 RDB 快照。",
    "lastSave": "LASTSAVE：返回上次成功保存的时间戳。",
    "bgReWriteAof": "BGREWRITEAOF：后台重写 AOF 文件。",
    "bgWriteAof": "BGWRITEAOF：后台写入 AOF。",
    "shutdown": "SHUTDOWN：关闭 Redis 服务器。",
    "info": "INFO：返回服务器/统计信息。",
    "dbSize": "DBSIZE：返回当前库 key 数量。",
    "time": "TIME：返回服务器当前时间。",
    "getConfig": "CONFIG GET：读取配置项。",
    "setConfig": "CONFIG SET：设置配置项。",
    "resetConfigStats": "CONFIG RESETSTAT：重置统计计数。",
    "getClientList": "CLIENT LIST：返回客户端连接列表。",
    "getClientName": "CLIENT GETNAME：获取当前连接名称。",
    "setClientName": "CLIENT SETNAME：设置当前连接名称。",
    "killClient": "CLIENT KILL：断开指定客户端连接。",
    "slaveOf": "SLAVEOF：配置当前节点为指定 master 的从节点。",
    "slaveOfNoOne": "SLAVEOF NO ONE：将从节点提升为独立 master。",
    "scriptLoad": "SCRIPT LOAD：加载 Lua 脚本并返回 SHA1。",
    "scriptExists": "SCRIPT EXISTS：检查脚本 SHA1 是否已缓存。",
    "scriptFlush": "SCRIPT FLUSH：清空脚本缓存。",
    "scriptKill": "SCRIPT KILL：终止正在执行的脚本。",
    "migrate": "MIGRATE：将 key 迁移到另一 Redis 实例。",
    "touch": "TOUCH：更新 key 的最后访问时间。",
    "refcount": "OBJECT REFCOUNT：返回 key 引用计数。",
    "encodingOf": "OBJECT ENCODING：返回 key 内部编码。",
    "idletime": "OBJECT IDLETIME：返回 key 空闲秒数。",
    "streamCommands": "返回 Stream 命令适配器。",
    "resetConnection": "重置底层连接状态。",
    "filterResults": "按过滤器筛选命令结果。",
    "isFilterOkResponses": "是否仅保留 OK 类响应。",
    "setFilterOkResponses": "设置是否仅保留 OK 类响应。",
    "indexCommand": "返回当前命令在管道/事务中的索引。",
    "toCommand": "将 Spring 命令对象转为 Redis 参数。",
    "convert": "将键值对 Map 展开为 Redis 参数列表。",
    "transform": "将 Redisson 异常转为 Spring DataAccessException。",
    "sync": "阻塞等待 {@link RFuture} 完成并返回结果。",
    "doScan": "SCAN 迭代器内部实现。",
    "write": "向指定 key 所在 slot 发送写命令。",
    "read": "向指定 key 所在 slot 发送读命令。",
    "xAck": "XACK：确认消费组已处理指定消息 ID。",
    "xAdd": "XADD：向 Stream 追加一条记录。",
    "xDel": "XDEL：按 ID 删除 Stream 消息。",
    "xLen": "XLEN：返回 Stream 当前长度。",
    "xGroupCreate": "XGROUP CREATE：创建消费组。",
    "xGroupDelConsumer": "XGROUP DELCONSUMER：删除消费组中的消费者。",
    "xGroupDestroy": "XGROUP DESTROY：销毁消费组。",
    "xRange": "XRANGE：按 ID 范围正序读取 Stream 记录。",
    "xRevRange": "XREVRANGE：按 ID 范围逆序读取 Stream 记录。",
    "xRead": "XREAD：从一个或多个 Stream 读取消息。",
    "xReadGroup": "XREADGROUP：以消费组身份读取 Stream 消息。",
    "xTrim": "XTRIM：按最大长度裁剪 Stream。",
    "xPending": "XPENDING：查询消费组待处理消息摘要或明细。",
    "xClaim": "XCLAIM：将空闲消息重新分配给新消费者。",
    "xClaimJustId": "XCLAIM JUSTID：认领消息并仅返回 ID。",
    "xPendingSummary": "XPENDING：返回消费组待处理消息汇总。",
    "xInfo": "XINFO STREAM：返回 Stream 元信息。",
    "xInfoGroups": "XINFO GROUPS：返回 Stream 消费组列表。",
    "xInfoConsumers": "XINFO CONSUMERS：返回消费组内消费者列表。",
    "decode": "将 Redis 原始响应解码为 Spring Stream 记录。",
    "range": "XRANGE/XREVRANGE 共用实现。",
    "toStringList": "将 RecordId 列表转为 Redis 命令参数字符串列表。",
    "toLowerBound": "将 Spring Range 下界转为 Stream ID 字符串。",
    "toUpperBound": "将 Spring Range 上界转为 Stream ID 字符串。",
    "zRemRangeByRank": "ZREMRANGEBYRANK：按 rank 区间删除 member。",
    "scanIterator": "ZSCAN 迭代器：按 cursor 分页拉取 member。",
    "pSetEX": "PSETEX：写入并设置毫秒过期。",
    "setEX": "SETEX：写入并设置秒级过期。",
    "clusterGetNodes": "CLUSTER NODES：获取集群全部节点拓扑。",
    "clusterGetSlaves": "查找指定 master 下的 replica 节点。",
    "clusterGetMasterSlaveMap": "构建 master 到 slaves 的映射。",
    "clusterGetSlotForKey": "KEYSLOT：计算 key 对应的哈希槽。",
    "clusterGetNodeForSlot": "查找负责指定槽的主节点。",
    "clusterGetNodeForKey": "按 key 查找负责其槽的主节点。",
    "clusterGetClusterInfo": "CLUSTER INFO：获取集群状态信息。",
    "clusterAddSlots": "CLUSTER ADDSLOTS：向节点分配槽位。",
    "clusterCountKeysInSlot": "CLUSTER COUNTKEYSINSLOT：统计槽内 key 数量。",
    "clusterDeleteSlots": "CLUSTER DELSLOTS：从节点移除槽位。",
    "clusterDeleteSlotsInRange": "CLUSTER DELSLOTS：按槽范围批量移除。",
    "clusterForget": "CLUSTER FORGET：从集群视图中移除节点。",
    "clusterGetKeysInSlot": "CLUSTER GETKEYSINSLOT：返回槽内 sample key。",
    "clusterMeet": "CLUSTER MEET：将节点加入集群。",
    "clusterReplicate": "CLUSTER REPLICATE：配置节点为指定 master 的 replica。",
    "clusterSetSlot": "CLUSTER SETSLOT：导入/迁移/绑定槽位。",
    "serverCommands": "返回 Server 命令适配器。",
    "mDel": "批量 DEL：一次删除多个 key。",
    "hGetAll": "HGETALL：返回 hash 全部 field-value。",
    "hIncrByFloat": "HINCRBYFLOAT：hash 字段按浮点数自增。",
}

FIELD_CN: list[tuple[str, str]] = [
    ("    private boolean closed;", "    /** 连接是否已关闭。 */\n    private boolean closed;"),
    ("    protected final Redisson redisson;", "    /** 底层 Redisson 客户端。 */\n    protected final Redisson redisson;"),
    ("    CommandAsyncExecutor executorService;", "    /** 异步命令执行器。 */\n    CommandAsyncExecutor executorService;"),
    ("    private RedissonSubscription subscription;", "    /** Pub/Sub 订阅管理器。 */\n    private RedissonSubscription subscription;"),
    ("    private final RedissonConnection connection;", "    /** 所属 {@link RedissonConnection}。 */\n    private final RedissonConnection connection;"),
    ("    private final CommandAsyncExecutor executor;", "    /** 异步命令执行器。 */\n    private final CommandAsyncExecutor executor;"),
    (
        "    public RedissonConnection(RedissonClient redisson) {",
        "    /** 绑定 {@link RedissonClient} 并初始化命令执行器。 */\n    public RedissonConnection(RedissonClient redisson) {",
    ),
    (
        "    public RedissonClusterConnection(RedissonClient redisson) {",
        "    /** 以集群模式创建连接。 */\n    public RedissonClusterConnection(RedissonClient redisson) {",
    ),
    (
        "    public RedissonStreamCommands(RedissonConnection connection, CommandAsyncExecutor executor) {",
        "    /** 绑定连接与命令执行器。 */\n    public RedissonStreamCommands(RedissonConnection connection, CommandAsyncExecutor executor) {",
    ),
    (
        "    RedissonReactiveStringCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n    RedissonReactiveStringCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    RedissonReactiveZSetCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n    RedissonReactiveZSetCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    RedissonReactiveStreamCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n    RedissonReactiveStreamCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    public RedissonReactiveKeyCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n    public RedissonReactiveKeyCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    RedissonReactiveHashCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n    RedissonReactiveHashCommands(CommandReactiveExecutor executorService) {",
    ),
]

METHOD_RE = re.compile(
    r"(?P<prefix>\n    )(?P<override>@Override\n    )?"
    r"(?P<sig>(?:public|protected|private)\s+(?:static\s+)?[\w<>,\?\[\]\s]+\s+(\w+)\s*\([^)]*\)\s*(?:throws\s+[\w.\s,]+)?\{)"
)


def has_comment_before(text: str, pos: int) -> bool:
    window = text[max(0, pos - 120) : pos]
    return "/**" in window or "//" in window.split("\n")[-1]


def method_comment(name: str) -> str:
    if name in METHOD_CN:
        return METHOD_CN[name]
    if name.startswith("x") and len(name) > 1 and name[1].isupper():
        return f"{name}：Redis Stream 命令。"
    if name.startswith("cluster"):
        return f"{name}：集群管理命令。"
    if name.startswith("z") and name[1].isupper():
        return f"{name}：有序集合命令。"
    if name.startswith("h") and name[1].isupper():
        return f"{name}：Hash 命令。"
    return f"{name}：Redis 命令实现。"


def collect_auto_replacements(text: str) -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []
    for old, new in FIELD_CN:
        if old in text and new.split("\n", 1)[0] not in text:
            reps.append((old, new))

    for m in METHOD_RE.finditer(text):
        if has_comment_before(text, m.start()):
            continue
        name = m.group(4)
        cn = method_comment(name)
        old = m.group(0)
        override = m.group("override")
        sig = m.group("sig")
        if override:
            new = f"\n    /** {cn} */\n    @Override\n    {sig}"
        else:
            new = f"\n    /** {cn} */\n    {sig}"
        reps.append((old, new))
    return reps


def load_reuse() -> dict[str, list[tuple[str, str]]]:
    reuse: dict[str, list[tuple[str, str]]] = {}

    def _load(mod: str, attr: str) -> dict[str, list[tuple[str, str]]]:
        spec = importlib.util.spec_from_file_location(mod, SCRIPTS / f"{mod}.py")
        m = importlib.util.module_from_spec(spec)
        assert spec.loader is not None
        spec.loader.exec_module(m)
        return getattr(m, attr)

    w12a = _load("wave12a_replacements_redisson", "W12A_REPLACEMENTS")
    w15a = _load("wave15a_replacements_redisson", "W15A_REPLACEMENTS")
    w11b = _load("wave11b_replacements_redisson", "W11B_REPLACEMENTS")

    reuse["RedissonReactiveStringCommands.java"] = w12a["RedissonReactiveStringCommands.java"]
    reuse["RedissonReactiveStreamCommands.java"] = w15a["RedissonReactiveStreamCommands.java"]
    reuse["RedissonReactiveKeyCommands.java"] = w12a["RedissonReactiveKeyCommands.java"]
    reuse["RedissonReactiveHashCommands.java"] = w11b["RedissonReactiveHashCommands.java"]
    return reuse


def class_javadoc_replacements(text: str, cls: str) -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []
    if cls in CLASS_JDOC:
        for old in (_EMPTY_JDOC, _EMPTY_JDOC2, _CONN_JDOC if cls == "RedissonConnection" else None):
            if old and old in text:
                reps.append((old, CLASS_JDOC[cls]))
                break
    return reps


def collect_replacements(rel: str) -> list[tuple[str, str]]:
    path = ORIG / rel
    text = path.read_text(encoding="utf-8")
    cls = Path(rel).stem
    reps: list[tuple[str, str]] = []
    seen: set[str] = set()

    def add(old: str, new: str) -> None:
        if old not in text or old in seen:
            return
        reps.append((old, new))
        seen.add(old)

    for old, new in class_javadoc_replacements(text, cls):
        add(old, new)
    for old, new in collect_auto_replacements(text):
        add(old, new)
    return reps


REUSE_SHORT = {
    "RedissonReactiveStringCommands.java",
    "RedissonReactiveStreamCommands.java",
    "RedissonReactiveKeyCommands.java",
    "RedissonReactiveHashCommands.java",
}


def emit() -> None:
    reuse = load_reuse()
    all_data: dict[str, list[tuple[str, str]]] = {}

    for rel in FILES:
        short = Path(rel).name
        if short in reuse and short in REUSE_SHORT:
            reps = list(reuse[short])
            extra_text = (ORIG / rel).read_text(encoding="utf-8")
            existing_olds = {o for o, _ in reps}
            for old, new in collect_auto_replacements(extra_text):
                if old not in existing_olds:
                    reps.append((old, new))
                    existing_olds.add(old)
        else:
            reps = collect_replacements(rel)
        if not reps:
            raise RuntimeError(f"No replacements for {rel}")
        all_data[rel] = reps

    lines = [
        '"""Chinese annotation replacements for Redisson 4.7.0 wave-55b spring-data [15:30]."""',
        "from __future__ import annotations",
        "",
        "W55B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
    ]
    emitted_short: set[str] = set()
    for rel, reps in all_data.items():
        short = Path(rel).name
        lines.append(f"    {rel!r}: [")
        for old, new in reps:
            lines.append(f"        ({old!r}, {new!r}),")
        lines.append("    ],")
        if short not in emitted_short:
            same = all(
                all_data[r] == reps for r in all_data if Path(r).name == short and r != rel
            )
            if same:
                lines.append(f"    {short!r}: [")
                for old, new in reps:
                    lines.append(f"        ({old!r}, {new!r}),")
                lines.append("    ],")
                emitted_short.add(short)
    lines.append("}")
    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {OUT} ({len(all_data)} files)")
    for rel, reps in all_data.items():
        print(f"  {rel}: {len(reps)} replacements")


if __name__ == "__main__":
    emit()
