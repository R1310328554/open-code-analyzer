"""Chinese annotation replacements for Redisson 4.7.0 wave-11a spring-data-18/20 [0:15]."""
from __future__ import annotations

W11A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- spring-data-18: ZSET replay decoders ---
W11A_REPLACEMENTS["ScoredSortedListReplayDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 有序集合批量响应解码器：member/score 交替排列，产出 {@link List}{@code <}{@link Tuple}{@code >}。\n"
        " * <p>奇数下标参数经 {@link DoubleCodec} 解析为 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        if (paramNum % 2 != 0) {",
        "        // 奇数下标为 score，使用 DoubleCodec。\n"
        "        if (paramNum % 2 != 0) {",
    ),
    (
        "    @Override\n    public List<Tuple> decode(List<Object> parts, State state) {",
        "    /** 每两个元素组装为一个 {@link DefaultTuple}（member 字节数组 + score）。 */\n"
        "    @Override\n"
        "    public List<Tuple> decode(List<Object> parts, State state) {",
    ),
]

W11A_REPLACEMENTS["ScoredSortedSetReplayDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 有序集合响应解码为 {@link Set}{@code <}{@link Tuple}{@code >}，以 {@link LinkedHashSet} 保留 Redis 返回顺序。\n"
        " * <p>奇数下标参数经 {@link DoubleCodec} 解析 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        if (paramNum % 2 != 0) {",
        "        // 奇数下标为 score，使用 DoubleCodec。\n"
        "        if (paramNum % 2 != 0) {",
    ),
    (
        "    @Override\n    public Set<Tuple> decode(List<Object> parts, State state) {",
        "    /** 成对解析 member/score 并加入 {@link LinkedHashSet}。 */\n"
        "    @Override\n"
        "    public Set<Tuple> decode(List<Object> parts, State state) {",
    ),
]

W11A_REPLACEMENTS["ScoredSortedSetReplayDecoderV2.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 单条 member/score 对解码为 {@link RedisZSetCommands.Tuple}（V2 接口）。\n"
        " * <p>适用于仅含一对元素的 ZSET 命令响应；奇数下标以 {@link DoubleCodec} 解析 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);",
        "        // 偶数下标 member 走默认 Codec 解码。\n"
        "        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);",
    ),
    (
        "    @Override\n    public RedisZSetCommands.Tuple decode(List<Object> parts, State state) {",
        "    /** 从两元素列表构造 {@link DefaultTuple}。 */\n"
        "    @Override\n"
        "    public RedisZSetCommands.Tuple decode(List<Object> parts, State state) {",
    ),
]

# --- spring-data-18: convertors & set decoder ---
W11A_REPLACEMENTS["SecondsConvertor.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 时间单位转换器：将 Redis 协议层 {@link Long} 从 {@code source} 换算为 {@code unit}。\n"
        " * <p>常用于 EXPIRE/PEXPIRE 等命令在秒与毫秒间对齐 Spring Data 语义。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final TimeUnit unit;\n    private final TimeUnit source;",
        "    /** 目标时间单位。 */\n"
        "    private final TimeUnit unit;\n"
        "    /** 源时间单位（Redis 返回值侧）。 */\n"
        "    private final TimeUnit source;",
    ),
    (
        "    public SecondsConvertor(TimeUnit unit, TimeUnit source) {",
        "    /** 指定源单位与目标单位的换算关系。 */\n"
        "    public SecondsConvertor(TimeUnit unit, TimeUnit source) {",
    ),
    (
        "    @Override\n    public Long convert(Object obj) {",
        "    /** 将 {@code Long} 时长从 {@code source} 换算为 {@code unit}。 */\n"
        "    @Override\n"
        "    public Long convert(Object obj) {",
    ),
]

W11A_REPLACEMENTS["SetReplayDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合型 Redis 响应解码器：将元素列表转为 {@link LinkedHashSet} 以保持顺序并去重。\n"
        " * <p>元素解码由构造时注入的 {@link Decoder} 负责。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final Decoder<Object> decoder;",
        "    /** 集合元素解码器。 */\n"
        "    private final Decoder<Object> decoder;",
    ),
    (
        "    public SetReplayDecoder(Decoder<Object> decoder) {",
        "    /** 指定集合元素解码器。 */\n"
        "    public SetReplayDecoder(Decoder<Object> decoder) {",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 返回构造时注入的元素解码器。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
    (
        "    @Override\n    public Set<T> decode(List<Object> parts, State state) {",
        "    /** 将已解码元素列表包装为 {@link LinkedHashSet}。 */\n"
        "    @Override\n"
        "    public Set<T> decode(List<Object> parts, State state) {",
    ),
]

# --- spring-data-20: protocol convertors ---
W11A_REPLACEMENTS["BinaryConvertor.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis 协议层字符串到字节数组转换器。\n"
        " * <p>供 Spring Data Redis 命令参数在 UTF-8 二进制与 {@link String} 间转换。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Object convert(Object obj) {",
        "    /** {@link String} 转为 UTF-8 字节数组；其他类型原样返回。 */\n"
        "    @Override\n"
        "    public Object convert(Object obj) {",
    ),
]

W11A_REPLACEMENTS["DataTypeConvertor.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis {@code TYPE} 命令返回值到 Spring {@link DataType} 的转换器。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public DataType convert(Object obj) {",
        "    /** 将 Redis 类型码字符串解析为 {@link DataType} 枚举。 */\n"
        "    @Override\n"
        "    public DataType convert(Object obj) {",
    ),
]

W11A_REPLACEMENTS["DistanceConvertor.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis GEO 距离数值（{@code double}）包装为 Spring {@link Distance}。\n"
        " * <p>构造时绑定 {@link Metric}（如 {@code Metrics.KILOMETERS}）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final Metric metric;",
        "    /** 距离度量单位。 */\n"
        "    private final Metric metric;",
    ),
    (
        "    public DistanceConvertor(Metric metric) {",
        "    /** 指定 {@link Distance} 使用的 {@link Metric}。 */\n"
        "    public DistanceConvertor(Metric metric) {",
    ),
    (
        "    @Override\n    public Distance convert(Object obj) {",
        "    /** 用 Redis 返回的浮点距离构造 {@link Distance}。 */\n"
        "    @Override\n"
        "    public Distance convert(Object obj) {",
    ),
]

# --- spring-data-20: GEO decoders ---
W11A_REPLACEMENTS["ByteBufferGeoResultsDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis GEO 批量响应解码器，产出 {@link GeoResults}{@code <}{@link GeoLocation}{@code <ByteBuffer>}{@code >}。\n"
        " * <p>member 以 {@link ByteBuffer} 包装；支持带 {@link Distance}（指定 {@link Metric}）\n"
        "或带 {@link Point} 坐标的嵌套列表；单元素条目仅含 member。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final Metric metric;",
        "    /** 距离度量；非空时嵌套列表第二项解析为 {@link Distance}。 */\n"
        "    private final Metric metric;",
    ),
    (
        "    public ByteBufferGeoResultsDecoder() {",
        "    /** 默认构造：嵌套列表第二项按 {@link Point} 解析坐标。 */\n"
        "    public ByteBufferGeoResultsDecoder() {",
    ),
    (
        "    public ByteBufferGeoResultsDecoder(Metric metric) {",
        "    /** 绑定 {@link Metric}，用于 GEORADIUS 等带距离字段的响应。 */\n"
        "    public ByteBufferGeoResultsDecoder(Metric metric) {",
    ),
    (
        "                if (metric != null) {",
        "                // GEORADIUS BYMEMBER 等返回 member + 距离的响应。\n"
        "                if (metric != null) {",
    ),
    (
        "                } else {",
        "                // GEOPOS 等返回 member + 经纬度 Point 的响应。\n"
        "                } else {",
    ),
    (
        "            } else {",
        "            // 仅 member 字节数组，无距离或坐标附加项。\n"
        "            } else {",
    ),
    (
        "    @Override\n    public GeoResults<GeoLocation<ByteBuffer>> decode(List<Object> parts, State state) {",
        "    /** 遍历响应片段，组装 {@link GeoResult} 列表并包装为 {@link GeoResults}。 */\n"
        "    @Override\n"
        "    public GeoResults<GeoLocation<ByteBuffer>> decode(List<Object> parts, State state) {",
    ),
]

W11A_REPLACEMENTS["GeoResultsDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis GEO 批量响应解码器，产出 {@link GeoResults}{@code <}{@link GeoLocation}{@code <byte[]>}{@code >}。\n"
        " * <p>支持带距离（{@code metric} 非空）或带坐标（{@code Point}）两种嵌套列表格式；\n"
        "单元素条目视为仅 member 无附加信息。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final Metric metric;",
        "    /** 距离度量；非空时嵌套列表第二项解析为 {@link Distance}。 */\n"
        "    private final Metric metric;",
    ),
    (
        "    public GeoResultsDecoder() {",
        "    /** 默认构造：嵌套列表第二项解析为 {@link Point}。 */\n"
        "    public GeoResultsDecoder() {",
    ),
    (
        "    public GeoResultsDecoder(Metric metric) {",
        "    /** 指定距离度量；非空时第二项解析为带 {@code metric} 的 {@link Distance}。 */\n"
        "    public GeoResultsDecoder(Metric metric) {",
    ),
    (
        "                if (metric != null) {",
        "                // GEORADIUS 等带距离字段的响应。\n"
        "                if (metric != null) {",
    ),
    (
        "                } else {",
        "                // GEOPOS 等返回经纬度 Point 的响应。\n"
        "                } else {",
    ),
    (
        "            } else {",
        "            // 单字节数组 member，无距离/坐标附加项。\n"
        "            } else {",
    ),
    (
        "    @Override\n    public GeoResults<GeoLocation<byte[]>> decode(List<Object> parts, State state) {",
        "    /** 遍历响应片段，组装 {@link GeoResult} 列表并包装为 {@link GeoResults}。 */\n"
        "    @Override\n"
        "    public GeoResults<GeoLocation<byte[]>> decode(List<Object> parts, State state) {",
    ),
]

# --- spring-data-20: replay / property decoders ---
W11A_REPLACEMENTS["ObjectListReplayDecoder2.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n * @param <T> type\n */",
        "/**\n"
        " * 列表型 Redis 响应解码器（第二版）：将空嵌套列表规范为 {@code null}。\n"
        " * <p>用于 Spring Data Redis 批量读取时对齐空集合语义。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <T> type\n"
        " */",
    ),
    (
        "    private final Decoder<Object> decoder;",
        "    /** 嵌套元素解码器；{@code null} 时使用默认 Codec。 */\n"
        "    private final Decoder<Object> decoder;",
    ),
    (
        "    public ObjectListReplayDecoder2() {",
        "    /** 使用默认 Codec 解码嵌套元素。 */\n"
        "    public ObjectListReplayDecoder2() {",
    ),
    (
        "    public ObjectListReplayDecoder2(Decoder<Object> decoder) {",
        "    /** 指定嵌套元素解码器；{@code null} 时使用默认 Codec 解码。 */\n"
        "    public ObjectListReplayDecoder2(Decoder<Object> decoder) {",
    ),
    (
        "                if (((List) object).isEmpty()) {",
        "                if (((List) object).isEmpty()) {\n"
        "                    // 空子列表视为 null，与 Spring Data 空值约定一致。\n",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 返回构造时注入的元素解码器。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
]

W11A_REPLACEMENTS["PointDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis GEO {@code GEOPOS} 等命令的经纬度对解码为 Spring {@link Point}。\n"
        " * <p>坐标分量使用 {@link DoubleCodec} 解析；空响应返回 {@code null}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 经纬度分量均按 {@code double} 解码。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
    (
        "        if (parts.isEmpty()) {",
        "        // 无坐标数据时返回 null。\n"
        "        if (parts.isEmpty()) {",
    ),
    (
        "    @Override\n    public Point decode(List<Object> parts, State state) {",
        "    /** 从 [经度, 纬度] 两元素列表构造 {@link Point}。 */\n"
        "    @Override\n"
        "    public Point decode(List<Object> parts, State state) {",
    ),
]

W11A_REPLACEMENTS["PropertiesDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis INFO/CONFIG 类 colon 分隔文本解码为 {@link Properties}。\n"
        " * <p>按行拆分 {@code key:value}，忽略格式非法行。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Properties decode(ByteBuf buf, State state) {",
        "    /** 从 Netty {@link ByteBuf} 读取 UTF-8 文本并解析为属性表。 */\n"
        "    @Override\n"
        "    public Properties decode(ByteBuf buf, State state) {",
    ),
    (
        "            if (second.charAt(second.length() - 1) == '\\r') {",
        "            // 去除 Windows 换行残留 \\r。\n"
        "            if (second.charAt(second.length() - 1) == '\\r') {",
    ),
]

W11A_REPLACEMENTS["PropertiesListDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis 扁平 key-value 列表响应解码为 {@link Properties}。\n"
        " * <p>按偶数下标为 key、奇数下标为 value 成对写入属性表。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Properties decode(List<Object> parts, State state) {",
        "    /** 步长为 2 遍历 parts，将 key/value 填入 {@link Properties}。 */\n"
        "    @Override\n"
        "    public Properties decode(List<Object> parts, State state) {",
    ),
]

W11A_REPLACEMENTS["RedisClusterNodeDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 解析 Redis {@code CLUSTER NODES} 文本响应为 {@link RedisClusterNode} 列表。\n"
        " * <p>提取 nodeId、地址、角色标志、master 关联、槽位范围与链路状态；\n"
        "空地址节点经 {@link ServiceManager#toURI} 规范化。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final ServiceManager serviceManager;",
        "    /** 用于将集群 bus 地址映射为客户端可连 URI。 */\n"
        "    private final ServiceManager serviceManager;",
    ),
    (
        "    public RedisClusterNodeDecoder(ServiceManager serviceManager) {",
        "    /** 注入 Redisson 连接管理器以解析节点地址。 */\n"
        "    public RedisClusterNodeDecoder(ServiceManager serviceManager) {",
    ),
    (
        "                if (name.isEmpty()) {\n                    // skip nodes with empty address",
        "                if (name.isEmpty()) {\n"
        "                    // 跳过无有效 host 的占位节点。\n"
        "                    // skip nodes with empty address",
    ),
    (
        "                    if (slots.indexOf(\"-<-\") != -1 || slots.indexOf(\"->-\") != -1) {",
        "                    // 忽略槽迁移中间态标记行。\n"
        "                    if (slots.indexOf(\"-<-\") != -1 || slots.indexOf(\"->-\") != -1) {",
    ),
    (
        "    @Override\n    public List<RedisClusterNode> decode(ByteBuf buf, State state) throws IOException {",
        "    /** 逐行解析 CLUSTER NODES 输出并组装 {@link RedisClusterNode}。 */\n"
        "    @Override\n"
        "    public List<RedisClusterNode> decode(ByteBuf buf, State state) throws IOException {",
    ),
]
