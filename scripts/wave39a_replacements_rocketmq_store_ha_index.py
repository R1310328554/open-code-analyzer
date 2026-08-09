"""Chinese JavaDoc replacements for RocketMQ wave39a store/ha/index/kv [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "store/src/main/java/org/apache/rocketmq/store/ha/autoswitch/MetadataFile.java": [
        (
            "public abstract class MetadataFile {",
            "/**\n * 自动切换元数据文件抽象基类：定义编码、持久化与内存清理契约。\n */\npublic abstract class MetadataFile {",
        ),
        (
            "    protected String filePath;",
            "    /** 元数据文件在磁盘上的路径。 */\n    protected String filePath;",
        ),
        (
            "    public abstract String encodeToStr();",
            "    /** 将内存中的元数据编码为字符串。 */\n    public abstract String encodeToStr();",
        ),
        (
            "    public abstract void decodeFromStr(String dataStr);",
            "    /** 从字符串解码并加载元数据到内存。 */\n    public abstract void decodeFromStr(String dataStr);",
        ),
        (
            "    public abstract boolean isLoaded();",
            "    /** 判断元数据是否已成功加载。 */\n    public abstract boolean isLoaded();",
        ),
        (
            "    public abstract void clearInMem();",
            "    /** 清空内存中的元数据字段。 */\n    public abstract void clearInMem();",
        ),
        (
            "    public void writeToFile() throws Exception {",
            "    /** 先删除旧文件，再将编码结果写入磁盘。 */\n    public void writeToFile() throws Exception {",
        ),
        (
            "    public void readFromFile() throws Exception {",
            "    /** 从磁盘读取文件内容并解码到内存。 */\n    public void readFromFile() throws Exception {",
        ),
        (
            "    public boolean fileExists() {",
            "    /** 判断元数据文件是否存在于磁盘。 */\n    public boolean fileExists() {",
        ),
        (
            "    public void clear() {",
            "    /** 清空内存并删除磁盘上的元数据文件。 */\n    public void clear() {",
        ),
        (
            "    public String getFilePath() {",
            "    /** 返回元数据文件路径。 */\n    public String getFilePath() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/autoswitch/TempBrokerMetadata.java": [
        (
            "public class TempBrokerMetadata extends BrokerMetadata {",
            "/**\n * 临时 Broker 元数据：注册阶段持久化集群名、Broker 名、ID 及校验码。\n */\npublic class TempBrokerMetadata extends BrokerMetadata {",
        ),
        (
            "    private String registerCheckCode;",
            "    /** 注册校验码，用于主从自动切换时的身份验证。 */\n    private String registerCheckCode;",
        ),
        (
            "    public TempBrokerMetadata(String filePath) {",
            "    /** 仅指定文件路径的构造，其余字段为空。 */\n    public TempBrokerMetadata(String filePath) {",
        ),
        (
            "    public TempBrokerMetadata(String filePath, String clusterName, String brokerName, Long brokerId, String registerCheckCode) {",
            "    /** 以完整字段初始化临时 Broker 元数据。 */\n    public TempBrokerMetadata(String filePath, String clusterName, String brokerName, Long brokerId, String registerCheckCode) {",
        ),
        (
            "    public void updateAndPersist(String clusterName, String brokerName, Long brokerId, String registerCheckCode) throws Exception {",
            "    /** 更新内存字段并立即持久化到文件。 */\n    public void updateAndPersist(String clusterName, String brokerName, Long brokerId, String registerCheckCode) throws Exception {",
        ),
        (
            "    @Override\n    public String encodeToStr() {",
            "    /** 以 # 分隔符拼接各字段编码为字符串。 */\n    @Override\n    public String encodeToStr() {",
        ),
        (
            "    @Override\n    public void decodeFromStr(String dataStr) {",
            "    /** 从 # 分隔的字符串解析各字段。 */\n    @Override\n    public void decodeFromStr(String dataStr) {",
        ),
        (
            "    @Override\n    public boolean isLoaded() {",
            "    /** 判断基础字段与注册校验码均已加载。 */\n    @Override\n    public boolean isLoaded() {",
        ),
        (
            "    @Override\n    public void clearInMem() {",
            "    /** 清空内存中的注册校验码及父类字段。 */\n    @Override\n    public void clearInMem() {",
        ),
        (
            "    public Long getBrokerId() {",
            "    /** 返回 Broker ID。 */\n    public Long getBrokerId() {",
        ),
        (
            "    public String getRegisterCheckCode() {",
            "    /** 返回注册校验码。 */\n    public String getRegisterCheckCode() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回包含各字段的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/io/AbstractHAReader.java": [
        (
            "public abstract class AbstractHAReader {",
            "/**\n * HA 读操作抽象基类：循环读取 Socket 数据并通过钩子回调。\n */\npublic abstract class AbstractHAReader {",
        ),
        (
            "    protected final List<HAReadHook> readHookList = new ArrayList<>();",
            "    /** 已注册的读钩子列表。 */\n    protected final List<HAReadHook> readHookList = new ArrayList<>();",
        ),
        (
            "    public boolean read(SocketChannel socketChannel, ByteBuffer byteBufferRead) {",
            "    /** 从 Socket 读取数据直至缓冲区满或连续三次零字节。 */\n    public boolean read(SocketChannel socketChannel, ByteBuffer byteBufferRead) {",
        ),
        (
            "    public void registerHook(HAReadHook readHook) {",
            "    /** 注册读完成后的回调钩子。 */\n    public void registerHook(HAReadHook readHook) {",
        ),
        (
            "    public void clearHook() {",
            "    /** 清空所有已注册的读钩子。 */\n    public void clearHook() {",
        ),
        (
            "    /**\n     * Process read result.\n     *\n     * @param byteBufferRead read result\n     * @return true if process succeed, false otherwise\n     */",
            "    /**\n     * 处理读到的字节缓冲区内容。\n     *\n     * @param byteBufferRead 读取结果缓冲区\n     * @return 处理成功返回 true，否则 false\n     */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/io/HAReadHook.java": [
        (
            "public interface HAReadHook {",
            "/**\n * HA 读钩子：每次 Socket 读取完成后触发回调。\n */\npublic interface HAReadHook {",
        ),
        (
            "    void afterRead(int readSize);",
            "    /** 读取完成后调用，参数为本次读取的字节数。 */\n    void afterRead(int readSize);",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/io/HAWriteHook.java": [
        (
            "public interface HAWriteHook {",
            "/**\n * HA 写钩子：每次 Socket 写入完成后触发回调。\n */\npublic interface HAWriteHook {",
        ),
        (
            "    void afterWrite(int writeSize);",
            "    /** 写入完成后调用，参数为本次写入的字节数。 */\n    void afterWrite(int writeSize);",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/io/HAWriter.java": [
        (
            "public class HAWriter {",
            "/**\n * HA 写操作封装：循环写入 Socket 并支持写钩子回调。\n */\npublic class HAWriter {",
        ),
        (
            "    protected final List<HAWriteHook> writeHookList = new ArrayList<>();",
            "    /** 已注册的写钩子列表。 */\n    protected final List<HAWriteHook> writeHookList = new ArrayList<>();",
        ),
        (
            "    public boolean write(SocketChannel socketChannel, ByteBuffer byteBufferWrite) throws IOException {",
            "    /** 将缓冲区数据写入 Socket，直至全部写完或连续三次零字节。 */\n    public boolean write(SocketChannel socketChannel, ByteBuffer byteBufferWrite) throws IOException {",
        ),
        (
            "    public void registerHook(HAWriteHook writeHook) {",
            "    /** 注册写完成后的回调钩子。 */\n    public void registerHook(HAWriteHook writeHook) {",
        ),
        (
            "    public void clearHook() {",
            "    /** 清空所有已注册的写钩子。 */\n    public void clearHook() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/hook/PutMessageHook.java": [
        (
            "public interface PutMessageHook {",
            "/**\n * 写消息前置钩子：在消息写入 CommitLog 前执行校验或转换。\n */\npublic interface PutMessageHook {",
        ),
        (
            "    /**\n     * Name of the hook.\n     *\n     * @return name of the hook\n     */",
            "    /**\n     * 钩子名称，用于标识与日志。\n     *\n     * @return 钩子名称\n     */",
        ),
        (
            "    /**\n     *  Execute before put message. For example, Message verification or special message transform\n     * @param msg\n     * @return\n     */",
            "    /**\n     * 写消息前执行，例如消息校验或特殊消息转换。\n     * @param msg 待写入的消息\n     * @return 写消息结果，非 OK 则中断写入\n     */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/hook/SendMessageBackHook.java": [
        (
            "public interface SendMessageBackHook {",
            "/**\n * 从节点回传消息钩子：HA 握手时按偏移将消息发回主节点。\n */\npublic interface SendMessageBackHook {",
        ),
        (
            "    /**\n     * Slave send message back to master at certain offset when HA handshake\n     *\n     * @param msgList\n     * @param brokerName\n     * @param brokerAddr\n     * @return\n     */",
            "    /**\n     * HA 握手阶段从节点将指定偏移的消息回传给主节点。\n     *\n     * @param msgList 待回传的消息列表\n     * @param brokerName Broker 名称\n     * @param brokerAddr Broker 地址\n     * @return 回传成功返回 true\n     */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/index/IndexFile.java": [
        (
            "public class IndexFile {",
            "/**\n * 消息索引文件：基于哈希槽与链表存储 Key 到 CommitLog 物理偏移的映射。\n */\npublic class IndexFile {",
        ),
        (
            "    private static int hashSlotSize = 4;",
            "    /** 哈希槽占用字节数。 */\n    private static int hashSlotSize = 4;",
        ),
        (
            "    private static int indexSize = 20;",
            "    /** 单条索引记录占用字节数。 */\n    private static int indexSize = 20;",
        ),
        (
            "    private static int invalidIndex = 0;",
            "    /** 无效索引位置标记值。 */\n    private static int invalidIndex = 0;",
        ),
        (
            "    private final int hashSlotNum;",
            "    /** 哈希槽数量。 */\n    private final int hashSlotNum;",
        ),
        (
            "    private final int indexNum;",
            "    /** 索引条目最大数量。 */\n    private final int indexNum;",
        ),
        (
            "    private final int fileTotalSize;",
            "    /** 索引文件总字节大小。 */\n    private final int fileTotalSize;",
        ),
        (
            "    private final MappedFile mappedFile;",
            "    /** 底层 Mapped 文件对象。 */\n    private final MappedFile mappedFile;",
        ),
        (
            "    private final MappedByteBuffer mappedByteBuffer;",
            "    /** 映射到内存的字节缓冲区。 */\n    private final MappedByteBuffer mappedByteBuffer;",
        ),
        (
            "    private final IndexHeader indexHeader;",
            "    /** 索引文件头，记录时间戳与计数信息。 */\n    private final IndexHeader indexHeader;",
        ),
        (
            "    public IndexFile(final String fileName, final int hashSlotNum, final int indexNum,\n        final long endPhyOffset, final long endTimestamp) throws IOException {",
            "    /** 创建或打开索引文件并初始化文件头。 */\n    public IndexFile(final String fileName, final int hashSlotNum, final int indexNum,\n        final long endPhyOffset, final long endTimestamp) throws IOException {",
        ),
        (
            "    public String getFileName() {",
            "    /** 返回索引文件名。 */\n    public String getFileName() {",
        ),
        (
            "    public int getFileSize() {",
            "    /** 返回索引文件总大小。 */\n    public int getFileSize() {",
        ),
        (
            "    public void load() {",
            "    /** 从 Mapped 缓冲区加载文件头到内存。 */\n    public void load() {",
        ),
        (
            "    public void shutdown() {",
            "    /** 刷盘后释放 Mapped 文件资源。 */\n    public void shutdown() {",
        ),
        (
            "    public void flush() {",
            "    /** 将文件头与索引数据强制刷盘。 */\n    public void flush() {",
        ),
        (
            "    public boolean isWriteFull() {",
            "    /** 判断索引条目是否已达上限。 */\n    public boolean isWriteFull() {",
        ),
        (
            "    public boolean destroy(final long intervalForcibly) {",
            "    /** 销毁 Mapped 文件，可强制等待。 */\n    public boolean destroy(final long intervalForcibly) {",
        ),
        (
            "    public boolean putKey(final String key, final long phyOffset, final long storeTimestamp) {",
            "    /** 写入一条 Key 到物理偏移的索引记录。 */\n    public boolean putKey(final String key, final long phyOffset, final long storeTimestamp) {",
        ),
        (
            "    public int indexKeyHashMethod(final String key) {",
            "    /** 计算 Key 的非负哈希值。 */\n    public int indexKeyHashMethod(final String key) {",
        ),
        (
            "    public long getBeginTimestamp() {",
            "    /** 返回索引覆盖的最早时间戳。 */\n    public long getBeginTimestamp() {",
        ),
        (
            "    public long getEndTimestamp() {",
            "    /** 返回索引覆盖的最晚时间戳。 */\n    public long getEndTimestamp() {",
        ),
        (
            "    public long getEndPhyOffset() {",
            "    /** 返回索引覆盖的最大物理偏移。 */\n    public long getEndPhyOffset() {",
        ),
        (
            "    public boolean isTimeMatched(final long begin, final long end) {",
            "    /** 判断查询时间范围是否与索引时间范围有交集。 */\n    public boolean isTimeMatched(final long begin, final long end) {",
        ),
        (
            "    public void selectPhyOffset(final List<Long> phyOffsets, final String key, final int maxNum,\n                                final long begin, final long end) {",
            "    /** 按 Key 与时间范围查询物理偏移列表。 */\n    public void selectPhyOffset(final List<Long> phyOffsets, final String key, final int maxNum,\n                                final long begin, final long end) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/index/IndexHeader.java": [
        (
            "/**\n * Index File Header. Format:",
            "/**\n * 索引文件头格式说明：",
        ),
        (
            " * Index File Header. Size:",
            " * 索引文件头大小：",
        ),
        (
            " * Begin Timestamp(8) + End Timestamp(8) + Begin Physical Offset(8) + End Physical Offset(8) + Hash Slot Count(4) + Index Count(4) = 40 Bytes\n */",
            " * 起始时间戳(8) + 结束时间戳(8) + 起始物理偏移(8) + 结束物理偏移(8) + 哈希槽数(4) + 索引数(4) = 40 字节\n */",
        ),
        (
            "public class IndexHeader {",
            "/**\n * 索引文件头：维护时间戳、物理偏移及哈希槽与索引条目计数。\n */\npublic class IndexHeader {",
        ),
        (
            "    public static final int INDEX_HEADER_SIZE = 40;",
            "    /** 索引文件头固定长度（字节）。 */\n    public static final int INDEX_HEADER_SIZE = 40;",
        ),
        (
            "    public IndexHeader(final ByteBuffer byteBuffer) {",
            "    /** 绑定 Mapped 缓冲区中的文件头区域。 */\n    public IndexHeader(final ByteBuffer byteBuffer) {",
        ),
        (
            "    public void load() {",
            "    /** 从 ByteBuffer 加载各字段到原子变量。 */\n    public void load() {",
        ),
        (
            "    public void updateByteBuffer() {",
            "    /** 将内存中的字段写回 ByteBuffer。 */\n    public void updateByteBuffer() {",
        ),
        (
            "    public long getBeginTimestamp() {",
            "    /** 返回索引最早时间戳。 */\n    public long getBeginTimestamp() {",
        ),
        (
            "    public void setBeginTimestamp(long beginTimestamp) {",
            "    /** 设置索引最早时间戳并同步到缓冲区。 */\n    public void setBeginTimestamp(long beginTimestamp) {",
        ),
        (
            "    public long getEndTimestamp() {",
            "    /** 返回索引最晚时间戳。 */\n    public long getEndTimestamp() {",
        ),
        (
            "    public void setEndTimestamp(long endTimestamp) {",
            "    /** 设置索引最晚时间戳并同步到缓冲区。 */\n    public void setEndTimestamp(long endTimestamp) {",
        ),
        (
            "    public long getBeginPhyOffset() {",
            "    /** 返回索引最早物理偏移。 */\n    public long getBeginPhyOffset() {",
        ),
        (
            "    public void setBeginPhyOffset(long beginPhyOffset) {",
            "    /** 设置索引最早物理偏移并同步到缓冲区。 */\n    public void setBeginPhyOffset(long beginPhyOffset) {",
        ),
        (
            "    public long getEndPhyOffset() {",
            "    /** 返回索引最晚物理偏移。 */\n    public long getEndPhyOffset() {",
        ),
        (
            "    public void setEndPhyOffset(long endPhyOffset) {",
            "    /** 设置索引最晚物理偏移并同步到缓冲区。 */\n    public void setEndPhyOffset(long endPhyOffset) {",
        ),
        (
            "    public AtomicInteger getHashSlotCount() {",
            "    /** 返回已使用的哈希槽计数原子变量。 */\n    public AtomicInteger getHashSlotCount() {",
        ),
        (
            "    public void incHashSlotCount() {",
            "    /** 哈希槽计数加一并写回缓冲区。 */\n    public void incHashSlotCount() {",
        ),
        (
            "    public int getIndexCount() {",
            "    /** 返回当前索引条目数量。 */\n    public int getIndexCount() {",
        ),
        (
            "    public void incIndexCount() {",
            "    /** 索引条目计数加一并写回缓冲区。 */\n    public void incIndexCount() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/index/QueryOffsetResult.java": [
        (
            "public class QueryOffsetResult {",
            "/**\n * 索引查询结果：包含物理偏移列表及索引最后更新时间信息。\n */\npublic class QueryOffsetResult {",
        ),
        (
            "    private final List<Long> phyOffsets;",
            "    /** 查询命中的 CommitLog 物理偏移列表。 */\n    private final List<Long> phyOffsets;",
        ),
        (
            "    private final long indexLastUpdateTimestamp;",
            "    /** 索引最后更新时间戳。 */\n    private final long indexLastUpdateTimestamp;",
        ),
        (
            "    private final long indexLastUpdatePhyoffset;",
            "    /** 索引最后更新时的物理偏移。 */\n    private final long indexLastUpdatePhyoffset;",
        ),
        (
            "    public QueryOffsetResult(List<Long> phyOffsets, long indexLastUpdateTimestamp,\n        long indexLastUpdatePhyoffset) {",
            "    /** 构造索引查询结果。 */\n    public QueryOffsetResult(List<Long> phyOffsets, long indexLastUpdateTimestamp,\n        long indexLastUpdatePhyoffset) {",
        ),
        (
            "    public List<Long> getPhyOffsets() {",
            "    /** 返回物理偏移列表。 */\n    public List<Long> getPhyOffsets() {",
        ),
        (
            "    public long getIndexLastUpdateTimestamp() {",
            "    /** 返回索引最后更新时间戳。 */\n    public long getIndexLastUpdateTimestamp() {",
        ),
        (
            "    public long getIndexLastUpdatePhyoffset() {",
            "    /** 返回索引最后更新的物理偏移。 */\n    public long getIndexLastUpdatePhyoffset() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/index/rocksdb/IndexRocksDBRecord.java": [
        (
            "public class IndexRocksDBRecord {",
            "/**\n * RocksDB 索引记录：封装 Topic、Key/Tag、时间戳与物理偏移的键值编码。\n */\npublic class IndexRocksDBRecord {",
        ),
        (
            "    public static final String KEY_SPLIT = \"@\";",
            "    /** 索引键各段之间的分隔符。 */\n    public static final String KEY_SPLIT = \"@\";",
        ),
        (
            "    public static final byte[] KEY_SPLIT_BYTES = KEY_SPLIT.getBytes(StandardCharsets.UTF_8);",
            "    /** 分隔符的 UTF-8 字节形式。 */\n    public static final byte[] KEY_SPLIT_BYTES = KEY_SPLIT.getBytes(StandardCharsets.UTF_8);",
        ),
        (
            "    private static final int VALUE_LENGTH = Long.BYTES;",
            "    /** 值字段固定长度（8 字节时间戳）。 */\n    private static final int VALUE_LENGTH = Long.BYTES;",
        ),
        (
            "    private long storeTime;",
            "    /** 消息存储时间戳。 */\n    private long storeTime;",
        ),
        (
            "    private String topic;",
            "    /** 消息 Topic。 */\n    private String topic;",
        ),
        (
            "    private String key;",
            "    /** 消息业务 Key（可为空）。 */\n    private String key;",
        ),
        (
            "    private String tag;",
            "    /** 消息 Tag（可为空）。 */\n    private String tag;",
        ),
        (
            "    private String uniqKey;",
            "    /** 消息唯一键。 */\n    private String uniqKey;",
        ),
        (
            "    private long offsetPy;",
            "    /** CommitLog 物理偏移。 */\n    private long offsetPy;",
        ),
        (
            "    public IndexRocksDBRecord(String topic, String key, String tag, long storeTime, String uniqKey, long offsetPy) {",
            "    /** 构造一条 RocksDB 索引记录。 */\n    public IndexRocksDBRecord(String topic, String key, String tag, long storeTime, String uniqKey, long offsetPy) {",
        ),
        (
            "    public byte[] getKeyBytes() {",
            "    /** 编码为 RocksDB 键字节数组，无效时返回 null。 */\n    public byte[] getKeyBytes() {",
        ),
        (
            "    public byte[] getValueBytes() {",
            "    /** 编码为 RocksDB 值字节数组（存储时间戳）。 */\n    public byte[] getValueBytes() {",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic。 */\n    public String getTopic() {",
        ),
        (
            "    public void setTopic(String topic) {",
            "    /** 设置 Topic。 */\n    public void setTopic(String topic) {",
        ),
        (
            "    public String getKey() {",
            "    /** 返回业务 Key。 */\n    public String getKey() {",
        ),
        (
            "    public void setKey(String key) {",
            "    /** 设置业务 Key。 */\n    public void setKey(String key) {",
        ),
        (
            "    public long getStoreTime() {",
            "    /** 返回存储时间戳。 */\n    public long getStoreTime() {",
        ),
        (
            "    public void setStoreTime(long storeTime) {",
            "    /** 设置存储时间戳。 */\n    public void setStoreTime(long storeTime) {",
        ),
        (
            "    public String getUniqKey() {",
            "    /** 返回唯一键。 */\n    public String getUniqKey() {",
        ),
        (
            "    public void setUniqKey(String uniqKey) {",
            "    /** 设置唯一键。 */\n    public void setUniqKey(String uniqKey) {",
        ),
        (
            "    public long getOffsetPy() {",
            "    /** 返回物理偏移。 */\n    public long getOffsetPy() {",
        ),
        (
            "    public void setOffsetPy(long offsetPy) {",
            "    /** 设置物理偏移。 */\n    public void setOffsetPy(long offsetPy) {",
        ),
        (
            "    public String getTag() {",
            "    /** 返回 Tag。 */\n    public String getTag() {",
        ),
        (
            "    public void setTag(String tag) {",
            "    /** 设置 Tag。 */\n    public void setTag(String tag) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/index/rocksdb/IndexRocksDBStore.java": [
        (
            "public class IndexRocksDBStore implements CommitLogDispatchStore {",
            "/**\n * 基于 RocksDB 的消息索引存储：异步构建 Key/Tag/唯一键到物理偏移的索引。\n */\npublic class IndexRocksDBStore implements CommitLogDispatchStore {",
        ),
        (
            "    private static final int DEFAULT_CAPACITY = 100000;",
            "    /** 索引构建队列默认容量。 */\n    private static final int DEFAULT_CAPACITY = 100000;",
        ),
        (
            "    private static final int BATCH_SIZE = 1000;",
            "    /** 批量写入 RocksDB 的批次大小。 */\n    private static final int BATCH_SIZE = 1000;",
        ),
        (
            "    private final MessageStore messageStore;",
            "    /** 所属 MessageStore 实例。 */\n    private final MessageStore messageStore;",
        ),
        (
            "    private final MessageStoreConfig storeConfig;",
            "    /** 消息存储配置。 */\n    private final MessageStoreConfig storeConfig;",
        ),
        (
            "    private final MessageRocksDBStorage messageRocksDBStorage;",
            "    /** RocksDB 存储引擎封装。 */\n    private final MessageRocksDBStorage messageRocksDBStorage;",
        ),
        (
            "    private volatile long lastDeleteIndexTime = 0L;",
            "    /** 上次删除过期索引的小时时间戳。 */\n    private volatile long lastDeleteIndexTime = 0L;",
        ),
        (
            "    private IndexBuildService indexBuildService;",
            "    /** 后台索引构建线程服务。 */\n    private IndexBuildService indexBuildService;",
        ),
        (
            "    private BlockingQueue<IndexRocksDBRecord> originIndexMsgQueue;",
            "    /** 待构建索引的记录队列。 */\n    private BlockingQueue<IndexRocksDBRecord> originIndexMsgQueue;",
        ),
        (
            "    public IndexRocksDBStore(MessageStore messageStore) {",
            "    /** 构造索引存储，配置启用时自动启动构建服务。 */\n    public IndexRocksDBStore(MessageStore messageStore) {",
        ),
        (
            "    public void shutdown() {",
            "    /** 关闭索引构建服务。 */\n    public void shutdown() {",
        ),
        (
            "    public QueryOffsetResult queryOffset(String topic, String key, int maxNum, long beginTime, long endTime, String indexType, String lastKey) {",
            "    /** 按 Topic、Key 与时间范围查询物理偏移。 */\n    public QueryOffsetResult queryOffset(String topic, String key, int maxNum, long beginTime, long endTime, String indexType, String lastKey) {",
        ),
        (
            "    public void buildIndex(DispatchRequest dispatchRequest) {",
            "    /** 根据分发请求异步构建 Key、Tag 与唯一键索引。 */\n    public void buildIndex(DispatchRequest dispatchRequest) {",
        ),
        (
            "    public void deleteExpiredIndex() {",
            "    /** 删除早于最早 CommitLog 文件的过期索引记录。 */\n    public void deleteExpiredIndex() {",
        ),
        (
            "    public boolean isMappedFileMatchedRecover(long phyOffset, long storeTimestamp,\n        boolean recoverNormally) throws RocksDBException {",
            "    /** 判断 MappedFile 偏移是否已在 RocksDB 索引中覆盖。 */\n    public boolean isMappedFileMatchedRecover(long phyOffset, long storeTimestamp,\n        boolean recoverNormally) throws RocksDBException {",
        ),
        (
            "    public void destroy() {",
            "    /** 销毁索引存储（当前为空实现）。 */\n    public void destroy() {",
        ),
        (
            "    @Override\n    public Long getDispatchFromPhyOffset(boolean recoverNormally) throws RocksDBException {",
            "    /** 返回索引已覆盖的最大物理偏移，用于恢复分发起点。 */\n    @Override\n    public Long getDispatchFromPhyOffset(boolean recoverNormally) throws RocksDBException {",
        ),
        (
            "    private class IndexBuildService extends ServiceThread {",
            "    /** 后台线程：从队列批量拉取记录并写入 RocksDB。 */\n    private class IndexBuildService extends ServiceThread {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/kv/CommitLogDispatcherCompaction.java": [
        (
            "public class CommitLogDispatcherCompaction implements CommitLogDispatcher {",
            "/**\n * CommitLog 压缩分发器：将分发请求转发给 CompactionService。\n */\npublic class CommitLogDispatcherCompaction implements CommitLogDispatcher {",
        ),
        (
            "    private final CompactionService cptService;",
            "    /** 底层压缩服务实例。 */\n    private final CompactionService cptService;",
        ),
        (
            "    public CommitLogDispatcherCompaction(CompactionService srv) {",
            "    /** 绑定压缩服务。 */\n    public CommitLogDispatcherCompaction(CompactionService srv) {",
        ),
        (
            "    @Override\n    public void dispatch(DispatchRequest request) {",
            "    /** 将分发请求提交给压缩服务处理。 */\n    @Override\n    public void dispatch(DispatchRequest request) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/kv/CompactionPositionMgr.java": [
        (
            "public class CompactionPositionMgr extends ConfigManager {",
            "/**\n * 压缩进度管理器：持久化各 Topic-Queue 的已压缩偏移检查点。\n */\npublic class CompactionPositionMgr extends ConfigManager {",
        ),
        (
            "    public static final String CHECKPOINT_FILE = \"position-checkpoint\";",
            "    /** 检查点文件名。 */\n    public static final String CHECKPOINT_FILE = \"position-checkpoint\";",
        ),
        (
            "    private transient String compactionPath;",
            "    /** 压缩数据根目录路径。 */\n    private transient String compactionPath;",
        ),
        (
            "    private transient String checkpointFileName;",
            "    /** 检查点文件完整路径。 */\n    private transient String checkpointFileName;",
        ),
        (
            "    private ConcurrentHashMap<String, Long> queueOffsetMap = new ConcurrentHashMap<>();",
            "    /** Topic_QueueId 到已压缩偏移的映射。 */\n    private ConcurrentHashMap<String, Long> queueOffsetMap = new ConcurrentHashMap<>();",
        ),
        (
            "    public CompactionPositionMgr(final String compactionPath) {",
            "    /** 指定压缩路径并加载检查点文件。 */\n    public CompactionPositionMgr(final String compactionPath) {",
        ),
        (
            "    public void setOffset(String topic, int queueId, final long offset) {",
            "    /** 记录指定队列的已压缩偏移。 */\n    public void setOffset(String topic, int queueId, final long offset) {",
        ),
        (
            "    public long getOffset(String topic, int queueId) {",
            "    /** 获取指定队列的已压缩偏移，不存在返回 -1。 */\n    public long getOffset(String topic, int queueId) {",
        ),
        (
            "    public boolean isEmpty() {",
            "    /** 判断是否尚未记录任何压缩偏移。 */\n    public boolean isEmpty() {",
        ),
        (
            "    public boolean isCompaction(String topic, int queueId, long offset) {",
            "    /** 判断给定偏移是否已被压缩覆盖。 */\n    public boolean isCompaction(String topic, int queueId, long offset) {",
        ),
        (
            "    @Override\n    public String configFilePath() {",
            "    /** 返回检查点文件路径。 */\n    @Override\n    public String configFilePath() {",
        ),
        (
            "    @Override\n    public String encode() {",
            "    /** 将进度映射编码为 JSON 字符串。 */\n    @Override\n    public String encode() {",
        ),
        (
            "    @Override\n    public String encode(boolean prettyFormat) {",
            "    /** 将进度映射编码为 JSON，可选格式化。 */\n    @Override\n    public String encode(boolean prettyFormat) {",
        ),
        (
            "    @Override\n    public void decode(String jsonString) {",
            "    /** 从 JSON 字符串解码进度映射。 */\n    @Override\n    public void decode(String jsonString) {",
        ),
        (
            "    public ConcurrentHashMap<String, Long> getQueueOffsetMap() {",
            "    /** 返回队列偏移映射表。 */\n    public ConcurrentHashMap<String, Long> getQueueOffsetMap() {",
        ),
        (
            "    public void setQueueOffsetMap(ConcurrentHashMap<String, Long> queueOffsetMap) {",
            "    /** 设置队列偏移映射表。 */\n    public void setQueueOffsetMap(ConcurrentHashMap<String, Long> queueOffsetMap) {",
        ),
    ],
}
