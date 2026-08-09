"""RocketMQ 5.5.0 wave13b common compression/config/consistenthash/constant [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["common/src/main/java/org/apache/rocketmq/common/compression/CompressorFactory.java"] = [
    (
        "public class CompressorFactory {",
        "/**\n * 压缩器工厂：按 {@link CompressionType} 注册并返回 LZ4/ZSTD/ZLIB 实现。\n */\npublic class CompressorFactory {",
    ),
    (
        "    private static final EnumMap<CompressionType, Compressor> COMPRESSORS;",
        "    /** 压缩类型到具体压缩器实例的映射表。 */\n    private static final EnumMap<CompressionType, Compressor> COMPRESSORS;",
    ),
    (
        "    public static Compressor getCompressor(CompressionType type) {",
        "    /** 根据压缩类型获取对应压缩器，未注册类型返回 null。 */\n    public static Compressor getCompressor(CompressionType type) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/compression/Lz4Compressor.java"] = [
    (
        "public class Lz4Compressor implements Compressor {",
        "/**\n * 基于 LZ4 Frame 格式的消息体压缩/解压实现。\n */\npublic class Lz4Compressor implements Compressor {",
    ),
    (
        "    @Override\n    public byte[] compress(byte[] src, int level) throws IOException {",
        "    /** 使用 LZ4 Frame 压缩字节数组；level 参数由框架传入，LZ4 实现中未直接使用。 */\n    @Override\n    public byte[] compress(byte[] src, int level) throws IOException {",
    ),
    (
        "    @Override\n    public byte[] decompress(byte[] src) throws IOException {",
        "    /** 解压 LZ4 Frame 格式的压缩数据并返回原始字节。 */\n    @Override\n    public byte[] decompress(byte[] src) throws IOException {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/compression/ZlibCompressor.java"] = [
    (
        "public class ZlibCompressor implements Compressor {",
        "/**\n * 基于 JDK Deflater/Inflater 的 ZLIB 压缩/解压实现。\n */\npublic class ZlibCompressor implements Compressor {",
    ),
    (
        "    @Override\n    public byte[] compress(byte[] src, int level) throws IOException {",
        "    /** 按指定压缩级别（0–9）对数据进行 ZLIB 压缩。 */\n    @Override\n    public byte[] compress(byte[] src, int level) throws IOException {",
    ),
    (
        "    @Override\n    public byte[] decompress(byte[] src) throws IOException {",
        "    /** 解压 ZLIB 格式数据并返回原始字节数组。 */\n    @Override\n    public byte[] decompress(byte[] src) throws IOException {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/compression/ZstdCompressor.java"] = [
    (
        "public class ZstdCompressor implements Compressor {",
        "/**\n * 基于 zstd-jni 的 ZSTD 压缩/解压实现。\n */\npublic class ZstdCompressor implements Compressor {",
    ),
    (
        "    @Override\n    public byte[] compress(byte[] src, int level) throws IOException {",
        "    /** 按指定压缩级别对数据进行 ZSTD 压缩。 */\n    @Override\n    public byte[] compress(byte[] src, int level) throws IOException {",
    ),
    (
        "    @Override\n    public byte[] decompress(byte[] src) throws IOException {",
        "    /** 解压 ZSTD 格式数据并返回原始字节数组。 */\n    @Override\n    public byte[] decompress(byte[] src) throws IOException {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/config/ConfigHelper.java"] = [
    (
        "public class ConfigHelper {",
        "/**\n * RocketMQ 配置 RocksDB 的列族与 DB 选项工厂：块缓存、Compaction、WAL 刷盘策略等。\n */\npublic class ConfigHelper {",
    ),
    (
        "    public static ColumnFamilyOptions createConfigColumnFamilyOptions() {",
        "    /** 创建配置存储专用列族选项（块表、Bloom 过滤、Level Compaction 等）。 */\n    public static ColumnFamilyOptions createConfigColumnFamilyOptions() {",
    ),
    (
        "            // Indicating if we'd put index/filter blocks to the block cache.",
        "            // 是否将索引/过滤块放入块缓存。",
    ),
    (
        "            // The target file size for compaction.",
        "            // Compaction 目标文件大小。",
    ),
    (
        "            // The upper-bound of the total size of L1 files in bytes",
        "            // L1 层文件总大小的上限（字节）。",
    ),
    (
        "    public static DBOptions createConfigDBOptions() {",
        "    /** 创建配置库 DB 选项：手动 WAL 刷盘、限速、Direct IO 等。 */\n    public static DBOptions createConfigDBOptions() {",
    ),
    (
        "        // Tune based on https://github.com/facebook/rocksdb/wiki/RocksDB-Tuning-Guide",
        "        // 调参参考 RocksDB Tuning Guide 及内部 JStorm 实践。",
    ),
    (
        "            /*\n             * We use manual flush to achieve desired balance between reliability and performance:\n             * for metadata that matters, including {topic, subscription}-config changes, each write incurs a\n             * flush-and-sync to ensure reliability; for {commit, pull}-offset advancements, group-flush are offered for\n             * every N(configurable, 1024 by default) writes or aging of writes, similar to OS page-cache flush\n             * mechanism.\n             */",
        "            /*\n             * 启用手动 WAL 刷盘，在可靠性与性能间折中：\n             * 对 Topic/订阅等关键元数据每次写入都会 flush-and-sync；\n             * 对 commit/pull 位点推进则按 N 次写入（默认 1024）或写入老化批量刷盘，类似 OS 页缓存机制。\n             */",
    ),
    (
        "            // This option takes effect only when we have multiple column families",
        "            // 仅在有多个列族时生效。",
    ),
    (
        "    public static String getDBLogDir() {",
        "    /** 在用户目录、临时目录或 /data 下递归创建并返回 RocketMQ RocksDB 日志目录。 */\n    public static String getDBLogDir() {",
    ),
    (
        "            // Refer bazel test encyclopedia: https://bazel.build/reference/test-encyclopedia",
        "            // 参考 Bazel 测试百科：并非所有目录在测试环境中可写。",
    ),
    (
        "            // Not all directories is available",
        "            // 跳过不存在或不可写的根路径。",
    ),
    (
        "            // Create directories recursively.",
        "            // 递归创建日志目录。",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/config/ConfigManagerVersion.java"] = [
    (
        "public enum ConfigManagerVersion {",
        "/**\n * 配置管理器存储格式版本标识。\n */\npublic enum ConfigManagerVersion {",
    ),
    (
        "    V1(\"v1\"),",
        "    /** 配置管理器 v1 格式。 */\n    V1(\"v1\"),",
    ),
    (
        "    V2(\"v2\"),",
        "    /** 配置管理器 v2 格式（RocksDB 等）。 */\n    V2(\"v2\"),",
    ),
    (
        "    public String getVersion() {",
        "    /** 返回版本字符串（如 v1、v2）。 */\n    public String getVersion() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/config/ConfigRocksDBStorage.java"] = [
    (
        "public class ConfigRocksDBStorage extends AbstractRocksDBStorage {",
        "/**\n * 基于 RocksDB 的配置键值存储：多列族、批量写入、只读/读写模式及全局实例缓存。\n */\npublic class ConfigRocksDBStorage extends AbstractRocksDBStorage {",
    ),
    (
        "    public static final Charset CHARSET = StandardCharsets.UTF_8;",
        "    /** 列族名与字符串键使用的字符集。 */\n    public static final Charset CHARSET = StandardCharsets.UTF_8;",
    ),
    (
        "    public static final ConcurrentMap<String, ConfigRocksDBStorage> STORE_MAP = new ConcurrentHashMap<>();",
        "    /** 按 dbPath 缓存的 ConfigRocksDBStorage 单例映射。 */\n    public static final ConcurrentMap<String, ConfigRocksDBStorage> STORE_MAP = new ConcurrentHashMap<>();",
    ),
    (
        "    protected void initOptions() {",
        "    /** 使用 {@link ConfigHelper} 初始化 DB 与列族选项。 */\n    protected void initOptions() {",
    ),
    (
        "    @Override\n    protected boolean postLoad() {",
        "    /** 打开或创建 DB：列举列族、加载句柄并绑定默认列族。 */\n    @Override\n    protected boolean postLoad() {",
    ),
    (
        "    @Override\n    protected void preShutdown() {",
        "    /** 关闭前释放各列族句柄。 */\n    @Override\n    protected void preShutdown() {",
    ),
    (
        "    // batch operations",
        "    // 批量写入操作",
    ),
    (
        "    public void writeBatchPutOperation(String cf, WriteBatch writeBatch, final byte[] key, final byte[] value) throws RocksDBException {",
        "    /** 向 WriteBatch 追加指定列族的 put 操作。 */\n    public void writeBatchPutOperation(String cf, WriteBatch writeBatch, final byte[] key, final byte[] value) throws RocksDBException {",
    ),
    (
        "    public void batchPut(final WriteBatch batch) throws RocksDBException {",
        "    /** 批量提交（默认写入选项，可能不刷 WAL）。 */\n    public void batchPut(final WriteBatch batch) throws RocksDBException {",
    ),
    (
        "    public void batchPutWithWal(final WriteBatch batch) throws RocksDBException {",
        "    /** 批量提交并写入 WAL（可靠刷盘路径）。 */\n    public void batchPutWithWal(final WriteBatch batch) throws RocksDBException {",
    ),
    (
        "    // operations with the specified cf",
        "    // 指定列族的读写删与遍历",
    ),
    (
        "    public void put(String cf, final byte[] keyBytes, final int keyLen, final byte[] valueBytes) throws Exception {",
        "    /** 向指定列族写入键值（带 WAL）。 */\n    public void put(String cf, final byte[] keyBytes, final int keyLen, final byte[] valueBytes) throws Exception {",
    ),
    (
        "    public void put(String cf, final ByteBuffer keyBB, final ByteBuffer valueBB) throws Exception {",
        "    /** 使用 ByteBuffer 向指定列族写入键值。 */\n    public void put(String cf, final ByteBuffer keyBB, final ByteBuffer valueBB) throws Exception {",
    ),
    (
        "    public byte[] get(String cf, final byte[] keyBytes) throws Exception {",
        "    /** 从指定列族按字节键读取值，列族不存在时返回 null。 */\n    public byte[] get(String cf, final byte[] keyBytes) throws Exception {",
    ),
    (
        "    public void delete(String cf, final byte[] keyBytes) throws Exception {",
        "    /** 从指定列族删除键（带 WAL）。 */\n    public void delete(String cf, final byte[] keyBytes) throws Exception {",
    ),
    (
        "    public void iterate(final String cf, BiConsumer<byte[], byte[]> biConsumer) throws RocksDBException {",
        "    /** 遍历指定列族全部键值并对每对调用 biConsumer。 */\n    public void iterate(final String cf, BiConsumer<byte[], byte[]> biConsumer) throws RocksDBException {",
    ),
    (
        "    public RocksIterator iterator() {",
        "    /** 返回默认列族的全序迭代器。 */\n    public RocksIterator iterator() {",
    ),
    (
        "    public ColumnFamilyHandle getOrCreateColumnFamily(String cf) throws RocksDBException {",
        "    /** 获取列族句柄；读写模式下不存在则动态创建。 */\n    public ColumnFamilyHandle getOrCreateColumnFamily(String cf) throws RocksDBException {",
    ),
    (
        "    public void addIfNotExists(List<byte[]> columnFamilyNames, byte[] byteArray) {",
        "    /** 若列表中尚无该列族名则追加。 */\n    public void addIfNotExists(List<byte[]> columnFamilyNames, byte[] byteArray) {",
    ),
    (
        "    public static ConfigRocksDBStorage getStore(String path, boolean readOnly, CompressionType compressionType) {",
        "    /** 按路径获取或创建 ConfigRocksDBStorage 实例（可指定压缩类型）。 */\n    public static ConfigRocksDBStorage getStore(String path, boolean readOnly, CompressionType compressionType) {",
    ),
    (
        "    public static ConfigRocksDBStorage getStore(String path, boolean readOnly) {",
        "    /** 按路径获取或创建 ConfigRocksDBStorage 实例。 */\n    public static ConfigRocksDBStorage getStore(String path, boolean readOnly) {",
    ),
    (
        "    public static void shutdown(String path) {",
        "    /** 关闭并移除指定路径的存储实例。 */\n    public static void shutdown(String path) {",
    ),
    (
        "    public static void destroy(String path) {",
        "    /** 关闭并销毁指定路径的 DB 文件。 */\n    public static void destroy(String path) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/consistenthash/ConsistentHashRouter.java"] = [
    (
        "/**\n * To hash Node objects to a hash ring with a certain amount of virtual node.\n * Method routeNode will return a Node instance which the object key should be allocated to according to consistent hash\n * algorithm\n */",
        "/**\n * 一致性哈希环路由器：将物理节点映射为若干虚拟节点，\n * {@link #routeNode(String)} 按对象键在环上顺时针选取承载节点。\n */",
    ),
    (
        "    private final SortedMap<Long, VirtualNode<T>> ring = new TreeMap<>();",
        "    /** 哈希值到虚拟节点的有序环。 */\n    private final SortedMap<Long, VirtualNode<T>> ring = new TreeMap<>();",
    ),
    (
        "    public ConsistentHashRouter(Collection<T> pNodes, int vNodeCount) {",
        "    /** 使用默认 MD5 哈希函数构造路由器。 */\n    public ConsistentHashRouter(Collection<T> pNodes, int vNodeCount) {",
    ),
    (
        "    /**\n     * @param pNodes collections of physical nodes\n     * @param vNodeCount amounts of virtual nodes\n     * @param hashFunction hash Function to hash Node instances\n     */",
        "    /**\n     * @param pNodes 物理节点集合\n     * @param vNodeCount 每个物理节点对应的虚拟节点数量\n     * @param hashFunction 节点/键哈希函数\n     */",
    ),
    (
        "    /**\n     * add physic node to the hash ring with some virtual nodes\n     *\n     * @param pNode physical node needs added to hash ring\n     * @param vNodeCount the number of virtual node of the physical node. Value should be greater than or equals to 0\n     */",
        "    /**\n     * 向哈希环添加物理节点及其虚拟节点副本。\n     *\n     * @param pNode 待加入的物理节点\n     * @param vNodeCount 该物理节点新增的虚拟节点数，须 ≥ 0\n     */",
    ),
    (
        "    /**\n     * remove the physical node from the hash ring\n     */",
        "    /** 从哈希环移除指定物理节点的全部虚拟节点。 */",
    ),
    (
        "    /**\n     * with a specified key, route the nearest Node instance in the current hash ring\n     *\n     * @param objectKey the object key to find a nearest Node\n     */",
        "    /**\n     * 根据对象键在环上定位顺时针最近的物理节点。\n     *\n     * @param objectKey 待路由的对象键\n     */",
    ),
    (
        "    public int getExistingReplicas(T pNode) {",
        "    /** 统计环上该物理节点已有的虚拟节点副本数。 */\n    public int getExistingReplicas(T pNode) {",
    ),
    (
        "    //default hash function",
        "    // 默认 MD5 哈希实现",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/consistenthash/HashFunction.java"] = [
    (
        "/**\n * Hash String to long value\n */",
        "/**\n * 将字符串键哈希为 long 值，供一致性哈希环定位使用。\n */",
    ),
    (
        "    long hash(String key);",
        "    /** 计算键的哈希值。 */\n    long hash(String key);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/consistenthash/Node.java"] = [
    (
        "/**\n * Represent a node which should be mapped to a hash ring\n */",
        "/**\n * 一致性哈希环上的节点抽象，提供用于映射的唯一键。\n */",
    ),
    (
        "    /**\n     * @return the key which will be used for hash mapping\n     */",
        "    /**\n     * @return 参与哈希映射的节点标识键\n     */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/consistenthash/VirtualNode.java"] = [
    (
        "public class VirtualNode<T extends Node> implements Node {",
        "/**\n * 物理节点在一致性哈希环上的虚拟副本，键为 physicalKey-replicaIndex。\n */\npublic class VirtualNode<T extends Node> implements Node {",
    ),
    (
        "    final T physicalNode;",
        "    /** 关联的物理节点。 */\n    final T physicalNode;",
    ),
    (
        "    final int replicaIndex;",
        "    /** 虚拟副本序号（同一物理节点可有多个）。 */\n    final int replicaIndex;",
    ),
    (
        "    public boolean isVirtualNodeOf(T pNode) {",
        "    /** 判断该虚拟节点是否属于给定物理节点。 */\n    public boolean isVirtualNodeOf(T pNode) {",
    ),
    (
        "    public T getPhysicalNode() {",
        "    /** 返回关联的物理节点。 */\n    public T getPhysicalNode() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/constant/CommonConstants.java"] = [
    (
        "public class CommonConstants {",
        "/**\n * 通用字符串分隔符与符号常量，供配置解析、路径拼接等使用。\n */\npublic class CommonConstants {",
    ),
    (
        "    public static final String COLON = \":\";",
        "    /** 冒号分隔符。 */\n    public static final String COLON = \":\";",
    ),
    (
        "    public static final String ASTERISK = \"*\";",
        "    /** 星号通配符。 */\n    public static final String ASTERISK = \"*\";",
    ),
    (
        "    public static final String COMMA = \",\";",
        "    /** 逗号分隔符。 */\n    public static final String COMMA = \",\";",
    ),
    (
        "    public static final String EQUAL = \"=\";",
        "    /** 等号（键值对分隔）。 */\n    public static final String EQUAL = \"=\";",
    ),
    (
        "    public static final String SLASH = \"/\";",
        "    /** 斜杠（路径分隔）。 */\n    public static final String SLASH = \"/\";",
    ),
    (
        "    public static final String SPACE = \" \";",
        "    /** 空格字符。 */\n    public static final String SPACE = \" \";",
    ),
    (
        "    public static final String HYPHEN = \"-\";",
        "    /** 连字符。 */\n    public static final String HYPHEN = \"-\";",
    ),
    (
        "    public static final String POUND = \"#\";",
        "    /** 井号字符。 */\n    public static final String POUND = \"#\";",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/constant/ConsumeInitMode.java"] = [
    (
        "public class ConsumeInitMode {",
        "/**\n * 消费位点初始化模式边界：MIN/MAX 用于校验合法取值范围。\n */\npublic class ConsumeInitMode {",
    ),
    (
        "    public static final int MIN = 0;",
        "    /** 消费初始化模式最小合法值。 */\n    public static final int MIN = 0;",
    ),
    (
        "    public static final int MAX = 1;",
        "    /** 消费初始化模式最大合法值。 */\n    public static final int MAX = 1;",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/constant/DBMsgConstants.java"] = [
    (
        "public class DBMsgConstants {",
        "/**\n * 数据库消息（DB 消息）相关容量常量。\n */\npublic class DBMsgConstants {",
    ),
    (
        "    public static final int MAX_BODY_SIZE = 64 * 1024 * 1024; //64KB",
        "    /** 消息体最大字节数（64 MiB；源码注释误写为 64KB）。 */\n    public static final int MAX_BODY_SIZE = 64 * 1024 * 1024; //64KB",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/constant/FIleReadaheadMode.java"] = [
    (
        "public class FIleReadaheadMode {",
        "/**\n * 文件预读模式配置项键名（类名 FIle 为历史拼写保留）。\n */\npublic class FIleReadaheadMode {",
    ),
    (
        "    public static final String READ_AHEAD_MODE = \"READ_AHEAD_MODE\";",
        "    /** RocksDB/存储层 READ_AHEAD_MODE 配置键。 */\n    public static final String READ_AHEAD_MODE = \"READ_AHEAD_MODE\";",
    ),
]
