#!/usr/bin/env python3
"""Generate wave50a_replacements_rocketmq_store.py for RocketMQ store wave50a [0:15]."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "rocketmq/rocketmq-all-5.5.0/original"
OUT = ROOT / "scripts/wave50a_replacements_rocketmq_store.py"
FILES = [
    ln.strip()
    for ln in Path("/tmp/rmq50a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FULL_JDOC: dict[str, tuple[str, str]] = {}
DESC_MAP: dict[str, str] = {}
INLINE_MAP: dict[str, str] = {}
CLASS_DESC: dict[str, str] = {}


def full(sub: str, cn_body: str) -> None:
    FULL_JDOC[sub] = (sub, cn_body)


def t(en: str, cn: str) -> None:
    DESC_MAP[en.strip()] = cn.strip()


def inline(en: str, cn: str) -> None:
    INLINE_MAP[en.strip()] = cn.strip()


# --- Class descriptions ---
CLASS_DESC.update(
    {
        "MessageStore": "消息存储核心接口：定义 Broker 落盘、读取、索引、HA 同步、位点管理与统计等契约，"
        "第三方可基于此实现自定义存储引擎。",
        "StoreStatsService": "存储层统计服务：采样记录 put/get TPS、延迟分布桶、Topic 维度计数及磁盘落后指标，"
        "周期性打印并供 Broker 查询。",
        "MessageStoreConfig": "MessageStore 运行时配置：路径、MappedFile 大小、刷盘/清理策略、HA、RocksDB、"
        "定时消息、Compaction 等全部可调参数。",
        "DLedgerCommitLog": "基于 DLedger 的 CommitLog 实现：将消息写入 Raft 复制日志，支持主从自动切换与 epoch 管理。",
        "DefaultHAClient": "经典 HA 从节点客户端：连接 Master 拉取 CommitLog 并上报 slaveMaxOffset。",
        "DefaultHAConnection": "经典 HA 主节点连接：向 Slave 传输 CommitLog 数据并接收 offset 上报。",
        "AutoSwitchHAClient": "自动切换 HA 从节点客户端：握手协商 SyncStateSet、epoch 与异步 Learner 角色后同步数据。",
        "AutoSwitchHAConnection": "自动切换 HA 主节点连接：处理握手、传输 CommitLog 并维护 SyncStateSet 同步状态。",
        "AutoSwitchHAService": "可切换角色的 HA 服务：管理 SyncStateSet、epoch 文件、主从 truncate 与 HA 连接生命周期。",
        "IndexService": "消息索引服务：按 key 维护 IndexFile 链表，支持异步/同步建索引与过期清理。",
        "CompactionLog": "Compaction 日志：对 KV/Compaction Topic 消息进行合并压缩，维护 Compaction CQ 与 offset 映射。",
        "DefaultMappedFile": "默认 MappedFile 实现：mmap 或 FileChannel 读写 CommitLog/CQ 映射文件，支持 TransientStorePool。",
        "AbstractPluginMessageStore": "MessageStore 插件抽象基类：将接口调用委托给 next 链，便于扩展存储能力。",
        "BatchConsumeQueue": "批量消费队列（BCQ）：每条 CQ 单元对应一批消息的 CommitLog 物理范围，提升批量消费效率。",
        "CombineConsumeQueueStore": "组合消费队列存储：按 Topic 类型在文件型 CQ 与 RocksDB CQ 之间路由读写。",
    }
)

# --- MessageStore interface javadocs ---
t(
    "This class defines contracting interfaces to implement, allowing third-party vendor to use customized message store.",
    "消息存储核心接口：第三方厂商可据此实现自定义存储引擎。",
)
t("Load previously stored messages.", "加载持久化消息与索引数据。")
t("Launch this message store.", "启动消息存储（Reput、HA、定时线程等）。")
t("Shutdown this message store.", "关闭消息存储并释放资源。")
t(
    "Destroy this message store. Generally, all persistent files should be removed after invocation.",
    "销毁消息存储；通常应删除全部持久化文件。",
)
t(
    "Store a message into store in async manner, the processor can process the next request rather than wait for",
    "异步落盘单条消息；Processor 无需阻塞等待结果。",
)
t("Store a batch of messages in async manner", "异步落盘批量消息。")
t("Store a message into store.", "同步落盘单条消息。")
t("Store a batch of messages.", "同步落盘批量消息。")
t(
    "Query at most <code>maxMsgNums</code> messages belonging to <code>topic</code> at <code>queueId</code> starting",
    "从指定 Topic/队列 offset 起最多拉取 maxMsgNums 条消息。",
)
t("Asynchronous get message", "异步拉取消息。")
t("Get maximum offset of the topic queue.", "返回 Topic 队列最大消费 offset。")
t("Get the minimum offset of the topic queue.", "返回 Topic 队列最小消费 offset。")
t(
    "Get the offset of the message in the commit log, which is also known as physical offset.",
    "返回消息在 CommitLog 中的物理 offset。",
)
t(
    "Look up the physical offset of the message whose store timestamp is as specified.",
    "按存储时间戳查找对应消息的 CommitLog 物理 offset。",
)
t(
    "Look up the physical offset of the message whose store timestamp is as specified with specific boundaryType.",
    "按存储时间戳与边界类型查找 CommitLog 物理 offset。",
)
t("Look up the message by given commit log offset.", "按 CommitLog 物理 offset 查找消息。")
t("Look up the message by given commit log offset and size.", "按 CommitLog offset 与 size 查找消息。")
t("Get one message from the specified commit log offset.", "从指定 CommitLog offset 读取单条消息。")
t("Get the running information of this store.", "返回存储运行时信息。")
t(
    "Message store runtime information, which should generally contains various statistical information.",
    "存储运行时信息，通常包含各类统计数据。",
)
t("HA runtime information", "HA 运行时信息。")
t("Get the maximum commit log offset.", "返回 CommitLog 最大物理 offset。")
t("Get the minimum commit log offset.", "返回 CommitLog 最小物理 offset。")
t("Get the store time of the earliest message in the given queue.", "返回指定队列最早消息的存储时间。")
t("Get the store time of the earliest message in this store.", "返回 Store 中最早消息的存储时间。")
t(
    "Asynchronous get the store time of the earliest message in this store.",
    "异步返回 Store 中最早消息的存储时间。",
)
t("Get the store time of the message specified.", "返回指定消息的存储时间。")
t("Asynchronous get the store time of the message specified.", "异步返回指定消息的存储时间。")
t("Get the total number of the messages in the specified queue.", "返回指定队列消息总数。")
t(
    "Get the raw commit log data starting from the given offset, which should used for replication purpose.",
    "从指定 offset 读取原始 CommitLog 数据（用于复制）。",
)
t(
    "Get the raw commit log data starting from the given offset, across multiple mapped files.",
    "跨多个 MappedFile 读取原始 CommitLog 数据。",
)
t("Append data to commit log.", "向 CommitLog 追加数据。")
t("Execute file deletion manually.", "手动触发过期文件删除。")
t("Query messages by given key.", "按消息 key 查询索引。")
t("Asynchronous query messages by given key.", "异步按 key 查询消息。")
t("Update HA master address.", "更新 HA Master 地址。")
t("Update master address.", "更新 Master 地址。")
t("Return how much the slave falls behind.", "返回从节点落后 Master 的字节数。")
t("Return the current timestamp of the store.", "返回 Store 当前时间戳。")
t("Delete topic's consume queue file and unused stats.", "删除 Topic 消费队列文件及无用统计。")
t("Clean unused topics which not in retain topic name set.", "清理不在保留集合中的 Topic。")
t("Clean expired consume queues.", "清理过期消费队列。")
t("Check if the given message has been swapped out of the memory.", "检查消息是否已被换出内存。")
t("Check if the given message is in the page cache.", "检查消息是否在 PageCache 中。")
t("Check if the given message is in store.", "检查消息是否仍在 Store 中。")
t(
    "Get number of the bytes that have been stored in commit log and not yet dispatched to consume queue.",
    "返回已写入 CommitLog 但尚未 dispatch 到 CQ 的字节数。",
)
t(
    "Get number of the bytes that have been stored in commit log and not yet flushed to disk.",
    "返回已写入 CommitLog 但尚未刷盘的字节数。",
)
t(
    "Get number of the milliseconds that have been stored in commit log and not yet dispatched to consume queue.",
    "返回 CommitLog 中尚未 dispatch 的数据对应的时间跨度（毫秒）。",
)
t("Flush the message store to persist all data.", "刷盘持久化全部数据。")
t("Get the current flushed offset.", "返回当前已刷盘 offset。")
t("Get confirm offset.", "返回 confirm offset。")
t("Set confirm offset.", "设置 confirm offset。")
t("Check if the operating system page cache is busy or not.", "检查 OS PageCache 是否繁忙。")
t("Get lock time in milliseconds of the store by far.", "返回 Store 迄今持锁时间（毫秒）。")
t("Check if the transient store pool is deficient.", "检查 TransientStorePool 是否不足。")
t("Get the dispatcher list.", "返回 CommitLog dispatch 处理器列表。")
t("Add dispatcher.", "添加 dispatch 处理器。")
t(
    "Get consume queue of the topic/queue. If consume queue not exist, will return null",
    "获取 Topic/队列消费队列；不存在时返回 null。",
)
t(
    "Get consume queue of the topic/queue. If consume queue not exist, will create one then return it.",
    "获取 Topic/队列消费队列；不存在时创建并返回。",
)
t("Get BrokerStatsManager of the messageStore.", "返回 Broker 统计管理器。")
t("Will be triggered when a new message is appended to commit log.", "CommitLog 追加新消息后触发。")
t("Will be triggered when a new dispatch request is sent to message store.", "向 Store 发送 dispatch 请求时触发。")
t("Get the message store config", "返回 MessageStore 配置。")
t("Get the statistics service", "返回存储统计服务。")
t("Get the store checkpoint component", "返回 Store 检查点组件。")
t("Get the system clock", "返回系统时钟。")
t("Get the commit log", "返回 CommitLog 实例。")
t("Get running flags", "返回运行标志。")
t("Get the transient store pool", "返回 TransientStorePool。")
t("Get the HA service", "返回 HA 服务。")
t("Get the allocate-mappedFile service", "返回 MappedFile 分配服务。")
t("Truncate dirty logic files", "截断脏逻辑文件。")
t("Unlock mappedFile", "解锁 MappedFile。")
t("Get the perf counter component", "返回性能计数器。")
t("Get the queue store", "返回消费队列存储。")
t("If 'sync disk flush' is configured in this message store", "是否配置为同步刷盘。")
t("If this message store is sync master role", "是否为 SyncMaster 角色。")
t(
    "Assign a message to queue offset. If there is a race condition, you need to lock/unlock this method",
    "为消息分配队列 offset；存在竞态时需外部加锁。",
)
t(
    "Increase queue offset in memory table. If there is a race condition, you need to lock/unlock this method",
    "递增内存位点表中的队列 offset；存在竞态时需外部加锁。",
)
t("Get master broker message store in process in broker container", "BrokerContainer 中获取同进程 Master Store。")
t("Set master broker message store in process", "设置同进程 Master Store 引用。")
t("Use FileChannel to get data", "通过 FileChannel 读取数据。")
t("Set the number of alive replicas in group.", "设置副本组存活副本数。")
t("Get the number of alive replicas in group.", "返回副本组存活副本数。")
t("Wake up AutoRecoverHAClient to start HA connection.", "唤醒 AutoRecoverHAClient 建立 HA 连接。")
t("Get master flushed offset.", "返回 Master 已刷盘 offset。")
t("Get broker init max offset.", "返回 Broker 初始化最大 offset。")
t("Set master flushed offset.", "设置 Master 已刷盘 offset。")
t("Set broker init max offset.", "设置 Broker 初始化最大 offset。")
t("Calculate the checksum of a certain range of data.", "计算指定数据范围的校验和。")
t("Truncate commitLog and consume queue to certain offset.", "将 CommitLog 与 CQ 截断到指定 offset。")
t("Check if the offset is aligned with one message.", "检查 offset 是否与单条消息对齐。")
t("Get put message hook list", "返回落盘 Hook 列表。")
t("Set send message back hook", "设置消息退回 Hook。")
t("Get send message back hook", "返回消息退回 Hook。")
t("Get last mapped file and return lase file first Offset", "返回最后一个 MappedFile 及其首条 offset。")
t("Get last mapped file", "返回最后一个 MappedFile。")
t("Set physical offset", "设置物理 offset。")
t("Return whether mapped file is empty", "返回 MappedFile 是否为空。")
t("Get state machine version", "返回状态机版本。")
t("Get store metrics manager", "返回 Store 指标管理器。")
t("Check message and return size", "校验消息并返回大小。")
t("Get remain transientStoreBuffer numbers", "返回 TransientStore 缓冲剩余数量。")
t("Get remain how many data to commit", "返回待 commit 数据量。")
t("Get remain how many data to flush", "返回待 flush 数据量。")
t("Get whether message store is shutdown", "返回 Store 是否已 shutdown。")
t(
    "Estimate number of messages, within [from, to], which match given filter",
    "估算 [from, to] 范围内匹配过滤器的消息数量。",
)
t("Get metrics view of store", "返回 Store 指标视图。")
t("Init store metrics", "初始化 Store 指标。")
t("Recover topic queue table", "恢复 Topic 队列表。")
t("notify message arrive if necessary", "必要时通知消息到达。")

# @param / @return common
PARAM_RETURN: dict[str, str] = {
    "@return true if success; false otherwise.": "@return 成功返回 true，否则 false",
    "@throws Exception if there is any error.": "@throws Exception 启动异常",
    "@param msg MessageInstance to store": "@param msg 待存储消息",
    "@param messageExtBatch the message batch": "@param messageExtBatch 批量消息",
}

# --- MessageStoreConfig inline ---
inline("The root directory in which the log data is kept", "日志数据根目录")
inline("The directory in which the commitlog is kept", "CommitLog 存储目录")
inline("The directory in which the epochFile is kept", "Epoch 文件目录")
inline("CommitLog file size,default is 1G", "CommitLog 单文件大小，默认 1G")
inline("CompactionLog file size, default is 100M", "CompactionLog 单文件大小，默认 100M")
inline("CompactionLog consumeQueue file size, default is 10M", "Compaction CQ 单文件大小，默认 10M")
inline("TimerLog file size, default is 100M", "TimerLog 单文件大小，默认 100M")
inline("default, defaultRocksDB", "默认 RocksDB 模式标识")
inline("ConsumeQueue file size,default is 30W", "ConsumeQueue 单文件条目数，默认 30 万")
inline("enable consume queue ext", "是否启用 ConsumeQueue 扩展文件")
inline("ConsumeQueue extend file size, 48M", "ConsumeQueue 扩展文件大小，48M")
inline("Bit count of filter bit map.", "过滤器位图 bit 数")
inline("this will be set by pipe of calculate filter bit map.", "由位图计算管道设置")
inline("CommitLog flush interval", "CommitLog 刷盘间隔")
inline("flush data to disk", "将数据 flush 到磁盘")
inline("Only used if TransientStorePool enabled", "仅 TransientStorePool 启用时有效")
inline("flush data to FileChannel", "将数据 flush 到 FileChannel")
inline("Whether schedule flush", "是否定时 flush")
inline("ConsumeQueue flush interval", "ConsumeQueue 刷盘间隔")
inline("Resource reclaim interval", "资源回收间隔")
inline("CommitLog removal interval", "CommitLog 过期删除检查间隔")
inline("ConsumeQueue removal interval", "ConsumeQueue 过期删除检查间隔")
inline("When to delete,default is at 4 am", "定时删除时刻，默认凌晨 4 点")
inline("The number of hours to keep a log file before deleting it (in hours)", "日志文件保留小时数")
inline("Flow control for ConsumeQueue", "ConsumeQueue 流控阈值")
inline(
    "The maximum size of message body,default is 4M,4M only for body length,not include others.",
    "消息体最大长度，默认 4M（仅 body，不含属性）",
)
inline(
    "The maximum size of message body can be  set in config;count with maxMsgNums * CQ_STORE_UNIT_SIZE(20 || 46)",
    "可配置的最大消息体；与 maxMsgNums * CQ_STORE_UNIT_SIZE 相关",
)
inline(
    "Whether check the CRC32 of the records consumed.",
    "消费时是否校验 CRC32",
)
inline(
    "This ensures no on-the-wire or on-disk corruption to the messages occurred.",
    "防止网络或磁盘损坏的消息被消费",
)
inline(
    "This check adds some overhead,so it may be disabled in cases seeking extreme performance.",
    "校验有开销，极致性能场景可关闭",
)
inline(
    "Whether check the commitlog offset validity during abnormal recovery.",
    "异常恢复时是否校验 CommitLog offset 有效性",
)
inline(
    "This helps detect and truncate old file data that may pass CRC checks but contains invalid offsets.",
    "帮助检测并截断 CRC 通过但 offset 无效的旧数据",
)
inline("How many pages are to be flushed when flush CommitLog", "CommitLog flush 页数")
inline("How many pages are to be committed when commit data to file", "commit 到文件的页数")
inline("Flush page size when the disk in warming state", "磁盘预热时的 flush 页大小")
inline("How many pages are to be flushed when flush ConsumeQueue", "ConsumeQueue flush 页数")
inline(
    "Used by GroupTransferService to sync messages from master to slave",
    "GroupTransferService 主从同步消息用",
)
inline(
    "Used by PutMessage to wait messages be flushed to disk and synchronized in current broker member group.",
    "PutMessage 等待刷盘并在副本组同步完成",
)
inline("DLedger message store config", "DLedger 存储配置")
inline("Polish dispatch", "Dispatch 优化开关")
inline("For recheck the reput", "Reput 复查用")
inline("Maximum length of topic, it will be removed in the future release", "Topic 最大长度（后续版本移除）")
inline("Sleep interval between to corrections", "校正间隔休眠时间")
inline("Force correct min offset interval", "强制校正最小 offset 间隔")
inline("swap", "Swap 相关配置")
inline("rocksdb mode", "RocksDB 模式")
inline("Shared byte buffer manager configuration", "共享 ByteBuffer 管理器配置")
inline(
    "In order to get this value from messageStoreConfig properties file created before v4.4.1.",
    "兼容 v4.4.1 之前 properties 文件读取该值",
)

full(
    "1. Register to broker after (startTime + disappearTimeAfterStart)",
    "\n * 1. Broker 在 (startTime + disappearTimeAfterStart) 后才注册\n"
    " * 2. 内部消息交换（PopReviveService、TimerDequeueGetService 等）也在该时刻后启动\n",
)
full(
    "introduced since 4.0.x. Determine whether to use mutex reentrantLock when putting message.",
    "\n * 自 4.0.x 起：落盘时是否使用互斥 ReentrantLock。\n",
)
full(
    "Maximum size of data to transfer to slave.",
    "\n * 向 Slave 传输数据的最大字节数；不可大于 HAClient.READ_MAX_BUFFER_SIZE。\n",
)

# --- StoreStatsService ---
inline("The rule to define buckets", "定义延迟分布桶的规则")
inline("buckets", "当前采样桶")
inline("for putMessageEntireTimeMax", "保护 putMessageEntireTimeMax 的锁")
inline("for getMessageEntireTimeMax", "保护 getMessageEntireTimeMax 的锁")

# --- DLedgerCommitLog ---
full(
    "Store all metadata downtime for recovery, data protection reliability",
    "\n * 存储恢复所需的全部元数据停机信息，保障数据可靠性。\n",
)
full("Serialize message", "\n * 序列化消息。\n")
inline("The id identifies the broker role, 0 means master, others means slave", "Broker 角色 ID：0 为 Master，非 0 为 Slave")
inline(
    "This offset separate the old commitlog from dledger commitlog",
    "分隔旧 CommitLog 与 DLedger CommitLog 的 offset",
)
inline("To prevent too much log in defaultMessageStore", "避免 DefaultMessageStore 日志过多")
inline("the old logic will keep the last file, here to delete it", "旧逻辑保留最后一个文件，此处主动删除")

# --- DefaultHAClient ---
full(
    "Report header buffer size. Schema: slaveMaxOffset.",
    "\n * 上报头缓冲区大小；协议字段：slaveMaxOffset（8 字节）。\n",
)
full("last time that slave reads date from master.", "\n * Slave 最近一次从 Master 读取数据的时间。\n")
full("last time that slave reports offset to master.", "\n * Slave 最近一次向 Master 上报 offset 的时间。\n")

# --- DefaultHAConnection ---
full(
    "Transfer Header buffer size. Schema: physic offset and body size.",
    "\n * 传输头缓冲区大小；包含物理 offset 与 body 大小。\n",
)
inline("Build Header", "构建传输头")
inline("Write Header", "写入传输头")
inline("Write Body", "写入消息体")

# --- AutoSwitchHA ---
full(
    "SwitchAble ha service, support switch role to master or slave.",
    "\n * 可切换角色的 HA 服务：支持 Master/Slave 角色切换与 SyncStateSet 管理。\n",
)
full(
    "Check and maybe shrink the SyncStateSet.",
    "\n * 检查并可能收缩 SyncStateSet：Slave 长时间未上报 offset 则移除。\n",
)
full(
    "Check and maybe add the slave to SyncStateSet.",
    "\n * 检查并可能将 Slave 加入 SyncStateSet：slaveMaxOffset 追平 confirmOffset 时加入。\n",
)
inline("Shutdown thread firstly", "先关闭 HA 线程")
inline("Original state", "原始 HA 状态")
inline("IsSyncFromLastFile", "是否从最后一个文件同步")
inline("IsAsyncLearner role", "是否为 AsyncLearner 角色")
inline("Use -1 to stand for Long.max", "用 -1 表示 Long.MAX_VALUE")
inline("SlaveBrokerId", "Slave Broker ID")
inline("Flag(isSyncFromLastFile)", "标志：是否从末文件同步")
inline("Flag(isAsyncLearner role)", "标志：AsyncLearner 角色")
inline(
    "Indicate whether the syncStateSet is currently in the process of being synchroni",
    "SyncStateSet 是否正在同步中",
)
inline("Stop ha client if needed", "必要时停止 HA Client")
inline("Truncate dirty file", "截断脏文件")
inline("Append new epoch to epochFile", "向 epoch 文件追加新 epoch")
inline("Whether the slave have already sent a handshake message", "Slave 是否已发送握手消息")

# --- IndexService ---
full("Maximum times to attempt index file creation.", "\n * 创建 IndexFile 的最大重试次数。\n")
full(
    "Retries to get or create index file.",
    "\n * 重试获取或创建 IndexFile。\n * @return {@link IndexFile} 或失败时 null\n",
)
inline("ascending order", "按时间升序")

# --- CompactionLog ---
inline("batch consume queue already separated", "批量 CQ 已分离存储")
inline("split bytebuffer to avoid encode message again", "拆分 ByteBuffer 避免重复编码")
inline("merge files", "合并 Compaction 文件")

# --- DefaultMappedFile ---
full(
    "Message will put to here first, and then reput to FileChannel if writeBuffer is not null.",
    "\n * 消息先写入 writeBuffer，非空时再 reput 到 FileChannel。\n",
)
full(
    "Configuration flag to use RandomAccessFile instead of MappedByteBuffer for writing",
    "\n * 写路径使用 RandomAccessFile 而非 MappedByteBuffer 的配置开关。\n",
)
full(
    "If this mapped file belongs to consume queue, this field stores store-timestamp of first message referenced b",
    "\n * CQ 映射文件时，存储首条引用消息的 store-timestamp。\n",
)
inline(
    "On the windows platform and openjdk 11 method isLoaded0 always returns false.",
    "Windows + OpenJDK11 下 isLoaded0 恒为 false",
)
inline("Still create MappedByteBuffer for reading operations", "读路径仍创建 MappedByteBuffer")
inline("Use MappedByteBuffer for both reading and writing (default behavior)", "读写均使用 MappedByteBuffer（默认）")

# --- BatchConsumeQueue ---
full(
    "BatchConsumeQueue's store unit. Format:",
    "\n * BatchConsumeQueue 存储单元格式说明。\n",
)
full(
    "Gets SelectMappedBufferResult by batch-message offset",
    "\n * 按批量消息 offset 获取 SelectMappedBufferResult；调用方负责 release。\n",
)
inline("iterate all BCQ files", "遍历全部 BCQ 文件")
inline("the offset is too small.", "offset 过小")
inline(
    "the timestamp is too small. so we decide to result first BCQ file.",
    "时间戳过小，返回第一个 BCQ 文件",
)
inline("tagscode", "Tag 哈希码")

# --- CombineConsumeQueueStore ---
full(
    "ConsumeQueueStore recovers through commitlog dispatch, so it needs to search which file in the commitLog to  ",
    "\n * CQ Store 通过 CommitLog dispatch 恢复，需定位 CommitLog 中起始恢复文件。\n",
)
inline("Inner consume queue store.", "内部文件型 CQ 存储")
inline("current read consume queue store.", "当前读 CQ 存储")
inline("consume queue store for assign offset and increase offset.", "分配/递增 offset 的 CQ 存储")
inline("make sure assignOffsetStore can be fully recovered", "确保 assignOffsetStore 可完整恢复")

# --- MessageStore inline ---
inline("The following interfaces are used for duplication mode", "以下接口用于副本/复制模式")

# Method name heuristics for methods without javadoc
METHOD_HEURISTIC: dict[str, str] = {
    "getServiceName": "返回后台线程服务名",
    "run": "后台线程主循环",
    "shutdown": "关闭并释放资源",
    "start": "启动服务",
    "load": "加载持久化数据",
    "destroy": "销毁并清理文件",
    "flush": "刷盘",
    "wakeup": "唤醒等待线程",
    "changeCurrentState": "切换 HA 连接状态",
    "changeTransferState": "切换传输状态",
    "changeToMasterState": "切换为 Master 状态",
    "changeToSlaveState": "切换为 Slave 状态",
    "processReadEvent": "处理读事件",
    "processWriteEvent": "处理写事件",
    "transferData": "传输 CommitLog 数据",
    "reportSlaveMaxOffset": "上报 Slave 最大 offset",
    "readSocket": "从 Socket 读取数据",
    "writeSocket": "向 Socket 写入数据",
    "close": "关闭连接",
    "handshake": "执行 HA 握手",
    "notifyTransferSome": "通知已传输部分数据",
    "putMessagePositionInfo": "写入索引位置信息",
    "buildIndex": "构建消息索引",
    "deleteExpiredFile": "删除过期索引文件",
    "destroy": "销毁索引服务",
    "recover": "恢复 CompactionLog",
    "compact": "执行 Compaction",
    "appendMessage": "追加消息到 CompactionLog",
    "getData": "读取映射文件数据",
    "appendMessage": "追加消息",
    "selectMappedBuffer": "选择映射缓冲区",
    "wrotePosition": "返回已写入位置",
    "getFileFromOffset": "按 offset 获取文件起始 offset",
    "renameTo": "重命名映射文件",
    "swapMap": "交换 mmap 映射",
    "hold": "持有引用计数",
    "release": "释放引用计数",
    "isAvailable": "是否可用",
    "getReadPosition": "返回可读位置",
    "truncateOffset": "截断到指定 offset",
}


def translate_jdoc_block(jdoc: str) -> str | None:
    for sub, (_, cn) in FULL_JDOC.items():
        if sub in jdoc:
            indent = re.match(r"(\s*)/\*\*", jdoc)
            ind = indent.group(1) if indent else "    "
            return f"{ind}/**{cn}\n{ind} */"
    result = jdoc
    changed = False
    for en, cn in DESC_MAP.items():
        if en in result:
            result = result.replace(en, cn, 1)
            changed = True
    for en, cn in PARAM_RETURN.items():
        if en in result:
            result = result.replace(en, cn)
            changed = True
    return result if changed and result != jdoc else None


def class_name_from_path(rel: str) -> str:
    return Path(rel).stem


def gen_pairs(rel: str, text: str) -> list[tuple[str, str]]:
    pairs: list[tuple[str, str]] = []
    cls = class_name_from_path(rel)

    for jdoc in re.findall(r"/\*\*.*?\*/", text, re.DOTALL):
        new_jdoc = translate_jdoc_block(jdoc)
        if new_jdoc and new_jdoc != jdoc:
            pairs.append((jdoc, new_jdoc))

    for m in re.finditer(
        r"^((?:public|protected|private)\s+(?:abstract\s+)?(?:class|interface|enum)\s+"
        + re.escape(cls)
        + r"\b[^{]*\{)",
        text,
        re.MULTILINE,
    ):
        old = m.group(1)
        if cls in CLASS_DESC:
            cn = CLASS_DESC[cls]
            marker = f"/**\n * {cn}"
            if marker not in text:
                pairs.append((old, f"/**\n * {cn}\n */\n{old}"))

    for en, cn in INLINE_MAP.items():
        pat = rf"^(\s*)//\s*{re.escape(en)}\s*$"
        for m in re.finditer(pat, text, re.MULTILINE):
            indent = m.group(1)
            line = m.group(0)
            if f"/** {cn}" not in text:
                pairs.append((line, f"{indent}/** {cn} */"))

    # Logger fields
    for pat, repl in [
        (r"    private static final Logger log = LoggerFactory.getLogger\(LoggerName\.STORE_LOGGER_NAME\);",
         "    /** 存储模块日志。 */\n    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);"),
        (r"    private static final Logger LOGGER = LoggerFactory.getLogger\(LoggerName\.STORE_LOGGER_NAME\);",
         "    /** 存储模块日志。 */\n    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);"),
    ]:
        if re.search(pat, text) and repl not in text:
            m = re.search(pat, text)
            if m and "/**" not in text[max(0, m.start() - 60) : m.start()]:
                pairs.append((m.group(0), repl))

    # Key fields per class
    FIELD_PATTERNS: list[tuple[str, str]] = [
        (r"    public static final String MULTI_PATH_SPLITTER =", "    /** 多路径 CommitLog 分隔符。 */\n    public static final String MULTI_PATH_SPLITTER ="),
        (r"    private static final int FREQUENCY_OF_SAMPLING = 1000;", "    /** 采样频率（毫秒）。 */\n    private static final int FREQUENCY_OF_SAMPLING = 1000;"),
        (r"    private static final int MAX_RECORDS_OF_SAMPLING = 60 \* 10;", "    /** 采样窗口最大记录数（10 分钟）。 */\n    private static final int MAX_RECORDS_OF_SAMPLING = 60 * 10;"),
        (r"    public static final int REPORT_HEADER_SIZE = 8;", "    /** 上报头大小（8 字节 slaveMaxOffset）。 */\n    public static final int REPORT_HEADER_SIZE = 8;"),
        (r"    private static final int READ_MAX_BUFFER_SIZE = 1024 \* 1024 \* 4;", "    /** 读缓冲上限（4MB）。 */\n    private static final int READ_MAX_BUFFER_SIZE = 1024 * 1024 * 4;"),
        (r"    private final AtomicReference<String> masterHaAddress =", "    /** Master HA 地址（原子引用）。 */\n    private final AtomicReference<String> masterHaAddress ="),
        (r"    private volatile HAConnectionState currentState =", "    /** 当前 HA 连接状态。 */\n    private volatile HAConnectionState currentState ="),
        (r"    private DefaultMessageStore defaultMessageStore;", "    /** 所属 DefaultMessageStore。 */\n    private DefaultMessageStore defaultMessageStore;"),
        (r"    protected MessageStore next;", "    /** 插件链下一层 MessageStore。 */\n    protected MessageStore next;"),
        (r"    protected MessageStoreConfig messageStoreConfig;", "    /** 消息存储配置。 */\n    protected MessageStoreConfig messageStoreConfig;"),
        (r"    public static final int BCQ_STORE_UNIT_SIZE =", "    /** BCQ 单条存储单元大小。 */\n    public static final int BCQ_STORE_UNIT_SIZE ="),
        (r"    private static final int MAX_INDEX_NUM =", "    /** 单 IndexFile 最大索引条数。 */\n    private static final int MAX_INDEX_NUM ="),
        (r"    private static final int TRY_CREATE_INDEX_FILE_TIMES =", "    /** 创建 IndexFile 最大重试次数。 */\n    private static final int TRY_CREATE_INDEX_FILE_TIMES ="),
    ]
    for pat, repl in FIELD_PATTERNS:
        if re.search(pat, text) and repl not in text:
            m = re.search(pat, text)
            if m and "/**" not in text[max(0, m.start() - 60) : m.start()]:
                pairs.append((m.group(0), repl))

    # Methods without javadoc - heuristic
    for m in re.finditer(
        r"^\s+(public|protected)\s+(?:static\s+)?(?:[\w<>,\s\[\]?]+\s+)+(\w+)\s*\(",
        text,
        re.MULTILINE,
    ):
        method_name = m.group(2)
        if method_name not in METHOD_HEURISTIC:
            continue
        start = m.start()
        if "/**" in text[max(0, start - 80) : start]:
            continue
        sig_start = m.group(0).lstrip()
        indent = m.group(0)[: len(m.group(0)) - len(sig_start)]
        old_line_start = text.rfind("\n", 0, start) + 1
        old = text[old_line_start : m.end()].rstrip()
        cn = METHOD_HEURISTIC[method_name]
        pairs.append((old, f"{indent}/** {cn}。 */\n{old}"))

    # AbstractPluginMessageStore - delegate pattern comment on class methods
    if cls == "AbstractPluginMessageStore":
        for m in re.finditer(r"    public (\w[\w<>,\s\[\]?]* )(\w+)\(([^)]*)\)", text):
            name = m.group(2)
            if name.startswith("get") or name.startswith("is") or name in ("load", "start", "shutdown", "destroy"):
                start = m.start()
                if "/**" not in text[max(0, start - 60) : start]:
                    sig = m.group(0)
                    cn = f"委托 next 执行 {name}"
                    pairs.append((sig, f"    /** {cn}。 */\n{sig}"))

    # Dedupe - longer first
    seen_old: set[str] = set()
    unique: list[tuple[str, str]] = []
    for old, new in sorted(pairs, key=lambda x: -len(x[0])):
        if old not in seen_old and old in text:
            seen_old.add(old)
            unique.append((old, new))
    return unique


def main() -> None:
    all_r: dict[str, list[tuple[str, str]]] = {}
    total = 0
    for rel in FILES:
        text = (ORIG / rel).read_text(encoding="utf-8")
        pairs = gen_pairs(rel, text)
        all_r[rel] = pairs
        total += len(pairs)
        print(f"{rel}: {len(pairs)} pairs")

    lines = [
        '"""Chinese JavaDoc replacements for RocketMQ wave50a store [0:15]."""',
        "",
        "R: dict[str, list[tuple[str, str]]] = {",
    ]
    for rel, pairs in all_r.items():
        lines.append(f"    {rel!r}: [")
        for old, new in pairs:
            lines.append(f"        ({old!r},")
            lines.append(f"         {new!r}),")
        lines.append("    ],")
    lines.append("}")
    lines.append("")
    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {OUT} total_pairs={total}")


if __name__ == "__main__":
    main()
