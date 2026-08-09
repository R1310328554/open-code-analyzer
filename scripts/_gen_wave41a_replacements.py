#!/usr/bin/env python3
"""Generate wave41a_replacements_rocketmq_store_timer_tieredstore.py."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "rocketmq/rocketmq-all-5.5.0/original"
OUT = ROOT / "scripts/wave41a_replacements_rocketmq_store_timer_tieredstore.py"
FILES = [
    ln.strip()
    for ln in Path("/tmp/rmq41a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FULL_JDOC: dict[str, tuple[str, str]] = {}
DESC_MAP: dict[str, str] = {}
CLASS_DESC: dict[str, str] = {}


def full(sub: str, cn_body: str) -> None:
    FULL_JDOC[sub] = (sub, cn_body)


def t(en: str, cn: str) -> None:
    DESC_MAP[en.strip()] = cn.strip()


# --- TimerWheel ---
full(
    "Perform backup operation.",
    "\n * 执行时间轮快照备份：按 flag 选择快照文件，写入临时文件后原子重命名。\n * @param flushWhere 用于选择快照文件的 flag\n * @throws IOException 备份过程 I/O 异常\n",
)
full(
    "Select snapshot file name based on flag.",
    "\n * 根据 flag 选择快照文件名。\n * @param flag 快照标识 flag\n * @return 快照文件路径\n",
)
full(
    "Clean up expired snapshot files.",
    "\n * 清理过期快照：删除 flag 较小的快照文件，保留 flag 最大的两个。\n",
)
full(
    "Get the maximum flag from existing snapshot files.",
    "\n * 获取已有快照文件中的最大 flag。\n * @return 最大 flag，无快照时返回 -1\n",
)
full(
    "Wrapper class for file and flag",
    "\n * 快照文件与 flag 的包装类\n",
)
CLASS_DESC["TimerWheel"] = "定时消息时间轮：基于 mmap/堆外缓冲管理槽位，支持快照备份与恢复。"

# --- TimerRocksDBRecord ---
CLASS_DESC["TimerRocksDBRecord"] = "Timer RocksDB 键值记录：序列化延迟时间、唯一键与 CommitLog 物理位置。"

# --- TransMessageRocksDBStore ---
CLASS_DESC["TransMessageRocksDBStore"] = "基于 RocksDB 的事务消息存储：从 CommitLog 分发构建事务索引并异步写入 RocksDB。"

# --- TransRocksDBRecord ---
CLASS_DESC["TransRocksDBRecord"] = "事务 RocksDB 记录：编码 topic、uniqKey、物理偏移与回查次数。"

# --- LibC ---
CLASS_DESC["LibC"] = "JNA 封装的 libc 接口：提供 mlock、madvise、msync 等内存映射系统调用。"
t("sync memory asynchronously", "异步同步内存到磁盘。")
t("invalidate mappings & caches", "使映射与缓存失效。")
t("synchronous memory sync", "同步刷盘内存映射。")

# --- PerfCounter ---
CLASS_DESC["PerfCounter"] = "延迟分布性能计数器：按耗时桶统计 TPS 与 TP 分位值。"
CLASS_DESC["Ticks"] = "多 key 性能采样后台线程：聚合 PerfCounter 并定期清理过期 key。"

# --- MessageStoreExecutor ---
CLASS_DESC["MessageStoreExecutor"] = "分层存储线程池单例：管理缓冲提交/拉取与文件回收执行器。"

# --- AppendResult ---
t("The append operation was successful.", "追加写入成功。")
t("The buffer used for the append operation is full.", "追加缓冲区已满。")
t("The file is full and cannot accept more data.", "文件已满，无法继续写入。")
t("The file is closed and cannot accept more data.", "文件已关闭，无法接受数据。")
t("An unknown error occurred during the append operation.", "追加过程中发生未知错误。")

# --- FileSegmentType ---
CLASS_DESC["FileSegmentType"] = "分层存储文件段类型：CommitLog、ConsumeQueue 或 Index。"

# --- GetMessageResultExt ---
CLASS_DESC["GetMessageResultExt"] = "扩展拉取结果：携带 tagCode 列表，支持对象存储顺序读取后的过滤。"
full(
    "Due to the message fetched from the object storage is sequential,",
    "\n * 对象存储按序拉取消息，过滤在数据读取完成后执行。\n * @param messageFilter 消息过滤器\n * @return 过滤后的拉取结果\n",
)

# --- GroupCommitContext ---
CLASS_DESC["GroupCommitContext"] = "分层存储组提交上下文：聚合待上传缓冲与 DispatchRequest。"

# --- SelectBufferResult ---
CLASS_DESC["SelectBufferResult"] = "分层存储选缓冲结果：封装 ByteBuffer、偏移、大小与 tagCode。"

# --- MessageStoreDispatcher ---
CLASS_DESC["MessageStoreDispatcher"] = "分层存储 CommitLog 分发器：调度异步上传与组提交。"

# --- MessageStoreDispatcherImpl ---
full(
    "Building indexes with offsetId is no longer supported because offsetId has changed in tiered storage",
    "\n * 分层存储中 offsetId 已变更，不再支持基于 offsetId 建索引。\n * @param topicId Topic 数值 ID\n * @param request 分发请求\n",
)
CLASS_DESC["MessageStoreDispatcherImpl"] = "分层存储分发实现：扫描本地 CQ 并组提交到对象存储，失败时缓存上下文。"

# --- MessageStoreFetcher ---
t(
    "Asynchronous get the store time of the earliest message in this store.",
    "异步获取该队列最早消息的存储时间戳。",
)
t(
    "Asynchronous get the store time of the message specified.",
    "异步获取指定消费队列偏移对应消息的存储时间戳。",
)
t("Message topic.", "消息 Topic。")
t("Queue ID.", "队列 ID。")
t("Consume queue offset.", "消费队列逻辑偏移。")
t("store timestamp of the message.", "消息存储时间戳。")
t(
    "Look up the physical offset of the message whose store timestamp is as specified.",
    "按存储时间戳查找匹配的消费队列偏移。",
)
t("Topic of the message.", "消息 Topic。")
t("Timestamp to look up.", "待查找的时间戳。")
t("physical offset which matches.", "匹配的消费队列偏移。")
t("Asynchronous get message", "异步拉取消息。")
t("Consumer group that launches this query.", "发起查询的消费者组。")
t("Topic to query.", "待查询 Topic。")
t("Queue ID to query.", "待查询队列 ID。")
t("Logical offset to start from.", "起始逻辑偏移。")
t("Maximum count of messages to query.", "最多拉取消息条数。")
t("Message filter used to screen desired messages.", "消息过滤器。")
t("Matched messages.", "匹配的消息结果。")
t("Asynchronous query messages by given key.", "按 key 异步查询消息。")
t("Message key.", "消息 Key。")
t("Maximum count of the messages possible.", "最多返回消息条数。")
t("Begin timestamp.", "起始时间戳。")
t("End timestamp.", "结束时间戳。")
CLASS_DESC["MessageStoreFetcher"] = "分层存储异步消息拉取接口：支持按时间/偏移查询与 key 检索。"


def translate_javadoc_block(block: str) -> str | None:
    inner = block[3:-2]
    lines = inner.split("\n")
    out_lines: list[str] = []
    changed = False
    for line in lines:
        stripped = line.strip().lstrip("*").strip()
        if not stripped:
            out_lines.append(line)
            continue
        if stripped.startswith("@param"):
            parts = stripped.split(None, 2)
            if len(parts) >= 2:
                param = parts[1]
                param_cn = {
                    "flushWhere": "用于选择快照文件的 flag",
                    "flag": "快照标识 flag",
                    "timerWheelPath": "时间轮文件路径",
                    "messageFilter": "消息过滤器",
                    "group": "消费者组",
                    "topic": "Topic",
                    "queueId": "队列 ID",
                    "consumeQueueOffset": "消费队列偏移",
                    "timestamp": "时间戳",
                    "type": "边界类型",
                    "offset": "起始逻辑偏移",
                    "maxCount": "最大条数",
                    "key": "消息 Key",
                    "begin": "起始时间戳",
                    "end": "结束时间戳",
                }.get(param, param)
                desc = parts[2] if len(parts) > 2 else ""
                for en, cn in DESC_MAP.items():
                    if en in desc:
                        desc = cn
                        changed = True
                        break
                if desc:
                    out_lines.append(f"     * @param {param} {desc}")
                    changed = True
                else:
                    out_lines.append(f"     * @param {param} {param_cn}")
                    changed = True
            else:
                out_lines.append(line)
        elif stripped.startswith("@return"):
            ret = stripped[7:].strip()
            ret_cn = {
                "timestamp of the earliest message in this store.": "该 Store 中最早消息存储时间戳",
                "store timestamp of the message.": "消息存储时间戳",
                "physical offset which matches.": "匹配的消费队列偏移",
                "Matched messages.": "匹配的消息结果",
                "Name of the snapshot file.": "快照文件路径",
                "The maximum flag value, or -1 if no snapshot files exist": "最大 flag，无快照时返回 -1",
            }.get(ret, ret)
            if ret in DESC_MAP:
                ret_cn = DESC_MAP[ret]
            if ret_cn != ret:
                changed = True
            out_lines.append(f"     * @return {ret_cn}")
        elif stripped.startswith("@throws"):
            out_lines.append(line.replace("If I/O error occurs during backup process.", "备份过程 I/O 异常"))
            changed = True
        else:
            replaced = stripped
            for en, cn in sorted(DESC_MAP.items(), key=lambda x: -len(x[0])):
                if en in replaced:
                    replaced = replaced.replace(en, cn)
                    changed = True
            for sub, (_, cn_body) in FULL_JDOC.items():
                if sub in replaced:
                    replaced = cn_body.strip().lstrip("*").strip()
                    changed = True
                    break
            prefix = line[: line.index("*") + 1] if "*" in line else "     *"
            out_lines.append(f"{prefix} {replaced}")
    if not changed:
        return None
    return "/**\n" + "\n".join(out_lines) + "\n     */"


def gen_pairs(rel: str, text: str) -> list[tuple[str, str]]:
    pairs: list[tuple[str, str]] = []

    for m in re.finditer(r"/\*\*.*?\*/", text, re.DOTALL):
        block = m.group(0)
        if re.search(r"[\u4e00-\u9fff]", block):
            continue
        new_block = translate_javadoc_block(block)
        if new_block and new_block != block:
            pairs.append((block, new_block))

    for sub, (_, cn_body) in FULL_JDOC.items():
        for m in re.finditer(r"/\*\*[^*]*?" + re.escape(sub) + r"[^*]*?\*/", text, re.DOTALL):
            old = m.group(0)
            if re.search(r"[\u4e00-\u9fff]", old):
                continue
            star = old.index("*", 2)
            prefix = old[: star + 1]
            new_block = prefix + cn_body + "\n     */"
            pairs.append((old, new_block))

    for cls, desc in CLASS_DESC.items():
        pat = rf"public (?:class|interface|enum) {cls}"
        m = re.search(pat, text)
        if m and f" * {desc}" not in text:
            old = m.group(0)
            pairs.append((old, f"/**\n * {desc}\n */\n{old}"))

    FIELD_PATTERNS: list[tuple[str, str]] = [
        (r"    private static final Logger log =", "    /** 存储模块日志。 */\n    private static final Logger log ="),
        (r"    private static final Logger logError =", "    /** 存储错误日志。 */\n    private static final Logger logError ="),
        (r"    public static final String TIMER_WHEEL_FILE_NAME =", "    /** 时间轮文件名前缀。 */\n    public static final String TIMER_WHEEL_FILE_NAME ="),
        (r"    public static final int BLANK = -1, IGNORE = -2;", "    /** 槽位空白/忽略占位常量。 */\n    public static final int BLANK = -1, IGNORE = -2;"),
        (r"    public final int slotsTotal;", "    /** 时间轮槽位总数（实际索引为 2 倍）。 */\n    public final int slotsTotal;"),
        (r"    public final int precisionMs;", "    /** 槽位时间精度（毫秒）。 */\n    public final int precisionMs;"),
        (r"    private final MappedByteBuffer mappedByteBuffer;", "    /** mmap 映射的时间轮文件缓冲。 */\n    private final MappedByteBuffer mappedByteBuffer;"),
        (r"    private final ByteBuffer byteBuffer;", "    /** 堆外 Direct 缓冲，读写槽位数据。 */\n    private final ByteBuffer byteBuffer;"),
        (r"    public static final byte TIMER_ROCKSDB_PUT =", "    /** RocksDB 写入操作标志。 */\n    public static final byte TIMER_ROCKSDB_PUT ="),
        (r"    public static final byte TIMER_ROCKSDB_DELETE =", "    /** RocksDB 删除操作标志。 */\n    public static final byte TIMER_ROCKSDB_DELETE ="),
        (r"    public static final byte TIMER_ROCKSDB_UPDATE =", "    /** RocksDB 更新操作标志。 */\n    public static final byte TIMER_ROCKSDB_UPDATE ="),
        (r"    private long delayTime;", "    /** 延迟触发时间戳。 */\n    private long delayTime;"),
        (r"    private String uniqKey;", "    /** 定时消息唯一键。 */\n    private String uniqKey;"),
        (r"    private int sizePy;", "    /** CommitLog 消息体大小。 */\n    private int sizePy;"),
        (r"    private long offsetPy;", "    /** CommitLog 物理偏移。 */\n    private long offsetPy;"),
        (r"    private long queueOffset;", "    /** 消费队列逻辑偏移。 */\n    private long queueOffset;"),
        (r"    private long checkPoint;", "    /** 检查点偏移。 */\n    private long checkPoint;"),
        (r"    private byte actionFlag;", "    /** 操作类型标志（put/delete/update）。 */\n    private byte actionFlag;"),
        (r"    private MessageExt messageExt;", "    /** 关联的消息体（可选）。 */\n    private MessageExt messageExt;"),
        (r"    private static final String REMOVE_TAG =", "    /** 删除半事务消息的操作 Tag。 */\n    private static final String REMOVE_TAG ="),
        (r"    private static final int DEFAULT_CAPACITY =", "    /** 事务索引构建队列默认容量。 */\n    private static final int DEFAULT_CAPACITY ="),
        (r"    private static final int BATCH_SIZE =", "    /** 批量写入 RocksDB 的批大小。 */\n    private static final int BATCH_SIZE ="),
        (r"    protected BlockingQueue<TransRocksDBRecord> originTransMsgQueue;", "    /** 待构建事务索引的记录队列。 */\n    protected BlockingQueue<TransRocksDBRecord> originTransMsgQueue;"),
        (r"    public static final int VALUE_LENGTH =", "    /** RocksDB value 固定长度。 */\n    public static final int VALUE_LENGTH ="),
        (r"    private static final String KEY_SPLIT =", "    /** 事务 key 中 topic 与 uniqKey 分隔符。 */\n    private static final String KEY_SPLIT ="),
        (r"    private int checkTimes = 0;", "    /** 事务回查次数。 */\n    private int checkTimes = 0;"),
        (r"    private boolean isOp;", "    /** 是否为操作（op）半消息。 */\n    private boolean isOp;"),
        (r"    private boolean delete;", "    /** 是否标记删除。 */\n    private boolean delete;"),
        (r"    int MADV_NORMAL = 0;", "    /** madvise 正常访问模式。 */\n    int MADV_NORMAL = 0;"),
        (r"    int MADV_RANDOM = 1;", "    /** madvise 随机访问模式。 */\n    int MADV_RANDOM = 1;"),
        (r"    int MADV_WILLNEED = 3;", "    /** madvise 预读提示。 */\n    int MADV_WILLNEED = 3;"),
        (r"    int MADV_DONTNEED = 4;", "    /** madvise 释放页缓存提示。 */\n    int MADV_DONTNEED = 4;"),
        (r"    int MS_SYNC = 0x0004;", "    /** msync 同步刷盘标志。 */\n    int MS_SYNC = 0x0004;"),
        (r"    LibC INSTANCE =", "    /** libc 单例实例。 */\n    LibC INSTANCE ="),
        (r"    private final AtomicInteger\[\] count;", "    /** 按耗时索引的计数桶数组。 */\n    private final AtomicInteger[] count;"),
        (r"    private final AtomicLong allCount;", "    /** 当前窗口累计计数。 */\n    private final AtomicLong allCount;"),
        (r"    private String prefix = \"DEFAULT\";", "    /** 日志前缀标识。 */\n    private String prefix = \"DEFAULT\";"),
        (r"    public final BlockingQueue<Runnable> bufferCommitThreadPoolQueue;", "    /** 缓冲提交线程池任务队列。 */\n    public final BlockingQueue<Runnable> bufferCommitThreadPoolQueue;"),
        (r"    public final ExecutorService bufferCommitExecutor;", "    /** 缓冲提交线程池。 */\n    public final ExecutorService bufferCommitExecutor;"),
        (r"    public final ScheduledExecutorService commonExecutor;", "    /** 分层存储通用调度线程池。 */\n    public final ScheduledExecutorService commonExecutor;"),
        (r"    private final List<Long> tagCodeList;", "    /** 与消息列表对应的 Tag 哈希码。 */\n    private final List<Long> tagCodeList;"),
        (r"    private long endOffset;", "    /** 组提交结束的消费队列偏移。 */\n    private long endOffset;"),
        (r"    private List<SelectMappedBufferResult> bufferList;", "    /** 待释放的映射缓冲列表。 */\n    private List<SelectMappedBufferResult> bufferList;"),
        (r"    private List<DispatchRequest> dispatchRequests;", "    /** 组提交产生的 DispatchRequest 列表。 */\n    private List<DispatchRequest> dispatchRequests;"),
        (r"    private final ByteBuffer byteBuffer;", "    /** 消息体 ByteBuffer。 */\n    private final ByteBuffer byteBuffer;"),
        (r"    private final long tagCode;", "    /** 消息 Tag 哈希码。 */\n    private final long tagCode;"),
        (r"    protected final TieredMessageStore messageStore;", "    /** 所属分层 MessageStore。 */\n    protected final TieredMessageStore messageStore;"),
        (r"    protected final FlatFileStore flatFileStore;", "    /** 扁平文件存储。 */\n    protected final FlatFileStore flatFileStore;"),
        (r"    protected final Semaphore semaphore;", "    /** 限制并发分发任务数。 */\n    protected final Semaphore semaphore;"),
        (r"    protected final Map<FlatFileInterface, GroupCommitContext> failedGroupCommitMap;", "    /** 组提交失败时缓存的上下文。 */\n    protected final Map<FlatFileInterface, GroupCommitContext> failedGroupCommitMap;"),
        (r"    COMMIT_LOG\(0\),", "    /** CommitLog 文件段。 */\n    COMMIT_LOG(0),"),
        (r"    CONSUME_QUEUE\(1\),", "    /** ConsumeQueue 文件段。 */\n    CONSUME_QUEUE(1),"),
        (r"    INDEX\(2\);", "    /** Index 文件段。 */\n    INDEX(2);"),
        (r"    SUCCESS,", "    /** 追加写入成功。 */\n    SUCCESS,"),
        (r"    BUFFER_FULL,", "    /** 追加缓冲区已满。 */\n    BUFFER_FULL,"),
        (r"    FILE_FULL,", "    /** 文件已满。 */\n    FILE_FULL,"),
        (r"    FILE_CLOSED,", "    /** 文件已关闭。 */\n    FILE_CLOSED,"),
        (r"    UNKNOWN_ERROR", "    /** 未知错误。 */\n    UNKNOWN_ERROR"),
    ]

    for pat, repl in FIELD_PATTERNS:
        if re.search(pat, text) and repl not in text:
            for m in re.finditer(pat, text):
                if "/**" not in text[max(0, m.start() - 60) : m.start()]:
                    pairs.append((m.group(0), repl))
                    break

    METHOD_CN: dict[str, str] = {
        "public TimerWheel(String fileName, int slotsTotal, int precisionMs)": "    /** 构造时间轮（无快照偏移）。 */\n    public TimerWheel(String fileName, int slotsTotal, int precisionMs)",
        "public void shutdown() {": "    /** 关闭时间轮并刷盘。 */\n    public void shutdown() {",
        "public void shutdown(boolean flush) {": "    /** 关闭时间轮，可选是否刷盘。 */\n    public void shutdown(boolean flush) {",
        "public void flush() {": "    /** 将 Direct 缓冲变更同步到 mmap 并 force。 */\n    public void flush() {",
        "public void backup(long flushWhere)": "    /** 按 flag 备份时间轮到快照文件。 */\n    public void backup(long flushWhere)",
        "public void cleanExpiredSnapshot() {": "    /** 清理过期快照文件。 */\n    public void cleanExpiredSnapshot() {",
        "public static long getMaxSnapshotFlag(String timerWheelPath)": "    /** 返回时间轮目录下最大快照 flag。 */\n    public static long getMaxSnapshotFlag(String timerWheelPath)",
        "public Slot getSlot(long timeMs) {": "    /** 按毫秒时间获取槽位（精度对齐）。 */\n    public Slot getSlot(long timeMs) {",
        "public Slot getRawSlot(long timeMs) {": "    /** 读取原始槽位数据（测试用）。 */\n    public Slot getRawSlot(long timeMs) {",
        "public int getSlotIndex(long timeMs) {": "    /** 计算时间对应的槽位索引。 */\n    public int getSlotIndex(long timeMs) {",
        "public void putSlot(long timeMs, long firstPos, long lastPos) {": "    /** 写入槽位首尾物理偏移。 */\n    public void putSlot(long timeMs, long firstPos, long lastPos) {",
        "public void putSlot(long timeMs, long firstPos, long lastPos, int num, int magic) {": "    /** 写入槽位及计数与魔数。 */\n    public void putSlot(long timeMs, long firstPos, long lastPos, int num, int magic) {",
        "public void reviseSlot(long timeMs, long firstPos, long lastPos, boolean force) {": "    /** 修正槽位偏移，可选强制覆盖。 */\n    public void reviseSlot(long timeMs, long firstPos, long lastPos, boolean force) {",
        "public long checkPhyPos(long timeStartMs, long maxOffset) {": "    /** 检查时间轮存储偏移是否超过 TimerLog 最大偏移。 */\n    public long checkPhyPos(long timeStartMs, long maxOffset) {",
        "public long getNum(long timeMs) {": "    /** 返回指定时间槽位的消息计数。 */\n    public long getNum(long timeMs) {",
        "public long getAllNum(long timeStartMs) {": "    /** 统计从起始时间起的槽位消息总数。 */\n    public long getAllNum(long timeStartMs) {",
        "public String getFileName() {": "    /** 返回时间轮文件路径。 */\n    public String getFileName() {",
        "public byte[] getKeyBytes() {": "    /** 序列化为 RocksDB key 字节。 */\n    public byte[] getKeyBytes() {",
        "public byte[] getValueBytes() {": "    /** 序列化为 RocksDB value 字节。 */\n    public byte[] getValueBytes() {",
        "public static TimerRocksDBRecord decode(byte[] key, byte[] value) {": "    /** 从 key/value 解码 Timer 记录。 */\n    public static TimerRocksDBRecord decode(byte[] key, byte[] value) {",
        "public void shutdown() {": "    /** 关闭事务 RocksDB 索引构建服务。 */\n    public void shutdown() {",
        "public void buildTransIndex(DispatchRequest dispatchRequest) {": "    /** 从 CommitLog 分发请求构建事务索引记录。 */\n    public void buildTransIndex(DispatchRequest dispatchRequest) {",
        "public void deletePrepareMessage(MessageExt messageExt) {": "    /** 写入 op 半消息删除 prepare 消息。 */\n    public void deletePrepareMessage(MessageExt messageExt) {",
        "public MessageExt getMessage(long offsetPy, int sizePy) {": "    /** 按物理偏移与大小读取 CommitLog 消息。 */\n    public MessageExt getMessage(long offsetPy, int sizePy) {",
        "public Integer getCheckTimes(String topic, String uniqKey, Long offsetPy) {": "    /** 查询事务消息回查次数。 */\n    public Integer getCheckTimes(String topic, String uniqKey, Long offsetPy)",
        "public static TransRocksDBRecord decode(byte[] key, byte[] value) {": "    /** 从 key/value 解码事务记录。 */\n    public static TransRocksDBRecord decode(byte[] key, byte[] value) {",
        "public float getLastTps() {": "    /** 返回最近一次统计窗口 TPS。 */\n    public float getLastTps() {",
        "public void flow(long cost) {": "    /** 记录一次耗时样本。 */\n    public void flow(long cost) {",
        "public void flow(long cost, int num) {": "    /** 记录指定数量的耗时样本。 */\n    public void flow(long cost, int num) {",
        "public void print() {": "    /** 输出 TP 分位与延迟分布统计。 */\n    public void print() {",
        "public void reset() {": "    /** 重置计数桶与窗口。 */\n    public void reset() {",
        "public void startTick() {": "    /** 开始计时采样。 */\n    public void startTick() {",
        "public void endTick() {": "    /** 结束计时并记录耗时。 */\n    public void endTick() {",
        "public static MessageStoreExecutor getInstance() {": "    /** 返回单例执行器。 */\n    public static MessageStoreExecutor getInstance() {",
        "public void shutdown() {": "    /** 关闭全部线程池。 */\n    public void shutdown() {",
        "public int getCode() {": "    /** 返回文件段类型编码。 */\n    public int getCode() {",
        "public static FileSegmentType valueOf(int fileType) {": "    /** 按编码解析文件段类型。 */\n    public static FileSegmentType valueOf(int fileType)",
        "public void addMessageExt(SelectMappedBufferResult bufferResult, long queueOffset, long tagCode) {": "    /** 添加消息并记录 tagCode。 */\n    public void addMessageExt(SelectMappedBufferResult bufferResult, long queueOffset, long tagCode) {",
        "public List<Long> getTagCodeList() {": "    /** 返回 tagCode 列表。 */\n    public List<Long> getTagCodeList() {",
        "public GetMessageResult doFilterMessage(MessageFilter messageFilter) {": "    /** 对顺序拉取结果执行 CQ/CommitLog 过滤。 */\n    public GetMessageResult doFilterMessage(MessageFilter messageFilter) {",
        "public long getEndOffset() {": "    /** 返回组提交结束偏移。 */\n    public long getEndOffset() {",
        "public void setEndOffset(long endOffset) {": "    /** 设置组提交结束偏移。 */\n    public void setEndOffset(long endOffset)",
        "public void release() {": "    /** 释放缓冲并清空请求列表。 */\n    public void release() {",
        "public ByteBuffer getByteBuffer() {": "    /** 返回消息 ByteBuffer。 */\n    public ByteBuffer getByteBuffer() {",
        "public long getStartOffset() {": "    /** 返回起始物理偏移。 */\n    public long getStartOffset() {",
        "public int getSize() {": "    /** 返回消息大小。 */\n    public int getSize() {",
        "public long getTagCode() {": "    /** 返回 Tag 哈希码。 */\n    public long getTagCode() {",
        "public AtomicLong getAccessCount() {": "    /** 返回访问计数器。 */\n    public AtomicLong getAccessCount() {",
        "void start();": "    /** 启动分发服务。 */\n    void start();",
        "void shutdown();": "    /** 关闭分发服务。 */\n    void shutdown();",
        "CompletableFuture<Boolean> doScheduleDispatch(FlatFileInterface flatFile, boolean force);": "    /** 调度异步组提交上传。 */\n    CompletableFuture<Boolean> doScheduleDispatch(FlatFileInterface flatFile, boolean force);",
        "public void dispatchWithSemaphore(FlatFileInterface flatFile) {": "    /** 在信号量控制下触发分发。 */\n    public void dispatchWithSemaphore(FlatFileInterface flatFile) {",
        "public void dispatch(DispatchRequest request) {": "    /** 接收 CommitLog 分发并注册 FlatFile。 */\n    public void dispatch(DispatchRequest request) {",
        "public CompletableFuture<Boolean> commitAsync(FlatFileInterface flatFile) {": "    /** 异步提交 FlatFile 到对象存储。 */\n    public CompletableFuture<Boolean> commitAsync(FlatFileInterface flatFile)",
        "public void constructIndexFile(long topicId, GroupCommitContext groupCommitContext) {": "    /** 异步构建索引文件并释放上下文。 */\n    public void constructIndexFile(long topicId, GroupCommitContext groupCommitContext) {",
        "public void constructIndexFile0(long topicId, DispatchRequest request) {": "    /** 为单条 DispatchRequest 写入索引 key。 */\n    public void constructIndexFile0(long topicId, DispatchRequest request) {",
        "public void releaseClosedPendingGroupCommit() {": "    /** 释放已关闭 FlatFile 的挂起组提交上下文。 */\n    public void releaseClosedPendingGroupCommit() {",
        "CompletableFuture<Long> getEarliestMessageTimeAsync(String topic, int queueId);": "    /** 异步获取最早消息存储时间。 */\n    CompletableFuture<Long> getEarliestMessageTimeAsync(String topic, int queueId);",
        "CompletableFuture<GetMessageResult> getMessageAsync(": "    /** 异步拉取消息。 */\n    CompletableFuture<GetMessageResult> getMessageAsync(",
        "CompletableFuture<QueryMessageResult> queryMessageAsync(": "    /** 按 key 异步查询消息。 */\n    CompletableFuture<QueryMessageResult> queryMessageAsync(",
        "public int mlock(Pointer var1, NativeLong var2);": "    /** 锁定内存页，防止换出。 */\n    int mlock(Pointer var1, NativeLong var2);",
        "public int madvise(Pointer var1, NativeLong var2, int var3);": "    /** 向内核提示内存访问模式。 */\n    int madvise(Pointer var1, NativeLong var2, int var3);",
        "public int msync(Pointer p, NativeLong length, int flags);": "    /** 同步 mmap 内存到磁盘。 */\n    int msync(Pointer p, NativeLong length, int flags);",
        "public int getpagesize();": "    /** 返回系统页大小。 */\n    int getpagesize();",
    }

    for old_frag, new_frag in METHOD_CN.items():
        if old_frag in text and new_frag not in text:
            if "/**" not in text[max(0, text.index(old_frag) - 50) : text.index(old_frag)]:
                pairs.append((old_frag, new_frag))

    # LibC inline comments
    for old, new in [
        ("    /* sync memory asynchronously */", "    /** 异步同步内存到磁盘。 */\n    /* sync memory asynchronously */"),
        ("    /* invalidate mappings & caches */", "    /** 使映射与缓存失效。 */\n    /* invalidate mappings & caches */"),
        ("    /* synchronous memory sync */", "    /** 同步刷盘内存映射。 */\n    /* synchronous memory sync */"),
    ]:
        if old in text and new not in text:
            pairs.append((old, new))

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
        if not pairs:
            print(f"WARN no pairs: {rel}")
        all_r[rel] = pairs
        total += len(pairs)
        print(f"{rel}: {len(pairs)} pairs")

    lines = [
        '"""Chinese JavaDoc replacements for RocketMQ wave41a store/timer/tieredstore [0:15]."""',
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
